package com.hoang.dmrgenetics.events;

import com.hoang.dmrgenetics.data.GeneticData;
import com.hoang.dmrgenetics.data.ModAttachments;
import com.hoang.dmrgenetics.logic.GeneticsManager;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "dmrgenetics")
public class GeneticsEventHandler {
    private static final Random RANDOM = new Random();

    // SỰ KIỆN 1: KHI LAI TẠO (BREEDING)
    @SubscribeEvent
    public static void onDragonBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA() instanceof TameableDragonEntity dad &&
            event.getParentB() instanceof TameableDragonEntity mom &&
            event.getChild() instanceof TameableDragonEntity baby) {

            // Lấy Gen của bố mẹ
            GeneticData dadGenes = dad.getData(ModAttachments.DRAGON_GENETICS);
            GeneticData momGenes = mom.getData(ModAttachments.DRAGON_GENETICS);

            // Tính toán Gen cho con non
            GeneticData babyGenes = GeneticsManager.createBabyGenetics(dadGenes, momGenes);
            
            // Gắn Gen vào con non
            baby.setData(ModAttachments.DRAGON_GENETICS, babyGenes);

            // Áp dụng kiểu hình lên con non
            GeneticsManager.applyPhenotype(baby, babyGenes);
        }
    }

    // SỰ KIỆN 2: KHI TRỨNG NỞ / THỰC THỂ LOAD VÀO WORLD
    @SubscribeEvent
    public static void onDragonJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof TameableDragonEntity dragon) {
            
            GeneticData genes = dragon.getData(ModAttachments.DRAGON_GENETICS);

            // Nếu rồng vừa nở (wasHatched) và chưa có gen (gen tự nhiên)
            if (dragon.wasHatched() && dragon.tickCount < 20) {
                // Xác định Giới tính theo Nhiệt độ block xung quanh (Temperature-Dependent Sex)
                boolean hasLava = false;
                boolean hasIce = false;
                
                BlockPos pos = dragon.blockPosition();
                for (BlockPos b : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                    if (event.getLevel().getBlockState(b).is(Blocks.LAVA)) hasLava = true;
                    if (event.getLevel().getBlockState(b).is(Blocks.ICE) || 
                        event.getLevel().getBlockState(b).is(Blocks.SNOW)) hasIce = true;
                }

                String initialSex = "ZZ"; // Default Đực 50-50 ZW
                if (RANDOM.nextBoolean()) initialSex = "ZW";

                if (hasLava && RANDOM.nextInt(100) < 80) initialSex = "ZZ"; // 80% Đực
                if (hasIce && RANDOM.nextInt(100) < 80) initialSex = "ZW";  // 80% Cái

                genes.setSex(initialSex);
                
                // Rồng nở tự nhiên có gene base ngẫu nhiên, ví dụ:
                genes.setEpistasis(RANDOM.nextInt(100) < 5 ? "vv" : "VV"); // 5% cơ hội nở ra Rồng Hư Không
                
                dragon.setData(ModAttachments.DRAGON_GENETICS, genes);
            }

            // Luôn đảm bảo Kiểu hình (Máu, Size, Ngoại hình) được áp dụng khi load lại rồng
            GeneticsManager.applyPhenotype(dragon, genes);
        }
    }
}