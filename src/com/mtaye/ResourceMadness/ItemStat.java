package com.mtaye.ResourceMadness;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ItemStat {
	private int id;
	private String name;
	private ChatColor color;
	private Material material;
	private ItemType type;
	private int points;
	private int durability;
	
	/*
	public ItemStat(int id, String name,  Material material){
		this(id, name, material, ItemType.OTHER, 0);
	}
	
	public ItemStat(int id, String name,  Material material, int points){
		this(id, name, material, ItemType.OTHER, points);
	}
	
	public ItemStat(int id, String name, Material material, ItemType type){
		this(id, name, material, type, 0);
	}
	
	public ItemStat(int id, String name, Material material, ItemType type, int points){
		this(id, name, material, type, points, 0);
	}
	
	*/
	public ItemStat(int id, String name, ChatColor color, Material material, ItemType type, int points, int durability){
		this.id = id;
		this.name = name;
		this.color = color;
		this.material = material;
		this.type = type;
		this.points = points;
		this.durability = durability;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public ChatColor getColor(){
		return color;
	}
	
	public Material getMaterial(){
		return material;
	}
	
	public ItemType getItemType(){
		return type;
	}
	
	public int getPoints(){
		return points;
	}
	
	public int getDurability(){
		return durability;
	}
}
