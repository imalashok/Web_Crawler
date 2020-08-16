package crawler;

import java.io.IOException;
import java.util.Arrays;

public class ThreadWorker extends Thread {

    @Override
    public void run() {
        String htmlLink = "";

        while (HtmlLinksParserAlgorithm.sitesParsed <= WebCrawler.maxDepth && WebCrawler.isRunning) {

            synchronized (HtmlLinksParserAlgorithm.class) {
                if (HtmlLinksParserAlgorithm.queue.size() > 0) {
                    HtmlLinksParserAlgorithm.sitesParsed++;
                    System.out.println(getName() + " started");
                    htmlLink = HtmlLinksParserAlgorithm.queue.poll();
                    System.out.println("queue= " + HtmlLinksParserAlgorithm.queue);
                }
            }

            if (htmlLink.length() > 0) {
                try {
                    HtmlLinksParserAlgorithm.parseAllHtmlLinksInURL(htmlLink);
                } catch (IOException ex) {
                    System.out.println("bla");
                    System.out.println(Arrays.toString(ex.getStackTrace()));
                }
            }

        }
        System.out.println(getName() + " finished");
        System.out.println("queue= " + HtmlLinksParserAlgorithm.queue);
    }
}
