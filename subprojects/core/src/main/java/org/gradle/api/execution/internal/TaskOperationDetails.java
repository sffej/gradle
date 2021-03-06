/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.execution.internal;

import org.gradle.api.internal.TaskInternal;
import org.gradle.internal.progress.NoResultBuildOperationDetails;


/**
 * Details about a task being run.
 *
 * This class is intentionally internal and consumed by the build scan plugin.
 */
public final class TaskOperationDetails implements NoResultBuildOperationDetails {
    // TODO: do not reference mutable state
    private final TaskInternal task;

    public TaskOperationDetails(TaskInternal task) {
        this.task = task;
    }

    public TaskInternal getTask() {
        return task;
    }

}
