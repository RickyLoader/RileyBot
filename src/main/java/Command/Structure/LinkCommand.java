package Command.Structure;

public class LinkCommand extends DiscordCommand {
    private final String link;

    /**
     * Create a link command
     *
     * @param trigger Trigger to call link command
     * @param desc    Description of command
     * @param link    Link to be sent when command is called
     */
    public LinkCommand(String trigger, String desc, String link) {
        super(trigger, desc);
        this.link = link;
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessageChannel().sendMessage(link).queue();
    }
}
