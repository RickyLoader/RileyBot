package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class TurboCommand extends VariableLinkCommand {
    public TurboCommand() {
        super("turbo specialists", "The turbo specialists!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("turbo", "https://i.lensdump.com/i/AThThm.jpg");
        versions.put("turbobob", "https://image.ibb.co/c0zrQq/Jt2kERs.png");
        versions.put("turbogreg", "https://image.ibb.co/ewVbrV/jGGKgmr.jpg");
    }
}
