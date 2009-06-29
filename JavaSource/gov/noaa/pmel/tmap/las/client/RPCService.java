package gov.noaa.pmel.tmap.las.client;


import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OperationSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.OptionSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import com.google.gwt.user.client.rpc.RemoteService;

public interface RPCService extends RemoteService {
	/**
	 * 
	 */
	public CategorySerializable[] getCategories(String id) throws RPCException;
	public VariableSerializable[] getVariables(String id) throws RPCException;
	public GridSerializable getGrid(String dsID, String varID) throws RPCException;
	public OperationSerializable[] getOperations(String view, String dsID, String varID) throws RPCException;
	public OptionSerializable[] getOptions(String opid) throws RPCException;
	public OptionSerializable[] getOptionsByOperationID(String operationid) throws RPCException;
	public CategorySerializable[] getTimeSeries() throws RPCException;
}
