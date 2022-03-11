/**
 * Copyright (C) 2022 Honiism
 * 
 * This file is part of Lemi-Bot.
 * 
 * Lemi-Bot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Lemi-Bot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lemi-Bot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.honiism.discord.lemi.data.items;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.honiism.discord.lemi.data.items.handler.EventType;
import com.honiism.discord.lemi.data.items.handler.ItemCategory;
import com.honiism.discord.lemi.data.items.handler.ItemInterface;
import com.honiism.discord.lemi.data.items.handler.ItemType;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.interactions.InteractionHook;

public abstract class Items implements ItemInterface {

    public static List<Items> allItems = new ArrayList<Items>();

    private static final Logger log = LoggerFactory.getLogger(Items.class);

    protected String name = "";
    protected String desc = "";
    protected String emoji = "";
    protected boolean isSellable = false;
    protected boolean isBuyable = false;
    protected boolean isGiftable = false;
    protected boolean isLimited = false;
    protected boolean useableAfterLimit = false;
    protected boolean isUsable = false;
    protected boolean disappearAfterUsage = false;
    protected boolean isBreakable = false;
    protected String itemLimitDate = null;
    protected long sellingPrice = 0;
    protected long buyingPrice = 0;
    protected ItemCategory category = ItemCategory.COMMON;
    protected ItemType type = ItemType.COLLECTABLE;
    protected EventType eventType = EventType.NONE;
    
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public String getId() {
        if (getName().contains(" ")) {
            return name.replaceAll(" ", "_");
        } else {
            return name;
        }
    }

    @Override
    public String getEmoji() {
        return emoji;
    }

    @Override
    public boolean isSellable() {
        return isSellable;
    }

    @Override
    public boolean isBuyable() {
        return isBuyable;
    }

    @Override
    public boolean isGiftAble() {
        return isGiftable;
    }

    @Override
    public boolean isLimited() {
        return isLimited;
    }

    @Override
    public boolean isUsable() {
        return isUsable;
    }

    @Override
    public boolean useableAfterLimit() {
        return useableAfterLimit;
    }

    @Override
    public String getLimitedDate() {
        return itemLimitDate;
    }

    @Override
    public long getBuyingPrice() {
        return buyingPrice;
    }

    @Override
    public long getSellingPrice() {
        return sellingPrice;
    }

    @Override
    public boolean disappearAfterUsage() {
        return disappearAfterUsage;
    }

    @Override
    public ItemCategory getCategory() {
        return category;
    }

    @Override
    public ItemType getType() {
        return type;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public void useItem(InteractionHook hook) {
        if (!isUsable()) {
            return;
        }

        try {
            Date limitDate = dateFormatter.parse(getLimitedDate());
            Date currentDate = dateFormatter.parse(dateFormatter.format(new Date()));

            if (isLimited()) {
                if (!useableAfterLimit() && limitDate.after(currentDate)) {
                    hook.sendMessage(":tulip: The item is only useable until " + getLimitedDate() + ".").queue();
                    return;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (disappearAfterUsage()) {
            useAction(hook);
            CurrencyTools.removeItemFromUser(hook.getInteraction().getUser().getIdLong(), getName(), 1);
            return;
        }

        useAction(hook);
    }

    public static List<Items> getCommonItems() {
        List<Items> commonItems = new ArrayList<Items>();

        for (Items item : allItems) {
            if (!item.getEventType().equals(EventType.NONE)) {
                continue;
            }
            commonItems.add(item);
        }

        return commonItems;
    }

    public static List<Items> getEventItems() {
        List<Items> eventItems = new ArrayList<Items>();

        for (Items item : allItems) {
            if (item.getEventType().equals(EventType.NONE)) {
                continue;
            }
            eventItems.add(item);
        }

        return eventItems;
    }

    public static List<Items> getItemsByType(ItemType itemType) {
        List<Items> itemsByType = new ArrayList<Items>();

        for (Items item : allItems) {
            if (!item.getType().equals(itemType)) {
                continue;
            }
            itemsByType.add(item);
        }

        return itemsByType;
    }

    public static List<Items> getItemsByName(String itemName) {
        List<Items> itemsByName = new ArrayList<Items>();

        for (Items item : allItems) {
            if (!item.getName().equals(itemName)) {
                continue;
            }
            itemsByName.add(item);
        }

        return itemsByName;
    }

    public static List<Items> getItemsById(String itemId) {
        List<Items> itemsById = new ArrayList<Items>();

        for (Items item : allItems) {
            if (!item.getId().equals(itemId)) {
                continue;
            }
            itemsById.add(item);
        }

        return itemsById;
    }

    public static List<Items> getEventItemsByType(EventType eventType) {
        List<Items> eventItemsByType = new ArrayList<Items>();

        for (Items item : allItems) {
            if (!item.getEventType().equals(eventType)) {
                continue;
            }
            eventItemsByType.add(item);
        }

        return eventItemsByType;
    }

    public static void addItemsToList() {
        allItems.add(new FishingRod());
        allItems.add(new Notebook());
        allItems.add(new Basket());
        allItems.add(new Pickaxe());
        allItems.add(new CoinSprikler());
        allItems.add(new LotteryTicket());

        allItems.add(new Lemon());
        allItems.add(new HoneyPot());
        allItems.add(new Cookie());
        allItems.add(new Donut());
        allItems.add(new LenSushi());

        allItems.add(new CommonChest());
        allItems.add(new RareChest());
        allItems.add(new LegendaryChest());
        
        allItems.add(new Fish());
        allItems.add(new Duck());
        allItems.add(new TropicalFish());
        allItems.add(new Whale());
        allItems.add(new Strawberry());
        allItems.add(new Blueberry());
        allItems.add(new Grapes());
        allItems.add(new SmallFossil());
        allItems.add(new LargeFossil());
        allItems.add(new GiganticFish());
        allItems.add(new Junk());

        allItems.add(new BankNote());
        allItems.add(new Coupon());
        allItems.add(new Sticker());
        
        log.info("Added all the items to the list.");
    }

    public abstract void useAction(InteractionHook hook);

    public static class FishingRod extends Items {

        public FishingRod() {
            name = "fishing rod";
            desc = "Catch fish and different type of collectables.";
            emoji = ":fishing_pole_and_fish:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = false;
            isBreakable = true;
            itemLimitDate = null;
            buyingPrice = 25000;
            sellingPrice = 2500;
            category = ItemCategory.COMMON;
            type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Notebook extends Items {

        public Notebook() {
            name = "notebook";
            desc = "Use this item to study!";
            emoji = ":notebook:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = false;
            isBreakable = false;
            itemLimitDate = null;
            buyingPrice = 15000;
            sellingPrice = 2000;
            category = ItemCategory.COMMON;
            type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Basket extends Items {

        public Basket() {
            name = "basket";
            desc = "Pick some fruits!";
            emoji = ":basket:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            isUsable = true;
            useableAfterLimit = false;
            disappearAfterUsage = false;
            isBreakable = true;
            itemLimitDate = null;
            buyingPrice = 15000;   
            sellingPrice = 2000;
            category = ItemCategory.COMMON;
            type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Pickaxe extends Items {

        public Pickaxe() {
            name = "pickaxe";
            desc = "Find some fossils, or more!";
            emoji = ":pick:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = false;
            isBreakable = true;
            itemLimitDate = null;
            buyingPrice = 25000;
            sellingPrice = 2500;
            category = ItemCategory.COMMON;
            type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class CoinSprikler extends Items {

        public CoinSprikler() {
            name = "coin sprinkler";
            desc = "Give you and 5 other random members a random amount of coins.";
            emoji = ":coin:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = true;
            itemLimitDate = null;
            buyingPrice = 10000;
            sellingPrice = 4500;
            category = ItemCategory.COMMON;
            type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class LotteryTicket extends Items {

        public LotteryTicket() {
            name = "lottery ticket";
            desc = "Get a chance of winning the lottery ticket!";
            emoji = ":ticket:";
            isSellable = false;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = true;
            itemLimitDate = null;
            buyingPrice = 10000;
            sellingPrice = 4500;
            category = ItemCategory.COMMON;
            type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    public static class Lemon extends Items {

        public Lemon() {
            name = "lemon";
            desc = "Lemon, *nom nom* **AAAAAAAAAAAA**! This one extremely cutie <3!";
            emoji = ":lemon:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 50000;
            category = ItemCategory.COMMON;
            type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class HoneyPot extends Items {

        public HoneyPot() {
            name = "honey pot";
            desc = "Uh- don't put your hand inside a honey pot.";
            emoji = ":honey_pot:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 100000;
            category = ItemCategory.COMMON;
            type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Cookie extends Items {

        public Cookie() {
            name = "cookie";
            desc = "Share some cookies with some friends!";
            emoji = ":cookie:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 2000;
            sellingPrice = 500;
            category = ItemCategory.COMMON;
            type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Donut extends Items {

        public Donut() {
            name = "donut";
            desc = "Share or eat this delicious chocolate donut. Remember, sharing is caring!";
            emoji = ":doughnut:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 2000;
            sellingPrice = 500;
            category = ItemCategory.COMMON;
            type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class LenSushi extends Items {

        public LenSushi() {
            name = "len sushi";
            desc = "This sushi smells fishy... is it made properly??";
            emoji = ":sushi:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 2000;
            sellingPrice = 500;
            category = ItemCategory.COMMON;
            type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    public static class CommonChest extends Items {

        public CommonChest() {
            name = "common chest";
            desc = "Wowza! You got a common loot chest.";
            emoji = ":package:";
            isSellable = false;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = true;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 0;
            category = ItemCategory.COMMON;
            type = ItemType.LOOT_CHESTS;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class RareChest extends Items {

        public RareChest() {
            name = "rare chest";
            desc = "Wowza! You got a rare loot chest.";
            emoji = ":diamond_shape_with_a_dot_inside: :package:";
            isSellable = false;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = true;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 0;
            category = ItemCategory.COMMON;
            type = ItemType.LOOT_CHESTS;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class LegendaryChest extends Items {

        public LegendaryChest() {
            name = "legendary chest";
            desc = "Wowza! You got a legendary loot chest.";
            emoji = ":trident: :package:";
            isSellable = false;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = true;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 0;
            category = ItemCategory.COMMON;
            type = ItemType.LOOT_CHESTS;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    public static class Fish extends Items {

        public Fish() {
            name = "fish";
            desc = "A common fish!";
            emoji = ":fish:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 750;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Duck extends Items {

        public Duck() {
            name = "duck";
            desc = "Quack quack.";
            emoji = ":duck:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 1750;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class TropicalFish extends Items {

        public TropicalFish() {
            name = "tropical fish";
            desc = "Fish! But... pretty!";
            emoji = ":tropical_fish:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 5000;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Whale extends Items {

        public Whale() {
            name = "whale";
            desc = "WOAAAH BIG FISHY";
            emoji = ":whale:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 20000;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Strawberry extends Items {

        public Strawberry() {
            name = "strawberry";
            desc = "Sweet and juicy one! Perfect for jam.";
            emoji = ":strawberry:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 750;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Blueberry extends Items {

        public Blueberry() {
            name = "blueberry";
            desc = "Did you know that blueberries are yum?";
            emoji = ":blueberries:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 1750;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Grapes extends Items {

        public Grapes() {
            name = "grapes";
            desc = "Grapes are Honey's favorite.";
            emoji = ":grapes:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 5000;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class SmallFossil extends Items {

        public SmallFossil() {
            name = "small fossil";
            desc = "small one, but hey that was lucky!";
            emoji = ":shell:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 1750;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class LargeFossil extends Items {

        public LargeFossil() {
            name = "large fossil";
            desc = "BIG FOSSIL WOWIE";
            emoji = ":diamond_shape_with_a_dot_inside: :shell:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 5000;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class GiganticFish extends Items {

        public GiganticFish() {
            name = "gigantic fossil";
            desc = "WHAT HOW?? UH OK?";
            emoji = ":trident: :shell:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 20000;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Junk extends Items {

        public Junk() {
            name = "junk";
            desc = "Bad luck :(";
            emoji = ":rock:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 200;
            category = ItemCategory.COMMON;
            type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    public static class BankNote extends Items {

        public BankNote() {
            name = "bank note";
            desc = "Ooh! Yippe! Use it to get some extra money.";
            emoji = ":ticket:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = true;
            disappearAfterUsage = true;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 4000;
            category = ItemCategory.COMMON;
            type = ItemType.POWER_UP;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Coupon extends Items {

        public Coupon() {
            name = "coupon";
            desc = "Set a 5% discount on any item! (Prompted when you are going to buy an item).";
            emoji = ":receipt:";
            isSellable = true;
            isBuyable = false;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 0;
            sellingPrice = 5000;
            category = ItemCategory.COMMON;
            type = ItemType.POWER_UP;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Sticker extends Items {

        public Sticker() {
            name = "sticker";
            desc = "Get better chances to ace your exams! (Prompted when using your notebook).";
            emoji = ":receipt:";
            isSellable = true;
            isBuyable = true;
            isGiftable = true;
            isLimited = false;
            useableAfterLimit = false;
            isUsable = false;
            disappearAfterUsage = false;
            itemLimitDate = null;
            buyingPrice = 2000;
            sellingPrice = 1000;
            category = ItemCategory.COMMON;
            type = ItemType.POWER_UP;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
}