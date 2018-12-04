package com.elevator.model;

import com.elevator.sensor.model.ISensor;

public interface ILift {
	
	boolean close();
	
	void start();
	
	void stop();
	
	boolean isOpen();
	
	void takeMeTo(Integer floor);
	
	Integer getPendingJobs();
	
	void announce();
	
	void setLiftId(Integer id);
	
	Integer getLiftId();
	
	void goUp();
	
	void goDown();
	
	void addSensors(ISensor sensor);
}
