package Command.Structure;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public abstract class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if(event.getUser().isBot()) {
            return;
        }
        handleButtonClick(event);
    }

    /**
     * Handle the given button click event
     *
     * @param event Button click event
     */
    public abstract void handleButtonClick(@NotNull ButtonClickEvent event);
}
