package com.nosorio.voiceChat.server;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class Server {

    ByteArrayOutputStream byteOutputStream;
    AudioFormat adFormat;
    TargetDataLine targetDataLine;
    AudioInputStream InputStream;
    SourceDataLine sourceLine;

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleInbits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
    }

    public void runVOIP() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(9876);
            byte[] receiveData = new byte[90000];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                try {
                    byte audioData[] = receivePacket.getData();//byteOutputStream.toByteArray();
                    InputStream byteInputStream = new ByteArrayInputStream(audioData);
                    AudioFormat adFormat = getAudioFormat();
                    InputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
                    sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    sourceLine.open(adFormat);
                    sourceLine.start();

                    Thread playThread = new Thread(new PlayThread());
                    playThread.start();

                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PlayThread extends Thread {
        byte tempBuffer[] = new byte[10000];
        public void run() {
            try {
                int cnt;
                while ((cnt = InputStream.read(tempBuffer, 0,tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        sourceLine.write(tempBuffer, 0, cnt);
                    }
                }
                sourceLine.drain();
                sourceLine.close();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }
}