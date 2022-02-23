package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class DeathHandler {

	public static void handle(LivingDeathEvent event) {
		//Check the source entity isn't null.  This should also reduce
		//the number of events processed.
		if (event.getSource().getEntity() == null) return;
		
		//Execute actual logic only if the source is a player
		if (event.getSource().getEntity() instanceof Player) {
			LivingEntity target = event.getEntityLiving();
			//Confirm our target is a living entity
			if (target == null) return;
			
			Player player = (Player) event.getSource().getEntity();
			if (target.equals(player))
				return;
			Core core = Core.get(player.level);			
			
			//===========================DEFAULT LOGIC===================================
			if (!core.isActionPermitted(ReqType.WEAPON, player.getMainHandItem(), player)) {
				event.setCanceled(true);
				//TODO notify player of inability to perform
				return;
			}
			if (!core.isActionPermitted(ReqType.KILL, target, player)) {
				event.setCanceled(true);
				//TODO notify player of inability to perform
			}
			boolean serverSide = !player.level.isClientSide;
			CompoundTag hookOutput = new CompoundTag();
			if (serverSide) {
				hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.DEATH, event, new CompoundTag());
				if (hookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
					event.setCanceled(true);
			}
			//Process perks
			hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.DEATH, player, hookOutput, core.getSide()));
			if (serverSide) {
				Map<String, Long> xpAward = core.getExperienceAwards(EventType.DEATH, target, player, hookOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
}
