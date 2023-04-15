package org.abstractica.gears.involutegears.examples;

import org.abstractica.gears.involutegears.GearData;
import org.abstractica.gears.involutegears.InvoluteGears;
import org.abstractica.javacsg.Geometry3D;
import org.abstractica.javacsg.JavaCSG;
import org.abstractica.javacsg.JavaCSGFactory;


public class TestInvolute
{
	public static void main(String[] args)
	{
		GearData data1 = new GearData(32, 40, 80, 20, -20);
		GearData data2 = new GearData(48, 40, 80, 20, 20);
		JavaCSG csg = JavaCSGFactory.createDefault();
		InvoluteGears gears = new InvoluteGears(csg);
		Geometry3D gear1 = gears.involuteGear3D(data1, 5, 0, false);
		Geometry3D gear2 = gears.involuteGear3D(data2, 5, 0, false);
		gear2 = csg.rotate3DZ(csg.degrees(360.0/96.0)).transform(gear2);
		gear2 = csg.translate3DX(40).transform(gear2);
		Geometry3D res = csg.union3D(gear1, gear2);
		csg.view(res);
	}
}
