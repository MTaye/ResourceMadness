package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.Stats.RMStat;
import com.mtaye.ResourceMadness.Game.FilterType;
import com.mtaye.ResourceMadness.Game.InterfaceState;
import com.mtaye.ResourceMadness.helper.Helper;
import com.mtaye.ResourceMadness.helper.InventoryHelper;
import com.mtaye.ResourceMadness.helper.TextHelper;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.time.PvpTimer;
import com.mtaye.ResourceMadness.time.Timer;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class GamePlayer {
	public enum PlayerAction{
		CREATE, REMOVE,
		INFO, INFO_FOUND, INFO_FILTER, INFO_FILTER_STRING, INFO_REWARD, INFO_REWARD_STRING, INFO_TOOLS, INFO_TOOLS_STRING,
		MODE, MODE_CYCLE, SETTINGS, SETTINGS_RESET,
		JOIN, JOIN_PASSWORD, QUIT, START, START_RANDOM, RESTART, STOP, PAUSE, RESUME,
		TEMPLATE_LIST, TEMPLATE_SAVE, TEMPLATE_LOAD, TEMPLATE_REMOVE, RESTORE,
		UNDO,
		FILTER, REWARD,	TOOLS,
		MONEY, MONEY_INFO,
		CLAIM_FOUND, CLAIM_FOUND_CHEST, CLAIM_FOUND_CHEST_SELECT,
		CLAIM_ITEMS_CHEST, CLAIM_REWARD_CHEST, CLAIM_TOOLS_CHEST, CLAIM_INFO_FOUND,
		KICK_PLAYER, KICK_TEAM, KICK_ALL, BAN_PLAYER, BAN_TEAM, BAN_ALL, BAN_LIST, UNBAN_PLAYER,
		SET,
		NONE;
	}

	public enum ChatMode { WORLD, GAME, TEAM };
	
	public static RM rm;
	private static TreeMap<String, GamePlayer> _players = new TreeMap<String, GamePlayer>();
	
	private PlayerAction _playerAction = PlayerAction.NONE;
	
	private String _name;
	private Team _team;
	//private List<RMGame> _games;
	private RequestFilter _requestFilter;
	private RequestMoney _requestMoney;
	private Block _requestBlock;
	private ItemStack[] _requestItems;
	private int _requestInt = 0;
	private IntRange _requestIntegerRange = new IntRange();
	private boolean _requestBool = false;
	private String _requestString = "";
	private InterfaceState _requestInterface = InterfaceState.FILTER;
	private List<String> _requestStringList;
	private String[] _requestStringArray;
	private Setting _requestSetting = null; 
	private FilterType _filterType = FilterType.NONE;
	private Stats _stats = new Stats();
	private Stash _items = new Stash();
	private Stash _reward = new Stash();
	private Stash _tools = new Stash();
	private TreeMap<String, Template> _templates = new TreeMap<String, Template>();
	private boolean _ready = false;
	private ChatMode _chatMode = ChatMode.WORLD;
	private Timer _playAreaTimer = new Timer(0, 0);
	private int _detectedEnemies = 0;
	private boolean _inSafeZone = false;
	private PlayerDrops _drops;
	private RequestFilter _requestFilterBackup;
	private String _selectedGame = null;
	
	private boolean _updateInventory = false;
	private Location _warpTeamLocation;
	private Location _warpReturnLocation;
	private boolean _warpToReturnLocation = false;
	
	private InventoryState _inventoryState = new InventoryState();
	private GamePlayerInventory _inventory = new GamePlayerInventory();
	
	private int _health;
	private int _foodLevel;
	private Location _location;
	//private ItemStack[] _inventoryStack;
	//private ItemStack[] _inventoryArmorStack;
	private Stats _matchStats = new Stats();
	
	public void setSelectedGame(String name){
		_selectedGame = name;
	}
	
	public String getSelectedGame(){
		return _selectedGame;
	}
	
	public void clearSelectedGame(){
		_selectedGame = null;
	}
	
	public void setInventoryState(InventoryState inventoryState){
		_inventoryState = inventoryState;
	}
	
	public InventoryState getInventoryState(){
		return _inventoryState;
	}
	
	public void clearInventoryState(){
		_inventoryState.clear();
	}
	
	/*
	public ItemStack[] getInventoryStack(){
		return _inventoryStack;
	}

	public void setInventoryStack(ItemStack[] items){
		_inventoryStack = items;
	}
	
	public ItemStack[] getInventoryArmorStack(){
		return _inventoryArmorStack;
	}
	public void setInventoryArmorStack(ItemStack[] items){
		_inventoryArmorStack = items;
	}
	*/
	
	public void addStat(RMStat stat){
		_stats.add(stat);
		_matchStats.add(stat);
	}
	
	public void addStat(RMStat stat, int value){
		_stats.add(stat, value);
		_matchStats.add(stat, value);
	}
	
	public void clearStat(RMStat stat){
		_stats.add(stat);
		_matchStats.clear(stat);
	}
	
	public void clearStats(){
		_stats.clearAll();
		_matchStats.clearAll();
	}
	
	public void setUpdateInventory(boolean bool){
		_updateInventory = bool;
	}
	public boolean getUpdateInventory(){
		return _updateInventory;
	}
	
	public void setWarpTeamLocation(Location loc){
		_warpTeamLocation = loc;
	}
	public Location getWarpTeamLocation(){
		return _warpTeamLocation;
	}
	public void setWarpToReturnLocation(boolean bool){
		_warpToReturnLocation = bool;
	}
	public boolean getWarpToReturnLocation(){
		return _warpToReturnLocation;
	}

	public void setRequestFilterBackup(RequestFilter requestFilter){
		_requestFilterBackup = requestFilter.clone();
	}
	public RequestFilter getRequestFilterBackup(){
		return _requestFilterBackup;
	}
	public void clearRequestFilterBackup(){
		_requestFilterBackup = null;
	}
	
	public void setPlayAreaTimer(Timer timer){
		_playAreaTimer = timer;
	}
	
	public Timer getPlayAreaTimer(){
		return _playAreaTimer;
	}
	
	public void setRequestSetting(Setting setting){
		_requestSetting = setting;
	}
	
	public Setting getRequestSetting(){
		return _requestSetting;
	}
	
	public void clearRequestSetting(){
		_requestSetting = null;
	}
	
	public String getChatMessage(ChatMode chatMode, String message){
		String str = "";
		if(message.length()==0) return str;
		Team team = getTeam();
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
				case WORLD: sendMessage(Text.getLabel("chat.world.already_active")); break;
				case GAME: sendMessage(Text.getLabel("chat.game.already_active")); break;
				case TEAM: sendMessage(Text.getLabel("chat.team.already_active")); break;
				}
				return;
			}
			switch(chatMode){
			case WORLD: sendMessage(Text.getLabel("chat.world.switched")); break;
			case GAME: sendMessage(Text.getLabel("chat.game.switched")); break;
			case TEAM: sendMessage(Text.getLabel("chat.team.switched")); break;
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
	
	public Template getTemplate(String name){
		return _templates.get(name);
	}
	
	public void setTemplate(Template template){
		_templates.put(template.getName(), template);
	}
	
	public boolean saveTemplate(Template template){
		if(template.isEmpty()){
			sendMessage(Text.getLabel("template.save.empty"));
			return false;
		}
		String name = template.getName();
		if(!_templates.containsKey(name)){
			_templates.put(name, template);
			sendMessage(Text.getLabelArgs("template.save", name));
			return true;
		}
		sendMessage(Text.getLabelArgs("template.already_exists", name));
		return false;
	}
	
	public Template loadTemplate(String name){
		if(_templates.containsKey(name)){
			return _templates.get(name);
		}
		sendMessage(Text.getLabelArgs("template.does_not_exist", name));
		return null;
	}
	
	public boolean removeTemplate(String name){
		if(_templates.containsKey(name)){
			_templates.remove(name);
			sendMessage(Text.getLabelArgs("template.remove", name));
			return true;
		}
		sendMessage(Text.getLabelArgs("template.does_not_exist", name));
		return false;
	}
	
	public void removeTemplates(List<String> templates){
		for(String template : templates){
			removeTemplate(template.toLowerCase());
		}
	}
	
	public TreeMap<String, Template> getTemplates(){
		return _templates;
	}
	
	public void setTemplates(TreeMap<String, Template> templates){
		_templates = templates;
	}
	
	public void clearTemplates(){
		_templates.clear();
	}
	
	public void getInfoItems(){
		String items = TextHelper.getStringSortedItems(_items.getItems());
		if(items.length()>0){
			sendMessage(Text.getLabelArgs("info.claim.items", items));
		}
		else sendMessage(Text.getLabel("info.claim.items.empty"));
	}
	
	public void getInfoReward(){
		String items = TextHelper.getStringSortedItems(_reward.getItems());
		if(items.length()>0){
			sendMessage(Text.getLabelArgs("info.claim.reward", items));
		}
		else sendMessage(Text.getLabel("info.claim.reward.empty"));
	}
	
	public void getInfoTools(){
		String items = TextHelper.getStringSortedItems(_tools.getItems());
		if(items.length()>0){
			sendMessage(Text.getLabelArgs("info.claim.tools", items));
		}
		else sendMessage(Text.getLabel("info.claim.tools.empty"));
	}
	
	public void getInfoClaim(){
		int strLength = 74;
		String items = TextHelper.getStringSortedItems(_items.getItems(), 0);
		String reward = TextHelper.getStringSortedItems(_reward.getItems(), 0);
		String tools = TextHelper.getStringSortedItems(_tools.getItems(), 0);
		if(items.length()>strLength) items = items.substring(0, strLength)+"...";
		if(reward.length()>strLength) reward = reward.substring(0, strLength)+"...";
		if(tools.length()>strLength) tools = tools.substring(0, strLength)+"...";
		sendMessage(Text.getLabel("claim.info.items")+": "+ChatColor.GREEN+_items.size()+ChatColor.WHITE+" "+Text.getLabel("claim.info.total")+": "+ChatColor.GREEN+_items.getAmount()+ChatColor.WHITE+" "+items);
		sendMessage(Text.getLabel("claim.info.reward")+": "+ChatColor.GREEN+_reward.size()+ChatColor.WHITE+" "+Text.getLabel("claim.info.total")+": "+ChatColor.GREEN+_reward.getAmount()+ChatColor.WHITE+" "+reward);
		sendMessage(Text.getLabel("claim.info.tools")+": "+ChatColor.GREEN+_tools.size()+ChatColor.WHITE+" "+Text.getLabel("claim.info.total")+": "+ChatColor.GREEN+_tools.getAmount()+ChatColor.WHITE+" "+tools);
	}
	
	public Stash getItems(){
		return _items;
	}
	public Stash getReward(){
		return _reward;
	}
	public Stash getTools(){
		return _tools;
	}
	public void setItems(Stash items){
		_items = items;
		_items.clearChanged();
	}
	public void setReward(Stash reward){
		_reward = reward;
		_reward.clearChanged();
	}
	public void setTools(Stash tools){
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
		List<ItemStack> items = new ArrayList<ItemStack>();
		Inventory inv = getInventory();
		if(inv==null) return items;
		for(ItemStack item : inv.getContents()){
			if(item!=null) items.add(item);
		}
		return items;
	}
	
	public void addItemsFromInventory(Stash rmStash){
		addItemsFromInventory(rmStash, false);
	}
	
	public void addItemsFromInventory(Stash rmStash, boolean addArmor){
		GamePlayerInventory inv = getInventory();
		if(inv==null) return;
		rmStash.addItems(inv.getContents());
		if(addArmor){
			rmStash.addItems(inv.getArmorContents());
			inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
		}
		inv.clear();
	}
	
	public void addArmorFromInventory(Stash rmStash){
		//_items = getItemsFromInventory();
		GamePlayerInventory inv = getInventory();
		if(inv==null) return;
		ItemStack[] contents = inv.getArmorContents();
		for(ItemStack item : contents){
			rmStash.addItem(item);
		}
		inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
	}
	
	public void announceNoItemsToGive(ClaimType claimType){
		switch(claimType){
			case FOUND:	sendMessage(Text.getLabel("claim.found.none")); break;
			case ITEMS:	sendMessage(Text.getLabel("claim.items.none")); break;
			case REWARD: sendMessage(Text.getLabel("claim.reward.none")); break;
			case TOOLS:	sendMessage(Text.getLabel("claim.tools.none")); break;
			default:
		}
	}
	
	public String getClaimFail(ClaimType claimType){
		switch(claimType){
		case FOUND:	return Text.getLabel("claim.found.fail");
		case ITEMS:	return Text.getLabel("claim.items.fail");
		case REWARD: return Text.getLabel("claim.reward.fail");
		case TOOLS:	return Text.getLabel("claim.tools.fail");
		default: return "";
		}
	}
	
	public String getClaimSuccess(ClaimType claimType){
		switch(claimType){
		case FOUND:	return Text.getLabel("claim.found.success");
		case ITEMS:	return Text.getLabel("claim.items.success");
		case REWARD: return Text.getLabel("claim.reward.success");
		case TOOLS:	return Text.getLabel("claim.tools.success");
		default: return "";
		}
	}
	
	public String getClaimRemaining(int remaining){
		if(remaining>0) return Text.getLabelArgs("claim.remaining", ""+remaining);
		return "";
	}
	
	public void announceFull(List<Boolean> checkFull, ClaimType claimType){
		String message = "";
		for(Boolean playerInv : checkFull){
			if(playerInv) message += Text.getLabel("claim.inv_full")+" ";
			else message += Text.getLabel("claim.chest_full")+" ";
		}
		sendMessage(message.trim());
		sendMessage(getClaimFail(claimType));
		
	}
	public void announceRemaining(List<Boolean> checkFull, List<Boolean> checkCheck, ClaimType claimType, int remaining){
		String message = "";
		for(Boolean playerInv : checkFull){
			if(playerInv) message += Text.getLabel("claim.inv_full")+" ";
			else message += Text.getLabel("claim.chest_full")+" ";
		}
		sendMessage(message.trim());
		message = getClaimRemaining(remaining)+" ";
		for(Boolean playerInv : checkCheck){
			if(playerInv) message += Text.getLabel("claim.check_inv")+" ";
			else message += Text.getLabel("claim.check_chest")+" ";
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
			if(playerInv) message += Text.getLabel("claim.check_inv")+" ";
			else message += Text.getLabel("claim.check_chest")+" ";
		}
		sendMessage(message.trim());
	}

	//Claim to Inventory
	public HashMap<Integer, ItemStack> claimToInventory(Stash rmStash, Inventory inv, Inventory invBackup, boolean claimWhole, ClaimType claimType, ItemStack... items){
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
			Stash stashStashItems = new Stash(stashItems);
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
	public HashMap<Integer, ItemStack> claim(Stash rmStash, ClaimType claimType, ItemStack... items){
		Inventory inv = getInventory();
		if(inv==null) return null;
		return claimToInventory(rmStash, inv, null, false, claimType, items);
	}
	//Claim
	public HashMap<Integer, ItemStack> claim(Stash rmStash, ClaimType claimType, boolean claimWhole, ItemStack... items){
		Inventory inv = getInventory();
		if(inv==null) return null;
		return claimToInventory(rmStash, inv, null, claimWhole, claimType, items);
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
		GameInventory rmInventory = new GameInventory((Chest)b.getState());
		
		switch(claimType){
		case FOUND: claimToInventory(Game.getGame(getRequestInt()).getGameConfig().getFound(), rmInventory, invBackup, false, ClaimType.FOUND, items); break;
		case ITEMS: claimToInventory(_items, rmInventory, invBackup, false, ClaimType.ITEMS, items); break;
		case REWARD: claimToInventory(_reward, rmInventory, invBackup, false, ClaimType.REWARD, items); break;
		case TOOLS: claimToInventory(_tools, rmInventory, invBackup, false, ClaimType.TOOLS, items); break;
		}
	}
	public HashMap<Integer, ItemStack> claimStashToChest(Stash stash, Block b, ClaimType claimType, Inventory invBackup, ItemStack... items){
		HashMap<Integer, ItemStack> returnItems = new HashMap<Integer, ItemStack>();
		GameInventory rmInventory = null;
		if((b!=null)&&(b.getType()==Material.CHEST)) rmInventory = new GameInventory((Chest)b.getState());
		
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
	
	public Stats getStats(){
		return _stats;
	}
	public Stats getMatchStats(){
		return _matchStats;
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
		_requestIntegerRange.clear();
	}
	
	//Request Integer Range
	public void setRequestIntegerRange(IntRange range){
		_requestIntegerRange = range.clone();
	}
	public IntRange getRequestIntegerRange(){
		IntRange requestIntegerRange = _requestIntegerRange.clone();
		clearRequestIntegerRange();
		return requestIntegerRange;
	}
	public void clearRequestIntegerRange(){
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
		String requestString = _requestString;
		clearRequestString();
		return requestString;
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
	
	//Constructor
	public GamePlayer(String player){
		_name = player;
		_playerAction = PlayerAction.NONE;
		_players.put(player, this);
	}
	public GamePlayer(String player, PlayerAction playerAction){
		_name = player;
		_playerAction = PlayerAction.NONE;
	}
	
	public RequestMoney getRequestMoney(){
		if(_requestMoney!=null) return _requestMoney;
		return null;
	}
	public void clearRequestMoney(){
		_requestMoney = null;
	}
	public void setRequestMoney(RequestMoney requestMoney){
		_requestMoney = requestMoney;
	}
	
	public void setRequestFilter(RequestFilter requestFilter){
		_requestFilter = requestFilter;
	}
	public RequestFilter getRequestFilter(){
		return _requestFilter;
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
	public void setRequestStringArray(String... list){
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
	public static TreeMap<String, GamePlayer> getPlayers(){
		return _players;
	}
	
	public static GamePlayer[] getPlayersArray(){
		return _players.values().toArray(new GamePlayer[_players.size()]);
	}
	
	public static GamePlayer getPlayerByName(String name){
		if(name.length()==0) return null;
		for(String player : _players.keySet()){
			if(player.equalsIgnoreCase(name)){
				return _players.get(player);
			}
		}
		Player p = rm.getServer().getPlayer(name);
		if(p!=null){
			GamePlayer rmp = new GamePlayer(p.getName());
			_players.put(name, rmp);
			return rmp;
		}
		return null;
	}
	
	public static GamePlayer getPlayerByNameOnly(String name){
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
	public void joinTeam(Team team){
		team.addPlayer(this);
		_team = team;
	}
	public void quitTeam(Team team){
		team.removePlayer(this);
		_team = null;
	}
	
	public void setTeam(Team rmt){
		_team = rmt;
	}
	public void clearTeam(){
		_team = null;
	}
	
	//Team GET/SET
	public Team getTeam(){
		return _team;
	}
	
	//Game
	public List<Game> getGames(){
		List<Game> rmGames = new ArrayList<Game>();
		for(Game rmGame : Game.getGames().values()){
			if(rmGame.getGameConfig().getOwnerName().equalsIgnoreCase(getPlayer().getName())){
				rmGames.add(rmGame);
			}
		}
		return rmGames;
	}
	
	//SendMessage
	public boolean sendMessage(String message){
		if(isOnline()){
			getPlayer().sendMessage(message);
			return true;
		}
		return false;
	}
	
	public void warpToTeam(){
		if(!isOnline()){
			_warpTeamLocation = getTeam().getWarpLocation().clone();
		}
		else if(isIngame()){
			_warpTeamLocation = null;
			warpToLocation(getTeam().getWarpLocation().clone());
		}
		else if(_warpTeamLocation!=null){
			warpToLocation(_warpTeamLocation);
			_warpTeamLocation = null;
		}
	}
	
	public void warpToLocation(Location loc){
		if(loc==null) return;
		if(isOnline()){
			Player p = getPlayer();
			loc.setPitch(p.getLocation().getPitch());
			loc.setYaw(p.getLocation().getYaw());
			p.teleport(loc);
		}
	}
	
	public Location getWarpReturnLocation(){
		return _warpReturnLocation;
	}
	public void markWarpReturnLocation(){
		if(isOnline()){
			setWarpReturnLocation(getPlayer().getLocation());
		}
	}
	public void setWarpReturnLocation(Location warpReturnLocation){
		if(warpReturnLocation!=null){
			_warpReturnLocation = warpReturnLocation;
			sendMessage(Text.getLabel("return.mark"));
		}
	}
	public void clearWarpReturnLocation(){
		if(_warpReturnLocation==null) return;
		_warpReturnLocation = null;
		sendMessage(Text.getLabel("return.clear"));
	}
	
	public void warpToReturnLocation(){
		if(!isOnline()){
			_warpToReturnLocation = true;
			Debug.warning("NOT ONLINE - warpToReturn: "+_warpToReturnLocation);
		}
		else if(!isIngame()){
			Debug.warning("ONLINE - warpToReturn: "+_warpToReturnLocation);
			if(_warpReturnLocation==null) return;
			_warpToReturnLocation = false;
			warpToLocation(_warpReturnLocation);
			sendMessage(Text.getLabel("return.success"));
			clearWarpReturnLocation();
		}
	}
	
	public Location findSafeWarpLocation(Location loc){
		return loc;
	}

	public boolean isSneaking(){
		if(isOnline()) return getPlayer().isSneaking();
		return false;
	}
	
	public boolean isInTeam(){
		if(getTeam()!=null) return true;
		return false;
	}
	
	public boolean isIngame(){
		if(getGameInProgress()!=null) return true;
		return false;
	}
	
	public Game getGame(){
		Team rmTeam = getTeam();
		if(rmTeam!=null){
			Game rmGame = rmTeam.getGame();
			if(rmGame!=null){
				return rmGame;
			}
		}
		return null;
	}
	
	public Game getGameInProgress(){
		Game rmGame = getGame();
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
			refreshPlayAreaTimeLimit();
			GameConfig config = getGameInProgress().getGameConfig();
			Timer timer = config.getTimer();
			if(timer.getTimeLimit()!=0){
				if(timer.getTimeElapsed()>timer.getTimeLimit()) sendMessage(Text.getLabelColorize("game.sudden_death", ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE));
				else sendMessage(Text.getLabelArgs("time.remaining", timer.getTextTimeRemaining())); 
			}
			PvpTimer pvpTimer = config.getPvpTimer();
			if(pvpTimer.getTimeLimit()!=0){
				sendMessage(Text.getLabelArgs("game.pvp.delay", ""+pvpTimer.getTextTimeRemaining()));
			}
			if(_warpTeamLocation!=null) warpToTeam();
		}
		if(_updateInventory){
			_updateInventory = false;
			 if(_warpToReturnLocation) warpToReturnLocation();
			else if(_warpTeamLocation!=null) warpToTeam();
			updatePlayerInventory();
		}
		updateProperties();
	}
	
	public void onPlayerQuit(){
		updateProperties();
		_inventory.setOfflineInventory();
	}
	
	public void onPlayerJoinTeam(Team team){
		_matchStats.clearAll();
		setTeam(team);
		setReady(false);
		setChatMode(ChatMode.GAME);
		getPlayAreaTimer().reset();
		refreshPlayAreaTimeLimit();
		updateProperties();
		markWarpReturnLocation();
	}
	
	public void onPlayerQuitTeam(){
		//addStat(RMStat.TIMES_PLAYED);
		//addStat(RMStat.TIME_PLAYED, getGame().getGameConfig().getTimer().getTimeElapsed());
		clearWarpReturnLocation();
		_matchStats.clearAll();
		getPlayAreaTimer().reset();
		clearTeam();
		setReady(false);
		updateProperties();
	}
	
	public void refreshPlayAreaTimeLimit(){
		int timeLimit = getGame().getGameConfig().getSettingInt(Setting.playareatime);
		if(_playAreaTimer.getTimeLimit()!=timeLimit){
			_playAreaTimer.setTimeLimit(timeLimit);
		}
	}
	
	public void checkDetectedEnemy(int radius){
		Player p = getPlayer();
		if((!isOnline())||(!isIngame())||(p.isDead())) return;
		List<Player> players = getNearbyEnemies(radius);
		if(_detectedEnemies != players.size()){
			_detectedEnemies = players.size();
			if(_detectedEnemies==0) return;
			String str = _detectedEnemies==1?Text.getLabel("enemyradar.enemy"):Text.getLabel("enemyradar.enemies");
			sendMessage(Text.getLabelArgs("enemyradar.caution", ""+_detectedEnemies, str));
			p.playEffect(getPlayer().getLocation(), Effect.CLICK1, 0);
		}
	}
	
	public void clearDetectedEnemy(){
		_detectedEnemies = 0;
	}
	
	public List<Player> getNearbyEnemies(int radius){
		Player p = getPlayer();
		List<Player> result = new ArrayList<Player>();
		if(!isOnline()) return result;
		GamePlayer[] players = getGame().getOnlineTeamPlayers();
		if(players.length==0) return result;
		
		Location loc = p.getLocation();
		for(GamePlayer rmPlayer : players){
			Player player = rmPlayer.getPlayer();
			if((player==this)||player.isDead()||(rmPlayer.getTeam()==getTeam())) continue;
			Location playerLoc = player.getLocation();
			if(Math.abs(loc.getBlockX()-playerLoc.getBlockX())>radius) continue;
			if(Math.abs(loc.getBlockY()-playerLoc.getBlockY())>radius) continue;
			if(Math.abs(loc.getBlockZ()-playerLoc.getBlockZ())>radius) continue;
			result.add(player);
		}
		return result;
	}
	
	public void checkPlayArea(int radius){
		Player p = getPlayer();
		if((!isOnline())||(!isIngame())||(p.isDead())) return;
		if(!inPlayArea(radius)){
			if(_playAreaTimer.getTimeElapsed()<_playAreaTimer.getTimeLimit()){
				if(_playAreaTimer.getTimeElapsed()==0){
					sendMessage(Text.getLabel("game.playarea.danger"));
					sendMessage(Text.getLabelArgs("game.playarea.time", _playAreaTimer.getTextTimeRemaining()));
				}
				else sendMessage(""+ChatColor.RED+_playAreaTimer.getTimeRemaining());
				_playAreaTimer.addTimeElapsed();
			}
			else if(_playAreaTimer.getTimeElapsed()==_playAreaTimer.getTimeLimit()){
				getPlayer().damage(getPlayer().getHealth());
				_playAreaTimer.setTimeElapsed(0);
				sendMessage(Text.getLabel("game.playarea.death"));
			}
		}
		else if(_playAreaTimer.getTimeElapsed()!=0){
			_playAreaTimer.setTimeElapsed(0);
			sendMessage(Text.getLabel("game.playarea.safe"));
		}
	}
	
	public boolean inPlayArea(int radius){
		Game rmGame = getGame();
		if(rmGame!=null) if(getPlayer()!=null) return rmGame.inRangeXZ(getPlayer(), radius);
		return false;
	}
	
	public boolean inSafeZone(int radius){
		Game rmGame = getGame();
		if(rmGame!=null) if(getPlayer()!=null) return rmGame.inRangeXYZ(getPlayer(), radius);
		return false;
	}
	
	public boolean inRange(Location loc, int radius){
		if(!isOnline()) return false;
		Location ploc = getPlayer().getLocation();
		if(Math.abs(loc.getX()-ploc.getX())>radius) return false;
		if(Math.abs(loc.getY()-ploc.getY())>radius) return false;
		if(Math.abs(loc.getZ()-ploc.getZ())>radius) return false;
		return true;
	}
	
	public void checkInSafeZone(int radius){
		if((!_inSafeZone)&&(inSafeZone(radius))){
			sendMessage(Text.getLabel("game.safezone.enter"));
			_inSafeZone = true;
		}
		else if((_inSafeZone)&&(!inSafeZone(radius))){
			sendMessage(Text.getLabel("game.safezone.leave"));
			_inSafeZone = false;
		}
	}
	
	public boolean isSafe(){
		if(inSafeZone(getGame().getGameConfig().getSettingInt(Setting.safezone))) return true;
		return false;
	}
	
	public boolean isOnline(){
		Player p = getPlayer();
		if(p==null) return false;
		if(!p.getName().equalsIgnoreCase(getName())) return false;
		else return p.isOnline();
	}
	
	public boolean hasOpPermission(){
		return hasOpPermission("resourcemadness.admin");
	}
	
	public boolean hasOpPermission(String node){
		if(isOnline()){
			Player p = getPlayer();
			if(rm.isPermissionEnabled()){
				return (rm.hasPermission(p, node));
			}
			else if(p.isOp()) return true;
		}
		return false;
	}
	
	public boolean hasOwnerPermission(String ownerName){
		if(isOnline()){
			Player p = getPlayer();
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
		if(isOnline()) return (rm.hasPermission(getPlayer(), node));
		return false;
	}
	
	public void restoreHealth(){
		if(isOnline()){
			Player p = getPlayer();
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
		}
	}
	
	public void setPercentageDrops(PlayerDrops drops){
		_drops = drops;
	}
	
	public void givePercentageDrops(){
		if(_drops==null) return;
		Debug.warning("PercentageDrops1");
		if(!isOnline()) return;
		PlayerInventory inv = getPlayer().getInventory();
		Debug.warning("PercentageDrops2");
		
		for(ItemStack drop : _drops.getDrops()){
			if(drop==null) continue;
			if(drop.getType()==Material.AIR) continue;
			Debug.warning("drop: "+drop.getType().name());
		}
		
		for(ItemStack drop : _drops.getDropsArmor()){
			if(drop==null) continue;
			if(drop.getType()==Material.AIR) continue;
			Debug.warning("drop: "+drop.getType().name());
		}
		inv.setContents(_drops.getDrops().toArray(new ItemStack[_drops.getDrops().size()]));
		inv.setArmorContents(_drops.getDropsArmor().toArray(new ItemStack[_drops.getDropsArmor().size()]));
		_drops = null;
	}

	public GamePlayerInventory getInventory(){
		Player p = getPlayer();
		if(p!=null) updateInternalInventory();
		return _inventory;
	}
	
	public void updateInternalInventory(){
		Player p = getPlayer();
		if(p==null) return;
		_inventory.setInventory(p.getInventory());
	}
	
	public void updatePlayerInventory(){
		Player p = getPlayer();
		if(p==null) return;
		_inventory.updatePlayerInventory(p.getInventory());
	}
	
	public void updateProperties(){
		if(!isOnline()) return;
		Player p = getPlayer();
		//Update Inventory;
		/*
		List<ItemStack> contents = InventoryHelper.getItemsExcept(getInventory().getContents(), Material.AIR);
		_inventoryStack = contents.toArray(new ItemStack[contents.size()]);
		contents = InventoryHelper.getItemsExcept(getInventory().getArmorContents(), Material.AIR);
		_inventoryArmorStack = contents.toArray(new ItemStack[contents.size()]);
		*/
		_health = p.getHealth();
		_foodLevel = p.getFoodLevel();
		_location = p.getLocation();
	}

	public void clearProperties(){
		_health = 0;
		_foodLevel = 0;
		/*
		_inventoryStack = null;
		_inventoryArmorStack = null;
		*/
		_location = null;
	}
	
	public int getHealth(){
		if(isOnline()) return getPlayer().getHealth();
		return _health;
	}
	public void setHealth(int health){
		_health = health;
		if(_health<0) _health = 0;
		else if(_health>20) _health = 20;
	}
	
	public int getFoodLevel(){
		if(isOnline()) return getPlayer().getFoodLevel();
		else return _foodLevel;
	}
	
	public void setFoodLevel(int foodLevel){
		_foodLevel = foodLevel;
		if(_foodLevel<0) _foodLevel = 0;
		else if(_foodLevel>20) _foodLevel = 20;
	}
	
	public Location getLocation(){
		if(isOnline()) return getPlayer().getLocation();
		else return _location;
	}
	
	public void setLocation(Location location){
		_location = location;
	}
	
	public int getFoodPoints(){
		return getItemPoints(getInventory().getContents(), ItemType.FOOD);
	}
	
	public int getArmorPoints(){
		return getItemPoints(InventoryHelper.joinItemStack(getInventory().getContents(), getInventory().getArmorContents()), ItemType.ARMOR);
	}
	
	public int getArmorOnlyPoints(){
		return getItemPoints(getInventory().getArmorContents(), ItemType.ARMOR);
	}
	
	public int getWeaponPoints(){
		return getItemPoints(getInventory().getContents(), ItemType.WEAPON);
	}
	
	private int getItemPoints(ItemStack[] items, ItemType itemType){
		int result = 0;
		if(items!=null){
			for(ItemStack item : items){
				if(item==null) continue;
				ItemStat is = ItemStats.get(item.getType());
				if((is!=null)&&(is.getItemType()==itemType)){
					result += is.getPoints()*item.getAmount();
				}
			}
		}
		return result;
	}
	
	public int getToolDurability(){
		return getItemDurability(getInventory().getContents(), ItemType.TOOL);
	}
	
	public int getWeaponDurability(){
		return getItemDurability(getInventory().getContents(), ItemType.WEAPON);
	}
	
	private int getItemDurability(ItemStack[] items, ItemType itemType){
		int result = 0;
		if(items!=null){
			for(ItemStack item : items){
				if(item==null) continue;
				ItemStat is = ItemStats.get(item.getType());
				if((is!=null)&&(is.getItemType()==itemType)) result += is.getDurability()-item.getDurability();
			}
		}
		return result;
	}

	
	//Info
	public String getHealthInfo(){
		return getBar(getHealth(), 20, ChatColor.GREEN, ChatColor.DARK_GRAY);
	}
	
	public String getArmorInfo(){
		return getBar(getArmorOnlyPoints(), 20, ChatColor.GRAY, ChatColor.DARK_GRAY);
	}
	
	public String getFoodLevelInfo(){
		return getBar(getFoodLevel(), 20, ChatColor.GOLD, ChatColor.DARK_GRAY);
	}
	
	public String getBar(int current){
		return getBar(current, 0);
	}
	
	public String getBar(int current, ChatColor color1){
		return getBar(current, 0, color1, null);
	}
	
	public String getBar(int current, int max){
		return getBar(current, max, null, null);
	}
	
	public String getBar(int current, int max, ChatColor color1, ChatColor color2){
		String result = "";
		int health = current/2;
		result+=(color1!=null?color1:"")+TextHelper.genString(":", health);
		if(current%2==1){
			result += ".";
			health++;
		}
		if(max/2>=health) result+=(color2!=null?color2:"")+TextHelper.genString(".", max/2-health);
		return result;
	}
	
	public String getToolInfo(){
		return getItemInfo(ItemType.TOOL);
	}
	
	public String getWeaponInfo(){
		return getItemInfo(ItemType.WEAPON);
	}
	
	public String getItemInfo(ItemType type){
		String result = "";
		switch(type){
		case TOOL:
			result += getItemInfo(type, "P");
			result += getItemInfo(type, "S");
			result += getItemInfo(type, "A");
			result += getItemInfo(type, "H");
			result += getItemInfo(type, "R");
			result += getItemInfo(type, "W");
			result += getItemInfo(type, "F");
			result += getItemInfo(type, "C");
			/*
			result += getItemInfo(type, "-)");
			result += getItemInfo(type, "-D");
			result += getItemInfo(type, "-7");
			result += getItemInfo(type, "->");
			*/
			break;
		case WEAPON:
			result += getItemInfo(type, "S");
			result += getItemInfo(type, "B");
			result += getItemInfo(type, "A");
			result += getItemInfo(type, "T");
			/*
			result += getItemInfo(type, "+-");
			result += getItemInfo(type, "D-");
			result += getItemInfo(type, "*-");
			result += getItemInfo(type, "[]");
			*/
			break;
		case ARMOR:
			result += getItemInfo(type, "H");
			result += getItemInfo(type, "C");
			result += getItemInfo(type, "L");
			result += getItemInfo(type, "B");
			break;
		}
		if(result.length()==0) result = ChatColor.GRAY+"None";
		if(result.length()!=0) result = result.trim()+" ";
		return result;
	}
	
	public String getItemInfo(ItemType type, String name){
		String result = "";
		Material[] materials = ItemStats.getMaterials(type, name);
		for(Material mat : materials){
			if(mat==null) continue;
			ItemStack[] items = findItems(mat, getInventory().getContents());
			ItemStat is = ItemStats.get(mat);
			name = is.getName();
			if(result.indexOf(name)>0) name = "";
			name = is.getColor()+name;
			int amount = findItemAmount(items);
			if(amount==0) continue;
			//if(mat==Material.ARROW) result += name+ChatColor.GREEN+amount;
			//else result += name+(amount>1?getBar(amount-1):"");
			if(amount>10) result += name+amount;
			else result += name+getBar(amount);
		}
		if(result.length()!=0) result += " ";
		return result;
	}
	
	private ItemStack[] findItems(Material mat, ItemStack... items){
		List<ItemStack> result = new ArrayList<ItemStack>();
		if(items!=null){
			for(ItemStack item : items){
				if(item==null) continue;
				if(item.getType()==mat) result.add(item);
			}
		}
		return result.toArray(new ItemStack[result.size()]);
	}
	
	private int findItemAmount(ItemStack... items){
		int result = 0;
		if(items!=null){
			for(ItemStack item : items){
				if(item==null) continue;
				result+=item.getAmount();
			}
		}
		return result;
	}
	
	public PlayerDirection getPlayerDirection(GamePlayer rmp){
		Location loc = getLocation();
		if(this==rmp) return PlayerDirection.C;
		else if(!rmp.isOnline()) return PlayerDirection.NONE;
		Location rmploc = rmp.getLocation();
		double radians = Math.atan2(rmploc.getBlockZ()-loc.getBlockZ(), rmploc.getBlockX()-loc.getBlockX());
		double degrees = (float)Math.toDegrees(radians);
		degrees = (0-((loc.getYaw()-22.5-degrees)+90));
		Debug.warning("degrees1: "+degrees);
		while(degrees>=360) degrees = degrees-360;
		while(degrees<0) degrees = degrees+360;
		int angle = (int)(degrees/45);
		Debug.warning("degrees2: "+degrees);
		
		PlayerDirection result;
		switch(angle){
		case 0: result = PlayerDirection.N; break;
		case 1: result = PlayerDirection.NE; break;
		case 2: result = PlayerDirection.E; break;
		case 3: result = PlayerDirection.SE; break;
		case 4: result = PlayerDirection.S; break;
		case 5: result = PlayerDirection.SW; break;
		case 6: result = PlayerDirection.W; break;
		case 7: result = PlayerDirection.NW; break;
		default: result = PlayerDirection.NONE; break;
		}
		
		if(rmploc.getBlockY()>loc.getBlockY()) result.setAltitude(true);
		else if(rmploc.getBlockY()<loc.getBlockY()) result.setAltitude(false);
		else result.setAltitude(null);
		return result;
	}
	
	public String getLocationInfo(GamePlayer player){
		Location loc = getLocation();
		Location pLoc = player.getLocation();
		if((loc==null)||(pLoc==null)) return "";
		
		ChatColor mainColor = ChatColor.GRAY;
		int xDist = Math.abs(loc.getBlockX() - pLoc.getBlockX());
		int zDist = Math.abs(loc.getBlockZ() - pLoc.getBlockZ());
		int distance = (int)Math.sqrt(xDist*xDist+zDist*zDist);
		String altitude = String.valueOf(pLoc.getBlockY()-loc.getBlockY());
		if(altitude.charAt(0)=='-') altitude = altitude.replace("-", ChatColor.RED+"-"+mainColor);
		else altitude = ""+ChatColor.RED+"+"+mainColor+altitude;
		
		String result = "";
		if(player!=this) result += ChatColor.RED+"d"+mainColor+distance+" "+altitude+" ";
		result += ChatColor.RED+"x"+mainColor+pLoc.getBlockX()+" ";
		result += ChatColor.RED+"y"+mainColor+pLoc.getBlockY()+" ";
		result += ChatColor.RED+"z"+mainColor+pLoc.getBlockZ()+" ";
		return result.trim();
	}
	
	//Team Info
	public void getTeamInfo(String arg){
		if(!isIngame()) return;
		GamePlayer[] teamPlayers = getTeam().getPlayers();
		List<GamePlayer> foundPlayers = new ArrayList<GamePlayer>();
		int id = Helper.getIntByString(arg);
		if(id!=-1) getTeamInfo(id, teamPlayers);
		else{
			for(GamePlayer teamPlayer : teamPlayers){
				if(teamPlayer.getName().equalsIgnoreCase(arg)){
					getTeamInfo(teamPlayer);
					return;
				}
				else if(teamPlayer.getName().toLowerCase().startsWith(arg.toLowerCase())){
					foundPlayers.add(teamPlayer);
				}
			}
			if(foundPlayers.size()==1){
				getTeamInfo(foundPlayers.get(0));
			}
			else if(foundPlayers.size()>1){
				getTeamInfo(-1, foundPlayers.toArray(new GamePlayer[foundPlayers.size()]));
			}
			else sendMessage(Text.getLabelArgs("team.player_not_found", arg));
		}
	}
	
	public void getTeamInfo(GamePlayer player){
		if(!isIngame()) return;
		sendMessage(Text.getLabel("team"));
		//sendMessage("Team "+player.getTeam().getTeamColorString());
		getPlayerAdvInfo(player, -1);
		getPlayerMatchStatsInfo(player);
	}
	
	public void getTeamInfo(int page, GamePlayer[] players){
		if(!isIngame()) return;
		ItemStats.reset();
		Team team = getTeam();
		int listLimit = 4;
		int id = page;
		if(id<1) id=1;
		int size = (int)Math.ceil((double)players.length/(double)listLimit);
		if(id>size) id=1;
		if(page!=-1) sendMessage(Text.getLabelArgs("team.page", ""+id, ""+size));
		else sendMessage(Text.getLabel("team"));
		//sendMessage(Text.getLabelArgs("team.found_match", ""+players.length));
		sendMessage("Team "+team.getTeamColorString());
		//sendMessage("Players: "+ChatColor.GREEN+playerMap.size());//+ChatColor.WHITE+"/"+getGame().getText(this, Setting.minteamplayers)+ChatColor.WHITE+"-"+getGame().getText(this, Setting.maxteamplayers));
		id=(id-1)*listLimit;
		int i = 0;
		while((i<listLimit)&&(id<players.length)){
			GamePlayer player = players[id];
			getPlayerAdvInfo(player, id);
			id++;
			i++;
		}
	}
	
	//Player Info
	public void getPlayerInfo(String arg){
		GamePlayer[] players = GamePlayer.getPlayersArray();
		List<GamePlayer> foundPlayers = new ArrayList<GamePlayer>();
		int id = Helper.getIntByString(arg);
		if(id!=-1) getPlayerInfo(id, players);
		else{
			for(GamePlayer player : players){
				if(player.getName().equalsIgnoreCase(arg)){
					getPlayerInfo(player);
					return;
				}
				else if(player.getName().toLowerCase().startsWith(arg.toLowerCase())){
					foundPlayers.add(player);
				}
			}
			if(foundPlayers.size()==1){
				getPlayerInfo(foundPlayers.get(0));
			}
			else if(foundPlayers.size()>1){
				getPlayerInfo(-1, foundPlayers.toArray(new GamePlayer[foundPlayers.size()]));
			}
			else sendMessage(Text.getLabelArgs("stats.player_not_found", arg));
		}
	}
	
	public void getPlayerInfo(GamePlayer player){
		sendMessage(Text.getLabel("stats"));
		if(player.getTeam()!=null){
			sendMessage("Team "+player.getTeam().getTeamColorString());
			if(getTeam()==player.getTeam()){
				getPlayerAdvInfo(player, -1);
				getPlayerMatchStatsInfo(player);
			}
		}
		else getPlayerSimpleInfo(player, -1);
		getPlayerStatsInfo(player);
	}
	
	public void getPlayerInfo(int page, GamePlayer[] players){
		//ItemStats.reset();
		int listLimit = 4;
		int id = page;
		if(id<1) id=1;
		int size = (int)Math.ceil((double)players.length/(double)listLimit);
		if(id>size) id=1;
		if(page!=-1) sendMessage(Text.getLabelArgs("stats.page", ""+id, ""+size));
		else sendMessage(Text.getLabel("stats"));
		//sendMessage(Text.getLabelArgs("stats.found_match", ""+players.length));
		//sendMessage("Players: "+ChatColor.GREEN+playerMap.size());//+ChatColor.WHITE+"/"+getGame().getText(this, Setting.minteamplayers)+ChatColor.WHITE+"-"+getGame().getText(this, Setting.maxteamplayers));
		id=(id-1)*listLimit;
		int i = 0;
		while((i<listLimit)&&(id<players.length)){
			GamePlayer player = players[id];
			getPlayerSimpleInfo(player, id);
			getPlayerStatsInfo(player);
			id++;
			i++;
		}
	}
	
	//Info
	public void getPlayerSimpleInfo(GamePlayer player, int id){
		boolean isOnline = player.isOnline();
		String color1 = Text.getLabel("simple.color.online");
		if(isOnline) player.updateProperties();
		else color1 = Text.getLabel("simple.color.offline");
		String message = "";
		if(id>=0) message += ""+ChatColor.YELLOW+id+" ";
		if(this==player) message += ChatColor.AQUA+player.getName()+" ";
		else message += (isOnline?ChatColor.GREEN:ChatColor.GRAY)+player.getName()+" ";
		message += color1+"Status: "+(player.isOnline()?ChatColor.GREEN+"Online":ChatColor.GRAY+"Offline")+" ";
		sendMessage(message);
	}
	
	public void getPlayerAdvInfo(GamePlayer player, int id){
		if(!isIngame()) return;
		PlayerDirection teamPlayerDirection = getPlayerDirection(player);
		boolean isOnline = player.isOnline();
		
		String color1 = Text.getLabel("team.color1.online");
		String color2 = Text.getLabel("team.color2.online");
		if(isOnline) player.updateProperties();
		else{
			color1 = Text.getLabel("team.color1.offline");
			color2 = Text.getLabel("team.color2.offline");
		}
		
		
		String message = "";
		if(id>=0) message += ""+ChatColor.YELLOW+id+" ";
		if(this==player) message += ChatColor.AQUA+player.getName()+" ";
		else message += (isOnline?ChatColor.GREEN:ChatColor.GRAY)+player.getName()+" ";
		message += color1+"Status: "+(player.isOnline()?ChatColor.GREEN+"Online":ChatColor.GRAY+"Offline")+" ";
		String message2 = ChatColor.GRAY+getLocationInfo(player);
		message2 = isOnline?message2:color1+ChatColor.stripColor(message2);
		sendMessage(message+message2);
		
		message = teamPlayerDirection.getLine1()+" ";
		message += color1+"Health: "+player.getHealthInfo()+" ";
		message += color1+"Armor: "+player.getArmorInfo()+" ";
		message += color1+"Satiety: "+player.getFoodLevelInfo()+" ";
		message += color1+"Food: "+color2+player.getFoodPoints()+" ";
		sendMessage(isOnline?message:color1+ChatColor.stripColor(message));
		
		message = teamPlayerDirection.getLine2()+" ";
		message += color1+"Weapons: "+player.getWeaponInfo();
		message += color1+"Armor: "+player.getItemInfo(ItemType.ARMOR);
		sendMessage(isOnline?message:color1+ChatColor.stripColor(message));
		
		message = teamPlayerDirection.getLine3()+" ";
		message += color1+"Tools: "+player.getToolInfo();
		message += color1+"Uses: "+color2+"W"+player.getWeaponDurability()+" ";
		message += color2+"T"+player.getToolDurability();
		sendMessage(isOnline?message:color1+ChatColor.stripColor(message));
	}
	
	public void getPlayerMatchStatsInfo(GamePlayer player){
		if(!isIngame()) return;
		boolean isOnline = player.isOnline();
		String color1 = Text.getLabel("matchstats.color1.online");
		String color2 = Text.getLabel("matchstats.color2.online");
		if(isOnline) player.updateProperties();
		else{
			color1 = Text.getLabel("matchstats.color1.offline");
			color2 = Text.getLabel("matchstats.color2.offline");
		}

		Stats stats = player.getMatchStats();
		
		stats.set(RMStat.TIME_PLAYED, getGameInProgress().getGameConfig().getTimer().getTimeElapsed());
		
		String message = "";
		message = color1+"Items Found: "+color2+stats.get(RMStat.ITEMS_FOUND_TOTAL)+" ";
		message += color1+"Kills: "+color2+stats.get(RMStat.KILLS)+" ";
		message += color1+"Deaths: "+color2+stats.get(RMStat.DEATHS)+" ";
		message += color1+"K/D: "+color2+stats.getRatioString(RMStat.KILLS, RMStat.DEATHS)+" ";
		//sendMessage(isOnline?message:color1+ChatColor.stripColor(message));
		sendMessage(message);
		
		message = color1+"Time played: "+color2+Timer.getTextTimeStatic(stats.get(RMStat.TIME_PLAYED))+" ";
		//sendMessage(isOnline?message:color1+ChatColor.stripColor(message));
		sendMessage(message);
	}
	
	public void getPlayerStatsInfo(GamePlayer player){
		boolean isOnline = player.isOnline();
		String color1 = Text.getLabel("stats.color1.online");
		String color2 = Text.getLabel("stats.color2.online");
		if(isOnline) player.updateProperties();
		else{
			color1 = Text.getLabel("stats.color1.offline");
			color2 = Text.getLabel("stats.color2.offline");
		}
			
		Stats stats = player.getMatchStats();
		String message = "";
		
		//sendMessage("Stats:");
		stats = player.getStats();
		message = color1+"Items Found: "+color2+stats.get(RMStat.ITEMS_FOUND_TOTAL)+" ";
		message += color1+"Kills: "+color2+stats.get(RMStat.KILLS)+" ";
		message += color1+"Deaths: "+color2+stats.get(RMStat.DEATHS)+" ";
		message += color1+"K/D: "+color2+stats.getRatioString(RMStat.KILLS, RMStat.DEATHS)+" ";
		//sendMessage(isOnline?message:color1+ChatColor.stripColor(message));
		sendMessage(message);
		
		message = color1+"Wins: "+color2+stats.get(RMStat.WINS)+" ";
		message += color1+"Losses: "+color2+stats.get(RMStat.LOSSES)+" ";
		message += color1+"Times played: "+color2+(stats.get(RMStat.WINS)+stats.get(RMStat.LOSSES))+" ";
		//sendMessage(isOnline?message:color1+ChatColor.stripColor(message));
		sendMessage(message);
		
		message = color1+"Time played: "+color2+Timer.getTextTimeStatic(stats.get(RMStat.TIME_PLAYED))+" ";
		//sendMessage(isOnline?message:mainColor+ChatColor.stripColor(message));
		sendMessage(message);
	}
}