package io.cockroachdb.pestcontrol.web.front;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.cockroachdb.pestcontrol.model.ClusterModel;
import io.cockroachdb.pestcontrol.model.NodeModel;
import io.cockroachdb.pestcontrol.service.CommandException;
import io.cockroachdb.pestcontrol.web.model.MessageModel;
import io.cockroachdb.pestcontrol.web.model.MessageType;
import io.cockroachdb.pestcontrol.web.push.TopicName;

@Controller
@RequestMapping("/cluster")
public class ClusterController extends AbstractModelController {
    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void scheduledStatusUpdate() {
        messagePublisher.convertAndSend(TopicName.DASHBOARD_MODEL_UPDATE);
    }

    @GetMapping
    public Callable<String> indexPage(
            @ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel,
            @RequestParam(name = "level", defaultValue = "1", required = false) Integer level,
            Model model) {

        model.addAttribute("cluster", createClusterModel());
        model.addAttribute("helper", new CardClassHelper(clusterModel.isAvailable()));
        model.addAttribute("level", level);

        return () -> "cluster";
    }

    @PostMapping("/node-action")
    public Callable<String> nodeAction(@ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel,
                                       @ModelAttribute("node-id") Integer nodeId,
                                       @ModelAttribute("node-action") String action,
                                       SessionStatus status,
                                       RedirectAttributes redirectAttributes) {
        final String clusterId = clusterModel.getId();

        logger.debug(">> Performing node action: clusterId=%s, nodeId=%s, action=%s"
                .formatted(clusterId, nodeId, action));

        try {
            if ("disrupt".equalsIgnoreCase(action)) {
                messagePublisher.convertAndSend(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Disrupt node " + nodeId)
                                .setMessageType(MessageType.warning));
                clusterManager.disruptNode(clusterId, nodeId);
            } else if ("recover".equalsIgnoreCase(action)) {
                messagePublisher.convertAndSend(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Recover node " + nodeId)
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

        redirectAttributes.addFlashAttribute("cluster");

        return () -> "redirect:/cluster";
    }

    @PostMapping("/region-action")
    public Callable<String> regionAction(@ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel,
                                         @ModelAttribute("region-name") String regionName,
                                         @ModelAttribute("region-action") String action) {
        final String clusterId = clusterModel.getId();

        Optional.ofNullable(regionName).ifPresent(x -> {
            logger.debug(">> Performing region action: clusterId=%s, regionName=%s, action=%s"
                    .formatted(clusterId, regionName, action));

            try {
                if ("Disrupt".equalsIgnoreCase(action)) {
                    messagePublisher.convertAndSend(TopicName.DASHBOARD_TOAST_MESSAGE,
                            MessageModel.from("Disrupt region " + regionName)
                                    .setMessageType(MessageType.warning));
                    clusterManager.disruptRegion(clusterId, regionName);
                } else if ("Recover".equalsIgnoreCase(action)) {
                    messagePublisher.convertAndSend(TopicName.DASHBOARD_TOAST_MESSAGE,
                            MessageModel.from("Recover region " + regionName)
                                    .setMessageType(MessageType.information));
                    clusterManager.recoverRegion(clusterId, regionName);
                }
            } catch (CommandException e) {
                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from(e.getMessage())
                                .setMessageType(MessageType.error));
            } finally {
                logger.debug("<< Done performing region action: id=%s, action=%s".formatted(regionName, action));
            }
        });

        return () -> "redirect:/cluster";
    }

    //
    // JSON endpoints below called from javascript triggered by STOMP messages.
    //

    @GetMapping("/update")
    public @ResponseBody ResponseEntity<Void> modelUpdate(
            @SessionAttribute(value = "cluster") ClusterModel clusterModel) {

        logger.debug("Performing cluster update, clusterId: %s".formatted(clusterModel.getId()));

        try {
            List<NodeModel> nodeModelList = clusterManager.queryAllNodes(clusterModel.getId());

            nodeModelList.forEach(
                    node -> messagePublisher.convertAndSend(TopicName.DASHBOARD_NODE_STATUS, node));

            if (!clusterModel.isAvailable()) {
                clusterModel.setAvailable(true);

                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_REFRESH_PAGE);
            } else if (!clusterModel.getNodes().isEmpty()
                       && clusterModel.getNodes().size() != nodeModelList.size()) {
                logger.warn("Node count %d != %d - forcing refresh"
                        .formatted(clusterModel.getNodes().size(), nodeModelList.size()));
                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_REFRESH_PAGE);
            }

//            clusterModel.setNodes(NodeRestController
//                    .nodeModelAssembler.toCollectionModel(nodeModelList).getContent());
        } catch (Exception e) {
            logger.warn("Cluster update failed: %s".formatted(e));

            if (clusterModel.isAvailable()) {
                clusterModel.setAvailable(false);

                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_TOAST_MESSAGE,
                        MessageModel.from("Cluster update failed: " + e.getMessage())
                                .setMessageType(MessageType.error));

                messagePublisher.convertAndSendLater(TopicName.DASHBOARD_REFRESH_PAGE);
            }
        }

        return ResponseEntity.ok().build();
    }
}
