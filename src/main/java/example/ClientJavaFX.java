/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author topman garbuja,
 *
 * This is the client which passes and get message to and from server and
 * further to multiple clients
 *
 * It also uses TaskReadThread.java file to be used in a new thread in order to
 * get simultaneous input from server
 */


public class ClientJavaFX extends Application {

    //controls
    MenuItem miConnect;
    MenuItem miExit;

    TextField txtName;
    TextField txtInput;
    TextField txtHost;
    TextField txtPort;
    ScrollPane scrollPane;
    public TextArea txtAreaDisplay;
    public Button btnConnect;

    Socket socket;
    DataOutputStream output = null;
    TaskReadThread socketRunner;

    @Override
    public void start(Stage primaryStage) {
        //MENU BAR
        Menu menu = new Menu("Socket");
        miConnect = new MenuItem("Connect");
        miConnect.setOnAction(new ButtonConnListener(this));
        miExit= new MenuItem("Exit");
        miExit.setOnAction(e -> {
            Platform.exit();
        });
        menu.getItems().add(miConnect);
        menu.getItems().add(miExit);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);

        //pane to hold scroll pane and HBox
        VBox vBox = new VBox(menuBar);

        scrollPane = new ScrollPane();   //pane to display text messages
        HBox hBox = new HBox(); //pane to hold input textfield and send button
        HBox hBox0 = new HBox(); //pane to hold input textfield and send button

        //Connection Parameters
        txtHost = new TextField("localhost");
        txtHost.setPromptText("Host");
        txtHost.setTooltip(new Tooltip("Insert Host name. "));
        txtPort = new TextField("8001");
        txtPort.setPromptText("Port");
        txtPort.setTooltip(new Tooltip("Insert port. "));
        btnConnect = new Button("Connect");
        btnConnect.setOnAction(new ButtonConnListener(this));

        hBox0.getChildren().addAll(txtHost, txtPort, btnConnect);
        hBox.setHgrow(txtHost, Priority.ALWAYS);
        hBox.setHgrow(txtPort, Priority.ALWAYS);

        txtAreaDisplay = new TextArea();
        txtAreaDisplay.setEditable(false);
        scrollPane.setContent(txtAreaDisplay);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        //define textfield and button and add to hBox
        txtName = new TextField();
        txtName.setPromptText("Name");
        txtName.setTooltip(new Tooltip("Write your name. "));
        txtInput = new TextField();
        txtInput.setPromptText("New message");
        txtInput.setTooltip(new Tooltip("Write your message. "));
        Button btnSend = new Button("Send");
        btnSend.setOnAction(new ButtonListener());

        hBox.getChildren().addAll(txtName, txtInput, btnSend);
        hBox.setHgrow(txtInput, Priority.ALWAYS);  //set textfield to grow as window size grows

        //set center and bottom of the borderPane with scrollPane and hBox
        vBox.getChildren().addAll(hBox0, scrollPane, hBox);
        vBox.setVgrow(scrollPane, Priority.ALWAYS);

        //create a scene and display
        Scene scene = new Scene(vBox, 450, 500);
        primaryStage.setTitle("Client: JavaFx Text Chat App");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Handle button action
     */
    private class ButtonListener implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent e) {
            try {
                //get username and message
                String username = txtName.getText().trim();
                String message = txtInput.getText().trim();

                //if username is empty set it to 'Unknown'
                if (username.length() == 0) {
                    username = "Unknown";
                }
                //if message is empty, just return : don't send the message
                if (message.length() == 0) {
                    return;
                }

                //send message to server
                output.writeUTF("[" + username + "]: " + message + "");
                output.flush();

                //clear the textfield
                txtInput.clear();
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }
    }

    /**
     * Handle button action
     */
    private class ButtonConnListener implements EventHandler<ActionEvent> {

        ClientJavaFX client;

        public ButtonConnListener(ClientJavaFX clientJavaFX) {
            client = clientJavaFX;
        }

        @Override
        public void handle(ActionEvent e) {
            try {
                if (!ConnectionUtil.connected ) {
                    ConnectionUtil.host = client.txtHost.getText().trim();
                    ConnectionUtil.port = Integer.parseInt(client.txtPort.getText().trim());

                    // Create a socket to connect to the server
                    socket = new Socket(ConnectionUtil.host, ConnectionUtil.port);

                    //Connection successful
                    txtAreaDisplay.appendText("\n" +  "Connected.");
                    btnConnect.setText("Disconnect");
                    miConnect.setText("Disconnect");
                    ConnectionUtil.connected = true;

                    // Create an output stream to send data to the server
                    output = new DataOutputStream(socket.getOutputStream());

                    //create a thread in order to read message from server continuously
                    socketRunner = new TaskReadThread(client, socket, output);
                    Thread thread = new Thread(socketRunner);
                    thread.start();
                }
                else{
                    socketRunner.exit();
                    socket.close();

                    //Connection closed
                    client.txtAreaDisplay.appendText("\n" + "Disconnected.");
                    client.btnConnect.setText("Connect");
                    client.miConnect.setText("Connect");
                    ConnectionUtil.connected = false;
                }
            } catch (Exception ex) {
                txtAreaDisplay.appendText(ex.toString() + '\n');
            }
        }
    }

}

