package io.azguards.services.enterprise.data.consumer;

import io.azguards.services.enterprise.data.common.Constant;
import io.azguards.services.enterprise.data.common.ErrorCodes;
import io.azguards.services.enterprise.data.dto.EntDataLoaded;
import io.azguards.services.enterprise.data.service.DataGroupService;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.model.event.AsyncRequestScopeAttributes;
import io.marketplace.commons.model.event.EventMessage;
import io.marketplace.commons.model.event.EventStatus;
import io.marketplace.commons.utils.EventMessageUtils;
import io.marketplace.services.enterprise.data.model.DataGroup;
import io.marketplace.services.pxchange.client.service.PXChangeServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EntDataGroupPushConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntDataGroupPushConsumer.class);
    private final EventMessageUtils eventMessageUtils;
    private final DataGroupService dataGroupService;
    private final PXChangeServiceClient pxChangeServiceClient;

    @Value("${spring.application.name}")
    private String applicationName;

    @KafkaListener(topics = "${kafka.producer.enterprise-master-data-change-topic:enterprise-master-data-changed}",
        groupId = "${kafka.consumer.ent-data-group-push.group-id:ent-data-group-push-group}",
        containerFactory = "kafkaListenerEntDataLoadedContainerFactory")
    public void listenProcessEventMessage(@Payload String message) {
        LOGGER.info("Received the Enterprise Data Group CDC event message: {}", message);
        EventMessage<DataGroup> eventMessage = eventMessageUtils.fromQueueMessage(message, DataGroup.class);
        RequestContextHolder.setRequestAttributes(new AsyncRequestScopeAttributes());
        String eventTraceId = UUID.randomUUID().toString();
        MDC.put("eventTraceId", eventTraceId);
        try {
            EventMessage<DataGroup> eventRequest = EventMessage.<DataGroup>builder()
                .activityName(Constant.PUSH_ENTERPRISE_DATA)
                .eventCode(Constant.EVENT_CODE_PUSH_ENTERPRISE_MASTER_DATA_CDC_REQUEST)
                .eventTitle("Kafka Consumer - Received the request to push enterprise data")
                .eventUser(eventMessage.getEventUser())
                .businessData(eventMessage.getBusinessData())
                .eventBusinessId(eventMessage.getEventBusinessId())
                .eventSource(applicationName)
                .eventTraceId(eventTraceId)
                .eventStatus(EventStatus.SUCCESS)
                .build();
            pxChangeServiceClient.addEvent(eventRequest);
            dataGroupService.pushDataGroupUponCDCEvent(eventMessage.getBusinessData());
            EventMessage<DataGroup> eventResponse = EventMessage.<DataGroup>builder()
                .activityName(Constant.PUSH_ENTERPRISE_DATA)
                .eventCode(Constant.EVENT_CODE_PUSH_ENTERPRISE_MASTER_DATA_CDC_RESPONSE)
                .eventTitle("Kafka Consumer - Received the response of push enterprise data")
                .eventUser(eventMessage.getEventUser())
                .businessData(eventMessage.getBusinessData())
                .eventBusinessId(eventMessage.getEventBusinessId())
                .eventSource(applicationName)
                .eventTraceId(eventTraceId)
                .eventStatus(EventStatus.SUCCESS)
                .build();
            pxChangeServiceClient.addEvent(eventResponse);
        } catch (Exception ex) {
            LOGGER.errorLog(
                String.format("%s: %s", ErrorCodes.PROCESS_MESSAGE.getMessage(), ex.getMessage()),
                EventMessage.builder().errorCode(ErrorCodes.PROCESS_MESSAGE.getMessage()).build(),
                ex);
            EventMessage<String> eventError = EventMessage.<String>builder()
                .activityName(Constant.PUSH_ENTERPRISE_DATA)
                .eventCode(Constant.EVENT_CODE_PUSH_ENTERPRISE_MASTER_DATA_CDC_RESPONSE)
                .eventTitle(ErrorCodes.PROCESS_MESSAGE.getMessage())
                .eventUser(eventMessage.getEventUser())
                .businessData(ex.getMessage())
                .eventBusinessId(eventMessage.getEventBusinessId())
                .eventSource(applicationName)
                .eventTraceId(eventTraceId)
                .errorCode(ErrorCodes.PROCESS_MESSAGE.getCode())
                .eventStatus(EventStatus.FAILED)
                .build();
            pxChangeServiceClient.addEvent(eventError);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
