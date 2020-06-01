package example;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.XMLPackager;

import java.io.IOException;

public class ConnectionUtil {
    public static String host = "localhost";
    public static int port = 8001;
    public static boolean connected = false;

    public static byte[] EchoIsoMsg(byte[] bmsg) throws ISOException, IOException {
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

        //if (m.getMTI().equals("0800")) {
        ISOMsg reply = (ISOMsg) m.clone();
        reply.setResponseMTI();
        reply.set(39, "00");

        return reply.pack();

    }
}
