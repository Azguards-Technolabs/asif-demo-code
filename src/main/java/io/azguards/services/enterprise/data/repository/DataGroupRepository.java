package io.azguards.services.enterprise.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.azguards.services.enterprise.data.entity.DataGroupEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataGroupRepository extends JpaRepository<DataGroupEntity, UUID> {

    @Query("SELECT dg FROM DataGroupEntity dg WHERE dg.isDeleted = false")
    List<DataGroupEntity> findValidDataGroups();

    @Query("SELECT dg FROM DataGroupEntity dg WHERE dg.groupCode = :groupCode OR dg.groupName = :groupName AND dg.isDeleted = false")
	Optional<DataGroupEntity> findByGroupCodeAndGroupName(@Param("groupCode") String groupCode, @Param("groupName") String groupName);

    @Query("SELECT dg FROM DataGroupEntity dg WHERE dg.id = :groupId AND dg.isDeleted = false")
    Optional<DataGroupEntity> findValidDataGroup(@Param("groupId") UUID groupId);

    @Query("SELECT DISTINCT dg FROM DataGroupEntity dg " +
        "WHERE dg.isDeleted = false " +
        "AND (:searchText IS NULL OR :searchText = '' OR LOWER(dg.groupName) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
        "AND (:appId IS NULL OR :appId = '' OR dg.appId = :appId) " +
        "AND (:entityId IS NULL OR :entityId = '' OR dg.entityId = :entityId) " +
        "AND (:platformId IS NULL OR :platformId = '' OR dg.platformId = :platformId) " +
        "AND (COALESCE(:dataGroupCodes, NULL) IS NULL OR dg.groupCode IN (:dataGroupCodes))")
    Page<DataGroupEntity> findDataGroupByMultipleConditions(
        Pageable pageable,
        @Param("searchText") String searchText,
        @Param("appId") String appId,
        @Param("entityId") String entityId,
        @Param("platformId") String platformId,
        @Param("dataGroupCodes") List<String> dataGroupCodes
    );

}
