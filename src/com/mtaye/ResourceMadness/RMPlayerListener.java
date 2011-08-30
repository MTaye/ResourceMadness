package com.mtaye.ResourceMadness;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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
									if((rmp.getName().equalsIgnoreCase(rmGame.getConfig().getOwnerName()))||(rmp.hasOwnerPermission("resourcemadness.admin"))){
										if(rmp.getPlayer().isSneaking()){
											e.setCancelled(true);
											rmGame.handleRightClick(b, rmp);
										}
									}
									else{
										e.setCancelled(true);
									}
									break;
								case COUNTDOWN:
									e.setCancelled(true);
									break;
								case GAMEPLAY: case GAMEOVER:
									RMTeam rmTeam = rmGame.getPlayerTeam(rmp);
									if(rmTeam!=null){
										if(b!=rmTeam.getChest().getChest().getBlock()){
											e.setCancelled(true);
										}
									}
									else e.setCancelled(true);
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
							case INFO_SETTINGS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.sendSettings(rmp);
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
							case FILTER:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.tryParseFilter(rmp);
								break;
							case REWARD:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.tryParseFilter(rmp);
								break;
							case TOOLS:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.tryParseFilter(rmp);
								break;
							case RESTORE:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.restoreWorld(rmp);
								break;
							case CLAIM_FOUND:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.claimFound(rmp);
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
							case SET_KEEP_INGAME:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setKeepIngame(rmp, rmp.getRequestInt());
								break;
							case SET_MIDGAME_JOIN:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.setAllowMidgameJoin(rmp, rmp.getRequestInt());
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
								rmp.sendMessage("This is not a game block");
								rmp.setPlayerAction(PlayerAction.NONE);
								break;
							}
							rmp.setPlayerAction(PlayerAction.NONE);
						}
						else{
							rmp.sendMessage("This is not a game block");
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
					p.sendMessage("Not an User");
				}
			}
		}
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent e){
		/*
		Player p = e.getPlayer();
		RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			rmp.onPlayerJoin();
		}
		*/
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent e){
		Player p = e.getPlayer();
		RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			rmp.onPlayerQuit();
		}
	}
	
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