package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.Helper.RMHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMChest{
	public RM plugin;
	private Chest _chest;
	private HashMap<Integer, RMItem> _items = new HashMap<Integer, RMItem>();
	private RMTeam _team;
	private RMStash _stash = new RMStash();
	
	//Constructor
	public RMChest(Chest chest, RM plugin){
		this.plugin = plugin;
		_chest = chest;
	}
	public RMChest(Chest chest, RMTeam team, RM plugin){
		this.plugin = plugin;
		_chest = chest;
		_team = team;
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
				if(!RMHelper.isMaterial(item.getType(), RMGame._hackMaterials)){
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
		
		HashMap<Integer, RMItem> items = getTeam().getGame().getConfig().getItems().getItems();
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
	public HashMap<Integer, RMItem> getRMItems(){
		return _items;
	}
	public void setRMItems(HashMap<Integer, RMItem> items){
		_items = items;
	}
	public void clearItems(){
		_items.clear();
	}
	
	public List<ItemStack> getItems(){
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(RMItem rmItem : _items.values()){
			items.add(rmItem.getItem());
		}
		return items;
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
		HashMap<Integer, RMItem> items = getTeam().getGame().getConfig().getItems().getItems();
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
		HashMap<Integer, RMItem> items = getTeam().getGame().getConfig().getItems().getItems();
		for(Integer item : items.keySet()){
			RMItem rmItem = items.get(item);
			int amount = rmItem.getAmount();
			if(_items.containsKey(item)) amount -= _items.get(item).getAmount();
			if(amount>0) itemsLeft.put(item, new RMItem(item, amount));
		}
		return itemsLeft;
	}
	
	public RMItem getItemLeft(Integer item){
		HashMap<Integer, RMItem> items = getTeam().getGame().getConfig().getItems().getItems();
		return new RMItem(item, items.get(item).getAmount() - _items.get(item).getAmount());
	}
	
	public int getTotalLeft(){
		return getTeam().getGame().getConfig().getItems().getItemsTotal() - getItemsTotal();
	}
	
	//Stash
	public RMStash getStash(){
		return _stash;
	}
	
	public void setStash(RMStash rmStash){
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
				RMGame rmGame = getTeam().getGame();
				rmGame.getConfig().getFound().addItems(returnedItems.values().toArray(new ItemStack[returnedItems.values().size()]));
				//RMPlayer rmp = rmGame.getConfig().getOwner();
				//rmp.getItems().addItems(returnedItems.values().toArray(new ItemStack[returnedItems.values().size()]), false);
			}
		}
	}
}