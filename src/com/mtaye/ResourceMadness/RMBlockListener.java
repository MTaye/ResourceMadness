package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

//import net.minecraft.server.Item;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.nijiko.data.UserStorage;

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
				if(!RMGame.tryRemoveGame(b, rmp, false)){
					e.setCancelled(true);
				}
				/*
				for(RMGame rmGame : RMGame.getGames()){
					List<Block> blocks = rmGame.getSimpleBlockList();
					for(Block block : blocks){
						if(block == b){
							if(rmGame.getOwnerName() != p.getName()){
								p.sendMessage("This is a game id "+rmGame.getId()+" block.");
								p.sendMessage("Only the owner "+rmGame.getOwnerName()+" can destroy it.");
								e.setCancelled(true);
							}
							else RMGame.tryRemoveGame(b, rmGame.getOwner());
						}
					}
				}
				*/
			}
			
		}
	}
}