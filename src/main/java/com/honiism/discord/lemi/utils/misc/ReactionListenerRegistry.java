package com.honiism.discord.lemi.utils.misc;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.MiscUtil;

public class ReactionListenerRegistry {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5, Tools.newThreadFactory("reaction-listener"));
    private final TLongObjectMap<ReactionListener> listeners = MiscUtil.newLongMap();

    boolean hasListener(long messageId) {
        return this.listeners.containsKey(messageId);
    }

    void newListener(Message message, List<String> allowedReactions, Set<User> allowedUsers,
                     int timeout, TimeUnit timeUnit, Consumer<Integer> callback) {
        new ReactionListener(message, allowedReactions, allowedUsers, timeout, timeUnit, callback);
    }

    private void register(long messageId, ReactionListener listener) {
        this.listeners.put(messageId, listener);
    }

    private void remove(long messageId) {
        this.listeners.remove(messageId);
    }

    void handle(final MessageReactionAddEvent event) {
        ReactionListener[] safeValues = this.listeners.values(new ReactionListener[this.listeners.size()]);

        for (final ReactionListener listener : safeValues) {
            listener.handle(event);
        }
    }

    void cancel(long messageId) {
        cancel(messageId, false);
    }

    void cancel(long messageId, boolean removeReactions) {
        ReactionListener reactionListener = this.listeners.get(messageId);
        
        if (reactionListener != null) {
            reactionListener.stop(removeReactions);
        }
    }

    private class ReactionListener {
            
        private final Message message;
        private final List<String> allowedReactions;
        private final Set<User> allowedUsers;
        private final Consumer<Integer> callback;
        private final ScheduledFuture<?> timeoutFuture;

        private boolean shouldDeleteReactions = true;

        private ReactionListener(Message message, List<String> allowedReactions, Set<User> allowedUsers,
                int timeout, TimeUnit timeUnit, Consumer<Integer> callback) {
            ReactionListenerRegistry.this.register(message.getIdLong(), this);
            this.message = message;
            this.allowedReactions = allowedReactions;
            this.allowedUsers = allowedUsers;
            this.callback = callback;
            this.timeoutFuture = ReactionListenerRegistry.EXECUTOR.schedule(this::cleanup, timeout, timeUnit);
            addReactions();
        }

        private void handle(MessageReactionAddEvent event) {
            if (event.getMessageIdLong() != message.getIdLong()) {
                return;
            }
            
            if (event.getUser() == event.getJDA().getSelfUser()) {
                return;
            }

            try {
                event.getReaction().removeReaction(event.getUser()).queue(null, ErrorResponseException.ignore(ErrorResponse.UNKNOWN_MESSAGE));
            } catch (PermissionException ignored) {}

            if (!allowedUsers.isEmpty() && !allowedUsers.contains(event.getUser())) {
                return;
            }

            MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();
            String reaction = reactionEmote.isEmote() ? String.valueOf(reactionEmote.getEmote().getIdLong()) : reactionEmote.getName();

            if (allowedReactions.contains(reaction)) {
                callback.accept(allowedReactions.indexOf(reaction));
            }
        }

        private void addReactions() {
            if (message.getChannelType() == ChannelType.TEXT 
                    && !message.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)) {
                return;
            }

            for (String reaction : allowedReactions) {
                Emote emote = null;
                try {
                    emote = message.getJDA().getEmoteById(reaction);
                } catch (NumberFormatException ignored) {}
                if (emote == null) {
                    message.addReaction(reaction).queue();
                } else {
                    message.addReaction(emote).queue();
                }
            }
        }

        private void stop(boolean removeReactions) {
            this.shouldDeleteReactions = removeReactions;
            this.timeoutFuture.cancel(true);
            cleanup();
        }

        private void cleanup() {
            ReactionListenerRegistry.this.remove(message.getIdLong());
            if (shouldDeleteReactions) {
                try {
                    message.clearReactions().queue();
                } catch (PermissionException ignored) {}
            }
        }
    }
}