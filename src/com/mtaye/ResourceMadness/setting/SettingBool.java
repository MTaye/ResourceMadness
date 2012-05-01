package com.mtaye.ResourceMadness.setting;

public class SettingBool extends SettingPrototype{
	private boolean _bool = true;
	
	public SettingBool(Setting setting){
		this(setting, true);
	}
	
	public SettingBool(Setting setting, boolean bool){
		super(setting);
		set(bool);
	}
	
	public void set(boolean bool){
		_bool = bool;
	}
	
	public void set(boolean bool, boolean lock){
		set(bool);
		setLock(lock);
	}
	
	public boolean get(){
		return _bool;
	}
	
	public void toggle(){
		if(_bool) _bool = false;
		else _bool = true;
	}
	
	public void clear(){
		_bool = true;
	}
	
	public String toString(){
		return Boolean.toString(_bool);
	}
	
	@Override
	public SettingBool clone(){
		SettingBool result = null;
		try{
			result = (SettingBool)super.clone();
		}
		catch(Exception e){}
	    return result;
	}
}
