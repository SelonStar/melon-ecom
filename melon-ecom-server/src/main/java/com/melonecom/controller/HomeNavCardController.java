package com.melonecom.controller;

import com.melonecom.model.dto.HomeNavCardSaveDTO;
import com.melonecom.model.vo.HomeNavCardVO;
import com.melonecom.result.Result;
import com.melonecom.service.IHomeNavCardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeNavCardController {

    private final IHomeNavCardService homeNavCardService;

    public HomeNavCardController(IHomeNavCardService homeNavCardService) {
        this.homeNavCardService = homeNavCardService;
    }

    @GetMapping("/admin/homeNavCards")
    public Result<List<HomeNavCardVO>> getAdminConfig() {
        return homeNavCardService.getAdminConfig();
    }

    @PutMapping("/admin/homeNavCards")
    public Result<?> saveConfig(@RequestBody HomeNavCardSaveDTO dto) {
        return homeNavCardService.saveConfig(dto);
    }

    @GetMapping("/home/navCards")
    public Result<List<HomeNavCardVO>> getPublicConfig() {
        return homeNavCardService.getPublicConfig();
    }
}
