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
	public static String noPermissionCommand = "You don't have permission to use this command.";
	public static String noPermissionAction = "You don't have permission to use this action.";
	public static String noChangeLocked = "This setting is locked. It cannot be changed.";
	public static String noOwnerCommand = "Only the owner can use this command.";
	public static String noOwnerAction = "Only the owner can use this action.";
	
	public static String maxGames = "Max games";
	public static String maxGamesPerPlayer = "Max games per player";
	public static String minPlayers = "Min players";
	public static String maxPlayers = "Max players";
	public static String minTeamPlayers = "Min team players";
	public static String maxTeamPlayers = "Max team players";
	public static String maxItems = "Max items";
	public static String autoRandomizeAmount = "Randomly pick amount of items every match";
	public static String warpToSafety = "Teleport players before and after match";
	public static String autoRestoreWorld = "Auto restore world changes after match";
	public static String warnHackedItems = "Warn when hacked items are added";
	public static String allowHackedItems = "Allow the use of hacked items";
	public static String keepIngame = "Keep offline players in-game";
	public static String allowMidgameJoin = "Allow players to join mid-game";
	public static String clearPlayerInventory = "Clear/return player's items at game start/finish";
	public static String warnUnequal = "Warn when reward/tools can't be distributed equally";
	public static String allowUnequal = "Allow reward/tools to be distributed unequally";
	public static String infiniteReward = "Use infinite reward";
	public static String infiniteTools = "Use infinite tools";
	
	public static String cAutoSave = "#Backup data at regular intervals to avoid loss. Interval is measured in minutes (0 = do not autosave).";
	public static String cUsePermissions = "#If you don't use permissions just leave it at false. Supported permissions are: p3, pex";
	public static String cUseRestore1 = "#Change this to false if you don't want the games to use the restore world changes functionality.";
	public static String cUseRestore2 = "#It may save some memory.";
	public static String cServerWide = "#These are server wide settings.";
	public static String cMaxGames = "#The maximum number of games allowed on server (0 = unlimited)";
	public static String cMaxGamesPerPlayer = "#The maximum number of games allowed per player. (0 = unlimited)";
	public static String cMinPlayersPerGame = "#The minimum number of players allowed per game. The Lowest number is 1 player. Only numbers higher than the amount of teams in a game will be evaluated.";
	public static String cMaxPlayersPerGame = "#The maximum number of players allowed per game. (0 = unlimited)";
	public static String cMinPlayersPerTeam = "#The minimum number of players allowed per team. The lowest number is 1 player";
	public static String cMaxPlayersPerTeam = "#The maximum number of players allowed per team. (0 = unlimited)";
	public static String cDefaultSettings1 = "#The following settings are the game defaults. Possible options are true or false.";
	public static String cDefaultSettings2 = "#Using :lock after true/false locks the setting for all games, e.g. allowHacked=false:lock";
	public static String cRestore = "#Auto restore world changes after match.";
	public static String cWarpToSafety = "#Teleport players before and after match.";
	public static String cWarnHackedItems = "#Warn when hacked items are added. Only the game's owner gets a warning.";
	public static String cAllowHackedItems = "#Allow the use of hacked items.";
	public static String cKeepIngame = "#Keep offline players in-game. Use this for persistent matches.";
	public static String cAllowMidgameJoin = "#Allow players to join mid-game.";
	public static String cClearPlayerInventory = "#Clear/return player's items at game start/finish.";
	public static String cWarnUnequal = "#Warn when reward/tools can't be distributed equally.";
	public static String cAllowUnequal = "#Allow reward/tools to be distributed unequally.";
	public static String cInfiniteReward = "#Use infinite reward";
	public static String cInfiniteTools = "#Use infinite tools";
	
	public static String gStartMatch = "ResourceMadness!";
	
	private RMText(){
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
		for(Material mat : materials){
			line+=mat.name()+", ";
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

		if(strSeparator==null) strSeparator = ",";
		String line = "";
		for(String str : strList){
			line+=str+strSeparator;
		}
		if(line.length()!=0) line = RMText.stripLast(line, strSeparator);
		return line;
	}
	
	//Get String By String List
	public static String getStringByStringList(List<String> strList, String strPre, String strAfter, String strSeparator){
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
	public static String getSortedItemsFromItemStackArray(ItemStack[] items){
		String strItems = "";
		
		HashMap<Integer, ItemStack> hashItems = RMInventoryHelper.combineItemsByItemStack(items);
		
		Integer[] array = hashItems.keySet().toArray(new Integer[hashItems.size()]);
		Arrays.sort(array);
		if(array.length>plugin.config.getTypeLimit()){
			for(Integer id : array){
				strItems += ChatColor.WHITE+""+id+":"+RMText.includeItem(new RMItem(hashItems.get(id)))+ChatColor.WHITE+", ";
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
}