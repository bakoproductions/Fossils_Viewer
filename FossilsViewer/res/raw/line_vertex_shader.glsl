uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;

void main() {
    // the matrix must be included as a modifier of gl_Position
	gl_Position = u_MVPMatrix * a_Position;
	gl_PointSize = 5.0;
}