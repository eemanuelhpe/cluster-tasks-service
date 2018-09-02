/*
	(c) Copyright 2018 Micro Focus or one of its affiliates.
	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
	You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and limitations under the License.
 */

package com.microfocus.octane.cluster.tasks.api.builders;

import com.microfocus.octane.cluster.tasks.api.dto.ClusterTask;

public interface TaskBuilder {
	TaskBuilder setDelayByMillis(Long delayByMillis) throws IllegalStateException;

	TaskBuilder setMaxTimeToRunMillis(Long maxTimeToRunMillis) throws IllegalStateException;

	TaskBuilder setBody(String body) throws IllegalStateException;

	ClusterTask build() throws IllegalStateException;
}
