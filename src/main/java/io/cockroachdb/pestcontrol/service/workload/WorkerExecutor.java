package io.cockroachdb.pestcontrol.service.workload;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.util.Metrics;

@Component
public class WorkerExecutor implements DisposableBean {
    /**
     * Only 40001 is safe to retry in terms of non-idempotent side effects (like INSERTs)
     */
    private static final List<String> TRANSIENT_CODES = List.of(
            "40001", "08001", "08003", "08004", "08006", "08007", "08S01", "57P01"
    );

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LinkedList<Future<?>> futures = new LinkedList<>();

    private final ThreadPoolTaskExecutor threadPoolExecutor;

    public WorkerExecutor(@Autowired @Qualifier("asyncTaskExecutor")
                            ThreadPoolTaskExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public <T> Future<T> submit(Callable<T> action,
                                Metrics metrics,
                                Predicate<Integer> predicate) {
        Future<T> future = threadPoolExecutor.submit(() -> {
            int calls = 0;
            int fails = 0;
            T rv = null;

            while (predicate.test(++calls)) {
                if (Thread.interrupted()) {
                    logger.warn("Cancellation requested");
                    break;
                }

                final Instant callTime = Instant.now();

                try {
                    rv = action.call();

                    metrics.markSuccess(Duration.between(callTime, Instant.now()));
                } catch (SQLException | DataAccessException e) {
                    Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);

                    boolean isTransient = false;
                    if (cause instanceof SQLException) {
                        String sqlState = ((SQLException) cause).getSQLState();
                        isTransient = TRANSIENT_CODES.contains(sqlState);
                        if (isTransient) {
                            logger.warn("Transient SQL error [%s]: [%s]".formatted(sqlState, cause));
                        } else {
                            logger.warn("Non-transient SQL error [%s]: [%s]".formatted(sqlState, cause));
                        }
                    } else {
                        logger.warn("Non-transient data access error: [%s]".formatted(e));
                    }

                    metrics.markFail(Duration.between(callTime, Instant.now()), cause, isTransient);

                    backoffDelay(++fails);
                } catch (InterruptedException e) {
                    logger.warn("Thread interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Uncategorized error - bailing out", e);
                    throw new UndeclaredThrowableException(e);
                }
            }

            return rv;
        });

        futures.add(future);

        return future;
    }

    private void backoffDelay(int fails) {
        try {
            TimeUnit.MILLISECONDS.sleep(Math.min((long) (Math.pow(2, ++fails) + Math.random() * 1000), 5000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void destroy() {
        cancelAllFutures();
    }

    private void cancelAllFutures() {
        futures.forEach(this::cancelAndWait);
        futures.clear();
    }

    private void cancelAndWait(Future<?> future) {
        try {
            future.cancel(true);
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted: " + e);
        } catch (ExecutionException e) {
            logger.warn("Failed: " + e.getCause());
        } catch (CancellationException e) {
            // ok
        }
    }
}