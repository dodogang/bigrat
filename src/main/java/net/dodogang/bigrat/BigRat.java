package net.dodogang.bigrat;

import net.dodogang.bigrat.init.BigRatEntities;
import net.dodogang.bigrat.init.BigRatItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BigRat implements ModInitializer {
    public static final String MOD_ID = "bigrat";
    public static final String MOD_NAME = "Big Rat";

    public static Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier(BigRat.MOD_ID, "item_group"), () -> new ItemStack(Items.BROWN_DYE));

    @Override
    public void onInitialize() {
        log("Initializing");

        new BigRatItems();
        new BigRatEntities();

        log("Initialized");
    }

    private static final String FORMATTED_MOD_NAME = "[" + MOD_NAME + "]";
    public static void log(Level level, String message) {
        LOGGER.log(level, FORMATTED_MOD_NAME + " " + message);
    }
    public static void log(String message) {
        log(Level.INFO, message);
    }

    public static Identifier texture(String path) {
        return new Identifier(MOD_ID, "textures/" + path + ".png");
    }
}
