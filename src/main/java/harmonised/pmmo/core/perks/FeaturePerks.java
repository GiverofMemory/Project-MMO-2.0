package harmonised.pmmo.core.perks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

public class FeaturePerks {
	private static final CompoundTag NONE = new CompoundTag();

	private static Map<UUID, Long> regen_cooldown = new HashMap<>();
	private static Map<UUID, Long> breathe_cooldown = new HashMap<>();
	
	private static final UUID speedModifierID  = UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3");	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> SPEED = (player, nbt, level) -> {
		double maxSpeedBoost = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 1d;
		double boostPerLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.5;
		AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
		double speedBoost = player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() 
							* Math.max(0, Math.min(maxSpeedBoost, Math.min(maxSpeedBoost, (level * boostPerLevel) / 100)));

		if(speedBoost > 0)
		{
			if(speedAttribute.getModifier(speedModifierID) == null || speedAttribute.getModifier(speedModifierID).getAmount() != speedBoost)
			{
				AttributeModifier speedModifier = new AttributeModifier(speedModifierID, "Speed bonus thanks to Agility Level", speedBoost, AttributeModifier.Operation.ADDITION);
				speedAttribute.removeModifier(speedModifierID);
				speedAttribute.addPermanentModifier(speedModifier);
			}
		}
		return NONE;
	};
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> SPEED_TERM = (p, nbt, l) -> {
		AttributeInstance speedAttribute = p.getAttribute(Attributes.MOVEMENT_SPEED);
		speedAttribute.removeModifier(speedModifierID);
		return NONE;
	};
	
	private static final UUID damageModifierID = UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DAMAGE = (player, nbt, level) -> {
		double maxDamage = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 1;
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.05;
		AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
		double damageBoost = Math.min(maxDamage, level * perLevel);
		AttributeModifier damageModifier = new AttributeModifier(damageModifierID, "Damage Boost thanks to Combat Level", damageBoost, AttributeModifier.Operation.MULTIPLY_BASE);
		damageAttribute.removeModifier(damageModifierID);
		damageAttribute.addPermanentModifier(damageModifier);
		return NONE;
	};
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DAMAGE_TERM = (player, nbt, level) -> {
		AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
		damageAttribute.removeModifier(damageModifierID);
		return NONE;
	};
	
	private static final UUID reachModifierID  = UUID.fromString("b20d3436-0d39-4868-96ab-d0a4856e68c6");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> REACH = (player, nbt, level) -> {
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.1;
		double maxReach = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 10d;
		double reach = -0.91 + (level * perLevel);
		reach = Math.min(maxReach, reach);
		reach = player.isCreative() ? Math.max(50, reach) : reach;		
		AttributeInstance reachAttribute = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
		if(reachAttribute.getModifier(reachModifierID) == null || reachAttribute.getModifier(reachModifierID).getAmount() != reach)
		{
			AttributeModifier reachModifier = new AttributeModifier(reachModifierID, "Reach bonus thanks to Build Level", reach, AttributeModifier.Operation.ADDITION);
			reachAttribute.removeModifier(reachModifierID);
			reachAttribute.addPermanentModifier(reachModifier);
		}
		return NONE;
	};
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> REACH_TERM = (player, nbt, level) -> {
		AttributeInstance reachAttribute = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
		reachAttribute.removeModifier(reachModifierID);
		return NONE;
	};
	
	private static final UUID hpModifierID     = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> HEALTH = (player, nbt, level) -> {
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.1;
		int maxHeart	= nbt.contains(APIUtils.MAX_BOOST) ? nbt.getInt(APIUtils.MAX_BOOST) : 10;
		int heartBoost = (int)(perLevel * (double)level);
		heartBoost = Math.min(maxHeart, heartBoost);		
		AttributeInstance hpAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		System.out.println("heartBoost: "+heartBoost+" | "+hpAttribute.toString());
		AttributeModifier hpModifier = new AttributeModifier(hpModifierID, "Max HP Bonus thanks to Endurance Level", heartBoost, AttributeModifier.Operation.ADDITION);
		hpAttribute.removeModifier(hpModifierID);
		hpAttribute.addPermanentModifier(hpModifier);
		return NONE;
	};
	
	public static final TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> HEALTH_TERM = (player, nbt, level) -> {
		AttributeInstance hpAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		hpAttribute.removeModifier(hpModifierID);
		return NONE;
	};

	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> NIGHT_VISION = (player, nbt, level) -> {
		int min = nbt.contains(APIUtils.MIN_LEVEL) ? nbt.getInt(APIUtils.MIN_LEVEL) : 50;
		int duration = nbt.contains(APIUtils.DURATION) ? nbt.getInt(APIUtils.DURATION) : 30000;
		if (level < min) return NONE;
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration));
		return NONE;
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> NIGHT_VISION_TERM = (player, nbt, level) -> {
		MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
		if (effect == null) return NONE;
		if (effect.getDuration() > 480) {
			player.removeEffect(MobEffects.NIGHT_VISION);
		}
		return NONE;
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> REGEN = (player, nbt, level) -> {
		long cooldown = nbt.contains(APIUtils.COOLDOWN) ? nbt.getLong(APIUtils.COOLDOWN) : 300l;
		int duration = nbt.contains(APIUtils.DURATION) ? nbt.getInt(APIUtils.DURATION) : 1;
		double strength = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.02;
		int perLevel = Math.max(0, (int)((double)level * strength));
		long currentCD = regen_cooldown.getOrDefault(player.getUUID(), System.currentTimeMillis());
		if (currentCD < System.currentTimeMillis() - cooldown 
				|| currentCD + 20 >= System.currentTimeMillis()) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, perLevel));
			regen_cooldown.put(player.getUUID(), System.currentTimeMillis());
		}
		return NONE;
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> JUMP = (player, nbt, level) -> {
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.0005;
		double maxBoost = nbt.contains(APIUtils.MAX_BOOST) ? nbt.getDouble(APIUtils.MAX_BOOST) : 0.33;
        double jumpBoost;
        jumpBoost = -0.011 + level * perLevel;
        jumpBoost = Math.min(maxBoost, jumpBoost);
        player.push(0, jumpBoost, 0);
        player.hurtMarked = true; 
        CompoundTag output = new CompoundTag();
        output.putDouble("power", jumpBoost);
        return output;
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> BREATH = (player, nbt, level) -> {
		long cooldown = nbt.contains(APIUtils.COOLDOWN) ? nbt.getLong(APIUtils.COOLDOWN) : 300l;
		double strength = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 1d;
		int perLevel = Math.max(1, (int)((double)level * strength));
		long currentCD = breathe_cooldown.getOrDefault(player.getUUID(), System.currentTimeMillis());
		int currentAir = player.getAirSupply();
		if (currentAir < 2 && (currentCD < System.currentTimeMillis() - cooldown 
				|| currentCD + 20 >= System.currentTimeMillis())) {
			player.setAirSupply(currentAir + perLevel);
			player.sendMessage(new TranslatableComponent("pmmo.perks.breathrefresh"), ChatType.GAME_INFO, player.getUUID());
			breathe_cooldown.put(player.getUUID(), System.currentTimeMillis());
		}
		return new CompoundTag();
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> FALL_SAVE = (player, nbt, level) -> {
		CompoundTag output = new CompoundTag();
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.1;
		int saved = (int)(perLevel * (double)level);
		output.putInt("saved", saved);
		return output;
	};

	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DAMAGE_BOOST = (player, nbt, level) -> {
		CompoundTag output = new CompoundTag();
		if (!nbt.contains("damageIn")) return output;
		double perLevel = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getDouble(APIUtils.PER_LEVEL) : 0.05;
		float damage = nbt.getFloat("damageIn") * (float)(perLevel * (double)level);
		output.putFloat("damage", damage);
		return output;
	};
}