%% classdef mapDataClass
%
%   Contains all intermediate and final results generated by MAP. This class is a handle so that it
%   can be passed from function to function by reference. This speeds up execution and improves
%   readibility. 
%
%   TODO: Create setter functions so that each property can be set only once.
%%
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

classdef mapDataClass < handle
    
    properties
        % These are for quick access so we are not calculating these numerous
        % times throughout the code
        nTargets = 0; % Just a int of the number of targets
        nBasisVectors = 0; % Number of regular basis vectors (not the spike Basis Vectors)
        nSpikeBasisVectors = 0; % Number of spike basis vectors
        nCadences = 0; % Number of cadences including gaps (assumed to be the same for each target)
        medianVariability = 0; % The median of the target variability, used to normalize stellar variability
    end

    properties
        alerts = []; % All alerts reported from PDC.
        mapFailed = false; % MAP was not performed?
        normTargetDataStruct; % The normalized data
        medianFlux; % [double(nTargets) Median flux for each target (found when normalizing)
        meanFlux; % [double(nTargets) Mean flux for each target (found when normalizing)
        stdFlux; % [double(nTargets) Standard Deviation flux for each target (found when normalizing)
        noiseFloor; % [double(nTargets) Standard Deviation of first differences of flux for each target (I.e. noise floor, found when normalizing)
        kic; % Structure of all kic data for easy accesibility
        centroid; % Contains the centroid motion data
        pixelData; % Data from pixels
        targetsWhereKicDataNotFound = []; % Logical array of targets where not all KIC data available  
        targetsWherePixelDataNotFound = []; % Logical array of targets where not all pixel prior data available  
        variability = []; % [double(nTarget)] Stellar Variability for each target
        uMatrix = []; % [nCadences x nSingularVectors]
        vMatrix = []; % [nCadences x nSingularVectors] 
        diagS       = []; % [nSingularVectors] Diagonal of S matrix in SVD (The Singular Values))
        basisVectors = []; %[nCadences x nBasisVectors] Basis Vectors for cotrending
        spikeBasisVectors = []; %[nCadences x nSpikeBasisVectors] Basis Vectors for cotrending
        basisVectorEntropy; %[nSingularVectors] entropy wrt Gaussian of similar width
        targetsForSvd; % Index of targets used for generating basis vectors
        targetsForGeneratingPriors; % Index of targets used for generating Prior PDFs
        robustFit = []; % [RobustFitStruct] See map_robust_fit.m
        spikeRobustFit = []; % [RobustFitStruct] See map_robust_fit.m ofr the Spike Basis Vectors
        pdf = []; % [mapPdfClass] The prior, conditional and posterior PDFs
        quickMap = []; % Information regarding quick map, see map_do_quick_map.m
    end

    methods
        function obj = mapDataClass()
        end
        
    end

end


