package com.techstore.user.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    /**
     * Regex số điện thoại di động Việt Nam
     * Tham khảo: unitop.com.vn
     */
    private static final String PHONE_REGEX = "^(032|033|034|035|036|037|038|039|" + "096|097|098|086|"
            + "083|084|085|081|082|088|091|094|"
            + "070|079|077|076|078|090|093|089|"
            + "056|058|092|059|099)[0-9]{7}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches(PHONE_REGEX);
    }
}
