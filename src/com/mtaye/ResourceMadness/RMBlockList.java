package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;

public class RMBlockList {
	
	private static List<Block> afterBlockList = new ArrayList<Block>();
	private static List<Block> preBlockList = new ArrayList<Block>();
	
	private RMBlockList(){
	}
	
	public static void addBlock(Block b){
		if(!preBlockList.contains(b)){
			preBlockList.add(b);
		}
	}
	
	public static void removeBlock(Block b){
		if(preBlockList.contains(b)){
			preBlockList.remove(b);
		}
	}
	
	public static void replaceBlock(Block rb, Block b){
		if(preBlockList.contains(rb)){
			preBlockList.set(preBlockList.indexOf(rb), b);
		}
	}
	
	public static void save(){
		/*
		if(wHash.size()>0)
		{
			File folder = getDataFolder();
			if(!folder.exists()){
				log.log(Level.INFO, "Creating config directory...");
				folder.mkdir();
			}
			File file = new File(folder.getAbsolutePath()+"/ResourceMadness.cfg");
			if(!file.exists()){
				log.log(Level.INFO, "Configuration file not found! Creating one...");
				try {
					file.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			try {
				FileOutputStream output = new FileOutputStream(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
				bw.write("[OuchPistons v"+pdfFile.getVersion()+"]\n");
				int i=0;
				while(i<wHash.size())
				{
					Location loc = wHash.get(i);
					String line;
					line = loc.getX()+","+loc.getY()+","+loc.getZ()+","+loc.getWorld().getName()+"\n";
					bw.write(line);
					i++;
				}
				bw.flush();
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Unexpected error.");
			}
		}
		*/
	}
	
	public static void load(){
		/*
		wHash.clear();
		File folder = getDataFolder();
		if(!folder.exists()){
			log.log(Level.INFO, "Config folder not found! Will create one on save...");
			return;
		}
		File file = new File(folder.getAbsolutePath()+"/OuchPistons.cfg");
		if(file.exists()){
			FileInputStream input;
			try {
				input = new FileInputStream(file.getAbsoluteFile());
				InputStreamReader isr = new InputStreamReader(input);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while(true){
					line = br.readLine();
					if(line == null){
						break;
					}
					if(line.startsWith("["))
					{
						continue;
					}
					String splitLine[] = line.split(",");
					if(splitLine.length>=4){
						Location loc = new Location(getServer().getWorld(splitLine[3]),new Double(splitLine[0]),new Double(splitLine[1]),new Double(splitLine[2]));
						wHash.put(wHash.size(), loc);
					}
				}
				input.close();
				//saveConfig();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Could not find chestfile");
		}
		*/
	}
}
