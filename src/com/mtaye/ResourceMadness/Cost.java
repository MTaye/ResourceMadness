package com.mtaye.ResourceMadness;

import java.util.HashMap;

public class Cost {
	
	public enum RMCostType{
	Create,
	Join,
	Quit
	}
	
	HashMap<RMCostType, Money> _map = new HashMap<RMCostType, Money>();
	
	private Cost(){
		for(RMCostType action : RMCostType.values()){
			_map.put(action, new Money());
		}
	}
	
	public HashMap<RMCostType, Money> getMap(){
		return _map;
	}
	
	public void setMap(HashMap<RMCostType, Money> map){
		_map = map;
	}
	
	public double get(RMCostType action){
		return _map.get(action).get();
	}
	
	public void set(RMCostType action, double amount){
		if(amount<0) amount = 0;
		_map.put(action, new Money(amount));
	}
	
	public double add(RMCostType action, double amount){
		double overflow = _map.get(action).get() + amount;
		if(overflow<0){
			_map.put(action, new Money());
			return -overflow;
		}
		else{
			_map.put(action, new Money(overflow));
			return 0;
		}
	}
	
	//ERROR
	public double sub(RMCostType action, double amount){
		double overflow = _map.get(action).get() - amount;
		if(overflow<0){
			_map.put(action, new Money());
			return -overflow;
		}
		else{
			_map.put(action, new Money(overflow));
			return 0;
		}
	}
	
	public void clear(RMCostType action){
		_map.put(action, new Money());
	}
}
