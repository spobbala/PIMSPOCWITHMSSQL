/*
 * Created on Nov 21, 2013
 * Author Shridhar Pobbala
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import com.cisco.pims.Utilities.*;

import org.apache.log4j.BasicConfigurator;

public class PIMSPOC {
	public boolean mailStatus = false;
	static String mailMessage = "";

	public static void main(String[] args) {
		BasicConfigurator.configure();

		DBConnectionDAO client = new DBConnectionDAO();
		DBConnectionDAO client1 = new DBConnectionDAO();
		ResultSet rSet = null;
		ResultSet rSet1 = null;
//		client.connect("ODBC",
//				"jdbc:oracle:thin:@//lnxdb-stg-vm-271:1536/PIMSSTG", "hvp",
//				"vqcwb$40!lwm");

//		 client.connect("ODBC",
//		 "jdbc:oracle:thin:@//lnxdb-prd-514.cisco.com:1546/PIMS", "pims_v2", "ny*(0023");

		client.connect("ODBC",
		 "jdbc:oracle:thin:@//lnxdb-stg-vm-271:1536/PIMSSTG", "pims_v2", "ga*#0402");
		if (client.errorStatus) {
			System.out.println("error");
			System.exit(0);
		}
		client1.connect(
				"mssql",
				"jdbc:sqlserver://173.36.28.36\\QMXSQL1:51005;databaseName=PIMS",
				"pimsdb", "qd#jd4zW*R.?@{~");
		// client.connect("cassandra", "jdbc:cassandra://localhost:9160/pims");

		if (client1.errorStatus) {
			mailMessage = client.errorMessage;
			if (mailMessage != null)
				System.out.println(mailMessage);
		} else {
			String readQuery = null;
//			readQuery = "select BATCH_ID, SHIP_TO_CUST_ID, DELIVERY_ID, INFO1, INFO2, REQUESTOR, DATE_CREATED, DATE_COMPLETED, NOTHING_BLOB, EMM1_BLOB, EMM2_BLOB, BATCH_STATUS_CD, FAIL_FLAG, LAST_MODIFIED_DATE, DP_ID, TIME_TAG, SOURCE_OF_DATA, SP_FLAG, RETRY_CNTR from batch where batch_id = 704477";
//			readQuery = "select p.dhct_sn, p.emm_file from product p join emm_batch_request d on p.dhct_sn = d.dhct_sn where batch_id = 704477";
			 readQuery = "select * from ca_certificates";
//			 "Select SERIAL_NUMBER, ITEM_NUMBER, ATTRIBUTE_GROUP_ID, ATTRIBUTE_ID, ATTRIBUTE_SEQUENCE, ATTRIBUTE_VALUE, ATTRIBUTE_VALUE_QUALIFIER, TRANSACTION_DATE, TRANSACTION_TIME from hvp_prt_serial_attributes";
			rSet = client.readDataFromTable(readQuery);
//			readQuery = "select p.dhct_sn, p.emm_file from dbo.pims_product p join dbo.pims_batch_detail d on p.dhct_sn = d.dhct_sn where batch_id = 704477";
//			rSet1 = client1.readDataFromTable(readQuery);
//			compareRSet(rSet,rSet1);
			String insertQuery = null;
//			 insertQuery = "INSERT INTO dbo.pims_prt_serial_attributes "
//			 +
//			 "(SERIAL_ATTRIBUTE_ID, SERIAL_NUMBER, ITEM_NUMBER, ATTRIBUTE_GROUP_ID, ATTRIBUTE_ID, ATTRIBUTE_SEQUENCE, ATTRIBUTE_VALUE,"
//			 +
//			 "ATTRIBUTE_VALUE_QUALIFIER, TRANSACTION_DATE_TIME, SITE_ID, CREATED_BY) "
//			 + " values(?,?,?,?,?,?,?,?,?,1,'SPOBBALA')";
//			insertQuery = "INSERT INTO dbo.PIMS_BATCH_HEADER " + "(BATCH_ID, "
//					+ "SHIP_TO_CUST_ID, " + "DELIVERY_ID, " + "INFO1, "
//					+ "INFO2, " + "REQUESTOR, " + "DATE_CREATED, "
//					+ "DATE_COMPLETED, " + "NOTHING_BLOB, " + "EMM1_BLOB, "
//					+ "EMM2_BLOB, " + "BATCH_STATUS_CD, " + "FAIL_FLAG,"
//					+ "LAST_MODIFIED_DATE, " + "DP_ID, " + "TIME_TAG, "
//					+ "SOURCE_OF_DATA, " + "SP_FLAG, " + "RETRY_CNTR) "
//					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
			insertQuery = "insert into dbo.pims_ca_certificates values(?,?,?)";
			 client1.insertData(insertQuery, rSet);
//			 readQuery =
//			 "select * from emm_batch_request where batch_id = 704477 and dhct_sn in ('CTBBDZDVN', 'CTBBDZFFX', 'CTBBDZFJM', 'CTBBDZDWL', 'CTBBDZDWQ')";
//			 rSet = client.readDataFromTable(readQuery);
//			 insertQuery = "INSERT INTO dbo.PIMS_BATCH_DETAIL "
//			 + "(BATCH_ID, "
//			 + "DHCT_SN, "
//			 + "REQUEST_STATUS_CD, "
//			 + "FAIL_FLAG, "
//			 + "PALLET_ID, "
//			 + "REPLACED_SN, "
//			 + "EMM_BOXTYPE, "
//			 + "EMM_SMSN, "
//			 + "EMM_MACADDRESS, "
//			 + "EMM_MODEL, "
//			 + "EMM_REV, "
//			 + "EMM_SNSOURCE, "
//			 + "EMM_PKEYIND, "
//			 + "EMM_MFGDATE, "
//			 + "EMM_MATLNO, "
//			 + "EMM_ORIGINALSN, "
//			 + "EMM_CCARD_ID, "
//			 + "EMM_CABLECARD_SN, "
//			 + "EMM_HOST_SN, "
//			 + "EMM_HOST_MODEL, "
//			 + "EMM_HOST_REV, "
//			 + "EMM_HOST_ID, "
//			 + "EMM_HOST_MAC, "
//			 + "EMM_HOST_CM_MAC) "
//			 + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
//			 client1.insertData(insertQuery, rSet);
			// readQuery =
			// "select BATCH_ID, SHIP_TO_CUST_ID, DELIVERY_ID, INFO1, INFO2, REQUESTOR, BATCH_STATUS_CD, FAIL_FLAG, DP_ID, SOURCE_OF_DATA, RETRY_CNTR  from pims.pims_batch_header where batch_status_cd = '70'";
			// readQuery = "select * from dbo.pims_prt_serial_attributes";
//			 ResultSet rSet1 = client1.readDataFromTable(readQuery);

//			ShowDataFromTable showTable1 = new ShowDataFromTable(rSet);
//
//			showTable1.setSize(1000, 500);
//			showTable1.setVisible(true);

			mailMessage = client.errorMessage;
			if (mailMessage != null)
				System.out.println(mailMessage);

			if (client != null)
				client.close();
			if (client1 != null)
				client1.close();
			try {
				rSet.close();
				rSet1.close();
				// rSet1.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void compareRSet(ResultSet rSet, ResultSet rSet1) {
		byte[] byteValue1 = null;
		byte[] byteValue2 = null;
		try {
			while(rSet.next() && rSet1.next())
			{
				System.out.println(rSet.getString(1));
				byteValue1 = rSet.getBytes(2);
				System.out.println(rSet1.getString(1));
				byteValue2 = rSet1.getBytes(2);
				displayBytes(byteValue1,byteValue2);
			}
//			while(rSet1.next())
//			{
//				System.out.println(rSet1.getString(1));
//				byteValue2 = rSet1.getBytes(2);
////				System.out.println(byteValue2.length);
//			}
//			displayBytes(byteValue1,byteValue2);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void displayBytes(byte[] byteValue1, byte[] byteValue2) {
		byte[] tbyte = new byte[2740];
//		byte[] bytes = Hex.decodeHex(hexString .toCharArray());
		//raw1 = 44, raw2 = 76, 1024 + 1024 + 
		boolean bol = Arrays.equals(byteValue1, byteValue2);
		System.out.println(bol);
		System.arraycopy(byteValue1, 0, tbyte, 0, 2740);
		String str1 = convertByteToHex(tbyte);
//		displaybyte(tbyte, "Oracle");
		System.arraycopy(byteValue2, 0, tbyte, 0, 2740);
//		displaybyte(tbyte, "MSSQL ");
		String str2 = convertByteToHex(tbyte);
		if(str1.equals(str2))
			System.out.println("true");
		else
			System.out.println("false");
	}
private static String convertByteToHex(byte[] a){
		   StringBuilder sb = new StringBuilder();
		   for(byte b: a)
		      sb.append(String.format("%02x", b&0xff));
		   System.out.println("HEX:"+sb.toString());
		   return sb.toString();
}
	private static void displaybyte(byte[] tbyte, String str) {
		String tStr = null;
		tStr = new String(tbyte);
		System.out.println(str + ":" +  tStr);		
	}
}
