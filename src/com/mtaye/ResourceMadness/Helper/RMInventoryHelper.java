package com.mtaye.ResourceMadness.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mtaye.ResourceMadness.RMText;
import com.mtaye.ResourceMadness.RM.ClaimType;

public final class RMInventoryHelper {
	public RMInventoryHelper(){
	}
	
	public static String encodeInventoryToString(ItemStack[] items, ClaimType claimType){
		String line = "";
		if((items!=null)&&(items.length>0)){
			HashMap<String, ItemStack> hashItems = new HashMap<String, ItemStack>();
			for(ItemStack item : items){
				if(item!=null){
					String idData = ""+item.getTypeId()+":"+item.getDurability();
					if(item.getData()!=null) idData += ":"+Byte.toString(item.getData().getData());
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
				//String[] splitItems = idData.split(":");
				//int id = getIntByString(splitItems[0]);
				ItemStack item = hashItems.get(idData);
				line+=item.getTypeId()+":"+item.getAmount()+":"+item.getDurability();
				if(item.getData()!=null) line+=":"+Byte.toString(item.getData().getData());
				line+=",";
			}
		}
		if(line.length()==0){
			switch(claimType){
				case ITEMS:
					return "ITEMS";
				case FOUND:
					return "FOUND";
				case REWARD:
					return "REWARD";
				case TOOLS:
					return "TOOLS";
				case CHEST:
					return "CHEST";
			}
		}
		line = RMText.stripLast(line,",");
		return line;
	}
	
	public static List<ItemStack> getItemStackByStringArray(String strArgs){
		List<ItemStack> items = new ArrayList<ItemStack>();
		String[] splitArgs = strArgs.split(",");
		for(String splitArg : splitArgs){
			String[] args = splitArg.split(":");
			int id = RMHelper.getIntByString(args[0]);
			int amount = RMHelper.getIntByString(args[1]);
			short durability = RMHelper.getShortByString(args[2]);
			if((id!=-1)&&(amount!=-1)&&(durability!=-1)){
				if(args.length==4){
					byte data = RMHelper.getByteByString(args[3]);
					if(data!=-1){
						Material mat = Material.getMaterial(id);
						ItemStack item = new ItemStack(mat, amount, durability, data);
						items.add(item);
					}
				}
				else if(args.length==3){
					Material mat = Material.getMaterial(id);
					while(amount>mat.getMaxStackSize()){
						ItemStack item = new ItemStack(mat, mat.getMaxStackSize(), durability);
						items.add(item);
						amount-=mat.getMaxStackSize();
					}
					ItemStack item = new ItemStack(mat,amount, durability);
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
}
