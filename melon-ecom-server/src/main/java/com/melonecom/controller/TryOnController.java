package com.melonecom.controller;

import com.melonecom.model.dto.TryOnCandidateSkuQueryDTO;
import com.melonecom.model.dto.TryOnCreateTaskDTO;
import com.melonecom.model.vo.TryOnCandidateSkuVO;
import com.melonecom.model.vo.TryOnPhotoUploadVO;
import com.melonecom.model.vo.TryOnTaskVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.ITryOnService;
import com.melonecom.util.ThreadLocalUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/tryon")
public class TryOnController {

    private final ITryOnService tryOnService;

    public TryOnController(ITryOnService tryOnService) {
        this.tryOnService = tryOnService;
    }

    private Long uid() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Object v = claims.get("userId");
        return v == null ? null : Long.valueOf(v.toString());
    }

    @PostMapping("/photos")
    public Result<TryOnPhotoUploadVO> uploadPhoto(@RequestParam("photoType") String photoType,
                                                  @RequestParam("file") MultipartFile file) {
        return tryOnService.uploadUserPhoto(uid(), photoType, file);
    }

    @GetMapping("/skus")
    public Result<PageResult<TryOnCandidateSkuVO>> getCandidateSkus(@RequestParam("slotType") String slotType,
                                                                    @RequestParam(value = "photoType", required = false) String photoType,
                                                                    @RequestParam(value = "keyword", required = false) String keyword,
                                                                    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                                    @RequestParam(value = "pageSize", defaultValue = "12") Integer pageSize) {
        TryOnCandidateSkuQueryDTO dto = new TryOnCandidateSkuQueryDTO();
        dto.setSlotType(slotType);
        dto.setPhotoType(photoType);
        dto.setKeyword(keyword);
        dto.setPage(pageNum);
        dto.setPageSize(pageSize);
        return tryOnService.getCandidateSkus(dto);
    }

    @PostMapping("/tasks")
    public Result<TryOnTaskVO> createTask(@RequestBody TryOnCreateTaskDTO dto) {
        return tryOnService.createTask(uid(), dto);
    }

    @GetMapping("/tasks/{taskId}")
    public Result<TryOnTaskVO> getTask(@PathVariable Long taskId) {
        return tryOnService.getTask(uid(), taskId);
    }
}
