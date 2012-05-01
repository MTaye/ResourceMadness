package com.mtaye.ResourceMadness.setting;

import com.mtaye.ResourceMadness.RMDebug;

public class SettingInt extends SettingPrototype{
	private int _value = 0;
	private Integer _minvalue;
	private Integer _maxvalue;
	
	public SettingInt(Setting setting, int... value){
		super(setting);
		if(value.length!=0){
			if(value.length>1){
				_minvalue = value[1];
				if(value.length>2) _minvalue = value[2];
			}
			set(value[0]);
		}
	}
	
	public void set(int value){
		_value = value;
		if(_minvalue!=null) if(_value<_minvalue) _value = _minvalue;
		if(_maxvalue!=null) if(_value>_maxvalue) _value = _maxvalue;
	}
	
	public void set(int value, boolean lock){
		RMDebug.warning("value:"+value);
		set(value);
		setLock(lock);
	}
	
	public int get(){
		return _value;
	}
	
	public void clear(){
		_value = 0;
	}
	
	public String toString(){
		return Integer.toString(_value);
	}
	
	@Override
	public SettingInt clone(){
		SettingInt result = null;
		try{
			result = (SettingInt)super.clone();
		}
		catch(Exception e){}
	    return result;
	}
}
