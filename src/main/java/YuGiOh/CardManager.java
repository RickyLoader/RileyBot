package YuGiOh;

import Network.NetworkRequest;
import Network.NetworkResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import static YuGiOh.CardType.*;

/**
 * Manage Yu-Gi-Oh card fetching/caching
 */
public class CardManager {
    private final HashMap<String, Card> cards = new HashMap<>();
    private final int STATIC_CARD = -1;

    /**
     * Add some static cards
     */
    public CardManager() {
        Card alf = new Card(
                "Alf Stewart",
                STATIC_CARD,
                new CardType(TYPE.SPELL_CARD),
                new CardStats.CardStatsBuilder()
                        .setUpvotes(0)
                        .setDownvotes(1000000)
                        .setTotalViews(30000000)
                        .setWeeklyViews(150000)
                        .build(),
                new String[]{
                        "https://i.imgur.com/ijgYQtv.jpg",
                        "https://i.imgur.com/KNopt6R.jpg",
                        "https://i.imgur.com/iUTiMZV.jpg",
                        "https://i.imgur.com/mlq69cj.jpg",
                        "https://i.imgur.com/5LpQSyt.jpg"
                }
        );
        alf.setUrl("https://en.wikipedia.org/wiki/Alf_Stewart");
        cards.put("alf stewart", alf);
        cards.put("alf", alf);
    }

    /**
     * Get a Yu-Gi-Oh card by name
     * Check the currently mapped cards and then attempt to locate the card by an
     * exact match, before falling back to a fuzzy match
     * Map the card to its name for faster re-retrieval
     *
     * @param cardName Name of card
     * @return Desired Yu-Gi-Oh card or null
     */
    public Card getCard(String cardName) {
        String key = cardName.toLowerCase();
        if(cards.containsKey(key)) {
            Card cached = cards.get(key);
            if(cached.getId() == STATIC_CARD) {
                cached.resetImageIndex();
                return cached;
            }

            // Deep copy as to not maintain image index
            return new Card(
                    cached.getName(),
                    cached.getId(),
                    cached.getType(),
                    cached.getStats(),
                    cached.getImages()
            );
        }
        Card card = cardSearch(cardName, true);
        if(card == null) {
            card = cardSearch(cardName, false);
        }
        if(card != null) {
            cards.put(card.getName().toLowerCase(), card);
        }
        return card;
    }

    /**
     * Search the API for a card of the given name
     * Perform either a fuzzy search or exact search
     *
     * @param cardName Name of card to search
     * @param exact    Exact search
     * @return Desired card or null
     */
    private Card cardSearch(String cardName, boolean exact) {
        NetworkResponse response = new NetworkRequest(
                "https://db.ygoprodeck.com/api/v7/cardinfo.php?"
                        + (exact ? "name" : "fname") + "=" + cardName + "&misc=yes",
                false
        ).get();

        // Card not found
        if(response.code == 400) {
            return null;
        }

        JSONArray cards = new JSONObject(response.body).getJSONArray("data");

        return exact ? parseCard(cards.getJSONObject(0)) : getMostPopularCard(cards);
    }

    /**
     * Get the most popular card from the provided list of cards
     * Used when fuzzy searching
     *
     * @param cards JSONArray of card data
     * @return Most popular card
     */
    private Card getMostPopularCard(JSONArray cards) {
        ArrayList<Card> results = new ArrayList<>();
        for(int i = 0; i < cards.length(); i++) {
            results.add(parseCard(cards.getJSONObject(i)));
        }
        results.sort((o1, o2) -> Long.compare(o2.getStats().getUpvotes(), o1.getStats().getUpvotes()));
        return results.get(0);
    }

    /**
     * Parse the popularity stats from the provided json into an object
     *
     * @param cardObj JSON representing card
     * @return Card popularity stats
     */
    private CardStats parseCardStats(JSONObject cardObj) {
        JSONObject stats = cardObj.getJSONArray("misc_info").getJSONObject(0);
        return new CardStats.CardStatsBuilder()
                .setTotalViews(stats.getLong("views"))
                .setWeeklyViews(stats.getLong("viewsweek"))
                .setUpvotes(stats.getLong("upvotes"))
                .setDownvotes(stats.getLong("downvotes"))
                .build();
    }

    /**
     * Parse a JSON object representing a Yu-Gi-Oh card in to
     * an instance of card
     *
     * @param cardObj JSON representing card
     * @return Yu-Gi-Oh card from JSON
     */
    private Card parseCard(JSONObject cardObj) {
        JSONArray artworkData = cardObj.getJSONArray("card_images");
        String[] artwork = new String[artworkData.length()];

        for(int i = 0; i < artworkData.length(); i++) {
            artwork[i] = artworkData.getJSONObject(i).getString("image_url");
        }

        return new Card(
                cardObj.getString("name"),
                cardObj.getLong("id"),
                new CardType(TYPE.byName(cardObj.getString("type"))),
                parseCardStats(cardObj),
                artwork
        );
    }

    /**
     * Get a random Yu-Gi-Oh card
     * The random card endpoint does not allow retrieving of popularity stats.
     * To get around this, use the endpoint to get a random card, and make another request
     * with the name of this card
     *
     * @return Random card or null (Name can occasionally not return any card)
     */
    public Card getRandomCard() {
        String randomCardName = new JSONObject(
                new NetworkRequest("https://db.ygoprodeck.com/api/v7/randomcard.php", false).get().body
        ).getString("name");
        return getCard(randomCardName);
    }
}
