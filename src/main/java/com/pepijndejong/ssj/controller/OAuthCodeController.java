package com.pepijndejong.ssj.controller;

import com.pepijndejong.ssj.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OAuthCodeController {

    private final SpotifyService spotifyService;

    @Autowired
    public OAuthCodeController(final SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/callback")
    @ResponseBody
    public String callback(@RequestParam final String code, @RequestParam final String state) {
        spotifyService.createAccessToken(state, code);
        return "Great success, you're good to go! Music should start playing now, make sure your Spotify app is running!";
    }

}
