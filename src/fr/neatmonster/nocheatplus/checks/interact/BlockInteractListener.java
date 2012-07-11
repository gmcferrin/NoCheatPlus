package fr.neatmonster.nocheatplus.checks.interact;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 */
public class BlockInteractListener extends CheckListener {

    private final ReachCheck reachCheck;
    private final DirectionCheck  directionCheck;

    public BlockInteractListener() {
        super("blockinteract");

        reachCheck = new ReachCheck();
        directionCheck = new DirectionCheck();
    }

    /**
     * We listen to PlayerInteractEvent events for obvious reasons
     * 
     * @param event
     *            the PlayerInteractEvent event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void handleBlockInteractEvent(final PlayerInteractEvent event) {

        if (event.getClickedBlock() == null)
            return;

        final NCPPlayer player = NCPPlayer.getPlayer(event.getPlayer());
        final BlockInteractConfig cc = (BlockInteractConfig) getConfig(player);
        final BlockInteractData data = (BlockInteractData) getData(player);

        boolean cancelled = false;

        // Remember these locations and put them in a simpler "format"
        data.blockInteracted.set(event.getClickedBlock());

	    data.blockInteractedFace = event.getBlockFace();

        // Now do the actual checks

        // First the reach check
        if (cc.reachCheck && !player.hasPermission(Permissions.BLOCKPLACE_REACH))
            cancelled = reachCheck.check(player);

        // Second the direction check
        if (!cancelled && cc.directionCheck && !player.hasPermission(Permissions.BLOCKPLACE_DIRECTION))
            cancelled = directionCheck.check(player);

        // If one of the checks requested to cancel the event, do so
        if (cancelled)
            event.setCancelled(cancelled);
    }
}
