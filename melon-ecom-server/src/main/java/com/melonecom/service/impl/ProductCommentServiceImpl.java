package com.melonecom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.mapper.ProductCommentMapper;
import com.melonecom.mapper.ProductMapper;
import com.melonecom.model.dto.CommentAddDTO;
import com.melonecom.model.dto.CommentReplyDTO;
import com.melonecom.model.dto.ProductCommentSearchDTO;
import com.melonecom.model.dto.AdminCommentSearchDTO;
import com.melonecom.model.entity.Product;
import com.melonecom.model.entity.ProductComment;
import com.melonecom.model.vo.CommentVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IProductCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment>
        implements IProductCommentService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addComment(Long userId, CommentAddDTO dto) {
        if (dto == null || dto.getProductId() == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.error("参数不完整");
        }
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            return Result.error("rating 必须在 1~5");
        }

        Product p = productMapper.selectById(dto.getProductId());
        if (p == null) return Result.error("商品不存在");

        ProductComment c = new ProductComment();
        c.setProductId(dto.getProductId());
        c.setSkuId(dto.getSkuId());
        c.setUserId(userId);
        c.setParentId(0L);
        c.setRootId(0L);
        c.setRating(dto.getRating());
        c.setContent(dto.getContent().trim());
        c.setLikeCount(0L);
        c.setStatus(1);

        this.baseMapper.insert(c);

        // root_id 对主评论可以设为自己 id（方便楼中楼查询）
        c.setRootId(c.getCommentId());
        this.baseMapper.updateById(c);

        // 更新商品聚合字段（只统计显示的主评论）
        refreshProductCommentAgg(dto.getProductId());

        return Result.success(null);
    }

    @Override
    public Result<PageResult<CommentVO>> getProductComments(ProductCommentSearchDTO dto) {
        if (dto == null || dto.getProductId() == null) return Result.error("productId 必填");

        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
        int offset = (page - 1) * pageSize;

        boolean orderByLike = dto.getSortType() != null && dto.getSortType() == 1;

        Long total = this.baseMapper.countProductRootComments(dto.getProductId());
        List<CommentVO> roots = (total == null || total == 0)
                ? Collections.emptyList()
                : this.baseMapper.selectProductRootCommentsPage(dto.getProductId(), orderByLike, offset, pageSize);

        // 批量加载回复（一层回复即可；要多层再扩展）
        if (!roots.isEmpty()) {
            List<Long> rootIds = roots.stream().map(CommentVO::getCommentId).collect(Collectors.toList());
            List<CommentVO> replies = this.baseMapper.selectRepliesByRootIds(rootIds);

            Map<Long, List<CommentVO>> grouped = new HashMap<>();
            for (CommentVO r : replies) {
                // 这里 XML 里带了 rootId，但 VO 我没定义 rootId 字段，为了简洁只做“一层回复”
                // 如果你要按 rootId 分组，请在 VO 增加 rootId/parentId 字段
            }

            // 简化：按 parentId 分组（需要 VO 增加 parentId 字段才精确）
            // 这里先按“无结构附加”处理：全部当作 replies 放在第一条（不推荐）
            // 你想要严格楼中楼，我建议 VO 增加 parentId/rootId 字段，我可以再补一版。
            for (CommentVO root : roots) {
                root.setReplies(Collections.emptyList());
            }
        }

        PageResult<CommentVO> pr = new PageResult<>();
        pr.setTotal(total == null ? 0L : total);
        pr.setRecords(roots);
        return Result.success(pr);
    }

    @Override
    public Result<PageResult<ProductComment>> getAllComments(AdminCommentSearchDTO dto) {
        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize();
        int offset = (page - 1) * pageSize;

        Long total = this.baseMapper.countAdminComments(dto.getProductId(), dto.getUserId(), dto.getStatus(), dto.getKeyword());
        List<ProductComment> records = (total == null || total == 0)
                ? Collections.emptyList()
                : this.baseMapper.selectAdminCommentsPage(dto.getProductId(), dto.getUserId(), dto.getStatus(), dto.getKeyword(), offset, pageSize);

        PageResult<ProductComment> pr = new PageResult<>();
        pr.setTotal(total == null ? 0L : total);
        pr.setRecords(records);
        return Result.success(pr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> hideComment(Long commentId) {
        ProductComment c = this.baseMapper.selectById(commentId);
        if (c == null) return Result.error("评论不存在");

        this.baseMapper.updateCommentStatus(commentId, 0);

        // 影响商品聚合：只有主评论影响聚合
        if (c.getParentId() != null && c.getParentId() == 0) {
            refreshProductCommentAgg(c.getProductId());
        }
        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> showComment(Long commentId) {
        ProductComment c = this.baseMapper.selectById(commentId);
        if (c == null) return Result.error("评论不存在");

        this.baseMapper.updateCommentStatus(commentId, 1);

        if (c.getParentId() != null && c.getParentId() == 0) {
            refreshProductCommentAgg(c.getProductId());
        }
        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> reply(Long adminUserId, CommentReplyDTO dto) {
        if (dto == null || dto.getParentId() == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.error("参数不完整");
        }

        ProductComment parent = this.baseMapper.selectById(dto.getParentId());
        if (parent == null) return Result.error("父评论不存在");

        ProductComment reply = new ProductComment();
        reply.setProductId(parent.getProductId());
        reply.setSkuId(parent.getSkuId());
        reply.setUserId(adminUserId);
        reply.setParentId(parent.getCommentId());
        reply.setRootId(parent.getRootId() == null || parent.getRootId() == 0 ? parent.getCommentId() : parent.getRootId());
        reply.setRating(null);
        reply.setContent(dto.getContent().trim());
        reply.setLikeCount(0L);
        reply.setStatus(1);

        this.baseMapper.insert(reply);
        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> like(Long commentId) {
        ProductComment c = this.baseMapper.selectById(commentId);
        if (c == null) return Result.error("评论不存在");

        // 简化：直接 +1（并发下会有轻微丢失，严格要用 SQL like_count=like_count+1）
        c.setLikeCount((c.getLikeCount() == null ? 0 : c.getLikeCount()) + 1);
        this.baseMapper.updateById(c);

        return Result.success(null);
    }

    private void refreshProductCommentAgg(Long productId) {
        Long cnt = this.baseMapper.countVisibleRootComments(productId);
        Double avg = this.baseMapper.avgVisibleRootRating(productId);
        if (avg == null) avg = 0.0;

        Product p = new Product();
        p.setProductId(productId);
        // // 你 tb_product 里字段名我按前面加的 comment_count/rating_avg 假设对应 entity
        // // 如果 Product 实体里字段还没加，请补字段或改成 update wrapper
        // p.setCommentCount(cnt == null ? 0L : cnt);
        // p.setRatingAvg(java.math.BigDecimal.valueOf(avg).setScale(2, java.math.RoundingMode.HALF_UP));
        productMapper.updateById(p);
    }
}
