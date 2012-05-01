package com.mtaye.ResourceMadness.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mtaye.ResourceMadness.RM;

public class SettingLibrary implements Cloneable {
	Map<Setting, SettingPrototype> map = new HashMap<Setting, SettingPrototype>();
	
	public SettingLibrary(){
		initSettings();
	}
	
	public SettingPrototype SettingLibrary(Setting setting){
		return get(setting);
	}
	
	private void initSettings(){
		add(Setting.minplayers, 1, 1);
		add(Setting.maxplayers);
		add(Setting.minteamplayers, 1, 1);
		add(Setting.maxteamplayers);
		add(Setting.safezone);
		add(Setting.timelimit);
		add(Setting.random);
		add(Setting.password, "");
		add(Setting.advertise, true);
		add(Setting.restore, true);
		add(Setting.warp, true);
		add(Setting.midgamejoin, true);
		add(Setting.healplayer, true);
		add(Setting.clearinventory, true);
		add(Setting.foundasreward, false);
		add(Setting.warnunequal, true);
		add(Setting.allowunequal, false);
		add(Setting.warnhacked, true);
		add(Setting.allowhacked, false);
		add(Setting.infinitereward, false);
		add(Setting.infinitetools, false);
	}
	
	private void add(Setting setting, int... value){
		map.put(setting, new SettingInt(setting, value));
	}
	
	private void add(Setting setting, boolean bool){
		map.put(setting, new SettingBool(setting, bool));
	}
	
	private void add(Setting setting, String str){
		map.put(setting, new SettingStr(setting, str));
	}
	
	public void set(Setting setting, int value){
		SettingInt s = (SettingInt)get(setting);
		if(s!=null) s.set(value);
	}
	
	public void set(Setting setting, boolean bool){
		SettingBool s = (SettingBool)get(setting);
		if(s!=null) s.set(bool);
	}
	
	public void set(Setting setting, String str){
		SettingStr s = (SettingStr)get(setting);
		if(s!=null) s.set(str);
	}
	
	public void set(Setting setting, int value, boolean lock){
		SettingInt s = (SettingInt)get(setting);
		if(s!=null) s.set(value, lock);
	}
	
	public void set(Setting setting, boolean bool, boolean lock){
		SettingBool s = (SettingBool)get(setting);
		if(s!=null) s.set(bool, lock);
	}
	
	public void set(Setting setting, String str, boolean lock){
		SettingStr s = (SettingStr)get(setting);
		if(s!=null) s.set(str, lock);
	}
	
	public void toggle(Setting setting){
		SettingPrototype s = get(setting);
		if(s!=null){
			if(s instanceof SettingBool) ((SettingBool) s).toggle();
		}
	}
	
	public void clear(Setting setting){
		SettingPrototype s = get(setting);
		if(s!=null){
			if(s instanceof SettingInt) ((SettingInt) s).clear();
			else if(s instanceof SettingBool) ((SettingBool) s).clear();
			else if(s instanceof SettingStr) ((SettingStr) s).clear();
		}
	}
	
	public SettingPrototype get(Setting setting){
		if(map.containsKey(setting)) return map.get(setting);
		return null;
	}
	
	public SettingInt getSettingInt(Setting setting){
		if(map.containsKey(setting)){
			if(map.get(setting) instanceof SettingInt){
				return (SettingInt)map.get(setting);
			}
		}
		return null;
	}
	
	public SettingBool getSettingBool(Setting setting){
		if(map.containsKey(setting)){
			if(map.get(setting) instanceof SettingBool){
				return (SettingBool)map.get(setting);
			}
		}
		return null;
	}
	
	public SettingStr getSettingStr(Setting setting){
		if(map.containsKey(setting)){
			if(map.get(setting) instanceof SettingStr){
				return (SettingStr)map.get(setting);
			}
		}
		return null;
	}
	
	public int getInt(Setting setting){
		return getSettingInt(setting).get();
	}
	
	public boolean getBool(Setting setting){
		return getSettingBool(setting).get();
	}
	
	public String getStr(Setting setting){
		return getSettingStr(setting).get();
	}
	
	private void addLock(Setting setting){
		if(map.containsKey(setting)) map.get(setting).addLock();
	}
	
	private void removeLock(Setting setting){
		if(map.containsKey(setting)) map.get(setting).removeLock();
	}
	
	private void toggleLock(Setting setting){
		if(map.containsKey(setting)) map.get(setting).toggleLock();
	}
	
	private boolean isLocked(Setting setting){
		if(map.containsKey(setting)) map.get(setting).isLocked();
		return false;
	}

	public int size(){
		return map.size();
	}
	
	public Set<Setting> keySet(){
		return map.keySet();
	}
	
	public Collection<SettingPrototype> varues(){
		Collection<SettingPrototype> list = new ArrayList<SettingPrototype>();
		for(Setting s : Setting.values()){
			list.add(get(s));
		}
		return list;
	}
	
	public SettingPrototype[] toArray(){
		return map.values().toArray(new SettingPrototype[map.size()]);
	}
	
	@Override
	public SettingLibrary clone() {
		SettingLibrary result = null;
		try{
			result = (SettingLibrary)super.clone();
		}
		catch(Exception e){}
	    return result;
	}
}
