package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.players.informations.Permissions;

/**
 * Configurations specific for the "BlockPlace" checks
 * Every world gets one of these assigned to it, or if a world doesn't get
 * it's own, it will use the "global" version
 * 
 */
public class BlockPlaceConfig extends CheckConfig {

    public final boolean      fastPlaceCheck;
    public final int          fastPlaceInterval;
    public final ActionList   fastPlaceActions;

    public final boolean      reachCheck;
    public final double       reachDistance;
    public final ActionList   reachActions;

    public final boolean      directionCheck;
    public final ActionList   directionActions;
    public final long         directionPenaltyTime;
    public final double       directionPrecision;

    public final boolean      projectileCheck;
    public final int          projectileInterval;
    public final ActionList   projectileActions;

	public final boolean    noswingCheck;
	public final ActionList noswingActions;

    public final List<String> fastSignExclusions = new ArrayList<String>();

    public BlockPlaceConfig(final ConfigFile data) {

        fastPlaceCheck = data.getBoolean(ConfPaths.BLOCKPLACE_FASTPLACE_CHECK);
        fastPlaceInterval = data.getInt(ConfPaths.BLOCKPLACE_FASTPLACE_INTERVAL);
        fastPlaceActions = data.getActionList(ConfPaths.BLOCKPLACE_FASTPLACE_ACTIONS, Permissions.BLOCKPLACE_FASTPLACE);

        reachCheck = data.getBoolean(ConfPaths.BLOCKPLACE_REACH_CHECK);
        reachDistance = 535D / 100D;
        reachActions = data.getActionList(ConfPaths.BLOCKPLACE_REACH_ACTIONS, Permissions.BLOCKPLACE_REACH);

        directionCheck = data.getBoolean(ConfPaths.BLOCKPLACE_DIRECTION_CHECK);
        directionPenaltyTime = data.getInt(ConfPaths.BLOCKPLACE_DIRECTION_PENALTYTIME);
        directionPrecision = data.getInt(ConfPaths.BLOCKPLACE_DIRECTION_PRECISION) / 100D;
        directionActions = data.getActionList(ConfPaths.BLOCKPLACE_DIRECTION_ACTIONS, Permissions.BLOCKPLACE_DIRECTION);

        projectileCheck = data.getBoolean(ConfPaths.BLOCKPLACE_PROJECTILE_CHECK);
        projectileInterval = data.getInt(ConfPaths.BLOCKPLACE_PROJECTILE_INTERVAL);
        projectileActions = data.getActionList(ConfPaths.BLOCKPLACE_PROJECTILE_ACTIONS,
                Permissions.BLOCKPLACE_PROJECTILE);

        for (final String exclusion : data.getStringList(ConfPaths.BLOCKPLACE_FASTSIGN_EXCLUSIONS))
            data.getStringList(ConfPaths.BLOCKPLACE_FASTSIGN_EXCLUSIONS).add(exclusion.toLowerCase());

	    noswingCheck = data.getBoolean(ConfPaths.BLOCKPLACE_NOSWING_CHECK);
	    noswingActions = data.getActionList(ConfPaths.BLOCKPLACE_NOSWING_ACTIONS, Permissions.BLOCKPLACE_NOSWING);
    }
}
