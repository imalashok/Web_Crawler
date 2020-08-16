package crawler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlLinksParserAlgorithm {
    static volatile Map<String, String> htmlLinks = new ConcurrentSkipListMap<>();
    static volatile Queue<String> queue = new ConcurrentLinkedQueue<>();
    static volatile int sitesParsed = 0;

    public static Map<String, String> parseAllHtmlLinksInURL(String url) throws IOException {

        String shortUrl = "" + url;
        if (url.matches("https?://.*/.*")) {
            shortUrl = url.substring(0, url.lastIndexOf("/"));
            //System.out.println(shortUrl);
        }
        String domainUrl = shortUrl.replaceAll("https?://", "");
        if (domainUrl.contains(".")) {
            domainUrl = domainUrl.substring(0, domainUrl.lastIndexOf("."));
        }
        String siteText = "";
        String urlPrefix = shortUrl.startsWith("https") ? "https://" : "http://";
        boolean isUrlEndsWithBackSlash = shortUrl.endsWith("/");

        System.out.println("url = " + url); //for debug

        URL urlLink = new URL(url);
        URLConnection urlConnection = urlLink.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        String urlContentType = urlConnection.getContentType();

        HttpURLConnection huc = (HttpURLConnection) urlLink.openConnection(); //check 404 links
        huc.setRequestMethod("GET");
        huc.connect();


        System.out.println(urlConnection.getContentType());
        int urlResponse = huc.getResponseCode();
        System.out.println(urlResponse);

        if (urlResponse < 400 && urlContentType != null && urlContentType.startsWith("text/html")) {
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            synchronized (HtmlLinksParserAlgorithm.class) {
            siteText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                htmlLinks.put(url, getSiteTitle(siteText));
                System.out.println("map = " + htmlLinks);
            }
        }

        Pattern pattern = Pattern.compile("(?<=href=\").*?(?=\\s)");
        Matcher matcher = pattern.matcher(siteText);

        while (matcher.find() && WebCrawler.isRunning) {

            String htmlLink = matcher.group();
            System.out.println(htmlLink); //for debug

            String domainHtmlUrl = htmlLink.replaceAll("^h?t*p?s?:?/*", "");
            int indexOfSlash = domainHtmlUrl.indexOf("/");
            if (indexOfSlash > 0) {
                domainHtmlUrl = domainHtmlUrl.substring(0, domainHtmlUrl.indexOf("/"));
            }
            System.out.println(domainHtmlUrl); //for debug

            /** The link can be presented in many ways:

             An absolute link like https://www.wikipedia.org/index.html.
             A relative link like page.html. It doesn't contain slashes. To get an absolute link you should cut the original link to the last slash and add this relative link: https://www.wikipedia.org/index.html=> page.html = https://www.wikipedia.org/page.html.
             A link without protocol like //en.wikipedia.org/ or en.wikipedia.org/. Here you need just to add protocol of original page: http or https.

             If statements below handle different types of the links explained above*/

            if (htmlLink.length() > 3 && !htmlLink.contains("@") && !htmlLink.contains(".ico")) { //to skip empty tags, e.g. "#" or "/"
                if (htmlLink.startsWith("http")) {
                    htmlLink = htmlLink.replaceFirst("[\"'][\\w\\W]*$", "");

                } else if (!htmlLink.startsWith("/*" + domainUrl) /*&& !domainHtmlUrl.contains(".")*/) {
                    if (isUrlEndsWithBackSlash) {
                        htmlLink = htmlLink.replaceFirst("^/*", shortUrl).replaceFirst("[\"'][\\w\\W]*$", "");
                    } else {
                        htmlLink = htmlLink.replaceFirst("^/*", shortUrl + "/").replaceFirst("[\"'][\\w\\W]*$", "");
                    }
                } else if (htmlLink.startsWith("/")) {
                    htmlLink = htmlLink.replaceFirst("^/{1,2}", urlPrefix).replaceFirst("[\"'][\\w\\W]*$", "");
                } else {
                    if (isUrlEndsWithBackSlash) {
                        htmlLink = htmlLink.replaceFirst("^", shortUrl).replaceFirst("[\"'][\\w\\W]*$", "");
                    } else {
                        htmlLink = htmlLink.replaceFirst("^", shortUrl + "/").replaceFirst("[\"'][\\w\\W]*$", "");
                    }
                }
                System.out.println("htmlLink = " + htmlLink); //for debug

                synchronized (HtmlLinksParserAlgorithm.class) {
                    if (!htmlLinks.containsKey(htmlLink) || queue.contains(htmlLink)) {
                        siteText = getSiteText(htmlLink);
                        queue.offer(htmlLink);
                    }

                if (siteText != null) {
                        htmlLinks.put(htmlLink, getSiteTitle(siteText));
                        System.out.println("map = " + htmlLinks);
                    }
                }
            }
        }

        //System.out.println(htmlLinks); for debug


        return htmlLinks;
    }


    public synchronized static String getSiteText(String url) throws IOException {
        String siteText;

        URL urlLink = new URL(url);
        URLConnection urlConnection = urlLink.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        String urlContentType = urlConnection.getContentType();


        HttpURLConnection huc = (HttpURLConnection) urlLink.openConnection(); //check 404 links
        huc.setRequestMethod("GET");
        huc.connect();
        int urlResponse = huc.getResponseCode();

        System.out.println(urlConnection.getContentType());
        System.out.println(huc.getResponseCode());


        if (urlResponse < 400 && urlContentType != null && urlContentType.startsWith("text/html")) {
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            siteText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return siteText;
        }

        return null;
    }


    public static synchronized String getSiteTitle(String siteText) {
        Pattern pattern = Pattern.compile("<title>\\s*.*?\\s*.*?</title>");
        Matcher matcher = pattern.matcher(siteText);
        String siteTitle = "NoTitle";

        if (matcher.find()) {
            siteTitle = matcher.group().replaceAll("</?title>", "").trim();
        }

        return siteTitle;
    }
}
