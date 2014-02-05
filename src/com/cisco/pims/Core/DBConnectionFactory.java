/*
 * Created on Nov 21, 2013
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.cisco.pims.Core;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
import java.sql.Statement;

public class DBConnectionFactory {
	public Connection connect(String server, String uName,
			String passWord) throws SQLException {
		Connection con = null;
		if(server==null ||
		   uName==null ||
		   passWord==null)
		{
			System.out.println("Mandatory Parameters missing for DB Connection");
			System.out.println("server="+server);
			System.out.println("uName="+uName);
			System.out.println("Password=" +passWord);
			System.exit(0);
		}
//			try {
//				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
			con = DriverManager.getConnection(server, uName, passWord);
		return con;
	}
	public static void close(Connection connection) {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			 System.err.println("Closing Connection failed: " + e.getMessage());
             e.printStackTrace();
		}
		
	}
	
    public static Date toSqlDate(java.util.Date date) {
        return (date != null) ? new Date(date.getTime()) : null;
       }
    
    /**
     * Returns a PreparedStatement of the given connection, set with the given SQL query and the
     * given parameter values.
     * @param connection The Connection to create the PreparedStatement from.
     * @param sql The SQL query to construct the PreparedStatement with.
     * @param returnGeneratedKeys Set whether to return generated keys or not.
     * @param values The parameter values to be set in the created PreparedStatement.
     * @throws SQLException If something fails during creating the PreparedStatement.
     */
    public static PreparedStatement prepareStatement
        (Connection connection, String sql, Object... values)
            throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        setValues(preparedStatement, values);
        return preparedStatement;
    }

    public static PreparedStatement prepareStatement
    (Connection connection, boolean genKeys, String sql, Object... values)
        throws SQLException
{
    	PreparedStatement preparedStatement = connection.prepareStatement(sql,
                genKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    setValues(preparedStatement, values);
    return preparedStatement;
}

    /**
     * Set the given parameter values in the given PreparedStatement.
     * @param connection The PreparedStatement to set the given parameter values in.
     * @param values The parameter values to be set in the created PreparedStatement.
     * @throws SQLException If something fails during setting the PreparedStatement values.
     */
    public static void setValues(PreparedStatement preparedStatement, Object... values)
        throws SQLException
    {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
        
        
    }
    
    /**
     * Quietly close the ResultSet. Any errors will be printed to the stderr.
     * @param resultSet The ResultSet to be closed quietly.
     */
    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Closing ResultSet failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    /**
     * Quietly close the Statement. Any errors will be printed to the stderr.
     * @param statement The Statement to be closed quietly.
     */
    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("Closing Statement failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    /**
     * Quietly close the Connection and Statement. Any errors will be printed to the stderr.
     * @param connection The Connection to be closed quietly.
     * @param statement The Statement to be closed quietly.
     */
    public static void close(Connection connection, Statement statement) {
        close(statement);
        close(connection);
    }

    public static void close(Statement statement, ResultSet resultSet) {
        close(resultSet);
    	close(statement);
    }

    /**
     * Quietly close the Connection, Statement and ResultSet. Any errors will be printed to the stderr.
     * @param connection The Connection to be closed quietly.
     * @param statement The Statement to be closed quietly.
     * @param resultSet The ResultSet to be closed quietly.
     */
    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        close(resultSet);
        close(statement);
        close(connection);
    }
    
		}
