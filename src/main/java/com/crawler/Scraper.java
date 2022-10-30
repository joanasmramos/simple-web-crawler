package com.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class Scraper implements Runnable {
    private URL currentURL;
    private HashSet<URL> children = new HashSet<URL>();
    private Playwright playwright;
    private ArrayList<String> stopWords = new ArrayList<String>();
    private ArrayList<String> importantWords = new ArrayList<String>();
    private ReportingFunction callbackFunction;

    protected Scraper(URL url, ArrayList<String> stopWords, ReportingFunction callback) {
        this.currentURL = url;
        this.stopWords = stopWords;
        this.callbackFunction = callback;
        try {
            this.playwright = Playwright.create();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void run() {
        Browser browser = playwright.chromium().launch();
        Page page = browser.newPage();
        page.navigate(this.currentURL.toExternalForm());

        this.findMostImportantWords(page);

        List<ElementHandle> links = page.querySelectorAll("a");
        for (ElementHandle link : links) {
            String hyperlink = link.getAttribute("href");
            URL constructedURL = constructURL(hyperlink);
            if (constructedURL != null) {
                children.add(constructedURL);
            }
        }

        this.callbackFunction.reportFromCrawledURL(currentURL, children, importantWords);
    }

    private URL constructURL(String hyperlink) {
        if (!(hyperlink instanceof String) || hyperlink == "") {
            return null;
        }
        try {
            if (hyperlink.startsWith("//")) {
                return new URL("https:" + hyperlink);
            }
            if (hyperlink.startsWith("#")) {
                return null;
            }
            return new URL(this.currentURL.getHost() + hyperlink);

        } catch (MalformedURLException e) {
            return null;
        }
    }

    private ArrayList<String> findMostImportantWords(Page page) {
        Locator body = page.locator("body");
        List<String> textContents = body.allInnerTexts();
        String allText = String.join(" ", textContents);

        HashMap<String, Integer> bagOfWords = this.getBagOfWords(allText);
        List<Map.Entry<String, Integer>> sortedBagOfWords = this.sortBagOfWords(bagOfWords);

        int numberOfImportantWords = (sortedBagOfWords.size() > 10) ? 10 : sortedBagOfWords.size();
        for (int i = 0; i < numberOfImportantWords; i++) {
            this.importantWords.add(sortedBagOfWords.get(i).getKey());
        }
        return this.importantWords;
    }

    private HashMap<String, Integer> getBagOfWords(String document) {
        HashMap<String, Integer> bagOfWords = new HashMap<String, Integer>();

        // cleanup string
        document = document.toLowerCase();
        document = document.replaceAll("[^A-Za-z\\s]+", " ");
        String[] words = document.split("\\s+");

        for (String word : words) {
            if (this.stopWords.contains(word) || word == "" || word.length() == 1)
                continue;

            Integer termFrequency = bagOfWords.get(word);
            if (termFrequency == null) {
                bagOfWords.put(word, 1);
            } else {
                bagOfWords.put(word, termFrequency + 1);
            }
        }
        return bagOfWords;
    }

    private List<Map.Entry<String, Integer>> sortBagOfWords(HashMap<String, Integer> bagOfWords) {
        List<Map.Entry<String, Integer>> sortedList = new LinkedList<Map.Entry<String, Integer>>(bagOfWords.entrySet());

        Collections.sort(sortedList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                    Map.Entry<String, Integer> o2) {
                return Integer.compare(o2.getValue(), o1.getValue());
            }
        });

        return sortedList;
    }

    private FileWriter getFileWriter(String filepath) {
        File file = new File(filepath);
        try {
            return new FileWriter(file);
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

    @Override
    protected void finalize() {
        try {
            this.playwright.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
