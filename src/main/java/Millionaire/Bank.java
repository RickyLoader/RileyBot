package Millionaire;

import Bot.DiscordUser;
import COD.Session;
import Command.Structure.EmbedHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Millionaire contestant reward bank
 */
public class Bank {
    private final long reward;
    private final int gamesPlayed;
    private final double average;
    private final String contestant;
    private final DecimalFormat dollarFormat;

    /**
     * Create a bank
     *
     * @param contestant  Contestant name who the bank belongs to
     * @param reward      Total contestant reward
     * @param gamesPlayed Total contestant games played
     */
    public Bank(String contestant, long reward, int gamesPlayed) {
        this.contestant = contestant;
        this.reward = reward;
        this.gamesPlayed = gamesPlayed;
        this.average = reward == 0 ? 0 : (double) reward / gamesPlayed;
        this.dollarFormat = new DecimalFormat("$#,###");
    }

    /**
     * Get the bank data for the given member
     *
     * @param member Member to get bank for
     * @return Member bank
     */
    public static Bank getMemberBank(Member member) {
        String json = DiscordUser.getMillionaireBankData(member.getIdLong());
        if(json == null) {
            return null;
        }
        return parseBank(new JSONObject(json));
    }

    /**
     * Parse the API JSON to a bank object
     *
     * @param bank Bank json
     * @return Bank object
     */
    private static Bank parseBank(JSONObject bank) {
        return new Bank(
                bank.getString("name"),
                bank.getLong("bank_value"),
                bank.getInt("games")
        );
    }

    /**
     * Get the bank leaderboard data
     *
     * @return List of bank
     */
    public static ArrayList<Bank> getLeaderboard() {
        String json = DiscordUser.getMillionaireBankLeaderboard();
        if(json == null) {
            return null;
        }
        ArrayList<Bank> leaderboard = new ArrayList<>();
        JSONArray banks = new JSONArray(json);
        for(int i = 0; i < banks.length(); i++) {
            leaderboard.add(parseBank(banks.getJSONObject(i)));
        }
        return leaderboard;
    }

    /**
     * Get the number of games the contestant has played
     *
     * @return Games played
     */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /**
     * Get the total reward the contestant has accrued
     *
     * @return Total reward
     */
    public long getReward() {
        return reward;
    }

    /**
     * Get the total reward formatted to a String
     *
     * @return Formatted total reward;
     */
    public String getFormattedReward() {
        return dollarFormat.format(reward);
    }

    /**
     * Get the formatted average reward per game
     *
     * @return Average reward the contestant receives
     */
    public String getFormattedAverage() {
        return dollarFormat.format(average);
    }

    /**
     * Build a message embed displaying the bank information
     *
     * @param help Help message to display in the embed
     * @return Message embed displaying bank summary
     */
    public MessageEmbed getBankMessage(String help) {
        String thumb = MillionaireGameshow.thumb;
        return new EmbedBuilder()
                .setTitle("Millionaire Bank: " + contestant)
                .setThumbnail(thumb)
                .setImage(EmbedHelper.SPACER_IMAGE)
                .setFooter(help, thumb)
                .setColor(EmbedHelper.PURPLE)
                .addField("Bank Value", getFormattedReward(), true)
                .addField("Games Played", getGamesPlayed() + " games", true)
                .addField("Average Reward", getFormattedAverage(), false)
                .build();
    }

    /**
     * Get the bank summary
     *
     * @return Bank summary
     */
    public String getSummary() {
        return "**Name**: " + contestant
                + "\n**Bank Value**: " + getFormattedReward()
                + "\n**Average Reward**: " + getFormattedAverage();
    }

    /**
     * Sort the bank entries for leaderboard purposes
     *
     * @param banks     Banks to sort
     * @param ascending Ascending rank or not
     */
    public static void sortBanks(ArrayList<Bank> banks, boolean ascending) {
        Comparator<Bank> sort = Comparator.comparingLong(Bank::getReward)
                .thenComparing(Comparator.comparingInt(Bank::getGamesPlayed).reversed());
        banks.sort(ascending ? sort.reversed() : sort);
    }
}
