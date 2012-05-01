package com.mtaye.ResourceMadness;

//import java.util.HashMap;

//import net.minecraft.server.Item;

//import com.mtaye.ResourceMadness.RMBlock;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.mtaye.ResourceMadness.Helper.RMHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMBlockListener implements Listener{
	
	private final RM rm;
	public RMBlockListener(RM plugin){
		rm = plugin;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(final BlockBreakEvent e){
		if(RMGame.getGames().size()!=0){
			Block b = e.getBlock();
			Player p = e.getPlayer();
			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
					switch(RMGame.tryRemoveGame(b, rmp, false)){
						case NO_CHANGE:
							e.setCancelled(true);
							RMGame.getGameByBlock(b).updateSigns();
							break;
					}
				}
				if(rmp.isIngame()){
					if(rm.config.getUseRestore()) rmp.getGame().addLog(b.getState());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(final BlockPlaceEvent e){
		if(RMGame.getGames().size()!=0){
			Player p = e.getPlayer();
			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(rmp.isIngame()){
					if(rm.config.getUseRestore()) rmp.getGame().addLog(e.getBlockReplacedState());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBurn(final BlockBurnEvent e){
		Block b = e.getBlock();
		if(b.getType() == Material.WOOL){
			for(RMGame game : RMGame.getGames().values()){
				List<Block> woolBlocks = game.getGameConfig().getPartList().getWoolList();
				if(woolBlocks.contains(b)){
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonExtend(final BlockPistonExtendEvent e){
		List<Block> blocks = e.getBlocks();
		for(RMGame game : RMGame.getGames().values()){
			List<Block> gameBlocks = game.getGameConfig().getPartList().getList();
			for(Block block : blocks){
				if(gameBlocks.contains(block)){
					e.setCancelled(true);
					break;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonRetract(final BlockPistonRetractEvent e){
		if(e.isSticky()){
			Block b = e.getRetractLocation().getBlock();
			for(RMGame game : RMGame.getGames().values()){
				List<Block> gameBlocks = game.getGameConfig().getPartList().getList();
				if(gameBlocks.contains(b)){
					e.setCancelled(true);
				}
			}
		}
	}
}