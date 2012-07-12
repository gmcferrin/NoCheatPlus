package fr.neatmonster.nocheatplus.checks.interact;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceCheck;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceData;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceEvent;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import org.bukkit.Bukkit;

/**
 * A check used to verify if the player isn't interacting with blocks too quickly
 * 
 */
public class FastInteractCheck extends BlockInteractCheck {

    public class FastIneractCheckEvent extends BlockInteractEvent {

        public FastIneractCheckEvent(final FastInteractCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public FastInteractCheck() {
        super("fastinteract");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final BlockInteractConfig cc = getConfig(player);
        final BlockInteractData data = getData(player);

	    if(!data.isBlockActivelyInteractedWith())
		    return false;

        boolean cancel = false;

        // Has the player interact with blocks too quickly
        if (data.lastInteractTime != 0 && System.currentTimeMillis() - data.lastInteractTime < cc.fastInteractInterval) {
            if (!NoCheatPlus.skipCheck()) {
                if (data.previousRefused) {
                    // He failed, increase vl and statistics
                    data.fastInteractVL += cc.fastInteractInterval - System.currentTimeMillis() + data.lastInteractTime;
                    incrementStatistics(player, Id.BI_FASTINERACT, cc.fastInteractInterval - System.currentTimeMillis()
                            + data.lastInteractTime);

                    // Execute whatever actions are associated with this check and the
                    // violation level and find out if we should cancel the event
                    cancel = executeActions(player, cc.fastInteractActions, data.fastInteractVL);
                }
                data.previousRefused = true;
            }
        } else {
            // Reward with lowering of the violation level
            data.fastInteractVL *= 0.90D;
            data.previousRefused = false;
        }

        data.lastInteractTime = System.currentTimeMillis();

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final FastIneractCheckEvent event = new FastIneractCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).fastInteractVL));
        else
            return super.getParameter(wildcard, player);
    }
}
