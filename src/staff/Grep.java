package staff;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

/** Search web pages for lines matching a pattern. */
public class Grep {
    public static void main(String[] args) throws Exception {
        
        // pattern to search for
        Pattern pattern = Pattern.compile("\\p{Upper}\\w*\\.\\p{Lower}\\w*\\(");
        
        // URLs to search
        String[] urls = new String[] {
                "http://web.mit.edu/6.005/www/sp13/psets/ps0/",
                "http://web.mit.edu/6.005/www/sp13/psets/ps1/",
                "http://web.mit.edu/6.005/www/sp13/psets/ps2/",
                "http://web.mit.edu/6.005/www/sp13/psets/ps3/",
        };
        
        // list for accumulating matching lines
        List<Line> matches = Collections.synchronizedList(new ArrayList<Line>());
        
        // queue for sending lines from producers to consumers
        BlockingQueue<Line> queue = new LinkedBlockingQueue<Line>();
        
        Thread[] producers = new Thread[urls.length]; // one producer per URL
        Thread[] consumers = new Thread[10];
        
        for (int ii = 0; ii < consumers.length; ii++) { // start Consumers
            Thread consumer = consumers[ii] = new Thread(
                    new Consumer(pattern, queue, matches));
            consumer.start();
        }
        
        for (int ii = 0; ii < urls.length; ii++) { // start Producers
            Thread producer = producers[ii] = new Thread(
                    new Producer(urls[ii], queue));
            producer.start();
        }
        
        for (Thread producer : producers) { // wait for Producers to stop
            producer.join();
        }
        
        for (int ii = 0; ii < consumers.length; ii++) { // stop Consumers
            queue.add(new Stop());
        }
        
        for (Thread consumer : consumers) { // wait for Consumers to stop
            consumer.join();
        }
        
        for (Line match : matches) {
            System.out.println(match);
        }
    }
}

class Producer implements Runnable {
    
    private final String url;
    private final BlockingQueue<Line> queue;
    
    Producer(String url, BlockingQueue<Line> queue) {
        this.url = url;
        this.queue = queue;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            int lineNo = 0;
            String line;
            while ((line = in.readLine()) != null) {
                queue.add(new Text(url, lineNo++, line));
                Thread.yield(); // DEMO
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
}

class Consumer implements Runnable {
    
    private final Pattern pattern;
    private final BlockingQueue<Line> queue;
    private final List<Line> matches;
    
    Consumer(Pattern pattern, BlockingQueue<Line> queue, List<Line> matches) {
        this.pattern = pattern;
        this.queue = queue;
        this.matches = matches;
    }

    public void run() {
        try {
            while (true) {
                Line line = queue.take();
                if (line.text() == null) {
                    break;
                }
                if (found(pattern, line.text())) {
                    matches.add(line);
                }
                Thread.yield(); // DEMO
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    
    /** Return true iff the given pattern appears in the given text. */
    private boolean found(Pattern p, CharSequence cs) {
        return p.matcher(cs).find();
    }
    
}

interface Line {
    /** Return the filename. */
    public String filename();
    /** Return the line number. */
    public int lineNo();
    /** Return the text on the line. */
    public String text();
}

class Text implements Line {
    private final String filename;
    private final int lineNo;
    private final String text;
    
    public Text(String filename, int lineNo, String text) {
        this.filename = filename;
        this.lineNo = lineNo;
        this.text = text;
    }
    
    public String filename() {
        return filename;
    }
    
    public int lineNo() {
        return lineNo;
    }
    
    public String text() {
        return text;
    }
    
    @Override public String toString() {
        return filename + ":" + lineNo + ":" + text;
    }
}

class Stop implements Line {
    
    public String filename() {
        return null;
    }
    
    public int lineNo() {
        return -1;
    }
    
    public String text() {
        return null;
    }
}
