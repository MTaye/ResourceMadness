package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ItemStats {
	private static List<Integer> listId = new ArrayList<Integer>();
	private static HashMap<Integer, ItemStat> mapId = new HashMap<Integer, ItemStat>();
	private static HashMap<Material, ItemStat> mapMaterial = new HashMap<Material, ItemStat>();
	
	public static void init(){
		add(0, "", Material.BREAD, ItemType.FOOD, 5);
		add(1, "", Material.CAKE, ItemType.FOOD, 12);
		add(2, "", Material.COOKIE, ItemType.FOOD, 1);
		add(3, "", Material.MELON, ItemType.FOOD, 2);
		add(4, "", Material.MUSHROOM_SOUP, ItemType.FOOD, 8);
		add(5, "", Material.RAW_CHICKEN, ItemType.FOOD, 5);
		add(6, "", Material.COOKED_CHICKEN, ItemType.FOOD, 6);
		add(7, "", Material.RAW_BEEF, ItemType.FOOD, 3);
		add(8, "", Material.COOKED_BEEF, ItemType.FOOD, 8);
		add(9, "", Material.PORK, ItemType.FOOD, 3);
		add(10, "", Material.GRILLED_PORK, ItemType.FOOD, 8);
		add(11, "", Material.RAW_FISH, ItemType.FOOD, 2);
		add(12, "", Material.COOKED_FISH, ItemType.FOOD, 5);
		add(13, "", Material.APPLE, ItemType.FOOD, 4);
		add(14, "", Material.GOLDEN_APPLE, ItemType.FOOD, 4);
		add(15, "", Material.ROTTEN_FLESH, ItemType.FOOD, 4);
		add(16, "", Material.SPIDER_EYE, ItemType.FOOD, 5);
		
		add(33, "H", ChatColor.AQUA, Material.DIAMOND_HELMET, ItemType.ARMOR, 3);
		add(34, "C", ChatColor.AQUA, Material.DIAMOND_CHESTPLATE, ItemType.ARMOR, 8);
		add(35, "L", ChatColor.AQUA, Material.DIAMOND_LEGGINGS, ItemType.ARMOR, 6);
		add(36, "B", ChatColor.AQUA, Material.DIAMOND_BOOTS, ItemType.ARMOR, 3);
		add(29, "H", ChatColor.WHITE, Material.IRON_HELMET, ItemType.ARMOR, 2);
		add(30, "C", ChatColor.WHITE, Material.IRON_CHESTPLATE, ItemType.ARMOR, 6);
		add(31, "L", ChatColor.WHITE, Material.IRON_LEGGINGS, ItemType.ARMOR, 5);
		add(32, "B", ChatColor.WHITE, Material.IRON_BOOTS, ItemType.ARMOR, 2);
		add(25, "H", ChatColor.DARK_GRAY, Material.CHAINMAIL_HELMET, ItemType.ARMOR, 2);
		add(26, "C", ChatColor.DARK_GRAY, Material.CHAINMAIL_CHESTPLATE, ItemType.ARMOR, 5);
		add(27, "L", ChatColor.DARK_GRAY, Material.CHAINMAIL_LEGGINGS, ItemType.ARMOR, 4);
		add(28, "B", ChatColor.DARK_GRAY, Material.CHAINMAIL_BOOTS, ItemType.ARMOR, 1);
		add(21, "H", ChatColor.YELLOW, Material.GOLD_HELMET, ItemType.ARMOR, 2);
		add(22, "C", ChatColor.YELLOW, Material.GOLD_CHESTPLATE, ItemType.ARMOR, 5);
		add(23, "L", ChatColor.YELLOW, Material.GOLD_LEGGINGS, ItemType.ARMOR, 3);
		add(24, "B", ChatColor.YELLOW, Material.GOLD_BOOTS, ItemType.ARMOR, 1);
		add(17, "H", ChatColor.RED, Material.LEATHER_HELMET, ItemType.ARMOR, 1);
		add(18, "C", ChatColor.RED, Material.LEATHER_CHESTPLATE, ItemType.ARMOR, 3);
		add(19, "L", ChatColor.RED, Material.LEATHER_LEGGINGS, ItemType.ARMOR, 2);
		add(20, "B", ChatColor.RED, Material.LEATHER_BOOTS, ItemType.ARMOR, 1);
		
		//Weapons
		add(37, "S", ChatColor.AQUA, Material.DIAMOND_SWORD, ItemType.WEAPON, 7, 1562);
		add(38, "S", ChatColor.WHITE, Material.IRON_SWORD, ItemType.WEAPON, 6, 251);
		add(39, "S", ChatColor.GRAY, Material.STONE_SWORD, ItemType.WEAPON, 5, 132);
		add(40, "S", ChatColor.YELLOW, Material.GOLD_SWORD, ItemType.WEAPON, 4, 33);
		add(41, "S", ChatColor.GOLD, Material.WOOD_SWORD, ItemType.WEAPON, 4, 60);
		add(42, "B", ChatColor.GOLD, Material.BOW, ItemType.WEAPON, 2);
		add(43, "A", ChatColor.GOLD, Material.ARROW, ItemType.WEAPON, 0);
		add(44, "T", ChatColor.RED, Material.TNT, ItemType.WEAPON, 0);
		add(45, "F", ChatColor.WHITE, Material.AIR, ItemType.WEAPON, 1);
		
		//Pickaxe
		add(46, "P", ChatColor.AQUA, Material.DIAMOND_PICKAXE, ItemType.TOOL, 5, 1562);
		add(47, "P", ChatColor.WHITE, Material.IRON_PICKAXE, ItemType.TOOL, 4, 251);
		add(48, "P", ChatColor.GRAY, Material.STONE_PICKAXE, ItemType.TOOL, 3, 132);
		add(49, "P", ChatColor.YELLOW, Material.GOLD_PICKAXE, ItemType.TOOL, 2, 33);
		add(50, "P", ChatColor.GOLD, Material.WOOD_PICKAXE, ItemType.TOOL, 2, 60);
		
		//Spade
		add(51, "S", ChatColor.AQUA, Material.DIAMOND_SPADE, ItemType.TOOL, 4, 1562);
		add(52, "S", ChatColor.WHITE, Material.IRON_SPADE, ItemType.TOOL, 3, 251);
		add(53, "S", ChatColor.GRAY, Material.STONE_SPADE, ItemType.TOOL, 2, 132);
		add(54, "S", ChatColor.YELLOW, Material.GOLD_SPADE, ItemType.TOOL, 1, 33);
		add(55, "S", ChatColor.GOLD, Material.WOOD_SPADE, ItemType.TOOL, 1, 60);
		
		//Axe
		add(56, "A", ChatColor.AQUA, Material.DIAMOND_AXE, ItemType.TOOL, 6, 1562);
		add(57, "A", ChatColor.WHITE, Material.IRON_AXE, ItemType.TOOL, 5, 251);
		add(58, "A", ChatColor.GRAY, Material.STONE_AXE, ItemType.TOOL, 4, 132);
		add(59, "A", ChatColor.YELLOW, Material.GOLD_AXE, ItemType.TOOL, 3, 33);
		add(60, "A", ChatColor.GOLD, Material.WOOD_AXE, ItemType.TOOL, 3, 60);
		
		//Hoe
		add(61, "H", ChatColor.AQUA, Material.DIAMOND_HOE, ItemType.TOOL, 1, 1562);
		add(62, "H", ChatColor.WHITE, Material.IRON_HOE, ItemType.TOOL, 1, 251);
		add(63, "H", ChatColor.GRAY, Material.STONE_HOE, ItemType.TOOL, 1, 132);
		add(64, "H", ChatColor.YELLOW, Material.GOLD_HOE,ItemType.TOOL, 1, 33);
		add(65, "H", ChatColor.GOLD, Material.WOOD_HOE, ItemType.TOOL, 1, 60);
		
		//Fishing Rod
		add(66, "R", ChatColor.GOLD, Material.FISHING_ROD, ItemType.TOOL, 1, 65);
		add(67, "W", ChatColor.GOLD, Material.WORKBENCH, ItemType.TOOL, 1, 65);
		add(68, "F", ChatColor.DARK_GRAY, Material.FURNACE, ItemType.TOOL, 1, 65);
		add(69, "C", ChatColor.GOLD, Material.CHEST, ItemType.TOOL, 1, 65);
		
		/*
		//Weapons
		add(37, "+-", ChatColor.AQUA, Material.DIAMOND_SWORD, ItemType.WEAPON, 7, 1562);
		add(38, "+-", ChatColor.WHITE, Material.IRON_SWORD, ItemType.WEAPON, 6, 251);
		add(39, "+-", ChatColor.GRAY, Material.STONE_SWORD, ItemType.WEAPON, 5, 132);
		add(40, "+-", ChatColor.YELLOW, Material.GOLD_SWORD, ItemType.WEAPON, 4, 33);
		add(41, "+-", ChatColor.GOLD, Material.WOOD_SWORD, ItemType.WEAPON, 4, 60);
		add(42, "D-", ChatColor.GOLD, Material.BOW, ItemType.WEAPON, 2);
		add(43, "*-", ChatColor.GOLD, Material.ARROW, ItemType.WEAPON, 0);
		add(44, "[]", ChatColor.RED, Material.TNT, ItemType.WEAPON, 0);
		add(45, "@", ChatColor.WHITE, Material.AIR, ItemType.WEAPON, 1);
		
	//Pickaxe
		add(46, "-)", ChatColor.AQUA, Material.DIAMOND_PICKAXE, ItemType.TOOL, 5, 1562);
		add(47, "-)", ChatColor.WHITE, Material.IRON_PICKAXE, ItemType.TOOL, 4, 251);
		add(48, "-)", ChatColor.GRAY, Material.STONE_PICKAXE, ItemType.TOOL, 3, 132);
		add(49, "-)", ChatColor.YELLOW, Material.GOLD_PICKAXE, ItemType.TOOL, 2, 33);
		add(50, "-)", ChatColor.GOLD, Material.WOOD_PICKAXE, ItemType.TOOL, 2, 60);
		
		//Spade
		add(51, "-D", ChatColor.AQUA, Material.DIAMOND_SPADE, ItemType.TOOL, 4, 1562);
		add(52, "-D", ChatColor.WHITE, Material.IRON_SPADE, ItemType.TOOL, 3, 251);
		add(53, "-D", ChatColor.GRAY, Material.STONE_SPADE, ItemType.TOOL, 2, 132);
		add(54, "-D", ChatColor.YELLOW, Material.GOLD_SPADE, ItemType.TOOL, 1, 33);
		add(55, "-D", ChatColor.GOLD, Material.WOOD_SPADE, ItemType.TOOL, 1, 60);
		
		//Axe
		add(56, "-7", ChatColor.AQUA, Material.DIAMOND_AXE, ItemType.TOOL, 6, 1562);
		add(57, "-7", ChatColor.WHITE, Material.IRON_AXE, ItemType.TOOL, 5, 251);
		add(58, "-7", ChatColor.GRAY, Material.STONE_AXE, ItemType.TOOL, 4, 132);
		add(59, "-7", ChatColor.YELLOW, Material.GOLD_AXE, ItemType.TOOL, 3, 33);
		add(60, "-7", ChatColor.GOLD, Material.WOOD_AXE, ItemType.TOOL, 3, 60);
		
		//Hoe
		add(61, "->", ChatColor.AQUA, Material.DIAMOND_HOE, ItemType.TOOL, 1, 1562);
		add(62, "->", ChatColor.WHITE, Material.IRON_HOE, ItemType.TOOL, 1, 251);
		add(63, "->", ChatColor.GRAY, Material.STONE_HOE, ItemType.TOOL, 1, 132);
		add(64, "->", ChatColor.YELLOW, Material.GOLD_HOE,ItemType.TOOL, 1, 33);
		add(65, "->", ChatColor.GOLD, Material.WOOD_HOE, ItemType.TOOL, 1, 60);
		*/
	}

	public static void add(int id, String name, ChatColor color, Material material){
		add(id, name, color, material, ItemType.OTHER, 0);
	}
	
	public static void add(int id, String name, ChatColor color, Material material, int points){
		add(id, name, color, material, ItemType.OTHER, points);
	}
	
	public static void add(int id, String name, ChatColor color, Material material, ItemType type){
		add(id, name, color, material, type, 0);
	}
	
	public static void add(int id, String name, Material material, ItemType type, int points){
		add(id, name, ChatColor.WHITE, material, type, points, 0);
	}
	
	public static void add(int id, String name, ChatColor color, Material material, ItemType type, int points){
		add(id, name, color, material, type, points, 0);
	}
	
	public static void add(int id, String name, ChatColor color, Material material, ItemType type, int points, int durability){
		ItemStat itemStat = new ItemStat(id, name, color, material, type, points, durability);
		listId.add(id);
		mapId.put(id, itemStat);
		mapMaterial.put(material, itemStat);
	}
	
	public static ItemStat get(int id){
		if(id<mapId.size()) return mapId.get(id);
		return null;
	}
	
	public static ItemStat get(Material mat){
		if(mapMaterial.containsKey(mat)) return mapMaterial.get(mat);
		return null;
	}
	
	public static String getName(int id){
		if(id<mapId.size()) return mapId.get(id).getName();
		return null;
	}
	
	public static Material getMaterial(int id){
		if(id<mapId.size()) return mapId.get(id).getMaterial();
		return null;
	}
	
	public static ItemType getItemType(int id){
		if(id<mapId.size()) return mapId.get(id).getItemType();
		return null;
	}
	
	public static int getPoints(int id){
		if(id<mapId.size()) return mapId.get(id).getPoints();
		return 0;
	}
	
	public static int getDurability(int id){
		if(id<mapId.size()) return mapId.get(id).getDurability();
		return 0;
	}
	
	public static ItemStat[] getItemStats(ItemType type){
		List<ItemStat> result = new ArrayList<ItemStat>();
		for(Integer i : listId){
			ItemStat is = mapId.get(i);
			if(is.getItemType()==type) result.add(is);
		}
		return result.toArray(new ItemStat[result.size()]);
	}
	
	public static Material[] getMaterials(ItemType type){
		List<Material> result = new ArrayList<Material>();
		for(Integer i : listId){
			ItemStat is = mapId.get(i);
			if(is.getItemType()==type){
				result.add(is.getMaterial());
			}
		}
		return result.toArray(new Material[result.size()]);
	}
	
	public static Material[] getMaterials(ItemType type, String name){
		List<Material> result = new ArrayList<Material>();
		for(Integer i : listId){
			ItemStat is = mapId.get(i);
			if(is.getItemType()==type){
				if(name.equalsIgnoreCase(ChatColor.stripColor(is.getName()))) result.add(is.getMaterial());
			}
		}
		return result.toArray(new Material[result.size()]);
	}
	
	public static void reset(){
		listId.clear();
		mapId.clear();
		mapMaterial.clear();
		init();
	}
	
	static{
		init();
	}
}
