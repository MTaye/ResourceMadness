package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.Helper.RMHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMTeam {

	private final RM plugin;
	private RMGame _game;
	private DyeColor _teamColor;
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	private static List<RMTeam> _teams = new ArrayList<RMTeam>();
	private RMChest _chest;
	private Sign _sign;
	private Location _warpLocation;
	private boolean _isDisqualified = false;
	
	public RMTeam(DyeColor color, Chest chest, RM plugin){
		this.plugin = plugin;
		init(color, chest, plugin);
	}
	
	public RMTeam(DyeColor color, RMGame game, Chest chest, RM plugin){
		this.plugin = plugin;
		this._game = game;
		init(color, chest, plugin);
	}
	
	public void init(DyeColor color, Chest chest, RM plugin){
		this._teamColor = color;
		_chest = new RMChest(chest, this, plugin);
		_sign = (Sign)chest.getBlock().getRelative(BlockFace.UP).getState();
		_warpLocation = findWarpLocation(_sign.getBlock());
		if(!_teams.contains(this))_teams.add(this);
	}
	
	public Location findWarpLocation(Block b){
		BlockFace face = RMDir.getBlockFaceByData(b.getData());
		face = RMDir.getBlockFaceOpposite(face);
		Location loc = b.getRelative(face).getLocation();
		loc = loc.add(0.5, -1, 0.5);
		return loc;
	}
	public Location getWarpLocation(){
		return _warpLocation;
	}
	
	//TeamColor
	public DyeColor getTeamColor(){
		return _teamColor;
	}
	public ChatColor getChatColor(){
		return RMHelper.getChatColorByDye(_teamColor);
	}
	public String getTeamColorString(){
		return RMHelper.getChatColorByDye(getTeamColor()) + _teamColor.name();
	}
	
	public void setTeamColor(DyeColor color){
		_teamColor = color;
	}

	//Game
	public RMGame getGame(){
		return _game;
	}
	public void setGame(RMGame game){
		_game = game;
	}
	
	public void addPlayerSilent(RMPlayer rmp){
		rmp.setTeam(this);
		_players.put(rmp.getName(), rmp);
	}
	
	//Player
	public void addRemovePlayer(RMPlayer rmp){
		if(!_players.containsKey(rmp.getName())){
			if(!rmp.hasPermission("resourcemadness.join")){
				rmp.sendMessage(RMText.noPermissionAction);
				return;
			}
			addPlayer(rmp);
			return;
		}
		else{
			if(!rmp.hasPermission("resourcemadness.quit")){
				rmp.sendMessage(RMText.noPermissionAction);
				return;
			}
			removePlayer(rmp);
			return;
		}
	}
	public void addPlayer(RMPlayer rmp){
		for(RMGame game : RMGame.getGames().values()){
			RMTeam rmTeam = game.getPlayerTeam(rmp);
			if(rmTeam!=null){
				if(rmTeam!=this){
					rmp.sendMessage("You must quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team from game id "+ChatColor.YELLOW+game.getConfig().getId()+ChatColor.WHITE+" first.");
					return;
				}
			}
		}
		if((_game.getConfig().getState() == GameState.GAMEPLAY)&&(!_game.getConfig().getAllowMidgameJoin())){
			rmp.sendMessage("You can't join a game while it's running.");
			return;
		}
		if((_game.getConfig().getMaxTeamPlayers()==0)||(_players.size()<_game.getConfig().getMaxTeamPlayers())){
			if((_game.getConfig().getMaxPlayers()==0)||(RMTeam.getAllPlayers().length<_game.getConfig().getMaxPlayers())){
				rmp.setTeam(this);
				_players.put(rmp.getName(), rmp);
				rmp.sendMessage(ChatColor.YELLOW+"Joined"+ChatColor.WHITE+" the "+getTeamColorString()+ChatColor.WHITE+" team.");
				_game.broadcastMessage(rmp.getName()+ChatColor.YELLOW+" joined"+ChatColor.WHITE+" the "+getTeamColorString()+ChatColor.WHITE+" team.", rmp);
				_game.updateSigns();
			}
			else rmp.sendMessage("All players slots in this game are already full.");
		}
		else rmp.sendMessage(getTeamColorString()+ChatColor.WHITE+" team is already full.");
		return;
	}
	
	public void removePlayer(RMPlayer rmp){
		if(_players.containsKey(rmp.getName())){
			switch(_game.getConfig().getState()){
			case SETUP: case GAMEPLAY: case PAUSED:
				rmp.clearTeam();
				_players.remove(rmp.getName());
				rmp.sendMessage(ChatColor.GRAY+"Quit"+ChatColor.WHITE+" the "+getTeamColorString()+ChatColor.WHITE+" team.");
				_game.broadcastMessage(rmp.getName()+ChatColor.GRAY+" quit"+ChatColor.WHITE+" the "+getTeamColorString()+ChatColor.WHITE+" team.", rmp);
				_game.updateSigns();
				rmp.onPlayerQuit(this);
				break;
			default: rmp.sendMessage(ChatColor.GRAY+"You can't quit the game now.");
			}
		}
	}
	public RMPlayer getPlayer(String name){
		return _players.get(name);
	}
	public RMPlayer[] getPlayers(){
		/*
		RMPlayer[] rmplayers = _players.values().toArray(new RMPlayer[_players.values().size()]);
		return rmplayers;
		*/
		List<RMPlayer> rmPlayers = new ArrayList<RMPlayer>();
		for(RMPlayer rmPlayer : _players.values()){
			if(rmPlayer!=null) rmPlayers.add(rmPlayer);
		}
		return rmPlayers.toArray(new RMPlayer[rmPlayers.size()]);
	}
	/*
	public RMPlayer[] getOnlinePlayers(){
		List<RMPlayer> rmPlayers = new ArrayList<RMPlayer>();
		for(RMPlayer rmPlayer : _players.values()){
			if(rmPlayer!=null) rmPlayers.add(rmPlayer);
		}
		return rmPlayers.toArray(new RMPlayer[rmPlayers.size()]);
	}
	*/
	public String getPlayersNames(){
		RMPlayer[] rmplayers = _players.values().toArray(new RMPlayer[_players.values().size()]);
		String names = "[";
		for(RMPlayer rmp : rmplayers){
			names+=rmp.getName()+(rmp.getReady()?ChatColor.RED+":R"+getChatColor():"")+",";
		}
		if(names.length()>1){
			names = RMText.stripLast(names, ",");
			names += "]";
			return names;
		}
		return "[]";
	}
	
	public void clearPlayers(){
		_players.clear();
	}

	//Player
	public static RMPlayer[] getAllPlayers(){
		List<RMPlayer> players = new ArrayList<RMPlayer>();
		for(RMTeam team : _teams){
			for(RMPlayer rmp : team.getPlayers()){
				players.add(rmp);
			}
		}
		return players.toArray(new RMPlayer[players.size()]);
	}
	
	//Teams
	public static List<RMTeam> getTeams(){
		return _teams;
	}
	
	public void setNull(){
		/*
		plugin = null;
		_game = null;
		_teamColor = null;
		_players = null;
		_teams = null;
		*/
		if(_teams.contains(this)) _teams.remove(this);
	}
	
	//Chest
	public RMChest getChest(){
		return _chest;
	}
	public RMChest[] getChests(){
		return _game.getChests();
	}
	public Sign getSign(){
		return _sign;
	}
	public List<Sign> getSigns(){
		return _game.getSigns();
	}
	
	public void teamMessage(String message){
		RMPlayer[] players = getPlayers();
		for(RMPlayer rmp : players){
			rmp.sendMessage(message);
		}
	}
	public void teamMessage(String message, RMPlayer ignorePlayer){
		RMPlayer[] players = getPlayers();
		for(RMPlayer rmp : players){
			if(rmp!=ignorePlayer) rmp.sendMessage(message);
		}
	}
	
	public boolean hasMininumPlayers(){
		if(getPlayers().length<getGame().getConfig().getMinTeamPlayers()) return false;
		return true;
	}
	public boolean isDisqualified(){
		return _isDisqualified;
	}
	public void isDisqualified(boolean isDisqualified){
		_isDisqualified = isDisqualified;
	}
}