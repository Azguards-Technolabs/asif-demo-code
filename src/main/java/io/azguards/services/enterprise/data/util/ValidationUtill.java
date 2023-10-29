package io.azguards.services.enterprise.data.util;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.azguards.services.enterprise.data.common.Constant;
import io.azguards.services.enterprise.data.common.DataGroupType;
import io.azguards.services.enterprise.data.common.ErrorCodes;
import io.azguards.services.enterprise.data.common.KeyDataType;
import io.azguards.services.enterprise.data.common.Language;
import io.azguards.services.enterprise.data.service.EventTrackingService;
import io.marketplace.commons.exception.BadRequestException;
import io.marketplace.services.enterprise.data.model.DataGroup;
import io.marketplace.services.enterprise.data.model.DataItem;

@Component
public class ValidationUtill {

    @Autowired
    private EventTrackingService eventTrackingService;

    private boolean haveDuplicateDataItem(List<DataItem> dataItems) {
        return !dataItems.stream().map(DataItem::getKey).allMatch(new HashSet<>()::add);
    }

    private boolean haveNullOrEmptyDataItemKey(List<DataItem> dataItems) {
        return dataItems.stream().anyMatch(dataItem -> ObjectUtils.isEmpty(dataItem.getKey()));
    }

    private boolean haveNullOrEmptyDataItemValue(List<DataItem> dataItems) {
        return dataItems.stream().anyMatch(dataItem -> ObjectUtils.isEmpty(dataItem.getValue()));
    }

    public void validateDataType(List<DataItem> dataItems, boolean validateKeyDataType, boolean validateValueDataType,
                                 String keyDataType, String valueDataType, String businessId) {
        dataItems.stream().forEach(dataItem -> {

            if (validateKeyDataType) {
                try {
                    this.validateDataType(dataItem.getKey(), keyDataType);
                } catch (Exception e) {
                    BadRequestException badRequestException = new BadRequestException(
                        ErrorCodes.DATA_ITEM_KEY_INVALID_DATA_TYPE.getCode(),
                        ErrorCodes.DATA_ITEM_KEY_INVALID_DATA_TYPE.getMessage(), businessId);
                    eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                        Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                        ErrorCodes.DATA_ITEM_KEY_INVALID_DATA_TYPE.getCode(),
                        ErrorCodes.DATA_ITEM_KEY_INVALID_DATA_TYPE.getMessage(), businessId, badRequestException);
                    throw badRequestException;
                }
            }

            if (validateValueDataType) {
                try {
                    this.validateDataType(dataItem.getValue(), valueDataType);
                } catch (Exception e) {
                    BadRequestException badRequestException = new BadRequestException(
                        ErrorCodes.DATA_ITEM_VALUE_INVALID_DATA_TYPE.getCode(),
                        ErrorCodes.DATA_ITEM_VALUE_INVALID_DATA_TYPE.getMessage(), businessId);
                    eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                        Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                        ErrorCodes.DATA_ITEM_VALUE_INVALID_DATA_TYPE.getCode(),
                        ErrorCodes.DATA_ITEM_VALUE_INVALID_DATA_TYPE.getMessage(), businessId, badRequestException);
                    throw badRequestException;
                }
            }
        });
    }

    public void validateCreateEnterpriseDataRequest(DataGroup dataGroup, String businessId) {
        if (ObjectUtils.isEmpty(DataGroupType.getByValue(dataGroup.getDataGroupType()))) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.INVALID_DATA_GROUP_TYPE.getCode(),
                ErrorCodes.INVALID_DATA_GROUP_TYPE.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.INVALID_DATA_GROUP_TYPE.getCode(),
                ErrorCodes.INVALID_DATA_GROUP_TYPE.getMessage(),
                businessId, badRequestException);
            throw badRequestException;
        }

        if (dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue()) && CollectionUtils.isEmpty(dataGroup.getDataItems())) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.DATA_ITEM_NOT_FOUND_IN_REQUEST.getCode(),
                ErrorCodes.DATA_ITEM_NOT_FOUND_IN_REQUEST.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.DATA_ITEM_NOT_FOUND_IN_REQUEST.getCode(),
                ErrorCodes.DATA_ITEM_NOT_FOUND_IN_REQUEST.getMessage(), businessId, badRequestException);
            throw badRequestException;
        }

        if (dataGroup.getDataGroupType().equals(DataGroupType.SingleItem.getValue()) && StringUtils.isEmpty(dataGroup.getGroupValue())) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.GROUP_VALUE_NOT_FOUND_IN_REQUEST.getCode(),
                ErrorCodes.GROUP_VALUE_NOT_FOUND_IN_REQUEST.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.GROUP_VALUE_NOT_FOUND_IN_REQUEST.getCode(),
                ErrorCodes.GROUP_VALUE_NOT_FOUND_IN_REQUEST.getMessage(), businessId, badRequestException);
            throw badRequestException;
        }

        if (dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue()) && this.haveDuplicateDataItem(dataGroup.getDataItems())) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.DUPLICATE_DATA_ITEM.getCode(),
                ErrorCodes.DUPLICATE_DATA_ITEM.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.DUPLICATE_DATA_ITEM.getCode(),
                ErrorCodes.DUPLICATE_DATA_ITEM.getMessage(),
                businessId, badRequestException);
            throw badRequestException;
        }

        if (ObjectUtils.isEmpty(Language.getByValue(dataGroup.getDefaultLanguage()))) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.INVALID_LANGUAGE_CODE.getCode(),
                ErrorCodes.INVALID_LANGUAGE_CODE.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.INVALID_LANGUAGE_CODE.getCode(),
                ErrorCodes.INVALID_LANGUAGE_CODE.getMessage(),
                businessId, badRequestException);
            throw badRequestException;
        }

        if (Boolean.TRUE.equals(dataGroup.getIncludeKey()
            && dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue()))
            && this.haveNullOrEmptyDataItemKey(dataGroup.getDataItems())) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.DATA_ITEM_KEY_NULL_EMPTY.getCode(),
                ErrorCodes.DATA_ITEM_KEY_NULL_EMPTY.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.DATA_ITEM_KEY_NULL_EMPTY.getCode(),
                ErrorCodes.DATA_ITEM_KEY_NULL_EMPTY.getMessage(),
                businessId, badRequestException);
            throw badRequestException;
        }

        if (Boolean.TRUE.equals(dataGroup.getIncludeValue()
	        && dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue()))
	        && this.haveNullOrEmptyDataItemValue(dataGroup.getDataItems())) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.DATA_ITEM_VALUE_NULL_EMPTY.getCode(),
                ErrorCodes.DATA_ITEM_VALUE_NULL_EMPTY.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.DATA_ITEM_VALUE_NULL_EMPTY.getCode(),
                ErrorCodes.DATA_ITEM_VALUE_NULL_EMPTY.getMessage(),
                businessId, badRequestException);
            throw badRequestException;
        }
    }

	public void validateUpdateEnterpriseDataRequest (DataGroup dataGroup, String businessId) {
	      if (ObjectUtils.isEmpty(DataGroupType.getByValue(dataGroup.getDataGroupType()))) {
	        	 BadRequestException badRequestException = new BadRequestException(
	                     ErrorCodes.INVALID_DATA_GROUP_TYPE.getCode(),
	                     ErrorCodes.INVALID_DATA_GROUP_TYPE.getMessage(),
	                     businessId);
	        	 eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
	                   Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
	                   ErrorCodes.INVALID_DATA_GROUP_TYPE.getCode(),
	                   ErrorCodes.INVALID_DATA_GROUP_TYPE.getMessage(),
	                   businessId, badRequestException);
	        	 throw badRequestException;
	        }

	        if (dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue()) && CollectionUtils.isEmpty(dataGroup.getDataItems())) {
	       	 	BadRequestException badRequestException = new BadRequestException(
	                     ErrorCodes.DATA_ITEM_NOT_FOUND_IN_REQUEST.getCode(),
	                     ErrorCodes.DATA_ITEM_NOT_FOUND_IN_REQUEST.getMessage(),
	                     businessId);
	       	 	eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
	                   Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
	                   ErrorCodes.DATA_ITEM_NOT_FOUND_IN_REQUEST.getCode(),
	                   ErrorCodes.DATA_ITEM_NOT_FOUND_IN_REQUEST.getMessage(), businessId, badRequestException);
				throw badRequestException;
			}

	        if (dataGroup.getDataGroupType().equals(DataGroupType.SingleItem.getValue()) && StringUtils.isEmpty(dataGroup.getGroupValue())) {
	       	 	BadRequestException badRequestException = new BadRequestException(
	                     ErrorCodes.GROUP_VALUE_NOT_FOUND_IN_REQUEST.getCode(),
	                     ErrorCodes.GROUP_VALUE_NOT_FOUND_IN_REQUEST.getMessage(),
	                     businessId);
	       	 	eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
	                   Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
	                   ErrorCodes.GROUP_VALUE_NOT_FOUND_IN_REQUEST.getCode(),
	                   ErrorCodes.GROUP_VALUE_NOT_FOUND_IN_REQUEST.getMessage(), businessId, badRequestException);
				throw badRequestException;
			}

	        if (dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue()) && this.haveDuplicateDataItem(dataGroup.getDataItems())) {
	        	 BadRequestException badRequestException = new BadRequestException(
	                      ErrorCodes.DUPLICATE_DATA_ITEM.getCode(),
	                      ErrorCodes.DUPLICATE_DATA_ITEM.getMessage(),
	                      businessId);
	            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
	                    Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
	                    ErrorCodes.DUPLICATE_DATA_ITEM.getCode(),
	                    ErrorCodes.DUPLICATE_DATA_ITEM.getMessage(),
	                    businessId, badRequestException);
	            throw badRequestException;
	        }

			if (ObjectUtils.isEmpty(Language.getByValue(dataGroup.getDefaultLanguage()))) {
				BadRequestException badRequestException = new BadRequestException(
	                    ErrorCodes.INVALID_LANGUAGE_CODE.getCode(),
	                    ErrorCodes.INVALID_LANGUAGE_CODE.getMessage(),
	                    businessId);
				eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
	                  Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
	                  ErrorCodes.INVALID_LANGUAGE_CODE.getCode(),
	                  ErrorCodes.INVALID_LANGUAGE_CODE.getMessage(),
	                  businessId, badRequestException);
	       	 	throw badRequestException;
			}

			if (dataGroup.getIncludeKey() && dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue())  && this.haveNullOrEmptyDataItemKey(dataGroup.getDataItems())) {
				BadRequestException badRequestException = new BadRequestException(
	                    ErrorCodes.DATA_ITEM_KEY_NULL_EMPTY.getCode(),
	                    ErrorCodes.DATA_ITEM_KEY_NULL_EMPTY.getMessage(),
	                    businessId);
				eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
	                  Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
	                  ErrorCodes.DATA_ITEM_KEY_NULL_EMPTY.getCode(),
	                  ErrorCodes.DATA_ITEM_KEY_NULL_EMPTY.getMessage(),
	                  businessId, badRequestException);
	       	 	throw badRequestException;
			}

			if (dataGroup.getIncludeValue() && dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue()) && this.haveNullOrEmptyDataItemValue(dataGroup.getDataItems())) {
				BadRequestException badRequestException = new BadRequestException(
	                    ErrorCodes.DATA_ITEM_VALUE_NULL_EMPTY.getCode(),
	                    ErrorCodes.DATA_ITEM_VALUE_NULL_EMPTY.getMessage(),
	                    businessId);
				eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
	                  Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
	                  ErrorCodes.DATA_ITEM_VALUE_NULL_EMPTY.getCode(),
	                  ErrorCodes.DATA_ITEM_VALUE_NULL_EMPTY.getMessage(),
	                  businessId, badRequestException);
	       	 	throw badRequestException;
			}
	}
    
	private void validateDataType(String value, String dataType) {
		try {
			switch (KeyDataType.valueOf(dataType)) {
				case String:
					break;
				case Integer:
					Integer.valueOf(value);
					break;
				case Long:
					Long.valueOf(value);
					break;
				case BigDecimal:
					new BigDecimal(value);
					break;
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid data type for value: " + value);
		}
	}

}
