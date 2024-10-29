package io.cockroachdb.pestcontrol.service.workload;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface Worker<T> extends Callable<T> {
}
