package pl.danlz.remotecontrol.samsung.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.concurrent.Task;
import pl.danlz.remotecontrol.samsung.logger.Logger;

/**
 * Executes submitted task directly, but skips execution if a task is being
 * currently executed.
 * 
 * @author Leszek
 */
public class SkippingExecutorService implements DirectExecutorService {

	private static final Logger LOG = Logger.getLogger(SkippingExecutorService.class);

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "Background Thread");
		}
	});

	private Task<?> currentTask;

	@Override
	public void execute(Task<?> command) {
		if (currentTask == null || !currentTask.isRunning()) {
			currentTask = command;
			executor.execute(currentTask);
		} else {
			LOG.debug("Skipping task");
		}
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}
}
