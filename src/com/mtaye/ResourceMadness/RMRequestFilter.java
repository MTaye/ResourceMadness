package com.mtaye.ResourceMadness;

import java.util.List;

import org.bukkit.Material;

import com.mtaye.ResourceMadness.RMGame.FilterType;

public class RMRequestFilter {
	Material[] _items;
	FilterType _type;
	Boolean _force;
	
	public RMRequestFilter(Material[] items, FilterType type, Boolean force){
		_items = items;
		_type = type;
		_force = force;
	}
	public RMRequestFilter(Material[] items, Boolean force){
		_items = items;
		_force = force;
	}
	public RMRequestFilter(FilterType type, Boolean force){
		_type = type;
		_force = force;
	}
	public RMRequestFilter(Material[] items){
		_items = items;
	}
	public RMRequestFilter(FilterType type){
		_type = type;
	}
	public RMRequestFilter(Boolean force){
		_force = force;
	}
	
	public Material[] getItems(){
		return _items;
	}
	public FilterType getType(){
		return _type;
	}
	public Boolean getForce(){
		return _force;
	}
}
