package com.mtaye.ResourceMadness;

import java.util.HashMap;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMFilterTemplate {
	private RM plugin;
	private int _id;
	private String _ownerName;
	private RMFilter _filter = new RMFilter();
	private RMStash _reward = new RMStash();
	private RMStash _tools = new RMStash();
	
	public RMFilterTemplate(RM plugin){
		this.plugin = plugin;
	}
	
	public RMFilterTemplate(RMFilterTemplate rmFilterTemplate, RM plugin){
		this.plugin = plugin;
		this._id = rmFilterTemplate._id;
		this._ownerName = rmFilterTemplate._ownerName;
		this._filter = rmFilterTemplate._filter;
	}
	
	//Get
	public int getId() { return _id; }
	public String getOwnerName() { return _ownerName; }
	public RMFilter getFilter() { return _filter; }
	public RMStash getReward() { return _reward; }
	public RMStash getTools() { return _tools; }
	
	//Set
	public void setId(int id){
		_id = id;
	}
	public void setOwnerName(String ownerName){
		_ownerName = ownerName;
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
	
	//////////
	//STATIC//
	//////////
	
	private static HashMap<Integer, RMFilterTemplate> _templates = new HashMap<Integer, RMFilterTemplate>();
	
	public static HashMap<Integer, RMFilterTemplate> getTemplates(){
		return _templates;
	}
	public static void setTemplates(HashMap<Integer, RMFilterTemplate> templates){
		_templates = templates;
	}
}