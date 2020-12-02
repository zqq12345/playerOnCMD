package cn.zqq;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.player.Player;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerMusic {

    public static void main(String[] args) {
        AtomicBoolean next = new AtomicBoolean(false);
        File files = new File("/Volumes/ext/music");
        File[] mp3s = files.listFiles(e -> e.getName().toLowerCase().endsWith("mp3")&&!e.getName().startsWith("._"));
        if (mp3s != null) {
            System.out.println("加载歌曲数量：" + mp3s.length);
        }else {
            System.exit(0);
        }
        AtomicInteger songTime = new AtomicInteger();
        new Thread(() -> {
            while (true) {
                int num = 0;
                if (mp3s != null) {
                    num = (int) (Math.random() * mp3s.length);
                    System.out.println("歌曲名#" + mp3s[num].getName());
                    System.out.println("歌曲大小#" + sizeFormat(mp3s[num].length())+"M");
                }
                FileInputStream stream = null;
                try {
                    if (mp3s != null) {
                        stream = new FileInputStream(mp3s[num]);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitstream bitstream = null;
                if (stream != null) {
                    bitstream = new Bitstream(stream);
                }
                int ms = 0;
                try {
                    if (bitstream != null) {
                        ms = Math.round((int) bitstream.readFrame().total_ms((int) mp3s[num].length()) / 1000);
                        songTime.set(ms);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("时长#" + minusFormat(ms));
                Player player = null;
                try {
                    if (stream != null) {
                        player = new Player(stream);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Player finalPlayer = player;
                FileInputStream finalStream = stream;
                CountDownLatch waitEnd = new CountDownLatch(1);
                new Thread(() -> {
                    int count = 0;
                    ConsoleProgressBar cpb = new ConsoleProgressBar(0, songTime.get(), 50, '.');
                    while (finalPlayer != null && !finalPlayer.isComplete()) {
                        count++;
                        //System.out.println("position:" + finalPlayer.getPosition());
                        if (next.get()) {
                            finalPlayer.close();
                            try {
                                finalStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            next.set(false);
                            System.out.println();
                            break;
                        }else{
                            cpb.show(count);
                        }
                        try {
                            Thread.sleep(900);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                try {
                    if (player != null) {
                        player.play();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (player != null && player.isComplete()) {
                    System.out.println("\n播放结束\n");
                }
            }
        }).start();
//        Scanner sc = new Scanner(System.in);
//        while (sc.hasNext()){
//            System.out.println("输入的是"+sc.next());
//        }

        while (true) {
            try {
                int read = System.in.read();
                //System.out.println("键盘录入:" + read);
                if(read == 10 || read == -1){
                    next.set(true);
                    if(read == -1){
                        System.out.println("\nbye");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String minusFormat(int time) {
        if (time <= 59) {
            return time+"s";
        }
        return time / 60 + "min:" + time % 60+"s";
    }
    private static String sizeFormat(long size){
        StringBuilder sb = new StringBuilder().append((float) size / 1024 / 1024);
       // System.out.println("sb = " + sb);
        int index = sb.indexOf(".");
        return sb.substring(0,index+3);

    }
}
