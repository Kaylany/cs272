package edu.orangecoastcollege.cs272.ic13.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import edu.orangecoastcollege.cs272.ic13.model.DBModel;
import edu.orangecoastcollege.cs272.ic13.model.User;
import edu.orangecoastcollege.cs272.ic13.model.VideoGame;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Controller {

	private static Controller theOne;

	private static final String DB_NAME = "vg_inventory.db";

	private static final String USER_TABLE_NAME = "user";
	private static final String[] USER_FIELD_NAMES = { "_id", "name", "email", "role", "password"};
	private static final String[] USER_FIELD_TYPES = { "INTEGER PRIMARY KEY", "TEXT", "TEXT", "TEXT", "TEXT"};

	private static final String VIDEO_GAME_TABLE_NAME = "video_game";
	private static final String[] VIDEO_GAME_FIELD_NAMES = { "_id", "name", "platform", "year", "genre", "publisher"};
	private static final String[] VIDEO_GAME_FIELD_TYPES = { "INTEGER PRIMARY KEY", "TEXT", "TEXT", "INTEGER", "TEXT", "TEXT"};
	private static final String VIDEO_GAME_DATA_FILE = "videogames_lite.csv";

	// Below is the relationship table "user_games" which associates users with the video games in their inventory
	private static final String USER_GAMES_TABLE_NAME = "user_games";
	private static final String[] USER_GAMES_FIELD_NAMES = { "user_id", "game_id"};
	private static final String[] USER_GAMES_FIELD_TYPES = { "INTEGER", "INTEGER"};

	private User mCurrentUser;
	private DBModel mUserDB;
	private DBModel mVideoGameDB;
	private DBModel mUserGamesDB;

	private ObservableList<User> mAllUsersList;
	private ObservableList<VideoGame> mAllGamesList;

	private Controller() {
	}

	public static Controller getInstance() {
		if (theOne == null) {
			theOne = new Controller();
			theOne.mAllUsersList = FXCollections.observableArrayList();
			theOne.mAllGamesList = FXCollections.observableArrayList();

			try {
				// Create the user table in the database
				theOne.mUserDB = new DBModel(DB_NAME, USER_TABLE_NAME, USER_FIELD_NAMES, USER_FIELD_TYPES);
				ArrayList<ArrayList<String>> resultsList = theOne.mUserDB.getAllRecords();
				for (ArrayList<String> values : resultsList)
				{
					int id = Integer.parseInt(values.get(0));
					String name = values.get(1);
					String email = values.get(2);
					String role = values.get(3);
					theOne.mAllUsersList.add(new User(id, name, email, role));
				}

				// Create the video game table in the database, loading games from the CSV file
				theOne.mVideoGameDB = new DBModel(DB_NAME, VIDEO_GAME_TABLE_NAME, VIDEO_GAME_FIELD_NAMES, VIDEO_GAME_FIELD_TYPES);
				theOne.initializeVideoGameDBFromFile();
				resultsList = theOne.mVideoGameDB.getAllRecords();
				for (ArrayList<String> values : resultsList)
				{
					int id = Integer.parseInt(values.get(0));
					String name = values.get(1);
					String platform = values.get(2);
					int year = Integer.parseInt(values.get(3));
					String genre = values.get(4);
					String publisher = values.get(5);
					theOne.mAllGamesList.add(new VideoGame(id, name, platform, year, genre, publisher));
				}


				// Create the relationship table between users and the video games they own
				theOne.mUserGamesDB= new DBModel(DB_NAME, USER_GAMES_TABLE_NAME, USER_GAMES_FIELD_NAMES, USER_GAMES_FIELD_TYPES);


			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return theOne;
	}

	public boolean isValidPassword(String password)
	{
		// Valid password must contain (see regex below):
		// At least one digit
		// At least one lower case letter
		// At least one upper case letter
		// At least one special character !@#$%^&*()_+\-=[]{};':"\|,.<>/?
		// At least 8 characters long, but no more than 16
		return password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\\\|,.<>\\/?]).{8,16}$");
	}

	public boolean isValidEmail(String email)
	{
		return email.matches(
				"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	}

	public String signUpUser(String name, String email, String password)
	{
		// Check email to see if valid
	    if (!isValidEmail(email))
	        return "Email address not valid.  Please try different address.";

	    // Check to see if email is already used
	    // Loop through all users list
	    for (User u : theOne.mAllUsersList)
	        if (email.equalsIgnoreCase(u.getEmail()))
	            return "Email address already used.  Please sign in or use different address.";

	    // Check password to see if valid
	    //if (!isValidPassword(password))
	    //    return "Password must be at least 8 characters, including 1 upper case letter, 1 number and 1 symbol.";

	    // Made it through all the checks, create the new user in the database
	    String[] values = {name, email, "user", password};
	    // Insert the new user into the database
	    try
        {
	        // Store the new id
            int id = theOne.mUserDB.createRecord(
                    Arrays.copyOfRange(USER_FIELD_NAMES, 1, USER_FIELD_NAMES.length), values);
            // Save the new user as the current user
            theOne.mCurrentUser = new User(id, name, email, "user");
            // Add the new user to the observable list
            theOne.mAllUsersList.add(theOne.mCurrentUser);
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Error creating user, please try again.";
        }



		return "SUCCESS";
	}

	public String signInUser(String email, String password) {
		//TODO: Implement this method
	    // Loop through the list of all users
	    for (User u : theOne.mAllUsersList)
	    {
	        if (u.getEmail().equalsIgnoreCase(email))
	        {
	            // Go into database to retrieve password:
	            try
                {
                    ArrayList<ArrayList<String>> userResults = theOne.mUserDB.getRecord(String.valueOf(u.getId()));
                    String storedPassword = userResults.get(0).get(4);
                    // Check the passwords
                    if (password.equals(storedPassword))
                    {
                        mCurrentUser = u;
                        return "SUCCESS";
                    }
                    else
                        break;

                }
                catch (SQLException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
	        }
	    }

	    return "Incorrect email and/or password.  Please try again.";
	}

	public ObservableList<VideoGame> getGamesForCurrentUser()
	{
		ObservableList<VideoGame> userGamesList = FXCollections.observableArrayList();
		//TODO: Implement this method
		// 1) With the user_games table (mUserGamesDB), get the records that match the current user's (mCurrentUser) id
		// Note: the records returned will only contain the user_id and game_id (both ints)
		// Loop through the all games list (mAllGamesList).  If any game in the list matches the game id, then:
		// 2) Add the matching game to the user games list
		// 3) Return the user games list.
		return userGamesList;
	}

	public boolean addGameToUsersInventory(VideoGame selectedGame)  {
		//TODO: Implement this method
		// 1) Create an ObservableList<VideoGame> assigned to the list returned from getGamesForCurrentUser
		// If this list contains the selected game, return false (game has already been added, so prevent duplicates)
		// 2) Create a String array of the values to insert into the user_games (mUserGamesDB) table.
		// There are only two values in this table: the user's id (mCurrentUser) and the selected game id
		// 3) Create a new record using the USER_GAMES_FIELD_NAMES and the values array
		// If a SQLException occurs, return false (could not be added)
		// Otherwise, return true.
		return true;
	}

	public User getCurrentUser()
	{
		return mCurrentUser;
	}

	public ObservableList<User> getAllUsers() {
		return theOne.mAllUsersList;
	}

	public ObservableList<VideoGame> getAllVideoGames() {
		return theOne.mAllGamesList;
	}

	public ObservableList<String> getDistinctPlatforms() {
		ObservableList<String> platforms = FXCollections.observableArrayList();
		for (VideoGame vg : theOne.mAllGamesList)
			if (!platforms.contains(vg.getPlatform()))
				platforms.add(vg.getPlatform());
		FXCollections.sort(platforms);
		return platforms;
	}

	public ObservableList<String> getDistinctPublishers() {
		ObservableList<String> publishers = FXCollections.observableArrayList();
		for (VideoGame vg : theOne.mAllGamesList)
			if (!publishers.contains(vg.getPublisher()))
				publishers.add(vg.getPublisher());
		FXCollections.sort(publishers);
		return publishers;
	}



	private int initializeVideoGameDBFromFile() throws SQLException {
		int recordsCreated = 0;

		// If the result set contains results, database table already has
		// records, no need to populate from file (so return false)
		if (theOne.mUserDB.getRecordCount() > 0)
			return 0;

		try {
			// Otherwise, open the file (CSV file) and insert user data
			// into database
			Scanner fileScanner = new Scanner(new File(VIDEO_GAME_DATA_FILE));
			// First read is for headings:
			fileScanner.nextLine();
			// All subsequent reads are for user data
			while (fileScanner.hasNextLine()) {
				String[] data = fileScanner.nextLine().split(",");
				// Length of values is one less than field names because values
				// does not have id (DB will assign one)
				String[] values = new String[VIDEO_GAME_FIELD_NAMES.length - 1];
				values[0] = data[1].replaceAll("'", "''");
				values[1] = data[2];
				values[2] = data[3];
				values[3] = data[4];
				values[4] = data[5];
				theOne.mVideoGameDB.createRecord(Arrays.copyOfRange(VIDEO_GAME_FIELD_NAMES, 1, VIDEO_GAME_FIELD_NAMES.length), values);
				recordsCreated++;
			}

			// All done with the CSV file, close the connection
			fileScanner.close();
		} catch (FileNotFoundException e) {
			return 0;
		}
		return recordsCreated;
	}

}
