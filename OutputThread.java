package peertopeerclient;

import java.net.*;
import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author peter
 */
public class OutputThread extends Thread {
    List<String> peerList;
    protected static int port = 23657;
    String username;
    PeerToPeerClient clientClass;
    
    public OutputThread(PeerToPeerClient clientClass){
        this.clientClass = clientClass;
    }
    
    //Waits for input on the command line and sends the message to all peers.
    public void run() {
        //boolean connected = false;
        BufferedReader commandLineIn = new BufferedReader(new InputStreamReader(System.in));
        String commandLine = "";
         
            while(commandLine.length() <= 0){
                System.out.print("Enter your username: ");
                try {  
                    commandLine = commandLineIn.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(OutputThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            username = commandLine;
            System.out.println("Welcome "+username+"! Type /help for help!");
          
        
        while (true){
            try {
                commandLine = commandLineIn.readLine();            
                
                if(commandLine.toLowerCase().startsWith("/join")) {
                    //Connect to another peer, with the peers know ip address.
                    //Add ip connected to, to peer list.
                    //Close socket.
                    //Send your own ip to all connected peers on list.
                    String[] splitCommandLine = commandLine.split("\\s+");
                    if(splitCommandLine.length != 2) {
                        System.out.println("Not enough arguments, usage is /join [IP]");
                    }else if(clientClass.countPeers() > 0) {
                        //Peer has already connected to a socket or is connected to chat
                        System.out.println("Peer is already connected!");
                    } else {
                        String toJoinIP = splitCommandLine[1]; //Get the next token.
                        System.out.println("["+clientClass.getTime()+"] (You): Attempting to join peer: "+toJoinIP);
                        try {
                        //Client is trying to join a peer.
                            Socket clientSocket = new Socket(toJoinIP, port);
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            out.println("/request");
                            
                            //Get requested peers in peerlist.
                            String response = in.readLine();
                            
                            if(response.startsWith("/list")){
                                
                                String[] splitResponse = response.split("\\s+");
                                if(!splitResponse[1].equalsIgnoreCase("null")){
                                    //If there are clients to be added from request add them.
                                    for(int i = 1; i <= splitResponse.length-1; i++){
                                        clientClass.addPeer(splitResponse[i]);
                                        //System.out.println(splitResponse[i]);
                                    }
                                }
                                //Add in client you have connected to IP.
                                clientClass.addPeer(toJoinIP);
                            }
                            
                            //Now Broadcast to all current members that you have joined.
                            sendMessage("/join "+username);
                            //out.println("/join "+username);
                            System.out.println("["+clientClass.getTime()+"] (You): You have joined Chat, say Hi!");
                            
                            clientSocket.close();

                        } catch (IOException ex) {
                                System.out.println("Error: Could not connect to peer!");
                        }
                    }
                    

                }else if (commandLine.toLowerCase().startsWith("/connected")) {
                    //Display connectivity to peer chat.
                    System.out.println("-----ALL IPs CURRENTLY IN CHAT-----");
                    peerList = clientClass.getPeerList();
                    for (String peerIP : peerList){
                        System.out.println(peerIP);
                    }
                    System.out.println("-----------------------------------");
                    
                    
                }else if (commandLine.toLowerCase().startsWith("/leave")){
                    //Send leave message to all clients and terminate connection.
                    sendMessage("/leave "+username);
                    clientClass.clearPeers();
                    System.out.println("["+clientClass.getTime()+"] (You): You have left chat.");
                    
                }else if (commandLine.toLowerCase().startsWith("/exit") || commandLine.toLowerCase().startsWith("/quit")){
                    if(clientClass.countPeers() > 0){
                        System.out.println("You must leave chat first! Use: /leave");
                    }else {
                        System.out.println("Leaving chat program, bye!");
                        break;
                    }
                    
                }else if (commandLine.toLowerCase().startsWith("/help")){
                    System.out.println("-----P2P CHAT COMMANDS-----");
                    System.out.println("/join [ip] to connect to a known peer.");
                    System.out.println("/connected to view all ips currently in chat.");
                    System.out.println("/leave to leave chat.");
                    System.out.println("/exit to quit chat program.");
                    System.out.println("---------------------------");

                }else {
                    //Send Message
                        String message = "/message "+username+" "+commandLine;
                        sendMessage(message);
                }
                
                
            } catch (IOException ex) {
                Logger.getLogger(OutputThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public void sendMessage(String message){
        //Iterate though all added ips
        peerList = clientClass.getPeerList();
        for (String peerIP : peerList){
            //Iterate through peerIP list and open socket and send massage.
            try {
                //Client is trying to join a peer.
                Socket clientSocket = new Socket(peerIP, port);
                
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
                
                clientSocket.close();

            } catch (IOException ex) {
                System.out.println("Error: Could not connect to peer!");
            }
        }
        if(!message.startsWith("/leave") || message.startsWith("/join")){
            message = message.replace("/message "+username+" ", "");
            System.out.println("["+clientClass.getTime()+"] (You): "+message);
        }
    }
}
