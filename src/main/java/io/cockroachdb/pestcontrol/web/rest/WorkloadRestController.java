package io.cockroachdb.pestcontrol.web.rest;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.pestcontrol.domain.WorkerEntity;
import io.cockroachdb.pestcontrol.model.ApplicationModel;
import io.cockroachdb.pestcontrol.service.workload.WorkerType;
import io.cockroachdb.pestcontrol.service.workload.WorkloadManager;
import io.cockroachdb.pestcontrol.web.model.MessageModel;
import io.cockroachdb.pestcontrol.web.model.WorkerForm;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/workload")
public class WorkloadRestController {
    private static final SimpleRepresentationModelAssembler<WorkerEntity> assembler
            = new SimpleRepresentationModelAssembler<>() {
        @Override
        public void addLinks(EntityModel<WorkerEntity> resource) {
            WorkerEntity entity = resource.getContent();

            Link selfLink = linkTo(methodOn(WorkloadRestController.class)
                    .findWorker(entity.getClusterId(), entity.getId()))
                    .withSelfRel();

            if (entity.isRunning()) {
                selfLink = selfLink.andAffordance(afford(methodOn(WorkloadRestController.class)
                        .cancelWorker(entity.getClusterId(), entity.getId())));

                resource.add(linkTo(methodOn(WorkloadRestController.class)
                        .cancelWorker(entity.getClusterId(), entity.getId()))
                        .withRel(LinkRelations.CANCEL_REL));
            } else {
                selfLink = selfLink.andAffordance(afford(methodOn(WorkloadRestController.class)
                        .deleteWorker(entity.getClusterId(), entity.getId())));

                resource.add(linkTo(methodOn(WorkloadRestController.class)
                        .deleteWorker(entity.getClusterId(), entity.getId()))
                        .withRel(LinkRelations.DELETE_REL));
            }

            resource.add(selfLink);
        }

        @Override
        public void addLinks(CollectionModel<EntityModel<WorkerEntity>> resources) {
        }
    };

    @Autowired
    private WorkloadManager workloadManager;

    @Autowired
    private ApplicationModel applicationModel;

    @GetMapping("/")
    public ResponseEntity<MessageModel> index() {
        MessageModel model = MessageModel.from("Cluster workloads");
        model.add(linkTo(methodOn(WorkloadRestController.class)
                .index())
                .withSelfRel());

        applicationModel.getClusterIds().forEach(clusterId -> {
            model.add(linkTo(methodOn(WorkloadRestController.class)
                    .findWorkers(clusterId))
                    .withRel(LinkRelations.WORKER_LIST_REL));
        });

        return ResponseEntity.ok(model);
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<CollectionModel<EntityModel<WorkerEntity>>> findWorkers(
            @PathVariable("clusterId") String clusterId) {
        CollectionModel<EntityModel<WorkerEntity>> collectionModel = assembler
                .toCollectionModel(workloadManager.getWorkers(clusterId));

        collectionModel.add(linkTo(methodOn(WorkloadRestController.class)
                .getWorkerForm(clusterId))
                .withRel(LinkRelations.FORM_REL));

        Links newLinks = collectionModel.getLinks().merge(Links.MergeMode.REPLACE_BY_REL,
                linkTo(methodOn(WorkloadRestController.class).findWorkers(clusterId))
                        .withSelfRel()
                        .andAffordance(afford(methodOn(WorkloadRestController.class)
                                .newWorker(clusterId, null))));

        return ResponseEntity.ok(CollectionModel.of(collectionModel.getContent(), newLinks));
    }

    @GetMapping(value = "/{clusterId}/worker/{id}")
    public HttpEntity<EntityModel<WorkerEntity>> findWorker(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        return ResponseEntity.ok(assembler.toModel(workloadManager.findById(clusterId, id)));
    }

    @PutMapping(value = "/{clusterId}/worker/{id}/cancel")
    public HttpEntity<EntityModel<WorkerEntity>> cancelWorker(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        WorkerEntity workload = workloadManager.findById(clusterId, id);
        if (workload.cancel()) {
            return ResponseEntity.ok(assembler.toModel(workload));
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(assembler.toModel(workload));
        }
    }

    @DeleteMapping(value = "/{clusterId}/worker/{id}/delete")
    public HttpEntity<Void> deleteWorker(
            @PathVariable("clusterId") String clusterId,
            @PathVariable("id") Integer id) {
        try {
            workloadManager.deleteById(clusterId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping(value = "/{clusterId}/worker")
    public HttpEntity<WorkerForm> getWorkerForm(@PathVariable("clusterId") String clusterId) {
        WorkerForm form = new WorkerForm();
        form.setWorkloadType(WorkerType.random_wait);
        form.setDuration("00:15");

        return ResponseEntity.ok(form
                .add(linkTo(methodOn(WorkloadRestController.class)
                        .getWorkerForm(clusterId))
                        .withSelfRel()
                        .andAffordance(afford(methodOn(WorkloadRestController.class)
                                .newWorker(clusterId, null)))
                ));
    }

    @PostMapping("/{clusterId}/worker")
    public HttpEntity<CollectionModel<EntityModel<WorkerEntity>>> newWorker(
            @PathVariable("clusterId") String clusterId,
            @RequestBody WorkerForm form) {

        final LocalTime time = LocalTime.parse(form.getDuration(), DateTimeFormatter.ofPattern("HH:mm"));
        final Duration duration = Duration.ofHours(time.getHour()).plusMinutes(time.getMinute());
        final DataSource dataSource = applicationModel.getDataSource(clusterId);

        List<WorkerEntity> entities = new ArrayList<>();

        IntStream.rangeClosed(1, form.getCount())
                .forEach(value -> {
                    final WorkerType workerType = form.getWorkloadType();
                    final Callable<?> workerAction = workerType.createWorker(dataSource);
                    workloadManager.submitWorker(clusterId, workerAction, workerType, duration);
                });

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toCollectionModel(entities));
    }

//    @GetMapping(value = "/data-points/p95")
//    public List<Map<String, Object>> getDataPointsP95() {
//        return getDataPoints(Metrics::getP95);
//    }
//
//    @GetMapping(value = "/data-points/p99")
//    public List<Map<String, Object>> getDataPointsP99() {
//        return getDataPoints(Metrics::getP99);
//    }
//
//    @GetMapping(value = "/data-points/tps")
//    public List<Map<String, Object>> getDataPointsTPS() {
//        return getDataPoints(Metrics::getOpsPerSec);
//    }
//
//    private List<Map<String, Object>> getDataPoints(Function<Metrics, Double> mapper) {
//        final List<Map<String, Object>> columnData = new ArrayList<>();
//
//        {
//            final Map<String, Object> headerElement = new HashMap<>();
//            List<Long> labels = workloadManager
//                    .getTimeSeriesInterval()
//                    .stream()
//                    .map(Instant::toEpochMilli)
//                    .toList();
//            headerElement.put("data", labels.toArray());
//            columnData.add(headerElement);
//        }
//
//        workloadManager.getWorkloads().forEach(workload -> {
//            Map<String, Object> dataElement = new HashMap<>();
//
//            List<Double> data = workloadManager
//                    .getTimeSeriesValues(workload.getId())
//                    .stream()
//                    .filter(metric -> !metric.isExpired())
//                    .map(mapper)
//                    .toList();
//
//            dataElement.put("id", workload.getId());
//            dataElement.put("name", "%s (%d)".formatted(workload.getTitle(), workload.getId()));
//            dataElement.put("data", data.toArray());
//
//            columnData.add(dataElement);
//        });
//
//        return columnData;
//    }
}
