package Command.Commands;

import Command.Structure.*;
import R34.Image;
import R34.R34ImageCollector;
import R34.Tag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * View cool pictures
 */
public class R34Command extends OnReadyDiscordCommand {
    private static final String
            TRIGGER = "r34",
            RANDOM = "random",
            IMAGE_SEARCH_HELP = TRIGGER + " [tag]",
            TAGS = "tags",
            TAG_SEARCH_HELP = TRIGGER + " " + TAGS + " [query]";
    private final String footer;
    private Emote scoreEmote, failEmote;

    public R34Command() {
        super(
                TRIGGER,
                "View some neat images!",
                IMAGE_SEARCH_HELP
                        + "\n" + TAG_SEARCH_HELP
                        + "\n" + TRIGGER + " " + RANDOM
                        + "\n[image url]"
                        + "\n\nTags are exact and if misspelled will not find any images, try "
                        + TAG_SEARCH_HELP
                        + " to search tags."
        );
        this.footer = "Type: " + getTrigger() + " for help";
    }

    @Override
    public void execute(CommandContext context) {
        final String messageContent = context.getLowerCaseMessage();
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

        // Embed from URL
        if(R34ImageCollector.isImageUrl(messageContent)) {
            Image image = R34ImageCollector.searchImagesByUrl(messageContent);
            Message message = context.getMessage();

            // Unable to locate image
            if(image == null) {
                message.addReaction(failEmote).queue();
                return;
            }
            message.delete().queue(deleted -> channel.sendMessage(
                    buildImageEmbed(image, "R34 User Post: " + member.getEffectiveName())
            ).queue());
            return;
        }

        String query = messageContent.replaceFirst(getTrigger(), "").trim();

        // No query
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        // Search tags
        if(query.startsWith(TAGS)) {
            query = query.replaceFirst(TAGS, "").trim();

            // No tag query provided
            if(query.isEmpty()) {
                channel.sendMessage("```" + TAG_SEARCH_HELP + "```").queue();
                return;
            }

            // More than one tag provided
            if(query.split(" ").length > 1) {
                channel.sendMessage(
                        member.getAsMention()
                                + " You've provided too many tags,"
                                + " tags are whitespace separated and only 1 can be searched at a time!"
                ).queue();
                return;
            }

            channel.sendTyping().queue();
            ArrayList<Tag> tags = R34ImageCollector.searchTags(query);

            // Result is null if too may tags are found for the query (website doesn't show them)
            if(tags == null) {
                channel.sendMessage(
                        member.getAsMention() +
                                " Too many tags were found for your query, you'll need to narrow it down!"
                ).queue();
                return;
            }
            displayTags(context, tags, query);
        }

        // Get random image
        else if(query.equals(RANDOM)) {
            channel.sendTyping().queue();
            Image random = R34ImageCollector.getRandomImage();

            // May fail to find a random image
            if(random == null) {
                channel.sendMessage(member.getAsMention() + "I'm sorry, I have failed you.").queue();
                return;
            }

            channel.sendMessage(buildImageEmbed(random, "R34 Random")).queue();
        }

        // Search images
        else {
            channel.sendTyping().queue();
            ArrayList<Image> images = R34ImageCollector.searchImagesByTag(query);

            // API error
            if(images == null) {
                channel.sendMessage(
                        member.getAsMention()
                                + " I encountered a **catastrophic** error while processing your query"
                ).queue();
                return;
            }
            displayImages(context, images, query);
        }
    }

    /**
     * Build a message embed displaying the given image
     *
     * @param image Image to display
     * @param title Title to use in the embed
     * @return Message embed displaying image
     */
    private MessageEmbed buildImageEmbed(Image image, String title) {
        return new EmbedBuilder()
                .setThumbnail(R34ImageCollector.THUMBNAIL)
                .setImage(image.getImageUrl())
                .setColor(EmbedHelper.PURPLE)
                .setTitle(title, image.getPostUrl())
                .setDescription(buildImageDescription(image))
                .setFooter(footer)
                .build();
    }

    /**
     * Display the given list of images in a pageable message
     *
     * @param context Command context
     * @param images  List of images found for the given query
     * @param query   Query used to find the images
     */
    private void displayImages(CommandContext context, ArrayList<Image> images, String query) {
        new CyclicalPageableEmbed<Image>(
                context,
                images,
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return new EmbedBuilder()
                        .setColor(EmbedHelper.PURPLE)
                        .setThumbnail(R34ImageCollector.THUMBNAIL)
                        .setFooter(pageDetails + " | " + footer);
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, Image image) {
                builder
                        .setTitle("R34 Image Search: " + query, image.getPostUrl())
                        .setDescription(buildImageDescription(image))
                        .setImage(image.getImageUrl());
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder("No images found")
                        .setTitle("R34 Image Search: No results")
                        .setDescription(
                                "There were no images found with the query: **" + query + "**\n\nTry: **"
                                        + TAG_SEARCH_HELP
                                        + "** to find valid tags!"
                        )
                        .setColor(EmbedHelper.RED)
                        .build();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }

    /**
     * Build the description to use in a message embed displaying the given image.
     * Display uploader, date, etc.
     *
     * @param image Image to build description for
     * @return Image description
     */
    private String buildImageDescription(Image image) {
        return "**Posted by**: "
                + image.getUploader()
                + " (" + new SimpleDateFormat("dd/MM/yyyy").format(image.getDatePosted()) + ")"
                + "\n**Score**: " + scoreEmote.getAsMention() + " " + image.getScore()
                + "\n\n**Tags**: ```" + image.getTagSummary(3) + "```";
    }

    /**
     * Display the given list of image tags in a pageable message.
     *
     * @param context Command context
     * @param tags    List of tags found for the given query
     * @param query   Query used to find the tags
     */
    private void displayTags(CommandContext context, ArrayList<Tag> tags, String query) {
        new PageableTableEmbed<Tag>(
                context,
                tags,
                R34ImageCollector.THUMBNAIL,
                "R34 Tag Search: " + query,
                "Try finding images with these tags: **" + IMAGE_SEARCH_HELP + "**",
                footer,
                new String[]{
                        "Posts",
                        "Name",
                        "Type"
                },
                5
        ) {
            @Override
            public String getNoItemsDescription() {
                return "There were no tags matching your query, try being less specific!";
            }

            @Override
            public String[] getRowValues(int index, Tag tag, boolean defaultSort) {
                return new String[]{
                        String.valueOf(tag.getTotalPosts()),
                        tag.getName().replaceAll("_","\\\\_"),
                        tag.getType()
                };
            }

            @Override
            public void sortItems(List<Tag> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance<Tag>(query, defaultSort) {
                    @Override
                    public String getString(Tag o) {
                        return o.getName();
                    }
                });
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger()) || R34ImageCollector.isImageUrl(query);
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.scoreEmote = emoteHelper.getRedditUpvote();
        this.failEmote = emoteHelper.getFail();
    }
}
