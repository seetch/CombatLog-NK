package xyz.minerune.combatlog;

import cn.nukkit.Player;
import cn.nukkit.scheduler.TaskHandler;
import lombok.Getter;
import me.hteppl.tools.string.StringTools;
import me.seetch.format.Format;

public class CombatHandler {

    private CombatLog plugin;
    private Player player;
    @Getter
    private boolean inCombat = false;

    private int combatTimeLeft;
    private int combatTimeout = 20;
    private TaskHandler combatTickTask = null;

    public Player attacker;

    public CombatHandler(CombatLog plugin, Player player, boolean inCombat) {
        this.plugin = plugin;
        this.player = player;
        this.inCombat = inCombat;
    }

    public void setAttacker(Player attacker) {
        this.attacker = attacker;
    }

    public void startCombat() {
        if (!isInCombat()) {
            if (attacker != null){
                player.sendMessage(Format.RED.message("Вы были атакованы %0. §7[Не выходите из игры.]", attacker.getName()));
            }else{
                player.sendMessage(Format.RED.message("Вы вошли в режим боя. §7[Не выходите из игры.]"));
            }
        }

        combatTimeLeft = combatTimeout;

        if (combatTickTask != null) {
            combatTickTask.cancel();
        }

        combatTickTask = plugin.getServer().getScheduler().scheduleRepeatingTask(this.plugin, () -> {
            if (combatTimeLeft == 0) {
                endCombat();
                return;
            }

            player.sendTip(Format.YELLOW.message("Режим боя закончится через %0.", StringTools.getFullPluralForm(combatTimeLeft, "секунду", "секунды", "секунд")));
            combatTimeLeft--;
        }, 20);

        inCombat = true;
    }

    public void endCombat() {
        if (combatTickTask != null) {
            combatTickTask.cancel();
        }

        combatTickTask = null;

        if (player.isConnected()) {
            player.sendTip(Format.GREEN.message("Режим боя окончился."));
        }

        inCombat = false;
        attacker = null;
    }

    public void reset() {
        endCombat();
        combatTimeLeft = 0;
    }
}
