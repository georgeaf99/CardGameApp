# CardGameApp
A card game application for Android.

The purpose of this application is emulate a deck of cards via Bluetooth, so that one can play any card game. Many applications exist for specific games, but this is the only one that allows the user to play any generic game.

The network architecture is a master-slave model, where one device (this host) is the server and all other devices the clients. When a client wants to edit the game state he sends a request to the server, the server validates the request against the model layer, and then pushes the updates back out to all the clients. The requests are encoded as serialized reflected methods and their parameters i.e. when the client wants to execute a method he passes a reflected version of the method and object parameters to the server (this method avoids passing unneccessary information over Bluetooth).

Known issues: I have not optimized the images for memory efficiency, thus the application crashes when too many cards are loaded; the GUI is bare bones, because I am not a front end designer; and sometimes state consistency issues between the server and client arise when actions are executed too rapidly.
