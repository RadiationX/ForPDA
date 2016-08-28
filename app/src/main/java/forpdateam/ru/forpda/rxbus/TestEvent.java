package forpdateam.ru.forpda.rxbus;

/**
 * Created by isanechek on 28.08.16.
 */

public class TestEvent {
    private TestEvent() {}

    public static class Message {
        public final String message;

        public Message(String message) {
            this.message = message;
        }
    }
}
