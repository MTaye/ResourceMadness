package com.mtaye.ResourceMadness;

public class RMBanTicket {
	private int _time = 0;
	private String _cause = "";
	//private List<String> _messages = new ArrayList<String>();
	
	public RMBanTicket(){
		//initMessages();
	}
	
	public RMBanTicket(int time){
		//initMessages();
		setTime(time);
	}
	
	public RMBanTicket(String cause){
		//initMessages();
		setCause(cause);
	}
	
	public RMBanTicket(int time, String cause){
		//initMessages();
		setTime(time);
		setCause(cause);
	}
	
	/*
	public void initMessages(){
		_messages.add("Caught cheating!");
		_messages.add("You stole from that guy Cvirps");
		_messages.add("Fuck you asshole, no block breaking!");
		_messages.add("You were caught griefing. Banned.");
		_messages.add("You were temp banned (1 day)");
		_messages.add("Banned because you're sparky");
		_messages.add("Fafner Challenge is here and you are out.");
		_messages.add("Hacking items is not allowed");
		_messages.add("X-ray mod is not allowed");
		_messages.add("Flying is forbidden");
		_cause = _messages.get((int)(Math.random()*_messages.size()));
		_time = (int)(Math.random()*24);
	}
	*/
	
	public void setTime(int time){
		if(time<0) time = 0;
		_time = time;
	}
	
	public int getTime(){
		return _time;
	}
	
	public void setCause(String cause){
		if((cause==null)||(cause.length()==0)) return;
		else _cause = cause;
	}
	
	public String getCause(){
		return _cause;
	}
}
