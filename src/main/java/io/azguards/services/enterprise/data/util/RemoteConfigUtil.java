package io.azguards.services.enterprise.data.util;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException;
import com.google.firebase.remoteconfig.Template;

import io.azguards.services.enterprise.data.common.ErrorCodes;
import io.azguards.services.enterprise.data.mapper.FireBaseTemplateMapper;
import io.azguards.services.enterprise.data.service.EventTrackingService;
import io.marketplace.commons.exception.BadRequestException;
import io.marketplace.commons.exception.InternalServerErrorException;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.services.enterprise.data.model.DataGroup;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RemoteConfigUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConfigUtil.class);

	@Value("${ent-data.firebase-config-name-prefix:}")
	private String firebaseConfigPrefix;
	
	private final EventTrackingService eventTrackingService;
	private final FireBaseTemplateMapper fireBaseTemplateMapper;
	private static final String SYSTEM = "SYSTEM";
	
	public void addRemoteConfig(DataGroup dataGroup, String activityName, String eventCode) {
		try {
			Template template = FirebaseRemoteConfig.getInstance().getTemplateAsync().get();
			
			if (!ObjectUtils.isEmpty(template.getParameters().get(dataGroup.getGroupCode()))) {
				BadRequestException badRequestException = new BadRequestException(
						ErrorCodes.DATA_GROUP_PRESENT_IN_FIREBASE.getCode(),
						ErrorCodes.DATA_GROUP_PRESENT_IN_FIREBASE.getMessage(), null);
				eventTrackingService.traceError(activityName,
						eventCode,
						ErrorCodes.DATA_GROUP_PRESENT_IN_FIREBASE.getCode(),
						ErrorCodes.DATA_GROUP_PRESENT_IN_FIREBASE.getMessage(), null, badRequestException);
				throw badRequestException;
			}
			
			template.getParameters()
					.put(buildFirebaseConfigKey(dataGroup.getGroupCode(), dataGroup.getEntityId(),
							dataGroup.getAppId(), dataGroup.getPlatformId()),
							fireBaseTemplateMapper.toParameter(dataGroup));
			
			FirebaseRemoteConfig.getInstance().publishTemplate(template);
			
		} catch (InterruptedException | ExecutionException | FirebaseRemoteConfigException ex) {
			InternalServerErrorException internalServerErrorException = new InternalServerErrorException(
					ErrorCodes.DATA_GROUP_INSERT_IN_FIREBASE.getCode(),
					ErrorCodes.DATA_GROUP_INSERT_IN_FIREBASE.getMessage(), null);
			eventTrackingService.traceError(activityName,
					eventCode,
					ErrorCodes.DATA_GROUP_INSERT_IN_FIREBASE.getCode(),
					ErrorCodes.DATA_GROUP_INSERT_IN_FIREBASE.getMessage(), null, internalServerErrorException);
			LOGGER.error(ex.getMessage());
			throw internalServerErrorException;
		}
	}
	
	private String buildFirebaseConfigKey(String groupCode, String entityId, String appId, String platformId) {
		StringBuilder configKey = new StringBuilder();
		
		if (!StringUtils.isEmpty(firebaseConfigPrefix)) {
			configKey.append(firebaseConfigPrefix).append("_");
		}
		configKey.append(groupCode);
		
		if (!StringUtils.isEmpty(entityId) && !entityId.equalsIgnoreCase(SYSTEM)) {
			configKey.append("_").append(entityId);
		}
		if (!StringUtils.isEmpty(appId) && !appId.equalsIgnoreCase(SYSTEM)) {
			configKey.append("_").append(appId);
		}
		if (!StringUtils.isEmpty(platformId) && !platformId.equalsIgnoreCase(SYSTEM)) {
			configKey.append("_").append(platformId);
		}
		return configKey.toString();
	}
}
