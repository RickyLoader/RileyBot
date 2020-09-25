package Command.Structure;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;

public class EmoteHelper {
    private Guild guild;

    public EmoteHelper(Guild guild) {
        this.guild = guild;
    }

    public EmoteHelper() {
        this.guild = null;
    }

    private Emote get(String id) {
        return guild.getEmoteById(id);
    }

    /**
     * Get the String form of an emote to display within a message
     *
     * @param e Emote to convert
     * @return String version of emote
     */
    public static String formatEmote(Emote e) {
        return "<:" + e.getName() + ":" + e.getId() + ">";
    }

    public Emote getUndo() {
        return get("742984226621620234");
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public boolean hasGuild() {
        return guild != null;
    }

    public Emote getStop() {
        return get("742984225808187494");
    }

    public Emote getPlex() {
        return get("754306729155756152");
    }

    public Emote getRadarr() {
        return get("754306729860268082");
    }

    public Emote getYoutube() {
        return get("755358421678555136");
    }

    public Emote getFacebook() {
        return get("755358696162459718");
    }

    public Emote getIMDB() {
        return get("755356580613980190");
    }

    public Emote getVictory() {
        return get("742984225959051331");
    }

    public Emote getDefeat() {
        return get("742984225606860813");
    }

    public Emote getSwordHandle() {
        return get("747800793276481576");
    }

    public Emote getSwordBlade() {
        return get("747800793419350047");
    }

    public Emote getSwordTip() {
        return get("747800793511624854");
    }

    public Emote getComplete() {
        return get("740917148125364234");
    }

    public Emote getNeutral() {
        return get("740917148096135208");
    }

    public Emote getFail() {
        return get("740917148091678742");
    }

    public Emote getEmptyBeer() {
        return get("738290105227280445");
    }

    public Emote getSubtractBeer() {
        return get("738092614271893628");
    }

    public Emote getAddBeer() {
        return get("738092575340363787");
    }

    public Emote getForward() {
        return get("728480325956665395");
    }

    public Emote getBackward() {
        return get("728480346399834194");
    }

    public Emote getReverse() {
        return get("731016788732543057");
    }

    public Emote getNightFine() {
        return get("758598371757129768");
    }

    public Emote getNightPartlyCloudy() {
        return get("758611377362305034");
    }

    public Emote getNightFewShowers() {
        return get("758611377337663508");
    }

    public Emote getNightDrizzle() {
        return get("758617768189886465");
    }

    public Emote getDayFine() {
        return get("758598371685564467");
    }

    public Emote getDayRain() {
        return get("758656025032654878");
    }

    public Emote getWindy() {
        return get("758935641630179378");
    }

    public Emote getDayPartlyCloudy() {
        return get("758598371698147328");
    }

    public Emote getShowers() {
        return get("758611612843507764");
    }

    public Emote getCloudy() {
        return get("758598371476111382");
    }

    public Emote getDayFewShowers() {
        return get("758611376993730621");
    }

    public Emote getMaxTemp() {
        return get("758598371719774219");
    }

    public Emote getMinTemp() {
        return get("758598371702472745");
    }

    public Emote getHumidity() {
        return get("758685947012710430");
    }

    public Emote getRain() {
        return get("758685946978762772");
    }

    public Emote getWind() {
        return get("758688992602095647");
    }

    public Emote getPressure() {
        return get("758930591847940176");
    }

    public Emote getClothing() {
        return get("758690648642945034");
    }
}
