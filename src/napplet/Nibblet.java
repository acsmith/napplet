package napplet;

import java.awt.Frame;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PStyle;

/**
 * Nit implementation that draws directly on its parent's display space.
 * 
 * @author acsmith
 * 
 */
public class Nibblet extends NitBase {

	public PStyle pStyle = new PStyle();

	public boolean persistentStyle = true;
	public boolean resetMatrix = true;

	public int frameCount = 0;

	public void setNAppletManager(NAppletManager nappletManager) {
		super.setNAppletManager(nappletManager);
		this.g = parentPApplet.g;
	}

	@Override
	public void runFrame() {

		boolean internalPersistentStyle = persistentStyle;
		boolean internalResetMatrix = resetMatrix;

		if (frameCount == 0)
			g.getStyle(pStyle);

		if (internalPersistentStyle) {
			g.pushStyle();
			g.style(pStyle);
		}
		if (internalResetMatrix) {
			g.pushMatrix();
			g.resetMatrix();
			g.translate(getPositionX(), getPositionY());
		}
		if (frameCount == 0) {
			this.preSetup();
			this.setup();
			this.postSetup();
		} else {
			this.preDraw();
			this.draw();
			this.postDraw();
		}
		if (internalResetMatrix) {
			g.popMatrix();
		}
		if (internalPersistentStyle) {
			g.getStyle(pStyle);
			g.popStyle();
		}

		frameCount++;

	}

	public void size(int w, int h) {
		width = w;
		height = h;
	}
	
	public void position(int x, int y) {
		nitX = x;
		nitY = y;
	}
	
	// The pre- and post- draw and setup methods are provided for subclassing
	// utility; for example, you can create a Nibblet subclass outside of the
	// PDE (e.g., in Eclipse) that does some things in preDraw() and postDraw(),
	// and then leaves the user who subclasses it further in the PDE free to do
	// whatever s/he wants in draw() without needing to worry about calling 
	// super.draw() and such.
	

	public void preSetup() {
	}

	public void postSetup() {
	}

	public void preDraw() {
	}

	public void postDraw() {
	}

	@Override
	public Frame getFrame() {
		return null;
	}

	// Everything below is transplanted from PApplet, pretty much by
	// cut-n-paste, just with comments and recorder commands removed.

	public void flush() {
		g.flush();
	}

	public void hint(int which) {
		g.hint(which);
	}

	public void beginShape() {
		g.beginShape();
	}

	public void beginShape(int kind) {
		g.beginShape(kind);
	}

	public void edge(boolean edge) {
		g.edge(edge);
	}

	public void normal(float nx, float ny, float nz) {
		g.normal(nx, ny, nz);
	}

	public void textureMode(int mode) {
		g.textureMode(mode);
	}

	public void texture(PImage image) {
		g.texture(image);
	}

	public void vertex(float x, float y) {
		g.vertex(x, y);
	}

	public void vertex(float x, float y, float z) {
		g.vertex(x, y, z);
	}

	public void vertex(float[] v) {
		g.vertex(v);
	}

	public void vertex(float x, float y, float u, float v) {
		g.vertex(x, y, u, v);
	}

	public void vertex(float x, float y, float z, float u, float v) {
		g.vertex(x, y, z, u, v);
	}

	public void breakShape() {
		g.breakShape();
	}

	public void endShape() {
		g.endShape();
	}

	public void endShape(int mode) {
		g.endShape(mode);
	}

	public void bezierVertex(float x2, float y2, float x3, float y3, float x4,
			float y4) {
		g.bezierVertex(x2, y2, x3, y3, x4, y4);
	}

	public void bezierVertex(float x2, float y2, float z2, float x3, float y3,
			float z3, float x4, float y4, float z4) {
		g.bezierVertex(x2, y2, z2, x3, y3, z3, x4, y4, z4);
	}

	public void curveVertex(float x, float y) {
		g.curveVertex(x, y);
	}

	public void curveVertex(float x, float y, float z) {
		g.curveVertex(x, y, z);
	}

	public void point(float x, float y) {
		g.point(x, y);
	}

	public void point(float x, float y, float z) {
		g.point(x, y, z);
	}

	public void line(float x1, float y1, float x2, float y2) {
		g.line(x1, y1, x2, y2);
	}

	public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
		g.line(x1, y1, z1, x2, y2, z2);
	}

	public void triangle(float x1, float y1, float x2, float y2, float x3,
			float y3) {
		g.triangle(x1, y1, x2, y2, x3, y3);
	}

	public void quad(float x1, float y1, float x2, float y2, float x3,
			float y3, float x4, float y4) {
		g.quad(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	public void rectMode(int mode) {
		g.rectMode(mode);
	}

	public void rect(float a, float b, float c, float d) {
		g.rect(a, b, c, d);
	}

	public void ellipseMode(int mode) {
		g.ellipseMode(mode);
	}

	public void ellipse(float a, float b, float c, float d) {
		g.ellipse(a, b, c, d);
	}

	public void arc(float a, float b, float c, float d, float start, float stop) {
		g.arc(a, b, c, d, start, stop);
	}

	public void box(float size) {
		g.box(size);
	}

	public void box(float w, float h, float d) {
		g.box(w, h, d);
	}

	public void sphereDetail(int res) {
		g.sphereDetail(res);
	}

	public void sphereDetail(int ures, int vres) {
		g.sphereDetail(ures, vres);
	}

	public void sphere(float r) {
		g.sphere(r);
	}

	public float bezierPoint(float a, float b, float c, float d, float t) {
		return g.bezierPoint(a, b, c, d, t);
	}

	public float bezierTangent(float a, float b, float c, float d, float t) {
		return g.bezierTangent(a, b, c, d, t);
	}

	public void bezierDetail(int detail) {
		g.bezierDetail(detail);
	}

	public void bezier(float x1, float y1, float x2, float y2, float x3,
			float y3, float x4, float y4) {
		g.bezier(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	public void bezier(float x1, float y1, float z1, float x2, float y2,
			float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
		g.bezier(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
	}

	public float curvePoint(float a, float b, float c, float d, float t) {
		return g.curvePoint(a, b, c, d, t);
	}

	public float curveTangent(float a, float b, float c, float d, float t) {
		return g.curveTangent(a, b, c, d, t);
	}

	public void curveDetail(int detail) {
		g.curveDetail(detail);
	}

	public void curveTightness(float tightness) {
		g.curveTightness(tightness);
	}

	public void curve(float x1, float y1, float x2, float y2, float x3,
			float y3, float x4, float y4) {
		g.curve(x1, y1, x2, y2, x3, y3, x4, y4);
	}

	public void curve(float x1, float y1, float z1, float x2, float y2,
			float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
		g.curve(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
	}

	public void smooth() {
		g.smooth();
	}

	public void noSmooth() {
		g.noSmooth();
	}

	public void imageMode(int mode) {
		g.imageMode(mode);
	}

	public void image(PImage image, float x, float y) {
		g.image(image, x, y);
	}

	public void image(PImage image, float x, float y, float c, float d) {
		g.image(image, x, y, c, d);
	}

	public void image(PImage image, float a, float b, float c, float d, int u1,
			int v1, int u2, int v2) {
		g.image(image, a, b, c, d, u1, v1, u2, v2);
	}

	public void shapeMode(int mode) {
		g.shapeMode(mode);
	}

	public void shape(PShape shape) {
		g.shape(shape);
	}

	public void shape(PShape shape, float x, float y) {
		g.shape(shape, x, y);
	}

	public void shape(PShape shape, float x, float y, float c, float d) {
		g.shape(shape, x, y, c, d);
	}

	public void textAlign(int align) {
		g.textAlign(align);
	}

	public void textAlign(int alignX, int alignY) {
		g.textAlign(alignX, alignY);
	}

	public float textAscent() {
		return g.textAscent();
	}

	public float textDescent() {
		return g.textDescent();
	}

	public void textFont(PFont which) {
		g.textFont(which);
	}

	public void textFont(PFont which, float size) {
		g.textFont(which, size);
	}

	public void textLeading(float leading) {
		g.textLeading(leading);
	}

	public void textMode(int mode) {
		g.textMode(mode);
	}

	public void textSize(float size) {
		g.textSize(size);
	}

	public float textWidth(char c) {
		return g.textWidth(c);
	}

	public float textWidth(String str) {
		return g.textWidth(str);
	}

	public float textWidth(char[] chars, int start, int length) {
		return g.textWidth(chars, start, length);
	}

	public void text(char c) {
		g.text(c);
	}

	public void text(char c, float x, float y) {
		g.text(c, x, y);
	}

	public void text(char c, float x, float y, float z) {
		g.text(c, x, y, z);
	}

	public void text(String str) {
		g.text(str);
	}

	public void text(String str, float x, float y) {
		g.text(str, x, y);
	}

	public void text(char[] chars, int start, int stop, float x, float y) {
		g.text(chars, start, stop, x, y);
	}

	public void text(String str, float x, float y, float z) {
		g.text(str, x, y, z);
	}

	public void text(char[] chars, int start, int stop, float x, float y,
			float z) {
		g.text(chars, start, stop, x, y, z);
	}

	public void text(String str, float x1, float y1, float x2, float y2) {
		g.text(str, x1, y1, x2, y2);
	}

	public void text(String s, float x1, float y1, float x2, float y2, float z) {
		g.text(s, x1, y1, x2, y2, z);
	}

	public void text(int num, float x, float y) {
		g.text(num, x, y);
	}

	public void text(int num, float x, float y, float z) {
		g.text(num, x, y, z);
	}

	public void text(float num, float x, float y) {
		g.text(num, x, y);
	}

	public void text(float num, float x, float y, float z) {
		g.text(num, x, y, z);
	}

	public void pushMatrix() {
		g.pushMatrix();
	}

	public void popMatrix() {
		g.popMatrix();
	}

	public void translate(float tx, float ty) {
		g.translate(tx, ty);
	}

	public void translate(float tx, float ty, float tz) {
		g.translate(tx, ty, tz);
	}

	public void rotate(float angle) {
		g.rotate(angle);
	}

	public void rotateX(float angle) {
		g.rotateX(angle);
	}

	public void rotateY(float angle) {
		g.rotateY(angle);
	}

	public void rotateZ(float angle) {
		g.rotateZ(angle);
	}

	public void rotate(float angle, float vx, float vy, float vz) {
		g.rotate(angle, vx, vy, vz);
	}

	public void scale(float s) {
		g.scale(s);
	}

	public void scale(float sx, float sy) {
		g.scale(sx, sy);
	}

	public void scale(float x, float y, float z) {
		g.scale(x, y, z);
	}

	public void skewX(float angle) {
		g.skewX(angle);
	}

	public void skewY(float angle) {
		g.skewY(angle);
	}

	public void resetMatrix() {
		g.resetMatrix();
	}

	public void applyMatrix(PMatrix source) {
		g.applyMatrix(source);
	}

	public void applyMatrix(PMatrix2D source) {
		g.applyMatrix(source);
	}

	public void applyMatrix(float n00, float n01, float n02, float n10,
			float n11, float n12) {
		g.applyMatrix(n00, n01, n02, n10, n11, n12);
	}

	public void applyMatrix(PMatrix3D source) {
		g.applyMatrix(source);
	}

	public void applyMatrix(float n00, float n01, float n02, float n03,
			float n10, float n11, float n12, float n13, float n20, float n21,
			float n22, float n23, float n30, float n31, float n32, float n33) {
		g.applyMatrix(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22,
				n23, n30, n31, n32, n33);
	}

	public PMatrix getMatrix() {
		return g.getMatrix();
	}

	public PMatrix2D getMatrix(PMatrix2D target) {
		return g.getMatrix(target);
	}

	public PMatrix3D getMatrix(PMatrix3D target) {
		return g.getMatrix(target);
	}

	public void setMatrix(PMatrix source) {
		g.setMatrix(source);
	}

	public void setMatrix(PMatrix2D source) {
		g.setMatrix(source);
	}

	public void setMatrix(PMatrix3D source) {
		g.setMatrix(source);
	}

	public void printMatrix() {
		g.printMatrix();
	}

	public void beginCamera() {
		g.beginCamera();
	}

	public void endCamera() {
		g.endCamera();
	}

	public void camera() {
		g.camera();
	}

	public void camera(float eyeX, float eyeY, float eyeZ, float centerX,
			float centerY, float centerZ, float upX, float upY, float upZ) {
		g.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
	}

	public void printCamera() {
		g.printCamera();
	}

	public void ortho() {
		g.ortho();
	}

	public void ortho(float left, float right, float bottom, float top,
			float near, float far) {
		g.ortho(left, right, bottom, top, near, far);
	}

	public void perspective() {
		g.perspective();
	}

	public void perspective(float fovy, float aspect, float zNear, float zFar) {
		g.perspective(fovy, aspect, zNear, zFar);
	}

	public void frustum(float left, float right, float bottom, float top,
			float near, float far) {
		g.frustum(left, right, bottom, top, near, far);
	}

	public void printProjection() {
		g.printProjection();
	}

	public float screenX(float x, float y) {
		return g.screenX(x, y);
	}

	public float screenY(float x, float y) {
		return g.screenY(x, y);
	}

	public float screenX(float x, float y, float z) {
		return g.screenX(x, y, z);
	}

	public float screenY(float x, float y, float z) {
		return g.screenY(x, y, z);
	}

	public float screenZ(float x, float y, float z) {
		return g.screenZ(x, y, z);
	}

	public float modelX(float x, float y, float z) {
		return g.modelX(x, y, z);
	}

	public float modelY(float x, float y, float z) {
		return g.modelY(x, y, z);
	}

	public float modelZ(float x, float y, float z) {
		return g.modelZ(x, y, z);
	}

	public void pushStyle() {
		g.pushStyle();
	}

	public void popStyle() {
		g.popStyle();
	}

	public void style(PStyle s) {
		g.style(s);
	}

	public void strokeWeight(float weight) {
		g.strokeWeight(weight);
	}

	public void strokeJoin(int join) {
		g.strokeJoin(join);
	}

	public void strokeCap(int cap) {
		g.strokeCap(cap);
	}

	public void noStroke() {
		g.noStroke();
	}

	public void stroke(int rgb) {
		g.stroke(rgb);
	}

	public void stroke(int rgb, float alpha) {
		g.stroke(rgb, alpha);
	}

	public void stroke(float gray) {
		g.stroke(gray);
	}

	public void stroke(float gray, float alpha) {
		g.stroke(gray, alpha);
	}

	public void stroke(float x, float y, float z) {
		g.stroke(x, y, z);
	}

	public void stroke(float x, float y, float z, float a) {
		g.stroke(x, y, z, a);
	}

	public void noTint() {
		g.noTint();
	}

	public void tint(int rgb) {
		g.tint(rgb);
	}

	public void tint(int rgb, float alpha) {
		g.tint(rgb, alpha);
	}

	public void tint(float gray) {
		g.tint(gray);
	}

	public void tint(float gray, float alpha) {
		g.tint(gray, alpha);
	}

	public void tint(float x, float y, float z) {
		g.tint(x, y, z);
	}

	public void tint(float x, float y, float z, float a) {
		g.tint(x, y, z, a);
	}

	public void noFill() {
		g.noFill();
	}

	public void fill(int rgb) {
		g.fill(rgb);
	}

	public void fill(int rgb, float alpha) {
		g.fill(rgb, alpha);
	}

	public void fill(float gray) {
		g.fill(gray);
	}

	public void fill(float gray, float alpha) {
		g.fill(gray, alpha);
	}

	public void fill(float x, float y, float z) {
		g.fill(x, y, z);
	}

	public void fill(float x, float y, float z, float a) {
		g.fill(x, y, z, a);
	}

	public void ambient(int rgb) {
		g.ambient(rgb);
	}

	public void ambient(float gray) {
		g.ambient(gray);
	}

	public void ambient(float x, float y, float z) {
		g.ambient(x, y, z);
	}

	public void specular(int rgb) {
		g.specular(rgb);
	}

	public void specular(float gray) {
		g.specular(gray);
	}

	public void specular(float x, float y, float z) {
		g.specular(x, y, z);
	}

	public void shininess(float shine) {
		g.shininess(shine);
	}

	public void emissive(int rgb) {
		g.emissive(rgb);
	}

	public void emissive(float gray) {
		g.emissive(gray);
	}

	public void emissive(float x, float y, float z) {
		g.emissive(x, y, z);
	}

	public void lights() {
		g.lights();
	}

	public void noLights() {
		g.noLights();
	}

	public void ambientLight(float red, float green, float blue) {
		g.ambientLight(red, green, blue);
	}

	public void ambientLight(float red, float green, float blue, float x,
			float y, float z) {
		g.ambientLight(red, green, blue, x, y, z);
	}

	public void directionalLight(float red, float green, float blue, float nx,
			float ny, float nz) {
		g.directionalLight(red, green, blue, nx, ny, nz);
	}

	public void pointLight(float red, float green, float blue, float x,
			float y, float z) {
		g.pointLight(red, green, blue, x, y, z);
	}

	public void spotLight(float red, float green, float blue, float x, float y,
			float z, float nx, float ny, float nz, float angle,
			float concentration) {
		g
				.spotLight(red, green, blue, x, y, z, nx, ny, nz, angle,
						concentration);
	}

	public void lightFalloff(float constant, float linear, float quadratic) {
		g.lightFalloff(constant, linear, quadratic);
	}

	public void lightSpecular(float x, float y, float z) {
		g.lightSpecular(x, y, z);
	}

	public void background(int rgb) {
		g.background(rgb);
	}

	public void background(int rgb, float alpha) {
		g.background(rgb, alpha);
	}

	public void background(float gray) {
		g.background(gray);
	}

	public void background(float gray, float alpha) {
		g.background(gray, alpha);
	}

	public void background(float x, float y, float z) {
		g.background(x, y, z);
	}

	public void background(float x, float y, float z, float a) {
		g.background(x, y, z, a);
	}

	public void background(PImage image) {
		g.background(image);
	}

	public void colorMode(int mode) {
		g.colorMode(mode);
	}

	public void colorMode(int mode, float max) {
		g.colorMode(mode, max);
	}

	public void colorMode(int mode, float maxX, float maxY, float maxZ) {
		g.colorMode(mode, maxX, maxY, maxZ);
	}

	public void colorMode(int mode, float maxX, float maxY, float maxZ,
			float maxA) {
		g.colorMode(mode, maxX, maxY, maxZ, maxA);
	}

	public final float alpha(int what) {
		return g.alpha(what);
	}

	public final float red(int what) {
		return g.red(what);
	}

	public final float green(int what) {
		return g.green(what);
	}

	public final float blue(int what) {
		return g.blue(what);
	}

	public final float hue(int what) {
		return g.hue(what);
	}

	public final float saturation(int what) {
		return g.saturation(what);
	}

	public final float brightness(int what) {
		return g.brightness(what);
	}

	public int lerpColor(int c1, int c2, float amt) {
		return g.lerpColor(c1, c2, amt);
	}

	static public int lerpColor(int c1, int c2, float amt, int mode) {
		return PGraphics.lerpColor(c1, c2, amt, mode);
	}

	public boolean displayable() {
		return g.displayable();
	}

	public void setCache(Object parent, Object storage) {
		g.setCache(parent, storage);
	}

	public Object getCache(Object parent) {
		return g.getCache(parent);
	}

	public void removeCache(Object parent) {
		g.removeCache(parent);
	}

	public int get(int x, int y) {
		return g.get(x, y);
	}

	public PImage get(int x, int y, int w, int h) {
		return g.get(x, y, w, h);
	}

	public PImage get() {
		return g.get();
	}

	public void set(int x, int y, int c) {
		g.set(x, y, c);
	}

	public void set(int x, int y, PImage src) {
		g.set(x, y, src);
	}

	public void mask(int maskArray[]) {
		g.mask(maskArray);
	}

	public void mask(PImage maskImg) {
		g.mask(maskImg);
	}

	public void filter(int kind) {
		g.filter(kind);
	}

	public void filter(int kind, float param) {
		g.filter(kind, param);
	}

	public void copy(int sx, int sy, int sw, int sh, int dx, int dy, int dw,
			int dh) {
		g.copy(sx, sy, sw, sh, dx, dy, dw, dh);
	}

	public void copy(PImage src, int sx, int sy, int sw, int sh, int dx,
			int dy, int dw, int dh) {
		g.copy(src, sx, sy, sw, sh, dx, dy, dw, dh);
	}

	static public int blendColor(int c1, int c2, int mode) {
		return PGraphics.blendColor(c1, c2, mode);
	}

	public void blend(int sx, int sy, int sw, int sh, int dx, int dy, int dw,
			int dh, int mode) {
		g.blend(sx, sy, sw, sh, dx, dy, dw, dh, mode);
	}

	public void blend(PImage src, int sx, int sy, int sw, int sh, int dx,
			int dy, int dw, int dh, int mode) {
		g.blend(src, sx, sy, sw, sh, dx, dy, dw, dh, mode);
	}
}
