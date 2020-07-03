package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class KillListCommand extends DiscordCommand {

    public KillListCommand() {
        super("kill list", "Check out who is on the kill list!");
    }

    @Override
    public void execute(CommandContext context) {
        StringBuilder summary = new StringBuilder();
        User author = context.getUser();
        List<Member> targets = context.getTargets();

        if(targets.isEmpty()) {
            summary.append("There are no targets sir");
        }
        else {
            String codeBlock = "```";
            summary
                    .append("Hello sir, here are the pending targets:\n")
                    .append(codeBlock)
                    .append("NUMBER TARGET NAME")
                    .append(codeBlock)
                    .append(codeBlock);

            int i = 1;
            for(Member target : targets) {
                summary.append(i).append(target.getUser().getName()).append("\n");
                i++;
            }
            summary
                    .append(codeBlock)
                    .append("\n\nExecute order 66 when you're ready sir");

        }
        PrivateChannel pm = author.openPrivateChannel().complete();
        pm.sendMessage(summary.toString()).queue();
        pm.close();
    }
}