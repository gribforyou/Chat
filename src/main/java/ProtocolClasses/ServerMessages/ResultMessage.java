package ProtocolClasses.ServerMessages;

public class ResultMessage extends AbstractServerMessage {
    private int resultCode;

    public ResultMessage(int resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String getContent() {
        return Integer.toString(resultCode);
    }
}
