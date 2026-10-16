package com.apextech.transferservice.constant;

public enum AccountStatusEnum {
    ACTIVE("A"),
    PROGRESS("I"),
    CLOSE("C"),
    DORMANT("D");

    final String value;

    AccountStatusEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
