package io.cockroachdb.pestcontrol.web;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.cockroachdb.pestcontrol.schema.ClusterModel;
import io.cockroachdb.pestcontrol.schema.ClusterProperties;
import io.cockroachdb.pestcontrol.service.ClusterManager;
import io.cockroachdb.pestcontrol.service.CommandException;
import io.cockroachdb.pestcontrol.web.api.cluster.ClusterHelper;
import io.cockroachdb.pestcontrol.web.api.cluster.ClusterRestController;
import io.cockroachdb.pestcontrol.web.push.MessageModel;
import io.cockroachdb.pestcontrol.web.push.MessageType;
import io.cockroachdb.pestcontrol.web.push.SimpMessagePublisher;
import io.cockroachdb.pestcontrol.web.push.TopicName;

@WebController
@RequestMapping("/cluster")
public class ClusterController extends AbstractSessionController {
    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ClusterRestController clusterRestController;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void scheduledStatusUpdate() {
        messagePublisher.convertAndSend(TopicName.DASHBOARD_MODEL_UPDATE);
    }

    private Optional<ClusterModel> newClusterModel() {
        ClusterProperties clusterProperties = WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));

        try {
            ClusterModel clusterModel = clusterRestController
                    .getCluster(clusterProperties.getClusterId())
                    .getBody();

            return Optional.of(clusterModel);
        } catch (Exception e) {
            logger.warn("Error creating cluster model", e);
            return Optional.empty();
        }
    }

    @GetMapping
    public Callable<String> indexPage(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @RequestParam(name = "level", defaultValue = "1", required = false) Integer level,
            Model model) {

        model.addAttribute("helper", clusterHelper);
        model.addAttribute("level", level);

        newClusterModel().ifPresentOrElse(x -> {
            clusterHelper.setClusterModel(x);
            clusterHelper.setAvailable(true);
        }, () -> {
            clusterHelper.setAvailable(false);
        });

        return () -> "cluster";
    }

    @PostMapping("/node-action")
    public Callable<String> nodeAction(@ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
                                       @ModelAttribute("node-id") Integer nodeId,
                                       @ModelAttribute("node-action") String action,
                                       SessionStatus status,
                                       RedirectAttributes redirectAttributes) {
        final String clusterId = clusterHelper.getId();

        logger.debug(">> Performing node action: clusterId=%s, nodeId=%s, action=%s"
                .formatted(clusterId, nodeId, action));

        try {
            if ("Disrupt".equalsIgnoreCase(action)) {
                messagePublisher.convertAndSend(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Disrupt Node " + nodeId)
                                .setMessageType(MessageType.warning));
                clusterManager.disruptNode(clusterId, nodeId);
            } else if ("Recover".equalsIgnoreCase(action)) {
                messagePublisher.convertAndSend(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Recover Node " + nodeId)
                                .setMessageType(MessageType.information));
                clusterManager.recoverNode(clusterId, nodeId);
            }
        } catch (CommandException e) {
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from(e.getMessage())
                            .setMessageType(MessageType.error));
        } finally {
            logger.debug("<< Done performing node action: id=%s, action=%s".formatted(nodeId, action));
            status.setComplete();
        }

        redirectAttributes.addFlashAttribute("helper");

        return () -> "redirect:/cluster";
    }

    @PostMapping("/locality-action")
    public Callable<String> localityAction(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @ModelAttribute("locality") String locality,
            @ModelAttribute("action") String action) {
        final String clusterId = clusterHelper.getId();

        logger.debug("Performing locality action: clusterId=%s, locality=%s, action=%s"
                .formatted(clusterId, locality, action));

        try {
            if ("Disrupt".equalsIgnoreCase(action)) {
                messagePublisher.convertAndSend(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Disrupt region " + locality)
                                .setMessageType(MessageType.warning));
                clusterManager.disruptLocality(clusterId, locality);
            } else if ("Recover".equalsIgnoreCase(action)) {
                messagePublisher.convertAndSend(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Recover region " + locality)
                                .setMessageType(MessageType.information));
                clusterManager.recoverLocality(clusterId, locality);
            }
        } catch (Exception e) {
            logger.warn("Locality action error", e);

            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from(e.getMessage())
                            .setMessageType(MessageType.error));
        }

        return () -> "redirect:/cluster";
    }

    //
    // JSON endpoints below called from javascript triggered by STOMP messages.
    //

    @GetMapping("/update")
    public @ResponseBody ResponseEntity<Void> modelUpdate(
            @SessionAttribute(value = "helper") ClusterHelper clusterHelper) {
        logger.debug("Performing cluster update (clusterId: %s)".formatted(clusterHelper.getId()));

        newClusterModel().ifPresentOrElse(x -> {
            logger.debug("Cluster update successful (clusterId: %s)".formatted(clusterHelper.getId()));

            if (clusterHelper.isDifferent(x) || !clusterHelper.isAvailable()) {
                logger.warn("Node count differs - forcing refresh");

                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Cluster status changed").setMessageType(MessageType.information), 500);
                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_REFRESH_PAGE, "", 1500);
            } else {
                x.getNodes().forEach(nodeModel -> {
                    messagePublisher.convertAndSendLater(TopicName.DASHBOARD_NODE_STATUS, nodeModel, 500);
                });
            }

            clusterHelper.setAvailable(true);
            clusterHelper.setClusterModel(x);
        }, () -> {
            logger.debug("Cluster update failed (clusterId: %s)".formatted(clusterHelper.getId()));

            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                    MessageModel.from("Cluster update failed").setMessageType(MessageType.error), 500);
            messagePublisher.convertAndSendLater(TopicName.DASHBOARD_REFRESH_PAGE, "", 1500);

            clusterHelper.setAvailable(false);
        });

        return ResponseEntity.ok().build();
    }
}
