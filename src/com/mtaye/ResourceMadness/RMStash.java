package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMGame.HandleState;

public class RMStash {
	public static RM plugin;
	private HashMap<Integer, Integer> _added = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> _modified = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> _removed = new HashMap<Integer, Integer>();
	private HashMap<Integer, RMStashItem> _items = new HashMap<Integer, RMStashItem>();
	boolean _modeInfinite = false;
	public int _lastAmount = 0;
	
	public RMStash(){
	}
	
	public RMStash(List<ItemStack> items){
		addItems(items);
	}
	
	public RMStash(ItemStack item){
		addItem(item);
	}
	
	public RMStash clone(){
		RMStash clone = new RMStash();
		List<ItemStack> items = getItems();
		for(ItemStack item : items){
			clone.addItem(item.clone());
		}
		return clone;
	}
	
	public void showChangedRelative(RMPlayer rmp){
		showAddedRelative(rmp);
		showModifiedRelative(rmp);
		showRemovedRelative(rmp);
		clearChanged();
	}
	
	public void showChanged(RMPlayer rmp){
		showAdded(rmp);
		showModified(rmp);
		showRemoved(rmp);
		clearChanged();
	}
	
	public void showAdded(RMPlayer rmp){
		HashMap<Integer, Integer> added = new HashMap<Integer, Integer>();
		for(Map.Entry<Integer, Integer> map : _added.entrySet()){
			int id = map.getKey();
			int amount = getAmountById(id);
			if(!added.containsKey(id)) added.put(id, amount);
			else added.put(id, map.getValue()+amount);
		}
		if(added.size()!=0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+getChangedString(added));
		clearAdded();
	}
	public void showModified(RMPlayer rmp){
		HashMap<Integer, Integer> modified = new HashMap<Integer, Integer>();
		for(Map.Entry<Integer, Integer> map : _modified.entrySet()){
			int id = map.getKey();
			int amount = getAmountById(id);
			if(!modified.containsKey(id)) modified.put(id, amount);
			else modified.put(id, map.getValue()+amount);
		}
		if(modified.size()!=0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+getChangedString(modified));
		clearModified();
	}
	public void showRemoved(RMPlayer rmp){
		HashMap<Integer, Integer> modified = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> removed = new HashMap<Integer, Integer>();
		for(Map.Entry<Integer, Integer> map : _removed.entrySet()){
			int id = map.getKey();
			int amount = getAmountById(id);
			if(amount!=0){
				if(!modified.containsKey(id)) modified.put(id, amount);
				else modified.put(id, modified.get(id)+amount);
			}
			else{
				if(!removed.containsKey(id)) if(_removed.containsKey(id)) removed.put(id, _removed.get(id));
				else if(_removed.containsKey(id)) removed.put(id, removed.get(id)+_removed.get(id));
			}
		}
		if(modified.size()!=0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+getChangedString(modified));
		if(removed.size()!=0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+getChangedString(removed));
		clearRemoved();
	}
	
	public void showAddedRelative(RMPlayer rmp){
		if(_added.size()!=0) rmp.sendMessage(ChatColor.YELLOW+"Added: "+getChangedString(_added));
	}
	public void showModifiedRelative(RMPlayer rmp){
		if(_modified.size()!=0) rmp.sendMessage(ChatColor.YELLOW+"Modified: "+getChangedString(_modified));
	}
	public void showRemovedRelative(RMPlayer rmp){
		if(_removed.size()!=0) rmp.sendMessage(ChatColor.GRAY+"Removed: "+getChangedString(_removed));
	}
	
	public void addItemToChanged(HashMap<Integer, Integer> changed, int id, int newAmount){
		if(changed.containsKey(id)){
			int amount = changed.get(id);
			amount += newAmount;
		}
		else changed.put(id, newAmount);
	}
	
	public void addItemToChanged(HashMap<Integer, Integer> changed, ItemStack item){
		addItemToChanged(changed, item.getTypeId(), item.getAmount());
	}
	
	public void addItemsToChanged(HashMap<Integer, Integer> changed, List<ItemStack> items){
		for(ItemStack item : items){
			addItemToChanged(changed, item);
		}
	}
	
	public void addItemsToChanged(HashMap<Integer, Integer> changed, HashMap<Integer, ItemStack> items){
		for(Map.Entry<Integer, ItemStack> map : items.entrySet()){
			ItemStack item = map.getValue();
			addItemToChanged(changed, item);
		}
	}
	public void addChangedToChanged(HashMap<Integer, Integer> changed, HashMap<Integer, Integer> items){
		for(Map.Entry<Integer, Integer> map : items.entrySet()){
			addItemToChanged(changed, map.getKey(), map.getValue());
		}
	}
	
	public void clearChanged(){
		_added.clear();
		_modified.clear();
		_removed.clear();
	}
	public HashMap<Integer, Integer> getAdded(){
		return _added;
	}
	public HashMap<Integer, Integer> getModified(){
		return _modified;
	}
	public HashMap<Integer, Integer> getRemoved(){
		return _removed;
	}
	public void clearAdded(){
		_added.clear();
	}
	public void clearModified(){
		_modified.clear();
	}
	public void clearRemoved(){
		_removed.clear();
	}
	
	public String getChangedString(HashMap<Integer, Integer> items){
		String line = "";
		Integer[] array = items.keySet().toArray(new Integer[items.size()]);
		Arrays.sort(array);
		for(Integer id : array){
			line+=ChatColor.WHITE+Material.getMaterial(id).name()+getChangedItemAmountString(items.get(id))+ChatColor.WHITE+", ";
		}
		line = RMText.stripLast(line, ",");
		return line;
	}
	
	public String getChangedItemAmountString(int amount){
		if(amount!=1) return ChatColor.GRAY+":"+amount;
		return "";
	}
	
	//Items
	//Get Hash Items
	public HashMap<Integer, RMStashItem> getHash(){
		return _items;
	}
	//Set Hash Items
	public void setHash(HashMap<Integer, RMStashItem> items){
		_items = items;
	}
	//Clear Items
	public void clear(){
		_items.clear();
		clearChanged();
	}
	
	public RMStashItem get(int id){
		return _items.get(id);
	}
	
	public Collection<Integer> keySet(){
		return _items.keySet();
	}
	
	public Collection<RMStashItem> values(){
		return _items.values();
	}
	
	
	public List<RMStashItem> getStashItems(){
		List<RMStashItem> rmStashItems = new ArrayList<RMStashItem>();
		for(RMStashItem rmStashItem : _items.values()){
			rmStashItems.add(rmStashItem);
		}
		return rmStashItems;
	}
	
	public RMStashItem getStashItemById(int id){
		if(_items.containsKey(id)){
			return _items.get(id);
		}
		return null;
	}
	
	//Get Items by IdAmount
	public List<ItemStack> getItemsByIdAmount(int id, int amount){
		List<ItemStack> items = new ArrayList<ItemStack>();
		if(_items.containsKey(id)){
			return _items.get(id).getItemByAmount(amount);
		}
		return items;
	}
	
	//Get Item by Id
	public ItemStack getItemById(int id){
		if(_items.containsKey(id)){
			return _items.get(id).getItem();
		}
		return null;
	}
	
	//Get Items by Id
	public List<ItemStack> getItemsById(int id){
		List<ItemStack> items = new ArrayList<ItemStack>();
		if(_items.containsKey(id)){
			items.addAll(_items.get(id).getItems());
		}
		return items;
	}
	
	//Get Items
	public List<ItemStack> getItems(){
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(RMStashItem rmStashItem : _items.values()){
			items.addAll(rmStashItem.getItems());
		}
		return items;
	}
	
	//Get Items Array
	public ItemStack[] getItemsArray(){
		List<ItemStack> items = getItems();
		return items.toArray(new ItemStack[items.size()]);
	}
	//Get Items Array
	public ItemStack[] getItemsArray(int size){
		List<ItemStack> items = getItems();
		return items.toArray(new ItemStack[size]);
	}
	
	public void setItems(List<ItemStack> items){
		RMStash stash = new RMStash(items);
		List<ItemStack> stashItems = stash.getItems();
		for(ItemStack item : stashItems){
			if(item==null) continue;
			int id = item.getTypeId();
			if(!_items.containsKey(id)){
				addItemToChanged(_added, item);
				_items.put(id, new RMStashItem(item));
			}
			else{
				if(!_added.containsKey(item.getTypeId())) addItemToChanged(_modified, item);
				else addItemToChanged(_added, item);
				RMStashItem rmStashItem = _items.get(id);
				rmStashItem.setItem(item);
				if(rmStashItem.getAmount()==0) removeRMStashItem(rmStashItem);
			}
		}
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
	public int size(){
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
	public HandleState addItem(ItemStack item){
		if((item == null)||(item.getType()==Material.AIR)) return HandleState.NO_CHANGE;
		
		int id = item.getTypeId();
		if(!_items.containsKey(id)){
			addItemToChanged(_added, item);
			_items.put(id, new RMStashItem(item));
			return HandleState.ADD;
		}
		else{
			RMStashItem rmStashItem = _items.get(id);
			if(!_added.containsKey(item.getTypeId())) addItemToChanged(_modified, item);
			else addItemToChanged(_added, item);
			return rmStashItem.addItem(item);
		}
	}
	
	//Add Items
	public void addItems(List<ItemStack> items){
		for(ItemStack item : items){
			if((item == null)||(item.getType()==Material.AIR)) continue;
			addItem(item);
		}
	}
	//Add Items
	public void addItems(ItemStack[] items){
		addItems(Arrays.asList(items));
	}
	
	//Add Stash Item
	public void addStashItem(RMStashItem rmStashItem){
		int id = rmStashItem.getId();
		if(!_items.containsKey(id)){
			addItemToChanged(_added, rmStashItem.getItem());
			_items.put(id, rmStashItem.clone());
		}
		else{
			addItemToChanged(_modified, rmStashItem.getItem());
			_items.get(id).addItems(rmStashItem.getItems());
		}
	}
	
	//Add Stash Items
	public void addStashItems(List<RMStashItem> rmStashItems){
		for(RMStashItem rmStashItem : rmStashItems){
			addStashItem(rmStashItem);
		}
	}
	//Add Stash Items
	public void addStashItems(RMStashItem[] rmStashItems){
		addStashItems(Arrays.asList(rmStashItems));
	}
	
	//Add Items By RMStash
	public void addItemsByRMStash(RMStash rmStash){
		addStashItems(rmStash.getStashItems());
	}
	
	//Remove by Amount
	public List<ItemStack> removeByIdAmount(int id, int amount){
		List<ItemStack> items = new ArrayList<ItemStack>();
		if(_items.containsKey(id)){
			RMStashItem item = _items.get(id);
			items = item.removeByAmount(amount);
			if(item.getAmount()==0) _items.remove(id);
		}
		return items;
	}
	
	//Remove by item
	public List<ItemStack> removeByItem(ItemStack item){
		List<ItemStack> items = new ArrayList<ItemStack>();
		if((item==null)||(item.getType()==Material.AIR)) return items;
		int id = item.getTypeId();
		if(_items.containsKey(id)){
			RMStashItem rmStashItem = _items.get(id);
			items = rmStashItem.removeByAmount(item.getAmount());
			if(rmStashItem.getAmount()==0){
				addItemsToChanged(_removed, items);
				_items.remove(id);
			}
			else addItemsToChanged(_modified, items);
		}
		return items;
	}
	
	//Remove Item
	public List<ItemStack> removeItemById(int id){
		List<ItemStack> items = new ArrayList<ItemStack>();
		if(_items.containsKey(id)){
			items = _items.get(id).getItems();
			addItemsToChanged(_removed,items);
			_items.remove(id);
		}
		return items;
	}
	
	public void removeOneItemById(int id){
		if(_items.containsKey(id)){
			ItemStack item = _items.get(id).getItem();
			addItemToChanged(_removed,item);
			_items.remove(id);
		}
	}
	
	public void removeItems(List<ItemStack> items){
		for(ItemStack item : items){
			if((item==null)||(item.getType()==Material.AIR)) continue;
			removeByItem(item);
		}
	}
	
	public void removeItems(ItemStack[] items){
		removeItems(Arrays.asList(items));
	}
	
	public List<ItemStack> removeItemsWhole(List<ItemStack> items){
		List<ItemStack> returnItems = new ArrayList<ItemStack>();
		for(ItemStack item : items){
			if((item==null)||(item.getType()==Material.AIR)) continue;
			int id = item.getTypeId();
			if(_items.containsKey(id)){
				returnItems.addAll(getItemsById(id));
				_items.remove(id);
			}
		}
		addItemsToChanged(_removed, returnItems);
		return returnItems;
	}
	
	public List<ItemStack> removeItemsWhole(ItemStack[] items){
		return removeItemsWhole(Arrays.asList(items));
	}
	
	//Remove Item
	public void removeItemsByIdSilent(List<Integer> items){
		for(Integer id : items){
			if(_items.containsKey(id)){
				_items.remove(id);
			}
		}
	}
	
	//Remove Item Precise
	public void removeItemByItemStack(ItemStack item, boolean byAmount){
		int id = item.getTypeId();
		if(_items.containsKey(id)){
			RMStashItem rmStashItem = _items.get(id);
			if(byAmount) rmStashItem.removeItemByItemStackByAmount(item);
			else rmStashItem.removeItemByItemStack(item);
			if(rmStashItem.getAmount()==0) addItemToChanged(_removed, item);
			else addItemToChanged(_modified, item);
		}
	}
	
	public void removeRMStashItem(RMStashItem item){
		if(_items.values().contains(item)) _items.remove(item);
	}
	
	//Remove difference
	public void removeByStash(RMStash stash){
		for(RMStashItem stashItem : stash.values()){
			removeByIdAmount(stashItem.getId(), stashItem.getAmount());
		}
	}
	
	public void transferFrom(RMStash rmStash){
		if(rmStash==null) return;
		if(this==rmStash) return;
		if(rmStash.size()==0) return;
		
		addItemsByRMStash(rmStash.clone());
		rmStash.clear();
	}
	
	public void transferTo(RMStash rmStash){
		if(rmStash==null) return;
		if(this==rmStash) return;
		if(size()==0) return;
		
		rmStash.addItemsByRMStash(this.clone());
		clear();
	}
	
	public void clearEmptyItems(){
		Iterator<RMStashItem> i = _items.values().iterator();
		while(i.hasNext()){
			RMStashItem item = i.next();
			if(item.getAmount()==0) i.remove();
		}
	}

	//Get IdData by ItemStack
	public String getIdDataByItemStack(ItemStack item){
		String idData = ""+item.getTypeId()+":"+item.getDurability();
		if(item.getData()!=null) idData += ":"+Byte.toString(item.getData().getData());
		return idData;
	}
	
	public void setItemsMatchInventory(Inventory inv, RMPlayer rmp, ClaimType claimType, HashMap<Integer, ItemStack> hashItems){
		if(inv==null) return;
		ItemStack[] contents = inv.getContents();
		List<ItemStack> claimItems = new ArrayList<ItemStack>();
		List<ItemStack> addItems = new ArrayList<ItemStack>();
		RMStash stashClone = this.clone();
		for(int i=0; i<contents.length; i++){
			ItemStack invItem = contents[i];
			if((invItem!=null)&&(invItem.getType()!=Material.AIR)){
				int id = invItem.getTypeId();
				if(hashItems.containsKey(id)){
					//begin
					ItemStack hashItem = hashItems.get(id);
					int hashAmount = hashItem.getAmount();
					int stashAmount = stashClone.getAmountById(id);
					int overflow = hashAmount - stashAmount;
					if(overflow==0) continue;
					if(overflow<0) claimItems.addAll(stashClone.removeByIdAmount(id, -overflow));
					else{
						int overflowAmount = overflow;
						overflow = overflowAmount - invItem.getAmount(); //STASH
						ItemStack itemClone = invItem.clone();
						itemClone.setAmount(invItem.getAmount()+(overflow<0?overflow:0));
						stashClone.addItem(itemClone);
						addItems.add(itemClone);
						if(overflow>=0) inv.clear(i);
						else invItem.setAmount(-overflow);
					}
				}
			}
		}
		if(addItems.size()!=0) addItems(addItems);
		if(claimItems.size()!=0) rmp.claimToInventory(this, inv, false, false, claimType, claimItems.toArray(new ItemStack[claimItems.size()]));
	}
}