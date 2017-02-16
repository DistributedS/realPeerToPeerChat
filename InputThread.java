package peertopeerclient;

import java.net.*;
import java.io.*;
import java.util.List;
/**
 *
 * @author peter
 */
public class InputThread extends Thread {
    
    PeerToPeerClient clientClass;
    ServerSocket serverSocket;
    
    public InputThread(PeerToPeerClient clientClass){
        this.clientClass = clientClass;
    }
    
    protected static int port = 23657;
    //InputStream inputStream;
    HandleIncommingThread handleIncommingThread;
    
    public String getPeerList(){
        //Return all stored peer IPs in a String to be sent to the requested client.
        List<String> peerList;
        
        peerList = clientClass.getPeerList();
        
        if(peerList.isEmpty()){
            return "null";
        }else {    
            StringBuilder listAsString = new StringBuilder();   
        
            for (String peer : peerList){
                listAsString.append(peer);
                listAsString.append(" ");
            }
            return listAsString.toString();
        }
    }
    
    public synchronized void Terminate() throws IOException {
        serverSocket.close();
        this.notify();
    }
    
    public void run() {
        Socket connectionToClient;
        
        try {
            serverSocket = new ServerSocket(port);
            //Server loop
            while (true) {
                connectionToClient = serverSocket.accept();

                //Could spin off new thread to handle incomming communications.
                
                handleIncommingThread = new HandleIncommingThread(connectionToClient);
                handleIncommingThread.start();
                
            }
            
        } catch (IOException e) {
            System.err.println(e);
        }       
        
        //System.out.println("Input Thread.");
    }
    
    class HandleIncommingThread extends Thread {
        
        Socket connectionToClient;
        BufferedReader inFromPeer;
        //constuctor
        public HandleIncommingThread(Socket connectionToClient) {
            this.connectionToClient = connectionToClient;
        };
        
        public void run(){
            try {
                inFromPeer = new BufferedReader(new InputStreamReader(connectionToClient.getInputStream()));
            } catch (IOException ex) {
                System.err.println("Could not open Buffered Reader.");
                return;
            }
            while(true){
                try {
                    String message = inFromPeer.readLine();
                    if(message == null){
                        break;
                    }
                    //System.out.println("Printing: "+messagePart);
                    //
                    //System.out.println("Printing: "+messagePart);
                
                    if(message.startsWith("/request")) {
                        //Peer is trying to connect
                        //Send contents of peer list to newly connected peer
                        //Get clients ip address and add it to peer list
                        //Send ip to all others in list
                      
                        //format ip to remove leading forward slash and port.
                        
                        
                        //TODO: Get and send list of all peers back. Then add newly added ip to list.
                        
                        String peerList = getPeerList();
                        PrintWriter out = new PrintWriter(connectionToClient.getOutputStream(), true);
                        out.println("/list "+peerList);
                    
                        System.out.println("["+clientClass.getTime()+"] Handled Request of new Peer.");
                        
                    } else if(message.startsWith("/join")) {
                        //Get join message, add joining clients IP to peer list and print join message.
                        String[] splitMessage = message.split("\\s+");
                        
                        String peerIP = connectionToClient.getRemoteSocketAddress().toString().replace("/","");
                        peerIP = peerIP.split(":")[0];
                        
                        //Adding newly connected peer IP to peer list.
                        clientClass.addPeer(peerIP);
                        
                        System.out.println("["+clientClass.getTime()+"] "+splitMessage[1]+" joined chat.");
                        
                        
                    } else if(message.startsWith("/leave")) {
                        
                        String[] splitMessage = message.split("\\s+");
                        //Get ip of peer leaving and remove it from peer list.
                        String peerIP = connectionToClient.getRemoteSocketAddress().toString().replace("/","");
                        peerIP = peerIP.split(":")[0];
                        clientClass.removePeer(peerIP);
                        
                        System.out.println("["+clientClass.getTime()+" "+splitMessage[1]+" left chat.");
                    
                    } else if(message.startsWith("/message")) {
                        //Format and print message.
                        String[] splitMessage = message.split("\\s+");
                        message = message.replace("/message "+splitMessage[1]+" ", "");
                        System.out.println("["+clientClass.getTime()+"] "+splitMessage[1]+": "+message);
                    
                    } else {
                        System.out.println("Not a valid Message!");
                    }
                    
                
                } catch (IOException ex) {
                    System.err.println("Input stream closed");
                }
            }
        }
    }
}
