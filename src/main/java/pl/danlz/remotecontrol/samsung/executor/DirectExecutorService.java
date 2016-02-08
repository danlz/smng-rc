package pl.danlz.remotecontrol.samsung.executor;

import javafx.concurrent.Task;

/**
 * Executor service, which executes the submitted task directly (or with minimal
 * delay).
 * 
 * @author Leszek
 */
public interface DirectExecutorService {

	/**
	 * Executes given task.
	 * 
	 * @param task
	 *            the task
	 */
	void execute(Task<?> task);

	/**
	 * Initiates shutdown of this executor.
	 */
	void shutdown();
}
