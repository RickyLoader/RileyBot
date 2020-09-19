package Command.Commands.Runescape.RS3.Stats;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageLoadingMessage;
import Network.ImgurManager;
import Network.NetworkRequest;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Build an image displaying a player's RS3 stats
 */
public class Hiscores extends ImageBuilder {
    private ImageLoadingMessage loading;
    private boolean timeout = false;

    public Hiscores(MessageChannel channel, EmoteHelper emoteHelper, String resourcePath, String fontName) {
        super(channel, emoteHelper, resourcePath, fontName);
    }

    /**
     * URL encode the player's name
     *
     * @param name Player name
     * @return URL encoded name
     */
    private String encodeName(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
            return name;
        }
    }

    /**
     * Get the URL for looking a player up on the RS3 hiscores
     *
     * @param type Account type
     * @param name Player name
     * @return Hiscores url
     */
    private String getURL(String type, String name) {
        return "https://secure.runescape.com/m=hiscore" + type + "/index_lite.ws?player=" + name;
    }

    /**
     * Get the URL for looking a player up on the RS3 hiscores
     *
     * @param name Player name
     * @return Normal hiscores URL
     */
    private String getNormalAccount(String name) {
        return getURL("", name);
    }

    /**
     * Get the URL for looking an ironman up on the RS3 hiscores
     *
     * @param name Player name
     * @return Ironman hiscores URL
     */
    private String getIronmanAccount(String name) {
        return getURL("_ironman", name);

    }

    /**
     * Get the URL for looking a hardcore ironman up on the RS3 hiscores
     *
     * @param name Player name
     * @return Hardcore ironman hiscores URL
     */
    private String getHardcoreAccount(String name) {
        return getURL("_hardcore_ironman", name);
    }

    /**
     * Look a player up on the RS3 hiscores and return an image displaying their skills
     *
     * @param nameQuery   Player name
     * @param helpMessage Help message to display in loading message
     * @param args        none
     */
    @Override
    public void buildImage(String nameQuery, String helpMessage, String... args) {
        String encodedName = encodeName(nameQuery);
        String defaultURL = getNormalAccount(encodedName);
        this.loading = new ImageLoadingMessage(
                getChannel(),
                getEmoteHelper(),
                "RS3 Hiscores lookup: " + nameQuery.toUpperCase(),
                "Give me a second, their website can be slow as fuck.",
                "https://vignette.wikia.nocookie.net/runescape2/images/a/a7/RuneScape_Companion_logo.png",
                helpMessage,
                new String[]{
                        "Player exists...",
                        "Checking account type...",
                        "Building image...",
                        "Uploading image..."
                }
        );
        loading.showLoading();

        String[] data = fetchPlayerData(encodedName);
        if(data == null) {
            if(timeout) {
                loading.failLoading("I wasn't able to connect to the " + EmbedHelper.embedURL("Hiscores", defaultURL));
                return;
            }
            loading.failLoading("That player " + EmbedHelper.embedURL("doesn't exist", defaultURL) + " cunt");
            return;
        }

        String[] clues = getClueScrolls(Arrays.copyOfRange(data, 137, 146));
        String[] stats = orderSkills(data);

        BufferedImage playerImage = buildImage(nameQuery, data[0], stats, clues);
        loading.completeStage();
        String url = ImgurManager.uploadImage(playerImage);
        loading.completeStage();
        loading.completeLoading(url, EmbedHelper.embedURL("View raw data", data[data.length - 1]));
    }

    /**
     * Build an image based on the player's stats, boss kills, and clue scrolls
     *
     * @param name   Player name
     * @param type   Player account type
     * @param skills Player stats
     * @param clues  Player clue scroll completions
     * @return Image showing player stats
     */
    private BufferedImage buildImage(String name, String type, String[] skills, String[] clues) {
        return null;
    }

    /**
     * Make a request to the OSRS hiscores API
     *
     * @param url Hiscores URL to query
     * @return Response from API
     */
    private String[] hiscoresRequest(String url) {
        String response = new NetworkRequest(url, false).get();
        if(response == null) {
            timeout = true;
            return null;
        }
        if(response.equals("err")) {
            return null;
        }
        response += "," + url;
        return response.replace("\n", ",").split(",");
    }

    /**
     * Order the skills skill data in to the in game order
     *
     * @param skills CSV from API
     * @return Skills in game order
     */
    private String[] orderSkills(String[] skills) {
        return new String[]{
                skills[4],     // ATTACK
                skills[13].equals("1") ? "10" : skills[13],    // HITPOINTS
                skills[46],    // MINING

                skills[10],    // STRENGTH
                skills[52],    // AGILITY
                skills[43],    // SMITHING

                skills[7],     // DEFENCE
                skills[49],    // HERBLORE
                skills[34],    // FISHING

                skills[16],    // RANGED
                skills[55],    // THIEVING
                skills[25],    // COOKING

                skills[19],    // PRAYER
                skills[40],    // CRAFTING
                skills[37],    // FIREMAKING

                skills[22],    // MAGIC
                skills[31],    // FLETCHING
                skills[28],    // WOODCUTTING

                skills[64],    // RUNECRAFT
                skills[58],    // SLAYER
                skills[61],    // FARMING

                skills[70],    // CONSTRUCTION
                skills[67],    // HUNTER
                skills[73],    // SUMMONING
                skills[76],    // DUNGEONEERING
                skills[79],    // DIVINATION
                skills[82],    // INVENTION
                skills[85],    // ARCHAEOLOGY
                skills[1].equals("0") ? "---" : skills[1],     // TOTAL
        };
    }

    /**
     * Format the skills clue scroll data to xN or x0
     *
     * @param data skills from API truncated to include only clue scrolls
     * @return Formatted clue scroll data
     */
    private String[] getClueScrolls(String[] data) {
        String[] clues = new String[6];
        int j = 0;
        for(int i = 1; i < data.length; i += 2) {
            int quantity = Integer.parseInt(data[i]);
            clues[j] = "x" + ((quantity == -1) ? "0" : data[i]);
            j++;
        }
        return clues;
    }

    /**
     * Fetch the CSV from the hiscores API
     *
     * @param name Player name
     * @return CSV data from API
     */
    private String[] fetchPlayerData(String name) {
        String[] normal = hiscoresRequest(getNormalAccount(name));

        if(normal == null) {
            return null;
        }

        loading.completeStage();

        normal[0] = null;
        loading.updateStage("Player exists, checking ironman hiscores");
        String[] iron = hiscoresRequest(getIronmanAccount(name));

        if(iron == null) {
            loading.completeStage("Player is a normal account!");
            return normal;
        }

        iron[0] = "iron";

        long ironXP = Long.parseLong(iron[2]);
        long normXP = Long.parseLong(normal[2]);

        if(normXP > ironXP) {
            loading.completeStage("Player is a de-ironed normal account!");
            return normal;
        }

        loading.updateStage("Player is an Ironman, checking Hardcore Ironman hiscores");
        String[] hardcore = hiscoresRequest(getHardcoreAccount(name));

        if(hardcore != null) {
            hardcore[0] = "hardcore";
            long hcXP = Long.parseLong(hardcore[2]);

            if(ironXP > hcXP) {
                loading.completeStage("Player was a Hardcore Ironman and died! What a loser!");
                return iron;
            }

            loading.completeStage("Player is a Hardcore Ironman!");
            return hardcore;
        }
        loading.completeStage("Player is an Ironman!");
        return iron;
    }
}
