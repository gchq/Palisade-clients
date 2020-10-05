package uk.gov.gchq.palisade.client.java;

import uk.gov.gchq.palisade.client.java.download.*;

import com.google.common.eventbus.*;

public class TestEvent {

    public TestEvent() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) throws Exception {
        var main = new TestEvent();
        main.go();
        Thread.sleep(1000);
    }

    private void go() {
        var eb = new EventBus();
        eb.register(this);
        new Thread(() -> eb.post(DownloadCompletedEvent.of("abcd-1"))).run();
        new Thread(() -> eb.post(DownloadStartedEvent.of("abcd-1"))).run();
    }

    @Subscribe
    public void a(DownloadCompletedEvent e) throws Exception {
        Thread.sleep(200);
        System.out.println("a");
    }

    @Subscribe
    public void b(DownloadStartedEvent e) throws Exception {
        System.out.println("b");
    }

}
