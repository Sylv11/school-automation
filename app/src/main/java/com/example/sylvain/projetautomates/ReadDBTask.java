package com.example.sylvain.projetautomates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;

import org.w3c.dom.Text;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReadDBTask
{
    // DB number and progress update message
    private final int DB_NUMBER = 5;
    private static final int MESSAGE_PROGRESS_UPDATE = 2;

    // AtomicBoolean for the thread
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // Components to change
    private TextView tv_pharma_conveyor_state;
    private TextView tv_pharma_read_number_tablets;

    // S7Client, Thread, AutomatesS7, param and data
    private AutomateS7 plcS7;
    private Thread readThread;
    private S7Client comS7;
    private String[] param = new String[10];
    private byte[] datasPLC = new byte[512];

    // DBB number
    private int start;

    public ReadDBTask(int start, TextView tv_pharma_conveyor_state, TextView tv_pharma_read_number_tablets){
        this.start = start;
        this.tv_pharma_conveyor_state = tv_pharma_conveyor_state;
        this.tv_pharma_read_number_tablets = tv_pharma_read_number_tablets;
        comS7 = new S7Client();
        plcS7 = new AutomateS7();
        readThread = new Thread(plcS7);
    }

    // Stop the thread and disconnect to the automaton
    public void stop(){
        isRunning.set(false);
        comS7.Disconnect();
        readThread.interrupt();
    }

    // Connection informations
    public void start(String ip, String rack, String slot){
        if (!readThread.isAlive()) {
            param[0] = ip;
            param[1] = rack;
            param[2] = slot;
            readThread.start();
            isRunning.set(true);
        }
    }

    // Send the informations to the dashboard and change some properties
    @SuppressLint("SetTextI18n")
    private void downloadOnProgressUpdate(boolean[] bitStatus) {
        if(start == 0) {
            if(bitStatus[0]) {
                this.tv_pharma_conveyor_state.setText("En marche");
                this.tv_pharma_conveyor_state.setTextColor(Color.GREEN);
            }else {
                this.tv_pharma_conveyor_state.setText("Stoppé");
                this.tv_pharma_conveyor_state.setTextColor(Color.RED);
            }
        }

        if(start == 4) {
            if(bitStatus[3]){
                this.tv_pharma_read_number_tablets.setText("5");
                this.tv_pharma_read_number_tablets.setTextColor(Color.GRAY);
            }else if(bitStatus[4]){
                this.tv_pharma_read_number_tablets.setText("10");
                this.tv_pharma_read_number_tablets.setTextColor(Color.GRAY);
            }else if(bitStatus[5]){
                this.tv_pharma_read_number_tablets.setText("15");
                this.tv_pharma_read_number_tablets.setTextColor(Color.GRAY);
            }
        }

    }

    // Run method according to the message
    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_PROGRESS_UPDATE:
                    downloadOnProgressUpdate((boolean[])msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private class AutomateS7 implements Runnable{
        // Result of the connection
        private Integer res;

        // Array of boolean for the byte
        boolean[] bitsStatus = new boolean[8];

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            // Try to connect
            try {
                comS7.SetConnectionType(S7.S7_BASIC);
                this.res = comS7.ConnectTo(param[0], Integer.valueOf(param[1]), Integer.valueOf(param[2]));
            }catch(Exception e){
                e.printStackTrace();
            }

            try {
                // Loop
                while(isRunning.get()){
                    // If the connection succeed
                    if (res.equals(0)){
                        // ReadArea(int Area, int DBNumber, int Start, int Amount, byte[] Data)
                        // Read data from PLC
                        int retInfo = comS7.ReadArea(S7.S7AreaDB, DB_NUMBER, start,1, datasPLC);

                        // If succeed
                        if (retInfo == 0) {

                            // We read each bit in the byte
                            for(int i=0; i<=7; i++){
                                bitsStatus[i] = S7.GetBitAt(datasPLC, 0, i);
                            }
                            sendProgressMessage(bitsStatus);
                        }
                        //Log.i("Variable A.P.I. -> ", String.valueOf(bitState));
                    }
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        // Send message to the handler
        private void sendProgressMessage(boolean[] bitsStatus) {
            Message progressMsg = new Message();
            progressMsg.what = MESSAGE_PROGRESS_UPDATE;
            progressMsg.obj = bitsStatus;
            myHandler.sendMessage(progressMsg);
        }
    }
}
