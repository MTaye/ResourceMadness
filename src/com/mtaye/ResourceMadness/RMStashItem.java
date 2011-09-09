package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMGame.HandleState;

public class RMStashItem {
	public static RM plugin;
	
	HashMap<String, ItemStack> _items = new HashMap<String, ItemStack>();
	public int _id = -1; 
	
	public RMStashItem(int id){
		_id = id;
	}
	public RMStashItem(ItemStack item){
		_id = item.getTypeId();
		addItem(item);
	}
	
	public RMStashItem clone(){
		RMStashItem clone = new RMStashItem(_id);
		List<ItemStack> items = getItems();
		for(ItemStack item : items){
			clone.addItem(item.clone());
		}
		return clone;
	}
	
	//Get Size
	public int size(){
		return _items.size();
	}
	
	public Material getType(){
		return Material.getMaterial(_id);
	}
	
	//Get Items
	public HashMap<String, ItemStack> getHashItems(){
		return _items;
	}
	//Set Items
	public void setHashItems(HashMap<String, ItemStack> items){
		_items = items;
	}
	
	public int getId(){
		return _id;
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
	public HandleState addItem(ItemStack item){
		if(_id!=item.getTypeId()) return HandleState.NO_CHANGE;
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)){
			ItemStack hashItem = _items.get(idData);
			hashItem.setAmount(hashItem.getAmount()+item.getAmount());
			return HandleState.MODIFY;
		}
		else{
			_items.put(idData, item.clone());
			return HandleState.ADD;
		}
	}
	
	//Add Items
	public void addItems(List<ItemStack> items){
		for(ItemStack item : items){
			addItem(item);
		}
	}
	
	//Get by Amount
	public List<ItemStack> getItemByAmount(int amount){
		List<ItemStack> returnItems = new ArrayList<ItemStack>();
		Iterator<ItemStack> i = _items.values().iterator();
		while (i.hasNext()) {
			ItemStack item = i.next().clone();
			if(amount>0){
				int overflow = item.getAmount()-amount;
				if(overflow>=0){
					ItemStack itemClone = item.clone();
					itemClone.setAmount(amount);
					returnItems.add(itemClone);
					item.setAmount(item.getAmount()-amount);
					break;
				}
				else{
					amount = -overflow;
					returnItems.add(item.clone());
				};
			}
			else break;
		}
		return returnItems;
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
	
	public void setItem(ItemStack item){
		clear();
		addItem(item);
	}
	
	public List<ItemStack> removeByItemStack(ItemStack item){
		List<ItemStack> returnItems = new ArrayList<ItemStack>();
		Iterator<ItemStack> i = _items.values().iterator();
		ItemStack itemClone = item.clone();
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)){
			ItemStack hashItem = i.next();
			int amount = itemClone.getAmount();
			if(amount>0){
				int overflow = hashItem.getAmount()-amount;
				if(overflow>=0){
					ItemStack hashItemClone = hashItem.clone();
					hashItemClone.setAmount(amount);
					returnItems.add(hashItemClone);
					hashItem.setAmount(hashItem.getAmount()-amount);
					if(hashItem.getAmount()==0) i.remove();
				}
				else{
					returnItems.add(hashItem.clone());
					i.remove();
				};
			}
		}
		return returnItems;
	}
	
	public List<ItemStack> removeByItemStackAll(ItemStack item){
		List<ItemStack> returnItems = new ArrayList<ItemStack>();
		Iterator<ItemStack> i = _items.values().iterator();
		ItemStack itemClone = item.clone();
		String idData = getIdDataByItemStack(item);
		if(_items.containsKey(idData)){
			ItemStack hashItem = i.next();
			int amount = itemClone.getAmount();
			if(amount>0){
				returnItems.add(hashItem.clone());
				i.remove();
			}
		}
		return returnItems;
	}
	
	public List<ItemStack> removeByAmount(int amount){
		List<ItemStack> returnItems = new ArrayList<ItemStack>();
		Iterator<ItemStack> i = _items.values().iterator();
		while (i.hasNext()) {
			ItemStack item = i.next();
			if(amount>0){
				int overflow = item.getAmount()-amount;
				if(overflow>=0){
					ItemStack itemClone = item.clone();
					itemClone.setAmount(amount);
					returnItems.add(itemClone);
					item.setAmount(item.getAmount()-amount);
					if(item.getAmount()==0){
						i.remove();
					}
					break;
				}
				else{
					amount = -overflow;
					returnItems.add(item.clone());
					i.remove();
				};
			}
			else break;
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
	
	//Remove Item by ItemStack by Amount
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