/*
 * Copyright 2017 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All Rights Reserved.
 * 
 * This file is available under the terms of the NASA Open Source Agreement
 * (NOSA). You should have received a copy of this agreement with the
 * Kepler source code; see the file NASA-OPEN-SOURCE-AGREEMENT.doc.
 * 
 * No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY
 * WARRANTY OF ANY KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY,
 * INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE
 * WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM
 * INFRINGEMENT, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR
 * FREE, OR ANY WARRANTY THAT DOCUMENTATION, IF PROVIDED, WILL CONFORM
 * TO THE SUBJECT SOFTWARE. THIS AGREEMENT DOES NOT, IN ANY MANNER,
 * CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY PRIOR RECIPIENT
 * OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR ANY
 * OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.
 * FURTHER, GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES
 * REGARDING THIRD-PARTY SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE,
 * AND DISTRIBUTES IT "AS IS."
 * 
 * Waiver and Indemnity: RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS
 * AGAINST THE UNITED STATES GOVERNMENT, ITS CONTRACTORS AND
 * SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT. IF RECIPIENT'S USE OF
 * THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES, DEMANDS, DAMAGES,
 * EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES FROM
 * PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT
 * SOFTWARE, RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED
 * STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY
 * PRIOR RECIPIENT, TO THE EXTENT PERMITTED BY LAW. RECIPIENT'S SOLE
 * REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE, UNILATERAL
 * TERMINATION OF THIS AGREEMENT.
 */

package gov.nasa.kepler.fc;

import gov.nasa.kepler.hibernate.fc.Geometry;
import gov.nasa.spiffy.common.persistable.Persistable;
import gov.nasa.spiffy.common.pi.PipelineException;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class GeometryModel implements Persistable {
    private double[] mjds = ArrayUtils.EMPTY_DOUBLE_ARRAY;
    private double[][] constants = new double[0][];
    private double[][] uncertainty = new double[0][];
    private FcModelMetadata fcModelMetadata = new FcModelMetadata();

    /**
     * Required by {@link Persistable}.
     */
    public GeometryModel() {
    }
    
    public GeometryModel(double[] mjds, double[][] constants) {
    	this.mjds = mjds;
    	this.constants = constants;
    	this.uncertainty = new double[constants.length][constants[0].length];
    }
   
    public GeometryModel(double[] mjds, double[][] constants, double[][] uncertainty) {
        this.mjds = mjds;
        this.constants = constants;
        this.uncertainty = uncertainty;
    }
    
    public GeometryModel(Geometry[] geometrys) {
        // Convert to arrays:
        //
        double[] mjdsArr = new double[geometrys.length];
        double[][] constantsArr = new double[geometrys.length][];
        double[][] uncertaintyArr = new double[geometrys.length][];
        for (int ii = 0; ii < geometrys.length; ++ii) {
            mjdsArr[ii] = geometrys[ii].getTime();
            constantsArr[ii] = geometrys[ii].getConstantsArray();
            uncertaintyArr[ii] = geometrys[ii].getUncertaintyArray();
        }       

        this.mjds = mjdsArr;
        this.constants = constantsArr;
        this.uncertainty = uncertaintyArr;
    }
    
    public GeometryModel(List<Geometry> geometrys) {
        // Convert to arrays:
        //
        double[] mjdsArr = new double[geometrys.size()];
        double[][] constantsArr = new double[geometrys.size()][];
        double[][] uncertaintyArr = new double[geometrys.size()][];
        
        for (int ii = 0; ii < geometrys.size(); ++ii) {
            mjdsArr[ii] = geometrys.get(ii).getTime();
            constantsArr[ii] = geometrys.get(ii).getConstantsArray();
            uncertaintyArr[ii] = geometrys.get(ii).getUncertaintyArray();
        }       

        this.mjds = mjdsArr;
        this.constants = constantsArr;
        this.uncertainty = uncertaintyArr;
    }
    
    private void checkSize() {
        boolean sizesAreSame = mjds.length == constants.length;
        if (!sizesAreSame) {
            throw new PipelineException("Inconsistent sizes in GeometryModel");
        } 
    }
    
    public int size() {
        checkSize();
        return mjds.length;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        try {
            for (int i = 0; i < size(); ++i) {
                out.append(mjds[i] + " ");
                for (int j = 0; j < constants[i].length; ++j) {
                    out.append(constants[i][j] + " ");
                }
                out.append("\n");
            }
        } catch (PipelineException p) {
            out.setLength(0);
            out.append("ERROR IN GeometryModel.toString()");
        }
        return out.toString();
    }

	public double[] getMjds() {
		return mjds;
	}

	public void setMjds(double[] mjds) {
		this.mjds = mjds;
	}

	public double[][] getConstants() {
		return constants;
	}

	public void setConstants(double[][] constants) {
		this.constants = constants;
	}

    public double[][] getUncertainty() {
        return this.uncertainty;
    }

    public void setUncertainty(double[][] uncertainty) {
        this.uncertainty = uncertainty;
    }

    public void setFcModelMetadata(FcModelMetadata fcModelMetadata) {
        this.fcModelMetadata = fcModelMetadata;
    }

    public FcModelMetadata getFcModelMetadata() {
        return fcModelMetadata;
    }
}
