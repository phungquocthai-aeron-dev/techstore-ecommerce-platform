package com.techstore.warehouse.configuration;

import com.techstore.warehouse.exception.AppException;
import com.techstore.warehouse.exception.ErrorCode;

import feign.Response;
import feign.codec.ErrorDecoder;

public class FileFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 403 -> new AppException(ErrorCode.ACCOUNT_DISABLED);
            case 409 -> new AppException(ErrorCode.ACCOUNT_ALREADY_LINKED);
            case 500, 502, 503 -> new AppException(ErrorCode.USER_SERVICE_UNAVAILABLE);
            default -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        };
    }
}
