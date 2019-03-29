package com.pepijndejong.ssj.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class RandomSentenceService {

    public String giveRandomSentence(final String fileName, final String... variables) {
        final InputStream cardsJsonStream = RandomSentenceService.class.getResourceAsStream("/sentences/" + fileName);
        final List<String> options = new BufferedReader(new InputStreamReader(cardsJsonStream))
                .lines().collect(Collectors.toList());

        int index = new Random().nextInt(options.size());
        return String.format(options.get(index), (Object[]) variables);
    }

}
