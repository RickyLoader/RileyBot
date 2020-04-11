package Bot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Interactive win/loss tracker - use emotes to track score
 */
class Gunfight {

    // ID of the game message, used to find the message in the channel
    private long id;
    private MessageChannel channel;
    private Emote win, loss;
    private int wins, losses, streak = 0;
    private Guild server;

    // User who started game, only user allowed to register score
    private User owner;

    Gunfight(MessageChannel channel, Guild server, User owner) {
        this.channel = channel;
        this.server = server;
        this.owner = owner;
        if(checkEmotes()) {
            startGame();
        }
        else {
            channel.sendMessage("This server needs an emote named \"victory\" and an emote named \"defeat\" cunt.").queue();
        }
    }

    /**
     * Attempts to locate emotes named win & loss, required for game
     *
     * @return Presence of emotes
     */
    private boolean checkEmotes() {
        List<Emote> victory = server.getEmotesByName("victory", true);
        List<Emote> defeat = server.getEmotesByName("defeat", true);
        if(victory.size() > 0 && defeat.size() > 0) {
            this.win = victory.get(0);
            this.loss = defeat.get(0);
            return true;
        }
        return false;
    }

    /**
     * Builds the game message. Called to begin game and build new messages as score is added.
     *
     * @param wins   Wins to display
     * @param losses Losses to display
     * @param streak Current streak (negative for loss streak)
     * @param desc   Feedback message to display based on current score
     * @return Game message
     */
    private MessageEmbed buildGameMessage(int wins, int losses, String streak, String desc) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle("GUNFIGHT");
        builder.setDescription(desc);
        builder.setThumbnail("https://www.pcgamesn.com/wp-content/uploads/2019/05/modern-warfare-captain-price-900x507.jpg");
        builder.addField("**WIN**", String.valueOf(wins), true);
        builder.addBlankField(true);
        builder.addField("**LOSS**", String.valueOf(losses), true);
        builder.addField("**STREAK**", streak, false);
        return builder.build();
    }

    /**
     * Send the game message to the channel to begin playing
     */
    private void startGame() {
        MessageEmbed game = buildGameMessage(wins, losses, String.valueOf(streak), createDesc());
        sendGameMessage(game);
    }

    /**
     * Check if reaction is a win or loss & update score appropriately
     *
     * @param reaction Emote reaction on game message (may be invalid)
     */
    void reactionAdded(MessageReaction reaction) {
        Emote emote = server.getEmotesByName(reaction.getReactionEmote().getName(), true).get(0);
        if(emote != win && emote != loss) {
            return;
        }
        if(emote == win) {
            addWin();
        }
        else {
            addLoss();
        }
        updateMessage();
    }

    /**
     * Update the game message to display new score
     */
    private void updateMessage() {
        Message message = channel.getMessageById(id).complete();

        // Which form of win/wins, loss/losses to use if more than 1
        String win = this.streak == 1 ? " WIN" : " WINS";
        String loss = this.streak == -1 ? " LOSS" : " LOSSES";

        // Streak message to display, a negative streak should show as 2 losses not -2 losses
        String streak = this.streak < 0 ? Math.abs(this.streak) + loss : this.streak + win;

        MessageEmbed update = buildGameMessage(wins, losses, streak, createDesc());

        // If the game message is not the most recent, delete the old and regenerate to make playing easier
        if(channel.getLatestMessageIdLong() != id) {
            message.delete().complete();
            sendGameMessage(update);
        }
        // Edit the message in place
        else {
            message.editMessage(update).complete();
        }
    }

    /**
     * Sends the game message and adds the win/loss emotes so the user can click to add score
     *
     * @param gameMessage Interactive game message
     */
    private void sendGameMessage(MessageEmbed gameMessage) {

        // Callback to add reactions and save message id
        Consumer<Message> addReactionCallback = (response) -> {
            id = response.getIdLong();
            response.addReaction(win).queue();
            response.addReaction(loss).queue();
        };
        channel.sendMessage(gameMessage).queue(addReactionCallback);
    }

    /**
     * Generate a message to be shown on the game based on the current score.
     *
     * @return An encouraging message to be displayed on the game
     */
    private String createDesc() {
        Random rand = new Random();
        String[] messages;

        // Beginning of game
        if(wins == 0 && losses == 0) {
            messages = new String[]{
                    "Let's do this!",
                    "Good news, the cheque cleared... It's go time!",
                    "Shut up, cock in and load up.",
                    "Lock 'em and load 'em ladies it's go time.",
                    "Keep your head up and your ass down, it's go time."
            };
        }

        // Score is even
        else if(wins == losses) {
            messages = new String[]{
                    "Neck and neck, pick your game up cunts!",
                    "You better not fall behind",
                    "Get that lead back!"
            };
        }

        // last match won
        else if(streak >= 1) {

            // just taken lead and it isn't first match
            if(wins - losses == 1 && losses != 0) {
                messages = new String[]{
                        "Now Maintain that lead cunts!",
                        "Nice work, you're back in the game!",
                        "What a cum back!"
                };
            }

            // first win
            else if(wins - losses == 1) {
                messages = new String[]{
                        "Off to a good start, how long before you fuck it up?",
                        "Nice job spastics!",
                        "Bon boulot, get ready for the next round",
                        "Let's get a streak going!",
                };
            }

            // win streak
            else if(wins - losses > 1) {
                messages = new String[]{"Nice streak!",
                        "What a lead!",
                        "Unstoppable!",
                        "They never stood a chance!",
                        "Take it easy on them!",
                        "On a roll!",
                        "HERE COMES JOE",
                        "Beautiful, I especially enjoyed EdgarStiles' performance, it was incredible!"
                };
            }

            // still behind
            else {
                messages = new String[]{
                        "You're still behind cunts, not good enough",
                        "You still need " + Math.abs(wins - losses) + " " + (Math.abs(wins - losses) == 1 ? "win" : "wins") + " to pass them cunts"
                };
            }
        }

        // last match lost
        else {

            // just fallen behind
            if(losses - wins == 1) {
                messages = new String[]{
                        "You're behind cunts",
                        "You better win the fucking next one",
                        "What the fuck was that?",
                        "How didn't you kill him?",
                        "They were terrible, that final killcam was pathetic cunts, pick up your fucking game"
                };
            }
            else if(losses - wins == 2) {
                messages = new String[]{
                        "Here we go again you useless cunts, tilt time"
                };
            }

            // loss streak
            else if(losses - wins > 2) {
                messages = new String[]{
                        "Is this is the fucking special olympics?",
                        "Fucking pistol rounds",
                        "Fucking JOE",
                        "That was pathetic",
                        "This is embarrassing",
                        "You fucking useless cunts",
                        "Fuck that's embarrassing",
                        "Time to give up?",
                        "Give up now lmao",
                        "How the fuck are you " + (losses - wins) + " games behind?"
                };
            }

            // still ahead
            else {
                messages = new String[]{
                        "Pathetic, but at least you're " + (wins - losses) + (wins - losses == 1 ? " win" : " wins") + " ahead cunts",
                        "Don't you dare fall behind cunts",
                        "We lost that round but it's not over yet, you get ready for the next one"
                };
            }
        }
        return messages[rand.nextInt(messages.length)];
    }

    /**
     * Add a win to the scoreboard, reset streak if on a loss streak.
     */
    private void addWin() {
        if(streak < 0) {
            streak = 0;
        }
        wins++;
        streak++;
    }

    /**
     * Add a loss to the scoreboard, reset streak if on a win streak.
     */
    private void addLoss() {
        if(streak > 0) {
            streak = 0;
        }
        losses++;
        streak--;
    }

    /**
     * Get id of the game message
     *
     * @return Game message id
     */
    long getGameId() {
        return id;
    }

    /**
     * Get the user who started the game
     *
     * @return User who started the game
     */
    User getOwner() {
        return owner;
    }
}
