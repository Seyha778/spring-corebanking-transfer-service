package com.apextech.transferservice.constant;

public enum TransferServiceEnum {
    OWN("OWN"),
    OTHER("OTHER");

    final String value;

    TransferServiceEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}