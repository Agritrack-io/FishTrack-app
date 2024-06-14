package io.agritrack.philosofish.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

import io.agritrack.philosofish.R;

public class SoundUtil {
    public static final int Msg = 1, Beep = 2, Geiger2 = 3, Geiger4 = 4, Geiger6 = 5;
    public static SoundPool soundPool;
    public static Map<Integer, Integer> soundMap;
    public static Context context;

    public static void initSoundPool(Context context) {
        SoundUtil.context = context;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        soundMap = new HashMap<>();
        soundMap.put(Msg, soundPool.load(context, R.raw.msg, 1));
        soundMap.put(Beep, soundPool.load(context, R.raw.beep, 1));
        soundMap.put(Geiger2, soundPool.load(context, R.raw.geiger2, 1));
        soundMap.put(Geiger4, soundPool.load(context, R.raw.geiger4, 1));
        soundMap.put(Geiger6, soundPool.load(context, R.raw.geiger6, 1));
    }

    //
    public static void play(int soundID, int loop, float rate) {
        if (SoundUtil.context == null || soundPool == null)
            return;

        AudioManager am = (AudioManager) SoundUtil.context.getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        soundPool.play(soundMap.get(soundID), audioMaxVolume /*volume * volumnRatio*/, audioMaxVolume, 1, loop, rate);
    }

    public static void pause() {
        soundPool.pause(0);
    }
}
