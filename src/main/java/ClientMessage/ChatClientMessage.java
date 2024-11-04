package ClientMessage;

public class ChatClientMessage extends AbstractClientMessage {
    private String messageText;
    private String nickName;

    public ChatClientMessage(String messageText, String nickName) {
        this.messageText = messageText;
        this.nickName = nickName;
    }

    @Override
    public String getContent(){
        return nickName + " ["+ messageText + "]";
    }
}
