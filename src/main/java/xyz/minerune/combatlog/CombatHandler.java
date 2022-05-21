package xyz.minerune.combatlog;

import cn.nukkit.Player;
import cn.nukkit.scheduler.TaskHandler;
import me.hteppl.tools.format.Message;
import me.hteppl.tools.string.StringTools;

public class CombatHandler {

    private CombatLog plugin;
    private Player player;
    private boolean inCombat = false;

    private int combatTimeLeft;
    private int combatTimeout = 20;
    private TaskHandler combatTickTask = null;

    public CombatHandler(CombatLog plugin, Player player, boolean inCombat) {
        this.plugin = plugin;
        this.player = player;
        this.inCombat = inCombat;
    }

    public boolean isInCombat() {
        return inCombat;
    }

    public void startCombat() {
        if (!isInCombat()) {
            player.sendTip(Message.gold("Вы вошли в режим боя."));
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

            player.sendTip(Message.yellow("Режим боя закончится через %0.", StringTools.getFullPluralForm(combatTimeLeft, "секунду", "секунды", "секунд")));
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
            player.sendTip(Message.gold("Режим боя окончился."));
        }

        inCombat = false;
    }

    public void reset() {
        endCombat();
        combatTimeLeft = 0;
    }
}
