package com.mtaye.ResourceMadness;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.Game.FilterState;
import com.mtaye.ResourceMadness.Game.FilterItemType;
import com.mtaye.ResourceMadness.Game.FilterType;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RequestFilter {
	HashMap<Integer, Item> _items;
	FilterState _state;
	FilterItemType _itemType;
	FilterType _type;
	int _randomize;
	
	public RequestFilter(HashMap<Integer, Item> items, FilterState state, FilterItemType itemType, FilterType type, int randomize){
		_items = items;
		_state = state;
		_itemType = itemType;
		_type = type;
		_randomize = randomize;
	}
	
	public HashMap<Integer, Item> getItems(){
		return _items;
	}
	public ItemStack[] getItemStackArray(){
		return Filter.convertToItemStackArray(_items);
	}
	public FilterState getFilterState(){
		return _state;
	}
	public FilterItemType getItemType(){
		return _itemType;
	}
	public FilterType getFilterType(){
		return _type;
	}
	public int getRandomize(){
		return _randomize;
	}
	
	public RequestFilter clone(){
		HashMap<Integer, Item> map = new HashMap<Integer, Item>();
		if(_items!=null){
			for(Integer i : _items.keySet()){
				map.put(i, _items.get(i));
			}
		}
		return new RequestFilter(map, _state, _itemType, _type, _randomize);
	}
}