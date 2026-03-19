package com.melonecom.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melonecom.client.tryon.TryOnProviderClient;
import com.melonecom.constant.TryOnConstant;
import com.melonecom.mapper.SkuMapper;
import com.melonecom.mapper.TryOnTaskMapper;
import com.melonecom.model.dto.TryOnCandidateSkuQueryDTO;
import com.melonecom.model.dto.TryOnCreateTaskDTO;
import com.melonecom.model.dto.TryOnProviderQueryDTO;
import com.melonecom.model.dto.TryOnProviderSubmitDTO;
import com.melonecom.model.entity.Sku;
import com.melonecom.model.entity.TryOnTask;
import com.melonecom.model.vo.TryOnCandidateSkuVO;
import com.melonecom.model.vo.TryOnPhotoUploadVO;
import com.melonecom.model.vo.TryOnProviderQueryVO;
import com.melonecom.model.vo.TryOnProviderSubmitVO;
import com.melonecom.model.vo.TryOnTaskVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.ITryOnService;
import com.melonecom.service.MinioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TryOnServiceImpl implements ITryOnService {

    private final MinioService minioService;
    private final SkuMapper skuMapper;
    private final TryOnTaskMapper tryOnTaskMapper;
    private final ObjectMapper objectMapper;
    private final Map<String, TryOnProviderClient> providerClientMap;

    public TryOnServiceImpl(MinioService minioService,
                            SkuMapper skuMapper,
                            TryOnTaskMapper tryOnTaskMapper,
                            ObjectMapper objectMapper,
                            List<TryOnProviderClient> providerClients) {
        this.minioService = minioService;
        this.skuMapper = skuMapper;
        this.tryOnTaskMapper = tryOnTaskMapper;
        this.objectMapper = objectMapper;
        this.providerClientMap = providerClients.stream()
                .collect(Collectors.toMap(TryOnProviderClient::providerCode, Function.identity(), (a, b) -> a));
    }

    @Override
    public Result<TryOnPhotoUploadVO> uploadUserPhoto(Long userId, String photoType, MultipartFile file) {
        if (userId == null) return Result.error("未登录");
        if (file == null || file.isEmpty()) return Result.error("请上传试穿照片");
        if (!isValidPhotoType(photoType)) return Result.error("photoType 不合法");

        String url = minioService.uploadFile(file, "tryon/users");
        TryOnPhotoUploadVO vo = new TryOnPhotoUploadVO();
        vo.setPhotoUrl(url);
        vo.setPhotoType(photoType);
        vo.setOriginalFileName(file.getOriginalFilename());
        return Result.success(vo);
    }

    @Override
    public Result<PageResult<TryOnCandidateSkuVO>> getCandidateSkus(TryOnCandidateSkuQueryDTO dto) {
        if (dto == null || !isValidSlot(dto.getSlotType())) return Result.error("slotType 不合法");
        if (StringUtils.hasText(dto.getPhotoType()) && !isValidPhotoType(dto.getPhotoType())) {
            return Result.error("photoType 不合法");
        }
        if (TryOnConstant.PHOTO_TYPE_HALF_BODY.equals(dto.getPhotoType()) && !supportsHalfBody(dto.getSlotType())) {
            return Result.success(new PageResult<>(0L, Collections.emptyList()));
        }

        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 12 : Math.min(dto.getPageSize(), 48);
        int offset = (page - 1) * pageSize;

        Long total = skuMapper.countTryOnCandidateSkus(dto.getSlotType(), dto.getKeyword());
        List<TryOnCandidateSkuVO> records = total == 0
                ? Collections.emptyList()
                : skuMapper.selectTryOnCandidateSkus(dto.getSlotType(), dto.getKeyword(), offset, pageSize);
        return Result.success(new PageResult<>(total, records));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<TryOnTaskVO> createTask(Long userId, TryOnCreateTaskDTO dto) {
        if (userId == null) return Result.error("未登录");
        if (dto == null) return Result.error("请求参数不能为空");
        if (!StringUtils.hasText(dto.getPhotoUrl())) return Result.error("请先上传试穿照片");
        if (!isValidPhotoType(dto.getPhotoType())) return Result.error("photoType 不合法");

        String compatibilityError = validateCompatibility(dto);
        if (compatibilityError != null) return Result.error(compatibilityError);

        List<Long> skuIds = collectSkuIds(dto);
        if (skuIds.isEmpty()) return Result.error("请至少选择一件试穿商品");

        Map<Long, Sku> skuMap = skuMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(Sku::getSkuId, Function.identity(), (a, b) -> a));
        String skuValidationError = validateSelectedSkus(dto, skuMap);
        if (skuValidationError != null) return Result.error(skuValidationError);

        String providerCode = StringUtils.hasText(dto.getProviderCode())
                ? dto.getProviderCode()
                : TryOnConstant.PROVIDER_CODE_MOCK;
        TryOnProviderClient providerClient = providerClientMap.get(providerCode);
        if (providerClient == null) return Result.error("未找到可用的试穿服务商");

        TryOnTask task = new TryOnTask();
        task.setUserId(userId);
        task.setPhotoUrl(dto.getPhotoUrl());
        task.setPhotoType(dto.getPhotoType());
        task.setTopSkuId(dto.getTopSkuId());
        task.setBottomSkuId(dto.getBottomSkuId());
        task.setDressSkuId(dto.getDressSkuId());
        task.setShoesSkuId(dto.getShoesSkuId());
        task.setStatus(TryOnConstant.TASK_STATUS_SUBMITTED);
        task.setProviderCode(providerCode);
        task.setRequestSnapshot(writeJsonSafely(dto));
        tryOnTaskMapper.insert(task);

        TryOnProviderSubmitDTO providerSubmitDTO = buildProviderSubmitDTO(task, dto, skuMap, providerCode);
        TryOnProviderSubmitVO providerResult = providerClient.submitTask(providerSubmitDTO);

        task.setProviderTaskId(providerResult.getProviderTaskId());
        task.setStatus(StringUtils.hasText(providerResult.getTaskStatus())
                ? providerResult.getTaskStatus()
                : TryOnConstant.TASK_STATUS_PROCESSING);
        task.setResultImageUrl(providerResult.getResultImageUrl());
        task.setErrorMessage(providerResult.getErrorMessage());
        task.setProviderResponse(providerResult.getRawResponse());
        tryOnTaskMapper.updateById(task);

        return Result.success(toTaskVO(task));
    }

    @Override
    public Result<TryOnTaskVO> getTask(Long userId, Long taskId) {
        if (userId == null) return Result.error("未登录");
        if (taskId == null) return Result.error("taskId 不能为空");

        TryOnTask task = tryOnTaskMapper.selectById(taskId);
        if (task == null || !Objects.equals(task.getUserId(), userId)) return Result.error("试穿任务不存在");

        if ((TryOnConstant.TASK_STATUS_SUBMITTED.equals(task.getStatus())
                || TryOnConstant.TASK_STATUS_PROCESSING.equals(task.getStatus()))
                && StringUtils.hasText(task.getProviderTaskId())) {
            TryOnProviderClient providerClient = providerClientMap.get(task.getProviderCode());
            if (providerClient != null) {
                TryOnProviderQueryDTO queryDTO = new TryOnProviderQueryDTO();
                queryDTO.setProviderCode(task.getProviderCode());
                queryDTO.setProviderTaskId(task.getProviderTaskId());

                TryOnProviderQueryVO queryVO = providerClient.queryTask(queryDTO);
                if (queryVO != null && StringUtils.hasText(queryVO.getTaskStatus())) {
                    task.setStatus(queryVO.getTaskStatus());
                    if (StringUtils.hasText(queryVO.getResultImageUrl())) {
                        task.setResultImageUrl(queryVO.getResultImageUrl());
                    }
                    task.setErrorMessage(queryVO.getErrorMessage());
                    task.setProviderResponse(queryVO.getRawResponse());
                    tryOnTaskMapper.updateById(task);
                }
            }
        }

        return Result.success(toTaskVO(task));
    }

    private TryOnProviderSubmitDTO buildProviderSubmitDTO(TryOnTask task,
                                                          TryOnCreateTaskDTO dto,
                                                          Map<Long, Sku> skuMap,
                                                          String providerCode) {
        TryOnProviderSubmitDTO submitDTO = new TryOnProviderSubmitDTO();
        submitDTO.setTaskId(String.valueOf(task.getTaskId()));
        submitDTO.setProviderCode(providerCode);
        submitDTO.setPersonImageUrl(dto.getPhotoUrl());
        submitDTO.setPhotoType(dto.getPhotoType());
        submitDTO.setCurrentResultImageUrl(dto.getCurrentResultImageUrl());
        submitDTO.setTopImageUrl(resolveTryOnImage(skuMap.get(dto.getTopSkuId())));
        submitDTO.setBottomImageUrl(resolveTryOnImage(skuMap.get(dto.getBottomSkuId())));
        submitDTO.setDressImageUrl(resolveTryOnImage(skuMap.get(dto.getDressSkuId())));
        submitDTO.setShoesImageUrl(resolveTryOnImage(skuMap.get(dto.getShoesSkuId())));
        return submitDTO;
    }

    private String resolveTryOnImage(Sku sku) {
        if (sku == null) return null;
        if (StringUtils.hasText(sku.getTryonImageUrl())) return sku.getTryonImageUrl();
        return sku.getImageUrl();
    }

    private String validateCompatibility(TryOnCreateTaskDTO dto) {
        if (dto.getDressSkuId() != null && (dto.getTopSkuId() != null || dto.getBottomSkuId() != null)) {
            return "裙子与上装/下装不能同时选择";
        }
        if (TryOnConstant.PHOTO_TYPE_HALF_BODY.equals(dto.getPhotoType())) {
            if (dto.getBottomSkuId() != null || dto.getShoesSkuId() != null) {
                return "半身照暂不支持下装或鞋子试穿";
            }
        }
        return null;
    }

    private String validateSelectedSkus(TryOnCreateTaskDTO dto, Map<Long, Sku> skuMap) {
        if (dto.getTopSkuId() != null) {
            String error = validateSelectedSku(dto.getTopSkuId(), TryOnConstant.SLOT_TOP, skuMap);
            if (error != null) return error;
        }
        if (dto.getBottomSkuId() != null) {
            String error = validateSelectedSku(dto.getBottomSkuId(), TryOnConstant.SLOT_BOTTOM, skuMap);
            if (error != null) return error;
        }
        if (dto.getDressSkuId() != null) {
            String error = validateSelectedSku(dto.getDressSkuId(), TryOnConstant.SLOT_DRESS, skuMap);
            if (error != null) return error;
        }
        if (dto.getShoesSkuId() != null) {
            String error = validateSelectedSku(dto.getShoesSkuId(), TryOnConstant.SLOT_SHOES, skuMap);
            if (error != null) return error;
        }
        return null;
    }

    private String validateSelectedSku(Long skuId, String slotType, Map<Long, Sku> skuMap) {
        Sku sku = skuMap.get(skuId);
        if (sku == null || sku.getStatus() == null || sku.getStatus() == 0) {
            return "试穿商品不存在或已下架";
        }
        if (!Objects.equals(sku.getAiTryonEnabled(), 1)) {
            return "所选商品未开启 AI 试穿";
        }
        if (!slotType.equals(sku.getTryonCategory())) {
            return "所选商品与试穿槽位不匹配";
        }
        if (!StringUtils.hasText(resolveTryOnImage(sku))) {
            return "所选商品缺少试穿素材图";
        }
        return null;
    }

    private List<Long> collectSkuIds(TryOnCreateTaskDTO dto) {
        List<Long> skuIds = new ArrayList<>();
        if (dto.getTopSkuId() != null) skuIds.add(dto.getTopSkuId());
        if (dto.getBottomSkuId() != null) skuIds.add(dto.getBottomSkuId());
        if (dto.getDressSkuId() != null) skuIds.add(dto.getDressSkuId());
        if (dto.getShoesSkuId() != null) skuIds.add(dto.getShoesSkuId());
        return skuIds;
    }

    private TryOnTaskVO toTaskVO(TryOnTask task) {
        TryOnTaskVO vo = new TryOnTaskVO();
        vo.setTaskId(task.getTaskId());
        vo.setStatus(task.getStatus());
        vo.setPhotoUrl(task.getPhotoUrl());
        vo.setPhotoType(task.getPhotoType());
        vo.setTopSkuId(task.getTopSkuId());
        vo.setBottomSkuId(task.getBottomSkuId());
        vo.setDressSkuId(task.getDressSkuId());
        vo.setShoesSkuId(task.getShoesSkuId());
        vo.setProviderCode(task.getProviderCode());
        vo.setProviderTaskId(task.getProviderTaskId());
        vo.setResultImageUrl(task.getResultImageUrl());
        vo.setErrorMessage(task.getErrorMessage());
        return vo;
    }

    private boolean isValidPhotoType(String photoType) {
        return TryOnConstant.PHOTO_TYPE_FULL_BODY.equals(photoType)
                || TryOnConstant.PHOTO_TYPE_HALF_BODY.equals(photoType);
    }

    private boolean isValidSlot(String slotType) {
        return TryOnConstant.SLOT_TOP.equals(slotType)
                || TryOnConstant.SLOT_BOTTOM.equals(slotType)
                || TryOnConstant.SLOT_DRESS.equals(slotType)
                || TryOnConstant.SLOT_SHOES.equals(slotType);
    }

    private boolean supportsHalfBody(String slotType) {
        return TryOnConstant.SLOT_TOP.equals(slotType) || TryOnConstant.SLOT_DRESS.equals(slotType);
    }

    private String writeJsonSafely(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
