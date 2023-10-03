package org.lrpc.manager.util;

import org.lrpc.manager.exception.NetworkException;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtil {
    public static String getIp() {

        try {
            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
//                过滤回环接口和虚拟接口
                if (iface.isLoopback() || iface.isVirtual() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> address = iface.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress addr = address.nextElement();
//                    过滤ipv6和回环地址
                    if (addr instanceof Inet6Address || addr.isLoopbackAddress()) {
                        continue;
                    }
                    String ipaddress = addr.getHostAddress();
                    return ipaddress;
                }
            }
            throw new NetworkException("没有获取到获取局域网ip");
        } catch (SocketException e) {
            throw new NetworkException("获取局域网ip失败");
        }
    }

    public static void main(String[] args) {
        System.out.println("getIp() = " + getIp());
    }
}
