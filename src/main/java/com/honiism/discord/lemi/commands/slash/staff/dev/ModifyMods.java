package com.honiism.discord.lemi.commands.slash.staff.dev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.honiism.discord.lemi.Lemi;
import com.honiism.discord.lemi.commands.slash.handler.CommandCategory;
import com.honiism.discord.lemi.commands.slash.handler.SlashCmd;
import com.honiism.discord.lemi.commands.slash.handler.UserCategory;
import com.honiism.discord.lemi.database.managers.LemiDbManager;
import com.honiism.discord.lemi.utils.misc.EmbedUtils;
import com.honiism.discord.lemi.utils.misc.Tools;
import com.honiism.discord.lemi.utils.paginator.Paginator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ModifyMods extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public ModifyMods() {
        this.name = "modifymods";
        this.desc = "Add/remove/view user(s) to/from the moderator database.";
        this.usage = "/modifymods ((subcommand))";
        this.category = CommandCategory.DEV;
        this.userCategory = UserCategory.DEV;
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),

                                     new SubcommandData("add", "Add a user to the official moderator list.")
                                         .addOption(OptionType.USER, "user", "The @user/id you want to add.", true)
                                         .addOption(OptionType.STRING, "key", "The key that will be assigned for this user.", true),

                                     new SubcommandData("remove", "Remove a user from the official moderator list.")
                                         .addOption(OptionType.USER, "user", "The @user/id you want to remove.", true),

                                     new SubcommandData("view", "View all details from the official moderator list.")
                                    );
    }
    
    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = event.getUser();
        Guild guild = event.getGuild();
        
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

            switch (subCmdName) {
                case "help":
                    hook.sendMessageEmbeds(this.getHelp(event)).queue();
                    break;

                case "add":
                    Member memberToAdd = event.getOption("user").getAsMember();
                    String keyToAdd = event.getOption("key").getAsString();
                    
                    if (memberToAdd == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.addModId(guild, memberToAdd, keyToAdd, event);
                    break;

                case "remove":
                    User userToRemove = event.getOption("user").getAsUser();
                    LemiDbManager.INS.removeModId(guild, userToRemove, event);
                    break;

                case "view":
                    viewAllIds(event);
                    break;
            }
        } else {
            String time = Tools.secondsToTime(((10 * 1000) - timeDelayed) / 1000);
                
            EmbedBuilder cooldownMsgEmbed = new EmbedBuilder()
                .setDescription("‧₊੭ :cherries: CHILL! ♡ ⋆｡˚\r\n" 
                        + "˚⊹ ˚︶︶꒷︶꒷꒦︶︶꒷꒦︶ ₊˚⊹.\r\n"
                        + author.getAsMention() 
                        + ", you can use this command again in `" + time + "`.")
                .setColor(0xffd1dc);
                
            hook.sendMessageEmbeds(cooldownMsgEmbed.build()).queue();
        }
    }

    private void viewAllIds(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> modDetails = new ArrayList<>();
        List<String> modIds = LemiDbManager.INS.getModIds(event);
        List<String> modKeys = LemiDbManager.INS.getModKeys(event);

        for (int i = 0; i < modIds.size(); i++) {
            modDetails.add("<@" + modIds.get(i) + "> `" 
                    + modIds.get(i) + " | key :` ||" 
                    + modKeys.get(i) + "||");
        }

        if (Tools.isEmpty(modDetails)) {
            hook.editOriginal(":fish_cake: There's no mods.").queue();
            return;
        }

        Paginator.Builder builder = new Paginator.Builder(event.getJDA())
            .setEmbedDesc("‧₊੭ :bread: **MODS!** ♡ ⋆｡˚")
            .setEventWaiter(Lemi.getInstance().getEventWaiter())
            .setItemsPerPage(10)
            .setItems(modDetails)
            .useNumberedItems(true)
            .useTimestamp(true)
            .addAllowedUsers(event.getUser().getIdLong())
            .setColor(0xffd1dc)
            .setTimeout(1, TimeUnit.MINUTES);

        int page = 1;

        event.getUser().openPrivateChannel().queue((msg) -> {
            msg.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
                .queue(message -> builder.build().paginate(message, page));
        });

        hook.editOriginal(":blueberries: Sent you the details.").queue();
    }
}