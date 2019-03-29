# slack-spotify-jukebox

Build with:
> ./gradlew clean build

Go to the build/bin directory, there add your properties:
```
## Port config, should match the spotify.redirectUrl
server.port=8081
## Spotify config
## Register here: https://developer.spotify.com/dashboard/applications
## to get your clientId and clientSecret
spotify.clientId=
spotify.clientSecret=
spotify.redirectUrl=http://localhost:8080/callback/
spotify.defaultPlaylistUri=spotify:user:opperpep:playlist:3UyRgqZNW5rR9WvJ3ZlJbD
## Slack config
## Create a bot here: https://[YOUR_SLACK_DOMAIN].slack.com/apps/manage/custom-integrations
## and paste the api-token here
slack.authToken=
## Make sure the channel exists and invite the bot to it
slack.channel=
## Your jukebox name. Pick any! Will be used in some slack-messages.
jukebox-name=Slack jukebox
```

Note: Optional!
Add your sound effects (if you have any) to the directory build/bin/sound_effects Only mp3 is supported!
You can also automatically play sound effects on certain messages in channels the bot has joined. Take a look at the example-properties if you want this.

Then run with:
> java -jar slack-spotify-jukebox-0.0.1-SNAPSHOT.jar

If you run the jukebox for the first time, you'll get the following message:
> No Spotify token found... Create one here: [SOME URL]

Follow the link and login with your Spotify account.

Once this is done, you will be redirected to a page that should say: 
> 'Great success, you're good to go!'

Now make sure your Spotify app is open, playing (anything) and cross-fading is disabled (which is the default).

That's it! Go to your slack-channel and type:
> help
