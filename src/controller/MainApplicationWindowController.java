package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import static model.treatment.Export.createDirectory;
import static model.treatment.Export.exportToObj;

import model.Parameter;
import model.mesh.Mesh;
import model.treatment.Treatment;
import model.viewer.Viewer3D;
import config.Config;

/**
 * MainWindowController Stage Is the controller of the main window stage
 * 
 * @author picharles
 */
public class MainApplicationWindowController extends Stage implements Initializable {

	@FXML
	private BorderPane borderPane;
	@FXML
	private ImageView viewImage;
	@FXML
	private Button saveButton, onTreatmentButton, openFileChooserButton, adjustWidthButton, adjustHeightButton, resetButton;
	@FXML
	private MenuItem themePreference1, themePreference2, englishLanguagePreference, frenchLanguagePreference, close;
	@FXML
	private Menu file, edit, language;
	@FXML
	private TextField heightField, widthField, heightMeshField, maxWidthPrintField, maxHeightPrintField;
	@FXML
	private Label labelStep1, labelStep2, labelStep3, labelStep4, heightLabel, widthLabel, meshHeightLabel,
			maxWidthOfPrintLabel, maxHeightOfPrintLabel;
	@FXML
	private GridPane gridPaneParameters, gridPaneTreatment, gridPaneExport;
	@FXML
	private ResourceBundle ressources;
	@FXML
	private ListView ListView3D;
	@FXML
	private Pane paneViewer3D;

	private SubScene subSceneViewer3D;
	private String imagePath;
	private Parameter parameters = new Parameter();
	public List<Mesh> parcelsList = new ArrayList<>();
	public Mesh clipMesh = new Mesh();
	private File selectedFile;
	private double heightMesh, maxWidthPrint, maxHeightPrint, height, width;
	private Image image;
	private StringProperty heightProperty = new SimpleStringProperty();
	private Double ratioHeight, ratioWidth;
	private Viewer3D viewer;


	/**
	 * Initialize method
	 */
	public void initialize(URL location, ResourceBundle bundle) {        
		ressources = bundle;
		initialize3dViewer();
		initializeFirstLaunch();
	}
	
	/**
	 * Method to initialize the 3D viewer : Use Viewer3D model class
	 * first launch
	 */
	private void initialize3dViewer() {
		viewer = new Viewer3D();        
        subSceneViewer3D = viewer.initializeViewer3D(paneViewer3D);
        paneViewer3D.getChildren().add(subSceneViewer3D);
        viewer.configure(subSceneViewer3D);
	}


	/**
	 * Method to initialize somes Nodes for the first luanch
	 * first launch
	 */
	private void initializeFirstLaunch() {
		gridPaneParameters.setDisable(true);
		gridPaneTreatment.setDisable(true);
		gridPaneExport.setDisable(true);
		subSceneViewer3D.heightProperty().bind(paneViewer3D.heightProperty());
		subSceneViewer3D.widthProperty().bind(paneViewer3D.widthProperty());
	}

	/**
	 * Method to change propertie file language
	 * 
	 * @param lang
	 *            : string language shortcut
	 */
	private void loadLang(String lang) {
		Config.Current_Language = new Locale(lang);
		ressources = ResourceBundle.getBundle("properties.lang_" + Config.Current_Language);
		refreshTextApplication();
	}

	/**
	 * Method luanch when user change language to french
	 * 
	 * @param event
	 */
	@FXML
	private void changeLanguageFrench(ActionEvent event) {
		loadLang("fr");
	}

	/**
	 * Method luanch when user change language to english
	 * 
	 * @param event
	 */
	@FXML
	private void changeLanguageEnglish(ActionEvent event) {
		loadLang("en");
	}

	/**
	 * Method execute when user click on oppen button
	 * 
	 * @param event
	 * @throws IOException
	 */
	@FXML
	public void openFileChooser(ActionEvent event) throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
				new FileChooser.ExtensionFilter("All Files", "*.*"));
		selectedFile = fileChooser.showOpenDialog(this);
		if (selectedFile != null) {
			imagePath = selectedFile.toURI().toString();
			image = new Image(imagePath);
			viewImage.setImage(image);
			ratioWidth = image.getWidth() / image.getHeight();
			ratioHeight = image.getHeight() / image.getWidth();
			gridPaneParameters.setDisable(false);
			gridPaneTreatment.setDisable(false);
		}
	}

	/**
	 * Method launched when user press close button
	 * 
	 * @param event
	 */
	@FXML
	public void close(ActionEvent event) {
		Platform.exit();
	}

	/**
	 * Method launch when user press Treatment button
	 * 
	 * @param envent
	 * @throws IOException
	 */
	@FXML
	public void onTreatement(ActionEvent envent) throws IOException {
		if (ValidateAction()) {
			Treatment treatment = new Treatment();
			parcelsList = treatment.executeTreatment(selectedFile.toURI(), this.parameters);
			gridPaneExport.setDisable(false);
		}
	}

	/**
	 * Method launch when user press save button
	 * 
	 * @param envent
	 * @throws IOException
	 */
	@FXML
	public void save(ActionEvent envent) throws IOException {
		int i = 1;
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File("C://"));

		File selectedSaveFile = directoryChooser.showDialog(this);
		if (Config.DEBUG) {
			System.out.println(selectedSaveFile.toString());
		}
		if (selectedSaveFile != null) {
			createDirectory(selectedSaveFile.toString(), "Mesh");
			for (Mesh mesh : parcelsList) {
				exportToObj(mesh, selectedSaveFile.toString(), "Mesh", i);
				i++;
			}
			if (Config.DEBUG) {
				System.out.println("Exportation termin�e");
			}
		}
	}

	/**
	 * Method using for select the theme 1
	 */
	@FXML
	public void changeThemePreference1() {
		borderPane.getStylesheets().clear();
		borderPane.getStylesheets().add(getClass().getResource("/stylesheet/theme1.css").toExternalForm());
	}

	/**
	 * Method using for select the theme 2
	 */
	@FXML
	public void changeThemePreference2() {
		borderPane.getStylesheets().clear();
		borderPane.getStylesheets().add(getClass().getResource("/stylesheet/theme2.css").toExternalForm());
	}

	/**
	 * Setter of the Height Property the heightProperty to set
	 * 
	 * @param heightProperty
	 */
	public void setHeightProperty(StringProperty heightProperty) {
		this.heightProperty = heightProperty;
	}

	/**
	 * Getter of the height property
	 * 
	 * @return the height property
	 */
	public StringProperty getHeightProperty() {
		return heightProperty;
	}

	/**
	 * Method to adjust the height entered by users
	 */
	@FXML
	public void AdjustHeightAction() {
		if (heightField.getText().isEmpty()) {
			heightField.setStyle("-fx-control-inner-background: red");
		} else {
			width = Double.parseDouble(widthField.getText());
			height = width * ratioHeight;
			heightField.setText(String.valueOf(height));
			adjustHeightButton.setDisable(true);
		}
	}

	/**
	 * Method to adjust the with entered by users
	 */
	@FXML
	public void AdjustWidthAction() {
		if (widthField.getText().isEmpty()) {
			widthField.setStyle("-fx-control-inner-background: red");
		} else {
			height = Double.parseDouble(widthField.getText());
			width = height * ratioWidth;
			widthField.setText(String.valueOf(width));
			adjustWidthButton.setDisable(true);
		}
	}

	/**
	 * Method to reset wiht and height fields
	 */
	@FXML
	public void ResetAction() {
		widthField.setText("");
		heightField.setText("");
		adjustWidthButton.setDisable(false);
		adjustHeightButton.setDisable(false);
	}

	/**
	 * Method to validate parameters fields
	 * 
	 * @return
	 */
	private boolean ValidateAction() {
		if (heightField.getText().isEmpty() || widthField.getText().isEmpty() || maxWidthPrintField.getText().isEmpty()
				|| maxHeightPrintField.getText().isEmpty()) {
			showErrorPopUp(ressources.getString("error"), ressources.getString("errorParameterLabel"),
					ressources.getString("errorParameterLabelMessage"));
		} else {
			height = Double.parseDouble(heightField.getText());
			width = Double.parseDouble(widthField.getText());
			heightMesh = Double.parseDouble(heightMeshField.getText());
			maxWidthPrint = Double.parseDouble(maxWidthPrintField.getText());
			maxHeightPrint = Double.parseDouble(maxHeightPrintField.getText());
			if (height / width != ratioHeight) {
				showErrorPopUp(ressources.getString("error"), ressources.getString("errorAdjustLabel"),
						ressources.getString("errorAdjustLabelMessage"));

			} else {
				parameters.setElements(height, heightMesh, width, maxHeightPrint, maxWidthPrint);
				return true;
			}
		}
		return false;
	}

	/**
	 * Method to display an error pop up when error occured
	 * 
	 * @param title
	 * @param errorName
	 * @param errorMessage
	 */
	private void showErrorPopUp(String title, String errorName, String errorMessage) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(errorName);
		alert.setContentText(errorMessage);
		alert.showAndWait();
	}

	/**
	 * Method to set all text in current selected language
	 */
	private void refreshTextApplication() {
		refreshLabelText();
		refreshButtonText();
		refreshMenuItemText();
	}

	/**
	 * Method to refresh label text
	 */
	private void refreshLabelText() {
		labelStep1.setText(ressources.getString("labelStep1"));
		labelStep2.setText(ressources.getString("labelStep2"));
		labelStep3.setText(ressources.getString("labelStep3"));
		labelStep4.setText(ressources.getString("labelStep4"));
		heightLabel.setText(ressources.getString("heightLabel"));
		widthLabel.setText(ressources.getString("widthLabel"));
		meshHeightLabel.setText(ressources.getString("meshHeightLabel"));
		maxWidthOfPrintLabel.setText(ressources.getString("maxWidthOfPrintLabel"));
		maxHeightOfPrintLabel.setText(ressources.getString("maxHeightOfPrintLabel"));
	}

	/**
	 * Methof to refresh button text
	 */
	private void refreshButtonText() {
		adjustHeightButton.setText(ressources.getString("adjustButton"));
		adjustWidthButton.setText(ressources.getString("adjustButton"));
		resetButton.setText(ressources.getString("resetButton"));
		openFileChooserButton.setText(ressources.getString("openFileChooserButton"));
		saveButton.setText(ressources.getString("saveButton"));
		onTreatmentButton.setText(ressources.getString("onTreatmentButton"));
	}

	/**
	 * Methof to refresh menu item text
	 */
	private void refreshMenuItemText() {
		englishLanguagePreference.setText(ressources.getString("englishLanguagePreference"));
		frenchLanguagePreference.setText(ressources.getString("frenchLanguagePreference"));
		themePreference1.setText(ressources.getString("themePreference1"));
		themePreference2.setText(ressources.getString("themePreference2"));
		close.setText(ressources.getString("close"));
		file.setText(ressources.getString("file"));
		edit.setText(ressources.getString("edit"));
		language.setText(ressources.getString("language"));
	}

}
