package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPlayer {
	public enum PlayerAction{
		ADD, REMOVE, INFO, INFO_FOUND, MODE, MODE_CYCLE, SETTINGS, SETTINGS_RESET,
		JOIN, QUIT, START, START_RANDOM, RESTART, STOP, PAUSE, RESUME,
		TEMPLATE_LIST, TEMPLATE_SAVE, TEMPLATE_LOAD, TEMPLATE_REMOVE, RESTORE,
		FILTER, FILTER_INFO, FILTER_INFO_STRING, REWARD, REWARD_INFO, REWARD_INFO_STRING, TOOLS, TOOLS_INFO, TOOLS_INFO_STRING,
		CLAIM_FOUND, CLAIM_FOUND_CHEST, CLAIM_FOUND_CHEST_SELECT, CLAIM_ITEMS_CHEST, CLAIM_REWARD_CHEST, CLAIM_TOOLS_CHEST,
		SET_MIN_PLAYERS, SET_MAX_PLAYERS, SET_MIN_TEAM_PLAYERS, SET_MAX_TEAM_PLAYERS, SET_MAX_ITEMS, SET_TIME_LIMIT, SET_RANDOM,
		SET_ADVERTISE, SET_RESTORE, SET_WARP, SET_MIDGAME_JOIN, SET_HEAL_PLAYER, SET_CLEAR_INVENTORY, SET_FOUND_AS_REWARD,
		SET_WARN_UNEQUAL, SET_ALLOW_UNEQUAL, SET_WARN_HACKED, SET_ALLOW_HACKED, SET_INFINITE_REWARD, SET_INFINITE_TOOLS,
		NONE;
	}
	
	public enum ChatMode { WORLD, GAME, TEAM };
	
	private String _name;
	private RMTeam _team;
	//private List<RMGame> _games;
	private RMRequestFilter _requestFilter;
	private ItemStack[] _requestItems;
	private int _requestInt = 0;
	private boolean _requestBool = false;
	private String _requestString = "";
	private InterfaceState _requestInterface = InterfaceState.FILTER;
	private RMStats _stats = new RMStats();
	private RMStash _items = new RMStash();
	private RMStash _reward = new RMStash();
	private RMStash _tools = new RMStash();
	private HashMap<String, RMTemplate> _templates = new HashMap<String, RMTemplate>();
	private boolean _ready = false;
	private Location _returnLocation;
	private ChatMode _chatMode = ChatMode.WORLD;
	
	public String getChatMessage(ChatMode chatMode, String message){
		String str = "";
		if(message.length()==0) return str;
		RMTeam team = getTeam();
		switch(chatMode){
		case WORLD: str = team.getChatColor()+"<"+getName()+ChatColor.WHITE+"@"+team.getChatColor()+"World> "+ChatColor.WHITE+message; break;
		case GAME: str = team.getChatColor()+"<"+getName()+ChatColor.WHITE+"@"+team.getChatColor()+"Game> "+ChatColor.WHITE+message; break;
		case TEAM: str = team.getChatColor()+"<"+getName()+ChatColor.WHITE+"@"+team.getChatColor()+"Team> "+ChatColor.WHITE+message; break;
		}
		return str;
	}
	
	public void chat(ChatMode chatMode, String message){
		if(message.length()==0) return;
		switch(chatMode){
		case WORLD: plugin.getServer().broadcastMessage(message); break;
		case GAME: getGameInProgress().teamBroadcastMessage(message); break;
		case TEAM: getTeam().teamMessage(message); break;
		}
	}
	
	public ChatMode getChatMode(){
		return _chatMode;
	}
	public void setChatMode(ChatMode chatMode, boolean notify){
		if(notify){
			if(_chatMode==chatMode){
				switch(chatMode){
				case WORLD: sendMessage(ChatColor.YELLOW+"World "+ChatColor.WHITE+"chat is already activated."); break;
				case GAME: sendMessage(ChatColor.YELLOW+"Game "+ChatColor.WHITE+"chat is already activated."); break;
				case TEAM: sendMessage(ChatColor.YELLOW+"Team "+ChatColor.WHITE+"chat is already activated."); break;
				}
				return;
			}
			switch(chatMode){
			case WORLD: sendMessage("Switched to "+ChatColor.YELLOW+"world "+ChatColor.WHITE+"chat."); break;
			case GAME: sendMessage("Switched to "+ChatColor.YELLOW+"game "+ChatColor.WHITE+"chat."); break;
			case TEAM: sendMessage("Switched to "+ChatColor.YELLOW+"team "+ChatColor.WHITE+"chat."); break;
			}
		}
		_chatMode = chatMode;
	}
	
	public void setChatMode(ChatMode chatMode){
		setChatMode(chatMode, false);
	}
	
	
	
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
	
	public RMTemplate getTemplate(String name){
		return _templates.get(name);
	}
	
	public void setTemplate(RMTemplate template){
		_templates.put(template.getName(), template);
	}
	
	public boolean saveTemplate(RMTemplate template){
		if(template.isEmpty()){
			sendMessage("Cannot save an empty template!");
			return false;
		}
		String name = template.getName();
		if(!_templates.containsKey(name)){
			_templates.put(name, template);
			sendMessage("Successfully "+ChatColor.YELLOW+"saved "+ChatColor.WHITE+"template "+ChatColor.GREEN+name+ChatColor.WHITE+".");
			return true;
		}
		sendMessage("Template "+ChatColor.GREEN+name+ChatColor.WHITE+" already exists.");
		return false;
	}
	
	public RMTemplate loadTemplate(String name){
		if(_templates.containsKey(name)){
			return _templates.get(name);
		}
		sendMessage("Template "+ChatColor.GREEN+name+ChatColor.WHITE+" does not exist.");
		return null;
	}
	
	public boolean removeTemplate(String name){
		if(_templates.containsKey(name)){
			_templates.remove(name);
			sendMessage("Successfully "+ChatColor.GRAY+"removed "+ChatColor.WHITE+"template "+ChatColor.GREEN+name+ChatColor.WHITE+".");
			return true;
		}
		sendMessage("Template "+ChatColor.GREEN+name+ChatColor.WHITE+" does not exist.");
		return false;
	}
	
	public void removeTemplates(List<String> templates){
		for(String template : templates){
			removeTemplate(template.toLowerCase());
		}
	}
	
	public HashMap<String, RMTemplate> getTemplates(){
		return _templates;
	}
	
	public void setTemplates(HashMap<String, RMTemplate> templates){
		_templates = templates;
	}
	
	public void clearTemplates(){
		_templates.clear();
	}
	
	public void getInfoItems(){
		String items = RMTextHelper.getStringSortedItems(_items.getItems());
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Items: "+items);
		}
		else sendMessage(ChatColor.GRAY+"No items found.");
	}
	
	public void getInfoReward(){
		String items = RMTextHelper.getStringSortedItems(_reward.getItems());
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Reward items: "+items);
		}
		else sendMessage(ChatColor.GRAY+"No reward found.");
	}
	
	public void getInfoTools(){
		String items = RMTextHelper.getStringSortedItems(_tools.getItems());
		if(items.length()>0){
			sendMessage(ChatColor.YELLOW+"Tools items: "+items);
		}
		else sendMessage(ChatColor.GRAY+"No tools found.");
	}
	
	public void getInfoClaim(){
		int strLength = 74;
		String items = RMTextHelper.getStringSortedItems(_items.getItems(), 0);
		String reward = RMTextHelper.getStringSortedItems(_reward.getItems(), 0);
		String tools = RMTextHelper.getStringSortedItems(_tools.getItems(), 0);
		if(items.length()>strLength) items = items.substring(0, strLength)+"...";
		if(reward.length()>strLength) reward = reward.substring(0, strLength)+"...";
		if(tools.length()>strLength) tools = tools.substring(0, strLength)+"...";
		sendMessage("Items: "+ChatColor.GREEN+_items.size()+ChatColor.WHITE+" Total: "+ChatColor.GREEN+_items.getAmount()+ChatColor.WHITE+" "+items);
		sendMessage("Reward: "+ChatColor.GREEN+_reward.size()+ChatColor.WHITE+" Total: "+ChatColor.GREEN+_reward.getAmount()+ChatColor.WHITE+" "+reward);
		sendMessage("Tools: "+ChatColor.GREEN+_tools.size()+ChatColor.WHITE+" Total: "+ChatColor.GREEN+_tools.getAmount()+ChatColor.WHITE+" "+tools);
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
	public void setRequestString(String str){
		_requestString = str;
	}
	public String getRequestString(){
		return _requestString;
	}
	public void clearRequestString(){
		_requestString = "";
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
		warpToSafety(_team.getWarpLocation().clone());
	}
	
	public void warpToSafety(Location loc){
		if(getPlayer()!=null){
			Player p = getPlayer();
			loc = findSafeWarpLocation(loc); //Not yet working
			loc.setPitch(p.getLocation().getPitch());
			loc.setYaw(p.getLocation().getYaw());
			p.teleport(loc);
		}
	}
	
	public Location getReturnLocation(){
		return _returnLocation;
	}
	public void setReturnLocation(Location returnLocation){
		if(_returnLocation==null){
			_returnLocation = returnLocation;
			sendMessage("Marked return location.");
		}
		else sendMessage("Return location already marked.");
	}
	public void clearReturnLocation(){
		_returnLocation = null;
	}
	
	public void warpToReturnLocation(){
		warpToSafety(_returnLocation);
		sendMessage("Returned.");
		clearReturnLocation();
	}
	
	public Location findSafeWarpLocation(Location loc){
		return loc;
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
				switch(rmGame.getConfig().getState()){
					case GAMEPLAY: case PAUSED:
					return rmGame;
				}
			}
		}
		return null;
	}
	
	public void onPlayerJoin(){
		if(isIngame()){
			RMGameTimer timer = getGameInProgress().getConfig().getTimer();
			if(timer.getTimeLimit()!=0){
				if(timer.getTimeElapsed()>timer.getTimeLimit()) sendMessage(RMText.g_SuddenDeathColorized);
				else sendMessage(ChatColor.AQUA+timer.getTextTimeRemaining()+" remaining"); 
			}
		}
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
	
	public boolean hasOpPermission(){
		return hasOpPermission("resourcemadness.admin");
	}
	
	public boolean hasOpPermission(String node){
		Player p = getPlayer();
		if(p!=null){
			if(plugin.isPermissionEnabled()){
				if(p.isOp()) return true;
				else return (plugin.hasPermission(p, node));
			}
			if(p.isOp()) return true;
		}
		return false;
	}
	
	public boolean hasOwnerPermission(String ownerName){
		Player p = getPlayer();
		if(p!=null){
			if(plugin.isPermissionEnabled()){
				if(ownerName.equalsIgnoreCase(p.getName())) return true;
				else if(p.isOp()) return true;
				else return (plugin.hasPermission(p, "resourcemadness.admin"));
			}
			if(ownerName.equalsIgnoreCase(p.getName())) return true;
			else if(p.isOp()) return true;
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