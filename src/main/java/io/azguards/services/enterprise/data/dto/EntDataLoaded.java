package io.azguards.services.enterprise.data.dto;

import com.google.gson.annotations.SerializedName;
import io.marketplace.commons.model.event.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntDataLoaded {
    @SerializedName("id")
    private UUID id;
    @SerializedName("uploadType")
    private String uploadType;
    @SerializedName("uploadFileName")
    private String uploadFileName;
    @SerializedName("dataset")
    private String dataset;
    @SerializedName("status")
    private EventStatus status;
    @SerializedName("createdAt")
    private LocalDateTime createdAt;
    @SerializedName("updatedAt")
    private LocalDateTime updatedAt;
    @SerializedName("inputFileName")
    private String inputFileName;
    @SerializedName("resultFileName")
    private String resultFileName;
}
