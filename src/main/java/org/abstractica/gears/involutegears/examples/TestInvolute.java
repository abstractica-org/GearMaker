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
		GearData data1 = new GearData(12, 20, 24, 20, 20);
		GearData data2 = new GearData(36, 20, 24, 20, -20);
		JavaCSG csg = JavaCSGFactory.createDefault();
		InvoluteGears gears = new InvoluteGears(csg);
		Geometry3D axleHole = csg.cylinder3D(3.4, 5+2, 32, false);
		axleHole = csg.translate3DZ(-1).transform(axleHole);
		Geometry3D plate = csg.box3D(50, 10, 3, false);
		Geometry3D h1 = csg.translate3DX(-20).transform(axleHole);
		Geometry3D h2 = csg.translate3DX(20).transform(axleHole);
		plate = csg.difference3D(plate, h1, h2);
		Geometry3D p1 = csg.translate3DY(-25).transform(plate);
		Geometry3D p2 = csg.translate3DY(25).transform(plate);
		Geometry3D gear1 = gears.involuteGear3D(data1, 5, 0.1, false);
		gear1 = csg.difference3D(gear1, axleHole);
		Geometry3D gear2 = gears.involuteGear3D(data2, 5, 0.1, false);
		gear2 = csg.difference3D(gear2, axleHole);
		gear2 = csg.rotate3DZ(csg.degrees(360.0/(2*36.0))).transform(gear2);
		gear2 = csg.translate3DX(50).transform(gear2);
		Geometry3D res = csg.union3D(gear1, gear2, p1, p2);
		csg.view(res);
	}
}
