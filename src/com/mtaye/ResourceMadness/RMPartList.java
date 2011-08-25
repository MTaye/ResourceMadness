package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMPartList {
	private Block _mainBlock;
	private List<List<Block>> _blockList = new ArrayList<List<Block>>();
	public RM plugin;
	
	private enum Part { CHEST, WALL_SIGN, WOOL; }
	
	public RMPartList(){
	}
	
	public RMPartList(Block b, RM plugin){
		init(b, null, plugin);
	}
	public RMPartList(Block b, Block bRemove, RM plugin){
		init(b, bRemove, plugin);
	}
	
	public void init(Block b, Block bRemove, RM plugin){
		this.plugin = plugin;
		if(b.getType()!=Material.GLASS){
			b = getMainBlock(b);
		}
		List<List<Block>> blockList = createListByBlock(b);
		if(bRemove!=null)  blockList = removeFromBlockList(bRemove, blockList);
		blockList = getCompleteParts(blockList);
		_blockList = blockList;
		_mainBlock = _blockList.get(0).get(0);
	}
	
	public List<List<Block>> getBlockList(){
		return _blockList;
	}
	public List<Block> getList(){
		List<Block> list = new ArrayList<Block>();
		for(List<Block> bList : _blockList){
			list.addAll(bList);
		}
		return list; 
	}
	
	public int size(){
		return _blockList.size();
	}
	
	public List<List<Block>> getPartList(){
		return _blockList.subList(2, _blockList.size());
		
	}
	
	/*
	public static Part getPartByMaterial(Material mat){
		switch(mat){
			case GLASS:
				return Part.GLASS;
			case STONE:
				return Part.STONE;
			case CHEST:
				return Part.CHEST;
			case WALL_SIGN:
				return Part.WALL_SIGN;
			case WOOL:
				return Part.WOOL;
		}
		return null;
	}
	*/
	
	public Block getMainBlock(){
		return _mainBlock;
	}
	public List<Block> getStoneList(){
		return _blockList.get(1);
	}
	public List<Block> getChestList(){
		List<Block> list = new ArrayList<Block>();
		List<List<Block>> blockList = getPartList();
		for(List<Block> bList : blockList){
			list.add(bList.get(Part.CHEST.ordinal()));
		}
		return list;
	}
	public List<Block> getSignList(){
		List<Block> list = new ArrayList<Block>();
		List<List<Block>> blockList = getPartList();
		for(List<Block> bList : blockList){
			list.add(bList.get(Part.WALL_SIGN.ordinal()));
		}
		return list;
	}
	public List<Block> getWoolList(){
		List<Block> list = new ArrayList<Block>();
		List<List<Block>> blockList = getPartList();
		for(List<Block> bList : blockList){
			list.add(bList.get(Part.WOOL.ordinal()));
		}
		return list;
	}
	
	//Tech

	//Get Block List
	public List<List<Block>> createListByBlock(Block b){
		if(b!=null){
			List<List<Block>> blockList = new ArrayList<List<Block>>();
			blockList.add(fetchParts(b, Material.GLASS, true, b));
			blockList.add(fetchParts(b, Material.STONE, true, b.getRelative(BlockFace.UP), b.getRelative(BlockFace.DOWN)));
			blockList.add(fetchParts(b.getRelative(BlockFace.DOWN), Material.CHEST, false));
			blockList.add(fetchParts(b, Material.WALL_SIGN, false));
			blockList.add(fetchParts(b.getRelative(BlockFace.UP), Material.WOOL, false));
			return blockList;
		}
		return null;
	}
	//Get parts
	private List<Block> fetchParts(Block b, Material mat, Boolean trim, Block... faces){
		List<Block> blocks = new ArrayList<Block>();
		List<Block> facings = new ArrayList<Block>();
		if(faces.length==0){
			facings.add(b.getRelative(BlockFace.NORTH));
			facings.add(b.getRelative(BlockFace.EAST));
			facings.add(b.getRelative(BlockFace.SOUTH));
			facings.add(b.getRelative(BlockFace.WEST));
		}
		else{
			for(int i=0; i<faces.length; i++){
				facings.add(faces[i]);
			}
		}
		if(mat==Material.WALL_SIGN){
				for(int i=0; i<facings.size(); i++){
					if((facings.get(i).getType()==Material.WALL_SIGN)&&(RMDir.getDirByData(facings.get(i).getData()).getOpposite()==RMDir.values()[i])){
						blocks.add(facings.get(i));
					}
					else if(!trim){
						blocks.add(null);
					}
				}
		}
		else{
			for(int i=0; i<facings.size(); i++){
				if(facings.get(i).getType() == mat){
					blocks.add(facings.get(i));
				}
				else if(!trim){
					blocks.add(null);
				}
			}
		}
		return blocks;
	}
	//Get Complete Parts
	private List<List<Block>> getCompleteParts(List<List<Block>> blockList){
		List<List<Block>> parts = new ArrayList<List<Block>>(blockList.subList(0, 2));
		blockList = blockList.subList(2, blockList.size());
		List<Byte> data = new ArrayList<Byte>();
		for(int i=0; i<4; i++){
			List<Block> partCount = new ArrayList<Block>();
			for(int j=0; j<blockList.size(); j++){
				Block b = blockList.get(j).get(i); 
				if(b!=null){
					partCount.add(b);
				}
			}
			if(partCount.size()==3){
				List<Block> blocks = new ArrayList<Block>();
				for(int j=0; j<partCount.size(); j++){
					blocks.add(partCount.get(j));
				}
				Byte d = blocks.get(Part.WOOL.ordinal()).getData();
				if(!data.contains(d)){
					data.add(d);
					parts.add(blocks);
				}
			}
		}
		return parts;
	}
	
	//Get Center Block
	public Block getMainBlock(Block b){
		switch(b.getType()){
			case WALL_SIGN:
				RMDir dir = RMDir.getDirByData(b.getData());
				Block c = getSignBlockByDir(b, dir);
				if(c.getType()==Material.GLASS) return c;
				return null;
			case CHEST:
				b = b.getRelative(BlockFace.UP);
				return getMainBlockByFace(b);
			case WOOL:
				b = b.getRelative(BlockFace.DOWN);
				return getMainBlockByFace(b);
			case STONE:
				return getMainBlockByFace(b, b.getRelative(BlockFace.UP),b.getRelative(BlockFace.DOWN));
			}
		return null;
	}
	//Get Center Block By Face
	public Block getMainBlockByFace(Block b, Block... faces){
		List<Block> facings;
		facings = new ArrayList<Block>();
		if(faces.length==0){
		facings.add(b.getRelative(BlockFace.NORTH));
		facings.add(b.getRelative(BlockFace.EAST));
		facings.add(b.getRelative(BlockFace.SOUTH));
		facings.add(b.getRelative(BlockFace.WEST));
		}
		else{
			for(int i=0; i<faces.length; i++){
			facings.add(faces[i]);
			}
		}
		for(Block face : facings){
			if(face.getType()==Material.GLASS) return face;
		}
		return null;
	}
	//Get Sign Block
	public static Block getSignBlockByDir(Block b, RMDir dir){
		switch(dir){
			case NORTH:
				return b.getRelative(BlockFace.NORTH);
			case EAST:
				return b.getRelative(BlockFace.EAST);
			case SOUTH:
				return b.getRelative(BlockFace.SOUTH);
			case WEST:
				return b.getRelative(BlockFace.WEST);
		}
		return null;
	}
	
	//Remove from BlockList
	public void removeFromBlockList(Block b){
		for(List<Block> bList : _blockList){
			if(bList.contains(b)){
				bList.remove(b);
			}
		}
	}
	
	public List<List<Block>> removeFromBlockList(Block b, List<List<Block>> blockList){
		for(List<Block> bList : blockList){
			if(bList.contains(b)){
				bList.set(bList.indexOf(b), null);
			}
		}
		return blockList;
	}
	
	//Fetch Teams
	public List<RMTeam> fetchTeams(){
		List<RMTeam> teams = new ArrayList<RMTeam>();
		List<List<Block>> blockList = _blockList.subList(2, _blockList.size());
		String items = "";
		for(List<Block> blocks : blockList){
			for(Block block : blocks){
				if(block!=null) items += block.getType().name();
				else items += "null";
				items += ",";
			}
		}
		for(List<Block> blocks : blockList){
			//plugin.getServer().broadcastMessage(blocks.size()+"");//+","+Part.WOOL.ordinal());
			teams.add(new RMTeam(DyeColor.getByData(blocks.get(Part.WOOL.ordinal()).getData()), (Chest)blocks.get(Part.CHEST.ordinal()).getState(), plugin));
		}
		return teams;
	}
	
	public String getTextBlockList(boolean allowNull){
		String line = "";
		for(List<Block> bList : _blockList){
			for(Block b : bList){
				if(b!=null){
					line+=b.getType().name();
				}
				else if(allowNull) line+="null";
				line+=",";
			}
		}
		return plugin.stripLast(line, ",");
	}
	
	//Match Part List
	public Boolean matchPartList(RMPartList bl){
		//plugin.getServer().broadcastMessage(plugin.getTextList(bl1.getChestList(),true)+":"+plugin.getTextList(bl2.getChestList(),true));
		//plugin.getServer().broadcastMessage(plugin.getTextList(bl1.getSignList(),true)+":"+plugin.getTextList(bl2.getSignList(),true));
		//plugin.getServer().broadcastMessage(plugin.getTextList(bl1.getWoolList(),true)+":"+plugin.getTextList(bl2.getWoolList(),true));
		if(!matchSimpleBlockList(getChestList(),bl.getChestList())) return false;
		if(!matchSimpleBlockList(getSignList(),bl.getSignList())) return false;
		if(!matchSimpleBlockList(getWoolList(),bl.getWoolList())) return false;
		return true;
	}
	
	public static Boolean matchPartList(RMPartList bl1, RMPartList bl2){
		//plugin.getServer().broadcastMessage(plugin.getTextList(bl1.getChestList(),true)+":"+plugin.getTextList(bl2.getChestList(),true));
		//plugin.getServer().broadcastMessage(plugin.getTextList(bl1.getSignList(),true)+":"+plugin.getTextList(bl2.getSignList(),true));
		//plugin.getServer().broadcastMessage(plugin.getTextList(bl1.getWoolList(),true)+":"+plugin.getTextList(bl2.getWoolList(),true));
		if(!matchSimpleBlockList(bl1.getChestList(),bl2.getChestList())) return false;
		if(!matchSimpleBlockList(bl1.getSignList(),bl2.getSignList())) return false;
		if(!matchSimpleBlockList(bl1.getWoolList(),bl2.getWoolList())) return false;
		return true;
	}
	
	//Match Simple Block List
	public static Boolean matchSimpleBlockList(List<Block> blocks1, List<Block> blocks2){
		if(blocks1.size()!=blocks2.size()) return false;
		for(int i=0; i<blocks1.size(); i++){
			if(blocks1.get(i)!=blocks2.get(i)) return false;
		}
		return true;
	}
}