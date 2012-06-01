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

import com.mtaye.ResourceMadness.helper.Helper;
import com.mtaye.ResourceMadness.setting.Setting;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class BlockListener implements Listener{
	
	private final RM rm;
	public BlockListener(RM plugin){
		rm = plugin;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(final BlockBreakEvent e){
		if(Game.getGames().size()!=0){
			Block b = e.getBlock();
			Player p = e.getPlayer();
			GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(Helper.isMaterial(b.getType(), Game.getMaterials())){
					switch(Game.tryRemoveGame(b, rmp)){
						case NO_CHANGE:
							e.setCancelled(true);
							Game.getGameByBlock(b).updateSigns();
							break;
					}
				}
				if(rmp.isIngame()){
					if(rmp.getGame().getGameConfig().getSettingBool(Setting.restore)) rmp.getGame().addLog(b.getState());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(final BlockPlaceEvent e){
		if(Game.getGames().size()!=0){
			Player p = e.getPlayer();
			GamePlayer rmp = GamePlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(rmp.isIngame()){
					if(rmp.getGame().getGameConfig().getSettingBool(Setting.restore)) rmp.getGame().addLog(e.getBlockReplacedState());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBurn(final BlockBurnEvent e){
		Block b = e.getBlock();
		if(b.getType() == Material.WOOL){
			for(Game game : Game.getGames().values()){
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
		for(Game game : Game.getGames().values()){
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
			for(Game game : Game.getGames().values()){
				List<Block> gameBlocks = game.getGameConfig().getPartList().getList();
				if(gameBlocks.contains(b)){
					e.setCancelled(true);
				}
			}
		}
	}
}