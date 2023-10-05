package org.lrpc.core.enumeration;

public enum RespCode {
    SUCCESS((byte) 1,"成功"), FAIL((byte) 2,"失败");
    private byte code;
    private String desc;
    RespCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
