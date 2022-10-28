package com.crawler;

import com.microsoft.playwright.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 */
public final class Crawler {
    private Playwright playwright;
    private Browser browser;
    private ArrayList<URL> toVisit = new ArrayList<URL>();
    private ArrayList<URL> visited = new ArrayList<URL>();

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

    private void findMostImportantWords(Page page) {
        Locator headings = page.locator("h1");
        System.out.println(headings.allTextContents());
    }

    private FileWriter getFileWriter(String filepath) {
        File file = new File(filepath);
        try {
            return new FileWriter(file);
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
