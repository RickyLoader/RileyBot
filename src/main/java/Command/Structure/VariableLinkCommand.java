package Command.Structure;

import org.json.JSONObject;

import java.util.HashMap;

public class VariableLinkCommand extends DiscordCommand {
    private final HashMap<String, String> versions = new HashMap<>();

    public VariableLinkCommand(String[] variations, String trigger, String desc, String helpname, String json) {
        super(trigger, desc, helpname);
        parseJSON(json, variations);
    }

    public VariableLinkCommand(String[] variations, String trigger, String desc, String json) {
        super(trigger, desc);
        parseJSON(json, variations);
    }


    private void parseJSON(String json, String[] variations) {
        try {
            for(String trigger : variations) {
                JSONObject o = new JSONObject(json).getJSONObject(trigger);
                versions.put(trigger, o.getString("link"));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessageChannel().sendMessage(versions.get(context.getLowerCaseMessage())).queue();
    }

    @Override
    public boolean matches(String query) {
        if(versions.containsKey(query)){
            return true;
        }
        return false;
    }
}
