package de.htw.grischa.chess.database.server;

import de.htw.grischa.chess.database.client.DatabaseEntry;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClientListenerTest {
    private RAMQueue queue;
    private Socket server;

    public ClientListenerTest() {
        try {
            ServerSocket writeSocket = new ServerSocket(8888);
            testClient client = new testClient();
            client.start();
            this.server = writeSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.queue = new RAMQueue();
    }

    @Test
    public void testConstructor() throws Exception {
        ClientListener listener = new ClientListener(server, queue);
        assertNotNull(listener);
        listener.start();
        Thread.sleep(100);
        assertEquals(0, this.queue.size());
//        Thread.sleep(10000);
//        assertEquals(3,this.queue.size());
//        listener.interrupt();
    }

    private class testClient extends Thread {
        private OutputStream out;
        private Socket clientSocket;

        @Override
        public void run() {
            try {
                Thread.sleep(500);
                clientSocket = new Socket("127.0.0.1", 8888);
                this.out = clientSocket.getOutputStream();
                Thread.sleep(3000);
                String source1 = "0aa88f1786fb5a990863a1f6a4ab60c3#0005#0003";
                String source2 = "0bb88f1786fb5a990863a1f6a4ab60c3#0004#0002";
                String source3 = "0cc88f1786fb5a990863a1f6a4ab60c3#0003#0001";
                DatabaseEntry test1 = new DatabaseEntry(source1);
                submit(test1);
                System.out.println("submit 1");
                DatabaseEntry test2 = new DatabaseEntry(source2);
                submit(test2);
                System.out.println("submit 2");
                DatabaseEntry test3 = new DatabaseEntry(source3);
                submit(test3);
                System.out.println("submit 3");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void submit(DatabaseEntry entry) throws Exception {
            byte[] messageBytes = entry.toString().getBytes(Charset.forName("UTF-8"));
            this.out.write(messageBytes);
            this.out.flush();
            Thread.sleep(2);
        }
    }
}