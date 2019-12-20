package com.nosorio.voiceChat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class Client extends JFrame {

	private static final long serialVersionUID = -9073116306823569263L;
	
	boolean stopaudioCapture = false;
    ByteArrayOutputStream byteOutputStream;
    AudioFormat adFormat;
    TargetDataLine targetDataLine;
    AudioInputStream InputStream;
    SourceDataLine sourceLine;

    public static void main(String args[]) {
    }

    public Client() {
        final JButton capture = new JButton("Capture");
        final JButton stop = new JButton("Stop");

        capture.setEnabled(true);
        stop.setEnabled(false);

        capture.addActionListener(
            new ActionListener() {
                public void actionPerformed(
                        ActionEvent e) {
                    capture.setEnabled(false);
                    stop.setEnabled(true);
                    captureAudio();
                }
            });
        getContentPane().add(capture);

        stop.addActionListener(
            new ActionListener() {
                public void actionPerformed(
                        ActionEvent e) {
                    capture.setEnabled(true);
                    stop.setEnabled(false);
                    stopaudioCapture = true;
                    targetDataLine.close();
                }
            });
        getContentPane().add(stop);
        getContentPane().setLayout(new FlowLayout());
        setTitle("Voice Chat");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 100);
        getContentPane().setBackground(Color.white);
        setVisible(true);
    }

    private void captureAudio() {
        try {
            adFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(adFormat);
            targetDataLine.start();

            Thread captureThread = new Thread(new CaptureThread());
            captureThread.start();
        } catch (Exception e) {
            StackTraceElement stackEle[] = e.getStackTrace();
            for (StackTraceElement val : stackEle) {
                System.out.println(val);
            }
            System.exit(0);
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleInbits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
    }

    class CaptureThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        public void run() {

            byteOutputStream = new ByteArrayOutputStream();
            stopaudioCapture = false;
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("192.168.0.142");

                while (!stopaudioCapture) {
                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    if (cnt > 0) {
                        DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, IPAddress, 9876);
                        clientSocket.send(sendPacket);
                        byteOutputStream.write(tempBuffer, 0, cnt);
                    }
                }
                byteOutputStream.close();

            } catch (Exception e) {
                System.out.println("CaptureThread::run()" + e);
                System.exit(0);
            }
        }
    }
}