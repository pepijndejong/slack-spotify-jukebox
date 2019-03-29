package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.MessageCreator;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends AbstractCommand {

    private final MessageCreator messageCreator;

    @Autowired
    public HelpCommand(final MessageCreator messageCreator) {
        this.messageCreator = messageCreator;
    }

    @Override
    public String command() {
        return "help$";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        final String message = messageCreator.help();

        return slackMessage(message);
    }
}
