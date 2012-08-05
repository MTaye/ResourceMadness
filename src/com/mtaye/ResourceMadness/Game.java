package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.Stats.RMStat;
import com.mtaye.ResourceMadness.Stats.RMStatServer;
import com.mtaye.ResourceMadness.helper.Helper;
import com.mtaye.ResourceMadness.helper.InventoryHelper;
import com.mtaye.ResourceMadness.helper.TextHelper;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.setting.SettingBool;
import com.mtaye.ResourceMadness.setting.SettingInt;
import com.mtaye.ResourceMadness.setting.SettingLibrary;
import com.mtaye.ResourceMadness.setting.SettingPrototype;
import com.mtaye.ResourceMadness.setting.SettingStr;
import com.mtaye.ResourceMadness.time.Timer;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class Game {

	private static TreeMap<String, Game> _games = new TreeMap<String, Game>();
	private static HashMap<Integer, String> _ids = new HashMap<Integer, String>();
	public static RM rm;
	public static Material[] _materials = {Material.GLASS, Material.STONE, Material.CHEST, Material.WALL_SIGN, Material.WOOL};
	public static Material[] _hackMaterials = {
		Material.AIR, Material.GRASS, Material.BEDROCK, Material.WATER,	Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA,
		Material.COAL_ORE, Material.SPONGE, Material.LAPIS_ORE, Material.BED_BLOCK, Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE,
		Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.DOUBLE_STEP, Material.FIRE, Material.MOB_SPAWNER,
		Material.REDSTONE_WIRE, Material.DIAMOND_ORE, Material.CROPS, Material.SOIL, Material.BURNING_FURNACE, Material.SIGN_POST,
		Material.WOODEN_DOOR, Material.WALL_SIGN, Material.IRON_DOOR_BLOCK, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
		Material.REDSTONE_TORCH_OFF, Material.SNOW_BLOCK, Material.ICE, Material.SUGAR_CANE_BLOCK, Material.PORTAL, Material.CAKE_BLOCK,
		Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.LOCKED_CHEST, Material.PUMPKIN_STEM, Material.MELON_STEM,
		Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS};
	
	private List<Material> _lastHackMaterials = new ArrayList<Material>();
	
	private GameConfig _config = new GameConfig();
	
	private static GamePlayer _requestPlayer;
	
	public static enum Part { GLASS, STONE, CHEST, WALL_SIGN, WOOL; }
	public static enum GameState { SETUP, COUNTDOWN, GAMEPLAY, GAMEOVER, PAUSED; }
	public static enum InterfaceState { FILTER, REWARD, TOOLS, FILTER_CLEAR, REWARD_CLEAR, TOOLS_CLEAR };
	public static enum ClickState { LEFT, RIGHT, NONE };
	public static enum HandleState { ADD, MODIFY, REMOVE, NO_CHANGE, NONE };
	public static enum FilterState { FILTER, FOUND, REWARD, TOOLS, ITEMS, NONE };
	public static enum FilterType { SET, ADD, SUBTRACT, CLEAR, RANDOMIZE, NONE};
	public static enum FilterItemType { ALL, BLOCK, ITEM, FOOD, RAW, CRAFTED};
	
	private final int _cdTimerLimit = 30; //3 seconds
	private int _cdTimer = _cdTimerLimit;
	
	private Team _winningTeam;
	private GamePlayer _winningPlayer;
	
	//Constructor
	public Game(String name, PartList partList, GamePlayer rmp, RM plugin){
		Game.rm = plugin;
		_config.setPartList(partList);
		_config.setPlayers(GamePlayer.getPlayers());
		_config.setId(Game.getFreeId());
		_config.setName(name);
		_config.setOwnerName(rmp.getName());
	}
	public Game(GameConfig gameConfig, RM plugin){
		Game.rm = plugin;
		_config = gameConfig;
	}
	
	//Config
	public GameConfig getGameConfig(){
		return _config;
	}
	public void setGameConfig(GameConfig config){
		_config = config;
	}
	public void resetSettings(GamePlayer rmp){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		_config = new GameConfig(rm.config);
		updateSigns();
		rmp.sendMessage(Text.getLabel("settings.reset"));
	}
	
	public boolean isRunning(){
		if(_config.getState()!=GameState.SETUP) return true;
		return false;
	}
	
	//PlayArea
	public boolean inRangeXZ(Entity ent, int radius){
		Block mainBlock = getMainBlock();
		Location loc = ent.getLocation();
		if(Math.abs(mainBlock.getX()-loc.getBlockX())>radius) return false;
		if(Math.abs(mainBlock.getZ()-loc.getBlockZ())>radius) return false;
		return true;
	}
	
	//SafeZone
	public boolean inRangeXYZ(Entity ent, int radius){
		Block mainBlock = getMainBlock();
		Location loc = ent.getLocation();
		if(Math.abs(mainBlock.getX()-loc.getBlockX())>radius) return false;
		if(Math.abs(mainBlock.getY()-loc.getBlockY())>radius) return false;
		if(Math.abs(mainBlock.getZ()-loc.getBlockZ())>radius) return false;
		return true;
	}
	
	//Ban Player
	public void banPlayer(GamePlayer rmp, boolean announce, List<String> names){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		for(String name : names){
			banPlayerSilent(rmp, true, name);
		}
		kickPlayer(rmp, false, names);
	}
	
	//Ban Team
	public void banTeam(GamePlayer rmp, boolean announce, List<String> colors){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		List<Team> teams = findRMTeamsByColors(rmp, announce, colors);
		Team[] arrayTeams = teams.toArray(new Team[teams.size()]);
		banTeamSilent(rmp, announce, arrayTeams);
		kickTeamSilent(rmp, false, arrayTeams);
	}
	
	//Ban All
	public void banAll(GamePlayer rmp, boolean announce){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		banAllSilent(rmp, announce);
	}
	
	//Unban Player
	public void unbanPlayer(GamePlayer rmp, boolean announce, List<String> names){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		List<String> notFound = new ArrayList<String>();
		List<String> found = new ArrayList<String>();
		Set<String> banList = _config.getBanList().keySet();
		for(String name : names){
			name =  name.trim().toLowerCase();
			if((name==null)||(name.length()==0)) continue;
			if(!banList.contains(name)) notFound.add(name);
			else found.add(name);
		}
		if(announce) if(notFound.size()!=0) if(rmp!=null) rmp.sendMessage(Text.getLabelArgs("game.banned_not_found", TextHelper.getStringByStringList(notFound, ", "), ""+_config.getName()));
		for(String name : found){
			unbanPlayerSilent(rmp, announce, name);
		}
	}
	
	//Kick
	public void kickPlayer(GamePlayer rmp, boolean announce, List<String> names){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		List<GamePlayer> found = findRMPlayersByNames(rmp, announce, names);
		kickPlayerSilent(rmp, announce, found.toArray(new GamePlayer[found.size()]));
	}

	public void kickTeam(GamePlayer rmp, boolean announce, List<String> colors){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		List<Team> found = findRMTeamsByColors(rmp, announce, colors);
		kickTeamSilent(rmp, announce, found.toArray(new Team[found.size()]));
	}
	
	public void kickAll(GamePlayer rmp, boolean announce){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		kickAllSilent(rmp, announce);
	}
	
	//Ban Silent
	public void banPlayerSilent(GamePlayer rmp, boolean announce, String name){
		_config.getBanList().add(name);
		if(announce){
			GamePlayer player = GamePlayer.getPlayerByNameOnly(name);
			if(player!=null){
				player.sendMessage(Text.getLabelArgs("ban.player", ""+getGameConfig().getName(), ""+_config.getId()));
				broadcastMessage(Text.getLabelArgs("ban.player.broadcast", player.getName(), ""+getGameConfig().getName(), ""+_config.getId()), player);
			}
			else broadcastMessage(Text.getLabelArgs("ban.player.broadcast", name, ""+getGameConfig().getName(), ""+_config.getId()), player);
		}
	}
	
	public void banTeamSilent(GamePlayer rmp, boolean announce, Team... teams){
		for(Team team : teams){
			String[] players = team.getPlayersNamesArray();
			if(players.length!=0){
				for(String player : players){
					banPlayerSilent(rmp, announce, player);
				}
			}
			else rmp.sendMessage(Text.getLabelArgs("team.empty", team.getTeamColorString()));
		}
	}
	

	private void banAllSilent(GamePlayer rmp, boolean announce){
		GamePlayer[] players = getTeamPlayers();
		if(players.length!=0){
			String[] strPlayers = getTeamPlayersNames();
			for(String player : strPlayers){
				banPlayerSilent(rmp, announce, player);
			}
			kickPlayerSilent(rmp, false, players);
		}
		else rmp.sendMessage(Text.getLabelArgs("game.empty", _config.getName()));
	}
	
	private void unbanPlayerSilent(GamePlayer rmp, boolean announce, String name){
		if(_config.getBanList().containsKey(name)){
			_config.getBanList().rem(name);
			if(announce){
				GamePlayer player = GamePlayer.getPlayerByNameOnly(name);
				if(player!=null){
					player.sendMessage(Text.getLabelArgs("unban.player", _config.getName(), ""+_config.getId()));
					broadcastMessage(Text.getLabelArgs("unban.player.broadcast", player.getName(), _config.getName(), ""+_config.getId()), player);
				}
				else broadcastMessage(Text.getLabelArgs("unban.player.broadcast", name, _config.getName(), ""+_config.getId()), player);
			}
		}
	}
	
	//Kick Silent
	private void kickPlayerSilent(GamePlayer rmp, boolean announce, GamePlayer... players){
		for(GamePlayer player : players){
			Team team = player.getTeam();
			team.removePlayer(player, true);
			if(announce){
				player.sendMessage(Text.getLabelArgs("kick.player", _config.getName(), ""+_config.getId()));
				broadcastMessage(Text.getLabelArgs("kick.player.broadcast", player.getName(), _config.getName(), ""+_config.getId()), player);
			}
		}
	}
	
	private void kickTeamSilent(GamePlayer rmp, boolean announce, Team... teams){
		for(Team team : teams){
			GamePlayer[] players = team.getPlayers();
			if(players.length!=0){
				kickPlayerSilent(rmp, announce, players);
			}
			else if(rmp!=null) rmp.sendMessage(Text.getLabelArgs("team.empty", team.getTeamColorString()));
		}
	}
	
	private void kickAllSilent(GamePlayer rmp, boolean announce){
		if(getTeamPlayers().length!=0){
			for(Team rmTeam : getTeams()){
				kickPlayerSilent(rmp, announce, rmTeam.getPlayers());
			}
		}
		else if(rmp!=null) rmp.sendMessage(Text.getLabelArgs("game.empty", _config.getName(), ""+_config.getId()));
	}
	
	//Find players/teams
	private List<GamePlayer> findRMPlayersByNames(GamePlayer rmp, boolean announce, List<String> names){
		return findRMPlayersByNames(rmp, true, announce, names);
	}
	
	private List<GamePlayer> findRMPlayersByNames(GamePlayer rmp, boolean findOnly, boolean announce, List<String> names){
		List<String> notFound = new ArrayList<String>();
		List<GamePlayer> found = new ArrayList<GamePlayer>();
		for(String name : names){
			name =  name.trim();
			if((name==null)||(name.length()==0)) continue;
			GamePlayer player = GamePlayer.getPlayerByNameOnly(name);
			if((player==null)||(player.getTeam()==null)) notFound.add(name);
			else found.add(player);
		}
		if(announce) if(notFound.size()!=0) if(rmp!=null) rmp.sendMessage(Text.getLabelArgs("game.players_not_found", TextHelper.getStringByStringList(notFound, ", "), _config.getName(), ""+_config.getId()));
		return found;
	}
	
	private List<Team> findRMTeamsByColors(GamePlayer rmp, boolean announce, List<String> colors){
		List<String> notFound = new ArrayList<String>();
		List<Team> found = new ArrayList<Team>();
		for(String color : colors){
			color =  color.trim();
			if((color==null)||(color.length()==0)) continue;
			Team rmTeam = getTeamByDye(color);
			if(rmTeam==null) notFound.add(color);
			else found.add(rmTeam);
		}
		if(announce) if(notFound.size()!=0) if(rmp!=null) rmp.sendMessage(Text.getLabelArgs("game.teams_not_found", TextHelper.getStringByStringList(notFound, ", "), _config.getName(), ""+_config.getId()));
		return found;
	}
	
	//Winning Team
	public Team getWinningTeam(){
		return _winningTeam;
	}
	public void setWinningTeam(Team rmTeam){
		_winningTeam = rmTeam;
	}
	public void clearWinningTeam(){
		_winningTeam = null;
	}
	
	//Winning Player
	public GamePlayer getWinningPlayer(){
		return _winningPlayer;
	}
	public void setWinningPlayer(GamePlayer rmp){
		_winningPlayer = rmp;
	}
	public void clearWinningPlayer(){
		_winningTeam = null;
	}
	
	//Pause
	public void pauseGame(GamePlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		switch(_config.getState()){
			case PAUSED: rmp.sendMessage(Text.getLabel("pause.game_already_paused")); break;
			case GAMEPLAY:
				_config.setState(GameState.PAUSED);
				broadcastMessage(Text.getLabel("pause.game_paused"));
				break;
			default: rmp.sendMessage(Text.getLabel("pause.must_be_ingame"));
		}
		updateSigns();
	}
	
	public void resumeGame(GamePlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		switch(_config.getState()){
			case PAUSED:
				_config.setState(GameState.GAMEPLAY);
				broadcastMessage(Text.getLabel("resume.game_resumed"));
				break;
			case GAMEPLAY: rmp.sendMessage(Text.getLabel("resume.game_not_paused")); break;
			default: rmp.sendMessage(Text.getLabel("resume.must_be_ingame"));
		}
		updateSigns();
	}
	
	//Mode
	public void cycleMode(GamePlayer rmp){
		cycleMode(rmp, true);
	}
	
	public void cycleMode(GamePlayer rmp, boolean cycleForward){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(cycleForward){
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
		}
		else{
			switch(_config.getInterface()){
			case FILTER: case FILTER_CLEAR:
				if(!changeMode(InterfaceState.TOOLS, rmp)) if(!changeMode(InterfaceState.REWARD, rmp)) changeMode(InterfaceState.FILTER, rmp);
				break;
			case REWARD:
				if(!changeMode(InterfaceState.FILTER, rmp)) if(!changeMode(InterfaceState.TOOLS, rmp)) changeMode(InterfaceState.REWARD, rmp);
				break;
			case TOOLS:
				if(!changeMode(InterfaceState.REWARD, rmp)) if(!changeMode(InterfaceState.FILTER, rmp)) changeMode(InterfaceState.TOOLS, rmp);
				break;
			}
		}
		updateSigns();
	}
	
	public boolean changeMode(InterfaceState interfaceState, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.mode")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return false;
		}
		if(_config.getState()!=GameState.SETUP) return false;
		switch(interfaceState){
			case FILTER:
				if(!rmp.hasPermission("resourcemadness.mode.filter")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return false;
			}
			case REWARD:
				if(!rmp.hasPermission("resourcemadness.mode.reward")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return false;
			}
			case TOOLS:
				if(!rmp.hasPermission("resourcemadness.mode.tools")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return false;
			}
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setInterface(interfaceState);
			rmp.sendMessage(Text.getLabelArgs("mode.change", Text.getLabelByInterfaceState(interfaceState)));
			updateSigns();
			return true;
		}
		return false;
	}
	
	//Money
	public void parseMoney(GamePlayer rmp, RequestMoney requestMoney){
		if(requestMoney == null) return;
		Double money = requestMoney.getMoney();
		FilterType filterType = requestMoney.getFilterType();
		
		Method economy = rm.economy;
		MethodAccount account = economy.getAccount(rmp.getName());
		//account.ge
		
		switch(filterType){
		case SET:
			if(!rmp.hasPermission("resourcemadness.money.set")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
			if(account.hasEnough(money)){
				account.subtract(money);
			}
			break;
		case ADD:
			if(!rmp.hasPermission("resourcemadness.money.set")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
			
			break;
		case SUBTRACT:
			if(!rmp.hasPermission("resourcemadness.money.set")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
			
			break;
		case CLEAR:
			if(!rmp.hasPermission("resourcemadness.money.set")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
			break;
		}
	}
		
	
	//Template Save
	public void saveTemplate(String template, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.template.save")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		Template rmTemplate = new Template(template, _config);
		rmp.saveTemplate(rmTemplate);
	}
	
	//Template Load
	public void loadTemplate(Template rmTemplate, GamePlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(!rmp.hasPermission("resourcemadness.template.load")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(rmTemplate!=null){
			_config.setFilter(rmTemplate.getFilter().clone());
			if(_config.getSettingBool(Setting.infinitereward)) _config.setReward(rmTemplate.getReward().clone());
			else _config.getReward().setItemsMatchInventory(rmp.getInventory(), rmp, ClaimType.REWARD, InventoryHelper.convertToHashMap(rmTemplate.getReward().clone().getItems()));
			if(_config.getSettingBool(Setting.infinitetools)) _config.setTools(rmTemplate.getTools().clone());
			else _config.getTools().setItemsMatchInventory(rmp.getInventory(), rmp, ClaimType.TOOLS, InventoryHelper.convertToHashMap(rmTemplate.getTools().clone().getItems()));
			rmp.sendMessage(Text.getLabelArgs("template.load", rmTemplate.getName()));
			updateSigns();
		}
	}

	//Filter
	public void clearFilter(GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.filter.clear")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		_config.getFilter().clearItems();
		rmp.sendMessage(Text.getLabel("filter.clear"));
	}
	
	//Stash
	public void clearReward(Block b, GamePlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.reward.clear")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(!_config.getSettingBool(Setting.infinitereward)){
			if(rmp.claimStashToChest(_config.getReward(), b, ClaimType.REWARD, rmp.getInventory(), items).size()==0){
				rmp.sendMessage(Text.getLabel("reward.clear"));
			}
		}
		else _config.getReward().clear();
	}
	public void clearTools(Block b, GamePlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.tools.clear")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(!_config.getSettingBool(Setting.infinitetools)){
			if(rmp.claimStashToChest(_config.getTools(), b, ClaimType.TOOLS, rmp.getInventory(), items).size()==0){
				rmp.sendMessage(Text.getLabel("tools.clear"));
			}
		}
		else _config.getTools().clear();
	}
	public void clearFound(GamePlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.found.clear")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(rmp.claim(_config.getFound(), ClaimType.FOUND, items).size()==0){
			rmp.sendMessage(Text.getLabel("found.clear"));
		}
	}

	public void addRewardByChest(GamePlayer rmp, GameChest rmChest, ClickState clickState){
		if(!rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		addChestToStash(rmp, rmChest, _config.getReward(), ClaimType.REWARD, clickState);
	}
	public void addToolsByChest(GamePlayer rmp, GameChest rmChest, ClickState clickState){
		if(!rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		addChestToStash(rmp, rmChest, _config.getTools(), ClaimType.TOOLS, clickState);
	}
	
	public void addChestToStash(GamePlayer rmp, GameChest rmChest, Stash rmStash, ClaimType claimType, ClickState clickState){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())) return;
		if(_config.getState()!=GameState.SETUP) return;
		List<ItemStack> items = rmChest.getContents();
		List<Material> hacked = new ArrayList<Material>();
		hacked = findHackMaterialsByItems(items);
		
		//Hacked
		if(_config.getSettingBool(Setting.warnhacked)) warnHackMaterials(hacked);
		if(!_config.getSettingBool(Setting.allowhacked)){
			items = removeHackMaterialsByItems(items);
			switch(claimType){
				case REWARD: if(!_config.getSettingBool(Setting.infinitereward)) rmChest.clearContentsExceptHacked(); break;
				case TOOLS: if(!_config.getSettingBool(Setting.infinitetools)) rmChest.clearContentsExceptHacked(); break;
			}
		}
		else switch(claimType){
			case REWARD: if(!_config.getSettingBool(Setting.infinitereward)) rmChest.clearContents(); break;
			case TOOLS: if(!_config.getSettingBool(Setting.infinitetools)) rmChest.clearContents(); break;
		}
		
		if(items.size()!=0){
			switch(claimType){
				case REWARD:
					if(!_config.getSettingBool(Setting.infinitereward)) rmStash.addItems(items);
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
					if(!_config.getSettingBool(Setting.infinitetools)) rmStash.addItems(items);
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
				if(rmp.isSneaking()){
					if(rmp.hasPermission("resourcemadness.reward.byhand")){
						addItemToStash(rmp, rmStash, claimType, clickState);
					}
					else rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				}
				else{
					if(_config.getReward().size()!=0){
						if(rmp.hasPermission("resourcemadness.reward.clear")){
							rmp.sendMessage(Text.getLabel("sign.clear_reward"));
							_config.setInterface(InterfaceState.REWARD_CLEAR);
						}
						else rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
					}
				}
				break;
			case TOOLS:
				if(rmp.isSneaking()){
					if(rmp.hasPermission("resourcemadness.tools.byhand")){
						addItemToStash(rmp, rmStash, claimType, clickState);
					}
					else rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				}
				else{
					if(_config.getTools().size()!=0){
						if(rmp.hasPermission("resourcemadness.tools.clear")){
							rmp.sendMessage(Text.getLabel("sign.clear_tools"));
							_config.setInterface(InterfaceState.TOOLS_CLEAR);
						}
						else rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
					}
				}
				break;
			}
		}
		else rmChest.clearContentsExceptHacked();
		updateSigns();
	}
	
	public void addItemToStash(GamePlayer rmp, Stash rmStash, ClaimType claimType, ClickState clickState){
		if(_config.getState()!=GameState.SETUP) return;
		if(!rmp.hasOwnerPermission(_config.getOwnerName())) return;
		ItemStack item = rmp.getPlayer().getItemInHand();
		List<ItemStack> items = new ArrayList<ItemStack>();
		items.add(item);
		
		if(_config.getSettingBool(Setting.warnhacked)) warnHackMaterialsByItems(items); 
		if(!_config.getSettingBool(Setting.allowhacked)) items = removeHackMaterialsByItems(items);
		
		if(items.size()!=0){
			ItemStack itemClone = item.clone();
			itemClone.setAmount(1);
			switch(clickState){
				case NONE: case LEFT:
					switch(claimType){
					case REWARD:
						rmStash.addItem(itemClone);
						if(!_config.getSettingBool(Setting.infinitereward)){
							item.setAmount(item.getAmount()-itemClone.getAmount());
							if(item.getAmount()==0) rmp.getPlayer().setItemInHand(null);
						}
						break;
					case TOOLS:
						rmStash.addItem(itemClone);
						if(!_config.getSettingBool(Setting.infinitetools)){
							item.setAmount(item.getAmount()-itemClone.getAmount());
							if(item.getAmount()==0) rmp.getPlayer().setItemInHand(null);
						}
						break;
					}
					break;
				case RIGHT:
					switch(claimType){
					case REWARD:
						if(_config.getSettingBool(Setting.infinitereward)) rmStash.removeByItem(itemClone);
						else rmp.claim(rmStash, ClaimType.NONE, itemClone);
						break;
					case TOOLS:
						if(_config.getSettingBool(Setting.infinitetools)) rmStash.removeByItem(itemClone);
						else rmp.claim(rmStash, ClaimType.NONE, itemClone);
						break;
					}
					break;
			}
			rmStash.showChanged(rmp);
		}
	}
	
	//Claim
	public void claimFound(GamePlayer rmp, ItemStack... items){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		rmp.claim(_config.getFound(), ClaimType.FOUND, items);
	}
	
	public void claimFoundToChest(Block b, GamePlayer rmp, ItemStack... items){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP){
			rmp.sendMessage(Text.getLabel("claim.found.cannot_while_ingame"));
			return;
		}
		rmp.claimToChest(b, ClaimType.FOUND, null, items);
	}
	
	//Check Equal Distribution
	public List<String> checkRewardEqual(GamePlayer rmp){
		return checkRewardEqualDistribution(rmp, _config.getReward());
	}
	public List<String> checkToolsEqual(GamePlayer rmp, List<GamePlayer> rmPlayers){
		return checkEqualDistribution(rmp, rmPlayers, _config.getTools());
	}
	
	public List<String> checkRewardEqualDistribution(GamePlayer rmp, Stash rmStash){
		List<String> strUnequal = new ArrayList<String>();
		if(rmStash.size()>0){
			for(Team rmTeam : _config.getTeams()){
				if(rmTeam!=null){
					int numOfPlayers = rmTeam.getPlayers().length;
					if(numOfPlayers>0){
						String strItems = "";
						for(StashItem rmStashItem : rmStash.values()){
							int amount = rmStashItem.getAmount();
							if(amount%numOfPlayers!=0){
								strItems+=Material.getMaterial(rmStashItem.getId()).name()+":"+ChatColor.GRAY+amount+ChatColor.WHITE+", ";
							}
						}
						if(strItems.length()>0){
							strItems = TextHelper.stripLast(strItems, ", ");
							strUnequal.add(rmTeam.getTeamColorString()+":["+ChatColor.WHITE+strItems+Helper.getChatColorByDye(rmTeam.getTeamColor())+"]");
						}
					}
				}
			}
		}
		return strUnequal;
	}
	
	public List<String> checkEqualDistribution(GamePlayer rmp, List<GamePlayer> rmPlayers, Stash rmStash){
		List<String> strUnequal = new ArrayList<String>();
		if(rmStash.size()>0){
			int numOfPlayers = rmPlayers.size();
			if(numOfPlayers>0){
				String strItems = "";
				for(StashItem rmStashItem : rmStash.values()){
					int amount = rmStashItem.getAmount();
					if(amount%numOfPlayers!=0){
						strItems+=Material.getMaterial(rmStashItem.getId()).name()+":"+ChatColor.GRAY+amount+ChatColor.WHITE+", ";
					}
				}
				if(strItems.length()>0){
					strItems = TextHelper.stripLast(strItems, ", ");
					strUnequal.add("["+ChatColor.WHITE+strItems+"]");
				}
			}
		}
		return strUnequal;
	}
	
	//Distribution
	public void distributeReward(Team rmTeam){
		distributeStash(rmTeam, ClaimType.REWARD);
	}
	public void distributeTools(Team rmTeam){
		distributeStash(rmTeam, ClaimType.TOOLS);
	}
	
	public void distributeStash(Team rmTeam, ClaimType claimType){
		Stash stash = new Stash();
		switch(claimType){
			case REWARD:
				if(_config.getSettingBool(Setting.infinitereward)) stash = _config.getReward().clone();
				else stash = _config.getReward();
				break;
			case TOOLS:
				if(_config.getSettingBool(Setting.infinitetools)) stash = _config.getTools().clone();
				else stash = _config.getTools();
				break;
		}
		if(stash.size()==0) return;
		if(rmTeam!=null) distributeStashToTeamDivide(rmTeam, stash, claimType);
		else distributeStashToTeamsDivide(stash,claimType);
	}
	
	public void distributeStashToTeamDivide(Team rmTeam, Stash stash, ClaimType claimType){
		int divisor = rmTeam.getPlayers().length;
		for(GamePlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	public void distributeStashToTeamsDivide(Stash stash, ClaimType claimType){
		GamePlayer[] players = getTeamPlayers();
		int divisor = players.length;
		for(GamePlayer rmp : players){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	
	public void distributeFromChests(Team rmTeam, ClaimType claimType){
		if(rmTeam==null) return;
		Stash stash = new Stash();
		for(GameChest rmChest : getChests()){
			stash.addItems(rmChest.getContents());
			rmChest.clearContents();
		}
		int divisor = rmTeam.getPlayers().length;
		for(GamePlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	public void distributeFromChest(GameChest rmChest, ClaimType claimType){
		Team rmTeam = rmChest.getTeam();
		if(rmTeam==null) return;
		Stash stash = new Stash(rmChest.getContents());
		rmChest.clearContents();
		int divisor = rmTeam.getPlayers().length;
		for(GamePlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	
	public List<ItemStack> distributeByDivisor(Stash stash, int divisor){
		List<ItemStack> foundItems = new ArrayList<ItemStack>();
		StashItem[] stashItems = stash.values().toArray(new StashItem[stash.values().size()]).clone();
		
		for(StashItem item : stashItems){
			int amount = (int)Math.ceil((double)item.getAmount()/(double)divisor);
			foundItems.addAll(stash.removeByIdAmount(item.getId(), amount));
		}
		return foundItems;
	}
	
	public void stashChestsContents(){
		for(GameChest rmChest : getChests()){
			rmChest.addInventoryToStash();
		}
	}
	
	public void returnChestsContents(){
		for(GameChest rmChest : getChests()){
			rmChest.returnInventoryFromStash();
		}
	}
	
	public void snatchInventories(){
		for(GamePlayer rmp : getTeamPlayers()){
			rmp.setInventoryState(new InventoryState(rmp.getInventory()));
			rmp.getInventory().clear();
			//rmp.addItemsFromInventory(rmp.getItems(), true);
		}
	}
	
	public void returnInventories(){
		for(GamePlayer rmp : getTeamPlayers()){
			rmp.getInventoryState().updatePlayerInventory(rmp.getInventory());
			rmp.clearInventoryState();
			//rmp.claimItems();
		}
	}
	
	public void healPlayers(){
		for(GamePlayer rmp : getTeamPlayers()){
			rmp.restoreHealth();
		}
	}
	
	public void warpPlayersToGame(){
		for(GamePlayer rmp : getTeamPlayers()){
			rmp.warpToTeam();
		}
	}
	
	public Team findWinningTeam(){
		for(GameChest rmChest : getChests()){
			if(rmChest.getItemsLeftInt()==0){
				return rmChest.getTeam();
			}
		}
		return null;
	}

	public Team findLeadingTeam(){
		List<Team> rmTeams = getTeams();
		List<Team> leading = new ArrayList<Team>();
		List<Team> added = new ArrayList<Team>();
		Debug.warning("rmTeams.size: "+rmTeams.size());
		Team leadingTeam = null;
		for(Team team : rmTeams){
			if(leading.size()==0){
				Debug.warning("Team: "+team.getTeamColorString());
				leading.add(team);
			}
			else{
				Debug.warning("Team: "+team.getTeamColorString());
				Debug.warning("leading.size: "+leading.size());
				Iterator<Team> iter = leading.iterator();
				while(iter.hasNext()){
					Team found = iter.next();
					int teamTotalLeft = team.getChest().getTotalLeft();
					int foundTotalLeft = found.getChest().getTotalLeft();
					Debug.warning("Found.totalLeft: "+foundTotalLeft);
					Debug.warning("newTeam.totalLeft: "+teamTotalLeft);
					if(teamTotalLeft<foundTotalLeft){
						Debug.warning("removeFoundTeam");
						iter.remove();
					}
					if(teamTotalLeft<=foundTotalLeft){
						Debug.warning("newTeamAdded");
						if(!added.contains(team)) added.add(team);
					}
				}
				Debug.warning("leading.size: "+leading.size());
				leading.addAll(added);
				added.clear();
				Debug.warning("leading.size: "+leading.size());
			}
		}
		Debug.warning("leading.size: "+leading.size());
		if(leading.size()==1){
			Debug.warning("Found LEADING!!!");
			leadingTeam = leading.get(0);
		}
		return leadingTeam;
	}
	
	public boolean hasMinimumPlayers(boolean announce){
		int minPlayers = _config.getSettingInt(Setting.minplayers);
		if(minPlayers<_config.getTeams().size()) minPlayers = _config.getTeams().size(); 
		if(getTeamPlayers().length<minPlayers){
			if(announce) broadcastMessage(Text.getLabelArgs("game.not_enough_players", ""+_config.getSettingInt(Setting.minplayers)));
			return false;
		}
		return true;
	}
	
	public boolean hasMinimumTeamPlayers(boolean announce){
		for(Team rmTeam : _config.getTeams()){
			if(rmTeam.getPlayers().length<_config.getSettingInt(Setting.minteamplayers)){
				//rmp.sendMessage("Each team must have at least one player.");
				if(announce) broadcastMessage(Text.getLabelArgs("game.not_enough_team_players", ""+_config.getSettingInt(Setting.minteamplayers)));
				return false;
			}
		}
		return true;
	}
	
	public void checkPlayerQuit(GamePlayer rmp, Team rmTeam){
		if(!rmTeam.hasMininumPlayers()){
			rmTeam.isDisqualified(true);
			rmTeam.clearPlayers();
			rmTeam.teamMessage(Text.getLabel("team.disqualified"));
			teamBroadcastMessage(Text.getLabelArgs("team.disqualified.broadcast", rmTeam.getTeamColorString()), rmTeam);
		}
		if(!hasMinimumPlayers(false)){
			Team winningTeam = null;
			for(Team rmt : getTeams()){
				if(rmt!=rmTeam){
					winningTeam = rmt;
				}
			}
			setWinningTeam(winningTeam);
			gameOver();
		}
	}
	
	public void toggleReady(GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.ready")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_command"));
			return;
		}
		rmp.toggleReady();
		if(rmp.getReady()){
			rmp.sendMessage(Text.getLabel("ready"));
			teamBroadcastMessage(Text.getLabelArgs("ready.broadcast", rmp.getTeam().getChatColor()+rmp.getName()), rmp);
		}
		else{
			rmp.sendMessage(Text.getLabel("ready.not"));
			teamBroadcastMessage(Text.getLabelArgs("ready.not.broadcast", rmp.getTeam().getChatColor()+rmp.getName()), rmp);
		}
	}
	
	public void checkReady(){
		if(!hasMinimumPlayers(false)) return;
		if(!hasMinimumTeamPlayers(false)) return;
		GamePlayer[] players = getTeamPlayers();
		for(GamePlayer rmp : players){
			if(!rmp.getReady()) return;
		}
		clearReady();
		startGame(null);
	}
	
	public void clearReady(){
		GamePlayer[] players = getTeamPlayers();
		for(GamePlayer rmp : players){
			rmp.setReady(false);
		}
	}
	
	public void initGameStart(){
		stashChestsContents();
		//Clear player's inventory
		if(_config.getSettingBool(Setting.clearinventory)) snatchInventories();
		if(_config.getSettingBool(Setting.healplayer)) healPlayers();
		_config.getTimer().resetDefaults();
		_config.getTimer().addTimeMessage(_config.getTimer().getTimeLimit());
		_config.getPvpTimer().reset();
		
		if((!_config.getSettingBool(Setting.infinitetools))||(_config.getSettingBool(Setting.dividetools))) distributeTools(null);
		else{
			for(GamePlayer rmp : getTeamPlayers()){
				_config.getTools().transferTo(rmp.getTools(), true);
			}
		}
		for(GamePlayer rmp : getTeamPlayers()){
			rmp.claimTools();
		}
		teamBroadcastMessage(Text.getLabel("game.start_match"));
		for(Team rmt : getTeams()){
			for(GamePlayer rmp : rmt.getPlayers()){
				rmp.getTools().transferTo(rmp.getItems());
				updateGameplayInfo(rmp, rmt);
				if(!rmp.isOnline()) rmp.setUpdateInventory(true);
			}
		}
		warpPlayersToGame();
		//if(_config.getPvpTimer().getTimeLimit()!=0) teamBroadcastMessage(RMText.getLabelArgs("game.pvp.delay", _config.getPvpTimer().getTextTimeRemaining()));
		updateSigns();
	}
	
	public boolean checkStartConditions(GamePlayer rmp){
		String rewardNotEqual = "";
		String toolsNotEqual = "";
		if((!_config.getSettingBool(Setting.infinitereward))||(_config.getSettingBool(Setting.dividereward))) rewardNotEqual = TextHelper.getStringByStringList(checkRewardEqual(rmp), ", ");
		if((!_config.getSettingBool(Setting.infinitetools))||(_config.getSettingBool(Setting.dividetools))) toolsNotEqual = TextHelper.getStringByStringList(checkToolsEqual(rmp, Arrays.asList(getTeamPlayers())), ", ");
		//Warn unequal
		if((_config.getSettingBool(Setting.warnunequal))||(!_config.getSettingBool(Setting.allowunequal))){
			if(rewardNotEqual.length()!=0) broadcastInstead(rmp, Text.getLabelArgs("reward.unequal", rewardNotEqual));
			if(toolsNotEqual.length()!=0) broadcastInstead(rmp, Text.getLabelArgs("tools.unequal", toolsNotEqual));
		}
		
		//Allow unequal
		if(!_config.getSettingBool(Setting.allowunequal)){
			boolean itemsNotEqual = false;
			if(rewardNotEqual.length()!=0){
				broadcastInstead(rmp, Text.getLabel("reward.cannot_be_equal"));
				itemsNotEqual = true;
			}
			if(toolsNotEqual.length()!=0){
				broadcastInstead(rmp, Text.getLabel("tools.cannot_be_equal"));
				itemsNotEqual = true;
			}
			if(itemsNotEqual){
				broadcastInstead(rmp, Text.getLabel("common.canceled"));
				return false;
			}
		}
		else{
			if(rewardNotEqual.length()!=0){
				teamBroadcastMessage(Text.getLabel("reward.will_not_be_equal"));
			}
			if(toolsNotEqual.length()!=0){
				teamBroadcastMessage(Text.getLabel("tools.will_not_be_equal"));
			}
		}
		if(!hasMinimumPlayers(true)) return false;
		if(!hasMinimumTeamPlayers(true)) return false;
		return true;
	}
	
	public void startGame(GamePlayer rmp){
		startGame(rmp, 0);
	}
	
	public void startGame(GamePlayer rmp, int random){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
		}
		
		/*
		if(_config.getState()!=GameState.SETUP){
			rmp.sendMessage("Please use "+ChatColor.GOLD+"restart "+ChatColor.WHITE+"game instead.");
			return;
		}
		*/
		if(_config.getState()!=GameState.SETUP){
			rmp.sendMessage(Text.getLabel("start.stop_game_first"));
			return;
		}
		for(GameChest rmChest : getChests()){
			rmChest.clearItems();
		}
		//Populate items by filter
		_config.getItems().populateByFilter(_config.getFilter());
		
		//Filter is empty
		if(_config.getItems().size()==0){
			broadcastInstead(rmp, Text.getLabel("filter.configure"));
			return;
		}

		_config.getItems().multiplyItemAmount(calculateMultiplierByPlayers());
		
		if(!checkStartConditions(rmp)) return;
		
		//Randomize
		if(random>0) _config.getItems().randomize(random);
		else _config.getItems().randomize(_config.getSettingInt(Setting.random));

		teamBroadcastMessage(Text.getLabel("game.prepare"));
		clearReady();
		_config.setState(GameState.COUNTDOWN);
	}
	
	public float calculateMultiplierByPlayers(){
		IntRange multiplier = _config.getSettingIntRange(Setting.multiplier);
		int min = _config.getSettingInt(Setting.minplayers);
		int max = _config.getSettingInt(Setting.maxplayers);
		if((max==0)||(!multiplier.hasHigh())) return multiplier.getLow();
		float high = multiplier.getHigh();
		float low = multiplier.getLow();
		float players = getTeamPlayers().length;
		
		float result = (high-low)/(max-min);
		result = (players-2)*result+low;
		Debug.warning("CalculateMultiplierResult: "+result);
		return result;
	}
	/*
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
	*/
	public void stopGame(GamePlayer rmp, boolean clearRandom){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		switch(_config.getState()){
			case GAMEPLAY:
				//rmp.sendMessage("Stopping game...");
				broadcastMessage(Text.getLabel("stop.stopping_game"));
				initGameOver();
				return;
			case SETUP: rmp.sendMessage(Text.getLabel("msg.no_game_in_progress")); return;
			case COUNTDOWN: rmp.sendMessage(Text.getLabel("game.countdown.wait_to_start")); return;
		}
	}
	
	private void initGameOver(){
		for(GameChest rmChest : getChests()){
			_config.getFound().addItems(rmChest.getItems());
			rmChest.clearItems();
			//distributeFromChest(rmChest, ClaimType.ITEMS);
			if(_config.getSettingBool(Setting.keepoverflow)) distributeFromChest(rmChest, ClaimType.ITEMS);
			else{
				_config.getFound().addItems(rmChest.getContents());
				rmChest.clearContents();
				for(GamePlayer rmp : rmChest.getTeam().getPlayers()){
					rmp.addItemsFromInventory(_config.getFound(), true);
				}
			}
		}
		if(_config.getSettingBool(Setting.clearinventory)) returnInventories();
		returnChestsContents();
		if(!_config.getSettingBool(Setting.autoreturn)) warpPlayersToGame();
		_config.setState(GameState.SETUP);
		if(_config.getSettingBool(Setting.autoreturn)) warpPlayersToReturnLocation();
		if(_config.getSettingBool(Setting.restore)) restoreLog();
		if(_config.getSettingBool(Setting.scrapfound)) _config.getFound().clear();
		_config.getTimer().reset();
		_config.getPvpTimer().reset();
		for(GamePlayer rmp : getTeamPlayers()){
			if(!rmp.isOnline()) rmp.setUpdateInventory(true);
		}
		clearTeamPlayers();
		updateSigns();
	}
	
	public void warpPlayersToReturnLocation(){
		for(GamePlayer rmp : getTeamPlayers()){
			rmp.warpToReturnLocation();
		}
	}
	
	public void gameOver(){
		Team rmTeam = getWinningTeam();
		if(rmTeam==null) return;
		_config.setState(GameState.GAMEOVER);
		teamBroadcastMessage(Text.getLabelArgs("team.win_match.broadcast", rmTeam.getTeamColorString()));
		for(GameChest rmChest : getChests()){
			_config.getFound().addItems(rmChest.getItems());;
			rmChest.clearItems();
			//distributeFromChest(rmChest, ClaimType.ITEMS);
			if(_config.getSettingBool(Setting.keepoverflow)) distributeFromChest(rmChest, ClaimType.ITEMS);
			else{
				_config.getFound().addItems(rmChest.getContents());
				rmChest.clearContents();
				for(GamePlayer rmp : rmChest.getTeam().getPlayers()){
					rmp.addItemsFromInventory(_config.getFound(), true);
				}
			}
		}
		
		for(Team rmt : getTeams()){
			if(rmt!=rmTeam){
				for(GamePlayer rmPlayer : rmt.getPlayers()){
					rmPlayer.getTools().transferTo(rmPlayer.getItems());
					rmPlayer.addStat(RMStat.LOSSES);
					_config.getStats().add(RMStat.LOSSES);
					Stats.add(RMStatServer.LOSSES);
				}
			}
		}
		for(GamePlayer rmPlayer : rmTeam.getPlayers()){
			rmPlayer.getTools().transferTo(rmPlayer.getItems());
			rmPlayer.addStat(RMStat.WINS);
			_config.getStats().add(RMStat.WINS);
			Stats.add(RMStatServer.WINS);
		}
		if((!_config.getSettingBool(Setting.infinitereward))||(_config.getSettingBool(Setting.dividereward))) distributeReward(rmTeam);
		else{
			for(GamePlayer rmp : rmTeam.getPlayers()){
				_config.getReward().transferTo(rmp.getReward(), true);
			}
		}
		if(_config.getSettingBool(Setting.foundasreward)) distributeStashToTeamDivide(rmTeam, _config.getFound(), ClaimType.REWARD);
		//setWinPlayer(rmp);
		update();
		initGameOver();
	}
	
	//UPDATE
	public void update(){
		switch(_config.getState()){
		case SETUP:
			checkReady();
			break;
		case COUNTDOWN:
			//cdTimer = 0; //////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(_cdTimer%10==0){
				if(_cdTimer!=0){
					teamBroadcastMessage(Text.getLabelArgs("game.countdown", ""+_cdTimer/10));
					updateSigns();
				}
			}
			if(_cdTimer>0){
				_cdTimer-=10;
			}
			else{
				_cdTimer = _cdTimerLimit;
				_config.setState(GameState.GAMEPLAY);
				initGameStart();
			}
			break;
		case GAMEPLAY:
			if(getOnlineTeamPlayers().length==0) return;
			GamePlayer[] players = getOnlineTeamPlayers();
			
			for(GamePlayer rmp : players){
				//rmp.getTeamInfo(0);
				if(rmp.getTools().size()>0) rmp.claimTools();
			}
			
			int safeZoneRadius = getGame().getGameConfig().getSettingInt(Setting.safezone);
			if(safeZoneRadius!=0){
				for(GamePlayer rmp : players){
					rmp.checkInSafeZone(safeZoneRadius);
				}
			}
			
			int enemyRadarRadius = getGame().getGameConfig().getSettingInt(Setting.enemyradar);
			if(enemyRadarRadius!=0){
				for(GamePlayer rmp : players){
					rmp.checkDetectedEnemy(enemyRadarRadius);
				}
			}
			
			int playAreaRadius = getGame().getGameConfig().getSettingInt(Setting.playarea);
			if(playAreaRadius!=0){
				for(GamePlayer rmp : players){
					rmp.checkPlayArea(playAreaRadius);
				}
			}
			
			Timer timer = _config.getTimer();
			if(timer.getTimeLimit()!=0){
				if(timer.getTimeElapsed()<timer.getTimeLimit()){
					timer.announceTimeLeft(this);
					timer.addTimeElapsed();
				}
				else if(timer.getTimeElapsed()==timer.getTimeLimit()){
					timer.announceTimeLeft(this);
					Debug.warning("findLeadingTeam");
					setWinningTeam(findLeadingTeam());
					gameOver();
					if(getWinningTeam()==null){
						teamBroadcastMessage(Text.getLabelColorize("game.sudden_death", ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE));
						timer.addTimeElapsed();
					}
				}
				else{
					setWinningTeam(findLeadingTeam());
					gameOver();
				}
			}
			
			Timer pvpTimer = _config.getPvpTimer();
			if(pvpTimer.getTimeLimit()!=0){
				if(pvpTimer.getTimeElapsed()<pvpTimer.getTimeLimit()){
					pvpTimer.announceTimeLeft(this);
					pvpTimer.addTimeElapsed();
				}
				else if(pvpTimer.getTimeElapsed()==pvpTimer.getTimeLimit()){
					teamBroadcastMessage(Text.getLabel("game.pvp.enabled"));
					pvpTimer.addTimeElapsed();
				}
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
					
					for(Team rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						if(sign==null) continue;
						sign.setLine(0, getSignString(Text.getLabel("sign.filter"), ": "+items));
						sign.setLine(1, getSignString(Text.getLabel("sign.total"), ": "+lineTotal));
						sign.setLine(2, getSignString(Text.getLabel("sign.ingame"), ": "+getTeamPlayers().length+getTextPlayersOfMax()));
						sign.setLine(3, getSignString(Text.getLabel("sign.inteam"), ": "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax()));
						sign.update();
					}
					break;
				case REWARD:
					for(Team rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						if(sign==null) continue;
						sign.setLine(0, getSignString(Text.getLabel("sign.reward"), ": "+_config.getReward().size()));//+items);
						sign.setLine(1, getSignString(Text.getLabel("sign.total"), ": "+_config.getReward().getAmount()));//lineTotal);
						sign.setLine(2, getSignString(Text.getLabel("sign.ingame"), ": "+getTeamPlayers().length+getTextPlayersOfMax()));
						sign.setLine(3, getSignString(Text.getLabel("sign.inteam"), ": "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax()));
						sign.update();
					}
					break;
				case TOOLS:
					for(Team rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						if(sign==null) continue;
						sign.setLine(0, getSignString(Text.getLabel("sign.tools"), ": "+_config.getTools().size()));//+items);
						sign.setLine(1, getSignString(Text.getLabel("sign.total"), ": "+_config.getTools().getAmount()));//lineTotal);
						sign.setLine(2, getSignString(Text.getLabel("sign.ingame"), ": "+getTeamPlayers().length+getTextPlayersOfMax()));
						sign.setLine(3, getSignString(Text.getLabel("sign.inteam"), ": "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax()));			
						sign.update();
					}
					break;
				case FILTER_CLEAR: case REWARD_CLEAR: case TOOLS_CLEAR:
					for(Team rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						if(sign==null) continue;
						sign.setLine(0, Text.getLabel("sign.clear0"));
						sign.setLine(1, Text.getLabel("sign.clear1"));
						sign.setLine(2, Text.getLabel("sign.clear2"));
						sign.setLine(3, Text.getLabel("sign.clear3"));
						sign.update();
					}
					break;
				}
				break;
			case COUNTDOWN:
				for(Sign sign : getSigns()){
					if(sign==null) continue;
					sign.setLine(0, Text.getLabelArgs("sign.countdown0", ""+_cdTimer/10));
					sign.setLine(1, Text.getLabelArgs("sign.countdown1", ""+_cdTimer/10));
					sign.setLine(2, Text.getLabelArgs("sign.countdown2", ""+_cdTimer/10));
					sign.setLine(3, Text.getLabelArgs("sign.countdown3", ""+_cdTimer/10));
					sign.update();
				}
				break;
			case GAMEPLAY:
				for(Team rmTeam : getTeams()){
					Sign sign = rmTeam.getSign();
					if(sign==null) continue;
					GameChest rmChest = rmTeam.getChest();
					if(!rmTeam.isDisqualified()){
						sign.setLine(0, getSignString(Text.getLabel("sign.items_left"), ": "+rmChest.getItemsLeftInt()));
						sign.setLine(1, getSignString(Text.getLabel("sign.total"), ": "+rmChest.getTotalLeft()));
						sign.setLine(2, getSignString(Text.getLabel("sign.ingame"), ": "+getTeamPlayers().length+getTextPlayersOfMax()));
						sign.setLine(3, getSignString(Text.getLabel("sign.inteam"), ": "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax()));
					}
					else{
						sign.setLine(0, Text.getLabel("sign.team_disqualified0"));
						sign.setLine(1, Text.getLabel("sign.team_disqualified1"));
						sign.setLine(2, Text.getLabel("sign.team_disqualified2"));
						sign.setLine(3, Text.getLabel("sign.team_disqualified3"));
					}
					sign.update();
				}
				break;
			case GAMEOVER:
				break;
			case PAUSED:
				for(Team rmTeam : getTeams()){
					Sign sign = rmTeam.getSign();
					if(sign==null) continue;
					sign.setLine(0, Text.getLabel("sign.game_paused0"));
					sign.setLine(1, Text.getLabel("sign.game_paused1"));
					sign.setLine(2, Text.getLabel("sign.game_paused2"));
					sign.setLine(3, Text.getLabel("sign.game_paused3"));
					sign.update();
				}
				break;
		}
	}
	
	public void undo(GamePlayer rmp){
		if(rmp.getRequestFilterBackup()==null){
			rmp.sendMessage(ChatColor.GRAY+"Cannot restore filter.");
			return;
		}
		rmp.setRequestFilter(rmp.getRequestFilterBackup().clone());
		tryParseFilter(getMainBlock(), rmp);
		rmp.sendMessage(ChatColor.YELLOW+"Filter restored.");
	}
	
	public void sendFilterInfo(GamePlayer rmp){
		updateSigns();
		String items = "";
		Integer[] array = _config.getFilter().keySet().toArray(new Integer[_config.getFilter().keySet().size()]);
		Arrays.sort(array);
		if(array.length>rm.config.getTypeLimit()){
			for(Integer id : array){
				items += ChatColor.WHITE+""+id+TextHelper.includeItem(_config.getFilter().getItem(id))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer id : array){
				items += ChatColor.WHITE+""+Material.getMaterial(id)+TextHelper.includeItem(_config.getFilter().getItem(id))+ChatColor.WHITE+", ";
			}
		}
		if(items.length()>0){
			items = items.substring(0, items.length()-2);
			rmp.sendMessage(Text.getLabelArgs("filter.items", items));
		}
		else rmp.sendMessage(Text.getLabel("filter.empty"));
	}
	
	public void sendFilterInfoString(GamePlayer rmp){
		rmp.sendMessage(ChatColor.AQUA+Filter.encodeFilterToString(getGameConfig().getFilter().getItems(), false));
	}
	
	public void sendRewardInfo(GamePlayer rmp){
		updateSigns();
		String items = TextHelper.getStringSortedItems(_config.getReward().getItems());
		if(items.length()>0){
			rmp.sendMessage(Text.getLabelArgs("reward.items", items));
		}
		else rmp.sendMessage(Text.getLabel("reward.empty"));
	}
	
	public void sendRewardInfoString(GamePlayer rmp){
		rmp.sendMessage(ChatColor.AQUA+getGameConfig().getReward().encodeToString(false));
	}
	
	public void sendToolsInfo(GamePlayer rmp){
		updateSigns();
		String items = TextHelper.getStringSortedItems(_config.getTools().getItems());
		if(items.length()>0){
			rmp.sendMessage(Text.getLabelArgs("tools.items", items));
		}
		else rmp.sendMessage(Text.getLabel("tools.empty"));
	}
	
	public void sendToolsInfoString(GamePlayer rmp){
		rmp.sendMessage(ChatColor.AQUA+getGameConfig().getTools().encodeToString(false));
	}
	
	public void trySignGameplayInfo(Block b, GamePlayer rmp){
		Team rmTeam = getTeamByBlock(b);
		GamePlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
		if((rmTeam!=null)&&(rmPlayer!=null)){
			updateGameplayInfo(rmp, rmTeam);
			updateSigns();
		}
		else if(rmTeam!=rmp.getTeam()){
			updateGameplayInfo(rmp, rmTeam);
			updateSigns();
		}
	}
	
	public void updateGameplayInfo(GamePlayer rmp, Team rmTeam){
		updateGameplayInfo(rmp, rmTeam, rm.config.getTypeLimit());
	}
	
	public void updateGameplayInfo(GamePlayer rmp, Team rmTeam, int typeLimit){
		//RMTeam rmTeam = rmp.getTeam();
		String strItems = "";
		GameChest rmChest = rmTeam.getChest();
		
		//Sort
		Integer[] array = _config.getItems().keySet().toArray(new Integer[_config.getItems().keySet().size()]);
		Arrays.sort(array);
		
		HashMap<Integer, Item> items = rmChest.getRMItems();
		
		if(array.length>typeLimit){
			for(Integer id : array){
				Item rmItem = _config.getItems().getItem(id);
				int amount = rmItem.getAmount();
				if(items.containsKey(id)) amount -= items.get(id).getAmount();
				if(amount>0) strItems += ChatColor.WHITE+""+id+TextHelper.includeItem(new Item(id, amount))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer id : array){
				Item rmItem = _config.getItems().getItem(id);
				int amount = rmItem.getAmount();
				if(items.containsKey(id)) amount -= items.get(id).getAmount();
				if(amount>0) strItems += ChatColor.WHITE+""+Material.getMaterial(id)+TextHelper.includeItem(new Item(id, amount))+ChatColor.WHITE+", ";
			}
		}
		if(strItems.length()>0){
			strItems = strItems.substring(0, strItems.length()-2);
			rmp.sendMessage(Text.getLabelArgs("items.left", strItems));
		}
		else rmp.sendMessage(Text.getLabel("items.empty"));
	}
	
	//Get items from chests
	public List<ItemStack> getFilterItemsFromChests(GameChest... rmChests){
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(GameChest rmChest : rmChests){
			ItemStack[] chestItems = rmChest.getChest().getInventory().getContents();
			items.addAll(Arrays.asList(chestItems));
		}
		/*
		if(_config.getAddOnlyOneStack()) items = removeDuplicates(items);
		else{
			if(_config.getAddWholeStack()) items = addDuplicates(items, true);
			else items = addDuplicates(items, false);
		}
		*/
		
		items = addDuplicates(items, false);
		if(_config.getSettingBool(Setting.warnhacked)) warnHackMaterialsByItems(items);
		if(!_config.getSettingBool(Setting.allowhacked)) items = removeHackMaterialsByItems(items);
		return items;
	}
	
	//Try Add Items
	public void tryAddItemsToFilter(Block b, GamePlayer rmp, ClickState clickState){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			List<ItemStack> items = new ArrayList<ItemStack>();
			boolean force;
			if(clickState!=ClickState.NONE){
				items = getFilterItemsFromChests(getChestByBlock(b));
				force = true;
			}
			else{
				items = getFilterItemsFromChests(getChestByBlock(b));
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
								Item rmItem = new Item(id);
								/*
								if(_config.getAddOnlyOneStack()) rmItem.setAmount(rmItem.getMaxStackSize());
								else rmItem.setAmount(is.getAmount());
								*/
								rmItem.setAmount(is.getAmount());
								switch(clickState){
									case NONE: case LEFT:
										switch(_config.getFilter().addItem(id, rmItem, force)){
											case ADD:
												addedItems+=ChatColor.WHITE+item.name()+TextHelper.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+TextHelper.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
										}
										break;
									case RIGHT:
										switch(_config.getFilter().removeItem(id, rmItem, force)){
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+TextHelper.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case REMOVE:
												removedItems+=ChatColor.WHITE+item.name()+TextHelper.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE+", ";
												break;
										}
										break;
								}
							}
						}
					}
				}
				if(addedItems.length()>0){
					addedItems = TextHelper.stripLast(addedItems, ",");
					rmp.sendMessage(Text.getLabelArgs("common.added", addedItems));
				}
				if(modifiedItems.length()>0){
					modifiedItems = TextHelper.stripLast(modifiedItems, ",");
					rmp.sendMessage(Text.getLabelArgs("common.modified", modifiedItems));
				}
				if(removedItems.length()>0){
					removedItems = TextHelper.stripLast(removedItems, ",");
					rmp.sendMessage(Text.getLabelArgs("common.removed", removedItems));
				}
			}
			else if(rmp.isSneaking()){
				if(clickState!=ClickState.NONE){
					if(rmp.hasPermission("resourcemadness.filter.byhand")) tryAddItemToFilter(rmp, clickState);
					else rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				}
			}
			else if(_config.getFilter().size()>0){
				if(rmp.hasPermission("resourcemadness.filter.clear")){
					if(clickState == ClickState.NONE){
						rmp.sendMessage(Text.getLabel("sign.clear_items"));
						_config.setInterface(InterfaceState.FILTER_CLEAR);
					}
				}
				else rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			}
			updateSigns();
		}
	}
	
	public void tryAddItemToFilter(GamePlayer rmp, ClickState clickState){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			ItemStack item = rmp.getPlayer().getItemInHand();
			
			boolean force;
			if(clickState!=ClickState.NONE) force = true;
			else force = false;
			
			if(item!=null){
				int id = item.getTypeId();
				Material mat = item.getType();
				if(!Helper.isMaterial(mat, _hackMaterials)){
					Item rmItem;
					int amount = 1;
					rmItem = new Item(id, amount);
					switch(clickState){
					case NONE: case LEFT:
						switch(_config.getFilter().addItem(id, rmItem, force)){
							case ADD:
								rmp.sendMessage(Text.getLabelArgs("common.added", ChatColor.WHITE+mat.name()+TextHelper.includeItem(rmItem)));
								break;
							case MODIFY:
								rmp.sendMessage(Text.getLabelArgs("common.modified", ChatColor.WHITE+mat.name()+TextHelper.includeItem(rmItem)));
								break;
						}
						break;
					case RIGHT:
						switch(_config.getFilter().removeItem(id, rmItem, force)){
							case MODIFY:
								rmp.sendMessage(Text.getLabelArgs("common.modified", ChatColor.WHITE+mat.name()+TextHelper.includeItem(rmItem)));
								break;
							case REMOVE:
								rmp.sendMessage(Text.getLabelArgs("common.removed", ChatColor.WHITE+mat.name()+TextHelper.includeItem(rmItem)));
								break;
						}
						break;
					}
					updateSigns();
				}
				else{
					_lastHackMaterials.clear();
					_lastHackMaterials.add(mat);
					if(_config.getSettingBool(Setting.warnhacked)) warnHackMaterials(_lastHackMaterials); 
				}
			}
		}
	}
	
	public void tryAddFoundItems(Block b, GamePlayer rmp){
		Team rmTeam = getTeamByBlock(b);
		if((rmTeam!=null)&&(rmTeam==rmp.getTeam())){
			GameChest rmChest = rmTeam.getChest();
			HashMap<Integer, Item> added = new HashMap<Integer, Item>();
			HashMap<Integer, Item> returned = new HashMap<Integer, Item>();
			int totalFound = 0;
			Inventory inv = rmChest.getChest().getInventory();
			returned = _config.getItems().cloneItems(-1);
			
			for(int i=0; i<inv.getSize(); i++){
				ItemStack item = inv.getItem(i);
				int id = -1;
				try{
					 id = item.getTypeId();
				}
				catch(Exception e){
					item = null;
				}
				if(item!=null){
					if(item.getType()!=Material.AIR){
						if(_config.getItems().containsKey(id)){
							int overflow = 0;
							overflow = rmChest.addItem(item);
							if(overflow!=item.getAmount()){
								Item rmItem;
								if(added.containsKey(id)){
									rmItem = added.get(id);
									rmItem.addAmount(item.getAmount()-overflow);
									added.put(id, rmItem);
								}
								else{
									rmItem = new Item(id, item.getAmount()-overflow);
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
			rmp.addStat(RMStat.ITEMS_FOUND_TOTAL, totalFound);
			_config.getStats().add(RMStat.ITEMS_FOUND_TOTAL, totalFound);
			Stats.add(RMStatServer.ITEMS_FOUND_TOTAL, totalFound);
			
			if(added.size()>0){
				if(returned.size()>0){
					rmp.getTeam().teamMessage(Text.getLabelArgs("items.left", getFormattedStringByHash(returned, rmp)));
					if(_config.getSettingBool(Setting.showitemsleft)) teamBroadcastMessage(Text.getLabelArgs("team.items_left.broadcast", rmp.getTeam().getTeamColorString(), ""+rmChest.getItemsLeftInt(), ""+rmChest.getTotalLeft()), rmp.getTeam());
				}
			}
			else updateGameplayInfo(rmp, rmTeam);
		}
		updateSigns();
	}
		
	public void handleRightClick(Block b, GamePlayer rmp){
		Material mat = b.getType();
		switch(_config.getState()){
			case SETUP:
				switch(_config.getInterface()){
					case FILTER:
						switch(mat){
						case CHEST:
							if(rmp.isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.RIGHT);
							break;
						case WALL_SIGN:
							if(rmp.isSneaking()) cycleMode(rmp, false);
							//trySignSetupInfo(rmp);
							break;
						case WOOL:
							//if(rmp.isSneaking()) joinQuitTeamByBlock(b, rmp, false);
							break;
						}
						break;
					case REWARD:
						switch(mat){
						case CHEST:
							if(rmp.isSneaking()) addRewardByChest(rmp, getChestByBlock(b), ClickState.RIGHT);
							break;
						case WALL_SIGN:
							if(rmp.isSneaking()) cycleMode(rmp, false);
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
							if(rmp.isSneaking()) addToolsByChest(rmp, getChestByBlock(b), ClickState.RIGHT);
							break;
						case WALL_SIGN:
							if(rmp.isSneaking()) cycleMode(rmp, false);
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
	
	public void handleLeftClick(Block b, GamePlayer rmp){
		Material mat = b.getType();
		switch(_config.getState()){
			// Setup State
			case SETUP:
				switch(_config.getInterface()){
				case FILTER: //MAIN
					switch(mat){
					case CHEST:
						if(!rmp.hasPermission("resourcemadness.filter")){
							rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
							return;
						}
						if(rmp.isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.isSneaking()) cycleMode(rmp);
						else sendFilterInfo(rmp);
						break;
					case WOOL:
						if(rmp.isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case REWARD:
					switch(mat){
					case CHEST:
						if(!rmp.hasPermission("resourcemadness.reward")){
							rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
							return;
						}
						//if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						//else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						if(rmp.isSneaking()) addRewardByChest(rmp, getChestByBlock(b), ClickState.LEFT);
						else addRewardByChest(rmp, getChestByBlock(b), ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.isSneaking()) cycleMode(rmp);
						else sendRewardInfo(rmp);
						break;
					case WOOL:
						if(rmp.isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case TOOLS:
					switch(mat){
					case CHEST:
						if(!rmp.hasPermission("resourcemadness.tools")){
							rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
							return;
						}
						//if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						//else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						if(rmp.isSneaking()) addToolsByChest(rmp, getChestByBlock(b), ClickState.LEFT);
						else addToolsByChest(rmp, getChestByBlock(b), ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.isSneaking()) cycleMode(rmp);
						else sendToolsInfo(rmp);
						break;
					case WOOL:
						if(rmp.isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case FILTER_CLEAR: case REWARD_CLEAR: case TOOLS_CLEAR: //FILTER CLEAR
					switch(mat){
					case CHEST: case GLASS: case STONE:
						if(rmp.hasOwnerPermission(_config.getOwnerName())){
							rmp.sendMessage(Text.getLabel("common.canceled"));
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
							rmp.sendMessage(Text.getLabel("common.canceled"));
							_config.setInterface(getParentInterface(_config.getInterface()));
							updateSigns();
						}
						else{
							if(rmp.isSneaking()) joinQuitTeamByBlock(b, rmp, false);
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
				case CHEST: case WALL_SIGN:
					break;
				case WOOL:
					sendTeamInfo(rmp);
					break;
				}
				break;
				
			// Gameplay State
			case GAMEPLAY:
				switch(mat){
				case CHEST:
					Team rmTeam = getTeamByBlock(b);
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
					if(rmp.isSneaking()) joinQuitTeamByBlock(b, rmp, false);
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
				rmp.sendMessage(Text.getLabel("game.is_paused"));
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
	
	//Game
	public Block getMainBlock(){
		return _config.getPartList().getMainBlock();
	}
	
	//Game GET-SET
	public Game getGame(){
		return this;
	}
	
	//Players
	public String getPlayersNames(){
		GamePlayer[] rmplayers = _config.getPlayers().values().toArray(new GamePlayer[_config.getPlayers().values().size()]);
		String names = "";
		for(GamePlayer rmp : rmplayers){
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
	
	public void broadcastInstead(GamePlayer rmp, String message){
		if(rmp!=null) rmp.sendMessage(message);
		else broadcastMessage(message);
	}

	public void broadcastMessage(String message){
		GamePlayer rmp = _config.getOwner();
		rmp.sendMessage(message);
		List<Team> teams = getTeams();
		for(Team rmt : teams){
			GamePlayer[] players = rmt.getPlayers();
			for(GamePlayer rmPlayer : players){
				if(rmp!=rmPlayer) rmPlayer.sendMessage(message);
			}
		}
	}
	public void broadcastMessage(String message, GamePlayer ignorePlayer){
		GamePlayer rmp = _config.getOwner();
		if((ignorePlayer==null)||(rmp!=ignorePlayer)) rmp.sendMessage(message);
		List<Team> teams = getTeams();
		for(Team rmt : teams){
			GamePlayer[] players = rmt.getPlayers();
			for(GamePlayer rmPlayer : players){
				if(rmp!=rmPlayer) if((ignorePlayer==null)||(rmPlayer!=ignorePlayer)) rmPlayer.sendMessage(message);
			}
		}
	}
	public void teamBroadcastMessage(String message){
		for(Team rmTeam : getTeams()){
			rmTeam.teamMessage(message);
		}
	}
	public void teamBroadcastMessage(String message, Team ignoreTeam){
		for(Team rmTeam : getTeams()){
			if(rmTeam!=ignoreTeam) rmTeam.teamMessage(message);
		}
	}
	public void teamBroadcastMessage(String message, GamePlayer ignorePlayer){
		for(Team rmTeam : getTeams()){
			rmTeam.teamMessage(message, ignorePlayer);
		}
	}
	//Team
	public void teamMessage(Team rmt, String message){
		if(rmt!=null) rmt.teamMessage(message);
	}
	public void teamMessage(Team rmt, String message, GamePlayer ignorePlayer){
		if(rmt!=null) rmt.teamMessage(message, ignorePlayer);
	}
	
	public void joinTeam(Team rmTeam, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.join")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		rmTeam.addPlayer(rmp);
	}
	public void quitTeam(Team rmTeam, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		rmTeam.removePlayer(rmp);
	}
	public void quitTeam(Game rmGame, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		Team rmTeam = rmGame.getTeamByPlayer(rmp);
		if(rmTeam!=null){
			rmTeam.removePlayer(rmp);
			//rmp.sendMessage("You quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team.");
		}
	}
	
	public Team joinTeamByBlock(Block b, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.join")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return null;
		}
		Team rmt = getTeamByBlock(b);
		if(rmt!=null){
			Team rmTeam = getTeamByPlayer(rmp);
			if(rmTeam!=null){
				if(rmt!=rmTeam){
					rmp.sendMessage(Text.getLabelArgs("join.must_quit_other_team", rmTeam.getTeamColorString()));
					return null;
				}
			}
			rmt.addRemovePlayer(rmp);
			return rmt;
		}
		else rmp.sendMessage(Text.getLabel("msg.team_does_not_exist"));
		return null;
	}
	
	public Team joinQuitTeamByBlock(Block b, GamePlayer rmp, boolean fromConsole){
		if(_config.getState() == GameState.SETUP){
			Team rmt = getTeamByBlock(b);
			if(rmt!=null){
				Team rmTeam = getTeamByPlayer(rmp);
				if(rmTeam!=null){
					if(rmt!=rmTeam){
						rmp.sendMessage(Text.getLabelArgs("join.must_quit_other_team", rmTeam.getTeamColorString()));
						return null;
					}
				}
				rmt.addRemovePlayer(rmp);
				return rmt;
			}
			else rmp.sendMessage(Text.getLabel("msg.team_does_not_exist"));
		}
		else{
			rmp.sendMessage("Teams: "+getTextTeamPlayers());
			if(rmp.isIngame()) rmp.sendMessage(Text.getLabel("quit.game_in_progress"));
			else rmp.sendMessage(Text.getLabel("join.game_in_progress"));
		}
		
		return null;
	}
	public Team quitTeamByBlock(Block b, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return null;
		}
		Team rmTeam = getTeamByBlock(b);
		if(rmTeam!=null){
			if(rmTeam.getPlayer(rmp.getName())!=null){
				rmTeam.removePlayer(rmp);
				rmp.sendMessage(Text.getLabelArgs("quit.alt", rmTeam.getTeamColorString()));
				return rmTeam;
			}
			else rmp.sendMessage(Text.getLabelArgs("quit.not_joined", rmTeam.getTeamColorString()));
		}
		return null;
	}
	public Team getTeamByBlock(Block b){
		if(Helper.isMaterial(b.getType(), Material.CHEST, Material.WALL_SIGN, Material.WOOL)){
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
	
	public Team getTeamByDye(String arg){
		DyeColor color = Helper.getDyeByString(arg);
		if(color!=null) return getTeam(color);
		return null;
	}
	
	public Team getTeamByPlayer(GamePlayer rmp){
		for(Team team : _config.getTeams()){
			if(team!=null){
				GamePlayer rmTeamPlayer = team.getPlayer(rmp.getName());
				if(rmTeamPlayer!=null) return team;
			}
		}
		return null;
	}
	
	private Team addTeam(Team rmt){
		if(!_config.getTeams().contains(rmt)){
			_config.getTeams().add(rmt);
			rmt.setGame(this);
		}
		return rmt;
	}
	public Team getTeam(int index){
		if(index<_config.getTeams().size()){
			if(_config.getTeams().get(index)!=null){
				return _config.getTeams().get(index);
			}
		}
		return null;
	}
	public Team getTeam(DyeColor color){
		for(Team rmTeam : _config.getTeams()){
			if(rmTeam.getTeamColor() == color){
				return rmTeam;
			}
		}
		return null;
	}
	
	public List<Team> getTeams(){
		return _config.getTeams();
	}
	
	public GamePlayer[] getTeamPlayers(){
		List<GamePlayer> result = new ArrayList<GamePlayer>();
		for(Team rmTeam : _config.getTeams()){
			for(GamePlayer rmPlayer : rmTeam.getPlayers()){
				result.add(rmPlayer);
			}
		}
		return result.toArray(new GamePlayer[result.size()]);
	}
	
	public String[] getTeamPlayersNames(){
		List<String> list = new ArrayList<String>();
		for(Team rmTeam : _config.getTeams()){
			for(GamePlayer rmPlayer : rmTeam.getPlayers()){
				list.add(rmPlayer.getName());
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	public GamePlayer[] getOnlineTeamPlayers(){
		List<GamePlayer> list = new ArrayList<GamePlayer>();
		for(Team rmTeam : _config.getTeams()){
			for(GamePlayer rmPlayer : rmTeam.getPlayers()){
				if(rmPlayer.isOnline()){
					list.add(rmPlayer);
				}
			}
		}
		return list.toArray(new GamePlayer[list.size()]);
	}
	
	public void clearTeamPlayers(){
		for(Team rmTeam : getTeams()){
			rmTeam.isDisqualified(false);
			rmTeam.clearPlayers();
		}
	}
	
	//Chest
	public GameChest getChest(int index){
		Team rmt = getTeam(index);
		return rmt.getChest();
	}
	public GameChest getChest(Team rmTeam){
		return rmTeam.getChest();
	}
	public GameChest[] getChests(){
		List<Team> rmTeams = getTeams();
		GameChest[] rmChests = new GameChest[rmTeams.size()];
		for(int i=0; i<rmTeams.size(); i++){
			rmChests[i] = rmTeams.get(i).getChest();
		}
		return rmChests;
	}
	
	private GameChest getChestByBlock(Block b){
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
		Team rmt = getTeam(index);
		return rmt.getSign();
	}
	public Sign getSign(Team rmTeam){
		return rmTeam.getSign();
	}
	public List<Sign> getSigns(){
		List<Sign> signList = new ArrayList<Sign>();
		for(Team rmt : getTeams()){
			signList.add(rmt.getSign());
		}
		return signList;
	}
	
	//String
	public String getTextTeamColors(){
		String line = "";
		for(Team team : _config.getTeams()){
			if(team.getTeamColor()!=null){
				line+=team.getTeamColorString()+ChatColor.WHITE+",";
			}
		}
		return line.substring(0,line.length()-1);
	}
	
	public GamePlayer[] getPlayersByTeam(Team rmt){
		if(rmt!=null) return rmt.getPlayers();
		return null;
	}
	
	public GamePlayer getPlayerByName(String name){
		for(GamePlayer rmPlayer : getTeamPlayers()){
			if(rmPlayer.getName().equalsIgnoreCase(name)) return rmPlayer;
		}
		return null;
	}
	
	public GamePlayer getTeamPlayerByName(Team rmTeam, String name){
		if(rmTeam!=null){
			for(GamePlayer rmPlayer : rmTeam.getPlayers()){
				if(rmPlayer.getName().equalsIgnoreCase(name)) return rmPlayer;
			}
		}
		return null;
	}
	
	public String getTextTeamPlayers(){
		String line = "";
		for(Team team : _config.getTeams()){
			if(team.getPlayers()==null) continue;
			line+=team.getTeamColorString()+":";
			line+=team.getPlayersNames()+" ";
		}
		
		if(line.length()>1) return line.substring(0,line.length()-1);
		return null;
	}
	
	public String getTextTeamPlayersNumbers(){
		String line = "";
		for(Team team : _config.getTeams()){
			if(team.getPlayers()==null) continue;
			line+=team.getTeamColorString()+":";
			line+="["+team.getPlayers().length+"] ";
		}
		
		if(line.length()>1) return line.substring(0,line.length()-1);
		return null;
	}

	//Try Parse Filter
	public Boolean tryParseFilter(Block b, GamePlayer rmp){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			Game.setRequestPlayer(rmp);
			parseFilter(b, rmp);
			Game.clearRequestPlayer();
			rmp.clearRequestFilter();
			updateSigns();
			return true;
		}
		else rmp.sendMessage(Text.getLabelArgs("filter.modify_owner", _config.getOwnerName()));
		return false;
	}
	
	private HashMap<Integer, Item> getItemsMatch(Inventory inv, HashMap<Integer, Item> hashItems){
		HashMap<Integer, Item> items = new HashMap<Integer, Item>();
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
						Item rmItem = new Item(item);
						rmItem.setAmount(rmItem.getAmount()+(overflow<0?overflow:0));
						items.put(id, rmItem);
					}
					else{
						Item rmItem = items.get(id);
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
	private void parseFilter(Block b, GamePlayer rmp){
		RequestFilter filter = rmp.getRequestFilter();
		if(filter!=null){
			FilterType filterType = filter.getFilterType();
			int randomize = filter.getRandomize();
			FilterState filterState = filter.getFilterState();
			HashMap<Integer, Item> items = filter.getItems();
			if((items!=null)&&(items.size()!=0)){
				switch(filterState){
				case REWARD: case TOOLS:
					if((filterType!=null)&&(filterType==FilterType.ADD)){
						switch(filterState){
							case REWARD: if(!_config.getSettingBool(Setting.infinitereward)) items = getItemsMatch(rmp.getInventory(), items); break;
							case TOOLS: if(!_config.getSettingBool(Setting.infinitetools)) items = getItemsMatch(rmp.getInventory(), items); break;
						}
					}
				}
				if(_config.getSettingBool(Setting.warnhacked)) warnHackMaterials(items);
				if(!_config.getSettingBool(Setting.allowhacked)){
					items = removeHackMaterials(items);
					if(items==null){
						rmp.sendMessage(Text.getLabel("filter.no_items_modified"));
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
				
				if(filterType!=null) parseFilterArgs(rmp, items, filterType);
				else parseFilterArgs(rmp, items);
				return;
			}
			else if(filterType==FilterType.CLEAR){
				switch(filterState){
					case FILTER: clearFilter(rmp); return;
					case REWARD: clearReward(b, rmp); return;
					case TOOLS: clearTools(b, rmp); return;
				}
			}
			else{
				switch(filterState){
					case FILTER: rm.rmFilterInfo(rmp); break;
					case REWARD: rm.rmRewardInfo(rmp); break;
					case TOOLS: rm.rmToolsInfo(rmp); break;
				}
			}
		}
		rmp.sendMessage(Text.getLabel("filter.no_items_modified"));
	}
	
	//Parse Filter Args
	private void parseFilterArgs(GamePlayer rmp, HashMap<Integer, Item> items){
		List<String> added = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		List<String> modified = new ArrayList<String>();
		FilterState filterState = rmp.getRequestFilter().getFilterState();
		String strItem;
		Boolean getId = false;
		if(items.size()>rm.config.getTypeLimit()) getId=true;
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
							added.add(ChatColor.WHITE+strItem+TextHelper.includeItem(items.get(item)));
							break;
						case MODIFY:
							modified.add(ChatColor.WHITE+strItem+TextHelper.includeItem(items.get(item)));	
							break;
						}
					}
					else{
						switch(_config.getFilter().removeAlwaysItem(item, items.get(item))){
						case REMOVE:
							removed.add(ChatColor.WHITE+strItem+TextHelper.includeItem(_config.getFilter().getLastItem()));
							break;
						}
					}
				}
			}
			
			if(added.size()>0) rmp.sendMessage(Text.getLabelArgs("common.added", TextHelper.getFormattedStringByList(added)));
			if(modified.size()>0) rmp.sendMessage(Text.getLabelArgs("common.modified", TextHelper.getFormattedStringByList(modified)));
			if(removed.size()>0) rmp.sendMessage(Text.getLabelArgs("common.removed", TextHelper.getFormattedStringByList(removed)));
		}
		else{
			switch(filterState){
			case REWARD:
				if(_config.getSettingBool(Setting.infinitereward)) _config.getReward().setItems(Filter.convertToListItemStack(items));
				else _config.getReward().setItemsMatchInventory(rmp.getInventory(), rmp, ClaimType.REWARD, Filter.convertRMHashToHash(items));
				
				_config.getReward().showChanged(rmp);
				break;
			case TOOLS:
				if(_config.getSettingBool(Setting.infinitetools)) _config.getTools().setItems(Filter.convertToListItemStack(items));
				else _config.getTools().setItemsMatchInventory(rmp.getInventory(), rmp, ClaimType.TOOLS, Filter.convertRMHashToHash(items));
				_config.getTools().showChanged(rmp);
			break;
			}
		}
	}
	
	//Parse Filter Args Force
	private void parseFilterArgs(GamePlayer rmp, HashMap<Integer, Item> items, FilterType filterType){
		List<String> added = new ArrayList<String>();
		List<String> modified = new ArrayList<String>();
		List<String> removed = new ArrayList<String>();
		FilterState filterState = rmp.getRequestFilter().getFilterState();
		String strItem;
		Boolean getId = false;
		if(items.size()>rm.config.getTypeLimit()) getId=true;
		Integer[] arrayItems = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(arrayItems);
		if(filterState==FilterState.FILTER){
			switch(filterType){
			case ADD:
				for(Integer item : arrayItems){
					Material mat = Material.getMaterial(item);
					if(mat!=Material.AIR){
						if(getId) strItem = ""+item;
						else strItem = mat.name();
						switch(_config.getFilter().addItem(item, items.get(item), true)){
							case ADD:
								added.add(ChatColor.WHITE+strItem+TextHelper.includeItem(items.get(item))+ChatColor.WHITE);
								break;
							case MODIFY:
								modified.add(ChatColor.WHITE+strItem+TextHelper.includeItem(items.get(item))+ChatColor.WHITE);
								break;
						}
					}
				}
				if(added.size()>0) rmp.sendMessage(Text.getLabelArgs("common.added", TextHelper.getFormattedStringByList(added)));
				if(modified.size()>0) rmp.sendMessage(Text.getLabelArgs("common.modified", TextHelper.getFormattedStringByList(modified)));
				break;
			case SUBTRACT:
				for(Integer item : arrayItems){
					Material mat = Material.getMaterial(item);
					if(mat!=Material.AIR){
						if(getId) strItem = ""+item;
						else strItem = mat.name();
						switch(_config.getFilter().removeItem(item, items.get(item), true)){
							case MODIFY:
								modified.add(ChatColor.WHITE+strItem+TextHelper.includeItem(items.get(item))+ChatColor.WHITE);
								break;
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+TextHelper.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE);
								break;
						}
					}
				}
				if(modified.size()>0) rmp.sendMessage(Text.getLabelArgs("common.modified", TextHelper.getFormattedStringByList(modified)));
				if(removed.size()>0) rmp.sendMessage(Text.getLabelArgs("common.removed", TextHelper.getFormattedStringByList(removed)));
				break;
			case CLEAR:
				for(Integer item : arrayItems){
					Material mat = Material.getMaterial(item);
					if(mat!=Material.AIR){
						if(getId) strItem = ""+item;
						else strItem = mat.name();
						switch(_config.getFilter().removeAlwaysItem(item, items.get(item))){
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+TextHelper.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE);
								break;
						}
					}
				}
				if(removed.size()>0) rmp.sendMessage(Text.getLabelArgs("common.removed", TextHelper.getFormattedStringByList(removed)));
				break;
			}
		}
		else{
			List<ItemStack> listItems = Filter.convertToListItemStack(items);
			switch(filterType){
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
					if(_config.getSettingBool(Setting.infinitereward)) _config.getReward().removeItems(listItems);
					else rmp.claim(_config.getReward(), ClaimType.REWARD, listItems.toArray(new ItemStack[listItems.size()]));
					_config.getReward().showChanged(rmp);
					break;
				case TOOLS:
					if(_config.getSettingBool(Setting.infinitetools)) _config.getTools().removeItems(listItems);
					else rmp.claim(_config.getTools(), ClaimType.TOOLS, listItems.toArray(new ItemStack[listItems.size()]));
					_config.getTools().showChanged(rmp);
					break;
				}
				break;
			case CLEAR:
				switch(filterState){
				case REWARD:
					if(_config.getSettingBool(Setting.infinitereward)) _config.getReward().removeItemsWhole(listItems);
					else rmp.claim(_config.getReward(), ClaimType.REWARD, true, listItems.toArray(new ItemStack[listItems.size()]));
					_config.getReward().showChanged(rmp);
					break;
				case TOOLS:
					if(_config.getSettingBool(Setting.infinitetools)) _config.getTools().removeItemsWhole(listItems);
					else rmp.claim(_config.getTools(), ClaimType.TOOLS, true, listItems.toArray(new ItemStack[listItems.size()]));
					_config.getTools().showChanged(rmp);
					break;
				}
			}
		}
	}
	
	//Get Formatted String by Hash
	public String getFormattedStringByHash(HashMap<Integer, Item> items, GamePlayer rmp){
		GameChest rmChest = rmp.getTeam().getChest();
		String line = "";
		Integer[] array = items.keySet().toArray(new Integer[items.keySet().size()]);
		Arrays.sort(array);
		boolean useId = array.length>rm.config.getTypeLimit()?true:false;
		for(Integer item : array){
			String itemId = useId?""+item:Material.getMaterial(item).name();
			Item rmItem = items.get(item);
			int amount = rmItem.getAmount(); 
			if(amount!=-1){
				itemId = Material.getMaterial(item).name();
				if(amount!=0){
					line+=ChatColor.GREEN+itemId+TextHelper.includeItem(rmItem)+ChatColor.WHITE+", ";
				}
				else line+=ChatColor.DARK_GREEN+itemId+":0"+ChatColor.WHITE+", ";
			}
			else{
				if(rmChest.getRMItems().containsKey(item)) amount = rmChest.getItemLeft(item).getAmount();
				else amount = _config.getItems().getItem(item).getAmount();
				if(amount!=0) line+=ChatColor.WHITE+itemId+TextHelper.includeItem(new Item(item, amount))+ChatColor.WHITE+", ";
			}
		}
		line = TextHelper.stripLast(line, ",");
		return line;
	}
	
	public String getSimpleFormattedStringByHash(HashMap<Integer, Item> items){
		String line = "";
		Integer[] array = items.keySet().toArray(new Integer[items.keySet().size()]);
		Arrays.sort(array);
		for(Integer item : array){
			Item rmItem = items.get(item);
			line+=ChatColor.WHITE+Material.getMaterial(item).name()+TextHelper.includeItem(new Item(item, rmItem.getAmount()))+ChatColor.WHITE+", ";
		}
		line = TextHelper.stripLast(line, ",");
		return line;
	}
	
	//Find Hack Materials
	public List<Material> findHackMaterials(HashMap<Integer, Item> materials){
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
	public HashMap<Integer, Item> removeHackMaterials(HashMap<Integer, Item> materials){
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
	public void addLog(BlockState... bStates){
		_config.getLog().add(bStates);
	}
	public void addLog(Block... blocks){
		_config.getLog().add(blocks);
	}
	//LOG ENTITY
	public void addLog(Creature... creatures){
		_config.getLog().add(creatures);
	}
	
	//Clear Log
	public void clearLog(){
		_config.getLog().clear();
	}
	
	public void restoreLog(){
		if(_config.getSettingBool(Setting.restore)){
			if(_config.getLog().restore()) broadcastMessage(Text.getLabel("restore.success"));
			List<GameCreature> creatures = _config.getLog().getCreatureList();
			for(GameCreature creature : creatures){
				World w = this.getMainBlock().getWorld();
				w.spawnCreature(creature.getLocation(), creature.getType());
			}
			_config.getLog().clear();
		}
	}
	
	//Restore World
	public void restoreWorld(GamePlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		switch(_config.getState()){
		case SETUP:
			Log log = _config.getLog();
			if(log.getList().size()+log.getItemList().size()+log.getCreatureList().size()!=0) restoreLog();
			else rmp.sendMessage(Text.getLabel("restore.nothing"));
			break;
		default: rmp.sendMessage(Text.getLabel("restore.game_in_progress"));
		}
	}
	
	//HACK MATERIALS
	//Warn Hack Materials
	
	public void warnHackMaterialsByItems(List<ItemStack> items){
		warnHackMaterialsMessage(TextHelper.getFormattedStringByListMaterial(findHackMaterialsByItems(items)));
	}
	public void warnHackMaterials(List<Material> items){
		warnHackMaterialsMessage(TextHelper.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterials(HashMap<Integer, Item> items){
		warnHackMaterialsMessage(TextHelper.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterialsMessage(String message){
		if(message.length()>0) _config.getOwner().sendMessage(Text.getLabelArgs("common.not_allowed", message));
	}
	
	//Config
	
	public void setSetting(GamePlayer rmp, Setting setting, int value){
		if(_config.getSettingLibrary().get(setting).isLocked()){
			rmp.sendMessage(Text.getLabel("msg.no_change_locked"));
			return;
		}
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		switch(setting){
			case minplayers: case maxplayers: case minteamplayers: case maxteamplayers:
				_config.setSetting(setting, value);
				_config.correctMinMaxNumbers(setting);
				rmp.sendMessage(Text.getLabel("setting."+setting.name())+": "+getText(rmp, setting));
				sendMinMax(rmp);
				break;
			case timelimit:
				_config.setSetting(setting, value*60);
				Timer timer = _config.getTimer();
				timer.setTimeLimit(value*60);
				timer.resetDefaults();
				rmp.sendMessage(Text.getLabel("setting."+setting.name())+": "+(value==0?getText(rmp, setting):ChatColor.GREEN+Timer.getTextTimeStatic(value*60)));
				break;
			case delaypvp:
				_config.setSetting(setting, value*60);
				timer = _config.getPvpTimer();
				timer.reset();
				timer.clearTimeMessages();
				timer.setTimeLimit(value*60);
				timer.resetDefaults();
				rmp.sendMessage(Text.getLabel("setting."+setting.name())+": "+(value==0?getText(rmp, setting):ChatColor.GREEN+Timer.getTextTimeStatic(value*60)));
				break;
			case safezone: case playarea: case enemyradar: case keepondeath: case random:
				_config.setSetting(setting, value);
				rmp.sendMessage(Text.getLabel("setting."+setting.name())+": "+getText(rmp, setting));
				break;
			case playareatime:
				_config.setSetting(setting, value);
				rmp.sendMessage(Text.getLabel("setting."+setting.name())+": "+(value==0?getText(rmp, setting):ChatColor.GREEN+Timer.getTextTimeStatic(value)));
				for(GamePlayer rmPlayer : getTeamPlayers()){
					rmPlayer.refreshPlayAreaTimeLimit();
					rmPlayer.getPlayAreaTimer().reset();
				}
				break;
			case timeofday:
				_config.setSetting(setting, value);
				rmp.sendMessage(Text.getLabel("setting."+setting.name())+": "+getText(rmp, setting));
			default:
				if(value==-1) _config.toggleSetting(setting);
				else _config.setSetting(setting, value>0?true:false);
				rmp.sendMessage(Text.getLabel("setting."+setting.name())+": "+isTrueFalse(_config.getSettingBool(setting)));
				break;
		}
		updateSigns();
	}
	
	public void setSetting(GamePlayer rmp, Setting setting, String str){
		if(_config.getSettingLibrary().get(setting).isLocked()){
			rmp.sendMessage(Text.getLabel("msg.no_change_locked"));
			return;
		}
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		switch(setting){
			case password:
				_config.setSetting(setting, str);
				rmp.sendMessage(Text.getLabel("setting.password")+": "+getPassword(str));
				break;
		}
		updateSigns();
	}
	
	public void setSetting(GamePlayer rmp, Setting setting, IntRange range){
		if(_config.getSettingLibrary().get(setting).isLocked()){
			rmp.sendMessage(Text.getLabel("msg.no_change_locked"));
			return;
		}
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		switch(setting){
			case multiplier:
				_config.setSetting(setting, range);
				rmp.sendMessage(Text.getLabel("setting."+setting.name())+": "+getText(rmp, setting));
				break;
		}
		updateSigns();
	}

	
	public String getText(GamePlayer rmp, Setting set){
		String str = "";
		SettingLibrary settingLib = _config.getSettingLibrary();
		SettingPrototype setting = settingLib.get(set);
		if(setting instanceof SettingInt) str = ""+settingLib.getInt(set);
		else if(setting instanceof SettingBool) str = isTrueFalse(settingLib.getBool(set));
		else if(setting instanceof SettingStr) str = settingLib.getStr(set);
		
		switch(set){
		case minplayers: case maxplayers: case minteamplayers: case maxteamplayers:
			str = (settingLib.getInt(set)>0?(ChatColor.GREEN+""+str):(Text.getLabel("common.no_limit")));
			break;
		case timelimit:
			str = (settingLib.getInt(set)>0?(ChatColor.GREEN+""+settingLib.getInt(set)/60):(Text.getLabel("common.no_limit")));
			break;
		case delaypvp:
			str = (settingLib.getInt(set)>0?(ChatColor.GREEN+""+settingLib.getInt(set)/60):(Text.getLabel("common.disabled")));
			break;
		case safezone: case playarea: case playareatime: case enemyradar: case keepondeath:
			str = (settingLib.getInt(set)>0?(ChatColor.GREEN+""+str):(Text.getLabel("common.disabled")));
			break;
		case multiplier:
			IntRange range = settingLib.getIntRange(set);
			str = ""+(range.hasLow()?""+ChatColor.GREEN+range.getLow():"")+(range.hasHigh()?ChatColor.WHITE+"-"+ChatColor.GREEN+range.getHigh():"");
			break;
		case random:
			str = (settingLib.getInt(set)>0?(ChatColor.GREEN+""+str+" "+Text.getLabel("common.item(s)")):(Text.getLabel("common.disabled")));
			break;
		case password:
			str = (!rmp.hasOwnerPermission(_config.getOwnerName())?getPassword(str,true):getPassword(str));
			break;
		case timeofday:
			str = (settingLib.getInt(set)>0?(ChatColor.GREEN+""+str+" "+Text.getLabel("common.item(s)")):(Text.getLabel("common.disabled")));
			break;
		default:
			str = ChatColor.GREEN+str;
			break;
		}
		return str;
	}

	//Set Randomize Amount
	public void setRandomizeAmount(GamePlayer rmp, int amount){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		_config.setSetting(Setting.random, amount);
	}

	//Is True / False
	public String isTrueFalse(boolean bool){
		return (bool?(ChatColor.GREEN+Text.getLabel("common.true")):(ChatColor.GRAY+Text.getLabel("common.false")));
	}
	
	public String getPassword(String str){
		return getPassword(str, false);
	}
	
	public String getPassword(String str, boolean hide){
		if(str.length()!=0){
			if(hide) str = ChatColor.GREEN+TextHelper.genString("*", str.length());
			else str = ChatColor.GREEN+str;
			return str;
		}
		return Text.getLabel("common.disabled");
	}
	
	//Get Text Players of Max
	public String getTextPlayersOfMax(){
		String line = "";
		if(_config.getSettingInt(Setting.minplayers)!=0) line += _config.getSettingInt(Setting.minplayers)+"-";
		else line+= ">"+"-";
		if(_config.getSettingInt(Setting.maxplayers)!=0) line += _config.getSettingInt(Setting.maxplayers);
		else line+= ">";
		if(line.length()!=0) line = "/"+line;
		return line;
	}
	
	//Get Text Team Players of Max
	public String getTextTeamPlayersOfMax(){
		String line = "";
		if(_config.getSettingInt(Setting.minteamplayers)!=0) line += _config.getSettingInt(Setting.minteamplayers)+"-";
		else line+= ">"+"-";
		if(_config.getSettingInt(Setting.maxteamplayers)!=0) line += _config.getSettingInt(Setting.maxteamplayers);
		else line+= ">";
		if(line.length()!=0) line = "/"+line;
		return line;
	}
	
	public static String getSignString(String variable, String fixed){
		int length = 15-fixed.length();
		fixed = variable.substring(0, length>variable.length()?variable.length():length) + fixed;
		return fixed;
	}
	
	//Send Info
	public void sendInfo(GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.info")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		String message = "";
		message += ChatColor.YELLOW+""+_config.getId()+ChatColor.WHITE+" ";
		message += ChatColor.AQUA+_config.getName()+ChatColor.WHITE+" ";
		message += Text.getLabel("info.world")+": "+ChatColor.YELLOW+TextHelper.firstLetterToUpperCase(_config.getWorld().getName().toLowerCase())+ChatColor.WHITE+" ";
		message += Text.getLabel("info.owner")+": "+ChatColor.YELLOW+_config.getOwnerName()+ChatColor.WHITE+" ";
		message += Text.getLabel("info.timelimit")+": "+getText(rmp, Setting.timelimit);
		rmp.sendMessage(message);
		message = Text.getLabel("info.players")+": "+ChatColor.GREEN+getTeamPlayers().length+ChatColor.WHITE+" ";
		message += Text.getLabel("info.ingame")+": "+getText(rmp, Setting.minplayers)+ChatColor.WHITE+"-"+getText(rmp, Setting.maxplayers)+ChatColor.WHITE+" ";
		message += Text.getLabel("info.inteam")+": "+getText(rmp, Setting.minteamplayers)+ChatColor.WHITE+"-"+getText(rmp, Setting.maxteamplayers);
		rmp.sendMessage(message);
		rmp.sendMessage(Text.getLabel("info.teams")+": "+getTextTeamPlayers());
	}
	
	public void sendTeamInfo(GamePlayer rmp){
		sendInfo(rmp);
	}
	
	public void sendMinMax(GamePlayer rmp){
		rmp.sendMessage(Text.getLabel("minmax.ingame")+": "+getText(rmp, Setting.minplayers)+ChatColor.WHITE+"-"+getText(rmp, Setting.maxplayers)+ChatColor.WHITE+" "+Text.getLabel("minmax.inteam")+": "+getText(rmp, Setting.minteamplayers)+ChatColor.WHITE+"-"+getText(rmp, Setting.maxteamplayers));
	}
	
	public void getInfoFound(GamePlayer rmp){
		switch(rmp.getPlayerAction()){
		case INFO_FOUND:
			if(!rmp.hasPermission("resourcemadness.info.found")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
			break;
		case CLAIM_INFO_FOUND:
			if(!rmp.hasPermission("resourcemadness.claim.info.found")){
				rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
				return;
			}
			break;
		}
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		String items = TextHelper.getStringSortedItems(_config.getFound().getItems());
		if(items.length()>0){
			rmp.sendMessage(Text.getLabelArgs("found.items", items));
		}
		else rmp.sendMessage(Text.getLabel("found.no_items"));
	}

	public void sendSettings(GamePlayer rmp, int page){
		if(!rmp.hasPermission("resourcemadness.info.settings")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return;
		}
		
		int size = 14;
		int pages = Setting.calculatePages(size);
		if(page<=0) page = 1;
		if(page>pages) page = pages;
		rmp.sendMessage(Text.getLabelArgs("help_settings", ""+page, ""+pages));
		
		Setting[] settings = Setting.values();
		SettingLibrary settingLib = _config.getSettingLibrary();
		
		int end = size*(page);
		int i = size*(page-1);
		
		while((i<end)&&(i<settings.length)){
			Setting set = settings[i];
			SettingPrototype s = settingLib.get(set);
			String str = getText(rmp, set);
			rmp.sendMessage((s.isLocked()?ChatColor.RED:ChatColor.YELLOW)+Text.getLabel("cmd.set."+s.name())+" "+str+" "+Text.getLabel("setting."+s.name())+".");
			i++;
		}
	}
	
	public void sendBanList(GamePlayer rmp, int id){
		int listLimit = 12;
		
		BanList banList = _config.getBanList();
		List<String> namesList = new ArrayList<String>();
		namesList.addAll(banList.keySet());
		
		if(namesList.size()==0){
			rmp.sendMessage(Text.getLabel("banlist.empty"));
			return;
		}
		int i = 0;
		if(id<1) id=1;
		int size = (int)Math.ceil((double)namesList.size()/(double)listLimit);
		if(id>size) id=1;
		i=(id-1)*listLimit;
		rmp.sendMessage(Text.getLabelArgs("banlist", ""+id, ""+size));
		int found = 0;
		while((found<listLimit)&&(i<namesList.size())){
			rmp.sendMessage(namesList.get(i));
			i++;
			found++;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//STATIC FUNCTIONS////STATIC FUNCTIONS////STATIC FUNCTIONS////STATIC FUNCTIONS////STATIC FUNCTIONS//
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////
	//Request Player//
	//////////////////
	
	//Get Request Player
	public static GamePlayer getRequestPlayer(){
		return _requestPlayer;
	}
	
	//Set Request Player
	public static void setRequestPlayer(GamePlayer rmp){
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
	public static HashMap<String, GamePlayer> getAllPlayers(){
		HashMap<String, GamePlayer> players = new HashMap<String, GamePlayer>();
		for(Game game : _games.values()){
			players.putAll(game._config.getPlayers());
		}
		return players;
	}
	
	////////
	//Game//
	////////
	
	public static int getFreeId(){
		int i=0;
		int freeId=-1;
		while(freeId==-1){
			if(!_ids.containsKey(i)) freeId = i;
			i++;
		}
		return freeId;
	}
	
	private static Game add(String name, Game game){
		//if((name!=null)&&(name.length()!=0)){
			if(!_games.containsKey(name)){
				_games.put(name, game);
				_ids.put(game._config.getId(), name);
				//return rmGame;
			}
			return game;
		//}
		//return null;
	}
	
	private static Game add(Game rmGame){
		String name = rmGame.getGameConfig().getName();
		return add(name, rmGame);
	}
	
	public static void tryCreateGameFromGameConfig(GameConfig gameConfig){
		Game rmGame = add(new Game(gameConfig, rm));
		for(Team rmt : rmGame.getGameConfig().getTeams()){
			rmt.setGame(rmGame);
		}
		rmGame.getGameConfig().correctMinMaxNumbers(Setting.minteamplayers);
		rmGame.updateSigns();
	}
	
	//Try Add Game
	public static HandleState tryCreateGame(String name, Block b, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.add")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return HandleState.NONE;
		}
		
		if(rm.config.getMaxGamesPerPlayer()>0){
			if(getGamesByOwner(rmp).size()>=rm.config.getMaxGamesPerPlayer()){
				rmp.sendMessage(Text.getLabel("game.max_games_reached"));
				return HandleState.NONE;
			}
		}
		
		if(_games.containsKey(name)){
			Game game = _games.get(name);
			rmp.sendMessage(Text.getLabelArgs("game.game_already_exists", game.getGameConfig().getName(), ""+game.getGameConfig().getId()));
			return HandleState.NONE;
		}
		
		PartList partList = new PartList(b, rm);
		/*
		Game rmGame = getGameByBlock(partList.getMainBlock(b));
		Boolean wasModified = false;
		GameConfig rmGameConfig = new GameConfig();
		if(rmGame==null){
			rmGame = Game.getGameByBlock(b);
		}
		*/
		Game rmGame = Game.getGameByBlock(b);
		if(rmGame!=null){
			rmp.sendMessage(Text.getLabelArgs("game.cannot_overwrite", rmGame.getGameConfig().getName(), ""+rmGame.getGameConfig().getId()));
			return HandleState.NONE;
			/*
			if(!rmp.hasOwnerPermission(rmGame._config.getOwnerName())){
				rmp.sendMessage(Text.getLabelArgs("game.owner", rmGame._config.getOwnerName()));
				return HandleState.NO_CHANGE;
			}
			if(!partList.matchPartList(rmGame._config.getPartList())){
				wasModified = true;
				rmGameConfig = rmGame._config;
				Game.removeGame(rmGame);
				rmGame = null;
			}
			else{
				if(rmGame.getTeams().size()==4){
					rmp.sendMessage(Text.getLabelArgs("game.has_max_teams", ""+rmGame._config.getName(), ""+rmGame._config.getId()));
					return HandleState.NO_CHANGE;
				}
				rmp.sendMessage(Text.getLabelArgs("game.exists", rmGame.getGameConfig().getName(), ""+rmGame.getGameConfig().getId()));;
				return HandleState.NO_CHANGE;
			}
			*/
		}
		
		if(partList.getStoneList().size()<2){
			rmp.sendMessage(Text.getLabelArgs("game.missing_stone", ""+(2-partList.getStoneList().size())));
			return HandleState.NONE;
		}
	
		List<Team> teams = partList.fetchTeams();
		if(teams.size()<2){
			rmp.sendMessage(Text.getLabelArgs("game.not_enough_teams", "2"));
			return HandleState.NONE;
		}
		
		rmGame = add(new Game(name, partList, rmp, rm));
		for(Team rmt : teams){
			rmGame.addTeam(rmt);
		}
		/*
		if(wasModified){
			GameConfig config = rmGame.getGameConfig();
			config.getDataFrom(rmGameConfig);
			rmGameConfig = null;
			rmp.sendMessage(Text.getLabelArgs("game.modified", ""+rmGame._config.getName(), ""+rmGame._config.getId()));
		}
		else{
		*/
		rmGame.getGameConfig().getDataFrom(rm.getRMConfig());
		rmp.sendMessage(Text.getLabelArgs("game.create", ""+rmGame.getGameConfig().getName(), ""+rmGame.getGameConfig().getId()));
		//}
		rmp.sendMessage(Text.getLabelArgs("game.teams_count", ""+teams.size())+" ("+rmGame.getTextTeamColors()+")");
		
		//Correct min/max numbers
		rmGame.getGameConfig().correctMinMaxNumbers(Setting.minteamplayers);
		
		rmGame.updateSigns();
		
		//if(wasModified) return HandleState.MODIFY;
		//else return HandleState.ADD;
		return HandleState.ADD;
	}
	
	// Remove Game
	private static Boolean removeGame(Game game){
		String name = game.getGameConfig().getName();
		if(_games.containsKey(name)){
			//Game.getGamesByOwnerName(rmGame.getGameConfig().getOwnerName()).remove(rmGame);
			for(Team rmt : game.getTeams()){
				rmt.setNull();
			}
			_games.remove(name);
			_ids.remove(game._config.getId());
			game = null;
			return true;
		}
		return false;
	}
	
	//Try Remove Game
	public static HandleState tryRemoveGame(Block b, GamePlayer rmp){
		Game rmGame = Game.getGameByBlock(b);
		if(rmGame!=null){
			return tryRemoveGame(rmGame, rmp);
		}
		return HandleState.NONE;
	}
	
	//Try Remove Game
	public static HandleState tryRemoveGame(Game rmGame, GamePlayer rmp){
		if(!rmp.hasPermission("resourcemadness.remove")){
			rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
			return HandleState.NO_CHANGE;
		}

		if(rmGame==null){
			rmp.sendMessage("Cannot remove a non-existing game");
			return HandleState.NONE;
		}

		if(rmGame.getGameConfig().getState() != GameState.SETUP){
			rmp.sendMessage(Text.getLabelArgs("game.remove.game_in_progress", rmGame.getGameConfig().getName(), ""+rmGame.getGameConfig().getId()));
			return HandleState.NO_CHANGE;
		}
		
		if(!rmp.hasOwnerPermission(rmGame._config.getOwnerName())){
			rmp.sendMessage(Text.getLabelArgs("game.owner", rmGame.getGameConfig().getOwnerName()));
			return HandleState.NO_CHANGE;
		}
		
		for(Sign sign : rmGame.getSigns()){
			if(sign==null) continue;
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update();
		}

		if(removeGame(rmGame)){
			rmp.sendMessage(Text.getLabelArgs("game.remove", rmGame.getGameConfig().getName(), ""+rmGame.getGameConfig().getId()));
			return HandleState.REMOVE;
		}
		return HandleState.NONE;
	}
	
	public static Material[] getMaterials(){
		return _materials;
	}
	
	//Get Game by Id (String)
	/*
	public static Game getGameById(String arg){
		int id = Helper.getIntByString(arg);
		if(id!=-1) return Game.getGame(id);
		return null;
	}
	*/
	
	public static Game getGame(int id){
		if(_ids.containsKey(id)){
			return _games.get(_ids.get(id));
		}
		return null;
	}
	
	public static Game getGame(String name){
		if(_games.containsKey(name)) return _games.get(name);
		return null;
	}
	
	public static TreeMap<String, Game> getGames(){
		return _games;
	}
	
	public static Game[] getAdvertisedGames(){
		List<Game> advertised = new ArrayList<Game>();
		for(Game game : _games.values()){
			if(game.getGameConfig().getSettingBool(Setting.advertise)){
				advertised.add(game);
			}
		}
		return advertised.toArray(new Game[advertised.size()]);
	}
	
	public static Game getGameByBlock(Block b){
		for(Game game : _games.values()){
			for(Block block : game._config.getPartList().getList()){
				if(block.equals(b)){
					return game;
				}
				
			}
		}
		return null;
	}
	private static List<Game> getGamesByOwner(GamePlayer rmp){
		List<Game> games = new ArrayList<Game>();
		for(Game game : _games.values()){
			if(rmp.hasOwnerPermission(game._config.getOwnerName())) games.add(game); 
		}
		return games;
	}
	
	
	private static List<Game> getGamesByOwnerName(String name){
		List<Game> games = new ArrayList<Game>();
		for(Game game : Game.getGames().values()){
			if(game._config.getOwnerName().equalsIgnoreCase(name)) games.add(game); 
		}
		return games;
	}
	
	////////
	//Team//
	////////
	
	//Get Team
	public static Team getTeam(Game rmGame, int index){
		if(rmGame._config.getTeams().size()>index) return rmGame._config.getTeams().get(index);
		return null;
	}
	
	//Get Teams
	public static List<Team> getTeams(Game rmGame){
		return rmGame._config.getTeams();
	}
	
	//Get All Teams
	public static List<Team> getAllTeams(){
		List<Team> teams = new ArrayList<Team>();
		for(Game game : _games.values()){
			teams.addAll(game._config.getTeams());
		}
		return teams;
	}
	
	//Get TeamById
	public static Team getTeamById(String arg, Game rmGame){
		int id = Helper.getIntByString(arg);
		if(id!=-1) return rmGame.getTeam(id);
		return null;
	}
	
	/////////
	//Chest//
	/////////
	
	//Get Chests from Block List
	public static List<GameChest> getChestsFromBlockList(List<List<Block>> bList, RM plugin){
		List<List<Block>> blockList = bList.subList(2, bList.size());
		List<GameChest> chestList = new ArrayList<GameChest>();
		for(List<Block> blocks : blockList){
			chestList.add(new GameChest((Chest)blocks.get(Part.CHEST.ordinal()-2).getState()));
		}
		if(chestList.size()>0) return chestList;
		else return null;
	}
}