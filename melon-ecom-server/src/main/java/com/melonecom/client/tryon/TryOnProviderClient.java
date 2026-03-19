package com.melonecom.client.tryon;

import com.melonecom.model.dto.TryOnProviderQueryDTO;
import com.melonecom.model.dto.TryOnProviderSubmitDTO;
import com.melonecom.model.vo.TryOnProviderQueryVO;
import com.melonecom.model.vo.TryOnProviderSubmitVO;

public interface TryOnProviderClient {
    String providerCode();

    TryOnProviderSubmitVO submitTask(TryOnProviderSubmitDTO dto);

    TryOnProviderQueryVO queryTask(TryOnProviderQueryDTO dto);
}
