package fr.neatmonster.nocheatplus.checks.interact;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceData;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;
import org.bukkit.block.BlockFace;

/**
 * Abstract base class for BlockInteract checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class BlockInteractCheck extends Check {

    public BlockInteractCheck(final String name) {
        super("blockinteract." + name, BlockInteractConfig.class, BlockInteractData.class);
    }

    public abstract boolean check(final NCPPlayer player, final Object... args);

    public BlockInteractConfig getConfig(final NCPPlayer player) {
        return (BlockInteractConfig) player.getConfig(this);
    }

    public BlockInteractData getData(final NCPPlayer player) {
        return (BlockInteractData) player.getData(this);
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {
        if (wildcard == ParameterName.LOCATION) {
            final SimpleLocation l = getData(player).blockInteracted;
            if (l.isSet())
                return String.valueOf(Math.round(l.x)) + " " + String.valueOf(Math.round(l.y)) + " "
                        + String.valueOf(Math.round(l.z));
            else
                return "null";
        } else if(wildcard == ParameterName.PLACE_AGAINST) {
	        final BlockFace f = getData(player).blockInteractedFace;
	        if (f != null)
		        return f.name();
	        else
		        return "null";
        }

        else
            return super.getParameter(wildcard, player);
    }
}
