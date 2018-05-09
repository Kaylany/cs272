package edu.orangecoastcollege.cs272.ic13.view;

import edu.orangecoastcollege.cs272.ic13.controller.Controller;
import edu.orangecoastcollege.cs272.ic13.model.VideoGame;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

// TODO: Implement the Initializable interface
public class ViewInventoryScene {

	private static Controller controller = Controller.getInstance();

	@FXML
	private ListView<VideoGame> userVideoGamesLV;
	@FXML
	private Label userLabel;
	
	// TODO: Override the initialize method (let Eclipse generate the method for you)
	// In this method:
	// 1) Set the text of the userLabel to the name of the current user from the controller
	// 2) Set the items of the userVideoGamesList to the games for the current user from the controller


	@FXML
	public Object backToAllGames()
	{
		//TODO: Complete this method
		// 1) Navigate back to the VideoGamesListScene
		return this;
	}
}
