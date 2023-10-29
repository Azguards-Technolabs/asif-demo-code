package io.azguards.services.enterprise.data.common;

import java.util.Arrays;
import java.util.List;

public class Constant {

    public static final List<String> SORTING_KEYS = Arrays.asList("createdAt", "updatedAt", "groupCode", "groupName", "-createdAt", "-updatedAt", "-groupCode", "-groupName");
    public static final String DATA_GROUP_ID = "Data group id: %s";
    public static final String ENTERPRISE_MASTER_DATA_EVENT_ENTITY = "EnterpriseMasterData";
    public static final String GET_ENTERPRISE_DATA = "Get Enterprise Data";
    public static final String POST_ENTERPRISE_DATA = "Create Enterprise Data";
    public static final String SEARCH_ENTERPRISE_DATA = "Search Enterprise Data";
    public static final String DELETE_ENTERPRISE_DATA = "Delete Enterprise Data";
    public static final String PUT_ENTERPRISE_DATA = "Update Enterprise Data";
    public static final String PUBLISH_ENTERPRISE_DATA = "Publish Enterprise Data";
    public static final String PUSH_ENTERPRISE_DATA = "Push Enterprise Data";
    public static final String EMIT_ENTERPRISE_DATA = "Emit Enterprise Data";
    public static final String EVENT_CODE_GET_ENTERPRISE_DATA_REQUEST = "GET.ENTERPRISE.DATA.REQUEST";
    public static final String EVENT_CODE_GET_ENTERPRISE_DATA_RESPONSE = "GET.ENTERPRISE.DATA.RESPONSE";
    public static final String EVENT_CODE_POST_ENTERPRISE_DATA_REQUEST = "POST.ENTERPRISE.DATA.REQUEST";
    public static final String EVENT_CODE_POST_ENTERPRISE_DATA_RESPONSE = "POST.ENTERPRISE.DATA.RESPONSE";
    public static final String EVENT_CODE_CREATE_ENTERPRISE_MASTER_DATA_CDC = "POST.ENTERPRISE.MASTER.DATA.CDC.EVENT";
    public static final String EVENT_CODE_UPDATE_ENTERPRISE_MASTER_DATA_CDC = "PUT.ENTERPRISE.MASTER.DATA.CDC.EVENT";
    public static final String EVENT_CODE_EMIT_ENTERPRISE_MASTER_DATA_CDC_REQUEST = "EMIT.ENTERPRISE.MASTER.DATA.CDC.EVENT.REQUEST";
    public static final String EVENT_CODE_EMIT_ENTERPRISE_MASTER_DATA_CDC_RESPONSE = "EMIT.ENTERPRISE.MASTER.DATA.CDC.EVENT.RESPONSE";
    public static final String EVENT_CODE_PUSH_ENTERPRISE_MASTER_DATA_CDC_REQUEST = "PUSH.ENTERPRISE.MASTER.DATA.CDC.EVENT.REQUEST";
    public static final String EVENT_CODE_PUSH_ENTERPRISE_MASTER_DATA_CDC_RESPONSE = "PUSH.ENTERPRISE.MASTER.DATA.CDC.EVENT.RESPONSE";
    public static final String EVENT_CODE_SEARCH_ENTERPRISE_DATA_REQUEST = "SEARCH.ENTERPRISE.DATA.REQUEST";
    public static final String EVENT_CODE_SEARCH_ENTERPRISE_DATA_RESPONSE = "SEARCH.ENTERPRISE.DATA.RESPONSE";
    public static final String EVENT_CODE_DELETE_ENTERPRISE_DATA_REQUEST = "DELETE.ENTERPRISE.DATA.REQUEST";
    public static final String EVENT_CODE_DELETE_ENTERPRISE_DATA_RESPONSE = "DELETE.ENTERPRISE.DATA.RESPONSE";
    public static final String EVENT_CODE_PUBLISH_ENTERPRISE_DATA_REQUEST = "PUBLISH.ENTERPRISE.DATA.REQUEST";
    public static final String EVENT_CODE_PUBLISH_ENTERPRISE_DATA_RESPONSE = "PUBLISH.ENTERPRISE.DATA.RESPONSE";
    public static final String SASL_JAAS_CONFIG_KEY = "sasl.jaas.config";
    public static final String SECURITY_PROTOCOL_CONFIG_KEY = "SASL_PLAINTEXT";
    public static final String SSL_ENDPOINT_ALG_CONFIG_KEY = "ssl.endpoint.identification.algorithm";
    public static final String EVENT_CODE_POST_ENTERPRISE_DATA_FIREBASE = "POST.ENTERPRISE.DATA.FIREBASE.EVENT";
    public static final String EVENT_CODE_PUT_ENTERPRISE_DATA_REQUEST = "PUT.ENTERPRISE.DATA.REQUEST";
    public static final String EVENT_CODE_PUT_ENTERPRISE_DATA_RESPONSE = "PUT.ENTERPRISE.DATA.RESPONSE";
    public static final String UPDATE_ENTERPRISE_MASTER_DATA_CDC_EVENT_CODE = "PUSH.ENTERPRISE.MASTER.DATA.CDC.EVENT";

}
