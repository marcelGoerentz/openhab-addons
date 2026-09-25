/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.km200.internal.handler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.common.NamedThreadFactory;

/**
 * The {@link KM200GatewayConnector} owns the single bounded worker pool used for all blocking gateway I/O:
 * capability discovery, periodic channel polling, on-demand channel refreshes and command sending all run on it.
 * <p>
 * This replaces the previous design of ad-hoc {@link Runnable} classes scheduled on a small fixed pool combined
 * with a coarse device-wide lock. Bounding the pool size keeps the number of concurrent HTTP requests to the
 * (resource constrained) embedded gateway limited, while still allowing several channels to be read or written in
 * parallel instead of strictly sequentially with artificial delays in between.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class KM200GatewayConnector implements Executor {

    /**
     * Number of gateway requests (discovery, reads, writes) that may be in flight at the same time. Kept
     * moderate since the embedded gateway is resource constrained; too high a value risks provoking HTTP 500 /
     * timeout responses from the device instead of speeding things up.
     */
    public static final int WORKER_POOL_SIZE = 8;

    private ScheduledExecutorService pool = newPool();

    private static ScheduledExecutorService newPool() {
        return Executors.newScheduledThreadPool(WORKER_POOL_SIZE,
                new NamedThreadFactory("org.openhab.binding.km200", true));
    }

    /**
     * Makes the connector ready to accept work again, e.g. after having been shut down by a previous
     * {@code dispose()}.
     */
    public synchronized void restart() {
        if (pool.isShutdown()) {
            pool = newPool();
        }
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }

    /**
     * Schedules a task for periodic execution on the worker pool.
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return pool.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    /**
     * Shuts the pool down immediately, interrupting any in-flight gateway requests so blocked callers (e.g. a
     * running discovery) can unblock promptly. Framework lifecycle methods such as {@code dispose()} are expected
     * to return promptly, so no attempt is made to wait for in-flight work to finish gracefully.
     */
    public void shutdownNow() {
        pool.shutdownNow();
    }
}
