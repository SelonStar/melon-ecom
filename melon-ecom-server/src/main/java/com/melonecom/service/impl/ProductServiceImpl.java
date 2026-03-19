package com.melonecom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.melonecom.mapper.ProductImageMapper;
import com.melonecom.mapper.ProductMapper;
import com.melonecom.mapper.SkuMapper;
import com.melonecom.mapper.StockMapper;
import com.melonecom.mapper.WarehouseMapper;
import com.melonecom.model.dto.ProductAddDTO;
import com.melonecom.model.dto.ProductSearchDTO;
import com.melonecom.model.dto.ProductUpdateDTO;
import com.melonecom.model.dto.SkuAddDTO;
import com.melonecom.model.dto.SkuUpdateDTO;
import com.melonecom.model.entity.Product;
import com.melonecom.model.entity.ProductImage;
import com.melonecom.model.entity.Sku;
import com.melonecom.model.entity.Stock;
import com.melonecom.model.entity.Warehouse;
import com.melonecom.model.vo.ProductAdminDetailVO;
import com.melonecom.model.vo.ProductAdminVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired private ProductMapper productMapper;
    @Autowired private SkuMapper skuMapper;
    @Autowired private ProductImageMapper productImageMapper;
    @Autowired private StockMapper stockMapper;
    @Autowired private WarehouseMapper warehouseMapper;

    /** 获取或创建默认仓库 ID */
    private Long getDefaultWarehouseId() {
        Warehouse wh = warehouseMapper.selectOne(
                new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getCode, "WH_DEFAULT").last("LIMIT 1"));
        if (wh != null) return wh.getWarehouseId();
        // 首次使用时自动创建
        Warehouse newWh = new Warehouse();
        newWh.setCode("WH_DEFAULT");
        newWh.setName("默认仓库");
        newWh.setProvince("广东省");
        newWh.setCity("广州市");
        newWh.setDistrict("天河区");
        newWh.setAddressDetail("默认仓库");
        newWh.setStatus(1);
        warehouseMapper.insert(newWh);
        return newWh.getWarehouseId();
    }

    /** SKU 插入后同步写/更新 tb_stock */
    private void upsertStock(Long skuId, Integer stockQty) {
        Long whId = getDefaultWarehouseId();
        Stock exist = stockMapper.selectByWarehouseAndSku(whId, skuId);
        if (exist == null) {
            Stock s = new Stock();
            s.setWarehouseId(whId);
            s.setSkuId(skuId);
            s.setAvailable(stockQty != null ? stockQty : 0);
            s.setLocked(0);
            s.setVersion(0);
            stockMapper.insert(s);
        } else {
            // 仅更新可售库存（不改 locked 和 version）
            exist.setAvailable(stockQty != null ? stockQty : 0);
            stockMapper.updateById(exist);
        }
    }

    @Override
    public Result<Long> getAllProductsCount(Integer status, Long categoryId, Long brandId, String keyword) {
        Long cnt = productMapper.countProducts(status, categoryId, brandId, splitKeywordList(keyword));
        return Result.success(cnt);
    }

    @Override
    public Result<PageResult<ProductAdminVO>> getAllProducts(ProductSearchDTO dto) {
        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
        int offset = (page - 1) * pageSize;
        List<String> keywordList = splitKeywordList(dto.getKeyword());

        Long total = productMapper.countProducts(dto.getStatus(), dto.getCategoryId(), dto.getBrandId(), keywordList);
        List<ProductAdminVO> records = total == 0
                ? new ArrayList<>()
                : productMapper.selectProductsPage(dto.getStatus(), dto.getCategoryId(), dto.getBrandId(), keywordList, offset, pageSize);

        // Fallback for old data: use first detail image when main image is empty.
        for (ProductAdminVO record : records) {
            if (record.getMainImageUrl() == null || record.getMainImageUrl().isBlank()) {
                record.setMainImageUrl(pickFirstImage(record.getProductId()));
            }
        }

        PageResult<ProductAdminVO> pr = new PageResult<>();
        pr.setTotal(total);
        pr.setRecords(records);
        return Result.success(pr);
    }

    @Override
    public Result<ProductAdminDetailVO> getProductDetail(Long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null) return Result.error("product not found");

        ProductAdminDetailVO vo = new ProductAdminDetailVO();
        vo.setProductId(p.getProductId());
        vo.setName(p.getName());
        vo.setSubTitle(p.getSubTitle());
        vo.setBrandId(p.getBrandId());
        vo.setCategoryId(p.getCategoryId());
        vo.setMainImageUrl(p.getMainImageUrl());
        vo.setDetailHtml(p.getDetailHtml());
        vo.setStatus(p.getStatus());

        vo.setImageUrls(productImageMapper.selectImageUrls(productId));

        // 用 tb_stock.available 覆盖 sku.stock，让客户端拿到真实可售库存
        List<Sku> skus = skuMapper.selectSkusByProductId(productId);
        for (Sku sku : skus) {
            Long whId = stockMapper.selectWarehouseIdBySkuId(sku.getSkuId());
            if (whId != null) {
                Stock stock = stockMapper.selectByWarehouseAndSku(whId, sku.getSkuId());
                if (stock != null) sku.setStock(stock.getAvailable());
            }
        }
        vo.setSkus(skus);
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addProduct(ProductAddDTO dto) {
        String normalizedMainImage = normalizeMainImageUrl(dto.getMainImageUrl(), dto.getImageUrls());
        if (normalizedMainImage == null || normalizedMainImage.isBlank()) {
            return Result.error("mainImageUrl cannot be empty");
        }

        Product p = new Product();
        p.setName(dto.getName());
        p.setSubTitle(dto.getSubTitle());
        p.setBrandId(dto.getBrandId());
        p.setCategoryId(dto.getCategoryId());
        p.setMainImageUrl(normalizedMainImage);
        p.setDetailHtml(dto.getDetailHtml());
        p.setStatus(1);

        productMapper.insert(p);
        Long productId = p.getProductId();

        if (dto.getImageUrls() != null) {
            int sort = 0;
            for (String url : dto.getImageUrls()) {
                if (url == null || url.isBlank()) continue;
                ProductImage img = new ProductImage();
                img.setProductId(productId);
                img.setUrl(url);
                img.setSort(sort++);
                productImageMapper.insert(img);
            }
        }

        if (dto.getSkus() != null) {
            for (SkuAddDTO s : dto.getSkus()) {
                s.setProductId(productId);
                addSku(s);
            }
        }

        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> updateProduct(ProductUpdateDTO dto) {
        Product p = productMapper.selectById(dto.getProductId());
        if (p == null) return Result.error("product not found");

        String normalizedMainImage = normalizeMainImageUrl(dto.getMainImageUrl(), dto.getImageUrls());
        if (normalizedMainImage == null || normalizedMainImage.isBlank()) {
            normalizedMainImage = p.getMainImageUrl();
        }
        if (normalizedMainImage == null || normalizedMainImage.isBlank()) {
            return Result.error("mainImageUrl cannot be empty");
        }

        p.setName(dto.getName());
        p.setSubTitle(dto.getSubTitle());
        p.setBrandId(dto.getBrandId());
        p.setCategoryId(dto.getCategoryId());
        p.setMainImageUrl(normalizedMainImage);
        p.setDetailHtml(dto.getDetailHtml());
        if (dto.getStatus() != null) p.setStatus(dto.getStatus());

        productMapper.updateById(p);

        if (dto.getImageUrls() != null) {
            productImageMapper.deleteByProductId(dto.getProductId());
            int sort = 0;
            for (String url : dto.getImageUrls()) {
                if (url == null || url.isBlank()) continue;
                ProductImage img = new ProductImage();
                img.setProductId(dto.getProductId());
                img.setUrl(url);
                img.setSort(sort++);
                productImageMapper.insert(img);
            }
        }

        if (dto.getSkus() != null) {
            skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getProductId, dto.getProductId()));
            for (SkuAddDTO s : dto.getSkus()) {
                s.setProductId(dto.getProductId());
                if (s.getSkuCode() == null || s.getSkuCode().isBlank()) {
                    s.setSkuCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
                }
                Sku sku = new Sku();
                sku.setProductId(s.getProductId());
                sku.setSkuCode(s.getSkuCode());
                sku.setName(s.getName());
                sku.setSpecJson(s.getSpecJson());
                sku.setPrice(s.getPrice());
                sku.setCostPrice(s.getCostPrice());
                sku.setStock(s.getStock() != null ? s.getStock() : 0);
                sku.setImageUrl(s.getImageUrl());
                sku.setAiTryonEnabled(s.getAiTryonEnabled() != null ? s.getAiTryonEnabled() : 0);
                sku.setTryonCategory(s.getTryonCategory());
                sku.setTryonImageUrl(s.getTryonImageUrl());
                sku.setTryonMaskUrl(s.getTryonMaskUrl());
                sku.setTryonSort(s.getTryonSort() != null ? s.getTryonSort() : 0);
                sku.setWeight(s.getWeight());
                sku.setStatus(s.getStatus() != null ? s.getStatus() : 1);
                skuMapper.insert(sku);
                upsertStock(sku.getSkuId(), sku.getStock());
            }
        }

        return Result.success(null);
    }

    @Override
    public Result<?> updateProductStatus(Long productId, Integer status) {
        Product p = productMapper.selectById(productId);
        if (p == null) return Result.error("product not found");
        p.setStatus(status);
        productMapper.updateById(p);
        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteProduct(Long productId) {
        productImageMapper.deleteByProductId(productId);
        skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getProductId, productId));
        productMapper.deleteById(productId);
        return Result.success(null);
    }

    @Override
    public Result<?> addSku(SkuAddDTO dto) {
        if (dto.getProductId() == null) return Result.error("productId cannot be null");
        Product p = productMapper.selectById(dto.getProductId());
        if (p == null) return Result.error("product not found");

        if (dto.getSkuCode() != null && !dto.getSkuCode().isBlank()) {
            Integer exists = skuMapper.existsSkuCode(dto.getSkuCode(), null);
            if (exists != null && exists > 0) return Result.error("skuCode already exists");
        } else {
            dto.setSkuCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }

        Sku sku = new Sku();
        sku.setProductId(dto.getProductId());
        sku.setSkuCode(dto.getSkuCode());
        sku.setName(dto.getName());
        sku.setSpecJson(dto.getSpecJson());
        sku.setPrice(dto.getPrice());
        sku.setCostPrice(dto.getCostPrice());
        sku.setStock(dto.getStock() != null ? dto.getStock() : 0);
        sku.setImageUrl(dto.getImageUrl());
        sku.setAiTryonEnabled(dto.getAiTryonEnabled() != null ? dto.getAiTryonEnabled() : 0);
        sku.setTryonCategory(dto.getTryonCategory());
        sku.setTryonImageUrl(dto.getTryonImageUrl());
        sku.setTryonMaskUrl(dto.getTryonMaskUrl());
        sku.setTryonSort(dto.getTryonSort() != null ? dto.getTryonSort() : 0);
        sku.setWeight(dto.getWeight());
        sku.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        skuMapper.insert(sku);
        upsertStock(sku.getSkuId(), sku.getStock());
        return Result.success(null);
    }

    @Override
    public Result<?> updateSku(SkuUpdateDTO dto) {
        Sku sku = skuMapper.selectById(dto.getSkuId());
        if (sku == null) return Result.error("sku not found");

        sku.setName(dto.getName());
        sku.setSpecJson(dto.getSpecJson());
        sku.setPrice(dto.getPrice());
        sku.setCostPrice(dto.getCostPrice());
        if (dto.getStock() != null) sku.setStock(dto.getStock());
        if (dto.getImageUrl() != null) sku.setImageUrl(dto.getImageUrl());
        if (dto.getAiTryonEnabled() != null) sku.setAiTryonEnabled(dto.getAiTryonEnabled());
        sku.setTryonCategory(dto.getTryonCategory());
        if (dto.getTryonImageUrl() != null) sku.setTryonImageUrl(dto.getTryonImageUrl());
        if (dto.getTryonMaskUrl() != null) sku.setTryonMaskUrl(dto.getTryonMaskUrl());
        if (dto.getTryonSort() != null) sku.setTryonSort(dto.getTryonSort());
        sku.setWeight(dto.getWeight());
        if (dto.getStatus() != null) sku.setStatus(dto.getStatus());

        skuMapper.updateById(sku);
        upsertStock(sku.getSkuId(), sku.getStock());
        return Result.success(null);
    }

    @Override
    public Result<?> updateSkuStatus(Long skuId, Integer status) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) return Result.error("sku not found");
        sku.setStatus(status);
        skuMapper.updateById(sku);
        return Result.success(null);
    }

    @Override
    public Result<?> deleteSku(Long skuId) {
        skuMapper.deleteById(skuId);
        return Result.success(null);
    }

    private String normalizeMainImageUrl(String mainImageUrl, List<String> imageUrls) {
        if (mainImageUrl != null && !mainImageUrl.isBlank()) {
            return mainImageUrl;
        }
        if (imageUrls == null || imageUrls.isEmpty()) {
            return mainImageUrl;
        }
        for (String url : imageUrls) {
            if (url != null && !url.isBlank()) {
                return url;
            }
        }
        return mainImageUrl;
    }

    private String pickFirstImage(Long productId) {
        if (productId == null) return null;
        List<String> imageUrls = productImageMapper.selectImageUrls(productId);
        if (imageUrls == null || imageUrls.isEmpty()) return null;
        for (String url : imageUrls) {
            if (url != null && !url.isBlank()) {
                return url;
            }
        }
        return null;
    }

    private List<String> splitKeywordList(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }

        String[] rawParts = keyword.split("[,，;；|\\r\\n]+");
        Set<String> normalized = new LinkedHashSet<>();
        for (String rawPart : rawParts) {
            if (!StringUtils.hasText(rawPart)) {
                continue;
            }
            normalized.add(rawPart.trim());
        }

        if (normalized.isEmpty() && StringUtils.hasText(keyword)) {
            normalized.add(keyword.trim());
        }

        return new ArrayList<>(normalized);
    }
}
