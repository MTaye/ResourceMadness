package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;
import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMGame.FilterState;
import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.ForceState;
import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.HandleState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPlayer {
	public enum PlayerAction{
		ADD, REMOVE, INFO, INFO_FOUND, MODE, MODE_CYCLE, SETTINGS,
		JOIN, QUIT, START, START_RANDOM, RESTART, STOP, PAUSE, RESUME,
		RESTORE, FILTER, REWARD, TOOLS, CLAIM_FOUND, CLAIM_FOUND_CHEST, CLAIM_FOUND_CHEST_SELECT, CLAIM_ITEMS_CHEST, CLAIM_REWARD_CHEST, CLAIM_TOOLS_CHEST,
		SET_MIN_PLAYERS, SET_MAX_PLAYERS, SET_MIN_TEAM_PLAYERS, SET_MAX_TEAM_PLAYERS, SET_MAX_ITEMS, SET_TIME_LIMIT, SET_RANDOM,
		SET_ADVERTISE, SET_RESTORE, SET_WARP, SET_MIDGAME_JOIN, SET_HEAL_PLAYER, SET_CLEAR_INVENTORY, SET_WARN_UNEQUAL, SET_ALLOW_UNEQUAL,
		SET_WARN_HACKED, SET_ALLOW_HACKED, SET_INFINITE_REWARD, SET_INFINITE_TOOLS,
		NONE;
	}
	private String _name;
	private RMTeam _team;
	//private List<RMGame> _games;
	private RMRequestFilter _requestFilter;
	private ItemStack[] _requestItems;
	private int _requestInt = 0;
	private boolean _requestBool = false;
	private InterfaceState _requestInterface = InterfaceState.FILTER;
	private RMStats _stats = new RMStats();
	private RMStash _items = new RMStash();
	private RMStash _reward = new RMStash();
	private RMStash _tools = new RMStash();
	private boolean _ready = false;
	
	public boolean getReady(){
		return _ready;
	}
	public void setReady(boolean ready){
		_ready = ready;
	}
	public void toggleReady(){
		if(_ready) _ready = false;
		else _ready = true;
	}
	

	public void getInfoItems(){
		String items = RMText.getStringSortedItems(_items.getItems());
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Items: "+items);
		}
		else sendMessage(ChatColor.GRAY+"No items found.");
	}
	public void getInfoReward(){
		String items = RMText.getStringSortedItems(_reward.getItems());
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Reward items: "+items);
		}
		else sendMessage(ChatColor.GRAY+"No reward found.");
	}
	public void getInfoTools(){
		String items = RMText.getStringSortedItems(_tools.getItems());
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Tools items: "+items);
		}
		else sendMessage(ChatColor.GRAY+"No tools found.");
	}
	
	public RMStash getItems(){
		return _items;
	}
	public RMStash getReward(){
		return _reward;
	}
	public RMStash getTools(){
		return _tools;
	}
	public void setItems(RMStash items){
		_items = items;
		_items.clearChanged();
	}
	public void setReward(RMStash reward){
		_reward = reward;
		_reward.clearChanged();
	}
	public void setTools(RMStash tools){
		_tools = tools;
		_tools.clearChanged();
	}
	public void clearItems(){
		_items.clear();
	}
	public void clearReward(){
		_reward.clear();
	}
	public void clearTools(){
		_tools.clear();
	}
	
	public List<ItemStack> getItemsFromInventory(){
		if(getPlayer()!=null){
			Inventory inv = getPlayer().getInventory();
			List<ItemStack> items = new ArrayList<ItemStack>();
			for(ItemStack item : inv.getContents()){
				if(item!=null) items.add(item);
			}
			return items;
		}
		return null;
	}
	
	public void addItemsFromInventory(RMStash rmStash){
		if(getPlayer()!=null){
			//_items = getItemsFromInventory();
			ItemStack[] contents = getPlayer().getInventory().getContents();
			for(ItemStack item : contents){
				rmStash.addItem(item);
			}
			getPlayer().getInventory().clear();
		}
	}
	/*
	public HandleState addItem(ItemStack item, RMStash rmStash){
		return rmStash.addItem(item);
	}
	*/
	
	public void announceNoItemsToGive(ClaimType claimType){
		switch(claimType){
			case FOUND:	sendMessage(ChatColor.GRAY+"No found items to give."); break;
			case ITEMS:	sendMessage(ChatColor.GRAY+"No items to return."); break;
			case REWARD: sendMessage(ChatColor.GRAY+"No reward to give."); break;
			case TOOLS:	sendMessage(ChatColor.GRAY+"No tools to give."); break;
			default:
		}
	}
	
	public void announceInventoryIsFull(int remaining){
		sendMessage(ChatColor.RED+"Your Inventory is full. "+ChatColor.YELLOW+remaining+ChatColor.WHITE+" item(s) remaining.");
	}
	
	public void announceInventoryIsFull(ClaimType claimType){
		switch(claimType){
			case FOUND:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give found items."); break;
			case ITEMS:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot return items."); break;
			case REWARD: sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give reward."); break;
			case TOOLS:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give tools."); break;
			default:
		}
	}
	
	public void announceAllReturned(ClaimType claimType){
		switch(claimType){
		case ITEMS:	sendMessage(ChatColor.YELLOW+"Items were given. "+ChatColor.WHITE+"Check your inventory."); break;
		case FOUND:	sendMessage(ChatColor.YELLOW+"Found items were given. "+ChatColor.WHITE+"Check your inventory."); break;
		case REWARD: sendMessage(ChatColor.YELLOW+"Reward was given. "+ChatColor.WHITE+"Check your inventory."); break;
		case TOOLS:	sendMessage(ChatColor.YELLOW+"Tools were given. "+ChatColor.WHITE+"Check your inventory."); break;
		default:
		}
	}

	public HashMap<Integer, ItemStack> claimToInventory(RMStash rmStash, Inventory inv, boolean usePlayerInv, boolean claimWhole, ClaimType claimType, ItemStack... items){
		HashMap<Integer, ItemStack> returnedItems = new HashMap<Integer, ItemStack>();
		if(rmStash.size()==0){
			announceNoItemsToGive(claimType);
			return returnedItems;
		}
		int rmStashAmount = rmStash.getAmount();
		
		List<ItemStack> stashItems = new ArrayList<ItemStack>();
		
		if((items==null)||(items.length==0)) stashItems = rmStash.getItems(); //Claim All
		else{
			for(ItemStack item : items){
				if((item==null)||(item.getType()==Material.AIR)) continue;
				if(!claimWhole) stashItems.addAll(rmStash.removeByIdAmount(item.getTypeId(), item.getAmount()));
				else stashItems.addAll(rmStash.removeItemById(item.getTypeId()));
			}
		}
		if(inv!=null) returnedItems = inv.addItem(stashItems.toArray(new ItemStack[stashItems.size()]));
		
		if(usePlayerInv){
			if(getPlayer()!=null){
				Inventory rmpInv = getPlayer().getInventory();
				if((rmpInv!=null)&&(rmpInv!=inv)){
					if(inv!=null) returnedItems = rmpInv.addItem(returnedItems.values().toArray(new ItemStack[returnedItems.size()]));
					else returnedItems = rmpInv.addItem(stashItems.toArray(new ItemStack[stashItems.size()]));
				}
			}
		}
		
		if((items==null)||(items.length==0)){  //Claim All
			rmStash.clear();
			if(returnedItems.size()!=0){
				rmStash.addItems(returnedItems.values().toArray(new ItemStack[returnedItems.size()]));
				rmStash.addChangedToChanged(rmStash.getRemoved(), rmStash.getAdded());
				rmStash.clearAdded();
			}
		}
		else{
			RMStash stashStashItems = new RMStash(stashItems);
			stashStashItems.removeItems(returnedItems.values().toArray(new ItemStack[returnedItems.values().size()]));
			
			if(returnedItems.size()!=0){
				rmStash.addItems(returnedItems.values().toArray(new ItemStack[returnedItems.values().size()]));
				rmStash.clearAdded();
				//rmStash.addItemsToChanged(rmStash.getRemoved(), stashStashItems.getItems());
			}
			stashStashItems.clearChanged();
			stashStashItems.removeItems(stashStashItems.getItems());
			rmStash.addChangedToChanged(rmStash.getModified(), stashStashItems.getModified());
			rmStash.addChangedToChanged(rmStash.getRemoved(), stashStashItems.getRemoved());
		}
		
		if(rmStashAmount==rmStash.getAmount()) announceInventoryIsFull(claimType);
		else if(returnedItems.size()>0) announceInventoryIsFull(rmStash.getAmount());
		else announceAllReturned(claimType);
		return returnedItems;
	}
	
	//Claim
	public HashMap<Integer, ItemStack> claim(RMStash rmStash, ClaimType claimType, ItemStack... items){
		if(getPlayer()==null) return null;
		return claimToInventory(rmStash, getPlayer().getInventory(), false, false, claimType, items);
	}
	//Claim
	public HashMap<Integer, ItemStack> claim(RMStash rmStash, ClaimType claimType, boolean claimWhole, ItemStack... items){
		if(getPlayer()==null) return null;
		return claimToInventory(rmStash, getPlayer().getInventory(), false, claimWhole, claimType, items);
	}
	
	public void claimItemsToChest(Block b, boolean usePlayerInv, ItemStack... items){
		claimToChest(b, ClaimType.ITEMS, usePlayerInv, items);
	}
	public void claimRewardToChest(Block b, boolean usePlayerInv, ItemStack... items){
		claimToChest(b, ClaimType.REWARD, usePlayerInv, items);
	}
	public void claimToolsToChest(Block b, boolean usePlayerInv, ItemStack... items){
		claimToChest(b, ClaimType.TOOLS, usePlayerInv, items);
	}
	public void claimToChest(Block b, ClaimType claimType, boolean usePlayerInv, ItemStack... items){
		if(b.getType()!=Material.CHEST) return;
		RMInventory rmInventory = new RMInventory((Chest)b.getState());
		
		switch(claimType){
		case FOUND: claimToInventory(RMGame.getGame(getRequestInt()).getConfig().getFound(), rmInventory, usePlayerInv, false, ClaimType.FOUND, items); break;
		case ITEMS: claimToInventory(_items, rmInventory, usePlayerInv, false, ClaimType.ITEMS, items); break;
		case REWARD: claimToInventory(_reward, rmInventory, usePlayerInv, false, ClaimType.REWARD, items); break;
		case TOOLS: claimToInventory(_tools, rmInventory, usePlayerInv, false, ClaimType.TOOLS, items); break;
		}
	}
	public HashMap<Integer, ItemStack> claimStashToChest(RMStash stash, Block b, ClaimType claimType, boolean usePlayerInv, ItemStack... items){
		HashMap<Integer, ItemStack> returnItems = new HashMap<Integer, ItemStack>();
		RMInventory rmInventory = null;
		if((b!=null)&&(b.getType()==Material.CHEST)) rmInventory = new RMInventory((Chest)b.getState());
		
		switch(claimType){
		case FOUND: returnItems = claimToInventory(stash, rmInventory, usePlayerInv, false, ClaimType.FOUND, items); break;
		case ITEMS: returnItems = claimToInventory(stash, rmInventory, usePlayerInv, false, ClaimType.ITEMS, items); break;
		case REWARD: returnItems = claimToInventory(stash, rmInventory, usePlayerInv, false, ClaimType.REWARD, items); break;
		case TOOLS: returnItems = claimToInventory(stash, rmInventory, usePlayerInv, false, ClaimType.TOOLS, items); break;
		}
		return returnItems;
	}

	public void claimItems(ItemStack... items){
		claim(_items, ClaimType.ITEMS, items);
	}
	public void claimReward(ItemStack... items){
		claim(_reward, ClaimType.REWARD, items);
	}
	public void claimTools(ItemStack... items){
		claim(_tools, ClaimType.TOOLS, items);
	}
	
	/*
	public RMClaimInfo claimBig(RMStash rmStash, ClaimType claimType){
		RMClaimInfo claimInfo = new RMClaimInfo(rmStash, HandleState.NO_CHANGE);
		if(getPlayer()==null) return claimInfo;
		RMDebug.log(Level.WARNING, "RMSTASH SIZE:"+rmStash.size());
		if(rmStash.size()==0){
			announceNoItemsToGive(claimType);
			return claimInfo;
		}
		Inventory inv = getPlayer().getInventory();
		int rmStashAmount = rmStash.getAmount();
		
		List<Integer> removeItems = new ArrayList<Integer>();
		for(Integer id : rmStash.keySet()){
			RMStashItem rmStashItem = rmStash.get(id);
			int amount = rmStashItem.getAmount();
			
			List<ItemStack> stashItems = rmStash.getItems();
			String strItems = "";
			for(ItemStack strItem : stashItems){
				strItems+=strItem.getType()+":"+strItem.getAmount()+",";
			}
			
			if(stashItems.size()==0) continue;
			
			while(rmStashItem.getAmount()>0){
				if(RMInventoryHelper.findUsableStack(inv, id)!=-1){
					HashMap<Integer, ? extends ItemStack> hashSlots = inv.all(id);
					RMDebug.log(Level.WARNING, "USABLE");
					for(ItemStack slotItem : hashSlots.values()){
						RMDebug.log(Level.WARNING, "SLOTITEM:"+slotItem.getAmount());
						Iterator<ItemStack> iter = stashItems.iterator();
						while(iter.hasNext()){
							ItemStack stashItem = iter.next();
							RMDebug.log(Level.WARNING, "ITER AMOUNT:"+stashItem.getAmount());
							int itemAmount = stashItem.getAmount();
							if(itemAmount==0){
								RMDebug.log(Level.WARNING, "CONTINUE");
								continue;
							}
							RMDebug.log(Level.WARNING, "slotItem.getMaxStackSize():"+slotItem.getMaxStackSize());
							RMDebug.log(Level.WARNING, "slotItem.getAmount():"+slotItem.getAmount());
							int freeAmount = slotItem.getMaxStackSize()-slotItem.getAmount();
							RMDebug.log(Level.WARNING, "freeAmount:"+freeAmount);
							if(freeAmount!=0){
								RMDebug.log(Level.WARNING, "freeAmount != 0:"+freeAmount);
								int overflow = freeAmount - itemAmount;
								if(overflow>=0){
									RMDebug.log(Level.WARNING, "overflow >= 0:"+overflow);
									RMDebug.log(Level.WARNING, "slotItem.getAmount():"+slotItem.getAmount());
									RMDebug.log(Level.WARNING, "slotItem.getAmount():"+slotItem.getAmount());
									RMDebug.log(Level.WARNING, "slotItem.getAmount():"+slotItem.getAmount());
									RMDebug.log(Level.WARNING, "slotItem.getAmount():"+slotItem.getAmount());
									RMDebug.log(Level.WARNING, "itemAmount:"+itemAmount);
									RMDebug.log(Level.WARNING, "itemAmount:"+itemAmount);
									RMDebug.log(Level.WARNING, "itemAmount:"+itemAmount);
									RMDebug.log(Level.WARNING, "itemAmount:"+itemAmount);
									RMDebug.log(Level.WARNING, "slotItem.getAmount()+itemAmount:"+slotItem.getAmount()+itemAmount);
									RMDebug.log(Level.WARNING, "slotItem.getAmount()+itemAmount:"+slotItem.getAmount()+itemAmount);
									RMDebug.log(Level.WARNING, "slotItem.getAmount()+itemAmount:"+slotItem.getAmount()+itemAmount);
									RMDebug.log(Level.WARNING, "slotItem.getAmount()+itemAmount:"+slotItem.getAmount()+itemAmount);
									slotItem.setAmount(slotItem.getAmount()+itemAmount);
									RMDebug.log(Level.WARNING, "slotItem.getAmount() NEW:"+slotItem.getAmount());
									RMDebug.log(Level.WARNING, "slotItem.getAmount() NEW:"+slotItem.getAmount());
									RMDebug.log(Level.WARNING, "slotItem.getAmount() NEW:"+slotItem.getAmount());
									RMDebug.log(Level.WARNING, "slotItem.getAmount() NEW:"+slotItem.getAmount());
									
									RMDebug.log(Level.WARNING, "rmStash.getAmount() BEFORE REMOVE:"+rmStash.getAmount());
									rmStash.removeByIdAmount(id, itemAmount);
									RMDebug.log(Level.WARNING, "rmStash.getAmount() AFTER REMOVE:"+rmStash.getAmount());
									RMDebug.log(Level.WARNING, "slotItem.getAmount() NEW:"+slotItem.getAmount());
									RMDebug.log(Level.WARNING, "stashItem.getAmount():"+stashItem.getAmount());
									//stashItem.setAmount(0);
									RMDebug.log(Level.WARNING, "stashItem.getAmount() NEW:"+stashItem.getAmount());
									//rmStashItem.removeByAmount(itemAmount);
								}
								else{
									RMDebug.log(Level.WARNING, "overflow < 0:"+overflow);
									//rmStashItem.removeByAmount(freeAmount);
									RMDebug.log(Level.WARNING, "stashItem.getAmount():"+stashItem.getAmount());
									RMDebug.log(Level.WARNING, "freeAmount:"+freeAmount);
									//stashItem.setAmount(stashItem.getAmount()-freeAmount);
									RMDebug.log(Level.WARNING, "stashItem.getAmount() NEW:"+stashItem.getAmount());
									RMDebug.log(Level.WARNING, "rmStash.getAmount() BEFORE REMOVE:"+rmStash.getAmount());
									RMDebug.log(Level.WARNING, "freeAmount:"+freeAmount);
									rmStash.removeByIdAmount(id, freeAmount);
									RMDebug.log(Level.WARNING, "rmStash.getAmount() AFTER REMOVE:"+rmStash.getAmount());
									RMDebug.log(Level.WARNING, "slotItem.getAmount():"+slotItem.getAmount());
									slotItem.setAmount(slotItem.getMaxStackSize());
									RMDebug.log(Level.WARNING, "slotItem.getAmount() NEW:"+slotItem.getAmount());
								}
								if(stashItem.getAmount()==0){
									RMDebug.log(Level.WARNING, "stashItem.getAmount()==0 REMOVE:"+stashItem.getAmount());
									iter.remove();
								}
							}
							else RMDebug.log(Level.WARNING, "freeAmount = 0:"+freeAmount);
						}
						if(rmStashItem.getAmount()==0){
							RMDebug.log(Level.WARNING, "rmStashItem.getAmount()==0 BREAK:"+rmStashItem.getAmount());
							break;
						}
					}
				}
				else if(inv.firstEmpty()!=-1){
					RMDebug.log(Level.WARNING, "FIRST EMPTY");
					while((rmStashItem.size()>0)&&(inv.firstEmpty()!=-1)){
						ItemStack item = rmStashItem.getItem();
						if((item!=null)&&(item.getType()!=Material.AIR)){
							Material mat = item.getType();
							while(item.getAmount()>mat.getMaxStackSize()){
								if(inv.firstEmpty()!=-1){
									ItemStack itemClone = item.clone();
									itemClone.setAmount(mat.getMaxStackSize());
									inv.addItem(itemClone);
									item.setAmount(item.getAmount()-mat.getMaxStackSize());
								}
								else break;
							}
							if(item.getAmount()<=mat.getMaxStackSize()){
								inv.addItem(item);
								HashMap<Integer, ItemStack> hashItems = inv.addItem(item);
								rmStashItem.removeItemByItemStack(item);
							}
						}
					}
					if(rmStashItem.size()==0){
						removeItems.add(id);
					}
				}
				else break;
			}
		}
		rmStash.removeItemsByIdSilent(removeItems);

		if(rmStashAmount==rmStash.getAmount()){
			announceInventoryIsFull(claimType);
		}
		else if(rmStash.size()>0){
			announceInventoryIsFull(rmStash.getAmount());
			claimInfo.setHandleState(HandleState.CLAIM_RETURNED_SOME);
		}
		else{
			announceAllReturned(claimType);
			claimInfo.setHandleState(HandleState.CLAIM_RETURNED_ALL);
		}
		return claimInfo;
	}
	*/
	
	/*
	public RMStash claimToSlots(RMStash rmStash){
		if(getPlayer()==null) return rmStash;
		Inventory inv = getPlayer().getInventory();
		
		for(RMStashItem rmStashItem : rmStash.values()){
			int id = rmStashItem.getId();
			int amount = rmStashItem.getAmount();
			int usableStack = RMInventoryHelper.findUsableStack(inv, id);
			if(usableStack==-1) continue;
			
			List<ItemStack> stashItems = rmStash.getItems();
			String strItems = "";
			for(ItemStack strItem : stashItems){
				strItems+=strItem.getType()+":"+strItem.getAmount()+",";
			}
			HashMap<Integer, ? extends ItemStack> hashSlots = inv.all(id);
			if(stashItems.size()==0) continue;

			if(hashSlots.size()!=0){
				for(ItemStack slotItem : hashSlots.values()){
					Iterator<ItemStack> iter = stashItems.iterator();
					while(iter.hasNext()){
						ItemStack stashItem = iter.next();
						int itemAmount = stashItem.getAmount();
						if(itemAmount==0){
							continue;
						}
						int freeAmount = slotItem.getMaxStackSize()-slotItem.getAmount();
						if(freeAmount!=0){
							int overflow = freeAmount - itemAmount;
							if(overflow>=0){
								slotItem.setAmount(slotItem.getAmount()+itemAmount);
								rmStash.removeByIdAmount(id, itemAmount);
								stashItem.setAmount(stashItem.getAmount()-itemAmount);
								amount -= itemAmount;
							}
							else{
								amount -= freeAmount;
								stashItem.setAmount(stashItem.getAmount()-freeAmount);
								rmStash.removeByIdAmount(id, freeAmount);
								slotItem.setAmount(slotItem.getMaxStackSize());
							}
							if(stashItem.getAmount()==0){
								iter.remove();
							}
						}
					}
					if(amount==0){
						break;
					}
				}
			}
		}
		if(rmStash.size()>0){
			sendMessage(ChatColor.RED+"Not all items could be returned. "+ChatColor.YELLOW+rmStash.getAmount()+ChatColor.WHITE+" item(s) remaining.");
		}
		else{
			sendMessage(ChatColor.YELLOW+"All items were returned.");
		}
		return rmStash;
	}
	*/
	
	public RMStats getStats(){
		return _stats;
	}
	
	public InterfaceState getRequestInterface(){
		return _requestInterface;
	}
	public void setRequestInterface(InterfaceState requestInterface){
		_requestInterface = requestInterface;
	}
	
	public void setRequestInt(int value){
		_requestInt = value;
	}
	public int getRequestInt(){
		int requestInt = _requestInt;
		clearRequestInt();
		return requestInt;
	}
	public void clearRequestInt(){
		_requestInt = 0;
	}
	public void setRequestBool(boolean value){
		_requestBool = value;
	}
	public boolean getRequestBool(){
		return _requestBool;
	}
	
	public static RM plugin;
	private static HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	
	private PlayerAction _playerAction = PlayerAction.NONE;
	
	//Constructor
	public RMPlayer(String player){
		_name = player;
		_playerAction = PlayerAction.NONE;
		_players.put(player, this);
	}
	public RMPlayer(String player, PlayerAction playerAction){
		_name = player;
		_playerAction = PlayerAction.NONE;
	}
	
	public void setRequestFilter(HashMap<Integer, RMItem> items, FilterState state, FilterType type, ForceState force, int randomize){
		_requestFilter = new RMRequestFilter(items,state,type,force,randomize);
	}
	public RMRequestFilter getRequestFilter(){
		if(_requestFilter!=null) return _requestFilter;
		return null;
	}
	public void clearRequestFilter(){
		_requestFilter = null;
	}
	public ItemStack[] getRequestItems(){
		return _requestItems;
	}
	public void setRequestItems(ItemStack[] requestItems){
		_requestItems = requestItems;
	}
	public void clearRequestItems(){
		_requestItems = null;
	}
	
	//Player GET/SET
	public String getName(){
		return _name;
	}
	public void setName(Player player){
		_name = player.getName();
	}
	
	public Player getPlayer(){
		return plugin.getServer().getPlayer(_name);
	}
	private void setPlayer(String player){
		_name = player;
	}
	public static HashMap<String, RMPlayer> getPlayers(){
		return _players;
	}
	public static RMPlayer getPlayerByName(String name){
		if(_players.containsKey(name)){
			return _players.get(name);
		}
		else{
			Player p = plugin.getServer().getPlayer(name);
			if(p!=null){
				RMPlayer rmp = new RMPlayer(p.getName());
				_players.put(name, rmp);
				return rmp;
			}
		}
		return null;
	}
	
	//Actions GET/SET
	public PlayerAction getPlayerAction(){
		return _playerAction;
	}
	public void setPlayerAction(PlayerAction playerAction){
		_playerAction = playerAction;
	}
	
	//Team
	public void joinTeam(RMTeam team){
		team.addPlayer(this);
		_team = team;
	}
	public void quitTeam(RMTeam team){
		team.removePlayer(this);
		_team = null;
	}
	
	public void setTeam(RMTeam rmt){
		_team = rmt;
	}
	public void clearTeam(){
		_team = null;
	}
	
	//Team GET/SET
	public RMTeam getTeam(){
		return _team;
	}
	
	//Game
	public List<RMGame> getGames(){
		List<RMGame> rmGames = new ArrayList<RMGame>();
		for(RMGame rmGame : RMGame.getGames().values()){
			if(rmGame.getConfig().getOwnerName().equalsIgnoreCase(getPlayer().getName())){
				rmGames.add(rmGame);
			}
		}
		return rmGames;
	}
	
	//SendMessage
	public boolean sendMessage(String message){
		if(getPlayer()!=null){
			getPlayer().sendMessage(message);
			return true;
		}
		return false;
	}
	
	public void warpToSafety(){
		if(getPlayer()!=null){
			Player p = getPlayer();
			//double pLocX = p.getLocation().getBlockX();
			//double pLocY = p.getLocation().getBlockY();
			//double pLocZ = p.getLocation().getBlockZ();
			Location loc = _team.getWarpLocation().clone();
			//if((Math.abs(pLocX-loc.getX())>4)||(Math.abs(pLocY-loc.getY())>4)||(Math.abs(pLocZ-loc.getZ())>4)){
				loc.setPitch(p.getLocation().getPitch());
				loc.setYaw(p.getLocation().getYaw());
				p.teleport(loc);
			//}
		}
	}
	
	public static void warpToSafety(Player p){
		RMPlayer rmp = getPlayerByName(p.getName());
		if(rmp!=null){
			Location loc = rmp.getTeam().getWarpLocation().clone();
			loc.setPitch(p.getLocation().getPitch());
			loc.setYaw(p.getLocation().getYaw());
		}
	}
	public boolean isSneaking(){
		if(getPlayer()!=null) return getPlayer().isSneaking();
		return false;
	}
	
	public boolean isIngame(){
		if(getGameInProgress()!=null) return true;
		return false;
	}
	
	public RMGame getGameInProgress(){
		RMTeam rmTeam = getTeam();
		if(rmTeam!=null){
			RMGame rmGame = rmTeam.getGame();
			if(rmGame!=null){
				if(rmGame.getConfig().getState()==GameState.GAMEPLAY){
					return rmGame;
				}
			}
		}
		return null;
	}
	
	public void onPlayerJoin(){
	}
	
	public void onPlayerQuit(RMTeam rmTeam){
		setReady(false);
		
		if(rmTeam!=null){
			RMGame rmGame = rmTeam.getGame();
			if(rmGame!=null){
				switch(rmGame.getConfig().getState()){
				case GAMEPLAY: case PAUSED:
					rmGame.checkPlayerQuit(this, rmTeam);
					break;
				}
			}
		}
	}
	
	public boolean isOnline(){
		Player p = getPlayer();
		if(p==null) return false;
		return p.isOnline();
	}
	
	public boolean hasOwnerPermission(String ownerName){
		Player p = getPlayer();
		if(p!=null){
			if(plugin.isPermissionEnabled()){
				if(ownerName.equalsIgnoreCase(p.getName())) return true;
				else return (plugin.hasPermission(p, "resourcemadness.admin.overlord"));
			}
			if(ownerName.equalsIgnoreCase(p.getName())) return true;
		}
		return false;
	}
	
	public boolean hasPermission(String node){
		Player p = getPlayer();
		if(p!=null) return (plugin.hasPermission(p, node));
		return false;
	}
	
	public void restoreHealth(){
		Player p = getPlayer();
		if(p!=null)	p.setHealth(20);
	}
}