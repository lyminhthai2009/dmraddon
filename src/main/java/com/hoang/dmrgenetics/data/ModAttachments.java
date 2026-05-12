package com.hoang.dmrgenetics.data;

import com.hoang.dmrgenetics.DMRGenetics;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.NeoForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.neoforged.neoforge.attachment.AttachmentType;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = 
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DMRGenetics.MODID);

    public static final RegistryObject<AttachmentType<GeneticData>> DRAGON_GENETICS = ATTACHMENTS.register(
            "dragon_genetics",
            () -> AttachmentType.serializable(() -> new GeneticData()).build()
    );
}