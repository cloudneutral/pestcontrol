package io.cockroachdb.pestcontrol.schema.status;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeStatus {
    private static final List<DateTimeFormatter> ISO_DATE_TIME_FORMATTERS
            = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    );

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonProperty("address")
    private String address;

    @JsonProperty("build")
    private String build;

    @JsonProperty("gossiped_replicas")
    private String gossipedReplicas;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("intent_bytes")
    private String intentBytes;

    @JsonProperty("is_available")
    private String isAvailable;

    @JsonProperty("is_decommissioning")
    private String isDecommissioning;

    @JsonProperty("is_draining")
    private String isDraining;

    @JsonProperty("is_live")
    private String isLive;

    @JsonProperty("key_bytes")
    private String keyBytes;

    @JsonProperty("live_bytes")
    private String liveBytes;

    @JsonProperty("locality")
    private String locality;

    @JsonProperty("membership")
    private String membership;

    @JsonProperty("range_key_bytes")
    private String rangeKeyBytes;

    @JsonProperty("range_value_bytes")
    private String rangeValueBytes;

    @JsonProperty("ranges")
    private String ranges;

    @JsonProperty("ranges_unavailable")
    private String rangesUnavailable;

    @JsonProperty("ranges_underreplicated")
    private String rangesUnderreplicated;

    @JsonProperty("replicas_leaders")
    private String replicasLeaders;

    @JsonProperty("replicas_leaseholders")
    private String replicasLeaseholders;

    @JsonProperty("sql_address")
    private String sqlAddress;

    @JsonProperty("started_at")
    private String startedAt;

    @JsonProperty("system_bytes")
    private String systemBytes;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("value_bytes")
    private String valueBytes;

    public String getRangeDetails() {
        return getRanges() + " (" + getRangesUnavailable() + "/"
               + getRangesUnderreplicated() + ")";
    }

    public String getLastActive() {
        if (!StringUtils.hasLength(getUpdatedAt())) {
            return "";
        }
        for (DateTimeFormatter dateTimeFormatter : ISO_DATE_TIME_FORMATTERS) {
            try {
                OffsetDateTime updated = LocalDateTime.parse(getUpdatedAt(), dateTimeFormatter)
                        .atOffset(ZoneOffset.UTC);
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                return ChronoUnit.SECONDS.between(updated, now) + " seconds";
            } catch (Exception e) {
                // ok, whatever
            }
        }
        throw new IllegalStateException("No suitable formatter for timestamp: " + getUpdatedAt());
    }


    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonProperty("build")
    public String getBuild() {
        return build;
    }

    @JsonProperty("build")
    public void setBuild(String build) {
        this.build = build;
    }

    @JsonProperty("gossiped_replicas")
    public String getGossipedReplicas() {
        return gossipedReplicas;
    }

    @JsonProperty("gossiped_replicas")
    public void setGossipedReplicas(String gossipedReplicas) {
        this.gossipedReplicas = gossipedReplicas;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("intent_bytes")
    public String getIntentBytes() {
        return intentBytes;
    }

    @JsonProperty("intent_bytes")
    public void setIntentBytes(String intentBytes) {
        this.intentBytes = intentBytes;
    }

    @JsonProperty("is_available")
    public String getIsAvailable() {
        return isAvailable;
    }

    @JsonProperty("is_available")
    public void setIsAvailable(String isAvailable) {
        this.isAvailable = isAvailable;
    }

    @JsonProperty("is_decommissioning")
    public String getIsDecommissioning() {
        return isDecommissioning;
    }

    @JsonProperty("is_decommissioning")
    public void setIsDecommissioning(String isDecommissioning) {
        this.isDecommissioning = isDecommissioning;
    }

    @JsonProperty("is_draining")
    public String getIsDraining() {
        return isDraining;
    }

    @JsonProperty("is_draining")
    public void setIsDraining(String isDraining) {
        this.isDraining = isDraining;
    }

    @JsonProperty("is_live")
    public String getIsLive() {
        return isLive;
    }

    @JsonProperty("is_live")
    public void setIsLive(String isLive) {
        this.isLive = isLive;
    }

    @JsonProperty("key_bytes")
    public String getKeyBytes() {
        return keyBytes;
    }

    @JsonProperty("key_bytes")
    public void setKeyBytes(String keyBytes) {
        this.keyBytes = keyBytes;
    }

    @JsonProperty("live_bytes")
    public String getLiveBytes() {
        return liveBytes;
    }

    @JsonProperty("live_bytes")
    public void setLiveBytes(String liveBytes) {
        this.liveBytes = liveBytes;
    }

    @JsonProperty("locality")
    public String getLocality() {
        return locality;
    }

    @JsonProperty("locality")
    public void setLocality(String locality) {
        this.locality = locality;
    }

    @JsonProperty("membership")
    public String getMembership() {
        return membership;
    }

    @JsonProperty("membership")
    public void setMembership(String membership) {
        this.membership = membership;
    }

    @JsonProperty("range_key_bytes")
    public String getRangeKeyBytes() {
        return rangeKeyBytes;
    }

    @JsonProperty("range_key_bytes")
    public void setRangeKeyBytes(String rangeKeyBytes) {
        this.rangeKeyBytes = rangeKeyBytes;
    }

    @JsonProperty("range_value_bytes")
    public String getRangeValueBytes() {
        return rangeValueBytes;
    }

    @JsonProperty("range_value_bytes")
    public void setRangeValueBytes(String rangeValueBytes) {
        this.rangeValueBytes = rangeValueBytes;
    }

    @JsonProperty("ranges")
    public String getRanges() {
        return ranges;
    }

    @JsonProperty("ranges")
    public void setRanges(String ranges) {
        this.ranges = ranges;
    }

    @JsonProperty("ranges_unavailable")
    public String getRangesUnavailable() {
        return rangesUnavailable;
    }

    @JsonProperty("ranges_unavailable")
    public void setRangesUnavailable(String rangesUnavailable) {
        this.rangesUnavailable = rangesUnavailable;
    }

    @JsonProperty("ranges_underreplicated")
    public String getRangesUnderreplicated() {
        return rangesUnderreplicated;
    }

    @JsonProperty("ranges_underreplicated")
    public void setRangesUnderreplicated(String rangesUnderreplicated) {
        this.rangesUnderreplicated = rangesUnderreplicated;
    }

    @JsonProperty("replicas_leaders")
    public String getReplicasLeaders() {
        return replicasLeaders;
    }

    @JsonProperty("replicas_leaders")
    public void setReplicasLeaders(String replicasLeaders) {
        this.replicasLeaders = replicasLeaders;
    }

    @JsonProperty("replicas_leaseholders")
    public String getReplicasLeaseholders() {
        return replicasLeaseholders;
    }

    @JsonProperty("replicas_leaseholders")
    public void setReplicasLeaseholders(String replicasLeaseholders) {
        this.replicasLeaseholders = replicasLeaseholders;
    }

    @JsonProperty("sql_address")
    public String getSqlAddress() {
        return sqlAddress;
    }

    @JsonProperty("sql_address")
    public void setSqlAddress(String sqlAddress) {
        this.sqlAddress = sqlAddress;
    }

    @JsonProperty("started_at")
    public String getStartedAt() {
        return startedAt;
    }

    @JsonProperty("started_at")
    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("system_bytes")
    public String getSystemBytes() {
        return systemBytes;
    }

    @JsonProperty("system_bytes")
    public void setSystemBytes(String systemBytes) {
        this.systemBytes = systemBytes;
    }

    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("value_bytes")
    public String getValueBytes() {
        return valueBytes;
    }

    @JsonProperty("value_bytes")
    public void setValueBytes(String valueBytes) {
        this.valueBytes = valueBytes;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "NodeStatus{" +
               "additionalProperties=" + additionalProperties +
               ", address='" + address + '\'' +
               ", build='" + build + '\'' +
               ", gossipedReplicas='" + gossipedReplicas + '\'' +
               ", id=" + id +
               ", intentBytes='" + intentBytes + '\'' +
               ", isAvailable='" + isAvailable + '\'' +
               ", isDecommissioning='" + isDecommissioning + '\'' +
               ", isDraining='" + isDraining + '\'' +
               ", isLive='" + isLive + '\'' +
               ", keyBytes='" + keyBytes + '\'' +
               ", liveBytes='" + liveBytes + '\'' +
               ", locality='" + locality + '\'' +
               ", membership='" + membership + '\'' +
               ", rangeKeyBytes='" + rangeKeyBytes + '\'' +
               ", rangeValueBytes='" + rangeValueBytes + '\'' +
               ", ranges='" + ranges + '\'' +
               ", rangesUnavailable='" + rangesUnavailable + '\'' +
               ", rangesUnderreplicated='" + rangesUnderreplicated + '\'' +
               ", replicasLeaders='" + replicasLeaders + '\'' +
               ", replicasLeaseholders='" + replicasLeaseholders + '\'' +
               ", sqlAddress='" + sqlAddress + '\'' +
               ", startedAt='" + startedAt + '\'' +
               ", systemBytes='" + systemBytes + '\'' +
               ", updatedAt='" + updatedAt + '\'' +
               ", valueBytes='" + valueBytes + '\'' +
               '}';
    }
}