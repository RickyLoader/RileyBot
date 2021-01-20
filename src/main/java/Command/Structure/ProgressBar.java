package Command.Structure;

import net.dv8tion.jda.api.entities.Emote;

/**
 * Create a progress bar
 */
public class ProgressBar {
    private final String start, section, end;

    /**
     * Initialise the progress bar with the required emotes
     *
     * @param start   Emote to use as the start of the progress bar
     * @param end     Emote to use as the end of the progress bar
     * @param section Emote to use as a section of the progress bar
     */
    public ProgressBar(Emote start, Emote section, Emote end) {
        this.start = EmoteHelper.formatEmote(start);
        this.section = EmoteHelper.formatEmote(section);
        this.end = EmoteHelper.formatEmote(end);
    }

    /**
     * Create a progress bar with the given number of sections
     *
     * @param sections Sections to add
     * @param complete Whether to cap the progress bar with the end emote
     * @return Progress bar emote String
     */
    public String build(int sections, boolean complete) {
        StringBuilder progressBar = new StringBuilder(start);
        for(int i = 0; i < sections; i++) {
            progressBar.append(section);
        }
        if(sections > 0 && complete) {
            progressBar.append(end);
        }
        return progressBar.toString();
    }
}
