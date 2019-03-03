package com.isprint.cnaac.server.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/** 
* @ClassName: Log4jInit 
* @Description: TODO(simple description this class what to do.) 
* @version 1.0 
*/
public class Log4jInit extends HttpServlet {
    /**
     * @Fields serialVersionUID : TODO(simple description what to do.)
     */
    private static final long serialVersionUID = 1L;

    /**
     * @Fields logger : TODO(simple description what to do.)
     */
    static Logger logger = Logger.getLogger(Log4jInit.class);

    public Log4jInit() {
    }

    public void init(ServletConfig config) throws ServletException {
        String prefix = config.getServletContext().getRealPath("/");
        String file = config.getInitParameter("log4j");
        String filePath = prefix + file;
        Properties props = new Properties();
        try {
            FileInputStream istream = new FileInputStream(filePath);
            props.load(istream);
            istream.close();
            String logFile = prefix + props.getProperty("log4j.appender.File.File");// set path
            props.setProperty("log4j.appender.File.File", logFile);
            PropertyConfigurator.configure(props);// load log4j.propertise
        }
        catch(IOException e) {
            toPrint("Could not read configuration file [" + filePath + "].");
            toPrint("Ignoring configuration file [" + filePath + "].");
            return;
        }
    }

    public static void toPrint(String content) {
        System.out.println(content);
    }
}
