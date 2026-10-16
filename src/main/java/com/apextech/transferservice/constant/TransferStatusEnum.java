package com.apextech.transferservice.constant;

public enum TransferStatusEnum {
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    SUCCESS("SUCCESS"),
    REVERSED("REVERSED"),
    FAILED("FAILED");

    final String value;

    TransferStatusEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}