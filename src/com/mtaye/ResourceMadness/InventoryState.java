package com.mtaye.ResourceMadness;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryState {
	private ItemStack[] items;
	private ItemStack[] armor;
	
	public InventoryState(){
		init();
	}
	
	public InventoryState(PlayerInventory inventory){
		items = inventory.getContents().clone();
		armor = inventory.getArmorContents().clone();
	}
	
	public InventoryState(GamePlayerInventory inventory){
		items = inventory.getContents().clone();
		armor = inventory.getArmorContents().clone();
	}
	
	private void init(){
		items = new ItemStack[36];
		armor = new ItemStack[4];
	}
	
	public void setContents(ItemStack[] contents){
		items = contents.clone();
	}
	
	public ItemStack[] getContents(){
		return items;
	}
	
	public void setArmorContents(ItemStack[] armorContents){
		armor = armorContents.clone();
	}
	
	public ItemStack[] getArmorContents(){
		return armor;
	}
	
	public PlayerInventory updatePlayerInventory(PlayerInventory inventory){
		inventory.setContents(items);
		inventory.setArmorContents(armor);
		return inventory;
	}
	
	public GamePlayerInventory updatePlayerInventory(GamePlayerInventory inventory){
		inventory.setContents(items);
		inventory.setArmorContents(armor);
		return inventory;
	}
	
	public void clear(){
		init();
	}
}
