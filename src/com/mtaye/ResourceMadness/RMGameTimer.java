package com.mtaye.ResourceMadness;

import java.util.HashMap;

import org.bukkit.ChatColor;

public class RMGameTimer {
	HashMap<Integer, String> _timeMessages = new HashMap<Integer, String>();
	private int _timeElapsed = 0;
	private int _timeLimit = 0;
	
	public static int minute = 60;
	public static int hour = minute*60;
	
	public RMGameTimer(){
	}
	public RMGameTimer(int timeElapsed){
		setTimeElapsed(timeElapsed);
		init();
	}
	public RMGameTimer(int timeElapsed, int timeLimit){
		setTimeElapsed(timeElapsed);
		setTimeLimit(timeLimit);
		init();
	}
	
	public void init(){
		addTimeMessage(1, ChatColor.AQUA+"1");
		addTimeMessage(2, ChatColor.AQUA+"2");
		addTimeMessage(3, ChatColor.AQUA+"3");
		addTimeMessage(4, ChatColor.AQUA+"4");
		addTimeMessage(5, ChatColor.AQUA+"5");
		addTimeMessage(10);
		addTimeMessage(30);
		addTimeMessage(1*minute);
		addTimeMessage(2*minute);
		addTimeMessage(5*minute);
		addTimeMessage(10*minute);
		addTimeMessage(20*minute);
		addTimeMessage(30*minute);
		addTimeMessage(1*hour);
		addTimeMessage(2*hour);
		addTimeMessage(5*hour);
		addTimeMessage(10*hour);
	}
	
	//Get
	public int getTimeElapsed(){
		return _timeElapsed;
	}
	public int getTimeLimit(){
		return _timeLimit;
	}
	public int getTimeRemaining(){
		int timeRemaining = _timeLimit-_timeElapsed;
		return timeRemaining<0?0:timeRemaining;
	}
	public HashMap<Integer, String> getTimeMessages(){
		return _timeMessages;
	}
	
	//Set
	public void setTimeElapsed(int timeElapsed){
		_timeElapsed = timeElapsed;
		if(_timeElapsed<0) _timeElapsed = 0;
	}
	public void setTimeLimit(int timeLimit){
		_timeLimit = timeLimit;
		if(_timeLimit<0) _timeLimit = 0;
	}
	public void setTimeMessages(HashMap<Integer, String> timeMessages){
		_timeMessages = timeMessages;
	}
	
	//Clear
	public void clearTimeElapsed(){
		_timeElapsed = 0;
	}
	public void clearTimeLimit(){
		_timeLimit = 0;
	}
	public void clearTimeMessages(){
		_timeMessages.clear();
	}
	
	//Add
	public void addTimeElapsed(){
		_timeElapsed++;
	}
	public void addTimeElapsed(int value){
		_timeElapsed+=value;
	}
	public void addTimeMessage(int time){
		_timeMessages.put(time, ChatColor.AQUA+getTextTime(time)+" remaining.");
	}
	public void addTimeMessage(int time, String message){
		_timeMessages.put(time, message);
	}
	
	//Special
	public String getTextTime(int time){
		if(time%hour==0) return time/hour + " hour(s)";
		else if(time%minute==0) return time/minute + " minute(s)";
		else return time + " second(s)";
	}
	
	public void announceTimeLeft(RMGame game){
		for(Integer time : _timeMessages.keySet()){
			if(time==getTimeRemaining()) game.teamBroadcastMessage(_timeMessages.get(time)); 
		}
	}
}