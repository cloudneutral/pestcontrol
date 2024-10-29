package io.cockroachdb.pestcontrol.web.front;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.pestcontrol.domain.WorkerEntity;
import io.cockroachdb.pestcontrol.model.ApplicationModel;
import io.cockroachdb.pestcontrol.model.ClusterModel;
import io.cockroachdb.pestcontrol.service.workload.WorkerType;
import io.cockroachdb.pestcontrol.service.workload.WorkloadManager;
import io.cockroachdb.pestcontrol.util.timeseries.Metrics;
import io.cockroachdb.pestcontrol.web.model.WorkerForm;
import io.cockroachdb.pestcontrol.web.push.TopicName;
import io.cockroachdb.pestcontrol.web.rest.LinkRelations;
import io.cockroachdb.pestcontrol.web.rest.WorkloadRestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/workload")
@SessionAttributes(value = {"cluster"})
public class WorkloadController extends AbstractModelController {
    private static final SimpleRepresentationModelAssembler<WorkerEntity> workloadAssembler
            = new SimpleRepresentationModelAssembler<>() {
        @Override
        public void addLinks(EntityModel<WorkerEntity> resource) {
            WorkerEntity workload = resource.getContent();
            resource.add(linkTo(methodOn(WorkloadRestController.class)
                    .findWorker(workload.getClusterId(), workload.getId()))
                    .withSelfRel());
            if (workload.isRunning()) {
                resource.add(linkTo(methodOn(WorkloadController.class)
                        .cancelWorker(null, workload.getId()))
                        .withRel(LinkRelations.CANCEL_REL));
            } else {
                resource.add(linkTo(methodOn(WorkloadController.class)
                        .deleteWorker(null, workload.getId()))
                        .withRel(LinkRelations.DELETE_REL));
            }
        }

        @Override
        public void addLinks(CollectionModel<EntityModel<WorkerEntity>> resources) {
        }
    };

    @Autowired
    private WorkloadManager workloadManager;

    @Autowired
    private ApplicationModel applicationModel;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void modelUpdate() {
        workloadManager.updateDataPoints();
        messagePublisher.convertAndSend(TopicName.WORKLOAD_MODEL_UPDATE);
    }

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void chartUpdate() {
        messagePublisher.convertAndSend(TopicName.WORKLOAD_CHARTS_UPDATE);
    }

    @GetMapping
    public Callable<String> indexPage(
            @ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel,
            Model model) {
        WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));

        WorkerForm workerForm = new WorkerForm();
        workerForm.setDuration("00:15");
        workerForm.setWorkloadType(WorkerType.profile_insert);

        model.addAttribute("form",
                workerForm);
        model.addAttribute("workers",
                workloadAssembler.toCollectionModel(workloadManager.getWorkers(clusterModel.getId())));
        model.addAttribute("aggregatedMetrics",
                workloadManager.getAggregatedMetrics(clusterModel.getId()));

        return () -> "workload";
    }

    @PostMapping
    public Callable<String> submitForm(
            @ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel,
            @ModelAttribute WorkerForm form,
            Model model) {

        final LocalTime time = LocalTime.parse(form.getDuration(), DateTimeFormatter.ofPattern("HH:mm"));
        final Duration duration = Duration.ofHours(time.getHour()).plusMinutes(time.getMinute());
        final DataSource dataSource = applicationModel.getDataSource(clusterModel.getId());

        IntStream.rangeClosed(1, form.getCount())
                .forEach(value -> {
                    final WorkerType workerType = form.getWorkloadType();
                    final Callable<?> workerAction = workerType.createWorker(dataSource);
                    workloadManager.submitWorker(clusterModel.getId(), workerAction, workerType, duration);
                });

        model.addAttribute("form", form);

        return () -> "redirect:workload";
    }

    @PostMapping(value = "/cancelAll")
    public RedirectView cancelAllWorkers(
            @ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel) {
        workloadManager.cancelAll(clusterModel.getId());
        return new RedirectView("/workload");
    }

    @PostMapping(value = "/deleteAll")
    public RedirectView deleteAllWorkers(
            @ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel) {
        workloadManager.deleteAll(clusterModel.getId());
        return new RedirectView("/workload");
    }

    @PostMapping(value = "/cancel/{id}")
    public RedirectView cancelWorker(
            @ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel,
            @PathVariable("id") Integer id) {
        WorkerEntity worker = workloadManager.findById(clusterModel.getId(), id);
        worker.cancel();
        return new RedirectView("/workload");
    }

    @PostMapping(value = "/delete/{id}")
    public RedirectView deleteWorker(
            @ModelAttribute(value = "cluster", binding = false) ClusterModel clusterModel,
            @PathVariable("id") Integer id) {
        workloadManager.deleteById(clusterModel.getId(), id);
        return new RedirectView("/workload");
    }

    //
    // JSON endpoints below called from javascript triggered by STOMP messages.
    //

    @GetMapping(value = "/data-points/p99",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getDataPointsP99(
            @SessionAttribute("cluster") ClusterModel clusterModel) {
        return workloadManager.getDataPoints(clusterModel.getId(), Metrics::getP99);
    }

    @GetMapping(value = "/data-points/tps",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getDataPointsTPS(
            @SessionAttribute("cluster") ClusterModel clusterModel) {
        return workloadManager.getDataPoints(clusterModel.getId(), Metrics::getOpsPerSec);
    }

    @GetMapping("/update/workers")
    public @ResponseBody ResponseEntity<List<WorkerEntity>> getModelUpdateWorkers(
            @SessionAttribute(value = "cluster") ClusterModel clusterModel) {
//        logger.debug("Update model workers - clusterId: %s".formatted(clusterModel.getId()));
        List<WorkerEntity> workers = workloadManager.getWorkers(clusterModel.getId());
        return ResponseEntity.ok(workers);
    }

    @GetMapping("/update/metrics")
    public @ResponseBody ResponseEntity<Metrics> getModelUpdateAggregatedMetrics(
            @SessionAttribute(value = "cluster") ClusterModel clusterModel) {
//        logger.debug("Update model metrics - clusterId: %s".formatted(clusterModel.getId()));
        Metrics metrics = workloadManager.getAggregatedMetrics(clusterModel.getId());
        return ResponseEntity.ok(metrics);
    }
}
