package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.time.Timer;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class EntityListener implements Listener{
	
	//private final RM rm;
	public EntityListener(RM plugin){
		//rm = plugin;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplode(final EntityExplodeEvent e){
		if(Game.getGames().size()!=0){
			List<Block> blockList = e.blockList();
			List<Game> affectedGames = new ArrayList<Game>();
			for(Game game : Game.getGames().values()){
				if(affectedGames.contains(game)) continue;
				GameConfig config = game.getGameConfig();
				List<Block> gameBlocks = config.getPartList().getList();
				for(Block b : blockList){
					if(gameBlocks.contains(b)){
						affectedGames.add(game);
						break;
					}
				}
			}
			if(affectedGames.size()!=0){
				e.setCancelled(true);
				for(Game game : affectedGames){
					GameConfig config = game.getGameConfig();
					game.broadcastMessage(Text.getLabelArgs("game.canceled_explosion", config.getName(), ""+config.getId()));
				}
			}
			Entity ent = e.getEntity();
			if((ent instanceof Creature)||(ent instanceof Explosive)){
				Debug.warning("Entity found");
				World w = ent.getWorld();
				for(Game rmGame : Game.getGames().values()){
					if(rmGame.getGameConfig().getWorld()==w){
						Debug.warning("Same world found");						
						if(rmGame.inRangeXZ(ent, rmGame.getGameConfig().getSettingInt(Setting.playarea))){
							rmGame.addLog(blockList);
							return;
						}
						for(GamePlayer rmp : rmGame.getOnlineTeamPlayers()){
							Debug.warning("rmp: "+rmp.getName());
							if(rmp.inRange(ent.getLocation(), 32)){
								rmGame.addLog(blockList);
								return;
							}
							else Debug.warning("not near location");
						}
					}
				}
			}
		}
	}
	
	/*
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEndermanPickup(final EndermanPickupEvent e){
		if(RMGame.getGames().size()!=0){
			Block b = e.getBlock();
			for(RMGame game : RMGame.getGames().values()){
				RMGameConfig config = game.getGameConfig();
				List<Block> gameBlocks = config.getPartList().getList();
				if(gameBlocks.contains(b)){
					e.setCancelled(true);
					String message = ChatColor.RED+"Prevented enderman from picking up a block at game id "+ChatColor.YELLOW+config.getId()+ChatColor.RED+"!";
					game.broadcastMessage(message);
				}
			}
		}
	}
	*/
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent e){
		Entity ent = e.getEntity();
		Entity entDamager = e.getDamager();
		if((ent instanceof Player)&&(entDamager instanceof Player)){
			GamePlayer rmp = GamePlayer.getPlayerByName(((Player)ent).getName());
			GamePlayer rmpDamager = GamePlayer.getPlayerByName(((Player)entDamager).getName());
			if((rmp!=null)&&(rmpDamager!=null)){
				if((rmp.isIngame())&&(rmpDamager.isIngame())){
					GameConfig config = rmp.getGame().getGameConfig();
					Timer pvpTimer = config.getPvpTimer();
					if(!config.getSettingBool(Setting.allowpvp)){
						e.setCancelled(true);
					}
					else if(rmp.getTeam()==rmpDamager.getTeam()){
						if(!config.getSettingBool(Setting.friendlyfire)){
							e.setCancelled(true);
						}
					}
					else if((rmp.isSafe())||(rmpDamager.isSafe())){
						e.setCancelled(true);
						rmpDamager.sendMessage(Text.getLabel("game.safezone.pvp"));
					}
					else if((pvpTimer.isSet())&&(pvpTimer.isTicking())){
						e.setCancelled(true);
						//rmpDamager.sendMessage(RMText.getLabelArgs("game.pvp.delay", pvpTimer.getTextTimeRemaining()));
						rmpDamager.sendMessage(Text.getLabelArgs("game.pvp.disabled"));
					}
				}
			}
		}
	}
}