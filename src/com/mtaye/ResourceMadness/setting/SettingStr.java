package com.mtaye.ResourceMadness.setting;

public class SettingStr extends SettingPrototype{
	private String _str = "";
	
	public SettingStr(Setting setting){
		this(setting, null);
	}
	
	public SettingStr(Setting setting, String str){
		super(setting);
		set(str);
	}
	
	public void set(String str){
		if(str==null) return;
		_str = str;
	}
	
	public void set(String str, boolean lock){
		set(str);
		setLock(lock);
	}
	
	public String get(){
		return _str;
	}
	
	public void clear(){
		_str = "";
	}
	
	public String toString(){
		return _str;
	}
	
	@Override
	public SettingStr clone(){
		SettingStr result = null;
		try{
			result = (SettingStr)super.clone();
		}
		catch(Exception e){}
	    return result;
	}
}
