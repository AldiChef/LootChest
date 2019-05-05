package fr.black_eyes.lootchest;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import fr.black_eyes.lootchest.Mat;
import fr.black_eyes.lootchest.commands.Lootchest;

public class Utils implements Listener {

	//message functions that automaticly get a message from language file
	public static void msg(CommandSender p, String path, String replacer, String replacement) {
		p.sendMessage(getMsg(path, replacer, replacement));
	}
	
	public static String getMsg(String path, String replacer, String replacement) {
		return Main.getInstance().getLang().getString(path).replace(replacer, replacement).replace("&", "�");
	}
	
	
	//function to copy a chest
	//fonction pour copier un coffre
	public static void copychest(String chest1, String chest2) {
		String holo = Main.getInstance().getData().getString("chests." + chest1 + ".holo");
		String effect = Main.getInstance().getData().getString("chests." + chest1 + ".particle");
		int time = Main.getInstance().getData().getInt("chests." + chest1 + ".time");
		Main.getInstance().getData().set("chests." + chest2 + ".inventory", null);
		for(String keys : Main.getInstance().getData().getConfigurationSection("chests." + chest1 + ".inventory").getKeys(false)) {
			ItemStack item = Main.getInstance().getData().getItemStack("chests." + chest1 + ".inventory." + keys);
			int chance = Main.getInstance().getData().getInt("chests." + chest1 + ".chance." + keys);
			Main.getInstance().getData().set("chests." + chest2 + ".inventory." + keys, item);
			Main.getInstance().getData().set("chests." + chest2 + ".chance." + keys, chance);
		}
		Main.getInstance().getData().set("chests." + chest2 + ".holo", holo);
		Main.getInstance().getData().set("chests." + chest2 + ".particle", effect);
		Main.getInstance().getData().set("chests." + chest2 + ".time", time);
		try {
			Main.getInstance().getData().save(Main.getInstance().getDataF());
			Main.getInstance().getData().load(Main.getInstance().getDataF());
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		restoreChest(chest2, true);
	}
	
	
	//cr�er le coffe et enregistrer les infos
	//chest creation and registering
	public static void saveChest(Block chest, String name) {
		Inventory inv = ((Chest) chest.getState()).getInventory();
		for(int i = 0 ; i < inv.getSize() ; i++) {
			if(inv.getItem(i) != null) {
				Main.getInstance().getData().set("chests." + name + ".inventory." + i, inv.getItem(i));
				Main.getInstance().getData().set("chests." + name + ".chance." + i, Main.getInstance().getConfig().getInt("default_item_chance"));
			}
		}
		@SuppressWarnings("deprecation")
		String data = chest.getState().getData().toString();
		String direction;
		if(data.indexOf(",type")<0) {
			direction = data.substring(data.indexOf("facing") + 7, data.length());
		}
		else {
			direction = data.substring(data.indexOf("facing") + 7, data.indexOf(",type"));
		}
		Main.getInstance().getData().set("chests." + name + ".direction", direction);
		Main.getInstance().getData().set("chests." + name + ".holo", name);
		Main.getInstance().getData().set("chests." + name + ".time", Main.getInstance().getConfig().getInt("default_reset_time"));
		setPosition(name, chest.getLocation());
		Main.getInstance().getData().set("chests." + name + ".lastreset", new Timestamp(System.currentTimeMillis()).getTime());
		Location loc = chest.getLocation();
	    final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
	   	loc2.setX(loc.getX()+0.5);
	    loc2.setY(loc.getY()+0.5);
	    loc2.setZ(loc.getZ()+0.5);
	   	Main.getInstance().getData().set("chests." +name+ ".particle", Main.getInstance().getConfig().getString("Particles.default_particle"));
		try {
			Main.getInstance().getData().save(Main.getInstance().getDataF());
			Main.getInstance().getData().load(Main.getInstance().getDataF());
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		((Chest) chest.getLocation().getBlock().getState()).getInventory().clear();
		chest.getLocation().getBlock().setType(Material.AIR);
		restoreChest(name, true);
	}
	
	
	//fonction pour changer la position d'un coffre
	//function to change a chest location
	public static void changepos(String name, Location loc3) {
		final Location loc = getPosition(name);
		Block chest = loc.getBlock();
		Utils.deleteholo(loc);
		chest.setType(Material.AIR);
		final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
		loc2.setX(loc.getX()+0.5);
		loc2.setY(loc.getY()+0.5);
		loc2.setZ(loc.getZ()+0.5);
		Main.part.remove(loc2);
		setPosition(name, loc3.getBlock().getLocation());
		restoreChest(name, true);
	}
	
	//deletes a chest
	//supprimes un coffre
	public static void deleteChest(String name) {
		if(Main.getInstance().getData().isSet("chests." + name + ".position")) {
			final Location loc = getPosition(name);
			Block chest = loc.getBlock();
			if(chest.getType().equals(Material.CHEST)) {
				((Chest) chest.getState()).getInventory().clear();
			}
			chest.setType(Material.AIR);
			final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
			loc2.setX(loc.getX()+0.5);
			loc2.setY(loc.getY()+0.5);
			loc2.setZ(loc.getZ()+0.5);
			Main.part.remove(loc2);
			deleteholo(chest.getLocation());
		}
		Main.getInstance().getData().set("chests." + name, null);
		try {
			Main.getInstance().getData().save(Main.getInstance().getDataF());
			Main.getInstance().getData().load(Main.getInstance().getDataF());
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
	}
	
	//to fill a chest or give chest to player
	public static void fillInventory(String name, Inventory inv, boolean clear, Player p) {
		if(clear) {
			inv.clear();
		}
		for(String keys : Main.getInstance().getData().getConfigurationSection("chests." + name + ".inventory").getKeys(false)) {
			ItemStack item = Main.getInstance().getData().getItemStack("chests." + name + ".inventory." + keys);
			int slot = Integer.parseInt(keys);
			int percent = ThreadLocalRandom.current().nextInt(0, 100 + 1);
			if(percent <= Main.getInstance().getData().getInt("chests." + name + ".chance." + keys)){
				if(inv.getItem(slot) == null || inv.getItem(slot).getType() == Material.AIR) {
					inv.setItem(slot, item);
				}
				else if(p!=null && p.getInventory().firstEmpty() == -1) {
					p.getWorld().dropItem(p.getLocation(), item);
				}
				else {
					inv.addItem(item);
				}
			}
		}
	}
	
	//se sert du data.yml pour set le coffre et remplir son inventaire, cr�er l'holo en fonction du nom 
	//Taking informations from data.yml to restore a specific chest if it is time to do it, or if we force respawn. 
	public static void restoreChest(String name, Boolean force) {
		final Location loc = getPosition(name);
		//si le coffre est vide, IL MEURT MOUAHAHA
		Block block = loc.getBlock();
		if(block.getType().equals(Material.CHEST)) {
			if(isEmpty(((Chest)block.getState()).getInventory()) && Main.getInstance().getConfig().getBoolean("RemoveEmptyChests")) {
				Utils.deleteholo(loc);
				block.setType(Material.AIR);
				final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
				loc2.setX(loc.getX()+0.5);
				loc2.setY(loc.getY()+0.5);
				loc2.setZ(loc.getZ()+0.5);
				Main.part.remove(loc2);
				return;
			}
		}

		long tempsactuel = (new Timestamp(System.currentTimeMillis())).getTime();
		long minutes = Main.getInstance().getData().getLong("chests." + name + ".time")*60*1000;
		long tempsenregistre = Main.getInstance().getData().getLong("chests." + name + ".lastreset");
		if(tempsactuel - tempsenregistre > minutes || force) {
			if(!force && Main.getInstance().getConfig().getBoolean("respawn_notify.natural_respawn.enabled") ) {
				String holo = Main.getInstance().getData().getString("chests." + name + ".holo");
				Bukkit.broadcastMessage((((Main.getInstance().getConfig().getString("respawn_notify.natural_respawn.message").replace("[Chest]", holo)).replace("[x]", block.getX()+"")).replace("[y]", block.getY()+"")).replace("[z]", block.getZ()+"").replace("&", "�"));
			}
			block.setType(Material.CHEST);
			Inventory inv = ((Chest) block.getState()).getInventory();
			fillInventory(name, inv, true, null);
			
			if(Main.getInstance().getConfig().getBoolean("UseHologram")){
				deleteholo(loc);
				makeHolo(loc, name);
			}
			else {
				deleteholo(loc);
			}
			Main.getInstance().getData().set("chests." + name + ".lastreset", tempsactuel);
			if(!Main.getInstance().getData().isSet("chests." + name + ".direction")) {
				Main.getInstance().getData().set("chests." + name + ".direction", "north");
			}
			try {
				Main.getInstance().getData().save(Main.getInstance().getDataF());
				Main.getInstance().getData().load(Main.getInstance().getDataF());
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			String direction = Main.getInstance().getData().getString("chests." + name + ".direction");
			BlockState state = loc.getBlock().getState();
			if(direction.equalsIgnoreCase("east")){
				state.setData(new org.bukkit.material.Chest(BlockFace.EAST));
			}
			if(direction.equalsIgnoreCase("north")){
				state.setData(new org.bukkit.material.Chest(BlockFace.NORTH));
			}			
			if(direction.equalsIgnoreCase("south")){
				state.setData(new org.bukkit.material.Chest(BlockFace.SOUTH));
			}			
			if(direction.equalsIgnoreCase("west")){
				state.setData(new org.bukkit.material.Chest(BlockFace.WEST));
			}
			state.update();
		}
		final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
		loc2.setX(loc.getX()+0.5);
		loc2.setY(loc.getY()+0.5);
		loc2.setZ(loc.getZ()+0.5);
		if(loc.getBlock().getType().equals(Material.CHEST) && !Bukkit.getVersion().contains("1.8")) {
			for(Object part : Main.particules) {
				if((""+part).contains(Main.getInstance().getData().getString("chests." + name + ".particle"))) {
					Main.part.put(loc2, (org.bukkit.Particle) part);
				}
			}
		}else {
			deleteholo(loc);
			if (!Bukkit.getVersion().contains("1.8")) {
				Main.part.remove(loc2);
			}
		}
	}
	
	//check for empty inventory
	public static boolean isEmpty(Inventory inv) {
	     for (ItemStack item : inv.getContents() ) {
	       if ( item != null ) {
	         return false;
	       }
	     }
	     return true;
	}

	//check if a chest is a lootchest by looking all lootchests locations
	public static String isLootChest(Location loc) {
		for(String keys : Main.getInstance().getData().getConfigurationSection("chests").getKeys(false)) {
			Location loc2 = getPosition(keys);
			if(loc2.equals(loc)) {
				return keys;
			}
		}
		return " ";
	}
	
	//geting chest position from data.yml
	public static Location getPosition(String name) {
		World world = Bukkit.getWorld(Main.getInstance().getData().getString("chests." + name + ".position.world"));
		double x = Main.getInstance().getData().getDouble("chests." + name + ".position.x");
		double y = Main.getInstance().getData().getDouble("chests." + name + ".position.y");
		double z = Main.getInstance().getData().getDouble("chests." + name + ".position.z");
		float pitch = (float) Main.getInstance().getData().getDouble("chests." + name + ".position.pitch");
		float yaw = (float) Main.getInstance().getData().getDouble("chests." + name + ".position.yaw");
		return new Location(world, x, y, z, pitch, yaw);
	}
	
	//seting chest position in data.yml
	public static void setPosition(String name, Location loc) {
		Main.getInstance().getData().set("chests." + name + ".position.world", loc.getWorld().getName());
		Main.getInstance().getData().set("chests." + name + ".position.x", loc.getX());
		Main.getInstance().getData().set("chests." + name + ".position.y", loc.getY());
		Main.getInstance().getData().set("chests." + name + ".position.z", loc.getZ());
		Main.getInstance().getData().set("chests." + name + ".position.pitch", loc.getPitch());
		Main.getInstance().getData().set("chests." + name + ".position.yaw", loc.getYaw());
	}
	
	//Pour suprimer un holograme
	//To delete hologram
	public static void deleteholo(Location loc) {
		final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
		loc2.setX(loc.getX()+0.5);
		loc2.setY(loc.getY()+Main.getInstance().getConfig().getInt("Hologram_distance_to_chest"));
		loc2.setZ(loc.getZ()+0.5);
		//the coordinates of a block are at the corner of the block
		for(Entity ent : loc2.getChunk().getEntities()) {
			if(ent instanceof ArmorStand && ent.getLocation().distance(loc2) <1.1) {
				ent.remove();
			}
		}
	}
	
	//Pour faire des hologrames au dessus des coffres
	//To do holograms near chests
	//the below function was created using some spigot forums code with some modifications
	public static void makeHolo(Location loc, String text) {
		final Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
		loc2.setX(loc.getX()+0.5);
		loc2.setY(loc.getY()+Main.getInstance().getConfig().getInt("Hologram_distance_to_chest"));
		loc2.setZ(loc.getZ()+0.5);
		//the coordinates of a block are at the corner of the block
		ArmorStand as = (ArmorStand) loc2.getWorld().spawnEntity(loc2, EntityType.ARMOR_STAND); //Spawn the ArmorStand

		as.setGravity(false); //Make sure it doesn't fall
		as.setCanPickupItems(false); //I'm not sure what happens if you leave this as it is, but you might as well disable it
		String name = Main.getInstance().getData().getString("chests." + text + ".holo").replace("&", "�");
		as.setCustomName(name); //Set this to the text you want
		if(!(name.equals("\"\"") || name.equals("\" \"") || name.equals("null"))) {
			as.setCustomNameVisible(true); //This makes the text appear no matter if your looking at the entity or not
		}
		
		as.setGravity(false); //Make sure it doesn't fall
		as.setCanPickupItems(false); //I'm not sure what happens if you leave this as it is, but you might as well disable it
		as.setCustomNameVisible(true); //This makes the text appear no matter if your looking at the entity or not
		as.setVisible(false); //Makes the ArmorStand invisible
	 	as.setArms(false);
	 	as.setBasePlate(false);
	 	as.setSmall(true);
	 	as.setMarker(true);

	}	
	
	//if an armorstand isn't visible, let's cancel all manipulations on it
	@EventHandler
	public void manipulate(PlayerArmorStandManipulateEvent e)
	{
	        if(!e.getRightClicked().isVisible())
	        {
	            e.setCancelled(true);
	        }
	}
	
	
	
	
	
	
	//Inventaires
	public static void invChances(Player p, String name) {
		final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, Utils.getMsg("Menu.chances.name", "[Chest]", name));
		for(String keys : Main.getInstance().getData().getConfigurationSection("chests." + name + ".inventory").getKeys(false)) {
			ItemStack item = Main.getInstance().getData().getItemStack("chests." + name + ".inventory." + keys);
			List<String> lore = new ArrayList<String>();
			lore.add(getMsg("Menu.chances.lore", "[Chest]", name));
			lore.add(Main.getInstance().getData().getString("chests." + name + ".chance." + keys) + "%");
			ItemMeta im = item.getItemMeta();
			im.setLore(lore);
			ItemStack item2 = new ItemStack(item.getType(), item.getAmount());
			item2.setItemMeta(im);
			int slot = Integer.parseInt(keys);
			inv.setItem(slot, item2);
		}
		Lootchest.editinv.put(p, name);
		p.openInventory(inv);
	}
	
	public static void invTime(Player p, String name) {
		final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, Utils.getMsg("Menu.time.name", "[Chest]", name));
		inv.setItem(4, getItem(Mat.TOTEM_OF_UNDYING, getMsg("Menu.time.infinite", " ", " ")));
		
		long temps = Main.getInstance().getData().getLong("chests." + name + ".time");
		long jours = temps/3600;
		long heures = temps/60 - jours*24;
		long minutes = temps - heures*60 - jours*3600;
		
		
		//Initialisation du menu selon le temps du coffre
		if (jours/10 == 0) {
			inv.setItem(9, getItem(Mat.BARRIER, getMsg("Menu.time.days", " ", " ")));
		} else {
			inv.setItem(9, getItem(Mat.GOLD_BLOCK, getMsg("Menu.time.days", " ", " ")));
			inv.getItem(9).setAmount((int) (jours/10));
		} if (jours/10*10 == jours) {
			inv.setItem(10, getItem(Mat.BARRIER, getMsg("Menu.time.days", " ", " ")));
		}else {
			inv.setItem(10, getItem(Mat.GOLD_BLOCK, getMsg("Menu.time.days", " ", " ")));
			inv.getItem(10).setAmount((int) (jours-jours/10*10));
		}if (heures/10 == 0) {
			inv.setItem(12, getItem(Mat.BARRIER, getMsg("Menu.time.hours", " ", " ")));
		}else {
			inv.setItem(12, getItem(Mat.GOLD_INGOT, getMsg("Menu.time.hours", " ", " ")));
			inv.getItem(12).setAmount((int) (heures/10));
		}if (heures/10*10 == heures) {
			inv.setItem(13, getItem(Mat.BARRIER, getMsg("Menu.time.hours", " ", " ")));
		}else {
			inv.setItem(13, getItem(Mat.GOLD_INGOT, getMsg("Menu.time.hours", " ", " ")));
			inv.getItem(13).setAmount((int) (heures-heures/10*10));
		}if (minutes/10 == 0) {
			inv.setItem(15, getItem(Mat.BARRIER, getMsg("Menu.time.minutes", " ", " ")));
		}else {
			inv.setItem(15, getItem(Mat.GOLD_NUGGET, getMsg("Menu.time.minutes", " ", " ")));
			inv.getItem(15).setAmount((int) (minutes/10));
		}if (minutes/10*10 == minutes) {
			inv.setItem(16, getItem(Mat.BARRIER, getMsg("Menu.time.minutes", " ", " ")));
		}else {
			inv.setItem(16, getItem(Mat.GOLD_NUGGET, getMsg("Menu.time.minutes", " ", " ")));
			inv.getItem(16).setAmount((int) (minutes-minutes/10*10));
		}
        inv.setItem(14, getItem(Mat.STICK, ""));
        inv.setItem(11, getItem(Mat.STICK, ""));
		Lootchest.editinv.put(p, name);
		ItemStack sign = new ItemStack(Mat.SIGN, 1);
		ItemMeta meta = sign.getItemMeta(); 
		meta.setDisplayName("Respawn time: " + jours+" days, " + heures + " hours, " + minutes + " minutes.");
		sign.setItemMeta(meta);
    	inv.setItem(22, sign);
		p.openInventory(inv);
	}
	
	public static void invEdit(Player p, String name) {
		final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, Utils.getMsg("Menu.items.name", "[Chest]", name));
		for(String keys : Main.getInstance().getData().getConfigurationSection("chests." + name + ".inventory").getKeys(false)) {
			ItemStack item = Main.getInstance().getData().getItemStack("chests." + name + ".inventory." + keys);
			int slot = Integer.parseInt(keys);
			inv.setItem(slot, item);
		}
		Lootchest.editinv.put(p, name);
		p.openInventory(inv);
	}
	
	public static void invcopy(Player p, String chest) {
		int i = 0;
		final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, getMsg("Menu.copy.name", " ", " "));
		for(String keys : Main.getInstance().getData().getConfigurationSection("chests").getKeys(false)) {
			if(!keys.equals(chest)) {
				String name = Main.getInstance().getData().getString("chests." + keys + ".holo").replace("&", "�");
				String effect = Main.getInstance().getData().getString("chests." + keys + ".particle");
				String world = getPosition(keys).getWorld().getName();
				ItemStack item = getItemWithLore(Material.CHEST, "�6" +keys, "�bHologram: �6" + name + "||�bWorld: �6"+ world + "||�bEffect: �6" + effect);
				inv.setItem(i++, item);
			}
		}
		p.openInventory(inv);
        Lootchest.editinv.put(p, chest);
	}
	
	
	public static void mainInv(Player p, String name) {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, getMsg("Menu.main.name", " ", " "));
        inv.setItem(4, getItem(Mat.ENDER_CHEST, getMsg("Menu.main.copychest", " ", " ")));
        if(Main.getInstance().getConfig().getBoolean("Particles.enable")) {
        	inv.setItem(11, getItem(Mat.ENDER_EYE, getMsg("Menu.main.particles", " ", " ")));
        }
        inv.setItem(13, getItem(Mat.CHEST, getMsg("Menu.main.content", " ", " ")));
        inv.setItem(15, getItem(Mat.CLOCK, getMsg("Menu.main.respawnTime", " ", " ")));
        inv.setItem(22, getItem(Mat.DIAMOND, getMsg("Menu.main.chances", " ", " ")));
        
        new BukkitRunnable() {       
            @Override
            public void run() {
            	p.openInventory(inv);
                Lootchest.editinv.put(p, name);
            }                
        }.runTaskLater(Main.getInstance(), 2);
    }
	
	
	
	// /!\ Certains items ne sont pas les m�mes selont que l'on est en 1.12 ou 1.13, � v�rifier pour particules
	public static void particleInv(Player p, String name) {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, getMsg("Menu.particles.name", " ", " "));
        inv.setItem(0, getItem(Mat.TNT, "Huge Explosion"));
        inv.setItem(1, getItem(Mat.TNT, "Large Explosion"));
        inv.setItem(2, getItem(Mat.TNT, "Normal Explosion"));
        inv.setItem(3, getItem(Mat.FIREWORK, "Fireworks Sparks"));
        inv.setItem(4, getItem(Mat.PRISMARINE, "Bubble Pop"));
        inv.setItem(5, getItem(Mat.STONE, "Suspended"));
        inv.setItem(6, getItem(Mat.MYCELIUM, "Town Aura"));
        inv.setItem(7, getItem(Mat.IRON_SWORD, "Crit"));
        inv.setItem(8, getItem(Mat.DIAMOND_SWORD, "Magic Crit"));
        inv.setItem(9, getItem(Mat.FURNACE, "Normal Smoke"));
        inv.setItem(10, getItem(Mat.FURNACE, "Large Smoke"));
        inv.setItem(11, getItem(Mat.ENCHANTED_BOOK, "Mob Spell"));
        inv.setItem(12, getItem(Mat.ENCHANTED_BOOK, "Mob Spell Ambient"));
        inv.setItem(13, getItem(Mat.ENCHANTED_BOOK, "Spell"));
        inv.setItem(14, getItem(Mat.ENCHANTED_BOOK, "Instant Spell"));
        inv.setItem(15, getItem(Mat.ENCHANTED_BOOK, "Witch Spell"));
        inv.setItem(16, getItem(Mat.NOTE_BLOCK, "Note"));
        inv.setItem(17, getItem(Mat.END_PORTAL_FRAME, "Portal"));
        inv.setItem(18, getItem(Mat.ENCHANTING_TABLE , "Enchantment Table"));
        inv.setItem(19, getItem(Mat.BLAZE_POWDER, "Flame"));
        inv.setItem(20, getItem(Mat.LAVA_BUCKET, "Lava"));
        inv.setItem(21, getItem(Mat.STONE, "Footstep"));
        inv.setItem(22, getItem(Mat.WATER_BUCKET, "Water Splash"));
        inv.setItem(23, getItem(Mat.WATER_BUCKET, "Water Wake"));
        inv.setItem(24, getItem(Mat.QUARTZ, "Cloud"));
        inv.setItem(25, getItem(Mat.REDSTONE, "Redstone"));
        inv.setItem(26, getItem(Mat.SNOW_BALL, "Snowball"));
        inv.setItem(27, getItem(Mat.WATER_BUCKET, "Drip Water"));
        inv.setItem(28, getItem(Mat.LAVA_BUCKET, "Drip Lava"));
        inv.setItem(29, getItem(Mat.IRON_SHOVEL, "Snow Shovel"));
        inv.setItem(30, getItem(Mat.SLIME_BALL, "Slime"));
        inv.setItem(31, getItem(Mat.ROSE_RED, "Heart"));
        inv.setItem(32, getItem(Mat.REDSTONE_BLOCK, "Angry Villager"));
        inv.setItem(33, getItem(Mat.EMERALD, "Happy Villager"));
        inv.setItem(34, getItem(Mat.BARRIER, "Barrier"));
        p.openInventory(inv);
        Lootchest.editinv.put(p, name);
    }


	public static ItemStack getItem(final Material enderChest, final String customName) {
		final ItemStack A = new ItemStack(enderChest, 1);
		final ItemMeta B = A.getItemMeta();
		if (customName != null) {
			B.setDisplayName(customName);
		}
		A.setItemMeta(B);
		return A;
	}
	
	public static ItemStack getItemWithLore(final Material material, final String customName,  String lore) {
		final ItemStack A = new ItemStack(material);
		final ItemMeta B = A.getItemMeta();
		if (customName != null) {
			B.setDisplayName(customName);
			List<String> lore2 = new ArrayList<String>(Arrays.asList(lore.split("\\|\\|")));
			B.setLore(lore2);
		}
		A.setItemMeta(B);
		return A;
	}
	
	
	
	
	
}
