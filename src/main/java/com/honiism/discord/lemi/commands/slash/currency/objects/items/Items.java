/*
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

package com.honiism.discord.lemi.commands.slash.currency.objects.items;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.honiism.discord.lemi.commands.slash.currency.objects.items.handler.EventType;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.handler.ItemCategory;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.handler.ItemInterface;
import com.honiism.discord.lemi.commands.slash.currency.objects.items.handler.ItemType;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.interactions.InteractionHook;

public abstract class Items implements ItemInterface {

    private static final Logger log = LoggerFactory.getLogger(Items.class);
    public static List<Items> allItems = new ArrayList<Items>();

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
    String currentDateString = dateFormatter.format(new Date());

    public static class FishingRod extends Items {

        public FishingRod() {
            this.name = "fishing rod";
            this.desc = "Catch fish and different type of collectables.";
            this.emoji = ":fishing_pole_and_fish:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = false;
            this.isBreakable = true;
            this.itemLimitDate = null;
            this.buyingPrice = 25000;
            this.sellingPrice = 2500;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Notebook extends Items {

        public Notebook() {
            this.name = "notebook";
            this.desc = "Use this item to study!";
            this.emoji = ":notebook:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = false;
            this.isBreakable = false;
            this.itemLimitDate = null;
            this.buyingPrice = 15000;
            this.sellingPrice = 2000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Basket extends Items {

        public Basket() {
            this.name = "basket";
            this.desc = "Pick some fruits!";
            this.emoji = ":basket:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.isUsable = true;
            this.useableAfterLimit = false;
            this.disappearAfterUsage = false;
            this.isBreakable = true;
            this.itemLimitDate = null;
            this.buyingPrice = 15000;   
            this.sellingPrice = 2000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Pickaxe extends Items {

        public Pickaxe() {
            this.name = "pickaxe";
            this.desc = "Find some fossils, or more!";
            this.emoji = ":pick:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = false;
            this.isBreakable = true;
            this.itemLimitDate = null;
            this.buyingPrice = 25000;
            this.sellingPrice = 2500;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class CoinSprikler extends Items {

        public CoinSprikler() {
            this.name = "coin sprinkler";
            this.desc = "Give you and 5 other random members a random amount of coins.";
            this.emoji = ":coin:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = true;
            this.itemLimitDate = null;
            this.buyingPrice = 10000;
            this.sellingPrice = 4500;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class LotteryTicket extends Items {

        public LotteryTicket() {
            this.name = "lottery ticket";
            this.desc = "Get a chance of winning the lottery ticket!";
            this.emoji = ":ticket:";
            this.isSellable = false;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = true;
            this.itemLimitDate = null;
            this.buyingPrice = 10000;
            this.sellingPrice = 4500;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.TOOL;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    public static class Lemon extends Items {

        public Lemon() {
            this.name = "lemon";
            this.desc = "Lemon, *nom nom* **AAAAAAAAAAAA**! This one extremely cutie <3!";
            this.emoji = ":lemon:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 50000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class HoneyPot extends Items {

        public HoneyPot() {
            this.name = "honey pot";
            this.desc = "Uh- don't put your hand inside a honey pot.";
            this.emoji = ":honey_pot:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 100000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Cookie extends Items {

        public Cookie() {
            this.name = "cookie";
            this.desc = "Share some cookies with some friends!";
            this.emoji = ":cookie:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 2000;
            this.sellingPrice = 500;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Donut extends Items {

        public Donut() {
            this.name = "donut";
            this.desc = "Share or eat this delicious chocolate donut. Remember, sharing is caring!";
            this.emoji = ":doughnut:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 2000;
            this.sellingPrice = 500;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class LenSushi extends Items {

        public LenSushi() {
            this.name = "len sushi";
            this.desc = "This sushi smells fishy... is it made properly??";
            this.emoji = ":sushi:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 2000;
            this.sellingPrice = 500;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.COLLECTABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    public static class CommonChest extends Items {

        public CommonChest() {
            this.name = "common chest";
            this.desc = "Wowza! You got a common loot chest.";
            this.emoji = ":package:";
            this.isSellable = false;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = true;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 0;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.LOOT_CHESTS;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class RareChest extends Items {

        public RareChest() {
            this.name = "rare chest";
            this.desc = "Wowza! You got a rare loot chest.";
            this.emoji = ":diamond_shape_with_a_dot_inside: :package:";
            this.isSellable = false;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = true;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 0;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.LOOT_CHESTS;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class LegendaryChest extends Items {

        public LegendaryChest() {
            this.name = "legendary chest";
            this.desc = "Wowza! You got a legendary loot chest.";
            this.emoji = ":trident: :package:";
            this.isSellable = false;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = true;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 0;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.LOOT_CHESTS;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    public static class Fish extends Items {

        public Fish() {
            this.name = "fish";
            this.desc = "A common fish!";
            this.emoji = ":fish:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 750;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Duck extends Items {

        public Duck() {
            this.name = "duck";
            this.desc = "Quack quack.";
            this.emoji = ":duck:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 1750;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class TropicalFish extends Items {

        public TropicalFish() {
            this.name = "tropical fish";
            this.desc = "Fish! But... pretty!";
            this.emoji = ":tropical_fish:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 5000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Whale extends Items {

        public Whale() {
            this.name = "whale";
            this.desc = "WOAAAH BIG FISHY";
            this.emoji = ":whale:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 20000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Strawberry extends Items {

        public Strawberry() {
            this.name = "strawberry";
            this.desc = "Sweet and juicy one! Perfect for jam.";
            this.emoji = ":strawberry:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 750;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Blueberry extends Items {

        public Blueberry() {
            this.name = "blueberry";
            this.desc = "Did you know that blueberries are yum?";
            this.emoji = ":blueberries:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 1750;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Grapes extends Items {

        public Grapes() {
            this.name = "grapes";
            this.desc = "Grapes are Honey's favorite.";
            this.emoji = ":grapes:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 5000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class SmallFossil extends Items {

        public SmallFossil() {
            this.name = "small fossil";
            this.desc = "small one, but hey that was lucky!";
            this.emoji = ":shell:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 1750;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class LargeFossil extends Items {

        public LargeFossil() {
            this.name = "large fossil";
            this.desc = "BIG FOSSIL WOWIE";
            this.emoji = ":diamond_shape_with_a_dot_inside: :shell:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 5000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class GiganticFish extends Items {

        public GiganticFish() {
            this.name = "gigantic fossil";
            this.desc = "WHAT HOW?? UH OK?";
            this.emoji = ":trident: :shell:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 20000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Junk extends Items {

        public Junk() {
            this.name = "junk";
            this.desc = "Bad luck :(";
            this.emoji = ":rock:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 200;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.SELLABLE;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    public static class BankNote extends Items {

        public BankNote() {
            this.name = "bank note";
            this.desc = "Ooh! Yippe! Use it to get some extra money.";
            this.emoji = ":ticket:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = true;
            this.disappearAfterUsage = true;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 4000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.POWER_UP;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Coupon extends Items {

        public Coupon() {
            this.name = "coupon";
            this.desc = "Set a 5% discount on any item! (Prompted when you are going to buy an item).";
            this.emoji = ":receipt:";
            this.isSellable = true;
            this.isBuyable = false;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 0;
            this.sellingPrice = 5000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.POWER_UP;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }
    public static class Sticker extends Items {

        public Sticker() {
            this.name = "sticker";
            this.desc = "Get better chances to ace your exams! (Prompted when using your notebook).";
            this.emoji = ":receipt:";
            this.isSellable = true;
            this.isBuyable = true;
            this.isGiftable = true;
            this.isLimited = false;
            this.useableAfterLimit = false;
            this.isUsable = false;
            this.disappearAfterUsage = false;
            this.itemLimitDate = null;
            this.buyingPrice = 2000;
            this.sellingPrice = 1000;
            this.category = ItemCategory.COMMON;
            this.type = ItemType.POWER_UP;
        }

        @Override
        public void useAction(InteractionHook hook) {
            
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.desc;
    }

    @Override
    public String getId() {
        if (getName().contains(" ")) {
            return this.name.replaceAll(" ", "_");
        } else {
            return this.name;
        }
    }

    @Override
    public String getEmoji() {
        return this.emoji;
    }

    @Override
    public boolean isSellable() {
        return this.isSellable;
    }

    @Override
    public boolean isBuyable() {
        return this.isBuyable;
    }

    @Override
    public boolean isGiftAble() {
        return this.isGiftable;
    }

    @Override
    public boolean isLimited() {
        return this.isLimited;
    }

    @Override
    public boolean isUsable() {
        return this.isUsable;
    }

    @Override
    public boolean useableAfterLimit() {
        return this.useableAfterLimit;
    }

    @Override
    public String getLimitedDate() {
        return this.itemLimitDate;
    }

    @Override
    public long getBuyingPrice() {
        return this.buyingPrice;
    }

    @Override
    public long getSellingPrice() {
        return this.sellingPrice;
    }

    @Override
    public boolean disappearAfterUsage() {
        return this.disappearAfterUsage;
    }

    @Override
    public ItemCategory getCategory() {
        return this.category;
    }

    @Override
    public ItemType getType() {
        return this.type;
    }

    @Override
    public EventType getEventType() {
        return this.eventType;
    }

    @Override
    public void useItem(InteractionHook hook) {
        if (!isUsable()) {
            return;
        }

        try {
            Date limitDate = dateFormatter.parse(getLimitedDate());
            Date currentDate = dateFormatter.parse(currentDateString);

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
            CurrencyTools.removeItemFromUser(String.valueOf(hook.getInteraction().getUser().getIdLong()), getName(), 1);
            return;
        }

        useAction(hook);
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

        CurrencyTools.createInvDb();
    }

    public abstract void useAction(InteractionHook hook);
}