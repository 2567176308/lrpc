package org.lrpc.manager.util.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZookeeperNode {
    private String nodePath;
    private byte[] data;




}
