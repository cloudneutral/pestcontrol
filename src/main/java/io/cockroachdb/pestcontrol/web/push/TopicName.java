package io.cockroachdb.pestcontrol.web.push;

public enum TopicName {
    DASHBOARD_NODE_STATUS("/topic/dashboard/status"),
    DASHBOARD_REFRESH_PAGE("/topic/dashboard/refresh"),
    DASHBOARD_TOAST_MESSAGE("/topic/dashboard/toast"),
    DASHBOARD_MODEL_UPDATE("/topic/dashboard/update"),

    WORKLOAD_MODEL_UPDATE("/topic/workload/update"),
    WORKLOAD_CHARTS_UPDATE("/topic/workload/charts");

    final String value;

    TopicName(java.lang.String value) {
        this.value = value;
    }
}
