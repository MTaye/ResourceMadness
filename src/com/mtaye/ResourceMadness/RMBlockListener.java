package com.mtaye.ResourceMadness;

//import java.util.HashMap;

//import net.minecraft.server.Item;

//import com.mtaye.ResourceMadness.RMBlock;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.mtaye.ResourceMadness.Helper.RMHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMBlockListener extends BlockListener{
	
	private final RM plugin;
	public RMBlockListener(RM plugin){
		this.plugin = plugin;
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent e){
		if(RMGame.getGames().size()!=0){
			Block b = e.getBlock();
			Player p = e.getPlayer();
			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(RMHelper.isMaterial(b.getType(), RMGame.getMaterials())){
					RMDebug.log(Level.SEVERE, "MATERIAL");
					switch(RMGame.tryRemoveGame(b, rmp, false)){
						case NO_CHANGE:
							RMDebug.log(Level.SEVERE, "Should cancel");
							e.setCancelled(true);
							break;
					}
				}
				else RMDebug.log(Level.SEVERE, "WHY NOT MATERIAL");
				if(rmp.isIngame()){
					RMGame rmGame = rmp.getTeam().getGame();
					if(rmGame!=null){
						switch(rmGame.getConfig().getState()){
							case COUNTDOWN: case GAMEPLAY: case GAMEOVER:
								if(plugin.config.getUseRestore()) rmGame.addLog(b.getState());
								break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent e){
		if(RMGame.getGames().size()!=0){
			Player p = e.getPlayer();
			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
			if(rmp!=null){
				if(rmp.isIngame()){
					RMGame rmGame = rmp.getTeam().getGame();
					if(rmGame!=null){
						switch(rmGame.getConfig().getState()){
							case SETUP:
								break;
							case COUNTDOWN: case GAMEPLAY: case GAMEOVER:
								if(plugin.config.getUseRestore()) rmGame.addLog(e.getBlockReplacedState());
								break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onBlockBurn(BlockBurnEvent e){
		Block b = e.getBlock();
		if(b.getType() == Material.WOOL){
			for(RMGame game : RMGame.getGames().values()){
				List<Block> woolBlocks = game.getConfig().getPartList().getWoolList();
				if(woolBlocks.contains(b)){
					RMDebug.warning("Burn B TYPE:"+b.getType().name());
					e.setCancelled(true);
				}
			}
		}
	}
	
	@Override
	public void onBlockPistonExtend(BlockPistonExtendEvent e){
		List<Block> blocks = e.getBlocks();
		for(RMGame game : RMGame.getGames().values()){
			List<Block> gameBlocks = game.getConfig().getPartList().getList();
			for(Block block : blocks){
				if(gameBlocks.contains(block)){
					RMDebug.warning("Piston Pushed Game Block:"+block.getType());
					e.setCancelled(true);
					break;
				}
			}
		}
	}
	
	@Override
	public void onBlockPistonRetract(BlockPistonRetractEvent e){
		if(e.isSticky()){
			Block b = e.getRetractLocation().getBlock();
			for(RMGame game : RMGame.getGames().values()){
				List<Block> gameBlocks = game.getConfig().getPartList().getList();
				if(gameBlocks.contains(b)){
					RMDebug.warning("Tryed to retract MAIN BLOCK:"+b.getType().name());
					e.setCancelled(true);
				}
			}
		}
	}
}