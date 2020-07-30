package Command.Structure;

import net.dv8tion.jda.api.entities.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class RandomCommand extends DiscordCommand {
    String[] possibilities;
    String filename, root;

    public RandomCommand(String trigger, String desc, String helpName, String filename, String root) {
        super(trigger, desc, helpName);
        this.filename = filename;
        this.root = root;
        this.possibilities = parseJSON();
        super.setTrigger(addTriggerRegex(trigger));
    }

    public RandomCommand(String trigger, String desc, String filename, String root) {
        super(trigger, desc);
        this.filename = filename;
        this.root = root;
        this.possibilities = parseJSON();
        super.setTrigger(addTriggerRegex(trigger));
    }

    private String addTriggerRegex(String trigger) {
        return trigger + " ?\\d?";
    }

    public String[] getPossibilities() {
        return possibilities;
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().complete();
        for(int i = 0; i < getQuantity(context.getMessage()); i++) {
            String link = getLink();
            context.getMessageChannel().sendMessage(link).queue();
        }
    }

    public String getLink() {
        Random rand = new Random();
        return possibilities[rand.nextInt(possibilities.length)];
    }

    /**
     * Attempts to pull an integer from the given message when a Random type command is called.
     *
     * @return The integer or 1 if not found. Determines how many times to repeat the command
     */
    private int getQuantity(Message message) {
        String text = message.getContentDisplay();
        try {
            int quantity = Integer.parseInt(text.split(" ")[1]);
            return quantity == 0 ? 1 : quantity;
        }
        catch(Exception e) {
            return 1;
        }
    }

    private String[] parseJSON() {
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
