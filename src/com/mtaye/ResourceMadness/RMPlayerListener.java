package com.mtaye.ResourceMadness;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;
import com.mtaye.ResourceMadness.Helper.RMHelper;
import com.mtaye.ResourceMadness.setting.Setting;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPlayerListener implements Listener{
	
	private final RM rm;
	public RMPlayerListener(RM plugin){
		rm = plugin;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(final PlayerInteractEvent e){
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(b.getType().isBlock()){
				RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
				if(rmp!=null){
					//if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
						RMGame rmGame = RMGame.getGameByBlock(b);
						if(rmGame!=null){
							switch(rmGame.getGameConfig().getState()){
								case SETUP:
									if(rmp.hasOwnerPermission(rmGame.getGameConfig().getOwnerName())){
										if(rmp.getPlayer().isSneaking()){
											if(b.getType()==Material.CHEST) e.setCancelled(true);
										}
										rmGame.handleRightClick(b, rmp);
									}
									else if(b.getType()==Material.CHEST) e.setCancelled(true);
									break;
								case COUNTDOWN: case PAUSED:
									 if(b.getType()==Material.CHEST) e.setCancelled(true);
									break;
								case GAMEPLAY: case GAMEOVER:
									RMTeam rmTeam = rmGame.getTeamByPlayer(rmp);
									if(rmTeam!=null){
										if(!b.equals(rmTeam.getChest().getChest().getBlock())){
											if(b.getType()==Material.CHEST) e.setCancelled(true);
										}
									}
									else if(b.getType()==Material.CHEST) e.setCancelled(true);
									break;
							}
							rmGame.updateSigns();
						}
					//}
				}
			}
		}
		if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
			if(b.getType().isBlock()){
				RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
				if(rmp!=null){
					PlayerAction action = rmp.getPlayerAction();
					if(action != PlayerAction.NONE){
						e.setCancelled(true);
						if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
							switch(action){
							case ADD: RMGame.tryAddGame(b, rmp, null); break;
							case REMOVE: RMGame.tryRemoveGame(b, rmp, true); break;
							default:
								RMGame rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									switch(action){
									case INFO:
										rmGame.sendInfo(rmp);
										break;
									case SETTINGS:
										rmGame.sendSettings(rmp, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SETTINGS_RESET:
										rmGame.resetSettings(rmp);
										break;
									case INFO_FOUND:
										rmGame.getInfoFound(rmp);
										break;
									case MODE:
										rmGame.changeMode(rmp.getRequestInterface(), rmp);
										break;
									case MODE_CYCLE:
										rmGame.cycleMode(rmp);
										break;
									case JOIN:
										rmGame.joinTeamByBlock(b, rmp);
										if(rmp.getPlayerAction()==PlayerAction.JOIN_PASSWORD) return;
										break;
									case JOIN_PASSWORD:
										rmp.sendMessage(RMText.getLabel("common.canceled"));
										break;
									case QUIT:
										rmGame.quitTeamByBlock(b, rmp);
										break;
									case START:
										rmGame.startGame(rmp);
										break;
									case START_RANDOM:
										rmGame.startGameRandomize(rmp, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
										/*
									case RESTART:
										rmGame.restartGame(rmp);
										break;
										*/
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
									case MONEY:
										//rmGame.parseMoney(rmp, rmp.getRequestForceState(), rmp.getRequestInt());
										break;
									case MONEY_INFO:
										//rmGame.moneyInfo();
										break;
									case TEMPLATE_SAVE:
										rmGame.saveTemplate(rmp.getRequestString(), rmp);
										rmp.clearRequestString();
										break;
									case TEMPLATE_LOAD:
										rmGame.loadTemplate(rmp.loadTemplate(rmp.getRequestString()), rmp);
										rmp.clearRequestString();
										break;
									case RESTORE:
										rmGame.restoreWorld(rmp);
										break;
									case CLAIM_FOUND:
										rmGame.claimFound(rmp, rmp.getRequestItems());
										rmp.clearRequestItems();
										break;
									case CLAIM_FOUND_CHEST_SELECT:
										rmp.setRequestInt(rmGame.getGameConfig().getId());
										rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST);
										rmp.sendMessage(RMText.getLabel("action.claim.found.chest.final"));
										return;
									case CLAIM_ITEMS_CHEST:
										rmp.sendMessage(RMText.getLabel("claim.chest.not_allowed"));
										break;
									case CLAIM_REWARD_CHEST:
										rmp.sendMessage(RMText.getLabel("claim.chest.not_allowed"));
										break;
									case CLAIM_TOOLS_CHEST:
										rmp.sendMessage(RMText.getLabel("claim.chest.not_allowed"));
										break;
									case KICK_PLAYER:
										rmGame.kickPlayer(rmp, true, rmp.getRequestStringList());
										rmp.clearRequestStringList();
										break;
									case KICK_TEAM:
										rmGame.kickTeam(rmp, true, rmp.getRequestStringList());
										rmp.clearRequestStringList();
										break;
									case KICK_ALL:
										rmGame.kickAll(rmp, true);
										break;
									case BAN_PLAYER:
										rmGame.banPlayer(rmp, true, rmp.getRequestStringList());
										rmp.clearRequestStringList();
										break;
									case BAN_TEAM:
										rmGame.banTeam(rmp, true, rmp.getRequestStringList());
										rmp.clearRequestStringList();
										break;
									case BAN_ALL:
										rmGame.banAll(rmp, true);
										break;
									case BAN_LIST:
										rmGame.sendBanList(rmp, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case UNBAN_PLAYER:
										rmGame.unbanPlayer(rmp, true, rmp.getRequestStringList());
										rmp.clearRequestStringList();
										break;
									case SET_MIN_PLAYERS:
										rmGame.setSetting(rmp, Setting.minplayers, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_MAX_PLAYERS:
										rmGame.setSetting(rmp, Setting.maxplayers, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_MIN_TEAM_PLAYERS:
										rmGame.setSetting(rmp, Setting.minteamplayers, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_MAX_TEAM_PLAYERS:
										rmGame.setSetting(rmp, Setting.maxteamplayers, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_SAFE_ZONE:
										rmGame.setSetting(rmp, Setting.safezone, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_TIME_LIMIT:
										rmGame.setSetting(rmp, Setting.timelimit, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_RANDOM:
										rmGame.setSetting(rmp, Setting.random, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_PASSWORD:
										rmGame.setSetting(rmp, Setting.password, rmp.getRequestString());
										rmp.clearRequestString();
										break;
									case SET_ADVERTISE:
										rmGame.setSetting(rmp, Setting.advertise, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_RESTORE:
										rmGame.setSetting(rmp, Setting.restore, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_WARP:
										rmGame.setSetting(rmp, Setting.warp, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_MIDGAME_JOIN:
										rmGame.setSetting(rmp, Setting.midgamejoin, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_HEAL_PLAYER:
										rmGame.setSetting(rmp, Setting.healplayer, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_CLEAR_INVENTORY:
										rmGame.setSetting(rmp, Setting.clearinventory, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_FOUND_AS_REWARD:
										rmGame.setSetting(rmp, Setting.foundasreward, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_WARN_UNEQUAL:
										rmGame.setSetting(rmp, Setting.warnunequal, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_ALLOW_UNEQUAL:
										rmGame.setSetting(rmp, Setting.allowunequal, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_WARN_HACKED:
										rmGame.setSetting(rmp, Setting.warnhacked, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_ALLOW_HACKED:
										rmGame.setSetting(rmp, Setting.allowhacked, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_INFINITE_REWARD:
										rmGame.setSetting(rmp, Setting.infinitereward, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									case SET_INFINITE_TOOLS:
										rmGame.setSetting(rmp, Setting.infinitetools, rmp.getRequestInt());
										rmp.clearRequestInt();
										break;
									default:
										rmp.sendMessage(RMText.getLabel("action.not_a_game_block"));
										rmp.setPlayerAction(PlayerAction.NONE);
										break;
									}
									rmGame.updateSigns();
								}
								else{
									switch(action){
									case CLAIM_FOUND_CHEST:
										RMGame game = RMGame.getGame(rmp.getRequestInt());
										if(game!=null){
											game.claimFoundToChest(b, rmp, rmp.getRequestItems());
											rmp.clearRequestItems();
										}
										rmp.clearRequestInt();
										break;
									case CLAIM_ITEMS_CHEST:
										rmp.claimItemsToChest(b, null, rmp.getRequestItems());
										rmp.clearRequestItems();
										break;
									case CLAIM_REWARD_CHEST:
										rmp.claimRewardToChest(b, null, rmp.getRequestItems());
										rmp.clearRequestItems();
										break;
									case CLAIM_TOOLS_CHEST:
										rmp.claimToolsToChest(b, null, rmp.getRequestItems());
										rmp.clearRequestItems();
										break;
									default:
										rmp.sendMessage(RMText.getLabel("action.not_a_game_block"));
										rmp.setPlayerAction(PlayerAction.NONE);
									}
								}
								break;
							}
							rmp.setPlayerAction(PlayerAction.NONE);
						}
						else{
							rmp.sendMessage(RMText.getLabel("action.not_a_game_block"));
							rmp.setPlayerAction(PlayerAction.NONE);
						}
					}
					else{
						RMGame rmGame = RMGame.getGameByBlock(b);
						if(rmGame!=null){
							e.setCancelled(true);
							if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
								if(!rmp.hasPermission("resourcemadness")){
									rmp.sendMessage(RMText.getLabel("msg.no_permission_action"));
									return;
								}
								rmGame.handleLeftClick(b, rmp);
							}
							//rmGame.updateSigns();
						}
					}
				}
				else{
					p.sendMessage(RMText.getLabel("action.not_an_user"));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(final PlayerJoinEvent e){
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
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerRespawn(final PlayerRespawnEvent e){
		Player p = e.getPlayer();
		RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			if(rmp.isIngame()){
				e.setRespawnLocation(rmp.getTeam().getWarpLocation());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChat(final PlayerChatEvent e){
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
			else if(rmp.getPlayerAction()==PlayerAction.JOIN_PASSWORD){
				e.setCancelled(true);
				rmp.setPlayerAction(PlayerAction.NONE);
				RMGame game = RMGame.getGame(rmp.getRequestInt());
				if(game!=null){
					if(game.getGameConfig().getSettingStr(Setting.password).equalsIgnoreCase(e.getMessage())){
						RMTeam team = game.getTeamByDye(rmp.getRequestString());
						if(team!=null){
							rmp.sendMessage(RMText.getLabel("join.password.success"));
							rmp.setRequestString(e.getMessage());
							team.addPlayer(rmp);
							rmp.clearRequestString();
							rmp.clearRequestInt();
							return;
						}
					}
					rmp.clearRequestString();
					rmp.clearRequestInt();
					rmp.sendMessage(RMText.getLabel("join.password.fail"));
				}
			}
		}
	}
}