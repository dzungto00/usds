package gov.usds.controller;

import gov.usds.service.UsdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.app.ConfigBean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@RestController
@RequestMapping(path = "/api/v2")
public class UsdsController {

    private static final Logger logger = LogManager.getLogger(UsdsController.class);

    @Value("${gov.ecfr.config.path}")
    private String ecfrConfigPath;

    @Value("${gov.usds.config.path}")
    private String usdsConfigPath;

    @Autowired
    private ConfigBean configBean;

    @Autowired
    private UsdsService usdsService;

    @GetMapping(path = "/status", produces = MediaType.TEXT_PLAIN_VALUE)
    public String serviceStatus() {
        return "Up and Running";
    }

    @GetMapping(path = "/initialize", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> initialize(HttpServletRequest request) {
        try {
            String ecfrConfigContent = configBean.getConfigContent(ecfrConfigPath);
            usdsService.saveToFile(ecfrConfigContent);
            usdsService.generateReports();
            String usdsConfigContent = configBean.getConfigContent(usdsConfigPath);
            if (usdsConfigContent == null || usdsConfigContent.trim().length() < 1) {
                return new ResponseEntity<String>("Initialize Status Failed!", HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<String>(usdsConfigContent, HttpStatus.OK);
            }
        } catch (IOException ioe) {
            logger.error(ioe);
            return new ResponseEntity<String>(ioe.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/loadLocalFile/{localFileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> localFileName(
            @PathVariable("localFileName") String localFileName) {
        try {
            String fileContent = usdsService.getFileContent(localFileName);
            logger.debug(localFileName + " content: " + fileContent);
            return new ResponseEntity<String>(fileContent, HttpStatus.OK);
        } catch (FileNotFoundException fnfe) {
            logger.error(fnfe);
            return new ResponseEntity<String>(fnfe.getMessage(), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping(path = "/report/statisticReport", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> statisticReport() {
        try {
            StringBuffer reportContent = new StringBuffer("[");
            reportContent.append(usdsService.getReportContent());
            reportContent.append("]");
            logger.debug("reportContent:" + reportContent.toString());
            return new ResponseEntity<String>(reportContent.toString(), HttpStatus.OK);
        } catch (FileNotFoundException fnfe) {
            logger.error(fnfe);
            return new ResponseEntity<String>(fnfe.getMessage(), HttpStatus.NO_CONTENT);
        }
    }

}
