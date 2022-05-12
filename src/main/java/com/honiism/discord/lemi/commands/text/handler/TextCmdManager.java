package com.honiism.discord.lemi.commands.text.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.honiism.discord.lemi.commands.text.staff.dev.Compile;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TextCmdManager {
    
    private Map<String, TextCmd> commands = new HashMap<>();

    public TextCmdManager() {
        addCommand(new Compile());
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
}