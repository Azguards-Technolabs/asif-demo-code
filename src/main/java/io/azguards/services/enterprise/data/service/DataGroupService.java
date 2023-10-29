package io.azguards.services.enterprise.data.service;

import io.azguards.services.enterprise.data.common.Constant;
import io.azguards.services.enterprise.data.common.DataGroupType;
import io.azguards.services.enterprise.data.common.DetailsLevel;
import io.azguards.services.enterprise.data.common.ErrorCodes;
import io.azguards.services.enterprise.data.common.KeyDataType;
import io.azguards.services.enterprise.data.common.TargetId;
import io.azguards.services.enterprise.data.dto.EntDataLoaded;
import io.azguards.services.enterprise.data.entity.DataGroupEntity;
import io.azguards.services.enterprise.data.entity.DataItemEntity;
import io.azguards.services.enterprise.data.entity.MultipleLangEntity;
import io.azguards.services.enterprise.data.mapper.DataGroupMapper;
import io.azguards.services.enterprise.data.repository.DataGroupRepository;
import io.azguards.services.enterprise.data.repository.DataItemRepository;
import io.azguards.services.enterprise.data.repository.MultipleLangRepository;
import io.azguards.services.enterprise.data.util.ChangeDataEventPublisherUtill;
import io.azguards.services.enterprise.data.util.EnterpriseDataUtil;
import io.azguards.services.enterprise.data.util.RemoteConfigUtil;
import io.azguards.services.enterprise.data.util.ValidationUtill;
import io.marketplace.commons.exception.BadRequestException;
import io.marketplace.commons.exception.InternalServerErrorException;
import io.marketplace.commons.exception.NotFoundException;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.model.event.EventStatus;
import io.marketplace.commons.model.event.EventType;
import io.marketplace.commons.utils.MembershipUtils;
import io.marketplace.commons.utils.StringUtils;
import io.marketplace.services.enterprise.data.model.DataGroup;
import io.marketplace.services.enterprise.data.model.DataItem;
import io.marketplace.services.enterprise.data.model.ListDataGroupIds;
import io.marketplace.services.enterprise.data.model.MultipleLang;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataGroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupService.class);

    private static final String EMIT_ENTERPRISE_DATA_GROUP = "Emit Data Loaded of data group id: %s";
    private static final String PUSH_ENTERPRISE_DATA_GROUP = "Push Data Group Loaded of data group id: %s";

    private final DataGroupRepository dataGroupRepository;
    private final DataItemRepository dataItemRepository;
    private final MultipleLangRepository multipleLangRepository;
    private final EventTrackingService eventTrackingService;
    private final ValidationUtill validationUtill;
    private final ChangeDataEventPublisherUtill changeDataEventPublisherUtill;
    private final RemoteConfigUtil remoteConfigUtil;

    @Value("${ent-data.auto-sync-in-realtime.fire-base:true}")
    private Boolean autoSyncFireBase;

    @Value("${ent-data.cdc-events.enabled:true}")
    private Boolean cdcEventsEnabled;

    @Value("${ent-data.auto-sync-upon-cdc-events.fire-base:true}")
    private Boolean autoSyncFirebaseUponCDC;

    @Value("${ent-data.cdc-events-success-data-loader.enabled:true}")
    private Boolean cdcEventsDataLoader;

    public DataGroup getByDataGroupId(UUID dataGroupId) {
        LOGGER.info("Get enterprise data by data group id: ", dataGroupId);
        String businessId = String.format(Constant.DATA_GROUP_ID, dataGroupId.toString());
        eventTrackingService.traceEvent(
            Constant.GET_ENTERPRISE_DATA,
            Constant.EVENT_CODE_GET_ENTERPRISE_DATA_REQUEST,
            "Received a request to get enterprise data",
            businessId,
            dataGroupId
        );

        Optional<DataGroupEntity> optionalDataGroupEntity =
            dataGroupRepository.findValidDataGroup(dataGroupId);
        if (optionalDataGroupEntity.isEmpty()) {
            NotFoundException notFoundException =
                new NotFoundException(
                    ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getCode(),
                    ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getMessage(),
                    businessId
                );
            eventTrackingService.traceError(
                Constant.GET_ENTERPRISE_DATA,
                Constant.EVENT_CODE_GET_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getCode(),
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getMessage(),
                businessId,
                notFoundException
            );
            throw notFoundException;
        }

        DataGroupEntity dataGroupEntity = optionalDataGroupEntity.get();
        DataGroup dataGroup = convertToDataGroup(DetailsLevel.FULL, dataGroupEntity);

        eventTrackingService.traceEvent(
            Constant.GET_ENTERPRISE_DATA,
            Constant.EVENT_CODE_GET_ENTERPRISE_DATA_RESPONSE,
            "Received a response of get enterprise data",
            businessId,
            dataGroup
        );

        return dataGroup;
    }

    public List<DataGroup> searchDataGroup(
        String searchText,
        String appId,
        String entityId,
        String platformId,
        List<String> dataGroupCodes,
        String detailsLevel,
        Long pageNumber,
        Long pageSize,
        List<String> sort) {

        String businessId = "";
        eventTrackingService.traceEvent(
            Constant.SEARCH_ENTERPRISE_DATA,
            Constant.EVENT_CODE_SEARCH_ENTERPRISE_DATA_REQUEST,
            "Received a request to search enterprise data",
            businessId,
            ""
        );

        DetailsLevel dLevel = Optional.ofNullable(detailsLevel)
            .map(DetailsLevel::valueOf)
            .orElse(null);

        final int intPageNumber =
            StringUtils.normalizePageNumber(pageNumber == null ? 0 : pageNumber.intValue());
        final int intPageSize =
            StringUtils.normalizePageSize(pageSize == null ? 10 : pageSize.intValue());

        PageRequest pageRequest =
            PageRequest.of(intPageNumber - 1, intPageSize, Sort.by(EnterpriseDataUtil.getSortingOrder(
                (sort != null && !sort.isEmpty()) ? sort : Collections.singletonList("-createdAt"))));

        Page<DataGroupEntity> dataGroupPage = dataGroupRepository.findDataGroupByMultipleConditions(
            pageRequest,
            searchText,
            appId,
            entityId,
            platformId,
            dataGroupCodes);

        List<DataGroup> dataGroups = dataGroupPage.map(entity -> convertToDataGroup(dLevel, entity)).stream().toList();

        eventTrackingService.traceEvent(
            Constant.SEARCH_ENTERPRISE_DATA,
            Constant.EVENT_CODE_SEARCH_ENTERPRISE_DATA_RESPONSE,
            "Received a response of search enterprise data",
            businessId,
            dataGroups);

        return dataGroups;
    }

    /**
     * @param detailsLevel
     * @param entity
     * @return
     */
    private DataGroup convertToDataGroup(DetailsLevel detailsLevel, DataGroupEntity entity) {
        DataGroup result = DataGroupMapper.fromEntity(entity);
        if (!DetailsLevel.FULL.equals(detailsLevel)) {
            return result;
        }
        List<DataItemEntity> dataItemEntities =
            dataItemRepository.findValidDataItemsByDataGroupId(entity.getId());
        List<MultipleLangEntity> multipleLangEntities =
            multipleLangRepository.findValidMultipleLangsByDataGroupId(entity.getId());
        if (CollectionUtils.isEmpty(dataItemEntities) && CollectionUtils.isEmpty(multipleLangEntities)) {
            return result;
        }
        result.setDataItems(new ArrayList<>());
        result.setMultipleLangs(new ArrayList<>());
        for (MultipleLangEntity mlGroupEntity : multipleLangEntities) {
            MultipleLang mlGrp = DataGroupMapper.fromEntity(mlGroupEntity);
            result.getMultipleLangs().add(mlGrp);
        }
        for (DataItemEntity dataItemEntity : dataItemEntities) {
            DataItem item = DataGroupMapper.fromEntity(dataItemEntity);
            List<MultipleLangEntity> subMultipleLangEntities =
                multipleLangRepository.findValidMultipleLangsByDataItemId(dataItemEntity.getId());
            item.setMultipleLangs(new ArrayList<>());
            for (MultipleLangEntity mlItemEntity : subMultipleLangEntities) {
                MultipleLang mlItem = DataGroupMapper.fromEntity(mlItemEntity);
                item.getMultipleLangs().add(mlItem);
            }
            result.getDataItems().add(item);
        }
        return result;
    }

    @Transactional(rollbackOn = {InternalServerErrorException.class})
    public void deleteEnterpriseData(UUID dataGroupId) {
        LOGGER.info("Delete enterprise data by data group id: {}", dataGroupId);
        String businessId = String.format(Constant.DATA_GROUP_ID, dataGroupId.toString());
        eventTrackingService.traceEvent(
            Constant.DELETE_ENTERPRISE_DATA,
            Constant.EVENT_CODE_DELETE_ENTERPRISE_DATA_REQUEST,
            "Received a request to delete enterprise data",
            businessId,
            dataGroupId);

        Optional<DataGroupEntity> optionalDataGroupEntity = dataGroupRepository.findById(dataGroupId);
        if (optionalDataGroupEntity.isEmpty()) {
            NotFoundException notFoundException = new NotFoundException(
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getCode(),
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getMessage(),
                businessId
            );
            eventTrackingService.traceError(Constant.GET_ENTERPRISE_DATA,
                Constant.EVENT_CODE_GET_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getCode(),
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getMessage(),
                businessId,
                notFoundException
            );
            throw notFoundException;
        }
        LocalDateTime now = LocalDateTime.now();
        DataGroupEntity dataGroupEntity = optionalDataGroupEntity.get();
        dataGroupEntity.setIsDeleted(Boolean.TRUE);
        dataGroupEntity.setDeletedAt(now);
        dataGroupRepository.save(dataGroupEntity);

        List<MultipleLangEntity> multipleLangsByDataGroupId = multipleLangRepository.findValidMultipleLangsByDataGroupId(dataGroupEntity.getId());
        for (MultipleLangEntity multipleLangEntity : multipleLangsByDataGroupId) {
            multipleLangEntity.setIsDeleted(Boolean.TRUE);
            multipleLangEntity.setDeletedAt(now);
        }
        multipleLangRepository.saveAll(multipleLangsByDataGroupId);

        List<DataItemEntity> dataItemEntities = dataItemRepository.findValidDataItemsByDataGroupId(dataGroupEntity.getId());
        for (DataItemEntity dataItemEntity : dataItemEntities) {
            dataItemEntity.setIsDeleted(Boolean.TRUE);
            dataItemEntity.setDeletedAt(now);
            List<MultipleLangEntity> multipleLangsByDataItemId = multipleLangRepository.findValidMultipleLangsByDataItemId(dataItemEntity.getId());
            for (MultipleLangEntity multipleLangEntity : multipleLangsByDataItemId) {
                multipleLangEntity.setIsDeleted(Boolean.TRUE);
                multipleLangEntity.setDeletedAt(now);
            }
            multipleLangRepository.saveAll(multipleLangsByDataItemId);
        }
        dataItemRepository.saveAll(dataItemEntities);

        DataGroup dataGroup = DataGroupMapper.fromDataGroupEntity(dataGroupEntity);

        handleAutoSyncFirebase(
            Constant.DELETE_ENTERPRISE_DATA,
            businessId,
            dataGroup,
            Constant.EVENT_CODE_DELETE_ENTERPRISE_DATA_REQUEST);
        handleCDCEvent(
            Constant.DELETE_ENTERPRISE_DATA,
            businessId,
            dataGroup,
            EventType.DELETE.getValue(),
            Constant.EVENT_CODE_DELETE_ENTERPRISE_DATA_REQUEST);

        eventTrackingService.traceEvent(
            Constant.DELETE_ENTERPRISE_DATA,
            Constant.EVENT_CODE_DELETE_ENTERPRISE_DATA_RESPONSE,
            "Received a response of delete enterprise data",
            businessId,
            dataGroup);
    }

    @Transactional
    public DataGroup createDataGroup(DataGroup dataGroup) {
        String businessId = String.format(Constant.DATA_GROUP_ID, dataGroup.getGroupCode());
        eventTrackingService.traceEvent(
            Constant.POST_ENTERPRISE_DATA,
            Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
            "Received a request to get enterprise data", businessId, "");

        validationUtill.validateCreateEnterpriseDataRequest(dataGroup, businessId);

        Optional<DataGroupEntity> optionalDataGroupEntity = dataGroupRepository.findByGroupCodeAndGroupName(dataGroup.getGroupCode(), dataGroup.getGroupName());

        if (!optionalDataGroupEntity.isEmpty()) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.DUPLICATE_GROUP_CODE_OR_GROUP_NAME.getCode(),
                ErrorCodes.DUPLICATE_GROUP_CODE_OR_GROUP_NAME.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.DUPLICATE_GROUP_CODE_OR_GROUP_NAME.getCode(),
                ErrorCodes.DUPLICATE_GROUP_CODE_OR_GROUP_NAME.getMessage(),
                businessId, badRequestException);
            throw badRequestException;
        }

        if (Boolean.TRUE.equals(dataGroup.getIncludeValue()) && dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue())) {
            validationUtill.validateDataType(dataGroup.getDataItems(),
                !ObjectUtils.isEmpty(KeyDataType.valueOf(dataGroup.getKeyDataType())),
                !ObjectUtils.isEmpty(KeyDataType.valueOf(dataGroup.getValueDataType())),
                dataGroup.getKeyDataType(), dataGroup.getValueDataType(), businessId);
        }

        DataGroupEntity dataGroupEntity = dataGroupRepository.save(DataGroupMapper.toDataGroupEntity(dataGroup));
        
        DataGroup dataGroupResponse = DataGroupMapper.fromDataGroupEntity(dataGroupEntity);

        handleAutoSyncFirebase(
            Constant.POST_ENTERPRISE_DATA,
            businessId,
            dataGroupResponse,
            Constant.EVENT_CODE_POST_ENTERPRISE_DATA_FIREBASE);
        handleCDCEvent(
            Constant.POST_ENTERPRISE_DATA,
            businessId,
            dataGroupResponse,
            EventType.INSERT.getValue(),
            Constant.EVENT_CODE_CREATE_ENTERPRISE_MASTER_DATA_CDC);

        eventTrackingService.traceEvent(
            Constant.POST_ENTERPRISE_DATA,
            Constant.EVENT_CODE_POST_ENTERPRISE_DATA_RESPONSE,
            "Data group created successfully",
            businessId,
            dataGroupResponse);

        return dataGroupResponse;

    }

    @Transactional
    public DataGroup updateDataGroup(UUID dataGroupId,DataGroup dataGroup) {
        String businessId = String.format(Constant.DATA_GROUP_ID, dataGroupId);
        eventTrackingService.traceEvent(
            Constant.PUT_ENTERPRISE_DATA,
            Constant.EVENT_CODE_PUT_ENTERPRISE_DATA_REQUEST,
            "Received a request to update enterprise data", businessId, dataGroup);

        validationUtill.validateUpdateEnterpriseDataRequest(dataGroup, businessId);

        Optional<DataGroupEntity> optionalDataGroupEntityById = dataGroupRepository.findById(dataGroupId);

        if (optionalDataGroupEntityById.isEmpty()) {
            BadRequestException badRequestException = new BadRequestException(
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getCode(),
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getMessage(),
                businessId);
            eventTrackingService.traceError(Constant.PUT_ENTERPRISE_DATA,
                Constant.EVENT_CODE_PUT_ENTERPRISE_DATA_REQUEST,
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getCode(),
                ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getMessage(),
                businessId, badRequestException);
            throw badRequestException;
        }

        Optional<DataGroupEntity> optionalDataGroupEntity = dataGroupRepository.findByGroupCodeAndGroupName(dataGroup.getGroupCode(), dataGroup.getGroupName());
        
        if (optionalDataGroupEntity.isPresent() && !optionalDataGroupEntity.get().getId().equals(dataGroupId)) {
        	 BadRequestException badRequestException = new BadRequestException(
                     ErrorCodes.DUPLICATE_GROUP_CODE_OR_GROUP_NAME.getCode(),
                     ErrorCodes.DUPLICATE_GROUP_CODE_OR_GROUP_NAME.getMessage(),
                     businessId);
             eventTrackingService.traceError(Constant.POST_ENTERPRISE_DATA,
                     Constant.EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST,
                     ErrorCodes.DUPLICATE_GROUP_CODE_OR_GROUP_NAME.getCode(),
                     ErrorCodes.DUPLICATE_GROUP_CODE_OR_GROUP_NAME.getMessage(),
                     businessId, badRequestException);
             throw badRequestException;
        }

        if (Boolean.TRUE.equals(dataGroup.getIncludeValue()) && dataGroup.getDataGroupType().equals(DataGroupType.MultipleItem.getValue())) {
            validationUtill.validateDataType(dataGroup.getDataItems(),
                !ObjectUtils.isEmpty(KeyDataType.valueOf(dataGroup.getKeyDataType())),
                !ObjectUtils.isEmpty(KeyDataType.valueOf(dataGroup.getValueDataType())),
                dataGroup.getKeyDataType(), dataGroup.getValueDataType(), businessId);
        }

        DataGroupEntity dataGroupEntity = DataGroupMapper.updateDataGroupEntity(optionalDataGroupEntityById.get(), dataGroup);

        DataGroupEntity dataGroupEntityFromDb = dataGroupRepository.save(dataGroupEntity);
        
        DataGroup dataGroupResponse = DataGroupMapper.fromDataGroupEntity(dataGroupEntityFromDb);

        handleAutoSyncFirebase(
                Constant.PUT_ENTERPRISE_DATA,
                businessId,
                dataGroupResponse,
                Constant.EVENT_CODE_POST_ENTERPRISE_DATA_FIREBASE);
        handleCDCEvent(
                Constant.PUT_ENTERPRISE_DATA,
                businessId,
                dataGroupResponse,
                EventType.INSERT.getValue(),
                Constant.UPDATE_ENTERPRISE_MASTER_DATA_CDC_EVENT_CODE);

        eventTrackingService.traceEvent(
            Constant.PUT_ENTERPRISE_DATA,
            Constant.EVENT_CODE_PUT_ENTERPRISE_DATA_RESPONSE,
            "Data group updated successfully",
            businessId,
            dataGroup);

        return DataGroupMapper.fromDataGroupEntity(dataGroupEntityFromDb);
    }
    
    public void publishEnterpriseData(String targetId, ListDataGroupIds listDataGroupIds) {
        LOGGER.info("Publish enterprise data with target id: {}", targetId);
        String businessId = String.format("TargetId: %s", targetId);

        for (UUID id : listDataGroupIds.getDataGroupIds()) {
            Optional<DataGroupEntity> optionalDataGroupEntity = dataGroupRepository.findValidDataGroup(id);

            if (optionalDataGroupEntity.isPresent()) {
                LOGGER.info("Publish enterprise data with data group id: {}", id);
                DataGroupEntity dataGroupEntity = optionalDataGroupEntity.get();
                DataGroup dataGroup = convertToDataGroup(DetailsLevel.FULL, dataGroupEntity);

                switch (TargetId.valueOf(targetId)) {
                    case Firebase -> handleFirebase(
                        Constant.PUBLISH_ENTERPRISE_DATA,
                        businessId,
                        dataGroup,
                        Constant.EVENT_CODE_PUBLISH_ENTERPRISE_DATA_REQUEST);
                    case CDCEvent -> handleCDCEvent(
                        Constant.PUBLISH_ENTERPRISE_DATA,
                        businessId,
                        dataGroup,
                        EventType.UPDATE.getValue(),
                        Constant.EVENT_CODE_PUBLISH_ENTERPRISE_DATA_REQUEST);
                    default -> {
                        BadRequestException badRequestException = new BadRequestException(
                            ErrorCodes.INVALID_TARGET_ID.getCode(),
                            ErrorCodes.INVALID_TARGET_ID.getMessage(),
                            businessId);
                        eventTrackingService.traceError(Constant.PUBLISH_ENTERPRISE_DATA,
                            Constant.EVENT_CODE_PUBLISH_ENTERPRISE_DATA_REQUEST,
                            ErrorCodes.INVALID_TARGET_ID.getCode(),
                            ErrorCodes.INVALID_TARGET_ID.getMessage(),
                            businessId, badRequestException);
                        throw badRequestException;
                    }
                }
            } else {
                NotFoundException notFoundException = new NotFoundException(
                    ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getCode(),
                    ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getMessage(),
                    businessId
                );
                eventTrackingService.traceError(
                    Constant.PUBLISH_ENTERPRISE_DATA,
                    Constant.EVENT_CODE_PUBLISH_ENTERPRISE_DATA_REQUEST,
                    ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getCode(),
                    ErrorCodes.DATA_GROUP_NOT_FOUND_ERROR.getMessage(),
                    businessId,
                    notFoundException
                );
            }
        }

        eventTrackingService.traceEvent(
            Constant.PUBLISH_ENTERPRISE_DATA,
            Constant.EVENT_CODE_PUBLISH_ENTERPRISE_DATA_RESPONSE,
            "Data group published successfully",
            businessId,
            ""
        );
    }

    private void handleFirebase(String activityName, String businessId, DataGroup dataGroup, String eventCode) {
        eventTrackingService.traceEvent(
            activityName,
            eventCode,
            "Firebase auto sync enabled. Syncing data group into firebase",
            businessId,
            dataGroup
        );
        remoteConfigUtil.addRemoteConfig(dataGroup, activityName, eventCode);
    }

    private void handleAutoSyncFirebase(String activityName, String businessId, DataGroup dataGroup, String eventCode) {
        if (Boolean.TRUE.equals(autoSyncFireBase)) {
            handleFirebase(activityName, businessId, dataGroup, eventCode);
        }
    }

    private void handleCDCEvent(String activityName, String businessId, DataGroup dataGroup, String eventType, String eventCode) {
        if (Boolean.TRUE.equals(cdcEventsEnabled)) {
            eventTrackingService.traceEvent(
                activityName,
                eventCode,
                "CDC event enabled. Publishing CDC event to topic", businessId, "");
            changeDataEventPublisherUtill.publishEnterpriseMasterDataChangeEvent(
                dataGroup,
                eventType,
                dataGroup,
                activityName,
                eventCode,
                MembershipUtils.getUserId()
            );
        }
    }

    public void emitCDCEventUponDataLoaded(EntDataLoaded entDataLoaded) {
        if (Boolean.TRUE.equals(cdcEventsDataLoader) && EventStatus.SUCCESS.equals(entDataLoaded.getStatus())) {
            List<DataGroupEntity> dataGroupEntities = dataGroupRepository.findValidDataGroups();
            for (DataGroupEntity dataGroupEntity : dataGroupEntities) {
                LOGGER.info("Emit CDC Event Upon Data Loaded: {}", dataGroupEntity.getId());
                DataGroup dataGroup = convertToDataGroup(DetailsLevel.FULL, dataGroupEntity);
                handleCDCEvent(
                    Constant.EMIT_ENTERPRISE_DATA,
                    String.format(Constant.DATA_GROUP_ID, dataGroup.getId()),
                    dataGroup,
                    EventType.INSERT.getValue(),
                    Constant.EVENT_CODE_EMIT_ENTERPRISE_MASTER_DATA_CDC_REQUEST);
            }
        }
    }

    public void pushDataGroupUponCDCEvent(DataGroup dataGroup) {
        String businessId = String.format(Constant.DATA_GROUP_ID, dataGroup.getId());
        Optional<DataGroupEntity> optionalDataGroupEntity = dataGroupRepository.findValidDataGroup(dataGroup.getId());
        if (optionalDataGroupEntity.isPresent() && Boolean.TRUE.equals(autoSyncFirebaseUponCDC)) {
            LOGGER.info("Push Data Group Upon CDC Event: {}", dataGroup.getId());
            handleFirebase(
                Constant.PUSH_ENTERPRISE_DATA,
                businessId,
                dataGroup,
                Constant.EVENT_CODE_PUSH_ENTERPRISE_MASTER_DATA_CDC_REQUEST
            );
        }
    }
}
