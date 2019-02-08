
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.utils.options.OptionParser;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.rec.dc.Constants;
import org.jlab.util.dedigitizer.detectors.BMTDedigitized;
import org.jlab.util.dedigitizer.detectors.BSTDedigitized;
import org.jlab.util.dedigitizer.detectors.CNDDedigitized;
import org.jlab.util.dedigitizer.detectors.CTOFDedigitized;
import org.jlab.util.dedigitizer.detectors.DCDedigitized;
import org.jlab.util.dedigitizer.detectors.ECALDedigitized;
import org.jlab.util.dedigitizer.detectors.FMTDedigitized;
import org.jlab.util.dedigitizer.detectors.FTOFDedigitized;

import org.jlab.detector.base.DetectorType;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.utils.CLASResources;
import org.jlab.utils.groups.IndexedTable;
/**
 *
 * @author ziegler
 */
public class Engine extends ReconstructionEngine {
    
    public Engine() throws FileNotFoundException {
        super("DeDigitizer", "ziegler", "1.0");
        
    }
    private AtomicInteger Run = new AtomicInteger(0);
    private double triggerPhase;
    private int newRun = 0;
    DCGeant4Factory dcDetector;
    String clasDictionaryPath ;
    String variationName = "default";

    public static double StartTime;
    
    PrintWriter pw;
    
    BMTDedigitized Bmt;
    BSTDedigitized Bst;
    CNDDedigitized Cnd;
    CTOFDedigitized Ctof;
    DCDedigitized Dc;
    ECALDedigitized Ecal;
    FMTDedigitized Fmt;
    FTOFDedigitized Ftof;
    
    @Override
    public boolean init() { 
        Constants.Load();
        this.LoadTables();

        Bmt     = new BMTDedigitized() ;
        Bst     = new BSTDedigitized() ;
        Cnd     = new CNDDedigitized() ;
        Ctof    = new CTOFDedigitized() ;
        Dc      = new DCDedigitized() ;
        Ecal    = new ECALDedigitized() ;
        Fmt     = new FMTDedigitized() ;
        Ftof    = new FTOFDedigitized() ;
        return true;
    }
    int eventNb=1;
    
    public void LoadTables() {
        
        // Load tables
        clasDictionaryPath= CLASResources.getResourcePath("etc");
        String[]  dcTables = new String[]{
            "/calibration/dc/signal_generation/doca_resolution",
          //  "/calibration/dc/time_to_distance/t2d",
            "/calibration/dc/time_to_distance/time2dist",
         //   "/calibration/dc/time_corrections/T0_correction",
            "/calibration/dc/time_corrections/tdctimingcuts",
            "/calibration/dc/time_jitter",
            "/calibration/dc/tracking/wire_status",
        };

        requireConstants(Arrays.asList(dcTables));
        // Get the constants for the correct variation
        String geomDBVar = this.getEngineConfigString("geomDBVariation");
        if (geomDBVar!=null) {
            System.out.println("["+this.getName()+"] run with geometry variation based on yaml ="+geomDBVar);
        }
        else {
            geomDBVar = System.getenv("GEOMDBVAR");
            if (geomDBVar!=null) {
                System.out.println("["+this.getName()+"] run with geometry variation chosen based on env ="+geomDBVar);
            }
        } 
        if (geomDBVar==null) {
            System.out.println("["+this.getName()+"] run with default geometry");
        }
        
        // Load the geometry
        ConstantProvider provider = GeometryFactory.getConstants(DetectorType.DC, 11, Optional.ofNullable(geomDBVar).orElse("default"));
        dcDetector = new DCGeant4Factory(provider, DCGeant4Factory.MINISTAGGERON);
        for(int l=0; l<6; l++) {
            Constants.wpdist[l] = provider.getDouble("/geometry/dc/superlayer/wpdist", l);
            System.out.println("****************** WPDIST READ *********FROM "+geomDBVar+"**** VARIATION ****** "+provider.getDouble("/geometry/dc/superlayer/wpdist", l));
        }
        
        // Get the constants for the correct variation
        String ccDBVar = this.getEngineConfigString("constantsDBVariation");
        if (ccDBVar!=null) {
            System.out.println("["+this.getName()+"] run with constants variation based on yaml ="+ccDBVar);
        }
        else {
            ccDBVar = System.getenv("CCDBVAR");
            if (ccDBVar!=null) {
                System.out.println("["+this.getName()+"] run with constants variation chosen based on env ="+ccDBVar);
            }
        } 
        if (ccDBVar==null) {
            System.out.println("["+this.getName()+"] run with default constants");
        }
        // Load the calibration constants
        String dcvariationName = Optional.ofNullable(ccDBVar).orElse("default");
        variationName = dcvariationName;
        this.getConstantsManager().setVariation(dcvariationName);
    }
    
    
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        if (!event.hasBank("RUN::config")) {
            return true;
        }

       DataBank bank = event.getBank("RUN::config");
       long timeStamp = bank.getLong("timestamp", 0);
       double triggerPhase = 0;

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);
       if (newRun == 0)
           return true;

       if (Run.get() == 0 || (Run.get() != 0 && Run.get() != newRun)) {
           if (timeStamp == -1)
               return true;
 //          if (debug.get()) startTime = System.currentTimeMillis();
           IndexedTable tabJ = super.getConstantsManager().getConstants(newRun, Constants.TIMEJITTER);
           double period = tabJ.getDoubleValue("period", 0, 0, 0);
           int phase = tabJ.getIntValue("phase", 0, 0, 0);
           int cycles = tabJ.getIntValue("cycles", 0, 0, 0);

           if (cycles > 0) triggerPhase = period * ((timeStamp + phase) % cycles);

           TableLoader.FillT0Tables(newRun, this.variationName);
           TableLoader.Fill(super.getConstantsManager().getConstants(newRun, Constants.TIME2DIST));
           
           Run.set(newRun);
           Dc.TriggerPhase = triggerPhase;

       }
       Dc.setT0Array(Constants.getT0());
        /* 1 */
        // get Field
        
        Dc.AppendHits(event, pw, eventNb++, (float) 0.0);
        
       // Ecal.AppendHits(event, pw, eventNb, startTime);
       // Fmt.AppendHits(event, pw, eventNb, startTime);
        //Ftof.AppendHits(event, pw, eventNb, startTime);
        return true;
    }
    
    
    public static void main(String[] args) {

        OptionParser parser = new OptionParser();
        parser.addRequired("-o");
        parser.addOption("-st","0.0");
        System.out.println(" PROCESSING ");
        parser.parse(args);
        if(parser.hasOption("-o")==true){
            MagFieldsEngine enf = new MagFieldsEngine();
            enf.init();
            Engine en = null;
            try {
                en = new Engine();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
            List<String> inputFiles = parser.getInputList();
            //String inputFile = parser.getOption("-i").stringValue();
            String outputFile = parser.getOption("-o").stringValue();
            
            try {
                en.pw = new PrintWriter(new File(outputFile));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(parser.hasOption("-st")==true){ System.out.println(" PARSING START TIME:");
                double startTime = parser.getOption("-st").doubleValue(); System.out.println(startTime);
                if(!Double.isNaN(startTime)) 
                    en.StartTime=startTime;
            }
            en.init();  
            for(String inputFile : inputFiles){
                System.out.println(">>>>>  processing file : " + inputFile);
                try {

                    HipoDataSource reader = new HipoDataSource();
                    reader.open(inputFile);

                    while (reader.hasEvent()) {

                        DataEvent event = reader.getNextEvent();
                        enf.processDataEvent(event);
                        en.processDataEvent(event);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println(">>>>>  processing file :  failed ");
                }
            System.out.println(">>>>>  processing file :  success ");
            System.out.println();            
            }
            en.pw.close();
           
        }
    }
    
    
    
}
