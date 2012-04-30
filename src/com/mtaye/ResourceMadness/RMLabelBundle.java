package com.mtaye.ResourceMadness;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;

public class RMLabelBundle {
	private HashMap<String, String> _bundle = new HashMap<String, String>();
	private String strNull = "xXx";
		
	public RMLabelBundle(){
	}
	
	public int size(){
		return _bundle.size();
	}
	
	public Object get(Object key){
		return _bundle.get(key);
	}
	
	public Set<String> keySet(){
		return _bundle.keySet();
	}
	
	public Set<Entry<String, String>> entrySet(){
		return _bundle.entrySet();
	}
	
	public Collection<String> values(){
		return _bundle.values();
	}
	
	public boolean contains(String label){
		return _bundle.containsKey(label);
	}
	
	public boolean allowLabel(String label){
		for(ChatColor chatColor : ChatColor.values()){
			if(label.equalsIgnoreCase(chatColor.name())) return false;
		}
		if(label.startsWith("arg")) return false;
		return true;
	}
	
	//Set Labels
	public void setLabels(HashMap<String, String> labels){
		_bundle = labels;
	}

	public void putLabels(HashMap<String, String> labels){
		_bundle.putAll(labels);
	}
	
	//Get Labels
	public HashMap<String, String> getLabels(){
		return _bundle;
	}
	
	public HashMap<String, String> getLabels(String text){
		HashMap<String, String> labels = new HashMap<String, String>();
		for(String label : _bundle.keySet()){
			if(label.startsWith(text)) labels.put(label, _bundle.get(label));
		}
		return labels;
	}
	
	//Set
	public void set(String label, String text){
		if((label==null)||(text==null)) return;
		if(label.length()==0) return;
		_bundle.put(label, text);;
	}
	
	//Add
	public void add(String label, String text){
		if((label==null)||(text==null)) return;
		if(label.length()==0) return;
		if(_bundle.containsKey(label)) _bundle.put(label, _bundle.get(label)+text);
		else _bundle.put(label, text);
	}
	
	public void addLabel(String label, String text){
		if(!allowLabel(label)) return;
		//if(label.startsWith("filter.par")) text = text.toLowerCase();
		text = processText(text);
		add(label, text);
	}
	
	public void setLabel(String label, String text){
		if(!allowLabel(label)) return;
		//if(label.startsWith("filter.par")) text = text.toLowerCase();
		text = processText(text);
		set(label, text);
	}
	
	//Get
	public String getLabel(String label){
		String text = _bundle.get(label);
		if(text==null){
			RMDebug.warning("Label is null: "+label);
			if(RMDebug.isEnabled()) return strNull;
		}
		return text;
	}
	
	//Remove
	public void removeLabel(String label){
		if(_bundle.containsKey(label)) _bundle.remove(label);
	}
	
	public void removeLabel(String... labels){
		for(String label : labels){
			removeLabel(label);
		}
	}
	
	//Process
	public String processText(String text){
		text = text.replace("|", "\n");
		text = processColors(text);
		for(String strLabel : _bundle.keySet()){
			text = text.replace("%"+strLabel+"%", getLabel(strLabel));
		}
		return text;
	}
	
	public String processColors(String text){
		for(ChatColor color : ChatColor.values()){
			text = text.replace("%"+color.name().toLowerCase()+"%", ""+color);
			text = text.replace("%"+color.name().toUpperCase()+"%", ""+color);
		}
		return text;
	}
}