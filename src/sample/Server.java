package sample;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;

/**
 * YOU MUST RUN SERVER FROM ASSIGNMENT 2 FOLDER
 * E.G: jack@Laptop:~/Documents/Programs/Java/Assignment_2$ java src/sample/Server.java
 */
public class Server {
    /**
     *
     * @return string of files seperated by \n
     */
    public static String getFileList(){
        String fileList = "";
        try {
            File myObj = new File("filenames.txt");
            Scanner reader = new Scanner(myObj);
            while (reader.hasNextLine()) {
                fileList  += reader.nextLine() + "\n";
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return fileList;
    }

    /**
     * adds file to list of files
     * @param newFile string name of file to be added
     */
    public static void addToFileList(String newFile){
        try(FileWriter fw = new FileWriter("filenames.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(newFile);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }
    public static void main(String[] args) throws Exception {

        try (var listener = new ServerSocket(25505)) {
            System.out.println("The server is running...");
            var pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new instance(listener.accept()));
            }
        }
    }


}
//one instance per connection
class instance implements Runnable {
    private Socket socket;

    instance(Socket socket) {
        System.out.println("new thread created");
        this.socket = socket;
    }
    @Override
    public void run() {
        System.out.println("Connected: " + socket);
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
             in = new DataInputStream(socket.getInputStream());
             out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
                /**
                 * format of a command is one argument per line
                 * first line is always the command eg DIR, UPLOAD, DOWNLOAD
                 * the following is what the client sends:
                 *      "UPLOAD" + "\n" + "filename"
                 *      "DIR"
                 *      "DOWNLOAD" + "\n" + "filename"
                 */
                String args[] = in.readUTF().split("\\r?\\n");
                System.out.println(args[0]);

                if(args[0].equals("DIR")){
                    out.writeUTF(Server.getFileList());
                }
                else if(args[0].equals("UPLOAD")){
                    String filename = args[1];
                    out.writeUTF("Got Filename");
                    out.flush();
                    //sets filepath
                    File dir = new File ("Server_Files");
                    File file = new File (dir, filename);
                    System.out.println("filepath = " + file.getAbsolutePath());


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

                    //update fileList
                    Server.addToFileList(filename);
                    out.writeUTF("successfully uploaded " + filename);

                }
                else if(args[0].equals("DOWNLOAD")){
                    String filename = args[1];
                        File dir = new File ("Server_Files");
                        File file = new File (dir, filename);
                    System.out.println("filepath = " + file.getAbsolutePath());

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
                    System.out.println(in.readUTF());
                } else {
                    out.writeUTF("not a recognized command");
                }
// messy error catches do not touch
        } catch (Exception e) {
            System.out.println("Error:" + e);
            try {
                out.writeUTF(e.toString());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
            System.out.println("Closed: " + socket);
        }
    }

}

