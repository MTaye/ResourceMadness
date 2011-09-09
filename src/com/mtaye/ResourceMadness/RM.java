package com.mtaye.ResourceMadness;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.bukkit.ChatColor;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.*;

import org.bukkit.util.config.Configuration;

import com.iConomy.*;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;

import com.mtaye.ResourceMadness.RMConfig.Lock;
import com.mtaye.ResourceMadness.RMConfig.PermissionType;
import com.mtaye.ResourceMadness.RMGame.FilterState;
import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.ForceState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;
import com.mtaye.ResourceMadness.Helper.RMHelper;
import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;
import com.mtaye.ResourceMadness.Helper.RMLogHelper;
import com.mtaye.ResourceMadness.Plugin.iConomyServer;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import com.ning.compress.lzf.LZFInputStream;
import com.ning.compress.lzf.LZFOutputStream;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RM extends JavaPlugin {
	private PluginDescriptionFile pdfFile;
	public Logger log;
	//public PermissionHandler Permissions = null;
	@SuppressWarnings("unused")
	
	private String ver = "0.1";
	
	public HashMap<Player, Boolean> players = new HashMap<Player, Boolean>();
	public RMConfig config = new RMConfig();

	private RMBlockListener blockListener = new RMBlockListener(this);
	private RMPlayerListener playerListener = new RMPlayerListener(this);
	private RMEntityListener entityListener = new RMEntityListener(this);
	//private RMLogListener logListener = new RMLogListener(this);
	
	public static enum ClaimType { ITEMS, FOUND, REWARD, TOOLS, CHEST, NONE };
	public static enum DataType { CONFIG, STATS, PLAYER, GAME, LOG, TEMPLATE, YAML_CONFIG, YAML_STATS, YAML_PLAYER, YAML_GAME };
	public static enum SaveType { MINI, YAML }; 
	
	private RMWatcher watcher;
	private int watcherid;
	//private RMInventoryListener inventoryListener = new RMPlayerListener(this);
	
	public PermissionHandler permissions = null;
	public PermissionManager permissionsEx = null;
	public iConomy iConomy = null;
	
	RMLogHelper rmLogHelper;
	
	public RM(){
		RMPlayer.plugin = this;
		RMGame.plugin = this;
		RMText.plugin = this;
		RMDebug.plugin = this;
	}
	
	@Override
	public void onEnable(){
		log = getServer().getLogger();
        RMDebug.enable();

        //setupPermissions();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.PLUGIN_ENABLE, new iConomyServer(this), Priority.Monitor, this);
		pm.registerEvent(Type.PLUGIN_DISABLE, new iConomyServer(this), Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BURN, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PISTON_EXTEND, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PISTON_RETRACT, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
		//pm.registerEvent(Type.CUSTOM_EVENT, logListener, Priority.Normal, this);

		pdfFile = this.getDescription();
		log.log(Level.INFO, pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled!" );
		//RMConfig.load();
		
		rmLogHelper = new RMLogHelper(this);
		loadAll();
		/*
		log.log(Level.INFO, "Autosave:"+config.getAutoSave());
		log.log(Level.INFO, "PermissionType:"+config.getPermissionType().name());
		log.log(Level.INFO, "UseRestore:"+config.getUseRestore());
		log.log(Level.INFO, "MaxGames:"+config.getMaxGames());
		log.log(Level.INFO, "MaxGamesPerPlayer:"+config.getMaxGamesPerPlayer());
		log.log(Level.INFO, "MaxPlayersPerGame:"+config.getMaxPlayersPerGame());
		log.log(Level.INFO, "MaxPlayersPerTeam:"+config.getMaxPlayersPerTeam());
		log.log(Level.INFO, "Restore:"+config.getRestore());
		log.log(Level.INFO, "WarpToSafety:"+config.getWarpToSafety());
		log.log(Level.INFO, "WarnHackedItems:"+config.getWarnHackedItems());
		log.log(Level.INFO, "AllowHackedItems:"+config.getAllowHackedItems());
		log.log(Level.INFO, "KeepIngame:"+config.getKeepIngame());
		log.log(Level.INFO, "AllowMidgameJoin:"+config.getAllowMidgameJoin());
		log.log(Level.INFO, "ClearPlayerInventory:"+config.getClearPlayerInventory());
		*/
		setupPermissions();
		watcher = new RMWatcher(this);
		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 20,20);
		
	}
	
	public void onDisable(){
		saveAll();
		getServer().getScheduler().cancelTask(watcherid);
		log.info(pdfFile.getName() + " disabled");
		//RMConfig.save();
	}
	
	public RMConfig getConfig(){
		return config;
	}
	
	public void setConfig(RMConfig config){
		this.config = config;
	}
	
	public void setupPermissions(){
		switch(config.getPermissionType()){
			case P3:
				try{
					Plugin permissionPlugin = getServer().getPluginManager().getPlugin("Permissions");
					if(this.permissions == null){
						try{
							this.permissions = ((Permissions)permissionPlugin).getHandler();
							log.log(Level.INFO, RMText.preLog+"Found Permissions 3");
						}
						catch (Exception e){
							this.permissions = null;
							log.log(Level.WARNING, RMText.preLog+"Permissions plugin is not enabled!");
						}
					}
				}
				catch (java.lang.NoClassDefFoundError e){
					this.permissions = null;
					log.log(Level.WARNING, RMText.preLog+"Permissions plugin not found!");
				}
				break;
			case PEX:
				try{
					if(getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
					    PermissionManager permissionsEx = PermissionsEx.getPermissionManager();
					    if(permissionsEx==null) log.log(Level.WARNING, "PermissionsEx plugin is not enabled!");
					    log.log(Level.INFO, RMText.preLog+"Found PermissionsEx");
					}
					else log.log(Level.WARNING, RMText.preLog+"PermissionsEx plugin not found.");
				}
				catch (Exception e){
					this.permissions = null;
					log.log(Level.WARNING, RMText.preLog+"PermissionsEx plugin not found!");
				}
				break;
			case FALSE: default:
				log.log(Level.INFO, RMText.preLog+"Running without permissions...");
				break;
		}
		if((permissions == null)&&(permissionsEx == null)) config.setPermissionType(PermissionType.FALSE);
	}
	
	public boolean isPermissionEnabled(){
		switch(config.getPermissionType()){
			case P3: case PEX: return true;
			case FALSE: default: return false;
		}
	}
	
	public boolean hasPermission(Player player, String node){
		if((permissions==null)&&(permissionsEx==null)) return true;
		if(player==null) return false;
		else{
			switch(config.getPermissionType()){
				case BUKKIT:
					if(node=="resourcemadness.admin.overlord") return player.hasPermission("resourcemadness.admin.overlord");
					else{
						if((player.hasPermission("resourcemadness.admin"))||(player.hasPermission("*"))) return true;
						else return player.hasPermission(node);
					}
				case P3:
					if(node=="resourcemadness.admin.overlord") return permissions.has(player, "resourcemadness.admin.overlord");
					else{
						if((permissions.has(player, "resourcemadness.admin"))||(permissions.has(player, "*"))) return true;
						else return permissions.has(player, node);
					}
				case PEX:
					if(node=="resourcemadness.admin.overlord") return permissions.has(player, "resourcemadness.admin.overlord");
					else{
						if((permissionsEx.has(player, "resourcemadness.admin"))||(permissionsEx.has(player, "*"))) return true;
						else return permissionsEx.has(player, node);
					}
				case FALSE: default: return true;
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
		Player p = null;
		if(sender.getClass().getName().contains("Player")){
			p = (Player)sender;
			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(cmd.getName().equals("resourcemadness")){
					if(!rmp.hasPermission("resourcemadness")) return rmp.sendMessage(RMText.noPermissionCommand);
					if(args.length==0){
						rmInfo(rmp, 1);
					}
					else{
						RMGame rmGame = null;
						int page = RMHelper.getIntByString(args[0]);
						//String[] argsItems = args.clone();
						if(args.length>1){
							int gameid = RMHelper.getIntByString(args[0]);
							if(gameid!=-1){
								rmGame = RMGame.getGameById(gameid);
								if(rmGame!=null){
									List<String> argsList = Arrays.asList(args);
									argsList = argsList.subList(1, argsList.size());
									args = argsList.toArray(new String[argsList.size()]);
								}
							}
						}
						//ADD
						if(args[0].equalsIgnoreCase("add")){
							if(!rmp.hasPermission("resourcemadness.add")) return rmp.sendMessage(RMText.noPermissionCommand);
							rmp.setPlayerAction(PlayerAction.ADD);
							rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"add "+ChatColor.WHITE+"a new game.");
							return true;
						}
						//REMOVE
						else if(args[0].equalsIgnoreCase("remove")){
							if(!rmp.hasPermission("resourcemadness.remove")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) RMGame.tryRemoveGame(rmGame, rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.REMOVE);
								rmp.sendMessage("Left click a game block to "+ChatColor.GRAY+"remove "+ChatColor.WHITE+"a game.");
							}
							return true;
						}
						//LIST
						else if(args[0].equalsIgnoreCase("list")){
							if(!rmp.hasPermission("resourcemadness.list")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length==2) sendListById(args[1], rmp);
							else sendListByInt(0, rmp);
							return true;
						}
						/*
						//SEARCH
						else if(args[0].equalsIgnoreCase("find")){
							if(!rmp.hasPermission("resourcemadness.search")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length==2) findGamesByOwner(args[1], rmp);
							else sendListByInt(0, rmp);
							return true;
						}
						*/
						//INFO
						else if(args[0].equalsIgnoreCase("info")){
							if(!rmp.hasPermission("resourcemadness.info")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length==2){
								if(args[1].equalsIgnoreCase("found")){
									if(!rmp.hasPermission("resourcemadness.info.found")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(rmGame!=null) rmGame.getInfoFound(rmp);
									else{
										rmp.setPlayerAction(PlayerAction.INFO_FOUND);
										rmp.sendMessage("Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about it's "+ChatColor.YELLOW+"found items"+ChatColor.WHITE+".");
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase("items")){
									if(!rmp.hasPermission("resourcemadness.info.items")) return rmp.sendMessage(RMText.noPermissionCommand);
									rmp.getInfoItems();
									return true;
								}
								else if(args[1].equalsIgnoreCase("reward")){
									if(!rmp.hasPermission("resourcemadness.info.reward")) return rmp.sendMessage(RMText.noPermissionCommand);
									rmp.getInfoReward();
									return true;
								}
								else if(args[1].equalsIgnoreCase("tools")){
									if(!rmp.hasPermission("resourcemadness.info.tools")) return rmp.sendMessage(RMText.noPermissionCommand);
									rmp.getInfoTools();
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.sendInfo(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.INFO);
									rmp.sendMessage("Left click a game block to get "+ChatColor.YELLOW+"info"+ChatColor.WHITE+".");
								}
								return true;
							}
						}
						//SETTINGS
						else if(args[0].equalsIgnoreCase("settings")){
							if(!rmp.hasPermission("resourcemadness.settings")) return rmp.sendMessage(RMText.noPermissionCommand);
							page = 0;
							if(args.length>1) page = RMHelper.getIntByString(args[1]);
							if(rmGame!=null) rmGame.sendSettings(rmp, page);
							else{
								rmp.setRequestInt(page);
								rmp.setPlayerAction(PlayerAction.SETTINGS);
								rmp.sendMessage("Left click a game block to get "+ChatColor.WHITE+"settings"+ChatColor.WHITE+".");
							}
							return true;
						}
						//MODE
						else if(args[0].equalsIgnoreCase("mode")){
							if(!rmp.hasPermission("resourcemadness.mode")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length==2){
								if(args[1].equalsIgnoreCase("filter")){
									if(!rmp.hasPermission("resourcemadness.mode.filter")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.FILTER, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.FILTER);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage("Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"filter"+ChatColor.WHITE+".");
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase("reward")){
									if(!rmp.hasPermission("resourcemadness.mode.reward")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.REWARD, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.REWARD);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage("Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"reward"+ChatColor.WHITE+".");
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase("tools")){
									if(!rmp.hasPermission("resourcemadness.mode.tools")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.TOOLS, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.TOOLS);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage("Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"tools"+ChatColor.WHITE+".");
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.cycleMode(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.MODE_CYCLE);
									rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"cycle "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"interface mode"+ChatColor.WHITE+".");
								}
								return true;
							}
						}
						/*
						//SAVE
						else if(args[0].equalsIgnoreCase("save")){
							saveData();
							return true;
							if(rmGame!=null){
								rmGame.saveConfig();
								return true;
							}
							else{
								rmp.setPlayerAction(PlayerAction.SAVE_CONFIG);
								rmp.sendMessage("Left click a game block to save your game game.");
								return true;
							}
						}
						//LOAD
						else if(args[0].equalsIgnoreCase("load")){
							loadData();
							return true;
						}
						*/
						//JOIN
						else if(args[0].equalsIgnoreCase("join")){
							if(!rmp.hasPermission("resourcemadness.join")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length==2){
								if(rmGame!=null){								
									RMTeam rmTeam = RMGame.getTeamById(args[1], rmGame);
									if(rmTeam!=null){
										rmGame.joinTeam(rmTeam, rmp);
										return true;
									}
									rmTeam = rmGame.getTeamByDye(args[1], rmGame);
									if(rmTeam!=null){
										rmGame.joinTeam(rmTeam, rmp);
										return true;
									}
									rmp.sendMessage("This team does not exist!");
									return true;
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.JOIN);
								rmp.sendMessage("Left click a team block to "+ChatColor.YELLOW+"join "+ChatColor.WHITE+"the team.");
								return true;
							}
						}
						//QUIT
						else if(args[0].equalsIgnoreCase("quit")){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(RMText.noPermissionCommand);
							for(RMTeam rmTeam : RMTeam.getTeams()){
								RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
								if(rmPlayer!=null){
									RMGame game = rmTeam.getGame();
									switch(game.getConfig().getState()){
									case SETUP: case GAMEPLAY: case PAUSED:
										rmTeam.removePlayer(rmPlayer);
										break;
									}
									return true;
								}
							}
							rmp.sendMessage("You did not "+ChatColor.YELLOW+"join "+ChatColor.WHITE+"any "+ChatColor.YELLOW+"team "+ChatColor.WHITE+"yet.");
							return true;
							
						}
						//READY
						else if(args[0].equalsIgnoreCase("ready")){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(RMText.noPermissionCommand);
							for(RMTeam rmTeam : RMTeam.getTeams()){
								RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
								if(rmPlayer!=null){
									RMGame game = rmTeam.getGame();
									if(game!=null){
										if(game.getConfig().getState()==GameState.SETUP){
											rmTeam.getGame().toggleReady(rmp);
										}
										else rmp.sendMessage(ChatColor.GRAY+"You cannot ready yourself while in-game.");
										return true;
									}
								}
							}
							rmp.sendMessage("You did not "+ChatColor.YELLOW+"join "+ChatColor.WHITE+"any "+ChatColor.YELLOW+"team "+ChatColor.WHITE+"yet.");
							return true;
							
						}
						//START
						else if(args[0].equalsIgnoreCase("start")){
							if(!rmp.hasPermission("resourcemadness.start")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length==2){
								int amount = RMHelper.getIntByString(args[1]);
								if(amount!=-1){
									if(!rmp.hasPermission("resourcemadness.start.random")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(rmGame!=null){
										rmGame.setRandomizeAmount(rmp, amount);
										rmGame.startGame(rmp);
									}
									else{
										rmp.setRequestInt(amount);
										rmp.setPlayerAction(PlayerAction.START_RANDOM);
										rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"start "+ChatColor.WHITE+"the game with "+ChatColor.GREEN+amount+"random item(s)"+ChatColor.WHITE+".");
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.startGame(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.START);
									rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"start "+ChatColor.WHITE+"the game.");
								}
								return true;
							}
						}
						/*
						//RESTART
						else if(args[0].equalsIgnoreCase("restart")){
							if(!rmp.hasPermission("resourcemadness.restart")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) rmGame.restartGame(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTART);
								rmp.sendMessage("Left click a game block to "+ChatColor.GOLD+"restart "+ChatColor.WHITE+"the game.");
							}
							return true;
						}
						*/
						//STOP
						else if(args[0].equalsIgnoreCase("stop")){
							if(!rmp.hasPermission("resourcemadness.stop")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) rmGame.stopGame(rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.STOP);
								rmp.sendMessage("Left click a game block to "+ChatColor.RED+"stop "+ChatColor.WHITE+"the game.");
							}
							return true;
						}
						//PAUSE
						else if(args[0].equalsIgnoreCase("pause")){
							if(!rmp.hasPermission("resourcemadness.pause")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) rmGame.pauseGame(rmp);
							else{
								rmp.setRequestBool(true);
								rmp.setPlayerAction(PlayerAction.PAUSE);
								rmp.sendMessage("Left click a game block to "+ChatColor.RED+"pause "+ChatColor.WHITE+"the game.");
							}
							return true;
						}
						//RESUME
						else if(args[0].equalsIgnoreCase("resume")){
							if(!rmp.hasPermission("resourcemadness.pause")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) rmGame.resumeGame(rmp);
							else{
								rmp.setRequestBool(false);
								rmp.setPlayerAction(PlayerAction.RESUME);
								rmp.sendMessage("Left click a game block to "+ChatColor.GREEN+"resume "+ChatColor.WHITE+"the game.");
							}
							return true;
						}
						//RESTORE WORLD
						else if(args[0].equalsIgnoreCase("restore")){
							if(!rmp.hasPermission("resourcemadness.restore")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) rmGame.restoreWorld(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTORE);
								rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"restore world changes "+ChatColor.WHITE+".");
							}
							return true;
						}
						//ITEMS
						else if(args[0].equalsIgnoreCase("items")){
							if(!rmp.hasPermission("resourcemadness.items")) return rmp.sendMessage(RMText.noPermissionCommand);
							RMTeam rmTeam = rmp.getTeam();
							if(rmTeam!=null){
								RMGame rmg = rmTeam.getGame(); 
								if(rmg!=null){
									if(rmg.getConfig().getState()==GameState.GAMEPLAY){
										rmg.updateGameplayInfo(rmp, rmTeam);
										return true;
									}
								}
							}
							rmp.sendMessage("You must be in a game to use this command.");
							return false;
						}
						//FILTER
						FilterState filterState = null;
						if(args[0].equalsIgnoreCase("filter")){
							if(!rmp.hasPermission("resourcemadness.filter")) return rmp.sendMessage(RMText.noPermissionCommand);
							filterState = FilterState.FILTER;
						}
						else if(args[0].equalsIgnoreCase("reward")){
							if(!rmp.hasPermission("resourcemadness.reward")) return rmp.sendMessage(RMText.noPermissionCommand);
							filterState = FilterState.REWARD;
						}
						else if(args[0].equalsIgnoreCase("tools")){
							if(!rmp.hasPermission("resourcemadness.tools")) return rmp.sendMessage(RMText.noPermissionCommand);
							filterState = FilterState.TOOLS;
						}
						if(filterState!=null){
							if(args.length>1){
								List<String> listArgs = new ArrayList<String>();
								for(int i=1; i<args.length; i++){
									listArgs.add(args[i]);
								}
								if(listArgs.size()>0){
									if(rmGame!=null){
										parseFilter(rmp, listArgs, filterState);
										rmGame.tryParseFilter(null, rmp);
										return true;
									}
									else{
										parseFilter(rmp, listArgs, filterState);
										switch(filterState){
											case FILTER:
												rmp.setPlayerAction(PlayerAction.FILTER);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"filter"+ChatColor.WHITE+".");
												break;
											case REWARD:
												rmp.setPlayerAction(PlayerAction.REWARD);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"reward"+ChatColor.WHITE+".");
												break;
											case TOOLS:
												rmp.setPlayerAction(PlayerAction.TOOLS);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"tools"+ChatColor.WHITE+".");
												break;
										}
										return true;
									}
								}
							}
							switch(filterState){
								case FILTER: rmFilterInfo(rmp); break;
								case REWARD: rmRewardInfo(rmp); break;
								case TOOLS: rmToolsInfo(rmp); break;
							}
							return true;
						}
						else if(args[0].equalsIgnoreCase("money")){
							
						}
						//CLAIM
						else if(args[0].equalsIgnoreCase("claim")){
							if(!rmp.hasPermission("resourcemadness.claim")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length>1){
								if(args[1].equalsIgnoreCase("found")){
									if(!rmp.hasPermission("resourcemadness.claim.found")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase("chest")){
												if(!rmp.hasPermission("resourcemadness.claim.found.chest")) return rmp.sendMessage(RMText.noPermissionCommand);
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												if(rmGame!=null){
													rmp.setRequestInt(rmGame.getConfig().getId());
													rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST);
													rmp.sendMessage("Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store items"+ChatColor.WHITE+".");
												}
												else{
													rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST_SELECT);
													rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"claim found items "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"chest"+ChatColor.WHITE+".");
												}
												return true;
											}
										}
										if(rmGame!=null) rmGame.claimFound(rmp, requestClaimItemsAtArgsPos(rmp, args, 2));
										else{
											rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 2));
											rmp.setPlayerAction(PlayerAction.CLAIM_FOUND);
											rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"claim found items"+ChatColor.WHITE+".");
										}
										return true;
									}
									else rmp.sendMessage("You can't claim the game's "+ChatColor.YELLOW+"found items "+ChatColor.WHITE+"while you're in-game.");
									return true;
								}
								else if(args[1].equalsIgnoreCase("items")){
									if(!rmp.hasPermission("resourcemadness.claim.items")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase("chest")){
												if(!rmp.hasPermission("resourcemadness.claim.items.chest")) return rmp.sendMessage(RMText.noPermissionCommand);
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												rmp.setPlayerAction(PlayerAction.CLAIM_ITEMS_CHEST);
												rmp.sendMessage("Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store items"+ChatColor.WHITE+".");
												return true;
											}
										}
										rmp.claimItems(requestClaimItemsAtArgsPos(rmp, args, 2));
										return true;
									}
									else rmp.sendMessage("You can't claim your "+ChatColor.YELLOW+"items "+ChatColor.WHITE+"while you're in-game.");
									return true;
								}
								else if(args[1].equalsIgnoreCase("reward")){
									if(!rmp.hasPermission("resourcemadness.claim.reward")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase("chest")){
												if(!rmp.hasPermission("resourcemadness.claim.reward.chest")) return rmp.sendMessage(RMText.noPermissionCommand);
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												rmp.setPlayerAction(PlayerAction.CLAIM_REWARD_CHEST);
												rmp.sendMessage("Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store reward"+ChatColor.WHITE+".");;
												return true;
											}
										}
										rmp.claimReward(requestClaimItemsAtArgsPos(rmp, args, 2));
										return true;
									}
									else rmp.sendMessage("You can't claim your "+ChatColor.YELLOW+"reward "+ChatColor.WHITE+"while you're in-game.");
									return true;
								}
								else if(args[1].equalsIgnoreCase("tools")){
									if(!rmp.hasPermission("resourcemadness.claim.tools")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(args.length>2){
										if(args[2].equalsIgnoreCase("chest")){
											if(!rmp.hasPermission("resourcemadness.claim.tools.chest")) return rmp.sendMessage(RMText.noPermissionCommand);
											rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
											rmp.setPlayerAction(PlayerAction.CLAIM_TOOLS_CHEST);
											rmp.sendMessage("Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store tools"+ChatColor.WHITE+".");
											return true;
										}
									}
									rmp.claimTools(requestClaimItemsAtArgsPos(rmp, args, 2));
									return true;
								}
							}
							rmClaimInfo(rmp);
							return true;
						}
						//SET
						else if(args[0].equalsIgnoreCase("set")){
							if(!rmp.hasPermission("resourcemadness.set")) return rmp.sendMessage(RMText.noPermissionCommand);
							page = 0;
							if(args.length>1){
								PlayerAction action = null;
								//MIN PLAYERS
								if(args[1].equalsIgnoreCase("min")){
									if(!rmp.hasPermission("resourcemadness.set.minplayers")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MIN_PLAYERS;
								}
								//MAX PLAYERS
								else if(args[1].equalsIgnoreCase("max")){
									if(!rmp.hasPermission("resourcemadness.set.maxplayers")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MAX_PLAYERS;
								}
								//MIN TEAM PLAYERS
								else if(args[1].equalsIgnoreCase("minteam")){
									if(!rmp.hasPermission("resourcemadness.set.minteamplayers")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MIN_TEAM_PLAYERS;
								}
								//MAX TEAM PLAYERS
								else if(args[1].equalsIgnoreCase("maxteam")){
									if(!rmp.hasPermission("resourcemadness.set.maxteamplayers")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MAX_TEAM_PLAYERS;
								}
								//MAX ITEMS
								else if(args[1].equalsIgnoreCase("maxitems")){
									if(!rmp.hasPermission("resourcemadness.set.maxitems")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MAX_ITEMS;
								}
								//MATCH TIME LIMIT
								else if(args[1].equalsIgnoreCase("timelimit")){
									if(!rmp.hasPermission("resourcemadness.set.timelimit")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_TIME_LIMIT;
								}
								//AUTO RANDOM ITEMS
								else if(args[1].equalsIgnoreCase("random")){
									if(!rmp.hasPermission("resourcemadness.set.random")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_RANDOM;
								}
								else page = RMHelper.getIntByString(args[1]);
								
								if(action!=null){
									if(args.length==3){
										int amount = RMHelper.getIntByString(args[2]);
										if(amount>-1){
											if(rmGame!=null){
												switch(action){
													case SET_MIN_PLAYERS: rmGame.setMinPlayers(rmp, amount); break;
													case SET_MAX_PLAYERS: rmGame.setMaxPlayers(rmp, amount); break;
													case SET_MIN_TEAM_PLAYERS: rmGame.setMinTeamPlayers(rmp, amount); break;
													case SET_MAX_TEAM_PLAYERS: rmGame.setMaxTeamPlayers(rmp, amount); break;
													case SET_MAX_ITEMS: rmGame.setMaxItems(rmp, amount); break;
													case SET_TIME_LIMIT: rmGame.setTimeLimit(rmp, amount); break;
													case SET_RANDOM: rmGame.setRandomizeAmount(rmp, amount); break;
												}
												return true;
											}
											else{
												rmp.setRequestInt(RMHelper.getIntByString(args[2]));
												switch(action){
													case SET_MIN_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MIN_PLAYERS);
														rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"set min players"+ChatColor.WHITE+".");
														break;
													case SET_MAX_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_PLAYERS);
														rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"set max players"+ChatColor.WHITE+".");
														break;
													case SET_MIN_TEAM_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MIN_TEAM_PLAYERS);
														rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"set min team players"+ChatColor.WHITE+".");
														break;
													case SET_MAX_TEAM_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_TEAM_PLAYERS);
														rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"set max team players"+ChatColor.WHITE+".");
														break;
													case SET_MAX_ITEMS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_ITEMS);
														rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"set max items"+ChatColor.WHITE+".");
														break;
													case SET_TIME_LIMIT:
														rmp.setPlayerAction(PlayerAction.SET_TIME_LIMIT);
														rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"set match time limit"+ChatColor.WHITE+".");
														break;
													case SET_RANDOM:
														rmp.setPlayerAction(PlayerAction.SET_RANDOM);
														rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"set auto randomize items"+ChatColor.WHITE+".");
														break;
												}
												return true;
											}
										}
									}
								}
								
								//SET & TOGGLE
								action = null;
								//ADVERTISE GAME IN SEARCH
								if(args[1].equalsIgnoreCase("advertise")){
									if(!rmp.hasPermission("resourcemadness.set.advertise")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_ADVERTISE;
								}
								//AUTO RESTORE WORLD
								else if(args[1].equalsIgnoreCase("restore")){
									if(!rmp.hasPermission("resourcemadness.set.restore")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_RESTORE;
								}
								//GATHER PLAYERS
								else if(args[1].equalsIgnoreCase("warp")){
									if(!rmp.hasPermission("resourcemadness.set.warp")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_WARP;
								}
								//ALLOW MIDGAME JOIN
								else if(args[1].equalsIgnoreCase("midgamejoin")){
									if(!rmp.hasPermission("resourcemadness.set.random")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MIDGAME_JOIN;
								}
								//HEAL PLAYER
								else if(args[1].equalsIgnoreCase("healplayer")){
									if(!rmp.hasPermission("resourcemadness.set.healplayer")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_HEAL_PLAYER;
								}
								//CLEAR PLAYER INVENTORY
								else if(args[1].equalsIgnoreCase("clearinventory")){
									if(!rmp.hasPermission("resourcemadness.set.clearinventory")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_CLEAR_INVENTORY;
								}
								//WARN UNEQUAL ITEMS
								else if(args[1].equalsIgnoreCase("warnunequal")){
									if(!rmp.hasPermission("resourcemadness.set.warnunequal")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_WARN_UNEQUAL;
								}
								//ALLOW UNEQUAL ITEMS
								else if(args[1].equalsIgnoreCase("allowunequal")){
									if(!rmp.hasPermission("resourcemadness.set.allowunequal")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_ALLOW_UNEQUAL;
								}
								//WARN HACK ITEMS
								else if(args[1].equalsIgnoreCase("warnhacked")){
									if(!rmp.hasPermission("resourcemadness.set.warnhacked")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_WARN_HACKED;
								}
								//ALLOW HACK ITEMS
								else if(args[1].equalsIgnoreCase("allowhacked")){
									if(!rmp.hasPermission("resourcemadness.set.allowhacked")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_ALLOW_HACKED;
								}
								//INFINITE REWARD ITEMS
								else if(args[1].equalsIgnoreCase("infinitereward")){
									if(!rmp.hasPermission("resourcemadness.set.infinitereward")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_INFINITE_REWARD;
								}
								//INFINITE TOOLS ITEMS
								else if(args[1].equalsIgnoreCase("infinitetools")){
									if(!rmp.hasPermission("resourcemadness.set.infinitetools")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_INFINITE_TOOLS;
								}
								
								if(action!=null){
									int i=-1;
									if(args.length==3){
										i = RMHelper.getBoolIntByString(args[2]);
										if(i!=-1){
											if(i>1) i=1;
										}
									}
									if(rmGame!=null){
										switch(action){
											case SET_ADVERTISE: rmGame.setAdvertise(rmp, i); break;
											case SET_RESTORE: rmGame.setAutoRestoreWorld(rmp, i); break;
											case SET_WARP: rmGame.setWarpToSafety(rmp, i); break;
											case SET_MIDGAME_JOIN: rmGame.setAllowMidgameJoin(rmp, i); break;
											case SET_HEAL_PLAYER: rmGame.setHealPlayer(rmp, i); break;
											case SET_CLEAR_INVENTORY: rmGame.setClearPlayerInventory(rmp, i); break;
											case SET_WARN_UNEQUAL: rmGame.setWarnUnequal(rmp, i); break;
											case SET_ALLOW_UNEQUAL: rmGame.setAllowUnequal(rmp, i); break;
											case SET_WARN_HACKED: rmGame.setWarnHackedItems(rmp, i); break;
											case SET_ALLOW_HACKED: rmGame.setAllowHackedItems(rmp, i); break;
											case SET_INFINITE_REWARD: rmGame.setInfiniteReward(rmp, i); break;
											case SET_INFINITE_TOOLS: rmGame.setInfiniteTools(rmp, i); break;
										}
									}
									else{
										rmp.setRequestInt(i);
										switch(action){
											case SET_ADVERTISE:
												rmp.setPlayerAction(PlayerAction.SET_ADVERTISE);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle advertise"+ChatColor.WHITE+".");
												break;
											case SET_RESTORE:
												rmp.setPlayerAction(PlayerAction.SET_RESTORE);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle auto restore world"+ChatColor.WHITE+".");
												break;
											case SET_WARP:
												rmp.setPlayerAction(PlayerAction.SET_WARP);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle teleport players"+ChatColor.WHITE+".");
												break;
											case SET_MIDGAME_JOIN:
												rmp.setPlayerAction(PlayerAction.SET_MIDGAME_JOIN);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle allow midgame join"+ChatColor.WHITE+".");
												break;
											case SET_HEAL_PLAYER:
												rmp.setPlayerAction(PlayerAction.SET_HEAL_PLAYER);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle heal player"+ChatColor.WHITE+".");
												break;
											case SET_CLEAR_INVENTORY:
												rmp.setPlayerAction(PlayerAction.SET_CLEAR_INVENTORY);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle clear player inventory"+ChatColor.WHITE+".");
												break;
											case SET_WARN_UNEQUAL:
												rmp.setPlayerAction(PlayerAction.SET_WARN_UNEQUAL);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle warn unequal items"+ChatColor.WHITE+".");
												break;
											case SET_ALLOW_UNEQUAL:
												rmp.setPlayerAction(PlayerAction.SET_ALLOW_UNEQUAL);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle allow unequal items"+ChatColor.WHITE+".");
												break;
											case SET_WARN_HACKED:
												rmp.setPlayerAction(PlayerAction.SET_WARN_HACKED);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle warn hacked items"+ChatColor.WHITE+".");
												break;
											case SET_ALLOW_HACKED:
												rmp.setPlayerAction(PlayerAction.SET_ALLOW_HACKED);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle allow hacked items"+ChatColor.WHITE+".");
												break;
											case SET_INFINITE_REWARD:
												rmp.setPlayerAction(PlayerAction.SET_INFINITE_REWARD);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle infinite reward"+ChatColor.WHITE+".");
												break;
											case SET_INFINITE_TOOLS:
												rmp.setPlayerAction(PlayerAction.SET_INFINITE_TOOLS);
												rmp.sendMessage("Left click a game block to "+ChatColor.YELLOW+"toggle infinite tools"+ChatColor.WHITE+".");
												break;
										}
									}
									return true;
								}
							}
							rmSetInfo(rmp, page);
							return true;
						}
						//ITEM - Get Item NAME by ID or Item ID by NAME
						else if(args[0].equalsIgnoreCase("item")){
							List<String> listArgs = new ArrayList<String>();
							for(int i=1; i<args.length; i++){
								listArgs.add(args[i]);
							}
							if(listArgs.size()>0){
								if(!rmp.hasPermission("resourcemadness.iteminfo")) return rmp.sendMessage(RMText.noPermissionCommand);
								HashMap<Integer, Material> items = new HashMap<Integer, Material>();
								List<String> itemsWarn = new ArrayList<String>();
								for(String str : listArgs){
									String[] strItems = str.split(",");
									for(String strItem : strItems){
										for(Material mat : Material.values()){
											if(mat.name().toLowerCase().contains(strItem.toLowerCase())){
												int id = mat.getId();
												if(!items.containsKey(id)) items.put(id, mat);
											}
										}
										if(strItem.contains("-")){
											String[] strItemsDash = strItem.split("-");
											int id1=RMHelper.getIntByString(strItemsDash[0]);
											int id2=RMHelper.getIntByString(strItemsDash.length>1?strItemsDash[1]:"-1");
											if((id1!=-1)&&(id2!=-1)){
												if(id1>id2){
													int id3=id1;
													id1=id2;
													id2=id3;
												}
												while(id1<=id2){
													Material mat = Material.getMaterial(id1);
													if(mat!=null) if(!items.containsKey(id1)) items.put(id1, mat);
													else if(!itemsWarn.contains(strItem)) itemsWarn.add(""+id1);
													id1++;
												}
											}
										}
										else{
											List<Integer> list = new ArrayList<Integer>();
											int id=RMHelper.getIntByString(strItem);
											if(id==-1) list = RMHelper.getMaterialIdListByString(strItem);
											list.add(id);
											Iterator<Integer> iter = list.iterator();
											while(iter.hasNext()){
												id = iter.next();
												Material mat = Material.getMaterial(id);
												if(mat!=null) if(!items.containsKey(id)) items.put(id, mat);
												else if(!itemsWarn.contains(strItem)) itemsWarn.add(""+id);
											}
										}
									}
								}
								if(items.size()>0){
									rmp.sendMessage(RMText.getFormattedItemStringByHashMap(items));
									return true;
								}
								else if(itemsWarn.size()>0){
									rmp.sendMessage("These items don't exist!");
									//rmp.sendMessage("These items don't exist: "+getFormattedStringByList(itemsWarn));
									return true;
								}
							}
						}
						rmInfo(rmp, page);
					}
				}
			}
		}
		return true;
	}
	public void saveTemplate(RMFilterTemplate template){
		if(template==null) return;
		File folder = new File(getDataFolder()+File.separator+"template");
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating templates directory...");
			folder.mkdir();
		}
	}
	public void saveAllBackup(){
		log.log(Level.INFO, RMText.preLog+"Autosaving...");
		if(RMGame.getGames().size()==0) return;
		File folder = new File(getDataFolder()+File.separator+"backup");
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating backup directory...");
			folder.mkdir();
		}
		File file = new File(folder.getAbsolutePath()+File.separator+"config.txt");
		if(!file.exists()) save(DataType.CONFIG, false, file);
		save(DataType.STATS, false, new File(folder.getAbsolutePath()+File.separator+"stats.txt"));
		//save(DataType.PLAYER, false, new File(folder.getAbsolutePath()+File.separator+"playerdata.txt"));
		saveYaml(DataType.PLAYER, new File(folder.getAbsolutePath()+File.separator+"playerdata.yml"));
		//save(DataType.GAME, false, new File(folder.getAbsolutePath()+File.separator+"gamedata.txt"));
		saveYaml(DataType.GAME, new File(folder.getAbsolutePath()+File.separator+"gamedata.yml"));
		save(DataType.LOG, true, new File(folder.getAbsolutePath()+File.separator+"gamelogdata.txt"));
	}
	public void saveConfig(){
		File folder = getDataFolder();
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating config directory...");
			folder.mkdir();
		}
		File file = new File(folder.getAbsolutePath()+File.separator+"config.txt");
		if(!file.exists()) save(DataType.CONFIG, false, file);
	}
	public void saveAll(){
		if(RMGame.getGames().size()==0) return;
		File folder = getDataFolder();
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating config directory...");
			folder.mkdir();
		}
		File file = new File(folder.getAbsolutePath()+File.separator+"config.txt");
		if(!file.exists()) save(DataType.CONFIG, false, file);
		save(DataType.STATS, false, new File(folder.getAbsolutePath()+File.separator+"stats.txt"));
		//save(DataType.PLAYER, false, new File(folder.getAbsolutePath()+File.separator+"playerdata.txt"));
		saveYaml(DataType.PLAYER, new File(folder.getAbsolutePath()+File.separator+"playerdata.yml"));
		//save(DataType.GAME, false, new File(folder.getAbsolutePath()+File.separator+"gamedata.txt"));
		saveYaml(DataType.GAME, new File(folder.getAbsolutePath()+File.separator+"gamedata.yml"));
		save(DataType.LOG, true, new File(folder.getAbsolutePath()+File.separator+"gamelogdata.txt"));
	}
	
	public boolean saveYaml(DataType dataType, File file){
		if(file==null){
			log.log(Level.WARNING, "Cannot load data. Data type unknown!");
			return false;
		}
		if(!file.exists()){
			switch(dataType){
				case CONFIG: log.log(Level.INFO, RMText.preLog+"Data file not found! Creating one..."); break;
				case STATS: log.log(Level.INFO, RMText.preLog+"Stats file not found! Creating one..."); break;
				case PLAYER: log.log(Level.INFO, RMText.preLog+"Player Data file not found! Creating one..."); break;
				case GAME: log.log(Level.INFO, RMText.preLog+"Game Data file not found! Creating one..."); break;
				case LOG: log.log(Level.INFO, RMText.preLog+"Game Data file not found! Creating one..."); break;
			}
			try{
				file.createNewFile();
			}
			catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		try{
			Configuration yml = new Configuration(file);
			yml.load();
			switch(dataType){
				case CONFIG:
					break;
				case STATS:
					break;
				case PLAYER:
					for(RMPlayer rmp : RMPlayer.getPlayers().values()){
						String root = rmp.getName()+".";
						setProperty(yml, root+"ready", rmp.getReady());
						//Stats
						root = rmp.getName()+".stats.";
						RMStats stats = rmp.getStats();
						setProperty(yml, root+"wins", stats.getWins());
						setProperty(yml, root+"losses", stats.getLosses());
						setProperty(yml, root+"timesplayed", stats.getTimesPlayed());
						setProperty(yml, root+"itemsfoundtotal", stats.getItemsFoundTotal());
						//Data
						root = rmp.getName()+".data.";
						setProperty(yml, root+"items", RMInventoryHelper.encodeInventoryToString(rmp.getItems().getItemsArray(), ClaimType.ITEMS));
						setProperty(yml, root+"reward", RMInventoryHelper.encodeInventoryToString(rmp.getReward().getItemsArray(), ClaimType.REWARD)); 
						setProperty(yml, root+"tools", RMInventoryHelper.encodeInventoryToString(rmp.getTools().getItemsArray(), ClaimType.TOOLS));
					}
					break;
				case GAME:
					HashMap<Integer, RMGame> games = RMGame.getGames();
					for(Integer id : games.keySet()){
						RMGameConfig config = games.get(id).getConfig();
						Block b = config.getPartList().getMainBlock();
						String location = b.getX()+","+b.getY()+","+b.getZ();
						String root = id+".";
						setProperty(yml, root+"location", location);
						setProperty(yml, root+"world",config.getWorldName());
						setProperty(yml, root+"owner",config.getOwnerName());
						setProperty(yml, root+"state",config.getState().ordinal());
						setProperty(yml, root+"interface",config.getInterface().ordinal());
						setProperty(yml, root+"timeelapsed",config.getTimer().getTimeElapsed());
						//Settings
						root = id+".settings.";
						setProperty(yml, root+"minplayers", config.getMinPlayers());
						setProperty(yml, root+"maxplayers", config.getMaxPlayers());
						setProperty(yml, root+"minteamplayers", config.getMinTeamPlayers());
						setProperty(yml, root+"maxteamplayers", config.getMaxTeamPlayers());
						setProperty(yml, root+"timelimit", config.getTimer().getTimeLimit());
						setProperty(yml, root+"autorandomizeamount", config.getAutoRandomizeAmount());
						setProperty(yml, root+"advertise", config.getAdvertise());
						setProperty(yml, root+"autorestoreworld", config.getAutoRestoreWorld());
						setProperty(yml, root+"warptosafety", config.getWarpToSafety());
						setProperty(yml, root+"allowmidgamejoin", config.getAllowMidgameJoin());
						setProperty(yml, root+"healplayer", config.getHealPlayer());
						setProperty(yml, root+"clearplayerinventory", config.getClearPlayerInventory());
						setProperty(yml, root+"warnunequal", config.getWarnUnequal());
						setProperty(yml, root+"allowunequal", config.getAllowUnequal());
						setProperty(yml, root+"warnhackeditems", config.getWarnHackedItems());
						setProperty(yml, root+"allowhackeditems", config.getAllowHackedItems());
						setProperty(yml, root+"infinitereward", config.getInfiniteReward());
						setProperty(yml, root+"infinitetools", config.getInfiniteTools());
						//Stats
						root = id+".stats.";
						RMStats stats = config.getStats();
						setProperty(yml, root+"wins", stats.getWins());
						setProperty(yml, root+"losses", stats.getLosses());
						setProperty(yml, root+"timesplayed", stats.getTimesPlayed());
						setProperty(yml, root+"itemsfoundtotal",stats.getItemsFoundTotal());
						
						////Teams
						for(int i=0; i<config.getTeams().size(); i++){
							root = id+".data.teams."+i+".";
							RMTeam team = config.getTeams().get(i);
							setProperty(yml, root+"isdisqualified", team.isDisqualified());
							setProperty(yml, root+"color", team.getTeamColor().name());
							List<String> players = new ArrayList<String>();
							for(RMPlayer rmp : team.getPlayers()){
								players.add(rmp.getName());
							}
							setProperty(yml, root+"players", players);
							setProperty(yml, root+"chest", RMInventoryHelper.encodeInventoryToString(team.getChest().getStash().getItemsArray(), ClaimType.CHEST));
							setProperty(yml, root+"items", RMFilter.encodeFilterToString(team.getChest().getRMItems(), FilterState.ITEMS));
						}
						//Items
						root = id+".data.";
						setProperty(yml, root+"filter", RMFilter.encodeFilterToString(config.getFilter().getItems(), FilterState.FILTER));
						setProperty(yml, root+"items", RMFilter.encodeFilterToString(config.getItems().getItems(), FilterState.ITEMS));
						setProperty(yml, root+"found", RMInventoryHelper.encodeInventoryToString(config.getFoundArray(), ClaimType.FOUND));
						setProperty(yml, root+"reward", RMInventoryHelper.encodeInventoryToString(config.getRewardArray(), ClaimType.REWARD));
						setProperty(yml, root+"tools", RMInventoryHelper.encodeInventoryToString(config.getToolsArray(), ClaimType.TOOLS));
					}
					break;
				case LOG:
					break;
			}
			yml.save();
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		if((file.exists())&&(file.length()>0)){
			File folderBackup = new File(getDataFolder()+File.separator+"backup");
			if(!folderBackup.exists()){
				try{
					folderBackup.mkdir();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
			if(!RMHelper.copyFile(file, new File(folderBackup.getAbsolutePath()+File.separator+file.getName()))){
				switch(dataType){
					case CONFIG: log.log(Level.INFO, RMText.preLog+"Could not create config backup file."); break;
					case STATS: log.log(Level.INFO, RMText.preLog+"Could not create stats backup file."); break;
					case PLAYER: log.log(Level.INFO, RMText.preLog+"Could not create player data backup file."); break;
					case GAME: log.log(Level.INFO, RMText.preLog+"Could not create game data backup file."); break;
					case LOG: log.log(Level.INFO, RMText.preLog+"Could not create game log data backup file."); break;
				}
			}
		}
		return true;
	}
	
	public void setProperty(Configuration yml, String root, Object x){
		yml.setProperty(root, x);
	}
	
	//Save Data
	public boolean save(DataType dataType, boolean useLZF, File file){
		if(file==null){
			log.log(Level.WARNING, "Cannot load data. Data type unknown!");
			return false;
		}
		if(!file.exists()){
			switch(dataType){
				case CONFIG: log.log(Level.INFO, RMText.preLog+"Data file not found! Creating one..."); break;
				case STATS: log.log(Level.INFO, RMText.preLog+"Stats file not found! Creating one..."); break;
				case PLAYER: log.log(Level.INFO, RMText.preLog+"Player Data file not found! Creating one..."); break;
				case GAME: log.log(Level.INFO, RMText.preLog+"Game Data file not found! Creating one..."); break;
				case LOG: log.log(Level.INFO, RMText.preLog+"Game Data file not found! Creating one..."); break;
			}
			try{
				file.createNewFile();
			}
			catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		if((file.exists())&&(file.length()>0)){
			
			File folderBackup = new File(getDataFolder()+File.separator+"backup");
			if(!folderBackup.exists()){
				try{
					folderBackup.mkdir();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
			if(!RMHelper.copyFile(file, new File(folderBackup.getAbsolutePath()+File.separator+file.getName()))){
				switch(dataType){
					case CONFIG: log.log(Level.INFO, RMText.preLog+"Could not create config backup file."); break;
					case STATS: log.log(Level.INFO, RMText.preLog+"Could not create stats backup file."); break;
					case PLAYER: log.log(Level.INFO, RMText.preLog+"Could not create player data backup file."); break;
					case GAME: log.log(Level.INFO, RMText.preLog+"Could not create game data backup file."); break;
					case LOG: log.log(Level.INFO, RMText.preLog+"Could not create game log data backup file."); break;
				}
			}
		}
		try{
			OutputStream output;
			if(useLZF) output = new LZFOutputStream(new FileOutputStream(file.getAbsoluteFile()));
			else output = new FileOutputStream(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
			String line = "";
			switch(dataType){
				case CONFIG:
					line = "";
					//bw.write("[Resource Madness v"+pdfFile.getVersion()+" Config]");
					line+=RMText.cAutoSave+"\n";
					line+="autosave="+config.getAutoSave()+"\n\n";
					line+=RMText.cUsePermissions+"\n";
					line+="usePermissions="+config.getPermissionType().name().toLowerCase()+"\n\n";
					line+=RMText.cUseRestore1+"\n";
					line+=RMText.cUseRestore2+"\n";
					line+="useRestore="+config.getUseRestore()+"\n\n";
					line+=RMText.cServerWide+"\n\n";
					//Max games
					line+=RMText.cMaxGames+"\n";
					line+="maxGames="+config.getMaxGames()+"\n\n";
					//Max games per player
					line+=RMText.cMaxGamesPerPlayer+"\n";
					line+="maxGamesPerPlayer="+config.getMaxGamesPerPlayer()+"\n\n";
					//Default game settings
					line+=RMText.cDefaultSettings1+"\n";
					line+=RMText.cDefaultSettings2+"\n\n";
					//Min players per game
					line+=RMText.cMinPlayers+"\n";
					line+="minPlayers="+config.getMinPlayers()+(config.isLocked(Lock.minPlayers)?":lock":"")+"\n\n";
					//Max player per game
					line+=RMText.cMaxPlayers+"\n";
					line+="maxPlayers="+config.getMaxPlayers()+(config.isLocked(Lock.maxPlayers)?":lock":"")+"\n\n";
					//Min players per team
					line+=RMText.cMinTeamPlayers+"\n";
					line+="minTeamPlayers="+config.getMinTeamPlayers()+(config.isLocked(Lock.minTeamPlayers)?":lock":"")+"\n\n";
					//Max players per team
					line+=RMText.cMaxTeamPlayers+"\n";
					line+="maxTeamPlayers="+config.getMaxTeamPlayers()+(config.isLocked(Lock.maxTeamPlayers)?":lock":"")+"\n\n";
					//Match time limit
					line+=RMText.cTimeLimit+"\n";
					line+="timeLimit="+config.getTimeLimit()+(config.isLocked(Lock.timeLimit)?":lock":"")+"\n\n";
					//Default game settings true/false
					line+=RMText.cDefaultSettings3+"\n";
					line+=RMText.cDefaultSettings4+"\n\n";
					//Advertise game in search
					line+=RMText.cAdvertise+"\n";
					line+="advertise="+config.getAdvertise()+(config.isLocked(Lock.advertise)?":lock":"")+"\n\n";
					//Auto restore world
					line+=RMText.cAutoRestoreWorld+"\n";
					line+="autoRestoreWorld="+config.getAutoRestoreWorld()+(config.isLocked(Lock.autoRestoreWorld)?":lock":"")+"\n\n";
					//Warp to safety
					line+=RMText.cWarpToSafety+"\n";
					line+="warpToSafety="+config.getWarpToSafety()+(config.isLocked(Lock.warpToSafety)?":lock":"")+"\n\n";
					//Allow players to join a game in progress
					line+=RMText.cAllowMidgameJoin+"\n";
					line+="allowMidgameJoin="+config.getAllowMidgameJoin()+(config.isLocked(Lock.allowMidgameJoin)?":lock":"")+"\n\n";
					//Heal players at game start
					line+=RMText.cHealPlayer+"\n";
					line+="healPlayer="+config.getHealPlayer()+(config.isLocked(Lock.healPlayer)?":lock":"")+"\n\n";
					//Clear/return player's items at game start/finish
					line+=RMText.cClearPlayerInventory+"\n";
					line+="clearPlayerInventory="+config.getClearPlayerInventory()+(config.isLocked(Lock.clearPlayerInventory)?":lock":"")+"\n\n";
					//Warn when reward/tools can't be distributed equally
					line+=RMText.cWarnUnequal+"\n";
					line+="warnunequal="+config.getWarnUnequal()+(config.isLocked(Lock.warnUnequal)?":lock":"")+"\n\n";
					//Allow reward/tools to be distributed unequally
					line+=RMText.cAllowUnequal+"\n";
					line+="allowunequal="+config.getAllowUnequal()+(config.isLocked(Lock.allowUnequal)?":lock":"")+"\n\n";
					//Warn when hacked items are added
					line+=RMText.cWarnHackedItems+"\n";
					line+="warnHackedItems="+config.getWarnHackedItems()+(config.isLocked(Lock.warnHackedItems)?":lock":"")+"\n\n";
					//Allow the use of hacked items
					line+=RMText.cAllowHackedItems+"\n";
					line+="allowHackedItems="+config.getAllowHackedItems()+(config.isLocked(Lock.allowHackedItems)?":lock":"")+"\n\n";
					//Use infinite reward
					line+=RMText.infiniteReward+"\n";
					line+="infinitereward="+config.getInfiniteReward()+(config.isLocked(Lock.infiniteReward)?":lock":"")+"\n\n";
					//Use infinite tools
					line+=RMText.cInfiniteTools+"\n";
					line+="infinitetools="+config.getInfiniteTools()+(config.isLocked(Lock.infiniteTools)?":lock":"");
					bw.write(line);
					break;
				case STATS:
					//bw.write("[Resource Madness v"+pdfFile.getVersion()+" Stats]");
					//Stats
					line = "";
					line += RMStats.getServerWins()+","+RMStats.getServerLosses()+","+RMStats.getServerTimesPlayed()+","+/*RMStats.getServerItemsFound()+","+*/RMStats.getServerItemsFoundTotal()+";";
					bw.write(line);
					bw.write("\n");
					break;
				case PLAYER:
					//bw.write("[Resource Madness v"+pdfFile.getVersion()+" Player Data]");
					for(RMPlayer rmp : RMPlayer.getPlayers().values()){
						line = "";
						line += rmp.getName()+",";
						line += rmp.getReady()+";";
						//Stats
						RMStats stats = rmp.getStats();
						line += stats.getWins()+","+stats.getLosses()+","+stats.getTimesPlayed()+","+stats.getItemsFoundTotal()+";";
						//Inventory items
						line += RMInventoryHelper.encodeInventoryToString(rmp.getItems().getItemsArray(), ClaimType.ITEMS) + ";";
						line += RMInventoryHelper.encodeInventoryToString(rmp.getReward().getItemsArray(), ClaimType.REWARD) + ";";
						line += RMInventoryHelper.encodeInventoryToString(rmp.getTools().getItemsArray(), ClaimType.TOOLS);
						bw.write(line);
						bw.write("\n");
					}
					break;
				case GAME:
					//bw.write("[Resource Madness v"+pdfFile.getVersion()+" Game Data]");
					for(RMGame rmGame : RMGame.getGames().values()){
						RMGameConfig config = rmGame.getConfig();
						line = "";
						//Game
						Block b = config.getPartList().getMainBlock();
						line = b.getX()+","+b.getY()+","+b.getZ()+",";
						line += config.getWorldName()+",";
						line += config.getId()+",";
						line += config.getOwnerName()+",";
						line += config.getState().ordinal()+",";
						line += config.getInterface().ordinal()+",";
						line += config.getTimer().getTimeElapsed();
						line += ";";
						//Settings
						line += config.getMinPlayers()+",";
						line += config.getMaxPlayers()+",";
						line += config.getMinTeamPlayers()+",";
						line += config.getMaxTeamPlayers()+",";
						line += config.getTimer().getTimeLimit()+",";
						line += config.getAutoRandomizeAmount()+",";
						line += config.getAdvertise()+",";
						line += config.getAutoRestoreWorld()+",";
						line += config.getWarpToSafety()+",";
						line += config.getAllowMidgameJoin()+",";
						line += config.getHealPlayer()+",";
						line += config.getClearPlayerInventory()+",";
						line += config.getWarnUnequal()+",";
						line += config.getAllowUnequal()+",";
						line += config.getWarnHackedItems()+",";
						line += config.getAllowHackedItems()+",";
						line += config.getInfiniteReward()+",";
						line += config.getInfiniteTools();
						line += ";";
						//Stats
						RMStats stats = config.getStats();
						line += stats.getWins()+","+stats.getLosses()+","+stats.getTimesPlayed()+","+stats.getItemsFoundTotal()+";";
						//Players
						for(RMTeam rmt : config.getTeams()){
							line+=rmt.getTeamColor().name()+":"+rmt.isDisqualified()+":";
							String players = "";
							for(RMPlayer rmp : rmt.getPlayers()){
								players += rmp.getName()+",";
							}
							players = RMText.stripLast(players,",");
							line += players+" ";
						}
						line = RMText.stripLast(line, " ");
						line += ";";
						//Filter items
						line += RMFilter.encodeFilterToString(config.getFilter().getItems(), FilterState.FILTER)+";";
						//Game items
						line += RMFilter.encodeFilterToString(config.getItems().getItems(), FilterState.ITEMS)+";";
						//Found items
						line += RMInventoryHelper.encodeInventoryToString(config.getFoundArray(), ClaimType.FOUND)+";";
						//Reward items
						line += RMInventoryHelper.encodeInventoryToString(config.getRewardArray(), ClaimType.REWARD)+";";
						//Tools items
						line += RMInventoryHelper.encodeInventoryToString(config.getToolsArray(), ClaimType.TOOLS)+";";
						//Chest items
						for(RMTeam rmt : config.getTeams()){
							line += RMInventoryHelper.encodeInventoryToString(rmt.getChest().getStash().getItemsArray(), ClaimType.CHEST)+".";
						}
						line = RMText.stripLast(line, ".");
						line += ";";
						//Team items
						for(RMTeam rmt : config.getTeams()){
							line += RMFilter.encodeFilterToString(rmt.getChest().getRMItems(), FilterState.ITEMS)+".";
						}
						line = RMText.stripLast(line, ".");
						bw.write(line);
						bw.write("\n");
					}
					break;
				case LOG:
					for(RMGame rmGame : RMGame.getGames().values()){
						line = "";
						//Log
						RMGameConfig config = rmGame.getConfig();
						line += rmLogHelper.encodeLogToString(config.getLog());
						bw.write(line);
						bw.write("\n");
					}
					break;
				case YAML_GAME:
					break;
			}
			bw.flush();
			output.close();
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//Load All
	public void loadAll(){
		File folder = getDataFolder();
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Config folder not found! Will create one on save...");
			return;
		}
		saveConfig();
		load(DataType.CONFIG, false, true);
		load(DataType.STATS, false, true);
		//load(DataType.PLAYER, false, true);
		loadYaml(DataType.PLAYER, true);
		//load(DataType.GAME, false, true);
		loadYaml(DataType.GAME, true);
		load(DataType.LOG, true, true);
	}
	
	public void loadYaml(DataType dataType, boolean loadBackup){
		File folder = getDataFolder();
		File file = null;
		switch(dataType){
			case CONFIG: file = new File(folder.getAbsolutePath()+File.separator+"config.yml"); break;
			case STATS: file = new File(folder.getAbsolutePath()+File.separator+"stats.yml"); break;
			case PLAYER: file = new File(folder.getAbsolutePath()+File.separator+"playerdata.yml"); break;
			case GAME: file = new File(folder.getAbsolutePath()+File.separator+"gamedata.yml"); break;
			case LOG: file = new File(folder.getAbsolutePath()+File.separator+"gamelogdata.yml"); break;
		}
		if(file==null){
			log.log(Level.WARNING, RMText.preLog+"Cannot load data. Data type unknown!");
			return;
		}
		if((file.exists())&&(file.length()>0)){
			try {
				Configuration yml = new Configuration(file);
				yml.load();
				switch(dataType){
				case CONFIG:
					break;
				case STATS:
					break;
				case PLAYER:
					List<String> players = yml.getKeys();
					for(String player : players){
						//name
						RMPlayer rmp = new RMPlayer(player);
						String root = player+".";
						rmp.setReady(yml.getBoolean(root+"ready", false));
						
						//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
						root = player+".stats.";
						RMStats stats = rmp.getStats();
						stats.setWins(yml.getInt(root+"wins", -1));
						stats.setLosses(yml.getInt(root+"losses", -1));
						stats.setTimesPlayed(yml.getInt(root+"timesplayed", -1));
						stats.setItemsFoundTotal(yml.getInt(root+"itemsfoundtotal", -1));
						
						//inventory items
						root = player+".data.";
						String data = yml.getString(root+"items");
						if((data.length()>0)&&(!data.equalsIgnoreCase("ITEMS"))){
							rmp.setItems(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//reward items
						data = yml.getString(root+"reward");
						if((data.length()>0)&&(!data.equalsIgnoreCase("REWARD"))){
							rmp.setReward(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//tools items
						data = yml.getString(root+"tools");
						if((data.length()>0)&&(!data.equalsIgnoreCase("TOOLS"))){
							rmp.setTools(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
					}
					break;
				case GAME:
					//x,y,z,world,id,owner
					List<String> ids = yml.getKeys();
					for(String id : ids){
						String root = id+".";
						String[] location = yml.getString(root+"location").split(",");
						int xLoc = RMHelper.getIntByString(location[0]);
						int yLoc = RMHelper.getIntByString(location[1]);
						int zLoc = RMHelper.getIntByString(location[2]);
						World world = getServer().getWorld(yml.getString(root+"world"));
						Block b = world.getBlockAt(xLoc, yLoc, zLoc);

						RMGameConfig config = new RMGameConfig(this);
						RMPartList partList = new RMPartList(b, this);
						if(partList.getMainBlock()==null) return;
						config.setPartList(partList);
						config.setId(RMHelper.getIntByString(id));
						config.setOwnerName(yml.getString(root+"owner"));
						config.setState(RMHelper.getStateByInt(yml.getInt(root+"state", -1)));
						config.setInterface(RMHelper.getInterfaceByInt(yml.getInt(root+"interface", -1)));
						config.setTimer(new RMGameTimer(yml.getInt(root+"timeelapsed", -1)));
						
						//minPlayers,maxPlayers,minTeamPlayers,maxTeamPlayers,timeLimit,autoRandomizeAmount
						//warpToSafety,autoRestoreWorld,warnHackedItems,allowHackedItems,allowPlayerLeave
						root = id+".settings.";
						config.setMinPlayers(yml.getInt(root+"minplayers", -1));
						config.setMaxPlayers(yml.getInt(root+"maxplayers", -1));
						config.setMinTeamPlayers(yml.getInt(root+"minteamplayers", -1));
						config.setMaxTeamPlayers(yml.getInt(root+"maxteamplayers", -1));
						config.getTimer().setTimeLimit(yml.getInt(root+"timelimit", -1));
						config.setAutoRandomizeAmount(yml.getInt(root+"autorandomizeamount", -1));
						config.setAdvertise(yml.getBoolean(root+"advertise", this.config.getAdvertise()));
						config.setAutoRestoreWorld(yml.getBoolean(root+"autorestoreworld", this.config.getAutoRestoreWorld()));
						config.setWarpToSafety(yml.getBoolean(root+"warptosafety", this.config.getWarpToSafety()));
						config.setAllowMidgameJoin(yml.getBoolean(root+"allowmidgamejoin", this.config.getAllowMidgameJoin()));
						config.setHealPlayer(yml.getBoolean(root+"healplayer", this.config.getHealPlayer()));
						config.setClearPlayerInventory(yml.getBoolean(root+"clearplayerinventory", this.config.getClearPlayerInventory()));
						config.setWarnUnequal(yml.getBoolean(root+"warnunequal", this.config.getWarnUnequal()));
						config.setAllowUnequal(yml.getBoolean(root+"allowunequal", this.config.getAllowUnequal()));
						config.setWarnHackedItems(yml.getBoolean(root+"warnhackeditems", this.config.getWarnHackedItems()));
						config.setAllowHackedItems(yml.getBoolean(root+"allowhackeditems", this.config.getAllowHackedItems()));
						config.setInfiniteReward(yml.getBoolean(root+"infinitereward", this.config.getInfiniteReward()));
						config.setInfiniteTools(yml.getBoolean(root+"infinitetools", this.config.getInfiniteTools()));

						//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
						root = id+".stats.";
						RMStats gameStats = config.getStats();
						gameStats.setWins(yml.getInt(root+"wins", -1));
						gameStats.setLosses(yml.getInt(root+"losses", -1));
						gameStats.setTimesPlayed(yml.getInt(root+"timesplayed", -1));
						gameStats.setItemsFoundTotal(yml.getInt(root+"itemsfoundtotal", -1));
						
						//teams
						List<RMTeam> rmTeams = config.getPartList().fetchTeams();
						for(RMTeam rmt : rmTeams){
							config.getTeams().add(rmt);
						}
						List<String> teamIds = yml.getKeys(id+".data.teams");
						String data;
						for(String teamId : teamIds){
							root = id+".data.teams."+teamId+".";
							RMTeam rmTeam = config.getTeams().get(Integer.parseInt(teamId));
							if(rmTeam!=null){
								rmTeam.isDisqualified(yml.getBoolean(root+"isdisqualified", false));
								List<String> teamPlayers = yml.getStringList(root+"players", new ArrayList<String>());
								if(teamPlayers!=null){
									for(String player : teamPlayers){
										RMPlayer rmp = RMPlayer.getPlayerByName(player);
										if(rmp!=null) rmTeam.addPlayerSilent(rmp);
									}
								}
								//team chest
								data = yml.getString(root+"chest");
								if((data.length()>0)&&(!data.equalsIgnoreCase("CHEST"))){
									rmTeam.getChest().setStash(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
								}
								//team items
								data = yml.getString(root+"items");
								if((data.length()>0)&&(!data.equalsIgnoreCase("ITEMS"))){
									HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(data), true);
									rmTeam.getChest().setRMItems(rmItems);
								}
							}
						}
							
						//filter items
						root = id+".data.";
						data = yml.getString(root+"filter");
						if((data.length()>0)&&(!data.equalsIgnoreCase("FILTER"))){
							HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(data), true);
							config.setFilter(new RMFilter(rmItems));
						}
						//game items
						data = yml.getString(root+"items");
						if((data.length()>0)&&(!data.equalsIgnoreCase("ITEMS"))){
							HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(data), true);
							config.setItems(new RMFilter(rmItems));
						}
						//found items
						data = yml.getString(root+"found");
						if((data.length()>0)&&(!data.equalsIgnoreCase("FOUND"))){
							config.setFound(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//reward items
						data = yml.getString(root+"reward");
						if((data.length()>0)&&(!data.equalsIgnoreCase("REWARD"))){
							config.setReward(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//tools items
						data = yml.getString(root+"tools");
						if((data.length()>0)&&(!data.equalsIgnoreCase("TOOLS"))){
							config.setTools(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						RMGame.tryAddGameFromConfig(config);
					}
					break;
				case LOG:
					break;
				}
				//saveConfig();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else{
			if(loadBackup){
				if(RMHelper.copyFile(new File(getDataFolder().getAbsolutePath()+File.separator+"backup"+File.separator+file.getName()), file)){
					loadYaml(dataType, false);
				}
				else{
					switch(dataType){
						case CONFIG: System.out.println("Could not find config backup file"); break;
						case STATS: System.out.println("Could not find stats backup file"); break;
						case PLAYER: System.out.println("Could not find player data backup file"); break;
						case GAME: System.out.println("Could not find game data backup file"); break;
						case LOG: System.out.println("Could not find game log data backup file"); break;
					}
				}
			}
			else{
				switch(dataType){
				case CONFIG: System.out.println("Could not find config file"); break;
				case STATS: System.out.println("Could not find stats file"); break;
				case PLAYER: System.out.println("Could not find player data file"); break;
				case GAME: System.out.println("Could not find game data file"); break;
				case LOG: System.out.println("Could not find game log data file"); break;
				}
			}
		}
	}
	
	public void load(DataType dataType, boolean useLZF, boolean loadBackup){
		int lineNum = 0;
		File folder = getDataFolder();
		File file = null;
		switch(dataType){
			case CONFIG: file = new File(folder.getAbsolutePath()+File.separator+"config.txt"); break;
			case STATS: file = new File(folder.getAbsolutePath()+File.separator+"stats.txt"); break;
			case PLAYER: file = new File(folder.getAbsolutePath()+File.separator+"playerdata.txt"); break;
			case GAME: file = new File(folder.getAbsolutePath()+File.separator+"gamedata.txt"); break;
			case LOG: file = new File(folder.getAbsolutePath()+File.separator+"gamelogdata.txt"); break;
		}
		if(file==null){
			log.log(Level.WARNING, RMText.preLog+"Cannot load data. Data type unknown!");
			return;
		}
		if((file.exists())&&(file.length()>0)){
			InputStream input;
			try {
				if(useLZF) input = new LZFInputStream(new FileInputStream(file.getAbsoluteFile()));
				else input = new FileInputStream(file.getAbsoluteFile());
				
				InputStreamReader isr = new InputStreamReader(input);
				BufferedReader br = new BufferedReader(isr);
				
				String line;
				while(true){
					line = br.readLine();
					if(line == null) break;
					if(line.startsWith("#")) continue;
					String[] args;
					switch(dataType){
						case CONFIG:
							args = line.split("=");
							if(args.length==2){
								if(args[0].equalsIgnoreCase("autosave")) config.setAutoSave(RMHelper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("usePermissions")) config.setPermissionTypeByString(args[1]);
								else if(args[0].equalsIgnoreCase("useRestore")) config.setUseRestore(Boolean.parseBoolean(args[1]));
								else if(args[0].equalsIgnoreCase("maxGames")) config.setMaxGames(RMHelper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("maxGamesPerPlayer")) config.setMaxGamesPerPlayer(RMHelper.getIntByString(args[1]));
								else{
									boolean lockArg = args[1].substring(args[1].indexOf(":")+1).equalsIgnoreCase("lock")?true:false;
									if(args[0].equalsIgnoreCase("minPlayersPerGame")) config.setMinPlayers(RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("maxPlayersPerGame")) config.setMaxPlayers(RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("minPlayersPerTeam")) config.setMinTeamPlayers(RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("maxPlayersPerTeam")) config.setMaxTeamPlayers(RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("matchTimeLimit")) config.setTimeLimit(RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("autoRandomizeAmount")) config.setAutoRandomizeAmount(RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("advertise")) config.setAdvertise(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("autoRestoreWorld")) config.setAutoRestoreWorld(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("warpToSafety")) config.setWarpToSafety(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("warnHackedItems")) config.setWarnHackedItems(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("allowHackedItems")) config.setAllowHackedItems(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("allowMidgameJoin")) config.setAllowMidgameJoin(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("healPlayer")) config.setHealPlayer(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("clearPlayerInventory")) config.setClearPlayerInventory(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("warnUnequal")) config.setWarnUnequal(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("allowUnequal")) config.setAllowUnequal(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("infiniteReward")) config.setInfiniteReward(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("infiniteTools")) config.setInfiniteTools(Boolean.parseBoolean(args[1]), lockArg);
								}
							}
							break;
						case STATS:
							args = line.split(",");
							//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
							RMStats.setServerWins(RMHelper.getIntByString(args[0]));
							RMStats.setServerLosses(RMHelper.getIntByString(args[1]));
							RMStats.setServerTimesPlayed(RMHelper.getIntByString(args[2]));
							//RMStats.setServerItemsFound(getIntByString(args[3]));
							RMStats.setServerItemsFoundTotal(RMHelper.getIntByString(args[3]));
							break;
						case PLAYER:
							parseLoadedPlayerData(line.split(";"));
							break;
						case GAME:
							parseLoadedGameData(line.split(";"));
							break;
						case LOG:
							rmLogHelper.parseLoadedLogData(line.split(";"), lineNum);
							break;
					}
					lineNum++;
				}
				input.close();
				//saveConfig();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else{
			if(loadBackup){
				if(RMHelper.copyFile(new File(getDataFolder().getAbsolutePath()+File.separator+"backup"+File.separator+file.getName()), file)){
					load(dataType, useLZF, false);
				}
				else{
					switch(dataType){
						case CONFIG: System.out.println("Could not find config backup file"); break;
						case STATS: System.out.println("Could not find stats backup file"); break;
						case PLAYER: System.out.println("Could not find player data backup file"); break;
						case GAME: System.out.println("Could not find game data backup file"); break;
						case LOG: System.out.println("Could not find game log data backup file"); break;
					}
				}
			}
			else{
				switch(dataType){
				case CONFIG: System.out.println("Could not find config file"); break;
				case STATS: System.out.println("Could not find stats file"); break;
				case PLAYER: System.out.println("Could not find player data file"); break;
				case GAME: System.out.println("Could not find game data file"); break;
				case LOG: System.out.println("Could not find game log data file"); break;
				}
			}
		}
	}
	
	public void parseLoadedPlayerData(String[] strArgs){
		//name
		String[] args = strArgs[0].split(",");
		RMPlayer rmp = new RMPlayer(args[0]);
		rmp.setReady(Boolean.parseBoolean(args[1]));
		
		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
		args = strArgs[1].split(",");
		RMStats stats = rmp.getStats();
		
		stats.setWins(RMHelper.getIntByString(args[0]));
		stats.setLosses(RMHelper.getIntByString(args[1]));
		stats.setTimesPlayed(RMHelper.getIntByString(args[2]));
		stats.setItemsFoundTotal(RMHelper.getIntByString(args[3]));
		
		//inventory items
		if(strArgs[2].length()>0){
			if(!strArgs[2].equalsIgnoreCase("ITEMS")){
				rmp.setItems(new RMStash(RMInventoryHelper.getItemStackByStringArray(strArgs[2])));
			}
		}
		//reward items
		if(strArgs[3].length()>0){
			if(!strArgs[3].equalsIgnoreCase("REWARD")){
				rmp.setReward(new RMStash(RMInventoryHelper.getItemStackByStringArray(strArgs[3])));
			}
		}
		//tools items
		if(strArgs[4].length()>0){
			if(!strArgs[4].equalsIgnoreCase("TOOLS")){
				rmp.setTools(new RMStash(RMInventoryHelper.getItemStackByStringArray(strArgs[4])));
			}
		}
	}
	
	public void parseLoadedGameData(String[] strArgs){
		String[] args = strArgs[0].split(",");
		//x,y,z,world,id,owner
		int xLoc = RMHelper.getIntByString(args[0]);
		int yLoc = RMHelper.getIntByString(args[1]);
		int zLoc = RMHelper.getIntByString(args[2]);
		World world = getServer().getWorld(args[3]);
		Block b = world.getBlockAt(xLoc, yLoc, zLoc);

		RMGameConfig config = new RMGameConfig(this);
		RMPartList partList = new RMPartList(b, this);
		if(partList.getMainBlock()==null) return;
		config.setPartList(partList);
		config.setId(RMHelper.getIntByString(args[4]));
		config.setOwnerName(args[5]);
		config.setState(RMHelper.getStateByInt(RMHelper.getIntByString(args[6])));
		config.setInterface(RMHelper.getInterfaceByInt(RMHelper.getIntByString(args[7])));
		config.setTimer(new RMGameTimer(RMHelper.getIntByString(args[8])));
		
		//minPlayers,maxPlayers,minTeamPlayers,maxTeamPlayers,timeLimit,autoRandomizeAmount
		//warpToSafety,autoRestoreWorld,warnHackedItems,allowHackedItems,allowPlayerLeave
		args = strArgs[1].split(",");
		config.setMinPlayers(RMHelper.getIntByString(args[0]));
		config.setMaxPlayers(RMHelper.getIntByString(args[1]));
		config.setMinTeamPlayers(RMHelper.getIntByString(args[2]));
		config.setMaxTeamPlayers(RMHelper.getIntByString(args[3]));
		config.getTimer().setTimeLimit(RMHelper.getIntByString(args[4]));
		config.setAutoRandomizeAmount(RMHelper.getIntByString(args[5]));
		config.setAdvertise(Boolean.parseBoolean(args[6]));
		config.setAutoRestoreWorld(Boolean.parseBoolean(args[7]));
		config.setWarpToSafety(Boolean.parseBoolean(args[8]));
		config.setAllowMidgameJoin(Boolean.parseBoolean(args[9]));
		config.setHealPlayer(Boolean.parseBoolean(args[10]));
		config.setClearPlayerInventory(Boolean.parseBoolean(args[11]));
		config.setWarnUnequal(Boolean.parseBoolean(args[12]));
		config.setAllowUnequal(Boolean.parseBoolean(args[13]));
		config.setWarnHackedItems(Boolean.parseBoolean(args[14]));
		config.setAllowHackedItems(Boolean.parseBoolean(args[15]));
		config.setInfiniteReward(Boolean.parseBoolean(args[16]));
		config.setInfiniteTools(Boolean.parseBoolean(args[17]));

		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
		args = strArgs[2].split(",");
		RMStats gameStats = config.getStats();
		
		gameStats.setWins(RMHelper.getIntByString(args[0]));
		gameStats.setLosses(RMHelper.getIntByString(args[1]));
		gameStats.setTimesPlayed(RMHelper.getIntByString(args[2]));
		//gameStats.setItemsFound(getIntByString(args[3]));
		gameStats.setItemsFoundTotal(RMHelper.getIntByString(args[3]));
		
		//team players
		args = strArgs[3].split(" ");
		List<RMTeam> rmTeams = config.getPartList().fetchTeams();
		for(RMTeam rmt : rmTeams){
			config.getTeams().add(rmt);
		}
		for(int j=0; j<args.length; j++){
			String[] splitArgs = args[j].split(":");
			RMTeam rmTeam = config.getTeams().get(j);
			if(rmTeam!=null){
				rmTeam.isDisqualified(Boolean.parseBoolean(splitArgs[1]));
				if(splitArgs.length==3){
					if(splitArgs[2].length()>0){
						String[] players = splitArgs[2].split(",");
						for(String player : players){
							RMPlayer rmp = RMPlayer.getPlayerByName(player);
							if(rmp!=null) rmTeam.addPlayerSilent(rmp);
						}
					}
				}
			}
		}
			
		//filter items
		if(strArgs[4].length()>0){
			if(!strArgs[4].equalsIgnoreCase("FILTER")){
				HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(strArgs[4]), true);
				config.setFilter(new RMFilter(rmItems));
			}
		}
		//game items
		if(strArgs[5].length()>0){
			if(!strArgs[5].equalsIgnoreCase("ITEMS")){
				HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(strArgs[5]), true);
				config.setItems(new RMFilter(rmItems));
			}
		}
		//found items
		if(strArgs[6].length()>0){
			if(!strArgs[6].equalsIgnoreCase("FOUND")){
				config.setFound(new RMStash(RMInventoryHelper.getItemStackByStringArray(strArgs[6])));
			}
		}
		//reward items
		if(strArgs[7].length()>0){
			if(!strArgs[7].equalsIgnoreCase("REWARD")){
				config.setReward(new RMStash(RMInventoryHelper.getItemStackByStringArray(strArgs[7])));
			}
		}
		//tools items
		if(strArgs[8].length()>0){
			if(!strArgs[8].equalsIgnoreCase("TOOLS")){
				config.setTools(new RMStash(RMInventoryHelper.getItemStackByStringArray(strArgs[8])));
			}
		}
		//chest items
		args = strArgs[9].split("\\.");
		for(int j=0; j<args.length; j++){
			if(args[j].length()>0){
				if(!args[j].equalsIgnoreCase("CHEST")){
					RMTeam rmTeam = config.getTeams().get(j);
					if(rmTeam!=null){
						List<ItemStack> items = RMInventoryHelper.getItemStackByStringArray(args[j]);
						rmTeam.getChest().setStash(new RMStash(items));
					}
				}
			}
		}
		
		//team items
		args = strArgs[10].split("\\.");
		for(int j=0; j<args.length; j++){
			if(args[j].length()>0){
				if(!args[j].equalsIgnoreCase("ITEMS")){
					HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(args[j]), true);
					RMTeam rmTeam = config.getTeams().get(j);
					if(rmTeam!=null){
						rmTeam.getChest().setRMItems(rmItems);
					}
				}
			}
		}
		RMGame.tryAddGameFromConfig(config);
	}
	
	public List<Integer[]> getAmountFromFilterArg(String arg, List<Integer> items){
		boolean useDefaultAmount = false;
		List<Integer[]> amount = new ArrayList<Integer[]>();
		amount.clear();
		if(arg.contains("stack")) useDefaultAmount = true;
		else if(arg.contains(":")){
			List<String> strArgs = RMFilter.splitArgsByColon(arg);
			String strAmount = ""; 
			String[] strSplit = strArgs.get(0).split(":");
			if(strSplit.length>1){
				strAmount = strSplit[1];
				Integer[] intAmount = RMFilter.checkInt(strAmount);
				if(intAmount!=null){
					for(int i=0; i<items.size(); i++){							
						amount.add(intAmount);
					}
				}
				else items.clear();
			}
		}
		else{
			Integer[] intAmount = new Integer[1];
			intAmount[0] = 1;
			for(int i=0; i<items.size(); i++){							
				amount.add(intAmount);
			}
		}
		if(useDefaultAmount) amount = getDefaultAmount(items);
		return amount;
	}
	
	public List<ItemStack> getListItemsFromFilter(String arg, FilterType type){
		List<ItemStack> listItems = new ArrayList<ItemStack>();
		List<Integer> items = getItemsFromFilter(type);
		for(Integer i : items){
			listItems.add(new ItemStack(i));
		}
		if(arg.contains("stack")){
			for(ItemStack item : listItems){
				item.setAmount(item.getType().getMaxStackSize());
			}
		}
		else if(arg.contains(":")){
			List<String> strArgs = RMFilter.splitArgsByColon(arg);
			String strAmount = ""; 
			String[] strSplit = strArgs.get(0).split(":");
			if(strSplit.length>1){
				strAmount = strSplit[1];
				Integer[] intAmount = RMFilter.checkInt(strAmount);
				if(intAmount!=null){
					for(ItemStack item : listItems){
						item.setAmount(intAmount[0]);
					}
				}
				else listItems.clear();
			}
		}
		else{
			Integer[] intAmount = new Integer[1];
			intAmount[0] = 1;
			for(ItemStack item : listItems){		
				item.setAmount(intAmount[0]);
			}
		}
		return listItems;
	}
	
	public FilterType getTypeFromArg(String arg){
		FilterType type = null;
		if(arg.contains("all")) type = FilterType.ALL;
		else if(arg.contains("block")) type = FilterType.BLOCK;
		else if(arg.contains("item")) type = FilterType.ITEM;
		else if(arg.contains("raw")) type = FilterType.RAW;
		else if(arg.contains("crafted")) type = FilterType.CRAFTED;
		return type;
	}
	
	public ItemStack[] requestClaimItemsAtArgsPos(RMPlayer rmp, String[] args, int pos){
		if(args.length>pos){
			List<String> listArgs = new ArrayList<String>();
			for(int i=pos; i<args.length; i++) listArgs.add(args[i]);
			return parseClaim(rmp, listArgs);
		}
		return null;
	}
	
	public ItemStack[] parseClaim(RMPlayer rmp, List<String> args){
		List<ItemStack> listItems = new ArrayList<ItemStack>();
		FilterType type = null;
		//args = args.subList(size, args.size());
		String arg0 = args.get(0);
		type = getTypeFromArg(arg0);

		if(type!=null){
			listItems = getListItemsFromFilter(arg0, type);
		}
		else{
			HashMap<Integer, Integer[]> hashItems = RMFilter.getItemsByStringArray(args, false);
			for(Integer id : hashItems.keySet()){
				byte data = 0;
				short durability = 0;
				ItemStack item = new ItemStack(id, hashItems.get(id)[0].intValue(), durability, data);
				listItems.add(item);
			}
		}
		if((type==null)&&(listItems.size()==0)){
			return null;
		}
		return listItems.toArray(new ItemStack[listItems.size()]);
	}
	
	public void parseFilter(RMPlayer rmp, List<String> args, FilterState filterState){
		int size = 0;
		List<Integer> items = new ArrayList<Integer>();
		List<Integer[]> amount = new ArrayList<Integer[]>();
		List<ItemStack> listItems = new ArrayList<ItemStack>();
		FilterType type = null;
		ForceState force = null;
		int randomize = 0;
		for(String arg : args){
			arg = arg.replace(" ", "");
		}
		if(args.size()==1){
			if(args.get(0).equalsIgnoreCase("clear")){
				force = ForceState.CLEAR;
				rmp.setRequestFilter(null, filterState, type, force, randomize);
				return;
			}
		}
		else if(args.size()>1){
			if(args.get(0).equalsIgnoreCase("add")){
				if(!rmp.hasPermission("resourcemadness.filter.add")){
					rmp.sendMessage(RMText.noPermissionCommand);
					return;
				}
				force = ForceState.ADD;
				size+=1;
			}
			else if(args.get(0).equalsIgnoreCase("subtract")){
				if(!rmp.hasPermission("resourcemadness.filter.subtract")){
					rmp.sendMessage(RMText.noPermissionCommand);
					return;
				}
				force = ForceState.SUBTRACT;
				size+=1;
			}
			else if(args.get(0).equalsIgnoreCase("random")){
				if(filterState==FilterState.FILTER){
					if(!rmp.hasPermission("resourcemadness.filter.random")){
						rmp.sendMessage(RMText.noPermissionCommand);
						return;
					}
					randomize = RMHelper.getIntByString(args.get(1));
					if(randomize>0) size+=1;
				}
			}
			else if(args.get(0).equalsIgnoreCase("clear")){
				force = ForceState.CLEAR;
				size+=1;
			}
			if(args.size()>2){
				if(args.get(1).equalsIgnoreCase("random")){
					if(filterState==FilterState.FILTER){
						if(!rmp.hasPermission("resourcemadness.filter.random")){
							rmp.sendMessage(RMText.noPermissionCommand);
							return;
						}
						randomize = RMHelper.getIntByString(args.get(1));
						if(randomize>0) size+=2;
					}
				}
			}
		}
		if(force == ForceState.CLEAR){
			switch(filterState){
			case FILTER:
				if(!rmp.hasPermission("resourcemadness.filter.clear")){
					rmp.sendMessage(RMText.noPermissionCommand);
					return;
				}
				break;
			case REWARD:
				if(!rmp.hasPermission("resourcemadness.reward.clear")){
					rmp.sendMessage(RMText.noPermissionCommand);
					return;
				}
				break;
			case TOOLS:
				if(!rmp.hasPermission("resourcemadness.tools.clear")){
					rmp.sendMessage(RMText.noPermissionCommand);
					return;
				}
				break;
			}
		}
		if(args.size()>0){
			args = args.subList(size, args.size());
			String arg0 = args.get(0);
			type = getTypeFromArg(arg0);
			
			if(type!=null){
				switch(filterState){
				case FILTER:
					items = getItemsFromFilter(type);
					amount = getAmountFromFilterArg(arg0, items);
					if(amount.size()==0) items.clear();
					break;
				case REWARD: case TOOLS:
					listItems = getListItemsFromFilter(arg0, type);
					break;
				}
			}
			else{
				HashMap<Integer, Integer[]> hashItems = RMFilter.getItemsByStringArray(args, false);
				switch(filterState){
					case FILTER:
						items = Arrays.asList(hashItems.keySet().toArray(new Integer[hashItems.size()]));
						amount = Arrays.asList(hashItems.values().toArray(new Integer[hashItems.size()][]));
						break;
					case REWARD: case TOOLS:
						for(Integer id : hashItems.keySet()){
							byte data = 0;
							short durability = 0;
							ItemStack item = new ItemStack(id, hashItems.get(id)[0].intValue(), durability, data);
							listItems.add(item);
						}
						break;
				}
			}
			if((type==null)&&(items.size()==0)&&(listItems.size()==0)){
				return;
			}
			HashMap<Integer, RMItem> rmItems = new HashMap<Integer, RMItem>();
			switch(filterState){
				case FILTER:
					for(int i=0; i<items.size(); i++){
						//hashItems.put(items.get(i), amount.get(i));
						
						Integer iItem = items.get(i);
						Integer[] iAmount = amount.get(i);
						int amount1 = -1;
						int amount2 = -1;
						if(iAmount.length>0) amount1 = iAmount[0];
						if(iAmount.length>1) amount2 = iAmount[1];
						
						RMItem rmItem = new RMItem(iItem);
						if(amount1 > -1) rmItem.setAmount(amount1);
						if(amount2 > -1) rmItem.setAmountHigh(amount2);
						
						rmItems.put(items.get(i), rmItem);
					}
					break;
				case REWARD: case TOOLS:
					for(ItemStack item : listItems){
						rmItems.put(item.getTypeId(), new RMItem(item));
					}
					break;
			}
			//rmp.setRequestFilter(hashItems, type, force);
			rmp.setRequestFilter(rmItems, filterState, type, force, randomize);
		}
	}
	
	public List<Integer[]> getDefaultAmount(List<Integer> items){
		List<Integer[]> amount = new ArrayList<Integer[]>();
		if(items==null) return null;
		else{
			for(Integer item : items){
				Material mat = Material.getMaterial(item);
				Integer[] intAmount = new Integer[1];
				intAmount[0] = mat.getMaxStackSize();
				amount.add(intAmount);
			}
		}
		return amount;
	}
	
	public List<Integer> getItemsFromFilter(FilterType type){
		List<Material> materials = Arrays.asList(Material.values());
		List<Integer> items = new ArrayList<Integer>();
		switch(type){
		case ALL:
			for(Material mat : materials) items.add(mat.getId());
			return items;
		case BLOCK:
			for(Material mat : materials) if(mat.isBlock()) items.add(mat.getId());
			return items;
		case ITEM:
			for(Material mat : materials) if(mat!=Material.AIR) if(!mat.isBlock()) items.add(mat.getId());
			return items;
		case RAW:
			return items;
		case CRAFTED:
			return items;
		}
		return items;
	}
	
	public void sendListById(String arg, RMPlayer rmp){
		int id = RMHelper.getIntByString(arg);
		sendListByInt(id, rmp);
	}
	public void sendListByInt(int id, RMPlayer rmp){
		int listLimit = 5;
		HashMap<Integer, RMGame> rmGames = RMGame.getGames();
		if(rmGames.size()==0){
			rmp.sendMessage("No games yet");
			return;
		}
		if(id<1) id=1;
		int size = (int)Math.ceil((double)rmGames.size()/(double)listLimit);
		if(id>size) id=1;
		int i=(id-1)*listLimit;
		rmp.sendMessage(ChatColor.GOLD+"/rm list "+ChatColor.GRAY+"(Page "+(id)+" of " +size+")");
		HashMap<Integer, String> hashGames = new HashMap<Integer, String>();
		HashMap<Integer, String> hashPlayers = new HashMap<Integer, String>();
		HashMap<Integer, String> hashTeams = new HashMap<Integer, String>();
		while(i<id*listLimit){
			RMGame rmGame = rmGames.get(i);
			if(rmGame==null) break;
			int gameId = rmGame.getConfig().getId();
			hashGames.put(gameId, ChatColor.AQUA+RMText.firstLetterToUpperCase(rmGame.getConfig().getWorldName())+ChatColor.WHITE+" Id: "+ChatColor.YELLOW+rmGame.getConfig().getId()+ChatColor.WHITE+" "+"Owner: "+ChatColor.YELLOW+rmGame.getConfig().getOwnerName()+ChatColor.WHITE+" TimeLimit: "+rmGame.getTextTimeLimit());
			hashPlayers.put(gameId, "Players: "+ChatColor.GREEN+rmGame.getTeamPlayers().length+ChatColor.WHITE+" inGame: "+rmGame.getTextMinPlayers()+ChatColor.WHITE+"-"+rmGame.getTextMaxPlayers()+ChatColor.WHITE+" inTeam: "+rmGame.getTextMinTeamPlayers()+ChatColor.WHITE+"-"+rmGame.getTextMaxTeamPlayers());
			hashTeams.put(gameId, "Teams: "+rmGame.getTextTeamColors());
			i++;
		}
		Integer[] gameIds = hashGames.keySet().toArray(new Integer[hashGames.size()]);
		Arrays.sort(gameIds);
		for(Integer gameId : gameIds){
			rmp.sendMessage(hashGames.get(gameId));
			rmp.sendMessage(hashPlayers.get(gameId));
			rmp.sendMessage(hashTeams.get(gameId));
		}
	}
	
	public void rmInfo(RMPlayer rmp, int page){
		int pageLimit = 2;
		if(page<=0) page = 1;
		if(page>pageLimit) page = pageLimit;
		rmp.sendMessage(ChatColor.GOLD+"ResourceMadness "+ChatColor.GRAY+"(Page "+page+" of "+pageLimit+")");
		rmp.sendMessage(ChatColor.GRAY+"Gray"+ChatColor.WHITE+"/"+ChatColor.GREEN+"green "+ChatColor.WHITE+"text is optional.");
		if(page==1){
			if(rmp.hasPermission("resourcemadness.add")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"add "+ChatColor.WHITE+"Create a new game.");
			if(rmp.hasPermission("resourcemadness.remove")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"remove "+ChatColor.WHITE+"Remove an existing game.");
			if(rmp.hasPermission("resourcemadness.list")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"list "+ChatColor.GREEN+"[page] "+ChatColor.WHITE+"List games.");
			
			//Info/Settings
			String line="";
			if(rmp.hasPermission("resourcemadness.info.found")) line+="found/";
			if(rmp.hasPermission("resourcemadness.info.items")) line+="items/";
			if(rmp.hasPermission("resourcemadness.info.reward")) line+="reward/";
			if(rmp.hasPermission("resourcemadness.info.tools")) line+="tools/";
			line = RMText.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"info "+ChatColor.GREEN+line+" "+ChatColor.WHITE+"Show "+line+".");
			
			if(rmp.hasPermission("resourcemadness.info.settings")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"settings "+ChatColor.GREEN+line+" "+ChatColor.WHITE+"Show settings.");
			
			if(rmp.hasPermission("resourcemadness.set")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"set "+ChatColor.WHITE+"Set various game related settings.");
			
			//Mode
			line="";
			if(rmp.hasPermission("resourcemadness.mode.filter")) line+="filter/";
			if(rmp.hasPermission("resourcemadness.mode.reward")) line+="reward/";
			if(rmp.hasPermission("resourcemadness.mode.tools")) line+="tools/";
			line = RMText.stripLast(line, "/");
			if(line.length()!=0) if(rmp.hasPermission("resourcemadness.mode")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"mode "+ChatColor.GREEN+line+" "+ChatColor.WHITE+"Change filter mode.");
			
			if(rmp.hasPermission("resourcemadness.filter")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.WHITE+"Add items to filter.");
			if(rmp.hasPermission("resourcemadness.reward")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"reward "+ChatColor.WHITE+"Add reward items.");
			if(rmp.hasPermission("resourcemadness.tools")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"tools "+ChatColor.WHITE+"Add tools items.");
		}
		else if(page==2){
			if(rmp.hasPermission("resourcemadness.start")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"start "+ChatColor.GREEN+"[amount] "+ChatColor.WHITE+"Start a game. Randomize with "+ChatColor.GREEN+"amount"+ChatColor.WHITE+".");
			
			//Restart/Stop
			String line="";
			//if(rmp.hasPermission("resourcemadness.restart")) line+="Restart/";
			if(rmp.hasPermission("resourcemadness.stop")) line+="Stop/";
			line = RMText.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+line.toLowerCase()+" "+ChatColor.WHITE+line+" a game.");
			
			line = "";
			if(rmp.hasPermission("resourcemadness.pause")) line+="Pause/";
			if(rmp.hasPermission("resourcemadness.resume")) line+="Resume/";
			if(line.length()!=0)  rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+line.toLowerCase()+" "+ChatColor.WHITE+line+" a game.");
			
			if(rmp.hasPermission("resourcemadness.restore")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"restore "+ChatColor.WHITE+"Restore game world changes.");
			if(rmp.hasPermission("resourcemadness.join")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"join "+ChatColor.GREEN+"[team(id/color)] "+ChatColor.WHITE+"Join a team.");
			if(rmp.hasPermission("resourcemadness.quit")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"quit "+ChatColor.WHITE+"Quit a team.");
			if(rmp.hasPermission("resourcemadness.ready")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"ready "+ChatColor.WHITE+"Ready yourself.");
			if(rmp.hasPermission("resourcemadness.items")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"items "+ChatColor.WHITE+"Get which items you need to gather.");
			if(rmp.hasPermission("resourcemadness.item")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"item "+ChatColor.AQUA+"[items(id/name)] "+ChatColor.WHITE+"Get the item's name or id");
			
			line = "";
			if(rmp.hasPermission("resourcemadness.claim.found")) line+="found/";
			if(rmp.hasPermission("resourcemadness.claim.items")) line+="items/";
			if(rmp.hasPermission("resourcemadness.claim.reward")) line+="reward/";
			if(rmp.hasPermission("resourcemadness.claim.tools")) line+="tools/";
			if(rmp.hasPermission("resourcemadness.claim")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"claim "+line+" "+ChatColor.GREEN+"chest "+ChatColor.WHITE+"Claim "+line.toLowerCase()+".");
		}
	}
	
	public void rmSetInfo(RMPlayer rmp, int page){
		if(rmp.hasPermission("resourcemadness.set")){
			int pageLimit = 2;
			if(page<=0) page = 1;
			if(page>pageLimit) page = pageLimit;
			rmp.sendMessage(ChatColor.GOLD+"/rm set "+ChatColor.GRAY+"(Page "+page+" of "+pageLimit+")");
			if(page==1){
				if(rmp.hasPermission("resourcemadness.set.minplayers")) if(!config.getLock().contains(Lock.minPlayers)) rmp.sendMessage(ChatColor.YELLOW+"min "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set min players.");
				if(rmp.hasPermission("resourcemadness.set.maxplayers")) if(!config.getLock().contains(Lock.maxPlayers)) rmp.sendMessage(ChatColor.YELLOW+"max "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set max players.");
				if(rmp.hasPermission("resourcemadness.set.minteamplayers")) if(!config.getLock().contains(Lock.minTeamPlayers)) rmp.sendMessage(ChatColor.YELLOW+"minteam "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set min team players.");
				if(rmp.hasPermission("resourcemadness.set.maxteamplayers")) if(!config.getLock().contains(Lock.maxTeamPlayers)) rmp.sendMessage(ChatColor.YELLOW+"maxteam "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set max team players.");
				if(rmp.hasPermission("resourcemadness.set.timelimit")) if(!config.getLock().contains(Lock.timeLimit)) rmp.sendMessage(ChatColor.YELLOW+"timelimit "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+RMText.timeLimit+".");
				if(rmp.hasPermission("resourcemadness.set.random")) if(!config.getLock().contains(Lock.autoRandomizeAmount)) rmp.sendMessage(ChatColor.YELLOW+"random "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Randomly pick "+ChatColor.GREEN+"amount "+ChatColor.WHITE+"of items every match.");
				if(rmp.hasPermission("resourcemadness.set.advertise")) if(!config.getLock().contains(Lock.advertise)) rmp.sendMessage(ChatColor.YELLOW+"advertise "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.advertise+".");
				if(rmp.hasPermission("resourcemadness.set.restore")) if(!config.getLock().contains(Lock.autoRestoreWorld)) rmp.sendMessage(ChatColor.YELLOW+"restore "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.autoRestoreWorld+".");
				if(rmp.hasPermission("resourcemadness.set.warp")) if(!config.getLock().contains(Lock.warpToSafety)) rmp.sendMessage(ChatColor.YELLOW+"warp "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.warpToSafety+".");
				if(rmp.hasPermission("resourcemadness.set.midgamejoin")) if(!config.getLock().contains(Lock.allowMidgameJoin)) rmp.sendMessage(ChatColor.YELLOW+"midgamejoin "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.allowMidgameJoin+".");
				if(rmp.hasPermission("resourcemadness.set.healplayer")) if(!config.getLock().contains(Lock.healPlayer)) rmp.sendMessage(ChatColor.YELLOW+"healplayer "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.healPlayer+".");
				if(rmp.hasPermission("resourcemadness.set.clearinventory")) if(!config.getLock().contains(Lock.clearPlayerInventory)) rmp.sendMessage(ChatColor.YELLOW+"clearinventory "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.clearPlayerInventory+".");
				if(rmp.hasPermission("resourcemadness.set.warnunequal")) if(!config.getLock().contains(Lock.warnUnequal)) rmp.sendMessage(ChatColor.YELLOW+"warnunequal "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.warnUnequal+".");
				if(rmp.hasPermission("resourcemadness.set.allowunequal")) if(!config.getLock().contains(Lock.allowUnequal)) rmp.sendMessage(ChatColor.YELLOW+"allowunequal "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.allowUnequal+".");
			}
			else if(page==2){
				if(rmp.hasPermission("resourcemadness.set.warnhacked")) if(!config.getLock().contains(Lock.warnHackedItems)) rmp.sendMessage(ChatColor.YELLOW+"warnhacked "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.warnHackedItems+".");
				if(rmp.hasPermission("resourcemadness.set.allowhacked")) if(!config.getLock().contains(Lock.allowHackedItems)) rmp.sendMessage(ChatColor.YELLOW+"allowhacked "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.allowHackedItems+".");
				if(rmp.hasPermission("resourcemadness.set.infinitereward")) if(!config.getLock().contains(Lock.infiniteReward)) rmp.sendMessage(ChatColor.YELLOW+"infinitereward "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.infiniteReward+".");
				if(rmp.hasPermission("resourcemadness.set.infinitetools")) if(!config.getLock().contains(Lock.infiniteTools)) rmp.sendMessage(ChatColor.YELLOW+"infinitetools "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.infiniteTools+".");
			}
		}
	}
	
	public void rmFilterInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.filter")){
			rmp.sendMessage(ChatColor.GOLD+"/rm filter");
			if(rmp.hasPermission("resourcemadness.filter.set")) rmp.sendMessage(ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.filter.random")) rmp.sendMessage(ChatColor.YELLOW+"random "+ChatColor.GREEN+"[amount] "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.filter.add")) rmp.sendMessage(ChatColor.YELLOW+"add "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.filter.subtract")) rmp.sendMessage(ChatColor.YELLOW+"subtract "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(ChatColor.YELLOW+"clear "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item");
			rmp.sendMessage(ChatColor.GOLD+"Examples:");
			if(rmp.hasPermission("resourcemadness.filter.set")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":stack "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32");
			if(rmp.hasPermission("resourcemadness.filter.random")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"random "+ChatColor.GREEN+"20 "+ChatColor.YELLOW+"all"+ChatColor.BLUE+":100-200");
			if(rmp.hasPermission("resourcemadness.filter.add")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"add "+ChatColor.AQUA+"20-40,1,3"+ChatColor.BLUE+":20");
			if(rmp.hasPermission("resourcemadness.filter.subtract")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"subtract "+ChatColor.AQUA+"1-10,20,288");
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"clear "+ChatColor.AQUA+"1-100"+ChatColor.BLUE+":stack");
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"clear");
		}
	}
	
	public void rmRewardInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(ChatColor.GOLD+"/rm reward");
			if(rmp.hasPermission("resourcemadness.reward.set")) rmp.sendMessage(ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.reward.add")) rmp.sendMessage(ChatColor.YELLOW+"add "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.reward.subtract")) rmp.sendMessage(ChatColor.YELLOW+"subtract "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(ChatColor.YELLOW+"clear "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item");
			rmp.sendMessage(ChatColor.GOLD+"Examples:");
			if(rmp.hasPermission("resourcemadness.reward.set")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"reward "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":stack "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32");
			if(rmp.hasPermission("resourcemadness.reward.add")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"reward "+ChatColor.YELLOW+"add "+ChatColor.AQUA+"20-40,1,3"+ChatColor.BLUE+":20");
			if(rmp.hasPermission("resourcemadness.reward.subtract")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"reward "+ChatColor.YELLOW+"subtract "+ChatColor.AQUA+"1-10,20,288");
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"reward "+ChatColor.YELLOW+"clear "+ChatColor.AQUA+"1-100"+ChatColor.BLUE+":stack");
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"reward "+ChatColor.YELLOW+"clear");
		}
	}
	
	public void rmToolsInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(ChatColor.GOLD+"/rm tools");
			if(rmp.hasPermission("resourcemadness.tools.set")) rmp.sendMessage(ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.tools.add")) rmp.sendMessage(ChatColor.YELLOW+"add "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.tools.subtract")) rmp.sendMessage(ChatColor.YELLOW+"subtract "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(ChatColor.YELLOW+"clear "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item");
			rmp.sendMessage(ChatColor.GOLD+"Examples:");
			if(rmp.hasPermission("resourcemadness.tools.set")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"tools "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":stack "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32");
			if(rmp.hasPermission("resourcemadness.tools.add")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"tools "+ChatColor.YELLOW+"add "+ChatColor.AQUA+"20-40,1,3"+ChatColor.BLUE+":20");
			if(rmp.hasPermission("resourcemadness.tools.subtract")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"tools "+ChatColor.YELLOW+"subtract "+ChatColor.AQUA+"1-10,20,288");
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"tools "+ChatColor.YELLOW+"clear "+ChatColor.AQUA+"1-100"+ChatColor.BLUE+":stack");
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"tools "+ChatColor.YELLOW+"clear");
		}
	}
	
	public void rmClaimInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.claim")){
			rmp.sendMessage(ChatColor.GOLD+"/rm claim");
			if(rmp.hasPermission("resourcemadness.claim.found")) rmp.sendMessage(ChatColor.YELLOW+"claim found "+ChatColor.GREEN+"chest "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.claim.items")) rmp.sendMessage(ChatColor.YELLOW+"items "+ChatColor.GREEN+"chest "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.claim.reward")) rmp.sendMessage(ChatColor.YELLOW+"reward "+ChatColor.GREEN+"chest "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			if(rmp.hasPermission("resourcemadness.claim.tools")) rmp.sendMessage(ChatColor.YELLOW+"tools "+ChatColor.GREEN+"chest "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item"+ChatColor.BLUE+":[amount/stack]");
			rmp.sendMessage(ChatColor.GOLD+"Examples:");
			if(rmp.hasPermission("resourcemadness.claim.found")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"claim found");
			if(rmp.hasPermission("resourcemadness.claim.found.chest")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"claim found "+ChatColor.GREEN+"chest "+ChatColor.AQUA+"10-20,22,24");
			if(rmp.hasPermission("resourcemadness.claim.items")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"claim items "+ChatColor.YELLOW+"block"+ChatColor.BLUE+":64");
			if(rmp.hasPermission("resourcemadness.claim.items.chest")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"claim items "+ChatColor.GREEN+"chest");
			if(rmp.hasPermission("resourcemadness.claim.reward")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"claim reward");
			if(rmp.hasPermission("resourcemadness.claim.reward.chest")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"claim reward "+ChatColor.GREEN+"chest "+ChatColor.AQUA+"50-100,200-300"+ChatColor.BLUE+":100"+ChatColor.AQUA+" 1,3,4"+ChatColor.BLUE+":10");
			if(rmp.hasPermission("resourcemadness.claim.tools")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"claim tools "+ChatColor.AQUA+"1-10,20,288"+ChatColor.BLUE+":stack");
			if(rmp.hasPermission("resourcemadness.claim.tools.chest")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"claim tools "+ChatColor.GREEN+"chest");
		}
	}
	
	public int getListItemStackTotalStack(List<ItemStack> items){
		int total = 0;
		for(ItemStack is : items){
			ItemStack item = is.clone();
			Material mat = item.getType();
			while(item.getAmount()>mat.getMaxStackSize()){
				total++;
				item.setAmount(item.getAmount()-mat.getMaxStackSize());
			}
			if(item.getAmount()<=mat.getMaxStackSize()){
				total++;
			}
		}
		return total;
	}
	
	public int getListItemStackTotal(List<ItemStack> items){
		int total = 0;
		for(ItemStack is : items){
			ItemStack item = is.clone();
			Material mat = item.getType();
			while(item.getAmount()>mat.getMaxStackSize()){
				total+=mat.getMaxStackSize();
				item.setAmount(item.getAmount()-mat.getMaxStackSize());
			}
			if(item.getAmount()<=mat.getMaxStackSize()){
				total+=item.getAmount();
			}
		}
		return total;
	}
}