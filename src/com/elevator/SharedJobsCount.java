package com.elevator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedJobsCount {
	
	Map<Integer, Integer> jobsCount = new ConcurrentHashMap<Integer, Integer>();
	
	static SharedJobsCount instance = new SharedJobsCount();
	
	public static void incrementJobsCountFor(Integer liftId) {
		if (instance.jobsCount.get(liftId) == null) {
			instance.jobsCount.put(liftId, 1);
		}
		Integer jobsCounter = instance.jobsCount.get(liftId);
		jobsCounter++;
	}
	
	public static void decrementJobsCountFor(Integer liftId) {
		if (instance.jobsCount.get(liftId) == null) {
			return;
		}
		Integer jobsCounter = instance.jobsCount.get(liftId);
		if (jobsCounter < 0) {
			return;
		}
		jobsCounter--;
	}
	
	private SharedJobsCount() {
		
	}

	public static SharedJobsCount getInstance() {
		return instance;
	}
}
