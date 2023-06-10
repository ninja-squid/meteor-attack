package io.github.ninjasquid.meteor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.MetadataValueAdapter;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

public class Meteor {

	// but actually we need to create a list of meteors which have::
	// impact site,
	Location impact;
	
	// bearing and elevation.
	double bearing;
	double elevation;
	
	// distance from impact.
	double distanceToImpact3d;
	
	// speed towards impact site.
	double speed3d;

	private @NotNull Server server;
	private long lastUpdated;
	
	// acceleration towards impact site.
	// composition? size?
	// actual position of the meteor is going to be 
	// impact site +
	
	public Location currentPosition() {
		
		double y = distanceToImpact3d * Math.cos(elevation);
		double distanceToImpact2d = distanceToImpact3d * Math.sin(elevation);
		double z = distanceToImpact2d * Math.cos(bearing);
		double x = distanceToImpact2d * Math.sin(bearing);
		return impact.clone().add(x, y, z);
		
	}
	
	public Meteor updatePosition() {
		long timeMs = System.currentTimeMillis() - this.lastUpdated;
		distanceToImpact3d = distanceToImpact3d - speed3d * timeMs;
		this.lastUpdated = this.lastUpdated + timeMs;
		return this;
	}
	
	public boolean animate() {
		Block start = this.currentPosition().getBlock();
		this.updatePosition();
		
		if (distanceToImpact3d < 3) {
			server.broadcast(Component.text("Meteor landed: ["+impact.blockX()+";"+impact.blockY()+";"+impact.blockZ()+"]"));
			// The meteor has arrived at impact site.
			// 10 TNT = 40
			this.impact.createExplosion(40F, true);
			// TODO: Drop goodies.
			// animation complete will remove this meteor from the master list in MeteorAttack
			
			return true;
		} else {
			// The meteor is still travelling (presumably through air)
			Block end = this.currentPosition().getBlock();
			if (!start.equals(end)) {
				try {
					clear(start);
					set(end);
				} catch (Exception e) {
					server.getLogger().info("Could not set block: "+e.getMessage());
				}
			}
		}
		// anmiation not complete
		return false;
	}
	
	private void clear(Block block) {
		block.setType(Material.AIR);
		// Entity ent = block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		// ent.setMetadata(null, );
	}
	
	private void set(Block block) {
		if (!block.isLiquid()) block.setType(Material.MAGMA_BLOCK);
	}
	
	// constructor 
	public Meteor(
			Location impact,
			double elevation,
			double bearing,
			double distanceToImpact3d,
			double speed3d, 
			@NotNull Server server) {
		this.bearing = bearing;
		this.impact = impact;
		this.elevation = elevation;
		this.distanceToImpact3d = distanceToImpact3d;
		this.speed3d = speed3d;
		this.server = server;
		this.lastUpdated = System.currentTimeMillis();
	}
}
