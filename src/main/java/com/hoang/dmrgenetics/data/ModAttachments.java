package com.hoang.dmrgenetics.data;

import com.hoang.dmrgenetics.DMRGenetics;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = 
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DMRGenetics.MODID);

    // NeoForge dùng Supplier thay cho RegistryObject
    public static final Supplier<AttachmentType<GeneticData>> DRAGON_GENETICS = ATTACHMENTS.register(
            "dragon_genetics",
            () -> AttachmentType.serializable(() -> new GeneticData()).build()
    );
}