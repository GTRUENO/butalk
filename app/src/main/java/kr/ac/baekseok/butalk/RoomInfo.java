package kr.ac.baekseok.butalk;

public class RoomInfo {
    private String roomId;
    private String lastMessage;
    private String sender;
    private int memberCount;
    private long timestamp;

    public RoomInfo() {}

    public RoomInfo(String roomId, String lastMessage, String sender, int memberCount, long timestamp) {
        this.roomId = roomId;
        this.lastMessage = lastMessage;
        this.sender = sender;
        this.memberCount = memberCount;
        this.timestamp = timestamp;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
