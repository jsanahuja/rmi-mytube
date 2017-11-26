/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import rmi.client.ClientInterface;
import rmi.common.MyTubeFile;

/**
 *
 * @author BanNsS1
 */
public class ServerImplementation extends UnicastRemoteObject implements ServerInterface{
    
    private final ArrayList<ServerInterface> servers;
    private final ArrayList<ClientInterface> clients;
    private final ArrayList<MyTubeFile> files;
    private final String uploadsPath;
    private final String ip;
    private final int port;
    private final int id;
    
    public ServerImplementation(String uploadsPath, String ip, int port, int id) throws RemoteException{
        super();
        this.servers = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.files = new ArrayList<>();
        this.uploadsPath = uploadsPath;
        this.ip = ip;
        this.port = port;
        this.id = id;
        
        new File(uploadsPath).mkdir();
    }
    
    ///////////////////////////////////
    //////////PRIVATE METHODS//////////
    ///////////////////////////////////
    
    //FILESYSTEM CONTROL//
    private boolean saveFile(ClientInterface client, MyTubeFile file){
        try{
            byte[] content = client.readFile(file.getFilename());
            String savepath = uploadsPath+"/"+Integer.toString(file.getId());
            new File(savepath).mkdir();
            savepath += "/"+ file.getFilename();

            try (FileOutputStream fos = new FileOutputStream(savepath)) {
                fos.write(content);
                return true;
            }
        }catch(IOException e){
            return false;
        }
    }
    
    private byte[] readFile(MyTubeFile file) throws IOException{
        String path = new File("").getAbsolutePath();
        path += "/" + uploadsPath + "/" + Integer.toString(file.getId()) + "/" + file.getFilename();
        return Files.readAllBytes(Paths.get(path));
    }
    
    private void rmFile(MyTubeFile file){
        String path = new File("").getAbsolutePath();
        path += "/" + uploadsPath + "/" + Integer.toString(file.getId());
        
        File f = new File(path + "/" + file.getFilename());
        File fo = new File(path);
        f.delete();
        fo.delete();
    }  
        
    //CLIENT ERRORS
    private void usernameAlreadyExists(ClientInterface client) throws RemoteException {
        client.sendMessage("REGISTER: The entered username is taken.");
    }
    private void alreadyRegistered(ClientInterface client) throws RemoteException {
        client.sendMessage("You are already registered");
    }
    private void registerRequired(ClientInterface client) throws RemoteException {
        client.sendMessage("You have to register first to perform that action.");
    }
    private void titleAlreadyExists(ClientInterface client) throws RemoteException {
        client.sendMessage("The entered title already belongs to an other file");
    }
    private void serverFSError(ClientInterface client) throws RemoteException {
        client.sendMessage("ERROR: File couldn't be upload. Please, try again.");
        System.out.println("ERROR: FOS Exception while uploading a file");
    }
    private void fileDoesNotExist(ClientInterface client) throws RemoteException {
        client.sendMessage("The selected file does not exist.");
    }
    private void notAllowedToDoThat(ClientInterface client) throws RemoteException {
        client.sendMessage("You don't have enough permissions to perform that action.");
    }
    //CLIENT SUCCESS
    private void registered(ClientInterface client) throws RemoteException {
        client.sendMessage("REGISTER: Successfully registered. Welcome "+client.getUsername());
        System.out.println("The client '"+client.getUsername()+"' just registered");
    }
    private void disconnected(ClientInterface client) throws RemoteException {
        client.sendMessage("DISCONNECT: Successfully disconnected.");
        System.out.println("The client '"+client.getUsername()+"' just disconnected and all its files were removed");
    }
    
    private void fileUploaded(ClientInterface client, MyTubeFile file) throws RemoteException {
        client.sendMessage("SERVER: File '"+file.getTitle()+"' uploaded successfully");
        System.out.println("File "+file.getTitle()+" uploaded by "+client.getUsername());
    }
    private void fileDeleted(ClientInterface client, String title) throws RemoteException {
        client.sendMessage("File '"+title+"' deleted.");
        System.out.println(client.getUsername()+" deleted the file '"+title+"'.");
    }
    private void fileTitleModified(ClientInterface client, String title, String newTitle) throws RemoteException {
        client.sendMessage("The title of the file is now '"+newTitle+"'.");
        System.out.println(client.getUsername()+" changed the title of its file '"+title+"' to '"+newTitle+".'");
    }
    private void fileDescriptionModified(ClientInterface client, MyTubeFile file, String description) throws RemoteException {
        client.sendMessage("The description of the file is now '"+file.getDescription()+"'.");
        System.out.println(client.getUsername()+" changed the description of its file '"+file.getTitle()+"' from '"+description+"' to '"+file.getDescription()+"'.");
    }
    
    ///////////////////////////////////
    /////// SERVERS CONNECTION ////////
    ///////////////////////////////////
    @Override
    public boolean serverRegister(String ip, int port, int id) throws RemoteException {
        try {
            if(this.ip.equals(ip) && this.port == port){
                return false;
            }else if(this.id == id){
                return false;
            }
            
            String url = "rmi://"+ip+":"+port+"/mytube";
            ServerInterface server = (ServerInterface) Naming.lookup(url);
                        
            if(server.acceptRegister(this.ip, this.port, this.id)){
                servers.add(server);
                //Check if servers are really bidirectionally connected.
                if(server.serverPing(this) == this.id){
                    System.out.println("CONNECT: Bidirectionally connected to the server "+url);
                    return true;
                }
            }
            return false;
        } catch (NotBoundException | MalformedURLException | RemoteException e){
            return false;
        }
    }
    @Override
    public boolean acceptRegister(String ip, int port, int id) throws RemoteException {
        try {
            String url = "rmi://"+ip+":"+port+"/mytube";
            ServerInterface server = (ServerInterface) Naming.lookup(url);
            servers.add(server);
            System.out.println("CONNECT: Accepted connection request to the server "+url);
            return true;
        } catch (NotBoundException | MalformedURLException | RemoteException e){
            System.out.println("ERROR: Server connection refused #"+id);
            System.out.println("\t Exception: "+e.toString());
            return false;
        }
    }
    
    @Override
    public int serverPing(ServerInterface server) throws RemoteException {
        if(servers.contains(server)){
            return server.serverPong();
        }
        return -1;
    }
    @Override
    public int serverPong() throws RemoteException {
        return this.id;
    }
    
    ///////////////////////////////////
    /////// CLIENTS COMUNICATION //////
    ///////////////////////////////////
    @Override
    public boolean register(ClientInterface client) throws RemoteException {
        if(!clients.contains(client)){
            client.setId(client.getUsername().hashCode());
            if(!global_userExists(client.getId())){
                clients.add(client);
                registered(client);
                return true;
            }else{
                usernameAlreadyExists(client);
            }
        }else{
            alreadyRegistered(client);
        }
        return false;
    }
    
    @Override
    public void disconnect(ClientInterface client) throws RemoteException {
        if(clients.contains(client)){
            disconnected(client);
            clients.remove(client);
            for(MyTubeFile file : files){
                if(file.getOwner() == client.getId()){
                    rmFile(file);
                    files.remove(file);
                }
            }
        }
    }

    @Override
    public boolean uploadFile(ClientInterface client, MyTubeFile file) throws RemoteException {
        if(clients.contains(client)){
            file.setOwner(client.getId());
            file.setId(file.getTitle().hashCode());
            if(!global_fileExists(file.getId())){
                if(saveFile(client, file)){
                    files.add(file);
                    global_notifyAll(file);
                    fileUploaded(client, file);
                    return true;
                }else{
                    serverFSError(client);
                }
            }else{
                titleAlreadyExists(client);
            }
        }else{
            registerRequired(client);
        }
        return false;
    }
    
    @Override
    public boolean deleteFile(ClientInterface client, String title) throws RemoteException{
        if(clients.contains(client)){
            int fid = title.hashCode();
            if(local_fileExists(this, fid)){
                MyTubeFile file = local_getFileById(fid);
                if(file.getOwner() == client.getId()){
                    files.remove(file);
                    rmFile(file);
                    fileDeleted(client, title);
                    return true;
                }else{
                    notAllowedToDoThat(client);
                }
            }else{
                fileDoesNotExist(client);
            }
        }else{
            registerRequired(client);
        }
        return false;
    }
    
    @Override
    public boolean modifyTitle(ClientInterface client, String title, String newTitle) throws RemoteException{
        if(clients.contains(client)){
            int fid = title.hashCode();
            int nfid = newTitle.hashCode();
            
            if(local_fileExists(this, fid)){
                if(!global_fileExists(nfid)){
                    MyTubeFile file = local_getFileById(fid);
                    if(file.getOwner() == client.getId()){
                        file.setTitle(newTitle);
                        file.setId(nfid);
                        fileTitleModified(client,title,newTitle);
                        return true;
                    }else{
                        notAllowedToDoThat(client);
                    }
                }else{
                    titleAlreadyExists(client);
                }
            }else{
                fileDoesNotExist(client);
            }
        }else{
            registerRequired(client);
        }
        return false;
    }
    
    @Override
    public boolean modifyDescription(ClientInterface client, String title, String description) throws RemoteException{
        if(clients.contains(client)){
            int fid = title.hashCode();
            if(local_fileExists(this, fid)){
                MyTubeFile file = local_getFileById(fid);
                if(file.getOwner() == client.getId()){
                    String prevDesc = file.getDescription();
                    file.setDescription(description);
                    fileDescriptionModified(client, file, prevDesc);
                    return true;
                }else{
                    notAllowedToDoThat(client);
                }
            }else{
                fileDoesNotExist(client);
            }
        }else{
            registerRequired(client);
        }
        return false;
    }

    @Override
    public ArrayList<MyTubeFile> getMyFiles(ClientInterface client) throws RemoteException {
        if(clients.contains(client)){
            ArrayList<MyTubeFile> result = new ArrayList<>();
            for(MyTubeFile f : files){
                if(f.getOwner() == client.getId()){
                    result.add(f);
                }
            }
            return result; 
        }else{
            registerRequired(client);
        }
        return null;
    }
    
    public MyTubeFile local_getFileById(int id) throws RemoteException{
        for(MyTubeFile file : files){
            if(file.getId() == id){
                return file;
            }
        }
        return (MyTubeFile) null;
    }
    
    @Override
    public ClientInterface local_getUserById(int id) throws RemoteException{
        for(ClientInterface client : clients){
            if(client.getId() == id){
                return client;
            }
        }
        return (ClientInterface) null;
    }
    
    @Override
    public String local_getFileName(ServerInterface server, int fid) throws RemoteException{
        if(server.equals(this) || servers.contains(server)){
            for(MyTubeFile f : files){
                if(f.getId() == fid)
                    return f.getFilename();
            }
        }
        return "";
    }
    
    //NOTIFY
    @Override
    public void local_notifyAll(ServerInterface server, String title, String owner) throws RemoteException{
        if(server.equals(this) || servers.contains(server)){
            for(ClientInterface client : clients){
                if(owner.hashCode() != client.getId())
                    client.sendMessage("New file '"+title+"' uploaded by "+owner);
            }
        }
    }
    @Override
    public void global_notifyAll(MyTubeFile file) throws RemoteException{
        ClientInterface owner = local_getUserById(file.getOwner());
        String username = owner.getUsername();
        String title = file.getTitle();

        this.local_notifyAll(this, title, username);
        for(ServerInterface server : servers){
            server.local_notifyAll(this, title, username);
        }
    }
    
    @Override
    public void local_serverDisconnect(ServerInterface server, int id) throws RemoteException{
        servers.remove(server);
        System.out.println("SERVER: Disconnected from #"+id+" due to shutdown.");
    }
    @Override
    public void global_serverDisconnect() throws RemoteException{
        for(ClientInterface client : clients){
            this.disconnect(client);
        }
        
        for(ServerInterface server : servers){
            server.local_serverDisconnect(this, this.id);
        }
    }
    
        //USER EXISTS (ID)
    @Override
    public boolean local_userExists(ServerInterface server, int id) throws RemoteException {
        if(server.equals(this) || servers.contains(server)){
            for(ClientInterface c : clients){
                if(c.getId() == id){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public boolean global_userExists(int id) throws RemoteException {
        if(this.local_userExists(this,id))
            return true;

        for(ServerInterface s: servers){
            if(s.local_userExists(this, id)){
                return true;
            }
        }
        return false;
    }
    
        //FILE EXISTS (ID)
    @Override
    public boolean local_fileExists(ServerInterface server, int id) throws RemoteException {
        if(server.equals(this) || servers.contains(server)){
            for(MyTubeFile f : files){
                if(f.getId() == id){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public boolean global_fileExists(int id) throws RemoteException {
        if(this.local_fileExists(this, id))
            return true;
        for(ServerInterface s: servers){
            if(s.local_fileExists(this, id)){
                return true;
            }
        }
        return false;
    }
    
        //LIST FILES BY DESCRIPTION
    @Override
    public ArrayList<MyTubeFile> local_listFilesByDescription(ServerInterface server, String description) throws RemoteException {
        if(server.equals(this) || servers.contains(server)){
            ArrayList<MyTubeFile> result = new ArrayList<>();
            for(MyTubeFile f : files){
                f.matchesDescription(description);
                if(f.matches > 0){
                    result.add(f);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }
    @Override
    public ArrayList<MyTubeFile> global_listFilesByDescription(ClientInterface client, String description) throws RemoteException{
        if(clients.contains(client)){
            ArrayList<MyTubeFile> result = new ArrayList<>();

            result.addAll(this.local_listFilesByDescription(this, description));

            for(ServerInterface server : servers){
                result.addAll(server.local_listFilesByDescription(this, description));
            }

            //Sorting elements
            ArrayList<MyTubeFile> sortedResult = new ArrayList<>();
            int max;
            MyTubeFile hmFile;
            
            while(!result.isEmpty()){
                max = 0;
                hmFile = null;
                for(MyTubeFile f : result){
                    if(f.matches >= max){
                        hmFile = f;
                        max = f.matches;
                    }
                }
                
                sortedResult.add(hmFile);
                result.remove(hmFile);

            }
            return sortedResult;
        
        }else{
            registerRequired(client);
        }
        return new ArrayList<>();
    }
    
    @Override
    public byte[] local_downloadFileByTitle(ServerInterface server, String title) throws RemoteException{
        if(server.equals(this) || servers.contains(server)){
            int fid = title.hashCode();
            for(MyTubeFile file : files){
                if(file.getId() == fid){
                    try {
                        return readFile(file);
                    } catch (IOException e) {
                        System.out.println("Exception at local_downloadFileByTitle: "+e.toString());
                        return null;
                    }
                }
            }
        }
        return null;
    }
    @Override
    public boolean global_downloadFileByTitle(ClientInterface client, String title) throws RemoteException{
        if(clients.contains(client)){
            int fid = title.hashCode();
            byte[] content = null;
            String filename = null;
            
            if(this.local_fileExists(this, fid)){
                content =  this.local_downloadFileByTitle(this, title);
                filename = this.local_getFileName(this, fid);
            }

            for(ServerInterface server : servers){
                if(server.local_fileExists(this, fid)){
                    content = server.local_downloadFileByTitle(this, title);
                    filename = server.local_getFileName(this, fid);
                }
            }
            
            if(content != null && filename != null){
                return client.writeFile(content, filename);
            }
        }else{
            registerRequired(client);
        }     
        return false;
    }

}
