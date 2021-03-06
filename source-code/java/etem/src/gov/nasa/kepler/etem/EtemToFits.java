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

package gov.nasa.kepler.etem;

import gov.nasa.spiffy.common.io.Filenames;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;

/**
 * Generates DMC-style cadence FITS files from pixel data generated by ETEM
 * 
 * @author Todd Klaus tklaus@arc.nasa.gov
 * 
 */
public class EtemToFits {
    private static final Log log = LogFactory.getLog(EtemToFits.class);

    private static final int NUM_CCD_ROWS = 1070;
    private static final int NUM_CCD_COLS = 1132;


    private static final String ETEM_DAT_FILENAME_PREFIX = "/long_cadence_q_black_gcr_run";
    private static final int UNTIL_EOF = -1;
    private static final int ETEM_OPT_AP_WIDTH = 11;
    private static final int ETEM_OPT_AP_HEIGHT = 11;
    // TODO changing these from 6,6 to 5,5 has not been tested.  It may be incorrect.
    private static final int ETEM_OPT_AP_CENTER_ROW = 5;
    private static final int ETEM_OPT_AP_CENTER_COL = 5;

    private String fitsDir;
    private String resultsDir;
    private int ccdModule;
    private int ccdOutput;
    private int observingQuarter;
    private int startCadence = 0;
    private int endCadence = UNTIL_EOF;

    private String runNum;

    private int numStarPixelsRead = 0;
    private int numBkgrnPixelsRead = 0;
    private int numCollateralPixelsRead = 0;

    /**
     * current cadence number being processed
     */
    private int cadenceNumber = 0;

    private boolean bigEndian = true;

    /**
     * This ctor is used when processing all available cadences
     * 
     * @param fitsDir
     * @param resultsDir
     * @param ccdModule
     * @param ccdOutput
     * @param observingQuarter
     */
    public EtemToFits(String fitsDir, String resultsDir, int ccdModule, int ccdOutput, int observingQuarter) {
        this.fitsDir = fitsDir;
        this.resultsDir = resultsDir;
        this.ccdModule = ccdModule;
        this.ccdOutput = ccdOutput;
        this.observingQuarter = observingQuarter;
    }

    /**
     * This ctor is used when processing a specific cadence range, typically by
     * the Etem2FitsPipelineModule
     * 
     * @param fitsDir
     * @param resultsDir
     * @param ccdModule
     * @param ccdOutput
     * @param observingQuarter
     * @param startCadence
     * @param endCadence
     */
    public EtemToFits(String fitsDir, String resultsDir, int ccdModule, int ccdOutput, int observingQuarter,
        int startCadence, int endCadence) {
        super();
        this.fitsDir = fitsDir;
        this.resultsDir = resultsDir;
        this.ccdModule = ccdModule;
        this.ccdOutput = ccdOutput;
        this.observingQuarter = observingQuarter;
        this.startCadence = startCadence;
        this.endCadence = endCadence;
    }

    /**
     * 
     * open long_cadence_quant_gcr_run###.dat open target_defs_run###.mat load
     * aperture_masks.mat
     * 
     * for each cadence processTargetPixels processBkgrndPixels
     * processCollateralPixels
     * 
     * @throws Exception
     */
    public void generateFits() throws Exception {

        runNum = "" + (ccdModule * 10000 + ccdOutput * 100 + observingQuarter);

        if (System.getProperty("sun.cpu.endian").equals("little")) {
            bigEndian = false;
        }

        log.debug("reading mat files...");

        MatFileReader targetDefsMat = new MatFileReader(resultsDir + "/run" + runNum + "/target_defs_run" + runNum
            + ".mat");
        Map<String, MLArray> targetDefsContent = targetDefsMat.getContent();

        // MatFileReader runParamsMat = new MatFileReader(resultsDir + "/run" +
        // runNum + "/run_params_run" + runNum
        // + ".mat");
        // Map<String, MLArray> runParamsContent = runParamsMat.getContent();

        log.debug("DONE reading mat files");

        if (log.isDebugEnabled()) {
            dump("target_defs", targetDefsMat);
        }

        DataInputStream pixelData = new DataInputStream(new BufferedInputStream(new FileInputStream(resultsDir + "/run"
            + runNum + ETEM_DAT_FILENAME_PREFIX + runNum + ".dat")));

        MLStructure targetDefs = (MLStructure) targetDefsContent.get("target_defs");
        MLStructure apertureMasks = (MLStructure) targetDefsContent.get("aperture_masks");
        MLDouble optimalAps = (MLDouble) targetDefsContent.get("aps");
        int numStarPixels = (int) getDoubleValue(targetDefsContent.get("numstarpixels"));
        MLDouble kBackPix = (MLDouble) targetDefsContent.get("kbackpix2");
        int numBackgroundPixels = kBackPix.getM();
        int ccdRows = (int) getDoubleValue(targetDefsContent.get("ccdRows"));
        int ccdCols = (int) getDoubleValue(targetDefsContent.get("ccdCols"));

        if (startCadence != 0) {
            /*
             *  TODO: this simplified approach assumes that the first cadence in the ETEM
             *  file is cadence 0.  In order to handle the case where ETEM data starts later,
             *  we'll need to figure out a way to get the cadence # out of etem...
             */

            // all values in the .dat file are 4-byte floats
            int bytesPerCadence = numStarPixels * 4 + numBackgroundPixels * 4 + ccdCols * 4 + // # of masked smear pixels 
                ccdCols * 4 + // # of virtual smear pixels
                ccdRows * 4; // # of leading black pixels

            pixelData.skip(startCadence * bytesPerCadence);
            cadenceNumber = startCadence;
        } else {
            cadenceNumber = 0;
        }

        try {
            while (true) { // for each cadence (until EOF or we reach endCadence)

                log.info("Processing (module/output/cadenceNumber) = (" + ccdModule + "/" + ccdOutput + "/"
                    + cadenceNumber + ")");

                log.debug("reading target pixels");
                processTargetPixels(pixelData, targetDefs, apertureMasks, optimalAps);
                log.debug("DONE reading target pixels");

                log.debug("reading background pixels");
                processBackgroundPixels(pixelData, kBackPix, ccdRows, ccdCols);
                log.debug("DONE reading background pixels");

                log.debug("reading collateral pixels");
                processCollateralPixels(pixelData);
                log.debug("DONE reading collateral pixels");

                log.debug("DONE cadenceNumber = " + cadenceNumber);

                if (cadenceNumber == endCadence) {
                    log.info("reached endCadence = " + endCadence + ", end of run");
                    break;
                }
                cadenceNumber++;
            }
        } catch (EOFException e) {
            log.info("caught EOF, end of run");
        }

        log.info("cadenceNumber = " + cadenceNumber);
        log.info("numStarPixelsRead = " + numStarPixelsRead);
        log.info("numBkgrnPixelsRead = " + numBkgrnPixelsRead);
        log.info("numCollateralPixelsRead = " + numCollateralPixelsRead);
    }

    /**
     * Read the target (star flux) pixels from the .dat file using the target
     * defs and optimal aps as a guide
     * 
     * <pre>
     * Star Pixels
     * 
     * for each target 
     *   read target def for this target 
     *   read 1x121 array from optaps for this target 
     *   read 11x11 mask from aperture masks for this target 
     *   for each pixel in the mask 
     *     if optaps(pixel) == 1 
     *       pixel value = read from lc dat file 
     *     else 
     *       pixel value = 0? - some dummy value 
     *     write pixel value to pixel data fits file 
     *     compute row, column from target def, mask 
     *     write metadata to pixel mapping reference file data
     * </pre>
     * 
     * @param pixelData
     * @param targetDefs
     * @param apertureMasks
     * @param optimalAps
     */
    private void processTargetPixels(DataInputStream pixelData, MLStructure targetDefs, MLStructure apertureMasks,
        MLDouble optimalAps) throws Exception {

        TargetPmrfFits ctlFits = null; //new TargetMapCadenceDataSet(fitsDir, CadenceType.LONG, cadenceNumber, ccdModule, ccdOutput, 0, 0);
        TargetCadenceFits fits = null; //new TargetCadenceDataSet(fitsDir, CadenceType.LONG, cadenceNumber, ccdModule, ccdOutput, 0, 0);

        int numTargets = targetDefs.getN();
        log.debug("numTargets = " + numTargets);

        int pixelsReadThisCadence = 0;
        int optApPixelsUsedCount = 0;
        int maskPixelsUsedCount = 0;
        boolean hasNext = false; // the next tgt def is for the same kicid

        for (int tgtIdx = 0; tgtIdx < numTargets; tgtIdx++) {
            hasNext = (getDoubleValue(targetDefs.getField("has_next", tgtIdx)) == 1.0 ? true : false);
            int kicId = (int) getDoubleValue(targetDefs.getField("kicid", tgtIdx));
            int maskIdx = (int) getDoubleValue(targetDefs.getField("mask_index", tgtIdx));
            double[][] apertureMask = ((MLDouble) apertureMasks.getField("mask", maskIdx - 1)).getArray();
            int refRow = (int) getDoubleValue(targetDefs.getField("ref_row", tgtIdx));
            int refCol = (int) getDoubleValue(targetDefs.getField("ref_col", tgtIdx));
            double[][] optimalAperture = ((MLDouble) targetDefs.getField("opt_ap", tgtIdx)).getArray();
            int optApRefRow = (int) getDoubleValue(targetDefs.getField("opt_ap_ref_row", tgtIdx));
            int optApRefCol = (int) getDoubleValue(targetDefs.getField("opt_ap_ref_col", tgtIdx));
            int expectedOptApPixelsUsedCount = (int) getDoubleValue(targetDefs.getField("opt_ap_count", tgtIdx));

            int optApOffsetRow = refRow - optApRefRow;
            int optApOffsetCol = refCol - optApRefCol;

            if (log.isDebugEnabled()) {
                log.debug("target index = " + tgtIdx);
                log.debug("  kicId = " + kicId);
                log.debug("  maskIdx = " + maskIdx);
                log.debug("  maskRefRow = " + refRow);
                log.debug("  maskRefCol = " + refCol);
                log.debug("  optApRefRow = " + optApRefRow);
                log.debug("  optApRefCol = " + optApRefCol);
                log.debug("  optApOffsetRow = " + optApOffsetRow);
                log.debug("  optApOffsetCol = " + optApOffsetCol);

                dumpMatrix("apertureMask", apertureMask);
                dumpMatrix("optimalAperture", optimalAperture);
            }

            for (int row = 0; row < apertureMask.length; row++) {
                for (int col = 0; col < apertureMask[row].length; col++) {
                    if (apertureMask[row][col] == 1) {
                        maskPixelsUsedCount++;
                        int optApRow = row + optApOffsetRow;
                        int optApCol = col + optApOffsetCol;

                        log.debug("m(" + row + "," + col + ") -> oa(" + optApRow + "," + optApCol + ")");

                        float pixelValue = 0;

                        if ((optApRow >= 0) && (optApRow <= (ETEM_OPT_AP_HEIGHT - 1)) && (optApCol >= 0)
                            && (optApCol <= (ETEM_OPT_AP_WIDTH - 1)) && (optimalAperture[optApRow][optApCol] == 1)) {
                            // etem generated a value for this pixel, read etem
                            // data file
                            int v = pixelData.readInt();
                            if (!bigEndian) {
                                v = Integer.reverseBytes(v); // for intel
                                // arch
                            }
                            pixelValue = Float.intBitsToFloat(v);
                            numStarPixelsRead++;
                            pixelsReadThisCadence++;
                            optApPixelsUsedCount++;
                        } else {
                            // this pixel is outside the optimal aperture,
                            // so there's no data available from etem
                            // for now, just use 0
                            // todo: use nearest neighbor in etem data?
                        }

                        // the ccd row/col is the optimal aperture reference
                        // row/col
                        // plus the distance from the center of the optimal
                        // aperture (6,6)
                        // (the optimal ap ref row/col is the center of the
                        // optimal aperture)

                        short ccdRow = (short) (optApRefRow + (optApRow - ETEM_OPT_AP_CENTER_ROW));
                        short ccdCol = (short) (optApRefCol + (optApCol - ETEM_OPT_AP_CENTER_COL));

                        log.debug("pixel (" + ccdRow + "," + ccdCol + ") = " + pixelValue);

//                        ctlFits.addRow(tgtIdx, (short) maskIdx, ccdRow, ccdCol);
//                        fits.addRow((int) pixelValue, (int) pixelValue);
                    }
                }
            }

            if (!hasNext) {
                if (expectedOptApPixelsUsedCount != optApPixelsUsedCount) {
                    log.warn("numTargets = " + numTargets);
                    log.warn("tgtIdx = " + tgtIdx);
                    log.warn("expectedOptApCount = " + expectedOptApPixelsUsedCount);
                    log.warn("actualOptApCount = " + optApPixelsUsedCount);
                    log.warn("maskCount = " + maskPixelsUsedCount);
                    throw new Exception("expected != actual");
                }
                // reset the counters because the next tgt def is for a different opt ap (diff star)
                optApPixelsUsedCount = 0;
                maskPixelsUsedCount = 0;
            }
        }

        ctlFits.save();
        fits.save();

        log.debug("target pixels read = " + pixelsReadThisCadence);
    }

    /**
     * Background Pixels (treat each bkpix as a target since etem does not
     * group)
     * 
     * <pre>
     * for each background pixel (length of kbackpix) 
     *   read from lc dat file
     *   write pixel value to pixel data fits file 
     *   write metadata to pixel mapping reference file
     * </pre>
     * 
     * @param pixelData
     * @param backPix
     * @param ccdCols
     * @param ccdRows
     * @throws Exception
     */
    private void processBackgroundPixels(DataInputStream pixelData, MLDouble backPix, int ccdRows, int ccdCols)
        throws Exception {

//        BackgroundPmrfFits ctlFits = null; //new BkgrndMapCadenceDataSet(fitsDir, CadenceType.LONG, cadenceNumber, ccdModule, ccdOutput, 0, 0);
//        BackgroundCadenceFits fits = null; //new BkgrndCadenceDataSet(fitsDir, CadenceType.LONG, cadenceNumber, ccdModule, ccdOutput, 0, 0);

        double[][] backgroundPixelLocations = backPix.getArray();

        log.debug("numBackgroundPixels = " + backgroundPixelLocations.length);

        float pixelValue;
        int tgtIdx = 0;

        for (int i = 0; i < backgroundPixelLocations.length; i++) {
            int v = pixelData.readInt();
            if (!bigEndian) {
                v = Integer.reverseBytes(v); // for intel arch
            }
            pixelValue = Float.intBitsToFloat(v);

            int offset = (int) backgroundPixelLocations[i][0];

            short row = (short) (offset % ccdRows);
            short col = (short) (offset / ccdRows);

            log.debug("bkgrnd pixel (" + row + "," + col + ") = " + pixelValue);

//            ctlFits.addRow(tgtIdx, (short) 0, row, col);
//            fits.addRow((int) pixelValue, (int) pixelValue);

            numBkgrnPixelsRead++;
            tgtIdx++;
        }
//        ctlFits.save();
//        fits.save();
    }

    /**
     * Collateral Pixels
     * 
     * for each collateral type read from lc dat file write pixel value to pixel
     * data fits file write metadata to pixel mapping reference file
     * 
     * @param pixelData
     * @throws Exception
     */
    private void processCollateralPixels(DataInputStream pixelData) throws Exception {

        short maskedSmearCol = 0;
        short virtualSmearCol = 0;
        short leadingBlackRow = 0;
        float pixelValue = 0.0f;

        CollateralPmrfFits ctlFits = null; //new CollateralMapCadenceDataSet(fitsDir, CadenceType.LONG, cadenceNumber, ccdModule, ccdOutput, 0, 0);
        CollateralCadenceFits fits = null; //new CollateralCadenceDataSet(fitsDir, CadenceType.LONG, cadenceNumber, ccdModule, ccdOutput, 0, 0);

        // masked smear
        for (int i = 0; i < NUM_CCD_COLS; i++) {
            int v = pixelData.readInt();
            if (!bigEndian) {
                v = Integer.reverseBytes(v); // for intel arch
            }
            pixelValue = Float.intBitsToFloat(v);
//            ctlFits.addRow(CollateralType.MASKED_SMEAR.byteValue(), maskedSmearCol, -1);
//            fits.addRow((int) pixelValue, (int) pixelValue);
            maskedSmearCol++;
            numCollateralPixelsRead++;
        }

        // virtual smear
        for (int i = 0; i < NUM_CCD_COLS; i++) {
            int v = pixelData.readInt();
            if (!bigEndian) {
                v = Integer.reverseBytes(v); // for intel arch
            }
            pixelValue = Float.intBitsToFloat(v);
//            ctlFits.addRow(CollateralType.VIRTUAL_SMEAR.byteValue(), virtualSmearCol, -1);
//            fits.addRow((int) pixelValue, (int) pixelValue);
            virtualSmearCol++;
            numCollateralPixelsRead++;
        }

        // leading black
        for (int i = 0; i < NUM_CCD_ROWS; i++) {
            int v = pixelData.readInt();
            if (!bigEndian) {
                v = Integer.reverseBytes(v); // for intel arch
            }
            pixelValue = Float.intBitsToFloat(v);
//            ctlFits.addRow(CollateralType.BLACK_LEVEL.byteValue(), leadingBlackRow, -1);
//            fits.addRow((int) pixelValue, (int) pixelValue);
            leadingBlackRow++;
            numCollateralPixelsRead++;
        }

        ctlFits.save();
        fits.save();
    }

    /**
     * 
     * @param array
     * @return
     */
    private double getDoubleValue(MLArray array) {
        MLDouble doubleArray = (MLDouble) array;
        return doubleArray.get(0).doubleValue();
    }

    /**
     * 
     * @param matReader
     */
    private void dump(String matName, MatFileReader matReader) {
        log.debug("MAT file name = " + matName);
        Map<String, MLArray> content = matReader.getContent();
        for (String name : content.keySet()) {
            log.debug("  name = " + name);
            MLArray value = content.get(name);
            log.debug("    value type = " + value.getClass());
            // log.debug("contentToString = " + value.contentToString() );

            int[] dims = value.getDimensions();
            for (int i = 0; i < dims.length; i++) {
                log.debug("    dims[ " + i + " ] = " + dims[i]);
            }

            log.debug("    getM = " + value.getM());
            log.debug("    getN = " + value.getN());
        }
    }

    private void dumpMatrix(String label, double[][] matrix) {
        log.debug(label);
        for (int row = 0; row < matrix.length; row++) {
            StringBuilder sb = new StringBuilder();
            for (int col = 0; col < matrix[row].length; col++) {
                sb.append(" " + (int) matrix[row][col] + " ");
            }
            log.debug(sb);
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure(Filenames.ETC + Filenames.LOG4J_CONFIG);

        EtemToFits etem2Fits = new EtemToFits("/data/etem/results/6/fits3", "/data/etem/results/6/", 7, 2, 0);

        etem2Fits.generateFits();
    }
}
