package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.ForceState;
import com.mtaye.ResourceMadness.RMGame.RMState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPlayer {
	public enum PlayerAction{
		ADD, REMOVE, SETUP, INFO,
		SAVE_TEMPLATE,
		JOIN, QUIT,
		START, START_RANDOMIZE, RESTART, STOP,
		FILTER,
		MAX_PLAYERS, MAX_TEAM_PLAYERS, MAX_ITEMS,
		AUTO_RANDOMIZE_ITEMS,
		RESTORE_WORLD, AUTO_RESTORE_WORLD,
		WARN_HACKED_ITEMS, ALLOW_HACKED_ITEMS,
		ALLOW_PLAYER_LEAVE, CLEAR_PLAYER_INVENTORY, ALLOW_MIDGAME_JOIN,
		NONE;
	}
	private String _name;
	private RMTeam _team;
	//private List<RMGame> _games;
	private RMRequestFilter _requestFilter;
	private boolean _sneak = false;
	private int _requestInt = 0;
	private RMStats _stats = new RMStats();
	private List<ItemStack> _inventory = new ArrayList<ItemStack>();
	private boolean _isOnline = false;
	
	public void clearInventoryContents(){
		_inventory.clear();
	}
	
	public void addContentsToInventory(ItemStack[] items){
		for(ItemStack item : items){
			_inventory.add(item);
		}
	}
	
	public ItemStack[] getContentsFromInventory(){
		return _inventory.toArray(new ItemStack[_inventory.size()]);
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
	private RMPlayer(String player){
		setPlayer(player);
		setPlayerAction(PlayerAction.NONE);
	}
	private RMPlayer(String player, PlayerAction playerAction){
		setPlayerAction(playerAction);
		setPlayer(player);
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
		plugin.log.log(Level.WARNING,"Player "+name+" doesn't exist.");
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
	public void sendMessage(String message){
		if(getPlayer()!=null) getPlayer().sendMessage(message);
	}
	
	public void warpToSafety(){
		if(getPlayer()!=null){
			Player p = getPlayer();
			double pLocX = p.getLocation().getX();
			double pLocY = p.getLocation().getY();
			double pLocZ = p.getLocation().getZ();
			Location loc = _team.getWarpLocation();
			if((Math.abs(pLocX-loc.getX())>4)||(Math.abs(pLocY-loc.getY())>4)||(Math.abs(pLocZ-loc.getZ())>4)){
				loc.setPitch(p.getLocation().getPitch());
				loc.setYaw(p.getLocation().getYaw());
				getPlayer().teleport(loc.add(0.5, -2+p.getEyeHeight(), 0.5));
			}
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
	
	public boolean isInGame(){
		RMTeam rmTeam = getTeam();
		if(rmTeam!=null){
			RMGame rmGame = rmTeam.getGame();
			if(rmGame!=null){
				if(rmGame.getState()==RMState.GAMEPLAY){
					return true;
				}
			}
		}
		return false;
	}
	
	public void onPlayerJoin(){
	}
	
	public void onPlayerQuit(){
		RMTeam rmTeam = getTeam();
		if(rmTeam!=null){
			RMGame rmGame = rmTeam.getGame();
			if(rmGame!=null){
				if(rmGame.getState()==RMState.GAMEPLAY){
					if(!rmGame.getConfig().getAllowPlayerLeave()){
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
}
