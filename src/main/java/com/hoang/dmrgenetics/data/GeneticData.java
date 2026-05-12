package com.hoang.dmrgenetics.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import java.util.UUID;

public class GeneticData implements INBTSerializable<CompoundTag> {
    private boolean isInitialized = false;
    
    // UUID để check cận huyết (Inbreeding)
    private UUID sireId = null; // Bố
    private UUID damId = null;  // Mẹ

    // Gen cơ bản
    private String sex = "ZZ"; 
    private String colorZ = "CC"; // Di truyền liên kết nst Z (Male: CC/Cc/cc, Female: C/c)
    private String elements = "FF";
    private String ivs = "AaBbCc";
    private String mutations = "MMNNBB";
    private String epistasis = "VV";
    
    // Gen mới
    private String behavior = "Tt"; // Tính cách
    private String healthStatus = "XX"; // XX: Khỏe, xx: Suy nhược do cận huyết

    public GeneticData() {}

    // Getters & Setters
    public boolean isInitialized() { return isInitialized; }
    public void setInitialized(boolean init) { this.isInitialized = init; }

    public UUID getSireId() { return sireId; }
    public UUID getDamId() { return damId; }
    public void setParents(UUID sire, UUID dam) { this.sireId = sire; this.damId = dam; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getColorZ() { return colorZ; }
    public void setColorZ(String colorZ) { this.colorZ = colorZ; }

    public String getElements() { return elements; }
    public void setElements(String elements) { this.elements = elements; }

    public String getIvs() { return ivs; }
    public void setIvs(String ivs) { this.ivs = ivs; }

    public String getMutations() { return mutations; }
    public void setMutations(String mutations) { this.mutations = mutations; }

    public String getEpistasis() { return epistasis; }
    public void setEpistasis(String epistasis) { this.epistasis = epistasis; }

    public String getBehavior() { return behavior; }
    public void setBehavior(String behavior) { this.behavior = behavior; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("IsInit", isInitialized);
        if (sireId != null) tag.putUUID("SireID", sireId);
        if (damId != null) tag.putUUID("DamID", damId);
        
        tag.putString("SexGen", sex);
        tag.putString("ColorZGen", colorZ);
        tag.putString("ElemGen", elements);
        tag.putString("IVGen", ivs);
        tag.putString("MutGen", mutations);
        tag.putString("EpiGen", epistasis);
        tag.putString("BehavGen", behavior);
        tag.putString("HealthGen", healthStatus);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.isInitialized = nbt.getBoolean("IsInit");
        if (nbt.hasUUID("SireID")) this.sireId = nbt.getUUID("SireID");
        if (nbt.hasUUID("DamID")) this.damId = nbt.getUUID("DamID");
        
        // Dùng fallback values để chống Crash nếu NBT cũ bị thiếu
        this.sex = nbt.getString("SexGen").isEmpty() ? "ZZ" : nbt.getString("SexGen");
        this.colorZ = nbt.getString("ColorZGen").isEmpty() ? "CC" : nbt.getString("ColorZGen");
        this.elements = nbt.getString("ElemGen").isEmpty() ? "FF" : nbt.getString("ElemGen");
        this.ivs = nbt.getString("IVGen").isEmpty() ? "AaBbCc" : nbt.getString("IVGen");
        this.mutations = nbt.getString("MutGen").isEmpty() ? "MMNNBB" : nbt.getString("MutGen");
        this.epistasis = nbt.getString("EpiGen").isEmpty() ? "VV" : nbt.getString("EpiGen");
        this.behavior = nbt.getString("BehavGen").isEmpty() ? "Tt" : nbt.getString("BehavGen");
        this.healthStatus = nbt.getString("HealthGen").isEmpty() ? "XX" : nbt.getString("HealthGen");
    }
}