package io.cockroachdb.pestcontrol.web.push;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimpMessagePublisher {
    private static final int SEND_DELAY_MS = 2500;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    public <T> void convertAndSend(TopicName topic, Object payload) {
        simpMessagingTemplate.convertAndSend(topic.value, payload);
    }

    public <T> void convertAndSend(TopicName topic) {
        convertAndSend(topic, "");
    }

    public <T> void convertAndSendLater(TopicName topic) {
        convertAndSendLater(topic, "");
    }

    public <T> void convertAndSendLater(TopicName topic, Object payload) {
        convertAndSendLater(topic, payload, SEND_DELAY_MS);
    }

    public <T> void convertAndSendLater(TopicName topic, Object payload, long delayMillis) {
        scheduledExecutorService.schedule(
                () -> convertAndSend(topic, payload), delayMillis, TimeUnit.MILLISECONDS);
    }
}
