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
  
  `dd select [#]` to select your case
  
  `dd open [#, #, #...]` to open cases

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
 Uses the [Riot Gamees API](https://developer.riotgames.com/apis) to look up a player's League of Legends stats and build an image displaying them.
  
  [View code](src/main/java/Command/Commands/Lookup/LOLLookupCommand.java)

  ### Usage
  `[region] lollookup [name]` where `region` is the optional region code e.g `oce` (region is assumed to be oce), and `name` is the player's summoner name.  
  
  #### Example(s)
  ![alt text](src/main/resources/Readme/LOLLookupCommand/lookup.gif "Looking up player stats")
 </details>
           <details>
  <summary>Execute Order 66 Command</summary>
 
  ### Description
This command uses a role named `target`.
             
This role is applied to all members when they join and may be manually added/removed as with any role.
             
Typing `kill list` will send a private message to the user displaying the list of members who are targeted.
             
By typing `execute order 66`, the bot will join the voice channel and play a random audio clip before kicking all members on the list. (Members who the bot cannot kick will be removed from the list prior to executing).
             
Kicked members will be sent a message with an invite back to the server.
  
  [View code](src/main/java/Command/Commands/ExecuteOrder/ExecuteOrder66Command.java)

  ### Usage
`kill list` to view targeted members.
             
`execute order 66` to kick the targeted members.
  
  #### Example(s)

  https://user-images.githubusercontent.com/42688609/122846622-5c0ff880-d35a-11eb-9420-842282f0c7d4.mp4
             
  ![alt text](src/main/resources/Readme/ExecuteOrder66Command/apology.png "Apology to the kicked member")

 </details>
             <details>
  <summary>Millionaire Command</summary>
 
  ### Description
This command allows the user to play a game of Who Wants to Be a Millionaire.
Buttons are used to answer questions/use the lifeline and winnings are saved in to a bank which is ranked on a leaderboard.
               
The questions are retrieved from the [Open Trivia DB](https://opentdb.com/) and are of random difficulty/topics.
  
  [View code](src/main/java/Command/Commands/MillionaireCommand.java)

  ### Usage
`millionaire start` to start the game.
               
`millionaire forfeit` to forfeit the game (keeping the winnings up until that point).
               
`millionaire bank` to view your bank.
               
`millionaire bank [@someone]` to view another member's bank (where `@someone` is the @mention of another member).
               
`millionaire leaderboard` to view the bank leaderboard
  
  #### Example(s)
             
  ![alt text](src/main/resources/Readme/MillionaireCommand/playing.gif "Playing Who Wants to Be a Millionaire")
  ![alt text](src/main/resources/Readme/MillionaireCommand/bank.gif "Viewing bank")
  ![alt text](src/main/resources/Readme/MillionaireCommand/leaderboard.gif "Viewing the leaderboard")

 </details>
           <details>
  <summary>MW Lookup Command</summary>
 
  ### Description

 Uses the [COD API](https://github.com/Lierrmm/Node-CallOfDuty) to look up a player's Modern Warfare stats and build an image displaying them. This image displays the player's best (most used) weapons, streaks, equipment, field upgrade, and commendations, alongside their Win/Loss and Kill/Death ratios.
  
  An optional query may be provided to display only a specific stat e.g a specific weapon or streak.
             
  [View code](src/main/java/Command/Commands/Lookup/MWStatsCommand.java)

  ### Usage
  `[platform] mwlookup [name]` where `platform` is one of `XBOX, XBL, PSN, BATTLE` (optional & assumed to be Battle.net), and `name` is the player name to search.
  
  Alternatively, `[platform] mwlookup [name] +[query]` may be provided, where `platform` and `name` are as above, and `query` is the name of a stat to display e.g `+bradley` to display only the player's Infantry Assault Vehicle stats.
             
  #### Example(s)
  ![alt text](src/main/resources/Readme/MWLookupCommand/full_lookup.gif "Looking up player's full stats")
  ![alt text](src/main/resources/Readme/MWLookupCommand/specific_lookup.gif "Looking up a specific stat for the player")
          <details>
          <summary>Player stats image</summary>
              ![alt text](src/main/resources/Readme/MWLookupCommand/stats.png "Player stats image")
         </details>
          <details>
          <summary>Specific stats image</summary>
              ![alt text](src/main/resources/Readme/MWLookupCommand/specific.png "Specific stats image")
         </details>
 </details>
             <details>
  <summary>MW Random Command</summary>
 
  ### Description
Generate a random Modern Warfare loadout and build an image to display it. Assign a random name to the loadout.
               
The generated loadout abides by all in-game restrictions e.g 2 primary weapons if the `overkill` perk is rolled. This also applies to the weapon attachments, where if a rolled attachment blocks another attachment category e.g a `barrel` attachment blocking the `muzzle` category, no `muzzle` category attachments will be added.
               
A short [comment](src/main/java/COD/LoadoutAnalysis.java) about the loadout is generated and appended to the message.
               
  Possible weapons & attachments can be seen [here](src/main/resources/COD/MW/Data/weapons.json).
                              
  [View code](src/main/java/Command/Commands/COD/MWRandomCommand.java)

  ### Usage
  `mwrandom`
             
  #### Example(s)
  ![alt text](src/main/resources/Readme/MWRandomCommand/loadouts.gif "Generating loadouts")
          <details>
          <summary>Example loadout</summary>
              ![alt text](src/main/resources/Readme/MWRandomCommand/example.png "Example loadout")
         </details>
 </details>
     <details>
  <summary>OSRS Lending Command</summary>
 
  ### Description
  Track OSRS item loans between members. Loans are accepted/declined via buttons and are displayed as a dynamically built bank image.
  
  [View code](src/main/java/Command/Commands/OSRSLendingCommand.java)

  ### Usage
 `osrslend [@loanee] [items]` where `@loanee` is the @mention of the member to loan items to, and `items` is a comma separated list of items with optional quantities e.g  `twisted bow, 500GP, monkfish x5`
       
 `osrslend forgive [loan id]` where `loan id` is the id of a loan to delete (must be the `loaner` of the items)
 
 `osrslend loans` to view a list of your incoming/outgoing loans
       
 `osrslend loans [loan id]` to view the details of a specific loan with the given `loan id`

  #### Example(s)
  ![alt text](src/main/resources/Readme/OSRSLendingCommand/creating_loan.gif "Creating a loan")
  ![alt text](src/main/resources/Readme/OSRSLendingCommand/viewing_loan.gif "Viewing loans")
  </details>
       <details>
  <summary>OSRS Lookup Command</summary>
 
  ### Description
 Uses the [OSRS Hiscores CSV API](https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=big+mike) and [Wise Old Man API](https://wiseoldman.net/docs) to look up a player's OSRS stats and build an image displaying them. This image displays the player's skills, bosses, clue scrolls, achievements, account type, and optional XP tracker.
  
  [View code](src/main/java/Command/Commands/Lookup/OSRSLookupCommand.java)

  ### Usage
  `[optional args] osrslookup [name]` where `name` is the name of the player to look up, and `optional args` is any combination of `xp`, `virtual`, and `league`.
  
  `xp` uses the [Wise Old Man API](https://wiseoldman.net/docs) to get the player's weekly XP gained and appends it to the image. If the player is not currently tracked on Wise Old Man, a request is made to begin tracking them for next time.
         
  `virtual` displays the player's virtual levels instead of their actual levels.
         
  `league` displays the player's Trailblazer League stats instead of their normal stats. Will display map/relic unlocks if exported from [osleague.tools](https://www.osleague.tools/) and uploaded using the [TrailBlazer Command](/src/main/java/Command/Commands/Runescape/TrailblazerCommand.java).
         
  The image is built in individual sections, allowing the exclusion/inclusion of sections dynamically, e.g if a player has no clue scroll completions, the clue scroll section is not added to the image.

  #### Example(s)
  ![alt text](src/main/resources/Readme/OSRSLookupCommand/lookup.gif "Looking up player stats")
          <details>
          <summary>Normal stats image</summary>
              ![alt text](src/main/resources/Readme/OSRSLookupCommand/standard.png "Standard image")
         </details>
         <details>
         <summary>League stats image</summary>
              ![alt text](src/main/resources/Readme/OSRSLookupCommand/league.png "League image")
         </details>
         <details>
           <summary>Virtual stats w/ XP tracker image</summary>       
              ![alt text](src/main/resources/Readme/OSRSLookupCommand/xp_virtual.png "Virtual stats w/ XP tracker")         
         </details>
  </details>
</details>
