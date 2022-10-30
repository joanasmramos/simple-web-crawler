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
        Crawler crawler = new Crawler("https://en.wikipedia.org/wiki/Timeline_of_the_far_future");

        Timer timer = new Timer();
        App app = new App(crawler, timer);
        // Stop the program after 10 seconds
        timer.schedule(app, 15000);
    }
}
