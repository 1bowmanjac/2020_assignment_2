package sample;

import java.io.*;
import java.util.Scanner;
import java.net.Socket;

/**
 * Client class:
 * I think you can run this from anywhere
 * I mostly ran it from jack@Laptop:~/Documents/Programs/Java/Assignment_2/src/sample
 * look at main method for example on how to use
 */
public class Client {
    DataInputStream in;
    DataOutputStream out;
    public Client(Socket socket) throws IOException {
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        //create socket and client
        var socket = new Socket("localhost", 25505);
        Client client = new Client(socket);
        /**
         * command examples that worked for me are commented above their if statement
         */
        //command: java Client.java DIR
        if(args[0].equals("DIR")){
            System.out.println("Available Files: ");
            System.out.print(client.DIR());

        //command: java Client.java UPLOAD ~jack/Downloads/WordCounter2.zip
        } if(args[0].equals("UPLOAD")){
            File file = new File(args[1]);
            client.UPLOAD(file);

        //command: java Client.java DOWNLOAD WordCounter2.zip ~jack/Downloads/
        } if(args[0].equals("DOWNLOAD")){
            client.DOWNLOAD(args[1],args[2]);
        }

    }

    /**
     * returns a string of all available files from the server
     * @return String of filenames seperated by \n
     * @throws IOException
     */
    public String DIR() throws IOException {
        out.writeUTF("DIR");
        String fileList = in.readUTF();
        return fileList;
    }

    /**
     * Uploads a file to the server
     * @param file File that you want to be uploaded
     * @return String File Upload Confirmation
     * @throws IOException
     */
    public String UPLOAD(File file) throws IOException {
        out.writeUTF("UPLOAD\n" + file.getName());
        System.out.println(in.readUTF());

        //this should read from file in bite (ha) sized pieces
        //rather than loading the entire file to ram
        FileInputStream fileIn = new FileInputStream(file);
        int bytes=0;
        out.writeLong(file.length());
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileIn.read(buffer))!=-1){
            out.write(buffer,0,bytes);
            out.flush();
        }
        return in.readUTF();
    }

    /**
     * Downloads selected file from server
     * @param filename name of file to be downloaded
     * @param downloadLocation path of where you want it saved e.g  ~jack/Downloads/
     * @return String of downloaded file and confirmation
     * @throws IOException
     */
    public String DOWNLOAD(String filename,String downloadLocation) throws IOException {

        out.writeUTF("DOWNLOAD\n" + filename);
        out.flush();
        //sets filepath
        System.out.println(downloadLocation);
        File dir = new File (downloadLocation);
        File file = new File (dir, filename);


        //this should write to file in bite (ha) sized pieces
        //rather than loading the entire file to ram
        long size = in.readLong();
        FileOutputStream fileOut = new FileOutputStream(file);
        byte[] buffer = new byte[4*1024];
        int bytes = 0;
        while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOut.write(buffer,0,bytes);
            size -= bytes;
        }fileOut.close();
        out.writeUTF("successfully downloaded " + filename);
        return "downloaded " + filename;
    }
}
