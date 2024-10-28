package io.cockroachdb.pestcontrol.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.dao.DataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import io.cockroachdb.pestcontrol.config.ClosableDataSource;
import io.cockroachdb.pestcontrol.config.RestClientProvider;
import io.cockroachdb.pestcontrol.model.ApplicationModel;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.model.ClusterType;
import io.cockroachdb.pestcontrol.model.NodeModel;
import io.cockroachdb.pestcontrol.model.nodes.NodeDetail;
import io.cockroachdb.pestcontrol.model.nodes.NodeDetails;
import io.cockroachdb.pestcontrol.model.status.NodeStatus;
import io.cockroachdb.pestcontrol.repository.ClusterRepository;
import io.cockroachdb.pestcontrol.repository.JdbcClusterRepository;

@Component
public class CommonClusterManager implements ClusterManager {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, String> sessionTokens
            = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationModel applicationModel;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    @Autowired
    private ObjectProvider<DisruptionManager> disruptionManagers;

    @Autowired
    private RestClientProvider restClientProvider;

    // Initial failing credentials handler
    private CredentialsHandler credentialsHandler = new CredentialsHandler() {
    };

//    private final AtomicReference<Optional<List<NodeModel>>> cachedModels
//            = new AtomicReference<>(Optional.empty());

    protected ClusterProperties findClusterProperties(String clusterId) {
        return applicationModel.getClusters()
                .stream()
                .filter(x -> x.getClusterId().equals(clusterId))
                .findFirst()
                .orElseThrow(() ->
                        new ConfigurationException("No cluster configuration with id: " + clusterId));
    }

    @Override
    public void setCredentialsHandler(CredentialsHandler credentialsHandler) {
        this.credentialsHandler = credentialsHandler;
    }

    protected String findSessionToken(String clusterId) {
        if (!sessionTokens.containsKey(clusterId)) {
            Pair<String, String> credentials = credentialsHandler
                    .getAuthenticationCredentials(clusterId);
            return login(clusterId, credentials.getFirst(), credentials.getSecond());
        }
        return sessionTokens.get(clusterId);
    }

    private List<NodeModel> doQueryNodes(String clusterId) {
        ClusterProperties clusterProperties = findClusterProperties(clusterId);

        List<NodeStatus> nodeStatusList = queryNodeStatus(clusterProperties);
        List<NodeDetail> nodeDetailList = queryNodeDetails(clusterProperties);

        List<NodeModel> nodeModels = new ArrayList<>();

        nodeDetailList.forEach(nodeDetail -> nodeStatusList.stream()
                .filter(nodeStatus -> nodeStatus.getId().equals(nodeDetail.getNodeId()))
                .findFirst()
                .ifPresentOrElse(nodeStatus -> {
                    nodeModels.add(new NodeModel(clusterId, nodeDetail, nodeStatus));
                }, () -> {
                    nodeModels.add(new NodeModel(clusterId, nodeDetail, new NodeStatus()));
                    logger.warn("Unable to pair node detail (id: %s) with node status"
                            .formatted(nodeDetail.getNodeId()));
                }));

        return nodeModels;
    }

    private List<NodeStatus> queryNodeStatus(ClusterProperties clusterProperties) {
        try (ClosableDataSource dataSource
                     = dataSourceFactory.apply(clusterProperties.getDataSourceProperties())) {

            ClusterRepository clusterRepository = new JdbcClusterRepository(dataSource);
            String json = clusterRepository.queryNodeStatus();

            return Objects.isNull(json) ? List.of() :
                    objectMapper.readerForListOf(NodeStatus.class).readValue(json);
        } catch (DataAccessException e) {
            throw new ClientErrorException("Unable to query node status", e);
        } catch (JsonProcessingException e) {
            throw new ClientErrorException("Unable to read status query JSON", e);
        }
    }

    private Optional<NodeStatus> queryNodeStatusById(ClusterProperties clusterProperties, Integer nodeId) {
        try (ClosableDataSource dataSource
                     = dataSourceFactory.apply(clusterProperties.getDataSourceProperties())) {

            ClusterRepository clusterRepository = new JdbcClusterRepository(dataSource);
            String json = clusterRepository.queryNodeStatusById(nodeId);

            NodeStatus nodeStatus = Objects.isNull(json) ? null
                    : objectMapper.readerFor(NodeStatus.class)
                    .readValue(json);
            return Optional.ofNullable(nodeStatus);
        } catch (DataAccessException e) {
            throw new ClientErrorException("Unable to query node #" + nodeId + " status", e);
        } catch (JsonProcessingException e) {
            throw new ClientErrorException("Unable to read status query JSON", e);
        }
    }

    private RestClient restClient(ClusterProperties clusterProperties) {
        return restClientProvider.matches(clusterProperties);
    }

    private List<NodeDetail> queryNodeDetails(ClusterProperties clusterProperties) {
        String sessionToken = findSessionToken(clusterProperties.getClusterId());

        // There's no way to narrow this down other than by pagination
        ResponseEntity<NodeDetails> responseEntity = restClient(clusterProperties)
                .get()
                .uri(clusterProperties.getAdminUrl() + "/api/v2/nodes/")
                .header("X-Cockroach-API-Session", sessionToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(NodeDetails.class);

        return Objects.requireNonNull(responseEntity.getBody()).getNodes();
    }

    private Optional<NodeDetail> queryNodeDetailById(ClusterProperties clusterProperties, Integer nodeId) {
        return queryNodeDetails(clusterProperties)
                .stream()
                .filter(nodeStatus -> nodeStatus.getNodeId().equals(nodeId))
                .findFirst();
    }

    @Override
    public List<String> getClusterIds() {
        return applicationModel.getClusters()
                .stream()
                .map(ClusterProperties::getClusterId)
                .toList();
    }

    @Override
    public String getClusterVersion(String clusterId) {
        ClusterProperties clusterProperties = findClusterProperties(clusterId);
        try (ClosableDataSource dataSource
                     = dataSourceFactory.apply(clusterProperties.getDataSourceProperties())) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.queryForObject("select version()", String.class);
        } catch (DataAccessException e) {
            throw new ServerErrorException("Unable to query cluster version", e);
        }
    }

    @Override
    public String login(String clusterId, String userName, String password) {
        ClusterProperties clusterProperties = findClusterProperties(clusterId);

        if (EnumSet.of(ClusterType.local_insecure, ClusterType.remote_insecure)
                .contains(clusterProperties.getClusterType())) {
            logger.info("Login redundant for cluster type: %s"
                    .formatted(clusterProperties.getClusterType()));
            sessionTokens.put(clusterId, "");
            return "";
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", userName);
        map.add("password", password);

        ResponseEntity<String> responseEntity = restClient(clusterProperties)
                .post()
                .uri(clusterProperties.getAdminUrl() + "/api/v2/login/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(map)
                .retrieve()
                .toEntity(String.class);

        String token = JsonPath.parse(responseEntity.getBody()).read("$.session", String.class);

        sessionTokens.put(clusterId, token);

        logger.debug("Login successful - received session token: " + token);

        return token;
    }

    @Override
    public boolean logout(String clusterId) {
        ClusterProperties clusterProperties = findClusterProperties(clusterId);

        String sessionToken = findSessionToken(clusterId);

        ResponseEntity<String> responseEntity = restClient(clusterProperties)
                .post()
                .uri(clusterProperties.getAdminUrl() + "/api/v2/logout/")
                .header("X-Cockroach-API-Session", sessionToken)
                .retrieve()
                .toEntity(String.class);

        boolean outcome = JsonPath.parse(responseEntity.getBody()).read("$.logged_out", Boolean.class);
        if (outcome) {
            sessionTokens.remove(clusterId);
            logger.info("Logout command successful");
        } else {
            logger.warn("Logout command failed: " + responseEntity.getBody());
        }

        return outcome;
    }

    @Override
    public boolean hasSessionToken(String clusterId) {
        return sessionTokens.containsKey(clusterId);
    }

    @Override
    public NodeDetail queryNodeDetailById(String clusterId, Integer id) {
        return queryNodeDetailById(findClusterProperties(clusterId), id)
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeStatus queryNodeStatusById(String clusterId, Integer id) {
        return queryNodeStatusById(findClusterProperties(clusterId), id)
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public List<NodeModel> queryAllNodes(String clusterId) {
        List<NodeModel> models = doQueryNodes(clusterId);
//        this.cachedModels.set(Optional.of(models));
        return models;
    }

    @Override
    public NodeModel queryNodeById(String clusterId, Integer id) {
//        cachedModels.get().ifPresent(nodeModels ->
//                logger.warn("Using cached model for node id %s".formatted(id)));
//        return cachedModels.get().orElseGet(() -> doQueryNodes(clusterId))
//                .stream()
//                .filter(node -> node.getNodeDetail().getNodeId().equals(id))
//                .findFirst()
//                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
        return doQueryNodes(clusterId)
                .stream()
                .filter(node -> node.getNodeDetail().getNodeId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No such node with ID: " + id));
    }

    @Override
    public void disruptNode(String clusterId, Integer id) {
        final ClusterProperties clusterProperties = findClusterProperties(clusterId);
        final NodeModel nodeModel = queryNodeById(clusterId, id);

        DisruptionManager disruptionManager = disruptionManagers.stream()
                .filter(x -> x.supports(clusterProperties.getClusterType()))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new IllegalStateException("No disruption manager for cluster type "
                                                             + clusterProperties.getClusterType()));

        disruptionManager.disruptNode(clusterProperties, nodeModel);
    }

    @Override
    public void recoverNode(String clusterId, Integer id) {
        final ClusterProperties clusterProperties = findClusterProperties(clusterId);
        final NodeModel nodeModel = queryNodeById(clusterId, id);

        DisruptionManager disruptionManager = disruptionManagers.stream()
                .filter(x -> x.supports(clusterProperties.getClusterType()))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new IllegalStateException("No disruption manager for cluster type "
                                                             + clusterProperties.getClusterType()));

        disruptionManager.recoverNode(clusterProperties, nodeModel);
    }

    @Override
    public void disruptRegion(String clusterId, String regionName) {
        final ClusterProperties clusterProperties = findClusterProperties(clusterId);

        DisruptionManager disruptionManager = disruptionManagers.stream()
                .filter(x -> x.supports(clusterProperties.getClusterType()))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new IllegalStateException("No disruption manager for cluster type "
                                                             + clusterProperties.getClusterType()));

        disruptionManager.disruptRegion(clusterProperties, regionName);
    }

    @Override
    public void recoverRegion(String clusterId, String regionName) {
        final ClusterProperties clusterProperties = findClusterProperties(clusterId);

        DisruptionManager disruptionManager = disruptionManagers.stream()
                .filter(x -> x.supports(clusterProperties.getClusterType()))
                .min(new AnnotationAwareOrderComparator())
                .orElseThrow(() -> new IllegalStateException("No disruption manager for cluster type "
                                                             + clusterProperties.getClusterType()));

        disruptionManager.recoverRegion(clusterProperties, regionName);
    }
}
