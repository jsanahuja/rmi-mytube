/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi.client;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author BanNsS1
 */
public interface ClientInterface extends Remote{
    
    public void sendMessage(String msg) throws RemoteException;
    
    public byte[] readFile(String relpath) throws RemoteException, IOException;
    public boolean writeFile(byte[] content, String relpath) throws RemoteException;
    
    public String getUsername() throws RemoteException;
    public void setId(int id) throws RemoteException;
    public int getId() throws RemoteException;
}
