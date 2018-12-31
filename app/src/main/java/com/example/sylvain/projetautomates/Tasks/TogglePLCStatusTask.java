package com.example.sylvain.projetautomates.Tasks;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;


public class TogglePLCStatusTask {

    private static final int MESSAGE_PRE_EXECUTE = 1;

    // Thread and Automate class to communicate with
    private AutomateS7 plcS7;
    private Thread readThread;

    // S7Client
    private S7Client comS7;
    private String[] param = new String[10];

    private boolean CPLstatus;

    // TextView of dashboard
    private TextView tv_dashboard_statusCPU;

    public TogglePLCStatusTask(boolean CPLstatus, TextView tv_dashboard_statusCPU) {
        this.CPLstatus = CPLstatus;
        this.tv_dashboard_statusCPU = tv_dashboard_statusCPU;

        this.comS7 = new S7Client();
        this.plcS7 = new AutomateS7();
        this.readThread = new Thread(this.plcS7);

    }

    // Stop the thread and disconnect to the automaton
    public void stop() {
        this.comS7.Disconnect();
        this.readThread.interrupt();
    }

    // Connection informations
    public void start(String ip, String rack, String slot) {

        if (!this.readThread.isAlive()) {
            this.param[0] = ip;
            this.param[1] = rack;
            this.param[2] = slot;
            this.readThread.start();
        }
    }

    // Send the informations to the dashboard and change some properties
    @SuppressLint("SetTextI18n")
    private void downloadOnPreExecute(Integer status) {

        try {
            if (status.equals(1)) {
                this.tv_dashboard_statusCPU.setText("En fonctionnement");
                this.tv_dashboard_statusCPU.setTextColor(android.graphics.Color.GREEN);
            } else if (status.equals(0)) {
                this.tv_dashboard_statusCPU.setText("Stoppé");
                this.tv_dashboard_statusCPU.setTextColor(android.graphics.Color.RED);
            }

            this.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Run method according to the message
    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_PRE_EXECUTE:
                    downloadOnPreExecute(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    private class AutomateS7 implements Runnable {

        private Integer res;

        @Override
        public void run() {

            // Connection to the automaton
            try {
                comS7.SetConnectionType(S7.S7_BASIC);
                this.res = comS7.ConnectTo(param[0], Integer.valueOf(param[1]), Integer.valueOf(param[2]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (this.res.equals(0)) {
                if (CPLstatus) {
                    // Change CPU status to STOP
                    try {
                        Integer response = comS7.PlcStop();

                        if (response.equals(0)) {
                            sendPreExecuteMessage(0);
                        } else {
                            System.out.println("An error has occured when shutting down PLC");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Change CPU status to RUN
                    try {
                        Integer response = comS7.PlcColdStart();

                        if (response.equals(0)) {
                            sendPreExecuteMessage(1);
                        } else {
                            System.out.println("An error has occured when starting PLC");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.printf("Connection to automaton failed");
            }
        }

        // Send message to the handler
        private void sendPreExecuteMessage(Integer status) {
            Message preExecuteMsg = new Message();
            preExecuteMsg.what = MESSAGE_PRE_EXECUTE;
            preExecuteMsg.arg1 = status;
            myHandler.sendMessage(preExecuteMsg);
        }

    }

}
