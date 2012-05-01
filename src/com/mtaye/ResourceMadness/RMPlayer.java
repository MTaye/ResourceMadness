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

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMGame.FilterItemType;
import com.mtaye.ResourceMadness.RMGame.FilterState;
import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPlayer {
	public enum PlayerAction{
		ADD, REMOVE, INFO, INFO_FOUND, MODE, MODE_CYCLE, SETTINGS, SETTINGS_RESET,
		JOIN, JOIN_PASSWORD, QUIT, START, START_RANDOM, RESTART, STOP, PAUSE, RESUME,
		TEMPLATE_LIST, TEMPLATE_SAVE, TEMPLATE_LOAD, TEMPLATE_REMOVE, RESTORE,
		FILTER, FILTER_INFO, FILTER_INFO_STRING, REWARD, REWARD_INFO, REWARD_INFO_STRING, TOOLS, TOOLS_INFO, TOOLS_INFO_STRING,
		MONEY, MONEY_INFO,
		CLAIM_FOUND, CLAIM_FOUND_CHEST, CLAIM_FOUND_CHEST_SELECT, CLAIM_ITEMS_CHEST, CLAIM_REWARD_CHEST, CLAIM_TOOLS_CHEST,
		KICK_PLAYER, KICK_TEAM, KICK_ALL, BAN_PLAYER, BAN_TEAM, BAN_ALL, BAN_LIST, UNBAN_PLAYER,
		SET_MIN_PLAYERS, SET_MAX_PLAYERS, SET_MIN_TEAM_PLAYERS, SET_MAX_TEAM_PLAYERS, SET_MAX_ITEMS, SET_SAFE_ZONE, SET_TIME_LIMIT, SET_RANDOM,
		SET_PASSWORD, SET_ADVERTISE, SET_RESTORE, SET_WARP, SET_MIDGAME_JOIN, SET_HEAL_PLAYER, SET_CLEAR_INVENTORY, SET_FOUND_AS_REWARD,
		SET_WARN_UNEQUAL, SET_ALLOW_UNEQUAL, SET_WARN_HACKED, SET_ALLOW_HACKED, SET_INFINITE_REWARD, SET_INFINITE_TOOLS,
		NONE;
	}
	
	public enum ChatMode { WORLD, GAME, TEAM };
	
	private String _name;
	private RMTeam _team;
	//private List<RMGame> _games;
	private RMRequestFilter _requestFilter;
	private RMRequestMoney _requestMoney;
	private Block _requestBlock;
	private ItemStack[] _requestItems;
	private int _requestInt = 0;
	private boolean _requestBool = false;
	private String _requestString = "";
	private InterfaceState _requestInterface = InterfaceState.FILTER;
	private List<String> _requestStringList;
	private String[] _requestStringArray;
	private FilterType _filterType = FilterType.NONE;
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
		case WORLD: rm.getServer().broadcastMessage(message); break;
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
				case WORLD: sendMessage(RMText.getLabel("chat.world.already_active")); break;
				case GAME: sendMessage(RMText.getLabel("chat.game.already_active")); break;
				case TEAM: sendMessage(RMText.getLabel("chat.team.already_active")); break;
				}
				return;
			}
			switch(chatMode){
			case WORLD: sendMessage(RMText.getLabel("chat.world.switched")); break;
			case GAME: sendMessage(RMText.getLabel("chat.game.switched")); break;
			case TEAM: sendMessage(RMText.getLabel("chat.team.switched")); break;
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
			sendMessage(RMText.getLabel("template.save.empty"));
			return false;
		}
		String name = template.getName();
		if(!_templates.containsKey(name)){
			_templates.put(name, template);
			sendMessage(RMText.getLabelArgs("template.save", name));
			return true;
		}
		sendMessage(RMText.getLabelArgs("template.already_exists", name));
		return false;
	}
	
	public RMTemplate loadTemplate(String name){
		if(_templates.containsKey(name)){
			return _templates.get(name);
		}
		sendMessage(RMText.getLabelArgs("template.does_not_exist", name));
		return null;
	}
	
	public boolean removeTemplate(String name){
		if(_templates.containsKey(name)){
			_templates.remove(name);
			sendMessage(RMText.getLabelArgs("template.remove", name));
			return true;
		}
		sendMessage(RMText.getLabelArgs("template.does_not_exist", name));
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
			sendMessage(RMText.getLabelArgs("info.items", items));
		}
		else sendMessage(RMText.getLabel("info.items.empty"));
	}
	
	public void getInfoReward(){
		String items = RMTextHelper.getStringSortedItems(_reward.getItems());
		if(items.length()>0){
			sendMessage(RMText.getLabelArgs("info.reward", items));
		}
		else sendMessage(RMText.getLabel("info.reward.empty"));
	}
	
	public void getInfoTools(){
		String items = RMTextHelper.getStringSortedItems(_tools.getItems());
		if(items.length()>0){
			sendMessage(RMText.getLabelArgs("info.tools", items));
		}
		else sendMessage(RMText.getLabel("info.tools.empty"));
	}
	
	public void getInfoClaim(){
		int strLength = 74;
		String items = RMTextHelper.getStringSortedItems(_items.getItems(), 0);
		String reward = RMTextHelper.getStringSortedItems(_reward.getItems(), 0);
		String tools = RMTextHelper.getStringSortedItems(_tools.getItems(), 0);
		if(items.length()>strLength) items = items.substring(0, strLength)+"...";
		if(reward.length()>strLength) reward = reward.substring(0, strLength)+"...";
		if(tools.length()>strLength) tools = tools.substring(0, strLength)+"...";
		sendMessage(RMText.getLabel("info.claim.items")+": "+ChatColor.GREEN+_items.size()+ChatColor.WHITE+" "+RMText.getLabel("info.claim.total")+": "+ChatColor.GREEN+_items.getAmount()+ChatColor.WHITE+" "+items);
		sendMessage(RMText.getLabel("info.claim.reward")+": "+ChatColor.GREEN+_reward.size()+ChatColor.WHITE+" "+RMText.getLabel("info.claim.total")+": "+ChatColor.GREEN+_reward.getAmount()+ChatColor.WHITE+" "+reward);
		sendMessage(RMText.getLabel("info.claim.tools")+": "+ChatColor.GREEN+_tools.size()+ChatColor.WHITE+" "+RMText.getLabel("info.claim.total")+": "+ChatColor.GREEN+_tools.getAmount()+ChatColor.WHITE+" "+tools);
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
			case FOUND:	sendMessage(RMText.getLabel("claim.found.none")); break;
			case ITEMS:	sendMessage(RMText.getLabel("claim.items.none")); break;
			case REWARD: sendMessage(RMText.getLabel("claim.reward.none")); break;
			case TOOLS:	sendMessage(RMText.getLabel("claim.tools.none")); break;
			default:
		}
	}
	
	public String getClaimFail(ClaimType claimType){
		switch(claimType){
		case FOUND:	return RMText.getLabel("claim.found.fail");
		case ITEMS:	return RMText.getLabel("claim.items.fail");
		case REWARD: return RMText.getLabel("claim.reward.fail");
		case TOOLS:	return RMText.getLabel("claim.tools.fail");
		default: return "";
		}
	}
	
	public String getClaimSuccess(ClaimType claimType){
		switch(claimType){
		case FOUND:	return RMText.getLabel("claim.found.success");
		case ITEMS:	return RMText.getLabel("claim.items.success");
		case REWARD: return RMText.getLabel("claim.reward.success");
		case TOOLS:	return RMText.getLabel("claim.tools.success");
		default: return "";
		}
	}
	
	public String getClaimRemaining(int remaining){
		if(remaining>0) return RMText.getLabelArgs("claim.remaining", ""+remaining);
		return "";
	}
	
	public void announceFull(List<Boolean> checkFull, ClaimType claimType){
		String message = "";
		for(Boolean playerInv : checkFull){
			if(playerInv) message += RMText.getLabel("claim.inv_full")+" ";
			else message += RMText.getLabel("claim.chest_full")+" ";
		}
		sendMessage(message.trim());
		sendMessage(getClaimFail(claimType));
		
	}
	public void announceRemaining(List<Boolean> checkFull, List<Boolean> checkCheck, ClaimType claimType, int remaining){
		String message = "";
		for(Boolean playerInv : checkFull){
			if(playerInv) message += RMText.getLabel("claim.inv_full")+" ";
			else message += RMText.getLabel("claim.chest_full")+" ";
		}
		sendMessage(message.trim());
		message = getClaimRemaining(remaining)+" ";
		for(Boolean playerInv : checkCheck){
			if(playerInv) message += RMText.getLabel("claim.check_inv")+" ";
			else message += RMText.getLabel("claim.check_chest")+" ";
		}
		sendMessage(message.trim());
	}
	public void announceAllReturned(List<Boolean> checkFull, List<Boolean> checkCheck, ClaimType claimType){
		String message = "";
		/*
		for(Boolean playerInv : checkFull){
			if(playerInv) message += RMText.getLabel("claim.inv_full")+" ";
			else message += RMText.getLabel("claim.chest_full")+" ";
		}
		*/
		message += getClaimSuccess(claimType)+" ";
		for(Boolean playerInv : checkCheck){
			if(playerInv) message += RMText.getLabel("claim.check_inv")+" ";
			else message += RMText.getLabel("claim.check_chest")+" ";
		}
		sendMessage(message.trim());
	}

	//Claim to Inventory
	public HashMap<Integer, ItemStack> claimToInventory(RMStash rmStash, Inventory inv, Inventory invBackup, boolean claimWhole, ClaimType claimType, ItemStack... items){
		HashMap<Integer, ItemStack> returnedItems = new HashMap<Integer, ItemStack>();
		if(rmStash.size()==0){
			announceNoItemsToGive(claimType);
			return returnedItems;
		}
		List<Boolean> checkFull = new ArrayList<Boolean>();
		List<Boolean> checkCheck = new ArrayList<Boolean>();
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
		if(inv!=null){
			returnedItems = inv.addItem(stashItems.toArray(new ItemStack[stashItems.size()]));
			if(inv.getContents().length==36){
				if(!checkCheck.contains(true)) checkCheck.add(true);
			}
			else if(!checkCheck.contains(false)) checkCheck.add(false);
			if(returnedItems.size()!=0){
				if(inv.getContents().length==36){
					if(!checkFull.contains(true)) checkFull.add(true);
				}
				else if(!checkFull.contains(false)) checkFull.add(false);
			}
		}
	
		if((invBackup!=null)&&(invBackup!=inv)){
			if(returnedItems.size()!=0){
				if(inv!=null) returnedItems = invBackup.addItem(returnedItems.values().toArray(new ItemStack[returnedItems.size()]));
				else returnedItems = invBackup.addItem(stashItems.toArray(new ItemStack[stashItems.size()]));
				if(invBackup.getContents().length==36){
					if(!checkCheck.contains(true)) checkCheck.add(true);
				}
				else if(!checkCheck.contains(false)) checkCheck.add(false);
				if(returnedItems.size()!=0){
					if(invBackup.getContents().length==36){
						if(!checkFull.contains(true)) checkFull.add(true);
					}
					else if(!checkFull.contains(false)) checkFull.add(false);
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
		if(rmStashAmount==rmStash.getAmount()){
			announceFull(checkFull, claimType);
		}
		else if(returnedItems.size()>0){
			announceRemaining(checkFull, checkCheck, claimType, rmStash.getAmount());
		}
		else announceAllReturned(checkFull, checkCheck, claimType);
		return returnedItems;
	}
	
	//Claim
	public HashMap<Integer, ItemStack> claim(RMStash rmStash, ClaimType claimType, ItemStack... items){
		if(getPlayer()==null) return null;
		return claimToInventory(rmStash, getPlayer().getInventory(), null, false, claimType, items);
	}
	//Claim
	public HashMap<Integer, ItemStack> claim(RMStash rmStash, ClaimType claimType, boolean claimWhole, ItemStack... items){
		if(getPlayer()==null) return null;
		return claimToInventory(rmStash, getPlayer().getInventory(), null, claimWhole, claimType, items);
	}
	
	public void claimItemsToChest(Block b, Inventory invBackup, ItemStack... items){
		claimToChest(b, ClaimType.ITEMS, invBackup, items);
	}
	public void claimRewardToChest(Block b, Inventory invBackup, ItemStack... items){
		claimToChest(b, ClaimType.REWARD, invBackup, items);
	}
	public void claimToolsToChest(Block b, Inventory invBackup, ItemStack... items){
		claimToChest(b, ClaimType.TOOLS, invBackup, items);
	}
	public void claimToChest(Block b, ClaimType claimType, Inventory invBackup, ItemStack... items){
		if(b.getType()!=Material.CHEST) return;
		RMInventory rmInventory = new RMInventory((Chest)b.getState());
		
		switch(claimType){
		case FOUND: claimToInventory(RMGame.getGame(getRequestInt()).getGameConfig().getFound(), rmInventory, invBackup, false, ClaimType.FOUND, items); break;
		case ITEMS: claimToInventory(_items, rmInventory, invBackup, false, ClaimType.ITEMS, items); break;
		case REWARD: claimToInventory(_reward, rmInventory, invBackup, false, ClaimType.REWARD, items); break;
		case TOOLS: claimToInventory(_tools, rmInventory, invBackup, false, ClaimType.TOOLS, items); break;
		}
	}
	public HashMap<Integer, ItemStack> claimStashToChest(RMStash stash, Block b, ClaimType claimType, Inventory invBackup, ItemStack... items){
		HashMap<Integer, ItemStack> returnItems = new HashMap<Integer, ItemStack>();
		RMInventory rmInventory = null;
		if((b!=null)&&(b.getType()==Material.CHEST)) rmInventory = new RMInventory((Chest)b.getState());
		
		switch(claimType){
		case FOUND: returnItems = claimToInventory(stash, rmInventory, invBackup, false, ClaimType.FOUND, items); break;
		case ITEMS: returnItems = claimToInventory(stash, rmInventory, invBackup, false, ClaimType.ITEMS, items); break;
		case REWARD: returnItems = claimToInventory(stash, rmInventory, invBackup, false, ClaimType.REWARD, items); break;
		case TOOLS: returnItems = claimToInventory(stash, rmInventory, invBackup, false, ClaimType.TOOLS, items); break;
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
	
	public Inventory getInventory(){
		Player p = getPlayer();
		if(p!=null) return p.getInventory();
		return null;
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
	
	public void setRequestFilterType(FilterType filterType){
		_filterType = filterType;
	}
	public FilterType getRequestFilterType(){
		return _filterType;
	}
	public void clearRequestFilterType(){
		_filterType = FilterType.NONE;
	}
	
	public static RM rm;
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
	
	public void setRequestFilter(HashMap<Integer, RMItem> items, FilterState state, FilterItemType filterItemType, FilterType filterType, int randomize){
		_requestFilter = new RMRequestFilter(items,state,filterItemType,filterType,randomize);
	}
	public RMRequestMoney getRequestMoney(){
		if(_requestMoney!=null) return _requestMoney;
		return null;
	}
	public void clearRequestMoney(){
		_requestMoney = null;
	}
	public void setRequestMoney(RMRequestMoney requestMoney){
		_requestMoney = requestMoney;
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
	public String[] getRequestStringArray(){
		return _requestStringArray;
	}
	public void setRequestStringArray(String[] list){
		_requestStringArray = list;
	}
	public void clearRequestStringArray(){
		_requestStringArray = null;
	}
	public List<String> getRequestStringList(){
		return _requestStringList;
	}
	public void setRequestStringList(List<String> list){
		_requestStringList = list;
	}
	public void clearRequestStringList(){
		_requestStringList.clear();
	}
	
	public void setRequestBlock(Block block){
		_requestBlock = block;
	}
	public Block getRequestBlock(){
		return _requestBlock;
	}
	
	//Player GET/SET
	public String getName(){
		return _name;
	}
	public void setName(Player player){
		_name = player.getName();
	}
	
	public Player getPlayer(){
		return rm.getServer().getPlayer(_name);
	}
	private void setPlayer(String player){
		_name = player;
	}
	public static HashMap<String, RMPlayer> getPlayers(){
		return _players;
	}
	public static RMPlayer getPlayerByName(String name){
		if(name.length()==0) return null;
		for(String player : _players.keySet()){
			if(player.equalsIgnoreCase(name)){
				return _players.get(player);
			}
		}
		Player p = rm.getServer().getPlayer(name);
		if(p!=null){
			RMPlayer rmp = new RMPlayer(p.getName());
			_players.put(name, rmp);
			return rmp;
		}
		return null;
	}
	
	public static RMPlayer getPlayerByNameOnly(String name){
		if(name.length()==0) return null;
		for(String player : _players.keySet()){
			if(player.equalsIgnoreCase(name)){
				return _players.get(player);
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
			if(rmGame.getGameConfig().getOwnerName().equalsIgnoreCase(getPlayer().getName())){
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
	
	public RMGame getGame(){
		RMTeam rmTeam = getTeam();
		if(rmTeam!=null){
			RMGame rmGame = rmTeam.getGame();
			if(rmGame!=null){
				return rmGame;
			}
		}
		return null;
	}
	
	public RMGame getGameInProgress(){
		RMGame rmGame = getGame();
		if(rmGame!=null){
			switch(rmGame.getGameConfig().getState()){
			case COUNTDOWN: case GAMEPLAY: case PAUSED:
				return rmGame;
			}
		}
		return null;
	}
	
	public void onPlayerJoin(){
		if(isIngame()){
			RMGameTimer timer = getGameInProgress().getGameConfig().getTimer();
			if(timer.getTimeLimit()!=0){
				if(timer.getTimeElapsed()>timer.getTimeLimit()) sendMessage(RMText.getLabelColorize("game.sudden_death", ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE));
				else sendMessage(RMText.getLabelArgs("time.remaining", timer.getTextTimeRemaining())); 
			}
		}
	}
	
	public void onPlayerQuit(RMTeam rmTeam){
		setReady(false);
		if(rmTeam!=null){
			RMGame rmGame = rmTeam.getGame();
			if(rmGame!=null){
				switch(rmGame.getGameConfig().getState()){
				case GAMEPLAY: case PAUSED:
					rmGame.checkPlayerQuit(this, rmTeam);
					break;
				}
			}
		}
	}
	
	public boolean inSafeZone(){
		RMGame rmGame = getGame();
		if(rmGame!=null){
			int safeZone = rmGame.getGameConfig().getSettingInt(Setting.safezone);
			Block mainBlock = rmGame.getMainBlock();
			Location loc = getPlayer().getLocation();
			if(Math.abs(mainBlock.getX()-loc.getBlockX())>safeZone) return false;
			if(Math.abs(mainBlock.getY()-loc.getBlockY())>safeZone) return false;
			if(Math.abs(mainBlock.getZ()-loc.getBlockZ())>safeZone) return false;
			return true;
		}
		return false;
	}
	
	public boolean isSafe(){
		if(inSafeZone()) return true;
		return false;
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
			if(rm.isPermissionEnabled()){
				return (rm.hasPermission(p, node));
			}
			else if(p.isOp()) return true;
		}
		return false;
	}
	
	public boolean hasOwnerPermission(String ownerName){
		Player p = getPlayer();
		if(p!=null){
			if(rm.isPermissionEnabled()){
				if(ownerName.equalsIgnoreCase(p.getName())) return true;
				else if(p.isOp()) return true;
				else return (rm.hasPermission(p, "resourcemadness.admin"));
			}
			else if(ownerName.equalsIgnoreCase(p.getName())) return true;
			else if(p.isOp()) return true;
		}
		return false;
	}
	
	public boolean hasPermission(String node){
		Player p = getPlayer();
		if(p!=null) return (rm.hasPermission(p, node));
		return false;
	}
	
	public void restoreHealth(){
		Player p = getPlayer();
		if(p!=null)	p.setHealth(20);
	}
}