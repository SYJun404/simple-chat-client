package com.chatapp.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javazoom.jl.player.Player;

public class AudioUtil {

    public static void play(String resourcePath) {
        new Thread(() -> {
            try (
                InputStream is = AudioUtil.class.getResourceAsStream(
                    resourcePath
                );
                BufferedInputStream bis = new BufferedInputStream(is)
            ) {
                if (is == null) {
                    System.err.println(
                        "[AudioUtil] 文件不存在: " + resourcePath
                    );
                    return;
                }
                new Player(bis).play();
            } catch (Exception e) {
                System.err.println("[AudioUtil] 播放失败: " + e.getMessage());
            }
        }, "audio-player").start();
    }
}
