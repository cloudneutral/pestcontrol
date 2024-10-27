package io.cockroachdb.pestcontrol.web.rest;

public class LinkRelations {
    public static final String ACTUATORS_REL = "actuators";

    public static final String CANCEL_REL = "cancel";

    public static final String DELETE_REL = "delete";

    public static final String VERSION_REL = "version";

    public static final String CLUSTER_REL = "cluster";

    public static final String DISRUPT_REL = "disrupt";

    public static final String RECOVER_REL = "recover";

    public static final String NODE_REL = "node";

    public static final String NODE_LIST_REL = "node-list";

    public static final String NODE_STATUS_REL = "status";

    public static final String NODE_DETAIL_REL = "detail";

    public static final String ADMIN_REL = "admin";

    // IANA standard link relations:
    // http://www.iana.org/assignments/link-relations/link-relations.xhtml

    public static final String CURIE_NAMESPACE = "pc";

    private LinkRelations() {
    }

}
