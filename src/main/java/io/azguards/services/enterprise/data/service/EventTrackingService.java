package io.azguards.services.enterprise.data.service;

import io.marketplace.commons.model.CustomRequestContext;
import io.marketplace.commons.model.event.EventCategory;
import io.marketplace.commons.model.event.EventMessage;
import io.marketplace.commons.model.event.EventStatus;
import io.marketplace.commons.utils.MembershipUtils;
import io.marketplace.commons.utils.StringUtils;
import io.marketplace.commons.utils.ThreadContextUtils;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventTrackingService {

    public static final String SYSTEM = "SYSTEM";

    @Value("${spring.application.id:}")
    private String serviceId;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.entity-id:SYSTEM}")
    private String entityId;


    private final PXChangeServiceClient pxClient;

    @Autowired
    public EventTrackingService(PXChangeServiceClient pxClient) {
        this.pxClient = pxClient;
    }


    public <T> void traceEvent(String activityName, String eventCode,
                               String eventTitle,
                               String businessId,
                               T businessData) {
        pxClient.addEvent(EventMessage.<T>builder()
            .appId(SYSTEM)
            .eventCategory(EventCategory.EVENT)
            .eventUser(MembershipUtils.getUserId())
            .eventTitle(eventTitle)
            .eventBusinessId(businessId)
            .eventCode(eventCode)
            .activityName(activityName)
            .entityId(entityId)
            .eventSource(applicationName)
            .eventStatus(EventStatus.SUCCESS)
            .businessData(businessData).build());
    }

    public void traceError(String activityName, String eventCode, String errorCode,
                           String errorMessage, String businessId, Exception ex) {
        traceError(activityName, eventCode,
            errorCode,
            errorMessage,
            businessId,
            ex != null ? ex.getMessage() : null);
    }

    public <T> void traceError(String activityName, String eventCode, String errorCode,
                               String eventTitle,
                               String businessId,
                               T businessData) {

        pxClient.addEvent(EventMessage.<T>builder()
            .errorCode(errorCode)
            .appId(SYSTEM)
            .eventCategory(EventCategory.ERROR)
            .eventUser(MembershipUtils.getUserId())
            .eventTitle(eventTitle)
            .eventBusinessId(businessId)
            .errorCode(errorCode)
            .activityName(activityName)
            .businessData(businessData)
            .eventCode(eventCode)
            .entityId(entityId)
            .eventSource(applicationName)
            .eventStatus(EventStatus.FAILED).build());
    }

    public void initProcessorContext(String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        CustomRequestContext customRequestContext = CustomRequestContext.builder()
            .requestId(requestId)
            .serviceId(serviceId)
            .serviceName(applicationName)
            .build();

        ThreadContextUtils.setCustomRequest(customRequestContext);
    }
}

