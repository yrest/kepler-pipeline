%*************************************************************************************************************
% function [quarters] = convert_from_cadence_to_quarter (cadences, cadenceType)
%
% Converts from a list of cadence indices to a list of quarters the cadecnes belong to.
%
% Thsi function relies on a HARD-CODED table of cadence ranges for each quarter. There are sandbox tools that can be used to awkwardly and inefficiently get this
% information but using such a method is VERY slow. The table is very fast but relies on a hard-coded lost that needs to be updated for each new quarter. Maybe
% some day the cadence ranges for each quarter will be available and automatically updated. Until then the hard-coded table will have to do.
%
% If the cadences do not fit within the listed quarter ranges below then -1
% is returned for quarters.
%
% NOTE: The cadence ranges given below are from a csv file provided by Jennifer Hall. However, they do not contain all cadences (there are gaps). The cadences in
% the gaps are included in the next quarter. For example, cadence 21007 is returned as quarter 6.
%
% Location of master .csv lists:
% svn+ssh://host/path/to/data --
%       map_lc_quarter_to_cadence_range_as_run.csv
%       map_sc_quarter_to_cadence_range_as_run.csv
%
% Inputs:
%   cadences    -- [int array] List of cadences to find quarters for
%   cadenceType -- [char] {'LONG', 'SHORT'}
%
% Outputs:
%   quarters    -- [float array(length(cadences))] List of quarters and (months if SC) corresponding to the given cadences in the form:
%                                                   quarter.month (caveat: Q4M1mod3 is given as 4.13)
%
%*************************************************************************************************************
% 
% Copyright 2017 United States Government as represented by the
% Administrator of the National Aeronautics and Space Administration.
% All Rights Reserved.
% 
% NASA acknowledges the SETI Institute's primary role in authoring and
% producing the Kepler Data Processing Pipeline under Cooperative
% Agreement Nos. NNA04CC63A, NNX07AD96A, NNX07AD98A, NNX11AI13A,
% NNX11AI14A, NNX13AD01A & NNX13AD16A.
% 
% This file is available under the terms of the NASA Open Source Agreement
% (NOSA). You should have received a copy of this agreement with the
% Kepler source code; see the file NASA-OPEN-SOURCE-AGREEMENT.doc.
% 
% No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY
% WARRANTY OF ANY KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY,
% INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE
% WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
% MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM
% INFRINGEMENT, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR
% FREE, OR ANY WARRANTY THAT DOCUMENTATION, IF PROVIDED, WILL CONFORM
% TO THE SUBJECT SOFTWARE. THIS AGREEMENT DOES NOT, IN ANY MANNER,
% CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY PRIOR RECIPIENT
% OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR ANY
% OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.
% FURTHER, GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES
% REGARDING THIRD-PARTY SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE,
% AND DISTRIBUTES IT "AS IS."
% 
% Waiver and Indemnity: RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS
% AGAINST THE UNITED STATES GOVERNMENT, ITS CONTRACTORS AND
% SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT. IF RECIPIENT'S USE OF
% THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES, DEMANDS, DAMAGES,
% EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES FROM
% PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT
% SOFTWARE, RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED
% STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY
% PRIOR RECIPIENT, TO THE EXTENT PERMITTED BY LAW. RECIPIENT'S SOLE
% REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE, UNILATERAL
% TERMINATION OF THIS AGREEMENT.
%

function [quarters] = convert_from_cadence_to_quarter (cadences, cadenceType)

switch cadenceType
    case 'LONG'
        mappingMatrix = [[0,568,1043]; ...
            [1,1105,2743]; ...
            [2,2965,7318]; ...
            [3,7404,11773]; ...
            [4,11914,16310]; ...
            [5,16373,21006]; ...
            [6,21069,25466]; ...
            [7,25509,29883]; ...
            [8,30657,33935]; ...
            [9,34237,39004]; ...
            [10,39049,43621]; ...
            [11,43667,48420]; ...
            [12,48473,52516]; ...
            [13,52551,56971]; ...
            [14,57024,61780]; ...
            [15,61886,66665]; ...
            [16,66712,70914]; ...
            [17,70976,72531]];
        
    case 'SHORT'
        mappingMatrix =[[0.0,5500,19779]; ...
            [1.0,21610,70779]; ...
            [2.1,77410,122619]; ...
            [2.2,122650,166659]; ...
            [2.3,168220,208029]; ...
            [3.1,210580,255129]; ...
            [3.2,256390,300489]; ...
            [3.3,303520,341679]; ...
            [4.1,345880,391479]; ...
            [4.2,392770,435249]; ...
            [4.3,435340,477789]; ...
            [5.1,479650,525939]; ...
            [5.2,527800,568359]; ...
            [5.3,569380,618669]; ...
            [6.1,620530,660309]; ...
            [6.2,661540,706929]; ...
            [6.3,708250,752469]; ...
            [7.1,753730,797949]; ...
            [7.2,799060,841929]; ...
            [7.3,843220,884979]; ...
            [8.1,908170,934149]; ...
            [8.2,934240,976959]; ...
            [8.3,977050,1006539]; ...
            [9.1,1015570,1069029]; ...
            [9.2,1070050,1111899]; ...
            [9.3,1112830,1158609]; ...
            [10.1,1159930,1204149]; ...
            [10.2,1205380,1251549]; ...
            [10.3,1252780,1297119]; ...
            [11.1,1298470,1344099]; ...
            [11.2,1345210,1389549]; ...
            [11.3,1390840,1441089]; ...
            [12.1,1442650,1481559]; ...
            [12.2,1482610,1522809]; ...
            [12.3,1524130,1563969]; ...
            [13.1,1564990,1612359]; ...
            [13.2,1613380,1656309]; ...
            [13.3,1657630,1697619]; ...
            [14.1,1699180,1723959]; ...
            [14.2,1746190,1790469]; ...
            [14.3,1791730,1841889]; ...
            [15.1,1845040,1890309]; ...
            [15.2,1891720,1935939]; ...
            [15.3,1937170,1988439]; ...
            [16.1,1989820,1997499]; ...
            [16.2,2014150,2067459]; ...
            [16.3,2069140,2115909]; ...
            [17.1,2117740,2150589]; ...
            [17.2,2158240,2164419]];
    otherwise
        error ('unknown cadenceType');
end

quarter      = mappingMatrix(:,1);
endCadence   = mappingMatrix(:,3);
% Do not use starting cadence in table for the beginning of each quarter. 
% Use the next cadence after the end of the previous quarter.
% I.e. fill in the gaps in the cadence ranges
%startCadence = mappingMatrix(:,2); % This line for reference purposes only
for iQuarter = 2 : length(endCadence)
    startCadence(iQuarter) = endCadence(iQuarter-1) + 1;
end

% treat Q0 as a special case
startCadence(1) = mappingMatrix(1,2);   % start cadence of Q0 for LC or SC

quarters = zeros(length(cadences),1);
for iCadence = 1 : length(cadences)
    loc = intersect(find(cadences(iCadence) >= startCadence), find(cadences(iCadence) <= endCadence));
    if (length(loc) > 1)
        error('Found more than one quarter for this cadence, something wrong with the look-up table!');
    end
    if isempty(loc)
        warning('convert_from_cadence_to_quarter: cadence ranges do not fit into any known quarter!');
        quarters(iCadence) = -1;
    else
        quarters(iCadence) = quarter(loc);
    end
end

return
