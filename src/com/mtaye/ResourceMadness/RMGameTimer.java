package com.mtaye.ResourceMadness;

import java.util.HashMap;

import org.bukkit.ChatColor;

public class RMGameTimer {
	HashMap<Integer, String> _timeMessages = new HashMap<Integer, String>();
	private int _timeElapsed = 0;
	private int _timeLimit = 0;
	
	public static int minuteInSeconds = 60;
	public static int hourInSeconds = minuteInSeconds*60;
	public static int hourInMinutes = 60;
	
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
		addDefaultTimeMessages();
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
	public void addDefaultTimeMessages(){
		addTimeMessage(1, ChatColor.AQUA+"1");
		addTimeMessage(2, ChatColor.AQUA+"2");
		addTimeMessage(3, ChatColor.AQUA+"3");
		addTimeMessage(4, ChatColor.AQUA+"4");
		addTimeMessage(5, ChatColor.AQUA+"5");
		addTimeMessage(10);
		addTimeMessage(30);
		addTimeMessage(1*minuteInSeconds);
		addTimeMessage(2*minuteInSeconds);
		addTimeMessage(5*minuteInSeconds);
		addTimeMessage(10*minuteInSeconds);
		addTimeMessage(20*minuteInSeconds);
		addTimeMessage(30*minuteInSeconds);
		addTimeMessage(1*minuteInSeconds);
		addTimeMessage(2*minuteInSeconds);
		addTimeMessage(5*minuteInSeconds);
		addTimeMessage(10*minuteInSeconds);
	}
	public void playCustomTimeMessage(RMGame game, int time, String message){
		game.teamBroadcastMessage(ChatColor.AQUA+getTextTime(time)+" remaining."); 
	}
	
	//Reset
	public void reset(){
		_timeElapsed = 0;
		_timeMessages.clear();
		addDefaultTimeMessages();
	}
	
	//Special
	public String getTextTime(){
		return getTextTime(_timeElapsed);
	}
	public String getTextTimeRemaining(){
		return getTextTime(getTimeRemaining());
	}
	public String getTextTimeLimit(){
		return getTextTime(_timeLimit);
	}
	
	public String getTextTime(int time){
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		hours = (int)(time/hourInSeconds);
		time = time-(hours*hourInSeconds);
		minutes = (int)(time/minuteInSeconds);
		time = time-(minutes*minuteInSeconds);
		seconds = time;
		if(hours>0) return hours+" hour(s) "+(minutes>0?minutes+" minute(s)":"") + (seconds>0?seconds+" second(s)":"");
		else if(minutes>0) return minutes+" minute(s) "+(seconds>0?seconds+" second(s)":"");
		else return seconds+" second(s)";
	}
	
	public void announceTimeLeft(RMGame game){
		for(Integer time : _timeMessages.keySet()){
			if(time==getTimeRemaining()) game.teamBroadcastMessage(_timeMessages.get(time)); 
		}
	}
}