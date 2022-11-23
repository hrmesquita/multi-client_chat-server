package org.academiadecodigo.thisfunctionals.MIRCServer;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunnableServer {


    ExecutorService threads = Executors.newFixedThreadPool(30);
    CopyOnWriteArrayList<Client> clientsPool = new CopyOnWriteArrayList<>();
    ServerSocket serverSocket;

    {
        try {
            serverSocket = new ServerSocket(9090);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Socket clientSocket;

    public void run() {
        while (true) {

            System.out.println("##################################");
            System.out.println("# Waiting for new connections... #");
            System.out.println("##################################");

            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = new Client(clientSocket);
            clientsPool.add(client);
            threads.submit(client);
            System.out.println("New connected IP: " + clientSocket.getLocalAddress());
            System.out.println("Connected on socket: " + (clientSocket.getLocalSocketAddress()).toString().split(":")[1]);
            System.out.println("Client socket: " + clientSocket.getRemoteSocketAddress().toString().split(":")[1]);

        }
    }

    public StringBuilder connectedClients() {
        StringBuilder clientCount = new StringBuilder();

        for (Client client : clientsPool) {
            clientCount.append(client.getName()).append("\n");
        }

        return clientCount;
    }

    private void resolve(OutputStreamWriter clientWriter, BufferedReader reader, String clientName, Socket socket) throws IOException {

        String name = " " + ("Client-" + clientName);

        clientWriter.write("*********************************************\n");
        clientWriter.write("* Hi! Please use the chat with moderation!! *\n");
        clientWriter.write("* If you have any questions just try /help! *\n");
        clientWriter.write("*********************************************\n");
        clientWriter.flush();


        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("/quit")) {
                clientWriter.write("Goodbye! =)\n");
                clientWriter.flush();
                clientsPool.remove(socket);
                socket.close();
                break;
            }
            if (line.equals("/help")) {
                clientWriter.write("The current chat commands are: /quit /help /lottery /changeName \n");
                clientWriter.flush();
                continue;
            }
            if (line.equals("/changeName")) {
                clientWriter.write("Tell me your new name: \n");
                clientWriter.flush();
                line = reader.readLine();
                name = line;
                clientWriter.write("Your name has been changed! \n");
                clientWriter.flush();
                continue;
            }
            if (line.equals("/lottery")) {
                clientWriter.write("" + ((Math.random() * 100) > 70 ? "OMG BIG WIN!!!!! *777*" : "You lost it dude, get back to work \n"));
                clientWriter.flush();
                continue;
            }
            if (line.equals("/available")) {
                clientWriter.write(connectedClients().toString());
                clientWriter.flush();
                continue;
            }
            if (line.split("")[0].equals("/")) {
                clientWriter.write("Sorry, but couldn't figure out that command! \n");
                clientWriter.flush();
                continue;
            }


            for (Client client : clientsPool) {
                System.out.println(client.socket.toString());
                client.getOutputStream().write(name + " said: " + line + "\n");
                client.getOutputStream().flush();
            }
            System.out.println("CHAT LOG # " + name + " : " + line);
        }

        clientWriter.write("Hate to see you going! See ya next time!!");
        System.out.println(name + " just disconnected!");

    }


    public class Client implements Runnable {
        OutputStreamWriter clientWriter;
        BufferedReader clientReader;
        String name = null;

        private Socket socket;

        public Client(Socket socket) {
            this.socket = socket;
            try {
                clientWriter = new OutputStreamWriter(socket.getOutputStream());
                clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            this.name = Thread.currentThread().getName().split("-")[3];
            try {
                resolve(clientWriter, clientReader, this.name, this.socket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public OutputStreamWriter getOutputStream() {
            return clientWriter;
        }

        public String getName() {
            return this.name;
        }

    }

}
