package com.crawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public final class Crawler {
    private volatile HashSet<URL> toVisit = new HashSet<URL>();
    private HashSet<URL> visited = new HashSet<URL>();

    private int currentLevel = 0;
    private int MAX_LEVELS = 5;

    private ArrayList<String> stopWords = new ArrayList<String>();

    public Crawler(String seed) {
        try {
            this.loadStopWordsList();

            URL seedURL = new URL(seed);

            ExecutorService executor = Executors.newFixedThreadPool(5);
            Runnable worker = new Scraper(seedURL, this.stopWords, (url, children, words) -> {
                getReportFromCrawledURL(url, children, words);
            });
            executor.execute(worker);

            while (!executor.isShutdown()) {

            }

            System.out.println("Finished all threads!\n");

        } catch (MalformedURLException e) {
        }
    }

    private void getReportFromCrawledURL(URL crawledURL, HashSet<URL> childrenURLs,
            ArrayList<String> mostImportantWords) {

        FileWriter fw = getFileWriter("./logs/log.txt", true);

        try {
            String line = "Crawled URL <" + crawledURL.toExternalForm() + ">, found " + childrenURLs.size()
                    + " new URLs to crawl and the most important words were [" + String.join(",", mostImportantWords)
                    + "].\n";
            fw.write(line);
            fw.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void loadStopWordsList() {
        BufferedReader br = this.getBufferedReader("./res/stop_words.txt");

        String word;
        try {
            word = br.readLine();
            while (word != null) {
                this.stopWords.add(word);
                word = br.readLine();
            }
        } catch (IOException e) {
        }
    }

    private FileWriter getFileWriter(String filepath, boolean appendMode) {
        File file = new File(filepath);
        try {
            return new FileWriter(file, appendMode);
        } catch (IOException e) {
            return null;
        }
    }

    private BufferedReader getBufferedReader(String filepath) {
        File file = new File(filepath);
        try {
            FileReader fr = new FileReader(file);
            return new BufferedReader(fr);
        } catch (IOException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Crawler myCrawler = new Crawler("https://en.wikipedia.org/wiki/Timeline_of_the_far_future");
        System.out.println("I AM DONE");
    }
}
