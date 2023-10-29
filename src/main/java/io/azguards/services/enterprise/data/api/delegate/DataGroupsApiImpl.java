package io.azguards.services.enterprise.data.api.delegate;

import io.azguards.services.enterprise.data.service.DataGroupService;
import io.marketplace.services.enterprise.data.api.DataGroupsApiDelegate;
import io.marketplace.services.enterprise.data.model.DataGroup;
import io.marketplace.services.enterprise.data.model.DataGroupResponse;
import io.marketplace.services.enterprise.data.model.ListDataGroupIds;
import io.marketplace.services.enterprise.data.model.ListDataGroupResponse;
import io.marketplace.services.enterprise.data.model.PagingInformation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataGroupsApiImpl implements DataGroupsApiDelegate {

    private final DataGroupService dataGroupService;

    @Override
    public ResponseEntity<DataGroupResponse> getEnterpriseData(UUID dataGroupId) {
        return ResponseEntity.ok(
            DataGroupResponse.builder()
                .data(dataGroupService.getByDataGroupId(dataGroupId))
                .build());
    }

    @Override
    public ResponseEntity<DataGroupResponse> createEnterpriseData(DataGroup dataGroup) {
        return ResponseEntity.ok(
            DataGroupResponse.builder()
                .data(dataGroupService.createDataGroup(dataGroup))
                .build());
    }

    @Override
    public ResponseEntity<DataGroupResponse> updateEnterpriseData(UUID dataGroupId, DataGroup dataGroup) {
        return ResponseEntity.ok(
                DataGroupResponse.builder()
                    .data(dataGroupService.updateDataGroup(dataGroupId,dataGroup))
                    .build());
    }

    @Override
    public ResponseEntity<ListDataGroupResponse> searchEnterpriseData(
        String searchText,
        String appId,
        String entityId,
        String platformId,
        List<String> dataGroupCodes,
        String detailsLevel,
        Long pageNumber,
        Long pageSize,
        List<String> sort) {
        List<DataGroup> dataGroups = dataGroupService.searchDataGroup(
            searchText,
            appId,
            entityId,
            platformId,
            dataGroupCodes,
            detailsLevel,
            pageNumber,
            pageSize,
            sort
        );
        PagingInformation paging =
            PagingInformation.builder()
                .pageNumber(Math.toIntExact(pageNumber))
                .pageSize(Math.toIntExact(pageSize))
                .totalRecords((long) dataGroups.size())
                .build();
        return ResponseEntity.ok(
            ListDataGroupResponse.builder()
                .paging(paging)
                .data(dataGroups)
                .build());
    }

    @Override
    public ResponseEntity<Void> deleteEnterpriseData(UUID dataGroupId) {
        dataGroupService.deleteEnterpriseData(dataGroupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> publishEnterpriseData(String targetId, ListDataGroupIds listDataGroupIds) {
        dataGroupService.publishEnterpriseData(targetId, listDataGroupIds);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
