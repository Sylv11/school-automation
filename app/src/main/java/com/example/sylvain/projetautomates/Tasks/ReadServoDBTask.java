package com.example.sylvain.projetautomates.Tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;
import com.example.sylvain.projetautomates.Utils.DataBlock;


import java.util.concurrent.atomic.AtomicBoolean;

/* This class provides the useful information's to the liquid level activity by reading it in the data block  */


public class ReadServoDBTask {
    // Update message
    private static final int MESSAGE_PROGRESS_UPDATE = 2;
    private static final int MESSAGE_LIQUID_LEVEL_UPDATE = 3;
    private static final int MESSAGE_AUTO_ORDER_UPDATE = 4;
    private static final int MESSAGE_MANUAL_ORDER_UPDATE = 5;
    private static final int MESSAGE_SLUICEGATE_WORD_UPDATE = 6;

    // AtomicBoolean for the thread
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // Components to change
    private TextView tv_servo_status_sluicegate_1;
    private TextView tv_servo_status_sluicegate_2;
    private TextView tv_servo_status_sluicegate_3;
    private TextView tv_servo_status_sluicegate_4;
    private TextView tv_servo_read_auto_manual;
    private ProgressBar pb_servo_liquid_level;
    private TextView tv_servo_read_auto_order;
    private TextView tv_servo_read_manual_order;
    private TextView tv_servo_read_sluicegate_word;
    private TextView tv_servo_read_local_remote;

    // Context of servo
    private Context context;


    // S7Client, Thread, AutomatesS7, param and data
    private AutomateS7 plcS7;
    private Thread readThread;
    private S7Client comS7;
    private String[] param = new String[10];
    private byte[] datasPLC = new byte[512];
    private byte[] dataLevel = new byte[512];
    private byte[] dataAuto = new byte[512];
    private byte[] dataManual = new byte[512];
    private byte[] dataSluicegate = new byte[512];

    // DBB number
    private int start;

    public ReadServoDBTask(int start, Context context,
                           TextView tv_servo_status_sluicegate_1,
                           TextView tv_servo_status_sluicegate_2,
                           TextView tv_servo_status_sluicegate_3,
                           TextView tv_servo_status_sluicegate_4,
                           TextView tv_servo_read_auto_manual,
                           ProgressBar pb_servo_liquid_level,
                           TextView tv_servo_read_auto_order,
                           TextView tv_servo_read_manual_order,
                           TextView tv_servo_read_sluicegate_word,
                           TextView tv_servo_read_local_remote) {

        this.start = start;
        this.context = context;

        this.tv_servo_status_sluicegate_1 = tv_servo_status_sluicegate_1;
        this.tv_servo_status_sluicegate_2 = tv_servo_status_sluicegate_2;
        this.tv_servo_status_sluicegate_3 = tv_servo_status_sluicegate_3;
        this.tv_servo_status_sluicegate_4 = tv_servo_status_sluicegate_4;
        this.tv_servo_read_auto_manual = tv_servo_read_auto_manual;
        this.pb_servo_liquid_level = pb_servo_liquid_level;
        this.tv_servo_read_auto_order = tv_servo_read_auto_order;
        this.tv_servo_read_manual_order = tv_servo_read_manual_order;
        this.tv_servo_read_sluicegate_word = tv_servo_read_sluicegate_word;
        this.tv_servo_read_local_remote = tv_servo_read_local_remote;

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

    // Send the information's to the servo activity and change some properties
    @SuppressLint("SetTextI18n")
    private void downloadOnProgressUpdate(boolean[] bitsStatus) {

        // If in DB5.DBB0
        if (start == 0) {
            // We read the first bit in the byte
            if (bitsStatus[1]) {
                this.tv_servo_status_sluicegate_1.setText("Ouverte");
                this.tv_servo_status_sluicegate_1.setTextColor(Color.GREEN);
            } else {
                this.tv_servo_status_sluicegate_1.setText("Fermée");
                this.tv_servo_status_sluicegate_1.setTextColor(Color.RED);
            }

            if (bitsStatus[2]) {
                this.tv_servo_status_sluicegate_2.setText("Ouverte");
                this.tv_servo_status_sluicegate_2.setTextColor(Color.GREEN);
            } else {
                this.tv_servo_status_sluicegate_2.setText("Fermée");
                this.tv_servo_status_sluicegate_2.setTextColor(Color.RED);
            }

            if (bitsStatus[3]) {
                this.tv_servo_status_sluicegate_3.setText("Ouverte");
                this.tv_servo_status_sluicegate_3.setTextColor(Color.GREEN);
            } else {
                this.tv_servo_status_sluicegate_3.setText("Fermée");
                this.tv_servo_status_sluicegate_3.setTextColor(Color.RED);
            }

            if (bitsStatus[4]) {
                this.tv_servo_status_sluicegate_4.setText("Ouverte");
                this.tv_servo_status_sluicegate_4.setTextColor(Color.GREEN);
            } else {
                this.tv_servo_status_sluicegate_4.setText("Fermée");
                this.tv_servo_status_sluicegate_4.setTextColor(Color.RED);
            }

            if (bitsStatus[5]) {
                this.tv_servo_read_auto_manual.setText("Auto");
                this.tv_servo_read_auto_manual.setTextColor(Color.GRAY);
            } else {
                this.tv_servo_read_auto_manual.setText("Manuel");
                this.tv_servo_read_auto_manual.setTextColor(Color.GRAY);
            }

            if (bitsStatus[6]) {
                this.tv_servo_read_local_remote.setText("Distant");
                this.tv_servo_read_local_remote.setTextColor(Color.GRAY);
            } else {
                this.tv_servo_read_local_remote.setText("Local");
                this.tv_servo_read_local_remote.setTextColor(Color.GRAY);
            }
        }
    }

    // Send the data to the servo activity
    private void downloadOnLiquidLevelUpdate(Integer liquidLevel) {
        this.pb_servo_liquid_level.setProgress(liquidLevel);
    }

    // Send the data to the servo activity
    @SuppressLint("SetTextI18n")
    private void downloadOnAutoOrderUpdate(Integer autoOrder) {
        this.tv_servo_read_auto_order.setTextColor(Color.GRAY);

        if (autoOrder >= 0) this.tv_servo_read_auto_order.setText(String.valueOf(autoOrder));
        else this.tv_servo_read_auto_order.setText("Valeur incorrecte");
    }

    // Send the data to the servo activity
    @SuppressLint("SetTextI18n")
    private void downloadOnManualOrderUpdate(Integer manualOrder) {
        this.tv_servo_read_manual_order.setTextColor(Color.GRAY);

        if (manualOrder >= 0) this.tv_servo_read_manual_order.setText(String.valueOf(manualOrder));
        else this.tv_servo_read_manual_order.setText("Valeur incorrecte");
    }

    // Send the data to the servo activity
    @SuppressLint("SetTextI18n")
    private void downloadOnSluicegateWordUpdate(Integer sluicegateWord) {
        this.tv_servo_read_sluicegate_word.setTextColor(Color.GRAY);

        if (sluicegateWord >= 0)
            this.tv_servo_read_sluicegate_word.setText(String.valueOf(sluicegateWord));
        else this.tv_servo_read_sluicegate_word.setText("Valeur incorrecte");
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
                case MESSAGE_LIQUID_LEVEL_UPDATE:
                    downloadOnLiquidLevelUpdate(msg.arg1);
                    break;
                case MESSAGE_AUTO_ORDER_UPDATE:
                    downloadOnAutoOrderUpdate(msg.arg1);
                    break;
                case MESSAGE_MANUAL_ORDER_UPDATE:
                    downloadOnManualOrderUpdate(msg.arg1);
                    break;
                case MESSAGE_SLUICEGATE_WORD_UPDATE:
                    downloadOnSluicegateWordUpdate(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private class AutomateS7 implements Runnable {
        // Result of the connection
        private Integer res;

        // Result of the A.P.I. call
        private Integer liquidLevel = 0;
        private Integer autoOrderNumber = 0;
        private Integer manualOrderNumber = 0;
        private Integer sluicegateWord = 0;
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
                        int retInfo = comS7.ReadArea(S7.S7AreaDB, DataBlock.DB, start, 1, datasPLC);

                        int levelInfo = comS7.ReadArea(S7.S7AreaDB, DataBlock.DB, 16, 1, dataLevel);

                        int autoInfo = comS7.ReadArea(S7.S7AreaDB, DataBlock.DB, 18, 1, dataAuto);

                        int manualInfo = comS7.ReadArea(S7.S7AreaDB, DataBlock.DB, 21, 1, dataManual);

                        int sluicegateInfo = comS7.ReadArea(S7.S7AreaDB, DataBlock.DB, 23, 1, dataSluicegate);

                        // If succeed
                        if (retInfo == 0) {

                            // We read each bit in the byte
                            for (int i = 0; i <= 7; i++) {
                                bitsStatus[i] = S7.GetBitAt(datasPLC, 0, i);
                            }
                            sendProgressMessage(bitsStatus);
                        }

                        if (levelInfo == 0) {
                            liquidLevel = (int) (S7.GetDIntAt(dataLevel, 0) / Math.pow(256, 2));
                            System.out.println(liquidLevel);
                            sendLiquidLevelMessage(liquidLevel);
                        }

                        if (autoInfo == 0) {
                            autoOrderNumber = (int) (S7.GetDIntAt(dataAuto, 0) / Math.pow(256, 2));
                            sendAutoOrderMessage(autoOrderNumber);
                        }

                        if (manualInfo == 0) {
                            manualOrderNumber = (int) (S7.GetDIntAt(dataManual, 0) / Math.pow(256, 3));
                            sendManualOrderMessage(manualOrderNumber);
                        }

                        if (sluicegateInfo == 0) {
                            sluicegateWord = (int) (S7.GetDIntAt(dataSluicegate, 0) / Math.pow(256, 3));
                            sendSluicegateWordMessage(sluicegateWord);
                        }

                        //Log.i("Variable A.P.I. -> ", String.valueOf(bitState));
                    }
                    try {
                        Thread.sleep(100);
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

        // Send message when liquid level updates to the handler
        private void sendLiquidLevelMessage(Integer number) {
            Message progressMsgLevel = new Message();
            progressMsgLevel.what = MESSAGE_LIQUID_LEVEL_UPDATE;
            progressMsgLevel.arg1 = number;
            myHandler.sendMessage(progressMsgLevel);
        }

        // Send message when the auto order updates to the handler
        private void sendAutoOrderMessage(Integer autoOrder) {
            Message autoOrderMsg = new Message();
            autoOrderMsg.what = MESSAGE_AUTO_ORDER_UPDATE;
            autoOrderMsg.arg1 = autoOrder;
            myHandler.sendMessage(autoOrderMsg);
        }

        // Send message when the manual order updates to the handler
        private void sendManualOrderMessage(Integer manualOrder) {
            Message manualOrderMsg = new Message();
            manualOrderMsg.what = MESSAGE_MANUAL_ORDER_UPDATE;
            manualOrderMsg.arg1 = manualOrder;
            myHandler.sendMessage(manualOrderMsg);
        }

        // Send message when the sluicegates word updates to the handler
        private void sendSluicegateWordMessage(Integer sluicegateWord) {
            Message sluicegateWordMsg = new Message();
            sluicegateWordMsg.what = MESSAGE_SLUICEGATE_WORD_UPDATE;
            sluicegateWordMsg.arg1 = sluicegateWord;
            myHandler.sendMessage(sluicegateWordMsg);
        }
    }
}
