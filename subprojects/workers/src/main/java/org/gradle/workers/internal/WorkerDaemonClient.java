/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.workers.internal;

import org.gradle.internal.concurrent.Stoppable;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.progress.BuildOperationState;
import org.gradle.internal.operations.CallableBuildOperation;
import org.gradle.internal.progress.BuildOperationDescriptor;
import org.gradle.internal.work.WorkerLeaseRegistry.WorkerLease;
import org.gradle.internal.work.WorkerLeaseRegistry.WorkerLeaseCompletion;
import org.gradle.process.internal.health.memory.JvmMemoryStatus;
import org.gradle.process.internal.worker.WorkerProcess;

class WorkerDaemonClient<T extends WorkSpec> implements Worker<T>, Stoppable {
    private final DaemonForkOptions forkOptions;
    private final WorkerDaemonProcess<T> workerDaemonProcess;
    private final WorkerProcess workerProcess;
    private final BuildOperationExecutor buildOperationExecutor;
    private int uses;

    public WorkerDaemonClient(DaemonForkOptions forkOptions, WorkerDaemonProcess<T> workerDaemonProcess, WorkerProcess workerProcess, BuildOperationExecutor buildOperationExecutor) {
        this.forkOptions = forkOptions;
        this.workerDaemonProcess = workerDaemonProcess;
        this.workerProcess = workerProcess;
        this.buildOperationExecutor = buildOperationExecutor;
    }

    @Override
    public DefaultWorkResult execute(final T spec, WorkerLease parentWorkerWorkerLease, final BuildOperationState parentBuildOperation) {
        WorkerLeaseCompletion workerLease = parentWorkerWorkerLease.startChild();
        try {
            return buildOperationExecutor.call(new CallableBuildOperation<DefaultWorkResult>() {
                @Override
                public DefaultWorkResult call(BuildOperationContext context) {
                    uses++;
                    return workerDaemonProcess.execute(spec);
                }

                @Override
                public BuildOperationDescriptor.Builder description() {
                    return BuildOperationDescriptor.displayName(spec.getDisplayName()).parent(parentBuildOperation);
                }
            });
        } finally {
            workerLease.leaseFinish();
        }
    }

    @Override
    public DefaultWorkResult execute(T spec) {
        throw new UnsupportedOperationException();
    }

    public boolean isCompatibleWith(DaemonForkOptions required) {
        return forkOptions.isCompatibleWith(required);
    }

    JvmMemoryStatus getJvmMemoryStatus() {
        return workerProcess.getJvmMemoryStatus();
    }

    @Override
    public void stop() {
        workerDaemonProcess.stop();
    }

    DaemonForkOptions getForkOptions() {
        return forkOptions;
    }

    public int getUses() {
        return uses;
    }
}
