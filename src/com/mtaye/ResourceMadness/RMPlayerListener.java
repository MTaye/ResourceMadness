package com.mtaye.ResourceMadness;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.mtaye.ResourceMadness.RMGame.Setting;
import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;
import com.mtaye.ResourceMadness.Helper.RMHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPlayerListener extends PlayerListener{
	
	private final RM plugin;
	public RMPlayerListener(RM plugin){
		this.plugin = plugin;
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent e){
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(b.getType().isBlock()){
				RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
				if(rmp!=null){
					if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
						RMGame rmGame = RMGame.getGameByBlock(b);
						if(rmGame!=null){
							switch(rmGame.getConfig().getState()){
								case SETUP:
									if(rmp.hasOwnerPermission(rmGame.getConfig().getOwnerName())){
										if(rmp.getPlayer().isSneaking()){
											if(b.getType()==Material.CHEST) e.setCancelled(true);
											rmGame.handleRightClick(b, rmp);
										}
									}
									else if(b.getType()==Material.CHEST) e.setCancelled(true);
									break;
								case COUNTDOWN: case PAUSED:
									 if(b.getType()==Material.CHEST) e.setCancelled(true);
									break;
								case GAMEPLAY: case GAMEOVER:
									RMTeam rmTeam = rmGame.getPlayerTeam(rmp);
									if(rmTeam!=null){
										if(b!=rmTeam.getChest().getChest().getBlock()){
											if(b.getType()==Material.CHEST) e.setCancelled(true);
										}
									}
									else if(b.getType()==Material.CHEST) e.setCancelled(true);
									break;
							}
						}
					}
				}
			}
		}
		if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
			if(b.getType().isBlock()){
				RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
				if(rmp!=null){
					PlayerAction action = rmp.getPlayerAction();
					if(action != PlayerAction.NONE){
						if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
							switch(action){
							case ADD: RMGame.tryAddGame(b, rmp, null); break;
							case REMOVE: RMGame.tryRemoveGame(b, rmp, true); break;
							default:
								RMGame rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									switch(action){
									case INFO:
										rmGame = RMGame.getGameByBlock(b);
										if(rmGame!=null) rmGame.sendInfo(rmp);
										break;
									case SETTINGS:
										rmGame = RMGame.getGameByBlock(b);
										if(rmGame!=null) rmGame.sendSettings(rmp, rmp.getRequestInt());
										break;
									case INFO_FOUND:
										rmGame = RMGame.getGameByBlock(b);
										if(rmGame!=null) rmGame.getInfoFound(rmp);
										break;
									case MODE:
										rmGame = RMGame.getGameByBlock(b);
										if(rmGame!=null) rmGame.changeMode(rmp.getRequestInterface(), rmp);
										break;
									case MODE_CYCLE:
										rmGame = RMGame.getGameByBlock(b);
										if(rmGame!=null) rmGame.cycleMode(rmp);
										break;
									case JOIN:
										rmGame.joinTeamByBlock(b, rmp);
										break;
									case QUIT:
										rmGame.quitTeamByBlock(b, rmp);
										break;
									case START:
										rmGame.startGame(rmp);
										break;
									case START_RANDOM:
										rmGame.setRandomizeAmount(rmp, rmp.getRequestInt());
										rmGame.startGame(rmp);
										break;
									case RESTART:
										rmGame.restartGame(rmp);
										break;
									case STOP:
										rmGame.stopGame(rmp, true);
										break;
									case PAUSE:
										rmGame.pauseGame(rmp);
										break;
									case RESUME:
										rmGame.resumeGame(rmp);
										break;
									case FILTER:
										rmGame.tryParseFilter(b, rmp);
										break;
									case FILTER_INFO:
										rmGame.sendFilterInfo(rmp);
										break;
									case FILTER_INFO_STRING:
										rmGame.sendFilterInfoString(rmp);
										break;
									case REWARD:
										rmGame.tryParseFilter(b, rmp);
										break;
									case REWARD_INFO:
										rmGame.sendRewardInfo(rmp);
										break;
									case REWARD_INFO_STRING:
										rmGame.sendRewardInfoString(rmp);
										break;
									case TOOLS:
										rmGame.tryParseFilter(b, rmp);
										break;
									case TOOLS_INFO:
										rmGame.sendToolsInfo(rmp);
										break;
									case TOOLS_INFO_STRING:
										rmGame.sendToolsInfoString(rmp);
										break;
									case TEMPLATE_SAVE:
										rmGame.saveTemplate(rmp.getRequestString(), rmp);
										rmp.clearRequestString();
										break;
									case TEMPLATE_LOAD:
										rmGame.loadTemplate(rmp.loadTemplate(rmp.getRequestString()), rmp);
										break;
									case RESTORE:
										rmGame.restoreWorld(rmp);
										break;
									case CLAIM_FOUND:
										rmGame.claimFound(rmp, rmp.getRequestItems());
										rmp.clearRequestItems();
										break;
									case CLAIM_FOUND_CHEST_SELECT:
										rmp.setRequestInt(rmGame.getConfig().getId());
										rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST);
										rmp.sendMessage("Now left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store items"+ChatColor.WHITE+".");
										return;
									case CLAIM_ITEMS_CHEST:
										rmp.sendMessage(ChatColor.GRAY+"You are not allowed to claim items into game chests.");
										break;
									case CLAIM_REWARD_CHEST:
										rmp.sendMessage(ChatColor.GRAY+"You are not allowed to claim items into game chests.");
										break;
									case CLAIM_TOOLS_CHEST:
										rmp.sendMessage(ChatColor.GRAY+"You are not allowed to claim items into game chests.");
										break;
									case SET_MIN_PLAYERS:
										rmGame.setSetting(rmp, Setting.minPlayers, rmp.getRequestInt());
										break;
									case SET_MAX_PLAYERS:
										rmGame.setSetting(rmp, Setting.maxPlayers, rmp.getRequestInt());
										break;
									case SET_MIN_TEAM_PLAYERS:
										rmGame.setSetting(rmp, Setting.minTeamPlayers, rmp.getRequestInt());
										break;
									case SET_MAX_TEAM_PLAYERS:
										rmGame.setSetting(rmp, Setting.maxTeamPlayers, rmp.getRequestInt());
										break;
									case SET_MAX_ITEMS:
										rmGame = RMGame.getGameByBlock(b);
										if(rmGame!=null) rmGame.setSetting(rmp, Setting.maxItems, rmp.getRequestInt());
										break;
									case SET_TIME_LIMIT:
										rmGame.setSetting(rmp, Setting.timeLimit, rmp.getRequestInt());
										break;
									case SET_RANDOM:
										rmGame.setSetting(rmp, Setting.autoRandomizeAmount, rmp.getRequestInt());
										break;
									case SET_ADVERTISE:
										rmGame.setSetting(rmp, Setting.advertise, rmp.getRequestInt());
										break;
									case SET_RESTORE:
										rmGame.setSetting(rmp, Setting.autoRestoreWorld, rmp.getRequestInt());
										break;
									case SET_WARP:
										rmGame.setSetting(rmp, Setting.warpToSafety, rmp.getRequestInt());
										break;
									case SET_MIDGAME_JOIN:
										rmGame.setSetting(rmp, Setting.allowMidgameJoin, rmp.getRequestInt());
										break;
									case SET_HEAL_PLAYER:
										rmGame.setSetting(rmp, Setting.healPlayer, rmp.getRequestInt());
										break;
									case SET_CLEAR_INVENTORY:
										rmGame.setSetting(rmp, Setting.clearPlayerInventory, rmp.getRequestInt());
										break;
									case SET_FOUND_AS_REWARD:
										rmGame.setSetting(rmp, Setting.foundAsReward, rmp.getRequestInt());
										break;
									case SET_WARN_UNEQUAL:
										rmGame.setSetting(rmp, Setting.warnUnequal, rmp.getRequestInt());
										break;
									case SET_ALLOW_UNEQUAL:
										rmGame.setSetting(rmp, Setting.allowUnequal, rmp.getRequestInt());
										break;
									case SET_WARN_HACKED:
										rmGame.setSetting(rmp, Setting.warnHackedItems, rmp.getRequestInt());
										break;
									case SET_ALLOW_HACKED:
										rmGame.setSetting(rmp, Setting.allowHackedItems, rmp.getRequestInt());
										break;
									case SET_INFINITE_REWARD:
										rmGame.setSetting(rmp, Setting.infiniteReward, rmp.getRequestInt());
										break;
									case SET_INFINITE_TOOLS:
										rmGame.setSetting(rmp, Setting.infiniteTools, rmp.getRequestInt());
										break;
									default:
										rmp.sendMessage(ChatColor.GRAY+"This is not a game block");
										rmp.setPlayerAction(PlayerAction.NONE);
										break;
									}
								}
								else{
									switch(action){
									case CLAIM_FOUND_CHEST:
										RMGame game = RMGame.getGame(rmp.getRequestInt());
										if(game!=null){
											game.claimFoundToChest(b, rmp, rmp.getRequestItems());
											rmp.clearRequestItems();
										}
										break;
									case CLAIM_ITEMS_CHEST:
										rmp.claimItemsToChest(b, false, rmp.getRequestItems());
										rmp.clearRequestItems();
										break;
									case CLAIM_REWARD_CHEST:
										rmp.claimRewardToChest(b, false, rmp.getRequestItems());
										rmp.clearRequestItems();
										break;
									case CLAIM_TOOLS_CHEST:
										rmp.claimToolsToChest(b, false, rmp.getRequestItems());
										rmp.clearRequestItems();
										break;
									default:
										rmp.sendMessage(ChatColor.GRAY+"This is not a game block");
										rmp.setPlayerAction(PlayerAction.NONE);
									}
								}
								break;
							}
							rmp.setPlayerAction(PlayerAction.NONE);
						}
						else{
							rmp.sendMessage(ChatColor.GRAY+"This is not a game block");
							rmp.setPlayerAction(PlayerAction.NONE);
						}
					}
					else{
						RMGame rmGame = RMGame.getGameByBlock(b);
						if(rmGame!=null){
							if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
								if(!rmp.hasPermission("resourcemadness")){
									rmp.sendMessage(RMText.eNoPermissionAction);
									return;
								}
								rmGame.handleLeftClick(b, rmp);
							}
						}
					}
				}
				else{
					p.sendMessage(ChatColor.GRAY+"Not an User");
				}
			}
		}
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			rmp.onPlayerJoin();
		}
	}
	
	/*
	@Override
	public void onPlayerQuit(PlayerQuitEvent e){
		Player p = e.getPlayer();
		RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			rmp.onPlayerQuit();
		}
	}
	*/
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent e){
		Player p = e.getPlayer();
		RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			if(rmp.isIngame()){
				e.setRespawnLocation(rmp.getTeam().getWarpLocation());
			}
		}
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent e){
		Player p = e.getPlayer();
		RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			if(rmp.isIngame()){
				String message = rmp.getChatMessage(rmp.getChatMode(), e.getMessage());
				if(message.length()!=0){
					e.setCancelled(true);
					rmp.chat(rmp.getChatMode(), message);
				}
			}
		}
	}
}