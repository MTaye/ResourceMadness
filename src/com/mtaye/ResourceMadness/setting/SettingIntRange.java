package com.mtaye.ResourceMadness.setting;

import com.mtaye.ResourceMadness.IntRange;

public class SettingIntRange extends SettingPrototype{
	private IntRange intRange = new IntRange();
	private int minvalue = -1;
	private int maxvalue = -1;
	
	public SettingIntRange(Setting setting, IntRange intRange, int... value){
		super(setting);
		if(intRange==null) return;
		if(value.length>0){
			minvalue = value[0];
			if(value.length>1) maxvalue = value[1];
		}
		set(intRange);
	}
	
	public void set(IntRange intRange){
		this.intRange = intRange.clone();
		this.intRange.normalize();
		
		if(minvalue!=-1) if(this.intRange.getLow()<minvalue) this.intRange.setLow(minvalue);
		if(maxvalue!=-1) if(this.intRange.getLow()>maxvalue) this.intRange.setLow(maxvalue);

		if(maxvalue!=-1) if(this.intRange.getHigh()>maxvalue) this.intRange.setHigh(maxvalue);
		if(this.intRange.getHigh()<=intRange.getLow()) this.intRange.setHigh(-1);
	}
	
	public void set(IntRange intRange, boolean lock){
		set(intRange);
		setLock(lock);
	}
	
	public IntRange get(){
		return intRange;
	}
	
	public void clear(){
		intRange.clear();
	}
	
	public String toString(){
		String result = String.valueOf(intRange.getLow());
		if(intRange.hasHigh()) result += "-"+String.valueOf(intRange.getHigh());
		return result;
	}
	
	@Override
	public SettingIntRange clone(){
		return new SettingIntRange(setting(), intRange, minvalue, maxvalue);
	}
}
