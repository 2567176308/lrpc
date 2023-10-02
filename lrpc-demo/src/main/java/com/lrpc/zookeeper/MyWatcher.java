package com.lrpc.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Watcher.Event.EventType.None) {
            if (event.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("zookeeper连接成功");
            }else if (event.getState() == Event.KeeperState.AuthFailed) {
                System.out.println("zookeeper认证失败");
            }else if (event.getState() == Event.KeeperState.Disconnected) {
                System.out.println("zookeeper断开连接");
            }
        }else if (event.getType() == Event.EventType.NodeCreated) {
            System.out.println(event.getPath() + "节点已创建");
        }else if (event.getType() == Event.EventType.NodeDeleted) {
            System.out.println(event.getPath() + "节点已被删除");
        }
    }
}
