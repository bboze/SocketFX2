package example;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import javafx.application.Platform;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.iso.packager.XMLPackager;


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

    public void exit(){
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
                System.out.println(finalLastMsg);


                //append message of the Text Area of UI (GUI Thread)
                if (read > 0)
                Platform.runLater(() -> {
                    client.txtAreaDisplay.appendText("\n" + "RECEIVED: ");
                });

                if (read > 0)
                Platform.runLater(() -> {
                    //display the message in the textarea
                    client.txtAreaDisplay.appendText(finalLastMsg + "\n");
                });

                try {
                    if (read > 0) EchoIsoMsg(Arrays.copyOfRange(buffer, 0, read));
                }catch (Exception e ) {
                    Platform.runLater(() -> {
                        client.txtAreaDisplay.appendText(e.toString() + '\n');
                    });
                }
            }

        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    private void EchoIsoMsg(byte[] bmsg) throws ISOException, IOException {
        //String hexmsg = msg;
        // convert hex string to byte array
        //byte[] bmsg = ISOUtil.hex2byte(hexmsg);
        ISOMsg m = new ISOMsg();
        // set packager, change ISO87BPackager for the matching one.
        m.setPackager(new XMLPackager());
        //unpack the message using the packager
        try {
            m.unpack(bmsg);
        } catch (ISOException e) {
            e.printStackTrace();
        }
        //dump the message to standar output
        m.dump(System.out, "");

        //if (m.getMTI().equals("0800")) {
        ISOMsg reply = (ISOMsg) m.clone();
        reply.setResponseMTI();
        reply.set(39, "00");

        Platform.runLater(() -> {
            client.txtAreaDisplay.appendText("\n" + "SENT: ");
        });

        Platform.runLater(() -> {
            try {
                client.txtAreaDisplay.appendText(new String(reply.pack()));
            } catch (ISOException e) {
                e.printStackTrace();
            }
        });

        output.write(reply.pack());
        output.flush();
        //}
    }

}
