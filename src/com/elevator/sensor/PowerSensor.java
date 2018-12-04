package com.elevator.sensor;
import com.elevator.sensor.model.ISensor;

public class PowerSensor implements ISensor {
	
	Integer issueIntervalCounter = 0;
	
	public static final String SENSOR_NAME = "PowerSensor";

	@Override
	public boolean senseIssue() {
		if (issueIntervalCounter % 100 == 0) {
			return true;
		}
		return false;
	}

}
