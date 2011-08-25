package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

public class RMConfig {
	
	public enum PermissionType { P3, PEX, BUKKIT, FALSE };
	public enum Lock { restore, warpToSafety, warnHackedItems, allowHackedItems, keepIngame, allowMidgameJoin, clearPlayerInventory, warnUnequal, allowUnequal, infiniteAward, infiniteTools };
	
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
	private boolean _restore = true;
	private boolean _warpToSafety = true;
	private boolean _warnHackedItems = true;
	private boolean _allowHackedItems = false;
	private boolean _keepIngame = true;
	private boolean _allowMidgameJoin = false;
	private boolean _clearPlayerInventory = true;
	private boolean _warnUnequal = true;
	private boolean _allowUnequal = true;
	private boolean _infiniteAward = false;
	private boolean _infiniteTools = false;
	
	public RMConfig(){
	}
	
	public RMConfig(RMConfig rmConfig){
		setLock(rmConfig.getLock());
		setAutoSave(rmConfig.getAutoSave());
		setPermissionType(rmConfig.getPermissionType());
		setUseRestore(rmConfig.getUseRestore());
		setMaxGames(rmConfig.getMaxGames());
		setMaxGamesPerPlayer(rmConfig.getMaxGamesPerPlayer());
		setMinPlayersPerGame(rmConfig.getMinPlayersPerGame());
		setMaxPlayersPerGame(rmConfig.getMaxPlayersPerGame());
		setMinPlayersPerTeam(rmConfig.getMinPlayersPerTeam());
		setMaxPlayersPerTeam(rmConfig.getMaxPlayersPerTeam());
		setRestore(rmConfig.getRestore(), rmConfig.getLock().contains(Lock.restore));
		setWarpToSafety(rmConfig.getWarpToSafety(), rmConfig.getLock().contains(Lock.warpToSafety));
		setWarnHackedItems(rmConfig.getWarnHackedItems(), rmConfig.getLock().contains(Lock.warnHackedItems));
		setAllowHackedItems(rmConfig.getAllowHackedItems(), rmConfig.getLock().contains(Lock.allowHackedItems));
		setKeepIngame(rmConfig.getKeepIngame(), rmConfig.getLock().contains(Lock.keepIngame));
		setAllowMidgameJoin(rmConfig.getAllowMidgameJoin(), rmConfig.getLock().contains(Lock.allowMidgameJoin));
		setClearPlayerInventory(rmConfig.getClearPlayerInventory(), rmConfig.getLock().contains(Lock.clearPlayerInventory));
		setWarnUnequal(rmConfig.getWarnUnequal(), rmConfig.getLock().contains(Lock.warnUnequal));
		setAllowUnequal(rmConfig.getAllowUnequal(), rmConfig.getLock().contains(Lock.allowUnequal));
		setInfiniteAward(rmConfig.getInfiniteAward(), rmConfig.getLock().contains(Lock.infiniteAward));
		setInfiniteTools(rmConfig.getInfiniteTools(), rmConfig.getLock().contains(Lock.infiniteTools));
	}
	
	//Get
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
	public boolean getRestore() { return _restore; }
	public boolean getWarpToSafety() { return _warpToSafety; }
	public boolean getWarnHackedItems() { return _warnHackedItems; }
	public boolean getAllowHackedItems() { return _allowHackedItems; }
	public boolean getKeepIngame() { return _keepIngame; }
	public boolean getAllowMidgameJoin() { return _allowMidgameJoin; }
	public boolean getClearPlayerInventory() { return _clearPlayerInventory; }
	public boolean getWarnUnequal() { return _warnUnequal; }
	public boolean getAllowUnequal() { return _allowUnequal; }
	public boolean getInfiniteAward() { return _infiniteAward; }
	public boolean getInfiniteTools() { return _infiniteTools; }
	
	//Set
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
	public void setMinPlayersPerGame(int minPlayersPerGame){
		_minPlayersPerGame = minPlayersPerGame;
		if(_minPlayersPerGame<0) _minPlayersPerGame = 1;
	}
	public void setMaxPlayersPerGame(int maxPlayersPerGame){
		_maxPlayersPerGame = maxPlayersPerGame;
		if(_maxPlayersPerGame<0) _maxPlayersPerGame = 0;
	}
	public void setMinPlayersPerTeam(int minTeamPlayersPerTeam){
		_minPlayersPerTeam = minTeamPlayersPerTeam;
		if(_minPlayersPerTeam<0) _minPlayersPerTeam = 1;
	}
	public void setMaxPlayersPerTeam(int maxTeamPlayersPerTeam){
		_maxPlayersPerTeam = maxTeamPlayersPerTeam;
		if(_maxPlayersPerTeam<0) _maxPlayersPerTeam = 0;
	}
	public void setRestore(boolean restore, boolean lock){
		if(lock) addLock(Lock.restore);
		_restore = restore;
	}
	public void setWarpToSafety(boolean warpToSafety, boolean lock){
		if(lock) addLock(Lock.warpToSafety);
		_warpToSafety = warpToSafety;
	}
	public void setWarnHackedItems(boolean warnHackedItems, boolean lock){
		if(lock) addLock(Lock.warnHackedItems);
		_warnHackedItems = warnHackedItems;
	}
	public void setAllowHackedItems(boolean allowHackedItems, boolean lock){
		if(lock) addLock(Lock.allowHackedItems);
		_allowHackedItems = allowHackedItems;
	}
	public void setKeepIngame(boolean keepIngame, boolean lock){
		if(lock) addLock(Lock.keepIngame);
		_keepIngame = keepIngame;
	}
	public void setAllowMidgameJoin(boolean allowMidgameJoin, boolean lock){
		if(lock) addLock(Lock.allowMidgameJoin);
		_allowMidgameJoin = allowMidgameJoin;
	}
	public void setClearPlayerInventory(boolean clearPlayerInventory, boolean lock){
		if(lock) addLock(Lock.clearPlayerInventory);
		_clearPlayerInventory = clearPlayerInventory;
	}
	public void setWarnUnequal(boolean warnUnequal, boolean lock){
		if(lock) addLock(Lock.warnUnequal);
		_warnUnequal = warnUnequal;
	}
	public void setAllowUnequal(boolean allowUnequal, boolean lock){
		if(lock) addLock(Lock.allowUnequal);
		_allowUnequal = allowUnequal;
	}
	public void setInfiniteAward(boolean infiniteAward, boolean lock){
		if(lock) addLock(Lock.infiniteAward);
		_infiniteAward = infiniteAward;
	}
	public void setInfiniteTools(boolean infiniteTools, boolean lock){
		if(lock) addLock(Lock.infiniteTools);
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
}