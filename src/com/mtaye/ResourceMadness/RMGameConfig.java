package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.setting.SettingLibrary;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMGameConfig {
	private RMPartList _partList = new RMPartList();
	private String _worldName;
	private int _id;
	private String _ownerName;
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	private RMFilter _filter = new RMFilter();
	private RMFilter _items = new RMFilter();
	private RMStash _found = new RMStash();
	private RMStash _reward = new RMStash();
	private RMStash _tools = new RMStash();
	private List<RMTeam> _teams = new ArrayList<RMTeam>();
	private RMLog _log;
	private boolean _paused = false;
	private GameState _state = GameState.SETUP;
	private InterfaceState _interface = InterfaceState.FILTER;
	
	private boolean _addWholeStack = false;
	private boolean _addOnlyOneStack = false;
	private int _randomizeAmount = 0;
	
	private RMStats _stats = new RMStats();
	private RMGameTimer _timer = new RMGameTimer();
	
	private RMBanList _banList = new RMBanList();
	private RMMoney _money;
	private RMCost _cost;
	private RMCost _payment;
	
	private SettingLibrary _settingLibrary = new SettingLibrary();
	
	public void setSettingLibrary(SettingLibrary settingLibrary) { _settingLibrary = settingLibrary; }
	public SettingLibrary getSettingLibrary() { return _settingLibrary; }
	
	public void setSetting(Setting setting, int value){
		if(setting==Setting.timelimit) _timer.setTimeLimit(value);
		_settingLibrary.set(setting, value);
	}
	
	public void setSetting(Setting setting, boolean bool){
		_settingLibrary.set(setting, bool);
	}
	
	public void setSetting(Setting setting, String str){
		_settingLibrary.set(setting, str);
	}
	
	public void setSetting(Setting setting, int value, boolean lock){
		if(setting==Setting.timelimit) _timer.setTimeLimit(value);
		_settingLibrary.set(setting, value, lock);
	}
	
	public void setSetting(Setting setting, boolean bool, boolean lock){
		_settingLibrary.set(setting, bool, lock);
	}
	
	public void setSetting(Setting setting, String str, boolean lock){
		_settingLibrary.set(setting, str, lock);
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
	
	public RMGameConfig(){
		_log = new RMLog();
	}
	
	public RMGameConfig(RMConfig config){
		_log = new RMLog();
		getDataFrom(config);
	}
	
	public RMGameConfig(RMGameConfig config, RM plugin){
		//this.plugin = plugin;
		this._partList = config._partList;
		this._id = config._id;
		this._ownerName = config._ownerName;
		this._settingLibrary = config._settingLibrary;
		
		this._players = config._players;
		this._filter = config._filter;
		this._items = config._items;
		this._found = config._found;
		this._reward = config._reward;
		this._tools = config._tools;
		this._teams = config._teams;
		this._log = config._log;
		this._paused = config._paused;
		this._state = config._state;
		this._interface = config._interface;
		this._stats = config._stats;
		this._timer = config._timer;
		
		this._banList = config._banList;
		this._money = config._money;
		this._cost = config._cost;
		this._payment = config._payment;
	}
	
	//Get
	public RMPartList getPartList() { return _partList; }
	public String getWorldName() { return _worldName; }
	public int getId() { return _id; }
	public String getOwnerName() { return _ownerName; }
	public RMPlayer getOwner() { return RMPlayer.getPlayerByName(getOwnerName()); }
	
	public HashMap<String, RMPlayer> getPlayers() { return _players; }
	public RMFilter getFilter() { return _filter; }
	public RMFilter getItems() { return _items; }
	public RMStash getFound() { return _found; }
	public RMStash getReward() { return _reward; }
	public RMStash getTools() { return _tools; }
	public ItemStack[] getFoundArray() { return _found.getItemsArray(); }
	public ItemStack[] getRewardArray() { return _reward.getItemsArray(); }
	public ItemStack[] getToolsArray() { return _tools.getItemsArray(); }
	public List<RMTeam> getTeams() { return _teams; }
	public RMLog getLog() { return _log; }
	public boolean getPaused() { return _paused; }
	public GameState getState() { return _state; }
	public InterfaceState getInterface() { return _interface; }
	public boolean getAddWholeStack() { return _addWholeStack; }
	public boolean getAddOnlyOneStack() { return _addOnlyOneStack; }
	public int getRandomizeAmount() { return _randomizeAmount; }
	
	public RMStats getStats() { return _stats; }
	public RMGameTimer getTimer() { return _timer; }
	
	public RMBanList getBanList() { return _banList; }
	public RMMoney getMoney() { return _money; }
	public RMCost getCost() { return _cost; }
	public RMCost getPayment() { return _payment; }
	
	//Set
	public void setPartList(RMPartList partList){
		if(partList==null) return;
		_partList = partList;
		Block mainBlock = partList.getMainBlock();
		if(mainBlock==null) return;
		_worldName = partList.getMainBlock().getWorld().getName();
	}
	public void setId(int id){
		_id = id;
	}
	public void setOwner(RMPlayer owner){
		setOwnerName(owner.getName());
	}
	public void setOwnerName(String ownerName){
		_ownerName = ownerName;
	}

	public void setPlayers(HashMap<String, RMPlayer> players){
		_players = players;
	}
	public void setFilter(RMFilter filter){
		_filter = filter;
	}
	public void setItems(RMFilter items){
		_items = items;
	}
	public void setFound(RMStash found){
		_found = found;
		_found.clearChanged();
	}
	public void setReward(RMStash reward){
		_reward = reward;
		_reward.clearChanged();
	}
	public void setTools(RMStash tools){
		_tools = tools;
		_tools.clearChanged();
	}
	public void setTeams(List<RMTeam> teams){
		_teams = teams;
	}
	public void setLog(RMLog log){
		_log = log;
	}
	public void setPaused(boolean paused){
		_paused = paused;
	}
	public void setState(GameState state){
		_state = state;
	}
	public void setInterface(InterfaceState interfaceState){
		_interface = interfaceState;
	}
	public void setStats(RMStats stats){
		_stats = stats;
	}
	public void setTimer(RMGameTimer timer){
		_timer = timer;
	}
	
	public void setBanList(RMBanList banList){
		_banList = banList;
	}
	public void setMoney(RMMoney money){
		_money = money;
	}
	public void setCost(RMCost cost){
		_cost = cost;
	}
	public void setPayment(RMCost payment){
		_payment = payment;
	}
	public void setPassword(String password){
		if(password==null) return;
	}
	
	//Clear
	public void clearTeams(){
		_teams.clear();
	}
	public void clearReward(){
		_reward.clear();
	}
	public void clearTools(){
		_tools.clear();
	}
	public void clearFound(){
		_found.clear();
	}
	
	//Parse
	public boolean parseId(int id){
		if(id==-1) return false;
		setId(id);
		return true;
	}
	
	public void togglePaused(){
		if(_paused) _paused = false;
		else _paused = true;
	}
	
	//getDataFrom
	public void getDataFrom(RMGameConfig config){
		setOwnerName(config.getOwnerName());
		setSettingLibrary(config.getSettingLibrary().clone());
		
		setPlayers(config.getPlayers());
		setFilter(config.getFilter());
		setItems(config.getItems());
		setFound(config.getFound());
		setReward(config.getReward());
		setTools(config.getTools());
		setLog(config.getLog());
		setPaused(config.getPaused());
		setState(config.getState());
		setInterface(config.getInterface());
		setStats(config.getStats());
		setTimer(config.getTimer());
		
		setBanList(config.getBanList());
		setMoney(config.getMoney());
		setCost(config.getCost());
		setPayment(config.getPayment());
	}
	
	public void getDataFrom(RMConfig config){
		setSettingLibrary(config.getSettingLibrary().clone());
		_timer.setTimeLimit(config.getSettingInt(Setting.timelimit));
	}
	
	public void correctMinMaxNumbers(Setting setting){
		int size = getTeams().size();
		int min = _settingLibrary.getInt(Setting.minplayers);
		int max = _settingLibrary.getInt(Setting.maxplayers);
		int minTeam = _settingLibrary.getInt(Setting.minteamplayers);
		int maxTeam = _settingLibrary.getInt(Setting.maxteamplayers);
		switch(setting){
		case minplayers:
			if(min<size) min = size;
			if(max!=0) if(min>max) max = min;
			if(min<minTeam*size) minTeam = (int)((double)min/(double)size);
			break;
		case maxplayers:
			if(max!=0){
				if(max<size) max = size;
				if(max<min) min = max;
				if(min<minTeam*size) minTeam = (int)((double)min/(double)size);
			}
			break;
		case minteamplayers:
			if(minTeam<1){
				minTeam = 1;
			}
			if(maxTeam!=0) if(minTeam>maxTeam) maxTeam = minTeam;
			if(minTeam*size>min) min = minTeam*size;
			if(max!=0) if(max<min) max = min;
			break;
		case maxteamplayers:
			if(maxTeam!=0){
				if(maxTeam<1) maxTeam = 1;
				if(maxTeam<minTeam) minTeam = maxTeam;
			}
			break;
		}
		_settingLibrary.set(Setting.minplayers, min);
		_settingLibrary.set(Setting.maxplayers, max);
		_settingLibrary.set(Setting.minteamplayers, minTeam);
		_settingLibrary.set(Setting.maxteamplayers, maxTeam);
	}
}