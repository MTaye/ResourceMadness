package com.mtaye.ResourceMadness;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.Commands.RMCommand;
import com.mtaye.ResourceMadness.Game.GameState;
import com.mtaye.ResourceMadness.Game.InterfaceState;
import com.mtaye.ResourceMadness.helper.Helper;
import com.mtaye.ResourceMadness.helper.TextHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public final class Text {
	public static RM rm;
	public static String preLog = "[ResourceMadness] ";

	public static LabelBundle _labels = new LabelBundle();
	
	public Text(){
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
	
	public static LabelBundle getLabelBundle() { return _labels; }
	public static void setLabelBundle(LabelBundle labels){
		_labels = labels;
	}
	
	public static void setLabelBundleMissing(LabelBundle labels){
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
			Debug.warning("L: "+map.getKey());
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
						int num = Helper.getIntByString(text.substring(pos+4, posEnd));
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
	public static String getLabelPermission(String label, GamePlayer rmp, String node){
		return getLabelArgs(label, (rmp.hasPermission(node)?getLabel("cmd.filter.info"):""));
	}
	
	//Colorized
	public static String getLabelColorize(String label, ChatColor... colors){
		return TextHelper.colorizeString(getLabel(label), colors);
	}
	
	//Alias
	public static String alias(RMCommand cmd){
		List<String> aliases = rm.getRMConfig().getCommands().getAliasMap().get(cmd);
		if(aliases != null) if(aliases.size()>0) return aliases.get(0);
		return cmd.name().toLowerCase().replace("_", " ");
	}
}