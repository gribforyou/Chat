package ProtocolClasses.ClientMessage;

public class RegistrationClientMessage extends AbstractClientMessage {
    private String nickName;

    public RegistrationClientMessage(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String getContent(){
        return nickName;
    }
}
