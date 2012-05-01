package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMEntityListener implements Listener{
	
	private final RM rm;
	public RMEntityListener(RM plugin){
		rm = plugin;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplode(final EntityExplodeEvent e){
		if(RMGame.getGames().size()!=0){
			List<Block> blockList = e.blockList();
			List<RMGame> affected = new ArrayList<RMGame>();
			for(RMGame game : RMGame.getGames().values()){
				if(affected.contains(game)) continue;
				RMGameConfig config = game.getGameConfig();
				List<Block> gameBlocks = config.getPartList().getList();
				for(Block b : blockList){
					if(gameBlocks.contains(b)){
						affected.add(game);
						break;
					}
				}
			}
			if(affected.size()!=0){
				e.setCancelled(true);
				for(RMGame game : affected){
					RMGameConfig config = game.getGameConfig();
					String message = ChatColor.RED+"Canceled an explosion near the game id "+ChatColor.YELLOW+config.getId()+ChatColor.RED+"!";
					game.broadcastMessage(message);
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
			RMPlayer rmp = RMPlayer.getPlayerByName(((Player)ent).getName());
			RMPlayer rmpDamager = RMPlayer.getPlayerByName(((Player)entDamager).getName());
			if((rmp!=null)&&(rmpDamager!=null)){
				if((rmp.isIngame())&&(rmpDamager.isIngame())){
					if((rmp.isSafe())||(rmpDamager.isSafe())){
						e.setCancelled(true);
						rmpDamager.sendMessage(RMText.getLabel("game.safe_zone"));
					}
				}
			}
		}
	}
	
	/*
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(final EntityDamageEvent e){
		Entity ent = e.getEntity();
		if(ent instanceof Player){
			switch(e.getCause()){
			case ENTITY_ATTACK: case PROJECTILE:
				Player p = (Player)ent;
				RMDebug.warning("Heal player "+p.getName());
				p.setHealth(20);
			}
		}
	}
	*/
}