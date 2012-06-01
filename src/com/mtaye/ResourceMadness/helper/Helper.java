package com.mtaye.ResourceMadness.helper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.Debug;
import com.mtaye.ResourceMadness.RM;
import com.mtaye.ResourceMadness.Game.GameState;
import com.mtaye.ResourceMadness.Game.InterfaceState;
import com.mtaye.ResourceMadness.GamePlayer.ChatMode;
import com.mtaye.ResourceMadness.setting.Setting;

public final class Helper {
	public static RM rm;
	public Helper(){
	}
	
	public static boolean copyFile(File inputFile, File outputFile){
		File inputFolder = new File(inputFile.getParent());
		File outputFolder = new File(outputFile.getParent());
		if(!inputFolder.exists()) return false;
		if(!inputFile.exists()) return false;
		if(!outputFolder.exists()) return false;
		if(outputFile.exists()) outputFile.delete();
		if(!outputFile.exists()){
			try{
				outputFile.createNewFile();
			}
			catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		try{
			FileReader in = new FileReader(inputFile);
			FileWriter out = new FileWriter(outputFile);
			int c;
			while ((c = in.read()) != -1) out.write(c);
			in.close();
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static short getShortByString(String arg){
		short data = 0;
		try{
			data = Byte.valueOf(arg);
			return data;
		} catch(Exception e){
			return -1;
		}
	}
	public static byte getByteByString(String arg){
		byte data = 0;
		try{
			data = Byte.valueOf(arg);
			return data;
		} catch(Exception e){
			return -1;
		}
	}
	public static Double getDoubleByString(String arg){
		Double d = null;
		try{
			d = Double.valueOf(arg);
			return d;
		} catch(Exception e){
			return null;
		}
	}
	
	public static int getIntByString(String arg){
		int i = 0;
		try{
			i = Integer.valueOf(arg);
			return i;
		} catch(Exception e){
			return -1;
		}
	}
	public static int getIntByString(String arg, int def){
		int i = 0;
		try{
			i = Integer.valueOf(arg);
			return i;
		} catch(Exception e){
			return def;
		}
	}
	public static int getBoolIntByString(String arg){
		int i = 0;
		try{
			i = Integer.valueOf(arg);
			return i;
		} catch(Exception e){
			try{
				i = Boolean.valueOf(arg)?1:0;
				return i;
			}
			catch(Exception ee){
				return -1;
			}
		}
	}
	public static int getMaterialIdByString(String arg){
		Material mat = Material.getMaterial(arg.toUpperCase());
		if(mat!=null) return mat.getId();
		else return -1;
	}
	
	public static List<Integer> getMaterialIdListByString(String arg){
		List<Integer> list = new ArrayList<Integer>();
		for(Material mat : Material.values()){
			if(mat.name().toLowerCase().contains(arg.toLowerCase())){
				list.add(mat.getId());
			}
		}
		return list;
	}
	
	public static GameState getStateByInt(int i){
		switch(i){
			case 0: return GameState.SETUP;
			case 1: return GameState.COUNTDOWN;
			case 2: return GameState.GAMEPLAY;
			case 3: return GameState.GAMEOVER;
			case 4: return GameState.PAUSED;
			default: return GameState.SETUP;
		}
	}
	
	public static InterfaceState getInterfaceByInt(int i){
		switch(i){
			case 0: return InterfaceState.FILTER;
			case 1: return InterfaceState.REWARD;
			case 2: return InterfaceState.TOOLS;
			case 3: return InterfaceState.FILTER_CLEAR;
			case 4: return InterfaceState.REWARD_CLEAR;
			case 5: return InterfaceState.TOOLS_CLEAR;
			default: return InterfaceState.FILTER;
		}
	}
	
	public static ChatMode getChatModeByInt(int i){
		switch(i){
		case 0: return ChatMode.WORLD;
		case 1: return ChatMode.GAME;
		case 2: return ChatMode.WORLD;
		default: return ChatMode.GAME;
		}
	}
	
	public static DyeColor getDyeByString(String color){
		for(DyeColor dyeColor : DyeColor.values()){
			if(dyeColor.name().equalsIgnoreCase(color.toLowerCase())){
				return dyeColor;
			}
		}
		return null;
	}
	
	public static ChatColor getChatColorByDye(DyeColor dye){
		switch(dye){
			case WHITE:
				return ChatColor.WHITE;
			case ORANGE:
				return ChatColor.GOLD;
			case MAGENTA:
				return ChatColor.LIGHT_PURPLE;
			case LIGHT_BLUE:
				return ChatColor.BLUE;
			case YELLOW:
				return ChatColor.YELLOW;
			case LIME:
				return ChatColor.GREEN;
			case PINK:
				return ChatColor.RED;
			case GRAY:
				return ChatColor.DARK_GRAY;
			case SILVER:
				return ChatColor.GRAY;
			case CYAN:
				return ChatColor.DARK_AQUA;
			case PURPLE:
				return ChatColor.DARK_PURPLE;
			case BLUE:
				return ChatColor.DARK_BLUE;
			case BROWN:
				return ChatColor.GOLD;
			case GREEN:
				return ChatColor.DARK_GREEN;
			case RED:
				return ChatColor.DARK_RED;
			case BLACK:
				return ChatColor.BLACK;
		}
		return null;
	}
	
	//Is Material
	public static Boolean isMaterial(Material b, Material... materials){
		for(Material mat : materials){
			if(b == mat){
				return true;
			}
		}
		return false;
	}
	
	public static List<ItemStack> cloneListItemStack(List<ItemStack> items){
		List<ItemStack> clonedItems = new ArrayList<ItemStack>();
		for(ItemStack item : items){
			clonedItems.add(item.clone());
		}
		return clonedItems;
	}
	
	public static Location getLocationByString(String location){
		if(location==null) return null;
		String[] args = location.split(",");
		if(args.length!=4) return null;
		double x = Helper.getIntByString(args[1]);
		double y = Helper.getIntByString(args[2]);
		double z = Helper.getIntByString(args[3]);
		World world = rm.getServer().getWorld(args[0]);
		if(world==null) return null;
		Location result = new Location(world, x, y, z);
		return result;
	}
}
