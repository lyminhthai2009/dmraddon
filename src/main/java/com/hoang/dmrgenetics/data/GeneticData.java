package com.hoang.dmrgenetics.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class GeneticData implements INBTSerializable<CompoundTag> {
    private String sex = "ZZ";
    private String elements = "FF";
    private String ivs = "AaBbCc";
    private String mutations = "MMNNBB";
    private String epistasis = "VV";

    public GeneticData() {}

    public GeneticData(String sex, String elements, String ivs, String mutations, String epistasis) {
        this.sex = sex;
        this.elements = elements;
        this.ivs = ivs;
        this.mutations = mutations;
        this.epistasis = epistasis;
    }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    
    public String getElements() { return elements; }
    public String getIvs() { return ivs; }
    public String getMutations() { return mutations; }
    
    public String getEpistasis() { return epistasis; }
    public void setEpistasis(String epistasis) { this.epistasis = epistasis; }

    // ĐÃ SỬA: Thêm HolderLookup.Provider cho NeoForge mới
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("SexGen", sex);
        tag.putString("ElemGen", elements);
        tag.putString("IVGen", ivs);
        tag.putString("MutGen", mutations);
        tag.putString("EpiGen", epistasis);
        return tag;
    }

    // ĐÃ SỬA: Thêm HolderLookup.Provider cho NeoForge mới
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.sex = nbt.getString("SexGen");
        this.elements = nbt.getString("ElemGen");
        this.ivs = nbt.getString("IVGen");
        this.mutations = nbt.getString("MutGen");
        this.epistasis = nbt.getString("EpiGen");
    }
}