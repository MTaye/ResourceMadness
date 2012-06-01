package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.mtaye.ResourceMadness.GamePlayer.PlayerAction;
import com.mtaye.ResourceMadness.Stats.RMStat;
import com.mtaye.ResourceMadness.helper.Helper;
import com.mtaye.ResourceMadness.setting.Setting;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class PlayerListener implements Listener{
	
	private final RM rm;
	public PlayerListener(RM plugin){
		rm = plugin;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(final PlayerInteractEvent e){
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(b.getType().isBlock()){
				GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
				if(rmp!=null){
					//if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
						Game rmGame = Game.getGameByBlock(b);
						if(rmGame!=null){
							switch(rmGame.getGameConfig().getState()){
								case SETUP:
									if(rmp.hasOwnerPermission(rmGame.getGameConfig().getOwnerName())){
										if(rmp.isSneaking()){
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
									if(rmp.isIngame()){
										if(!b.equals(rmp.getTeam().getChest().getChest().getBlock())){
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
				GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
				if(rmp!=null){
					PlayerAction action = rmp.getPlayerAction();
					if(action != PlayerAction.NONE){
						e.setCancelled(true);
						if(Helper.isMaterial(b.getType(), Game.getMaterials())){
							switch(action){
							case CREATE: Game.tryCreateGame(rmp.getRequestString(), b, rmp); break;
							case REMOVE: Game.tryRemoveGame(b, rmp); break;
							default:
								Game rmGame = Game.getGameByBlock(b);
								if(rmGame!=null){
									switch(action){
									case INFO:
										rmGame.sendInfo(rmp);
										break;
									case INFO_FOUND:
										rmGame.getInfoFound(rmp);
										break;
									case INFO_FILTER:
										rmGame.sendFilterInfo(rmp);
										break;
									case INFO_FILTER_STRING:
										rmGame.sendFilterInfoString(rmp);
										break;
									case INFO_REWARD:
										rmGame.sendRewardInfo(rmp);
										break;
									case INFO_REWARD_STRING:
										rmGame.sendRewardInfoString(rmp);
										break;
									case INFO_TOOLS:
										rmGame.sendToolsInfo(rmp);
										break;
									case INFO_TOOLS_STRING:
										rmGame.sendToolsInfoString(rmp);
										break;
									case SETTINGS:
										rmGame.sendSettings(rmp, rmp.getRequestInt());
										break;
									case SETTINGS_RESET:
										rmGame.resetSettings(rmp);
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
										rmp.sendMessage(Text.getLabel("common.canceled"));
										break;
									case QUIT:
										rmGame.quitTeamByBlock(b, rmp);
										break;
									case START:
										rmGame.startGame(rmp);
										break;
									case START_RANDOM:
										rmGame.startGame(rmp, rmp.getRequestInt());
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
									case REWARD:
										rmGame.tryParseFilter(b, rmp);
										break;
									case TOOLS:
										rmGame.tryParseFilter(b, rmp);
										break;
									case MONEY:
										//rmGame.parseMoney(rmp, rmp.getRequestForceState(), rmp.getRequestInt());
										break;
									case MONEY_INFO:
										//rmGame.moneyInfo();
										break;
									case TEMPLATE_SAVE:
										rmGame.saveTemplate(rmp.getRequestString(), rmp);
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
										rmp.setRequestInt(rmGame.getGameConfig().getId());
										rmp.setPlayerAction(PlayerAction.CLAIM_FOUND_CHEST);
										rmp.sendMessage(Text.getLabel("action.claim.found.chest.final"));
										return;
									case CLAIM_ITEMS_CHEST:
										rmp.sendMessage(Text.getLabel("claim.chest.not_allowed"));
										break;
									case CLAIM_REWARD_CHEST:
										rmp.sendMessage(Text.getLabel("claim.chest.not_allowed"));
										break;
									case CLAIM_TOOLS_CHEST:
										rmp.sendMessage(Text.getLabel("claim.chest.not_allowed"));
										break;
									case CLAIM_INFO_FOUND:
										rmGame.getInfoFound(rmp);
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
										break;
									case UNBAN_PLAYER:
										rmGame.unbanPlayer(rmp, true, rmp.getRequestStringList());
										rmp.clearRequestStringList();
										break;
									case SET:
										Setting setting = rmp.getRequestSetting();
										switch(setting){
										case multiplier:
											rmGame.setSetting(rmp, setting, rmp.getRequestIntegerRange());
											rmp.clearRequestIntegerRange();
											break;
										case password:
											rmGame.setSetting(rmp, setting, rmp.getRequestString());
											break;
										default:
											rmGame.setSetting(rmp, setting, rmp.getRequestInt());
											break;
										}
										rmp.clearRequestSetting();
										break;
									case UNDO:
										rmGame.undo(rmp);
										break;
									default:
										rmp.sendMessage(Text.getLabel("action.not_a_game_block"));
										rmp.setPlayerAction(PlayerAction.NONE);
										break;
									}
									rmGame.updateSigns();
								}
								else{
									switch(action){
									case CLAIM_FOUND_CHEST:
										Game game = Game.getGame(rmp.getRequestString());
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
										rmp.sendMessage(Text.getLabel("action.not_a_game_block"));
										rmp.setPlayerAction(PlayerAction.NONE);
									}
								}
								break;
							}
							rmp.setPlayerAction(PlayerAction.NONE);
						}
						else{
							rmp.sendMessage(Text.getLabel("action.not_a_game_block"));
							rmp.setPlayerAction(PlayerAction.NONE);
						}
					}
					else{
						Game rmGame = Game.getGameByBlock(b);
						if(rmGame!=null){
							e.setCancelled(true);
							if(Helper.isMaterial(b.getType(), Game.getMaterials())){
								if(!rmp.hasPermission("resourcemadness")){
									rmp.sendMessage(Text.getLabel("msg.no_permission_action"));
									return;
								}
								rmGame.handleLeftClick(b, rmp);
							}
							//rmGame.updateSigns();
						}
					}
				}
				else{
					p.sendMessage(Text.getLabel("action.not_an_user"));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(final PlayerJoinEvent e){
		Player p = e.getPlayer();
		GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			rm.getServer().getScheduler().scheduleSyncDelayedTask(rm, new WatcherPlayerJoin(rmp), 10);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e){
		GamePlayer rmp = GamePlayer.getPlayerByName(e.getPlayer().getName());
		if(rmp!=null){
			rmp.onPlayerQuit();
			if(rmp.isIngame()){
				rmp.clearDetectedEnemy();
				rmp.updateProperties();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(final PlayerRespawnEvent e){
		Player p = e.getPlayer();
		final GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
		Debug.warning("RESPAWN1");
		if(rmp!=null){
			if(rmp.isIngame()){
				Debug.warning("RESPAWN2");
				rmp.clearDetectedEnemy();
				e.setRespawnLocation(rmp.getTeam().getWarpLocation());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChat(final PlayerChatEvent e){
		Player p = e.getPlayer();
		GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
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
				String[] args = rmp.getRequestStringArray();
				if((args==null)||(args.length!=2)) return;
				Game game = Game.getGame(args[0]);
				if(game!=null){
					if(game.getGameConfig().getSettingStr(Setting.password).equalsIgnoreCase(e.getMessage())){
						Team team = game.getTeamByDye(args[1]);
						if(team!=null){
							rmp.sendMessage(Text.getLabel("join.password.success"));
							rmp.setRequestString(e.getMessage());
							team.addPlayer(rmp);
							rmp.clearRequestString();
							rmp.clearRequestInt();
							return;
						}
					}
					rmp.clearRequestStringArray();
					rmp.sendMessage(Text.getLabel("join.password.fail"));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(final PlayerDeathEvent e){
		Player p = (Player)e.getEntity();
		GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
		if(rmp!=null){
			if(rmp.isIngame()){
				PlayerInventory inv = p.getInventory();
				inv.clear();
				List<ItemStack> drops = e.getDrops();
				Stash stash = new Stash();
				double percentage = ((double)(100-rmp.getGame().getGameConfig().getSettingInt(Setting.keepondeath)))/100;
				int size = (int)Math.round(drops.size()*percentage);
				while(drops.size()>size){
					int random = (int)Math.round(Math.random()*(double)(drops.size()-1));
					ItemStack item = drops.get(random);
					stash.addItem(item);
					drops.remove(random);
				}
				rmp.getTools().addItems(stash.getItems());
				rmp.addStat(RMStat.DEATHS);
				//rmp.sendMessage("Stat.DEATHS: "+rmp.getStats()._lastInt+"+1="+rmp.getStats().get(RMStat.DEATHS));
				
				Entity ent = rmp.getPlayer().getKiller();
				if(ent!=null){
					GamePlayer rmpKiller = GamePlayer.getPlayerByName(((Player)rmp.getPlayer().getKiller()).getName());
					if(rmpKiller!=null){
						rmpKiller.addStat(RMStat.KILLS);
						//rmpKiller.sendMessage("Stat.KILLS: "+rmpKiller.getStats()._lastInt+"+1="+rmpKiller.getStats().get(RMStat.KILLS));
					}
				}
			}
		}
	}
}