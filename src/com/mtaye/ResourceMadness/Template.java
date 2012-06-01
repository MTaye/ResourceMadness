package com.mtaye.ResourceMadness;

import java.util.Arrays;

import com.mtaye.ResourceMadness.helper.InventoryHelper;
/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class Template {
	private String _name;
	private Filter _filter = new Filter();
	private Stash _reward = new Stash();
	private Stash _tools = new Stash();
	
	public Template(String name){
		_name = name;
	}
	
	public Template(String name, GameConfig config){
		_name = name;
		setFromConfig(config);
	}
	
	public Template(Template rmTemplate){
		this._name = rmTemplate._name;
		this._filter = rmTemplate._filter.clone();
		this._reward = rmTemplate._reward.clone();
		this._tools = rmTemplate._tools.clone();
	}
	
	//Get
	public String getName() { return _name; }

	public Filter getFilter() { return _filter; }
	public Stash getReward() { return _reward; }
	public Stash getTools() { return _tools; }
	public String getEncodeToStringFilter() { return Filter.encodeFilterToString(_filter.getItems(), true); }
	public String getEncodeToStringReward() { return InventoryHelper.encodeInventoryToString(_reward.getItemsArray()); }
	public String getEncodeToStringTools() { return InventoryHelper.encodeInventoryToString(_tools.getItemsArray()); }
	
	//Set
	public void setName(String name){
		_name = name;
	}
	public void setFilter(Filter filter){
		_filter = filter;
	}
	public void setReward(Stash reward){
		_reward = reward;
	}
	public void setTools(Stash tools){
		_tools = tools;
	}
	
	public void setFilterParseString(String str){
		_filter = new Filter(Filter.getRMItemsByStringArray(Arrays.asList(str), true));
	}
	public void setRewardParseString(String str){
		_reward = new Stash(InventoryHelper.getItemStackByString(str));
	}
	public void setToolsParseString(String str){
		_tools = new Stash(InventoryHelper.getItemStackByString(str));
	}
	
	public void setFromConfig(GameConfig config){
		_filter = config.getFilter().clone();
		_reward = config.getReward().clone();
		_tools = config.getTools().clone();
	}
	
	public boolean isEmpty(){
		if(_filter.size()+_reward.size()+_tools.size()==0) return true;
		return false;
	}
}