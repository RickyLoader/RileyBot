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
        return guild.getEmoteById("742984226621620234");
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public boolean hasGuild() {
        return guild != null;
    }

    public Emote getStop() {
        return guild.getEmoteById("742984225808187494");
    }

    public Emote getPlex() {
        return guild.getEmoteById("754306729155756152");
    }

    public Emote getRadarr() {
        return guild.getEmoteById("754306729860268082");
    }

    public Emote getYoutube() {
        return guild.getEmoteById("755358421678555136");
    }

    public Emote getFacebook() {
        return guild.getEmoteById("755358696162459718");
    }

    public Emote getIMDB() {
        return guild.getEmoteById("755356580613980190");
    }

    public Emote getVictory() {
        return guild.getEmoteById("742984225959051331");
    }

    public Emote getDefeat() {
        return guild.getEmoteById("742984225606860813");
    }

    public Emote getSwordHandle() {
        return guild.getEmoteById("747800793276481576");
    }

    public Emote getSwordBlade() {
        return guild.getEmoteById("747800793419350047");
    }

    public Emote getSwordTip() {
        return guild.getEmoteById("747800793511624854");
    }

    public Emote getComplete() {
        return guild.getEmoteById("740917148125364234");
    }

    public Emote getNeutral() {
        return guild.getEmoteById("740917148096135208");
    }

    public Emote getFail() {
        return guild.getEmoteById("740917148091678742");
    }

    public Emote getEmptyBeer() {
        return guild.getEmoteById("738290105227280445");
    }

    public Emote getSubtractBeer() {
        return guild.getEmoteById("738092614271893628");
    }

    public Emote getAddBeer() {
        return guild.getEmoteById("738092575340363787");
    }

    public Emote getForward() {
        return guild.getEmoteById("728480325956665395");
    }

    public Emote getBackward() {
        return guild.getEmoteById("728480346399834194");
    }

    public Emote getReverse() {
        return guild.getEmoteById("731016788732543057");
    }
}
