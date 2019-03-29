package com.pepijndejong.ssj.command;

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;

abstract public class AbstractCommand implements Command {

    protected static SlackPreparedMessage slackMessage(final String message) {
        return new SlackPreparedMessage.Builder()
                .withMessage(message)
                .build();
    }

    protected static SlackPreparedMessage slackMessageWithoutUnfurl(final String message) {
        return new SlackPreparedMessage.Builder()
                .withMessage(message)
                .withUnfurl(false)
                .build();
    }

}
