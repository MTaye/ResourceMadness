package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;
import com.mtaye.ResourceMadness.RM.ClaimType;
import com.mtaye.ResourceMadness.RMGame.FilterState;

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
		setEncodeFromFilter(config.getFilter());
		setEncodeFromReward(config.getReward());
		setEncodeFromTools(config.getTools());
	}
	
	public RMTemplate(RMTemplate rmTemplate){
		this._name = rmTemplate._name;
		this._filter = rmTemplate._filter;
		this._reward = rmTemplate._reward;
		this._tools = rmTemplate._tools;
	}
	
	//Get
	public String getName() { return _name; }

	public String getFilter() { return _filter; }
	public String getReward() { return _reward; }
	public String getTools() { return _tools; }
	public RMFilter getParseFilter() { return new RMFilter(RMFilter.getRMItemsByStringArray(Arrays.asList(_filter), false)); }
	public List<ItemStack> getParseReward() { return RMInventoryHelper.getItemStackByStringArray(_reward); }
	public List<ItemStack> getParseTools() { return RMInventoryHelper.getItemStackByStringArray(_tools); }
	public RMStash getStashReward() { return new RMStash(getParseReward()); }
	public RMStash getStashTools() { return new RMStash(getParseTools()); }
	
	//Set
	public void setName(String name){
		_name = name;
	}
	public void setFilter(String filter){
		_filter = filter;
	}
	public void setReward(String reward){
		_reward = reward;
	}
	public void setTools(String tools){
		_tools = tools;
	}
	
	public void setEncodeFromFilter(RMFilter filter){
		_filter = RMFilter.encodeFilterToString(filter.getItems(), false);
	}
	
	public void setEncodeFromReward(RMStash reward){
		_reward = RMInventoryHelper.encodeInventoryToString(reward.getItemsArray());
	}
	
	public void setEncodeFromTools(RMStash tools){
		_tools = RMInventoryHelper.encodeInventoryToString(tools.getItemsArray());
	}
	
	public void setEncodeFromConfig(RMGameConfig config){
		setEncodeFromFilter(config.getFilter());
		setEncodeFromReward(config.getReward());
		setEncodeFromTools(config.getTools());
	}
	
	public boolean isEmpty(){
		RMDebug.warning("Length:"+_filter.length()+_reward.length()+_tools.length());
		RMDebug.warning("Filter:"+_filter);
		RMDebug.warning("FilterLength:"+_filter.length());
		RMDebug.warning("Reward:"+_reward);
		RMDebug.warning("RewardLength:"+_reward.length());
		RMDebug.warning("Tools:"+_tools);
		RMDebug.warning("ToolsLength:"+_tools.length());
		if(_filter.length()+_reward.length()+_tools.length()==0) return true;
		return false;
	}
}