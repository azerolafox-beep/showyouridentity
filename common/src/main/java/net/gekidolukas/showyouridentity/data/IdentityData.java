package net.gekidolukas.showyouridentity.data;

import net.gekidolukas.showyouridentity.IdentityDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface IdentityData {


    IdentityEntry getIdentity(Player player);
    IdentityEntry getIdentity(UUID playerUUID);
    void putIdentity(Player player,IdentityEntry entry);
    void removeIdentity(Player player);
    void putMap(Map<UUID, IdentityEntry> map);
    void reset();
    void markNeoDirty();

    void sync(Level level);
    void syncToPlayer(ServerPlayer serverPlayer);
    static IdentityData get(Level level) {
        return IdentityDataAccessor.getIdentityData(level);
    }




    static Map<UUID, IdentityEntry> nbtToMap(CompoundTag nbt) {
        Map<UUID, IdentityEntry> map = new HashMap<>();

        for (String key : nbt.getAllKeys()) {
            CompoundTag profileNbt = nbt.getCompound(key);

            IdentityEntry identity = IdentityEntry.fromNBT(profileNbt);

            map.put(UUID.fromString(key), identity);
        }

        return map;
    }

    static CompoundTag mapToNbt(Map<UUID, IdentityEntry> map) {
        CompoundTag nbt = new CompoundTag();

        for (Map.Entry<UUID, IdentityEntry> mapEntry : map.entrySet()) {
            String key = mapEntry.getKey().toString();
            IdentityEntry entry = mapEntry.getValue();

            CompoundTag profileNbt = entry.toNBT();

            nbt.put(key, profileNbt);
        }

        return nbt;
    }
}
