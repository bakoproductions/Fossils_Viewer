package com.bakoproductions.fossilsviewer.viewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.SlidingDrawer;

import com.bakoproductions.fossilsviewer.annotations.Annotation;
import com.bakoproductions.fossilsviewer.annotations.Annotations;
import com.bakoproductions.fossilsviewer.annotations.DialogResult;
import com.bakoproductions.fossilsviewer.objects.BoundingSphere;
import com.bakoproductions.fossilsviewer.objects.Light;
import com.bakoproductions.fossilsviewer.objects.Line;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.objects.ModelPart;
import com.bakoproductions.fossilsviewer.util.MathHelper;
import com.bakoproductions.fossilsviewer.viewer.ViewerGLSurfaceView.DialogHandler;

public class ViewerRenderer implements Renderer, DialogResult {
	private static final String TAG = ViewerRenderer.class.getSimpleName();
	
	private Context context;
	private DialogHandler dialogHanlder;
	
	private String filePath;
	private Model model;
	private Model pushPin;
	private Light light;
	private Line line;
	private Annotations annotations;
    
    private boolean lockedTranslation;
    
    /* ====== Annotation popup ====== */
    private int displayedAnnotation = -1;
    
    private static final int CLOSED_STAGE = 0;
    private static final int OPENED_STAGE = 1;
    private int displayedStage = CLOSED_STAGE;
    /* ============================== */
    
    /* ====== Touch events parameters ====== */
    private float scaleFactor = 1.0f;
	   
    private float posX = 0.0f;
    private float posY = 0.0f;
 
    private float rotX = 0.0f;
    private float rotY = 0.0f;
    /* =============================-======== */
	
	/* = Perspective projection parameters = */
	private float zNear = 1.0f;
	private float zFar;
	/* ===================================== */
	
	/* ======== User Interaction =========== */
	private boolean userRequestedAnnotation;
	private boolean userRequestedPopup;
	
	private float clickX;
	private float clickY;
	
	private float[] intersection;
	/* ===================================== */
	
	private float pushPinSizeFactor = 0.05f; 
	
	/* ======== Matrices of the Mesh =========== */
	private float[] modelMatrix = new float[16];
	private float[] viewMatrix = new float[16];
	private float[] projectionMatrix = new float[16];
	private float[] MVMatrix = new float[16];
	private float[] MVPMatrix = new float[16];
	private int[] viewport;
	/* ======================================== */
	
	public ViewerRenderer(Context context, String filePath, Model model, Model pushPin) {
		this.context = context;
		this.setModel(model);
		this.pushPin = pushPin;
		
		lockedTranslation = false;
		userRequestedAnnotation = false;
		
		String[] file = filePath.split("/");
		this.filePath = file[file.length - 1];
		annotations = new Annotations(this.filePath);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(.5f, .5f, .5f, 0.5f);
		
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glFrontFace(GLES20.GL_CW);
	    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	    
	    Matrix.setLookAtM(viewMatrix, 0, -posX, -posY, model.getSphere().getDiameter(), -posX, -posY, 0, 0.0f, 1.0f, 0.0f);
	    light = new Light(context);
	    
		model.prepareTextures();
		pushPin.prepareTextures();
		model.bindBuffers();
		pushPin.bindBuffers();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}
		
		GLES20.glViewport(0, 0, width, height);
		viewport = new int[] {0, 0, width, height};

		BoundingSphere sphere = getModel().getSphere();
		zFar = (zNear + sphere.getDiameter())*5.0f;
		
		final float ratio = (float) width / height;	
		Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, zNear, zFar);
	}

	@Override
	public void onDrawFrame(GL10 gl) {		
		GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
				
		// =================== 3D scene ============================ //
		float[] center = model.getSphere().getCenter();
        float diameter = model.getSphere().getDiameter();
        
        Matrix.setLookAtM(viewMatrix, 0, -posX, -posY, diameter, -posX, -posY, 0.0f, 0.0f, 1.0f, 0.0f);
        light.position(new float[] {0.0f, 0.0f, diameter});      
        
        ArrayList<Annotation> annotationList = annotations.getAnnotations();
        for(int i=0;i<annotationList.size();i++) {
        	Annotation annotation = annotationList.get(i);
        	
        	float[] intersection = annotation.getIntersection();
        	float[] positioningMatrix = new float[16];
        	Matrix.setIdentityM(positioningMatrix, 0);
        	pushPin.position(positioningMatrix, new float[]{-center[0], -center[1], -center[2]}, new float[]{rotX, rotY}, scaleFactor);
        	
        	float[] alignementMatrix = new float[16];
        	Matrix.setIdentityM(alignementMatrix, 0);
        	pushPin.alignToNormalManual(alignementMatrix, intersection, annotation.getNormal());
        	float scaling = getPushPinSize(pushPin.getSphere().getDiameter(), model.getSphere().getDiameter());
        	pushPin.scale(alignementMatrix, scaling);
        	annotation.calculateSphere(alignementMatrix, scaling, pushPin.getSphere());
        	
        	Matrix.setIdentityM(modelMatrix, 0);
        	Matrix.multiplyMM(modelMatrix, 0, positioningMatrix, 0, alignementMatrix, 0);
            pushPin.draw(modelMatrix, viewMatrix, projectionMatrix, MVMatrix, MVPMatrix);
            light.draw(pushPin.getLightPosUniform(), viewMatrix, projectionMatrix);
        }
        
        light.position(new float[] {0.0f, 0.0f, diameter});
        Matrix.setIdentityM(modelMatrix, 0);
        model.position(modelMatrix, new float[] {-center[0], -center[1], -center[2]}, new float[] {rotX, rotY}, scaleFactor);
        model.draw(modelMatrix, viewMatrix, projectionMatrix, MVMatrix, MVPMatrix);
        light.draw(model.getLightPosUniform(), viewMatrix, projectionMatrix);
        
        if(userRequestedAnnotation) {
        	float[] P1 = new float[3];
        	float[] P2 = new float[3];
        	createRay(P1, P2); 
        	
        	intersection = findIntersection(P1, P2); 
        	
        	if(intersection != null) {
        		Message message = new Message();
        		message.what = DialogHandler.ADD_ANNOTATION;
        		
        		float[] inter = new float[3];
        		inter[0] = intersection[0];
        		inter[1] = intersection[1];
        		inter[2] = intersection[2];
        		
        		float[] nor = new float[3];
        		nor[0] = intersection[3];
        		nor[1] = intersection[4];
        		nor[2] = intersection[5];
        		
        		Bundle data = new Bundle();
        		data.putFloatArray("intersection", inter);
        		data.putFloatArray("normal", nor);
        		message.setData(data);
        		dialogHanlder.sendMessage(message);
        	} else {
        		Message message = new Message();
        		message.what = DialogHandler.NO_HIT;
        		dialogHanlder.sendMessage(message);
        	}
	        userRequestedAnnotation = false;
        }
        
        if(userRequestedPopup) {
        	float[] P1 = new float[3];
        	float[] P2 = new float[3];
        	createRay(P1, P2);        	
            
            int hitted = 0;
            float min = 0;
        	for(int i=0;i<annotationList.size();i++) {
	        	Annotation annotation = annotationList.get(i);
	        	float[] inter = annotation.getIntersectionWithPin(P1, P2);
	   
	        	if(inter != null) {
	        		if(hitted == 0) {
	        			min = MathHelper.distance(P1, annotation.getIntersection());
	        			displayedAnnotation = i;
	        		} else {
	        			float dist = MathHelper.distance(P1, annotation.getIntersection());
	        			if(dist < min) {
	        				min = dist;
	        				displayedAnnotation = i;		
	        			}
	        		}
	        		hitted++;
	        	}
        	}        	
        	userRequestedPopup = false;
        }
        
        if(displayedAnnotation != -1) {
        	if(displayedStage == CLOSED_STAGE) {
        		Annotation annotation = annotationList.get(displayedAnnotation);
            	int[] point = get2dPoint(annotation.getIntersection(), viewport[2], viewport[3]);
            	
        		Message message = new Message();
        		message.what = DialogHandler.OPEN_ANNOTATION;
        	
        		Bundle bundle = new Bundle();
        		bundle.putParcelable("annotation", annotation);
        		bundle.putIntArray("location", point);
        		message.setData(bundle);
        		dialogHanlder.sendMessage(message);
        		
        		displayedStage = OPENED_STAGE;
        	} else if(displayedStage == OPENED_STAGE){
        		Annotation annotation = null;
        		try {
        			annotation = annotationList.get(displayedAnnotation);
        		} catch (IndexOutOfBoundsException e) {
        			displayedAnnotation = -1;
        			return;
        		}
        		
            	int[] point = get2dPoint(annotation.getIntersection(), viewport[2], viewport[3]);
            	
            	Message message = new Message();
        		message.what = DialogHandler.MOVE_ANNOTATION;
        		
        		Bundle bundle = new Bundle();
        		bundle.putString("title", annotation.getTitle());
        		bundle.putString("description", annotation.getText());
        		bundle.putIntArray("location", point);
        		message.setData(bundle);
        		dialogHanlder.sendMessage(message);
        	}
        }
	}
	
	public boolean onBackPressed() {
		if(displayedStage == OPENED_STAGE) {
			if(dialogHanlder != null) {
				Message message = new Message();
        		message.what = DialogHandler.CLOSE_ANNOTATION_FROM_RENDERER;
        		dialogHanlder.sendMessage(message);
				return true;
			} else
				return false;			
		} else
			return false;
	}
	
	public void setHandler(DialogHandler dialogHandler) {
		this.dialogHanlder = dialogHandler;
	}
	
	public Model getPushPin() {
		return pushPin;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setLockedTranslation(boolean lockedTranslation) {
		this.lockedTranslation = lockedTranslation;
	}
	
	public boolean isLockedTranslation() {
		return lockedTranslation;
	}
	
	public void centerModel() {
		setPosX(0);
		setPosY(0);
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public float getPosX() {
		return posX;
	}

	public void setPosX(float posX) {
		this.posX = posX;
	}

	public float getPosY() {
		return posY;
	}

	public void setPosY(float posY) {
		this.posY = posY;
	}

	public float getRotX() {
		return rotX;
	}

	public void setRotX(float rotX) {
		this.rotX = rotX;
	}

	public float getRotY() {
		return rotY;
	}

	public void setRotY(float rotY) {
		this.rotY = rotY;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
	public void setUserRequestedAnnotation(boolean requestedAnnotation) {
		this.userRequestedAnnotation = requestedAnnotation;
	}
	
	public void setUserRequestedPopup(boolean requestedPopup) {
		this.userRequestedPopup = requestedPopup;
	}
	
	public void setClickX(float clickX) {
		this.clickX = clickX;
	}
	
	public void setClickY(float clickY) {
		this.clickY = clickY;
	}
	
	public int getDisplayedAnnotation() {
		return displayedAnnotation;
	}
	
	public void setDisplayedAnnotation(int displayedAnnotation) {
		this.displayedAnnotation = displayedAnnotation;
	}
	
	private float getPushPinSize(float pushDiam, float meshDiam) {
		float neededDiam = meshDiam * pushPinSizeFactor;
			
		return neededDiam / pushDiam;
	}
	
	/*
	 * 3D Picking methods
	 */
	
	private void createRay(float[] P1, float[] P2) {
		float[] temp = new float[4];
		float[] temp2 = new float[4];
		
		float inverseY = viewport[3] - clickY;
		
		int result = GLU.gluUnProject(clickX, inverseY, 0.0f, MVMatrix, 0, projectionMatrix, 0, viewport, 0, temp, 0);
		if(result == GL10.GL_TRUE) {
			P1[0] = temp[0] / temp[3];
			P1[1] = temp[1] / temp[3];
            P1[2] = temp[2] / temp[3];
		}
		
		result = GLU.gluUnProject(clickX, inverseY, 1.0f, MVMatrix, 0, projectionMatrix, 0, viewport, 0, temp2, 0);
		if(result == GL10.GL_TRUE) {
			P2[0] = temp2[0] / temp2[3];
			P2[1] = temp2[1] / temp2[3];
            P2[2] = temp2[2] / temp2[3];
		}
	}
	
	private float[] findIntersection(float[] P1, float[] P2) {
		ArrayList<float[]> points = new ArrayList<float[]>();  
    	FloatBuffer vertexBuffer = model.getVertexBuffer();
        for(ModelPart part: model.getParts()) {
        	ShortBuffer faceBuffer = part.getFaceBuffer();
        	for(int i=0;i<faceBuffer.capacity()/3;i++) {
        		int index1 = faceBuffer.get(i*3);
        		int index2 = faceBuffer.get(i*3 + 1);
        		int index3 = faceBuffer.get(i*3 + 2);
        		
        		float[] v1 = new float[3];
        		v1[0] = vertexBuffer.get(index1*3);
        		v1[1] = vertexBuffer.get(index1*3 + 1);
        		v1[2] = vertexBuffer.get(index1*3 + 2);
        		
        		float[] v2 = new float[3];
        		v2[0] = vertexBuffer.get(index2*3);
        		v2[1] = vertexBuffer.get(index2*3 + 1);
        		v2[2] = vertexBuffer.get(index2*3 + 2);
        		
        		float[] v3 = new float[3];
        		v3[0] = vertexBuffer.get(index3*3);
        		v3[1] = vertexBuffer.get(index3*3 + 1);
        		v3[2] = vertexBuffer.get(index3*3 + 2);
        		
        		float[] hit = new float[6];
        		if(intersects(v1, v2, v3, P1, P2, hit)) {
        			points.add(hit);
        		}
        	}
        }
        
        float min;
        int minPos;
        if(points.size() > 0) {
        	min = MathHelper.distance(P1, points.get(0));
        	minPos = 0;
        	
        	for(int i=1;i<points.size();i++) {
        		float distance = MathHelper.distance(P1, points.get(i));
        		if(distance < min) {
        			min = distance;
        			minPos = i;
        		}
        	}
        } else {
        	return null;
        }
   
        return points.get(minPos);        
	}
	
	private boolean intersects(float[] v1, float[] v2, float[] v3, float[] P1, float[] P2, float[] hit) {
		float[] normal = new float[3];
		float[] intersectPos = new float[3];
				
		normal = MathHelper.crossProduct(MathHelper.sub(v2, v1), MathHelper.sub(v3, v1));
		
		float dist1 = MathHelper.dot(MathHelper.sub(P1, v1), normal);
		float dist2 = MathHelper.dot(MathHelper.sub(P2, v1), normal);
		
		// line doesn't cross the triangle
		if((dist1 * dist2) >= 0.0f)
			return false;
		
		// line and plane are parallel
		if(dist1 == dist2) 
			return false;
		
		intersectPos = MathHelper.add(P1, MathHelper.scalarProduct((-dist1/(dist2-dist1)), MathHelper.sub(P2, P1)));
			
		float[] test = new float[3];
		test = MathHelper.crossProduct(normal, MathHelper.sub(v2, v1));
		if(MathHelper.dot(test, MathHelper.sub(intersectPos, v1)) < 0.0f)
			return false;
		
		test = MathHelper.crossProduct(normal, MathHelper.sub(v3, v2));
		if(MathHelper.dot(test, MathHelper.sub(intersectPos, v2)) < 0.0f)
			return false;
		
		test = MathHelper.crossProduct(normal, MathHelper.sub(v1, v3));
		if(MathHelper.dot(test, MathHelper.sub(intersectPos, v1)) < 0.0f)
			return false;
		
		/*// trying to find an angle
		float[] u = MathHelper.sub(intersectPos, P1);
		
		float arithm = Math.abs(normal[0] * u[0] + normal[1] * u[1] + normal[2] * u[2]);
		float paran = MathHelper.length(normal) * MathHelper.length(u);
		
		double angle = Math.asin(arithm/paran);*/
		
		hit[0] = intersectPos[0];
		hit[1] = intersectPos[1];
		hit[2] = intersectPos[2];
		
		hit[3] = normal[0];
		hit[4] = normal[1];
		hit[5] = normal[2];
		
		return true;
	}
	
	/*
	 * 3D to 2D pixel coordinates
	 */
	private int[] get2dPoint(float[] pos, int width, int height) {
		float[] vpMatrix = new float[16];
		Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, MVMatrix, 0);

		float[] projectedPoint = new float[4];
		Matrix.multiplyMV(projectedPoint, 0, vpMatrix, 0, new float[] {pos[0], pos[1], pos[2], 1}, 0);
		
		float x = projectedPoint[0] / projectedPoint[3];
		float y = projectedPoint[1] / projectedPoint[3];
		
		int[] ret = new int[2];
		ret[0] = (int) (Math.round(((x + 1) / 2.0) * width));
		ret[1] = (int) (Math.round(((1 - y) / 2.0) * height));
		return ret;
	}
	
	/*
	 * Dialog listeners implementation
	 */
	@Override
	public void saveAnnotation(Annotation annotation) {
		annotations.add(context, annotation);
	}

	@Override
	public void deleteAnnotation(int id) {
		annotations.remove(context, id);		
	}
	
	@Override
	public void editAnnotation(int id, String title, String text) {
		annotations.edit(context, id, title, text);
	}
	
	@Override
	public void closeAnnotation() {
		displayedAnnotation = -1;
		displayedStage = CLOSED_STAGE;
	}
	
	public static void checkGlError(String op) {
	    int error;
	    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
	    	Log.d(TAG, op + ": glError " + error);
	    	throw new RuntimeException(op + ": glError " + error);
	    }
	}
}
