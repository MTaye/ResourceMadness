package com.mtaye.ResourceMadness.setting;

public class SettingInt extends SettingPrototype{
	private int _value = 0;
	private int _minvalue = -1;
	private int _maxvalue = -1;
	
	public SettingInt(Setting setting, int... value){
		super(setting);
		if(value.length!=0){
			if(value.length>1){
				_minvalue = value[1];
				if(value.length>2) _maxvalue = value[2];
			}
			set(value[0]);
		}
	}
	
	public void set(int value){
		_value = value;
		if(_minvalue!=-1) if(_value<_minvalue) _value = _minvalue;
		if(_maxvalue!=-1) if(_value>_maxvalue) _value = _maxvalue;
	}
	
	public void set(int value, boolean lock){
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
		return new SettingInt(setting(), _value, _minvalue, _maxvalue);
	}
}
