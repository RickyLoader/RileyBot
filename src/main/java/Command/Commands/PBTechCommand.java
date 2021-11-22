package Command.Commands;

import Command.Structure.*;
import PBTech.PBTech;
import PBTech.Product;
import PBTech.Product.StarRating;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Embed URLs from PBTech
 */
public class PBTechCommand extends OnReadyDiscordCommand {
    private String starEmoteMention, halfStarEmoteMention, emptyStarEmoteMention;

    public PBTechCommand() {
        super("[pbtech url]", "Embed PBTech products!");
        setSecret(true);
    }

    @Override
    public void execute(CommandContext context) {
        Product product = PBTech.getProductByUrl(context.getLowerCaseMessage());

        // Failed to parse product details from URL
        if(product == null) {
            return;
        }

        context.getMessage().delete().queue(deleted -> displayProduct(context, product));
    }

    /**
     * Display the given product in a message embed with buttons to page through product images.
     *
     * @param context Command context
     * @param product Product to display
     */
    private void displayProduct(CommandContext context, Product product) {
        final String[] images = product.getImages();

        new CyclicalPageableEmbed<String>(
                context,
                Arrays.asList(images),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return new EmbedBuilder()
                        .setThumbnail(PBTech.LOGO)
                        .setColor(EmbedHelper.PBTECH_BLUE)
                        .setFooter(pageDetails);
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, String item) {
                String description = StringUtils.substring(product.getDescription(), 0, 100)
                        + "\n\n**Price**: " + product.getPrice().getPriceFormatted();

                // Rating not always present
                StarRating rating = product.getStarRating();
                if(rating != null) {
                    StringBuilder starEmotes = new StringBuilder();

                    // Always display the max stars, e.g 3/5 show 3 full stars and 2 empty stars
                    for(int i = 1; i <= StarRating.MAX_STARS; i++) {

                        // Add a full star as the rating is higher/equal to the current count
                        if(i <= rating.getStars()) {
                            starEmotes.append(starEmoteMention);
                        }

                        // Rating is lower, add a half star e.g 3.2/5 and current index is 4
                        else if(i - rating.getStars() < 1) {
                            starEmotes.append(halfStarEmoteMention);
                        }

                        // Add an empty star
                        else {
                            starEmotes.append(emptyStarEmoteMention);
                        }
                    }

                    final int reviews = rating.getReviews();

                    // e.g "Rating: <star emotes> 4.3/5.0 (75 reviews)"
                    description += "\n**Rating**: " + starEmotes.toString()
                            + " " + rating.getStars() + "/" + StarRating.MAX_STARS
                            + " (" + reviews + " " + (reviews == 1 ? "review" : "reviews") + ")";
                }

                builder
                        .setTitle(StringUtils.substring(product.getName(), 0, 50), product.getUrl())
                        .setDescription(description + "\n**Shopper**: " + context.getMember().getAsMention())
                        .setImage(item);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder(getPageDetails()).build();
            }

            @Override
            public String getPageDetails() {
                return images.length == 0 ? "No images for product" : "Photo: " + getPage() + "/" + getPages();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.starEmoteMention = emoteHelper.getStar().getAsMention();
        this.halfStarEmoteMention = emoteHelper.getHalfStar().getAsMention();
        this.emptyStarEmoteMention = emoteHelper.getEmptyStar().getAsMention();
    }

    @Override
    public boolean matches(String query, Message message) {
        return PBTech.isProductUrl(query);
    }
}
