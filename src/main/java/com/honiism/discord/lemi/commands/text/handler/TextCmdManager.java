package com.honiism.discord.lemi.commands.text.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.honiism.discord.lemi.commands.handler.CommandCategory;
import com.honiism.discord.lemi.commands.text.staff.admins.Announce;
import com.honiism.discord.lemi.commands.text.staff.admins.Embed;
import com.honiism.discord.lemi.commands.text.staff.admins.ResetCurrData;
import com.honiism.discord.lemi.commands.text.staff.admins.ShardRestart;
import com.honiism.discord.lemi.commands.text.staff.admins.UserBan;
import com.honiism.discord.lemi.commands.text.staff.dev.ClearCmds;
import com.honiism.discord.lemi.commands.text.staff.dev.Compile;
import com.honiism.discord.lemi.commands.text.staff.dev.Eval;
import com.honiism.discord.lemi.commands.text.staff.dev.ManageItems;
import com.honiism.discord.lemi.commands.text.staff.dev.ModifyAdmins;
import com.honiism.discord.lemi.commands.text.staff.dev.ModifyMods;
import com.honiism.discord.lemi.commands.text.staff.dev.ReloadSlash;
import com.honiism.discord.lemi.commands.text.staff.dev.SetDebug;
import com.honiism.discord.lemi.commands.text.staff.dev.Shutdown;
import com.honiism.discord.lemi.commands.text.staff.dev.UpsertSlash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TextCmdManager {
    
    private static final Logger log = LoggerFactory.getLogger(TextCmdManager.class);

    private Map<String, TextCmd> commands = new HashMap<>();
    private Map<CommandCategory, List<TextCmd>> cmdsByCategory = new HashMap<>();

    public void initialize() {
        setCmds();
        updateCmdCategory();
    }

    public void handle(MessageReceivedEvent event, String prefix) {
        String[] split = event.getMessage().getContentRaw()
            .replaceFirst("(?i)" + Pattern.quote(prefix), "")
            .split("\\s+");

        String invoke = split[0].toLowerCase();
        TextCmd cmd = getCommand(invoke);

        if (cmd != null) {
            event.getChannel().sendTyping().queue();
            
            List<String> args = Arrays.asList(split).subList(1, split.length);
            CommandContext ctx = new CommandContext(event, args);

            cmd.preAction(ctx);
        }
    }

    private void addCommand(TextCmd cmd) {
        if (!commands.containsKey(cmd.getName())) {
            commands.put(cmd.getName(), cmd);
        }

        if (cmd.getAliases() == null) {
            return;
        }

        for (String alias : cmd.getAliases()) {
            if (!commands.containsKey(alias)) {
                commands.put(alias, cmd);
            }
        }
    }

    private TextCmd getCommand(String input) {
        if (commands.containsKey(input.toLowerCase())) {
            return commands.get(input.toLowerCase());
        }
        return null;
    }

    private void updateCmdCategory() {
        for (CommandCategory category : CommandCategory.values()) {
            cmdsByCategory.put(category,
                    commands.values()
                        .stream()
                        .filter(cmd -> cmd.getCategory().equals(category))
                        .collect(Collectors.toList())
            );
        }

        log.info("Updated all TEXT commands according to it's categories.");
    }

    private void setCmds() {
        // dev
        addCommand(new Compile());
        addCommand(new Eval());
        addCommand(new ManageItems());
        addCommand(new ModifyAdmins());
        addCommand(new ModifyMods());
        addCommand(new SetDebug());
        addCommand(new Shutdown());
        addCommand(new ReloadSlash());
        addCommand(new UpsertSlash());
        addCommand(new ClearCmds());

        // admins
        addCommand(new Announce());
        addCommand(new Embed());
        addCommand(new ResetCurrData());
        addCommand(new ShardRestart());
        addCommand(new UserBan());
    }
}