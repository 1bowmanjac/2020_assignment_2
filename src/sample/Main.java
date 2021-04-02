package sample;

import javafx.application.Application;
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

public class Main extends Application {
    ListView<String> listView = new ListView();
    ListView<String> clientView = new ListView();

    @Override
    public void start(Stage primaryStage) throws Exception{
        // Server Screen
        var socket = new Socket("localhost", 25505);
        Client cl = new Client(socket);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Text scenetitle = new Text("Server");
        scenetitle.setFill(Color.WHITE);
        scenetitle.setId("welcome-text");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));


        //button
        Button download = new Button("Download");

        //Iterating the List element using for-each loop
        String lines[] = cl.DIR().split("\\r?\\n");

        viewFiles(listView,lines);

        listView.setPrefHeight(500);
        listView.setPrefWidth(1000);
        listView.setStyle("-fx-background-color: transparent");
        download.setOnAction(e-> {
            try {
                downloadButton(cl);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        grid.add(scenetitle, 0, 0, 1, 1);
        grid.add(listView, 0, 2, 2, 1);
        grid.add(download, 0, 3, 1, 1);


        SplitPane splitPane1 = new SplitPane();
        splitPane1.setOrientation(Orientation.VERTICAL);
        splitPane1.setPrefSize(200, 200);

//--------------------------------------------------------------------------------------------------------------
        // Local Screen
        GridPane grid1 = new GridPane();
        grid1.setPadding(new Insets(10, 10, 10, 10));
        grid1.setVgap(8);
        grid1.setHgap(10);
        Label name1 = new Label("Client");
        name1.setFont(Font.font(20));
        grid1.add(name1, 0, 1);
        Button uploadBtn = new Button("Upload");
        grid1.add(uploadBtn, 0, 3);


        File directoryPath = new File("Local_Files");
        String contents[] = directoryPath.list();

        for(int i=0; i<contents.length; i++) {
            System.out.println(contents[i]);
        }
        viewFiles(clientView,contents);
        clientView.setPrefHeight(500);
        clientView.setPrefWidth(1000);
        grid1.add(clientView,0,2);

        uploadBtn.setOnAction(e-> {
            try {
                uploadButton(cl);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        //listView.refresh();
        splitPane1.getItems().addAll(grid,grid1);

        Scene scene = new Scene(splitPane1, 1000, 1000);
        scene.getStylesheets().add("Viper.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX App");

        primaryStage.show();
    }

    private void viewFiles(ListView<String> listView, String[] lines) {
        listView.getItems().addAll(lines);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().getSelectedItem();
    }

    private void addFile(ListView<String> listView, String line) {
        listView.getItems().addAll(line);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().getSelectedItem();
    }


    private void downloadButton(Client cl) throws IOException {

        String message = "";
        ObservableList<String> selected;
        selected = (ObservableList<String>) listView.getSelectionModel().getSelectedItems();
        for(String m: selected){
            message+=m;
        }
        System.out.println(message);
        Socket socket = new Socket("localhost",25505);
        Client client = new Client(socket);
        client.DOWNLOAD(message,"Local_Files");
        //(clientView,client.DIR().split("\\r?\\n"));
        addFile(clientView,message);
    }

    private void uploadButton(Client cl) throws IOException {

        String message = "";
        ObservableList<String> selected;
        selected = (ObservableList<String>) clientView.getSelectionModel().getSelectedItems();
        for(String m: selected){
            message+=m;
        }
        System.out.println("Uploaded : " + message);
        var socket = new Socket("localhost", 25505);
        Client client = new Client(socket);
        File file = new File("Local_Files/"+message);
        client.UPLOAD(file);

        File directoryPath = new File("Server_Files");
        addFile(listView,message);
    }

    public static void main(String[] args) { launch(args); }
}
