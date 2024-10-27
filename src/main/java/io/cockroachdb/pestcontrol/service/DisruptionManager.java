package io.cockroachdb.pestcontrol.service;

import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.model.NodeModel;

public interface DisruptionManager {
    boolean supports(ClusterType clusterType);

    void disruptNode(ClusterProperties clusterProperties, NodeModel nodeModel);

    void recoverNode(ClusterProperties clusterProperties, NodeModel nodeModel);

    void disruptRegion(ClusterProperties clusterProperties, String regionName);

    void recoverRegion(ClusterProperties clusterProperties, String regionName);
}
