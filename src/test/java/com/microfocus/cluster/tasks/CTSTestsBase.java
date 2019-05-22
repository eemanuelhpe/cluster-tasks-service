package com.microfocus.cluster.tasks;

import com.microfocus.cluster.tasks.api.ClusterTasksService;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CTSTestsBase {
	@Autowired
	public ClusterTasksService clusterTasksService;

	@Before
	public void ensurePrerequisites() {
		clusterTasksService.getReadyPromise().join();
	}
}
