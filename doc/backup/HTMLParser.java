package kr.ac.kaist.jsaf.nodes_util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.contrib.input.LineNumberElement;
import org.jdom.contrib.input.LineNumberSAXBuilder;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;

import dk.brics.tajs.htmlparser.HTMLVisitorImpl;
import dk.brics.tajs.htmlparser.JavaScriptSource;
import dk.brics.tajs.util.Collections;

public class HTMLParser {

	private static Logger logger = Logger.getLogger(HTMLParser.class); 
	
	private List<JavaScriptSource> jsList;
	
	public HTMLParser() { }
	
	public Document build(String inputFileName) throws IOException {
		String outputFileName;
        if (inputFileName.endsWith(".htm")) {
            outputFileName = inputFileName.substring(0, inputFileName.indexOf(".htm")) + ".tidy.htm";
        } else if (inputFileName.endsWith(".html")) {
            outputFileName = inputFileName.substring(0, inputFileName.indexOf(".html")) + ".tidy.html";
        } else {
            outputFileName = inputFileName + ".tidy";
        }

        {
            Tidy tidy = newTidy();
            org.w3c.dom.Document document = tidy.parseDOM(new FileInputStream(inputFileName), null);
            DOMImplementationRegistry registry;
			try {
				registry = DOMImplementationRegistry.newInstance();
	            DOMImplementationLS impl = 
	                    (DOMImplementationLS)registry.getDOMImplementation("LS");
                LSSerializer serializer = impl.createLSSerializer();
                LSOutput output = impl.createLSOutput();
                output.setByteStream(new FileOutputStream(outputFileName));
                FileOutputStream outputStream = new FileOutputStream(outputFileName);
                tidy.pprint(document, outputStream);
                outputStream.close();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        SAXBuilder builder = new LineNumberSAXBuilder();
        Document document;
        try {
            document = builder.build(outputFileName);
        } catch (JDOMException e) {
            throw new IOException(e);
        }

        JavaScriptVisitor visitor = new JavaScriptVisitor(document, outputFileName);
        visitor.visitDocument();
        jsList = visitor.getJavaScript();

        return document;
	}
	
    private static Tidy newTidy() {
        Tidy tidy = new Tidy();
        tidy.setMessageListener(new TidyMessageListener() {

            @Override
            public void messageReceived(TidyMessage msg) {
    			if (logger.isDebugEnabled()) 
    				logger.debug(String.format("HTML warning at %s:%s : %s", new Object[]{
    						msg.getLine(), msg.getColumn(), msg.getMessage()}));
            }
        });
        tidy.setDropEmptyParas(false);
        tidy.setDropFontTags(false);
        tidy.setDropProprietaryAttributes(false);
        tidy.setTrimEmptyElements(false);
        tidy.setXHTML(true);
        tidy.setIndentAttributes(false);
        tidy.setIndentCdata(false);
        tidy.setIndentContent(false);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        tidy.setShowErrors(0);
        tidy.setEncloseBlockText(false);
        tidy.setEscapeCdata(false);
        tidy.setDocType("omit");
        tidy.setInputEncoding("UTF-8");
        tidy.setRawOut(true);
        tidy.setOutputEncoding("UTF-8");
        tidy.setFixUri(false);
        Properties prop = new Properties();
        prop.put("new-blocklevel-tags", "canvas");
        tidy.getConfiguration().addProps(prop);
        return tidy;
    }	
	
    public String tidyJS() {
    	String output = "";
    	int lineCounter = 0;
    	for (int i = 0; i < jsList.size(); i++) {
    		String js = jsList.get(i).getJavaScript().replaceAll("(?s)<!--.*?-->", "");
    		int lineNumber = jsList.get(i).getLineNumber();
    		for (int j = 0; j < lineNumber-lineCounter-1; j++)
    			output += "\n";
    		lineCounter = lineNumber + countLines(js);
    		output += js;
    	}
    	return output;
    }
    
    private static int countLines(String str){
	   String[] lines = str.split("\r\n|\r|\n");
	   return lines.length;
    }
    
	private static class JavaScriptVisitor extends HTMLVisitorImpl {

        private List<JavaScriptSource> fileToJS = Collections.newList();

        private final String htmlFileName;
        private final File file;

        private JavaScriptVisitor(Document document, String htmlFileName) {
            super(document);
            this.htmlFileName = htmlFileName;
            this.file = new File(htmlFileName);
        }

        public List<JavaScriptSource> getJavaScript() {
            return fileToJS;
        }
        
        @Override
        public void visitScript(Element element) {
            LineNumberElement elm = (LineNumberElement) element;
            String src = element.getAttributeValue("src");
            
            if (src == null) {
                // Embedded script
                fileToJS.add(new JavaScriptSource(htmlFileName, element.getText() + "\n", elm.getStartLine()));
            }
        }
	}


}
