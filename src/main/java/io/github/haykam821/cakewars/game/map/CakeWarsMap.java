package io.github.haykam821.cakewars.game.map;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;

public class CakeWarsMap {
	private final MapTemplate template;
	private final LongSet initialBlocks = new LongOpenHashSet();
	private final Vec3d spawnPos;

	public CakeWarsMap(MapTemplate template) {
		this.template = template;
		for (BlockPos pos : this.template.getBounds()) {
			if (!template.getBlockState(pos).isAir()) {
				this.initialBlocks.add(pos.asLong());
			}
		}

		this.spawnPos = this.calculateSpawnPos();
	}

	public MapTemplate getTemplate() {
		return this.template;
	}

	public boolean isInitialBlock(BlockPos pos) {
		return this.initialBlocks.contains(pos.asLong());
	}

	public Vec3d getSpawnPos() {
		return this.spawnPos;
	}

	private Vec3d calculateSpawnPos() {
		TemplateRegion spawnRegion = this.template.getMetadata().getFirstRegion("nether_star_beacon");
		if (spawnRegion == null) {
			return this.template.getBounds().getCenter();
		}

		Vec3d center = spawnRegion.getBounds().getCenter();
		return new Vec3d(center.getX(), spawnRegion.getBounds().getMin().getY() + 2, center.getZ());
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}