package com.mtaye.ResourceMadness;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMGame.FilterState;
import com.mtaye.ResourceMadness.RMGame.FilterItemType;
import com.mtaye.ResourceMadness.RMGame.FilterType;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMRequestFilter {
	HashMap<Integer, RMItem> _items;
	ItemStack[] _itemsArray;
	FilterState _state;
	FilterItemType _itemType;
	FilterType _type;
	int _randomize;
	
	public RMRequestFilter(HashMap<Integer, RMItem> items, FilterState state, FilterItemType itemType, FilterType type, int randomize){
		_items = items;
		_state = state;
		_itemType = itemType;
		_type = type;
		_randomize = randomize;
	}
	
	public HashMap<Integer, RMItem> getItems(){
		return _items;
	}
	public ItemStack[] getItemStackArray(){
		return RMFilter.convertToItemStackArray(_items);
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
}