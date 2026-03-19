package com.melonecom.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class HomeNavCardSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<CardConfig> cards;

    @Data
    public static class CardConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long cardId;
        private String title;
        private Integer sort;
        private List<ItemConfig> items;
    }

    @Data
    public static class ItemConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long itemId;
        private String keyword;
        private String title;
        private String imageUrl;
        private Integer sort;
    }
}
