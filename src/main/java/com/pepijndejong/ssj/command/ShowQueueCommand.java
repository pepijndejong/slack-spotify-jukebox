package com.pepijndejong.ssj.command;

import com.pepijndejong.ssj.service.PlayerStateService;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowQueueCommand extends AbstractCommand {

    private final PlayerStateService playerStateService;

    @Autowired
    public ShowQueueCommand(final PlayerStateService playerStateService) {
        this.playerStateService = playerStateService;
    }

    @Override
    public String command() {
        return "show queue$";
    }

    @Override
    public SlackPreparedMessage execute(final String username, final String commandData) {
        final String printableQueue = playerStateService.getPrintableQueue();
        return slackMessageWithoutUnfurl(printableQueue);
    }
}
