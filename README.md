## Introduction

This was a four person group project for our introduction to software engineering
course. My specific part was to make the AI player. This was implemented
with a Java backend, Spark framework, and JavaScript, HTML/CSS front end.

## Obelisk

The goal of this project is to create a digital version of
the board game Obelisk (manual can be found at
https://www.thegamecrafter.com/games/obelisk2
under ‘Downloads’). One of our group members knows the creator of the game,
so we would actually be able to get his feedback and potentially some art
assets for the GUI.

### Game Overview
The board is a grid of “map tiles” with arrows on them.
It is a cooperative game with two alternating phases, “day” and “night.”
During the day players take turns performing actions, to set up defenses
and/or rotate map tiles. At night, enemies come from a portal and follow
the arrows on the map tiles. The game is won if the players successfully
capture all the enemies, and it is lost if the enemies either follow the
arrows off the map or walk on the same tile twice.

### Requirements
#### Backend for basic game
The rules need to be implemented, and there needs to be a way to keep track
of the player, the enemies, the board tiles, and other details of the game.
This part of the project will likely have the most code.

We anticipate the most difficult aspect of the section to be the design of the
software, because there are a lot of interacting components and details to keep
track of.
#### Frontend
There needs to be an interactive board that allows the player to rotate
tiles and display enemies moving around the board, as well as other details,
including starting the game and selecting a difficulty. There could also be
a chat functionality if the game is being played cooperatively.

We anticipate the most difficult part of this section to be making a UI that
looks good and is responsive. This will be a more involved UI than any of us
have worked on previously.
#### Multiplayer Capability
The collaborative aspect could be expanded to multiple devices, where each
player is on one device and takes a turn to perform their actions.
All players can watch the other players’ actions during the day and then
watch the enemies move during the night. Potentially, multiple groups will
be able to play distinct games at the same time.

None of us have done anything like this before, so we will have to figure out
everything that is involved with multiple clients connecting to the same source
and modifying data, then displaying those changes to all clients.
#### Alternate rules/game expansion
The manual includes alternate rule sets as well as other pieces that add
challenge/complexity to the game. There would be a setting to enable some
of these features when starting a game. We think it would be best to add
this functionality after completing all of the other aspects of the game,
because it is most important that the basic game works.

We think that the most difficult part of this section will be integrating
brand new features into the rest of the codebase. We will have to design
the base game with extensibility in mind.
