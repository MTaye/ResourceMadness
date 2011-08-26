package com.mtaye.ResourceMadness;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
final class RMText {
	
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
	public static String warpToSafety = "Warp players before and after match";
	public static String autoRestoreWorld = "Auto restore world changes after match";
	public static String warnHackedItems = "Warn when hacked items are added";
	public static String allowHackedItems = "Allow the use of hacked items";
	public static String keepIngame = "Keep offline players in-game";
	public static String allowMidgameJoin = "Allow players to join mid-game";
	public static String clearPlayerInventory = "Clear/return player's items at game start/finish";
	public static String warnUnequal = "Warn when award/tools can't be distributed equally";
	public static String allowUnequal = "Allow award/tools to be distributed unequally";
	public static String infiniteAward = "Use infinite award";
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
	public static String cWarpToSafety = "#Warp players before and after match.";
	public static String cWarnHackedItems = "#Warn when hacked items are added. Only the game's owner gets a warning.";
	public static String cAllowHackedItems = "#Allow the use of hacked items.";
	public static String cKeepIngame = "#Keep offline players in-game. Use this for persistent matches.";
	public static String cAllowMidgameJoin = "#Allow players to join mid-game.";
	public static String cClearPlayerInventory = "#Clear/return player's items at game start/finish.";
	public static String cWarnUnequal = "#Warn when award/tools can't be distributed equally.";
	public static String cAllowUnequal = "#Allow award/tools to be distributed unequally.";
	public static String cInfiniteAward = "#Use infinite award";
	public static String cInfiniteTools = "#Use infinite tools";
	
	public static String gStartMatch = "ResourceMadness!";
	
	private RMText(){
	}
}