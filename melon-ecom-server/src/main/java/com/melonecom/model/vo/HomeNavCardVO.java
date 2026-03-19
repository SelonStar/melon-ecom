package com.melonecom.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class HomeNavCardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long cardId;
    private String title;
    private Integer sort;
    private List<ItemVO> items;

    @Data
    public static class ItemVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long itemId;
        private String keyword;
        private String title;
        private String imageUrl;
        private Integer sort;
    }
}
