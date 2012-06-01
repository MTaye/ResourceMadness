package com.mtaye.ResourceMadness.time;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.Game;
import com.mtaye.ResourceMadness.Text;
import com.mtaye.ResourceMadness.helper.Helper;

public class PvpTimer extends Timer {
	
	public PvpTimer(){
		this(false);
	}
	public PvpTimer(boolean addDefault){
		super(addDefault);
	}
	public PvpTimer(int timeElapsed){
		super(timeElapsed);
	}
	public PvpTimer(int timeElapsed, boolean addDefault){
		super(timeElapsed, addDefault);
	}
	public PvpTimer(int timeElapsed, int timeLimit){
		super(timeElapsed, timeLimit);
	}
	public PvpTimer(int timeElapsed, int timeLimit, boolean addDefault){
		super(timeElapsed, timeLimit, addDefault);
	}
	public PvpTimer(int timeElapsed, HashMap<Integer, String> timeMessages){
		super(timeElapsed, timeMessages);
	}
	public PvpTimer(int timeElapsed, int timeLimit, HashMap<Integer, String> timeMessages){
		super(timeElapsed, timeLimit, timeMessages);
	}
	
	@Override
	public void addDefaultTimeMessages(){
		if(getTimeLimit()!=0) addTimeMessage(getTimeLimit(), Text.getLabelArgs("game.pvp.delay", ""+Timer.getTextTimeStatic(getTimeLimit())));
		addTimeMessage(30, Text.getLabelArgs("game.pvp.delay", ""+Timer.getTextTimeStatic(30)));
		addTimeMessage(10, Text.getLabelArgs("game.pvp.delay", ""+Timer.getTextTimeStatic(10)));
		addTimeMessage(5, ChatColor.AQUA+"5");
		addTimeMessage(4, ChatColor.AQUA+"4");
		addTimeMessage(3, ChatColor.AQUA+"3");
		addTimeMessage(2, ChatColor.AQUA+"2");
		addTimeMessage(1, ChatColor.AQUA+"1");
	}
	
	@Override
	public PvpTimer clone(){
		PvpTimer result = new PvpTimer(getTimeElapsed(), getTimeLimit(), getTimeMessages());
		return result;
	}
}