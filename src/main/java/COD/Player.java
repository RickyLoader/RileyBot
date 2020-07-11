package COD;

import Network.ApiRequest;
import Network.NetworkInfo;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;

public class Player {
    private final String name, platform, data;
    private Ratio wl, kd;
    private int streak, spm;
    private SearchAndDestroy searchAndDestroy;
    private Weapon primary, secondary;

    public Player(String name, String platform) {
        this.name = name;
        this.platform = platform;
        this.data = fetchPlayerData();
    }

    public String getData() {
        return data;
    }

    public String getPlatform() {
        return platform;
    }

    private String fetchPlayerData() {
        String json;
        try {
            String name = URLEncoder.encode(this.name, "UTF-8");
            json = ApiRequest.executeQuery(NetworkInfo.getAddress() + ":8080/DiscordBotAPI/api/modernwarfare/" + name + "/" + platform, "GET", null, false);

            JSONObject player = new JSONObject(json).getJSONObject("lifetime");

            JSONObject basic = player.getJSONObject("all").getJSONObject("properties");

            this.kd = new Ratio(basic.getDouble("kdRatio"), basic.getInt("kills"), basic.getInt("deaths"));
            this.wl = new Ratio(basic.getDouble("wlRatio"), basic.getInt("wins"), basic.getInt("losses"));
            this.streak = basic.getInt("recordKillStreak");
            this.spm = (int) Math.ceil((double) (basic.getInt("score")) / basic.getInt("timePlayedTotal"));

            JSONObject mode = player.getJSONObject("mode").getJSONObject("sd").getJSONObject("properties");
            this.searchAndDestroy = new SearchAndDestroy(
                    mode.getLong("timePlayed"),
                    mode.getLong("score"),
                    mode.getInt("plants"),
                    mode.getInt("defuses"),
                    mode.getDouble("scorePerMinute"),
                    new Ratio(mode.getDouble("kdRatio"), mode.getInt("kills"), mode.getInt("deaths"))
            );

            JSONObject categories = player.getJSONObject("itemData");
            this.primary = getFavourite(categories, true);
            this.secondary = getFavourite(categories, false);
        }
        catch(Exception e) {
            return null;
        }
        return json;
    }

    public int getLongestKillStreak() {
        return streak;
    }

    public int getSpm() {
        return spm;
    }

    private Weapon getFavourite(JSONObject items, boolean primary) {
        String[] desiredCategories = primary ? new String[]{"sniper", "lmg", "assault_rifle", "other", "shotgun", "smg", "marksman"} : new String[]{"launcher", "pistol", "melee"};

        Weapon favWeapon = null;
        for(String desiredCategory : desiredCategories) {
            desiredCategory = "weapon_" + desiredCategory;
            JSONObject category = items.getJSONObject(desiredCategory);
            for(String weaponName : category.keySet()) {
                JSONObject weapon = category.getJSONObject(weaponName).getJSONObject("properties");
                Weapon currentWeapon = new Weapon(
                        weaponName,
                        desiredCategory,
                        new Ratio(weapon.getDouble("kdRatio"), weapon.getInt("kills"), weapon.getInt("deaths")),
                        new Ratio(weapon.getDouble("accuracy"), weapon.getInt("hits"), weapon.getInt("shots")),
                        weapon.getInt("headshots"),
                        primary
                );
                if(favWeapon == null || favWeapon.getKills() < currentWeapon.getKills()) {
                    favWeapon = currentWeapon;
                }
            }
        }
        return favWeapon;
    }

    public String getName() {
        return name;
    }

    public Weapon getPrimary() {
        return primary;
    }

    public Weapon getSecondary() {
        return secondary;
    }

    public String getKD() {
        return kd.formatRatio(kd.getRatio());
    }

    public SearchAndDestroy getSearchAndDestroy() {
        return searchAndDestroy;
    }

    public int getWins() {
        return wl.getPos();
    }

    public String getWinLoss() {
        return wl.formatRatio(wl.getRatio());
    }

    public int getLosses() {
        return wl.getNeg();
    }

    public int getKills() {
        return kd.pos;
    }

    public int getDeaths() {
        return kd.getNeg();
    }

    private static class SearchAndDestroy {
        private final long timePlayed, score;
        private final int plants, defuses;
        private final double spm;
        private final Ratio kd;

        public SearchAndDestroy(long timePlayed, long score, int plants, int defuses, double spm, Ratio kd) {
            this.timePlayed = timePlayed;
            this.score = score;
            this.plants = plants;
            this.defuses = defuses;
            this.spm = spm;
            this.kd = kd;
        }

        public int getDefuses() {
            return defuses;
        }

        public int getDeaths() {
            return kd.getNeg();
        }

        public int getKills() {
            return kd.getPos();
        }

        public int getPlants() {
            return plants;
        }

        public long getScore() {
            return score;
        }

        public String getKd() {
            return kd.formatRatio(kd.getRatio());
        }

        public double getSpm() {
            return spm;
        }

        public long getTimePlayed() {
            return timePlayed;
        }
    }

    private static class Ratio {
        private final double ratio;
        private final int pos, neg, diff;

        public Ratio(double ratio, int pos, int neg) {
            this.ratio = ratio;
            this.neg = neg;
            this.pos = pos;
            this.diff = pos - neg;
        }

        public String formatRatio(double ratio) {
            return new DecimalFormat("0.00").format(ratio);
        }

        public String getRatioPercentage() {
            return formatRatio(ratio * 100) + "%";
        }

        public int getNeg() {
            return neg;
        }

        public double getRatio() {
            return ratio;
        }

        public int getPos() {
            return pos;
        }

        public int getDiff() {
            return diff;
        }
    }

    public static class Weapon {
        private final Ratio kd, accuracy;
        private final int headshots;
        private final String name, iwName, imageTitle;
        private final File image;

        public Weapon(String name, String category, Ratio kd, Ratio accuracy, int headshots, boolean primary) {
            this.iwName = name;
            this.name = getRealName(iwName);
            this.kd = kd;
            this.accuracy = accuracy;
            this.headshots = headshots;
            this.image = new File("src/main/resources/COD/Weapons/" + category + "/" + name + ".png");
            this.imageTitle = primary ? "Primary Weapon of Choice" : "Secondary Weapon of Choice";
        }

        public String getImageTitle() {
            return imageTitle;
        }

        public String getName() {
            return name;
        }

        public int getHeadshots() {
            return headshots;
        }

        public File getImage() {
            return image;
        }

        public String getAccuracy() {
            return accuracy.getRatioPercentage();
        }

        public int getShotsFired() {
            return accuracy.getNeg();
        }

        public String getKd() {
            return kd.formatRatio(kd.getRatio());
        }

        public int getShotsHit() {
            return accuracy.getPos();
        }

        public int getKills() {
            return kd.getPos();
        }

        public int getDeaths() {
            return kd.getNeg();
        }

        private String getRealName(String name) {
            HashMap<String, String> names = new HashMap<>();
            names.put("iw8_knife", "Combat Knife");
            names.put("iw8_me_akimboblunt", "Kali Sticks");

            names.put("iw8_pi_cpapa", ".357");
            names.put("iw8_pi_decho", ".50 GS");
            names.put("iw8_pi_golf21", "X16");
            names.put("iw8_pi_mike9", "Renetti");
            names.put("iw8_pi_mike1911", "1911");
            names.put("iw8_pi_papa320", "M19");

            names.put("iw8_la_gromeo", "PILA");
            names.put("iw8_la_juliet", "JOKR");
            names.put("iw8_la_kgolf", "Strela-P");
            names.put("iw8_la_rpapa7", "RPG-7");

            names.put("iw8_sn_crossbow", "Crossbow");
            names.put("iw8_sn_kilo98", "Kar98k");
            names.put("iw8_sn_mike14", "EBR-14");
            names.put("iw8_sn_sbeta", "MK2 Carbine");
            names.put("iw8_sn_sksierra", "SKS");

            names.put("iw8_sm_augolf", "AUG");
            names.put("iw8_sm_beta", "PP19 Bizon");
            names.put("iw8_sm_mpapa5", "MP5");
            names.put("iw8_sm_mpapa7", "MP7");
            names.put("iw8_sm_papa90", "P90");
            names.put("iw8_sm_smgolf4", "Striker-45");
            names.put("iw8_sm_uzulu", "Uzi");
            names.put("iw8_sm_victor", "Fennec");

            names.put("iw8_sh_charlie725", "725");
            names.put("iw8_sh_dpapa12", "R9-0 Shotgun");
            names.put("iw8_sh_mike26", "VLK Rogue");
            names.put("iw8_sh_oscar12", "Origin 12 Shotgun");
            names.put("iw8_sh_romeo870", "Model 680");

            names.put("iw8_me_riotshield", "Riot Shield");

            names.put("iw8_ar_akilo47", "AK-47");
            names.put("iw8_ar_asierra12", "Oden");
            names.put("iw8_ar_falima", "FAL");
            names.put("iw8_ar_falpha", "FR 5.56");
            names.put("iw8_ar_galima", "CR-56 AMAX");
            names.put("iw8_ar_kilo433", "Kilo 141");
            names.put("iw8_ar_mcharlie", "M13");
            names.put("iw8_ar_mike4", "M4A1");
            names.put("iw8_ar_scharlie", "FN SCAR 17");
            names.put("iw8_ar_sierra552", "Grau 5.56");
            names.put("iw8_ar_tango21", "RAM-7");

            names.put("iw8_lm_kilo121", "Bruen Mk9");
            names.put("iw8_lm_lima86", "SA87");
            names.put("iw8_lm_mgolf34", "MG34");
            names.put("iw8_lm_mgolf36", "Holger-26");
            names.put("iw8_lm_mkilo3", "M91");
            names.put("iw8_lm_pkilo", "PKM");

            names.put("iw8_sn_alpha50", "AX-50");
            names.put("iw8_sn_delta", "Dragunov");
            names.put("iw8_sn_hdromeo", "HDR");
            names.put("iw8_sn_xmike109", "Rytec AMR");
            return names.get(name);
        }
    }
}
