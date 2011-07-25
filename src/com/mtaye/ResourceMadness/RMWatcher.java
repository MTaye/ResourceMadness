package com.mtaye.ResourceMadness;

import java.util.Date;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import org.bukkit.entity.Item;

public class RMWatcher implements Runnable {
	private RM plugin;
	
	public RMWatcher(RM plugin){
		this.plugin = plugin;
	}

	@Override
	public void run() {
		try{
			for(RMGame rmGame : RMGame.getGames()){
				rmGame.update();
			}
		} catch(Exception e){
			System.out.println("Warning! An error occured!");
			e.printStackTrace();
		}
	}
}
