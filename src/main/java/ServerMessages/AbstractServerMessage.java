package ServerMessages;

import java.io.Serializable;

public abstract class AbstractServerMessage implements Serializable {
    public abstract String getContent();
}
