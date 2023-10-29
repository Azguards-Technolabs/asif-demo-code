package io.azguards.services.enterprise.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"multipleLangs"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data_item")
public class DataItemEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private DataGroupEntity dataGroup;

    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "raw_data")
    private String rawData;

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
    @OneToMany(mappedBy = "dataItem", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<MultipleLangEntity> multipleLangs = new HashSet<>();
}
