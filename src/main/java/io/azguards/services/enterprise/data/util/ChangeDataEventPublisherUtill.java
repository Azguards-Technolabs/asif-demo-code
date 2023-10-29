package io.azguards.services.enterprise.data.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.azguards.services.enterprise.data.common.Constant;
import io.marketplace.commons.model.event.EventCategory;
import io.marketplace.commons.model.event.EventMessage;
import io.marketplace.commons.model.event.EventType;
import io.marketplace.commons.utils.MembershipUtils;
import io.marketplace.services.enterprise.data.model.DataGroup;
import io.marketplace.services.pxchange.client.service.EventServiceClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChangeDataEventPublisherUtill {
	
    private final EventServiceClient eventClient;
	
    @Value("${kafka.producer.enterprise-master-data-change-topic:enterprise-master-data-changed}")
    private String enterpriseMasterDataChangeTopic;

    public void publishEnterpriseMasterDataChangeEvent(DataGroup dataGroup, String eventType,DataGroup originalDataGroup, String activityName, String eventCode, String dataOwnerId) {

        EventMessage<DataGroup> changeDataEvent = eventClient.buildEventMessage(dataGroup,
            EventCategory.CDC_EVENT);
        changeDataEvent.setEventType(EventType.valueOf(eventType));
        changeDataEvent.setEventEntity(Constant.ENTERPRISE_MASTER_DATA_EVENT_ENTITY);
        changeDataEvent.setDataOwnerId(MembershipUtils.getUserId());
        changeDataEvent.setBusinessData(dataGroup);
        changeDataEvent.setOriginalBusinessData(originalDataGroup);
        changeDataEvent.setEventBusinessId(dataGroup.getGroupCode());     
        changeDataEvent.setEventTitle(activityName);
        changeDataEvent.setEventUser(MembershipUtils.getUserId());
        changeDataEvent.setEventCode(eventCode);
        this.eventClient.addEvent(enterpriseMasterDataChangeTopic, changeDataEvent);
    }
}
