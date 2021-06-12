package com.iminling.common.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * XML通用处理类
 * @author yslao@outlook.com
 */
public class XmlUtils {

    /**
     * 扩展xstream，使其支持CDATA块
     */
    private static XStream xstream = new XStream(new XppDriver() {
        @Override
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new PrettyPrintWriter(out) {
                // 对所有xml节点的转换都增加CDATA标记
                boolean cdata = true;

                @Override
                public void startNode(String name, Class clazz) {
                    super.startNode(name, clazz);
                }

                @Override
                protected void writeText(QuickWriter writer, String text) {
                    if (cdata) {
                        writer.write("<![CDATA[");
                        writer.write(text);
                        writer.write("]]>");
                    } else {
                        writer.write(text);
                    }
                }
            };
        }
    });


    /**
     * 将对象数据转换为xml数据
     * @param object 对象
     * @param clazz 类
     * @return 字符串
     */
    public static String object2Xml(Object object, Class<?> clazz) {
        xstream.alias("xml", clazz);
        return xstream.toXML(object);
    }

    /**
     * xml转map对象
     * @param xmlBuffer StringBuffer
     * @return baseMap
     * @throws DocumentException 文档异常
     */
    public static Map<String, String> xml2Map(StringBuffer xmlBuffer) throws DocumentException {
        return xml2Map(new String(xmlBuffer.toString().getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8));
    }

    /**
     * xml字符串转map对象
     * @param xml xml字符串
     * @return baseMap
     * @throws DocumentException 文档异常
     */
    public static Map<String, String> xml2Map(String xml) throws DocumentException {
        Document document = DocumentHelper.parseText(xml);
        return xml2Map(document);
    }

    /**
     * document对象转map对象
     * @param document document
     * @return basemap
     */
    public static Map<String, String> xml2Map(Document document) {
        Map<String, String> jsonObject = new HashMap<>();
        Element root = document.getRootElement();
        List<Element> elementList = root.elements();
        for (Element element : elementList){
            jsonObject.put(element.getName(), element.getText());
        }
        return jsonObject;
    }

    /**
     * 流转map
     * @param inputStream InputStream inputStream = request.getInputStream();
     * @return basemap
     * @throws IOException io异常
     * @throws DocumentException 文档异常
     */
    public static Map<String, String> xml2Map(InputStream inputStream) throws IOException, DocumentException {
        Map<String, String> jsonObject = new HashMap<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();
        List<Element> list = root.elements();
        for(Element e:list){
            jsonObject.put(e.getName(), e.getText());
        }
        inputStream.close();
        return jsonObject;
    }

    /**
     * map转xml
     * @param map map
     * @return 字符串
     */
    public static String map2Xml(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("<xml>");
            Set<String> keys = map.keySet();
            for (String key : keys) {
                sb.append("<"+key+"><![CDATA["+ map.get(key) +"]]></"+key+">");
            }
            sb.append("</xml>");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
