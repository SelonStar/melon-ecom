package com.melonecom.service;

import com.melonecom.model.dto.TryOnCandidateSkuQueryDTO;
import com.melonecom.model.dto.TryOnCreateTaskDTO;
import com.melonecom.model.vo.TryOnCandidateSkuVO;
import com.melonecom.model.vo.TryOnPhotoUploadVO;
import com.melonecom.model.vo.TryOnTaskVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface ITryOnService {
    Result<TryOnPhotoUploadVO> uploadUserPhoto(Long userId, String photoType, MultipartFile file);

    Result<PageResult<TryOnCandidateSkuVO>> getCandidateSkus(TryOnCandidateSkuQueryDTO dto);

    Result<TryOnTaskVO> createTask(Long userId, TryOnCreateTaskDTO dto);

    Result<TryOnTaskVO> getTask(Long userId, Long taskId);
}
