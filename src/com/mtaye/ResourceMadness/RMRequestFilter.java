package com.mtaye.ResourceMadness;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMGame.FilterState;
import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.ForceState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMRequestFilter {
	HashMap<Integer, RMItem> _items;
	ItemStack[] _itemsArray;
	FilterState _state;
	FilterType _type;
	ForceState _force;
	int _randomize;
	
	public RMRequestFilter(HashMap<Integer, RMItem> items, FilterState state, FilterType type, ForceState force, int randomize){
		_items = items;
		_state = state;
		_type = type;
		_force = force;
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
	public FilterType getType(){
		return _type;
	}
	public ForceState getForce(){
		return _force;
	}
	public int getRandomize(){
		return _randomize;
	}
}