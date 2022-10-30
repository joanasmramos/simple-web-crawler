package com.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Main thread.
 * Handles the visited/to visit URLs by launching scrapers and assessing their
 * results.
 */
public final class Crawler {
    private URL seedUrl;
    private HashMap<Integer, LinkedList<URL>> toVisit = new HashMap<Integer, LinkedList<URL>>();
    private HashSet<URL> visited = new HashSet<URL>();

    private int maxNumberOfLevels;
    private HashMap<Integer, Integer> urlsPerLevel = new HashMap<Integer, Integer>();

    private ArrayList<String> stopWords = new ArrayList<String>();

    private FileWriter logFileWriter;
    private FileWriter csvFileWriter;

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    private boolean timeIsUp = false;

    public Crawler(String seed, int maxNumberOfLevels) {
        this.loadStopWordsList();
        this.logFileWriter = FileUtils.getFileWriter("./logs/log.txt", false);
        this.csvFileWriter = FileUtils.getFileWriter("./logs/log.csv", false);
        this.maxNumberOfLevels = maxNumberOfLevels;
        try {
            this.seedUrl = new URL(seed);
            Runnable scraper = new Scraper(this.seedUrl, this.stopWords, (url, children, words) -> {
                getReportFromcrawledUrl(url, children, words, 0);
            });

            this.executor.execute(scraper);
        } catch (MalformedURLException e) {
        }
    }

    /**
     * Handle results from the scraper for a certain URL.
     * 
     * @param crawledUrl         - URL.
     * @param childrenURLs       - Hyperlinks found on this page.
     * @param mostImportantWords - Most important words on this page.
     * @param urlLevel           - Depth of the URL in the crawling tree.
     */
    private void getReportFromcrawledUrl(URL crawledUrl, HashSet<URL> childrenURLs,
            ArrayList<String> mostImportantWords, int urlLevel) {

        this.visited.add(crawledUrl);

        int nextLevel = urlLevel + 1;
        LinkedList<URL> nextLevelUrls = this.toVisit.get(nextLevel);

        if (nextLevelUrls == null) {
            nextLevelUrls = new LinkedList<URL>();
        }

        nextLevelUrls.addAll(childrenURLs);
        this.toVisit.put(nextLevel, nextLevelUrls);

        // Write to log files
        try {
            String line = "Crawled URL <" + crawledUrl.toExternalForm() + ">, found " + childrenURLs.size()
                    + " new URLs to crawl and the most important words were [" + String.join(",", mostImportantWords)
                    + "].\n";
            logFileWriter.write(line);
            logFileWriter.flush();

            if (urlLevel == 0) {
                String headerLine = "URL,URL level,Children URLs size,Most important words\n";
            }

            String csvLine = crawledUrl.toExternalForm() + "," + urlLevel + "," + childrenURLs.size() + ","
                    + String.join(";", mostImportantWords) + "\n";
            csvFileWriter.write(csvLine);
            csvFileWriter.flush();
            System.out.println("[INFO] Visited URL " + crawledUrl);
        } catch (IOException e) {
            System.out.println("[ERROR] Error writing to log files:");
            e.printStackTrace();
        }

        // Launch crawler
        if (urlLevel == 0) {
            this.crawl();
        }
    }

    /**
     * Crawls hyperlinks in a breadth-first approach (crawling all hyperlinks on the
     * same level before moving on to the next level).
     */
    private void crawl() {
        for (int i = 1; i <= maxNumberOfLevels; i++) {
            final int level = i;
            LinkedList<URL> urls = this.toVisit.get(level);
            urlsPerLevel.put(level, urls.size());
            while (urls.peek() != null) {
                if (this.timeIsUp) {
                    this.executor.shutdownNow();

                    System.out.println("[INFO] Shutting down...");
                    report();
                    try {
                        this.executor.awaitTermination(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        this.executor.shutdownNow();
                        System.exit(0);
                    }
                    return;
                }

                URL nextUrl = urls.remove();
                if (this.visited.contains(nextUrl)) {
                    continue;
                }
                this.executor.submit(new Scraper(nextUrl, this.stopWords, (url, children, words) -> {
                    getReportFromcrawledUrl(url, children, words, level);
                }));
            }
        }
    }

    /**
     * Loads the stop words list.
     */
    private void loadStopWordsList() {
        BufferedReader br = FileUtils.getBufferedReader("./res/stop_words.txt");

        String word;
        try {
            word = br.readLine();
            while (word != null) {
                this.stopWords.add(word);
                word = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Error reading stop words file:");
            e.printStackTrace();
        }
    }

    private void report() {
        System.out.println("--- Execution report");
        for (Integer level : urlsPerLevel.keySet()) {
            int total = urlsPerLevel.get(level);
            System.out.println("--- Level " + level + ":");
            System.out.println("      Total of " + total + " URLs");
        }

        System.out.print("--- Total URLs visited: " + visited.size());
    }

    /**
     * Sets the `timeIsUp` property that is used to stop child threads.
     */
    protected void stopExecution() {
        this.timeIsUp = true;
    }

    /**
     * Closes the file writers.
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        logFileWriter.close();
        csvFileWriter.close();
    }
}
