package Command.Commands;

import Command.Structure.*;
import PCPartPicker.*;
import PCPartPicker.Component.PurchaseLocation;
import Steam.Price;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Display PC component lists from https://pcpartpicker.com
 */
public class PCPartPickerCommand extends DiscordCommand {
    public PCPartPickerCommand() {
        super("[pcpartpicker list url]", "Embed PCPartPicker component lists");
        setSecret(true);
    }

    @Override
    public void execute(CommandContext context) {
        ComponentList componentList = PCPartPicker.fetchComponentListByUrl(context.getMessageContent());

        // Failed to scrape
        if(componentList == null) {
            return;
        }

        Price totalPrice = componentList.getTotalPrice();
        String desc = "**Total Price**: ";
        String priceCategory = "Price";

        if(totalPrice != null) {
            priceCategory += " (" + totalPrice.getCurrency() + ")";
            desc += "$" + totalPrice.getPrice();
        }
        else {
            desc += "-";
        }

        desc += "\n**Builder**: " + context.getMember().getAsMention();

        PageableEmbed<Component> componentMessage = new PageableTableEmbed<Component>(
                context,
                componentList.getComponents(),
                PCPartPicker.LOGO,
                null,
                componentList.getName(),
                componentList.getUrl(),
                desc,
                null,
                new String[]{"Category", "Name", priceCategory},
                5,
                EmbedHelper.PURPLE
        ) {
            @Override
            public String getNoItemsDescription() {
                return "Are there supposed to be any components in this one?";
            }

            @Override
            public String[] getRowValues(int index, Component component, boolean defaultSort) {
                String price = "-";
                PurchaseLocation purchaseLocation = component.getPurchaseLocation();

                if(purchaseLocation != null) {
                    price = "$" + purchaseLocation.getPrice().getPrice();
                    String storeUrl = purchaseLocation.getStoreUrl();
                    if(storeUrl != null) {
                        price = EmbedHelper.embedURL(price, storeUrl);
                    }
                }

                return new String[]{
                        component.getCategory().getName(),
                        EmbedHelper.embedURL(
                                StringUtils.substring(component.getName(), 0, 100),
                                component.getUrl()
                        ),
                        price
                };
            }

            @Override
            public void sortItems(List<Component> items, boolean defaultSort) {
                items.sort((c1, c2) -> {
                    int o1 = c1.getCategory().ordinal();
                    int o2 = c2.getCategory().ordinal();
                    return defaultSort ? o1 - o2 : o2 - o1;
                });
            }
        };

        context.getMessage().delete().queue(deleted -> componentMessage.showMessage());
    }

    @Override
    public boolean matches(String query, Message message) {
        return PCPartPicker.isListUrl(query);
    }
}
