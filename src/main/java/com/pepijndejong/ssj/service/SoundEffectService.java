package com.pepijndejong.ssj.service;

import com.pepijndejong.ssj.service.exception.SoundEffectNotFoundException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class SoundEffectService {

    public void playSoundEffect(final String soundEffectFileName) {
        try {
            final InputStream fis = new FileInputStream("./sound_effects/" + soundEffectFileName + ".mp3");
            final Player playMP3 = new Player(fis);

            playMP3.play();
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new SoundEffectNotFoundException();
        }
    }

    public List<String> getSounds() {
        final File directory = new File("./sound_effects/");

        final File[] soundFiles = directory.listFiles(
                (dir, name) -> name.endsWith(".mp3"));

        final List<String> sounds = new ArrayList<>();
        for (File soundFile : soundFiles) {
            sounds.add(soundFile.getName().replaceFirst("\\.mp3", ""));
        }
        return sounds;
    }

}
