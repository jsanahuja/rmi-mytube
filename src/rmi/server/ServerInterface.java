/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi.server;

/**
 *
 * @author BanNsS1
 */
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import rmi.client.ClientInterface;
import rmi.common.MyTubeFile;

public interface ServerInterface extends Remote{
    
    public ClientInterface local_getUserById(int id) throws RemoteException;
    public String local_getFileName(ServerInterface server, int fid) throws RemoteException;
    
    
    //DISTRIBUTED SERVER FEATURES
    public void local_serverDisconnect(ServerInterface server, int id) throws RemoteException;
    public void global_serverDisconnect() throws RemoteException;
    
    public void local_notifyAll(ServerInterface server, String title, String owner) throws RemoteException;
    public void global_notifyAll(MyTubeFile file) throws RemoteException;
        
    public boolean local_userExists(ServerInterface server, int id) throws RemoteException;
    public boolean global_userExists(int id) throws RemoteException;
    
    public boolean local_fileExists(ServerInterface server, int id) throws RemoteException;
    public boolean global_fileExists(int id) throws RemoteException;
    
    public ArrayList<MyTubeFile> local_listFilesByDescription(ServerInterface server, String description) throws RemoteException;
    public ArrayList<MyTubeFile> global_listFilesByDescription(ClientInterface client, String description) throws RemoteException;
    
    public byte[] local_downloadFileByTitle(ServerInterface server, String title) throws RemoteException;
    public boolean global_downloadFileByTitle(ClientInterface client, String title) throws RemoteException;
    
    //SERVERS CONNECTION
    public boolean serverRegister(String ip, int port, int id) throws RemoteException;
    public boolean acceptRegister(String ip, int port, int id) throws RemoteException;
    public int serverPing(ServerInterface server) throws RemoteException;
    public int serverPong() throws RemoteException;
        
    //CLIENT COMUNICATION
    public boolean register(ClientInterface client) throws RemoteException;
    public void disconnect(ClientInterface client) throws RemoteException;
    
    public ArrayList<MyTubeFile> getMyFiles(ClientInterface client) throws RemoteException;
    
    public boolean uploadFile(ClientInterface client, MyTubeFile file) throws RemoteException;
    public boolean deleteFile(ClientInterface client, String title) throws RemoteException;
    public boolean modifyTitle(ClientInterface client, String title, String newTitle) throws RemoteException;
    public boolean modifyDescription(ClientInterface client, String title, String description) throws RemoteException;
}  