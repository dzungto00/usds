package gov.usds.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import gov.usds.app.ConfigBean;
import gov.usds.app.WebApplication;
import gov.usds.service.UsdsService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = WebApplication.class)
public class UsdsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsdsService usdsService; // Mock the dependency
    
    @Mock
    private ConfigBean configBean;

    @InjectMocks
    private UsdsController usdsController; // Inject mocks into the controller

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        mockMvc = MockMvcBuilders.standaloneSetup(usdsController).build(); // Setup MockMvc
    }
    
    @Test
    public void testInitialize_NoContent() throws Exception {
    	String jsonContent = "jsonContent";
    	
    	when(configBean.getConfigContent(anyString())).thenReturn(jsonContent);
    	jsonContent = configBean.getConfigContent(anyString());
    	
    	doNothing().when(usdsService).saveToFile(anyString());
    	usdsService.saveToFile(anyString());
    	
    	when(configBean.getConfigContent(anyString())).thenReturn("");
    	configBean.getConfigContent(anyString());
    	
        mockMvc.perform(get("/api/v2/initialize")
                .accept(MediaType.TEXT_PLAIN_VALUE))
        		.andExpect(status().isNoContent());
    }
    
    @Test
    public void testSaveToFile_Success() throws Exception {

    	String fileContent = "fileContent";

    	when(usdsService.getFileContent(anyString())).thenReturn(fileContent);

        mockMvc.perform(get("/api/v2/loadLocalFile/localFileName")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(fileContent));
    }
    
    @Test
    public void testSaveToFile_NoContent() throws Exception {

        mockMvc.perform(get("/api/v2/loadLocalFile/localFileName")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void testStatisticalReport() throws Exception {

        StringBuffer reportContent = new StringBuffer();
        when(usdsService.getReportContent()).thenReturn(reportContent);

        mockMvc.perform(get("/api/v2/report/statisticReport")
                .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

 
}
