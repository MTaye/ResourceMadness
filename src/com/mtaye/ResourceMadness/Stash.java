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
import com.mtaye.ResourceMadness.Game.HandleState;
import com.mtaye.ResourceMadness.helper.TextHelper;

public class Stash implements Cloneable{
	private HashMap<Integer, Integer> _added = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> _modified = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> _removed = new HashMap<Integer, Integer>();
	private HashMap<Integer, StashItem> _items = new HashMap<Integer, StashItem>();
	boolean _modeInfinite = false;
	public int _lastAmount = 0;
	
	public Stash(){
	}
	
	public Stash(List<ItemStack> items){
		addItems(items);
	}
	
	public Stash(ItemStack item){
		addItem(item);
	}
	
	public Stash clone(){
		Stash clone = new Stash();
		List<ItemStack> items = getItems();
		for(ItemStack item : items){
			clone.addItem(item.clone());
		}
		return clone;
	}
	
	public void showChangedRelative(GamePlayer rmp){
		showAddedRelative(rmp);
		showModifiedRelative(rmp);
		showRemovedRelative(rmp);
		clearChanged();
	}
	
	public void showChanged(GamePlayer rmp){
		showAdded(rmp);
		showModified(rmp);
		showRemoved(rmp);
		clearChanged();
	}
	
	public void showAdded(GamePlayer rmp){
		HashMap<Integer, Integer> added = new HashMap<Integer, Integer>();
		for(Map.Entry<Integer, Integer> map : _added.entrySet()){
			int id = map.getKey();
			int amount = getAmountById(id);
			if(!added.containsKey(id)) added.put(id, amount);
			else added.put(id, map.getValue()+amount);
		}
		if(added.size()!=0) rmp.sendMessage(Text.getLabelArgs("common.added", getChangedString(added)));
		clearAdded();
	}
	public void showModified(GamePlayer rmp){
		HashMap<Integer, Integer> modified = new HashMap<Integer, Integer>();
		for(Map.Entry<Integer, Integer> map : _modified.entrySet()){
			int id = map.getKey();
			int amount = getAmountById(id);
			if(!modified.containsKey(id)) modified.put(id, amount);
			else modified.put(id, map.getValue()+amount);
		}
		if(modified.size()!=0) rmp.sendMessage(Text.getLabelArgs("common.modified", getChangedString(modified)));
		clearModified();
	}
	public void showRemoved(GamePlayer rmp){
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
		if(modified.size()!=0) rmp.sendMessage(Text.getLabelArgs("common.modified", getChangedString(modified)));
		if(removed.size()!=0) rmp.sendMessage(Text.getLabelArgs("common.removed", getChangedString(removed)));
		clearRemoved();
	}
	
	public void showAddedRelative(GamePlayer rmp){
		if(_added.size()!=0) rmp.sendMessage(Text.getLabelArgs("common.added", getChangedString(_added)));
	}
	public void showModifiedRelative(GamePlayer rmp){
		if(_modified.size()!=0) rmp.sendMessage(Text.getLabelArgs("common.modified", getChangedString(_modified)));
	}
	public void showRemovedRelative(GamePlayer rmp){
		if(_removed.size()!=0) rmp.sendMessage(Text.getLabelArgs("common.removed", getChangedString(_removed)));
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
		line = TextHelper.stripLast(line, ",");
		return line;
	}
	
	public String getChangedItemAmountString(int amount){
		if(amount!=1) return ChatColor.GRAY+":"+amount;
		return "";
	}
	
	//Items
	//Get Hash Items
	public HashMap<Integer, StashItem> getHash(){
		return _items;
	}
	//Set Hash Items
	public void setHash(HashMap<Integer, StashItem> items){
		_items = items;
	}
	//Clear Items
	public void clear(){
		_items.clear();
		clearChanged();
	}
	
	public StashItem get(int id){
		return _items.get(id);
	}
	
	public Collection<Integer> keySet(){
		return _items.keySet();
	}
	
	public Collection<StashItem> values(){
		return _items.values();
	}
	
	
	public List<StashItem> getStashItems(){
		List<StashItem> rmStashItems = new ArrayList<StashItem>();
		for(StashItem rmStashItem : _items.values()){
			rmStashItems.add(rmStashItem);
		}
		return rmStashItems;
	}
	
	public StashItem getStashItemById(int id){
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
		for(StashItem rmStashItem : _items.values()){
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
		List<Integer> match = new ArrayList<Integer>();
		Stash stash = new Stash(items);
		List<ItemStack> stashItems = stash.getItems();
		Iterator<ItemStack> iter = stashItems.iterator();
		while(iter.hasNext()){
			if(iter.next()==null) iter.remove();
		}
		for(ItemStack item : stashItems){
			int id = item.getTypeId();
			if(!match.contains(id)){
				match.add(id);
				_items.remove(id);
			}
		}
		for(ItemStack item : stashItems){
			int id = item.getTypeId();
			if(!match.contains(id)) addItemToChanged(_added, item);
			else{
				if(!_added.containsKey(item.getTypeId())) addItemToChanged(_modified, item);
				else addItemToChanged(_added, item);
			}
			if(!_items.containsKey(id)) _items.put(id, new StashItem(item));
			else{
				StashItem rmStashItem = _items.get(id);
				rmStashItem.addItem(item);
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
		for(StashItem item : _items.values()){
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
			_items.put(id, new StashItem(item));
			return HandleState.ADD;
		}
		else{
			StashItem rmStashItem = _items.get(id);
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
	public void addStashItem(StashItem rmStashItem){
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
	public void addStashItems(List<StashItem> rmStashItems){
		for(StashItem rmStashItem : rmStashItems){
			addStashItem(rmStashItem);
		}
	}
	//Add Stash Items
	public void addStashItems(StashItem[] rmStashItems){
		addStashItems(Arrays.asList(rmStashItems));
	}
	
	//Add Items By RMStash
	public void addItemsByRMStash(Stash rmStash){
		addStashItems(rmStash.getStashItems());
	}
	
	//Remove by Amount
	public List<ItemStack> removeByIdAmount(int id, int amount){
		List<ItemStack> items = new ArrayList<ItemStack>();
		if(_items.containsKey(id)){
			StashItem item = _items.get(id);
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
			StashItem rmStashItem = _items.get(id);
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
			StashItem rmStashItem = _items.get(id);
			if(byAmount) rmStashItem.removeItemByItemStackByAmount(item);
			else rmStashItem.removeItemByItemStack(item);
			if(rmStashItem.getAmount()==0) addItemToChanged(_removed, item);
			else addItemToChanged(_modified, item);
		}
	}
	
	public void removeRMStashItem(StashItem item){
		if(_items.values().contains(item)) _items.remove(item);
	}
	
	//Remove difference
	public void removeByStash(Stash stash){
		for(StashItem stashItem : stash.values()){
			removeByIdAmount(stashItem.getId(), stashItem.getAmount());
		}
	}
	
	public void transferFrom(Stash rmStash){
		transferFrom(rmStash, false);
	}
	
	public void transferFrom(Stash rmStash, boolean copy){
		if(rmStash==null) return;
		if(this==rmStash) return;
		if(rmStash.size()==0) return;
		
		addItemsByRMStash(rmStash.clone());
		if(!copy) rmStash.clear();
	}
	
	public void transferTo(Stash rmStash){
		transferTo(rmStash, false);
	}
	
	public void transferTo(Stash rmStash, boolean copy){
		if(rmStash==null) return;
		if(this==rmStash) return;
		if(size()==0) return;
		
		rmStash.addItemsByRMStash(this.clone());
		if(!copy) clear();
	}
	
	public void clearEmptyItems(){
		Iterator<StashItem> i = _items.values().iterator();
		while(i.hasNext()){
			StashItem item = i.next();
			if(item.getAmount()==0) i.remove();
		}
	}

	//Get IdData by ItemStack
	public String getIdDataByItemStack(ItemStack item){
		String idData = ""+item.getTypeId()+":"+item.getDurability();
		if(item.getData()!=null) idData += ":"+Byte.toString(item.getData().getData());
		return idData;
	}
	
	public void setItemsMatchInventory(Inventory inv, GamePlayer rmp, ClaimType claimType, HashMap<Integer, ItemStack> hashItems){
		if(inv==null) return;
		ItemStack[] contents = inv.getContents();
		List<ItemStack> claimItems = new ArrayList<ItemStack>();
		List<ItemStack> addItems = new ArrayList<ItemStack>();
		Stash stashClone = this.clone();
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
		if(claimItems.size()!=0) rmp.claimToInventory(this, inv, null, false, claimType, claimItems.toArray(new ItemStack[claimItems.size()]));
	}
	
	
	public String encodeToString(boolean invert){
		if(_items.size()==0){
			return "";
		}
		HashMap<Integer, List<Integer>> foundItems = new HashMap<Integer, List<Integer>>();
		for(Integer i : _items.keySet()){
			int amount = _items.get(i).getAmount();
			if(foundItems.containsKey(amount)){
				List<Integer> list = foundItems.get(amount);
				if(!list.contains(i)) list.add(i);
				foundItems.put(amount, list);
			}
			else{
				List<Integer> list = new ArrayList<Integer>();
				list.add(i);
				foundItems.put(amount, list);
			}
		}
	
		String line = "";
		for(Integer amount : foundItems.keySet()){
			if(line!=""){
				line = TextHelper.stripLast(line, ",");
				line+=" ";
			}
			if(invert) line += amount+":";
			List<Integer> listAmount = foundItems.get(amount);
			Integer[] array = listAmount.toArray(new Integer[listAmount.size()]);
			Arrays.sort(array);
			
			int firstItem = -1;
			int lastItem = -1;
			for(Integer item : array){
				if(firstItem==-1){
					firstItem = item;
					lastItem = item;
				}
				else{
					if(item-lastItem!=1){
						if(lastItem-firstItem>1){
							line += firstItem+"-"+lastItem+",";
						}
						else{
							if(firstItem!=lastItem) line += firstItem+","+lastItem+",";
							else line += firstItem+",";
						}
						firstItem = item;
						lastItem = item;
					}
					else lastItem = item;
				}
			}
			if(lastItem-firstItem>1) line += firstItem+"-"+lastItem+",";
			else{
				if(firstItem!=lastItem) line += firstItem+","+lastItem+",";
				else line += firstItem+",";
			}
			if(!invert){
				if(amount!=1) line += ":"+amount;
			}
		}
		line = TextHelper.stripLast(line,",");
		return line;
	}
}