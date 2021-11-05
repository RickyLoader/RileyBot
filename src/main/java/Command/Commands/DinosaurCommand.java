package Command.Commands;

import Command.Structure.*;
import Dinosaurs.DinosaurDatabase;
import Dinosaurs.DinosaurFacts;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fun facts about dinosaurs
 */
public class DinosaurCommand extends DiscordCommand {
    private static final String
            TRIGGER = "dino",
            RANDOM = "random",
            THUMBNAIL = "https://i.imgur.com/KxktlzK.png";

    private final DinosaurDatabase dinosaurDatabase;
    private final String footer;

    public DinosaurCommand() {
        super(
                TRIGGER,
                "Get some fun facts about dinosaurs!",
                TRIGGER + " " + RANDOM + "\n" + TRIGGER + " [dinosaur name]"
        );
        this.dinosaurDatabase = new DinosaurDatabase();
        this.footer = "Try: " + getHelpName().replace("\n", " | ");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        final String query = context.getLowerCaseMessage().replaceFirst(TRIGGER, "").trim();

        // No query provided
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        DinosaurFacts dinosaurFacts;

        // Get a random dinosaur
        if(query.equals(RANDOM)) {
            channel.sendTyping().queue();
            dinosaurFacts = dinosaurDatabase.getRandomDinosaurFacts();
        }

        // Search dinosaurs
        else {
            final ArrayList<String> searchResults = dinosaurDatabase.searchDinosaurNames(query);

            // Single result
            if(searchResults.size() == 1) {
                channel.sendTyping().queue();
                dinosaurFacts = dinosaurDatabase.getDinosaurFactsByName(searchResults.get(0));
            }

            // Show search results
            else {
                showSearchResults(context, query, searchResults);
                return;
            }
        }

        // Failed to parse facts
        if(dinosaurFacts == null) {
            channel.sendMessage(
                    context.getMember().getAsMention()
                            + " I am so sorry, I ran in to a catastrophic error in the mainframe while plundering"
                            + " through those dinosaur facts and I am unable to provide you with this information."
            ).queue();
            return;
        }

        showDinosaurFacts(context, dinosaurFacts);
    }

    /**
     * Display the dinosaur name search results in a pageable message
     *
     * @param context       Command context
     * @param query         Query used to find the search results
     * @param searchResults Dinosaur name search results
     */
    private void showSearchResults(CommandContext context, String query, ArrayList<String> searchResults) {
        new PageableTableEmbed<String>(
                context,
                searchResults,
                THUMBNAIL,
                "Dinosaur Search Results: " + query,
                null,
                footer,
                new String[]{"Name"},
                5
        ) {
            @Override
            public String getNoItemsDescription() {
                return "Nothing by that name ever wobbled around on this earth!";
            }

            @Override
            public String[] getRowValues(int index, String item, boolean defaultSort) {
                return new String[]{item};
            }

            @Override
            public void sortItems(List<String> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance<String>(StringUtils.capitalize(query), defaultSort) {
                    @Override
                    public String getString(String o) {
                        return o;
                    }
                });
            }
        }.showMessage();
    }

    /**
     * Display the given dinosaur facts in a pageable message.
     * Allow paging through images of the dinosaur.
     *
     * @param context       Command context
     * @param dinosaurFacts Dinosaur facts to display
     */
    private void showDinosaurFacts(CommandContext context, DinosaurFacts dinosaurFacts) {
        new CyclicalPageableEmbed<String>(
                context,
                dinosaurFacts.getImages(),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return new EmbedBuilder()
                        .setTitle(
                                dinosaurFacts.getName()
                                        + " (" + StringUtils.capitalize(dinosaurFacts.getType()) + ")",
                                dinosaurFacts.getDatabaseUrl()
                        )
                        .setDescription(buildDescription())
                        .setThumbnail(THUMBNAIL)
                        .setColor(EmbedHelper.ORANGE)
                        .setFooter(pageDetails);
            }

            /**
             * Build the description to use in the dinosaur message embed.
             * This contains the basic overview of the dinosaur.
             *
             * @return Dinosaur message embed description
             */
            private String buildDescription() {
                String description = "**Diet**: " + StringUtils.capitalize(dinosaurFacts.getDiet())
                        + "\n**Period**: " + dinosaurFacts.getPeriod();

                // Not all dinosaurs will have trivia
                final ArrayList<String> trivia = dinosaurFacts.getTrivia();

                // Add the list of trivia
                if(trivia != null && !trivia.isEmpty()) {
                    StringBuilder triviaBuilder = new StringBuilder();

                    for(int i = 0; i < trivia.size(); i++) {
                        triviaBuilder.append(i + 1).append(".");

                        // Don't add too much trivia, break after adding the next number to indicate there is still more
                        if(triviaBuilder.length() > 200) {
                            triviaBuilder.append("..");
                            break;
                        }

                        triviaBuilder.append(" ").append(trivia.get(i));

                        // Don't add new line to final item
                        if(i < trivia.size() - 1) {
                            triviaBuilder.append("\n");
                        }
                    }

                    description += "\n\n**Trivia**```" + triviaBuilder.toString() + "```";
                }

                return description;
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, String image) {
                builder.setImage(image);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder("No images available | " + footer).build();
            }

            @Override
            public String getPageDetails() {
                return "Image: " + getPage() + "/" + getPages() + " | " + footer;
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(TRIGGER);
    }
}
