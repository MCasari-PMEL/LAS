/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.ContainerComparator;
import gov.noaa.pmel.tmap.las.util.NameValuePair;
import gov.noaa.pmel.tmap.las.util.Operation;
import gov.noaa.pmel.tmap.las.util.Option;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/** 
 * MyEclipse Struts
 * Creation date: 03-13-2007
 * 
 * XDoclet definition:
 * @struts.action validate="true"
 */
public class getOperations extends Action {
    private static Logger log = LogManager.getLogger(getOperations.class.getName());
    /*
     * Generated Methods
     */

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);
        String dsID = request.getParameter("dsid");
        String varID = request.getParameter("varid");
        String view = request.getParameter("view");
        String format = request.getParameter("format");
        log.info("Finishing: getOperations.do?dsid="+dsID+" and varid="+varID+".");
        String grid_type = "";
        if ( format == null ) {
            format = "json";
        }
        log.info("Starting: getOperations?dsid="+dsID+"&varid="+varID+"&view="+view+".");
        try {
            grid_type = lasConfig.getGridType(dsID, varID);
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String ui_default = "";
        try {
            ui_default = lasConfig.getVariablePropertyValue("/lasdata/datasets/dataset[@ID='"+dsID+"']/variables/variable[@ID='"+varID+"']","ui", "default");
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ArrayList<Operation> operations = new ArrayList<Operation>();
        try {
            if ( ui_default != null && !ui_default.equals("") ) {
                ui_default = ui_default.substring(ui_default.indexOf("#")+1, ui_default.length());
                operations = lasConfig.getOperationsByDefault(view, ui_default);
            } else {
                operations = lasConfig.getOperationsByIntervalAndGridType(view, grid_type);
            }
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Collections.sort(operations, new ContainerComparator("order", "name"));
        try {
            PrintWriter respout = response.getWriter();

            if ( format.equals("xml") ) {
                response.setContentType("application/xml");
                respout.print(Util.toXML(operations, "operations"));
            } else {
                response.setContentType("application/json");
                JSONObject json_response = toJSON(operations, "operations");
                log.debug(json_response.toString(3));
                json_response.write(respout);      
            }

        } catch (JSONException e) {
            // TODO fix
            e.printStackTrace();
        } catch (IOException e) {
            // TODO fix
            e.printStackTrace();
        }
        log.info("Finished: getOperations.do?dsid="+dsID+"&varid="+varID+"&view="+view+".");
        return null;
    }
    private JSONObject toJSON(ArrayList<Operation> operations, String string) throws JSONException {
        JSONObject json_response = new JSONObject();
        JSONObject operations_object = new JSONObject();
        for (Iterator operIt = operations.iterator(); operIt.hasNext();) {
            Operation op = (Operation) operIt.next();
            JSONObject operation = op.toJSON();        
            operations_object.array_accumulate("operation", operation);
        }
        json_response.put("operations", operations_object);
        return json_response;
    }
}