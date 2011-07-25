package com.mtaye.ResourceMadness;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mtaye.ResourceMadness.RMGame.RMState;
import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;

public class RMPlayerListener extends PlayerListener{
	
	private final RM plugin;
	
	public RMPlayerListener(RM plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent e){
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(b.getType().isBlock()){
				RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
				if(rmp!=null){
					if(b.getType() == Material.CHEST){
						RMGame rmGame = RMGame.getGameByBlock(b);
						if(rmGame!=null){
							switch(rmGame.getState()){
								case SETUP: case COUNTDOWN: case GAMEOVER:
									if(rmp.getName()!=rmGame.getOwnerName()){
										e.setCancelled(true);
									}
									break;
								case GAMEPLAY:
									RMTeam rmTeam = rmGame.getPlayerTeam(rmp);
									if(rmTeam!=null){
										if(b!=rmTeam.getChest().getChest().getBlock()){
											e.setCancelled(true);
										}
									}
									break;
							}
						}
					}
				}
			}
		}
		if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
			if(b.getType().isBlock()){
				RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
				if(rmp!=null){
					if(rmp.getPlayerAction() != PlayerAction.NONE){
						if(RMGame.isMaterial(b.getType(), RMGame.getMaterials())){
							RMGame rmGame;
							switch(rmp.getPlayerAction()){
							case ADD:
								RMGame.tryAddGame(b, rmp, null);
								break;
							case REMOVE:
								if(!RMGame.tryRemoveGame(b, rmp, true)) rmp.sendMessage("You can't remove this game.");
								break;
							case JOIN:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.joinTeamByBlock(b, rmp);
								break;
							case QUIT:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null) rmGame.quitTeamByBlock(b, rmp);
								break;
							case START:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									rmGame.startGame(rmp);
								}
								break;
							case RESTART:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									rmGame.restartGame(rmp);
								}
								break;
							case STOP:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									rmGame.stopGame(rmp);
								}
								break;
							case FILTER:
								rmGame = RMGame.getGameByBlock(b);
								if(rmGame!=null){
									rmGame.tryParseFilter(rmp);
								}
								break;
							}
							rmp.setPlayerAction(PlayerAction.NONE);
						}
						else rmp.sendMessage("This is not a game block");
					}
					else{
						RMGame rmGame = RMGame.getGameByBlock(b);
						if(rmGame!=null){
							switch(b.getType()){
								case CHEST:
									rmGame.tryAddItems(b, rmp);
									break;
								case WALL_SIGN:
									rmGame.trySignInfo(b, rmp);
									break;
								case WOOL:
									rmGame.joinTeamByBlock(b, rmp);
									break;
							}
						}
					}
				}
				else{
					p.sendMessage("Not an User");
				}
			}
		}
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		/*
		Player p = e.getPlayer();
		if(!plugin.users.containsKey(p))
		{
			plugin.users.put(p, true);
			plugin.players.add(p);
		}
		*/
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		/*
		Player p = e.getPlayer();
		
		if(plugin.users.containsKey(p))
		{
			plugin.users.remove(p);
			plugin.players.remove(p);
		}
		*/
	}
}
