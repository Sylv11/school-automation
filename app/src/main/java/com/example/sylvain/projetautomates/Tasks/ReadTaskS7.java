package com.example.sylvain.projetautomates.Tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.SimaticS7.IntByRef;
import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;
import com.example.sylvain.projetautomates.SimaticS7.S7CpuInfo;
import com.example.sylvain.projetautomates.SimaticS7.S7OrderCode;
import com.example.sylvain.projetautomates.Utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class ReadTaskS7 {
    private static final int MESSAGE_PRE_EXECUTE = 1;

    // TextView and Button of dashboard
    private TextView tv_dashboard_numCPU;
    private TextView tv_dashboard_modelPU;
    private TextView tv_dashboard_statusCPU;
    private TextView tv_dashboard_error;
    private Button btn_dashboard_powerPLC;

    private Context context;

    // Thread and Automate class to communicate with
    private AutomateS7 plcS7;
    private Thread readThread;

    // S7Client
    private S7Client comS7;
    private String[] param = new String[10];

    public ReadTaskS7(TextView tv_dashboard_numCPU,
                      TextView tv_dashboard_statusCPU,
                      TextView tv_dashboard_error,
                      TextView tv_dashboard_modelPU,
                      Button btn_dashboard_powerPLC,
                      Context context) {

        this.context = context;
        this.tv_dashboard_numCPU = tv_dashboard_numCPU;
        this.tv_dashboard_modelPU = tv_dashboard_modelPU;
        this.tv_dashboard_statusCPU = tv_dashboard_statusCPU;
        this.tv_dashboard_error = tv_dashboard_error;
        this.btn_dashboard_powerPLC = btn_dashboard_powerPLC;

        this.comS7 = new S7Client();
        this.plcS7 = new AutomateS7();
        this.readThread = new Thread(this.plcS7);

    }

    // Stop the thread and disconnect to the automaton
    public void stop() {
        this.comS7.Disconnect();
        this.readThread.interrupt();
    }

    // Connection data
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
    private void downloadOnPreExecute(Object obj) {
        ArrayList<String> data = ((ArrayList<String>) obj);

        try {

            if (data.size() > 0) {
                this.tv_dashboard_numCPU.setText(String.valueOf(data.get(0)));
                this.tv_dashboard_numCPU.setTextColor(Color.GRAY);

                if (String.valueOf(data.get(1)).equals("En fonctionnement")) {
                    this.tv_dashboard_statusCPU.setTextColor(Color.GREEN);

                    this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this.context, R.color.colorRed));
                    this.btn_dashboard_powerPLC.setText("Stop");
                    this.btn_dashboard_powerPLC.setCompoundDrawablesWithIntrinsicBounds(R.drawable.stop, 0, 0, 0);
                } else {
                    this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this.context, R.color.colorGreen));
                    this.btn_dashboard_powerPLC.setText("Run");
                    this.btn_dashboard_powerPLC.setCompoundDrawablesWithIntrinsicBounds(R.drawable.run, 0, 0, 0);

                    this.tv_dashboard_statusCPU.setTextColor(Color.RED);
                }
                this.tv_dashboard_statusCPU.setText(String.valueOf(data.get(1)));

                this.tv_dashboard_modelPU.setText(String.valueOf(data.get(2)));
                this.tv_dashboard_modelPU.setTextColor(Color.GRAY);
            } else {
                this.errorDisplay();
            }
            this.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void errorDisplay() {
        this.tv_dashboard_numCPU.setText(" ⚠️Impossible de récupérer la référence");
        this.tv_dashboard_numCPU.setTextColor(Color.GRAY);

        this.tv_dashboard_modelPU.setText(" ⚠️Impossible de récupérer le modèle");
        this.tv_dashboard_modelPU.setTextColor(Color.GRAY);

        this.tv_dashboard_statusCPU.setText("⚠️Impossible de récupérer le statut");
        this.tv_dashboard_statusCPU.setTextColor(Color.GRAY);

        this.tv_dashboard_error.setVisibility(View.VISIBLE);
        this.btn_dashboard_powerPLC.setEnabled(false);
        this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this.context, R.color.colorDarkGray));
        ToastUtil.show(this.context, "La connexion à l'automate a échoué");
    }


    // Run method according to the message
    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_PRE_EXECUTE:
                    downloadOnPreExecute(msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private class AutomateS7 implements Runnable {

        private ArrayList<String> data = new ArrayList<String>();
        private Integer res;
        String numCPU = "";
        int cplStatus = -1;

        @Override
        public void run() {

            // Connection to the automaton
            try {
                comS7.SetConnectionType(S7.S7_BASIC);
                this.res = comS7.ConnectTo(param[0], Integer.valueOf(param[1]), Integer.valueOf(param[2]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // If the connection succeed
            if (this.res.equals(0)) {

                // Get CPU order code
                try {
                    S7OrderCode orderCode = new S7OrderCode();
                    Integer result = comS7.GetOrderCode(orderCode);

                    if (result.equals(0)) {
                        numCPU = String.valueOf(orderCode.Code());
                        this.data.add(String.valueOf(numCPU));
                    }

                } catch (Exception e) {
                    this.data.add("Impossible de récupérer le numéro du CPU");
                    e.printStackTrace();
                }

                // Get CPU status
                try {
                    String state = "Undefined";
                    IntByRef ref = new IntByRef();
                    Integer response = comS7.GetPlcStatus(ref);

                    if (response.equals(0)) {
                        cplStatus = ref.Value;

                        switch (cplStatus) {
                            case S7.S7CpuStatusRun:
                                state = "En fonctionnement";
                                break;
                            case S7.S7CpuStatusStop:
                                state = "Stoppé";
                                break;
                        }
                        this.data.add(state);
                    }

                } catch (Exception e) {
                    this.data.add("Impossible de récupérer le statut du CPU");
                    e.printStackTrace();
                }

                // Get CPU model
                try {
                    StringBuilder builderInfos = new StringBuilder();
                    S7CpuInfo cpuInfo = new S7CpuInfo();
                    Integer response = comS7.GetCpuInfo(cpuInfo);

                    if (response.equals(0)) {
                        builderInfos.append(cpuInfo.ASName()).append("\n");
                        builderInfos.append(cpuInfo.Copyright()).append("\n");
                        builderInfos.append(cpuInfo.SerialNumber()).append("\n");
                        builderInfos.append(cpuInfo.ModuleTypeName()).append("\n");
                        builderInfos.append(cpuInfo.ModuleName()).append("\n");

                        this.data.add(builderInfos.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sendPreExecuteMessage(data);
            } else {
                System.out.print("Connection to automaton failed");
                sendPreExecuteMessage(data);
            }
        }

        // Send message to the handler
        private void sendPreExecuteMessage(List<String> data) {
            Message preExecuteMsg = new Message();
            preExecuteMsg.what = MESSAGE_PRE_EXECUTE;
            preExecuteMsg.obj = data;
            myHandler.sendMessage(preExecuteMsg);
        }

    }

}
