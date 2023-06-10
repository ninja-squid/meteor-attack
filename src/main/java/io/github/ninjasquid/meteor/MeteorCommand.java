package io.github.ninjasquid.meteor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MeteorCommand implements CommandExecutor {

	private MeteorAttack plugin;

	public MeteorCommand(MeteorAttack plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		
		// find out if the command had a name given:
		String name = null;
		if (args.length > 0) {
			name = args[0];
		} else if (sender instanceof Player) {
			//otherwise use the name of the player who issued the command
			name = ((Player) sender).getName();
		}
		
		// This is the target of the meteor strike
		Player player = plugin.getPlayer(name);
		
		// How many meteors in strike?
		int meteors = 1;
		try {
			meteors = args.length > 1 ? Integer.parseInt(args[1]) : 1;
		} catch (NumberFormatException e) {
			sender.sendMessage("the second argument must be an integer");
		}
		
		meteors = Math.min(
				meteors, 
				plugin.getConfig().getInt("max_meteors"));
		
		plugin.queueMeteorStrike(player, meteors);
		
		return plugin.onCommand(sender, command, label, args);
	}

}
