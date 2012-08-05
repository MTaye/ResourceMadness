package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.mtaye.ResourceMadness.setting.Setting;

public class Commands {
	
	public enum RMCommand {
		CREATE,
		REMOVE,
		LIST,
		COMMANDS,
		INFO,
		INFO_FOUND,
		INFO_FILTER,
		INFO_FILTER_STRING,
		INFO_REWARD,
		INFO_REWARD_STRING,
		INFO_TOOLS,
		INFO_TOOLS_STRING,
		SETTINGS,
		SETTINGS_RESET,
		SET,
		SET_MINPLAYERS,
		SET_MAXPLAYERS,
		SET_MINTEAMPLAYERS,
		SET_MAXTEAMPLAYERS,
		SET_TIMELIMIT,
		SET_SAFEZONE,
		SET_PLAYAREA,
		SET_PLAYAREATIME,
		SET_ENEMYRADAR,
		SET_KEEPONDEATH,
		SET_MULTIPLIER,
		SET_RANDOM,
		SET_PASSWORD,
		SET_ADVERTISE,
		SET_RESTORE,
		SET_ALLOWPVP,
		SET_DELAYPVP,
		SET_FRIENDLYFIRE,
		SET_HEALPLAYER,
		SET_TIMEOFDAY,
		SET_AUTORETURN,
		SET_MIDGAMEJOIN,
		SET_SHOWITEMSLEFT,
		SET_CLEARINVENTORY,
		SET_SCRAPFOUND,
		SET_FOUNDASREWARD,
		SET_KEEPOVERFLOW,
		SET_WARNUNEQUAL,
		SET_ALLOWUNEQUAL,
		SET_WARNHACKED,
		SET_ALLOWHACKED,
		SET_INFINITEREWARD,
		SET_INFINITETOOLS,
		SET_DIVIDEREWARD,
		SET_DIVIDETOOLS,
		MODE,
		MODE_FILTER,
		MODE_REWARD,
		MODE_TOOLS,
		FILTER,
		FILTER_RANDOM,
		FILTER_ADD,
		FILTER_SUBTRACT,
		FILTER_CLEAR,
		REWARD,
		REWARD_ADD,
		REWARD_SUBTRACT,
		REWARD_CLEAR,
		TOOLS,
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
		RETURN,
		STATS,
		TEAM,
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
		CLAIM_INFO,
		CLAIM_INFO_FOUND,
		CLAIM_INFO_ITEMS,
		CLAIM_INFO_REWARD,
		CLAIM_INFO_TOOLS,
		KICK,
		KICK_TEAM,
		KICK_ALL,
		BAN,
		BAN_TEAM,
		BAN_ALL,
		BAN_LIST,
		UNBAN,
		SAVE,
		UNDO
	}
	public List<String> commandList = new ArrayList<String>();
	public TreeMap<RMCommand, String> _commandMap = new TreeMap<RMCommand, String>();
	public TreeMap<RMCommand, List<String>> _aliasMap = new TreeMap<RMCommand, List<String>>();
	public List<String> _commandList = new ArrayList<String>();
	
	public Commands(){
		init();
	}
	
	public Commands(Commands commands){
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
		/*
		addAlias(RMCommand.FILTER, "f");
		addAlias(RMCommand.FILTER_ADD, "fa");
		addAlias(RMCommand.FILTER_CLEAR, "fc");
		addAlias(RMCommand.FILTER_INFO, "fi");
		addAlias(RMCommand.FILTER_INFO_STRING, "fis");
		addAlias(RMCommand.FILTER_SUBTRACT, "fs");
		addAlias(RMCommand.REWARD, "r");
		addAlias(RMCommand.REWARD_ADD, "ra");
		addAlias(RMCommand.REWARD_CLEAR, "rc");
		addAlias(RMCommand.REWARD_INFO, "ri");
		addAlias(RMCommand.REWARD_INFO_STRING, "ris");
		addAlias(RMCommand.REWARD_SUBTRACT, "rs");
		addAlias(RMCommand.TOOLS, "t");
		addAlias(RMCommand.TOOLS_ADD, "ta");
		addAlias(RMCommand.TOOLS_CLEAR, "tc");
		addAlias(RMCommand.TOOLS_INFO, "ti");
		addAlias(RMCommand.TOOLS_INFO_STRING, "tis");
		addAlias(RMCommand.TOOLS_SUBTRACT, "ts")
		*/;
		addAlias(RMCommand.SET_MINPLAYERS, "set min");
		addAlias(RMCommand.SET_MAXPLAYERS, "set max");
		addAlias(RMCommand.SET_MINTEAMPLAYERS, "set minteam");
		addAlias(RMCommand.SET_MAXTEAMPLAYERS, "set maxteam");
		addAlias(RMCommand.SET_TIMELIMIT, "set tl");
		addAlias(RMCommand.SET_SAFEZONE, "set sz");
		addAlias(RMCommand.SET_PLAYAREA, "set pa");
		addAlias(RMCommand.SET_PLAYAREATIME, "set pat");
		addAlias(RMCommand.SET_ENEMYRADAR, "set er");
		addAlias(RMCommand.SET_KEEPONDEATH, "set kod");
		addAlias(RMCommand.SET_MULTIPLIER, "set m");
		addAlias(RMCommand.SET_RANDOM, "set rand");
		addAlias(RMCommand.SET_PASSWORD, "set pass", "set p");
		addAlias(RMCommand.SET_ADVERTISE, "set a");
		addAlias(RMCommand.SET_RESTORE, "set r");
		addAlias(RMCommand.SET_ALLOWPVP, "set ap");
		addAlias(RMCommand.SET_DELAYPVP, "set dp");
		addAlias(RMCommand.SET_FRIENDLYFIRE, "set ff");
		addAlias(RMCommand.SET_AUTORETURN, "ar");
		addAlias(RMCommand.SET_HEALPLAYER, "set hp");
		addAlias(RMCommand.SET_AUTORETURN, "set ar");
		addAlias(RMCommand.SET_MIDGAMEJOIN, "set mj", "set mgj");
		addAlias(RMCommand.SET_SHOWITEMSLEFT, "set sil");
		addAlias(RMCommand.SET_CLEARINVENTORY, "set ci");
		addAlias(RMCommand.SET_SCRAPFOUND, "set sf");
		addAlias(RMCommand.SET_FOUNDASREWARD, "set far");
		addAlias(RMCommand.SET_KEEPOVERFLOW, "set ko");
		addAlias(RMCommand.SET_WARNUNEQUAL, "set wu");
		addAlias(RMCommand.SET_ALLOWUNEQUAL, "set au");
		addAlias(RMCommand.SET_WARNHACKED, "set wh");
		addAlias(RMCommand.SET_ALLOWHACKED, "set ah");
		addAlias(RMCommand.SET_INFINITEREWARD, "set ir");
		addAlias(RMCommand.SET_INFINITETOOLS, "set it");
		addAlias(RMCommand.SET_DIVIDEREWARD, "set dr");
		addAlias(RMCommand.SET_DIVIDETOOLS, "set dt");
		addAlias(RMCommand.SET_PASSWORD, "set pass");
		addAlias(RMCommand.CHAT_WORLD, "w");
		addAlias(RMCommand.CHAT_GAME, "g");
		addAlias(RMCommand.CHAT_TEAM, "t");
		addAlias(RMCommand.ITEMS, "i");
	}
	
	public void clear(){
		_commandMap.clear();
		init();
		//initCommands();
	}
	
	public TreeMap<RMCommand, String> getCommandMap(){
		return _commandMap;
	}
	public void setCommandMap(TreeMap<RMCommand, String> commandMap){
		_commandMap = commandMap;
	}
	public TreeMap<RMCommand, List<String>> getAliasMap(){
		return _aliasMap;
	}
	public void setAliasMap(TreeMap<RMCommand, List<String>> aliasMap){
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
		commandList.add(Text.getLabel("cmd.create"));
		commandList.add(Text.getLabel("cmd.remove"));
		commandList.add(Text.getLabel("cmd.list"));
		commandList.add(Text.getLabel("cmd.commands"));
		commandList.add(Text.getLabel("cmd.info"));
		commandList.add(Text.getLabel("cmd.info")+" "+Text.getLabel("cmd.info.found"));
		commandList.add(Text.getLabel("cmd.info")+" "+Text.getLabel("cmd.info.filter"));
		commandList.add(Text.getLabel("cmd.info")+" "+Text.getLabel("cmd.info.filter")+" "+Text.getLabel("cmd.info.x.string"));
		commandList.add(Text.getLabel("cmd.info")+" "+Text.getLabel("cmd.info.reward"));
		commandList.add(Text.getLabel("cmd.info")+" "+Text.getLabel("cmd.info.reward")+" "+Text.getLabel("cmd.info.x.string"));
		commandList.add(Text.getLabel("cmd.info")+" "+Text.getLabel("cmd.info.tools"));
		commandList.add(Text.getLabel("cmd.info")+" "+Text.getLabel("cmd.info.tools")+" "+Text.getLabel("cmd.info.x.string"));
		commandList.add(Text.getLabel("cmd.settings"));
		commandList.add(Text.getLabel("cmd.settings")+" "+Text.getLabel("cmd.settings.reset"));
		commandList.add(Text.getLabel("cmd.set"));
		
		for(Setting setting : Setting.values()){
			commandList.add(Text.getLabel("cmd.set")+" "+Text.getLabel("cmd.set."+setting.name()));
		}
		
		commandList.add(Text.getLabel("cmd.mode"));
		commandList.add(Text.getLabel("cmd.mode")+" "+Text.getLabel("cmd.mode.filter"));
		commandList.add(Text.getLabel("cmd.mode")+" "+Text.getLabel("cmd.mode.reward"));
		commandList.add(Text.getLabel("cmd.mode")+" "+Text.getLabel("cmd.mode.tools"));
		commandList.add(Text.getLabel("cmd.filter"));
		commandList.add(Text.getLabel("cmd.filter")+" "+Text.getLabel("cmd.filter.random"));
		commandList.add(Text.getLabel("cmd.filter")+" "+Text.getLabel("cmd.filter.add"));
		commandList.add(Text.getLabel("cmd.filter")+" "+Text.getLabel("cmd.filter.subtract"));
		commandList.add(Text.getLabel("cmd.filter")+" "+Text.getLabel("cmd.filter.clear"));
		commandList.add(Text.getLabel("cmd.reward"));
		commandList.add(Text.getLabel("cmd.reward")+" "+Text.getLabel("cmd.filter.add"));
		commandList.add(Text.getLabel("cmd.reward")+" "+Text.getLabel("cmd.filter.subtract"));
		commandList.add(Text.getLabel("cmd.reward")+" "+Text.getLabel("cmd.filter.clear"));
		commandList.add(Text.getLabel("cmd.tools"));
		commandList.add(Text.getLabel("cmd.tools")+" "+Text.getLabel("cmd.filter.add"));
		commandList.add(Text.getLabel("cmd.tools")+" "+Text.getLabel("cmd.filter.subtract"));
		commandList.add(Text.getLabel("cmd.tools")+" "+Text.getLabel("cmd.filter.clear"));
		commandList.add(Text.getLabel("cmd.template"));
		commandList.add(Text.getLabel("cmd.template")+" "+Text.getLabel("cmd.template.list"));
		commandList.add(Text.getLabel("cmd.template")+" "+Text.getLabel("cmd.template.load"));
		commandList.add(Text.getLabel("cmd.template")+" "+Text.getLabel("cmd.template.save"));
		commandList.add(Text.getLabel("cmd.template")+" "+Text.getLabel("cmd.template.remove"));
		commandList.add(Text.getLabel("cmd.start"));
		commandList.add(Text.getLabel("cmd.stop"));
		commandList.add(Text.getLabel("cmd.pause"));
		commandList.add(Text.getLabel("cmd.resume"));
		commandList.add(Text.getLabel("cmd.join"));
		commandList.add(Text.getLabel("cmd.quit"));
		commandList.add(Text.getLabel("cmd.ready"));
		commandList.add(Text.getLabel("cmd.return"));
		commandList.add(Text.getLabel("cmd.stats"));
		commandList.add(Text.getLabel("cmd.team"));
		commandList.add(Text.getLabel("cmd.time"));
		commandList.add(Text.getLabel("cmd.items"));
		commandList.add(Text.getLabel("cmd.item"));
		commandList.add(Text.getLabel("cmd.restore"));
		commandList.add(Text.getLabel("cmd.chat"));
		commandList.add(Text.getLabel("cmd.chat")+" "+Text.getLabel("cmd.chat.world"));
		commandList.add(Text.getLabel("cmd.chat")+" "+Text.getLabel("cmd.chat.game"));
		commandList.add(Text.getLabel("cmd.chat")+" "+Text.getLabel("cmd.chat.team"));
		commandList.add(Text.getLabel("cmd.claim"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.found"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.found")+" "+Text.getLabel("cmd.claim.x.chest"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.items"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.items")+" "+Text.getLabel("cmd.claim.x.chest"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.reward"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.reward")+" "+Text.getLabel("cmd.claim.x.chest"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.tools"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.tools")+" "+Text.getLabel("cmd.claim.x.chest"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.info"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.info")+" "+Text.getLabel("cmd.claim.found"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.info")+" "+Text.getLabel("cmd.claim.items"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.info")+" "+Text.getLabel("cmd.claim.reward"));
		commandList.add(Text.getLabel("cmd.claim")+" "+Text.getLabel("cmd.claim.info")+" "+Text.getLabel("cmd.claim.tools"));
		commandList.add(Text.getLabel("cmd.kick"));
		commandList.add(Text.getLabel("cmd.kick")+" "+Text.getLabel("cmd.kick.team"));
		commandList.add(Text.getLabel("cmd.kick")+" "+Text.getLabel("cmd.kick.all"));
		commandList.add(Text.getLabel("cmd.ban"));
		commandList.add(Text.getLabel("cmd.ban")+" "+Text.getLabel("cmd.ban.team"));
		commandList.add(Text.getLabel("cmd.ban")+" "+Text.getLabel("cmd.ban.all"));
		commandList.add(Text.getLabel("cmd.ban")+" "+Text.getLabel("cmd.ban.list"));
		commandList.add(Text.getLabel("cmd.unban"));
		commandList.add(Text.getLabel("cmd.save"));
		commandList.add(Text.getLabel("cmd.undo"));
	}
}