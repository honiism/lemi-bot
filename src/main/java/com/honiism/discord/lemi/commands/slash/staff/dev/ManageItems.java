package com.honiism.discord.lemi.commands.slash.staff.dev;

import java.util.Arrays;
import java.util.HashMap;

import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.utils.currency.CurrencyTools;
import com.honiism.discord.lemi.utils.misc.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ManageItems extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ManageItems() {
        this.name = "manageitems";
        this.desc = "Add/remove items to/from the database.";
        this.usage = "/manageitems ((subcommand))";
        this.category = CommandCategory.DEV;
        this.userCategory = UserCategory.DEV;
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),

                                     new SubcommandData("add", "Add a new item to the database.")
                                         .addOption(OptionType.STRING, "name", "The name of the item to add.", true),

                                     new SubcommandData("remove", "Remove an existing item from the database.")
                                         .addOption(OptionType.STRING, "name", "The name of the item to remove.", true)
                                    );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();
        
        if (delay.containsKey(author.getIdLong())) {
            timeDelayed = System.currentTimeMillis() - delay.get(author.getIdLong());
        } else {
            timeDelayed = (10 * 1000);
        }
            
        if (timeDelayed >= (10 * 1000)) {        
            if (delay.containsKey(author.getIdLong())) {
                delay.remove(author.getIdLong());
            }
        
            delay.put(author.getIdLong(), System.currentTimeMillis());

            String subCmdName = event.getSubcommandName();
            String itemName = event.getOption("name").getAsString();

            switch (subCmdName) {
                case "help":
                    hook.sendMessageEmbeds(this.getHelp(event)).queue();
                    break;

                case "add":
                    if (CurrencyTools.checkIfItemExists(itemName)) {
                        hook.sendMessage(":cherries: This item already exists in the database.").queue();
                        return;
                    }

                    if (CurrencyTools.getItemsByName(itemName).get(0) == null) {
                        hook.sendMessage(":sunflower: You haven't added this item in the internal list manually.").queue();
                        return;
                    }

                    CurrencyTools.addNewItemToDb(itemName.replaceAll(" ", "_"), hook);
                    break;

                case "remove":
                    if (!CurrencyTools.checkIfItemExists(itemName)) {
                        hook.sendMessage(":snowflake: This item doesn't exist in the database.").queue();
                        return;
                    }

                    if (CurrencyTools.getItemsByName(itemName).get(0) != null) {
                        hook.sendMessage(":tulip: You haven't removed this item in the internal list manually.").queue();
                        return;
                    }

                    CurrencyTools.removeItemFromDb(itemName.replaceAll(" ", "_"), hook);
            }
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }
}