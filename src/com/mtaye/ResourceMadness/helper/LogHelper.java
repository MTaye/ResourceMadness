package com.mtaye.ResourceMadness.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;

import com.mtaye.ResourceMadness.GameCreature;
import com.mtaye.ResourceMadness.LogBlock;
import com.mtaye.ResourceMadness.RM;
import com.mtaye.ResourceMadness.Game;
import com.mtaye.ResourceMadness.GameConfig;
import com.mtaye.ResourceMadness.Log;

public class LogHelper {
	public RM rm;
	Helper rmHelper;
	
	public LogHelper(RM plugin){
		this.rm = plugin;
	}
	
	public String encodeLogListToString(List<LogBlock> logList){
		String line = "";
		if(logList.size()==0) return "LOG";
		
		HashMap<String, HashMap<Integer, HashMap<Integer, List<String>>>> worldList = new HashMap<String, HashMap<Integer, HashMap<Integer, List<String>>>>();
		
		for(LogBlock rmBlock : logList){
			Location loc = rmBlock.getLocation();
			String world = loc.getWorld().getName();
			int id = rmBlock.getType().getId();
			int data = rmBlock.getData();
			String pos = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
			if(!worldList.containsKey(world)) worldList.put(world, new HashMap<Integer, HashMap<Integer, List<String>>>());
			
			HashMap<Integer, HashMap<Integer, List<String>>> idList = worldList.get(world);
			if(!idList.containsKey(id)) idList.put(id, new HashMap<Integer, List<String>>());
				
			HashMap<Integer, List<String>> dataList = idList.get(id);
			if(!dataList.containsKey(data)) dataList.put(data, new ArrayList<String>());
				
			List<String> posList = dataList.get(data);
			if(!posList.contains(pos)) posList.add(pos);
		}
		
		for(String world : worldList.keySet()){
			line+=world;
			//ID
			HashMap<Integer, HashMap<Integer, List<String>>> idList = worldList.get(world);
			for(Integer id : idList.keySet()){
				line+=":"+id;
				//DATA
				HashMap<Integer, List<String>> dataList = idList.get(id);
				for(Integer data : dataList.keySet()){
					line+="."+data;
					//POS
					List<String> posList = dataList.get(data);
					for(String pos : posList){
						line+=","+pos;
					}
				}
			}
			line+=" ";
		}
		line = TextHelper.stripLast(line, " ");
		return line;
	}
	
	public String encodeLogCreatureListToString(List<GameCreature> logCreatureList){
		String line = "";
		if(logCreatureList.size()==0) return "LOG";

		HashMap<String, HashMap<Integer, List<String>>> worldList = new HashMap<String, HashMap<Integer, List<String>>>();
		
		for(GameCreature creature : logCreatureList){
			Location loc = creature.getLocation();
			String world = loc.getWorld().getName();
			int type = creature.getType().getTypeId();
			String pos = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
			
			if(!worldList.containsKey(world)) worldList.put(world, new HashMap<Integer, List<String>>());
			
			HashMap<Integer, List<String>> typeList = worldList.get(world);
			if(!typeList.containsKey(type)) typeList.put(type, new ArrayList<String>());
				
			List<String> posList = typeList.get(type);
			if(!posList.contains(pos)) posList.add(pos);
		}
		
		for(String world : worldList.keySet()){
			line+=world;
			//TYPE
			HashMap<Integer, List<String>> typeList = worldList.get(world);
			for(Integer type : typeList.keySet()){
				line+=":"+type;
				//POS
				List<String> posList = typeList.get(type);
				for(String pos : posList){
					line+=","+pos;
				}
			}
			line+=" ";
		}
		line = TextHelper.stripLast(line, " ");
		return line;
	}
	
	public String encodeLogToString(Log log){
		String line = "";
		line += encodeLogListToString(log.getList())+";";
		line += encodeLogListToString(log.getItemList())+";";
		line += encodeLogCreatureListToString(log.getCreatureList());
		return line;
	}
		
	public void parseLoadedLogData(String line){
		String[] args = line.split("=");
		if(args.length!=2) return;
		Game game = Game.getGame(args[0]);
		if(game==null) return;
		GameConfig config = game.getGameConfig();
		args = args[1].split(";");
		if((args.length>0)&&(args[0].length()>0)&&(args[0]!="LOG")) config.getLog().setList(getLogDataByString(args[0]));
		if((args.length>1)&&(args[1].length()>0)&&(args[1]!="LOG")) config.getLog().setItemList(getLogDataByString(args[1]));
		if((args.length>2)&&(args[2].length()>0)&&(args[2]!="LOG")) config.getLog().setCreatureList(getLogCreatureDataByString(args[2]));
		config.getLog().resetLocList();
	}
	
	public List<LogBlock> getLogDataByString(String strArg){
		List<LogBlock> listLog = new ArrayList<LogBlock>();
		String[] worldArgs = strArg.split(" ");
		for(String worldArg : worldArgs){
			String[] idArgs = worldArg.split(":"); 
			World world = rm.getServer().getWorld(idArgs[0]);
			
			if(world==null) continue;
			idArgs = Arrays.copyOfRange(idArgs, 1, idArgs.length);
			
			for(String idArg : idArgs){
				String[] dataArgs = idArg.split("\\.");
				int id = Helper.getIntByString(dataArgs[0]);
				
				if(id==-1) continue;
				Material mat = Material.getMaterial(id);
				
				if(mat==null) continue;
				dataArgs = Arrays.copyOfRange(dataArgs, 1, dataArgs.length);
				for(String dataArg : dataArgs){
					String[] posArgs = dataArg.split(",");
					byte data = Helper.getByteByString(posArgs[0]);
					
					if(data==-1) continue;
					posArgs = Arrays.copyOfRange(posArgs, 1, posArgs.length);
					for(int i=0; i<posArgs.length-2; i+=3){
						int xPos = Helper.getIntByString(posArgs[i]);
						int yPos = Helper.getIntByString(posArgs[i+1]);
						int zPos = Helper.getIntByString(posArgs[i+2]);
						Block b = world.getBlockAt(xPos, yPos, zPos);
						listLog.add(new LogBlock(b, mat, data));
					}
				}
			}
		}
		return listLog;
	}
	
	public List<GameCreature> getLogCreatureDataByString(String strArg){
		List<GameCreature> listLogCreature = new ArrayList<GameCreature>();
		String[] worldArgs = strArg.split(" ");
		for(String worldArg : worldArgs){
			String[] typeArgs = worldArg.split(":"); 
			World world = rm.getServer().getWorld(typeArgs[0]);
			
			if(world==null) continue;
			typeArgs = Arrays.copyOfRange(typeArgs, 1, typeArgs.length);
			
			for(String typeArg : typeArgs){
				String[] posArgs = typeArg.split(",");
				int type = Helper.getIntByString(posArgs[0]);
				if(type==-1) continue;
				EntityType entityType = EntityType.fromId(type);
				if(entityType==null) continue;
				posArgs = Arrays.copyOfRange(posArgs, 1, posArgs.length);
				for(int i=0; i<posArgs.length-2; i+=3){
					int x = Helper.getIntByString(posArgs[i]);
					int y = Helper.getIntByString(posArgs[i+1]);
					int z = Helper.getIntByString(posArgs[i+2]);
					Location loc = new Location(world, x, y, z);
					if(loc!=null) listLogCreature.add(new GameCreature(loc, entityType));
				}
			}
		}
		return listLogCreature;
	}
}
