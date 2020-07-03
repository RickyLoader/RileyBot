package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class SawCommand extends DiscordCommand {

    public SawCommand() {
        super("saw", "The choice is yours!");
    }

    @Override
    public void execute(CommandContext context) {
        new DiscordAudioPlayer(context.getMember(), context.getGuild()).play("https://www.youtube.com/watch?v=4VB2vjfNp_o");
    }
}
