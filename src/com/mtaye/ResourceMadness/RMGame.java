package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMStats.RMStat;
import com.mtaye.ResourceMadness.RMStats.RMStatServer;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.setting.SettingBool;
import com.mtaye.ResourceMadness.setting.SettingInt;
import com.mtaye.ResourceMadness.setting.SettingLibrary;
import com.mtaye.ResourceMadness.setting.SettingPrototype;
import com.mtaye.ResourceMadness.setting.SettingStr;
import com.mtaye.ResourceMadness.Helper.RMHelper;
import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMGame {

	private static HashMap<Integer, RMGame> _games = new HashMap<Integer, RMGame>();
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
	
	private RMGameConfig _config = new RMGameConfig();
	
	private static RMPlayer _requestPlayer;
	
	public static enum Part { GLASS, STONE, CHEST, WALL_SIGN, WOOL; }
	public static enum GameState { SETUP, COUNTDOWN, GAMEPLAY, GAMEOVER, PAUSED; }
	public static enum InterfaceState { FILTER, REWARD, TOOLS, FILTER_CLEAR, REWARD_CLEAR, TOOLS_CLEAR };
	public static enum FilterItemType { ALL, BLOCK, ITEM, FOOD, RAW, CRAFTED};
	public static enum ClickState { LEFT, RIGHT, NONE };
	public static enum ItemHandleState { ADD, MODIFY, REMOVE, NONE };
	public static enum HandleState { ADD, MODIFY, REMOVE, NO_CHANGE, CLAIM_RETURNED_ALL, CLAIM_RETURNED_SOME, NONE };
	public static enum FilterState { FILTER, FOUND, REWARD, TOOLS, ITEMS, NONE };
	public static enum FilterType { SET, ADD, SUBTRACT, CLEAR, RANDOMIZE, NONE};
	
	private final int cdTimerLimit = 30; //3 seconds
	private int cdTimer = cdTimerLimit;
	
	private RMTeam _winningTeam;
	private RMPlayer _winningPlayer;
	
	//Constructor
	public RMGame(RMPartList partList, RMPlayer rmp, RM plugin){
		RMGame.rm = plugin;
		_config.setPartList(partList);
		_config.setPlayers(RMPlayer.getPlayers());
		_config.setId(getFreeId());
		_config.setOwnerName(rmp.getName());
	}
	public RMGame(RMGameConfig config, RM plugin){
		RMGame.rm = plugin;
		_config = config;
	}
	
	//Config
	public RMGameConfig getGameConfig(){
		return _config;
	}
	public void setGameConfig(RMGameConfig config){
		_config = config;
	}
	public void resetSettings(RMPlayer rmp){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
		}
		_config = new RMGameConfig(rm.config);
		updateSigns();
		rmp.sendMessage(RMText.getLabel("settings.reset"));
	}
	
	//Ban Player
	public void banPlayer(RMPlayer rmp, boolean announce, List<String> names){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
		}
		for(String name : names){
			banPlayerSilent(rmp, true, name);
		}
		kickPlayer(rmp, false, names);
	}
	
	//Ban Team
	public void banTeam(RMPlayer rmp, boolean announce, List<String> colors){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
		}
		List<RMTeam> teams = findRMTeamsByColors(rmp, announce, colors);
		RMTeam[] arrayTeams = teams.toArray(new RMTeam[teams.size()]);
		banTeamSilent(rmp, announce, arrayTeams);
		kickTeamSilent(rmp, false, arrayTeams);
	}
	
	//Ban All
	public void banAll(RMPlayer rmp, boolean announce){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
		}
		banAllSilent(rmp, announce);
	}
	
	//Unban Player
	public void unbanPlayer(RMPlayer rmp, boolean announce, List<String> names){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
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
		if(announce) if(notFound.size()!=0) if(rmp!=null) rmp.sendMessage(RMText.getLabelArgs("game.banned_not_found", RMTextHelper.getStringByStringList(notFound, ", "), ""+_config.getId()));
		for(String name : found){
			unbanPlayerSilent(rmp, announce, name);
		}
	}
	
	//Kick
	public void kickPlayer(RMPlayer rmp, boolean announce, List<String> names){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
		}
		List<RMPlayer> found = findRMPlayersByNames(rmp, announce, names);
		kickPlayerSilent(rmp, announce, found.toArray(new RMPlayer[found.size()]));
	}

	public void kickTeam(RMPlayer rmp, boolean announce, List<String> colors){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
		}
		List<RMTeam> found = findRMTeamsByColors(rmp, announce, colors);
		kickTeamSilent(rmp, announce, found.toArray(new RMTeam[found.size()]));
	}
	
	public void kickAll(RMPlayer rmp, boolean announce){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
		}
		kickAllSilent(rmp, announce);
	}
	
	//Ban Silent
	public void banPlayerSilent(RMPlayer rmp, boolean announce, String name){
		_config.getBanList().add(name);
		if(announce){
			RMPlayer player = RMPlayer.getPlayerByNameOnly(name);
			if(player!=null){
				player.sendMessage(RMText.getLabelArgs("ban.player", ""+getGameConfig().getId()));
				broadcastMessage(RMText.getLabelArgs("ban.player.broadcast", player.getName(), ""+getGameConfig().getId()), player);
			}
			else broadcastMessage(RMText.getLabelArgs("ban.player.broadcast", name, ""+getGameConfig().getId()), player);
		}
	}
	
	public void banTeamSilent(RMPlayer rmp, boolean announce, RMTeam... teams){
		for(RMTeam team : teams){
			String[] players = team.getPlayersNamesArray();
			if(players.length!=0){
				for(String player : players){
					banPlayerSilent(rmp, announce, player);
				}
			}
			else rmp.sendMessage(RMText.getLabelArgs("team.empty", team.getTeamColorString()));
		}
	}
	

	private void banAllSilent(RMPlayer rmp, boolean announce){
		RMPlayer[] players = getTeamPlayers();
		if(players.length!=0){
			String[] strPlayers = getTeamPlayersNames();
			for(String player : strPlayers){
				banPlayerSilent(rmp, announce, player);
			}
			kickPlayerSilent(rmp, false, players);
		}
		else rmp.sendMessage(RMText.getLabelArgs("game.empty", ""+_config.getId()));
	}
	
	private void unbanPlayerSilent(RMPlayer rmp, boolean announce, String name){
		if(_config.getBanList().containsKey(name)){
			_config.getBanList().rem(name);
			if(announce){
				RMPlayer player = RMPlayer.getPlayerByNameOnly(name);
				if(player!=null){
					player.sendMessage(RMText.getLabelArgs("unban.player", ""+getGameConfig().getId()));
					broadcastMessage(RMText.getLabelArgs("unban.player.broadcast", player.getName(), ""+getGameConfig().getId()), player);
				}
				else broadcastMessage(RMText.getLabelArgs("unban.player.broadcast", name, ""+getGameConfig().getId()), player);
			}
		}
	}
	
	//Kick Silent
	private void kickPlayerSilent(RMPlayer rmp, boolean announce, RMPlayer... players){
		for(RMPlayer player : players){
			RMTeam team = player.getTeam();
			team.removePlayer(player, true);
			if(announce){
				player.sendMessage(RMText.getLabelArgs("kick.player", ""+getGameConfig().getId()));
				broadcastMessage(RMText.getLabelArgs("kick.player.broadcast", player.getName(), ""+getGameConfig().getId()), player);
			}
		}
	}
	
	private void kickTeamSilent(RMPlayer rmp, boolean announce, RMTeam... teams){
		for(RMTeam team : teams){
			RMPlayer[] players = team.getPlayers();
			if(players.length!=0){
				kickPlayerSilent(rmp, announce, players);
			}
			else if(rmp!=null) rmp.sendMessage(RMText.getLabelArgs("team.empty", team.getTeamColorString()));
		}
	}
	
	private void kickAllSilent(RMPlayer rmp, boolean announce){
		if(getTeamPlayers().length!=0){
			for(RMTeam rmTeam : getTeams()){
				kickPlayerSilent(rmp, announce, rmTeam.getPlayers());
			}
		}
		else if(rmp!=null) rmp.sendMessage(RMText.getLabelArgs("game.empty", ""+_config.getId()));
	}
	
	//Find players/teams
	private List<RMPlayer> findRMPlayersByNames(RMPlayer rmp, boolean announce, List<String> names){
		return findRMPlayersByNames(rmp, true, announce, names);
	}
	
	private List<RMPlayer> findRMPlayersByNames(RMPlayer rmp, boolean findOnly, boolean announce, List<String> names){
		List<String> notFound = new ArrayList<String>();
		List<RMPlayer> found = new ArrayList<RMPlayer>();
		for(String name : names){
			name =  name.trim();
			if((name==null)||(name.length()==0)) continue;
			RMPlayer player = RMPlayer.getPlayerByNameOnly(name);
			if((player==null)||(player.getTeam()==null)) notFound.add(name);
			else found.add(player);
		}
		if(announce) if(notFound.size()!=0) if(rmp!=null) rmp.sendMessage(RMText.getLabelArgs("game.players_not_found", RMTextHelper.getStringByStringList(notFound, ", "), ""+_config.getId()));
		return found;
	}
	
	private List<RMTeam> findRMTeamsByColors(RMPlayer rmp, boolean announce, List<String> colors){
		List<String> notFound = new ArrayList<String>();
		List<RMTeam> found = new ArrayList<RMTeam>();
		for(String color : colors){
			color =  color.trim();
			if((color==null)||(color.length()==0)) continue;
			RMTeam rmTeam = getTeamByDye(color);
			if(rmTeam==null) notFound.add(color);
			else found.add(rmTeam);
		}
		if(announce) if(notFound.size()!=0) if(rmp!=null) rmp.sendMessage(RMText.getLabelArgs("game.teams_not_found", RMTextHelper.getStringByStringList(notFound, ", "), ""+_config.getId()));
		return found;
	}
	
	//Winning Team
	public RMTeam getWinningTeam(){
		return _winningTeam;
	}
	public void setWinningTeam(RMTeam rmTeam){
		_winningTeam = rmTeam;
	}
	public void clearWinningTeam(){
		_winningTeam = null;
	}
	
	//Winning Player
	public RMPlayer getWinningPlayer(){
		return _winningPlayer;
	}
	public void setWinningPlayer(RMPlayer rmp){
		_winningPlayer = rmp;
	}
	public void clearWinningPlayer(){
		_winningTeam = null;
	}
	
	//Pause
	public void pauseGame(RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		switch(_config.getState()){
			case PAUSED: rmp.sendMessage(RMText.getLabel("pause.game_already_paused")); break;
			case GAMEPLAY:
				_config.setState(GameState.PAUSED);
				broadcastMessage(RMText.getLabel("pause.game_paused"));
				break;
			default: rmp.sendMessage(RMText.getLabel("pause.must_be_ingame"));
		}
		updateSigns();
	}
	
	public void resumeGame(RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		switch(_config.getState()){
			case PAUSED:
				_config.setState(GameState.GAMEPLAY);
				broadcastMessage(RMText.getLabel("resume.game_resumed"));
				break;
			case GAMEPLAY: rmp.sendMessage(RMText.getLabel("resume.game_not_paused")); break;
			default: rmp.sendMessage(RMText.getLabel("resume.must_be_ingame"));
		}
		updateSigns();
	}
	
	//Mode
	public void cycleMode(RMPlayer rmp){
		cycleMode(rmp, true);
	}
	
	public void cycleMode(RMPlayer rmp, boolean cycleForward){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
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
	
	public boolean changeMode(InterfaceState interfaceState, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.mode")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return false;
		}
		if(_config.getState()!=GameState.SETUP) return false;
		switch(interfaceState){
			case FILTER:
				if(!rmp.hasPermission("resourcemadness.mode.filter")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return false;
			}
			case REWARD:
				if(!rmp.hasPermission("resourcemadness.mode.reward")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return false;
			}
			case TOOLS:
				if(!rmp.hasPermission("resourcemadness.mode.tools")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return false;
			}
		}
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			_config.setInterface(interfaceState);
			rmp.sendMessage(RMText.getLabelArgs("mode.change", RMText.getLabelByInterfaceState(interfaceState)));
			updateSigns();
			return true;
		}
		return false;
	}
	
	//Money
	public void parseMoney(RMPlayer rmp, RMRequestMoney requestMoney){
		if(requestMoney == null) return;
		Double money = requestMoney.getMoney();
		FilterType filterType = requestMoney.getFilterType();
		
		Method economy = rm.economy;
		MethodAccount account = economy.getAccount(rmp.getName());
		//account.ge
		
		switch(filterType){
		case SET:
			if(!rmp.hasPermission("resourcemadness.money.set")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
			if(account.hasEnough(money)){
				account.subtract(money);
			}
			break;
		case ADD:
			if(!rmp.hasPermission("resourcemadness.money.set")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
			
			break;
		case SUBTRACT:
			if(!rmp.hasPermission("resourcemadness.money.set")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
			
			break;
		case CLEAR:
			if(!rmp.hasPermission("resourcemadness.money.set")){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				return;
			}
			break;
		}
	}
		
	
	//Template
	public void saveTemplate(String template, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.template.save")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		RMTemplate rmTemplate = new RMTemplate(template, _config);
		rmp.saveTemplate(rmTemplate);
	}
	public void loadTemplate(RMTemplate rmTemplate, RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(!rmp.hasPermission("resourcemadness.template.load")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(rmTemplate!=null){
			_config.setFilter(rmTemplate.getFilter());
			if(_config.getSettingBool(Setting.infinitereward)) _config.setReward(rmTemplate.getReward());
			else _config.getReward().setItemsMatchInventory(rmp.getPlayer().getInventory(), rmp, ClaimType.REWARD, RMInventoryHelper.convertToHashMap(rmTemplate.getReward().getItems()));
			if(_config.getSettingBool(Setting.infinitetools)) _config.setTools(rmTemplate.getTools());
			else _config.getTools().setItemsMatchInventory(rmp.getPlayer().getInventory(), rmp, ClaimType.TOOLS, RMInventoryHelper.convertToHashMap(rmTemplate.getTools().getItems()));
			rmp.sendMessage(RMText.getLabelArgs("template.load", rmTemplate.getName()));
			updateSigns();
		}
	}

	//Filter
	public void clearFilter(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.filter.clear")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		_config.getFilter().clearItems();
		rmp.sendMessage(RMText.getLabel("filter.clear"));
	}
	
	//Stash
	public void clearReward(Block b, RMPlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.reward.clear")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(!_config.getSettingBool(Setting.infinitereward)){
			if(rmp.claimStashToChest(_config.getReward(), b, ClaimType.REWARD, rmp.getInventory(), items).size()==0){
				rmp.sendMessage(RMText.getLabel("reward.clear"));
			}
		}
		else _config.getReward().clear();
	}
	public void clearTools(Block b, RMPlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.tools.clear")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(!_config.getSettingBool(Setting.infinitetools)){
			if(rmp.claimStashToChest(_config.getTools(), b, ClaimType.TOOLS, rmp.getInventory(), items).size()==0){
				rmp.sendMessage(RMText.getLabel("tools.clear"));
			}
		}
		else _config.getTools().clear();
	}
	public void clearFound(RMPlayer rmp, ItemStack... items){
		if(!rmp.hasPermission("resourcemadness.found.clear")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		if(rmp.claim(_config.getFound(), ClaimType.FOUND, items).size()==0){
			rmp.sendMessage(RMText.getLabel("found.clear"));
		}
	}

	
	public void addRewardByChest(RMPlayer rmp, RMChest rmChest, ClickState clickState){
		if(!rmp.hasPermission("resourcemadness.reward")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		addChestToStash(rmp, rmChest, _config.getReward(), ClaimType.REWARD, clickState);
	}
	public void addToolsByChest(RMPlayer rmp, RMChest rmChest, ClickState clickState){
		if(!rmp.hasPermission("resourcemadness.tools")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP) return;
		addChestToStash(rmp, rmChest, _config.getTools(), ClaimType.TOOLS, clickState);
	}
	
	public void addChestToStash(RMPlayer rmp, RMChest rmChest, RMStash rmStash, ClaimType claimType, ClickState clickState){
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
				if(rmp.getPlayer().isSneaking()){
					if(rmp.hasPermission("resourcemadness.reward.byhand")){
						addItemToStash(rmp, rmStash, claimType, clickState);
					}
					else rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				}
				else{
					if(_config.getReward().size()!=0){
						if(rmp.hasPermission("resourcemadness.reward.clear")){
							rmp.sendMessage(RMText.getLabel("sign.clear_reward"));
							_config.setInterface(InterfaceState.REWARD_CLEAR);
						}
						else rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
					}
				}
				break;
			case TOOLS:
				if(rmp.getPlayer().isSneaking()){
					if(rmp.hasPermission("resourcemadness.tools.byhand")){
						addItemToStash(rmp, rmStash, claimType, clickState);
					}
					else rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				}
				else{
					if(_config.getTools().size()!=0){
						if(rmp.hasPermission("resourcemadness.tools.clear")){
							rmp.sendMessage(RMText.getLabel("sign.clear_tools"));
							_config.setInterface(InterfaceState.TOOLS_CLEAR);
						}
						else rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
					}
				}
				break;
			}
		}
		else rmChest.clearContentsExceptHacked();
		updateSigns();
	}
	
	public void addItemToStash(RMPlayer rmp, RMStash rmStash, ClaimType claimType, ClickState clickState){
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
	public void claimFound(RMPlayer rmp, ItemStack... items){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		rmp.claim(_config.getFound(), ClaimType.FOUND, items);
	}
	
	public void claimFoundToChest(Block b, RMPlayer rmp, ItemStack... items){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(_config.getState()!=GameState.SETUP){
			rmp.sendMessage(RMText.getLabel("claim.found.cannot_while_ingame"));
			return;
		}
		rmp.claimToChest(b, ClaimType.FOUND, null, items);
	}
	
	//Check Equal Distribution
	public List<String> checkRewardEqual(RMPlayer rmp){
		return checkRewardEqualDistribution(rmp, _config.getReward());
	}
	public List<String> checkToolsEqual(RMPlayer rmp, List<RMPlayer> rmPlayers){
		return checkEqualDistribution(rmp, rmPlayers, _config.getTools());
	}
	
	public List<String> checkRewardEqualDistribution(RMPlayer rmp, RMStash rmStash){
		List<String> strUnequal = new ArrayList<String>();
		if(rmStash.size()>0){
			for(RMTeam rmTeam : _config.getTeams()){
				if(rmTeam!=null){
					int numOfPlayers = rmTeam.getPlayers().length;
					if(numOfPlayers>0){
						String strItems = "";
						for(RMStashItem rmStashItem : rmStash.values()){
							int amount = rmStashItem.getAmount();
							if(amount%numOfPlayers!=0){
								strItems+=Material.getMaterial(rmStashItem.getId()).name()+":"+ChatColor.GRAY+amount+ChatColor.WHITE+", ";
							}
						}
						if(strItems.length()>0){
							strItems = RMTextHelper.stripLast(strItems, ", ");
							strUnequal.add(rmTeam.getTeamColorString()+":["+ChatColor.WHITE+strItems+RMHelper.getChatColorByDye(rmTeam.getTeamColor())+"]");
						}
					}
				}
			}
		}
		return strUnequal;
	}
	
	public List<String> checkEqualDistribution(RMPlayer rmp, List<RMPlayer> rmPlayers, RMStash rmStash){
		List<String> strUnequal = new ArrayList<String>();
		if(rmStash.size()>0){
			int numOfPlayers = rmPlayers.size();
			if(numOfPlayers>0){
				String strItems = "";
				for(RMStashItem rmStashItem : rmStash.values()){
					int amount = rmStashItem.getAmount();
					if(amount%numOfPlayers!=0){
						strItems+=Material.getMaterial(rmStashItem.getId()).name()+":"+ChatColor.GRAY+amount+ChatColor.WHITE+", ";
					}
				}
				if(strItems.length()>0){
					strItems = RMTextHelper.stripLast(strItems, ", ");
					strUnequal.add("["+ChatColor.WHITE+strItems+"]");
				}
			}
		}
		return strUnequal;
	}
	
	//Distribution
	public void distributeReward(RMTeam rmTeam){
		distributeStash(rmTeam, ClaimType.REWARD);
	}
	public void distributeTools(RMTeam rmTeam){
		distributeStash(rmTeam, ClaimType.TOOLS);
	}
	
	public void distributeStash(RMTeam rmTeam, ClaimType claimType){
		RMStash stash = new RMStash();
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
	
	public void distributeStashToTeamDivide(RMTeam rmTeam, RMStash stash, ClaimType claimType){
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	public void distributeStashToTeamsDivide(RMStash stash, ClaimType claimType){
		RMPlayer[] players = getTeamPlayers();
		int divisor = players.length;
		for(RMPlayer rmp : players){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	
	public void distributeFromChests(RMTeam rmTeam, ClaimType claimType){
		if(rmTeam==null) return;
		RMStash stash = new RMStash();
		for(RMChest rmChest : getChests()){
			stash.addItems(rmChest.getContents());
			rmChest.clearContents();
		}
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	public void distributeFromChest(RMChest rmChest, ClaimType claimType){
		RMTeam rmTeam = rmChest.getTeam();
		if(rmTeam==null) return;
		RMStash stash = new RMStash(rmChest.getContents());
		rmChest.clearContents();
		int divisor = rmTeam.getPlayers().length;
		for(RMPlayer rmp : rmTeam.getPlayers()){
			List<ItemStack> items = distributeByDivisor(stash, divisor);
			switch(claimType){
				case ITEMS: rmp.getItems().addItems(items); break;
				case REWARD: rmp.getReward().addItems(items); break;
				case TOOLS: rmp.getTools().addItems(items); break;
			}
			divisor--;
		}
	}
	
	public List<ItemStack> distributeByDivisor(RMStash stash, int divisor){
		List<ItemStack> foundItems = new ArrayList<ItemStack>();
		RMStashItem[] stashItems = stash.values().toArray(new RMStashItem[stash.values().size()]).clone();
		
		for(RMStashItem item : stashItems){
			int amount = (int)Math.ceil((double)item.getAmount()/(double)divisor);
			foundItems.addAll(stash.removeByIdAmount(item.getId(), amount));
		}
		return foundItems;
	}
	
	public void stashChestsContents(){
		for(RMChest rmChest : getChests()){
			rmChest.addInventoryToStash();
		}
	}
	
	public void returnChestsContents(){
		for(RMChest rmChest : getChests()){
			rmChest.returnInventoryFromStash();
		}
	}
	
	public void snatchInventories(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				if(rmp.getPlayer()!=null){
					rmp.addItemsFromInventory(rmp.getItems());
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
	
	public void healPlayer(){
		for(RMPlayer rmp : getTeamPlayers()){
			rmp.restoreHealth();
		}
	}
	
	public void warpPlayersToSafety(){
		for(RMTeam rmt : getTeams()){
			for(RMPlayer rmp : rmt.getPlayers()){
				rmp.warpToSafety();
			}
		}
	}
	
	public RMTeam findWinningTeam(){
		for(RMChest rmChest : getChests()){
			if(rmChest.getItemsLeftInt()==0){
				return rmChest.getTeam();
			}
		}
		return null;
	}

	public RMTeam findLeadingTeam(){
		List<RMTeam> rmTeams = getTeams();
		List<RMTeam> leading = new ArrayList<RMTeam>();
		List<RMTeam> added = new ArrayList<RMTeam>();
		RMTeam leadingTeam = null;
		for(RMTeam team : rmTeams){
			if(leading.size()==0) leading.add(team);
			else{
				Iterator<RMTeam> iter = leading.iterator();
				while(iter.hasNext()){
					RMTeam found = iter.next();
					int teamTotalLeft = team.getChest().getTotalLeft();
					int foundTotalLeft = found.getChest().getTotalLeft();
					if(teamTotalLeft<foundTotalLeft) iter.remove();
					if(teamTotalLeft<=foundTotalLeft) added.add(team);
				}
				leading.addAll(added);
			}
		}
		if(leading.size()==1) leadingTeam = leading.get(0);
		return leadingTeam;
	}
	
	public boolean hasMinimumPlayers(boolean broadcast){
		int minPlayers = _config.getSettingInt(Setting.minplayers);
		if(minPlayers<_config.getTeams().size()) minPlayers = _config.getTeams().size(); 
		if(getTeamPlayers().length<minPlayers){
			if(broadcast) broadcastMessage(RMText.getLabelArgs("game.not_enough_players", ""+_config.getSettingInt(Setting.minplayers)));
			return false;
		}
		return true;
	}
	
	public boolean hasMinimumTeamPlayers(boolean broadcast){
		for(RMTeam rmTeam : _config.getTeams()){
			if(rmTeam.getPlayers().length<_config.getSettingInt(Setting.minteamplayers)){
				//rmp.sendMessage("Each team must have at least one player.");
				if(broadcast) broadcastMessage(RMText.getLabelArgs("game.not_enough_team_players", ""+_config.getSettingInt(Setting.minteamplayers)));
				return false;
			}
		}
		return true;
	}
	
	public void checkPlayerQuit(RMPlayer rmp, RMTeam rmTeam){
		if(!rmTeam.hasMininumPlayers()){
			rmTeam.isDisqualified(true);
			rmTeam.clearPlayers();
			rmTeam.teamMessage(RMText.getLabel("team.disqualified"));
			teamBroadcastMessage(RMText.getLabelArgs("team.disqualified.broadcast", rmTeam.getTeamColorString()), rmTeam);
		}
		if(!hasMinimumPlayers(false)){
			RMTeam winningTeam = null;
			for(RMTeam rmt : getTeams()){
				if(rmt!=rmTeam){
					winningTeam = rmt;
				}
			}
			setWinningTeam(winningTeam);
			gameOver();
		}
	}
	
	public void toggleReady(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.ready")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_command"));
			return;
		}
		rmp.toggleReady();
		if(rmp.getReady()){
			rmp.sendMessage(RMText.getLabel("ready"));
			broadcastMessage(RMText.getLabelArgs("ready.broadcast", rmp.getTeam().getChatColor()+rmp.getName()), rmp);
		}
		else{
			rmp.sendMessage(RMText.getLabel("ready.not"));
			broadcastMessage(RMText.getLabelArgs("ready.not.broadcast", rmp.getTeam().getChatColor()+rmp.getName()), rmp);
		}
	}
	
	public void checkReady(){
		if(!hasMinimumPlayers(false)) return;
		if(!hasMinimumTeamPlayers(false)) return;
		RMPlayer[] players = getTeamPlayers();
		for(RMPlayer rmp : players){
			if(!rmp.getReady()) return;
		}
		clearReady();
		startGame(null);
	}
	
	public void clearReady(){
		RMPlayer[] players = getTeamPlayers();
		for(RMPlayer rmp : players){
			rmp.setReady(false);
		}
	}
	
	public void initGameStart(){
		stashChestsContents();
		//Clear player's inventory
		if(_config.getSettingBool(Setting.clearinventory)) snatchInventories();
		if(_config.getSettingBool(Setting.healplayer)) healPlayer();
		_config.getTimer().reset();
		_config.getTimer().addTimeMessage(_config.getTimer().getTimeLimit());
		updateSigns();
	}
	
	public boolean checkStartConditions(RMPlayer rmp){
		String rewardNotEqual = RMTextHelper.getStringByStringList(checkRewardEqual(rmp), ", ");
		String toolsNotEqual = RMTextHelper.getStringByStringList(checkToolsEqual(rmp, Arrays.asList(getTeamPlayers())), ", ");
		//Warn unequal
		if((_config.getSettingBool(Setting.warnunequal))||(!_config.getSettingBool(Setting.allowunequal))){
			if(rewardNotEqual.length()!=0) broadcastInstead(rmp, RMText.getLabelArgs("reward.unequal", rewardNotEqual));
			if(toolsNotEqual.length()!=0) broadcastInstead(rmp, RMText.getLabelArgs("tools.unequal", toolsNotEqual));
		}
		//Allow unequal
		if(!_config.getSettingBool(Setting.allowunequal)){
			boolean itemsNotEqual = false;
			if(rewardNotEqual.length()!=0){
				broadcastInstead(rmp, RMText.getLabel("reward.cannot_be_equal"));
				itemsNotEqual = true;
			}
			if(toolsNotEqual.length()!=0){
				broadcastInstead(rmp, RMText.getLabel("tools.cannot_be_equal"));
				itemsNotEqual = true;
			}
			if(itemsNotEqual){
				broadcastInstead(rmp, RMText.getLabel("common.canceled"));
				return false;
			}
		}
		else{
			if(rewardNotEqual.length()!=0){
				broadcastMessage(RMText.getLabel("reward.will_not_be_equal"));
			}
			if(toolsNotEqual.length()!=0){
				broadcastMessage(RMText.getLabel("tools.will_not_be_equal"));
			}
		}
		if(!hasMinimumPlayers(true)) return false;
		if(!hasMinimumTeamPlayers(true)) return false;
		return true;
	}
	
	public void startGameRandomize(RMPlayer rmp, int random){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		setRandomizeAmount(rmp, rmp.getRequestInt());
		startGame(rmp);
		
	}
	
	public void startGame(RMPlayer rmp){
		if(rmp!=null){
			if(!rmp.hasOwnerPermission(_config.getOwnerName())){
				rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
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
			rmp.sendMessage(RMText.getLabel("start.stop_game_first"));
			return;
		}
		for(RMChest rmChest : getChests()){
			rmChest.clearItems();
		}
		//Populate items by filter
		_config.getItems().populateByFilter(_config.getFilter());
		
		//Filter is empty
		if(_config.getItems().size()==0){
			broadcastInstead(rmp, RMText.getLabel("filter.configure"));
			return;
		}
		
		if(!checkStartConditions(rmp)) return;
		
		//Randomize
		if(_config.getRandomizeAmount()>0) _config.getItems().randomize(_config.getRandomizeAmount());
		else _config.getItems().randomize(_config.getSettingInt(Setting.random));

		broadcastMessage(RMText.getLabel("game.prepare"));
		clearReady();
		_config.setState(GameState.COUNTDOWN);
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
	public void stopGame(RMPlayer rmp, boolean clearRandom){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		switch(_config.getState()){
			case GAMEPLAY:
				//rmp.sendMessage("Stopping game...");
				broadcastMessage(RMText.getLabel("stop.stopping_game"));
				initGameOver(clearRandom);
				return;
			case SETUP: rmp.sendMessage(RMText.getLabel("msg.no_game_in_progress")); return;
			case COUNTDOWN: rmp.sendMessage(RMText.getLabel("game.countdown.wait_to_start")); return;
		}
	}
	
	private void initGameOver(boolean clearRandom){
		if(clearRandom) _config.clearSetting(Setting.random);
		for(RMChest rmChest : getChests()){
			_config.getFound().addItems(rmChest.getItems());
			rmChest.clearItems();
			distributeFromChest(rmChest, ClaimType.ITEMS);
		}
		if(_config.getSettingBool(Setting.clearinventory)) returnInventories();
		returnChestsContents();
		if(_config.getSettingBool(Setting.warp)) warpPlayersToSafety();
		if(_config.getSettingBool(Setting.restore)) restoreLog();
		_config.setState(GameState.SETUP);
		clearTeamPlayers();
		updateSigns();
	}
	
	public void gameOver(){
		RMTeam rmTeam = getWinningTeam();
		if(rmTeam!=null){
			_config.setState(GameState.GAMEOVER);
			broadcastMessage(RMText.getLabelArgs("team.win_match.broadcast", rmTeam.getTeamColorString()));
			for(RMChest rmChest : getChests()){
				_config.getFound().addItems(rmChest.getItems());;
				rmChest.clearItems();
				distributeFromChest(rmChest, ClaimType.ITEMS);
			}
			for(RMTeam rmt : getTeams()){
				if(rmt!=rmTeam){
					for(RMPlayer rmPlayer : rmt.getPlayers()){
						rmPlayer.getTools().transferTo(rmPlayer.getItems());
						rmPlayer.getStats().add(RMStat.LOSSES);
						rmPlayer.getStats().add(RMStat.TIMES_PLAYED);
						_config.getStats().add(RMStat.LOSSES);
						_config.getStats().add(RMStat.TIMES_PLAYED);
						RMStats.add(RMStatServer.LOSSES);
						RMStats.add(RMStatServer.TIMES_PLAYED);
					}
				}
			}
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				rmPlayer.getTools().transferTo(rmPlayer.getItems());
				rmPlayer.getStats().add(RMStat.WINS);
				rmPlayer.getStats().add(RMStat.TIMES_PLAYED);
				_config.getStats().add(RMStat.WINS);
				_config.getStats().add(RMStat.TIMES_PLAYED);
				RMStats.add(RMStatServer.WINS);
				RMStats.add(RMStatServer.TIMES_PLAYED);
			}
			distributeReward(rmTeam);
			if(_config.getSettingBool(Setting.foundasreward)) distributeStashToTeamDivide(rmTeam, _config.getFound(), ClaimType.REWARD);
			//setWinPlayer(rmp);
			update();
			initGameOver(true);
		}
	}
	
	//UPDATE
	public void update(){
		switch(_config.getState()){
		case SETUP:
			checkReady();
			break;
		case COUNTDOWN:
			//cdTimer = 0; //////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(cdTimer%10==0){
				if(cdTimer!=0){
					broadcastMessage(RMText.getLabelArgs("game.countdown", ""+cdTimer/10));
					updateSigns();
				}
			}
			if(cdTimer>0){
				cdTimer-=10;
			}
			else{
				initGameStart();
				broadcastMessage(RMText.getLabel("game.start_match"));
				cdTimer = cdTimerLimit;
				_config.setState(GameState.GAMEPLAY);

				for(RMTeam rmt : getTeams()){
					for(RMPlayer rmp : rmt.getPlayers()){
						rmp.getTools().transferTo(rmp.getItems());
						updateGameplayInfo(rmp, rmt);
					}
				}

				distributeTools(null);
				for(RMTeam rmt : getTeams()){
					for(RMPlayer rmp : rmt.getPlayers()){
						rmp.claimTools();
					}
					
				}
				if(_config.getSettingBool(Setting.warp)) warpPlayersToSafety();
				updateSigns();
			}
			break;
		case GAMEPLAY:
			RMGameTimer timer = _config.getTimer();
			if((getOnlineTeamPlayers().length==0)||(timer.getTimeLimit()==0)) return;
			if(timer.getTimeElapsed()<timer.getTimeLimit()){
				timer.announceTimeLeft(this);
				timer.addTimeElapsed();
			}
			else if(timer.getTimeElapsed()==timer.getTimeLimit()){
				timer.announceTimeLeft(this);
				setWinningTeam(findLeadingTeam());
				gameOver();
				if(getWinningTeam()==null){
					teamBroadcastMessage(RMText.getLabelColorize("game.sudden_death", ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE));
					timer.addTimeElapsed();
				}
			}
			else{
				setWinningTeam(findLeadingTeam());
				gameOver();
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
					
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, getSignString(RMText.getLabel("sign.filter"), ": "+items));
						sign.setLine(1, getSignString(RMText.getLabel("sign.total"), ": "+lineTotal));
						sign.setLine(2, getSignString(RMText.getLabel("sign.ingame"), ": "+getTeamPlayers().length+getTextPlayersOfMax()));
						sign.setLine(3, getSignString(RMText.getLabel("sign.inteam"), ": "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax()));
						sign.update();
					}
					break;
				case REWARD:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, getSignString(RMText.getLabel("sign.reward"), ": "+_config.getReward().size()));//+items);
						sign.setLine(1, getSignString(RMText.getLabel("sign.total"), ": "+_config.getReward().getAmount()));//lineTotal);
						sign.setLine(2, getSignString(RMText.getLabel("sign.ingame"), ": "+getTeamPlayers().length+getTextPlayersOfMax()));
						sign.setLine(3, getSignString(RMText.getLabel("sign.inteam"), ": "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax()));
						sign.update();
					}
					break;
				case TOOLS:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, getSignString(RMText.getLabel("sign.tools"), ": "+_config.getTools().size()));//+items);
						sign.setLine(1, getSignString(RMText.getLabel("sign.total"), ": "+_config.getTools().getAmount()));//lineTotal);
						sign.setLine(2, getSignString(RMText.getLabel("sign.ingame"), ": "+getTeamPlayers().length+getTextPlayersOfMax()));
						sign.setLine(3, getSignString(RMText.getLabel("sign.inteam"), ": "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax()));			
						sign.update();
					}
					break;
				case FILTER_CLEAR: case REWARD_CLEAR: case TOOLS_CLEAR:
					for(RMTeam rmTeam : getTeams()){
						Sign sign = rmTeam.getSign();
						sign.setLine(0, RMText.getLabel("sign.clear0"));
						sign.setLine(1, RMText.getLabel("sign.clear1"));
						sign.setLine(2, RMText.getLabel("sign.clear2"));
						sign.setLine(3, RMText.getLabel("sign.clear3"));
						sign.update();
					}
					break;
				}
				break;
			case COUNTDOWN:
				for(Sign sign : getSigns()){
					sign.setLine(0, RMText.getLabelArgs("sign.countdown0", ""+cdTimer/10));
					sign.setLine(1, RMText.getLabelArgs("sign.countdown1", ""+cdTimer/10));
					sign.setLine(2, RMText.getLabelArgs("sign.countdown2", ""+cdTimer/10));
					sign.setLine(3, RMText.getLabelArgs("sign.countdown3", ""+cdTimer/10));
					sign.update();
				}
				break;
			case GAMEPLAY:
				for(RMTeam rmTeam : getTeams()){
					Sign sign = rmTeam.getSign();
					RMChest rmChest = rmTeam.getChest();
					if(!rmTeam.isDisqualified()){
						sign.setLine(0, getSignString(RMText.getLabel("sign.items_left"), ": "+rmChest.getItemsLeftInt()));
						sign.setLine(1, getSignString(RMText.getLabel("sign.total"), ": "+rmChest.getTotalLeft()));
						sign.setLine(2, getSignString(RMText.getLabel("sign.ingame"), ": "+getTeamPlayers().length+getTextPlayersOfMax()));
						sign.setLine(3, getSignString(RMText.getLabel("sign.inteam"), ": "+rmTeam.getPlayers().length+getTextTeamPlayersOfMax()));
					}
					else{
						sign.setLine(0, RMText.getLabel("sign.team_disqualified0"));
						sign.setLine(1, RMText.getLabel("sign.team_disqualified1"));
						sign.setLine(2, RMText.getLabel("sign.team_disqualified2"));
						sign.setLine(3, RMText.getLabel("sign.team_disqualified3"));
					}
					sign.update();
				}
				break;
			case GAMEOVER:
				break;
			case PAUSED:
				for(RMTeam rmTeam : getTeams()){
					Sign sign = rmTeam.getSign();
					sign.setLine(0, RMText.getLabel("sign.game_paused0"));
					sign.setLine(1, RMText.getLabel("sign.game_paused1"));
					sign.setLine(2, RMText.getLabel("sign.game_paused2"));
					sign.setLine(3, RMText.getLabel("sign.game_paused3"));
					sign.update();
				}
				break;
		}
	}
	
	public void sendFilterInfo(RMPlayer rmp){
		updateSigns();
		String items = "";
		Integer[] array = _config.getFilter().keySet().toArray(new Integer[_config.getFilter().keySet().size()]);
		Arrays.sort(array);
		if(array.length>rm.config.getTypeLimit()){
			for(Integer id : array){
				items += ChatColor.WHITE+""+id+RMTextHelper.includeItem(_config.getFilter().getItem(id))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer id : array){
				items += ChatColor.WHITE+""+Material.getMaterial(id)+RMTextHelper.includeItem(_config.getFilter().getItem(id))+ChatColor.WHITE+", ";
			}
		}
		if(items.length()>0){
			items = items.substring(0, items.length()-2);
			rmp.sendMessage(RMText.getLabelArgs("filter.items", items));
		}
		else rmp.sendMessage(RMText.getLabel("filter.empty"));
	}
	
	public void sendFilterInfoString(RMPlayer rmp){
		rmp.sendMessage(ChatColor.AQUA+RMFilter.encodeFilterToString(getGameConfig().getFilter().getItems(), false));
	}
	
	public void sendRewardInfo(RMPlayer rmp){
		updateSigns();
		String items = RMTextHelper.getStringSortedItems(_config.getReward().getItems());
		if(items.length()>0){
			rmp.sendMessage(RMText.getLabelArgs("reward.items", items));
		}
		else rmp.sendMessage(RMText.getLabel("reward.empty"));
	}
	
	public void sendRewardInfoString(RMPlayer rmp){
		rmp.sendMessage(ChatColor.AQUA+getGameConfig().getReward().encodeToString(false));
	}
	
	public void sendToolsInfo(RMPlayer rmp){
		updateSigns();
		String items = RMTextHelper.getStringSortedItems(_config.getTools().getItems());
		if(items.length()>0){
			rmp.sendMessage(RMText.getLabelArgs("tools.items", items));
		}
		else rmp.sendMessage(RMText.getLabel("tools.empty"));
	}
	
	public void sendToolsInfoString(RMPlayer rmp){
		rmp.sendMessage(ChatColor.AQUA+getGameConfig().getTools().encodeToString(false));
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
			updateSigns();
		}
	}
	public void updateGameplayInfo(RMPlayer rmp, RMTeam rmTeam){
		updateGameplayInfo(rmp, rmTeam, rm.config.getTypeLimit());
	}
	
	public void updateGameplayInfo(RMPlayer rmp, RMTeam rmTeam, int typeLimit){
		//RMTeam rmTeam = rmp.getTeam();
		String strItems = "";
		RMChest rmChest = rmTeam.getChest();
		
		//Sort
		Integer[] array = _config.getItems().keySet().toArray(new Integer[_config.getItems().keySet().size()]);
		Arrays.sort(array);
		
		HashMap<Integer, RMItem> items = rmChest.getRMItems();
		
		if(array.length>typeLimit){
			for(Integer id : array){
				RMItem rmItem = _config.getItems().getItem(id);
				int amount = rmItem.getAmount();
				if(items.containsKey(id)) amount -= items.get(id).getAmount();
				if(amount>0) strItems += ChatColor.WHITE+""+id+RMTextHelper.includeItem(new RMItem(id, amount))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer id : array){
				RMItem rmItem = _config.getItems().getItem(id);
				int amount = rmItem.getAmount();
				if(items.containsKey(id)) amount -= items.get(id).getAmount();
				if(amount>0) strItems += ChatColor.WHITE+""+Material.getMaterial(id)+RMTextHelper.includeItem(new RMItem(id, amount))+ChatColor.WHITE+", ";
			}
		}
		if(strItems.length()>0){
			strItems = strItems.substring(0, strItems.length()-2);
			rmp.sendMessage(RMText.getLabelArgs("items.left", strItems));
		}
		else rmp.sendMessage(RMText.getLabel("items.empty"));
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
		if(_config.getSettingBool(Setting.warnhacked)) warnHackMaterialsByItems(items);
		if(!_config.getSettingBool(Setting.allowhacked)) items = removeHackMaterialsByItems(items);
		return items;
	}
	
	//Try Add Items
	public void tryAddItemsToFilter(Block b, RMPlayer rmp, ClickState clickState){
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
								RMItem rmItem = new RMItem(id);
								if(_config.getAddOnlyOneStack()) rmItem.setAmount(rmItem.getMaxStackSize());
								else rmItem.setAmount(is.getAmount());
								
								switch(clickState){
									case NONE: case LEFT:
										switch(_config.getFilter().addItem(id, rmItem, force)){
											case ADD:
												addedItems+=ChatColor.WHITE+item.name()+RMTextHelper.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+RMTextHelper.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
										}
										break;
									case RIGHT:
										switch(_config.getFilter().removeItem(id, rmItem, force)){
											case MODIFY:
												modifiedItems+=ChatColor.WHITE+item.name()+RMTextHelper.includeItem(rmItem)+ChatColor.WHITE+", ";
												break;
											case REMOVE:
												removedItems+=ChatColor.WHITE+item.name()+RMTextHelper.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE+", ";
												break;
										}
										break;
								}
							}
						}
					}
				}
				if(addedItems.length()>0){
					addedItems = RMTextHelper.stripLast(addedItems, ",");
					rmp.sendMessage(RMText.getLabelArgs("common.added", addedItems));
				}
				if(modifiedItems.length()>0){
					modifiedItems = RMTextHelper.stripLast(modifiedItems, ",");
					rmp.sendMessage(RMText.getLabelArgs("common.modified", modifiedItems));
				}
				if(removedItems.length()>0){
					removedItems = RMTextHelper.stripLast(removedItems, ",");
					rmp.sendMessage(RMText.getLabelArgs("common.removed", removedItems));
				}
			}
			else if(rmp.getPlayer().isSneaking()){
				if(clickState!=ClickState.NONE){
					if(rmp.hasPermission("resourcemadness.filter.byhand")) tryAddItemToFilter(rmp, clickState);
					else rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
				}
			}
			else if(_config.getFilter().size()>0){
				if(rmp.hasPermission("resourcemadness.filter.clear")){
					if(clickState == ClickState.NONE){
						rmp.sendMessage(RMText.getLabel("sign.clear_items"));
						_config.setInterface(InterfaceState.FILTER_CLEAR);
					}
				}
				else rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			}
			updateSigns();
		}
	}
	
	public void tryAddItemToFilter(RMPlayer rmp, ClickState clickState){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
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
								rmp.sendMessage(RMText.getLabelArgs("common.added", ChatColor.WHITE+mat.name()+RMTextHelper.includeItem(rmItem)));
								break;
							case MODIFY:
								rmp.sendMessage(RMText.getLabelArgs("common.modified", ChatColor.WHITE+mat.name()+RMTextHelper.includeItem(rmItem)));
								break;
						}
						break;
					case RIGHT:
						switch(_config.getFilter().removeItem(id, rmItem, force)){
							case MODIFY:
								rmp.sendMessage(RMText.getLabelArgs("common.modified", ChatColor.WHITE+mat.name()+RMTextHelper.includeItem(rmItem)));
								break;
							case REMOVE:
								rmp.sendMessage(RMText.getLabelArgs("common.removed", ChatColor.WHITE+mat.name()+RMTextHelper.includeItem(rmItem)));
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
			rmp.getStats().add(RMStat.ITEMS_FOUND_TOTAL, totalFound);
			_config.getStats().add(RMStat.ITEMS_FOUND_TOTAL, totalFound);
			RMStats.add(RMStatServer.ITEMS_FOUND_TOTAL, totalFound);
			if(added.size()>0){
				if(returned.size()>0){
					rmp.getTeam().teamMessage(RMText.getLabelArgs("items.left", getFormattedStringByHash(returned, rmp)));
					teamBroadcastMessage(RMText.getLabelArgs("team.items_left.broadcast", rmp.getTeam().getTeamColorString(), ""+rmChest.getItemsLeftInt(), ""+rmChest.getTotalLeft()), rmp.getTeam());
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
							if(rmp.getPlayer().isSneaking()) cycleMode(rmp, false);
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
							if(rmp.getPlayer().isSneaking()) addRewardByChest(rmp, getChestByBlock(b), ClickState.RIGHT);
							break;
						case WALL_SIGN:
							if(rmp.getPlayer().isSneaking()) cycleMode(rmp, false);
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
							if(rmp.getPlayer().isSneaking()) addToolsByChest(rmp, getChestByBlock(b), ClickState.RIGHT);
							break;
						case WALL_SIGN:
							if(rmp.getPlayer().isSneaking()) cycleMode(rmp, false);
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
							rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
							return;
						}
						if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.getPlayer().isSneaking()) cycleMode(rmp);
						else sendFilterInfo(rmp);
						break;
					case WOOL:
						if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case REWARD:
					switch(mat){
					case CHEST:
						if(!rmp.hasPermission("resourcemadness.reward")){
							rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
							return;
						}
						//if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						//else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						if(rmp.getPlayer().isSneaking()) addRewardByChest(rmp, getChestByBlock(b), ClickState.LEFT);
						else addRewardByChest(rmp, getChestByBlock(b), ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.getPlayer().isSneaking()) cycleMode(rmp);
						else sendRewardInfo(rmp);
						break;
					case WOOL:
						if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case TOOLS:
					switch(mat){
					case CHEST:
						if(!rmp.hasPermission("resourcemadness.tools")){
							rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
							return;
						}
						//if(rmp.getPlayer().isSneaking()) tryAddItemsToFilter(b, rmp, ClickState.LEFT);
						//else tryAddItemsToFilter(b, rmp, ClickState.NONE);
						if(rmp.getPlayer().isSneaking()) addToolsByChest(rmp, getChestByBlock(b), ClickState.LEFT);
						else addToolsByChest(rmp, getChestByBlock(b), ClickState.NONE);
						break;
					case WALL_SIGN:
						if(rmp.getPlayer().isSneaking()) cycleMode(rmp);
						else sendToolsInfo(rmp);
						break;
					case WOOL:
						if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
						else sendTeamInfo(rmp);
						break;
					}
					break;
				case FILTER_CLEAR: case REWARD_CLEAR: case TOOLS_CLEAR: //FILTER CLEAR
					switch(mat){
					case CHEST: case GLASS: case STONE:
						if(rmp.hasOwnerPermission(_config.getOwnerName())){
							rmp.sendMessage(RMText.getLabel("common.canceled"));
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
							rmp.sendMessage(RMText.getLabel("common.canceled"));
							_config.setInterface(getParentInterface(_config.getInterface()));
							updateSigns();
						}
						else{
							if(rmp.getPlayer().isSneaking()) joinQuitTeamByBlock(b, rmp, false);
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
				rmp.sendMessage(RMText.getLabel("game.is_paused"));
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
		int freeId=-1;
		HashMap<Integer, RMGame> games = RMGame.getGames();
		while(freeId==-1){
			if(!games.containsKey(i)) freeId = i;
			i++;
		}
		return freeId;
	}
	
	//Game
	public Block getMainBlock(){
		return _config.getPartList().getMainBlock();
	}
	
	//Game GET-SET
	public RMGame getGame(){
		return this;
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
	
	/*
	public void sendMessage(String message){
		RMPlayer[] rmplayers = _config.getPlayers().values().toArray(new RMPlayer[_config.getPlayers().values().size()]);
		for(RMPlayer rmp : rmplayers){
			rmp.sendMessage(message);
		}
	}
	*/
	
	public void broadcastInstead(RMPlayer rmp, String message){
		if(rmp!=null) rmp.sendMessage(message);
		else broadcastMessage(message);
	}

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
		if((ignorePlayer==null)||(rmp!=ignorePlayer)) rmp.sendMessage(message);
		List<RMTeam> teams = getTeams();
		for(RMTeam rmt : teams){
			RMPlayer[] players = rmt.getPlayers();
			for(RMPlayer rmPlayer : players){
				if(rmp!=rmPlayer) if((ignorePlayer==null)||(rmPlayer!=ignorePlayer)) rmPlayer.sendMessage(message);
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
	public void teamBroadcastMessage(String message, RMPlayer ignorePlayer){
		for(RMTeam rmTeam : getTeams()){
			rmTeam.teamMessage(message, ignorePlayer);
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
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		rmTeam.addPlayer(rmp);
	}
	public void quitTeam(RMTeam rmTeam, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		rmTeam.removePlayer(rmp);
	}
	public void quitTeam(RMGame rmGame, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		RMTeam rmTeam = rmGame.getTeamByPlayer(rmp);
		if(rmTeam!=null){
			rmTeam.removePlayer(rmp);
			//rmp.sendMessage("You quit the "+rmTeam.getTeamColorString()+ChatColor.WHITE+" team.");
		}
	}
	
	public RMTeam joinTeamByBlock(Block b, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.join")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return null;
		}
		RMTeam rmt = getTeamByBlock(b);
		if(rmt!=null){
			RMTeam rmTeam = getTeamByPlayer(rmp);
			if(rmTeam!=null){
				if(rmt!=rmTeam){
					rmp.sendMessage(RMText.getLabelArgs("join.must_quit_other_team", rmTeam.getTeamColorString()));
					return null;
				}
			}
			rmt.addRemovePlayer(rmp);
			return rmt;
		}
		else rmp.sendMessage(RMText.getLabel("msg.team_does_not_exist"));
		return null;
	}
	
	public RMTeam joinQuitTeamByBlock(Block b, RMPlayer rmp, boolean fromConsole){
		if(_config.getState() == GameState.SETUP){
			RMTeam rmt = getTeamByBlock(b);
			if(rmt!=null){
				RMTeam rmTeam = getTeamByPlayer(rmp);
				if(rmTeam!=null){
					if(rmt!=rmTeam){
						rmp.sendMessage(RMText.getLabelArgs("join.must_quit_other_team", rmTeam.getTeamColorString()));
						return null;
					}
				}
				rmt.addRemovePlayer(rmp);
				return rmt;
			}
			else rmp.sendMessage(RMText.getLabel("msg.team_does_not_exist"));
		}
		else{
			rmp.sendMessage("Teams: "+getTextTeamPlayers());
			if(rmp.isIngame()) rmp.sendMessage(RMText.getLabel("quit.game_in_progress"));
			else rmp.sendMessage(RMText.getLabel("join.game_in_progress"));
		}
		
		return null;
	}
	public RMTeam quitTeamByBlock(Block b, RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.quit")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return null;
		}
		RMTeam rmTeam = getTeamByBlock(b);
		if(rmTeam!=null){
			if(rmTeam.getPlayer(rmp.getName())!=null){
				rmTeam.removePlayer(rmp);
				rmp.sendMessage(RMText.getLabelArgs("quit.alt", rmTeam.getTeamColorString()));
				return rmTeam;
			}
			else rmp.sendMessage(RMText.getLabelArgs("quit.not_joined", rmTeam.getTeamColorString()));
		}
		return null;
	}
	public RMTeam getTeamByBlock(Block b){
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
	
	public RMTeam getTeamByDye(String arg){
		DyeColor color = RMHelper.getDyeByString(arg);
		if(color!=null) return getTeam(color);
		return null;
	}
	
	public RMTeam getTeamByPlayer(RMPlayer rmp){
		for(RMTeam team : _config.getTeams()){
			if(team!=null){
				RMPlayer rmTeamPlayer = team.getPlayer(rmp.getName());
				if(rmTeamPlayer!=null) return team;
			}
		}
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
	
	public RMPlayer[] getTeamPlayers(){
		List<RMPlayer> list = new ArrayList<RMPlayer>();
		for(RMTeam rmTeam : _config.getTeams()){
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				list.add(rmPlayer);
			}
		}
		return list.toArray(new RMPlayer[list.size()]);
	}
	
	public String[] getTeamPlayersNames(){
		List<String> list = new ArrayList<String>();
		for(RMTeam rmTeam : _config.getTeams()){
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				list.add(rmPlayer.getName());
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	public RMPlayer[] getOnlineTeamPlayers(){
		List<RMPlayer> list = new ArrayList<RMPlayer>();
		for(RMTeam rmTeam : _config.getTeams()){
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				if(rmPlayer.isOnline()) list.add(rmPlayer);
			}
		}
		return list.toArray(new RMPlayer[list.size()]);
	}
	
	public void clearTeamPlayers(){
		for(RMTeam rmTeam : getTeams()){
			rmTeam.isDisqualified(false);
			rmTeam.clearPlayers();
		}
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
	public RMPlayer[] getPlayersByTeam(RMTeam rmt){
		if(rmt!=null) return rmt.getPlayers();
		return null;
	}
	
	public RMPlayer getPlayerByName(String name){
		for(RMPlayer rmPlayer : getTeamPlayers()){
			if(rmPlayer.getName().equalsIgnoreCase(name)) return rmPlayer;
		}
		return null;
	}
	
	public RMPlayer getTeamPlayerByName(RMTeam rmTeam, String name){
		if(rmTeam!=null){
			for(RMPlayer rmPlayer : rmTeam.getPlayers()){
				if(rmPlayer.getName().equalsIgnoreCase(name)) return rmPlayer;
			}
		}
		return null;
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
	public Boolean tryParseFilter(Block b, RMPlayer rmp){
		if(rmp.hasOwnerPermission(_config.getOwnerName())){
			RMGame.setRequestPlayer(rmp);
			parseFilter(b, rmp);
			RMGame.clearRequestPlayer();
			rmp.clearRequestFilter();
			updateSigns();
			return true;
		}
		else rmp.sendMessage(RMText.getLabelArgs("filter.modify_owner", _config.getOwnerName()));
		return false;
	}
	
	private HashMap<Integer, RMItem> getItemsMatch(Inventory inv, HashMap<Integer, RMItem> hashItems){
		HashMap<Integer, RMItem> items = new HashMap<Integer, RMItem>();
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
						RMItem rmItem = new RMItem(item);
						rmItem.setAmount(rmItem.getAmount()+(overflow<0?overflow:0));
						items.put(id, rmItem);
					}
					else{
						RMItem rmItem = items.get(id);
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
	private void parseFilter(Block b, RMPlayer rmp){
		RMRequestFilter filter = rmp.getRequestFilter();
		if(filter!=null){
			FilterType filterType = filter.getFilterType();
			int randomize = filter.getRandomize();
			FilterState filterState = filter.getFilterState();
			HashMap<Integer, RMItem> items = filter.getItems();
			if((items!=null)&&(items.size()!=0)){
				switch(filterState){
				case REWARD: case TOOLS:
					if((filterType!=null)&&(filterType==FilterType.ADD)){
						switch(filterState){
							case REWARD: if(!_config.getSettingBool(Setting.infinitereward)) items = getItemsMatch(rmp.getPlayer().getInventory(), items); break;
							case TOOLS: if(!_config.getSettingBool(Setting.infinitetools)) items = getItemsMatch(rmp.getPlayer().getInventory(), items); break;
						}
					}
				}
				if(_config.getSettingBool(Setting.warnhacked)) warnHackMaterials(items);
				if(!_config.getSettingBool(Setting.allowhacked)){
					items = removeHackMaterials(items);
					if(items==null){
						rmp.sendMessage(RMText.getLabel("filter.no_items_modified"));
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
		rmp.sendMessage(RMText.getLabel("filter.no_items_modified"));
	}
	
	//Parse Filter Args
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, RMItem> items){
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
							added.add(ChatColor.WHITE+strItem+RMTextHelper.includeItem(items.get(item)));
							break;
						case MODIFY:
							modified.add(ChatColor.WHITE+strItem+RMTextHelper.includeItem(items.get(item)));	
							break;
						}
					}
					else{
						switch(_config.getFilter().removeAlwaysItem(item, items.get(item))){
						case REMOVE:
							removed.add(ChatColor.WHITE+strItem+RMTextHelper.includeItem(_config.getFilter().getLastItem()));
							break;
						}
					}
				}
			}
			
			if(added.size()>0) rmp.sendMessage(RMText.getLabelArgs("common.added", RMTextHelper.getFormattedStringByList(added)));
			if(modified.size()>0) rmp.sendMessage(RMText.getLabelArgs("common.modified", RMTextHelper.getFormattedStringByList(modified)));
			if(removed.size()>0) rmp.sendMessage(RMText.getLabelArgs("common.removed", RMTextHelper.getFormattedStringByList(removed)));
		}
		else{
			switch(filterState){
			case REWARD:
				if(_config.getSettingBool(Setting.infinitereward)) _config.getReward().setItems(RMFilter.convertToListItemStack(items));
				else _config.getReward().setItemsMatchInventory(rmp.getPlayer().getInventory(), rmp, ClaimType.REWARD, RMFilter.convertRMHashToHash(items));
				
				_config.getReward().showChanged(rmp);
				break;
			case TOOLS:
				if(_config.getSettingBool(Setting.infinitetools)) _config.getTools().setItems(RMFilter.convertToListItemStack(items));
				else _config.getTools().setItemsMatchInventory(rmp.getPlayer().getInventory(), rmp, ClaimType.TOOLS, RMFilter.convertRMHashToHash(items));
				_config.getTools().showChanged(rmp);
			break;
			}
		}
	}
	
	//Parse Filter Args Force
	private void parseFilterArgs(RMPlayer rmp, HashMap<Integer, RMItem> items, FilterType filterType){
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
								added.add(ChatColor.WHITE+strItem+RMTextHelper.includeItem(items.get(item))+ChatColor.WHITE);
								break;
							case MODIFY:
								modified.add(ChatColor.WHITE+strItem+RMTextHelper.includeItem(items.get(item))+ChatColor.WHITE);
								break;
						}
					}
				}
				if(added.size()>0) rmp.sendMessage(RMText.getLabelArgs("common.added", RMTextHelper.getFormattedStringByList(added)));
				if(modified.size()>0) rmp.sendMessage(RMText.getLabelArgs("common.modified", RMTextHelper.getFormattedStringByList(modified)));
				break;
			case SUBTRACT:
				for(Integer item : arrayItems){
					Material mat = Material.getMaterial(item);
					if(mat!=Material.AIR){
						if(getId) strItem = ""+item;
						else strItem = mat.name();
						switch(_config.getFilter().removeItem(item, items.get(item), true)){
							case MODIFY:
								modified.add(ChatColor.WHITE+strItem+RMTextHelper.includeItem(items.get(item))+ChatColor.WHITE);
								break;
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+RMTextHelper.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE);
								break;
						}
					}
				}
				if(modified.size()>0) rmp.sendMessage(RMText.getLabelArgs("common.modified", RMTextHelper.getFormattedStringByList(modified)));
				if(removed.size()>0) rmp.sendMessage(RMText.getLabelArgs("common.removed", RMTextHelper.getFormattedStringByList(removed)));
				break;
			case CLEAR:
				for(Integer item : arrayItems){
					Material mat = Material.getMaterial(item);
					if(mat!=Material.AIR){
						if(getId) strItem = ""+item;
						else strItem = mat.name();
						switch(_config.getFilter().removeAlwaysItem(item, items.get(item))){
							case REMOVE:
								removed.add(ChatColor.WHITE+strItem+RMTextHelper.includeItem(_config.getFilter().getLastItem())+ChatColor.WHITE);
								break;
						}
					}
				}
				if(removed.size()>0) rmp.sendMessage(RMText.getLabelArgs("common.removed", RMTextHelper.getFormattedStringByList(removed)));
				break;
			}
		}
		else{
			List<ItemStack> listItems = RMFilter.convertToListItemStack(items);
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
	public String getFormattedStringByHash(HashMap<Integer, RMItem> items, RMPlayer rmp){
		RMChest rmChest = rmp.getTeam().getChest();
		String line = "";
		Integer[] array = items.keySet().toArray(new Integer[items.keySet().size()]);
		Arrays.sort(array);
		boolean useId = array.length>rm.config.getTypeLimit()?true:false;
		for(Integer item : array){
			String itemId = useId?""+item:Material.getMaterial(item).name();
			RMItem rmItem = items.get(item);
			int amount = rmItem.getAmount(); 
			if(amount!=-1){
				itemId = Material.getMaterial(item).name();
				if(amount!=0){
					line+=ChatColor.GREEN+itemId+RMTextHelper.includeItem(rmItem)+ChatColor.WHITE+", ";
				}
				else line+=ChatColor.DARK_GREEN+itemId+":0"+ChatColor.WHITE+", ";
			}
			else{
				if(rmChest.getRMItems().containsKey(item)) amount = rmChest.getItemLeft(item).getAmount();
				else amount = _config.getItems().getItem(item).getAmount();
				if(amount!=0) line+=ChatColor.WHITE+itemId+RMTextHelper.includeItem(new RMItem(item, amount))+ChatColor.WHITE+", ";
			}
		}
		line = RMTextHelper.stripLast(line, ",");
		return line;
	}
	
	public String getSimpleFormattedStringByHash(HashMap<Integer, RMItem> items){
		String line = "";
		Integer[] array = items.keySet().toArray(new Integer[items.keySet().size()]);
		Arrays.sort(array);
		for(Integer item : array){
			RMItem rmItem = items.get(item);
			line+=ChatColor.WHITE+Material.getMaterial(item).name()+RMTextHelper.includeItem(new RMItem(item, rmItem.getAmount()))+ChatColor.WHITE+", ";
		}
		line = RMTextHelper.stripLast(line, ",");
		return line;
	}
	
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
	public HashMap<Integer, RMItem> removeHackMaterials(HashMap<Integer, RMItem> materials){
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
	public void addLog(BlockState bState){
		_config.getLog().add(bState);
	}
	
	//Clear Log
	public void clearLog(){
		_config.getLog().clear();
	}
	
	public void restoreLog(){
		if(rm.config.getUseRestore()) if(_config.getLog().restore()) broadcastMessage(RMText.getLabel("restore.success"));
	}
	
	//Restore World
	public void restoreWorld(RMPlayer rmp){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		switch(_config.getState()){
		case SETUP:
			RMLog log = _config.getLog();
			if(log.getList().size()+log.getItemList().size()!=0) restoreLog();
			else rmp.sendMessage(RMText.getLabel("restore.nothing"));
			break;
		default: rmp.sendMessage(RMText.getLabel("restore.game_in_progress"));
		}
	}
	
	//HACK MATERIALS
	//Warn Hack Materials
	
	public void warnHackMaterialsByItems(List<ItemStack> items){
		warnHackMaterialsMessage(RMTextHelper.getFormattedStringByListMaterial(findHackMaterialsByItems(items)));
	}
	public void warnHackMaterials(List<Material> items){
		warnHackMaterialsMessage(RMTextHelper.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterials(HashMap<Integer, RMItem> items){
		warnHackMaterialsMessage(RMTextHelper.getFormattedStringByListMaterial(findHackMaterials(items)));
	}
	public void warnHackMaterialsMessage(String message){
		if(message.length()>0) _config.getOwner().sendMessage(RMText.getLabelArgs("common.not_allowed", message));
	}
	
	//Config
	
	public void setSetting(RMPlayer rmp, Setting setting, int value){
		if(_config.getSettingLibrary().get(setting).isLocked()){
			rmp.sendMessage(RMText.getLabel("msg.no_change_locked"));
			return;
		}
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		switch(setting){
			case minplayers: case maxplayers: case minteamplayers: case maxteamplayers:
				_config.setSetting(setting, value);
				_config.correctMinMaxNumbers(setting);
				rmp.sendMessage(RMText.getLabel("setting."+setting.name())+": "+getText(rmp, setting));
				sendMinMax(rmp);
				break;
			case safezone: case random:
				_config.setSetting(setting, value);
				rmp.sendMessage(RMText.getLabel("setting."+setting.name())+": "+getText(rmp, setting));
				break;
			case timelimit:
				_config.setSetting(setting, value);
				RMGameTimer timer = _config.getTimer();
				timer.setTimeLimit(value*60);
				timer.reset();
				timer.addTimeMessage(timer.getTimeLimit());
				rmp.sendMessage(RMText.getLabel("setting."+setting.name())+": "+getText(rmp, setting));
				break;
			default:
				if(value==-1) _config.toggleSetting(setting);
				else _config.setSetting(setting, value>0?true:false);
				rmp.sendMessage(RMText.getLabel("setting."+setting.name())+": "+isTrueFalse(_config.getSettingBool(setting)));
				break;
		}
		updateSigns();
	}
	
	public void setSetting(RMPlayer rmp, Setting setting, String str){
		if(_config.getSettingLibrary().get(setting).isLocked()){
			rmp.sendMessage(RMText.getLabel("msg.no_change_locked"));
			return;
		}
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		switch(setting){
			case password:
				_config.setSetting(setting, str);
				rmp.sendMessage(RMText.getLabel("setting.password")+": "+getPassword(str));
				break;
		}
		updateSigns();
	}

	//Set Randomize Amount
	public void setRandomizeAmount(RMPlayer rmp, int amount){
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		_config.setSetting(Setting.random, amount);
	}

	//Is True / False
	public String isTrueFalse(boolean bool){
		return (bool?(ChatColor.GREEN+RMText.getLabel("common.true")):(ChatColor.GRAY+RMText.getLabel("common.false")));
	}
	
	public String getPassword(String str){
		return getPassword(str, false);
	}
	
	public String getPassword(String str, boolean hide){
		if(str.length()!=0){
			if(hide) str = ChatColor.GREEN+RMTextHelper.genString("*", str.length());
			else str = ChatColor.GREEN+str;
			return str;
		}
		return RMText.getLabel("common.disabled");
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
	public void sendInfo(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.info")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		rmp.sendMessage(ChatColor.AQUA+RMTextHelper.firstLetterToUpperCase(_config.getWorldName())+ChatColor.WHITE+
				" "+RMText.getLabel("info.id")+": "+ChatColor.YELLOW+_config.getId()+ChatColor.WHITE+
				" "+RMText.getLabel("info.owner")+": "+ChatColor.YELLOW+_config.getOwnerName()+ChatColor.WHITE+
				" "+RMText.getLabel("info.timelimit")+": "+getText(rmp, Setting.timelimit));
		rmp.sendMessage(RMText.getLabel("info.players")+": "+ChatColor.GREEN+getTeamPlayers().length+ChatColor.WHITE+
				" "+RMText.getLabel("info.ingame")+": "+getText(rmp, Setting.minplayers)+ChatColor.WHITE+"-"+getText(rmp, Setting.maxplayers)+ChatColor.WHITE+
				" "+RMText.getLabel("info.inteam")+": "+getText(rmp, Setting.minteamplayers)+ChatColor.WHITE+"-"+getText(rmp, Setting.maxteamplayers));
		rmp.sendMessage(RMText.getLabel("info.teams")+": "+getTextTeamPlayers());
	}
	
	public void sendTeamInfo(RMPlayer rmp){
		sendInfo(rmp);
	}
	
	public void sendMinMax(RMPlayer rmp){
		rmp.sendMessage(RMText.getLabel("minmax.ingame")+": "+getText(rmp, Setting.minplayers)+ChatColor.WHITE+"-"+getText(rmp, Setting.maxplayers)+ChatColor.WHITE+" "+RMText.getLabel("minmax.inteam")+": "+getText(rmp, Setting.minteamplayers)+ChatColor.WHITE+"-"+getText(rmp, Setting.maxteamplayers));
	}
	
	public void getInfoFound(RMPlayer rmp){
		if(!rmp.hasPermission("resourcemadness.info.found")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		if(!rmp.hasOwnerPermission(_config.getOwnerName())){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		String items = RMTextHelper.getStringSortedItems(_config.getFound().getItems());
		if(items.length()>0){
			rmp.sendMessage(RMText.getLabelArgs("found.items", items));
		}
		else rmp.sendMessage(RMText.getLabel("found.no_items"));
	}
	
	public void sendSettings(RMPlayer rmp, int page){
		if(!rmp.hasPermission("resourcemadness.info.settings")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return;
		}
		int pages = 2;
		if(page<=0) page = 1;
		if(page>pages) page = pages;
		
		//SettingPrototype[] settingLib = _config.getSettingLibrary().toArray();
		Setting[] settings = Setting.values();
		SettingLibrary settingLib = _config.getSettingLibrary();

		int i = 0;
		int iEnd = 0;
		
		if(page==1){
			i = 0;
			iEnd = Setting.warnunequal.ordinal();
		}
		else if(page==2){
			i = Setting.warnunequal.ordinal();
			iEnd = Setting.values().length;
		}
		
		rmp.sendMessage(RMText.getLabelArgs("help_settings", ""+page, ""+pages));
		
		while(i<iEnd){
			Setting set = settings[i];
			SettingPrototype s = settingLib.get(set);
			String str = getText(rmp, set);
			rmp.sendMessage((s.isLocked()?ChatColor.RED:ChatColor.YELLOW)+RMText.getLabel("cmd.set."+s.name())+" "+str+" "+RMText.getLabel("setting."+s.name())+".");
			i++;
		}
	}
	
	public String getText(RMPlayer rmp, Setting set){
		String str = "";
		SettingLibrary settingLib = _config.getSettingLibrary();
		SettingPrototype setting = settingLib.get(set);
		if(setting instanceof SettingInt){
			RMDebug.warning("instanceof Int");
			str = ""+settingLib.getInt(set);
			RMDebug.warning("strInt: "+str);
		}
		if(setting instanceof SettingBool){
			RMDebug.warning("instanceof Bool");
			str = isTrueFalse(settingLib.getBool(set));
			RMDebug.warning("strBool: "+str);
		}
		if(setting instanceof SettingStr){
			RMDebug.warning("instanceof Str");
			str = settingLib.getStr(set);
			RMDebug.warning("strStr: "+str);
		}
		
		switch(set){
		case minplayers: case maxplayers: case minteamplayers: case maxteamplayers: case timelimit:
			RMDebug.warning("str: "+str);
			str = (settingLib.getInt(set)>0?(ChatColor.GREEN+""+str):(RMText.getLabel("common.no_limit")));
			RMDebug.warning("str: "+str);
			break;
		case safezone:
			str = (settingLib.getInt(set)>0?(ChatColor.GREEN+""+str):(RMText.getLabel("common.disabled")));
			break;
		case random:
			str = ChatColor.AQUA + (settingLib.getInt(set)>0?(ChatColor.GREEN+""+str+" "+RMText.getLabel("common.item(s)")):(RMText.getLabel("common.disabled")));
			break;
		case password:
			str = (!rmp.hasOwnerPermission(_config.getOwnerName())?getPassword(str,true):getPassword(str));
			break;
		}
		return str;
	}
	
	public void sendBanList(RMPlayer rmp, int id){
		int listLimit = 12;
		
		RMBanList banList = _config.getBanList();
		List<String> namesList = new ArrayList<String>();
		namesList.addAll(banList.keySet());
		
		if(namesList.size()==0){
			rmp.sendMessage(RMText.getLabel("banlist.empty"));
			return;
		}
		int i = 0;
		if(id<1) id=1;
		int size = (int)Math.ceil((double)namesList.size()/(double)listLimit);
		if(id>size) id=1;
		i=(id-1)*listLimit;
		rmp.sendMessage(RMText.getLabelArgs("banlist", ""+id, ""+size));
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
	
	//////////
	//Player//
	//////////
	
	//Get All Players
	public static HashMap<String, RMPlayer> getAllPlayers(){
		HashMap<String, RMPlayer> players = new HashMap<String, RMPlayer>();
		for(RMGame game : _games.values()){
			players.putAll(game._config.getPlayers());
		}
		return players;
	}
	
	////////
	//Game//
	////////
	
	private static RMGame addGame(RMGame rmGame){
		int id = rmGame.getGameConfig().getId();
		if(!_games.containsKey(id)){
			_games.put(id, rmGame);
		}
		return rmGame;
	}
	public static void tryAddGameFromConfig(RMGameConfig config){
		RMGame rmGame = addGame(new RMGame(config, rm));
		for(RMTeam rmt : rmGame.getGameConfig().getTeams()){
			rmt.setGame(rmGame);
		}
		rmGame.getGameConfig().correctMinMaxNumbers(Setting.minteamplayers);
		rmGame.updateSigns();
	}
	
	public static HandleState tryAddGame(Block b, RMPlayer rmp, Block bRemove){
		if(!rmp.hasPermission("resourcemadness.add")){
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return HandleState.NONE;
		}
		RMPartList partList;
		if(bRemove!=null) partList = new RMPartList(b, bRemove, rm);
		else partList = new RMPartList(b, rm);
		RMGame rmGame = getGameByBlock(partList.getMainBlock(b));
		Boolean wasModified = false;
		RMGameConfig rmGameConfig = new RMGameConfig();
		if(rmGame==null){
			rmGame = RMGame.getGameByBlock(b);
		}
		if(rmGame!=null){
			if(!rmp.hasOwnerPermission(rmGame._config.getOwnerName())){
				rmp.sendMessage(RMText.getLabelArgs("game.owner", rmGame._config.getOwnerName()));
				return HandleState.NO_CHANGE;
			}
			if(!partList.matchPartList(rmGame._config.getPartList())){
				wasModified = true;
				rmGameConfig = rmGame._config;
				RMGame.removeGame(rmGame);
				rmGame = null;
			}
			else{
				if(rmGame.getTeams().size()==4){
					rmp.sendMessage(RMText.getLabelArgs("game.has_max_teams", ""+rmGame._config.getId()));
					return HandleState.NO_CHANGE;
				}
				rmp.sendMessage(RMText.getLabelArgs("game.exists", ""+rmGame._config.getId()));
				return HandleState.NO_CHANGE;
			}
		}
		
		if(partList.getStoneList().size()<2){
			rmp.sendMessage(RMText.getLabelArgs("game.missing_stone", ""+(2-partList.getStoneList().size())));
			return HandleState.NONE;
		}
	
		List<RMTeam> teams = partList.fetchTeams();
		if(teams.size()<2){
			rmp.sendMessage(RMText.getLabelArgs("game.not_enough_teams", "2"));
			return HandleState.NONE;
		}
		if(rm.config.getMaxGamesPerPlayer()>0){
			if(getGamesByOwner(rmp).size()>=rm.config.getMaxGamesPerPlayer()){
				rmp.sendMessage(RMText.getLabel("game.max_games_reached"));
				return HandleState.NONE;
			}
		}
		rmGame = addGame(new RMGame(partList, rmp, rm));
		for(RMTeam rmt : teams){
			rmGame.addTeam(rmt);
		}
		if(wasModified){
			RMGameConfig config = rmGame.getGameConfig();
			config.getDataFrom(rmGameConfig);
			rmGameConfig = null;
			rmp.sendMessage(RMText.getLabelArgs("game.modified", ""+rmGame._config.getId()));
		}
		else{
			rmGame.getGameConfig().getDataFrom(rm.getRMConfig());
			rmp.sendMessage(RMText.getLabelArgs("game.create", ""+rmGame._config.getId()));
		}
		rmp.sendMessage(RMText.getLabelArgs("game.teams_count", ""+teams.size())+" ("+rmGame.getTextTeamColors()+")");
		
		//Correct min/max numbers
		rmGame.getGameConfig().correctMinMaxNumbers(Setting.minteamplayers);
		
		rmGame.updateSigns();
		
		if(wasModified) return HandleState.MODIFY;
		else return HandleState.ADD;
	}
	
	private static Boolean removeGame(RMGame rmGame){
		int id = rmGame.getGameConfig().getId();
		if(_games.containsKey(id)){
			RMGame.getGamesByOwnerName(rmGame.getGameConfig().getOwnerName()).remove(rmGame);
			for(RMTeam rmt : rmGame.getTeams()){
				rmt.setNull();
			}
			_games.remove(id);
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
			rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
			return HandleState.NO_CHANGE;
		}
		RMGame rmGame = getGameByBlock(b);
		if(rmGame!=null){
			if(rmGame.getGameConfig().getState() == GameState.SETUP){
				if(rmp.hasOwnerPermission(rmGame._config.getOwnerName())){
					if((RMHelper.isMaterial(b.getType(), Material.CHEST, Material.WALL_SIGN, Material.WOOL))&&(!justRemove)){
						List<Block> blocks = rmGame._config.getPartList().getList();
						for(Block block : blocks){
							if(rmGame.getMainBlock().equals(block)){
								HandleState handleState = tryAddGame(rmGame.getMainBlock(), rmp, b);
								switch(handleState){
									case NONE:
										rmp.sendMessage(RMText.getLabelArgs("game.remove", ""+rmGame._config.getId()));
										for(Sign sign : rmGame.getSigns()){
											sign.setLine(0, "");
											sign.setLine(1, "");
											sign.setLine(2, "");
											sign.setLine(3, "");
											sign.update();
										}
									default: return handleState;
								}
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
					rmp.sendMessage(RMText.getLabelArgs("game.remove", ""+rmGame._config.getId()));
					removeGame(rmGame);
					return HandleState.REMOVE;
				}
				else{
					rmp.sendMessage(RMText.getLabelArgs("game.owner", rmGame._config.getOwnerName()));
					return HandleState.NO_CHANGE;
				}
			}
			else rmp.sendMessage(RMText.getLabelArgs("game.remove.game_in_progress", ""+rmGame._config.getId()));
			return HandleState.NO_CHANGE;
		}
		return HandleState.NONE;
	}
	
	public static Material[] getMaterials(){
		return _materials;
	}
	
	//Get Game by Id (String)
	public static RMGame getGameById(String arg){
		int id = RMHelper.getIntByString(arg);
		if(id!=-1) return RMGame.getGame(id);
		return null;
	}
	
	public static RMGame getGame(int id){
		if((id>=0)&&(id<_games.size())) return _games.get(id);
		else return null;
	}
	public static HashMap<Integer, RMGame> getGames(){
		return _games;
	}
	public static Integer[] getAdvertisedGames(){
		List<Integer> advertised = new ArrayList<Integer>();
		for(RMGame game : _games.values()){
			if(game.getGameConfig().getSettingBool(Setting.advertise)){
				advertised.add(game.getGameConfig().getId());
			}
		}
		return advertised.toArray(new Integer[advertised.size()]);
	}
	public static RMGame getGameByBlock(Block b){
		for(RMGame game : _games.values()){
			for(Block block : game._config.getPartList().getList()){
				if(block.equals(b)){
					return game;
				}
				
			}
		}
		return null;
	}
	private static List<RMGame> getGamesByOwner(RMPlayer rmp){
		List<RMGame> games = new ArrayList<RMGame>();
		for(RMGame game : _games.values()){
			if(rmp.hasOwnerPermission(game._config.getOwnerName())) games.add(game); 
		}
		return games;
	}
	private static List<RMGame> getGamesByOwnerName(String name){
		List<RMGame> games = new ArrayList<RMGame>();
		for(RMGame game : RMGame.getGames().values()){
			if(game._config.getOwnerName().equalsIgnoreCase(name)) games.add(game); 
		}
		return games;
	}
	
	//Get Game by Id
	public static RMGame getGameById(int arg){
		return RMGame.getGame(arg);
	}
	
	////////
	//Team//
	////////
	
	//Get Team
	public static RMTeam getTeam(RMGame rmGame, int index){
		if(rmGame._config.getTeams().size()>index) return rmGame._config.getTeams().get(index);
		return null;
	}
	
	//Get Teams
	public static List<RMTeam> getTeams(RMGame rmGame){
		return rmGame._config.getTeams();
	}
	
	//Get All Teams
	public static List<RMTeam> getAllTeams(){
		List<RMTeam> teams = new ArrayList<RMTeam>();
		for(RMGame game : _games.values()){
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
	
	/////////
	//Chest//
	/////////
	
	//Get Chests from Block List
	public static List<RMChest> getChestsFromBlockList(List<List<Block>> bList, RM plugin){
		List<List<Block>> blockList = bList.subList(2, bList.size());
		List<RMChest> chestList = new ArrayList<RMChest>();
		for(List<Block> blocks : blockList){
			chestList.add(new RMChest((Chest)blocks.get(Part.CHEST.ordinal()-2).getState()));
		}
		if(chestList.size()>0) return chestList;
		else return null;
	}
}