package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMEntityListener extends EntityListener{
	
	private final RM plugin;
	public RMEntityListener(RM plugin){
		this.plugin = plugin;
	}
	
	@Override
	public void onEntityExplode(EntityExplodeEvent e){
		List<Block> blockList = e.blockList();
		if(RMGame.getGames().size()!=0){
			List<RMGame> affected = new ArrayList<RMGame>();
			for(RMGame game : RMGame.getGames().values()){
				RMGameConfig config = game.getConfig();
				if(affected.contains(game)) continue;
				List<Block> gameBlocks = config.getPartList().getList();
				Iterator<Block> iter = blockList.iterator();
				while(iter.hasNext()){
					Block b = iter.next();
					if(gameBlocks.contains(b)){
						affected.add(game);
						break;
					}
				}
			}
			if(affected.size()!=0){
				e.setCancelled(true);
				for(RMGame game : affected){
					RMGameConfig config = game.getConfig();
					String message = ChatColor.RED+"Canceled an explosion near the game id "+ChatColor.YELLOW+config.getId()+ChatColor.RED+"!";
					game.broadcastMessage(message);
				}
			}
		}
	}
}