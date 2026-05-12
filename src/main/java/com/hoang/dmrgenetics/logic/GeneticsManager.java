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
    private static final UUID IV_HEALTH_MODIFIER_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    // Lai tạo 2 chuỗi Gen (Ví dụ: "Aa" + "Aa" -> "AA", "Aa", "aa")
    public static String mixGenes(String p1, String p2, int pairs) {
        StringBuilder babyGenes = new StringBuilder();
        for (int i = 0; i < pairs; i++) {
            char allele1 = p1.charAt(i * 2 + RANDOM.nextInt(2));
            char allele2 = p2.charAt(i * 2 + RANDOM.nextInt(2));
            
            // Sắp xếp in hoa đứng trước in thường, ưu tiên theo alphabet
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

    // Tạo kiểu gen cho con non
    public static GeneticData createBabyGenetics(GeneticData dad, GeneticData mom) {
        // ZW System: Bố luôn cho Z (từ ZZ), Mẹ cho Z hoặc W (từ ZW)
        String babySex = "Z" + (RANDOM.nextBoolean() ? "Z" : "W");
        
        String babyElements = mixGenes(dad.getElements(), mom.getElements(), 1);
        String babyIVs = mixGenes(dad.getIvs(), mom.getIvs(), 3);
        String babyMutations = mixGenes(dad.getMutations(), mom.getMutations(), 3);
        String babyEpistasis = mixGenes(dad.getEpistasis(), mom.getEpistasis(), 1);

        return new GeneticData(babySex, babyElements, babyIVs, babyMutations, babyEpistasis);
    }

    // Đếm số lượng Gen trội (chữ in hoa) trong IVs
    public static int countDominantIVs(String ivs) {
        int count = 0;
        for (char c : ivs.toCharArray()) {
            if (Character.isUpperCase(c)) count++;
        }
        return count;
    }

    // ÉP KIỂU HÌNH LÊN THỰC THỂ (Phenotype application)
    public static void applyPhenotype(TameableDragonEntity dragon, GeneticData genes) {
        // 1. Áp dụng IVs -> Tăng Max Health
        int dominantCount = countDominantIVs(genes.getIvs());
        double bonusHealth = dominantCount * 10.0;
        
        var healthAttr = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(IV_HEALTH_MODIFIER_UUID);
            healthAttr.addPermanentModifier(new AttributeModifier(
                    IV_HEALTH_MODIFIER_UUID, "IV Health Bonus", bonusHealth, AttributeModifier.Operation.ADDITION));
            dragon.setHealth(dragon.getMaxHealth()); // Hồi máu sau khi nâng max HP
        }

        // Tăng size dựa trên IVs (Cách tác động gián tiếp nếu DMR không hỗ trợ setSize)
        // Nếu NeoForge > 1.20.4, sử dụng Attributes.SCALE
        var scaleAttr = dragon.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            float scaleMultiplier = 1.0f + (dominantCount / 12.0f); // Tối đa bự hơn 50%
            scaleAttr.setBaseValue(scaleMultiplier);
        }

        // 2. Xét Đột biến Át chế (Epistasis) -> Ghi đè toàn bộ!
        if (genes.getEpistasis().equals("vv")) {
            dragon.setBreed(DragonBreedsRegistry.getDragonBreed("end")); // Rồng hư không
            dragon.setVariant("void");
            return; // Ngừng áp dụng các màu sắc khác
        }

        // 3. Xét Hệ (Elements - Codominance)
        String elements = genes.getElements();
        if (elements.equals("FW") || elements.equals("WF")) {
            dragon.setBreed(DragonBreedsRegistry.getDragonBreed("steam"));
        } else if (elements.equals("FI") || elements.equals("IF")) {
            dragon.setBreed(DragonBreedsRegistry.getDragonBreed("obsidian"));
        }
        // Có thể thêm nhiều logic codominance khác ở đây

        // 4. Xét Đột biến ngoại hình (Mutations - Lặn)
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