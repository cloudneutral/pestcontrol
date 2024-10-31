package io.cockroachdb.pestcontrol.repository;

import java.io.Serializable;

import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractEntity<ID extends Serializable> implements Persistable<ID> {
    @Transient
    private boolean isNew = true;

    protected void markNotNew() {
        this.isNew = false;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }
}
