package sample;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Server {
    private static ArrayList<String> fileList = new ArrayList<>();
    public static ArrayList<String> getFileList(){
        return fileList;
    }
    public static void addToFileList(String newFile){
        fileList.add(newFile);
    }
    public static void main(String[] args) throws Exception {
        addToFileList("first File");
        try (var listener = new ServerSocket(25505)) {
            System.out.println("The server is running...");
            var pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new instance(listener.accept()));
            }
        }
    }


}

class instance implements Runnable {
    private Socket socket;

    instance(Socket socket) {
        System.out.println("new thread created");
        this.socket = socket;
    }
    @Override
    public void run() {
        System.out.println("Connected: " + socket);
        try {
            var in = new Scanner(socket.getInputStream());
            var out = new PrintWriter(socket.getOutputStream(), true);
            while (in.hasNextLine()) {
                String arg = in.nextLine();
                System.out.println(arg);
                if(arg.equals("DIR")){
                    for (int i=0;i < Server.getFileList().size();i++){
                        out.println(Server.getFileList().get(i));
                    }
                    break;
                } if(arg.equals("UPLOAD")){
                    Server.addToFileList(in.nextLine());
                    break;
                } if(arg.equals("DOWNLOAD")){
                    String fileName = in.nextLine();
                    for(int i = 0; i < Server.getFileList().size();i++){
                        if(Server.getFileList().get(i).equals(fileName)){
                            out.println(fileName);
                            break;
                        }
                    }
                    break;
                } else {
                    out.println("not a recognized command");
                }
            }
        } catch (Exception e) {
            System.out.println("Error:" + socket);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
            System.out.println("Closed: " + socket);
        }
    }
    private void addToFileList(String newFile){

    }
}

