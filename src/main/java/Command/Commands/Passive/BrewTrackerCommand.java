package Command.Commands.Passive;

import Command.Structure.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Track alcohol consumption with cool emotes
 */
public class BrewTrackerCommand extends OnReadyDiscordCommand {
    private final HashMap<Long, BrewsMessage> brewMessages; // Channel ID -> Brews message

    public BrewTrackerCommand() {
        super("brew tracker!", "A tracker for alcoholism!");
        this.brewMessages = new HashMap<>();
    }

    /**
     * Begin tracking brews, notify of incorrect trigger
     *
     * @param context Context of command
     */
    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        if(!context.getLowerCaseMessage().equalsIgnoreCase(getTrigger())) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        BrewsMessage brewsMessage = brewMessages.get(channel.getIdLong());

        if(brewsMessage == null) {
            brewMessages.put(
                    channel.getIdLong(),
                    new BrewsMessage(channel, context.getEmoteHelper(), context.getGuild())
            );
            return;
        }
        brewsMessage.relocate();
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        jda.addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                BrewsMessage brewsMessage = brewMessages.get(event.getMessageChannel().getIdLong());
                if(brewsMessage == null || event.getMessageIdLong() != brewsMessage.getId()) {
                    return;
                }
                brewsMessage.buttonPressed(event);
            }
        });
    }

    @Override
    public boolean matches(String query, Message message) {
        return super.matches(query, message) || query.startsWith(getTrigger().split(" ")[0]);
    }

    /**
     * Embedded message for tracking brews with cool emotes
     */
    private static class BrewsMessage {
        private final MessageChannel channel;
        private long id;
        private final LinkedHashMap<Long, Alcoholic> alcoholics = new LinkedHashMap<>(); // User ID -> alcoholic
        private final Emote emptyBeer;
        private final Guild guild;
        private final Button increment, decrement;

        /**
         * Initialise the brew tracker
         *
         * @param channel     Channel to send message to
         * @param emoteHelper Emote helper
         * @param guild       Guild to find member
         */
        public BrewsMessage(MessageChannel channel, EmoteHelper emoteHelper, Guild guild) {
            this.channel = channel;
            this.guild = guild;
            this.increment = new ButtonImpl(
                    "increment",
                    "Add Beer",
                    ButtonStyle.SUCCESS,
                    false,
                    Emoji.fromEmote(emoteHelper.getAddBeer())
            );
            this.decrement = new ButtonImpl(
                    "decrement",
                    "Remove Beer",
                    ButtonStyle.DANGER,
                    false,
                    Emoji.fromEmote(emoteHelper.getSubtractBeer())
            );
            this.emptyBeer = emoteHelper.getEmptyBeer();
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
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Brew Tracker!")
                    .setDescription("Use the emotes to keep track of your alcoholism!")
                    .setThumbnail("https://i.imgur.com/0lQ4Cxh.png")
                    .setImage(EmbedHelper.SPACER_IMAGE)
                    .setColor(EmbedHelper.PURPLE)
                    .setFooter(
                            "Take the quiz: https://www.alcohol.org.nz/quiz",
                            "https://i.imgur.com/uogtXCW.png"
                    );
            int total = 0;
            int index = 0;
            if(!alcoholics.isEmpty()) {
                Alcoholic winner = getWinner();
                for(Alcoholic alcoholic : alcoholics.values()) {
                    builder.addField(
                            alcoholic.getName(guild),
                            getBrews(alcoholic.getBrews(), winner.getBrews()),
                            true
                    );
                    total += alcoholic.getBrews();

                    // Embed wraps after 3 inline fields, add a blank field after the first on each line to have only 2 on each
                    if(((index + 2) % 2 == 0)) {
                        builder.addBlankField(true);
                    }
                    index++;
                }
                builder.addBlankField(false)
                        .addField("Total", getBrewSummary(total), true)
                        .addField(
                                "Winner? - " + winner.getName(guild),
                                getBrewSummary(winner.getBrews()),
                                true
                        );
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
            for(int i = 0; i < max; i++) {
                if(i < count) {
                    brews.append(beer);
                    continue;
                }
                brews.append(emptyBeer);
            }
            return brews.toString();
        }

        /**
         * Send the brew tracker message with a callback to remember the id of the message
         *
         * @param message Brew tracker message
         */
        private void sendMessage(MessageEmbed message) {
            channel.sendMessage(message).setActionRows(ActionRow.of(increment, decrement)).queue(response -> id = response.getIdLong());
        }

        /**
         * Called when a button is clicked, either delete and resend the brew tracker message if it is not
         * the most recent message in the channel, or edit the existing message
         *
         * @param event Button click event to acknowledge
         */
        private void updateMessage(ButtonClickEvent event) {
            if(channel.getLatestMessageIdLong() == id) {
                event.deferEdit().setEmbeds(getEmbed()).queue();
            }
            else {
                relocate(event);
            }
        }

        /**
         * Delete the current brew tracker message and resend as to make it the most recent message in the channel
         *
         * @param event Optional button click event to acknowledge
         */
        public void relocate(ButtonClickEvent... event) {
            if(event.length > 0) {
                event[0].deferEdit().queue();
            }
            channel.deleteMessageById(id).queue();
            sendMessage(getEmbed());
        }

        /**
         * Called when a button on the message is clicked, change the quantity of brews for
         * the member who clicked.
         *
         * @param event Button click event
         */
        public void buttonPressed(ButtonClickEvent event) {
            long userId = event.getUser().getIdLong();
            Alcoholic alcoholic = alcoholics.get(userId);

            if(alcoholic == null) {
                alcoholic = new Alcoholic(userId);
                alcoholics.put(userId, alcoholic);
            }

            String buttonId = event.getComponentId();
            if(buttonId.equals(increment.getId())) {
                alcoholic.incrementBrews();
            }
            else {
                if(alcoholic.decrementBrews()) {
                    alcoholics.remove(userId);
                }
            }
            updateMessage(event);
        }

        /**
         * Wrap user in class to track their brews
         */
        private static class Alcoholic implements Comparable<Alcoholic> {
            private final long userId;
            private int brews = 0;

            /**
             * Create a new alcoholic to track
             *
             * @param userId ID of the user to track
             */
            public Alcoholic(long userId) {
                this.userId = userId;
            }

            /**
             * Increment the user's brews if they are < 24
             */
            public void incrementBrews() {
                if(this.brews == 24) {
                    return;
                }
                this.brews++;
            }

            /**
             * Get the name of the alcoholic by attempting to locate the member via the user ID
             *
             * @param guild Guild to attempt to locate member
             * @return Name of user or "Unknown"
             */
            public String getName(Guild guild) {
                Member member = guild.getMemberById(userId);
                return member == null ? "Unknown" : member.getEffectiveName();
            }

            /**
             * Decrement the user's brews and return if they have 0 or fewer
             *
             * @return Whether user now has 0 or fewer brews
             */
            public boolean decrementBrews() {
                this.brews--;
                return this.brews <= 0;
            }

            /**
             * Get the quantity of brews
             *
             * @return Quantity of brews
             */
            public int getBrews() {
                return brews;
            }

            /**
             * Sort users in descending order brew quantity
             *
             * @param o user to compare to current
             * @return Sort value
             */
            @Override
            public int compareTo(@NotNull BrewTrackerCommand.BrewsMessage.Alcoholic o) {
                return o.getBrews() - this.getBrews();
            }
        }
    }
}
