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
        String path = "/Commands/" + filename;
        JSONObject file = readJSONFile(path);

        if(file == null) {
            System.out.println("MISSING: " + path);
            return null;
        }

        JSONArray list = file.getJSONArray(root);
        for(int i = 0; i < list.length(); i++) {
            String link = list.getJSONObject(i).getString("possibility");
            links.add(link);
        }
        return links.toArray(new String[0]);
    }
}
