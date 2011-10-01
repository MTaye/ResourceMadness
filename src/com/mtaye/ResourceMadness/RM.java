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
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import com.mtaye.ResourceMadness.RMCommands.RMCommand;
import com.mtaye.ResourceMadness.RMConfig.PermissionType;
import com.mtaye.ResourceMadness.RMGame.FilterState;
import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.ForceState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.RMGame.Setting;
import com.mtaye.ResourceMadness.RMPlayer.ChatMode;
import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;
import com.mtaye.ResourceMadness.Helper.RMHelper;
import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;
import com.mtaye.ResourceMadness.Helper.RMLogHelper;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;
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
	public PluginDescriptionFile pdfFile;
	public Logger log;
	//public PermissionHandler Permissions = null;
	
	public HashMap<Player, Boolean> players = new HashMap<Player, Boolean>();
	public RMConfig config = new RMConfig();

	private RMBlockListener blockListener = new RMBlockListener(this);
	private RMPlayerListener playerListener = new RMPlayerListener(this);
	private RMEntityListener entityListener = new RMEntityListener(this);
	
	public static enum ClaimType { ITEMS, FOUND, REWARD, TOOLS, CHEST, NONE };
	public static enum DataType { CONFIG, ALIASES, STATS, PLAYER, GAME, LOG, TEMPLATE, LABELS };
	public static enum DataSave { SUCCESS, FAIL, NO_DATA };
	
	private RMWatcher watcher;
	private int watcherid;
	
	public PermissionHandler permissions = null;
	public PermissionManager permissionsEx = null;
	public iConomy iConomy = null;
	
	RMLogHelper rmLogHelper;
	
	public RM(){
	}
	
	@Override
	public void onEnable(){
		pdfFile = this.getDescription();
		RMPlayer.plugin = this;
		RMGame.plugin = this;
		RMText.plugin = this;
		RMText.init();
		RMText.initCommandList();
		RMTextHelper.plugin = this;
		RMDebug.plugin = this;
		
		log = getServer().getLogger();
		log.log(Level.INFO, pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled!" );
        RMDebug.enable();

        //setupPermissions();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.PLUGIN_ENABLE, new iConomyServer(this), Priority.Monitor, this);
		pm.registerEvent(Type.PLUGIN_DISABLE, new iConomyServer(this), Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BURN, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PISTON_EXTEND, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PISTON_RETRACT, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
		pm.registerEvent(Type.ENDERMAN_PICKUP, entityListener, Priority.Normal, this);
		//RMConfig.load();
		
		rmLogHelper = new RMLogHelper(this);
		loadAll();
		setupPermissions();
		watcher = new RMWatcher(this);
		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 20, 20);
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
			case P3: case PEX: case BUKKIT: return true;
			case FALSE: default: return false;
		}
	}
	
	public boolean hasPermission(Player player, String node){
		if((permissions==null)&&(permissionsEx==null)) return true;
		if(player==null) return false;
		else{
			switch(config.getPermissionType()){
				case BUKKIT:
					if(node=="resourcemadness.admin") return player.hasPermission("resourcemadness.admin");
					else{
						if((player.hasPermission("resourcemadness.owner"))||(player.hasPermission("*"))) return true;
						else return player.hasPermission(node);
					}
				case P3:
					if(node=="resourcemadness.admin") return permissions.has(player, "resourcemadness.admin");
					else{
						if((permissions.has(player, "resourcemadness.owner"))||(permissions.has(player, "*"))) return true;
						else return permissions.has(player, node);
					}
				case PEX:
					if(node=="resourcemadness.admin") return permissions.has(player, "resourcemadness.admin");
					else{
						if((permissionsEx.has(player, "resourcemadness.owner"))||(permissionsEx.has(player, "*"))) return true;
						else return permissionsEx.has(player, node);
					}
				case FALSE: default: return true;
			}
		}
	}
	
	public String[] processCommandArgs(String[] args){
		if(args.length!=0){
			String strArg = Arrays.toString(args);
			strArg = strArg.replace("[", "");
			strArg = strArg.replace("]", "");
			strArg = strArg.replace(",", "");
			strArg = processBestMatch(strArg);
			if(strArg!=null){
				RMDebug.warning("strArg:"+strArg);
				return strArg.trim().split(" ");
			}
		}
		return args;
	}
	
	public String processBestMatch(String arg){
		RMCommand bestCommand = null;
		String bestAlias = "";
		int bestPoints = 0;
		RMCommands commands = config.getCommands();
		for(RMCommand cmd : RMCommand.values()){
			List<String> aliases = commands.getAliasMap().get(cmd);
			if(aliases == null) continue;
			for(String alias : aliases){
				String str = arg;
				if(str.equalsIgnoreCase(alias)){
					bestCommand = cmd;
					bestAlias = alias;
				}
				else if(str.startsWith(alias)){
					int points = 0;
					for(int i=0; i<str.length(); i++){
						if(i<alias.length()){
							if(str.charAt(i)==alias.charAt(i)) points++;
						}
						else{
							if(i==alias.length()){
								if(!str.substring(i, i+1).equalsIgnoreCase(" ")){
									points = 0;
									break;
								}
							}
						}
					}
					if(points!=0){
						if(points>bestPoints){
							bestPoints = points;
							bestCommand = cmd;
							bestAlias = alias;
						}
					}
				}
			}
		}
		if(bestCommand==null) return null;
		RMDebug.warning("bestPoints:"+bestPoints);
		RMDebug.warning("bestCommand:"+bestCommand);
		RMDebug.warning("bestAlias:"+bestAlias);
		
		return arg.replace(bestAlias, bestCommand.name().toLowerCase().replaceAll("_", " "));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
		Player p = null;
		if(sender.getClass().getName().contains("Player")){
			p = (Player)sender;
			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(cmd.getName().equals("resourcemadness")){
					if(!rmp.hasPermission("resourcemadness")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
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
						args = processCommandArgs(args);
						//ADD
						if(args[0].equalsIgnoreCase(RMText.c_Add)){
							if(!rmp.hasPermission("resourcemadness.add")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							rmp.setPlayerAction(PlayerAction.ADD);
							rmp.sendMessage(RMText.a_Add);
							return true;
						}
						//REMOVE
						else if(args[0].equalsIgnoreCase(RMText.c_Remove)){
							if(!rmp.hasPermission("resourcemadness.remove")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(rmGame!=null) RMGame.tryRemoveGame(rmGame, rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.REMOVE);
								rmp.sendMessage(RMText.a_Remove);
							}
							return true;
						}
						//LIST
						else if(args[0].equalsIgnoreCase(RMText.c_List)){
							if(!rmp.hasPermission("resourcemadness.list")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length==2) sendListById(args[1], rmp);
							else sendListByInt(0, rmp);
							return true;
						}
						//ALIASES
						else if(args[0].equalsIgnoreCase(RMText.c_Commands)){
							if(!rmp.hasPermission("resourcemadness.commands")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length==2) sendAliasesById(args[1], rmp);
							else sendAliasesById("0", rmp);
							return true;
						}
						//INFO
						else if(args[0].equalsIgnoreCase(RMText.c_Info)){
							if(!rmp.hasPermission("resourcemadness.info")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length>1){
								if(args[1].equalsIgnoreCase(RMText.c_InfoFound)){
									if(!rmp.hasPermission("resourcemadness.info.found")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(rmGame!=null) rmGame.getInfoFound(rmp);
									else{
										rmp.setPlayerAction(PlayerAction.INFO_FOUND);
										rmp.sendMessage(RMText.a_InfoFound);
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_InfoClaim)){
									if(!rmp.hasPermission("resourcemadness.info.claim")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									rmp.getInfoClaim();
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_InfoItems)){
									if(!rmp.hasPermission("resourcemadness.info.items")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									rmp.getInfoItems();
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_InfoReward)){
									if(!rmp.hasPermission("resourcemadness.info.reward")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									rmp.getInfoReward();
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_InfoTools)){
									if(!rmp.hasPermission("resourcemadness.info.tools")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									rmp.getInfoTools();
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.sendInfo(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.INFO);
									rmp.sendMessage(RMText.a_Info);
								}
								return true;
							}
						}
						//SETTINGS
						else if(args[0].equalsIgnoreCase(RMText.c_Settings)){
							if(!rmp.hasPermission("resourcemadness.settings")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if((args.length==2)&&(args[1].equalsIgnoreCase(RMText.c_SettingsReset))){
								if(!rmp.hasPermission("resourcemadness.settings")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
								if(rmGame!=null) rmGame.resetSettings(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.SETTINGS_RESET);
									rmp.sendMessage(RMText.a_SettingsReset);
								}
								return true;
							}
							page = 0;
							if(args.length>1) page = RMHelper.getIntByString(args[1]);
							if(rmGame!=null) rmGame.sendSettings(rmp, page);
							else{
								rmp.setRequestInt(page);
								rmp.setPlayerAction(PlayerAction.SETTINGS);
								rmp.sendMessage(RMText.a_Settings);
							}
							return true;
						}
						//MODE
						else if(args[0].equalsIgnoreCase(RMText.c_Mode)){
							if(!rmp.hasPermission("resourcemadness.mode")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length==2){
								if(args[1].equalsIgnoreCase(RMText.c_ModeFilter)){
									if(!rmp.hasPermission("resourcemadness.mode.filter")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.FILTER, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.FILTER);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(RMText.a_ModeFilter);
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_ModeReward)){
									if(!rmp.hasPermission("resourcemadness.mode.reward")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.REWARD, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.REWARD);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(RMText.a_ModeReward);
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_ModeTools)){
									if(!rmp.hasPermission("resourcemadness.mode.tools")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.TOOLS, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.TOOLS);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(RMText.a_ModeTools);
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.cycleMode(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.MODE_CYCLE);
									rmp.sendMessage(RMText.a_ModeCycle);
								}
								return true;
							}
						}
						//SAVE
						else if(args[0].equalsIgnoreCase(RMText.c_Save)){
							if(!rmp.hasOpPermission("resourcemadness.admin.save")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(RMGame.getGames().size()!=0){
								rmp.sendMessage(RMText.save_Saving);
								log.log(Level.INFO, RMText.preLog+"Saving...");
								switch(saveAll()){
								case SUCCESS:
									rmp.sendMessage(RMText.save_Success);
									log.log(Level.INFO, RMText.preLog+"Data was saved successfully.");
									break;
								case FAIL:
									rmp.sendMessage(RMText.save_Fail);
									log.log(Level.INFO, RMText.preLog+"Data was not saved properly!");
									break;
								case NO_DATA:
									rmp.sendMessage(RMText.save_NoData);
									log.log(Level.INFO, RMText.preLog+"No data to save.");
									break;
								}
							}
							else rmp.sendMessage(RMText.save_NoData);
							return true;
						}
						//JOIN
						else if(args[0].equalsIgnoreCase(RMText.c_Join)){
							if(!rmp.hasPermission("resourcemadness.join")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length==2){
								if(rmGame!=null){								
									RMTeam rmTeam = RMGame.getTeamById(args[1], rmGame);
									if(rmTeam!=null){
										rmp.setRequestBool(true);
										rmGame.joinTeam(rmTeam, rmp);
										return true;
									}
									rmTeam = rmGame.getTeamByDye(args[1], rmGame);
									if(rmTeam!=null){
										rmp.setRequestBool(true);
										rmGame.joinTeam(rmTeam, rmp);
										return true;
									}
									rmp.sendMessage(RMText.e_TeamDoesNotExist);
									return true;
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.JOIN);
								rmp.sendMessage(RMText.a_Join);
								return true;
							}
						}
						//QUIT
						else if(args[0].equalsIgnoreCase(RMText.c_Quit)){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
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
							rmp.sendMessage(RMText.e_DidNotJoinAnyTeamYet);
							return true;
							
						}
						//READY
						else if(args[0].equalsIgnoreCase(RMText.c_Ready)){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							for(RMTeam rmTeam : RMTeam.getTeams()){
								RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
								if(rmPlayer!=null){
									RMGame game = rmTeam.getGame();
									if(game!=null){
										if(game.getConfig().getState()==GameState.SETUP){
											rmTeam.getGame().toggleReady(rmp);
										}
										else rmp.sendMessage(RMText.e_CannotReadyWhileIngame);
										return true;
									}
								}
							}
							rmp.sendMessage(RMText.e_DidNotJoinAnyTeamYet);
							return true;
							
						}
						/*
						//RETURN
						else if(args[0].equalsIgnoreCase("return")){
							if(!rmp.hasPermission("resourcemadness.return")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(!rmp.isIngame())
								if(rmp.getReturnLocation()!=null){
									rmp.warpToReturnLocation();
								}
								else rmp.sendMessage("No return location marked");
							else rmp.sendMessage("You can't return while ingame.");
							return true;
						}
						*/
						//START
						else if(args[0].equalsIgnoreCase(RMText.c_Start)){
							if(!rmp.hasPermission("resourcemadness.start")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length==2){
								int amount = RMHelper.getIntByString(args[1]);
								if(amount!=-1){
									if(!rmp.hasPermission("resourcemadness.start.random")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(rmGame!=null){
										rmGame.setRandomizeAmount(rmp, amount);
										rmGame.startGame(rmp);
									}
									else{
										rmp.setRequestInt(amount);
										rmp.setPlayerAction(PlayerAction.START_RANDOM);
										rmp.sendMessage(RMText.a_StartRandom.replace("*", ""+amount));
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.startGame(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.START);
									rmp.sendMessage(RMText.a_Start);
								}
								return true;
							}
						}
						/*
						//RESTART
						else if(args[0].equalsIgnoreCase("restart")){
							if(!rmp.hasPermission("resourcemadness.restart")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(rmGame!=null) rmGame.restartGame(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTART);
								rmp.sendMessage(RMText.a_Restart);
							}
							return true;
						}
						*/
						//STOP
						else if(args[0].equalsIgnoreCase(RMText.c_Stop)){
							if(!rmp.hasPermission("resourcemadness.stop")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(rmGame!=null) rmGame.stopGame(rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.STOP);
								rmp.sendMessage(RMText.a_Stop);
							}
							return true;
						}
						//PAUSE
						else if(args[0].equalsIgnoreCase(RMText.c_Pause)){
							if(!rmp.hasPermission("resourcemadness.pause")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(rmGame!=null) rmGame.pauseGame(rmp);
							else{
								rmp.setRequestBool(true);
								rmp.setPlayerAction(PlayerAction.PAUSE);
								rmp.sendMessage(RMText.a_Pause);
							}
							return true;
						}
						//RESUME
						else if(args[0].equalsIgnoreCase(RMText.c_Resume)){
							if(!rmp.hasPermission("resourcemadness.pause")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(rmGame!=null) rmGame.resumeGame(rmp);
							else{
								rmp.setRequestBool(false);
								rmp.setPlayerAction(PlayerAction.RESUME);
								rmp.sendMessage(RMText.a_Resume);
							}
							return true;
						}
						//RESTORE WORLD
						else if(args[0].equalsIgnoreCase(RMText.c_Restore)){
							if(!rmp.hasPermission("resourcemadness.restore")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(rmGame!=null) rmGame.restoreWorld(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTORE);
								rmp.sendMessage(RMText.a_Restore);
							}
							return true;
						}
						//ITEMS
						else if(args[0].equalsIgnoreCase(RMText.c_Items)){
							if(!rmp.hasPermission("resourcemadness.items")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(rmp.isIngame()){
								rmp.getGameInProgress().updateGameplayInfo(rmp, rmp.getTeam());
								return true;
							}
							rmp.sendMessage(RMText.e_MustBeIngameCommand);
							return false;
						}
						//FILTER
						FilterState filterState = null;
						if(args[0].equalsIgnoreCase(RMText.c_Filter)){
							if(!rmp.hasPermission("resourcemadness.filter")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if((args.length>1)&&(args[1].equalsIgnoreCase(RMText.c_FilterRewardToolsInfo))){
								if(!rmp.hasPermission("resourcemadness.filter.info")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
								if(rmGame!=null){
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.c_FilterRewardToolsInfoString))){
										rmGame.sendFilterInfoString(rmp);
									}
									else rmGame.sendFilterInfo(rmp);
								}
								else{
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.c_FilterRewardToolsInfoString))){
										rmp.setPlayerAction(PlayerAction.FILTER_INFO_STRING);
										rmp.sendMessage(RMText.a_FilterInfoString);
									}
									else{
										rmp.setPlayerAction(PlayerAction.FILTER_INFO);
										rmp.sendMessage(RMText.a_FilterInfo);
									}
								}
								return true;
							}
							filterState = FilterState.FILTER;
						}
						else if(args[0].equalsIgnoreCase(RMText.c_Reward)){
							if(!rmp.hasPermission("resourcemadness.reward")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if((args.length>1)&&(args[1].equalsIgnoreCase(RMText.c_FilterRewardToolsInfo))){
								if(!rmp.hasPermission("resourcemadness.reward.info")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
								if(rmGame!=null){
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.c_FilterRewardToolsInfoString))){
										rmGame.sendRewardInfoString(rmp);
									}
									else rmGame.sendRewardInfo(rmp);
								}
								else{
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.c_FilterRewardToolsInfoString))){
										rmp.setPlayerAction(PlayerAction.REWARD_INFO_STRING);
										rmp.sendMessage(RMText.a_RewardInfoString);
									}
									else{
										rmp.setPlayerAction(PlayerAction.REWARD_INFO);
										rmp.sendMessage(RMText.a_RewardInfo);
									}
								}
								return true;
							}
							filterState = FilterState.REWARD;
						}
						else if(args[0].equalsIgnoreCase(RMText.c_Tools)){
							if(!rmp.hasPermission("resourcemadness.tools")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if((args.length>1)&&(args[1].equalsIgnoreCase(RMText.c_FilterRewardToolsInfo))){
								if(!rmp.hasPermission("resourcemadness.tools.info")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
								if(rmGame!=null){
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.c_FilterRewardToolsInfoString))){
										rmGame.sendToolsInfoString(rmp);
									}
									else rmGame.sendToolsInfo(rmp);
								}
								else{
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.c_FilterRewardToolsInfoString))){
										rmp.setPlayerAction(PlayerAction.TOOLS_INFO_STRING);
										rmp.sendMessage(RMText.a_ToolsInfoString);
									}
									else{
										rmp.setPlayerAction(PlayerAction.TOOLS_INFO);
										rmp.sendMessage(RMText.a_ToolsInfo);
									}
								}
								return true;
							}
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
												rmp.sendMessage(RMText.a_Filter);
												break;
											case REWARD:
												rmp.setPlayerAction(PlayerAction.REWARD);
												rmp.sendMessage(RMText.a_Reward);
												break;
											case TOOLS:
												rmp.setPlayerAction(PlayerAction.TOOLS);
												rmp.sendMessage(RMText.a_Tools);
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
						//TEMPLATE
						else if(args[0].equalsIgnoreCase(RMText.c_Template)){
							if(!rmp.hasPermission("resourcemadness.template")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length>1){
								if(args[1].equalsIgnoreCase(RMText.c_TemplateList)){
									if(!rmp.hasPermission("resourcemadness.template.list")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(args.length==2) sendTemplateListById(args[1], rmp);
									else sendTemplateListById("0", rmp);
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_TemplateLoad)){
									if(!rmp.hasPermission("resourcemadness.template.load")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(args.length==3){
										RMTemplate template = rmp.loadTemplate(args[2].toLowerCase());
										if(template!=null){
											if(rmGame!=null) rmGame.loadTemplate(template, rmp);
											else{
												rmp.setRequestString(args[2]);
												rmp.setPlayerAction(PlayerAction.TEMPLATE_LOAD);
												rmp.sendMessage(RMText.a_TemplateLoad.replace("*", args[2]));
											}
										}
										return true;
									}
								}
								else if(args[1].equalsIgnoreCase(RMText.c_TemplateSave)){
									if(!rmp.hasPermission("resourcemadness.template.save")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(args.length==3){
										if(rmGame!=null) rmGame.saveTemplate(args[2].toLowerCase(), rmp);
										else{
											rmp.setRequestString(args[2].toLowerCase());
											rmp.setPlayerAction(PlayerAction.TEMPLATE_SAVE);
											rmp.sendMessage(RMText.a_TemplateSave.replace("*", args[2]));
										}
										return true;
									}
								}
								else if(args[1].equalsIgnoreCase(RMText.c_TemplateRemove)){
									if(!rmp.hasPermission("resourcemadness.template.remove")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(args.length>2){
										rmp.removeTemplates(Arrays.asList(args).subList(2, args.length));
										return true;
									}
								}
							}
							rmTemplateInfo(rmp);
							return true;
						}
						//CLAIM
						else if(args[0].equalsIgnoreCase(RMText.c_Claim)){
							if(!rmp.hasPermission("resourcemadness.claim")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length>1){
								if(args[1].equalsIgnoreCase(RMText.c_ClaimFound)){
									if(!rmp.hasPermission("resourcemadness.claim.found")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(RMText.c_ClaimFoundItemsRewardToolsChest)){
												if(!rmp.hasPermission("resourcemadness.claim.found.chest")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												if(rmGame!=null){
													rmp.setRequestInt(rmGame.getConfig().getId());
													rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST);
													rmp.sendMessage(RMText.a_ClaimFoundChest);
												}
												else{
													rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST_SELECT);
													rmp.sendMessage(RMText.a_ClaimFoundChestSelect);
												}
												return true;
											}
										}
										if(rmGame!=null) rmGame.claimFound(rmp, requestClaimItemsAtArgsPos(rmp, args, 2));
										else{
											rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 2));
											rmp.setPlayerAction(PlayerAction.CLAIM_FOUND);
											rmp.sendMessage(RMText.a_ClaimFound);
										}
										return true;
									}
									else rmp.sendMessage(RMText.e_CannotClaimFoundIngame);
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_ClaimItems)){
									if(!rmp.hasPermission("resourcemadness.claim.items")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(RMText.c_ClaimFoundItemsRewardToolsChest)){
												if(!rmp.hasPermission("resourcemadness.claim.items.chest")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												rmp.setPlayerAction(PlayerAction.CLAIM_ITEMS_CHEST);
												rmp.sendMessage(RMText.a_ClaimItemsChest);
												return true;
											}
										}
										rmp.claimItems(requestClaimItemsAtArgsPos(rmp, args, 2));
										return true;
									}
									else rmp.sendMessage(RMText.e_CannotClaimItemsIngame);
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_ClaimReward)){
									if(!rmp.hasPermission("resourcemadness.claim.reward")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(RMText.c_ClaimFoundItemsRewardToolsChest)){
												if(!rmp.hasPermission("resourcemadness.claim.reward.chest")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												rmp.setPlayerAction(PlayerAction.CLAIM_REWARD_CHEST);
												rmp.sendMessage(RMText.a_ClaimRewardChest);;
												return true;
											}
										}
										rmp.claimReward(requestClaimItemsAtArgsPos(rmp, args, 2));
										return true;
									}
									else rmp.sendMessage(RMText.e_CannotClaimRewardIngame);
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_ClaimTools)){
									if(!rmp.hasPermission("resourcemadness.claim.tools")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									if(args.length>2){
										if(args[2].equalsIgnoreCase(RMText.c_ClaimFoundItemsRewardToolsChest)){
											if(!rmp.hasPermission("resourcemadness.claim.tools.chest")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
											rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
											rmp.setPlayerAction(PlayerAction.CLAIM_TOOLS_CHEST);
											rmp.sendMessage(RMText.a_ClaimToolsChest);
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
						else if(args[0].equalsIgnoreCase(RMText.c_Set)){
							if(!rmp.hasPermission("resourcemadness.set")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							page = 0;
							if(args.length>1){
								PlayerAction action = null;
								//MIN PLAYERS
								if(args[1].equalsIgnoreCase(RMText.c_SetMinPlayers)){
									if(!rmp.hasPermission("resourcemadness.set.minplayers")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_MIN_PLAYERS;
								}
								//MAX PLAYERS
								else if(args[1].equalsIgnoreCase(RMText.c_SetMaxPlayers)){
									if(!rmp.hasPermission("resourcemadness.set.maxplayers")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_MAX_PLAYERS;
								}
								//MIN TEAM PLAYERS
								else if(args[1].equalsIgnoreCase(RMText.c_SetMinTeamPlayers)){
									if(!rmp.hasPermission("resourcemadness.set.minteamplayers")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_MIN_TEAM_PLAYERS;
								}
								//MAX TEAM PLAYERS
								else if(args[1].equalsIgnoreCase(RMText.c_SetMaxTeamPlayers)){
									if(!rmp.hasPermission("resourcemadness.set.maxteamplayers")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_MAX_TEAM_PLAYERS;
								}
								/*
								//MAX ITEMS
								else if(args[1].equalsIgnoreCase("maxitems")){
									if(!rmp.hasPermission("resourcemadness.set.maxitems")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_MAX_ITEMS;
								}
								*/
								//MATCH TIME LIMIT
								else if(args[1].equalsIgnoreCase(RMText.c_SetTimeLimit)){
									if(!rmp.hasPermission("resourcemadness.set.timelimit")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_TIME_LIMIT;
								}
								//AUTO RANDOM ITEMS
								else if(args[1].equalsIgnoreCase(RMText.c_SetRandom)){
									if(!rmp.hasPermission("resourcemadness.set.random")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_RANDOM;
								}
								else page = RMHelper.getIntByString(args[1]);
								
								if(action!=null){
									if(args.length==3){
										int amount = RMHelper.getIntByString(args[2]);
										if(amount>-1){
											if(rmGame!=null){
												switch(action){
													case SET_MIN_PLAYERS: rmGame.setSetting(rmp, Setting.minPlayers, amount); break;
													case SET_MAX_PLAYERS: rmGame.setSetting(rmp, Setting.maxPlayers, amount); break;
													case SET_MIN_TEAM_PLAYERS: rmGame.setSetting(rmp, Setting.minTeamPlayers, amount); break;
													case SET_MAX_TEAM_PLAYERS: rmGame.setSetting(rmp, Setting.maxPlayers, amount); break;
													//case SET_MAX_ITEMS: rmGame.setSetting(rmp, Setting.maxItems, amount); break;
													case SET_TIME_LIMIT: rmGame.setSetting(rmp, Setting.timeLimit, amount); break;
													case SET_RANDOM: rmGame.setSetting(rmp, Setting.autoRandomizeAmount, amount); break;
												}
												return true;
											}
											else{
												rmp.setRequestInt(RMHelper.getIntByString(args[2]));
												switch(action){
													case SET_MIN_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MIN_PLAYERS);
														rmp.sendMessage(RMText.a_SetMinPlayers);
														break;
													case SET_MAX_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_PLAYERS);
														rmp.sendMessage(RMText.a_SetMaxPlayers);
														break;
													case SET_MIN_TEAM_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MIN_TEAM_PLAYERS);
														rmp.sendMessage(RMText.a_SetMinTeamPlayers);
														break;
													case SET_MAX_TEAM_PLAYERS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_TEAM_PLAYERS);
														rmp.sendMessage(RMText.a_SetMaxTeamPlayers);
														break;
													/*
													case SET_MAX_ITEMS:
														rmp.setPlayerAction(PlayerAction.SET_MAX_ITEMS);
														rmp.sendMessage(RMText.a_SetMaxItems);
														break;
													*/
													case SET_TIME_LIMIT:
														rmp.setPlayerAction(PlayerAction.SET_TIME_LIMIT);
														rmp.sendMessage(RMText.a_SetTimeLimit);
														break;
													case SET_RANDOM:
														rmp.setPlayerAction(PlayerAction.SET_RANDOM);
														rmp.sendMessage(RMText.a_SetRandom);
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
								if(args[1].equalsIgnoreCase(RMText.c_SetAdvertise)){
									if(!rmp.hasPermission("resourcemadness.set.advertise")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_ADVERTISE;
								}
								//AUTO RESTORE WORLD
								else if(args[1].equalsIgnoreCase(RMText.c_SetRestore)){
									if(!rmp.hasPermission("resourcemadness.set.restore")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_RESTORE;
								}
								//GATHER PLAYERS
								else if(args[1].equalsIgnoreCase(RMText.c_SetWarp)){
									if(!rmp.hasPermission("resourcemadness.set.warp")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_WARP;
								}
								//ALLOW MIDGAME JOIN
								else if(args[1].equalsIgnoreCase(RMText.c_SetMidgameJoin)){
									if(!rmp.hasPermission("resourcemadness.set.midgamejoin")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_MIDGAME_JOIN;
								}
								//HEAL PLAYER
								else if(args[1].equalsIgnoreCase(RMText.c_SetHealPlayer)){
									if(!rmp.hasPermission("resourcemadness.set.healplayer")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_HEAL_PLAYER;
								}
								//CLEAR PLAYER INVENTORY
								else if(args[1].equalsIgnoreCase(RMText.c_SetClearInventory)){
									if(!rmp.hasPermission("resourcemadness.set.clearinventory")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_CLEAR_INVENTORY;
								}
								//USE A GAME'S FOUND ITEMS AS REWARD
								else if(args[1].equalsIgnoreCase(RMText.c_SetFoundAsReward)){
									if(!rmp.hasPermission("resourcemadness.set.foundasreward")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_FOUND_AS_REWARD;
								}
								//WARN UNEQUAL ITEMS
								else if(args[1].equalsIgnoreCase(RMText.c_SetWarnUnequal)){
									if(!rmp.hasPermission("resourcemadness.set.warnunequal")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_WARN_UNEQUAL;
								}
								//ALLOW UNEQUAL ITEMS
								else if(args[1].equalsIgnoreCase(RMText.c_SetAllowUnequal)){
									if(!rmp.hasPermission("resourcemadness.set.allowunequal")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_ALLOW_UNEQUAL;
								}
								//WARN HACK ITEMS
								else if(args[1].equalsIgnoreCase(RMText.c_SetWarnHacked)){
									if(!rmp.hasPermission("resourcemadness.set.warnhacked")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_WARN_HACKED;
								}
								//ALLOW HACK ITEMS
								else if(args[1].equalsIgnoreCase(RMText.c_SetAllowHacked)){
									if(!rmp.hasPermission("resourcemadness.set.allowhacked")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_ALLOW_HACKED;
								}
								//INFINITE REWARD ITEMS
								else if(args[1].equalsIgnoreCase(RMText.c_SetInfiniteReward)){
									if(!rmp.hasPermission("resourcemadness.set.infinitereward")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									action = PlayerAction.SET_INFINITE_REWARD;
								}
								//INFINITE TOOLS ITEMS
								else if(args[1].equalsIgnoreCase(RMText.c_SetInfiniteTools)){
									if(!rmp.hasPermission("resourcemadness.set.infinitetools")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
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
											case SET_ADVERTISE: rmGame.setSetting(rmp, Setting.advertise, i); break;
											case SET_RESTORE: rmGame.setSetting(rmp, Setting.autoRestoreWorld, i); break;
											case SET_WARP: rmGame.setSetting(rmp, Setting.warpToSafety, i); break;
											case SET_MIDGAME_JOIN: rmGame.setSetting(rmp, Setting.allowMidgameJoin, i); break;
											case SET_HEAL_PLAYER: rmGame.setSetting(rmp, Setting.healPlayer, i); break;
											case SET_CLEAR_INVENTORY: rmGame.setSetting(rmp, Setting.clearPlayerInventory, i); break;
											case SET_FOUND_AS_REWARD: rmGame.setSetting(rmp, Setting.foundAsReward, i); break;
											case SET_WARN_UNEQUAL: rmGame.setSetting(rmp, Setting.warnUnequal, i); break;
											case SET_ALLOW_UNEQUAL: rmGame.setSetting(rmp, Setting.allowUnequal, i); break;
											case SET_WARN_HACKED: rmGame.setSetting(rmp, Setting.warnHackedItems, i); break;
											case SET_ALLOW_HACKED: rmGame.setSetting(rmp, Setting.allowHackedItems, i); break;
											case SET_INFINITE_REWARD: rmGame.setSetting(rmp, Setting.infiniteReward, i); break;
											case SET_INFINITE_TOOLS: rmGame.setSetting(rmp, Setting.infiniteTools, i); break;
										}
									}
									else{
										rmp.setRequestInt(i);
										switch(action){
											case SET_ADVERTISE:
												rmp.setPlayerAction(PlayerAction.SET_ADVERTISE);
												rmp.sendMessage(RMText.a_SetAdvertise);
												break;
											case SET_RESTORE:
												rmp.setPlayerAction(PlayerAction.SET_RESTORE);
												rmp.sendMessage(RMText.a_SetRestore);
												break;
											case SET_WARP:
												rmp.setPlayerAction(PlayerAction.SET_WARP);
												rmp.sendMessage(RMText.a_SetWarp);
												break;
											case SET_MIDGAME_JOIN:
												rmp.setPlayerAction(PlayerAction.SET_MIDGAME_JOIN);
												rmp.sendMessage(RMText.a_SetMidgameJoin);
												break;
											case SET_HEAL_PLAYER:
												rmp.setPlayerAction(PlayerAction.SET_HEAL_PLAYER);
												rmp.sendMessage(RMText.a_SetHealPlayer);
												break;
											case SET_CLEAR_INVENTORY:
												rmp.setPlayerAction(PlayerAction.SET_CLEAR_INVENTORY);
												rmp.sendMessage(RMText.a_SetClearInventory);
												break;
											case SET_FOUND_AS_REWARD:
												rmp.setPlayerAction(PlayerAction.SET_FOUND_AS_REWARD);
												rmp.sendMessage(RMText.a_SetFoundAsReward);
											case SET_WARN_UNEQUAL:
												rmp.setPlayerAction(PlayerAction.SET_WARN_UNEQUAL);
												rmp.sendMessage(RMText.a_SetWarnUnequal);
												break;
											case SET_ALLOW_UNEQUAL:
												rmp.setPlayerAction(PlayerAction.SET_ALLOW_UNEQUAL);
												rmp.sendMessage(RMText.a_SetAllowUnequal);
												break;
											case SET_WARN_HACKED:
												rmp.setPlayerAction(PlayerAction.SET_WARN_HACKED);
												rmp.sendMessage(RMText.a_SetWarnHacked);
												break;
											case SET_ALLOW_HACKED:
												rmp.setPlayerAction(PlayerAction.SET_ALLOW_HACKED);
												rmp.sendMessage(RMText.a_SetAllowHacked);
												break;
											case SET_INFINITE_REWARD:
												rmp.setPlayerAction(PlayerAction.SET_INFINITE_REWARD);
												rmp.sendMessage(RMText.a_SetInfiniteReward);
												break;
											case SET_INFINITE_TOOLS:
												rmp.setPlayerAction(PlayerAction.SET_INFINITE_TOOLS);
												rmp.sendMessage(RMText.a_SetInfiniteTools);
												break;
										}
									}
									return true;
								}
							}
							rmSetInfo(rmp, page);
							return true;
						}
						else if(args[0].equalsIgnoreCase(RMText.c_Chat)){
							if(!rmp.hasPermission("resourcemadness.chat")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
							if(args.length>1){
								ChatMode chatMode = null;
								if(args[1].equalsIgnoreCase(RMText.c_ChatWorld)){
									if(!rmp.hasPermission("resourcemadness.chat.world")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									chatMode = ChatMode.WORLD;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_ChatGame)){
									if(!rmp.hasPermission("resourcemadness.chat.game")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									chatMode = ChatMode.GAME;
								}
								else if(args[1].equalsIgnoreCase(RMText.c_ChatTeam)){
									if(!rmp.hasPermission("resourcemadness.chat.team")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
									chatMode = ChatMode.TEAM;
								}
								
								if(chatMode!=null){
									if(rmp.isIngame()){
										if(args.length>2){
											String message = RMTextHelper.getTextFromArgs(args, 2);
											rmp.chat(chatMode, rmp.getChatMessage(chatMode, message));
										}
										else rmp.setChatMode(chatMode, true);
									}
									else{
										switch(chatMode){
										case WORLD: rmp.sendMessage(RMText.e_MustBeIngameChatWorld); break;
										case GAME: rmp.sendMessage(RMText.e_MustBeIngameChatGame); break;
										case TEAM: rmp.sendMessage(RMText.e_MustBeIngameChatTeam); break;
										}
									}
									return true;
								}
							}
							rmChatInfo(rmp);
							return true;
						}
						//ITEM - Get Item NAME by ID or Item ID by NAME
						else if(args[0].equalsIgnoreCase(RMText.c_Item)){
							List<String> listArgs = new ArrayList<String>();
							for(int i=1; i<args.length; i++){
								listArgs.add(args[i]);
							}
							if(listArgs.size()>0){
								if(!rmp.hasPermission("resourcemadness.item")) return rmp.sendMessage(RMText.e_NoPermissionCommand);
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
									rmp.sendMessage(RMTextHelper.getFormattedItemStringByHashMap(items));
									return true;
								}
								else if(itemsWarn.size()>0){
									rmp.sendMessage(RMText.e_ItemsDoNotExist);
									//rmp.sendMessage("These items don't exist: "+getFormattedStringByList(itemsWarn));
									return true;
								}
							}
							else{
								rmItemInfo(rmp);
								return true;
							}
						}
						rmInfo(rmp, page);
					}
				}
			}
		}
		return true;
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
		if(!file.exists()) save(DataType.CONFIG, false, file, false);
		save(DataType.STATS, false, new File(folder.getAbsolutePath()+File.separator+"stats.txt"), true);
		//save(DataType.PLAYER, false, new File(folder.getAbsolutePath()+File.separator+"playerdata.txt"));
		saveYaml(DataType.PLAYER, new File(folder.getAbsolutePath()+File.separator+"playerdata.yml"), true);
		//save(DataType.GAME, false, new File(folder.getAbsolutePath()+File.separator+"gamedata.txt"));
		saveYaml(DataType.GAME, new File(folder.getAbsolutePath()+File.separator+"gamedata.yml"), true);
		save(DataType.LOG, true, new File(folder.getAbsolutePath()+File.separator+"gamelogdata.txt"), true);
	}
	
	public void saveConfig(){
		File folder = getDataFolder();
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating config directory...");
			folder.mkdir();
		}
		File file = new File(folder.getAbsolutePath()+File.separator+"config.txt");
		if(!file.exists()) save(DataType.CONFIG, false, file, false);
		file = new File(folder.getAbsolutePath()+File.separator+"aliases.yml");
		if(!file.exists()){
			RMCommands commands = config.getCommands();
			commands.clear();
			commands.initAliases();
			commands.initDefaultAliases();
			saveYaml(DataType.ALIASES, file, false);
		}
	}
	
	public DataSave saveAll(){
		List<Boolean> decision = new ArrayList<Boolean>();
		if(RMGame.getGames().size()==0) return DataSave.NO_DATA;
		File folder = getDataFolder();
		File file = new File(folder.getAbsolutePath()+File.separator+"aliases.yml");
		if(file.exists()) saveYaml(DataType.ALIASES, file, false);
		saveConfig();
		decision.add(save(DataType.STATS, false, new File(folder.getAbsolutePath()+File.separator+"stats.txt"), true));
		//decision.add(save(DataType.PLAYER, false, new File(folder.getAbsolutePath()+File.separator+"playerdata.txt")));
		decision.add(saveYaml(DataType.PLAYER, new File(folder.getAbsolutePath()+File.separator+"playerdata.yml"), true));
		//decision.add(save(DataType.GAME, false, new File(folder.getAbsolutePath()+File.separator+"gamedata.txt")));
		decision.add(saveYaml(DataType.GAME, new File(folder.getAbsolutePath()+File.separator+"gamedata.yml"), true));
		decision.add(save(DataType.LOG, true, new File(folder.getAbsolutePath()+File.separator+"gamelogdata.txt"), true));
		if(decision.contains(false)) return DataSave.FAIL;
		return DataSave.SUCCESS;
	}
	
	public boolean saveYaml(DataType dataType, File file, boolean saveBackup){
		if(file==null){
			log.log(Level.WARNING, "Cannot load data. Data type unknown!");
			return false;
		}
		if(!file.exists()){
			switch(dataType){
				case CONFIG: log.log(Level.INFO, RMText.preLog+"Data file not found! Creating one..."); break;
				case ALIASES: log.log(Level.INFO, RMText.preLog+"Aliases file not found! Creating one..."); break;
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
				case ALIASES:
					yml.setHeader("# ResourceMadness v"+pdfFile.getVersion()+" Aliases file\n\n"+RMText.config_Aliases);
					RMCommands commands = config.getCommands();
					for(RMCommand cmd : RMCommand.values()){
						yml.load();
						String aliases = commands.getAliasMap().get(cmd).toString();
						aliases = aliases.replace("[", "");
						aliases = aliases.replace("]", "");
						String root = cmd.name().toLowerCase().replaceAll("_", " ");
						if((yml.getString(root) == null)||(yml.getString(root).length()==0)) setProperty(yml, root, aliases);
						yml.save();
					}
					break;
				case PLAYER:
					List<String> keys = yml.getKeys();
					for(String key : keys){
						yml.removeProperty(key);
					}
					yml.setHeader("# ResourceMadness v"+pdfFile.getVersion()+" Player data file");
					for(RMPlayer rmp : RMPlayer.getPlayers().values()){
						String root = rmp.getName()+".";
						setProperty(yml, root+"ready", rmp.getReady());
						setProperty(yml, root+"chatmode", rmp.getChatMode().ordinal());
						//Stats
						root = rmp.getName()+".stats.";
						RMStats stats = rmp.getStats();
						setProperty(yml, root+"wins", stats.getWins());
						setProperty(yml, root+"losses", stats.getLosses());
						setProperty(yml, root+"timesplayed", stats.getTimesPlayed());
						setProperty(yml, root+"itemsfoundtotal", stats.getItemsFoundTotal());
						//Data
						root = rmp.getName()+".data.";
						setProperty(yml, root+"items", RMInventoryHelper.encodeInventoryToString(rmp.getItems().getItemsArray()));
						setProperty(yml, root+"reward", RMInventoryHelper.encodeInventoryToString(rmp.getReward().getItemsArray())); 
						setProperty(yml, root+"tools", RMInventoryHelper.encodeInventoryToString(rmp.getTools().getItemsArray()));
						//Templates
						
						root=rmp.getName()+".";
						for(RMTemplate template : rmp.getTemplates().values()){
							root=rmp.getName()+".templates."+template.getName()+".";
							setProperty(yml, root+"filter", template.getEncodeToStringFilter());
							setProperty(yml, root+"reward", template.getEncodeToStringReward());
							setProperty(yml, root+"tools", template.getEncodeToStringTools());
						}
					}
					break;
				case GAME:
					keys = yml.getKeys();
					for(String key : keys){
						yml.removeProperty(key);
					}
					yml.setHeader("# ResourceMadness v"+pdfFile.getVersion()+" Game data file");
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
						setProperty(yml, root+"foundasreward", config.getFoundAsReward());
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
							setProperty(yml, root+"chest", RMInventoryHelper.encodeInventoryToString(team.getChest().getStash().getItemsArray()));
							setProperty(yml, root+"items", RMFilter.encodeFilterToString(team.getChest().getRMItems(), true));
						}
						//Items
						root = id+".data.";
						setProperty(yml, root+"filter", RMFilter.encodeFilterToString(config.getFilter().getItems(), true));
						setProperty(yml, root+"items", RMFilter.encodeFilterToString(config.getItems().getItems(), true));
						setProperty(yml, root+"found", RMInventoryHelper.encodeInventoryToString(config.getFoundArray()));
						setProperty(yml, root+"reward", RMInventoryHelper.encodeInventoryToString(config.getRewardArray()));
						setProperty(yml, root+"tools", RMInventoryHelper.encodeInventoryToString(config.getToolsArray()));
					}
					break;
			}
			yml.save();
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		if(saveBackup){
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
						case ALIASES: log.log(Level.INFO, RMText.preLog+"Could not create aliases backup file."); break;
						case STATS: log.log(Level.INFO, RMText.preLog+"Could not create stats backup file."); break;
						case PLAYER: log.log(Level.INFO, RMText.preLog+"Could not create player data backup file."); break;
						case GAME: log.log(Level.INFO, RMText.preLog+"Could not create game data backup file."); break;
						case LOG: log.log(Level.INFO, RMText.preLog+"Could not create game log data backup file."); break;
					}
				}
			}
		}
		return true;
	}
	
	public void setProperty(Configuration yml, String root, Object x){
		yml.setProperty(root, x);
	}
	
	//Save Data
	public boolean save(DataType dataType, boolean useLZF, File file, boolean saveBackup){
		if(file==null){
			log.log(Level.WARNING, "Cannot load data. Data type unknown!");
			return false;
		}
		if(!file.exists()){
			switch(dataType){
				case CONFIG: log.log(Level.INFO, RMText.preLog+"Data file not found! Creating one..."); break;
				case ALIASES: log.log(Level.INFO, RMText.preLog+"Aliases file not found! Creating one..."); break;
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
			OutputStream output;
			if(useLZF) output = new LZFOutputStream(new FileOutputStream(file.getAbsoluteFile()));
			else output = new FileOutputStream(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
			String line = "";
			switch(dataType){
				case CONFIG:
					line = "";
					line+="# ResourceMadness v"+pdfFile.getVersion()+" Config file\n\n";
					line+=RMText.config_AutoSave+"\n";
					line+="autosave="+config.getAutoSave()+"\n\n";
					line+=RMText.config_UsePermissions+"\n";
					line+="usePermissions="+config.getPermissionType().name().toLowerCase()+"\n\n";
					line+=RMText.config_UseRestore+"\n";
					line+="useRestore="+config.getUseRestore()+"\n\n";
					line+=RMText.config_ServerWide+"\n\n";
					//Max games
					line+=RMText.config_MaxGames+"\n";
					line+="maxGames="+config.getMaxGames()+"\n\n";
					//Max games per player
					line+=RMText.config_MaxGamesPerPlayer+"\n";
					line+="maxGamesPerPlayer="+config.getMaxGamesPerPlayer()+"\n\n";
					//Default game settings
					line+=RMText.config_DefaultSettings1+"\n\n";
					//Min players per game
					line+=RMText.config_MinPlayers+"\n";
					line+="minPlayers="+config.getMinPlayers()+(config.isLocked(Setting.minPlayers)?":lock":"")+"\n\n";
					//Max player per game
					line+=RMText.config_MaxPlayers+"\n";
					line+="maxPlayers="+config.getMaxPlayers()+(config.isLocked(Setting.maxPlayers)?":lock":"")+"\n\n";
					//Min players per team
					line+=RMText.config_MinTeamPlayers+"\n";
					line+="minTeamPlayers="+config.getMinTeamPlayers()+(config.isLocked(Setting.minTeamPlayers)?":lock":"")+"\n\n";
					//Max players per team
					line+=RMText.config_MaxTeamPlayers+"\n";
					line+="maxTeamPlayers="+config.getMaxTeamPlayers()+(config.isLocked(Setting.maxTeamPlayers)?":lock":"")+"\n\n";
					//Match time limit
					line+=RMText.config_TimeLimit+"\n";
					line+="timeLimit="+config.getTimeLimit()+(config.isLocked(Setting.timeLimit)?":lock":"")+"\n\n";
					//Default game settings true/false
					line+=RMText.config_DefaultSettings2+"\n\n";
					//Advertise game in search
					line+=RMText.config_Advertise+"\n";
					line+="advertise="+config.getAdvertise()+(config.isLocked(Setting.advertise)?":lock":"")+"\n\n";
					//Auto restore world
					line+=RMText.config_AutoRestoreWorld+"\n";
					line+="autoRestoreWorld="+config.getAutoRestoreWorld()+(config.isLocked(Setting.autoRestoreWorld)?":lock":"")+"\n\n";
					//Warp to safety
					line+=RMText.config_WarpToSafety+"\n";
					line+="warpToSafety="+config.getWarpToSafety()+(config.isLocked(Setting.warpToSafety)?":lock":"")+"\n\n";
					//Allow players to join a game in progress
					line+=RMText.config_AllowMidgameJoin+"\n";
					line+="allowMidgameJoin="+config.getAllowMidgameJoin()+(config.isLocked(Setting.allowMidgameJoin)?":lock":"")+"\n\n";
					//Heal players at game start
					line+=RMText.config_HealPlayer+"\n";
					line+="healPlayer="+config.getHealPlayer()+(config.isLocked(Setting.healPlayer)?":lock":"")+"\n\n";
					//Clear/return player's items at game start/finish
					line+=RMText.config_ClearPlayerInventory+"\n";
					line+="clearPlayerInventory="+config.getClearPlayerInventory()+(config.isLocked(Setting.clearPlayerInventory)?":lock":"")+"\n\n";
					//Use the game's found items as reward
					line+=RMText.config_FoundAsReward+"\n";
					line+="foundAsReward="+config.getFoundAsReward()+(config.isLocked(Setting.foundAsReward)?":lock":"")+"\n\n";
					//Warn when reward/tools can't be distributed equally
					line+=RMText.config_WarnUnequal+"\n";
					line+="warnunequal="+config.getWarnUnequal()+(config.isLocked(Setting.warnUnequal)?":lock":"")+"\n\n";
					//Allow reward/tools to be distributed unequally
					line+=RMText.config_AllowUnequal+"\n";
					line+="allowunequal="+config.getAllowUnequal()+(config.isLocked(Setting.allowUnequal)?":lock":"")+"\n\n";
					//Warn when hacked items are added
					line+=RMText.config_WarnHackedItems+"\n";
					line+="warnHackedItems="+config.getWarnHackedItems()+(config.isLocked(Setting.warnHackedItems)?":lock":"")+"\n\n";
					//Allow the use of hacked items
					line+=RMText.config_AllowHackedItems+"\n";
					line+="allowHackedItems="+config.getAllowHackedItems()+(config.isLocked(Setting.allowHackedItems)?":lock":"")+"\n\n";
					//Use infinite reward
					line+=RMText.config_InfiniteReward+"\n";
					line+="infiniteReward="+config.getInfiniteReward()+(config.isLocked(Setting.infiniteReward)?":lock":"")+"\n\n";
					//Use infinite tools
					line+=RMText.config_InfiniteTools+"\n";
					line+="infiniteTools="+config.getInfiniteTools()+(config.isLocked(Setting.infiniteTools)?":lock":"");
					bw.write(line);
					break;
				case ALIASES:
					line = "";
					line+="# ResourceMadness v"+pdfFile.getVersion()+" Aliases file\n\n";
					line+=RMText.config_Aliases;
					RMCommands commands = config.getCommands();
					for(RMCommand cmd : RMCommand.values()){
						List<String> aliases = commands.getAliasMap().get(cmd);
						line+="\n";
						line+=cmd.name().toLowerCase().replaceAll("_", " ");
						/*
						String str = "";
						for(String alias : aliases){
							str+=alias+", ";
						}
						if(str.length()!=0){
							str = RMText.stripLast(str, ", ");
							line+=str;
						}
						else line = RMText.stripLast(line, " ");
						*/
						String str = aliases.toString();
						str = str.replace("[", ": ");
						str = RMTextHelper.stripLast(str, "]");
						line+=str;
					}
					bw.write(line);
					break;
				case STATS:
					bw.write("# ResourceMadness v"+pdfFile.getVersion()+" Stats file\n\n");
					//Stats
					line = "";
					line += RMStats.getServerWins()+","+RMStats.getServerLosses()+","+RMStats.getServerTimesPlayed()+","+/*RMStats.getServerItemsFound()+","+*/RMStats.getServerItemsFoundTotal()+";";
					bw.write(line);
					bw.write("\n");
					break;
				case PLAYER:
					bw.write("# Resource Madness v"+pdfFile.getVersion()+" Player data file\n\n");
					for(RMPlayer rmp : RMPlayer.getPlayers().values()){
						line = "";
						line += rmp.getName()+",";
						line += rmp.getReady()+";";
						//Stats
						RMStats stats = rmp.getStats();
						line += stats.getWins()+","+stats.getLosses()+","+stats.getTimesPlayed()+","+stats.getItemsFoundTotal()+";";
						//Inventory items
						line += RMInventoryHelper.encodeInventoryToString(rmp.getItems().getItemsArray()) + ";";
						line += RMInventoryHelper.encodeInventoryToString(rmp.getReward().getItemsArray()) + ";";
						line += RMInventoryHelper.encodeInventoryToString(rmp.getTools().getItemsArray());
						bw.write(line);
						bw.write("\n");
					}
					break;
				case GAME:
					bw.write("# Resource Madness v"+pdfFile.getVersion()+" Game data file\n\n");
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
						line += config.getFoundAsReward();
						line += config.getWarnUnequal()+",";
						line += config.getAllowUnequal()+",";
						line += config.getWarnHackedItems()+",";
						line += config.getAllowHackedItems()+",";
						line += config.getInfiniteReward()+",";
						line += config.getInfiniteTools()+",";
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
							players = RMTextHelper.stripLast(players,",");
							line += players+" ";
						}
						line = RMTextHelper.stripLast(line, " ");
						line += ";";
						//Filter items
						line += RMFilter.encodeFilterToString(config.getFilter().getItems(), true)+";";
						//Game items
						line += RMFilter.encodeFilterToString(config.getItems().getItems(), true)+";";
						//Found items
						line += RMInventoryHelper.encodeInventoryToString(config.getFoundArray())+";";
						//Reward items
						line += RMInventoryHelper.encodeInventoryToString(config.getRewardArray())+";";
						//Tools items
						line += RMInventoryHelper.encodeInventoryToString(config.getToolsArray())+";";
						//Chest items
						for(RMTeam rmt : config.getTeams()){
							line += RMInventoryHelper.encodeInventoryToString(rmt.getChest().getStash().getItemsArray())+".";
						}
						line = RMTextHelper.stripLast(line, ".");
						line += ";";
						//Team items
						for(RMTeam rmt : config.getTeams()){
							line += RMFilter.encodeFilterToString(rmt.getChest().getRMItems(), true)+".";
						}
						line = RMTextHelper.stripLast(line, ".");
						bw.write(line);
						bw.write("\n");
					}
					break;
				case LOG:
					bw.write("# Resource Madness v"+pdfFile.getVersion()+" Log data file\n\n");
					for(RMGame rmGame : RMGame.getGames().values()){
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
		if(saveBackup){
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
						case ALIASES: log.log(Level.INFO, RMText.preLog+"Could not create aliases backup file."); break;
						case STATS: log.log(Level.INFO, RMText.preLog+"Could not create stats backup file."); break;
						case PLAYER: log.log(Level.INFO, RMText.preLog+"Could not create player data backup file."); break;
						case GAME: log.log(Level.INFO, RMText.preLog+"Could not create game data backup file."); break;
						case LOG: log.log(Level.INFO, RMText.preLog+"Could not create game log data backup file."); break;
					}
				}
			}
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
		load(DataType.CONFIG, false, false);
		load(DataType.LABELS, false, false);
		loadYaml(DataType.ALIASES, false);
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
			case ALIASES: file = new File(folder.getAbsolutePath()+File.separator+"aliases.yml"); break;
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
				case ALIASES:
					List<String> cmdKeys = yml.getKeys();
					RMCommands commands = config.getCommands();
					commands.clear();
					commands.initAliases();
					commands.initDefaultAliases();
					for(String cmdKey : cmdKeys){
						String str = yml.getString(cmdKey);
						if(str==null) continue;
						RMCommand cmd = RMCommand.valueOf(cmdKey.toUpperCase().replaceAll(" ", "_"));
						commands.clearAlias(cmd);
						String[] args = str.split(",");
						for(String arg : args){
							str = arg.trim();
							if(str.length()!=0){
								commands.addAlias(cmd, str);
							}
						}
					}
					break;
				case PLAYER:
					List<String> players = yml.getKeys();
					for(String player : players){
						//name
						RMPlayer rmp = new RMPlayer(player);
						String root = player+".";
						rmp.setReady(yml.getBoolean(root+"ready", false));
						rmp.setChatMode(RMHelper.getChatModeByInt(yml.getInt(root+"chatmode", -1)));
						
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
						if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("ITEMS"))){
							rmp.setItems(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//reward items
						data = yml.getString(root+"reward");
						if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("REWARD"))){
							rmp.setReward(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//tools items
						data = yml.getString(root+"tools");
						if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("TOOLS"))){
							rmp.setTools(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//templates
						
						root = player+".";
						List<String> templates = yml.getKeys(root+"templates");
						if(templates!=null){
							for(String template : templates){
								root = player+".templates."+template+".";
								RMTemplate rmTemplate = new RMTemplate(template);
								//filter
								data = yml.getString(root+"filter");
								if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("FILTER"))){
									rmTemplate.setFilterParseString(data);
								}
								//reward
								data = yml.getString(root+"reward");
								if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("REWARD"))){
									rmTemplate.setRewardParseString(data);
								}
								//tools
								data = yml.getString(root+"tools");
								if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("TOOLS"))){
									rmTemplate.setToolsParseString(data);
								}
								rmp.setTemplate(rmTemplate);
							}
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
						if(partList.getMainBlock()==null) continue;
						if((partList.getStoneList()==null)||(partList.getStoneList().size()!=2)) continue;
						config.setPartList(partList);
						
						//Fetch teams - at least two
						List<RMTeam> rmTeams = config.getPartList().fetchTeams();
						if(rmTeams.size()<2) continue;
						
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
						config.setFoundAsReward(yml.getBoolean(root+"foundasreward", this.config.getFoundAsReward()));
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
								if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("CHEST"))){
									rmTeam.getChest().setStash(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
								}
								//team items
								data = yml.getString(root+"items");
								if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("ITEMS"))){
									HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(data), true);
									rmTeam.getChest().setRMItems(rmItems);
								}
							}
						}
							
						//filter items
						root = id+".data.";
						data = yml.getString(root+"filter");
						if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("FILTER"))){
							HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(data), true);
							config.setFilter(new RMFilter(rmItems));
						}
						//game items
						data = yml.getString(root+"items");
						if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("ITEMS"))){
							HashMap<Integer, RMItem> rmItems = RMFilter.getRMItemsByStringArray(Arrays.asList(data), true);
							config.setItems(new RMFilter(rmItems));
						}
						//found items
						data = yml.getString(root+"found");
						if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("FOUND"))){
							config.setFound(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//reward items
						data = yml.getString(root+"reward");
						if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("REWARD"))){
							config.setReward(new RMStash(RMInventoryHelper.getItemStackByStringArray(data)));
						}
						//tools items
						data = yml.getString(root+"tools");
						if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("TOOLS"))){
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
						case ALIASES: System.out.println("Could not find aliases backup file"); break;
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
				case ALIASES: System.out.println("Could not find aliases file"); break;
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
			case LABELS: file = new File(folder.getAbsolutePath()+File.separator+"commands.yml"); break;
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
					if((line.startsWith("#"))||(line.length()==0)) continue;
					String[] args;
					switch(dataType){
						case LABELS:
							if(!line.contains(":")) continue;
							String key = line.substring(0, line.indexOf(":"));
							String value = line.substring(key.length()+1).trim();
							RMDebug.warning("key: "+key);
							RMDebug.warning("value: "+value);
							break;
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
									if(args[0].equalsIgnoreCase("minPlayers")) config.setSetting(Setting.minPlayers, RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("maxPlayers")) config.setSetting(Setting.maxPlayers, RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("minTeamPlayers")) config.setSetting(Setting.minTeamPlayers, RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("maxTeamPlayers")) config.setSetting(Setting.maxTeamPlayers, RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("timeLimit")) config.setSetting(Setting.timeLimit, RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("autoRandomizeAmount")) config.setSetting(Setting.autoRandomizeAmount, RMHelper.getIntByString(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("advertise")) config.setSetting(Setting.advertise, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("autoRestoreWorld")) config.setSetting(Setting.autoRestoreWorld, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("warpToSafety")) config.setSetting(Setting.warpToSafety, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("allowMidgameJoin")) config.setSetting(Setting.allowMidgameJoin, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("healPlayer")) config.setSetting(Setting.healPlayer, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("clearPlayerInventory")) config.setSetting(Setting.clearPlayerInventory, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("foundAsReward")) config.setSetting(Setting.foundAsReward, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("warnUnequal")) config.setSetting(Setting.warnUnequal, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("allowUnequal")) config.setSetting(Setting.allowUnequal, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("warnHackedItems")) config.setSetting(Setting.warnHackedItems, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("allowHackedItems")) config.setSetting(Setting.allowHackedItems, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("infiniteReward")) config.setSetting(Setting.infiniteReward, Boolean.parseBoolean(args[1]), lockArg);
									else if(args[0].equalsIgnoreCase("infiniteTools")) config.setSetting(Setting.infiniteTools, Boolean.parseBoolean(args[1]), lockArg);
								}
							}
							break;
						case STATS:
							args = line.split(",");
							//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
							RMStats.setServerWins(RMHelper.getIntByString(args[0]));
							RMStats.setServerLosses(RMHelper.getIntByString(args[1]));
							RMStats.setServerTimesPlayed(RMHelper.getIntByString(args[2]));
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
		config.setFoundAsReward(Boolean.parseBoolean(args[12]));
		config.setWarnUnequal(Boolean.parseBoolean(args[13]));
		config.setAllowUnequal(Boolean.parseBoolean(args[14]));
		config.setWarnHackedItems(Boolean.parseBoolean(args[15]));
		config.setAllowHackedItems(Boolean.parseBoolean(args[16]));
		config.setInfiniteReward(Boolean.parseBoolean(args[17]));
		config.setInfiniteTools(Boolean.parseBoolean(args[18]));

		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
		args = strArgs[2].split(",");
		RMStats gameStats = config.getStats();
		
		gameStats.setWins(RMHelper.getIntByString(args[0]));
		gameStats.setLosses(RMHelper.getIntByString(args[1]));
		gameStats.setTimesPlayed(RMHelper.getIntByString(args[2]));
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
		if(arg.contains(RMText.m_Stack)) useDefaultAmount = true;
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
		if(arg.contains(RMText.m_Stack)){
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
		if(arg.contains(RMText.m_FilterTypeAll)) type = FilterType.ALL;
		else if(arg.contains(RMText.m_FilterTypeBlock)) type = FilterType.BLOCK;
		else if(arg.contains(RMText.m_FilterTypeItem)) type = FilterType.ITEM;
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
			if(args.get(0).equalsIgnoreCase(RMText.c_FilterRewardToolsClear)){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.clear")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.clear")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.clear")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break; 
				}
				force = ForceState.CLEAR;
				rmp.setRequestFilter(null, filterState, type, force, randomize);
				return;
			}
		}
		else if(args.size()>1){
			if(args.get(0).equalsIgnoreCase(RMText.c_FilterRewardToolsAdd)){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.add")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.add")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.add")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break; 
				}
				force = ForceState.ADD;
				size+=1;
			}
			else if(args.get(0).equalsIgnoreCase(RMText.c_FilterRewardToolsSubtract)){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.subtract")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.subtract")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.subtract")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break; 
				}
				force = ForceState.SUBTRACT;
				size+=1;
			}
			else if(args.get(0).equalsIgnoreCase(RMText.c_FilterRandom)){
				if(filterState==FilterState.FILTER){
					if(!rmp.hasPermission("resourcemadness.filter.random")){
						rmp.sendMessage(RMText.e_NoPermissionCommand);
						return;
					}
					randomize = RMHelper.getIntByString(args.get(1));
					if(randomize>0) size+=2;
				}
			}
			else if(args.get(0).equalsIgnoreCase(RMText.c_FilterRewardToolsClear)){
				force = ForceState.CLEAR;
				size+=1;
			}
			if(args.size()>2){
				if(args.get(1).equalsIgnoreCase(RMText.c_FilterRandom)){
					if(filterState==FilterState.FILTER){
						if(!rmp.hasPermission("resourcemadness.filter.random")){
							rmp.sendMessage(RMText.e_NoPermissionCommand);
							return;
						}
						randomize = RMHelper.getIntByString(args.get(2));
						if(randomize>0) size+=2;
					}
				}
			}
		}
		if(force == ForceState.CLEAR){
			switch(filterState){
			case FILTER: if(!rmp.hasPermission("resourcemadness.filter.clear")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
			case REWARD: if(!rmp.hasPermission("resourcemadness.reward.clear")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
			case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.clear")){ rmp.sendMessage(RMText.e_NoPermissionCommand); return; } break;
			}
		}
		if(args.size()>0){
			args = args.subList(size, args.size());
			String arg0 = args.get(0);
			type = getTypeFromArg(arg0);
			
			if(type!=null){
				switch(filterState){
				case FILTER:
					RMDebug.warning("TYPE NOT NULL");
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
				RMDebug.warning(RMTextHelper.getStringByStringList(args, ", "));
				HashMap<Integer, Integer[]> hashItems = RMFilter.getItemsByStringArray(args, false);
				switch(filterState){
					case FILTER:
						RMDebug.warning("HASH ITEMS SIZE:"+hashItems.size());
						RMDebug.warning("TYPE IS NULL");
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
				RMDebug.warning("RETURN");
				return;
			}
			HashMap<Integer, RMItem> rmItems = new HashMap<Integer, RMItem>();
			switch(filterState){
				case FILTER:
					for(int i=0; i<items.size(); i++){
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
			RMDebug.warning("FINISH");
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
	
	public void sendAliasesById(String arg, RMPlayer rmp){
		int listLimit = 12;
		int id = RMHelper.getIntByString(arg);
		
		HashMap<RMCommand, List<String>> aliasMap = config.getCommands().getAliasMap();
		HashMap<Integer, RMCommand> numAliasMap = new HashMap<Integer, RMCommand>();
		int i = 0;
		for(RMCommand cmd : RMCommand.values()){
			List<String> aliases = aliasMap.get(cmd);
			if(aliases == null) continue;
			if(aliases.size()!=0){
				RMDebug.warning(cmd.name());
				RMDebug.warning("size:"+aliases.size());
				numAliasMap.put(i, cmd);
				i++;
			}
		}
		
		if(numAliasMap.size()==0){
			rmp.sendMessage(RMText.e_NoAliasesYet);
			return;
		}
		if(id<1) id=1;
		int size = (int)Math.ceil((double)numAliasMap.size()/(double)listLimit);
		if(id>size) id=1;
		i=(id-1)*listLimit;
		rmp.sendMessage(RMText.iAliasList(id, size));
		int found = 0;
		while((found<listLimit)&&(i<numAliasMap.size())){
			RMCommand cmd = numAliasMap.get(i);
			String strCmd = cmd.name().toLowerCase().replaceAll("_", " ");
			String aliases = RMTextHelper.getStringByStringList(aliasMap.get(cmd), ", ");
			rmp.sendMessage(ChatColor.WHITE+strCmd+": "+ChatColor.GREEN+aliases);
			i++;
			found++;
		}
	}
		
	public void sendTemplateListById(String arg, RMPlayer rmp){
		int listLimit = 5;
		int strLength = 74;
		int id = RMHelper.getIntByString(arg);
		HashMap<String, RMTemplate> rmpTemplates = rmp.getTemplates();
		String[] templates = rmpTemplates.keySet().toArray(new String[rmpTemplates.size()]);
		Arrays.sort(templates);
		if(templates.length==0){
			rmp.sendMessage(RMText.e_NoTemplateYet);
			return;
		}
		if(id<1) id=1;
		int size = (int)Math.ceil((double)templates.length/(double)listLimit);
		if(id>size) id=1;
		int i=(id-1)*listLimit;
		rmp.sendMessage(RMText.l_TemplateList(id, size));
		int found = 0;
		while((found<listLimit)&&(i<templates.length)){
			RMTemplate rmTemplate = rmpTemplates.get(templates[i]);
			rmp.sendMessage(""+ChatColor.YELLOW+found+" "+ChatColor.GREEN+templates[i]);
			RMFilter filter = rmTemplate.getFilter();
			RMStash reward = rmTemplate.getReward();
			RMStash tools = rmTemplate.getTools();
			
			List<ItemStack> filterItems = RMFilter.convertToListItemStack(filter.getItems());
			List<ItemStack> rewardItems = tools.getItems();
			List<ItemStack> toolsItems = tools.getItems();
			
			String strFilter = RMTextHelper.getStringSortedItems(filterItems, 0);
			String strReward = RMTextHelper.getStringSortedItems(rewardItems, 0);
			String strTools = RMTextHelper.getStringSortedItems(toolsItems, 0);
			
			
			if(strFilter.length()>strLength) strFilter = strFilter.substring(0, strLength)+"...";
			if(strReward.length()>strLength) strReward = strReward.substring(0, strLength)+"...";
			if(strTools.length()>strLength) strTools = strTools.substring(0, strLength)+"...";
			
			if(filter.size()!=0) rmp.sendMessage(ChatColor.WHITE+RMText.l_TemplateListFilter+": "+ChatColor.GREEN+filter.size()+ChatColor.WHITE+" "+RMText.l_TemplateListTotal+": "+ChatColor.GREEN+filter.getItemsTotal()+(filter.getItemsTotalHigh()>0?ChatColor.WHITE+"-"+filter.getItemsTotalHigh():"")+" "+strFilter+ChatColor.WHITE);
			if(reward.size()!=0) rmp.sendMessage(ChatColor.WHITE+RMText.l_TemplateListReward+": "+ChatColor.GREEN+reward.size()+ChatColor.WHITE+" "+RMText.l_TemplateListTotal+": "+ChatColor.GREEN+reward.getAmount()+" "+strReward+ChatColor.WHITE);
			if(tools.size()!=0) rmp.sendMessage(ChatColor.WHITE+RMText.l_TemplateListTools+": "+ChatColor.GREEN+tools.size()+ChatColor.WHITE+" "+RMText.l_TemplateListTotal+": "+ChatColor.GREEN+tools.getAmount()+" "+strTools+ChatColor.WHITE);
			i++;
			found++;
		}
	}
	
	public void sendListById(String arg, RMPlayer rmp){
		int id = RMHelper.getIntByString(arg);
		sendListByInt(id, rmp);
	}
	
	public void sendListByInt(int id, RMPlayer rmp){
		int listLimit = 5;
		HashMap<Integer, RMGame> rmGames = RMGame.getGames();
		Integer[] games = RMGame.getAdvertisedGames();
		Arrays.sort(games);
		if(games.length==0){
			rmp.sendMessage(RMText.e_NoGamesYet);
			return;
		}
		if(id<1) id=1;
		int size = (int)Math.ceil((double)games.length/(double)listLimit);
		if(id>size) id=1;
		int i=(id-1)*listLimit;
		rmp.sendMessage(RMText.l_List(id, size));
		
		int found = 0;
		while((found<listLimit)&&(i<games.length)){
			int listId = games[i];
			RMGame rmGame = rmGames.get(listId);
			i++;
			found++;
			rmp.sendMessage(ChatColor.AQUA+RMTextHelper.firstLetterToUpperCase(rmGame.getConfig().getWorldName())+ChatColor.WHITE+
					" "+RMTextHelper.firstLetterToUpperCase(RMText.l_ListId)+": "+ChatColor.YELLOW+rmGame.getConfig().getId()+ChatColor.WHITE+
					" "+RMTextHelper.firstLetterToUpperCase(RMText.l_ListOwner)+": "+ChatColor.YELLOW+rmGame.getConfig().getOwnerName()+ChatColor.WHITE+
					" "+RMTextHelper.firstLetterToUpperCase(RMText.l_ListTimeLimit)+": "+rmGame.getText(Setting.timeLimit));
			rmp.sendMessage(RMText.l_ListPlayers+": "+ChatColor.GREEN+rmGame.getTeamPlayers().length+ChatColor.WHITE+
					" "+RMText.l_ListInGame+": "+rmGame.getText(Setting.minPlayers)+ChatColor.WHITE+"-"+rmGame.getText(Setting.maxPlayers)+ChatColor.WHITE+
					" "+RMText.l_ListInTeam+": "+rmGame.getText(Setting.minTeamPlayers)+ChatColor.WHITE+"-"+rmGame.getText(Setting.maxTeamPlayers));
			rmp.sendMessage(RMText.l_ListTeams+": "+rmGame.getTextTeamColors());
		}
	}
	
	public void rmInfo(RMPlayer rmp, int page){
		int pageLimit = 2;
		if(page<=0) page = 1;
		if(page>pageLimit) page = pageLimit;
		rmp.sendMessage(RMText.rmInfo(page, pageLimit));
		rmp.sendMessage(RMText.d_GrayGreenOptional);
		if(page==1){
			if(rmp.hasPermission("resourcemadness.add")) rmp.sendMessage(RMText.rmInfo_Add);
			if(rmp.hasPermission("resourcemadness.remove")) rmp.sendMessage(RMText.rmInfo_Remove);
			if(rmp.hasPermission("resourcemadness.list")) rmp.sendMessage(RMText.rmInfo_List);
			if(rmp.hasPermission("resourcemadness.commands")) rmp.sendMessage(RMText.rmInfo_Commands);
			//Info/Settings
			String line="";
			if(rmp.hasPermission("resourcemadness.info.found")) line+=RMText.c_InfoFound+"/";
			if(rmp.hasPermission("resourcemadness.info.items")) line+=RMText.c_InfoItems+"/";
			if(rmp.hasPermission("resourcemadness.info.reward")) line+=RMText.c_InfoReward+"/";
			if(rmp.hasPermission("resourcemadness.info.tools")) line+=RMText.c_InfoTools+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(RMText.rmInfo_Info(line));
			
			line ="";
			if(rmp.hasPermission("resourcemadness.info.settings.reset")) line+=RMText.c_SettingsReset;
			if(rmp.hasPermission("resourcemadness.info.settings")) rmp.sendMessage(RMText.rmInfo_Settings(line));
			if(rmp.hasPermission("resourcemadness.set")) rmp.sendMessage(RMText.rmInfo_Set);
			
			//Mode
			line="";
			if(rmp.hasPermission("resourcemadness.mode.filter")) line+=RMText.c_ModeFilter+"/";
			if(rmp.hasPermission("resourcemadness.mode.reward")) line+=RMText.c_ModeReward+"/";
			if(rmp.hasPermission("resourcemadness.mode.tools")) line+=RMText.c_ModeTools+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) if(rmp.hasPermission("resourcemadness.mode")) rmp.sendMessage(RMText.rmInfo_Mode(line));
			
			if(rmp.hasPermission("resourcemadness.filter")) rmp.sendMessage(RMText.rmInfo_Filter(rmp));
			if(rmp.hasPermission("resourcemadness.reward")) rmp.sendMessage(RMText.rmInfo_Reward(rmp));
			if(rmp.hasPermission("resourcemadness.tools")) rmp.sendMessage(RMText.rmInfo_Tools(rmp));
			
			line="";
			if(rmp.hasPermission("resourcemadness.template.list")) line+=RMText.c_TemplateList+"/";
			if(rmp.hasPermission("resourcemadness.template.load")) line+=RMText.c_TemplateLoad+"/";
			if(rmp.hasPermission("resourcemadness.template.save")) line+=RMText.c_TemplateSave+"/";
			if(rmp.hasPermission("resourcemadness.template.remove")) line+=RMText.c_TemplateRemove+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(rmp.hasPermission("resourcemadness.template")) rmp.sendMessage(RMText.rmInfo_Template(line));
		}
		else if(page==2){
			if(rmp.hasPermission("resourcemadness.start")) rmp.sendMessage(RMText.rmInfo_Start);
			
			//Restart/Stop
			String line="";
			//if(rmp.hasPermission("resourcemadness.restart")) line+="Restart/";
			if(rmp.hasPermission("resourcemadness.stop")) line+=RMText.c_Stop+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(RMText.rmInfo_Stop(line));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.pause")) line+=RMText.c_Pause+"/";
			if(rmp.hasPermission("resourcemadness.resume")) line+=RMText.c_Resume+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(RMText.rmInfo_Pause(line));
			
			if(rmp.hasPermission("resourcemadness.restore")) rmp.sendMessage(RMText.rmInfo_Restore);
			if(rmp.hasPermission("resourcemadness.join")) rmp.sendMessage(RMText.rmInfo_Join);
			if(rmp.hasPermission("resourcemadness.quit")) rmp.sendMessage(RMText.rmInfo_Quit);
			if(rmp.hasPermission("resourcemadness.ready")) rmp.sendMessage(RMText.rmInfo_Ready);
			
			line = "";
			if(rmp.hasPermission("resourcemadness.chat.world")) line+=RMText.c_ChatWorld+"/";
			if(rmp.hasPermission("resourcemadness.chat.game")) line+=RMText.c_ChatGame+"/";
			if(rmp.hasPermission("resourcemadness.chat.team")) line+=RMText.c_ChatTeam+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) if(rmp.hasPermission("resourcemadness.chat")) rmp.sendMessage(RMText.rmInfo_Chat(line));
			
			if(rmp.hasPermission("resourcemadness.items")) rmp.sendMessage(RMText.rmInfo_Items);
			if(rmp.hasPermission("resourcemadness.item")) rmp.sendMessage(RMText.rmInfo_Item);
			
			line = "";
			if(rmp.hasPermission("resourcemadness.claim.found")) line+=RMText.c_ClaimFound+"/";
			if(rmp.hasPermission("resourcemadness.claim.items")) line+=RMText.c_ClaimItems+"/";
			if(rmp.hasPermission("resourcemadness.claim.reward")) line+=RMText.c_ClaimReward+"/";
			if(rmp.hasPermission("resourcemadness.claim.tools")) line+=RMText.c_ClaimTools+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(rmp.hasPermission("resourcemadness.claim")) rmp.sendMessage(RMText.rmInfo_Claim(line));
		}
	}
	
	public void rmSetInfo(RMPlayer rmp, int page){
		if(rmp.hasPermission("resourcemadness.set")){
			int pageLimit = 2;
			if(page<=0) page = 1;
			if(page>pageLimit) page = pageLimit;
			rmp.sendMessage(RMText.rmSetInfo(page, pageLimit));
			if(page==1){
				if(rmp.hasPermission("resourcemadness.set.minplayers")) if(!config.getLock().contains(Setting.minPlayers)) rmp.sendMessage(RMText.setInfo_MinPlayers);
				if(rmp.hasPermission("resourcemadness.set.maxplayers")) if(!config.getLock().contains(Setting.maxPlayers)) rmp.sendMessage(RMText.setInfo_MaxPlayers);
				if(rmp.hasPermission("resourcemadness.set.minteamplayers")) if(!config.getLock().contains(Setting.minTeamPlayers)) rmp.sendMessage(RMText.setInfo_MinTeamPlayers);
				if(rmp.hasPermission("resourcemadness.set.maxteamplayers")) if(!config.getLock().contains(Setting.maxTeamPlayers)) rmp.sendMessage(RMText.setInfo_MaxTeamPlayers);
				if(rmp.hasPermission("resourcemadness.set.timelimit")) if(!config.getLock().contains(Setting.timeLimit)) rmp.sendMessage(RMText.setInfo_TimeLimit);
				if(rmp.hasPermission("resourcemadness.set.random")) if(!config.getLock().contains(Setting.autoRandomizeAmount)) rmp.sendMessage(RMText.setInfo_Random);
				if(rmp.hasPermission("resourcemadness.set.advertise")) if(!config.getLock().contains(Setting.advertise)) rmp.sendMessage(RMText.setInfo_Advertise);
				if(rmp.hasPermission("resourcemadness.set.restore")) if(!config.getLock().contains(Setting.autoRestoreWorld)) rmp.sendMessage(RMText.setInfo_Restore);
				if(rmp.hasPermission("resourcemadness.set.warp")) if(!config.getLock().contains(Setting.warpToSafety)) rmp.sendMessage(RMText.setInfo_Warp);
				if(rmp.hasPermission("resourcemadness.set.midgamejoin")) if(!config.getLock().contains(Setting.allowMidgameJoin)) rmp.sendMessage(RMText.setInfo_MidgameJoin);
				if(rmp.hasPermission("resourcemadness.set.healplayer")) if(!config.getLock().contains(Setting.healPlayer)) rmp.sendMessage(RMText.setInfo_HealPlayer);
				if(rmp.hasPermission("resourcemadness.set.clearinventory")) if(!config.getLock().contains(Setting.clearPlayerInventory)) rmp.sendMessage(RMText.setInfo_ClearInventory);
				if(rmp.hasPermission("resourcemadness.set.foundasreward")) if(!config.getLock().contains(Setting.foundAsReward)) rmp.sendMessage(RMText.setInfo_FoundAsReward);
			}
			else if(page==2){
				if(rmp.hasPermission("resourcemadness.set.warnunequal")) if(!config.getLock().contains(Setting.warnUnequal)) rmp.sendMessage(RMText.setInfo_WarnUnequal);
				if(rmp.hasPermission("resourcemadness.set.allowunequal")) if(!config.getLock().contains(Setting.allowUnequal)) rmp.sendMessage(RMText.setInfo_AllowUnequal);
				if(rmp.hasPermission("resourcemadness.set.warnhacked")) if(!config.getLock().contains(Setting.warnHackedItems)) rmp.sendMessage(RMText.setInfo_WarnHacked);
				if(rmp.hasPermission("resourcemadness.set.allowhacked")) if(!config.getLock().contains(Setting.allowHackedItems)) rmp.sendMessage(RMText.setInfo_AllowHacked);
				if(rmp.hasPermission("resourcemadness.set.infinitereward")) if(!config.getLock().contains(Setting.infiniteReward)) rmp.sendMessage(RMText.setInfo_InfiniteReward);
				if(rmp.hasPermission("resourcemadness.set.infinitetools")) if(!config.getLock().contains(Setting.infiniteTools)) rmp.sendMessage(RMText.setInfo_InfiniteTools);
			}
		}
	}
	
	public void rmFilterInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.filter")){
			rmp.sendMessage(RMText.filterInfo);
			if(rmp.hasPermission("resourcemadness.filter.set")) rmp.sendMessage(RMText.filterInfo_Set);
			if(rmp.hasPermission("resourcemadness.filter.random")) rmp.sendMessage(RMText.filterInfo_Random);
			if(rmp.hasPermission("resourcemadness.filter.add")) rmp.sendMessage(RMText.filterInfo_Add);
			if(rmp.hasPermission("resourcemadness.filter.subtract")) rmp.sendMessage(RMText.filterInfo_Subtract);
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(RMText.filterInfo_Clear);
			rmp.sendMessage(ChatColor.GOLD+RMText.m_Examples+":");
			if(rmp.hasPermission("resourcemadness.filter.info")) rmp.sendMessage(RMText.filterExample_Info);
			if(rmp.hasPermission("resourcemadness.filter.set")) rmp.sendMessage(RMText.filterExample_Set);
			if(rmp.hasPermission("resourcemadness.filter.random")) rmp.sendMessage(RMText.filterExample_Random);
			if(rmp.hasPermission("resourcemadness.filter.add")) rmp.sendMessage(RMText.filterExample_Add);
			if(rmp.hasPermission("resourcemadness.filter.subtract")) rmp.sendMessage(RMText.filterExample_Subtract);
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(RMText.filterExample_Clear1);
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(RMText.filterExample_Clear2);
		}
	}
	
	public void rmRewardInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(RMText.rewardInfo);
			if(rmp.hasPermission("resourcemadness.reward.set")) rmp.sendMessage(RMText.rewardInfo_Set);
			if(rmp.hasPermission("resourcemadness.reward.add")) rmp.sendMessage(RMText.rewardInfo_Add);
			if(rmp.hasPermission("resourcemadness.reward.subtract")) rmp.sendMessage(RMText.rewardInfo_Subtract);
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(RMText.rewardInfo_Clear);
			rmp.sendMessage(ChatColor.GOLD+RMText.m_Examples+":");
			if(rmp.hasPermission("resourcemadness.reward.info")) rmp.sendMessage(RMText.rewardExample_Info);
			if(rmp.hasPermission("resourcemadness.reward.set")) rmp.sendMessage(RMText.rewardExample_Set);
			if(rmp.hasPermission("resourcemadness.reward.add")) rmp.sendMessage(RMText.rewardExample_Add);
			if(rmp.hasPermission("resourcemadness.reward.subtract")) rmp.sendMessage(RMText.rewardExample_Subtract);
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(RMText.rewardExample_Clear1);
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(RMText.rewardExample_Clear2);
		}
	}
	
	public void rmToolsInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(RMText.toolsInfo);
			if(rmp.hasPermission("resourcemadness.tools.set")) rmp.sendMessage(RMText.toolsInfo_Set);
			if(rmp.hasPermission("resourcemadness.tools.add")) rmp.sendMessage(RMText.toolsInfo_Add);
			if(rmp.hasPermission("resourcemadness.tools.subtract")) rmp.sendMessage(RMText.toolsInfo_Subtract);
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(RMText.toolsInfo_Clear);
			rmp.sendMessage(ChatColor.GOLD+RMText.m_Examples+":");
			if(rmp.hasPermission("resourcemadness.tools.info")) rmp.sendMessage(RMText.toolsExample_Info);
			if(rmp.hasPermission("resourcemadness.tools.set")) rmp.sendMessage(RMText.toolsExample_Set);
			if(rmp.hasPermission("resourcemadness.tools.add")) rmp.sendMessage(RMText.toolsExample_Add);
			if(rmp.hasPermission("resourcemadness.tools.subtract")) rmp.sendMessage(RMText.toolsExample_Subtract);
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(RMText.toolsExample_Clear1);
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(RMText.toolsExample_Clear2);
		}
	}

	public void rmTemplateInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.template")){
			rmp.sendMessage(RMText.templateInfo);
			if(rmp.hasPermission("resourcemadness.template.list")) rmp.sendMessage(RMText.templateInfo_List);
			if(rmp.hasPermission("resourcemadness.template.load")) rmp.sendMessage(RMText.templateInfo_Load);
			if(rmp.hasPermission("resourcemadness.template.save")) rmp.sendMessage(RMText.templateInfo_Save);
			if(rmp.hasPermission("resourcemadness.template.remove")) rmp.sendMessage(RMText.templateInfo_Remove);
			rmp.sendMessage(ChatColor.GOLD+RMText.m_Examples+":");
			if(rmp.hasPermission("resourcemadness.template.list")) rmp.sendMessage(RMText.templateExample_List);
			if(rmp.hasPermission("resourcemadness.template.load")) rmp.sendMessage(RMText.templateExample_Load);
			if(rmp.hasPermission("resourcemadness.template.save")) rmp.sendMessage(RMText.templateExample_Save);
			if(rmp.hasPermission("resourcemadness.template.remove")) rmp.sendMessage(RMText.templateExample_Remove);
		}
	}
	
	public void rmClaimInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.claim")){
			rmp.sendMessage(RMText.claimInfo);
			if(rmp.hasPermission("resourcemadness.claim.found")) rmp.sendMessage(RMText.claimInfo_Found);
			if(rmp.hasPermission("resourcemadness.claim.items")) rmp.sendMessage(RMText.claimInfo_Items);
			if(rmp.hasPermission("resourcemadness.claim.reward")) rmp.sendMessage(RMText.claimInfo_Reward);
			if(rmp.hasPermission("resourcemadness.claim.tools")) rmp.sendMessage(RMText.claimInfo_Tools);
			rmp.sendMessage(ChatColor.GOLD+RMText.m_Examples+":");
			if(rmp.hasPermission("resourcemadness.claim.found")) rmp.sendMessage(RMText.claimExample_Found);
			if(rmp.hasPermission("resourcemadness.claim.found.chest")) rmp.sendMessage(RMText.claimExample_FoundChest);
			if(rmp.hasPermission("resourcemadness.claim.items")) rmp.sendMessage(RMText.claimExample_Items);
			if(rmp.hasPermission("resourcemadness.claim.items.chest")) rmp.sendMessage(RMText.claimExample_ItemsChest);
			if(rmp.hasPermission("resourcemadness.claim.reward")) rmp.sendMessage(RMText.claimExample_Reward);
			if(rmp.hasPermission("resourcemadness.claim.reward.chest")) rmp.sendMessage(RMText.claimExample_RewardChest);
			if(rmp.hasPermission("resourcemadness.claim.tools")) rmp.sendMessage(RMText.claimExample_Tools);
			if(rmp.hasPermission("resourcemadness.claim.tools.chest")) rmp.sendMessage(RMText.claimExample_ToolsChest);
		}
	}
	
	public void rmChatInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.chat")){
			rmp.sendMessage(RMText.chatInfo);
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(RMText.chatInfo_World);
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(RMText.chatInfo_Game);
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(RMText.chatInfo_Team);
			rmp.sendMessage(ChatColor.GOLD+RMText.m_Examples+":");
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(RMText.chatExample_World);
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(RMText.chatExample_WorldMessage);
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(RMText.chatExample_Game);
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(RMText.chatExample_GameMessage);
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(RMText.chatExample_Team);
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(RMText.chatExample_TeamMessage);
		}
	}
	
	public void rmItemInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.item")){
			rmp.sendMessage(RMText.itemInfo);
			rmp.sendMessage(RMText.itemInfo_Arg);
			rmp.sendMessage(ChatColor.GOLD+RMText.m_Examples);
			rmp.sendMessage(RMText.itemExample1);
			rmp.sendMessage(RMText.itemExample2);
			rmp.sendMessage(RMText.itemExample3);
			rmp.sendMessage(RMText.itemExample4);
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