package model;

public class Timer {
	
	private long startTime;
	private long stopTime;
		
	/**
	 * Gets the start time.
	 * 
	 * @return	the start time
	 */
	private long getStartTime() {
		return startTime;
	}
	
	/**
	 * Sets the start time.
	 * 
	 * @param startTime	the start time
	 */
	private void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets the stop time.
	 * 
	 * @return	the stop time
	 */
	private long getStopTime() {
		return stopTime;
	}

	/**
	 * Sets the stop time.
	 * 
	 * @param stopTime	the stop time
	 */
	private void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	
	/**
	 * Method which calculates the difference between stop time and start time.
	 * 
	 * @return	the difference between stop time and start time
	 */
	public long enlapsedTime()
	{
		return getStopTime() - getStartTime();
	}
	
	/**
	 * Method which sets the start time of calculations.
	 */
	public void startMeasure()
	{
		setStartTime(System.currentTimeMillis());
	}
	
	/**
	 * Method which sets the stop time of calculations.
	 */
	public void endMeasure()
	{
		setStopTime(System.currentTimeMillis());
	}

}
