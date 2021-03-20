import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
        private static Socket clientSocket;
        private static ServerSocket server;
        private static DataInputStream inStream;
        private static DataOutputStream outStream;
        private int port;

        public void setPort(int port){
            this.port = port;
            try {
                server = new ServerSocket(port);
            } catch (IOException e) {
                setPort(port+1);
            }
        }

        public void loadServer() {
            try {
                System.out.println("Сервер запущен");
                clientSocket = server.accept();
                if (clientSocket.isConnected()) {
                    System.out.println(ServerMessages.MESSAGE_ACCESS + clientSocket.getInetAddress());
                    inStream= new DataInputStream(clientSocket.getInputStream());
                    outStream=new DataOutputStream((clientSocket.getOutputStream()));
                    outStream.writeUTF(inStream.readUTF()+ServerMessages.USER_MESSAGE_ACCESS);
                    outStream.flush();
                }
                while (!clientSocket.isClosed()){
                    JSONObject jsonObject = new JSONObject(inStream.readUTF());
                    System.out.println(ServerMessages.MESSAGE_REQUEST+jsonObject.getString("request")+" "+jsonObject.getString("name")+ ServerMessages.MESSAGE_RESULT_YES);
                    switch (jsonObject.getString("request")){
                        case Requests.getVideo:
                            outStream.writeUTF(new JSONObject("{\"request\":\"OK\",\"video\":\"65\"}").toString());
                            outStream.flush();
                            byte[] array = Files.readAllBytes(Paths.get(jsonObject.getString("name")));
                            outStream.write(array);
                            outStream.flush();
                            break;
                        default:
                            System.out.println(ServerMessages.MESSAGE_RESULT_NO);
                            outStream.writeUTF(ServerMessages.MESSAGE_RESULT_NO);
                            outStream.flush();
                    }
                }
            }
            catch (IOException e) {
                System.out.println(ServerMessages.MESSAGE_END);
                try {
                    server.close();
                    inStream.close();
                    outStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
}

