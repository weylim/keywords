/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keywords;
/**
 *
 * @author WeeYong
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class MySQL {
    private Connection con = null;
    
    public void printStuff(String stuff) {
        System.out.println("in MySQL.printStuff");
        System.out.println(stuff);
    }
    
    /** Connects to MySQL with given parameters */
    public boolean connectDB(String user, String password, String host, String database) throws SQLException {
        try {
            // Load the JDBC driver
            String driverName = "com.mysql.jdbc.Driver";
            Class.forName(driverName);
            String url = "jdbc:mysql://" + host + "/" + database; // a JDBC url
            con = DriverManager.getConnection(url, user, password);
        } 
        catch (ClassNotFoundException ex) {    
            //Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Cannot connect MySQL because... " + ex.toString());
        }
        return true;
    }
    
    /** Disconnects from MySQL */
    public void disconnect() throws SQLException {
        con.close();
    }
    
    /** Read a sample from database */
    public Sample readSingle(String table, int id) throws SQLException{ 
        if (!con.isValid(0)) {System.out.println("No connection!");}
        
        Statement statement = con.createStatement();
        System.out.println("Select * from " + table + " limit " + id + ",1");
        Sample sample;
        
        
        	ResultSet result = statement.executeQuery("Select * from " + table + " limit " + id + ",1");
	        result.first();
	        sample = new Sample(result.getInt("Id"), result.getString("Title"), result.getString("Body"), result.getString("Tags"));
	        result.close();
        


        return sample;
    }
    
}