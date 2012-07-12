package fr.neatmonster.nocheatplus.checks.interact;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.players.informations.Permissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Configurations specific for the "blockinteract" checks
 * Every world gets one of these assigned to it, or if a world doesn't get
 * it's own, it will use the "global" version
 * 
 */
public class BlockInteractConfig extends CheckConfig {
	public final boolean    fastInteractCheck;
	public final int        fastInteractInterval;
	public final ActionList fastInteractActions;

    public final boolean      reachCheck;
    public final double       reachDistance;
    public final ActionList   reachActions;

    public final boolean      directionCheck;
    public final ActionList   directionActions;
    public final long         directionPenaltyTime;
    public final double       directionPrecision;

	public final boolean    noswingCheck;
	public final ActionList noswingActions;

    public BlockInteractConfig(final ConfigFile data) {

	    fastInteractCheck = data.getBoolean(ConfPaths.BLOCKINTERACT_FASTINTERACT_CHECK);
	    fastInteractInterval = data.getInt(ConfPaths.BLOCKINTERACT_FASTINTERACT_INTERVAL);
	    fastInteractActions = data.getActionList(ConfPaths.BLOCKINTERACT_FASTINTERACT_ACTIONS, Permissions.BLOCKINTERACT_FASTINTERACT);

        reachCheck = data.getBoolean(ConfPaths.BLOCKINTERACT_REACH_CHECK);
        reachDistance = 535D / 100D;
        reachActions = data.getActionList(ConfPaths.BLOCKINTERACT_REACH_ACTIONS, Permissions.BLOCKINTERACT_REACH);

        directionCheck = data.getBoolean(ConfPaths.BLOCKINTERACT_DIRECTION_CHECK);
        directionPenaltyTime = data.getInt(ConfPaths.BLOCKINTERACT_DIRECTION_PENALTYTIME);
        directionPrecision = data.getInt(ConfPaths.BLOCKINTERACT_DIRECTION_PRECISION) / 100D;
        directionActions = data.getActionList(ConfPaths.BLOCKINTERACT_DIRECTION_ACTIONS, Permissions.BLOCKINTERACT_DIRECTION);

	    noswingCheck = data.getBoolean(ConfPaths.BLOCKINTERACT_NOSWING_CHECK);
	    noswingActions = data.getActionList(ConfPaths.BLOCKINTERACT_NOSWING_ACTIONS, Permissions.BLOCKINTERACT_NOSWING);
    }
}
