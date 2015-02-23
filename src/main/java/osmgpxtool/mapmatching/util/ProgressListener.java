package osmgpxtool.mapmatching.util;

/**
 * Interface designed for updating step-based progress information.
 *
 * E.g. this can be used in a time-consuming process B that
 * shall be observed within process A running in another thread.
 * By calling update() B notifies A about the current process state.
 * By calling checkTimeout() B can check if the a timeout defined for
 * this process has been reached yet.
 *
 */
public interface ProgressListener {

	/**
	 * Updates information about the current process.
	 *
	 * @param currentCount number of currently finished steps.
	 */
	void update(int currentCount);
	
	/**
	 * Increments this progress with by one step.
	 *
	 */
	void increment();
	
	/**
	 * Start this progress without (re)setting the overall steps to be done.
	 */
	void start();
	
	/**
	 * Start this progress and set the overall steps to be done.
	 * 
	 * @param overall overall steps to be done.
	 */
	void start(int overall);
}
