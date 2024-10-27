package io.cockroachdb.pestcontrol.web.front;

import io.cockroachdb.pestcontrol.model.status.NodeStatus;

public class CardClassHelper {
    private final boolean available;

    public CardClassHelper(boolean available) {
        this.available = available;
    }

    public boolean isUnAvailable() {
        return !available;
    }

    public String getCardClass(NodeStatus nodeStatus) {
        if (isUnAvailable()) {
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
        if (isUnAvailable()) {
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
