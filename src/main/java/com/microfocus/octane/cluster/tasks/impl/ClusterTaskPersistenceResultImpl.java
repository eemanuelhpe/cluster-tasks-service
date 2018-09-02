/*
	(c) Copyright 2018 Micro Focus or one of its affiliates.
	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
	You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and limitations under the License.
 */

package com.microfocus.octane.cluster.tasks.impl;

import com.microfocus.octane.cluster.tasks.api.dto.ClusterTaskPersistenceResult;
import com.microfocus.octane.cluster.tasks.api.enums.CTPPersistStatus;

/**
 * Created by gullery on 16/12/2016.
 */

class ClusterTaskPersistenceResultImpl implements ClusterTaskPersistenceResult {
	private final CTPPersistStatus status;

	ClusterTaskPersistenceResultImpl(CTPPersistStatus status) {
		this.status = status;
	}

	@Override
	public CTPPersistStatus getStatus() {
		return status;
	}
}
