import java.io.BufferedReader;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.CollationElementIterator;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server implements Runnable {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(8888);
            System.out.println("-- Server online --");
            Socket socket = null;
            while (true) {
                socket = ss.accept();
                Thread t = new Thread(new Server(socket));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Server(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            login();
            transferData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login() throws IOException {
        System.out.println("-- Communicate with " + socket.getInetAddress().getHostAddress() + " --");
        send("System: Welcome!", socket);
        send("System: Please enter your nick name: ", socket);
        name = receive(socket);
        while (clients.containsKey(name)) {
            send("System: This nick name is existed!", socket);
            send("System: Please enter your nick name: ", socket);
            name = receive(socket);
        }
        send("System: login successfully", socket);
        System.out.println(format.format(new Date()) + " Client " + name + " online");
        clients.put(name, socket);
    }

    private void transferData() throws IOException {
        String message = null;
        String[] info = new String[2];
        exit:
        while (true) {
            message = receive(socket);
            switch (analyse(message, info)) {
                case 0:
                    send("System: Message format wrong!", socket);
                    break;
                case 1:
                    clients.remove(name, socket);
                    System.out.println(format.format(new Date()) + " Client " + name + " offline");
                    break exit;
                case 2:
                    send("System: online clients (" + clients.keySet().size() + ")", socket);
                    StringBuffer buffer = new StringBuffer();
                    for (String s : clients.keySet()) {
                        buffer.append("\t" + s + "\n");
                    }
                    buffer.deleteCharAt(buffer.length() - 1);
                    send(buffer.toString(), socket);
                    break;
                case 3:
                    if (info[1].equalsIgnoreCase("all"))
                        sendToAll(name + ": " + info[0]);
                    else
                        send(name + ": " + info[0], clients.get(info[1]));
                    break;
                case 4: /* do nothing to renew the console of the client */
            }
        }
    }

    private boolean send(String message, Socket socket) throws IOException {
        if (socket == null) return false;
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(message);
        out.flush();
        return true;
    }

    private boolean sendToAll(String message) throws IOException {
        boolean flag = false;
        for (Socket value : clients.values()) {
            flag = send(message, value);
        }
        return flag;
    }

    private String receive(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        return in.readUTF();
    }

    private int analyse(String message, String[] info) {
        /* message format: send/content/to/client or list or exit */
        StringTokenizer tokenizer = new StringTokenizer(message, "/");
        if (tokenizer.countTokens() != 1 && tokenizer.countTokens() != 4) return 0;
        /* if the format of command is right */
        String command = tokenizer.nextToken();
        if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("exit/")) return 1;
        if (command.equalsIgnoreCase("list") || command.equalsIgnoreCase("list/")) return 2;
        if (command.equalsIgnoreCase("receive") || command.equalsIgnoreCase("receive/")) return 4;
        if (!command.equalsIgnoreCase("send")) return 0;
        /* if the command is send */
        info[0] = tokenizer.nextToken();
        if (!tokenizer.nextToken().equalsIgnoreCase("to")) return 0;
        info[1] = tokenizer.nextToken();
        return 3;
    }

    private String name;
    private Socket socket;
    private static HashMap<String, Socket> clients = new HashMap<>();
    private Scanner in = new Scanner(System.in);
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}