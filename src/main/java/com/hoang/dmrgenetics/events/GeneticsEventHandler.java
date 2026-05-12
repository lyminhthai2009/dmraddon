package com.hoang.dmrgenetics.events;

import com.hoang.dmrgenetics.data.GeneticData;
import com.hoang.dmrgenetics.data.ModAttachments;
import com.hoang.dmrgenetics.logic.GeneticsManager;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Random;

@EventBusSubscriber(modid = "dmrgenetics")
public class GeneticsEventHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onDragonBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA() instanceof TameableDragonEntity dad &&
            event.getParentB() instanceof TameableDragonEntity mom &&
            event.getChild() instanceof TameableDragonEntity baby) {

            GeneticData dadGenes = dad.getData(ModAttachments.DRAGON_GENETICS);
            GeneticData momGenes = mom.getData(ModAttachments.DRAGON_GENETICS);

            // Pass Entity vào để lấy UUID
            GeneticData babyGenes = GeneticsManager.createBabyGenetics(dadGenes, momGenes, dad, mom);
            babyGenes.setInitialized(true); // Trẻ sinh ra qua lai tạo đã được thiết lập gen chuẩn
            
            baby.setData(ModAttachments.DRAGON_GENETICS, babyGenes);
            GeneticsManager.applyPhenotype(baby, babyGenes);
        }
    }

    @SubscribeEvent
    public static void onDragonJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof TameableDragonEntity dragon) {
            // Chỉ chạy trên Server để tránh bất đồng bộ logic (Client tự động nhận Attribute từ Server)
            if (event.getLevel().isClientSide()) return; 

            GeneticData genes = dragon.getData(ModAttachments.DRAGON_GENETICS);

            // FIX 3: Chỉ khởi tạo gen 1 lần duy nhất trong đời con rồng
            if (dragon.wasHatched() && !genes.isInitialized()) {
                boolean hasLava = false, hasIce = false;
                BlockPos pos = dragon.blockPosition();
                
                for (BlockPos b : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                    if (event.getLevel().getBlockState(b).is(Blocks.LAVA)) hasLava = true;
                    if (event.getLevel().getBlockState(b).is(Blocks.ICE) || event.getLevel().getBlockState(b).is(Blocks.SNOW)) hasIce = true;
                }

                String initialSex = RANDOM.nextBoolean() ? "ZZ" : "ZW";
                if (hasLava && RANDOM.nextInt(100) < 80) initialSex = "ZZ"; 
                if (hasIce && RANDOM.nextInt(100) < 80) initialSex = "ZW";  

                genes.setSex(initialSex);
                genes.setEpistasis(RANDOM.nextInt(100) < 5 ? "vv" : "VV"); 
                genes.setInitialized(true); // Đánh dấu vĩnh viễn đã xong khởi tạo
                
                dragon.setData(ModAttachments.DRAGON_GENETICS, genes);
            }
            
            // Luôn áp dụng Kiểu hình khi join thế giới (đảm bảo cập nhật màu, size)
            GeneticsManager.applyPhenotype(dragon, genes);
        }
    }

    // NÂNG CẤP: Thích nghi môi trường theo gen
    @SubscribeEvent
    public static void onDragonTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof TameableDragonEntity dragon && !dragon.level().isClientSide()) {
            // Chạy kiểm tra mỗi 2 giây (40 ticks) để tránh lag Server
            if (dragon.tickCount % 40 == 0) {
                GeneticData genes = dragon.getData(ModAttachments.DRAGON_GENETICS);
                
                // Rồng Băng (Ice gen lặn) ở môi trường nóng
                if (genes.getElements().contains("I") && dragon.level().dimension() == net.minecraft.world.level.Level.NETHER) {
                    dragon.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
                }
                
                // Rồng khỏe (Behavior Năng động) tự hồi máu nếu đứng yên
                if (genes.getBehavior().equals("Tt") && dragon.getDeltaMovement().lengthSqr() < 0.01) {
                    if (dragon.getHealth() < dragon.getMaxHealth()) {
                        dragon.heal(1.0f);
                    }
                }
            }
        }
    }
}