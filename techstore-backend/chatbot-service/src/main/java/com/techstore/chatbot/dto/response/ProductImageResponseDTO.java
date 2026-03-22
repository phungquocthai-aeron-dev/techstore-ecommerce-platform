package com.techstore.chatbot.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageResponseDTO {
    private Long id;
    private String url;
    private Boolean isPrimary;
}
