package com.hoang.dmrgenetics.logic;

import com.hoang.dmrgenetics.data.GeneticData;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.Random;
import java.util.UUID;

public class GeneticsManager {
    private static final Random RANDOM = new Random();
    // NeoForge 1.20+ có thể dùng tên thay vì UUID, nhưng xài UUID cho an toàn tương thích ngược
    private static final UUID IV_HEALTH_MODIFIER_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    public static String mixGenes(String p1, String p2, int pairs) {
        StringBuilder babyGenes = new StringBuilder();
        for (int i = 0; i < pairs; i++) {
            char allele1 = p1.charAt(i * 2 + RANDOM.nextInt(2));
            char allele2 = p2.charAt(i * 2 + RANDOM.nextInt(2));
            
            if (Character.toLowerCase(allele1) > Character.toLowerCase(allele2) || 
               (Character.toLowerCase(allele1) == Character.toLowerCase(allele2) && allele1 > allele2)) {
                char temp = allele1;
                allele1 = allele2;
                allele2 = temp;
            }
            babyGenes.append(allele1).append(allele2);
        }
        return babyGenes.toString();
    }

    public static GeneticData createBabyGenetics(GeneticData dad, GeneticData mom) {
        String babySex = "Z" + (RANDOM.nextBoolean() ? "Z" : "W");
        String babyElements = mixGenes(dad.getElements(), mom.getElements(), 1);
        String babyIVs = mixGenes(dad.getIvs(), mom.getIvs(), 3);
        String babyMutations = mixGenes(dad.getMutations(), mom.getMutations(), 3);
        String babyEpistasis = mixGenes(dad.getEpistasis(), mom.getEpistasis(), 1);

        return new GeneticData(babySex, babyElements, babyIVs, babyMutations, babyEpistasis);
    }

    public static int countDominantIVs(String ivs) {
        int count = 0;
        for (char c : ivs.toCharArray()) {
            if (Character.isUpperCase(c)) count++;
        }
        return count;
    }

    public static void applyPhenotype(TameableDragonEntity dragon, GeneticData genes) {
        int dominantCount = countDominantIVs(genes.getIvs());
        double bonusHealth = dominantCount * 10.0;
        
        var healthAttr = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(IV_HEALTH_MODIFIER_UUID);
            // Fix chuẩn AttributeModifier cho bản 1.20.4
            healthAttr.addPermanentModifier(new AttributeModifier(
                    IV_HEALTH_MODIFIER_UUID, "IV Health Bonus", bonusHealth, AttributeModifier.Operation.ADDITION));
            dragon.setHealth(dragon.getMaxHealth()); 
        }

        var scaleAttr = dragon.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            float scaleMultiplier = 1.0f + (dominantCount / 12.0f);
            scaleAttr.setBaseValue(scaleMultiplier);
        }

        if (genes.getEpistasis().equals("vv")) {
            dragon.setBreed(DragonBreedsRegistry.getDragonBreed("end")); 
            dragon.setVariant("void");
            return; 
        }

        String elements = genes.getElements();
        if (elements.equals("FW") || elements.equals("WF")) {
            dragon.setBreed(DragonBreedsRegistry.getDragonBreed("steam"));
        } else if (elements.equals("FI") || elements.equals("IF")) {
            dragon.setBreed(DragonBreedsRegistry.getDragonBreed("obsidian"));
        }

        String muts = genes.getMutations();
        if (muts.contains("mm")) {
            dragon.setVariant("albino");
        } else if (muts.contains("nn")) {
            dragon.setVariant("melanistic");
        } else if (muts.contains("bb")) {
            dragon.setVariant("bioluminescent");
        }
    }
}