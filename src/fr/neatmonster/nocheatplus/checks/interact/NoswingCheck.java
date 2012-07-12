package fr.neatmonster.nocheatplus.checks.interact;

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
 * We require that the player moves his arm between blockinteracts, this is
 * what gets checked here.
 * 
 */
public class NoswingCheck extends BlockInteractCheck {

    public class NoswingCheckEvent extends BlockInteractEvent {

        public NoswingCheckEvent(final NoswingCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public NoswingCheck() {
        super("noswing");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final BlockInteractConfig cc = getConfig(player);
        final BlockInteractData data = getData(player);

	    if(!data.isBlockActivelyInteractedWith())
		    return false;

        boolean cancel = false;

        // did he swing his arm before
        if (data.armswung) {
            // "consume" the flag
            data.armswung = false;
            // reward with lowering of the violation level
            data.noswingVL *= 0.90D;
        } else {
            // he failed, increase vl and statistics
            data.noswingVL += 1;
            incrementStatistics(player, Id.BP_NOSWING, 1);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.noswingActions, data.noswingVL);
        }

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final NoswingCheckEvent event = new NoswingCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).noswingVL));
        else
            return super.getParameter(wildcard, player);
    }
}
