package messages;

import com.fasterxml.jackson.annotation.JsonProperty;


public class TelemetryMessage<T> {

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("id ")
    private String id;

    @JsonProperty("type")
    private String type;

    /*@JsonProperty("room_id")
    private String room_id;*/

    @JsonProperty("data")
    private T dataValue;

    public TelemetryMessage() {
    }

    public TelemetryMessage(String type, String id, /*String room_id,*/ T dataValue) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.id = id;
        //this.room_id = room_id;
        this.dataValue = dataValue;
    }

    public TelemetryMessage(long timestamp, String type, String room_id, T dataValue) {
        this.timestamp = timestamp;
        this.type = type;
        //this.room_id = room_id;
        this.dataValue = dataValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    //public String getRoom_id() { return room_id; }

    //public void setRoom_id(String room_id) { this.room_id = room_id; }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getDataValue() {
        return dataValue;
    }

    public void setDataValue(T dataValue) {
        this.dataValue = dataValue;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TelemetryMessage{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", type='").append(type).append('\'');
        //sb.append(", room_id=").append(room_id);
        sb.append(", id='").append(id).append('\'');
        sb.append(", dataValue=").append(dataValue);
        sb.append('}');
        return sb.toString();
    }
}
