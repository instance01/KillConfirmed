package com.comze_instancelabs.killconfirm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import net.milkbowl.vault.Metrics;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class Main extends JavaPlugin implements Listener {


	public static Economy econ = null;

	public static HashMap<String, Boolean> ingame = new HashMap<String, Boolean>(); 
	public static HashMap<Player, String> arenap = new HashMap<Player, String>(); 
	public static HashMap<String, String> arenap_ = new HashMap<String, String>(); 
	public static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>();
	public static HashMap<String, String> pteam = new HashMap<String, String>(); // red = 1; blue = 2

	int default_max_players = 4;
	int default_min_players = 3;

	boolean economy = true;
	int reward = 30;
	int itemid = 264;
	int itemamount = 1;
	boolean command_reward = false;
	String cmd = "";
	boolean start_announcement = false;
	boolean winner_announcement = false;

	int start_countdown = 5;

	public String saved_arena = "";
	public String saved_lobby = "";
	public String saved_setup = "";
	public String saved_mainlobby = "";
	public String not_in_arena = "";
	public String reloaded = "";
	public String arena_ingame = "";
	public String arena_invalid = "";
	public String arena_invalid_sign = "";
	public String you_fell = "";
	public String arena_invalid_component = "";
	public String you_won = "";
	public String starting_in = "";
	public String starting_in2 = "";
	public String arena_full = "";
	public String removed_arena = "";
	public String winner_an = "";

	// anouncements
	public String starting = "";
	public String started = "";

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		getConfig().options().header("I recommend you to set auto_updating to true for possible future bugfixes. If use_economy is set to false, the winner will get the item reward.");
		getConfig().addDefault("config.auto_updating", true);
		getConfig().addDefault("config.default_max_players", 4);
		getConfig().addDefault("config.default_min_players", 3);
		getConfig().addDefault("config.use_economy_reward", true);
		getConfig().addDefault("config.money_reward_per_game", 30);
		getConfig().addDefault("config.itemid", 264); // diamond
		getConfig().addDefault("config.itemamount", 1);
		getConfig().addDefault("config.use_command_reward", false);
		getConfig().addDefault("config.command_reward", "pex user [user] group set ColorPro");
		getConfig().addDefault("config.start_announcement", false);
		getConfig().addDefault("config.winner_announcement", false);

		getConfig().addDefault("strings.saved.arena", "&aSuccessfully saved arena.");
		getConfig().addDefault("strings.saved.lobby", "&aSuccessfully saved lobby.");
		getConfig().addDefault("strings.saved.setup", "&6Successfully saved spawn. Now setting up, might &2lag&6 a little bit.");
		getConfig().addDefault("strings.removed_arena", "&cSuccessfully removed arena.");
		getConfig().addDefault("strings.not_in_arena", "&cYou don't seem to be in an arena right now.");
		getConfig().addDefault("strings.config_reloaded", "&6Successfully reloaded config.");
		getConfig().addDefault("strings.arena_is_ingame", "&cThe arena appears to be ingame.");
		getConfig().addDefault("strings.arena_invalid", "&cThe arena appears to be invalid.");
		getConfig().addDefault("strings.arena_invalid_sign", "&cThe arena appears to be invalid, because a join sign is missing.");
		getConfig().addDefault("strings.arena_invalid_component", "&2The arena appears to be invalid (missing components or misstyped arena)!");
		getConfig().addDefault("strings.you_fell", "&3You fell! Type &6/cm leave &3to leave.");
		getConfig().addDefault("strings.you_won", "&aYou won this round, awesome man! Here, enjoy your reward.");
		getConfig().addDefault("strings.starting_in", "&aStarting in &6");
		getConfig().addDefault("strings.starting_in2", "&a seconds.");
		getConfig().addDefault("strings.arena_full", "&cThis arena is full!");
		getConfig().addDefault("strings.starting_announcement", "&aStarting a new killconfirmed Game in &6");
		getConfig().addDefault("strings.started_announcement", "&aA new killconfirmed Round has started!");
		getConfig().addDefault("strings.winner_announcement", "&6<player> &awon the game on arena &6<arena>!");

		getConfig().options().copyDefaults(true);
		if (getConfig().isSet("config.min_players")) {
			getConfig().set("config.min_players", null);
		}
		this.saveConfig();

		getConfigVars();

		
		try{
			Metrics metrics = new Metrics(this); 
			metrics.start();
		}catch(IOException e){
			
		}
		
		/*
		 * if (getConfig().getBoolean("config.auto_updating")) { Updater updater
		 * = new Updater(this, 71774, this.getFile(),
		 * Updater.UpdateType.DEFAULT, false); }
		 */

		if (economy) {
			if (!setupEconomy()) {
				getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
				economy = false;
			}
		}

	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public void getConfigVars() {
		default_max_players = getConfig().getInt("config.default_max_players");
		default_min_players = getConfig().getInt("config.default_min_players");
		reward = getConfig().getInt("config.money_reward");
		itemid = getConfig().getInt("config.itemid");
		itemamount = getConfig().getInt("config.itemamount");
		economy = getConfig().getBoolean("config.use_economy_reward");
		command_reward = getConfig().getBoolean("config.use_command_reward");
		cmd = getConfig().getString("config.command_reward");
		start_countdown = getConfig().getInt("config.start_countdown");
		start_announcement = getConfig().getBoolean("config.start_announcement");
		winner_announcement = getConfig().getBoolean("config.winner_announcement");

		saved_arena = getConfig().getString("strings.saved.arena").replaceAll("&", "§");
		saved_lobby = getConfig().getString("strings.saved.lobby").replaceAll("&", "§");
		saved_setup = getConfig().getString("strings.saved.setup").replaceAll("&", "§");
		saved_mainlobby = "§aSuccessfully saved main lobby";
		not_in_arena = getConfig().getString("strings.not_in_arena").replaceAll("&", "§");
		reloaded = getConfig().getString("strings.config_reloaded").replaceAll("&", "§");
		arena_ingame = getConfig().getString("strings.arena_is_ingame").replaceAll("&", "§");
		arena_invalid = getConfig().getString("strings.arena_invalid").replaceAll("&", "§");
		arena_invalid_sign = getConfig().getString("strings.arena_invalid_sign").replaceAll("&", "§");
		you_fell = getConfig().getString("strings.you_fell").replaceAll("&", "§");
		arena_invalid_component = getConfig().getString("strings.arena_invalid_component").replace("&", "§");
		you_won = getConfig().getString("strings.you_won").replaceAll("&", "§");
		starting_in = getConfig().getString("strings.starting_in").replaceAll("&", "§");
		starting_in2 = getConfig().getString("strings.starting_in2").replaceAll("&", "§");
		arena_full = getConfig().getString("strings.arena_full").replaceAll("&", "§");
		starting = getConfig().getString("strings.starting_announcement").replaceAll("&", "§");
		started = getConfig().getString("strings.started_announcement").replaceAll("&", "§");
		removed_arena = getConfig().getString("strings.removed_arena").replaceAll("&", "§");
		winner_an = getConfig().getString("strings.winner_announcement").replaceAll("&", "§");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("kc") || cmd.getName().equalsIgnoreCase("killconfirmed")) {
			if (args.length > 0) {
				String action = args[0];
				if (action.equalsIgnoreCase("createarena")) {
					// create arena
					if (args.length > 1) {
						if (sender.hasPermission("killconfirmed.setup")) {
							String arenaname = args[1];
							getConfig().set(arenaname + ".name", arenaname);
							this.saveConfig();
							this.setArenaDifficulty(arenaname, 1);
							sender.sendMessage(saved_arena);
						}
					}
				} else if (action.equalsIgnoreCase("removearena")) {
					// remove arena
					if (args.length > 1) {
						if (sender.hasPermission("killconfirmed.setup")) {
							String arenaname = args[1];
							if (isValidArena(arenaname)) {
								sender.sendMessage("§cRemoving " + arenaname + ". ");
								try {
									getSignFromArena(arenaname).getBlock().setType(Material.AIR);
								} catch (Exception e) {

								}
								getConfig().set(arenaname, null);
								this.saveConfig();
								sender.sendMessage(removed_arena);
							} else {
								sender.sendMessage(arena_invalid);
							}
						}
					}
				} else if (action.equalsIgnoreCase("setlobby")) {
					if (args.length > 1) {
						if (sender.hasPermission("killconfirmed.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							getConfig().set(arenaname + ".lobby.world", p.getWorld().getName());
							getConfig().set(arenaname + ".lobby.loc.x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".lobby.loc.y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".lobby.loc.z", p.getLocation().getBlockZ());
							this.saveConfig();
							sender.sendMessage(saved_lobby);
						}
					}
				} else if (action.equalsIgnoreCase("setspawn")) {
					if (args.length > 2) {
						if (sender.hasPermission("killconfirmed.setup")) {
							Player p = (Player) sender;
							String arenaname = args[1];
							String count = args[2];
							if(!isNumeric(count)){
								sender.sendMessage(ChatColor.RED + "Please provide a number as second argument, 1 or 2!");
								return true;
							}
							getConfig().set(arenaname + ".spawn" + count + ".world", p.getWorld().getName());
							getConfig().set(arenaname + ".spawn" + count + ".loc.x", p.getLocation().getBlockX());
							getConfig().set(arenaname + ".spawn" + count + ".loc.y", p.getLocation().getBlockY());
							getConfig().set(arenaname + ".spawn" + count + ".loc.z", p.getLocation().getBlockZ());
							this.saveConfig();
							sender.sendMessage(ChatColor.GREEN + "Successfully saved spawn " + count);
						}
					}
				} else if (action.equalsIgnoreCase("setmainlobby")) {
					if (sender.hasPermission("killconfirmed.setup")) {
						Player p = (Player) sender;
						getConfig().set("mainlobby.world", p.getWorld().getName());
						getConfig().set("mainlobby.loc.x", p.getLocation().getBlockX());
						getConfig().set("mainlobby.loc.y", p.getLocation().getBlockY());
						getConfig().set("mainlobby.loc.z", p.getLocation().getBlockZ());
						this.saveConfig();
						sender.sendMessage(saved_mainlobby);
					}
				} else if (action.equalsIgnoreCase("leave")) {
					Player p = (Player) sender;
					if (arenap.containsKey(p)) {
						leaveArena(p, true, false);
					} else {
						p.sendMessage(not_in_arena);
					}
				} else if (action.equalsIgnoreCase("setmaxplayers")) {
					if (sender.hasPermission("killconfirmed.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String playercount = args[2];
							if (!isNumeric(playercount)) {
								playercount = Integer.toString(default_max_players);
								sender.sendMessage("§cPlayercount is invalid. Setting to default value.");
							}
							if (!getConfig().isSet(arena)) {
								sender.sendMessage("§cCould not find this arena.");
								return true;
							}
							this.setArenaMaxPlayers(arena, Integer.parseInt(playercount));
							sender.sendMessage("§eSuccessfully set!");
						} else {
							sender.sendMessage("§cUsage: /cm setmaxplayers [arena] [count].");
						}
					}
				} else if (action.equalsIgnoreCase("setminplayers")) {
					if (sender.hasPermission("killconfirmed.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String playercount = args[2];
							if (!isNumeric(playercount)) {
								playercount = Integer.toString(default_min_players);
								sender.sendMessage("§cPlayercount is invalid. Setting to default value.");
							}
							if (!getConfig().isSet(arena)) {
								sender.sendMessage("§cCould not find this arena.");
								return true;
							}
							this.setArenaMinPlayers(arena, Integer.parseInt(playercount));
							sender.sendMessage("§eSuccessfully set!");
						} else {
							sender.sendMessage("§cUsage: /cm setminplayers [arena] [count].");
						}
					}
				} else if (action.equalsIgnoreCase("setdifficulty")) {
					if (sender.hasPermission("killconfirmed.setup")) {
						if (args.length > 2) {
							String arena = args[1];
							String difficulty = args[2];
							if (!isNumeric(difficulty)) {
								difficulty = "1";
								sender.sendMessage("§cDifficulty is invalid. Possible difficulties: 0, 1, 2.");
							}
							if (!getConfig().isSet(arena)) {
								sender.sendMessage("§cCould not find this arena.");
								return true;
							}
							this.setArenaDifficulty(arena, Integer.parseInt(difficulty));
							sender.sendMessage("§eSuccessfully set!");
						} else {
							sender.sendMessage("§cUsage: /cm setdifficulty [arena] [difficulty]. Difficulty can be 0, 1 or 2.");
						}
					}
				} else if (action.equalsIgnoreCase("join")) {
					if (args.length > 1) {
						if (isValidArena(args[1])) {
							Sign s = null;
							try {
								s = this.getSignFromArena(args[1]);
							} catch (Exception e) {
								getLogger().warning("No sign found for arena " + args[1] + ". May lead to errors.");
							}
							if (s != null) {
								if (s.getLine(1).equalsIgnoreCase("§2[join]")) {
									joinLobby((Player) sender, args[1]);
								} else {
									sender.sendMessage(arena_ingame);
								}
							} else {
								sender.sendMessage(arena_invalid_sign);
							}
						} else {
							sender.sendMessage(arena_invalid);
						}
					}
				} else if (action.equalsIgnoreCase("start")) {
					if (args.length > 1) {
						if (sender.hasPermission("killconfirmed.start")) {
							final String arena = args[1];
							if (!ingame.containsKey(arena)) {
								ingame.put(arena, false);
							}
							int count = 0;
							for (Player p : arenap.keySet()) {
								if (arenap.get(p).equalsIgnoreCase(arena)) {
									count++;
								}
							}
							if (count < 1) {
								sender.sendMessage("§cNoone is in this arena.");
								return true;
							}
							if (!ingame.get(arena)) {
								setAllTeams(arena);
								ingame.put(arena, true);
								for (Player p_ : arenap.keySet()) {
									if (arenap.get(p_).equalsIgnoreCase(arena)) {
										final Player p__ = p_;
										Bukkit.getScheduler().runTaskLater(this, new Runnable() {
											public void run() {
												p__.teleport(getSpawnForPlayer(arena, pteam.get(p__.getName())));
											}
										}, 5);
									}
								}
								Bukkit.getScheduler().runTaskLater(this, new Runnable() {
									public void run() {
										start(arena);
									}
								}, 10);
							}
						}
					}
				} else if (action.equalsIgnoreCase("reload")) {
					if (sender.hasPermission("killconfirmed.reload")) {
						this.reloadConfig();
						getConfigVars();
						sender.sendMessage(reloaded);
					}
				} else if (action.equalsIgnoreCase("list")) {
					if (sender.hasPermission("killconfirmed.list")) {
						sender.sendMessage("§6-= Arenas =-");
						for (String arena : getConfig().getKeys(false)) {
							if (!arena.equalsIgnoreCase("mainlobby") && !arena.equalsIgnoreCase("strings") && !arena.equalsIgnoreCase("config")) {
								sender.sendMessage("§2" + arena);
							}
						}
					}
				} else {
					sender.sendMessage("§6-= KillConfirmed §2help: §6=-");
					sender.sendMessage("§2To §6setup the main lobby §2, type in §c/kc setmainlobby");
					sender.sendMessage("§2To §6setup §2a new arena, type in the following commands:");
					sender.sendMessage("§2/kc createarena [name]");
					sender.sendMessage("§2/kc setlobby [name] §6 - for the waiting lobby");
					sender.sendMessage("§2/kc setspawn [name] [count]");
					sender.sendMessage("");
					sender.sendMessage("§2You can join with §c/kc join [name] §2and leave with §c/kc leave§2.");
					sender.sendMessage("§2You can force an arena to start with §c/kc start [name]§2.");
				}
			} else {
				sender.sendMessage("§6-= KillConfirmed §2help: §6=-");
				sender.sendMessage("§2To §6setup the main lobby §2, type in §c/kc setmainlobby");
				sender.sendMessage("§2To §6setup §2a new arena, type in the following commands:");
				sender.sendMessage("§2/kc createarena [name]");
				sender.sendMessage("§2/kc setlobby [name] §6 - for the waiting lobby");
				sender.sendMessage("§2/kc setspawn [name] [count]");
				sender.sendMessage("");
				sender.sendMessage("§2You can join with §c/kc join [name] §2and leave with §c/kc leave§2.");
				sender.sendMessage("§2You can force an arena to start with §c/kc start [name]§2.");
			}
			return true;
		}
		return false;
	}

	public ArrayList<String> left_players = new ArrayList<String>();

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (arenap.containsKey(event.getPlayer())) {
			String arena = arenap.get(event.getPlayer());
			getLogger().info(arena);
			int count = 0;
			for (Player p_ : arenap.keySet()) {
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					count++;
				}
			}

			try {
				Sign s = this.getSignFromArena(arena);
				if (s != null) {
					s.setLine(1, "§2[Join]");
					s.setLine(3, Integer.toString(count - 1) + "/" + Integer.toString(getArenaMaxPlayers(arena)));
					s.update();
				}
			} catch (Exception e) {
				getLogger().warning("You forgot to set a sign for arena " + arena + "! This might lead to errors.");
			}

			leaveArena(event.getPlayer(), true, true);
			left_players.add(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (left_players.contains(event.getPlayer().getName())) {
			final Player p = event.getPlayer();
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					p.teleport(getMainLobby());
					p.setFlying(false);
				}
			}, 5);
			left_players.remove(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (arenap_.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHunger(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (arenap_.containsKey(p.getName())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		// if (arenap_.containsKey(event.getPlayer().getName())) {
		if (arenap.containsKey(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		// if (arenap_.containsKey(event.getPlayer().getName())) {
		if (arenap.containsKey(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	/*
	 * @EventHandler public void onPlayerMove(PlayerMoveEvent event) { if
	 * (arenap_.containsKey(event.getPlayer().getName())) { if
	 * (lost.containsKey(event.getPlayer())) { Location l =
	 * getSpawn(lost.get(event.getPlayer())); final Location spectatorlobby =
	 * new Location(l.getWorld(), l.getBlockX(), l.getBlockY() + 30,
	 * l.getBlockZ()); if (event.getPlayer().getLocation().getBlockY() <
	 * spectatorlobby.getBlockY() || event.getPlayer().getLocation().getBlockY()
	 * > spectatorlobby.getBlockY()) { final Player p = event.getPlayer(); final
	 * float b = p.getLocation().getYaw(); final float c =
	 * p.getLocation().getPitch(); final String arena =
	 * arenap.get(event.getPlayer());
	 * Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new
	 * Runnable() {
	 * 
	 * @Override public void run() { try { p.setAllowFlight(true);
	 * p.setFlying(true); p.teleport(new Location(p.getWorld(),
	 * p.getLocation().getBlockX(), spectatorlobby.getBlockY(),
	 * p.getLocation().getBlockZ(), b, c)); updateScoreboard(arena); } catch
	 * (Exception e) { e.printStackTrace(); } } }, 5); p.sendMessage(you_fell);
	 * } } if (event.getPlayer().getLocation().getBlockY() <
	 * getSpawn(arenap_.get(event.getPlayer().getName())).getBlockY() - 2) {
	 * lost.put(event.getPlayer(), arenap.get(event.getPlayer())); final Player
	 * p__ = event.getPlayer(); final String arena =
	 * arenap.get(event.getPlayer()); Bukkit.getScheduler().runTaskLater(this,
	 * new Runnable() { public void run() { try { Location l = getSpawn(arena);
	 * p__.teleport(new Location(l.getWorld(), l.getBlockX(), l.getBlockY() +
	 * 30, l.getBlockZ())); p__.setAllowFlight(true); p__.setFlying(true); }
	 * catch (Exception e) { e.printStackTrace(); } } }, 5);
	 * 
	 * int count = 0;
	 * 
	 * for (Player p : arenap.keySet()) { if
	 * (arenap.get(p).equalsIgnoreCase(arena)) { if (!lost.containsKey(p)) {
	 * count++; } } }
	 * 
	 * if (count < 2) { // last man standing! stop(arena); } } } }
	 */

	@EventHandler
	public void onSignUse(PlayerInteractEvent event) {
		if (event.hasBlock()) {
			if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) {
				final Sign s = (Sign) event.getClickedBlock().getState();
				if (s.getLine(0).toLowerCase().contains("killconfirm")) {
					if (s.getLine(1).equalsIgnoreCase("§2[join]")) {
						if (isValidArena(s.getLine(2))) {
							joinLobby(event.getPlayer(), s.getLine(2));
						} else {
							event.getPlayer().sendMessage(arena_invalid);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (event.getLine(0).toLowerCase().equalsIgnoreCase("killconfirm")) {
			if (event.getPlayer().hasPermission("cm.sign") || event.getPlayer().hasPermission("killconfirmed.sign") || event.getPlayer().isOp()) {
				event.setLine(0, "§6§lkillconfirmed");
				if (!event.getLine(2).equalsIgnoreCase("")) {
					String arena = event.getLine(2);
					if (isValidArena(arena)) {
						getConfig().set(arena + ".sign.world", p.getWorld().getName());
						getConfig().set(arena + ".sign.loc.x", event.getBlock().getLocation().getBlockX());
						getConfig().set(arena + ".sign.loc.y", event.getBlock().getLocation().getBlockY());
						getConfig().set(arena + ".sign.loc.z", event.getBlock().getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage("§2Successfully created arena sign.");
					} else {
						p.sendMessage(arena_invalid_component);
						event.getBlock().breakNaturally();
					}
					event.setLine(1, "§2[Join]");
					event.setLine(2, arena);
					event.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
				}
			}
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (arenap.containsKey(event.getPlayer())) {
			if (!event.getMessage().startsWith("/kc") && !event.getMessage().startsWith("/killconfirmed")) {
				event.getPlayer().sendMessage("§cPlease use §6/kc leave §cto leave this minigame.");
				event.setCancelled(true);
				return;
			}
		}
	}

	public Sign getSignFromArena(String arena) {
		Location b_ = new Location(getServer().getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getInt(arena + ".sign.loc.x"), getConfig().getInt(arena + ".sign.loc.y"), getConfig().getInt(arena + ".sign.loc.z"));
		BlockState bs = b_.getBlock().getState();
		Sign s_ = null;
		if (bs instanceof Sign) {
			s_ = (Sign) bs;
		} else {
		}
		return s_;
	}

	public Location getLobby(String arena) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobby.world")), getConfig().getInt(arena + ".lobby.loc.x"), getConfig().getInt(arena + ".lobby.loc.y"), getConfig().getInt(arena + ".lobby.loc.z"));
		}
		return ret;
	}

	public Location getMainLobby() {
		Location ret;
		if (getConfig().isSet("mainlobby")) {
			ret = new Location(Bukkit.getWorld(getConfig().getString("mainlobby.world")), getConfig().getInt("mainlobby.loc.x"), getConfig().getInt("mainlobby.loc.y"), getConfig().getInt("mainlobby.loc.z"));
		} else {
			ret = null;
			getLogger().warning("A Mainlobby could not be found. This will lead to errors, please fix this with /kc setmainlobby.");
		}
		return ret;
	}

	public Location getSpawnForPlayer(String arena, String count) {
		Location ret = null;
		if (isValidArena(arena)) {
			ret = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn" + count + ".world")), getConfig().getInt(arena + ".spawn" + count + ".loc.x"), getConfig().getInt(arena + ".spawn" + count + ".loc.y") + 2, getConfig().getInt(arena + ".spawn" + count + ".loc.z"));
		}
		return ret;
	}

	public boolean isValidArena(String arena) {
		if (getConfig().isSet(arena + ".spawn1") && getConfig().isSet(arena + ".spawn2") && getConfig().isSet(arena + ".lobby")) {
			return true;
		}
		return false;
	}

	public HashMap<Player, Boolean> winner = new HashMap<Player, Boolean>();

	public void leaveArena(final Player p, boolean flag, boolean hmmthisbug) {
		try {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (p.isOnline()) {
						p.teleport(getMainLobby());
						// p.setFlying(false);
					}
				}
			}, 5);

			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (p.isOnline()) {
						p.setAllowFlight(false);
						p.setFlying(false);
					}
				}
			}, 10);

			/*
			 * if (p.isOnline()) { p.setAllowFlight(false); p.setFlying(false);
			 * }
			 */

			final String arena = arenap.get(p);

			removeScoreboard(arena, p);

			if (flag) {
				if (arenap.containsKey(p)) {
					arenap.remove(p);
				}
			}
			if (arenap_.containsKey(p.getName())) {
				arenap_.remove(p.getName());
			}

			//updateScoreboard(arena);

			removeScoreboard(arena, p);

			if (p.isOnline()) {
				p.getInventory().setContents(pinv.get(p));
				p.updateInventory();
			}

			if (winner.containsKey(p)) {
				if (economy) {
					EconomyResponse r = econ.depositPlayer(p.getName(), getConfig().getDouble("config.money_reward_per_game"));
					if (!r.transactionSuccess()) {
						getServer().getPlayer(p.getName()).sendMessage(String.format("An error occured: %s", r.errorMessage));
					}
				} else {
					p.getInventory().addItem(new ItemStack(Material.getMaterial(itemid), itemamount));
					p.updateInventory();
				}

				// command reward
				if (command_reward) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("[user]", p.getName()));
				}
			}

			int count = 0;
			for (Player p_ : arenap.keySet()) {
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					count++;
				}
			}

			if (hmmthisbug && count > 0) {
				getLogger().info("Sorry, I could not fix the game. Stopping now.");
				stop(arena);
			}

			if (count < 2) {
				if (flag) {
					stop(arena);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void joinLobby(final Player p, final String arena) {
		// check first if max players are reached.
		int count_ = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count_++;
			}
		}
		if (count_ > getArenaMaxPlayers(arena) - 1) {
			p.sendMessage(arena_full);
			return;
		}

		// continue
		arenap.put(p, arena);
		pinv.put(p, p.getInventory().getContents());
		p.setGameMode(GameMode.SURVIVAL);
		p.getInventory().clear();
		p.updateInventory();
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				p.teleport(getLobby(arena));
				p.setFoodLevel(20);
			}
		}, 4);

		int count = 0;
		for (Player p_ : arenap.keySet()) {
			if (arenap.get(p_).equalsIgnoreCase(arena)) {
				count++;
			}
		}
		if (count > getArenaMinPlayers(arena) - 1) {
			setAllTeams(arena);
			for (Player p_ : arenap.keySet()) {
				final Player p__ = p_;
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					Bukkit.getScheduler().runTaskLater(this, new Runnable() {
						public void run() {
							p__.teleport(getSpawnForPlayer(arena, pteam.get(p__.getName())));
						}
					}, 7);
				}
			}
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (!ingame.containsKey(arena)) {
						ingame.put(arena, false);
					}
					if (!ingame.get(arena)) {
						start(arena);
					}
				}
			}, 10);
		}

		if (!ingame.containsKey(arena)) {
			ingame.put(arena, false);
		}
		if (ingame.get(arena)) {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					p.teleport(getSpawnForPlayer(arena, pteam.get(p.getName())));
				}
			}, 7);
		}

		//updateScoreboard(arena);

		try {
			Sign s = this.getSignFromArena(arena);
			if (s != null) {
				s.setLine(3, Integer.toString(count) + "/" + Integer.toString(getArenaMaxPlayers(arena)));
				s.update();
			}
		} catch (Exception e) {
			getLogger().warning("You forgot to set a sign for arena " + arena + "! This may lead to errors.");
		}

	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity().getKiller() != null) {
			if (event.getEntity().getKiller() instanceof Player && event.getEntity() instanceof Player && arenap.containsKey(event.getEntity()) && arenap.containsKey(event.getEntity().getKiller())) {
				event.getEntity().setHealth(20);
				String killerName = event.getEntity().getKiller().getName();
				String entityKilled = event.getEntity().getName();
				getLogger().info(killerName + " killed " + entityKilled);
				final Player p1 = event.getEntity().getKiller(); // killer
				final Player p2 = event.getEntity(); // killed
				final String arena = arenap.get(event.getEntity().getKiller());

				final Location l = p1.getLocation();

				Bukkit.getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						p2.teleport(getSpawnForPlayer(arena, pteam.get(p2.getName())));
						p2.playSound(p2.getLocation(), Sound.CAT_MEOW, 1F, 1);
						setTeam(p2, pteam.get(p2.getName()));
						Byte team = 1;
						if(pteam.get(p1.getName()) == "1"){
							team = 11;
						}else{
							team = 14;
						}
						p1.getWorld().dropItemNaturally(l, new ItemStack(Material.WOOL, 1, team));
					}
				}, 10L);

				/*
				 * for (PotionEffect effect : p2.getActivePotionEffects())
				 * p2.removePotionEffect(effect.getType());
				 * 
				 * for (PotionEffect effect : p1.getActivePotionEffects())
				 * p1.removePotionEffect(effect.getType());
				 */

				p1.playEffect(p1.getLocation(), Effect.POTION_BREAK, 5);

				p1.setFoodLevel(20);
				p1.setHealth(20);
				p2.setHealth(20);
				p2.setFoodLevel(20);
			}
		}
	}

	
	public HashMap<String, Integer> redkills = new HashMap<String, Integer>();
	public HashMap<String, Integer> bluekills = new HashMap<String, Integer>();

	@EventHandler
	public void onPlayerPickup(PlayerPickupItemEvent event){
		if(arenap.containsKey(event.getPlayer())){
			if(event.getItem().getItemStack().getType() == Material.WOOL){
				MaterialData b = event.getItem().getItemStack().getData();
				String arena = arenap.get(event.getPlayer());
				if(!redkills.containsKey(arena)){
					redkills.put(arena, 0);
				}
				if(!bluekills.containsKey(arena)){
					bluekills.put(arena, 0);
				}
				if( b.getData() == (byte) 11 ){
					if(pteam.get(event.getPlayer().getName()) == "1"){
						// kill confirmed for red (picked up blue)
						redkills.put(arena, redkills.get(arena) + 1);
						getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + event.getPlayer().getName() + " confirmed a kill!");
					} else {
						// kill denied from blue (picked up blue)
						getServer().broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + event.getPlayer().getName() + " denied a kill!");
					}
				}else if( b.getData() == (byte) 14 ){
					if(pteam.get(event.getPlayer().getName()) == "2"){
						// kill confirmed for blue (picked up red)
						bluekills.put(arena, bluekills.get(arena) + 1);
						getServer().broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + event.getPlayer().getName() + " confirmed a kill!");
					}else{
						// kill denied from red (picked up red)
						getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + event.getPlayer().getName() + " denied a kill!");
					}
				}else{
					event.setCancelled(true);
				}
				
				// TODO item rewards might not work
				// TODO add customization: 20
				if (redkills.get(arena) > 20) {
					for(Player p : arenap.keySet()){
						if(arenap.get(p).equalsIgnoreCase(arena)){
							if(pteam.get(p.getName()) == "1"){
								if (economy) {
									EconomyResponse r = econ.depositPlayer(p.getName(), getConfig().getDouble("config.money_reward_per_game"));
									if (!r.transactionSuccess()) {
										getServer().getPlayer(p.getName()).sendMessage(String.format("An error occured: %s", r.errorMessage));
									}
								} else {
									p.getInventory().addItem(new ItemStack(Material.getMaterial(itemid), itemamount));
									p.updateInventory();
								}

								// command reward
								if (command_reward) {
									Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("<player>", p.getName()));
								}
							}
						}
					}
					stop(arena);
					getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "The red team won!");
				}
				if (bluekills.get(arena) > 20) {
					for(Player p : arenap.keySet()){
						if(arenap.get(p).equalsIgnoreCase(arena)){
							if(pteam.get(p.getName()) == "2"){
								if (economy) {
									EconomyResponse r = econ.depositPlayer(p.getName(), getConfig().getDouble("config.money_reward_per_game"));
									if (!r.transactionSuccess()) {
										getServer().getPlayer(p.getName()).sendMessage(String.format("An error occured: %s", r.errorMessage));
									}
								} else {
									p.getInventory().addItem(new ItemStack(Material.getMaterial(itemid), itemamount));
									p.updateInventory();
								}

								// command reward
								if (command_reward) {
									Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("<player>", p.getName()));
								}
							}
						}
					}
					stop(arena);
					getServer().broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "The blue team won!");
				}
			}else{
				event.setCancelled(true);
			}
		}
	}
	
	
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event)
	{
    	if(event.getEntity() instanceof Player && event.getDamager() instanceof Player){
	    	if (arenap.containsKey((Player)event.getEntity())){
		    	Player p1 = (Player)event.getEntity();
		    	Player p2 = (Player)event.getDamager();
		    	if(pteam.get(p1.getName()) == pteam.get(p2.getName())){
		    		event.setCancelled(true);
		    	}
		    }	
    	}
	}
	
	final Main m = this;

	public void start(final String arena) {
		ingame.put(arena, true);

		// start countdown timer
		if (start_announcement) {
			Bukkit.getServer().broadcastMessage(starting + " " + Integer.toString(start_countdown));
		}

		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				// clear hostile mobs on start:
				for (Player p : arenap.keySet()) {
					p.playSound(p.getLocation(), Sound.CAT_MEOW, 1, 0);
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						for (Entity t : p.getNearbyEntities(64, 64, 64)) {
							if (t.getType() == EntityType.ZOMBIE || t.getType() == EntityType.SKELETON || t.getType() == EntityType.CREEPER || t.getType() == EntityType.CAVE_SPIDER || t.getType() == EntityType.SPIDER || t.getType() == EntityType.WITCH || t.getType() == EntityType.GIANT) {
								t.remove();
							}
						}
						break;
					}
				}
			}
		}, 20L);

		Sign s = getSignFromArena(arena);
		if (s != null) {
			s.setLine(1, "§4[Ingame]");
			s.update();
		}

		
		if (start_announcement) {
			Bukkit.getServer().broadcastMessage(started);
		}

	}

	public void stop(final String arena) {
		ingame.put(arena, false);

		redkills.clear();
		bluekills.clear();
		
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				ArrayList<Player> torem = new ArrayList<Player>();
				for (Player p : arenap.keySet()) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						removeScoreboard(arena, p);
						leaveArena(p, false, false);
						torem.add(p);
					}
				}

				for (Player p : torem) {
					arenap.remove(p);
				}
				torem.clear();

				winner.clear();

				Sign s = getSignFromArena(arena);
				if (s != null) {
					s.setLine(1, "§2[Join]");
					s.setLine(3, "0/" + Integer.toString(getArenaMaxPlayers(arena)));
					s.update();
				}

				clean();
			}

		}, 20); // 1 second

	}

	public void clean() {
		for (Player p : arenap.keySet()) {
			if (!p.isOnline()) {
				leaveArena(p, false, false);
			}
		}
	}

	/*public void updateScoreboard(String arena) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();

			int count = 0;
			for (Player p_ : arenap.keySet()) {
				if (arenap.get(p_).equalsIgnoreCase(arena)) {
					count++;
				}
			}

			int lostcount = 0;
			for (Player p : arenap.keySet()) {
				if (arenap.get(p).equalsIgnoreCase(arena)) {
					if (lost.containsKey(p)) {
						lostcount++;
					}
				}
			}

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (arenap.containsKey(p)) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						Scoreboard board = manager.getNewScoreboard();

						Objective objective = board.registerNewObjective("test", "dummy");
						objective.setDisplaySlot(DisplaySlot.SIDEBAR);

						objective.setDisplayName("§cC§3o§dl§5o§6r§1M§aa§2t§4c§eh!"); // <-
																						// killconfirmed

						objective.getScore(Bukkit.getOfflinePlayer(" ")).setScore(5);
						objective.getScore(Bukkit.getOfflinePlayer("§aArena")).setScore(4);
						objective.getScore(Bukkit.getOfflinePlayer("§d" + arena)).setScore(3);
						objective.getScore(Bukkit.getOfflinePlayer("  ")).setScore(2);
						objective.getScore(Bukkit.getOfflinePlayer("§aPlayers Left")).setScore(1);
						objective.getScore(Bukkit.getOfflinePlayer(Integer.toString(count - lostcount) + "/" + Integer.toString(count))).setScore(0);

						p.setScoreboard(board);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public void removeScoreboard(String arena) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard sc = manager.getNewScoreboard();

			sc.clearSlot(DisplaySlot.SIDEBAR);

			getLogger().info("Removing scoreboard.");

			for (Player p : Bukkit.getOnlinePlayers()) {
				p.setScoreboard(sc);
				if (arenap.containsKey(p)) {
					if (arenap.get(p).equalsIgnoreCase(arena)) {
						getLogger().info(p.getName());
						p.setScoreboard(sc);
						p.setScoreboard(null);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeScoreboard(String arena, Player p) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard sc = manager.getNewScoreboard();

			sc.clearSlot(DisplaySlot.SIDEBAR);
			p.setScoreboard(sc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getArenaDifficulty(String arena) {
		if (!getConfig().isSet(arena + ".difficulty")) {
			setArenaDifficulty(arena, 1);
		}
		return getConfig().getInt(arena + ".difficulty");
	}

	public void setArenaDifficulty(String arena, int difficulty) {
		getConfig().set(arena + ".difficulty", difficulty);
		this.saveConfig();
	}

	public int getArenaMaxPlayers(String arena) {
		if (!getConfig().isSet(arena + ".max_players")) {
			setArenaMaxPlayers(arena, default_max_players);
		}
		return getConfig().getInt(arena + ".max_players");
	}

	public void setArenaMaxPlayers(String arena, int players) {
		getConfig().set(arena + ".max_players", players);
		this.saveConfig();
	}

	public int getArenaMinPlayers(String arena) {
		if (!getConfig().isSet(arena + ".min_players")) {
			setArenaMinPlayers(arena, default_min_players);
		}
		return getConfig().getInt(arena + ".min_players");
	}

	public void setArenaMinPlayers(String arena, int players) {
		getConfig().set(arena + ".min_players", players);
		this.saveConfig();
	}

	public boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}
	
	
	public void setTeam(Player p, String team){
		pteam.put(p.getName(), team);
		
		ItemStack lhelmet = new ItemStack(Material.LEATHER_HELMET, 1);
	    LeatherArmorMeta lam = (LeatherArmorMeta)lhelmet.getItemMeta();
	    
	    ItemStack lboots = new ItemStack(Material.LEATHER_BOOTS, 1);
	    LeatherArmorMeta lam1 = (LeatherArmorMeta)lboots.getItemMeta();
	    
	    ItemStack lchestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
	    LeatherArmorMeta lam2 = (LeatherArmorMeta)lchestplate.getItemMeta();
	    
	    ItemStack lleggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
	    LeatherArmorMeta lam3 = (LeatherArmorMeta)lleggings.getItemMeta();

	    Color c;
	    if(team.equalsIgnoreCase("1")){
	    	c = Color.RED;
	    }else{
	    	c = Color.BLUE;
	    }
	    lam3.setColor(c);
	    lam2.setColor(c);
	    lam1.setColor(c);
	    lam.setColor(c);
	   
	    lhelmet.setItemMeta(lam);
	    lboots.setItemMeta(lam1);
	    lchestplate.setItemMeta(lam2);
	    lleggings.setItemMeta(lam3);
		
	    p.getInventory().setBoots(lboots);
	    p.getInventory().setHelmet(lhelmet);
	    p.getInventory().setChestplate(lchestplate);
	    p.getInventory().setLeggings(lleggings);
	    
		p.sendMessage("§aYou are in Team §6" + team + " §anow!");
	}

	public void setAllTeams(String arena){
		String lastteam = "1";
		for(Player p : arenap.keySet()){
			if(arenap.get(p).equalsIgnoreCase(arena)){
				if(lastteam == "1"){
					setTeam(p, "2");
					lastteam = "2";
				}else{
					setTeam(p, "1");
					lastteam = "1";
				}
			}
		}
	}
	
}
