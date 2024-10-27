package io.cockroachdb.pestcontrol.web.model;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.pestcontrol.domain.WorkloadType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonPropertyOrder({"links", "embedded", "templates"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkloadForm extends RepresentationModel<WorkloadForm> {
    @NotNull
    private WorkloadType workloadType;

    @NotNull
    @Pattern(regexp = "^[0-2][0-3]:[0-5][0-9]$")
    private String duration;

    @NotNull
    private Integer workloadCount = 1;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public WorkloadType getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(WorkloadType workloadType) {
        this.workloadType = workloadType;
    }

    public @NotNull Integer getWorkloadCount() {
        return workloadCount;
    }

    public void setWorkloadCount(@NotNull Integer workloadCount) {
        this.workloadCount = workloadCount;
    }
}
