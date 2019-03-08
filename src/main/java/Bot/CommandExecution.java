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

public class CommandExecution{

    private GuildMessageReceivedEvent e;
    private MessageChannel chan;
    private DiscordCommand c;
    private User self;
    private GuildController admin;
    private String msg;


    public CommandExecution(GuildMessageReceivedEvent e, DiscordCommand c){
        this.e = e;
        this.chan = e.getChannel();
        this.c = c;
        this.self = e.getGuild().getSelfMember().getUser();
        this.admin = e.getGuild().getController();
        this.msg = e.getMessage().getContentDisplay();

    }

    private void brewCount(){
        String num = e.getMessage().getContentDisplay().split(" ")[0];
        String author = e.getAuthor().getAsMention();
        String result = "Hey @everyone my name is " + author + " and I have had " + num + " brews!";
        chan.sendMessage(result).queue();
    }

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

    private String getInvite(){
        Invite invite = e.getChannel().createInvite().complete();
        String result = invite.getURL();
        return result;
    }

    private void alcoholic(){
        String user = e.getAuthor().getAsMention();
        String result = "Hey @everyone my name is " + user + " and I am an alcoholic";
        chan.sendMessage(result).queue();
    }

    private void invite(){
        send(getInvite());
    }

    private void randomName(){
        Random rand = new Random();
        int attempts = 0;
        String name = "";
        int maxLength = 15;

        while(attempts <= 2){
            String word = c.getLink();
            if(name.length() + word.length() <= maxLength){
                name += word;
            }
            attempts++;
        }
        int charToMax = (maxLength - name.length());
        if(charToMax > 0){
            name += rand.nextInt(10 ^ charToMax);
        }
        chan.sendMessage(name).queue();
    }

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


    private Message getLastMessage(int index){
        Message m = chan.getHistory().retrievePast(index + 1).complete().get(index);
        return m;
    }

    private void tagImage(){
        String tag = e.getMessage().getContentDisplay().toLowerCase().replace("tag: ", "");
        Message last = getLastMessage(1);
        String URL = last.getContentDisplay();
        String description = "A meme that was tagged as " + tag + ".";
        DiscordCommand c = new DiscordCommand("LINK", tag, description);
        c.setImage(URL);
        DiscordCommand.addCommand(c, commands);
    }

    private void killList(){
        String summary;
        User author = e.getAuthor();
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
        else{
            send(unauthReact(author));
        }
    }

    private void pardonAll(){
        User author = e.getAuthor();
        String msg = "There are no targets bro fuck off";

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
        else{
            send(unauthReact(author));
        }
    }

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

    private void deleteMessage(Message m){
        chan.deleteMessageById(m.getId()).complete();
    }

    private void play(){
        String audio = e.getMessage().getContentDisplay().replace("!play ", "");
        deleteMessage(getLastMessage(0));
        playAudio(audio, new TrackEndListener(null, e.getGuild()));
    }

    private void playAudio(String audio, TrackEndListener listener){
        VoiceChannel vc = e.getMember().getVoiceState().getChannel();
        if(vc==null){
            send(e.getAuthor().getAsMention() + " join a voice channel first cunt");
            return;
        }
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer player = playerManager.createPlayer();
        player.addListener(listener);
        e.getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
        e.getGuild().getAudioManager().openAudioConnection(vc);

        playerManager.loadItem(audio, new AudioLoadResultHandler(){
            @Override
            public void trackLoaded(AudioTrack audioTrack){
                System.out.println(audioTrack.getIdentifier()+" loaded!");
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

    private boolean isAuthorised(User killer){
        boolean userPerm = admin.getGuild().getMember(killer).hasPermission(BAN_MEMBERS);
        boolean botPerm = admin.getGuild().getMember(self).hasPermission(BAN_MEMBERS);
        return userPerm && botPerm;
    }

    private String unauthReact(User author){
        DiscordUser authorObj = findUser(author.getAsMention());
        String reply;
        if(!targets.contains(author)){
            if(authorObj.markUser()){
                targets.add(author);
                reply = author.getAsMention() + " You think I wouldn't notice you aren't authorised to do that?\nNow YOU'RE on the kill list cunt";
            }
            else{
                reply = author.getAsMention() + " You're not authorised to do that cunt";
            }
        }
        else{
            reply = author.getAsMention() + " Normally this is where i'd add YOU to the kill list for trying to do something you're not allowed to do. " +
                    "\nI'm not actually going to do to that though because it turns out you're already on it CUNT";
        }
        return reply;
    }

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

    private void mark(){
        String targetName = msg.replace("mark @", "");
        User target = getServerUser(targetName);
        User author = e.getAuthor();
        String reply;
        String mention = author.getAsMention();

        if(isAuthorised(author)){
            if(target != null){
                if(!targets.contains(target)){
                    DiscordUser authorObj = findUser(target.getAsMention());
                    if(authorObj.markUser()){
                        targets.add(target);
                        reply = mention + " " + targetName + " has been added to the kill list! Congratulations sir!";
                    }
                    else{
                        reply = mention + " I was unable to add " + targetName + " to the kill list, but now he knows you want to dumbass";
                    }
                }
                else{
                    reply = author.getAsMention() + " " + target.getName() + " is already on the kill list, now he knows cunt, better execute order 66 real quick";
                }
            }
            else{
                reply = author.getAsMention() + " that cunt doesn't exist, fuck off";
            }
        }
        else{
            reply = unauthReact(author);
        }
        send(reply);
    }

    private void logTTS(String msg,User author) {
        MessageChannel log = e.getGuild().getTextChannelsByName("tts-log", true).get(0);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setDescription("**Author**: "+author.getAsMention() + " **Submitted At**: "+new Date());
        builder.setTitle("**TTS Log**");
        builder.addField("Contents:",msg,false);
        log.sendMessage(builder.build()).queue();
    }


    private void chatTime(){
        try{
            String base = "https://talk.moustacheminer.com/api/gen.wav?dectalk=";
            String content = msg.replaceFirst(".", "");
            String url = URLEncoder.encode(content, "UTF-8");
            String audio = base + url;
            logTTS(content,e.getAuthor());
            playAudio(audio,null);
            deleteMessage(getLastMessage(0));
        }
        catch(UnsupportedEncodingException e){
            System.out.println("cunt");
        }
    }

    private void pardon(){
        String targetName = msg.replace("pardon @", "");
        User target = getServerUser(targetName);
        User author = e.getAuthor();
        String reply;
        String mention = "@everyone ";

        if(isAuthorised(author)){
            if(targets.contains(target)){
                DiscordUser targetObj = findUser(target.getAsMention());
                if(targetObj.pardonUser()){
                    targets.remove(target);
                    reply = mention + target.getName() + " was successfully pardoned, congratulations sir";
                }
                else{
                    reply = mention + target.getName() + " was not pardoned";
                }
            }
            else{
                reply = author.getAsMention() + " Nobody by that name is on the kill list cunt";
            }
        }
        else{
            reply = unauthReact(author);
        }
        send(reply);
    }

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

    private List<User> getBans(){
        Guild server = e.getGuild();
        return server.getBanList().complete().stream().map(ban -> ban.getUser()).collect(Collectors.toList());
    }

    private void adminCommand(){
        boolean ban = !msg.contains("unban");
        String target;
        User user;
        if(ban){
            target = msg.replace("ban @", "");
            user = getServerUser(target);
        }
        else{
            target = msg.replace("unban ", "");
            user = getBannedUser(target);
        }

        Guild server = admin.getGuild();
        String mention = e.getAuthor().getAsMention();
        String result = mention + " HOW DARE YOU DISRESPECT ME, I'M NOT ALLOWED TO DO THAT";
        boolean userPerm = server.getMember(e.getAuthor()).hasPermission(BAN_MEMBERS);
        boolean botPerm = server.getSelfMember().hasPermission(BAN_MEMBERS);

        if(userPerm && botPerm){
            if(user == null){
                result = mention + " That cunt doesn't exist you fucking idiot.";
            }
            else{
                try{
                    result = adminAction(ban, user);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        chan.sendMessage(result).queue();
    }

    public User getBannedUser(String target){
        Guild server = e.getGuild();
        User result = null;
        List<User> bannedUsers = getBans();
        for(User u : bannedUsers){
            if(u.getName().equalsIgnoreCase(target)){
                result = u;
                break;
            }
        }
        return result;
    }

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

    private void updateAlias(){
        msg = msg.toLowerCase();
        String desiredAlias = msg.split(":alias ")[1];
        String id = e.getAuthor().getAsMention();
        DiscordUser targetUser = DiscordBot.findUser(id);
        String old = targetUser.getAlias();

        if(old.equals(desiredAlias)){
            chan.sendMessage(id + " " + desiredAlias + " is already your alias cunt, fuck off").queue();
        }
        else if(targetUser.setAlias(desiredAlias)){
            chan.sendMessage(id + " Your alias was successfully updated from " + old + " to " + targetUser.getAlias() + "!").queue();
            DiscordBot.refactorUsers();
        }
        else{
            chan.sendMessage(id + " I couldn't update your alias cunt").queue();
        }
    }

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

    private void grandExchange(){
        if((System.currentTimeMillis() - exchangeData.getLastCalled()) > 900000){
            exchangeData = new ExchangeData();
        }
        chan.sendMessage(exchangeData.requestItem(e.getMessage().getContentDisplay().split("!")[1])).queue();
    }

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
            switch(c.getType()){
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

    private void send(String msg){
        chan.sendMessage(msg).queue();
    }

    private void executeOrder66(){
        String[] tracks = new String[]{
                "https://youtu.be/rV2l_WNd7Wo",
                "https://www.youtube.com/watch?v=rAfFSu-_3cA"
        };
        Random rand = new Random();
        String audio = tracks[rand.nextInt(tracks.length)];
        System.out.println(audio);
        String message = "YES SIR, STAND BY.";

        deleteMessage(getLastMessage(0));
        if(isAuthorised(e.getAuthor())){
            if(!targets.isEmpty()){
                TrackEndListener.Response method = () -> new Thread(() -> purgeTargets()).start();
                TrackEndListener listener = new TrackEndListener(method, e.getGuild());
                playAudio(audio, listener);
            }
            else{
                message = "TARGET SECTORS ARE ALREADY CLEAR SIR, OVER";
            }
        }
        else{
            message = unauthReact(e.getAuthor());
        }
        User author = e.getAuthor();
        PrivateChannel pm = author.openPrivateChannel().complete();
        pm.sendMessage(message).queue();
        pm.close();
    }

    private void nuke(){
        String audio = "https://youtu.be/rV2l_WNd7Wo";
        User author = e.getAuthor();
        deleteMessage(getLastMessage(0));
        if(isAuthorised(author)){
            TrackEndListener.Response method = () -> new Thread(() -> deployNuke()).start();
            TrackEndListener listener = new TrackEndListener(method, e.getGuild());
            playAudio(audio, listener);
        }
        else{
            String message = unauthReact(author);
            send(author.getAsMention() + " " + message);
        }
    }

    private void unban(User loser){
        admin.unban(loser).complete();
    }

    private void deployNuke(){
        Guild server = e.getGuild();
        List<Member> targets = server.getMembers();
        User owner = server.getOwner().getUser();
        Role botRole = server.getSelfMember().getRoles().get(0);

        for(Member target : targets){
            User user = target.getUser();
            if(user.equals(self) || user.equals(owner)){
                continue;
            }
            if(target.getRoles().size() > 0){
                Role userRole = target.getRoles().get(0);
                if(botRole.getPosition() <= userRole.getPosition()){
                    continue;
                }
            }
            comfortBan(user);
            admin.ban(user, 7).complete();
            unban(user);
        }

    }
}
