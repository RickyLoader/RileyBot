package Command.Structure;

import java.util.Arrays;
import java.util.List;

public abstract class CodImageBuilderCommand extends ImageBuilderCommand {
    private final List<String> platforms;
    private String platform;

    public CodImageBuilderCommand(String trigger, String desc) {
        super(trigger, desc, "acti/battle/psn/xbox", 30);
        platforms = Arrays.asList("battle", "acti", "xbox", "psn");
    }

    @Override
    public String stripArguments(String query) {
        String platform = query.split(" ")[0];
        if(platform.equals(getTrigger())) {
            this.platform = "acti";
        }
        else {
            this.platform = platform;
            query = query.replaceFirst(platform, "").trim();
        }
        if(query.endsWith("#0")) {
            query = query.replace("#0", "");
        }
        return query;
    }

    @Override
    public void buildImage(String name, ImageBuilder builder) {
        builder.buildImage(name, getHelpName(), platform);
    }

    @Override
    public boolean matches(String query) {
        String[] args = query.split(" ");
        return query.startsWith(getTrigger()) || platforms.contains(args[0]) && args[1].matches(getTrigger());
    }
}
