package io.cockroachdb.pestcontrol.web.front;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import io.cockroachdb.pestcontrol.model.ClusterModel;
import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.service.ClusterManager;
import io.cockroachdb.pestcontrol.web.model.MessageModel;
import io.cockroachdb.pestcontrol.web.model.MessageType;
import io.cockroachdb.pestcontrol.web.push.SimpMessagePublisher;
import io.cockroachdb.pestcontrol.web.push.TopicName;
import io.cockroachdb.pestcontrol.web.rest.ClusterModelAssembler;
import io.cockroachdb.pestcontrol.web.rest.NodeModelAssembler;

@Controller
@SessionAttributes("cluster")
public abstract class AbstractModelController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ClusterManager clusterManager;

    @Autowired
    protected SimpMessagePublisher messagePublisher;

    @Autowired
    protected NodeModelAssembler nodeModelAssembler;

    @Autowired
    protected ClusterModelAssembler clusterModelAssembler;

    @ModelAttribute("cluster")
    public ClusterModel createClusterModel() {
        ClusterProperties clusterProperties = WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));

        try {
            ClusterModel clusterModel = ClusterModel.available(clusterProperties);
            clusterModel.setNodes(nodeModelAssembler.toCollectionModel(clusterManager.queryAllNodes(
                    clusterProperties.getClusterId())).getContent());

            logger.debug("Created cluster model");

            return clusterModelAssembler.toModel(clusterModel);
        } catch (Exception e) {
            logger.warn("Error creating cluster model", e);

            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from(e.getMessage()).setMessageType(MessageType.error));

            return clusterModelAssembler.toModel(ClusterModel.unavailable(clusterProperties));
        }
    }
}
