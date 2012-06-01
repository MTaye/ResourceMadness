package com.mtaye.ResourceMadness.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class InventoryHelper {
	public InventoryHelper(){
	}
	
	public static String encodeInventoryToString(ItemStack[] items){
		String line = "";
		if((items!=null)&&(items.length>0)){
			HashMap<String, ItemStack> hashItems = new HashMap<String, ItemStack>();
			for(ItemStack item : items){
				if((item!=null)&&(item.getType()!=Material.AIR)){
					String idData = ""+item.getTypeId()+":"+item.getDurability();
					if(hashItems.containsKey(idData)){
						int amount = hashItems.get(idData).getAmount()+item.getAmount();
						ItemStack itemClone = item.clone();
						itemClone.setAmount(amount);
						hashItems.put(idData, itemClone);
					}
					else hashItems.put(idData, item);
				}
			}
			String[] array = hashItems.keySet().toArray(new String[hashItems.size()]);
			Arrays.sort(array);
			for(String idData : array){
				ItemStack item = hashItems.get(idData);
				line+=item.getTypeId()+":"+item.getAmount()+":"+item.getDurability();
				line+=",";
			}
		}
		if(line.length()==0){
			return "";
		}
		line = TextHelper.stripLast(line,",");
		return line;
	}
	
	public static ItemStack[] getItemStackArrayByString(String strArgs){
		List<ItemStack> items = getItemStackByString(strArgs);
		return items.toArray(new ItemStack[items.size()]);
	}
	
	public static List<ItemStack> getItemStackByString(String strArgs){
		List<ItemStack> items = new ArrayList<ItemStack>();
		if((strArgs==null)||(strArgs.length()==0)) return items;
		String[] splitArgs = strArgs.split(",");
		for(String splitArg : splitArgs){
			String[] args = splitArg.split(":");
			int id = Helper.getIntByString(args[0]);
			int amount = Helper.getIntByString(args[1]);
			short durability = Helper.getShortByString(args[2]);
			if((id!=-1)&&(amount!=-1)&&(durability!=-1)){
				if(args.length>2){
					Material mat = Material.getMaterial(id);
					while(amount>mat.getMaxStackSize()){
						ItemStack item = new ItemStack(mat, mat.getMaxStackSize(), durability);
						items.add(item);
						amount-=mat.getMaxStackSize();
					}
					ItemStack item = new ItemStack(mat, amount, durability);
					items.add(item);
				}
			}
		}
		return items;
	}
	
	public static HashMap<Integer, ItemStack> combineItemsByItemStack(List<ItemStack> items){
		HashMap<Integer, ItemStack> hashItems = new HashMap<Integer, ItemStack>();
		for(ItemStack rewardItem : items){
			ItemStack item = rewardItem.clone();
			if(item!=null){
				int id = item.getTypeId();
				if(hashItems.containsKey(id)){
					int amount = hashItems.get(id).getAmount()+item.getAmount();
					ItemStack itemClone = item.clone();
					itemClone.setAmount(amount);
					hashItems.put(id, itemClone);
				}
				else hashItems.put(id, item);
			}
		}
		return hashItems;
	}
	
	public static int findUsableStack(Inventory inv){
		ItemStack[] contents = inv.getContents();
		for(int i = 0; i<contents.length; i++){
			ItemStack invItem = contents[i];
			if((invItem==null)||(invItem.getType()==Material.AIR)) continue;
			if(invItem.getAmount()<invItem.getMaxStackSize()) return i;
		}
		return -1;
	}
	
	public static int findUsableStack(Inventory inv, int id){
		ItemStack[] contents = inv.getContents();
		for(int i = 0; i<contents.length; i++){
			ItemStack invItem = contents[i];
			if((invItem==null)||(invItem.getType()==Material.AIR)) continue;
			if(invItem.getAmount()<invItem.getMaxStackSize()){
				if(invItem.getTypeId() == id) return i;
			}
		}
		return -1;
	}
	
	public static void clearInventoryItem(Inventory inv, ItemStack item){
		ItemStack[] contents = inv.getContents();
		for(int i=0; i<contents.length; i++){
			ItemStack invItem = contents[i];
			if(invItem==null) continue;
			if(invItem.getType() == item.getType()) if(invItem.getAmount() == item.getAmount()) inv.clear(i);
		}
	}
	
	public static HashMap<Integer, ItemStack> convertToHashMap(List<ItemStack> items){
		HashMap<Integer, ItemStack> hashItems = new HashMap<Integer, ItemStack>();
		for(ItemStack item : items){
			if(item==null) continue;
			int id = item.getTypeId();
			if(hashItems.containsKey(id)){
				ItemStack hashItem = hashItems.get(id);
				hashItem.setAmount(hashItem.getAmount()+item.getAmount());
			}
			else hashItems.put(id, item.clone());
		}
		return hashItems;
	}
	
	public static List<ItemStack> getItemsExcept(PlayerInventory inv, Material... materials){
		List<ItemStack> result = new ArrayList<ItemStack>();
		result.addAll(getItemsExcept(inv.getContents(), materials));
		result.addAll(getItemsExcept(inv.getArmorContents(), materials));
		return result;
	}
	
	public static List<ItemStack> getItemsExcept(ItemStack[] items, Material... materials){
		List<ItemStack> result = new ArrayList<ItemStack>();
		if(materials.length==0) return result;
		for(ItemStack item : items){
			if(item==null) continue;
			for(Material mat : materials){
				if(mat==null) continue;
				if(item.getType()==mat) continue;
				result.add(item);
			}
		}
		return result;
	}
	
	public static ItemStack[] joinItemStack(ItemStack[]... args){
		List<ItemStack> result = new ArrayList<ItemStack>();
		for(ItemStack[] items : args){
			if(items==null) continue;
			result.addAll(Arrays.asList(items));
		}
		return result.toArray(new ItemStack[result.size()]);
	}
}
