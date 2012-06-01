package com.mtaye.ResourceMadness;

import java.util.HashMap;

import org.bukkit.ChatColor;

public enum PlayerDirection {
	N(0),
	NE(1),
	E(2),
	SE(3),
	S(4),
	SW(5),
	W(6),
	NW(7),
	C(8),
	NONE(9);
	
	private static String empty = Text.getLabel("team.map.empty");
	private static String player = Text.getLabel("team.map.player");
	private static String[] target = {Text.getLabel("team.map.target"), Text.getLabel("team.map.higher"), Text.getLabel("team.map.lower")};
	
	private int id;
	private String[] graphicBackup;
	private String[] graphic;
	private int altitude;
	private static int[] arrayId = new int[10];
	private static HashMap<Integer, PlayerDirection> mapId = new HashMap<Integer, PlayerDirection>(); 
	
	private PlayerDirection(int id){
		this.id = id;
	}
	
	public String getLine1(){
		return graphic[0];
	}
	
	public String getLine2(){
		return graphic[1];
	}
	
	public String getLine3(){
		return graphic[2];
	}
	
	public void setAltitude(Boolean altitude){
		if(altitude==null) this.altitude = 0;
		else if(altitude) this.altitude = 1;
		else this.altitude = 2;
		updateGraphic();
	}
	
	private void updateGraphic(){
		for(int i = 0; i<graphicBackup.length; i++){
			graphic[i] = graphicBackup[i].replace(target[0], target[altitude]);
		}
	}
	
	public PlayerDirection[] getDirections(){
		PlayerDirection[] result = new PlayerDirection[arrayId.length];
		for(Integer id : arrayId){
			result[id] = mapId.get(id);
		}
		return result;
	}
	
	static{
		for(PlayerDirection dir : values()){
			arrayId[dir.id] = dir.id;
			switch(dir){
			case N: dir.graphic = new String[]{
					empty+target[0]+empty,
					empty+player+empty,
					empty+empty+empty};
				break;
			case NE: dir.graphic = new String[]{
					empty+empty+target[0],
					empty+player+empty,
					empty+empty+empty};
				break;
			case E: dir.graphic = new String[]{
					empty+empty+empty,
					empty+player+target[0],
					empty+empty+empty};
				break;
			case SE: dir.graphic = new String[]{
					empty+empty+empty,
					empty+player+empty,
					empty+empty+target[0]};
				break;
			case S: dir.graphic = new String[]{
					empty+empty+empty,
					empty+player+empty,
					empty+target[0]+empty};
				break;
			case SW: dir.graphic = new String[]{
					empty+empty+empty,
					empty+player+empty,
					target[0]+empty+empty};
				break;
			case W: dir.graphic = new String[]{
					empty+empty+empty,
					target[0]+player+empty,
					empty+empty+empty};
				break;
			case NW: dir.graphic = new String[]{
					target[0]+empty+empty,
					empty+player+empty,
					empty+empty+empty};
				break;
			case C: dir.graphic = new String[]{
					empty+empty+empty,
					empty+player+empty,
					empty+empty+empty};
				break;
			case NONE: dir.graphic = new String[]{
					empty+empty+empty,
					empty+empty+empty,
					empty+empty+empty};
				break;
			}
			dir.graphicBackup = dir.graphic.clone();
		}
	}
}
