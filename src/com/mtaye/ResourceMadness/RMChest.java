package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RMChest{
	private Chest _chest;
	private HashMap<Integer, RMItem> _items = new HashMap<Integer, RMItem>();
	private RMTeam _team;
	private RM plugin;
	
	public RMChest(Chest chest, RM plugin){
		_chest = chest;
		this.plugin = plugin;
	}
	public RMChest(Chest chest, RMTeam team, RM plugin){
		_chest = chest;
		_team = team;
		this.plugin = plugin;
	}
	
	public RMTeam getTeam(){
		return _team;
	}
	
	public Chest getChest(){
		return _chest;
	}
	public void setChest(Chest chest){
		if(chest!=null){
			_chest = chest;
		}
	}
	
	public ItemStack[] getContents(){
		if(_chest!=null){
			List<ItemStack> items = new ArrayList<ItemStack>();
			Inventory inv = _chest.getInventory();
			for(ItemStack item : inv.getContents()){
				if((item!=null)&&(item.getType()!=Material.AIR)){
					items.add(item);
				}
			}
			return items.toArray(new ItemStack[items.toArray().length]);
		}
		return null;
	}
	
	public void clearItems(){
		_items.clear();
	}
	
	public int addItem(ItemStack item){
		int id = item.getTypeId();
		int overflow = 0;
		int newAmount;
		if(_items.containsKey(id)){
			newAmount = _items.get(id).getAmount() + item.getAmount();
		}
		else newAmount = item.getAmount();
		
		HashMap<Integer, RMItem> items = getTeam().getGame().getItems().getItems();
		if(items.containsKey(id)) overflow = items.get(id).getAmount() - newAmount;

		if(overflow<0){
			overflow=-overflow;
			newAmount-=overflow;
		}
		else overflow = 0;

		_items.put(id, new RMItem(id, newAmount));
		return overflow;
	}
	
	public int getItemAmount(Material mat){
		if(_items.containsKey(mat)){
			return _items.get(mat).getAmount();
		}
		return -1;
	}
	public HashMap<Integer, RMItem> getItems(){
		return _items;//_itemsToFind;
	}
	public int getItemsTotal(){
		int total = 0;
		for(RMItem rmItem : _items.values()){
			total+=rmItem.getAmount();
		}
		return total;
	}
	public int getItemsLeftInt(){
		int itemsLeft = 0;
		HashMap<Integer, RMItem> items = getTeam().getGame().getItems().getItems();
		for(Integer item : items.keySet()){
			RMItem rmItem = items.get(item);
			int amount = rmItem.getAmount();
			if(_items.containsKey(item)) amount -= _items.get(item).getAmount();
			if(amount>0) itemsLeft++;
		}
		return itemsLeft;
	}
	public HashMap<Integer, RMItem> getItemsLeft(){
		HashMap<Integer, RMItem> itemsLeft = new HashMap<Integer, RMItem>();
		HashMap<Integer, RMItem> items = getTeam().getGame().getItems().getItems();
		for(Integer item : items.keySet()){
			RMItem rmItem = items.get(item);
			int amount = rmItem.getAmount();
			if(_items.containsKey(item)) amount -= _items.get(item).getAmount();
			if(amount>0) itemsLeft.put(item, new RMItem(item, amount));
		}
		return itemsLeft;
	}
	
	public RMItem getItemLeft(Integer item){
		HashMap<Integer, RMItem> items = getTeam().getGame().getItems().getItems();
		plugin.getServer().broadcastMessage("items:"+items.get(item).getAmount()+",_items:"+_items.get(item).getAmount()+"="+(items.get(item).getAmount() - _items.get(item).getAmount()));
		return new RMItem(item, items.get(item).getAmount() - _items.get(item).getAmount());
	}
	
	public int getTotalLeft(){
		return getTeam().getGame().getItems().getItemsTotal() - getItemsTotal();
	}
}
