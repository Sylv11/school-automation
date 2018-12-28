package com.example.sylvain.projetautomates;

import android.util.Log;

import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;

import java.util.concurrent.atomic.AtomicBoolean;

public class WriteTaskS7
{
    private final int DB_NUMBER = 5;
    private int bit;
    int start;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // Thread and Automate class to communicate with
    private Thread writeThread;
    private AutomateS7 plcS7;

    // S7Client
    private S7Client comS7;

    // Parameters of connection to communicate with the automaton
    private String[] parConnexion = new String[10];
    private byte[] wordCommand = new byte[10];

    public WriteTaskS7(int start){
        this.start = start;
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

    // Connection data
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
                // Write request in A.P.I. variable
                // WriteArea(memoryArea, datablock address, variable location, number of variable to transfer, data)
                Integer writePLC = comS7.WriteArea(S7.S7AreaDB, DB_NUMBER, start,1,wordCommand);

                // If the request succeed
                /*if(writePLC.equals(0)) {
                    Log.i("Writting in ", "DB" + String.valueOf(DB_NUMBER) + ".DBB" + String.valueOf(start) + "." + String.valueOf(bit));
                }*/

                /*try {
                    Thread.sleep(1000);
                }catch (Exception e) {e.printStackTrace();}*/
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
