package com.mtaye.ResourceMadness;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.mtaye.ResourceMadness.RMGame.FilterType;

public class RMPlayer {
	public enum PlayerAction{
		ADD, REMOVE, SETUP, JOIN, QUIT, START, RESTART, STOP, FILTER, NONE;
	}
	
	private String _playerName;
	private Player _player;
	private RMTeam _team;
	private List<RMGame> _games;
	private RMRequestFilter _requestFilter;
	
	public static RM plugin;
	private static HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	
	private PlayerAction _playerAction = PlayerAction.NONE;
	
	//Constructor
	private RMPlayer(Player player){
		setPlayer(player);
		setPlayerAction(PlayerAction.NONE);
	}
	private RMPlayer(Player player, PlayerAction playerAction, RM plugin){
		setPlayerAction(playerAction);
		setPlayer(player);
	}
	
	public void setRequestFilter(Material[] items, FilterType type, Boolean force){
		_requestFilter = new RMRequestFilter(items,type,force);
	}
	public RMRequestFilter getRequestFilter(){
		if(_requestFilter!=null) return _requestFilter;
		return null;
	}
	public void clearRequestFilter(){
		_requestFilter = null;
	}
	
	//Name GET/SET
	public String getName(){
		return _playerName;
	}
	private void setName(String name){
		_playerName = name;
	}
	
	//Player GET/SET
	public Player getPlayer(){
		return _player;
	}
	private void setPlayer(Player player){
		_player = player;
		setName(player.getName());
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
				RMPlayer rmp = new RMPlayer(p);
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
	
	//Team GET/SET
	public RMTeam getTeam(){
		return _team;
	}
	
	//Game
	public void setGames(List<RMGame> games){
		_games = games;
	}
	public List<RMGame> getGames(){
		return _games;
	}
	
	//SendMessage
	public void sendMessage(String message){
		getPlayer().sendMessage(message);
	}
}
