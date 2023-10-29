package io.azguards.services.enterprise.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.azguards.services.enterprise.data.entity.MultipleLangEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface MultipleLangRepository extends JpaRepository<MultipleLangEntity, UUID> {
    @Query("SELECT ml FROM MultipleLangEntity ml WHERE ml.dataItem.id = :itemId AND ml.isDeleted = false")
    List<MultipleLangEntity> findValidMultipleLangsByDataItemId(@Param("itemId") UUID itemId);
    
    @Query("SELECT ml FROM MultipleLangEntity ml WHERE ml.dataGroup.id = :dataGroupId AND ml.isDeleted = false")
    List<MultipleLangEntity> findValidMultipleLangsByDataGroupId(@Param("dataGroupId") UUID dataGroupId);
}
