package com.mtaye.ResourceMadness.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RM;
import com.mtaye.ResourceMadness.RMDebug;
import com.mtaye.ResourceMadness.RMItem;

public class RMTextHelper {
	public static RM rm;
	
	private RMTextHelper(){
	}
	
	public static String colorizeString(String str, ChatColor... colors){
		if((str==null)||(str.length() == 0)) return str;
		if((colors == null)||(colors.length == 0)) return str;
		String line = "";
		int pos = 0;
		int colorPos = 0;
		while(pos<str.length()){
			line+=colors[colorPos]+str.substring(pos, pos+1);
			colorPos++;
			pos++;
			if(colorPos==colors.length) colorPos = 0;
		}
		return line;
	}
	
	public static String stripLast(String str, String s){
		int pos = str.lastIndexOf(s);
		if(pos!=-1){
			String part1 = str.substring(0, pos);
			String part2 = str.substring(pos+s.length());
			return part1+part2;
		}
		return str;
	}
	
	public static String getFormattedItemStringByHashMap(HashMap<Integer, Material> hashMap){
		String line = "";
		Integer[] array = hashMap.keySet().toArray(new Integer[hashMap.size()]);
		Arrays.sort(array);
		for(Integer id : array){
			line+=""+ChatColor.YELLOW+id+ChatColor.GRAY+":"+ChatColor.WHITE+hashMap.get(id).name()+", ";
		}
		line = stripLast(line, ",");
		return line;
	}
	
	//Get Formatted String By List
	public static String getFormattedStringByList(List<String> strList){
		String line = "";
		for(String str : strList){
			line+=str+ChatColor.WHITE+", ";
		}
		line = stripLast(line, ",");
		return line;
	}
	
	//Get Formatted String By List Material
	public static String getFormattedStringByListMaterial(List<Material> materials){
		String line = "";
		if(materials.size()!=1){
			for(Material mat : materials){
				line+=mat.getId()+", ";
			}
		}
		else{
			for(Material mat : materials){
				line+=mat.name()+", ";
			}
		}
		line = stripLast(line, ",");
		return line;
	}
	
	//Get Text BlockList
	public static String getTextBlockList(List<List<Block>> blockList, boolean allowNull){
		String line = "";
		for(List<Block> bList : blockList){
			for(Block b : bList){
				if(b!=null){
					line+=b.getType().name();
				}
				else if(allowNull) line+="null";
				line+=",";
			}
		}
		return stripLast(line, ",");
	}
	
	//Get Text List
	public static String getTextList(List<Block> bList, boolean allowNull){
		String line = "";
		for(Block b : bList){
			if(b!=null){
				line+=b.getType().name();
			}
			else if(allowNull) line+="null";
			line+=",";
		}
		return stripLast(line, ",");
	}
	
	public static List<String> getStringListFromArray(String[] args){
		List<String> list = new ArrayList<String>();
		for(String arg : args){
			list.add(arg);
		}
		return list;
	}
	
	public static List<String> separateStringToList(String str, String strMainSeparator, String... strSeparators){
		return getStringListFromArray(separateStringToArray(str, strMainSeparator, strSeparators));
	}
	
	public static String[] separateStringToArray(String str, String strMainSeparator, String... strSeparators){
		RMDebug.warning("str: "+str);
		for(String strSeparator : strSeparators){
			str = str.replace(strSeparator, strMainSeparator);
			RMDebug.warning("str_replaced: "+str);
		}
		String[] strArgs = str.split(strMainSeparator);
		for(String strArg : strArgs){
			RMDebug.warning("str_split: "+strArg);
			strArg = strArg.trim();
			RMDebug.warning("str_trim: "+strArg);
		}
		return strArgs;
	}
	
	//Get String By String List
	public static String getStringByStringList(List<String> strList, String strSeparator){
		return getStringByStringList(strList, strSeparator, null, null);
	}
	
	//Get String By String List
	public static String getStringByStringList(List<String> strList, String strSeparator, String strPre, String strAfter){
		if(strPre==null) strPre = "";
		if(strAfter==null) strAfter = "";
		if(strSeparator==null) strSeparator = ",";
		String line = "";
		if(strList!=null){
			for(String str : strList){
				line+=strPre+str+strAfter+strSeparator;
			}
			if(line.length()!=0) line = stripLast(line, strSeparator);
		}
		return line;
	}
	
	//Include Item
	public static String includeItem(RMItem rmItem){
		int i1 = rmItem.getAmount();
		int i2 = rmItem.getAmountHigh();
		if(i2>0) return ChatColor.GRAY+":"+i1+"-"+i2;
		else if(i1!=1) return ChatColor.GRAY+":"+i1;
		return "";
	}
	
	//Get Sorted Items from ItemStack Array
	public static String getStringSortedItems(List<ItemStack> items){
		return getStringSortedItems(items, rm.config.getTypeLimit());
	}
	public static String getStringSortedItems(List<ItemStack> items, int typeLimit){
		String strItems = "";
		HashMap<Integer, ItemStack> hashItems = RMInventoryHelper.combineItemsByItemStack(items);
		
		Integer[] array = hashItems.keySet().toArray(new Integer[hashItems.size()]);
		Arrays.sort(array);
		if(array.length>typeLimit){
			for(Integer id : array){
				strItems += ChatColor.WHITE+""+id+includeItem(new RMItem(hashItems.get(id)))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer id : array){
				strItems += ChatColor.WHITE+""+Material.getMaterial(id)+includeItem(new RMItem(hashItems.get(id)))+ChatColor.WHITE+", ";
			}
		}
		strItems = stripLast(strItems, ", ");
		return strItems;
	}
	
	public static String firstLetterToUpperCase(String str){
		if(str.length()>0){
			String character = str.substring(0, 1);
			str = str.replaceFirst(character,character.toUpperCase());
		}
		return str;
	}
	
	public static String getTextFromArgs(String[] args){
		return getTextFromArgs(args, 0);
	}
	
	public static String getTextFromArgs(String[] args, int beginIndex){
		String str = "";
		for(int i=beginIndex; i<args.length; i++){
			str+=args[i]+" ";
		}
		return str.trim();
	}
	
	public static boolean equalsIgnoreCase(String arg, String... strArray){
		for(String str : strArray){
			if(str.toLowerCase() == arg.toLowerCase()) return true;
		}
		return false;
	}
	
	public static String genString(String str, int length){
		if((str==null)||(str.length()==0)) return "";
		while(str.length()<length){
			str+=str;
		}
		return str.substring(0, length);
	}
}
