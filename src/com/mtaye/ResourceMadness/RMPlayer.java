package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMGame.FilterState;
import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.ForceState;
import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.HandleState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPlayer {
	public enum PlayerAction{
		ADD, REMOVE, INFO, INFO_SETTINGS, INFO_FOUND, MODE, MODE_CYCLE,
		JOIN, QUIT, START, START_RANDOM, RESTART, STOP,
		RESTORE, FILTER, AWARD, TOOLS, CLAIM_FOUND,
		SET_MIN_PLAYERS, SET_MAX_PLAYERS, SET_MIN_TEAM_PLAYERS, SET_MAX_TEAM_PLAYERS, SET_MAX_ITEMS, SET_RANDOM,
		SET_WARP, SET_RESTORE, SET_WARN_HACKED, SET_ALLOW_HACKED,
		SET_KEEP_INGAME, SET_MIDGAME_JOIN, SET_CLEAR_INVENTORY,
		SET_WARN_UNEQUAL, SET_ALLOW_UNEQUAL, SET_INFINITE_AWARD, SET_INFINITE_TOOLS,
		NONE;
	}
	private String _name;
	private RMTeam _team;
	//private List<RMGame> _games;
	private RMRequestFilter _requestFilter;
	private int _requestInt = 0;
	private InterfaceState _requestInterface = InterfaceState.FILTER;
	private RMStats _stats = new RMStats();
	private List<ItemStack> _items = new ArrayList<ItemStack>();
	private List<ItemStack> _award = new ArrayList<ItemStack>();
	private List<ItemStack> _tools = new ArrayList<ItemStack>();
	private boolean _isOnline = false;

	public void getInfoItems(){
		String items = plugin.getSortedItemsFromItemStackArray(_items.toArray(new ItemStack[_items.size()]));
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Items: "+items);
		}
		else sendMessage(ChatColor.YELLOW+"No items found.");
	}
	public void getInfoAward(){
		String items = plugin.getSortedItemsFromItemStackArray(_award.toArray(new ItemStack[_award.size()]));
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Award items: "+items);
		}
		else sendMessage(ChatColor.YELLOW+"No award found.");
	}
	public void getInfoTools(){
		String items = plugin.getSortedItemsFromItemStackArray(_tools.toArray(new ItemStack[_tools.size()]));
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Tools items: "+items);
		}
		else sendMessage(ChatColor.YELLOW+"No tools found.");
	}
	
	public List<ItemStack> getItems(){
		return _items;
	}
	public List<ItemStack> getAward(){
		return _award;
	}
	public List<ItemStack> getTools(){
		return _tools;
	}
	public void setItems(List<ItemStack> items){
		_items = items;
	}
	public void setAward(List<ItemStack> award){
		_award = award;
	}
	public void setTools(List<ItemStack> tools){
		_tools = tools;
	}
	public void clearItems(){
		_items.clear();
	}
	public void clearAward(){
		_award.clear();
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
	
	public void addItems(ClaimType claimType){
		if(getPlayer()!=null){
			//_items = getItemsFromInventory();
			ItemStack[] contents = getPlayer().getInventory().getContents();
			for(ItemStack item : contents){
				if((item!=null)&&(item.getType()!=Material.AIR)){
					addItem(item, claimType);
				}
			}
			getPlayer().getInventory().clear();
		}
	}
	
	public HandleState addItem(ItemStack item, ClaimType claimType){
		if((item!=null)&&(item.getType()!=Material.AIR)){
			List<ItemStack> items = new ArrayList<ItemStack>();
			switch(claimType){
				case ITEMS: items = _items; break;
				case AWARD: items = _award; break;
				case TOOLS: items = _tools; break;
			}
			for(ItemStack isItem : items){
				if(isItem.getType() == item.getType()){
					isItem.setAmount(isItem.getAmount()+item.getAmount());
					return HandleState.MODIFY;
				}
			}
			items.add(item);
			return HandleState.ADD;
		}
		return HandleState.NO_CHANGE;
	}

	public void addItemsByItemStack(ItemStack[] items){
		for(ItemStack item : items){
			if((item!=null)&&(item.getType()!=Material.AIR)){
				_items.add(item);
			}
		}
	}
	public void addByListItemStack(List<ItemStack> items, ClaimType claimType){
		for(ItemStack item : items){
			if((item!=null)&&(item.getType()!=Material.AIR)){
				addItem(item, claimType);
			}
		}
	}
	public void addByItemStack(ItemStack[] items, ClaimType claimType){
		for(ItemStack item : items){
			if((item!=null)&&(item.getType()!=Material.AIR)){
				addItem(item, claimType);
			}
		}
	}
	
	public void claimTransfer(ClaimType source, ClaimType destination){
		List<ItemStack> itemsSource = new ArrayList<ItemStack>();
		if(source==destination) return;
		switch(source){
			case ITEMS:	itemsSource = _items; break;
			case AWARD:	itemsSource = _award; break;
			case TOOLS:	itemsSource = _tools; break;
		}
		if(itemsSource==null) return;
		addByListItemStack(itemsSource, destination);
		itemsSource.clear();
	}

	public HandleState claim(List<ItemStack> items, ClaimType claimType){
		if(items.size()==0){
			switch(claimType){
				case ITEMS:	sendMessage("No items to return."); break;
				case FOUND:	sendMessage("No found items to give."); break;
				case AWARD:	sendMessage("No award to give."); break;
				case TOOLS:	sendMessage("No tools to give."); break;
			}
			return HandleState.NO_CHANGE;
		}
		if(getPlayer()!=null){
			Inventory inv = getPlayer().getInventory();
			if(inv.firstEmpty()==-1){
				switch(claimType){
					case ITEMS:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot return items."); break;
					case FOUND:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give found items."); break;
					case AWARD:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give award."); break;
					case TOOLS:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give tools."); break;
				}
				return HandleState.NO_CHANGE;
			}
			List<ItemStack> removeItems = new ArrayList<ItemStack>();
			for(int i=0; i<items.size(); i++){
				if(inv.firstEmpty()!=-1){
					ItemStack item = items.get(i);
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
							removeItems.add(item);
						}
					}
				}
				else break;
			}
			for(ItemStack item : removeItems){
				items.remove(item);
			}
			//inv.clear();
			//clearInventoryContents();
			if(items.size()>0){
				sendMessage(ChatColor.RED+"Your Inventory is full. "+ChatColor.YELLOW+plugin.getListItemStackTotalStack(items)+ChatColor.WHITE+" item(s) remaining.");
				return HandleState.CLAIM_RETURNED_SOME;
			}
			else{
				switch(claimType){
					case ITEMS:	sendMessage(ChatColor.YELLOW+"All items were returned. "+ChatColor.WHITE+"Check your inventory."); break;
					case FOUND:	sendMessage(ChatColor.YELLOW+"Found items were given. "+ChatColor.WHITE+"Check your inventory."); break;
					case AWARD:	sendMessage(ChatColor.YELLOW+"Award was given. "+ChatColor.WHITE+"Check your inventory."); break;
					case TOOLS:	sendMessage(ChatColor.YELLOW+"Tools were given. "+ChatColor.WHITE+"Check your inventory."); break;
				}
				return HandleState.CLAIM_RETURNED_ALL;
			}
		}
		return HandleState.NO_CHANGE;
	}
	
	public HandleState claimItem(List<ItemStack> items, ItemStack isItem, ClaimType claimType){
		if(items.size()==0){
			switch(claimType){
				case ITEMS:	sendMessage("No item to return."); break;
				case AWARD:	sendMessage("No award to give."); break;
				case TOOLS:	sendMessage("No tools to give."); break;
			}
			return HandleState.NO_CHANGE;
		}
		if(getPlayer()!=null){
			Inventory inv = getPlayer().getInventory();
			if(inv.firstEmpty()==-1){
				switch(claimType){
					case ITEMS:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot return item."); break;
					case AWARD:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give award."); break;
					case TOOLS:	sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give tools."); break;
				}
				return HandleState.NO_CHANGE;
			}
			ItemStack removeItem = null;
			for(int i=0; i<items.size(); i++){
				if(inv.firstEmpty()!=-1){
					ItemStack item = items.get(i);
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
							removeItem = item;
						}
					}
				}
				else break;
			}
			if(removeItem!=null){
				items.remove(removeItem);
				switch(claimType){
					case ITEMS:	sendMessage("Item was returned. Check your inventory."); break;
					case AWARD:	sendMessage("Award was given. Check your inventory."); break;
					case TOOLS:	sendMessage("Tools were given. Check your inventory."); break;
				}
				return HandleState.REMOVE;
			}
			else{
				sendMessage("Inventory is full. "+ChatColor.YELLOW+plugin.getListItemStackTotalStack(items)+ChatColor.WHITE+" item(s) remaining.");
				return HandleState.MODIFY;
			}
		}
		return HandleState.NO_CHANGE;
	}
	
	public void claimItems(){
		claim(_items, ClaimType.ITEMS);
	}
	public void claimAward(){
		claim(_award, ClaimType.AWARD);
	}
	public void claimTools(){
		claim(_tools, ClaimType.TOOLS);
	}
	
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
		for(RMGame rmGame : RMGame.getGames()){
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
		if(getGame()!=null) return true;
		return false;
	}
	
	public RMGame getGame(){
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
	
	public void onPlayerQuit(){
		RMTeam rmTeam = getTeam();
		if(rmTeam!=null){
			RMGame rmGame = rmTeam.getGame();
			if(rmGame!=null){
				if(rmGame.getConfig().getState()==GameState.GAMEPLAY){
					if(!rmGame.getConfig().getKeepIngame()){
						rmGame.quitTeam(rmTeam, this);
						if(!rmGame.hasMinimumPlayers()){
							
							RMTeam winningTeam = null;
							for(RMTeam rmt : rmGame.getTeams()){
								if(rmt!=rmTeam){
									winningTeam = rmt;
								}
							}
							rmGame.setWinningTeam(winningTeam);
							rmGame.gameOver();
						}
					}
				}
			}
		}
	}
	
	public boolean isOnline(){
		return _isOnline;
	}
	
	public void isOnline(boolean isOnline){
		_isOnline = isOnline;
	}
	
	public boolean hasOwnerPermission(String node){
		Player p = getPlayer();
		if(p!=null) return (plugin.hasPermission(p, node, false));
		return false;
	}
	
	public boolean hasPermission(String node){
		Player p = getPlayer();
		if(p!=null) return (plugin.hasPermission(p, node, true));
		return false;
	}
}