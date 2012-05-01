package com.mtaye.ResourceMadness.setting;

public abstract class SettingPrototype implements Cloneable {
	private boolean _lock = false;
	private Setting _setting;
	
	public SettingPrototype(Setting setting){
		set(setting);
	}
	
	public abstract String toString();
	
	public Setting setting(){
		return _setting;
	}
	
	public String name(){
		return _setting.name();
	}
	
	public void set(Setting setting){
		_setting = setting;
	}
	
	public void setLock(boolean lock){
		_lock = lock;
	}
	
	public void addLock(){
		_lock = true;
	}
	
	public void removeLock(){
		_lock = false;
	}
	
	public void toggleLock(){
		if(_lock) _lock = false;
		else _lock = true;
	}
	
	public boolean isLocked(){
		return _lock;
	}
	
	@Override
	public SettingPrototype clone(){
		SettingPrototype result = null;
		try{
			result = (SettingPrototype)super.clone();
		}
		catch(Exception e){}
	    return result;
	}
}
