package com.mtaye.ResourceMadness;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public enum RMDir{
	NORTH,EAST,SOUTH,WEST,UP,DOWN,NONE;
	
	private RMDir(){
	}

	public static RMDir getDirByData(int data){
		switch(data){
		case 5:	case 13: return RMDir.NORTH;
		case 3:	case 11: return RMDir.EAST;
		case 4:	case 12: return RMDir.SOUTH;
		case 2:	case 10: return RMDir.WEST;
		case 1: case 9:	return RMDir.UP;
		case 0:	case 8:	return RMDir.DOWN;
		}
		return RMDir.NONE;
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
	
	public RMDir getOpposite(){
		switch(this){
		case NORTH: return RMDir.SOUTH;
		case EAST: return RMDir.WEST;
		case SOUTH: return RMDir.NORTH;
		case WEST: return RMDir.EAST;
		case UP: return RMDir.DOWN;
		case DOWN: return RMDir.UP;
		}
		return RMDir.NONE;
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

