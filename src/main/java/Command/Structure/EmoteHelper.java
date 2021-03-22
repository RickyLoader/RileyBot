package Command.Structure;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;

public class EmoteHelper {
    private final JDA jda;

    /**
     * Create the Emote helper with the JDA
     * Allows use of emotes from any server the bot is a part of
     *
     * @param jda Instance of JDA to retrieve emotes from
     */
    public EmoteHelper(JDA jda) {
        this.jda = jda;
    }

    /**
     * Retrieve an emote by its unique id from the servers that the bot
     * is a member of
     *
     * @param id ID of emote
     * @return Emote
     */
    private Emote get(String id) {
        return jda.getEmoteById(id);
    }

    /**
     * Get the String form of an emote used to display the image within a message
     *
     * @param e Emote to convert
     * @return String version of emote
     */
    public static String formatEmote(Emote e) {
        return "<:" + e.getName() + ":" + e.getId() + ">";
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/742984226621620234.png"/>
     */
    public Emote getUndo() {
        return get("742984226621620234");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/742984225808187494.png"/>
     */
    public Emote getStop() {
        return get("742984225808187494");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/754306729155756152.png"/>
     */
    public Emote getPlex() {
        return get("754306729155756152");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/754306729860268082.png"/>
     */
    public Emote getRadarr() {
        return get("754306729860268082");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/755358421678555136.png"/>
     */
    public Emote getYoutube() {
        return get("755358421678555136");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/755358696162459718.png"/>
     */
    public Emote getFacebook() {
        return get("755358696162459718");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/755356580613980190.png"/>
     */
    public Emote getIMDB() {
        return get("755356580613980190");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/742984225959051331.png"/>
     */
    public Emote getVictory() {
        return get("742984225959051331");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/742984225606860813.png"/>
     */
    public Emote getDefeat() {
        return get("742984225606860813");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/747800793276481576.png"/>
     */
    public Emote getSwordHandle() {
        return get("747800793276481576");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/747800793419350047.png"/>
     */
    public Emote getSwordBlade() {
        return get("747800793419350047");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/747800793511624854.png"/>
     */
    public Emote getSwordTip() {
        return get("747800793511624854");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/740917148125364234.png"/>
     */
    public Emote getComplete() {
        return get("740917148125364234");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/740917148096135208.png"/>
     */
    public Emote getNeutral() {
        return get("740917148096135208");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/770129906293014568.png"/>
     */
    public Emote getDraw() {
        return get("770129906293014568");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/740917148091678742.png"/>
     */
    public Emote getFail() {
        return get("740917148091678742");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/738290105227280445.png"/>
     */
    public Emote getEmptyBeer() {
        return get("738290105227280445");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/738092614271893628.png"/>
     */
    public Emote getSubtractBeer() {
        return get("738092614271893628");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/738092575340363787.png"/>
     */
    public Emote getAddBeer() {
        return get("738092575340363787");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/728480325956665395.png"/>
     */
    public Emote getForward() {
        return get("728480325956665395");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/728480346399834194.png"/>
     */
    public Emote getBackward() {
        return get("728480346399834194");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/731016788732543057.png"/>
     */
    public Emote getReverse() {
        return get("731016788732543057");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758598371757129768.png"/>
     */
    public Emote getNightFine() {
        return get("758598371757129768");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758611377362305034.png"/>
     */
    public Emote getNightPartlyCloudy() {
        return get("758611377362305034");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758611377337663508.png"/>
     */
    public Emote getNightFewShowers() {
        return get("758611377337663508");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758617768189886465.png"/>
     */
    public Emote getNightDrizzle() {
        return get("758617768189886465");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758598371685564467.png"/>
     */
    public Emote getDayFine() {
        return get("758598371685564467");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758656025032654878.png"/>
     */
    public Emote getRain() {
        return get("758656025032654878");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758935641630179378.png"/>
     */
    public Emote getWindy() {
        return get("758935641630179378");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758598371698147328.png"/>
     */
    public Emote getDayPartlyCloudy() {
        return get("758598371698147328");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758611612843507764.png"/>
     */
    public Emote getShowers() {
        return get("758611612843507764");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758598371476111382.png"/>
     */
    public Emote getCloudy() {
        return get("758598371476111382");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758611376993730621.png"/>
     */
    public Emote getDayFewShowers() {
        return get("758611376993730621");
    }

    /**
     * <img src=https://cdn.discordapp.com/emojis/758598371719774219.png"/>
     */
    public Emote getMaxTemp() {
        return get("758598371719774219");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758598371702472745.png"/>
     */
    public Emote getMinTemp() {
        return get("758598371702472745");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758685947012710430.png"/>
     */
    public Emote getHumidity() {
        return get("758685947012710430");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758688992602095647.png"/>
     */
    public Emote getWind() {
        return get("758688992602095647");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758930591847940176.png"/>
     */
    public Emote getPressure() {
        return get("758930591847940176");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/758690648642945034.png"/>
     */
    public Emote getClothing() {
        return get("758690648642945034");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/759221421816807425.png"/>
     */
    public Emote getWindRain() {
        return get("759221421816807425");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/759961872312696912.png"/>
     */
    public Emote getSnow() {
        return get("759961872312696912");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/760119013145313310.png"/>
     */
    public Emote getDownvote() {
        return get("760119013145313310");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/760118777253462047.png"/>
     */
    public Emote getUpvote() {
        return get("760118777253462047");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/768689497327271958.png"/>
     */
    public Emote getMillionaireOptionA() {
        return get("768689497327271958");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/768689497625460776.png"/>
     */
    public Emote getMillionaireOptionB() {
        return get("768689497625460776");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/768689497294110721.png"/>
     */
    public Emote getMillionaireOptionC() {
        return get("768689497294110721");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/768689497616547840.png"/>
     */
    public Emote getMillionaireOptionD() {
        return get("768689497616547840");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/768695195783921694.png"/>
     */
    public Emote getLifeline() {
        return get("768695195783921694");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/770829814424272936.png"/>
     */
    public Emote getBlankGap() {
        return get("770829814424272936");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/779571150647721984.png"/>
     */
    public Emote getGust() {
        return get("779571150647721984");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/795108573658873857.png"/>
     */
    public Emote getDayThunder() {
        return get("795108573658873857");
    }

    /**
     * <img src="https://cdn.discordapp.com/emojis/795225898898817025.png"/>
     */
    public Emote getNextImage() {
        return get("795225898898817025");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801394652141977600.png"/>
     */
    public Emote getYellowProgressSection() {
        return get("801394652141977600");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801396267761795082.png"/>
     */
    public Emote getYellowProgressStart() {
        return get("801396267761795082");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801396225936850944.png"/>
     */
    public Emote getYellowProgressEnd() {
        return get("801396225936850944");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801399141389041674.png"/>
     */
    public Emote getRedProgressEnd() {
        return get("801399141389041674");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801397407149326336.png"/>
     */
    public Emote getRedProgressStart() {
        return get("801397407149326336");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801397407048663050.png"/>
     */
    public Emote getRedProgressSection() {
        return get("801397407048663050");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801398417762680862.png"/>
     */
    public Emote getGreenProgressEnd() {
        return get("801398417762680862");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801397677098795058.png"/>
     */
    public Emote getGreenProgressSection() {
        return get("801397677098795058");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801397677132611584.png"/>
     */
    public Emote getGreenProgressStart() {
        return get("801397677132611584");
    }

    /**
     * Get the required emotes for a green progress bar
     *
     * @return Green start, section, and end emotes
     */
    public Emote[] getGreenProgressBar() {
        return new Emote[]{
                getGreenProgressStart(),
                getGreenProgressSection(),
                getGreenProgressEnd()
        };
    }

    /**
     * Get the required emotes for a yellow progress bar
     *
     * @return Yellow start, section, and end emotes
     */
    public Emote[] getYellowProgressBar() {
        return new Emote[]{
                getYellowProgressStart(),
                getYellowProgressSection(),
                getYellowProgressEnd()
        };
    }

    /**
     * Get the required emotes for a red progress bar
     *
     * @return Red start, section, and end emotes
     */
    public Emote[] getRedProgressBar() {
        return new Emote[]{
                getRedProgressStart(),
                getRedProgressSection(),
                getRedProgressEnd()
        };
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801427904197689354.png"/>
     */
    public Emote getPollOptionA() {
        return get("801427904197689354");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801429096784592906.png"/>
     */
    public Emote getPollOptionB() {
        return get("801429096784592906");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801429893644156978.png"/>
     */
    public Emote getPollOptionC() {
        return get("801429893644156978");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/801430434860367922.png"/>
     */
    public Emote getPollOptionD() {
        return get("801430434860367922");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/802472508438609940.png"/>
     */
    public Emote getPlayers() {
        return get("802472508438609940");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/802472508112371753.png"/>
     */
    public Emote getStats() {
        return get("802472508112371753");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/804264824170217473.png"/>
     */
    public Emote getLoadouts() {
        return get("804264824170217473");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/815871333358633010.png"/>
     */
    public Emote getFacebookReactions() {
        return get("815871333358633010");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/815867467557634048.png"/>
     */
    public Emote getFacebookComments() {
        return get("815867467557634048");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/816570375793278986.png"/>
     */
    public Emote getModeBreakdown() {
        return get("816570375793278986");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/816570375676231680.png"/>
     */
    public Emote getMapBreakdown() {
        return get("816570375676231680");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/818743385027510273.png"/>
     */
    public Emote getHome() {
        return get("818743385027510273");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/818744837632950292.png"/>
     */
    public Emote getPointOfInterest() {
        return get("818744837632950292");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/818745191900905472.png"/>
     */
    public Emote getResources() {
        return get("818745191900905472");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/818745730466054147.png"/>
     */
    public Emote getCreatures() {
        return get("818745730466054147");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/819440272264921122.png"/>
     */
    public Emote getCrypto() {
        return get("819440272264921122");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820572636588277760.png"/>
     */
    public Emote getThumbsUp() {
        return get("820572636588277760");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820572636445802548.png"/>
     */
    public Emote getThumbsDown() {
        return get("820572636445802548");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820911304712912916.png"/>
     */
    public Emote getSellPrice() {
        return get("820911304712912916");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820911304611987476.png"/>
     */
    public Emote getBuyPrice() {
        return get("820911304611987476");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820911304242888706.png"/>
     */
    public Emote getHighAlch() {
        return get("820911304242888706");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820911304566243328.png"/>
     */
    public Emote getLowAlch() {
        return get("820911304566243328");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820941335573692466.png"/>
     */
    public Emote getMembers() {
        return get("820941335573692466");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820941335409852417.png"/>
     */
    public Emote getFreeToPlay() {
        return get("820941335409852417");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/820980358550913076.png"/>
     */
    public Emote getBuyLimit() {
        return get("820980358550913076");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/823492124174712892.png"/>
     */
    public Emote getRedditUpvote() {
        return get("823492124174712892");
    }

    /**
     * <img src = "https://cdn.discordapp.com/emojis/823492123872460821.png"/>
     */
    public Emote getRedditDownvote() {
        return get("823492123872460821");
    }
}