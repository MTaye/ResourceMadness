package com.mtaye.ResourceMadness;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.RMCommands.RMCommand;
import com.mtaye.ResourceMadness.RMGame.GameState;
import com.mtaye.ResourceMadness.RMGame.InterfaceState;
import com.mtaye.ResourceMadness.Helper.RMHelper;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMText {
	public static RM plugin;
	public static String preLog = "ResourceMadness: ";

	public static RMLabelBundle _labels = new RMLabelBundle();
	
	public RMText(){
	}
	
	public static String getLabelByGameState(GameState state){
		switch(state){
		case SETUP: return getLabel("game.setup").toUpperCase();
		case COUNTDOWN: return getLabel("game.countdown").toUpperCase();
		case GAMEPLAY: return getLabel("game.gameplay").toUpperCase();
		case GAMEOVER: return getLabel("game.gameover").toUpperCase();
		case PAUSED: return getLabel("game.paused").toUpperCase();
		default: return "";
		}
	}
	
	public static String getLabelByInterfaceState(InterfaceState state){
		switch(state){
		case FILTER: case FILTER_CLEAR: return getLabel("cmd.filter").toUpperCase();
		case REWARD: case REWARD_CLEAR: return getLabel("cmd.reward").toUpperCase();
		case TOOLS: case TOOLS_CLEAR: return getLabel("cmd.tools").toUpperCase();
		default: return "";
		}
	}
	
	public static RMLabelBundle getLabelBundle() { return _labels; }
	public static void setLabelBundle(RMLabelBundle labels){
		_labels = labels;
	}
	
	public static void setLabelBundleMissing(RMLabelBundle labels){
		for(Map.Entry<String, String> map: labels.entrySet()){
			String label = map.getKey();
			if(!_labels.contains(label)) _labels.addLabel(label, map.getValue());
		}
	}
	
	public static String getLabel(String label){
		return _labels.getLabel(label);
	}
	
	public static void addLabel(String label, String text){
		_labels.addLabel(label, text);
	}
	
	public static void setLabel(String label, String text){
		_labels.setLabel(label, text);
	}
	
	public static boolean containsLabel(String label){
		return _labels.contains(label);
	}
	
	public static void debugLabels(){
		for(Map.Entry<String, String> map : _labels.entrySet()){
			RMDebug.warning("L: "+map.getKey());
			RMDebug.warning("T: "+map.getValue());
		}
	}
	
	public static String getLabelArgs(String label, String... args){
		String text = getLabel(label);
		if((args==null)||(args.length==0)) return text;
		HashMap<Integer, String> argMap = new HashMap<Integer, String>();
		if(text.contains("%arg%")){
			text = text.replace("%arg%", args[0]);
			if(args.length==1) return text;
			else args = Arrays.copyOfRange(args, 1, args.length);
		}
		if(text.contains("%arg")){
			int pos = text.indexOf("%arg", 0);
			int posEnd;
			while(pos!=-1){
				posEnd = text.indexOf("%", pos+1);
				if(posEnd!=-1){
					if(pos+4<posEnd){
						int num = RMHelper.getIntByString(text.substring(pos+4, posEnd));
						if(num>-1) argMap.put(num, "%arg"+num+"%");
					}
				}
				pos = text.indexOf("%arg", posEnd+1);
			}
			Integer[] argArray = argMap.keySet().toArray(new Integer[argMap.size()]);
			Arrays.sort(argArray);
			for(int i=0; i<args.length; i++){
				if(i==argArray.length) break;
				text = text.replace(argMap.get(argArray[i]), args[i]);
			}
		}
		return text;
	}
	
	//Permission
	public static String getLabelPermission(String label, RMPlayer rmp, String node){
		return getLabelArgs(label, (rmp.hasPermission(node)?getLabel("cmd.filter.info"):""));
	}
	
	//Colorized
	public static String getLabelColorize(String label, ChatColor... colors){
		return RMTextHelper.colorizeString(getLabel(label), colors);
	}
	
	//Alias
	public static String alias(RMCommand cmd){
		List<String> aliases = plugin.getRMConfig().getCommands().getAliasMap().get(cmd);
		if(aliases != null) if(aliases.size()>0) return aliases.get(0);
		return cmd.name().toLowerCase().replace("_", " ");
	}
}