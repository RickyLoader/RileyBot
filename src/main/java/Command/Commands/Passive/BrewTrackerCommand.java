package Command.Commands.Passive;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmoteListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;


public class BrewTrackerCommand extends DiscordCommand {
    private BrewsMessage brewsMessage;
    private EmoteListener listener;

    public BrewTrackerCommand() {
        super("brew tracker!", "A tracker for alcoholism!");
    }

    private EmoteListener getEmoteListener() {
        return new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long reactID = reaction.getMessageIdLong();
                if(brewsMessage != null && reactID == brewsMessage.getId()) {
                    brewsMessage.reactionAdded(reaction, user);
                }
            }
        };
    }

    private void addEmoteListener(JDA jda) {
        if(this.listener == null) {
            this.listener = getEmoteListener();
            jda.addEventListener(this.listener);
        }
    }

    private void showBrewTracker(CommandContext context) {
        if(brewsMessage == null || brewsMessage.timedOut()) {
            addEmoteListener(context.getJDA());
            brewsMessage = new BrewsMessage(context.getMessageChannel(), context.getGuild());
            return;
        }
        brewsMessage.relocate();
    }

    @Override
    public boolean matches(String query) {
        return query.contains("brew") && query.contains("!");
    }

    @Override
    public void execute(CommandContext context) {
        String trigger = context.getLowerCaseMessage();
        if(trigger.equals("brew tracker!")) {
            showBrewTracker(context);
            return;
        }
        context.getMessageChannel().sendMessage(getHelpNameCoded()).queue();
    }

    private static class BrewsMessage {
        private final MessageChannel channel;
        private long id, lastUpdate;
        private final Guild guild;
        private final LinkedHashMap<Member, Alcoholic> alcoholics = new LinkedHashMap<>();
        private final Emote increment, decrement, emptyBeer;

        public BrewsMessage(MessageChannel channel, Guild guild) {
            this.channel = channel;
            this.guild = guild;
            this.increment = guild.getEmotesByName("add_beer", true).get(0);
            this.decrement = guild.getEmotesByName("subtract_beer", true).get(0);
            this.emptyBeer = guild.getEmotesByName("empty_beer", true).get(0);
            this.lastUpdate = System.currentTimeMillis();
            sendMessage(getEmbed());
        }

        /**
         * Get the id of the brew tracker message
         *
         * @return brew tracker message id
         */
        public long getId() {
            return id;
        }

        /**
         * Time out the tracker after 2 hours
         *
         * @return Whether it has been 2 hours since the last update
         */
        public boolean timedOut() {
            return System.currentTimeMillis() - lastUpdate > 7200000;
        }

        /**
         * Get the member with the current highest brew count
         *
         * @return member with highest brew count
         */
        private Alcoholic getWinner() {
            ArrayList<Alcoholic> sorted = new ArrayList<>(alcoholics.values());
            Collections.sort(sorted);
            return sorted.get(0);
        }

        /**
         * Build the brew tracker message
         *
         * @return brew tracker message
         */
        private MessageEmbed getEmbed() {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Brew Tracker!");
            builder.setDescription("Use the emotes to keep track of your alcoholism!");
            builder.setThumbnail("https://i.imgur.com/0lQ4Cxh.png");
            builder.setImage("https://i.imgur.com/24Xf03H.png");
            builder.setColor(16711935);
            builder.setFooter("Take the quiz: https://www.alcohol.org.nz/quiz", "https://i.imgur.com/uogtXCW.png");
            int total = 0;
            int index = 0;
            if(!alcoholics.isEmpty()) {
                Alcoholic winner = getWinner();
                for(Alcoholic alcoholic : alcoholics.values()) {
                    builder.addField(alcoholic.getName(), getBrews(alcoholic.getBrews(), winner.getBrews()), true);
                    total += alcoholic.getBrews();

                    if(((index + 2) % 2 == 0)) {
                        builder.addBlankField(true);
                    }
                    index++;
                }
                builder.addBlankField(false);
                builder.addField("Total", getBrewSummary(total), true);
                builder.addField("Winner? - " + winner.getName(), getBrewSummary(winner.getBrews()), true);

            }
            return builder.build();
        }

        /**
         * Apply the appropriate suffix to a given number of brews
         *
         * @param total brews to apply suffix to
         * @return Number with " brews" or " brew" attached
         */
        private String getBrewSummary(int total) {
            String brewSuffix = (total == 1 ? " brew" : " brews");
            return total + brewSuffix;
        }

        /**
         * Build a string containing the given quantity of beer emojis and pad out the difference to the
         * highest brew count with a slightly transparent beer emoji
         *
         * @param count Number of beer emojis to use
         * @param max   Current highest brew count
         * @return String containing beer emojis
         */
        private String getBrews(int count, int max) {
            StringBuilder brews = new StringBuilder();
            String beer = "\uD83C\uDF7A ";
            String emptyBeer = "<:" + this.emptyBeer.getName() + ":" + this.emptyBeer.getId() + "> ";
            for(int i = 0; i < count; i++) {
                brews.append(beer);
            }
            for(int i = count; i < max; i++) {
                brews.append(emptyBeer);
            }
            return brews.toString();
        }

        /**
         * Send the brew tracker message with a callback
         * to apply the reaction emotes and remember the id of the message
         *
         * @param message Brew tracker message
         */
        private void sendMessage(MessageEmbed message) {
            channel.sendMessage(message).queue(response -> {
                id = response.getIdLong();
                response.addReaction(decrement).queue();
                response.addReaction(increment).queue();
            });
        }

        /**
         * Called when a reaction is received, either delete and resend the brew tracker message if it is not
         * the most recent message in the channel, or edit the existing message
         */
        private void updateMessage() {
            channel.retrieveMessageById(id).queue(message -> {
                MessageEmbed updateMessage = getEmbed();
                if(channel.getLatestMessageIdLong() == id) {
                    message.editMessage(updateMessage).queue();
                }
                else {
                    message.delete().queue();
                    sendMessage(updateMessage);
                }
            });
        }

        /**
         * Delete the current brew tracker message and resend as to make it the most recent message in the channel
         */
        public void relocate() {
            channel.retrieveMessageById(id).queue(message -> {
                message.delete().queue();
                sendMessage(getEmbed());
            });
        }

        /**
         * Called when a reaction is added to the brew tracker message, change the quantity of brews for a member
         *
         * @param reaction Reaction that was added
         * @param user     User who added the reaction
         */
        public void reactionAdded(MessageReaction reaction, User user) {
            Emote emote = reaction.getReactionEmote().getEmote();
            Member member = guild.getMember(user);
            if(emote != increment && emote != decrement) {
                return;
            }
            Alcoholic alcoholic = alcoholics.get(member);
            if(alcoholic == null) {
                alcoholic = new Alcoholic(member);
                alcoholics.put(member, alcoholic);
            }
            if(emote == increment) {
                alcoholic.incrementBrews();
            }
            else {
                if(alcoholic.decrementBrews()) {
                    alcoholics.remove(member);
                }
            }
            this.lastUpdate = System.currentTimeMillis();
            updateMessage();
        }

        private static class Alcoholic implements Comparable<Alcoholic> {
            private final Member member;
            private int brews = 0;

            public Alcoholic(Member member) {
                this.member = member;
            }

            public void incrementBrews() {
                if(this.brews == 24) {
                    return;
                }
                this.brews++;
            }

            public boolean decrementBrews() {
                if(brews == 0){
                    return true;
                }
                this.brews--;
                return this.brews == 0;
            }

            public int getBrews() {
                return brews;
            }

            public String getName() {
                return member.getNickname() == null ? member.getUser().getName() : member.getNickname();
            }

            @Override
            public int compareTo(@NotNull BrewTrackerCommand.BrewsMessage.Alcoholic o) {
                return o.getBrews() - this.getBrews();
            }
        }
    }
}
