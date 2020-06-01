package example;

import  example.ConnectionUtil;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.XMLPackager;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;


public class SocketFX2Test {

    @Test
    public void echoMsg() throws IOException, ISOException {

        StringBuilder xmlBuilder = new StringBuilder("<isomsg>");
        xmlBuilder.append("<field id=\"0\" value=\"0800\"/>");
        xmlBuilder.append("<field id=\"11\" value=\"016271\"/>");
        xmlBuilder.append("<field id=\"33\" value=\"770023\"/>");
        xmlBuilder.append("<field id=\"70\" value=\"001\"/>");
        xmlBuilder.append("<field id=\"100\" value=\"00000000000\"/>");
        xmlBuilder.append("</isomsg>");
        String msg = xmlBuilder.toString();
        byte[] inMsg = msg.getBytes();

        xmlBuilder = new StringBuilder("<isomsg>").append("\n");
        xmlBuilder.append("  <!-- org.jpos.iso.packager.XMLPackager -->").append("\n");
        xmlBuilder.append("  <field id=\"0\" value=\"0810\"/>").append("\n");
        xmlBuilder.append("  <field id=\"11\" value=\"016271\"/>").append("\n");
        xmlBuilder.append("  <field id=\"33\" value=\"770023\"/>").append("\n");
        xmlBuilder.append("  <field id=\"39\" value=\"00\"/>").append("\n");
        xmlBuilder.append("  <field id=\"70\" value=\"001\"/>").append("\n");
        xmlBuilder.append("  <field id=\"100\" value=\"00000000000\"/>").append("\n");
        xmlBuilder.append("</isomsg>").append("\n");
        String expectedMsg = xmlBuilder.toString();

        byte[] out = ConnectionUtil.EchoIsoMsg(inMsg);
        String outMsg = new String(out, StandardCharsets.UTF_8);

        assertEquals(expectedMsg, outMsg);
    }

}
