package com.bookshop.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String title;
    
    @NotBlank(message = "L'auteur est obligatoire")
    @Size(max = 150, message = "L'auteur ne peut pas dépasser 150 caractères")
    private String author;
    
    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private BigDecimal price;
    
    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;
    
    private String description;
    
    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;
}
