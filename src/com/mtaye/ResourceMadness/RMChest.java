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
	private HashMap<Material, Integer> _items = new HashMap<Material, Integer>();
	private RMTeam _team;
	
	public RMChest(Chest chest){
		_chest = chest;
	}
	public RMChest(Chest chest, RMTeam team){
		_chest = chest;
		_team = team;
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
	
	public int addItem(ItemStack item){
		Material mat = item.getType();
		int overflow;
		int newAmount;
		if(_items.containsKey(mat)){
			newAmount = _items.get(mat) + item.getAmount();
		}
		else newAmount = item.getAmount();
		
		overflow = 0;//overflow = _game..get(mat) - newAmount;
		if(overflow<0){
			overflow=-overflow;
			newAmount-=overflow;
		}
		else overflow = 0;
		_items.put(mat, newAmount);
		return overflow;
	}
	
	public Integer getItemAmount(Material mat){
		if(_items.containsKey(mat)){
			return _items.get(mat);
		}
		return -1;
	}
	public HashMap<Material, Integer> getItems(){
		return _items;//_itemsToFind;
	}
	public Integer getItemsAmount(){
		int amount = 0;
		for(int i : _items.values()){
			amount+=i;
		}
		return amount;
	}
}
