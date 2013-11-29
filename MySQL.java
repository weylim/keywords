/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keywords;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySQL {
    private Connection con = null;
    
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
    public Sample readSingle (String table, int id) throws SQLException{ 
        if (!con.isValid(0)) {System.out.println("No connection!"); assert(false);}
        
        Statement statement = con.createStatement();
        Sample sample;
        try (ResultSet result = statement.executeQuery("Select * from " + table + " limit " + id + ",1")) {
            result.first();
            sample = new Sample(result.getInt("Id"), result.getString("Title"), result.getString("Body"), result.getString("Tags"));
        }
        return sample;
    } 
    
    /** Count number of records which contains a specified substring in a specified column in a specified table in the database 
     * @param table table of interest 
     * @param column column of interest 
     * @param substr substring to be present in records in the specified column of the specified table 
     * @return number of records whose column contains the specified substring */
    public int containsSubstr (String table, String column, String substr) throws SQLException {
        if (!con.isValid(0)) {System.out.println("No connection!"); assert(false);}
        
        int count;
        Statement statement = con.createStatement();
        Texter texter = new Texter();
        substr = texter.escapeChars(substr, ".^$*+?()[{\\|", "\\\\");
        substr = texter.escapeChars(substr, "'", "\\");
        //System.out.println("Select count(*) from " + table + " where " + column + " REGEXP '(^|[^0-9a-z])" + substr + "($|[^0-9a-z])'");
        try (ResultSet result = statement.executeQuery("Select count(*) from " + table + " where " + column + " REGEXP '(^|[^0-9a-z])" + substr + "($|[^0-9a-z])'")) {
            result.first();
            count = result.getInt(1);
        }
        return count;
    }
    
    /** Return list of Integers in the intColumn field for all records whose substrColumn field contains a specified substring */
    public List<Integer> containsSubstr (String table, String substrColumn, String substr, String intColumn) throws SQLException {
        List<Integer> IDs = new ArrayList<>(); 
        if (!con.isValid(0)) {System.out.println("No connection!"); assert(false);}
        
        Statement statement = con.createStatement();
        // select id from association1000 where tagset like "% 919 %";
        try (ResultSet result = statement.executeQuery("Select " + intColumn + " from " + table + " where " + substrColumn + " like %" + substr + "%")) {
            while(result.next()) {
                IDs.add(result.getInt(1));
            }
        }
        return IDs;
    } 
    
    /** Query to get the number of rows in a table 
     * @param table table of interest 
     * @result number of rows in table */
    public int getNRows(String table) throws SQLException {
        if (!con.isValid(0)) {System.out.println("No connection!"); assert(false);}
        int Nrows;
        Statement statement = con.createStatement();
        try (ResultSet result = statement.executeQuery("Select count(*) from " + table)) {
            result.first();
            Nrows = result.getInt(1);
        }
        return Nrows;
    }
       
    ///////////////////////////////////
    //// Histogram-specific tables ////
    ///////////////////////////////////
    /** Update the frequency count of a term in a table containing a string as its key 
     * @param table table of interest 
     * @param keyField field for the key 
     * @param key key as a string
     * @param countField field containing the frequency count for the key */
    public int incrementHistogram(String table, String keyField, String key, String countField) throws SQLException {
        if (!con.isValid(0)) {System.out.println("No connection!"); assert(false);}
        
        String cmd = "insert into " + table + " (" + keyField + "," + countField + ") values(\"" + key + "\",1) on duplicate key update " + countField + "=" + countField + "+1";        
        Statement statement = con.createStatement();
        int Naffected = statement.executeUpdate(cmd);
        assert(Naffected == 1 || Naffected == 2);
        return Naffected;
    }
    
    /** Read corresponding int value for specified key string from table */
    public int getInt (String table, String keyColumn, String key, String intColumn) throws SQLException {
        if (!con.isValid(0)) {System.out.println("No connection!"); assert(false);}
        Statement statement = con.createStatement();
        int value = 0;
        key = key.replaceAll("\"", "\\\\\""); // escape all double quoutes in the key
        try (ResultSet result = statement.executeQuery("Select " + intColumn + " from " + table + " where " + keyColumn + "=\"" + key + "\"")) {
            if (result.next()) {
                result.first();
                value = result.getInt(1);
            }
        }
        return value;
    }
    
    /** Read corresponding String value for specified key Integer from table */
    public String getStr (String table, String keyColumn, Integer key, String strColumn) throws SQLException {
        if (!con.isValid(0)) {System.out.println("No connection!"); assert(false);}
        Statement statement = con.createStatement();
        String value = null;
        try (ResultSet result = statement.executeQuery("Select " + strColumn + " from " + table + " where " + keyColumn + "=" + key + "")) {
            if (result.next()) {
                result.first();
                value = result.getString(1);
            }
        }
        return value;
    }
}