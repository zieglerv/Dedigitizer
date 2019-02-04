/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer.detectors;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cnd.constants.CalibrationConstantsLoader;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.util.dedigitizer.ADegitizer;

/**
 *
 * @author ziegler
 */
public class CNDDedigitized extends ADegitizer {
    public CNDDedigitized() {
        this.setDetector("CND");
    }
    ADCConvertor adcConv;
    @Override
    public void AppendHits(DataEvent event, PrintWriter pw, int eventNb, float startTime) {
        if(event.hasBank("CND::adc")==false || event.hasBank("CND::tdc")==false) {
			//System.err.println("there is no CND bank :-(");
	} 
        List<HalfHit> halfhits = new ArrayList<HalfHit>();
        DataBank bankADC = event.getBank("CND::adc");
        DataBank bankTDC = event.getBank("CND::tdc");

	if(bankADC.rows() != bankTDC.rows()) 
            return ;
        int[] order = new int[bankADC.rows()];
        int[] sector = new int[bankADC.rows()];
        int[] layer = new int[bankADC.rows()];
        int[] component = new int[bankADC.rows()];
        int[] ADC = new int[bankADC.rows()];
        int[] TDC = new int[bankADC.rows()];
	
	for(int i = 0; i<bankADC.rows(); i++) { 
            sector[i]    = bankADC.getByte("sector",i);  // one of the 24 "blocks"
            layer[i]     = bankADC.getByte("layer",i);  
            order[i]     = bankADC.getByte("order",i);
            component[i] = order[i] + 1; // get the component 1 is left 2 is right

	    //assume that ADC and TDC have then same index in both adc and tdc raw list
            ADC[i] = bankADC.getInt("ADC",i);   
            TDC[i] = bankTDC.getInt("TDC",i);

            halfhits.add(new HalfHit(sector[i], layer[i], component[i], ADC[i], TDC[i], i));
            //pw.printf("%s\t %d\t %d\t %d\t %d\t %f\t %f\t\n", this.getDetector(), eventNb, sector[i], layer[i], component[i], TDC[i], ADC[i]);
        }

        if(halfhits.size() > 0) {

        // Loop through the half-hits array to find possible physical combinations with neighbours.


        double E1=0;//used to check that the two component of the deposited energy are roughtly the same
        double E2=0;

        int neigh = 0;       // index of the coupled neighbour paddle to the one under consideration
        int pad = 0;         // index of paddle (component) under consideration in the half-hit list
        int lay = 0;         // index of layer under consideration in the half-hit list
        int block = 0;       // index of block (sector) under consideration in the half-hit list 
        int pad_d = 0;       // index of the paddle with the direct signal
        int pad_n = 0;       // index of the paddle with the indirect (neighbour) signal

        int indexR=0;	 // index of the hit in the row adcR/tdcR bank
        int indexL=0;	 // index of the hit in the row adcL/tdcL bank

        double Tup = 0.;      // Time at upstream end of hit paddle (taken from direct signal)
        double Tdown = 0.;    // Time at downstream end of hit paddle (taken from the neighbour signal)
        double Eup = 0.;      // Energy at upstream end of hit paddle
        double Edown = 0.;    // Energy at downstream end of hit paddle

        double Z_av = 0.;    // Z of the hit position (local co-ordinates, wrt the centre of the paddle)
        double T_hit = 0.;   // Reconstructed time of particle hit in the paddle
        double z_hit = 0.;   // Reconstructed position of the particle hit in the paddle (global co-ordinates, wrt to centre of the Central Detector)
        double x_hit = 0.;   // Reconstructed position of the particle hit in the paddle (global co-ordinates, wrt to centre of the Central Detector)
        double y_hit = 0.;   // Reconstructed position of the particle hit in the paddle (global co-ordinates, wrt to centre of the Central Detector)
        double E_hit = 0;	 // Reconstructed energy deposit of the particle in the paddle 
        double r_hit = 0.;   // Perpendicular distance of the hit position (assuming center of paddle) from the beam-axis  
        double path = 0.;	 // path length travelled by particle (assuming a straight line)
        double phi_hit = 0.; // Phi angle of the hit position (assuming center of paddle) from the x-axis.
        double theta_hit = 0.; // Theta angle of the hit position from the z-axis;

        int totrec = 0;      // counter for "good" reconstructions

        for(int i = 0; i < (halfhits.size()); i++) {	
                HalfHit hit1 = halfhits.get(i);   // first, get the half-hit			

                // for each half-hit (signal), work out the coupled paddle:
                block = hit1.Sector();    // the sector (block) of the hit
                pad = hit1.Component();   // the paddle associated with the hit
                lay = hit1.Layer();
                if (pad == 1) neigh = 2;  // the neighbouring paddle
                else neigh = 1;

                // Now loop through the half-hits again and match any which can give a physical reconstruction,
                // but off-set the start of the list to make sure no repeats:

                for (int j = i+1; j < halfhits.size(); j++) {	
                        HalfHit hit2 = halfhits.get(j);   // get the second half-hit	

                        if (block != hit2.Sector()) continue;             // half-hits must be in the same sector
                        if (lay != hit2.Layer()) continue;                // half-hits must be in the same layer					
                        if (hit2.Component() != neigh) continue;             // half-hits must come from coupled paddles

                        // Decide which one of the two signals is the direct and which one is indirect on the basis of timing.
                        // Works if effective velocities in the coupled paddles don't differ much.

                        HalfHit hit_d;
                        HalfHit hit_n;

                        if (hit1.Tprop() < hit2.Tprop()) 
                        {
                                hit_d = hit1;
                                hit_n = hit2;
                                pad_d = i;
                                pad_n = j;
                        }
                        else if (hit1.Tprop() > hit2.Tprop()) 
                        {                                                                         
                                hit_d = hit2;
                                hit_n = hit1;
                                pad_d = j;
                                pad_n = i;
                        }
                        else continue;	   // loose events where it's really not clear which paddle they hit in.		

                        // Now calculate the time and energy at the upstream and downstream ends of the paddle the hit happened in:	
                        // attlen is in cm. need to convert to mm -> *10
                        Tup = hit_d.Tprop();
                        Tdown = hit_n.Tprop() - CalibrationConstantsLoader.LENGTH[lay-1]/(10.*CalibrationConstantsLoader.EFFVEL[block-1][lay-1][hit_n.Component()-1]) - CalibrationConstantsLoader.UTURNTLOSS[block-1][lay-1];
                        Eup = hit_d.Eatt()/CalibrationConstantsLoader.MIPDIRECT[block-1][lay-1][hit_d.Component()-1];
                        Edown = hit_n.Eatt()/(Math.exp(-1.*CalibrationConstantsLoader.LENGTH[lay-1]/(10.*CalibrationConstantsLoader.ATNLEN[block-1][lay-1][hit_n.Component()-1]))*CalibrationConstantsLoader.UTURNELOSS[block-1][lay-1]*CalibrationConstantsLoader.MIPDIRECT[block-1][lay-1][hit_n.Component()-1]);

                        //The next two lines have to be used if want to use MIP Indirect for reconstruction
                        //					Eup = hit_d.Eatt()/CalibrationConstantsLoader.MIPDIRECT[hit_d.Sector()-1][hit_d.Layer()-1][hit_d.Component()-1];
                        //					Edown = hit_n.Eatt()/CalibrationConstantsLoader.MIPINDIRECT[hit_d.Sector()-1][hit_d.Layer()-1][hit_d.Component()-1];

                        // For this particular combination, check whether this gives a z within the paddle length (+/- z resolution).
                        // "local" position of hit on the paddle (wrt paddle center):
                        Z_av = ((Tup-Tdown) * 10. * CalibrationConstantsLoader.EFFVEL[block-1][lay-1][hit_d.Component()-1]) / 2.;                                                      

                        //if ( (Z_av < ((CalibrationConstantsLoader.LENGTH[lay-1] / (-2.)) - 10.*Parameters.Zres[lay-1])) || (Z_av > ((CalibrationConstantsLoader.LENGTH[lay-1] / 2.) + 10.*Parameters.Zres[lay-1])) ) continue;                                       

                        // Calculate time of hit in paddle and check that it's in a physical window for the event:
                        T_hit = (Tup + Tdown - (CalibrationConstantsLoader.LENGTH[lay-1] / (10.*CalibrationConstantsLoader.EFFVEL[block-1][lay-1][hit_d.Component()-1]))) / 2.;  // time of hit in the paddle

                        // Calculate the deposited energy and check whether it's over the imposed threshold.			        
                        E_hit = (Eup / Math.exp(-1.*(CalibrationConstantsLoader.LENGTH[lay-1]/2. + Z_av) / (10.*CalibrationConstantsLoader.ATNLEN[block-1][lay-1][hit_d.Component()-1]))) +  (Edown / Math.exp(-1.*(CalibrationConstantsLoader.LENGTH[lay-1]/2. - Z_av) / (10.*CalibrationConstantsLoader.ATNLEN[block-1][lay-1][hit_d.Component()-1])));

                        
                        // set the index of the right and left signal for the reconstucted hit
                        // index of adc and tdc on one side (left or right) are the same. See hitreader
                        if(hit1.Component()==2){
                                indexR=hit1.BankIndex();
                                indexL=hit2.BankIndex();
                        }
                        else{
                                indexR=hit2.BankIndex();
                                indexL=hit1.BankIndex();
                        }
                        
                        pw.printf("%s\t %d\t %d\t %d\t %d\t %f\t %f\t %f\t\n", this.getDetector(), eventNb, hit1._sector, hit1._layer, hit1._component, T_hit, E_hit, startTime);
                        pw.printf("%s\t %d\t %d\t %d\t %d\t %f\t %f\t %\f\t\n", this.getDetector(), eventNb, hit2._sector, hit2._layer, hit2._component, T_hit, E_hit, startTime);
                }  // close loop over j
        } // close loop over i  		
        }

        
    }
    
    protected class HalfHit {
	HalfHit(int sector, int layer, int component, int adc, int tdc, int bank_index) {
		this._sector = sector;
		this._layer = layer;
		this._component = component;	
		this._bankindex = bank_index;

		//first step of the adc and tdc processing
		this._Eatt = (double)adc  * ((0.1956*CalibrationConstantsLoader.THICKNESS[0])/(2.)); // the 2 accounts for the splitting of the deposited energy along the two coupled paddles
		this._Tprop = ((double)tdc * CalibrationConstantsLoader.TDCTOTIMESLOPE[sector-1][layer-1][component-1])+ CalibrationConstantsLoader.TDCTOTIMEOFFSET[sector-1][layer-1][component-1] + CalibrationConstantsLoader.TIMEOFFSETSECT[sector-1][layer-1] + CalibrationConstantsLoader.TIMEOFFSETSLR[sector-1][layer-1] ; // And other constants!
        }

        private double _Eatt;      // Attenuated energy (MeV) at the upstream end of the paddle 
        private double _Tprop;     // Time (ns) at the upstream end of the paddle

        private int _sector;       // sector (block) of the CND 				
        private int _layer;        // layer in which the signal is registered
        private int _component;    // component (paddle) with which the signal is associated

        private int _bankindex;    // Index of the signal in the raw CND bank

        public int Sector() {
                return _sector;
        }	

        public int Layer() {
                return _layer;
        }	

        public int Component() {
                return _component;
        }

        public int BankIndex() {
                return _bankindex;
        }


        public double Eatt() {
                return _Eatt;
        }

        public double Tprop() {
                return _Tprop;
        }
	}

}
