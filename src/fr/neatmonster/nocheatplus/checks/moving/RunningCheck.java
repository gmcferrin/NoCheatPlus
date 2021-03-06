package fr.neatmonster.nocheatplus.checks.moving;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

/**
 * The counterpart to the FlyingCheck. People that are not allowed to fly
 * get checked by this. It will try to identify when they are jumping, check if
 * they aren't jumping too high or far, check if they aren't moving too fast on
 * normal ground, while sprinting, sneaking or swimming.
 * 
 */
public class RunningCheck extends MovingCheck {

    public class RunningCheckEvent extends MovingEvent {

        public RunningCheckEvent(final RunningCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    private final static double maxBonus     = 1D;

    // How many move events can a player have in air before he is expected to
    // lose altitude (or eventually land somewhere)
    private final static int    jumpingLimit = 6;

    private final NoFallCheck   noFallCheck;

    public RunningCheck() {
        super("running");

        noFallCheck = new NoFallCheck();
    }

    public PreciseLocation check(final NCPPlayer player, final Object... args) {
        final MovingConfig cc = getConfig(player);
        final MovingData data = getData(player);

        // Some shortcuts:
        final PreciseLocation setBack = data.runflySetBackPoint;
        final PreciseLocation to = data.to;
        final PreciseLocation from = data.from;

        // Calculate some distances
        final double xDistance = data.to.x - from.x;
        final double zDistance = to.z - from.z;
        final double horizontalDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);

        if (!setBack.isSet())
            setBack.set(from);

        // To know if a player "is on ground" is useful
        final int fromType = CheckUtils.evaluateLocation(player.getWorld(), from);
        final int toType = CheckUtils.evaluateLocation(player.getWorld(), to);

        final boolean fromOnGround = CheckUtils.isOnGround(fromType);
        final boolean fromInGround = CheckUtils.isInGround(fromType);
        final boolean toOnGround = CheckUtils.isOnGround(toType);
        final boolean toInGround = CheckUtils.isInGround(toType);

        PreciseLocation newToLocation = null;

        final double resultHoriz = Math.max(
                0.0D,
                checkHorizontal(player, data, CheckUtils.isLiquid(fromType) && CheckUtils.isLiquid(toType),
                        horizontalDistance, cc));
        final double resultVert = Math.max(
                0.0D,
                checkVertical(player, data, fromOnGround, toOnGround,
                        CheckUtils.isLiquid(fromType) && CheckUtils.isLiquid(toType), cc));

        final double result = (resultHoriz + resultVert) * 100;

        data.jumpPhase++;

        // Slowly reduce the level with each event
        data.runflyVL *= 0.95;

        // Did the player move in unexpected ways?
        if (result > 0) {
            // Increment violation counter
            data.runflyVL += result;

            incrementStatistics(player, data.statisticCategory, result);

            final boolean cancel = executeActions(player, cc.actions, data.runflyVL);

            // Was one of the actions a cancel? Then do it
            if (cancel)
                newToLocation = setBack;
            else if (toOnGround || toInGround) {
                // In case it only gets logged, not stopped by NoCheatPlus
                // Update the setback location at least a bit
                setBack.set(to);
                data.jumpPhase = 0;

            }
        } else if (toInGround && from.y >= to.y || CheckUtils.isLiquid(toType)) {
            // Yes, if the player moved down "into" the ground or into liquid
            setBack.set(to);
            setBack.y = Math.ceil(setBack.y);
            data.jumpPhase = 0;
        } else if (toOnGround && (from.y >= to.y || setBack.y <= Math.floor(to.y))) {
            // Yes, if the player moved down "onto" the ground and the new
            // setback point is higher up than the old or at least at the
            // same height
            setBack.set(to);
            setBack.y = Math.floor(setBack.y);
            data.jumpPhase = 0;
        } else if (fromOnGround || fromInGround || toOnGround || toInGround)
            // The player at least touched the ground somehow
            data.jumpPhase = 0;

        /********* EXECUTE THE NOFALL CHECK ********************/
        final boolean checkNoFall = cc.nofallCheck && !player.hasPermission(Permissions.MOVING_NOFALL);

        if (checkNoFall && newToLocation == null) {
            data.fromOnOrInGround = fromOnGround || fromInGround;
            data.toOnOrInGround = toOnGround || toInGround;
            noFallCheck.check(player, data, cc);
        }

        return newToLocation;
    }

    /**
     * Calculate how much the player failed this check
     * 
     */
    private double checkHorizontal(final NCPPlayer player, final MovingData data, final boolean isSwimming,
            final double totalDistance, final MovingConfig cc) {

        // How much further did the player move than expected??
        double distanceAboveLimit = 0.0D;

        // A player is considered sprinting if the flag is set and if he has
        // enough food level (configurable)
        final boolean sprinting = player.getBukkitPlayer().isSprinting() && player.getBukkitPlayer().getFoodLevel() > 5;

        double limit = 0.0D;

        Id statisticsCategory = null;

        // Player on ice? Give him higher max speed
        final Block b = player.getLocation().getBlock();
        if (b.getType() == Material.ICE || b.getRelative(0, -1, 0).getType() == Material.ICE)
            data.onIce = 20;
        else if (data.onIce > 0)
            data.onIce--;

        if (cc.blockingCheck && player.getBukkitPlayer().isBlocking()
                && !player.hasPermission(Permissions.MOVING_BLOCKING)) {
            limit = cc.blockingSpeedLimit;
            statisticsCategory = Id.MOV_BLOCKING;
            if (cc.sneakingCheck && player.getBukkitPlayer().isSneaking()
                    && !player.hasPermission(Permissions.MOVING_SNEAKING))
                limit = Math.min(cc.sneakingSpeedLimit, cc.blockingSpeedLimit);
        } else if (cc.sneakingCheck && player.getBukkitPlayer().isSneaking()
                && !player.hasPermission(Permissions.MOVING_SNEAKING)) {
            limit = cc.sneakingSpeedLimit;
            statisticsCategory = Id.MOV_SNEAKING;
        } else if (isSwimming && !player.hasPermission(Permissions.MOVING_SWIMMING)) {
            limit = cc.swimmingSpeedLimit;
            statisticsCategory = Id.MOV_SWIMMING;
        } else if (!sprinting) {
            limit = cc.walkingSpeedLimit;
            statisticsCategory = Id.MOV_RUNNING;
        } else {
            limit = cc.sprintingSpeedLimit;
            statisticsCategory = Id.MOV_RUNNING;
        }

        if (data.onIce > 0)
            limit *= 2.5D;

        // If the player is in web, we need a fixed limit
        final World world = player.getWorld();
        if (CheckUtils.isWeb(CheckUtils.evaluateLocation(world, data.from))
                && CheckUtils.isWeb(CheckUtils.evaluateLocation(world, data.to))
                && !player.hasPermission(Permissions.MOVING_COBWEB)) {
            limit = cc.cobWebHoriSpeedLimit;
            statisticsCategory = Id.MOV_COBWEB;
        }

        // Taken directly from Minecraft code, should work
        limit *= player.getSpeedAmplifier();

        distanceAboveLimit = totalDistance - limit - data.horizFreedom;

        data.bunnyhopdelay--;

        // Did he go too far?
        if (distanceAboveLimit > 0 && sprinting)
            // Try to treat it as a the "bunnyhop" problem
            if (data.bunnyhopdelay <= 0 && distanceAboveLimit > 0.05D && distanceAboveLimit < 0.4D) {
                data.bunnyhopdelay = 9;
                distanceAboveLimit = 0;
            }

        if (distanceAboveLimit > 0) {
            // Try to consume the "buffer"
            distanceAboveLimit -= data.horizontalBuffer;
            data.horizontalBuffer = 0;

            // Put back the "overconsumed" buffer
            if (distanceAboveLimit < 0)
                data.horizontalBuffer = -distanceAboveLimit;
        } else
            data.horizontalBuffer = Math.min(maxBonus, data.horizontalBuffer - distanceAboveLimit);

        if (distanceAboveLimit > 0)
            data.statisticCategory = statisticsCategory;

        return distanceAboveLimit;
    }

    /**
     * Calculate if and how much the player "failed" this check.
     * 
     */
    private double checkVertical(final NCPPlayer player, final MovingData data, final boolean fromOnGround,
            final boolean toOnGround, final boolean isSwimming, final MovingConfig cc) {

        // How much higher did the player move than expected??
        double distanceAboveLimit = 0.0D;

        // Potion effect "Jump"
        final double jumpAmplifier = player.getJumpAmplifier();
        if (jumpAmplifier > data.lastJumpAmplifier)
            data.lastJumpAmplifier = jumpAmplifier;

        double limit = data.vertFreedom + cc.jumpheight;

        limit *= data.lastJumpAmplifier;

        if (data.jumpPhase > jumpingLimit + data.lastJumpAmplifier)
            limit -= (data.jumpPhase - jumpingLimit) * 0.15D;

        // Handle the calculation differently if the player is in water
        if (isSwimming && data.to.y - data.from.y > 0D) {
            // We need to make sure the player isn't a special block
            // We get the bounding box of the player
            final EntityPlayer entity = ((CraftPlayer) player.getBukkitPlayer()).getHandle();
            final AxisAlignedBB aabb = entity.boundingBox.clone();
            // Grow it of the minimum value (to collide with blocks)
            aabb.grow(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
            final double xValue = (aabb.d - aabb.a) / 2D;
            final double zValue = (aabb.f - aabb.c) / 2D;
            if (!isSpecial(player.getWorld(), data.to.x - xValue, data.to.y, data.to.z - zValue)
                    && !isSpecial(player.getWorld(), data.to.x + xValue, data.to.y, data.to.z - zValue)
                    && !isSpecial(player.getWorld(), data.to.x - xValue, data.to.y, data.to.z + zValue)
                    && !isSpecial(player.getWorld(), data.to.x + xValue, data.to.y, data.to.z + zValue))
                distanceAboveLimit = data.to.y - data.from.y - cc.verticalSwimmingSpeedLimit;
        }

        // Handle the calculation differently if the player is in cobweb
        if (distanceAboveLimit <= 0D
                && new Location(player.getWorld(), data.to.x, data.to.y, data.to.z).getBlock().getType() == Material.WEB)
            distanceAboveLimit = Math.abs(data.to.y - data.from.y) - cc.cobWebVertSpeedLimit;

        if (distanceAboveLimit <= 0D)
            distanceAboveLimit = data.to.y - data.runflySetBackPoint.y - limit;

        if (distanceAboveLimit > 0D)
            data.statisticCategory = Id.MOV_FLYING;

        if (toOnGround || fromOnGround)
            data.lastJumpAmplifier = 0;

        // Bukkit.broadcastMessage("d = " + distanceAboveLimit);

        return distanceAboveLimit;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final RunningCheckEvent event = new RunningCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.CHECK)
            // Workaround for something until I find a better way to do it
            return getData(player).statisticCategory.toString();
        else if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).runflyVL));
        else
            return super.getParameter(wildcard, player);
    }

    /**
     * Checks if a block special (checks if the block is stairs/fence or not)
     * 
     * @param world
     * @param x
     * @param y
     * @param z
     * @param above
     * @return is the block special?
     */
    private boolean isSpecial(final World world, final double x, final double y, final double z) {
        Material material = new Location(world, x, y, z).getBlock().getType();
        if (material == Material.BRICK_STAIRS || material == Material.COBBLESTONE_STAIRS
                || material == Material.NETHER_BRICK_STAIRS || material == Material.SMOOTH_STAIRS
                || material == Material.STEP || material == Material.WOOD_STAIRS)
            return true;
        material = new Location(world, x, y - 1, z).getBlock().getType();
        if (material == Material.FENCE || material == Material.IRON_FENCE || material == Material.NETHER_FENCE)
            return true;
        return false;
    }
}
