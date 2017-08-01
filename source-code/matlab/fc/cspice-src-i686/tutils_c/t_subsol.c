/* t_subsol.f -- translated by f2c (version 19980913).
   You must link the resulting object file with the libraries:
	-lf2c -lm   (in that order)
*/

#include "f2c.h"

/* $Procedure      T_SUBSOL ( Sub-solar point ) */
/* Subroutine */ int t_subsol__(char *method, char *target, doublereal *et, 
	char *abcorr, char *obsrvr, doublereal *spoint, ftnlen method_len, 
	ftnlen target_len, ftnlen abcorr_len, ftnlen obsrvr_len)
{
    /* Initialized data */

    static doublereal origin[3] = { 0.,0.,0. };

    doublereal radii[3];
    extern /* Subroutine */ int chkin_(char *, ftnlen), errch_(char *, char *,
	     ftnlen, ftnlen), ltime_(doublereal *, integer *, char *, integer 
	    *, doublereal *, doublereal *, ftnlen);
    logical found;
    extern logical eqstr_(char *, char *, ftnlen, ftnlen);
    doublereal sunlt;
    extern /* Subroutine */ int bods2c_(char *, integer *, logical *, ftnlen);
    integer obscde;
    doublereal lt;
    integer frcode;
    extern /* Subroutine */ int cidfrm_(integer *, integer *, char *, logical 
	    *, ftnlen);
    integer nradii;
    char frname[80];
    integer trgcde;
    extern /* Subroutine */ int bodvar_(integer *, char *, integer *, 
	    doublereal *, ftnlen);
    doublereal ettarg;
    extern /* Subroutine */ int nearpt_(doublereal *, doublereal *, 
	    doublereal *, doublereal *, doublereal *, doublereal *), sigerr_(
	    char *, ftnlen), chkout_(char *, ftnlen), setmsg_(char *, ftnlen);
    extern logical return_(void);
    extern /* Subroutine */ int spkpos_(char *, doublereal *, char *, char *, 
	    char *, doublereal *, doublereal *, ftnlen, ftnlen, ftnlen, 
	    ftnlen), surfpt_(doublereal *, doublereal *, doublereal *, 
	    doublereal *, doublereal *, doublereal *, logical *);
    doublereal alt, pos[3];

/* $ Abstract */

/*     Determine the coordinates of the sub-solar point on a target */
/*     body as seen by a specified observer at a specified epoch, */
/*     optionally corrected for planetary (light time) and stellar */
/*     aberration. */

/*     This test utility differs from the SPICELIB routine SUBSOL in */
/*     that this routine allows the observer and target to coincide. */

/* $ Disclaimer */

/*     THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE */
/*     CALIFORNIA INSTITUTE OF TECHNOLOGY (CALTECH) UNDER A U.S. */
/*     GOVERNMENT CONTRACT WITH THE NATIONAL AERONAUTICS AND SPACE */
/*     ADMINISTRATION (NASA). THE SOFTWARE IS TECHNOLOGY AND SOFTWARE */
/*     PUBLICLY AVAILABLE UNDER U.S. EXPORT LAWS AND IS PROVIDED "AS-IS" */
/*     TO THE RECIPIENT WITHOUT WARRANTY OF ANY KIND, INCLUDING ANY */
/*     WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR A */
/*     PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC */
/*     SECTIONS 2312-2313) OR FOR ANY PURPOSE WHATSOEVER, FOR THE */
/*     SOFTWARE AND RELATED MATERIALS, HOWEVER USED. */

/*     IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA */
/*     BE LIABLE FOR ANY DAMAGES AND/OR COSTS, INCLUDING, BUT NOT */
/*     LIMITED TO, INCIDENTAL OR CONSEQUENTIAL DAMAGES OF ANY KIND, */
/*     INCLUDING ECONOMIC DAMAGE OR INJURY TO PROPERTY AND LOST PROFITS, */
/*     REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE ADVISED, HAVE */
/*     REASON TO KNOW, OR, IN FACT, SHALL KNOW OF THE POSSIBILITY. */

/*     RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF */
/*     THE SOFTWARE AND ANY RELATED MATERIALS, AND AGREES TO INDEMNIFY */
/*     CALTECH AND NASA FOR ALL THIRD-PARTY CLAIMS RESULTING FROM THE */
/*     ACTIONS OF RECIPIENT IN THE USE OF THE SOFTWARE. */

/* $ Required_Reading */

/*     FRAME */
/*     PCK */
/*     SPK */
/*     TIME */

/* $ Keywords */

/*     GEOMETRY */

/* $ Declarations */
/* $ Brief_I/O */

/*     Variable  I/O  Description */
/*     --------  ---  -------------------------------------------------- */
/*     METHOD     I   Computation method. */
/*     TARGET     I   Name of target body. */
/*     ET         I   Epoch in ephemeris seconds past J2000 TDB. */
/*     ABCORR     I   Aberration correction. */
/*     OBSRVR     I   Name of observing body. */
/*     SPOINT     O   Sub-solar point on the target body. */

/* $ Detailed_Input */

/*     METHOD      is a short string specifying the computation method */
/*                 to be used.  The choices are: */

/*                    'Near point'       The sub-solar point is defined */
/*                                       as the nearest point on the */
/*                                       target to the sun. */

/*                    'Intercept'        The sub-observer point is */
/*                                       defined as the target surface */
/*                                       intercept of the line */
/*                                       containing the target's center */
/*                                       and the sun's center. */

/*                 In both cases, the intercept computation treats the */
/*                 surface of the target body as a triaxial ellipsoid. */
/*                 The ellipsoid's radii must be available in the kernel */
/*                 pool. */

/*                 Neither case nor white space are significant in */
/*                 METHOD.  For example, the string ' NEARPOINT' is */
/*                 valid. */


/*     TARGET      is the name of the target body.  TARGET is */
/*                 case-insensitive, and leading and trailing blanks in */
/*                 TARGET are not significant. Optionally, you may */
/*                 supply a string containing the integer ID code for */
/*                 the object.  For example both 'MOON' and '301' are */
/*                 legitimate strings that indicate the moon is the */
/*                 target body. */

/*                 This routine assumes that the target body is modeled */
/*                 by a tri-axial ellipsoid, and that a PCK file */
/*                 containing its radii has been loaded into the kernel */
/*                 pool via FURNSH. */


/*     ET          is the epoch in ephemeris seconds past J2000 at which */
/*                 the sub-solar point on the target body is to be */
/*                 computed. */


/*     ABCORR      indicates the aberration corrections to be applied */
/*                 when computing the observer-target state.  ABCORR */
/*                 may be any of the following. */

/*                    'NONE'     Apply no correction. Return the */
/*                               geometric sub-solar point on the target */
/*                               body. */

/*                    'LT'       Correct for planetary (light time) */
/*                               aberration.  Both the state and rotation */
/*                               of the target body are corrected for one */
/*                               way light time from target to observer. */

/*                               The state of the sun relative to the */
/*                               target is corrected for one way light */
/*                               from the sun to the target; this state */
/*                               is evaluated at the epoch obtained by */
/*                               retarding ET by the one way light time */
/*                               from target to observer. */

/*                    'LT+S'     Correct for planetary (light time) and */
/*                               stellar aberrations.  Light time */
/*                               corrections are the same as in the 'LT' */
/*                               case above.  The target state is */
/*                               additionally corrected for stellar */
/*                               aberration as seen by the observer, and */
/*                               the sun state is corrected for stellar */
/*                               aberration as seen from the target. */

/*                    'CN'       Converged Newtonian light time */
/*                               corrections.  This is the same as LT */
/*                               corrections but with further iterations */
/*                               to a converged Newtonian light time */
/*                               solution.  Given that relativistic */
/*                               effects may be as large as the higher */
/*                               accuracy achieved by this computation, */
/*                               this is correction is seldom worth the */
/*                               additional computations required unless */
/*                               the user incorporates additional */
/*                               relativistic corrections.  Light */
/*                               time corrections are applied as in the */
/*                               'LT' case. */

/*                    'CN+S'     Converged Newtonian light time */
/*                               corrections and stellar aberration. */
/*                               Light time and stellar aberration */
/*                               corrections are applied as in the */
/*                               'LT+S' case. */


/*     OBSRVR      is the name of the observing body, typically a */
/*                 spacecraft, the earth, or a surface point on the */
/*                 earth.  OBSRVR is case-insensitive, and leading and */
/*                 trailing blanks in OBSRVR are not significant. */
/*                 Optionally, you may supply a string containing the */
/*                 integer ID code for the object.  For example both */
/*                 'EARTH' and '399' are legitimate strings that indicate */
/*                 the earth is the observer. */

/* $ Detailed_Output */

/*     SPOINT      is the sub-solar point on the target body at ET */
/*                 expressed relative to the body-fixed frame of the */
/*                 target body. */

/*                 The sub-solar point is defined either as the point on */
/*                 the target body that is closest to the sun, or the */
/*                 target surface intercept of the line containing the */
/*                 target's center and the sun's center; the input */
/*                 argument METHOD selects the definition to be used. */

/*                 The body-fixed frame, which is time-dependent, is */
/*                 evaluated at ET if ABCORR is 'NONE'; otherwise the */
/*                 frame is evaluated at ET-LT, where LT is the one way */
/*                 light time from target to observer. */

/*                 The state of the target body is corrected for */
/*                 aberration as specified by ABCORR; the corrected */
/*                 state is used in the geometric computation.  As */
/*                 indicated above, the rotation of the target is */
/*                 retarded by one way light time if ABCORR specifies */
/*                 that light time correction is to be done. */

/*                 The state of the sun as seen from the observing */
/*                 body is also corrected for aberration as specified */
/*                 by ABCORR.  The corrections, when selected, are */
/*                 applied at the epoch ET-LT, where LT is the one way */
/*                 light time from target to observer. */


/* $ Parameters */

/*     None. */

/* $ Exceptions */

/*     If any of the listed errors occur, the output arguments are */
/*     left unchanged. */


/*     1) If the input argument METHOD is not recognized, the error */
/*        SPICE(DUBIOUSMETHOD) is signaled. */

/*     2) If either of the input body names TARGET or OBSRVR cannot be */
/*        mapped to NAIF integer codes, the error SPICE(IDCODENOTFOUND) */
/*        is signaled. */

/*     3) If OBSRVR and TARGET map to the same NAIF integer ID codes, the */
/*        error SPICE(BODIESNOTDISTINCT) is signaled. */

/*     4) If frame definition data enabling the evaluation of the state */
/*        of the target relative to the observer in target body-fixed */
/*        coordinates have not been loaded prior to calling T_SUBSOL, the */
/*        error will be diagnosed and signaled by a routine in the call */
/*        tree of this routine. */

/*     5) If the specified aberration correction is not recognized, the */
/*        error will be diagnosed and signaled by a routine in the call */
/*        tree of this routine. */

/*     6) If insufficient ephemeris data have been loaded prior to */
/*        calling T_SUBSOL, the error will be diagnosed and signaled by */
/*        a routine in the call tree of this routine. */

/*     7) If the triaxial radii of the target body have not been loaded */
/*        into the kernel pool prior to calling T_SUBSOL, the error will */
/*        be diagnosed and signaled by a routine in the call tree of */
/*        this routine. */

/*     8) The target must be an extended body:  if any of the radii of */
/*        the target body are non-positive, the error will be diagnosed */
/*        and signaled by routines in the call tree of this routine. */

/*     9) If PCK data supplying a rotation model for the target body */
/*        have not been loaded prior to calling T_SUBSOL, the error will */
/*        be diagnosed and signaled by a routine in the call tree of */
/*        this routine. */

/* $ Files */

/*     Appropriate SPK, PCK, and frame data must be available to */
/*     the calling program before this routine is called.  Typically */
/*     the data are made available by loading kernels; however the */
/*     data may be supplied via subroutine interfaces if applicable. */

/*     The following data are required: */

/*        - SPK data:  ephemeris data for sun, target, and observer must */
/*          be loaded.  If aberration corrections are used, the states of */
/*          sun, target, and observer relative to the solar system */
/*          barycenter must be calculable from the available ephemeris */
/*          data. Ephemeris data are made available by loading */
/*          one or more SPK files via FURNSH. */

/*        - PCK data:  triaxial radii for the target body must be loaded */
/*          into the kernel pool.  Typically this is done by loading a */
/*          text PCK file via FURNSH. */

/*        - Further PCK data:  a rotation model for the target body must */
/*          be loaded.  This may be provided in a text or binary PCK */
/*          file which is loaded via FURNSH. */

/*        - Frame data:  if a frame definition is required to convert */
/*          the sun, observer, and target states to the body-fixed frame */
/*          of the target, that definition must be available in the */
/*          kernel pool.  Typically the definition is supplied by loading */
/*          a frame kernel via FURNSH. */

/*     In all cases, kernel data are normally loaded once per program */
/*     run, NOT every time this routine is called. */

/* $ Particulars */

/*     T_SUBSOL computes the sub-solar point on a target body, as seen by */
/*     a specified observer. */

/*     There are two different popular ways to define the sub-solar */
/*     point:  "nearest point on target to the sun" or "target surface */
/*     intercept of line containing target and sun."  These coincide */
/*     when the target is spherical and generally are distinct otherwise. */

/*     When comparing sub-point computations with results from sources */
/*     other than SPICE, it's essential to make sure the same geometric */
/*     definitions are used. */

/* $ Examples */


/*     In the following example program, the file MGS.BSP is a */
/*     hypothetical binary SPK ephemeris file containing data for the */
/*     Mars Global Surveyor orbiter.  The SPK file de405s.bsp contains */
/*     data for the planet barycenters as well as the Earth, Moon, and */
/*     Sun for the time period including the date 1997 Dec 31 12:000 */
/*     UTC. MGS0000A.TPC is a planetary constants kernel file */
/*     containing radii and rotation model constants.  MGS00001.TLS is */
/*     a leapseconds file.  (File names shown here that are specific */
/*     to MGS are not names of actual files.) */

/*           IMPLICIT NONE */

/*           CHARACTER*25          METHOD ( 2 ) */

/*           INTEGER               I */

/*           DOUBLE PRECISION      DPR */
/*           DOUBLE PRECISION      ET */
/*           DOUBLE PRECISION      LAT */
/*           DOUBLE PRECISION      LON */
/*           DOUBLE PRECISION      RADIUS */
/*           DOUBLE PRECISION      SPOINT ( 3 ) */

/*           DATA                  METHOD / 'Intercept', 'Near point' / */

/*     C */
/*     C     Load kernel files. */
/*     C */
/*           CALL FURNSH ( 'MGS00001.TLS' ) */
/*           CALL FURNSH ( 'MGS0000A.TPC' ) */
/*           CALL FURNSH ( 'de405s.bsp'   ) */
/*           CALL FURNSH ( 'MGS.BSP'      ) */

/*     C */
/*     C     Convert the UTC request time to ET (seconds past */
/*     C     J2000, TDB). */
/*     C */
/*           CALL STR2ET ( '1997 Dec 31 12:00:00', ET ) */

/*     C */
/*     C     Compute sub-spacecraft point using light time and stellar */
/*     C     aberration corrections.  Use the "target surface intercept" */
/*     C     definition of sub-spacecraft point on the first loop */
/*     C     iteration, and use the "near point" definition on the */
/*     C     second. */
/*     C */
/*           DO I = 1, 2 */

/*              CALL T_SUBSOL ( METHOD(I), */
/*          .                 'MARS',  ET,  'LT+S',  'MGS',  SPOINT ) */

/*     C */
/*     C        Convert rectangular coordinates to planetocentric */
/*     C        latitude and longitude.  Convert radians to degrees. */
/*     C */
/*              CALL RECLAT ( SPOINT, RADIUS, LON, LAT  ) */

/*              LON = LON * DPR () */
/*              LAT = LAT * DPR () */

/*     C */
/*     C        Write the results. */
/*     C */
/*              WRITE (*,*) ' ' */
/*              WRITE (*,*) 'Computation method: ', METHOD(I) */
/*              WRITE (*,*) ' ' */
/*              WRITE (*,*) '  Radius                   (km)  = ', RADIUS */
/*              WRITE (*,*) '  Planetocentric Latitude  (deg) = ', LAT */
/*              WRITE (*,*) '  Planetocentric Longitude (deg) = ', LON */
/*              WRITE (*,*) ' ' */

/*           END DO */

/*           END */

/* $ Restrictions */

/*     The appropriate kernel data must have been loaded before this */
/*     routine is called.  See the Files section above. */

/* $ Literature_References */

/*     None. */

/* $ Author_and_Institution */

/*     N.J. Bachman   (JPL) */
/*     J.E. McLean    (JPL) */

/* $ Version */

/* -    TESTUTIL Version 1.0.0, 18-OCT-2005 (NJB) */

/* -& */
/* $ Index_Entries */

/*     sub-solar point */

/* -& */

/*     SPICELIB functions */


/*     Local parameters */


/*     Local variables */


/*     Saved variables */


/*     Initial values */


/*     Standard SPICE error handling. */

    if (return_()) {
	return 0;
    } else {
	chkin_("T_SUBSOL", (ftnlen)8);
    }

/*     Obtain integer codes for the target and observer. */

    bods2c_(target, &trgcde, &found, target_len);
    if (! found) {
	setmsg_("The target, '#', is not a recognized name for an ephemeris "
		"object. The cause of this problem may be that you need an up"
		"dated version of the SPICE Toolkit. ", (ftnlen)155);
	errch_("#", target, (ftnlen)1, target_len);
	sigerr_("SPICE(IDCODENOTFOUND)", (ftnlen)21);
	chkout_("T_SUBSOL", (ftnlen)8);
	return 0;
    }
    bods2c_(obsrvr, &obscde, &found, obsrvr_len);
    if (! found) {
	setmsg_("The observer, '#', is not a recognized name for an ephemeri"
		"s object. The cause of this problem may be that you need an "
		"updated version of the SPICE Toolkit. ", (ftnlen)157);
	errch_("#", obsrvr, (ftnlen)1, obsrvr_len);
	sigerr_("SPICE(IDCODENOTFOUND)", (ftnlen)21);
	chkout_("T_SUBSOL", (ftnlen)8);
	return 0;
    }

/*     Get the radii of the target body from the kernel pool. */

    bodvar_(&trgcde, "RADII", &nradii, radii, (ftnlen)5);

/*     Find the name of the body-fixed frame associated with the */
/*     target body.  We'll want the state of the target relative to */
/*     the observer in this body-fixed frame. */

    cidfrm_(&trgcde, &frcode, frname, &found, (ftnlen)80);
    if (! found) {
	setmsg_("No body-fixed frame is associated with target body #; a fra"
		"me kernel must be loaded to make this association.  Consult "
		"the FRAMES Required Reading for details.", (ftnlen)159);
	errch_("#", target, (ftnlen)1, target_len);
	sigerr_("SPICE(NOFRAME)", (ftnlen)14);
	chkout_("T_SUBSOL", (ftnlen)8);
	return 0;
    }

/*     If we're using aberration corrections, we'll need the */
/*     one way light time from the target to the observer.  Otherwise, */
/*     we set the time time to zero. */

/*     The correction is not needed when the target and observer */
/*     coincide. */

    if (eqstr_(abcorr, "NONE", abcorr_len, (ftnlen)4)) {
	lt = 0.;
	ettarg = *et;
    } else {
	if (obscde != trgcde) {
	    ltime_(et, &obscde, "<-", &trgcde, &ettarg, &lt, (ftnlen)2);
	} else {
	    lt = 0.;
	    ettarg = *et;
	}
    }

/*     Determine the position of the sun in target body-fixed */
/*     coordinates. */

/*     Call SPKEZ to compute the position of the sun as seen from the */
/*     target body and the light time between them SUNLT.  This state is */
/*     evaluated at the target epoch ETTARG. We request that the */
/*     coordinates of the target-sun position vector POS be returned */
/*     relative to the body fixed reference frame associated with the */
/*     target body, using aberration corrections specified by the input */
/*     argument ABCORR. */

    spkpos_("SUN", &ettarg, frname, abcorr, target, pos, &sunlt, (ftnlen)3, (
	    ftnlen)80, abcorr_len, target_len);

/*     Find the sub-solar point using the specified geometric definition. */

    if (eqstr_(method, "Near point", method_len, (ftnlen)10)) {

/*        Locate the nearest point to the sun on the target. */

	nearpt_(pos, radii, &radii[1], &radii[2], spoint, &alt);
    } else if (eqstr_(method, "Intercept", method_len, (ftnlen)9)) {
	surfpt_(origin, pos, radii, &radii[1], &radii[2], spoint, &found);

/*        Since the line in question passes through the center of the */
/*        target, there will always be a surface intercept.  So we should */
/*        never have FOUND = .FALSE. */

	if (! found) {
	    setmsg_("Call to SURFPT returned FOUND=FALSE even though vertex "
		    "of ray is at target center. This indicates a bug. Please"
		    " contact NAIF.", (ftnlen)125);
	    sigerr_("SPICE(BUG)", (ftnlen)10);
	    chkout_("T_SUBSOL", (ftnlen)8);
	    return 0;
	}
    } else {
	setmsg_("The computation method # was not recognized. Allowed values"
		" are \"Near point\" and \"Intercept.\"", (ftnlen)93);
	errch_("#", method, (ftnlen)1, method_len);
	sigerr_("SPICE(DUBIOUSMETHOD)", (ftnlen)20);
	chkout_("T_SUBSOL", (ftnlen)8);
	return 0;
    }
    chkout_("T_SUBSOL", (ftnlen)8);
    return 0;
} /* t_subsol__ */
