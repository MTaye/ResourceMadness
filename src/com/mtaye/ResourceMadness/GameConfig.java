package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.Game.GameState;
import com.mtaye.ResourceMadness.Game.InterfaceState;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.setting.SettingLibrary;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class GameConfig extends GameSettings{
	private PartList _partList = new PartList();
	private World _world;
	private int _id;
	private String _name;
	private String _ownerName;
	private TreeMap<String, GamePlayer> _players = new TreeMap<String, GamePlayer>();
	private Filter _items = new Filter();
	private Stash _found = new Stash();
	private List<Team> _teams = new ArrayList<Team>();
	private Log _log;
	private boolean _paused = false;
	private GameState _state = GameState.SETUP;
	private InterfaceState _interface = InterfaceState.FILTER;
	private Stats _stats = new Stats();
	private BanList _banList = new BanList();
	private Money _money;
	private Cost _cost;
	private Cost _payment;
	
	public GameConfig(){
		super();
		_log = new Log();
	}
	
	public GameConfig(Config config){
		super(config);
		_log = new Log();
		getDataFrom(config);
	}
	
	public GameConfig(GameConfig config){
		super(config);
		//this.plugin = plugin;
		this._partList = config._partList;
		this._id = config._id;
		this._name = config._name;
		this._ownerName = config._ownerName;
		
		this._players = config._players;
		this._items = config._items;
		this._found = config._found;
		this._teams = config._teams;
		this._log = config._log;
		this._paused = config._paused;
		this._state = config._state;
		this._interface = config._interface;
		this._stats = config._stats;
		
		this._banList = config._banList;
		this._money = config._money;
		this._cost = config._cost;
		this._payment = config._payment;
	}
	
	//Get
	public PartList getPartList() { return _partList; }
	public World getWorld() { return _world; }
	public int getId() { return _id; }
	public String getName() { return _name; }
	public String getOwnerName() { return _ownerName; }
	public GamePlayer getOwner() { return GamePlayer.getPlayerByName(getOwnerName()); }
	
	public TreeMap<String, GamePlayer> getPlayers() { return _players; }
	public Filter getItems() { return _items; }
	public Stash getFound() { return _found; }
	public ItemStack[] getFoundArray() { return _found.getItemsArray(); }
	public List<Team> getTeams() { return _teams; }
	public Log getLog() { return _log; }
	public boolean getPaused() { return _paused; }
	public GameState getState() { return _state; }
	public InterfaceState getInterface() { return _interface; }
	
	public Stats getStats() { return _stats; }
	
	public BanList getBanList() { return _banList; }
	public Money getMoney() { return _money; }
	public Cost getCost() { return _cost; }
	public Cost getPayment() { return _payment; }
	
	//Set
	public void setPartList(PartList partList){
		if(partList==null) return;
		_partList = partList;
		Block mainBlock = partList.getMainBlock();
		if(mainBlock==null) return;
		_world = partList.getMainBlock().getWorld();
	}
	public void setId(int id){
		_id = id;
	}
	public void setName(String name){
		_name = name;
	}
	public void setOwner(GamePlayer owner){
		setOwnerName(owner.getName());
	}
	public void setOwnerName(String ownerName){
		_ownerName = ownerName;
	}

	public void setPlayers(TreeMap<String, GamePlayer> players){
		_players = players;
	}
	public void setItems(Filter items){
		_items = items;
	}
	public void setFound(Stash found){
		_found = found;
		_found.clearChanged();
	}
	public void setTeams(List<Team> teams){
		_teams = teams;
	}
	public void setLog(Log log){
		_log = log;
	}
	public void setPaused(boolean paused){
		_paused = paused;
	}
	public void setState(GameState state){
		_state = state;
	}
	public void setInterface(InterfaceState interfaceState){
		_interface = interfaceState;
	}
	public void setStats(Stats stats){
		_stats = stats;
	}
	
	public void setBanList(BanList banList){
		_banList = banList;
	}
	public void setMoney(Money money){
		_money = money;
	}
	public void setCost(Cost cost){
		_cost = cost;
	}
	public void setPayment(Cost payment){
		_payment = payment;
	}
	
	//Clear
	public void clearTeams(){
		_teams.clear();
	}
	public void clearFound(){
		_found.clear();
	}
	
	public void togglePaused(){
		if(_paused) _paused = false;
		else _paused = true;
	}
	
	//getDataFrom
	public void getDataFrom(GameConfig config){
		setId(config.getId());
		setName(config.getName());
		setOwnerName(config.getOwnerName());
		
		setPlayers(config.getPlayers());
		setItems(config.getItems());
		setFound(config.getFound());
		setLog(config.getLog());
		setPaused(config.getPaused());
		setState(config.getState());
		setInterface(config.getInterface());
		setStats(config.getStats());
		
		setBanList(config.getBanList());
		setMoney(config.getMoney());
		setCost(config.getCost());
		setPayment(config.getPayment());
	}
	
	public void correctMinMaxNumbers(Setting setting){
		int size = getTeams().size();
		int min = getSettingLibrary().getInt(Setting.minplayers);
		int max = getSettingLibrary().getInt(Setting.maxplayers);
		int minTeam = getSettingLibrary().getInt(Setting.minteamplayers);
		int maxTeam = getSettingLibrary().getInt(Setting.maxteamplayers);
		switch(setting){
		case minplayers:
			if(min<size) min = size;
			if(max!=0) if(min>max) max = min;
			if(min<minTeam*size) minTeam = (int)((double)min/(double)size);
			break;
		case maxplayers:
			if(max!=0){
				if(max<size) max = size;
				if(max<min) min = max;
				if(min<minTeam*size) minTeam = (int)((double)min/(double)size);
			}
			break;
		case minteamplayers:
			if(minTeam<1){
				minTeam = 1;
			}
			if(maxTeam!=0) if(minTeam>maxTeam) maxTeam = minTeam;
			if(minTeam*size>min) min = minTeam*size;
			if(max!=0) if(max<min) max = min;
			break;
		case maxteamplayers:
			if(maxTeam!=0){
				if(maxTeam<1) maxTeam = 1;
				if(maxTeam<minTeam) minTeam = maxTeam;
			}
			break;
		}
		getSettingLibrary().set(Setting.minplayers, min);
		getSettingLibrary().set(Setting.maxplayers, max);
		getSettingLibrary().set(Setting.minteamplayers, minTeam);
		getSettingLibrary().set(Setting.maxteamplayers, maxTeam);
	}
}