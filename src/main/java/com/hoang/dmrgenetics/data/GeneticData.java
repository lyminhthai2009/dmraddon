package com.hoang.dmrgenetics.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class GeneticData implements INBTSerializable<CompoundTag> {
    private String sex = "ZZ";           // ZZ (Đực), ZW (Cái)
    private String elements = "FF";      // F (Fire), W (Water), I (Ice)
    private String ivs = "AaBbCc";       // A, B, C (Trội), a, b, c (Lặn)
    private String mutations = "MMNNBB"; // m (Albino), n (Melanism), b (Bioluminescent)
    private String epistasis = "VV";     // v (Void - lặn, vv = Rồng hư không)

    // Khởi tạo Gen mặc định cho rồng tự nhiên
    public GeneticData() {}

    public GeneticData(String sex, String elements, String ivs, String mutations, String epistasis) {
        this.sex = sex;
        this.elements = elements;
        this.ivs = ivs;
        this.mutations = mutations;
        this.epistasis = epistasis;
    }

    // Getters & Setters
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    
    public String getElements() { return elements; }
    public String getIvs() { return ivs; }
    public String getMutations() { return mutations; }
    public String getEpistasis() { return epistasis; }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("SexGen", sex);
        tag.putString("ElemGen", elements);
        tag.putString("IVGen", ivs);
        tag.putString("MutGen", mutations);
        tag.putString("EpiGen", epistasis);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.sex = nbt.getString("SexGen");
        this.elements = nbt.getString("ElemGen");
        this.ivs = nbt.getString("IVGen");
        this.mutations = nbt.getString("MutGen");
        this.epistasis = nbt.getString("EpiGen");
    }
}