package io.azguards.services.enterprise.data.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntDataGroupLoaded {
    @SerializedName("dataGroupId")
    private UUID dataGroupId;
}
