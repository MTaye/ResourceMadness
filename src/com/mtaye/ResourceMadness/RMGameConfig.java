package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.RMGame.MinMaxType;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMGameConfig {
	private RM plugin;

	private RMPartList _partList = new RMPartList();
	private String _worldName;
	private int _id;
	//private RMPlayer _owner;
	private String _ownerName;
	private int _minPlayers = 0;
	private int _maxPlayers = 0;
	private int _minTeamPlayers = 0;
	private int _maxTeamPlayers = 0;
	private int _autoRandomizeAmount = 0;
	private boolean _advertise = true;
	private boolean _autoRestoreWorld = true;
	private boolean _warpToSafety = true;
	private boolean _allowMidgameJoin = true;
	private boolean _healPlayer = false;
	private boolean _clearPlayerInventory = true;
	private boolean _warnUnequal = true;
	private boolean _allowUnequal = true;
	private boolean _warnHackedItems = true;
	private boolean _allowHackedItems = false;
	private boolean _infiniteReward = false;
	private boolean _infiniteTools = false;
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	//private List<RMTeam> _teams = new ArrayList<RMTeam>();
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
	private int _maxItems = 0;
	private int _randomizeAmount = 0;
	
	private RMStats _stats = new RMStats();
	private RMGameTimer _timer = new RMGameTimer();
	private int _moneyReward = 0;
	private int _moneyJoin = 0;
	private int _moneyQuit = 0;
	
	public RMGameConfig(RM plugin){
		this.plugin = plugin;
		_log = new RMLog(plugin);
	}
	
	public RMGameConfig(RMGameConfig config, RM plugin){
		this.plugin = plugin;
		this._partList = config._partList;
		this._id = config._id;
		//this._owner = rmGameConfig._owner;
		this._ownerName = config._ownerName;
		this._minPlayers = config._minPlayers;
		this._maxPlayers = config._maxPlayers;
		this._maxTeamPlayers = config._maxTeamPlayers;
		this._minTeamPlayers = config._minTeamPlayers;
		this._autoRandomizeAmount = config._autoRandomizeAmount;
		this._advertise = config._advertise;
		this._autoRestoreWorld = config._autoRestoreWorld;
		this._warpToSafety = config._warpToSafety;
		this._allowMidgameJoin = config._allowMidgameJoin;
		this._healPlayer = config._healPlayer;
		this._clearPlayerInventory = config._clearPlayerInventory;
		this._warnUnequal = config._warnUnequal;
		this._allowUnequal = config._allowUnequal;
		this._warnHackedItems = config._warnHackedItems;
		this._allowHackedItems = config._allowHackedItems;
		this._infiniteReward = config._infiniteReward;
		this._infiniteTools = config._infiniteTools;
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
	}
	
	//Get
	public RMPartList getPartList() { return _partList; }
	public String getWorldName() { return _worldName; }
	public int getId() { return _id; }
	public String getOwnerName() { return _ownerName; }
	public RMPlayer getOwner() { return RMPlayer.getPlayerByName(getOwnerName()); }
	public int getMinPlayers() { return _minPlayers; }
	public int getMaxPlayers() { return _maxPlayers; }
	public int getMinTeamPlayers() { return _minTeamPlayers; }
	public int getMaxTeamPlayers() { return _maxTeamPlayers; }
	public int getAutoRandomizeAmount() { return _autoRandomizeAmount; }
	public boolean getAdvertise() { return _advertise; }
	public boolean getAutoRestoreWorld() { return _autoRestoreWorld; }
	public boolean getWarpToSafety() { return _warpToSafety; }
	public boolean getAllowMidgameJoin() { return _allowMidgameJoin; }
	public boolean getHealPlayer() { return _healPlayer; }
	public boolean getClearPlayerInventory() { return _clearPlayerInventory; }
	public boolean getWarnUnequal() { return _warnUnequal; };
	public boolean getAllowUnequal() { return _allowUnequal; };
	public boolean getWarnHackedItems() { return _warnHackedItems; }
	public boolean getAllowHackedItems() { return _allowHackedItems; }
	public boolean getInfiniteReward() { return _infiniteReward; }
	public boolean getInfiniteTools() { return _infiniteTools; }
	public HashMap<String, RMPlayer> getPlayers() { return _players; }
	//public List<RMTeam> getTeams() { return _teams; }
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
	public int getMaxItems() { return _maxItems; }
	public int getRandomizeAmount() { return _randomizeAmount; }
	
	public RMStats getStats() { return _stats; }
	public RMGameTimer getTimer() { return _timer; }
	
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
	/*
	public void setOwnerByName(String ownerName){
		_ownerName = ownerName;
	}
	*/
	public void setOwnerName(String ownerName){
		_ownerName = ownerName;
	}
	public void setMinPlayers(int minPlayers){
		_minPlayers = minPlayers;
		if(_minPlayers<1) _minPlayers = 1;
	}
	public void setMaxPlayers(int maxPlayers){
		_maxPlayers = maxPlayers;
		if(_maxPlayers<0) _maxPlayers = 0;
	}
	public void setMinTeamPlayers(int minTeamPlayers){
		_minTeamPlayers = minTeamPlayers;
		if(_minTeamPlayers<1) _minTeamPlayers = 1;
	}
	public void setMaxTeamPlayers(int maxTeamPlayers){
		_maxTeamPlayers = maxTeamPlayers;
		if(_maxTeamPlayers<0) _maxTeamPlayers = 0;
	}
	public void setMaxItems(int maxItems){
		_maxItems = maxItems;
		if(_maxItems<0) _maxItems = 0;
	}
	public void setRandomizeAmount(int amount){
		_randomizeAmount = amount;
		if(_randomizeAmount<0) _randomizeAmount = 0;
	}
	public void setAutoRandomizeAmount(int amount){
		_autoRandomizeAmount = amount;
		if(_autoRandomizeAmount<0) _autoRandomizeAmount = 0;
	}
	public void setAdvertise(boolean advertise){
		_advertise = advertise;
	}
	public void setAutoRestoreWorld(boolean restore){
		_autoRestoreWorld = restore;
	}
	public void setWarpToSafety(boolean warp){
		_warpToSafety = warp;
	}
	public void setAllowMidgameJoin(boolean allow){
		_allowMidgameJoin = allow;
	}
	public void setHealPlayer(boolean restore){
		_healPlayer = restore;
	}
	public void setClearPlayerInventory(boolean clear){
		_clearPlayerInventory = clear;
	}
	public void setWarnUnequal(boolean warn){
		_warnUnequal = warn;
	}
	public void setAllowUnequal(boolean allow){
		_allowUnequal = allow;
	}
	public void setWarnHackedItems(boolean warn){
		_warnHackedItems = warn;
	}
	public void setAllowHackedItems(boolean allow){
		_allowHackedItems = allow;
	}
	public void setInfiniteReward(boolean infiniteReward){
		_infiniteReward = infiniteReward;
	}
	public void setInfiniteTools(boolean infiniteTools){
		_infiniteTools = infiniteTools;
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
	
	//Clear
	public void clearRandomizeAmount(){
		_randomizeAmount = 0;
	}
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
	
	//Toggle
	public void toggleAdvertise(){
		if(_advertise) _advertise = false;
		else _advertise = true;
	}
	public void toggleAutoRestoreWorld(){
		if(_autoRestoreWorld) _autoRestoreWorld = false;
		else _autoRestoreWorld = true;
	}
	public void toggleWarpToSafety(){
		if(_warpToSafety) _warpToSafety = false;
		else _warpToSafety = true;
	}
	public void toggleAllowMidgameJoin(){
		if(_allowMidgameJoin) _allowMidgameJoin = false;
		else _allowMidgameJoin = true;
	}
	public void toggleHealPlayer(){
		if(_healPlayer) _healPlayer = false;
		else _healPlayer = true;
	}
	public void toggleClearPlayerInventory(){
		if(_clearPlayerInventory) _clearPlayerInventory = false;
		else _clearPlayerInventory = true;
	}
	public void toggleWarnUnequal(){
		if(_warnUnequal) _warnUnequal = false;
		else _warnUnequal = true;
	}
	public void toggleAllowUnequal(){
		if(_allowUnequal) _allowUnequal = false;
		else _allowUnequal = true;
	}
	public void toggleWarnHackedItems(){
		if(_warnHackedItems) _warnHackedItems = false;
		else _warnHackedItems = true;
	}
	public void toggleAllowHackedItems(){
		if(_allowHackedItems) _allowHackedItems = false;
		else _allowHackedItems = true;
	}
	public void toggleInfiniteReward(){
		if(_infiniteReward) _infiniteReward = false;
		else _infiniteReward = true;
	}
	public void toggleInfiniteTools(){
		if(_infiniteTools) _infiniteTools = false;
		else _infiniteTools = true;
	}
	public void togglePaused(){
		if(_paused) _paused = false;
		else _paused = true;
	}
	
	//getDataFrom
	public void getDataFrom(RMGameConfig config){
		setOwnerName(config.getOwnerName());
		setMinPlayers(config.getMinPlayers());
		setMaxPlayers(config.getMaxPlayers());
		setMinTeamPlayers(config.getMinTeamPlayers());
		setMaxTeamPlayers(config.getMaxTeamPlayers());
		setAutoRandomizeAmount(config.getAutoRandomizeAmount());
		setAutoRestoreWorld(config.getAutoRestoreWorld());
		setWarpToSafety(config.getWarpToSafety());
		setAllowMidgameJoin(config.getAllowMidgameJoin());
		setHealPlayer(config.getHealPlayer());
		setClearPlayerInventory(config.getClearPlayerInventory());
		setWarnUnequal(config.getWarnUnequal());
		setAllowUnequal(config.getAllowUnequal());
		setWarnHackedItems(config.getWarnHackedItems());
		setAllowHackedItems(config.getAllowHackedItems());
		setInfiniteReward(config.getInfiniteReward());
		setInfiniteTools(config.getInfiniteTools());
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
	}
	
	public void getDataFrom(RMConfig config){
		setMinPlayers(config.getMinPlayers());
		setMaxPlayers(config.getMaxPlayers());
		setMinTeamPlayers(config.getMinTeamPlayers());
		setMaxTeamPlayers(config.getMaxTeamPlayers());
		_timer.setTimeLimit(config.getTimeLimit());
		setAutoRandomizeAmount(config.getAutoRandomizeAmount());
		setAutoRestoreWorld(config.getAutoRestoreWorld());
		setWarpToSafety(config.getWarpToSafety());
		setAllowMidgameJoin(config.getAllowMidgameJoin());
		setHealPlayer(config.getHealPlayer());
		setClearPlayerInventory(config.getClearPlayerInventory());
		setWarnUnequal(config.getWarnUnequal());
		setAllowUnequal(config.getAllowUnequal());
		setWarnHackedItems(config.getWarnHackedItems());
		setAllowHackedItems(config.getAllowHackedItems());
		setInfiniteReward(config.getInfiniteReward());
		setInfiniteTools(config.getInfiniteTools());
	}
	
	public void correctMinMaxNumbers(MinMaxType correct){
		int size = getTeams().size();
		int min = getMinPlayers();
		int max = getMaxPlayers();
		int minTeam = getMinTeamPlayers();
		int maxTeam = getMaxTeamPlayers();
		switch(correct){
		case MIN_PLAYERS:
			if(min<size) min = size;
			if(max!=0) if(min>max) max = min;
			if(min<minTeam*size) minTeam = (int)((double)min/(double)size);
			break;
		case MAX_PLAYERS:
			if(max!=0){
				if(max<size) max = size;
				if(max<min) min = max;
				if(min<minTeam*size) minTeam = (int)((double)min/(double)size);
			}
			break;
		case MIN_TEAM_PLAYERS:
			if(minTeam<1){
				minTeam = 1;
			}
			if(maxTeam!=0) if(minTeam>maxTeam) maxTeam = minTeam;
			if(minTeam*size>min) min = minTeam*size;
			if(max!=0) if(max<min) max = min;
			break;
		case MAX_TEAM_PLAYERS:
			if(maxTeam!=0){
				if(maxTeam<1) maxTeam = 1;
				if(maxTeam<minTeam) minTeam = maxTeam;
			}
			break;
		}
		setMinPlayers(min);
		setMaxPlayers(max);
		setMinTeamPlayers(minTeam);
		setMaxTeamPlayers(maxTeam);
	}
}