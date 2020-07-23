package COD;

import Network.ApiRequest;
import Network.NetworkInfo;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class Player {
    private final String name, platform, data;
    private Ratio wl, kd;
    private int streak, spm;
    private Weapon primary, secondary, lethal;
    private ArrayList<Commendation> commendations;

    public Player(String name, String platform) {
        this.name = name;
        this.platform = platform;
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
            String name = URLEncoder.encode(this.name, "UTF-8");
            json = ApiRequest.executeQuery(NetworkInfo.getAddress() + ":8080/DiscordBotAPI/api/modernwarfare/" + name + "/" + platform, "GET", null, false);
            System.out.println(NetworkInfo.getAddress() + ":8080/DiscordBotAPI/api/modernwarfare/" + name + "/" + platform);
            if(json == null) {
                return null;
            }
            JSONObject player = new JSONObject(json).getJSONObject("lifetime");

            JSONObject basic = player.getJSONObject("all").getJSONObject("properties");

            this.kd = new Ratio(basic.getInt("kills"), basic.getInt("deaths"));
            this.wl = new Ratio(basic.getInt("wins"), basic.getInt("losses"));
            this.streak = basic.getInt("recordKillStreak");
            this.spm = (int) Math.ceil((double) (basic.getInt("score")) / basic.getInt("timePlayedTotal"));


            JSONObject weapons = mergeGunNames(player.getJSONObject("itemData"));

            this.primary = getFavourite(weapons, Weapon.TYPE.PRIMARY);
            this.secondary = getFavourite(weapons, Weapon.TYPE.SECONDARY);
            this.lethal = getFavourite(weapons, Weapon.TYPE.LETHAL);

            this.commendations = parsePlayerCommendations(player.getJSONObject("accoladeData").getJSONObject("properties"));
        }
        catch(Exception e) {
            return null;
        }
        return json;
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
            JSONObject commendationNames = readJSONFile("src/main/resources/COD/Data/commendations.json");
            if(commendationNames == null) {
                return null;
            }
            commendationNames = commendationNames.getJSONObject("commendations");
            for(String commendationName : commendationNames.keySet()) {
                JSONObject commendationInfo = commendationNames.getJSONObject(commendationName);
                int quantity = items.getInt(commendationName);
                String title = commendationInfo.getString("title");
                String desc = commendationInfo.getString("desc");
                commendations.add(new Commendation(commendationName, title, desc, quantity));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        Collections.sort(commendations);
        return commendations;
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
            JSONObject gunNames = readJSONFile("src/main/resources/COD/Data/guns.json");
            if(gunNames == null) {
                return null;
            }
            gunNames = gunNames.getJSONObject("guns");
            for(String categoryName : items.keySet()) {
                JSONObject category = items.getJSONObject(categoryName);
                for(String gunName : category.keySet()) {
                    JSONObject gun = category.getJSONObject(gunName).getJSONObject("properties");
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
     * Read a file in as a JSON object
     *
     * @param path Path to file
     * @return JSON object of file
     */
    private JSONObject readJSONFile(String path) {
        try {
            return new JSONObject(new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8));
        }
        catch(Exception e) {
            return null;
        }
    }

    private Weapon getFavourite(JSONObject items, Weapon.TYPE type) {
        String[] desiredCategories = Weapon.getCategories(type);
        Weapon favWeapon = null;
        for(String desiredCategory : desiredCategories) {
            JSONObject category = items.getJSONObject(desiredCategory);
            for(String weaponName : category.keySet()) {
                JSONObject weapon = category.getJSONObject(weaponName).getJSONObject("properties");
                int kills = weapon.getInt("kills");
                int deaths = weapon.getInt("deaths");
                int hits = weapon.getInt("hits");
                int shots = weapon.getInt("shots");
                int headshots = (weapon.has("headshots")) ? weapon.getInt("headshots") : weapon.getInt("headShots");
                Weapon currentWeapon = new Weapon(
                        weaponName,
                        weapon.getString("real_name"),
                        desiredCategory,
                        new Ratio(kills, deaths),
                        new Ratio(hits, shots),
                        headshots,
                        type
                );
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
        return wl.getPos();
    }

    /**
     * Get total losses
     *
     * @return Total losses
     */
    public int getLosses() {
        return wl.getNeg();
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
     * Check if a player was found on the API
     *
     * @return Boolean account exists
     */
    public boolean exists() {
        return data != null;
    }

    /**
     * Wrap Win/Loss, Kill/Death, Hits/Shots data
     */
    private static class Ratio {
        private final double ratio;
        private final int pos, neg, diff;

        public Ratio(int pos, int neg) {
            if(pos == 0 || neg == 0) {
                this.ratio = 0;
            }
            else {
                this.ratio = (double) pos / (double) neg;
            }
            this.neg = neg;
            this.pos = pos;

            // How many ahead/behind
            this.diff = pos - neg;
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
        public int getPos() {
            return pos;
        }

        /**
         * Get negative value (death, shots, losses)
         *
         * @return Negative value
         */
        public int getNeg() {
            return neg;
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

        public Weapon(String iwName, String name, String category, Ratio kd, Ratio accuracy, int headshots, TYPE type) {
            this.iwName = iwName;
            this.category = category;
            this.name = name;
            this.kd = kd;
            this.type = type;
            this.accuracy = accuracy;
            this.headshots = headshots;
            this.image = new File("src/main/resources/COD/Weapons/" + category + "/" + iwName + ".png");
            this.imageTitle = setImageTitle(type);
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
        public int getShotsFired() {
            return accuracy.getNeg();
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
        public int getShotsHit() {
            return accuracy.getPos();
        }

        /**
         * Get number of kills
         *
         * @return Kills
         */
        public int getKills() {
            return kd.getPos();
        }

        /**
         * Get number of deaths
         *
         * @return Deaths
         */
        public int getDeaths() {
            return kd.getNeg();
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

    public static class Commendation implements Comparable<Commendation> {
        private final int quantity;
        private final String iwName, title, desc;
        private final File image;

        public Commendation(String iwName, String title, String desc, int quantity) {
            this.iwName = iwName;
            this.title = title;
            this.desc = desc;
            this.quantity = quantity;
            this.image = new File("src/main/resources/COD/Accolades/" + iwName + ".png");
        }

        public File getImage() {
            return image;
        }

        public String formatQuantity() {
            return "x" + quantity;
        }

        public String getDesc() {
            return desc;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getTitle() {
            return title;
        }

        public String getIwName() {
            return iwName;
        }

        @Override
        public int compareTo(@NotNull Player.Commendation o) {
            return o.getQuantity() - this.getQuantity();
        }
    }
}
