package sample;

import java.io.PrintWriter;
import java.util.Scanner;
import java.net.Socket;
import java.io.IOException;

/**
 * A command line client for the date server. Requires the IP address of the
 * server as the sole argument. Exits after printing the response.
 */
public class Client {
    public static void main(String[] args) throws IOException {

        var socket = new Socket("localhost", 25505);
        var in = new Scanner(socket.getInputStream());
        var out = new PrintWriter(socket.getOutputStream(), true);

        String command = "";
        for (String item:
             args) {
            command += item + "\n";
        }
        out.println(command);
        while (in.hasNextLine()){
            System.out.println("Server response: " + in.nextLine());
        }

    }
}
