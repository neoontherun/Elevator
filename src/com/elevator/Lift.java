package com.elevator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.elevator.model.ILift;
import com.elevator.sensor.model.ISensor;

public class Lift implements ILift {

	SortedMap<Integer, Integer> requests = Collections.synchronizedSortedMap(new TreeMap<Integer, Integer>());

	AtomicBoolean open = new AtomicBoolean(Boolean.FALSE);

	AtomicBoolean stop = new AtomicBoolean(Boolean.TRUE);

	Integer id = 0;

	Integer numOfFloors;

	Integer currentFloor = 0;

	Integer currentRequestedFloor = 0;

	Boolean amIBusy = Boolean.FALSE;

	SharedJobsCount jobsCount = SharedJobsCount.getInstance();

	Set<ISensor> sensors = new HashSet<ISensor>();
	
	Timer sensorTimer;

	ExecutorService service;
	
	public Lift(Integer liftId, Integer noOfFloors) {
		this.numOfFloors = noOfFloors;
	}
	
	public Lift(Integer liftId, Integer noOfFloors, Set<ISensor> sensorList) {
		this.numOfFloors = noOfFloors;
		sensors = sensorList;
	}

	public boolean open() {
		System.out.println("Opening the door");
		return open.compareAndSet(Boolean.FALSE, Boolean.TRUE);
	}

	public boolean close() {
		System.out.println("Closing the door");
		return open.compareAndSet(Boolean.TRUE, Boolean.FALSE);
	}

	public void stop() {
		stop.set(Boolean.TRUE);
		sensorTimer.cancel();
		service.shutdown();
		System.out.println("Lift Stopped");
	}

	public void takeMeTo(Integer floor) {
		if (floor < 0 || floor > numOfFloors) {
			// return for invalid floors
			System.out.println("Invalid Floor : " + floor);
			return;
		}

		if (requests.get(floor) == null) { // add to request if not already in
			// the request
			System.out.println("Creating lift request.. for floor : " + floor);
			requests.put(floor, floor);
			jobsCount.incrementJobsCountFor(id);
			//currentRequestedFloor = floor;
		} else { // if already present, no need to do anything, it will be done.
			System.out.println("Request already present for floor " + floor);
			return;
		}
	}

	private void openAndWaitAndCloseTheLiftDoor() throws InterruptedException {
		open();
		synchronized (this) {
			this.wait(3000);
		}
		close();
	}

	public Integer getPendingJobs() {
		return requests.size();
	}

	public void announce() {
		System.out.println("Announcement Made");
	}

	public boolean isOpen() {
		return open.get();
	}

	@Override
	public void setLiftId(Integer id) {
		this.id = id;
	}

	@Override
	public Integer getLiftId() {
		return id;
	}

	@Override
	public void goUp() {
		while (currentFloor < currentRequestedFloor) {
			currentFloor++;
			System.out.println("Going up. Went to floor : " + currentFloor);
			Integer floorRequest = requests.firstKey();
//			System.out.println("FirstKey" + requests.firstKey() + " LastKey" + requests.lastKey());
			if (floorRequest == currentFloor) {
				try {
					openAndWaitAndCloseTheLiftDoor();
					requests.remove(floorRequest);
					jobsCount.decrementJobsCountFor(id);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void goDown() {
		while (currentFloor > currentRequestedFloor) {
			currentFloor--;
			System.out.println("Going down. Went to floor : " + currentFloor);
			if (requests.firstKey() == currentFloor) {
				try {
					openAndWaitAndCloseTheLiftDoor();
					requests.remove(requests.firstKey());
					jobsCount.decrementJobsCountFor(id);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void stayIntheSameFloor() throws InterruptedException {
		System.out.println("We are in the same floor.. Opening the door..");
		openAndWaitAndCloseTheLiftDoor();
		requests.remove(currentRequestedFloor);
		jobsCount.decrementJobsCountFor(id);
	}

	@Override
	public void addSensors(ISensor sensor) {
		sensors.add(sensor);
	}

	@Override
	public void start() {
		scheduleSensors();
		startLiftOperationsExecutor();
	}
	
	private void startLiftOperationsExecutor() {
		service = Executors.newFixedThreadPool(1);
		LiftJobExecutor command = new LiftJobExecutor();
		service.execute(command);
	}

	private void scheduleSensors() {
		sensorTimer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				try {
					for (ISensor iSensor : sensors) {
						if (iSensor.senseIssue()) {
							System.out.println("Issue Detected. Please take necessary actions..");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		sensorTimer.schedule(timerTask, 10000, 10000);
	}

	class LiftJobExecutor implements Runnable {

		public void run() {
			while (true) {
				waitForJobs();
				System.out.println("Pending jobs : " + getPendingJobs());
				checkBusyAndExecute();
			}
		}

		private boolean canExecuteRequest(Integer floor) {
			return !amIBusy && currentFloor != floor;
			//&& !isOpen();
		}

		private void checkBusyAndExecute() {
			// this part needs to be kind of schedule & exit. There needs to be
			// another guy taking care of these requests.
			try {
				executeRequests();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void executeRequests() throws InterruptedException {
		currentRequestedFloor = requests.firstKey();
		System.out.println("Got floor " + currentRequestedFloor + " as request..");
		amIBusy = true;
		if (currentRequestedFloor > currentFloor) {
			goUp();
		} else if (currentRequestedFloor < currentFloor) {
			goDown();
		} else {
			stayIntheSameFloor();
		}
		amIBusy = false;
	}

	private void waitForJobs() {
		while (requests.isEmpty()) {
			try {
				System.out.println("Waiting for requests...");
				synchronized (this) {
					this.wait(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}