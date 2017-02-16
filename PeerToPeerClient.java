package peertopeerclient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *
 * @author peter
 */
public class PeerToPeerClient {
    
    private List<String> peerList = new ArrayList<>();
    private boolean connected = false;
    
    //Getter function for peer list.
    public List<String> getPeerList() {
        return peerList;
    }
    
    //Function to add peer to peer list.
    public synchronized void addPeer(String IP) {
        if(!peerList.contains(IP)){
            peerList.add(IP);  
        }
    }
    
    //Function to remove a peer from the list given an ip.
    public synchronized void removePeer(String IP) {
        if(peerList.contains(IP)){
            peerList.remove(IP);
        }
    }
    
    public synchronized void clearPeers(){
        peerList = new ArrayList<>();
    }
    
    public synchronized int countPeers(){
        return peerList.size();
    }
    
    public String getTime(){
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PeerToPeerClient clientClass = new PeerToPeerClient();
        
        InputThread inputThread = new InputThread(clientClass);
        inputThread.start();
        
        OutputThread outputThread = new OutputThread(clientClass);
        outputThread.start();
        
        try {
            outputThread.join();
            inputThread.Terminate();
        
            inputThread.join();
            
        } catch (InterruptedException | IOException e){
            System.err.println(e);
        }
    }
    
}
