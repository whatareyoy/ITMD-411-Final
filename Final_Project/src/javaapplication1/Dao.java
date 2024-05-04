package javaapplication1;
/**********************************************************************
*  +-+-+-+-+-+-+-+-+-+ +-+-+-+-+-+
*  |J|o|r|g|e| |M|a|r|t|i|n|e|z|
*  +-+-+-+-+-+-+-+-+-+ +-+-+-+-+-+
* 
* ITMD-411
* Date: 05/04/2024
*
* Assignment: Final Project
* The project deploys a GUI that will ask for a password that will determine if the admin is available. 
* If found in the CSV file then it will allow the user to update or delete tickets. 
* But regardless of who logins all employees and admins can view tickets and open a ticket.
*/
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dao {
	// instance fields
	static Connection connect = null;
	Statement statement = null;

	// constructor
	public Dao() {
	  
	}


	public Connection getConnection() {
		// Setup the connection with the DB
		try {
			connect = DriverManager
					.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false"
							+ "&user=fp411&password=411");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connect;
	}

	// CRUD implementation

	public void createTables() {
	    // variables for SQL Query table creations
	    final String createTicketsTable = "CREATE TABLE jMart_Tickets(ticket_id INT AUTO_INCREMENT PRIMARY KEY, ticket_issuer VARCHAR(30), ticket_description VARCHAR(200), status VARCHAR(30), start_date DATE, end_date DATE)";
	    final String createUsersTable = "CREATE TABLE jMart_Users(uid INT AUTO_INCREMENT PRIMARY KEY, uname VARCHAR(30), upass VARCHAR(30), admin int)";

	    try {
	        // execute queries to create tables
	        statement = getConnection().createStatement();
	        statement.executeUpdate(createTicketsTable);
	        statement.executeUpdate(createUsersTable);
	        System.out.println("Created tables in given database...");

	        // end create table
	        // close connection/statement object
	        statement.close();
	        connect.close();
	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	    }
	    // add users to user table
	    addUsers();
	}
	

	public void addUsers() {
		// add list of users from userlist.csv file to users table

		// variables for SQL Query inserts
		String sql;

		Statement statement;
		BufferedReader br;
		List<List<String>> array = new ArrayList<>(); // list to hold (rows & cols)

		// read data from file
		try {
			br = new BufferedReader(new FileReader(new File("./userlist.csv")));

			String line;
			while ((line = br.readLine()) != null) {
				array.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file");
		}

		try {

			// Setup the connection with the DB

			statement = getConnection().createStatement();

			// create loop to grab each array index containing a list of values
			// and PASS (insert) that data into your User table
			for (List<String> rowData : array) {

				sql = "insert into jMart_Users(uname,upass,admin) " + "values('" + rowData.get(0) + "'," + " '"
						+ rowData.get(1) + "','" + rowData.get(2) + "');";
				statement.executeUpdate(sql);
			}
			System.out.println("Inserts completed in the given database...");

			// close statement object
			statement.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	

	public int insertRecords(String ticketName, String ticketDesc) {
		int id = 0;
		try {
			statement = getConnection().createStatement();
			statement.executeUpdate("Insert into jMart_Tickets" + "(ticket_issuer, ticket_description, start_date, status) values(" + " '"
					+ ticketName + "','" + ticketDesc + "', CURRENT_DATE(), 'OPEN')", Statement.RETURN_GENERATED_KEYS);

			// retrieve ticket id number newly auto generated upon record insertion
			ResultSet resultSet = null;
			resultSet = statement.getGeneratedKeys();
			if (resultSet.next()) {
				// retrieve first field in table
				id = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}



	public ResultSet readRecords() {

		ResultSet results = null;
		try {
			statement = connect.createStatement();
			results = statement.executeQuery("SELECT * FROM jMart_Tickets");
			//connect.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	
// My code for update ticket
		public void updateTicket(int ticketId, String newDescription, String status) {  
	    try {
	        System.out.println("Creating update statement...");  // Will print "Creating update statement"
	        String sql;
	        if ("CLOSED".equals(status)) {  //If user types "CLOSED" it will update the the end_data with the current date in the ticket ID location.
	            sql = "UPDATE jMart_Tickets SET ticket_description = ?, status = ?, end_date = CURRENT_DATE() WHERE ticket_id = ?";
	        } else { // If its not CLOSED the it will only update ticket_description and status into the Ticket ID.
	            sql = "UPDATE jMart_Tickets SET ticket_description = ?, status = ? WHERE ticket_id = ?";
	        }
	        // Sql database changes
	        PreparedStatement pState = getConnection().prepareStatement(sql);
	        pState.setString(1, newDescription);  // change into the ticket_description location
	        pState.setString(2, status);  // change into the status location
	        pState.setInt(3, ticketId);  // change into the ticketId location
	        pState.executeUpdate(); 
	        System.out.println("Ticket updated successfully!");  //After that all changes happen then it will print "Ticket updated successfully! ".
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

		// My code for delete ticket
	public void deleteTicket(int tickID) {
	    try {
	        System.out.println("Creating statement...");  // Will print "Creating update statement"
	        String sql = "DELETE FROM jMart_Tickets WHERE ticket_id = ?";  //command line for deletion
	        PreparedStatement pState = getConnection().prepareStatement(sql);  //Connection to the database
	        pState.setInt(1, tickID); // First row as ID ticket
	        //The delete option will ask the user for ticket ID and then it will ask the user for Confirmition 
	        int response = JOptionPane.showConfirmDialog(null, "Delete ticket # " + tickID + "?", 
	                                   "Confirm",  JOptionPane.YES_NO_OPTION, 
	                                   JOptionPane.QUESTION_MESSAGE);
	        if (response == JOptionPane.NO_OPTION) {  //If the recored does not exist then it will stop and tell user of its absents
	           System.out.println("No record deleted");
	        } else if (response == JOptionPane.YES_OPTION) {  // If ticket ID is found then it will a countine and delete the ticket.
	        	pState.executeUpdate();
	          System.out.println("Record deleted");
	        } else if (response == JOptionPane.CLOSED_OPTION) {
	          System.out.println("Request cancelled");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	}
	
}
