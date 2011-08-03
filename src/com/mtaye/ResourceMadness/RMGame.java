package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import java.util.Hashtable;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;

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
		Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS
	};
	
	private static Material[] _floorMaterials = { Material.BED_BLOCK, Material.BROWN_MUSHROOM, Material.CACTUS, Material.CROPS, Material.DEAD_BUSH,
		Material.DETECTOR_RAIL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.IRON_DOOR_BLOCK, Material.LEVER, Material.LONG_GRASS,
		Material.POWERED_RAIL, Material.RAILS, Material.RED_MUSHROOM, Material.RED_ROSE, Material.REDSTONE, Material.REDSTONE_WIRE,
		Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.SAPLING, Material.SIGN_POST, Material.SNOW, Material.STONE_PLATE,
		Material.SUGAR_CANE_BLOCK, Material.TORCH, Material.WOODEN_DOOR, Material.WOOD_PLATE, Material.YELLOW_FLOWER};
	private static Material[] _sideMaterials = { Material.LADDER, Material.LEVER, Material.PAINTING, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON,
		Material.TORCH, Material.TRAP_DOOR, Material.WALL_SIGN, Material.WEB};
	
	private static Material[] _blockItemMaterials = {Material.BED_BLOCK, Material.BROWN_MUSHROOM, Material.CACTUS, Material.CROPS, Material.DEAD_BUSH,
		Material.DETECTOR_RAIL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.IRON_DOOR_BLOCK, Material.LEVER, Material.LONG_GRASS,
		Material.POWERED_RAIL, Material.RAILS, Material.RED_MUSHROOM, Material.RED_ROSE, Material.REDSTONE, Material.REDSTONE_WIRE, Material.SAPLING,
		Material.SIGN_POST, Material.SNOW, Material.STONE_PLATE, Material.SUGAR_CANE_BLOCK, Material.TORCH, Material.WOODEN_DOOR, Material.WOOD_PLATE,
		Material.YELLOW_FLOWER, Material.LADDER, Material.PAINTING, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.TRAP_DOOR,
		Material.WALL_SIGN, Material.WEB};
	
	//private List<Block> _preBlockList = new ArrayList<Block>();
	//private List<Block> _afterBlockList = new ArrayList<Block>();
	private List<List<Block>> _blockList = new ArrayList<List<Block>>();
	private HashMap<Location, RMBlock> _logBlockList = new HashMap<Location, RMBlock>();
	private HashMap<Location, RMBlock> _logBlockItemList = new HashMap<Location, RMBlock>();
	private boolean _warpToSafety = true;
	private boolean _addWholeStack = false;
	private boolean _addOnlyOneStack = false;
	private boolean _warnHackMaterials = true;
	private Boolean _autoRestoreWorld = true;
	private int _maxPlayers = 0;
	private int _maxTeamPlayers = 0;
	private int _maxItems = 0;
	private List<Material> _lastHackMaterials = new ArrayList<Material>();
	
	private int _id;
	private String _ownerName;
	private RMPlayer _owner;
	private List<RMTeam> _teams = new ArrayList<RMTeam>();
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	private RMState _state = RMState.SETUP;
	private InterfaceState _interfaceState = InterfaceState.MAIN;
	//private Inventory _inventory;
	
	private HashMap<Integer, RMItem> _filter = new HashMap<Integer, RMItem>();
	private static RMPlayer _requestPlayer;
	private int _amount;
	private final int _amountLimit = 40;
	private boolean _random = true;
	private boolean _allowHackMaterials = false;
	
	private RMItem _lastFilterRMItem;
	
	private enum Part { GLASS, STONE, CHEST, WALL_SIGN, WOOL; }
	public enum RMState { SETUP, COUNTDOWN, GAMEPLAY, GAMEOVER; }
	public enum InterfaceState { MAIN, FILTER_CLEAR };
	public int _menuItems = 0;
	public enum FilterType { ALL, CLEAR, BLOCK, ITEM, RAW, CRAFTED};
	public enum ClickState { LEFT, RIGHT, NONE };
	public enum ItemHandleState { ADD, MODIFY, REMOVE, NONE };
	public enum ForceState { ADD, REMOVE, RANDOMIZE, NONE};
	
	private final int cdTimerLimit = 300; //3 seconds
	private int cdTimer = cdTimerLimit;
	
	private HashMap<Integer, RMItem> _items = new HashMap<Integer, RMItem>();
	public HashMap<Integer, RMItem> getItemsHash(){
		return _items;
	}
	
	private RMPlayer _winner;
	
	public RMPlayer getWinner(){
		return _winner;
	}
	public void setWinner(RMPlayer rmp){
		_winner = rmp;
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

	public HashMap<Integer, RMItem> getFilterItems(){
		return _filter;
	}
	public int getFilterItemsTotal(){
		int total = 0;
		for(RMItem rmItem : _filter.values()){
			//for(Integer intAmount : amount){
				total+=rmItem.getAmount();
			//}
		}
		return total;
	}
	public int getFilterItemsTotalHigh(){
		int total = 0;
		for(RMItem rmItem : _filter.values()){
			//for(Integer intAmount : amount){
				total+=rmItem.getAmountHigh();
			//}
		}
		return total;
	}
	
	public int getItemsTotal(){
		int total = 0;
		for(RMItem rmItem : _items.values()){
			total+=rmItem.getAmount();
		}
		return total;
	}
	
	public void populateItemsByFilter(RMPlayer rmp){
		_items.clear();
		List<String> added = new ArrayList<String>();
		for(Integer item : _filter.keySet()){
			RMItem rmItem = _filter.get(item);
			int amount1 = rmItem.getAmount();
			int amount2 = rmItem.getAmountHigh();
			
			int val = amount1;
			if(amount2>0){
				val = Math.abs(amount1-amount2);
				val = (int)(Math.random()*val);
				val = amount1 + val;
				if(val<1) val = 1;
				addItem(item, new RMItem(item, val));
			}
			else{
				addItem(item, new RMItem(item, amount1));
			}
			//added.add(ChatColor.WHITE+item.toString()+includeItem(val)+ChatColor.WHITE);
		}
		//rmp.sendMessage(ChatColor.YELLOW+"Items in game: "+plugin.getFormattedStringByList(added));
	}
	
	public void startGame(RMPlayer rmp){
		if(rmp.getName()==getOwnerName()){
			for(RMChest rmChest : getChests()){
				rmChest.clearItems();
			}
			populateItemsByFilter(rmp);
			if(_items.size()>0){
				/*
				for(RMTeam rmTeam : _teams){
					if(rmTeam.getPlayers().length==0){
						rmp.sendMessage("Each team must have at least one player");
						return;
					}
				}
				*/
				rmp.sendMessage("Starting game...");
				setState(RMState.COUNTDOWN);
			}
			else rmp.sendMessage("Configure the "+ChatColor.YELLOW+"filtered items"+ChatColor.WHITE+" first.");
		}
		else rmp.sendMessage("Only the owner "+getOwnerName()+" can start the game.");
	}
	public void restartGame(RMPlayer rmp){
		if(rmp.getName()==getOwnerName()){
			rmp.sendMessage("Restarting game...");
			startGame(rmp);
		}
		else rmp.sendMessage("Only the owner "+getOwnerName()+" can restart the game.");
	}
	public void stopGame(RMPlayer rmp){
		if(rmp.getName()==getOwnerName()){
			rmp.sendMessage("Stopping game...");
			for(RMChest rmChest : getChests()){
				rmChest.clearItems();
			}
			setState(RMState.SETUP);
			updateSigns();
		}
		else rmp.sendMessage("Only the owner "+getOwnerName()+" can stop the game.");
	}

	
	//FILTER
	public HashMap<Integer, RMItem> getItemsToFilter(){
		return _filter;
	}
	
	public void clearItemsToFilter(){
		_filter.clear();
	}
	private ItemHandleState addItemToFilter(Integer i, RMItem rmItem, boolean add){
		_lastFilterRMItem = rmItem;
		if(_filter.containsKey(i)){
			if(add){
				rmItem.setAmount(rmItem.getAmount() + _filter.get(i).getAmount());
			}
			_filter.put(i, rmItem);
			return ItemHandleState.MODIFY;
		}
		_filter.put(i, rmItem);
		return ItemHandleState.ADD;
	}
	private ItemHandleState removeItemToFilter(Integer i, RMItem rmItem, boolean dec){
		if(_filter.containsKey(i)){
			int amount = _filter.get(i).getAmount();
			if(dec) amount-= rmItem.getAmount();
			if(amount>0){
				rmItem.setAmount(amount);
				_lastFilterRMItem = rmItem;
				_filter.put(i, rmItem);
				return ItemHandleState.MODIFY;
			}
			else if(amount<=0){
				_lastFilterRMItem = _filter.get(i);
				_filter.remove(i);
				return ItemHandleState.REMOVE;
			}
		}
		return ItemHandleState.NONE;
	}
	private ItemHandleState removeAlwaysItemToFilter(Integer i, RMItem rmItem){
		if(_filter.containsKey(i)){
			_lastFilterRMItem = _filter.get(i);
			_filter.remove(i);
			return ItemHandleState.REMOVE;
		}
		return ItemHandleState.NONE;
	}
	private Boolean addRemoveItemToFilter(Integer i, RMItem rmItem){
		if(!_filter.containsKey(i)){
			_lastFilterRMItem = rmItem;
			_filter.put(i, rmItem);
			return true;
		}
		else{
			_lastFilterRMItem = rmItem;
			if(rmItem.getAmountHigh()<1){ ///////////////////////////////////////////////////////////////////////////////////////////
				if(rmItem.getAmount() != _filter.get(i).getAmount()){
					_filter.put(i, rmItem);
					return true;
				}
			}
			_lastFilterRMItem = _filter.get(i);
			_filter.remove(i);
			return false;
		}
	}
	public List<Integer> addItemsToFilter(HashMap<Integer, RMItem> items){
		List<Integer> addedItems = new ArrayList<Integer>();
		if(!_allowHackMaterials){
			//if(_warnHackMaterials) warnHackMaterials(items);
			items = removeHackMaterials(items);
		}
		for(Integer item : items.keySet()){
			if(Material.getMaterial(item)!=Material.AIR){
				if(addItemToFilter(item, items.get(item), false) == ItemHandleState.ADD) addedItems.add(item);
			}
		}
		return addedItems;
	}
	public List<Integer> removeItemsToFilter(HashMap<Integer, RMItem> items){
		List<Integer> removedItems = new ArrayList<Integer>();
		if(!_allowHackMaterials){
			//if(_warnHackMaterials) warnHackMaterials(items);
			items = removeHackMaterials(items);
		}
		for(Integer item : items.keySet()){
			if(Material.getMaterial(item)!=Material.AIR){
				if(removeItemToFilter(item, items.get(item), false) == ItemHandleState.REMOVE) removedItems.add(item);
			}
		}
		return removedItems;
	}
	public List<Integer>[] addRemoveItemsToFilter(HashMap<Integer, RMItem> items){
		List<Integer> addedItems = new ArrayList<Integer>();
		List<Integer> removedItems = new ArrayList<Integer>();
		if(!_allowHackMaterials){
			//if(_warnHackMaterials) warnHackMaterials(items);
			items = removeHackMaterials(items);
		}
		for(Integer item : items.keySet()){
			if(Material.getMaterial(item)!=Material.AIR){
				if(addRemoveItemToFilter(item, items.get(item))) addedItems.add(item);
				else removedItems.add(item);
			}
		}
		List<Integer>[] allItems = new List[2];
		allItems[0] = addedItems;
		allItems[1] = removedItems;
		return allItems;
	}
	public void sortItemsToFilter(){
		/*
		if(_filter.size()!=0){
			Map.sort(_filter);
		}
		*/
	}
	
	//ITEMS TO FIND
	public void clearItems(){
		_items.clear();
	}
	public void addItem(int id, RMItem rmItem){
		if(!_items.containsKey(id)){
			_items.put(id, rmItem);
		}
	}
	public void addItems(HashMap<Integer, RMItem> items){
		for(int item : items.keySet()){
			addItem(item, items.get(item));
		}
	}
	public void removeItem(int id){
		if(_items.containsKey(id)){
			_items.remove(id);
		}
	}
	public void removeItems(HashMap<Integer, RMItem> items){
		for(int item : items.keySet()){
			removeItem(item);
		}
	}
	public void removeItems(List<Integer> items){
		for(int item : items){
			removeItem(item);
		}
	}
	public Boolean addRemoveItem(int id, RMItem rmItem){
		if(_items.containsKey(id)){
			_items.remove(id);
			return false;
		}
		else{
			_items.put(id, rmItem);
			return true;
		}
	}
	public void addRemoveItems(HashMap<Integer, RMItem> items){
		for(Integer item : items.keySet()){
			addRemoveItem(item, items.get(item));
		}
	}
	public HashMap<Integer, RMItem> getItems(){
		return _items;
	}
	
	public RMItem getItem(int id){
		return _items.get(id);
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
					int items = getFilterItems().size();
					int total = getFilterItemsTotal();
					int totalHigh = getFilterItemsTotalHigh();
					String lineTotal = ""+total;
					if(totalHigh>0) lineTotal+="-"+totalHigh;
					int length = lineTotal.length();
					if(length<9) lineTotal = "Total: "+lineTotal;
					else if(length<11) lineTotal = "Ttl: "+lineTotal;
					else if(length<13) lineTotal = "T: "+lineTotal;
					String maxPlayers = "";
					if(getMaxPlayers()!=0) maxPlayers = "/"+getMaxPlayers();
					String maxTeamPlayers = "";
					if(getMaxTeamPlayers()!=0) maxTeamPlayers = "/"+getMaxTeamPlayers();
					
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Filtered: "+items);
						sign.setLine(1, lineTotal);
						sign.setLine(2, "Joined: "+rmTeam.getPlayers().length+maxTeamPlayers);
						sign.setLine(3, "Total: "+RMTeam.getAllPlayers().length+maxPlayers);
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
					sign.setLine(2, "");
					sign.setLine(3, "");
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
		Integer[] array = _filter.keySet().toArray(new Integer[_filter.keySet().size()]);
		Arrays.sort(array);
		
		if(array.length>_amountLimit){
			for(Integer i : array){
				items += ChatColor.WHITE+""+i+includeItem(_filter.get(i))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer i : array){
				items += ChatColor.WHITE+""+Material.getMaterial(i)+includeItem(_filter.get(i))+ChatColor.WHITE+", ";
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
			RMItem rmItem = _items.get(i);
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
		if(_addOnlyOneStack) items = removeDuplicates(items);
		else{
			if(_addWholeStack) items = addDuplicates(items, true);
			else items = addDuplicates(items, false);
		}
		if(_warnHackMaterials) warnHackMaterialsListItemStack(items);
		items = removeHackMaterialsListItemStack(items);
		return items;
	}
	
	//Try Add Items
	public void tryAddItemsToFilter(Block b, RMPlayer rmp, ClickState clickState){
		if(rmp.getName() == getOwnerName()){
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
								if(_addOnlyOneStack) rmItem.setAmount(rmItem.getMaxStackSize());
								else rmItem.setAmount(is.getAmount());
								
								switch(clickState){
									case NONE: case LEFT:
										switch(addItemToFilter(id, rmItem, force)){
											case ADD:
												addedItems+=ChatColor.WHITE+item.name()+includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
										}
										break;
									case RIGHT:
										switch(removeItemToFilter(id, rmItem, force)){
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case REMOVE:
												removedItems+=ChatColor.WHITE+item.name()+includeItem(_lastFilterRMItem)+ChatColor.WHITE+", ";
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
			else if(_filter.size()>0){
				if(clickState == clickState.NONE){
					rmp.sendMessage("Click sign to clear all items.");
					setInterfaceState(InterfaceState.FILTER_CLEAR);
					//rmp.sendMessage(ChatColor.GRAY+"Removed all filter items.");
					//clearItemsToFilter();
				}
				/*
				List<String> clearedItems = new ArrayList<String>();
				for(Integer i : _filter.keySet()){
					clearedItems.add(ChatColor.WHITE+Material.getMaterial(i).name()+includeItem(_filter.get(i))+ChatColor.WHITE+", ");
				}
				if(clearedItems.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+plugin.getFormattedStringByList(clearedItems));
				*/
			}
			//else if(_filter.size()>0) setInterfaceState(InterfaceState.FILTER_CLEAR);
			updateSigns();
		}
	}
	
	public void tryAddItemToFilter(RMPlayer rmp, ClickState clickState){
		if(rmp.getName() == getOwnerName()){
			Player p = rmp.getPlayer();
			ItemStack item = p.getItemInHand();
			
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
						switch(addItemToFilter(id, rmItem, force)){
							case ADD:
								rmp.sendMessage(ChatColor.YELLOW+"Added: "+ChatColor.WHITE+mat.name()+includeItem(rmItem));
								break;
							case MODIFY:
								rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+includeItem(rmItem));
								break;
						}
						break;
					case RIGHT:
						switch(removeItemToFilter(id, rmItem, force)){
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
					if(_warnHackMaterials) warnHackMaterials(_lastHackMaterials); 
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
			HashMap<Integer, RMItem> left = new HashMap<Integer, RMItem>();
			Inventory inv = rmChest.getChest().getInventory();
			for(int i=0; i<inv.getSize(); i++){
				ItemStack item = inv.getItem(i);
				int id = item.getTypeId();
				if(item!=null){
					if(item.getType()!=Material.AIR){
						if(getItems().containsKey(id)){
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
								returned.put(id, rmChest.getItemLeft(id));
								rmp.sendMessage("getItemLeft:"+rmChest.getItemLeft(id));
							}
							if(overflow>0){
								item.setAmount(overflow);
								//if(returned.containsKey(id)) returned.put(id, returned.get(id)+overflow);
								//else returned.put(id, overflow);
							}
							else inv.clear(i);
						}
					}
				}
			}
			if(added.size()>0){
				for(Integer id : _items.keySet()){
					if(!returned.containsKey(id)){
						//RMItem rmItem = _items.get(id).clone();
						//rmItem.setAmount(-1);
						returned.put(id, new RMItem(id, -1));
					}
				}
				//trySignGameplayInfo(b, rmp);
				//rmp.sendMessage(ChatColor.YELLOW+"Items added: "+getFormattedStringByHash(added));
				if(returned.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Items left: "+getFormattedStringByHash(returned, rmp));
				//rmTeam.teamMessage(rmChest.getItemsLeft()+" items left. "+rmChest.getTotalLeft()+" total.");
			}
			else updateGameplayInfo(rmp);
			//if(returned.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Items overflown: "+getFormattedStringByHash(returned));
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
						joinTeamByBlock(b, rmp);
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
						joinTeamByBlock(b, rmp);
						break;
					}
					break;
				case FILTER_CLEAR: //FILTER CLEAR
					switch(mat){
					case CHEST: case GLASS: case STONE:
						if(rmp.getName() == getOwnerName()){
							rmp.sendMessage(ChatColor.GRAY+"Canceled.");
							setInterfaceState(InterfaceState.MAIN);
							updateSigns();
						}
						break;
					case WALL_SIGN:
						if(rmp.getName() == getOwnerName()){
							clearItemsToFilter();
							rmp.sendMessage(ChatColor.GRAY+"Filter items cleared.");
							setInterfaceState(InterfaceState.MAIN);
							updateSigns();
						}
						break;
					case WOOL:
						if(rmp.getName() == getOwnerName()){
							rmp.sendMessage(ChatColor.GRAY+"Canceled.");
							setInterfaceState(InterfaceState.MAIN);
							updateSigns();
						}
						else joinTeamByBlock(b, rmp);
						break;
					}
					break;
				}
				break;
				
			// Countdown State
			case COUNTDOWN:
				switch(mat){
				case CHEST: case WALL_SIGN: case WOOL:
					if(rmp.getName() == getOwnerName()){
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
					checkWinner(rmp);
					break;
				case WALL_SIGN:
					trySignGameplayInfo(b, rmp);
					break;
				case WOOL:
					//joinTeamByBlock(b, rmp);
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
	
	public void checkWinner(RMPlayer rmp){
		for(RMChest rmChest : getChests()){
			if(rmChest.getItemsLeftInt()==0){
				RMTeam rmTeam = rmChest.getTeam();
				setState(RMState.GAMEOVER);
				broadcastMessage(rmTeam.getTeamColorString()+ChatColor.WHITE+" team has won the match!");
				setWinner(rmp);
				update();
				return;
			}
		}
	}
	
	//UPDATE
	public void update(){
		for(RMChest rmChest : getChests()){
			switch(getState()){
				case SETUP:
					break;
				case COUNTDOWN:
					//cdTimer = 0; //////////////////////////////////////////////////////////////////////////////////////////////////////////////
					if(cdTimer%100==0){
						if(cdTimer!=0){
							sendMessage(""+cdTimer/100);
							updateSigns(""+cdTimer/100);
						}
					}
					if(cdTimer>0){
						cdTimer-=10;
					}
					else{
						sendMessage("LET THE RESOURCE MADNESS BEGIN!");
						cdTimer = cdTimerLimit;
						setState(RMState.GAMEPLAY);
						for(RMTeam rmt : getTeams()){
							for(RMPlayer rmp : rmt.getPlayers()){
								updateGameplayInfo(rmp);
							}
						}
						warpPlayersToSafety();
						updateSigns();
					}
					break;
				case GAMEPLAY:
					break;
				case GAMEOVER:
					setState(RMState.SETUP);
					if(_warpToSafety) warpPlayersToSafety();
					if(_autoRestoreWorld) restoreLog();
					updateSigns();
					break;
			}
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
	public RMGame(List<List<Block>> blockList, RMPlayer rmp, RM plugin){
		_players = RMPlayer.getPlayers();
		setId(_games.size());
		setOwner(rmp);
		_blockList = blockList;
	}
	
	//Id GET-SET
	public int getId(){
		return _id;
	}
	private void setId(int id){
		_id = id;
	}
	
	//Owner GET-SET
	public RMPlayer getOwner(){
		return _owner;
	}
	public String getOwnerName(){
		return _ownerName;
	}
	private void setOwner(RMPlayer owner){
		_owner = owner;
		_ownerName = owner.getName();
	}
	
	//Game
	private static RMGame addGame(RMGame rmGame){
		if(!_games.contains(rmGame)){
			_games.add(rmGame);
			rmGame._owner.setGames(_games);
		}
		return rmGame;
	}
	public static Boolean tryAddGame(Block b, RMPlayer rmp, Block bRemove){
		RMGame rmGame = getGameByBlock(b);
		if(b.getType()!=Material.GLASS){
			b = getCenterBlock(b);
		}
		List<List<Block>> blockList = RMGame.getBlockList(b);
		if(blockList==null) return false;
		if(bRemove!=null) blockList = RMGame.removeFromBlockList(blockList, bRemove);
		blockList = getCompleteParts(blockList);
		
		Boolean wasModified = false;
		if(rmGame==null) rmGame = RMGame.getGameByBlock(b);
		if(rmGame!=null){
			if(rmp!=rmGame.getOwner()){
				rmp.sendMessage("The owner is "+rmGame.getOwnerName()+".");
				return false;
			}
			if(!matchBlockList(blockList, rmGame.getBlockListByGroup())){
				wasModified = true;
				RMGame.removeGame(rmGame);
				rmGame = null;
			}
			else{
				if(rmGame.getTeams().size()==4){
					rmp.sendMessage("Game id "+rmGame.getId()+" has the maximum amount of teams!");
					return false;
				}
				rmp.sendMessage("Game id "+rmGame.getId()+" already exists!");
				return false;
			}
		}
		
		if(blockList.get(Part.STONE.ordinal()).size()<2){
			rmp.sendMessage("You're missing "+(2-blockList.get(Part.STONE.ordinal()).size())+" stone block." );
			return false;
		}
	
		List<RMTeam> teams = getTeamsFromBlockList(blockList);
		if(teams.size()<2){
			rmp.sendMessage("You need at least two teams to create a game");
			return false;
		}
		
		/*
		String items = "";
		for(List<Block> blocks : blockList){
			for(Block block: blocks){
				if(block!=null) items+=block.getType();
			}
		}
		plugin.getServer().broadcastMessage(""+items.substring(0, items.length()-1));
		*/
		
		rmGame = addGame(new RMGame(blockList, rmp, plugin));
		if(wasModified) rmp.sendMessage("Game id "+rmGame.getId()+" has been modified.");
		else rmp.sendMessage("Game id "+rmGame.getId()+" has been created.");

		for(RMTeam rmt : teams){
			rmGame.addTeam(rmt);
		}
		rmp.sendMessage("Found "+teams.size()+" teams. ("+rmGame.getTeamsColors()+")");
		rmGame.updateSigns();
		return true;
		
	}
	
	private static Boolean removeGame(RMGame rmGame){
		if(_games.contains(rmGame)){
			rmGame._owner.getGames().remove(rmGame);
			for(RMTeam rmt : rmGame.getTeams()){
				rmt.setNull();
			}
			_games.remove(rmGame);
			rmGame = null;
			return true;
		}
		return false;
	}
	public static Boolean tryRemoveGame(Block b, RMPlayer rmp, Boolean justRemove){
		RMGame rmGame = getGameByBlock(b);
		if(rmGame!=null){
			if(rmGame.getState() == RMState.SETUP){
				if(rmp == rmGame.getOwner()){
					if(!justRemove){
						List<List<Block>> blockList = rmGame.getBlockListByGroup();
						List<Block> blocks = RMGame.getSimpleBlockList((blockList.subList(2, blockList.size())));
						for(Block block : blocks){
							if(b == block){
								if(tryAddGame(b, rmp, b)) return true;
								break;
							}
						}
					}
					for(Sign sign : rmGame.getSigns()){
						sign.setLine(0, "");
						sign.setLine(1, "");
						sign.setLine(2, "");
						sign.setLine(3, "");
						sign.update();
					}
					rmp.sendMessage("Successfully removed game with id "+rmGame.getId());
					removeGame(rmGame);
				}
				else{
					rmp.sendMessage("The owner is "+rmGame.getOwnerName()+".");
					return false;
				}
			}
			else return false;
		}
		return true;
	}
	public static Boolean tryRemoveGame(RMGame rmGame, RMPlayer rmp){
		if(rmGame!=null){
			if(rmGame.getState() == RMState.SETUP){
				if(rmp == rmGame.getOwner()){
					//REMOVE ALL
					rmp.sendMessage("Successfully removed game with id "+rmGame.getId());
					RMGame.removeGame(rmGame);
					return true;
				}
				else{
					rmp.sendMessage("The owner is "+rmGame.getOwnerName()+".");
					return false;
				}
			}
			else{
				//plugin.getServer().broadcastMessage("GAY");
				return false;
			}
		}
		return true;
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
			for(Block rmb : game.getSimpleBlockList()){
				if(rmb == b){
					return game;
				}
			}
		}
		return null;
	}
	private static List<RMGame> getGamesByOwner(RMPlayer rmp){
		List<RMGame> games = new ArrayList<RMGame>();
		for(RMGame game : RMGame.getGames()){
			if(game._owner==rmp) games.add(game); 
		}
		return games;
	}
	private static List<RMGame> getGamesByOwnerName(String name){
		List<RMGame> games = new ArrayList<RMGame>();
		for(RMGame game : RMGame.getGames()){
			if(game._ownerName==name) games.add(game); 
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
	public HashMap<String, RMPlayer> getPlayers(){
		return _players;
	}
	public String getPlayersNames(){
		RMPlayer[] rmplayers = _players.values().toArray(new RMPlayer[_players.values().size()]);
		String names = "";
		for(RMPlayer rmp : rmplayers){
			names+=rmp.getName()+",";
		}
		return names.substring(0, names.length()-1);
	}
	public static HashMap<String, RMPlayer> getAllPlayers(){
		HashMap<String, RMPlayer> players = new HashMap<String, RMPlayer>();
		for(RMGame game : _games){
			players.putAll(game.getPlayers());
		}
		return players;
	}
	
	public void sendMessage(String message){
		RMPlayer[] rmplayers = _players.values().toArray(new RMPlayer[_players.values().size()]);
		for(RMPlayer rmp : rmplayers){
			rmp.sendMessage(message);
		}
	}

	public void broadcastMessage(String message){
		List<RMTeam> teams = getTeams();
		for(RMTeam rmt : teams){
			RMPlayer[] players = rmt.getPlayers();
			for(RMPlayer rmp : players){
				rmp.sendMessage(message);
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
			rmp.sendMessage("You quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team.");
		}
	}
	public RMTeam joinTeamByBlock(Block b, RMPlayer rmp){
		if(getState() == RMState.SETUP){
			RMTeam rmt = getTeamByBlock(b);
			if(rmt!=null){
				RMTeam rmTeam = getPlayerTeam(rmp);
				if(rmTeam!=null){
					if(rmt!=rmTeam){
						rmp.sendMessage("You must quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team first.");
						return null;
					}
				}
				rmt.addPlayer(rmp);
				return rmt;
			}
			else rmp.sendMessage("This team does not exist!");
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
			List<List<Block>> blockList = getBlockListByGroup();
			blockList = blockList.subList(2, blockList.size());
			int i=0;
			for(List<Block> blocks : blockList){
				if(blocks.contains(b)){
					return getTeam(i);
				}
				i++;
			}
		}
		return null;
	}
	
	private RMTeam addTeam(RMTeam rmt){
		if(!_teams.contains(rmt)){
			_teams.add(rmt);
			rmt.setGame(this);
		}
		return rmt;
	}
	public RMTeam getTeam(int index){
		if(_teams.get(index)!=null) return _teams.get(index);
		return null;
	}
	public List<RMTeam> getTeams(){
		return _teams;
	}
	public static RMTeam getTeam(RMGame rmGame, int index){
		if(rmGame._teams.size()>index) return rmGame._teams.get(index);
		return null;
	}
	public static List<RMTeam> getTeams(RMGame rmGame){
		return rmGame._teams;
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
	public static List<RMChest> getChestsFromBlockList(List<List<Block>> bList){
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
			List<List<Block>> blockList = getBlockListByGroup();
			blockList = blockList.subList(2, blockList.size());
			int i=0;
			for(List<Block> blocks : blockList){
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
			List<List<Block>> blockList = getBlockListByGroup();
			blockList = blockList.subList(2, blockList.size());
			int i=0;
			for(List<Block> blocks : blockList){
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
	public String getTeamsColors(){
		String line = "";
		for(RMTeam team : _teams){
			if(team.getTeamColor()!=null){
				line+=team.getTeamColorString()+ChatColor.WHITE+",";
			}
		}
		return line.substring(0,line.length()-1);
	}
	public RMTeam getPlayerTeam(RMPlayer rmp){
		for(RMTeam team : _teams){
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
	
	public String getTeamsPlayers(){
		String line = "";
		for(RMTeam team : _teams){
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
			teams.addAll(game._teams);
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
	public static Part getPartByMaterial(Material mat){
		switch(mat){
			case GLASS:
				return Part.GLASS;
			case STONE:
				return Part.STONE;
			case CHEST:
				return Part.CHEST;
			case WALL_SIGN:
				return Part.WALL_SIGN;
			case WOOL:
				return Part.WOOL;
		}
		return null;
	}

	//Block List
	public List<List<Block>> getBlockListByGroup(){
		return _blockList;
	}
	public List<Block> getSimpleBlockList(){
		List<Block> blockList = new ArrayList<Block>();
			for(List<Block> blocks : getBlockListByGroup()){
				for(Block b : blocks){
					blockList.add(b);
				}
			}
		return blockList;
	}
	public static List<Block> getSimpleBlockList(List<List<Block>> bList){
		List<Block> blockList = new ArrayList<Block>();
			for(List<Block> blocks : bList){
				for(Block b : blocks){
					blockList.add(b);
				}
			}
		return blockList;
	}
	public static List<List<Block>> getBlockList(Block b){
		if(b!=null){
			List<List<Block>> blockList = new ArrayList<List<Block>>();
			blockList.add(getParts(b, Material.GLASS, true, b));
			blockList.add(getParts(b, Material.STONE, true, b.getRelative(BlockFace.UP), b.getRelative(BlockFace.DOWN)));
			blockList.add(getParts(b.getRelative(BlockFace.DOWN), Material.CHEST, false));
			blockList.add(getParts(b, Material.WALL_SIGN, false));
			blockList.add(getParts(b.getRelative(BlockFace.UP), Material.WOOL, false));
			return blockList;
		}
		return null;
	}
	public static String getStringBlockList(List<List<Block>> blockList, Boolean getNull){
		return getStringSimpleBlockList(getSimpleBlockList(blockList), getNull );
	}
	public static String getStringSimpleBlockList(List<Block> blocks, Boolean getNull){
		String list = "";
		for(Block block : blocks){
			if(block!=null)	list+=block.getType().name();
			else if(getNull) list+="null";
			list+=",";
		}
		return list.substring(0,list.length()-1);
	}
	
	private List<List<Block>> removeFromBlockList(Block b){
		List<List<Block>> blockList = getBlockListByGroup();
		return removeFromBlockList(blockList, b);
	}
	private static List<List<Block>> removeFromBlockList(List<List<Block>> blockList, Block b){
		for(int i=0;i<blockList.size();i++){
			for(int j=0;j<blockList.get(i).size();j++){
				if(blockList.get(i).get(j)==b){
					blockList.get(i).set(j, null);
				}
			}
		}
		return blockList;
	}
	private static List<Block> getParts(Block b, Material mat, Boolean trim, Block... faces){
		List<Block> blocks = new ArrayList<Block>();
		List<Block> facings = new ArrayList<Block>();
		if(faces.length==0){
			facings.add(b.getRelative(BlockFace.NORTH));
			facings.add(b.getRelative(BlockFace.EAST));
			facings.add(b.getRelative(BlockFace.SOUTH));
			facings.add(b.getRelative(BlockFace.WEST));
		}
		else{
			for(int i=0; i<faces.length; i++){
				facings.add(faces[i]);
			}
		}
		if(mat==Material.WALL_SIGN){
				for(int i=0; i<facings.size(); i++){
					if((facings.get(i).getType()==Material.WALL_SIGN)&&(RMDir.getDirByData(facings.get(i).getData()).getOpposite()==RMDir.values()[i])){
						blocks.add(facings.get(i));
					}
					else if(!trim){
						blocks.add(null);
					}
				}
		}
		else{
			for(int i=0; i<facings.size(); i++){
				if(facings.get(i).getType() == mat){
					blocks.add(facings.get(i));
				}
				else if(!trim){
					blocks.add(null);
				}
			}
		}
		return blocks;
	}
	
	private static List<List<Block>> getCompleteParts(List<List<Block>> blockList){
		List<List<Block>> parts = new ArrayList<List<Block>>(blockList.subList(0, 2));
		blockList = blockList.subList(2, blockList.size());
		List<Byte> data = new ArrayList<Byte>();
		for(int i=0; i<4; i++){
			List<Block> partCount = new ArrayList<Block>();
			for(int j=0; j<blockList.size(); j++){
				Block b = blockList.get(j).get(i); 
				if(b!=null){
					partCount.add(b);
				}
			}
			if(partCount.size()==3){
				List<Block> blocks = new ArrayList<Block>();
				for(int j=0; j<partCount.size(); j++){
					blocks.add(partCount.get(j));
				}
				Byte d = blocks.get(Part.WOOL.ordinal()-2).getData();
				if(!data.contains(d)){
					data.add(d);
					parts.add(blocks);
				}
			}
		}
		return parts;
	}
	public static List<RMTeam> getTeamsFromBlockList(List<List<Block>> blockList){
		blockList = blockList.subList(2, blockList.size());
		List<RMTeam> teams = new ArrayList<RMTeam>();
		for(List<Block> blocks : blockList){
			teams.add(new RMTeam(DyeColor.getByData(blocks.get(Part.WOOL.ordinal()-2).getData()), (Chest)blocks.get(Part.CHEST.ordinal()-2).getState(), plugin));
		}
		return teams;
	}
	
	//Get Center Block
	private static Block getCenterBlock(Block b){
		switch(b.getType()){
			case WALL_SIGN:
				RMDir dir = RMDir.getDirByData(b.getData());
				Block c = getSignBlock(b, dir);
				if(c.getType()==Material.GLASS) return c;
				return null;
			case CHEST:
				b = b.getRelative(BlockFace.UP);
				return getCenterBlockByFace(b);
			case WOOL:
				b = b.getRelative(BlockFace.DOWN);
				return getCenterBlockByFace(b);
			case STONE:
				return getCenterBlockByFace(b, b.getRelative(BlockFace.UP),b.getRelative(BlockFace.DOWN));
			}
		return null;
	}
	private static Block getCenterBlockByFace(Block b, Block... faces){
		List<Block> facings;
		facings = new ArrayList<Block>();
		if(faces.length==0){
		facings.add(b.getRelative(BlockFace.NORTH));
		facings.add(b.getRelative(BlockFace.EAST));
		facings.add(b.getRelative(BlockFace.SOUTH));
		facings.add(b.getRelative(BlockFace.WEST));
		}
		else{
			for(int i=0; i<faces.length; i++){
			facings.add(faces[i]);
			}
		}
		for(Block face : facings){
			if(face.getType()==Material.GLASS) return face;
		}
		return null;
	}
	
	//Get Sign Block
	public static Block getSignBlock(Block b, RMDir dir){
		switch(dir){
			case NORTH:
				return b.getRelative(BlockFace.NORTH);
			case EAST:
				return b.getRelative(BlockFace.EAST);
			case SOUTH:
				return b.getRelative(BlockFace.SOUTH);
			case WEST:
				return b.getRelative(BlockFace.WEST);
		}
		return null;
	}
	
	public static Boolean matchBlockList(List<List<Block>> bl1, List<List<Block>> bl2){
		List<Block> blocks1 = RMGame.getSimpleBlockList(bl1);
		List<Block> blocks2 = RMGame.getSimpleBlockList(bl2);
		
		if(blocks1.size()!=blocks2.size()) return false;
		for(int i=0; i<blocks1.size(); i++){
			if(blocks1.get(i)!=blocks2.get(i))
			{
				return false;
			}
		}
		return true;
	}
	
	public static Boolean matchSimpleBlockList(List<Block> blocks1, List<Block> blocks2){
		if(blocks1.size()!=blocks2.size()) return false;
		for(int i=0; i<blocks1.size(); i++){
			if(blocks1.get(i)!=blocks2.get(i))
			{
				return false;
			}
		}
		return true;
	}
	
	/*
	//OTHER
	//BLOCK LIST
	public void addBlock(Block b){
		if(!_preBlockList.contains(b)){
			_preBlockList.add(b);
		}
	}
	public void removeBlock(Block b){
		if(_preBlockList.contains(b)){
			_preBlockList.remove(b);
		}
	}
	public void replaceBlock(Block rb, Block b){
		if(_preBlockList.contains(rb)){
			_preBlockList.set(_preBlockList.indexOf(rb), b);
		}
	}
	//BLOCK LIST//END//
	
	*/
	public void save(){
	}
	public void load(){
	}
	//Options
	/*
	public void setFilter(String filter){
		if(filter!=null){
			if(filter.length()>1){
				_filter.clear();
				String[] list = filter.split(",");
				for(String item : list){
					try{
						int i = Integer.valueOf(item);
						Material mat = Material.getMaterial(i);
						if(mat!=null) _filter.add(mat);
					}
					catch (Exception e){
						plugin.log.log(Level.SEVERE, "Error:"+e);
						e.printStackTrace();
					}
				}
			}
		}
	}
	public void addFilterItemById(int id){
		Material item = Material.getMaterial(id);
		if(item!=null) addFilterItem(item);
	}
	public void addFilterItem(Material mat){
		if(!_filter.contains(mat)){
			_filter.add(mat);
		}
		else _filter.remove(mat);
	}
	*/
	//Amount
	public int getAmount(){
		return _amount;
	}
	public void setAmount(int amount){
		_amount = amount;
		if(_amount<0) _amount = 0;
		else if(_amount>20) _amount = _amountLimit;
	}
	//Random
	public Boolean getRandom(){
		return _random;
	}
	public void setRandom(Boolean random){
		_random = random;
	}
	//HackMaterials
	public boolean getAllowHackMaterials(){
		return _allowHackMaterials;
	}
	public void setAllowHackMaterials(Boolean allow){
		_allowHackMaterials = allow;
	}
	
	//ParseFilter
	public Boolean tryParseFilter(RMPlayer rmp){
		if(rmp.getName() == getOwnerName()){
			RMGame.setRequestPlayer(rmp);
			parseFilter(rmp);
			RMGame.clearRequestPlayer();
			rmp.clearRequestFilter();
			updateSigns();
			return true;
		}
		else rmp.sendMessage("Only the owner "+getOwnerName()+" can modify the filter.");
		return false;
	}
	
	
	private void parseFilter(RMPlayer rmp){
		RMRequestFilter filter = rmp.getRequestFilter();
		if(filter!=null){
			ForceState force = filter.getForce();
			int randomize = filter.getRandomize();
			HashMap<Integer, RMItem> items = filter.getItems();
			if((items!=null)&&(items.size()!=0)){
				if(!_allowHackMaterials){
					if(_warnHackMaterials) warnHackMaterials(items);
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
					clearItemsToFilter();
					rmp.sendMessage(ChatColor.GRAY+"Filter cleared.");
					return;
				}
			}
		}
		rmp.sendMessage(ChatColor.GRAY+"No items modified.");
	}
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, RMItem> items){
		List<String> added = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		List<String> modified = new ArrayList<String>();
		String strItem;
		Boolean getId = false;
		if(items.size()>_amountLimit) getId=true;
		Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(arrayItems);
		for(Integer item : arrayItems){
			Material mat = Material.getMaterial(item);
			if(mat!=Material.AIR){
				if(getId) strItem = ""+item;
				else strItem = mat.name();
				Integer amount = items.get(item).getAmount();
				if(amount!=0){
					switch(addItemToFilter(item, items.get(item), false)){
						case ADD:
							added.add(ChatColor.WHITE+strItem+includeItem(items.get(item)));
							break;
						case MODIFY:
							modified.add(ChatColor.WHITE+strItem+includeItem(items.get(item)));	
							break;
					}
				}
				else{
					switch(removeItemToFilter(item, items.get(item), false)){
						case REMOVE:
							removed.add(ChatColor.WHITE+strItem+includeItem(_lastFilterRMItem));
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
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, RMItem> items, ForceState force){
		List<String> added = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		String strItem;
		Boolean getId = false;
		if(items.size()>_amountLimit) getId=true;
		Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(arrayItems);

		switch(force){
		case ADD: case RANDOMIZE:
			for(Integer item : arrayItems){
				Material mat = Material.getMaterial(item);
				if(mat!=Material.AIR){
					if(getId) strItem = ""+item;
					else strItem = mat.name();
					switch(addItemToFilter(item, items.get(item), false)){
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
					switch(removeAlwaysItemToFilter(item, items.get(item))){
						case REMOVE:
							removed.add(ChatColor.WHITE+strItem+includeItem(_lastFilterRMItem)+ChatColor.WHITE);
							break;
					}
				}
			}
			if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+plugin.getFormattedStringByList(removed));
			break;
		}
	}
	
	public String getFormattedStringByHash(HashMap<Integer, RMItem> items, RMPlayer rmp){
		RMChest rmChest = rmp.getTeam().getChest();
		String line = "";
		for(Integer item : items.keySet()){
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
				else amount = _items.get(item).getAmount();
				if(amount!=0) line+=ChatColor.WHITE+Material.getMaterial(item).name()+includeItem(new RMItem(item, amount))+ChatColor.WHITE+", ";
			}
		}
		line = plugin.stripLast(line, ",");
		return line;
	}
	
	public String includeItem(RMItem rmItem, boolean... less){
		int i1 = rmItem.getAmount();
		int i2 = rmItem.getAmountHigh();
		if((i1!=1)&&(less.length==0)){
			if(i2>0) return ChatColor.GRAY+":"+i1+"-"+i2;
			return ChatColor.GRAY+":"+i1;
		}
		return "";
	}
	
	public List<Material> findHackMaterials(ItemStack[] items){
		List<Material> materials = Arrays.asList(_hackMaterials);
		List<Material> list = new ArrayList<Material>();
		for(ItemStack item : items){
			Material mat = item.getType();
			if(materials.contains(mat)) list.add(mat);
		}
		return list;
	}
	
	public List<Material> findHackMaterials(HashMap<Integer, RMItem> materials){
		List<Material> list = new ArrayList<Material>();
		for(Material mat : _hackMaterials){
			if(materials.containsKey(mat.getId())) list.add(mat);
		}
		return list;
	}
	public List<Material> findHackMaterials(List<Material> items){
		List<Material> list = new ArrayList<Material>();
		for(Material mat : _hackMaterials){
			if(items.contains(mat)) list.add(mat);
		}
		return list;
	}
	public List<Material> findHackMaterialsListItemStack(List<ItemStack> items){
		List<Material> materials = Arrays.asList(_hackMaterials);
		List<Material> list = new ArrayList<Material>();
		for(ItemStack item : items){
			Material mat = item.getType();
			if(materials.contains(mat)) list.add(mat);
		}
		return list;
	}
	
	public HashMap<Integer, RMItem> removeHackMaterials(HashMap<Integer, RMItem> materials){
		for(Material mat : _hackMaterials) materials.remove(mat.getId());
		return materials;
	}
	
	public List<ItemStack> removeHackMaterialsListItemStack(List<ItemStack> items){
		List<Material> materials = Arrays.asList(_hackMaterials);
		List<ItemStack> list = new ArrayList<ItemStack>();
		for(ItemStack item : items){
			Material mat = item.getType();
			if(!materials.contains(mat)) list.add(item);
		}
		return list;
	}
	
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
		return newItems;
	}
			
	public void addLog(BlockState bState){
		Block b = bState.getBlock();
		Material mat = bState.getType();
		if(isMaterial(mat, _blockItemMaterials)){
			if(!_logBlockItemList.containsKey(bState.getBlock().getLocation())) _logBlockItemList.put(b.getLocation(), new RMBlock(bState));
		}
		else if(!_logBlockList.containsKey(bState.getBlock().getLocation())) _logBlockList.put(b.getLocation(), new RMBlock(bState));
		checkLog(b);
	}
	
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
	public void clearLog(){
		_logBlockList.clear();
		_logBlockItemList.clear();
	}
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
	
	public void warnHackMaterials(ItemStack[] items){
		warnHackMaterialsMessage(plugin.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
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
		if(message.length()>0) getOwner().sendMessage(ChatColor.RED+"Not allowed: "+message);
	}
	
	public int getMaxPlayers(){
		return _maxPlayers;
	}
	public void setMaxPlayers(RMPlayer rmp, int maxPlayers){
		_maxPlayers = maxPlayers;
		if(_maxPlayers<-1) _maxPlayers = 0;
		rmp.sendMessage("Max players: "+_maxPlayers);
		updateSigns();
	}
	public int getMaxTeamPlayers(){
		return _maxTeamPlayers;
	}
	public void setMaxTeamPlayers(RMPlayer rmp, int maxTeamPlayers){
		_maxTeamPlayers = maxTeamPlayers;
		if(_maxTeamPlayers<-1) _maxTeamPlayers = 0;
		rmp.sendMessage("Max team players: "+_maxTeamPlayers);
		updateSigns();
	}
	public int getMaxItems(){
		return _maxItems;
	}
	public void setMaxItems(RMPlayer rmp, int maxItems){
		_maxItems = maxItems;
		if(_maxItems<-1) _maxItems = 0;
		updateSigns();
	}
	public void toggleAutoRestoreWorld(RMPlayer rmp){
		if(_autoRestoreWorld) _autoRestoreWorld = false;
		else _autoRestoreWorld = true;
		rmp.sendMessage("Auto restore: "+_autoRestoreWorld);
	}
	public void restoreWorld(RMPlayer rmp){
		restoreLog();
	}
	public void sendInfo(RMPlayer rmp){
		rmp.sendMessage("Game id: "+ChatColor.YELLOW+getId());
		rmp.sendMessage(ChatColor.GRAY+"Owner:"+getOwnerName()+ChatColor.WHITE+" Teams:"+getTeamsPlayers());
		rmp.sendMessage("maxplayers: "+getMaxPlayers());
		rmp.sendMessage("maxteamplayers: "+getMaxTeamPlayers());
		rmp.sendMessage("warptosafety: "+_warpToSafety);
		rmp.sendMessage("warnhackmaterials: "+_warnHackMaterials);
		rmp.sendMessage("autorestoreworld: "+_autoRestoreWorld);
	}
}
