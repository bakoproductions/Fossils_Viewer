package com.bakoproductions.fossilsviewer.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class BoundingSphere implements Parcelable{
	private float[] center;
	private float diameter;
	
	double dTheta = 25 * (Math.PI/180);
    double dPhi = dTheta;
	
	public BoundingSphere(float[] center, float diameter){
		this.center = center;
		this.diameter = diameter;
	}
	
	public float[] getCenter() {
		return center;
	}
	
	public float getDiameter() {
		return diameter;
	}
	
	public static final Parcelable.Creator<BoundingSphere> CREATOR = new Parcelable.Creator<BoundingSphere>() {
        public BoundingSphere createFromParcel(Parcel pc) {
            return new BoundingSphere(pc);
        }
        public BoundingSphere[] newArray(int size) {
            return new BoundingSphere[size];
        }
	};

	public BoundingSphere(Parcel parcel) {
		center = parcel.createFloatArray();
		diameter = parcel.readFloat();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloatArray(center);
		dest.writeFloat(diameter);
	}
	
	public void printSphereData() {
		Log.i(BoundingSphere.class.getSimpleName(), "Center " + center[0] + ", " + center[1] + ", " + center[2] + " Diameter " + diameter);
	}
}
