package com.honiism.discord.lemi.commands.slash.staff.admins;

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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class UserBan extends SlashCmd {

    private  HashMap<Long, Long> delay = new HashMap<>();
    private long timeDelayed;

    public UserBan() {
        this.name = "userban";
        this.desc = "Bans a user from using Lemi bot.";
        this.usage = "/userban ((subcommand))";
        this.category = CommandCategory.ADMINS;
        this.userCategory = UserCategory.ADMINS;
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.botPermissions = new Permission[] {Permission.ADMINISTRATOR};
        this.subCmds = Arrays.asList(new SubcommandData("help", "View the help guide for this command."),

                                     new SubcommandData("add", "Ban a user from using Lemi.")
                                         .addOption(OptionType.USER, "user", "The @user/id you want to ban.", true)
                                         .addOption(OptionType.STRING, "reason", "The reason why they're getting banned.", true),

                                     new SubcommandData("remove", "Unban a previously banned user.")
                                         .addOption(OptionType.USER, "user", "The @user/id you want to unban.", true),

                                     new SubcommandData("view", "View all details from the ban list.")
                                    );
    }

    @Override
    public void action(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        User author = hook.getInteraction().getUser();

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
                    String reason = event.getOption("reason").getAsString();
                    
                    if (memberToAdd == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.addUserId(memberToAdd, reason, event);
                    break;

                case "remove":
                    Member memberToRemove = event.getOption("user").getAsMember();

                    if (memberToRemove == null) {
                        hook.sendMessage(":grapes: That user doesn't exist in the guild.").queue();
                        return;
                    }

                    LemiDbManager.INS.removeUserId(memberToRemove, event);
                    break;

                case "view":
                    viewAllBans(event);
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

    private void viewAllBans(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        List<String> banDetails = new ArrayList<>();
        List<String> authorIds = LemiDbManager.INS.getAuthorIds(event);
        List<String> bannedUserIds = LemiDbManager.INS.getUserIds(event);
        List<String> reasons = LemiDbManager.INS.getReasons(event);

        for (int i = 0; i < bannedUserIds.size(); i++) {
            banDetails.add("Admin : <@" + authorIds.get(i) + ">" 
                    + " | Banned user : <@" + bannedUserIds.get(i)
                    + " | Reason : `" + reasons.get(i) + "`");
        }

        if (Tools.isEmpty(banDetails)) {
            hook.editOriginal(":fish_cake: There's no banned users.").queue();
            return;
        }

        Paginator.Builder builder = new Paginator.Builder(event.getJDA())
            .setEmbedDesc("‧₊੭ :bread: **BANNED LIST!** ♡ ⋆｡˚")
            .setEventWaiter(Lemi.getInstance().getEventWaiter())
            .setItemsPerPage(10)
            .setItems(banDetails)
            .useNumberedItems(true)
            .useTimestamp(true)
            .addAllowedUsers(event.getUser().getIdLong())
            .setColor(0xffd1dc)
            .setTimeout(1, TimeUnit.MINUTES);

        int page = 1;

        hook.sendMessageEmbeds(EmbedUtils.getSimpleEmbed(":tea: Loading..."))
            .queue(message -> builder.build().paginate(message, page));
    }
}