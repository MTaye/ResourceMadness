package com.mtaye.ResourceMadness;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMBlock {
	private Block _block;
	private Material _type;
	private byte _data;
	
	public RMBlock(Block b){
		_block = b;
		_data = _block.getData();
		_type = _block.getType();
	}
	public RMBlock(BlockState bState){
		_block = bState.getBlock();
		_data = bState.getRawData();
		_type = bState.getType();
	}
	public RMBlock(Block b, Material type, byte data){
		_block = b;
		_data = data;
		_type = type;
	}
	public void restore(){
		_block.setType(_type);
		_block.setData(_data, false);
	}
	public Material getType(){
		return _type;
	}
	public byte getData(){
		return _data;
	}
	public Location getLocation(){
		return _block.getLocation();
	}
}