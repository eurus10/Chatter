import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1");
            client.run(8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Client(String host) {
        this.host = host;
        System.out.println("-- Client online --");
    }

    /**
     * connect to the server with the specific port
     *
     * @param port the port to connect the server
     * @throws IOException
     */
    public void connect(int port) throws IOException {
        socket = new Socket(host, port);
        System.out.println("-- Communicate with port " + port + "--");
    }

    public void run(int port) throws IOException {
        connect(port);
        String message = null;
        while (true) {
            /* waiting for message from the server */
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            receive();
            System.out.print("upload: ");
            message = in.nextLine();
            send(message);
            if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("exit/")) break;
        }
        socket.close();
        System.out.println("-- Client offline --");
    }

    public void send(String message) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(message);
        out.flush();
    }

    public void receive() throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        while (in.available() != 0) System.out.println(in.readUTF());
    }

    private String host;
    private Socket socket;
    private Scanner in = new Scanner(System.in);
}
