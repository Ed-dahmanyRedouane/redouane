package com.bookshop.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    
    private List<CartItemResponse> items;
    
    private BigDecimal totalAmount;
    
    private Integer totalItems;
}
