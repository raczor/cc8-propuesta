package project.routing;

import tcp.model.MessageType;

//Modulo de Routing, Mensaje base
public class MessageJsonRoutingBase {
    private String from;
    private MessageType type;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
