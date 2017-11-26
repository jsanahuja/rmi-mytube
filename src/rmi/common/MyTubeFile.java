/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi.common;
import java.io.Serializable;
/**
 *
 * @author BanNsS1
 */
public class MyTubeFile implements Serializable{
    private String title;
    private String description;
    private int owner;
    private int id;
    
    //Temporary  value for listing files by relevance.
    public int matches;
    
    private final String filename;
    
    public MyTubeFile(String title, String description, String filename){
        this.title = title;
        this.description = description;
        this.filename = filename;
    }
    
    public int matchesDescription(String description){
        String[] fdesc = this.description.split("\\s");
        String[] cdesc = description.split("\\s");
        
        this.matches = 0;
        
        //counts matching words
        for(String word1 : fdesc){
            for(String word2 : cdesc){
                if(word1.equals(word2)){
                    this.matches++;
                }
            }
        }        
        return this.matches;
    }
    
    //SETTERS
    public void setTitle(String title){
        this.title = title;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public void setOwner(int owner){
        this.owner = owner;
    }
    
    public void setId(int id){
        this.id = id;
    }
        
    //GETTERS
    public String getTitle(){
        return this.title;
    }
    
    public String getDescription(){
        return this.description;
    }
    
    public int getOwner(){
        return this.owner;
    }
    
    public int getId(){
        return this.id;
    }
    
    public String getFilename(){
        return this.filename;
    }
        
}
