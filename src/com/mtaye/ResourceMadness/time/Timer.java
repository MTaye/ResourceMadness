package com.mtaye.ResourceMadness.time;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.Game;
import com.mtaye.ResourceMadness.Text;
import com.mtaye.ResourceMadness.helper.Helper;

public class Timer implements Cloneable {
	HashMap<Integer, String> _timeMessages = new HashMap<Integer, String>();
	private int _timeElapsed = 0;
	private int _timeLimit = 0;
	
	public static int minuteInSeconds = 60;
	public static int hourInSeconds = minuteInSeconds*60;
	public static int hourInMinutes = 60;
	public static int dayInSeconds = hourInSeconds*24;
	public static int dayInMinutes = hourInMinutes*24;
	public static int dayInHours = 24;
	
	public Timer(){
		this(false);
	}
	public Timer(boolean addDefault){
		if(addDefault) addDefaultTimeMessages();
	}
	public Timer(int timeElapsed){
		this(timeElapsed, false);
	}
	public Timer(int timeElapsed, boolean addDefault){
		setTimeElapsed(timeElapsed);
		if(addDefault) addDefaultTimeMessages();
	}
	public Timer(int timeElapsed, int timeLimit){
		this(timeElapsed, timeLimit, false);
	}
	public Timer(int timeElapsed, int timeLimit, boolean addDefault){
		setTimeElapsed(timeElapsed);
		setTimeLimit(timeLimit);
		if(addDefault) addDefaultTimeMessages();
	}
	public Timer(int timeElapsed, HashMap<Integer, String> timeMessages){
		setTimeElapsed(timeElapsed);
		addTimeMessages(timeMessages);
	}
	public Timer(int timeElapsed, int timeLimit, HashMap<Integer, String> timeMessages){
		setTimeElapsed(timeElapsed);
		setTimeLimit(timeLimit);
		addTimeMessages(timeMessages);
	}
	
	//Boolean
	public boolean atStart(){
		if(_timeElapsed==0) return true;
		else return false;
	}
	public boolean atEnd(){
		if(_timeElapsed==_timeLimit) return true;
		else return false;
	}
	public boolean isTicking(){
		if(_timeElapsed<_timeLimit) return true;
		return false;
	}
	public boolean isSet(){
		if(_timeLimit!=0) return true;
		return false;
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
	public void setTimeElapsed(String... args){
		setTimeElapsed(parseTime(args));
	}
	public void setTimeElapsed(int timeElapsed){
		_timeElapsed = timeElapsed;
		if(_timeElapsed<0) _timeElapsed = 0;
	}
	public void setTimeLimit(String... args){
		setTimeElapsed(parseTime(args));
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
	public void addTimeElapsed(int time){
		_timeElapsed+=time;
	}
	
	//Add Time Message
	public void addTimeMessage(String... args){
		int time = parseTime(args);
		addTimeMessage(time);
	}
	public void addTimeMessage(String message, String... args){
		int time = parseTime(args);
		addTimeMessage(time, message);
	}

	public void addTimeMessage(int time){
		addTimeMessage(time, Text.getLabelArgs("time.remaining", getTextTime(time)));
	}
	public void addTimeMessage(int time, String message){
		_timeMessages.put(time, message);
	}
	public void addTimeMessages(HashMap<Integer, String> timeMessages){
		for(Integer time : timeMessages.keySet()){
			addTimeMessage(time, timeMessages.get(time));
		}
	}
	public void addDefaultTimeMessages(){
		if(getTimeLimit()!=0) addTimeMessage(getTimeLimit());
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
		addTimeMessage(1*hourInSeconds);
		addTimeMessage(2*hourInSeconds);
		addTimeMessage(5*hourInSeconds);
		addTimeMessage(10*hourInSeconds);
	}
	public void playCustomTimeMessage(Game game, int time, String message){
		game.teamBroadcastMessage(Text.getLabelArgs("time.remaining", getTextTime(time))); 
	}
	
	/**
	 * Resets the elapsed time. 
	 */
	public void reset(){
		_timeElapsed = 0;
	}
	
	/**
	 * Resets the elapsed time, clears the time messages and adds the default time messages.
	 */
	public void resetDefaults(){
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
		return getTextTimeStatic(time);
	}
	
	public void announceTimeLeft(Game game){
		for(Integer time : _timeMessages.keySet()){
			if(time==getTimeRemaining()) game.teamBroadcastMessage(_timeMessages.get(time)); 
		}
	}
	
	@Override
	public Timer clone(){
		Timer result = new Timer(_timeElapsed, _timeLimit, _timeMessages);
		return result;
	}
	
	//Static
	public static String getTextTimeStatic(int time){
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		days = (int)(time/dayInSeconds);
		time = time-(days*dayInSeconds);
		hours = (int)(time/hourInSeconds);
		time = time-(hours*hourInSeconds);
		minutes = (int)(time/minuteInSeconds);
		time = time-(minutes*minuteInSeconds);
		seconds = time;
		String str = "";
		if(days>0) str+=" "+days+" "+Text.getLabel("time.days");
		if(hours>0) str+=" "+hours+" "+Text.getLabel("time.hours");
		if(minutes>0) str+=" "+minutes+" "+Text.getLabel("time.minutes");
		if(seconds>0) str+=" "+seconds+" "+Text.getLabel("time.seconds");
		return str.trim();
	}
	
	public static int parseTime(String... args){
		int result = 0;
		for(String time : args){
			if(time.length()<2) continue;
			if(time.contains("d")) result += parseTimeInt(time, TimeType.DAY);
			else if(time.contains("h")) result += parseTimeInt(time, TimeType.HOUR);
			else if(time.contains("m")) result += parseTimeInt(time, TimeType.MINUTE);
			else result += parseTimeInt(time, TimeType.SECOND);
		}
		return result;
	}
	
	private static int parseTimeInt(String time, TimeType tt){
		if(time.length()<2) return 0;
		int t = Helper.getIntByString((time.substring(0, time.length()-1)), 0);
		return t;
	}
}