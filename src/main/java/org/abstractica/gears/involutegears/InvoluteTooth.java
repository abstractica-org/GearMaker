package org.abstractica.gears.involutegears;

import org.abstractica.javacsg.Geometry2D;
import org.abstractica.javacsg.Vector2D;

public class InvoluteTooth
{
	public final Geometry2D toothGeometry;
	public final Vector2D connectionPointRight;
	public final Vector2D connectionPointLeft;

	public InvoluteTooth(Geometry2D toothGeometry, Vector2D connectionPointRight, Vector2D connectionPointLeft)
	{
		this.toothGeometry = toothGeometry;
		this.connectionPointRight = connectionPointRight;
		this.connectionPointLeft = connectionPointLeft;
	}
}
