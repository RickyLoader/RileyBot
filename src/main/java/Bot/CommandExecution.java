package Bot;

import Audio.AudioPlayerSendHandler;
import Audio.TrackEndListener;

import static Bot.DiscordBot.*;

import Exchange.ExchangeData;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import static net.dv8tion.jda.core.Permission.BAN_MEMBERS;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.GuildController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * CommandExecution.java holds methods of INTERACTIVE type commands invoked via reflection
 *
 * @author Ricky Loader
 * @version 5000.0
 */
public class CommandExecution {

    // Information relating to the guild, user, and message which invoked the method
    private GuildMessageReceivedEvent e;
    private MessageChannel chan;
    private DiscordCommand c;
    private User self;
    private GuildController admin;
    private String msg;
    private Message rawMsg;
    private Guild guild;

    /**
     * Constructor takes a message event and a command object. Command object holds information relevant to the
     * command and the event holds information about the user, guild, and message.
     *
     * @param e Message event
     * @param c Command that was called
     */
    public CommandExecution(GuildMessageReceivedEvent e, DiscordCommand c) {
        this.e = e;
        this.chan = e.getChannel();
        this.c = c;
        this.self = e.getGuild().getSelfMember().getUser();
        this.admin = e.getGuild().getController();
        this.msg = e.getMessage().getContentDisplay();
        this.rawMsg = e.getMessage();
        this.guild = e.getGuild();
    }

    public CommandExecution(Guild guild) {
        this.guild = guild;
    }

    /**
     * Sends the user a message with an invite back to the server, apologises for banning them.
     *
     * @param loser The user to be banned
     */
    private void comfortBan(User loser) {
        if(loser.isBot() || loser.isFake()) {
            return;
        }
        PrivateChannel pc = loser.openPrivateChannel().complete();
        String header = "__**I AM SORRY**__\n\n";
        pc.sendMessage(header + "```Sorry about that man, " +
                "I'm actually a really friendly bot but when someone tells me to do something I have to do it!" +
                "\n\nFeel free to jump on in again if you'd like, ask someone to pardon you though otherwise you'll get added back to" +
                " the emperor's list when you join!```" + getInvite()).complete();
        pc.close();
    }

    /**
     * Generates an invite to the guild where the message was received.
     *
     * @return The generated invite
     */
    private String getInvite() {
        Invite invite = e.getChannel().createInvite().complete();
        String result = invite.getURL();
        return result;
    }

    /**
     * Alcoholic command
     */
    private void alcoholic() {
        String user = e.getAuthor().getAsMention();
        String result = "Hey @everyone my name is " + user + " and I am an alcoholic";
        chan.sendMessage(result).queue();
    }

    /**
     * Invite! command
     */
    private void invite() {
        send(getInvite());
    }

    /**
     * Brews command
     */
    private void brewCount() {
        String num = e.getMessage().getContentDisplay().split(" ")[0];
        String author = e.getAuthor().getAsMention();
        String result = "Hey @everyone my name is " + author + " and I have had " + num + " brews!";
        send(result);
    }

    /**
     * New name command. Generates a random name up to 15 characters in length.
     */
    private void randomName() {
        Random rand = new Random();
        int attempts = 0;
        String name = "";
        int maxLength = 15;

        // 3 attempts
        while(attempts <= 2) {

            // Get a random word
            String word = c.getLink();

            // Append it to the current name if it can fit
            if(name.length() + word.length() <= maxLength) {
                name += word;
            }

            // Either way the attempts increment
            attempts++;
        }

        // How many characters left to to make the max length
        int charToMax = (maxLength - name.length());

        // Generate a number from 0 to the maximum number that could fit in that space. E.g (10^5)-1 = 99,999 (5 chars)
        if(charToMax > 0) {
            name += rand.nextInt(10 ^ charToMax);
        }
        chan.sendMessage(name).queue();
    }

    /**
     * Get the message n+1 messages back in the current channel. 0 will be the message used to invoke the command.
     *
     * @param n The index of the message to obtain, 0 being the most recent.
     * @return The message at the given index.
     */
    private Message getLastMessage(int n) {
        Message m = chan.getHistory().retrievePast(n + 1).complete().get(n);
        return m;
    }

    /**
     * Send a message containing the marked targets to the user if they have permission, mark them if they don't.
     */
    private void killList() {
        String summary;
        User author = e.getAuthor();

        if(targets.isEmpty()) {
            summary = "There are no targets sir";
        }
        else {
            String codeBlock = "```";
            String intro = "Hello sir, here are the pending targets:\n";
            summary = codeBlock;
            int i = 1;
            for(User target : targets) {
                summary += i + getSpaces(5) + target.getName() + "\n";
                i++;
            }
            summary += codeBlock;
            String header = codeBlock + "NUMBER" + getSpaces(5) + "TARGET NAME" + codeBlock;
            summary = intro + header + summary + "\n\nExecute order 66 when you're ready sir";
        }
        PrivateChannel pm = author.openPrivateChannel().complete();
        pm.sendMessage(summary).queue();
        pm.close();
    }

    /**
     * Pardon all targets if the user has permission, mark them if they don't.
     */
    private void pardonAll() {
        User author = e.getAuthor();
        String msg = "There are no targets bro fuck off";
        if(targets.isEmpty()) {
            return;
        }
        else {
            for(User target : targets) {
                DiscordUser container = findUser(target.getAsMention());
                if(container.pardonUser()) {
                    msg = "@everyone " + target.getName() + " was successfully pardoned, congratulations sir";
                }
                else {
                    msg = "@everyone " + target.getName() + " was not pardoned";
                }
            }
        }
        targets = new ArrayList<>();
        send(msg);
    }

    /**
     * Ban all targets on the kill list and send a summary of the operation.
     */
    private void purgeTargets() {
        int killed = 0;
        ArrayList<User> survived = new ArrayList<>();
        for(User target : targets) {
            System.out.println("Attempting to ban " + target.getName() + " (" + target.getAsMention() + ") ");
            try {
                comfortBan(target);
                admin.ban(target, 7).complete();
                unban(target);
                killed++;
                System.out.println("Success!");
            }
            catch(Exception e) {
                survived.add(target);
                System.out.println(target.getName() + " (" + target.getAsMention() + ") was not banned");
            }
        }
        send(killed + "/" + targets.size() + " targets EXTERMINATED");
        targets = survived;
    }

    /**
     * Delete the given message
     *
     * @param m Message to be deleted
     */
    private void deleteMessage(Message m) {
        chan.deleteMessageById(m.getId()).complete();
    }

    /**
     * !play command. Plays the given youtube URL in the voice channel
     */
    private void play() {
        String audio = e.getMessage().getContentDisplay().replace("!play ", "");
        deleteMessage(getLastMessage(0));
        playAudio(audio, new TrackEndListener(null, e.getGuild()));
    }

    private VoiceChannel findChannel() {
        VoiceChannel channel = null;
        for(Guild g : self.getMutualGuilds()) {
            for(VoiceChannel vc : g.getVoiceChannels()) {
                List<Member> chatters = vc.getMembers();
                if(!chatters.isEmpty()) {
                    if(channel == null || chatters.size() > channel.getMembers().size()) {
                        channel = vc;
                    }
                }
            }
        }

        return channel;
    }

    /**
     * Plays the given audio in the voice channel.
     *
     * @param audio    The URL to be played
     * @param listener The instance of TrackEndListener to be used
     */
    public void playAudio(String audio, TrackEndListener listener) {
        VoiceChannel vc = findChannel();
        if(vc == null) {
            send("There's nobody for me to talk to cunt");
            return;
        }

        // Initialise audio player
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer player = playerManager.createPlayer();
        player.addListener(listener);
        vc.getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));

        // Join the voice channel
        vc.getGuild().getAudioManager().openAudioConnection(vc);
        if(vc.getGuild().getMember(self).getVoiceState().isMuted()) {
            vc.getGuild().getController().setMute(vc.getGuild().getMember(self), false);
        }
        // Load the URL in to the player
        playerManager.loadItem(audio, new AudioLoadResultHandler() {

            /**
             * Play the audio once loaded
             *
             * @param audioTrack Track loaded in to player
             */
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                player.playTrack(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Returns whether a user has the permissions to execute a ban related command. Bot perms must be checked as it does
     * the banning, user perms checked to stop users using the bot to ban people if they can't.
     *
     * @param killer User attempting to call a ban related command
     * @return Boolean authorised to ban users
     */
    private boolean isAuthorised(User killer, Member target) {
        boolean userPerm = guild.getMember(killer).hasPermission(BAN_MEMBERS);
        boolean botPerm = guild.getMember(self).hasPermission(BAN_MEMBERS);
        boolean rolePerm = guild.getMember(self).canInteract(target);
        return userPerm && botPerm && rolePerm;
    }

    /**
     * Adds the user to the kill list if they aren't already on it, warns them otherwise.
     *
     * @param author User who called a command they weren't authorised for
     * @return The reply to be sent to the User
     */
    private String unauthReact(User author) {
        DiscordUser authorObj = findUser(author.getAsMention());
        String reply = "";
        if(!targets.contains(author)) {
            if(authorObj.markUser()) {
                targets.add(author);
                reply = author.getAsMention() + " You think I wouldn't notice you aren't authorised to do that?\nNow YOU'RE on the kill list cunt";
            }
        }
        else {
            reply = author.getAsMention() + " Normally this is where i'd add YOU to the kill list for trying to do something you're not allowed to do. " +
                    "\nI'm not actually going to do to that though because it turns out you're already on it CUNT";
        }
        return reply;
    }

    /**
     * Log a TTS call in a dedicated channel.
     *
     * @param msg    The content that was converted from text to speech
     * @param author The user who called TTS
     */
    private void logTTS(String msg, User author) {
        List<TextChannel> channels = guild.getTextChannelsByName("tts-log", true);
        if(!channels.isEmpty()) {
            MessageChannel log = channels.get(0);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(15655767);
            builder.setDescription("**Author**: " + author.getAsMention() + " **Submitted At**: " + new Date());
            builder.setTitle("**TTS Log**");
            builder.addField("Contents:", msg, false);
            log.sendMessage(builder.build()).queue();
        }
    }

    /**
     * .command (TTS) Play the given text in the voice channel, delete the message, log the result.
     */
    private void chatTime() {
        try {
            String base = "http://192.168.1.76/DiscordBotAPI/api/dectalk/";
            String content = msg.replaceFirst(".", "");
            if(content.isEmpty()) {
                content = "Give me something to say cunt";
            }
            String url = URLEncoder.encode(content, "UTF-8");
            String audio = base + url;
            System.out.println(audio);
            deleteMessage(getLastMessage(0));
            logTTS(content, e.getAuthor());
            playAudio(audio, new TrackEndListener(null, guild));
        }
        catch(UnsupportedEncodingException e) {
            System.out.println("cunt");
        }
    }


    /**
     * survivor command
     */
    private void survivor() {
        try {
            String base = "http://192.168.1.76/DiscordBotAPI/api/survivor/";
            String content = msg.replaceFirst(".survivor ", "");
            String url = URLEncoder.encode(content, "UTF-8");
            String audio = base + url;
            logTTS(content, e.getAuthor());
            playAudio(audio, new TrackEndListener(null, guild));
            deleteMessage(getLastMessage(0));
        }
        catch(UnsupportedEncodingException e) {
            System.out.println("cunt");
        }
    }

    /**
     * TODO
     */
    private void tagImage() {

    }

    /**
     * Mark a user for extermination by the bot
     */
    private void mark() {
        String targetName = msg.replace("mark @", "");
        System.out.println("Beginning mark process:\n\n");
        System.out.println("Message was: " + msg + "\n\n");
        System.out.println(targetName);
        Member targetMember = getServerMember(targetName);
        User author = e.getAuthor();
        String reply;
        String mention = author.getAsMention();
        System.out.println("Target is: " + targetName + " \n\n");

        if(targetMember == null) {
            send(author.getAsMention() + " that cunt doesn't exist, fuck off");
            return;
        }
        User target = targetMember.getUser();

        if(isAuthorised(author, targetMember)) {
            if(targets.contains(target)) {
                reply = author.getAsMention() + " " + target.getName() + " is already on the kill list, now he knows cunt, better execute order 66 real quick";
            }
            else {
                DiscordUser authorObj = findUser(target.getAsMention());
                authorObj.markUser();
                targets.add(target);
                reply = mention + " " + targetName + " has been added to the kill list! Congratulations sir!";
            }
        }
        else {
            reply = "One of us isn't allowed to ban that person cunt.";
        }
        send(reply);
    }


    private void pardon() {
        String targetName = msg.replace("pardon @", "");
        Member targetMember = getServerMember(targetName);
        User author = e.getAuthor();
        String reply;
        String mention = "@everyone ";

        if(targetMember == null) {
            send("Who the fuck is that?");
            return;
        }
        User target = targetMember.getUser();

        if(targets.contains(target)) {
            DiscordUser targetObj = findUser(target.getAsMention());

            // Successfully pardoned the target
            if(targetObj.pardonUser()) {
                System.out.println("Pardoning " + targetObj.getAlias() + "...\n\n");
                targets.remove(target);
                reply = mention + target.getName() + " was successfully pardoned, congratulations sir";
            }
            // Pardon failed for API related reason
            else {
                reply = mention + target.getName() + " was not pardoned";
            }
        }
        // Target is not marked
        else {
            reply = author.getAsMention() + " Nobody by that name is on the kill list cunt";
        }
        send(reply);
    }

    /**
     * Ban or unban a given user.
     *
     * @param ban    Boolean true = ban false = unban
     * @param target The user for the action to be performed on
     * @return A String result of the action
     */
    private String adminAction(boolean ban, User target) {
        String all = "@everyone";
        String result = all + " Ladies and gentlemen, we got him.";

        if(ban) {
            comfortBan(target);
            admin.ban(target, 7).complete();
            unban(target);
        }
        else {
            admin.unban(target).complete();
            result = all + " Ladies and gentlemen, we saved him.";
        }
        return result;
    }

    /**
     * Obtain a list of user objects from the list of banned member objects
     *
     * @return A list of all banned users of the current guild
     */
    private List<User> getBans() {
        return guild.getBanList().complete().stream().map(ban -> ban.getUser()).collect(Collectors.toList());
    }

    /**
     * Get the user object of a given @mention
     *
     * @param target String containing the @mention of a user
     * @return The user object or null if the target does not exist
     */
    public Member getServerMember(String target) {
        Member result = null;
        List<Member> serverMembers = guild.getMembers();

        for(Member m : serverMembers) {
            if(m.getEffectiveName().equals(target)) {
                result = m;
                break;
            }
        }
        return result;
    }

    /**
     * TODO cunt
     * :ANY EXISTING COMMAND - shows the number of times the given command has been called
     */
    private void commandStats() {
        if(rawMsg.getEmotes().size() > 0) {
            return;
        }
        msg = msg.toLowerCase();
        DiscordCommand targetCommand = DiscordBot.getCommand(msg.replaceFirst(":", ""));
        String result = "That command doesn't exist cunt.";
        if(targetCommand != null) {
            result = targetCommand.summary();
        }
        chan.sendMessage(result).queue();
    }

    /**
     * !ITEM command - Find the price of an item in the grand exchange
     */
    private void grandExchange() {
        String item = e.getMessage().getContentDisplay().split("!")[1];
        if(item.equalsIgnoreCase("rank")) {
            return;
        }
        // Object containing exchange data is read in on start up. If the data is > 15 minutes old, refresh it
        if((System.currentTimeMillis() - exchangeData.getLastCalled()) > 900000) {
            exchangeData = new ExchangeData();
        }
        chan.sendMessage(exchangeData.requestItem(item)).queue();
    }

    /**
     * Searches the commands for any containing a given term.
     */
    private void findCommand() {
        msg = msg.toLowerCase();
        String query = msg.replace("find: ", "");
        StringBuilder result = new StringBuilder();
        int count = 0;
        String commandsFound = "";
        for(String trigger : DiscordBot.commands.keySet()) {
            DiscordCommand c = commands.get(trigger);
            String help = c.getHelpName();
            String desc = c.getDesc();

            String searchable = trigger + " " + desc + " " + help;
            if(searchable.contains(query)) {
                count++;
                commandsFound
                        += count + ". " + DiscordBot.commands.get(trigger).getHelpName() + " - " +
                        DiscordBot.commands.get(trigger).getDesc() + "\n";
            }
        }
        if(count > 0) {
            result.append("I found " + count + " commands matching your search query cunt:\n\n");
            result.append("```" + commandsFound + "```");
        }
        else {
            result.append("I found nothing matching that search query dumb fuck");
        }


        chan.sendMessage(result.toString()).queue();
    }

    private void sandCasino() {
        Random rand = new Random();
        String[] result = {"WON", "LOST"};
        send(e.getAuthor().getAsMention() + " YOU HAVE " + result[rand.nextInt(2)] + " THIS DUEL!");
    }

    /**
     * Send a message to the current text channel
     *
     * @param msg String message to be sent
     */
    private void send(String msg) {
        chan.sendMessage(msg).queue();
    }

    private Member getMember(User target) {
        return admin.getGuild().getMember(target);
    }

    /**
     * Bans every user on the kill list and resets the list. Plays audio prior to firing.
     */
    private void executeOrder66() {
        System.out.println("Received the order from " + rawMsg.getAuthor().getName() + ", Executing order 66...\n\n");

        String[] tracks = new String[]{
                "https://www.youtube.com/watch?v=A8ZZmU8orvg",
                "https://youtu.be/rV2l_WNd7Wo",
                "https://www.youtube.com/watch?v=rAfFSu-_3cA",
                "https://www.youtube.com/watch?v=b_KYjfYjk0Q",
                "https://www.youtube.com/watch?v=NKqPBShXv8M",
                "https://www.youtube.com/watch?v=M_wxitPU54s"
        };

        Random rand = new Random();
        String audio = tracks[rand.nextInt(tracks.length)];

        String message = "YES SIR, STAND BY.";

        // Delete the message which activated the command (snitches get stitches)
        deleteMessage(getLastMessage(0));

        // There are targets to be exterminated
        if(!targets.isEmpty()) {
            // Implement the Response interface method to purge the kill list after the track finishes
            TrackEndListener.Response method = () -> new Thread(() -> purgeTargets()).start();
            TrackEndListener listener = new TrackEndListener(method, guild);
            // Play thr track
            playAudio(audio, listener);
        }
        // No targets
        else {
            message = "TARGET SECTORS ARE ALREADY CLEAR SIR, OVER";
        }

        User author = e.getAuthor();
        if(!author.isBot()) {
            PrivateChannel pm = author.openPrivateChannel().complete();
            pm.sendMessage(message).queue();
            pm.close();
        }
    }

    /**
     * Drop a tactical nuke on the server, ban everyone
     */
    private void nuke() {
        String audio = "https://www.youtube.com/watch?v=b_KYjfYjk0Q";
        deleteMessage(getLastMessage(0));

        // Implement the Response interface method to drop the nuke after the track finishes
        TrackEndListener.Response method = () -> new Thread(() -> admin.kick(e.getMember())).start();
        TrackEndListener listener = new TrackEndListener(method, guild);

        // Play the track
        playAudio(audio, listener);
    }

    /**
     * Unban the given user
     *
     * @param loser A banned user
     */
    private void unban(User loser) {
        admin.unban(loser).complete();
    }
}
