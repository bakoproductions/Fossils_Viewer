uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;

attribute vec4 a_Position;
attribute vec2 a_TexCoordinate;
attribute vec3 a_Normal;
attribute vec3 a_LightColor;

varying vec3 v_Position;
varying vec2 v_TexCoordinate;
varying vec3 v_Normal;
varying vec3 v_LightColor;

void main() {
	v_Position = vec3(u_MVMatrix * a_Position);
	v_TexCoordinate = a_TexCoordinate;
	v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
	
	v_LightColor = a_LightColor;
	
	gl_Position = u_MVPMatrix * a_Position;
}