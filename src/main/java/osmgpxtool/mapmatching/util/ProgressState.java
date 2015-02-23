package osmgpxtool.mapmatching.util;

/**
 * Interface designed to retrieve information about
 * the progress of an underlying process.
 *
 * E. g. this is used in process A that wants to display the
 * progress of process B running in another thread.
 *
 * @author Stefan Hahmann
 */
public interface ProgressState {

	/**
	 * Gets the percentage of progress of a process.
	 *
	 * @return the percentage value between 0 and 100.
	 */
	double getProgressPercent();

	/**
	 * Gets the total number of steps of this process.
	 *
	 * @return the total number of steps.
	 */
	int getTotalSteps();

	/**
	 * Gets the currently finished number of steps of this process.
	 *
	 * @return the current number of finished steps.
	 */
	int getProcessedSteps();

	/**
	 * Gets the number of steps, which still need to be done.
	 *
	 * @return the current number of unfinished steps.
	 */
	int getRemainingSteps();

	/**
	 * Gets the total time elapsed since observing the
	 * state of this process.
	 *
	 * @return the total elapsed time in ms.
	 */
	long getElapsedTime();

	/**
	 * Gets the predicted time still to be elapsed to finish
	 * the process.
	 *
	 * @return the time prediction in ms.
	 */
	long getPredictedRemainingTime();
	
	/**
	 * Gets the average time per processed step.
	 *
	 * @return the average time in ms.
	 */
	long getAverageTimePerStep();
	
	/**
	 * Gets the time of the current step.
	 * 
	 * @return the time of the current step
	 */
	long getCurrentStepTime();

	/**
	 * Gets the message associated with the current progress state.
	 * May be null.
	 *
	 * @return the message being assicoated with the current progress.
	 */
	String getProgressMessage();
}
