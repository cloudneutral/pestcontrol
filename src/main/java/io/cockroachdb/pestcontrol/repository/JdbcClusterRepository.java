package io.cockroachdb.pestcontrol.repository;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcClusterRepository implements ClusterRepository {
    private static final String SQL_QUERY_NODE_STATUS = """
             SELECT *
              FROM ((((SELECT node_id   AS id,
                              address,
                              sql_address,
                              build_tag AS build,
                              started_at,
                              updated_at,
                              locality,
                              CASE
                                  WHEN split_part(expiration, ',', 1)::decimal > now()::decimal
                                      THEN true
                                  ELSE false
                                  END   AS is_available,
                              IFNULL(is_live, false) AS is_live
                       FROM crdb_internal.gossip_liveness
                                LEFT JOIN crdb_internal.gossip_nodes USING (node_id)) as glgn LEFT
                  JOIN (SELECT node_id                                                      AS id,
                               sum((metrics ->> 'replicas.leaders')::DECIMAL)::INT8         AS replicas_leaders,
                               sum((metrics ->> 'replicas.leaseholders')::DECIMAL)::INT8    AS replicas_leaseholders,
                               sum((metrics ->> 'replicas')::DECIMAL)::INT8                 AS ranges,
                               sum((metrics ->> 'ranges.unavailable')::DECIMAL)::INT8       AS ranges_unavailable,
                               sum((metrics ->> 'ranges.underreplicated')::DECIMAL)::INT8   AS ranges_underreplicated
                        FROM crdb_internal.kv_store_status
                        GROUP BY node_id) as kss USING (id)) LEFT
                  JOIN (SELECT node_id                                                       AS id,
                               sum((metrics ->> 'livebytes')::DECIMAL)::INT8                 AS live_bytes,
                               sum((metrics ->> 'keybytes')::DECIMAL)::INT8                  AS key_bytes,
                               sum((metrics ->> 'valbytes')::DECIMAL)::INT8                  AS value_bytes,
                               sum(coalesce((metrics ->> 'rangekeybytes')::DECIMAL, 0))::INT AS range_key_bytes,
                               sum(coalesce((metrics ->> 'rangevalbytes')::DECIMAL, 0))::INT AS range_value_bytes,
                               sum((metrics ->> 'intentbytes')::DECIMAL)::INT8               AS intent_bytes,
                               sum((metrics ->> 'sysbytes')::DECIMAL)::INT8                  AS system_bytes
                        FROM crdb_internal.kv_store_status
                        GROUP BY node_id) as k USING (id)) LEFT
                  JOIN (SELECT node_id                AS id,
                               ranges                 AS gossiped_replicas,
                               membership != 'active' AS is_decommissioning,
                               membership             AS membership,
                               draining               AS is_draining
                        FROM crdb_internal.gossip_liveness
                                 LEFT JOIN crdb_internal.gossip_nodes USING (node_id)) as g USING (id)
            """;

    private static final String SQL_QUERY_NODE_STATUS_ALL
            = "WITH sq AS (" + SQL_QUERY_NODE_STATUS + ") ORDER BY id) SELECT json_agg(row_to_json(sq)) FROM sq";

    private static final String SQL_QUERY_NODE_STATUS_BY_ID
            = "WITH sq AS (" + SQL_QUERY_NODE_STATUS + ") WHERE ID=? ORDER BY id) SELECT row_to_json(sq) FROM sq";

    private final JdbcTemplate jdbcTemplate;

    public JdbcClusterRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public String queryNodeStatus() {
        return jdbcTemplate.queryForObject(SQL_QUERY_NODE_STATUS_ALL, String.class);
    }

    @Override
    public String queryNodeStatusById(Integer id) {
        return jdbcTemplate.queryForObject(SQL_QUERY_NODE_STATUS_BY_ID, String.class, id);
    }
}
