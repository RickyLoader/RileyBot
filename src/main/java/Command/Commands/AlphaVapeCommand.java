package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.VapeStoreCommand;
import Vape.Product;
import Vape.Vapo;

/**
 * View the amazing alpha vape
 */
public class AlphaVapeCommand extends DiscordCommand {
    private static final long ALPHA_ID = 9757349896L;

    public AlphaVapeCommand() {
        super("alpha", "View the amazing alpha vape!");
    }

    @Override
    public void execute(CommandContext context) {
        Vapo vapo = Vapo.getInstance();
        Product alphaVape = vapo.getProductById(ALPHA_ID, false);
        if(alphaVape == null) {
            context.getMessageChannel().sendMessage(
                    context.getMember().getAsMention() + " I am so sorry, the **ALPHA VAPE** is nowhere to be found!"
            ).queue();
            return;
        }
        VapeStoreCommand.showProduct(context, alphaVape, vapo, "Try: " + getTrigger());
    }
}
