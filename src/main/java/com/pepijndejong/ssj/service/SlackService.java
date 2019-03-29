package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.command.Command;
import com.pepijndejong.ssj.config.AutoSoundConfig;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SlackService {

    private final ApplicationContext applicationContext;

    private final AutoSoundService autoSoundService;

    private final MessageCreator messageCreator;

    private SlackSession session;

    @Value("${slack.channel}")
    private String channelName;

    @Value("${slack.authToken}")
    private String authToken;

    @Autowired
    public SlackService(final ApplicationContext applicationContext, final AutoSoundService autoSoundService, final MessageCreator messageCreator) {
        this.applicationContext = applicationContext;
        this.autoSoundService = autoSoundService;
        this.messageCreator = messageCreator;
    }

    @PostConstruct
    public void initialize() {
        session = SlackSessionFactory.createWebSocketSlackSession(authToken);

        final SlackMessagePostedListener messagePostedListener = (event, session1) -> {
            onMessage(event, session);
        };
        session.addMessagePostedListener(messagePostedListener);

        if (channelName.isEmpty()) {
            log.error("Slack channel not set. Add it in the application.properties file.");
        }

        try {
            session.connect();
        } catch (IOException e) {
            log.error("Failed to connect to slack. Did you set the slack.authToken in the application.properties file?", e);
        }
    }

    protected void onMessage(final SlackMessagePosted event, final SlackSession session) {
        try {
            processMessage(event, session);
        } catch (RuntimeException e) {
            log.error("Error on message {}.", event.getMessageContent(), e);
        }
    }

    private void processMessage(final SlackMessagePosted event, final SlackSession session) {
        final SlackChannel channelOnWhichMessageWasPosted = event.getChannel();
        final String messageContent = event.getMessageContent().trim();
        final SlackUser messageSender = event.getSender();
        final String username = messageSender.getUserName();
        final boolean messageIsFromMyself = messageSender.getId().equals(session.sessionPersona().getId());
        final boolean messageIsNotOnConfiguredChannel = !channelName.equals(channelOnWhichMessageWasPosted.getName());

        if (messageIsFromMyself) {
            log.debug("Ignoring message from myself.");
        } else if (messageIsNotOnConfiguredChannel) {
            session.sendMessage(channelOnWhichMessageWasPosted,
                    messageCreator.notSupportedChannel(channelName));
        } else {
            processMessage(session, channelOnWhichMessageWasPosted, messageContent, username);
        }
    }

    private void processMessage(final SlackSession session, final SlackChannel channelOnWhichMessageWasPosted, final String messageContent, final String username) {
        final Map<String, Command> commandClasses = applicationContext.getBeansOfType(Command.class);
        for (Command commandClass : commandClasses.values()) {
            final String commandRegex = String.format("^(?<command>%s)(?<commandData>.*)?", commandClass.command());

            final Pattern pattern = Pattern.compile(commandRegex, Pattern.CASE_INSENSITIVE);
            final Matcher matcher = pattern.matcher(messageContent);
            if (matcher.find()) {
                final String commandData = matcher.group("commandData");
                final String strippedCommandData = stripTagsAddedBySlack(commandData);

                log.info("Command found from {}: {}", username, messageContent);

                try {
                    final SlackPreparedMessage slackMessage = commandClass.execute(username, strippedCommandData);
                    if (slackMessage != null) {
                        session.sendMessage(channelOnWhichMessageWasPosted, slackMessage);
                    }
                } catch (RuntimeException e) {
                    log.error("Error in command {}", commandClass.getClass(), e);
                }
            }
        }

        final AutoSoundConfig.AutoSound autoSound = autoSoundService.searchForAutoSounds(messageContent);
        if (autoSound != null) {
            final SlackPreparedMessage message = new SlackPreparedMessage.Builder()
                    .withMessage(autoSound.getMessage()).build();
            sendMessage(message);
            autoSoundService.triggerAutoSound(autoSound.getSoundeffect());
        }
    }

    private String stripTagsAddedBySlack(final String commandData) {
        if (commandData == null) {
            return null;
        }
        return commandData.replaceAll("\\<|\\>", "");
    }

    public void sendMessage(final SlackPreparedMessage slackMessage) {
        log.info("TO_SLACK:: {}", slackMessage.getMessage());
        session.sendMessage(session.findChannelByName(channelName), slackMessage);
    }

}
