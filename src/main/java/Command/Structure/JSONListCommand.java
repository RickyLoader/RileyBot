package Command.Structure;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class JSONListCommand extends DiscordCommand {
    private final String[] list;

    public JSONListCommand(String trigger, String desc, String filename, String root) {
        super(trigger, desc);
        this.list = parseJSON(filename, root);
    }

    public JSONListCommand(String trigger, String desc, String helpName, String filename, String root) {
        super(trigger, desc, helpName);
        this.list = parseJSON(filename, root);
    }

    public String[] getList() {
        return list;
    }

    private String[] parseJSON(String filename, String root) {
        ArrayList<String> links = new ArrayList<>();
        System.out.println("Loading " + getTrigger() + "...");
        try {
            JSONObject jo = readJSONFile(filename);
            JSONArray ja = jo.getJSONArray(root);
            for(int i = 0; i < ja.length(); i++) {
                JSONObject o = (JSONObject) ja.get(i);
                links.add(o.getString("possibility"));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return links.toArray(new String[0]);
    }
}