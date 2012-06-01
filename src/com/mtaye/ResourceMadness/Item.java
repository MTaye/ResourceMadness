package com.mtaye.ResourceMadness;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class Item {
	private int _amountHigh = -1;
	private ItemStack _item;
	
	public Item(int id){
		_item = new ItemStack(id);
	}
	public Item(int id, int amount){
		_item = new ItemStack(id);
		_item.setAmount(amount);
	}
	public Item(int id, int amount, short durability){
		_item = new ItemStack(id);
		_item.setAmount(amount);
		_item.setDurability(durability);
	}
	public Item(int id, int amount, short durability, byte data){
		_item = new ItemStack(id);
		_item.setAmount(amount);
		_item.setDurability(durability);
		_item.setData(new MaterialData(Material.getMaterial(id), data));
	}
	public Item(int id, int amount, int amountHigh){
		_item = new ItemStack(id);
		_item.setAmount(amount);
		_amountHigh = amountHigh;
	}
	public Item(int id, int amount, int amountHigh, short durability){
		_item = new ItemStack(id);
		_item.setAmount(amount);
		_amountHigh = amountHigh;
		_item.setDurability(durability);
	}
	public Item(int id, int amount, int amountHigh, short durability, byte data){
		_item = new ItemStack(id);
		_item.setAmount(amount);
		_amountHigh = amountHigh;
		_item.setDurability(durability);
		_item.setData(new MaterialData(Material.getMaterial(id), data));
	}
	public Item(int id, int amount, int amountHigh, int maxStack){
		_item = new ItemStack(id);
		_item.setAmount(amount);
		_amountHigh = amountHigh;
	}
	public Item(ItemStack item){
		_item = item.clone();
	}
	
	public Item clone(){
		Item rmItem = new Item(_item);
		return rmItem;
	}
	
	//Id
	public int getId(){
		return _item.getTypeId();
	}
	public void setId(int id){
		_item.setTypeId(id);
	}
	
	//Material
	public Material getMaterial(){
		return _item.getType();
	}
	public void setMaterial(Material material){
		_item.setType(material);
	}
	
	public int getMaxStackSize(){
		return _item.getMaxStackSize();
	}
	public void setAmountMaxStack(){
		_item.setAmount(_item.getMaxStackSize());
	}
	
	//Amount
	public int getAmount(){
		return _item.getAmount();
	}
	public void addAmount(int amount){
		_item.setAmount(_item.getAmount()+amount);
	}
	public void decAmount(int amount){
		_item.setAmount(_item.getAmount()-amount);
	}
	public void setAmount(int amount){
		_item.setAmount(amount);
	}
	
	//AmountHigh
	public int getAmountHigh(){
		return _amountHigh;
	}
	public void addAmountHigh(int amountHigh){
		_amountHigh+=(amountHigh);
	}
	public void decAmountHigh(int amountHigh){
		_amountHigh-=(amountHigh);
		if(_amountHigh<0) _amountHigh=0;
	}
	public void setAmountHigh(int amountHigh){
		_amountHigh = amountHigh;
		if(_amountHigh<0) _amountHigh=0;
	}
	
	//Item
	public ItemStack getItem(){
		return _item;
	}
	public ItemStack getItemClone(){
		return _item.clone();
	}
}