package gov.noaa.pmel.tmap.las.service.shape;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASBackendRequest;
import gov.noaa.pmel.tmap.las.jdom.LASBackendResponse;
import gov.noaa.pmel.tmap.las.service.BackendService;

public class ShapeFileService extends BackendService {
	 private static Logger log = LogManager.getLogger(ShapeFileService.class.getName());
	 public String getProduct(String backendRequestXML, String cacheFileName) throws Exception {       
	        LASBackendRequest lasBackendRequest = new LASBackendRequest();      
	        JDOMUtils.XML2JDOM(backendRequestXML, lasBackendRequest);
	        
	        String debug = lasBackendRequest.getProperty("las", "debug");
	        
	        setLogLevel(debug);
	        
	        // Report logging level only for "debug" levels.
	        log.debug("Logging set to " + log.getEffectiveLevel().toString()+ " for "+log.getName());
	        
	        LASBackendResponse lasBackendResponse = new LASBackendResponse();    
	        if ( lasBackendRequest.isCancelRequest()) {           
	            lasBackendResponse.setError("ShapeFile backend request canceled.");
	            log.info("ShapeFile backend request canceled: "+lasBackendRequest.toCompactString());
	            return lasBackendResponse.toString();
	        }
	        ShapeFileTool shapeTool = new ShapeFileTool();
	        lasBackendResponse = shapeTool.run(lasBackendRequest);
	        if ( lasBackendResponse.getError() != null && !lasBackendResponse.getError().equals("") ) {
	          log.info("ShapeFile backend request failed: "+lasBackendResponse.getError());
	        } 
	        return lasBackendResponse.toString();
	    }
}
