package io.azguards.services.enterprise.data.mapper;

import io.azguards.services.enterprise.data.common.DataGroupType;
import io.azguards.services.enterprise.data.common.KeyDataType;
import io.azguards.services.enterprise.data.common.ValueDataType;
import io.azguards.services.enterprise.data.entity.DataGroupEntity;
import io.azguards.services.enterprise.data.entity.DataItemEntity;
import io.azguards.services.enterprise.data.entity.MultipleLangEntity;
import io.marketplace.commons.utils.MembershipUtils;
import io.marketplace.services.enterprise.data.model.DataGroup;
import io.marketplace.services.enterprise.data.model.DataItem;
import io.marketplace.services.enterprise.data.model.MultipleLang;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;

public class DataGroupMapper {

    public static DataGroup fromDataGroupEntity(DataGroupEntity entity) {
    	
        return DataGroup.builder()
            .id(entity.getId())
            .groupCode(entity.getGroupCode())
            .groupName(entity.getGroupName())
            .defaultLanguage(entity.getDefaultLanguage())
            .includeKey(entity.getIncludeKey())
            .includeValue(entity.getIncludeValue())
            .keyDataType(entity.getKeyDataType().getValue())
            .valueDataType(entity.getValueDataType().getValue())
            .groupValue(entity.getGroupValue())
            .appId(entity.getAppId())
            .platformId(entity.getPlatformId())
            .entityId(entity.getEntityId())
            .dataGroupType(entity.getDataGroupType().name())
            .multipleLangs(entity.getMultipleLangs().stream().distinct()
                    .map(ml -> MultipleLang.builder()
                        .id(ml.getId())
                        .value(ml.getValue())
                        .language(ml.getLanguage())
                        .isDeleted(ml.getIsDeleted())
                        .build())
                    .collect(Collectors.toList()))
            .dataItems(Optional.ofNullable(entity.getDataItems()).stream().flatMap(Collection::stream)
                .map(di -> DataItem.builder()
                    .id(di.getId())
                    .key(di.getKey())
                    .value(di.getValue())
                    .sortOrder(di.getSortOrder())
                    .isDefault(di.getIsDefault())
                    .isDeleted(di.getIsDeleted())
                    .multipleLangs(Optional.ofNullable(di.getMultipleLangs()).stream().flatMap(Collection::stream)
                        .map(ml -> MultipleLang.builder()
                            .id(ml.getId())
                            .value(ml.getValue())
                            .language(ml.getLanguage())
                            .build())
                        .collect(Collectors.toList()))
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    public static DataGroupEntity toDataGroupEntity(DataGroup dto) {
        LocalDateTime now = LocalDateTime.now();
        String userId = MembershipUtils.getUserId();
        DataGroupEntity dataGroupEntity = DataGroupEntity.builder()
            .groupCode(dto.getGroupCode())
            .groupName(dto.getGroupName())
            .defaultLanguage(dto.getDefaultLanguage())
            .includeKey(dto.getIncludeKey())
            .groupValue(dto.getGroupValue())
            .includeValue(dto.getIncludeValue())
            .keyDataType(KeyDataType.valueOf(dto.getKeyDataType()))
            .valueDataType(ValueDataType.valueOf(dto.getValueDataType()))
            .entityId(dto.getEntityId())
            .appId(dto.getAppId())
            .platformId(dto.getPlatformId())
            .dataGroupType(DataGroupType.getByValue(dto.getDataGroupType()))
            .createdAt(now)
            .createdBy(userId)
            .isDeleted(Boolean.FALSE)
            .updatedAt(now)
            .updatedBy(userId)
            .build();
        dataGroupEntity.setDataItems(new HashSet<>(toDataItemEntity(dto, dataGroupEntity)));
        dataGroupEntity.setMultipleLangs(new HashSet<>(toGroupNameMultipleLangEntity(dto, dataGroupEntity)));

        return dataGroupEntity;
    }

    private static List<DataItemEntity> toDataItemEntity(DataGroup dto, DataGroupEntity dataGroupEntity) {
        List<DataItemEntity> dataItemEntities = new LinkedList<>();

        if (!CollectionUtils.isEmpty(dto.getDataItems())) {
            dto.getDataItems().forEach(di -> {
                DataItemEntity dataItemEntity = DataItemEntity.builder()
                    .key(di.getKey())
                    .value(di.getValue())
                    .dataGroup(dataGroupEntity)
                    .sortOrder(di.getSortOrder())
                    .isDefault(di.getIsDefault())
                    .createdAt(LocalDateTime.now())
                    .createdBy(MembershipUtils.getUserId())
                    .isDeleted(Boolean.FALSE)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(MembershipUtils.getUserId())
                    .build();

                dataItemEntity.setMultipleLangs(new HashSet<>(toMultipleLangEntity(di, dataItemEntity)));
                dataItemEntities.add(dataItemEntity);
            });
        }
        return dataItemEntities;
    }

    private static List<MultipleLangEntity> toMultipleLangEntity(DataItem dataItem, DataItemEntity dataItemEntity) {
        List<MultipleLangEntity> multipleLangEntities = new LinkedList<>();
        dataItem.getMultipleLangs().forEach(ml -> {

            MultipleLangEntity multipleLangEntity = MultipleLangEntity.builder()
                .value(ml.getValue())
                .language(ml.getLanguage())
                .createdAt(LocalDateTime.now())
                .createdBy(MembershipUtils.getUserId())
                .dataItem(dataItemEntity)
                .isDeleted(Boolean.FALSE)
                .updatedAt(LocalDateTime.now())
                .updatedBy(MembershipUtils.getUserId())
                .build();

            multipleLangEntities.add(multipleLangEntity);
        });
        return multipleLangEntities;
    }

    private static List<MultipleLangEntity> toGroupNameMultipleLangEntity(DataGroup dto, DataGroupEntity dataGroupEntity) {
        List<MultipleLangEntity> multipleLangEntities = new LinkedList<>();
        Optional.ofNullable(dto.getMultipleLangs()).map(Collection::stream).orElseGet(Stream::empty).forEach(ml -> {
            MultipleLangEntity multipleLangEntity = MultipleLangEntity.builder()
                .value(ml.getValue())
                .language(ml.getLanguage())
                .createdAt(LocalDateTime.now())
                .createdBy(MembershipUtils.getUserId())
                .dataGroup(dataGroupEntity)
                .isDeleted(Boolean.FALSE)
                .updatedAt(LocalDateTime.now())
                .updatedBy(MembershipUtils.getUserId())
                .build();

            dataGroupEntity.addMultipleLangEntity(multipleLangEntity);
            multipleLangEntities.add(multipleLangEntity);
        });
        return multipleLangEntities;
    }

    public static DataGroup fromEntity(DataGroupEntity entity) {
        return DataGroup.builder()
            .id(entity.getId())
            .groupCode(entity.getGroupCode())
            .groupName(entity.getGroupName())
            .defaultLanguage(entity.getDefaultLanguage())
            .includeKey(entity.getIncludeKey())
            .includeValue(entity.getIncludeValue())
            .keyDataType(entity.getKeyDataType().getValue())
            .valueDataType(entity.getValueDataType().getValue())
            .dataGroupType(entity.getDataGroupType().getValue())
            .groupValue(entity.getGroupValue())
            .isDeleted(entity.getIsDeleted())
            .build();
    }

    public static DataItem fromEntity(DataItemEntity di) {
        return DataItem.builder()
            .id(di.getId())
            .key(di.getKey())
            .value(di.getValue())
            .sortOrder(di.getSortOrder())
            .isDefault(di.getIsDefault())
            .isDeleted(di.getIsDeleted())
            .build();
    }

    public static MultipleLang fromEntity(MultipleLangEntity ml) {
        return MultipleLang.builder()
            .id(ml.getId())
            .value(ml.getValue())
            .language(ml.getLanguage())
            .isDeleted(ml.getIsDeleted())
            .build();
    }
    
    public static DataGroupEntity updateDataGroupEntity(DataGroupEntity dataGroupEntity,  DataGroup dto) {
    	dataGroupEntity.setGroupCode(dto.getGroupCode());
    	dataGroupEntity.setGroupName(dto.getGroupName());
    	dataGroupEntity.setDefaultLanguage(dto.getDefaultLanguage());
    	dataGroupEntity.setIncludeKey(dto.getIncludeKey());
    	dataGroupEntity.setIncludeValue(dto.getIncludeValue());
    	dataGroupEntity.setGroupValue(dto.getGroupValue());
    	dataGroupEntity.setKeyDataType(KeyDataType.valueOf(dto.getKeyDataType()));
    	dataGroupEntity.setValueDataType(ValueDataType.valueOf(dto.getValueDataType()));
    	dataGroupEntity.setEntityId(dto.getEntityId());
    	dataGroupEntity.setAppId(dto.getAppId());
    	dataGroupEntity.setPlatformId(dto.getPlatformId());
    	dataGroupEntity.setDataGroupType(DataGroupType.getByValue(dto.getDataGroupType()));
    	dataGroupEntity.setUpdatedAt(LocalDateTime.now());
    	dataGroupEntity.setUpdatedBy(MembershipUtils.getUserId());          
        updateDataItemEntity(dto, dataGroupEntity);
        updateGroupNameMultipleLangEntity(dto,dataGroupEntity);
        return dataGroupEntity;
    }
    
    private static void updateDataItemEntity(DataGroup dto, DataGroupEntity dataGroupEntity) {

    	
		Map<String, DataItem> dataItemMap = Optional.ofNullable(dto.getDataItems()).map(Collection::stream)
				.orElseGet(Stream::empty).collect(Collectors.toMap(DataItem::getKey, e -> e));
		Map<String, DataItemEntity> dataItemDbMap = dataGroupEntity.getDataItems().stream()
				.collect(Collectors.toMap(DataItemEntity::getKey, e -> e));

		List<DataItemEntity> toBeRemoved = dataGroupEntity.getDataItems().stream()
				.filter(e -> !dataItemMap.containsKey(e.getKey())).toList();
		List<DataItemEntity> toBeUpdated = dataGroupEntity.getDataItems().stream()
				.filter(e -> dataItemMap.containsKey(e.getKey())).toList();
		List<DataItem> toBeAdded = Optional.ofNullable(dto.getDataItems()).map(Collection::stream)
				.orElseGet(Stream::empty)
				.filter(e -> !dataItemDbMap.containsKey(e.getKey()))
				.toList();
    	
		dataGroupEntity.getDataItems().removeAll(toBeRemoved);
		toBeUpdated.forEach(existingDataItemEntity -> {
			DataItem dataItem = dataItemMap.get(existingDataItemEntity.getKey());
			existingDataItemEntity.setValue(dataItem.getValue());
    		existingDataItemEntity.setSortOrder(dataItem.getSortOrder());
    		existingDataItemEntity.setIsDefault(dataItem.getIsDefault());
    		existingDataItemEntity.setIsDeleted(Boolean.FALSE);
    		existingDataItemEntity.setUpdatedAt(LocalDateTime.now());
    		existingDataItemEntity.setUpdatedBy(MembershipUtils.getUserId());
    		updateDataItemMultipleLangEntity(dataItem,existingDataItemEntity);
			dataGroupEntity.getDataItems().add(existingDataItemEntity);
		});

		toBeAdded.forEach(dataItem -> {
			 DataItemEntity dataItemEntity = DataItemEntity.builder()
                     .key(dataItem.getKey())
                     .value(dataItem.getValue())
                     .dataGroup(dataGroupEntity)
                     .sortOrder(dataItem.getSortOrder())
                     .isDefault(dataItem.getIsDefault())
                     .createdAt(LocalDateTime.now())
                     .createdBy(MembershipUtils.getUserId())
                     .isDeleted(Boolean.FALSE)
                     .updatedAt(LocalDateTime.now())
                     .updatedBy(MembershipUtils.getUserId())
                     .build();
                 dataItemEntity.getMultipleLangs().addAll(toMultipleLangEntity(dataItem, dataItemEntity));
                 dataGroupEntity.getDataItems().add(dataItemEntity);		
        });
    }
    
    private static void updateDataItemMultipleLangEntity(DataItem dataItem, DataItemEntity dataItemEntity) {
    	
    	Map<String, MultipleLang> multipleLangMap = Optional.ofNullable( dataItem.getMultipleLangs()).map(Collection::stream)
				.orElseGet(Stream::empty)
				.collect(Collectors.toMap(MultipleLang::getLanguage, e -> e));
		Map<String, MultipleLangEntity> multipleLangDbMap = dataItemEntity.getMultipleLangs().stream()
				.collect(Collectors.toMap(MultipleLangEntity::getLanguage, e -> e));

		List<MultipleLangEntity> toBeRemoved = dataItemEntity.getMultipleLangs().stream()
				.filter(e -> !multipleLangMap.containsKey(e.getLanguage())).toList();
		List<MultipleLangEntity> toBeUpdated = dataItemEntity.getMultipleLangs().stream()
				.filter(e -> multipleLangMap.containsKey(e.getLanguage())).toList();
		List<MultipleLang> toBeAdded = Optional.ofNullable( dataItem.getMultipleLangs()).map(Collection::stream)
				.orElseGet(Stream::empty)
				.filter(e -> !multipleLangDbMap.containsKey(e.getLanguage()))
				.toList();
    	
		dataItemEntity.getMultipleLangs().removeAll(toBeRemoved);

		toBeUpdated.forEach(existingMultiLangEntity -> {
			MultipleLang multipleLang = multipleLangMap.get(existingMultiLangEntity.getLanguage());
			existingMultiLangEntity.setValue(multipleLang.getValue());
    		existingMultiLangEntity.setIsDeleted(Boolean.FALSE);
    		existingMultiLangEntity.setUpdatedAt(LocalDateTime.now());
    		existingMultiLangEntity.setUpdatedBy(MembershipUtils.getUserId());
    		dataItemEntity.getMultipleLangs().add(existingMultiLangEntity);
		});
		
		toBeAdded.forEach(ml -> {
            MultipleLangEntity multipleLangEntity = MultipleLangEntity.builder()
                .value(ml.getValue())
                .language(ml.getLanguage())
                .createdAt(LocalDateTime.now())
                .createdBy(MembershipUtils.getUserId())
                .dataItem(dataItemEntity)
                .isDeleted(Boolean.FALSE)
                .updatedAt(LocalDateTime.now())
                .updatedBy(MembershipUtils.getUserId())
                .build();
            dataItemEntity.getMultipleLangs().add(multipleLangEntity);
        });
    }
    
    private static void updateGroupNameMultipleLangEntity(DataGroup dto, DataGroupEntity dataGroupEntity) {

    	Map<String, MultipleLang> multipleLangMap = Optional.ofNullable( dto.getMultipleLangs()).map(Collection::stream)
				.orElseGet(Stream::empty)
				.collect(Collectors.toMap(MultipleLang::getLanguage, e -> e));
		Map<String, MultipleLangEntity> multipleLangDbMap = dataGroupEntity.getMultipleLangs().stream()
				.collect(Collectors.toMap(MultipleLangEntity::getLanguage, e -> e));

		List<MultipleLangEntity> toBeRemoved = dataGroupEntity.getMultipleLangs().stream()
				.filter(e -> !multipleLangMap.containsKey(e.getLanguage())).toList();
		List<MultipleLangEntity> toBeUpdated = dataGroupEntity.getMultipleLangs().stream()
				.filter(e -> multipleLangMap.containsKey(e.getLanguage())).toList();
		List<MultipleLang> toBeAdded = Optional.ofNullable( dto.getMultipleLangs()).map(Collection::stream)
				.orElseGet(Stream::empty)
				.filter(e -> !multipleLangDbMap.containsKey(e.getLanguage()))
				.toList();
    	
		dataGroupEntity.getMultipleLangs().removeAll(toBeRemoved);

		toBeUpdated.forEach(existingMultiLangEntity -> {
			MultipleLang multipleLang = multipleLangMap.get(existingMultiLangEntity.getLanguage());
			existingMultiLangEntity.setValue(multipleLang.getValue());
    		existingMultiLangEntity.setIsDeleted(Boolean.FALSE);
    		existingMultiLangEntity.setUpdatedAt(LocalDateTime.now());
    		existingMultiLangEntity.setUpdatedBy(MembershipUtils.getUserId());
    		dataGroupEntity.getMultipleLangs().add(existingMultiLangEntity);
		});
		
		toBeAdded.forEach(ml -> {
	        MultipleLangEntity multipleLangEntity = MultipleLangEntity.builder()
	                .value(ml.getValue())
	                .language(ml.getLanguage())
	                .createdAt(LocalDateTime.now())
	                .createdBy(MembershipUtils.getUserId())
	                .dataGroup(dataGroupEntity)
	                .isDeleted(Boolean.FALSE)
	                .updatedAt(LocalDateTime.now())
	                .updatedBy(MembershipUtils.getUserId())
	                .build();

	        dataGroupEntity.addMultipleLangEntity(multipleLangEntity);

        });
    }

}
