package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamReaderThread extends Thread {

    private BufferedReader reader;
    private String output;
    
    public StreamReaderThread(InputStream stream) {
        this.reader = new BufferedReader(new InputStreamReader(stream));
        this.output = "";
    }
    
    public String getOutput() throws InterruptedException {
        return this.output;
    }
    
    @Override
    public void run()
    {
        try {
            /* Read the output from the stream. */
            String o = null;
            while ((o = this.reader.readLine()) != null) {
                output += o;
            }
        } catch (IOException e) {
            System.err.println("Exception occurred while reading stream input.");
        } 
    }
}
