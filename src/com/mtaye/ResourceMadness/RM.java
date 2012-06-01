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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ru.tehkode.permissions.PermissionManager;

import java.io.*;

import org.bukkit.configuration.file.YamlConfiguration;

import com.nijikokun.register.payment.Method;

import com.mtaye.ResourceMadness.Commands.RMCommand;
import com.mtaye.ResourceMadness.Game.FilterItemType;
import com.mtaye.ResourceMadness.Game.FilterState;
import com.mtaye.ResourceMadness.Game.FilterType;
import com.mtaye.ResourceMadness.Game.GameState;
import com.mtaye.ResourceMadness.Game.InterfaceState;
import com.mtaye.ResourceMadness.GamePlayer.ChatMode;
import com.mtaye.ResourceMadness.GamePlayer.PlayerAction;
import com.mtaye.ResourceMadness.Stats.RMStat;
import com.mtaye.ResourceMadness.Stats.RMStatServer;
import com.mtaye.ResourceMadness.helper.Helper;
import com.mtaye.ResourceMadness.helper.InventoryHelper;
import com.mtaye.ResourceMadness.helper.LogHelper;
import com.mtaye.ResourceMadness.helper.TextHelper;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.setting.SettingBool;
import com.mtaye.ResourceMadness.setting.SettingInt;
import com.mtaye.ResourceMadness.setting.SettingIntRange;
import com.mtaye.ResourceMadness.setting.SettingLibrary;
import com.mtaye.ResourceMadness.setting.SettingPrototype;
import com.mtaye.ResourceMadness.setting.SettingStr;
import com.mtaye.ResourceMadness.time.PvpTimer;
import com.mtaye.ResourceMadness.time.Timer;
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
	public Config config;
	
	public static enum ClaimType { ITEMS, FOUND, REWARD, TOOLS, CHEST, NONE };
	public static enum DataType { CONFIG, ALIASES, STATS, PLAYER, GAME, LOG, TEMPLATE, LABELS };
	public static enum FolderType { PLUGIN, BACKUP, LANGUAGES };
	public static enum DataSave { SUCCESS, FAIL, NO_DATA };
	
	public boolean useRegister = false;
	private Watcher watcher;
	private int watcherid;
	
	public PermissionHandler permissions = null;
	public PermissionManager permissionsEx = null;
	public boolean permissionBukkit = false;
	public Method economy = null;
	
	LogHelper rmLogHelper;
	
	public RM(){
	}
	
	@Override
	public void onEnable(){
		pdfFile = this.getDescription();
		GamePlayer.rm = this;
		Debug.rm = this;
		Game.rm = this;
		Text.rm = this;
		TextHelper.rm = this;
		Helper.rm = this;
		
		log = getServer().getLogger();
		log.log(Level.INFO, Text.preLog + pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled!" );
		Debug.enable();

        //setupPermissions();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new BlockListener(this), this);
		pm.registerEvents(new EntityListener(this), this);
		//RMConfig.load();
		
		rmLogHelper = new LogHelper(this);
		
		loadAll();
		pm.registerEvents(new PermissionListener(this), this);
		
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
		loadAllLater();
		watcher = new Watcher(this);
		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 20, 20);
		//setupPermissions();
	}
	
	public void onDisable(){
		saveAll();
		getServer().getScheduler().cancelTask(watcherid);
		log.log(Level.INFO, Text.preLog + pdfFile.getName() + " v" + pdfFile.getVersion() + " disabled!" );
		//RMConfig.save();
	}
	
	public Config getRMConfig(){
		return config;
	}
	
	public void setRMConfig(Config config){
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
			String strArg = TextHelper.getTextFromArgs(args);
			strArg = processBestMatch(strArg);
			if(strArg!=null) return strArg.trim().split(" ");
		}
		return args;
	}
	
	public String processBestMatch(String arg){
		RMCommand bestCommand = null;
		String bestAlias = "";
		int bestPoints = 0;
		Commands commands = config.getCommands();
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
			GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(cmd.getName().equals("resourcemadness")){
					if(!rmp.hasPermission("resourcemadness")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
					if(args.length==0){
						rmInfo(rmp, 1);
					}
					else{
						Game rmGame = null;
						int page = Helper.getIntByString(args[0]);
						//String[] argsItems = args.clone();
						if(args.length>1){
							int gameid = Helper.getIntByString(args[0]);
							if(gameid!=-1){
								rmGame = Game.getGame(gameid);
								if(rmGame!=null){
									List<String> argsList = Arrays.asList(args);
									argsList = argsList.subList(1, argsList.size());
									args = argsList.toArray(new String[argsList.size()]);
								}
							}
						}
						args = processCommandArgs(args);
						//CREATE
						if(args[0].equalsIgnoreCase(Text.getLabel("cmd.create"))){
							if(!rmp.hasPermission("resourcemadness.create")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(Game.getGame(args[1])==null){
									rmp.setRequestString(args[1]);
									rmp.setPlayerAction(PlayerAction.CREATE);
									rmp.sendMessage(Text.getLabel("action.create"));
								}
								else rmp.sendMessage(Text.getLabelArgs("game.already_exists", args[1]));
								return true;
							}
							else{
								rmp.sendMessage(Text.getLabel("game.forgot_name"));
								return true;
							}
						}
						//REMOVE
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.remove"))){
							if(!rmp.hasPermission("resourcemadness.remove")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmGame!=null) Game.tryRemoveGame(rmGame, rmp);
							else{
								rmp.setPlayerAction(PlayerAction.REMOVE);
								rmp.sendMessage(Text.getLabel("action.remove"));
							}
							return true;
						}
						//LIST
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.list"))){
							if(!rmp.hasPermission("resourcemadness.list")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length==2) sendList(args[1], rmp);
							else sendList("0", rmp);
							return true;
						}
						//ALIASES
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.commands"))){
							if(!rmp.hasPermission("resourcemadness.commands")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length==2) sendAliasesById(args[1], rmp);
							else sendAliasesById("0", rmp);
							return true;
						}
						//INFO
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.info"))){
							if(!rmp.hasPermission("resourcemadness.info")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(Text.getLabel("cmd.info.found"))){
									if(!rmp.hasPermission("resourcemadness.info.found")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null) rmGame.getInfoFound(rmp);
									else{
										rmp.setPlayerAction(PlayerAction.INFO_FOUND);
										rmp.sendMessage(Text.getLabel("action.info.found"));
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.info.filter"))){
									if(!rmp.hasPermission("resourcemadness.info.filter")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										if((args.length==3)&&(args[2].equalsIgnoreCase(Text.getLabel("cmd.info.x.string")))){
											rmGame.sendFilterInfoString(rmp);
										}
										else rmGame.sendFilterInfo(rmp);
									}
									else{
										if((args.length==3)&&(args[2].equalsIgnoreCase(Text.getLabel("cmd.info.x.string")))){
											rmp.setPlayerAction(PlayerAction.INFO_FILTER_STRING);
											rmp.sendMessage(Text.getLabel("action.info.filter.string"));
										}
										else{
											rmp.setPlayerAction(PlayerAction.INFO_FILTER);
											rmp.sendMessage(Text.getLabel("action.info.filter"));
										}
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.info.reward"))){
									if(!rmp.hasPermission("resourcemadness.info.reward")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										if((args.length==3)&&(args[2].equalsIgnoreCase(Text.getLabel("cmd.info.rx.string")))){
											rmGame.sendRewardInfoString(rmp);
										}
										else rmGame.sendRewardInfo(rmp);
									}
									else{
										if((args.length==3)&&(args[2].equalsIgnoreCase(Text.getLabel("cmd.info.x.string")))){
											rmp.setPlayerAction(PlayerAction.INFO_REWARD_STRING);
											rmp.sendMessage(Text.getLabel("action.info.reward.string"));
										}
										else{
											rmp.setPlayerAction(PlayerAction.INFO_REWARD);
											rmp.sendMessage(Text.getLabel("action.info.reward"));
										}
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.info.tools"))){
									if(!rmp.hasPermission("resourcemadness.info.tools")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										if((args.length==3)&&(args[2].equalsIgnoreCase(Text.getLabel("cmd.info.x.string")))){
											rmGame.sendToolsInfoString(rmp);
										}
										else rmGame.sendToolsInfo(rmp);
									}
									else{
										if((args.length==3)&&(args[2].equalsIgnoreCase(Text.getLabel("cmd.info.x.string")))){
											rmp.setPlayerAction(PlayerAction.INFO_TOOLS_STRING);
											rmp.sendMessage(Text.getLabel("action.info.tools.string"));
										}
										else{
											rmp.setPlayerAction(PlayerAction.INFO_TOOLS);
											rmp.sendMessage(Text.getLabel("action.info.tools"));
										}
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.sendInfo(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.INFO);
									rmp.sendMessage(Text.getLabel("action.info"));
								}
								return true;
							}
						}
						//SETTINGS
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.settings"))){
							if(!rmp.hasPermission("resourcemadness.settings")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if((args.length==2)&&(args[1].equalsIgnoreCase(Text.getLabel("cmd.settings.reset")))){
								if(!rmp.hasPermission("resourcemadness.settings")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
								if(rmGame!=null) rmGame.resetSettings(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.SETTINGS_RESET);
									rmp.sendMessage(Text.getLabel("action.settings.reset"));
								}
								return true;
							}
							page = 0;
							if(args.length>1) page = Helper.getIntByString(args[1]);
							if(rmGame!=null) rmGame.sendSettings(rmp, page);
							else{
								rmp.setRequestInt(page);
								rmp.setPlayerAction(PlayerAction.SETTINGS);
								rmp.sendMessage(Text.getLabel("action.settings"));
							}
							return true;
						}
						//MODE
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.mode"))){
							if(!rmp.hasPermission("resourcemadness.mode")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmp.isIngame()) return rmp.sendMessage(Text.getLabel("msg.cannot_use_command_while_ingame"));
							if(args.length==2){
								if(args[1].equalsIgnoreCase(Text.getLabel("cmd.mode.filter"))){
									if(!rmp.hasPermission("resourcemadness.mode.filter")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.FILTER, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.FILTER);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(Text.getLabel("action.mode.filter"));
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.mode.reward"))){
									if(!rmp.hasPermission("resourcemadness.mode.reward")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.REWARD, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.REWARD);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(Text.getLabel("action.mode.reward"));
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.mode.tools"))){
									if(!rmp.hasPermission("resourcemadness.mode.tools")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										rmGame.changeMode(InterfaceState.TOOLS, rmp);
									}
									else{
										rmp.setRequestInterface(InterfaceState.TOOLS);
										rmp.setPlayerAction(PlayerAction.MODE);
										rmp.sendMessage(Text.getLabel("action.mode.tools"));
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.cycleMode(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.MODE_CYCLE);
									rmp.sendMessage(Text.getLabel("action.mode.cycle"));
								}
								return true;
							}
						}
						//SAVE
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.save"))){
							if(!rmp.hasOpPermission("resourcemadness.admin.save")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							//if(RMGame.getGames().size()!=0){
								rmp.sendMessage(Text.getLabel("save.saving"));
								log.log(Level.INFO, Text.preLog+"Saving...");
								switch(saveAll()){
								case SUCCESS:
									rmp.sendMessage(Text.getLabel("save.success"));
									log.log(Level.INFO, Text.preLog+"Data was saved successfully.");
									break;
								case FAIL:
									rmp.sendMessage(Text.getLabel("save.fail"));
									log.log(Level.INFO, Text.preLog+"Data was not saved properly!");
									break;
								case NO_DATA:
									rmp.sendMessage(Text.getLabel("save.no_data"));
									log.log(Level.INFO, Text.preLog+"No data to save.");
									break;
								}
							//}
							//else rmp.sendMessage(RMText.getLabel("save.no_data"));
							return true;
						}
						//KICK
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.kick"))){
							if(!rmp.hasPermission("resourcemadness.kick")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(Text.getLabel("cmd.kick.team"))){
									if(!rmp.hasPermission("resourcemadness.kick.team")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(args.length>2){
										List<String> list = findTeamColorsFromArgs(args, 2);
										if(list.size()!=0){
											if(rmGame!=null) rmGame.kickTeam(rmp, true, list);
											else{
												rmp.setRequestStringList(list);
												rmp.setPlayerAction(PlayerAction.KICK_TEAM);
												rmp.sendMessage(Text.getLabelArgs("action.kick.team", TextHelper.getStringByStringList(list, ", ")));
											}
											return true;
										}
									}
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.kick.all"))){
									if(!rmp.hasPermission("resourcemadness.kick.all")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null) rmGame.kickAll(rmp, true);
									else{
										rmp.setPlayerAction(PlayerAction.KICK_ALL);
										rmp.sendMessage(Text.getLabel("action.kick.all"));
									}
									return true;
								}
								else{
									if(!rmp.hasPermission("resourcemadness.kick.player")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									List<String> list = findPlayerNamesFromArgs(args, 2);
									if(rmGame!=null) rmGame.kickPlayer(rmp, true, list);
									else{
										rmp.setRequestStringList(list);
										rmp.setPlayerAction(PlayerAction.KICK_PLAYER);
										rmp.sendMessage(Text.getLabelArgs("action.kick.player", TextHelper.getStringByStringList(list, ", ")));
									}
									return true;
								}
							}
							rmKickInfo(rmp);
							return true;
						}
						//BAN
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.ban"))){
							if(!rmp.hasPermission("resourcemadness.ban")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(Text.getLabel("cmd.ban.list"))){
									if(!rmp.hasPermission("resourcemadness.ban.list")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(args.length==3){
										int value = Helper.getIntByString(args[2]);
										if(rmGame!=null) rmGame.sendBanList(rmp, value);
										else{
											rmp.setRequestInt(value);
											rmp.setPlayerAction(PlayerAction.BAN_LIST);
											rmp.sendMessage(Text.getLabel("action.ban.list"));
										}
									}
									else{
										if(rmGame!=null) rmGame.sendBanList(rmp, 0);
										else{
											rmp.setRequestInt(0);
											rmp.setPlayerAction(PlayerAction.BAN_LIST);
											rmp.sendMessage(Text.getLabel("action.ban.list"));
										}
									}
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.ban.team"))){
									if(!rmp.hasPermission("resourcemadness.ban.team")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(args.length>2){
										List<String> list = findTeamColorsFromArgs(args, 2);
										if(list.size()!=0){
											if(rmGame!=null) rmGame.banTeam(rmp, true, list);
											else{
												rmp.setRequestStringList(list);
												rmp.setPlayerAction(PlayerAction.BAN_TEAM);
												rmp.sendMessage(Text.getLabelArgs("action.ban.team", TextHelper.getStringByStringList(list, ", ")));
											}
											return true;
										}
									}
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.ban.all"))){
									if(!rmp.hasPermission("resourcemadness.ban.all")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null) rmGame.banAll(rmp, true);
									else{
										rmp.setPlayerAction(PlayerAction.BAN_ALL);
										rmp.sendMessage(Text.getLabel("action.ban.all"));
									}
									return true;
								}
								else{
									if(!rmp.hasPermission("resourcemadness.ban.player")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									List<String> list = findPlayerNamesFromArgs(args, 2);
									if(rmGame!=null) rmGame.banPlayer(rmp, true, list);
									else{
										rmp.setRequestStringList(list);
										rmp.setPlayerAction(PlayerAction.BAN_PLAYER);
										rmp.sendMessage(Text.getLabelArgs("action.ban.player", TextHelper.getStringByStringList(list, ", ")));
									}
									return true;
								}
							}
							rmBanInfo(rmp);
							return true;
						}
						//UNBAN
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.unban"))){
							if(!rmp.hasPermission("resourcemadness.unban")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(!rmp.hasPermission("resourcemadness.unban.player")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
								List<String> list = findPlayerNamesFromArgs(args, 2);
								if(rmGame!=null) rmGame.unbanPlayer(rmp, true, list);
								else{
									rmp.setRequestStringList(list);
									rmp.setPlayerAction(PlayerAction.UNBAN_PLAYER);
									rmp.sendMessage(Text.getLabelArgs("action.unban.player", TextHelper.getStringByStringList(list, ", ")));
								}
								return true;
							}
							rmUnbanInfo(rmp);
							return true;
						}
						//JOIN
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.join"))){
							if(!rmp.hasPermission("resourcemadness.join")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length==2){
								if(rmGame!=null){								
									Team rmTeam = Game.getTeamById(args[1], rmGame);
									if(rmTeam!=null){
										rmGame.joinTeam(rmTeam, rmp);
										//if(rmp.getTeam()==rmTeam) rmp.markWarpReturnLocation();
										return true;
									}
									rmTeam = rmGame.getTeamByDye(args[1]);
									if(rmTeam!=null){
										rmGame.joinTeam(rmTeam, rmp);
										//if(rmp.getTeam()==rmTeam) rmp.markWarpReturnLocation();
										return true;
									}
									rmp.sendMessage(Text.getLabel("msg.team_does_not_exist"));
									return true;
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.JOIN);
								rmp.sendMessage(Text.getLabel("action.join"));
								return true;
							}
						}
						//QUIT
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.quit"))){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							Game game = rmp.getGame();
							if(game!=null) rmp.getTeam().removePlayer(rmp);
							else rmp.sendMessage(Text.getLabel("msg.did_not_join_any_team_yet"));
							return true;
							
						}
						//READY
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.ready"))){
							if(!rmp.hasPermission("resourcemadness.quit")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							Game game = rmp.getGame();
							if(game!=null){
								if(game.getGameConfig().getState()==GameState.SETUP){
									game.toggleReady(rmp);
								}
								else rmp.sendMessage(Text.getLabel("ready.cannot_while_ingame"));
							}
							else rmp.sendMessage(Text.getLabel("msg.did_not_join_any_team_yet"));
							return true;
							
						}
						//RETURN
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.return"))){
							if(!rmp.hasPermission("resourcemadness.return")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));							
							if((rmp.isInTeam())||(rmp.isIngame())) rmp.sendMessage(Text.getLabel("msg.cannot_use_command_while_ingame"));
							else{
								if(rmp.getWarpReturnLocation()!=null){
									rmp.warpToReturnLocation();
								}
								else rmp.sendMessage(Text.getLabel("return.not_marked"));
							}
							return true;
						}
						//START
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.start"))){
							if(!rmp.hasPermission("resourcemadness.start")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length==2){
								int amount = Helper.getIntByString(args[1]);
								if(amount!=-1){
									if(!rmp.hasPermission("resourcemadness.start.random")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(rmGame!=null){
										rmGame.setRandomizeAmount(rmp, amount);
										rmGame.startGame(rmp);
									}
									else{
										rmp.setRequestInt(amount);
										rmp.setPlayerAction(PlayerAction.START_RANDOM);
										rmp.sendMessage(Text.getLabelArgs("action.start.random", ""+amount));
									}
									return true;
								}
							}
							else{
								if(rmGame!=null) rmGame.startGame(rmp);
								else{
									rmp.setPlayerAction(PlayerAction.START);
									rmp.sendMessage(Text.getLabel("action.start"));
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
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.stop"))){
							if(!rmp.hasPermission("resourcemadness.stop")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.stopGame(rmp, true);
							else{
								rmp.setPlayerAction(PlayerAction.STOP);
								rmp.sendMessage(Text.getLabel("action.stop"));
							}
							return true;
						}
						//PAUSE
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.pause"))){
							if(!rmp.hasPermission("resourcemadness.pause")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.pauseGame(rmp);
							else{
								rmp.setRequestBool(true);
								rmp.setPlayerAction(PlayerAction.PAUSE);
								rmp.sendMessage(Text.getLabel("action.pause"));
							}
							return true;
						}
						//RESUME
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.resume"))){
							if(!rmp.hasPermission("resourcemadness.pause")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.resumeGame(rmp);
							else{
								rmp.setRequestBool(false);
								rmp.setPlayerAction(PlayerAction.RESUME);
								rmp.sendMessage(Text.getLabel("action.resume"));
							}
							return true;
						}
						//RESTORE WORLD
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.restore"))){
							if(!rmp.hasPermission("resourcemadness.restore")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmGame!=null) rmGame.restoreWorld(rmp);
							else{
								rmp.setPlayerAction(PlayerAction.RESTORE);
								rmp.sendMessage(Text.getLabel("action.restore"));
							}
							return true;
						}
						//STATS
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.stats"))){
							if(!rmp.hasPermission("resourcemadness.team")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length==2){
								rmp.getPlayerInfo(args[1]); 
							}
							else rmp.getPlayerInfo("0");
							return true;
						}
						//TEAM
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.team"))){
							if(!rmp.hasPermission("resourcemadness.team")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmp.isIngame()){
								if(args.length==2){
									rmp.getTeamInfo(args[1]); 
								}
								else rmp.getTeamInfo("0");
								return true;
							}
							rmp.sendMessage(Text.getLabel("msg.must_be_ingame_command"));
							return false;
						}
						//ITEMS
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.items"))){
							if(!rmp.hasPermission("resourcemadness.items")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmp.isIngame()){
								rmp.getGameInProgress().updateGameplayInfo(rmp, rmp.getTeam());
								return true;
							}
							rmp.sendMessage(Text.getLabel("msg.must_be_ingame_command"));
							return false;
						}
						//TIME
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.time"))){
							if(!rmp.hasPermission("resourcemadness.time")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmp.isIngame()){
								Timer timer = rmp.getGame().getGameConfig().getTimer();
								if(timer.getTimeLimit()!=0){
									if(timer.getTimeRemaining()!=0){
										rmp.sendMessage(Text.getLabelArgs("time.remaining", timer.getTextTimeRemaining()));
									}
								}
								else rmp.sendMessage(Text.getLabel("time.no_time_limit"));
								return true;
							}
							else rmp.sendMessage(Text.getLabel("msg.must_be_ingame_command"));
							return false;
						}
						//UNDO
						if(args[0].equalsIgnoreCase(Text.getLabel("cmd.undo"))){
							if(!rmp.hasPermission("resourcemadness.undo")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(rmGame!=null){
								rmGame.undo(rmp);
							}
							else{
								rmp.setPlayerAction(PlayerAction.UNDO);
								rmp.sendMessage(Text.getLabel("action.undo"));
							}
							return true;
						}
						//FILTER
						FilterState filterState = null;
						if(args[0].equalsIgnoreCase(Text.getLabel("cmd.filter"))){
							if(!rmp.hasPermission("resourcemadness.filter")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							filterState = FilterState.FILTER;
						}
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.reward"))){
							if(!rmp.hasPermission("resourcemadness.reward")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							filterState = FilterState.REWARD;
						}
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.tools"))){
							if(!rmp.hasPermission("resourcemadness.tools")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							filterState = FilterState.TOOLS;
						}
						if(filterState!=null){
							if(args.length>1){
								//Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
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
												rmp.sendMessage(Text.getLabel("action.filter"));
												break;
											case REWARD:
												rmp.setPlayerAction(PlayerAction.REWARD);
												rmp.sendMessage(Text.getLabel("action.reward"));
												break;
											case TOOLS:
												rmp.setPlayerAction(PlayerAction.TOOLS);
												rmp.sendMessage(Text.getLabel("action.tools"));
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
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.money"))){
							if(!rmp.hasPermission("resourcemadness.money")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(!economy.hasAccount(rmp.getName())) return rmp.sendMessage(Text.getLabel("money.no_account_yet"));
							if(args.length>1){
								int beginIndex = 1;
								FilterType filterType = null;
								if(args.length>2){
									if(args[1].equalsIgnoreCase(Text.getLabel("cmd.filter.add"))){
										if(!rmp.hasPermission("resourcemadness.money.add")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
										filterType = FilterType.ADD;
										beginIndex++;
									}
									else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.filter.subtract"))){
										if(!rmp.hasPermission("resourcemadness.money.add")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
										filterType = FilterType.SUBTRACT;
										beginIndex++;
									}
									else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.filter.clear"))){
										if(!rmp.hasPermission("resourcemadness.money.add")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
										filterType = FilterType.CLEAR;
										beginIndex++;
									}
								}
								else{
									if(!rmp.hasPermission("resourcemadness.money.set")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									filterType = FilterType.SET;
								}
								if(filterType!=null){
									Double d = Helper.getDoubleByString(args[beginIndex]);
									if(d!=null){
										if(rmGame!=null){
											rmGame.parseMoney(rmp, new RequestMoney(filterType, d));
										}
										else{
											switch(filterType){
											case SET: rmp.sendMessage(Text.getLabel("action.money.set")); break;
											case ADD: rmp.sendMessage(Text.getLabel("action.money.add")); break;
											case SUBTRACT: rmp.sendMessage(Text.getLabel("action.money.subtract")); break;
											case CLEAR: rmp.sendMessage(Text.getLabel("action.money.clear")); break;
											}
											rmp.setRequestMoney(new RequestMoney(filterType, d));
											rmp.setPlayerAction(PlayerAction.MONEY);
										}
										return true;
									}
								}
							}
							//rmMoneyInfo(rmp);
						}
						//TEMPLATE
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.template"))){
							if(!rmp.hasPermission("resourcemadness.template")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(Text.getLabel("cmd.template.list"))){
									if(!rmp.hasPermission("resourcemadness.template.list")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(args.length==3) sendTemplateListById(args[2], rmp);
									else sendTemplateListById("0", rmp);
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.template.load"))){
									if(!rmp.hasPermission("resourcemadness.template.load")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(args.length==3){
										Template template = rmp.loadTemplate(args[2].toLowerCase());
										if(template!=null){
											if(rmGame!=null) rmGame.loadTemplate(template, rmp);
											else{
												rmp.setRequestString(args[2]);
												rmp.setPlayerAction(PlayerAction.TEMPLATE_LOAD);
												rmp.sendMessage(Text.getLabelArgs("action.template.load", args[2]));
											}
										}
										return true;
									}
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.template.save"))){
									if(!rmp.hasPermission("resourcemadness.template.save")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(args.length==3){
										if(rmGame!=null) rmGame.saveTemplate(args[2].toLowerCase(), rmp);
										else{
											rmp.setRequestString(args[2].toLowerCase());
											rmp.setPlayerAction(PlayerAction.TEMPLATE_SAVE);
											rmp.sendMessage(Text.getLabelArgs("action.template.save", args[2]));
										}
										return true;
									}
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.template.remove"))){
									if(!rmp.hasPermission("resourcemadness.template.remove")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
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
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.claim"))){
							if(!rmp.hasPermission("resourcemadness.claim")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length>1){
								if(args[1].equalsIgnoreCase(Text.getLabel("cmd.claim.found"))){
									if(!rmp.hasPermission("resourcemadness.claim.found")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(Text.getLabel("cmd.claim.x.chest"))){
												if(!rmp.hasPermission("resourcemadness.claim.found.chest")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												if(rmGame!=null){
													rmp.setRequestInt(rmGame.getGameConfig().getId());
													rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST);
													rmp.sendMessage(Text.getLabel("action.claim.found.chest"));
												}
												else{
													rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST_SELECT);
													rmp.sendMessage(Text.getLabel("action.claim.found.chest.select"));
												}
												return true;
											}
										}
										if(rmGame!=null) rmGame.claimFound(rmp, requestClaimItemsAtArgsPos(rmp, args, 2));
										else{
											rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 2));
											rmp.setPlayerAction(PlayerAction.CLAIM_FOUND);
											rmp.sendMessage(Text.getLabel("action.claim.found"));
										}
										return true;
									}
									else rmp.sendMessage(Text.getLabel("claim.found.cannot_while_ingame"));
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.claim.items"))){
									if(!rmp.hasPermission("resourcemadness.claim.items")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(Text.getLabel("cmd.claim.x.chest"))){
												if(!rmp.hasPermission("resourcemadness.claim.items.chest")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												rmp.setPlayerAction(PlayerAction.CLAIM_ITEMS_CHEST);
												rmp.sendMessage(Text.getLabel("action.claim.items.chest"));
												return true;
											}
										}
										rmp.claimItems(requestClaimItemsAtArgsPos(rmp, args, 2));
										return true;
									}
									else rmp.sendMessage(Text.getLabel("claim.items.cannot_while_ingame"));
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.claim.reward"))){
									if(!rmp.hasPermission("resourcemadness.claim.reward")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(!rmp.isIngame()){
										if(args.length>2){
											if(args[2].equalsIgnoreCase(Text.getLabel("cmd.claim.x.chest"))){
												if(!rmp.hasPermission("resourcemadness.claim.reward.chest")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
												rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
												rmp.setPlayerAction(PlayerAction.CLAIM_REWARD_CHEST);
												rmp.sendMessage(Text.getLabel("action.claim.reward.chest"));;
												return true;
											}
										}
										rmp.claimReward(requestClaimItemsAtArgsPos(rmp, args, 2));
										return true;
									}
									else rmp.sendMessage(Text.getLabel("claim.reward.cannot_while_ingame"));
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.claim.tools"))){
									if(!rmp.hasPermission("resourcemadness.claim.tools")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(args.length>2){
										if(args[2].equalsIgnoreCase(Text.getLabel("cmd.claim.x.chest"))){
											if(!rmp.hasPermission("resourcemadness.claim.tools.chest")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
											rmp.setRequestItems(requestClaimItemsAtArgsPos(rmp, args, 3));
											rmp.setPlayerAction(PlayerAction.CLAIM_TOOLS_CHEST);
											rmp.sendMessage(Text.getLabel("action.claim.tools.chest"));
											return true;
										}
									}
									rmp.claimTools(requestClaimItemsAtArgsPos(rmp, args, 2));
									return true;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.claim.info"))){
									if(!rmp.hasPermission("resourcemadness.claim.info")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									if(args.length==3){
										if(args[2].equalsIgnoreCase(Text.getLabel("cmd.claim.found"))){
											if(!rmp.hasPermission("resourcemadness.claim.info.found")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
											if(rmGame!=null) rmGame.getInfoFound(rmp);
											else{
												rmp.setPlayerAction(PlayerAction.CLAIM_INFO_FOUND);
												rmp.sendMessage(Text.getLabel("action.claim.info.found"));
											}
											return true;
										}
										else if(args[2].equalsIgnoreCase(Text.getLabel("cmd.claim.items"))){
											if(!rmp.hasPermission("resourcemadness.claim.info.items")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
											rmp.getInfoItems();
											return true;
										}
										else if(args[2].equalsIgnoreCase(Text.getLabel("cmd.claim.reward"))){
											if(!rmp.hasPermission("resourcemadness.claim.info.reward")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
											rmp.getInfoReward();
											return true;
										}
										else if(args[2].equalsIgnoreCase(Text.getLabel("cmd.claim.tools"))){
											if(!rmp.hasPermission("resourcemadness.claim.info.tools")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
											rmp.getInfoTools();
											return true;
										}
									}
									rmp.getInfoClaim();
									return true;
								}
							}
							rmClaimInfo(rmp);
							return true;
						}
						//SET
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.set"))){
							if(!rmp.hasPermission("resourcemadness.set")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							page = 0;
							if(args.length>1){
								for(Setting setting : Setting.values()){
									if(args[1].equalsIgnoreCase(Text.getLabel("cmd.set."+setting.name()))){
										if(!rmp.hasPermission("resourcemadness.set."+setting.name())) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
										rmp.setRequestSetting(setting);
										break;
									}
								}
								if(rmp.getRequestSetting()==null) page = Helper.getIntByString(args[1]); 
								else{
									Setting setting = rmp.getRequestSetting();
									SettingLibrary settingLib = config.getSettingLibrary();
									SettingPrototype sp = settingLib.get(setting);
									if(sp instanceof SettingInt){
										if(args.length==3){
											int amount = Helper.getIntByString(args[2]);
											if(amount>-1){
												if(rmGame!=null){
													rmGame.setSetting(rmp, setting, amount);
												}
												else{
													rmp.setRequestInt(Helper.getIntByString(args[2]));
													rmp.sendMessage(Text.getLabelArgs("action.set", setting.name()));
													rmp.setPlayerAction(PlayerAction.SET);
												}
												return true;
											}
										}
									}
									else if(sp instanceof SettingIntRange){
										if(args.length==3){
											IntRange range = new IntRange(args[2]);
											if(range.hasLow()){
												if(rmGame!=null){
													rmGame.setSetting(rmp, setting, range);
												}
												else{
													rmp.setRequestIntegerRange(range);
													rmp.sendMessage(Text.getLabelArgs("action.set", setting.name()));
													rmp.setPlayerAction(PlayerAction.SET);
												}
												return true;
											}
										}
									}
									else if(sp instanceof SettingBool){
										int i=-1;
										if(args.length==3){
											i = Helper.getBoolIntByString(args[2]);
											if(i!=-1){
												if(i>1) i=1;
											}
										}
										if(rmGame!=null){
											rmGame.setSetting(rmp, setting, i);
										}
										else{
											rmp.setRequestInt(i);
											rmp.sendMessage(Text.getLabelArgs("action.set", setting.name()));
											rmp.setPlayerAction(PlayerAction.SET);
										}
										return true;
									}
									else if(sp instanceof SettingStr){
										String arg = null;
										if(args.length==3) arg = args[2];
										else if(args.length==2) arg = "";
										if(arg!=null){
											if(rmGame!=null){
												rmGame.setSetting(rmp, setting, arg);
											}
											else{
												rmp.setRequestString(arg);
												rmp.sendMessage(Text.getLabelArgs("action.set", setting.name()));
												rmp.setPlayerAction(PlayerAction.SET);
											}
											return true;
										}
									}
								}
							}
							rmSetInfo(rmp, page);
							return true;
						}
						//CHAT
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.chat"))){
							if(!rmp.hasPermission("resourcemadness.chat")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							if(args.length>1){
								ChatMode chatMode = null;
								if(args[1].equalsIgnoreCase(Text.getLabel("cmd.chat.world"))){
									if(!rmp.hasPermission("resourcemadness.chat.world")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									chatMode = ChatMode.WORLD;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.chat.game"))){
									if(!rmp.hasPermission("resourcemadness.chat.game")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									chatMode = ChatMode.GAME;
								}
								else if(args[1].equalsIgnoreCase(Text.getLabel("cmd.chat.team"))){
									if(!rmp.hasPermission("resourcemadness.chat.team")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
									chatMode = ChatMode.TEAM;
								}
								
								if(chatMode!=null){
									if(rmp.isIngame()){
										if(args.length>2){
											String message = TextHelper.getTextFromArgs(args, 2);
											rmp.chat(chatMode, rmp.getChatMessage(chatMode, message));
										}
										else rmp.setChatMode(chatMode, true);
									}
									else{
										switch(chatMode){
										case WORLD: rmp.sendMessage(Text.getLabel("chat.world.must_be_ingame")); break;
										case GAME: rmp.sendMessage(Text.getLabel("chat.game.must_be_ingame")); break;
										case TEAM: rmp.sendMessage(Text.getLabel("chat.team.must_be_ingame")); break;
										}
									}
									return true;
								}
							}
							rmChatInfo(rmp);
							return true;
						}
						//TEAM
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.team"))){
							
						}
						//ITEM - Get Item NAME by ID or Item ID by NAME
						else if(args[0].equalsIgnoreCase(Text.getLabel("cmd.item"))){
							List<String> listArgs = new ArrayList<String>();
							for(int i=1; i<args.length; i++){
								listArgs.add(args[i]);
							}
							if(listArgs.size()>0){
								if(!rmp.hasPermission("resourcemadness.item")) return rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
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
											int id1=Helper.getIntByString(strItemsDash[0]);
											int id2=Helper.getIntByString(strItemsDash.length>1?strItemsDash[1]:"-1");
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
											int id=Helper.getIntByString(strItem);
											if(id==-1) list = Helper.getMaterialIdListByString(strItem);
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
									rmp.sendMessage(Text.getLabelArgs("items.found_match", ""+items.size()));
									rmp.sendMessage(TextHelper.getFormattedItemStringByHashMap(items));
									return true;
								}
								else if(itemsWarn.size()>0){
									rmp.sendMessage(Text.getLabel("items.found_none"));
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
		List<String> list = TextHelper.separateStringToList(TextHelper.getTextFromArgs(Arrays.copyOfRange(args, 2, args.length)), " ", ",");
		Iterator<String> iter = list.iterator();
		while(iter.hasNext()){
			DyeColor color = Helper.getDyeByString(iter.next());
			if(color==null) iter.remove();
		}
		return list;
	}
	
	public List<String> findPlayerNamesFromArgs(String[] args, int beginIndex){
		List<String> list = TextHelper.separateStringToList(TextHelper.getTextFromArgs(Arrays.copyOfRange(args, 1, args.length)), " ", ",");
		Iterator<String> iter = list.iterator();
		while(iter.hasNext()){
			String name = iter.next();
			if((name==null)||(name.length()==0)) iter.remove();
		}
		return list;
	}
	
	public void saveAllBackup(){
		log.log(Level.INFO, Text.preLog+"Autosaving...");
		//if(RMGame.getGames().size()==0) return;
		saveConfig();
		File folder = new File(getDataFolder()+File.separator+"backup");
		if(!folder.exists()){
			log.log(Level.INFO, Text.preLog+"Creating backup directory...");
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
			log.log(Level.INFO, Text.preLog+"Config folder not found! Creating one...");
			folder.mkdir();
		}
		if(!langFolder.exists()){
			log.log(Level.INFO, Text.preLog+"Languages folder not found! Creating one...");
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
			Commands commands = config.getCommands();
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
		//decision.add(save(DataType.LOG, false, new File(folder.getAbsolutePath()+File.separator+"gamelogdataun.txt"), true));
		if(decision.contains(false)) return DataSave.FAIL;
		return DataSave.SUCCESS;
	}
	
	public boolean saveYaml(DataType dataType, File file, boolean saveBackup){
		if(file==null){
			log.log(Level.WARNING, Text.preLog+"Cannot load data. Data type unknown!");
			return false;
		}
		if(!file.exists()){
			switch(dataType){
				case CONFIG: log.log(Level.INFO, Text.preLog+"Config file not found! Creating one..."); break;
				case ALIASES: log.log(Level.INFO, Text.preLog+"Aliases file not found! Creating one..."); break;
				case STATS: log.log(Level.INFO, Text.preLog+"Stats file not found! Creating one..."); break;
				case PLAYER: log.log(Level.INFO, Text.preLog+"Player data file not found! Creating one..."); break;
				case GAME: log.log(Level.INFO, Text.preLog+"Game data file not found! Creating one..."); break;
				case LOG: log.log(Level.INFO, Text.preLog+"Game data file not found! Creating one..."); break;
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
					yml.options().header("# ResourceMadness v"+pdfFile.getVersion()+" Aliases file\n\n"+Text.getLabel("config.aliases"));
					Commands commands = config.getCommands();
					for(RMCommand cmd : RMCommand.values()){
						try{
							yml.load();
							String aliases = commands.getAliasMap().get(cmd).toString();
							aliases = aliases.replace("[", "");
							aliases = aliases.replace("]", "");
							String root = cmd.name().toLowerCase().replace("_", " ");
							if((yml.getString(root) == null)||(yml.getString(root).length()==0)) setProperty(yml, root, aliases);
							yml.save();
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					break;
				case PLAYER:
					List<String> keys = yml.getKeys();
					for(String key : keys){
						yml.removeProperty(key);
					}
					yml.options().header("# ResourceMadness v"+pdfFile.getVersion()+" Player data file\n");
					for(GamePlayer rmp : GamePlayer.getPlayers().values()){
						try{
							String root = rmp.getName()+".";
							setProperty(yml, root+"health", rmp.getHealth());
							setProperty(yml, root+"foodlevel", rmp.getFoodLevel());
							setProperty(yml, root+"ready", rmp.getReady());
							setProperty(yml, root+"chatmode", rmp.getChatMode().ordinal());
							setProperty(yml, root+"playareatimeelapsed", rmp.getPlayAreaTimer().getTimeElapsed());
							setProperty(yml, root+"playareatimelimit", rmp.getPlayAreaTimer().getTimeLimit());
							setProperty(yml, root+"updateinventory", rmp.getUpdateInventory());
							
							Location loc = rmp.getWarpTeamLocation();
							if(loc!=null) setProperty(yml, root+"teamlocation", loc.getWorld().getName()+","+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
							
							setProperty(yml, root+"warptoreturnlocation", rmp.getWarpToReturnLocation());
							loc = rmp.getWarpReturnLocation();
							if(loc!=null) setProperty(yml, root+"returnlocation", loc.getWorld().getName()+","+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ());
							//Stats
							root = rmp.getName()+".stats.";
							Stats stats = rmp.getStats();
							if(stats!=null){
								setProperty(yml, root+"wins", stats.get(RMStat.WINS));
								setProperty(yml, root+"losses", stats.get(RMStat.LOSSES));
								setProperty(yml, root+"kills", stats.get(RMStat.KILLS));
								setProperty(yml, root+"deaths", stats.get(RMStat.DEATHS));
								setProperty(yml, root+"timeplayed", stats.get(RMStat.TIME_PLAYED));
								setProperty(yml, root+"itemsfoundtotal", stats.get(RMStat.ITEMS_FOUND_TOTAL));
								setProperty(yml, root+"kicked", stats.get(RMStat.KICKED));
								setProperty(yml, root+"banned", stats.get(RMStat.BANNED));
								setProperty(yml, root+"tempbanned", stats.get(RMStat.TEMP_BANNED));
							}
							//MatchStats
							root = rmp.getName()+".matchstats.";
							stats = rmp.getMatchStats();
							if(stats!=null){
								setProperty(yml, root+"kills", stats.get(RMStat.KILLS));
								setProperty(yml, root+"deaths", stats.get(RMStat.DEATHS));
								setProperty(yml, root+"timeplayed", stats.get(RMStat.TIME_PLAYED));
								setProperty(yml, root+"itemsfoundtotal", stats.get(RMStat.ITEMS_FOUND_TOTAL));
							}
							
							//Data
							root = rmp.getName()+".data.";
							setProperty(yml, root+"inventorystate", InventoryHelper.encodeInventoryToString(rmp.getInventoryState().getContents()));
							setProperty(yml, root+"inventoryarmorstate", InventoryHelper.encodeInventoryToString(rmp.getInventoryState().getArmorContents()));
							setProperty(yml, root+"inventory", InventoryHelper.encodeInventoryToString(rmp.getInventory().getContents()));
							setProperty(yml, root+"inventoryarmor", InventoryHelper.encodeInventoryToString(rmp.getInventory().getArmorContents()));
							//setProperty(yml, root+"inventorystack", InventoryHelper.encodeInventoryToString(rmp.getInventoryStack()));
							//setProperty(yml, root+"inventoryarmorstack", InventoryHelper.encodeInventoryToString(rmp.getInventoryArmorStack()));
							setProperty(yml, root+"items", InventoryHelper.encodeInventoryToString(rmp.getItems().getItemsArray()));
							setProperty(yml, root+"reward", InventoryHelper.encodeInventoryToString(rmp.getReward().getItemsArray())); 
							setProperty(yml, root+"tools", InventoryHelper.encodeInventoryToString(rmp.getTools().getItemsArray()));
							
							//Templates
							root=rmp.getName()+".";
							for(Template template : rmp.getTemplates().values()){
								root=rmp.getName()+".templates."+template.getName()+".";
								setProperty(yml, root+"filter", template.getEncodeToStringFilter());
								setProperty(yml, root+"reward", template.getEncodeToStringReward());
								setProperty(yml, root+"tools", template.getEncodeToStringTools());
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					break;
				case GAME:
					keys = yml.getKeys();
					for(String key : keys){
						yml.removeProperty(key);
					}
					yml.options().header("# ResourceMadness v"+pdfFile.getVersion()+" Game data file\n");
					TreeMap<String, Game> games = Game.getGames();
					for(String name : games.keySet()){
						try{
							GameConfig config = games.get(name).getGameConfig();
							Block b = config.getPartList().getMainBlock();
							String root = name+".";
							setProperty(yml, root+"id",config.getId());
							//setProperty(yml, root+"name",config.getName());
							setProperty(yml, root+"location", config.getWorld().getName().toLowerCase()+","+b.getX()+","+b.getY()+","+b.getZ());
							setProperty(yml, root+"owner",config.getOwnerName());
							setProperty(yml, root+"state",config.getState().ordinal());
							setProperty(yml, root+"interface",config.getInterface().ordinal());
							setProperty(yml, root+"timeelapsed",config.getTimer().getTimeElapsed());
							setProperty(yml, root+"pvptimeelapsed",config.getPvpTimer().getTimeElapsed());
							//Settings
							root = name+".settings.";
							
							for(Setting setting : Setting.values()){
								SettingPrototype s = config.getSettingLibrary().get(setting);
								if(s instanceof SettingInt) setProperty(yml, root+s.name(), ((SettingInt) s).get());
								else if(s instanceof SettingBool) setProperty(yml, root+s.name(), ((SettingBool) s).get());
								else if(s instanceof SettingStr) setProperty(yml, root+s.name(), ((SettingStr) s).get());
								else if(s instanceof SettingIntRange) setProperty(yml, root+s.name(), ((SettingIntRange) s).get().toString());
							}
							//Stats
							root = name+".stats.";
							Stats stats = config.getStats();
							if(stats!=null){
								setProperty(yml, root+"wins", stats.get(RMStat.WINS));
								setProperty(yml, root+"losses", stats.get(RMStat.LOSSES));
								setProperty(yml, root+"itemsfoundtotal", stats.get(RMStat.ITEMS_FOUND_TOTAL));
								setProperty(yml, root+"kicked", stats.get(RMStat.KICKED));
								setProperty(yml, root+"banned", stats.get(RMStat.BANNED));
								setProperty(yml, root+"tempbanned", stats.get(RMStat.TEMP_BANNED));
							}
							
							////Teams
							for(int i=0; i<config.getTeams().size(); i++){
								root = name+".data.teams."+i+".";
								Team team = config.getTeams().get(i);
								setProperty(yml, root+"isdisqualified", team.isDisqualified());
								setProperty(yml, root+"color", team.getTeamColor().name());
								List<String> players = new ArrayList<String>();
								for(GamePlayer rmp : team.getPlayers()){
									players.add(rmp.getName());
								}
								setProperty(yml, root+"players", players);
								setProperty(yml, root+"chest", InventoryHelper.encodeInventoryToString(team.getChest().getStash().getItemsArray()));
								setProperty(yml, root+"items", Filter.encodeFilterToString(team.getChest().getRMItems(), true));
							}
							//Items
							root = name+".data.";
							setProperty(yml, root+"filter", Filter.encodeFilterToString(config.getFilter().getItems(), true));
							setProperty(yml, root+"items", Filter.encodeFilterToString(config.getItems().getItems(), true));
							setProperty(yml, root+"found", InventoryHelper.encodeInventoryToString(config.getFoundArray()));
							setProperty(yml, root+"reward", InventoryHelper.encodeInventoryToString(config.getRewardArray()));
							setProperty(yml, root+"tools", InventoryHelper.encodeInventoryToString(config.getToolsArray()));
							
							//BanList
							root = name+".banned.";
							BanList banList = config.getBanList();
							for(Map.Entry<String, BanTicket> map : banList.entrySet()){
								setProperty(yml, root+map.getKey(), "");
								if(map.getValue().getTime()!=0) setProperty(yml, root+map.getKey()+".time", map.getValue().getTime());
								if(map.getValue().getCause().length()!=0) setProperty(yml, root+map.getKey()+".cause", map.getValue().getCause());
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					break;
			}
			yml.save();
		}
		catch(Exception e){
			Debug.warning("Could not save data: "+dataType.name());
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
				if(!Helper.copyFile(file, new File(folderBackup.getAbsolutePath()+File.separator+file.getName()))){
					switch(dataType){
						case CONFIG: log.log(Level.INFO, Text.preLog+"Could not create config backup file."); break;
						case ALIASES: log.log(Level.INFO, Text.preLog+"Could not create aliases backup file."); break;
						case STATS: log.log(Level.INFO, Text.preLog+"Could not create stats backup file."); break;
						case PLAYER: log.log(Level.INFO, Text.preLog+"Could not create player data backup file."); break;
						case GAME: log.log(Level.INFO, Text.preLog+"Could not create game data backup file."); break;
						case LOG: log.log(Level.INFO, Text.preLog+"Could not create game log data backup file."); break;
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
			log.log(Level.WARNING, Text.preLog+"Cannot load data. Data type unknown!");
			return false;
		}
		if(!file.exists()){
			switch(dataType){
				case CONFIG: log.log(Level.INFO, Text.preLog+"Config file not found! Creating one..."); break;
				case STATS: log.log(Level.INFO, Text.preLog+"Stats file not found! Creating one..."); break;
				case LOG: log.log(Level.INFO, Text.preLog+"Game data file not found! Creating one..."); break;
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
					line+=Text.getLabel("config.language")+"\n";
					line+="language="+config.getLanguage()+"\n\n";
					line+=Text.getLabel("config.autosave")+"\n";
					line+="autosave="+config.getAutoSave()+"\n\n";
					line+=Text.getLabel("config.use_permissions")+"\n";
					line+="usepermissions="+config.getPermissionType().name().toLowerCase()+"\n\n";
					line+=Text.getLabel("config.server_wide")+"\n\n";
					//Max games
					line+=Text.getLabel("config.max_games")+"\n";
					line+="maxgames="+config.getMaxGames()+"\n\n";
					//Max games per player
					line+=Text.getLabel("config.max_games_per_player")+"\n";
					line+="maxgamesperplayer="+config.getMaxGamesPerPlayer()+"\n\n";
					//Default game settings
					line+=Text.getLabel("config.default_settings1")+"\n\n";
					
					for(Setting setting : Setting.values()){
						SettingPrototype s = config.getSettingLibrary().get(setting);
						if(setting==Setting.advertise) line+=Text.getLabel("config.default_settings2")+"\n\n";
						Debug.warning("Setting."+setting.name()+": "+s.toString());
						line+=Text.getLabel("config."+s.name())+"\n";
						line+=s.name()+"="+s.toString()+(s.isLocked()?":lock":"")+"\n\n";
					}

					bw.write(line);
					break;
				case STATS:
					bw.write("# ResourceMadness v"+pdfFile.getVersion()+" Stats file\n\n");
					//Stats
					line = "";
					line += Stats.get(RMStatServer.WINS)+","+
							Stats.get(RMStatServer.LOSSES)+","+
							Stats.get(RMStatServer.ITEMS_FOUND_TOTAL)+","+
							Stats.get(RMStatServer.KICKED)+","+
							Stats.get(RMStatServer.BANNED)+","+
							Stats.get(RMStatServer.TEMP_BANNED)+";";
					
					bw.write(line);
					bw.write("\n");
					break;
				case LOG:
					bw.write("# Resource Madness v"+pdfFile.getVersion()+" Log data file\n\n");
					for(Game rmGame : Game.getGames().values()){
						line = "";
						//Log
						GameConfig config = rmGame.getGameConfig();
						line += config.getName()+"="+rmLogHelper.encodeLogToString(config.getLog());
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
				if(!Helper.copyFile(file, new File(folderBackup.getAbsolutePath()+File.separator+file.getName()))){
					switch(dataType){
						case CONFIG: log.log(Level.INFO, Text.preLog+"Could not create config backup file."); break;
						case STATS: log.log(Level.INFO, Text.preLog+"Could not create stats backup file."); break;
						case LOG: log.log(Level.INFO, Text.preLog+"Could not create game log data backup file."); break;
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
		config = new Config();
		saveConfig();
		load(DataType.CONFIG, false, false);
		loadYaml(DataType.ALIASES, false);
		if(config.getLanguage()!="") loadLabels(langFolder+File.separator+config.getLanguage()+".lng");
	}
	
	public void loadAllLater(){
		createFolders();
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
			log.log(Level.WARNING, Text.preLog+"Cannot load data. Data type unknown!");
			return;
		}
		if((file.exists())&&(file.length()>0)){
			try {
				Configuration yml = new Configuration(file);
				yml.load();
				switch(dataType){
				case ALIASES:
					Set<String> cmdKeys = yml.getRoot().getKeys(false);
					Commands commands = config.getCommands();
					commands.clear();
					commands.initAliases();
					commands.initDefaultAliases();
					for(String cmdKey : cmdKeys){
						try{
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
						catch(Exception e) {
							e.printStackTrace();
						}
					}
					break;
				case PLAYER:
					Set<String> players = yml.getRoot().getKeys(false);
					for(String player : players){
						try{
							//name
							GamePlayer rmp = new GamePlayer(player);
							String root = player+".";
							rmp.setHealth(yml.getInt(root+"health", 0));
							rmp.setFoodLevel(yml.getInt(root+"foodlevel", 0));
							
							Location loc = Helper.getLocationByString(yml.getString(root+"location"));
							if(loc!=null) rmp.setLocation(loc.add(0.5, 0, 0.5));
							
							rmp.setReady(yml.getBoolean(root+"ready", false));
							rmp.setChatMode(Helper.getChatModeByInt(yml.getInt(root+"chatmode", -1)));
							rmp.getPlayAreaTimer().setTimeElapsed(yml.getInt(root+"playareatimeelapsed", -1));
							rmp.getPlayAreaTimer().setTimeLimit(yml.getInt(root+"playareatimelimit", -1));
							rmp.setUpdateInventory(yml.getBoolean(root+"updateinventory", false));
							
							loc = Helper.getLocationByString(yml.getString(root+"teamlocation"));
							if(loc!=null) rmp.setWarpTeamLocation(loc.add(0.5, 0, 0.5));
							
							rmp.setWarpToReturnLocation(yml.getBoolean(root+"warptoreturnlocation", false));
							
							loc = Helper.getLocationByString(yml.getString(root+"returnlocation"));
							if(loc!=null) rmp.setWarpReturnLocation(loc.add(0.5, 0, 0.5));
							
							//Stats
							root = player+".stats.";
							Stats stats = rmp.getStats();
							stats.set(RMStat.WINS, yml.getInt(root+"wins", -1));
							stats.set(RMStat.LOSSES, yml.getInt(root+"losses", -1));
							stats.set(RMStat.KILLS, yml.getInt(root+"kills", -1));
							stats.set(RMStat.DEATHS, yml.getInt(root+"deaths", -1));
							stats.set(RMStat.TIME_PLAYED, yml.getInt(root+"timeplayed", -1));
							stats.set(RMStat.ITEMS_FOUND_TOTAL, yml.getInt(root+"itemsfoundtotal", -1));
							stats.set(RMStat.KICKED, yml.getInt(root+"kicked", -1));
							stats.set(RMStat.BANNED, yml.getInt(root+"banned", -1));
							stats.set(RMStat.TEMP_BANNED, yml.getInt(root+"tempbanned", -1));
							//MatchStats
							root = player+".matchstats.";
							stats = rmp.getMatchStats();
							stats.set(RMStat.KILLS, yml.getInt(root+"kills", -1));
							stats.set(RMStat.DEATHS, yml.getInt(root+"deaths", -1));
							stats.set(RMStat.TIME_PLAYED, yml.getInt(root+"timeplayed", -1));
							stats.set(RMStat.ITEMS_FOUND_TOTAL, yml.getInt(root+"itemsfoundtotal", -1));
							
							//Data
							root = player+".data.";
							
							InventoryState inventoryState = rmp.getInventoryState();
							inventoryState.setContents(InventoryHelper.getItemStackArrayByString(yml.getString(root+"inventorystate")));
							inventoryState.setArmorContents(InventoryHelper.getItemStackArrayByString(yml.getString(root+"inventoryarmorstate")));
							
							GamePlayerInventory inv = rmp.getInventory();
							inv.setContents(InventoryHelper.getItemStackArrayByString(yml.getString(root+"inventory")));
							inv.setArmorContents(InventoryHelper.getItemStackArrayByString(yml.getString(root+"inventoryarmor")));
							//rmp.setInventoryStack(InventoryHelper.getItemStackArrayByString(yml.getString(root+"inventorystack")));
							//rmp.setInventoryArmorStack(InventoryHelper.getItemStackArrayByString(yml.getString(root+"inventoryarmorstack")));
							
							//items
							rmp.setItems(new Stash(InventoryHelper.getItemStackByString(yml.getString(root+"items"))));
							//reward
							rmp.setReward(new Stash(InventoryHelper.getItemStackByString(yml.getString(root+"reward"))));
							//tools
							rmp.setTools(new Stash(InventoryHelper.getItemStackByString(yml.getString(root+"tools"))));
							
							//templates
							root = player+".";
							List<String> templates = yml.getKeys(root+"templates");
							if(templates!=null){
								for(String template : templates){
									root = player+".templates."+template+".";
									Template rmTemplate = new Template(template);
									//filter
									rmTemplate.setFilterParseString(yml.getString(root+"filter"));
									//reward
									rmTemplate.setRewardParseString(yml.getString(root+"reward"));
									//tools
									rmTemplate.setToolsParseString(yml.getString(root+"tools"));
									rmp.setTemplate(rmTemplate);
								}
							}
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
					break;
				case GAME:
					//x,y,z,world,id,owner
					Set<String> names = yml.getRoot().getKeys(false);
					for(String name : names){
						try{
							String root = name+".";
							Location loc = Helper.getLocationByString(yml.getString(root+"location"));
							if(loc==null){
								Debug.warning("Location for game id "+name+" not found.");
								continue;
							}
							Block b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

							GameConfig config = new GameConfig();
							PartList partList = new PartList(b, this);
							if(partList.getMainBlock()==null) continue;
							if((partList.getStoneList()==null)||(partList.getStoneList().size()!=2)) continue;
							config.setPartList(partList);
							
							//Fetch teams - at least two
							List<Team> rmTeams = config.getPartList().fetchTeams();
							if(rmTeams.size()<2) continue;
							
							config.setId(yml.getInt(root+"id", Game.getFreeId()));
							//config.setName(yml.getString(root+"name"));
							config.setName(name);
							config.setOwnerName(yml.getString(root+"owner"));
							config.setState(Helper.getStateByInt(yml.getInt(root+"state", -1)));
							config.setInterface(Helper.getInterfaceByInt(yml.getInt(root+"interface", -1)));
							config.setTimer(new Timer(yml.getInt(root+"timeelapsed", -1)));
							config.setPvpTimer(new PvpTimer(yml.getInt(root+"pvptimeelapsed", -1)));
							
							//minPlayers,maxPlayers,minTeamPlayers,maxTeamPlayers,safeZone,timeLimit,autoRandomizeAmount
							//warpToSafety,autoRestoreWorld,warnHackedItems,allowHackedItems,allowPlayerLeave
							root = name+".settings.";
							
							for(Setting setting : Setting.values()){
								SettingPrototype s = config.getSettingLibrary().get(setting);
								if(s instanceof SettingInt) config.setSetting(s.setting(), yml.getInt(root+s.name()));
								else if(s instanceof SettingBool) config.setSetting(s.setting(), yml.getBoolean(root+s.name(), config.getSettingBool(s.setting())));
								else if(s instanceof SettingStr) config.setSetting(s.setting(), yml.getString(root+s.name()));
								else if(s instanceof SettingIntRange) config.setSetting(s.setting(), new IntRange(yml.getString(root+s.name())));
							}

							//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
							root = name+".stats.";
							Stats gameStats = config.getStats();
							gameStats.set(RMStat.WINS, yml.getInt(root+"wins", -1));
							gameStats.set(RMStat.LOSSES, yml.getInt(root+"losses", -1));
							gameStats.set(RMStat.ITEMS_FOUND_TOTAL, yml.getInt(root+"itemsfoundtotal", -1));
							gameStats.set(RMStat.KICKED, yml.getInt(root+"kicked", -1));
							gameStats.set(RMStat.BANNED, yml.getInt(root+"banned", -1));
							gameStats.set(RMStat.TEMP_BANNED, yml.getInt(root+"tempbanned", -1));
								
							String data;
							//filter items
							root = name+".data.";
							data = yml.getString(root+"filter");
							if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("FILTER"))){
								HashMap<Integer, Item> rmItems = Filter.getRMItemsByStringArray(Arrays.asList(data), true);
								config.setFilter(new Filter(rmItems));
							}
							//game items
							data = yml.getString(root+"items");
							if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("ITEMS"))){
								HashMap<Integer, Item> rmItems = Filter.getRMItemsByStringArray(Arrays.asList(data), true);
								config.setItems(new Filter(rmItems));
							}
							//found items
							data = yml.getString(root+"found");
							if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("FOUND"))){
								config.setFound(new Stash(InventoryHelper.getItemStackByString(data)));
							}
							//reward items
							data = yml.getString(root+"reward");
							if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("REWARD"))){
								config.setReward(new Stash(InventoryHelper.getItemStackByString(data)));
							}
							//tools items
							data = yml.getString(root+"tools");
							if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("TOOLS"))){
								config.setTools(new Stash(InventoryHelper.getItemStackByString(data)));
							}
							
							//ban list
							BanList banList = config.getBanList();
							root = name+".banned";
							List<String> keys = yml.getKeys(root);
							if(keys!=null){
								for(String key : keys){
									root = name+".banned."+key;
									String cause = yml.getString(root+".cause");
									if(cause!=null) banList.add(yml.getInt(root, 0), cause, key);
									else banList.add(yml.getInt(root, 0), key);
								}
							}
							
							//teams
							for(Team rmt : rmTeams){
								config.getTeams().add(rmt);
							}
							List<String> teamIds = yml.getKeys(name+".data.teams");
							for(String teamId : teamIds){
								root = name+".data.teams."+teamId+".";
								Team rmTeam = config.getTeams().get(Integer.parseInt(teamId));
								if(rmTeam!=null){
									rmTeam.isDisqualified(yml.getBoolean(root+"isdisqualified", false));
									List<String> teamPlayers = yml.getStringList(root+"players", new ArrayList<String>());
									if(teamPlayers!=null){
										for(String player : teamPlayers){
											GamePlayer rmp = GamePlayer.getPlayerByName(player);
											if(rmp!=null) rmTeam.addPlayerSilent(rmp);
										}
									}
									//team chest
									data = yml.getString(root+"chest");
									if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("CHEST"))){
										rmTeam.getChest().setStash(new Stash(InventoryHelper.getItemStackByString(data)));
									}
									//team items
									data = yml.getString(root+"items");
									if((data!=null)&&(data.length()>0)&&(!data.equalsIgnoreCase("ITEMS"))){
										HashMap<Integer, Item> rmItems = Filter.getRMItemsByStringArray(Arrays.asList(data), true);
										rmTeam.getChest().setRMItems(rmItems);
									}
								}
							}
							Game.tryCreateGameFromGameConfig(config);
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
					break;
				case LOG:
					break;
				}
				//saveConfig();
			}
			catch(Exception e) {
				Debug.warning("Could not load data: "+dataType.name());
				e.printStackTrace();
			}
		}
		else{
			if(loadBackup){
				if(Helper.copyFile(new File(getDataFolder().getAbsolutePath()+File.separator+"backup"+File.separator+file.getName()), file)){
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
			case LOG: file = new File(folder.getAbsolutePath()+File.separator+"gamelogdata.txt"); break;
		}
		if(file==null){
			log.log(Level.WARNING, Text.preLog+"Cannot load data. Data type unknown!");
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
								else if(args[0].equalsIgnoreCase("autosave")) config.setAutoSave(Helper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("usePermissions")) config.setPermissionTypeByString(args[1]);
								else if(args[0].equalsIgnoreCase("maxGames")) config.setMaxGames(Helper.getIntByString(args[1]));
								else if(args[0].equalsIgnoreCase("maxGamesPerPlayer")) config.setMaxGamesPerPlayer(Helper.getIntByString(args[1]));
								else{
									boolean lockArg = args[1].substring(args[1].indexOf(":")+1).equalsIgnoreCase("lock")?true:false;
									for(Setting setting : Setting.values()){
										SettingPrototype s = config.getSettingLibrary().get(setting);
										if(args[0].equalsIgnoreCase(s.name())){
											if(s instanceof SettingInt) config.setSetting(s.setting(), Helper.getIntByString(args[1]), lockArg);
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
							Stats.set(RMStatServer.WINS, Helper.getIntByString(args[0]));
							Stats.set(RMStatServer.LOSSES, Helper.getIntByString(args[1]));
							Stats.set(RMStatServer.ITEMS_FOUND_TOTAL, Helper.getIntByString(args[3]));
							Stats.set(RMStatServer.KICKED, Helper.getIntByString(args[4]));
							Stats.set(RMStatServer.BANNED, Helper.getIntByString(args[5]));
							Stats.set(RMStatServer.TEMP_BANNED, Helper.getIntByString(args[6]));
							break;
						case LOG:
							rmLogHelper.parseLoadedLogData(line);
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
				if(Helper.copyFile(new File(getDataFolder().getAbsolutePath()+File.separator+"backup"+File.separator+file.getName()), file)){
					load(dataType, useLZF, false);
				}
				else{
					/*
					switch(dataType){
						case CONFIG: log.log(Level.INFO, RMText.preLog+"Could not find config backup file"); break;
						case STATS: log.log(Level.INFO, RMText.preLog+"Could not find stats backup file"); break;
						case LOG: log.log(Level.INFO, RMText.preLog+"Could not find game log data backup file"); break;
					}
					*/
				}
			}
			else{
				switch(dataType){
				case CONFIG: log.log(Level.INFO, Text.preLog+"Could not find config file"); break;
				case STATS: log.log(Level.INFO, Text.preLog+"Could not find stats file"); break;
				case LOG: log.log(Level.INFO, Text.preLog+"Could not find game log data file"); break;
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
				
				LabelBundle labels = new LabelBundle();
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
				Text.getLabelBundle().putLabels(labels.getLabels());
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		else if(!file.exists()){
			log.log(Level.INFO, Text.preLog+"The language '"+TextHelper.firstLetterToUpperCase(config.getLanguage())+"' could not be found.");
		}
	}
	
	public void loadSystemLabels(String path){
		try{
			InputStream input;
			input = getClass().getResourceAsStream(path);
			if(input==null){
				log.log(Level.WARNING, Text.preLog+"System language file not found!");
				log.log(Level.INFO, Text.preLog+"You should re-download ResourceMadness to avoid errors.");
				return;
			}
			InputStreamReader isr = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(isr);
			
			LabelBundle labels = new LabelBundle();
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
			Text.getLabelBundle().putLabels(labels.getLabels());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void saveSystemLabels(String path, File file){
		try{
			if(!file.exists()){
				log.log(Level.INFO, Text.preLog+"Language template file not found! Creating one...");
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
	
	public List<Integer[]> getAmountFromFilterArg(String arg, List<Integer> items){
		boolean useDefaultAmount = false;
		List<Integer[]> amount = new ArrayList<Integer[]>();
		amount.clear();
		if(arg.contains(Text.getLabel("filter.par.stack"))) useDefaultAmount = true;
		else if(arg.contains(":")){
			List<String> strArgs = Filter.splitArgsByColon(arg);
			String strAmount = ""; 
			String[] strSplit = strArgs.get(0).split(":");
			if(strSplit.length>1){
				strAmount = strSplit[1];
				Integer[] intAmount = Filter.checkInt(strAmount);
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
		if(arg.contains(Text.getLabel("filter.par.stack"))){
			for(ItemStack item : listItems){
				item.setAmount(item.getType().getMaxStackSize());
			}
		}
		else if(arg.contains(":")){
			List<String> strArgs = Filter.splitArgsByColon(arg);
			String strAmount = ""; 
			String[] strSplit = strArgs.get(0).split(":");
			if(strSplit.length>1){
				strAmount = strSplit[1];
				Integer[] intAmount = Filter.checkInt(strAmount);
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
		arg = "|"+arg;
		FilterItemType type = null;
		if(arg.contains("|"+Text.getLabel("filter.par.all"))) type = FilterItemType.ALL;
		else if(arg.contains("|"+Text.getLabel("filter.par.block"))) type = FilterItemType.BLOCK;
		else if(arg.contains("|"+Text.getLabel("filter.par.item"))) type = FilterItemType.ITEM;
		return type;
	}
	
	public ItemStack[] requestClaimItemsAtArgsPos(GamePlayer rmp, String[] args, int pos){
		if(args.length>pos){
			List<String> listArgs = new ArrayList<String>();
			for(int i=pos; i<args.length; i++) listArgs.add(args[i]);
			return parseClaim(rmp, listArgs);
		}
		return null;
	}
	
	public ItemStack[] parseClaim(GamePlayer rmp, List<String> args){
		List<ItemStack> listItems = new ArrayList<ItemStack>();
		FilterItemType filterItemType = null;
		//args = args.subList(size, args.size());
		String arg0 = args.get(0);
		filterItemType = getFilterItemTypeFromArg(arg0);

		if(filterItemType!=null){
			listItems = getListItemsFromFilter(arg0, filterItemType);
		}
		else{
			HashMap<Integer, Integer[]> hashItems = Filter.getItemsByStringArray(args, false);
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
	
	public void parseFilter(GamePlayer rmp, List<String> args, FilterState filterState){
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
			if(args.get(0).equalsIgnoreCase(Text.getLabel("cmd.filter.clear"))){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.clear")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.clear")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.clear")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break; 
				}
				filterType = FilterType.CLEAR;
				//if(rmp.getRequestFilter()!=null) rmp.setRequestFilterBackup(rmp.getRequestFilter().clone());
				rmp.setRequestFilter(new RequestFilter(null, filterState, filterItemType, filterType, randomize));
				return;
			}
		}
		else if(args.size()>1){
			if(args.get(0).equalsIgnoreCase(Text.getLabel("cmd.filter.add"))){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.add")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.add")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.add")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break; 
				}
				filterType = FilterType.ADD;
				size+=1;
			}
			else if(args.get(0).equalsIgnoreCase(Text.getLabel("cmd.filter.subtract"))){
				switch(filterState){
				case FILTER: if(!rmp.hasPermission("resourcemadness.filter.subtract")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
				case REWARD: if(!rmp.hasPermission("resourcemadness.reward.subtract")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
				case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.subtract")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break; 
				}
				filterType = FilterType.SUBTRACT;
				size+=1;
			}
			else if(args.get(0).equalsIgnoreCase(Text.getLabel("cmd.filter.random"))){
				if(filterState==FilterState.FILTER){
					if(!rmp.hasPermission("resourcemadness.filter.random")){
						rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
						return;
					}
					randomize = Helper.getIntByString(args.get(1));
					if(randomize>0) size+=2;
				}
			}
			else if(args.get(0).equalsIgnoreCase(Text.getLabel("cmd.filter.clear"))){
				filterType = FilterType.CLEAR;
				size+=1;
			}
			if(args.size()>2){
				if(args.get(1).equalsIgnoreCase(Text.getLabel("cmd.filter.random"))){
					if(filterState==FilterState.FILTER){
						if(!rmp.hasPermission("resourcemadness.filter.random")){
							rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
							return;
						}
						randomize = Helper.getIntByString(args.get(2));
						if(randomize>0) size+=2;
					}
				}
			}
		}
		if(filterType == FilterType.CLEAR){
			switch(filterState){
			case FILTER: if(!rmp.hasPermission("resourcemadness.filter.clear")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
			case REWARD: if(!rmp.hasPermission("resourcemadness.reward.clear")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
			case TOOLS: if(!rmp.hasPermission("resourcemadness.tools.clear")){ rmp.sendMessage(Text.getLabel("msg.no_permission_command")); return; } break;
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
				HashMap<Integer, Integer[]> hashItems = Filter.getItemsByStringArray(args, false);
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
			HashMap<Integer, Item> rmItems = new HashMap<Integer, Item>();
			switch(filterState){
				case FILTER:
					for(int i=0; i<items.size(); i++){
						Integer iItem = items.get(i);
						Integer[] iAmount = amount.get(i);
						int amount1 = -1;
						int amount2 = -1;
						if(iAmount.length>0) amount1 = iAmount[0];
						if(iAmount.length>1) amount2 = iAmount[1];
						
						Item rmItem = new Item(iItem);
						if(amount1 > -1) rmItem.setAmount(amount1);
						if(amount2 > -1) rmItem.setAmountHigh(amount2);
						
						rmItems.put(items.get(i), rmItem);
					}
					break;
				case REWARD: case TOOLS:
					for(ItemStack item : listItems){
						rmItems.put(item.getTypeId(), new Item(item));
					}
					break;
			}
			//if(rmp.getRequestFilter()!=null) rmp.setRequestFilterBackup(rmp.getRequestFilter().clone());
			rmp.setRequestFilter(new RequestFilter(rmItems, filterState, filterItemType, filterType, randomize));
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
	
	public void sendAliasesById(String arg, GamePlayer rmp){
		int listLimit = 12;
		int id = Helper.getIntByString(arg);
		
		TreeMap<RMCommand, List<String>> aliasMap = config.getCommands().getAliasMap();
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
			rmp.sendMessage(Text.getLabel("commandslist.no_aliases_yet"));
			return;
		}
		if(id<1) id=1;
		int size = (int)Math.ceil((double)numAliasMap.size()/(double)listLimit);
		if(id>size) id=1;
		rmp.sendMessage(Text.getLabelArgs("commandslist", ""+id, ""+size));
		id=(id-1)*listLimit;
		i = 0;
		while((i<listLimit)&&(id<numAliasMap.size())){
			RMCommand cmd = numAliasMap.get(id);
			String strCmd = config.getCommands().getCommandMap().get(cmd);//cmd.name().toLowerCase().replace("_", " ");
			String aliases = TextHelper.getStringByStringList(aliasMap.get(cmd), ChatColor.WHITE+", ", ChatColor.GREEN+"", "");
			rmp.sendMessage(ChatColor.WHITE+strCmd+": "+aliases);
			id++;
			i++;
		}
	}
		
	public void sendTemplateListById(String arg, GamePlayer rmp){
		int listLimit = 4;
		int strLength = 74;
		int id = Helper.getIntByString(arg);
		Debug.warning("arg: "+arg);
		Debug.warning("id: "+id);
		TreeMap<String, Template> rmpTemplates = rmp.getTemplates();
		Template[] templates = rmpTemplates.values().toArray(new Template[rmpTemplates.size()]);
		if(templates.length==0){
			rmp.sendMessage(Text.getLabel("templatelist.no_templates_yet"));
			return;
		}
		if(id<1) id=1;
		Debug.warning("id: "+id);
		int size = (int)Math.ceil((double)templates.length/(double)listLimit);
		if(id>size) id=1;
		Debug.warning("id: "+id);
		rmp.sendMessage(Text.getLabelArgs("templatelist", ""+id, ""+size));
		id=(id-1)*listLimit;
		int i = 0;
		while((i<listLimit)&&(id<templates.length)){
			Template rmTemplate = templates[id];
			rmp.sendMessage(""+ChatColor.YELLOW+id+" "+ChatColor.GREEN+rmTemplate.getName());
			Filter filter = rmTemplate.getFilter();
			Stash reward = rmTemplate.getReward();
			Stash tools = rmTemplate.getTools();
			
			List<ItemStack> filterItems = Filter.convertToListItemStack(filter.getItems());
			List<ItemStack> rewardItems = reward.getItems();
			List<ItemStack> toolsItems = tools.getItems();
			
			String strFilter = TextHelper.getStringSortedItems(filterItems, 0);
			String strReward = TextHelper.getStringSortedItems(rewardItems, 0);
			String strTools = TextHelper.getStringSortedItems(toolsItems, 0);
			
			if(strFilter.length()>strLength) strFilter = strFilter.substring(0, strLength)+"...";
			if(strReward.length()>strLength) strReward = strReward.substring(0, strLength)+"...";
			if(strTools.length()>strLength) strTools = strTools.substring(0, strLength)+"...";
			
			if(filter.size()!=0) rmp.sendMessage(ChatColor.WHITE+Text.getLabel("templatelist.filter")+": "+ChatColor.GREEN+filter.size()+ChatColor.WHITE+" "+Text.getLabel("templatelist.total")+": "+ChatColor.GREEN+filter.getItemsTotal()+(filter.getItemsTotalHigh()>0?ChatColor.WHITE+"-"+ChatColor.GREEN+filter.getItemsTotalHigh():"")+" "+strFilter+ChatColor.WHITE);
			if(reward.size()!=0) rmp.sendMessage(ChatColor.WHITE+Text.getLabel("templatelist.reward")+": "+ChatColor.GREEN+reward.size()+ChatColor.WHITE+" "+Text.getLabel("templatelist.total")+": "+ChatColor.GREEN+reward.getAmount()+" "+strReward+ChatColor.WHITE);
			if(tools.size()!=0) rmp.sendMessage(ChatColor.WHITE+Text.getLabel("templatelist.tools")+": "+ChatColor.GREEN+tools.size()+ChatColor.WHITE+" "+Text.getLabel("templatelist.total")+": "+ChatColor.GREEN+tools.getAmount()+" "+strTools+ChatColor.WHITE);
			id++;
			i++;
		}
	}
	
	public void sendList(String arg, GamePlayer rmp){
		Game[] games = Game.getAdvertisedGames();
		if(games.length==0){
			rmp.sendMessage(Text.getLabel("list.no_games_yet"));
			return;
		}
		List<Game> foundGames = new ArrayList<Game>();
		int id = Helper.getIntByString(arg);
		if(id!=-1) sendListById(id, rmp, games);
		else{
			for(Game game : games){
				if(game.getGameConfig().getName().equalsIgnoreCase(arg)){
					game.sendInfo(rmp);
					return;
				}
				else if(game.getGameConfig().getName().toLowerCase().startsWith(arg.toLowerCase())){
					foundGames.add(game);
				}
			}
			if(foundGames.size()==1){
				foundGames.get(0).sendInfo(rmp);
			}
			else if(foundGames.size()>1){
				sendListById(-1, rmp, foundGames.toArray(new Game[foundGames.size()]));
			}
			else rmp.sendMessage(Text.getLabelArgs("list.game_not_found", arg));
		}
	}
	
	public void sendListById(int page, GamePlayer rmp, Game... games){
		int listLimit = 5;
		int id = page;
		if(id<1) id=1;
		int size = (int)Math.ceil((double)games.length/(double)listLimit);
		if(id>size) id=1;
		if(page!=-1) rmp.sendMessage(Text.getLabelArgs("list.page", ""+id, ""+size));
		else rmp.sendMessage(Text.getLabel("list"));
		id=(id-1)*listLimit;
		int i = 0;
		while((i<listLimit)&&(id<games.length)){
			Game game = games[id];
			String message = "";
			message += ChatColor.YELLOW+""+game.getGameConfig().getId()+ChatColor.WHITE+" ";
			message += ChatColor.AQUA+game.getGameConfig().getName()+ChatColor.WHITE+" ";
			message += Text.getLabel("list.world")+": "+ChatColor.YELLOW+TextHelper.firstLetterToUpperCase(game.getGameConfig().getWorld().getName().toLowerCase())+ChatColor.WHITE+" ";
			message += TextHelper.firstLetterToUpperCase(Text.getLabel("list.owner"))+": "+ChatColor.YELLOW+game.getGameConfig().getOwnerName()+ChatColor.WHITE+" ";
			message += TextHelper.firstLetterToUpperCase(Text.getLabel("list.timelimit"))+": "+game.getText(rmp, Setting.timelimit);
			rmp.sendMessage(message);
			message = Text.getLabel("list.players")+": "+ChatColor.GREEN+game.getTeamPlayers().length+ChatColor.WHITE+" ";
			message += Text.getLabel("list.ingame")+": "+game.getText(rmp, Setting.minplayers)+ChatColor.WHITE+"-"+game.getText(rmp, Setting.maxplayers)+ChatColor.WHITE+" ";
			message += Text.getLabel("list.inteam")+": "+game.getText(rmp, Setting.minteamplayers)+ChatColor.WHITE+"-"+game.getText(rmp, Setting.maxteamplayers);
			rmp.sendMessage(message);
			rmp.sendMessage(Text.getLabel("list.teams")+": "+game.getTextTeamPlayersNumbers());
			id++;
			i++;
		}
	}
	
	public void rmInfo(GamePlayer rmp, int page){
		int pages = 2;
		if(page<=0) page = 1;
		if(page>pages) page = pages;
		rmp.sendMessage(Text.getLabelArgs("help", ""+page, ""+pages));
		rmp.sendMessage(Text.getLabel("desc.gray_green_optional"));
		if(page==1){
			if(rmp.hasPermission("resourcemadness.create")) rmp.sendMessage(Text.getLabel("help.create"));
			if(rmp.hasPermission("resourcemadness.remove")) rmp.sendMessage(Text.getLabel("help.remove"));
			if(rmp.hasPermission("resourcemadness.list")) rmp.sendMessage(Text.getLabel("help.list"));
			if(rmp.hasPermission("resourcemadness.commands")) rmp.sendMessage(Text.getLabel("help.commands"));
			//Info/Settings
			String line="";
			if(rmp.hasPermission("resourcemadness.info.found")) line+=Text.getLabel("cmd.info.found")+"/";
			if(rmp.hasPermission("resourcemadness.info.filter")) line+=Text.getLabel("cmd.info.filter")+"/";
			if(rmp.hasPermission("resourcemadness.info.reward")) line+=Text.getLabel("cmd.info.reward")+"/";
			if(rmp.hasPermission("resourcemadness.info.tools")) line+=Text.getLabel("cmd.info.tools")+"/";
			line = TextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(Text.getLabelArgs("help.info", line, line));
			
			line ="";
			if(rmp.hasPermission("resourcemadness.info.settings.reset")) line+=Text.getLabel("cmd.settings.reset");
			if(rmp.hasPermission("resourcemadness.info.settings")) rmp.sendMessage(Text.getLabelArgs("help.settings", line, (line.length()>0?"/"+line:"")));
			if(rmp.hasPermission("resourcemadness.set")) rmp.sendMessage(Text.getLabel("help.set"));
			
			//Mode
			line="";
			if(rmp.hasPermission("resourcemadness.mode.filter")) line+=Text.getLabel("cmd.mode.filter")+"/";
			if(rmp.hasPermission("resourcemadness.mode.reward")) line+=Text.getLabel("cmd.mode.reward")+"/";
			if(rmp.hasPermission("resourcemadness.mode.tools")) line+=Text.getLabel("cmd.mode.tools")+"/";
			line = TextHelper.stripLast(line, "/");
			if(line.length()!=0) if(rmp.hasPermission("resourcemadness.mode")) rmp.sendMessage(Text.getLabelArgs("help.mode", line, TextHelper.firstLetterToUpperCase(line)));
			
			if(rmp.hasPermission("resourcemadness.filter")) rmp.sendMessage(Text.getLabel("help.filter"));
			if(rmp.hasPermission("resourcemadness.reward")) rmp.sendMessage(Text.getLabel("help.reward"));
			if(rmp.hasPermission("resourcemadness.tools")) rmp.sendMessage(Text.getLabel("help.tools"));
			
			line="";
			if(rmp.hasPermission("resourcemadness.template.list")) line+=Text.getLabel("cmd.template.list")+"/";
			if(rmp.hasPermission("resourcemadness.template.load")) line+=Text.getLabel("cmd.template.load")+"/";
			if(rmp.hasPermission("resourcemadness.template.save")) line+=Text.getLabel("cmd.template.save")+"/";
			if(rmp.hasPermission("resourcemadness.template.remove")) line+=Text.getLabel("cmd.template.remove")+"/";
			line = TextHelper.stripLast(line, "/");
			if(rmp.hasPermission("resourcemadness.template")) rmp.sendMessage(Text.getLabelArgs("help.template", line, TextHelper.firstLetterToUpperCase(line)));
			
			if(rmp.hasPermission("resourcemadness.kick")) rmp.sendMessage(Text.getLabel("help.kick"));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.ban")) line+=Text.getLabel("cmd.ban")+"/";
			if(rmp.hasPermission("resourcemadness.unban")) line+=Text.getLabel("cmd.unban")+"/";
			line = TextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(Text.getLabelArgs("help.ban", line, TextHelper.firstLetterToUpperCase(line)));
		}
		else if(page==2){
			if(rmp.hasPermission("resourcemadness.start")) rmp.sendMessage(Text.getLabel("help.start"));
			
			//Restart/Stop
			String line="";
			//if(rmp.hasPermission("resourcemadness.restart")) line+="Restart/";
			if(rmp.hasPermission("resourcemadness.stop")) line+=Text.getLabel("cmd.stop")+"/";
			line = TextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(Text.getLabelArgs("help.stop", line, TextHelper.firstLetterToUpperCase(line)));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.pause")) line+=Text.getLabel("cmd.pause")+"/";
			if(rmp.hasPermission("resourcemadness.resume")) line+=Text.getLabel("cmd.resume")+"/";
			line = TextHelper.stripLast(line, "/");
			if(line.length()!=0) rmp.sendMessage(Text.getLabelArgs("help.pause", line, TextHelper.firstLetterToUpperCase(line)));
			
			if(rmp.hasPermission("resourcemadness.restore")) rmp.sendMessage(Text.getLabel("help.restore"));
			if(rmp.hasPermission("resourcemadness.join")) rmp.sendMessage(Text.getLabel("help.join"));
			if(rmp.hasPermission("resourcemadness.quit")) rmp.sendMessage(Text.getLabel("help.quit"));
			if(rmp.hasPermission("resourcemadness.ready")) rmp.sendMessage(Text.getLabel("help.ready"));
			if(rmp.hasPermission("resourcemadness.return")) rmp.sendMessage(Text.getLabel("help.return"));
			if(rmp.hasPermission("resourcemadness.stats")) rmp.sendMessage(Text.getLabel("help.stats"));
			if(rmp.hasPermission("resourcemadness.team")) rmp.sendMessage(Text.getLabel("help.team"));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.chat.world")) line+=Text.getLabel("cmd.chat.world")+"/";
			if(rmp.hasPermission("resourcemadness.chat.game")) line+=Text.getLabel("cmd.chat.game")+"/";
			if(rmp.hasPermission("resourcemadness.chat.team")) line+=Text.getLabel("cmd.chat.team")+"/";
			line = TextHelper.stripLast(line, "/");
			if(line.length()!=0) if(rmp.hasPermission("resourcemadness.chat")) rmp.sendMessage(Text.getLabelArgs("help.chat", line, TextHelper.firstLetterToUpperCase(line)));
			
			if(rmp.hasPermission("resourcemadness.time")) rmp.sendMessage(Text.getLabel("help.time"));
			if(rmp.hasPermission("resourcemadness.items")) rmp.sendMessage(Text.getLabel("help.items"));
			if(rmp.hasPermission("resourcemadness.item")) rmp.sendMessage(Text.getLabel("help.item"));
			
			line = "";
			if(rmp.hasPermission("resourcemadness.claim.found")) line+=Text.getLabel("cmd.claim.found")+"/";
			if(rmp.hasPermission("resourcemadness.claim.items")) line+=Text.getLabel("cmd.claim.items")+"/";
			if(rmp.hasPermission("resourcemadness.claim.reward")) line+=Text.getLabel("cmd.claim.reward")+"/";
			if(rmp.hasPermission("resourcemadness.claim.tools")) line+=Text.getLabel("cmd.claim.tools")+"/";
			line = TextHelper.stripLast(line, "/");
			if(rmp.hasPermission("resourcemadness.claim")) rmp.sendMessage(Text.getLabelArgs("help.claim", line, line));
		}
	}
	
	public void rmSetInfo(GamePlayer rmp, int page){
		if(rmp.hasPermission("resourcemadness.set")){
			Debug.warning("Setting.values().length: "+Setting.values().length);
			Debug.warning("Setting.values().length/14 - "+Setting.values().length/(double)14);
			Debug.warning("Math.ceil(Setting.values().length/14) - "+Math.ceil(Setting.values().length/(double)14));
			Debug.warning("(int)Math.ceil(Setting.values().length/14) - "+((int)Math.ceil(Setting.values().length/(double)14)));
			
			int size = 14;
			int pages = Setting.calculatePages(size);
			if(page<=0) page = 1;
			if(page>pages) page = pages;
			rmp.sendMessage(Text.getLabelArgs("help_set", ""+page, ""+pages));
			
			Setting[] settings = Setting.values();
			SettingLibrary settingLib = config.getSettingLibrary();

			int end = size*(page);
			int i = size*(page-1);
			
			while((i<end)&&(i<settings.length)){
				Setting set = settings[i];
				SettingPrototype s = settingLib.get(set);
				if(rmp.hasPermission("resourcemadness.set."+s.name())) if(!s.isLocked()) rmp.sendMessage(Text.getLabelArgs("help_set."+s.name(), Text.getLabel("cmd.set."+s.name()), Text.getLabel("setting."+s.name())));
				i++;
			}
		}
	}
	
	public void rmFilterInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.filter")){
			rmp.sendMessage(Text.getLabel("help_filter"));
			if(rmp.hasPermission("resourcemadness.filter.set")) rmp.sendMessage(Text.getLabel("help_filter.set"));
			if(rmp.hasPermission("resourcemadness.filter.random")) rmp.sendMessage(Text.getLabel("help_filter.random"));
			if(rmp.hasPermission("resourcemadness.filter.add")) rmp.sendMessage(Text.getLabel("help_filter.add"));
			if(rmp.hasPermission("resourcemadness.filter.subtract")) rmp.sendMessage(Text.getLabel("help_filter.subtract"));
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(Text.getLabel("help_filter.clear"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.filter.info")) rmp.sendMessage(Text.getLabel("help_filter.example.info"));
			if(rmp.hasPermission("resourcemadness.filter.set")) rmp.sendMessage(Text.getLabel("help_filter.example.set"));
			if(rmp.hasPermission("resourcemadness.filter.random")) rmp.sendMessage(Text.getLabel("help_filter.example.random"));
			if(rmp.hasPermission("resourcemadness.filter.add")) rmp.sendMessage(Text.getLabel("help_filter.example.add"));
			if(rmp.hasPermission("resourcemadness.filter.subtract")) rmp.sendMessage(Text.getLabel("help_filter.example.subtract"));
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(Text.getLabel("help_filter.example.clear1"));
			if(rmp.hasPermission("resourcemadness.filter.clear")) rmp.sendMessage(Text.getLabel("help_filter.example.clear2"));
		}
	}
	
	public void rmRewardInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(Text.getLabel("help_reward"));
			if(rmp.hasPermission("resourcemadness.reward.set")) rmp.sendMessage(Text.getLabel("help_reward.set"));
			if(rmp.hasPermission("resourcemadness.reward.add")) rmp.sendMessage(Text.getLabel("help_reward.add"));
			if(rmp.hasPermission("resourcemadness.reward.subtract")) rmp.sendMessage(Text.getLabel("help_reward.subtract"));
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(Text.getLabel("help_reward.clear"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.reward.info")) rmp.sendMessage(Text.getLabel("help_reward.example.info"));
			if(rmp.hasPermission("resourcemadness.reward.set")) rmp.sendMessage(Text.getLabel("help_reward.example.set"));
			if(rmp.hasPermission("resourcemadness.reward.add")) rmp.sendMessage(Text.getLabel("help_reward.example.add"));
			if(rmp.hasPermission("resourcemadness.reward.subtract")) rmp.sendMessage(Text.getLabel("help_reward.example.subtract"));
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(Text.getLabel("help_reward.example.clear1"));
			if(rmp.hasPermission("resourcemadness.reward.clear")) rmp.sendMessage(Text.getLabel("help_reward.example.clear2"));
		}
	}
	
	public void rmToolsInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(Text.getLabel("help_tools"));
			if(rmp.hasPermission("resourcemadness.tools.set")) rmp.sendMessage(Text.getLabel("help_tools.set"));
			if(rmp.hasPermission("resourcemadness.tools.add")) rmp.sendMessage(Text.getLabel("help_tools.add"));
			if(rmp.hasPermission("resourcemadness.tools.subtract")) rmp.sendMessage(Text.getLabel("help_tools.subtract"));
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(Text.getLabel("help_tools.clear"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.tools.info")) rmp.sendMessage(Text.getLabel("help_tools.example.info"));
			if(rmp.hasPermission("resourcemadness.tools.set")) rmp.sendMessage(Text.getLabel("help_tools.example.set"));
			if(rmp.hasPermission("resourcemadness.tools.add")) rmp.sendMessage(Text.getLabel("help_tools.example.add"));
			if(rmp.hasPermission("resourcemadness.tools.subtract")) rmp.sendMessage(Text.getLabel("help_tools.example.subtract"));
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(Text.getLabel("help_tools.example.clear1"));
			if(rmp.hasPermission("resourcemadness.tools.clear")) rmp.sendMessage(Text.getLabel("help_tools.example.clear2"));
		}
	}

	public void rmTemplateInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.template")){
			rmp.sendMessage(Text.getLabel("help_template"));
			if(rmp.hasPermission("resourcemadness.template.list")) rmp.sendMessage(Text.getLabel("help_template.list"));
			if(rmp.hasPermission("resourcemadness.template.load")) rmp.sendMessage(Text.getLabel("help_template.load"));
			if(rmp.hasPermission("resourcemadness.template.save")) rmp.sendMessage(Text.getLabel("help_template.save"));
			if(rmp.hasPermission("resourcemadness.template.remove")) rmp.sendMessage(Text.getLabel("help_template.remove"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.template.list")) rmp.sendMessage(Text.getLabel("help_template.example.list"));
			if(rmp.hasPermission("resourcemadness.template.load")) rmp.sendMessage(Text.getLabel("help_template.example.load"));
			if(rmp.hasPermission("resourcemadness.template.save")) rmp.sendMessage(Text.getLabel("help_template.example.save"));
			if(rmp.hasPermission("resourcemadness.template.remove")) rmp.sendMessage(Text.getLabel("help_template.example.remove"));
		}
	}
	
	public void rmClaimInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.claim")){
			rmp.sendMessage(Text.getLabel("help_claim"));
			
			String line = "";
			if(rmp.hasPermission("resourcemadness.claim.info.found")) line+=Text.getLabel("cmd.claim.found")+"/";
			if(rmp.hasPermission("resourcemadness.claim.info.items")) line+=Text.getLabel("cmd.claim.items")+"/";
			if(rmp.hasPermission("resourcemadness.claim.info.reward")) line+=Text.getLabel("cmd.claim.reward")+"/";
			if(rmp.hasPermission("resourcemadness.claim.info.tools")) line+=Text.getLabel("cmd.claim.tools")+"/";
			line = TextHelper.stripLast(line, "/");
			if(rmp.hasPermission("resourcemadness.claim.info")) rmp.sendMessage(Text.getLabelArgs("help_claim.info", line.length()!=0?line:""));
			
			if(rmp.hasPermission("resourcemadness.claim.found")) rmp.sendMessage(Text.getLabel("help_claim.found"));
			if(rmp.hasPermission("resourcemadness.claim.items")) rmp.sendMessage(Text.getLabel("help_claim.items"));
			if(rmp.hasPermission("resourcemadness.claim.reward")) rmp.sendMessage(Text.getLabel("help_claim.reward"));
			if(rmp.hasPermission("resourcemadness.claim.tools")) rmp.sendMessage(Text.getLabel("help_claim.tools"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			
			if(rmp.hasPermission("resourcemadness.claim.found")) rmp.sendMessage(Text.getLabel("help_claim.example.found"));
			if(rmp.hasPermission("resourcemadness.claim.found.chest")) rmp.sendMessage(Text.getLabel("help_claim.example.found.chest"));
			if(rmp.hasPermission("resourcemadness.claim.items")) rmp.sendMessage(Text.getLabel("help_claim.example.items"));
			if(rmp.hasPermission("resourcemadness.claim.items.chest")) rmp.sendMessage(Text.getLabel("help_claim.example.items.chest"));
			if(rmp.hasPermission("resourcemadness.claim.reward")) rmp.sendMessage(Text.getLabel("help_claim.example.reward"));
			if(rmp.hasPermission("resourcemadness.claim.reward.chest")) rmp.sendMessage(Text.getLabel("help_claim.example.reward.chest"));
			if(rmp.hasPermission("resourcemadness.claim.tools")) rmp.sendMessage(Text.getLabel("help_claim.example.tools"));
			if(rmp.hasPermission("resourcemadness.claim.tools.chest")) rmp.sendMessage(Text.getLabel("help_claim.example.tools.chest"));
		}
	}
	
	public void rmChatInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.chat")){
			rmp.sendMessage(Text.getLabel("help_chat"));
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(Text.getLabel("help_chat.world"));
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(Text.getLabel("help_chat.game"));
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(Text.getLabel("help_chat.team"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(Text.getLabel("help_chat.example.world"));
			if(rmp.hasPermission("resourcemadness.chat.world")) rmp.sendMessage(Text.getLabel("help_chat.example.world.message"));
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(Text.getLabel("help_chat.example.game"));
			if(rmp.hasPermission("resourcemadness.chat.game")) rmp.sendMessage(Text.getLabel("help_chat.example.game.message"));
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(Text.getLabel("help_chat.example.team"));
			if(rmp.hasPermission("resourcemadness.chat.team")) rmp.sendMessage(Text.getLabel("help_chat.example.team.message"));
		}
	}
	
	public void rmItemInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.item")){
			rmp.sendMessage(Text.getLabel("help_item"));
			rmp.sendMessage(Text.getLabel("help_item.item"));
			rmp.sendMessage(Text.getLabel("common.examples"));
			rmp.sendMessage(Text.getLabel("help_item.example1"));
			rmp.sendMessage(Text.getLabel("help_item.example2"));
			rmp.sendMessage(Text.getLabel("help_item.example3"));
			rmp.sendMessage(Text.getLabel("help_item.example4"));
		}
	}
	
	public void rmKickInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.kick")){
			rmp.sendMessage(Text.getLabel("help_kick"));
			if(rmp.hasPermission("resourcemadness.kick.player")) rmp.sendMessage(Text.getLabel("help_kick.player"));
			if(rmp.hasPermission("resourcemadness.kick.team")) rmp.sendMessage(Text.getLabel("help_kick.team"));
			if(rmp.hasPermission("resourcemadness.kick.all")) rmp.sendMessage(Text.getLabel("help_kick.all"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.kick.player")) rmp.sendMessage(Text.getLabel("help_kick.example.player"));
			if(rmp.hasPermission("resourcemadness.kick.team")) rmp.sendMessage(Text.getLabel("help_kick.example.team"));
			if(rmp.hasPermission("resourcemadness.kick.all")) rmp.sendMessage(Text.getLabel("help_kick.example.all"));
		}
	}
	
	public void rmBanInfo(GamePlayer rmp){
		
		if(rmp.hasPermission("resourcemadness.ban")){
			rmp.sendMessage(Text.getLabel("help_ban"));
			if(rmp.hasPermission("resourcemadness.ban.player")) rmp.sendMessage(Text.getLabel("help_ban.player"));
			if(rmp.hasPermission("resourcemadness.ban.team")) rmp.sendMessage(Text.getLabel("help_ban.team"));
			if(rmp.hasPermission("resourcemadness.ban.all")) rmp.sendMessage(Text.getLabel("help_ban.all"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.ban.player")) rmp.sendMessage(Text.getLabel("help_ban.example.player"));
			if(rmp.hasPermission("resourcemadness.ban.team")) rmp.sendMessage(Text.getLabel("help_ban.example.team"));
			if(rmp.hasPermission("resourcemadness.ban.all")) rmp.sendMessage(Text.getLabel("help_ban.example.all"));
		}
	}
	
	public void rmUnbanInfo(GamePlayer rmp){
		if(rmp.hasPermission("resourcemadness.unban")){
			rmp.sendMessage(Text.getLabel("help_unban"));
			if(rmp.hasPermission("resourcemadness.unban.player")) rmp.sendMessage(Text.getLabel("help_unban.player"));
			rmp.sendMessage(Text.getLabel("common.examples")+":");
			if(rmp.hasPermission("resourcemadness.unban.player")) rmp.sendMessage(Text.getLabel("help_unban.example.player"));
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