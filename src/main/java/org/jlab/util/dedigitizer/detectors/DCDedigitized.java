/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer.detectors;

import java.io.PrintWriter;
import org.jlab.io.base.DataBank;
import org.jlab.util.dedigitizer.ADegitizer;

import org.jlab.io.base.DataEvent;
import org.jlab.util.dedigitizer.Engine;
/**
 *
 * @author ziegler
 */
public class DCDedigitized extends ADegitizer{

    public double TriggerPhase;
    

    public DCDedigitized() {
        this.setDetector("dc");
    }
    
    
    private double get_T0(int sector, int superlayer, int layer, int wire, double[][][][] T0) {
       
        int cable = this.getCableID1to6(layer, wire);
        int slot = this.getSlotID1to7(wire);
        
        return T0[sector - 1][superlayer - 1][slot - 1][cable - 1]; 
        
    }

    private int getSlotID1to7(int wire1to112) {
        int iSlot = (int) ((wire1to112 - 1) / 16) + 1;
        return iSlot;
    }

    private int getCableID1to6(int layer1to6, int wire1to112) {
        /*96 channels are grouped into 6 groups of 16 channels and each group 
            joins with a connector & a corresponding cable (with IDs 1,2,3,4,& 6)*/
        int wire1to16 = (int) ((wire1to112 - 1) % 16 + 1);
        int cable_id = this.CableID[layer1to6 - 1][wire1to16 - 1];
        return cable_id;
    }
    //Map of Cable ID (1, .., 6) in terms of Layer number (1, ..., 6) and localWire# (1, ..., 16)
    private final int[][] CableID = { //[nLayer][nLocWire] => nLocWire=16, 7 groups of 16 wires in each layer
        {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 1
        {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 2
        {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 3
        {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 4
        {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 5
        {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 6  
    //===> 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 (Local wire ID: 0 for 1st, 16th, 32th, 48th, 64th, 80th, 96th wires)
    };
    
    @Override
    public void AppendHits(DataEvent event, PrintWriter pw, int eventNb, float npho) {
                
        if (event.hasBank("DC::tdc") == true) {
            
            DataBank bankDGTZ = event.getBank("DC::tdc");

            int rows = bankDGTZ.rows();
            int[] sector = new int[rows];
            int[] layer = new int[rows];
            int[] component = new int[rows];
            float[] time = new float[rows];
            int[] useMChit = new int[rows];

            for (int i = 0; i < rows; i++) {
                sector[i] = bankDGTZ.getByte("sector", i);
                layer[i] = bankDGTZ.getByte("layer", i);
                component[i] = bankDGTZ.getShort("component", i);
                time[i] = (float) bankDGTZ.getInt("TDC", i);
            }

            if (event.hasBank("DC::doca") == true) {
                DataBank bankD = event.getBank("DC::doca");
                for (int i = 0; i < bankD.rows(); i++) {
                    if (bankD.getFloat("stime", i) < 0) {
                        useMChit[i] = -1;
                    }
                }
            }
            int size = layer.length;
            
            for (int i = 0; i < size; i++) {
                int superlayerNum = (layer[i] - 1) / 6 + 1;
                int layerNum = layer[i] - (superlayerNum - 1) * 6;
                time[i] -= (this.get_T0(sector[i], superlayerNum, layerNum, component[i], _T0) + 
                        this.TriggerPhase + Engine.StartTime);
            }
        
            for (int i = 0; i < size; i++) {
                if (component[i] != -1 && useMChit[i] != -1 ) {//"detector", "event", "numIds", "sector", "layer", "component", "time", "energy", "nbph");
                    //System.out.printf("%s\t %d\t %d\t %d\t %d\t %f\t\n", "DC", eventNb, sector[i], layer[i], component[i], time[i]);
                    pw.printf("%s\t %d\t %d\t %d\t %d\t %d\t %f\t %f\t %d\t\n", this.getDetector(), eventNb, 3, sector[i], layer[i], component[i], time[i], 0.0, 0);
                    //Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], component[i], smearedTime[i], 0, 0, hitno[i]);			
                }
            }
        }
    }
    private double[][][][] _T0;
    public void setT0Array(double[][][][] T0) {
        _T0 = T0;
    }
    public double[][][][] getT0Array() {
        return _T0;
    }

    
    
}
