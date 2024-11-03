package io.cockroachdb.pestcontrol.web.api;

public class LinkRelations {
    public static final String ACTUATORS_REL = "actuators";

    public static final String CANCEL_REL = "cancel";

    public static final String DELETE_REL = "delete";

    public static final String ENABLE_REL = "enable";

    public static final String DISABLE_REL = "disable";

    public static final String VERSION_REL = "version";

    public static final String CLUSTER_REL = "cluster";

    public static final String CLUSTER_LIST_REL = "cluster-list";

    public static final String DISRUPT_REL = "disrupt";

    public static final String RECOVER_REL = "recover";

    public static final String NODE_REL = "node";

    public static final String NODE_LIST_REL = "node-list";

    public static final String NODE_STATUS_REL = "node-status";

    public static final String NODE_DETAIL_REL = "node-detail";

    public static final String ADMIN_REL = "admin";

    public static final String TOXIPROXY_INDEX_REL = "toxiproxy";

    public static final String PROXY_REL = "proxy";

    public static final String PROXY_LIST_REL = "proxy-list";

    public static final String FORM_REL = "form";

    public static final String TOXIC_REL = "toxic";

    public static final String TOXIC_LIST_REL = "toxic-list";

    public static final String RESET_REL = "reset";

    public static final String WORKER_REL = "worker";

    public static final String WORKER_LIST_REL = "worker-list";

    public static final String LOCALITY_REL = "locality";

    public static final String LOCALITY_LIST_REL = "locality-list";

    // IANA standard link relations:
    // http://www.iana.org/assignments/link-relations/link-relations.xhtml

    public static final String CURIE_NAMESPACE = "pc";

    private LinkRelations() {
    }

}
