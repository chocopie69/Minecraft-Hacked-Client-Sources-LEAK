package cedo.modules.combat;

import cedo.Fan;
import cedo.Wrapper;
import cedo.events.Event;
import cedo.events.listeners.EventMotion;
import cedo.events.listeners.EventMove;
import cedo.events.listeners.EventPacket;
import cedo.modules.Module;
import cedo.modules.exploit.Antibot;
import cedo.settings.impl.BooleanSetting;
import cedo.settings.impl.ModeSetting;
import cedo.settings.impl.NumberSetting;
import cedo.util.Stopwatch;
import cedo.util.Timer;
import cedo.util.movement.MovementUtil;
import cedo.util.random.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Killaura extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Switch","Switch", "Priority");
    public ModeSetting sortingMode = new ModeSetting("Sorting Mode", "Health","Health", "Angle", "Distance");
    public BooleanSetting entities = new BooleanSetting("Players", true);
    public BooleanSetting mobs = new BooleanSetting("Mobs", false);
    public BooleanSetting hvhMode = new BooleanSetting("HVH Mode", true);

    public NumberSetting aps = new NumberSetting("APS", 11, 1, 20, 1);
    public NumberSetting range = new NumberSetting("Range", 4.45,  3, 6,  0.05);
    public NumberSetting blockRange = new NumberSetting("Block Range",  6, 3, 8, 0.05);
    public NumberSetting aimSpeed = new NumberSetting("Aim Speed", 10, 0.2, 10, 1.0);
    public NumberSetting switchDelay = new NumberSetting("Switch Delay", 250, 1, 1000, 1);

    public BooleanSetting autoblock = new BooleanSetting("Auto Block", true);
    public BooleanSetting lockView = new BooleanSetting("Lock View", false);
    public BooleanSetting teams = new BooleanSetting("Teams", true);
    public BooleanSetting walls = new BooleanSetting("Through Walls", true);
    public BooleanSetting disableOnDeath = new BooleanSetting("Disable", true);

    public boolean blocking;

    private int targetIndex;

    public EntityLivingBase target;

    private Timer attackTimer = new Timer();
    private Timer switchTimer = new Timer();

    private List<EntityLivingBase> targetList = new ArrayList<>();

    private final String[] strings = new String[]{"1st Killer - ", "1st Place - ", "You died! Want to play again? Click here!", " - Damage Dealt - ", "1st - ", "Winning Team - ", "Winners: ", "Winner: ", "Winning Team: ", " win the game!", "1st Place: ", "Last team standing!", "Winner #1 (", "Top Survivors", "Winners - "};


    public Killaura() {
        super("Killaura", Keyboard.KEY_R, Category.COMBAT);
        addSettings(mode, sortingMode, autoblock, teams, walls, disableOnDeath, blockRange, range, aps, hvhMode, lockView, aimSpeed, switchDelay, entities, mobs);
    }

    @Override
    public void onEnable() {
        targetList.clear();
        target = null;
        blocking = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        targetList.clear();
        target = null;
        blocking = false;
        super.onDisable();
    }






    public void onEvent(Event e) {
        if (e instanceof EventPacket) {
            if (e.isIncoming()) {
                if (((EventPacket) e).getPacket() instanceof S02PacketChat &&
                        disableOnDeath.isEnabled()) {
                    for (String string : strings) {
                        if (((S02PacketChat) ((EventPacket) e).getPacket()).getChatComponent().getUnformattedText().contains(string))
                            if (this.isEnabled())
                                this.toggle();
                    }
                }
            }
        }
        if (e instanceof EventMotion) {
            if(Fan.targetHud.size.is("Script")){
                this.setSuffix("\247cMichealXF");
            }else
            this.setSuffix(mode.getSelected());

            collectTargets();

            sortTargets();

            if (switchTimer.hasTimeElapsed((long) switchDelay.getValue(), true) && mode.getSelected().equals("Switch")) {
                targetIndex++;
            }

            if (targetIndex >= targetList.size())
                targetIndex = 0;

            target = !targetList.isEmpty() &&
                    targetIndex < targetList.size() ?
                    targetList.get(targetIndex) :
                    null;

            if (!isHoldingSword())
                blocking = false;

            if (target == null) {
                if (blocking)
                    unblock();
                return;
            }

            switch (e.getType()) {
                case PRE:
                    float yaw = RotationUtils.getRotations(target, (float) (aimSpeed.getValue() * 5F))[0];
                    float pitch = RotationUtils.getRotations(target, (float) (aimSpeed.getValue() * 5F))[1];
                    if (lockView.isEnabled()) {
                        mc.thePlayer.rotationYaw = yaw;
                        mc.thePlayer.rotationPitch = pitch;
                    } else {
                        ((EventMotion) e).setYaw(yaw);
                        ((EventMotion) e).setPitch(pitch);
                    }

                    if (attackTimer.hasTimeElapsed((long) (1000 / aps.getValue()), true)) {
                        if (isValid(target, false)) {

                            MovingObjectPosition ray = RotationUtils.rayCast(
                                    mc.thePlayer,
                                    target.posX,
                                    target.posY + target.getEyeHeight(),
                                    target.posZ);

                            if (ray != null) {
                                Entity entityHit = ray.entityHit;
                                if (entityHit instanceof EntityLivingBase)
                                    if (isValid((EntityLivingBase) entityHit, false))
                                        target = (EntityLivingBase) entityHit;
                            }

                            if (isHoldingSword())
                                unblock();

                            mc.thePlayer.swingItem();
                            attack(target);
                        }
                    }
                    break;
                case POST:
                    if (isHoldingSword())
                        block();
                    break;
            }
        }
    }

    private void attack(EntityLivingBase entityLivingBase) {
        final float sharpLevel = EnchantmentHelper.func_152377_a(
                mc.thePlayer.getHeldItem(),
                EnumCreatureAttribute.UNDEFINED);

        if (sharpLevel > 0.0F)
            mc.thePlayer.onEnchantmentCritical(entityLivingBase);

        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(
                new C02PacketUseEntity(entityLivingBase,
                        C02PacketUseEntity.Action.ATTACK));
    }

    private void block() {
        if (autoblock.isEnabled() &&
                !mc.gameSettings.keyBindUseItem.isPressed() &&
                !blocking) {
            mc.getNetHandler().getNetworkManager().sendPacket(
                    new C08PacketPlayerBlockPlacement(
                            new BlockPos(-1, -1, -1),
                            255,
                            mc.thePlayer.getCurrentEquippedItem(),
                            0,
                            0,
                            0));
            blocking = true;
        }
    }

    private void unblock() {
        double autoblockValue;
        if(hvhMode.isEnabled()){
            autoblockValue = RandomUtils.nextDouble(Double.MIN_VALUE, Double.MAX_VALUE);
        }else
            autoblockValue = 1;
        if (autoblock.isEnabled() && blocking) {
            mc.getNetHandler().getNetworkManager().sendPacket((
                            new C07PacketPlayerDigging(
                                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                                    new BlockPos(autoblockValue, autoblockValue, autoblockValue),
                                    EnumFacing.DOWN)));
            blocking = false;
        }
    }

    private void sortTargets() {
        switch (sortingMode.getSelected()) {
            case "Angle":
                targetList.sort(
                        Comparator.comparingDouble(
                                RotationUtils::getAngleChange));
                break;
            case "Distance":
                targetList.sort(
                        Comparator.comparingDouble(
                                RotationUtils::getDistanceToEntity));
                break;
            case "Health":
                targetList.sort(
                        Comparator.comparingDouble(
                                EntityLivingBase::getHealth));
                break;
        }
    }

    private void collectTargets() {
        targetList.clear();

        for (Entity entity : mc.thePlayer.getEntityWorld().loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                if (isValid(entityLivingBase, true))
                    targetList.add(entityLivingBase);
            }
        }
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }

    private boolean isValid(EntityLivingBase entityLivingBase, boolean blocking) {
        if (entityLivingBase == mc.thePlayer || entityLivingBase.isDead || entityLivingBase.getDistanceToEntity(mc.thePlayer) > (blocking ? blockRange.getValue() : range.getValue()) || entityLivingBase.getHealth() == 0 || entityLivingBase.isInvisible() || entityLivingBase instanceof EntityArmorStand)
            return false;

        if (!entityLivingBase.canEntityBeSeen(mc.thePlayer) && !walls.isEnabled())
            return false;

        if (entityLivingBase instanceof EntityPlayer && !entities.isEnabled())
            return false;

        if (RotationUtils.isOnSameTeam(entityLivingBase) && teams.isEnabled())
            return false;

        if ((entityLivingBase instanceof EntityMob || entityLivingBase instanceof EntityAmbientCreature || entityLivingBase instanceof EntityWaterMob) && !mobs.isEnabled())
            return false;

        if (entityLivingBase instanceof EntityAnimal)
            return false;

        if (entityLivingBase instanceof EntityGolem)
            return false;

        return !(entityLivingBase instanceof EntityVillager);
    }
    
}
