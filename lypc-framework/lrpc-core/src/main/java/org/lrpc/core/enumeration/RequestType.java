package org.lrpc.core.enumeration;

public enum RequestType {

    REQUEST((byte) 1,"普通请求"),HEART_BEAT((byte) 2,"心跳检测请求");

    private byte id;

    private String type;
    RequestType(byte id,String type) {
        this.type = type;
        this.id = id;
    }
    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
