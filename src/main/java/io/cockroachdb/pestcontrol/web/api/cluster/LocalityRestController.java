package io.cockroachdb.pestcontrol.web.api.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.schema.ClusterModel;
import io.cockroachdb.pestcontrol.schema.ClusterType;
import io.cockroachdb.pestcontrol.schema.LocalityModel;
import io.cockroachdb.pestcontrol.schema.nodes.Locality;
import io.cockroachdb.pestcontrol.service.ClusterManager;

@RestController
@RequestMapping("/api/cluster")
public class LocalityRestController {
    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ClusterRestController clusterRestController;

    @GetMapping("/{id}/locality")
    public ResponseEntity<CollectionModel<LocalityModel>> getLocalities(@PathVariable("id") String id) {
        ClusterModel clusterModel = clusterRestController.getCluster(id).getBody();
        return ResponseEntity.ok(clusterModel.getLocalities());
    }

    @GetMapping("/{id}/locality/{tiers}")
    public ResponseEntity<LocalityModel> getLocality(@PathVariable("id") String id,
                                                     @PathVariable("tiers") String tiers
    ) {
        Locality locality = Locality.fromTiers(tiers);
        ClusterType clusterType = clusterManager.getClusterType(id);
        return ResponseEntity.ok(new LocalityModelAssembler(id, clusterType).toModel(locality));
    }

    @PostMapping("/{id}/locality/{tiers}/disrupt")
    public ResponseEntity<Void> disruptLocalityTier(@PathVariable("id") String clusterId,
                                                    @PathVariable("tiers") String tiers) {
        clusterManager.disruptLocality(clusterId, tiers);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/locality/{tiers}/recover")
    public ResponseEntity<Void> recoverLocalityTier(@PathVariable("id") String clusterId,
                                                    @PathVariable("tiers") String tiers) {
        clusterManager.recoverLocality(clusterId, tiers);
        return ResponseEntity.ok().build();
    }

}
