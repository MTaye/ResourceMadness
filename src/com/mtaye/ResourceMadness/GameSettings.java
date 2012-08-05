package com.mtaye.ResourceMadness;

import java.util.Arrays;
import org.bukkit.inventory.ItemStack;
import com.mtaye.ResourceMadness.helper.InventoryHelper;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.setting.SettingLibrary;
import com.mtaye.ResourceMadness.time.PvpTimer;
import com.mtaye.ResourceMadness.time.Timer;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class GameSettings implements Cloneable{
	private Filter _filter = new Filter();
	private Stash _reward = new Stash();
	private Stash _tools = new Stash();
	private Timer _timer = new Timer();
	private PvpTimer _pvpTimer = new PvpTimer();
	private SettingLibrary _settingLibrary = new SettingLibrary();
	
	//Constructor
	public GameSettings(){
	}
	
	public GameSettings(Config config){
		getDataFrom(config);
	}
	
	public GameSettings(GameSettings config){
		this._settingLibrary = config._settingLibrary.clone();
		this._filter = config._filter.clone();
		this._reward = config._reward.clone();
		this._tools = config._tools.clone();
		this._timer = config._timer.clone();
		this._pvpTimer = config._pvpTimer.clone();
	}
	
	//Setting Library	
	public void setSettingLibrary(SettingLibrary settingLibrary) { _settingLibrary = settingLibrary; }
	public SettingLibrary getSettingLibrary() { return _settingLibrary; }
	
	public void setSetting(Setting setting, int value){
		switch(setting){
		case timelimit:{
			_timer.setTimeLimit(value);
			_timer.addDefaultTimeMessages();
			break;
		}
		case delaypvp:
			_pvpTimer.setTimeLimit(value);
			_pvpTimer.addDefaultTimeMessages();
			break;
		}
		_settingLibrary.set(setting, value);
	}
	
	public void setSetting(Setting setting, boolean bool){
		_settingLibrary.set(setting, bool);
	}
	
	public void setSetting(Setting setting, String str){
		_settingLibrary.set(setting, str);
	}
	
	public void setSetting(Setting setting, IntRange range){
		_settingLibrary.set(setting, range);
	}
	
	public void setSetting(Setting setting, int value, boolean lock){
		switch(setting){
		case timelimit: _timer.setTimeLimit(value); break;
		case delaypvp: _pvpTimer.setTimeLimit(value); break;
		}
		_settingLibrary.set(setting, value, lock);
	}
	
	public void setSetting(Setting setting, boolean bool, boolean lock){
		_settingLibrary.set(setting, bool, lock);
	}
	
	public void setSetting(Setting setting, String str, boolean lock){
		_settingLibrary.set(setting, str, lock);
	}
	
	public void setSetting(Setting setting, IntRange range, boolean lock){
		_settingLibrary.set(setting, range, lock);
	}
	
	public int getSettingInt(Setting setting){
		return _settingLibrary.getInt(setting);
	}
	
	public boolean getSettingBool(Setting setting){
		return _settingLibrary.getBool(setting);
	}
	
	public String getSettingStr(Setting setting){
		return _settingLibrary.getStr(setting);
	}
	
	public IntRange getSettingIntRange(Setting setting){
		return _settingLibrary.getIntRange(setting);
	}
	
	public void toggleSetting(Setting setting){
		_settingLibrary.toggle(setting);
	}
	
	public void clearSetting(Setting setting){
		_settingLibrary.clear(setting);
	}
	
	public void addLock(Setting setting){
		_settingLibrary.get(setting).addLock();
	}
	
	public void removeLock(Setting setting){
		_settingLibrary.get(setting).removeLock();
	}
	
	public boolean isLocked(Setting setting){
		return _settingLibrary.get(setting).isLocked();
	}
	
	//Get
	public Filter getFilter() { return _filter; }
	public Stash getReward() { return _reward; }
	public Stash getTools() { return _tools; }
	public ItemStack[] getRewardArray() { return _reward.getItemsArray(); }
	public ItemStack[] getToolsArray() { return _tools.getItemsArray(); }
	public Timer getTimer() { return _timer; }
	public PvpTimer getPvpTimer() { return _pvpTimer; }
	
	//Set
	public void setFilter(Filter filter){
		_filter = filter;
	}
	public void setReward(Stash reward){
		_reward = reward;
		_reward.clearChanged();
	}
	public void setTools(Stash tools){
		_tools = tools;
		_tools.clearChanged();
	}
	public void setTimer(Timer timer){
		_timer = timer;
	}
	public void setPvpTimer(PvpTimer timer){
		_pvpTimer = timer;
	}
	
	//Clear
	public void clearReward(){
		_reward.clear();
	}
	public void clearTools(){
		_tools.clear();
	}
	
	public String getEncodeToStringFilter() { return Filter.encodeFilterToString(_filter.getItems(), true); }
	public String getEncodeToStringReward() { return InventoryHelper.encodeInventoryToString(_reward.getItemsArray()); }
	public String getEncodeToStringTools() { return InventoryHelper.encodeInventoryToString(_tools.getItemsArray()); }
	
	public void setFilterParseString(String str){
		_filter = new Filter(Filter.getRMItemsByStringArray(Arrays.asList(str), true));
	}
	public void setRewardParseString(String str){
		_reward = new Stash(InventoryHelper.getItemStackByString(str));
	}
	public void setToolsParseString(String str){
		_tools = new Stash(InventoryHelper.getItemStackByString(str));
	}
	
	//getDataFrom
	public void getDataFrom(GameSettings config){
		setSettingLibrary(config.getSettingLibrary().clone());
		setFilter(config.getFilter().clone());
		setReward(config.getReward().clone());
		setTools(config.getTools().clone());
		setTimer(config.getTimer().clone());
		setPvpTimer(config.getPvpTimer().clone());
	}
	
	public void getDataFrom(Config config){
		setSettingLibrary(config.getSettingLibrary().clone());
		_timer.setTimeLimit(config.getSettingInt(Setting.timelimit));
		_pvpTimer.setTimeLimit(config.getSettingInt(Setting.delaypvp));
	}
	
	public GameSettings clone(){
		GameSettings result = new GameSettings(this);
		return result;
	}
}