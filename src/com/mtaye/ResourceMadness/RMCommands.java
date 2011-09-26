package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class RMCommands {
	
	public enum RMCommand {
		ADD, REMOVE, LIST, COMMANDS, INFO, INFO_FOUND, INFO_CLAIM, INFO_ITEMS, INFO_REWARD, INFO_TOOLS,
		SETTINGS, SETTINGS_RESET,
		SET, SET_MINPLAYERS, SET_MAXPLAYERS, SET_MINTEAMPLAYERS, SET_MAXTEAMPLAYERS, SET_TIMELIMIT, SET_RANDOM,
		SET_ADVERTISE, SET_RESTORE, SET_WARP, SET_MIDGAMEJOIN, SET_HEALPLAYER, SET_CLEARINVENTORY, SET_FOUNDASREWARD,
		SET_WARNUNEQUAL, SET_ALLOWUNEQUAL, SET_WARNHACKED, SET_ALLOWHACKED, SET_INFINITEREWARD, SET_INFINITETOOLS,
		MODE, MODE_FILTER, MODE_REWARD, MODE_TOOLS,
		FILTER, FILTER_INFO, FILTER_INFO_STRING, FILTER_RANDOM, FILTER_ADD, FILTER_SUBTRACT, FILTER_CLEAR,
		REWARD, REWARD_INFO, REWARD_INFO_STRING, REWARD_ADD, REWARD_SUBTRACT, REWARD_CLEAR,
		TOOLS, TOOLS_INFO, TOOLS_INFO_STRING, TOOLS_ADD, TOOLS_SUBTRACT, TOOLS_CLEAR,
		TEMPLATE, TEMPLATE_LIST, TEMPLATE_LOAD, TEMPLATE_SAVE, TEMPLATE_REMOVE,
		START, STOP, PAUSE, RESUME, JOIN, QUIT, READY, ITEMS, ITEM, RESTORE,
		CHAT, CHAT_WORLD, CHAT_GAME, CHAT_TEAM,
		CLAIM, CLAIM_FOUND, CLAIM_FOUND_CHEST, CLAIM_ITEMS, CLAIM_ITEMS_CHEST, CLAIM_REWARD, CLAIM_REWARD_CHEST, CLAIM_TOOLS, CLAIM_TOOLS_CHEST,
		SAVE
	}
	public HashMap<RMCommand, List<String>> _commandMap = new HashMap<RMCommand, List<String>>();
	
	public RMCommands(){
		init();
	}
	
	public RMCommands(RMCommands commands){
		setCommandMap(commands.getCommandMap());
	}
	
	private void init(){
		for(RMCommand cmd : RMCommand.values()){
			addCommand(cmd, cmd.name().toLowerCase().replaceAll("_", " "));
		}
	}
	
	public void initDefaults(){
		addCommand(RMCommand.SET_MINPLAYERS, "set min");
		addCommand(RMCommand.SET_MAXPLAYERS, "set max");
		addCommand(RMCommand.SET_MINTEAMPLAYERS, "set minteam");
		addCommand(RMCommand.SET_MAXTEAMPLAYERS, "set maxteam");
		addCommand(RMCommand.CHAT_WORLD, "w");
		addCommand(RMCommand.CHAT_GAME, "g");
		addCommand(RMCommand.CHAT_TEAM, "t");
	}
	
	public void clear(){
		_commandMap.clear();
		init();
	}
	public Set<RMCommand> keySet(){
		return _commandMap.keySet();
	}
	public boolean containsKey(RMCommand cmd){
		return _commandMap.containsKey(cmd);
	}
	public boolean containsValue(List<String> value){
		return _commandMap.containsValue(value);
	}
	public Set<Entry<RMCommand, List<String>>> entrySet(){
		return _commandMap.entrySet();
	}
	public List<String> getKey(RMCommand cmd){
		return _commandMap.get(cmd);
	}
	public int size(){
		return _commandMap.size();
	}
	public Collection<List<String>> values(){
		return _commandMap.values();
	}
	
	public HashMap<RMCommand, List<String>> getCommandMap(){
		return _commandMap;
	}
	
	public void setCommandMap(HashMap<RMCommand, List<String>> commandMap){
		_commandMap = commandMap;
	}
	
	//Clear
	public void clearCommandMap(){
		_commandMap.clear();
	}
		
	public void addCommand(RMCommand cmd, String alias){
		if(!_commandMap.containsKey(cmd)){
			List<String> aliasList = new ArrayList<String>();
			aliasList.add(alias);
			_commandMap.put(cmd, aliasList);
		}
		else{
			List<String> aliasList = _commandMap.get(cmd);
			if(!aliasList.contains(alias)) aliasList.add(alias);
		}
	}
	
	public void addCommand(RMCommand cmd, String... aliases){
		for(String alias : aliases){
			addCommand(cmd, alias);
		}
	}
	
	public void removeCommand(RMCommand cmd, String alias){
		if(_commandMap.containsKey(cmd)){
			List<String> aliasList = _commandMap.get(cmd);
			if(aliasList.contains(alias)) aliasList.remove(alias);
		}
	}
	
	public void clearCommand(RMCommand cmd){
		if(_commandMap.containsKey(cmd)) _commandMap.get(cmd).clear();
	}
}
