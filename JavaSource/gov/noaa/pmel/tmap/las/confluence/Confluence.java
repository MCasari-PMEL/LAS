package gov.noaa.pmel.tmap.las.confluence;

import gov.noaa.pmel.tmap.las.jdom.JDOMUtils;
import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.jdom.LASUIRequest;
import gov.noaa.pmel.tmap.las.product.server.LASAction;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.ui.GetMetadata;
import gov.noaa.pmel.tmap.las.ui.LASProxy;
import gov.noaa.pmel.tmap.las.ui.Util;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Constants;
import gov.noaa.pmel.tmap.las.util.Tributary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.JDOMException;
import org.json.JSONException;

public class Confluence extends LASAction {

	private static Logger log = LogManager.getLogger(Confluence.class.getName());

	private static final LASProxy lasProxy = new LASProxy();

	private String request_format;

	public static final HashMap<String, String[]> ajaxCalls =   
		new HashMap<String, String[]>()   
		{  
			//Unnamed Block.  
			{  
				put(Constants.GET_CATEGORIES, new String[]{"catid"});  
				put(Constants.GET_DATACONSTRAINTS, new String[]{"dsid", "varid"});  
				put(Constants.GET_GRID,  new String[]{"dsid", "varid"});  
				put(Constants.GET_METADATA, new String[]{"dsid", "catitem", "opendap"});  
				put(Constants.GET_OPERATIONS, new String[]{"dsid", "varid", "view"}); 
				put(Constants.GET_OPTIONS, new String[]{"dsid", "opid"});
				put(Constants.GET_REGIONS, new String[]{"dsid", "varid"});
				put(Constants.GET_VARIABLE, new String[]{"dsid", "varid"});
				put(Constants.GET_VIEWS, new String[]{"dsid", "varid"});
			}  
		};
		
		public static final HashMap<String, String> services =   
			new HashMap<String, String>()   
			{  
				//Unnamed Block.  
				{  
					put(Constants.GET_CATEGORIES_KEY, Constants.GET_CATEGORIES);  
					put(Constants.GET_DATACONSTRAINTS_KEY, Constants.GET_DATACONSTRAINTS);  
					put(Constants.GET_GRID_KEY, Constants.GET_GRID);  
					put(Constants.GET_METADATA_KEY, Constants.GET_METADATA);  
					put(Constants.GET_OPERATIONS_KEY, Constants.GET_OPERATIONS); 
					put(Constants.GET_OPTIONS_KEY, Constants.GET_OPTIONS);
					put(Constants.GET_REGIONS_KEY, Constants.GET_REGIONS);
					put(Constants.GET_VARIABLES_KEY, Constants.GET_VARIABLE);
					put(Constants.GET_VIEWS_KEY, Constants.GET_VIEWS);
				}  
			};
		public ActionForward execute(ActionMapping mapping,
				ActionForm form,
				HttpServletRequest request,
				HttpServletResponse response){

			Cookie[] cookies = request.getCookies();
			String openid = null;
			for (int i = 0; i < cookies.length; i++) {
				if ( cookies[i].getName().equals("esg.openid.identity.cookie") ) {
					openid = cookies[i].getValue();
				}
			}
			LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY);

			ArrayList<Tributary> servers = new ArrayList<Tributary>();

			servers = lasConfig.getTributaries();

			request_format = "";
			// Process the request...
			String url = request.getRequestURL().toString();


			if ( url.contains(Constants.GET_CATEGORIES) ) {
				ArrayList<Category> categories = new  ArrayList<Category>();
				if ( request.getParameter("catid") ==  null) {
					// The only other possible parameter is the format.
					if ( request.getParameter("format") != null ) {
						// Save it.  When use it when we stream the response...
						request_format = request.getParameter("format");
					}
					try {
						// Start with the categories on this server...
						Category local_cat = new Category(lasConfig.getTitle(), lasConfig.getTopLevelCategoryID()); 
						categories.add(local_cat);

						for (Iterator servIt = servers.iterator(); servIt.hasNext();) {
							Tributary trib = (Tributary) servIt.next();
                            Category server_cat = new Category(trib.getName(), trib.getTopLevelCategoryID());
							categories.add(server_cat);
						}
						InputStream is = new ByteArrayInputStream(Util.toJSON(categories, "categories").toString().getBytes("UTF-8"));
						lasProxy.stream(is, response.getOutputStream());
					} catch (JSONException e) {
						log.error("Unable to get categories", e);
					} catch (UnsupportedEncodingException e) {
						log.error("Unable to get categories", e);
					} catch (IOException e) {
						log.error("Unable to get categories", e);
					} catch (JDOMException e) {
						log.error("Unable to get categories", e);
					}

				} else {
					String catid = request.getParameter("catid");
					String format = request.getParameter("format");
					if ( format == null ) format = "json";
					String server_key = catid.split(Constants.NAME_SPACE_SPARATOR)[0];
					try {
						if ( server_key.equals(lasConfig.getBaseServerURLKey()) ) {
							
							return mapping.findForward(Constants.GET_CATEGORIES_KEY);

						} else {
							
							Tributary trib = lasConfig.getTributary(server_key);
							String las_url = trib.getURL() + Constants.GET_CATEGORIES;					

							if ( !catid.endsWith(Constants.NAME_SPACE_SPARATOR+JDOMUtils.MD5Encode(trib.getName()))) {
								// If this is a request of the top level category for this server
								// the id must be left off the request.  In this case it's not so
								// add it on.
								las_url = las_url + "?" + request.getQueryString();
							}
							lasProxy.executeGetMethodAndStreamResult(las_url, response);
						}
					} catch (HttpException e) {
						log.error("Unable to fetch categories.", e);
					} catch (IOException e) {
						log.error("Unable to fetch categories.", e);
					} catch (JDOMException e) {
						log.error("Unable to fetch categories.", e);
					} 				
				}
			} else if (url.contains(Constants.GET_OPTIONS)) {
				try {
					// I think there is a bug in the client where it requests the options without the server key initially
					// so I'm going to throw that request away.

					String opid = request.getParameter("opid");
					if ( opid.contains(Constants.NAME_SPACE_SPARATOR) ) {
						String[] parts = opid.split(Constants.NAME_SPACE_SPARATOR);
						String server_key = parts[0];
						if ( server_key.equals(lasConfig.getBaseServerURLKey()) ) {
							return mapping.findForward(Constants.GET_OPTIONS_KEY);
						}

						opid = parts[1];

						Tributary trib = lasConfig.getTributary(server_key);
						String las_url = trib.getURL() + Constants.GET_OPTIONS + "?opid=" + opid;
						lasProxy.executeGetMethodAndStreamResult(las_url, response);
					} else {
						// Send it to the local server which will work most of the time..
						return mapping.findForward(Constants.GET_OPTIONS_KEY);
					}
				} catch (HttpException e) {
					log.error("Unable to fetch options.", e);
				} catch (IOException e) {
					log.error("Unable to fetch options.", e);
				} catch (JDOMException e) {
					log.error("Unable to fetch options.", e);
				}
			} else if (url.contains(Constants.PRODUCT_SERVER)) {
				String xml = request.getParameter("xml");
				LASUIRequest ui_request = new LASUIRequest();
				try {
					JDOMUtils.XML2JDOM(xml, ui_request);
				} catch (IOException e) {
					logerror(request, "Failed to parse XML request.", e);
					return mapping.findForward("error");					
				} catch (JDOMException e) {
					logerror(request, "Failed to parse XML request.", e);
					return mapping.findForward("error");
				}
				ArrayList<String> ids = ui_request.getDatasetIDs();
				if ( ids.size() == 0 ) {
					// Forward to the local server...
					return mapping.findForward("LocalProductServer");
				} else if ( ids.size() > 1 ) {
					// Check to see how many servers are needed for this product.
					
					HashMap<String, Tributary> tribs = new HashMap<String, Tributary>();
					for (Iterator idsIt = ids.iterator(); idsIt.hasNext();) {
						String id = (String) idsIt.next();
						String server_key = id.split(Constants.NAME_SPACE_SPARATOR)[0];
						Tributary trib = lasConfig.getTributary(server_key);
						tribs.put(server_key, trib);
					}
					try {
						if ( tribs.size() == 1 ) {
							String key = (String) tribs.keySet().toArray()[0];
							// Multiple variables, but one server so send to the appropriate server.
							if ( key.equals(lasConfig.getBaseServerURLKey()) ) {
								// Process here as normal...
								return mapping.findForward(Constants.LOCAL_PRODUCT_SERVER_KEY);
							} else {
								String las_url = tribs.get(key).getURL();
								las_url = las_url + Constants.PRODUCT_SERVER + "?" + request.getQueryString();	
								lasProxy.executeGetMethodAndStreamResult(las_url, response);
//								response.sendRedirect(las_url);
							}
						} else {
							// Add the special parameter to create product locally using remote analysis and send to local product server.
							return new ActionForward(Constants.LOCAL_PRODUCT_SERVER+"?remote_las=true&xml="+xml);
						}
					} catch (JDOMException e) {
						logerror(request, "Unable to fetch product.", e);
					} catch (UnsupportedEncodingException e) {
						logerror(request, "Unable to fetch product.", e);
					} catch (HttpException e) {
						logerror(request, "Unable to fetch product.", e);
					} catch (IOException e) {
						logerror(request, "Unable to fetch product.", e);
					}
				} else {
					try {
						// Get the server key and forward to that server...
						String server_key = ids.get(0).split(Constants.NAME_SPACE_SPARATOR)[0];
						if ( server_key.equals(lasConfig.getBaseServerURLKey()) ) {
							return mapping.findForward("LocalProductServer");
						}
						Tributary trib = lasConfig.getTributary(server_key);
						String las_url = trib.getURL();
						las_url = las_url + Constants.PRODUCT_SERVER + "?" + request.getQueryString();	
//						response.sendRedirect(las_url);
						lasProxy.executeGetMethodAndStreamResult(las_url, response);
					} catch (HttpException e) {
						logerror(request, "Unable to fetch product.", e);
					} catch (IOException e) {
						logerror(request, "Unable to fetch product.", e);
					} catch (JDOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				String dsid = request.getParameter("dsid");
				// All xpaths will come from the same data set for now.  :-)
				// TODO cross-server data set comparisons
				if ( dsid == null ) {
					String[] xpaths = request.getParameterValues("xpath");
					dsid = LASConfig.getDSIDfromXPath(xpaths[0]);
				}
				String server_key = dsid.split(Constants.NAME_SPACE_SPARATOR)[0];
				try {
					boolean local = server_key.equals(lasConfig.getBaseServerURLKey());
					String las_url = null;
					if ( !local ) {
						Tributary trib = lasConfig.getTributary(server_key);
						las_url = trib.getURL();
					}
					if ( url.contains(Constants.GET_DATACONSTRAINTS) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_DATACONSTRAINTS_KEY);
						} else {
						    las_url = las_url + Constants.GET_DATACONSTRAINTS + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_GRID) ) {
						if ( local ) {	
							return mapping.findForward(Constants.GET_GRID_KEY);
						} else {
						    las_url = las_url + Constants.GET_GRID + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_METADATA) ) {
						if ( local ) {
							return new ActionForward("/LocalProductServer.do?xml="+GetMetadata.prepareURL(request, response));
						} else {		
							las_url = las_url + Constants.GET_METADATA + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_OPERATIONS) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_OPERATIONS_KEY);
						} else {
							las_url = las_url + Constants.GET_OPERATIONS + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_REGIONS) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_REGIONS_KEY);
						} else {
						    las_url = las_url + Constants.GET_REGIONS + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_VARIABLES) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_VARIABLES_KEY);
						} else {
						   las_url = las_url + Constants.GET_VARIABLES + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_VARIABLE) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_VARIABLE_KEY);
						} else {
						   las_url = las_url + Constants.GET_VARIABLE + "?" + request.getQueryString();
						}
					} else if ( url.contains(Constants.GET_VIEWS) ) {
						if ( local ) {
							return mapping.findForward(Constants.GET_VIEWS_KEY);
						} else {
						    las_url = las_url + Constants.GET_VIEWS + "?" + request.getQueryString();
						}
					}
					if ( !local ) {
					    lasProxy.executeGetMethodAndStreamResult(las_url, response);
					}					
				} catch (HttpException e) {
					logerror(request, "Unable to do local request.", e);
				} catch (IOException e) {
					logerror(request, "Unable to do local request.", e);
				} catch (JDOMException e) {
					logerror(request, "Unable to do local request.", e);
				}
			}
			return null;
		}
		
}