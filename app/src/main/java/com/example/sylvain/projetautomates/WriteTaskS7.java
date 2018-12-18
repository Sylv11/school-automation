package com.example.sylvain.projetautomates;

import android.util.Log;

import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;

import java.util.concurrent.atomic.AtomicBoolean;

public class WriteTaskS7
{
    private final int DB_NUMBER = 5;
    private final int START = 5;
    private int bit;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // Thread and Automate class to communicate with
    private Thread writeThread;
    private AutomateS7 plcS7;

    // S7Client
    private S7Client comS7;

    // Parameters of connection to communicate with the automaton
    private String[] parConnexion = new String[10];
    private byte[] wordCommand = new byte[10];

    public WriteTaskS7(){
        comS7 = new S7Client();
        plcS7 = new AutomateS7();
        writeThread = new Thread(plcS7);
    }

    // Stop the thread and disconnect to the automaton
    public void stop(){
        isRunning.set(false);
        comS7.Disconnect();
        writeThread.interrupt();
    }

    // Connection informations
    public void start(String ip, String rack, String slot){
        if (!writeThread.isAlive()) {
            parConnexion[0] = ip;
            parConnexion[1] = rack;
            parConnexion[2] = slot;
            writeThread.start();
            isRunning.set(true);
        }
    }

    private class AutomateS7 implements Runnable {

        private Integer response;

        @Override
        public void run() {

            // Connection to the automaton
            try {
                comS7.SetConnectionType(S7.S7_BASIC);
                this.response = comS7.ConnectTo(parConnexion[0],
                        Integer.valueOf(parConnexion[1]),Integer.valueOf(parConnexion[2]));
            }catch (Exception e) {
                e.printStackTrace();
            }

            while(isRunning.get() && (response.equals(0))){
                // Write request in API variable
                // WriteArea(memoryArea, datablock address, variable lcoation, number of variable to transfer, data)
                Integer writePLC = comS7.WriteArea(S7.S7AreaDB, DB_NUMBER, START,1,wordCommand);

                // If the request succeed
                if(writePLC.equals(0)) {
                    Log.i("Writting in ", "DB" + String.valueOf(DB_NUMBER) + ".DBB" + String.valueOf(START) + "." + String.valueOf(bit));
                }
            }
        }
    }

    public void setWriteBool(int b, int v){
    // Masking
        this.bit = (b == 1) ? 0 : b / 2;
        // Activate ( | => OR)
        if(v==1) wordCommand[0] = (byte)(b | wordCommand[0]);
        // Deactivate ( ~ => dual, & => AND)
        else wordCommand[0] = (byte)(~b & wordCommand[0]);
    }
}
