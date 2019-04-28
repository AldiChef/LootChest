package fr.black_eyes.lootchest.listeners;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import fr.black_eyes.lootchest.Main;
import fr.black_eyes.lootchest.Utils;



public class DeleteListener implements Listener  {
	
	
	public static HashMap<Player, Location> openInvs = new HashMap<Player, Location>();
	//g�re la destruction d'un coffre au niveau des hologrames
	
    @EventHandler
    public void clickblock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block b = e.getClickedBlock();
                if (b.getType().equals(Material.CHEST)){
                	if(((Chest) b.getState()).getInventory().getViewers().size() >0) {
                		e.setCancelled(true);
                	}
                    if(openInvs.containsKey(p)) {
                    	openInvs.remove(p);
                    	return;
                    }
                    else {
                    	openInvs.put(p, b.getLocation());
                    }
                    
                }
        }
    }
    
    @EventHandler
    public void oncloseInventory(InventoryCloseEvent e) {
    	Inventory inv = e.getInventory();
    	Player p = Bukkit.getPlayer(e.getPlayer().getName());
    	if((Utils.isEmpty(inv) || Main.getInstance().getConfig().getBoolean("RemoveChestAfterFirstOpenning")) && openInvs.containsKey(p)) {
    		String keys = Utils.isLootChest(openInvs.get(p));
    		if(!keys.equals(" ")) {
    			Location loc = openInvs.get(p);
    			if(Main.getInstance().getConfig().getBoolean("RemoveEmptyChests") || Main.getInstance().getConfig().getBoolean("RemoveChestAfterFirstOpenning")) {
    				inv.clear();
    				loc.getBlock().setType(Material.AIR);
    				if(Main.getInstance().getData().getInt("chests."+keys+".time")==0) {
    					Utils.restoreChest(keys, true);
    				}
    			}
    			Utils.deleteholo(loc);
    			final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    	    	loc2.setX(loc.getX()+0.5);
    	    	loc2.setY(loc.getY()+0.5);
    	    	loc2.setZ(loc.getZ()+0.5);
    	    	if(!Bukkit.getVersion().contains("1.8")) {
    	    		Main.part.remove(loc2);
    	    	}
    			Main.getInstance().getData().set("chests." + keys + ".lastreset", new Timestamp(System.currentTimeMillis()).getTime());
    			try {
    				Main.getInstance().getData().save(Main.getInstance().getDataF());
    				Main.getInstance().getData().load(Main.getInstance().getDataF());
    			} catch (IOException | InvalidConfigurationException e1) {
    				e1.printStackTrace();
    			}

    		}
    	}
    	openInvs.remove(p);
    }
    
    @EventHandler
    public void onchestbreak(BlockBreakEvent e) {
    	if(e.getBlock().getType().equals(Material.CHEST)) {
    		String keys = Utils.isLootChest(e.getBlock().getLocation());
    		if(!keys.equals(" ")) {
    			Location loc = e.getBlock().getLocation();
    			Utils.deleteholo(loc);
    	        final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    	    	loc2.setX(loc.getX()+0.5);
    	    	loc2.setY(loc.getY()+0.5);
    	    	loc2.setZ(loc.getZ()+0.5);
    	    	if(!Bukkit.getVersion().contains("1.8")) {
    	    		Main.part.remove(loc2);
    	    	}
    			Main.getInstance().getData().set("chests." + keys + ".lastreset", new Timestamp(System.currentTimeMillis()).getTime());
    			try {
    				Main.getInstance().getData().save(Main.getInstance().getDataF());
    				Main.getInstance().getData().load(Main.getInstance().getDataF());
    			} catch (IOException | InvalidConfigurationException e1) {
    				e1.printStackTrace();
    			}
    			return;
    		}
    	}
    }
    

}
