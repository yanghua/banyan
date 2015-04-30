package com.messagebus.scenario.client;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by yanghua on 4/4/15.
 */
public class XSLTTestCase {

    public static void main(String[] args) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Source xmlSource = new StreamSource(classLoader.getResourceAsStream("producerXml.xml"));
        Source xsltSource = new StreamSource(classLoader.getResourceAsStream("pToc.xsl"));

        TransformerFactory transFact = TransformerFactory.newInstance();
        try {
            Transformer trans = transFact.newTransformer(xsltSource);
            trans.transform(xmlSource, new StreamResult(System.out));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

}
