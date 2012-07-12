package fr.neatmonster.nocheatplus.checks.interact;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceData;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 */
public class BlockInteractListener extends CheckListener {

    private final ReachCheck reachCheck;
    private final DirectionCheck  directionCheck;
	private final NoswingCheck  noswingCheck;

    public BlockInteractListener() {
        super("blockinteract");

        reachCheck = new ReachCheck();
        directionCheck = new DirectionCheck();
	    noswingCheck = new NoswingCheck();
    }

	/**
	 * We listen to PlayerAnimationEvent because it is (currently) equivalent
	 * to "player swings arm" and we want to check if he did that between
	 * blockbreaks.
	 *
	 * @param event
	 *            The PlayerAnimation Event
	 */
	@EventHandler(
			priority = EventPriority.MONITOR)
	public void armSwing(final PlayerAnimationEvent event) {
		// Just set a flag to true when the arm was swung
		((BlockPlaceData) getData(NCPPlayer.getPlayer(event.getPlayer()))).armswung = true;
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

	    // First the noswing check
	    if (cc.noswingCheck && !player.hasPermission(Permissions.BLOCKINTERACT_NOSWING))
		    cancelled = noswingCheck.check(player);

        // Second the reach check
        if (!cancelled && cc.reachCheck && !player.hasPermission(Permissions.BLOCKINTERACT_REACH))
            cancelled = reachCheck.check(player);

        // Third the direction check
        if (!cancelled && cc.directionCheck && !player.hasPermission(Permissions.BLOCKINTERACT_DIRECTION))
            cancelled = directionCheck.check(player);

        // If one of the checks requested to cancel the event, do so
        if (cancelled)
            event.setCancelled(cancelled);
    }
}
