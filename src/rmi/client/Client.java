/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi.client;

import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;
import rmi.common.MyTubeFile;
import rmi.server.ServerInterface;

/**
 *
 * @author BanNsS1
 */
public class Client {
    private static final String IP = "localhost";
    private static final int PORT = 65535;
    private static final int[] ALLOWED_ACTIONS = new int[]{1,2,3,4,5,6,7,8};
    private static boolean connected = false;
    
    public static void main(String args[]) {
        try {
            System.out.println("Connect to the server.");
            String ip = askForIP();
            int port = askForPort();
            ClientImplementation client = new ClientImplementation(askForUsername());
            
            String url = "rmi://"+ip+":"+port+"/mytube";
            ServerInterface server = (ServerInterface) Naming.lookup(url);
            
            System.out.println("\nCONNECT: Successfully connected to "+url);
            
            if(!server.register(client)){
                System.exit(0);
            }else{
                connected = true;
            }
            
            System.out.println("");
            
            Runtime.getRuntime().addShutdownHook(
                new Thread(){
                    @Override
                    public void run(){
                        try {
                            server.disconnect(client);
                        } catch (RemoteException ex) {
                            System.out.println("ERROR: Couldn't execute end program clean.");
                        }
                    }
                }
            );
            
            int option;
            while(true){
                option = askForOption();
                switch(option){
                    
                    // 2 list my 3 mod title 4 mod desc 5 delete
                    
                    case 1:
                        System.out.println("OPTION 1: Selected upload file.");
                        String title = askForTitle();
                        String description = askForDescription();
                        String path = askForFilePath();
                        MyTubeFile file = new MyTubeFile(title, description, path);
                        server.uploadFile(client, file);
                        break;
                    case 2:
                        System.out.println("OPTION 2: List my files.");
                        ArrayList<MyTubeFile> list_myfiles = server.getMyFiles(client);
                        printFileList(list_myfiles,
                                "OPTION 2: Listing your files",
                                "OPTION 2: You have not upload any file yet!"
                                );
                        break;
                    case 3:
                        System.out.println("OPTION 3: Modify file title");
                        String title_modifyt1 = askForTitle("Current Title");
                        String title_modifyt2 = askForTitle("New Title");
                        server.modifyTitle(client, title_modifyt1, title_modifyt2);
                        break;
                    case 4:
                        System.out.println("OPTION 4: Modify file description");
                        String title_modifyd = askForTitle();
                        String description_modifyd = askForDescription("New description");
                        server.modifyDescription(client, title_modifyd, description_modifyd);
                        break;
                    case 5:    
                        System.out.println("OPTION 5: Delete file");
                        String title_delete = askForTitle();
                        server.deleteFile(client, title_delete);
                        break;
                    case 6:
                        System.out.println("OPTION 6: Selected search file.");
                        String search_description = askForDescription();
                        ArrayList<MyTubeFile> list_search = server.global_listFilesByDescription(client, search_description);
                        printFileList(list_search, 
                                "\tOPTION 6: Listing files matching '"+ search_description +"' description.",
                                "\tOPTION 6: Couldn't find any files matching '"+ search_description+"' description.");
                        break;
                    case 7:
                        System.out.println("OPTION 7: Download file.");
                        String title_download = askForTitle();
                        if(server.global_downloadFileByTitle(client, title_download)){
                            System.out.println("\tOPTION 7: File downloaded successfully");
                        }else{
                            System.out.println("\tOPTION 7: Unexpected Error: Couldn't download the file.");
                        }
                        break;
                    case 8:
                        System.out.println("OPTION 8: Disconnect");
                        server.disconnect(client);
                        System.exit(0);
                        break;
                }
                
                System.out.println("");
            }
        } catch (RemoteException | NotBoundException e){
            System.out.println("ERROR: Couldn't connect to the server.");
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("ERROR: Couldn't access that file.");
            System.out.println("\tException:"+e.toString());
            e.printStackTrace();
        }
    }
    
    private static void printOptions(){
        System.out.println("--------------------------------------");
        System.out.println("What action would you like to perform?");
        System.out.println("\t1: Upload File\t\t2: List My Files\t\t3:Modify file Title");
        System.out.println("\t4: Modify file Description\t\t5:Delete File\t\t6:Search File");
        System.out.println("\t7: Download File\t\t8:Disconnect");
    }
    
    private static void printFileList(ArrayList<MyTubeFile> list, String header, String header_error){
        if(list != null && !list.isEmpty()){
            System.out.println(header);
            for(MyTubeFile f : list){
                System.out.println("\t- "+f.getTitle() +" - "+ f.getDescription());
            }
        }else{
            System.out.println(header_error);
        }
    }
    
    private static String askForTitle(){
        String title = askForInput("\tFile Title:");
        if(!title.equals("")){
            return title;
        }
        return askForTitle();
    }
    
    private static String askForTitle(String extra){
        String title = askForInput("\tFile Title ("+extra+":");
        if(!title.equals("")){
            return title;
        }
        return askForTitle();
    }    
    
    private static String askForDescription(){
        String description = askForInput("\tFile Description:");
        if(!description.equals("")){
            return description;
        }
        return askForDescription();
    }    
    
    private static String askForDescription(String extra){
        String description = askForInput("\tFile Description ("+extra+"):");
        if(!description.equals("")){
            return description;
        }
        return askForDescription();
    }
    
    private static String askForFilePath(){
        String abspath = new File("").getAbsolutePath();
        String path = askForInput("\tFile path (must be relative to "+abspath+"): ");
        if(!path.equals("")){
            File f = new File(path);
            if(f.exists() && !f.isDirectory()) { 
                return path;
            }
        }
        return askForFilePath();
    }
    
    private static String askForUsername(){
        String username = askForInput("\tChoose your username: ");
        if(!username.equals("")){
            return username;
        }
        return askForUsername();
    }
    
    private static String askForIP(){
        String ip = askForInput("\tSERVER IP (Default: "+IP+"): ");
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
    
    private static int askForOption(){
        printOptions();
        String option = askForInput("Type the option code here: ");
        if(!option.equals("")){
            int value = Integer.parseInt(option);
            for(int action : ALLOWED_ACTIONS){
                if(action == value)
                    return value;
            }
        }
        return askForOption();
    }
    
    private static String askForInput(String feed){
        Scanner scn = new Scanner (System.in);
        String input;
        
        System.out.print(feed);
        input = scn.nextLine();
        
        return input;
    }
}
