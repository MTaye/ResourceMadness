package com.mtaye.ResourceMadness;

import java.util.HashMap;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMStats {
	public enum RMStatServer {
		WINS, LOSSES, TIMES_PLAYED, ITEMS_FOUND_TOTAL,
		KICKED, BANNED, TEMP_BANNED;
	}
	
	public enum RMStat {
		WINS, LOSSES, TIMES_PLAYED, ITEMS_FOUND_TOTAL,
		KICKED, BANNED, TEMP_BANNED;
	}
	
	private static HashMap<RMStatServer, Integer> _statsServer = new HashMap<RMStatServer, Integer>();
	private HashMap<RMStat, Integer> _stats = new HashMap<RMStat, Integer>();
	
	public RMStats(){
		for(RMStat stat : RMStat.values()){
			_stats.put(stat, 0);
		}
		for(RMStatServer stat : RMStatServer.values()){
			_statsServer.put(stat, 0);
		}
	}
	
	//Get
	public int get(RMStat stat){
		return _stats.get(stat);
	}
	
	//Get Ratio
	public String getTextRatio(){
		return get(RMStat.WINS)+":"+get(RMStat.LOSSES);
	}
	
	//Set
	public void set(RMStat stat, int value){
		if(value==-1) return;
		_stats.put(stat, value);
	}
	
	//Add
	public void add(RMStat stat){
		add(stat, 1);
	}
	public void add(RMStat stat, int value){
		if(value<0) value = 0;
		_stats.put(stat, _stats.get(stat)+value);
	}
	
	//Clear
	public void clear(RMStat stat){
		_stats.put(stat, 0);
	}
	
	//Static
	
	//Get
	public static int get(RMStatServer stat){
		return _statsServer.get(stat);
	}
	
	//Get Ratio
	public static String getServerTextRatio(){
		return get(RMStatServer.WINS)+":"+get(RMStatServer.LOSSES);
	}
	
	//Set
	public static void set(RMStatServer stat, int value){
		if(value==-1) return;
		_statsServer.put(stat, value);
	}
	
	//Add
	public static void add(RMStatServer stat){
		add(stat, 1);
	}
	public static void add(RMStatServer stat, int value){
		if(value<0) value = 0;
		_statsServer.put(stat, _statsServer.get(stat)+value);
	}
	
	//Clear
	public static void clear(RMStatServer stat){
		_statsServer.put(stat, 0);
	}
}