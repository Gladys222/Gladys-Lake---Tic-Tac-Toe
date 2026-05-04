package tictactoe;

import javax.sound.sampled.*;

public class MusicPlayer
{
    private static final int SR = 44100;

    public static void playWinMusic()
    {
        new Thread(() -> {
            try {
                chordStab(new int[]{262, 330, 392}, 160, 0.20f); Thread.sleep(50);
                chordStab(new int[]{330, 415, 494}, 160, 0.20f); Thread.sleep(50);
                chordStab(new int[]{392, 494, 587}, 200, 0.22f); Thread.sleep(90);
                int[] mel  = {262,294,330,349,392,440,494,523};
                int[] durs = { 75, 75, 75, 75, 75, 75, 75,300};
                for (int i = 0; i < mel.length; i++) { tone(mel[i], durs[i], 0.24f, 'T'); Thread.sleep(Math.max(4, durs[i]-14)); }
                Thread.sleep(70);
                chordStab(new int[]{262,330,392,523}, 480, 0.18f);
            } catch (Exception ignored) {}
        }).start();
    }

    public static void playDrawMusic()
    {
        new Thread(() -> {
            try {
                int[][] chords = {{220,262,330,392},{196,247,294,370},{175,220,262,330},{165,208,262,330}};
                int[]   durs   = {380, 360, 360, 560};
                for (int i = 0; i < chords.length; i++) { chordSoft(chords[i], durs[i], 0.15f); Thread.sleep(Math.max(8, durs[i]-28)); }
                int[] res  = {440,392,330,220}; int[] rdur = {200,170,170,480};
                for (int i = 0; i < res.length; i++) { tone(res[i], rdur[i], 0.19f, 'S'); Thread.sleep(Math.max(4, rdur[i]-18)); }
            } catch (Exception ignored) {}
        }).start();
    }

    public static void playClickSound()  { new Thread(() -> { try { pluck(700, 110, 0.20f); } catch (Exception ignored) {} }).start(); }
    public static void playHoverSound()  { new Thread(() -> { try { pluck(900,  60, 0.10f); } catch (Exception ignored) {} }).start(); }
    public static void playMenuClick()   { new Thread(() -> { try { pluck(500,  90, 0.18f); } catch (Exception ignored) {} }).start(); }
    public static void playErrorSound()
    {
        new Thread(() -> {
            try { tone(200, 120, 0.18f, 'Q'); Thread.sleep(80); tone(180, 120, 0.18f, 'Q'); }
            catch (Exception ignored) {}
        }).start();
    }

    private static void chordStab(int[] freqs, int ms, float vol) throws Exception { play(buildChord(freqs, ms, vol, true),  ms); }
    private static void chordSoft(int[] freqs, int ms, float vol) throws Exception { play(buildChord(freqs, ms, vol, false), ms); }

    private static byte[] buildChord(int[] freqs, int ms, float vol, boolean stab)
    {
        int n = SR * ms / 1000; byte[] buf = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            double t = (double)i/SR;
            double env = stab ? Math.min(1.0,i/120.0)*Math.exp(-3.2*t) : Math.min(1.0,i/380.0)*Math.min(1.0,(n-i)/550.0);
            double raw = 0; for (int f : freqs) raw += Math.sin(2*Math.PI*t*f)/freqs.length;
            short v = (short)(raw*32767*vol*env); buf[i*2]=(byte)(v&0xFF); buf[i*2+1]=(byte)((v>>8)&0xFF);
        }
        return buf;
    }

    private static void pluck(int hz, int ms, float vol) throws Exception
    {
        int n = SR*ms/1000; byte[] buf = new byte[n*2];
        for (int i = 0; i < n; i++) {
            double t=(double)i/SR; double env=Math.exp(-16*t);
            double raw=Math.sin(2*Math.PI*t*hz)+0.35*Math.sin(2*Math.PI*t*hz*2)+0.12*Math.sin(2*Math.PI*t*hz*3);
            short v=(short)(raw/1.47*32767*vol*env); buf[i*2]=(byte)(v&0xFF); buf[i*2+1]=(byte)((v>>8)&0xFF);
        }
        play(buf, ms);
    }

    private static void tone(int hz, int ms, float vol, char wave) throws Exception
    {
        int n=SR*ms/1000; byte[] buf=new byte[n*2];
        for (int i=0;i<n;i++) {
            double t=(double)i/SR; double env=Math.min(1.0,i/280.0)*Math.min(1.0,(n-i)/380.0);
            double raw; switch(wave) { case 'T': raw=2.0*Math.abs(2*((t*hz)%1.0)-1.0)-1.0; break; case 'Q': raw=Math.sin(2*Math.PI*t*hz)>=0?0.45:-0.45; break; default: raw=Math.sin(2*Math.PI*t*hz); }
            short v=(short)(raw*32767*vol*env); buf[i*2]=(byte)(v&0xFF); buf[i*2+1]=(byte)((v>>8)&0xFF);
        }
        play(buf, ms);
    }

    private static void play(byte[] buf, int ms) throws Exception
    {
        AudioFormat fmt=new AudioFormat(SR,16,1,true,false); DataLine.Info di=new DataLine.Info(SourceDataLine.class,fmt);
        if (!AudioSystem.isLineSupported(di)) return;
        SourceDataLine line=(SourceDataLine)AudioSystem.getLine(di); line.open(fmt,4096); line.start();
        line.write(buf,0,buf.length); line.drain(); line.close();
    }
}