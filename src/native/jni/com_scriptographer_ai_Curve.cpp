/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * $Id$
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "aiGlobals.h"
#include "com_scriptographer_ai_Curve.h"

/*
 * com.scriptographer.ai.Bezier
 */

/*
 * double nativeGetLength(double p1x, double p1y, double h1x, double h1y, double h2x, double h2y, double p2x, double p2y)
 */
JNIEXPORT jdouble JNICALL Java_com_scriptographer_ai_Curve_nativeGetLength(JNIEnv *env, jclass cls, jdouble p1x, jdouble p1y, jdouble h1x, jdouble h1y, jdouble h2x, jdouble h2y, jdouble p2x, jdouble p2y) {
	try {
		DEFINE_BEZIER(bezier, p1x, p1y, h1x, h1y, h2x, h2y, p2x, p2y);
		// Use a default value for flatness since AI doc says it ignores it anyway in CS4
		return sAIRealBezier->Length(&bezier, 0.1f);
	} EXCEPTION_CONVERT(env);
	return 0.0;
}

/*
 * void nativeAdjustThroughPoint(float[] values, float x, float y, float parameter)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ai_Curve_nativeAdjustThroughPoint(JNIEnv *env, jclass cls, jfloatArray values, jfloat x, jfloat y, jfloat parameter) {
	try {
		AIPathSegment *segments = (AIPathSegment *) env->GetFloatArrayElements(values, NULL);
		DEFINE_POINT(pt, x, y);
		AIRealBezier bezier;
		bezier.p0 = segments[0].p;
		bezier.p1 = segments[0].out;
		bezier.p2 = segments[1].in;
		bezier.p3 = segments[1].p;
		sAIRealBezier->AdjustThroughPoint(&bezier, &pt, parameter);
		segments[0].p = bezier.p0;
		segments[0].out = bezier.p1;
		segments[1].in = bezier.p2;
		segments[1].p = bezier.p3;
		env->ReleaseFloatArrayElements(values, (jfloat *) segments, 0);
	} EXCEPTION_CONVERT(env);
}
