package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Creature;

import com.mtaye.ResourceMadness.helper.Helper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class Log {
	RM rm;
	
	private List<Block> _checkList = new ArrayList<Block>();
 	private List<Location> _locList = new ArrayList<Location>();
	private List<LogBlock> _logList = new ArrayList<LogBlock>();
	private List<LogBlock> _logItemList = new ArrayList<LogBlock>();
	private List<GameCreature> _logCreatureList = new ArrayList<GameCreature>();
	private static Material[] _blockItemMaterials = {Material.BED_BLOCK, Material.BROWN_MUSHROOM, Material.CACTUS, Material.CROPS, Material.DEAD_BUSH,
		Material.DETECTOR_RAIL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.IRON_DOOR_BLOCK, Material.LEVER, Material.LONG_GRASS,
		Material.POWERED_RAIL, Material.RAILS, Material.RED_MUSHROOM, Material.RED_ROSE, Material.REDSTONE, Material.REDSTONE_WIRE, Material.SAPLING,
		Material.SIGN_POST, Material.SNOW, Material.STONE_PLATE, Material.SUGAR_CANE_BLOCK, Material.TORCH, Material.WOODEN_DOOR, Material.WOOD_PLATE,
		Material.YELLOW_FLOWER, Material.LADDER, Material.PAINTING, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.TRAP_DOOR,
		Material.WALL_SIGN, Material.WEB, Material.BREWING_STAND, Material.CAKE_BLOCK, Material.CAULDRON, Material.MELON_BLOCK, Material.PUMPKIN_STEM,
		Material.STONE_BUTTON, Material.VINE, Material.WATER_LILY};
	
	public Log(){
	}
	
	public void add(Creature... creatures){
		for(Creature creature : creatures){
			add(creature);
		}
	}
	
	private void add(Creature creature){
		if(creature==null) return;
		_logCreatureList.add(new GameCreature(creature));
	}
	
	//LOG WORLD
	public void add(Block... blocks){
		for(Block block : blocks){
			add(block.getState());
		}
	}
	
	public void add(BlockState... bStates){
		for(BlockState bState : bStates){
			add(bState);
		}
	}
	
	private void add(BlockState bState){
		Block b = bState.getBlock();
		Material mat = bState.getType();
		Location loc = bState.getBlock().getLocation();
		if(!_locList.contains(loc)){
			if(Helper.isMaterial(mat, _blockItemMaterials)){
				_locList.add(loc);
				_logItemList.add(new LogBlock(bState));
			}
			else{
				_locList.add(loc);
				_logList.add(new LogBlock(bState));
			}
		}
		check(b);
		_checkList.clear();
		/*
		if(_logBatchSize!=0){
			checkBatchSize();
		}
		*/
	}
	
	//Check Log
	public void check(Block b){
		if((b==null)||(_checkList.contains(b))) return;
		_checkList.add(b);
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
				if(!_locList.contains(loc)){
					if(Helper.isMaterial(mat, _blockItemMaterials)){
							_locList.add(loc);
							_logItemList.add(new LogBlock(block));
					}
					else{
						_locList.add(loc);
						_logList.add(new LogBlock(block));
					}
				}
				if(mat==Material.LEAVES) checkAround(block);
			}
			else{
				if(Helper.isMaterial(mat, Material.CACTUS, Material.CROPS, Material.SUGAR_CANE_BLOCK, Material.GRAVEL, Material.SAND)){
					if(!_locList.contains(loc)){
						if(Helper.isMaterial(mat, _blockItemMaterials)){
							_locList.add(loc);
							_logItemList.add(new LogBlock(block));
						}
						else{
							_locList.add(loc);
							_logList.add(new LogBlock(block));
						}
					}
					check(block);
				}
				else{
					if(!_locList.contains(loc)){
						if(Helper.isMaterial(mat, _blockItemMaterials)){
							_locList.add(loc);
							_logItemList.add(new LogBlock(block));
						}
						else{
							_locList.add(loc);
							_logList.add(new LogBlock(block));
						}
					}
				}
			}
		}
	}
	
	public void checkAround(Block b){
		if((b==null)||(_checkList.contains(b))) return;
		_checkList.add(b);
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
			if(!_locList.contains(loc)){
				if(Helper.isMaterial(mat, _blockItemMaterials)){
						_locList.add(loc);
						_logItemList.add(new LogBlock(block));
				}
				else{
					_locList.add(loc);
					_logList.add(new LogBlock(block));
				}
			}
			if(mat==Material.LEAVES) checkAround(block);
		}
	}
	
	public List<LogBlock> getList(){
		return _logList;
	}
	public List<LogBlock> getItemList(){
		return _logItemList;
	}
	public List<GameCreature> getCreatureList(){
		return _logCreatureList;
	}
	
	public void setList(List<LogBlock> logList){
		_logList = logList;
	}
	public void setItemList (List<LogBlock> logItemList){
		_logItemList = logItemList;
	}
	public void setCreatureList (List<GameCreature> logCreatureList){
		_logCreatureList = logCreatureList;
	}
	
	public void resetLocList(){
		_locList.clear();
		for(LogBlock rmBlock : _logList){
			Location loc = rmBlock.getLocation();
			if(!_locList.contains(loc)) _locList.add(loc);
		}
		for(LogBlock rmBlock : _logItemList){
			Location loc = rmBlock.getLocation();
			if(!_locList.contains(loc)) _locList.add(loc);
		}
	}
	
	//Clear Log
	public void clear(){
		_locList.clear();
		_logList.clear();
		_logItemList.clear();
		_logCreatureList.clear();
	}
	
	//Restore Log
	public boolean restore(){
		if(_locList.size()==0) return false;
		if(_logList.size()>0){
			for(LogBlock rmBlock : _logList){
				rmBlock.restore();
			}
			_logList.clear();
		}
		if(_logItemList.size()>0){
			for(LogBlock rmBlock : _logItemList){
				rmBlock.restore();
			}
			_logItemList.clear();
		}
		_locList.clear();
		return true;
	}
}