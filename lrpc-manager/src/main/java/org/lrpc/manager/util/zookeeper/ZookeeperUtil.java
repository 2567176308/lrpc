package org.lrpc.manager.util.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.lrpc.common.Constant;
import org.lrpc.manager.exception.ZookeeperException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
@Slf4j
public class ZookeeperUtil {

    static CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     *  默认连接地址与超时时间
     * @return zookeeper连接
     */
    public static ZooKeeper createZookeeper() {
//        定义连接参数
        String connectString = Constant.DEFAULT_ZK_CONNECT; //127.0.0.1:2181
        int timeout = Constant.TIME_OUT; // 1000

        return createZookeeper(connectString,timeout);

    }

    /**
     * 建立zookeeper连接
     * @param connectString 连接地址
     * @param timeout 超时时间
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper(String connectString, int timeout) {


        try {
            final ZooKeeper zookeeper = new ZooKeeper(connectString,timeout,event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    countDownLatch.countDown();
                }
            });
            log.info("zookeeper实例创建成功");
            return zookeeper;
//            创建zookeeper实例，建立连接
//            只有zookeeper实例，建立连接才放行
        } catch (IOException e) {
            log.info(e.getMessage());
            throw new ZookeeperException("zookeeper创建异常");
        }
    }

    /**
     * 创建一个节点的工具方法
     * @param zooKeeper zookeeper实例
     * @param node 节点
     * @param watcher watcher实例
     * @param createMode 节点的类型
     * @return true 创建成功,false 创建失败
     */
    public static Boolean createNode(ZooKeeper zooKeeper,ZookeeperNode node,Watcher watcher,CreateMode createMode) {
        try {
            createZookeeper();
            if (zooKeeper.exists(node.getNodePath(),watcher) == null) {

                String res = zooKeeper.create(node.getNodePath(),node.getData()
                        , ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点{}创建成功",res);
                return true;
            }else {
                log.info("节点{}已存在",node.getNodePath());
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            throw new ZookeeperException("创建节点时发生异常");
        }
    }

    /**
     * 关闭zookeeper连接
     * @param zookeeper zookeeper实例
     */
    public static void close(ZooKeeper zookeeper) {
        try {
            zookeeper.close();
        } catch (InterruptedException e) {
            throw new ZookeeperException("关闭连接时发送异常");
        }
    }

    /**
     * 判断节点是否存在
     * @param zk zookeeper实例
     * @param nodePath 节点路径
     * @param watcher watcher
     * @return true 存在 | false 不存在
     */
    public static boolean exists(ZooKeeper zk,String nodePath,Watcher watcher) {
        try {
            return zk.exists(nodePath,watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点{}是否存在发生异常",nodePath,e);
            throw new ZookeeperException("检查zookeeper节点是否存在发生异常");
        }
    }
}
