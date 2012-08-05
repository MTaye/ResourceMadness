package com.mtaye.ResourceMadness;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class Stats {
	public enum RMStatServer {
		WINS, LOSSES, ITEMS_FOUND_TOTAL,
		KICKED, BANNED, TEMP_BANNED;
	}
	
	public enum RMStat {
		WINS, LOSSES, KILLS, DEATHS, TIME_PLAYED, ITEMS_FOUND_TOTAL,
		KICKED, BANNED, TEMP_BANNED;
	}
	
	private static HashMap<RMStatServer, Integer> _statsServer = new HashMap<RMStatServer, Integer>();
	private HashMap<RMStat, Integer> _stats = new HashMap<RMStat, Integer>();
	public int _lastInt = 0; 
	
	public Stats(){
		reset();
	}
	
	public void reset(){
		for(RMStat stat : RMStat.values()){
			_stats.put(stat, 0);
		}
	}
	
	//Get
	public int get(RMStat stat){
		return _stats.get(stat);
	}
	
	//Get Ratio
	public String getSeparated(RMStat stat1, RMStat stat2){
		return getSeparated(stat1, stat2, ":");
	}
	
	public String getSeparated(RMStat stat1, RMStat stat2, String separator){
		return get(stat1)+separator+get(stat2);
	}
	
	public String getRatioString(RMStat stat1, RMStat stat2){
		if((stat1==null)||(stat2==null)) return "NULL";
		double ratio = getRatio(stat1, stat2);
		double round = Math.round(ratio);
		String result = "";
		if(ratio==round) return Integer.toString((int)ratio);
		try{
			result = Float.toString(Float.parseFloat(new DecimalFormat("#.##").format(ratio)));
		}
		catch(Exception e){
			result = new DecimalFormat("#.##").format(ratio);
			result = result.replace(",", ".");
		}
		return result;
	}
	
	public float getRatio(RMStat stat1, RMStat stat2){
		float s1 = get(stat1);
		float s2 = get(stat2);
		if(s2==0) s2++;
		return s1/s2;
	}
	
	//Set
	public void set(RMStat stat, int value){
		if(value==-1) return;
		_stats.put(stat, value);
	}
	
	//Add
	public void add(RMStat stat){
		_lastInt = get(stat);
		add(stat, 1);
		Debug.warning(stat.name()+": "+_lastInt+"+1="+get(stat));
	}
	public void add(RMStat stat, int value){
		_lastInt = get(stat);
		if(value<0) value = 0;
		_stats.put(stat, _stats.get(stat)+value);
		Debug.warning(stat.name()+": "+_lastInt+"+1="+get(stat));
	}
	
	//Clear
	public void clear(RMStat stat){
		_stats.put(stat, 0);
	}
	
	public void clearAll(){
		reset();
	}
	
	//Static
	
	static{
		for(RMStatServer stat : RMStatServer.values()){
			_statsServer.put(stat, 0);
		}
	}
	
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
	
	public static void clearAllServer(){
		_statsServer.clear();
	}
}