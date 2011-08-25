package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMConfig.Lock;
import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMGameConfig {
	public static RM plugin;

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
	private boolean _warpToSafety = true;
	private boolean _autoRestoreWorld = true;
	private boolean _warnHackedItems = true;
	private boolean _allowHackedItems = false;
	private boolean _keepIngame = true;
	private boolean _allowMidgameJoin = false;
	private boolean _clearPlayerInventory = true;
	private boolean _warnUnequal = true;
	private boolean _allowUnequal = false;
	private boolean _infiniteAward = false;
	private boolean _infiniteTools = false;
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	//private List<RMTeam> _teams = new ArrayList<RMTeam>();
	private RMFilter _filter = new RMFilter();
	private RMFilter _items = new RMFilter();
	private List<ItemStack> _award = new ArrayList<ItemStack>();
	private List<ItemStack> _tools = new ArrayList<ItemStack>();
	private List<RMTeam> _teams = new ArrayList<RMTeam>();
	private RMLog _log = new RMLog(plugin);
	private GameState _state = GameState.SETUP;
	private InterfaceState _interface = InterfaceState.FILTER;
	private int _wordLimit = 40;
	
	private boolean _addWholeStack = false;
	private boolean _addOnlyOneStack = false;
	private int _maxItems = 0;
	private int _randomizeAmount = 0;
	
	private RMStats _gameStats = new RMStats();
	
	public RMGameConfig(){
	}
	
	public RMGameConfig(RMGameConfig rmGameConfig){
		this._partList = rmGameConfig._partList;
		this._id = rmGameConfig._id;
		//this._owner = rmGameConfig._owner;
		this._ownerName = rmGameConfig._ownerName;
		this._minPlayers = rmGameConfig._minPlayers;
		this._maxPlayers = rmGameConfig._maxPlayers;
		this._maxTeamPlayers = rmGameConfig._maxTeamPlayers;
		this._minTeamPlayers = rmGameConfig._minTeamPlayers;
		this._autoRandomizeAmount = rmGameConfig._autoRandomizeAmount;
		this._warpToSafety = rmGameConfig._warpToSafety;
		this._autoRestoreWorld = rmGameConfig._autoRestoreWorld;
		this._warnHackedItems = rmGameConfig._warnHackedItems;
		this._allowHackedItems = rmGameConfig._allowHackedItems;
		this._keepIngame = rmGameConfig._keepIngame;
		this._allowMidgameJoin = rmGameConfig._allowMidgameJoin;
		this._clearPlayerInventory = rmGameConfig._clearPlayerInventory;
		this._warnUnequal = rmGameConfig._warnUnequal;
		this._allowUnequal = rmGameConfig._allowUnequal;
		this._infiniteAward = rmGameConfig._infiniteAward;
		this._infiniteTools = rmGameConfig._infiniteTools;
		this._players = rmGameConfig._players;
		this._filter = rmGameConfig._filter;
		this._items = rmGameConfig._items;
		this._award = rmGameConfig._award;
		this._tools = rmGameConfig._tools;
		this._teams = rmGameConfig._teams;
		this._log = rmGameConfig._log;
		this._state = rmGameConfig._state;
		this._interface = rmGameConfig._interface;
		this._wordLimit = rmGameConfig._wordLimit;
		this._gameStats = rmGameConfig._gameStats;
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
	public boolean getWarpToSafety() { return _warpToSafety; }
	public boolean getAutoRestoreWorld() { return _autoRestoreWorld; }
	public boolean getWarnHackedItems() { return _warnHackedItems; }
	public boolean getAllowHackedItems() { return _allowHackedItems; }
	public boolean getKeepIngame() { return _keepIngame; }
	public boolean getAllowMidgameJoin() { return _allowMidgameJoin; }
	public boolean getClearPlayerInventory() { return _clearPlayerInventory; }
	public boolean getWarnUnequal() { return _warnUnequal; };
	public boolean getAllowUnequal() { return _allowUnequal; };
	public boolean getInfiniteAward() { return _infiniteAward; }
	public boolean getInfiniteTools() { return _infiniteTools; }
	public HashMap<String, RMPlayer> getPlayers() { return _players; }
	//public List<RMTeam> getTeams() { return _teams; }
	public RMFilter getFilter() { return _filter; }
	public RMFilter getItems() { return _items; }
	public List<ItemStack> getAward() { return _award; }
	public List<ItemStack> getTools() { return _tools; }
	public ItemStack[] getAwardArray() { return _award.toArray(new ItemStack[_award.size()]); }
	public ItemStack[] getToolsArray() { return _tools.toArray(new ItemStack[_tools.size()]); }
	public List<RMTeam> getTeams() { return _teams; }
	public RMLog getLog() { return _log; }
	public GameState getState() { return _state; }
	public InterfaceState getInterface() { return _interface; }
	
	public int getWordLimit() { return _wordLimit; }
	public boolean getAddWholeStack() { return _addWholeStack; }
	public boolean getAddOnlyOneStack() { return _addOnlyOneStack; }
	public int getMaxItems() { return _maxItems; }
	public int getRandomizeAmount() { return _randomizeAmount; }
	
	public RMStats getGameStats() { return _gameStats; }
	
	//Set
	public void setPartList(RMPartList partList){
		_partList = partList;
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
		if(_minPlayers<0) _minPlayers = 0;
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
	public void setWarpToSafety(boolean warp){
		_warpToSafety = warp;
	}
	public void setAutoRestoreWorld(boolean restore){
		_autoRestoreWorld = restore;
	}
	public void setWarnHackedItems(boolean warn){
		_warnHackedItems = warn;
	}
	public void setAllowHackedItems(boolean allow){
		_allowHackedItems = allow;
	}
	public void setKeepIngame(boolean keep){
		_keepIngame = keep;
	}
	public void setAllowMidgameJoin(boolean allow){
		_allowMidgameJoin = allow;
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
	public void setInfiniteAward(boolean infiniteAward){
		_infiniteAward = infiniteAward;
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
	public void setAward(List<ItemStack> award){
		_award = award;
	}
	public void setTools(List<ItemStack> tools){
		_tools = tools;
	}
	public void setTeams(List<RMTeam> teams){
		_teams = teams;
	}
	public void setLog(RMLog log){
		_log = log;
	}
	public void setState(GameState state){
		_state = state;
	}
	public void setInterface(InterfaceState interfaceState){
		_interface = interfaceState;
	}
	public void setWordLimit(int limit){
		_wordLimit = limit;
		if(_wordLimit<10) _wordLimit = 10;
	}
	public void setGameStats(RMStats stats){
		_gameStats = stats;
	}
	
	//Clear
	public void clearRandomizeAmount(){
		_randomizeAmount = 0;
	}
	public void clearTeams(){
		_teams.clear();
	}
	public void clearAward(){
		_award.clear();
	}
	public void clearTools(){
		_tools.clear();
	}
	
	//Parse
	public boolean parseId(int id){
		if(id==-1) return false;
		setId(id);
		return true;
	}
	
	//Toggle
	public void toggleWarpToSafety(){
		if(_warpToSafety) _warpToSafety = false;
		else _warpToSafety = true;
	}
	public void toggleAutoRestoreWorld(){
		if(_autoRestoreWorld) _autoRestoreWorld = false;
		else _autoRestoreWorld = true;
	}
	public void toggleWarnHackedItems(){
		if(_warnHackedItems) _warnHackedItems = false;
		else _warnHackedItems = true;
	}
	public void toggleAllowHackedItems(){
		if(_allowHackedItems) _allowHackedItems = false;
		else _allowHackedItems = true;
	}
	public void toggleKeepIngame(){
		if(_keepIngame) _keepIngame = false;
		else _keepIngame = true;
	}
	public void toggleAllowMidgameJoin(){
		if(_allowMidgameJoin) _allowMidgameJoin = false;
		else _allowMidgameJoin = true;
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
	public void toggleInfiniteAward(){
		if(_infiniteAward) _infiniteAward = false;
		else _infiniteAward = true;
	}
	public void toggleInfiniteTools(){
		if(_infiniteTools) _infiniteTools = false;
		else _infiniteTools = true;
	}
	
	//getDataFrom
	public void getDataFrom(RMGameConfig rmGameConfig){
		setMinPlayers(rmGameConfig.getMinPlayers());
		setMaxPlayers(rmGameConfig.getMaxPlayers());
		setMinTeamPlayers(rmGameConfig.getMinTeamPlayers());
		setMaxTeamPlayers(rmGameConfig.getMaxTeamPlayers());
		setAutoRandomizeAmount(rmGameConfig.getAutoRandomizeAmount());
		setWarpToSafety(rmGameConfig.getWarpToSafety());
		setAutoRestoreWorld(rmGameConfig.getAutoRestoreWorld());
		setWarnHackedItems(rmGameConfig.getWarnHackedItems());
		setAllowHackedItems(rmGameConfig.getAllowHackedItems());
		setKeepIngame(rmGameConfig.getKeepIngame());
		setAllowMidgameJoin(rmGameConfig.getAllowMidgameJoin());
		setClearPlayerInventory(rmGameConfig.getClearPlayerInventory());
		setWarnUnequal(rmGameConfig.getWarnUnequal());
		setAllowUnequal(rmGameConfig.getAllowUnequal());
		setInfiniteAward(rmGameConfig.getInfiniteAward());
		setInfiniteTools(rmGameConfig.getInfiniteTools());
		setPlayers(rmGameConfig.getPlayers());
		setFilter(rmGameConfig.getFilter());
		setItems(rmGameConfig.getItems());
		setAward(rmGameConfig.getAward());
		setTools(rmGameConfig.getTools());
		setLog(rmGameConfig.getLog());
		setState(rmGameConfig.getState());
		setInterface(rmGameConfig.getInterface());
		setWordLimit(rmGameConfig.getWordLimit());
		setGameStats(rmGameConfig.getGameStats());
	}
}