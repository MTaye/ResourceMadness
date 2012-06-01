package com.mtaye.ResourceMadness;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public enum Dir{
	NORTH,EAST,SOUTH,WEST,UP,DOWN,NONE;
	
	private Dir(){
	}

	public static Dir getDirByData(int data){
		switch(data){
		case 5:	case 13: return Dir.NORTH;
		case 3:	case 11: return Dir.EAST;
		case 4:	case 12: return Dir.SOUTH;
		case 2:	case 10: return Dir.WEST;
		case 1: case 9:	return Dir.UP;
		case 0:	case 8:	return Dir.DOWN;
		}
		return Dir.NONE;
	}
	
	public static BlockFace getBlockFaceByData(int data){
		switch(data){
		case 5:	case 13: return BlockFace.NORTH;
		case 3:	case 11: return BlockFace.EAST;
		case 4:	case 12: return BlockFace.SOUTH;
		case 2:	case 10: return BlockFace.WEST;
		case 1: case 9:	return BlockFace.UP;
		case 0:	case 8:	return BlockFace.DOWN;
		}
		return BlockFace.SELF;
	}
	
	public byte getData(){
		switch(this){
			case NORTH: return 5;
			case EAST: return 3;
			case SOUTH: return 4;
			case WEST: return 2;
			case UP: return 1;
			case DOWN: return 0;
		}
		return 0;
	}
	
	public static BlockFace getBlockFaceOpposite(BlockFace face){
		switch(face){
		case NORTH: return BlockFace.SOUTH;
		case EAST: return BlockFace.WEST;
		case SOUTH: return BlockFace.NORTH;
		case WEST: return BlockFace.EAST;
		case UP: return BlockFace.DOWN;
		case DOWN: return BlockFace.UP;
		}
		return BlockFace.SELF;
	}
	
	public Dir getOpposite(){
		switch(this){
		case NORTH: return Dir.SOUTH;
		case EAST: return Dir.WEST;
		case SOUTH: return Dir.NORTH;
		case WEST: return Dir.EAST;
		case UP: return Dir.DOWN;
		case DOWN: return Dir.UP;
		}
		return Dir.NONE;
	}
	
	public Vector getVector(){
		switch(this){
		case NORTH: return new Vector(1, 0, 0);
		case EAST: return new Vector(0, 0, 1);
		case SOUTH: return new Vector(-1, 0, 0);
		case WEST: return new Vector(0, 0, -1);
		case UP: return new Vector(0, 1, 0);
		case DOWN: return new Vector(0, -1, 0);
		}
		return new Vector(0,0,0);
	}
}