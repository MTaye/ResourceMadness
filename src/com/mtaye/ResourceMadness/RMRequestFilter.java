package com.mtaye.ResourceMadness;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;

import com.mtaye.ResourceMadness.RMGame.FilterType;

public class RMRequestFilter {
	HashMap<Integer, Integer[]> _items;
	FilterType _type;
	Boolean _force;
	
	public RMRequestFilter(HashMap<Integer, Integer[]> items, FilterType type, Boolean force){
		_items = items;
		_type = type;
		_force = force;
	}
	public RMRequestFilter(HashMap<Integer, Integer[]> items, Boolean force){
		_items = items;
		_force = force;
	}
	public RMRequestFilter(FilterType type, Boolean force){
		_type = type;
		_force = force;
	}
	public RMRequestFilter(HashMap<Integer, Integer[]> items){
		_items = items;
	}
	public RMRequestFilter(FilterType type){
		_type = type;
	}
	public RMRequestFilter(Boolean force){
		_force = force;
	}
	
	public HashMap<Integer, Integer[]> getItems(){
		return _items;
	}
	public FilterType getType(){
		return _type;
	}
	public Boolean getForce(){
		return _force;
	}
}
