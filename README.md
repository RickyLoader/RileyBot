# RileyBot

Discord Bot written in Java using the [JDA](https://github.com/DV8FromTheWorld/JDA) Discord API wrapper.
<details>
  <summary>Command Examples</summary>

  ## Commands
  <details>
  <summary>Stocks Command</summary>
 
  ### Description
  Uses the [Finnhub](https://finnhub.io/) and [Messari](https://messari.io/) APIs to search for and display stock/crypto currency prices.
  
  [View code](src/main/java/Command/Commands/StocksCommand.java)

  ### Usage
  `$query` where `query` is a stock/crypto symbol, company name, or ID.

  Alternatively, `$stock query` or `$crypto query` may be used to search specifically for stock or crypto.

  If a singular matching symbol is found, the market data for that symbol is displayed, otherwise a pageable message containing the search results will be shown.

  #### Example(s)
  ![alt text](src/main/resources/Readme/StocksCommand/single_result.gif "Single result")
  ![alt text](src/main/resources/Readme/StocksCommand/search_results.gif "Multiple results")
  </details>
  <details>
  <summary>Blitz Command</summary>
 
  ### Description
  Uses the [Blitz.gg](https://blitz.gg/) API to display League of Legends champion build data.
  
  [View code](src/main/java/Command/Commands/BlitzCommand.java)

  ### Usage
  `blitz [champion] [role]` where `champion` is the name of a champion and `role` is the name of a role.

  #### Example(s)
  ![alt text](src/main/resources/Readme/BlitzCommand/champion_search.gif "Darius champion build")
  </details>
    <details>
  <summary>Match History Command</summary>
 
  ### Description
  Uses the [COD API](https://github.com/Lierrmm/Node-CallOfDuty) to display a player's match history in Modern Warfare/Cold War.
  
  [View code](src/main/java/Command/Structure/MatchHistoryCommand.java)

  ### Usage
  `[platform] [trigger] [name] [match ID]` where `platform` is one of `XBOX, XBL, PSN, BATTLE` (optional & assumed to be Battle.net), `trigger` is one of `mwhistory, cwhistory` (depending on the game), `name` is the player name to search, and `match ID` is optional however when specified will display the player's stats for a specific match.

  #### Example(s)
  ![alt text](src/main/resources/Readme/MatchHistoryCommand/match_history.gif "View match history")
  ![alt text](src/main/resources/Readme/MatchHistoryCommand/specific_match.gif "View specific match")
  </details>
    <details>
  <summary>Deal or No Deal Command</summary>
 
  ### Description
  Play a game of Deal or No Deal using a programmatically built image as the UI.
  
  [View code](src/main/java/Command/Commands/DealOrNoDealCommand.java)

  ### Usage
  `dd start` to begin a game
  
  `dd select [case #]` to select your case
  
  `dd open [case #]` to open a case
  
  `dd [deal/no deal]` to make/deny a deal
  
  `dd forfeit` to end a game
 
  #### Example(s)
  ![alt text](src/main/resources/Readme/DealOrNoDealCommand/playing.gif "Playing Deal or No Deal")
  </details>
   <details>
  <summary>Dictionary Command</summary>
 
  ### Description
  Uses the Merriam-Webster dictionary to display definitions of a given word, pairs the definition with a random image found using the [Unsplash API - Search photos by keyword endpoint](https://unsplash.com/documentation#search-photos) (with the dictionary word as the keyword).
  
  [View code](src/main/java/Command/Commands/DictionaryCommand.java)

  ### Usage
  `define [word]` where `word` is the word to find in the dictionary.
  If more than one word is found, the results will be displayed in a pageable message.

  #### Example(s)
  ![alt text](src/main/resources/Readme/DictionaryCommand/definition.gif "Word definition")
  ![alt text](src/main/resources/Readme/DictionaryCommand/multiple_definitions.gif "Multiple results")
  </details>
     <details>
  <summary>Grand Exchange Command</summary>
 
  ### Description
  Uses the [OSRS Wiki Grand Exchange API](https://oldschool.runescape.wiki/w/RuneScape:Real-time_Prices) to search for & display item prices.
  
  [View code](src/main/java/Command/Commands/GrandExchangeCommand.java)

  ### Usage
  `g.e [query]` where `query` is either the name or ID of an item.
  If more than one item is found, the results will be displayed in a pageable message.

  #### Example(s)
  ![alt text](src/main/resources/Readme/GrandExchangeCommand/item_price.gif "Item price")
  ![alt text](src/main/resources/Readme/GrandExchangeCommand/multiple_items.gif "Multiple results")
  </details>
       <details>
  <summary>Gunfight Command</summary>
 
  ### Description
  Win/loss tracker for Modern Warfare. Buttons are used to add wins & losses, results are saved to a leaderboard.
  
  [View code](src/main/java/Command/Commands/Passive/GunfightCommand.java)

  ### Usage
  `gunfight!` to begin a session or to re-send the tracker message.
         
  `leaderboard!` to view the leaderboard.
         
  `leaderboard! [#]` to view a specific session from the leaderboard.

  #### Example(s)
  ![alt text](src/main/resources/Readme/GunfightCommand/tracker.gif "Using the tracker")
  ![alt text](src/main/resources/Readme/GunfightCommand/leaderboard.gif "Viewing the leaderboard")
 </details>
         <details>
  <summary>Hangman Command</summary>
 
  ### Description
  Play a game of Hangman using a programmatically built image as the UI. The gallows is randomly selected from a variety of options. Either choose a word to play or get the bot to pick one from the dictionary.
  
  [View code](src/main/java/Command/Commands/HangmanCommand.java)

  ### Usage
  `hm start [word]` to start a game using the given `word`.
           
  `hm ai` to start a game with a word the bot picks from the dictionary.
           
  `hm stop` to forfeit the game.
  
  `hm hint` to unlock a letter in the word.

  #### Example(s)
  ![alt text](src/main/resources/Readme/HangmanCommand/playing.gif "Playing Hangman with a dictionary selected word")
 </details>
           <details>
  <summary>LOL Lookup Command</summary>
 
  ### Description
 Look up a player's League of Legends stats and build an image displaying them.
  
  [View code](src/main/java/Command/Commands/Lookup/LOLLookupCommand.java)

  ### Usage
  `[region] lollookup [name]` where `region` is the optional region code e.g `oce` (region is assumed to be oce), and `name` is the player's summoner name.  
  
  #### Example(s)
  ![alt text](src/main/resources/Readme/LOLLookupCommand/lookup.gif "Looking up player stats")
 </details>
</details>
