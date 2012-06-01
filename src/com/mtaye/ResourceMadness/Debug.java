package com.mtaye.ResourceMadness;

import java.util.logging.Level;

public final class Debug {
	public static RM rm;
	private static boolean _enabled = false;
	public Debug(){
	}
	
	public static void enable(){
		_enabled = true;
		warning(Text.preLog+"Debug messages are enabled!");
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
		if(_enabled) rm.log.log(level, Text.preLog+message);
	}
	public static void warning(String message){
		if(_enabled) rm.log.log(Level.WARNING, Text.preLog+message);
	}
	public static void severe(String message){
		if(_enabled) rm.log.log(Level.SEVERE, Text.preLog+message);
	}
	
}
