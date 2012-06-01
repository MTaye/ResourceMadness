package com.mtaye.ResourceMadness;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class GamePlayerInventory implements Inventory{
	private Inventory inventory;
	private ItemStack[] inventoryArmor;
	
	public GamePlayerInventory(){
		initEmptyInventory();
	}
	
	public GamePlayerInventory(PlayerInventory inventory){
		initEmptyInventory();
		setInventory(inventory);
	}
	
	public GamePlayerInventory(ItemStack[] contents, ItemStack[] armorContents){
		initEmptyInventory();
		inventory.setContents(contents);
		try{
			PlayerInventory playerInventory = (PlayerInventory)inventory;
			playerInventory.setArmorContents(armorContents);
		}
		catch(Exception e){}
		inventoryArmor = armorContents;
	}
	
	public void initEmptyInventory(){
		inventory = Bukkit.createInventory(null, InventoryType.PLAYER);
		try{
			PlayerInventory playerInventory = (PlayerInventory)inventory;
			playerInventory.setArmorContents(new ItemStack[playerInventory.getArmorContents().length]);
		}
		catch(Exception e){}
		inventoryArmor = new ItemStack[4];
	}
	
	public void setOfflineInventory(){
		ItemStack[] contents = inventory.getContents();
		ItemStack[] armorContents;
		try{
			PlayerInventory playerInventory = (PlayerInventory)inventory;
			armorContents = playerInventory.getArmorContents();
		}
		catch(Exception e){
			armorContents = inventoryArmor;
		}
		initEmptyInventory();
		inventory.setContents(contents);
		try{
			PlayerInventory playerInventory = (PlayerInventory)inventory;
			playerInventory.setArmorContents(armorContents);
		}
		catch(Exception e){}
		inventoryArmor = armorContents;
	}
	
	public void setInventory(PlayerInventory inventory){
		if(inventory==null) return;
		this.inventory = inventory;
		inventoryArmor = inventory.getArmorContents();
	}
	
	public void updatePlayerInventory(PlayerInventory inventory){
		if(inventory==null) return;
		inventory.setContents(this.inventory.getContents());
		/*
		try{
			PlayerInventory playerInventory = (PlayerInventory)this.inventory;
			inventory.setArmorContents(playerInventory.getArmorContents());
		}
		catch(Exception e){}
		*/
		inventory.setArmorContents(inventoryArmor);
		setInventory(inventory);
	}

	@Override
	public HashMap<Integer, ItemStack> addItem(ItemStack... arg0) {
		return inventory.addItem(arg0);
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(int arg0) {
		return inventory.all(arg0);
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(Material arg0) {
		return inventory.all(arg0);
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(ItemStack arg0) {
		return inventory.all(arg0);
	}

	@Override
	public void clear() {
		inventory.clear();
		try{
			PlayerInventory playerInventory = (PlayerInventory)inventory;
			playerInventory.setArmorContents(new ItemStack[playerInventory.getArmorContents().length]);
		}
		catch(Exception e){}
		inventoryArmor = new ItemStack[inventoryArmor.length];
	}

	@Override
	public void clear(int arg0) {
		inventory.clear(arg0);
		
	}

	@Override
	public boolean contains(int arg0) {
		return inventory.contains(arg0);
	}

	@Override
	public boolean contains(Material arg0) {
		return inventory.contains(arg0);
	}

	@Override
	public boolean contains(ItemStack arg0) {
		return inventory.contains(arg0);
	}

	@Override
	public boolean contains(int arg0, int arg1) {
		return inventory.contains(arg0);
	}

	@Override
	public boolean contains(Material arg0, int arg1) {
		return inventory.contains(arg0);
	}

	@Override
	public boolean contains(ItemStack arg0, int arg1) {
		return inventory.contains(arg0, arg1);
	}

	@Override
	public int first(int arg0) {
		return inventory.first(arg0);
	}

	@Override
	public int first(Material arg0) {
		return inventory.first(arg0);
	}

	@Override
	public int first(ItemStack arg0) {
		return inventory.first(arg0);
	}

	@Override
	public int firstEmpty() {
		return inventory.firstEmpty();
	}

	@Override
	public ItemStack[] getContents() {
		return inventory.getContents();
	}

	@Override
	public ItemStack getItem(int arg0) {
		return inventory.getItem(arg0);
	}

	@Override
	public int getMaxStackSize() {
		if(inventory==null) return -1;
		return inventory.getMaxStackSize();
	}

	@Override
	public String getName() {
		return inventory.getName();
	}

	@Override
	public int getSize() {
		return inventory.getSize();
	}

	@Override
	public String getTitle() {
		return inventory.getTitle();
	}

	@Override
	public InventoryType getType() {
		return inventory.getType();
	}

	@Override
	public List<HumanEntity> getViewers() {
		return inventory.getViewers();
	}

	@Override
	public ListIterator<ItemStack> iterator() {
		return inventory.iterator();
	}

	@Override
	public ListIterator<ItemStack> iterator(int arg0) {
		return inventory.iterator(arg0);
	}

	@Override
	public void remove(int arg0) {
		inventory.remove(arg0);
	}

	@Override
	public void remove(Material arg0) {
		inventory.remove(arg0);
	}

	@Override
	public void remove(ItemStack arg0) {
		inventory.remove(arg0);
	}

	@Override
	public HashMap<Integer, ItemStack> removeItem(ItemStack... arg0) {
		return inventory.removeItem(arg0);
	}

	@Override
	public void setContents(ItemStack[] arg0) {
		inventory.setContents(arg0);
	}

	@Override
	public void setItem(int arg0, ItemStack arg1) {
		inventory.setItem(arg0, arg1);
	}

	@Override
	public void setMaxStackSize(int arg0) {
		inventory.setMaxStackSize(arg0);
	}

	public ItemStack[] getArmorContents() {
		try{
			PlayerInventory playerInventory = (PlayerInventory)inventory;
			return playerInventory.getArmorContents();
		}
		catch(Exception e){
			return inventoryArmor;
		}
	}

	public void setArmorContents(ItemStack[] arg0) {
		try{
			PlayerInventory playerInventory = (PlayerInventory)inventory;
			playerInventory.setArmorContents(arg0);
		}
		catch(Exception e){
		}
		inventoryArmor = arg0;
	}

	@Override
	public InventoryHolder getHolder() {
		return inventory.getHolder();
	}
}
