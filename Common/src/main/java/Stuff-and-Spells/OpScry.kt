package StuffAndSpells.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.entity.Entity

object OpScry : SpellOperator {
	override val argc = 3

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val pos = args.getChecked<Vec3>(0, argc)
		val dir = args.getChecked<Vec3>(1, argc)
		val time = args.getChecked<Double>(0, argc)
		const val COST = (0.25 * ManaConstants.DUST_UNIT * time * (pos - ctx.caster.asActionResult.eyePosition).length()).toInt()
		
		return Triple(
			Spell(pos),
			COST,
			listOf(ParticleSpray.burst(pos, 1.0))
		)
	}

	private data class Spell(val pos: Vec3) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			Minecraft mc = Minecraft.getMinecraft()
			Networking.INSTANCE.sendToServer(new PacketMountCamera(pos))
			if (!level.isClientSide) {
				ServerLevel serverLevel = (ServerLevel) level
				ServerPlayer serverPlayer = (ServerPlayer) player
				SectionPos chunkPos = SectionPos.of(pos)
				int viewDistance = serverPlayer.server.getPlayerList().getViewDistance()
				Entity var10 = serverPlayer.getCamera()
				ScryerCamera dummyEntity
				if (var10 instanceof ScryerCamera cam) {
					dummyEntity = new ScryerCamera(level, pos, cam)
				} else {
					dummyEntity = new ScryerCamera(level, pos)
				}
				level.addFreshEntity(dummyEntity)
				for (int x = chunkPos.getX() - viewDistance; x <= chunkPos.getX() + viewDistance; x++) {
					for (int z = chunkPos.getZ() - viewDistance; z <= chunkPos.getZ() + viewDistance; z++) {
						ForgeChunkManager.forceChunk(serverLevel, ArsNouveau.MODID, dummyEntity, x, z, true, false)
					}
				}
				serverPlayer.camera = dummyEntity
				Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PacketSetCameraView(dummyEntity))
				startViewing()
			}
			ctx.world.setBlockAndUpdate(BlockPos(pos), Blocks.PUMPKIN.defaultBlockState())
		}
	}
}