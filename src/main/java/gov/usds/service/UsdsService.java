package gov.usds.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Scanner;

@Service
public class UsdsService {

    private static final Logger logger = LogManager.getLogger(UsdsService.class);

    private final static String ECFR_BASE_API = "https://www.ecfr.gov/";

    private final static String SOURCE_DATA_FOR_REPORT_API =
            ECFR_BASE_API + "api/search/v1/counts/hierarchy";
    
    private final static String REPORT_FILE_NAME = "reportStatistic";
            
    @Value("${gov.usds.json.data.folder}")
    private String dataFolder;

    @Value("${gov.usds.json.report.folder}")
    private String reportFolder;

    private StringBuffer extractFileContent(String filePath) throws FileNotFoundException {
        StringBuffer buffer = new StringBuffer();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                buffer.append(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            throw e;
        }
        logger.debug("extractFileContent:" + buffer.toString());
        return buffer;
    }

    public String getConfigContent(String configFile) throws FileNotFoundException {
        if (configFile == null || configFile.trim().length() < 1) {
            throw new FileNotFoundException();
        }
        logger.debug(configFile);
        StringBuffer buffer = extractFileContent(configFile);
        logger.debug("config buffer: " + buffer);
        return buffer.toString();
    }

    public String getFileContent(String localFileName) throws FileNotFoundException {
        if (localFileName == null || localFileName.trim().length() < 1) {
            throw new FileNotFoundException();
        }

        String filePath = dataFolder + File.separator + localFileName + ".json";
        logger.debug(filePath);

        StringBuffer buffer = extractFileContent(filePath);
        logger.debug("file buffer: " + buffer);
        return buffer.toString();
    }
    
    public void saveToFile(final String jsonContent) throws IOException {
        if (jsonContent != null &&  jsonContent.trim().length() > 0) {
            JSONArray ja = new JSONArray(jsonContent);
            for (Object object : ja) {
                if (object instanceof JSONObject) {
                    JSONObject jo = (JSONObject) object;
                    JSONArray serviceApis = jo.getJSONArray("serviceApi");
                    for (Object obj : serviceApis) {
                        if (obj instanceof JSONObject) {
                            JSONObject serviceApi = (JSONObject) obj;
                            String localFileName = serviceApi.get("localFileName").toString();
                            String fileName = String.format("%s.json",
                                    dataFolder + File.separator + localFileName);
                            String api = serviceApi.get("api").toString();
                            if (api.contains("{") || api.contains("}")) {
                                continue;
                            }
                            String uri = ECFR_BASE_API + api;
                            RestTemplate restTemplate = new RestTemplate();
                            String json = restTemplate.getForObject(uri, String.class);
                            if (json != null && json.trim().length() > 0) {
                            	File folder = new File(dataFolder);
                            	if (!folder.exists()) {
                            		folder.mkdir();
                            	}
                                try (BufferedWriter writer = new BufferedWriter(
                                        new FileWriter(fileName, false))) {
                                    writer.write(json);
                                    logger.debug(fileName + " saved!");
                                } catch (IOException e) {
                                    throw e;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private String checkSumSHA256Hash(Object input) {
        String checksum = "";
        if (input == null || input.toString().trim().length() < 1) {
            return checksum;
        }

        try {
            // Get a MessageDigest instance for SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Update the digest with the input string's bytes
            md.update(input.toString().getBytes());

            // Get the hash bytes
            byte[] hashBytes = md.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            checksum = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
        }

        return checksum;
    }

    public void generateReports() throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String reportContent = restTemplate.getForObject(SOURCE_DATA_FOR_REPORT_API, String.class);
        if (reportContent == null || reportContent.trim().length() < 1) return;

        JSONObject jsonReport = new JSONObject(reportContent);
        JSONObject countNameValue = new JSONObject();
        JSONObject checkSumNameValue = new JSONObject();
        if (jsonReport.has("children")) {
            Object children = jsonReport.get("children");
            if (children instanceof JSONArray) {
                for (Object child : (JSONArray) children) {
                    JSONObject json = (JSONObject) child;
                    logger.debug("level: " + json.get("level"));
                    logger.debug("heading: " + json.get("heading"));
                    logger.debug("count: " + json.get("count"));
                    if (json.has("level") && json.get("level").equals("title")) {
                        if (json.has("heading") && json.has("count")) {
                            String keyTag = json.get("heading").toString();
                            String tagValue = json.get("count").toString();
                            countNameValue.put(keyTag, tagValue);
                            logger.debug("countNameValue: " + countNameValue);
                            if (json.has("children")) {
                                Object chilren = json.get("children");
                                checkSumNameValue.put(keyTag, checkSumSHA256Hash(chilren));
                                logger.debug("checkSumNameValue: " + checkSumNameValue);
                            }
                        }
                    }
                }
            }
        }

        File folder = new File(reportFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        JSONObject reportJson = new JSONObject();
        String reportDate = new Date().toString();
        reportJson.put("Report Date", reportDate);
        reportJson.put("Word(s) Count By Agencies", countNameValue);
        reportJson.put("Checksum By Agencies", checkSumNameValue);

        String reportFileName = reportFolder + File.separator + REPORT_FILE_NAME + ".json";
        reportContent = reportJson.toString();
        File file = new File(reportFileName);
        if (file.exists()) {
            reportContent += "," + extractFileContent(reportFileName).toString();
        }
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(reportFileName, false))) {
            writer.write(reportContent);
            logger.debug(reportFileName + " saved!");
        } catch (IOException e) {
            throw e;
        }

    }

    public StringBuffer getReportContent() throws FileNotFoundException {
        return extractFileContent(reportFolder + File.separator + REPORT_FILE_NAME + ".json");
    }

}
