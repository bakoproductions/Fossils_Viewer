package com.bakoproductions.fossilsviewer.annotations;

import com.bakoproductions.fossilsviewer.objects.BoundingSphere;
import com.bakoproductions.fossilsviewer.util.MathHelper;

import android.opengl.Matrix;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Annotation implements Parcelable {
	private int id;
	
	private float x;
	private float y;
	private float z;
	
	private float nx;
	private float ny;
	private float nz;
	
	private String title;
	private String text;
	
	private BoundingSphere sphere;
	
	public Annotation() {
	}
	
	public float[] getIntersection() {
		float[] inter = new float[3];
		
		inter[0] = x;
		inter[1] = y;
		inter[2] = z;
		
		return inter;
	}
	
	public float[] getNormal() {
		float[] normal = new float[3];
		
		normal[0] = nx;
		normal[1] = ny;
		normal[2] = nz;
		
		return normal;
	}
	
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		this.z = z;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public float getNx() {
		return nx;
	}

	public void setNx(float nx) {
		this.nx = nx;
	}

	public float getNy() {
		return ny;
	}

	public void setNy(float ny) {
		this.ny = ny;
	}

	public float getNz() {
		return nz;
	}

	public void setNz(float nz) {
		this.nz = nz;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void calculateSphere(float[] modelMatrix, float scaling, BoundingSphere pushPinSphere) {
		float[] pCenter = pushPinSphere.getCenter();
		float[] center = new float[4];
		Matrix.multiplyMV(center, 0, modelMatrix, 0, new float[] {pCenter[0], pCenter[1], pCenter[2], 1}, 0);
		float diameter = pushPinSphere.getDiameter() * scaling;
		
		if(sphere == null)
			sphere = new BoundingSphere(new float[] {center[0], center[1], center[2]}, diameter);
		else {
			sphere.setCenter(new float[] {center[0], center[1], center[2]});
			sphere.setDiameter(diameter);
		}
	}
	
	public float[] getIntersectionWithPin(float[] P1, float[] P2) {
		float[] vpc = MathHelper.getLineVector(P1, sphere.getCenter());				// vpc = c - P1
		Log.i("Bako", "VPC: " + vpc[0] + ", " + vpc[1] + ", " + vpc[2]);
		float[] d = MathHelper.getLineVector(P1, P2);
		float radious = sphere.getDiameter() / 2;
		float[] c = sphere.getCenter();
		float vpcLen = MathHelper.length(vpc);
		
		if(MathHelper.dot(vpc, d) < 0) {											// P1 is further from c
			if(vpcLen > radious)													// P1 is outside the sphere
				return null;
			else if(vpcLen == radious)												// P1 is on the edge of the sphere
				return P1;
			else {
				float[] pc = MathHelper.findProjection(P1, P2, c);					// pc: projection of c on the line
				float dist = (float) Math.sqrt((radious * radious) - (MathHelper.distance(pc, c) * MathHelper.distance(pc, c)));
				float di1 = dist - MathHelper.distance(pc, P1);
				return MathHelper.pointOnLine(P1, d, di1);
			}
		} else {																	// P1 is behind c
			float[] pc = MathHelper.findProjection(P1, P2, c);						// pc: projection of c on the line
			if(MathHelper.distance(c, pc) > radious)								// pc is outside the sphere
				return null;
			else {																	// pc is inside the sphere
				float dist = (float) Math.sqrt((radious * radious) - (MathHelper.distance(pc, c) * MathHelper.distance(pc, c)));
				float di1;
				if(vpcLen > radious)
					di1 = Math.abs(MathHelper.distance(pc, P1)) - dist;
				else
					di1 = Math.abs(MathHelper.distance(pc, P1)) + dist;
				
				return MathHelper.pointOnLine(P1, d, di1);
			}
		}
	}
	
	public static final Parcelable.Creator<Annotation> CREATOR = new Parcelable.Creator<Annotation>() {
        public Annotation createFromParcel(Parcel pc) {
            return new Annotation(pc);
        }
        public Annotation[] newArray(int size) {
            return new Annotation[size];
        }
	};
	
	public Annotation(Parcel parcel) {
		this.id = parcel.readInt();
		
		this.x = parcel.readFloat();
		this.y = parcel.readFloat();
		this.z = parcel.readFloat();
		
		this.nx = parcel.readFloat();
		this.ny = parcel.readFloat();
		this.nz = parcel.readFloat();
		
		this.title = parcel.readString();
		this.text = parcel.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		
		dest.writeFloat(x);
		dest.writeFloat(y);
		dest.writeFloat(z);
		
		dest.writeFloat(nx);
		dest.writeFloat(ny);
		dest.writeFloat(nz);
		
		dest.writeString(title);
		dest.writeString(text);
	}
}
