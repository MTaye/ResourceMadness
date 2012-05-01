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
import com.mtaye.ResourceMadness.RMPlayer.ChatMode;
import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.Helper.RMHelper;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMTeam {

	//private final RM plugin;
	private RMGame _game;
	private DyeColor _teamColor;
	private HashMap<String, RMPlayer> _players = new HashMap<String, RMPlayer>();
	private static List<RMTeam> _teams = new ArrayList<RMTeam>();
	private RMChest _chest;
	private Sign _sign;
	private Location _warpLocation;
	private boolean _isDisqualified = false;
	
	public RMTeam(DyeColor color, Chest chest, RM plugin){
		//this.plugin = plugin;
		init(color, chest, plugin);
	}
	
	public RMTeam(DyeColor color, RMGame game, Chest chest, RM plugin){
		//this.plugin = plugin;
		this._game = game;
		init(color, chest, plugin);
	}
	
	public void init(DyeColor color, Chest chest, RM plugin){
		this._teamColor = color;
		_chest = new RMChest(chest, this);
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
		//if(_game!=null) if(_game.getGameConfig().getBanList().isBanned(rmp.getName())) return;
		rmp.setTeam(this);
		_players.put(rmp.getName(), rmp);
	}
	
	//Player
	public void addRemovePlayer(RMPlayer rmp){
		if(!_players.containsKey(rmp.getName())){
			if(!rmp.hasPermission("resourcemadness.join")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
			addPlayer(rmp);
			return;
		}
		else{
			if(!rmp.hasPermission("resourcemadness.quit")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
			removePlayer(rmp);
			return;
		}
	}
	public void addPlayer(RMPlayer rmp){
		if(!rmp.isIngame()){
			if(!_game.getGameConfig().getSettingStr(Setting.password).equalsIgnoreCase(rmp.getRequestString())){
				rmp.setRequestInt(_game.getGameConfig().getId());
				rmp.setRequestString(getTeamColor().name());
				rmp.setPlayerAction(PlayerAction.JOIN_PASSWORD);
				rmp.sendMessage(RMText.getLabelArgs("join.password.type", ""+_game.getGameConfig().getId()));
				return;
			}
		}
		if(_game.getGameConfig().getBanList().isBanned(rmp.getName())){
			rmp.sendMessage(RMText.getLabelArgs("join.banned", ""+_game.getGameConfig().getId()));
			return;
		}
		switch(_game.getGameConfig().getState()){
		case SETUP: case GAMEPLAY: case PAUSED:
			for(RMGame game : RMGame.getGames().values()){
				RMTeam rmTeam = game.getTeamByPlayer(rmp);
				if(rmTeam!=null){
					if(rmTeam!=this){
						rmp.sendMessage(RMText.getLabelArgs("join.must_quit_other_game_team", rmTeam.getTeamColorString(), ""+game.getGameConfig().getId()));
						return;
					}
				}
			}
			if((_game.getGameConfig().getState() == GameState.GAMEPLAY)&&(!_game.getGameConfig().getSettingBool(Setting.midgamejoin))){
				rmp.sendMessage(RMText.getLabel("join.game_in_progress.not"));
				return;
			}
			if((_game.getGameConfig().getSettingInt(Setting.maxteamplayers)==0)||(_players.size()<_game.getGameConfig().getSettingInt(Setting.maxteamplayers))){
				if((_game.getGameConfig().getSettingInt(Setting.maxplayers)==0)||(RMTeam.getAllPlayers().length<_game.getGameConfig().getSettingInt(Setting.maxplayers))){
					rmp.setTeam(this);
					_players.put(rmp.getName(), rmp);
					rmp.setReady(false);
					rmp.setChatMode(ChatMode.GAME);
					if(rmp.getPlayer()!=null){
						if(rmp.getRequestBool()){
							//rmp.setReturnLocation(rmp.getPlayer().getLocation());
							//rmp.setRequestBool(false);
						}
					}
					rmp.sendMessage(RMText.getLabelArgs("join", getTeamColorString()));
					_game.broadcastMessage(RMText.getLabelArgs("join.broadcast", rmp.getName(), getTeamColorString()), rmp);
					_game.updateSigns();
				}
				else rmp.sendMessage(RMText.getLabelArgs("join.game_full", ""+_game.getGameConfig().getId()));
			}
			else rmp.sendMessage(RMText.getLabelArgs("join.team_full", getTeamColorString()));
			break;
		default: rmp.sendMessage(RMText.getLabel("join.countdown")); break;
		}
	}
	
	public void removePlayer(RMPlayer rmp){
		removePlayer(rmp, false);
	}
	
	public void removePlayer(RMPlayer rmp, boolean kick){
		if(_players.containsKey(rmp.getName())){
			switch(_game.getGameConfig().getState()){
			case SETUP: case GAMEPLAY: case PAUSED:
				if(!kick){
					rmp.sendMessage(RMText.getLabelArgs("quit",getTeamColorString()));
					_game.broadcastMessage(RMText.getLabelArgs("quit.broadcast", rmp.getName(), getTeamColorString()), rmp);
				}
				break;
			default:
				rmp.sendMessage(RMText.getLabel("quit.countdown"));
				return;
			}
			rmp.clearTeam();
			rmp.setReady(false);
			_players.remove(rmp.getName());
			_game.updateSigns();
			rmp.onPlayerQuit(this);
		}
	}
	
	public RMPlayer getPlayer(String name){
		return _players.get(name);
	}
	
	public RMPlayer[] getPlayers(){
		List<RMPlayer> rmPlayers = new ArrayList<RMPlayer>();
		for(RMPlayer rmPlayer : _players.values()){
			if(rmPlayer!=null) rmPlayers.add(rmPlayer);
		}
		return rmPlayers.toArray(new RMPlayer[rmPlayers.size()]);
	}
	
	public String[] getPlayersNamesArray(){
		List<String> rmPlayers = new ArrayList<String>();
		for(RMPlayer rmPlayer : _players.values()){
			if(rmPlayer!=null) rmPlayers.add(rmPlayer.getName());
		}
		return rmPlayers.toArray(new String[rmPlayers.size()]);
	}

	public String getPlayersNames(){
		RMPlayer[] rmplayers = _players.values().toArray(new RMPlayer[_players.values().size()]);
		String names = "[";
		for(RMPlayer rmp : rmplayers){
			names+=rmp.getName()+(rmp.getReady()?ChatColor.RED+":R"+getChatColor():"")+",";
		}
		if(names.length()>1){
			names = RMTextHelper.stripLast(names, ",");
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