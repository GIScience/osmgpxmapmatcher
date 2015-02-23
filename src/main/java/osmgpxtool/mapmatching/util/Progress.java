package osmgpxtool.mapmatching.util;


/**
 * Class for transferring the progress status of a task
 * running on a different machine.
 * By default, for each signal to transfer, a block of bytes
 * of at least the size passed as input parameter will be
 * written to a specified output stream.
 *
 */
public class Progress
implements ProgressListener, ProgressState {

	/**
	 * The start time of a progress in milliseconds since 1.1.1970
	 */
    private long startTime = 0;

    /**
	 * The current time of a progress in milliseconds since 1.1.1970
	 */
    private long currentTime = 0;
    
    /**
	 * The time of the last step of the progress in milliseconds since 1.1.1970
	 */
    private long lastTime = 0;

	/**
	 * Number of currently processed steps
	 */
	private int currentStep;

	/**
	 * Number of total steps that need to processed
	 */
	private int overall;

	public Progress() {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(int current) {

		lastTime = currentTime;
		currentTime = System.currentTimeMillis();
		this.currentStep = current;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void increment() {
		update(this.currentStep + 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public void start(int overall) {
		this.start();
		this.overall = overall;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {
		currentTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		currentStep = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getProcessedSteps() {
		return currentStep;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTotalSteps() {
		return overall;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProgressMessage(String currentElement) {
		String message = "step=" + getProcessedSteps() + "/" + getTotalSteps() + " (" + Math.round(getProgressPercent()) + "%)";
		if (currentElement != null) {
			message += ", current element="+currentElement;
		}
		message += "\n";
		message += "time for current step="+TimeTools.convertMillisToHourMinuteSecond(getCurrentStepTime())+", current time="+TimeTools.getLogTime();
		message += "\n";
		message += "elapsed time="+(TimeTools.convertMillisToHourMinuteSecond(getElapsedTime()))+", processed steps="+getProcessedSteps()+", average time per step="+TimeTools.convertMillisToHourMinuteSecond(getAverageTimePerStep());
		message += "\n";
		message += "remaining time="+(TimeTools.convertMillisToHourMinuteSecond(getPredictedRemainingTime()))+", remaining steps="+getRemainingSteps();
		
		return message;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getProgressMessage() {

		return getProgressMessage(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public double getProgressPercent() {
		if (getTotalSteps() > 0) {
			double percent = 100d * getProcessedSteps() / getTotalSteps();
			if (percent < 0)
				percent = 0;
			if (percent > 100)
				percent = 100;
			return percent;
		}
		else {
			return 0;
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRemainingSteps() {
		return (getTotalSteps() - getProcessedSteps());
	}

	/**
	 * {@inheritDoc}
	 */
	public long getElapsedTime() {
		if (currentTime == 0)
			return currentTime;
		return currentTime - startTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPredictedRemainingTime() {

		if (getProcessedSteps() > 0) {
			return getRemainingSteps() * getElapsedTime() / getProcessedSteps();
		}
		else {
			return 0;
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getAverageTimePerStep() {

		if (getProcessedSteps() > 0) {
			return Math.round(getElapsedTime() / getProcessedSteps());
		}
		else {
			return 0;
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getCurrentStepTime() {
		return currentTime - lastTime;
	}
}
