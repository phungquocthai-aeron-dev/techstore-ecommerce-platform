package com.techstore.review.dto.response;

import java.util.Map;

import lombok.Data;

@Data
public class ModerationResult {
    private String label; // Spam | Toxic | Valid
    private Double confidence;
    private Map<String, Double> probabilities;
    private String processedText;
}
