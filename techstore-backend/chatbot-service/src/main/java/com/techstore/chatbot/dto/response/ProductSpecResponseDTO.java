package com.techstore.chatbot.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSpecResponseDTO {
    private Long id;
    private String specKey;
    private String specValue;
}
