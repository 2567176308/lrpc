package org.lrpc.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.lrpc.common.IdGenerator;
import org.lrpc.core.loadbalancer.LoadBalancer;
import org.lrpc.core.loadbalancer.impl.RoundRobinLoadBalancer;
import org.lrpc.core.serializer.Serializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * BootStrap配置类
 */
@Data
@Slf4j
public class Configuration {
//    服务端口
    private int port = 9094;
//    压缩方式
    private String compressType = "gzip";
//    应用名称
    private String appName = "default";
//    注册中心
    private RegistryConfig registryConfig;
//    协议
    private ProtocolConfig protocolConfig;
//    id发号器
    private IdGenerator idGenerator = new IdGenerator(1,2);
//    序列化协议
    private String serializeType = "jdk";

//    负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();


    public Configuration() {
//        读取xml配置信息
        loadFromXml(this);
    }

    /**
     * 加载xml配置类信息
     * @param configuration this实例
     */
    private void loadFromXml(Configuration configuration) {

        try {
//            创建document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("lrpc.xml");
            Document doc = builder.parse(resourceAsStream);

//            2、获取一个xpath解析器

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            String expression = "/configuration/serializer";
//            3、解析一个表达式
            Serializer serializer = parseObject(xPath, doc, expression, null);
            System.out.println(serializer);
            String expressionPort = "/configuration/port";
            System.out.println(parseString(xPath, doc, expressionPort));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("读取xml文件配置信息失败");
            throw new RuntimeException(e);
        }
    }
    /**
     * 解析一个节点返回属性值 <port>8080</port>
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @param expression xpath表达式
     * @return 节点的值
     */
    private String parseString(XPath xPath, Document doc,String expression)  {

        try {
            XPathExpression expr = xPath.compile(expression);
//        表达式获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("没有对应的配置策略，请检查您的配置文件是否正确");
            throw new RuntimeException(e);
        }

    }

    /**
     * 解析一个节点返回属性值 <port class="8080"></port>
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @param expression xpath表达式
     * @param attributeName 节点名称
     * @return 节点的值
     */
    private String parseString(XPath xPath, Document doc,String expression,String attributeName)  {

        try {
            XPathExpression expr = xPath.compile(expression);
//        表达式获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem("class").getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("没有对应的配置策略，请检查您的配置文件是否正确");
            throw new RuntimeException(e);
        }

    }
    /**
     * 解析一个节点、返回一个实例
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @param expression xpath表达式
     * @param paramType 参数类型列表
     * @param paramValue 参数值
     * @return 实例对象
     * @param <T> 泛型对象
     */
    @SuppressWarnings("unchecked")
    private <T> T parseObject(XPath xPath, Document doc,String expression,Class<?> paramType,Object... paramValue)  {

        try {
            XPathExpression expr = xPath.compile(expression);

//        表达式获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = null;
            aClass = Class.forName(className);
            Object instant = null;
            if (paramType == null) {
//            有参构造
                instant = aClass.getConstructor().newInstance();
            }else {
//            无参构造
                instant = aClass.getConstructor(paramType).newInstance(paramValue);
            }
            return (T)instant;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | XPathExpressionException e) {
            log.error("没有对应的配置策略，请检查您的配置文件是否正确");
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }
}
