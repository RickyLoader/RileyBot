package Command.Commands;

import COD.Player;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Webhook;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ImpersonateCommand extends DiscordCommand {

    public ImpersonateCommand() {
        super("impersonate [@someone]", "Impersonate a person!");
    }

    @Override
    public boolean matches(String query) {
        return query.contains("impersonate ");
    }

    @Override
    public void execute(CommandContext context) {
        Message m = context.getMessage();
        m.delete().queue();
        List<Member> mentions = m.getMentionedMembers();
        MessageChannel channel = context.getMessageChannel();
        if(mentions.isEmpty()) {
            return;
        }

        context.getGuild().retrieveWebhooks().queue(webhooks -> {
            ArrayList<Webhook> filter = webhooks.stream().filter(webhook -> webhook.getName().equalsIgnoreCase("impersonate")).collect(Collectors.toCollection(ArrayList::new));
            if(filter.isEmpty()) {
                channel.sendMessage("I need a webhook named ```impersonate``` to do that cunt").queue();
                return;
            }
            try {
                Member target = mentions.get(0);
                Webhook impersonate = filter.get(0);
                String url = impersonate.getUrl().replaceFirst("discord", "discordapp");
                WebhookClient client = new WebhookClientBuilder(url).build();
                WebhookMessage message = new WebhookMessageBuilder()
                        .setUsername(target.getEffectiveName())
                        .setAvatarUrl(target.getUser().getEffectiveAvatarUrl())
                        .setContent(getMessage(target.getEffectiveName()))
                        .build();
                client.send(message);
                client.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String getMessage(String name) {
        String[] houses = new String[]{"Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin", "Prison", "Hagrid's house", "the whomping willow", "dumbledoor's room", "the goblet of fire"};
        String[] reasons = new String[]{"I want to slobber on Hagrid's cock", "i'm not gay", "the cunt hat would put me there", "I love cum"};
        String[] spells = new String[]{"wobblio tobblio", "isuzu6speedatonus", "expecto patronum", "cumlium yumlium", "go go gadget extendocock"};
        String[] quotes = new String[]{
                "Hey! I'm " + name + " and i'm gay!",
                "There's nothing I love more than slobbering on a rock hard hog and feeling the cum drip out of my mouth and down my chest",
                "Check out my favourite website: https://www.pornhub.com/gayporn",
                "I love cum",
                "@everyone Does anybody want to play with me?",
                "One time I tried to measure my wriggly with a measuring tape and it snapped back and circumcised me =(",
                "Hey @everyone I just wanted to get out in front and say it here first before you hear from someone else, i'm gay. Hope this doesn't change anything, cheers",
                "@everyone Anyone up for a group sesh? Jump in the chat & we can stream the hub and time when we cum so we both do at the same time and stuff",
                "Yum yum cum cum",
                "@everyone I'm starting up league if anyone wants to join",
                "@everyone I can't stop thinking about slobbering on kim possible",
                "If I was at hogwarts I would be in " + houses[new Random().nextInt(houses.length)] + " because " + reasons[new Random().nextInt(reasons.length)] + " and my special spell would be " + spells[new Random().nextInt(spells.length)]
        };
        return quotes[new Random().nextInt(quotes.length)];
    }
}
