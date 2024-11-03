package io.cockroachdb.pestcontrol.web.api.cluster;

import io.cockroachdb.pestcontrol.schema.ClusterModel;
import io.cockroachdb.pestcontrol.schema.status.NodeStatus;

public class ClusterHelper {
    private boolean available;

    private ClusterModel clusterModel;

    public ClusterHelper(boolean available) {
        this.available = available;
    }

    public String getId() {
        return clusterModel.getId();
    }

    public ClusterModel getClusterModel() {
        return clusterModel;
    }

    public boolean isDifferent(ClusterModel clusterModel) {
        int currentNodeSize = this.clusterModel.getNodes().getContent().size();
        int nodeSize = clusterModel.getNodes().getContent().size();
        return currentNodeSize!=nodeSize;
    }

    public ClusterHelper setClusterModel(ClusterModel clusterModel) {
        this.clusterModel = clusterModel;
        return this;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getCardClass(NodeStatus nodeStatus) {
        if (!isAvailable()) {
            return "border-warning alert-warning";
        }

        boolean isLive = "true".equals(nodeStatus.getIsLive());  // up and running
        boolean isAvailable = "true".equals(nodeStatus.getIsAvailable()); // part of quorum
        if (isLive) {
            if (isAvailable) {
                return "border-success alert-success";
            }
            return "border-warning alert-warning";
        } else {
            if (isAvailable) {
                return "border-warning alert-warning";
            }
            return "border-danger alert-danger";
        }
    }

    public String getCardImage(NodeStatus nodeStatus) {
        if (!isAvailable()) {
            return "#db-warn";
        }

        boolean isLive = "true".equals(nodeStatus.getIsLive());  // up and running
        boolean isAvailable = "true".equals(nodeStatus.getIsAvailable()); // part of quorum
        if (isLive) {
            if (isAvailable) {
                return "#db-ok";
            }
            return "#db-warn";
        } else {
            if (isAvailable) {
                return "#db-warn";
            }
            return "#db-fail";
        }
    }

}
