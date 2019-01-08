package com.example.sylvain.projetautomates.Tasks;

import com.example.sylvain.projetautomates.SimaticS7.S7;
import com.example.sylvain.projetautomates.SimaticS7.S7Client;
import com.example.sylvain.projetautomates.Utils.DataBlock;

import java.util.concurrent.atomic.AtomicBoolean;

/* This class performs all writing actions */

public class WriteTaskS7 {
    // bit and start
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

        // Result of the connection
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
                    comS7.WriteArea(S7.S7AreaDB, DataBlock.DB, start, 1, wordCommand);

                    if (writeBottles) {
                        bottlesValue = (int) (bottlesValue * Math.pow(256, 3));
                        S7.SetDIntAt(bottleCommand, 0, bottlesValue);

                        comS7.WriteArea(S7.S7AreaDB, DataBlock.DB, 18, 1, bottleCommand);
                        writeBottles = false;
                    }

                    if (writeLevel) {
                        levelValue = (int) (levelValue * Math.pow(256, 3));
                        S7.SetDIntAt(levelCommand, 0, levelValue);

                        comS7.WriteArea(S7.S7AreaDB, DataBlock.DB, 24, 1, levelCommand);
                        writeLevel = false;
                    }

                    if (writeAuto) {
                        autoValue = (int) (autoValue * Math.pow(256, 3));
                        S7.SetDIntAt(autoCommand, 0, autoValue);

                        comS7.WriteArea(S7.S7AreaDB, DataBlock.DB, 26, 1, autoCommand);
                        writeAuto = false;
                    }

                    if (writeManual) {
                        manualValue = (int) (manualValue * Math.pow(256, 3));
                        S7.SetDIntAt(manualCommand, 0, manualValue);

                        comS7.WriteArea(S7.S7AreaDB, DataBlock.DB, 28, 1, manualCommand);
                        writeManual = false;
                    }

                    if (writeSluicegate) {
                        sluicegateValue = (int) (sluicegateValue * Math.pow(256, 3));
                        S7.SetDIntAt(sluicegateWordCommand, 0, sluicegateValue);

                        comS7.WriteArea(S7.S7AreaDB, DataBlock.DB, 30, 1, sluicegateWordCommand);
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
        // Activate ( | => OR)
        if (v == 1) wordCommand[0] = (byte) (b | wordCommand[0]);
            // Deactivate ( ~ => dual, & => AND)
        else wordCommand[0] = (byte) (~b & wordCommand[0]);
    }

    // Method called when click on the SET button for bottles
    public void setBottles(int value) {
        bottlesValue = value;
        writeBottles = true;
    }

    // Method called when click on the SET button for liquid level
    public void setLevel(int value) {
        levelValue = value;
        writeLevel = true;
    }

    // Method called when click on the SET button for the auto order
    public void setAuto(int value) {
        autoValue = value;
        writeAuto = true;
    }

    // Method called when click on the SET button for the manual order
    public void setManual(int value) {
        manualValue = value;
        writeManual = true;
    }

    // Method called when click on the SET button for the sluicegate word
    public void setSluicegateWord(int value) {
        sluicegateValue = value;
        writeSluicegate = true;
    }
}
