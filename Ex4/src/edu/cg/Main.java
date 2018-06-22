package edu.cg;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;

import edu.cg.models.*;


public class Main {

	static IRenderable[] models = {new RollerCoaster(), new Locomotive()};
	static Point prevMouse;
	static int currentModel;
	static Frame frame;

	/**
	 * Create frame, canvas and viewer, and load the first model.
	 *
	 * @param args
	 *            No arguments
	 */
	public static void main(String[] args) {

		frame = new JFrame();

		// General OpenGL init
		GLProfile.initSingleton();
		GLProfile glp = GLProfile.get(GLProfile.GL2);
		GLCapabilities caps = new GLCapabilities(glp);
		final GLJPanel canvas = new GLJPanel(caps);

		frame.setSize(500, 500);
		frame.setLayout(new BorderLayout());
		frame.add(canvas, BorderLayout.CENTER);

		// Create viewer and initialize with first model
		final Viewer viewer = new Viewer(canvas);
		viewer.setModel(nextModel());

		// Add event listeners
		canvas.addGLEventListener(viewer);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				System.exit(1);
			}
		});

		canvas.addKeyListener(new KeyAdapter() {
			int num = 0;

			@Override
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				switch(code) {
					// Toggle wireframe mode
					case KeyEvent.VK_P:
						viewer.toggleRenderMode();
						break;

					// Toggle axes
					case KeyEvent.VK_A:
						viewer.toggleAxes();
						break;

					// Toggle light spheres
					case KeyEvent.VK_L:
						viewer.toggleLightSpheres();
						break;

					// Show next model
					case KeyEvent.VK_SPACE:
					case KeyEvent.VK_M:
						viewer.setModel(nextModel());
						break;

					// Set camera to follow model (ex4) ///////NEW
					case KeyEvent.VK_C:
						viewer.toggleModelCamera();
						break;

					case KeyEvent.VK_UP:
						viewer.getModel().control(KeyEvent.VK_UP, null);
						break;

					case KeyEvent.VK_DOWN:
						viewer.getModel().control(KeyEvent.VK_DOWN, null);
						break;

					case KeyEvent.VK_ENTER:
						int num = this.num;
						this.num = 0;
						viewer.getModel().control(KeyEvent.VK_ENTER, num);
						break;

					case KeyEvent.VK_ESCAPE:
						System.exit(0);
				}

				if(code >= KeyEvent.VK_0 & code <= KeyEvent.VK_9) {
					int n = code - KeyEvent.VK_0;
					num = num*10 + n;
				} else if(code >= KeyEvent.VK_NUMPAD0 & code <= KeyEvent.VK_NUMPAD9) {
					int n = code - KeyEvent.VK_NUMPAD0;
					num = num*10 + n;
				}

				canvas.repaint();
			}
		});

		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				// Let mouse drag affect trackball view
				viewer.trackball(prevMouse, e.getPoint());
				prevMouse = e.getPoint();
			}
		});

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				prevMouse = e.getPoint();
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				viewer.trackball(prevMouse, e.getPoint());
				super.mouseReleased(e);
			}
		});

		canvas.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// Let mouse wheel affect zoom
				double rot = e.getWheelRotation();
				viewer.zoom(rot); //zoom out for negative rot, zoom in for positive rot.
				canvas.repaint();
			}
		});

		// Show frame

		canvas.setFocusable(true);
		canvas.requestFocus();
		frame.setVisible(true);
		canvas.repaint();
	}

	/**
	 * Return the next model in the array
	 *
	 * @return IRenderable model
	 */
	private static IRenderable nextModel() {
		IRenderable model = models[currentModel++];
		frame.setTitle("Exercise 4 - " + model.toString());
		currentModel = currentModel%models.length;
		return model;
	}
}
