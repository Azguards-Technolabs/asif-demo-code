package io.azguards.services.enterprise.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import io.azguards.services.enterprise.data.common.DataGroupType;
import io.azguards.services.enterprise.data.common.KeyDataType;
import io.azguards.services.enterprise.data.common.ValueDataType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"dataItems","multipleLangs"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data_group")
public class DataGroupEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(name = "group_code")
    private String groupCode;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "default_language")
    private String defaultLanguage;

    @Column(name = "include_value")
    private Boolean includeValue;

    @Column(name = "include_key")
    private Boolean includeKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_data_type")
    private ValueDataType valueDataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_data_type")
    private KeyDataType keyDataType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "platform_id")
    private String platformId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_group_type")
    private DataGroupType dataGroupType;

    @Column(name = "group_value")
    private String groupValue;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Builder.Default
    @OneToMany(mappedBy = "dataGroup", orphanRemoval = true, cascade = {CascadeType.ALL})
    private Set<DataItemEntity> dataItems  = new HashSet<DataItemEntity>();

    @Builder.Default
    @OneToMany(mappedBy = "dataGroup", orphanRemoval = true, cascade = {CascadeType.ALL})
    private Set<MultipleLangEntity> multipleLangs = new HashSet<MultipleLangEntity>();
    
	public void addMultipleLangEntity(MultipleLangEntity multipleLangEntity) {
		this.multipleLangs.add(multipleLangEntity);
	}

	public void addDataItem(DataItemEntity dataItemEntity) {
		this.dataItems.add(dataItemEntity);
	}

	public void removeMultipleLangEntity(MultipleLangEntity multipleLangEntity) {
		this.multipleLangs.remove(multipleLangEntity);
	}

	public void removeDataItem(DataItemEntity dataItemEntity) {
		this.dataItems.remove(dataItemEntity);
	}
}
