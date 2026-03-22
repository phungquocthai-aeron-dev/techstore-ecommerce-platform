package com.techstore.chatbot.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String categoryType;
    private String pcComponentType;
    private String description;
}
