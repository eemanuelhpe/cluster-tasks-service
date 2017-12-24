package com.microfocus.octane.cluster.tasks;

import com.microfocus.octane.cluster.tasks.api.ClusterTasksService;
import com.microfocus.octane.cluster.tasks.api.builders.TaskBuilders;
import com.microfocus.octane.cluster.tasks.api.dto.ClusterTask;
import com.microfocus.octane.cluster.tasks.api.enums.ClusterTasksDataProviderType;
import com.microfocus.octane.cluster.tasks.processors.ClusterTasksHC_A_test;
import com.microfocus.octane.cluster.tasks.processors.ClusterTasksHC_B_test;
import com.microfocus.octane.cluster.tasks.processors.ClusterTasksHC_C_test;
import com.microfocus.octane.cluster.tasks.processors.ClusterTasksHC_D_test;
import com.microfocus.octane.cluster.tasks.processors.ClusterTasksHC_E_test;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

/**
 * Created by gullery on 02/06/2016.
 * <p>
 * Collection of integration tests for Cluster Tasks Processor Service to check how the whole system performs in a 'heavy' cluster and load simulation
 * - we are raising a number nodes as specified below
 * - we are creating few scheduled tasks
 * - we are creating few regular tasks processor and push the specified amount of tasks for each of those
 * - tasks' processor will do nothing except incrementing the counters, so the only time consuming logic here is the own CTS logic
 * - we will than be measuring the time when all of the tasks got drained
 */

public class ClusterTasksHeavyClusterTest {
	private static final Logger logger = LoggerFactory.getLogger(ClusterTasksHeavyClusterTest.class);
	private int numberOfNodes = 4;
	private int numberOfTasks = 100;

	@Test
	public void TestA_heavy_cluster() throws InterruptedException {
		//  load contexts to simulate cluster of a multiple nodes
		CountDownLatch waitForAllInit = new CountDownLatch(numberOfNodes);
		List<ApplicationContext> contexts = new LinkedList<>();
		ApplicationContext context;
		for (int i = 0; i < numberOfNodes; i++) {
			context = new ClassPathXmlApplicationContext(
					"classpath*:/SpringIOC/**/*.xml",
					"/cluster-tasks-heavy-cluster-context-test.xml"
			);
			contexts.add(context);
			context.getBean(ClusterTasksService.class)
					.getReadyPromise()
					.handleAsync((r, e) -> {
						if (r != null && r) {
							waitForAllInit.countDown();
						} else {
							throw new IllegalStateException("some of the contexts failed to get initialized", e);
						}
						return null;
					});
		}
		waitForAllInit.await();
		logger.info(numberOfNodes + " nodes initialized successfully");

		long startTime = System.currentTimeMillis();

		//  enqueue tasks for all of the contexts
		CountDownLatch waitForAllTasksPush = new CountDownLatch(numberOfNodes);
		ExecutorService tasksPushPool = Executors.newFixedThreadPool(numberOfNodes);
		contexts.forEach(c ->
				tasksPushPool.execute(() -> {
					try {
						for (int j = 0; j < numberOfTasks / 5; j++) {
							ClusterTasksService clusterTasksService = c.getBean(ClusterTasksService.class);
							ClusterTask task = TaskBuilders.simpleTask().build();
							clusterTasksService.enqueueTasks(ClusterTasksDataProviderType.DB, ClusterTasksHC_A_test.class.getSimpleName(), task);
							clusterTasksService.enqueueTasks(ClusterTasksDataProviderType.DB, ClusterTasksHC_B_test.class.getSimpleName(), task);
							clusterTasksService.enqueueTasks(ClusterTasksDataProviderType.DB, ClusterTasksHC_C_test.class.getSimpleName(), task);
							clusterTasksService.enqueueTasks(ClusterTasksDataProviderType.DB, ClusterTasksHC_D_test.class.getSimpleName(), task);
							clusterTasksService.enqueueTasks(ClusterTasksDataProviderType.DB, ClusterTasksHC_E_test.class.getSimpleName(), task);
						}
					} catch (Exception e) {
						logger.error("one of the nodes' task push failed", e);
					} finally {
						logger.info("one of the nodes done with tasks push");
						waitForAllTasksPush.countDown();
					}
				})
		);
		waitForAllTasksPush.await();
		long timeToPush = System.currentTimeMillis() - startTime;
		logger.info(numberOfNodes * numberOfTasks + " tasks has been pushed in " + timeToPush + "ms; average of " + (timeToPush / (numberOfNodes * numberOfTasks)) + "ms for task");

		//  wait for all tasks to be drained
		CountDownLatch waitForAllTasksDone = new CountDownLatch(5);
		ExecutorService tasksDonePool = Executors.newFixedThreadPool(5);
		tasksDonePool.execute(() -> {
			while (ClusterTasksHC_A_test.tasksProcessed < numberOfNodes * numberOfTasks / 5) {
				ClusterTasksITUtils.sleepSafely(100);
			}
			waitForAllTasksDone.countDown();
		});
		tasksDonePool.execute(() -> {
			while (ClusterTasksHC_B_test.tasksProcessed < numberOfNodes * numberOfTasks / 5) {
				ClusterTasksITUtils.sleepSafely(100);
			}
			waitForAllTasksDone.countDown();
		});
		tasksDonePool.execute(() -> {
			while (ClusterTasksHC_C_test.tasksProcessed < numberOfNodes * numberOfTasks / 5) {
				ClusterTasksITUtils.sleepSafely(100);
			}
			waitForAllTasksDone.countDown();
		});
		tasksDonePool.execute(() -> {
			while (ClusterTasksHC_D_test.tasksProcessed < numberOfNodes * numberOfTasks / 5) {
				ClusterTasksITUtils.sleepSafely(100);
			}
			waitForAllTasksDone.countDown();
		});
		tasksDonePool.execute(() -> {
			while (ClusterTasksHC_E_test.tasksProcessed < numberOfNodes * numberOfTasks / 5) {
				ClusterTasksITUtils.sleepSafely(100);
			}
			waitForAllTasksDone.countDown();
		});
		waitForAllTasksDone.await();
		long timeToDone = System.currentTimeMillis() - startTime - timeToPush;
		logger.info(numberOfNodes * numberOfTasks + " tasks has been processed in " + timeToDone + "ms; average of " + (timeToDone / (numberOfNodes * numberOfTasks)) + "ms for task");
	}
}
