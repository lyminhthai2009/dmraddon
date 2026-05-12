package com.hoang.dmrgenetics.logic;

import com.hoang.dmrgenetics.DMRGenetics;
import com.hoang.dmrgenetics.data.GeneticData;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class GeneticsManager {
    private static final Random RANDOM = new Random();
    
    // Đăng ký ID chuẩn cho NeoForge 1.21+
    private static final ResourceLocation MOD_HEALTH = ResourceLocation.fromNamespaceAndPath(DMRGenetics.MODID, "genetic_health");
    private static final ResourceLocation MOD_SCALE = ResourceLocation.fromNamespaceAndPath(DMRGenetics.MODID, "genetic_scale");
    private static final ResourceLocation MOD_DAMAGE = ResourceLocation.fromNamespaceAndPath(DMRGenetics.MODID, "genetic_damage");
    private static final ResourceLocation MOD_SPEED = ResourceLocation.fromNamespaceAndPath(DMRGenetics.MODID, "genetic_speed");

    // FIX 1: Phòng thủ chuỗi String bị ngắn gây Crash Server
    private static String padString(String str, int targetLength, char defaultChar) {
        if (str == null) str = "";
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < targetLength) sb.append(defaultChar);
        return sb.toString();
    }

    // NÂNG CẤP: Lai gen có tỷ lệ đột biến lật alen (1-5%)
    public static String mixGenes(String p1, String p2, int pairs, double mutationChance) {
        p1 = padString(p1, pairs * 2, 'A');
        p2 = padString(p2, pairs * 2, 'A');
        
        StringBuilder babyGenes = new StringBuilder();
        for (int i = 0; i < pairs; i++) {
            char allele1 = p1.charAt(i * 2 + RANDOM.nextInt(2));
            char allele2 = p2.charAt(i * 2 + RANDOM.nextInt(2));

            // Đột biến (Lật chữ hoa/thường)
            if (RANDOM.nextDouble() < mutationChance) allele1 = flipCase(allele1);
            if (RANDOM.nextDouble() < mutationChance) allele2 = flipCase(allele2);
            
            // Xếp chữ hoa lên trước (Trội đè Lặn)
            if (Character.isLowerCase(allele1) && Character.isUpperCase(allele2)) {
                char temp = allele1; allele1 = allele2; allele2 = temp;
            }
            babyGenes.append(allele1).append(allele2);
        }
        return babyGenes.toString();
    }

    private static char flipCase(char c) {
        return Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c);
    }

    public static GeneticData createBabyGenetics(GeneticData dad, GeneticData mom, TameableDragonEntity dadEntity, TameableDragonEntity momEntity) {
        GeneticData baby = new GeneticData();
        
        // 1. Lưu gia phả & Cận huyết (Inbreeding)
        baby.setParents(dadEntity.getUUID(), momEntity.getUUID());
        boolean inbred = false;
        if (dad.getSireId() != null && mom.getSireId() != null) {
            if (dad.getSireId().equals(mom.getSireId()) || dad.getDamId().equals(mom.getDamId())) {
                inbred = true;
            }
        }
        baby.setHealthStatus(inbred && RANDOM.nextInt(100) < 60 ? "xx" : "XX"); // 60% dính gen bệnh nếu cận huyết

        // 2. Di truyền liên kết giới tính (Nhiễm sắc thể Z)
        boolean isMale = RANDOM.nextBoolean();
        baby.setSex(isMale ? "ZZ" : "ZW");
        
        char dadZ = padString(dad.getColorZ(), 2, 'C').charAt(RANDOM.nextInt(2)); // Bố luôn có 2 gen Z
        char momZ = padString(mom.getColorZ(), 1, 'C').charAt(0); // Mẹ chỉ truyền 1 gen Z (W không mang màu)

        if (isMale) {
            char z1 = dadZ; char z2 = momZ;
            if (Character.isLowerCase(z1) && Character.isUpperCase(z2)) { char t = z1; z1 = z2; z2 = t; }
            baby.setColorZ("" + z1 + z2); // Con đực cần 2 alen lặn mới biến dị
        } else {
            baby.setColorZ("" + dadZ); // Con cái chỉ cần 1 alen lặn từ bố là phát màu hiếm!
        }

        // 3. Các gen bình thường (Có 2% đột biến)
        baby.setElements(mixGenes(dad.getElements(), mom.getElements(), 1, 0.02));
        baby.setIvs(mixGenes(dad.getIvs(), mom.getIvs(), 3, 0.02));
        baby.setMutations(mixGenes(dad.getMutations(), mom.getMutations(), 3, 0.02));
        baby.setEpistasis(mixGenes(dad.getEpistasis(), mom.getEpistasis(), 1, 0.005));
        baby.setBehavior(mixGenes(dad.getBehavior(), mom.getBehavior(), 1, 0.02));

        return baby;
    }

    public static void applyPhenotype(TameableDragonEntity dragon, GeneticData genes) {
        // --- CHỈ SỐ (ATTRIBUTES) ---
        int dominantIVs = 0;
        for (char c : genes.getIvs().toCharArray()) if (Character.isUpperCase(c)) dominantIVs++;

        // Máu & Cận huyết
        var healthAttr = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(MOD_HEALTH);
            double bonusHealth = dominantIVs * 10.0;
            if (genes.getHealthStatus().equals("xx")) bonusHealth -= 20.0; // Phạt máu nếu cận huyết
            healthAttr.addPermanentModifier(new AttributeModifier(MOD_HEALTH, bonusHealth, AttributeModifier.Operation.ADD_VALUE));
        }

        // Kích thước (FIX 2: Dùng ADD_MULTIPLIED_BASE để không đè cơ chế lớn lên của mod gốc)
        var scaleAttr = dragon.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            scaleAttr.removeModifier(MOD_SCALE);
            double scaleBonus = dominantIVs * 0.05; // Tối đa to hơn 30% (6 alen x 0.05)
            scaleAttr.addPermanentModifier(new AttributeModifier(MOD_SCALE, scaleBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        // Tính cách (Behavior)
        var dmgAttr = dragon.getAttribute(Attributes.ATTACK_DAMAGE);
        var speedAttr = dragon.getAttribute(Attributes.MOVEMENT_SPEED);
        
        if (dmgAttr != null) dmgAttr.removeModifier(MOD_DAMAGE);
        if (speedAttr != null) speedAttr.removeModifier(MOD_SPEED);

        if (genes.getBehavior().equals("TT") && dmgAttr != null) {
            dmgAttr.addPermanentModifier(new AttributeModifier(MOD_DAMAGE, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)); // +20% Dmg
        } else if (genes.getBehavior().equals("Tt") && speedAttr != null) {
            speedAttr.addPermanentModifier(new AttributeModifier(MOD_SPEED, 0.30, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)); // +30% Tốc độ
        }

        // --- MÀU SẮC & BIẾN THỂ (PHENOTYPE) ---
        // Át gen (Void đè bẹp tất cả)
        if (genes.getEpistasis().equals("vv")) {
            dragon.setBreed(DragonBreedsRegistry.getDragonBreed("end")); 
            dragon.setVariant("void");
            return; 
        }

        String elements = genes.getElements();
        if (elements.equals("FW") || elements.equals("WF")) dragon.setBreed(DragonBreedsRegistry.getDragonBreed("steam"));
        else if (elements.equals("FI") || elements.equals("IF")) dragon.setBreed(DragonBreedsRegistry.getDragonBreed("obsidian"));

        // Gen lặn liên kết giới tính (c) cho ra màu Vàng (Golden)
        String colorZ = genes.getColorZ();
        if (colorZ.equals("cc") || colorZ.equals("c")) {
            dragon.setVariant("golden"); 
        } 
        // Các biến dị khác
        else if (genes.getMutations().contains("mm")) dragon.setVariant("albino");
        else if (genes.getMutations().contains("nn")) dragon.setVariant("melanistic");
        else if (genes.getMutations().contains("bb")) dragon.setVariant("bioluminescent");
    }
}