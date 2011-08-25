package com.mtaye.ResourceMadness;

//import java.util.HashMap;

//import net.minecraft.server.Item;

//import com.mtaye.ResourceMadness.RMBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

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
				if(RMGame.isMaterial(b.getType(), RMGame.getMaterials())){
					switch(RMGame.tryRemoveGame(b, rmp, false)){
						case NO_CHANGE:
							e.setCancelled(true);
							break;
					}
				}
				if(rmp.isIngame()){
					RMGame rmGame = rmp.getTeam().getGame();
					if(rmGame!=null){
						switch(rmGame.getConfig().getState()){
							case SETUP:
								break;
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
}