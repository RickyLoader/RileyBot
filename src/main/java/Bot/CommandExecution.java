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
public class CommandExecution{

    // Information relating to the guild, user, and message which invoked the method
    private GuildMessageReceivedEvent e;
    private MessageChannel chan;
    private DiscordCommand c;
    private User self;
    private GuildController admin;
    private String msg;

    /**
     * Constructor takes a message event and a command object. Command object holds information relevant to the
     * command and the event holds information about the user, guild, and message.
     *
     * @param e Message event
     * @param c Command that was called
     */
    public CommandExecution(GuildMessageReceivedEvent e, DiscordCommand c){
        this.e = e;
        this.chan = e.getChannel();
        this.c = c;
        this.self = e.getGuild().getSelfMember().getUser();
        this.admin = e.getGuild().getController();
        this.msg = e.getMessage().getContentDisplay();
    }

    /**
     * Sends the user a message with an invite back to the server, apologises for banning them.
     *
     * @param loser The user to be banned
     */
    private void comfortBan(User loser){
        if(loser.isBot() || loser.isFake()){
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
    private String getInvite(){
        Invite invite = e.getChannel().createInvite().complete();
        String result = invite.getURL();
        return result;
    }

    /**
     * Alcoholic command
     */
    private void alcoholic(){
        String user = e.getAuthor().getAsMention();
        String result = "Hey @everyone my name is " + user + " and I am an alcoholic";
        chan.sendMessage(result).queue();
    }

    /**
     * Invite! command
     */
    private void invite(){
        send(getInvite());
    }

    /**
     * Brews command
     */
    private void brewCount(){
        String num = e.getMessage().getContentDisplay().split(" ")[0];
        String author = e.getAuthor().getAsMention();
        String result = "Hey @everyone my name is " + author + " and I have had " + num + " brews!";
        send(result);
    }

    /**
     * New name command. Generates a random name up to 15 characters in length.
     */
    private void randomName(){
        Random rand = new Random();
        int attempts = 0;
        String name = "";
        int maxLength = 15;

        // 3 attempts
        while(attempts <= 2){

            // Get a random word
            String word = c.getLink();

            // Append it to the current name if it can fit
            if(name.length() + word.length() <= maxLength){
                name += word;
            }

            // Either way the attempts increment
            attempts++;
        }

        // How many characters left to to make the max length
        int charToMax = (maxLength - name.length());

        // Generate a number from 0 to the maximum number that could fit in that space. E.g (10^5)-1 = 99,999 (5 chars)
        if(charToMax > 0){
            name += rand.nextInt(10 ^ charToMax);
        }
        chan.sendMessage(name).queue();
    }

    /**
     * TODO implement
     */
    private void flagImage(){
      /*  Message m = getLastMessage(1);
        if(m.getAuthor() == self){
            m.delete().complete();
            String data = m.getContentDisplay();
            //c.updateData(data);
            chan.sendMessage("Message has been flagged as offensive, i'm gay!").queue();
        }
        else{
            chan.sendMessage("Nothing to flag cunt, fuck off.").queue();
        }*/
    }

    /**
     * Get the message n+1 messages back in the current channel. 0 will be the message used to invoke the command.
     *
     * @param n The index of the message to obtain, 0 being the most recent.
     * @return The message at the given index.
     */
    private Message getLastMessage(int n){
        Message m = chan.getHistory().retrievePast(n + 1).complete().get(n);
        return m;
    }

    /**
     * TODO implement
     */
    private void tagImage(){
        String tag = e.getMessage().getContentDisplay().toLowerCase().replace("tag: ", "");
        Message last = getLastMessage(1);
        String URL = last.getContentDisplay();
        String description = "A meme that was tagged as " + tag + ".";
        DiscordCommand c = new DiscordCommand("LINK", tag, description);
        c.setImage(URL);
        DiscordCommand.addCommand(c, commands);
    }

    /**
     * Send a message containing the marked targets to the user if they have permission, mark them if they don't.
     */
    private void killList(){
        String summary;
        User author = e.getAuthor();

        // Authorised user
        if(isAuthorised(author)){
            if(targets.isEmpty()){
                summary = "There are no targets sir";
            }
            else{
                String codeBlock = "```";
                String intro = "Hello sir, here are the pending targets:\n";
                summary = codeBlock;
                int i = 1;
                for(User target : targets){
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

        // Mark the unauthorised user
        else{
            send(unauthReact(author));
        }
    }

    /**
     * Pardon all targets if the user has permission, mark them if they don't.
     */
    private void pardonAll(){
        User author = e.getAuthor();
        String msg = "There are no targets bro fuck off";

        // Authorised user
        if(isAuthorised(author)){
            if(targets.isEmpty()){
                return;
            }
            else{
                for(User target : targets){
                    DiscordUser container = findUser(target.getAsMention());
                    if(container.pardonUser()){
                        msg = "@everyone " + target.getName() + " was successfully pardoned, congratulations sir";
                    }
                    else{
                        msg = "@everyone " + target.getName() + " was not pardoned";
                    }
                }
            }
            targets = new ArrayList<>();
            send(msg);
        }
        // Mark the unauthorised user
        else{
            send(unauthReact(author));
        }
    }

    /**
     * Ban all targets on the kill list and send a summary of the operation.
     */
    private void purgeTargets(){
        int killed = 0;
        String msg;
        String killResult;
        ArrayList<String> survived = new ArrayList<>();
        for(User target : targets){
            try{
                comfortBan(target);
                admin.ban(target, 7).complete();
                unban(target);
                killed++;
            }
            catch(Exception e){
                survived.add(target.getAsMention());
                System.out.println(target.getName() + " is no longer here!");
            }
        }

        killResult = killed + "/" + targets.size() + " targets ";


        if(killed == 0){
            String survivors = "";
            for(String name : survived){
                survivors += name + "\n";
            }
            msg = "Unfortunately " + killResult + " were exterminated, congratulations:\n\n" + survivors + "\n\n You are free to go!";
        }
        else{
            msg = killResult + "EXTERMINATED";
        }
        send(msg);
        targets = new ArrayList<>();
    }

    /**
     * Delete the given message
     *
     * @param m Message to be deleted
     */
    private void deleteMessage(Message m){
        chan.deleteMessageById(m.getId()).complete();
    }

    /**
     * !play command. Plays the given youtube URL in the voice channel
     */
    private void play(){
        String audio = e.getMessage().getContentDisplay().replace("!play ", "");
        deleteMessage(getLastMessage(0));
        playAudio(audio, new TrackEndListener(null, e.getGuild()));
    }

    /**
     * Plays the given audio in the voice channel.
     *
     * @param audio    The URL to be played
     * @param listener The instance of TrackEndListener to be used
     */
    private void playAudio(String audio, TrackEndListener listener){
        VoiceChannel vc = e.getMember().getVoiceState().getChannel();

        // User is not in a voice channel
        if(vc == null){
            send(e.getAuthor().getAsMention() + " join a voice channel first cunt");
            return;
        }
        // Initialise audio player
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer player = playerManager.createPlayer();
        player.addListener(listener);
        e.getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));

        // Join the voice channel
        e.getGuild().getAudioManager().openAudioConnection(vc);

        // Load the URL in to the player
        playerManager.loadItem(audio, new AudioLoadResultHandler(){

            /**
             * Play the audio once loaded
             *
             * @param audioTrack Track loaded in to player
             */
            @Override
            public void trackLoaded(AudioTrack audioTrack){
                System.out.println(audioTrack.getIdentifier() + " loaded!");
                player.playTrack(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist){

            }

            @Override
            public void noMatches(){

            }

            @Override
            public void loadFailed(FriendlyException e){
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
    private boolean isAuthorised(User killer){
        boolean userPerm = admin.getGuild().getMember(killer).hasPermission(BAN_MEMBERS);
        boolean botPerm = admin.getGuild().getMember(self).hasPermission(BAN_MEMBERS);
        return userPerm && botPerm;
    }

    /**
     * Adds the user to the kill list if they aren't already on it, warns them otherwise.
     *
     * @param author User who called a command they weren't authorised for
     * @return The reply to be sent to the User
     */
    private String unauthReact(User author){
        DiscordUser authorObj = findUser(author.getAsMention());
        String reply = "";
        if(!targets.contains(author)){
            if(authorObj.markUser()){
                targets.add(author);
                reply = author.getAsMention() + " You think I wouldn't notice you aren't authorised to do that?\nNow YOU'RE on the kill list cunt";
            }
        }
        else{
            reply = author.getAsMention() + " Normally this is where i'd add YOU to the kill list for trying to do something you're not allowed to do. " +
                    "\nI'm not actually going to do to that though because it turns out you're already on it CUNT";
        }
        return reply;
    }

    /**
     * Unban all banned users
     * TODO check
     */
    private void clearBanList(){
        User author = e.getAuthor();
        String summary = "";
        List<User> bans = getBans();
        if(isAuthorised(author)){
            if(bans.isEmpty()){
                summary = author.getAsMention() + " there are no banned users pussy";
            }
            else{
                for(User ban : bans){
                    admin.unban(ban).complete();
                    comfortBan(ban);
                }
                summary = bans.size() + " cunts have been set free!";
            }
        }
        else{
            send(unauthReact(author));
        }
        send(summary);

    }

    /**
     * Show a table containing all the banned users
     */
    private void showBans(){
        List<User> targets = getBans();
        User author = e.getAuthor();
        String summary;

        if(targets.isEmpty()){
            summary = author.getAsMention() + " there are no banned users pussy";
        }
        else{
            String codeBlock = "```";
            String intro = "Here are the unlucky cunts:\n";
            summary = codeBlock;
            int i = 1;
            for(User target : targets){
                summary += i + getSpaces(5) + target.getName() + "\n";
                i++;
            }
            summary += codeBlock;
            String header = codeBlock + "NUMBER" + getSpaces(5) + "TARGET NAME" + codeBlock;
            summary = intro + header + summary;
        }
        send(summary);
    }

    /**
     * Log a TTS call in a dedicated channel.
     *
     * @param msg    The content that was converted from text to speech
     * @param author The user who called TTS
     */
    private void logTTS(String msg, User author){
        MessageChannel log = e.getGuild().getTextChannelsByName("tts-log", true).get(0);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setDescription("**Author**: " + author.getAsMention() + " **Submitted At**: " + new Date());
        builder.setTitle("**TTS Log**");
        builder.addField("Contents:", msg, false);
        log.sendMessage(builder.build()).queue();
    }

    /**
     * .command (TTS) Play the given text in the voice channel, delete the message, log the result.
     */
    private void chatTime(){
        try{
            String base = "http://192.168.1.69/DiscordBotAPI/public/index.php/api/dectalk/";
            String content = msg.replaceFirst(".", "");
            String url = URLEncoder.encode(content, "UTF-8");
            String audio = base + url;
            logTTS(content, e.getAuthor());
            playAudio(audio, new TrackEndListener(null, e.getGuild()));
            deleteMessage(getLastMessage(0));
        }
        catch(UnsupportedEncodingException e){
            System.out.println("cunt");
        }
    }

    /**
     * Mark a user for extermination by the bot
     */
    private void mark(){
        String targetName = msg.replace("mark @", "");
        User target = getServerUser(targetName);
        User author = e.getAuthor();
        String reply;
        String mention = author.getAsMention();

        // Authorised user
        if(isAuthorised(author)){

            // Target is provided
            if(target != null){

                // Target is not on the kill list
                if(!targets.contains(target)){
                    DiscordUser authorObj = findUser(target.getAsMention());

                    // Successfully marked the target
                    if(authorObj.markUser()){
                        targets.add(target);
                        reply = mention + " " + targetName + " has been added to the kill list! Congratulations sir!";
                    }

                    // Marking target failed for API related reason
                    else{
                        reply = mention + " I was unable to add " + targetName + " to the kill list, but now he knows you want to dumbass";
                    }
                }

                // Target is already on the kill list
                else{
                    reply = author.getAsMention() + " " + target.getName() + " is already on the kill list, now he knows cunt, better execute order 66 real quick";
                }
            }

            // Target is not provided
            else{
                reply = author.getAsMention() + " that cunt doesn't exist, fuck off";
            }
        }
        // Unauthorised user
        else{
            reply = unauthReact(author);
        }
        send(reply);
    }

    /**
     *
     */
    private void pardon(){
        String targetName = msg.replace("pardon @", "");
        User target = getServerUser(targetName);
        User author = e.getAuthor();
        String reply;
        String mention = "@everyone ";

        // Authorised user
        if(isAuthorised(author)){

            // Target is currently marked
            if(targets.contains(target)){
                DiscordUser targetObj = findUser(target.getAsMention());

                // Successfully pardoned the target
                if(targetObj.pardonUser()){
                    targets.remove(target);
                    reply = mention + target.getName() + " was successfully pardoned, congratulations sir";
                }
                // Pardon failed for API related reason
                else{
                    reply = mention + target.getName() + " was not pardoned";
                }
            }
            // Target is not marked
            else{
                reply = author.getAsMention() + " Nobody by that name is on the kill list cunt";
            }
        }
        // Unauthorised user
        else{
            reply = unauthReact(author);
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
    private String adminAction(boolean ban, User target){
        String all = "@everyone";
        String result = all + " Ladies and gentlemen, we got him.";

        if(ban){
            comfortBan(target);
            admin.ban(target, 7).complete();
            unban(target);
        }
        else{
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
    private List<User> getBans(){
        Guild server = e.getGuild();
        return server.getBanList().complete().stream().map(ban -> ban.getUser()).collect(Collectors.toList());
    }

    /**
     * Get the user object of a given @mention
     *
     * @param target String containing thr @mention of a user
     * @return The user object or null if the target does not exist
     */
    public User getServerUser(String target){
        Guild server = e.getGuild();
        User result = null;
        List<Member> serverMembers = server.getMembers();

        for(Member m : serverMembers){
            if(m.getEffectiveName().equals(target) && !m.getUser().isBot()){
                result = m.getUser();
                break;
            }
        }
        return result;
    }

    /**
     * Scull command
     */
    private void scull(){
        msg = msg.toLowerCase();
        String target = msg.replace(" scull!", "");
        DiscordUser user = users.get(target);
        String result;
        String quote = "\"";
        if(user == null){
            result = e.getAuthor().getAsMention() + " " + quote + target + quote + " does not exist cunt fuck off.";
        }
        else{
            result = "Hey " + user.getID() + "! scull a brew cunt!";
        }
        chan.sendMessage(result).queue();
    }

    /**
     * :alias command. Updates the user's alias.
     */
    private void updateAlias(){
        msg = msg.toLowerCase();
        String desiredAlias = msg.split(":alias ")[1];
        String id = e.getAuthor().getAsMention();
        DiscordUser targetUser = DiscordBot.findUser(id);
        String old = targetUser.getAlias();

        if(old.equals(desiredAlias)){
            chan.sendMessage(id + " " + desiredAlias + " is already your alias cunt, fuck off").queue();
        }
        else
            if(targetUser.setAlias(desiredAlias)){
                chan.sendMessage(id + " Your alias was successfully updated from " + old + " to " + targetUser.getAlias() + "!").queue();
                DiscordBot.refactorUsers();
            }
            else{
                chan.sendMessage(id + " I couldn't update your alias cunt").queue();
            }
    }

    /**
     * :ANY EXISTING COMMAND - shows the number of times the given command has been called
     */
    private void commandStats(){
        msg = msg.toLowerCase();
        String targetTrigger = DiscordBot.getTrigger(msg.replaceFirst(":", ""));
        DiscordCommand targetCommand = commands.get(targetTrigger);
        String result = "That command doesn't exist cunt.";
        if(targetCommand != null){
            result = targetCommand.summary();
        }
        chan.sendMessage(result).queue();
    }

    /**
     * !ITEM command - Find the price of an item in the grand exchange
     */
    private void grandExchange(){

        // Object containing exchange data is read in on start up. If the data is > 15 minutes old, refresh it
        if((System.currentTimeMillis() - exchangeData.getLastCalled()) > 900000){
            exchangeData = new ExchangeData();
        }
        chan.sendMessage(exchangeData.requestItem(e.getMessage().getContentDisplay().split("!")[1])).queue();
    }

    /**
     * Searches the commands for any containing a given term.
     */
    private void findCommand(){
        msg = msg.toLowerCase();
        String query = msg.replace("find: ", "");
        StringBuilder result = new StringBuilder();
        int count = 0;
        String commandsFound = "";
        for(String trigger : DiscordBot.commands.keySet()){
            DiscordCommand c = commands.get(trigger);
            String help = c.getHelpName();
            String desc = c.getDesc();
            String info = "";
            switch(c.getType()) {
                case "LINK":
                    info = c.getImage().getDesc();
                    break;
            }

            String searchable = trigger + " " + desc + " " + help + " " + info;
            if(searchable.contains(query)){
                count++;
                commandsFound
                        += count + ". " + DiscordBot.commands.get(trigger).getHelpName() + " - " +
                        DiscordBot.commands.get(trigger).getDesc() + "\n";
            }
        }
        if(count > 0){
            result.append("I found " + count + " commands matching your search query cunt:\n\n");
            result.append("```" + commandsFound + "```");
        }
        else{
            result.append("I found nothing matching that search query dumb fuck");
        }


        chan.sendMessage(result.toString()).queue();
    }

    /**
     * Searches the text of all images in RANDOM type commands for a term.
     */
    private void search(){
        String cleared = msg.replace("search ", "");
        String target = cleared.split(" ")[0];
        String search = cleared.replace(target + " ", "");
        DiscordCommand targetCommand = commands.get(getTrigger(target));
        ArrayList<DiscordImage> images = new ArrayList<>();
        int longestImage = 0;

        if(targetCommand == null || !targetCommand.getType().equals("RANDOM")){
            send("That command does not exist or is not of type RANDOM, take a look at the help! command next time cunt");
        }
        else{
            for(DiscordImage i : targetCommand.getImages()){
                if(i.getDesc().contains(search)){
                    images.add(i);
                    int imageLength = i.getImage().length();

                    if(imageLength > longestImage){
                        longestImage = imageLength;
                    }
                }
            }
        }

        if(images.size() > 0){
            int longestNum = String.valueOf(images.size()).length() + 1;
            String header = "```"
                    + "NUMBER"
                    + getSpaces(longestNum)
                    + "IMAGE"
                    + getSpaces(longestImage)
                    + "DESCRIPTION"
                    + "```";

            String result = "I found " + images.size() + " images in the " + targetCommand.getHelpName() + " command matching your search query cunt:\n\n";

            StringBuilder block = new StringBuilder(result + header + "```");

            int index = 1;
            for(DiscordImage i : images){
                String image = i.getImage();
                String desc = i.getDesc();
                if(desc.length() > 90){
                    desc = desc.substring(0, 90);
                }

                if(block.toString().length() > 1500){
                    send(block.toString() + "```");
                    block = new StringBuilder("```");
                }

                int numToImage = ("NUMBER".length() + longestNum) - (index + ".").length();
                int imageToDesc = longestImage - image.length() + "IMAGE".length();
                block
                        .append(index + ".")
                        .append(getSpaces(numToImage) + image)
                        .append(getSpaces(imageToDesc) + desc.trim())
                        .append("\n");

                index++;
            }
            send(block.toString() + "```");
        }
        else{
            send("There are no images in the " + targetCommand.getHelpName() + " command that match your search query of " + search);
        }
    }

    /**
     * Send a message to the current text channel
     *
     * @param msg String message to be sent
     */
    private void send(String msg){
        chan.sendMessage(msg).queue();
    }

    /**
     * Bans every user on the kill list and resets the list. Plays audio prior to firing.
     */
    private void executeOrder66(){

        // Pick between a tactical nuke or that wrinkly cunt
        String[] tracks = new String[]{
                "https://youtu.be/rV2l_WNd7Wo",
                "https://www.youtube.com/watch?v=rAfFSu-_3cA"
        };
        Random rand = new Random();
        String audio = tracks[rand.nextInt(tracks.length)];

        String message = "YES SIR, STAND BY.";

        // Delete the message which activated the command (snitches get stitches)
        deleteMessage(getLastMessage(0));

        // User is authorised
        if(isAuthorised(e.getAuthor())){

            // There are targets to be exterminated
            if(!targets.isEmpty()){

                // Implement the Response interface method to purge the kill list after the track finishes
                TrackEndListener.Response method = () -> new Thread(() -> purgeTargets()).start();
                TrackEndListener listener = new TrackEndListener(method, e.getGuild());

                // Play thr track
                playAudio(audio, listener);
            }

            // No targets
            else{
                message = "TARGET SECTORS ARE ALREADY CLEAR SIR, OVER";
            }
        }
        // User is not authorised, add him to the kill list too
        else{
            message = unauthReact(e.getAuthor());
        }

        User author = e.getAuthor();
        PrivateChannel pm = author.openPrivateChannel().complete();
        pm.sendMessage(message).queue();
        pm.close();
    }

    /**
     * Drop a tactical nuke on the server, ban everyone
     */
    private void nuke(){
        String audio = "https://youtu.be/rV2l_WNd7Wo";
        User author = e.getAuthor();
        deleteMessage(getLastMessage(0));

        // User is authorised
        if(isAuthorised(author)){
            // Implement the Response interface method to drop the nuke after the track finishes
            TrackEndListener.Response method = () -> new Thread(() -> deployNuke()).start();
            TrackEndListener listener = new TrackEndListener(method, e.getGuild());

            // Play the track
            playAudio(audio, listener);
        }

        // User unauthorised, add him to the kill list
        else{
            String message = unauthReact(author);
            send(author.getAsMention() + " " + message);
        }
    }

    /**
     * Unban the given user
     *
     * @param loser A banned user
     */
    private void unban(User loser){
        admin.unban(loser).complete();
    }

    /**
     * Deploy the nuke and ban everyone
     */
    private void deployNuke(){
        Guild server = e.getGuild();
        List<Member> targets = server.getMembers();
        User owner = server.getOwner().getUser();
        Role botRole = server.getSelfMember().getRoles().get(0);

        // All server members
        for(Member target : targets){
            User user = target.getUser();

            // Ignore self and the server owner
            if(user.equals(self) || user.equals(owner)){
                continue;
            }

            // Compare the role of the bot to the role of the target
            if(target.getRoles().size() > 0){
                Role userRole = target.getRoles().get(0);

                // Skip if the person cannot be banned by the bot
                if(botRole.getPosition() <= userRole.getPosition()){
                    continue;
                }
            }

            // Ban the user
            comfortBan(user);
            admin.ban(user, 7).complete();
            unban(user);
        }
    }
}
