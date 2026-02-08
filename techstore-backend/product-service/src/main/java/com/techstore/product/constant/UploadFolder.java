package com.techstore.product.constant;

public enum UploadFolder {
    USER_AVATAR("img/users"),
    PRODUCT_IMAGE("img/products");

    private final String relativePath;

    UploadFolder(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getRelativePath() {
        return relativePath;
    }
}
