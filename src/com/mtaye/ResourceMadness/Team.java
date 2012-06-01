package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

import com.mtaye.ResourceMadness.Game.GameState;
import com.mtaye.ResourceMadness.GamePlayer.ChatMode;
import com.mtaye.ResourceMadness.GamePlayer.PlayerAction;
import com.mtaye.ResourceMadness.Stats.RMStat;
import com.mtaye.ResourceMadness.helper.Helper;
import com.mtaye.ResourceMadness.helper.TextHelper;
import com.mtaye.ResourceMadness.setting.Setting;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class Team {

	//private final RM plugin;
	private Game _game;
	private DyeColor _teamColor;
	private TreeMap<String, GamePlayer> _players = new TreeMap<String, GamePlayer>();
	private static List<Team> _teams = new ArrayList<Team>();
	private GameChest _chest;
	private SignWrapper _sign;
	private Location _warpLocation;
	private boolean _isDisqualified = false;
	
	public Team(DyeColor color, Chest chest, RM plugin){
		//this.plugin = plugin;
		init(color, chest, plugin);
	}
	
	public Team(DyeColor color, Game game, Chest chest, RM plugin){
		//this.plugin = plugin;
		this._game = game;
		init(color, chest, plugin);
	}
	
	public void init(DyeColor color, Chest chest, RM plugin){
		this._teamColor = color;
		_chest = new GameChest(chest, this);
		_sign =  new SignWrapper((Sign)chest.getBlock().getRelative(BlockFace.UP).getState());
		Sign sign = _sign.getHandle();
		if(sign!=null) _warpLocation = findWarpLocation(sign.getBlock());
		if(!_teams.contains(this))_teams.add(this);
	}
	
	public Location findWarpLocation(Block b){
		BlockFace face = Dir.getBlockFaceByData(b.getData());
		face = Dir.getBlockFaceOpposite(face);
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
		return Helper.getChatColorByDye(_teamColor);
	}
	public String getTeamColorString(){
		return Helper.getChatColorByDye(getTeamColor()) + _teamColor.name();
	}
	
	public void setTeamColor(DyeColor color){
		_teamColor = color;
	}

	//Game
	public Game getGame(){
		return _game;
	}
	public void setGame(Game game){
		_game = game;
	}
	
	public void addPlayerSilent(GamePlayer rmp){
		//if(_game!=null) if(_game.getGameConfig().getBanList().isBanned(rmp.getName())) return;
		rmp.setTeam(this);
		_players.put(rmp.getName(), rmp);
	}
	
	//Player
	public void addRemovePlayer(GamePlayer rmp){
		if(!_players.containsKey(rmp.getName())){
			if(!rmp.hasPermission("resourcemadness.join")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
			addPlayer(rmp);
			return;
		}
		else{
			if(!rmp.hasPermission("resourcemadness.quit")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
			removePlayer(rmp);
			return;
		}
	}
	public void addPlayer(GamePlayer rmp){
		if(!rmp.isIngame()){
			String password = _game.getGameConfig().getSettingStr(Setting.password);
			if((password.length()!=0)&&(!password.equalsIgnoreCase(rmp.getRequestString()))){
				rmp.setRequestStringArray(_game.getGameConfig().getName(), getTeamColor().name());
				rmp.setPlayerAction(PlayerAction.JOIN_PASSWORD);
				rmp.sendMessage(Text.getLabelArgs("join.password.type", _game.getGameConfig().getName(), ""+_game.getGameConfig().getId()));
				return;
			}
		}
		if(_game.getGameConfig().getBanList().isBanned(rmp.getName())){
			rmp.sendMessage(Text.getLabelArgs("join.banned", _game.getGameConfig().getName(), ""+_game.getGameConfig().getId()));
			return;
		}
		switch(_game.getGameConfig().getState()){
		case SETUP: case GAMEPLAY: case PAUSED:
			for(Game game : Game.getGames().values()){
				Team rmTeam = game.getTeamByPlayer(rmp);
				if(rmTeam!=null){
					if(rmTeam!=this){
						rmp.sendMessage(Text.getLabelArgs("join.must_quit_other_game_team", rmTeam.getTeamColorString(), game.getGameConfig().getName(), ""+_game.getGameConfig().getId()));
						return;
					}
				}
			}
			if((_game.getGameConfig().getState() == GameState.GAMEPLAY)&&(!_game.getGameConfig().getSettingBool(Setting.midgamejoin))){
				rmp.sendMessage(Text.getLabel("join.game_in_progress.not"));
				return;
			}
			if((_game.getGameConfig().getSettingInt(Setting.maxteamplayers)==0)||(_players.size()<_game.getGameConfig().getSettingInt(Setting.maxteamplayers))){
				if((_game.getGameConfig().getSettingInt(Setting.maxplayers)==0)||(Team.getAllPlayers().length<_game.getGameConfig().getSettingInt(Setting.maxplayers))){
					_players.put(rmp.getName(), rmp);
					rmp.sendMessage(Text.getLabelArgs("join", getTeamColorString()));
					_game.broadcastMessage(Text.getLabelArgs("join.broadcast", rmp.getName(), getTeamColorString()), rmp);
					rmp.onPlayerJoinTeam(this);
					_game.updateSigns();
				}
				else rmp.sendMessage(Text.getLabelArgs("join.game_full", _game.getGameConfig().getName(), ""+_game.getGameConfig().getId()));
			}
			else rmp.sendMessage(Text.getLabelArgs("join.team_full", getTeamColorString()));
			break;
		default: rmp.sendMessage(Text.getLabel("join.countdown")); break;
		}
	}
	
	public void removePlayer(GamePlayer rmp){
		removePlayer(rmp, false);
	}
	
	public void removePlayer(GamePlayer rmp, boolean kick){
		if(_players.containsKey(rmp.getName())){
			GameState state = _game.getGameConfig().getState();
			switch(state){
			case SETUP: case GAMEPLAY: case PAUSED:
				if(!kick){
					switch(state){
					case GAMEPLAY: case PAUSED:
						rmp.getStats().add(RMStat.LOSSES);
						break;
					}
					rmp.sendMessage(Text.getLabelArgs("quit",getTeamColorString()));
					_game.broadcastMessage(Text.getLabelArgs("quit.broadcast", rmp.getName(), getTeamColorString()), rmp);
				}
				break;
			default:
				rmp.sendMessage(Text.getLabel("quit.countdown"));
				return;
			}
			//rmp.clearReturnLocation();
			clearPlayer(rmp);
			_game.updateSigns();
			Game game = getGame();
			if(game!=null){
				switch(game.getGameConfig().getState()){
				case GAMEPLAY: case PAUSED:
					game.checkPlayerQuit(rmp, this);
					break;
				}
			}
		}
	}
	
	/*
	public TreeMap<String, GamePlayer> getPlayerMap(){
		return _players;
	}
	*/
	
	public GamePlayer getPlayer(String name){
		return _players.get(name);
	}
	
	public GamePlayer[] getPlayers(){
		List<GamePlayer> result = new ArrayList<GamePlayer>();
		for(GamePlayer rmPlayer : _players.values()){
			result.add(rmPlayer);
		}
		return result.toArray(new GamePlayer[result.size()]);
	}
	
	public String[] getPlayersNamesArray(){
		List<String> rmPlayers = new ArrayList<String>();
		for(GamePlayer rmPlayer : _players.values()){
			if(rmPlayer!=null) rmPlayers.add(rmPlayer.getName());
		}
		return rmPlayers.toArray(new String[rmPlayers.size()]);
	}

	public String getPlayersNames(){
		GamePlayer[] players = getPlayers();
		String names = "[";
		for(GamePlayer rmp : players){
			names+=rmp.getName()+(rmp.getReady()?ChatColor.RED+":R"+getChatColor():"")+",";
		}
		if(names.length()>1){
			names = TextHelper.stripLast(names, ",");
			names += "]";
			return names;
		}
		return "[]";
	}
	
	public void clearPlayer(GamePlayer rmp){
		if(!_players.containsKey(rmp.getName())) return;
		rmp.onPlayerQuitTeam();
		_players.remove(rmp.getName());
	}
	
	public void clearPlayers(){
		for(GamePlayer rmp : _players.values()){
			rmp.onPlayerQuitTeam();
		}
		_players.clear();
	}

	//Player
	public static GamePlayer[] getAllPlayers(){
		List<GamePlayer> players = new ArrayList<GamePlayer>();
		for(Team team : _teams){
			for(GamePlayer rmp : team.getPlayers()){
				players.add(rmp);
			}
		}
		return players.toArray(new GamePlayer[players.size()]);
	}
	
	//Teams
	public static List<Team> getTeams(){
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
	public GameChest getChest(){
		return _chest;
	}
	public GameChest[] getChests(){
		return _game.getChests();
	}
	public Sign getSign(){
		return _sign.getHandle();
	}
	public List<Sign> getSigns(){
		return _game.getSigns();
	}
	
	public void teamMessage(String message){
		GamePlayer[] players = getPlayers();
		for(GamePlayer rmp : players){
			rmp.sendMessage(message);
		}
	}
	public void teamMessage(String message, GamePlayer ignorePlayer){
		GamePlayer[] players = getPlayers();
		for(GamePlayer rmp : players){
			if((ignorePlayer==null)||(rmp!=ignorePlayer)) rmp.sendMessage(message);
		}
	}
	
	public boolean hasMininumPlayers(){
		if(getPlayers().length<getGame().getGameConfig().getSettingInt(Setting.minteamplayers)) return false;
		return true;
	}
	public boolean isDisqualified(){
		return _isDisqualified;
	}
	public void isDisqualified(boolean isDisqualified){
		_isDisqualified = isDisqualified;
	}
}