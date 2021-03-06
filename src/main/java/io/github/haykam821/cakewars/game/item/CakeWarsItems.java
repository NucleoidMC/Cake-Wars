package io.github.haykam821.cakewars.game.item;

import io.github.haykam821.cakewars.Main;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public enum CakeWarsItems implements ItemConvertible {
	WHITE_DEPLOY_PLATFORM("white_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.WHITE)),
	ORANGE_DEPLOY_PLATFORM("orange_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.ORANGE)),
	MAGENTA_DEPLOY_PLATFORM("magenta_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.MAGENTA)),
	LIGHT_BLUE_DEPLOY_PLATFORM("light_blue_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.LIGHT_BLUE)),
	YELLOW_DEPLOY_PLATFORM("yellow_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.YELLOW)),
	LIME_DEPLOY_PLATFORM("lime_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.LIME)),
	PINK_DEPLOY_PLATFORM("pink_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.PINK)),
	GRAY_DEPLOY_PLATFORM("gray_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.GRAY)),
	LIGHT_GRAY_DEPLOY_PLATFORM("light_gray_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.LIGHT_GRAY)),
	CYAN_DEPLOY_PLATFORM("cyan_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.CYAN)),
	PURPLE_DEPLOY_PLATFORM("purple_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.PURPLE)),
	BLUE_DEPLOY_PLATFORM("blue_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.BLUE)),
	BROWN_DEPLOY_PLATFORM("brown_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.BROWN)),
	GREEN_DEPLOY_PLATFORM("green_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.GREEN)),
	RED_DEPLOY_PLATFORM("red_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.RED)),
	BLACK_DEPLOY_PLATFORM("black_deploy_platform", new DeployPlatformItem(new Item.Settings(), DyeColor.BLACK));

	private final Item item;

	private CakeWarsItems(String path, Item item) {
		Identifier id = new Identifier(Main.MOD_ID, path);
		this.item = item;

		Registry.register(Registry.ITEM, id, item);
	}

	@Override
	public Item asItem() {
		return this.item;
	}

	public static void initialize() {
		DispenserBehavior behavior = new FallibleItemDispenserBehavior() {
			@Override
			protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				this.setSuccess(false);

				if (stack.getItem() instanceof DeployPlatformItem) {
					DeployPlatformItem deployPlatform = (DeployPlatformItem) stack.getItem();

					World world = pointer.getWorld();
					Direction facing = pointer.getBlockState().get(DispenserBlock.FACING);
					BlockPos centerPos = pointer.getBlockPos().offset(facing, 2);

					if (deployPlatform.placeAround(centerPos, world)) {
						deployPlatform.playSound(world, null, centerPos);
						stack.decrement(1);
						this.setSuccess(true);
					}
				}

				return stack;
			}
		};

		DispenserBlock.registerBehavior(CakeWarsItems.WHITE_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.ORANGE_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.MAGENTA_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.LIGHT_BLUE_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.YELLOW_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.LIME_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.PINK_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.GRAY_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.LIGHT_GRAY_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.CYAN_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.PURPLE_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.BLUE_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.BROWN_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.GREEN_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.RED_DEPLOY_PLATFORM, behavior);
		DispenserBlock.registerBehavior(CakeWarsItems.BLACK_DEPLOY_PLATFORM, behavior);
	}
}
