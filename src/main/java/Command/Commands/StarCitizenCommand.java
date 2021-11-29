package Command.Commands;

import Command.Structure.*;
import StarCitizen.Ship;
import StarCitizen.Ship.Manufacturer;
import StarCitizen.Ship.Measurements;
import StarCitizen.Ship.Measurements.SHIP_SIZE;
import StarCitizen.Ship.StructuralDetails;
import StarCitizen.ShipManager;
import Steam.Price;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Look up ships/players for Star Citizen
 */
public class StarCitizenCommand extends DiscordCommand {
    private final static String
            TRIGGER = "sc",
            RANDOM = "random",
            REFRESH = "refresh",
            ALL = "all",
            SHIP_HELP = TRIGGER + " [query/" + RANDOM + "/" + ALL + "]",
            MISSING_VALUE = "-",
            MESSAGE_HELP = "Try: " + SHIP_HELP,
            LOGO = "https://i.imgur.com/CBK0Tbu.png";

    private static final ShipManager SHIP_MANAGER = ShipManager.getInstance();

    public StarCitizenCommand() {
        super(
                TRIGGER,
                "View Star Citizen ships!",
                SHIP_HELP + "\n" + TRIGGER + " " + REFRESH + "\n[ship store URL]"
        );
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String message = context.getLowerCaseMessage();

        // URL to a ship, attempt to find the ship and send a message
        if(ShipManager.isShipUrl(message)) {
            Ship ship = SHIP_MANAGER.getShipByUrl(message);

            // Failed to fetch ship matching URL
            if(ship == null) {
                return;
            }

            context.getMessage().delete().queue(deleted -> displayShipMessage(context, ship, context.getMember()));
            return;
        }

        final String query = message.replaceFirst(getTrigger(), "").trim();

        // Send help message as no query was provided
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        // Force refresh the cached ships
        if(query.equals(REFRESH)) {
            channel.sendMessage("Refreshing the ships!").queue(refreshMessageSent -> {
                channel.sendTyping().queue();
                SHIP_MANAGER.updateShips(true);
                channel.sendMessage("All done!").queue();
            });
            return;
        }

        channel.sendTyping().queue();
        switch(query) {

            // Display a random ship
            case RANDOM:
                displayShipMessage(context, SHIP_MANAGER.getRandomShip());
                break;

            // Display all ships sorted by size (largest to smallest)
            case ALL:
                displayShipsMessage(context, SHIP_MANAGER.getShips(), ALL, false);
                break;

            // Search for ships by the given query
            default:
                ArrayList<Ship> ships = SHIP_MANAGER.getShipsByName(query);

                // Show singular result
                if(ships.size() == 1) {
                    displayShipMessage(context, ships.get(0));
                }

                // Show pageable results
                else {
                    displayShipsMessage(context, ships, query, true);
                }
        }
    }

    /**
     * Display the given list of ships in a pageable embed
     *
     * @param context   Command context
     * @param ships     List of ships matching query
     * @param query     Query used to find ships
     * @param searching Results are from a search (sort by relevancy to query otherwise sort by ship size)
     */
    private void displayShipsMessage(CommandContext context, ArrayList<Ship> ships, String query, boolean searching) {
        new PageableTableEmbed<Ship>(
                context,
                ships,
                LOGO,
                "Ship Search",
                "**" + ships.size() + "** ships found for: **" + query + "**",
                MESSAGE_HELP,
                new String[]{
                        "Size",
                        "Name",
                        "Status"
                },
                5,
                ships.isEmpty() ? EmbedHelper.RED : EmbedHelper.PURPLE
        ) {
            @Override
            public String getNoItemsDescription() {
                return "There's nothing here, I'm so sorry.";
            }

            @Override
            public String[] getRowValues(int index, Ship ship, boolean defaultSort) {
                return new String[]{
                        ship.getMeasurements().getSize().name(),
                        EmbedHelper.embedURL(ship.getName(), ship.getStoreUrl()),
                        ship.getProductionStatus().name()
                };
            }

            @Override
            public void sortItems(List<Ship> items, boolean defaultSort) {

                // Sort by relevancy to query
                if(searching) {
                    items.sort(new LevenshteinDistance<Ship>(query, defaultSort) {
                        @Override
                        public String getString(Ship o) {
                            return o.getName();
                        }
                    });
                }

                // Sort by size (largest to smallest)
                else {
                    items.sort((o1, o2) -> {
                        SHIP_SIZE s1 = o1.getMeasurements().getSize();
                        SHIP_SIZE s2 = o2.getMeasurements().getSize();
                        return defaultSort ? s2.compareTo(s1) : s1.compareTo(s2);
                    });
                }
            }
        }.showMessage();
    }

    /**
     * Display the given ship in a message embed allowing paging through the images.
     *
     * @param context Command context
     * @param ship    Ship to display
     * @param member  Optional member to include in the message
     */
    private void displayShipMessage(CommandContext context, Ship ship, Member... member) {
        ShipManager.applyStoreDetails(ship);
        final Price price = ship.getPrice();

        new CyclicalPageableEmbed<String>(
                context,
                ship.getImages(),
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                StructuralDetails structuralDetails = ship.getStructuralDetails();
                final String crewSummary = structuralDetails.getCrewSummary();

                Measurements measurements = ship.getMeasurements();
                final SHIP_SIZE size = measurements.getSize();

                Manufacturer manufacturer = ship.getManufacturer();
                final String footerUrl = manufacturer.getImageUrl() == null ? LOGO : manufacturer.getImageUrl();

                String desc = "";

                final String description = ship.getDescription();

                // Truncate if too long
                if(description != null) {
                    desc += StringUtils.substring(description, 0, 100) + "...";
                }

                // Add member to description
                if(member.length > 0) {

                    // Insert gap between description
                    if(!desc.isEmpty()) {
                        desc += "\n\n";
                    }

                    desc += "**Pilot**: " + member[0].getAsMention();
                }

                return new EmbedBuilder()
                        .setTitle(ship.getName(), ship.getStoreUrl())
                        .setColor(EmbedHelper.PURPLE)
                        .setThumbnail(LOGO)
                        .setFooter(pageDetails + " | " + MESSAGE_HELP, footerUrl)
                        .setDescription(desc)

                        // Most present
                        .addField("Status", ship.getProductionStatus().name(), true)
                        .addField("Type", ship.getType().name(), true)
                        .addField("Size", size == null ? MISSING_VALUE : size.name(), true)
                        .addField("Manufacturer", ship.getManufacturer().getName(), true)
                        .addField(
                                "Len/Beam/Height",
                                measurements.getLength() + "m x "
                                        + measurements.getBeam() + "m x "
                                        + measurements.getHeight() + "m",
                                true
                        )
                        .addBlankField(true)

                        // Optional
                        .addField(getOptionalIntegerField("Mass", measurements.getMass(), "kg"))
                        .addField(getOptionalIntegerField("Cargo Capacity", structuralDetails.getCargoCapacity()))
                        .addField("Crew", crewSummary == null ? MISSING_VALUE : crewSummary, true)
                        .addField(
                                "Price",
                                price == null ? MISSING_VALUE : price.getPriceFormatted(),
                                true
                        );
            }

            /**
             * Get a message embed field with the given title and Integer value.
             * If the value is null, "-" will be used in place of the value.
             *
             * @param title Title for field - e.g "Cargo Capacity"
             * @param value Optional Integer value to display
             * @param suffix Optional suffix for the value (only included if the value is not null)
             * @return Field with given title and value
             */
            private MessageEmbed.Field getOptionalIntegerField(String title, @Nullable Integer value, String... suffix) {
                String valueString;

                // No value
                if(value == null) {
                    valueString = MISSING_VALUE;
                }

                // Value provided
                else {
                    valueString = new DecimalFormat("#,###").format(value);

                    // Add optional suffix
                    if(suffix.length > 0) {
                        valueString += suffix[0];
                    }
                }

                return new MessageEmbed.Field(title, valueString, true);
            }

            @Override
            public String getPageDetails() {
                return "Image: " + getPage() + "/" + getPages();
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, String image) {
                builder.setImage(image);
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder("No images to display!").build();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {

        // "sc" || "sc [query]" | NOT "screw you Dave"
        return query.equals(getTrigger()) || query.startsWith(getTrigger() + " ") || ShipManager.isShipUrl(query);
    }
}
