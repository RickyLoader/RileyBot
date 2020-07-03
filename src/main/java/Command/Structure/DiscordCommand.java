package Command.Structure;

import java.util.regex.Pattern;

public abstract class DiscordCommand {
    private String trigger;
    private final String desc;
    private final String helpName;

    public DiscordCommand(String trigger, String desc, String helpName) {
        this.trigger = trigger;
        this.helpName = helpName;
        this.desc = desc;
    }

    public DiscordCommand(String trigger, String desc) {
        this.trigger = trigger;
        this.helpName = trigger;
        this.desc = desc;
    }

    public abstract void execute(CommandContext context);

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getDesc() {
        return desc;
    }

    public String getHelpName() {
        return helpName;
    }

    public boolean matches(String query) {
        return Pattern.compile(this.getTrigger().toLowerCase()).matcher(query).matches();
    }
}
