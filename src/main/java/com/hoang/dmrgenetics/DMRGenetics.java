package com.hoang.dmrgenetics;

import com.hoang.dmrgenetics.data.ModAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(DMRGenetics.MODID)
public class DMRGenetics {
    public static final String MODID = "dmrgenetics";

    public DMRGenetics(IEventBus modEventBus) {
        // Đăng ký Attachments (Biến lưu trữ NBT)
        ModAttachments.ATTACHMENTS.register(modEventBus);
    }
}