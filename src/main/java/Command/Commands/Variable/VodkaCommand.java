package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class VodkaCommand extends VariableLinkCommand {
    public VodkaCommand() {
        super("spirits", "Nothing like a nice refreshing glug of vodka!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("vodka", "https://i.lensdump.com/i/ATh6zo.jpg");
        versions.put("vodka2", "https://cdn.discordapp.com/attachments/602077196571377675/738970352213819453/Screenshot_20200801_160327_com.huawei.himovie.overseas.jpg");
        versions.put("codka", "https://i.lensdump.com/i/AE23mc.jpg");
    }
}
