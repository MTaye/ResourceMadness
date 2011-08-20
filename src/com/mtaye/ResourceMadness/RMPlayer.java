package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.ForceState;
import com.mtaye.ResourceMadness.RMGame.GameState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPlayer {
	public enum PlayerAction{
		ADD, REMOVE, INFO,
		JOIN, QUIT, START, START_RANDOM, RESTART, STOP,
		RESTORE, FILTER,
		SET_MAX_PLAYERS, SET_MAX_TEAM_PLAYERS, SET_MAX_ITEMS, SET_RANDOM,
		SET_WARP, SET_RESTORE, SET_WARN_HACKED, SET_ALLOW_HACKED,
		SET_KEEP_INGAME, SET_CLEAR_INVENTORY, SET_MIDGAME_JOIN,
		NONE;
	}
	private String _name;
	private RMTeam _team;
	//private List<RMGame> _games;
	private RMRequestFilter _requestFilter;
	private boolean _sneak = false;
	private int _requestInt = 0;
	private RMStats _stats = new RMStats();
	private List<ItemStack> _items = new ArrayList<ItemStack>();
	private List<ItemStack> _award = new ArrayList<ItemStack>();
	private boolean _isOnline = false;
	
	public ItemStack[] getItems(){
		return _items.toArray(new ItemStack[_items.size()]);
	}
	public ItemStack[] getAward(){
		return _award.toArray(new ItemStack[_award.size()]);
	}
	public void clearItems(){
		_items.clear();
	}
	public void clearAward(){
		_award.clear();
	}
	
	public void addItems(){
		if(getPlayer()!=null){
			Inventory inv = getPlayer().getInventory();
			//clearInventoryContents();
			for(ItemStack item : inv.getContents()){
				if((item!=null)&&(item.getType()!=Material.AIR)){
					_items.add(item);
				}
			}
			inv.clear();
		}
	}

	public void addItemsByItemStack(ItemStack[] items){
		//clearInventoryContents();
		for(ItemStack item : items){
			if((item!=null)&&(item.getType()!=Material.AIR)){
				_items.add(item);
			}
		}
	}
	public void addAwardByItemStack(ItemStack[] items){
		//clearInventoryContents();
		for(ItemStack item : items){
			if((item!=null)&&(item.getType()!=Material.AIR)){
				_award.add(item);
			}
		}
	}
	
	public void claim(List<ItemStack> items, ClaimType claimType){
		if(_items.size()==0){
			switch(claimType){
				case ITEMS:	sendMessage("No items to return."); break;
				case AWARD:	sendMessage("No award to return."); break;
			}
			return;
		}
		if(getPlayer()!=null){
			Inventory inv = getPlayer().getInventory();
			if(inv.firstEmpty()==-1){
				switch(claimType){
					case ITEMS:	sendMessage("Your inventory is full. Cannot return items."); break;
					case AWARD:	sendMessage("Your inventory is full. Cannot give award."); break;
				}
				return;
			}
			List<ItemStack> removeItems = new ArrayList<ItemStack>();
			for(int i=0; i<_items.size(); i++){
				if(inv.firstEmpty()!=-1){
					ItemStack item = _items.get(i);
					if((item!=null)&&(item.getType()!=Material.AIR)){
						inv.addItem(item);
						//removeItems.add(item);
						removeItems.add(item);
					}
				}
				else break;
			}
			for(ItemStack item : removeItems){
				_items.remove(item);
			}
			//inv.clear();
			//clearInventoryContents();
			if(_items.size()>0){
				sendMessage("Inventory is full. "+ChatColor.YELLOW+_items.size()+ChatColor.WHITE+" item(s) remaining.");
			}
			else{
				switch(claimType){
					case ITEMS:	sendMessage("All items were returned. Check your inventory."); break;
					case AWARD:	sendMessage("Award was given. Check your inventory."); break;
				}
			}
		}
	}
	
	public void claimItems(){
		claim(_items, ClaimType.ITEMS);
	}
	public void claimAward(){
		claim(_award, ClaimType.AWARD);
	}
	
	public RMStats getStats(){
		return _stats;
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
	
	public void setRequestFilter(HashMap<Integer, RMItem> items, FilterType type, ForceState force, int randomize){
		_requestFilter = new RMRequestFilter(items,type,force,randomize);
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
	
	public void sneakOn(){
		_sneak = true;
	}
	public void sneakOff(){
		_sneak = false;
	}
	public boolean isSneaking(){
		return _sneak;
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
	
	public boolean hasPermission(String node){
		Player p = getPlayer();
		if(p!=null) return (plugin.hasPermission(p, node));
		return false;
	}
}
