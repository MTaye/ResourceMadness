package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class RMCommands {
	
	public RM plugin;
	
	public enum RMCommand {
		ADD,
		REMOVE,
		LIST,
		COMMANDS,
		INFO,
		INFO_FOUND,
		INFO_CLAIM,
		INFO_ITEMS,
		INFO_REWARD,
		INFO_TOOLS,
		SETTINGS,
		SETTINGS_RESET,
		SET,
		SET_MINPLAYERS,
		SET_MAXPLAYERS,
		SET_MINTEAMPLAYERS,
		SET_MAXTEAMPLAYERS,
		SET_TIMELIMIT,
		SET_RANDOM,
		SET_ADVERTISE,
		SET_RESTORE,
		SET_WARP,
		SET_MIDGAMEJOIN,
		SET_HEALPLAYER,
		SET_CLEARINVENTORY,
		SET_FOUNDASREWARD,
		SET_WARNUNEQUAL,
		SET_ALLOWUNEQUAL,
		SET_WARNHACKED,
		SET_ALLOWHACKED,
		SET_INFINITEREWARD,
		SET_INFINITETOOLS,
		MODE,
		MODE_FILTER,
		MODE_REWARD,
		MODE_TOOLS,
		FILTER,
		FILTER_INFO,
		FILTER_INFO_STRING,
		FILTER_RANDOM,
		FILTER_ADD,
		FILTER_SUBTRACT,
		FILTER_CLEAR,
		REWARD,
		REWARD_INFO,
		REWARD_INFO_STRING,
		REWARD_ADD,
		REWARD_SUBTRACT,
		REWARD_CLEAR,
		TOOLS,
		TOOLS_INFO,
		TOOLS_INFO_STRING,
		TOOLS_ADD,
		TOOLS_SUBTRACT,
		TOOLS_CLEAR,
		TEMPLATE,
		TEMPLATE_LIST,
		TEMPLATE_LOAD,
		TEMPLATE_SAVE,
		TEMPLATE_REMOVE,
		START,
		STOP,
		PAUSE,
		RESUME,
		JOIN,
		QUIT,
		READY,
		ITEMS,
		ITEM,
		RESTORE,
		CHAT,
		CHAT_WORLD,
		CHAT_GAME,
		CHAT_TEAM,
		CLAIM,
		CLAIM_FOUND,
		CLAIM_FOUND_CHEST,
		CLAIM_ITEMS,
		CLAIM_ITEMS_CHEST,
		CLAIM_REWARD,
		CLAIM_REWARD_CHEST,
		CLAIM_TOOLS,
		CLAIM_TOOLS_CHEST,
		SAVE
	}
	public HashMap<RMCommand, String> _commandMap = new HashMap<RMCommand, String>();
	public HashMap<RMCommand, List<String>> _aliasMap = new HashMap<RMCommand, List<String>>();
	public List<String> _commandList = new ArrayList<String>();
	
	public RMCommands(){
		init();
	}
	
	public RMCommands(List<String> commandList){
		init();
	}
	
	public RMCommands(RMCommands commands){
		setCommandMap(commands.getCommandMap());
	}
	
	public void init(){
		RMCommand[] commands = RMCommand.values();
		for(int i=0; i<commands.length; i++){
			RMCommand cmd = commands[i];
			//_commandMap.put(cmd, RMText.commandList.get(i));
			_aliasMap.put(cmd, new ArrayList<String>());
		}
	}
	
	public void initAliases(){
		RMCommand[] commands = RMCommand.values();
		for(int i=0; i<commands.length; i++){
			RMCommand cmd = commands[i];
			addAlias(cmd, cmd.name().toLowerCase().replace("_", " "));
		}
	}
	
	public void initDefaultAliases(){
		addAlias(RMCommand.SET_MINPLAYERS, "set min");
		addAlias(RMCommand.SET_MAXPLAYERS, "set max");
		addAlias(RMCommand.SET_MINTEAMPLAYERS, "set minteam");
		addAlias(RMCommand.SET_MAXTEAMPLAYERS, "set maxteam");
		addAlias(RMCommand.CHAT_WORLD, "w");
		addAlias(RMCommand.CHAT_GAME, "g");
		addAlias(RMCommand.CHAT_TEAM, "t");
	}
	
	public void clear(){
		_commandMap.clear();
		init();
		//initCommands();
	}
	
	public HashMap<RMCommand, String> getCommandMap(){
		return _commandMap;
	}
	public void setCommandMap(HashMap<RMCommand, String> commandMap){
		_commandMap = commandMap;
	}
	public HashMap<RMCommand, List<String>> getAliasMap(){
		return _aliasMap;
	}
	public void setAliasMap(HashMap<RMCommand, List<String>> aliasMap){
		_aliasMap = aliasMap;
	}
	
	//Clear
	public void clearCommandMap(){
		_commandMap.clear();
	}
	
	public void addCommand(RMCommand cmd, String command){
		command = command.toLowerCase();
		_commandMap.put(cmd, command);
	}
	
	public void addCommand(RMCommand cmd, String... commands){
		for(String command : commands){
			addCommand(cmd, command);
		}
	}
	
	public void removeCommand(RMCommand cmd, String command){
		command = command.toLowerCase();
		if(_commandMap.containsKey(cmd)){
			_commandMap.remove(cmd);
		}
	}
	
	public void clearAliasMap(){
		_aliasMap.clear();
	}
		
	public void addAlias(RMCommand cmd, String alias){
		alias = alias.toLowerCase();
		if(!_aliasMap.containsKey(cmd)){
			List<String> aliasList = new ArrayList<String>();
			aliasList.add(alias);
			_aliasMap.put(cmd, aliasList);
		}
		else{
			List<String> aliasList = _aliasMap.get(cmd);
			if(!aliasList.contains(alias)) aliasList.add(alias);
		}
	}
	
	public void addAlias(RMCommand cmd, String... aliases){
		for(String alias : aliases){
			addAlias(cmd, alias);
		}
	}
	
	public void removeAlias(RMCommand cmd, String alias){
		alias = alias.toLowerCase();
		if(_aliasMap.containsKey(cmd)){
			List<String> aliasList = _aliasMap.get(cmd);
			if(aliasList.contains(alias)) aliasList.remove(alias);
		}
	}
	
	public void clearAlias(RMCommand cmd){
		if(_aliasMap.containsKey(cmd)) _aliasMap.get(cmd).clear();
	}
}