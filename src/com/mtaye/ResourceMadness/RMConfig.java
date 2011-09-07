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
	public enum Lock { minPlayersPerGame, maxPlayersPerGame, minPlayersPerTeam, maxPlayersPerTeam, timeLimit,
		autoRandomizeAmount, autoRestoreWorld, warpToSafety, keepIngame, allowMidgameJoin, healPlayer, clearPlayerInventory,
		warnUnequal, allowUnequal, warnHackedItems, allowHackedItems, infiniteReward, infiniteTools };
	
	private List<Lock> _lock = new ArrayList<Lock>();
	private int _autoSave = 10;
	private PermissionType _permissionType = PermissionType.FALSE;
	private boolean _useRestore = true;
	private int _maxGames = 0;
	private int _maxGamesPerPlayer = 0;
	private int _minPlayersPerGame = 1;
	private int _maxPlayersPerGame = 0;
	private int _minPlayersPerTeam = 1;
	private int _maxPlayersPerTeam = 0;
	private int _timeLimit = 0;
	private int _autoRandomizeAmount = 0;
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
		setMinPlayersPerGame(config.getMinPlayersPerGame(), config.getLock().contains(Lock.minPlayersPerGame));
		setMaxPlayersPerGame(config.getMaxPlayersPerGame(), config.getLock().contains(Lock.maxPlayersPerGame));
		setMinPlayersPerTeam(config.getMinPlayersPerTeam(), config.getLock().contains(Lock.minPlayersPerTeam));
		setMaxPlayersPerTeam(config.getMaxPlayersPerTeam(), config.getLock().contains(Lock.maxPlayersPerTeam));
		setTimeLimit(config.getTimeLimit(), config.getLock().contains(Lock.timeLimit));
		setAutoRandomizeAmount(config.getAutoRandomizeAmount(), config.getLock().contains(Lock.autoRandomizeAmount));
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
	public int getMinPlayersPerGame() { return _minPlayersPerGame; }
	public int getMaxPlayersPerGame() { return _maxPlayersPerGame; }
	public int getMinPlayersPerTeam() { return _minPlayersPerTeam; }
	public int getMaxPlayersPerTeam() { return _maxPlayersPerTeam; }
	public int getTimeLimit() { return _timeLimit; }
	public int getAutoRandomizeAmount() { return _autoRandomizeAmount; }
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
	public void setMinPlayersPerGame(int minPlayersPerGame, boolean lock){
		if(lock) addLock(Lock.minPlayersPerGame);
		else removeLock(Lock.minPlayersPerGame);
		_minPlayersPerGame = minPlayersPerGame;
		if(_minPlayersPerGame<0) _minPlayersPerGame = 1;
	}
	public void setMaxPlayersPerGame(int maxPlayersPerGame, boolean lock){
		if(lock) addLock(Lock.maxPlayersPerGame);
		else removeLock(Lock.maxPlayersPerGame);
		_maxPlayersPerGame = maxPlayersPerGame;
		if(_maxPlayersPerGame<0) _maxPlayersPerGame = 0;
	}
	public void setMinPlayersPerTeam(int minTeamPlayersPerTeam, boolean lock){
		if(lock) addLock(Lock.minPlayersPerTeam);
		else removeLock(Lock.minPlayersPerTeam);
		_minPlayersPerTeam = minTeamPlayersPerTeam;
		if(_minPlayersPerTeam<0) _minPlayersPerTeam = 1;
	}
	public void setMaxPlayersPerTeam(int maxTeamPlayersPerTeam, boolean lock){
		if(lock) addLock(Lock.maxPlayersPerTeam);
		else removeLock(Lock.maxPlayersPerTeam);
		_maxPlayersPerTeam = maxTeamPlayersPerTeam;
		if(_maxPlayersPerTeam<0) _maxPlayersPerTeam = 0;
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