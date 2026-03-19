package com.melonecom.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartBatchAddDTO {
    private List<CartBatchAddItemDTO> items;
}
