package com.mtaye.ResourceMadness;

import java.util.HashMap;

public class BanList extends HashMap<String, BanTicket>{
	
	public BanList(){
		new HashMap<String, BanTicket>();
	}
	
	private void add(BanTicket banTicket, String name){
		put(name.toLowerCase(), banTicket);
	}
	
	private void add(BanTicket banTicket, String... names){
		for(String name : names){
			add(banTicket, name);
		}
	}
	
	//Add
	public void add(String name){
		add(new BanTicket(), name);
	}
	
	public void add(String... names){
		add(new BanTicket(), names);
	}
	
	//Add Time
	public void add(int time, String name){
		add(new BanTicket(time), name);
	}
	
	public void add(int time, String... names){
		add(new BanTicket(time), names);
	}
	
	//Add Cause
	public void add(String cause, String name){
		add(new BanTicket(cause), name);
	}
	
	public void add(String cause, String... names){
		add(new BanTicket(cause), names);
	}

	//Add Time, Cause
	public void add(int time, String cause, String name){
		add(new BanTicket(time, cause), name);
	}
	
	public void add(int time, String cause, String... names){
		add(new BanTicket(time, cause), names);
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
