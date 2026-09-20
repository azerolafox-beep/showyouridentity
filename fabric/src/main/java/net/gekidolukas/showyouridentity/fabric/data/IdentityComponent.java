package net.gekidolukas.showyouridentity.fabric.data;

import dev.architectury.networking.NetworkManager;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.gekidolukas.showyouridentity.data.NameFlagPos;
import net.gekidolukas.showyouridentity.data.PrideFlag;
import net.gekidolukas.showyouridentity.data.IdentityData;
import net.gekidolukas.showyouridentity.data.IdentityEntry;
import net.gekidolukas.showyouridentity.networking.IdentityMapPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IdentityComponent implements IdentityData, ComponentV3, AutoSyncedComponent {

    Map<UUID, IdentityEntry> IDENTITIES;



    public IdentityComponent() {
        IDENTITIES  = new HashMap<>();
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if(tag.contains("identities")) {
            this.IDENTITIES = IdentityData.nbtToMap(tag.getCompound("identities"));
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.put("identities",  IdentityData.mapToNbt(IDENTITIES));
    }

    @Override
    public IdentityEntry getIdentity(Player player) {
        if(player == null) return null;
        return IDENTITIES.get(player.getUUID());
    }

    @Override
    public IdentityEntry getIdentity(UUID playerUUID) {
        return IDENTITIES.get(playerUUID);
    }


    @Override
    public void putIdentity(@NotNull Player player, @NotNull IdentityEntry entry) {
        IDENTITIES.put(player.getUUID(),entry);
    }

    @Override
    public void removeIdentity(@NotNull Player player) {
        IDENTITIES.remove(player.getUUID());
    }

    @Override
    public void putMap(Map<UUID, IdentityEntry> map) {

    }

    @Override
    public void reset() {
        IDENTITIES.clear();
    }

    @Override
    public void markNeoDirty() {
        //Unused
    }

    @Override
    public void sync(Level level) {
        SYIComponents.IDENTITY_DATA.sync(level.getScoreboard());
    }

    @Override
    public void syncToPlayer(ServerPlayer serverPlayer) {
        System.out.println("sending to " + serverPlayer);
        NetworkManager.sendToPlayer(serverPlayer, IdentityMapPacket.ID, IdentityMapPacket.encode(IDENTITIES));
    }

    @Override
    public void applySyncPacket(FriendlyByteBuf buf) {
        IDENTITIES.clear();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUUID();
            String pronouns = buf.readUtf(32767);

            int indexPrimary = buf.readVarInt();
            int indexSecondary = buf.readVarInt();
            int indexFlagPos = buf.readVarInt();
            PrideFlag primaryFlag;
            PrideFlag secondaryFlag;
            NameFlagPos flagPos;
            try {
                primaryFlag = PrideFlag.values()[indexPrimary];
                secondaryFlag = PrideFlag.values()[indexSecondary];
                flagPos = NameFlagPos.values()[indexFlagPos];
            } catch (Exception ignored) {
                primaryFlag = PrideFlag.NONE;
                secondaryFlag = PrideFlag.NONE;
                flagPos = NameFlagPos.PLAYER_NAME;
            }

            IDENTITIES.put(uuid, new IdentityEntry(pronouns, primaryFlag,secondaryFlag,flagPos));
        }
    }

    @Override
    public void writeSyncPacket(FriendlyByteBuf buf, ServerPlayer recipient) {
        buf.writeVarInt(IDENTITIES.size());
        for (Map.Entry<UUID, IdentityEntry> entry : IDENTITIES.entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeUtf(entry.getValue().getPronouns());
            buf.writeVarInt(entry.getValue().getPrimaryFlag().ordinal());
            buf.writeVarInt(entry.getValue().getSecondaryFlag().ordinal());
            buf.writeVarInt(entry.getValue().getFlagPos().ordinal());
        }
    }
}
