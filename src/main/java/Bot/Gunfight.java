package Bot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.Random;
import java.util.function.Consumer;

public class Gunfight {

    private long id;
    private MessageChannel channel;

    private Emote win;
    private Emote loss;

    private int wins, losses, streak = 0;

    private Guild server;

    public Gunfight(MessageChannel channel, Guild server) {
        this.channel = channel;
        this.server = server;
        this.win = server.getEmotesByName("victory", true).get(0);
        this.loss = server.getEmotesByName("defeat", true).get(0);
        startGame();
    }

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

    private void startGame() {
        MessageEmbed game = buildGameMessage(wins, losses, String.valueOf(streak), createDesc());
        Consumer<Message> callback = (response) -> {
            id = response.getIdLong();
            response.addReaction(win).queue();
            response.addReaction(loss).queue();
        };
        channel.sendMessage(game).queue(callback);
    }

    public long getGameId() {
        return id;
    }

    public void reactionAdded(MessageReaction reaction) {
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

    private void updateMessage() {
        Message message = channel.getMessageById(id).complete();
        String win = this.streak == 1 ? " WIN" : " WINS";
        String loss = this.streak == -1 ? " LOSS" : " LOSSES";
        String streak = this.streak < 0 ? Math.abs(this.streak) + loss : this.streak + win;
        message.editMessage(buildGameMessage(wins, losses, streak, createDesc())).complete();
    }

    private String createDesc() {

        Random rand = new Random();
        String result;

        if(wins == 0 && losses == 0) {
            result = "Let's do this!";
        }

        else if(wins == losses) {
            String[] neck = new String[]{"Neck and neck, pick your game up cunts!", "You better not fall behind", "Get that lead back!"};
            result = neck[rand.nextInt(neck.length)];
        }

        // last match won
        else if(streak >= 1) {
            // just taken lead
            if(wins - losses == 1 && losses != 0) {
                String[] lead = new String[]{"Now Maintain that lead cunts!", "Nice work, you're back in the game!", "What a cum back!"};
                result = lead[rand.nextInt(lead.length)];
            }
            // first win
            else if(wins - losses == 1) {
                String[] start = new String[]{"Off to a good start, bets on how long before you fuck it up?", "Nice job spastics!", "Let's get a streak going!"};
                result = start[rand.nextInt(start.length)];
            }
            // win streak
            else if(wins - losses > 1) {
                String[] streak = new String[]{"Nice streak!", "What a lead!", "Unstoppable!", "They never stood a chance!", "Take it easy on them!", "On a roll!", "HERE COMES JOE", "Beautiful, I especially enjoyed EdgarStiles' performance, it was incredible!"};
                result = streak[rand.nextInt(streak.length)];
            }
            // still behind
            else {
                String[] wonBehind = new String[]{"You're still behind cunts, not good enough", "You still need " + Math.abs(wins - losses) + " " + (Math.abs(wins - losses) == 1 ? "win" : "wins") + " to pass them cunts"};
                result = wonBehind[rand.nextInt(wonBehind.length)];
            }
        }
        // last match lost
        else {
            // just fallen behind
            if(losses - wins == 1) {
                String[] lead = new String[]{"You're behind cunts", "You better win the fucking next one", "What the fuck was that?", "How didn't you kill him?", "They were fucking terrible, that final killcam was pathetic cunts, pick up your fucking game"};
                result = lead[rand.nextInt(lead.length)];
            }
            else if(losses - wins == 2) {
                result = "Here we go again you useless cunts, tilt time";
            }
            // loss streak
            else if(losses - wins > 2) {
                String[] streak = new String[]{"Is this is the fucking special olympics?", "Fucking pistol rounds", "Fucking JOE", "That was pathetic", "This is embarrassing", "You fucking useless cunts", "Fuck that's embarrassing", "Time to give up?", "Yea give up now lmao", "How the fuck are you " + (losses - wins) + " wins behind?"};
                result = streak[rand.nextInt(streak.length)];
            }
            // still ahead
            else {
                String[] lostAhead = new String[]{"Pathetic, but at least you're " + (wins - losses) + " wins ahead cunts", "Don't you dare fall behind cunts"};
                result = lostAhead[rand.nextInt(lostAhead.length)];
            }
        }
        return result + "                            ";
    }

    private void addWin() {
        if(streak < 0) {
            streak = 0;
        }
        wins++;
        streak++;
    }

    private void addLoss() {
        if(streak > 0) {
            streak = 0;
        }
        losses++;
        streak--;
    }
}
