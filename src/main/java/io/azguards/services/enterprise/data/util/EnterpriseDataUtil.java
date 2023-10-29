package io.azguards.services.enterprise.data.util;

import io.azguards.services.enterprise.data.common.Constant;
import io.azguards.services.enterprise.data.common.ErrorCodes;
import io.marketplace.commons.exception.BadRequestException;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class EnterpriseDataUtil {
    public static List<Sort.Order> getSortingOrder(List<String> order) {
        boolean isValid = true;
        List<Sort.Order> orderList = new ArrayList<>();
        for (String sortKey : order) {
            if (Constant.SORTING_KEYS.contains(sortKey)) {
                Sort.Direction direction = sortKey.contains("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
                orderList.add(new Sort.Order(direction, sortKey.replace("-", "")));
            } else {
                isValid = false;
                break;
            }
        }
        if (!isValid) {
            throw new BadRequestException(
                ErrorCodes.INVALID_SORTING_KEYS.getCode(),
                ErrorCodes.INVALID_SORTING_KEYS.getMessage(),
                "");
        }
        return orderList;
    }
}
