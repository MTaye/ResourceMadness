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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.io.*;

import com.mtaye.ResourceMadness.RMGame.FilterType;
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
		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 10,10);
	
		//setupPermissions();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);

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
			
			if(cmd.getName().equals("resourcemadness")){
				boolean syntaxError=false;
				if(args.length==0){
					syntaxError(rmp);
				}
				else{
					//ADD
					if(args[0].equalsIgnoreCase("add")){
						rmp.setPlayerAction(PlayerAction.ADD);
						rmp.sendMessage("Left click a game block to create your new game.");
					}
					//REMOVE
					else if(args[0].equalsIgnoreCase("remove")){
						if(args.length==2){
							RMGame rmGame = getGameById(args[1]);
							if(rmGame!=null){
								RMGame.tryRemoveGame(rmGame, rmp);
							}
							else syntaxError(rmp);
						}
						else{
							rmp.setPlayerAction(PlayerAction.REMOVE);
							rmp.sendMessage("Left click a game block to remove your game.");
						}
					}
					//LIST
					else if(args[0].equalsIgnoreCase("list")){
						if(args.length==2){
							sendListById(args[1], rmp);
						}
						else sendListById("0", rmp);
					}
					//INFO
					else if(args[0].equalsIgnoreCase("info")){
					}
					//JOIN
					else if(args[0].equalsIgnoreCase("join")){
						if(args.length==3){
							RMGame rmGame = getGameById(args[1]);
							if(rmGame!=null){
								RMTeam rmTeam = getTeamById(args[2], rmGame);
								if(rmTeam!=null) rmGame.joinTeam(rmTeam, rmp);
								else syntaxError(rmp);
							}
							else syntaxError(rmp);
						}
						else{
							rmp.setPlayerAction(PlayerAction.JOIN);
							rmp.sendMessage("Left click a chest, sign or wool block to join the team.");
						}
					}
					//QUIT
					else if(args[0].equalsIgnoreCase("quit")){
						if(args.length==2){
							RMGame rmGame = getGameById(args[1]);
							if(rmGame!=null){
								rmGame.quitTeam(rmGame, rmp);
							}
							else syntaxError(rmp);
						}
						else{
							rmp.setPlayerAction(PlayerAction.QUIT);
							rmp.sendMessage("Left click a chest, sign or wool block to quit the team.");
						}
					}
					//START
					else if(args[0].equalsIgnoreCase("start")){
						if(args.length==2){
							RMGame rmGame = getGameById(args[1]);
							if(rmGame!=null){
								rmGame.startGame(rmp);
							}
							else syntaxError(rmp);
						}
						else{
							rmp.setPlayerAction(PlayerAction.START);
							rmp.sendMessage("Left click a chest, sign or wool block to start the game.");
						}
					}
					//RESTART
					else if(args[0].equalsIgnoreCase("restart")){
						if(args.length==2){
							RMGame rmGame = getGameById(args[1]);
							if(rmGame!=null){
								rmGame.restartGame(rmp);
							}
							else syntaxError(rmp);
						}
						else{
							rmp.setPlayerAction(PlayerAction.RESTART);
							rmp.sendMessage("Left click a chest, sign or wool block to restart the game.");
						}
					}
					//STOP
					else if(args[0].equalsIgnoreCase("stop")){
						if(args.length==2){
							RMGame rmGame = getGameById(args[1]);
							if(rmGame!=null){
								rmGame.stopGame(rmp);
							}
							else syntaxError(rmp);
						}
						else{
							rmp.setPlayerAction(PlayerAction.STOP);
							rmp.sendMessage("Left click a chest, sign or wool block to stop the game.");
						}
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
								}
								else{
									parseFilter(rmp, listArgs, true);
								}
							}
							else syntaxError(rmp);
						}
						else syntaxError(rmp);
					}
					else syntaxError(rmp);
				}
			}
		}
		return true;
	}
	
	public void parseFilter(RMPlayer rmp, List<String> args, Boolean viaPlayer){
		int size = args.size()-1;
		List<Material> items = new ArrayList<Material>();
		FilterType type = null;
		Boolean force = null;
		if(args.size()==2){
			if(args.get(0).equalsIgnoreCase("add")) force = true;
			else if(args.get(0).equalsIgnoreCase("remove")) force = false;
			else{
				syntaxError(rmp);
				return;
			}
		}
		if(args.size()>0){
			if(args.get(size).equalsIgnoreCase("all")) type = FilterType.ALL;
			else if(args.get(size).equalsIgnoreCase("clear")) type = FilterType.CLEAR;
			else if(args.get(size).equalsIgnoreCase("block")) type = FilterType.BLOCK;
			else if(args.get(size).equalsIgnoreCase("item")) type = FilterType.ITEM;
			else if(args.get(size).equalsIgnoreCase("raw")) type = FilterType.RAW;
			else if(args.get(size).equalsIgnoreCase("crafted")) type = FilterType.CRAFTED;
			else{
				String[] strItems = args.get(size).split(",");
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
									items.add(mat);
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
								items.add(mat);
							}
						}
					}
				}
				if(items.size()==0){
					syntaxError(rmp);
					return;
				}
			}
			Material[] matItems = items.toArray(new Material[items.size()]);
			rmp.setRequestFilter(matItems, type, force);
			if(viaPlayer){
				rmp.setPlayerAction(PlayerAction.FILTER);
				rmp.sendMessage("Left click a chest, sign or wool block to modify the filter.");
			}
		}
	}
	
	public void sendListById(String arg, RMPlayer rmp){
		int id = getIntByString(arg, 0);
		String list = "";
		List<RMGame> rmGames = RMGame.getGames();
		if(rmGames.size()==0){
			rmp.sendMessage("No games yet");
			return;
		}
		if(id<0) id=0;
		int i=id*10;
		rmp.sendMessage("Page "+id+" of " +(int)(rmGames.size()/5));
		while(i<rmGames.size()){
			RMGame rmGame = rmGames.get(i);
			rmp.sendMessage("Game "+ChatColor.YELLOW+rmGame.getId()+ChatColor.WHITE+" - "+ChatColor.GRAY+"Owner:"+rmGame.getOwnerName()+ChatColor.WHITE+" Teams:"+rmGame.getTeamsPlayers());
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
		rmp.sendMessage("ResourceMadness Commands:");
		rmp.sendMessage(ChatColor.GRAY+"(Gray colored text is optional)");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"add "+ChatColor.GRAY+"Create a new game.");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"remove "+ChatColor.DARK_PURPLE+"[game] "+ChatColor.GRAY+"Remove an existing game");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"list "+ChatColor.GRAY+"[page=1] "+ChatColor.WHITE+"List of games");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"join "+ChatColor.GRAY+"[page=1] "+ChatColor.WHITE+"Join a team");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"quit "+ChatColor.GRAY+"[page=1] "+ChatColor.WHITE+"Quit a team");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"start "+ChatColor.GRAY+"[gameid] "+ChatColor.WHITE+"Start the game");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"restart "+ChatColor.GRAY+"[gameid] "+ChatColor.WHITE+"Restart the game");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"stop "+ChatColor.GRAY+"[gameid] "+ChatColor.WHITE+"Stop the game");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"filter "+ChatColor.GRAY+"[gameid] "+ChatColor.BLUE+"[items(id)/all/clear] "+ChatColor.WHITE+"Modify the item filter");
		rmp.sendMessage(" /rm "+ChatColor.YELLOW+"filter "+ChatColor.GRAY+"[gameid] "+ChatColor.LIGHT_PURPLE+"[add/remove] "+ChatColor.BLUE+"[items(id)/all/clear]"+ChatColor.WHITE+"Modify the item filter");
	}
	
	public String stripLast(String str, String s){
		if(str.charAt(str.length()-1)==s.charAt(0)){
			return str.substring(0, str.length()-1);
		}
		return str;
	}
}