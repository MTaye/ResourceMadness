package com.mtaye.ResourceMadness;

import java.util.HashMap;

public class RMBanList extends HashMap<String, RMBanTicket>{
	
	public RMBanList(){
		new HashMap<String, RMBanTicket>();
	}
	
	private void add(RMBanTicket banTicket, String name){
		put(name.toLowerCase(), banTicket);
	}
	
	private void add(RMBanTicket banTicket, String... names){
		for(String name : names){
			add(banTicket, name);
		}
	}
	
	//Add
	public void add(String name){
		add(new RMBanTicket(), name);
	}
	
	public void add(String... names){
		add(new RMBanTicket(), names);
	}
	
	//Add Time
	public void add(int time, String name){
		add(new RMBanTicket(time), name);
	}
	
	public void add(int time, String... names){
		add(new RMBanTicket(time), names);
	}
	
	//Add Cause
	public void add(String cause, String name){
		add(new RMBanTicket(cause), name);
	}
	
	public void add(String cause, String... names){
		add(new RMBanTicket(cause), names);
	}

	//Add Time, Cause
	public void add(int time, String cause, String name){
		add(new RMBanTicket(time, cause), name);
	}
	
	public void add(int time, String cause, String... names){
		add(new RMBanTicket(time, cause), names);
	}
	
	//Remove
	public void rem(String name){
		if(containsKey(name)) remove(name);
	}
	
	public void rem(String... names){
		for(String name : names){
			rem(name);
		}
	}
	
	//isBanned
	public boolean isBanned(String name){
		if(containsKey(name.toLowerCase())) return true;
		else return false;
	}
}
