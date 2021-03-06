package mytown.protection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.MyTown;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.BlockPos;
import mytown.util.ChunkPos;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * Created by AfterWind on 9/22/2014.
 * Protection for Botania mod
 */
public class BotaniaProtection extends Protection {

    private Class<? extends Item> clsTerraPick;
    private Class<? extends Item> clsShardLaputa;
    private Class<? extends Item> clsTerraFirmaRod;
    private Class<? extends Item> clsHellsRod;

    @SuppressWarnings("unchecked")
    public BotaniaProtection() {
        isHandlingEvents = true;
        try {
            clsTerraPick = (Class<? extends Item>) Class.forName("vazkii.botania.common.item.equipment.tool.ItemTerraPick");
            clsShardLaputa = (Class<? extends Item>) Class.forName("vazkii.botania.common.item.ItemLaputaShard");
            clsTerraFirmaRod = (Class<? extends Item>) Class.forName("vazkii.botania.common.item.rod.ItemTerraformRod");
            clsHellsRod = (Class<? extends Item>) Class.forName("vazkii.botania.common.item.rod.ItemFireRod");
        } catch (Exception e) {
            MyTown.instance.log.error("Failed to load Botania classes!");
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onPlayerBreaks(PlayerInteractEvent ev) {
        try {
            if (ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                if (ev.entityPlayer.getHeldItem() != null && clsTerraPick.isAssignableFrom(ev.entityPlayer.getHeldItem().getItem().getClass())) {

                    boolean isEnabled = ev.entityPlayer.getHeldItem().getTagCompound().getBoolean("enabled");

                    // Basically checking if pick is enabled for area mode
                    if (isEnabled) {
                        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);

                        ForgeDirection direction = ForgeDirection.getOrientation(ev.face);
                        boolean doX = direction.offsetX == 0;
                        boolean doZ = direction.offsetZ == 0;
                        int level = getLevel(ev.entityPlayer.getHeldItem());
                        int range = Math.max(0, level - 1);

                        // DEV:
                        //MyTown.instance.log.info("Got range: " + range);

                        List<Town> towns = MyTownUtils.getTownsInRange(ev.world.provider.dimensionId, ev.x, ev.z, doX ? range : 0, doZ ? range : 0);

                        for (Town town : towns) {
                            boolean breakFlag = (Boolean) town.getValue(FlagType.modifyBlocks);
                            if (!breakFlag && town.checkPermission(res, FlagType.modifyBlocks)) {
                                ev.setCanceled(true);
                                res.sendMessage(FlagType.modifyBlocks.getLocalizedProtectionDenial());
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyTown.instance.log.error("Failed to call methods or some other nasty error!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkItemUsage(ItemStack itemStack, Resident res, BlockPos bp) {
        if(clsShardLaputa.isAssignableFrom(itemStack.getItem().getClass())) {
            int range = 14 + itemStack.getItemDamage();
            //MyTown.instance.log.info("Got range: " + range);
            List<ChunkPos> chunks = MyTownUtils.getChunksInBox(bp.x - range, bp.z - range, bp.x + range, bp.z + range);
            for (ChunkPos chunk : chunks) {
                Town town = MyTownUtils.getTownAtPosition(bp.dim, chunk.getX(), chunk.getZ());
                if (town != null) {
                    if (!town.checkPermission(res, FlagType.modifyBlocks)) {
                        res.protectionDenial(FlagType.modifyBlocks.getLocalizedProtectionDenial(), Formatter.formatOwnerToString(town.getMayor()));
                        return true;
                    }
                }
            }
        } else if(clsTerraFirmaRod.isAssignableFrom(itemStack.getItem().getClass())) {
            int range = 16;
            List<ChunkPos> chunks = MyTownUtils.getChunksInBox((int) res.getPlayer().posX - range, (int) res.getPlayer().posZ - range, (int) res.getPlayer().posX + range, (int) res.getPlayer().posZ + range);
            for(ChunkPos chunk : chunks) {
                Town town = MyTownUtils.getTownAtPosition(bp.dim, chunk.getX(), chunk.getZ());
                if(town != null) {
                    if(!town.checkPermission(res, FlagType.modifyBlocks)) {
                        res.protectionDenial(FlagType.modifyBlocks.getLocalizedProtectionDenial(), Formatter.formatOwnerToString(town.getMayor()));
                        return true;
                    }
                }
            }
        } else if(clsHellsRod.isAssignableFrom(itemStack.getItem().getClass())) {
            int radius = 5;
            float posX = bp.x + 0.5F, posY = bp.y + 1.0F, posZ = bp.z + 0.5F;
            AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX, posY, posZ).expand(radius, radius, radius);
            List<EntityLivingBase> entities = res.getPlayer().getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, boundingBox);
            for(EntityLivingBase entity : entities) {
                //MyTown.instance.log.info("Got entity: " + entity.toString());
                Town town = MyTownUtils.getTownAtPosition(entity.dimension, (int) entity.posX >> 4, (int) entity.posZ >> 4);
                if(town != null) {
                    if (!town.checkPermission(res, FlagType.attackEntities, entity.dimension, (int) entity.posX, (int) entity.posY, (int) entity.posZ)) {
                        //MyTown.instance.log.info("Checking entity.");
                        for (Protection prot : Protections.instance.protections.values()) {
                            if (prot.protectedEntities.contains(entity.getClass())) {
                                res.protectionDenial(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.animalCruelty"), Formatter.formatOwnersToString(town.getOwnersAtPosition(entity.dimension, (int) entity.posX, (int) entity.posY, (int) entity.posZ)));
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * @param stack
     * @return
     * @author Vazkii
     */
    public int getLevel(ItemStack stack) {
        int[] LEVELS = new int[]{
                0, 10000, 1000000, 10000000, 100000000, 1000000000
        };

        int mana = stack.getTagCompound().getInteger("mana");
        for (int i = LEVELS.length - 1; i > 0; i--)
            if (mana >= LEVELS[i])
                return i;
        return 0;
    }


}
