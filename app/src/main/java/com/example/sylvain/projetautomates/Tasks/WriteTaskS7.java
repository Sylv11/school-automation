package com.example.sylvain.projetautomates.Tasks;


import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;

import java.util.concurrent.atomic.AtomicBoolean;

public class WriteTaskS7 {
    private static final int DB_NUMBER = 5;
    private int bit;
    private int start;

    // To write the number of bottles in DB
    private boolean writeBottles;
    private int bottlesValue;
    private byte[] bottleCommand = new byte[124];

    // To write the level of liquid in DB
    private boolean writeLevel;
    private int levelValue;
    private byte[] levelCommand = new byte[124];

    // To write the auto mode in DB
    private boolean writeAuto;
    private int autoValue;
    private byte[] autoCommand = new byte[124];

    // To write the manual mode in DB
    private boolean writeManual;
    private int manualValue;
    private byte[] manualCommand = new byte[124];

    // To write the sluicegate word in DB
    private boolean writeSluicegate;
    private int sluicegateValue;
    private byte[] sluicegateWordCommand = new byte[124];

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // Thread and Automate class to communicate with
    private Thread writeThread;
    private AutomateS7 plcS7;

    // S7Client
    private S7Client comS7;

    // Parameters of connection to communicate with the automaton
    private String[] parConnexion = new String[10];
    private byte[] wordCommand = new byte[10];

    public WriteTaskS7(int start) {
        this.start = start;
        comS7 = new S7Client();
        plcS7 = new AutomateS7();
        writeThread = new Thread(plcS7);
    }

    // Stop the thread and disconnect to the automaton
    public void stop() {
        isRunning.set(false);
        comS7.Disconnect();
        writeThread.interrupt();
    }

    // Connection data
    public void start(String ip, String rack, String slot) {
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
                        Integer.valueOf(parConnexion[1]), Integer.valueOf(parConnexion[2]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                while (isRunning.get() && (response.equals(0))) {
                    // Write request in A.P.I. variable
                    // WriteArea(memoryArea, datablock address, variable location, number of variable to transfer, data)
                    comS7.WriteArea(S7.S7AreaDB, DB_NUMBER, start, 1, wordCommand);

                    // Write the number of bottles in DB5.DBW18
                    if (writeBottles) {
                        bottlesValue = (int) (bottlesValue * Math.pow(256, 3));
                        S7.SetDIntAt(bottleCommand, 0, bottlesValue);

                        comS7.WriteArea(S7.S7AreaDB, DB_NUMBER, 18, 1, bottleCommand);
                        writeBottles = false;
                    }

                    // Write the liquid level in DB5.DBW24
                    if (writeLevel) {
                        levelValue = (int) (levelValue * Math.pow(256, 3));
                        S7.SetDIntAt(levelCommand, 0, levelValue);

                        comS7.WriteArea(S7.S7AreaDB, DB_NUMBER, 24, 1, levelCommand);
                        writeLevel = false;
                    }

                    // Write auto in DB5.DBW26
                    if (writeAuto) {
                        autoValue = (int) (autoValue * Math.pow(256, 3));
                        S7.SetDIntAt(autoCommand, 0, autoValue);

                        comS7.WriteArea(S7.S7AreaDB, DB_NUMBER, 26, 1, autoCommand);
                        writeAuto = false;
                    }

                    // Write manual in DB5.DBW28
                    if (writeManual) {
                        manualValue = (int) (manualValue * Math.pow(256, 3));
                        S7.SetDIntAt(manualCommand, 0, manualValue);

                        comS7.WriteArea(S7.S7AreaDB, DB_NUMBER, 28, 1, manualCommand);
                        writeManual = false;
                    }

                    // Write the sluicegate word in DB5.DBW30
                    if (writeSluicegate) {
                        sluicegateValue = (int) (sluicegateValue * Math.pow(256, 3));
                        S7.SetDIntAt(sluicegateWordCommand, 0, sluicegateValue);

                        comS7.WriteArea(S7.S7AreaDB, DB_NUMBER, 30, 1, sluicegateWordCommand);
                        writeSluicegate = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setWriteBool(int b, int v) {
        // Masking
        this.bit = (b == 1) ? 0 : b / 2;
        // Activate ( | => OR)
        if (v == 1) wordCommand[0] = (byte) (b | wordCommand[0]);
            // Deactivate ( ~ => dual, & => AND)
        else wordCommand[0] = (byte) (~b & wordCommand[0]);
    }

    public void setBottles(int value) {
        bottlesValue = value;
        writeBottles = true;
    }

    public void setLevel(int value) {
        levelValue = value;
        writeLevel = true;
    }

    public void setAuto(int value) {
        autoValue = value;
        writeAuto = true;
    }

    public void setManual(int value) {
        manualValue = value;
        writeManual = true;
    }

    public void setSluicegateWord(int value) {
        sluicegateValue = value;
        writeSluicegate = true;
    }
}