package com.mtaye.ResourceMadness;

import java.util.Arrays;

import com.mtaye.ResourceMadness.helper.InventoryHelper;
/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class GameTemplate {
	private String _name;
	private Filter _filter = new Filter();
	private Stash _reward = new Stash();
	private Stash _tools = new Stash();
	
	public GameTemplate(String name){
		_name = name;
	}
	
	public GameTemplate(String name, GameConfig config){
		_name = name;
		setFromConfig(config);
	}
	
	public GameTemplate(GameTemplate rmTemplate){
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
		if((str==null)||(str.length()==0)) return;
		_filter = new Filter(Filter.getRMItemsByStringArray(Arrays.asList(str), true));
	}
	public void setRewardParseString(String str){
		if((str==null)||(str.length()==0)) return;
		_reward = new Stash(InventoryHelper.getItemStackByString(str));
	}
	public void setToolsParseString(String str){
		if((str==null)||(str.length()==0)) return;
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
	
	public GameTemplate clone(){
		try {
			return (GameTemplate)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}