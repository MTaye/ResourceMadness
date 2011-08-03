package com.mtaye.ResourceMadness;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;

import com.mtaye.ResourceMadness.RMGame.FilterType;
import com.mtaye.ResourceMadness.RMGame.ForceState;

public class RMRequestFilter {
	HashMap<Integer, RMItem> _items;
	FilterType _type;
	ForceState _force;
	int _randomize;
	
	public RMRequestFilter(HashMap<Integer, RMItem> items, FilterType type, ForceState force, int randomize){
		_items = items;
		_type = type;
		_force = force;
		_randomize = randomize;
	}
	
	public HashMap<Integer, RMItem> getItems(){
		return _items;
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
