/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author BanNsS1
 */
public class ClientImplementation extends UnicastRemoteObject implements ClientInterface{
    private final String username;
    private int id;
    
    private static String SAVEPATH = "client_downloads";
    
    public ClientImplementation(String username) throws RemoteException{
        super();
        this.username = username;
    }
    

    @Override
    public void sendMessage(String msg) throws RemoteException {
        System.out.println(msg);
    }
    
    @Override
    public byte[] readFile(String relpath) throws RemoteException, IOException{
        String path = new File("").getAbsolutePath();
        return Files.readAllBytes(Paths.get(path +"/"+ relpath));
    }
    
    @Override
    public boolean writeFile(byte[] content, String relpath) throws RemoteException{
        try{
            String filepath = SAVEPATH +"/"+ relpath;

            try (FileOutputStream fos = new FileOutputStream(filepath)) {
                fos.write(content);
                return true;
            }
        }catch(IOException e){
            return false;
        }
    }

    @Override
    public String getUsername() throws RemoteException {
        return username;
    }
    
    @Override
    public void setId(int id) throws RemoteException {
        this.id = id;
    }
    
    @Override
    public int getId() throws RemoteException {
        return id;
    }
}
