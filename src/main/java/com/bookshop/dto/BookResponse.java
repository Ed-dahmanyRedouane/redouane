package com.bookshop.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    
    private Long id;
    
    private String title;
    
    private String author;
    
    private BigDecimal price;
    
    private Integer stock;
    
    private String description;
    
    private CategoryResponse category;
}
