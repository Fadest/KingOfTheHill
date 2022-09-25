# King of the Hill

This is a trial project made for [AkumaMC](https://www.akumamc.net/), the guidelines for this project are
located in [this link](https://docs.google.com/document/d/1chJ7oqXzj7a-XgDcMKhHIwdJoAbhOcS-gEeLmDBfao0/edit).

## Commands

- /koth list » Shows the scheduled games
- /koth info [name] » Show the information of a game
- /koth schedule [name] [time] (-add) » Schedule a game
- /koth cancel [name] » Cancels the schedule of a yet to start game
- /koth stop [name] » Stops a started game
- /koth create [name] » Creates a new game
- /koth delete [name] » Deletes a new game
- /koth edit [name] [world/rewards/min_rewards/max_rewards/capture_zone/global_zone/duration] » Edit the specified game

## Information

- This project was made using [Spigot](https://spigotmc.org/) 1.8.9 and tested using the latest stable version.
- This project requires a local build of [Spigot](https://spigotmc.org/), which you can get
  using [BuildTools](https://hub.spigotmc.org/jenkins/job/BuildTools/), a guide to do that
  is [here](https://www.spigotmc.org/wiki/buildtools/) (*1.8 is the only supported version*).
- No libraries or external plugins are required in this plugin.
- Configuration files are loaded using the JSON format since it's the most efficient way to store multiple games in
  different folders and files
- The plugin was made for [AkumaMC](https://www.akumamc.net/), and is not intended for use on other servers
  since there is no support for it.
- The plugin is licensed under the [MIT License](https://choosealicense.com/licenses/mit/).