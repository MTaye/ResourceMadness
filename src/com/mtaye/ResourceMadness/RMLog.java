package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import com.mtaye.ResourceMadness.Helper.RMHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMLog {
	RM plugin;
	
	private List<Location> _locList = new ArrayList<Location>();
	private List<RMBlock> _logList = new ArrayList<RMBlock>();
	private List<RMBlock> _logItemList = new ArrayList<RMBlock>();
	private static Material[] _blockItemMaterials = {Material.BED_BLOCK, Material.BROWN_MUSHROOM, Material.CACTUS, Material.CROPS, Material.DEAD_BUSH,
		Material.DETECTOR_RAIL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.IRON_DOOR_BLOCK, Material.LEVER, Material.LONG_GRASS,
		Material.POWERED_RAIL, Material.RAILS, Material.RED_MUSHROOM, Material.RED_ROSE, Material.REDSTONE, Material.REDSTONE_WIRE, Material.SAPLING,
		Material.SIGN_POST, Material.SNOW, Material.STONE_PLATE, Material.SUGAR_CANE_BLOCK, Material.TORCH, Material.WOODEN_DOOR, Material.WOOD_PLATE,
		Material.YELLOW_FLOWER, Material.LADDER, Material.PAINTING, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.TRAP_DOOR,
		Material.WALL_SIGN, Material.WEB, Material.BREWING_STAND, Material.CAKE_BLOCK, Material.CAULDRON, Material.MELON_BLOCK, Material.PUMPKIN_STEM,
		Material.STONE_BUTTON, Material.VINE, Material.WATER_LILY};
	
	public RMLog(){
	}
	
	//LOG WORLD
	public void add(BlockState bState){
		Block b = bState.getBlock();
		Material mat = bState.getType();
		Location loc = bState.getBlock().getLocation();
		if(!_locList.contains(loc)){
			if(RMHelper.isMaterial(mat, _blockItemMaterials)){
				_locList.add(loc);
				_logItemList.add(new RMBlock(bState));
			}
			else{
				_locList.add(loc);
				_logList.add(new RMBlock(bState));
			}
		}
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
					if(!_locList.contains(loc)){
						if(RMHelper.isMaterial(mat, _blockItemMaterials)){
								_locList.add(loc);
								_logItemList.add(new RMBlock(block));
						}
						else{
							_locList.add(loc);
							_logList.add(new RMBlock(block));
						}
					}
					//}
				}
				else{
					if(RMHelper.isMaterial(mat, Material.CACTUS, Material.CROPS, Material.SUGAR_CANE_BLOCK, Material.GRAVEL, Material.SAND)){
						if(!_locList.contains(loc)){
							if(RMHelper.isMaterial(mat, _blockItemMaterials)){
								_locList.add(loc);
								_logItemList.add(new RMBlock(block));
							}
							else{
								_locList.add(loc);
								_logList.add(new RMBlock(block));
							}
						}
						check(block);
					}
					else{
						if(!_locList.contains(loc)){
							if(RMHelper.isMaterial(mat, _blockItemMaterials)){
								_locList.add(loc);
								_logItemList.add(new RMBlock(block));
							}
							else{
								_locList.add(loc);
								_logList.add(new RMBlock(block));
							}
						}
					}
				}
			}
		}
	}
	
	public List<RMBlock> getList(){
		return _logList;
	}
	public List<RMBlock> getItemList(){
		return _logItemList;
	}
	
	public void setList(List<RMBlock> logList){
		_logList = logList;
	}
	public void setItemList (List<RMBlock> logItemList){
		_logItemList = logItemList;
	}
	
	public void resetLocList(){
		_locList.clear();
		for(RMBlock rmBlock : _logList){
			Location loc = rmBlock.getLocation();
			if(!_locList.contains(loc)) _locList.add(loc);
		}
		for(RMBlock rmBlock : _logItemList){
			Location loc = rmBlock.getLocation();
			if(!_locList.contains(loc)) _locList.add(loc);
		}
	}
	
	//Clear Log
	public void clear(){
		_locList.clear();
		_logList.clear();
		_logItemList.clear();
	}
	
	//Restore Log
	public boolean restore(){
		if(_locList.size()==0) return false;
		if(_logList.size()>0){
			for(RMBlock rmBlock : _logList){
				rmBlock.restore();
			}
			_logList.clear();
		}
		if(_logItemList.size()>0){
			for(RMBlock rmBlock : _logItemList){
				rmBlock.restore();
			}
			_logItemList.clear();
		}
		_locList.clear();
		return true;
	}
}