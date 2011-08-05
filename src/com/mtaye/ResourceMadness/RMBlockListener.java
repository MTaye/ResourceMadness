package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

//import net.minecraft.server.Item;

//import com.mtaye.ResourceMadness.RMBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.nijiko.data.UserStorage;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMBlockListener extends BlockListener{
	
	private final RM plugin;
	
	public RMBlockListener(RM piston){
		
		this.plugin = piston;
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent e){
		if(RMGame.getGames().size()!=0){
			Block b = e.getBlock();
			if(RMGame.isMaterial(b.getType(), RMGame.getMaterials())){
				Player p = e.getPlayer();
				RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
				switch(RMGame.tryRemoveGame(b, rmp, false)){
					case NOCHANGE:
						e.setCancelled(true);
						break;
				}
			}
			for(RMGame rmGame : RMGame.getGames()){
				if(rmGame!=null){
					switch(rmGame.getState()){
					case SETUP:
						break;
					case COUNTDOWN: case GAMEPLAY: case GAMEOVER:
						rmGame.addLog(b.getState());
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent e){
		Block b = e.getBlock();
		if(RMGame.getGames().size()!=0){
			for(RMGame rmGame : RMGame.getGames()){
				if(rmGame!=null){
					switch(rmGame.getState()){
					case SETUP:
						break;
					case COUNTDOWN: case GAMEPLAY: case GAMEOVER:
						e.getBlockReplacedState().getBlock();
						rmGame.addLog(e.getBlockReplacedState());
						break;
					}
				}
			}
		}
	}
}