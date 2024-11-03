package io.cockroachdb.pestcontrol.web;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.pestcontrol.config.ApplicationProperties;
import io.cockroachdb.pestcontrol.service.workload.WorkerModel;
import io.cockroachdb.pestcontrol.service.workload.WorkerType;
import io.cockroachdb.pestcontrol.service.workload.WorkloadManager;
import io.cockroachdb.pestcontrol.util.Metrics;
import io.cockroachdb.pestcontrol.web.api.LinkRelations;
import io.cockroachdb.pestcontrol.web.api.cluster.ClusterHelper;
import io.cockroachdb.pestcontrol.web.api.workload.WorkerForm;
import io.cockroachdb.pestcontrol.web.api.workload.WorkloadRestController;
import io.cockroachdb.pestcontrol.web.push.SimpMessagePublisher;
import io.cockroachdb.pestcontrol.web.push.TopicName;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@WebController
@RequestMapping("/workload")
public class WorkloadController extends AbstractSessionController {
    private static final RepresentationModelAssembler<WorkerModel, WorkerModel> workloadAssembler
            = entity -> {
        entity.add(linkTo(methodOn(WorkloadRestController.class)
                .getWorker(entity.getClusterId(), entity.getId()))
                .withSelfRel());
        if (entity.isRunning()) {
            entity.add(linkTo(methodOn(WorkloadController.class)
                    .cancelWorker(null, entity.getId()))
                    .withRel(LinkRelations.CANCEL_REL + "-redirect"));
        } else {
            entity.add(linkTo(methodOn(WorkloadController.class)
                    .deleteWorker(null, entity.getId()))
                    .withRel(LinkRelations.DELETE_REL + "-redirect"));
        }
        return entity;
    };

    @Autowired
    private WorkloadManager workloadManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Value("${application.samplePeriodSeconds}")
    private int samplePeriodSeconds;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void dataPointsUpdate() {
        workloadManager.updateDataPoints(Duration.ofSeconds(samplePeriodSeconds));
    }

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void modelUpdate() {
        messagePublisher.convertAndSend(TopicName.WORKLOAD_MODEL_UPDATE);
    }

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void chartUpdate() {
        messagePublisher.convertAndSend(TopicName.WORKLOAD_CHARTS_UPDATE);
    }

    @GetMapping
    public Callable<String> indexPage(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            Model model) {
        WebUtils.getAuthenticatedClusterProperties().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Expected authentication token"));

        WorkerForm workerForm = new WorkerForm();
        workerForm.setDuration("00:15");
        workerForm.setWorkloadType(WorkerType.profile_insert);

        model.addAttribute("form", workerForm);
        model.addAttribute("workers",
                workloadAssembler.toCollectionModel(workloadManager.getWorkers(clusterHelper.getId())));
        model.addAttribute("aggregatedMetrics",
                workloadManager.getAggregatedMetrics(clusterHelper.getId()));

        return () -> "workload";
    }

    @PostMapping
    public Callable<String> submitForm(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @ModelAttribute WorkerForm form,
            Model model) {

        final LocalTime time = LocalTime.parse(form.getDuration(), DateTimeFormatter.ofPattern("HH:mm"));
        final Duration duration = Duration.ofHours(time.getHour()).plusMinutes(time.getMinute());
        final DataSource dataSource = applicationProperties.getDataSource(clusterHelper.getId());

        IntStream.rangeClosed(1, form.getCount())
                .forEach(value -> {
                    final WorkerType workerType = form.getWorkloadType();
                    final Callable<?> workerAction = workerType.createWorker(dataSource);
                    workloadManager.submitWorker(clusterHelper.getId(), workerAction, workerType, duration);
                });

        model.addAttribute("form", form);

        return () -> "redirect:workload";
    }

    @PostMapping(value = "/cancelAll")
    public RedirectView cancelAllWorkers(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper) {
        workloadManager.cancelAll(clusterHelper.getId());
        return new RedirectView("/workload");
    }

    @PostMapping(value = "/deleteAll")
    public RedirectView deleteAllWorkers(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper) {
        workloadManager.deleteAll(clusterHelper.getId());
        return new RedirectView("/workload");
    }

    @GetMapping(value = "/cancel/{id}")
    public RedirectView cancelWorker(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @PathVariable("id") Integer id) {
        WorkerModel worker = workloadManager.findById(clusterHelper.getId(), id);
        worker.cancel();
        return new RedirectView("/workload");
    }

    @GetMapping(value = "/delete/{id}")
    public RedirectView deleteWorker(
            @ModelAttribute(value = "helper", binding = false) ClusterHelper clusterHelper,
            @PathVariable("id") Integer id) {
        workloadManager.deleteById(clusterHelper.getId(), id);
        return new RedirectView("/workload");
    }

    @GetMapping("/data-points/clear")
    public RedirectView clearDataPoints(
            @SessionAttribute(value = "helper") ClusterHelper clusterHelper) {
        workloadManager.clearDataPoints(clusterHelper.getId());
        return new RedirectView("/workload");
    }

    //
    // JSON endpoints below called from javascript triggered by STOMP messages.
    //

    @GetMapping(value = "/data-points/p99",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getDataPointsP99(
            @SessionAttribute("helper") ClusterHelper clusterHelper) {
        return workloadManager.getDataPoints(clusterHelper.getId(), Metrics::getP99);
    }

    @GetMapping(value = "/data-points/tps",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Map<String, Object>> getDataPointsTPS(
            @SessionAttribute("helper") ClusterHelper clusterHelper) {
        return workloadManager.getDataPoints(clusterHelper.getId(), Metrics::getOpsPerSec);
    }

    @GetMapping("/metrics/update")
    public @ResponseBody Metrics getModelUpdateAggregatedMetrics(
            @SessionAttribute(value = "helper") ClusterHelper clusterHelper) {
        return workloadManager.getAggregatedMetrics(clusterHelper.getId());
    }

    @GetMapping("/workers/update")
    public @ResponseBody List<WorkerModel> getModelUpdateWorkers(
            @SessionAttribute(value = "helper") ClusterHelper clusterHelper) {
        return workloadManager.getWorkers(clusterHelper.getId());
    }
}
