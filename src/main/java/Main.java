import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStream;
import java.io.*;
import java.nio.file.Files;
import java.io.File;


public class Main {
  public static void main(String[] args) {
	 System.out.println("Logs from your program will appear here!");
     ServerSocket serverSocket = null;
     String directory = null;
     if(args.length == 2 && args[0].equals("--directory")) {
    	 directory = args[1];
     }
     
     
     try {
       serverSocket = new ServerSocket(4221);
       serverSocket.setReuseAddress(true);
       try {
    	   final String d= directory;
    	   while(true) {
    		   var accept = serverSocket.accept();
    		   Thread.ofVirtual().start(()-> {
    			   try {
    				   getRes(accept,d);
    			   }catch (IOException e){
    				   throw new RuntimeException(e);
    			   }
    		   });
    	   }
       }finally { 	   
    	   serverSocket.close();

     } 
  }catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
     
 }
  
  private static void getRes(Socket clientSocket, String directory) throws IOException{
	  System.out.println("accepted new connection");
	  System.out.println("accepted new connection" + directory);
      BufferedReader in = new BufferedReader(
   	          new InputStreamReader(clientSocket.getInputStream()));
      OutputStream outStream = clientSocket.getOutputStream();
      String response = null;
      String line = in.readLine();
      String[] parts = line.split(" ");
      String method = parts[0];
      String path = parts[1];
      
      if (path.equals("/")) {
   	   response = "HTTP/1.1 200 OK\r\n\r\n";
      }else if(path.startsWith("/echo/")) {
   	   String substring =
   			   path.startsWith("/echo/")? path.substring(6) : path.substring(5);
   	   String contentype = "Content-type: text/plain \r\n";
   	   String contentlength = "Content-Length: " + substring.length();
   	   
   	   response = "HTTP/1.1 200 OK\r\n" + contentype + contentlength + "\r\n\r\n" + substring + "\r\n";
   	   
   	   
      }else if(path.startsWith("/user-agent")){
   	   String userAgent ="";
   	   String contentype = "Content-type: text/plain \r\n";
   	  
   	   while(!line.isEmpty()) {
   		   line = in.readLine();
   		   if(line.contains("User-Agent")) {
   			   userAgent = line.split(": ")[1];
   		   }
   	   }
   	   
   	   userAgent.trim();
   	   String contentlength = "Content-Length: "+userAgent.length() + "\r\n";
   	   response = "HTTP/1.1 200 OK\r\n" + contentype + contentlength + "\r\n" + userAgent;
   	   
   	   	
   	   
      }else if(path.startsWith("/files/")){
    	  String filename = path.substring(7);
    	  File file = new File(directory,filename);
    	  
    	  if(file.exists()) {
    		  byte[] fileContents = Files.readAllBytes(file.toPath());
    		  System.out.println(new String(fileContents));
    		  response ="HTTP/1.1 200 OK\r\nConnection: close\\r\\nContent-Type: application/octet-stream\\r\\nContent-Length: " +fileContents.length + "\r\n\r\n" + new String(fileContents);
    	  }
    	  
      }else {
   	   response ="HTTP/1.1 404 Not Found\r\n\r\n";
      }
      outStream.write(response.getBytes());
	  
	  
	  
  }
  
  
  
  
}
