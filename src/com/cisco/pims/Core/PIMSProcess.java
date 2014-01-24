/*
 * Created on Dec 11, 2013
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


public class PIMSProcess {
	
public static void main(String args[]) {
		String osName = null;
		Connection lclCon = null;
		InputStream inStream = null;
		DBConnectionFactory pimsDAO = new DBConnectionFactory();
		String processName = null;
		int batchid = 0;
		Properties propFile = new Properties();
		
		// Determine underlying OS where this program is run
		osName = System.getProperty("os.name").contains("Windows") ? "Windows"
				: "Other";
		
		// Unix/Linux environment logic
		if (!osName.equalsIgnoreCase("Windows")) {
			if (args.length == 1) {
				processName = args[0];
			} else if (args.length > 1) {
				processName = args[0];
				batchid = Integer.parseInt(args[1]);
			} else {
				System.out.println("Please pass arguments in below order:");
				System.out.println("1. properties file Location.(Mandatory)");
				System.out.println("2. Process Name.(Mandatory)");
				System.out.println("2. Batch ID.(Optional)");
			}
		}
		
		// Windows environment logic
		else if (args.length == 1)
			processName = args[0];
		else if (args.length > 1) {
			processName = args[0];
			batchid = Integer.parseInt(args[1]);
		} else
			processName = "17_20";
		
		try {
			inStream = PIMSProcess.class
					.getResourceAsStream("PIMSProperties.properties");
				propFile.load(inStream);
					inStream.close();
			lclCon = pimsDAO.connect("mssql",
					propFile.getProperty("MSSQLServer"),
					propFile.getProperty("DBUserName"),
					propFile.getProperty("DBPassword"));
			if (processName.equals("17_20")) { // 17_20 Process
				PIMSProcess_17_20 proc17_20 = new PIMSProcess_17_20(lclCon, propFile);
				proc17_20.process(batchid);
			} else if (processName.equals("20_30")) { // 20_30 Process
				PIMSProcess_20_30 proc20_30 = new PIMSProcess_20_30(lclCon, propFile);
				proc20_30.process(batchid);
			} else if (processName.equals("30_40")) { // 30_40 Process
				PIMSProcess_30_40 proc30_40 = new PIMSProcess_30_40(lclCon, propFile);
				proc30_40.process(batchid);
			} else if (processName.equals("40_50")) { // 40_50 Process
				PIMSProcess_40_50 proc40_50 = new PIMSProcess_40_50(lclCon, propFile);
				proc40_50.process(batchid);
			} else if (processName.equals("50_70")) { // 50_70 Process
				PIMSProcess_50_70 proc50_70 = new PIMSProcess_50_70(lclCon, propFile);
				proc50_70.process(batchid);
			} else {
				System.out.println("Invalid process Name, valid values:");
				System.out.println("17_20/20_30/30_40/40_50/50_70");
			}
	} catch (IOException e) {
		e.printStackTrace();
	} catch (SQLException e) {
		e.printStackTrace();
	}
}
}
