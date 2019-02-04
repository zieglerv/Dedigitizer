/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer.detectors;

import java.io.PrintWriter;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.util.dedigitizer.ADegitizer;

/**
 *
 * @author ziegler
 */
public class ECALDedigitized extends ADegitizer{
    public ECALDedigitized() {
            this.setDetector("ECAL");
        }  
    private double              iGain = 1.0;
    private double              iADC_to_MEV  = 1.0/10000.0;
    
    
    @Override
    public void AppendHits(DataEvent event, PrintWriter pw, int eventNb, float startTime) {
        if (event.hasBank("ECAL::adc") == false) {
          
            return;
        }

    
        if(event.hasBank("ECAL::adc")==true){
                DataBank bank = event.getBank("ECAL::adc");
                int rows = bank.rows();
                for(int loop = 0; loop < rows; loop++){
                    int sector    = bank.getByte("sector", loop);
                    int layer     = bank.getByte("layer", loop);
                    int component = bank.getShort("component", loop);
                    float energy = (float) ((float)bank.getInt("ADC", loop)*iGain*iADC_to_MEV);
                    pw.printf("%s\t %d\t %d\t %d\t %d\t %f\t %f\t %f\t\n", this.getDetector(), eventNb, sector, layer, component, 0.0, energy, startTime);
                }
        }
    }
}
