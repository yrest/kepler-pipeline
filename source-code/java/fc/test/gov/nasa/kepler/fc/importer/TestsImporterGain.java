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

package gov.nasa.kepler.fc.importer;

import gov.nasa.kepler.common.FcConstants;
import gov.nasa.kepler.common.SocEnvVars;
import gov.nasa.kepler.fc.FocalPlaneException;
import gov.nasa.kepler.fc.gain.GainOperations;
import gov.nasa.kepler.hibernate.dbservice.DatabaseService;
import gov.nasa.kepler.hibernate.dbservice.DatabaseServiceFactory;
import gov.nasa.kepler.hibernate.dbservice.TestUtils;
import gov.nasa.kepler.hibernate.fc.Gain;
import gov.nasa.spiffy.common.junit.ReflectionEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestsImporterGain {
    private static DatabaseService dbService;

    private ReflectionEquals reflectionEquals = new ReflectionEquals();

    @Before
    public void setUp() throws Exception {

        dbService = DatabaseServiceFactory.getInstance();

        TestUtils.setUpDatabase(dbService);
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.tearDownDatabase(dbService);
    }

    public void loadStuff() throws IOException {
        ImporterGain importer = new ImporterGain();
        try {
            dbService.beginTransaction();
            importer.appendNew("TestImporterGain", new Date());
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
    }

    @Test
    public void testImportIntegrity() throws Exception {
        loadStuff();

        ImporterGain importer = new ImporterGain();

        String[] gainFiles = ImporterParentNonImage.getDataFilenames(
            ImporterGain.DATAFILE_REGEX, ImporterGain.DATAFILE_DIRECTORY_NAME);
        for (String gainFile : gainFiles) {
            // Get gains from file, via importer:
            //
            List<Gain> importerGains = importer.parseFile(gainFile);
            double mjdImporterGains = importerGains.get(0)
                .getMjd();

            // Get gains from db for the MJD from the gainFile:
            //
            List<Gain> databaseGains = new ArrayList<Gain>();
            GainOperations ops = new GainOperations(dbService);
            for (int ccdModule : FcConstants.modulesList) {
                for (int ccdOutput : FcConstants.outputsList) {
                    Gain gain = ops.retrieveGain(ccdModule, ccdOutput,
                        mjdImporterGains);
                    databaseGains.add(gain);
                }
            }

            // Check for sameness:
            //
            reflectionEquals.excludeField(".*\\.id");
            reflectionEquals.excludeField(".*\\.history");
            reflectionEquals.assertEquals(importerGains, databaseGains);
        }
    }

    @Test
    public void TestAppendNewNominal() throws IOException {
        ImporterGain importer = new ImporterGain();
        try {
            dbService.beginTransaction();
            importer.appendNew(SocEnvVars.getLocalTestDataDir()
                + "/fc/gain/v2/", "this should work", new Date());
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }

        try {
            dbService.beginTransaction();
            importer.appendNew("this should work; the v3 data has a later MJ",
                new Date());
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }

    }

    @Test
    public void TestRewriteHistoryNominal() throws
        IOException {
        ImporterGain importer = new ImporterGain();
        try {
            dbService.beginTransaction();
            importer.rewriteHistory("this should work");
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
        try {
            dbService.beginTransaction();
            importer.rewriteHistory("this should also work");
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
    }

    @Test
    public void TestChangeExistingNominal() throws
        IOException {
        ImporterGain importer = new ImporterGain();
        try {
            dbService.beginTransaction();
            importer.appendNew("this should work", new Date());
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
        try {
            dbService.beginTransaction();
            importer.changeExisting("this should also work");
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
    }

    @Test(expected = FocalPlaneException.class)
    public void TestAppendNewFail() throws IOException {
        ImporterGain importer = new ImporterGain();
        try {
            dbService.beginTransaction();
            importer.appendNew("this should work", new Date());
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
        try {
            dbService.beginTransaction();
            importer.appendNew("this should fail", new Date());
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
    }

    @Test(expected = FocalPlaneException.class)
    public void TestChangeExistingFail() throws IOException,
        FocalPlaneException {
        ImporterGain importer = new ImporterGain();
        try {
            dbService.beginTransaction();
            importer.appendNew(SocEnvVars.getLocalTestDataDir()
                + "/fc/gain/v2/", "this should also work", new Date());
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
        try {
            dbService.beginTransaction();
            importer.changeExisting("this should fail; the v3 dates have a different later MJD");
            dbService.commitTransaction();
        } finally {
            dbService.rollbackTransactionIfActive();
            dbService.closeCurrentSession();
        }
    }
}
