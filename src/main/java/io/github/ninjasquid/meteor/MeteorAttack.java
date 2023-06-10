package io.github.ninjasquid.meteor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;


public class MeteorAttack extends JavaPlugin implements Listener {

		// The main list of all unexploded meteors
	    private ArrayList<Meteor> meteors = new ArrayList<>();
		
		
		@Override
	    public void onEnable() {
	        Bukkit.getPluginManager().registerEvents(this, this);
	        this.getCommand("meteor").setExecutor(new MeteorCommand(this));
	    }

		
	    @EventHandler
	    public void onTickEvent(ServerTickEndEvent event) {
	        
	    	int maxStormSize = getConfig().getInt("max_meteors");
	    	double meteorTickProbability = getConfig().getDouble("meteor_tick_probability");
	    	//int tick = event.getTickNumber();
	        
	    	
	    	// decide if a spontaneous meteor storm is triggered
	        if (Math.random() < meteorTickProbability) {
	        	// work out where the impact player is
	        	Player player = getRandomPlayer();
	        	queueMeteorStrike(player, ThreadLocalRandom.current().nextInt(1, maxStormSize));
	        }
	    	
	        // look through all current meteors in the sky
	        Iterator<Meteor> it = meteors.iterator();
	        // copy of the meteor list to filter out meteors that explode
	        ArrayList<Meteor> meteorsCopy = new ArrayList<>();
	        while (it.hasNext()) {
	        	Meteor meteor = it.next();
	        	// animate returns true if the meteor has exploded.
	        	boolean collided = meteor.animate();
	        	// if the meteor has reached its impact point it has exploded and we want to remove it from the 
	        	// main meteor list
	        	if (!collided) meteorsCopy.add(meteor);
	        }
	        this.meteors = meteorsCopy;
	        // update their position moving them towards their impact site
	        // if impact site reached do the explosion etc.
	        
	    }

	    // get a player by name or a random player if no name
	    Player getPlayer(String name) {
	    	Player player = null;
			if (name != null) {
				player = this.getServer().getPlayer(name);
			}
			if (player == null) {
				player = getRandomPlayer();
			}
			return player;
	    }
	    
	    Player getRandomPlayer() {
	    	List<? extends Player> players = new ArrayList<>(this.getServer().getOnlinePlayers());
	    	if (players.isEmpty()) return null;
	        int randomNum = ThreadLocalRandom.current().nextInt(0, players.size());
	        Player player = players.get(randomNum);
	        return player;
	    }
	    
		
		public void queueMeteorStrike(Player player, int nMeteors) {
			
			if (player == null) {
				this.getServer().getLogger().info("Meteor strike aborted as no players online");
				return;
			}
			
			player.spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 10);
			
			this.getServer().broadcast(Component.text("METEOR STRIKE WARNING!", TextColor.color(255, 0, 0)));
			
			// Determine the location of the impact of the strikes.
			Location loc = player.getLocation();
			// The bearing and elevation from which the strike will come
			
			double bearing = Math.random()*2*Math.PI;
			double elevation = Math.random()*Math.PI/2;
			double distanceToImpact3d = getConfig().getDouble("meteor_spawn_distance_3d");
			double speed3d = getConfig().getDouble("meteor_speed_blocks_per_ms");
			
			int maxDist = getConfig().getInt("impact_max_dist");
			int minDist = getConfig().getInt("impact_min_dist");
			double distanceOfImpactFromPlayer = Math.random()*(maxDist-minDist)+minDist;
			double directionOfImpactFromPlayer = Math.random()*2*Math.PI; // same as 360 degrees
			
			for (int i=0; i<nMeteors; i++) {
				
				// decide the impact site of each meteor
				// The scatter of multiple meteors
				
				Location impact = loc.add(
							distanceOfImpactFromPlayer * Math.cos(directionOfImpactFromPlayer), // x coord
							0, // y is height
							distanceOfImpactFromPlayer * Math.sin(directionOfImpactFromPlayer) // z coord
						).add(
								(Math.random()-0.5)*2*Math.sqrt((double) nMeteors)*getConfig().getDouble("meteor_cluster_spread"), // 10 is the spread 
								0,
								(Math.random()-0.5)*2*Math.sqrt((double) nMeteors)*getConfig().getDouble("meteor_cluster_spread") // 10 is the spread 
						).toHighestLocation();
				
				this.getServer().getLogger().info("Spawning meteor impact ["+impact.blockX()+";"+impact.blockY()+";"+impact.blockZ()+"]");
				
				// for test
				impact.getBlock().setType(Material.DIAMOND_BLOCK);
				
				Meteor m = new Meteor(
						impact,
						elevation,
						bearing,
						distanceToImpact3d * (Math.random()+99.5)/100,
						speed3d,
						this.getServer()
				);
				
				meteors.add(m);
				
				// but actually we need to create a list of meteors which have::
				// impact site,
				// bearing and elevation.
				// distance from impact.
				// speed towards impact site.
				// acceleration towards impact site.
				// composition? size?
				// actual position of the meteor is going to be 
				// impact site +
				// y: distanceToImpact3d * cos(elevation)
				// distanceToImpact2d: distanceToImpact3d * sin(elevation)
				// z: distanceToImpact2d * cos(bearing)
				// x: distanceToImpact2d * sin(bearing)
			
			}

	}
	    
}
