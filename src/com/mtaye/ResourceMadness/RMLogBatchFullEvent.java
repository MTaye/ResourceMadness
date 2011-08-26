package com.mtaye.ResourceMadness;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.event.Event;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMLogBatchFullEvent extends Event {
	private HashMap<Location, RMBlock> _logList;
	private HashMap<Location, RMBlock> _logItemList;
	
	public RMLogBatchFullEvent(HashMap<Location, RMBlock> logList, HashMap<Location, RMBlock> logItemList){
		super("RMLogBatchFullEvent");
		_logList = logList;
		_logItemList = logItemList;
	}
	
	public HashMap<Location, RMBlock> getLogList(){
		return _logList;
	}
	public HashMap<Location, RMBlock> getLogItemList(){
		return _logItemList;
	}
}