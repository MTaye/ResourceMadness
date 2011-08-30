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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.*;

import org.bukkit.permissions.Permission;

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
	//private RMLogListener logListener = new RMLogListener(this);
	
	public static enum ClaimType { ITEMS, FOUND, REWARD, TOOLS, CHEST };
	public static enum DataType { CONFIG, STATS, PLAYER, GAME, LOG };
	
	private RMWatcher watcher;
	private int watcherid;
	//private RMInventoryListener inventoryListener = new RMPlayerListener(this);
	
	public PermissionHandler permissions = null;
	public PermissionManager permissionsEx = null;
	
	RMLogHelper rmLogHelper;
	
	public RM(){
		RMPlayer.plugin = this;
		RMGame.plugin = this;
	}

	public void onEnable(){
		log = getServer().getLogger();
	
		//setupPermissions();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
		//pm.registerEvent(Type.CUSTOM_EVENT, logListener, Priority.Normal, this);

		pdfFile = this.getDescription();
		log.log(Level.INFO, pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled!" );
		//RMConfig.load();
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
		
		rmLogHelper = new RMLogHelper(this);
	}
	
	public void onDisable(){
		saveAll();
		getServer().getScheduler().cancelTask(watcherid);
		log.info(pdfFile.getName() + " disabled");
		//RMConfig.save();
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
	
	public boolean hasPermission(Player player, String node, boolean result){
		if((permissions==null)&&(permissionsEx==null)) return result;
		if(player==null) return false;
		else{
			switch(config.getPermissionType()){
				case P3:
					if((permissions.has(player, "resourcemadness.admin"))||(permissions.has(player, "*"))) return true;
					else return permissions.has(player, node);
				case PEX:
					if((permissionsEx.has(player, "resourcemadness.admin"))||(permissionsEx.has(player, "*"))) return true;
					else return permissionsEx.has(player, node);
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
						rmInfo(rmp);
					}
					else{
						RMGame rmGame = null;
						String[] argsItems = args.clone();
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
							rmp.sendMessage("Left click a game block to create your new game.");
							return true;
						}
						//REMOVE
						else if(args[0].equalsIgnoreCase("remove")){
							if(!rmp.hasPermission("resourcemadness.remove")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) RMGame.tryRemoveGame(rmGame, rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.REMOVE);
								rmp.sendMessage("Left click a game block to remove your game.");
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
						//INFO
						else if(args[0].equalsIgnoreCase("info")){
							if(!rmp.hasPermission("resourcemadness.info")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length==2){
								if(args[1].equalsIgnoreCase("settings")){
									if(!rmp.hasPermission("resourcemadness.info.settings")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(rmGame!=null) rmGame.sendInfo(rmp);
									else{
										rmp.setPlayerAction(PlayerAction.INFO_SETTINGS);
										rmp.sendMessage("Left click a game block to get settings.");
									}
									return true;
								}
								if(args[1].equalsIgnoreCase("found")){
									if(!rmp.hasPermission("resourcemadness.info.found")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(rmGame!=null) rmGame.getInfoFound(rmp);
									else{
										rmp.setPlayerAction(PlayerAction.INFO_FOUND);
										rmp.sendMessage("Left click a game block to get settings.");
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
									rmp.sendMessage("Left click a game block to get info.");
								}
								return true;
							}
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
										rmp.sendMessage("Left click a game block to change the interface mode to filter.");
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
										rmp.sendMessage("Left click a game block to change the interface mode to reward.");
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
										rmp.sendMessage("Left click a game block to change the interface mode to tools.");
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.cycleMode(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.MODE_CYCLE);
									rmp.sendMessage("Left click a game block to cycle the interface mode.");
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
								rmp.sendMessage("Left click a team block to join the team.");
								return true;
							}
						}
						//QUIT
						else if(args[0].equalsIgnoreCase("quit")){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(RMText.noPermissionCommand);
							for(RMTeam rmTeam : RMTeam.getTeams()){
								RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
								if(rmPlayer!=null){
									rmTeam.removePlayer(rmPlayer);
									return true;
								}
							}
							rmp.sendMessage("You did not join any team yet.");
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
										rmp.sendMessage("Left click a game block to start the game.");
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.startGame(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.START);
									rmp.sendMessage("Left click a game block to start the game.");
								}
								return true;
							}
						}
						//RESTART
						else if(args[0].equalsIgnoreCase("restart")){
							if(!rmp.hasPermission("resourcemadness.restart")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) rmGame.restartGame(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTART);
								rmp.sendMessage("Left click a game block to restart the game.");
							}
							return true;
						}
						//STOP
						else if(args[0].equalsIgnoreCase("stop")){
							if(!rmp.hasPermission("resourcemadness.stop")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) rmGame.stopGame(rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.STOP);
								rmp.sendMessage("Left click a game block to stop the game.");
							}
							return true;
						}
						//RESTORE WORLD
						else if(args[0].equalsIgnoreCase("restore")){
							if(!rmp.hasPermission("resourcemadness.restore")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(rmGame!=null) rmGame.restoreWorld(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTORE);
								rmp.sendMessage("Left click a game block to restore world changes.");
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
										rmGame.tryParseFilter(rmp);
										return true;
									}
									else{
										parseFilter(rmp, listArgs, filterState);
										switch(filterState){
											case FILTER:
												rmp.setPlayerAction(PlayerAction.FILTER);
												rmp.sendMessage("Left click a game block to modify the filter.");
												break;
											case REWARD:
												rmp.setPlayerAction(PlayerAction.REWARD);
												rmp.sendMessage("Left click a game block to modify the reward.");
												break;
											case TOOLS:
												rmp.setPlayerAction(PlayerAction.TOOLS);
												rmp.sendMessage("Left click a game block to modify the tools.");
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
						//CLAIM
						else if(args[0].equalsIgnoreCase("claim")){
							if(!rmp.hasPermission("resourcemadness.claim")) return rmp.sendMessage(RMText.noPermissionCommand);
							if(args.length==2){
								if(args[1].equalsIgnoreCase("found")){
									if(!rmp.hasPermission("resourcemadness.claim.found")) return rmp.sendMessage(RMText.noPermissionCommand);
									if(rmGame!=null){
										rmGame.claimFound(rmp);
									}
									else{
										rmp.setPlayerAction(PlayerAction.CLAIM_FOUND);
										rmp.sendMessage("Left click a game block to claim found items.");
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase("items")){
									if(!rmp.hasPermission("resourcemadness.claim.items")) return rmp.sendMessage(RMText.noPermissionCommand);
									RMTeam rmTeam = rmp.getTeam();
									if((rmTeam==null)||((rmTeam!=null)&&(rmTeam.getGame().getConfig().getState()==GameState.SETUP))){
										rmp.claimItems();
									}
									else rmp.sendMessage("You can't claim your items while you're in-game.");
									return true;
								}
								else if(args[1].equalsIgnoreCase("reward")){
									if(!rmp.hasPermission("resourcemadness.claim.reward")) return rmp.sendMessage(RMText.noPermissionCommand);
									RMTeam rmTeam = rmp.getTeam();
									if((rmTeam==null)||((rmTeam!=null)&&(rmTeam.getGame().getConfig().getState()==GameState.SETUP))){
										rmp.claimReward();
									}
									else rmp.sendMessage("You can't claim your reward while you're in-game.");
									return true;
								}
								else if(args[1].equalsIgnoreCase("tools")){
									if(!rmp.hasPermission("resourcemadness.claim.tools")) return rmp.sendMessage(RMText.noPermissionCommand);
									rmp.claimTools();
									return true;
								}
							}
						}
						//SET
						else if(args[0].equalsIgnoreCase("set")){
							if(!rmp.hasPermission("resourcemadness.set")) return rmp.sendMessage(RMText.noPermissionCommand);
							
							if(args.length>1){
								PlayerAction action = null;
								//MIN PLAYERS
								if(args[1].equalsIgnoreCase("minplayers")){
									if(!rmp.hasPermission("resourcemadness.set.minplayers")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MIN_PLAYERS;
								}
								//MAX PLAYERS
								else if(args[1].equalsIgnoreCase("maxplayers")){
									if(!rmp.hasPermission("resourcemadness.set.maxplayers")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MAX_PLAYERS;
								}
								//MIN TEAM PLAYERS
								else if(args[1].equalsIgnoreCase("minteamplayers")){
									if(!rmp.hasPermission("resourcemadness.set.minteamplayers")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MIN_TEAM_PLAYERS;
								}
								//MAX TEAM PLAYERS
								else if(args[1].equalsIgnoreCase("maxteamplayers")){
									if(!rmp.hasPermission("resourcemadness.set.maxteamplayers")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MAX_TEAM_PLAYERS;
								}
								//MAX ITEMS
								else if(args[1].equalsIgnoreCase("maxitems")){
									if(!rmp.hasPermission("resourcemadness.set.maxitems")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MAX_ITEMS;
								}
								//AUTO RANDOM ITEMS
								else if(args[1].equalsIgnoreCase("random")){
									if(!rmp.hasPermission("resourcemadness.set.random")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_RANDOM;
								}
								
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
													case SET_RANDOM: rmGame.setRandomizeAmount(rmp, amount); break;
												}
												return true;
											}
											else{
												rmp.setRequestInt(RMHelper.getIntByString(args[2]));
												switch(action){
													case SET_MIN_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MIN_PLAYERS);
														rmp.sendMessage("Left click a game block to set min players.");
														break;
													case SET_MAX_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_PLAYERS);
														rmp.sendMessage("Left click a game block to set max players.");
														break;
													case SET_MIN_TEAM_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MIN_TEAM_PLAYERS);
														rmp.sendMessage("Left click a game block to set min team players.");
														break;
													case SET_MAX_TEAM_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_TEAM_PLAYERS);
														rmp.sendMessage("Left click a game block to set max team players.");
														break;
													case SET_MAX_ITEMS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_ITEMS);
														rmp.sendMessage("Left click a game block to set max items.");
														break;
													case SET_RANDOM:
														rmp.setPlayerAction(PlayerAction.SET_RANDOM);
														rmp.sendMessage("Left click a game block to set auto randomize items.");
														break;
												}
												return true;
											}
										}
									}
								}
								
								//SET & TOGGLE
								action = null;
								//GATHER PLAYERS
								if(args[1].equalsIgnoreCase("warp")){
									if(!rmp.hasPermission("resourcemadness.set.warp")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_WARP;
								}
								//AUTO RESTORE WORLD
								else if(args[1].equalsIgnoreCase("restore")){
									if(!rmp.hasPermission("resourcemadness.set.restore")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_RESTORE;
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
								//ALLOW PLAYER LEAVE
								else if(args[1].equalsIgnoreCase("keepingame")){
									if(!rmp.hasPermission("resourcemadness.set.keepingame")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_KEEP_INGAME;
								}
								//ALLOW MIDGAME JOIN
								else if(args[1].equalsIgnoreCase("midgamejoin")){
									if(!rmp.hasPermission("resourcemadness.set.random")) return rmp.sendMessage(RMText.noPermissionCommand);
									action = PlayerAction.SET_MIDGAME_JOIN;
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
											case SET_WARP: rmGame.setWarpToSafety(rmp, i); break;
											case SET_RESTORE: rmGame.setAutoRestoreWorld(rmp, i); break;
											case SET_WARN_HACKED: rmGame.setWarnHackedItems(rmp, i); break;
											case SET_ALLOW_HACKED: rmGame.setAllowHackedItems(rmp, i); break;
											case SET_KEEP_INGAME: rmGame.setKeepIngame(rmp, i); break;
											case SET_MIDGAME_JOIN: rmGame.setAllowMidgameJoin(rmp, i); break;
											case SET_CLEAR_INVENTORY: rmGame.setClearPlayerInventory(rmp, i); break;
											case SET_WARN_UNEQUAL: rmGame.setWarnUnequal(rmp, i); break;
											case SET_ALLOW_UNEQUAL: rmGame.setAllowUnequal(rmp, i); break;
											case SET_INFINITE_REWARD: rmGame.setInfiniteReward(rmp, i); break;
											case SET_INFINITE_TOOLS: rmGame.setInfiniteTools(rmp, i); break;
										}
									}
									else{
										rmp.setRequestInt(i);
										switch(action){
											case SET_WARP:
												rmp.setPlayerAction(PlayerAction.SET_WARP);
												rmp.sendMessage("Left click a game block to toggle teleport players.");
												break;
											case SET_RESTORE:
												rmp.setPlayerAction(PlayerAction.SET_RESTORE);
												rmp.sendMessage("Left click a game block to toggle auto restore world.");
												break;
											case SET_WARN_HACKED:
												rmp.setPlayerAction(PlayerAction.SET_WARN_HACKED);
												rmp.sendMessage("Left click a game block to toggle warn hacked items.");
												break;
											case SET_ALLOW_HACKED:
												rmp.setPlayerAction(PlayerAction.SET_ALLOW_HACKED);
												rmp.sendMessage("Left click a game block to toggle allow hacked items.");
												break;
											case SET_KEEP_INGAME:
												rmp.setPlayerAction(PlayerAction.SET_KEEP_INGAME);
												rmp.sendMessage("Left click a game block to toggle allow player leave.");
												break;
											case SET_MIDGAME_JOIN:
												rmp.setPlayerAction(PlayerAction.SET_MIDGAME_JOIN);
												rmp.sendMessage("Left click a game block to toggle allow midgame join.");
												break;
											case SET_CLEAR_INVENTORY:
												rmp.setPlayerAction(PlayerAction.SET_CLEAR_INVENTORY);
												rmp.sendMessage("Left click a game block to toggle clear player inventory.");
												break;
											case SET_WARN_UNEQUAL:
												rmp.setPlayerAction(PlayerAction.SET_WARN_UNEQUAL);
												rmp.sendMessage("Left click a game block to toggle warn unequal items.");
												break;
											case SET_ALLOW_UNEQUAL:
												rmp.setPlayerAction(PlayerAction.SET_ALLOW_UNEQUAL);
												rmp.sendMessage("Left click a game block to toggle allow unequal items.");
												break;
											case SET_INFINITE_REWARD:
												rmp.setPlayerAction(PlayerAction.SET_INFINITE_REWARD);
												rmp.sendMessage("Left click a game block to toggle infinite reward.");
												break;
											case SET_INFINITE_TOOLS:
												rmp.setPlayerAction(PlayerAction.SET_INFINITE_TOOLS);
												rmp.sendMessage("Left click a game block to toggle infinite tools.");
												break;
										}
									}
									return true;
								}
							}
							rmSetInfo(rmp);
							return true;
						}
						//Get Item NAME by ID or Item ID by NAME
						else if(rmp.hasPermission("resourcemadness.iteminfo")){
							List<String> items = new ArrayList<String>();
							List<String> itemsWarn = new ArrayList<String>();
							for(String str : argsItems){
								String[] strItems = str.split(",");
								for(String strItem : strItems){
									for(Material mat : Material.values()){
										if(strItem.equalsIgnoreCase(mat.name())){
											if(!items.contains(mat)) items.add(ChatColor.WHITE+mat.name()+":"+ChatColor.YELLOW+mat.getId());
										}
									}
									if(strItem.contains("-")){
										String[] strItems2 = strItem.split("-");
										int id1=RMHelper.getIntByString(strItems2[0]);
										int id2=RMHelper.getIntByString(strItems2[1]);
										if((id1!=-1)&&(id2!=-1)){
											if(id1>id2){
												int id3=id1;
												id1=id2;
												id2=id3;
											}
											while(id1<=id2){
												Material mat = Material.getMaterial(id1);
												if(mat!=null){
													if(!items.contains(mat)) items.add(""+ChatColor.WHITE+id1+":"+ChatColor.YELLOW+Material.getMaterial(id1).name());
												}
												else if(!itemsWarn.contains(strItem)) itemsWarn.add(""+id1);
												id1++;
											}
										}
									}
									else{
										int id = RMHelper.getIntByString(strItem);
										if(id!=-1){
											Material mat = Material.getMaterial(id);
											if(mat!=null){
												if(!items.contains(mat)) items.add(""+ChatColor.WHITE+id+":"+ChatColor.YELLOW+Material.getMaterial(id).name());
											}
											else if(!itemsWarn.contains(strItem)) itemsWarn.add(""+id);
										}
									}
								}
							}
							if(items.size()>0){
								rmp.sendMessage(RMText.getFormattedStringByList(items));
								return true;
							}
							else if(itemsWarn.size()>0){
								rmp.sendMessage("These items don't exist!");
								//rmp.sendMessage("These items don't exist: "+getFormattedStringByList(itemsWarn));
								return true;
							}
						}
						rmInfo(rmp);
					}
				}
			}
		}
		return true;
	}
	
	public void saveAllBackup(){
		log.log(Level.INFO, RMText.preLog+"Autosaving...");
		if(RMGame.getGames().size()==0) return;
		File folder = new File(getDataFolder()+"/backup");
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating backup directory...");
			folder.mkdir();
		}
		File file = new File(folder.getAbsolutePath()+"/config.txt");
		if(!file.exists()) save(DataType.CONFIG, false, file);
		save(DataType.STATS, false, new File(folder.getAbsolutePath()+"/stats.txt"));
		save(DataType.PLAYER, false, new File(folder.getAbsolutePath()+"/playerdata.txt"));
		save(DataType.GAME, false, new File(folder.getAbsolutePath()+"/gamedata.txt"));
		save(DataType.LOG, true, new File(folder.getAbsolutePath()+"/gamelogdata.txt"));
	}
	public void saveConfig(){
		File folder = getDataFolder();
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating config directory...");
			folder.mkdir();
		}
		File file = new File(folder.getAbsolutePath()+"/config.txt");
		if(!file.exists()) save(DataType.CONFIG, false, file);
	}
	public void saveAll(){
		if(RMGame.getGames().size()==0) return;
		File folder = getDataFolder();
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating config directory...");
			folder.mkdir();
		}
		File file = new File(folder.getAbsolutePath()+"/config.txt");
		if(!file.exists()) save(DataType.CONFIG, false, file);
		save(DataType.STATS, false, new File(folder.getAbsolutePath()+"/stats.txt"));
		save(DataType.PLAYER, false, new File(folder.getAbsolutePath()+"/playerdata.txt"));
		save(DataType.GAME, false, new File(folder.getAbsolutePath()+"/gamedata.txt"));
		save(DataType.LOG, true, new File(folder.getAbsolutePath()+"/gamelogdata.txt"));
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
			
			File folderBackup = new File(getDataFolder()+"/backup");
			if(!folderBackup.exists()){
				try{
					folderBackup.mkdir();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
			if(!RMHelper.copyFile(file, new File(folderBackup.getAbsolutePath()+"/"+file.getName()))){
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
					line+="useRestore="+config.getRestore()+"\n\n";
					line+=RMText.cServerWide+"\n\n";
					//Max games
					line+=RMText.cMaxGames+"\n";
					line+="maxGames="+config.getMaxGames()+"\n\n";
					//Max games per player
					line+=RMText.cMaxGamesPerPlayer+"\n";
					line+="maxGamesPerPlayer="+config.getMaxGamesPerPlayer()+"\n\n";
					//Min players per game
					line+=RMText.cMinPlayersPerGame+"\n";
					line+="minPlayersPerGame="+config.getMinPlayersPerGame()+"\n\n";
					//Max player per game
					line+=RMText.cMaxPlayersPerGame+"\n";
					line+="maxPlayersPerGame="+config.getMaxPlayersPerGame()+"\n\n";
					//Min players per team
					line+=RMText.cMinPlayersPerTeam+"\n";
					line+="minPlayersPerTeam="+config.getMinPlayersPerTeam()+"\n\n";
					//Max players per team
					line+=RMText.cMaxPlayersPerTeam+"\n";
					line+="maxPlayersPerTeam="+config.getMaxPlayersPerTeam()+"\n\n";
					//Default game settings
					line+=RMText.cDefaultSettings1+"\n";
					line+=RMText.cDefaultSettings2+"\n\n";
					//Auto restore world
					line+=RMText.cRestore+"\n";
					line+="restore="+config.getRestore()+(config.isLocked(Lock.restore)?":lock":"")+"\n\n";
					//Warp to safety
					line+=RMText.cWarpToSafety+"\n";
					line+="warpToSafety="+config.getWarpToSafety()+(config.isLocked(Lock.warpToSafety)?":lock":"")+"\n\n";
					//Warn when hacked items are added
					line+=RMText.cWarnHackedItems+"\n";
					line+="warnHackedItems="+config.getWarnHackedItems()+(config.isLocked(Lock.warnHackedItems)?":lock":"")+"\n\n";
					//Allow the use of hacked items
					line+=RMText.cAllowHackedItems+"\n";
					line+="allowHackedItems="+config.getAllowHackedItems()+(config.isLocked(Lock.allowHackedItems)?":lock":"")+"\n\n";
					//Keep offline players in-game
					line+=RMText.cKeepIngame+"\n";
					line+="keepingame="+config.getKeepIngame()+(config.isLocked(Lock.keepIngame)?":lock":"")+"\n\n";
					//Allow players to join a game in progress
					line+=RMText.cAllowMidgameJoin+"\n";
					line+="allowMidgameJoin="+config.getAllowMidgameJoin()+(config.isLocked(Lock.allowMidgameJoin)?":lock":"")+"\n\n";
					//Clear/return player's items at game start/finish
					line+=RMText.cClearPlayerInventory+"\n";
					line+="clearPlayerInventory="+config.getClearPlayerInventory()+(config.isLocked(Lock.clearPlayerInventory)?":lock":"")+"\n\n";
					//Warn when reward/tools can't be distributed equally
					line+=RMText.cWarnUnequal+"\n";
					line+="warnunequal="+config.getWarnUnequal()+(config.isLocked(Lock.warnUnequal)?":lock":"")+"\n\n";
					//Allow reward/tools to be distributed unequally
					line+=RMText.cAllowUnequal+"\n";
					line+="allowunequal="+config.getAllowUnequal()+(config.isLocked(Lock.allowUnequal)?":lock":"")+"\n\n";
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
						line = rmp.getName()+";";
						//Stats
						RMStats stats = rmp.getStats();
						line += stats.getWins()+","+stats.getLosses()+","+stats.getTimesPlayed()+","+/*stats.getItemsFound()+","+*/stats.getItemsFoundTotal()+";";
						//Inventory items
						line += RMInventoryHelper.encodeInventoryToString(rmp.getItems().toArray(new ItemStack[rmp.getItems().size()]), ClaimType.ITEMS) + ";";
						line += RMInventoryHelper.encodeInventoryToString(rmp.getReward().toArray(new ItemStack[rmp.getReward().size()]), ClaimType.REWARD) + ";";
						line += RMInventoryHelper.encodeInventoryToString(rmp.getTools().toArray(new ItemStack[rmp.getTools().size()]), ClaimType.TOOLS);
						bw.write(line);
						bw.write("\n");
					}
					break;
				case GAME:
					//bw.write("[Resource Madness v"+pdfFile.getVersion()+" Game Data]");
					for(RMGame rmGame : RMGame.getGames()){
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
						line += config.getMinPlayers()+",";
						line += config.getMaxPlayers()+",";
						line += config.getMinTeamPlayers()+",";
						line += config.getMaxTeamPlayers()+",";
						line += config.getAutoRandomizeAmount()+",";
						line += config.getWarpToSafety()+",";
						line += config.getAutoRestoreWorld()+",";
						line += config.getWarnHackedItems()+",";
						line += config.getAllowHackedItems()+",";
						line += config.getKeepIngame()+",";
						line += config.getAllowMidgameJoin()+",";
						line += config.getClearPlayerInventory()+",";
						line += config.getWarnUnequal()+",";
						line += config.getAllowUnequal();
						line += ";";
						//Stats
						RMStats stats = config.getGameStats();
						line += stats.getWins()+","+stats.getLosses()+","+stats.getTimesPlayed()+","+/*stats.getItemsFound()+","+*/stats.getItemsFoundTotal()+";";
						//Players
						for(RMTeam rmt : config.getTeams()){
							line+=rmt.getTeamColor().name()+":";
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
							line += RMInventoryHelper.encodeInventoryToString(rmt.getChest().getInventory(), ClaimType.CHEST)+".";
						}
						line = RMText.stripLast(line, ".");
						line += ";";
						//Team items
						for(RMTeam rmt : config.getTeams()){
							line += RMFilter.encodeFilterToString(rmt.getChest().getItems(), FilterState.ITEMS)+".";
						}
						line = RMText.stripLast(line, ".");
						bw.write(line);
						bw.write("\n");
					}
					break;
				case LOG:
					for(RMGame rmGame : RMGame.getGames()){
						line = "";
						//Log
						RMGameConfig config = rmGame.getConfig();
						line += rmLogHelper.encodeLogToString(config.getLog());
						bw.write(line);
						bw.write("\n");
					}
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
		load(DataType.PLAYER, false, true);
		load(DataType.GAME, false, true);
		load(DataType.LOG, true, true);
	}
	
	public void load(DataType dataType, boolean useLZF, boolean loadBackup){
		int lineNum = 0;
		File folder = getDataFolder();
		File file = null;
		switch(dataType){
			case CONFIG: file = new File(folder.getAbsolutePath()+"/config.txt"); break;
			case STATS: file = new File(folder.getAbsolutePath()+"/stats.txt"); break;
			case PLAYER: file = new File(folder.getAbsolutePath()+"/playerdata.txt"); break;
			case GAME: file = new File(folder.getAbsolutePath()+"/gamedata.txt"); break;
			case LOG: file = new File(folder.getAbsolutePath()+"/gamelogdata.txt"); break;
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
								else if(args[0].equalsIgnoreCase("minPlayersPerGame")) config.setMinPlayersPerGame(RMHelper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("maxPlayersPerGame")) config.setMaxPlayersPerGame(RMHelper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("minPlayersPerTeam")) config.setMinPlayersPerTeam(RMHelper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("maxPlayersPerTeam")) config.setMaxPlayersPerTeam(RMHelper.getIntByString(args[1]));
								else{
									boolean lockArg = args[1].substring(args[1].indexOf(":")+1).equalsIgnoreCase("lock")?true:false; 
									if(args[0].equalsIgnoreCase("restore")) config.setRestore(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("warpToSafety")) config.setWarpToSafety(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("warnHackedItems")) config.setWarnHackedItems(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("allowHackedItems")) config.setAllowHackedItems(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("keepIngame")) config.setKeepIngame(Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("allowMidgameJoin")) config.setAllowMidgameJoin(Boolean.parseBoolean(args[1]), lockArg);
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
				if(RMHelper.copyFile(new File(getDataFolder().getAbsolutePath()+"/backup/"+file.getName()), file)){
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
		RMPlayer rmp = new RMPlayer(strArgs[0]);
		
		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
		String[] args = strArgs[1].split(",");
		RMStats stats = rmp.getStats();
		
		stats.setWins(RMHelper.getIntByString(args[0]));
		stats.setLosses(RMHelper.getIntByString(args[1]));
		stats.setTimesPlayed(RMHelper.getIntByString(args[2]));
		//stats.setItemsFound(getIntByString(args[3]));
		stats.setItemsFoundTotal(RMHelper.getIntByString(args[3]));
		
		//inventory items
		if(strArgs[2].length()>0){
			if(!strArgs[2].equalsIgnoreCase("ITEMS")){
				rmp.setItems(RMInventoryHelper.getItemStackByStringArray(strArgs[2]));
			}
		}
		//reward items
		if(strArgs[3].length()>0){
			if(!strArgs[3].equalsIgnoreCase("REWARD")){
				rmp.setReward(RMInventoryHelper.getItemStackByStringArray(strArgs[3]));
			}
		}
		//tools items
		if(strArgs[4].length()>0){
			if(!strArgs[4].equalsIgnoreCase("TOOLS")){
				rmp.setTools(RMInventoryHelper.getItemStackByStringArray(strArgs[4]));
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

		//maxPlayers,maxTeamPlayers,autoRandomizeAmount
		//warpToSafety,autoRestoreWorld,warnHackedItems,allowHackedItems,allowPlayerLeave 
		RMGameConfig config = new RMGameConfig(this);
		config.setPartList(new RMPartList(b, this));
		
		config.setOwnerName(args[5]);
		
		config.setState(RMHelper.getStateByInt(RMHelper.getIntByString(args[6])));
		config.setInterface(RMHelper.getInterfaceByInt(RMHelper.getIntByString(args[7])));
		config.setMinPlayers(RMHelper.getIntByString(args[8]));
		config.setMaxPlayers(RMHelper.getIntByString(args[9]));
		config.setMinTeamPlayers(RMHelper.getIntByString(args[10]));
		config.setMaxTeamPlayers(RMHelper.getIntByString(args[11]));
		config.setAutoRandomizeAmount(RMHelper.getIntByString(args[12]));
		config.setWarpToSafety(Boolean.parseBoolean(args[13]));
		config.setAutoRestoreWorld(Boolean.parseBoolean(args[14]));
		config.setWarnHackedItems(Boolean.parseBoolean(args[15]));
		config.setAllowHackedItems(Boolean.parseBoolean(args[16]));
		config.setKeepIngame(Boolean.parseBoolean(args[17]));
		config.setAllowMidgameJoin(Boolean.parseBoolean(args[18]));
		config.setClearPlayerInventory(Boolean.parseBoolean(args[19]));
		config.setWarnUnequal(Boolean.parseBoolean(args[20]));
		config.setAllowUnequal(Boolean.parseBoolean(args[21]));
		

		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
		args = strArgs[1].split(",");
		RMStats gameStats = config.getGameStats();
		
		gameStats.setWins(RMHelper.getIntByString(args[0]));
		gameStats.setLosses(RMHelper.getIntByString(args[1]));
		gameStats.setTimesPlayed(RMHelper.getIntByString(args[2]));
		//gameStats.setItemsFound(getIntByString(args[3]));
		gameStats.setItemsFoundTotal(RMHelper.getIntByString(args[3]));
		
		//team players
		args = strArgs[2].split(" ");
		List<RMTeam> rmTeams = config.getPartList().fetchTeams();
		for(RMTeam rmt : rmTeams){
			config.getTeams().add(rmt);
		}
		for(int j=0; j<args.length; j++){
			String[] splitArgs = args[j].split(":");
			if(splitArgs.length==2){
				if(splitArgs[1].length()>0){
					String[] players = splitArgs[1].split(",");
					for(String player : players){
						RMTeam rmTeam = config.getTeams().get(j);
						if(rmTeam!=null){
							RMPlayer rmp = RMPlayer.getPlayerByName(player);
							if(rmp!=null) rmTeam.addPlayerSilent(rmp);
						}
					}
				}
			}
		}
			
		//filter items
		if(strArgs[3].length()>0){
			if(!strArgs[3].equalsIgnoreCase("FILTER")){
				HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(strArgs[3]), true);
				config.setFilter(new RMFilter(rmItems));
			}
		}
		//game items
		if(strArgs[4].length()>0){
			if(!strArgs[4].equalsIgnoreCase("ITEMS")){
				HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(strArgs[4]), true);
				config.setItems(new RMFilter(rmItems));
			}
		}
		//found items
		if(strArgs[5].length()>0){
			if(!strArgs[5].equalsIgnoreCase("FOUND")){
				config.setFound(RMInventoryHelper.getItemStackByStringArray(strArgs[5]));
			}
		}
		//reward items
		if(strArgs[6].length()>0){
			if(!strArgs[6].equalsIgnoreCase("REWARD")){
				config.setReward(RMInventoryHelper.getItemStackByStringArray(strArgs[6]));
			}
		}
		//tools items
		if(strArgs[7].length()>0){
			if(!strArgs[7].equalsIgnoreCase("TOOLS")){
				config.setTools(RMInventoryHelper.getItemStackByStringArray(strArgs[7]));
			}
		}
		//chest items
		args = strArgs[8].split("\\.");
		for(int j=0; j<args.length; j++){
			if(args[j].length()>0){
				if(!args[j].equalsIgnoreCase("CHEST")){
					List<ItemStack> items = RMInventoryHelper.getItemStackByStringArray(args[j]);
					List<ItemStack> inventory = new ArrayList<ItemStack>();
					for(ItemStack item : items){
						inventory.add(item);
					}
					RMTeam rmTeam = config.getTeams().get(j);
					if(rmTeam!=null){
						rmTeam.getChest().setInventory(inventory);
					}
				}
			}
		}
		
		//team items
		args = strArgs[9].split("\\.");
		for(int j=0; j<args.length; j++){
			if(args[j].length()>0){
				if(!args[j].equalsIgnoreCase("ITEMS")){
					HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(args[j]), true);
					RMTeam rmTeam = config.getTeams().get(j);
					if(rmTeam!=null){
						rmTeam.getChest().setItems(rmItems);
					}
				}
			}
		}
		RMGame.tryAddGameFromConfig(config);
	}
	
	public void parseFilter(RMPlayer rmp, List<String> args, FilterState filterState){
		int size = 0;
		List<Integer> items = new ArrayList<Integer>();
		List<Integer[]> amount = new ArrayList<Integer[]>();
		List<ItemStack> isItems = new ArrayList<ItemStack>();
		FilterType type = null;
		ForceState force = null;
		int randomize = 0;
		for(String arg : args){
			arg = arg.replace(" ", "");
		}
		
		if(args.size()>1){
			/*
			if(args.get(0).equalsIgnoreCase("add")){
				force = ForceState.ADD;
				size+=1;
			}
			*/
			if(filterState==FilterState.FILTER){
				if(args.get(0).equalsIgnoreCase("remove")){
					if(!rmp.hasPermission("resourcemadness.filter.remove")){
						rmp.sendMessage(RMText.noPermissionCommand);
						return;
					}
					force = ForceState.REMOVE;
					size+=1;
				}
				if(args.get(0).equalsIgnoreCase("random")){
					if(!rmp.hasPermission("resourcemadness.filter.random")){
						rmp.sendMessage(RMText.noPermissionCommand);
						return;
					}
					randomize = RMHelper.getIntByString(args.get(1));
					if(randomize>0){
						force = ForceState.RANDOMIZE;
						size+=2;
					}
				}
			}
		}
		if(args.size()>0){
			args = args.subList(size, args.size());
			String arg0 = args.get(0);
			if(arg0.contains("all")) type = FilterType.ALL;
			else if(arg0.contains("clear")) type = FilterType.CLEAR;
			else if(arg0.contains("block")) type = FilterType.BLOCK;
			else if(arg0.contains("item")) type = FilterType.ITEM;
			else if(arg0.contains("raw")) type = FilterType.RAW;
			else if(arg0.contains("crafted")) type = FilterType.CRAFTED;
			
			if(type!=null){
				switch(type){
					//CLEAR
					case CLEAR:
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
						break;
					//DEFAULT
					default:
						switch(filterState){
						case FILTER:
							boolean useDefaultAmount = false;
							items = getItemsFromFilter(type);
							amount.clear();
							if(arg0.contains("stack")) useDefaultAmount = true;
							else if(arg0.contains(":")){
								List<String> strArgs = RMFilter.splitArgsByColon(arg0);
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
							break;
						case REWARD: case TOOLS:
							items = getItemsFromFilter(type);
							for(Integer i : items){
								isItems.add(new ItemStack(i));
							}
							if(arg0.contains("stack")){
								for(ItemStack item : isItems){
									item.setAmount(item.getType().getMaxStackSize());
								}
							}
							else if(arg0.contains(":")){
								List<String> strArgs = RMFilter.splitArgsByColon(arg0);
								String strAmount = ""; 
								String[] strSplit = strArgs.get(0).split(":");
								if(strSplit.length>1){
									strAmount = strSplit[1];
									Integer[] intAmount = RMFilter.checkInt(strAmount);
									if(intAmount!=null){
										for(ItemStack item : isItems){
											//log.log(Level.WARNING, "amount:"+intAmount[0]);
											item.setAmount(intAmount[0]);
										}
									}
									else isItems.clear();
								}
							}
							else{
								Integer[] intAmount = new Integer[1];
								intAmount[0] = 1;
								for(ItemStack item : isItems){		
									//log.log(Level.WARNING, "amount:"+intAmount[0]);
									item.setAmount(intAmount[0]);
								}
							}
							break;
						}
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
							isItems.add(item);
						}
						break;
				}
			}
			if((type==null)&&(items.size()==0)&&(isItems.size()==0)){
				rmInfo(rmp);
				return;
			}
			//HashMap<Integer, Integer[]> hashItems = new HashMap<Integer, Integer[]>();
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
					/*
					HashMap<Integer, ItemStack> hashItems = combineItemsByItemStack(isItems.toArray(new ItemStack[isItems.size()]));
					for(Integer id : hashItems.keySet()){
						ItemStack item = hashItems.get(id);
						rmItems.put(id, new RMItem(item));
					}
					*/
					for(ItemStack item : isItems){
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
		int id = RMHelper.getIntByString(arg, 0);
		if(id<0) id=0;
		sendListByInt(id, rmp);
	}
	public void sendListByInt(int id, RMPlayer rmp){
		if(id<0) id=0;
		List<RMGame> rmGames = RMGame.getGames();
		if(rmGames.size()==0){
			rmp.sendMessage("No games yet");
			return;
		}
		int i=id*10;
		if(rmGames.size()>0) rmp.sendMessage("Page "+(id+1)+" of " +(int)(1+rmGames.size()/5));
		HashMap<Integer, String> hashGames = new HashMap<Integer, String>();
		while(i<rmGames.size()){
			RMGame rmGame = rmGames.get(i);
			hashGames.put(rmGame.getConfig().getId(), "Game: "+ChatColor.YELLOW+rmGame.getConfig().getId()+ChatColor.WHITE+" - "+"Owner: "+ChatColor.YELLOW+rmGame.getConfig().getOwnerName()+ChatColor.WHITE+" Teams: "+rmGame.getTextTeamPlayers());
			if(i==id*10+10) break;
			i++;
		}
		Integer[] gameIds = hashGames.keySet().toArray(new Integer[hashGames.size()]);
		Arrays.sort(gameIds);
		for(Integer gameId : gameIds){
			rmp.sendMessage(hashGames.get(gameId));
		}
	}
	
	public void rmInfo(RMPlayer rmp){
		rmp.sendMessage(ChatColor.GOLD+"ResourceMadness Commands:");
		rmp.sendMessage(ChatColor.GRAY+"Gray"+ChatColor.WHITE+"/"+ChatColor.GREEN+"green "+ChatColor.WHITE+"text is optional.");
		if(rmp.hasPermission("resourcemadness.add")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"add "+ChatColor.WHITE+"Create a new game.");
		if(rmp.hasPermission("resourcemadness.remove")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"remove "+ChatColor.WHITE+"Remove an existing game.");
		if(rmp.hasPermission("resourcemadness.list")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"list "+ChatColor.GRAY+"[page] "+ChatColor.WHITE+"List games.");
		
		//Info/Settings
		String line="";
		if(rmp.hasPermission("resourcemadness.info.settings")) line+="settings/";
		if(rmp.hasPermission("resourcemadness.info.found")) line+="found/";
		if(rmp.hasPermission("resourcemadness.info.items")) line+="items/";
		if(rmp.hasPermission("resourcemadness.info.reward")) line+="reward/";
		if(rmp.hasPermission("resourcemadness.info.tools")) line+="tools/";
		line = RMText.stripLast(line, "/");
		if(line.length()!=0) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"info "+ChatColor.GREEN+line+" "+ChatColor.WHITE+"Show "+line+".");
		
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
		if(rmp.hasPermission("resourcemadness.iteminfo")) rmp.sendMessage("/rm "+ChatColor.AQUA+"[items(id/name)] "+ChatColor.WHITE+"Get the item's name or id");
		if(rmp.hasPermission("resourcemadness.restore")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"restore "+ChatColor.WHITE+"Restore game world changes.");
		if(rmp.hasPermission("resourcemadness.start")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"start "+ChatColor.GREEN+"[amount] "+ChatColor.WHITE+"Start a game. Randomize with "+ChatColor.GREEN+"amount"+ChatColor.WHITE+".");
		
		//Restart/Stop
		line="";
		if(rmp.hasPermission("resourcemadness.restart")) line+="Restart/";
		if(rmp.hasPermission("resourcemadness.stop")) line+="Stop/";
		line = RMText.stripLast(line, "/");
		if(line.length()!=0) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+line.toLowerCase()+" "+ChatColor.WHITE+line+" a game.");
		
		if(rmp.hasPermission("resourcemadness.join")) rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"join "+ChatColor.GREEN+"[team(id/color)] "+ChatColor.WHITE+"Join a team.");
		if(rmp.hasPermission("resourcemadness.quit")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"quit "+ChatColor.WHITE+"Quit a team.");
		if(rmp.hasPermission("resourcemadness.items")) rmp.sendMessage("/rm "+ChatColor.YELLOW+"items "+ChatColor.WHITE+"Get which items you need to gather.");
		
		//Claim Items/Reward
		line="";
		if(rmp.hasPermission("resourcemadness.claim.found")) line+="found/";
		if(rmp.hasPermission("resourcemadness.claim.items")) line+="items/";
		if(rmp.hasPermission("resourcemadness.claim.reward")) line+="reward/";
		if(rmp.hasPermission("resourcemadness.claim.tools")) line+="tools/";
		line = RMText.stripLast(line, "/");
		if(line.length()!=0) rmp.sendMessage("/rm "+ChatColor.YELLOW+"claim "+line+" "+ChatColor.WHITE+"Claim your "+line+".");
	}
	
	public void rmSetInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.set")){
			rmp.sendMessage(ChatColor.GOLD+"/rm set");
			if(rmp.hasPermission("resourcemadness.set.minplayers")) rmp.sendMessage(ChatColor.YELLOW+"minplayers "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set min players.");
			if(rmp.hasPermission("resourcemadness.set.maxplayers")) rmp.sendMessage(ChatColor.YELLOW+"maxplayers "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set max players.");
			if(rmp.hasPermission("resourcemadness.set.minteamplayers")) rmp.sendMessage(ChatColor.YELLOW+"minteamplayers "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set min team players.");
			if(rmp.hasPermission("resourcemadness.set.maxteamplayers")) rmp.sendMessage(ChatColor.YELLOW+"maxteamplayers "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set max team players.");
			if(rmp.hasPermission("resourcemadness.set.random")) rmp.sendMessage(ChatColor.YELLOW+"random "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Randomly pick "+ChatColor.GREEN+"amount "+ChatColor.WHITE+"of items every match.");
			if(rmp.hasPermission("resourcemadness.set.warp")) if(!config.getLock().contains(Lock.warpToSafety)) rmp.sendMessage(ChatColor.YELLOW+"warp "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.warpToSafety+".");
			if(rmp.hasPermission("resourcemadness.set.restore")) if(!config.getLock().contains(Lock.restore)) rmp.sendMessage(ChatColor.YELLOW+"restore "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.autoRestoreWorld+".");
			if(rmp.hasPermission("resourcemadness.set.warnhacked")) if(!config.getLock().contains(Lock.warnHackedItems)) rmp.sendMessage(ChatColor.YELLOW+"warnhacked "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.warnHackedItems+".");
			if(rmp.hasPermission("resourcemadness.set.allowhacked")) if(!config.getLock().contains(Lock.allowHackedItems)) rmp.sendMessage(ChatColor.YELLOW+"allowhacked "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.allowHackedItems+".");
			if(rmp.hasPermission("resourcemadness.set.keepingame")) if(!config.getLock().contains(Lock.keepIngame)) rmp.sendMessage(ChatColor.YELLOW+"keepingame "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.keepIngame+".");
			if(rmp.hasPermission("resourcemadness.set.midgamejoin")) if(!config.getLock().contains(Lock.allowMidgameJoin)) rmp.sendMessage(ChatColor.YELLOW+"midgamejoin "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.allowMidgameJoin+".");
			if(rmp.hasPermission("resourcemadness.set.clearinventory")) if(!config.getLock().contains(Lock.clearPlayerInventory)) rmp.sendMessage(ChatColor.YELLOW+"clearinventory "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.clearPlayerInventory+".");
			if(rmp.hasPermission("resourcemadness.set.warnunequal")) if(!config.getLock().contains(Lock.warnUnequal)) rmp.sendMessage(ChatColor.YELLOW+"warnunequal "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.warnUnequal+".");
			if(rmp.hasPermission("resourcemadness.set.allowunequal")) if(!config.getLock().contains(Lock.allowUnequal)) rmp.sendMessage(ChatColor.YELLOW+"allowunequal "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.allowUnequal+".");
			if(rmp.hasPermission("resourcemadness.set.infinitereward")) if(!config.getLock().contains(Lock.infiniteReward)) rmp.sendMessage(ChatColor.YELLOW+"infinitereward "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.infiniteReward+".");
			if(rmp.hasPermission("resourcemadness.set.infinitetools")) if(!config.getLock().contains(Lock.infiniteTools)) rmp.sendMessage(ChatColor.YELLOW+"infinitetools "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.infiniteTools+".");
		}
	}
	
	public void rmFilterInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.filter")){
			rmp.sendMessage(ChatColor.GOLD+"/rm filter");
			/*
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"remove "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"random "+ChatColor.GREEN+"[amount] "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
			 */
			rmp.sendMessage(ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
			rmp.sendMessage(ChatColor.YELLOW+"remove "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear");
			rmp.sendMessage(ChatColor.YELLOW+"random "+ChatColor.GREEN+"[amount] "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
			rmp.sendMessage(ChatColor.GOLD+"Examples:");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"clear");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":stack "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"remove "+ChatColor.AQUA+"1-10,20,288");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"random "+ChatColor.GREEN+"20 "+ChatColor.AQUA+"all"+ChatColor.BLUE+":100-200");
		}
	}
	
	public void rmRewardInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(ChatColor.GOLD+"/rm reward");
			rmp.sendMessage(ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
			rmp.sendMessage(ChatColor.GOLD+"Examples:");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"reward "+ChatColor.YELLOW+"clear");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"reward "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":stack "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32");
		}
	}
	
	public void rmToolsInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(ChatColor.GOLD+"/rm tools");
			rmp.sendMessage(ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
			rmp.sendMessage(ChatColor.GOLD+"Examples:");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"tools "+ChatColor.YELLOW+"clear");
			rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"tools "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":stack "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32");
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