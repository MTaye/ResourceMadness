package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import java.util.Hashtable;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMGame {

	private static List<RMGame> _games = new ArrayList<RMGame>();
	public static RM plugin;
	private static Material[] _materials = {Material.GLASS, Material.STONE, Material.CHEST, Material.WALL_SIGN, Material.WOOL};
	private static Material[] _hackMaterials = {
		Material.AIR, Material.GRASS, Material.BEDROCK, Material.WATER,	Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA,
		Material.COAL_ORE, Material.SPONGE, Material.LAPIS_ORE, Material.BED, Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.DOUBLE_STEP,
		Material.FIRE, Material.MOB_SPAWNER, Material.REDSTONE_WIRE, Material.DIAMOND_ORE, Material.CROPS, Material.SOIL, Material.BURNING_FURNACE,
		Material.SIGN_POST, Material.WOODEN_DOOR, Material.WALL_SIGN, Material.IRON_DOOR_BLOCK, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
		Material.REDSTONE_TORCH_OFF, Material.SNOW_BLOCK, Material.ICE, Material.SUGAR_CANE_BLOCK, Material.PORTAL, Material.CAKE_BLOCK,
		Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.LOCKED_CHEST, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
		Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS};
	private List<Material> _lastHackMaterials = new ArrayList<Material>();
	
	private static Material[] _blockItemMaterials = {Material.BED_BLOCK, Material.BROWN_MUSHROOM, Material.CACTUS, Material.CROPS, Material.DEAD_BUSH,
		Material.DETECTOR_RAIL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.IRON_DOOR_BLOCK, Material.LEVER, Material.LONG_GRASS,
		Material.POWERED_RAIL, Material.RAILS, Material.RED_MUSHROOM, Material.RED_ROSE, Material.REDSTONE, Material.REDSTONE_WIRE, Material.SAPLING,
		Material.SIGN_POST, Material.SNOW, Material.STONE_PLATE, Material.SUGAR_CANE_BLOCK, Material.TORCH, Material.WOODEN_DOOR, Material.WOOD_PLATE,
		Material.YELLOW_FLOWER, Material.LADDER, Material.PAINTING, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.TRAP_DOOR,
		Material.WALL_SIGN, Material.WEB};
	
	private RMGameConfig _config = new RMGameConfig();
	private HashMap<Location, RMBlock> _logBlockList = new HashMap<Location, RMBlock>();
	private HashMap<Location, RMBlock> _logBlockItemList = new HashMap<Location, RMBlock>();
	
	//private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	private RMState _state = RMState.SETUP;
	private InterfaceState _interfaceState = InterfaceState.MAIN;
	//private Inventory _inventory;
	
	private RMFilter _items = new RMFilter();
	private static RMPlayer _requestPlayer;
	private int _menuItems = 0;
	
	private enum Part { GLASS, STONE, CHEST, WALL_SIGN, WOOL; }
	public enum RMState { MODIFY, SETUP, COUNTDOWN, GAMEPLAY, GAMEOVER; }
	public enum InterfaceState { MAIN, FILTER_CLEAR };
	public enum FilterType { ALL, CLEAR, BLOCK, ITEM, RAW, CRAFTED};
	public enum ClickState { LEFT, RIGHT, NONE };
	public enum ItemHandleState { ADD, MODIFY, REMOVE, NONE };
	public enum HandleState { ADD, MODIFY, REMOVE, NOCHANGE, NONE };
	public enum ForceState { ADD, REMOVE, RANDOMIZE, NONE};
	
	private final int cdTimerLimit = 30; //3 seconds
	private int cdTimer = cdTimerLimit;
	
	public RMGameConfig getConfig(){
		return _config;
	}
	public void setConfig(RMGameConfig config){
		_config = config;
	}
	public void saveTemplate(){
		//plugin.saveTemaplte(_config);
	}

	private RMTeam _winningTeam;
	private RMPlayer _winningPlayer;
	
	public RMTeam getWinningTeam(){
		return _winningTeam;
	}
	public void setWinningTeam(RMTeam rmTeam){
		_winningTeam = rmTeam;
	}
	public void clearWinningTeam(){
		_winningTeam = null;
	}
	
	public RMPlayer getWinningPlayer(){
		return _winningPlayer;
	}
	public void setWinningPlayer(RMPlayer rmp){
		_winningPlayer = rmp;
	}
	public void clearWinningPlayer(){
		_winningTeam = null;
	}
	
	public static RMPlayer getRequestPlayer(){
		return _requestPlayer;
	}
	public static void setRequestPlayer(RMPlayer rmp){
		_requestPlayer = rmp;
	}
	public static void clearRequestPlayer(){
		_requestPlayer = null;
	}
	
	public RMFilter getItems(){
		return _items;
	}
	
	public void startGame(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			for(RMChest rmChest : getChests()){
				rmChest.clearItems();
			}
			_items.populateByFilter(_config.getFilter());
			if(_config.getRandomizeAmount()>0) _items.randomize(_config.getRandomizeAmount());
			else _items.randomize(_config.getRandomizeAmount());
			if(_items.size()>0){
				/*
				for(RMTeam rmTeam : _config.getTeams()){
					if(rmTeam.getPlayers().length==0){
						//rmp.sendMessage("Each team must have at least one player.");
						broadcastMessage("Each team must have at least one player.");
						return;
					}
				}
				*/
				//rmp.sendMessage("Starting game...");
				broadcastMessage("Starting game...");
				setState(RMState.COUNTDOWN);
			}
			else rmp.sendMessage("Configure the "+ChatColor.YELLOW+"filtered items"+ChatColor.WHITE+" first.");
		}
		else rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can start the game.");
	}
	public void restartGame(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			if(_state == RMState.GAMEPLAY){
				//rmp.sendMessage("Restarting game...");
				broadcastMessage("Restarting game...");
				stopGame(rmp);
				startGame(rmp);
			}
			/*
			_config.clearRandomizeAmount();
			startGame(rmp);
			if(_config.getWarpToSafety()) warpPlayersToSafety();
			if(_config.getAutoRestoreWorld()) restoreLog();
			*/
		}
		else rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can restart the game.");
	}
	public void stopGame(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			//rmp.sendMessage("Stopping game...");
			broadcastMessage("Stopping game...");
			initGameOver();
		}
		else rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can stop the game.");
	}
	
	public void initGameOver(){
		_config.clearRandomizeAmount();
		for(RMChest rmChest : getChests()){
			rmChest.clearItems();
		}
		returnInventories();
		if(_config.getWarpToSafety()) warpPlayersToSafety();
		if(_config.getAutoRestoreWorld()) restoreLog();
		setState(RMState.SETUP);
		clearTeamPlayers();
		updateSigns();
	}

	//Sign info
	public void updateSigns(String... args){
		if(args==null){
			args = new String[4];
			args[0] = "";
			args[1] = "";
			args[2] = "";
			args[3] = "";
		}
		switch(_state){
			case SETUP:
				switch(_interfaceState){
				case MAIN:
					int items = _config.getFilter().size();
					int total = _config.getFilter().getItemsTotal();
					int totalHigh = _config.getFilter().getItemsTotalHigh();
					String lineTotal = ""+total;
					if(totalHigh>0) lineTotal+="-"+totalHigh;
					int length = lineTotal.length();
					if(length<9) lineTotal = "Total: "+lineTotal;
					else if(length<11) lineTotal = "Ttl: "+lineTotal;
					else if(length<13) lineTotal = "T: "+lineTotal;
					
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Filtered: "+items);
						sign.setLine(1, lineTotal);
						sign.setLine(2, "Joined: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());
						sign.setLine(3, "Total: "+getTeamPlayers().length+getTextPlayersOfMax());
						sign.update();
					}
					break;
				case FILTER_CLEAR:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Click sign to");
						sign.setLine(1, "CLEAR ALL ITEMS");
						sign.setLine(2, "or chest to");
						sign.setLine(3, "CANCEL");
						sign.update();
					}
					break;
				}
				break;
			case COUNTDOWN:
				for(Sign sign : getSigns()){
					sign.setLine(0, "");
					sign.setLine(1, "Prepare!");
					sign.setLine(2, args[0]);
					sign.setLine(3, "");
					sign.update();
				}
				break;
			case GAMEPLAY:
				for(RMTeam rmTeam : getTeams()){
					Sign sign = rmTeam.getSign();
					RMChest rmChest = rmTeam.getChest();
					sign.setLine(0, "Items Left: "+rmChest.getItemsLeftInt());
					sign.setLine(1, "Total: "+rmChest.getTotalLeft());
					sign.setLine(2, "Joined: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());
					sign.setLine(3, "Total: "+getTeamPlayers().length+getTextPlayersOfMax());
					sign.update();
				}
				break;
			case GAMEOVER:
				break;
		}
	}
	public void trySignSetupInfo(RMPlayer rmp){
		updateSigns();
		String items = "";
		
		//Sort
		Integer[] array = _config.getFilter().keySet().toArray(new Integer[_config.getFilter().keySet().size()]);
		Arrays.sort(array);
		
		if(array.length>_config.getWordLimit()){
			for(Integer i : array){
				items += ChatColor.WHITE+""+i+includeItem(_config.getFilter().getItem(i))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer i : array){
				items += ChatColor.WHITE+""+Material.getMaterial(i)+includeItem(_config.getFilter().getItem(i))+ChatColor.WHITE+", ";
			}
		}
		if(items.length()>0){
			items = items.substring(0, items.length()-2);
			rmp.sendMessage(ChatColor.YELLOW+"Filtered items: "+items);
		}
		else rmp.sendMessage(ChatColor.YELLOW+"No items in filter.");
	}
	
	public void trySignGameplayInfo(Block b, RMPlayer rmp){
		RMTeam rmTeam = getTeamByBlock(b);
		RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
		if((rmTeam!=null)&&(rmPlayer!=null)){
			updateGameplayInfo(rmp);
			updateSigns();
		}
	}
	
	public void updateGameplayInfo(RMPlayer rmp){
		RMTeam rmTeam = rmp.getTeam();
		String strItems = "";
		RMChest rmChest = rmTeam.getChest();
		
		//Sort
		Integer[] array = _items.keySet().toArray(new Integer[_items.keySet().size()]);
		Arrays.sort(array);
		
		HashMap<Integer, RMItem> items = rmChest.getItems();
		
		for(Integer i : array){
			RMItem rmItem = _items.getItem(i);
			int amount = rmItem.getAmount();
			if(items.containsKey(i)) amount -= items.get(i).getAmount();
			if(amount>0) strItems += ChatColor.WHITE+""+Material.getMaterial(i)+includeItem(new RMItem(i, amount))+ChatColor.WHITE+", ";
		}
		if(strItems.length()>0){
			strItems = strItems.substring(0, strItems.length()-2);
			rmp.sendMessage(ChatColor.YELLOW+"Items left: "+strItems);
		}
		else rmp.sendMessage(ChatColor.YELLOW+"No items left.");
	}
	
	//Get items from chests
	public List<ItemStack> getItemsFromChests(RMChest... rmChests){
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(RMChest rmChest : rmChests){
			ItemStack[] chestItems = rmChest.getChest().getInventory().getContents();
			items.addAll(Arrays.asList(chestItems));
		}
		if(_config.getAddOnlyOneStack()) items = removeDuplicates(items);
		else{
			if(_config.getAddWholeStack()) items = addDuplicates(items, true);
			else items = addDuplicates(items, false);
		}
		if(_config.getWarnHackedItems()) warnHackMaterialsListItemStack(items);
		items = removeHackMaterialsListItemStack(items);
		return items;
	}
	
	//Try Add Items
	public void tryAddItemsToFilter(Block b, RMPlayer rmp, ClickState clickState){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			List<ItemStack> items = new ArrayList<ItemStack>();
			boolean force;
			if(clickState!=ClickState.NONE){
				items = getItemsFromChests(getChestByBlock(b));
				force = true;
			}
			else{
				items = getItemsFromChests(getChests());
				force = false;
			}
			String addedItems = "";
			String modifiedItems = "";
			String removedItems = "";
			
			if(items.size()>0){
				for(int i=0; i<items.size(); i++){
					ItemStack is = items.get(i);
					if(is != null){
						Material item = is.getType();
						if(item!=null){
							if(item!=Material.AIR){
								int id = item.getId();
								RMItem rmItem = new RMItem(id);
								if(_config.getAddOnlyOneStack()) rmItem.setAmount(rmItem.getMaxStackSize());
								else rmItem.setAmount(is.getAmount());
								
								switch(clickState){
									case NONE: case LEFT:
										switch(_config.getFilter().addItem(id, rmItem, force)){
											case ADD:
												addedItems+=ChatColor.WHITE+item.name()+includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
										}
										break;
									case RIGHT:
										switch(_config.getFilter().removeItem(id, rmItem, force)){
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case REMOVE:
												removedItems+=ChatColor.WHITE+item.name()+includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE+", ";
												break;
										}
										break;
								}
							}
						}
					}
				}
				if(addedItems.length()>0){
					addedItems = plugin.stripLast(addedItems, ",");
					rmp.sendMessage(ChatColor.YELLOW+"Added: "+addedItems);
				}
				if(modifiedItems.length()>0){
					modifiedItems = plugin.stripLast(modifiedItems, ",");
					rmp.sendMessage(ChatColor.YELLOW+"Modified: "+modifiedItems);
				}
				if(removedItems.length()>0){
					removedItems = plugin.stripLast(removedItems, ",");
					rmp.sendMessage(ChatColor.GRAY+"Removed: "+removedItems);
				}
			}
			else if(rmp.getPlayer().isSneaking()){
				if(clickState!=ClickState.NONE) tryAddItemToFilter(rmp, clickState);
			}
			else if(_config.getFilter().size()>0){
				if(clickState == ClickState.NONE){
					rmp.sendMessage("Click sign to clear all items.");
					setInterfaceState(InterfaceState.FILTER_CLEAR);
				}
			}
			updateSigns();
		}
	}
	
	public void tryAddItemToFilter(RMPlayer rmp, ClickState clickState){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			ItemStack item = rmp.getPlayer().getItemInHand();
			
			boolean force;
			if(clickState!=ClickState.NONE) force = true;
			else force = false;
			
			if(item!=null){
				int id = item.getTypeId();
				Material mat = item.getType();
				if(!isMaterial(mat, _hackMaterials)){
					RMItem rmItem;
					int amount = 1;
					rmItem = new RMItem(id, amount);
					switch(clickState){
					case NONE: case LEFT:
						switch(_config.getFilter().addItem(id, rmItem, force)){
							case ADD:
								rmp.sendMessage(ChatColor.YELLOW+"Added: "+ChatColor.WHITE+mat.name()+includeItem(rmItem));
								break;
							case MODIFY:
								rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+includeItem(rmItem));
								break;
						}
						break;
					case RIGHT:
						switch(_config.getFilter().removeItem(id, rmItem, force)){
							case MODIFY:
								rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+includeItem(rmItem));
								break;
							case REMOVE:
								rmp.sendMessage(ChatColor.GRAY+"Removed: "+ChatColor.WHITE+mat.name()+includeItem(rmItem));
								break;
						}
						break;
					}
					updateSigns();
				}
				else{
					_lastHackMaterials.clear();
					_lastHackMaterials.add(mat);
					if(_config.getWarnHackedItems()) warnHackMaterials(_lastHackMaterials); 
				}
			}
		}
	}
	
	public void tryAddFoundItems(Block b, RMPlayer rmp){
		RMTeam rmTeam = getTeamByBlock(b);
		RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
		if((rmTeam!=null)&&(rmPlayer!=null)){
			RMChest rmChest = rmTeam.getChest();
			HashMap<Integer, RMItem> added = new HashMap<Integer, RMItem>();
			HashMap<Integer, RMItem> returned = new HashMap<Integer, RMItem>();
			int totalFound = 0;
			Inventory inv = rmChest.getChest().getInventory();
			returned = _items.cloneItems(-1);
			for(int i=0; i<inv.getSize(); i++){
				ItemStack item = inv.getItem(i);
				int id = item.getTypeId();
				if(item!=null){
					if(item.getType()!=Material.AIR){
						if(_items.containsKey(id)){
							int overflow = 0;
							overflow = rmChest.addItem(item);
							if(overflow!=item.getAmount()){
								RMItem rmItem;
								if(added.containsKey(id)){
									rmItem = added.get(id);
									rmItem.addAmount(item.getAmount()-overflow);
									added.put(id, rmItem);
								}
								else{
									rmItem = new RMItem(id, item.getAmount()-overflow);
									added.put(id, rmItem);
								}
								totalFound+=item.getAmount()-overflow;
								returned.put(id, rmChest.getItemLeft(id));
								//rmp.sendMessage("getItemLeft:"+rmChest.getItemLeft(id));
							}
							if(overflow>0){
								item.setAmount(overflow);
							}
							else inv.clear(i);
						}
					}
				}
			}
			rmp.getStats().addItemsFoundTotal(totalFound);
			_config.getGameStats().addItemsFoundTotal(totalFound);
			RMStats.addServerItemsFoundTotal(totalFound);
			if(added.size()>0){
				if(returned.size()>0){
					rmp.sendMessage(ChatColor.YELLOW+"Items left: "+getFormattedStringByHash(returned, rmp));
					broadcastMessage(rmp.getTeam().getTeamColorString()+ChatColor.WHITE+" team has "+ChatColor.YELLOW+rmChest.getItemsLeftInt()+ChatColor.WHITE+" item(s) ("+ChatColor.YELLOW+rmChest.getTotalLeft()+ChatColor.WHITE+" total) left.",rmp);
				}
			}
			else updateGameplayInfo(rmp);
		}
		updateSigns();
	}
		
	public void handleRightClick(Block b, RMPlayer rmp){
		Material mat = b.getType();
		switch(getState()){
			case SETUP:
				switch(getInterfaceState()){
				case MAIN:
					switch(mat){
					case CHEST:
						tryAddItemsToFilter(b, rmp, ClickState.RIGHT);
						break;
					case WALL_SIGN:
						trySignSetupInfo(rmp);
						break;
					case WOOL:
						joinTeamByBlock(b, rmp, false);
						break;
					}
					break;
				}
				break;
		}
	}
	
	public void handleLeftClick(Block b, RMPlayer rmp){
		Material mat = b.getType();
		switch(getState()){
		
			// Setup State
			case SETUP:
				switch(getInterfaceState()){
				case MAIN: //MAIN
					switch(mat){
					case CHEST:
						if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						break;
					case WALL_SIGN:
						trySignSetupInfo(rmp);
						break;
					case WOOL:
						joinTeamByBlock(b, rmp, false);
						break;
					}
					break;
				case FILTER_CLEAR: //FILTER CLEAR
					switch(mat){
					case CHEST: case GLASS: case STONE:
						if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
							rmp.sendMessage(ChatColor.GRAY+"Canceled.");
							setInterfaceState(InterfaceState.MAIN);
							updateSigns();
						}
						break;
					case WALL_SIGN:
						if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
							_config.getFilter().clearItems();
							rmp.sendMessage(ChatColor.GRAY+"Filter items cleared.");
							setInterfaceState(InterfaceState.MAIN);
							updateSigns();
						}
						break;
					case WOOL:
						if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
							rmp.sendMessage(ChatColor.GRAY+"Canceled.");
							setInterfaceState(InterfaceState.MAIN);
							updateSigns();
						}
						else joinTeamByBlock(b, rmp, false);
						break;
					}
					break;
				}
				break;
				
			// Countdown State
			case COUNTDOWN:
				switch(mat){
				case CHEST: case WALL_SIGN: case WOOL:
					if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
						//stopGame(rmp);
					}
					break;
				}
				break;
				
			// Gameplay State
			case GAMEPLAY:
				switch(mat){
				case CHEST:
					tryAddFoundItems(b, rmp);
					setWinningTeam(findWinningTeam());
					gameOver();
					break;
				case WALL_SIGN:
					trySignGameplayInfo(b, rmp);
					break;
				case WOOL:
					joinTeamByBlock(b, rmp, false);
					break;
				}
				break;
				
			// Gameover State
			case GAMEOVER:
				switch(mat){
				case CHEST:
					break;
				case WALL_SIGN:
					break;
				case WOOL:
					break;
				}
		}
	}
	
	public boolean hasMinimumPlayers(){
		int teams = 0;
		for(RMTeam rmTeam : getTeams()){
			if(rmTeam.getPlayers().length>0){
				teams++;
			}
		}
		if(teams>1) return true;
		else return false;
	}
	
	public RMTeam findWinningTeam(){
		for(RMChest rmChest : getChests()){
			if(rmChest.getItemsLeftInt()==0){
				return rmChest.getTeam();
			}
		}
		return null;
	}
	
	public RMTeam findLeadingTeam(){
		List<RMTeam> rmTeams = getTeams();
		int total = -1;
		RMTeam leadingTeam = null;
		for(RMTeam rmt : rmTeams){
			int totalLeft = rmt.getChest().getTotalLeft();
			if((total==-1)||(total<totalLeft)){
				total = totalLeft;
				leadingTeam = rmt;
			}
			
		}
		return leadingTeam;
	}
	
	public void gameOver(){
		RMTeam rmTeam = getWinningTeam();
		if(rmTeam!=null){
			setState(RMState.GAMEOVER);
			broadcastMessage(rmTeam.getTeamColorString()+ChatColor.WHITE+" team has won the match!");
			for(RMTeam rmt : getTeams()){
				if(rmt!=rmTeam){
					for(RMPlayer rmPlayer : rmt.getPlayers()){
						rmPlayer.getStats().addLosses();
						rmPlayer.getStats().addTimesPlayed();
						_config.getGameStats().addLosses();
						_config.getGameStats().addTimesPlayed();
						RMStats.addServerLosses();
						RMStats.addServerTimesPlayed();
					}
				}
			}
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				rmPlayer.getStats().addWins();
				rmPlayer.getStats().addTimesPlayed();
				_config.getGameStats().addWins();
				_config.getGameStats().addTimesPlayed();
				RMStats.addServerWins();
				RMStats.addServerTimesPlayed();
			}
			//setWinPlayer(rmp);
			update();
		}
	}
	
	//UPDATE
	public void update(){
		switch(getState()){
		case SETUP:
			break;
		case COUNTDOWN:
			//cdTimer = 0; //////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(cdTimer%10==0){
				if(cdTimer!=0){
					broadcastMessage(""+cdTimer/10);
					updateSigns(""+cdTimer/10);
				}
			}
			if(cdTimer>0){
				cdTimer-=10;
			}
			else{
				broadcastMessage("LET THE RESOURCE MADNESS BEGIN!");
				cdTimer = cdTimerLimit;
				setState(RMState.GAMEPLAY);
				for(RMTeam rmt : getTeams()){
					for(RMPlayer rmp : rmt.getPlayers()){
						updateGameplayInfo(rmp);
					}
				}
				snatchInventories();
				warpPlayersToSafety();
				updateSigns();
			}
			break;
		case GAMEPLAY:
			break;
		case GAMEOVER:
			initGameOver();
			break;
		}
	}
	
	public void snatchInventories(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				if(rmp.getPlayer()!=null){
					Inventory inv = rmp.getPlayer().getInventory();
					rmp.clearInventoryContents();
					rmp.addContentsToInventory(inv.getContents());
					inv.clear();
				}
			}
		}
	}
	
	public void returnInventories(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				if(rmp.getPlayer()!=null){
					Inventory inv = rmp.getPlayer().getInventory();
					inv.clear();
					inv.setContents(rmp.getContentsFromInventory());
					rmp.clearInventoryContents();
				}
			}
		}
	}
	
	public void clearTeamPlayers(){
		for(RMTeam rmTeam : getTeams()){
			rmTeam.clearPlayers();
		}
	}
	
	public void warpPlayersToSafety(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				rmp.warpToSafety();
			}
		}
	}

	//Constructor
	public RMGame(RMPartList partList, RMPlayer rmp, RM plugin){
		this.plugin = plugin;
		_config.setPartList(partList);
		_config.setPlayers(RMPlayer.getPlayers());
		_config.setId(getFreeId());
		_config.setOwnerName(rmp.getName());
	}
	public RMGame(RMGameConfig config, RM plugin){
		this.plugin = plugin;
		_config = config;
		_config.setId(getFreeId());
	}
	
	public int getFreeId(){
		int i=0;
		for(RMGame rmGame : _games){
			i = checkIdMatch(i, rmGame);
		}
		return i;
	}
	
	public int checkIdMatch(int i, RMGame rmGame){
		if(i==rmGame.getConfig().getId()){
			i++;
			i = checkIdMatch(i, rmGame);
		}
		return i;
	}
	
	//Game
	private static RMGame addGame(RMGame rmGame){
		if(!_games.contains(rmGame)){
			_games.add(rmGame);
		}
		return rmGame;
	}
	public static void tryAddGameFromConfig(RMGameConfig config){
		RMGame rmGame = addGame(new RMGame(config, plugin));
		for(RMTeam rmt : rmGame.getConfig().getTeams()){
			rmt.setGame(rmGame);
		}
		rmGame.updateSigns();
	}
	
	public static HandleState tryAddGame(Block b, RMPlayer rmp, Block bRemove){
		RMPartList partList;
		if(bRemove!=null) partList = new RMPartList(b, bRemove, plugin);
		else partList = new RMPartList(b, plugin);
		RMGame rmGame = getGameByBlock(partList.getMainBlock(b));
		
		Boolean wasModified = false;
		RMGameConfig rmGameConfig = new RMGameConfig();
		if(rmGame==null){
			//plugin.getServer().broadcastMessage("ISNULL!");
			rmGame = RMGame.getGameByBlock(b);
		}
		if(rmGame!=null){
			if(!rmp.getName().equalsIgnoreCase(rmGame._config.getOwnerName())){
				rmp.sendMessage("The owner is "+rmGame._config.getOwnerName()+".");
				return HandleState.NOCHANGE;
			}
			if(!partList.matchPartList(rmGame._config.getPartList())){
				//plugin.getServer().broadcastMessage("NO MATCH!");
				wasModified = true;
				rmGameConfig = rmGame._config;
				RMGame.removeGame(rmGame);
				rmGame = null;
			}
			else{
				//plugin.getServer().broadcastMessage("MATCH PERFECTLY!");
				if(rmGame.getTeams().size()==4){
					rmp.sendMessage("Game id "+rmGame._config.getId()+" has the maximum amount of teams!");
					return HandleState.NOCHANGE;
				}
				rmp.sendMessage("Game id "+rmGame._config.getId()+" already exists!");
				return HandleState.NOCHANGE;
			}
		}
		
		if(partList.getStoneList().size()<2){
			rmp.sendMessage("You're missing "+(2-partList.getStoneList().size())+" stone block." );
			return HandleState.NONE;
		}
	
		List<RMTeam> teams = partList.fetchTeams();
		if(teams.size()<2){
			rmp.sendMessage("You need at least two teams to create a game");
			return HandleState.NONE;
		}

		rmGame = addGame(new RMGame(partList, rmp, plugin));
		for(RMTeam rmt : teams){
			rmGame.addTeam(rmt);
		}
		if(wasModified){
			RMGameConfig config = rmGame.getConfig();
			config.getDataFrom(rmGameConfig);
			rmGameConfig = null;
			rmp.sendMessage("Game id "+rmGame._config.getId()+" has been modified.");
		}
		else rmp.sendMessage("Game id "+rmGame._config.getId()+" has been created.");
		rmp.sendMessage("Found "+teams.size()+" teams. ("+rmGame.getTextTeamColors()+")");
		rmGame.updateSigns();
		
		if(wasModified) return HandleState.MODIFY;
		else return HandleState.ADD;
		
	}
	
	private static Boolean removeGame(RMGame rmGame){
		if(_games.contains(rmGame)){
			//rmGame._config.getOwner().getGames().remove(rmGame);
			RMGame.getGamesByOwnerName(rmGame.getConfig().getOwnerName()).remove(rmGame);
			for(RMTeam rmt : rmGame.getTeams()){
				rmt.setNull();
			}
			_games.remove(rmGame);
			rmGame = null;
			return true;
		}
		return false;
	}
	public static HandleState tryRemoveGame(RMGame rmGame, RMPlayer rmp, Boolean justRemove){
		if(rmGame!=null){
			return tryRemoveGame(rmGame._config.getPartList().getMainBlock(), rmp, justRemove);
		}
		return HandleState.NONE;
	}
	public static HandleState tryRemoveGame(Block b, RMPlayer rmp, boolean justRemove){
		RMGame rmGame = getGameByBlock(b);
		if(rmGame!=null){
			if(rmGame.getState() == RMState.SETUP){
				if(rmp.getName().equalsIgnoreCase(rmGame._config.getOwnerName())){
					if((isMaterial(b.getType(), Material.CHEST, Material.WALL_SIGN, Material.WOOL))&&(!justRemove)){
						List<List<Block>> blockList = rmGame._config.getPartList().getBlockList();
						List<Block> blocks = rmGame._config.getPartList().getList();
						//plugin.getServer().broadcastMessage("JUSTREMOVE");
						for(Block block : blocks){
							if(rmGame.getMainBlock() == block){
								//plugin.getServer().broadcastMessage("MAINBLOCK");
								return tryAddGame(rmGame.getMainBlock(), rmp, b);
							}
							//plugin.getServer().broadcastMessage("OUTNONE");
						}
					}
					for(Sign sign : rmGame.getSigns()){
						sign.setLine(0, "");
						sign.setLine(1, "");
						sign.setLine(2, "");
						sign.setLine(3, "");
						sign.update();
					}
					rmp.sendMessage("Successfully removed game with id "+rmGame._config.getId());
					removeGame(rmGame);
					return HandleState.REMOVE;
				}
				else{
					rmp.sendMessage("The owner is "+rmGame._config.getOwnerName()+".");
					return HandleState.NOCHANGE;
				}
			}
		}
		//plugin.getServer().broadcastMessage("NOGAME");
		return HandleState.NONE;
	}
	
	public Block getMainBlock(){
		return _config.getPartList().getMainBlock();
	}
	
	//Game GET-SET
	public RMGame getGame(){
		return this;
	}
	public static RMGame getGame(int id){
		if((id>=0)&&(id<_games.size())) return _games.get(id);
		else return null;
	}
	public static List<RMGame> getGames(){
		return _games;
	}
	public static RMGame getGameByBlock(Block b){
		for(RMGame game : RMGame.getGames()){
			for(Block block : game._config.getPartList().getList()){
				if(block == b){
					return game;
				}
				
			}
		}
		return null;
	}
	private static List<RMGame> getGamesByOwner(RMPlayer rmp){
		List<RMGame> games = new ArrayList<RMGame>();
		for(RMGame game : RMGame.getGames()){
			if(rmp.getName().equalsIgnoreCase(game._config.getOwnerName())) games.add(game); 
		}
		return games;
	}
	private static List<RMGame> getGamesByOwnerName(String name){
		List<RMGame> games = new ArrayList<RMGame>();
		for(RMGame game : RMGame.getGames()){
			if(game._config.getOwnerName().equalsIgnoreCase(name)) games.add(game); 
		}
		return games;
	}
	
	//State
	public RMState getState(){
		return _state;
	}
	public void setState(RMState state){
		_state = state;
	}
	
	public InterfaceState getInterfaceState(){
		return _interfaceState;
	}
	public void setInterfaceState(InterfaceState state){
		_interfaceState = state;
		switch(state){
			case MAIN:
				_menuItems = 0;
				break;
			case FILTER_CLEAR:
				_menuItems = 3;
				break;
		}
	}
	
	//Players
	public String getPlayersNames(){
		RMPlayer[] rmplayers = _config.getPlayers().values().toArray(new RMPlayer[_config.getPlayers().values().size()]);
		String names = "";
		for(RMPlayer rmp : rmplayers){
			names+=rmp.getName()+",";
		}
		return names.substring(0, names.length()-1);
	}
	public static HashMap<String, RMPlayer> getAllPlayers(){
		HashMap<String, RMPlayer> players = new HashMap<String, RMPlayer>();
		for(RMGame game : _games){
			players.putAll(game._config.getPlayers());
		}
		return players;
	}
	
	/*
	public void sendMessage(String message){
		RMPlayer[] rmplayers = _config.getPlayers().values().toArray(new RMPlayer[_config.getPlayers().values().size()]);
		for(RMPlayer rmp : rmplayers){
			rmp.sendMessage(message);
		}
	}
	*/

	public void broadcastMessage(String message){
		List<RMTeam> teams = getTeams();
		for(RMTeam rmt : teams){
			RMPlayer[] players = rmt.getPlayers();
			for(RMPlayer rmp : players){
				rmp.sendMessage(message);
			}
		}
	}
	public void broadcastMessage(String message, RMPlayer ignorePlayer){
		List<RMTeam> teams = getTeams();
		for(RMTeam rmt : teams){
			RMPlayer[] players = rmt.getPlayers();
			for(RMPlayer rmp : players){
				if(rmp!=ignorePlayer) rmp.sendMessage(message);
			}
		}
	}
	//Team
	public void teamMessage(RMTeam rmt, String message){
		rmt.teamMessage(message);
	}
	
	public void joinTeam(RMTeam rmTeam, RMPlayer rmp){
		rmTeam.addPlayer(rmp);
	}
	public void quitTeam(RMTeam rmTeam, RMPlayer rmp){
		rmTeam.removePlayer(rmp);
	}
	public void quitTeam(RMGame rmGame, RMPlayer rmp){
		RMTeam rmTeam = rmGame.getPlayerTeam(rmp);
		if(rmTeam!=null){
			rmTeam.removePlayer(rmp);
			//rmp.sendMessage("You quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team.");
		}
	}
	public RMTeam joinTeamByBlock(Block b, RMPlayer rmp, boolean fromConsole){
		if(_state == RMState.SETUP){
			RMTeam rmt = getTeamByBlock(b);
			if(rmt!=null){
				RMTeam rmTeam = getPlayerTeam(rmp);
				if(rmTeam!=null){
					if(rmt!=rmTeam){
						rmp.sendMessage("You must quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team first.");
						return null;
					}
				}
				rmt.addRemovePlayer(rmp);
				return rmt;
			}
			else rmp.sendMessage("This team does not exist!");
		}
		else{
			rmp.sendMessage("To join/quit a game in progress, use /rm "+ChatColor.YELLOW+"join"+ChatColor.WHITE+"/"+ChatColor.YELLOW+"quit"+ChatColor.WHITE+".");
		}
		return null;
	}
	public RMTeam quitTeamByBlock(Block b, RMPlayer rmp){
		RMTeam rmTeam = getTeamByBlock(b);
		if(rmTeam!=null){
			if(rmTeam.getPlayer(rmp.getName())!=null){
				rmTeam.removePlayer(rmp);
				rmp.sendMessage("You quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team.");
				return rmTeam;
			}
			else rmp.sendMessage("You are not on the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team.");
		}
		return null;
	}
	private RMTeam getTeamByBlock(Block b){
		if(RMGame.isMaterial(b.getType(), Material.CHEST, Material.WALL_SIGN, Material.WOOL)){
			List<List<Block>> partList = _config.getPartList().getPartList();
			int i=0;
			for(List<Block> blocks : partList){
				if(blocks.contains(b)){
					return getTeam(i);
				}
				i++;
			}
		}
		return null;
	}
	
	private RMTeam addTeam(RMTeam rmt){
		if(!_config.getTeams().contains(rmt)){
			_config.getTeams().add(rmt);
			rmt.setGame(this);
		}
		return rmt;
	}
	public RMTeam getTeam(int index){
		if(index<_config.getTeams().size()){
			if(_config.getTeams().get(index)!=null){
				return _config.getTeams().get(index);
			}
		}
		return null;
	}
	public RMTeam getTeam(DyeColor color){
		for(RMTeam rmTeam : _config.getTeams()){
			if(rmTeam.getTeamColor() == color){
				return rmTeam;
			}
		}
		return null;
	}
	
	public List<RMTeam> getTeams(){
		return _config.getTeams();
	}
	public static RMTeam getTeam(RMGame rmGame, int index){
		if(rmGame._config.getTeams().size()>index) return rmGame._config.getTeams().get(index);
		return null;
	}
	public static List<RMTeam> getTeams(RMGame rmGame){
		return rmGame._config.getTeams();
	}
	public RMPlayer[] getTeamPlayers(){
		List<RMPlayer> list = new ArrayList<RMPlayer>();
		for(RMTeam rmTeam : _config.getTeams()){
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				list.add(rmPlayer);
			}
		}
		return list.toArray(new RMPlayer[list.size()]);
	}
	
	//Chest
	public RMChest getChest(int index){
		RMTeam rmt = getTeam(index);
		return rmt.getChest();
	}
	public RMChest getChest(RMTeam rmTeam){
		return rmTeam.getChest();
	}
	public RMChest[] getChests(){
		List<RMTeam> rmTeams = getTeams();
		RMChest[] rmChests = new RMChest[rmTeams.size()];
		for(int i=0; i<rmTeams.size(); i++){
			rmChests[i] = rmTeams.get(i).getChest();
		}
		return rmChests;
	}
	public static List<RMChest> getChestsFromBlockList(List<List<Block>> bList, RM plugin){
		List<List<Block>> blockList = bList.subList(2, bList.size());
		List<RMChest> chestList = new ArrayList<RMChest>();
		for(List<Block> blocks : blockList){
			chestList.add(new RMChest((Chest)blocks.get(Part.CHEST.ordinal()-2).getState(), plugin));
		}
		if(chestList.size()>0) return chestList;
		else return null;
	}
	private RMChest getChestByBlock(Block b){
		if(b.getType() == Material.CHEST){
			List<List<Block>> partList = _config.getPartList().getPartList();
			int i=0;
			for(List<Block> blocks : partList){
				if(blocks.contains(b)){
					return getChest(i);
				}
				i++;
			}
		}
		return null;
	}
	
	//Sign
	private Sign getSignByBlock(Block b){
		if(b.getType() == Material.WALL_SIGN){
			List<List<Block>> partList = _config.getPartList().getPartList();
			int i=0;
			for(List<Block> blocks : partList){
				if(blocks.contains(b)){
					return getSign(i);
				}
				i++;
			}
		}
		return null;
	}
	public Sign getSign(int index){
		RMTeam rmt = getTeam(index);
		return rmt.getSign();
	}
	public Sign getSign(RMTeam rmTeam){
		return rmTeam.getSign();
	}
	public List<Sign> getSigns(){
		List<Sign> signList = new ArrayList<Sign>();
		for(RMTeam rmt : getTeams()){
			signList.add(rmt.getSign());
		}
		return signList;
	}
	
	//String
	public String getTextTeamColors(){
		String line = "";
		for(RMTeam team : _config.getTeams()){
			if(team.getTeamColor()!=null){
				line+=team.getTeamColorString()+ChatColor.WHITE+",";
			}
		}
		return line.substring(0,line.length()-1);
	}
	public RMTeam getPlayerTeam(RMPlayer rmp){
		for(RMTeam team : _config.getTeams()){
			if(team!=null){
				RMPlayer rmTeamPlayer = team.getPlayer(rmp.getName());
				if(rmTeamPlayer!=null) return team;
			}
		}
		return null;
	}
	public RMPlayer[] getPlayersByTeam(RMTeam rmt){
		return rmt.getPlayers();
	}
	
	public String getTextTeamPlayers(){
		String line = "";
		for(RMTeam team : _config.getTeams()){
			if(team.getPlayers()!=null){
				line+=team.getTeamColorString()+":";
				line+=team.getPlayersNames()+",";
			}
		}
		
		if(line.length()>1) return line.substring(0,line.length()-1);
		return "nope";
	}
	public static List<RMTeam> getAllTeams(){
		List<RMTeam> teams = new ArrayList<RMTeam>();
		for(RMGame game : _games){
			teams.addAll(game._config.getTeams());
		}
		return teams;
	}
	
	//Material
	public static Material[] getMaterials(){
		return _materials;
	}
	public static Boolean isMaterial(Material b, Material... materials){
		for(Material mat : materials){
			if(b == mat){
				return true;
			}
		}
		return false;
	}

	//Try Parse Filter
	public Boolean tryParseFilter(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			RMGame.setRequestPlayer(rmp);
			parseFilter(rmp);
			RMGame.clearRequestPlayer();
			rmp.clearRequestFilter();
			updateSigns();
			return true;
		}
		else rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can modify the filter.");
		return false;
	}
	
	//Parse Filter
	private void parseFilter(RMPlayer rmp){
		RMRequestFilter filter = rmp.getRequestFilter();
		if(filter!=null){
			ForceState force = filter.getForce();
			int randomize = filter.getRandomize();
			HashMap<Integer, RMItem> items = filter.getItems();
			if((items!=null)&&(items.size()!=0)){
				if(!_config.getAllowHackedItems()){
					if(_config.getWarnHackedItems()) warnHackMaterials(items);
					items = removeHackMaterials(items);
					if(items==null){
						rmp.sendMessage(ChatColor.GRAY+"No items modified.");
						return;
					}
				}
				if(force!=null){
					if((randomize>0)&&(items.size()>randomize)){
						Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
						List<Integer> listItems = new ArrayList<Integer>();
						for(Integer i : arrayItems){
							listItems.add(i);
						}
						int size = listItems.size();
						while(size>randomize){
							int random = (int)Math.round((Math.random()*(size-1)));
							items.remove(listItems.get(random));
							listItems.remove(random);
							size--;
						}
					}
					parseFilterArgs(rmp, items, force);
					return;
				}
				else{
					parseFilterArgs(rmp, items);
					return;
				}
			}
			FilterType type = filter.getType();
			if(type!=null){
				if(type==FilterType.CLEAR){
					_config.getFilter().clearItems();
					rmp.sendMessage(ChatColor.GRAY+"Filter cleared.");
					return;
				}
			}
		}
		rmp.sendMessage(ChatColor.GRAY+"No items modified.");
	}
	
	//Parse Filter Args
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, RMItem> items){
		List<String> added = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		List<String> modified = new ArrayList<String>();
		String strItem;
		Boolean getId = false;
		if(items.size()>_config.getWordLimit()) getId=true;
		Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(arrayItems);
		for(Integer item : arrayItems){
			Material mat = Material.getMaterial(item);
			if(mat!=Material.AIR){
				if(getId) strItem = ""+item;
				else strItem = mat.name();
				Integer amount = items.get(item).getAmount();
				if(amount!=0){
					switch(_config.getFilter().addItem(item, items.get(item), false)){
						case ADD:
							added.add(ChatColor.WHITE+strItem+includeItem(items.get(item)));
							break;
						case MODIFY:
							modified.add(ChatColor.WHITE+strItem+includeItem(items.get(item)));	
							break;
					}
				}
				else{
					switch(_config.getFilter().removeItem(item, items.get(item), false)){
						case REMOVE:
							removed.add(ChatColor.WHITE+strItem+includeItem(_config.getFilter().getLastItem()));
							break;
					}
				}
				//if(addRemoveItemToFilter(item, items.get(item))) added.add(ChatColor.WHITE+strItem+includeItem(items.get(item)));
				//else removed.add(ChatColor.WHITE+strItem+includeItem(items.get(item)));
			}
		}
		if(added.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+plugin.getFormattedStringByList(added));
		if(modified.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+plugin.getFormattedStringByList(modified));
		if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+plugin.getFormattedStringByList(removed));
	}
	
	//Parse Filter Args Force
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, RMItem> items, ForceState force){
		List<String> added = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		String strItem;
		Boolean getId = false;
		if(items.size()>_config.getWordLimit()) getId=true;
		Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(arrayItems);

		switch(force){
		case ADD: case RANDOMIZE:
			for(Integer item : arrayItems){
				Material mat = Material.getMaterial(item);
				if(mat!=Material.AIR){
					if(getId) strItem = ""+item;
					else strItem = mat.name();
					switch(_config.getFilter().addItem(item, items.get(item), false)){
						case ADD:
							added.add(ChatColor.WHITE+strItem+includeItem(items.get(item))+ChatColor.WHITE);
							break;
					}
				}
			}
			if(added.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+plugin.getFormattedStringByList(added));
			break;
		case REMOVE:
			for(Integer item : arrayItems){
				Material mat = Material.getMaterial(item);
				if(mat!=Material.AIR){
					if(getId) strItem = ""+item;
					else strItem = mat.name();
					switch(_config.getFilter().removeAlwaysItem(item, items.get(item))){
						case REMOVE:
							removed.add(ChatColor.WHITE+strItem+includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE);
							break;
					}
				}
			}
			if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+plugin.getFormattedStringByList(removed));
			break;
		}
	}
	
	//Get Formatted String by Hash
	public String getFormattedStringByHash(HashMap<Integer, RMItem> items, RMPlayer rmp){
		RMChest rmChest = rmp.getTeam().getChest();
		String line = "";
		Integer[] array = items.keySet().toArray(new Integer[items.keySet().size()]);
		Arrays.sort(array);
		for(Integer item : array){
			RMItem rmItem = items.get(item);
			int amount = rmItem.getAmount(); 
			if(amount!=-1){
				if(amount!=0){
					line+=ChatColor.GREEN+Material.getMaterial(item).name()+includeItem(rmItem)+ChatColor.WHITE+", ";
				}
				else line+=ChatColor.DARK_GREEN+Material.getMaterial(item).name()+":0"+ChatColor.WHITE+", ";
			}
			else{
				if(rmChest.getItems().containsKey(item)) amount = rmChest.getItemLeft(item).getAmount();
				else amount = _items.getItem(item).getAmount();
				if(amount!=0) line+=ChatColor.WHITE+Material.getMaterial(item).name()+includeItem(new RMItem(item, amount))+ChatColor.WHITE+", ";
			}
		}
		line = plugin.stripLast(line, ",");
		return line;
	}
	
	//Include Item
	public String includeItem(RMItem rmItem, boolean... less){
		int i1 = rmItem.getAmount();
		int i2 = rmItem.getAmountHigh();
		if((i1!=1)&&(less.length==0)){
			if(i2>0) return ChatColor.GRAY+":"+i1+"-"+i2;
			return ChatColor.GRAY+":"+i1;
		}
		return "";
	}
	
	/*
	public List<Material> findHackMaterials(ItemStack[] items){
		List<Material> materials = Arrays.asList(_hackMaterials);
		List<Material> list = new ArrayList<Material>();
		for(ItemStack item : items){
			Material mat = item.getType();
			if(materials.contains(mat)) list.add(mat);
		}
		return list;
	}
	*/
	
	//Find Hack Materials
	public List<Material> findHackMaterials(HashMap<Integer, RMItem> materials){
		List<Material> list = new ArrayList<Material>();
		for(Material mat : _hackMaterials){
			if(materials.containsKey(mat.getId())) list.add(mat);
		}
		return list;
	}
	
	//Find Hack Materials
	public List<Material> findHackMaterials(List<Material> items){
		List<Material> list = new ArrayList<Material>();
		for(Material mat : _hackMaterials){
			if(items.contains(mat)) list.add(mat);
		}
		return list;
	}
	
	//Find Hack Materials List Item Stack
	public List<Material> findHackMaterialsListItemStack(List<ItemStack> items){
		List<Material> materials = Arrays.asList(_hackMaterials);
		List<Material> list = new ArrayList<Material>();
		for(ItemStack item : items){
			Material mat = item.getType();
			if(materials.contains(mat)) list.add(mat);
		}
		return list;
	}
	
	//Remove Hack Materials
	public HashMap<Integer, RMItem> removeHackMaterials(HashMap<Integer, RMItem> materials){
		for(Material mat : _hackMaterials) materials.remove(mat.getId());
		return materials;
	}
	
	//Remove Hack Materials
	public List<ItemStack> removeHackMaterialsListItemStack(List<ItemStack> items){
		List<Material> materials = Arrays.asList(_hackMaterials);
		List<ItemStack> list = new ArrayList<ItemStack>();
		for(ItemStack item : items){
			Material mat = item.getType();
			if(!materials.contains(mat)) list.add(item);
		}
		return list;
	}

	/*
	public Material[] removeHackMaterials(Material[] materials){
		List<Material> list = new ArrayList<Material>(Arrays.asList(materials));
		list.removeAll(Arrays.asList(_hackMaterials));
		return list.toArray(new Material[list.size()]);
	}
	
	public List<Material> removeHackMaterials(List<Material> materials){
		materials.removeAll(Arrays.asList(_hackMaterials));
		return materials;
	}
	
	public ItemStack[] removeHackMaterials(ItemStack[] items){
		List<Material> materials = Arrays.asList(_hackMaterials);
		List<ItemStack> list = new ArrayList<ItemStack>();
		for(ItemStack item : items){
			Material mat = item.getType();
			if(!materials.contains(mat)) list.add(item);
		}
		return list.toArray(new ItemStack[list.size()]);
	}
	*/
	
	//Remove Duplicates
	public List<ItemStack> removeDuplicates(List<ItemStack> items){
		List<Material> materials = new ArrayList<Material>();
		for(ItemStack item : items){
			if(item!=null){
				Material mat = item.getType();
				if(!materials.contains(mat)) materials.add(mat);
			}
		}
		List<ItemStack> newItems = new ArrayList<ItemStack>();
		for(int i=0; i<materials.size(); i++){
			Material mat = materials.get(i);
			newItems.add(new ItemStack(mat, mat.getMaxStackSize()));
		}
		return newItems;
	}
	
	//Add Duplicates
	public List<ItemStack> addDuplicates(List<ItemStack> items, boolean fullStack){
		HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
		
		for(ItemStack item : items){
			if(item!=null){
				int amount = item.getAmount();
				if(fullStack) amount = 64;
				int id = item.getTypeId();
				if(!hash.containsKey(id)){
					hash.put(id, amount);
				}
				else hash.put(id, hash.get(id) + amount);
			}
		}
		
		List<ItemStack> newItems = new ArrayList<ItemStack>();
		Integer[] array = hash.keySet().toArray(new Integer[hash.keySet().size()]);
		Arrays.sort(array);
			
		for(Integer i : array){
			newItems.add(new ItemStack(Material.getMaterial(i), hash.get(i)));
		}
		RMStats temp = new RMStats();
		return newItems;
	}
	
	//LOG WORLD
	public void addLog(BlockState bState){
		Block b = bState.getBlock();
		Material mat = bState.getType();
		if(isMaterial(mat, _blockItemMaterials)){
			if(!_logBlockItemList.containsKey(bState.getBlock().getLocation())) _logBlockItemList.put(b.getLocation(), new RMBlock(bState));
		}
		else if(!_logBlockList.containsKey(bState.getBlock().getLocation())) _logBlockList.put(b.getLocation(), new RMBlock(bState));
		checkLog(b);
	}
	
	//Check Log
	public void checkLog(Block b){
		if(b!=null){
			List<Block> blocks = new ArrayList<Block>();
			blocks.add(b.getRelative(BlockFace.NORTH));
			blocks.add(b.getRelative(BlockFace.EAST));
			blocks.add(b.getRelative(BlockFace.SOUTH));
			blocks.add(b.getRelative(BlockFace.WEST));
			blocks.add(b.getRelative(BlockFace.DOWN));
			blocks.add(b.getRelative(BlockFace.UP));
			for(int i=0; i<blocks.size(); i++){
				Block block = blocks.get(i);
				Location loc = block.getLocation();
				Material mat = block.getType();
				if(i<5){ //NORTH,EAST,SOUTH,WEST
					//if(isMaterial(mat, Material.TORCH, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF, Material.LEVER, Material.LADDER, Material.PAINTING, Material.STONE_BUTTON, Material.WALL_SIGN)){
					if(isMaterial(mat, _blockItemMaterials)){
						if(!_logBlockItemList.containsKey(loc)) _logBlockItemList.put(loc, new RMBlock(block));
					}
					else if(!_logBlockList.containsKey(loc)) _logBlockList.put(loc, new RMBlock(block));
					//}
				}
				else{
					if(isMaterial(mat, Material.CACTUS, Material.CROPS, Material.SUGAR_CANE_BLOCK, Material.GRAVEL, Material.SAND)){
						if(isMaterial(mat, _blockItemMaterials)){
							if(!_logBlockItemList.containsKey(loc)) _logBlockItemList.put(loc, new RMBlock(block));
						}
						else if(!_logBlockList.containsKey(loc)) _logBlockList.put(loc, new RMBlock(block));
						checkLog(block);
					}
					else{
						if(isMaterial(mat, _blockItemMaterials)){
							if(!_logBlockItemList.containsKey(loc)) _logBlockItemList.put(loc, new RMBlock(block));
						}
						else if(!_logBlockList.containsKey(loc)) _logBlockList.put(loc, new RMBlock(block));
					}
				}
			}
		}
	}
	
	//Clear Log
	public void clearLog(){
		_logBlockList.clear();
		_logBlockItemList.clear();
	}
	
	//Restore Log
	public void restoreLog(){
		for(RMBlock rmBlock : _logBlockList.values()){
			rmBlock.restore();
		}
		_logBlockList.clear();
		for(RMBlock rmBlock : _logBlockItemList.values()){
			rmBlock.restore();
		}
		_logBlockItemList.clear();
		broadcastMessage("World restored.");
	}
	
	//Restore World
	public void restoreWorld(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			restoreLog();
		}
	}
	
	//HACK MATERIALS
	//Warn Hack Materials
	
	/*public void warnHackMaterials(ItemStack[] items){
		warnHackMaterialsMessage(plugin.getFormattedStringByListMaterial(findHackMaterials(items)));
	}*/
	
	public void warnHackMaterialsListItemStack(List<ItemStack> items){
		warnHackMaterialsMessage(plugin.getFormattedStringByListMaterial(findHackMaterialsListItemStack(items)));
	}
	public void warnHackMaterials(List<Material> items){
		warnHackMaterialsMessage(plugin.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterials(HashMap<Integer, RMItem> items){
		warnHackMaterialsMessage(plugin.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterialsMessage(String message){
		if(message.length()>0) _config.getOwner().sendMessage(ChatColor.RED+"Not allowed: "+message);
	}
	
	//Config
	
	//Set Max Players
	public void setMaxPlayers(RMPlayer rmp, int maxPlayers){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.setMaxPlayers(maxPlayers);
			rmp.sendMessage("Max players: "+_config.getMaxPlayers());
			updateSigns();
		}
	}
	
	//Set Max Team Players
	public void setMaxTeamPlayers(RMPlayer rmp, int maxTeamPlayers){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.setMaxTeamPlayers(maxTeamPlayers);
			rmp.sendMessage("Max team players: "+_config.getMaxTeamPlayers());
			updateSigns();
		}
	}
	
	//Set Max Items
	public void setMaxItems(RMPlayer rmp, int maxItems){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.setMaxItems(maxItems);
			updateSigns();
		}
	}
	
	//Set Randomize Amount
	public void setRandomizeAmount(RMPlayer rmp, int amount){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.setRandomizeAmount(amount);
		}
	}
	
	//Set Auto Randomize Amount
	public void setAutoRandomizeAmount(RMPlayer rmp, int amount){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.setAutoRandomizeAmount(amount);
			rmp.sendMessage("Auto randomize amount every match: "+getTextAutoRandomizeAmount());
		}
	}
	
	//Set Toggle Auto Restore World
	public void toggleAutoRestoreWorld(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.toggleAutoRestoreWorld();
			rmp.sendMessage("Auto restore world after match: "+isTrueFalse(_config.getAutoRestoreWorld()));
		}
	}
	
	//Set Toggle Warn Hacked Items
	public void toggleWarnHackedItems(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.toggleWarnHackedItems();
			rmp.sendMessage("Warn when user adds hacked items: "+isTrueFalse(_config.getWarnHackedItems()));
		}
	}
	
	//Set Toggle Allow Hacked Items
	public void toggleAllowHackedItems(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.toggleAllowHackedItems();
			rmp.sendMessage("Allow user to add hacked items: "+isTrueFalse(_config.getAllowHackedItems()));
		}
	}
	
	//Set Toggle Allow Player Leave
	public void toggleAllowPlayerLeave(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.toggleAllowPlayerLeave();
			rmp.sendMessage("Allow a player to stay in-game while disconnected: "+isTrueFalse(_config.getAllowPlayerLeave()));
		}
	}
	
	//Set Clear Player Inventory
	public void toggleClearPlayerInventory(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.toggleClearPlayerInventory();
			rmp.sendMessage("Clear/return player's items at game start/finish: "+isTrueFalse(_config.getClearPlayerInventory()));
		}
	}
	
	public void toggleAllowMidgameJoin(RMPlayer rmp){
		if(rmp.getName().equalsIgnoreCase(_config.getOwnerName())){
			_config.toggleAllowMidgameJoin();
			rmp.sendMessage("Allow players to join midgame: "+isTrueFalse(_config.getAllowMidgameJoin()));
		}
	}
	
	//Is True / False
	public String isTrueFalse(boolean bool){
		return (bool?(ChatColor.GREEN+"True"):(ChatColor.GRAY+"False"));
	}
	
	//Get Text Auto Randomize Amount
	public String getTextAutoRandomizeAmount(){
		return (_config.getAutoRandomizeAmount()>0?(ChatColor.GREEN+""+_config.getAutoRandomizeAmount()+" item(s)"):(ChatColor.GRAY+"Disabled"));
	}
	
	//Get Text Max Players
	public String getTextMaxPlayers(){
		return (_config.getMaxPlayers()>0?(ChatColor.GREEN+""+_config.getMaxPlayers()):(ChatColor.GRAY+"No limit"));
	}
	
	//Get Text Max Team Players
	public String getTextMaxTeamPlayers(){
		return (_config.getMaxTeamPlayers()>0?(ChatColor.GREEN+""+_config.getMaxTeamPlayers()):(ChatColor.GRAY+"No limit"));
	}
	
	//Get Text Players of Max
	public String getTextPlayersOfMax(){
		String maxPlayers = "";
		if(_config.getMaxPlayers()!=0) maxPlayers = "/"+_config.getMaxPlayers();
		return maxPlayers;
	}
	
	//Get Text Team Players of Max
	public String getTextTeamPlayersOfMax(){
		String maxTeamPlayers = "";
		if(_config.getMaxTeamPlayers()!=0) maxTeamPlayers = "/"+_config.getMaxTeamPlayers();
		return maxTeamPlayers;
	}
	
	//Send Info
	public void sendInfo(RMPlayer rmp){
		rmp.sendMessage("Game id: "+ChatColor.YELLOW+_config.getId());
		rmp.sendMessage("Owner: "+ChatColor.YELLOW+_config.getOwnerName());
		rmp.sendMessage("Teams: "+getTextTeamPlayers());
		rmp.sendMessage("Players: "+ChatColor.YELLOW+getTeamPlayers().length);
		rmp.sendMessage("Max Players: "+getTextMaxPlayers());
		rmp.sendMessage("Max Team Players: "+getTextMaxTeamPlayers());
		rmp.sendMessage("Randomize items to find every match: "+getTextAutoRandomizeAmount());
		rmp.sendMessage("Warp before and after match: "+isTrueFalse(_config.getWarpToSafety()));
		rmp.sendMessage("Auto restore world after match: "+isTrueFalse(_config.getAutoRestoreWorld()));
		rmp.sendMessage("Warn when user adds hacked items: "+isTrueFalse(_config.getWarnHackedItems()));
		rmp.sendMessage("Allow user to add hacked items: "+isTrueFalse(_config.getAllowHackedItems()));
		rmp.sendMessage("Allow a player to stay in-game while disconnected: "+isTrueFalse(_config.getAllowPlayerLeave()));
		rmp.sendMessage("Clear/return player's items at game start/finish: "+isTrueFalse(_config.getClearPlayerInventory()));
		rmp.sendMessage("Allow players to join midgame: "+isTrueFalse(_config.getAllowMidgameJoin()));
	}
}
