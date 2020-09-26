package COD;

import Network.NetworkRequest;
import Network.NetworkInfo;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class CODPlayer {
    private final String name, platform, data, endpoint, res;
    private String status;
    private Ratio wl, kd;
    private int streak;
    private Weapon primary, secondary, lethal;
    private ArrayList<Killstreak> killstreaks;
    private ArrayList<Commendation> commendations;

    public CODPlayer(String name, String platform, String endpoint, String res) {
        this.name = name;
        this.platform = platform;
        this.endpoint = endpoint;
        this.res = "src/main/resources/COD/" + res + "/";
        this.data = fetchPlayerData();
    }

    /**
     * Fetch player data from the API
     *
     * @return JSON from API
     */
    private String fetchPlayerData() {
        String json;
        try {
            String name = URLEncoder.encode(this.name, "UTF-8").replaceAll("\\+", "%20");
            json = new NetworkRequest(NetworkInfo.getAddress() + ":8080/DiscordBotAPI/api/" + endpoint + "/" + name + "/" + platform, false).get();
            if(json == null) {
                status = "Failed to communicate with API, try again later.";
                return null;
            }

            JSONObject data = new JSONObject(json);
            status = data.getString("status");

            if(!status.equals("success")) {
                return null;
            }

            JSONObject player = data.getJSONObject("data").getJSONObject("lifetime");

            JSONObject basic = player.getJSONObject("all").getJSONObject("properties");

            this.kd = new Ratio(basic.getInt("kills"), basic.getInt("deaths"));
            this.wl = new Ratio(basic.getInt("wins"), basic.getInt("losses"));
            this.streak = basic.getInt("recordKillStreak");
            JSONObject weapons = mergeGunNames(player.getJSONObject("itemData"));

            this.primary = getFavourite(weapons, Weapon.TYPE.PRIMARY);
            this.secondary = getFavourite(weapons, Weapon.TYPE.SECONDARY);
            this.lethal = getFavourite(weapons, Weapon.TYPE.LETHAL);

            JSONObject killstreaks = player.getJSONObject("scorestreakData");
            killstreaks = mergeStreakData(killstreaks.getJSONObject("lethalScorestreakData"), killstreaks.getJSONObject("supportScorestreakData"));
            if(killstreaks == null) {
                status = "Error getting killstreak info";
                return null;
            }
            this.killstreaks = parseKillstreaks(killstreaks);
            this.commendations = parsePlayerCommendations(player.getJSONObject("accoladeData").getJSONObject("properties"));
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Read a file in as a JSON object
     *
     * @param filename Filename
     * @return JSON object of file
     */
    JSONObject readJSONFile(String filename) {
        try {
            return new JSONObject(new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8));
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get the commendations for the player
     *
     * @param items Commendations from API
     * @return Player commendations
     */
    private ArrayList<Commendation> parsePlayerCommendations(JSONObject items) {
        ArrayList<Commendation> commendations = new ArrayList<>();
        try {
            JSONObject commendationNames = readJSONFile(res + "Data/commendations.json");
            if(commendationNames == null) {
                return null;
            }
            commendationNames = commendationNames.getJSONObject("commendations");
            for(String commendationName : commendationNames.keySet()) {
                JSONObject commendationInfo = commendationNames.getJSONObject(commendationName);
                int quantity = items.getInt(commendationName);
                String title = commendationInfo.getString("title");
                String desc = commendationInfo.getString("desc");
                commendations.add(new Commendation(commendationName, title, desc, quantity, res));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        Collections.sort(commendations);
        return commendations;
    }

    /**
     * Add the real killstreak names and stat type to the JSON returned from the API
     * Killstreaks have an "extraStat1" quantity which may refer to kills/assists/.. depending on the streak.
     *
     * @param lethalStreaks  Lethal killstreaks from the API
     * @param supportStreaks Support killstreaks from the API
     * @return Killstreak JSON containing real names and stat type
     */
    private JSONObject mergeStreakData(JSONObject lethalStreaks, JSONObject supportStreaks) {
        JSONObject streakData = readJSONFile(res + "Data/streaks.json");
        if(streakData == null) {
            return null;
        }
        streakData = streakData.getJSONObject("streaks");

        // Combine lethalStreaks in to supportStreaks
        for(String lethalKey : lethalStreaks.keySet()) {
            supportStreaks.put(lethalKey, lethalStreaks.getJSONObject(lethalKey));
        }

        for(String killstreakName : supportStreaks.keySet()) {
            JSONObject streak = supportStreaks.getJSONObject(killstreakName).getJSONObject("properties");
            JSONObject streakExtra = streakData.getJSONObject(killstreakName);
            streak.put("real_name", streakExtra.getString("real_name"));
            if(streakExtra.has("extra_stat")) {
                streak.put("extra_stat", streakExtra.getString("extra_stat"));
            }
        }
        return supportStreaks;
    }

    /**
     * Add the real gun names to the JSON returned from the API
     * (only contains gun variable names e.g iw8_me_akimboblunt = Kali Sticks)
     *
     * @param items Weapons from API
     * @return Weapon JSON containing real names
     */
    private JSONObject mergeGunNames(JSONObject items) {
        try {
            JSONObject gunNames = readJSONFile(res + "Data/guns.json");
            if(gunNames == null) {
                return null;
            }
            gunNames = gunNames.getJSONObject("guns");
            for(String categoryName : items.keySet()) {
                JSONObject category = items.getJSONObject(categoryName);
                for(String gunName : category.keySet()) {
                    JSONObject gun = category.getJSONObject(gunName).getJSONObject("properties");
                    if(!gunNames.has(categoryName) || !gunNames.getJSONObject(categoryName).has(gunName)) {
                        System.out.println("MISSING GUN: " + gunName + " IN CATEGORY: " + categoryName);
                        continue;
                    }
                    String realName = gunNames.getJSONObject(categoryName).getJSONObject(gunName).getString("real_name");
                    gun.put("real_name", realName);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Parse the player's killstreak JSON to an array of Killstreak objects
     *
     * @param killstreakData Killstreak JSON
     * @return Player's top 3 killstreaks
     */
    private ArrayList<Killstreak> parseKillstreaks(JSONObject killstreakData) {
        ArrayList<Killstreak> killstreaks = new ArrayList<>();
        for(String killstreakName : killstreakData.keySet()) {
            JSONObject killstreak = killstreakData.getJSONObject(killstreakName).getJSONObject("properties");
            killstreaks.add(new Killstreak(
                            killstreakName,
                            killstreak.getString("real_name"),
                            killstreak.getInt("uses"),
                            killstreak.has("extra_stat") ? killstreak.getString("extra_stat") : null,
                            killstreak.getInt("extraStat1"),
                            res
                    )
            );
        }
        Collections.sort(killstreaks);
        return new ArrayList<>(killstreaks.subList(0, 5));
    }

    /**
     * Get the player's favourite weapon of a given type - PRIMARY, SECONDARY, LETHAL
     *
     * @param items JSON weapons from the API
     * @param type  Weapon type
     * @return Player's favourite weapon
     */
    private Weapon getFavourite(JSONObject items, Weapon.TYPE type) {
        String[] desiredCategories = Weapon.getCategories(type);
        Weapon favWeapon = null;
        for(String desiredCategory : desiredCategories) {
            JSONObject category = items.getJSONObject(desiredCategory);
            for(String weaponName : category.keySet()) {
                JSONObject weapon = category.getJSONObject(weaponName).getJSONObject("properties");
                String name = weapon.has("real_name") ? weapon.getString("real_name") : "MISSING NAME";
                Weapon currentWeapon;
                if(type == Weapon.TYPE.LETHAL || type == Weapon.TYPE.TACTICAL) {
                    currentWeapon = new Weapon(
                            weaponName,
                            name,
                            desiredCategory,
                            new Ratio(weapon.getInt("kills"), weapon.getInt("uses")),
                            type,
                            res
                    );
                }
                else {
                    currentWeapon = new Weapon(
                            weaponName,
                            name,
                            desiredCategory,
                            new Ratio(weapon.getInt("kills"), weapon.getInt("deaths")),
                            new Ratio(weapon.getInt("hits"), weapon.getInt("shots")),
                            (weapon.has("headshots")) ? weapon.getInt("headshots") : weapon.getInt("headShots"),
                            type,
                            res
                    );
                }
                if(favWeapon == null || favWeapon.getKills() < currentWeapon.getKills()) {
                    favWeapon = currentWeapon;
                }
            }
        }
        return favWeapon;
    }

    /**
     * Get player name
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Get player killstreaks
     *
     * @return Player killstreaks
     */
    public ArrayList<Killstreak> getKillstreaks() {
        return killstreaks;
    }

    /**
     * Get favourite primary weapon
     *
     * @return Primary weapon
     */
    public Weapon getPrimary() {
        return primary;
    }

    /**
     * Get player commendations
     *
     * @return Player commendations
     */
    public ArrayList<Commendation> getCommendations() {
        return commendations;
    }

    /**
     * Get favourite secondary weapon
     *
     * @return Secondary weapon
     */
    public Weapon getSecondary() {
        return secondary;
    }

    /**
     * Get favourite lethal equipment
     *
     * @return Lethal weapon
     */
    public Weapon getLethal() {
        return lethal;
    }

    /**
     * Get formatted kill/death ratio
     *
     * @return K/D String
     */
    public String getKD() {
        return kd.formatRatio(kd.getRatio());
    }

    /**
     * Get formatted win/loss ratio
     *
     * @return W/L String
     */
    public String getWinLoss() {
        return wl.formatRatio(wl.getRatio());
    }

    /**
     * Get total wins
     *
     * @return Total wins
     */
    public int getWins() {
        return wl.getNumerator();
    }

    /**
     * Get total losses
     *
     * @return Total losses
     */
    public int getLosses() {
        return wl.getDenominator();
    }

    /**
     * Get longest kill streak
     *
     * @return Longest kill streak
     */
    public int getLongestKillStreak() {
        return streak;
    }

    /**
     * Check if the network request for player data was successful
     *
     * @return Boolean account exists
     */
    public boolean success() {
        return data != null;
    }

    /**
     * Get the status message from from the player data request
     *
     * @return Status message
     */
    public String getStatus() {
        return status;
    }

    /**
     * Wrap Win/Loss, Kill/Death, Hits/Shots data
     */
    public static class Ratio {
        private final double ratio;
        private final int numerator, denominator, diff;
        private final DecimalFormat commaFormat;

        public Ratio(int numerator, int denominator) {
            if(numerator == 0 || denominator == 0) {
                this.ratio = 0;
            }
            else {
                this.ratio = (double) numerator / (double) denominator;
            }
            this.denominator = denominator;
            this.numerator = numerator;

            // How many ahead/behind
            this.diff = numerator - denominator;
            this.commaFormat = new DecimalFormat("#,###");
        }

        /**
         * Format the ratio to 2 decimal places
         *
         * @param ratio Ratio to be formatted
         * @return Formatted ratio
         */
        public String formatRatio(double ratio) {
            return new DecimalFormat("0.00").format(ratio);
        }

        /**
         * Return the ratio as a percentage instead of decimal
         *
         * @return Percentage formatted ratio
         */
        public String getRatioPercentage() {
            return formatRatio(ratio * 100) + "%";
        }

        /**
         * Get decimal ratio
         *
         * @return Decimal ratio
         */
        public double getRatio() {
            return ratio;
        }

        /**
         * Get positive value (kill, hits, wins)
         *
         * @return Positive value
         */
        public int getNumerator() {
            return numerator;
        }

        /**
         * Get the numerator formatted with commas
         *
         * @return Numerator formatted with commas
         */
        public String formatNumerator() {
            return commaFormat.format(numerator);
        }

        /**
         * Get the denominator formatted with commas
         *
         * @return Denominator formatted with commas
         */
        public String formatDenominator() {
            return commaFormat.format(denominator);
        }

        /**
         * Get negative value (death, shots, losses)
         *
         * @return Negative value
         */
        public int getDenominator() {
            return denominator;
        }
    }

    /**
     * Hold weapon information
     */
    public static class Weapon {
        private final Ratio kd, accuracy;
        private final int headshots;
        private final String name, iwName, imageTitle, category;
        private final File image;
        private final TYPE type;

        public enum TYPE {
            PRIMARY,
            SECONDARY,
            LETHAL,
            TACTICAL
        }

        /**
         * Create a weapon
         *
         * @param iwName    Infinity Ward name of weapon e.g "iw8_me_akimboblunt"
         * @param name      Real name of weapon e.g "Kali Sticks"
         * @param category  Infinity Ward name of weapon category e.g "weapon_melee"
         * @param kd        Kill/Death ratio of weapon
         * @param accuracy  Shots Hit/Shots Fired ratio of weapon
         * @param headshots Number of headshots with weapon
         * @param type      Type of weapon
         * @param res       Resource location
         */
        public Weapon(String iwName, String name, String category, Ratio kd, Ratio accuracy, int headshots, TYPE type, String res) {
            this.iwName = iwName;
            this.category = category;
            this.name = name;
            this.kd = kd;
            this.type = type;
            this.accuracy = accuracy;
            this.headshots = headshots;
            this.image = new File(res + "Weapons/" + category + "/" + iwName + ".png");
            this.imageTitle = setImageTitle(type);
        }

        /**
         * Create a tactical/lethal equipment
         *
         * @param iwName   Infinity Ward name of weapon e.g "iw8_me_akimboblunt"
         * @param name     Real name of weapon e.g "Kali Sticks"
         * @param category Infinity Ward name of weapon category e.g "weapon_melee"
         * @param kd       Kills/Uses Ratio of equipment
         * @param type     Type of weapon
         * @param res      Resource location
         */
        public Weapon(String iwName, String name, String category, Ratio kd, TYPE type, String res) {
            this(iwName, name, category, kd, null, 0, type, res);
        }

        /**
         * Set title used above weapon in combat record image
         *
         * @param type Enum type of weapon
         * @return String title
         */
        private String setImageTitle(TYPE type) {
            String imageTitle = "";
            switch(type) {

                case PRIMARY:
                    imageTitle = "Primary Weapon of Choice";
                    break;
                case SECONDARY:
                    imageTitle = "Secondary Weapon of Choice";
                    break;
                case LETHAL:
                    imageTitle = "Lethal Equipment of Choice";
                    break;
                case TACTICAL:
                    imageTitle = "Tactical Equipment of Choice";
                    break;
            }
            return imageTitle;
        }

        /**
         * Get the Infinity Ward name of the weapon
         *
         * @return Infinity Ward name of weapon
         */
        public String getIwName() {
            return iwName;
        }

        /**
         * Get the image title for use in combat record
         *
         * @return Image title
         */
        public String getImageTitle() {
            return imageTitle;
        }

        /**
         * Get the name of the weapon
         *
         * @return Weapon name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the weapon type
         *
         * @return Weapon type
         */
        public TYPE getType() {
            return type;
        }

        /**
         * Get the image of the weapon
         *
         * @return Weapon image
         */
        public File getImage() {
            return image;
        }

        /**
         * Get the percentage formatted accuracy ratio of the weapon
         *
         * @return Accuracy ratio
         */
        public String getAccuracy() {
            return accuracy.getRatioPercentage();
        }

        /**
         * Get the shots fired
         *
         * @return Shots fired
         */
        public String getShotsFired() {
            return accuracy.formatDenominator();
        }

        /**
         * Get the K/D ratio formatted to 2 decimal places
         *
         * @return K/D ratio
         */
        public String getKd() {
            return kd.formatRatio(kd.getRatio());
        }

        /**
         * Get number of shots hit
         *
         * @return Shots hit
         */
        public String getShotsHit() {
            return accuracy.formatNumerator();
        }

        /**
         * Get number of kills
         *
         * @return Kills
         */
        public int getKills() {
            return kd.getNumerator();
        }

        /**
         * Get number of deaths
         *
         * @return Deaths
         */
        public int getDeaths() {
            return kd.getDenominator();
        }

        /**
         * Get the weapon categories associated with the given weapon type
         *
         * @param type (PRIMARY, SECONDARY...)
         * @return Weapon categories
         */
        public static String[] getCategories(TYPE type) {
            String[] categories = new String[]{};
            switch(type) {
                case PRIMARY:
                    categories = new String[]{"weapon_sniper", "weapon_lmg", "weapon_assault_rifle", "weapon_other", "weapon_shotgun", "weapon_smg", "weapon_marksman"};
                    break;
                case SECONDARY:
                    categories = new String[]{"weapon_launcher", "weapon_pistol", "weapon_melee"};
                    break;
                case LETHAL:
                    categories = new String[]{"lethals"};
                    break;
                case TACTICAL:
                    categories = new String[]{"tacticals"};
                    break;
            }
            return categories;
        }
    }

    /**
     * Hold commendation information
     */
    public static class Commendation implements Comparable<Commendation> {
        private final int quantity;
        private final String iwName, title, desc;
        private final File image;

        /**
         * Create a commendation
         *
         * @param iwName   Infinity Ward name of commendation e.g "noDeathsFromBehind"
         * @param title    Real name of commendation e.g "Sixth Sense"
         * @param desc     Description of commendation e.g "No deaths from behind"
         * @param quantity Player quantity of commendation
         * @param res      Resource location
         */
        public Commendation(String iwName, String title, String desc, int quantity, String res) {
            this.iwName = iwName;
            this.title = title;
            this.desc = desc;
            this.quantity = quantity;
            this.image = new File(res + "Accolades/" + iwName + ".png");
        }

        /**
         * Get the image of the commendation
         *
         * @return Commendation image
         */
        public File getImage() {
            return image;
        }

        /**
         * Format the quantity from 1 to x1
         *
         * @return Formatted quantity
         */
        public String formatQuantity() {
            return "x" + quantity;
        }

        /**
         * Get the description of the commendation (what it is awarded for)
         *
         * @return Description of commendation
         */
        public String getDesc() {
            return desc;
        }

        /**
         * Get the quantity of the commendation
         *
         * @return How many have been awarded to the player
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Get the title of the commendation (The name)
         *
         * @return Name of commendation
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sort descending by quantity
         */
        @Override
        public int compareTo(@NotNull CODPlayer.Commendation o) {
            return o.getQuantity() - quantity;
        }
    }

    /**
     * Hold killstreak information
     */
    public static class Killstreak implements Comparable<Killstreak> {
        private final int stat, uses;
        private final String name, statName, iwName;
        private final File image;
        private double average;

        /**
         * Create a killstreak
         *
         * @param iwName   Infinity Ward name of streak e.g "radar_drone_overwatch"
         * @param name     Real name of streak e.g "Personal Radar"
         * @param uses     Quantity of uses
         * @param statName Name of provided stat quantity, kills/assists/..
         * @param stat     Quantity of the given stat
         * @param res      Resource location
         */
        public Killstreak(String iwName, String name, int uses, String statName, int stat, String res) {
            this.iwName = iwName;
            this.name = name;
            this.uses = uses;
            this.statName = statName;
            this.stat = stat;
            this.image = new File(res + "Killstreaks/" + iwName + ".png");
            if(statName != null) {
                this.average = (double) stat / uses;
            }
        }

        /**
         * Get the average stat per use of the killstreak
         *
         * @return Average stat per use
         */
        public String getAverage() {
            return new DecimalFormat("0.00").format(average);
        }

        /**
         * Get the killstreak name
         *
         * @return Killstreak name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the killstreak image
         *
         * @return Killstreak image
         */
        public BufferedImage getImage() {
            try {
                return ImageIO.read(image);
            }
            catch(IOException e) {
                return null;
            }
        }

        /**
         * Return if the killstreak doesn't have an extra stat
         *
         * @return Killstreak has no extra stat
         */
        public boolean noExtraStat() {
            return statName == null;
        }

        /**
         * Get the quantity of the given stat
         *
         * @return Quantity of stat
         */
        public int getStat() {
            return stat;
        }

        /**
         * Get the quantity of uses
         *
         * @return Killstreak uses
         */
        public int getUses() {
            return uses;
        }

        /**
         * Get the name of the given stat
         *
         * @return Name of stat
         */
        public String getStatName() {
            return statName;
        }

        /**
         * Sort descending by quantity
         */
        @Override
        public int compareTo(@NotNull CODPlayer.Killstreak o) {
            return o.getUses() - uses;
        }
    }
}
