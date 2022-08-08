import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import java.io.OutputStream;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.*;

public class Server {
    static final int BUFFER_SIZE = 1024;

    //Get Server IP adddress
    public static String getServerIP () throws IOException
    {
        InetAddress IP = InetAddress.getLocalHost();
        String IPAddress = IP.getHostAddress();
        return IPAddress;
    }
    
    // Get Server port
    public static int getServerPort (ServerSocket socket)
    {
        return socket.getLocalPort();
    }
    
    public static String getClientIP (Socket socket)
    {
        InetAddress IP = socket.getInetAddress();
        String IPAddress = IP.getHostAddress();
        return IPAddress;
    }
    public static int getClientPort (Socket socket)
    {
        return socket.getPort();
    }
    
    // Get instruction from message
    public static String getInstruction (String message)
    {
        return message.substring(0, message.indexOf(" "));
    }
    
    //Get file size
    public static int getFileSize (File file) throws IOException
    {
        InputStream inputStream = new FileInputStream (file);
        int size = 0;
        byte[] buffer = new byte [BUFFER_SIZE];
        int length;
        while ((length = inputStream.read(buffer)) > 0)
        {
            size += length;
        }
        
        inputStream.close();
        return size;
    }
    
    //Check file exists or not
    public static boolean checkFileExists (String filepath) throws IOException
    {
        Path path = Paths.get (filepath);
        if (Files.exists(path))
            return true;
        return false;
    }
    
    public static String getFilepath (String message) throws IOException
    {
        int index = message.indexOf("http://");
        index += "http://".length();
        index = message.indexOf("/", index);
        
        return message.substring(index + 1);
    }

    public static void main(String[] args) throws IOException {
        
        
        try {
            // Create a socket with port 80
            int port = 80;
            ServerSocket srvSocket = new ServerSocket(port);
            
            // Get server IP address and port
            String serverIP = getServerIP ();
            int serverPort = getServerPort (srvSocket);
            // Print out server IP address and port
            System.out.println ("Created Server:");
            System.out.println ("IP: " + serverIP);
            System.out.println ("Port: " + serverPort + "\n");
           
            // Waiting for a connection
            Socket sock = srvSocket.accept();
            
            //print client connected
            String clientIP = getClientIP (sock);
            int clientPort = getClientPort (sock);
            System.out.println ("Client connected:");
            System.out.println ("IP Address: " + clientIP);
            System.out.println ("Port: " + clientPort + "\n");

            // buffer for the received data
            byte[] buffer = new byte[BUFFER_SIZE];

            // Incoming stream
            InputStream in = sock.getInputStream();
            // Receive data from the stream (in.read returns the size of the data)
            int readSize = in.read(buffer);
            
            // Remove unnecessary part of the message using the data size
            buffer = Arrays.copyOf(buffer, readSize);
            String rcvMessage = new String(buffer);
            System.out.println("Message Received: " + rcvMessage + "\n");
            
            //Parse and print info
            System.out.println ("Parsing:");
            String instruction = getInstruction (rcvMessage);
            String filepath = getFilepath (rcvMessage);
            System.out.println ("\tInstruction: " + instruction);
            System.out.println ("Filepath: " + filepath + "\n");
               
            //Create output stream for sending message
            OutputStream out = sock.getOutputStream ();
            
            // Check the GET instruction
            if (instruction.equals ("GET"))
            {
                //Check file exists or not
                boolean fileExists = false;
                if (checkFileExists (filepath))
                {
                    fileExists = true;
                    System.out.println ("Requested file exists!\n");
                }
                
                //If file exists
                if (fileExists)
                {
                    //get file size
                    File fileSource = new File (filepath);
                    int fileSize = getFileSize(fileSource);

                    // Response by the following instruction and file size
                    String rpdMessage = "HTTP/1.1 200 OK\n" + "Size: " + String.valueOf (fileSize);
                    out.write(rpdMessage.getBytes());
                    System.out.println ("Responding Message:\n" + rpdMessage + "\n");

                    //Read file and transfer data 
                    fileSource = new File (filepath);
                    InputStream fileInputStream = new FileInputStream (fileSource);

                    System.out.println ("Start transfering file...");
                    int read;
                    while ((read = fileInputStream.read(buffer)) != -1)
                    {
                        out.write(buffer, 0, read); 
                    }
                    System.out.println ("Done transfering file!");

                    // Close the file input stream
                    fileInputStream.close ();
                }
                else //File does not exist
                {
                    //Print file does not exist
                    System.out.println ("Requested file does not exist!");

                    //Response for file does not exist 
                    String rpdMessage = "HTTP/1.1 404 Not Found";
                    out.write (rpdMessage.getBytes());
                    System.out.println ("Responding Message: " + rpdMessage);
                }
            }
            else //Not GET instruction
            {
                System.out.println ("Requested message not understood by server!");
                String rpdMessage = "HTTP/1.1 400 Bad Request";
                out.write (rpdMessage.getBytes());
                System.out.println ("Responding Message:\n" + rpdMessage + "\n");
            }
            
            // Close the output stream
            out.close(); 

            // Close the stream
            in.close();      

            // Close the server
            srvSocket.close();
            
            // Print exit server
            System.out.println ("\nServer exits!");

        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}