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
public class EntDataLoadedConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntDataLoadedConsumer.class);

    private final DataGroupService dataGroupService;
    private final EventMessageUtils eventMessageUtils;
    private final PXChangeServiceClient pxChangeServiceClient;

    @Value("${spring.application.name}")
    private String applicationName;

    @KafkaListener(topics = "${kafka.consumer.ent-data-loaded-topic:ent-data-loaded}",
        groupId = "${kafka.consumer.ent-data-loaded.group-id:ent-data-loaded-group}",
        containerFactory = "kafkaListenerEntDataLoadedContainerFactory")
    public void listenProcessEventMessage(@Payload String message) {
        LOGGER.info("Received the Enterprise Data Loaded event message: {}", message);
        EventMessage<EntDataLoaded> eventMessage = eventMessageUtils.fromQueueMessage(message, EntDataLoaded.class);
        RequestContextHolder.setRequestAttributes(new AsyncRequestScopeAttributes());
        String eventTraceId = UUID.randomUUID().toString();
        MDC.put("eventTraceId", eventTraceId);
        try {
            EventMessage<EntDataLoaded> eventRequest = EventMessage.<EntDataLoaded>builder()
                .activityName(Constant.EMIT_ENTERPRISE_DATA)
                .eventCode(Constant.EVENT_CODE_EMIT_ENTERPRISE_MASTER_DATA_CDC_REQUEST)
                .eventTitle("Kafka Consumer - Received the request to emit enterprise data")
                .eventUser(eventMessage.getEventUser())
                .businessData(eventMessage.getBusinessData())
                .eventBusinessId(eventMessage.getEventBusinessId())
                .eventSource(applicationName)
                .eventTraceId(eventTraceId)
                .eventStatus(EventStatus.SUCCESS)
                .build();
            pxChangeServiceClient.addEvent(eventRequest);
            dataGroupService.emitCDCEventUponDataLoaded(eventMessage.getBusinessData());
            EventMessage<EntDataLoaded> eventResponse = EventMessage.<EntDataLoaded>builder()
                .activityName(Constant.EMIT_ENTERPRISE_DATA)
                .eventCode(Constant.EVENT_CODE_EMIT_ENTERPRISE_MASTER_DATA_CDC_RESPONSE)
                .eventTitle("Kafka Consumer - Received the response of emit enterprise data")
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
                .activityName(Constant.EMIT_ENTERPRISE_DATA)
                .eventCode(Constant.EVENT_CODE_EMIT_ENTERPRISE_MASTER_DATA_CDC_RESPONSE)
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
