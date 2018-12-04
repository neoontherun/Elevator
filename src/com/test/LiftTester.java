package com.test;

import java.util.HashSet;
import java.util.Set;

import com.elevator.Lift;
import com.elevator.model.ILift;
import com.elevator.sensor.FireSensor;
import com.elevator.sensor.PowerSensor;
import com.elevator.sensor.model.ISensor;

public class LiftTester {
	
	public static void main(String[] args) {
		Integer noOfFloors = 5;
		Set<ISensor> sensorList = new HashSet<ISensor>();
		ISensor sensor = new FireSensor();
		sensorList.add(sensor);
		sensor = new PowerSensor();
		sensorList.add(sensor);
		ILift lift = new Lift(1, 5, sensorList);
		lift.start();
//		System.out.println("Pending jobs : " + lift.getPendingJobs());
		lift.takeMeTo(7);
		lift.takeMeTo(3);
		lift.takeMeTo(4);
		lift.takeMeTo(0);
//		System.out.println("Pending jobs : " + lift.getPendingJobs());
		lift.takeMeTo(1);
		lift.takeMeTo(3);
//		System.out.println("Pending jobs : " + lift.getPendingJobs());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ie){
			ie.printStackTrace();
		}
		lift.takeMeTo(1);
		lift.takeMeTo(2);
	}

}
