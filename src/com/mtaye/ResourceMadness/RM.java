package com.mtaye.ResourceMadness;

import java.io.File;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.io.*;

import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.RMState;
import com.mtaye.ResourceMadness.RMGame.ForceState;
import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 * DropChest for Bukkit
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

	private RMBlockListener blockListener = new RMBlockListener(this);
	private RMPlayerListener playerListener = new RMPlayerListener(this);
	
	private RMWatcher watcher;
	private int watcherid;
	//private RMInventoryListener inventoryListener = new RMPlayerListener(this);
	
	public RM(){
		RMPlayer.plugin = this;
		RMGame.plugin = this;
	}

	public void onEnable(){
		log = getServer().getLogger();
		
		watcher = new RMWatcher(this);
		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 5,5);
	
		//setupPermissions();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);

		pdfFile = this.getDescription();
		log.log(Level.INFO, pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled!" );
		//RMConfig.load();
	}
	
	public void onDisable(){
		getServer().getScheduler().cancelTask(watcherid);
		log.info(pdfFile.getName() + " disabled");
		//RMConfig.save();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
		Player p = null;
		if(sender.getClass().getName().contains("Player")){
			p = (Player)sender;
			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(cmd.getName().equals("resourcemadness")){
					boolean syntaxError=false;
					if(args.length==0){
						syntaxError(rmp);
					}
					else{
						if(args[0].equalsIgnoreCase("examples")){
							showExamples(rmp);
							return true;
						}
						//ADD
						else if(args[0].equalsIgnoreCase("add")){
							rmp.setPlayerAction(PlayerAction.ADD);
							rmp.sendMessage("Left click a game block to create your new game.");
						}
						//REMOVE
						else if(args[0].equalsIgnoreCase("remove")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									RMGame.tryRemoveGame(rmGame, rmp);
									return true;
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.REMOVE);
								rmp.sendMessage("Left click a game block to remove your game.");
								return true;
							}
						}
						//LIST
						else if(args[0].equalsIgnoreCase("list")){
							if(args.length==2){
								sendListById(args[1], rmp);
								return true;
							}
							else{
								sendListById("0", rmp);
								return true;
							}
						}
						//INFO
						else if(args[0].equalsIgnoreCase("info")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									rmGame.sendInfo(rmp);
									return true;
								}
							}
							if(args.length==1){
								rmp.setPlayerAction(PlayerAction.INFO);
								rmp.sendMessage("Left click a game block to get info.");
								return true;
							}
						}
						//JOIN
						else if(args[0].equalsIgnoreCase("join")){
							if(args.length==3){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									for(RMTeam rmTeam : rmGame.getTeams()){
										if(rmTeam!=null){
											if(args[2].equalsIgnoreCase(rmTeam.getTeamColor().name())){
												rmGame.joinTeam(rmTeam, rmp);
												return true;
											}
										}
									}
									RMTeam rmTeam = getTeamById(args[2], rmGame);
									if(rmTeam!=null){
										rmGame.joinTeam(rmTeam, rmp);
										return true;
									}
									
									//RMTeam rmTeam = getTeamById(args[2], rmGame);
									//if(rmTeam!=null) rmGame.joinTeam(rmTeam, rmp);
									//else syntaxError(rmp);
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.JOIN);
								rmp.sendMessage("Left click a game block to join the team.");
								return true;
							}
						}
						//QUIT
						else if(args[0].equalsIgnoreCase("quit")){
							for(RMTeam rmTeam : RMTeam.getTeams()){
								RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
								if(rmPlayer!=null){
									rmTeam.removePlayer(rmPlayer);
									return true;
								}
							}
							rmp.sendMessage("You did not yet join any team.");
							return true;
							
						}
						//START
						else if(args[0].equalsIgnoreCase("start")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									rmGame.startGame(rmp);
									return true;
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.START);
								rmp.sendMessage("Left click a game block to start the game.");
								return true;
							}
						}
						//RESTART
						else if(args[0].equalsIgnoreCase("restart")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									rmGame.restartGame(rmp);
									return true;
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.RESTART);
								rmp.sendMessage("Left click a game block to restart the game.");
								return true;
							}
						}
						//STOP
						else if(args[0].equalsIgnoreCase("stop")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									rmGame.stopGame(rmp);
									return true;
								}
							}
							else{
								rmp.setPlayerAction(PlayerAction.STOP);
								rmp.sendMessage("Left click a game block to stop the game.");
								return true;
							}
						}
						//MAX PLAYERS
						else if(args[0].equalsIgnoreCase("maxplayers")){
							if(args.length==3){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									int amount = getIntByString(args[2]);
									if(amount>-1){
										rmGame.setMaxPlayers(rmp, amount);
										return true;
									}
								}
							}
							if(args.length==2){
								rmp.setRequestInt(getIntByString(args[1]));
								rmp.setPlayerAction(PlayerAction.MAX_PLAYERS);
								rmp.sendMessage("Left click a game block to set max players.");
								return true;
							}
						}
						//MAX TEAM PLAYERS
						else if(args[0].equalsIgnoreCase("maxteamplayers")){
							if(args.length==3){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									int amount = getIntByString(args[2]);
									if(amount>-1){
										rmGame.setMaxTeamPlayers(rmp, amount);
										return true;
									}
								}
							}
							if(args.length==2){
								rmp.setRequestInt(getIntByString(args[1]));
								rmp.setPlayerAction(PlayerAction.MAX_TEAM_PLAYERS);
								rmp.sendMessage("Left click a game block to set max team players.");
								return true;
							}
						}
						//MAX ITEMS
						else if(args[0].equalsIgnoreCase("maxitems")){
							if(args.length==3){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									int amount = getIntByString(args[2]);
									if(amount>-1){									
										rmGame.setMaxItems(rmp, amount);
										return true;
									}
								}
							}
							if(args.length==2){
								rmp.setRequestInt(getIntByString(args[1]));
								rmp.setPlayerAction(PlayerAction.MAX_ITEMS);
								rmp.sendMessage("Left click a game block to set max items.");
								return true;
							}
						}
						//RESTORE WORLD
						else if(args[0].equalsIgnoreCase("restore")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									rmGame.restoreWorld(rmp);
									return true;
								}
							}
							if(args.length==1){
								rmp.setPlayerAction(PlayerAction.RESTORE_WORLD);
								rmp.sendMessage("Left click a game block to restore world changes.");
								return true;
							}
						}
						//AUTO RESTORE WORLD
						else if(args[0].equalsIgnoreCase("autorestore")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									rmGame.toggleAutoRestoreWorld(rmp);
									return true;
								}
							}
							if(args.length==1){
								rmp.setPlayerAction(PlayerAction.AUTO_RESTORE_WORLD);
								rmp.sendMessage("Left click a game block to toggle auto restore world.");
								return true;
							}
						}
						//WARN HACK ITEMS
						else if(args[0].equalsIgnoreCase("warnhackeditems")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									rmGame.toggleWarnHackedItems(rmp);
									return true;
								}
							}
							if(args.length==1){
								rmp.setPlayerAction(PlayerAction.WARN_HACKED_ITEMS);
								rmp.sendMessage("Left click a game block to toggle auto restore world.");
								return true;
							}
						}
						//ALLOW HACK ITEMS
						else if(args[0].equalsIgnoreCase("allowhackeditems")){
							if(args.length==2){
								RMGame rmGame = getGameById(args[1]);
								if(rmGame!=null){
									rmGame.toggleAllowHackedItems(rmp);
									return true;
								}
							}
							if(args.length==1){
								rmp.setPlayerAction(PlayerAction.ALLOW_HACKED_ITEMS);
								rmp.sendMessage("Left click a game block to toggle auto restore world.");
								return true;
							}
						}
						//ITEMS
						else if(args[0].equalsIgnoreCase("items")){
							RMTeam rmTeam = rmp.getTeam();
							if(rmTeam!=null){
								RMGame rmGame = rmTeam.getGame();
								if(rmGame!=null){
									if(rmGame.getState()==RMState.GAMEPLAY){
										rmGame.updateGameplayInfo(rmp);
										return true;
									}
								}
							}
							rmp.sendMessage("You must be in a game to use this command.");
							return false;
						}
						//FILTER
						else if(args[0].equalsIgnoreCase("filter")){
							if(args.length>1){
								List<String> listArgs = new ArrayList<String>();
								for(int i=1; i<args.length; i++){
									listArgs.add(args[i]);
								}
								if(listArgs.size()>0){
									RMGame rmGame = getGameById(args[1]);
									if(rmGame!=null){
										parseFilter(rmp, listArgs.subList(1, listArgs.size()), false);
										rmGame.tryParseFilter(rmp);
										return true;
									}
									else{
										parseFilter(rmp, listArgs, true);
										return true;
									}
								}
							}
						}
						else{
							List<String> items = new ArrayList<String>();
							for(String str : args){
								String[] strItems = str.split(",");
								for(String strItem : strItems){
									for(Material mat : Material.values()){
										if(strItem.equalsIgnoreCase(mat.name())){
											if(!items.contains(mat))items.add(ChatColor.WHITE+mat.name()+":"+ChatColor.YELLOW+mat.getId());
										}
									}
									if(strItem.contains("-")){
										String[] strItems2 = strItem.split("-");
										int id1=getIntByString(strItems2[0]);
										int id2=getIntByString(strItems2[1]);
										if((id1!=-1)&&(id2!=-1)){
											if(id1>id2){
												int id3=id1;
												id1=id2;
												id2=id3;
											}
											while(id1<=id2){
												Material mat = Material.getMaterial(id1);
												if(mat!=null){
													if(!items.contains(mat))items.add(""+ChatColor.WHITE+id1+":"+ChatColor.YELLOW+Material.getMaterial(id1).name());
												}
												id1++;
											}
										}
									}
									else{
										int id = getIntByString(strItem);
										if(id!=-1){
											Material mat = Material.getMaterial(id);
											if(mat!=null) if(!items.contains(mat)){
												items.add(""+ChatColor.WHITE+id+":"+ChatColor.YELLOW+Material.getMaterial(id).name());
											}
										}
									}
								}
							}
							if(items.size()>0){
								rmp.sendMessage(getFormattedStringByList(items));
								return true;
							}
							syntaxError(rmp);
						}
					}
				}
			}
		}
		return true;
	}
	
	public void parseFilter(RMPlayer rmp, List<String> args, Boolean viaPlayer){
		int size = 0;
		List<Integer> items = new ArrayList<Integer>();
		List<Integer[]> amount = new ArrayList<Integer[]>();
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
			if(args.get(0).equalsIgnoreCase("remove")){
				force = ForceState.REMOVE;
				size+=1;
			}
			if(args.get(0).equalsIgnoreCase("random")){
				randomize = getIntByString(args.get(1));
				if(randomize>0){
					force = ForceState.RANDOMIZE;
					size+=2;
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

			if((type!=null)&&(type!=FilterType.CLEAR)){
				boolean useDefaultAmount = false;
				items = getItemsFromFilter(type);
				amount.clear();
				if(arg0.contains("stack")) useDefaultAmount = true;
				else if(arg0.contains(":")){
					List<String> strArgs = splitArgs(arg0);
					String strAmount = ""; 
					String[] strSplit = strArgs.get(0).split(":");
					if(strSplit.length>1){
						strAmount = strSplit[1];
						Integer[] intAmount = checkInt(strAmount);
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
			}
			else{
				for(String arg : args){
					List<String> strArgs = splitArgs(arg);
					for(String strArg : strArgs){
						String strAmount = "";
						String[] strSplit = strArg.split(":");
						String[] strItems = strSplit[0].split(",");
						Integer[] intAmount = null;
						if(strSplit.length>1){
							strAmount = strSplit[1];
							intAmount = checkInt(strAmount);
						}
						for(String str : strItems){
							if(str.contains("-")){
								String[] strItems2 = str.split("-");
								int id1=getIntByString(strItems2[0]);
								int id2=getIntByString(strItems2[1]);
								if((id1!=-1)&&(id2!=-1)){
									if(id1>id2){
										int id3=id1;
										id1=id2;
										id2=id3;
									}
									while(id1<=id2){
										Material mat = Material.getMaterial(id1);
										if(mat!=null){
											if(intAmount==null){
												intAmount = new Integer[1];
												if(strArg.contains("stack")){
													intAmount[0] = mat.getMaxStackSize();
												}
												else intAmount[0] = 1;
											}
											items.add(mat.getId());
											amount.add(intAmount);
										}
										id1++;
									}
								}
							}
							else{
								int id=getIntByString(str);
								if(id!=-1){
									Material mat = Material.getMaterial(id);
									if(mat!=null){
										if(intAmount==null){
											intAmount = new Integer[1];
											if(strArg.contains("stack")){
												intAmount[0] = mat.getMaxStackSize();
											}
											else intAmount[0] = 1;
										}
										items.add(mat.getId());
										amount.add(intAmount);
									}
								}
							}
						}
					}
				}
			}
			if((type==null)&&(items.size()==0)){
				syntaxError(rmp);
				return;
			}
			//HashMap<Integer, Integer[]> hashItems = new HashMap<Integer, Integer[]>();
			HashMap<Integer, RMItem> rmItems = new HashMap<Integer, RMItem>();
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
			//rmp.setRequestFilter(hashItems, type, force);
			rmp.setRequestFilter(rmItems, type, force, randomize);
			if(viaPlayer){
				rmp.setPlayerAction(PlayerAction.FILTER);
				rmp.sendMessage("Left click a game block to modify the filter.");
			}
		}
	}
	
	public Integer[] checkInt(String arg){
		List<Integer> values = new ArrayList<Integer>();
		
		if(arg.contains("-")){
			String[] split = arg.split("-");
			int val1 = 0;
			int val2 = 0;
			if(split.length>0) val1 = getIntByString(split[0]);
			if(split.length>1) val2 = getIntByString(split[1]);
			if(val1>0) values.add(val1);
			if(val2>0) values.add(val2);
		}
		else{
			int val = getIntByString(arg);
			if(val>=0) values.add(val);
		}
		if(values.size()==0) return null;
		
		return values.toArray(new Integer[values.size()]);
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
	
	public List<String> splitArgs(String listArg){
		List<String> args = new ArrayList<String>();
		//getServer().broadcastMessage("listArg:"+listArg);
		if(listArg.contains(":")){
			int pos = 0;
			int posEnd = 0;
			while(pos!=-1){
				posEnd = listArg.indexOf(":",pos);
				if(posEnd!=-1) posEnd = listArg.indexOf(",",posEnd);
				if(posEnd!=-1){
					//getServer().broadcastMessage("add:"+listArg.substring(pos,posEnd));
					args.add(listArg.substring(pos,posEnd));
					pos = posEnd+1;
				}
				else{
					//getServer().broadcastMessage("add:"+listArg.substring(pos));
					args.add(listArg.substring(pos));
					pos = -1;
				}
			}
			return args;
		}
		else return Arrays.asList(listArg);
	}
	
	public void sendListById(String arg, RMPlayer rmp){
		int id = getIntByString(arg, 0);
		List<RMGame> rmGames = RMGame.getGames();
		if(rmGames.size()==0){
			rmp.sendMessage("No games yet");
			return;
		}
		if(id<0) id=0;
		int i=id*10;
		if(rmGames.size()>0) rmp.sendMessage("Page "+id+" of " +(int)(rmGames.size()/5));
		while(i<rmGames.size()){
			RMGame rmGame = rmGames.get(i);
			rmp.sendMessage("Game: "+ChatColor.YELLOW+rmGame.getId()+ChatColor.WHITE+" - "+"Owner: "+ChatColor.YELLOW+rmGame.getOwnerName()+ChatColor.WHITE+" Teams: "+rmGame.getTeamsPlayers());
			if(i==id*10+10) break;
			i++;
		}
	}
	
	public RMGame getGameById(String arg){
		return RMGame.getGame(getIntByString(arg));
	}
	public RMTeam getTeamById(String arg, RMGame rmGame){
		return rmGame.getTeam(getIntByString(arg));
	}
	
	public int getIntByString(String arg){
		int i = 0;
		try{
			i = Integer.valueOf(arg);
			return i;
		} catch(Exception e){
			return -1;
		}
	}
	public int getIntByString(String arg, int def){
		int i = 0;
		try{
			i = Integer.valueOf(arg);
			return i;
		} catch(Exception e){
			return def;
		}
	}
	
	public ChatColor getChatColorByDye(DyeColor dye){
		switch(dye){
			case WHITE:
				return ChatColor.WHITE;
			case ORANGE:
				return ChatColor.GOLD;
			case MAGENTA:
				return ChatColor.LIGHT_PURPLE;
			case LIGHT_BLUE:
				return ChatColor.BLUE;
			case YELLOW:
				return ChatColor.YELLOW;
			case LIME:
				return ChatColor.GREEN;
			case PINK:
				return ChatColor.RED;
			case GRAY:
				return ChatColor.DARK_GRAY;
			case SILVER:
				return ChatColor.GRAY;
			case CYAN:
				return ChatColor.DARK_AQUA;
			case PURPLE:
				return ChatColor.DARK_PURPLE;
			case BLUE:
				return ChatColor.DARK_BLUE;
			case BROWN:
				return ChatColor.GOLD;
			case GREEN:
				return ChatColor.DARK_GREEN;
			case RED:
				return ChatColor.DARK_RED;
			case BLACK:
				return ChatColor.BLACK;
		}
		return ChatColor.WHITE;
	}
	
	public void syntaxError(RMPlayer rmp){
		rmp.sendMessage("ResourceMadness Commands: "+ChatColor.GRAY+"(Gray colored text is optional)");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"add "+ChatColor.WHITE+"Create a new game.");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"remove "+ChatColor.GRAY+"[id] "+ChatColor.WHITE+"Remove an existing game");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"list "+ChatColor.GRAY+"[page=0] "+ChatColor.WHITE+"List of games");
		rmp.sendMessage(" /rm "+ChatColor.AQUA+"[items(id/name)] "+ChatColor.WHITE+"Get the item's name or id");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"join "+ChatColor.GRAY+"[id] "+ChatColor.AQUA+"[team(id/color)] "+ChatColor.WHITE+"Join a team");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"quit "+ChatColor.WHITE+"Quit a team");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"start "+ChatColor.GRAY+"[id] "+ChatColor.WHITE+"Start the game");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"restart "+ChatColor.GRAY+"[id] "+ChatColor.WHITE+"Restart the game");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"stop "+ChatColor.GRAY+"[id] "+ChatColor.WHITE+"Stop the game");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"items "+ChatColor.WHITE+"Get items left to find. (ingame)");
		rmp.sendMessage("Filter: ");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"filter "+ChatColor.GRAY+"[id] "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"filter "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"remove "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"filter "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"random "+ChatColor.GREEN+"[amount] "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
	}
	
	public void showExamples(RMPlayer rmp){
		rmp.sendMessage("Examples:");
		rmp.sendMessage(" /rm filter "+ChatColor.YELLOW+"clear");
		rmp.sendMessage(" /rm filter "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":stack "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32");
		rmp.sendMessage(" /rm filter "+ChatColor.YELLOW+"remove "+ChatColor.AQUA+"1-10,20,288");
		rmp.sendMessage(" /rm filter "+ChatColor.YELLOW+"random "+ChatColor.GREEN+"20 "+ChatColor.AQUA+"all"+ChatColor.BLUE+":100-200");
	}
	
	public String stripLast(String str, String s){
		int pos = str.lastIndexOf(s);
		if(pos!=-1){
			String part1 = str.substring(0, pos);
			String part2 = str.substring(pos+s.length());
			return part1+part2;
		}
		return str;
	}
	
	public String getFormattedStringByList(List<String> strList){
		String line = "";
		for(String str : strList){
			line+=str+ChatColor.WHITE+", ";
		}
		line = stripLast(line, ",");
		return line;
	}
	public String getFormattedStringByListMaterial(List<Material> materials){
		String line = "";
		for(Material mat : materials){
			line+=mat.name()+", ";
		}
		line = stripLast(line, ",");
		return line;
	}
}