package com.example.sylvain.projetautomates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sylvain.projetautomates.SimaticS7.IntByRef;
import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;
import com.example.sylvain.projetautomates.SimaticS7.S7CpInfo;
import com.example.sylvain.projetautomates.SimaticS7.S7CpuInfo;
import com.example.sylvain.projetautomates.SimaticS7.S7OrderCode;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadTaskS7
{
    private static final int MESSAGE_PRE_EXECUTE = 1;

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private TextView tv_dashboard_numCPU;
    private TextView tv_dashboard_modelPU;
    private TextView tv_dashboard_statusCPU;
    private TextView tv_dashboard_error;
    private Button btn_dashboard_powerPLC;

    private Context context;

    private AutomateS7 plcS7;
    private Thread readThread;

    private S7Client comS7;
    private String[] param = new String[10];

    public ReadTaskS7(TextView tv_dashboard_numCPU, TextView tv_dashboard_statusCPU, TextView tv_dashboard_error, TextView tv_dashboard_modelPU, Button btn_dashboard_powerPLC, Context context) {
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

    public void stop() {
        this.isRunning.set(false);
        this.comS7.Disconnect();
        this.readThread.interrupt();
    }

    public void start(String ip, String rack, String slot) {

        if (!this.readThread.isAlive()) {
            this.param[0] = ip;
            this.param[1] = rack;
            this.param[2] = slot;
            this.readThread.start();
            this.isRunning.set(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void downloadOnPreExecute(Object obj) {
        ArrayList<String> data = ((ArrayList<String>)obj);

        try {
            if(data.size() > 0) {
                this.tv_dashboard_numCPU.setText(String.valueOf(data.get(0)));

                if(String.valueOf(data.get(1)).equals("En fonctionnement")) {
                    this.tv_dashboard_statusCPU.setTextColor(android.graphics.Color.GREEN);

                    this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this.context, R.color.colorRed));
                    this.btn_dashboard_powerPLC.setText("Stop");
                    this.btn_dashboard_powerPLC.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop, 0, 0, 0);
                }
                else  {
                    this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this.context, R.color.colorGreen));
                    this.btn_dashboard_powerPLC.setText("Run");
                    this.btn_dashboard_powerPLC.setCompoundDrawablesWithIntrinsicBounds( R.drawable.run, 0, 0, 0);

                    this.tv_dashboard_statusCPU.setTextColor(Color.RED);
                }
                this.tv_dashboard_statusCPU.setText(String.valueOf(data.get(1)));
                this.tv_dashboard_modelPU.setText(String.valueOf(data.get(2)));
            }else {
                this.tv_dashboard_numCPU.setText(" ⚠️Impossible de récupérer le numéro");
                this.tv_dashboard_modelPU.setText(" ⚠️Impossible de récupérer le modèle");
                this.tv_dashboard_statusCPU.setText("⚠️Impossible de récupérer le statut");
                this.tv_dashboard_error.setVisibility(View.VISIBLE);
            }
            this.stop();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case MESSAGE_PRE_EXECUTE :
                    downloadOnPreExecute(msg.obj);
                    break;
                default :
                    break;
            }
        }
    };

    private class AutomateS7 implements Runnable {

        private ArrayList<String> data = new ArrayList<String>();
        private Integer res;
        String numCPU="";
        int cplStatus=-1;

        @Override
        public void run() {

            try {
                comS7.SetConnectionType(S7.S7_BASIC);
                this.res = comS7.ConnectTo(param[0],Integer.valueOf(param[1]),Integer.valueOf(param[2]));
            }catch (Exception e) {
                e.printStackTrace();
            }

            if(this.res.equals(0)) {

                try {
                    S7OrderCode orderCode = new S7OrderCode();
                    Integer result = comS7.GetOrderCode(orderCode);

                    if(result.equals(0)) {
                        numCPU = String.valueOf(orderCode.Code());
                        this.data.add(String.valueOf(numCPU));
                    }

                }catch (Exception e) {
                    this.data.add("Impossible de récupérer le numéro du CPU");
                    e.printStackTrace();
                }

                try {
                    String state = "Undefined";
                    IntByRef ref = new IntByRef();
                    Integer response = comS7.GetPlcStatus(ref);

                    if(response.equals(0)) {
                        cplStatus = Integer.valueOf(ref.Value);

                        switch(cplStatus) {
                            case S7.S7CpuStatusRun :
                                state = "En fonctionnement";
                                break;
                            case S7.S7CpuStatusStop:
                                state = "Stoppé";
                                break;
                        }
                        this.data.add(state);
                    }

                }catch (Exception e) {
                    this.data.add("Impossible de récupérer le statut du CPU");
                    e.printStackTrace();
                }

                try {
                    StringBuilder builderInfos = new StringBuilder();
                    S7CpuInfo cpuInfo = new S7CpuInfo();
                    Integer response = comS7.GetCpuInfo(cpuInfo);

                    if(response.equals(0)) {
                        builderInfos.append(cpuInfo.ASName() + "\n");
                        builderInfos.append(cpuInfo.Copyright() + "\n");
                        builderInfos.append(cpuInfo.SerialNumber() + "\n");
                        builderInfos.append(cpuInfo.ModuleTypeName() + "\n");
                        builderInfos.append(cpuInfo.ModuleName() + "\n");

                        this.data.add(builderInfos.toString());
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }

                sendPreExecuteMessage(data);
            }else {
                System.out.printf("Connection to automaton failed");
                sendPreExecuteMessage(data);
            }
        }

        private void sendPreExecuteMessage(List<String> data) {
            Message preExecuteMsg = new Message();
            preExecuteMsg.what = MESSAGE_PRE_EXECUTE;
            preExecuteMsg.obj = data;
            myHandler.sendMessage(preExecuteMsg);
        }

    }

}
