/*
 * Created on Nov 25, 2013
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class ShowDataFromTable extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTable table; // The table for displaying data
	String[] cNames = null;
	String[][] tValues = null;
	Connection con = null;
	String serial_number = null;
	JTextField textField_SN = null;
	Container contentPane = null;

	public ShowDataFromTable(ResultSet resultSet) {

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
					System.exit(0);
			}
		});

		// Initialize and lay out window controls
		getContentsOfTable(resultSet);
		// getContentsOfTable("SELECT TOP (20) SERIAL_NUMBER, ITEM_NUMBER, ATTRIBUTE_GROUP_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE"
		// +" FROM proteus.dbo.hvp_prt_serial_attributes");
		setTitle("Table Data");

		contentPane = getContentPane();
		contentPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// JLabel label_SN = new JLabel("Enter Serial Number:");
		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.anchor = GridBagConstraints.LINE_START;
		// c.weightx = 0.25;
		// c.weighty = 0;
		// c.gridx = 0;
		// c.gridy = 1;
		// c.gridwidth = 1;
		// contentPane.add(label_SN, c);
		//
		// textField_SN = new JTextField();
		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.anchor = GridBagConstraints.LINE_START;
		// c.weightx = 0.75;
		// c.weighty = 0;
		// c.gridx = 1;
		// c.gridy = 1;
		// c.gridwidth = 1;
		// contentPane.add(textField_SN, c);
		//
		// JButton button_GET = new JButton("Get Records");
		// c.fill = GridBagConstraints.HORIZONTAL;
		// c.anchor = GridBagConstraints.LINE_END;
		// c.weightx = 0.5;
		// c.weighty = 0;
		// c.gridx = 2;
		// c.gridy = 1;
		// c.gridwidth = 1;
		// contentPane.add(button_GET, c);

		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = .5;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		contentPane.add(new JScrollPane(table), c);

		/*
		 * button_GET.addActionListener(new ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent e) { serial_number =
		 * textField_SN.getText().trim(); getContentsOfTable(
		 * "select * from proteus.prt_serial_numbers where serial_number = '"
		 * +serial_number +"' and item_number='4031761';");
		 * System.out.println("testing"); } });
		 */
	}

	/*
	 * public static void main(String[] args) throws Exception {
	 * BasicConfigurator.configure(); GetDataFromTable qf = new
	 * GetDataFromTable(); // qf.pack(); qf.setSize(1000, 500);
	 * qf.setVisible(true);
	 * 
	 * }
	 */
	public void getContentsOfTable(ResultSet results) {
		try {
			ResultSetMetaData rsmd = results.getMetaData();
			cNames = new String[rsmd.getColumnCount()];
			int noOfColumns = rsmd.getColumnCount();
			for (int i = 0; i < noOfColumns; i++) {
				cNames[i] = rsmd.getColumnName(i + 1);
			}
			ArrayList<String> al = new ArrayList<String>();
			int noOfRows = 0;
			int size = 0;
			while (results.next()) {
				for (int i = 1; i <= noOfColumns; i++) {
					al.add(results.getString(i));
				}
				noOfRows++;
			}
			tValues = new String[noOfRows][noOfColumns];
			for (int j = 0; j < noOfRows; j++) {
				for (int i = 0; i < noOfColumns; i++) {
					tValues[j][i] = al.get(size);
					size++;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		table = new JTable(tValues, cNames); // Displays the table
	}
}
