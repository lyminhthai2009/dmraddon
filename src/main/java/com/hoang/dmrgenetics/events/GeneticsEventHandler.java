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

@Mod.EventBusSubscriber(modid = "dmrgenetics", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GeneticsEventHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onDragonBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA() instanceof TameableDragonEntity dad &&
            event.getParentB() instanceof TameableDragonEntity mom &&
            event.getChild() instanceof TameableDragonEntity baby) {

            GeneticData dadGenes = dad.getData(ModAttachments.DRAGON_GENETICS);
            GeneticData momGenes = mom.getData(ModAttachments.DRAGON_GENETICS);

            GeneticData babyGenes = GeneticsManager.createBabyGenetics(dadGenes, momGenes);
            
            baby.setData(ModAttachments.DRAGON_GENETICS, babyGenes);
            GeneticsManager.applyPhenotype(baby, babyGenes);
        }
    }

    @SubscribeEvent
    public static void onDragonJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof TameableDragonEntity dragon) {
            
            GeneticData genes = dragon.getData(ModAttachments.DRAGON_GENETICS);

            if (dragon.wasHatched() && dragon.tickCount < 20) {
                boolean hasLava = false;
                boolean hasIce = false;
                
                BlockPos pos = dragon.blockPosition();
                for (BlockPos b : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                    if (event.getLevel().getBlockState(b).is(Blocks.LAVA)) hasLava = true;
                    if (event.getLevel().getBlockState(b).is(Blocks.ICE) || 
                        event.getLevel().getBlockState(b).is(Blocks.SNOW)) hasIce = true;
                }

                String initialSex = "ZZ"; 
                if (RANDOM.nextBoolean()) initialSex = "ZW";

                if (hasLava && RANDOM.nextInt(100) < 80) initialSex = "ZZ"; 
                if (hasIce && RANDOM.nextInt(100) < 80) initialSex = "ZW";  

                genes.setSex(initialSex);
                genes.setEpistasis(RANDOM.nextInt(100) < 5 ? "vv" : "VV"); 
                
                dragon.setData(ModAttachments.DRAGON_GENETICS, genes);
            }
            GeneticsManager.applyPhenotype(dragon, genes);
        }
    }
}