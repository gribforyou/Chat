package ProtocolClasses.ClientMessage;

import java.io.Serializable;

public abstract class AbstractClientMessage implements Serializable {
    public abstract String getContent();
}
