package gov.usds.service;

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

import gov.usds.app.WebApplication;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = WebApplication.class)
public class UsdsServiceTest {

    private MockMvc mockMvc;

    @Mock
    private UsdsService usdsService; // Mock the dependency
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        mockMvc = MockMvcBuilders.standaloneSetup(usdsService).build(); // Setup MockMvc
    }

    @Test
    public void testGetFileContent_Success() throws Exception {

    	String filePath = "filePath";
    	String fileContent = "fileContent";
    	
    	when(usdsService.getFileContent(filePath)).thenReturn(fileContent);
    	
    	fileContent = usdsService.getFileContent(filePath);
    	assertTrue(fileContent.equals("fileContent"));
    }

    @Test
    public void testSaveToFile_Exception() throws IOException {

        doThrow(new IOException()).when(usdsService).saveToFile(null);
        try {
        	usdsService.saveToFile(null);
        } catch (IOException ioe) {
        	// ignore 
        }
    }

}
