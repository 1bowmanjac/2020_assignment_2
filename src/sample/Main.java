package sample;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class Main extends Application {
    ListView<String> listView = new ListView<>();
    ListView<String> clientView = new ListView<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        // Login Scene
        Stage window = primaryStage;
        primaryStage.setTitle("File Sharing System");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Text scenetitle = new Text("File Share 420.24");
        scenetitle.setFill(Color.YELLOW);
        scenetitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        // User Input
        Label name = new Label("User:");
        grid.add(name, 0, 1);
        TextField name_field = new TextField();
        name_field.setPromptText("User");
        grid.add(name_field, 1, 1);

        // Password Input
        Label password = new Label("Password:");
        grid.add(password, 0, 2);
        PasswordField password_field = new PasswordField();
        grid.add(password_field, 1, 2);
        password_field.setPromptText("password");

        Button register = new Button("Register");
        register.setOnAction(actionEvent -> {
            Scene fileUI = null;
            try {
                fileUI = getFileUI(primaryStage, name_field.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("User: " + name_field.getText() + "\nPassword:" + password_field.getText());
            window.setScene(fileUI);
        });

        grid.add(register, 1, 4);


        Scene scene = new Scene(grid, 300, 300);

        //Did use some code from the java oracle css styling website
        scene.getStylesheets().add("login.css");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private Scene getFileUI(Stage primaryStage, String name) throws Exception{
        var socket = new Socket("localhost", 25505);
        Client cl = new Client(socket);

        // Iterating the List element using for-each loop
        String[] lines = cl.DIR().split("\\r?\\n");

        // ------------ Server Screen ------------

        // set Server Grid
        GridPane serverGrid = new GridPane();
        setGrid(serverGrid);

        Label serverTitle = new Label("Server");
        serverTitle.setFont(Font.font(20));

        // Download Button
        Button downloadBtn = new Button("Download");
        downloadBtn.setOnAction(e-> {
            try {
                downloadButton();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // View Server Files
        Button viewServerBtn = new Button("View");
        viewServerBtn.setOnAction(e-> {
            try {
                viewServerButton();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        serverGrid.add(viewServerBtn, 0, 4);

        // Server File List UI
        viewFiles(serverGrid,listView,lines,serverTitle,downloadBtn);

        // split screen
        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.VERTICAL);
        sp.setPrefSize(200, 200);

        // ------------ Local Screen ------------

        // set Server Grid
        GridPane clientGrid = new GridPane();
        setGrid(clientGrid);

        Label clientTitle = new Label("Client");
        clientTitle.setFont(Font.font(20));

        // to view client local shared files
        File directoryPath = new File("Local_Files");
        String[] contents = directoryPath.list();

        // Files in local shared file
        System.out.println("Files in local shared file");
        for(int i = 0; i< Objects.requireNonNull(contents).length; i++) {
            System.out.println(contents[i]);
        }

        // Upload Button
        Button uploadBtn = new Button("Upload");
        uploadBtn.setOnAction(e-> {
            try {
                uploadButton();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // View Client Files
        Button viewClientBtn = new Button("View");
        viewClientBtn.setOnAction(e-> {
            try {
                viewClientButton();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        clientGrid.add(viewClientBtn, 0, 4);

        // Client File List UI
        viewFiles(clientGrid,clientView,contents,clientTitle,uploadBtn);

        sp.getItems().addAll(serverGrid,clientGrid);

        Scene scene = new Scene(sp, 1000, 1000);
        scene.getStylesheets().add("Viper.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("File Sharing System");

        primaryStage.show();
        return scene;
    }

    /**
     * Sets up GridPane
     * @param grid File that you want to be uploaded
     */
    private void setGrid(GridPane grid){
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);
    }

    /**
     * Takes most responsibility of showing UI
     * @param grid  Sets up the grid
     * @param listView File UI
     * @param lines Files themselves
     * @param title Server/Client as titles
     * @param btn Download/Upload buttons
     */
    private void viewFiles(GridPane grid, ListView<String> listView, String[] lines, Label title, Button btn) {
        listView.getItems().addAll(lines);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().getSelectedItem();
        listView.setPrefHeight(500);
        listView.setPrefWidth(1000);
        listView.setStyle("-fx-background-color: transparent");
        grid.add(title, 0, 1);
        grid.add(listView,0,2);
        grid.add(btn, 0, 3);
    }

    /**
     * Updates file UI when a download/upload occurs
     * @param listView File UI
     * @param line Files themselves

     */
    private void addFile(ListView<String> listView, String line) {
        listView.getItems().addAll(line);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().getSelectedItem();
    }

    /**
     * Downloads file from Server to Local Client Shared Folder
     * @throws IOException
     */
    private void downloadButton() throws IOException {
        String message = "";
        ObservableList<String> selected;

        // grabs selected file from list
        selected = listView.getSelectionModel().getSelectedItems();
        for(String m: selected){
            message += m;
        }

        // makes connection and downloads file
        System.out.println("Downloaded : " +message);
        Socket socket = new Socket("localhost",25505);
        Client client = new Client(socket);
        client.DOWNLOAD(message,"Local_Files");

        // updates UI
        addFile(clientView,message);
    }

    /**
     * Uploads file from Local Client to Server Shared Folder
     * @throws IOException
     */
    private void uploadButton() throws IOException {
        String message = "";
        ObservableList<String> selected;

        // grabs selected file from list
        selected = clientView.getSelectionModel().getSelectedItems();
        for(String m: selected){
            message += m;
        }

        // makes connection and uploads file
        System.out.println("Uploaded : " + message);
        var socket = new Socket("localhost", 25505);
        Client client = new Client(socket);
        File file = new File("Local_Files/"+message);
        client.UPLOAD(file);

        // updates UI
        addFile(listView,message);
    }

    /**
     * View files content in server shared folder
     */
    private void viewServerButton() throws IOException {
        String message = "";
        ObservableList<String> selected;
        selected = listView.getSelectionModel().getSelectedItems();
        for(String m: selected){
            message += m;
        }
        System.out.println(selected);
        File file = new File("Server_Files/"+message);
        HostServices hostServices = getHostServices();
        hostServices.showDocument(file.getAbsolutePath());
    }

    /**
     * View files content in client local shared folder
     */
    private void viewClientButton() throws IOException {
        String message = "";
        ObservableList<String> selected;
        selected = clientView.getSelectionModel().getSelectedItems();
        for(String m: selected){
            message += m;
        }
        System.out.println(selected);
        File file = new File("Local_Files/"+message);
        HostServices hostServices = getHostServices();
        hostServices.showDocument(file.getAbsolutePath());
    }

    public static void main(String[] args) { launch(args); }
}
