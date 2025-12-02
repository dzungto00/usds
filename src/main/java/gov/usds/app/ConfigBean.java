package gov.usds.app;

import gov.usds.service.UsdsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;

public class ConfigBean {

    private static final Logger logger = LogManager.getLogger(ConfigBean.class);

    @Autowired
    UsdsService usdsService;

    public String getConfigContent(String jsonFilePath) throws FileNotFoundException {
        String jsonContent = null;
        try {
            jsonContent = usdsService.getConfigContent(jsonFilePath);
        } catch (FileNotFoundException e) {
            throw e;
        }
        logger.debug(jsonContent);
        return jsonContent;
    }
}
