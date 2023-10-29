package io.azguards.services.enterprise.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class FireBaseDataItem {

	@JsonProperty("id")
	private UUID id;

	@JsonProperty("key")
	private String key;

	@JsonProperty("value")
	private String value;

	@JsonProperty("isDefault")
	private Boolean isDefault;

	@JsonProperty("isDeleted")
	private Boolean isDeleted;

	@JsonProperty("sortOrder")
	private Integer sortOrder;
}
