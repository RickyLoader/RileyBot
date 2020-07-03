package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Audio.TrackEndListener;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Random;

public class ExecuteOrder66Command extends DiscordCommand {

    public ExecuteOrder66Command() {
        super("execute order 66", "Execute targets on the kill list!");
    }

    @Override
    public void execute(CommandContext context) {
        User author = context.getUser();
        System.out.println("\n\nReceived the order from " + author.getName() + ", Executing order 66...\n\n");

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

        // Delete the message which activated the command (snitches get stitches)
        context.getMessage().delete().complete();

        List<Member> targets = context.getTargets();

        // There are targets to be exterminated
        if(!targets.isEmpty()) {
            System.out.println(targets.size() + " targets found...\n\n");

            // Implement the Response interface method to purge the kill list after the track finishes
            TrackEndListener.Response method = () -> new Thread(() -> context.getMessageChannel().sendMessage(purgeTargets(targets, context)).queue()).start();
            TrackEndListener listener = new TrackEndListener(method, context.getGuild());

            // Play the track
            new DiscordAudioPlayer(context.getMember(), context.getGuild(), listener).play(audio);
            return;
        }

        if(!author.isBot()) {
            PrivateChannel pm = author.openPrivateChannel().complete();
            pm.sendMessage("TARGET SECTORS ARE ALREADY CLEAR SIR, OVER").queue();
            pm.close();
        }
    }

    /**
     * Ban all targets on the kill list and send a summary of the operation.
     */
    private String purgeTargets(List<Member> targets, CommandContext context) {
        int killed = 0;
        for(Member target : targets) {
            System.out.println("Attempting to ban " + target.getUser().getName());
            try {
                kick(target, context);
                killed++;
                System.out.println("Success!");
            }
            catch(Exception e) {
                e.printStackTrace();
                System.out.println(target.getUser().getName() + " (" + target.getAsMention() + ") was not banned");
            }
        }
        return killed + "/" + targets.size() + " targets EXTERMINATED";
    }


    /**
     * Kicks and sends the user a message with an invite back to the server, apologises for kicking them.
     *
     * @param loser The user to be kicked
     */
    private void kick(Member loser, CommandContext context) {
        User user = loser.getUser();
        if(user.isBot() || user.isFake()) {
            return;
        }
        PrivateChannel pc = user.openPrivateChannel().complete();
        String header = "__**I AM SORRY**__\n\n";
        pc.sendMessage(header + "```Sorry about that man, " +
                "I'm actually a really friendly bot but when someone tells me to do something I have to do it!" +
                "\n\nFeel free to jump on in again if you'd like, ask someone to pardon you though otherwise you'll get added back to" +
                " the emperor's list when you join!```" + context.getInvite()).complete();
        pc.close();
        context.getGuild().kick(loser).queue();
    }
}
