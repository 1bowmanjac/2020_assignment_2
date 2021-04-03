package sample;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
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

        // Client File List UI
        viewFiles(clientGrid,clientView,contents,clientTitle,uploadBtn);

        sp.getItems().addAll(serverGrid,clientGrid);

        Scene scene = new Scene(sp, 1000, 1000);
        scene.getStylesheets().add("Viper.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("File Sharing System");

        primaryStage.show();
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

    public static void main(String[] args) { launch(args); }
}
