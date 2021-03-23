import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
        private Socket clientSocket;
        private ServerSocket server;
        private DataInputStream inStream;
        private DataOutputStream outStream;
        private int port;
        private byte[] buffer;
        private boolean flag=true;
        private Exception FileNotFound;

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
                System.out.println("Server started");
                System.out.println(port);
                    clientSocket = server.accept();
                    if (clientSocket.isConnected()) {
                        System.out.println(ServerMessages.MESSAGE_ACCESS + clientSocket.getInetAddress());
                        inStream = new DataInputStream(clientSocket.getInputStream());
                        outStream = new DataOutputStream((clientSocket.getOutputStream()));
                        outStream.writeUTF(inStream.readUTF() + ServerMessages.USER_MESSAGE_ACCESS);
                        outStream.flush();
                    }
                    while (!clientSocket.isClosed()) {
                        JSONObject jsonObject = new JSONObject(inStream.readUTF());
                        System.out.println(ServerMessages.MESSAGE_REQUEST + jsonObject.getString("request") + " " + jsonObject.getString("name") + ServerMessages.MESSAGE_RESULT_YES);
                        switch (jsonObject.getString("request")) {
                            case Requests.getFile:
                                try {
                                    System.out.println("Making file object name: "+jsonObject.getString("name"));
                                    File f = new File(new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent()+"\\"+jsonObject.getString("name"));
                                    System.out.println(f.getPath());
                                    if (f.exists()) {
                                        System.out.println("File exist. Sending response to client");
                                        outStream.writeUTF(new JSONObject("{\"request\":\"OK\",\"file\":\"" + f.length() + "\"}").toString());
                                        outStream.flush();
                                        buffer = new byte[(int) f.length()];
                                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
                                        bis.read(buffer, 0, buffer.length);
                                        System.out.println("Start sending file "+f.getName());
                                        outStream.write(buffer, 0, buffer.length);
                                        outStream.flush();
                                        System.out.println("File was send successful");
                                    }
                                    else{
                                        System.out.println("Throw exception");
                                        throw FileNotFound;
                                    }
                                } catch (Exception e) {
                                    String requestResultNo = "File not exist";
                                    outStream.writeUTF(new JSONObject("{\"request\":\""+requestResultNo+"\",\"file\":\"0\"}").toString());
                                    outStream.flush();
                                }
                                break;
                            case Requests.stop:
                                server.close();
                                inStream.close();
                                outStream.close();
                                flag = false;
                                break;
                            default:
                                System.out.println(ServerMessages.MESSAGE_RESULT_NO);
                                outStream.writeUTF(ServerMessages.MESSAGE_RESULT_NO);
                                outStream.flush();
                        }
                    }
                } catch (IOException e) {
                System.out.println(ServerMessages.MESSAGE_END);
            }
    }
}

