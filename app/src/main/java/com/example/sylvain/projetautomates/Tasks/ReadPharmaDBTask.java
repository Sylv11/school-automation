package com.example.sylvain.projetautomates.Tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;
import com.example.sylvain.projetautomates.Utils.ToastUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReadPharmaDBTask {
    // DB number and progress update message
    private static final int DB_NUMBER = 5;
    private static final int MESSAGE_PROGRESS_UPDATE = 2;
    private static final int MESSAGE_TABLETS_NUMBER_UPDATE = 3;
    private static final int MESSAGE_BOTTLES_NUMBER_UPDATE = 4;

    // AtomicBoolean for the thread
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // Components to change
    private TextView tv_pharma_conveyor_state;
    private TextView tv_pharma_read_number_tablets;
    private TextView tv_pharma_read_bottles_status;
    private TextView tv_pharma_read_live_tablets;
    private TextView tv_pharma_read_live_bottles;

    // Context of pharma
    private Context context;


    // S7Client, Thread, AutomatesS7, param and data
    private AutomateS7 plcS7;
    private Thread readThread;
    private S7Client comS7;
    private String[] param = new String[10];
    private byte[] datasPLC = new byte[512];
    private byte[] dataTablets = new byte[512];
    private byte[] dataBottles = new byte[512];

    // DBB number
    private int start;

    public ReadPharmaDBTask(int start, Context context,
                            TextView tv_pharma_conveyor_state,
                            TextView tv_pharma_read_number_tablets,
                            TextView tv_pharma_read_bottles_status,
                            TextView tv_pharma_read_live_tablets,
                            TextView tv_pharma_read_live_bottles) {
        this.start = start;
        this.context = context;
        this.tv_pharma_conveyor_state = tv_pharma_conveyor_state;
        this.tv_pharma_read_number_tablets = tv_pharma_read_number_tablets;
        this.tv_pharma_read_bottles_status = tv_pharma_read_bottles_status;
        this.tv_pharma_read_live_tablets = tv_pharma_read_live_tablets;
        this.tv_pharma_read_live_bottles = tv_pharma_read_live_bottles;

        comS7 = new S7Client();
        plcS7 = new AutomateS7();
        readThread = new Thread(plcS7);
    }

    // Stop the thread and disconnect to the automaton
    public void stop() {
        isRunning.set(false);
        comS7.Disconnect();
        readThread.interrupt();
    }

    // Connection data
    public void start(String ip, String rack, String slot) {
        if (!readThread.isAlive()) {
            param[0] = ip;
            param[1] = rack;
            param[2] = slot;
            readThread.start();
            isRunning.set(true);
        }
    }

    // Send the informations to the pharma activity and change some properties
    @SuppressLint("SetTextI18n")
    private void downloadOnProgressUpdate(boolean[] bitsStatus) {

        // If in DB5.DBB0
        if (start == 0) {
            // We read the first bit in the byte
            if (bitsStatus[0]) {
                this.tv_pharma_conveyor_state.setText("En marche");
                this.tv_pharma_conveyor_state.setTextColor(Color.GREEN);
            } else {
                this.tv_pharma_conveyor_state.setText("Stoppé");
                this.tv_pharma_conveyor_state.setTextColor(Color.RED);
            }
        }

        // If in DB5.DBB4
        if (start == 4) {
            // We read the third, fourth and fifth bit in the byte
            if (bitsStatus[3]) {
                this.tv_pharma_read_number_tablets.setText("5");
                this.tv_pharma_read_number_tablets.setTextColor(Color.GRAY);
            } else if (bitsStatus[4]) {
                this.tv_pharma_read_number_tablets.setText("10");
                this.tv_pharma_read_number_tablets.setTextColor(Color.GRAY);
            } else if (bitsStatus[5]) {
                this.tv_pharma_read_number_tablets.setText("15");
                this.tv_pharma_read_number_tablets.setTextColor(Color.GRAY);
            }
        }

        // If in DB5.DBB1
        if (start == 1) {
            if (bitsStatus[2]) ToastUtil.show(context, "Le compteur de flacons a été rénitialisé");

            // We read the third bit in the byte
            if (bitsStatus[3]) {
                this.tv_pharma_read_bottles_status.setText("Ouvert");
                this.tv_pharma_read_bottles_status.setTextColor(Color.GREEN);
            } else {
                this.tv_pharma_read_bottles_status.setText("Fermé");
                this.tv_pharma_read_bottles_status.setTextColor(Color.RED);
            }
        }
    }

    // Send the informations to the pharma activity and change some properties
    @SuppressLint("SetTextI18n")
    private void downloadOnNumberTabletsUpdate(Integer numberTablets) {
        this.tv_pharma_read_live_tablets.setTextColor(Color.GRAY);
        if (numberTablets >= 0)
            this.tv_pharma_read_live_tablets.setText(String.valueOf(numberTablets));
        else this.tv_pharma_read_live_tablets.setText("Valeur incorrecte");
    }

    // Send the informations to the pharma activity and change some properties
    @SuppressLint("SetTextI18n")
    private void downloadOnNumberBottlesUpdate(Integer numberBottles) {
        this.tv_pharma_read_live_bottles.setTextColor(Color.GRAY);
        if (numberBottles >= 0)
            this.tv_pharma_read_live_bottles.setText(String.valueOf(numberBottles));
        else this.tv_pharma_read_live_bottles.setText("Valeur incorrecte");
    }

    // Run method according to the message
    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_PROGRESS_UPDATE:
                    downloadOnProgressUpdate((boolean[]) msg.obj);
                    break;
                case MESSAGE_TABLETS_NUMBER_UPDATE:
                    downloadOnNumberTabletsUpdate(msg.arg1);
                    break;
                case MESSAGE_BOTTLES_NUMBER_UPDATE:
                    downloadOnNumberBottlesUpdate(msg.arg2);
                    break;
                default:
                    break;
            }
        }
    };

    private class AutomateS7 implements Runnable {
        // Result of the connection
        private Integer res;
        private Integer numberOfTablets = 0;
        private Integer numberOfBottles = 0;

        private boolean[] bitsStatus = new boolean[8];

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            // Try to connect
            try {
                comS7.SetConnectionType(S7.S7_BASIC);
                this.res = comS7.ConnectTo(param[0], Integer.valueOf(param[1]), Integer.valueOf(param[2]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // Loop
                while (isRunning.get()) {
                    // If the connection succeed
                    if (res.equals(0)) {
                        // ReadArea(int Area, int DBNumber, int Start, int Amount, byte[] Data)
                        // Read data from PLC
                        int retInfo = comS7.ReadArea(S7.S7AreaDB, DB_NUMBER, start, 1, datasPLC);

                        int tabletsInfo = comS7.ReadArea(S7.S7AreaDB, DB_NUMBER, 15, 1, dataTablets);

                        int bottlesInfo = comS7.ReadArea(S7.S7AreaDB, DB_NUMBER, 17, 1, dataBottles);

                        // If succeed
                        if (retInfo == 0) {

                            // We read each bit in the byte
                            for (int i = 0; i <= 7; i++) {
                                bitsStatus[i] = S7.GetBitAt(datasPLC, 0, i);
                            }
                            sendProgressMessage(bitsStatus);
                        }

                        // If succeed
                        if (tabletsInfo == 0) {
                            // We get the Word
                            numberOfTablets = Integer.valueOf(Integer.toHexString((int) (S7.GetDIntAt(dataTablets, 0) / Math.pow(256, 3))));
                            sendNumberTabletsProgressMessage(numberOfTablets);
                        }

                        // If succeed
                        if (bottlesInfo == 0) {
                            numberOfBottles = (int) (S7.GetDIntAt(dataBottles, 0) / Math.pow(256, 3));

                            sendNumberBottlesProgressMessage(numberOfBottles);
                        }

                        //Log.i("Variable A.P.I. -> ", String.valueOf(bitState));
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
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

        // Send message when tablets number updates to the handler
        private void sendNumberTabletsProgressMessage(Integer number) {
            Message progressMsgTablets = new Message();
            progressMsgTablets.what = MESSAGE_TABLETS_NUMBER_UPDATE;
            progressMsgTablets.arg1 = number;
            myHandler.sendMessage(progressMsgTablets);
        }

        // Send message when bottles number updates to the handler
        private void sendNumberBottlesProgressMessage(Integer numberBottles) {
            Message progressMsgBottles = new Message();
            progressMsgBottles.what = MESSAGE_BOTTLES_NUMBER_UPDATE;
            progressMsgBottles.arg2 = numberBottles;
            myHandler.sendMessage(progressMsgBottles);
        }
    }
}
