package org.lrpc.core.loadbalancer.impl;

import lombok.extern.slf4j.Slf4j;
import org.lrpc.common.exception.LoadBalancerException;
import org.lrpc.core.LrpcBootStrap;
import org.lrpc.core.loadbalancer.AbstractLoadBalancer;
import org.lrpc.core.loadbalancer.Selector;
import org.lrpc.core.transport.message.LrpcRequest;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮寻的负载均衡策略
 */
@Slf4j
public class ConsistentHashSelectorBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serverList) {
        return new ConsistentHashSelector(serverList,128);
    }



    private static class ConsistentHashSelector implements Selector {

        /*
        hash环用来存储服务器节点
         */
        private SortedMap<Integer,InetSocketAddress> circle = new TreeMap<>();
//        虚拟节点个数
        private int virtualNodes;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
//            我们应该尝试将节点转换为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress address : serviceList) {
                addNodeToCircle(address);
            }
        }
        @Override
        public InetSocketAddress getNext() {
            LrpcRequest lrpcRequest = LrpcBootStrap.REQUEST_THREAD_LOCAL.get();

//            根据请求的特征来选择服务器 id
            String requestId = Long.toString(lrpcRequest.getRequestId());

//            对请求id做hash，字符串默认hash不行，连续请求，hash也会是连续
            int hash = hash(requestId);
//            判断hash是否直接落在一个服务器上,和服务器的hash一样
            if (!circle.containsKey(hash)) {
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);

                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();


            }


            return circle.get(hash);

        }


        /**
         * 将每个节点挂在到hash环上
         * @param inetSocketAddress 地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
//                挂载到hash环上
                circle.put(hash,inetSocketAddress);
                if (log.isDebugEnabled()) {
                    log.debug("hash[{}]的节点已经挂在到了哈希环上",hash);
                }
            }
        }
        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
//                挂载到hash环上
                circle.remove(hash);
            }
        }
        /**
         * 具体hash算法
         * @param s
         * @return hash值
         */
        private int hash(String s) {

            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md.digest(s.getBytes());
//            md5得到的结果是一个字节数组
            int res = 0;
            for (int i = 0; i < 2; i++) {
                res = res << 8;
                if (digest[i] < 0) {
                    res = res | (digest[i] & 255);
                }
                res = res | digest[i];

            }
            return res;
        }
    }



}
