package com.mtaye.ResourceMadness;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.mtaye.ResourceMadness.RM.ClaimType;
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
											e.setCancelled(true);
											rmGame.handleRightClick(b, rmp);
										}
									}
									else if(b.getType()==Material.CHEST) e.setCancelled(true);
									break;
								case COUNTDOWN: case PAUSED:
									e.setCancelled(true);
									break;
								case GAMEPLAY: case GAMEOVER:
									RMTeam rmTeam = rmGame.getPlayerTeam(rmp);
									if(rmTeam!=null){
										if(b!=rmTeam.getChest().getChest().getBlock()){
											e.setCancelled(true);
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
					if(rmp.getPlayerAction() != PlayerAction.NONE){
						if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
							RMGame rmGame;
							switch(rmp.getPlayerAction()){
							case ADD:
								rmGame = RMGame.getGameByBlock(b);
								//if(rmGame!=null) //RMGame.tryAddGame(rmGame, rmp, b);
								//else 
								RMGame.tryAddGame(b, rmp, null);
								break;
							case REMOVE:
								RMGame.tryRemoveGame(b, rmp, true);
								break;
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
								/*
							case SAVE_TEMPLATE:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.saveTemplate();
								break;
								*/
							case JOIN:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.joinTeamByBlock(b, rmp);
								break;
							case QUIT:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.quitTeamByBlock(b, rmp);
								break;
							case START:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.startGame(rmp);
								break;
							case START_RANDOM:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									rmGame.setRandomizeAmount(rmp, rmp.getRequestInt());
									rmGame.startGame(rmp);
								}
								break;
							case RESTART:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.restartGame(rmp);
								break;
							case STOP:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.stopGame(rmp, true);
								break;
							case PAUSE:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.pauseGame(rmp);
								break;
							case RESUME:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.resumeGame(rmp);
								break;
							case FILTER:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.tryParseFilter(b, rmp);
								break;
							case REWARD:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.tryParseFilter(b, rmp);
								break;
							case TOOLS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.tryParseFilter(b, rmp);
								break;
							case RESTORE:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.restoreWorld(rmp);
								break;
							case CLAIM_FOUND:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									rmGame.claimFound(rmp, rmp.getRequestItems());
									rmp.clearRequestItems();
								}
								break;
							case CLAIM_FOUND_CHEST_SELECT:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									rmp.setRequestInt(rmGame.getConfig().getId());
									rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST);
									rmp.sendMessage("Now left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store items"+ChatColor.WHITE+".");
									return;
								}
								break;
							case CLAIM_FOUND_CHEST:
								rmGame = RMGame.getGame(rmp.getRequestInt());
								if(rmGame!=null){
									rmGame.claimFoundToChest(b, rmp, rmp.getRequestItems());
									rmp.clearRequestItems();
								}
								break;
							case CLAIM_ITEMS_CHEST:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmp.sendMessage(ChatColor.GRAY+"You are not allowed to claim items into game chests.");
								else{
									rmp.claimItemsToChest(b, false, rmp.getRequestItems());
									rmp.clearRequestItems();
								}
								break;
							case CLAIM_REWARD_CHEST:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmp.sendMessage(ChatColor.GRAY+"You are not allowed to claim items into game chests.");
								else{
									rmp.claimRewardToChest(b, false, rmp.getRequestItems());
									rmp.clearRequestItems();
								}
								break;
							case CLAIM_TOOLS_CHEST:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmp.sendMessage(ChatColor.GRAY+"You are not allowed to claim items into game chests.");
								else{
									rmp.claimToolsToChest(b, false, rmp.getRequestItems());
									rmp.clearRequestItems();
								}
								break;
							case SET_MIN_PLAYERS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setMinPlayers(rmp, rmp.getRequestInt());
								break;
							case SET_MAX_PLAYERS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setMaxPlayers(rmp, rmp.getRequestInt());
								break;
							case SET_MIN_TEAM_PLAYERS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setMinTeamPlayers(rmp, rmp.getRequestInt());
								break;
							case SET_MAX_TEAM_PLAYERS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setMaxTeamPlayers(rmp, rmp.getRequestInt());
								break;
							case SET_MAX_ITEMS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setMaxItems(rmp, rmp.getRequestInt());
								break;
							case SET_TIME_LIMIT:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setTimeLimit(rmp, rmp.getRequestInt());
								break;
							case SET_RANDOM:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setAutoRandomizeAmount(rmp, rmp.getRequestInt());
								break;
							case SET_WARP:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setWarpToSafety(rmp, rmp.getRequestInt());
								break;
							case SET_RESTORE:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setAutoRestoreWorld(rmp, rmp.getRequestInt());
								break;
							case SET_WARN_HACKED:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setWarnHackedItems(rmp, rmp.getRequestInt());
								break;
							case SET_ALLOW_HACKED:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setAllowHackedItems(rmp, rmp.getRequestInt());
								break;
							case SET_MIDGAME_JOIN:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setAllowMidgameJoin(rmp, rmp.getRequestInt());
								break;
							case SET_HEAL_PLAYER:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setHealPlayer(rmp, rmp.getRequestInt());
								break;
							case SET_CLEAR_INVENTORY:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setClearPlayerInventory(rmp, rmp.getRequestInt());
								break;
							case SET_WARN_UNEQUAL:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setWarnUnequal(rmp, rmp.getRequestInt());
								break;
							case SET_ALLOW_UNEQUAL:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setAllowUnequal(rmp, rmp.getRequestInt());
								break;
							case SET_INFINITE_REWARD:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setInfiniteReward(rmp, rmp.getRequestInt());
								break;
							case SET_INFINITE_TOOLS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setInfiniteTools(rmp, rmp.getRequestInt());
								break;
							default:
								rmp.sendMessage(ChatColor.GRAY+"This is not a game block");
								rmp.setPlayerAction(PlayerAction.NONE);
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
									rmp.sendMessage(RMText.noPermissionAction);
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
	
	/*
	@Override
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			rmp.onPlayerJoin();
		}
	}
	*/
	
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
}