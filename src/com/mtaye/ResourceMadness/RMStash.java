package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMGame.HandleState;

public class RMStash {
	public RM plugin;
	private HashMap<Integer, RMStashItem> _items = new HashMap<Integer, RMStashItem>();
	boolean _modeInfinite = false;
	
	public RMStash(RM plugin){
		this.plugin = plugin;
	}
	
	//Items
	public HashMap<Integer, RMStashItem> getItems(){
		return _items;
	}
	public void setItems(HashMap<Integer, RMStashItem> items){
		_items = items;
	}
	public void clearItems(){
		_items.clear();
	}
	
	//ModeInfinite
	public boolean getMode(){
		return _modeInfinite;
	}
	public void setMode(boolean mode){
		_modeInfinite = mode;
	}
	public void toggleMode(){
		if(_modeInfinite) _modeInfinite = false;
		else _modeInfinite = true;
	}
	
	//Get Size
	public int getSize(){
		return _items.size();
	}
	
	//Get Amount
	public int getAmount(){
		int amount = 0;
		for(RMStashItem item : _items.values()){
			amount += item.getAmount();
		}
		return amount;
	}
	
	//Get Amount by Id
	public int getAmountById(int id){
		int amount = 0;
		if(_items.containsKey(id)){
			amount = _items.get(id).getAmount();
		}
		return amount;
	}
	
	//Get Amount Item
	public int getAmountByItemStack(ItemStack item){
		int amount = 0;
		if(item == null) return amount;
		if(_items.containsKey(item.getTypeId())){
			amount = _items.get(item).getAmountByItemStack(item);
		}
		return amount;
	}
	
	//Add Item
	public void addItem(ItemStack item){
		if((item == null)||(item.getType()==Material.AIR)) return;
		int id = item.getTypeId();
		if(!_items.containsKey(id)){
			_items.put(id, new RMStashItem(item));
		}
		else{
			_items.get(id).addItem(item);
		}
	}
	
	//Add Items
	public void addItems(List<ItemStack> items){
		for(ItemStack item : items){
			addItem(item);
		}
	}
	
	//Remove Item
	public void removeItemById(int id){
		if(_items.containsKey(id)){
			_items.remove(id);
		}
	}
	
	//Remove Item Precise
	public void removeItemByItemStack(ItemStack item, boolean byAmount){
		int id = item.getTypeId();
		if(_items.containsKey(id)){
			if(byAmount) _items.get(id).removeItemByItemStackByAmount(item);
			else _items.get(id).removeItemByItemStack(item);
		}
	}
	
	//Claim
	public HandleState claim(RMPlayer rmp, ClaimType claimType, RMChest rmChest){
		if(rmp.getPlayer()==null) return HandleState.NO_CHANGE;
		if(_items.size()==0){
			switch(claimType){
				case FOUND:	rmp.sendMessage("No found items to give."); break;
				case ITEMS:	rmp.sendMessage("No items to return."); break;
				case REWARD: rmp.sendMessage("No reward to give."); break;
				case TOOLS:	rmp.sendMessage("No tools to give."); break;
			}
			return HandleState.NO_CHANGE;
		}
		Inventory inv = rmp.getPlayer().getInventory();
		if(inv.firstEmpty()==-1){
			switch(claimType){
				case FOUND:	rmp.sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give found items."); break;
				case ITEMS:	rmp.sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot return items."); break;
				case REWARD: rmp.sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give reward."); break;
				case TOOLS:	rmp.sendMessage(ChatColor.RED+"Your inventory is full. "+ChatColor.WHITE+"Cannot give tools."); break;
			}
			return HandleState.NO_CHANGE;
		}
		
		List<Integer> removeItems = new ArrayList<Integer>();
		for(Integer id : _items.keySet()){
			if(inv.firstEmpty()!=-1){
				RMStashItem rmStashItem = _items.get(id);
				while(rmStashItem.getSize()>0){
					ItemStack item = rmStashItem.getItem();
					if((item!=null)&&(item.getType()!=Material.AIR)){
						Material mat = item.getType();
						while(item.getAmount()>mat.getMaxStackSize()){
							if(inv.firstEmpty()!=-1){
								ItemStack itemClone = item.clone();
								itemClone.setAmount(mat.getMaxStackSize());
								inv.addItem(itemClone);
								item.setAmount(item.getAmount()-mat.getMaxStackSize());
							}
							else break;
						}
						if(item.getAmount()<=mat.getMaxStackSize()){
							inv.addItem(item);
							rmStashItem.removeItemByItemStack(item);
						}
					}
				}
				if(rmStashItem.getSize()==0) removeItems.add(id);
			}
			else break;
		}
		for(Integer removeItem : removeItems){
			_items.remove(removeItem);
		}
		
		//inv.clear();
		//clearInventoryContents();
		if(_items.size()>0){
			rmp.sendMessage(ChatColor.RED+"Your Inventory is full. "+ChatColor.YELLOW+getAmount()+ChatColor.WHITE+" item(s) remaining.");
			return HandleState.CLAIM_RETURNED_SOME;
		}
		else{
			switch(claimType){
				case FOUND:	rmp.sendMessage(ChatColor.YELLOW+"Found items were given. "+ChatColor.WHITE+"Check your inventory."); break;
				case ITEMS:	rmp.sendMessage(ChatColor.YELLOW+"All items were returned. "+ChatColor.WHITE+"Check your inventory."); break;
				case REWARD: rmp.sendMessage(ChatColor.YELLOW+"Reward was given. "+ChatColor.WHITE+"Check your inventory."); break;
				case TOOLS:	rmp.sendMessage(ChatColor.YELLOW+"Tools were given. "+ChatColor.WHITE+"Check your inventory."); break;
			}
			return HandleState.CLAIM_RETURNED_ALL;
		}
	}
}