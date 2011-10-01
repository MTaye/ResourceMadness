package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMCommands.RMCommand;
import com.mtaye.ResourceMadness.RMGame.Setting;
import com.mtaye.ResourceMadness.Helper.RMInventoryHelper;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMText {
	
	public enum RMTextStr{
	//Error
	Error_NoPermissionCommand,
	Error_NoPermissionAction,
	Error_NoChangeLocked,
	Error_NoOwnerCommand,
	Error_NoOwnerAction,
	Error_NoGamesYet,
	Error_NoAliasesYet,
	Error_NoTemplateYet,
	Error_TeamDoesNotExist,
	Error_DidNotJoinAnyTeamYet,
	Error_CannotReadyWhileIngame,
	Error_CannotClaimFoundIngame,
	Error_CannotClaimItemsIngame,
	Error_CannotClaimRewardIngame,
	Error_MustBeIngameCommand,
	Error_MustBeIngameAction,
	Error_MustBeIngameChatWorld,
	Error_MustBeIngameChatGame,
	Error_MustBeIngameChatTeam,
	Error_ItemsDoNotExist,
	
	//Save
	Save_Saving,
	Save_Success,
	Save_Fail,
	Save_NoData,
	
	//Action
	Action_Add,
	Action_Remove,
	Action_InfoFound,
	Action_Info,
	Action_SettingsReset,
	Action_Settings,
	Action_ModeFilter,
	Action_ModeReward,
	Action_ModeTools,
	Action_ModeCycle,
	Action_Join,
	Action_StartRandom,
	Action_Start,
	Action_Restart,
	Action_Stop,
	Action_Pause,
	Action_Resume,
	Action_Restore,
	Action_FilterInfoString,
	Action_FilterInfo,
	Action_RewardInfoString,
	Action_RewardInfo,
	Action_ToolsInfoString,
	Action_ToolsInfo,
	Action_Filter,
	Action_Reward,
	Action_Tools,
	Action_TemplateLoad,
	Action_TemplateSave,
	Action_ClaimFoundChest,
	Action_ClaimFoundChestSelect,
	Action_ClaimFound,
	Action_ClaimItemsChest,
	Action_ClaimRewardChest,
	Action_ClaimToolsChest,
	Action_SetMinPlayers,
	Action_SetMaxPlayers,
	Action_SetMinTeamPlayers,
	Action_SetMaxTeamPlayers,
	Action_SetMaxItems,
	Action_SetTimeLimit,
	Action_SetRandom,
	Action_SetAdvertise,
	Action_SetRestore,
	Action_SetWarp,
	Action_SetMidgameJoin,
	Action_SetHealPlayer,
	Action_SetClearInventory,
	Action_SetFoundAsReward,
	Action_SetWarnUnequal,
	Action_SetAllowUnequal,
	Action_SetWarnHacked,
	Action_SetAllowHacked,
	Action_SetInfiniteReward,
	Action_SetInfiniteTools,

	//Setting description
	Setting_MaxGames,
	Setting_MaxGamesPerPlayer,
	Setting_MinPlayers,
	Setting_MaxPlayers,
	Setting_MinTeamPlayers,
	Setting_MaxTeamPlayers,
	Setting_MaxItems,
	Setting_TimeLimit,
	Setting_AutoRandomizeAmount,
	Setting_Advertise,
	Setting_AutoRestoreWorld,
	Setting_WarpToSafety,
	Setting_AllowMidgameJoin,
	Setting_HealPlayer,
	Setting_ClearPlayerInventory,
	Setting_WarnUnequal,
	Setting_AllowUnequal,
	Setting_WarnHackedItems,
	Setting_AllowHackedItems,
	Setting_InfiniteReward,
	Setting_InfiniteTools,
	Setting_FoundAsReward,

	//Config
	Config_AutoSave,
	Config_UsePermissions,
	Config_UseRestore,
	Config_ServerWide,
	Config_MaxGames,
	Config_MaxGamesPerPlayer,
	Config_DefaultSettings1,
	Config_MinPlayers,
	Config_MaxPlayers,
	Config_MinTeamPlayers,
	Config_MaxTeamPlayers,
	Config_TimeLimit,
	Config_DefaultSettings2,
	Config_Advertise,
	Config_AutoRestoreWorld,
	Config_WarpToSafety,
	Config_AllowMidgameJoin,
	Config_HealPlayer,
	Config_ClearPlayerInventory,
	Config_FoundAsReward,
	Config_WarnUnequal,
	Config_AllowUnequal,
	Config_WarnHackedItems,
	Config_AllowHackedItems,
	Config_InfiniteReward,
	Config_InfiniteTools,
	Config_Aliases,
	
	//Commands
	Command_Add,
	Command_Remove,
	Command_List,
	Command_Commands,
	Command_Info,
	Command_InfoFound,
	Command_InfoClaim,
	Command_InfoItems,
	Command_InfoReward,
	Command_InfoTools,
	Command_Settings,
	Command_SettingsReset,
	Command_Set,
	Command_SetMinPlayers,
	Command_SetMaxPlayers,
	Command_SetMinTeamPlayers,
	Command_SetMaxTeamPlayers,
	Command_SetTimeLimit,
	Command_SetRandom,
	Command_SetAdvertise,
	Command_SetRestore,
	Command_SetWarp,
	Command_SetMidgameJoin,
	Command_SetHealPlayer,
	Command_SetClearInventory,
	Command_SetFoundAsReward,
	Command_SetWarnUnequal,
	Command_SetAllowUnequal,
	Command_SetWarnHacked,
	Command_SetAllowHacked,
	Command_SetInfiniteReward,
	Command_SetInfiniteTools,
	Command_Mode,
	Command_ModeFilter,
	Command_ModeReward,
	Command_ModeTools,
	Command_Filter,
	Command_FilterRandom,
	Command_Reward,
	Command_Tools,
	Command_FilterRewardToolsInfo,
	Command_FilterRewardToolsInfoString,
	Command_FilterRewardToolsAdd,
	Command_FilterRewardToolsSubtract,
	Command_FilterRewardToolsClear,
	Command_Template,
	Command_TemplateList,
	Command_TemplateLoad,
	Command_TemplateSave,
	Command_TemplateRemove,
	Command_Start,
	Command_Stop,
	Command_Pause,
	Command_Resume,
	Command_Join,
	Command_Quit,
	Command_Ready,
	Command_Items,
	Command_Item,
	Command_Restore,
	Command_Chat,
	Command_ChatWorld,
	Command_ChatGame,
	Command_ChatTeam,
	Command_Claim,
	Command_ClaimFound,
	Command_ClaimItems,
	Command_ClaimReward,
	Command_ClaimTools,
	Command_ClaimFoundItemsRewardToolsChest,
	Command_Save,

	//Description
	Desc_Page,
	Desc_GrayGreenOptional,
	Desc_Add,
	Desc_Remove,
	Desc_List,
	Desc_Commands,
	Desc_Info,
	Desc_Settings,
	Desc_Set,
	Desc_Mode,
	Desc_Filter,
	Desc_Reward,
	Desc_Tools,
	Desc_Template,
	Desc_TemplateList,
	Desc_TemplateLoad,
	Desc_TemplateSave,
	Desc_TemplateRemove,
	Desc_Start,
	Desc_Stop,
	Desc_Pause,
	Desc_Restore,
	Desc_Join,
	Desc_Quit,
	Desc_Ready,
	Desc_Chat,
	Desc_ChatWorld,
	Desc_ChatGame,
	Desc_ChatTeam,
	Desc_Items,
	Desc_Item,
	Desc_Claim,
	Desc_Example_ChatWorld,
	Desc_Example_ChatWorldMessage,
	Desc_Example_ChatGame,
	Desc_Example_ChatGameMessage,
	Desc_Example_ChatTeam,
	Desc_Example_ChatTeamMessage,
	
	//Info
	Misc_FilterTypeAll,
	Misc_FilterTypeBlock,
	Misc_FilterTypeItem,
	Misc_Amount,
	Misc_Stack,
	Misc_AppName,
	Misc_Id,
	Misc_Page,
	Misc_RM,
	Misc_Examples,
	Misc_TeamIdColor,
	Misc_ItemsId,
	Misc_ItemIdName,
	Misc_Template,
	Misc_ChatMessage,
	Misc_ClaimChest,
	
	//List
	List_ListId,
	List_ListOwner,
	List_ListPlayers,
	List_ListTimeLimit,
	List_ListInGame,
	List_ListInTeam,
	List_ListTeams,
	List_TemplateListFilter,
	List_TemplateListReward,
	List_TemplateListTools,
	List_TemplateListTotal;
	}
	
	public enum RMTextType{
		ERROR,
		SAVE,
		ACTION,
		SETTING,
		CONFIG,
		DESCRIPTION,
		INFO,
		LIST;
	}
	
	public static RM plugin;
	public static String preLog = "ResourceMadness: ";
	
	public static HashMap<RMTextStr, String> textMap = new HashMap<RMTextStr, String>();
	
	//Variables;
	//Error
	public static String e_NoPermissionCommand = "";
	public static String e_NoPermissionAction = "";
	public static String e_NoChangeLocked = "";
	public static String e_NoOwnerCommand = "";
	public static String e_NoOwnerAction = "";
	public static String e_NoGamesYet = "";
	public static String e_NoAliasesYet = "";
	public static String e_NoTemplateYet = "";
	public static String e_TeamDoesNotExist = "";
	public static String e_DidNotJoinAnyTeamYet = "";
	public static String e_CannotReadyWhileIngame = "";
	public static String e_CannotClaimFoundIngame = "";
	public static String e_CannotClaimItemsIngame = "";
	public static String e_CannotClaimRewardIngame = "";
	public static String e_MustBeIngameCommand = "";
	public static String e_MustBeIngameAction = "";
	public static String e_MustBeIngameChatWorld = "";
	public static String e_MustBeIngameChatGame = "";
	public static String e_MustBeIngameChatTeam = "";
	public static String e_ItemsDoNotExist = "";
	
	//Save
	public static String save_Saving = "";
	public static String save_Success = "";
	public static String save_Fail = "";
	public static String save_NoData = "";
	
	//Action
	public static String a_Add = "";
	public static String a_Remove = "";
	public static String a_InfoFound = "";
	public static String a_Info = "";
	public static String a_SettingsReset = "";
	public static String a_Settings = "";
	public static String a_ModeFilter = "";
	public static String a_ModeReward = "";
	public static String a_ModeTools = "";
	public static String a_ModeCycle = "";
	public static String a_Join = "";
	public static String a_StartRandom = "";
	public static String a_Start = "";
	public static String a_Restart = "";
	public static String a_Stop = "";
	public static String a_Pause = "";
	public static String a_Resume = "";
	public static String a_Restore = "";
	public static String a_FilterInfoString = "";
	public static String a_FilterInfo = "";
	public static String a_RewardInfoString = "";
	public static String a_RewardInfo = "";
	public static String a_ToolsInfoString = "";
	public static String a_ToolsInfo = "";
	public static String a_Filter = "";
	public static String a_Reward = "";
	public static String a_Tools = "";
	public static String a_TemplateLoad = "";
	public static String a_TemplateSave = "";
	public static String a_ClaimFoundChest = "";
	public static String a_ClaimFoundChestSelect = "";
	public static String a_ClaimFound = "";
	public static String a_ClaimItemsChest = "";
	public static String a_ClaimRewardChest = "";
	public static String a_ClaimToolsChest = "";
	public static String a_SetMinPlayers = "";
	public static String a_SetMaxPlayers = "";
	public static String a_SetMinTeamPlayers = "";
	public static String a_SetMaxTeamPlayers = "";
	public static String a_SetMaxItems = "";
	public static String a_SetTimeLimit = "";
	public static String a_SetRandom = "";
	public static String a_SetAdvertise = "";
	public static String a_SetRestore = "";
	public static String a_SetWarp = "";
	public static String a_SetMidgameJoin = "";
	public static String a_SetHealPlayer = "";
	public static String a_SetClearInventory = "";
	public static String a_SetFoundAsReward = "";
	public static String a_SetWarnUnequal = "";
	public static String a_SetAllowUnequal = "";
	public static String a_SetWarnHacked = "";
	public static String a_SetAllowHacked = "";
	public static String a_SetInfiniteReward = "";
	public static String a_SetInfiniteTools = "";
	
	//Settings
	public static String s_MaxGames = "";
	public static String s_MaxGamesPerPlayer = "";
	public static String s_MinPlayers = "";
	public static String s_MaxPlayers = "";
	public static String s_MinTeamPlayers = "";
	public static String s_MaxTeamPlayers = "";
	public static String s_MaxItems = "";
	public static String s_TimeLimit = "";
	public static String s_AutoRandomizeAmount = "";
	public static String s_Advertise = "";
	public static String s_AutoRestoreWorld = "";
	public static String s_WarpToSafety = "";
	public static String s_AllowMidgameJoin = "";
	public static String s_HealPlayer = "";
	public static String s_ClearPlayerInventory = "";
	public static String s_WarnUnequal = "";
	public static String s_AllowUnequal = "";
	public static String s_WarnHackedItems = "";
	public static String s_AllowHackedItems = "";
	public static String s_InfiniteReward = "";
	public static String s_InfiniteTools = "";
	public static String s_FoundAsReward = "";
	
	//Config
	public static String config_AutoSave = "";
	public static String config_UsePermissions = "";
	public static String config_UseRestore = "";
	public static String config_ServerWide = "";
	public static String config_MaxGames = "";
	public static String config_MaxGamesPerPlayer = "";
	public static String config_DefaultSettings1 = "";
	public static String config_MinPlayers = "";
	public static String config_MaxPlayers = "";
	public static String config_MinTeamPlayers = "";
	public static String config_MaxTeamPlayers = "";
	public static String config_TimeLimit = "";
	public static String config_DefaultSettings2 = "";
	public static String config_Advertise = "";
	public static String config_AutoRestoreWorld = "";
	public static String config_WarpToSafety = "";
	public static String config_AllowMidgameJoin = "";
	public static String config_HealPlayer = "";
	public static String config_ClearPlayerInventory = "";
	public static String config_FoundAsReward = "";
	public static String config_WarnUnequal = "";
	public static String config_AllowUnequal = "";
	public static String config_WarnHackedItems = "";
	public static String config_AllowHackedItems = "";
	public static String config_InfiniteReward = "";
	public static String config_InfiniteTools = "";
	public static String config_Aliases = "";
	
	//Commands
	public static String c_Add = "";
	public static String c_Remove = "";
	public static String c_List = "";
	public static String c_Commands = "";
	public static String c_Info = "";
	public static String c_InfoFound = "";
	public static String c_InfoClaim = "";
	public static String c_InfoItems = "";
	public static String c_InfoReward = "";
	public static String c_InfoTools = "";
	public static String c_Settings = "";
	public static String c_SettingsReset = "";
	public static String c_Set = "";
	public static String c_SetMinPlayers = "";
	public static String c_SetMaxPlayers = "";
	public static String c_SetMinTeamPlayers = "";
	public static String c_SetMaxTeamPlayers = "";
	public static String c_SetTimeLimit = "";
	public static String c_SetRandom = "";
	public static String c_SetAdvertise = "";
	public static String c_SetRestore = "";
	public static String c_SetWarp = "";
	public static String c_SetMidgameJoin = "";
	public static String c_SetHealPlayer = "";
	public static String c_SetClearInventory = "";
	public static String c_SetFoundAsReward = "";
	public static String c_SetWarnUnequal = "";
	public static String c_SetAllowUnequal = "";
	public static String c_SetWarnHacked = "";
	public static String c_SetAllowHacked = "";
	public static String c_SetInfiniteReward = "";
	public static String c_SetInfiniteTools = "";
	public static String c_Mode = "";
	public static String c_ModeFilter = "";
	public static String c_ModeReward = "";
	public static String c_ModeTools = "";
	public static String c_Filter = "";
	public static String c_FilterRandom = "";
	public static String c_Reward = "";
	public static String c_Tools = "";
	public static String c_FilterRewardToolsInfo = "";
	public static String c_FilterRewardToolsInfoString = "";
	public static String c_FilterRewardToolsAdd = "";
	public static String c_FilterRewardToolsSubtract = "";
	public static String c_FilterRewardToolsClear = "";
	public static String c_Template = "";
	public static String c_TemplateList = "";
	public static String c_TemplateLoad = "";
	public static String c_TemplateSave = "";
	public static String c_TemplateRemove = "";
	public static String c_Start = "";
	public static String c_Stop = "";
	public static String c_Pause = "";
	public static String c_Resume = "";
	public static String c_Join = "";
	public static String c_Quit = "";
	public static String c_Ready = "";
	public static String c_Items = "";
	public static String c_Item = "";
	public static String c_Restore = "";
	public static String c_Chat = "";
	public static String c_ChatWorld = "";
	public static String c_ChatGame = "";
	public static String c_ChatTeam = "";
	public static String c_Claim = "";
	public static String c_ClaimFound = "";
	public static String c_ClaimItems = "";
	public static String c_ClaimReward = "";
	public static String c_ClaimTools = "";
	public static String c_ClaimFoundItemsRewardToolsChest = "";
	public static String c_Save = "";
	
	//Description
	public static String d_Page = "";
	public static String d_GrayGreenOptional = "";
	public static String d_Add = "";
	public static String d_Remove = "";
	public static String d_List = "";
	public static String d_Commands = "";
	public static String d_Info = "";
	public static String d_Settings = "";
	public static String d_Set = "";
	public static String d_Mode = "";
	public static String d_Filter = "";
	public static String d_Reward = "";
	public static String d_Tools = "";
	public static String d_Template = "";
	public static String d_TemplateList = "";
	public static String d_TemplateLoad = "";
	public static String d_TemplateSave = "";
	public static String d_TemplateRemove = "";
	public static String d_Start = "";
	public static String d_Stop = "";
	public static String d_Pause = "";
	public static String d_Restore = "";
	public static String d_Join = "";
	public static String d_Quit = "";
	public static String d_Ready = "";
	public static String d_Chat = "";
	public static String d_ChatWorld = "";
	public static String d_ChatGame = "";
	public static String d_ChatTeam = "";
	public static String d_Items = "";
	public static String d_Item = "";
	public static String d_Claim = "";
	public static String d_Example_ChatWorld = "";
	public static String d_Example_ChatWorldMessage = "";
	public static String d_Example_ChatGame = "";
	public static String d_Example_ChatGameMessage = "";
	public static String d_Example_ChatTeam = "";
	public static String d_Example_ChatTeamMessage = "";
	
	//Commands
	//List
	public static String l_ListId = "";
	public static String l_ListOwner = "";
	public static String l_ListPlayers = "";
	public static String l_ListTimeLimit = "";
	public static String l_ListInGame = "";
	public static String l_ListInTeam = "";
	public static String l_ListTeams = "";
	public static String l_List(int page, int pageLimit){
		return ChatColor.GOLD+m_RM+" "+c_List+" "+d_Page(page, pageLimit);
	}
	
	//TemplateList
	public static String l_TemplateList(int page, int pageLimit){
		return ChatColor.GOLD+m_RM+" "+c_Template+" "+d_Page(page, pageLimit);
	}
	public static String l_TemplateListFilter = "";
	public static String l_TemplateListReward = "";
	public static String l_TemplateListTools = "";
	public static String l_TemplateListTotal = "";
	
	//AliasList
	public static String iAliasList(int page, int pageLimit){
		return ChatColor.GOLD+m_RM+" "+c_Commands+" "+d_Page(page, pageLimit);
	}
	
	//no caps
	public static String m_FilterTypeAll = "";
	public static String m_FilterTypeBlock = "";
	public static String m_FilterTypeItem = "";
	public static String m_AllBlockItem = "";
	public static String m_Amount = "";
	public static String m_Stack = "";
	public static String m_AmountStack = "";
	
	public static String m_appName = "";
	public static String m_Id = "";
	public static String m_Page = "";
	public static String m_RM = "";
	public static String m_Examples = "";
	public static String m_TeamIdColor = "";
	public static String m_ItemsId = "";
	public static String m_ItemIdName = "";
	public static String m_Template = "";
	public static String m_ChatMessage = "";
	
	public static String d_Page(int page, int pageLimit){
		return d_Page.replace("*1", ""+page).replace("*2", ""+pageLimit);
	}
	
	///////////////////
	//RM INFO SECTION//
	///////////////////
	
	//rmInfo
	public static String rmInfo(int page, int pageLimit){
		return ChatColor.GOLD+m_appName+" "+d_Page(page, pageLimit);
	}
	public static String rmInfo_Add = "";
	public static String rmInfo_Remove = "";
	public static String rmInfo_List = "";
	public static String rmInfo_Commands = "";
	public static String rmInfo_Info(String line){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Info+" "+ChatColor.GREEN+line+" "+d_Info.replace("*", line);
	}
	public static String rmInfo_Set = "";
	public static String rmInfo_Settings(String line){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Settings+" "+ChatColor.GREEN+line+" "+d_Settings.replace("*", (line.length()>0?"/"+line:""));
	}
	public static String rmInfo_Mode(String line){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Mode+" "+ChatColor.GREEN+line+" "+d_Mode;
	}
	public static String rmInfo_Filter(RMPlayer rmp){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Filter+" "+ChatColor.GREEN+(rmp.hasPermission("resourcemadness.filter.info")?c_FilterRewardToolsInfo:"")+" "+d_Filter;
	}
	public static String rmInfo_Reward(RMPlayer rmp){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Reward+" "+ChatColor.GREEN+(rmp.hasPermission("resourcemadness.reward.info")?c_FilterRewardToolsInfo:"")+" "+d_Reward;
	}
	public static String rmInfo_Tools(RMPlayer rmp){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Tools+" "+ChatColor.GREEN+(rmp.hasPermission("resourcemadness.tools.info")?c_FilterRewardToolsInfo:"")+" "+d_Tools;
	}
	public static String rmInfo_Template(String line){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Template+" "+line+" "+d_Template.replace("*", RMTextHelper.firstLetterToUpperCase(line));
	}
	public static String rmInfo_Start = "";
	public static String rmInfo_Stop(String line){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+line.toLowerCase()+" "+d_Stop.replace("*", RMTextHelper.firstLetterToUpperCase(line));
	}
	public static String rmInfo_Pause(String line){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+line+" "+d_Pause.replace("*", RMTextHelper.firstLetterToUpperCase(line));
	}
	public static String rmInfo_Restore = "";
	public static String rmInfo_Join = "";
	public static String rmInfo_Quit = "";
	public static String rmInfo_Ready = "";
	public static String rmInfo_Chat(String line){
		return ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Chat+" "+line+" "+d_Chat.replace("*", RMTextHelper.firstLetterToUpperCase(line));
	}
	public static String rmInfo_Items = "";
	public static String rmInfo_Item = "";
	public static String rmInfo_Claim(String line){
		return ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Claim+" "+line+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest+" "+d_Claim.replace("*", line.toLowerCase());
	}
	
	//rmSetInfo
	public static String rmSetInfo(int page, int pageLimit){
		return ChatColor.GOLD+m_RM+" "+c_Set+" "+d_Page(page, pageLimit);
	}
	public static String setInfo_MinPlayers = "";
	public static String setInfo_MaxPlayers = "";
	public static String setInfo_MinTeamPlayers = "";
	public static String setInfo_MaxTeamPlayers = "";
	public static String setInfo_TimeLimit = "";
	public static String setInfo_Random = "";
	public static String setInfo_Advertise = "";
	public static String setInfo_Restore = "";
	public static String setInfo_Warp = "";
	public static String setInfo_MidgameJoin = "";
	public static String setInfo_HealPlayer = "";
	public static String setInfo_ClearInventory = "";
	public static String setInfo_FoundAsReward = "";
	public static String setInfo_WarnUnequal = "";
	public static String setInfo_AllowUnequal = "";
	public static String setInfo_WarnHacked = "";
	public static String setInfo_AllowHacked = "";
	public static String setInfo_InfiniteReward = "";
	public static String setInfo_InfiniteTools = "";
	
	//rmFilterInfo
	public static String filterInfo = "";
	public static String filterInfo_Set = "";
	public static String filterInfo_Random = "";
	public static String filterInfo_Add = "";
	public static String filterInfo_Subtract = "";
	public static String filterInfo_Clear = "";
	//rmFilterInfo examples
	public static String filterExample_Info = "";
	public static String filterExample_Set = "";
	public static String filterExample_Random = "";
	public static String filterExample_Add = "";
	public static String filterExample_Subtract = "";
	public static String filterExample_Clear1 = "";
	public static String filterExample_Clear2 = "";
	
	//rmRewardInfo
	public static String rewardInfo = "";
	public static String rewardInfo_Set = "";
	public static String rewardInfo_Add = "";
	public static String rewardInfo_Subtract = "";
	public static String rewardInfo_Clear = "";
	//rmRewardInfo examples
	public static String rewardExample_Info = "";
	public static String rewardExample_Set = "";
	public static String rewardExample_Add = "";
	public static String rewardExample_Subtract = "";
	public static String rewardExample_Clear1 = "";
	public static String rewardExample_Clear2 = "";
	
	//rmToolsInfo
	public static String toolsInfo = "";
	public static String toolsInfo_Set = "";
	public static String toolsInfo_Add = "";
	public static String toolsInfo_Subtract = "";
	public static String toolsInfo_Clear = "";
	//rmToolsInfo examples
	public static String toolsExample_Info = "";
	public static String toolsExample_Set = "";
	public static String toolsExample_Add = "";
	public static String toolsExample_Subtract = "";
	public static String toolsExample_Clear1 = "";
	public static String toolsExample_Clear2 = "";
	
	//rmTemplateInfo
	public static String templateInfo = "";
	public static String templateInfo_List = "";
	public static String templateInfo_Load = "";
	public static String templateInfo_Save = "";
	public static String templateInfo_Remove = "";
	//rmTemplate examples
	public static String templateExample_List = "";
	public static String templateExample_Load = "";
	public static String templateExample_Save = "";
	public static String templateExample_Remove = "";
	
	//rmClaimInfo
	public static String claimInfo = "";
	public static String claimInfo_Found = "";
	public static String claimInfo_Items = "";
	public static String claimInfo_Reward = "";
	public static String claimInfo_Tools = "";
	//rmClaimInfo examples
	public static String claimExample_Found = "";
	public static String claimExample_FoundChest = "";
	public static String claimExample_Items = "";
	public static String claimExample_ItemsChest = "";
	public static String claimExample_Reward = "";
	public static String claimExample_RewardChest = "";
	public static String claimExample_Tools = "";
	public static String claimExample_ToolsChest = "";
	
	//rmChatInfo
	public static String chatInfo = "";
	public static String chatInfo_World = "";
	public static String chatInfo_Game = "";
	public static String chatInfo_Team = "";
	//rmChatInfo examples
	public static String chatExample_World = "";
	public static String chatExample_WorldMessage = "";
	public static String chatExample_Game = "";
	public static String chatExample_GameMessage = "";
	public static String chatExample_Team = "";
	public static String chatExample_TeamMessage = "";
	
	//rmItemInfo
	public static String itemInfo = "";
	public static String itemInfo_Arg = "";
	//rmItemInfo examples
	public static String itemExample1 = "";
	public static String itemExample2 = "";
	public static String itemExample3 = "";
	public static String itemExample4 = "";
	
	//Alias
	public static String alias(RMCommand cmd){
		List<String> aliases = plugin.getConfig().getCommands().getAliasMap().get(cmd);
		if(aliases != null){
			if(aliases.size()>0){
				RMDebug.warning(cmd.name()+"ALIASES.SIZE:"+aliases.size()+":"+aliases.get(0));
				return aliases.get(0);
			}
		}
		RMDebug.warning("ALIASES == NULL, "+cmd.name().toLowerCase().replace("_", " "));
		return cmd.name().toLowerCase().replace("_", " ");
	}
	
	//Game section
	public static String g_Prepare = ChatColor.GOLD+"Prepare yourselves...";
	public static String g_StartMatch = ChatColor.GOLD+"ResourceMadness!";
	public static String g_SuddenDeath = "Sudden Death!";
	public static String g_SuddenDeathColorized = RMTextHelper.colorizeString(g_SuddenDeath, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE);
	
	public RMText(){
		RMDebug.warning("ONE TIME");
		init();
		initCommandList();
	}
	
	/*
	public static HashMap<RMText, String> getErrorMap() { return errorMap; }
	public static HashMap<RMText, String> getSaveMap() { return saveMap; }
	public static HashMap<RMText, String> getActionMap() { return actionMap; }
	public static HashMap<RMText, String> getSettingMap() { return settingMap; }
	public static HashMap<RMText, String> getConfigMap() { return configMap; }
	public static HashMap<RMCommand, String> getCommandMap() { return commandMap; }
	public static HashMap<RMText, String> getDescMap() { return descMap; }
	public static HashMap<RMText, String> getInfoMap() { return infoMap; }
	public static HashMap<RMText, String> getListMap() { return listMap; }
	*/
	
 	public static void initMapError(){
		textMap.put(RMTextStr.Error_NoPermissionCommand, ChatColor.RED+"You don't have permission to use this command.");
		textMap.put(RMTextStr.Error_NoPermissionAction, ChatColor.RED+"You don't have permission to use this action.");
		textMap.put(RMTextStr.Error_NoChangeLocked, ChatColor.RED+"This setting is locked. "+ChatColor.GRAY+"It cannot be changed.");
		textMap.put(RMTextStr.Error_NoOwnerCommand,  ChatColor.RED+"Only the owner can use this command.");
		textMap.put(RMTextStr.Error_NoOwnerAction,  ChatColor.RED+"Only the owner can use this action.");
		textMap.put(RMTextStr.Error_NoGamesYet, ChatColor.GRAY+"No games yet");
		textMap.put(RMTextStr.Error_NoAliasesYet, ChatColor.GRAY+"No aliases yet");
		textMap.put(RMTextStr.Error_NoTemplateYet, ChatColor.GRAY+"No templates yet");
		textMap.put(RMTextStr.Error_TeamDoesNotExist, "This team does not exist!");
		textMap.put(RMTextStr.Error_DidNotJoinAnyTeamYet, "You did not "+ChatColor.YELLOW+"join "+ChatColor.WHITE+"any "+ChatColor.YELLOW+"team "+ChatColor.WHITE+"yet.");
		textMap.put(RMTextStr.Error_CannotReadyWhileIngame, ChatColor.GRAY+"You cannot ready yourself while in a game.");
		textMap.put(RMTextStr.Error_CannotClaimFoundIngame, "You can't claim the game's "+ChatColor.YELLOW+"found items "+ChatColor.WHITE+"while you're in a game.");
		textMap.put(RMTextStr.Error_CannotClaimItemsIngame, "You can't claim your "+ChatColor.YELLOW+"items "+ChatColor.WHITE+"while you're in a game.");
		textMap.put(RMTextStr.Error_CannotClaimRewardIngame, "You can't claim your "+ChatColor.YELLOW+"reward "+ChatColor.WHITE+"while you're in a game.");
		textMap.put(RMTextStr.Error_MustBeIngameCommand, "You must be in a game to use this command.");
		textMap.put(RMTextStr.Error_MustBeIngameAction, "You must be in a game to use this action.");
		textMap.put(RMTextStr.Error_MustBeIngameChatWorld, "You must be in a game to use "+ChatColor.YELLOW+"world "+ChatColor.WHITE+"chat.");
		textMap.put(RMTextStr.Error_MustBeIngameChatGame, "You must be in a game to use "+ChatColor.YELLOW+"game "+ChatColor.WHITE+"chat.");
		textMap.put(RMTextStr.Error_MustBeIngameChatTeam, "You must be in a game to use "+ChatColor.YELLOW+"team "+ChatColor.WHITE+"chat.");
		textMap.put(RMTextStr.Error_ItemsDoNotExist, "These items do not exist!");
	}
	
	public static void initMapSave(){
		textMap.put(RMTextStr.Save_Saving, ChatColor.RED+"Saving...");
		textMap.put(RMTextStr.Save_Success, ChatColor.GREEN+"Data was saved successfully.");
		textMap.put(RMTextStr.Save_Fail, ChatColor.RED+"Data was not saved properly!");
		textMap.put(RMTextStr.Save_NoData, ChatColor.GRAY+"No data to save.!");
	}
	
	public static void initMapAction(){
		textMap.put(RMTextStr.Action_Add, "Left click a game block to "+ChatColor.YELLOW+"add "+ChatColor.WHITE+"a new game.");
		textMap.put(RMTextStr.Action_Remove, "Left click a game block to "+ChatColor.GRAY+"remove "+ChatColor.WHITE+"a game.");
		textMap.put(RMTextStr.Action_InfoFound, "Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about the game's "+ChatColor.YELLOW+"found items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_Info, "Left click a game block to get "+ChatColor.YELLOW+"info"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SettingsReset, "Left click a game block to "+ChatColor.YELLOW+"reset "+ChatColor.WHITE+"settings.");
		textMap.put(RMTextStr.Action_Settings, "Left click a game block to get "+ChatColor.YELLOW+"settings"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ModeFilter, "Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"filter"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ModeReward, "Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"reward"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ModeTools, "Left click a game block to change the "+ChatColor.YELLOW+"interface mode "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"tools"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ModeCycle, "Left click a game block to "+ChatColor.YELLOW+"cycle "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"interface mode"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_Join, "Left click a team block to "+ChatColor.YELLOW+"join "+ChatColor.WHITE+"the team.");
		textMap.put(RMTextStr.Action_StartRandom, "Left click a game block to "+ChatColor.YELLOW+"start "+ChatColor.WHITE+"the game with "+ChatColor.GREEN+"* "+"random item(s)"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_Start, "Left click a game block to "+ChatColor.YELLOW+"start "+ChatColor.WHITE+"the game.");
		textMap.put(RMTextStr.Action_Restart, "Left click a game block to "+ChatColor.GOLD+"restart "+ChatColor.WHITE+"the game.");
		textMap.put(RMTextStr.Action_Stop, "Left click a game block to "+ChatColor.RED+"stop "+ChatColor.WHITE+"the game.");
		textMap.put(RMTextStr.Action_Pause, "Left click a game block to "+ChatColor.RED+"pause "+ChatColor.WHITE+"the game.");
		textMap.put(RMTextStr.Action_Resume, "Left click a game block to "+ChatColor.GREEN+"resume "+ChatColor.WHITE+"the game.");
		textMap.put(RMTextStr.Action_Restore, "Left click a game block to "+ChatColor.YELLOW+"restore world changes "+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_FilterInfoString, "Left click a game block to get the game's "+ChatColor.YELLOW+"filter "+ChatColor.WHITE+"as a "+ChatColor.YELLOW+"string"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_FilterInfo, "Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about the game's "+ChatColor.YELLOW+"filter"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_RewardInfoString, "Left click a game block to get the game's "+ChatColor.YELLOW+"reward "+ChatColor.WHITE+"as a "+ChatColor.YELLOW+"string"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_RewardInfo, "Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about the game's "+ChatColor.YELLOW+"reward"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ToolsInfoString, "Left click a game block to get the game's "+ChatColor.YELLOW+"tools "+ChatColor.WHITE+"as a "+ChatColor.YELLOW+"string"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ToolsInfo, "Left click a game block to get "+ChatColor.YELLOW+"info "+ChatColor.WHITE+"about the game's "+ChatColor.YELLOW+"tools"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_Filter, "Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"filter"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_Reward, "Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"reward"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_Tools, "Left click a game block to "+ChatColor.YELLOW+"modify "+ChatColor.WHITE+"the "+ChatColor.YELLOW+"tools"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_TemplateLoad, "Left click a game block to "+ChatColor.YELLOW+"load "+ChatColor.WHITE+"template "+ChatColor.GREEN+"*"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_TemplateSave, "Left click a game block to "+ChatColor.YELLOW+"save "+ChatColor.WHITE+"template "+ChatColor.GREEN+"*"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ClaimFoundChest, "Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ClaimFoundChestSelect, "Left click a game block to "+ChatColor.YELLOW+"claim found items "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"chest"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ClaimFound, "Left click a game block to "+ChatColor.YELLOW+"claim found items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ClaimItemsChest, "Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ClaimRewardChest, "Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store reward"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_ClaimToolsChest, "Left click a "+ChatColor.YELLOW+"chest "+ChatColor.WHITE+"to "+ChatColor.YELLOW+"store tools"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetMinPlayers, "Left click a game block to "+ChatColor.YELLOW+"set min players"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetMaxPlayers, "Left click a game block to "+ChatColor.YELLOW+"set max players"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetMinTeamPlayers, "Left click a game block to "+ChatColor.YELLOW+"set min team players"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetMaxTeamPlayers, "Left click a game block to "+ChatColor.YELLOW+"set max team players"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetMaxItems, "Left click a game block to "+ChatColor.YELLOW+"set max items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetTimeLimit, "Left click a game block to "+ChatColor.YELLOW+"set match time limit"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetRandom, "Left click a game block to "+ChatColor.YELLOW+"set auto randomize items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetAdvertise, "Left click a game block to "+ChatColor.YELLOW+"set advertise"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetRestore, "Left click a game block to "+ChatColor.YELLOW+"set "+Setting.autoRestoreWorld.name()+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetWarp, "Left click a game block to "+ChatColor.YELLOW+"set teleport players"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetMidgameJoin, "Left click a game block to "+ChatColor.YELLOW+"set allow midgame join"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetHealPlayer, "Left click a game block to "+ChatColor.YELLOW+"set heal player"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetClearInventory, "Left click a game block to "+ChatColor.YELLOW+"set clear player inventory"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetFoundAsReward, "Left click a game block to "+ChatColor.YELLOW+"set use found as reward"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetWarnUnequal, "Left click a game block to "+ChatColor.YELLOW+"set warn unequal items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetAllowUnequal, "Left click a game block to "+ChatColor.YELLOW+"set allow unequal items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetWarnHacked, "Left click a game block to "+ChatColor.YELLOW+"set warn hacked items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetAllowHacked, "Left click a game block to "+ChatColor.YELLOW+"set allow hacked items"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetInfiniteReward, "Left click a game block to "+ChatColor.YELLOW+"set infinite reward"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Action_SetInfiniteTools, "Left click a game block to "+ChatColor.YELLOW+"set infinite tools"+ChatColor.WHITE+".");
	}
	
	public static void initMapSettings(){
		textMap.put(RMTextStr.Setting_MaxGames, "Max games");
		textMap.put(RMTextStr.Setting_MaxGamesPerPlayer, "Max games per player");
		textMap.put(RMTextStr.Setting_MinPlayers, "Min players");
		textMap.put(RMTextStr.Setting_MaxPlayers, "Max players");
		textMap.put(RMTextStr.Setting_MinTeamPlayers, "Min team players");
		textMap.put(RMTextStr.Setting_MaxTeamPlayers, "Max team players");
		textMap.put(RMTextStr.Setting_MaxItems, "Max items");
		textMap.put(RMTextStr.Setting_TimeLimit, "Match time limit");
		textMap.put(RMTextStr.Setting_AutoRandomizeAmount, "Randomly pick "+ChatColor.GREEN+"amount "+ChatColor.WHITE+"of items every match");
		textMap.put(RMTextStr.Setting_Advertise, "Advertise game in list");
		textMap.put(RMTextStr.Setting_AutoRestoreWorld, "Auto restore world changes after match");
		textMap.put(RMTextStr.Setting_WarpToSafety, "Teleport players before and after match");
		textMap.put(RMTextStr.Setting_AllowMidgameJoin, "Allow players to join mid-game");
		textMap.put(RMTextStr.Setting_HealPlayer, "Heal players at game start");
		textMap.put(RMTextStr.Setting_ClearPlayerInventory, "Clear/return player's items at game start/finish");
		textMap.put(RMTextStr.Setting_WarnUnequal, "Warn when reward/tools can't be distributed equally");
		textMap.put(RMTextStr.Setting_AllowUnequal, "Allow reward/tools to be distributed unequally");
		textMap.put(RMTextStr.Setting_WarnHackedItems, "Warn when hacked items are added");
		textMap.put(RMTextStr.Setting_AllowHackedItems, "Allow the use of hacked items");
		textMap.put(RMTextStr.Setting_InfiniteReward, "Use infinite reward");
		textMap.put(RMTextStr.Setting_InfiniteTools, "Use infinite tools");
		textMap.put(RMTextStr.Setting_FoundAsReward, "Use the game's found items as reward");
	}
	
	public static void initMapConfig(){
		textMap.put(RMTextStr.Config_AutoSave,
				"# Backup data at regular intervals to avoid loss.\n" +
				"# Interval is measured in minutes (0 = do not autosave).");
		
		textMap.put(RMTextStr.Config_UsePermissions,
				"# If you don't use permissions just leave it at false.\n" +
				"# Supported permissions are: p3, pex, bukkit");
		
		textMap.put(RMTextStr.Config_UseRestore,
				"# Change this to false if you don't want the games to use the restore world changes functionality.\n" +
				"# It may save some memory.");
		
		textMap.put(RMTextStr.Config_ServerWide, "# These are server wide settings.");
		textMap.put(RMTextStr.Config_MaxGames, "# The maximum number of games allowed on server. (0 = unlimited)");
		textMap.put(RMTextStr.Config_MaxGamesPerPlayer, "# The maximum number of games allowed per player. (0 = unlimited)");
		textMap.put(RMTextStr.Config_DefaultSettings1,
				"# All settings from here on out are the game defaults.\n" +
				"# Using :lock after a setting locks it for all games, e.g. minPlayersPerTeam=2:lock");
		
		textMap.put(RMTextStr.Config_MinPlayers,
				"# The minimum number of players allowed per game. The Lowest number is 1 player.\n" +
				"# Only numbers higher than the amount of teams in a game will be evaluated.");
		
		textMap.put(RMTextStr.Config_MaxPlayers, "# The maximum number of players allowed per game. (0 = unlimited)");
		textMap.put(RMTextStr.Config_MinTeamPlayers, "# The minimum number of players allowed per team. The lowest number is 1 player.");
		textMap.put(RMTextStr.Config_MaxTeamPlayers, "# The maximum number of players allowed per team. (0 = unlimited)");
		textMap.put(RMTextStr.Config_TimeLimit, "# Match time limit. (0 = no time limit)");
		textMap.put(RMTextStr.Config_DefaultSettings2,
				"# The following settings can be true or false.\n" +
				"# Using :lock after true/false locks the setting for all games, e.g. allowHacked=false:lock");
		
		textMap.put(RMTextStr.Config_Advertise, "# Advertise game in list.");
		textMap.put(RMTextStr.Config_AutoRestoreWorld, "# Auto restore world changes after match.");
		textMap.put(RMTextStr.Config_WarpToSafety, "#T eleport players before and after match.");
		textMap.put(RMTextStr.Config_AllowMidgameJoin, "# Allow players to join mid-game.");
		textMap.put(RMTextStr.Config_HealPlayer, "# Heal players at game start");
		textMap.put(RMTextStr.Config_ClearPlayerInventory, "# Clear/return player's items at game start/finish.");
		textMap.put(RMTextStr.Config_FoundAsReward, "# Use the game's found items as reward.");
		textMap.put(RMTextStr.Config_WarnUnequal, "# Warn when reward/tools can't be distributed equally.");
		textMap.put(RMTextStr.Config_AllowUnequal, "# Allow reward/tools to be distributed unequally.");
		textMap.put(RMTextStr.Config_WarnHackedItems, "# Warn when hacked items are added. Only the game's owner gets a warning.");
		textMap.put(RMTextStr.Config_AllowHackedItems, "# Allow the use of hacked items.");
		textMap.put(RMTextStr.Config_InfiniteReward, "# Use infinite reward");
		textMap.put(RMTextStr.Config_InfiniteTools, "# Use infinite tools");
		
		textMap.put(RMTextStr.Config_Aliases,
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
				"# Alias list:");
	}
	
	public static void initMapDesc(){
		textMap.put(RMTextStr.Desc_Page, ChatColor.GRAY+"(Page *1 of *2)");
		textMap.put(RMTextStr.Desc_GrayGreenOptional, ChatColor.GRAY+"Gray"+ChatColor.WHITE+"/"+ChatColor.GREEN+"green "+ChatColor.WHITE+"text is optional.");
		textMap.put(RMTextStr.Desc_Add, ChatColor.WHITE+"Create a new game.");
		textMap.put(RMTextStr.Desc_Remove, ChatColor.WHITE+"Remove an existing game.");
		textMap.put(RMTextStr.Desc_List, ChatColor.WHITE+"List games.");
		textMap.put(RMTextStr.Desc_Commands, ChatColor.WHITE+"List commands and aliases.");
		textMap.put(RMTextStr.Desc_Info, ChatColor.WHITE+"Show *.");
		textMap.put(RMTextStr.Desc_Settings, ChatColor.WHITE+"Show* settings.");
		textMap.put(RMTextStr.Desc_Set, ChatColor.WHITE+"Set various game related settings.");
		textMap.put(RMTextStr.Desc_Mode, ChatColor.WHITE+"Change filter mode.");
		textMap.put(RMTextStr.Desc_Filter, ChatColor.WHITE+"Add items to filter.");
		textMap.put(RMTextStr.Desc_Reward, ChatColor.WHITE+"Add reward items.");
		textMap.put(RMTextStr.Desc_Tools, ChatColor.WHITE+"Add tools items.");
		textMap.put(RMTextStr.Desc_Template, ChatColor.WHITE+"* templates.");
		textMap.put(RMTextStr.Desc_TemplateList, ChatColor.WHITE+"Show list of templates.");
		textMap.put(RMTextStr.Desc_TemplateLoad, ChatColor.WHITE+"Load a template.");
		textMap.put(RMTextStr.Desc_TemplateSave, ChatColor.WHITE+"Save a template.");
		textMap.put(RMTextStr.Desc_TemplateRemove, ChatColor.WHITE+"Remove a template.");
		textMap.put(RMTextStr.Desc_Start, ChatColor.WHITE+"Start a game. Randomize with "+ChatColor.GREEN+"amount"+ChatColor.WHITE+".");
		textMap.put(RMTextStr.Desc_Stop, ChatColor.WHITE+"* a game.");
		textMap.put(RMTextStr.Desc_Pause, ChatColor.WHITE+"* a game.");
		textMap.put(RMTextStr.Desc_Restore, ChatColor.WHITE+"Restore game world changes.");
		textMap.put(RMTextStr.Desc_Join, ChatColor.WHITE+"Join a team.");
		textMap.put(RMTextStr.Desc_Quit, ChatColor.WHITE+"Quit a team.");
		textMap.put(RMTextStr.Desc_Ready, ChatColor.WHITE+"Ready yourself.");
		textMap.put(RMTextStr.Desc_Chat, ChatColor.WHITE+"* chat.");
		textMap.put(RMTextStr.Desc_ChatWorld, ChatColor.WHITE+"Regular world chat.");
		textMap.put(RMTextStr.Desc_ChatGame, ChatColor.WHITE+"Chat with all teams in the game.");
		textMap.put(RMTextStr.Desc_ChatTeam, ChatColor.WHITE+"Chat with your team.");
		textMap.put(RMTextStr.Desc_Items, ChatColor.WHITE+"Get which items you need to gather.");
		textMap.put(RMTextStr.Desc_Item, ChatColor.WHITE+"Get the item's name or id.");
		textMap.put(RMTextStr.Desc_Claim, ChatColor.WHITE+"Claim * to inventory or chest.");
		textMap.put(RMTextStr.Desc_Example_ChatWorld, ChatColor.WHITE+"Switch to world chat.");
		textMap.put(RMTextStr.Desc_Example_ChatWorldMessage, ChatColor.WHITE+"Message everyone on the server.");
		textMap.put(RMTextStr.Desc_Example_ChatGame, ChatColor.WHITE+"Switch to game chat.");
		textMap.put(RMTextStr.Desc_Example_ChatGameMessage, ChatColor.WHITE+"Message all teams in the game.");
		textMap.put(RMTextStr.Desc_Example_ChatTeam, ChatColor.WHITE+"Switch to team chat.");
		textMap.put(RMTextStr.Desc_Example_ChatTeamMessage, ChatColor.WHITE+"Message your team.");
	}
	
	public static void initMapMisc(){
		//no caps
		textMap.put(RMTextStr.Misc_FilterTypeAll, "all");
		textMap.put(RMTextStr.Misc_FilterTypeBlock, "block");
		textMap.put(RMTextStr.Misc_FilterTypeItem, "item");
		textMap.put(RMTextStr.Misc_Amount, "amount");
		textMap.put(RMTextStr.Misc_Stack, "stack");
		textMap.put(RMTextStr.Misc_AppName, "ResourceMadness");
		textMap.put(RMTextStr.Misc_Id, "id");
		textMap.put(RMTextStr.Misc_Page, "page");
		textMap.put(RMTextStr.Misc_RM, "/rm");
		textMap.put(RMTextStr.Misc_Examples, "examples");
		textMap.put(RMTextStr.Misc_TeamIdColor, "team(id/color)");
		textMap.put(RMTextStr.Misc_ItemsId, "items(id)");
		textMap.put(RMTextStr.Misc_ItemIdName, "items(id/name)");
		textMap.put(RMTextStr.Misc_Template, "template");
		textMap.put(RMTextStr.Misc_ChatMessage, "message");
	}
	
	public static void initMapList(){
		textMap.put(RMTextStr.List_ListId, "Id");
		textMap.put(RMTextStr.List_ListOwner, "Owner");
		textMap.put(RMTextStr.List_ListPlayers, "Players");
		textMap.put(RMTextStr.List_ListTimeLimit, "TimeLimit");
		textMap.put(RMTextStr.List_ListInGame, "inGame");
		textMap.put(RMTextStr.List_ListInTeam, "inTeam");
		textMap.put(RMTextStr.List_ListTeams, "Teams");
		textMap.put(RMTextStr.List_TemplateListFilter, "Filter");
		textMap.put(RMTextStr.List_TemplateListReward, "Reward");
		textMap.put(RMTextStr.List_TemplateListTools, "Tools");
		textMap.put(RMTextStr.List_TemplateListTotal, "Total");
	}
	
	public static void initMapCommands(){
		textMap.put(RMTextStr.Command_Add, "add");
		textMap.put(RMTextStr.Command_Remove, "remove");
		textMap.put(RMTextStr.Command_List, "list");
		textMap.put(RMTextStr.Command_Commands, "commands");
		textMap.put(RMTextStr.Command_Info, "info");
		textMap.put(RMTextStr.Command_InfoFound, "found");
		textMap.put(RMTextStr.Command_InfoClaim, "claim");
		textMap.put(RMTextStr.Command_InfoItems, "items");
		textMap.put(RMTextStr.Command_InfoReward, "reward");
		textMap.put(RMTextStr.Command_InfoTools, "tools");
		textMap.put(RMTextStr.Command_Settings, "settings");
		textMap.put(RMTextStr.Command_SettingsReset, "reset");
		textMap.put(RMTextStr.Command_Set, "set");
		textMap.put(RMTextStr.Command_SetMinPlayers, "minplayers");
		textMap.put(RMTextStr.Command_SetMaxPlayers, "maxplayers");
		textMap.put(RMTextStr.Command_SetMinTeamPlayers, "minteamplayers");
		textMap.put(RMTextStr.Command_SetMaxTeamPlayers, "maxteamplayers");
		textMap.put(RMTextStr.Command_SetTimeLimit, "timelimit");
		textMap.put(RMTextStr.Command_SetRandom, "random");
		textMap.put(RMTextStr.Command_SetAdvertise, "advertise");
		textMap.put(RMTextStr.Command_SetRestore, "restore");
		textMap.put(RMTextStr.Command_SetWarp, "warp");
		textMap.put(RMTextStr.Command_SetMidgameJoin, "midgamejoin");
		textMap.put(RMTextStr.Command_SetHealPlayer, "healplayer");
		textMap.put(RMTextStr.Command_SetClearInventory, "clearinventory");
		textMap.put(RMTextStr.Command_SetFoundAsReward, "foundasreward");
		textMap.put(RMTextStr.Command_SetWarnUnequal, "warnunequal");
		textMap.put(RMTextStr.Command_SetAllowUnequal, "allowunequal");
		textMap.put(RMTextStr.Command_SetWarnHacked, "warnhacked");
		textMap.put(RMTextStr.Command_SetAllowHacked, "allowhacked");
		textMap.put(RMTextStr.Command_SetInfiniteReward, "infinitereward");
		textMap.put(RMTextStr.Command_SetInfiniteTools, "infinitetools");
		textMap.put(RMTextStr.Command_Mode, "mode");
		textMap.put(RMTextStr.Command_ModeFilter, "filter");
		textMap.put(RMTextStr.Command_ModeReward, "reward");
		textMap.put(RMTextStr.Command_ModeTools, "tools");
		textMap.put(RMTextStr.Command_Filter, "filter");
		textMap.put(RMTextStr.Command_FilterRandom, "random");
		textMap.put(RMTextStr.Command_Reward, "reward");
		textMap.put(RMTextStr.Command_Tools, "tools");
		textMap.put(RMTextStr.Command_FilterRewardToolsInfo, "info");
		textMap.put(RMTextStr.Command_FilterRewardToolsInfoString, "string");
		textMap.put(RMTextStr.Command_FilterRewardToolsAdd, "add");
		textMap.put(RMTextStr.Command_FilterRewardToolsSubtract, "subtract");
		textMap.put(RMTextStr.Command_FilterRewardToolsClear, "clear");
		textMap.put(RMTextStr.Command_Template, "template");
		textMap.put(RMTextStr.Command_TemplateList, "list");
		textMap.put(RMTextStr.Command_TemplateLoad, "load");
		textMap.put(RMTextStr.Command_TemplateSave, "save");
		textMap.put(RMTextStr.Command_TemplateRemove, "remove");
		textMap.put(RMTextStr.Command_Start, "start");
		textMap.put(RMTextStr.Command_Stop, "stop");
		textMap.put(RMTextStr.Command_Pause, "pause");
		textMap.put(RMTextStr.Command_Resume, "resume");
		textMap.put(RMTextStr.Command_Join, "join");
		textMap.put(RMTextStr.Command_Quit, "quit");
		textMap.put(RMTextStr.Command_Ready, "ready");
		textMap.put(RMTextStr.Command_Items, "items");
		textMap.put(RMTextStr.Command_Item, "item");
		textMap.put(RMTextStr.Command_Restore, "restore");
		textMap.put(RMTextStr.Command_Chat, "chat");
		textMap.put(RMTextStr.Command_ChatWorld, "world");
		textMap.put(RMTextStr.Command_ChatGame, "game");
		textMap.put(RMTextStr.Command_ChatTeam, "team");
		textMap.put(RMTextStr.Command_Claim, "claim");
		textMap.put(RMTextStr.Command_ClaimFound, "found");
		textMap.put(RMTextStr.Command_ClaimItems, "items");
		textMap.put(RMTextStr.Command_ClaimReward, "reward");
		textMap.put(RMTextStr.Command_ClaimTools, "tools");
		textMap.put(RMTextStr.Command_ClaimFoundItemsRewardToolsChest, "chest");
		textMap.put(RMTextStr.Command_Save, "save");
	}
	
	public static void init(){
		initMapError();
		initMapSave();
		initMapAction();
		initMapSettings();
		initMapConfig();
		initMapDesc();
		initMapMisc();
		initMapList();
		initMapCommands();
		
		initText();
	}
	
	public static void initText(){
		//Error
		e_NoPermissionCommand = textMap.get(RMTextStr.Error_NoPermissionCommand);
		e_NoPermissionAction = textMap.get(RMTextStr.Error_NoPermissionAction);
		e_NoChangeLocked = textMap.get(RMTextStr.Error_NoChangeLocked);
		e_NoOwnerCommand = textMap.get(RMTextStr.Error_NoOwnerCommand);
		e_NoOwnerAction = textMap.get(RMTextStr.Error_NoOwnerAction);
		e_NoGamesYet = textMap.get(RMTextStr.Error_NoGamesYet);
		e_NoAliasesYet = textMap.get(RMTextStr.Error_NoAliasesYet);
		e_NoTemplateYet = textMap.get(RMTextStr.Error_NoTemplateYet);
		e_TeamDoesNotExist = textMap.get(RMTextStr.Error_TeamDoesNotExist);
		e_DidNotJoinAnyTeamYet = textMap.get(RMTextStr.Error_DidNotJoinAnyTeamYet);
		e_CannotReadyWhileIngame = textMap.get(RMTextStr.Error_CannotReadyWhileIngame);
		e_CannotClaimFoundIngame = textMap.get(RMTextStr.Error_CannotClaimFoundIngame);
		e_CannotClaimItemsIngame = textMap.get(RMTextStr.Error_CannotClaimItemsIngame);
		e_CannotClaimRewardIngame = textMap.get(RMTextStr.Error_CannotClaimRewardIngame);
		e_MustBeIngameCommand = textMap.get(RMTextStr.Error_MustBeIngameCommand);
		e_MustBeIngameAction = textMap.get(RMTextStr.Error_MustBeIngameAction);
		e_MustBeIngameChatWorld = textMap.get(RMTextStr.Error_MustBeIngameChatWorld);
		e_MustBeIngameChatGame = textMap.get(RMTextStr.Error_MustBeIngameChatGame);
		e_MustBeIngameChatTeam = textMap.get(RMTextStr.Error_MustBeIngameChatTeam);
		e_ItemsDoNotExist = textMap.get(RMTextStr.Error_ItemsDoNotExist);
		
		//Save
		save_Saving = textMap.get(RMTextStr.Save_Saving);
		save_Success = textMap.get(RMTextStr.Save_Success);
		save_Fail = textMap.get(RMTextStr.Save_Fail);
		save_NoData = textMap.get(RMTextStr.Save_NoData);
		
		//Action
		a_Add = textMap.get(RMTextStr.Action_Add);
		a_Remove = textMap.get(RMTextStr.Action_Remove);
		a_InfoFound = textMap.get(RMTextStr.Action_InfoFound);
		a_Info = textMap.get(RMTextStr.Action_Info);
		a_SettingsReset = textMap.get(RMTextStr.Action_SettingsReset);
		a_Settings = textMap.get(RMTextStr.Action_Settings);
		a_ModeFilter = textMap.get(RMTextStr.Action_ModeFilter);
		a_ModeReward = textMap.get(RMTextStr.Action_ModeReward);
		a_ModeTools = textMap.get(RMTextStr.Action_ModeTools);
		a_ModeCycle = textMap.get(RMTextStr.Action_ModeCycle);
		a_Join = textMap.get(RMTextStr.Action_Join);
		a_StartRandom = textMap.get(RMTextStr.Action_StartRandom);
		a_Start = textMap.get(RMTextStr.Action_Start);
		a_Restart = textMap.get(RMTextStr.Action_Restart);
		a_Stop = textMap.get(RMTextStr.Action_Stop);
		a_Pause = textMap.get(RMTextStr.Action_Pause);
		a_Resume = textMap.get(RMTextStr.Action_Resume);
		a_Restore = textMap.get(RMTextStr.Action_Restore);
		a_FilterInfoString = textMap.get(RMTextStr.Action_FilterInfoString);
		a_FilterInfo = textMap.get(RMTextStr.Action_FilterInfo);
		a_RewardInfoString = textMap.get(RMTextStr.Action_RewardInfoString);
		a_RewardInfo = textMap.get(RMTextStr.Action_RewardInfo);
		a_ToolsInfoString = textMap.get(RMTextStr.Action_ToolsInfoString);
		a_ToolsInfo = textMap.get(RMTextStr.Action_ToolsInfo);
		a_Filter = textMap.get(RMTextStr.Action_Filter);
		a_Reward = textMap.get(RMTextStr.Action_Reward);
		a_Tools = textMap.get(RMTextStr.Action_Tools);
		a_TemplateLoad = textMap.get(RMTextStr.Action_TemplateLoad);
		a_TemplateSave = textMap.get(RMTextStr.Action_TemplateSave);
		a_ClaimFoundChest = textMap.get(RMTextStr.Action_ClaimFoundChest);
		a_ClaimFoundChestSelect = textMap.get(RMTextStr.Action_ClaimFoundChestSelect);
		a_ClaimFound = textMap.get(RMTextStr.Action_ClaimFound);
		a_ClaimItemsChest = textMap.get(RMTextStr.Action_ClaimItemsChest);
		a_ClaimRewardChest = textMap.get(RMTextStr.Action_ClaimRewardChest);
		a_ClaimToolsChest = textMap.get(RMTextStr.Action_ClaimToolsChest);
		a_SetMinPlayers = textMap.get(RMTextStr.Action_SetMinPlayers);
		a_SetMaxPlayers = textMap.get(RMTextStr.Action_SetMaxPlayers);
		a_SetMinTeamPlayers = textMap.get(RMTextStr.Action_SetMinTeamPlayers);
		a_SetMaxTeamPlayers = textMap.get(RMTextStr.Action_SetMaxTeamPlayers);
		//a_SetMaxItems = textMap.get(RMTextStr.Action_SetMaxItems);
		a_SetTimeLimit = textMap.get(RMTextStr.Action_SetTimeLimit);
		a_SetRandom = textMap.get(RMTextStr.Action_SetRandom);
		a_SetAdvertise = textMap.get(RMTextStr.Action_SetAdvertise);
		a_SetRestore = textMap.get(RMTextStr.Action_SetRestore);
		a_SetWarp = textMap.get(RMTextStr.Action_SetWarp);
		a_SetMidgameJoin = textMap.get(RMTextStr.Action_SetMidgameJoin);
		a_SetHealPlayer = textMap.get(RMTextStr.Action_SetHealPlayer);
		a_SetClearInventory = textMap.get(RMTextStr.Action_SetClearInventory);
		a_SetFoundAsReward = textMap.get(RMTextStr.Action_SetFoundAsReward);
		a_SetWarnUnequal = textMap.get(RMTextStr.Action_SetWarnUnequal);
		a_SetAllowUnequal = textMap.get(RMTextStr.Action_SetAllowUnequal);
		a_SetWarnHacked = textMap.get(RMTextStr.Action_SetWarnHacked);
		a_SetAllowHacked = textMap.get(RMTextStr.Action_SetAllowHacked);
		a_SetInfiniteReward = textMap.get(RMTextStr.Action_SetInfiniteReward);
		a_SetInfiniteTools = textMap.get(RMTextStr.Action_SetInfiniteTools);
		
		//Settings
		s_MaxGames = textMap.get(RMTextStr.Setting_MaxGames);
		s_MaxGamesPerPlayer = textMap.get(RMTextStr.Setting_MaxGamesPerPlayer);
		s_MinPlayers = textMap.get(RMTextStr.Setting_MinPlayers);
		s_MaxPlayers = textMap.get(RMTextStr.Setting_MaxPlayers);
		s_MinTeamPlayers = textMap.get(RMTextStr.Setting_MinTeamPlayers);
		s_MaxTeamPlayers = textMap.get(RMTextStr.Setting_MaxTeamPlayers);
		//s_MaxItems = textMap.get(RMTextStr.Setting_MaxItems);
		s_TimeLimit = textMap.get(RMTextStr.Setting_TimeLimit);
		s_AutoRandomizeAmount = textMap.get(RMTextStr.Setting_AutoRandomizeAmount);
		s_Advertise = textMap.get(RMTextStr.Setting_Advertise);
		s_AutoRestoreWorld = textMap.get(RMTextStr.Setting_AutoRestoreWorld);
		s_WarpToSafety = textMap.get(RMTextStr.Setting_WarpToSafety);
		s_AllowMidgameJoin = textMap.get(RMTextStr.Setting_AllowMidgameJoin);
		s_HealPlayer = textMap.get(RMTextStr.Setting_HealPlayer);
		s_ClearPlayerInventory = textMap.get(RMTextStr.Setting_ClearPlayerInventory);
		s_WarnUnequal = textMap.get(RMTextStr.Setting_WarnUnequal);
		s_AllowUnequal = textMap.get(RMTextStr.Setting_AllowUnequal);
		s_WarnHackedItems = textMap.get(RMTextStr.Setting_WarnHackedItems);
		s_AllowHackedItems = textMap.get(RMTextStr.Setting_AllowHackedItems);
		s_InfiniteReward = textMap.get(RMTextStr.Setting_InfiniteReward);
		s_InfiniteTools = textMap.get(RMTextStr.Setting_InfiniteTools);
		s_FoundAsReward = textMap.get(RMTextStr.Setting_FoundAsReward);
		
		//Config
		config_AutoSave = textMap.get(RMTextStr.Config_AutoSave);
		config_UsePermissions = textMap.get(RMTextStr.Config_UsePermissions);
		config_UseRestore = textMap.get(RMTextStr.Config_UseRestore);
		config_ServerWide = textMap.get(RMTextStr.Config_ServerWide);
		config_MaxGames = textMap.get(RMTextStr.Config_MaxGames);
		config_MaxGamesPerPlayer = textMap.get(RMTextStr.Config_MaxGamesPerPlayer);
		config_DefaultSettings1 = textMap.get(RMTextStr.Config_MaxGamesPerPlayer);
		config_MinPlayers = textMap.get(RMTextStr.Config_MinPlayers);
		config_MaxPlayers = textMap.get(RMTextStr.Config_MaxPlayers);
		config_MinTeamPlayers = textMap.get(RMTextStr.Config_MinTeamPlayers);
		config_MaxTeamPlayers = textMap.get(RMTextStr.Config_MinTeamPlayers);
		config_TimeLimit = textMap.get(RMTextStr.Config_TimeLimit);
		config_DefaultSettings2 = textMap.get(RMTextStr.Config_DefaultSettings2);
		config_Advertise = textMap.get(RMTextStr.Config_Advertise);
		config_AutoRestoreWorld = textMap.get(RMTextStr.Config_AutoRestoreWorld);
		config_WarpToSafety = textMap.get(RMTextStr.Config_WarpToSafety);
		config_AllowMidgameJoin = textMap.get(RMTextStr.Config_AllowMidgameJoin);
		config_HealPlayer = textMap.get(RMTextStr.Config_HealPlayer);
		config_ClearPlayerInventory = textMap.get(RMTextStr.Config_ClearPlayerInventory);
		config_FoundAsReward = textMap.get(RMTextStr.Config_FoundAsReward);
		config_WarnUnequal = textMap.get(RMTextStr.Config_WarnUnequal);
		config_AllowUnequal = textMap.get(RMTextStr.Config_AllowUnequal);
		config_WarnHackedItems = textMap.get(RMTextStr.Config_AllowUnequal);
		config_AllowHackedItems = textMap.get(RMTextStr.Config_AllowHackedItems);
		config_InfiniteReward = textMap.get(RMTextStr.Config_AllowHackedItems);
		config_InfiniteTools = textMap.get(RMTextStr.Config_AllowHackedItems);
		config_Aliases = textMap.get(RMTextStr.Config_Aliases);
		
		//Description
		d_Page = textMap.get(RMTextStr.Desc_Page);
		d_GrayGreenOptional = textMap.get(RMTextStr.Desc_GrayGreenOptional);
		d_Add = textMap.get(RMTextStr.Desc_Add);
		d_Remove = textMap.get(RMTextStr.Desc_Remove);
		d_List = textMap.get(RMTextStr.Desc_List);
		d_Commands = textMap.get(RMTextStr.Desc_Commands);
		d_Info = textMap.get(RMTextStr.Desc_Info);
		d_Settings = textMap.get(RMTextStr.Desc_Settings);
		d_Set = textMap.get(RMTextStr.Desc_Set);
		d_Mode = textMap.get(RMTextStr.Desc_Mode);
		d_Filter = textMap.get(RMTextStr.Desc_Filter);
		d_Reward = textMap.get(RMTextStr.Desc_Reward);
		d_Tools = textMap.get(RMTextStr.Desc_Tools);
		d_Template = textMap.get(RMTextStr.Desc_Template);
		d_TemplateList = textMap.get(RMTextStr.Desc_TemplateList);
		d_TemplateLoad = textMap.get(RMTextStr.Desc_TemplateLoad);
		d_TemplateSave = textMap.get(RMTextStr.Desc_TemplateSave);
		d_TemplateRemove = textMap.get(RMTextStr.Desc_TemplateRemove);
		d_Start = textMap.get(RMTextStr.Desc_Start);
		d_Stop = textMap.get(RMTextStr.Desc_Stop);
		d_Pause = textMap.get(RMTextStr.Desc_Pause);
		d_Restore = textMap.get(RMTextStr.Desc_Restore);
		d_Join = textMap.get(RMTextStr.Desc_Join);
		d_Quit = textMap.get(RMTextStr.Desc_Quit);
		d_Ready = textMap.get(RMTextStr.Desc_Ready);
		d_Chat = textMap.get(RMTextStr.Desc_Chat);
		d_ChatWorld = textMap.get(RMTextStr.Desc_ChatWorld);
		d_ChatGame = textMap.get(RMTextStr.Desc_ChatGame);
		d_ChatTeam = textMap.get(RMTextStr.Desc_ChatTeam);
		d_Items = textMap.get(RMTextStr.Desc_Items);
		d_Item = textMap.get(RMTextStr.Desc_Item);
		d_Claim = textMap.get(RMTextStr.Desc_Claim);
		d_Example_ChatWorld = textMap.get(RMTextStr.Desc_Example_ChatWorld);
		d_Example_ChatWorldMessage = textMap.get(RMTextStr.Desc_Example_ChatWorldMessage);
		d_Example_ChatGame = textMap.get(RMTextStr.Desc_Example_ChatGame);
		d_Example_ChatGameMessage = textMap.get(RMTextStr.Desc_Example_ChatGameMessage);
		d_Example_ChatTeam = textMap.get(RMTextStr.Desc_Example_ChatTeam);
		d_Example_ChatTeamMessage = textMap.get(RMTextStr.Desc_Example_ChatTeamMessage);
	
		//List
		l_ListId = textMap.get(RMTextStr.List_ListId);
		l_ListOwner = textMap.get(RMTextStr.List_ListOwner);
		l_ListPlayers = textMap.get(RMTextStr.List_ListPlayers);
		l_ListTimeLimit = textMap.get(RMTextStr.List_ListTimeLimit);
		l_ListInGame = textMap.get(RMTextStr.List_ListInGame);
		l_ListInTeam = textMap.get(RMTextStr.List_ListInTeam);
		l_ListTeams = textMap.get(RMTextStr.List_ListTeams);

		l_TemplateListFilter = textMap.get(RMTextStr.List_TemplateListFilter);
		l_TemplateListReward = textMap.get(RMTextStr.List_TemplateListReward);
		l_TemplateListTools = textMap.get(RMTextStr.List_TemplateListTools);
		l_TemplateListTotal = textMap.get(RMTextStr.List_TemplateListTotal);
		
		//Misc
		//no caps
		m_FilterTypeAll = textMap.get(RMTextStr.Misc_FilterTypeAll);
		m_FilterTypeBlock = textMap.get(RMTextStr.Misc_FilterTypeBlock);
		m_FilterTypeItem = textMap.get(RMTextStr.Misc_FilterTypeItem);
		m_Amount = textMap.get(RMTextStr.Misc_Amount);
		m_Stack = textMap.get(RMTextStr.Misc_Stack);
		
		m_appName = textMap.get(RMTextStr.Misc_AppName);
		m_Id = textMap.get(RMTextStr.Misc_Id);
		m_Page = textMap.get(RMTextStr.Misc_Page);
		m_RM = textMap.get(RMTextStr.Misc_RM);
		m_Examples = textMap.get(RMTextStr.Misc_Examples);
		m_TeamIdColor = textMap.get(RMTextStr.Misc_TeamIdColor);
		m_ItemsId = textMap.get(RMTextStr.Misc_ItemsId);
		m_ItemIdName = textMap.get(RMTextStr.Misc_ItemIdName);
		m_Template = textMap.get(RMTextStr.Misc_Template);
		m_ChatMessage = textMap.get(RMTextStr.Misc_ChatMessage);
		
		//Commands
		c_Add = textMap.get(RMTextStr.Command_Add);
		c_Remove = textMap.get(RMTextStr.Command_Remove);
		c_List = textMap.get(RMTextStr.Command_List);
		c_Commands = textMap.get(RMTextStr.Command_Commands);
		c_Info = textMap.get(RMTextStr.Command_Info);
		c_InfoFound = textMap.get(RMTextStr.Command_InfoFound);
		c_InfoClaim = textMap.get(RMTextStr.Command_InfoClaim);
		c_InfoItems = textMap.get(RMTextStr.Command_InfoItems);
		c_InfoReward = textMap.get(RMTextStr.Command_InfoReward);
		c_InfoTools = textMap.get(RMTextStr.Command_InfoTools);
		c_Settings = textMap.get(RMTextStr.Command_Settings);
		c_SettingsReset = textMap.get(RMTextStr.Command_SettingsReset);
		c_Set = textMap.get(RMTextStr.Command_Set);
		c_SetMinPlayers = textMap.get(RMTextStr.Command_SetMinPlayers);
		c_SetMaxPlayers = textMap.get(RMTextStr.Command_SetMaxPlayers);
		c_SetMinTeamPlayers = textMap.get(RMTextStr.Command_SetMinTeamPlayers);
		c_SetMaxTeamPlayers = textMap.get(RMTextStr.Command_SetMaxTeamPlayers);
		c_SetTimeLimit = textMap.get(RMTextStr.Command_SetTimeLimit);
		c_SetRandom = textMap.get(RMTextStr.Command_SetRandom);
		c_SetAdvertise = textMap.get(RMTextStr.Command_SetAdvertise);
		c_SetRestore = textMap.get(RMTextStr.Command_SetRestore);
		c_SetWarp = textMap.get(RMTextStr.Command_SetWarp);
		c_SetMidgameJoin = textMap.get(RMTextStr.Command_SetMidgameJoin);
		c_SetHealPlayer = textMap.get(RMTextStr.Command_SetHealPlayer);
		c_SetClearInventory = textMap.get(RMTextStr.Command_SetClearInventory);
		c_SetFoundAsReward = textMap.get(RMTextStr.Command_SetFoundAsReward);
		c_SetWarnUnequal = textMap.get(RMTextStr.Command_SetWarnUnequal);
		c_SetAllowUnequal = textMap.get(RMTextStr.Command_SetAllowUnequal);
		c_SetWarnHacked = textMap.get(RMTextStr.Command_SetWarnHacked);
		c_SetAllowHacked = textMap.get(RMTextStr.Command_SetAllowHacked);
		c_SetInfiniteReward = textMap.get(RMTextStr.Command_SetInfiniteReward);
		c_SetInfiniteTools = textMap.get(RMTextStr.Command_SetInfiniteTools);
		c_Mode = textMap.get(RMTextStr.Command_Mode);
		c_ModeFilter = textMap.get(RMTextStr.Command_ModeFilter);
		c_ModeReward = textMap.get(RMTextStr.Command_ModeReward);
		c_ModeTools = textMap.get(RMTextStr.Command_ModeTools);
		c_Filter = textMap.get(RMTextStr.Command_Filter);
		c_FilterRandom = textMap.get(RMTextStr.Command_FilterRandom);
		c_Reward = textMap.get(RMTextStr.Command_Reward);
		c_Tools = textMap.get(RMTextStr.Command_Tools);
		c_FilterRewardToolsInfo = textMap.get(RMTextStr.Command_FilterRewardToolsInfo);
		c_FilterRewardToolsInfoString = textMap.get(RMTextStr.Command_FilterRewardToolsInfoString);
		c_FilterRewardToolsAdd = textMap.get(RMTextStr.Command_FilterRewardToolsAdd);
		c_FilterRewardToolsSubtract = textMap.get(RMTextStr.Command_FilterRewardToolsSubtract);
		c_FilterRewardToolsClear = textMap.get(RMTextStr.Command_FilterRewardToolsClear);
		c_Template = textMap.get(RMTextStr.Command_Template);
		c_TemplateList = textMap.get(RMTextStr.Command_TemplateList);
		c_TemplateLoad = textMap.get(RMTextStr.Command_TemplateLoad);
		c_TemplateSave = textMap.get(RMTextStr.Command_TemplateSave);
		c_TemplateRemove = textMap.get(RMTextStr.Command_TemplateRemove);
		c_Start = textMap.get(RMTextStr.Command_Start);
		c_Stop = textMap.get(RMTextStr.Command_Stop);
		c_Pause = textMap.get(RMTextStr.Command_Pause);
		c_Resume = textMap.get(RMTextStr.Command_Resume);
		c_Join = textMap.get(RMTextStr.Command_Join);
		c_Quit = textMap.get(RMTextStr.Command_Quit);
		c_Ready = textMap.get(RMTextStr.Command_Ready);
		c_Items = textMap.get(RMTextStr.Command_Items);
		c_Item = textMap.get(RMTextStr.Command_Item);
		c_Restore = textMap.get(RMTextStr.Command_Restore);
		c_Chat = textMap.get(RMTextStr.Command_Chat);
		c_ChatWorld = textMap.get(RMTextStr.Command_ChatWorld);
		c_ChatGame = textMap.get(RMTextStr.Command_ChatGame);
		c_ChatTeam = textMap.get(RMTextStr.Command_ChatTeam);
		c_Claim = textMap.get(RMTextStr.Command_Claim);
		c_ClaimFound = textMap.get(RMTextStr.Command_ClaimFound);
		c_ClaimItems = textMap.get(RMTextStr.Command_ClaimItems);
		c_ClaimReward = textMap.get(RMTextStr.Command_ClaimReward);
		c_ClaimTools = textMap.get(RMTextStr.Command_ClaimTools);
		c_ClaimFoundItemsRewardToolsChest = textMap.get(RMTextStr.Command_ClaimFoundItemsRewardToolsChest);
		c_Save = textMap.get(RMTextStr.Command_Save);
		
		//Need to do this
		m_AllBlockItem = ChatColor.YELLOW+"/"+m_FilterTypeAll+"/"+m_FilterTypeBlock+"/"+m_FilterTypeItem;
		m_AmountStack = ChatColor.BLUE+":["+m_Amount+"/"+m_Stack+"]";
		
		///////////////////
		//RM INFO SECTION//
		///////////////////
		
		//rmInfo
		rmInfo_Add = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Add+" "+ChatColor.WHITE+d_Add;
		rmInfo_Remove = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Remove+" "+d_Remove;
		rmInfo_List = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_List+" "+ChatColor.GREEN+"["+m_Page+"] "+d_List;
		rmInfo_Commands = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Commands+" "+ChatColor.GREEN+"["+m_Page+"] "+d_Commands;
		rmInfo_Set = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+"set "+d_Set;
		rmInfo_Start = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Start+" "+ChatColor.GREEN+"["+m_Amount+"] "+d_Start;
		rmInfo_Restore = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Restore+" "+d_Restore;
		rmInfo_Join = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Join+" "+ChatColor.GREEN+"["+m_TeamIdColor+"] "+d_Join;
		rmInfo_Quit = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Quit+" "+d_Quit;
		rmInfo_Ready = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Ready+" "+d_Ready;
		rmInfo_Items = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Items+" "+d_Items;
		rmInfo_Item = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Item+" "+ChatColor.AQUA+"["+m_ItemIdName+"] "+d_Item;
		
		//rmSetInfo
		setInfo_MinPlayers = ChatColor.YELLOW+c_SetMinPlayers+" "+ChatColor.AQUA+"["+m_Amount+"] "+ChatColor.WHITE+s_MinPlayers+".";
		setInfo_MaxPlayers = ChatColor.YELLOW+c_SetMaxPlayers+" "+ChatColor.AQUA+"["+m_Amount+"] "+ChatColor.WHITE+s_MaxPlayers+".";
		setInfo_MinTeamPlayers = ChatColor.YELLOW+c_SetMinTeamPlayers+" "+ChatColor.AQUA+"["+m_Amount+"] "+ChatColor.WHITE+s_MinTeamPlayers+".";
		setInfo_MaxTeamPlayers = ChatColor.YELLOW+c_SetMaxTeamPlayers+" "+ChatColor.AQUA+"["+m_Amount+"] "+ChatColor.WHITE+s_MaxTeamPlayers+".";
		setInfo_TimeLimit = ChatColor.YELLOW+c_SetTimeLimit+" "+ChatColor.AQUA+"["+m_Amount+"] "+ChatColor.WHITE+s_TimeLimit+".";
		setInfo_Random = ChatColor.YELLOW+c_SetRandom+" "+ChatColor.AQUA+"["+m_Amount+"] "+ChatColor.WHITE+s_AutoRandomizeAmount+".";
		setInfo_Advertise = ChatColor.YELLOW+c_SetAdvertise+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_Advertise+".";
		setInfo_Restore = ChatColor.YELLOW+c_SetRestore+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_AutoRestoreWorld+".";
		setInfo_Warp = ChatColor.YELLOW+c_SetWarp+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_WarpToSafety+".";
		setInfo_MidgameJoin = ChatColor.YELLOW+c_SetMidgameJoin+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_AllowMidgameJoin+".";
		setInfo_HealPlayer = ChatColor.YELLOW+c_SetHealPlayer+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_HealPlayer+".";
		setInfo_ClearInventory = ChatColor.YELLOW+c_SetClearInventory+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_ClearPlayerInventory+".";
		setInfo_FoundAsReward = ChatColor.YELLOW+c_SetFoundAsReward+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_FoundAsReward+".";
		setInfo_WarnUnequal = ChatColor.YELLOW+c_SetWarnUnequal+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_WarnUnequal+".";
		setInfo_AllowUnequal = ChatColor.YELLOW+c_SetAllowUnequal+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_AllowUnequal+".";
		setInfo_WarnHacked = ChatColor.YELLOW+c_SetWarnHacked+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_WarnHackedItems+".";
		setInfo_AllowHacked = ChatColor.YELLOW+c_SetAllowHacked+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_AllowHackedItems+".";
		setInfo_InfiniteReward = ChatColor.YELLOW+c_SetInfiniteReward+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_InfiniteReward+".";
		setInfo_InfiniteTools = ChatColor.YELLOW+c_SetInfiniteTools+" "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+s_InfiniteTools+".";
		
		//rmFilterInfo
		filterInfo = ChatColor.GOLD+m_RM+" "+c_Filter;
		filterInfo_Set = ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		filterInfo_Random = ChatColor.YELLOW+c_FilterRandom+" "+ChatColor.GREEN+"["+m_Amount+"] "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		filterInfo_Add = ChatColor.YELLOW+c_FilterRewardToolsAdd+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		filterInfo_Subtract = ChatColor.YELLOW+c_FilterRewardToolsSubtract+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		filterInfo_Clear = ChatColor.YELLOW+c_FilterRewardToolsClear+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem;
		//rmFilterInfo examples
		filterExample_Info = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Filter+" "+ChatColor.YELLOW+c_FilterRewardToolsInfo;
		filterExample_Set = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Filter+" "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":"+m_Stack+" "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32";
		filterExample_Random = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Filter+" "+ChatColor.YELLOW+c_FilterRandom+" "+ChatColor.GREEN+"20 "+ChatColor.YELLOW+m_FilterTypeAll+ChatColor.BLUE+":100-200";
		filterExample_Add = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Filter+" "+ChatColor.YELLOW+c_FilterRewardToolsAdd+" "+ChatColor.AQUA+"20-40,1,3"+ChatColor.BLUE+":20";
		filterExample_Subtract = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Filter+" "+ChatColor.YELLOW+c_FilterRewardToolsSubtract+" "+ChatColor.AQUA+"1-10,20,288";
		filterExample_Clear1 = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Filter+" "+ChatColor.YELLOW+c_FilterRewardToolsClear+" "+ChatColor.AQUA+"1-100"+ChatColor.BLUE+":"+m_Stack;
		filterExample_Clear2 = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Filter+" "+ChatColor.YELLOW+c_FilterRewardToolsClear;
		
		//rmRewardInfo
		rewardInfo = ChatColor.GOLD+m_RM+" "+c_Reward;
		rewardInfo_Set = ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		rewardInfo_Add = ChatColor.YELLOW+c_FilterRewardToolsAdd+" "+ChatColor.AQUA+"[items(id)]"+m_AllBlockItem+m_AmountStack;
		rewardInfo_Subtract = ChatColor.YELLOW+c_FilterRewardToolsSubtract+" "+ChatColor.AQUA+"[items(id)]"+m_AllBlockItem+m_AmountStack;
		rewardInfo_Clear = ChatColor.YELLOW+c_FilterRewardToolsClear+" "+ChatColor.AQUA+"[items(id)]"+m_AllBlockItem;
		//rmRewardInfo examples
		rewardExample_Info = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Reward+" "+ChatColor.YELLOW+c_FilterRewardToolsInfo;
		rewardExample_Set = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Reward+" "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":"+m_Stack+" "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32";
		rewardExample_Add = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Reward+" "+ChatColor.YELLOW+c_FilterRewardToolsAdd+" "+ChatColor.AQUA+"20-40,1,3"+ChatColor.BLUE+":20";
		rewardExample_Subtract = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Reward+" "+ChatColor.YELLOW+c_FilterRewardToolsSubtract+" "+ChatColor.AQUA+"1-10,20,288";
		rewardExample_Clear1 = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Reward+" "+ChatColor.YELLOW+c_FilterRewardToolsClear+" "+ChatColor.AQUA+"1-100"+ChatColor.BLUE+":"+m_Stack;
		rewardExample_Clear2 = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Reward+" "+ChatColor.YELLOW+c_FilterRewardToolsClear;
		
		//rmToolsInfo
		toolsInfo = ChatColor.GOLD+m_RM+" "+c_Tools;
		toolsInfo_Set = ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		toolsInfo_Add = ChatColor.YELLOW+c_FilterRewardToolsAdd+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		toolsInfo_Subtract = ChatColor.YELLOW+c_FilterRewardToolsSubtract+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		toolsInfo_Clear = ChatColor.YELLOW+c_FilterRewardToolsClear+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem;
		//rmToolsInfo examples
		toolsExample_Info = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Tools+" "+ChatColor.YELLOW+c_FilterRewardToolsInfo;
		toolsExample_Set = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Tools+" "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":"+m_Stack+" "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32";
		toolsExample_Add = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Tools+" "+ChatColor.YELLOW+c_FilterRewardToolsAdd+" "+ChatColor.AQUA+"20-40,1,3"+ChatColor.BLUE+":20";
		toolsExample_Subtract = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Tools+" "+ChatColor.YELLOW+c_FilterRewardToolsSubtract+" "+ChatColor.AQUA+"1-10,20,288";
		toolsExample_Clear1 = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Tools+" "+ChatColor.YELLOW+c_FilterRewardToolsClear+" "+ChatColor.AQUA+"1-100"+ChatColor.BLUE+":"+m_Stack;
		toolsExample_Clear2 = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Tools+" "+ChatColor.YELLOW+c_FilterRewardToolsClear;
		
		//rmTemplateInfo
		templateInfo = ChatColor.GOLD+m_RM+" "+c_Template;
		templateInfo_List = ChatColor.YELLOW+c_TemplateList+" "+ChatColor.GREEN+"["+m_Page+"] "+d_TemplateList;
		templateInfo_Load = ChatColor.YELLOW+c_TemplateLoad+" "+ChatColor.AQUA+"["+m_Template+"] "+d_TemplateLoad;
		templateInfo_Save = ChatColor.YELLOW+c_TemplateSave+" "+ChatColor.AQUA+"["+m_Template+"] "+d_TemplateSave;
		templateInfo_Remove = ChatColor.YELLOW+c_TemplateRemove+" "+ChatColor.AQUA+"["+m_Template+"] "+d_TemplateRemove;
		//rmTemplate examples
		templateExample_List = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_TemplateList+" "+ChatColor.GREEN+"3";
		templateExample_Load = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_TemplateLoad+" "+ChatColor.AQUA+"3v3_fast";
		templateExample_Save = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_TemplateSave+" "+ChatColor.AQUA+"1v1v1v1_match";
		templateExample_Remove = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_TemplateRemove+" "+ChatColor.AQUA+"projectd";
		
		//rmClaimInfo
		claimInfo = ChatColor.GOLD+m_RM+" "+c_Claim;
		claimInfo_Found = ChatColor.YELLOW+c_ClaimFound+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		claimInfo_Items = ChatColor.YELLOW+c_ClaimItems+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		claimInfo_Reward = ChatColor.YELLOW+c_ClaimReward+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		claimInfo_Tools = ChatColor.YELLOW+c_ClaimTools+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest+" "+ChatColor.AQUA+"["+m_ItemsId+"]"+m_AllBlockItem+m_AmountStack;
		//rmClaimInfo examples
		claimExample_Found = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Claim+" "+c_ClaimFound;
		claimExample_FoundChest = ChatColor.WHITE+m_RM+" "+ChatColor.GRAY+"["+m_Id+"] "+ChatColor.YELLOW+c_Claim+" "+c_ClaimFound+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest+" "+ChatColor.AQUA+"10-20,22,24";
		claimExample_Items = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Claim+" "+c_ClaimItems+" "+ChatColor.YELLOW+m_FilterTypeBlock+ChatColor.BLUE+":64";
		claimExample_ItemsChest = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Claim+" "+c_ClaimItems+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest;
		claimExample_Reward = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Claim+" "+c_ClaimReward;
		claimExample_RewardChest = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Claim+" "+c_ClaimReward+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest+" "+ChatColor.AQUA+"50-100,200-300"+ChatColor.BLUE+":100"+ChatColor.AQUA+" 1,3,4"+ChatColor.BLUE+":10";
		claimExample_Tools = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Claim+" "+c_ClaimTools+" "+ChatColor.AQUA+"1-10,20,288"+ChatColor.BLUE+":"+m_Stack;
		claimExample_ToolsChest = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Claim+" "+c_ClaimTools+" "+ChatColor.GREEN+c_ClaimFoundItemsRewardToolsChest;
		
		//rmChatInfo
		chatInfo = ChatColor.GOLD+m_RM+" "+c_Chat;
		chatInfo_World = ChatColor.YELLOW+c_ChatWorld+" "+ChatColor.GREEN+m_ChatMessage+" "+d_ChatWorld;
		chatInfo_Game = ChatColor.YELLOW+c_ChatGame+" "+ChatColor.GREEN+m_ChatMessage+" "+d_ChatGame;
		chatInfo_Team = ChatColor.YELLOW+c_ChatTeam+" "+ChatColor.GREEN+m_ChatMessage+" "+d_ChatTeam;
		//rmChatInfo examples
		chatExample_World = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Chat+" "+c_ChatWorld+" "+d_Example_ChatWorld;
		chatExample_WorldMessage = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Chat+" "+c_ChatWorld+" "+ChatColor.GREEN+m_ChatMessage+" "+d_Example_ChatWorldMessage;
		chatExample_Game = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Chat+" "+c_ChatGame+" "+d_Example_ChatGame;
		chatExample_GameMessage = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Chat+" "+c_ChatGame+" "+ChatColor.GREEN+m_ChatMessage+" "+d_Example_ChatGameMessage;
		chatExample_Team = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Chat+" "+c_ChatTeam+" "+d_Example_ChatTeam;
		chatExample_TeamMessage = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Chat+" "+c_ChatTeam+" "+ChatColor.GREEN+m_ChatMessage+" "+d_Example_ChatTeamMessage;
		
		//rmItemInfo
		itemInfo = ChatColor.GOLD+m_RM+" "+c_Item;
		itemInfo_Arg = ChatColor.AQUA+"["+m_ItemIdName+"]";
		//rmItemInfo examples
		itemExample1 = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Item+" "+ChatColor.AQUA+"1-10 263";
		itemExample2 = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Item+" "+ChatColor.AQUA+"lava diamond";
		itemExample3 = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Item+" "+ChatColor.AQUA+"pickaxe chest 20";
		itemExample4 = ChatColor.WHITE+m_RM+" "+ChatColor.YELLOW+c_Item+" "+ChatColor.AQUA+"pickaxe,chest,20";
	}
	
	public static String get(String label){
		return "";
	}
	
	public static List<String> commandList = new ArrayList<String>();
	
	public static void initCommandList(){
		RMDebug.warning("Commands were added to CommandList");
		commandList.clear();
		commandList.add(c_Add);
		commandList.add(c_Remove);
		commandList.add(c_List);
		commandList.add(c_Commands);
		commandList.add(c_Info);
		commandList.add(c_Info+" "+c_InfoFound);
		commandList.add(c_Info+" "+c_InfoClaim);
		commandList.add(c_Info+" "+c_InfoItems);
		commandList.add(c_Info+" "+c_InfoReward);
		commandList.add(c_Info+" "+c_InfoTools);
		commandList.add(c_Settings);
		commandList.add(c_Settings+" "+c_SettingsReset);
		commandList.add(c_Set);
		commandList.add(c_Set+" "+c_SetMinPlayers);
		commandList.add(c_Set+" "+c_SetMaxPlayers);
		commandList.add(c_Set+" "+c_SetMinTeamPlayers);
		commandList.add(c_Set+" "+c_SetMaxTeamPlayers);
		commandList.add(c_Set+" "+c_SetTimeLimit);
		commandList.add(c_Set+" "+c_SetRandom);
		commandList.add(c_Set+" "+c_SetAdvertise);
		commandList.add(c_Set+" "+c_SetRestore);
		commandList.add(c_Set+" "+c_SetWarp);
		commandList.add(c_Set+" "+c_SetMidgameJoin);
		commandList.add(c_Set+" "+c_SetHealPlayer);
		commandList.add(c_Set+" "+c_SetClearInventory);
		commandList.add(c_Set+" "+c_SetFoundAsReward);
		commandList.add(c_Set+" "+c_SetWarnUnequal);
		commandList.add(c_Set+" "+c_SetAllowUnequal);
		commandList.add(c_Set+" "+c_SetWarnHacked);
		commandList.add(c_Set+" "+c_SetAllowHacked);
		commandList.add(c_Set+" "+c_SetInfiniteReward);
		commandList.add(c_Set+" "+c_SetInfiniteTools);
		commandList.add(c_Mode);
		commandList.add(c_Mode+" "+c_ModeFilter);
		commandList.add(c_Mode+" "+c_ModeReward);
		commandList.add(c_Mode+" "+c_ModeTools);
		commandList.add(c_Filter);
		commandList.add(c_Filter+" "+c_FilterRewardToolsInfo);
		commandList.add(c_Filter+" "+c_FilterRewardToolsInfo+" "+c_FilterRewardToolsInfoString);
		commandList.add(c_Filter+" "+c_FilterRandom);
		commandList.add(c_Filter+" "+c_FilterRewardToolsAdd);
		commandList.add(c_Filter+" "+c_FilterRewardToolsSubtract);
		commandList.add(c_Filter+" "+c_FilterRewardToolsClear);
		commandList.add(c_Reward);
		commandList.add(c_Reward+" "+c_FilterRewardToolsInfo);
		commandList.add(c_Reward+" "+c_FilterRewardToolsInfo+" "+c_FilterRewardToolsInfoString);
		commandList.add(c_Reward+" "+c_FilterRewardToolsAdd);
		commandList.add(c_Reward+" "+c_FilterRewardToolsSubtract);
		commandList.add(c_Reward+" "+c_FilterRewardToolsClear);
		commandList.add(c_Tools);
		commandList.add(c_Tools+" "+c_FilterRewardToolsInfo);
		commandList.add(c_Tools+" "+c_FilterRewardToolsInfo+" "+c_FilterRewardToolsInfoString);
		commandList.add(c_Tools+" "+c_FilterRewardToolsAdd);
		commandList.add(c_Tools+" "+c_FilterRewardToolsSubtract);
		commandList.add(c_Tools+" "+c_FilterRewardToolsClear);
		commandList.add(c_Template);
		commandList.add(c_Template+" "+c_TemplateList);
		commandList.add(c_Template+" "+c_TemplateLoad);
		commandList.add(c_Template+" "+c_TemplateSave);
		commandList.add(c_Template+" "+c_TemplateRemove);
		commandList.add(c_Start);
		commandList.add(c_Stop);
		commandList.add(c_Pause);
		commandList.add(c_Resume);
		commandList.add(c_Join);
		commandList.add(c_Quit);
		commandList.add(c_Ready);
		commandList.add(c_Items);
		commandList.add(c_Item);
		commandList.add(c_Restore);
		commandList.add(c_Chat);
		commandList.add(c_Chat+" "+c_ChatWorld);
		commandList.add(c_Chat+" "+c_ChatGame);
		commandList.add(c_Chat+" "+c_ChatTeam);
		commandList.add(c_Claim);
		commandList.add(c_Claim+" "+c_ClaimFound);
		commandList.add(c_Claim+" "+c_ClaimFound+" "+c_ClaimFoundItemsRewardToolsChest);
		commandList.add(c_Claim+" "+c_ClaimItems);
		commandList.add(c_Claim+" "+c_ClaimItems+" "+c_ClaimFoundItemsRewardToolsChest);
		commandList.add(c_Claim+" "+c_ClaimReward);
		commandList.add(c_Claim+" "+c_ClaimReward+" "+c_ClaimFoundItemsRewardToolsChest);
		commandList.add(c_Claim+" "+c_ClaimTools);
		commandList.add(c_Claim+" "+c_ClaimTools+" "+c_ClaimFoundItemsRewardToolsChest);
		commandList.add(c_Save);
	}
}