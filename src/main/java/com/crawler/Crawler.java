package com.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
    private volatile HashMap<Integer, LinkedList<URL>> toVisit = new HashMap<Integer, LinkedList<URL>>();
    private HashSet<URL> visited = new HashSet<URL>();

    private int MAX_LEVELS = 5;

    private ArrayList<String> stopWords = new ArrayList<String>();

    private FileWriter logFileWriter;
    private FileWriter CSVFileWriter;

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public Crawler(String seed) {
        this.loadStopWordsList();
        this.logFileWriter = getFileWriter("./logs/log.txt", true);
        this.CSVFileWriter = getFileWriter("./logs/log.csv", true);
        try {
            URL seedURL = new URL(seed);
            Runnable worker = new Scraper(seedURL, this.stopWords, (url, children, words) -> {
                getReportFromCrawledURL(url, children, words, 0);
            });

            this.executor.execute(worker);

        } catch (MalformedURLException e) {
        }
    }

    private void getReportFromCrawledURL(URL crawledURL, HashSet<URL> childrenURLs,
            ArrayList<String> mostImportantWords, int urlLevel) {

        this.visited.add(crawledURL);

        int nextLevel = urlLevel + 1;
        LinkedList<URL> nextLevelUrls = this.toVisit.get(nextLevel);

        if (nextLevelUrls == null) {
            nextLevelUrls = new LinkedList<URL>();
        }

        nextLevelUrls.addAll(childrenURLs);
        this.toVisit.put(nextLevel, nextLevelUrls);

        if (urlLevel == 0) {
            this.crawl();
        }

        try {
            String line = "Crawled URL <" + crawledURL.toExternalForm() + ">, found " + childrenURLs.size()
                    + " new URLs to crawl and the most important words were [" + String.join(",", mostImportantWords)
                    + "].\n";
            logFileWriter.write(line);
            logFileWriter.flush();

            String csvLine = crawledURL.toExternalForm() + "," + urlLevel + "," + childrenURLs.size() + ","
                    + String.join(";", mostImportantWords) + "\n";
            CSVFileWriter.write(csvLine);
            CSVFileWriter.flush();
            System.out.println("Visited URL " + crawledURL);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

    }

    private void crawl() {
        for (int i = 1; i <= MAX_LEVELS; i++) {
            final int level = i;
            LinkedList<URL> urls = this.toVisit.get(level);
            while (urls.peek() != null) {
                URL nextUrl = urls.remove();
                if (this.visited.contains(nextUrl)) {
                    continue;
                }
                this.executor.submit(new Scraper(nextUrl, this.stopWords, (url, children, words) -> {
                    getReportFromCrawledURL(url, children, words, level);
                }));
            }
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
