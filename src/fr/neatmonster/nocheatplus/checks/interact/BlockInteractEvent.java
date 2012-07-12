package fr.neatmonster.nocheatplus.checks.interact;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceCheck;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

public class BlockInteractEvent extends CheckEvent {

    public BlockInteractEvent(final BlockInteractCheck check, final NCPPlayer player, final ActionList actions,
                              final double vL) {
        super(check, player, actions, vL);
    }

    @Override
    public BlockInteractCheck getCheck() {
        return (BlockInteractCheck) super.getCheck();
    }
}
