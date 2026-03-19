package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.dto.BannerDTO;
import com.melonecom.model.entity.Banner;
import com.melonecom.model.vo.BannerVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;

import java.util.List;

public interface IBannerService extends IService<Banner> {

    Result<PageResult<BannerVO>> getAllBanners(BannerDTO bannerDTO);

    Result addBanner(String bannerUrl, String jumpType, Long jumpTargetId);

    Result updateBanner(Long bannerId, String bannerUrl, String jumpType, Long jumpTargetId);

    Result updateBannerStatus(Long bannerId, Integer status);

    Result deleteBanner(Long bannerId);

    Result deleteBanners(List<Long> bannerIds);

    Result<List<BannerVO>> getBannerList();
}
