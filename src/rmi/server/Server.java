/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 *
 * @author BanNsS1
 */
public class Server {
    private static final String IP = "localhost";
    private static final int PORT = 65535;
    private static final int ID = 1;
    private static final String SAVE_PATH = "server_uploads";
    private static final String YES = "y";
    private static final String NO = "n";
    
    public static void main(String args[]) {
        try{
            //Server url params.
            System.out.println("Setting up the server.");
            String ip = askForIP();
            int port = askForPort();
            int id = askForId();
            
            //Initializing Server object.
            ServerImplementation server = new ServerImplementation(SAVE_PATH, ip, port, id);
            
            //Starting server.
            startRegistry(port);
            String url = "rmi://"+ip+":"+port+"/mytube";
            Naming.rebind(url, server);
                    
            
            //Asking to connect to other servers.
            String answer = askIfConnectServers();
            while(answer.equals(YES)){
                String s_ip = askForIP();
                int s_port = askForPort();
                int s_id = askForId();
                if(!server.serverRegister(s_ip, s_port, s_id)){
                    System.out.println("\t\tERROR: Couldn't connect to the server.");
                }else{
                    System.out.println("\t\tSUCCESS: Connected to rmi://"+s_ip+":"+s_port+"/mytube");
                }
                answer = askIfConnectServers();
            }
            
            System.out.println("\nServer running at: "+url);
            System.out.println("Mytube service server #"+id+" is online!");
            
            Runtime.getRuntime().addShutdownHook(
                new Thread(){
                    @Override
                    public void run(){
                        try {
                            server.global_serverDisconnect();
                        } catch (RemoteException ex) {
                            System.out.println("ERROR: Couldn't execute end program clean");
                        }
                    }
                }
            );
            
        }catch(Exception e){
            System.out.println("ERROR: An error ocurred.");
            System.out.println("\t Exception: "+e.toString());
        }
    }
    
    private static String askForIP(){
        String ip = askForInput("\tSERVER IP (Default: "+IP+"):");
        if(!ip.equals("")){
            return ip;
        }
        return IP;
    }
    
    private static int askForPort(){
        String port = askForInput("\tSERVER PORT (Default: "+PORT+"): ");
        if(!port.equals("")){
            return Integer.parseInt(port);
        }
        return PORT;
    }
    
    private static int askForId(){
        String id = askForInput("\tSERVER ID (Default: "+ID+"): ");
        if(!id.equals("")){
            return Integer.parseInt(id);
        }
        return ID;
    }
    
    private static String askIfConnectServers(){
        String answer = askForInput("\tDo you want to connect this server to other servers? ("+YES+"/"+NO+"): ");
        if(answer.equals(YES) || answer.equals(NO)){
            return answer;
        }
        return askIfConnectServers();
    }
    
    private static String askForInput(String feed){
        Scanner scn = new Scanner (System.in);
        String input;
        
        System.out.print(feed);
        input = scn.nextLine();
        
        return input;
    }
    
    public static void startRegistry(int port) throws RemoteException{
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list();
        }catch(RemoteException ex){
            Registry registry = LocateRegistry.createRegistry(port);
        }
    }
}
