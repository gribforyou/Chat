package ServerMessages;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMessage extends AbstractServerMessage{
    private String message;
    private String senderNick;
    private Date date;

    public ChatMessage(String message, String senderNick, Date date) {
        this.message = message;
        this.senderNick = senderNick;
        this.date = date;
    }

    @Override
    public String getContent(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return "<" + formatter.format(date) + ">" + senderNick + ": " + message;
    }
}
