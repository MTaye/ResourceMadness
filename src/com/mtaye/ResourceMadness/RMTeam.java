package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

public class RMTeam {

	private RM plugin;
	private RMGame _game;
	private DyeColor _teamColor;
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	private static List<RMTeam> _teams = new ArrayList<RMTeam>();
	private RMChest _chest;
	private Sign _sign;
	private Location _warpLocation;
	
	public RMTeam(DyeColor color, Chest chest, RM plugin){
		init(color, chest, plugin);
	}
	
	public RMTeam(DyeColor color, RMGame game, Chest chest, RM plugin){
		this._game = game;
		init(color, chest, plugin);
	}
	
	public void init(DyeColor color, Chest chest, RM plugin){
		this.plugin = plugin;
		this._teamColor = color;
		_chest = new RMChest(chest, this, plugin);
		_sign = (Sign)chest.getBlock().getRelative(BlockFace.UP).getState();
		_warpLocation = findWarpLocation(_sign.getBlock());
	}
	
	public Location findWarpLocation(Block b){
		BlockFace face = RMDir.getBlockFaceByData(b.getData());
		face = RMDir.getBlockFaceOpposite(face);
		Location loc = b.getRelative(face).getLocation();
		loc = loc.add(0.5, 0.5, 0.5);
		return loc;
	}
	public Location getWarpLocation(){
		return _warpLocation;
	}
	
	//TeamColor
	public DyeColor getTeamColor(){
		return _teamColor;
	}
	public String getTeamColorString(){
		return plugin.getChatColorByDye(getTeamColor()) + _teamColor.name();
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
	
	//Player
	public void addPlayer(RMPlayer rmp){
		if(!_players.containsKey(rmp.getName())){
			for(RMGame game : RMGame.getGames()){
				RMTeam rmTeam = game.getPlayerTeam(rmp);
				if(rmTeam!=null){
					if(rmTeam!=this){
						rmp.sendMessage("You must quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team from game id "+ChatColor.YELLOW+game.getId()+ChatColor.WHITE+" first.");
						return;
					}
				}
			}
			rmp.setTeam(this);
			_players.put(rmp.getName(), rmp);
			rmp.sendMessage(ChatColor.YELLOW+"Joined"+ChatColor.WHITE+" the "+getTeamColorString()+ChatColor.WHITE+" team.");
			_game.updateSigns();
			return;
		}
		else{
			rmp.clearTeam();
			_players.remove(rmp.getName());
			rmp.sendMessage(ChatColor.GRAY+"Quit"+ChatColor.WHITE+" the "+getTeamColorString()+ChatColor.WHITE+" team.");
			_game.updateSigns();
			return;
		}
	}
	public void removePlayer(RMPlayer rmp){
		if(_players.containsKey(rmp.getName())){
			_players.remove(rmp.getName());
		}
	}
	public RMPlayer getPlayer(String name){
		return _players.get(name);
	}
	public RMPlayer[] getPlayers(){
		RMPlayer[] rmplayers = _players.values().toArray(new RMPlayer[_players.values().size()]);
		return rmplayers;
	}
	public String getPlayersNames(){
		RMPlayer[] rmplayers = _players.values().toArray(new RMPlayer[_players.values().size()]);
		String names = "";
		for(RMPlayer rmp : rmplayers){
			names+=rmp.getName()+",";
		}
		if(names.length()>1) return names.substring(0, names.length()-1);
		return "[]";
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
	}
	
	//Chest
	public RMChest getChest(){
		return _chest;
	}
	public List<RMChest> getChests(){
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
}