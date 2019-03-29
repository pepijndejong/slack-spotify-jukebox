package com.pepijndejong.ssj.command;

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;

public interface Command {

    String command();

    SlackPreparedMessage execute(String username, String commandData);

}
