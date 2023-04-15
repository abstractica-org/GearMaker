package org.abstractica.gears.involutegears;

import org.abstractica.javacsg.*;

import java.util.ArrayList;
import java.util.List;

public class InvoluteGears
{
	private final JavaCSG csg;

	public InvoluteGears(JavaCSG csg)
	{
		this.csg = csg;
	}


	public Geometry2D unitRack4(double unit, int length)
	{
		int teethPrUnit = 4;
		int totalTeeth = 24;
		double module = (2.0 * unit) / totalTeeth;
		double pitch = unit * Math.PI * 2.0 / totalTeeth;
		double preassureAngle = Math.toRadians(20.0);
		double height = 2 * module;
		double slopeWidth = Math.tan(preassureAngle) * height;
		double bottomWidth = (pitch - 2 * slopeWidth) * 0.5;
		double totalLength = (bottomWidth + slopeWidth) * 2 * teethPrUnit; //4 teeth per unit
		double extraLength = totalLength - unit;
		double topWidth = bottomWidth - (extraLength / teethPrUnit);
		double x = unit * length;
		double yMin = unit - module;
		double yMax = unit + module;
		List<Vector2D> vertices = new ArrayList<>();
		vertices.add(csg.vector2D(0, 0));
		vertices.add(csg.vector2D(unit * length, 0));
		vertices.add(csg.vector2D(x, yMin));
		x -= 0.5 * bottomWidth;
		for (int i = 0; i < length * teethPrUnit; i++)
		{
			vertices.add(csg.vector2D(x, yMin));
			x -= slopeWidth;
			vertices.add(csg.vector2D(x, yMax));
			x -= topWidth;
			vertices.add(csg.vector2D(x, yMax));
			x -= slopeWidth;
			vertices.add(csg.vector2D(x, yMin));
			x -= bottomWidth;
		}
		x += 0.5 * bottomWidth;
		vertices.add(csg.vector2D(x, yMin));
		vertices.add(csg.vector2D(0, yMin));
		Geometry2D res = csg.polygon2D(vertices);
		return res;
	}


	public Geometry2D rack2D(GearData gearData, double pitchHeight, double fitting)
	{
		double height = 2 * gearData.module;
		double slopeWidth = Math.tan(gearData.pressureAngle) * height;
		double topBottomWidth = (gearData.pitch - 2 * slopeWidth) * 0.5;
		List<Vector2D> points = new ArrayList<>();
		double x = -(slopeWidth + 0.5 * topBottomWidth);
		double y = -pitchHeight;
		points.add(csg.vector2D(x, y));
		y += pitchHeight - gearData.module;
		points.add(csg.vector2D(x, y));
		x += slopeWidth;
		y += 2.0 * gearData.module;
		points.add(csg.vector2D(x, y));
		x += topBottomWidth;
		points.add(csg.vector2D(x, y));
		x += slopeWidth;
		y -= 2.0 * gearData.module;
		points.add(csg.vector2D(x, y));
		for (int i = 1; i < gearData.numberOfTeeth; ++i)
		{
			x += topBottomWidth;
			points.add(csg.vector2D(x, y));
			x += slopeWidth;
			y += 2.0 * gearData.module;
			points.add(csg.vector2D(x, y));
			x += topBottomWidth;
			points.add(csg.vector2D(x, y));
			x += slopeWidth;
			y -= 2.0 * gearData.module;
			points.add(csg.vector2D(x, y));
		}
		y -= pitchHeight - gearData.module;
		points.add(csg.vector2D(x, y));
		return csg.polygon2D(points);
	}

	public Geometry3D involuteGear3D(GearData gearData, double height, double fitting, boolean centerZ)
	{
		Geometry2D gear2D = involuteGear2D(gearData, fitting);
		if(gearData.helixAngle == 0)
		{
			return csg.linearExtrude(height, centerZ, gear2D);
		}
		else
		{
			double dist = height * Math.cos((0.5*Math.PI)-gearData.helixAngle);
			double twist = dist / (gearData.pitchRadius * 2*Math.PI);
			int slices = (int) Math.ceil(height / 0.2);
			return csg.linearExtrude(height, twist*360, 1.0, slices, centerZ, gear2D);
		}
	}


	public Geometry2D involuteGear2D(GearData gearData, double fitting)
	{
		InvoluteTooth tooth = involuteTooth2D(gearData, fitting);
		List<Geometry2D> union = new ArrayList<>();
		List<Vector2D> center = new ArrayList<>();
		for (int i = 0; i < gearData.numberOfTeeth; ++i)
		{
			Vector2D nextRight = rotate(tooth.connectionPointRight, i * gearData.pitchAngle);
			Vector2D nextLeft = rotate(tooth.connectionPointLeft, i * gearData.pitchAngle);
			center.add(nextRight);
			center.add(nextLeft);
			Transform2D rot = csg.rotate2D(csg.radians(i * gearData.pitchAngle));
			union.add(rot.transform(tooth.toothGeometry));
		}
		union.add(csg.polygon2D(center));
		Geometry2D res = csg.union2D(union);
		return res;
	}


	public InvoluteTooth involuteTooth2D(GearData gearData, double fitting)
	{
		//Cutout
		List<Vector2D> cutoutPoints = cutout(gearData, fitting);
		//Find closest to origo
		double minSqrDist = Double.MAX_VALUE;
		Vector2D minPoint = null;
		for (Vector2D point : cutoutPoints)
		{
			double x = point.x();
			double y = point.y();
			double sqrDist = x * x + y * y;
			if (sqrDist < minSqrDist)
			{
				minSqrDist = sqrDist;
				minPoint = point;
			}
		}
		double minDist = Math.sqrt(minSqrDist);
		Vector2D polarMinPoint = cartesianToPolar(minPoint);
		//Involute
		List<Vector2D> polarPoints = new ArrayList<>();
		int involuteSteps = 32;
		double maxRollOffAngle = Math.acos(gearData.baseRadius / (gearData.tipRadius - fitting));//-2*fitting
		double stepSize = maxRollOffAngle / involuteSteps;
		Vector2D last = null;
		for (int i = 0; i <= involuteSteps; ++i)
		{
			if (last == null)
			{
				last = csg.vector2D(0, 0);
			}
			Vector2D ci = circleInvolute(last, gearData.baseRadius, i * stepSize, fitting);
			Vector2D polar = csg.vector2D(ci.x(), ci.y() - gearData.baseToothAngle * 0.5); //+fittingAngle
			if (polar.x() < minDist)
			{
				minDist = polar.x();
				polarMinPoint = polar;
			}
			polarPoints.add(polar);
			last = polarToCartesian(polar);
		}

		List<Vector2D> halfToothPoints = new ArrayList<>();
		halfToothPoints.add(csg.vector2D(0, 0));
		Vector2D polarRight = polarMinPoint;
		Vector2D polarLeft = csg.vector2D(polarRight.x(), -polarRight.y());
		halfToothPoints.add(polarToCartesian(polarMinPoint));
		for (Vector2D polar : polarPoints)
		{
			halfToothPoints.add(polarToCartesian(polar.x(), polar.y()));
		}
		halfToothPoints.add(csg.vector2D(gearData.tipRadius - 2 * fitting, 0));
		Geometry2D halfTooth = csg.polygon2D(halfToothPoints);
		Geometry2D diff = csg.difference2D(halfTooth, csg.polygon2D(cutoutPoints));
		Transform2D mirror = csg.mirror2D(0, 1);
		Geometry2D tooth = csg.union2D(diff, mirror.transform(diff));
		return new InvoluteTooth(tooth, polarToCartesian(polarRight), polarToCartesian(polarLeft));
	}


	public List<Vector2D> cutout(GearData gearData, double fitting)
	{
		//cutout polygon
		List<Vector2D> points = new ArrayList<>();
		int steps = 64;
		//Vector2D lastPoint = cornerPath(0.0, gearData, fitting);
		for (int i = 1; i <= steps; ++i)
		{
			double t = i * (3.0 / steps);
			double rotAngle = -t * gearData.pitchAngle;
			Vector2D point = cornerPath(t, gearData, fitting);
			Vector2D rotatedPoint = rotate(point.x(), point.y(), rotAngle);
			points.add(rotatedPoint);
		}
		return points;
	}


	private Vector2D cornerPath(double t, GearData gearData, double fitting)
	{
		double slopeWidth = Math.tan(gearData.pressureAngle) * 2 * gearData.module;
		double topBottomWidth = (gearData.pitch - 2 * slopeWidth) * 0.5;
		double x = gearData.pitchRadius - gearData.module - 2 * fitting;
		double y = t * gearData.pitch - (slopeWidth + 0.5 * topBottomWidth) + fitting;
		return csg.vector2D(x, y);

	}

	//    Circle involute function:
	//    Returns the polar coordinates of an involute of a circle
	//    r = radius of the base circle
	//    rho = roll-off angle in radians
	private Vector2D circleInvolute(Vector2D last, double r, double rho, double fitting)
	{
		Vector2D pointPolar = csg.vector2D(r / Math.cos(rho), Math.tan(rho) - rho);
		Vector2D pointCartesian = polarToCartesian(pointPolar);
		Vector2D dir = csg.normalize(csg.fromTo(last, pointCartesian));
		Vector2D normal = perpendicularCCW(dir);
		Vector2D adjust = csg.mul(normal, fitting);
		return cartesianToPolar(csg.add(pointCartesian, adjust));
	}


	//Conversions

	private Vector2D polarToCartesian(Vector2D polar)
	{
		return polarToCartesian(polar.x(), polar.y());
	}

	private Vector2D polarToCartesian(double r, double angle)
	{
		return csg.vector2D(r * Math.cos(angle), r * Math.sin(angle));
	}

	private Vector2D cartesianToPolar(Vector2D cartesian)
	{
		return cartesianToPolar(cartesian.x(), cartesian.y());
	}

	private Vector2D cartesianToPolar(double x, double y)
	{
		double r = Math.sqrt(x * x + y * y);
		double angle = Math.atan(y / x);
		return csg.vector2D(r, angle);
	}

	private Vector2D rotate(Vector2D point, double angle)
	{
		return rotate(point.x(), point.y(), angle);
	}

	private Vector2D rotate(double x, double y, double angle)
	{
		double ca = Math.cos(angle);
		double sa = Math.sin(angle);
		double resX = ca * x - sa * y;
		double resY = sa * x + ca * y;
		return csg.vector2D(resX, resY);
	}

	private Vector2D normalized(Vector2D v)
	{
		double f = 1.0 / csg.length(v);
		return csg.vector2D(f * v.x(), f * v.y());
	}

	public Vector2D perpendicularCW(Vector2D v)
	{
		return csg.vector2D(v.y(), -v.x());
	}

	public Vector2D perpendicularCCW(Vector2D v)
	{
		return csg.vector2D(-v.y(), v.x());
	}
}
