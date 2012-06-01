package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class PlayerDrops {
	List<ItemStack> _drops;
	List<ItemStack> _dropsArmor;
	
	public PlayerDrops(List<ItemStack> drops){
		this(drops, new ArrayList<ItemStack>());
	}
	
	public PlayerDrops(List<ItemStack> drops, List<ItemStack> dropsArmor){
		_drops = drops;
		_dropsArmor = dropsArmor;
	}
	
	public List<ItemStack> getDrops(){
		return _drops;
	}
	public void setDrops(List<ItemStack> drops){
		_drops = drops;
	}
	public void clearDrops(){
		_drops = null;
	}
	
	public List<ItemStack> getDropsArmor(){
		return _dropsArmor;
	}
	public void setDropsArmor(List<ItemStack> drops){
		_dropsArmor = drops;
	}
	public void clearDropsArmor(){
		_dropsArmor = null;
	}
	
	public void clear(){
		_drops = null;
		_dropsArmor = null;
	}
}
