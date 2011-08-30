package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RMStashItem {
	HashMap<String, ItemStack> _items = new HashMap<String, ItemStack>();
	public RMStashItem(){
	}
	public RMStashItem(ItemStack item){
		addItem(item);
	}
	
	//Get Size
	public int getSize(){
		return _items.size();
	}
	
	//Get Items
	public HashMap<String, ItemStack> getHashItems(){
		return _items;
	}
	//Set Items
	public void setHashItems(HashMap<String, ItemStack> items){
		_items = items;
	}
	
	//Get Amount
	public int getAmount(){
		int amount = 0;
		for(ItemStack hashItem : _items.values()){
			amount+=hashItem.getAmount();
		}
		return amount;
	}
	
	//Get Amount by ItemStack
	public int getAmountByItemStack(ItemStack item){
		int amount = 0;
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)){
			amount = _items.get(idData).getAmount();
		}
		return amount;
	}
	
	//Add Item
	public void addItem(ItemStack item){
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)){
			int amount = _items.get(idData).getAmount()+item.getAmount();
			ItemStack itemClone = item.clone();
			itemClone.setAmount(amount);
			_items.put(idData, itemClone);
		}
		else _items.put(idData, item);
	}
	
	//Get Items
	public List<ItemStack> getItems(){
		List<ItemStack> returnItems = new ArrayList<ItemStack>();
		for(ItemStack hashItem : _items.values()){
			returnItems.add(hashItem);
		}
		return returnItems;
	}
	
	//Get Item
	public ItemStack getItem(){
		for(ItemStack hashItem : _items.values()){
			return hashItem;
		}
		return null;
	}
	
	//Get Item By ItemStack
	public ItemStack getItemByItemStack(ItemStack item){
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)) return _items.get(idData);
		return null;
	}
	
	//Remove All - Obsolete
	public List<ItemStack> removeAll(ItemStack item, boolean byAmount){
		List<ItemStack> returnItems = new ArrayList<ItemStack>();
		Iterator<ItemStack> i = _items.values().iterator();
		ItemStack itemClone = item.clone();
		while (i.hasNext()) {
			ItemStack hashItem = i.next();
			if(hashItem.getTypeId()==itemClone.getTypeId()){
				if(byAmount){
					int amount = itemClone.getAmount();
					if(amount!=0){
						int overflow = hashItem.getAmount()-amount;
						if(overflow>=0){
							ItemStack hashItemClone = hashItem.clone();
							hashItemClone.setAmount(amount);
							returnItems.add(hashItemClone);
							hashItem.setAmount(hashItem.getAmount()-amount);
							if(hashItem.getAmount()==0) i.remove();
						}
						else{
							itemClone.setAmount(-overflow);
							returnItems.add(hashItem.clone());
							i.remove();
						}
					}
				}
				else{
					returnItems.add(hashItem);
					i.remove();
				}
			}
		}
		return returnItems;
	}
	
	//Remove Item by ItemStack
	public void removeItemByItemStack(ItemStack item){
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)){
			_items.remove(idData);
		}
	}
	
	//Remove Item by ItemStack By Amount
	public void removeItemByItemStackByAmount(ItemStack item){
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)){
			ItemStack hashItem = _items.get(idData);
			int amount = item.getAmount();
			if(amount!=0){
				hashItem.setAmount(hashItem.getAmount()-amount);
			}
			if(hashItem.getAmount()==0) _items.remove(hashItem);
		}
	}
	
	//Clear
	public void clear(){
		_items.clear();
	}
	
	//Clear Item by ItemStack
	public void clearItemByItemStack(ItemStack item){
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)){
			_items.remove(idData);
		}
	}
	
	//Get IdData by ItemStack
	public String getIdDataByItemStack(ItemStack item){
		String idData = ""+item.getTypeId()+":"+item.getDurability();
		if(item.getData()!=null) idData += ":"+Byte.toString(item.getData().getData());
		return idData;
	}
}