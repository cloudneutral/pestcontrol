package io.cockroachdb.pestcontrol.web.push;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"links", "embedded", "templates"})
public class MessageModel extends RepresentationModel<MessageModel> {
    private String message;

    private MessageType messageType = MessageType.information;

    public MessageModel() {
    }

    public MessageModel(String message) {
        this.message = message;
    }

    public static MessageModel from(String message) {
        return new MessageModel(message);
    }

    public String getMessage() {
        return message;
    }

    public MessageModel setMessage(String message) {
        this.message = message;
        return this;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public MessageModel setMessageType(MessageType messageType) {
        this.messageType = messageType;
        return this;
    }
}
