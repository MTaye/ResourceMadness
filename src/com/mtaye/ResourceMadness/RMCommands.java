package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		SET_SAFEZONE,
		SET_TIMELIMIT,
		SET_RANDOM,
		SET_PASSWORD,
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
		TIME,
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
		KICK,
		KICK_TEAM,
		KICK_ALL,
		BAN,
		BAN_TEAM,
		BAN_ALL,
		BAN_LIST,
		UNBAN,
		SAVE
	}
	public List<String> commandList = new ArrayList<String>();
	public HashMap<RMCommand, String> _commandMap = new HashMap<RMCommand, String>();
	public HashMap<RMCommand, List<String>> _aliasMap = new HashMap<RMCommand, List<String>>();
	public List<String> _commandList = new ArrayList<String>();
	
	public RMCommands(){
		init();
	}
	
	public RMCommands(RMCommands commands){
		setCommandMap(commands.getCommandMap());
	}
	
	public void init(){
		initCommandList();
		RMCommand[] commands = RMCommand.values();
		for(int i=0; i<commands.length; i++){
			RMCommand cmd = commands[i];
			addCommand(cmd, commandList.get(i));
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
		addAlias(RMCommand.SET_PASSWORD, "set pass");
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
	
	public void initCommandList(){
		commandList.clear();
		commandList.add(RMText.getLabel("cmd.add"));
		commandList.add(RMText.getLabel("cmd.remove"));
		commandList.add(RMText.getLabel("cmd.list"));
		commandList.add(RMText.getLabel("cmd.commands"));
		commandList.add(RMText.getLabel("cmd.info"));
		commandList.add(RMText.getLabel("cmd.info")+" "+RMText.getLabel("cmd.info.found"));
		commandList.add(RMText.getLabel("cmd.info")+" "+RMText.getLabel("cmd.info.claim"));
		commandList.add(RMText.getLabel("cmd.info")+" "+RMText.getLabel("cmd.info.items"));
		commandList.add(RMText.getLabel("cmd.info")+" "+RMText.getLabel("cmd.info.reward"));
		commandList.add(RMText.getLabel("cmd.info")+" "+RMText.getLabel("cmd.info.tools"));
		commandList.add(RMText.getLabel("cmd.settings"));
		commandList.add(RMText.getLabel("cmd.settings")+" "+RMText.getLabel("cmd.settings.reset"));
		commandList.add(RMText.getLabel("cmd.set"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.minplayers"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.maxplayers"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.minteamplayers"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.maxteamplayers"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.safezone"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.timelimit"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.random"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.password"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.advertise"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.restore"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.warp"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.midgamejoin"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.healplayer"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.clearinventory"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.foundasreward"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.warnunequal"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.allowunequal"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.warnhacked"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.allowhacked"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.infinitereward"));
		commandList.add(RMText.getLabel("cmd.set")+" "+RMText.getLabel("cmd.set.infinitetools"));
		commandList.add(RMText.getLabel("cmd.mode"));
		commandList.add(RMText.getLabel("cmd.mode")+" "+RMText.getLabel("cmd.mode.filter"));
		commandList.add(RMText.getLabel("cmd.mode")+" "+RMText.getLabel("cmd.mode.reward"));
		commandList.add(RMText.getLabel("cmd.mode")+" "+RMText.getLabel("cmd.mode.tools"));
		commandList.add(RMText.getLabel("cmd.filter"));
		commandList.add(RMText.getLabel("cmd.filter")+" "+RMText.getLabel("cmd.filter.info"));
		commandList.add(RMText.getLabel("cmd.filter")+" "+RMText.getLabel("cmd.filter.info")+" "+RMText.getLabel("cmd.filter.info.string"));
		commandList.add(RMText.getLabel("cmd.filter")+" "+RMText.getLabel("cmd.filter.random"));
		commandList.add(RMText.getLabel("cmd.filter")+" "+RMText.getLabel("cmd.filter.add"));
		commandList.add(RMText.getLabel("cmd.filter")+" "+RMText.getLabel("cmd.filter.subtract"));
		commandList.add(RMText.getLabel("cmd.filter")+" "+RMText.getLabel("cmd.filter.clear"));
		commandList.add(RMText.getLabel("cmd.reward"));
		commandList.add(RMText.getLabel("cmd.reward")+" "+RMText.getLabel("cmd.filter.info"));
		commandList.add(RMText.getLabel("cmd.reward")+" "+RMText.getLabel("cmd.filter.info")+" "+RMText.getLabel("cmd.filter.info.string"));
		commandList.add(RMText.getLabel("cmd.reward")+" "+RMText.getLabel("cmd.filter.add"));
		commandList.add(RMText.getLabel("cmd.reward")+" "+RMText.getLabel("cmd.filter.subtract"));
		commandList.add(RMText.getLabel("cmd.reward")+" "+RMText.getLabel("cmd.filter.clear"));
		commandList.add(RMText.getLabel("cmd.tools"));
		commandList.add(RMText.getLabel("cmd.tools")+" "+RMText.getLabel("cmd.filter.info"));
		commandList.add(RMText.getLabel("cmd.tools")+" "+RMText.getLabel("cmd.filter.info")+" "+RMText.getLabel("cmd.filter.info.string"));
		commandList.add(RMText.getLabel("cmd.tools")+" "+RMText.getLabel("cmd.filter.add"));
		commandList.add(RMText.getLabel("cmd.tools")+" "+RMText.getLabel("cmd.filter.subtract"));
		commandList.add(RMText.getLabel("cmd.tools")+" "+RMText.getLabel("cmd.filter.clear"));
		commandList.add(RMText.getLabel("cmd.template"));
		commandList.add(RMText.getLabel("cmd.template")+" "+RMText.getLabel("cmd.template.list"));
		commandList.add(RMText.getLabel("cmd.template")+" "+RMText.getLabel("cmd.template.load"));
		commandList.add(RMText.getLabel("cmd.template")+" "+RMText.getLabel("cmd.template.save"));
		commandList.add(RMText.getLabel("cmd.template")+" "+RMText.getLabel("cmd.template.remove"));
		commandList.add(RMText.getLabel("cmd.start"));
		commandList.add(RMText.getLabel("cmd.stop"));
		commandList.add(RMText.getLabel("cmd.pause"));
		commandList.add(RMText.getLabel("cmd.resume"));
		commandList.add(RMText.getLabel("cmd.join"));
		commandList.add(RMText.getLabel("cmd.quit"));
		commandList.add(RMText.getLabel("cmd.ready"));
		commandList.add(RMText.getLabel("cmd.time"));
		commandList.add(RMText.getLabel("cmd.items"));
		commandList.add(RMText.getLabel("cmd.item"));
		commandList.add(RMText.getLabel("cmd.restore"));
		commandList.add(RMText.getLabel("cmd.chat"));
		commandList.add(RMText.getLabel("cmd.chat")+" "+RMText.getLabel("cmd.chat.world"));
		commandList.add(RMText.getLabel("cmd.chat")+" "+RMText.getLabel("cmd.chat.game"));
		commandList.add(RMText.getLabel("cmd.chat")+" "+RMText.getLabel("cmd.chat.team"));
		commandList.add(RMText.getLabel("cmd.claim"));
		commandList.add(RMText.getLabel("cmd.claim")+" "+RMText.getLabel("cmd.claim.found"));
		commandList.add(RMText.getLabel("cmd.claim")+" "+RMText.getLabel("cmd.claim.found")+" "+RMText.getLabel("cmd.claim.x.chest"));
		commandList.add(RMText.getLabel("cmd.claim")+" "+RMText.getLabel("cmd.claim.items"));
		commandList.add(RMText.getLabel("cmd.claim")+" "+RMText.getLabel("cmd.claim.items")+" "+RMText.getLabel("cmd.claim.x.chest"));
		commandList.add(RMText.getLabel("cmd.claim")+" "+RMText.getLabel("cmd.claim.reward"));
		commandList.add(RMText.getLabel("cmd.claim")+" "+RMText.getLabel("cmd.claim.reward")+" "+RMText.getLabel("cmd.claim.x.chest"));
		commandList.add(RMText.getLabel("cmd.claim")+" "+RMText.getLabel("cmd.claim.tools"));
		commandList.add(RMText.getLabel("cmd.claim")+" "+RMText.getLabel("cmd.claim.tools")+" "+RMText.getLabel("cmd.claim.x.chest"));
		commandList.add(RMText.getLabel("cmd.kick"));
		commandList.add(RMText.getLabel("cmd.kick")+" "+RMText.getLabel("cmd.kick.team"));
		commandList.add(RMText.getLabel("cmd.kick")+" "+RMText.getLabel("cmd.kick.all"));
		commandList.add(RMText.getLabel("cmd.ban"));
		commandList.add(RMText.getLabel("cmd.ban")+" "+RMText.getLabel("cmd.ban.team"));
		commandList.add(RMText.getLabel("cmd.ban")+" "+RMText.getLabel("cmd.ban.all"));
		commandList.add(RMText.getLabel("cmd.ban")+" "+RMText.getLabel("cmd.ban.list"));
		commandList.add(RMText.getLabel("cmd.unban"));
		commandList.add(RMText.getLabel("cmd.save"));
	}
}