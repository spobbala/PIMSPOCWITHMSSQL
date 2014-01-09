/*
 * Created on Nov 21, 2013
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
import com.microsoft.sqlserver.jdbc.SQLServerException;

public class DBConnectionDAO {
	private Connection con = null;
	public boolean errorStatus = false;
	public String errorMessage = "";

	public void connect(String dbName, String server, String uName,
			String passWord) {
		try {
			con = DriverManager.getConnection(server, uName, passWord);
			System.out.println("Connected to database:" + server);
		} catch (SQLException e) {
			this.setStatus(e.getMessage(), true);
			e.printStackTrace();
		}
	}
	public void close() {
		try {
			if (con != null)
				con.close();
		} catch (SQLException e) {
			this.setStatus(e.getMessage(), true);
		}
	}
	public void insertData(String insertQuery, ResultSet rSet) {
		try {
			PreparedStatement updateTable = con.prepareStatement(insertQuery);
			ResultSetMetaData rsMetaData = rSet.getMetaData();
			Blob blobData = null;
			int blobLength = 0;
			byte[] blobAsBytes = null;
			int count = 0;
			while (rSet.next()) {
				count++;
				// String uuid = UUID.randomUUID().toString();
				// updateTable.setString(1, uuid);
				// updateTable.setString(2, rSet.getString(1));
				// updateTable.setString(3, rSet.getString(2));
				// updateTable.setInt(4, rSet.getInt(3));
				// updateTable.setInt(5, rSet.getInt(4));
				// updateTable.setInt(6, rSet.getInt(5));
				// updateTable.setString(7, rSet.getString(6));
				// updateTable.setString(8, rSet.getString(7));
				// String dateTime = rSet.getString(8) + rSet.getString(9);
				// Date theDate = (Date) new SimpleDateFormat("yyyyMMddhhmmss",
				// Locale.ENGLISH).parse(dateTime);
				// java.sql.Timestamp sqlDate = new
				// Timestamp(theDate.getTime());
				// updateTable.setTimestamp(9, sqlDate);

				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					// System.out.println(+i +"->" +rsMetaData.getColumnName(i)
					// +":" +rsMetaData.getColumnTypeName(i) + ":"
					// +rsMetaData.getColumnType(i) +":");
					switch (rsMetaData.getColumnType(i)) {
					case -4:
						// if(rSet.getBytes(i)!=null)
						updateTable.setBytes(i, rSet.getBytes(i));
						break;
					case 2004: // BLOB
						if (rSet.getBlob(i) != null) {
							blobData = rSet.getBlob(i);
							blobLength = (int) blobData.length();
							blobAsBytes = blobData.getBytes(1, blobLength);
							updateTable.setBytes(i, blobAsBytes);
						} else
							updateTable.setBytes(i, null);
						break;
					case 12: // VARCHAR2
						updateTable.setString(i, rSet.getString(i));
						break;
					case 2: // NUMBER
						updateTable.setInt(i, rSet.getInt(i));
						break;
					case 93: // DATE
						if (rSet.getDate(i) != null)
							updateTable.setDate(i, rSet.getDate(i));
						else
							updateTable.setString(i, null);
						break;
					default:
						System.out.println("Inside default");
						break;
					}

				}
				try {
					updateTable.executeUpdate();
				} catch (SQLServerException e) {
					e.printStackTrace();
				}
			}
			updateTable.close();
			System.out.println("No of Records Inserted:" + count);
		} catch (SQLException e) {
			this.setStatus(e.getMessage(), true);
			e.printStackTrace();
		} catch (Exception e) {
			this.setStatus(e.getMessage(), true);
			e.printStackTrace();
		}
	}
	public void insertProductTable(Map<String, String> mValues,
			String insertQuery) {
		PreparedStatement updateTable = null;
		try {
			updateTable = con.prepareStatement(insertQuery);
			updateTable.setString(1, mValues.get("SN")); // DHCT_SN
			updateTable.setString(2, mValues.get("SMSN"));
			updateTable.setString(3, mValues.get("MACADDR"));
			updateTable.setString(4, mValues.get("MODEL"));
			updateTable.setString(6, mValues.get("MFGDATE"));
			updateTable.setString(5, mValues.get("HWVER")); // HW REV
			updateTable.setString(7, mValues.get("CAAID"));
			updateTable.setString(8, mValues.get("MFGID"));
			updateTable.executeUpdate();
			updateTable.close();

		} catch (SQLException e) {
			this.setStatus(e.getMessage(), true);
			e.printStackTrace();
		}
	}
	public void updateBatchTable(String updateQuery, int batchid, String param1) {
		PreparedStatement updateBatch;
		try {
			updateBatch = con.prepareStatement(updateQuery);
			updateBatch.setString(1, param1);
			updateBatch.setInt(2, batchid);
			updateBatch.executeUpdate();
			updateBatch.close();
		} catch (SQLException e) {
			this.setStatus(e.getMessage(), true);
			e.printStackTrace();
		}

	}
	
	public void updateProductTable(String updateQuery, byte[] byteValue,String mfgdate, int emm_len, String dhctSN) {
		PreparedStatement updateProduct;
		try {
			SimpleDateFormat format = new SimpleDateFormat("MMddyyyyhhmmss");
	        Date parsed = format.parse(mfgdate);
	        java.sql.Timestamp tDate = new java.sql.Timestamp(parsed.getTime());
			updateProduct = con.prepareStatement(updateQuery);
			updateProduct.setTimestamp(2, tDate);
			updateProduct.setBytes(1, byteValue);
			updateProduct.setInt(3, emm_len);
			updateProduct.setString(4, dhctSN);
			updateProduct.executeUpdate();
			updateProduct.close();
		} catch (SQLException e) {
			this.setStatus(e.getMessage(), true);
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
	public ResultSet readDataFromTable(String queryString) {
		Statement stmt = null;
		ResultSet results = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			results = stmt.executeQuery(queryString);
		} catch (SQLException e) {
			this.setStatus(e.getMessage(), true);
			e.printStackTrace();
		}
		return results;
	}

	public ResultSet readDataFromTable(String queryString, String param) {
		ResultSet results = null;
		PreparedStatement pstmt;
		try {
			pstmt = con.prepareStatement(queryString);
			pstmt.setString(1, param);
			results = pstmt.executeQuery();
		} catch (SQLException e) {
			this.setStatus(e.getMessage(), true);
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * @param errorStatus
	 *            the errorStatus to set
	 */
	public void setStatus(String errorMessage, boolean errorStatus) {
		this.errorStatus = errorStatus;
		this.setStatus(errorMessage);
	}

	public void setStatus(String errorMessage) {
		this.errorMessage = this.errorMessage + "<br>" + errorMessage;
	}
	public void loadNothingBlob(String updateQuery, byte[] nothingBlob,
			int batchid) {
		PreparedStatement updateBatch;
		try {
			updateBatch = con.prepareStatement(updateQuery);
			updateBatch.setBytes(1, nothingBlob);
			updateBatch.setInt(2, batchid);
			updateBatch.executeUpdate();
			updateBatch.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
