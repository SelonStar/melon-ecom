package com.melonecom.service.impl;

import com.melonecom.constant.OrderConstant;
import com.melonecom.mapper.OrderItemMapper;
import com.melonecom.mapper.OrderMapper;
import com.melonecom.mapper.ProductMapper;
import com.melonecom.mapper.SkuMapper;
import com.melonecom.mapper.StockMapper;
import com.melonecom.mapper.UserMapper;
import com.melonecom.model.entity.Order;
import com.melonecom.model.entity.OrderItem;
import com.melonecom.model.vo.MockOrderSkuCandidateVO;
import com.melonecom.result.Result;
import com.melonecom.service.IMockOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MockOrderServiceImpl implements IMockOrderService {

    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final AtomicLong ORDER_SEQUENCE = new AtomicLong(0);

    private final UserMapper userMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StockMapper stockMapper;

    public MockOrderServiceImpl(UserMapper userMapper,
                                SkuMapper skuMapper,
                                ProductMapper productMapper,
                                OrderMapper orderMapper,
                                OrderItemMapper orderItemMapper,
                                StockMapper stockMapper) {
        this.userMapper = userMapper;
        this.skuMapper = skuMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.stockMapper = stockMapper;
    }

    /**
     * 该接口用于本地模拟订单数据，生成销量数据，为首页推荐和热销排序提供数据支撑。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> mockOrder(Integer count) {
        if (count == null || count <= 0) {
            return Result.error("count 必须大于 0");
        }

        List<Long> userIds = userMapper.selectEnabledUserIds();
        if (userIds == null || userIds.isEmpty()) {
            return Result.error("tb_user 中没有可用于模拟下单的启用用户");
        }

        List<MockOrderSkuCandidateVO> skuCandidates = skuMapper.selectMockOrderSkuCandidates();
        if (skuCandidates == null || skuCandidates.isEmpty()) {
            return Result.error("tb_sku/tb_stock 中没有可用于模拟下单的上架商品");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int successCount = 0;
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < count; i++) {
            if (skuCandidates.isEmpty()) {
                break;
            }

            Long userId = userIds.get(random.nextInt(userIds.size()));
            int candidateIndex = random.nextInt(skuCandidates.size());
            MockOrderSkuCandidateVO candidate = skuCandidates.get(candidateIndex);

            int maxQuantity = Math.min(3, Math.max(candidate.getAvailable(), 0));
            if (maxQuantity <= 0) {
                skuCandidates.remove(candidateIndex);
                i--;
                continue;
            }

            int quantity = random.nextInt(1, maxQuantity + 1);
            LocalDateTime now = LocalDateTime.now();
            BigDecimal itemAmount = candidate.getPrice().multiply(BigDecimal.valueOf(quantity));

            Order order = new Order();
            order.setOrderNo(generateMockOrderNo());
            order.setUserId(userId);
            order.setStatus(OrderConstant.PAID);
            order.setTotalAmount(itemAmount);
            order.setPayTime(now);
            orderMapper.insert(order);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setSkuId(candidate.getSkuId());
            orderItem.setWarehouseId(candidate.getWarehouseId());
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(candidate.getPrice());
            orderItem.setAmount(itemAmount);
            orderItemMapper.insert(orderItem);

            // 当前项目库存表使用 tb_stock.available；如果你的实际字段名是 stock，可将这里对应 SQL 改为 stock = stock - quantity。
            int stockRows = stockMapper.decreaseAvailableByWarehouseAndSku(
                    candidate.getWarehouseId(), candidate.getSkuId(), quantity);
            if (stockRows != 1) {
                throw new IllegalStateException("模拟下单扣库存失败，skuId=" + candidate.getSkuId());
            }

            // 当前项目将销量写入 tb_sku.sales_count，同时同步累加 tb_product.sales_count，方便首页热销按 SPU 排序。
            skuMapper.increaseSalesCount(candidate.getSkuId(), quantity);
            productMapper.increaseSalesCount(candidate.getProductId(), quantity);

            candidate.setAvailable(candidate.getAvailable() - quantity);
            if (candidate.getAvailable() <= 0) {
                skuCandidates.remove(candidateIndex);
            }

            successCount++;
            totalQuantity += quantity;
            totalAmount = totalAmount.add(itemAmount);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("requestedCount", count);
        data.put("successCount", successCount);
        data.put("failedCount", count - successCount);
        data.put("totalQuantity", totalQuantity);
        data.put("totalAmount", totalAmount);

        String message = successCount == count
                ? "批量模拟下单完成"
                : "批量模拟下单完成，部分请求因可售库存耗尽而跳过";
        return Result.success(message, data);
    }

    /**
     * 随机用户实现：预加载启用用户 ID 列表，然后按随机下标选取。
     * 随机商品实现：预加载上架且有库存的 SKU 列表，然后按随机下标选取。
     */
    private String generateMockOrderNo() {
        long sequence = ORDER_SEQUENCE.incrementAndGet();
        return "MOCK" + LocalDateTime.now().format(ORDER_NO_TIME_FORMATTER) + String.format("%06d", sequence);
    }
}
