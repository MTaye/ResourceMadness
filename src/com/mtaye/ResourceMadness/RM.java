package com.mtaye.ResourceMadness;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.bukkit.ChatColor;

import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
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

import java.io.*;

import org.bukkit.configuration.file.YamlConfiguration;

import com.nijikokun.register.payment.Method;

import com.mtaye.ResourceMadness.RMCommands.RMCommand;
import com.mtaye.ResourceMadness.RMGame.FilterItemType;
import com.mtaye.ResourceMadness.RMGame.FilterState;
import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.RMPlayer.ChatMode;
import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;
import com.mtaye.ResourceMadness.RMStats.RMStat;
import com.mtaye.ResourceMadness.RMStats.RMStatServer;
import com.mtaye.ResourceMadness.Helper.RMHelper;
import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;
import com.mtaye.ResourceMadness.Helper.RMLogHelper;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.setting.SettingBool;
import com.mtaye.ResourceMadness.setting.SettingInt;
import com.mtaye.ResourceMadness.setting.SettingLibrary;
import com.mtaye.ResourceMadness.setting.SettingPrototype;
import com.mtaye.ResourceMadness.setting.SettingStr;
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
	public RMConfig config;
	
	public static enum ClaimType { ITEMS, FOUND, REWARD, TOOLS, CHEST, NONE };
	public static enum DataType { CONFIG, ALIASES, STATS, PLAYER, GAME, LOG, TEMPLATE, LABELS };
	public static enum FolderType { PLUGIN, BACKUP, LANGUAGES };
	public static enum DataSave { SUCCESS, FAIL, NO_DATA };
	
	public boolean useRegister = false;
	private RMWatcher watcher;
	private int watcherid;
	
	public PermissionHandler permissions = null;
	public PermissionManager permissionsEx = null;
	public boolean permissionBukkit = false;
	public Method economy = null;
	
	RMLogHelper rmLogHelper;
	
	public RM(){
	}
	
	@Override
	public void onEnable(){
		pdfFile = this.getDescription();
		RMPlayer.rm = this;
		RMDebug.rm = this;
		RMGame.rm = this;
		RMText.rm = this;
		RMTextHelper.rm = this;
		RMHelper.rm = this;
		
		log = getServer().getLogger();
		log.log(Level.INFO, pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled!" );
        RMDebug.enable();

        //setupPermissions();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new RMPlayerListener(this), this);
		pm.registerEvents(new RMBlockListener(this), this);
		pm.registerEvents(new RMEntityListener(this), this);
		//RMConfig.load();
		
		rmLogHelper = new RMLogHelper(this);
		
        if (getServer().getScheduler().scheduleSyncDelayedTask(this,
        		new Runnable(){
        			public void run(){
        				loadWhenReady();
                	}
        		})==-1)
        {
        	loadWhenReady();
        }
	}
	
	public void loadWhenReady(){
		loadAll();
		getServer().getPluginManager().registerEvents(new RMPermissionListener(this), this);
		watcher = new RMWatcher(this);
		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 20, 20);
		//setupPermissions();
	}
	
	public void onDisable(){
		saveAll();
		getServer().getScheduler().cancelTask(watcherid);
		log.log(Level.INFO, pdfFile.getName() + " v" + pdfFile.getVersion() + " disabled!" );
		//RMConfig.save();
	}
	
	public RMConfig getRMConfig(){
		return config;
	}
	
	public void setRMConfig(RMConfig config){
		this.config = config;
	}
	
	/*
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
					    if(permissionsEx==null) log.log(Level.WARNING, RMText.preLog+"PermissionsEx plugin is not enabled!");
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
	*/
	
	public boolean isPermissionEnabled(){
		switch(config.getPermissionType()){
			case P3: case PEX: case BUKKIT: return true;
			case FALSE: default: return false;
		}
	}
	
	public boolean hasPermission(Player player, String node){
		//if((permissions==null)&&(permissionsEx==null)&&(!permissionBukkit)) return true;
		if(player==null) return false;
		else{
			switch(config.getPermissionType()){
				case BUKKIT:
					if(player.hasPermission("*")) return true;
					else if(node=="resourcemadness.admin.save") return player.hasPermission("resourcemadness.admin.save");
					else if(player.hasPermission("resourcemadness.admin")) return true;
					else if((player.hasPermission("resourcemadness.owner"))&&(node!="resourcemadness.admin")) return true;
					else return player.hasPermission(node);
				case P3:
					if(permissions.has(player, "*")) return true;
					else if(node=="resourcemadness.admin.save") return permissions.has(player, "resourcemadness.admin.save");
					else if(permissions.has(player, "resourcemadness.admin")) return true;
					else if((permissions.has(player, "resourcemadness.owner"))&&(node!="resourcemadness.admin")) return true;
					else return permissions.has(player, node);
				case PEX:
					if(permissionsEx.has(player, "*")) return true;
					else if(node=="resourcemadness.admin.save") return permissionsEx.has(player, "resourcemadness.admin.save");
					else if(permissionsEx.has(player, "resourcemadness.admin")) return true;
					else if((permissionsEx.has(player, "resourcemadness.owner"))&&(node!="resourcemadness.admin")) return true;
					else return permissionsEx.has(player, node);
				case FALSE: default:
					return true;
			}
		}
	}
	
	public String[] processCommandArgs(String[] args){
		if(args.length!=0){
			String strArg = RMTextHelper.getTextFromArgs(args);
			strArg = processBestMatch(strArg);
			if(strArg!=null) return strArg.trim().split(" ");
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
		return arg.replaceFirst(bestAlias, config.getCommands().getCommandMap().get(bestCommand));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player p = null;
		if(sender.getClass().getName().contains("Player")){
			p = (Player)sender;
			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(cmd.getName().equals("resourcemadness")){
					if(!rmp.hasPermission("resourcemadness")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
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
						if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.add"))){
							if(!rmp.hasPermission("resourcemadness.add")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							rmp.setPlayerAction(PlayerAction.ADD);
							rmp.sendMessage(RMText.getLabel("action.add"));
							return true;
						}
						//REMOVE
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.remove"))){
							if(!rmp.hasPermission("resourcemadness.remove")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(rmGame!=null) RMGame.tryRemoveGame(rmGame, rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.REMOVE);
								rmp.sendMessage(RMText.getLabel("action.remove"));
							}
							return true;
						}
						//LIST
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.list"))){
							if(!rmp.hasPermission("resourcemadness.list")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length==2) sendListById(args[1], rmp);
							else sendListByInt(0, rmp);
							return true;
						}
						//ALIASES
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.commands"))){
							if(!rmp.hasPermission("resourcemadness.commands")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length==2) sendAliasesById(args[1], rmp);
							else sendAliasesById("0", rmp);
							return true;
						}
						//INFO
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.info"))){
							if(!rmp.hasPermission("resourcemadness.info")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.info.found"))){
									if(!rmp.hasPermission("resourcemadness.info.found")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(rmGame!=null) rmGame.getInfoFound(rmp);
									else{
										rmp.setPlayerAction(PlayerAction.INFO_FOUND);
										rmp.sendMessage(RMText.getLabel("action.info.found"));
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.info.claim"))){
									if(!rmp.hasPermission("resourcemadness.info.claim")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									rmp.getInfoClaim();
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.info.items"))){
									if(!rmp.hasPermission("resourcemadness.info.items")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									rmp.getInfoItems();
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.info.reward"))){
									if(!rmp.hasPermission("resourcemadness.info.reward")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									rmp.getInfoReward();
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.info.tools"))){
									if(!rmp.hasPermission("resourcemadness.info.tools")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									rmp.getInfoTools();
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.sendInfo(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.INFO);
									rmp.sendMessage(RMText.getLabel("action.info"));
								}
								return true;
							}
						}
						//SETTINGS
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.settings"))){
							if(!rmp.hasPermission("resourcemadness.settings")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if((args.length==2)&&(args[1].equalsIgnoreCase(RMText.getLabel("cmd.settings.reset")))){
								if(!rmp.hasPermission("resourcemadness.settings")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
								if(rmGame!=null) rmGame.resetSettings(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.SETTINGS_RESET);
									rmp.sendMessage(RMText.getLabel("action.settings.reset"));
								}
								return true;
							}
							page = 0;
							if(args.length>1) page = RMHelper.getIntByString(args[1]);
							if(rmGame!=null) rmGame.sendSettings(rmp, page);
							else{
								rmp.setRequestInt(page);
								rmp.setPlayerAction(PlayerAction.SETTINGS);
								rmp.sendMessage(RMText.getLabel("action.settings"));
							}
							return true;
						}
						//MODE
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.mode"))){
							if(!rmp.hasPermission("resourcemadness.mode")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length==2){
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.mode.filter"))){
									if(!rmp.hasPermission("resourcemadness.mode.filter")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.FILTER, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.FILTER);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(RMText.getLabel("action.mode.filter"));
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.mode.reward"))){
									if(!rmp.hasPermission("resourcemadness.mode.reward")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.REWARD, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.REWARD);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(RMText.getLabel("action.mode.reward"));
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.mode.tools"))){
									if(!rmp.hasPermission("resourcemadness.mode.tools")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.TOOLS, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.TOOLS);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(RMText.getLabel("action.mode.tools"));
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.cycleMode(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.MODE_CYCLE);
									rmp.sendMessage(RMText.getLabel("action.mode.cycle"));
								}
								return true;
							}
						}
						//SAVE
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.save"))){
							if(!rmp.hasOpPermission("resourcemadness.admin.save")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							//if(RMGame.getGames().size()!=0){
								rmp.sendMessage(RMText.getLabel("save.saving"));
								log.log(Level.INFO, RMText.preLog+"Saving...");
								switch(saveAll()){
								case SUCCESS:
									rmp.sendMessage(RMText.getLabel("save.success"));
									log.log(Level.INFO, RMText.preLog+"Data was saved successfully.");
									break;
								case FAIL:
									rmp.sendMessage(RMText.getLabel("save.fail"));
									log.log(Level.INFO, RMText.preLog+"Data was not saved properly!");
									break;
								case NO_DATA:
									rmp.sendMessage(RMText.getLabel("save.no_data"));
									log.log(Level.INFO, RMText.preLog+"No data to save.");
									break;
								}
							//}
							//else rmp.sendMessage(RMText.getLabel("save.no_data"));
							return true;
						}
						//KICK
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.kick"))){
							if(!rmp.hasPermission("resourcemadness.kick")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.kick.team"))){
									if(!rmp.hasPermission("resourcemadness.kick.team")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(args.length>2){
										List<String> list = findTeamColorsFromArgs(args, 2);
										if(list.size()!=0){
											if(rmGame!=null) rmGame.kickTeam(rmp, true, list);
											else{
												rmp.setRequestStringList(list);
												rmp.setPlayerAction(PlayerAction.KICK_TEAM);
												rmp.sendMessage(RMText.getLabelArgs("action.kick.team", RMTextHelper.getStringByStringList(list, ", ")));
											}
											return true;
										}
									}
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.kick.all"))){
									if(!rmp.hasPermission("resourcemadness.kick.all")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(rmGame!=null) rmGame.kickAll(rmp, true);
									else{
										rmp.setPlayerAction(PlayerAction.KICK_ALL);
										rmp.sendMessage(RMText.getLabel("action.kick.all"));
									}
									return true;
								}
								else{
									if(!rmp.hasPermission("resourcemadness.kick.player")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									List<String> list = findPlayerNamesFromArgs(args, 2);
									if(rmGame!=null) rmGame.kickPlayer(rmp, true, list);
									else{
										rmp.setRequestStringList(list);
										rmp.setPlayerAction(PlayerAction.KICK_PLAYER);
										rmp.sendMessage(RMText.getLabelArgs("action.kick.player", RMTextHelper.getStringByStringList(list, ", ")));
									}
									return true;
								}
							}
							rmKickInfo(rmp);
							return true;
						}
						//BAN
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.ban"))){
							if(!rmp.hasPermission("resourcemadness.ban")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.ban.list"))){
									if(!rmp.hasPermission("resourcemadness.ban.list")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(args.length==3){
										int value = RMHelper.getIntByString(args[2]);
										if(rmGame!=null) rmGame.sendBanList(rmp, value);
										else{
											rmp.setRequestInt(value);
											rmp.setPlayerAction(PlayerAction.BAN_LIST);
											rmp.sendMessage(RMText.getLabel("action.ban.list"));
										}
									}
									else{
										if(rmGame!=null) rmGame.sendBanList(rmp, 0);
										else{
											rmp.setRequestInt(0);
											rmp.setPlayerAction(PlayerAction.BAN_LIST);
											rmp.sendMessage(RMText.getLabel("action.ban.list"));
										}
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.ban.team"))){
									if(!rmp.hasPermission("resourcemadness.ban.team")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(args.length>2){
										List<String> list = findTeamColorsFromArgs(args, 2);
										if(list.size()!=0){
											if(rmGame!=null) rmGame.banTeam(rmp, true, list);
											else{
												rmp.setRequestStringList(list);
												rmp.setPlayerAction(PlayerAction.BAN_TEAM);
												rmp.sendMessage(RMText.getLabelArgs("action.ban.team", RMTextHelper.getStringByStringList(list, ", ")));
											}
											return true;
										}
									}
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.ban.all"))){
									if(!rmp.hasPermission("resourcemadness.ban.all")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(rmGame!=null) rmGame.banAll(rmp, true);
									else{
										rmp.setPlayerAction(PlayerAction.BAN_ALL);
										rmp.sendMessage(RMText.getLabel("action.ban.all"));
									}
									return true;
								}
								else{
									if(!rmp.hasPermission("resourcemadness.ban.player")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									List<String> list = findPlayerNamesFromArgs(args, 2);
									if(rmGame!=null) rmGame.banPlayer(rmp, true, list);
									else{
										rmp.setRequestStringList(list);
										rmp.setPlayerAction(PlayerAction.BAN_PLAYER);
										rmp.sendMessage(RMText.getLabelArgs("action.ban.player", RMTextHelper.getStringByStringList(list, ", ")));
									}
									return true;
								}
							}
							rmBanInfo(rmp);
							return true;
						}
						//UNBAN
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.unban"))){
							if(!rmp.hasPermission("resourcemadness.unban")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(!rmp.hasPermission("resourcemadness.unban.player")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
								List<String> list = findPlayerNamesFromArgs(args, 2);
								if(rmGame!=null) rmGame.unbanPlayer(rmp, true, list);
								else{
									rmp.setRequestStringList(list);
									rmp.setPlayerAction(PlayerAction.UNBAN_PLAYER);
									rmp.sendMessage(RMText.getLabelArgs("action.unban.player", RMTextHelper.getStringByStringList(list, ", ")));
								}
								return true;
							}
							rmUnbanInfo(rmp);
							return true;
						}
						//JOIN
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.join"))){
							if(!rmp.hasPermission("resourcemadness.join")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length==2){
								if(rmGame!=null){								
									RMTeam rmTeam = RMGame.getTeamById(args[1], rmGame);
									if(rmTeam!=null){
										//rmp.allowMarkLocation(true);
										rmGame.joinTeam(rmTeam, rmp);
										return true;
									}
									rmTeam = rmGame.getTeamByDye(args[1]);
									if(rmTeam!=null){
										//rmp.allowMarkLocation(true);
										rmGame.joinTeam(rmTeam, rmp);
										return true;
									}
									rmp.sendMessage(RMText.getLabel("msg.team_does_not_exist"));
									return true;
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.JOIN);
								rmp.sendMessage(RMText.getLabel("action.join"));
								return true;
							}
						}
						//QUIT
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.quit"))){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							RMGame game = rmp.getGame();
							if(game!=null) rmp.getTeam().removePlayer(rmp);
							else rmp.sendMessage(RMText.getLabel("msg.did_not_join_any_team_yet"));
							return true;
							
						}
						//READY
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.ready"))){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							RMGame game = rmp.getGame();
							if(game!=null){
								if(game.getGameConfig().getState()==GameState.SETUP){
									game.toggleReady(rmp);
								}
								else rmp.sendMessage(RMText.getLabel("ready.cannot_while_ingame"));
							}
							else rmp.sendMessage(RMText.getLabel("msg.did_not_join_any_team_yet"));
							return true;
							
						}
						//RETURN
						/*
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.return"))){
							if(!rmp.hasPermission("resourcemadness.return")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
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
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.start"))){
							if(!rmp.hasPermission("resourcemadness.start")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length==2){
								int amount = RMHelper.getIntByString(args[1]);
								if(amount!=-1){
									if(!rmp.hasPermission("resourcemadness.start.random")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										rmGame.setRandomizeAmount(rmp, amount);
										rmGame.startGame(rmp);
									}
									else{
										rmp.setRequestInt(amount);
										rmp.setPlayerAction(PlayerAction.START_RANDOM);
										rmp.sendMessage(RMText.getLabelArgs("action.start.random", ""+amount));
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.startGame(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.START);
									rmp.sendMessage(RMText.getLabel("action.start"));
								}
								return true;
							}
						}
						/*
						//RESTART
						else if(args[0].equalsIgnoreCase("restart")){
							if(!rmp.hasPermission("resourcemadness.restart")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.restartGame(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTART);
								rmp.sendMessage(RMText.a_Restart);
							}
							return true;
						}
						*/
						//STOP
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.stop"))){
							if(!rmp.hasPermission("resourcemadness.stop")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.stopGame(rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.STOP);
								rmp.sendMessage(RMText.getLabel("action.stop"));
							}
							return true;
						}
						//PAUSE
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.pause"))){
							if(!rmp.hasPermission("resourcemadness.pause")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.pauseGame(rmp);
							else{
								rmp.setRequestBool(true);
								rmp.setPlayerAction(PlayerAction.PAUSE);
								rmp.sendMessage(RMText.getLabel("action.pause"));
							}
							return true;
						}
						//RESUME
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.resume"))){
							if(!rmp.hasPermission("resourcemadness.pause")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.resumeGame(rmp);
							else{
								rmp.setRequestBool(false);
								rmp.setPlayerAction(PlayerAction.RESUME);
								rmp.sendMessage(RMText.getLabel("action.resume"));
							}
							return true;
						}
						//RESTORE WORLD
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.restore"))){
							if(!rmp.hasPermission("resourcemadness.restore")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.restoreWorld(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTORE);
								rmp.sendMessage(RMText.getLabel("action.restore"));
							}
							return true;
						}
						//ITEMS
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.items"))){
							if(!rmp.hasPermission("resourcemadness.items")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(rmp.isIngame()){
								rmp.getGameInProgress().updateGameplayInfo(rmp, rmp.getTeam());
								return true;
							}
							rmp.sendMessage(RMText.getLabel("msg.must_be_ingame_command"));
							return false;
						}
						//TIME
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.time"))){
							if(!rmp.hasPermission("resourcemadness.time")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(rmp.isIngame()){
								RMGameTimer timer = rmp.getGame().getGameConfig().getTimer();
								if(timer.getTimeLimit()!=0){
									if(timer.getTimeRemaining()!=0){
										rmp.sendMessage(RMText.getLabelArgs("time.remaining", timer.getTextTimeRemaining()));
									}
								}
								else rmp.sendMessage(RMText.getLabel("time.no_time_limit"));
								return true;
							}
							else rmp.sendMessage(RMText.getLabel("msg.must_be_ingame_command"));
							return false;
						}
						//FILTER
						FilterState filterState = null;
						if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.filter"))){
							if(!rmp.hasPermission("resourcemadness.filter")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if((args.length>1)&&(args[1].equalsIgnoreCase(RMText.getLabel("cmd.filter.info")))){
								if(!rmp.hasPermission("resourcemadness.filter.info")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
								if(rmGame!=null){
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.getLabel("cmd.filter.info.string")))){
										rmGame.sendFilterInfoString(rmp);
									}
									else rmGame.sendFilterInfo(rmp);
								}
								else{
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.getLabel("cmd.filter.info.string")))){
										rmp.setPlayerAction(PlayerAction.FILTER_INFO_STRING);
										rmp.sendMessage(RMText.getLabel("action.filter.info.string"));
									}
									else{
										rmp.setPlayerAction(PlayerAction.FILTER_INFO);
										rmp.sendMessage(RMText.getLabel("action.filter.info"));
									}
								}
								return true;
							}
							filterState = FilterState.FILTER;
						}
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.reward"))){
							if(!rmp.hasPermission("resourcemadness.reward")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if((args.length>1)&&(args[1].equalsIgnoreCase(RMText.getLabel("cmd.filter.info")))){
								if(!rmp.hasPermission("resourcemadness.reward.info")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
								if(rmGame!=null){
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.getLabel("cmd.filter.info.string")))){
										rmGame.sendRewardInfoString(rmp);
									}
									else rmGame.sendRewardInfo(rmp);
								}
								else{
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.getLabel("cmd.filter.info.string")))){
										rmp.setPlayerAction(PlayerAction.REWARD_INFO_STRING);
										rmp.sendMessage(RMText.getLabel("action.reward.info.string"));
									}
									else{
										rmp.setPlayerAction(PlayerAction.REWARD_INFO);
										rmp.sendMessage(RMText.getLabel("action.reward.info"));
									}
								}
								return true;
							}
							filterState = FilterState.REWARD;
						}
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.tools"))){
							if(!rmp.hasPermission("resourcemadness.tools")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if((args.length>1)&&(args[1].equalsIgnoreCase(RMText.getLabel("cmd.filter.info")))){
								if(!rmp.hasPermission("resourcemadness.tools.info")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
								if(rmGame!=null){
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.getLabel("cmd.filter.info.string")))){
										rmGame.sendToolsInfoString(rmp);
									}
									else rmGame.sendToolsInfo(rmp);
								}
								else{
									if((args.length==3)&&(args[2].equalsIgnoreCase(RMText.getLabel("cmd.filter.info.string")))){
										rmp.setPlayerAction(PlayerAction.TOOLS_INFO_STRING);
										rmp.sendMessage(RMText.getLabel("action.tools.info.string"));
									}
									else{
										rmp.setPlayerAction(PlayerAction.TOOLS_INFO);
										rmp.sendMessage(RMText.getLabel("action.tools.info"));
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
												rmp.sendMessage(RMText.getLabel("action.filter"));
												break;
											case REWARD:
												rmp.setPlayerAction(PlayerAction.REWARD);
												rmp.sendMessage(RMText.getLabel("action.reward"));
												break;
											case TOOLS:
												rmp.setPlayerAction(PlayerAction.TOOLS);
												rmp.sendMessage(RMText.getLabel("action.tools"));
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
						//MONEY
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.money"))){
							if(!rmp.hasPermission("resourcemadness.money")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(!economy.hasAccount(rmp.getName())) return rmp.sendMessage(RMText.getLabel("money.no_account_yet"));
							if(args.length>1){
								int beginIndex = 1;
								FilterType filterType = null;
								if(args.length>2){
									if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.filter.add"))){
										if(!rmp.hasPermission("resourcemadness.money.add")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
										filterType = FilterType.ADD;
										beginIndex++;
									}
									else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.filter.subtract"))){
										if(!rmp.hasPermission("resourcemadness.money.add")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
										filterType = FilterType.SUBTRACT;
										beginIndex++;
									}
									else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.filter.clear"))){
										if(!rmp.hasPermission("resourcemadness.money.add")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
										filterType = FilterType.CLEAR;
										beginIndex++;
									}
								}
								else{
									if(!rmp.hasPermission("resourcemadness.money.set")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									filterType = FilterType.SET;
								}
								if(filterType!=null){
									Double d = RMHelper.getDoubleByString(args[beginIndex]);
									if(d!=null){
										if(rmGame!=null){
											rmGame.parseMoney(rmp, new RMRequestMoney(filterType, d));
										}
										else{
											switch(filterType){
											case SET: rmp.sendMessage(RMText.getLabel("action.money.set")); break;
											case ADD: rmp.sendMessage(RMText.getLabel("action.money.add")); break;
											case SUBTRACT: rmp.sendMessage(RMText.getLabel("action.money.subtract")); break;
											case CLEAR: rmp.sendMessage(RMText.getLabel("action.money.clear")); break;
											}
											rmp.setRequestMoney(new RMRequestMoney(filterType, d));
											rmp.setPlayerAction(PlayerAction.MONEY);
										}
										return true;
									}
								}
							}
							//rmMoneyInfo(rmp);
						}
						//TEMPLATE
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.template"))){
							if(!rmp.hasPermission("resourcemadness.template")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.template.list"))){
									if(!rmp.hasPermission("resourcemadness.template.list")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(args.length==2) sendTemplateListById(args[1], rmp);
									else sendTemplateListById("0", rmp);
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.template.load"))){
									if(!rmp.hasPermission("resourcemadness.template.load")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(args.length==3){
										RMTemplate template = rmp.loadTemplate(args[2].toLowerCase());
										if(template!=null){
											if(rmGame!=null) rmGame.loadTemplate(template, rmp);
											else{
												rmp.setRequestString(args[2]);
												rmp.setPlayerAction(PlayerAction.TEMPLATE_LOAD);
												rmp.sendMessage(RMText.getLabelArgs("action.template.load", args[2]));
											}
										}
										return true;
									}
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.template.save"))){
									if(!rmp.hasPermission("resourcemadness.template.save")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(args.length==3){
										if(rmGame!=null) rmGame.saveTemplate(args[2].toLowerCase(), rmp);
										else{
											rmp.setRequestString(args[2].toLowerCase());
											rmp.setPlayerAction(PlayerAction.TEMPLATE_SAVE);
											rmp.sendMessage(RMText.getLabelArgs("action.template.save", args[2]));
										}
										return true;
									}
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.template.remove"))){
									if(!rmp.hasPermission("resourcemadness.template.remove")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
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
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.claim"))){
							if(!rmp.hasPermission("resourcemadness.claim")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.claim.found"))){
									if(!rmp.hasPermission("resourcemadness.claim.found")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(RMText.getLabel("cmd.claim.x.chest"))){
												if(!rmp.hasPermission("resourcemadness.claim.found.chest")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												if(rmGame!=null){
													rmp.setRequestInt(rmGame.getGameConfig().getId());
													rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST);
													rmp.sendMessage(RMText.getLabel("action.claim.found.chest"));
												}
												else{
													rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST_SELECT);
													rmp.sendMessage(RMText.getLabel("action.claim.found.chest.select"));
												}
												return true;
											}
										}
										if(rmGame!=null) rmGame.claimFound(rmp, requestClaimItemsAtArgsPos(rmp, args, 2));
										else{
											rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 2));
											rmp.setPlayerAction(PlayerAction.CLAIM_FOUND);
											rmp.sendMessage(RMText.getLabel("action.claim.found"));
										}
										return true;
									}
									else rmp.sendMessage(RMText.getLabel("msg.claim.found.cannot_while_ingame"));
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.claim.items"))){
									if(!rmp.hasPermission("resourcemadness.claim.items")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(RMText.getLabel("cmd.claim.x.chest"))){
												if(!rmp.hasPermission("resourcemadness.claim.items.chest")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												rmp.setPlayerAction(PlayerAction.CLAIM_ITEMS_CHEST);
												rmp.sendMessage(RMText.getLabel("action.claim.items.chest"));
												return true;
											}
										}
										rmp.claimItems(requestClaimItemsAtArgsPos(rmp, args, 2));
										return true;
									}
									else rmp.sendMessage(RMText.getLabel("msg.claim.items.cannot_while_ingame"));
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.claim.reward"))){
									if(!rmp.hasPermission("resourcemadness.claim.reward")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(RMText.getLabel("cmd.claim.x.chest"))){
												if(!rmp.hasPermission("resourcemadness.claim.reward.chest")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												rmp.setPlayerAction(PlayerAction.CLAIM_REWARD_CHEST);
												rmp.sendMessage(RMText.getLabel("action.claim.reward.chest"));;
												return true;
											}
										}
										rmp.claimReward(requestClaimItemsAtArgsPos(rmp, args, 2));
										return true;
									}
									else rmp.sendMessage(RMText.getLabel("msg.claim.reward.cannot_while_ingame"));
									return true;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.claim.tools"))){
									if(!rmp.hasPermission("resourcemadness.claim.tools")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									if(args.length>2){
										if(args[2].equalsIgnoreCase(RMText.getLabel("cmd.claim.x.chest"))){
											if(!rmp.hasPermission("resourcemadness.claim.tools.chest")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
											rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
											rmp.setPlayerAction(PlayerAction.CLAIM_TOOLS_CHEST);
											rmp.sendMessage(RMText.getLabel("action.claim.tools.chest"));
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
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.set"))){
							if(!rmp.hasPermission("resourcemadness.set")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							page = 0;
							if(args.length>1){
								PlayerAction action = null;
								//MIN PLAYERS
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.minplayers"))){
									if(!rmp.hasPermission("resourcemadness.set.minplayers")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_MIN_PLAYERS;
								}
								//MAX PLAYERS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.maxplayers"))){
									if(!rmp.hasPermission("resourcemadness.set.maxplayers")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_MAX_PLAYERS;
								}
								//MIN TEAM PLAYERS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.minteamplayers"))){
									if(!rmp.hasPermission("resourcemadness.set.minteamplayers")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_MIN_TEAM_PLAYERS;
								}
								//MAX TEAM PLAYERS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.maxteamplayers"))){
									if(!rmp.hasPermission("resourcemadness.set.maxteamplayers")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_MAX_TEAM_PLAYERS;
								}
								/*
								//MAX ITEMS
								else if(args[1].equalsIgnoreCase("maxitems")){
									if(!rmp.hasPermission("resourcemadness.set.maxitems")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_MAX_ITEMS;
								}
								*/
								//SAFE ZONE RADIUS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.safezone"))){
									if(!rmp.hasPermission("resourcemadness.set.safezone")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_SAFE_ZONE;
								}
								//MATCH TIME LIMIT
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.timelimit"))){
									if(!rmp.hasPermission("resourcemadness.set.timelimit")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_TIME_LIMIT;
								}
								//AUTO RANDOM ITEMS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.random"))){
									if(!rmp.hasPermission("resourcemadness.set.random")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_RANDOM;
								}
								else page = RMHelper.getIntByString(args[1]);
								
								if(action!=null){
									if(args.length==3){
										int amount = RMHelper.getIntByString(args[2]);
										if(amount>-1){
											if(rmGame!=null){
												switch(action){
													case SET_MIN_PLAYERS: rmGame.setSetting(rmp, Setting.minplayers, amount); break;
													case SET_MAX_PLAYERS: rmGame.setSetting(rmp, Setting.maxplayers, amount); break;
													case SET_MIN_TEAM_PLAYERS: rmGame.setSetting(rmp, Setting.minteamplayers, amount); break;
													case SET_MAX_TEAM_PLAYERS: rmGame.setSetting(rmp, Setting.maxplayers, amount); break;
													case SET_SAFE_ZONE: rmGame.setSetting(rmp, Setting.safezone, amount); break;
													case SET_TIME_LIMIT: rmGame.setSetting(rmp, Setting.timelimit, amount); break;
													case SET_RANDOM: rmGame.setSetting(rmp, Setting.random, amount); break;
												}
												return true;
											}
											else{
												rmp.setRequestInt(RMHelper.getIntByString(args[2]));
												rmp.setPlayerAction(action);
												switch(action){
													case SET_MIN_PLAYERS: rmp.sendMessage(RMText.getLabel("action.set.minplayers")); break;
													case SET_MAX_PLAYERS: rmp.sendMessage(RMText.getLabel("action.set.maxplayers")); break;
													case SET_MIN_TEAM_PLAYERS: rmp.sendMessage(RMText.getLabel("action.set.minteamplayers")); break;
													case SET_MAX_TEAM_PLAYERS: rmp.sendMessage(RMText.getLabel("action.set.maxteamplayers")); break;
													case SET_SAFE_ZONE: rmp.sendMessage(RMText.getLabel("action.set.safezone")); break;
													case SET_TIME_LIMIT: rmp.sendMessage(RMText.getLabel("action.set.timelimit")); break;
													case SET_RANDOM: rmp.sendMessage(RMText.getLabel("action.set.random")); break;
												}
												return true;
											}
										}
									}
								}
								
								//SET & TOGGLE
								action = null;
								//ADVERTISE GAME IN SEARCH
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.advertise"))){
									if(!rmp.hasPermission("resourcemadness.set.advertise")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_ADVERTISE;
								}
								//AUTO RESTORE WORLD
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.restore"))){
									if(!rmp.hasPermission("resourcemadness.set.restore")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_RESTORE;
								}
								//GATHER PLAYERS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.warp"))){
									if(!rmp.hasPermission("resourcemadness.set.warp")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_WARP;
								}
								//ALLOW MIDGAME JOIN
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.midgamejoin"))){
									if(!rmp.hasPermission("resourcemadness.set.midgamejoin")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_MIDGAME_JOIN;
								}
								//HEAL PLAYER
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.healplayer"))){
									if(!rmp.hasPermission("resourcemadness.set.healplayer")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_HEAL_PLAYER;
								}
								//CLEAR PLAYER INVENTORY
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.clearinventory"))){
									if(!rmp.hasPermission("resourcemadness.set.clearinventory")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_CLEAR_INVENTORY;
								}
								//USE A GAME'S FOUND ITEMS AS REWARD
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.foundasreward"))){
									if(!rmp.hasPermission("resourcemadness.set.foundasreward")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_FOUND_AS_REWARD;
								}
								//WARN UNEQUAL ITEMS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.warnunequal"))){
									if(!rmp.hasPermission("resourcemadness.set.warnunequal")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_WARN_UNEQUAL;
								}
								//ALLOW UNEQUAL ITEMS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.allowunequal"))){
									if(!rmp.hasPermission("resourcemadness.set.allowunequal")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_ALLOW_UNEQUAL;
								}
								//WARN HACK ITEMS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.warnhacked"))){
									if(!rmp.hasPermission("resourcemadness.set.warnhacked")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_WARN_HACKED;
								}
								//ALLOW HACK ITEMS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.allowhacked"))){
									if(!rmp.hasPermission("resourcemadness.set.allowhacked")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_ALLOW_HACKED;
								}
								//INFINITE REWARD ITEMS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.infinitereward"))){
									if(!rmp.hasPermission("resourcemadness.set.infinitereward")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_INFINITE_REWARD;
								}
								//INFINITE TOOLS ITEMS
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.infinitetools"))){
									if(!rmp.hasPermission("resourcemadness.set.infinitetools")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
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
											case SET_RESTORE: rmGame.setSetting(rmp, Setting.restore, i); break;
											case SET_WARP: rmGame.setSetting(rmp, Setting.warp, i); break;
											case SET_MIDGAME_JOIN: rmGame.setSetting(rmp, Setting.midgamejoin, i); break;
											case SET_HEAL_PLAYER: rmGame.setSetting(rmp, Setting.healplayer, i); break;
											case SET_CLEAR_INVENTORY: rmGame.setSetting(rmp, Setting.clearinventory, i); break;
											case SET_FOUND_AS_REWARD: rmGame.setSetting(rmp, Setting.foundasreward, i); break;
											case SET_WARN_UNEQUAL: rmGame.setSetting(rmp, Setting.warnunequal, i); break;
											case SET_ALLOW_UNEQUAL: rmGame.setSetting(rmp, Setting.allowunequal, i); break;
											case SET_WARN_HACKED: rmGame.setSetting(rmp, Setting.warnhacked, i); break;
											case SET_ALLOW_HACKED: rmGame.setSetting(rmp, Setting.allowhacked, i); break;
											case SET_INFINITE_REWARD: rmGame.setSetting(rmp, Setting.infinitereward, i); break;
											case SET_INFINITE_TOOLS: rmGame.setSetting(rmp, Setting.infinitetools, i); break;
										}
									}
									else{
										rmp.setRequestInt(i);
										rmp.setPlayerAction(action);
										switch(action){
											case SET_ADVERTISE:	rmp.sendMessage(RMText.getLabel("action.set.advertise")); break;
											case SET_RESTORE: rmp.sendMessage(RMText.getLabel("action.set.restore")); break;
											case SET_WARP: rmp.sendMessage(RMText.getLabel("action.set.warp")); break;
											case SET_MIDGAME_JOIN: rmp.sendMessage(RMText.getLabel("action.set.midgamejoin")); break;
											case SET_HEAL_PLAYER: rmp.sendMessage(RMText.getLabel("action.set.healplayer")); break;
											case SET_CLEAR_INVENTORY: rmp.sendMessage(RMText.getLabel("action.set.clearinventory")); break;
											case SET_FOUND_AS_REWARD: rmp.sendMessage(RMText.getLabel("action.set.foundasreward")); break;
											case SET_WARN_UNEQUAL: rmp.sendMessage(RMText.getLabel("action.set.warnunequal")); break;
											case SET_ALLOW_UNEQUAL: rmp.sendMessage(RMText.getLabel("action.set.allowunequal")); break;
											case SET_WARN_HACKED: rmp.sendMessage(RMText.getLabel("action.set.warnhacked")); break;
											case SET_ALLOW_HACKED: rmp.sendMessage(RMText.getLabel("action.set.allowhacked")); break;
											case SET_INFINITE_REWARD: rmp.sendMessage(RMText.getLabel("action.set.infinitereward")); break;
											case SET_INFINITE_TOOLS: rmp.sendMessage(RMText.getLabel("action.set.infinitetools")); break;
										}
									}
									return true;
								}
								
								//SET STRING
								action = null;
								//PASSWORD
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.set.password"))){
									if(!rmp.hasPermission("resourcemadness.set.password")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									action = PlayerAction.SET_PASSWORD;
								}
								if(action!=null){
									String arg = null;
									if(args.length==3) arg = args[2];
									else if(args.length==2) arg = "";
									if(arg!=null){
										if(rmGame!=null){
											switch(action){
												case SET_PASSWORD: rmGame.setSetting(rmp, Setting.password, arg); break;
											}
										}
										else{
											rmp.setRequestString(arg);
											rmp.setPlayerAction(action);
											switch(action){
												case SET_PASSWORD: rmp.sendMessage(RMText.getLabel("action.set.password")); break;
											}
										}
										return true;
									}
								}
							}
							rmSetInfo(rmp, page);
							return true;
						}
						//CHAT
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.chat"))){
							if(!rmp.hasPermission("resourcemadness.chat")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							if(args.length>1){
								ChatMode chatMode = null;
								if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.chat.world"))){
									if(!rmp.hasPermission("resourcemadness.chat.world")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									chatMode = ChatMode.WORLD;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.chat.game"))){
									if(!rmp.hasPermission("resourcemadness.chat.game")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
									chatMode = ChatMode.GAME;
								}
								else if(args[1].equalsIgnoreCase(RMText.getLabel("cmd.chat.team"))){
									if(!rmp.hasPermission("resourcemadness.chat.team")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
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
										case WORLD: rmp.sendMessage(RMText.getLabel("chat.world.must_be_ingame")); break;
										case GAME: rmp.sendMessage(RMText.getLabel("chat.game.must_be_ingame")); break;
										case TEAM: rmp.sendMessage(RMText.getLabel("chat.team.must_be_ingame")); break;
										}
									}
									return true;
								}
							}
							rmChatInfo(rmp);
							return true;
						}
						//ITEM - Get Item NAME by ID or Item ID by NAME
						else if(args[0].equalsIgnoreCase(RMText.getLabel("cmd.item"))){
							List<String> listArgs = new ArrayList<String>();
							for(int i=1; i<args.length; i++){
								listArgs.add(args[i]);
							}
							if(listArgs.size()>0){
								if(!rmp.hasPermission("resourcemadness.item")) return rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
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
												if(id!=-1){
													Material mat = Material.getMaterial(id);
													if(mat!=null) if(!items.containsKey(id)) items.put(id, mat);
													else if(!itemsWarn.contains(strItem)) itemsWarn.add(""+id);
												}
											}
										}
									}
								}
								if(items.size()>0){
									rmp.sendMessage(RMText.getLabelArgs("items.found_match", ""+items.size()));
									rmp.sendMessage(RMTextHelper.getFormattedItemStringByHashMap(items));
									return true;
								}
								else if(itemsWarn.size()>0){
									rmp.sendMessage(RMText.getLabel("items.found_none"));
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
	
	public List<String> findTeamColorsFromArgs(String[] args, int beginIndex){
		List<String> list = RMTextHelper.separateStringToList(RMTextHelper.getTextFromArgs(Arrays.copyOfRange(args, 2, args.length)), " ", ",");
		Iterator<String> iter = list.iterator();
		while(iter.hasNext()){
			DyeColor color = RMHelper.getDyeByString(iter.next());
			if(color==null) iter.remove();
		}
		return list;
	}
	
	public List<String> findPlayerNamesFromArgs(String[] args, int beginIndex){
		List<String> list = RMTextHelper.separateStringToList(RMTextHelper.getTextFromArgs(Arrays.copyOfRange(args, 1, args.length)), " ", ",");
		Iterator<String> iter = list.iterator();
		while(iter.hasNext()){
			String name = iter.next();
			if((name==null)||(name.length()==0)) iter.remove();
		}
		return list;
	}
	
	public void saveAllBackup(){
		log.log(Level.INFO, RMText.preLog+"Autosaving...");
		//if(RMGame.getGames().size()==0) return;
		saveConfig();
		File folder = new File(getDataFolder()+File.separator+"backup");
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Creating backup directory...");
			folder.mkdir();
		}
		//File file = new File(folder.getAbsolutePath()+File.separator+"config.txt");
		//if(!file.exists()) save(DataType.CONFIG, false, file, false);
		save(DataType.STATS, false, new File(folder.getAbsolutePath()+File.separator+"stats.txt"), true);
		//save(DataType.PLAYER, false, new File(folder.getAbsolutePath()+File.separator+"playerdata.txt"));
		saveYaml(DataType.PLAYER, new File(folder.getAbsolutePath()+File.separator+"playerdata.yml"), true);
		//save(DataType.GAME, false, new File(folder.getAbsolutePath()+File.separator+"gamedata.txt"));
		saveYaml(DataType.GAME, new File(folder.getAbsolutePath()+File.separator+"gamedata.yml"), true);
		save(DataType.LOG, true, new File(folder.getAbsolutePath()+File.separator+"gamelogdata.txt"), true);
	}
	
	public void createFolders(){
		File folder = getDataFolder();
		File langFolder = new File(folder.getAbsolutePath()+File.separator+"lang");
		if(!folder.exists()){
			log.log(Level.INFO, RMText.preLog+"Config folder not found! Creating one...");
			folder.mkdir();
		}
		if(!langFolder.exists()){
			log.log(Level.INFO, RMText.preLog+"Languages folder not found! Creating one...");
			langFolder.mkdir();
		}
	}
	
	public void saveConfig(){
		createFolders();
		File folder = getDataFolder();
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
		createFolders();
		File folder = getDataFolder();
		List<Boolean> decision = new ArrayList<Boolean>();
		//if(RMGame.getGames().size()==0) return DataSave.NO_DATA;
		File file = new File(folder.getAbsolutePath()+File.separator+"aliases.yml");
		if(file.exists()) saveYaml(DataType.ALIASES, file, false);
		saveConfig();
		decision.add(save(DataType.STATS, false, new File(folder.getAbsolutePath()+File.separator+"stats.txt"), true));
		//decision.add(save(DataType.PLAYER, false, new File(folder.getAbsolutePath()+File.separator+"playerdata.txt")));
		decision.add(saveYaml(DataType.PLAYER, new File(folder.getAbsolutePath()+File.separator+"playerdata.yml"), true));
		//decision.add(save(DataType.GAME, false, new File(folder.getAbsolutePath()+File.separator+"gamedata.txt")));
		decision.add(saveYaml(DataType.GAME, new File(folder.getAbsolutePath()+File.separator+"gamedata.yml"), true));
		decision.add(save(DataType.LOG, true, new File(folder.getAbsolutePath()+File.separator+"gamelogdata.txt"), true));
		decision.add(save(DataType.LOG, false, new File(folder.getAbsolutePath()+File.separator+"gamelogdataun.txt"), true));
		if(decision.contains(false)) return DataSave.FAIL;
		return DataSave.SUCCESS;
	}
	
	public boolean saveYaml(DataType dataType, File file, boolean saveBackup){
		if(file==null){
			log.log(Level.WARNING, RMText.preLog+"Cannot load data. Data type unknown!");
			return false;
		}
		if(!file.exists()){
			switch(dataType){
				case CONFIG: log.log(Level.INFO, RMText.preLog+"Config file not found! Creating one..."); break;
				case ALIASES: log.log(Level.INFO, RMText.preLog+"Aliases file not found! Creating one..."); break;
				case STATS: log.log(Level.INFO, RMText.preLog+"Stats file not found! Creating one..."); break;
				case PLAYER: log.log(Level.INFO, RMText.preLog+"Player data file not found! Creating one..."); break;
				case GAME: log.log(Level.INFO, RMText.preLog+"Game data file not found! Creating one..."); break;
				case LOG: log.log(Level.INFO, RMText.preLog+"Game data file not found! Creating one..."); break;
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
					yml.options().header("# ResourceMadness v"+pdfFile.getVersion()+" Aliases file\n\n"+RMText.getLabel("config.aliases"));
					RMCommands commands = config.getCommands();
					for(RMCommand cmd : RMCommand.values()){
						yml.load();
						String aliases = commands.getAliasMap().get(cmd).toString();
						aliases = aliases.replace("[", "");
						aliases = aliases.replace("]", "");
						String root = cmd.name().toLowerCase().replace("_", " ");
						if((yml.getString(root) == null)||(yml.getString(root).length()==0)) setProperty(yml, root, aliases);
						yml.save();
					}
					break;
				case PLAYER:
					List<String> keys = yml.getKeys();
					for(String key : keys){
						yml.removeProperty(key);
					}
					yml.options().header("# ResourceMadness v"+pdfFile.getVersion()+" Player data file\n");
					for(RMPlayer rmp : RMPlayer.getPlayers().values()){
						String root = rmp.getName()+".";
						setProperty(yml, root+"ready", rmp.getReady());
						setProperty(yml, root+"chatmode", rmp.getChatMode().ordinal());
						//Stats
						root = rmp.getName()+".stats.";
						RMStats stats = rmp.getStats();
						setProperty(yml, root+"wins", stats.get(RMStat.WINS));
						setProperty(yml, root+"losses", stats.get(RMStat.LOSSES));
						setProperty(yml, root+"timesplayed", stats.get(RMStat.TIMES_PLAYED));
						setProperty(yml, root+"itemsfoundtotal", stats.get(RMStat.ITEMS_FOUND_TOTAL));
						setProperty(yml, root+"kicked", stats.get(RMStat.KICKED));
						setProperty(yml, root+"banned", stats.get(RMStat.BANNED));
						setProperty(yml, root+"tempbanned", stats.get(RMStat.TEMP_BANNED));
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
					yml.options().header("# ResourceMadness v"+pdfFile.getVersion()+" Game data file\n");
					HashMap<Integer, RMGame> games = RMGame.getGames();
					for(Integer id : games.keySet()){
						RMGameConfig config = games.get(id).getGameConfig();
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
						
						for(Setting setting : Setting.values()){
							SettingPrototype s = config.getSettingLibrary().get(setting);
							if(s instanceof SettingInt) setProperty(yml, root+s.name(), ((SettingInt) s).get());
							else if(s instanceof SettingBool) setProperty(yml, root+s.name(), ((SettingBool) s).get());
							else if(s instanceof SettingStr) setProperty(yml, root+s.name(), ((SettingStr) s).get());
						}
						//Stats
						root = id+".stats.";
						RMStats stats = config.getStats();
						setProperty(yml, root+"wins", stats.get(RMStat.WINS));
						setProperty(yml, root+"losses", stats.get(RMStat.LOSSES));
						setProperty(yml, root+"timesplayed", stats.get(RMStat.TIMES_PLAYED));
						setProperty(yml, root+"itemsfoundtotal", stats.get(RMStat.ITEMS_FOUND_TOTAL));
						setProperty(yml, root+"kicked", stats.get(RMStat.KICKED));
						setProperty(yml, root+"banned", stats.get(RMStat.BANNED));
						setProperty(yml, root+"tempbanned", stats.get(RMStat.TEMP_BANNED));
						
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
						
						//BanList
						root = id+".banned.";
						RMBanList banList = config.getBanList();
						for(Map.Entry<String, RMBanTicket> map : banList.entrySet()){
							setProperty(yml, root+map.getKey(), "");
							if(map.getValue().getTime()!=0) setProperty(yml, root+map.getKey()+".time", map.getValue().getTime());
							if(map.getValue().getCause().length()!=0) setProperty(yml, root+map.getKey()+".cause", map.getValue().getCause());
						}
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
	
	public void setProperty(YamlConfiguration yml, String root, Object x){
		yml.set(root, x);
	}
	
	//Save Data
	public boolean save(DataType dataType, boolean useLZF, File file, boolean saveBackup){
		if(file==null){
			log.log(Level.WARNING, RMText.preLog+"Cannot load data. Data type unknown!");
			return false;
		}
		if(!file.exists()){
			switch(dataType){
				case CONFIG: log.log(Level.INFO, RMText.preLog+"Config file not found! Creating one..."); break;
				case ALIASES: log.log(Level.INFO, RMText.preLog+"Aliases file not found! Creating one..."); break;
				case STATS: log.log(Level.INFO, RMText.preLog+"Stats file not found! Creating one..."); break;
				case PLAYER: log.log(Level.INFO, RMText.preLog+"Player data file not found! Creating one..."); break;
				case GAME: log.log(Level.INFO, RMText.preLog+"Game data file not found! Creating one..."); break;
				case LOG: log.log(Level.INFO, RMText.preLog+"Game data file not found! Creating one..."); break;
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
					line+=RMText.getLabel("config.language")+"\n";
					line+="language="+config.getLanguage()+"\n\n";
					line+=RMText.getLabel("config.autosave")+"\n";
					line+="autosave="+config.getAutoSave()+"\n\n";
					line+=RMText.getLabel("config.use_permissions")+"\n";
					line+="usepermissions="+config.getPermissionType().name().toLowerCase()+"\n\n";
					line+=RMText.getLabel("config.use_restore")+"\n";
					line+="userestore="+config.getUseRestore()+"\n\n";
					line+=RMText.getLabel("config.server_wide")+"\n\n";
					//Max games
					line+=RMText.getLabel("config.max_games")+"\n";
					line+="maxgames="+config.getMaxGames()+"\n\n";
					//Max games per player
					line+=RMText.getLabel("config.max_games_per_player")+"\n";
					line+="maxgamesperplayer="+config.getMaxGamesPerPlayer()+"\n\n";
					//Default game settings
					line+=RMText.getLabel("config.default_settings1")+"\n\n";
					
					for(Setting setting : Setting.values()){
						SettingPrototype s = config.getSettingLibrary().get(setting);
						if(setting==Setting.advertise) line+=RMText.getLabel("config.default_settings2")+"\n\n";
						line+=RMText.getLabel("config."+s.name())+"\n";
						line+=s.name()+"="+s.toString()+(s.isLocked()?":lock":"")+"\n\n";
					}

					bw.write(line);
					break;
				case ALIASES:
					line = "";
					line+="# ResourceMadness v"+pdfFile.getVersion()+" Aliases file\n\n";
					line+=RMText.getLabel("config.aliases");
					RMCommands commands = config.getCommands();
					for(RMCommand cmd : RMCommand.values()){
						List<String> aliases = commands.getAliasMap().get(cmd);
						line+="\n";
						line+=cmd.name().toLowerCase().replace("_", " ");
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
					line += RMStats.get(RMStatServer.WINS)+","+
							RMStats.get(RMStatServer.LOSSES)+","+
							RMStats.get(RMStatServer.TIMES_PLAYED)+","+
							RMStats.get(RMStatServer.ITEMS_FOUND_TOTAL)+","+
							RMStats.get(RMStatServer.KICKED)+","+
							RMStats.get(RMStatServer.BANNED)+","+
							RMStats.get(RMStatServer.TEMP_BANNED)+";";
					
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
						line += stats.get(RMStat.WINS)+","+
								stats.get(RMStat.LOSSES)+","+
								stats.get(RMStat.TIMES_PLAYED)+","+
								stats.get(RMStat.ITEMS_FOUND_TOTAL)+","+
								stats.get(RMStat.KICKED)+","+
								stats.get(RMStat.BANNED)+","+
								stats.get(RMStat.TEMP_BANNED)+";";
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
						RMGameConfig config = rmGame.getGameConfig();
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
						for(Setting setting : Setting.values()){
							SettingPrototype s = config.getSettingLibrary().get(setting);
							line+=s.toString()+",";
						}
						line += ";";
						//Stats
						RMStats stats = config.getStats();
						line += stats.get(RMStat.WINS)+","+
								stats.get(RMStat.LOSSES)+","+
								stats.get(RMStat.TIMES_PLAYED)+","+
								stats.get(RMStat.ITEMS_FOUND_TOTAL)+","+
								stats.get(RMStat.KICKED)+","+
								stats.get(RMStat.BANNED)+","+
								stats.get(RMStat.TEMP_BANNED)+";";
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
						RMGameConfig config = rmGame.getGameConfig();
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
		createFolders();
		File folder = getDataFolder();
		File langFolder = new File(folder.getAbsolutePath()+File.separator+"lang");
		//load(DataType.LABELS, false, false);
		loadSystemLabels("/data/labels_config.lng");
		loadSystemLabels("/data/labels.lng");
		saveSystemLabels("/data/labels.lng", new File(langFolder+File.separator+"template.lng"));
		config = new RMConfig();
		saveConfig();
		load(DataType.CONFIG, false, false);
		loadYaml(DataType.ALIASES, false);
		if(config.getLanguage()!="") loadLabels(langFolder+File.separator+config.getLanguage()+".lng");
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
					Set<String> cmdKeys = yml.getRoot().getKeys(false);
					RMCommands commands = config.getCommands();
					commands.clear();
					commands.initAliases();
					commands.initDefaultAliases();
					for(String cmdKey : cmdKeys){
						String str = yml.getString(cmdKey);
						if(str==null) continue;
						RMCommand cmd = RMCommand.valueOf(cmdKey.toUpperCase().replace(" ", "_"));
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
					Set<String> players = yml.getRoot().getKeys(false);
					for(String player : players){
						//name
						RMPlayer rmp = new RMPlayer(player);
						String root = player+".";
						rmp.setReady(yml.getBoolean(root+"ready", false));
						rmp.setChatMode(RMHelper.getChatModeByInt(yml.getInt(root+"chatmode", -1)));
						
						//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
						root = player+".stats.";
						RMStats stats = rmp.getStats();
						stats.set(RMStat.WINS, yml.getInt(root+"wins", -1));
						stats.set(RMStat.LOSSES, yml.getInt(root+"losses", -1));
						stats.set(RMStat.TIMES_PLAYED, yml.getInt(root+"timesplayed", -1));
						stats.set(RMStat.ITEMS_FOUND_TOTAL, yml.getInt(root+"itemsfoundtotal", -1));
						stats.set(RMStat.KICKED, yml.getInt(root+"kicked", -1));
						stats.set(RMStat.BANNED, yml.getInt(root+"banned", -1));
						stats.set(RMStat.TEMP_BANNED, yml.getInt(root+"tempbanned", -1));
						
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
					Set<String> ids = yml.getRoot().getKeys(false);
					for(String id : ids){
						String root = id+".";
						String[] location = yml.getString(root+"location").split(",");
						int xLoc = RMHelper.getIntByString(location[0]);
						int yLoc = RMHelper.getIntByString(location[1]);
						int zLoc = RMHelper.getIntByString(location[2]);
						World world = getServer().getWorld(yml.getString(root+"world"));
						if(world==null) RMDebug.warning("WORLD IS NULL");
						Block b = world.getBlockAt(xLoc, yLoc, zLoc);

						RMGameConfig config = new RMGameConfig();
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
						
						//minPlayers,maxPlayers,minTeamPlayers,maxTeamPlayers,safeZone,timeLimit,autoRandomizeAmount
						//warpToSafety,autoRestoreWorld,warnHackedItems,allowHackedItems,allowPlayerLeave
						root = id+".settings.";
						
						for(Setting setting : Setting.values()){
							SettingPrototype s = config.getSettingLibrary().get(setting);
							RMDebug.warning("Setting: "+s.name());
							if(s instanceof SettingInt){
								RMDebug.warning("SettingInt!");
								RMDebug.warning("root: "+root+s.name());
								RMDebug.warning("root2: '"+root+s.name()+"'");
								RMDebug.warning("yml.get: "+yml.getInt(root+s.name()));
								config.setSetting(s.setting(), yml.getInt(root+s.name()));
							}
							if(s instanceof SettingBool){
								RMDebug.warning("SettingBool!");
								RMDebug.warning("root: "+root+s.name());
								RMDebug.warning("yml.get: "+yml.getBoolean(root+s.name(), config.getSettingBool(s.setting())));
								config.setSetting(s.setting(), yml.getBoolean(root+s.name(), config.getSettingBool(s.setting())));
							}
							if(s instanceof SettingStr){
								RMDebug.warning("SettingStr!");
								RMDebug.warning("root: "+root+s.name());
								RMDebug.warning("yml.get: "+yml.getString(root+s.name()));
								config.setSetting(s.setting(), yml.getString(root+s.name()));
							}
						}

						//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
						root = id+".stats.";
						RMStats gameStats = config.getStats();
						gameStats.set(RMStat.WINS, yml.getInt(root+"wins", -1));
						gameStats.set(RMStat.LOSSES, yml.getInt(root+"losses", -1));
						gameStats.set(RMStat.TIMES_PLAYED, yml.getInt(root+"timesplayed", -1));
						gameStats.set(RMStat.ITEMS_FOUND_TOTAL, yml.getInt(root+"itemsfoundtotal", -1));
						gameStats.set(RMStat.KICKED, yml.getInt(root+"kicked", -1));
						gameStats.set(RMStat.BANNED, yml.getInt(root+"banned", -1));
						gameStats.set(RMStat.TEMP_BANNED, yml.getInt(root+"tempbanned", -1));
							
						String data;
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
						
						//ban list
						RMBanList banList = config.getBanList();
						root = id+".banned";
						List<String> keys = yml.getKeys(root);
						if(keys!=null){
							for(String key : keys){
								root = id+".banned."+key;
								String cause = yml.getString(root+".cause");
								if(cause!=null) banList.add(yml.getInt(root, 0), cause, key);
								else banList.add(yml.getInt(root, 0), key);
							}
						}
						
						//teams
						for(RMTeam rmt : rmTeams){
							config.getTeams().add(rmt);
						}
						List<String> teamIds = yml.getKeys(id+".data.teams");
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
					/*
					switch(dataType){
						case CONFIG: log.log(Level.INFO, "Could not find config backup file"); break;
						case ALIASES: log.log(Level.INFO, "Could not find aliases backup file"); break;
						case STATS: log.log(Level.INFO, "Could not find stats backup file"); break;
						case PLAYER: log.log(Level.INFO, "Could not find player data backup file"); break;
						case GAME:log.log(Level.INFO, "Could not find game data backup file"); break;
						case LOG: log.log(Level.INFO, "Could not find game log data backup file"); break;
					}
					*/
				}
			}
			else{
				switch(dataType){
				case CONFIG: log.log(Level.INFO, "Could not find config file"); break;
				case ALIASES: log.log(Level.INFO, "Could not find aliases file"); break;
				case STATS: log.log(Level.INFO, "Could not find stats file"); break;
				case PLAYER: log.log(Level.INFO, "Could not find player data file"); break;
				case GAME: log.log(Level.INFO, "Could not find game data file"); break;
				case LOG: log.log(Level.INFO, "Could not find game log data file"); break;
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
					if((line.startsWith("#"))||(line.length()==0)) continue;
					String[] args;
					switch(dataType){
						case CONFIG:
							args = line.split("=");
							if(args.length==2){
								if(args[0].equalsIgnoreCase("language")) config.setLanguage(args[1]);
								else if(args[0].equalsIgnoreCase("autosave")) config.setAutoSave(RMHelper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("usePermissions")) config.setPermissionTypeByString(args[1]);
								else if(args[0].equalsIgnoreCase("useRestore")) config.setUseRestore(Boolean.parseBoolean(args[1]));
								else if(args[0].equalsIgnoreCase("maxGames")) config.setMaxGames(RMHelper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("maxGamesPerPlayer")) config.setMaxGamesPerPlayer(RMHelper.getIntByString(args[1]));
								else{
									boolean lockArg = args[1].substring(args[1].indexOf(":")+1).equalsIgnoreCase("lock")?true:false;
									
									for(Setting setting : Setting.values()){
										SettingPrototype s = config.getSettingLibrary().get(setting);
										if(args[0].equalsIgnoreCase(s.name())){
											if(s instanceof SettingInt) config.setSetting(s.setting(), RMHelper.getIntByString(args[1]), lockArg);
											if(s instanceof SettingBool) config.setSetting(s.setting(), Boolean.parseBoolean(args[1]), lockArg);
											if(s instanceof SettingStr) config.setSetting(s.setting(), args[1], lockArg);
										}
									}
								}
							}
							break;
						case STATS:
							args = line.split(",");
							//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
							RMStats.set(RMStatServer.WINS, RMHelper.getIntByString(args[0]));
							RMStats.set(RMStatServer.LOSSES, RMHelper.getIntByString(args[1]));
							RMStats.set(RMStatServer.TIMES_PLAYED, RMHelper.getIntByString(args[2]));
							RMStats.set(RMStatServer.ITEMS_FOUND_TOTAL, RMHelper.getIntByString(args[3]));
							RMStats.set(RMStatServer.KICKED, RMHelper.getIntByString(args[4]));
							RMStats.set(RMStatServer.BANNED, RMHelper.getIntByString(args[5]));
							RMStats.set(RMStatServer.TEMP_BANNED, RMHelper.getIntByString(args[6]));
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
					/*
					switch(dataType){
						case CONFIG: log.log(Level.INFO, RMText.preLog+"Could not find config backup file"); break;
						case STATS: log.log(Level.INFO, RMText.preLog+"Could not find stats backup file"); break;
						case PLAYER: log.log(Level.INFO, RMText.preLog+"Could not find player data backup file"); break;
						case GAME: log.log(Level.INFO, RMText.preLog+"Could not find game data backup file"); break;
						case LOG: log.log(Level.INFO, RMText.preLog+"Could not find game log data backup file"); break;
					}
					*/
				}
			}
			else{
				switch(dataType){
				case CONFIG: log.log(Level.INFO, RMText.preLog+"Could not find config file"); break;
				case STATS: log.log(Level.INFO, RMText.preLog+"Could not find stats file"); break;
				case PLAYER: log.log(Level.INFO, RMText.preLog+"Could not find player data file"); break;
				case GAME: log.log(Level.INFO, RMText.preLog+"Could not find game data file"); break;
				case LOG: log.log(Level.INFO, RMText.preLog+"Could not find game log data file"); break;
				}
			}
		}
	}
	
	public void loadLabels(String path){
		File file = new File(path);
		if((file.exists())&&(file.length()>0)){
			try {
				InputStream input = new FileInputStream(file.getAbsoluteFile());
				InputStreamReader isr = new InputStreamReader(input);
				BufferedReader br = new BufferedReader(isr);
				
				RMLabelBundle labels = new RMLabelBundle();
				String line;
				while(true){
					line = br.readLine();
					if(line == null) break;
					if((line.startsWith("#"))||(line.length()==0)) continue;
					if(!line.contains(":")) continue;
					String label = line.substring(0, line.indexOf(":")).trim().toLowerCase();
					String text = line.substring(label.length()+1).trim();
					if(label.startsWith("cmd.")) text = text.toLowerCase();
					if(label.startsWith("filter.par")) text = text.toLowerCase();
					labels.addLabel(label, text);
				}
				input.close();
				RMText.getLabelBundle().putLabels(labels.getLabels());
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		else if(!file.exists()){
			log.log(Level.INFO, RMText.preLog+"The language '"+RMTextHelper.firstLetterToUpperCase(config.getLanguage())+"' could not be found.");
		}
	}
	
	public void loadSystemLabels(String path){
		try{
			InputStream input;
			input = getClass().getResourceAsStream(path);
			if(input==null){
				log.log(Level.WARNING, RMText.preLog+"System language file not found!");
				log.log(Level.INFO, RMText.preLog+"You should re-download ResourceMadness to avoid errors.");
				return;
			}
			InputStreamReader isr = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(isr);
			
			RMLabelBundle labels = new RMLabelBundle();
			String line;
			while(true){
				line = br.readLine();
				if(line == null) break;
				if((line.startsWith("#"))||(line.length()==0)) continue;
				if(!line.contains(":")) continue;
				String label = line.substring(0, line.indexOf(":")).trim().toLowerCase();
				String text = line.substring(label.length()+1).trim();
				if(label.startsWith("cmd.")) text = text.toLowerCase();
				if(label.startsWith("filter.par")) text = text.toLowerCase();
				labels.addLabel(label, text);
			}
			input.close();
			RMText.getLabelBundle().putLabels(labels.getLabels());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void saveSystemLabels(String path, File file){
		try{
			if(!file.exists()){
				log.log(Level.INFO, RMText.preLog+"Language template file not found! Creating one...");
				copyFromJarToFile(path, file);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean copyFromJarToFile(String input, File output){
		File outputFolder = new File(output.getParent());
		if(!outputFolder.exists()) return false;
		if(output.exists()) output.delete();
		if(!output.exists()){
			try{
				output.createNewFile();
			}
			catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		try{
			InputStream is = getClass().getResourceAsStream(input);
			OutputStream os = new FileOutputStream(output);
			byte[] buffer = new byte[4096];
			int length;
			while ((length = is.read(buffer)) > 0) {
			    os.write(buffer, 0, length);
			}
			os.close();
			is.close();
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void parseLoadedPlayerData(String[] strArgs){
		//name
		String[] args = strArgs[0].split(",");
		RMPlayer rmp = new RMPlayer(args[0]);
		rmp.setReady(Boolean.parseBoolean(args[1]));
		
		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
		args = strArgs[1].split(",");
		RMStats stats = rmp.getStats();
		
		stats.set(RMStat.WINS, RMHelper.getIntByString(args[0]));
		stats.set(RMStat.LOSSES, RMHelper.getIntByString(args[1]));
		stats.set(RMStat.TIMES_PLAYED, RMHelper.getIntByString(args[2]));
		stats.set(RMStat.ITEMS_FOUND_TOTAL, RMHelper.getIntByString(args[3]));
		stats.set(RMStat.KICKED, RMHelper.getIntByString(args[4]));
		stats.set(RMStat.BANNED, RMHelper.getIntByString(args[5]));
		stats.set(RMStat.TEMP_BANNED, RMHelper.getIntByString(args[6]));
		
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

		RMGameConfig config = new RMGameConfig();
		RMPartList partList = new RMPartList(b, this);
		if(partList.getMainBlock()==null) return;
		config.setPartList(partList);
		config.setId(RMHelper.getIntByString(args[4]));
		config.setOwnerName(args[5]);
		config.setState(RMHelper.getStateByInt(RMHelper.getIntByString(args[6])));
		config.setInterface(RMHelper.getInterfaceByInt(RMHelper.getIntByString(args[7])));
		config.setTimer(new RMGameTimer(RMHelper.getIntByString(args[8])));
		
		//minPlayers,maxPlayers,minTeamPlayers,maxTeamPlayers,safeZone,timeLimit,autoRandomizeAmount
		//warpToSafety,autoRestoreWorld,warnHackedItems,allowHackedItems,allowPlayerLeave
		args = strArgs[1].split(",");
		
		for(Setting setting : Setting.values()){
			SettingPrototype s = config.getSettingLibrary().get(setting);
			if(args[0].equalsIgnoreCase(s.name())){
				if(s instanceof SettingInt) config.setSetting(s.setting(), RMHelper.getIntByString(args[1]));
				if(s instanceof SettingBool) config.setSetting(s.setting(), Boolean.parseBoolean(args[1]));
				if(s instanceof SettingStr) config.setSetting(s.setting(), args[1]);
			}
		}

		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
		args = strArgs[2].split(",");
		RMStats gameStats = config.getStats();
		
		gameStats.set(RMStat.WINS, RMHelper.getIntByString(args[0]));
		gameStats.set(RMStat.LOSSES, RMHelper.getIntByString(args[1]));
		gameStats.set(RMStat.TIMES_PLAYED, RMHelper.getIntByString(args[2]));
		gameStats.set(RMStat.ITEMS_FOUND_TOTAL, RMHelper.getIntByString(args[3]));
		gameStats.set(RMStat.KICKED, RMHelper.getIntByString(args[4]));
		gameStats.set(RMStat.BANNED, RMHelper.getIntByString(args[5]));
		gameStats.set(RMStat.TEMP_BANNED, RMHelper.getIntByString(args[6]));
		
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
		if(arg.contains(RMText.getLabel("filter.par.stack"))) useDefaultAmount = true;
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
	
	public List<ItemStack> getListItemsFromFilter(String arg, FilterItemType filterItemType){
		List<ItemStack> listItems = new ArrayList<ItemStack>();
		List<Integer> items = getItemsFromFilter(filterItemType);
		for(Integer i : items){
			listItems.add(new ItemStack(i));
		}
		if(arg.contains(RMText.getLabel("filter.par.stack"))){
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
	
	public FilterItemType getFilterItemTypeFromArg(String arg){
		FilterItemType type = null;
		if(arg.contains(RMText.getLabel("filter.par.all"))) type = FilterItemType.ALL;
		else if(arg.contains(RMText.getLabel("filter.par.block"))) type = FilterItemType.BLOCK;
		else if(arg.contains(RMText.getLabel("filter.par.item"))) type = FilterItemType.ITEM;
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
		FilterItemType filterItemType = null;
		//args = args.subList(size, args.size());
		String arg0 = args.get(0);
		filterItemType = getFilterItemTypeFromArg(arg0);

		if(filterItemType!=null){
			listItems = getListItemsFromFilter(arg0, filterItemType);
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
		if((filterItemType==null)&&(listItems.size()==0)){
			return null;
		}
		return listItems.toArray(new ItemStack[listItems.size()]);
	}
	
	public void parseFilter(RMPlayer rmp, List<String> args, FilterState filterState){
		int size = 0;
		List<Integer> items = new ArrayList<Integer>();
		List<Integer[]> amount = new ArrayList<Integer[]>();
		List<ItemStack> listItems = new ArrayList<ItemStack>();
		FilterItemType filterItemType = null;
		FilterType filterType = null;
		int randomize = 0;
		for(String arg : args){
			arg = arg.replace(" ", "");
		}
		if(args.size()==1){
			if(args.get(0).equalsIgnoreCase(RMText.getLabel("cmd.filter.clear"))){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.clear")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.clear")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.clear")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break; 
				}
				filterType = FilterType.CLEAR;
				rmp.setRequestFilter(null, filterState, filterItemType, filterType, randomize);
				return;
			}
		}
		else if(args.size()>1){
			if(args.get(0).equalsIgnoreCase(RMText.getLabel("cmd.filter.add"))){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.add")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.add")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.add")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break; 
				}
				filterType = FilterType.ADD;
				size+=1;
			}
			else if(args.get(0).equalsIgnoreCase(RMText.getLabel("cmd.filter.subtract"))){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.subtract")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.subtract")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.subtract")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break; 
				}
				filterType = FilterType.SUBTRACT;
				size+=1;
			}
			else if(args.get(0).equalsIgnoreCase(RMText.getLabel("cmd.filter.random"))){
				if(filterState==FilterState.FILTER){
					if(!rmp.hasPermission("resourcemadness.filter.random")){
						rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
						return;
					}
					randomize = RMHelper.getIntByString(args.get(1));
					if(randomize>0) size+=2;
				}
			}
			else if(args.get(0).equalsIgnoreCase(RMText.getLabel("cmd.filter.clear"))){
				filterType = FilterType.CLEAR;
				size+=1;
			}
			if(args.size()>2){
				if(args.get(1).equalsIgnoreCase(RMText.getLabel("cmd.filter.random"))){
					if(filterState==FilterState.FILTER){
						if(!rmp.hasPermission("resourcemadness.filter.random")){
							rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
							return;
						}
						randomize = RMHelper.getIntByString(args.get(2));
						if(randomize>0) size+=2;
					}
				}
			}
		}
		if(filterType == FilterType.CLEAR){
			switch(filterState){
			case FILTER: if(!rmp.hasPermission("resourcemadness.filter.clear")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
			case REWARD: if(!rmp.hasPermission("resourcemadness.reward.clear")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
			case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.clear")){ rmp.sendMessage(RMText.getLabel("msg.no_permission_command")); return; } break;
			}
		}
		if(args.size()>0){
			args = args.subList(size, args.size());
			String arg0 = args.get(0);
			filterItemType = getFilterItemTypeFromArg(arg0);
			
			if(filterItemType!=null){
				switch(filterState){
				case FILTER:
					items = getItemsFromFilter(filterItemType);
					amount = getAmountFromFilterArg(arg0, items);
					if(amount.size()==0) items.clear();
					break;
				case REWARD: case TOOLS:
					listItems = getListItemsFromFilter(arg0, filterItemType);
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
			if((filterItemType==null)&&(items.size()==0)&&(listItems.size()==0)) return;
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
			rmp.setRequestFilter(rmItems, filterState, filterItemType, filterType, randomize);
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
		
	public List<Integer> getItemsFromFilter(FilterItemType filterItemType){
		List<Material> materials = Arrays.asList(Material.values());
		List<Integer> items = new ArrayList<Integer>();
		switch(filterItemType){
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
		case FOOD:
			for(Material mat : materials) if(mat!=Material.AIR) if(!mat.isEdible()) items.add(mat.getId());
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
				numAliasMap.put(i, cmd);
				i++;
			}
		}
		
		if(numAliasMap.size()==0){
			rmp.sendMessage(RMText.getLabel("commandslist.no_aliases_yet"));
			return;
		}
		if(id<1) id=1;
		int size = (int)Math.ceil((double)numAliasMap.size()/(double)listLimit);
		if(id>size) id=1;
		i=(id-1)*listLimit;
		rmp.sendMessage(RMText.getLabelArgs("commandslist", ""+id, ""+size));
		int found = 0;
		while((found<listLimit)&&(i<numAliasMap.size())){
			RMCommand cmd = numAliasMap.get(i);
			String strCmd = config.getCommands().getCommandMap().get(cmd);//cmd.name().toLowerCase().replace("_", " ");
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
			rmp.sendMessage(RMText.getLabel("templatelist.no_templates_yet"));
			return;
		}
		if(id<1) id=1;
		int size = (int)Math.ceil((double)templates.length/(double)listLimit);
		if(id>size) id=1;
		int i=(id-1)*listLimit;
		rmp.sendMessage(RMText.getLabelArgs("templatelist", ""+id, ""+size));
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
			
			if(filter.size()!=0) rmp.sendMessage(ChatColor.WHITE+RMText.getLabel("templatelist.filter")+": "+ChatColor.GREEN+filter.size()+ChatColor.WHITE+" "+RMText.getLabel("templatelist.total")+": "+ChatColor.GREEN+filter.getItemsTotal()+(filter.getItemsTotalHigh()>0?ChatColor.WHITE+"-"+filter.getItemsTotalHigh():"")+" "+strFilter+ChatColor.WHITE);
			if(reward.size()!=0) rmp.sendMessage(ChatColor.WHITE+RMText.getLabel("templatelist.reward")+": "+ChatColor.GREEN+reward.size()+ChatColor.WHITE+" "+RMText.getLabel("templatelist.total")+": "+ChatColor.GREEN+reward.getAmount()+" "+strReward+ChatColor.WHITE);
			if(tools.size()!=0) rmp.sendMessage(ChatColor.WHITE+RMText.getLabel("templatelist.tools")+": "+ChatColor.GREEN+tools.size()+ChatColor.WHITE+" "+RMText.getLabel("templatelist.total")+": "+ChatColor.GREEN+tools.getAmount()+" "+strTools+ChatColor.WHITE);
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
			rmp.sendMessage(RMText.getLabel("list.no_games_yet"));
			return;
		}
		if(id<1) id=1;
		int size = (int)Math.ceil((double)games.length/(double)listLimit);
		if(id>size) id=1;
		int i=(id-1)*listLimit;
		rmp.sendMessage(RMText.getLabelArgs("list", ""+id, ""+size));
		
		int found = 0;
		while((found<listLimit)&&(i<games.length)){
			int listId = games[i];
			RMGame rmGame = rmGames.get(listId);
			i++;
			found++;
			rmp.sendMessage(ChatColor.AQUA+RMTextHelper.firstLetterToUpperCase(rmGame.getGameConfig().getWorldName())+ChatColor.WHITE+
					" "+RMTextHelper.firstLetterToUpperCase(RMText.getLabel("list.id"))+": "+ChatColor.YELLOW+rmGame.getGameConfig().getId()+ChatColor.WHITE+
					" "+RMTextHelper.firstLetterToUpperCase(RMText.getLabel("list.owner"))+": "+ChatColor.YELLOW+rmGame.getGameConfig().getOwnerName()+ChatColor.WHITE+
					" "+RMTextHelper.firstLetterToUpperCase(RMText.getLabel("list.timelimit"))+": "+rmGame.getText(rmp, Setting.timelimit));
			rmp.sendMessage(RMText.getLabel("list.players")+": "+ChatColor.GREEN+rmGame.getTeamPlayers().length+ChatColor.WHITE+
					" "+RMText.getLabel("list.ingame")+": "+rmGame.getText(rmp, Setting.minplayers)+ChatColor.WHITE+"-"+rmGame.getText(rmp, Setting.maxplayers)+ChatColor.WHITE+
					" "+RMText.getLabel("list.inteam")+": "+rmGame.getText(rmp, Setting.minteamplayers)+ChatColor.WHITE+"-"+rmGame.getText(rmp, Setting.maxteamplayers));
			rmp.sendMessage(RMText.getLabel("list.teams")+": "+rmGame.getTextTeamColors());
		}
	}
	
	public void rmInfo(RMPlayer rmp, int page){
		int pages = 2;
		if(page<=0) page = 1;
		if(page>pages) page = pages;
		rmp.sendMessage(RMText.getLabelArgs("help", ""+page, ""+pages));
		rmp.sendMessage(RMText.getLabel("desc.gray_green_optional"));
		if(page==1){
			if(rmp.hasPermission("resourcemadness.add")) rmp.sendMessage(RMText.getLabel("help.add"));
			if(rmp.hasPermission("resourcemadness.remove")) rmp.sendMessage(RMText.getLabel("help.remove"));
			if(rmp.hasPermission("resourcemadness.list")) rmp.sendMessage(RMText.getLabel("help.list"));
			if(rmp.hasPermission("resourcemadness.commands")) rmp.sendMessage(RMText.getLabel("help.commands"));
			//Info/Settings
			String line="";
			if(rmp.hasPermission("resourcemadness.info.found")) line+=RMText.getLabel("cmd.info.found")+"/";
			if(rmp.hasPermission("resourcemadness.info.items")) line+=RMText.getLabel("cmd.info.items")+"/";
			if(rmp.hasPermission("resourcemadness.info.reward")) line+=RMText.getLabel("cmd.info.reward")+"/";
			if(rmp.hasPermission("resourcemadness.info.tools")) line+=RMText.getLabel("cmd.info.tools")+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(RMText.getLabelArgs("help.info", line, line));
			
			line ="";
			if(rmp.hasPermission("resourcemadness.info.settings.reset")) line+=RMText.getLabel("cmd.settings.reset");
			if(rmp.hasPermission("resourcemadness.info.settings")) rmp.sendMessage(RMText.getLabelArgs("help.settings", line, (line.length()>0?"/"+line:"")));
			if(rmp.hasPermission("resourcemadness.set")) rmp.sendMessage(RMText.getLabel("help.set"));
			
			//Mode
			line="";
			if(rmp.hasPermission("resourcemadness.mode.filter")) line+=RMText.getLabel("cmd.mode.filter")+"/";
			if(rmp.hasPermission("resourcemadness.mode.reward")) line+=RMText.getLabel("cmd.mode.reward")+"/";
			if(rmp.hasPermission("resourcemadness.mode.tools")) line+=RMText.getLabel("cmd.mode.tools")+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) if(rmp.hasPermission("resourcemadness.mode")) rmp.sendMessage(RMText.getLabelArgs("help.mode", line, RMTextHelper.firstLetterToUpperCase(line)));
			
			if(rmp.hasPermission("resourcemadness.filter")) rmp.sendMessage(RMText.getLabelPermission("help.filter", rmp, "resourcemadness.filter.info"));
			if(rmp.hasPermission("resourcemadness.reward")) rmp.sendMessage(RMText.getLabelPermission("help.reward", rmp, "resourcemadness.filter.info"));
			if(rmp.hasPermission("resourcemadness.tools")) rmp.sendMessage(RMText.getLabelPermission("help.tools", rmp, "resourcemadness.filter.info"));
			
			line="";
			if(rmp.hasPermission("resourcemadness.template.list")) line+=RMText.getLabel("cmd.template.list")+"/";
			if(rmp.hasPermission("resourcemadness.template.load")) line+=RMText.getLabel("cmd.template.load")+"/";
			if(rmp.hasPermission("resourcemadness.template.save")) line+=RMText.getLabel("cmd.template.save")+"/";
			if(rmp.hasPermission("resourcemadness.template.remove")) line+=RMText.getLabel("cmd.template.remove")+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(rmp.hasPermission("resourcemadness.template")) rmp.sendMessage(RMText.getLabelArgs("help.template", line, RMTextHelper.firstLetterToUpperCase(line)));
		}
		else if(page==2){
			if(rmp.hasPermission("resourcemadness.start")) rmp.sendMessage(RMText.getLabel("help.start"));
			
			//Restart/Stop
			String line="";
			//if(rmp.hasPermission("resourcemadness.restart")) line+="Restart/";
			if(rmp.hasPermission("resourcemadness.stop")) line+=RMText.getLabel("cmd.stop")+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(RMText.getLabelArgs("help.stop", line, RMTextHelper.firstLetterToUpperCase(line)));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.pause")) line+=RMText.getLabel("cmd.pause")+"/";
			if(rmp.hasPermission("resourcemadness.resume")) line+=RMText.getLabel("cmd.resume")+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(RMText.getLabelArgs("help.pause", line, RMTextHelper.firstLetterToUpperCase(line)));
			
			if(rmp.hasPermission("resourcemadness.restore")) rmp.sendMessage(RMText.getLabel("help.restore"));
			if(rmp.hasPermission("resourcemadness.join")) rmp.sendMessage(RMText.getLabel("help.join"));
			if(rmp.hasPermission("resourcemadness.quit")) rmp.sendMessage(RMText.getLabel("help.quit"));
			if(rmp.hasPermission("resourcemadness.ready")) rmp.sendMessage(RMText.getLabel("help.ready"));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.chat.world")) line+=RMText.getLabel("cmd.chat.world")+"/";
			if(rmp.hasPermission("resourcemadness.chat.game")) line+=RMText.getLabel("cmd.chat.game")+"/";
			if(rmp.hasPermission("resourcemadness.chat.team")) line+=RMText.getLabel("cmd.chat.team")+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) if(rmp.hasPermission("resourcemadness.chat")) rmp.sendMessage(RMText.getLabelArgs("help.chat", line, RMTextHelper.firstLetterToUpperCase(line)));
			
			if(rmp.hasPermission("resourcemadness.time")) rmp.sendMessage(RMText.getLabel("help.time"));
			if(rmp.hasPermission("resourcemadness.items")) rmp.sendMessage(RMText.getLabel("help.items"));
			if(rmp.hasPermission("resourcemadness.item")) rmp.sendMessage(RMText.getLabel("help.item"));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.claim.found")) line+=RMText.getLabel("cmd.claim.found")+"/";
			if(rmp.hasPermission("resourcemadness.claim.items")) line+=RMText.getLabel("cmd.claim.items")+"/";
			if(rmp.hasPermission("resourcemadness.claim.reward")) line+=RMText.getLabel("cmd.claim.reward")+"/";
			if(rmp.hasPermission("resourcemadness.claim.tools")) line+=RMText.getLabel("cmd.claim.tools")+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(rmp.hasPermission("resourcemadness.claim")) rmp.sendMessage(RMText.getLabelArgs("help.claim", line, line));
			
			if(rmp.hasPermission("resourcemadness.kick")) rmp.sendMessage(RMText.getLabel("help.kick"));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.ban")) line+=RMText.getLabel("cmd.ban")+"/";
			if(rmp.hasPermission("resourcemadness.unban")) line+=RMText.getLabel("cmd.unban")+"/";
			line = RMTextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(RMText.getLabelArgs("help.ban", line, RMTextHelper.firstLetterToUpperCase(line)));
		}
	}
	
	public void rmSetInfo(RMPlayer rmp, int page){
		if(rmp.hasPermission("resourcemadness.set")){
			int pages = 2;
			if(page<=0) page = 1;
			if(page>pages) page = pages;
			
			Setting[] settings = Setting.values();
			SettingLibrary settingLib = config.getSettingLibrary();
			
			int i = 0;
			int iEnd = 0;
			
			if(page==1){
				i = 0;
				iEnd = Setting.warnunequal.ordinal();
			}
			else if(page==2){
				i = Setting.warnunequal.ordinal();
				iEnd = Setting.values().length;
			}
			
			rmp.sendMessage(RMText.getLabelArgs("help_set", ""+page, ""+pages));
			
			while(i<iEnd){
				Setting set = settings[i];
				SettingPrototype s = settingLib.get(set);
				if(rmp.hasPermission("resourcemadness.set."+s.name())) if(!s.isLocked()) rmp.sendMessage(RMText.getLabel("help_set."+s.name()));
				i++;
			}
		}
	}
	
	public void rmFilterInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.filter")){
			rmp.sendMessage(RMText.getLabel("help_filter"));
			if(rmp.hasPermission("resourcemadness.filter.set")) rmp.sendMessage(RMText.getLabel("help_filter.set"));
			if(rmp.hasPermission("resourcemadness.filter.random")) rmp.sendMessage(RMText.getLabel("help_filter.random"));
			if(rmp.hasPermission("resourcemadness.filter.add")) rmp.sendMessage(RMText.getLabel("help_filter.add"));
			if(rmp.hasPermission("resourcemadness.filter.subtract")) rmp.sendMessage(RMText.getLabel("help_filter.subtract"));
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(RMText.getLabel("help_filter.clear"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.filter.info")) rmp.sendMessage(RMText.getLabel("help_filter.example.info"));
			if(rmp.hasPermission("resourcemadness.filter.set")) rmp.sendMessage(RMText.getLabel("help_filter.example.set"));
			if(rmp.hasPermission("resourcemadness.filter.random")) rmp.sendMessage(RMText.getLabel("help_filter.example.random"));
			if(rmp.hasPermission("resourcemadness.filter.add")) rmp.sendMessage(RMText.getLabel("help_filter.example.add"));
			if(rmp.hasPermission("resourcemadness.filter.subtract")) rmp.sendMessage(RMText.getLabel("help_filter.example.subtract"));
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(RMText.getLabel("help_filter.example.clear1"));
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(RMText.getLabel("help_filter.example.clear2"));
		}
	}
	
	public void rmRewardInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(RMText.getLabel("help_reward"));
			if(rmp.hasPermission("resourcemadness.reward.set")) rmp.sendMessage(RMText.getLabel("help_reward.set"));
			if(rmp.hasPermission("resourcemadness.reward.add")) rmp.sendMessage(RMText.getLabel("help_reward.add"));
			if(rmp.hasPermission("resourcemadness.reward.subtract")) rmp.sendMessage(RMText.getLabel("help_reward.subtract"));
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(RMText.getLabel("help_reward.clear"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.reward.info")) rmp.sendMessage(RMText.getLabel("help_reward.example.info"));
			if(rmp.hasPermission("resourcemadness.reward.set")) rmp.sendMessage(RMText.getLabel("help_reward.example.set"));
			if(rmp.hasPermission("resourcemadness.reward.add")) rmp.sendMessage(RMText.getLabel("help_reward.example.add"));
			if(rmp.hasPermission("resourcemadness.reward.subtract")) rmp.sendMessage(RMText.getLabel("help_reward.example.subtract"));
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(RMText.getLabel("help_reward.example.clear1"));
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(RMText.getLabel("help_reward.example.clear2"));
		}
	}
	
	public void rmToolsInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(RMText.getLabel("help_tools"));
			if(rmp.hasPermission("resourcemadness.tools.set")) rmp.sendMessage(RMText.getLabel("help_tools.set"));
			if(rmp.hasPermission("resourcemadness.tools.add")) rmp.sendMessage(RMText.getLabel("help_tools.add"));
			if(rmp.hasPermission("resourcemadness.tools.subtract")) rmp.sendMessage(RMText.getLabel("help_tools.subtract"));
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(RMText.getLabel("help_tools.clear"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.tools.info")) rmp.sendMessage(RMText.getLabel("help_tools.example.info"));
			if(rmp.hasPermission("resourcemadness.tools.set")) rmp.sendMessage(RMText.getLabel("help_tools.example.set"));
			if(rmp.hasPermission("resourcemadness.tools.add")) rmp.sendMessage(RMText.getLabel("help_tools.example.add"));
			if(rmp.hasPermission("resourcemadness.tools.subtract")) rmp.sendMessage(RMText.getLabel("help_tools.example.subtract"));
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(RMText.getLabel("help_tools.example.clear1"));
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(RMText.getLabel("help_tools.example.clear2"));
		}
	}

	public void rmTemplateInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.template")){
			rmp.sendMessage(RMText.getLabel("help_template"));
			if(rmp.hasPermission("resourcemadness.template.list")) rmp.sendMessage(RMText.getLabel("help_template.list"));
			if(rmp.hasPermission("resourcemadness.template.load")) rmp.sendMessage(RMText.getLabel("help_template.load"));
			if(rmp.hasPermission("resourcemadness.template.save")) rmp.sendMessage(RMText.getLabel("help_template.save"));
			if(rmp.hasPermission("resourcemadness.template.remove")) rmp.sendMessage(RMText.getLabel("help_template.remove"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.template.list")) rmp.sendMessage(RMText.getLabel("help_template.example.list"));
			if(rmp.hasPermission("resourcemadness.template.load")) rmp.sendMessage(RMText.getLabel("help_template.example.load"));
			if(rmp.hasPermission("resourcemadness.template.save")) rmp.sendMessage(RMText.getLabel("help_template.example.save"));
			if(rmp.hasPermission("resourcemadness.template.remove")) rmp.sendMessage(RMText.getLabel("help_template.example.remove"));
		}
	}
	
	public void rmClaimInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.claim")){
			rmp.sendMessage(RMText.getLabel("help_claim"));
			if(rmp.hasPermission("resourcemadness.claim.found")) rmp.sendMessage(RMText.getLabel("help_claim.found"));
			if(rmp.hasPermission("resourcemadness.claim.items")) rmp.sendMessage(RMText.getLabel("help_claim.items"));
			if(rmp.hasPermission("resourcemadness.claim.reward")) rmp.sendMessage(RMText.getLabel("help_claim.reward"));
			if(rmp.hasPermission("resourcemadness.claim.tools")) rmp.sendMessage(RMText.getLabel("help_claim.tools"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.claim.found")) rmp.sendMessage(RMText.getLabel("help_claim.example.found"));
			if(rmp.hasPermission("resourcemadness.claim.found.chest")) rmp.sendMessage(RMText.getLabel("help_claim.example.found.chest"));
			if(rmp.hasPermission("resourcemadness.claim.items")) rmp.sendMessage(RMText.getLabel("help_claim.example.items"));
			if(rmp.hasPermission("resourcemadness.claim.items.chest")) rmp.sendMessage(RMText.getLabel("help_claim.example.items.chest"));
			if(rmp.hasPermission("resourcemadness.claim.reward")) rmp.sendMessage(RMText.getLabel("help_claim.example.reward"));
			if(rmp.hasPermission("resourcemadness.claim.reward.chest")) rmp.sendMessage(RMText.getLabel("help_claim.example.reward.chest"));
			if(rmp.hasPermission("resourcemadness.claim.tools")) rmp.sendMessage(RMText.getLabel("help_claim.example.tools"));
			if(rmp.hasPermission("resourcemadness.claim.tools.chest")) rmp.sendMessage(RMText.getLabel("help_claim.example.tools.chest"));
		}
	}
	
	public void rmChatInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.chat")){
			rmp.sendMessage(RMText.getLabel("help_chat"));
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(RMText.getLabel("help_chat.world"));
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(RMText.getLabel("help_chat.game"));
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(RMText.getLabel("help_chat.team"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(RMText.getLabel("help_chat.example.world"));
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(RMText.getLabel("help_chat.example.world.message"));
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(RMText.getLabel("help_chat.example.game"));
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(RMText.getLabel("help_chat.example.game.message"));
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(RMText.getLabel("help_chat.example.team"));
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(RMText.getLabel("help_chat.example.team.message"));
		}
	}
	
	public void rmItemInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.item")){
			rmp.sendMessage(RMText.getLabel("help_item"));
			rmp.sendMessage(RMText.getLabel("help_item.item"));
			rmp.sendMessage(RMText.getLabel("common.examples"));
			rmp.sendMessage(RMText.getLabel("help_item.example1"));
			rmp.sendMessage(RMText.getLabel("help_item.example2"));
			rmp.sendMessage(RMText.getLabel("help_item.example3"));
			rmp.sendMessage(RMText.getLabel("help_item.example4"));
		}
	}
	
	public void rmKickInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.kick")){
			rmp.sendMessage(RMText.getLabel("help_kick"));
			if(rmp.hasPermission("resourcemadness.kick.player")) rmp.sendMessage(RMText.getLabel("help_kick.player"));
			if(rmp.hasPermission("resourcemadness.kick.team")) rmp.sendMessage(RMText.getLabel("help_kick.team"));
			if(rmp.hasPermission("resourcemadness.kick.all")) rmp.sendMessage(RMText.getLabel("help_kick.all"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.kick.player")) rmp.sendMessage(RMText.getLabel("help_kick.example.player"));
			if(rmp.hasPermission("resourcemadness.kick.team")) rmp.sendMessage(RMText.getLabel("help_kick.example.team"));
			if(rmp.hasPermission("resourcemadness.kick.all")) rmp.sendMessage(RMText.getLabel("help_kick.example.all"));
		}
	}
	
	public void rmBanInfo(RMPlayer rmp){
		
		if(rmp.hasPermission("resourcemadness.ban")){
			rmp.sendMessage(RMText.getLabel("help_ban"));
			if(rmp.hasPermission("resourcemadness.ban.player")) rmp.sendMessage(RMText.getLabel("help_ban.player"));
			if(rmp.hasPermission("resourcemadness.ban.team")) rmp.sendMessage(RMText.getLabel("help_ban.team"));
			if(rmp.hasPermission("resourcemadness.ban.all")) rmp.sendMessage(RMText.getLabel("help_ban.all"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.ban.player")) rmp.sendMessage(RMText.getLabel("help_ban.example.player"));
			if(rmp.hasPermission("resourcemadness.ban.team")) rmp.sendMessage(RMText.getLabel("help_ban.example.team"));
			if(rmp.hasPermission("resourcemadness.ban.all")) rmp.sendMessage(RMText.getLabel("help_ban.example.all"));
		}
	}
	
	public void rmUnbanInfo(RMPlayer rmp){
		if(rmp.hasPermission("resourcemadness.unban")){
			rmp.sendMessage(RMText.getLabel("help_unban"));
			if(rmp.hasPermission("resourcemadness.unban.player")) rmp.sendMessage(RMText.getLabel("help_unban.player"));
			rmp.sendMessage(RMText.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.unban.player")) rmp.sendMessage(RMText.getLabel("help_unban.example.player"));
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