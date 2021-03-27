package Command.Commands;

import Command.Structure.*;
import Steam.AppInfo;
import Steam.Application;
import Steam.SteamStore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.*;

import static Bot.DiscordCommandManager.steamStore;

/**
 * View specific/top steam games
 */
public class SteamCommand extends DiscordCommand {
    public SteamCommand() {
        super("steam", "View steam games!", "steam [top/name/id/store url]");
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        String message = context.getLowerCaseMessage();

        if(SteamStore.isSteamUrl(message)) {
            String[] urlArgs = message.split("/");
            int idIndex = message.matches(SteamStore.STEAM_STORE_DESKTOP_URL) ? urlArgs.length - 2 : urlArgs.length - 1;
            AppInfo targetInfo = steamStore.getApplicationInfo(Integer.parseInt(urlArgs[idIndex]));
            if(targetInfo == null) {
                return;
            }
            Application targetApp = steamStore.fetchApplicationDetails(targetInfo);
            if(targetApp == null) {
                return;
            }
            context.getMessage().delete().queue(deleted -> {
                EmbedBuilder builder = getApplicationEmbedBuilder(targetApp);
                builder.setAuthor(
                        member.getEffectiveName(),
                        null,
                        member.getUser().getAvatarUrl()
                );
                channel.sendMessage(builder.build()).queue();
            });
            return;
        }

        String query = message.replace(getTrigger(), "").trim();
        steamStore.updateStoreData();

        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        if(query.equals("top")) {
            channel.sendTyping().queue();
            showTopSteamGames(context);
            return;
        }

        int appId = getQuantity(query);
        AppInfo appInfo;
        if(appId == 0 && !query.equals("0")) {
            ArrayList<AppInfo> searchResults = steamStore.getApplicationsByName(query);
            if(searchResults.size() != 1) {
                showSearchResults(context, searchResults, query);
                return;
            }
            appInfo = searchResults.get(0);
        }
        else {
            appInfo = steamStore.getApplicationInfo(appId);
            if(appInfo == null) {
                channel.sendMessage(
                        member.getAsMention() + " I didn't find anything with the id: **" + appId + "**"
                ).queue();
                return;
            }
        }
        channel.sendTyping().queue();
        Application application = steamStore.fetchApplicationDetails(appInfo);
        if(application == null) {
            channel.sendMessage(
                    member.getAsMention() + " I wasn't able to parse any info about " + appInfo.getSummary()
            ).queue();
            return;
        }
        channel.sendMessage(getApplicationEmbedBuilder(application).build()).queue();
    }

    /**
     * Build a message embed detailing the given Steam application
     *
     * @param application Steam application
     * @return Message embed detailing Steam application
     */
    EmbedBuilder getApplicationEmbedBuilder(Application application) {
        AppInfo appInfo = application.getAppInfo();
        return new EmbedBuilder()
                .setTitle("Steam | " + appInfo.getSummary(), appInfo.getStoreUrl())
                .setThumbnail(SteamStore.STEAM_LOGO)
                .setImage(application.getThumbnail())
                .setDescription(application.getDescription())
                .setColor(EmbedHelper.BLUE)
                .addField("Type", application.getType(), true)
                .addField(
                        "Price",
                        application.isFree() ? "Free to Play" : application.getPrice().getPriceFormatted(),
                        true
                )
                .addField("Concurrent Players", application.formatConcurrentPlayers(), true)
                .setFooter("Type: " + getTrigger() + " for help");
    }

    /**
     * Display the Steam application search results for the given query in a pageable message embed
     *
     * @param context       Command context
     * @param searchResults List of applications found with the given query
     * @param query         Query used to find search results
     */
    private void showSearchResults(CommandContext context, ArrayList<AppInfo> searchResults, String query) {
        boolean noResults = searchResults.isEmpty();
        new PageableTableEmbed(
                context,
                searchResults,
                SteamStore.STEAM_LOGO,
                "Steam Search Results",
                (noResults ? "No" : searchResults.size())
                        + " Results found for the query: **" + query + "**",
                "Type: " + getTrigger() + " for help",
                new String[]{
                        "Name",
                        "ID"
                },
                5,
                noResults ? EmbedHelper.RED : EmbedHelper.BLUE
        ) {
            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String n1 = ((AppInfo) o1).getName();
                    String n2 = ((AppInfo) o2).getName();
                    if(defaultSort) {
                        return levenshteinDistance(n1, query) - levenshteinDistance(n2, query);
                    }
                    return levenshteinDistance(n2, query) - levenshteinDistance(n1, query);
                });
            }

            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                AppInfo appInfo = (AppInfo) items.get(index);
                return new String[]{
                        appInfo.getName(),
                        String.valueOf(appInfo.getId())
                };
            }
        }.showMessage();
    }

    /**
     * Display the top Steam applications of the last 2 weeks in a pageable message embed
     *
     * @param context Command context
     */
    private void showTopSteamGames(CommandContext context) {
        ArrayList<Application> topApplications = steamStore.fetchTopSteamApplications();
        new PageableTableEmbed(
                context,
                topApplications,
                SteamStore.STEAM_LOGO,
                "Steam Top Games (Last 2 Weeks)",
                "Based on concurrent players",
                "Type: " + getTrigger() + " for help",
                new String[]{
                        "Name (ID)",
                        "Price",
                        "Concurrent Players"
                },
                5,
                EmbedHelper.BLUE
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Application application = (Application) items.get(index);
                AppInfo appInfo = application.getAppInfo();
                return new String[]{
                        appInfo.getName() + " (" + appInfo.getId() + ")",
                        application.isFree() ? "Free to Play" : application.getPrice().getPriceFormatted(),
                        application.formatConcurrentPlayers()
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    long players1 = ((Application) o1).getConcurrentPlayers();
                    long players2 = ((Application) o2).getConcurrentPlayers();
                    return defaultSort ? Long.compare(players2, players1) : Long.compare(players1, players2);
                });
            }
        }.showMessage();
    }


    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger()) || SteamStore.isSteamUrl(query);
    }
}
