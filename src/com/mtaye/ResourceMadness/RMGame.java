package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMConfig.Lock;
import com.mtaye.ResourceMadness.Helper.RMHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMGame {

	private static List<RMGame> _games = new ArrayList<RMGame>();
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
	public static enum GameState { SETUP, COUNTDOWN, GAMEPLAY, GAMEOVER; }
	public static enum InterfaceState { FILTER, REWARD, TOOLS, FILTER_CLEAR, REWARD_CLEAR, TOOLS_CLEAR };
	public static enum FilterType { ALL, CLEAR, BLOCK, ITEM, RAW, CRAFTED};
	public static enum ClickState { LEFT, RIGHT, NONE };
	public static enum ItemHandleState { ADD, MODIFY, REMOVE, NONE };
	public static enum HandleState { ADD, MODIFY, REMOVE, NO_CHANGE, CLAIM_RETURNED_ALL, CLAIM_RETURNED_SOME, NONE };
	public static enum ForceState { ADD, REMOVE, RANDOMIZE, NONE};
	public static enum FilterState { FILTER, FOUND, REWARD, TOOLS, ITEMS, NONE };
	
	private final int cdTimerLimit = 30; //3 seconds
	private int cdTimer = cdTimerLimit;
	
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
	
	public void clearFilter(RMPlayer rmp){
		_config.getFilter().clearItems();
		rmp.sendMessage(ChatColor.GRAY+"Filter cleared.");
	}
	
	//Mode
	public void cycleMode(RMPlayer rmp){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
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
	}
	
	public boolean changeMode(InterfaceState interfaceState, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.mode")){
			rmp.sendMessage(RMText.noPermissionAction);
			return false;
		}
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
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			_config.setInterface(interfaceState);
			rmp.sendMessage("Interface mode changed to "+ChatColor.YELLOW+interfaceState.name());
			updateSigns();
			return true;
		}
		return false;
	}	
	
	//Stash
	public void clearReward(RMPlayer rmp){
		if(rmp.claim(_config.getReward(), ClaimType.REWARD) == HandleState.CLAIM_RETURNED_ALL) rmp.sendMessage(ChatColor.GRAY+"Reward cleared.");
	}
	public void clearTools(RMPlayer rmp){
		if(rmp.claim(_config.getTools(), ClaimType.TOOLS) == HandleState.CLAIM_RETURNED_ALL) rmp.sendMessage(ChatColor.GRAY+"Tools cleared.");
	}
	public void clearFound(RMPlayer rmp){
		if(rmp.claim(_config.getFound(), ClaimType.FOUND) == HandleState.CLAIM_RETURNED_ALL) rmp.sendMessage(ChatColor.GRAY+"Found items cleared.");
	}

	
	public void tryAddRewardFromChest(Block b, RMPlayer rmp, ClickState clickState){
		if(!rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		tryAddFromChest(b, rmp, ClaimType.REWARD, clickState);
	}
	public void tryAddToolsFromChest(Block b, RMPlayer rmp, ClickState clickState){
		if(!rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		tryAddFromChest(b, rmp, ClaimType.TOOLS, clickState);
	}
	public void tryAddFromChest(Block b, RMPlayer rmp, ClaimType claimType, ClickState clickState){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			RMChest rmChest = getChestByBlock(b);
			Inventory inv = rmChest.getChest().getInventory();
			ItemStack[] contents = inv.getContents();
			HashMap<Integer, RMItem> added = new HashMap<Integer, RMItem>();
			HashMap<Integer, RMItem> hacked = new HashMap<Integer, RMItem>();

			for(int i=0; i<contents.length; i++){
				ItemStack item = contents[i];
				if(item!=null){
					int id = item.getTypeId();
					if(RMHelper.isMaterial(item.getType(), _hackMaterials)){
						if(!_config.getAllowHackedItems()){
							if(hacked.containsKey(id)){
								ItemStack itemClone = item.clone();
								itemClone.setAmount(hacked.get(id).getAmount()+item.getAmount());
								hacked.put(id, new RMItem(itemClone));
							}
							else hacked.put(id, new RMItem(item));
							continue;
						}
					}
					switch(addItem(item, claimType)){
						case ADD: case MODIFY:
							if(added.containsKey(id)){
								ItemStack itemClone = item.clone();
								itemClone.setAmount(added.get(id).getAmount()+item.getAmount());
								added.put(id, new RMItem(itemClone));
							}
							else added.put(id, new RMItem(item));
							inv.clear(i);
							break;
					}
				}
			}
			
			if(_config.getWarnHackedItems()) if(hacked.size()!=0) warnHackMaterials(hacked);
			if(added.size()!=0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+getSimpleFormattedStringByHash(added));
			else if(hacked.size()==0){
				if(rmp.getPlayer().isSneaking()){
					if(rmp.hasPermission("resourcemadness.reward.byhand")){
						tryAddItem(rmp, claimType, clickState);
					}
					else rmp.sendMessage(RMText.noPermissionAction);
				}
				else{
					switch(claimType){
					case REWARD:
						if(_config.getReward().size()!=0){
							if(rmp.hasPermission("resourcemadness.reward.clear")){
								rmp.sendMessage("Click sign to clear all rewards.");
								_config.setInterface(InterfaceState.REWARD_CLEAR);
							}
							else rmp.sendMessage(RMText.noPermissionAction);
						}
						break;
					case TOOLS:
						if(_config.getTools().size()!=0){
							if(rmp.hasPermission("resourcemadness.tools.clear")){
								rmp.sendMessage("Click sign to clear all tools.");
								_config.setInterface(InterfaceState.TOOLS_CLEAR);
							}
							else rmp.sendMessage(RMText.noPermissionAction);
						}
						break;
					}
				}
			}
			updateSigns();
		}
	}
	
	public void tryAddItem(RMPlayer rmp, ClaimType claimType, ClickState clickState){
		/*
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			ItemStack item = rmp.getPlayer().getItemInHand();
			Inventory inv = rmp.getPlayer().getInventory();
			ItemStack[] contents = inv.getContents();
			if(item!=null){
				int id = item.getTypeId();
				Material mat = item.getType();
				if(!isMaterial(mat, _hackMaterials)){
					switch(clickState){
					case NONE: case LEFT:
						switch(addItem(item, claimType)){
							case ADD:
								for(int i=0; i<contents.length; i++){
									ItemStack invItem = contents[i];
									if(invItem!=null){
										if(item==invItem){
											inv.clear(i);
										}
									}
								}
								rmp.sendMessage(ChatColor.YELLOW+"Added: "+ChatColor.WHITE+mat.name()+includeItem(new RMItem(item))); break;
							case MODIFY:
								for(int i=0; i<contents.length; i++){
									ItemStack invItem = contents[i];
									if(invItem!=null){
										if(item==invItem){
											inv.clear(i);
										}
									}
								}
								rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+includeItem(new RMItem(item))); break;
						}
						break;
					case RIGHT:
						switch(claimType){
							case REWARD:
								switch(rmp.claimItem(_config.getReward(), item, claimType)){
									case MODIFY:
										rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+includeItem(new RMItem(item))); break;
									case REMOVE:
										rmp.sendMessage(ChatColor.GRAY+"Removed: "+ChatColor.WHITE+mat.name()+includeItem(new RMItem(item))); break;
								}
								break;
							case TOOLS:
								switch(rmp.claimItem(_config.getTools(), item, claimType)){
									case MODIFY:
										rmp.sendMessage(ChatColor.YELLOW+"Modified: "+ChatColor.WHITE+mat.name()+includeItem(new RMItem(item))); break;
									case REMOVE:
										rmp.sendMessage(ChatColor.GRAY+"Removed: "+ChatColor.WHITE+mat.name()+includeItem(new RMItem(item))); break;
								}
								break;
						}
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
		*/
	}
	
	public void tryAddRewardFromChest(RMChest rmChest){
		tryAddFromChest(rmChest, ClaimType.REWARD);
	}
	public void tryAddToolsFromChest(RMChest rmChest){
		tryAddFromChest(rmChest, ClaimType.TOOLS);
	}
	public void tryAddFromChest(RMChest rmChest, ClaimType claimType){
		Inventory inv = rmChest.getChest().getInventory();
		ItemStack[] contents = inv.getContents();
		
		for(int i=0; i<contents.length; i++){
			ItemStack item = contents[i];
			if(item!=null){
				addItem(item, claimType);
				inv.clear(i);
			}
		}
	}
	
	public void tryAddFromChest(RMChest rmChest, List<ItemStack> items){
		Inventory inv = rmChest.getChest().getInventory();
		ItemStack[] contents = inv.getContents();
		
		for(int i=0; i<contents.length; i++){
			ItemStack item = contents[i];
			if(item!=null){
				addItem(item, items);
				inv.clear(i);
			}
		}
	}
	
	public void tryAddFromHashMap(HashMap<Integer, RMItem> rmItems, List<ItemStack> items){
		for(RMItem rmItem : rmItems.values()){
			ItemStack item = rmItem.getItem();
			if(item!=null){
				addItem(item, items);
			}
		}
	}

	public HandleState addItem(ItemStack item, ClaimType claimType){
		if((item!=null)&&(item.getType()!=Material.AIR)){
			List<ItemStack> items = new ArrayList<ItemStack>();
			switch(claimType){
				case FOUND: items = _config.getFound(); break;
				case REWARD: items = _config.getReward(); break;
				case TOOLS: items = _config.getTools(); break;
			}
			return addItem(item, items);
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
	
	public HandleState removeItem(ItemStack item, ClaimType claimType){
		if((item!=null)&&(item.getType()!=Material.AIR)){
			List<ItemStack> items = new ArrayList<ItemStack>();
			switch(claimType){
				case REWARD: items = _config.getReward(); break;
				case TOOLS: items = _config.getTools(); break;
			}
			for(ItemStack isItem : items){
				if(isItem.getType() == item.getType()){
					if(item.getAmount()>isItem.getAmount()){
						return HandleState.REMOVE;
					}
					else return HandleState.MODIFY;
				}
			}
		}
		return HandleState.NO_CHANGE;
	}
	
	public void claimFound(RMPlayer rmp){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			rmp.claim(_config.getFound(), ClaimType.FOUND);
		}
		else rmp.sendMessage("Only the owner can claim the items found.");
	}
	
	public List<String> checkRewardEqual(RMPlayer rmp){
		return checkEqualDistribution(rmp, ClaimType.REWARD);
	}
	public List<String> checkToolsEqual(RMPlayer rmp){
		return checkEqualDistribution(rmp, ClaimType.TOOLS);
	}
	
	public List<String> checkEqualDistribution(RMPlayer rmp, ClaimType claimType){
		List<String> strUnequal = new ArrayList<String>();
		List<ItemStack> items = new ArrayList<ItemStack>();
		switch(claimType){
			case REWARD: items = _config.getReward(); break;
			case TOOLS: items = _config.getTools(); break;
		}
		HashMap<Integer, Integer> hashItems = new HashMap<Integer, Integer>();
		if(items.size()>0){
			for(ItemStack is : items){
				ItemStack item = is.clone();
				int id = item.getTypeId();
				Material mat = item.getType();
				while(item.getAmount()>mat.getMaxStackSize()){
					if(hashItems.containsKey(id)){
						hashItems.put(id, hashItems.get(id)+mat.getMaxStackSize());
					}
					else hashItems.put(id, mat.getMaxStackSize());
					item.setAmount(item.getAmount()-mat.getMaxStackSize());
				}
				if(item.getAmount()<=mat.getMaxStackSize()){
					if(hashItems.containsKey(id)){
						hashItems.put(id, hashItems.get(id)+item.getAmount());
					}
					else hashItems.put(id, item.getAmount());
				}
			}
			for(RMTeam rmTeam : _config.getTeams()){
				if(rmTeam!=null){
					int numOfPlayers = rmTeam.getPlayers().length;
					if(numOfPlayers>0){
						String strItems = "";
						for(Integer id : hashItems.keySet()){
							int amount = hashItems.get(id);
							//plugin.log.log(Level.WARNING, "numOfPlayers:"+numOfPlayers);
							//plugin.log.log(Level.WARNING, "amount:"+amount);
							//plugin.log.log(Level.WARNING, "amount%numOfPlayers:"+(amount%numOfPlayers));
							if(amount%numOfPlayers!=0){
								strItems+=Material.getMaterial(id).name()+":"+ChatColor.GRAY+amount+", ";
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
	public void distributeReward(RMTeam rmTeam){
		distributeItems(rmTeam, ClaimType.REWARD);
	}
	public void distributeTools(RMTeam rmTeam){
		distributeItems(rmTeam, ClaimType.TOOLS);
	}
	public void distributeItems(RMTeam rmTeam, ClaimType claimType){
		List<ItemStack> items = new ArrayList<ItemStack>();
		switch(claimType){
			case REWARD:
				if(_config.getInfiniteReward()){
					//plugin.log.log(Level.WARNING, "TRUE");
					items = cloneListItemStack(_config.getReward());
				}
				else items = _config.getReward();
				break;
			case TOOLS:
				if(_config.getInfiniteTools()) items = cloneListItemStack(_config.getTools());
				else items = _config.getTools();
				break;
		}
		if(items==null) return;
		if(rmTeam!=null) distributeItemsToTeamDivide(rmTeam, items, claimType);
		else distributeItemsToTeamsDivide(items,claimType);
	}
	
	public void distributeItemsToTeamDivide(RMTeam rmTeam, List<ItemStack> items, ClaimType claimType){
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			rmp.addByListItemStack(distributeByDivisor(items, divisor), claimType);
			divisor--;
		}
	}
	public void distributeItemsToTeamsDivide(List<ItemStack> items, ClaimType claimType){
		RMPlayer[] players = getTeamPlayers();
		int divisor = players.length;
		for(RMPlayer rmp : players){
			rmp.addByListItemStack(distributeByDivisor(items, divisor), claimType);
			divisor--;
		}
	}
	
	public void distributeFromChests(RMTeam rmTeam, ClaimType claimType){
		if(rmTeam==null) return;
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(RMChest rmChest : getChests()){
			tryAddFromChest(rmChest, items);
			rmChest.getChest().getInventory().clear();
		}
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			rmp.addByListItemStack(distributeByDivisor(items, divisor), claimType);
			divisor--;
		}
	}
	public void distributeFromChest(RMChest rmChest, ClaimType claimType){
		RMTeam rmTeam = rmChest.getTeam();
		if(rmTeam==null) return;
		List<ItemStack> items = new ArrayList<ItemStack>();
		tryAddFromChest(rmChest, items);
		rmChest.getChest().getInventory().clear();
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			rmp.addByListItemStack(distributeByDivisor(items, divisor), claimType);
			divisor--;
		}
	}
	
	public List<ItemStack> distributeByDivisor(List<ItemStack> items, int divisor){
		List<ItemStack> foundItems = new ArrayList<ItemStack>();
		List<ItemStack> removeItems = new ArrayList<ItemStack>();
		
		for(ItemStack item : items){
			int amount = (int)Math.ceil(item.getAmount()/divisor);
			if(amount!=0){
				ItemStack itemClone = item.clone();
				itemClone.setAmount(amount);
				foundItems.add(itemClone);
				item.setAmount(item.getAmount()-amount);
			}
			if(item.getAmount()==0) removeItems.add(item);
		}
		for(ItemStack item : removeItems){
			items.remove(item);
		}
		return foundItems;
	}
	
	public List<ItemStack> cloneListItemStack(List<ItemStack> items){
		List<ItemStack> clonedItems = new ArrayList<ItemStack>();
		for(ItemStack item : items){
			clonedItems.add(item.clone());
		}
		return clonedItems;
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
	
	public RMFilter getItems(){
		return _config.getItems();
	}
	
	public void stashChestsContents(){
		for(RMChest rmChest : getChests()){
			rmChest.addContentsToInventory();
		}
	}
	public void returnChestsContents(){
		for(RMChest rmChest : getChests()){
			rmChest.returnContentsFromInventory();
		}
	}
	
	public void startGame(RMPlayer rmp){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(_config.getState()!=GameState.SETUP){
				rmp.sendMessage("Please use restart game instead.");
				return;
			}
			for(RMChest rmChest : getChests()){
				rmChest.clearItems();
			}
			//Populate items by filter
			_config.getItems().populateByFilter(_config.getFilter());
			
			//Randomize
			if(_config.getRandomizeAmount()>0) _config.getItems().randomize(_config.getRandomizeAmount());
			else _config.getItems().randomize(_config.getAutoRandomizeAmount());
			
			//Filter is empty
			if(_config.getItems().size()==0){
				rmp.sendMessage("Configure the "+ChatColor.YELLOW+"filtered items"+ChatColor.WHITE+" first.");
				return;
			}
			String rewardNotEqual = RMText.getStringByStringList(checkRewardEqual(rmp), ", ");
			String toolsNotEqual = RMText.getStringByStringList(checkToolsEqual(rmp), ", ");
			//Warn unequal
			if((_config.getWarnUnequal())||(!_config.getAllowUnequal())){
				if(rewardNotEqual.length()!=0) rmp.sendMessage("Unequal Reward distribution for: "+rewardNotEqual);
				if(toolsNotEqual.length()!=0) rmp.sendMessage("Unequal tools distribution for: "+toolsNotEqual);
			}
			//Allow unequal
			if(!_config.getAllowUnequal()){
				boolean itemsNotEqual = false;
				if(rewardNotEqual.length()!=0){
					rmp.sendMessage(ChatColor.RED+"Reward cannot be distributed equally!");
					itemsNotEqual = true;
				}
				if(toolsNotEqual.length()!=0){
					rmp.sendMessage(ChatColor.RED+"Tools cannot be distributed equally!");
					itemsNotEqual = true;
				}
				if(itemsNotEqual){
					rmp.sendMessage(ChatColor.GRAY+"Canceled");
					return;
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
			/*
			//Check for mininum number of players in each team
			for(RMTeam rmTeam : _config.getTeams()){
				if(rmTeam.getPlayers().length<_config.getMinTeamPlayers()){
					//rmp.sendMessage("Each team must have at least one player.");
					broadcastMessage("Each team must have at least "+_config.getMinTeamPlayers()+" player(s).");
					return;
				}
			}
			//Check for minimum number of players in game
			int minPlayers = _config.getMinPlayers();
			if(minPlayers<_config.getTeams().size()) minPlayers = _config.getTeams().size(); 
			if(getTeamPlayers().length<minPlayers){
				broadcastMessage("This match is set up for at least "+_config.getMinPlayers()+" player(s).");
				return;
			}
			*/
			stashChestsContents();
			//Clear player's inventory
			if(_config.getClearPlayerInventory()) snatchInventories();
			updateSigns();
			
			//rmp.sendMessage("Starting game...");
			broadcastMessage("Starting game...");
			_config.setState(GameState.COUNTDOWN);
		}
		else rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can start the game.");
	}
	public void restartGame(RMPlayer rmp){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			switch(_config.getState()){
				case GAMEPLAY:
					//rmp.sendMessage("Restarting game...");
					broadcastMessage("Restarting game...");
					stopGame(rmp, false);
					startGame(rmp);
					return;
				case SETUP:
					rmp.sendMessage("No game in progress.");
					return;
				case COUNTDOWN:
					rmp.sendMessage("Please wait for the game to start.");
					return;
			}
		}
		else rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can restart the game.");
	}
	public void stopGame(RMPlayer rmp, boolean clearRandom){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			switch(_config.getState()){
				case GAMEPLAY:
					//rmp.sendMessage("Stopping game...");
					broadcastMessage("Stopping game...");
					initGameOver(clearRandom);
					return;
				case SETUP:
					rmp.sendMessage("No game in progress.");
					return;
				case COUNTDOWN:
					rmp.sendMessage("Please wait for the game to start.");
					return;
			}
		}
		else rmp.sendMessage("Only the owner "+_config.getOwnerName()+" can stop the game.");
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
	
	public void initGameOver(boolean clearRandom){
		if(clearRandom) _config.clearRandomizeAmount();
		for(RMChest rmChest : getChests()){
			tryAddFromHashMap(rmChest.getItems(), _config.getFound());
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
				tryAddFromHashMap(rmChest.getItems(), _config.getFound());
				rmChest.clearItems();
				distributeFromChest(rmChest, ClaimType.ITEMS);
			}
			for(RMTeam rmt : getTeams()){
				if(rmt!=rmTeam){
					for(RMPlayer rmPlayer : rmt.getPlayers()){
						rmPlayer.claimTransfer(ClaimType.TOOLS, ClaimType.ITEMS);
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
				rmPlayer.claimTransfer(ClaimType.TOOLS, ClaimType.ITEMS);
				rmPlayer.getStats().addWins();
				rmPlayer.getStats().addTimesPlayed();
				_config.getGameStats().addWins();
				_config.getGameStats().addTimesPlayed();
				RMStats.addServerWins();
				RMStats.addServerTimesPlayed();
			}
			distributeReward(rmTeam);
			//setWinPlayer(rmp);
			update();
			initGameOver(true);
		}
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
	
	public void snatchInventories(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				if(rmp.getPlayer()!=null){
					rmp.addItems(ClaimType.ITEMS);
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
	
	public void clearTeamPlayers(){
		for(RMTeam rmTeam : getTeams()){
			rmTeam.clearPlayers();
		}
	}
	
	//UPDATE
	public void update(){
		switch(_config.getState()){
		case SETUP:
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
				broadcastMessage(ChatColor.GOLD+RMText.gStartMatch);
				cdTimer = cdTimerLimit;
				_config.setState(GameState.GAMEPLAY);
				for(RMTeam rmt : getTeams()){
					for(RMPlayer rmp : rmt.getPlayers()){
						rmp.claimTransfer(ClaimType.TOOLS, ClaimType.ITEMS);
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
						sign.setLine(1, getStringTotal(lineTotal));
						sign.setLine(2, "Joined: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());
						sign.setLine(3, "Total: "+getTeamPlayers().length+getTextPlayersOfMax());
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
				case REWARD:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Reward: "+_config.getReward().size());//+items);
						sign.setLine(1, getStringTotal(""+plugin.getListItemStackTotal(_config.getReward())));//lineTotal);
						sign.setLine(2, "Joined: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());
						sign.setLine(3, "Total: "+getTeamPlayers().length+getTextPlayersOfMax());
						sign.update();
					}
					break;
				case TOOLS:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, "Tools: "+_config.getTools().size());//+items);
						sign.setLine(1, getStringTotal(""+plugin.getListItemStackTotal(_config.getTools())));//lineTotal);
						sign.setLine(2, "Joined: "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax());
						sign.setLine(3, "Total: "+getTeamPlayers().length+getTextPlayersOfMax());
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
	
	public void warpPlayersToSafety(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				rmp.warpToSafety();
			}
		}
	}
	
	public String getStringTotal(String str){
		int length = str.length();
		if(length<9) str = "Total: "+str;
		else if(length<11) str = "Ttl: "+str;
		else if(length<13) str = "T: "+str;
		return str;
	}
	
	public void trySignSetupInfo(RMPlayer rmp){
		updateSigns();
		String items = "";
		
		//Sort
		Integer[] array = _config.getFilter().keySet().toArray(new Integer[_config.getFilter().keySet().size()]);
		Arrays.sort(array);
		
		if(array.length>plugin.config.getTypeLimit()){
			for(Integer i : array){
				items += ChatColor.WHITE+""+i+RMText.includeItem(_config.getFilter().getItem(i))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer i : array){
				items += ChatColor.WHITE+""+Material.getMaterial(i)+RMText.includeItem(_config.getFilter().getItem(i))+ChatColor.WHITE+", ";
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
		}
	}
	
	public void tryGetTeamInfo(RMPlayer rmp){
		rmp.sendMessage("Teams: "+getTextTeamPlayers());
	}
	
	public void trySignSetupRewardInfo(RMPlayer rmp){
		//plugin.log.log(Level.WARNING, "isEqual:"+checkEqualDistribution(rmp, ClaimType.REWARD));
		updateSigns();
		String items = RMText.getSortedItemsFromItemStackArray(_config.getRewardArray());
		if(items.length()>0){
			rmp.sendMessage(ChatColor.YELLOW+"Reward: "+items);
		}
		else rmp.sendMessage(ChatColor.YELLOW+"No Reward added.");
	}
	
	public void trySignSetupToolsInfo(RMPlayer rmp){
		//plugin.log.log(Level.WARNING, "isEqual:"+checkEqualDistribution(rmp, ClaimType.TOOLS));
		updateSigns();
		String items = RMText.getSortedItemsFromItemStackArray(_config.getToolsArray());
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
		
		HashMap<Integer, RMItem> items = rmChest.getItems();
		
		for(Integer i : array){
			RMItem rmItem = _config.getItems().getItem(i);
			int amount = rmItem.getAmount();
			if(items.containsKey(i)) amount -= items.get(i).getAmount();
			if(amount>0) strItems += ChatColor.WHITE+""+Material.getMaterial(i)+RMText.includeItem(new RMItem(i, amount))+ChatColor.WHITE+", ";
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
		if(_config.getWarnHackedItems()) warnHackMaterialsListItemStack(items);
		if(!_config.getAllowHackedItems()) items = removeHackMaterialsListItemStack(items);
		return items;
	}
	
	//Try Add Items
	public void tryAddItemsToFilter(Block b, RMPlayer rmp, ClickState clickState){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
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
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
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
			_config.getGameStats().addItemsFoundTotal(totalFound);
			RMStats.addServerItemsFoundTotal(totalFound);
			if(added.size()>0){
				if(returned.size()>0){
					teamBroadcastMessage(ChatColor.YELLOW+"Items left: "+getFormattedStringByHash(returned, rmp));
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
							if(rmp.getPlayer().isSneaking()) tryAddRewardFromChest(b, rmp, ClickState.RIGHT);
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
							if(rmp.getPlayer().isSneaking()) tryAddToolsFromChest(b, rmp, ClickState.RIGHT);
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
						else tryGetTeamInfo(rmp);
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
						if(rmp.getPlayer().isSneaking()) tryAddRewardFromChest(b, rmp, ClickState.LEFT);
						else tryAddRewardFromChest(b, rmp, ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.getPlayer().isSneaking()) cycleMode(rmp);
						else trySignSetupRewardInfo(rmp);
						break;
					case WOOL:
						if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else tryGetTeamInfo(rmp);
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
						if(rmp.getPlayer().isSneaking()) tryAddToolsFromChest(b, rmp, ClickState.LEFT);
						else tryAddToolsFromChest(b, rmp, ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.getPlayer().isSneaking()) cycleMode(rmp);
						else trySignSetupToolsInfo(rmp);
						break;
					case WOOL:
						if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else tryGetTeamInfo(rmp);
						break;
					}
					break;
				case FILTER_CLEAR: case REWARD_CLEAR: case TOOLS_CLEAR: //FILTER CLEAR
					switch(mat){
					case CHEST: case GLASS: case STONE:
						if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
							rmp.sendMessage(ChatColor.GRAY+"Canceled.");
							_config.setInterface(getParentInterface(_config.getInterface()));
							updateSigns();
						}
						break;
					case WALL_SIGN:
						if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
							switch(_config.getInterface()){
								case FILTER_CLEAR:
									clearFilter(rmp);
									break;
								case REWARD_CLEAR:
									clearReward(rmp);
									break;
								case TOOLS_CLEAR:
									clearTools(rmp);
									break;
							}
							_config.setInterface(getParentInterface(_config.getInterface()));
							updateSigns();
						}
						break;
					case WOOL:
						if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
							rmp.sendMessage(ChatColor.GRAY+"Canceled.");
							_config.setInterface(getParentInterface(_config.getInterface()));
							updateSigns();
						}
						else{
							if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
							else tryGetTeamInfo(rmp);
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
					if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
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
					else tryGetTeamInfo(rmp);
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
		rmGame.correctMinMaxNumbers();
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
			if((!rmp.getName().equalsIgnoreCase(rmGame._config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
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
			rmp.sendMessage("You need at least two teams to create a game.");
			return HandleState.NONE;
		}
		
		if(plugin.config.getMaxGamesPerPlayer()>0){
			if(getGamesByOwner(rmp).size()>=plugin.config.getMaxGamesPerPlayer()){
				rmp.sendMessage("You can't create any more games.");
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
			rmp.sendMessage("Game id "+rmGame._config.getId()+" has been modified.");
		}
		else rmp.sendMessage("Game id "+rmGame._config.getId()+" has been created.");
		rmp.sendMessage("Found "+teams.size()+" teams. ("+rmGame.getTextTeamColors()+")");
		
		//Correct min/max numbers
		rmGame.correctMinMaxNumbers();
		
		rmGame.updateSigns();
		
		if(wasModified) return HandleState.MODIFY;
		else return HandleState.ADD;
		
	}
	
	public void correctMinMaxNumbers(){
		int teamSize = getTeams().size();
		if(_config.getMinPlayers()<teamSize) _config.setMinPlayers(teamSize);
		if((_config.getMaxPlayers()!=0)&&(_config.getMaxPlayers()<teamSize)) _config.setMaxPlayers(teamSize);
		//if(_config.getMinTeamPlayers()<1) _config.setMinTeamPlayers(1);
		if((_config.getMaxTeamPlayers()!=0)&&(_config.getMaxTeamPlayers()<teamSize)) _config.setMaxTeamPlayers(teamSize);
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
		if(!rmp.hasPermission("resourcemadness.remove")){
			rmp.sendMessage(RMText.noPermissionAction);
			return HandleState.NONE;
		}
		RMGame rmGame = getGameByBlock(b);
		if(rmGame!=null){
			if(rmGame.getConfig().getState() == GameState.SETUP){
				if((rmp.getName().equalsIgnoreCase(rmGame._config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
					if((RMHelper.isMaterial(b.getType(), Material.CHEST, Material.WALL_SIGN, Material.WOOL))&&(!justRemove)){
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
					return HandleState.NO_CHANGE;
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
			if((rmp.getName().equalsIgnoreCase(game._config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))) games.add(game); 
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
		else rmp.sendMessage("This team does not exist!");
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
			else rmp.sendMessage("This team does not exist!");
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

	//Try Parse Filter
	public Boolean tryParseFilter(RMPlayer rmp){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
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
	
	private HashMap<Integer, RMItem> getItemsMatch(RMPlayer rmp, HashMap<Integer, RMItem> hashItems){
		Inventory inv = rmp.getPlayer().getInventory();
		ItemStack[] contents = inv.getContents();
		//plugin.log.log(Level.WARNING, "contents:"+contents.length);
		//plugin.log.log(Level.WARNING, "hashItems:"+hashItems.size());
		HashMap<Integer, RMItem> items = new HashMap<Integer, RMItem>();
		for(int i=0; i<contents.length; i++){
			ItemStack item = contents[i];
			if((item!=null)&&(item.getType()!=Material.AIR)){
				int id = item.getTypeId();
				//plugin.log.log(Level.WARNING, "no:"+Material.getMaterial(id));
				if(hashItems.containsKey(id)){
					//plugin.log.log(Level.WARNING, "type:"+item.getType().name()+",i:"+i);
					//plugin.log.log(Level.WARNING, "itemAmount:"+item.getAmount());
					ItemStack hashItem = hashItems.get(id).getItem();
					//plugin.log.log(Level.WARNING, "hashItemAmount:"+hashItem.getAmount());
					int overflow = hashItem.getAmount() - item.getAmount();
					//plugin.log.log(Level.WARNING, "overflow:"+overflow);
					if(!items.containsKey(id)){
						//plugin.log.log(Level.WARNING, "not contain");
						RMItem rmItem = new RMItem(item);
						rmItem.setAmount(rmItem.getAmount()+(overflow<0?overflow:0));
						items.put(id, rmItem);
					}
					else{
						//plugin.log.log(Level.WARNING, "contain");
						RMItem rmItem = items.get(id);
						rmItem.setAmount(rmItem.getAmount()+item.getAmount()+(overflow<0?overflow:0));
						items.put(id, rmItem);
					}
					hashItem.setAmount(overflow<=0?0:overflow);
					//plugin.log.log(Level.WARNING, "hashItemAmount2:"+hashItem.getAmount());
					if(hashItem.getAmount()<=0) hashItems.remove(id);
					if(overflow>=0) inv.clear(i);
					else{
						item.setAmount(-overflow);
						//hashItems.remove(id);
					}
				}
			}
		}
		return items;
	}
	
	//Parse Filter
	private void parseFilter(RMPlayer rmp){
		RMRequestFilter filter = rmp.getRequestFilter();
		if(filter!=null){
			ForceState force = filter.getForce();
			int randomize = filter.getRandomize();
			FilterState filterState = filter.getFilterState();
			HashMap<Integer, RMItem> items = filter.getItems();
			
			if((items!=null)&&(items.size()!=0)){
				switch(filterState){
					case REWARD: case TOOLS:
						String strItems = "";
						List<ItemStack> isItems = new ArrayList<ItemStack>();
						for(RMItem rmItem : items.values()){
							strItems+=rmItem.getMaterial().name()+":"+rmItem.getAmount()+",";
							isItems.add(rmItem.getItem());
						}
						//plugin.log.log(Level.WARNING, "items1:"+strItems);
						strItems = "";
						for(ItemStack item : isItems){
							strItems+=item.getType().name()+":"+item.getAmount()+",";
						}
						//plugin.log.log(Level.WARNING, "isItems2:"+strItems);
						items = getItemsMatch(rmp, items);
						
						strItems = "";
						for(RMItem rmItem : items.values()){
							strItems+=rmItem.getMaterial().name()+":"+rmItem.getAmount()+",";
						}
						//plugin.log.log(Level.WARNING, "items3:"+strItems);
						
						break;
				}
				if(_config.getWarnHackedItems()) warnHackMaterials(items);
				if(!_config.getAllowHackedItems()){
					items = removeHackMaterials(items);
					if(items==null){
						rmp.sendMessage(ChatColor.GRAY+"No items modified.");
						return;
					}
					//plugin.log.log(Level.WARNING, "removehack:"+items.size());
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
					switch(filterState){
						case FILTER:
							clearFilter(rmp);
							return;
						case REWARD:
							clearReward(rmp);
							return;
						case TOOLS:
							clearTools(rmp);
							return;
					}
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
		for(Integer item : arrayItems){
			Material mat = Material.getMaterial(item);
			if(mat!=Material.AIR){
				if(getId) strItem = ""+item;
				else strItem = mat.name();
				Integer amount = items.get(item).getAmount();
				if(amount!=0){
					switch(filterState){
						case FILTER:
							switch(_config.getFilter().addItem(item, items.get(item), false)){
							case ADD:
								added.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item)));
								break;
							case MODIFY:
								modified.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item)));	
								break;
							}
							break;
						case REWARD:
							switch(addItem(items.get(item).getItem(), ClaimType.REWARD)){
							case ADD: case MODIFY:
								added.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item)));
							break;
							}
						case TOOLS:
							switch(addItem(items.get(item).getItem(), ClaimType.TOOLS)){
							case ADD: case MODIFY:
								added.add(ChatColor.WHITE+strItem+RMText.includeItem(items.get(item)));
							break;
							}
					}
				}
				else{
					switch(filterState){
						case FILTER:
							switch(_config.getFilter().removeAlwaysItem(item, items.get(item))){
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+RMText.includeItem(_config.getFilter().getLastItem()));
								break;
							}
							break;
							/*
						case REWARD:
							switch(removeItem(items.get(item))){
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+includeItem(_config.getFilter().getLastItem()));
								break;
							}
							break;
						case TOOLS:
							switch(removeItem(items.get(item))){
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+includeItem(_config.getFilter().getLastItem()));
								break;
							}
							break;
							*/
					}
				}
				//if(addRemoveItemToFilter(item, items.get(item))) added.add(ChatColor.WHITE+strItem+includeItem(items.get(item)));
				//else removed.add(ChatColor.WHITE+strItem+includeItem(items.get(item)));
			}
		}
		if(added.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+RMText.getFormattedStringByList(added));
		if(modified.size()>0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+RMText.getFormattedStringByList(modified));
		if(removed.size()>0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+RMText.getFormattedStringByList(removed));
	}
	
	//Parse Filter Args Force
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, RMItem> items, ForceState force){
		List<String> added = new ArrayList<String>();
		List<String> modified = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		String strItem;
		Boolean getId = false;
		if(items.size()>plugin.config.getTypeLimit()) getId=true;
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
		case REMOVE:
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
					line+=ChatColor.GREEN+Material.getMaterial(item).name()+RMText.includeItem(rmItem)+ChatColor.WHITE+", ";
				}
				else line+=ChatColor.DARK_GREEN+Material.getMaterial(item).name()+":0"+ChatColor.WHITE+", ";
			}
			else{
				if(rmChest.getItems().containsKey(item)) amount = rmChest.getItemLeft(item).getAmount();
				else amount = _config.getItems().getItem(item).getAmount();
				if(amount!=0) line+=ChatColor.WHITE+Material.getMaterial(item).name()+RMText.includeItem(new RMItem(item, amount))+ChatColor.WHITE+", ";
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
		if(plugin.config.getUseRestore()) if(_config.getLog().restore()) broadcastMessage("World restored.");
	}
	
	//Restore World
	public void restoreWorld(RMPlayer rmp){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			restoreLog();
		}
		else rmp.sendMessage("Only the owner can restore the world changes");
	}
	
	//HACK MATERIALS
	//Warn Hack Materials
	
	/*public void warnHackMaterials(ItemStack[] items){
		warnHackMaterialsMessage(plugin.getFormattedStringByListMaterial(findHackMaterials(items)));
	}*/
	
	public void warnHackMaterialsListItemStack(List<ItemStack> items){
		warnHackMaterialsMessage(RMText.getFormattedStringByListMaterial(findHackMaterialsListItemStack(items)));
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
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(minPlayers<getTeams().size()) minPlayers = getTeams().size();
			_config.setMinPlayers(minPlayers);
			rmp.sendMessage(RMText.minPlayers+": "+getTextMinPlayers());
			updateSigns();
		}
	}
	
	//Set Max Players
	public void setMaxPlayers(RMPlayer rmp, int maxPlayers){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(maxPlayers<getTeams().size()) maxPlayers = getTeams().size();
			_config.setMaxPlayers(maxPlayers);
			rmp.sendMessage(RMText.maxPlayers+": "+getTextMaxPlayers());
			updateSigns();
		}
	}
	
	//Set Min Team Players
	public void setMinTeamPlayers(RMPlayer rmp, int minTeamPlayers){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			_config.setMinTeamPlayers(minTeamPlayers);
			rmp.sendMessage(RMText.minTeamPlayers+": "+getTextMinTeamPlayers());
			updateSigns();
		}
	}
	
	//Set Max Team Players
	public void setMaxTeamPlayers(RMPlayer rmp, int maxTeamPlayers){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			_config.setMaxTeamPlayers(maxTeamPlayers);
			rmp.sendMessage(RMText.maxTeamPlayers+": "+getTextMaxTeamPlayers());
			updateSigns();
		}
	}
	
	//Set Max Items
	public void setMaxItems(RMPlayer rmp, int maxItems){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			_config.setMaxItems(maxItems);
			updateSigns();
		}
	}
	
	//Set Randomize Amount
	public void setRandomizeAmount(RMPlayer rmp, int amount){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			_config.setRandomizeAmount(amount);
		}
	}
	
	//Set Auto Randomize Amount
	public void setAutoRandomizeAmount(RMPlayer rmp, int amount){
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			_config.setAutoRandomizeAmount(amount);
			rmp.sendMessage("Auto randomize amount every match: "+getTextAutoRandomizeAmount());
		}
	}
	
	//SET & TOGGLE
	//Set Toggle Warp to Safety
	public void setWarpToSafety(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.warpToSafety)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleWarpToSafety();
			else _config.setWarpToSafety(i>0?true:false);
			rmp.sendMessage(RMText.warpToSafety+": "+isTrueFalse(_config.getWarpToSafety()));
		}
	}
	
	//Set Toggle Auto Restore World
	public void setAutoRestoreWorld(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.restore)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleAutoRestoreWorld();
			else _config.setAutoRestoreWorld(i>0?true:false);
			rmp.sendMessage(RMText.autoRestoreWorld+": "+isTrueFalse(_config.getAutoRestoreWorld()));
		}
	}
	
	//Set Toggle Warn Hacked Items
	public void setWarnHackedItems(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.warnHackedItems)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
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
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleAllowHackedItems();
			else _config.setAllowHackedItems(i>0?true:false);
			rmp.sendMessage(RMText.allowHackedItems+": "+isTrueFalse(_config.getAllowHackedItems()));
		}
	}
	
	//Set Toggle Allow Player Leave
	public void setKeepIngame(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.keepIngame)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleKeepIngame();
			else _config.setKeepIngame(i>0?true:false);
			rmp.sendMessage(RMText.keepIngame+": "+isTrueFalse(_config.getKeepIngame()));
		}
	}
	
	//Set Toggle Midgame Join
	public void setAllowMidgameJoin(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.allowMidgameJoin)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleAllowMidgameJoin();
			else _config.setAllowMidgameJoin(i>0?true:false);
			rmp.sendMessage(RMText.allowMidgameJoin+": "+isTrueFalse(_config.getAllowMidgameJoin()));
		}
	}
	
	//Set Toggle Clear Player Inventory
	public void setClearPlayerInventory(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.clearPlayerInventory)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleClearPlayerInventory();
			else _config.setClearPlayerInventory(i>0?true:false);
			rmp.sendMessage(RMText.clearPlayerInventory+": "+isTrueFalse(_config.getClearPlayerInventory()));
		}
	}
	
	public void setWarnUnequal(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.warnUnequal)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleWarnUnequal();
			else _config.setWarnUnequal(i>0?true:false);
			rmp.sendMessage(RMText.warnUnequal+": "+isTrueFalse(_config.getWarnUnequal()));
		}
	}
	
	public void setAllowUnequal(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.allowUnequal)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleAllowUnequal();
			else _config.setAllowUnequal(i>0?true:false);
			rmp.sendMessage(RMText.allowUnequal+": "+isTrueFalse(_config.getAllowUnequal()));
		}
	}
	
	public void setInfiniteReward(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.infiniteReward)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			if(i==-1) _config.toggleInfiniteReward();
			else _config.setInfiniteReward(i>0?true:false);
			rmp.sendMessage(RMText.infiniteReward+": "+isTrueFalse(_config.getInfiniteReward()));
		}
	}
	
	public void setInfiniteTools(RMPlayer rmp, int i){
		if(plugin.config.getLock().contains(Lock.infiniteTools)){
			rmp.sendMessage(RMText.noChangeLocked);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
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
		rmp.sendMessage("Game id: "+ChatColor.YELLOW+_config.getId());
		rmp.sendMessage("Owner: "+ChatColor.YELLOW+_config.getOwnerName());
		rmp.sendMessage("Players: "+ChatColor.YELLOW+getTeamPlayers().length);
		rmp.sendMessage("Teams: "+getTextTeamPlayers());
	}
	
	public void getInfoFound(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.info.found")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		if((rmp.getName().equalsIgnoreCase(_config.getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
			String items = RMText.getSortedItemsFromItemStackArray(_config.getFoundArray());
			if(items.length()>0){
				rmp.sendMessage(ChatColor.YELLOW+"Found items: "+items);
			}
			else rmp.sendMessage(ChatColor.YELLOW+"No found items.");
		}
	}
	
	public void sendSettings(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.info.settings")){
			rmp.sendMessage(RMText.noPermissionAction);
			return;
		}
		rmp.sendMessage(ChatColor.YELLOW+"minplayers "+getTextMinPlayers()+" "+ChatColor.WHITE+RMText.minPlayers+".");
		rmp.sendMessage(ChatColor.YELLOW+"maxplayers "+getTextMaxPlayers()+" "+ChatColor.WHITE+RMText.maxPlayers+".");
		rmp.sendMessage(ChatColor.YELLOW+"minteamplayers "+getTextMinTeamPlayers()+" "+ChatColor.WHITE+RMText.minTeamPlayers+".");
		rmp.sendMessage(ChatColor.YELLOW+"maxteamplayers "+getTextMaxTeamPlayers()+" "+ChatColor.WHITE+RMText.maxTeamPlayers+".");
		rmp.sendMessage(ChatColor.YELLOW+"random "+ChatColor.AQUA+getTextAutoRandomizeAmount()+" "+ChatColor.WHITE+"Randomly pick "+ChatColor.AQUA+"amount "+ChatColor.WHITE+"of items every match.");
		rmp.sendMessage(ChatColor.YELLOW+"warp "+isTrueFalse(_config.getWarpToSafety())+" "+ChatColor.WHITE+RMText.warpToSafety+".");
		rmp.sendMessage(ChatColor.YELLOW+"restore "+isTrueFalse(_config.getAutoRestoreWorld())+" "+ChatColor.WHITE+RMText.autoRestoreWorld+".");
		rmp.sendMessage(ChatColor.YELLOW+"warnhacked "+isTrueFalse(_config.getWarnHackedItems())+" "+ChatColor.WHITE+RMText.warnHackedItems+".");
		rmp.sendMessage(ChatColor.YELLOW+"allowhacked "+isTrueFalse(_config.getAllowHackedItems())+" "+ChatColor.WHITE+RMText.allowHackedItems+".");
		rmp.sendMessage(ChatColor.YELLOW+"keepingame "+isTrueFalse(_config.getKeepIngame())+" "+ChatColor.WHITE+RMText.keepIngame+".");
		rmp.sendMessage(ChatColor.YELLOW+"midgamejoin "+isTrueFalse(_config.getAllowMidgameJoin())+" "+ChatColor.WHITE+RMText.allowMidgameJoin+".");
		rmp.sendMessage(ChatColor.YELLOW+"clearinventory "+isTrueFalse(_config.getClearPlayerInventory())+" "+ChatColor.WHITE+RMText.clearPlayerInventory+".");
		rmp.sendMessage(ChatColor.YELLOW+"warnunequal "+isTrueFalse(_config.getWarnUnequal())+" "+ChatColor.WHITE+RMText.warnUnequal+".");
		rmp.sendMessage(ChatColor.YELLOW+"allowunequal "+isTrueFalse(_config.getAllowUnequal())+" "+ChatColor.WHITE+RMText.allowUnequal+".");
		rmp.sendMessage(ChatColor.YELLOW+"infinitereward "+isTrueFalse(_config.getInfiniteReward())+" "+ChatColor.WHITE+RMText.infiniteReward+".");
		rmp.sendMessage(ChatColor.YELLOW+"infinitetools "+isTrueFalse(_config.getInfiniteTools())+" "+ChatColor.WHITE+RMText.infiniteTools+".");
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
	
	
	////////
	//Game//
	////////
	
	public static Material[] getMaterials(){
		return _materials;
	}
	
	//Get Game by Id (String)
	public static RMGame getGameById(String arg){
		int id = RMHelper.getIntByString(arg);
		if(id!=-1) return RMGame.getGame(id);
		return null;
	}
	
	//Get Game by Id
	public static RMGame getGameById(int arg){
		return RMGame.getGame(arg);
	}
	
	////////
	//Team//
	////////
	
	//Get All Teams
	public static List<RMTeam> getAllTeams(){
		List<RMTeam> teams = new ArrayList<RMTeam>();
		for(RMGame game : _games){
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
}