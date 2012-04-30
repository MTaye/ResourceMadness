package com.mtaye.ResourceMadness;

import java.util.logging.Level;

public final class RMDebug {
	public static RM plugin;
	private static boolean _enabled = false;
	public RMDebug(){
	}
	
	public static void enable(){
		_enabled = true;
		warning(RMText.preLog+"Debug messages are enabled!");
	}
	public static void disable(){
		_enabled = false;
	}
	public static boolean isEnabled(){
		return _enabled;
	}
	public static void setEnabled(boolean enabled){
		_enabled = enabled;
	}
	
	public static void log(Level level, String message){
		if(_enabled) plugin.log.log(level, message);
	}
	public static void warning(String message){
		if(_enabled) plugin.log.log(Level.WARNING, message);
	}
	public static void severe(String message){
		if(_enabled) plugin.log.log(Level.SEVERE, message);
	}
	
}
