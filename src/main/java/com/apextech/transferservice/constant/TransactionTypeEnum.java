package com.apextech.transferservice.constant;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TransactionTypeEnum {
    CREDIT("CR", "Receiver"),
    DEBIT("DR", "Sender");

    final String value;
    final String description;

    TransactionTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static String getDescriptionByValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .map(TransactionTypeEnum::getDescription)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid transaction type: " + value
                ));
    }

    @Override
    public String toString() {
        return value;
    }
}
