package com.mtaye.ResourceMadness;

import java.util.HashMap;

public class RMCost {
	
	public enum RMCostType{
	Create,
	Join,
	Quit
	}
	
	HashMap<RMCostType, RMMoney> _map = new HashMap<RMCostType, RMMoney>();
	
	private RMCost(){
		for(RMCostType action : RMCostType.values()){
			_map.put(action, new RMMoney());
		}
	}
	
	public HashMap<RMCostType, RMMoney> getMap(){
		return _map;
	}
	
	public void setMap(HashMap<RMCostType, RMMoney> map){
		_map = map;
	}
	
	public double get(RMCostType action){
		return _map.get(action).get();
	}
	
	public void set(RMCostType action, double amount){
		if(amount<0) amount = 0;
		_map.put(action, new RMMoney(amount));
	}
	
	public double add(RMCostType action, double amount){
		double overflow = _map.get(action).get() + amount;
		if(overflow<0){
			_map.put(action, new RMMoney());
			return -overflow;
		}
		else{
			_map.put(action, new RMMoney(overflow));
			return 0;
		}
	}
	
	//ERROR
	public double sub(RMCostType action, double amount){
		double overflow = _map.get(action).get() - amount;
		if(overflow<0){
			_map.put(action, new RMMoney());
			return -overflow;
		}
		else{
			_map.put(action, new RMMoney(overflow));
			return 0;
		}
	}
	
	public void clear(RMCostType action){
		_map.put(action, new RMMoney());
	}
}
