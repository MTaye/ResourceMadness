package com.mtaye.ResourceMadness;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMLogListener extends CustomEventListener{
	RM plugin;
	
	public RMLogListener(RM plugin){
		this.plugin = plugin;
	}
	public void onRMLogBatchFull(RMLogBatchFullEvent event){
	}
	
	@Override
	public void onCustomEvent(Event e){
		if(e.getEventName().equals("RMLogBatchFullEvent")){
			RMLogBatchFullEvent event = (RMLogBatchFullEvent)e;
			onRMLogBatchFull(event);
		}
	}
}