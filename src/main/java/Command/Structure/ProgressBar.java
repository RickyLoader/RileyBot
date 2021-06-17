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
        this.start = start.getAsMention();
        this.section = section.getAsMention();
        this.end = end.getAsMention();
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
        if(shouldEndProgressBar(sections, complete)) {
            progressBar.append(end);
        }
        return progressBar.toString();
    }

    /**
     * Get the number of emotes that will be used to build a progress bar with the given arguments.
     *
     * @param sections Sections to add
     * @param complete Whether to cap the progress bar with the end emote
     * @return Emote length of progress bar
     */
    public int getEmoteLength(int sections, boolean complete) {
        final int base = 1 + sections; // Start emote & sections
        return shouldEndProgressBar(sections, complete) ? base + 1 : base;
    }

    /**
     * Check whether a progress bar with the given arguments should have the end emote added.
     * This is if there is more than one section and complete is true.
     *
     * @param sections Sections to add
     * @param complete Whether to cap the progress bar with the end emote
     * @return Progress bar should have end emote added
     */
    private boolean shouldEndProgressBar(int sections, boolean complete) {
        return complete && sections > 0;
    }
}
