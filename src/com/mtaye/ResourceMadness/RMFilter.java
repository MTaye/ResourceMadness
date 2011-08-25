package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.mtaye.ResourceMadness.RMGame.ItemHandleState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMFilter {
	private HashMap<Integer, RMItem> _filter = new HashMap<Integer, RMItem>();
	private RMItem _lastItem; 
	
	public RMFilter(){
	}
	public RMFilter(HashMap<Integer, RMItem> items){
		_filter = items;
	}
	public RMFilter(RMFilter filter){
		_filter = filter.cloneItems();
	}
	
	public boolean containsKey(Integer key){
		return _filter.containsKey(key)?true:false;
	}
	public HashMap<Integer, RMItem> getItems(){
		return _filter;
	}
	public RMItem getItem(int index){
		return _filter.get(index);
	}
	public Set<Integer> keySet(){
		return _filter.keySet();
	}
	public int size(){
		return _filter.size();
	}
	public Collection<RMItem> values(){
		return _filter.values();
	}
	public void clearItems(){
		_filter.clear();
	}
	public RMItem getLastItem(){
		return _lastItem;
	}
	public int getItemsTotal(){
		int total = 0;
		for(RMItem rmItem : _filter.values()){
			total+=rmItem.getAmount();
		}
		return total;
	}
	public int getItemsTotalHigh(){
		int total = 0;
		for(RMItem rmItem : _filter.values()){
			total+=rmItem.getAmountHigh();
		}
		return total;
	}
	public RMFilter clone(){
		RMFilter clone = new RMFilter();
		for(RMItem rmItem : values()){
			clone.addItem(rmItem.getId(), new RMItem(rmItem.getId(), rmItem.getAmount(), rmItem.getAmountHigh(), rmItem.getMaxStackSize()), false);
		}
		return clone;
	}
	public HashMap<Integer, RMItem> cloneItems(){
		HashMap<Integer, RMItem> clone = new HashMap<Integer, RMItem>();
		for(RMItem rmItem : values()){
			clone.put(rmItem.getId(), new RMItem(rmItem.getId(), rmItem.getAmount(), rmItem.getAmountHigh(), rmItem.getMaxStackSize()));
		}
		return clone;
	}
	public HashMap<Integer, RMItem> cloneItems(int amount){
		HashMap<Integer, RMItem> clone = new HashMap<Integer, RMItem>();
		for(RMItem rmItem : values()){
			clone.put(rmItem.getId(), new RMItem(rmItem.getId(), amount, amount, rmItem.getMaxStackSize()));
		}
		return clone;
	}
	public RMFilter cloneRandomize(int randomize){
		RMFilter items = clone();
		if(items!=null) items.randomize(randomize);
		return items;
	}
	public void randomize(int randomize){
		if((randomize>0)&&(_filter.size()>randomize)){
			Integer[] arrayItems = _filter.keySet().toArray(new Integer[_filter.size()]);
			List<Integer> listItems = new ArrayList<Integer>();
			for(Integer i : arrayItems){
				listItems.add(i);
			}
			int size = listItems.size();
			while(size>randomize){
				int random = (int)Math.round((Math.random()*(size-1)));
				_filter.remove(listItems.get(random));
				listItems.remove(random);
				size--;
			}
		}
	}
	
	public void populateByFilter(RMFilter filter){
		_filter.clear();
		for(Integer item : filter.keySet()){
			RMItem rmItem = filter.getItem(item);
			int amount1 = rmItem.getAmount();
			int amount2 = rmItem.getAmountHigh();
			
			int val = amount1;
			if(amount2>0){
				val = Math.abs(amount1-amount2);
				val = (int)(Math.random()*val);
				val = amount1 + val;
				if(val<1) val = 1;
				addItem(item, new RMItem(item, val), false);
			}
			else{
				addItem(item, new RMItem(item, amount1), false);
			}
		}
	}
	
	//Simple Add/Remove
	public void addItem(int id, RMItem rmItem){
		if(!_filter.containsKey(id)){
			_filter.put(id, rmItem);
		}
	}
	public void removeItem(int id){
		if(_filter.containsKey(id)){
			_filter.remove(id);
		}
	}
	public Boolean addRemoveItem(int id, RMItem rmItem){
		if(_filter.containsKey(id)){
			_filter.remove(id);
			return false;
		}
		else{
			_filter.put(id, rmItem);
			return true;
		}
	}
	//Add/Remove
	public ItemHandleState addItem(Integer i, RMItem rmItem, boolean add){
		_lastItem = rmItem;
		if(_filter.containsKey(i)){
			if(add){
				rmItem.setAmount(rmItem.getAmount() + _filter.get(i).getAmount());
			}
			_filter.put(i, rmItem);
			return ItemHandleState.MODIFY;
		}
		_filter.put(i, rmItem);
		return ItemHandleState.ADD;
	}
	public ItemHandleState removeItem(Integer i, RMItem rmItem, boolean dec){
		if(_filter.containsKey(i)){
			int amount = _filter.get(i).getAmount();
			if(dec) amount-= rmItem.getAmount();
			if(amount>0){
				rmItem.setAmount(amount);
				_lastItem = rmItem;
				_filter.put(i, rmItem);
				return ItemHandleState.MODIFY;
			}
			else if(amount<=0){
				_lastItem = _filter.get(i);
				_filter.remove(i);
				return ItemHandleState.REMOVE;
			}
		}
		return ItemHandleState.NONE;
	}
	public ItemHandleState removeAlwaysItem(Integer i, RMItem rmItem){
		if(_filter.containsKey(i)){
			_lastItem = _filter.get(i);
			_filter.remove(i);
			return ItemHandleState.REMOVE;
		}
		return ItemHandleState.NONE;
	}
	public Boolean addRemoveItem(Integer i, RMItem rmItem){
		if(!_filter.containsKey(i)){
			_lastItem = rmItem;
			_filter.put(i, rmItem);
			return true;
		}
		else{
			_lastItem = rmItem;
			if(rmItem.getAmountHigh()<1){ ///////////////////////////////////////////////////////////////////////////////////////////
				if(rmItem.getAmount() != _filter.get(i).getAmount()){
					_filter.put(i, rmItem);
					return true;
				}
			}
			_lastItem = _filter.get(i);
			_filter.remove(i);
			return false;
		}
	}
}