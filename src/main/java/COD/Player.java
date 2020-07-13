package COD;

import Network.ApiRequest;
import Network.NetworkInfo;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Player {
    private final String name, platform, data;
    private Ratio wl, kd;
    private int streak, spm;
    private SearchAndDestroy searchAndDestroy;
    private Weapon primary, secondary;
    private AccoladeManager accoladeManager;

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
            this.accoladeManager = new AccoladeManager(player.getJSONObject("accoladeData").getJSONObject("properties"));
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

    private class AccoladeManager {
        ArrayList<Accolade> accolades;

        public AccoladeManager(JSONObject o) {
            for(String accoladeKey : o.keySet()) {
                if(o.getInt(accoladeKey) == 2){
                    System.out.println(accoladeKey + ": " + o.getInt(accoladeKey));
                }

            }
        }

        private Accolade getAccolade(String iwName) {
            ArrayList<Accolade> accolades = new ArrayList<>();
            accolades.add(new Accolade("noDeathsFromBehind", "Sixth Sense", "No deaths from behind"));
            accolades.add(new Accolade("leastAssists", "Selfish", "Fewest Assists"));
            accolades.add(new Accolade("highestMultikill", "Devastation", "Highest multikill"));
            accolades.add(new Accolade("reloads", "Load & Load", "Most reloads"));
            accolades.add(new Accolade("skippedKillcams", "Starter", "Most killcams skipped"));
            accolades.add(new Accolade("deadSilenceKills", "Silent But Deadly", "Most kills with dead silence active"));
            accolades.add(new Accolade("mostDeaths", "Ragdoll", "Most deaths"));
            accolades.add(new Accolade("mostAssists", "Wingman", "Most assists"));
            accolades.add(new Accolade("killsFromBehind", "Flanker", "Most kills from behind"));
            accolades.add(new Accolade("smgDeaths", "Run and Gunner", "Most SMG deaths"));
            accolades.add(new Accolade("pointBlankKills", "Personal Space", "Most point blank kills"));
            accolades.add(new Accolade("hipfireKills", "Sprayer", "Most hipfire kills"));
            accolades.add(new Accolade("highestRankedKills", "Regicide", "Most kills on the highest ranked scoreboard players"));
            accolades.add(new Accolade("shotsFired", "Trigger Happy", "Most shots fired"));
            accolades.add(new Accolade("smokesUsed", "Chimney", "Most smoke grenades used"));
            accolades.add(new Accolade("stimDamageHealed", "Not Today", "Most healing from stim"));
            accolades.add(new Accolade("arDeaths", "Assaulted", "Most assault rifle deaths"));
            accolades.add(new Accolade("pistolKills", "Hard Boiled", "Most pistol kills"));
            accolades.add(new Accolade("longestStreak", "Unstoppable", "Longest killstreak"));
            accolades.add(new Accolade("leastDeaths", "Juggernaut", "Fewest deaths"));
            accolades.add(new Accolade("deathsFromBehind", "Blindsided", "Most deaths from behind"));
            accolades.add(new Accolade("longshotKills", "Marksman", "Most longshots"));
            accolades.add(new Accolade("higherRankedKills", "Upriser", "Most kills on higher ranked scoreboard players"));
            accolades.add(new Accolade("mostMultikills", "Genocidal", "Most multikills"));
            accolades.add(new Accolade("mostKills", "The Feared", "Most kills"));
            accolades.add(new Accolade("oneShotOneKills", "One Shot Kill", "Most one shot kills"));
            accolades.add(new Accolade("avengerKills", "Avenger", "Most avenger kills"));
            accolades.add(new Accolade("sniperDeaths", "Zeroed In", "Most sniper deaths"));
            accolades.add(new Accolade("comebackKills", "Rally", "Most comebacks"));
            accolades.add(new Accolade("explosionsSurvived", "Shell Shocked", "Most explosions survived"));
            accolades.add(new Accolade("lowerRankedKills", "Alpha", "Most kills on lower ranked scoreboard players"));
            accolades.add(new Accolade("timeProne", "Grassy Knoll", "Most time spent prone"));
            accolades.add(new Accolade("headshots", "Sharpshooter", "Most headshots"));
            accolades.add(new Accolade("distanceTravelled", "Nomad", "Longest distance traveled"));
            accolades.add(new Accolade("longestLife", "Lifer", "Longest life"));
            accolades.add(new Accolade("leastKills", "The Fearful", "Fewest kills"));
            accolades.add(new Accolade("riotShieldDamageAbsorbed", "Guardian", "Most damage absorbed with riot shield"));
            accolades.add(new Accolade("shotgunDeaths", "Clay Pigeon", "Most shotgun deaths"));
            accolades.add(new Accolade("shotgunKills", "Buckshot", "Most shotgun kills"));
            accolades.add(new Accolade("sniperKills", "Sniper", "Most sniper kills"));
            accolades.add(new Accolade("fragKills", "Fragger", "Most frag grenade kills"));
            accolades.add(new Accolade("penetrationKills", "Blindfire", "Most bullet penetration kills"));
            accolades.add(new Accolade("classChanges", "Evolver", "Most classes changed"));
            accolades.add(new Accolade("arKills", "AR Specialist", "Most assault rifle kills"));
            accolades.add(new Accolade("weaponPickups", "Loaner", "Most kills with picked up weapons"));
            accolades.add(new Accolade("adsKills", "Hairtrigger", "Most ADS Kills"));
            accolades.add(new Accolade("highestAvgAltitude", "High Command", "Highest average altitude"));
            accolades.add(new Accolade("lowestAvgAltitude", "Low Profile", "Lowest average altitude"));
            accolades.add(new Accolade("shortestLife", "Terminal", "Shortest life"));
            accolades.add(new Accolade("pistolPeaths", "Hands Up", "Most pistol deaths"));
            accolades.add(new Accolade("timeCrouched", "Sneaker", "Most time spent crouched"));
            accolades.add(new Accolade("suicides", "Accident Prone", "Most suicides"));
            accolades.add(new Accolade("pistolHeadshots", "Smoking Gun", "Most pistol headshots"));
            accolades.add(new Accolade("smgKills", "CQB", "Most SMG Kills"));
            accolades.add(new Accolade("revengeKills", "Payback", "Most revenge kills"));
            accolades.add(new Accolade("claymoreKills", "Ambusher", "Most claymore kills"));
            accolades.add(new Accolade("mostKillsLongestStreak", "Grim Reaper", "Most kills and longest killstreak"));
            accolades.add(new Accolade("arHeadshots", "AR Expert", "Most assault rifle headshots"));
            accolades.add(new Accolade("stunHits", "Stunner", "Most stun grenade hits"));
            accolades.add(new Accolade("killstreakUAVAssists", "Map Awareness", "Most assists with UAVs"));
            accolades.add(new Accolade("destroyedKillstreaks", "Scrap Metal", "Most killstreaks destroyed"));
            accolades.add(new Accolade("sniperHeadshots", "Dead Aim", "Most sniper headshots"));
            accolades.add(new Accolade("killstreakKills", "Streaker", "Most killstreak kills"));
            accolades.add(new Accolade("lmgDeaths", "Target Practice", "Most LMG deaths"));
            accolades.add(new Accolade("meleeKills", "Brawler", "Most melee kills"));
            accolades.add(new Accolade("defends", "Defense", "Most defend kills"));
            accolades.add(new Accolade("killEnemyTeam", "None Spared", "Killed entire enemy team"));
            accolades.add(new Accolade("smgHeadshots", "SMG Expert", "Most SMG headshots"));
            accolades.add(new Accolade("shotgunHeadshots", "Boomstick", "Most shotgun headshots"));
            accolades.add(new Accolade("meleeDeaths", "Knocked Out", "Most melee deaths"));
            accolades.add(new Accolade("flashbangHits", "Blinder", "Most flashbang hits"));
            accolades.add(new Accolade("mostKillsLeastDeaths", "MVP", "Most kills and fewest deaths"));
            accolades.add(new Accolade("bombPlanted", "Bomb Expert", "Most plants"));
            accolades.add(new Accolade("lmgKills", "7.62mm", "Most LMG kills"));
            accolades.add(new Accolade("killstreakVTOLJetKills", "Guard Dog", "Most VTOL Jet kills"));
            accolades.add(new Accolade("mostKillsMostHeadshots", "Overkill", "Most kills and most headshots"));
            accolades.add(new Accolade("lmgHeadshots", "LMG Expert", "Most LMG headshots"));
            accolades.add(new Accolade("captures", "Captured", "Most captures"));
            accolades.add(new Accolade("bombDefused", "Defuser", "Most defuses"));
            accolades.add(new Accolade("killstreakAirstrikeKills", "Thunderstruck", "Most precision airstrike kills"));
            accolades.add(new Accolade("stoppingPowerKills", "Overpowered", "Most stopping power kills"));
            accolades.add(new Accolade("executionKills", "Executioner", "Most finishing move kills"));
            accolades.add(new Accolade("clutch", "Clutched", "Most kills as the last alive"));
            accolades.add(new Accolade("killstreakCruiseMissileKills", "Cruise Control", "Most cruise missile kills"));
            accolades.add(new Accolade("thermiteKills", "Red Iron", "Most thermite kills"));
            accolades.add(new Accolade("carepackagesCaptured", "Hoarder", "Most care packages captured"));
            accolades.add(new Accolade("killstreakCluserStrikeKills", "Shelled", "Most cluster strike kills"));
            accolades.add(new Accolade("killstreakGroundKills", "Ground Control", "Most ground based killstreak kills"));
            accolades.add(new Accolade("bombDetonated", "Destroyer", "Most targets destroyed"));
            accolades.add(new Accolade("timeWatchingKillcams", "Spy Game", "Most time watching killcams"));
            accolades.add(new Accolade("throwingKnifeKills", "Butcher", "Most throwing knife kills"));
            accolades.add(new Accolade("noKillsWithDeath", "Participant", "No kills with at least 1 death"));
            accolades.add(new Accolade("tagsDenied", "Denied", "Denied the most tags"));
            accolades.add(new Accolade("semtexKills", "Sticky", "Most semtex kills"));
            accolades.add(new Accolade("launcherKills", "Explosivo", "Most launcher kills"));
            accolades.add(new Accolade("killstreakWheelsonKills", "Roll Out", "Most wheelson kills"));
            accolades.add(new Accolade("killstreakChopperGunnerKills", "Hunter", "Most chopper gunner kills"));
            accolades.add(new Accolade("killstreakChopperSupportKills", "Twin Cannons", "Most support helo kills"));
            accolades.add(new Accolade("killstreakCUAVAssists", "Jammed", "Most assists with scrambler drones"));
            accolades.add(new Accolade("tagsCaptured", "Confirmed Kills", "Collected the most tags"));
            accolades.add(new Accolade("molotovKills", "Arsonist", "Most molotov kills"));
            accolades.add(new Accolade("launcherDeaths", "Fubar", "Most launcher deaths"));
            accolades.add(new Accolade("launcherHeadshots", "Heads Up", "Most launcher headshots"));
            accolades.add(new Accolade("proximityMineKills", "Trapper", "Most proximity mine kills"));
            accolades.add(new Accolade("snapshotHits", "Photographer", "Most snapshot grenade hits"));
            accolades.add(new Accolade("decoyHits", "Made You Look", "Most decoy grenade hits"));
            accolades.add(new Accolade("noKillNoDeath", "AFK", "No kills and no deaths"));
            accolades.add(new Accolade("gasHits", "Gaseous", "Most gas grenade hits"));
            accolades.add(new Accolade("kills10NoDeaths", "The Show", "10 or more kills and no deaths"));
            accolades.add(new Accolade("killstreakWhitePhosphorousKillsAssists", "Burnout", "Most kills and assists with white phosphorous"));
            accolades.add(new Accolade("deployableCoverUsed", "Combat Engineer", "Most deployable covers used"));
            accolades.add(new Accolade("killstreakGunshipKills", "Death From Above", "Most gunship kills"));
            accolades.add(new Accolade("killstreakPersonalUAVKills", "Nothing Personal", "Most kills with a personal radar active"));
            accolades.add(new Accolade("killstreakJuggernautKills", "Heavy Metal", "Most juggernaut kills"));
            accolades.add(new Accolade("timeOnPoint", "King of the Hill", "Most time spent on the objective"));
            accolades.add(new Accolade("killstreakTankKills", "Bulldozer", "Most infantry assault vehicle kills"));
            accolades.add(new Accolade("killstreakSentryGunKills", "Proxy Kill", "Most sentry gun kills"));
            accolades.add(new Accolade("assaults", "Offense", "Most assault kills"));
            accolades.add(new Accolade("spawnSelectBase", "Home Base", "Most base spawns"));
            accolades.add(new Accolade("noKill10Deaths", "Mission Failed", "No kills and at least 10 deaths"));
            accolades.add(new Accolade("returns", "Flag Returner", "Most flags returned"));
            accolades.add(new Accolade("tagsMegaBanked", "Megabank", "Megabanked Tags"));
            accolades.add(new Accolade("empDroneHits", "Short Circuit", "Most hits with EMP drone"));
            accolades.add(new Accolade("ammoBoxUsed", "Provider", "Most munitions boxes used"));
            accolades.add(new Accolade("carrierKills", "Carrier", "Most kills as carrier"));
            accolades.add(new Accolade("spawnSelectSquad", "Squad Goals", "Most squadmate spawns"));
            accolades.add(new Accolade("spawnSelectVehicle", "Oscar Mike", "Most vehicle spawns"));
            accolades.add(new Accolade("timeSpentAsPassenger", "Navigator", "Most time spent as a passenger"));
            accolades.add(new Accolade("firstInfected", "Patient Zero", "First infected"));
            accolades.add(new Accolade("killstreakAUAVAssists", "Target Rich Environment", "Most advanced UAV assists"));
            accolades.add(new Accolade("infectedKills", "Contagion", "Most kills as infected"));
            accolades.add(new Accolade("killstreakShieldTurretKills", "Shielded", "Most shield turret kills"));
            accolades.add(new Accolade("reconDroneMarks", "Fly On The Wall", "Most targets marked with recon drone"));
            accolades.add(new Accolade("c4Kills", "Handle With Care", "Most C4 kills"));
            accolades.add(new Accolade("killstreakAirKills", "Air Superiority","Most aerial killstreak kills"));
            accolades.add(new Accolade("defenderKills", "Savior","Most teammates saved"));
            HashMap<String, Accolade> test = new HashMap<>();
            for(Accolade a : accolades) {
                test.put(a.getIwName(), a);
            }
            return test.get(iwName);
        }
    }

    private class Accolade implements Comparable<Accolade> {
        private int quantity;
        private final String iwName, title, desc;

        public Accolade(String iwName, String title, String desc) {
            this.iwName = iwName;
            this.title = title;
            this.desc = desc;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
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
        public int compareTo(@NotNull Player.Accolade o) {
            return o.getQuantity() - this.getQuantity();
        }

    }
}
