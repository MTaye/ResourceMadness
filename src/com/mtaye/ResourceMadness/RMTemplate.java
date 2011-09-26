package com.mtaye.ResourceMadness;

import java.util.Arrays;

import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;
/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMTemplate {
	private String _name;
	private RMFilter _filter = new RMFilter();
	private RMStash _reward = new RMStash();
	private RMStash _tools = new RMStash();
	
	public RMTemplate(String name){
		_name = name;
	}
	
	public RMTemplate(String name, RMGameConfig config){
		_name = name;
		setFromConfig(config);
	}
	
	public RMTemplate(RMTemplate rmTemplate){
		this._name = rmTemplate._name;
		this._filter = rmTemplate._filter.clone();
		this._reward = rmTemplate._reward.clone();
		this._tools = rmTemplate._tools.clone();
	}
	
	//Get
	public String getName() { return _name; }

	public RMFilter getFilter() { return _filter; }
	public RMStash getReward() { return _reward; }
	public RMStash getTools() { return _tools; }
	public String getEncodeToStringFilter() { return RMFilter.encodeFilterToString(_filter.getItems(), true); }
	public String getEncodeToStringReward() { return RMInventoryHelper.encodeInventoryToString(_reward.getItemsArray()); }
	public String getEncodeToStringTools() { return RMInventoryHelper.encodeInventoryToString(_tools.getItemsArray()); }
	
	//Set
	public void setName(String name){
		_name = name;
	}
	public void setFilter(RMFilter filter){
		_filter = filter;
	}
	public void setReward(RMStash reward){
		_reward = reward;
	}
	public void setTools(RMStash tools){
		_tools = tools;
	}
	
	public void setFilterParseString(String str){
		_filter = new RMFilter(RMFilter.getRMItemsByStringArray(Arrays.asList(str), true));
	}
	public void setRewardParseString(String str){
		_reward = new RMStash(RMInventoryHelper.getItemStackByStringArray(str));
	}
	public void setToolsParseString(String str){
		_tools = new RMStash(RMInventoryHelper.getItemStackByStringArray(str));
	}
	
	public void setFromConfig(RMGameConfig config){
		_filter = config.getFilter().clone();
		_reward = config.getReward().clone();
		_tools = config.getTools().clone();
	}
	
	public boolean isEmpty(){
		if(_filter.size()+_reward.size()+_tools.size()==0) return true;
		return false;
	}
}