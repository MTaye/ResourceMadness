package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMConfig {

	private int _typeLimit = 50;
	
	public enum PermissionType { P3, PEX, BUKKIT, FALSE };
	public enum Lock { minPlayers, maxPlayers, minTeamPlayers, maxTeamPlayers, timeLimit, autoRandomizeAmount,
		advertise, autoRestoreWorld, warpToSafety, keepIngame, allowMidgameJoin, healPlayer, clearPlayerInventory,
		warnUnequal, allowUnequal, warnHackedItems, allowHackedItems, infiniteReward, infiniteTools };
	
	private List<Lock> _lock = new ArrayList<Lock>();
	private int _autoSave = 10;
	private PermissionType _permissionType = PermissionType.FALSE;
	private boolean _useRestore = true;
	private int _maxGames = 0;
	private int _maxGamesPerPlayer = 0;
	private int _minPlayers = 1;
	private int _maxPlayers = 0;
	private int _minTeamPlayers = 1;
	private int _maxTeamPlayers = 0;
	private int _timeLimit = 0;
	private int _autoRandomizeAmount = 0;
	private boolean _advertise = false;
	private boolean _autoRestoreWorld = true;
	private boolean _warpToSafety = true;
	private boolean _allowMidgameJoin = true;
	private boolean _healPlayer = true;
	private boolean _clearPlayerInventory = true;
	private boolean _warnUnequal = true;
	private boolean _allowUnequal = true;
	private boolean _warnHackedItems = true;
	private boolean _allowHackedItems = false;
	private boolean _infiniteReward = false;
	private boolean _infiniteTools = false;
	
	public RMConfig(){
		_lock.add(Lock.allowHackedItems);
		_lock.add(Lock.infiniteReward);
		_lock.add(Lock.infiniteTools);
	}
	
	public RMConfig(RMConfig config){
		setTypeLimit(config.getTypeLimit());
		setLock(config.getLock());
		setAutoSave(config.getAutoSave());
		setPermissionType(config.getPermissionType());
		setUseRestore(config.getUseRestore());
		setMaxGames(config.getMaxGames());
		setMaxGamesPerPlayer(config.getMaxGamesPerPlayer());
		setMinPlayers(config.getMinPlayers(), config.getLock().contains(Lock.minPlayers));
		setMaxPlayers(config.getMaxPlayers(), config.getLock().contains(Lock.maxPlayers));
		setMinTeamPlayers(config.getMinTeamPlayers(), config.getLock().contains(Lock.minTeamPlayers));
		setMaxTeamPlayers(config.getMaxTeamPlayers(), config.getLock().contains(Lock.maxTeamPlayers));
		setTimeLimit(config.getTimeLimit(), config.getLock().contains(Lock.timeLimit));
		setAutoRandomizeAmount(config.getAutoRandomizeAmount(), config.getLock().contains(Lock.autoRandomizeAmount));
		setAdvertise(config.getAdvertise(), config.getLock().contains(Lock.advertise));
		setAutoRestoreWorld(config.getAutoRestoreWorld(), config.getLock().contains(Lock.autoRestoreWorld));
		setWarpToSafety(config.getWarpToSafety(), config.getLock().contains(Lock.warpToSafety));
		setAllowMidgameJoin(config.getAllowMidgameJoin(), config.getLock().contains(Lock.allowMidgameJoin));
		setHealPlayer(config.getHealPlayer(), config.getLock().contains(Lock.healPlayer));
		setClearPlayerInventory(config.getClearPlayerInventory(), config.getLock().contains(Lock.clearPlayerInventory));
		setWarnUnequal(config.getWarnUnequal(), config.getLock().contains(Lock.warnUnequal));
		setAllowUnequal(config.getAllowUnequal(), config.getLock().contains(Lock.allowUnequal));
		setWarnHackedItems(config.getWarnHackedItems(), config.getLock().contains(Lock.warnHackedItems));
		setAllowHackedItems(config.getAllowHackedItems(), config.getLock().contains(Lock.allowHackedItems));
		setInfiniteReward(config.getInfiniteReward(), config.getLock().contains(Lock.infiniteReward));
		setInfiniteTools(config.getInfiniteTools(), config.getLock().contains(Lock.infiniteTools));
	}
	
	//Get
	public int getTypeLimit() { return _typeLimit; }
	public List<Lock> getLock() { return _lock; }
	public int getAutoSave() { return _autoSave; }
	public PermissionType getPermissionType() { return _permissionType; }
	public boolean getUseRestore() { return _useRestore; }
	public int getMaxGames() { return _maxGames; }
	public int getMaxGamesPerPlayer() { return _maxGamesPerPlayer; }
	public int getMinPlayers() { return _minPlayers; }
	public int getMaxPlayers() { return _maxPlayers; }
	public int getMinTeamPlayers() { return _minTeamPlayers; }
	public int getMaxTeamPlayers() { return _maxTeamPlayers; }
	public int getTimeLimit() { return _timeLimit; }
	public int getAutoRandomizeAmount() { return _autoRandomizeAmount; }
	public boolean getAdvertise() { return _advertise; }
	public boolean getAutoRestoreWorld() { return _autoRestoreWorld; }
	public boolean getWarpToSafety() { return _warpToSafety; }
	public boolean getAllowMidgameJoin() { return _allowMidgameJoin; }
	public boolean getHealPlayer() { return _healPlayer; }
	public boolean getClearPlayerInventory() { return _clearPlayerInventory; }
	public boolean getWarnUnequal() { return _warnUnequal; }
	public boolean getAllowUnequal() { return _allowUnequal; }
	public boolean getWarnHackedItems() { return _warnHackedItems; }
	public boolean getAllowHackedItems() { return _allowHackedItems; }
	public boolean getInfiniteReward() { return _infiniteReward; }
	public boolean getInfiniteTools() { return _infiniteTools; }
	
	//Set
	public void setTypeLimit(int typeLimit){
		_typeLimit = typeLimit;
		if(_typeLimit<10) _typeLimit = 10;
	}
	public void setLock(List<Lock> locked) { _lock = locked; }
	public void setAutoSave(int value) { _autoSave = value<0?0:value; }
	public void setPermissionType(PermissionType permissionType) { _permissionType = permissionType; }
	public void setPermissionTypeByString(String arg){
		if((arg.equalsIgnoreCase("p3"))||(arg.equalsIgnoreCase("perm3"))||(arg.equalsIgnoreCase("permissions3"))) setPermissionType(PermissionType.P3);
		else if((arg.equalsIgnoreCase("pex"))||(arg.equalsIgnoreCase("permex"))||(arg.equalsIgnoreCase("permissionsex"))) setPermissionType(PermissionType.PEX);
		//else if(arg.equalsIgnoreCase("bukkit")) setPermissionType(PermissionType.BUKKIT);
		else setPermissionType(PermissionType.FALSE);
		return;
	}
	public void setUseRestore(boolean useRestore) { _useRestore = useRestore; }
	public void setMaxGames(int maxGames){
		_maxGames = maxGames;
		if(_maxGames<0) _maxGames = 0;
	}
	public void setMaxGamesPerPlayer(int maxGamesPerPlayer){
		_maxGamesPerPlayer = maxGamesPerPlayer;
		if(_maxGamesPerPlayer<0) _maxGamesPerPlayer = 0;
	}
	public void setMinPlayers(int minPlayers, boolean lock){
		if(lock) addLock(Lock.minPlayers);
		else removeLock(Lock.minPlayers);
		_minPlayers = minPlayers;
		if(_minPlayers<0) _minPlayers = 1;
	}
	public void setMaxPlayers(int maxPlayers, boolean lock){
		if(lock) addLock(Lock.maxPlayers);
		else removeLock(Lock.maxPlayers);
		_maxPlayers = maxPlayers;
		if(_maxPlayers<0) _maxPlayers = 0;
	}
	public void setMinTeamPlayers(int minTeamPlayers, boolean lock){
		if(lock) addLock(Lock.minTeamPlayers);
		else removeLock(Lock.minTeamPlayers);
		_minTeamPlayers = minTeamPlayers;
		if(_minTeamPlayers<0) _minTeamPlayers = 1;
	}
	public void setMaxTeamPlayers(int maxTeamPlayers, boolean lock){
		if(lock) addLock(Lock.maxTeamPlayers);
		else removeLock(Lock.maxTeamPlayers);
		_maxTeamPlayers = maxTeamPlayers;
		if(_maxTeamPlayers<0) _maxTeamPlayers = 0;
	}
	public void setTimeLimit(int timeLimit, boolean lock){
		if(lock) addLock(Lock.timeLimit);
		else removeLock(Lock.timeLimit);
		_timeLimit = timeLimit;
		if(_timeLimit<0) _timeLimit = 0;
	}
	public void setAutoRandomizeAmount(int autoRandomizeAmount, boolean lock){
		if(lock) addLock(Lock.autoRandomizeAmount);
		else removeLock(Lock.autoRandomizeAmount);
		_autoRandomizeAmount = autoRandomizeAmount;
		if(_autoRandomizeAmount<0) _autoRandomizeAmount = 0;
	}
	public void setAdvertise(boolean advertise, boolean lock){
		if(lock) addLock(Lock.advertise);
		else removeLock(Lock.advertise);
		_advertise = advertise;
	}
	public void setAutoRestoreWorld(boolean autoRestoreWorld, boolean lock){
		if(lock) addLock(Lock.autoRestoreWorld);
		else removeLock(Lock.autoRestoreWorld);
		_autoRestoreWorld = autoRestoreWorld;
	}
	public void setWarpToSafety(boolean warpToSafety, boolean lock){
		if(lock) addLock(Lock.warpToSafety);
		else removeLock(Lock.warpToSafety);
		_warpToSafety = warpToSafety;
	}
	public void setAllowMidgameJoin(boolean allowMidgameJoin, boolean lock){
		if(lock) addLock(Lock.allowMidgameJoin);
		else removeLock(Lock.allowMidgameJoin);
		_allowMidgameJoin = allowMidgameJoin;
	}
	public void setHealPlayer(boolean healPlayer, boolean lock){
		if(lock) addLock(Lock.healPlayer);
		else removeLock(Lock.healPlayer);
		_healPlayer = healPlayer;
	}
	public void setClearPlayerInventory(boolean clearPlayerInventory, boolean lock){
		if(lock) addLock(Lock.clearPlayerInventory);
		else removeLock(Lock.clearPlayerInventory);
		_clearPlayerInventory = clearPlayerInventory;
	}
	public void setWarnUnequal(boolean warnUnequal, boolean lock){
		if(lock) addLock(Lock.warnUnequal);
		else removeLock(Lock.warnUnequal);
		_warnUnequal = warnUnequal;
	}
	public void setAllowUnequal(boolean allowUnequal, boolean lock){
		if(lock) addLock(Lock.allowUnequal);
		else removeLock(Lock.allowMidgameJoin);
		_allowUnequal = allowUnequal;
	}
	public void setWarnHackedItems(boolean warnHackedItems, boolean lock){
		if(lock) addLock(Lock.warnHackedItems);
		else removeLock(Lock.warnHackedItems);
		_warnHackedItems = warnHackedItems;
	}
	public void setAllowHackedItems(boolean allowHackedItems, boolean lock){
		if(lock) addLock(Lock.allowHackedItems);
		else removeLock(Lock.allowHackedItems);
		_allowHackedItems = allowHackedItems;
	}
	public void setInfiniteReward(boolean infiniteReward, boolean lock){
		if(lock) addLock(Lock.infiniteReward);
		else removeLock(Lock.infiniteReward);
		_infiniteReward = infiniteReward;
	}
	public void setInfiniteTools(boolean infiniteTools, boolean lock){
		if(lock) addLock(Lock.infiniteTools);
		else removeLock(Lock.infiniteTools);
		_infiniteTools = infiniteTools;
	}
	
	//Add
	public void addLock(Lock lock){
		if(!_lock.contains(lock)) _lock.add(lock);
	}
	//Remove
	public void removeLock(Lock lock){
		if(_lock.contains(lock)) _lock.remove(lock);
	}
	
	public boolean isLocked(Lock lock){
		if(_lock.contains(lock)) return true;
		return false;
	}
}