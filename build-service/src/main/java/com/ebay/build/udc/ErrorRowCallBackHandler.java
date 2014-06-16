package com.ebay.build.udc;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.ebay.build.profiler.filter.ErrorClassifier;
import com.ebay.build.profiler.filter.model.Filter;

public abstract class ErrorRowCallBackHandler implements RowCallbackHandler {
	private static int batchSize = 1000;
	private ErrorClassifier errorClassifier;
	private List<UsageDataInfo> lsNeedUpdated= new ArrayList<UsageDataInfo>();
	public int updateAmount = 0;
	public ErrorRowCallBackHandler(ErrorClassifier errorClassifier){
		this.errorClassifier = errorClassifier;
	}
	
	public void setBatchSize(int size){
		batchSize = size;
	}
	
	@Override
	public void processRow(ResultSet rs) throws SQLException {
		if(errorClassifier == null)
			return;
		UsageDataInfo record = new UsageDataInfo();  
        record.setId(rs.getLong("id"));
        record.setKind(rs.getString("kind"));
        record.setWhat(rs.getString("what"));
        Clob clobException = rs.getClob("exception");
        String strException;
		try {
			strException = clobToString(clobException);
		} catch (Exception e) {
			e.printStackTrace();
			strException = "Unknown";
		}
        record.setException(strException);
        
        Filter filter = errorClassifier.doClassify(record.getWhat(), record.getException());
        if(filter != null){
			record.setCategory(filter.getCategory());
			record.setErrorCode(filter.getName());
			lsNeedUpdated.add(record);
		}
	    if(lsNeedUpdated.size()==batchSize){
        	updateRecords();
        	lsNeedUpdated.clear();
        }
	}
	/**
	 *  parse clob 
	 */
	private String clobToString(Clob clob) throws SQLException, IOException{
        BufferedReader br = new BufferedReader(clob.getCharacterStream());
        String s = br.readLine();
        StringBuffer sb = new StringBuffer();
        while (s != null) {
            sb.append(s);
            s = br.readLine();
        }
        br.close();
        return sb.toString();
	}
	
	private void updateRecords(){
		if(lsNeedUpdated == null || lsNeedUpdated.size() == 0)
			return;
		updateAmount += lsNeedUpdated.size();
		updateRecordsToDB(this.lsNeedUpdated);
	}
	
	protected abstract int updateRecordsToDB(List<UsageDataInfo> ls);
	
	public void flush(){
		updateRecords();
	}
}