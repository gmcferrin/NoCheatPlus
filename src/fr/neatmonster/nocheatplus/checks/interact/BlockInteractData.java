package fr.neatmonster.nocheatplus.checks.interact;

import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;
import org.bukkit.block.BlockFace;

/**
 * Player specific data for the blockbreak checks
 * 
 */
public class BlockInteractData extends CheckData {

    // Keep track of violation levels for the two checks
    public double               reachVL                    = 0.0D;
    public double               directionVL                = 0.0D;
	public double               noswingVL                  = 0.0D;

    // Used to know if the previous event was refused
    public boolean              previousRefused            = false;

    // Used for the penalty time feature of the direction check
    public long                 directionLastViolationTime = 0;

    // Have a nicer/simpler way to work with block locations instead of
    // Bukkits own "Location" class
    public final SimpleLocation blockInteracted            = new SimpleLocation();
	public BlockFace blockInteractedFace                   = BlockFace.SELF;

	// indicate if the player swung his arm since he got checked last time
	public boolean              armswung                   = true;

	// For logging, remember the reachDistance that was calculated in the
	// reach check
	public double               reachdistance;
}
