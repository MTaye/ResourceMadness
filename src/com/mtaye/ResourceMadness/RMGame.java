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
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
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
	
	private int _id;
	private String _ownerName;
	private RMPlayer _owner;
	private List<RMTeam> _teams = new ArrayList<RMTeam>();
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	private RMState _state = RMState.SETUP;
	//private Inventory _inventory;
	
	private final int _amountLimit = 20;
	private List<Material> _filter = new ArrayList<Material>();
	private static RMPlayer _requestPlayer;
	private int _amount;
	private boolean _random = true;
	private boolean _allowHackMaterials = false;
	
	private enum Part { GLASS, STONE, CHEST, WALL_SIGN, WOOL; }
	public enum RMState { SETUP, COUNTDOWN, GAMEPLAY, GAMEOVER; }
	public enum FilterType { ALL, CLEAR, BLOCK, ITEM, RAW, CRAFTED};
	
	private final int cdTimerLimit = 300; //3 seconds
	private int cdTimer = cdTimerLimit;
	
	private HashMap<Material, Integer> _items = new HashMap<Material, Integer>();
	public HashMap<Material, Integer> getItemsHashMap(){
		return _items;
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

	public List<Material> getFilterItems(){
		return _filter;
	}
	
	public Material[] getItems(){
		if(_items!=null){
			Material[] newItems = new Material[_items.size()];
			Object[] items = _items.keySet().toArray();
			for(int i=0; i<_items.size(); i++){
				newItems[i] = (Material)items[i];
			}
			return newItems;
		}
		return null;
	}
	public int[] getItemsAmount(){
		Integer[] intArray = (Integer[])_items.values().toArray();
		int[] iArray = new int[intArray.length];
		for(int i=0; i<intArray.length; i++){
			iArray[i] = intArray[i];
		}
		return iArray;
	}
	public void prepareGame(){
		
	}
	
	public void startGame(RMPlayer rmp){
		if(rmp.getName()==getOwnerName()){
			if(_items.size()>0){
				for(RMTeam rmTeam : _teams){
					if(rmTeam.getPlayers().length==0){
						rmp.sendMessage("Each team must have at least one player");
						return;
					}
				}
				prepareGame();
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
			setState(RMState.SETUP);
		}
		else rmp.sendMessage("Only the owner "+getOwnerName()+" can stop the game.");
	}

	
	//FILTER
	public void clearItemsToFilter(){
		_filter.clear();
	}
	private Boolean addItemToFilter(Material mat){
		if(!_filter.contains(mat)){
			_filter.add(mat);
			return true;
		}
		return false;
	}
	private Boolean removeItemToFilter(Material mat){
		if(_filter.contains(mat)){
			_filter.remove(mat);
			return true;
		}
		return false;
	}
	private Boolean addRemoveItemToFilter(Material mat){
		if(!_filter.contains(mat)){
			_filter.add(mat);
			return true;
		}
		else{
			_filter.remove(mat);
			return false;
		}
	}
	public List<Material> addItemsToFilter(List<Material> items){
		List<Material> addedItems = new ArrayList<Material>();
		if(!_allowHackMaterials) items = removeHackMaterials(items);
		for(Material item : items){
			if(item!=Material.AIR){
				if(addItemToFilter(item)) addedItems.add(item);
			}
		}
		return addedItems;
	}
	public List<Material> removeItemsToFilter(List<Material> items){
		List<Material> removedItems = new ArrayList<Material>();
		if(!_allowHackMaterials) items = removeHackMaterials(items);
		for(Material item : items){
			if(item!=Material.AIR){
				if(removeItemToFilter(item)) removedItems.add(item);
			}
		}
		return removedItems;
	}
	public List<Material>[] addRemoveItemsToFilter(List<Material> items){
		List<Material> addedItems = new ArrayList<Material>();
		List<Material> removedItems = new ArrayList<Material>();
		if(!_allowHackMaterials) items = removeHackMaterials(items);
		for(Material item : items){
			if(item!=Material.AIR){
				if(addRemoveItemToFilter(item)) addedItems.add(item);
				else removedItems.add(item);
			}
		}
		List<Material>[] allItems = new List[2];
		allItems[0] = addedItems;
		allItems[1] = removedItems;
		return allItems;
	}
	public void sortItemsToFilter(){
		if(_filter.size()!=0){
			Collections.sort(_filter);
		}
	}
	
	//ITEMS TO FIND
	public void clearItems(){
		_items.clear();
	}
	public void addItem(ItemStack item){
		Material mat = item.getType();
		if(!_items.containsKey(mat)){
			_items.put(mat, 0);
		}
	}
	public void addItem(Material mat){
		if(!_items.containsKey(mat)){
			_items.put(mat, 0);
		}
	}
	public void addItems(List<Material> items){
		for(Material item : items){
			addItem(item);
		}
	}
	public void removeItem(ItemStack item){
		Material mat = item.getType();
		if(_items.containsKey(mat)){
			_items.remove(mat);
		}
	}
	public void removeItem(Material mat){
		if(_items.containsKey(mat)){
			_items.remove(mat);
		}
	}
	public void removeItems(List<Material> items){
		for(Material item : items){
			removeItem(item);
		}
	}
	public Boolean addRemoveItem(ItemStack item){
		Material mat = item.getType();
		if(_items.containsKey(mat)){
			_items.remove(mat);
			return false;
		}
		else{
			_items.put(mat, 0);
			return true;
		}
	}
	public Boolean addRemoveItem(Material mat){
		if(_items.containsKey(mat)){
			_items.remove(mat);
			return false;
		}
		else{
			_items.put(mat, 0);
			return true;
		}
	}
	public void addRemoveItems(List<Material> items){
		for(Material item : items){
			addRemoveItem(item);
		}
	}
	public HashMap<Material, Integer> getItemsToFind(){
		return _items;
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
				for(RMTeam rmTeam : getTeams()){
					Sign sign = rmTeam.getSign();
					sign.setLine(0, "Filtered items:");
					sign.setLine(1, ""+getFilterItems().size());
					sign.setLine(2, "Players:");
					sign.setLine(3, ""+rmTeam.getPlayers().length);
					sign.update();
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
					sign.setLine(0, "Items to find:");
					sign.setLine(1, ""+rmChest.getItems().size());
					sign.setLine(2, "Amount to find:");
					sign.setLine(3, ""+rmChest.getItemsAmount());
					sign.update();
				}
				break;
			case GAMEOVER:
				break;
		}
	}
	public void trySignInfo(Block b, RMPlayer rmp){
		updateSigns();
		switch(getState()){
			case SETUP:
				if(rmp.getName() == getOwnerName()){
					String items = "";
					for(int i=0; i<_filter.size(); i++){
						items += _filter.get(i).getId()+",";
					}
					if(items.length()>0){
						items = items.substring(0, items.length()-1);
						rmp.sendMessage(ChatColor.YELLOW+"Filtered items:"+ChatColor.WHITE+items);
					}
					else rmp.sendMessage("No items to find.");
				}
				break;
			case GAMEPLAY:
				break;
		}
	}
	//Try Add Items
	public void tryAddItems(Block b, RMPlayer rmp){
		switch(getState()){
			case SETUP:
				if(rmp.getName() == getOwnerName()){
					RMChest rmChest = getChestByBlock(b);
					Inventory inv = rmChest.getChest().getInventory();
					ItemStack[] items = inv.getContents();
					String addedItems = "";
					String removedItems = "";
					for(int i=0; i<items.length; i++){
						ItemStack is = items[i];
						if(is != null){
							Material item = is.getType();
							if(item!=null){
								if(item!=Material.AIR){
									if(!isMaterial(item, _hackMaterials)){
										if(addRemoveItemToFilter(item)){
											addedItems+=item+",";
										}
										else{
											removedItems+=item+",";
										}
									}
								}
							}
						}
					}
					if(addedItems.length()>0){
						addedItems = addedItems.substring(0,addedItems.length()-1);
						rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+addedItems);
					}
					if(removedItems.length()>0){
						removedItems = removedItems.substring(0,removedItems.length()-1);
						rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+removedItems);
					}
					sortItemsToFilter();
					updateSigns();
				}
				break;
			case GAMEPLAY:
				RMTeam rmTeam = getTeamByBlock(b);
				RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
				if((rmTeam!=null)&&(rmPlayer!=null)){
					RMChest rmChest = rmTeam.getChest();
					for(ItemStack item : rmChest.getContents()){
						rmChest.addItem(item);
					}
				}
				break;
		}
	}

	//UPDATE
	public void update(){
		for(RMChest rmChest : getChests()){

			switch(getState()){
				case SETUP:
					break;
				case COUNTDOWN:
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
					}
					break;
				case GAMEPLAY:
					break;
				case GAMEOVER:
					setState(RMState.SETUP);
					break;
			}
			/*
			int con1 = 0;
			int con2 = 0;
			if(rmChest.getContents()!=null) con1=rmChest.getContents().length;
			if(rmChest.getOldContents()!=null) con2=rmChest.getOldContents().length;
				
			if((rmChest.getContents()!=null)&&(rmChest.getOldContents()!=null)){
				plugin.getServer().broadcastMessage("size0:"+con1+"size1:"+con2);
			}
			//if(rmChest.getContents().length!=rmChest.getOldContents().length){
				//rmChest.setOldChest(rmChest.getChest());
			//}
			Inventory inv = rmChest.getChest().getInventory();
			Inventory oldInv = rmChest.getOldChest().getInventory();
			//If contents have changed
			//if((inv.getSize()!=oldInv.getSize())||(inv!=oldInv)){
			plugin.getServer().broadcastMessage("new:"+inv.getContents().length+",old:"+oldInv.getContents().length);
			if(inv.getContents().length!=oldInv.getContents().length){
				rmChest.setOldChest(rmChest.getChest());
				plugin.getServer().broadcastMessage("JUP!");
			}
			*/
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
		//plugin.getServer().broadcastMessage("i0:"+getStringBlockList(blockList, true));
		if(bRemove!=null) blockList = RMGame.removeFromBlockList(blockList, bRemove);
		//plugin.getServer().broadcastMessage("i1:"+getStringBlockList(blockList, true));
		blockList = getCompleteParts(blockList);
		//plugin.getServer().broadcastMessage("i2:"+getStringBlockList(blockList, true));
		
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
		return true;
	}
	public static Boolean tryRemoveGame(RMGame rmGame, RMPlayer rmp){
		if(rmGame!=null){
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
	
	//Team
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
			rmp.sendMessage("You quit the "+plugin.getChatColorByDye(rmTeam.getTeamColor())+rmTeam.getTeamColor()+ChatColor.WHITE+" team.");
		}
	}
	public RMTeam joinTeamByBlock(Block b, RMPlayer rmp){
		if(getState() == RMState.SETUP){
			RMTeam rmt = getTeamByBlock(b);
			if(rmt!=null){
				RMTeam rmTeam = getPlayerTeam(rmp);
				if(rmTeam!=null){
					if(rmt!=rmTeam){
						rmp.sendMessage("You must quit the "+plugin.getChatColorByDye(rmTeam.getTeamColor())+rmTeam.getTeamColor()+ChatColor.WHITE+" team first.");
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
				rmp.sendMessage("You quit the "+plugin.getChatColorByDye(rmTeam.getTeamColor())+rmTeam.getTeamColor()+ChatColor.WHITE+" team.");
				return rmTeam;
			}
			else rmp.sendMessage("You are not on the "+plugin.getChatColorByDye(rmTeam.getTeamColor())+rmTeam.getTeamColor()+ChatColor.WHITE+" team.");
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
			//rmt.setGame(this);
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
		plugin.getServer().broadcastMessage(""+rmGame._teams.size());
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
			chestList.add(new RMChest((Chest)blocks.get(Part.CHEST.ordinal()-2).getState()));
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
				line+=plugin.getChatColorByDye(team.getTeamColor())+team.getTeamColor().name()+ChatColor.WHITE+",";
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
	
	public String getTeamsPlayers(){
		String line = "";
		for(RMTeam team : _teams){
			if(team.getPlayers()!=null){
				line+=plugin.getChatColorByDye(team.getTeamColor())+team.getTeamColor().name()+":";
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
	//Filter
	public List<Material> getFilter(){
		return _filter;
	}
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
			Material[] items = filter.getItems();
			if((items!=null)&&(items.length!=0)){
				if(!_allowHackMaterials){
					items = removeHackMaterials(items);
					if(items==null){
						rmp.sendMessage("No items modified.");
						return;
					}
				}
				if(force!=null){
					parseFilterArgs(rmp, items, force);
					sortItemsToFilter();
					return;
				}
				else{
					parseFilterArgs(rmp, items);
					sortItemsToFilter();
					return;
				}
			}
			FilterType type = filter.getType();
			if(type!=null){
				if(force!=null){
					parseFilterArgs(rmp, type, force);
					sortItemsToFilter();
					return;
				}
				else{
					parseFilterArgs(rmp, type);
					sortItemsToFilter();
					return;
				}
			}
		}
		rmp.sendMessage("No items modified.");
	}
	private void parseFilterArgs(RMPlayer rmp,Material[] items){
		String added = "";
		String removed = "";
		String strItem;
		Boolean getId = false;
		if(items.length>40) getId=true;
		for(Material item : items){
			if(item!=Material.AIR){
				if(getId) strItem = ""+item.getId();
				else strItem = item.name();
				
				if(addRemoveItemToFilter(item)) added+=strItem+",";
				else removed+=strItem+",";
			}
		}
		if(added.length()>0) rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+plugin.stripLast(added, ","));
		if(removed.length()>0) rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+plugin.stripLast(removed, ","));
	}
	private void parseFilterArgs(RMPlayer rmp,Material[] items, Boolean force){
		String added = "";
		String removed = "";
		String strItem;
		Boolean getId = false;
		if(items.length>40) getId=true;
		if(force){
			for(Material item : items){
				if(item!=Material.AIR){
					if(getId) strItem = ""+item.getId();
					else strItem = item.name();
					if(addItemToFilter(item)) added+=strItem+",";
				}
			}
			if(added.length()>0) rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+plugin.stripLast(added, ","));
		}
		else{
			for(Material item : items){
				if(item!=Material.AIR){
					if(getId) strItem = ""+item.getId();
					else strItem = item.name();
					if(removeItemToFilter(item)) removed+=strItem+",";
				}
			}
			if(removed.length()>0) rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+plugin.stripLast(removed, ","));
		}
	}
	private void parseFilterArgs(RMPlayer rmp,FilterType type){
		String added = "";
		String removed = "";
		if(type == FilterType.CLEAR){
			clearItemsToFilter();
			rmp.sendMessage(ChatColor.YELLOW+"Filter cleared.");
			return;
		}
		else{
			Material[] materials = Material.values();
			if(!_allowHackMaterials) materials = removeHackMaterials(materials);
			switch(type){
			case ALL:
				for(Material mat : materials){
					if(mat!=Material.AIR){
						if(addRemoveItemToFilter(mat)) added+=mat.getId()+",";
						else removed+=mat.getId()+",";
					}
				}
				if(added.length()>0) rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+plugin.stripLast(added, ","));
				if(removed.length()>0) rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+plugin.stripLast(removed, ","));
				return;
			case BLOCK:
				for(Material mat : materials){
					if(mat!=Material.AIR){
						if(mat.isBlock()){
							if(addRemoveItemToFilter(mat)) added+=mat.getId()+",";
							else removed+=mat.getId()+",";
						}
					}
				}
				if(added.length()>0) rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+plugin.stripLast(added, ","));
				if(removed.length()>0) rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+plugin.stripLast(removed, ","));
				return;
			case ITEM:
				for(Material mat : materials){
					if(mat!=Material.AIR){
						if(!mat.isBlock()){
							if(addRemoveItemToFilter(mat)) added+=mat.getId()+",";
							else removed+=mat.getId()+",";
						}
					}
				}
				if(added.length()>0) rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+plugin.stripLast(added, ","));
				if(removed.length()>0) rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+plugin.stripLast(removed, ","));
				return;
			case RAW:
				return;
			case CRAFTED:
				return;
		}
		}
	}
	private void parseFilterArgs(RMPlayer rmp, FilterType type, Boolean force){
		String added = "";
		String removed = "";
		if(type == FilterType.CLEAR){
			clearItemsToFilter();
			rmp.sendMessage(ChatColor.YELLOW+"Filter cleared.");
			return;
		}
		else{
			Material[] materials = Material.values();
			if(!_allowHackMaterials) materials = removeHackMaterials(materials);
			switch(type){
			case ALL:
				materials = Material.values();
				if(!_allowHackMaterials) materials = removeHackMaterials(materials);
				if(force){
					for(Material mat : materials){
						if(mat!=Material.AIR) if(addItemToFilter(mat)) added+=mat.getId()+",";
					}
					if(added.length()>0) rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+plugin.stripLast(added, ","));
					return;
				}
				else{
					for(Material mat : materials){
						if(mat!=Material.AIR) if(removeItemToFilter(mat)) removed+=mat.getId()+",";
					}
					if(added.length()>0) rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+plugin.stripLast(removed, ","));
					return;
				}
			case BLOCK:
				materials = Material.values();
				if(!_allowHackMaterials) materials = removeHackMaterials(materials);
				if(force){
					for(Material mat : materials){
						if(mat!=Material.AIR) if(mat.isBlock()) if(addItemToFilter(mat)) added+=mat.getId()+",";
					}
					if(added.length()>0) rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+plugin.stripLast(added, ","));
					return;
				}
				else{
					for(Material mat : materials){
						if(mat!=Material.AIR) if(mat.isBlock()) if(removeItemToFilter(mat)) removed+=mat.getId()+",";
					}
					if(added.length()>0) rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+plugin.stripLast(removed, ","));
					return;
				}
			case ITEM:
				materials = Material.values();
				if(!_allowHackMaterials) materials = removeHackMaterials(materials);
				if(force){
					for(Material mat : materials){
						
						if(mat!=Material.AIR) if(!mat.isBlock()) if(addItemToFilter(mat)) added+=mat.getId()+",";
					}
					if(added.length()>0) rmp.sendMessage(ChatColor.YELLOW+"Added:"+ChatColor.WHITE+plugin.stripLast(added, ","));
					return;
				}
				else{
					for(Material mat : materials){
						if(mat!=Material.AIR) if(!mat.isBlock()) if(removeItemToFilter(mat)) removed+=mat.getId()+",";
					}
					if(added.length()>0) rmp.sendMessage(ChatColor.GRAY+"Removed:"+ChatColor.WHITE+plugin.stripLast(removed, ","));
					return;
				}
			case RAW:
				return;
			case CRAFTED:
				return;
			}
		}
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
}
