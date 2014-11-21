precision mediump float;

uniform vec3 u_LightPos;       	
uniform sampler2D u_Texture;

varying vec3 v_Position;
varying vec2 v_TexCoordinate;
varying vec3 v_Normal;
varying vec3 v_LightColor;

void main() {
	float distance = length(u_LightPos - v_Position);
	
	vec3 lightVector = normalize(u_LightPos - v_Position);
	
	float diffuse = max(dot(v_Normal, lightVector), 0.0);
	diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance)));
	diffuse = diffuse + 0.7;
	
	
	
	gl_FragColor = (diffuse * texture2D(u_Texture, v_TexCoordinate));
	
}