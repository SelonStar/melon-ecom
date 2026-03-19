package com.melonecom.controller;

import com.melonecom.model.dto.BannerDTO;
import com.melonecom.model.vo.BannerVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IBannerService;
import com.melonecom.service.MinioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class BannerController {

    private final IBannerService bannerService;
    private final MinioService minioService;

    public BannerController(IBannerService bannerService, MinioService minioService) {
        this.bannerService = bannerService;
        this.minioService = minioService;
    }

    @PostMapping("/admin/getAllBanners")
    public Result<PageResult<BannerVO>> getAllBanners(@RequestBody BannerDTO bannerDTO) {
        return bannerService.getAllBanners(bannerDTO);
    }

    @PostMapping("/admin/addBanner")
    public Result addBanner(@RequestParam("banner") MultipartFile banner,
                            @RequestParam(value = "jumpType", defaultValue = "NONE") String jumpType,
                            @RequestParam(value = "jumpTargetId", required = false) Long jumpTargetId) {
        String bannerUrl = minioService.uploadFile(banner, "banners");
        return bannerService.addBanner(bannerUrl, jumpType, jumpTargetId);
    }

    @PatchMapping("/admin/updateBanner/{id}")
    public Result updateBanner(@PathVariable("id") Long bannerId,
                               @RequestParam(value = "banner", required = false) MultipartFile banner,
                               @RequestParam(value = "jumpType", defaultValue = "NONE") String jumpType,
                               @RequestParam(value = "jumpTargetId", required = false) Long jumpTargetId) {
        String bannerUrl = null;
        if (banner != null && !banner.isEmpty()) {
            bannerUrl = minioService.uploadFile(banner, "banners");
        }
        return bannerService.updateBanner(bannerId, bannerUrl, jumpType, jumpTargetId);
    }

    @PatchMapping("/admin/updateBannerStatus/{id}")
    public Result updateBannerStatus(@PathVariable("id") Long bannerId,
                                     @RequestParam("status") Integer status) {
        return bannerService.updateBannerStatus(bannerId, status);
    }

    @DeleteMapping("/admin/deleteBanner/{id}")
    public Result deleteBanner(@PathVariable("id") Long bannerId) {
        return bannerService.deleteBanner(bannerId);
    }

    @DeleteMapping("/admin/deleteBanners")
    public Result deleteBanners(@RequestBody List<Long> bannerIds) {
        return bannerService.deleteBanners(bannerIds);
    }

    @GetMapping("/banner/getBannerList")
    public Result<List<BannerVO>> getBannerList() {
        return bannerService.getBannerList();
    }
}
