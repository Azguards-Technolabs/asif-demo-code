package io.azguards.services.enterprise.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.azguards.services.enterprise.data.entity.DataItemEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataItemRepository extends JpaRepository<DataItemEntity, UUID> {

    @Query("SELECT di FROM DataItemEntity di WHERE di.dataGroup.id = :groupId AND di.isDeleted = false")
    List<DataItemEntity> findValidDataItemsByDataGroupId(@Param("groupId") UUID groupId);
}
