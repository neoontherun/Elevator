package com.elevator.sensor;

import com.elevator.sensor.model.ISensor;

public class FireSensor implements ISensor {
	
	public static final String SENSOR_NAME = "FireSensor";
	
	Integer issueIntervalCounter = 0;

	@Override
	public boolean senseIssue() {
		if (issueIntervalCounter % 50 == 0) {
			return true;
		}
		return false;
	}

}
