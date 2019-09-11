# Zork Reader
**Zork Reader** is a simple android application offering a GUI to interact with my [Zork API](https://github.com/jphomick/zorkapi). The API address is hardcoded.

The [**ReadHelper.java**](https://github.com/jphomick/ZorkReaderAndroid/blob/master/app/src/main/java/com/example/zorkreader/ReadHelper.java) class contains two short static methods for reading the result of the API query; [**MainActivity.java**](https://github.com/jphomick/ZorkReaderAndroid/blob/master/app/src/main/java/com/example/zorkreader/MainActivity.java) contains everything else, minus the [resources](https://github.com/jphomick/ZorkReaderAndroid/tree/master/app/src/main/res) and [manifest](https://github.com/jphomick/ZorkReaderAndroid/blob/master/app/src/main/AndroidManifest.xml).

The entire application takes place in one activity, with two layouts: one to get a session id and another hosting the actual GUI.

## The GUI
The GUI for the Zork Reader has three simple parts: the **text history**, the **command submit**, and the **command builder**.

## Text History
The text history will display the responses from the API

## Command Submit
Here you can type in commands and press the **GO!** button to communicate with the API. While the the app is waiting for a response from the API, the button will be replaced with a progress bar.

## Command Builder
This exciting feature allows you to send commands without typing! Zork Reader will analyze the responses from the Zork API and automatically create buttons for the objects you encounter during your Zork adventure. Each button will add its text to the command submit. The buttons display with a real-time filter. 

Using the command builder, it is possible to play an entire game of Zork without typing in a single word!

For more information on playing Zork through the Zork API, check the [Zork API](https://github.com/jphomick/zorkapi) readme.
