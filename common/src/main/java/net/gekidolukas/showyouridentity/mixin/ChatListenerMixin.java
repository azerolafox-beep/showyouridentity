package net.gekidolukas.showyouridentity.mixin;

import com.mojang.authlib.GameProfile;
import net.gekidolukas.showyouridentity.data.IdentityData;
import net.gekidolukas.showyouridentity.data.IdentityEntry;
import net.gekidolukas.showyouridentity.data.PrideFlag;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin {

    private static final ThreadLocal<GameProfile> CURRENT_CHAT_PROFILE = new ThreadLocal<>();

    @Inject(method = "handlePlayerChatMessage", at = @At("HEAD"))
    private void captureSenderProfile(PlayerChatMessage playerChatMessage, GameProfile profile, ChatType.Bound bound, CallbackInfo ci) {
        CURRENT_CHAT_PROFILE.set(profile);
    }

    @ModifyVariable(
            method = "handlePlayerChatMessage",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private ChatType.Bound modifyBound(ChatType.Bound bound) {
        GameProfile profile = CURRENT_CHAT_PROFILE.get();
        Minecraft mc = Minecraft.getInstance();

        if (profile != null && mc.level != null) {
               if (profile.getId() != null) {
                IdentityData identityData = IdentityData.get(mc.level);
                IdentityEntry entry = identityData.getIdentity(profile.getId());

                if (entry != null && entry.getPronouns() != null && !entry.getPronouns().isEmpty()) {
                    ResourceLocation defaultFont = new ResourceLocation("minecraft:default");

                    Component pronouns = Component.literal(" ")
                            .append(Component.literal("- ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(entry.getPronouns()).withStyle(ChatFormatting.GOLD))
                            ;


                    Component newName = PrideFlag.applyChatFlags(bound.name(), entry.getPrimaryFlag() ,entry.getSecondaryFlag()).copy().append(pronouns.copy().withStyle(style -> style.withFont(defaultFont)));

                    return new ChatType.Bound(bound.chatType(), newName, bound.targetName());
                }
            }
        }
        return bound;
    }
    @Inject(method = "handlePlayerChatMessage", at = @At("RETURN"))
    private void clearSenderProfile(PlayerChatMessage playerChatMessage, GameProfile gameProfile, ChatType.Bound bound, CallbackInfo ci) {
        CURRENT_CHAT_PROFILE.remove();
    }
}
