package net.dodogang.bigrat.init;

import net.dodogang.bigrat.BigRat;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("unused")
public class BigRatItems {
    public static Item register(String id, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(BigRat.MOD_ID, id), item);
    }
}
