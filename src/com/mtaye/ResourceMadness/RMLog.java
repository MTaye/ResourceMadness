package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMLog {
	RM plugin;
	
	HashMap<Location, RMBlock> _logList = new HashMap<Location, RMBlock>();
	HashMap<Location, RMBlock> _logItemList = new HashMap<Location, RMBlock>();
	public static Material[] _blockItemMaterials = {Material.BED_BLOCK, Material.BROWN_MUSHROOM, Material.CACTUS, Material.CROPS, Material.DEAD_BUSH,
		Material.DETECTOR_RAIL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.IRON_DOOR_BLOCK, Material.LEVER, Material.LONG_GRASS,
		Material.POWERED_RAIL, Material.RAILS, Material.RED_MUSHROOM, Material.RED_ROSE, Material.REDSTONE, Material.REDSTONE_WIRE, Material.SAPLING,
		Material.SIGN_POST, Material.SNOW, Material.STONE_PLATE, Material.SUGAR_CANE_BLOCK, Material.TORCH, Material.WOODEN_DOOR, Material.WOOD_PLATE,
		Material.YELLOW_FLOWER, Material.LADDER, Material.PAINTING, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.TRAP_DOOR,
		Material.WALL_SIGN, Material.WEB};
	
	public RMLog(RM plugin){
		this.plugin = plugin;
	}
	
	//LOG WORLD
	public void add(BlockState bState){
		Block b = bState.getBlock();
		Material mat = bState.getType();
		if(RMGame.isMaterial(mat, _blockItemMaterials)){
			if(!_logItemList.containsKey(bState.getBlock().getLocation())) _logItemList.put(b.getLocation(), new RMBlock(bState));
		}
		else if(!_logList.containsKey(bState.getBlock().getLocation())) _logList.put(b.getLocation(), new RMBlock(bState));
		check(b);
		/*
		if(_logBatchSize!=0){
			checkBatchSize();
		}
		*/
	}
	
	//Check Log
	public void check(Block b){
		if(b!=null){
			List<Block> blocks = new ArrayList<Block>();
			blocks.add(b.getRelative(BlockFace.NORTH));
			blocks.add(b.getRelative(BlockFace.EAST));
			blocks.add(b.getRelative(BlockFace.SOUTH));
			blocks.add(b.getRelative(BlockFace.WEST));
			blocks.add(b.getRelative(BlockFace.DOWN));
			blocks.add(b.getRelative(BlockFace.UP));
			for(int i=0; i<blocks.size(); i++){
				Block block = blocks.get(i);
				Location loc = block.getLocation();
				Material mat = block.getType();
				if(i<5){ //NORTH,EAST,SOUTH,WEST
					//if(isMaterial(mat, Material.TORCH, Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF, Material.LEVER, Material.LADDER, Material.PAINTING, Material.STONE_BUTTON, Material.WALL_SIGN)){
					if(RMGame.isMaterial(mat, _blockItemMaterials)){
						if(!_logItemList.containsKey(loc)) _logItemList.put(loc, new RMBlock(block));
					}
					else if(!_logList.containsKey(loc)) _logList.put(loc, new RMBlock(block));
					//}
				}
				else{
					if(RMGame.isMaterial(mat, Material.CACTUS, Material.CROPS, Material.SUGAR_CANE_BLOCK, Material.GRAVEL, Material.SAND)){
						if(RMGame.isMaterial(mat, _blockItemMaterials)){
							if(!_logItemList.containsKey(loc)) _logItemList.put(loc, new RMBlock(block));
						}
						else if(!_logList.containsKey(loc)) _logList.put(loc, new RMBlock(block));
						check(block);
					}
					else{
						if(RMGame.isMaterial(mat, _blockItemMaterials)){
							if(!_logItemList.containsKey(loc)) _logItemList.put(loc, new RMBlock(block));
						}
						else if(!_logList.containsKey(loc)) _logList.put(loc, new RMBlock(block));
					}
				}
			}
		}
	}
	
	public HashMap<Location, RMBlock> getList(){
		return _logList;
	}
	public HashMap<Location, RMBlock> getItemList(){
		return _logItemList;
	}
	
	public void setList(HashMap<Location, RMBlock> logList){
		_logList = logList;
	}
	public void setItemList (HashMap<Location, RMBlock> logItemList){
		_logItemList = logItemList;
	}
	
	//Clear Log
	public void clear(){
		_logList.clear();
		_logItemList.clear();
	}
	
	//Restore Log
	public boolean restore(){
		if((_logList.size()==0)&&(_logItemList.size()==0)) return false;
		if(_logList.size()>0){
			for(RMBlock rmBlock : _logList.values()){
				rmBlock.restore();
			}
			_logList.clear();
		}
		if(_logItemList.size()>0){
			for(RMBlock rmBlock : _logItemList.values()){
				rmBlock.restore();
			}
			_logItemList.clear();
		}
		return true;
	}
}