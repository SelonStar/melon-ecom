package com.melonecom.client.tryon.impl;

import com.melonecom.client.tryon.TryOnProviderClient;
import com.melonecom.constant.TryOnConstant;
import com.melonecom.model.dto.TryOnProviderQueryDTO;
import com.melonecom.model.dto.TryOnProviderSubmitDTO;
import com.melonecom.model.vo.TryOnProviderQueryVO;
import com.melonecom.model.vo.TryOnProviderSubmitVO;
import org.springframework.stereotype.Component;

@Component
public class MockTryOnProviderClient implements TryOnProviderClient {

    @Override
    public String providerCode() {
        return TryOnConstant.PROVIDER_CODE_MOCK;
    }

    @Override
    public TryOnProviderSubmitVO submitTask(TryOnProviderSubmitDTO dto) {
        TryOnProviderSubmitVO vo = new TryOnProviderSubmitVO();
        vo.setProviderTaskId("mock-" + System.currentTimeMillis());
        vo.setTaskStatus(TryOnConstant.TASK_STATUS_SUCCESS);
        vo.setResultImageUrl(dto.getPersonImageUrl());
        vo.setRawResponse("{\"provider\":\"mock\",\"mode\":\"sync\"}");
        return vo;
    }

    @Override
    public TryOnProviderQueryVO queryTask(TryOnProviderQueryDTO dto) {
        TryOnProviderQueryVO vo = new TryOnProviderQueryVO();
        vo.setProviderTaskId(dto.getProviderTaskId());
        vo.setTaskStatus(TryOnConstant.TASK_STATUS_SUCCESS);
        vo.setRawResponse("{\"provider\":\"mock\",\"mode\":\"query\"}");
        return vo;
    }
}
