package DOND;

import Bot.ResourceHandler;
import Command.Commands.DealOrNoDealCommand;
import Command.Structure.EmbedHelper;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageLoadingMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.*;

/**
 * Deal or No Deal game
 */
public class DealOrNoDeal {
    private final Member contestant;
    private final MessageChannel channel;
    private ArrayList<Briefcase> briefcases, availableCases;
    private Briefcase selected;
    private static Font FONT;
    private long id;
    private boolean visible, finalOffer, running;
    private GAME_STATUS status;
    private int casesToOpen, roundCases, maxOffer = 0;
    private final LinkedList<Integer> offers;
    private final double[] offerMultipliers = new double[]{0.11, 0.15, 0.22, 0.37, 0.70, 0.90, 1.0};
    private final static String PATH = "/DOND/", LOGO = "https://i.imgur.com/YKIJisy.png";

    private static BufferedImage
            BRIEFCASE_CLOSED,
            BRIEFCASE_CLOSED_CHOSEN,
            BRIEFCASE_OPEN_CHOSEN,
            BRIEFCASE_OPEN_HIGH,
            BRIEFCASE_OPEN_LOW,
            PRIZE_LOW,
            PRIZE_HIGH,
            PRIZE_WON,
            PRIZE_LOST,
            VALUE_CONTAINER,
            BRIEFCASE_CONTAINER,
            BANNER,
            BANKER_ON_PHONE,
            BANKER_SAD,
            BANKER_HAPPY;

    private enum GAME_STATUS {
        SELECTING_CASE,
        SELECTED_CASE,
        OPENING_CASES,
        DEAL_OFFERED,
        DEAL_ACCEPTED,
        DEAL_DECLINED,
        OFFER_SWAP,
        FORFEIT,
        FINALE
    }

    /**
     * Create a game of Deal or No Deal
     *
     * @param contestant Member to play
     * @param channel    Channel to play in
     */
    public DealOrNoDeal(Member contestant, MessageChannel channel) {
        this.contestant = contestant;
        this.channel = channel;
        this.offers = new LinkedList<>();
    }

    /**
     * Register the Deal or No Deal font and image assets
     *
     * @param handler Resource handler
     */
    public static void registerAssets(ResourceHandler handler) {
        FONT = ImageBuilder.registerFont(PATH + "dond.ttf", handler);
        BRIEFCASE_CLOSED = handler.getImageResource(PATH + "briefcase_closed.png");
        BRIEFCASE_CLOSED_CHOSEN = handler.getImageResource(PATH + "briefcase_closed_chosen.png");
        BRIEFCASE_OPEN_CHOSEN = handler.getImageResource(PATH + "briefcase_open_chosen.png");
        BRIEFCASE_OPEN_HIGH = handler.getImageResource(PATH + "briefcase_open_high.png");
        BRIEFCASE_OPEN_LOW = handler.getImageResource(PATH + "briefcase_open_low.png");
        PRIZE_LOW = handler.getImageResource(PATH + "prize_low.png");
        PRIZE_HIGH = handler.getImageResource(PATH + "prize_high.png");
        PRIZE_WON = handler.getImageResource(PATH + "prize_won.png");
        PRIZE_LOST = handler.getImageResource(PATH + "prize_lost.png");
        VALUE_CONTAINER = handler.getImageResource(PATH + "value_container.png");
        BRIEFCASE_CONTAINER = handler.getImageResource(PATH + "briefcase_container.png");
        BANNER = handler.getImageResource(PATH + "banner.png");
        BANKER_ON_PHONE = handler.getImageResource(PATH + "banker_making_call.png");
        BANKER_SAD = handler.getImageResource(PATH + "banker_sad.png");
        BANKER_HAPPY = handler.getImageResource(PATH + "banker_happy.png");
    }

    /**
     * Check if game is currently running
     *
     * @return Game is running
     */
    public boolean isActive() {
        return running;
    }

    /**
     * Start the game
     * Get the reward values & assign randomly to briefcases
     */
    public void start() {
        this.briefcases = new ArrayList<>();
        ArrayList<Double> rewards = getRewards();
        Random rand = new Random();
        final int totalRewards = rewards.size();
        int currentRewards = totalRewards;

        for(int i = 0; i < totalRewards; i++) {
            briefcases.add(
                    new Briefcase(
                            i + 1,
                            rewards.remove(rand.nextInt(currentRewards))
                    )
            );
            currentRewards--;
        }

        this.running = true;
        this.status = GAME_STATUS.SELECTING_CASE;
        sendGameMessage(buildGameMessage());
    }

    /**
     * Forfeit the game and open all the briefcases
     */
    public void forfeit() {
        status = GAME_STATUS.FORFEIT;
        for(Briefcase briefcase : briefcases) {
            briefcase.openCase();
        }
        updateGame();
    }

    /**
     * Send the given game message to the channel and remember the id
     * Mark the game as visible
     *
     * @param gameMessage Game message embed
     */
    private void sendGameMessage(MessageEmbed gameMessage) {
        channel.sendMessage(gameMessage).addFile(
                ImageLoadingMessage.imageToByteArray(buildGameImage()),
                "image.png"
        ).queue(message -> {
            id = message.getIdLong();
            visible = true;
        });
    }

    /**
     * Build the game message embed
     *
     * @return Game message
     */
    private MessageEmbed buildGameMessage() {
        return new EmbedBuilder()
                .setColor(EmbedHelper.getYellow())
                .setTitle(contestant.getEffectiveName() + " | Deal or No Deal")
                .setDescription(getGameStatusMessage())
                .setImage("attachment://image.png")
                .setThumbnail(LOGO)
                .setFooter(getHelpMessage(), LOGO)
                .build();
    }

    /**
     * Get the help message to display in the game embed footer, based on the current game status
     *
     * @return Game status help message
     */
    private String getHelpMessage() {
        switch(status) {
            case SELECTING_CASE:
                return "Type " + DealOrNoDealCommand.SELECT_CASE + " to select your case";
            case OPENING_CASES:
                return "Type " + DealOrNoDealCommand.OPEN_CASE + " to open a case";
            case DEAL_OFFERED:
                return "Type " + DealOrNoDealCommand.DEAL_OFFER + " to answer the banker";
            case OFFER_SWAP:
                return "Type " + DealOrNoDealCommand.SWAP_OFFER + " to make your choice";
            default:
                return "Type " + DealOrNoDealCommand.TRIGGER + " for help";
        }
    }

    /**
     * Format the given reward to a String
     * 10000 -> $10,000
     *
     * @param reward Reward to format
     * @return Formatted reward
     */
    private String formatReward(double reward) {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return "$" + format.format(reward);
    }

    /**
     * Answer the banker's offer
     * Open the selected case to reveal the reward which was sold to the banker
     *
     * @param accept Offer accepted
     */
    public void answerOffer(boolean accept) {
        if(status != GAME_STATUS.DEAL_OFFERED) {
            channel.sendMessage(
                    contestant.getAsMention() + " I didn't hear the banker call, did you?"
            ).queue();
            return;
        }
        if(accept) {
            selected.openCase();
            status = GAME_STATUS.DEAL_ACCEPTED;
        }
        else {
            status = GAME_STATUS.DEAL_DECLINED;
        }
        updateGame();
    }

    /**
     * Answer the offer to swap cases with the final case
     * Open the selected and final cases to reveal their rewards
     *
     * @param swap Accept to swap
     */
    public void answerSwap(boolean swap) {
        if(status != GAME_STATUS.OFFER_SWAP) {
            channel.sendMessage(
                    contestant.getAsMention() + " You're not there yet bro"
            ).queue();
            return;
        }

        if(swap) {
            Briefcase newSelection = availableCases.get(0);
            availableCases.add(selected);
            availableCases.remove(newSelection);
            selected = newSelection;
        }

        availableCases.get(0).openCase();
        selected.openCase();

        status = GAME_STATUS.FINALE;
        updateGame();
    }

    /**
     * Build the game image
     * Display available rewards on the left and right side of the briefcases
     *
     * @return Game image
     */
    private BufferedImage buildGameImage() {
        BufferedImage image = new BufferedImage(
                BANNER.getWidth() + (2 * VALUE_CONTAINER.getWidth()),
                VALUE_CONTAINER.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        ArrayList<Briefcase> rewards = new ArrayList<>(briefcases);
        rewards.sort(Comparator.comparingDouble(Briefcase::getReward));

        BufferedImage leftRewards = buildRewardStackImage(
                rewards.subList(0, 13).toArray(new Briefcase[0])
        );
        BufferedImage rightRewards = buildRewardStackImage(
                rewards.subList(13, rewards.size()).toArray(new Briefcase[0])
        );

        Graphics g = image.getGraphics();
        g.drawImage(leftRewards, 0, 0, null);
        g.drawImage(BANNER, leftRewards.getWidth(), 0, null);
        g.drawImage(buildCaseAreaImage(), leftRewards.getWidth(), BANNER.getHeight(), null);
        g.drawImage(rightRewards, leftRewards.getWidth() + BANNER.getWidth(), 0, null);
        g.dispose();
        return image;
    }

    /**
     * Build the image displaying the briefcases
     * Draw the banker if an offer is being made
     *
     * @return Briefcase container image
     */
    private BufferedImage buildCaseAreaImage() {
        BufferedImage image = new BufferedImage(
                BRIEFCASE_CONTAINER.getWidth(),
                BRIEFCASE_CONTAINER.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = image.getGraphics();
        g.drawImage(BRIEFCASE_CONTAINER, 0, 0, null);

        int xGap = 35, yGap = 25, x = xGap, y = yGap;

        for(Briefcase briefcase : briefcases) {
            BufferedImage briefcaseImage = buildBriefcaseImage(briefcase);
            g.drawImage(briefcaseImage, x, y, null);

            if(briefcase.getCaseNumber() % 5 == 0) {
                x = xGap;
                y += briefcaseImage.getHeight() + yGap;
            }
            else {
                x += briefcaseImage.getWidth() + xGap;
            }
        }

        BufferedImage banker = null;
        if(status == GAME_STATUS.DEAL_OFFERED) {
            banker = BANKER_ON_PHONE;
        }
        else if(status == GAME_STATUS.DEAL_ACCEPTED) {
            banker = offers.getFirst() >= selected.getReward() ? BANKER_SAD : BANKER_HAPPY;
        }
        if(banker != null) {
            g.drawImage(
                    banker,
                    image.getHeight() - banker.getHeight(),
                    image.getWidth() - banker.getWidth(),
                    null
            );
        }
        g.dispose();
        return image;
    }

    /**
     * Build an image displaying the briefcase.
     * Use either the case number or reward based on whether it has been opened
     * Use different images to denote high/low value rewards when opened
     *
     * @param briefcase Briefcase to draw
     * @return Image displaying briefcase
     */
    private BufferedImage buildBriefcaseImage(Briefcase briefcase) {
        boolean open = briefcase.isOpened();
        boolean selectedCase = briefcase == selected;
        BufferedImage briefcaseImage;

        if(open) {
            boolean highValue = briefcase.getReward() > 1000;
            briefcaseImage = selectedCase ? BRIEFCASE_OPEN_CHOSEN : (highValue ? BRIEFCASE_OPEN_HIGH : BRIEFCASE_OPEN_LOW);
        }
        else {
            briefcaseImage = selectedCase ? BRIEFCASE_CLOSED_CHOSEN : BRIEFCASE_CLOSED;
        }

        BufferedImage image = new BufferedImage(
                briefcaseImage.getWidth(),
                briefcaseImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics g = image.getGraphics();
        g.drawImage(briefcaseImage, 0, 0, null);
        g.setFont(FONT.deriveFont(open ? 18f : 32f));
        g.setColor(open ? Color.WHITE : Color.BLACK);
        FontMetrics fm = g.getFontMetrics();

        String text = open ? briefcase.getCaseLabelReward() : String.valueOf(briefcase.getCaseNumber());
        g.drawString(
                text,
                (image.getWidth() / 2) - (fm.stringWidth(text) / 2),
                (image.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );
        g.dispose();
        return image;
    }

    /**
     * Build the reward stack to be displayed on each side of the image
     *
     * @param briefcases Briefcases in order of reward
     * @return Reward stack image
     */
    private BufferedImage buildRewardStackImage(Briefcase[] briefcases) {
        BufferedImage image = new BufferedImage(
                VALUE_CONTAINER.getWidth(),
                VALUE_CONTAINER.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics g = image.getGraphics();
        g.drawImage(VALUE_CONTAINER, 0, 0, null);
        g.setFont(FONT);
        int border = 10, x = 10, y = border;
        for(Briefcase briefcase : briefcases) {
            BufferedImage reward = buildRewardImage(briefcase);
            g.drawImage(reward, x, y, null);
            y += reward.getHeight() + border;
        }
        g.dispose();
        return image;
    }

    /**
     * Build the reward image displayed within the reward stack
     * Use a different image based on the value of the reward and whether it
     * has been revealed in the briefcase
     *
     * @param briefcase Briefcase to build reward image for
     * @return Reward image
     */
    private BufferedImage buildRewardImage(Briefcase briefcase) {
        BufferedImage background;
        if(briefcase.isOpened()) {
            background = (briefcase == selected && status == GAME_STATUS.FINALE ? PRIZE_WON : PRIZE_LOST);
        }
        else {
            background = (briefcase.getReward() < 1000 ? PRIZE_LOW : PRIZE_HIGH);
        }

        BufferedImage image = new BufferedImage(
                background.getWidth(),
                background.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics g = image.getGraphics();
        g.drawImage(background, 0, 0, null);
        g.setFont(FONT.deriveFont(22f));
        g.setColor(background == PRIZE_WON ? Color.BLACK : Color.decode("#dcdcdc"));
        FontMetrics fm = g.getFontMetrics();

        double reward = briefcase.getReward();
        String rewardString = formatReward(reward);

        g.drawString(
                rewardString,
                (image.getWidth() / 2) - (fm.stringWidth(rewardString) / 2),
                (image.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );
        g.dispose();
        return image;
    }


    /**
     * Update the game message within the channel
     * Delete the previous message and send the new one
     * Mark the game as not visible
     */
    private void updateMessage() {
        MessageEmbed gameMessage = buildGameMessage();
        channel.deleteMessageById(id).queue(success -> {
            visible = false;
            sendGameMessage(gameMessage);
        }, throwable -> sendGameMessage(gameMessage));
    }

    /**
     * Process game progression
     * Basic game logic - Open n cases - get offer - open n-1 cases... open 1 case until two remain or a deal is made
     * End conditions - Accepting an offer/Contestant case + 1 other remain, player chooses which to keep
     */
    private void updateGame() {
        switch(status) {
            case SELECTED_CASE:
                roundCases = 6;
                casesToOpen = roundCases;
                status = GAME_STATUS.OPENING_CASES;
                break;
            case OPENING_CASES:
                if(casesToOpen > 0) {
                    break;
                }
                status = GAME_STATUS.DEAL_OFFERED;
                finalOffer = availableCases.size() == 1;
                int offer = getBankerOffer();
                offers.push(offer);
                if(offer > maxOffer) {
                    maxOffer = offer;
                }
                break;
            case DEAL_DECLINED:
                roundCases = roundCases == 1 ? 1 : roundCases - 1;
                casesToOpen = roundCases;
                status = finalOffer ? GAME_STATUS.OFFER_SWAP : GAME_STATUS.OPENING_CASES;
                break;
            case FORFEIT:
            case DEAL_ACCEPTED:
            case FINALE:
                running = false;
        }
        updateMessage();
    }

    /**
     * Get the offer from the banker to buy the contestant's case (CC)
     * Calculate the expected value (EV) by taking the sum of all remaining values on the board (including CC),
     * and dividing by the number of remaining values (also including CC)
     * <p>
     * Multiply this by a value which increases with every subsequent offer (normalising at 1) to get the
     * banker's offer
     * This results in an offer far below the EV in the early rounds and an offer of the EV in later rounds
     *
     * @return Banker offer
     * @see <a href="https://bit.ly/3pYFpgB">Basic formula @David W</a>
     */
    private int getBankerOffer() {
        double expectedValue = selected.getReward();

        for(Briefcase b : availableCases) {
            expectedValue += b.getReward();
        }

        expectedValue = expectedValue / (availableCases.size() + 1);
        int index = Math.min(offers.size(), offerMultipliers.length - 1);
        return (int) Math.ceil(expectedValue * offerMultipliers[index]);
    }

    /**
     * Open a case and remove it from the list of available cases.
     *
     * @param caseNumber Case number to open
     */
    public void openCase(int caseNumber) {
        if(status != GAME_STATUS.OPENING_CASES) {
            channel.sendMessage(contestant.getAsMention() + " You aren't picking any cases right now!").queue();
            return;
        }
        if(!visible) {
            channel.sendMessage(contestant.getAsMention() + " Patience! You can't even see the cases!").queue();
            return;
        }

        Briefcase selected = getCase(caseNumber);

        if(selected == null) {
            channel.sendMessage(contestant.getAsMention() + " How can I open that?").queue();
            return;
        }
        if(selected.isOpened()) {
            channel.sendMessage(contestant.getAsMention() + " That case is already open for fuck's sake").queue();
            return;
        }
        if(selected == this.selected) {
            channel.sendMessage(contestant.getAsMention() + " That's your own case!").queue();
            return;
        }

        selected.openCase();
        availableCases.remove(selected);
        casesToOpen--;
        updateGame();
    }

    /**
     * Get a case by the case number
     * <p>
     * Case number 1 -> index 0
     *
     * @param caseNumber Case number
     * @return Briefcase or null
     */
    private Briefcase getCase(int caseNumber) {
        caseNumber--;
        if(caseNumber >= briefcases.size() || caseNumber < 0) {
            return null;
        }
        return briefcases.get(caseNumber);
    }

    /**
     * Select the case to play for
     * <p>
     * Create a copy of the briefcases & remove the selected case to represent the available rewards
     *
     * @param caseNumber Selected case number
     */
    public void selectCase(int caseNumber) {
        if(status != GAME_STATUS.SELECTING_CASE) {
            channel.sendMessage(
                    contestant.getAsMention() + " You've already selected case " + selected.getCaseNumber() + "!"
            ).queue();
            return;
        }
        Briefcase selected = getCase(caseNumber);
        if(selected == null) {
            channel.sendMessage(
                    contestant.getAsMention() + " That's not a case!"
            ).queue();
            return;
        }
        this.selected = selected;
        this.availableCases = new ArrayList<>(briefcases);
        availableCases.remove(selected);
        status = GAME_STATUS.SELECTED_CASE;
        updateGame();
    }

    /**
     * Get the ordered list of reward values
     *
     * @return Rewards
     */
    private ArrayList<Double> getRewards() {
        return new ArrayList<>(Arrays.asList(
                0.01,
                1.0,
                5.0,
                10.0,
                25.0,
                50.0,
                75.0,
                100.0,
                200.0,
                300.0,
                400.0,
                500.0,
                750.0,
                1000.0,
                5000.0,
                10000.0,
                25000.0,
                50000.0,
                75000.0,
                100000.0,
                200000.0,
                300000.0,
                400000.0,
                500000.0,
                750000.0,
                1000000.0
        ));
    }

    /**
     * Get the game status message to display in the game embed.
     * Describe the current stage of the game and what to do
     *
     * @return Game status
     */
    private String getGameStatusMessage() {
        if(status == GAME_STATUS.SELECTING_CASE) {
            return "**__Choose your briefcase__**\n\n"
                    + "Which one will it be?";
        }

        StringBuilder builder = new StringBuilder();

        if(maxOffer > 0) {
            builder
                    .append("**Max offer**: ")
                    .append(formatReward(maxOffer))
                    .append("\n");
        }

        boolean offerScreen = status == GAME_STATUS.DEAL_OFFERED;
        boolean dealScreen = status == GAME_STATUS.DEAL_ACCEPTED;

        if(offers.size() > 1 || offers.size() == 1 && !offerScreen && !dealScreen) {
            int lastOfferIndex = offerScreen || dealScreen ? 1 : 0;
            builder
                    .append("**Last offer**: ")
                    .append(formatReward(offers.get(lastOfferIndex)))
                    .append("\n\n");
        }
        else {
            builder.append("\n");
        }

        String caseReward = null, caseNumber = null;

        if(selected != null) {
            caseReward = formatReward(selected.getReward());
            caseNumber = "**Case #" + selected.getCaseNumber() + "**";
        }
        switch(status) {
            case OPENING_CASES:
                builder
                        .append("**__Briefcase opening__**").append("\n\n")
                        .append("Cases left to open: **").append(casesToOpen).append("**");
                break;
            case DEAL_OFFERED:
                builder
                        .append("**__RING RING__**").append("\n\n")
                        .append("The banker has made you an offer: **")
                        .append(formatReward(offers.getFirst()))
                        .append("**.");
                if(finalOffer) {
                    builder.append("\n\nThis is your **final offer**, good luck.");
                }
                break;
            case DEAL_ACCEPTED:
                builder
                        .append("**__DEAL__**").append("\n\n")
                        .append("You sold ").append(caseNumber).append(" to the banker for ")
                        .append("**").append(formatReward(offers.getFirst())).append("**\n")
                        .append("It contained **").append(caseReward).append("**\n\n")
                        .append(offers.getFirst() >= selected.getReward() ? "**Run!** Here he comes!" : "What a deal!");
                break;
            case OFFER_SWAP:
                builder
                        .append("**__Will you swap?__**").append("\n\n")
                        .append("You have a choice. Do you want to keep your case, or swap with the remaining case?\n\n")
                        .append("You will win the briefcase that you choose.");
                break;
            case FINALE:
                builder
                        .append("**__Congratulations!__**").append("\n\n")
                        .append("Your selected case (").append(caseNumber).append(") ")
                        .append("contained **").append(caseReward).append("**");
                if(maxOffer > selected.getReward()) {
                    builder.append("\nShould have taken that deal earlier cunt.");
                }
                break;
            case FORFEIT:
                builder.append("**__Forfeit__**")
                        .append("\n\nWhat a loser!");
                if(selected != null) {
                    builder
                            .append("\n\n")
                            .append("Your case did contain **").append(caseReward)
                            .append("**... but you get **NOTHING**.");
                }
                break;
        }
        return builder.toString();
    }
}
