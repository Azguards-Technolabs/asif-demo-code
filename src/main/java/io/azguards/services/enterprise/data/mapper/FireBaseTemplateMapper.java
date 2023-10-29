package io.azguards.services.enterprise.data.mapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.marketplace.services.enterprise.data.model.DataGroup;
import io.marketplace.services.enterprise.data.model.DataItem;
import io.marketplace.services.enterprise.data.model.MultipleLang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.firebase.remoteconfig.Parameter;
import com.google.firebase.remoteconfig.ParameterValue;
import com.google.firebase.remoteconfig.ParameterValueType;
import com.google.gson.Gson;

import io.azguards.services.enterprise.data.common.DataGroupType;
import io.azguards.services.enterprise.data.dto.FireBaseDataGroup;
import io.azguards.services.enterprise.data.dto.FireBaseDataItem;

@Component
public class FireBaseTemplateMapper {

    @Value("#{${firebase-condition.mapping}}")
    private Map<String, String> firebaseConditionMapping;

    @Autowired
    private Gson gson;

    public Parameter toParameter(DataGroup dataGroup) {
        Parameter parameter = new Parameter();
        Map<String, ParameterValue> conditionalValuesMap = this.toConditionalValuesMap(dataGroup);
        parameter.setConditionalValues(conditionalValuesMap);
        parameter.setValueType(ParameterValueType.JSON);
        parameter.setDefaultValue(conditionalValuesMap.getOrDefault(dataGroup.getDefaultLanguage(), null));
        return parameter;
    }

    private Map<String, ParameterValue> toConditionalValuesMap(DataGroup dataGroup) {
        Map<String, ParameterValue> conditionalValuesMap = new HashMap<>();
        this.groupDataByLanguageCode(dataGroup).forEach((key, value) -> {
            String conditionKey = Optional.ofNullable(firebaseConditionMapping.get(key)).orElse(key);
            conditionalValuesMap.put(conditionKey, ParameterValue.of(gson.toJson(value)));
        });
        return conditionalValuesMap;
    }

    private Map<String, FireBaseDataGroup> groupDataByLanguageCode(DataGroup dataGroup) {
        if (Boolean.TRUE.equals(dataGroup.getIsDeleted())) {
            return new HashMap<>();
        }

        Map<String, FireBaseDataGroup> fireBaseDataGroupMap = new HashMap<>();
        Map<DataItem, Map<String, MultipleLang>> dataItemLangMap = new HashMap<>();

		if (DataGroupType.SingleItem.getValue().equals(dataGroup.getDataGroupType())) {

			FireBaseDataGroup fireBaseDataGroup = FireBaseDataGroup.builder().id(dataGroup.getId())
					.groupCode(dataGroup.getGroupCode()).groupName(dataGroup.getGroupName())
					.defaultLanguage(dataGroup.getDefaultLanguage()).includeKey(dataGroup.getIncludeKey())
					.includeValue(dataGroup.getIncludeValue()).keyDataType(dataGroup.getKeyDataType())
					.valueDataType(dataGroup.getValueDataType()).dataGroupType(dataGroup.getDataGroupType())
					.groupValue(dataGroup.getGroupValue()).build();

			fireBaseDataGroupMap.put(dataGroup.getDefaultLanguage(), fireBaseDataGroup);
			return fireBaseDataGroupMap;

		}

        if (DataGroupType.MultipleItem.getValue().equals(dataGroup.getDataGroupType())) {
            dataGroup.getDataItems()
                .forEach(dataItemEntity -> dataItemLangMap.put(
                    dataItemEntity,
                    dataItemEntity.getMultipleLangs()
                        .stream()
                        .collect(Collectors.toMap(MultipleLang::getLanguage, Function.identity()))));
        }

        Map<String, String> groupNameMultiLangMap = dataGroup.getMultipleLangs()
            .stream()
            .collect(Collectors.toMap(MultipleLang::getLanguage, MultipleLang::getValue));

        groupNameMultiLangMap.forEach((key, value) -> {
            FireBaseDataGroup fireBaseDataGroup = FireBaseDataGroup.builder()
                .id(dataGroup.getId())
                .groupCode(dataGroup.getGroupCode()).groupName(value)
                .defaultLanguage(dataGroup.getDefaultLanguage())
                .includeKey(dataGroup.getIncludeKey())
                .includeValue(dataGroup.getIncludeValue())
                .keyDataType(dataGroup.getKeyDataType())
                .valueDataType(dataGroup.getValueDataType())
                .dataGroupType(dataGroup.getDataGroupType())
                .build();

            if (DataGroupType.MultipleItem.getValue().equals(dataGroup.getDataGroupType())) {
                List<FireBaseDataItem> dataItems = new LinkedList<>();
                dataItemLangMap.forEach((key1, value1) ->
                    {
                        if (value1.get(key) != null) {
                            dataItems.add(FireBaseDataItem.builder()
                                .id(value1.get(key).getId())
                                .key(key1.getKey())
                                .value(value1.get(key).getValue())
                                .sortOrder(key1.getSortOrder())
                                .isDefault(key1.getIsDefault())
                                .build());
                        }
                    }
                );
                fireBaseDataGroup.setDataItems(dataItems);
            } 
            fireBaseDataGroupMap.put(key, fireBaseDataGroup);
        });
        return fireBaseDataGroupMap;
    }
}
