import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private String ipAddress;
    private int port;

    public Client(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            client = new Socket(ipAddress, port);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // Start a separate thread to handle user input
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            // Read messages from the server
            String inMessage;
            while (!done && (inMessage = in.readLine()) != null) {
                System.out.println("Server: " + inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            e.printStackTrace(); // Log the exception
        }
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try (BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in))) {
                String message;
                while (!done) {
                    message = inReader.readLine();
                    if (message == null || message.equals("/quit")) {
                        shutdown();
                        break;
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Client <IP Address> <Port>");
            return;
        }

        String ipAddress = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(ipAddress, port);
        client.run();
    }
}
