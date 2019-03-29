# slack-spotify-jukebox

Build with:
> ./gradlew clean build

### Step 1
Register as a developer at Spotify: https://developer.spotify.com/dashboard/applications

Fill in the spotify.clientId and spotify.clientSecret in the properties file:
build/bin/application.properties

### Step 2
Create a custom slack integration (bot): https://[YOUR_SLACK_DOMAIN].slack.com/apps/manage/custom-integrations

Fill in the slack.authToken in the properties file:
build/bin/application.properties

### Step 3
Create a slack channel and add your newly created bot to it.

Fill in the slack.channel in the properties file:
build/bin/application.properties

### Step 4
Create a directory build/bin/sound_effects and fill it with your favorite sound effects. Only mp3 supported!

### Step 4b (optional)
You can also automatically play sound effects on certain messages in channels the bot has joined. Take a look at the 'Auto sounds' section in the application.properties file if you want this.

### Step 5
Start the application:
> java -jar slack-spotify-jukebox-0.0.1-SNAPSHOT.jar

If you run the jukebox for the first time, you'll get the following message:
> No Spotify token found... Create one here: [SOME URL]

Follow the link and login with your Spotify account.

Once this is done, you will be redirected to a page that should say: 
> 'Great success, you're good to go! ....'

Now make sure your Spotify app is open, playing (anything) and cross-fading is disabled (which is the default).

That's it! Go to your slack-channel and type:
> help
