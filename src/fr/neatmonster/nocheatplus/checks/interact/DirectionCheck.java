package fr.neatmonster.nocheatplus.checks.interact;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * The DirectionCheck will find out if a player tried to interact with something
 * that's not in his field of view.
 * 
 */
public class DirectionCheck extends BlockInteractCheck {

    public class DirectionCheckEvent extends BlockInteractEvent {

        public DirectionCheckEvent(final DirectionCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public DirectionCheck() {
        super("direction");
    }

	@Override
	public boolean check(final NCPPlayer player, final Object... args) {
		final BlockInteractConfig cc = getConfig(player);
		final BlockInteractData data = getData(player);

		boolean cancel = false;

		final SimpleLocation blockPlaced = data.blockInteracted;
		final BlockFace blockFace = data.blockInteractedFace;

		// How far "off" is the player with his aim. We calculate from the
		// players eye location and view direction to the center of the target
		// block. If the line of sight is more too far off, "off" will be
		// bigger than 0
		double off = CheckUtils.directionCheck(player, blockPlaced.x + 0.5D, blockPlaced.y + 0.5D,
				blockPlaced.z + 0.5D, 1D, 1D, cc.directionPrecision);

		// now check if the player is looking at the block from the correct side
		double off2 = 0.0D;

		// Find out against which face the player tried to build, and if he
		// stood on the correct side of it
		final Location eyes = player.getBukkitPlayer().getEyeLocation();
		if (blockFace == BlockFace.SOUTH)
			off2 = blockPlaced.x + 0.5D - eyes.getX();
		else if (blockFace == BlockFace.NORTH)
			off2 = -(blockPlaced.x + 0.5D - eyes.getX());
		else if (blockFace == BlockFace.UP)
			off2 = blockPlaced.y + 0.5D - eyes.getY();
		else if (blockFace == BlockFace.DOWN)
			off2 = -(blockPlaced.y + 0.5D - eyes.getY());
		else if (blockFace == BlockFace.WEST)
			off2 = blockPlaced.z + 0.5D - eyes.getZ();
		else if (blockFace == BlockFace.EAST)
			off2 = -(blockPlaced.z + 0.5D - eyes.getZ());

		// If he wasn't on the correct side, add that to the "off" value
		if (off2 > 0.0D)
			off += off2;

		final long time = System.currentTimeMillis();

		if (off < 0.1D)
			// Player did nothing wrong
			// reduce violation counter to reward him
			data.directionVL *= 0.9D;
		else {
			// Player failed the check
			// Increment violation counter and statistics
			data.directionVL += off;
			incrementStatistics(player, Id.BI_DIRECTION, off);

			// Execute whatever actions are associated with this check and the
			// violation level and find out if we should cancel the event
			cancel = executeActions(player, cc.directionActions, data.directionVL);

			if (cancel)
				// if we should cancel, remember the current time too
				data.directionLastViolationTime = time;
		}

		// If the player is still in penalty time, cancel the event anyway
		if (data.directionLastViolationTime + cc.directionPenaltyTime > time) {
			// A safeguard to avoid people getting stuck in penalty time
			// indefinitely in case the system time of the server gets changed
			if (data.directionLastViolationTime > time)
				data.directionLastViolationTime = 0;

			// He is in penalty time, therefore request cancelling of the event
			return true;
		}

		return cancel;
	}

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final DirectionCheckEvent event = new DirectionCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(getData(player).directionVL);
        else
            return super.getParameter(wildcard, player);
    }
}
