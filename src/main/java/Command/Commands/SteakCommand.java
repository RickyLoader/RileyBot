package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.EmbedHelper;
import Command.Structure.SelfieCommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

/**
 * Put the user's image in a selfie with Steak!
 */
public class SteakCommand extends SelfieCommand {
    private final Quote[] quotes;

    public SteakCommand() {
        super(
                "steak",
                "Get a photo with steak!",
                new SelfieManager(
                        "/LOL/steak.png",
                        "https://i.imgur.com/KXnIE3C.png",
                        new SelfieManager.ProfileDimensions(
                                180,
                                50,
                                240,
                                240
                        )
                )
        );
        this.quotes = getQuotes();
    }

    /**
     * Get an array of quotes from/about Steak
     *
     * @return Array of Steak quotes
     */
    private Quote[] getQuotes() {
        final String steak = "Steak";
        return new Quote[]{
                new Quote("I wanted a short name and steak is my favourite food", steak),
                new Quote("Yeah so like Maple's gonna carry me no matter what", steak),
                new Quote("It's pretty easy yeah", steak),
                new Quote("I think we're gonna make it through like, I don't know, I think", steak),
                new Quote("We have pretty much confident in this tournament", steak),
                new Quote(
                        "Yeah but, hmm actually I think.. It's possible.. well the.. second to six team",
                        steak
                ),
                new Quote(
                        "Everybody has like the same skill set yeah, except for SKT1, yeah they're the best",
                        steak
                ),
                new Quote(
                        "If you count the whole region I think we might not even be better than wildcard",
                        steak
                ),
                new Quote(
                        "Uh we prepared a lot, I would say enough, yeah but um our gameplay was pretty bad",
                        steak
                ),
                new Quote(
                        "Like our communication and our team play was really really bad, so that's why we lost",
                        steak
                ),
                new Quote(
                        "I'd say we're really really good, and I hope we can show you that in a few days, yeah",
                        steak
                ),
                new Quote(
                        "Um thank you for your support and I hope we do good in the next few games, yeah",
                        steak
                ),
                new Quote(
                        "Aphromoo is kinda famous for his hair, yeah but I think my hair crushes his",
                        steak
                ),
                new Quote(
                        "They just kinda leave Steak to do whatever, most of the time he's just dead",
                        "Doublelift"
                ),
                new Quote(
                        "Best hair award, there we go, for NA, that was me. I don't know what Steak's won.. but",
                        "Aphromoo"
                ),
                new Quote(
                        "I think strategicca..strateg..whatever. Strategy yeah, yeah strategy isn't that much from the coach",
                        steak
                ),
                new Quote(
                        "Uh it's pretty easy to go in the game knowing that i'm gonna be the only one to troll our team",
                        steak
                )
        };
    }

    /**
     * Get a random quote from/about Steak
     * The quote text is formatted in italics with quotation marks and the author is bold
     *
     * @return Steak quote
     */
    private String getRandomQuote() {
        Quote quote = quotes[new Random().nextInt(quotes.length)];
        return "*\"" + quote.getText() + "\"* - **" + quote.getAuthor() + "**";
    }

    @Override
    public EmbedBuilder getEmbedBuilder(CommandContext context) {
        return getDefaultEmbedBuilder()
                .setColor(EmbedHelper.PURPLE)
                .setTitle(context.getMember().getEffectiveName() + " - Photo with Steak")
                .setDescription("The **GREATEST** League of Legends player in LCS history!\n\n" + getRandomQuote());
    }

    private static class Quote {
        private final String text, author;

        /**
         * Create a quote
         *
         * @param text   Quote text
         * @param author Quote author
         */
        public Quote(String text, String author) {
            this.text = text;
            this.author = author;
        }

        /**
         * Get the name of the author of the quote
         *
         * @return Quote author
         */
        public String getAuthor() {
            return author;
        }

        /**
         * Get the quote text
         *
         * @return Quote text
         */
        public String getText() {
            return text;
        }
    }
}
