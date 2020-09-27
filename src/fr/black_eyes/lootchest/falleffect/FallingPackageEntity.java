package fr.black_eyes.lootchest.falleffect;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;



import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.util.Vector;
import org.inventivetalent.particle.ParticleEffect;


import fr.black_eyes.lootchest.Config;
import fr.black_eyes.lootchest.Main;


import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World;

public class FallingPackageEntity extends PackageEntity {
	Main instance = Main.getInstance();
	Config config = Main.getConfigFiles();
    World world;
    Location startLoc;
    Material material;
    Object blocky;
    Boolean armorstand;
    Boolean letAlive;
    Boolean loaded;
    Location target;
    Double speed;
    Boolean fireworks;
    
    public FallingPackageEntity(final Location loc, Boolean loaded,Location target) {
    	this.fireworks = config.getConfig().getBoolean("Fall_Effect.Enable_Fireworks");
    	this.target = target;
    	this.loaded = loaded;
    	this.letAlive = config.getConfig().getBoolean("Fall_Effect.Let_Block_Above_Chest_After_Fall");

    	this.armorstand = Main.UseArmorStands;
        this.blocky = null;
        this.startLoc = this.applyOffset(loc);
        this.world = loc.getWorld();
        this.material = Material.valueOf(config.getConfig().getString("Fall_Effect.Block"));
        this.speed = config.getConfig().getDouble("Fall_Effect.Speed");
        if (Bukkit.getVersion().contains("1.7")) {
        	this.armorstand = false;
        }
        this.summon();
    }
    

	@SuppressWarnings("deprecation")
	@Override
    public void summon() {
		if((!this.armorstand) && loaded) {
			this.blocky = this.world.spawnFallingBlock(startLoc, this.material, (byte)0);
		}else {
			if(!loaded && letAlive) {
				
				startLoc.setY(startLoc.getWorld().getHighestBlockYAt(startLoc)+2);
				if (Bukkit.getVersion().contains("1.15")|| Bukkit.getVersion().contains("1.16")) {
					startLoc.setY(startLoc.getWorld().getHighestBlockYAt(startLoc)+3);
				}
				
			}
			if(loaded || letAlive) {
				this.blocky = (org.bukkit.entity.ArmorStand) this.world.spawnEntity(startLoc, org.bukkit.entity.EntityType.ARMOR_STAND);
	
	
				((org.bukkit.entity.ArmorStand) blocky).setVisible(false); //Makes the ArmorStand invisible
			 	((org.bukkit.entity.ArmorStand) blocky).setHelmet(new ItemStack(this.material, 1));
			 	if(!Bukkit.getVersion().contains("1.13") && !Bukkit.getVersion().contains("1.14") && !Bukkit.getVersion().contains("1.15")&& !Bukkit.getVersion().contains("1.16")) {
				 	if(material.equals(Material.valueOf("WOOL"))) {
				 		((org.bukkit.entity.ArmorStand) blocky).setHelmet(new ItemStack(this.material, 1, DyeColor.valueOf(config.getConfig().getString("Optionnal_Color_If_Block_Is_Wool")).getDyeData()));
				 	}
			 	}
			 	((org.bukkit.entity.ArmorStand) blocky).setBasePlate(false);
			 	((org.bukkit.entity.ArmorStand) blocky).setGravity(true);
			}
		 	
		 	
		 	//((org.bukkit.entity.ArmorStand) blocky).setSmall(true);
		}
		if(loaded) {
			if(fireworks) {
				this.summonSpawnFireworks();
			}
			this.tick();
		}
    }
    
	public Location goodLocation() {
		Location loc = ((Entity) this.blocky).getLocation();
		if(!armorstand) return loc;
		else {
			Location loc2 = loc.clone();
			loc2.setY(loc.getY()+3);
			return loc2;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void tick() {
		Vector v = ((Entity) blocky).getVelocity();
		v.setY(-(speed));
		((Entity) blocky).setVelocity(v);
		
		if((((Entity) this.blocky).getLocation().getY() - target.getY()) <2) {
			if(!this.armorstand || this.armorstand && !this.letAlive) this.remove();
        }
		else if (this.world.getBlockAt(LocationUtils.offset(((Entity) this.blocky).getLocation(), 0.0, -1.0, 0.0)).getType() == Material.AIR) {
            ++this.counter;
            if(!Bukkit.getVersion().contains("1.8")) {
				if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16")) {
		           	this.world.spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, goodLocation(), 50, 0.1, 0.1, 0.1, 0.1);
				}				
				else{
				 ParticleEffect.SMOKE_NORMAL.send(((Entity) this.blocky).getLocation().getWorld().getPlayers(), ((Entity) this.blocky).getLocation(), 0.1, 0.1, 0.1, 0.1, 50, 100);			
				}
            }
            if (((Entity) this.blocky).isDead()) {
                final Location oldLoc = ((Entity) this.blocky).getLocation();
                final Vector oldVelocity = ((Entity) this.blocky).getVelocity();
        		if(!this.armorstand) {
        			((Entity) (this.blocky = this.world.spawnFallingBlock(oldLoc, this.material, (byte)0))).setVelocity(oldVelocity);
        		}else {
        			this.blocky = (org.bukkit.entity.ArmorStand) this.world.spawnEntity(oldLoc, org.bukkit.entity.EntityType.ARMOR_STAND);

        			((org.bukkit.entity.ArmorStand) blocky).setVisible(false); //Makes the ArmorStand invisible
        		 	((org.bukkit.entity.ArmorStand) blocky).setGravity(true);
        		 	((org.bukkit.entity.ArmorStand) blocky).setHelmet(new ItemStack(this.material, 1));
        		 	((Entity) blocky).setVelocity(oldVelocity);
        		}
                
            }
            if (this.counter % 5 == 0 && (   (((Entity) this.blocky).getLocation().getY() - target.getY()) >3 || counter > 100) && fireworks ) {
                this.summonUpdateFireworks();
            }
            if((((Entity) this.blocky).getLocation().getY() - target.getY()) <1) {
            	if(!this.armorstand || this.armorstand && !this.letAlive) this.remove();
            }
            else if(counter < 100){
            	this.retick();
            }else {
            	this.remove();
            }
        }
        else {
        	 this.remove();
        }
    }
    
    @Override
    public void remove() {
        ((Entity) this.blocky).remove();

    }
    
    private void summonUpdateFireworks() {
        //if (Main.getInstance().getConfig().getBoolean("options.fireworks_on_fall")) {
            final Firework fw = (Firework)this.world.spawnEntity(goodLocation(), EntityType.FIREWORK);
            final FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.RED).withColor(Color.WHITE).build());
            fw.setFireworkMeta(fwm);
            Main.getInstance().getServer().getScheduler().runTaskLater((Plugin)Main.getInstance(), (Runnable)new Runnable() {
                @Override
                public void run() {
                    fw.detonate();
                }
            }, 1L);
        //}
    }
    
    private void summonSpawnFireworks() {
        //if (Main.getInstance().getConfig().getBoolean("options.fireworks_on_fall")) {
            final Firework fw = (Firework)this.world.spawnEntity(goodLocation(), EntityType.FIREWORK);
            final FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.RED).withColor(Color.WHITE).build());
            fw.setFireworkMeta(fwm);
            Main.getInstance().getServer().getScheduler().runTaskLater((Plugin)Main.getInstance(), (Runnable)new Runnable() {
                @Override
                public void run() {
                    fw.detonate();
                }
            }, 1L);
        //}
    }
    
    private Location applyOffset(final Location loc) {
    	return loc;
    	/*
        final int bounds = Main.getInstance().getConfig().getInt("options.drop_location_offset");
        if (bounds < 1) {
            return loc;
        }
        final Random r = new Random();
        final int zOff = r.nextInt(bounds * 2) + 1 - bounds;
        final int xOff = r.nextInt(bounds * 2) + 1 - bounds;
        return LocationUtils.offset(loc, xOff, 0.0, zOff);
        */
    }
}
