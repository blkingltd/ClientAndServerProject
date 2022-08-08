import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import java.net.InetAddress;
import java.util.Arrays;
import java.io.*;



public class Client {
    public static String getClientIP (Socket socket) throws IOException
    {
        InetAddress IP = socket.getLocalAddress();
        String IPAddress = IP.getHostAddress();
        return IPAddress;
    }
    
    public static int getClientPort(Socket socket)
    {
        return socket.getLocalPort ();
    }
    public static String getServerIP (Socket socket) throws IOException
    {
        InetAddress IP = socket.getInetAddress();
        String IPAddress = IP.getHostAddress();
        return IPAddress;
    }
    
    public static int getServerPort (Socket socket)
    {
        return socket.getPort();
    }
    
    public static String getInstruction (String message)
    {
        if (message.indexOf("HTTP/1.1 200 OK") == 0)
            return "HTTP/1.1 200 OK";
        else if (message.indexOf("HTTP/1.1 404 Not Found") == 0)
            return "HTTP/1.1 404 Not Found";
        else if (message.indexOf("400 Bad Request") == 0)
            return "HTTP/1.1 400 Bad Request";
        
        return "";
    }
    public static void main(String[] args) {
        
        
        final int BUFFER_SIZE = 1024;
        
        try {
            // Connect to the server on the same machine
            Socket sock = new Socket("localhost", 80);
            
            //Get client IP address and port
            String clientIPAddress = getClientIP(sock);
            int clientPort = getClientPort (sock);
            System.out.println ("Client IP: " + clientIPAddress);
            System.out.println ("Client Port: " + clientPort + "\n");
            
            // Get server IP address and port
            String serverIPAddress = getServerIP(sock);
            int serverPort = getServerPort (sock);
            System.out.println ("Server IP: " + serverIPAddress);
            System.out.println ("Server Port: "+ serverPort + "\n");
            
            // Prepare output stream
            OutputStream out = sock.getOutputStream();

            // Request file
            String requestedFilepath = "files/hello.txt";
            String msg = "GET" + " " + "http://" + serverIPAddress + "/" + requestedFilepath;
            
            // Send the message to byte stream
            out.write (msg.getBytes());
            System.out.println ("Message Sending: " + msg);
            
            //Create input stream for receiving message and data
            InputStream in = sock.getInputStream();
            
            //Create buffer and receive message
            byte[] buffer = new byte[BUFFER_SIZE];
            int readSize = in.read(buffer);
            buffer = Arrays.copyOf(buffer, readSize);
            String rcvMessage = new String(buffer);
            
            //Print the message
            System.out.println ("\nMessage Received:\n" + rcvMessage + "\n");
            
            //Get instruction and print out
            String instruction = getInstruction (rcvMessage);
            System.out.println ("Instruction: " + instruction + "\n");
            
            if (instruction.equals("HTTP/1.1 200 OK"))
            {
                System.out.println ("Requested successfully!");
                
                //Extract filename;
                String filename = requestedFilepath.substring(requestedFilepath.lastIndexOf("/") + 1);
                System.out.println ("Filename: " + filename);
                
                //Get fileSize
                String size = rcvMessage.substring(instruction.length() + "Size: ".length()).strip();
                int fileSize = Integer.valueOf(size);
                System.out.println ("File size: " + fileSize + " (Bytes)\n");

                //Get data from server and write to file
                System.out.println ("Start receiving file...");    
                File fileDest = new File (filename);
                OutputStream fileOutputStream = null;
                try
                {
                    fileOutputStream = new FileOutputStream (fileDest);
                    int count = 0;
                
                    while (count < fileSize)
                    {
                        in = sock.getInputStream();
                        buffer = new byte[BUFFER_SIZE];
                        readSize = in.read(buffer);
                        buffer = Arrays.copyOf(buffer, readSize);

                        fileOutputStream.write(buffer, 0, readSize);
                        count += readSize;
                    }
                    
                    //Print succesfully received file
                    System.out.println ("Succesfully received file!");
                }
                finally
                {
                    fileOutputStream.close();
                }
                
            }
            else if (instruction.equals("HTTP/1.1 404 Not Found"))
            {
                System.out.println ("Requested file does not exist!");
            }
            else if (instruction.equals("HTTP/1.1 400 Bad Request"))
            {
                System.out.println ("Requested message not understood by server!");
            }

            // Close the stream
            in.close();
            out.close();
            
            // Close the socket
            sock.close();
            
            //Print exit
            System.out.println ("\nClient exits!");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}