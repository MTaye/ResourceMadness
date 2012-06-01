package com.mtaye.ResourceMadness.time;

import java.util.Arrays;

public enum TimeType {
	SECOND(0, 1),
	MINUTE(1, 60),
	HOUR(2, 3600),
	DAY(3, 86400);
	
	private int id;
	private int seconds;
	private static TimeType[] byId = new TimeType[4];
	
	private TimeType(int id, int seconds){
		this.id = id;
		this.seconds = seconds;
	}
	
	/**
	 * Returns the ordinal id of this TimeType.
	 * @return
	 */
	public int getId(){
		return id;
	}
	
	/**
	 * Returns an int of this TimeType in seconds.
	 * @return
	 */
	public int getSeconds(){
		return seconds;
	}
	
	/**
	 * Returns the ordinal id of this TimeType.
	 * @return
	 */
	public static int getId(TimeType timeType){
		return timeType.id;
	}
	
	/**
	 * Returns an int of this TimeType in seconds.
	 * @return
	 */
	public static int getSeconds(TimeType timeType){
		return timeType.seconds;
	}
	
	static{
		for (TimeType tt : values()) {
            if (byId.length > tt.id) {
                byId[tt.id] = tt;
            } else {
                byId = Arrays.copyOfRange(byId, 0, tt.id + 2);
                byId[tt.id] = tt;
            }
        }
	}
};
