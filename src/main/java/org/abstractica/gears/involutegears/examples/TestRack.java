package org.abstractica.gears.involutegears.examples;

import org.abstractica.gears.involutegears.GearData;
import org.abstractica.gears.involutegears.InvoluteGears;
import org.abstractica.javacsg.JavaCSG;
import org.abstractica.javacsg.JavaCSGFactory;


public class TestRack
{
	public static void main(String[] args)
	{
		GearData data = new GearData(20, 40, 40, 20, 0);
		JavaCSG csg = JavaCSGFactory.createDefault();
		InvoluteGears gears = new InvoluteGears(csg);
		csg.view(gears.rack2D(data, 10.0, 0.0));
	}
}
