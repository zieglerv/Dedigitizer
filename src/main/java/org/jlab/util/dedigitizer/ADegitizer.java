/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer;

/**
 *
 * @author ziegler
 */

public abstract class ADegitizer implements IDedigitize {
   
    private String _Detector;

    public String getDetector() {
        return _Detector;
    }

    public void setDetector(String _Detector) {
        this._Detector = _Detector;
    }
    
}
