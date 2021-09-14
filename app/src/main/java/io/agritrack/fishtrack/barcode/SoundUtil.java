package io.agritrack.fishtrack.barcode;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

import io.agritrack.fishtrack.R;

public class SoundUtil {

    public static SoundPool sp;
    public static Map<Integer, Integer> soundMap;
    public static Context context;

    public static void initSoundPool(Context context) {
        SoundUtil.context = context;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

        sp = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        soundMap = new HashMap<Integer, Integer>();
        soundMap.put(1, sp.load(context, R.raw.msg, 1));
        soundMap.put(2, sp.load(context, R.raw.beep, 1));
        soundMap.put(3, sp.load(context, R.raw.geiger2, 1));
        soundMap.put(4, sp.load(context, R.raw.geiger4, 1));
        soundMap.put(5, sp.load(context, R.raw.geiger6, 1));
    }

    //
    public static void play(int soundID, float volume, int loop, float rate) {
        AudioManager am = (AudioManager) SoundUtil.context.getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        float volumnRatio = audioCurrentVolume / audioMaxVolume;
        sp.play(soundMap.get(soundID),volume * volumnRatio,0,1, loop, rate);
    }
}
