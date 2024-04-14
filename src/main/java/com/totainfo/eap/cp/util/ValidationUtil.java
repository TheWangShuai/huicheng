package com.totainfo.eap.cp.util;

import com.totainfo.eap.cp.mode.ValidationItem;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author WangShuai
 * @date 2024/4/12
 */
public class ValidationUtil {
    public static List<ValidationItem> getValidationItemList() throws DocumentException {
        SAXReader saxReader = new SAXReader();
        File file = new File("D:\\IdeaFile\\huicheng\\eap-cp\\config\\ValidationConfig.xml");
        Document document = saxReader.read(file);
        Element root = document.getRootElement();
        List<ValidationItem> validationItemList = new ArrayList<>();
        Element element;
        List<Node> nodes = root.selectNodes("Item[@name='validationItem']/Item");
        for (Node node : nodes) {
            element = (Element) node;
            String paramNo = element.attributeValue("paramNo");
            String paramId = element.attributeValue("paramId");
            String paramName = element.attributeValue("paramName");
            String paramType = element.attributeValue("paramType");
            String defaultValue = element.attributeValue("defaultValue");
            String remark = element.attributeValue("remark");
            ValidationItem validationItem = new ValidationItem();
            validationItem.setParamNo(paramNo);
            validationItem.setParamId(paramId);
            validationItem.setParamName(paramName);
            validationItem.setParamType(paramType);
            validationItem.setDefaultValue(defaultValue);
            validationItem.setRemark(remark);
            validationItemList.add(validationItem);
        }
        return validationItemList;
    }

    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        strings.add("1");
        strings.add("2");
        strings.add("3");
        strings.add("4");
        strings.add("5");
        strings.add("6");
        Iterator<String> iterator = strings.iterator();
        if (iterator.hasNext()){
            String next = iterator.next();
            String next1 = iterator.next();
            String next2 = iterator.next();
            System.out.println(next2);
        }
    }
}