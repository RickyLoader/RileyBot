package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.PageableTrademeListing;
import Trademe.Listing;
import Trademe.Trademe;
import net.dv8tion.jda.api.entities.Message;

/**
 * Take Trademe listing URLs and replace with an embed detailing the listing
 */
public class TrademeCommand extends DiscordCommand {
    private final Trademe trademe;

    public TrademeCommand() {
        super("[trademe url]", "Embed Trademe listings!");
        setSecret(true);
        this.trademe = new Trademe();

    }

    @Override
    public void execute(CommandContext context) {
        Listing listing = trademe.getListingByUrl(context.getMessageContent());
        if(listing == null) {
            return;
        }
        context.getMessage().delete().queue(deleted -> new PageableTrademeListing(context, listing).showMessage());
    }

    @Override
    public boolean matches(String query, Message message) {
        return message.getContentDisplay().matches(Trademe.TRADEME_URL);
    }
}
