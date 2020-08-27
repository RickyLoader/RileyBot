package Command.Structure;

import java.util.Arrays;
import java.util.List;

public abstract class CodImageBuilderCommand extends ImageBuilderCommand {
    private final List<String> platforms;
    private String platform;

    public CodImageBuilderCommand(String trigger, String desc, String prefix) {
        super(trigger, desc, prefix, 30);
        platforms = Arrays.asList("bnet", "acti");
    }

    @Override
    public String stripArguments(String query) {
        String platform = query.split(" ")[0];
        if(platform.equals(getTrigger())) {
            this.platform = "bnet";
            return query;
        }
        this.platform = platform;
        return query.replaceFirst(platform, "").trim();
    }

    @Override
    public void buildImage(String name, ImageBuilder builder) {
        builder.buildImage(name, platform);
    }

    @Override
    public boolean matches(String query) {
        String[] args = query.split(" ");
        return query.startsWith(getTrigger()) || platforms.contains(args[0]) && args[1].matches(getTrigger());
    }
}
