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

package gov.nasa.kepler.systest;

import gov.nasa.kepler.common.pi.PlannedPhotometerConfigParameters;
import gov.nasa.kepler.hibernate.gar.CompressionCrud;
import gov.nasa.kepler.hibernate.gar.ExportTable;
import gov.nasa.kepler.hibernate.gar.RequantTable;
import gov.nasa.kepler.hibernate.pi.PipelineInstance;
import gov.nasa.kepler.hibernate.pi.PipelineModule;
import gov.nasa.kepler.hibernate.pi.PipelineTask;
import gov.nasa.kepler.hibernate.pi.UnitOfWorkTask;
import gov.nasa.kepler.mc.uow.SingleUowTask;
import gov.nasa.spiffy.common.pi.Parameters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SystestRequantUplinkerPipelineModule extends PipelineModule {

    public static final String MODULE_NAME = "requant-uplinker";

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public Class<? extends UnitOfWorkTask> unitOfWorkTaskType() {
        return SingleUowTask.class;
    }

    @Override
    public List<Class<? extends Parameters>> requiredParameters() {
        List<Class<? extends Parameters>> requiredParams = new ArrayList<Class<? extends Parameters>>();
        requiredParams.add(PlannedPhotometerConfigParameters.class);
        return requiredParams;
    }

    @Override
    public void processTask(PipelineInstance pipelineInstance,
        PipelineTask pipelineTask) {

        PlannedPhotometerConfigParameters photometerConfigParams = pipelineTask.getParameters(PlannedPhotometerConfigParameters.class);

        CompressionCrud compressionCrud = new CompressionCrud();

        List<RequantTable> allRequantTables = compressionCrud.retrieveAllRequantTables();

        // Find the newest table.
        long newestDatabaseId = Long.MIN_VALUE;
        for (RequantTable requantTable : allRequantTables) {
            if (requantTable.getId() > newestDatabaseId) {
                newestDatabaseId = requantTable.getId();
            }
        }

        // Set table states.
        for (RequantTable requantTable : allRequantTables) {
            if (requantTable.getId() == newestDatabaseId) {
                // Mark the newest table as uplinked.
                requantTable.setExternalId(photometerConfigParams.getCompressionExternalId());
                requantTable.setPlannedStartTime(new Date());
                requantTable.setPlannedEndTime(new Date());
                requantTable.setState(gov.nasa.kepler.hibernate.gar.ExportTable.State.UPLINKED);
            } else {
                if (photometerConfigParams.getCompressionExternalId() == requantTable.getExternalId()) {
                    // Mark any old tables as not uplinked.
                    requantTable.setExternalId(ExportTable.INVALID_EXTERNAL_ID);
                    requantTable.setState(gov.nasa.kepler.hibernate.gar.ExportTable.State.LOCKED);
                }
            }
        }
    }
}
