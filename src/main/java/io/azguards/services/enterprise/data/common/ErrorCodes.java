package io.azguards.services.enterprise.data.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodes {

    // 404
    DATA_GROUP_NOT_FOUND_ERROR("136.01.404.01", "Data group not found in the system"),

    // 400
    INVALID_SORTING_KEYS("136.01.400.01", "Sorting keys must be from {createdAt, updatedAt, groupCode, groupName, -createdAt, -updatedAt, -groupCode, -groupName}"),
    DUPLICATE_DATA_ITEM("136.01.400.02", "Duplicate data item in request"),
    DUPLICATE_GROUP_CODE_OR_GROUP_NAME("136.01.400.03", "Duplicate data with group code or group name"),
    INVALID_LANGUAGE_CODE("136.01.400.04", "Invalid language code"),
    DATA_ITEM_KEY_NULL_EMPTY("136.01.400.05", "Date items key is null or empty"),
    DATA_ITEM_VALUE_NULL_EMPTY("136.01.400.06", "Date items value is null or empty"),
    DATA_ITEM_KEY_INVALID_DATA_TYPE("136.01.400.07", "Invalid data item key data type"),
    DATA_ITEM_VALUE_INVALID_DATA_TYPE("136.01.400.08", "Invalid data item value data type"),
    DATA_GROUP_PRESENT_IN_FIREBASE("136.01.400.09", "Data group present in firebase"),
    CONDITION_MAPPING_KEY_NOT_CONFIGURED("136.01.400.10", "Condition mapping key not configured"),
    INVALID_DATA_GROUP_TYPE("136.01.400.11", "Invalid value of data group type"),
    DATA_ITEM_NOT_FOUND_IN_REQUEST("136.01.400.12", "Data item not found in request"),
    GROUP_VALUE_NOT_FOUND_IN_REQUEST("136.01.400.13", "Group value not found in request"),
    INVALID_TARGET_ID("136.01.400.14", "Invalid target id"),

    // 500
    DATA_GROUP_INSERT_IN_FIREBASE("136.01.500.02", "Error while insert data group template in firebase"),
    PROCESS_MESSAGE("136.01.500.01", "Error while process this message");
    private final String code;
    private final String message;
}
