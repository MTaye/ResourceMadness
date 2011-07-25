package com.mtaye.ResourceMadness;

import java.util.List;

import org.bukkit.block.Block;

public class BlockList{
	
	List<Block[]> _blockList;
	
	public BlockList(){
	}
	
	public void BlockList(List<Block[]> blockList){
		_blockList = blockList;
	}
	
	public List<Block[]> BlockList(){
		return _blockList;
	}
}
