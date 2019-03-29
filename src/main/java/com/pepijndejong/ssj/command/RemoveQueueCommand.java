package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.MessageCreator;
import com.pepijndejong.ssj.service.PlayerStateService;
import com.pepijndejong.ssj.service.exception.NotOwnerOfQueueNumberException;
import com.pepijndejong.ssj.service.exception.UnknownQueueNumberException;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemoveQueueCommand extends AbstractCommand {

    private final PlayerStateService playerStateService;

    private final MessageCreator messageCreator;

    @Autowired
    public RemoveQueueCommand(final PlayerStateService playerStateService, final MessageCreator messageCreator) {
        this.playerStateService = playerStateService;
        this.messageCreator = messageCreator;
    }

    @Override
    public String command() {
        return "remove queue ";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        try {
            final Integer queueNumber = Integer.valueOf(commandData);
            playerStateService.removeFromQueue(queueNumber, username);
        } catch (NotOwnerOfQueueNumberException e) {
            return slackMessage(messageCreator.removeQueueNotOwner(username));
        } catch (NumberFormatException | UnknownQueueNumberException e) {
            return slackMessage(messageCreator.removeQueueUnknownNumber(username, commandData));
        }

        final String printableQueue = playerStateService.getPrintableQueue();
        final String message = messageCreator.removeQueue(username, printableQueue);
        return slackMessageWithoutUnfurl(message);
    }
}
