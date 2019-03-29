package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.command.Command;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SlackServiceTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private AutoSoundService autoSoundService;

    @Mock
    private MessageCreator messageCreator;

    private SlackService slackService;

    @Before
    public void setup() {
        slackService = new SlackService(applicationContext, autoSoundService, messageCreator);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(messageCreator, autoSoundService);
    }

    @Test
    public void onMessage() throws Exception {
        final SlackMessagePosted slackMessagePosted = mock(SlackMessagePosted.class);
        final SlackSession slackSession = mock(SlackSession.class);

        final HashMap<String, Command> commandBeans = new HashMap<>();
        final Command command = mock(Command.class);
        when(command.command()).thenReturn("play ");
        commandBeans.put("play", command);
        when(applicationContext.getBeansOfType(Command.class)).thenReturn(commandBeans);
        final SlackUser slackUser = mock(SlackUser.class);
        final String username = "userX";
        when(slackUser.getUserName()).thenReturn(username);
        when(slackUser.getId()).thenReturn("id1");
        when(slackMessagePosted.getSender()).thenReturn(slackUser);
        when(slackMessagePosted.getMessageContent()).thenReturn("Play some song");
        SlackChannel slackChannel = mock(SlackChannel.class);
        when(slackMessagePosted.getChannel()).thenReturn(slackChannel);
        SlackPersona botSlackUser = mock(SlackPersona.class);
        when(slackSession.sessionPersona()).thenReturn(botSlackUser);
        when(botSlackUser.getId()).thenReturn("id2");
        when(slackChannel.getName()).thenReturn("some-channel");
        ReflectionTestUtils.setField(slackService, "channelName", "some-channel");

        slackService.onMessage(slackMessagePosted, slackSession);

        verify(command).execute(username, "some song");
        verify(autoSoundService).searchForAutoSounds("Play some song");
    }

    @Test
    public void onMessage_NoCommandData() throws Exception {
        final SlackMessagePosted slackMessagePosted = mock(SlackMessagePosted.class);
        final SlackSession slackSession = mock(SlackSession.class);

        final HashMap<String, Command> commandBeans = new HashMap<>();
        final Command command = mock(Command.class);
        when(command.command()).thenReturn("help");
        commandBeans.put("help", command);
        when(applicationContext.getBeansOfType(Command.class)).thenReturn(commandBeans);
        final SlackUser slackUser = mock(SlackUser.class);
        final String username = "userX";
        when(slackUser.getUserName()).thenReturn(username);
        when(slackMessagePosted.getSender()).thenReturn(slackUser);
        when(slackMessagePosted.getMessageContent()).thenReturn("help");
        when(slackUser.getId()).thenReturn("id1");
        SlackChannel slackChannel = mock(SlackChannel.class);
        when(slackMessagePosted.getChannel()).thenReturn(slackChannel);
        SlackPersona botSlackUser = mock(SlackPersona.class);
        when(slackSession.sessionPersona()).thenReturn(botSlackUser);
        when(botSlackUser.getId()).thenReturn("id2");
        when(slackChannel.getName()).thenReturn("some-channel");
        ReflectionTestUtils.setField(slackService, "channelName", "some-channel");

        slackService.onMessage(slackMessagePosted, slackSession);

        verify(command).execute(username, "");
        verify(autoSoundService).searchForAutoSounds("help");
    }

    @Test
    public void onMessage_IgnoreMyself() throws Exception {
        final SlackMessagePosted slackMessagePosted = mock(SlackMessagePosted.class);
        final SlackSession slackSession = mock(SlackSession.class);

        final SlackUser slackUser = mock(SlackUser.class);
        final String username = "botx";
        when(slackUser.getUserName()).thenReturn(username);
        when(slackUser.getId()).thenReturn("id1");
        when(slackMessagePosted.getSender()).thenReturn(slackUser);
        when(slackMessagePosted.getMessageContent()).thenReturn("Play some song");
        SlackChannel slackChannel = mock(SlackChannel.class);
        when(slackMessagePosted.getChannel()).thenReturn(slackChannel);
        SlackPersona botSlackUser = mock(SlackPersona.class);
        when(slackSession.sessionPersona()).thenReturn(botSlackUser);
        when(botSlackUser.getId()).thenReturn("id1");
        when(slackChannel.getName()).thenReturn("some-channel");
        ReflectionTestUtils.setField(slackService, "channelName", "some-channel");

        slackService.onMessage(slackMessagePosted, slackSession);

        //should not do anything
    }

    @Test
    public void onMessage_IgnoreWrongChannel() throws Exception {
        final SlackMessagePosted slackMessagePosted = mock(SlackMessagePosted.class);
        final SlackSession slackSession = mock(SlackSession.class);

        final HashMap<String, Command> commandBeans = new HashMap<>();
        final Command command = mock(Command.class);
        commandBeans.put("play", command);
        final SlackUser slackUser = mock(SlackUser.class);
        final String username = "botx";
        when(slackUser.getUserName()).thenReturn(username);
        when(slackUser.getId()).thenReturn("id1");
        when(slackMessagePosted.getSender()).thenReturn(slackUser);
        when(slackMessagePosted.getMessageContent()).thenReturn("Play some song");
        SlackChannel slackChannel = mock(SlackChannel.class);
        when(slackMessagePosted.getChannel()).thenReturn(slackChannel);
        SlackPersona botSlackUser = mock(SlackPersona.class);
        when(slackSession.sessionPersona()).thenReturn(botSlackUser);
        when(botSlackUser.getId()).thenReturn("id2");
        when(slackChannel.getName()).thenReturn("some-other-channel");
        ReflectionTestUtils.setField(slackService, "channelName", "some-channel");

        slackService.onMessage(slackMessagePosted, slackSession);

        verify(messageCreator).notSupportedChannel("some-channel");
    }

}
