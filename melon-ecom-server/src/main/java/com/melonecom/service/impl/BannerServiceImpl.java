package com.melonecom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.constant.MessageConstant;
import com.melonecom.mapper.BannerMapper;
import com.melonecom.mapper.CategoryMapper;
import com.melonecom.mapper.ProductMapper;
import com.melonecom.model.dto.BannerDTO;
import com.melonecom.model.entity.Banner;
import com.melonecom.model.entity.Category;
import com.melonecom.model.entity.Product;
import com.melonecom.model.vo.BannerVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IBannerService;
import com.melonecom.service.MinioService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "bannerCache")
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService {

    private static final String LEGACY_CATEGORY_NAME = "图书音像";
    private static final String TARGET_CATEGORY_NAME = "时尚潮玩";

    private final BannerMapper bannerMapper;
    private final MinioService minioService;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    public BannerServiceImpl(BannerMapper bannerMapper, MinioService minioService,
                             ProductMapper productMapper, CategoryMapper categoryMapper) {
        this.bannerMapper = bannerMapper;
        this.minioService = minioService;
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Cacheable(key = "'adminPage:' + #bannerDTO.pageNum + ':' + #bannerDTO.pageSize + ':' + #bannerDTO.status")
    public Result<PageResult<BannerVO>> getAllBanners(BannerDTO bannerDTO) {
        Page<Banner> page = new Page<>(bannerDTO.getPageNum(), bannerDTO.getPageSize());

        LambdaQueryWrapper<Banner> qw = new LambdaQueryWrapper<>();
        if (bannerDTO.getStatus() != null) {
            qw.eq(Banner::getStatus, bannerDTO.getStatus());
        }
        qw.orderByDesc(Banner::getBannerId);

        IPage<Banner> bannerPage = bannerMapper.selectPage(page, qw);

        List<BannerVO> voList = bannerPage.getRecords().stream().map(b -> {
            BannerVO vo = new BannerVO();
            BeanUtils.copyProperties(b, vo);
            JumpConfig jumpConfig = resolveJumpConfig(b.getJumpType(), b.getJumpTargetId(), b.getLinkUrl());
            vo.setJumpType(jumpConfig.jumpType());
            vo.setJumpTargetId(jumpConfig.jumpTargetId());
            vo.setJumpTargetName(resolveJumpTargetName(jumpConfig.jumpType(), jumpConfig.jumpTargetId()));
            return vo;
        }).toList();

        return Result.success(new PageResult<>(bannerPage.getTotal(), voList));
    }

    @Override
    @CacheEvict(allEntries = true)
    public Result addBanner(String bannerUrl, String jumpType, Long jumpTargetId) {
        String resolvedJumpType = normalizeJumpType(jumpType);
        Long resolvedJumpTargetId = "NONE".equals(resolvedJumpType) ? null : jumpTargetId;

        Banner banner = new Banner();
        banner.setBannerUrl(bannerUrl);
        // Persist by link_url for compatibility with older schema.
        banner.setLinkUrl(buildLinkUrl(resolvedJumpType, resolvedJumpTargetId));
        banner.setStatus(1);
        banner.setSort(0);

        if (bannerMapper.insert(banner) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    @Override
    @CacheEvict(allEntries = true)
    public Result updateBanner(Long bannerId, String bannerUrl, String jumpType, Long jumpTargetId) {
        Banner banner = bannerMapper.selectById(bannerId);
        if (banner == null) {
            return Result.error(MessageConstant.DATA_NOT_FOUND);
        }

        if (bannerUrl != null) {
            String oldUrl = banner.getBannerUrl();
            if (oldUrl != null && !oldUrl.isEmpty()) {
                minioService.deleteFile(oldUrl);
            }
            banner.setBannerUrl(bannerUrl);
        }

        // 用 UpdateWrapper 显式写入所有字段（含 null），避免 updateById 跳过 null 值
        String resolvedJumpType = normalizeJumpType(jumpType);
        Long resolvedJumpTargetId = "NONE".equals(resolvedJumpType) ? null : jumpTargetId;
        LambdaUpdateWrapper<Banner> uw = new LambdaUpdateWrapper<Banner>()
                .eq(Banner::getBannerId, bannerId)
                .set(Banner::getBannerUrl, banner.getBannerUrl())
                .set(Banner::getLinkUrl, buildLinkUrl(resolvedJumpType, resolvedJumpTargetId));

        if (bannerMapper.update(null, uw) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    @CacheEvict(allEntries = true)
    public Result updateBannerStatus(Long bannerId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(MessageConstant.BANNER_STATUS_INVALID);
        }

        Banner banner = new Banner();
        banner.setBannerId(bannerId);
        banner.setStatus(status);

        if (bannerMapper.updateById(banner) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    @CacheEvict(allEntries = true)
    public Result deleteBanner(Long bannerId) {
        Banner banner = bannerMapper.selectById(bannerId);
        if (banner == null) {
            return Result.error(MessageConstant.DATA_NOT_FOUND);
        }

        String url = banner.getBannerUrl();
        if (url != null && !url.isEmpty()) {
            minioService.deleteFile(url);
        }

        if (bannerMapper.deleteById(bannerId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    @CacheEvict(allEntries = true)
    public Result deleteBanners(List<Long> bannerIds) {
        if (bannerIds == null || bannerIds.isEmpty()) {
            return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
        }

        List<Banner> banners = bannerMapper.selectBatchIds(bannerIds);
        for (Banner b : banners) {
            String url = b.getBannerUrl();
            if (url != null && !url.isEmpty()) {
                minioService.deleteFile(url);
            }
        }

        int deleted = bannerMapper.deleteBatchIds(bannerIds);
        if (deleted <= 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    public Result<List<BannerVO>> getBannerList() {
        List<Banner> banners = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSort)
                        .orderByDesc(Banner::getBannerId)
                        .last("limit 9")
        );

        List<BannerVO> voList = banners.stream().map(b -> {
            JumpConfig jumpConfig = resolveJumpConfig(b.getJumpType(), b.getJumpTargetId(), b.getLinkUrl());
            BannerVO vo = new BannerVO();
            vo.setBannerId(b.getBannerId());
            vo.setTitle(b.getTitle());
            vo.setBannerUrl(b.getBannerUrl());
            vo.setLinkUrl(b.getLinkUrl());
            vo.setJumpType(jumpConfig.jumpType());
            vo.setJumpTargetId(jumpConfig.jumpTargetId());
            return vo;
        }).toList();

        return Result.success(voList);
    }

    // ===== 工具方法 =====

    private record JumpConfig(String jumpType, Long jumpTargetId) {
    }

    private String normalizeJumpType(String jumpType) {
        if (jumpType == null) return "NONE";
        String normalized = jumpType.trim().toUpperCase();
        if ("PRODUCT".equals(normalized) || "CATEGORY".equals(normalized)) {
            return normalized;
        }
        return "NONE";
    }

    private JumpConfig resolveJumpConfig(String jumpType, Long jumpTargetId, String linkUrl) {
        String normalizedJumpType = normalizeJumpType(jumpType);
        if (!"NONE".equals(normalizedJumpType)) {
            return new JumpConfig(normalizedJumpType, jumpTargetId);
        }
        return parseJumpConfigFromLinkUrl(linkUrl);
    }

    private JumpConfig parseJumpConfigFromLinkUrl(String linkUrl) {
        if (linkUrl == null || linkUrl.isBlank()) {
            return new JumpConfig("NONE", null);
        }
        if (linkUrl.startsWith("/product/")) {
            return new JumpConfig("PRODUCT", parseTailId(linkUrl, "/product/"));
        }
        if (linkUrl.startsWith("/category/")) {
            return new JumpConfig("CATEGORY", parseTailId(linkUrl, "/category/"));
        }
        return new JumpConfig("NONE", null);
    }

    private Long parseTailId(String linkUrl, String prefix) {
        String tail = linkUrl.substring(prefix.length()).trim();
        if (tail.isEmpty()) return null;
        try {
            return Long.parseLong(tail);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String buildLinkUrl(String jumpType, Long jumpTargetId) {
        String normalizedJumpType = normalizeJumpType(jumpType);
        if (jumpTargetId == null || "NONE".equals(normalizedJumpType)) return null;
        if ("PRODUCT".equals(normalizedJumpType)) return "/product/" + jumpTargetId;
        if ("CATEGORY".equals(normalizedJumpType)) return "/category/" + jumpTargetId;
        return null;
    }

    private String resolveJumpTargetName(String jumpType, Long jumpTargetId) {
        String normalizedJumpType = normalizeJumpType(jumpType);
        if (jumpTargetId == null || "NONE".equals(normalizedJumpType)) return null;
        if ("PRODUCT".equals(normalizedJumpType)) {
            Product p = productMapper.selectById(jumpTargetId);
            return p != null ? p.getName() : String.valueOf(jumpTargetId);
        }
        if ("CATEGORY".equals(normalizedJumpType)) {
            Category c = categoryMapper.selectById(jumpTargetId);
            return c != null ? normalizeCategoryName(c.getName()) : String.valueOf(jumpTargetId);
        }
        return null;
    }

    private String normalizeCategoryName(String name) {
        if (LEGACY_CATEGORY_NAME.equals(name)) {
            return TARGET_CATEGORY_NAME;
        }
        return name;
    }
}
