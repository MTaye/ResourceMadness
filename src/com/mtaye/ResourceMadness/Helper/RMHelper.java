package com.mtaye.ResourceMadness.Helper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import com.mtaye.ResourceMadness.RM;
import com.mtaye.ResourceMadness.RMGame;
import com.mtaye.ResourceMadness.RMTeam;
import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;

public final class RMHelper {
	public RM plugin;
	public RMHelper(RM plugin){
		this.plugin = plugin;
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
	public static int getIntByStringMaterial(String arg){
		Material mat = Material.getMaterial(arg.toUpperCase());
		if(mat!=null) return mat.getId();
		else return -1;
	}
	
	public static GameState getStateByInt(int i){
		switch(i){
			case 0: return GameState.SETUP;
			case 1: return GameState.COUNTDOWN;
			case 2: return GameState.GAMEPLAY;
			case 3: return GameState.GAMEOVER;
			default: return GameState.SETUP;
		}
	}
	
	public static InterfaceState getInterfaceByInt(int i){
		switch(i){
			case 0: return InterfaceState.FILTER;
			case 1: return InterfaceState.REWARD;
			case 2: return InterfaceState.TOOLS;
			case 3: return InterfaceState.FILTER_CLEAR;
			default: return InterfaceState.FILTER;
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
		return ChatColor.WHITE;
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
}
