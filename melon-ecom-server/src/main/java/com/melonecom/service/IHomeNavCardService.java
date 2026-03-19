package com.melonecom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.melonecom.model.dto.HomeNavCardSaveDTO;
import com.melonecom.model.entity.HomeNavCard;
import com.melonecom.model.vo.HomeNavCardVO;
import com.melonecom.result.Result;

import java.util.List;

public interface IHomeNavCardService extends IService<HomeNavCard> {

    Result<List<HomeNavCardVO>> getAdminConfig();

    Result<List<HomeNavCardVO>> getPublicConfig();

    Result<?> saveConfig(HomeNavCardSaveDTO dto);
}
