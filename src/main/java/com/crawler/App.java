package com.crawler;

import java.util.Timer;
import java.util.TimerTask;

public class App extends TimerTask {
    private Crawler crawler;
    private Timer timer;

    public App(Crawler crawler, Timer timer) {
        this.crawler = crawler;
        this.timer = timer;
    }

    @Override
    public void run() {
        System.out.println("INTERRUPTING PROGRAM");
        crawler.stopExecution();
        timer.cancel();
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        String seed = args[0];
        int maxNumberOfLevels = Integer.parseInt(args[1]);
        int maxTimeInSeconds = Integer.parseInt(args[2]);

        // seed = "https://en.wikipedia.org/wiki/Open-source_intelligence";
        // maxNumberOfLeves = 5;
        // maxTimeInSeconds = 300;
        Crawler crawler = new Crawler(seed, maxNumberOfLevels);

        Timer timer = new Timer();
        App app = new App(crawler, timer);
        // Stop the program after a certain time
        timer.schedule(app, maxTimeInSeconds * 1000);
    }
}
