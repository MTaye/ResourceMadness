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
	private HashMap<Integer, Integer> _items = new HashMap<Integer, Integer>();
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
			newAmount = _items.get(id) + item.getAmount();
		}
		else newAmount = item.getAmount();
		
		HashMap<Integer, Integer> items = getTeam().getGame().getItems();
		if(items.containsKey(id)) overflow = items.get(id) - newAmount;

		if(overflow<0){
			overflow=-overflow;
			newAmount-=overflow;
		}
		else overflow = 0;

		_items.put(id, newAmount);
		return overflow;
	}
	
	public Integer getItemAmount(Material mat){
		if(_items.containsKey(mat)){
			return _items.get(mat);
		}
		return -1;
	}
	public HashMap<Integer, Integer> getItems(){
		return _items;//_itemsToFind;
	}
	public Integer getItemsTotal(){
		int total = 0;
		for(int amount : _items.values()){
			total+=amount;
		}
		return total;
	}
	public int getItemsLeftInt(){
		int itemsLeft = 0;
		HashMap<Integer, Integer> items = getTeam().getGame().getItems();
		for(Integer item : items.keySet()){
			int amount = items.get(item);
			if(_items.containsKey(item)) amount-= _items.get(item);
			if(amount>0) itemsLeft++;
		}
		return itemsLeft;
	}
	public HashMap<Integer, Integer> getItemsLeft(){
		HashMap<Integer, Integer> itemsLeft = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> items = getTeam().getGame().getItems();
		for(Integer item : items.keySet()){
			itemsLeft.put(item, items.get(item));
		}
		return itemsLeft;
	}
	
	public Integer getItemLeft(Integer item){
		HashMap<Integer, Integer> items = getTeam().getGame().getItems();
		return items.get(item) - _items.get(item);
	}
	
	public int getTotalLeft(){
		return getTeam().getGame().getItemsTotal() - getItemsTotal();
	}
}
