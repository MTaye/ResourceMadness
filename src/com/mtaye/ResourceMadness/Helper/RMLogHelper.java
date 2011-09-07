package com.mtaye.ResourceMadness.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.mtaye.ResourceMadness.RM;
import com.mtaye.ResourceMadness.RMBlock;
import com.mtaye.ResourceMadness.RMGame;
import com.mtaye.ResourceMadness.RMGameConfig;
import com.mtaye.ResourceMadness.RMLog;
import com.mtaye.ResourceMadness.RMText;

public class RMLogHelper {
	public RM plugin;
	RMHelper rmHelper;
	
	public RMLogHelper(RM plugin){
		this.plugin = plugin;
	}
	
	public String encodeLogListToString(List<RMBlock> logList){
		String line = "";
		if(logList.size()==0) return "LOG";
		
		HashMap<String, HashMap<Integer, HashMap<Integer, List<String>>>> worldList = new HashMap<String, HashMap<Integer, HashMap<Integer, List<String>>>>();
		
		for(RMBlock rmBlock : logList){
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
		line = RMText.stripLast(line, " ");
		return line;
	}
	
	public String encodeLogToString(RMLog log){
		String line = "";
		line += encodeLogListToString(log.getList())+";";
		line += encodeLogListToString(log.getItemList());
		return line;
	}
		
	public void parseLoadedLogData(String[] strArgs, int lineNum){
		RMGame rmGame = RMGame.getGame(lineNum);
		if(rmGame!=null){
			RMGameConfig config = rmGame.getConfig();
			if((strArgs[0].length()>0)&&(strArgs[0]!="LOG")) config.getLog().setList(getLogDataByString(strArgs[0]));
			if((strArgs[1].length()>0)&&(strArgs[1]!="LOG")) config.getLog().setItemList(getLogDataByString(strArgs[1]));
			config.getLog().resetLocList();
		}
	}
	
	public List<RMBlock> getLogDataByString(String strArg){
		List<RMBlock> listLog = new ArrayList<RMBlock>();
		String[] worldArgs = strArg.split(" ");
		for(String worldArg : worldArgs){
			String[] idArgs = worldArg.split(":"); 
			World world = plugin.getServer().getWorld(idArgs[0]);
			if(world!=null){
				idArgs = Arrays.copyOfRange(idArgs, 1, idArgs.length);
				for(String idArg : idArgs){
					String[] dataArgs = idArg.split("\\.");
					int id = RMHelper.getIntByString(dataArgs[0]);
					if(id!=-1){
						Material mat = Material.getMaterial(id);
						if(mat!=null){
							dataArgs = Arrays.copyOfRange(dataArgs, 1, dataArgs.length);
							for(String dataArg : dataArgs){
								String[] posArgs = dataArg.split(",");
								byte data = RMHelper.getByteByString(posArgs[0]);
								if(data!=-1){
									posArgs = Arrays.copyOfRange(posArgs, 1, posArgs.length);
									for(int i=0; i<posArgs.length-2; i+=3){
										int xPos = RMHelper.getIntByString(posArgs[i]);
										int yPos = RMHelper.getIntByString(posArgs[i+1]);
										int zPos = RMHelper.getIntByString(posArgs[i+2]);
										Block b = world.getBlockAt(xPos, yPos, zPos);
										listLog.add(new RMBlock(b, mat, data));
									}
								}
							}
						}
					}
				}
			}
		}
		return listLog;
	}
}
