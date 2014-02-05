/*
 * Created on Jan 14, 2014
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class PIMSLogging {
	//Configuration Variables
	private int errMsgId;
	private String errMsgType;
	private int SuccessMsgId;
	private String successMsgType;
	private int trackingMsgId;
	private String trackingMsgType;
	private int warningMsgId;
	private String warningMsgType;
	private String priorityCritical;
	private String priorityHigh;
	private String priorityMedium;
	private String priorityLow;
	private String iEMMSuccess;
	private String iEMMFailure;
	private int sequence;
	private Connection dbCon;
	private int notifID;
	private Set<Integer> nIDs = new LinkedHashSet<Integer>();
	//Getters and Setters Methods for Configuration Variables
	
	public void addNotificationID(int id){
		nIDs.add(id);
	}
	public Set<Integer> getNotificationIDSet(){
		return nIDs;
	}

	public void setNotificationID(int id){
		this.notifID = id;		
	}
	public int getNotificationID(){
		return notifID;
	}
	public int getErrMsgId() {
		return errMsgId;
	}

	public void setErrMsgId(int errMsgId) {
		this.errMsgId = errMsgId;
	}

	public String getErrMsgType() {
		return errMsgType;
	}

	public void setErrMsgType(String errMsgType) {
		this.errMsgType = errMsgType;
	}

		public int getSuccessMsgId() {
		return SuccessMsgId;
	}

	public void setSuccessMsgId(int successMsgId) {
		SuccessMsgId = successMsgId;
	}

	public String getSuccessMsgType() {
		return successMsgType;
	}

	public void setSuccessMsgType(String successMsgType) {
		this.successMsgType = successMsgType;
	}

	public int getTrackingMsgId() {
		return trackingMsgId;
	}

	public void setTrackingMsgId(int trackingMsgId) {
		this.trackingMsgId = trackingMsgId;
	}

	public String getTrackingMsgType() {
		return trackingMsgType;
	}

	public void setTrackingMsgType(String trackingMsgType) {
		this.trackingMsgType = trackingMsgType;
	}

	public int getWarningMsgId() {
		return warningMsgId;
	}

	public void setWarningMsgId(int warningMsgId) {
		this.warningMsgId = warningMsgId;
	}
	
	public String getWarningMsgType() {
		return warningMsgType;
	}

	public void setWarningMsgType(String warningMsgType) {
		this.warningMsgType = warningMsgType;
	}

	public String getPriorityCritical() {
		return priorityCritical;
	}

	public void setPriorityCritical(String priorityCritical) {
		this.priorityCritical = priorityCritical;
	}

	public String getPriorityHigh() {
		return priorityHigh;
	}

	public void setPriorityHigh(String priorityHigh) {
		this.priorityHigh = priorityHigh;
	}

	public String getPriorityMedium() {
		return priorityMedium;
	}

	public void setPriorityMedium(String priorityMedium) {
		this.priorityMedium = priorityMedium;
	}

	public String getPriorityLow() {
		return priorityLow;
	}

	public void setPriorityLow(String priorityLow) {
		this.priorityLow = priorityLow;
	}

	public String getiEMMSuccess() {
		return iEMMSuccess;
	}

	public void setiEMMSuccess(String iEMMSuccess) {
		this.iEMMSuccess = iEMMSuccess;
	}

	public String getiEMMFailure() {
		return iEMMFailure;
	}

	public void setiEMMFailure(String iEMMFailure) {
		this.iEMMFailure = iEMMFailure;
	}
	
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	//Initialize all Configuration Variables from Database
	public PIMSLogging(Connection con, String environment){
		this.dbCon = con;
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		try {
				pstmt = DBConnectionFactory.prepareStatement(con,
						PIMSConstants.INITGLOBAL);
				rSet = pstmt.executeQuery();
				while (rSet.next()) {
					if (rSet.getString(2).equals(PIMSConstants.configErrMsgId)) {
						this.setErrMsgId(Integer.parseInt(rSet.getString(3)));
					} else if (rSet.getString(2).equals(PIMSConstants.configErrMsg)) {
						this.setErrMsgType(rSet.getString(3));
					} else if (rSet.getString(2).equals(PIMSConstants.configSuccessMsgId)) {
						this.setSuccessMsgId(Integer.parseInt(rSet.getString(3)));
					} else if (rSet.getString(2).equals(PIMSConstants.configSuccessMsg)) {
						this.setSuccessMsgType(rSet.getString(3));
					} else if (rSet.getString(2).equals(PIMSConstants.configTrackingMsgId)) {
						this.setTrackingMsgId(Integer.parseInt(rSet.getString(3)));
					} else if (rSet.getString(2).equals(PIMSConstants.configTrackingMsg)) {
						this.setTrackingMsgType(rSet.getString(3));
					} else if (rSet.getString(2).equals(PIMSConstants.configWarningMsgId)) {
						this.setWarningMsgId(Integer.parseInt(rSet.getString(3)));
					} else if (rSet.getString(2).equals(PIMSConstants.configWarningMsg)) {
						this.setWarningMsgType(rSet.getString(3));
					} else if (rSet.getString(2).equals(PIMSConstants.configPriorityC)) {
						this.setPriorityCritical(rSet.getString(3));
					} else if (rSet.getString(2).equals(PIMSConstants.configPriorityH)) {
						this.setPriorityHigh(rSet.getString(3));
					} else if (rSet.getString(2).equals(PIMSConstants.configPriorityM)) {
						this.setPriorityMedium(rSet.getString(3));
					} else if (rSet.getString(2).equals(PIMSConstants.configPriorityL)) {
						this.setPriorityLow(rSet.getString(3));
					} else if (rSet.getString(2).equals(PIMSConstants.configEmmSuccess) && environment.equals(PIMSConstants.envTest) && rSet.getInt(1) == PIMSConstants.testSeq) {
						this.setiEMMSuccess(rSet.getString(3));
						this.setSequence(PIMSConstants.testSeq);
					} else if (rSet.getString(2).equals(PIMSConstants.configEmmSuccess) && environment.equals(PIMSConstants.envProd) && rSet.getInt(1) == PIMSConstants.prodSeq) {
						this.setiEMMSuccess(rSet.getString(3));
						this.setSequence(PIMSConstants.prodSeq);
					} else if (rSet.getString(2).equals(PIMSConstants.configEmmFailure) && environment.equals(PIMSConstants.envTest) && rSet.getInt(1) == PIMSConstants.testSeq) {
						this.setiEMMFailure(rSet.getString(3));
						this.setSequence(PIMSConstants.testSeq);
					} else if (rSet.getString(2).equals(PIMSConstants.configEmmFailure) && environment.equals(PIMSConstants.envProd) && rSet.getInt(1) == PIMSConstants.prodSeq) {
						this.setiEMMFailure(rSet.getString(3));
						this.setSequence(PIMSConstants.prodSeq);
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBConnectionFactory.close(pstmt, rSet);
			}
	}
	public void logSuccessMessage(int iBatchId, String dhctSN,
			String iMessageDetails){
		this.logMessage(iBatchId, dhctSN, null, this.getSequence(),
				this.getPriorityLow(), this.getSuccessMsgId(),
				iMessageDetails);
	}
	public void logTrackingMessage(int iBatchId, String dhctSN,
			String iMessageDetails){
		this.logMessage(iBatchId, dhctSN, null, this.getSequence(),
				this.getPriorityLow(), this.getTrackingMsgId(),
				iMessageDetails);
	}

	public void logErrorMessage(int iBatchId, String dhctSN, 
			String iMessageDetails){
		this.logMessage(iBatchId, dhctSN, null, this.getSequence(),
				this.getPriorityHigh(), this.getErrMsgId(),
				iMessageDetails);
	}

	public void logMessage(int iBatchId, String iDhctSn, String iItemNo,
			int iSeqNo, String iPriority, Integer iMessageId,
			String iMessageDetails) {
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		String messageType;
		String errorDump;
		int messageId;
		if (iMessageId == null) {
			messageId = 0;
		} else {
			messageId = iMessageId;
		}
		if (messageId == getErrMsgId()) {
			messageType = getErrMsgType();
		} else if (messageId == getSuccessMsgId()) {
			messageType = getSuccessMsgType();
		} else if (messageId == getTrackingMsgId()) {
			messageType = getTrackingMsgType();
		} else if (messageId == getWarningMsgId()) {
			messageType = getWarningMsgType();
		} else {
			messageType = getErrMsgType();
		}
		// Get Environment Details of DB and its Connection
		try {
			errorDump = (messageId == getErrMsgId()) ? getErrorReport(iBatchId) : null;
			pstmt = DBConnectionFactory.prepareStatement(dbCon,true,
					PIMSConstants.LOGUPDATEQUERY, iBatchId, iDhctSn, iItemNo,
					iSeqNo, messageType, messageId, iMessageDetails, errorDump);
			pstmt.executeUpdate();
			dbCon.commit();
			rSet = pstmt.getGeneratedKeys();
			while(rSet.next()){
			    this.setNotificationID(rSet.getInt(1));
			    this.addNotificationID(rSet.getInt(1));
			}
		} catch (SQLException e) {
			System.out.println("Error while Updating Log message");
		} finally {
			DBConnectionFactory.close(pstmt,rSet);
		}
	}
	private String getErrorReport(int batchid) {
		String errMsg = null;
		String serverName = null;
		String dbInstance = null;
		int sepPos;
		PreparedStatement pstmt = null;
		ResultSet rSet = null;

		try {
			// Get Environment Details of DB and its Connection
			pstmt = DBConnectionFactory.prepareStatement(dbCon,
					PIMSConstants.LOGINITQUERY);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				serverName = rSet.getString(1);
				sepPos = serverName.indexOf("\\");
				dbInstance = serverName.substring(sepPos + 1,
						serverName.length());
				serverName = serverName.substring(0, sepPos);
				errMsg = PIMSConstants.COM_ERR_RPT
						+ "' + CHAR(13)+CHAR(10) + CHAR(13)+CHAR(10) + 'DATABASE DETAILS' + CHAR(13)+CHAR(10) + 'Host: "
						+ serverName
						+ "' + CHAR(13)+CHAR(10) + 'Instance: "
						+ dbInstance
						+ "' + CHAR(13)+CHAR(10) + 'DB Name: "
						+ rSet.getString(2)
						+ "' + CHAR(13)+CHAR(10) + 'Schema Name: "
						+ rSet.getString(3)
						+ "' + CHAR(13)+CHAR(10) + CHAR(13)+CHAR(10) + "
						+ "'CONNECTION DETAILS' + CHAR(13)+CHAR(10) + 'Session Id: "
						+ rSet.getString(4)
						+ "' + CHAR(13)+CHAR(10) + 'Session User: "
						+ rSet.getString(5)
						+ "' + CHAR(13)+CHAR(10) + 'Terminal: "
						+ rSet.getString(6)
						+ "' + CHAR(13)+CHAR(10) + 'Program: "
						+ rSet.getString(7);
			}
		} catch (SQLException sql) {
			this.logMessage(batchid, null, null, getSequence(),
					getPriorityHigh(), getErrMsgId(),
					"Error in 30_40 Process while upload blobs, Error Details:"
							+ sql.getMessage());
		} finally {
			DBConnectionFactory.close(pstmt);
		}
		return errMsg;
	}
	public void clearNotificationID() {
		nIDs.clear();
	}
	
	}
