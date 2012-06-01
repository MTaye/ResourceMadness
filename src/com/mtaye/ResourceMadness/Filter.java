package com.mtaye.ResourceMadness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.Game.HandleState;
import com.mtaye.ResourceMadness.helper.Helper;
import com.mtaye.ResourceMadness.helper.TextHelper;
import com.mtaye.ResourceMadness.time.Timer;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class Filter implements Cloneable {
	private HashMap<Integer, Item> _filter = new HashMap<Integer, Item>();
	private Item _lastItem; 
	
	public Filter(){
	}
	public Filter(HashMap<Integer, Item> items){
		_filter = items;
	}
	public Filter(Filter filter){
		_filter = filter.cloneItems();
	}
	public boolean containsKey(Integer key){
		return _filter.containsKey(key)?true:false;
	}
	public HashMap<Integer, Item> getItems(){
		return _filter;
	}
	public Item getItem(int index){
		return _filter.get(index);
	}
	public Set<Integer> keySet(){
		return _filter.keySet();
	}
	public int size(){
		return _filter.size();
	}
	public Collection<Item> values(){
		return _filter.values();
	}
	public void clearItems(){
		_filter.clear();
	}
	public Item getLastItem(){
		return _lastItem;
	}
	public int getItemsTotal(){
		int total = 0;
		for(Item rmItem : _filter.values()){
			total+=rmItem.getAmount();
		}
		return total;
	}
	public int getItemsTotalHigh(){
		int total = 0;
		for(Item rmItem : _filter.values()){
			total+=rmItem.getAmountHigh();
		}
		return total;
	}
	public Filter clone(){
		Filter clone = new Filter();
		for(Item rmItem : values()){
			clone.addItem(rmItem.getId(), new Item(rmItem.getId(), rmItem.getAmount(), rmItem.getAmountHigh(), rmItem.getMaxStackSize()), false);
		}
		return clone;
	}
	public HashMap<Integer, Item> cloneItems(){
		HashMap<Integer, Item> clone = new HashMap<Integer, Item>();
		for(Item rmItem : values()){
			clone.put(rmItem.getId(), new Item(rmItem.getId(), rmItem.getAmount(), rmItem.getAmountHigh(), rmItem.getMaxStackSize()));
		}
		return clone;
	}
	public HashMap<Integer, Item> cloneItems(int amount){
		HashMap<Integer, Item> clone = new HashMap<Integer, Item>();
		for(Item rmItem : values()){
			clone.put(rmItem.getId(), new Item(rmItem.getId(), amount, amount, rmItem.getMaxStackSize()));
		}
		return clone;
	}
	public Filter cloneRandomize(int randomize){
		Filter items = clone();
		if(items!=null) items.randomize(randomize);
		return items;
	}
	public void randomize(int randomize){
		if((randomize>0)&&(_filter.size()>randomize)){
			Integer[] arrayItems = _filter.keySet().toArray(new Integer[_filter.size()]);
			List<Integer> listItems = new ArrayList<Integer>();
			for(Integer i : arrayItems){
				listItems.add(i);
			}
			int size = listItems.size();
			while(size>randomize){
				int random = (int)Math.round((Math.random()*(size-1)));
				_filter.remove(listItems.get(random));
				listItems.remove(random);
				size--;
			}
		}
	}
	
	public void populateByFilter(Filter filter){
		_filter.clear();
		for(Integer item : filter.keySet()){
			Item rmItem = filter.getItem(item);
			int amount1 = rmItem.getAmount();
			int amount2 = rmItem.getAmountHigh();
			
			int val = amount1;
			if(amount2>0){
				val = Math.abs(amount1-amount2);
				val = (int)(Math.random()*val);
				val = amount1 + val;
				if(val<1) val = 1;
				addItem(item, new Item(item, val), false);
			}
			else{
				addItem(item, new Item(item, amount1), false);
			}
		}
	}
	
	public void multiplyItemAmount(float multiplier){
		for(Integer item : _filter.keySet()){
			Item rmItem = _filter.get(item);
			int amount1 = rmItem.getAmount();
			int amount2 = rmItem.getAmountHigh();
			if(amount2>0) rmItem.setAmountHigh((int)((float)amount2*multiplier));
			rmItem.setAmount((int)((float)amount1*multiplier));
		}
	}
	
	//Simple Add/Remove
	public void addItem(int id, Item rmItem){
		if(!_filter.containsKey(id)){
			_filter.put(id, rmItem);
		}
	}
	public void removeItem(int id){
		if(_filter.containsKey(id)){
			_filter.remove(id);
		}
	}
	public Boolean addRemoveItem(int id, Item rmItem){
		if(_filter.containsKey(id)){
			_filter.remove(id);
			return false;
		}
		else{
			_filter.put(id, rmItem);
			return true;
		}
	}
	//Add/Remove
	public HandleState addItem(Integer i, Item rmItem, boolean add){
		_lastItem = rmItem;
		if(_filter.containsKey(i)){
			if(add){
				rmItem.setAmount(rmItem.getAmount() + _filter.get(i).getAmount());
			}
			_filter.put(i, rmItem);
			return HandleState.MODIFY;
		}
		_filter.put(i, rmItem);
		return HandleState.ADD;
	}
	public HandleState removeItem(Integer i, Item rmItem, boolean dec){
		if(_filter.containsKey(i)){
			int amount = _filter.get(i).getAmount();
			if(dec) amount-= rmItem.getAmount();
			if(amount>0){
				rmItem.setAmount(amount);
				_lastItem = rmItem;
				_filter.put(i, rmItem);
				return HandleState.MODIFY;
			}
			else if(amount<=0){
				_lastItem = _filter.get(i);
				_filter.remove(i);
				return HandleState.REMOVE;
			}
		}
		return HandleState.NONE;
	}
	public HandleState removeAlwaysItem(Integer i, Item rmItem){
		if(_filter.containsKey(i)){
			_lastItem = _filter.get(i);
			_filter.remove(i);
			return HandleState.REMOVE;
		}
		return HandleState.NONE;
	}
	public Boolean addRemoveItem(Integer i, Item rmItem){
		if(!_filter.containsKey(i)){
			_lastItem = rmItem;
			_filter.put(i, rmItem);
			return true;
		}
		else{
			_lastItem = rmItem;
			if(rmItem.getAmountHigh()<1){ ///////////////////////////////////////////////////////////////////////////////////////////
				if(rmItem.getAmount() != _filter.get(i).getAmount()){
					_filter.put(i, rmItem);
					return true;
				}
			}
			_lastItem = _filter.get(i);
			_filter.remove(i);
			return false;
		}
	}
	
	////////////////////
	//STATIC FUNCTIONS//
	////////////////////
	
	//Encode Filter to String
	public static String encodeFilterToString(HashMap<Integer, Item> filter, boolean invert){
		if(filter.size()==0){
			return "";
		}
		HashMap<Integer, String> rmItems = new HashMap<Integer, String>();
		for(Item rmItem : filter.values()){
			String amount = ""+rmItem.getAmount();
			if(rmItem.getAmountHigh()>0) amount+="-"+rmItem.getAmountHigh();
			rmItems.put(rmItem.getId(), amount);
		}
		
		HashMap<String, List<Integer>> foundItems = new HashMap<String, List<Integer>>();
		for(Integer i : rmItems.keySet()){
			String amount = rmItems.get(i);
			if(foundItems.containsKey(rmItems.get(i))){
				List<Integer> list = foundItems.get(amount);
				if(!list.contains(i)) list.add(i);
				foundItems.put(amount, list);
			}
			else{
				List<Integer> list = new ArrayList<Integer>();
				list.add(i);
				foundItems.put(amount, list);
			}
		}
	
		String line = "";
		for(String amount : foundItems.keySet()){
			if(line!=""){
				line = TextHelper.stripLast(line, ",");
				line+=" ";
			}
			if(invert) line += amount+":";
			List<Integer> listAmount = foundItems.get(amount);
			Integer[] array = listAmount.toArray(new Integer[listAmount.size()]);
			Arrays.sort(array);
			
			int firstItem = -1;
			int lastItem = -1;
			for(Integer item : array){
				if(firstItem==-1){
					firstItem = item;
					lastItem = item;
				}
				else{
					if(item-lastItem!=1){
						if(lastItem-firstItem>1){
							line += firstItem+"-"+lastItem+",";
						}
						else{
							if(firstItem!=lastItem) line += firstItem+","+lastItem+",";
							else line += firstItem+",";
						}
						firstItem = item;
						lastItem = item;
					}
					else lastItem = item;
				}
			}
			if(lastItem-firstItem>1) line += firstItem+"-"+lastItem+",";
			else{
				if(firstItem!=lastItem) line += firstItem+","+lastItem+",";
				else line += firstItem+",";
			}
			if(!invert){
				if(amount!="1") line += ":"+amount;
			}
		}
		line = TextHelper.stripLast(line,",");
		return line;
	}
		
	//Get RMItems By String Array
	public static HashMap<Integer, Item> getRMItemsByStringArray(List<String> args, boolean invert){
		HashMap<Integer, Item> rmItems = new HashMap<Integer, Item>();
		HashMap<Integer, Integer[]> items = getItemsByStringArray(args, invert);

		for(Map.Entry<Integer, Integer[]> map : items.entrySet()){
			Integer item = map.getKey();
			Integer[] amount = map.getValue();
			int amount1 = -1;
			int amount2 = -1;
			if(amount.length>0) amount1 = amount[0];
			if(amount.length>1) amount2 = amount[1];
			
			Item rmItem = new Item(item);
			if(amount1 > -1) rmItem.setAmount(amount1);
			if(amount2 > -1) rmItem.setAmountHigh(amount2);
			
			rmItems.put(item, rmItem);
		}
		return rmItems;
	}
	
	//Get Items By String Array
	public static HashMap<Integer, Integer[]> getItemsByStringArray(List<String> args, boolean invert){
		HashMap<Integer, Integer[]> items = new HashMap<Integer, Integer[]>();
		if(args==null) return items;
		for(String arg : args){
			List<String> strArgs = new ArrayList<String>();
			if(invert) strArgs = Arrays.asList(arg.split(" "));
			else strArgs = splitArgsByColon(arg);
			for(String strArg : strArgs){
				String strAmount = "";
				String[] strSplit = strArg.split(":");
				if(strSplit.length==0) return items;
				String[] strItems = strSplit[invert?1:0].split(",");
				Integer[] intAmount = null;
				if(strSplit.length>1){
					strAmount = strSplit[invert?0:1];
					intAmount = checkInt(strAmount);
				}
				for(String str : strItems){
					if(str.contains("-")){
						String[] strItems2 = str.split("-");
						int id1=Helper.getIntByString(strItems2[0]);
						int id2=Helper.getIntByString(strItems2.length>1?strItems2[1]:"-1");
						//Check if material name
						if(id1==-1) id1=Helper.getMaterialIdByString(strItems2[0]);
						if(id2==-1) id2=Helper.getMaterialIdByString(strItems2[1]);
						if((id1!=-1)&&(id2!=-1)){
							if(id1>id2){
								int id3=id1;
								id1=id2;
								id2=id3;
							}
							while(id1<=id2){
								Material mat = Material.getMaterial(id1);
								if(mat!=null){
									if(intAmount==null){
										intAmount = new Integer[1];
										if(strArg.contains(Text.getLabel("filter.par.stack"))){
											intAmount[0] = mat.getMaxStackSize();
										}
										else intAmount[0] = 1;
									}
									items.put(mat.getId(),intAmount);
								}
								id1++;
							}
						}
					}
					else{
						int id=Helper.getIntByString(str);
						//Check if material name
						if(id==-1) id=Helper.getMaterialIdByString(str);
						if(id!=-1){
							Material mat = Material.getMaterial(id);
							if(mat!=null){
								if(intAmount==null){
									intAmount = new Integer[1];
									if(strArg.contains(Text.getLabel("filter.par.stack"))){
										intAmount[0] = mat.getMaxStackSize();
									}
									else intAmount[0] = 1;
								}
								items.put(mat.getId(),intAmount);
							}
						}
					}
				}
			}
		}
		return items;
	}
	
	//Split Args By Colon
	public static List<String> splitArgsByColon(String listArg){
		List<String> args = new ArrayList<String>();
		if(listArg==null) return args;
		if(listArg.contains(":")){
			int pos = 0;
			int posEnd = 0;
			while(pos!=-1){
				posEnd = listArg.indexOf(":",pos);
				if(posEnd!=-1) posEnd = listArg.indexOf(",",posEnd);
				if(posEnd!=-1){
					args.add(listArg.substring(pos,posEnd));
					pos = posEnd+1;
				}
				else{
					args.add(listArg.substring(pos));
					pos = -1;
				}
			}
			return args;
		}
		else return Arrays.asList(listArg);
	}
	
	//CheckInt
	//Check if integer is right for filter
	public static Integer[] checkInt(String arg){
		List<Integer> values = new ArrayList<Integer>();
		
		if(arg.contains("-")){
			String[] split = arg.split("-");
			int val1 = 0;
			int val2 = 0;
			if(split.length>0) val1 = Helper.getIntByString(split[0]);
			if(split.length>1) val2 = Helper.getIntByString(split[1]);
			if(val1>0) values.add(val1);
			if(val2>0) values.add(val2);
		}
		else{
			int val = Helper.getIntByString(arg);
			if(val>=0) values.add(val);
		}
		if(values.size()==0) return null;
		
		return values.toArray(new Integer[values.size()]);
	}
	
	public static List<ItemStack> convertToListItemStack(HashMap<Integer, Item> items){
		List<ItemStack> listItems = new ArrayList<ItemStack>();
		for(Item rmItem : items.values()){
			listItems.add(rmItem.getItem());
		}
		return listItems;
	}
	
	public static ItemStack[] convertToItemStackArray(HashMap<Integer, Item> items){
		List<ItemStack> listItems = convertToListItemStack(items);
		return listItems.toArray(new ItemStack[listItems.size()]);
	}
	
	public static HashMap<Integer, ItemStack> convertRMHashToHash(HashMap<Integer, Item> hashRMItems){
		HashMap<Integer, ItemStack> hashItems = new HashMap<Integer, ItemStack>();
		for(Map.Entry<Integer, Item> map : hashRMItems.entrySet()){
			hashItems.put(map.getKey(), map.getValue().getItem());
		}
		return hashItems;
	}
}