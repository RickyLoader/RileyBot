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
    private Standard primary, secondary;
    private Lethal lethal;
    private Tactical tactical;
    private FieldUpgrade fieldUpgrade;
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

            JSONObject weapons = player.getJSONObject("itemData");
            JSONObject supers = weapons.getJSONObject("supers");
            weapons.remove("supers");
            JSONObject mergedWeaponData = mergeGunStats(weapons);

            this.primary = (Standard) getFavourite(mergedWeaponData, Weapon.TYPE.PRIMARY);
            this.secondary = (Standard) getFavourite(mergedWeaponData, Weapon.TYPE.SECONDARY);
            this.lethal = (Lethal) getFavourite(mergedWeaponData, Weapon.TYPE.LETHAL);
            this.tactical = (Tactical) getFavourite(mergedWeaponData, Weapon.TYPE.TACTICAL);
            this.fieldUpgrade = parseFavouriteFieldUpgrade(mergeSuperData(supers));

            System.out.println(tactical.toString());
            System.out.println(fieldUpgrade.toString());

            JSONObject killstreaks = player.getJSONObject("scorestreakData");
            killstreaks = mergeStreakData(killstreaks.getJSONObject("lethalScorestreakData"), killstreaks.getJSONObject("supportScorestreakData"));

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
        try {
            JSONObject streakData = readJSONFile(res + "Data/streaks.json").getJSONObject("streaks");

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
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return supportStreaks;
    }

    /**
     * Add the real field upgrade names and property names to the JSON returned from the API
     * Field upgrades have a "misc1" and "misc2" quantity which refers to different stats
     *
     * @param supers Field upgrade data from the API
     * @return Field upgrade JSON containing real names and property names
     */
    private JSONObject mergeSuperData(JSONObject supers) {
        try {
            JSONObject superData = readJSONFile(res + "Data/supers.json").getJSONObject("supers");

            for(String superName : supers.keySet()) {
                JSONObject fieldUpgrade = supers.getJSONObject(superName).getJSONObject("properties");
                if(!superData.has(superName)) {
                    continue;
                }
                JSONObject data = superData.getJSONObject(superName);
                fieldUpgrade.put("real_name", data.getString("real_name"));
                if(data.has("misc1")) {
                    fieldUpgrade.put("misc1_name", data.getString("misc1_name"));
                }
                if(data.has("misc2_name")) {
                    fieldUpgrade.put("misc2_name", data.getString("misc2_name"));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return supers;
    }

    /**
     * Add the real gun names and equipment stat names to the JSON returned from the API
     * e.g iw8_me_akimboblunt = Kali Sticks
     * e.g extraStat1 of equip_adrenaline = Health Healed
     *
     * @param items Weapons from API
     * @return Weapon JSON containing real names
     */
    private JSONObject mergeGunStats(JSONObject items) {
        try {
            JSONObject weapons = readJSONFile(res + "Data/weapons.json").getJSONObject("weapons");
            for(String categoryName : items.keySet()) {
                JSONObject category = items.getJSONObject(categoryName);
                for(String weaponName : category.keySet()) {
                    JSONObject weapon = category.getJSONObject(weaponName).getJSONObject("properties");
                    if(!weapons.has(categoryName) || !weapons.getJSONObject(categoryName).has(weaponName)) {
                        System.out.println("MISSING WEAPON: " + weaponName + " IN CATEGORY: " + categoryName);
                        continue;
                    }
                    JSONObject weaponData = weapons.getJSONObject(categoryName).getJSONObject(weaponName);
                    weapon.put("real_name", weaponData.getString("real_name"));
                    if(weaponData.has("property")) {
                        weapon.put("property", weaponData.getString("property"));
                    }
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
                            res,
                            killstreak.has("extra_stat") ? killstreak.getString("extra_stat") : null,
                            new Ratio(killstreak.getInt("extraStat1"), killstreak.getInt("uses"))
                    )
            );
        }
        Collections.sort(killstreaks);
        return new ArrayList<>(killstreaks.subList(0, 5));
    }

    /**
     * Parse the player's field upgrade JSON to find the
     *
     * @param superData Field upgrade JSON
     * @return Player's top field upgrade
     */
    private FieldUpgrade parseFavouriteFieldUpgrade(JSONObject superData) {
        ArrayList<FieldUpgrade> fieldUpgrades = new ArrayList<>();

        for(String superName : superData.keySet()) {
            JSONObject fieldUpgrade = superData.getJSONObject(superName).getJSONObject("properties");
            if(!fieldUpgrade.has("real_name")) {
                continue;
            }
            int uses = fieldUpgrade.getInt("uses");
            FieldUpgrade.Property first = null;
            if(fieldUpgrade.has("misc1_name")) {
                first = new FieldUpgrade.Property(
                        fieldUpgrade.getString("misc1_name"),
                        fieldUpgrade.getInt("misc1")
                );
            }

            fieldUpgrades.add(
                    new FieldUpgrade(
                            superName,
                            fieldUpgrade.getString("real_name"),
                            res,
                            new Ratio(fieldUpgrade.getInt("kills"), uses),
                            first
                    )
            );
        }
        Collections.sort(fieldUpgrades);
        return fieldUpgrades.get(0);
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
        ArrayList<Weapon> weapons = new ArrayList<>();
        for(String desiredCategory : desiredCategories) {
            JSONObject category = items.getJSONObject(desiredCategory);
            for(String weaponName : category.keySet()) {
                JSONObject weapon = category.getJSONObject(weaponName).getJSONObject("properties");
                String name = weapon.has("real_name") ? weapon.getString("real_name") : "MISSING NAME";
                Weapon currentWeapon;
                switch(type) {
                    case LETHAL:
                        currentWeapon = new Lethal(
                                weaponName,
                                name,
                                res,
                                new Ratio(weapon.getInt("kills"), weapon.getInt("uses"))
                        );
                        break;
                    case TACTICAL:
                        currentWeapon = new Tactical(
                                weaponName,
                                name,
                                res,
                                weapon.has("property") ? weapon.getString("property") : null,
                                new Ratio(weapon.getInt("extraStat1"), weapon.getInt("uses"))
                        );
                        break;
                    default:
                        currentWeapon = new Standard(
                                weaponName,
                                name,
                                desiredCategory,
                                type,
                                res,
                                new Ratio(weapon.getInt("kills"), weapon.getInt("deaths")),
                                new Ratio(weapon.getInt("hits"), weapon.getInt("shots")),
                                (weapon.has("headshots")) ? weapon.getInt("headshots") : weapon.getInt("headShots")
                        );
                        break;
                }
                weapons.add(currentWeapon);
            }
        }
        Collections.sort(weapons);
        return weapons.get(0);
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
        private final int numerator, denominator;
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
     * Hold commendation information
     */
    public static class Commendation implements Comparable<Commendation> {
        private final int quantity;
        private final String title, desc;
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
        private final Ratio statUse;
        private final String name, statName;
        private final File image;

        /**
         * Create a killstreak
         *
         * @param iwName   Infinity Ward name of streak e.g "radar_drone_overwatch"
         * @param name     Real name of streak e.g "Personal Radar"
         * @param statName Name of provided stat quantity, kills/assists/..
         * @param statUse  Stat/Use Ratio
         * @param res      Resource location
         */
        public Killstreak(String iwName, String name, String res, String statName, Ratio statUse) {
            this.name = name;
            this.statName = statName;
            this.statUse = statUse;
            this.image = new File(res + "Killstreaks/" + iwName + ".png");
        }

        /**
         * Get the average stat per use of the killstreak
         *
         * @return Average stat per use
         */
        public String getAverage() {
            return statUse.formatRatio(statUse.getRatio());
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
         * Return if the killstreak has an extra stat
         *
         * @return Killstreak has extra stat
         */
        public boolean hasExtraStat() {
            return statName != null;
        }

        /**
         * Get the quantity of the given stat
         *
         * @return Quantity of stat
         */
        public int getStat() {
            return statUse.getNumerator();
        }

        /**
         * Get the quantity of uses
         *
         * @return Killstreak uses
         */
        public int getUses() {
            return statUse.getDenominator();
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
            return o.getUses() - getUses();
        }
    }

    private static class FieldUpgrade implements Comparable<FieldUpgrade> {
        private final Ratio killUse;
        private final String name;
        private final File image;
        private final Property first;

        /**
         * Create a killstreak
         *
         * @param iwName  Infinity Ward name of field upgrade e.g "super_deadsilence"
         * @param name    Real name of field upgrade e.g "Dead Silence"
         * @param killUse Kill/Use Ratio
         * @param res     Resource location
         * @param first   First property
         */
        public FieldUpgrade(String iwName, String name, String res, Ratio killUse, Property first) {
            this.name = name;
            this.killUse = killUse;
            //this.image = new File(res + "Supers/" + iwName + ".png");
            this.image = null;
            this.first = first;
        }

        /**
         * Get the field upgrade image
         *
         * @return Field upgrade image
         */
        public File getImage() {
            return image;
        }

        /**
         * Return field upgrade has a first property
         *
         * @return Presence of first property
         */
        public boolean hasProperty() {
            return first != null;
        }

        /**
         * Get the first property of the field upgrade
         *
         * @return First property
         */
        public Property getProperty() {
            return first;
        }

        /**
         * Return field upgrade has kill stat
         *
         * @return Field upgrade has kills
         */
        public boolean hasKills() {
            return getKills() > 0;
        }

        /**
         * Get kills
         *
         * @return Field upgrade kills
         */
        public int getKills() {
            return killUse.getNumerator();
        }

        /**
         * Get the name of the field upgrade
         *
         * @return Name of field upgrade
         */
        public String getName() {
            return name;
        }

        /**
         * Get the number of uses
         *
         * @return Number of uses
         */
        public int getUses() {
            return killUse.getDenominator();
        }

        /**
         * Sort in descending order of uses
         */
        @Override
        public int compareTo(@NotNull CODPlayer.FieldUpgrade o) {
            return o.getUses() - getUses();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("name: ").append(name)
                    .append(" kills: ").append(hasKills() ? getKills() : "N/A")
                    .append(" uses: ").append(getUses());
            if(hasProperty()) {
                builder.append(" ").append(first.getName()).append(": ").append(first.getValue());
            }
            return builder.toString();
        }

        /**
         * Property of field upgrade
         */
        private static class Property {
            private final String name;
            private final int quantity;

            /**
             * Create a property
             *
             * @param name     Name of property
             * @param quantity Quantity of property
             */
            public Property(String name, int quantity) {
                this.name = name;
                this.quantity = quantity;
            }

            /**
             * Get the property name
             *
             * @return Property name
             */
            public String getName() {
                return name;
            }

            /**
             * Get the value of the property
             *
             * @return Property value
             */
            public int getValue() {
                return quantity;
            }
        }
    }
}
