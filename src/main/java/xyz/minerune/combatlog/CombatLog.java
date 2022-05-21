package xyz.minerune.combatlog;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.plugin.PluginBase;
import me.hteppl.tools.format.Message;

import java.util.concurrent.ConcurrentHashMap;

// TODO: Check Gamemode from GamemodeManager.
public class CombatLog extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private final ConcurrentHashMap<Player, CombatHandler> players = new ConcurrentHashMap<>();

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Entity player = event.getEntity();
        Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

        if (damager.getLevel().getId() == this.getServer().getDefaultLevel().getId()) {
            return;
        }

        if (damager instanceof Player && player instanceof Player) {
            if (!(((Player) damager).hasPermission("combatlog.bypass"))) {
                players.get(damager).startCombat();
            }

            if (!(((Player) player).hasPermission("combatlog.bypass"))) {
                players.get(player).startCombat();
            }
        } else if (damager instanceof EntityProjectile && player instanceof Player) {
            Entity shootingEntity = ((EntityProjectile) damager).shootingEntity;

            if (shootingEntity instanceof Player) {
                if (!(((Player) shootingEntity).hasPermission("combatlog.bypass"))) {
                    players.get(shootingEntity).startCombat();
                }

                if (!(((Player) player).hasPermission("combatlog.bypass"))) {
                    players.get(player).startCombat();
                }
            }
        }
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        players.put(player, (new CombatHandler(this, player, false)));
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        CombatHandler combatHandler = players.get(player);

        if (combatHandler != null) {
            if (combatHandler.isInCombat()) {
                if (player.hasPermission("combatlog.bypass")) {
                    return;
                }

                player.kill();
                for (Player onlinePlayer : this.getServer().getOnlinePlayers().values()) {
                    if (player.getLevel().getId() != onlinePlayer.getLevel().getId()) {
                        continue;
                    }

                    onlinePlayer.sendMessage(Message.red("Игрок %0 покинул игру во время боя и был наказан.", player.getName()));
                }
            }
        }

        if (combatHandler != null) {
            players.remove(player);
        }
    }

    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        CombatHandler combatHandler = players.get(player);
        if (combatHandler != null) {
            players.remove(player);
        }
    }

    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        CombatHandler combatHandler = players.get(player);
        if (combatHandler != null) {
            players.remove(player);
        }
    }

    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();
        if (message.startsWith("/") || message.startsWith("./")) {
            CombatHandler combatHandler = players.get(player);
            if (combatHandler != null) {
                if (combatHandler.isInCombat()) {
                    if (player.hasPermission("combatlog.bypass")) {
                        return;
                    }

                    event.setCancelled();
                    player.sendMessage(Message.red("Вы не можете использовать команды во время боя."));
                }
            }
        }
    }
}
