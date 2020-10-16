package Command.Commands;

import Bot.ResourceHandler;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Get details on the FBI most wanted list!
 */
public class FBICommand extends DiscordCommand {
    private final ArrayList<Fugitive> mostWanted;

    /**
     * Cache the FBI most wanted data
     */
    public FBICommand() {
        super("fbi", "Get details about the FBI's most wanted!");
        this.mostWanted = parseWanted();
    }

    /**
     * Get the most wanted data from the FBI json
     *
     * @return List of fugitives
     */
    private ArrayList<Fugitive> parseWanted() {
        System.out.println("Fetching FBI most wanted...");

        JSONArray wanted = new JSONObject(
                new ResourceHandler().getResourceFileAsString("/FBI/fbi_most_wanted.json")
        ).getJSONArray("wanted");

        ArrayList<Fugitive> fugitives = new ArrayList<>();
        for(int i = 0; i < wanted.length(); i++) {
            JSONObject fug = wanted.getJSONObject(i);
            String desc = fug.isNull("description") ?
                    "No case details available"
                    :
                    fug.getString("description")
                            .replace(";", "\n")
                            .replace(" - ", "\n")
                            .replace(", ", "\n");

            if(!fug.getJSONArray("subjects").isEmpty() && fug.getJSONArray("subjects").getString(0).toLowerCase().contains("seeking information")) {
                System.out.println(fug.getString("title"));
                continue;
            }
            fugitives.add(
                    new Fugitive(
                            fug.getString("title"),
                            fug.getString("sex"),
                            desc.length() > 200 ? desc.substring(0, 200) + "..." : desc,
                            fug.getString("url"),
                            fug.isNull("reward_text") ? "No reward information available" : fug.getString("reward_text"),
                            fug.getJSONArray("images").getJSONObject(0).getString("original")
                    )
            );
        }
        return fugitives;
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessageChannel().sendMessage(
                getFugitiveEmbed(mostWanted.get(new Random().nextInt(mostWanted.size())))
        ).queue();
    }

    /**
     * Get a fugitive message embed displaying fugitive info
     *
     * @param fug Fugitive to display
     * @return Message embed displaying fugitive info
     */
    private MessageEmbed getFugitiveEmbed(Fugitive fug) {
        String logo = "https://i.imgur.com/uapkFjS.png";
        return new EmbedBuilder()
                .setThumbnail(logo)
                .setColor(Color.YELLOW)
                .setFooter("Have you seen this " + (fug.getSex().equals("Male") ? "man" : "woman") + "? | Try: " + getHelpName(), logo)
                .setTitle("FBI Most Wanted: " + fug.getName())
                .setImage(fug.getImage())
                .addField("Reward", EmbedHelper.embedURL(fug.getReward(), fug.getURL()), false)
                .addField("Charges", fug.getCharges(), false)
                .build();
    }

    /**
     * Hold data on a Fugitive
     */
    private static class Fugitive {
        private final String name, sex, reward, charges, url, image;

        /**
         * Create a fugitive
         *
         * @param name    Name
         * @param sex     Sex - Male/Female
         * @param charges New line separated charges
         * @param url     URL to FBI page on fugitive
         * @param reward  Reward information
         * @param image   Image URL
         */
        public Fugitive(String name, String sex, String charges, String url, String reward, String image) {
            this.name = name;
            this.sex = sex;
            this.charges = charges;
            this.url = url;
            this.reward = reward;
            this.image = image;
        }

        /**
         * Get the sex of the fugitive
         *
         * @return Sex - Male/Female
         */
        public String getSex() {
            return sex;
        }

        /**
         * Get the URL to the FBI page on the fugitive
         *
         * @return URL to FBI page
         */
        public String getURL() {
            return url;
        }

        /**
         * Get the fugitive name
         *
         * @return Fugitive name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the fugitive image
         *
         * @return Fugitive image
         */
        public String getImage() {
            return image;
        }

        /**
         * Get the reward information
         *
         * @return Reward information
         */
        public String getReward() {
            return reward;
        }

        /**
         * Get the case charges
         *
         * @return Fugitive charges
         */
        public String getCharges() {
            return charges;
        }
    }
}
