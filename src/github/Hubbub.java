package github;

import java.io.IOException;
import java.net.*;

import org.json.*;

/** GitHub event notifier. */
public class Hubbub {
    
    public static void main(String[] args) throws Exception {
        HubPublisher pub = new HubPublisher();
        
        HubListener listen = new ConsoleListener();
        pub.addListener(listen);
        // TODO also: HubGUI.create(pub);
        
        new Thread(pub).start();
    }
}

/** Notifies listeners of new GitHub event stream events. */
class HubPublisher implements Runnable {
    
    private final URL events;
    
    HubPublisher() throws MalformedURLException {
        events = new URL("https://api.github.com/events");
    }
    
    public void addListener(HubListener listener) {
        // TODO add listener
    }
    
    private void announce(JSONObject json) throws IOException {
        HubEvent event = new HubEvent(json);
        // TODO announce event to listeners
    }
    
    public void run() {
        try {
            while (true) {
                URLConnection conn;
                conn = events.openConnection();
                JSONArray arr = new JSONArray(new JSONTokener(conn.getInputStream()));
                
                // We are only allowed to poll for events at a certain interval.
                // To simulate a continuous stream of incoming events,
                // we will sleep a fraction of that interval between each result.
                int delay = conn.getHeaderFieldInt("X-Poll-Interval", 60) * 1000 / arr.length();
                
                for (int ii = 0; ii < arr.length(); ii++) {
                    announce(arr.getJSONObject(ii));
                    Thread.sleep(delay);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}

interface HubListener {
    public void event(HubEvent event);
}

class ConsoleListener implements HubListener {
    public void event(HubEvent event) {
        System.out.println(event);
    }
}

/** Represents a single event in the GitHub event stream. */
class HubEvent {
    
    enum Type {
        CommitCommentEvent,
        CreateEvent,
        DeleteEvent,
        DownloadEvent,
        FollowEvent,
        ForkEvent,
        ForkApplyEvent,
        GistEvent,
        GollumEvent,
        IssueCommentEvent,
        IssuesEvent,
        MemberEvent,
        PublicEvent,
        PullRequestEvent,
        PullRequestReviewCommentEvent,
        PushEvent,
        TeamAddEvent,
        WatchEvent
    };
    
    public final Type type;
    public final URL avatar;
    public final String repo;
    
    HubEvent(JSONObject obj) throws MalformedURLException {
        type = Type.valueOf(obj.getString("type"));
        avatar = new URL(obj.getJSONObject("actor").getString("avatar_url"));
        repo = obj.getJSONObject("repo").getString("name");
    }
    
    @Override public String toString() {
        return type + ":" + repo;
    }
}
