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

package gov.nasa.kepler.ui.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.nasa.kepler.hibernate.Constraint;
import gov.nasa.kepler.hibernate.Constraint.Conjunction;
import gov.nasa.kepler.hibernate.Constraint.Operator;
import gov.nasa.kepler.hibernate.cm.Kic;
import gov.nasa.kepler.hibernate.cm.KicCrud;
import gov.nasa.kepler.hibernate.cm.SortDirection;
import gov.nasa.kepler.hibernate.dbservice.DatabaseService;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the {@link CelestialObjectOperationsProxy} class.
 * 
 * @author Bill Wohler
 */
@RunWith(JMock.class)
public class CelestialObjectOperationsProxyTest {

    private DatabaseService databaseService;

    private KicCrud kicCrud;
    private CelestialObjectOperationsProxy celestialObjectOperationsProxy;

    private final Mockery mockery = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Before
    public void setUp() {
        databaseService = mockery.mock(DatabaseService.class);
        ProxyTestHelper.createProxyAssertions(mockery, databaseService);
        kicCrud = mockery.mock(KicCrud.class);
        celestialObjectOperationsProxy = new CelestialObjectOperationsProxy(
            databaseService);
        celestialObjectOperationsProxy.setKicCrud(kicCrud);
    }

    @Test
    public void provideCodeCoverageForDefaultConstructor() {
        ProxyTestHelper.dismissProxyAssertions(databaseService);
        new CelestialObjectOperationsProxy();
    }

    @Test
    public void testGetMaxKicResultSetCount() {
        ProxyTestHelper.dismissProxyAssertions(databaseService);
        assertTrue(CelestialObjectOperationsProxy.getMaxKicResultSetCount() > 0);
    }

    @Test
    public void testRetrieveKics() {
        Kic kic = new Kic.Builder(42, 42.0, 42.0).build();
        final List<Constraint> constraints = Arrays.asList(new Constraint[] { new Constraint(
            Conjunction.AND, Kic.Field.KEPLER_ID, Operator.EQUAL, "foo") });
        final SortDirection sortDirection = SortDirection.ASCENDING;
        final int rowCount = 42;
        final List<Kic> kics = Arrays.asList(new Kic[] { kic });

        mockery.checking(new Expectations() {
            {
                one(kicCrud).retrieveKics(constraints, Kic.Field.KEPLER_ID,
                    sortDirection, rowCount);
                will(returnValue(kics));
            }
        });

        assertEquals(kics,
            celestialObjectOperationsProxy.retrieveCelestialObjects(
                constraints, Kic.Field.KEPLER_ID, sortDirection, rowCount));
    }
}
