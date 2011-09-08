package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.DyeColor;
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

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMConfig.Lock;
import com.mtaye.ResourceMadness.Helper.RMHelper;
import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMGame {

	private static HashMap<Integer, RMGame> _games = new HashMap<Integer, RMGame>();
	public static RM plugin;
	public static Material[] _materials = {Material.GLASS, Material.STONE, Material.CHEST, Material.WALL_SIGN, Material.WOOL};
	public static Material[] _hackMaterials = {
		Material.AIR, Material.GRASS, Material.BEDROCK, Material.WATER,	Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA,
		Material.COAL_ORE, Material.SPONGE, Material.LAPIS_ORE, Material.BED_BLOCK, Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.DOUBLE_STEP,
		Material.FIRE, Material.MOB_SPAWNER, Material.REDSTONE_WIRE, Material.DIAMOND_ORE, Material.CROPS, Material.SOIL, Material.BURNING_FURNACE,
		Material.SIGN_POST, Material.WOODEN_DOOR, Material.WALL_SIGN, Material.IRON_DOOR_BLOCK, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
		Material.REDSTONE_TORCH_OFF, Material.SNOW_BLOCK, Material.ICE, Material.SUGAR_CANE_BLOCK, Material.PORTAL, Material.CAKE_BLOCK,
		Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.LOCKED_CHEST, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
		Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS};
	
	private List<Material> _lastHackMaterials = new ArrayList<Material>();
	
	private RMGameConfig _config = new RMGameConfig(plugin);
	
	//private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	//private Inventory _inventory;
	
	private static RMPlayer _requestPlayer;
	
	public static enum Part { GLASS, STONE, CHEST, WALL_SIGN, WOOL; }
	public static enum GameState { SETUP, COUNTDOWN, GAMEPLAY, SUDDEN_DEATH, GAMEOVER, PAUSED; }
	public static enum InterfaceState { FILTER, REWARD, TOOLS, FILTER_CLEAR, REWARD_CLEAR, TOOLS_CLEAR };
	public static enum FilterType { ALL, CLEAR, BLOCK, ITEM, RAW, CRAFTED};
	public static enum ClickState { LEFT, RIGHT, NONE };
	public static enum ItemHandleState { ADD, MODIFY, REMOVE, NONE };
	public static enum HandleState { ADD, MODIFY, REMOVE, NO_CHANGE, CLAIM_RETURNED_ALL, CLAIM_RETURNED_SOME, NONE };
	public static enum ForceState { SET, ADD, SUBTRACT, CLEAR, RANDOMIZE, NONE};
	public static enum FilterState { FILTER, FOUND, REWARD, TOOLS, ITEMS, NONE };
	public static enum MinMaxType { MIN_PLAYERS, MAX_PLAYERS, MIN_TEAM_PLAYERS, MAX_TEAM_PLAYERS };
	
	private final int cdTimerLimit = 30; //3 seconds
	private int cdTimer = cdTimerLimit;
	
	private RMTeam _winningTeam;
	private RMPlayer _winningPlayer;
	
	//Constructor
	public RMGame(RMPartList partList, RMPlayer rmp, RM plugin){
		RMGame.plugin = plugin;
		_config.setPartList(partList);
		_config.setPlayers(RMPlayer.getPlayers());
		_config.setId(getFreeId());
		_config.setOwnerName(rmp.getName());
	}
	public RMGame(RMGameConfig config, RM plugin){
		RMGame.plugin = plugin;
		_config = config;
		_config.setId(getFreeId());
	}
	
	//Config
	public RMGameConfig getConfig(){
		return _config;
	}
	public void setConfig(RMGameConfig config){
		_config = config;
	}
	
	//Winning Team
	public RMTeam getWinningTeam(){
		return _winningTeam;
	}
	public void setWinningTeam(RMTeam rmTeam){
		_winningTeam = rmTeam;
	}
	public void clearWinningTeam(){
		_winningTeam = null;
	}
	
	//Winning Player
	public RMPlayer getWinningPlayer(){
		return _winningPlayer;
	}
	public void setWinningPlayer(RMPlayer rmp){
		_winningPlayer = rmp;
	}
	public void clearWinningPlayer(){
		_winningTeam = null;
	}
	
	//Pause
	public void pauseGame(RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		switch(_config.getState()){
			case PAUSED: broadcastMessage(ChatColor.GRAY+"Game is already paused."); break;
			case GAMEPLAY:
				_config.setState(GameState.PAUSED);
				broadcastMessage(ChatColor.RED+"Game was paused.");
				break;
			default: rmp.sendMessage("You must be in-game to "+ChatColor.RED+"pause "+ChatColor.WHITE+"the game");
		}
		updateSigns();
	}
	
	public void resumeGame(RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		switch(_config.getState()){
			case PAUSED:
				_config.setState(GameState.GAMEPLAY);
				broadcastMessage(ChatColor.GREEN+"Game was resumed.");
				break;
			case GAMEPLAY: broadcastMessage(ChatColor.GRAY+"Game is not paused."); break;
			default: rmp.sendMessage("You must be in-game to "+ChatColor.GREEN+"resume "+ChatColor.WHITE+"the game");
		}
		updateSigns();
	}
	
	//Mode
	public void cycleMode(RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		switch(_config.getInterface()){
			case FILTER: case FILTER_CLEAR:
				if(!changeMode(InterfaceState.REWARD, rmp)) if(!changeMode(InterfaceState.TOOLS, rmp)) changeMode(InterfaceState.FILTER, rmp);
				break;
			case REWARD:
				if(!changeMode(InterfaceState.TOOLS, rmp)) if(!changeMode(InterfaceState.FILTER, rmp)) changeMode(InterfaceState.REWARD, rmp);
				break;
			case TOOLS:
				if(!changeMode(InterfaceState.FILTER, rmp)) if(!changeMode(InterfaceState.REWARD, rmp)) changeMode(InterfaceState.TOOLS, rmp);
				break;
		}
		updateSigns();
	}
	
	public boolean changeMode(InterfaceState interfaceState, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.mode")){
			rmp.sendMessage(RMText.noPermissionAction);
			return false;
		}
		if(_config.getState()!=GameState.SETUP) return false;
		switch(interfaceState){
			case FILTER:
				if(!rmp.hasPermission("resourcemadness.mode.filter")){
				rmp.sendMessage(RMText.noPermissionAction);
				return false;
			}
			case REWARD:
				if(!rmp.hasPermission("resourcemadness.mode.reward")){
				rmp.sendMessage(RMText.noPermissionAction);
				return false;
			}
			case TOOLS:
				if(!rmp.hasPermission("resourcemadness.mode.tools")){
				rmp.sendMessage(RMText.noPermissionAction);
				return false;
			}
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setInterface(interfaceState);
			rmp.sendMessage("Interface mode changed to "+ChatColor.YELLOW+interfaceState.name());
			updateSigns();
			return true;
		}
		return false;
	}	

	//Filter
	public void clearFilter(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.filter.clear")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		_config.getFilter().clearItems();
		rmp.sendMessage(ChatColor.GRAY+"Filter cleared.");
	}
	
	//Stash
	public void clearReward(Block b, RMPlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.reward.clear")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(!_config.getInfiniteReward()){
			if(rmp.claimStashToChest(_config.getReward(), b, ClaimType.REWARD, true, items).size()==0){
				rmp.sendMessage(ChatColor.GRAY+"Reward cleared.");
			}
		}
		else _config.getReward().clear();
	}
	public void clearTools(Block b, RMPlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.tools.clear")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(!_config.getInfiniteTools()){
			if(rmp.claimStashToChest(_config.getTools(), b, ClaimType.TOOLS, true, items).size()==0){
				rmp.sendMessage(ChatColor.GRAY+"Tools cleared.");
			}
		}
		else _config.getTools().clear();
	}
	public void clearFound(RMPlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.found.clear")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(rmp.claim(_config.getFound(), ClaimType.FOUND, items).size()==0){
			rmp.sendMessage(ChatColor.GRAY+"Found items cleared.");
		}
	}

	
	public void addRewardByChest(RMPlayer rmp, RMChest rmChest, ClickState clickState){
		if(!rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		addChestToStash(rmp, rmChest, _config.getReward(), ClaimType.REWARD, clickState);
	}
	public void addToolsByChest(RMPlayer rmp, RMChest rmChest, ClickState clickState){
		if(!rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		addChestToStash(rmp, rmChest, _config.getTools(), ClaimType.TOOLS, clickState);
	}
	
	public void addChestToStash(RMPlayer rmp, RMChest rmChest, RMStash rmStash, ClaimType claimType, ClickState clickState){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())) return;
		if(_config.getState()!=GameState.SETUP) return;
		List<ItemStack> items = rmChest.getContents();
		List<Material> hacked = new ArrayList<Material>();
		hacked = findHackMaterialsByItems(items);
		
		//Hacked
		if(_config.getWarnHackedItems()) warnHackMaterials(hacked);
		if(!_config.getAllowHackedItems()){
			items = removeHackMaterialsByItems(items);
			switch(claimType){
				case REWARD: if(!_config.getInfiniteReward()) rmChest.clearContentsExceptHacked(); break;
				case TOOLS: if(!_config.getInfiniteTools()) rmChest.clearContentsExceptHacked(); break;
			}
		}
		else switch(claimType){
			case REWARD: if(!_config.getInfiniteReward()) rmChest.clearContents(); break;
			case TOOLS: if(!_config.getInfiniteTools()) rmChest.clearContents(); break;
		}
		
		if(items.size()!=0){
			switch(claimType){
				case REWARD:
					if(!_config.getInfiniteReward()) rmStash.addItems(items);
					else{
						switch(clickState){
						case NONE: rmStash.setItems(items); break;
						case LEFT: rmStash.addItems(items); break;
						case RIGHT: rmStash.removeItems(items); break;
						}
					}
					rmStash.showChanged(rmp);
					break;
				case TOOLS:
					if(!_config.getInfiniteTools()) rmStash.addItems(items);
					else{
						switch(clickState){
						case NONE: rmStash.setItems(items); break;
						case LEFT: rmStash.addItems(items); break;
						case RIGHT: rmStash.removeItems(items); break;
						}
					}
					rmStash.showChanged(rmp);
				break;
			}
		}
		else if(hacked.size()==0){
			switch(claimType){
			case REWARD:
				if(rmp.getPlayer().isSneaking()){
					if(rmp.hasPermission("resourcemadness.reward.byhand")){
						addItemToStash(rmp, rmStash, claimType, clickState);
					}
					else rmp.sendMessage(RMText.noPermissionAction);
				}
				else{
					if(_config.getReward().size()!=0){
						if(rmp.hasPermission("resourcemadness.reward.clear")){
							rmp.sendMessage("Click sign to "+ChatColor.GRAY+"clear all rewards"+ChatColor.WHITE+".");
							_config.setInterface(InterfaceState.REWARD_CLEAR);
						}
						else rmp.sendMessage(RMText.noPermissionAction);
					}
				}
				break;
			case TOOLS:
				if(rmp.getPlayer().isSneaking()){
					if(rmp.hasPermission("resourcemadness.tools.byhand")){
						addItemToStash(rmp, rmStash, claimType, clickState);
					}
					else rmp.sendMessage(RMText.noPermissionAction);
				}
				else{
					if(_config.getTools().size()!=0){
						if(rmp.hasPermission("resourcemadness.tools.clear")){
							rmp.sendMessage("Click sign to "+ChatColor.GRAY+"clear all tools"+ChatColor.WHITE+".");
							_config.setInterface(InterfaceState.TOOLS_CLEAR);
						}
						else rmp.sendMessage(RMText.noPermissionAction);
					}
				}
				break;
			}
		}
		else rmChest.clearContentsExceptHacked();
		updateSigns();
	}
	
	public void addItemToStash(RMPlayer rmp, RMStash rmStash, ClaimType claimType, ClickState clickState){
		if(_config.getState()!=GameState.SETUP) return;
		if(!rmp.hasOwnerPermission(_config.getOwnerName())) return;
		ItemStack item = rmp.getPlayer().getItemInHand();
		List<ItemStack> items = new ArrayList<ItemStack>();
		items.add(item);
		
		if(_config.getWarnHackedItems()) warnHackMaterialsByItems(items); 
		if(!_config.getAllowHackedItems()) items = removeHackMaterialsByItems(items);
		
		ItemStack itemClone = item.clone();
		itemClone.setAmount(1);
		if(items.size()!=0){
			switch(clickState){
				case NONE: case LEFT:
					switch(claimType){
					case REWARD:
						rmStash.addItem(itemClone);
						if(!_config.getInfiniteReward()){
							item.setAmount(item.getAmount()-itemClone.getAmount());
							if(item.getAmount()==0) rmp.getPlayer().setItemInHand(null);
						}
						break;
					case TOOLS:
						rmStash.addItem(itemClone);
						if(!_config.getInfiniteTools()){
							item.setAmount(item.getAmount()-itemClone.getAmount());
							if(item.getAmount()==0) rmp.getPlayer().setItemInHand(null);
						}
						break;
					}
					break;
				case RIGHT:
					switch(claimType){
					case REWARD:
						if(_config.getInfiniteReward()) rmStash.removeByItem(itemClone);
						else rmp.claim(rmStash, ClaimType.NONE, itemClone);
						break;
					case TOOLS:
						if(_config.getInfiniteTools()) rmStash.removeByItem(itemClone);
						else rmp.claim(rmStash, ClaimType.NONE, itemClone);
						break;
					}
					break;
			}
			rmStash.showChanged(rmp);
		}
	}
	
	/*
	public HandleState addItem(ItemStack item, ClaimType claimType){
		if((item!=null)&&(item.getType()!=Material.AIR)){
			RMStash items = new RMStash();
			switch(claimType){
				case FOUND: items = _config.getFound(); break;
				case REWARD: items = _config.getReward(); break;
				case TOOLS: items = _config.getTools(); break;
			}
			return items.addItem(item);
		}
		return HandleState.NO_CHANGE;
	}
	
	public HandleState addItem(ItemStack item, List<ItemStack> items){
		for(ItemStack isItem : items){
			if(isItem.getType() == item.getType()){
				isItem.setAmount(isItem.getAmount()+item.getAmount());
				return HandleState.MODIFY;
			}
		}
		items.add(item);
		return HandleState.ADD;
	}
	*/
	
	public HandleState removeItem(ItemStack item, ClaimType claimType){
		if((item!=null)&&(item.getType()!=Material.AIR)){
			RMStash items = new RMStash();
			switch(claimType){
				case REWARD: items = _config.getReward(); break;
				case TOOLS: items = _config.getTools(); break;
			}
			/*
			for(ItemStack isItem : items){
				if(isItem.getType() == item.getType()){
					if(item.getAmount()>isItem.getAmount()){
						return HandleState.REMOVE;
					}
					else return HandleState.MODIFY;
				}
			}
			*/
		}
		return HandleState.NO_CHANGE;
	}
	
	//Claim
	public void claimFound(RMPlayer rmp, ItemStack... items){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		rmp.claim(_config.getFound(), ClaimType.FOUND, items);
	}
	
	public void claimFoundToChest(Block b, RMPlayer rmp, ItemStack... items){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if(_config.getState()!=GameState.SETUP){
			rmp.sendMessage("You can't claim the game's "+ChatColor.YELLOW+"found items "+ChatColor.WHITE+"while you're in-game.");
			return;
		}
		rmp.claimToChest(b, ClaimType.FOUND, false, items);
	}
	
	//Check Equal Distribution
	public List<String> checkRewardEqual(RMPlayer rmp){
		return checkRewardEqualDistribution(rmp, _config.getReward());
	}
	public List<String> checkToolsEqual(RMPlayer rmp, List<RMPlayer> rmPlayers){
		return checkEqualDistribution(rmp, rmPlayers, _config.getTools());
	}
	
	public List<String> checkRewardEqualDistribution(RMPlayer rmp, RMStash rmStash){
		List<String> strUnequal = new ArrayList<String>();
		if(rmStash.size()>0){
			for(RMTeam rmTeam : _config.getTeams()){
				if(rmTeam!=null){
					int numOfPlayers = rmTeam.getPlayers().length;
					if(numOfPlayers>0){
						String strItems = "";
						for(RMStashItem rmStashItem : rmStash.values()){
							int amount = rmStashItem.getAmount();
							if(amount%numOfPlayers!=0){
								strItems+=Material.getMaterial(rmStashItem.getId()).name()+":"+ChatColor.GRAY+amount+ChatColor.WHITE+", ";
							}
						}
						if(strItems.length()>0){
							strItems = RMText.stripLast(strItems, ", ");
							strUnequal.add(rmTeam.getTeamColorString()+":["+ChatColor.WHITE+strItems+RMHelper.getChatColorByDye(rmTeam.getTeamColor())+"]");
						}
					}
				}
			}
		}
		return strUnequal;
	}
	
	public List<String> checkEqualDistribution(RMPlayer rmp, List<RMPlayer> rmPlayers, RMStash rmStash){
		List<String> strUnequal = new ArrayList<String>();
		if(rmStash.size()>0){
			int numOfPlayers = rmPlayers.size();
			if(numOfPlayers>0){
				String strItems = "";
				for(RMStashItem rmStashItem : rmStash.values()){
					int amount = rmStashItem.getAmount();
					if(amount%numOfPlayers!=0){
						strItems+=Material.getMaterial(rmStashItem.getId()).name()+":"+ChatColor.GRAY+amount+ChatColor.WHITE+", ";
					}
				}
				if(strItems.length()>0){
					strItems = RMText.stripLast(strItems, ", ");
					strUnequal.add("["+ChatColor.WHITE+strItems+"]");
				}
			}
		}
		return strUnequal;
	}
	
	//Distribution
	public void distributeReward(RMTeam rmTeam){
		distributeStash(rmTeam, ClaimType.REWARD);
	}
	public void distributeTools(RMTeam rmTeam){
		distributeStash(rmTeam, ClaimType.TOOLS);
	}
	
	public void distributeStash(RMTeam rmTeam, ClaimType claimType){
		RMStash stash = new RMStash();
		switch(claimType){
			case REWARD:
				if(_config.getInfiniteReward()) stash = _config.getReward().clone();
				else stash = _config.getReward();
				break;
			case TOOLS:
				if(_config.getInfiniteTools()) stash = _config.getTools().clone();
				else stash = _config.getTools();
				break;
		}
		if(stash.size()==0) return;
		if(rmTeam!=null) distributeStashToTeamDivide(rmTeam, stash, claimType);
		else distributeStashToTeamsDivide(stash,claimType);
	}
	
	public void distributeStashToTeamDivide(RMTeam rmTeam, RMStash stash, ClaimType claimType){
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	public void distributeStashToTeamsDivide(RMStash stash, ClaimType claimType){
		RMPlayer[] players = getTeamPlayers();
		int divisor = players.length;
		for(RMPlayer rmp : players){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	
	public void distributeFromChests(RMTeam rmTeam, ClaimType claimType){
		if(rmTeam==null) return;
		RMStash stash = new RMStash();
		for(RMChest rmChest : getChests()){
			stash.addItems(rmChest.getContents());
			rmChest.clearContents();
		}
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	public void distributeFromChest(RMChest rmChest, ClaimType claimType){
		RMTeam rmTeam = rmChest.getTeam();
		if(rmTeam==null) return;
		RMStash stash = new RMStash(rmChest.getContents());
		rmChest.clearContents();
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	
	public List<ItemStack> distributeByDivisor(RMStash stash, int divisor){
		List<ItemStack> foundItems = new ArrayList<ItemStack>();
		RMStashItem[] stashItems = stash.values().toArray(new RMStashItem[stash.values().size()]).clone();
		
		for(RMStashItem item : stashItems){
			int amount = (int)Math.ceil((double)item.getAmount()/(double)divisor);
			foundItems.addAll(stash.removeByIdAmount(item.getId(), amount));
		}
		return foundItems;
	}
	
	public void stashChestsContents(){
		for(RMChest rmChest : getChests()){
			rmChest.addInventoryToStash();
		}
	}
	public void returnChestsContents(){
		for(RMChest rmChest : getChests()){
			rmChest.returnInventoryFromStash();
		}
	}
	
	public void snatchInventories(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				if(rmp.getPlayer()!=null){
					rmp.addItemsFromInventory(rmp.getItems());
				}
			}
		}
	}
	
	public void returnInventories(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				if(rmp.getPlayer()!=null){
					rmp.claimItems();
				}
			}
		}
	}
	
	public void healPlayer(){
		for(RMPlayer rmp : getTeamPlayers()){
			rmp.restoreHealth();
		}
	}
	
	public void warpPlayersToSafety(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				rmp.warpToSafety();
			}
		}
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
		List<RMTeam> leading = new ArrayList<RMTeam>();
		List<RMTeam> added = new ArrayList<RMTeam>();
		RMTeam leadingTeam = null;
		for(RMTeam team : rmTeams){
			if(leading.size()==0) leading.add(team);
			else{
				Iterator<RMTeam> iter = leading.iterator();
				while(iter.hasNext()){
					RMTeam found = iter.next();
					int teamTotalLeft = team.getChest().getTotalLeft();
					int foundTotalLeft = found.getChest().getTotalLeft();
					if(teamTotalLeft<foundTotalLeft) iter.remove();
					if(teamTotalLeft<=foundTotalLeft) added.add(team);
				}
				leading.addAll(added);
			}
		}
		if(leading.size()==1) leadingTeam = leading.get(0);
		return leadingTeam;
	}
	
	public boolean hasMinimumPlayers(boolean broadcast){
		int minPlayers = _config.getMinPlayers();
		if(minPlayers<_config.getTeams().size()) minPlayers = _config.getTeams().size(); 
		if(getTeamPlayers().length<minPlayers){
			if(broadcast) broadcastMessage("This match is set up for at least "+ChatColor.YELLOW+_config.getMinPlayers()+ChatColor.WHITE+" player(s).");
			return false;
		}
		return true;
	}
	
	public boolean hasMinimumTeamPlayers(boolean broadcast){
		for(RMTeam rmTeam : _config.getTeams()){
			if(rmTeam.getPlayers().length<_config.getMinTeamPlayers()){
				//rmp.sendMessage("Each team must have at least one player.");
				if(broadcast) broadcastMessage("Each team must have at least "+ChatColor.YELLOW+_config.getMinTeamPlayers()+ChatColor.YELLOW+" player(s).");
				return false;
			}
		}
		return true;
	}
	
	public void checkPlayerQuit(RMPlayer rmp, RMTeam rmTeam){
		if(!rmTeam.hasMininumPlayers()){
			rmTeam.isDisqualified(true);
			rmTeam.clearPlayers();
			rmTeam.teamMessage(ChatColor.RED+"Your team was disqualified. "+ChatColor.GRAY+"It does not have enough players.");
			teamBroadcastMessage(rmTeam.getTeamColorString()+ChatColor.RED+" team was disqualified. "+ChatColor.GRAY+"It does not have enough players.", rmTeam);
		}
		if(!hasMinimumPlayers(false)){
			RMTeam winningTeam = null;
			for(RMTeam rmt : getTeams()){
				if(rmt!=rmTeam){
					winningTeam = rmt;
				}
			}
			setWinningTeam(winningTeam);
			gameOver();
		}
	}
	
	public void toggleReady(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.ready")){
			rmp.sendMessage(RMText.noPermissionCommand);
			return;
		}
		rmp.toggleReady();
		if(rmp.getReady()){
			rmp.sendMessage(ChatColor.GREEN+"Your body is ready.");
			broadcastMessage(rmp.getTeam().getChatColor()+rmp.getName()+ChatColor.GREEN+" is ready.", rmp);
		}
		else{
			rmp.sendMessage(ChatColor.RED+"Your body is not ready.");
			broadcastMessage(rmp.getTeam().getChatColor()+rmp.getName()+ChatColor.RED+" is not ready.", rmp);
		}
	}
	
	public void checkReady(){
		if(!hasMinimumPlayers(false)) return;
		if(!hasMinimumTeamPlayers(false)) return;
		RMPlayer[] players = getTeamPlayers();
		for(RMPlayer rmp : players){
			if(!rmp.getReady()) return;
		}
		clearReady();
		startGame(null);
	}
	
	public void clearReady(){
		RMPlayer[] players = getTeamPlayers();
		for(RMPlayer rmp : players){
			rmp.setReady(false);
		}
	}
	
	public void initGameStart(){
		stashChestsContents();
		//Clear player's inventory
		if(_config.getClearPlayerInventory()) snatchInventories();
		if(_config.getHealPlayer()) healPlayer();
		_config.getTimer().reset();
		_config.getTimer().addTimeMessage(_config.getTimer().getTimeLimit());
		updateSigns();
	}
	
	public boolean checkStartConditions(RMPlayer rmp){
		String rewardNotEqual = RMText.getStringByStringList(checkRewardEqual(rmp), ", ");
		String toolsNotEqual = RMText.getStringByStringList(checkToolsEqual(rmp, Arrays.asList(getTeamPlayers())), ", ");
		//Warn unequal
		if((_config.getWarnUnequal())||(!_config.getAllowUnequal())){
			if(rewardNotEqual.length()!=0) broadcastInstead(rmp, ChatColor.GRAY+"Unequal Reward distribution for: "+ChatColor.WHITE+rewardNotEqual);
			if(toolsNotEqual.length()!=0) broadcastInstead(rmp, ChatColor.GRAY+"Unequal tools distribution for: "+ChatColor.WHITE+toolsNotEqual);
		}
		//Allow unequal
		if(!_config.getAllowUnequal()){
			boolean itemsNotEqual = false;
			if(rewardNotEqual.length()!=0){
				broadcastInstead(rmp, ChatColor.RED+"Reward cannot be distributed equally!");
				itemsNotEqual = true;
			}
			if(toolsNotEqual.length()!=0){
				broadcastInstead(rmp, ChatColor.RED+"Tools cannot be distributed equally!");
				itemsNotEqual = true;
			}
			if(itemsNotEqual){
				broadcastInstead(rmp, ChatColor.GRAY+"Canceled");
				return false;
			}
		}
		else{
			if(rewardNotEqual.length()!=0){
				broadcastMessage(ChatColor.RED+"Reward won't be distributed equally!");
			}
			if(toolsNotEqual.length()!=0){
				broadcastMessage(ChatColor.RED+"Tools won't be distributed equally!");
			}
		}
		if(!hasMinimumPlayers(true)) return false;
		if(!hasMinimumTeamPlayers(true)) return false;
		return true;
	}
	
	public void startGame(RMPlayer rmp){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.noPermissionAction);
				return;
			}
			if(_config.getState()!=GameState.SETUP){
				rmp.sendMessage("Please use "+ChatColor.GOLD+"restart "+ChatColor.WHITE+"game instead.");
				return;
			}
		}
		for(RMChest rmChest : getChests()){
			rmChest.clearItems();
		}
		//Populate items by filter
		_config.getItems().populateByFilter(_config.getFilter());
		
		//Filter is empty
		if(_config.getItems().size()==0){
			broadcastInstead(rmp, "Configure the "+ChatColor.YELLOW+"filtered items"+ChatColor.WHITE+" first.");
			return;
		}
		
		if(!checkStartConditions(rmp)) return;
		
		//Randomize
		if(_config.getRandomizeAmount()>0) _config.getItems().randomize(_config.getRandomizeAmount());
		else _config.getItems().randomize(_config.getAutoRandomizeAmount());

		broadcastMessage(RMText.gPrepare);
		clearReady();
		_config.setState(GameState.COUNTDOWN);
	}
	public void restartGame(RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can restart the game.");
			return;
		}
		switch(_config.getState()){
			case GAMEPLAY:
				//rmp.sendMessage("Restarting game...");
				broadcastMessage(ChatColor.GREEN+"Restarting game...");
				stopGame(rmp, false);
				startGame(rmp);
				return;
			case SETUP:
				rmp.sendMessage(ChatColor.GRAY+"No game in progress.");
				return;
			case COUNTDOWN:
				rmp.sendMessage(ChatColor.GRAY+"Please wait for the game to start.");
				return;
		}
	}
	public void stopGame(RMPlayer rmp, boolean clearRandom){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		switch(_config.getState()){
			case GAMEPLAY:
				//rmp.sendMessage("Stopping game...");
				broadcastMessage(ChatColor.RED+"Stopping game...");
				initGameOver(clearRandom);
				return;
			case SETUP: rmp.sendMessage(ChatColor.GRAY+"No game in progress."); return;
			case COUNTDOWN: rmp.sendMessage(ChatColor.GRAY+"Please wait for the game to start."); return;
		}
	}
	
	private void initGameOver(boolean clearRandom){
		if(clearRandom) _config.clearRandomizeAmount();
		for(RMChest rmChest : getChests()){
			_config.getFound().addItems(rmChest.getItems());
			rmChest.clearItems();
			distributeFromChest(rmChest, ClaimType.ITEMS);
		}
		if(_config.getClearPlayerInventory()) returnInventories();
		returnChestsContents();
		if(_config.getWarpToSafety()) warpPlayersToSafety();
		if(_config.getAutoRestoreWorld()) restoreLog();
		_config.setState(GameState.SETUP);
		clearTeamPlayers();
		updateSigns();
	}
	
	public void gameOver(){
		RMTeam rmTeam = getWinningTeam();
		if(rmTeam!=null){
			_config.setState(GameState.GAMEOVER);
			broadcastMessage(rmTeam.getTeamColorString()+ChatColor.WHITE+" team has won the match!");
			for(RMChest rmChest : getChests()){
				_config.getFound().addItems(rmChest.getItems());;
				rmChest.clearItems();
				distributeFromChest(rmChest, ClaimType.ITEMS);
			}
			for(RMTeam rmt : getTeams()){
				if(rmt!=rmTeam){
					for(RMPlayer rmPlayer : rmt.getPlayers()){
						rmPlayer.getTools().transferTo(rmPlayer.getItems());
						rmPlayer.getStats().addLosses();
						rmPlayer.getStats().addTimesPlayed();
						_config.getStats().addLosses();
						_config.getStats().addTimesPlayed();
						RMStats.addServerLosses();
						RMStats.addServerTimesPlayed();
					}
				}
			}
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				rmPlayer.getTools().transferTo(rmPlayer.getItems());
				rmPlayer.getStats().addWins();
				rmPlayer.getStats().addTimesPlayed();
				_config.getStats().addWins();
				_config.getStats().addTimesPlayed();
				RMStats.addServerWins();
				RMStats.addServerTimesPlayed();
			}
			distributeReward(rmTeam);
			//setWinPlayer(rmp);
			update();
			initGameOver(true);
		}
	}
	
	//UPDATE
	public void update(){
		switch(_config.getState()){
		case SETUP:
			checkReady();
			break;
		case COUNTDOWN:
			//cdTimer = 0; //////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(cdTimer%10==0){
				if(cdTimer!=0){
					broadcastMessage(""+cdTimer/10);
					updateSigns();
				}
			}
			if(cdTimer>0){
				cdTimer-=10;
			}
			else{
				initGameStart();
				broadcastMessage(RMText.gStartMatch);
				cdTimer = cdTimerLimit;
				_config.setState(GameState.GAMEPLAY);

				for(RMTeam rmt : getTeams()){
					for(RMPlayer rmp : rmt.getPlayers()){
						rmp.getTools().transferTo(rmp.getItems());
						updateGameplayInfo(rmp, rmt);
					}
				}

				distributeTools(null);
				for(RMTeam rmt : getTeams()){
					for(RMPlayer rmp : rmt.getPlayers()){
						rmp.claimTools();
					}
					
				}
				if(_config.getWarpToSafety()) warpPlayersToSafety();
				updateSigns();
			}
			break;
		case GAMEPLAY:
			RMGameTimer timer = _config.getTimer();
			if((getOnlineTeamPlayers().length==0)||(timer.getTimeLimit()==0)) return;
			RMDebug.warning("time elapsed:"+timer.getTimeElapsed());
			RMDebug.warning("time remaining:"+timer.getTimeRemaining());
			if(timer.getTimeElapsed()<timer.getTimeLimit()){
				timer.announceTimeLeft(this);
				timer.addTimeElapsed();
			}
			else if(timer.getTimeElapsed()==timer.getTimeLimit()){
				timer.announceTimeLeft(this);
				setWinningTeam(findLeadingTeam());
				gameOver();
				if(getWinningTeam()==null){
					teamBroadcastMessage(RMText.gSuddenDeath);
					timer.addTimeElapsed();
				}
			}
			else{
				setWinningTeam(findLeadingTeam());
				gameOver();
			}
			break;
		case GAMEOVER:
			//initGameOver();
			break;
		}
	}

	//Sign info
	public void updateSigns(){
		InterfaceState interfaceState = _config.getInterface();
		switch(_config.getState()){
			case SETUP:
				switch(interfaceState){
				case FILTER:
					int items = _config.getFilter().size();
					int total = _config.getFilter().getItemsTotal();
					int totalHigh = _config.getFilter().getItemsTotalHigh();
					String lineTotal = ""+total;
					if(totalHigh>0) lineTotal+="-"+totalHigh;
					
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Filtered: "+items);
						sign.setLine(1, RMText.getStringTotal(lineTotal));
						sign.setLine(2, "inGame: "+getTeamPlayers().length+getTextPlayersOfMax());
						sign.setLine(3, "inTeam: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());
						sign.update();
					}
					break;
				case REWARD:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Reward: "+_config.getReward().size());//+items);
						sign.setLine(1, RMText.getStringTotal(""+_config.getReward().getAmount()));//lineTotal);
						sign.setLine(2, "inGame: "+getTeamPlayers().length+getTextPlayersOfMax());
						sign.setLine(3, "inTeam: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());
						sign.update();
					}
					break;
				case TOOLS:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Tools: "+_config.getTools().size());//+items);
						sign.setLine(1, RMText.getStringTotal(""+_config.getTools().getAmount()));//lineTotal);
						sign.setLine(2, "inGame: "+getTeamPlayers().length+getTextPlayersOfMax());
						sign.setLine(3, "inTeam: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());						
						sign.update();
					}
					break;
				case FILTER_CLEAR: case REWARD_CLEAR: case TOOLS_CLEAR:
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
					sign.setLine(2, ""+cdTimer/10);
					sign.setLine(3, "");
					sign.update();
				}
				break;
			case GAMEPLAY:
				for(RMTeam rmTeam : getTeams()){
					Sign sign = rmTeam.getSign();
					RMChest rmChest = rmTeam.getChest();
					if(!rmTeam.isDisqualified()){
						sign.setLine(0, "Items Left: "+rmChest.getItemsLeftInt());
						sign.setLine(1, "Total: "+rmChest.getTotalLeft());
						sign.setLine(2, "inGame: "+getTeamPlayers().length+getTextPlayersOfMax());
						sign.setLine(3, "inTeam: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());
					}
					else{
						sign.setLine(0, "");
						sign.setLine(1, "TEAM");
						sign.setLine(2, "DISQUALIFIED");
						sign.setLine(3, "");
					}
					sign.update();
				}
				break;
			case GAMEOVER:
				break;
			case PAUSED:
				for(RMTeam rmTeam : getTeams()){
					Sign sign = rmTeam.getSign();
					sign.setLine(0, "");
					sign.setLine(1, "GAME");
					sign.setLine(2, "PAUSED");
					sign.setLine(3, "");
					sign.update();
				}
				break;
		}
	}
	
	public void trySignSetupInfo(RMPlayer rmp){
		updateSigns();
		String items = "";
		
		//Sort
		Integer[] array = _config.getFilter().keySet().toArray(new Integer[_config.getFilter().keySet().size()]);
		Arrays.sort(array);
		
		if(array.length>plugin.config.getTypeLimit()){
			for(Integer id : array){
				items += ChatColor.WHITE+""+id+RMText.includeItem(_config.getFilter().getItem(id))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer id : array){
				items += ChatColor.WHITE+""+Material.getMaterial(id)+RMText.includeItem(_config.getFilter().getItem(id))+ChatColor.WHITE+", ";
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
			updateGameplayInfo(rmp, rmTeam);
			updateSigns();
		}
		else if(rmTeam!=rmp.getTeam()){
			updateGameplayInfo(rmp, rmTeam);
			updateSigns();
		}
	}
	
	public void trySignSetupRewardInfo(RMPlayer rmp){
		updateSigns();
		String items = RMText.getStringSortedItems(_config.getReward().getItems());
		if(items.length()>0){
			rmp.sendMessage(ChatColor.YELLOW+"Reward: "+items);
		}
		else rmp.sendMessage(ChatColor.YELLOW+"No Reward added.");
	}
	
	public void trySignSetupToolsInfo(RMPlayer rmp){
		updateSigns();
		String items = RMText.getStringSortedItems(_config.getTools().getItems());
		if(items.length()>0){
			rmp.sendMessage(ChatColor.YELLOW+"Tools: "+items);
		}
		else rmp.sendMessage(ChatColor.YELLOW+"No tools added.");
	}
	
	public void updateGameplayInfo(RMPlayer rmp, RMTeam rmTeam){
		//RMTeam rmTeam = rmp.getTeam();
		String strItems = "";
		RMChest rmChest = rmTeam.getChest();
		
		//Sort
		Integer[] array = _config.getItems().keySet().toArray(new Integer[_config.getItems().keySet().size()]);
		Arrays.sort(array);
		
		HashMap<Integer, RMItem> items = rmChest.getRMItems();
		
		if(array.length>plugin.config.getTypeLimit()){
			for(Integer id : array){
				RMItem rmItem = _config.getItems().getItem(id);
				int amount = rmItem.getAmount();
				if(items.containsKey(id)) amount -= items.get(id).getAmount();
				if(amount>0) strItems += ChatColor.WHITE+""+id+RMText.includeItem(new RMItem(id, amount))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer id : array){
				RMItem rmItem = _config.getItems().getItem(id);
				int amount = rmItem.getAmount();
				if(items.containsKey(id)) amount -= items.get(id).getAmount();
				if(amount>0) strItems += ChatColor.WHITE+""+Material.getMaterial(id)+RMText.includeItem(new RMItem(id, amount))+ChatColor.WHITE+", ";
			}
		}
		if(strItems.length()>0){
			strItems = strItems.substring(0, strItems.length()-2);
			rmp.sendMessage(ChatColor.YELLOW+"Items left: "+strItems);
		}
		else rmp.sendMessage(ChatColor.YELLOW+"No items left.");
	}
	
	//Get items from chests
	public List<ItemStack> getFilterItemsFromChests(RMChest... rmChests){
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
		if(_config.getWarnHackedItems()) warnHackMaterialsByItems(items);
		if(!_config.getAllowHackedItems()) items = removeHackMaterialsByItems(items);
		return items;
	}
	
	//Try Add Items
	public void tryAddItemsToFilter(Block b, RMPlayer rmp, ClickState clickState){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			List<ItemStack> items = new ArrayList<ItemStack>();
			boolean force;
			if(clickState!=ClickState.NONE){
				items = getFilterItemsFromChests(getChestByBlock(b));
				force = true;
			}
			else{
				items = getFilterItemsFromChests(getChests());
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
												addedItems+=ChatColor.WHITE+item.name()+RMText.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+RMText.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
										}
										break;
									case RIGHT:
										switch(_config.getFilter().removeItem(id, rmItem, force)){
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+RMText.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case REMOVE:
												removedItems+=ChatColor.WHITE+item.name()+RMText.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE+", ";
												break;
										}
										break;
								}
							}
						}
					}
				}
				if(addedItems.length()>0){
					addedItems = RMText.stripLast(addedItems, ",");
					rmp.sendMessage(ChatColor.YELLOW+"Added: "+addedItems);
				}
				if(modifiedItems.length()>0){
					modifiedItems = RMText.stripLast(modifiedItems, ",");
					rmp.sendMessage(ChatColor.YELLOW+"Modified: "+modifiedItems);
				}
				if(removedItems.length()>0){
					removedItems = RMText.stripLast(removedItems, ",");
					rmp.sendMessage(ChatColor.GRAY+"Removed: "+removedItems);
				}
			}
			else if(rmp.getPlayer().isSneaking()){
				if(clickState!=ClickState.NONE){
					if(rmp.hasPermission("resourcemadness.filter.byhand")) tryAddItemToFilter(rmp, clickState);
					else rmp.sendMessage(RMText.noPermissionAction);
				}
			}
			else if(_config.getFilter().size()>0){
				if(rmp.hasPermission("resourcemadness.filter.clear")){
					if(clickState == ClickState.NONE){
						rmp.sendMessage("Click sign to clear all items.");
						_config.setInterface(InterfaceState.FILTER_CLEAR);
					}
				}
				else rmp.sendMessage(RMText.noPermissionAction);
			}
			updateSigns();
		}
	}
	
	public void tryAddItemToFilter(RMPlayer rmp, ClickState clickState){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			ItemStack item = rmp.getPlayer().getItemInHand();
			
			boolean force;
			if(clickState!=ClickState.NONE) force = true;
			else force = false;
			
			if(item!=null){
				int id = item.getTypeId();
				Material mat = item.getType();
				if(!RMHelper.isMaterial(mat, _hackMaterials)){
					RMItem rmItem;
					int amount = 1;
					rmItem = new RMItem(id, amount);
					switch(clickState){
					case NONE: case LEFT:
						switch(_config.getFilter().addItem(id, rmItem, force)){
							case ADD:
								rmp.sendMessage(ChatColor.YELLOW+"Added: "+ChatColor.WHITE+mat.name()+RMText.includeItem(rmItem));
								break;
							case MODIFY:
								rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+RMText.includeItem(rmItem));
								break;
						}
						break;
					case RIGHT:
						switch(_config.getFilter().removeItem(id, rmItem, force)){
							case MODIFY:
								rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+RMText.includeItem(rmItem));
								break;
							case REMOVE:
								rmp.sendMessage(ChatColor.GRAY+"Removed: "+ChatColor.WHITE+mat.name()+RMText.includeItem(rmItem));
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
			returned = _config.getItems().cloneItems(-1);
			for(int i=0; i<inv.getSize(); i++){
				ItemStack item = inv.getItem(i);
				int id = item.getTypeId();
				if(item!=null){
					if(item.getType()!=Material.AIR){
						if(_config.getItems().containsKey(id)){
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
			_config.getStats().addItemsFoundTotal(totalFound);
			RMStats.addServerItemsFoundTotal(totalFound);
			if(added.size()>0){
				if(returned.size()>0){
					rmp.getTeam().teamMessage(ChatColor.YELLOW+"Items left: "+getFormattedStringByHash(returned, rmp));
					teamBroadcastMessage(rmp.getTeam().getTeamColorString()+ChatColor.WHITE+" team has "+ChatColor.YELLOW+rmChest.getItemsLeftInt()+ChatColor.WHITE+" item(s) ("+ChatColor.YELLOW+rmChest.getTotalLeft()+ChatColor.WHITE+" total) left.", rmp.getTeam());
				}
			}
			else updateGameplayInfo(rmp, rmTeam);
		}
		updateSigns();
	}
		
	public void handleRightClick(Block b, RMPlayer rmp){
		Material mat = b.getType();
		switch(_config.getState()){
			case SETUP:
				switch(_config.getInterface()){
					case FILTER:
						switch(mat){
						case CHEST:
							if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.RIGHT);
							break;
						case WALL_SIGN:
							//trySignSetupInfo(rmp);
							break;
						case WOOL:
							//if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
							break;
						}
						break;
					case REWARD:
						switch(mat){
						case CHEST:
							if(rmp.getPlayer().isSneaking()) addRewardByChest(rmp, getChestByBlock(b), ClickState.RIGHT);
							break;
						case WALL_SIGN:
							//trySignSetupInfo(rmp);
							break;
						case WOOL:
							//if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
							break;
						}
						break;
					case TOOLS:
						switch(mat){
						case CHEST:
							if(rmp.getPlayer().isSneaking()) addToolsByChest(rmp, getChestByBlock(b), ClickState.RIGHT);
							break;
						case WALL_SIGN:
							//trySignSetupInfo(rmp);
							break;
						case WOOL:
							//if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
							break;
						}
						break;
				}
				break;
		}
	}
	
	public void handleLeftClick(Block b, RMPlayer rmp){
		Material mat = b.getType();
		switch(_config.getState()){
			// Setup State
			case SETUP:
				switch(_config.getInterface()){
				case FILTER: //MAIN
					switch(mat){
					case CHEST:
						if(!rmp.hasPermission("resourcemadness.filter")){
							rmp.sendMessage(RMText.noPermissionAction);
							return;
						}
						if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.getPlayer().isSneaking()) cycleMode(rmp);
						else trySignSetupInfo(rmp);
						break;
					case WOOL:
						if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case REWARD:
					switch(mat){
					case CHEST:
						if(!rmp.hasPermission("resourcemadness.reward")){
							rmp.sendMessage(RMText.noPermissionAction);
							return;
						}
						//if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						//else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						if(rmp.getPlayer().isSneaking()) addRewardByChest(rmp, getChestByBlock(b), ClickState.LEFT);
						else addRewardByChest(rmp, getChestByBlock(b), ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.getPlayer().isSneaking()) cycleMode(rmp);
						else trySignSetupRewardInfo(rmp);
						break;
					case WOOL:
						if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case TOOLS:
					switch(mat){
					case CHEST:
						if(!rmp.hasPermission("resourcemadness.tools")){
							rmp.sendMessage(RMText.noPermissionAction);
							return;
						}
						//if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						//else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						if(rmp.getPlayer().isSneaking()) addToolsByChest(rmp, getChestByBlock(b), ClickState.LEFT);
						else addToolsByChest(rmp, getChestByBlock(b), ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.getPlayer().isSneaking()) cycleMode(rmp);
						else trySignSetupToolsInfo(rmp);
						break;
					case WOOL:
						if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case FILTER_CLEAR: case REWARD_CLEAR: case TOOLS_CLEAR: //FILTER CLEAR
					switch(mat){
					case CHEST: case GLASS: case STONE:
						if(rmp.hasOwnerPermission(_config.getOwnerName())){
							rmp.sendMessage(ChatColor.GRAY+"Canceled.");
							_config.setInterface(getParentInterface(_config.getInterface()));
							updateSigns();
						}
						break;
					case WALL_SIGN:
						if(rmp.hasOwnerPermission(_config.getOwnerName())){
							switch(_config.getInterface()){
								case FILTER_CLEAR:
									clearFilter(rmp);
									break;
								case REWARD_CLEAR:
									clearReward(b.getRelative(BlockFace.DOWN), rmp);
									break;
								case TOOLS_CLEAR:
									clearTools(b.getRelative(BlockFace.DOWN), rmp);
									break;
							}
							_config.setInterface(getParentInterface(_config.getInterface()));
							updateSigns();
						}
						break;
					case WOOL:
						if(rmp.hasOwnerPermission(_config.getOwnerName())){
							rmp.sendMessage(ChatColor.GRAY+"Canceled.");
							_config.setInterface(getParentInterface(_config.getInterface()));
							updateSigns();
						}
						else{
							if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
							else sendTeamInfo(rmp);
						}
						break;
					}
					break;
				}
				break;
				
			// Countdown State
			case COUNTDOWN:
				switch(mat){
				case CHEST: case WALL_SIGN: case WOOL:
					if(rmp.hasOwnerPermission(_config.getOwnerName())){
						//stopGame(rmp);
					}
					break;
				}
				break;
				
			// Gameplay State
			case GAMEPLAY:
				switch(mat){
				case CHEST:
					RMTeam rmTeam = getTeamByBlock(b);
					if((rmTeam!=null)&&(rmp.getTeam()!=rmTeam)){
						trySignGameplayInfo(rmTeam.getChest().getChest().getBlock(), rmp);
						break;
					}
					tryAddFoundItems(b, rmp);
					setWinningTeam(findWinningTeam());
					gameOver();
					break;
				case WALL_SIGN:
					trySignGameplayInfo(b, rmp);
					break;
				case WOOL:
					if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
					else sendTeamInfo(rmp);
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
			case PAUSED:
				rmp.sendMessage(ChatColor.RED+"Game is paused. "+ChatColor.WHITE+"Use "+ChatColor.YELLOW+"/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"resume "+ChatColor.WHITE+"to resume it.");
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
	
	public InterfaceState getParentInterface(InterfaceState interfaceState){
		switch(interfaceState){
			case FILTER_CLEAR: return InterfaceState.FILTER;
			case REWARD_CLEAR: return InterfaceState.REWARD;
			case TOOLS_CLEAR: return InterfaceState.TOOLS;
			default: return InterfaceState.FILTER;
		}
	}
	
	public int getFreeId(){
		int i=0;
		int freeId=-1;
		HashMap<Integer, RMGame> games = RMGame.getGames();
		while(freeId==-1){
			if(!games.containsKey(i)) freeId = i;
			i++;
		}
		return freeId;
	}
	
	//Game
	public Block getMainBlock(){
		return _config.getPartList().getMainBlock();
	}
	
	//Game GET-SET
	public RMGame getGame(){
		return this;
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
	
	/*
	public void sendMessage(String message){
		RMPlayer[] rmplayers = _config.getPlayers().values().toArray(new RMPlayer[_config.getPlayers().values().size()]);
		for(RMPlayer rmp : rmplayers){
			rmp.sendMessage(message);
		}
	}
	*/
	
	public void broadcastInstead(RMPlayer rmp, String message){
		if(rmp!=null) rmp.sendMessage(message);
		else broadcastMessage(message);
	}

	public void broadcastMessage(String message){
		RMPlayer rmp = _config.getOwner();
		rmp.sendMessage(message);
		List<RMTeam> teams = getTeams();
		for(RMTeam rmt : teams){
			RMPlayer[] players = rmt.getPlayers();
			for(RMPlayer rmPlayer : players){
				if(rmp!=rmPlayer) rmPlayer.sendMessage(message);
			}
		}
	}
	public void broadcastMessage(String message, RMPlayer ignorePlayer){
		RMPlayer rmp = _config.getOwner();
		if(rmp!=ignorePlayer) rmp.sendMessage(message);
		List<RMTeam> teams = getTeams();
		for(RMTeam rmt : teams){
			RMPlayer[] players = rmt.getPlayers();
			for(RMPlayer rmPlayer : players){
				if(rmp!=rmPlayer) if(rmPlayer!=ignorePlayer) rmPlayer.sendMessage(message);
			}
		}
	}
	public void teamBroadcastMessage(String message){
		for(RMTeam rmTeam : getTeams()){
			rmTeam.teamMessage(message);
		}
	}
	public void teamBroadcastMessage(String message, RMTeam ignoreTeam){
		for(RMTeam rmTeam : getTeams()){
			if(rmTeam!=ignoreTeam) rmTeam.teamMessage(message);
		}
	}
	//Team
	public void teamMessage(RMTeam rmt, String message){
		if(rmt!=null) rmt.teamMessage(message);
	}
	public void teamMessage(RMTeam rmt, String message, RMPlayer ignorePlayer){
		if(rmt!=null) rmt.teamMessage(message, ignorePlayer);
	}
	
	public void joinTeam(RMTeam rmTeam, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.join")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		rmTeam.addPlayer(rmp);
	}
	public void quitTeam(RMTeam rmTeam, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		rmTeam.removePlayer(rmp);
	}
	public void quitTeam(RMGame rmGame, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		RMTeam rmTeam = rmGame.getPlayerTeam(rmp);
		if(rmTeam!=null){
			rmTeam.removePlayer(rmp);
			//rmp.sendMessage("You quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team.");
		}
	}
	
	public RMTeam joinTeamByBlock(Block b, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.join")){
			rmp.sendMessage(RMText.noPermissionAction);
			return null;
		}
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
		else rmp.sendMessage(ChatColor.GRAY+"This team does not exist!");
		return null;
	}
	
	public RMTeam joinQuitTeamByBlock(Block b, RMPlayer rmp, boolean fromConsole){
		if(_config.getState() == GameState.SETUP){
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
			else rmp.sendMessage(ChatColor.GRAY+"This team does not exist!");
		}
		else{
			rmp.sendMessage("Teams: "+getTextTeamPlayers());
			if(rmp.isIngame()) rmp.sendMessage("To quit a game in progress, use /rm "+ChatColor.YELLOW+"quit"+ChatColor.WHITE+".");
			else rmp.sendMessage("To join a game in progress, use /rm "+ChatColor.YELLOW+"join"+ChatColor.WHITE+".");
		}
		
		return null;
	}
	public RMTeam quitTeamByBlock(Block b, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(RMText.noPermissionAction);
			return null;
		}
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
		if(RMHelper.isMaterial(b.getType(), Material.CHEST, Material.WALL_SIGN, Material.WOOL)){
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
	
	public RMTeam getTeamByDye(String arg, RMGame rmGame){
		DyeColor color = RMHelper.getDyeByString(arg);
		if(color!=null) return rmGame.getTeam(color);
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
	
	public RMPlayer[] getTeamPlayers(){
		List<RMPlayer> list = new ArrayList<RMPlayer>();
		for(RMTeam rmTeam : _config.getTeams()){
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				list.add(rmPlayer);
			}
		}
		return list.toArray(new RMPlayer[list.size()]);
	}
	
	public RMPlayer[] getOnlineTeamPlayers(){
		List<RMPlayer> list = new ArrayList<RMPlayer>();
		for(RMTeam rmTeam : _config.getTeams()){
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				if(rmPlayer.isOnline()) list.add(rmPlayer);
			}
		}
		return list.toArray(new RMPlayer[list.size()]);
	}
	
	public void clearTeamPlayers(){
		for(RMTeam rmTeam : getTeams()){
			rmTeam.isDisqualified(false);
			rmTeam.clearPlayers();
		}
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

	//Try Parse Filter
	public Boolean tryParseFilter(Block b, RMPlayer rmp){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			RMGame.setRequestPlayer(rmp);
			parseFilter(b, rmp);
			RMGame.clearRequestPlayer();
			rmp.clearRequestFilter();
			updateSigns();
			return true;
		}
		else rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can modify the filter.");
		return false;
	}
	
	private HashMap<Integer, RMItem> getItemsMatch(Inventory inv, HashMap<Integer, RMItem> hashItems){
		HashMap<Integer, RMItem> items = new HashMap<Integer, RMItem>();
		if(inv==null) return items;
		ItemStack[] contents = inv.getContents();
		for(int i=0; i<contents.length; i++){
			ItemStack item = contents[i];
			if((item!=null)&&(item.getType()!=Material.AIR)){
				int id = item.getTypeId();
				if(hashItems.containsKey(id)){
					ItemStack hashItem = hashItems.get(id).getItem();
					int overflow = hashItem.getAmount() - item.getAmount();
					if(!items.containsKey(id)){
						RMItem rmItem = new RMItem(item);
						rmItem.setAmount(rmItem.getAmount()+(overflow<0?overflow:0));
						items.put(id, rmItem);
					}
					else{
						RMItem rmItem = items.get(id);
						rmItem.setAmount(rmItem.getAmount()+item.getAmount()+(overflow<0?overflow:0));
						items.put(id, rmItem);
					}
					hashItem.setAmount(overflow<=0?0:overflow);
					if(hashItem.getAmount()<=0) hashItems.remove(id);
					if(overflow>=0) inv.clear(i);
					else item.setAmount(-overflow);
				}
			}
		}
		return items;
	}
	
	//Parse Filter
	private void parseFilter(Block b, RMPlayer rmp){
		RMRequestFilter filter = rmp.getRequestFilter();
		if(filter!=null){
			ForceState force = filter.getForce();
			int randomize = filter.getRandomize();
			FilterState filterState = filter.getFilterState();
			HashMap<Integer, RMItem> items = filter.getItems();
			if((items!=null)&&(items.size()!=0)){
				switch(filterState){
				case REWARD: case TOOLS:
					if((force!=null)&&(force==ForceState.ADD)){
						switch(filterState){
							case REWARD: if(!_config.getInfiniteReward()) items = getItemsMatch(rmp.getPlayer().getInventory(), items); break;
							case TOOLS: if(!_config.getInfiniteTools()) items = getItemsMatch(rmp.getPlayer().getInventory(), items); break;
						}
					}
				}
				if(_config.getWarnHackedItems()) warnHackMaterials(items);
				if(!_config.getAllowHackedItems()){
					items = removeHackMaterials(items);
					if(items==null){
						rmp.sendMessage(ChatColor.GRAY+"No items modified.");
						return;
					}
				}
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
				
				if(force!=null) parseFilterArgs(rmp, items, force);
				else parseFilterArgs(rmp, items);
				return;
			}
			else if(force==ForceState.CLEAR){
				switch(filterState){
					case FILTER: clearFilter(rmp); return;
					case REWARD: clearReward(b, rmp); return;
					case TOOLS: clearTools(b, rmp); return;
				}
			}
			else{
				switch(filterState){
					case FILTER: plugin.rmFilterInfo(rmp); break;
					case REWARD: plugin.rmRewardInfo(rmp); break;
					case TOOLS: plugin.rmToolsInfo(rmp); break;
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
		FilterState filterState = rmp.getRequestFilter().getFilterState();
		String strItem;
		Boolean getId = false;
		if(items.size()>plugin.config.getTypeLimit()) getId=true;
		Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(arrayItems);
		if(filterState==FilterState.FILTER){
			for(Integer item : arrayItems){
				Material mat = Material.getMaterial(item);
				if(mat!=Material.AIR){
					if(getId) strItem = ""+item;
					else strItem = mat.name();
					Integer amount = items.get(item).getAmount();
					if(amount!=0){
						switch(_config.getFilter().addItem(item, items.get(item), false)){
						case ADD:
							added.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item)));
							break;
						case MODIFY:
							modified.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item)));	
							break;
						}
					}
					else{
						switch(_config.getFilter().removeAlwaysItem(item, items.get(item))){
						case REMOVE:
							removed.add(ChatColor.WHITE+strItem+RMText.includeItem(_config.getFilter().getLastItem()));
							break;
						}
					}
				}
			}
			
			if(added.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+RMText.getFormattedStringByList(added));
			if(modified.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+RMText.getFormattedStringByList(modified));
			if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+RMText.getFormattedStringByList(removed));
		}
		else{
			switch(filterState){
			case REWARD:
				_config.getReward().setItemsMatchInventory(rmp.getPlayer().getInventory(), rmp, ClaimType.REWARD, RMFilter.convertRMHashToHash(items));
				_config.getReward().showChanged(rmp);
				break;
			case TOOLS:
				_config.getTools().setItemsMatchInventory(rmp.getPlayer().getInventory(), rmp, ClaimType.TOOLS, RMFilter.convertRMHashToHash(items));
				_config.getTools().showChanged(rmp);
			break;
			}
		}
	}
	
	//Parse Filter Args Force
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, RMItem> items, ForceState force){
		List<String> added = new ArrayList<String>();
		List<String> modified = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		FilterState filterState = rmp.getRequestFilter().getFilterState();
		String strItem;
		Boolean getId = false;
		if(items.size()>plugin.config.getTypeLimit()) getId=true;
		Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(arrayItems);
		if(filterState==FilterState.FILTER){
			switch(force){
			case ADD:
				for(Integer item : arrayItems){
					Material mat = Material.getMaterial(item);
					if(mat!=Material.AIR){
						if(getId) strItem = ""+item;
						else strItem = mat.name();
						switch(_config.getFilter().addItem(item, items.get(item), true)){
							case ADD:
								added.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item))+ChatColor.WHITE);
								break;
							case MODIFY:
								modified.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item))+ChatColor.WHITE);
								break;
						}
					}
				}
				if(added.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+RMText.getFormattedStringByList(added));
				if(modified.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+RMText.getFormattedStringByList(modified));
				break;
			case SUBTRACT:
				for(Integer item : arrayItems){
					Material mat = Material.getMaterial(item);
					if(mat!=Material.AIR){
						if(getId) strItem = ""+item;
						else strItem = mat.name();
						switch(_config.getFilter().removeItem(item, items.get(item), true)){
							case MODIFY:
								modified.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item))+ChatColor.WHITE);
								break;
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+RMText.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE);
								break;
						}
					}
				}
				if(modified.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+RMText.getFormattedStringByList(modified));
				if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+RMText.getFormattedStringByList(removed));
				break;
			case CLEAR:
				for(Integer item : arrayItems){
					Material mat = Material.getMaterial(item);
					if(mat!=Material.AIR){
						if(getId) strItem = ""+item;
						else strItem = mat.name();
						switch(_config.getFilter().removeAlwaysItem(item, items.get(item))){
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+RMText.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE);
								break;
						}
					}
				}
				if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+RMText.getFormattedStringByList(removed));
				break;
			}
		}
		else{
			List<ItemStack> listItems = RMFilter.convertToListItemStack(items);
			switch(force){
			case ADD:
				switch(filterState){
				case REWARD:
					_config.getReward().addItems(listItems);
					_config.getReward().showChanged(rmp);
					break;
				case TOOLS:
					_config.getTools().addItems(listItems);
					_config.getTools().showChanged(rmp);
					break;
				}
				break;
			case SUBTRACT:
				switch(filterState){
				case REWARD:
					if(_config.getInfiniteReward()) _config.getReward().removeItems(listItems);
					else rmp.claim(_config.getReward(), ClaimType.REWARD, listItems.toArray(new ItemStack[listItems.size()]));
					_config.getReward().showChanged(rmp);
					break;
				case TOOLS:
					if(_config.getInfiniteTools()) _config.getTools().removeItems(listItems);
					else rmp.claim(_config.getTools(), ClaimType.TOOLS, listItems.toArray(new ItemStack[listItems.size()]));
					_config.getTools().showChanged(rmp);
					break;
				}
				break;
			case CLEAR:
				switch(filterState){
				case REWARD:
					if(_config.getInfiniteReward()) _config.getReward().removeItemsWhole(listItems);
					else rmp.claim(_config.getReward(), ClaimType.REWARD, true, listItems.toArray(new ItemStack[listItems.size()]));
					_config.getReward().showChanged(rmp);
					break;
				case TOOLS:
					if(_config.getInfiniteTools()) _config.getTools().removeItemsWhole(listItems);
					else rmp.claim(_config.getTools(), ClaimType.TOOLS, true, listItems.toArray(new ItemStack[listItems.size()]));
					_config.getTools().showChanged(rmp);
					break;
				}
			}
		}
	}
	
	//Get Formatted String by Hash
	public String getFormattedStringByHash(HashMap<Integer, RMItem> items, RMPlayer rmp){
		RMChest rmChest = rmp.getTeam().getChest();
		String line = "";
		Integer[] array = items.keySet().toArray(new Integer[items.keySet().size()]);
		Arrays.sort(array);
		boolean useId = array.length>plugin.config.getTypeLimit()?true:false;
		for(Integer item : array){
			String itemId = useId?""+item:Material.getMaterial(item).name();
			RMItem rmItem = items.get(item);
			int amount = rmItem.getAmount(); 
			if(amount!=-1){
				itemId = Material.getMaterial(item).name();
				if(amount!=0){
					line+=ChatColor.GREEN+itemId+RMText.includeItem(rmItem)+ChatColor.WHITE+", ";
				}
				else line+=ChatColor.DARK_GREEN+itemId+":0"+ChatColor.WHITE+", ";
			}
			else{
				if(rmChest.getRMItems().containsKey(item)) amount = rmChest.getItemLeft(item).getAmount();
				else amount = _config.getItems().getItem(item).getAmount();
				if(amount!=0) line+=ChatColor.WHITE+itemId+RMText.includeItem(new RMItem(item, amount))+ChatColor.WHITE+", ";
			}
		}
		line = RMText.stripLast(line, ",");
		return line;
	}
	
	public String getSimpleFormattedStringByHash(HashMap<Integer, RMItem> items){
		String line = "";
		Integer[] array = items.keySet().toArray(new Integer[items.keySet().size()]);
		Arrays.sort(array);
		for(Integer item : array){
			RMItem rmItem = items.get(item);
			line+=ChatColor.WHITE+Material.getMaterial(item).name()+RMText.includeItem(new RMItem(item, rmItem.getAmount()))+ChatColor.WHITE+", ";
		}
		line = RMText.stripLast(line, ",");
		return line;
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
	public List<Material> findHackMaterialsByItems(List<ItemStack> items){
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
	public List<ItemStack> removeHackMaterialsByItems(List<ItemStack> items){
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
		return newItems;
	}
	
	//LOG WORLD
	public void addLog(BlockState bState){
		_config.getLog().add(bState);
	}
	
	//Clear Log
	public void clearLog(){
		_config.getLog().clear();
	}
	
	public void restoreLog(){
		if(plugin.config.getUseRestore()) if(_config.getLog().restore()) broadcastMessage(ChatColor.YELLOW+"World restored.");
	}
	
	//Restore World
	public void restoreWorld(RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		switch(_config.getState()){
		case SETUP: restoreLog(); break;
		default: rmp.sendMessage(ChatColor.GRAY+"Cannot restore world changes while a game is in progress.");
		}
	}
	
	//HACK MATERIALS
	//Warn Hack Materials
	
	/*public void warnHackMaterials(ItemStack[] items){
		warnHackMaterialsMessage(plugin.getFormattedStringByListMaterial(findHackMaterials(items)));
	}*/
	
	public void warnHackMaterialsByItems(List<ItemStack> items){
		warnHackMaterialsMessage(RMText.getFormattedStringByListMaterial(findHackMaterialsByItems(items)));
	}
	public void warnHackMaterials(List<Material> items){
		warnHackMaterialsMessage(RMText.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterials(HashMap<Integer, RMItem> items){
		warnHackMaterialsMessage(RMText.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterialsMessage(String message){
		if(message.length()>0) _config.getOwner().sendMessage(ChatColor.RED+"Not allowed: "+message);
	}
	
	//Config
	
	//Set Min Players
	public void setMinPlayers(RMPlayer rmp, int minPlayers){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setMinPlayers(minPlayers);
			_config.correctMinMaxNumbers(MinMaxType.MIN_PLAYERS);
			rmp.sendMessage(RMText.minPlayers+": "+getTextMinPlayers());
			sendMinMax(rmp);
			updateSigns();
		}
	}
	
	//Set Max Players
	public void setMaxPlayers(RMPlayer rmp, int maxPlayers){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setMaxPlayers(maxPlayers);
			_config.correctMinMaxNumbers(MinMaxType.MAX_PLAYERS);
			rmp.sendMessage(RMText.maxPlayers+": "+getTextMaxPlayers());
			sendMinMax(rmp);
			updateSigns();
		}
	}
	
	//Set Min Team Players
	public void setMinTeamPlayers(RMPlayer rmp, int minTeamPlayers){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setMinTeamPlayers(minTeamPlayers);
			_config.correctMinMaxNumbers(MinMaxType.MIN_TEAM_PLAYERS);
			rmp.sendMessage(RMText.minTeamPlayers+": "+getTextMinTeamPlayers());
			sendMinMax(rmp);
			updateSigns();
		}
	}
	
	//Set Max Team Players
	public void setMaxTeamPlayers(RMPlayer rmp, int maxTeamPlayers){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setMaxTeamPlayers(maxTeamPlayers);
			_config.correctMinMaxNumbers(MinMaxType.MAX_TEAM_PLAYERS);
			rmp.sendMessage(RMText.maxTeamPlayers+": "+getTextMaxTeamPlayers());
			sendMinMax(rmp);
			updateSigns();
		}
	}
	
	//Set Max Items
	public void setMaxItems(RMPlayer rmp, int maxItems){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setMaxItems(maxItems);
			updateSigns();
		}
	}
	
	//Set Time Limit
	public void setTimeLimit(RMPlayer rmp, int limit){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.getTimer().setTimeLimit(limit*60);
			_config.getTimer().reset();
			_config.getTimer().addTimeMessage(_config.getTimer().getTimeLimit());
			rmp.sendMessage(RMText.timeLimit+": "+getTextTimeLimit());
		}
	}
	
	//Set Randomize Amount
	public void setRandomizeAmount(RMPlayer rmp, int amount){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setRandomizeAmount(amount);
		}
	}
	
	//Set Auto Randomize Amount
	public void setAutoRandomizeAmount(RMPlayer rmp, int amount){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setAutoRandomizeAmount(amount);
			rmp.sendMessage("Auto randomize amount every match: "+getTextAutoRandomizeAmount());
		}
	}
	
	//SET & TOGGLE
	//Set Toggle Advertise
	public void setAdvertise(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.advertise)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleAdvertise();
			else _config.setAdvertise(i>0?true:false);
			rmp.sendMessage(RMText.advertise+": "+isTrueFalse(_config.getAdvertise()));
		}
	}
	
	//Set Toggle Auto Restore World
	public void setAutoRestoreWorld(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.autoRestoreWorld)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleAutoRestoreWorld();
			else _config.setAutoRestoreWorld(i>0?true:false);
			rmp.sendMessage(RMText.autoRestoreWorld+": "+isTrueFalse(_config.getAutoRestoreWorld()));
		}
	}
	
	//Set Toggle Warp to Safety
	public void setWarpToSafety(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.warpToSafety)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleWarpToSafety();
			else _config.setWarpToSafety(i>0?true:false);
			rmp.sendMessage(RMText.warpToSafety+": "+isTrueFalse(_config.getWarpToSafety()));
		}
	}
	
	//Set Toggle Midgame Join
	public void setAllowMidgameJoin(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.allowMidgameJoin)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleAllowMidgameJoin();
			else _config.setAllowMidgameJoin(i>0?true:false);
			rmp.sendMessage(RMText.allowMidgameJoin+": "+isTrueFalse(_config.getAllowMidgameJoin()));
		}
	}
	
	//Set Toggle Heal Player
	public void setHealPlayer(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.healPlayer)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleHealPlayer();
			else _config.setHealPlayer(i>0?true:false);
			rmp.sendMessage(RMText.healPlayer+": "+isTrueFalse(_config.getHealPlayer()));
		}
	}
	
	//Set Toggle Clear Player Inventory
	public void setClearPlayerInventory(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.clearPlayerInventory)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleClearPlayerInventory();
			else _config.setClearPlayerInventory(i>0?true:false);
			rmp.sendMessage(RMText.clearPlayerInventory+": "+isTrueFalse(_config.getClearPlayerInventory()));
		}
	}
	
	//Set Warn Unequal
	public void setWarnUnequal(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.warnUnequal)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleWarnUnequal();
			else _config.setWarnUnequal(i>0?true:false);
			rmp.sendMessage(RMText.warnUnequal+": "+isTrueFalse(_config.getWarnUnequal()));
		}
	}
	
	//Set Allow Unequal
	public void setAllowUnequal(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.allowUnequal)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleAllowUnequal();
			else _config.setAllowUnequal(i>0?true:false);
			rmp.sendMessage(RMText.allowUnequal+": "+isTrueFalse(_config.getAllowUnequal()));
		}
	}
	
	//Set Toggle Warn Hacked Items
	public void setWarnHackedItems(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.warnHackedItems)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleWarnHackedItems();
			else _config.setWarnHackedItems(i>0?true:false);
			rmp.sendMessage(RMText.warnHackedItems+": "+isTrueFalse(_config.getWarnHackedItems()));
		}
	}
	
	//Set Toggle Allow Hacked Items
	public void setAllowHackedItems(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.allowHackedItems)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleAllowHackedItems();
			else _config.setAllowHackedItems(i>0?true:false);
			rmp.sendMessage(RMText.allowHackedItems+": "+isTrueFalse(_config.getAllowHackedItems()));
		}
	}
	
	//Set Infinite Reward
	public void setInfiniteReward(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.infiniteReward)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleInfiniteReward();
			else _config.setInfiniteReward(i>0?true:false);
			rmp.sendMessage(RMText.infiniteReward+": "+isTrueFalse(_config.getInfiniteReward()));
		}
	}
	
	//Set Infinite Tools
	public void setInfiniteTools(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.infiniteTools)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			if(i==-1) _config.toggleInfiniteTools();
			else _config.setInfiniteTools(i>0?true:false);
			rmp.sendMessage(RMText.infiniteTools+": "+isTrueFalse(_config.getInfiniteTools()));
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
	
	//Get Text Min Players
	public String getTextMinPlayers(){
		return (_config.getMinPlayers()>0?(ChatColor.GREEN+""+_config.getMinPlayers()):(ChatColor.GRAY+"No limit"));
	}
	
	//Get Text Max Players
	public String getTextMaxPlayers(){
		return (_config.getMaxPlayers()>0?(ChatColor.GREEN+""+_config.getMaxPlayers()):(ChatColor.GRAY+"No limit"));
	}
	
	//Get Text Min Team Players
	public String getTextMinTeamPlayers(){
		return (_config.getMinTeamPlayers()>0?(ChatColor.GREEN+""+_config.getMinTeamPlayers()):(ChatColor.GRAY+"No limit"));
	}
	
	//Get Text Max Team Players
	public String getTextMaxTeamPlayers(){
		return (_config.getMaxTeamPlayers()>0?(ChatColor.GREEN+""+_config.getMaxTeamPlayers()):(ChatColor.GRAY+"No limit"));
	}
	
	//Get Text Time Limit
	public String getTextTimeLimit(){
		return (_config.getTimer().getTimeLimit()>0?(ChatColor.GREEN+""+_config.getTimer().getTextTime()):(ChatColor.GRAY+"No limit"));
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
		if(!rmp.hasPermission("resourcemadness.info")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		rmp.sendMessage(ChatColor.AQUA+RMText.firstLetterToUpperCase(_config.getWorldName())+ChatColor.WHITE+" Id: "+ChatColor.YELLOW+_config.getId()+ChatColor.WHITE+" "+"Owner: "+ChatColor.YELLOW+_config.getOwnerName()+ChatColor.WHITE+" TimeLimit: "+getTextTimeLimit());
		rmp.sendMessage("Players: "+ChatColor.GREEN+getTeamPlayers().length+ChatColor.WHITE+" inGame: "+getTextMinPlayers()+ChatColor.WHITE+"-"+getTextMaxPlayers()+ChatColor.WHITE+" inTeam: "+getTextMinTeamPlayers()+ChatColor.WHITE+"-"+getTextMaxTeamPlayers());
		rmp.sendMessage("Teams: "+getTextTeamPlayers());
	}
	
	public void sendTeamInfo(RMPlayer rmp){
		sendInfo(rmp);
	}
	
	public void sendMinMax(RMPlayer rmp){
		rmp.sendMessage("inGame: "+getTextMinPlayers()+ChatColor.WHITE+"-"+getTextMaxPlayers()+ChatColor.WHITE+" inTeam: "+getTextMinTeamPlayers()+ChatColor.WHITE+"-"+getTextMaxTeamPlayers());
	}
	
	public void getInfoFound(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.info.found")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			String items = RMText.getStringSortedItems(_config.getFound().getItems());
			if(items.length()>0){
				rmp.sendMessage(ChatColor.YELLOW+"Found items: "+items);
			}
			else rmp.sendMessage(ChatColor.YELLOW+"No found items.");
		}
	}
	
	public void sendSettings(RMPlayer rmp, int page){
		if(!rmp.hasPermission("resourcemadness.info.settings")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		int pageLimit = 2;
		if(page<=0) page = 1;
		if(page>pageLimit) page = pageLimit;
		rmp.sendMessage(ChatColor.GOLD+"/rm settings "+ChatColor.GRAY+"(Page "+page+" of "+pageLimit+")");
		if(page==1){
			rmp.sendMessage(ChatColor.YELLOW+"min "+getTextMinPlayers()+" "+ChatColor.WHITE+RMText.minPlayers+".");
			rmp.sendMessage(ChatColor.YELLOW+"max "+getTextMaxPlayers()+" "+ChatColor.WHITE+RMText.maxPlayers+".");
			rmp.sendMessage(ChatColor.YELLOW+"minteam "+getTextMinTeamPlayers()+" "+ChatColor.WHITE+RMText.minTeamPlayers+".");
			rmp.sendMessage(ChatColor.YELLOW+"maxteam "+getTextMaxTeamPlayers()+" "+ChatColor.WHITE+RMText.maxTeamPlayers+".");
			rmp.sendMessage(ChatColor.YELLOW+"timelimit "+getTextTimeLimit()+" "+ChatColor.WHITE+RMText.timeLimit+".");
			rmp.sendMessage(ChatColor.YELLOW+"random "+ChatColor.AQUA+getTextAutoRandomizeAmount()+" "+ChatColor.WHITE+"Randomly pick "+ChatColor.AQUA+"amount "+ChatColor.WHITE+"of items every match.");
			rmp.sendMessage(ChatColor.YELLOW+"advertise "+isTrueFalse(_config.getAdvertise())+" "+ChatColor.WHITE+RMText.advertise+".");
			rmp.sendMessage(ChatColor.YELLOW+"restore "+isTrueFalse(_config.getAutoRestoreWorld())+" "+ChatColor.WHITE+RMText.autoRestoreWorld+".");
			rmp.sendMessage(ChatColor.YELLOW+"warp "+isTrueFalse(_config.getWarpToSafety())+" "+ChatColor.WHITE+RMText.warpToSafety+".");
			rmp.sendMessage(ChatColor.YELLOW+"midgamejoin "+isTrueFalse(_config.getAllowMidgameJoin())+" "+ChatColor.WHITE+RMText.allowMidgameJoin+".");
			rmp.sendMessage(ChatColor.YELLOW+"healplayer "+isTrueFalse(_config.getHealPlayer())+" "+ChatColor.WHITE+RMText.healPlayer+".");
			rmp.sendMessage(ChatColor.YELLOW+"clearinventory "+isTrueFalse(_config.getClearPlayerInventory())+" "+ChatColor.WHITE+RMText.clearPlayerInventory+".");
			rmp.sendMessage(ChatColor.YELLOW+"warnunequal "+isTrueFalse(_config.getWarnUnequal())+" "+ChatColor.WHITE+RMText.warnUnequal+".");
			rmp.sendMessage(ChatColor.YELLOW+"allowunequal "+isTrueFalse(_config.getAllowUnequal())+" "+ChatColor.WHITE+RMText.allowUnequal+".");
		}
		else if(page==2){
			rmp.sendMessage(ChatColor.YELLOW+"warnhacked "+isTrueFalse(_config.getWarnHackedItems())+" "+ChatColor.WHITE+RMText.warnHackedItems+".");
			rmp.sendMessage(ChatColor.YELLOW+"allowhacked "+isTrueFalse(_config.getAllowHackedItems())+" "+ChatColor.WHITE+RMText.allowHackedItems+".");
			rmp.sendMessage(ChatColor.YELLOW+"infinitereward "+isTrueFalse(_config.getInfiniteReward())+" "+ChatColor.WHITE+RMText.infiniteReward+".");
			rmp.sendMessage(ChatColor.YELLOW+"infinitetools "+isTrueFalse(_config.getInfiniteTools())+" "+ChatColor.WHITE+RMText.infiniteTools+".");
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//STATIC FUNCTIONS////STATIC FUNCTIONS////STATIC FUNCTIONS////STATIC FUNCTIONS////STATIC FUNCTIONS//
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////
	//Request Player//
	//////////////////
	
	//Get Request Player
	public static RMPlayer getRequestPlayer(){
		return _requestPlayer;
	}
	
	//Set Request Player
	public static void setRequestPlayer(RMPlayer rmp){
		_requestPlayer = rmp;
	}
	
	//Clear Request Player
	public static void clearRequestPlayer(){
		_requestPlayer = null;
	}
	
	//////////
	//Player//
	//////////
	
	//Get All Players
	public static HashMap<String, RMPlayer> getAllPlayers(){
		HashMap<String, RMPlayer> players = new HashMap<String, RMPlayer>();
		for(RMGame game : _games.values()){
			players.putAll(game._config.getPlayers());
		}
		return players;
	}
	
	////////
	//Game//
	////////
	
	private static RMGame addGame(RMGame rmGame){
		int id = rmGame.getConfig().getId();
		if(!_games.containsKey(id)){
			_games.put(id, rmGame);
		}
		return rmGame;
	}
	public static void tryAddGameFromConfig(RMGameConfig config){
		RMGame rmGame = addGame(new RMGame(config, plugin));
		for(RMTeam rmt : rmGame.getConfig().getTeams()){
			rmt.setGame(rmGame);
		}
		rmGame.getConfig().correctMinMaxNumbers(MinMaxType.MIN_TEAM_PLAYERS);
		rmGame.updateSigns();
	}
	
	public static HandleState tryAddGame(Block b, RMPlayer rmp, Block bRemove){
		if(!rmp.hasPermission("resourcemadness.add")){
			rmp.sendMessage(RMText.noPermissionAction);
			return HandleState.NONE;
		}
		RMPartList partList;
		if(bRemove!=null) partList = new RMPartList(b, bRemove, plugin);
		else partList = new RMPartList(b, plugin);
		RMGame rmGame = getGameByBlock(partList.getMainBlock(b));
		
		Boolean wasModified = false;
		RMGameConfig rmGameConfig = new RMGameConfig(plugin);
		if(rmGame==null){
			//plugin.getServer().broadcastMessage("ISNULL!");
			rmGame = RMGame.getGameByBlock(b);
		}
		if(rmGame!=null){
			if(!rmp.hasOwnerPermission(rmGame._config.getOwnerName())){
				rmp.sendMessage("The owner is "+rmGame._config.getOwnerName()+".");
				return HandleState.NO_CHANGE;
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
					return HandleState.NO_CHANGE;
				}
				rmp.sendMessage("Game id "+rmGame._config.getId()+" already exists!");
				return HandleState.NO_CHANGE;
			}
		}
		
		if(partList.getStoneList().size()<2){
			rmp.sendMessage("You're missing "+(2-partList.getStoneList().size())+" stone block." );
			return HandleState.NONE;
		}
	
		List<RMTeam> teams = partList.fetchTeams();
		if(teams.size()<2){
			rmp.sendMessage("You need at least "+ChatColor.YELLOW+"two teams "+ChatColor.WHITE+"to create a game.");
			return HandleState.NONE;
		}
		
		if(plugin.config.getMaxGamesPerPlayer()>0){
			if(getGamesByOwner(rmp).size()>=plugin.config.getMaxGamesPerPlayer()){
				rmp.sendMessage(ChatColor.GRAY+"You can't create any more games.");
				return HandleState.NONE;
			}
		}
		rmGame = addGame(new RMGame(partList, rmp, plugin));
		for(RMTeam rmt : teams){
			rmGame.addTeam(rmt);
		}
		if(wasModified){
			RMGameConfig config = rmGame.getConfig();
			config.getDataFrom(rmGameConfig);
			rmGameConfig = null;
			rmp.sendMessage("Game id "+ChatColor.YELLOW+rmGame._config.getId()+ChatColor.WHITE+" has been "+ChatColor.YELLOW+"modified"+ChatColor.WHITE+".");
		}
		else{
			rmGame.getConfig().getDataFrom(plugin.getConfig());
			rmp.sendMessage("Game id "+ChatColor.YELLOW+rmGame._config.getId()+ChatColor.WHITE+" has been "+ChatColor.YELLOW+"created"+ChatColor.WHITE+".");
		}
		rmp.sendMessage("Found "+ChatColor.YELLOW+teams.size()+ChatColor.WHITE+" teams. ("+rmGame.getTextTeamColors()+")");
		
		//Correct min/max numbers
		rmGame.getConfig().correctMinMaxNumbers(MinMaxType.MIN_TEAM_PLAYERS);
		
		rmGame.updateSigns();
		
		if(wasModified) return HandleState.MODIFY;
		else return HandleState.ADD;
	}
	
	private static Boolean removeGame(RMGame rmGame){
		int id = rmGame.getConfig().getId();
		if(_games.containsKey(id)){
			//rmGame._config.getOwner().getGames().remove(rmGame);
			RMGame.getGamesByOwnerName(rmGame.getConfig().getOwnerName()).remove(rmGame);
			for(RMTeam rmt : rmGame.getTeams()){
				rmt.setNull();
			}
			_games.remove(id);
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
		if(!rmp.hasPermission("resourcemadness.remove")){
			rmp.sendMessage(RMText.noPermissionAction);
			return HandleState.NO_CHANGE;
		}
		RMGame rmGame = getGameByBlock(b);
		if(rmGame!=null){
			if(rmGame.getConfig().getState() == GameState.SETUP){
				if(rmp.hasOwnerPermission(rmGame._config.getOwnerName())){
					if((RMHelper.isMaterial(b.getType(), Material.CHEST, Material.WALL_SIGN, Material.WOOL))&&(!justRemove)){
						List<Block> blocks = rmGame._config.getPartList().getList();
						//plugin.getServer().broadcastMessage("JUSTREMOVE");
						for(Block block : blocks){
							if(rmGame.getMainBlock() == block){
								//plugin.getServer().broadcastMessage("MAINBLOCK");
								HandleState handleState = tryAddGame(rmGame.getMainBlock(), rmp, b);
								switch(handleState){
									case NONE:
										rmp.sendMessage("Successfully "+ChatColor.GRAY+"removed "+ChatColor.WHITE+"game with id "+ChatColor.YELLOW+rmGame._config.getId()+ChatColor.WHITE+".");
										for(Sign sign : rmGame.getSigns()){
											sign.setLine(0, "");
											sign.setLine(1, "");
											sign.setLine(2, "");
											sign.setLine(3, "");
											sign.update();
										}
									default: return handleState;
								}
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
					rmp.sendMessage("Successfully "+ChatColor.GRAY+"removed "+ChatColor.WHITE+"game with id "+ChatColor.YELLOW+rmGame._config.getId()+ChatColor.WHITE+".");
					removeGame(rmGame);
					return HandleState.REMOVE;
				}
				else{
					rmp.sendMessage("The owner is "+rmGame._config.getOwnerName()+".");
					return HandleState.NO_CHANGE;
				}
			}
			return HandleState.NO_CHANGE;
		}
		//plugin.getServer().broadcastMessage("NOGAME");
		return HandleState.NONE;
	}
	
	public static Material[] getMaterials(){
		return _materials;
	}
	
	//Get Game by Id (String)
	public static RMGame getGameById(String arg){
		int id = RMHelper.getIntByString(arg);
		if(id!=-1) return RMGame.getGame(id);
		return null;
	}
	
	public static RMGame getGame(int id){
		if((id>=0)&&(id<_games.size())) return _games.get(id);
		else return null;
	}
	public static HashMap<Integer, RMGame> getGames(){
		return _games;
	}
	public static RMGame getGameByBlock(Block b){
		for(RMGame game : _games.values()){
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
		for(RMGame game : _games.values()){
			if(rmp.hasOwnerPermission(game._config.getOwnerName())) games.add(game); 
		}
		return games;
	}
	private static List<RMGame> getGamesByOwnerName(String name){
		List<RMGame> games = new ArrayList<RMGame>();
		for(RMGame game : RMGame.getGames().values()){
			if(game._config.getOwnerName().equalsIgnoreCase(name)) games.add(game); 
		}
		return games;
	}
	
	//Get Game by Id
	public static RMGame getGameById(int arg){
		return RMGame.getGame(arg);
	}
	
	////////
	//Team//
	////////
	
	//Get Team
	public static RMTeam getTeam(RMGame rmGame, int index){
		if(rmGame._config.getTeams().size()>index) return rmGame._config.getTeams().get(index);
		return null;
	}
	
	//Get Teams
	public static List<RMTeam> getTeams(RMGame rmGame){
		return rmGame._config.getTeams();
	}
	
	//Get All Teams
	public static List<RMTeam> getAllTeams(){
		List<RMTeam> teams = new ArrayList<RMTeam>();
		for(RMGame game : _games.values()){
			teams.addAll(game._config.getTeams());
		}
		return teams;
	}
	
	//Get TeamById
	public static RMTeam getTeamById(String arg, RMGame rmGame){
		int id = RMHelper.getIntByString(arg);
		if(id!=-1) return rmGame.getTeam(id);
		return null;
	}
	
	/////////
	//Chest//
	/////////
	
	//Get Chests from Block List
	public static List<RMChest> getChestsFromBlockList(List<List<Block>> bList, RM plugin){
		List<List<Block>> blockList = bList.subList(2, bList.size());
		List<RMChest> chestList = new ArrayList<RMChest>();
		for(List<Block> blocks : blockList){
			chestList.add(new RMChest((Chest)blocks.get(Part.CHEST.ordinal()-2).getState(), plugin));
		}
		if(chestList.size()>0) return chestList;
		else return null;
	}
}