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
		Material.COAL_ORE, Material.SPONGE, Material.LAPIS_ORE, Material.BED, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.DOUBLE_STEP,
		Material.FIRE, Material.MOB_SPAWNER, Material.REDSTONE_WIRE, Material.DIAMOND_ORE, Material.CROPS, Material.SOIL, Material.BURNING_FURNACE,
		Material.SIGN_POST, Material.WOODEN_DOOR, Material.WALL_SIGN, Material.IRON_DOOR_BLOCK, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
		Material.REDSTONE_TORCH_OFF, Material.SNOW_BLOCK, Material.ICE, Material.SUGAR_CANE_BLOCK, Material.PORTAL, Material.CAKE_BLOCK,
		Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.LOCKED_CHEST, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
		Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS
	};
	
	//private List<Block> _preBlockList = new ArrayList<Block>();
	//private List<Block> _afterBlockList = new ArrayList<Block>();
	private List<List<Block>> _blockList = new ArrayList<List<Block>>();
	private List<RMBlock> _logBlockList = new ArrayList<RMBlock>();
	private boolean _restoreWorld = true;
	private boolean _warpToSafety = true;
	private boolean _addWholeStack = false;
	private boolean _warnHackMaterials = true;
	private List<Material> _lastHackMaterials = new ArrayList<Material>();
	
	private int _id;
	private String _ownerName;
	private RMPlayer _owner;
	private List<RMTeam> _teams = new ArrayList<RMTeam>();
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	private RMState _state = RMState.SETUP;
	private InterfaceState _interfaceState = InterfaceState.MAIN;
	//private Inventory _inventory;
	
	private HashMap<Integer, Integer[]> _filter = new HashMap<Integer, Integer[]>();
	private static RMPlayer _requestPlayer;
	private int _amount;
	private final int _amountLimit = 40;
	private boolean _random = true;
	private boolean _allowHackMaterials = false;
	
	private Material _lastFilterMaterial = Material.AIR;
	private Integer[] _lastFilterAmount = new Integer[2];
	
	private enum Part { GLASS, STONE, CHEST, WALL_SIGN, WOOL; }
	public enum RMState { SETUP, COUNTDOWN, GAMEPLAY, GAMEOVER; }
	public enum InterfaceState { MAIN, FILTER_CLEAR };
	public int _menuItems = 0;
	public enum FilterType { ALL, CLEAR, BLOCK, ITEM, RAW, CRAFTED};
	
	private final int cdTimerLimit = 300; //3 seconds
	private int cdTimer = cdTimerLimit;
	
	private HashMap<Integer, Integer> _items = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> getItemsHash(){
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

	public HashMap<Integer, Integer[]> getFilterItems(){
		return _filter;
	}
	public int getFilterItemsTotal(){
		int total = 0;
		for(Integer[] amount : _filter.values()){
			//for(Integer intAmount : amount){
				total+=amount[0];
			//}
		}
		return total;
	}
	
	public int getItemsTotal(){
		int total = 0;
		for(Integer amount : _items.values()){
			total+=amount;
		}
		return total;
	}
	
	public void populateItemsByFilter(RMPlayer rmp){
		_items.clear();
		List<String> added = new ArrayList<String>();
		for(Integer item : _filter.keySet()){
			Integer[] amount = _filter.get(item);
			int val = amount[0];
			if(amount.length>1){
				val = Math.abs(amount[0]-amount[1]);
				val = (int)(Math.random()*val);
				val = amount[0] + val;
				if(val<1) val = 1;
				addItem(item, val);
			}
			else{
				addItem(item, amount[0]);
			}
			//added.add(ChatColor.WHITE+item.toString()+includeAmount(val)+ChatColor.WHITE);
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
		}
		else rmp.sendMessage("Only the owner "+getOwnerName()+" can stop the game.");
	}

	
	//FILTER
	public HashMap<Integer, Integer[]> getItemsToFilter(){
		return _filter;
	}
	
	public void clearItemsToFilter(){
		_filter.clear();
	}
	private Boolean addItemToFilter(Integer i, Integer[] amount){
		_lastFilterMaterial = Material.getMaterial(i);
		_lastFilterAmount = amount;
		if(_filter.containsKey(i)){
			_filter.put(i, amount);
			return false;
		}
		_filter.put(i, amount);
		return true;
	}
	private Boolean removeItemToFilter(Integer i){
		if(_filter.containsKey(i)){
			_lastFilterMaterial = Material.getMaterial(i);
			_lastFilterAmount = _filter.get(i);
			_filter.remove(i);
			return true;
		}
		return false;
	}
	private Boolean addRemoveItemToFilter(Integer i, Integer[] amount){
		if(!_filter.containsKey(i)){
			_lastFilterMaterial = Material.getMaterial(i);
			_lastFilterAmount = amount;
			_filter.put(i, amount);
			return true;
		}
		else{
			_lastFilterMaterial = Material.getMaterial(i);
			if(amount.length==1){
				if(amount[0].intValue() != _filter.get(i)[0].intValue()){
					_lastFilterAmount = amount;
					_filter.put(i, amount);
					return true;
				}
			}
			_lastFilterAmount = _filter.get(i);
			_filter.remove(i);
			return false;
		}
	}
	public List<Integer> addItemsToFilter(HashMap<Integer, Integer[]> items){
		List<Integer> addedItems = new ArrayList<Integer>();
		if(!_allowHackMaterials){
			//if(_warnHackMaterials) warnHackMaterials(items);
			items = removeHackMaterials(items);
		}
		for(Integer item : items.keySet()){
			if(Material.getMaterial(item)!=Material.AIR){
				if(addItemToFilter(item, items.get(item))) addedItems.add(item);
			}
		}
		return addedItems;
	}
	public List<Integer> removeItemsToFilter(HashMap<Integer, Integer[]> items){
		List<Integer> removedItems = new ArrayList<Integer>();
		if(!_allowHackMaterials){
			//if(_warnHackMaterials) warnHackMaterials(items);
			items = removeHackMaterials(items);
		}
		for(Integer item : items.keySet()){
			if(Material.getMaterial(item)!=Material.AIR){
				if(removeItemToFilter(item)) removedItems.add(item);
			}
		}
		return removedItems;
	}
	public List<Integer>[] addRemoveItemsToFilter(HashMap<Integer, Integer[]> items){
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
	public void addItem(int id, int amount){
		if(!_items.containsKey(id)){
			_items.put(id, amount);
		}
	}
	public void addItems(HashMap<Integer, Integer> items){
		for(int item : items.keySet()){
			addItem(item, items.get(item));
		}
	}
	public void removeItem(int id){
		if(_items.containsKey(id)){
			_items.remove(id);
		}
	}
	public void removeItems(HashMap<Integer, Integer> items){
		for(int item : items.keySet()){
			removeItem(item);
		}
	}
	public void removeItems(List<Integer> items){
		for(int item : items){
			removeItem(item);
		}
	}
	public Boolean addRemoveItem(int id, int amount){
		if(_items.containsKey(id)){
			_items.remove(id);
			return false;
		}
		else{
			_items.put(id, amount);
			return true;
		}
	}
	public void addRemoveItems(HashMap<Integer, Integer> items){
		for(Integer item : items.keySet()){
			addRemoveItem(item, items.get(item));
		}
	}
	public HashMap<Integer, Integer> getItems(){
		return _items;
	}
	
	public int getItem(int id){
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
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Filtered: "+items);
						sign.setLine(1, "Total: "+total);
						sign.setLine(2, "Players: "+rmTeam.getPlayers().length);
						sign.setLine(3, ""+rmTeam.getPlayers().length);
						sign.update();
					}
					break;
				case FILTER_CLEAR:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Clear filter items?");
						sign.setLine(1, "Wool = NO");
						sign.setLine(2, "Sign = YES");
						sign.setLine(3, "Chest = NO");
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
				items += ChatColor.WHITE+""+i+includeAmount(_filter.get(i))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer i : array){
				items += ChatColor.WHITE+""+Material.getMaterial(i)+includeAmount(_filter.get(i))+ChatColor.WHITE+", ";
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
			updateSigns();
			updateGameplayInfo(rmp);
		}
	}
	
	public void updateGameplayInfo(RMPlayer rmp){
		RMTeam rmTeam = rmp.getTeam();
		String strItems = "";
		RMChest rmChest = rmTeam.getChest();
		
		//Sort
		Integer[] array = _items.keySet().toArray(new Integer[_items.keySet().size()]);
		Arrays.sort(array);
		
		HashMap<Integer, Integer> items = rmChest.getItems();
		
		for(Integer i : array){
			int amount = _items.get(i);
			if(items.containsKey(i)) amount -= items.get(i);
			if(amount>0) strItems += ChatColor.WHITE+""+Material.getMaterial(i)+includeAmount(amount)+ChatColor.WHITE+", ";
		}
		if(strItems.length()>0){
			strItems = strItems.substring(0, strItems.length()-2);
			rmp.sendMessage(ChatColor.YELLOW+"Items left: "+strItems);
		}
		else rmp.sendMessage(ChatColor.YELLOW+"No items left.");
	}
	
	//Try Add Items
	public void tryAddItemsToFilter(RMPlayer rmp){
		if(rmp.getName() == getOwnerName()){
			List<ItemStack> items = new ArrayList<ItemStack>();
			for(RMChest rmChest : getChests()){
				ItemStack[] chestItems = rmChest.getChest().getInventory().getContents();
				if(_addWholeStack) chestItems = removeDuplicates(chestItems);
				else chestItems = addDuplicates(chestItems, true);
				items.addAll(Arrays.asList(chestItems));
			}
			String addedItems = "";
			String modifiedItems = "";
			if(_warnHackMaterials) warnHackMaterialsListItemStack(items);
			items = removeHackMaterialsListItemStack(items);
			if(items.size()>0){
				for(int i=0; i<items.size(); i++){
					ItemStack is = items.get(i);
					if(is != null){
						Material item = is.getType();
						if(item!=null){
							if(item!=Material.AIR){
								Integer[] intAmount = new Integer[1];
								
								if(_addWholeStack) intAmount[0] = item.getMaxStackSize();
								else intAmount[0] = is.getAmount();
								
								if(addItemToFilter(item.getId(), intAmount)){
									addedItems+=ChatColor.WHITE+item.name()+includeAmount(intAmount)+ChatColor.WHITE+", ";
								}
								else{
									modifiedItems+=ChatColor.WHITE+item.name()+includeAmount(intAmount)+ChatColor.WHITE+", ";
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
			}
			//else if(_filter.size()>0){
				//rmp.sendMessage(ChatColor.GRAY+"Removed all filter items.");
				//clearItemsToFilter();
				/*
				List<String> clearedItems = new ArrayList<String>();
				for(Integer i : _filter.keySet()){
					clearedItems.add(ChatColor.WHITE+Material.getMaterial(i).name()+includeAmount(_filter.get(i))+ChatColor.WHITE+", ");
				}
				if(clearedItems.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+plugin.getFormattedStringByList(clearedItems));
				*/
			//}
			//else if(_filter.size()>0) setInterfaceState(InterfaceState.FILTER_CLEAR);
			updateSigns();
		}
	}
	
	public void tryAddItemToFilter(RMPlayer rmp){
		if(rmp.getName() == getOwnerName()){
			Player p = rmp.getPlayer();
			ItemStack item = p.getItemInHand();
			if(item!=null){
				int id = item.getTypeId();
				Material mat = item.getType();
				if(!isMaterial(mat, _hackMaterials)){
					Integer[] amount;
					if(_filter.containsKey(id)){
						amount = _filter.get(id);
						amount[0]+=1;
					}
					else{
						amount = new Integer[1];
						amount[0] = 1;
					}
					addItemToFilter(id, amount);
					rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+includeAmount(amount[0]));
					updateSigns();
				}
				else{
					_lastHackMaterials.clear();
					_lastHackMaterials.add(mat);
					if(_warnHackMaterials) warnHackMaterials(_lastHackMaterials); 
				}
			}
			else tryAddItemsToFilter(rmp);
		}
	}
	
	public void tryAddFoundItems(Block b, RMPlayer rmp){
		RMTeam rmTeam = getTeamByBlock(b);
		RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
		if((rmTeam!=null)&&(rmPlayer!=null)){
			RMChest rmChest = rmTeam.getChest();
			HashMap<Integer, Integer> added = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> returned = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> left = new HashMap<Integer, Integer>();
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
								if(added.containsKey(id)) added.put(id, added.get(id)+(item.getAmount()-overflow));
								else added.put(id, item.getAmount()-overflow);
								returned.put(id, rmChest.getItemLeft(id));
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
						returned.put(id, -1);
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
		
	public void handleLeftClick(Block b, RMPlayer rmp){
		Material mat = b.getType();
		switch(getState()){
		
			// Setup State
			case SETUP:
				switch(getInterfaceState()){
				case MAIN:
					switch(mat){
					case CHEST:
						tryAddItemsToFilter(rmp);
						break;
					case WALL_SIGN:
						trySignSetupInfo(rmp);
						break;
					case WOOL:
						joinTeamByBlock(b, rmp);
						break;
					}
					break;
				case FILTER_CLEAR:
					switch(mat){
					case CHEST:
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
						updateSigns();
					}
					break;
				case GAMEPLAY:
					break;
				case GAMEOVER:
					setState(RMState.SETUP);
					if(_warpToSafety) warpPlayersToSafety();
					if(_restoreWorld) restoreLog();
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
	public List<RMChest> getChests(){
		List<RMChest> chestList = new ArrayList<RMChest>();
		for(RMTeam rmt : getTeams()){
			chestList.add(rmt.getChest());
		}
		return chestList;
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
			Boolean force = filter.getForce();
			HashMap<Integer, Integer[]> items = filter.getItems();
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
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, Integer[]> items){
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
				Integer[] amount = items.get(item);
				if(amount[0]!=0){
					if(addItemToFilter(item, items.get(item))) added.add(ChatColor.WHITE+strItem+includeAmount(items.get(item)));
					else modified.add(ChatColor.WHITE+strItem+includeAmount(items.get(item)));	
				}
				else if(removeItemToFilter(item)) removed.add(ChatColor.WHITE+strItem+includeAmount(_lastFilterAmount));
				//if(addRemoveItemToFilter(item, items.get(item))) added.add(ChatColor.WHITE+strItem+includeAmount(items.get(item)));
				//else removed.add(ChatColor.WHITE+strItem+includeAmount(items.get(item)));
			}
		}
		if(added.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+plugin.getFormattedStringByList(added));
		if(modified.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+plugin.getFormattedStringByList(modified));
		if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+plugin.getFormattedStringByList(removed));
	}
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, Integer[]> items, Boolean force){
		List<String> added = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		String strItem;
		Boolean getId = false;
		if(items.size()>_amountLimit) getId=true;
		Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(arrayItems);
		if(force){
			for(Integer item : arrayItems){
				Material mat = Material.getMaterial(item);
				if(mat!=Material.AIR){
					if(getId) strItem = ""+item;
					else strItem = mat.name();
					if(addItemToFilter(item, items.get(item))) added.add(ChatColor.WHITE+strItem+includeAmount(items.get(item))+ChatColor.WHITE);
				}
			}
			if(added.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+plugin.getFormattedStringByList(added));
		}
		else{
			for(Integer item : arrayItems){
				Material mat = Material.getMaterial(item);
				if(mat!=Material.AIR){
					if(getId) strItem = ""+item;
					else strItem = mat.name();
					if(removeItemToFilter(item)) removed.add(ChatColor.WHITE+strItem+includeAmount(_lastFilterAmount)+ChatColor.WHITE);
				}
			}
			if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+plugin.getFormattedStringByList(removed));
		}
	}
	
	public String getFormattedStringByHash(HashMap<Integer, Integer> items, RMPlayer rmp){
		RMChest rmChest = rmp.getTeam().getChest();
		String line = "";
		for(Integer item : items.keySet()){
			int amount = items.get(item); 
			if(amount!=-1){
				if(amount!=0) line+=ChatColor.GREEN+Material.getMaterial(item).name()+includeAmount(amount)+ChatColor.WHITE+", ";
				else line+=ChatColor.DARK_GREEN+Material.getMaterial(item).name()+":0"+ChatColor.WHITE+", ";
			}
			else{
				if(rmChest.getItems().containsKey(item)) amount = rmChest.getItemLeft(item);
				else amount = _items.get(item);
				if(amount!=0) line+=ChatColor.WHITE+Material.getMaterial(item).name()+includeAmount(amount)+ChatColor.WHITE+", ";
			}
		}
		line = plugin.stripLast(line, ",");
		return line;
	}
	
	public String includeAmount(Integer[] amount, boolean... less){
		int i = amount[0];
		if((i!=1)&&(less.length==0)){
			return ChatColor.GRAY+":"+i;
		}
		return "";
	}
	public String includeAmount(Integer amount, boolean... less){
		if((amount!=1)&&(less.length==0)){
			return ChatColor.GRAY+":"+amount;
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
	
	public List<Material> findHackMaterials(HashMap<Integer, Integer[]> materials){
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
	
	public HashMap<Integer, Integer[]> removeHackMaterials(HashMap<Integer, Integer[]> materials){
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
	
	public ItemStack[] removeDuplicates(ItemStack[] items){
		List<Material> materials = new ArrayList<Material>();
		for(ItemStack item : items){
			if(item!=null){
				Material mat = item.getType();
				if(!materials.contains(mat)) materials.add(mat);
			}
		}
		ItemStack[] newItems = new ItemStack[materials.size()];
		for(int i=0; i<materials.size(); i++){
			Material mat = materials.get(i);
			newItems[i] = new ItemStack(mat, mat.getMaxStackSize());
		}
		return newItems;
	}
	
	public ItemStack[] addDuplicates(ItemStack[] items, boolean fullStack){
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
		
		ItemStack[] newItems = new ItemStack[hash.size()];
		Integer[] array = hash.keySet().toArray(new Integer[hash.keySet().size()]);
		Arrays.sort(array);
			
		int j = 0;
		for(Integer i : array){
			newItems[j] = new ItemStack(Material.getMaterial(i), hash.get(i));
			j++;
		}
		return newItems;
	}
			
	public void addLog(Block b){
		if(!_logBlockList.contains(b)){
			_logBlockList.add(new RMBlock(b));
		}
	}
	public void addLog(Block b, Material mat){
		if(!_logBlockList.contains(b)){
			_logBlockList.add(new RMBlock(b, mat));
		}
	}
	public void clearLog(){
		_logBlockList.clear();
	}
	public void restoreLog(){
		for(RMBlock rmb : _logBlockList){
			rmb.restore();
		}
		_logBlockList.clear();
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
	public void warnHackMaterials(HashMap<Integer, Integer[]> items){
		warnHackMaterialsMessage(plugin.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterialsMessage(String message){
		if(message.length()>0) getOwner().sendMessage(ChatColor.RED+"Not allowed: "+message);
	}
}
