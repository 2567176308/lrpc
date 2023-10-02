package com.lrpc;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperTest {

//    private static final String host = "127.0.0.1:2181";
    private static final String host = "192.168.200.131:2181";
    private static final Integer sessionTimeout = 3000;

    CountDownLatch countDownLatch = new CountDownLatch(1);


    private ZooKeeper zooKeeper;

    @Before
    public void createZK() {
        try {
//            默认watcher
            zooKeeper = new ZooKeeper(host,sessionTimeout,event -> {
                if (event.getType() == Watcher.Event.EventType.None) {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        System.out.println(event + " zookeeper已连接");
                        countDownLatch.countDown();
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createNode() {
        try {
            countDownLatch.await();
            zooKeeper.create("/lrpc/demo","hello rpc".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void deleteNode() {
        try {
            countDownLatch.await();
            zooKeeper.delete("/lrpc/demo",-1);
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void updateNode() {
        try {
            zooKeeper.setData("/lrpc/demo","hi lerpc".getBytes(),2);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if (zooKeeper != null) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
   @Test
    public void existsNode() {
       try {
           Stat exists = zooKeeper.exists("/lrpc/demo", null);
           int aversion = exists.getAversion();
           int cversion = exists.getCversion();
           int version = exists.getVersion();
           System.out.println("aversion = " + aversion);
           System.out.println("cversion = " + cversion);
           System.out.println("version = " + version);
       } catch (KeeperException | InterruptedException e) {
           throw new RuntimeException(e);
       }
   }


   @Test
    public void testWatcher() {
       try {
           /*
           可以选择new watcher 也可以选择 true ，选择默认watcher(由连接时候选定的)
            */
           Stat exists = zooKeeper.exists("/lrpc/demo", true);

           while (true) {
               Thread.sleep(10000);
           }
       } catch (KeeperException | InterruptedException e) {
           throw new RuntimeException(e);
       }finally {
           if (zooKeeper != null) {
               try {
                   zooKeeper.close();
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }
       }
   }
}
