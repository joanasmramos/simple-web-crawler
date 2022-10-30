package com.crawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Functional interface for scraper callback.
 */
@FunctionalInterface
public interface ReportingFunction {

    void reportFromCrawledURL(URL crawledURL, HashSet<URL> childrenURLs, ArrayList<String> mostImportantWords);
}
