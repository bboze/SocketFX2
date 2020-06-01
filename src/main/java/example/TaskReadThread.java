package example;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;


/**
 * @author topman garbuja,
 * <p>
 * It is used to get input from server simultaneously
 */
public class TaskReadThread implements Runnable {
    //private variables
    Socket socket;
    ClientJavaFX client;
    DataInputStream input;
    DataOutputStream output = null;

    InputStream is = null;

    private volatile boolean active = true;

    //constructor
    public TaskReadThread(ClientJavaFX client, Socket socket, DataOutputStream output) {
        this.client = client;
        this.socket = socket;
        this.output = output;
    }

    public void exit() {
        active = false;
    }

    @Override
    public void run() {

        try {
            byte[] buffer = new byte[2024];
            int read = 0;

            is = socket.getInputStream();

            //continuously loop it
            while (active) {

                try {
                    read = is.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String lastMsg = "";
                lastMsg = new String(buffer, 0, read);
                String finalLastMsg = lastMsg;

                //append message of the Text Area of UI (GUI Thread)
                if (read > 0)
                    Platform.runLater(() -> {
                        //display the message in the textarea
                        client.txtAreaDisplay.appendText("\n" + "RECEIVED: ");
                        client.txtAreaDisplay.appendText(finalLastMsg + "\n");
                    });

                try {
                    if (read > 0) {

                        byte[] reply = ConnectionUtil.EchoIsoMsg(Arrays.copyOfRange(buffer, 0, read));

                        Platform.runLater(() -> {
                            client.txtAreaDisplay.appendText("\n" + "SENT: ");
                            client.txtAreaDisplay.appendText(new String(reply));
                        });

                        output.write(reply);
                        output.flush();
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        client.txtAreaDisplay.appendText(e.toString() + '\n');
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
