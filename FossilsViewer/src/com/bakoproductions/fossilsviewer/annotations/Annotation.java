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
	
	public BoundingSphere getSphere() {
		return sphere;
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
		float[] CE = sphere.getCenter();
		float r = sphere.getDiameter() / 2.0f;
		
		float xB_A = P2[0] - P1[0]; 								// Xb - Xa
		float yB_A = P2[1] - P1[1];									// Yb - Ya
		float zB_A = P2[2] - P1[2];									// Zb - Za
		
		float xA_C = P1[0] - CE[0];									// Xa - Xc
		float yA_C = P1[1] - CE[1];									// Ya - Yc	
		float zA_C = P1[2] - CE[2];									// Za - Zc
	
		float a = (xB_A * xB_A) + (yB_A * yB_A) + (zB_A * zB_A);
		float b = 2 * ((xB_A * xA_C) + (yB_A * yA_C) + (zB_A * zA_C));
		float c = (xA_C * xA_C) + (yA_C * yA_C) + (zA_C * zA_C) - (r * r);
		float delta = (b*b) - (4*a*c);
		
		if(delta < 0)
			return null;
		else if(delta == 0) {
			float d = - (b / (2*a));
			
			float[] ret = new float[3];
			ret[0] = P1[0] + (d * (P2[0] - P1[0]));
			ret[1] = P1[1] + (d * (P2[1] - P1[1]));
			ret[2] = P1[2] + (d * (P2[2] - P1[2]));
			return ret;
		} else {
			float d1 = -b + ((float)Math.sqrt(delta)/(2*a));
			float d2 = -b - ((float)Math.sqrt(delta)/(2*a));
			
			float[] ret1 = new float[3];
			float[] ret2 = new float[3];
			
			ret1[0] = P1[0] + (d1 * (P2[0] - P1[0]));
			ret1[1] = P1[1] + (d1 * (P2[1] - P1[1]));
			ret1[2] = P1[2] + (d1 * (P2[2] - P1[2]));
			
			ret2[0] = P1[0] + (d2 * (P2[0] - P1[0]));
			ret2[1] = P1[1] + (d2 * (P2[1] - P1[1]));
			ret2[2] = P1[2] + (d2 * (P2[2] - P1[2]));
			
			float min = MathHelper.distance(P1, ret1);
			if(MathHelper.distance(P1, ret2) < min)
				return ret2;
			else
				return ret1;		
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
