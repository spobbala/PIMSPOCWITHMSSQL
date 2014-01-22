/*
 * Created on Jan 21, 2014
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PIMSProcess_17_20 {
	private PIMSLogging pimsLogging = null;
	private Connection pimsCon;
	private Properties propFile;
	
	public PIMSProcess_17_20(Connection lclCon, Properties propFile) {
		this.pimsCon = lclCon;
		this.propFile = propFile;
		this.pimsLogging = new PIMSLogging(pimsCon,
				propFile.getProperty("Environment"));
	}
	public void process(int batchid){
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		try
		{
			pstmt = batchid > 0 ? DBConnectionFactory.prepareStatement(
				this.pimsCon, PIMSConstants.QUERYBATCH, batchid,
				PIMSConstants.STATUS_17) : DBConnectionFactory
				.prepareStatement(this.pimsCon,
						PIMSConstants.QUERYSTATUS,
						PIMSConstants.STATUS_17);
		rSet = pstmt.executeQuery();
		SendMailDAO mailProc = new SendMailDAO(pimsCon, propFile);
		while (rSet.next()) {
			this.prepareProductTable_17_20(rSet
					.getInt("BATCH_ID"));
			
			mailProc.generateEmail(rSet.getInt("BATCH_ID"),
					PIMSConstants.PROCESS17_20);
		}
		}catch (SQLException sql) {
			pimsLogging.logMessage(
					batchid,
					null,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"DB Error in 17_20 Process, Error Details:"
							+ sql.getMessage());
		} finally {
			DBConnectionFactory.close(this.pimsCon, pstmt, rSet);
		}
	}
	public void prepareProductTable_17_20(int batchid) {
		Map<String, String> mValues = new HashMap<String, String>();
		Map<String, String> mapID = new HashMap<String, String>();

		String serialNumber = null;

		boolean uStatus = true;

		int cnt = 0;
		int count = 0;
		
		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
				pimsLogging.getPriorityLow(), pimsLogging.getTrackingMsgId(),
				PIMSConstants.MSG_START_17_20);
		try {
			pstmt = DBConnectionFactory.prepareStatement(pimsCon,
					PIMSConstants.QUERYBDET_17_20, batchid);
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				serialNumber = rSet.getString("DHCT_SN");
				mValues = this.getSNAttribs(serialNumber);
				if (mValues.get("SMSN") == null) {
					pimsLogging.logMessage(batchid, serialNumber, null,
							pimsLogging.getSequence(),
							pimsLogging.getPriorityHigh(),
							pimsLogging.getErrMsgId(),
							PIMSConstants.MSG_ERRSMSN_17_20);
					uStatus = false;
				}
				String mfgID = null;
				String tempStr = null;
				String custCAAID = null;
				tempStr = mValues.get("MODEL") + mValues.get("HWVER");
				mfgID = mapID.get(tempStr);
				if (mfgID == null) {
					cnt++;
					mfgID = this.getMfgID(mValues.get("MODEL"), mValues.get("HWVER"));
					mapID.put("HWVER" + cnt, mValues.get("HWVER"));
					if (mfgID == null) {
						mfgID = this.getMfgID(mValues.get("MODEL"),
								mValues.get("MATREVLEVEL"));
						mapID.put("HWVER" + cnt, mValues.get("MATREVLEVEL"));
					}
					mapID.put(tempStr, mfgID);
				}
				custCAAID = mapID.get(rSet.getString("SHIP_TO_CUST_ID"));
				if (custCAAID == null) {
					custCAAID = this.getCustID(rSet
							.getString("SHIP_TO_CUST_ID"));
					mapID.put(rSet.getString("SHIP_TO_CUST_ID"), custCAAID);
				}
				if (custCAAID == null) {
					pimsLogging.logMessage(
							batchid,
							serialNumber,
							null,
							pimsLogging.getSequence(),
							pimsLogging.getPriorityHigh(),
							pimsLogging.getErrMsgId(),
							"No CUSTCAAID setup for SHIP_TO:"
									+ rSet.getString("SHIP_TO_CUST_ID")
									+ " for Serial Number:" + serialNumber
									+ " in Batch ID:" + rSet.getInt("BATCH_ID"));
					uStatus = false;
				}
				if (mfgID == null) {
					pimsLogging.logMessage(
							batchid,
							serialNumber,
							null,
							pimsLogging.getSequence(),
							pimsLogging.getPriorityHigh(),
							pimsLogging.getErrMsgId(),
							"No MFGID setup for model:" + mValues.get("MODEL")
									+ " and HWVER/MATREVLEVEL "
									+ mValues.get("HWVER") + "/"
									+ mValues.get("MATREVLEVEL")
									+ " for Serial Number:" + serialNumber
									+ " in Batch ID:" + rSet.getInt("BATCH_ID"));
					uStatus = false;
				}
				mValues.put("SN", serialNumber);
				mValues.put("MFGID", mfgID);
				mValues.put("CAAID", custCAAID);
				mValues.put("HWVER", mapID.get("HWVER" + cnt));
				if (uStatus) {
					count++;
					this.insertProductTable(batchid, mValues,
							PIMSConstants.INSERTQUERY_17_20);
				}
				mValues.clear();
			}
			DBConnectionFactory.close(pstmt, rSet);
			mapID.clear();
			if (uStatus) {

				pimsLogging.logMessage(batchid, null, null, pimsLogging.getSequence(),
						pimsLogging.getPriorityLow(),
						pimsLogging.getTrackingMsgId(),
						"Total records updated in Product Table:" + count);
				pimsCon.commit();
				pstmt = DBConnectionFactory.prepareStatement(pimsCon,
						PIMSConstants.UPDATEBQUERY, PIMSConstants.STATUS_20,
						batchid);
				pstmt.executeUpdate();
				pimsCon.commit();
			}
		} catch (SQLException sql) {
			pimsLogging.logMessage(
					batchid,
					null,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"DB Error in 17_20 Process, Error Details:"
							+ sql.getMessage());
		} catch (Exception e) {
			pimsLogging.logMessage(
					batchid,
					null,
					null,
					pimsLogging.getSequence(),
					pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Exception in 17_20 Process, Error Details:"
							+ e.getMessage());

		} finally {
			DBConnectionFactory.close(pstmt, rSet);
		}
	}
	public Map<String, String> getSNAttribs(String serialNumber)
			throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rSet1 = null;

		String formatString = null;

		Map<String, String> mValues = new HashMap<String, String>();

		pstmt = DBConnectionFactory.prepareStatement(pimsCon,
				PIMSConstants.QUERYSNGET, serialNumber);
		rSet1 = pstmt.executeQuery();
		while (rSet1.next()) {
			switch (rSet1.getInt("ATTRIBUTE_ID")) {
			case 5: // MFGDATE
				mValues.put("MFGDATE", rSet1.getString("ATTRIBUTE_VALUE"));
				break;
			case 71: // SMSN
				mValues.put("SMSN", rSet1.getString("ATTRIBUTE_VALUE"));
				break;
			case 7: // MACADDR
				if (rSet1.getString("ATTRIBUTE_SEQUENCE").equals("1"))
					mValues.put("MACADDR", rSet1.getString("ATTRIBUTE_VALUE"));
				break;
			case 20: // MODEL
				formatString = rSet1.getString("ATTRIBUTE_VALUE");
				formatString = formatString.replaceAll("[^0-9]", "");
				mValues.put("MODEL", formatString);
				break;
			case 14: // HWVER
				formatString = rSet1.getString("ATTRIBUTE_VALUE");
				formatString = formatString.replaceAll("\\.", "");
				if (formatString.contains("0x"))
					formatString = "01";
				mValues.put("HWVER", formatString);
				break;
			case 16: // MATERIALREVLEVEL
				formatString = rSet1.getString("ATTRIBUTE_VALUE");
				formatString = formatString.replaceAll("\\.", "");
				mValues.put("MATREVLEVEL", formatString);
				break;

			default:
				// do nothing
				break;
			}
		}
		DBConnectionFactory.close(pstmt, rSet1);
		return mValues;
	}
	public String getMfgID(String model, String hwverOrMatRev)
			throws SQLException {

		String mfgID = null;

		PreparedStatement pstmt = null;
		ResultSet rSet = null;
		pstmt = DBConnectionFactory.prepareStatement(pimsCon,
				PIMSConstants.QUERYMFGID, model, hwverOrMatRev);
		rSet = pstmt.executeQuery();
		while (rSet.next())
			mfgID = rSet.getString(1);

		DBConnectionFactory.close(pstmt, rSet);
		return mfgID;
	}
	public String getCustID(String shipToID) throws SQLException {
		String custID = null;

		PreparedStatement pstmt = null;
		ResultSet rSet = null;

		pstmt = DBConnectionFactory.prepareStatement(pimsCon,
				PIMSConstants.QUERYCAAGET, shipToID);
		rSet = pstmt.executeQuery();
		while (rSet.next())
			custID = rSet.getString(1);
		DBConnectionFactory.close(pstmt, rSet);
		return custID;
	}

	public void insertProductTable(int batchid, Map<String, String> mValues,
			String insertQuery) {
		PreparedStatement updateTable = null;
		try {
			updateTable = pimsCon.prepareStatement(insertQuery);
			updateTable.setString(1, mValues.get("SN")); // DHCT_SN
			updateTable.setString(2, mValues.get("SMSN"));
			updateTable.setString(3, mValues.get("MACADDR"));
			updateTable.setString(4, mValues.get("MODEL"));
			updateTable.setString(6, mValues.get("MFGDATE"));
			updateTable.setString(5, mValues.get("HWVER")); // HW REV
			updateTable.setString(7, mValues.get("CAAID"));
			updateTable.setString(8, mValues.get("MFGID"));
			updateTable.executeUpdate();
			mValues.clear();
		} catch (SQLException sql) {
			pimsLogging.logMessage(batchid, mValues.get("SN"), null,
					pimsLogging.getSequence(), pimsLogging.getPriorityHigh(),
					pimsLogging.getErrMsgId(),
					"Error while updating Nothing Blob for the batch id:"
							+ batchid + ", Error Details:" + sql.getMessage());
		} finally {
			DBConnectionFactory.close(updateTable);
		}
	}
}
