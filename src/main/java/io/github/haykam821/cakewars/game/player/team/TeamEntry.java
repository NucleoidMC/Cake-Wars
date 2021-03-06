package io.github.haykam821.cakewars.game.player.team;

import org.apache.commons.lang3.RandomStringUtils;

import io.github.haykam821.cakewars.game.phase.CakeWarsActivePhase;
import io.github.haykam821.cakewars.game.player.PlayerEntry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class TeamEntry {
	private static final Formatting HAS_PLAYERS_FORMATTING = Formatting.GREEN;
	private static final String HAS_CAKE_STRING = "" + TeamEntry.HAS_PLAYERS_FORMATTING + Formatting.BOLD + "✔";
	private static final String NO_PLAYERS_STRING = "" + Formatting.RED + Formatting.BOLD + "❌";

	private final CakeWarsActivePhase phase;
	private final GameTeam gameTeam;
	private final Team scoreboardTeam;
	private final TeamUpgrades upgrades = new TeamUpgrades();
	private final BlockBounds spawnBounds;
	private final BlockBounds generatorBounds;
	private final BlockBounds cakeBounds;
	private boolean cake = true;
	private int cakeEatCooldown = 0;
	private int generatorCooldown = 0;

	public TeamEntry(CakeWarsActivePhase phase, GameTeam gameTeam, MinecraftServer server, MapTemplate template) {
		this.phase = phase;
		this.gameTeam = gameTeam;

		ServerScoreboard scoreboard = server.getScoreboard();
		String key = RandomStringUtils.randomAlphanumeric(16);
		this.scoreboardTeam = TeamEntry.getOrCreateScoreboardTeam(key, scoreboard);
		this.initializeTeam();

		this.spawnBounds = this.getBoundsOrDefault(template, "spawn");
		this.generatorBounds = this.getBoundsOrDefault(template, "generator");
		this.cakeBounds = this.getBoundsOrDefault(template, "cake");
	}

	public ItemStack getHelmet() {
		return this.getArmorItem(Items.LEATHER_HELMET);
	}

	public ItemStack getChestplate() {
		return this.getArmorItem(Items.LEATHER_CHESTPLATE);
	}

	public ItemStack getLeggings() {
		return this.getArmorItem(Items.LEATHER_LEGGINGS);
	}

	public ItemStack getBoots() {
		return this.getArmorItem(Items.LEATHER_BOOTS);
	}

	private ItemStack getArmorItem(ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.setColor(this.gameTeam.getColor())
			.setUnbreakable()
			.build();
	}

	public GameTeam getGameTeam() {
		return this.gameTeam;
	}

	public Team getScoreboardTeam() {
		return this.scoreboardTeam;
	}

	public TeamUpgrades getUpgrades() {
		return this.upgrades;
	}

	public BlockBounds getSpawnBounds() {
		return this.spawnBounds;
	}

	public BlockBounds getGeneratorBounds() {
		return this.generatorBounds;
	}

	public BlockBounds getCakeBounds() {
		return this.cakeBounds;
	}

	public boolean hasCake() {
		return this.cake;
	}

	public void removeCake(PlayerEntry eater) {
		this.cake = false;

		this.phase.pling();
		this.phase.getSidebar().update();
		this.phase.getGameSpace().getPlayers().sendMessage(this.getCakeEatenText(eater.getPlayer().getDisplayName()));

		// Title
		Text title = new TranslatableText("text.cakewars.cake_eaten.title").formatted(this.gameTeam.getFormatting()).formatted(Formatting.BOLD);
		Text subtitle = new TranslatableText("text.cakewars.cake_eaten.subtitle");

		for (PlayerEntry player : this.phase.getPlayers()) {
			if (this == player.getTeam()) {
				player.sendPacket(new TitleS2CPacket(10, 60, 10));
				player.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, title));
				player.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, subtitle));
			}
		}
	}

	private Text getCakeEatenText(Text eaterName) {
		return new TranslatableText("text.cakewars.cake_eaten", this.getName(), eaterName).formatted(Formatting.RED);
	}

	public void resetCakeEatCooldown() {
		this.cakeEatCooldown = this.phase.getConfig().getCakeEatCooldown();
	}

	public void spawnGeneratorItem(ItemConvertible item) {
		ItemStack stack = new ItemStack(item, 1);

		boolean inserted = false;
		for (PlayerEntry player : this.phase.getPlayers()) {
			if (this.generatorBounds.contains(player.getPlayer().getBlockPos())) {
				player.getPlayer().giveItemStack(stack.copy());
				inserted = true;
			}
		}

		if (!inserted) {
			Vec3d centerPos = this.generatorBounds.getCenter();
			ServerWorld world = this.phase.getGameSpace().getWorld();

			ItemEntity itemEntity = new ItemEntity(world, centerPos.getX(), this.generatorBounds.getMin().getY(), centerPos.getZ(), stack);
			itemEntity.setVelocity(Vec3d.ZERO);
			itemEntity.setToDefaultPickupDelay();
			world.spawnEntity(itemEntity);
		}
	}

	public void tick() {
		if (this.cakeEatCooldown > 0) {
			this.cakeEatCooldown -= 1;
		}

		if (this.generatorCooldown > 0) {
			this.generatorCooldown -= 1;
		}
		if (this.generatorCooldown <= 0) {
			this.generatorCooldown = this.phase.getConfig().getBrickGeneratorCooldown();
			this.spawnGeneratorItem(Items.BRICK);
		}
	}

	public boolean canEatCake() {
		return this.cakeEatCooldown == 0;
	}

	public String getSidebarEntryString(int playerCount) {
		return this.getSidebarEntryIcon(playerCount) + " " + this.getBoldNameString();
	}

	public String getSidebarEntryIcon(int playerCount) {
		if (this.cake) {
			return TeamEntry.HAS_CAKE_STRING;
		} else if (playerCount == 0) {
			return TeamEntry.NO_PLAYERS_STRING;
		} else {
			return "" + TeamEntry.HAS_PLAYERS_FORMATTING + playerCount;
		}
	}

	public String getBoldNameString() {
		return "" + this.gameTeam.getFormatting() + Formatting.BOLD + this.gameTeam.getDisplay();
	}

	public Text getName() {
		return new LiteralText(this.gameTeam.getDisplay()).formatted(this.gameTeam.getFormatting());
	}

	private Text getUncoloredName() {
		return new LiteralText(this.gameTeam.getDisplay());
	}

	public void sendMessage(Text message) {
		for (PlayerEntry player : this.phase.getPlayers()) {
			if (player.getTeam() == this) {
				player.getPlayer().sendMessage(message, false);
			}
		}
	}

	public void sendMessageIncludingSpectators(Text message) {
		for (ServerPlayerEntity player : this.phase.getGameSpace().getPlayers()) {
			PlayerEntry entry = this.phase.getPlayerEntry(player);
			if (entry == null || entry.getTeam() == this) {
				player.sendMessage(message, false);
			}
		}
	}

	private void initializeTeam() {
		// Display
		this.scoreboardTeam.setDisplayName(this.getUncoloredName());
		this.scoreboardTeam.setColor(this.gameTeam.getFormatting());

		// Rules
		this.scoreboardTeam.setFriendlyFireAllowed(false);
		this.scoreboardTeam.setShowFriendlyInvisibles(true);
		this.scoreboardTeam.setCollisionRule(Team.CollisionRule.PUSH_OTHER_TEAMS);
	}

	private static Team getOrCreateScoreboardTeam(String key, ServerScoreboard scoreboard) {
		Team scoreboardTeam = scoreboard.getTeam(key);
		if (scoreboardTeam == null) {
			return scoreboard.addTeam(key);
		}
		return scoreboardTeam;
	}

	private BlockBounds getBoundsOrDefault(MapTemplate template, String key) {
		BlockBounds bounds = template.getMetadata().getFirstRegionBounds(this.gameTeam.getKey() + "_" + key);
		return bounds == null ? BlockBounds.EMPTY : bounds;
	}
}
