package com.mtaye.ResourceMadness;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class Config extends GameConfig{

	private int _typeLimit = 50;
	
	public enum PermissionType { P3, PEX, BUKKIT, AUTO, FALSE };
	
	private String _language = "";
	private int _autosave = 30;
	private PermissionType _permissionType = PermissionType.AUTO;
	private int _maxGames = 0;
	private int _maxGamesPerPlayer = 0;
	
	private Commands _commands = new Commands();
	
	public Config(){
		//addLock(Setting.allowhacked);
		//addLock(Setting.infinitereward);
		//addLock(Setting.infinitetools);
	}
	
	public Config(Config config){
		setTypeLimit(config.getTypeLimit());
		setAutoSave(config.getAutoSave());
		setLanguage(config.getLanguage());
		setPermissionType(config.getPermissionType());
		setMaxGames(config.getMaxGames());
		setMaxGamesPerPlayer(config.getMaxGamesPerPlayer());
		setSettingLibrary(config.getSettingLibrary().clone());
		setCommands(config.getCommands());
	}
	
	//Get
	public int getTypeLimit() { return _typeLimit; }
	public String getLanguage() { return _language; }
	public int getAutoSave() { return _autosave; }
	public PermissionType getPermissionType() { return _permissionType; }
	public int getMaxGames() { return _maxGames; }
	public int getMaxGamesPerPlayer() { return _maxGamesPerPlayer; }
	public Commands getCommands() { return _commands; }
		
	//Set
	public void setTypeLimit(int typeLimit){
		_typeLimit = typeLimit;
		if(_typeLimit<10) _typeLimit = 10;
	}
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
	public void setMaxGames(int maxGames){
		_maxGames = maxGames;
		if(_maxGames<0) _maxGames = 0;
	}
	public void setMaxGamesPerPlayer(int maxGamesPerPlayer){
		_maxGamesPerPlayer = maxGamesPerPlayer;
		if(_maxGamesPerPlayer<0) _maxGamesPerPlayer = 0;
	}
	
	public void setCommands(Commands commands){
		_commands = commands;
	}
}