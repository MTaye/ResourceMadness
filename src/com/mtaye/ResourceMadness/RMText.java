package com.mtaye.ResourceMadness;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public final class RMText {
	public static RM plugin;
	
	public static String preLog = "ResourceMadness: ";
	public static String noPermissionCommand =  ChatColor.RED+"You don't have permission to use this command.";
	public static String noPermissionAction =  ChatColor.RED+"You don't have permission to use this action.";
	public static String noChangeLocked = ChatColor.RED+"This setting is locked. "+ChatColor.GRAY+"It cannot be changed.";
	public static String noOwnerCommand =  ChatColor.RED+"Only the owner can use this command.";
	public static String noOwnerAction =  ChatColor.RED+"Only the owner can use this action.";
	
	public static String maxGames = "Max games";
	public static String maxGamesPerPlayer = "Max games per player";
	public static String minPlayers = "Min players";
	public static String maxPlayers = "Max players";
	public static String minTeamPlayers = "Min team players";
	public static String maxTeamPlayers = "Max team players";
	public static String maxItems = "Max items";
	public static String timeLimit = "Match time limit";
	public static String autoRandomizeAmount = "Randomly pick amount of items every match";
	public static String advertise = "Advertise game in list";
	public static String autoRestoreWorld = "Auto restore world changes after match";
	public static String warpToSafety = "Teleport players before and after match";
	public static String allowMidgameJoin = "Allow players to join mid-game";
	public static String healPlayer = "Heal players at game start";
	public static String clearPlayerInventory = "Clear/return player's items at game start/finish";
	public static String warnUnequal = "Warn when reward/tools can't be distributed equally";
	public static String allowUnequal = "Allow reward/tools to be distributed unequally";
	public static String warnHackedItems = "Warn when hacked items are added";
	public static String allowHackedItems = "Allow the use of hacked items";
	public static String infiniteReward = "Use infinite reward";
	public static String infiniteTools = "Use infinite tools";
	public static String foundAsReward = "Use the game's found items as reward";
	
	//Config section
	public static String cAutoSave =
			"# Backup data at regular intervals to avoid loss.\n" +
			"# Interval is measured in minutes (0 = do not autosave).";
	
	public static String cUsePermissions =
			"# If you don't use permissions just leave it at false.\n" +
			"# Supported permissions are: p3, pex, bukkit";
	
	public static String cUseRestore =
			"# Change this to false if you don't want the games to use the restore world changes functionality.\n" +
			"# It may save some memory.";
	
	public static String cServerWide = "# These are server wide settings.";
	public static String cMaxGames = "# The maximum number of games allowed on server. (0 = unlimited)";
	public static String cMaxGamesPerPlayer = "# The maximum number of games allowed per player. (0 = unlimited)";
	public static String cDefaultSettings1 =
			"# All settings from here on out are the game defaults.\n" +
			"# Using :lock after a setting locks it for all games, e.g. minPlayersPerTeam=2:lock";
	
	public static String cMinPlayers =
			"# The minimum number of players allowed per game. The Lowest number is 1 player.\n" +
			"# Only numbers higher than the amount of teams in a game will be evaluated.";
	
	public static String cMaxPlayers = "# The maximum number of players allowed per game. (0 = unlimited)";
	public static String cMinTeamPlayers = "# The minimum number of players allowed per team. The lowest number is 1 player.";
	public static String cMaxTeamPlayers = "# The maximum number of players allowed per team. (0 = unlimited)";
	public static String cTimeLimit = "# Match time limit. (0 = no time limit)";
	public static String cDefaultSettings2 =
			"# The following settings can be true or false.\n" +
			"# Using :lock after true/false locks the setting for all games, e.g. allowHacked=false:lock";
	
	public static String cAdvertise = "# Advertise game in list.";
	public static String cAutoRestoreWorld = "# Auto restore world changes after match.";
	public static String cWarpToSafety = "#T eleport players before and after match.";
	public static String cAllowMidgameJoin = "# Allow players to join mid-game.";
	public static String cHealPlayer = "# Heal players at game start";
	public static String cClearPlayerInventory = "# Clear/return player's items at game start/finish.";
	public static String cFoundAsReward = "# Use the game's found items as reward.";
	public static String cWarnUnequal = "# Warn when reward/tools can't be distributed equally.";
	public static String cAllowUnequal = "# Allow reward/tools to be distributed unequally.";
	public static String cWarnHackedItems = "# Warn when hacked items are added. Only the game's owner gets a warning.";
	public static String cAllowHackedItems = "# Allow the use of hacked items.";
	public static String cInfiniteReward = "# Use infinite reward";
	public static String cInfiniteTools = "# Use infinite tools";
	
	//Aliases section
	public static String aAliases =
			"# You can use aliases for most commands.\n" +
			"# Usage:\n" +
			"#    command: alias\n\n" +
			"# Multiple aliases must be separated with a comma.\n" +
			"# Usage:\n" +
			"#    command: alias1, alias2, alias3\n\n" +
			"# Example: filter random: filter r\n" +
			"# Result: /rm filter r\n\n" +
			"# Example: filter random: filter r\n" +
			"# Result: /rm filter r\n\n" +
			"# Example: filter random: filter r\n" +
			"# Result: /rm filter r\n\n" +
			"# Command list:";
	
	public static String gPrepare = ChatColor.GOLD+"Prepare yourselves...";
	public static String gStartMatch = ChatColor.GOLD+"ResourceMadness!";
	public static String gSuddenDeath = paintString("Sudden Death!", ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE);
	
	HashMap<Integer, String> gTimeLeft = new HashMap<Integer, String>();
	
	private RMText(){
	}
	
	public static String paintString(String str, ChatColor... colors){
		if((str==null)||(str.length() == 0)) return str;
		if((colors == null)||(colors.length == 0)) return str;
		String line = "";
		int pos = 0;
		int colorPos = 0;
		while(pos<str.length()){
			line+=colors[colorPos]+str.substring(pos, pos+1);
			colorPos++;
			pos++;
			if(colorPos==colors.length) colorPos = 0;
		}
		return line;
	}
	
	public static String stripLast(String str, String s){
		int pos = str.lastIndexOf(s);
		if(pos!=-1){
			String part1 = str.substring(0, pos);
			String part2 = str.substring(pos+s.length());
			return part1+part2;
		}
		return str;
	}
	
	public static String getFormattedItemStringByHashMap(HashMap<Integer, Material> hashMap){
		String line = "";
		Integer[] array = hashMap.keySet().toArray(new Integer[hashMap.size()]);
		Arrays.sort(array);
		for(Integer id : array){
			line+=""+ChatColor.YELLOW+id+ChatColor.GRAY+":"+ChatColor.WHITE+hashMap.get(id).name()+", ";
		}
		line = RMText.stripLast(line, ",");
		return line;
	}
	
	//Get Formatted String By List
	public static String getFormattedStringByList(List<String> strList){
		String line = "";
		for(String str : strList){
			line+=str+ChatColor.WHITE+", ";
		}
		line = RMText.stripLast(line, ",");
		return line;
	}
	
	//Get Formatted String By List Material
	public static String getFormattedStringByListMaterial(List<Material> materials){
		String line = "";
		if(materials.size()!=1){
			for(Material mat : materials){
				line+=mat.getId()+", ";
			}
		}
		else{
			for(Material mat : materials){
				line+=mat.name()+", ";
			}
		}
		line = RMText.stripLast(line, ",");
		return line;
	}
	
	//Get Text BlockList
	public static String getTextBlockList(List<List<Block>> blockList, boolean allowNull){
		String line = "";
		for(List<Block> bList : blockList){
			for(Block b : bList){
				if(b!=null){
					line+=b.getType().name();
				}
				else if(allowNull) line+="null";
				line+=",";
			}
		}
		return RMText.stripLast(line, ",");
	}
	
	//Get Text List
	public static String getTextList(List<Block> bList, boolean allowNull){
		String line = "";
		for(Block b : bList){
			if(b!=null){
				line+=b.getType().name();
			}
			else if(allowNull) line+="null";
			line+=",";
		}
		return RMText.stripLast(line, ",");
	}
	
	//Get String By String List
	public static String getStringByStringList(List<String> strList, String strSeparator){
		return getStringByStringList(strList, strSeparator, null, null);
	}
	
	//Get String By String List
	public static String getStringByStringList(List<String> strList, String strSeparator, String strPre, String strAfter){
		if(strPre==null) strPre = "";
		if(strAfter==null) strAfter = "";
		if(strSeparator==null) strSeparator = ",";
		String line = "";
		for(String str : strList){
			line+=strPre+str+strAfter+strSeparator;
		}
		if(line.length()!=0) line = RMText.stripLast(line, strSeparator);
		return line;
	}
	
	//Include Item
	public static String includeItem(RMItem rmItem, boolean... less){
		int i1 = rmItem.getAmount();
		int i2 = rmItem.getAmountHigh();
		if((i1!=1)&&(less.length==0)){
			if(i2>0) return ChatColor.GRAY+":"+i1+"-"+i2;
			return ChatColor.GRAY+":"+i1;
		}
		return "";
	}
	
	//Get Sorted Items from ItemStack Array
	public static String getStringSortedItems(List<ItemStack> items){
		return getStringSortedItems(items, plugin.config.getTypeLimit());
	}
	public static String getStringSortedItems(List<ItemStack> items, int typeLimit){
		String strItems = "";
		HashMap<Integer, ItemStack> hashItems = RMInventoryHelper.combineItemsByItemStack(items);
		
		Integer[] array = hashItems.keySet().toArray(new Integer[hashItems.size()]);
		Arrays.sort(array);
		if(array.length>typeLimit){
			for(Integer id : array){
				strItems += ChatColor.WHITE+""+id+RMText.includeItem(new RMItem(hashItems.get(id)))+ChatColor.WHITE+", ";
			}
		}
		else{
			for(Integer id : array){
				strItems += ChatColor.WHITE+""+Material.getMaterial(id)+RMText.includeItem(new RMItem(hashItems.get(id)))+ChatColor.WHITE+", ";
			}
		}
		strItems = RMText.stripLast(strItems, ", ");
		return strItems;
	}
	
	public static String getStringTotal(String str){
		int length = str.length();
		if(length<9) str = "Total: "+str;
		else if(length<11) str = "Ttl: "+str;
		else if(length<13) str = "T: "+str;
		return str;
	}
	
	public static String firstLetterToUpperCase(String str){
		if(str.length()>0){
			String character = str.substring(0, 1);
			str = str.replaceFirst(character,character.toUpperCase());
		}
		return str;
	}
	
	public static String getTextFromArgs(String[] args){
		return getTextFromArgs(args, 0);
	}
	
	public static String getTextFromArgs(String[] args, int beginIndex){
		String str = "";
		for(int i=beginIndex; i<args.length; i++){
			str+=args[i]+" ";
		}
		str = stripLast(str, " ");
		return str;
	}
	
	public static boolean equalsIgnoreCase(String arg, String... strArray){
		for(String str : strArray){
			if(str.toLowerCase() == arg.toLowerCase()) return true;
		}
		return false;
	}
}