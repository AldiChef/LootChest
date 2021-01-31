package fr.black_eyes.lootchest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.particle.ParticleEffect;
import org.spigotmc.SpigotConfig;

import fr.black_eyes.lootchest.commands.LootchestCommand;
import fr.black_eyes.lootchest.listeners.Armorstand;
import fr.black_eyes.lootchest.listeners.DeleteListener;
import fr.black_eyes.lootchest.listeners.InventoryListeners;
import fr.black_eyes.lootchest.Utils;







public class Main extends JavaPlugin {
	//public ArrayList<LootChest> lc = new ArrayList<LootChest>();
	public static Object particules[] = new Object[34];
	public static HashMap<Location, Object> part = new HashMap<Location, Object>();
	public static Config configs;
	private HashMap<String, Lootchest> LootChest;
	private static Main instance;
	private Files configFiles;
	private static Utils utils;
	public static Boolean UseArmorStands;
	
	public HashMap<String, Lootchest> getLootChest(){
		
		return LootChest;
	}
	
	

	
	public void backUp() {
		File directoryPath = new File(instance.getDataFolder() + "/backups/");
		if(!directoryPath.exists()) {
			directoryPath.mkdir();
		}
		List<String> contents = Arrays.asList(directoryPath.list());
		int i=0;
		//finding valid backup name
		if(!contents.isEmpty()) {
			while( !contents.contains(i+"data.yml")) i++;
		}
		while( contents.contains(i+"data.yml")) {
			if (contents.contains((i+10)+"data.yml")) {
				Path oldbackup = Paths.get(instance.getDataFolder() +"/backups/"+ (i)+"data.yml");
				try {
					java.nio.file.Files.deleteIfExists(oldbackup);
				} catch (IOException e) {
					e.printStackTrace();
				}
				i+=9;
			}
			i++;
		}
		
		//auto-deletion of backup to keep only the 10 last ones
		Path oldbackup = Paths.get(instance.getDataFolder() +"/backups/"+ (i-10)+"data.yml");
		try {
			java.nio.file.Files.deleteIfExists(oldbackup);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//backing up
		Path source = Paths.get(instance.getDataFolder() + "/data.yml");
	    Path target = Paths.get(instance.getDataFolder() + "/backups/"+i+"data.yml");
	    try {
	    	java.nio.file.Files.copy(source, target);
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
	}
	
	public void onDisable() {
		utils.updateData();
		backUp();
		logInfo("&aBacked up data file in case of crash");
	}
	
    public static void logInfo(String msg) {
    	if(Main.getInstance().configFiles.getConfig() ==null || !Main.getInstance().configFiles.getConfig().isSet("ConsoleMessages") || Main.getInstance().configFiles.getConfig().getBoolean("ConsoleMessages")) {
    		instance.getLogger().info(msg.replace("&", "§"));
    	}
    }
    
    private boolean hasBungee(){
        boolean bungee = SpigotConfig.bungee;
        boolean onlineMode = Bukkit.getServer().getOnlineMode();
        if(bungee && (!(onlineMode))){
            return true;
        }
        return false;
    }
	
	@SuppressWarnings("deprecation")
	public void onEnable() {
		instance = this;
		configFiles = new Files();
		LootChest = new HashMap<String, Lootchest>();
		utils = new Utils();
		UseArmorStands = true;
		logInfo("Loading config files...");
		if(!configFiles.initFiles()) {
        	getLogger().info("§cConfig or data files couldn't be initialized, the plugin will stop.");
        	return;
        }
		if(!org.bukkit.Bukkit.getVersion().contains("1.7")){
			this.getServer().getPluginManager().registerEvents(new Armorstand(), this);
		}
		this.getServer().getPluginManager().registerEvents(new DeleteListener(), this);
		this.getServer().getPluginManager().registerEvents(new InventoryListeners(), this);
        this.getCommand("lootchest").setExecutor(new LootchestCommand());
        this.getCommand("lootchest").setTabCompleter(new LootchestCommand());
        super.onEnable();
        
        
        //In many versions, I add some text an config option. These lines are done to update config and language files without erasing options that are already set
        configFiles.setConfig("Particles.enable", true);
        configFiles.setConfig("Hologram_distance_to_chest", 1);
        configFiles.setConfig("UseHologram", true);
        configFiles.setConfig("RemoveEmptyChests", true);
        configFiles.setConfig("RemoveChestAfterFirstOpenning", false);
        configFiles.setConfig("respawn_notify.natural_respawn.enabled", true);
        configFiles.setConfig("respawn_notify.respawn_with_command.enabled", true);
        configFiles.setConfig("respawn_notify.respawn_all_with_command.enabled", true);
        configFiles.setConfig("respawn_notify.natural_respawn.message", "&6The chest &b[Chest] &6has just respawned at [x], [y], [z]!");
        configFiles.setConfig("respawn_notify.respawn_with_command.message", "&6The chest &b[Chest] &6has just respawned at [x], [y], [z]!");
        configFiles.setConfig("respawn_notify.respawn_all_with_command.message", "&6All chests where forced to respawn! Get them guys!");
        configFiles.setConfig("PreventHopperPlacingUnderLootChest", true);
        configFiles.setConfig("respawn_notify.per_world_message", true);
        configFiles.setConfig("respawn_notify.message_on_chest_take", true);
        configFiles.setConfig("Minimum_Number_Of_Players_For_Natural_Spawning", 0);
        configFiles.setConfig("use_players_locations_for_randomspawn", false);
        configFiles.setConfig("Cooldown_Before_Plugin_Start", 0);
        configFiles.setConfig("Prevent_Chest_Spawn_In_Protected_Places", false);
        configFiles.setLang("PluginReloaded", "&aConfig file, lang, and chest data were reloaded");
        configFiles.setLang("PlayerIsNotOnline", "&cThe player [Player] is not online");
        configFiles.setLang("givefrom", "&aYou were given the [Chest] chest by [Player]");
        configFiles.setLang("giveto", "&aYou gave the chest [Chest] to player [Player]");
        configFiles.setLang("ListCommand", "&aList of all chests: [List]");
        configFiles.setLang("Menu.main.copychest", "&1Copy settings from anyther chest");
        configFiles.setLang("Menu.copy.name", "&1Choose a chest to copy its settings");
        configFiles.setLang("copiedChest", "&6You copied the chest &b[Chest1] &6into the chest &b[Chest2]");
        configFiles.setLang("changedPosition", "&6You set the location of chest &b[Chest] &6to your location");
        configFiles.setLang("settime", "&6You successfully set the time of the chest &b[Chest]");
        configFiles.setLang("Menu.time.infinite", "&6Desactivates the respawn time");
        configFiles.setLang("chestRadiusSet", "&aYou defined a spawn radius for the chest [Chest]");
        configFiles.setLang("Menu.copy.page", "&2---> Page &b[Number]");
        configFiles.setLang("teleportedToChest", "&aYou were teleported to chest [Chest]");
        configFiles.setLang("enabledFallEffect", "&aYou enabled fall effect for chest &b[Chest]");
        configFiles.setLang("disabledFallEffect", "&cYou disabled fall effect for chest &b[Chest]");
        configFiles.setLang("playerTookChest", "&6Oh no! &b[Player] &6found the chest &b[Chest] &6and took everything in it!");
        configFiles.setLang("disabledChestRadius", "&cYou disabled random spawn for chest [Chest]");
        configFiles.setLang("Menu.main.disable_fall", "&aFall effect is enabled. Click to &cDISABLE &ait");
        configFiles.setLang("Menu.main.disable_respawn_natural", "&aNatural-respawn message is enabled. Click to &cDISABLE &ait");
        configFiles.setLang("Menu.main.disable_respawn_cmd", "&aCommand-respawn message is enabled. Click to &cDISABLE &ait");
        configFiles.setLang("Menu.main.disable_take_message", "&aMessage on chest take is enabled. Click to &cDISABLE &ait");
        configFiles.setLang("Menu.main.enable_fall", "&cFall effect is disabled. Click to &aENABLE &cit");
        configFiles.setLang("Menu.main.enable_respawn_natural", "&cNatural-respawn message is disabled. Click to &aENABLE &cit");
        configFiles.setLang("Menu.main.enable_respawn_cmd", "&cCommand-respawn message is disabled. Click to &aENABLE &cit");
        configFiles.setLang("Menu.main.type", "&1Choose type (Barrel, trapped chest, chest)");
        configFiles.setLang("Menu.type.name", "&1Choose type (Barrel, trapped chest, chest)");
        configFiles.setLang("Menu.main.enable_take_message", "&cMessage on chest take is disabled. Click to &aENABLE &cit");
        configFiles.setLang("locate_command.main_message",  "&6Location of loot chests:");
        configFiles.setLang("editedChestType", "&aEdited type of chest &b[Chest]");
        configFiles.setLang("locate_command.chest_list", "- &b[Chest]&6: [x], [y], [z] in world [world]");
        if (configFiles.getLang().isSet("help.line1")) {
            final List<String> tab = new ArrayList<String>();
            for (int i = 1; i <= 17; ++i) {
                if (configFiles.getLang().getString("help.line" + i) != null) {
                    tab.add(configFiles.getLang().getString("help.line" + i));
                }
            }
            configFiles.getLang().set("help", (Object)tab);
            try {
                configFiles.getLang().save(configFiles.getLangF());
                configFiles.getLang().load(configFiles.getLangF());
            }
            catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        configFiles.setConfig("Fall_Effect.Let_Block_Above_Chest_After_Fall", false);
        configFiles.setConfig("Fall_Effect.Optionnal_Color_If_Block_Is_Wool", "CYAN");
        configFiles.setConfig("Fall_Effect.Block",  configFiles.getConfig().getString("Fall_Effect_Block"));
        configFiles.setConfig("Fall_Effect.Height",  configFiles.getConfig().getInt("Fall_Effect_Height"));
        configFiles.setConfig("Fall_Effect.Enabled",  configFiles.getConfig().getBoolean("Enable_fall_effect"));
        configFiles.setConfig("Fall_Effect.Enable_Fireworks",  true);
        configFiles.setConfig("Fall_Effect.Speed", 0.9);
        configFiles.setConfig("respawn_notify.bungee_broadcast", false);
        configFiles.setConfig("ConsoleMessages", true);
        configFiles.setConfig("save_Chest_Locations_At_Every_Spawn", true);
        configFiles.setConfig("Show_Timer_On_Hologram", true);
        configFiles.setConfig("Protect_From_Explosions", false);
        configFiles.setLang("Menu.time.notInfinite", "&6Reactivate respawn time");
        configFiles.setLang("commandGetName", "&6Your'e looking the chest &b[Chest]");
        if(!configFiles.getLang().getStringList("help").toString().contains("getname")){
        	Bukkit.broadcastMessage(configFiles.getLang().getStringList("help").toString());
        	List<String> help = configFiles.getLang().getStringList("help");
        	help.add("&a/lc getname &b: get the name of the targeted LootChest");
        	configFiles.getLang().set("help", help);
        	configFiles.saveLang();
        }
        if(!configFiles.getLang().getStringList("help").toString().contains("locate")){
        	Bukkit.broadcastMessage(configFiles.getLang().getStringList("help").toString());
        	List<String> help = configFiles.getLang().getStringList("help");
        	help.add("&a/lc locate &b: gives locations of all chests that haves natural respawn message enabled");
        	configFiles.getLang().set("help", help);
        	configFiles.saveLang();        	
        }
        if(configFiles.getConfig().isSet("Optionnal_Color_If_ArmorStand_Head_Is_Wool")) {
        	configFiles.getConfig().set("Fall_Effect.Optionnal_Color_If_Block_Is_Wool",configFiles.getConfig().getString("Optionnal_Color_If_ArmorStand_Head_Is_Wool") );
        	configFiles.getConfig().set("Optionnal_Color_If_ArmorStand_Head_Is_Wool", null);
        	configFiles.getConfig().set("Fall_Effect.Block", configFiles.getConfig().getString("Armor_Stand_Head_Item"));
        	configFiles.getConfig().set("Armor_Stand_Head_Item", null);
        	configFiles.getConfig().set("Use_ArmorStand_Instead_Of_Block", null);
        	configFiles.getConfig().set("Fall_Effect.Let_Block_Above_Chest_After_Fall", configFiles.getConfig().getBoolean("Let_ArmorStand_On_Chest_After_Fall"));
        	configFiles.getConfig().set("Let_ArmorStand_On_Chest_After_Fall", null);
        	configFiles.saveConfig();
        }
        if(configFiles.getConfig().isSet("Fall_Effect_Height")){
        	configFiles.getConfig().set("Fall_Effect_Height", null);
        	configFiles.getConfig().set("Fall_Effect_Block", null);
        	configFiles.getConfig().set("Enable_fall_effect", null);
        	configFiles.saveConfig();
        }
        if(configFiles.getLang().getString("Menu.chances.lore").equals("&aLeft click: +1; right: -1; shift+right: -10; shift+left: +10; tab+right: -50") || configFiles.getLang().getString("Menu.chances.lore").equals("&aLeft click to up percentage, Right click to down it")) {
        	configFiles.getLang().set("Menu.chances.lore", "&aLeft click: +1||&aright: -1||&ashift+right: -10||&ashift+left: +10||&atab+right: -50");
        	configFiles.saveLang();
        }
        configFiles.saveConfig();
        configFiles.saveLang();
        this.getServer().getMessenger().registerOutgoingPluginChannel((Plugin)this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel((Plugin)this, "BungeeCord", (org.bukkit.plugin.messaging.PluginMessageListener)new BungeeChannel(this));

        if(Bukkit.getVersion().contains("Paper") && !Bukkit.getVersion().contains("1.8") && !Bukkit.getVersion().contains("1.7") ) {
        	if(org.bukkit.Bukkit.getServer().spigot().getPaperConfig().isSet("world-settings.default.armor-stands-tick")) {
	        	if(!org.bukkit.Bukkit.getServer().spigot().getPaperConfig().getBoolean("world-settings.default.armor-stands-tick")) {
	        		UseArmorStands = false;
	        		getLogger().info("§eYou disabled 'armor-stands-tick' in paper.yml. ArmorStands will not have gravity, so fall effect will use falling blocks instead! Some blocks can't be used as falling blocks. If so, only fireworks will show!");
	        		getLogger().info("§eIf no blocks are spawned with the fireworks, use another type of block for fall-effect in config.yml or enable 'armor-stands-tick' in paper.yml");
	        	}
        	}
        }
        
        configs= Config.getInstance(configFiles.getConfig());
        if(!hasBungee() && configs.NOTE_bungee_broadcast) {
    		getLogger().info("§cYou enaled bungee broadcast in config but you didn't enable bungeecord in spigot config!");
    		getLogger().info("§cSo if this server isn't in a bungee network, no messages will be sent at all on chest spawn!");
        }
        
        /*

        config.setConfig("Fall_Effect_Block", "NOTE_BLOCK");
       
        config.setLang("PluginReloaded", "&aConfig file, lang, and chest data were reloaded");*/

        

        if(configs.CheckForUpdates) {
        	logInfo("Checking for update...");
        	 new Updater(this);
        }

        //initialisation des matériaux dans toutes les verions du jeu
        //initializing materials in all game versions, to allow cross-version compatibility
        logInfo("Starting particles...");
        Mat.init_materials();
        

    		initParticles();
        
        //One particle was created in 1.13 so that other versions won't have it. Let's remove it if you're not in 1.13
        if (!(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16"))) {
        	particules[21] = ParticleEffect.valueOf("FOOTSTEP");
        }

        

        //Initialisation des particules
        //Particle initialization
  
    	//loop de tous les coffres tous les 1/4 (modifiable dans la config) de secondes pour faire spawn des particules
    	//loop of all chests every 1/4 (editable in config) of seconds to spawn particles 
    	new BukkitRunnable() {
    		public void run() {
    			double radius = configs.PART_radius;
    			if (configs.PART_enable) {
    				for(Location keys : part.keySet()) {
    					Boolean loaded = keys.getWorld().isChunkLoaded(keys.getBlockX()/16, keys.getBlockZ()/16) ;
    					if (loaded) {
	    					int players = 0;
	    					if(Bukkit.getVersion().contains("1.7") || Bukkit.getVersion().contains("1.6") ) {
	    						players = org.bukkit.Bukkit.getOnlinePlayers().toArray().length;
	    					}else {
	    						players = org.bukkit.Bukkit.getOnlinePlayers().size();
	    					}
	    					if( players>0) {
	    						if(Bukkit.getVersion().contains("1.12") || Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16")) {
	    							keys.getWorld().spawnParticle( (org.bukkit.Particle) part.get(keys), keys, configs.PART_number, radius, radius, radius, configs.PART_speed);
	        					} 
	    						else{
	    							((ParticleEffect) part.get(keys)).send(keys.getWorld().getPlayers(), keys, radius, radius, radius, configs.PART_speed, configs.PART_number, 50);
	    						}
	
	    					}
    					}
    					
    				}
    			}
    		}
    	}.runTaskTimer(this, 0, configs.PART_respawn_ticks);
    	Integer cooldown = configs.Cooldown_Before_Plugin_Start;
    	if(cooldown>0) {
    		logInfo("Chests will load in "+ cooldown + " seconds.");
    	}
        this.getServer().getScheduler().runTaskLater(this, (Runnable)new Runnable() {
            @Override
            public void run() {
		    	logInfo("Loading chests...");
		    	long current = (new Timestamp(System.currentTimeMillis())).getTime();
				for(String keys : configFiles.getData().getConfigurationSection("chests").getKeys(false)) {
					String name = configFiles.getData().getString("chests." + keys + ".position.world");
					String randomname = name;
					if( configFiles.getData().getInt("chests." + keys + ".randomradius")>0) {
						 randomname = configFiles.getData().getString("chests." + keys + ".randomPosition.world");
					}
					if(name != null && org.bukkit.Bukkit.getWorld(randomname) != null && org.bukkit.Bukkit.getWorld(name) != null) {
						Main.getInstance().getLootChest().put(keys, new Lootchest(keys));
					}
					else {
		    			Main.getInstance().getLogger().info("§cCouldn't load chest "+keys +" : the world " + configFiles.getData().getString("chests." + keys + ".position.world") + " is not loaded.");
					}
		    	}
				logInfo("Loaded "+LootChest.size() + " Lootchests in "+((new Timestamp(System.currentTimeMillis())).getTime()-current) + " miliseconds");
				logInfo("Starting LootChest timers asynchronously...");
				for (final Lootchest lc : Main.getInstance().LootChest.values()) {
		            Bukkit.getScheduler().scheduleAsyncDelayedTask(getInstance(), () -> {
		                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
		                            if (!Main.utils.restoreChest(lc, false)) {
		                                Main.utils.sheduleRespawn(lc);
		                            }
		                            Main.utils.reactivateEffects(lc);
		                    }, 0L);
		            }, 5L);
		        }
		    	logInfo("Plugin loaded");
            
	        }
	    }, cooldown*20);
    		/*if(!config.getData().isSet("chests." + keys + ".time") ) {
    			config.getData().set("chests." + keys, null);
				config.reloadData();
    		}*/
        
	}
    		

    	
  
   
    
	
	public static Main getInstance() {
        return instance;
    }
	public static Files getConfigFiles() {
        return Main.getInstance().configFiles;
    }
	
	public FileConfiguration getData() {
		return configFiles.getData();
		
	}

	
	//particle initialozation
	private void initParticles() {
		Object parti[];
		if(Bukkit.getVersion().contains("1.12") || Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16")) {
			parti = new org.bukkit.Particle[] {org.bukkit.Particle.EXPLOSION_HUGE, org.bukkit.Particle.EXPLOSION_LARGE, org.bukkit.Particle.EXPLOSION_NORMAL, org.bukkit.Particle.FIREWORKS_SPARK, org.bukkit.Particle.WATER_BUBBLE, org.bukkit.Particle.SUSPENDED, org.bukkit.Particle.TOWN_AURA, org.bukkit.Particle.CRIT, org.bukkit.Particle.CRIT_MAGIC, org.bukkit.Particle.SMOKE_NORMAL, org.bukkit.Particle.SMOKE_LARGE, org.bukkit.Particle.SPELL_MOB, org.bukkit.Particle.SPELL_MOB_AMBIENT, org.bukkit.Particle.SPELL, org.bukkit.Particle.SPELL_INSTANT, org.bukkit.Particle.SPELL_WITCH, org.bukkit.Particle.NOTE, org.bukkit.Particle.PORTAL, org.bukkit.Particle.ENCHANTMENT_TABLE, org.bukkit.Particle.FLAME, org.bukkit.Particle.LAVA, org.bukkit.Particle.LAVA, org.bukkit.Particle.WATER_SPLASH, org.bukkit.Particle.WATER_WAKE, org.bukkit.Particle.CLOUD, org.bukkit.Particle.SNOWBALL, org.bukkit.Particle.DRIP_WATER, org.bukkit.Particle.DRIP_LAVA, org.bukkit.Particle.SNOW_SHOVEL, org.bukkit.Particle.SLIME, org.bukkit.Particle.HEART, org.bukkit.Particle.VILLAGER_ANGRY, org.bukkit.Particle.VILLAGER_HAPPY, org.bukkit.Particle.BARRIER};

		}else {
			parti = new ParticleEffect[] {ParticleEffect.EXPLOSION_HUGE, ParticleEffect.EXPLOSION_LARGE, ParticleEffect.EXPLOSION_NORMAL, ParticleEffect.FIREWORKS_SPARK, ParticleEffect.WATER_BUBBLE, ParticleEffect.SUSPENDED, ParticleEffect.TOWN_AURA, ParticleEffect.CRIT, ParticleEffect.CRIT_MAGIC, ParticleEffect.SMOKE_NORMAL, ParticleEffect.SMOKE_LARGE, ParticleEffect.SPELL_MOB, ParticleEffect.SPELL_MOB_AMBIENT, ParticleEffect.SPELL, ParticleEffect.SPELL_INSTANT, ParticleEffect.SPELL_WITCH, ParticleEffect.NOTE, ParticleEffect.PORTAL, ParticleEffect.ENCHANTMENT_TABLE, ParticleEffect.FLAME, ParticleEffect.LAVA, ParticleEffect.LAVA, ParticleEffect.WATER_SPLASH, ParticleEffect.WATER_WAKE, ParticleEffect.CLOUD, ParticleEffect.SNOWBALL, ParticleEffect.DRIP_WATER, ParticleEffect.DRIP_LAVA, ParticleEffect.SNOW_SHOVEL, ParticleEffect.SLIME, ParticleEffect.HEART, ParticleEffect.VILLAGER_ANGRY, ParticleEffect.VILLAGER_HAPPY, ParticleEffect.BARRIER};
		}
		for(int i = 0; i<parti.length; i++) {
			particules[i] = parti[i];
		}
	}
}
