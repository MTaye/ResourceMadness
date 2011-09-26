package com.mtaye.ResourceMadness;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMGame.Setting;
import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public final class RMText {
	public static RM plugin;
	
	public static String preLog = "ResourceMadness: ";
	
	//Errors
	public static String eNoPermissionCommand =  ChatColor.RED+"You don't have permission to use this command.";
	public static String eNoPermissionAction =  ChatColor.RED+"You don't have permission to use this action.";
	public static String eNoChangeLocked = ChatColor.RED+"This setting is locked. "+ChatColor.GRAY+"It cannot be changed.";
	public static String eNoOwnerCommand =  ChatColor.RED+"Only the owner can use this command.";
	public static String eNoOwnerAction =  ChatColor.RED+"Only the owner can use this action.";
	public static String eNoGamesYet = ChatColor.GRAY+"No games yet";
	public static String eNoAliasesYet = ChatColor.GRAY+"No aliases yet";
	public static String eNoTemplateYet = ChatColor.GRAY+"No templates yet";
	public static String eTeamDoesNotExist = "This team does not exist!";
	public static String eDidNotJoinAnyTeamYet = "You did not "+ChatColor.YELLOW+"join "+ChatColor.WHITE+"any "+ChatColor.YELLOW+"team "+ChatColor.WHITE+"yet.";
	public static String eCannotReadyWhileIngame = ChatColor.GRAY+"You cannot ready yourself while in a game.";
	public static String eCannotClaimFoundIngame = "You can't claim the game's "+ChatColor.YELLOW+"found items "+ChatColor.WHITE+"while you're in a game.";
	public static String eCannotClaimItemsIngame = "You can't claim your "+ChatColor.YELLOW+"items "+ChatColor.WHITE+"while you're in a game.";
	public static String eCannotClaimRewardIngame = "You can't claim your "+ChatColor.YELLOW+"reward "+ChatColor.WHITE+"while you're in a game.";
	public static String eMustBeIngameCommand = "You must be in a game to use this command.";
	public static String eMustBeIngameAction = "You must be in a game to use this action.";
	public static String eMustBeIngameChatWorld = "You must be in a game to use "+ChatColor.YELLOW+"world "+ChatColor.WHITE+"chat.";
	public static String eMustBeIngameChatGame = "You must be in a game to use "+ChatColor.YELLOW+"game "+ChatColor.WHITE+"chat.";
	public static String eMustBeIngameChatTeam = "You must be in a game to use "+ChatColor.YELLOW+"team "+ChatColor.WHITE+"chat.";
	public static String eItemsDoNotExist = "These items do not exist!";
	
	//Info
	public static String iPage = ChatColor.GRAY+"(Page *1 of *2)";
	public static String iGrayGreenOptional = ChatColor.GRAY+"Gray"+ChatColor.WHITE+"/"+ChatColor.GREEN+"green "+ChatColor.WHITE+"text is optional.";
	public static String iAdd = ChatColor.WHITE+"Create a new game.";
	public static String iRemove = ChatColor.WHITE+"Remove an existing game.";
	public static String iList = ChatColor.WHITE+"List games.";
	public static String iCommands = ChatColor.WHITE+"List commands and aliases.";
	public static String iInfo = ChatColor.WHITE+"Show *.";
	public static String iSettings = ChatColor.WHITE+"Show* settings.";
	public static String iSet = ChatColor.WHITE+"Set various game related settings.";
	public static String iMode = ChatColor.WHITE+"Change filter mode.";
	public static String iFilter = ChatColor.WHITE+"Add items to filter.";
	public static String iReward = ChatColor.WHITE+"Add reward items.";
	public static String iTools = ChatColor.WHITE+"Add tools items.";
	public static String iTemplate = ChatColor.WHITE+"* templates.";
	public static String iStart = ChatColor.WHITE+"Start a game. Randomize with "+ChatColor.GREEN+"amount"+ChatColor.WHITE+".";
	public static String iStop = ChatColor.WHITE+"* a game.";
	public static String iPause = ChatColor.WHITE+"* a game.";
	public static String iRestore = ChatColor.WHITE+"Restore game world changes.";
	public static String iJoin = ChatColor.WHITE+"Join a team.";
	public static String iQuit = ChatColor.WHITE+"Quit a team.";
	public static String iReady = ChatColor.WHITE+"Ready yourself.";
	public static String iChat = ChatColor.WHITE+"* chat.";
	public static String iItems = ChatColor.WHITE+"Get which items you need to gather.";
	public static String iItem = ChatColor.WHITE+"Get the item's name or id.";
	public static String iClaim = ChatColor.WHITE+"Claim * to inventory or chest.";
	
	//Save
	public static String saveSaving = ChatColor.RED+"Saving...";
	public static String saveSuccess = ChatColor.GREEN+"Data was saved successfully.";
	public static String saveFail = ChatColor.RED+"Data was not saved properly!";
	public static String saveNoData = ChatColor.GRAY+"No data to save.!";
	
	//Command Actions
	public static String aAdd = "Left click a game block to "+ChatColor.YELLOW+"add "+ChatColor.WHITE+"a new game.";
	public static String aRemove = "Left click a game block to "+ChatColor.GRAY+"remove "+ChatColor.WHITE+"a game.";
	public static String aInfoFound = "Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about the game's "+ChatColor.YELLOW+"found items"+ChatColor.WHITE+".";
	public static String aInfo = "Left click a game block to get "+ChatColor.YELLOW+"info"+ChatColor.WHITE+".";
	public static String aSettingsReset = "Left click a game block to "+ChatColor.YELLOW+"reset "+ChatColor.WHITE+"settings.";
	public static String aSettings = "Left click a game block to get "+ChatColor.YELLOW+"settings"+ChatColor.WHITE+".";
	public static String aModeFilter = "Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"filter"+ChatColor.WHITE+".";
	public static String aModeReward = "Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"reward"+ChatColor.WHITE+".";
	public static String aModeTools = "Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+" tools"+ChatColor.WHITE+".";
	public static String aModeCycle = "Left click a game block to "+ChatColor.YELLOW+"cycle "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"interface mode"+ChatColor.WHITE+".";
	public static String aJoin = "Left click a team block to "+ChatColor.YELLOW+"join "+ChatColor.WHITE+"the team.";
	public static String aStartRandom = "Left click a game block to "+ChatColor.YELLOW+"start "+ChatColor.WHITE+"the game with "+ChatColor.GREEN+"* "+"random item(s)"+ChatColor.WHITE+".";
	public static String aStart = "Left click a game block to "+ChatColor.YELLOW+"start "+ChatColor.WHITE+"the game.";
	//public static String aRestart = "Left click a game block to "+ChatColor.GOLD+"restart "+ChatColor.WHITE+"the game.";
	public static String aStop = "Left click a game block to "+ChatColor.RED+"stop "+ChatColor.WHITE+"the game.";
	public static String aPause = "Left click a game block to "+ChatColor.RED+"pause "+ChatColor.WHITE+"the game.";
	public static String aResume = "Left click a game block to "+ChatColor.GREEN+"resume "+ChatColor.WHITE+"the game.";
	public static String aRestore = "Left click a game block to "+ChatColor.YELLOW+"restore world changes "+ChatColor.WHITE+".";
	public static String aFilterInfoString = "Left click a game block to get the game's "+ChatColor.YELLOW+"filter "+ChatColor.WHITE+"as a "+ChatColor.YELLOW+"string"+ChatColor.WHITE+".";
	public static String aFilterInfo = "Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about the game's "+ChatColor.YELLOW+"filter"+ChatColor.WHITE+".";
	public static String aRewardInfoString = "Left click a game block to get the game's "+ChatColor.YELLOW+"reward "+ChatColor.WHITE+"as a "+ChatColor.YELLOW+"string"+ChatColor.WHITE+".";
	public static String aRewardInfo = "Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about the game's "+ChatColor.YELLOW+"reward"+ChatColor.WHITE+".";
	public static String aToolsInfoString = "Left click a game block to get the game's "+ChatColor.YELLOW+"tools "+ChatColor.WHITE+"as a "+ChatColor.YELLOW+"string"+ChatColor.WHITE+".";
	public static String aToolsInfo = "Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about the game's "+ChatColor.YELLOW+"tools"+ChatColor.WHITE+".";
	public static String aFilter = "Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"filter"+ChatColor.WHITE+".";
	public static String aReward = "Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"reward"+ChatColor.WHITE+".";
	public static String aTools = "Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"tools"+ChatColor.WHITE+".";
	public static String aTemplateLoad = "Left click a game block to "+ChatColor.YELLOW+"load "+ChatColor.WHITE+"template "+ChatColor.GREEN+"*"+ChatColor.WHITE+".";
	public static String aTemplateSave = "Left click a game block to "+ChatColor.YELLOW+"save "+ChatColor.WHITE+"template "+ChatColor.GREEN+"*"+ChatColor.WHITE+".";
	public static String aClaimFoundChest = "Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store items"+ChatColor.WHITE+".";
	public static String aClaimFoundChestSelect = "Left click a game block to "+ChatColor.YELLOW+"claim found items "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"chest"+ChatColor.WHITE+".";
	public static String aClaimFound = "Left click a game block to "+ChatColor.YELLOW+"claim found items"+ChatColor.WHITE+".";
	public static String aClaimItemsChest = "Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store items"+ChatColor.WHITE+".";
	public static String aClaimRewardChest = "Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store reward"+ChatColor.WHITE+".";
	public static String aClaimToolsChest = "Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store tools"+ChatColor.WHITE+".";
	public static String aSetMinPlayers = "Left click a game block to "+ChatColor.YELLOW+"set min players"+ChatColor.WHITE+".";
	public static String aSetMaxPlayers = "Left click a game block to "+ChatColor.YELLOW+"set max players"+ChatColor.WHITE+".";
	public static String aSetMinTeamPlayers = "Left click a game block to "+ChatColor.YELLOW+"set min team players"+ChatColor.WHITE+".";
	public static String aSetMaxTeamPlayers = "Left click a game block to "+ChatColor.YELLOW+"set max team players"+ChatColor.WHITE+".";
	//public static String aSetMaxItems = "Left click a game block to "+ChatColor.YELLOW+"set max items"+ChatColor.WHITE+".";
	public static String aSetTimeLimit = "Left click a game block to "+ChatColor.YELLOW+"set match time limit"+ChatColor.WHITE+".";
	public static String aSetRandom = "Left click a game block to "+ChatColor.YELLOW+"set auto randomize items"+ChatColor.WHITE+".";
	public static String aSetAdvertise = "Left click a game block to "+ChatColor.YELLOW+"set advertise"+ChatColor.WHITE+".";
	public static String aSetRestore = "Left click a game block to "+ChatColor.YELLOW+"set "+Setting.autoRestoreWorld.name()+ChatColor.WHITE+".";
	public static String aSetWarp = "Left click a game block to "+ChatColor.YELLOW+"set teleport players"+ChatColor.WHITE+".";
	public static String aSetMidgameJoin = "Left click a game block to "+ChatColor.YELLOW+"set allow midgame join"+ChatColor.WHITE+".";
	public static String aSetHealPlayer = "Left click a game block to "+ChatColor.YELLOW+"set heal player"+ChatColor.WHITE+".";
	public static String aSetClearInventory = "Left click a game block to "+ChatColor.YELLOW+"set clear player inventory"+ChatColor.WHITE+".";
	public static String aSetFoundAsReward = "Left click a game block to "+ChatColor.YELLOW+"set use found as reward"+ChatColor.WHITE+".";
	public static String aSetWarnUnequal = "Left click a game block to "+ChatColor.YELLOW+"set warn unequal items"+ChatColor.WHITE+".";
	public static String aSetAllowUnequal = "Left click a game block to "+ChatColor.YELLOW+"set allow unequal items"+ChatColor.WHITE+".";
	public static String aSetWarnHacked = "Left click a game block to "+ChatColor.YELLOW+"set warn hacked items"+ChatColor.WHITE+".";
	public static String aSetAllowHacked = "Left click a game block to "+ChatColor.YELLOW+"set allow hacked items"+ChatColor.WHITE+".";
	public static String aSetInfiniteReward = "Left click a game block to "+ChatColor.YELLOW+"set infinite reward"+ChatColor.WHITE+".";
	public static String aSetInfiniteTools = "Left click a game block to "+ChatColor.YELLOW+"set infinite tools"+ChatColor.WHITE+".";
	
	//Settting description
	public static String sMaxGames = "Max games";
	public static String sMaxGamesPerPlayer = "Max games per player";
	public static String sMinPlayers = "Min players";
	public static String sMaxPlayers = "Max players";
	public static String sMinTeamPlayers = "Min team players";
	public static String sMaxTeamPlayers = "Max team players";
	public static String sMaxItems = "Max items";
	public static String sTimeLimit = "Match time limit";
	public static String sAutoRandomizeAmount = "Randomly pick "+ChatColor.GREEN+"amount "+ChatColor.WHITE+"of items every match";
	public static String sAdvertise = "Advertise game in list";
	public static String sAutoRestoreWorld = "Auto restore world changes after match";
	public static String sWarpToSafety = "Teleport players before and after match";
	public static String sAllowMidgameJoin = "Allow players to join mid-game";
	public static String sHealPlayer = "Heal players at game start";
	public static String sClearPlayerInventory = "Clear/return player's items at game start/finish";
	public static String sWarnUnequal = "Warn when reward/tools can't be distributed equally";
	public static String sAllowUnequal = "Allow reward/tools to be distributed unequally";
	public static String sWarnHackedItems = "Warn when hacked items are added";
	public static String sAllowHackedItems = "Allow the use of hacked items";
	public static String sInfiniteReward = "Use infinite reward";
	public static String sInfiniteTools = "Use infinite tools";
	public static String sFoundAsReward = "Use the game's found items as reward";
	
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
	public static String gSuddenDeath = colorizeString("Sudden Death!", ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE);
	
	HashMap<Integer, String> gTimeLeft = new HashMap<Integer, String>();
	
	private RMText(){
	}
	
	public static String colorizeString(String str, ChatColor... colors){
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