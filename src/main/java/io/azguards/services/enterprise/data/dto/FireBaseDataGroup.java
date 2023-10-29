package io.azguards.services.enterprise.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class FireBaseDataGroup {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("groupCode")
    private String groupCode;

    @JsonProperty("groupName")
    private String groupName;

    @JsonProperty("defaultLanguage")
    private String defaultLanguage;

    @JsonProperty("includeKey")
    private Boolean includeKey;

    @JsonProperty("includeValue")
    private Boolean includeValue;

    @JsonProperty("keyDataType")
    private String keyDataType;

    @JsonProperty("valueDataType")
    private String valueDataType;

    @JsonProperty("dataGroupType")
    private String dataGroupType;

    @JsonProperty("groupValue")
    private String groupValue;

    @JsonProperty("isDeleted")
    private Boolean isDeleted;

    @JsonProperty("dataItems")
    private List<FireBaseDataItem> dataItems = null;
}
