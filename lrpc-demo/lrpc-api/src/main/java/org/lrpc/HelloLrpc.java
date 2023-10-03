package org.lrpc;

public interface HelloLrpc {
    /**
     * 通用接口，server和client都需要依赖
     * @param msg 发送的具体消息
     * @return 返回结果
     */
    String sayHi(String msg);
}
