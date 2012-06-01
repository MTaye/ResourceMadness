package com.mtaye.ResourceMadness.setting;

public enum Setting{
	minplayers,
	maxplayers,
	minteamplayers,
	maxteamplayers,
	timelimit,
	safezone,
	playarea,
	playareatime,
	enemyradar,
	keepondeath,
	multiplier,
	random,
	password,
	advertise,
	restore,
	allowpvp,
	delaypvp,
	friendlyfire,
	healplayer,
	autoreturn,
	midgamejoin,
	showitemsleft,
	clearinventory,
	scrapfound,
	foundasreward,
	keepoverflow,
	warnunequal,
	allowunequal,
	warnhacked,
	allowhacked,
	infinitereward,
	infinitetools,
	dividereward,
	dividetools;
	
	public static int calculatePages(int entries){
		return (int)Math.ceil(values().length/(double)entries);
	}
}