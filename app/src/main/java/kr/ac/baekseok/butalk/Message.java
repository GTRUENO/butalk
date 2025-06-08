package kr.ac.baekseok.butalk;

public class Message {
    private String sender;        // UID
    private String message;
    private long timestamp;

    public Message() {}  // Firebase용 기본 생성자

    // 기존 시스템 메시지용 생성자
    public Message(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    // 닉네임 고정 저장용 새 생성자
    public Message(String sender, String senderName, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }



    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }



    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
