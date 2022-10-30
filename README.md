# Simple web crawler

Simple crawler that starts from a URL, determines the 10 most important words on that page (according to a simplistic heuristic) and crawls the hyperlinks that it finds. It then repeats this process with those URLs.

## Command line args

1. URL of a page to start (including protocol)
2. Maximum number of levels (how deep the crawler can go)
3. Maximum execution time in seconds

## Results

The program writes the results into two files:
```
└───logs
│   │   log.txt
│   │   log.csv
```

They both contain the same information. Each line represents a visited URL and mentions:
- the URL
- the level of that URL
- how many children URLs were put in the queue while scraping that URL
- 10 most important words
