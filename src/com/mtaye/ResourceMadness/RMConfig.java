package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import com.mtaye.ResourceMadness.RMGame.Setting;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMConfig {

	private int _typeLimit = 50;
	
	public enum PermissionType { P3, PEX, BUKKIT, AUTO, FALSE };
	
	public List<Setting> _lock = new ArrayList<Setting>();
	private String _language = "";
	private int _autosave = 10;
	private PermissionType _permissionType = PermissionType.AUTO;
	private boolean _useRestore = true;
	private int _maxGames = 0;
	private int _maxGamesPerPlayer = 0;
	private int _minPlayers = 1;
	private int _maxPlayers = 0;
	private int _minTeamPlayers = 1;
	private int _maxTeamPlayers = 0;
	private int _safeZone = 0;
	private int _timeLimit = 0;
	private int _autoRandomizeAmount = 0;
	private boolean _advertise = false;
	private boolean _autoRestoreWorld = true;
	private boolean _warpToSafety = true;
	private boolean _allowMidgameJoin = true;
	private boolean _healPlayer = true;
	private boolean _clearPlayerInventory = true;
	private boolean _foundAsReward = false;
	private boolean _warnUnequal = true;
	private boolean _allowUnequal = true;
	private boolean _warnHackedItems = true;
	private boolean _allowHackedItems = false;
	private boolean _infiniteReward = false;
	private boolean _infiniteTools = false;
	private RMCommands _commands = new RMCommands();
	
	public RMConfig(){
		_lock.add(Setting.allowHackedItems);
		_lock.add(Setting.infiniteReward);
		_lock.add(Setting.infiniteTools);
	}
	
	public RMConfig(RMConfig config){
		setTypeLimit(config.getTypeLimit());
		setLock(config.getLock());
		setAutoSave(config.getAutoSave());
		setLanguage(config.getLanguage());
		setPermissionType(config.getPermissionType());
		setUseRestore(config.getUseRestore());
		setMaxGames(config.getMaxGames());
		setMaxGamesPerPlayer(config.getMaxGamesPerPlayer());
		setSetting(Setting.minPlayers, config.getMinPlayers(), config.getLock().contains(Setting.minPlayers));
		setSetting(Setting.maxPlayers, config.getMaxPlayers(), config.getLock().contains(Setting.maxPlayers));
		setSetting(Setting.minTeamPlayers, config.getMinTeamPlayers(), config.getLock().contains(Setting.minTeamPlayers));
		setSetting(Setting.maxTeamPlayers, config.getMaxTeamPlayers(), config.getLock().contains(Setting.maxTeamPlayers));
		setSetting(Setting.safeZone, config.getSafeZone(), config.getLock().contains(Setting.safeZone));
		setSetting(Setting.timeLimit, config.getTimeLimit(), config.getLock().contains(Setting.timeLimit));
		setSetting(Setting.autoRandomizeAmount, config.getAutoRandomizeAmount(), config.getLock().contains(Setting.autoRandomizeAmount));
		setSetting(Setting.advertise, config.getAdvertise(), config.getLock().contains(Setting.advertise));
		setSetting(Setting.autoRestoreWorld, config.getAutoRestoreWorld(), config.getLock().contains(Setting.autoRestoreWorld));
		setSetting(Setting.warpToSafety, config.getWarpToSafety(), config.getLock().contains(Setting.warpToSafety));
		setSetting(Setting.allowMidgameJoin, config.getAllowMidgameJoin(), config.getLock().contains(Setting.allowMidgameJoin));
		setSetting(Setting.healPlayer, config.getHealPlayer(), config.getLock().contains(Setting.healPlayer));
		setSetting(Setting.clearPlayerInventory, config.getClearPlayerInventory(), config.getLock().contains(Setting.clearPlayerInventory));
		setSetting(Setting.foundAsReward, config.getFoundAsReward(), config.getLock().contains(Setting.foundAsReward));
		setSetting(Setting.warnUnequal, config.getWarnUnequal(), config.getLock().contains(Setting.warnUnequal));
		setSetting(Setting.allowUnequal, config.getAllowUnequal(), config.getLock().contains(Setting.allowUnequal));
		setSetting(Setting.warnHackedItems, config.getWarnHackedItems(), config.getLock().contains(Setting.warnHackedItems));
		setSetting(Setting.allowHackedItems, config.getAllowHackedItems(), config.getLock().contains(Setting.allowHackedItems));
		setSetting(Setting.infiniteReward, config.getInfiniteReward(), config.getLock().contains(Setting.infiniteReward));
		setSetting(Setting.infiniteTools, config.getInfiniteTools(), config.getLock().contains(Setting.infiniteTools));
		setCommands(config.getCommands());
	}
	
	//Get
	public int getTypeLimit() { return _typeLimit; }
	public List<Setting> getLock() { return _lock; }
	public String getLanguage() { return _language; }
	public int getAutoSave() { return _autosave; }
	public PermissionType getPermissionType() { return _permissionType; }
	public boolean getUseRestore() { return _useRestore; }
	public int getMaxGames() { return _maxGames; }
	public int getMaxGamesPerPlayer() { return _maxGamesPerPlayer; }
	public int getMinPlayers() { return _minPlayers; }
	public int getMaxPlayers() { return _maxPlayers; }
	public int getMinTeamPlayers() { return _minTeamPlayers; }
	public int getMaxTeamPlayers() { return _maxTeamPlayers; }
	public int getSafeZone() { return _safeZone; }
	public int getTimeLimit() { return _timeLimit; }
	public int getAutoRandomizeAmount() { return _autoRandomizeAmount; }
	public boolean getAdvertise() { return _advertise; }
	public boolean getAutoRestoreWorld() { return _autoRestoreWorld; }
	public boolean getWarpToSafety() { return _warpToSafety; }
	public boolean getAllowMidgameJoin() { return _allowMidgameJoin; }
	public boolean getHealPlayer() { return _healPlayer; }
	public boolean getClearPlayerInventory() { return _clearPlayerInventory; }
	public boolean getFoundAsReward() { return _foundAsReward; }
	public boolean getWarnUnequal() { return _warnUnequal; }
	public boolean getAllowUnequal() { return _allowUnequal; }
	public boolean getWarnHackedItems() { return _warnHackedItems; }
	public boolean getAllowHackedItems() { return _allowHackedItems; }
	public boolean getInfiniteReward() { return _infiniteReward; }
	public boolean getInfiniteTools() { return _infiniteTools; }
	public RMCommands getCommands() { return _commands; }
		
	//Set
	public void setTypeLimit(int typeLimit){
		_typeLimit = typeLimit;
		if(_typeLimit<10) _typeLimit = 10;
	}
	public void setLock(List<Setting> locked) { _lock = locked; }
	public void setLanguage(String language){
		if(language==null) language = "";
		_language = language.trim().replace(".lng", "").toLowerCase();
	}
	public void setAutoSave(int value) { _autosave = value<0?0:value; }
	public void setPermissionType(PermissionType permissionType) { _permissionType = permissionType; }
	public void setPermissionTypeByString(String arg){
		if((arg.equalsIgnoreCase("p3"))||(arg.equalsIgnoreCase("perm3"))||(arg.equalsIgnoreCase("permissions3"))) setPermissionType(PermissionType.P3);
		else if((arg.equalsIgnoreCase("pex"))||(arg.equalsIgnoreCase("permex"))||(arg.equalsIgnoreCase("permissionsex"))) setPermissionType(PermissionType.PEX);
		else if(arg.equalsIgnoreCase("bukkit")) setPermissionType(PermissionType.BUKKIT);
		else if(arg.equalsIgnoreCase("auto")) setPermissionType(PermissionType.AUTO);
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
	
	public void setSetting(Setting setting, int value, boolean lock){
		if(lock) addLock(setting);
		else removeLock(setting);
		switch(setting){
			case minPlayers:
				_minPlayers = value;
				if(_minPlayers<0) _minPlayers = 1;
				break;
			case maxPlayers:
				_maxPlayers = value;
				if(_maxPlayers<0) _maxPlayers = 0;
				break;
			case minTeamPlayers:
				_minTeamPlayers = value;
				if(_minTeamPlayers<0) _minTeamPlayers = 1;
				break;
			case maxTeamPlayers:
				_maxTeamPlayers = value;
				if(_maxTeamPlayers<0) _maxTeamPlayers = 0;
				break;
			case timeLimit:
				_timeLimit = value;
				if(_timeLimit<0) _timeLimit = 0;
				break;
			case autoRandomizeAmount:
				_autoRandomizeAmount = value;
				if(_autoRandomizeAmount<0) _autoRandomizeAmount = 0;
				break;
		}
	}
	
	public void setSetting(Setting setting, boolean value, boolean lock){
		if(lock) addLock(setting);
		else removeLock(setting);
		switch(setting){
			case advertise: _advertise = value; break;
			case autoRestoreWorld: _autoRestoreWorld = value; break;
			case warpToSafety: _warpToSafety = value; break;
			case allowMidgameJoin: _allowMidgameJoin = value; break;
			case healPlayer: _healPlayer = value; break;
			case clearPlayerInventory: _clearPlayerInventory = value; break;
			case foundAsReward: _foundAsReward = value; break;
			case warnUnequal: _warnUnequal = value; break;
			case allowUnequal: _allowUnequal = value; break;
			case warnHackedItems: _warnHackedItems = value; break;
			case allowHackedItems: _allowHackedItems = value; break;
			case infiniteReward: _infiniteReward = value; break;
			case infiniteTools: _infiniteTools = value; break;
		}
	}
	
	public void setCommands(RMCommands commands){
		_commands = commands;
	}
	
	//LOCK
	//Add
	public void addLock(Setting lock){
		if(!_lock.contains(lock)) _lock.add(lock);
	}
	//Remove
	public void removeLock(Setting lock){
		if(_lock.contains(lock)) _lock.remove(lock);
	}
	
	public boolean isLocked(Setting lock){
		if(_lock.contains(lock)) return true;
		return false;
	}
}