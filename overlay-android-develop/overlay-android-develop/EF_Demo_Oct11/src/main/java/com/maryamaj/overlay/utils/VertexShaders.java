package com.maryamaj.overlay.utils;

public class VertexShaders {
	
	
	 public static final String AREA_VERTEX_SHADER =
		        // This matrix member variable provides a hook to manipulate
		        // the coordinates of the objects that use this vertex shader
		        "uniform mat4 modelViewProjectionMatrix;" +
		        "attribute vec4 vertexPosition;" +
		        "void main() {" +
		        // the matrix must be included as a modifier of gl_Position
		        // Note that the uMVPMatrix factor *must be first* in order
		        // for the matrix multiplication product to be correct.
		        "  gl_Position = modelViewProjectionMatrix * vertexPosition;" +
		        "}";

	public static final String AREA_FRAGMENT_SHADER =
			"precision mediump float;" +
					"uniform vec4 color;" +
					"void main() {" +
					"  gl_FragColor = vec4(color.r, color.g, color.b, 1.0);" +
					"}";
	

}
