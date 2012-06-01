package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.helper.Helper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class GameChest{
	private Chest _chest;
	private HashMap<Integer, Item> _items = new HashMap<Integer, Item>();
	private Team _team;
	private Stash _stash = new Stash();
	
	//Constructor
	public GameChest(Chest chest){
		_chest = chest;
	}
	public GameChest(Chest chest, Team team){
		_chest = chest;
		_team = team;
	}
	
	public Team getTeam(){
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
	
	public List<ItemStack> getContents(){
		if(_chest!=null){
			List<ItemStack> items = new ArrayList<ItemStack>();
			Inventory inv = _chest.getInventory();
			for(ItemStack item : inv.getContents()){
				if((item!=null)&&(item.getType()!=Material.AIR)){
					items.add(item);
				}
			}
			return items;
		}
		return null;
	}
	
	public ItemStack[] getContentsArray(){
		List<ItemStack> contents = getContents();
		return contents.toArray(new ItemStack[contents.size()]);
	}
	
	public void clearContents(){
		getChest().getInventory().clear();
	}
	
	public void clearContentsExceptHacked(){
		Inventory inv = getChest().getInventory();
		ItemStack[] contents = inv.getContents();
		for(int i=0; i<contents.length; i++){
			ItemStack item = contents[i];
			if(item!=null){
				if(!Helper.isMaterial(item.getType(), Game._hackMaterials)){
					inv.clear(i);
				}
			}
		}
	}
	
	public int addItem(ItemStack item){
		int id = item.getTypeId();
		int overflow = 0;
		int newAmount;
		if(_items.containsKey(id)){
			newAmount = _items.get(id).getAmount() + item.getAmount();
		}
		else newAmount = item.getAmount();
		
		HashMap<Integer, Item> items = getTeam().getGame().getGameConfig().getItems().getItems();
		if(items.containsKey(id)) overflow = items.get(id).getAmount() - newAmount;

		if(overflow<0){
			overflow=-overflow;
			newAmount-=overflow;
		}
		else overflow = 0;
		
		_items.put(id, new Item(id, newAmount));
		return overflow;
	}
	
	public int getItemAmount(Material mat){
		if(_items.containsKey(mat)){
			return _items.get(mat).getAmount();
		}
		return -1;
	}
	public HashMap<Integer, Item> getRMItems(){
		return _items;
	}
	public void setRMItems(HashMap<Integer, Item> items){
		_items = items;
	}
	public void clearItems(){
		_items.clear();
	}
	
	public List<ItemStack> getItems(){
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(Item rmItem : _items.values()){
			items.add(rmItem.getItem());
		}
		return items;
	}
	
	
	public int getItemsTotal(){
		int total = 0;
		for(Item rmItem : _items.values()){
			total+=rmItem.getAmount();
		}
		return total;
	}
	public int getItemsLeftInt(){
		int itemsLeft = 0;
		HashMap<Integer, Item> items = getTeam().getGame().getGameConfig().getItems().getItems();
		for(Integer item : items.keySet()){
			Item rmItem = items.get(item);
			int amount = rmItem.getAmount();
			if(_items.containsKey(item)) amount -= _items.get(item).getAmount();
			if(amount>0) itemsLeft++;
		}
		return itemsLeft;
	}
	public HashMap<Integer, Item> getItemsLeft(){
		HashMap<Integer, Item> itemsLeft = new HashMap<Integer, Item>();
		HashMap<Integer, Item> items = getTeam().getGame().getGameConfig().getItems().getItems();
		for(Integer item : items.keySet()){
			Item rmItem = items.get(item);
			int amount = rmItem.getAmount();
			if(_items.containsKey(item)) amount -= _items.get(item).getAmount();
			if(amount>0) itemsLeft.put(item, new Item(item, amount));
		}
		return itemsLeft;
	}
	
	public Item getItemLeft(Integer item){
		HashMap<Integer, Item> items = getTeam().getGame().getGameConfig().getItems().getItems();
		return new Item(item, items.get(item).getAmount() - _items.get(item).getAmount());
	}
	
	public int getTotalLeft(){
		return getTeam().getGame().getGameConfig().getItems().getItemsTotal() - getItemsTotal();
	}
	
	//Stash
	public Stash getStash(){
		return _stash;
	}
	
	public void setStash(Stash rmStash){
		_stash = rmStash;
	}
	
	public void clearStash(){
		_stash.clear();
	}

	public void addInventoryToStash(){
		Inventory inv =_chest.getInventory();
		_stash.addItems(inv.getContents());
		inv.clear();
	}
	
	public void returnInventoryFromStash(){
		if(_stash==null) return;
		if(_stash.size()>0){
			Inventory inv =_chest.getInventory();
			ItemStack[] items = _stash.getItemsArray();
			HashMap<Integer, ItemStack> returnedItems = new HashMap<Integer, ItemStack>();
			returnedItems = inv.addItem(items);
			_stash.clear();
			if(returnedItems.size()!=0){
				Game rmGame = getTeam().getGame();
				rmGame.getGameConfig().getFound().addItems(returnedItems.values().toArray(new ItemStack[returnedItems.values().size()]));
			}
		}
	}
}