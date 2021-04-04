package Command.Commands;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.EmbedHelper;
import Command.Structure.EmbedLoadingMessage;
import Command.Structure.MultiLookupCommand;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import Steam.AppInfo;
import Steam.Application;
import Steam.SteamStore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

import static Bot.GlobalReference.STEAM_STORE;

/**
 * Pick a random Steam game that all users own
 */
public class SteamGameCommand extends MultiLookupCommand {

    public SteamGameCommand() {
        super(
                "steamgame",
                "Pick a random Steam game that all mentioned users own!",
                "Get your Steam64 Id from: https://steamidfinder.com/",
                17
        );
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.STEAM_ID);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.STEAM_ID, channel, user);
    }

    @Override
    public void processNames(ArrayList<UserSavedData> savedSteamIds, CommandContext context) {
        String footer = "Type: " + getTrigger() + " for help";
        EmbedLoadingMessage loadingMessage = getEmbedLoadingMessage(context, savedSteamIds, footer);
        loadingMessage.showLoading();
        HashMap<Integer, Integer> gameFrequencyMap = new HashMap<>();
        ArrayList<User> noGames = new ArrayList<>();

        for(UserSavedData userSavedData : savedSteamIds) {
            ArrayList<Integer> userGames = fetchUserGameIds(userSavedData.getData());
            if(userGames.isEmpty()) {
                loadingMessage.failStage("No games found!");
                noGames.add(userSavedData.getUser());
                continue;
            }
            for(int gameId : userGames) {
                gameFrequencyMap.put(gameId, gameFrequencyMap.getOrDefault(gameId, 0) + 1);
            }
            loadingMessage.completeStage(userGames.size() + " games found!");
        }
        ArrayList<Integer> commonGameIds = gameFrequencyMap
                .keySet()
                .stream()
                .filter(gameId -> gameFrequencyMap.get(gameId) == savedSteamIds.size() - noGames.size())
                .collect(Collectors.toCollection(ArrayList::new));

        MessageChannel channel = context.getMessageChannel();
        if(commonGameIds.isEmpty()) {
            loadingMessage.failLoading("I didn't find any common games between those users!");
            return;
        }
        loadingMessage.completeStage(commonGameIds.size() + " common games");
        Application game = getRandomGame(commonGameIds, loadingMessage);
        loadingMessage.completeStage("Game info fetched!");
        ArrayList<User> users = savedSteamIds
                .stream()
                .map(UserSavedData::getUser)
                .collect(Collectors.toCollection(ArrayList::new));

        MessageEmbed gameEmbed = buildRandomGameEmbed(game, commonGameIds.size(), noGames, users, footer);
        loadingMessage.completeLoading(gameEmbed);
    }

    /**
     * Get the loading message to use while loading the given steam libraries
     *
     * @param context       Command context
     * @param savedSteamIds Steam user ids of libraries to fetch
     * @param footer        Footer to use in loading message
     * @return Loading message
     */
    private EmbedLoadingMessage getEmbedLoadingMessage(CommandContext context, ArrayList<UserSavedData> savedSteamIds, String footer) {
        ArrayList<String> loadingSteps = new ArrayList<>();
        for(UserSavedData userSavedData : savedSteamIds) {
            loadingSteps.add("Fetching " + userSavedData.getUser().getName() + " Steam library...");
        }
        loadingSteps.add("Checking common games...");
        loadingSteps.add("Fetching game info...");

        return new EmbedLoadingMessage(
                context.getMessageChannel(),
                context.getEmoteHelper(),
                "Steam Random",
                "Let me fetch those Steam games.",
                SteamStore.STEAM_LOGO,
                footer,
                loadingSteps.toArray(new String[0])
        );
    }

    /**
     * Build an embed detailing the random game chosen from the common games owned by the given users
     *
     * @param game        Randomly chosen common game
     * @param commonGames Number of common games shared between the users
     * @param noGames     Users with no games
     * @param users       Users whose common library the random game was chosen from
     * @param footer      Footer to use in embed
     * @return Message embed detailing random game chosen
     */
    private MessageEmbed buildRandomGameEmbed(Application game, int commonGames, ArrayList<User> noGames, ArrayList<User> users, String footer) {
        AppInfo appInfo = game.getAppInfo();
        return new EmbedBuilder()
                .setTitle("Steam Random | " + appInfo.getSummary(), appInfo.getStoreUrl())
                .setThumbnail(SteamStore.STEAM_LOGO)
                .setImage(game.getThumbnail())
                .setDescription(buildDescription(game, commonGames, noGames, users))
                .setColor(EmbedHelper.BLUE)
                .addField("Concurrent Players", game.formatConcurrentPlayers(), true)
                .setFooter(footer)
                .build();
    }

    /**
     * Take a list of users and return a comma separated String of user mentions
     *
     * @param users Users to mention
     * @return Comma separated String of user mentions
     */
    private String getUsersAsMentions(ArrayList<User> users) {
        StringBuilder userMentions = new StringBuilder();
        for(int i = 0; i < users.size(); i++) {
            userMentions.append(users.get(i).getAsMention());
            if(i < users.size() - 1) {
                userMentions.append(", ");
            }
        }
        return userMentions.toString();
    }

    /**
     * Get the description to use in the random game embed.
     * Display the number of common games chosen from, the users involved, and any users whose libraries
     * were not able to be fetched. Also display the description of the game.
     *
     * @param game        Randomly chosen common game
     * @param commonGames Number of common games shared between the users
     * @param noGames     Users with no games
     * @param users       Users whose common library the random game was chosen from
     * @return Embed description
     */
    private String buildDescription(Application game, int commonGames, ArrayList<User> noGames, ArrayList<User> users) {
        String description = "**Users**: " + getUsersAsMentions(users)
                + "\n**Common Games**: " + commonGames;
        if(!noGames.isEmpty()) {
            description += "\n**Couldn't get library for**: " + getUsersAsMentions(noGames);
        }
        return description + "\n\n" + game.getDescription();
    }

    /**
     * Get a random game from the given list of game ids
     *
     * @param commonGameIds  List of game ids
     * @param loadingMessage Loading message to update with random game attempts
     * @return Application details of random chosen game
     */
    private Application getRandomGame(ArrayList<Integer> commonGameIds, EmbedLoadingMessage loadingMessage) {
        Random rand = new Random();
        Application application = null;
        while(application == null) {
            int appId = commonGameIds.get(rand.nextInt(commonGameIds.size()));
            loadingMessage.updateStage("Attempting to get game info for: " + appId);
            AppInfo appInfo = STEAM_STORE.getApplicationInfo(appId);
            if(appInfo == null) {
                continue;
            }
            application = STEAM_STORE.fetchApplicationDetails(appInfo);
        }
        return application;
    }


    /**
     * Fetch a list of the Steam game ids owned by the given Steam user id
     *
     * @param steamId Steam user id to fetch game ids for
     * @return List of user owned Steam game ids
     */
    private ArrayList<Integer> fetchUserGameIds(String steamId) {
        String url = SteamStore.STEAM_API_BASE_URL +
                "IPlayerService/GetOwnedGames/v0001/?key="
                + Secret.STEAM_KEY
                + "&include_played_free_games=1&steamid="
                + steamId;
        NetworkResponse response = new NetworkRequest(url, false).get();
        ArrayList<Integer> games = new ArrayList<>();
        if(response.code != 200) {
            return games;
        }
        JSONArray gameList = new JSONObject(response.body)
                .getJSONObject("response")
                .getJSONArray("games");

        for(int i = 0; i < gameList.length(); i++) {
            games.add(gameList.getJSONObject(i).getInt("appid"));
        }
        return games;
    }
}
