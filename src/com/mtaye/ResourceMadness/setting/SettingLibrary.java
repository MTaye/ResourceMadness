package com.mtaye.ResourceMadness.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mtaye.ResourceMadness.IntRange;
import com.mtaye.ResourceMadness.time.Timer;

public class SettingLibrary implements Cloneable {
	Map<Setting, SettingPrototype> map = new HashMap<Setting, SettingPrototype>();
	
	public SettingLibrary(){
		initSettings();
	}
	
	private void initSettings(){
		add(Setting.password, "");
		add(Setting.minplayers, 1, 1);
		add(Setting.maxplayers);
		add(Setting.minteamplayers, 1, 1);
		add(Setting.maxteamplayers);
		add(Setting.timelimit, 60*Timer.minuteInSeconds);
		add(Setting.safezone, 10);
		add(Setting.playarea, 100);
		add(Setting.playareatime, 60);
		add(Setting.enemyradar, 25);
		add(Setting.keepondeath, 50, 0, 100);
		add(Setting.multiplier, new IntRange(1), 1);
		add(Setting.random);
		add(Setting.advertise, false);
		add(Setting.restore, true);
		add(Setting.allowpvp, true);
		add(Setting.delaypvp, 5*Timer.minuteInSeconds);
		add(Setting.friendlyfire, false);
		add(Setting.healplayer, true);
		add(Setting.timeofday, -1, -1, 24000);
		add(Setting.autoreturn, true);
		add(Setting.midgamejoin, false);
		add(Setting.showitemsleft, true);
		add(Setting.clearinventory, true);
		add(Setting.scrapfound, true);
		add(Setting.foundasreward, false);
		add(Setting.keepoverflow, false);
		add(Setting.warnunequal, true);
		add(Setting.allowunequal, false);
		add(Setting.warnhacked, true);
		add(Setting.allowhacked, false);
		add(Setting.infinitereward, false);
		add(Setting.infinitetools, false);
		add(Setting.dividereward, true);
		add(Setting.dividetools, true);
	}
	
	@SuppressWarnings("unused")
	private void add(SettingPrototype setting){
		if(setting instanceof SettingInt) map.put(setting.setting(), (SettingInt)setting);
		else if(setting instanceof SettingBool) map.put(setting.setting(), (SettingBool)setting);
		else if(setting instanceof SettingStr) map.put(setting.setting(), (SettingStr)setting);
		else if(setting instanceof SettingIntRange) map.put(setting.setting(), (SettingIntRange)setting);
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
	
	private void add(Setting setting, IntRange range, int... value){
		map.put(setting, new SettingIntRange(setting, range, value));
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
	
	public void set(Setting setting, IntRange range){
		SettingIntRange s = (SettingIntRange)get(setting);
		if(s!=null) s.set(range);
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
	
	public void set(Setting setting, IntRange range, boolean lock){
		SettingIntRange s = (SettingIntRange)get(setting);
		if(s!=null) s.set(range, lock);
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
			else if(s instanceof SettingIntRange) ((SettingIntRange) s).clear();
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
	
	public SettingIntRange getSettingIntRange(Setting setting){
		if(map.containsKey(setting)){
			if(map.get(setting) instanceof SettingIntRange){
				return (SettingIntRange)map.get(setting);
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
	
	public IntRange getIntRange(Setting setting){
		return getSettingIntRange(setting).get();
	}
	
	@SuppressWarnings("unused")
	private void addLock(Setting setting){
		if(map.containsKey(setting)) map.get(setting).addLock();
	}
	
	@SuppressWarnings("unused")
	private void removeLock(Setting setting){
		if(map.containsKey(setting)) map.get(setting).removeLock();
	}
	
	@SuppressWarnings("unused")
	private void toggleLock(Setting setting){
		if(map.containsKey(setting)) map.get(setting).toggleLock();
	}
	
	@SuppressWarnings("unused")
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
	
	public Collection<SettingPrototype> values(){
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
		SettingLibrary result = new SettingLibrary();
		for(Setting setting : map.keySet()){
			SettingPrototype s = map.get(setting);
			if(s instanceof SettingInt) map.put(s.setting(), (SettingInt)s.clone());
			else if(s instanceof SettingBool) map.put(s.setting(), (SettingBool)s.clone());
			else if(s instanceof SettingStr) map.put(s.setting(), (SettingStr)s.clone());
			else if(s instanceof SettingIntRange) map.put(s.setting(), (SettingIntRange)s.clone());
		}
		return result;
	}
}
