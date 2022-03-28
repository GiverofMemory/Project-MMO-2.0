package harmonised.pmmo.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;

import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ChunkDataProvider implements ICapabilitySerializable<CompoundTag>{
	public static final ResourceLocation CHUNK_CAP_ID = new ResourceLocation(Reference.MOD_ID, "placed_data");
	public static final Capability<IChunkData> CHUNK_CAP = CapabilityManager.get(new CapabilityToken<IChunkData>() {});
	
	private final ChunkDataHandler backend = new ChunkDataHandler();
	private LazyOptional<IChunkData> instance = LazyOptional.of(() -> backend);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == CHUNK_CAP ? instance.cast() : LazyOptional.empty();
	}
	
	private static final Codec<Map<BlockPos, UUID>> CODEC = Codec.unboundedMap(CodecTypes.BLOCKPOS_CODEC, SerializableUUID.CODEC);
	@Override
	public CompoundTag serializeNBT() {
		return (CompoundTag)CODEC.encodeStart(NbtOps.INSTANCE, getCapability(CHUNK_CAP, null).orElse(backend).getMap()).result().orElse(new CompoundTag());
	}
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		getCapability(CHUNK_CAP, null).orElse(backend).setMap(CODEC.parse(NbtOps.INSTANCE, nbt).result().orElse(new HashMap<>()));		
	}
	
	
}