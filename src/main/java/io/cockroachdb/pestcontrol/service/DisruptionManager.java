package io.cockroachdb.pestcontrol.service;

import io.cockroachdb.pestcontrol.schema.ClusterProperties;
import io.cockroachdb.pestcontrol.schema.ClusterType;
import io.cockroachdb.pestcontrol.schema.NodeModel;
import io.cockroachdb.pestcontrol.schema.nodes.Locality;

public interface DisruptionManager {
    boolean supports(ClusterType clusterType);

    void disruptNode(ClusterProperties clusterProperties, NodeModel nodeModel);

    void recoverNode(ClusterProperties clusterProperties, NodeModel nodeModel);

    void disruptLocality(ClusterProperties clusterProperties, Locality locality);

    void recoverLocality(ClusterProperties clusterProperties, Locality locality);
}
