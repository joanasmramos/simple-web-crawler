package com.crawler;

import com.microsoft.playwright.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Hello world!
 */
public final class Crawler {
    private Playwright playwright;
    private Browser browser;
    private ArrayList<URL> toVisit = new ArrayList<URL>();
    private ArrayList<URL> visited = new ArrayList<URL>();

    private ArrayList<String> stopWords = new ArrayList<String>();

    public Crawler(String seed) {
        try {
            this.playwright = Playwright.create();
        } catch (Exception e) {
            // TODO: handle exception
        }
        this.browser = playwright.chromium().launch();
        scrapePage(seed);
    }

    private void scrapePage(String url) {
        Page page = this.browser.newPage();
        page.navigate(url);

        findMostImportantWords(page);

        try {
            URL currentURL = new URL(url);
            List<ElementHandle> links = page.querySelectorAll("a");
            FileWriter fw = getFileWriter("./debugging/urls.txt");
            for (ElementHandle link : links) {
                String hyperlink = link.getAttribute("href");
                URL constructedURL = constructURL(currentURL, hyperlink);
                if (constructedURL != null) {
                    toVisit.add(constructedURL);
                    try {
                        fw.write(constructedURL.toExternalForm() + "\n");
                    } catch (IOException e) {
                    }
                }
            }
            visited.add(currentURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private URL constructURL(URL currentURL, String hyperlink) {
        if (!(hyperlink instanceof String) || hyperlink == "") {
            return null;
        }
        try {
            if (hyperlink.startsWith("//")) {
                return new URL("https:" + hyperlink);
            }
            if (hyperlink.startsWith("#")) {
                return new URL(currentURL.toExternalForm() + hyperlink);
            }
            return new URL(currentURL.getHost() + hyperlink);

        } catch (MalformedURLException e) {
            return null;
        }

    }

    private ArrayList<String> findMostImportantWords(Page page) {
        Locator body = page.locator("body");
        FileWriter fw = getFileWriter("./debugging/bag_of_words2.txt");
        List<String> textContents = body.allInnerTexts();
        String allText = String.join(" ", textContents);

        loadStopWordsList();

        HashMap<String, Integer> bagOfWords = getBagOfWords(allText);
        List<Map.Entry<String, Integer>> sortedBagOfWords = sortBagOfWords(bagOfWords);

        for (Map.Entry<String, Integer> term : sortedBagOfWords) {
            try {
                fw.write(term.getKey() + "=" + term.getValue() + "\n");
            } catch (IOException e) {
            }
        }

        ArrayList<String> importantWords = new ArrayList<String>();
        int numberOfImportantWords = (sortedBagOfWords.size() > 10) ? 10 : sortedBagOfWords.size();
        for (int i = 0; i < numberOfImportantWords; i++) {
            importantWords.add(sortedBagOfWords.get(i).getKey());
        }
        return importantWords;
    }

    private void loadStopWordsList() {
        BufferedReader br = getBufferedReader("./res/stop_words.txt");

        String word;
        try {
            word = br.readLine();
            while (word != null) {
                stopWords.add(word);
                word = br.readLine();
            }
        } catch (IOException e) {
        }
    }

    private HashMap<String, Integer> getBagOfWords(String document) {
        HashMap<String, Integer> bagOfWords = new HashMap<String, Integer>();

        // cleanup string
        document = document.toLowerCase();
        document = document.replaceAll("[^A-Za-z\\s]+", " ");
        String[] words = document.split("\\s+");

        for (String word : words) {
            if (stopWords.contains(word) || word == "" || word.length() == 1)
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

        // Sort the list
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

    protected void finalize() {
        try {
            this.playwright.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
        Crawler myCrawler = new Crawler("https://en.wikipedia.org/wiki/Timeline_of_the_far_future");
        System.out.println("I AM DONE");
    }
}
