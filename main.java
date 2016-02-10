import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;

public class main {
	static Board board;
	static Logger logger;
	static Handler handler;
	static int finalCost;
	
	public static void main(String[] args) {
		board = new Board(new State(100, 100), new State(400, 400));
		logger = Logger.getLogger(main.class.getName());
		finalCost = 0;
		
		try {
			handler = new FileHandler("logfile%g.txt");
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);
		}
		catch (IOException Err) {
			System.out.println(Err.getMessage());
			logger.log(Level.WARNING, "Error creating log file.");
		}
		
		try {
			board.setBoard("terrain.png");
			logger.log(Level.INFO, "Loaded terrain image.");
		}
		catch (IOException NoFile) {
			logger.log(Level.SEVERE,"Error loading terrain image.");
			logger.log(Level.SEVERE, NoFile.getMessage());	
			return;
		}
		
		board.createOutImage();
		
		logger.log(Level.INFO, "Starting.");
		finalCost = board.findPath(logger);
		if (finalCost == -1) {
			logger.log(Level.INFO, "No path found.");
		}
		else {
			logger.log(Level.INFO, "Cost was " + Integer.toString(finalCost));
		}
		
		board.drawPath();
		logger.log(Level.INFO, "Creating output image.");
		try {
			board.setOutImage("path.png");
		}
		catch (IOException FileErr) {
			logger.log(Level.SEVERE, "Error creating path image.");
		}
		
		logger.log(Level.INFO, "Complete.");
	}
}

class Board {
	State startState;
	State goalState;
	
	BufferedImage board;
	BufferedImage newImage;
	PriorityQueue<State> stateQueue;
	TreeSet<State> beenThere;
	
	int height;
	int width;
	
	public Board(State start, State goal) {
		stateQueue = new PriorityQueue<State>(10, new CostComparator());
		beenThere = new TreeSet<State>(new StateComparator());
		startState = start;
		startState.setCost(0);
		startState.parent = null;
		
		goalState = goal;
	}
	
	public void setBoard(String file) throws IOException {
		board = ImageIO.read(new File(file));
		height = board.getHeight();
		width = board.getWidth();
	}
	
	public void createOutImage() {
		newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	
	public void setOutImage(String file) throws IOException {
		ImageIO.write(newImage, "png", new File(file));
	}
	
	public void drawPath() {
		State currentState = goalState;
		
		while (currentState.parent != null) {
			newImage.setRGB(currentState.getX(), currentState.getY(), 0xFFFF0000);
			currentState = currentState.parent;
		};
	}
	
	public int findPath(Logger logger) {
		int x;
		int y;
		int cost = 0;
		Color c;
		State tempState;
		int iterations = 0;
		
		stateQueue.add(startState);
		beenThere.add(startState);
		
		while (stateQueue.size() > 0) {
			State currentState = stateQueue.remove();
			
			if (currentState.isEquals(goalState)) {
				goalState.parent = currentState.parent;
				return currentState.getCost();
			}
			
			x = currentState.getX();
			y = currentState.getY();
			
			if (iterations++ % 5000 < 1000) {
				newImage.setRGB(x, y, 0xff00ff00);
			}
			else {
				newImage.setRGB(x, y, board.getRGB(x, y));
			}
			
			// look up
			if (x != 0) {
				State upState = new State(x-1, y);
				c = new Color(board.getRGB(x-1, y));
				cost = c.getGreen();
				
				if (beenThere.contains(upState)) {
					tempState = beenThere.floor(upState);
					if (currentState.getCost() + cost < tempState.getCost()) {
						tempState.setCost(currentState.getCost() + cost);
						tempState.parent = currentState;
					}
				}
				else {
					upState.setCost(currentState.getCost() + cost);
					upState.parent = currentState;
					stateQueue.add(upState);
					beenThere.add(upState);
				}
			}
			
			// look down
			if (x < (height - 1)) {
				State downState = new State(x+1, y);
				c = new Color(board.getRGB(x+1, y));
				cost = c.getGreen();
				
				if (beenThere.contains(downState)) {
					tempState = beenThere.floor(downState);
					if (currentState.getCost() + cost < tempState.getCost()) {
						tempState.setCost(currentState.getCost() + cost);
						tempState.parent = currentState;
					}
				}
				else {
					downState.setCost(currentState.getCost() + cost);
					downState.parent = currentState;
					stateQueue.add(downState);
					beenThere.add(downState);
				}
			}
			
			// look left
			if (y != 0) {
				State leftState = new State(x, y-1);
				c = new Color(board.getRGB(x, y-1));
				cost = c.getGreen();
				
				if (beenThere.contains(leftState)) {
					tempState = beenThere.floor(leftState);
					if (currentState.getCost() + cost < tempState.getCost()) {
						tempState.setCost(currentState.getCost() + cost);
						tempState.parent = currentState;
					}
				}
				else {
					leftState.setCost(currentState.getCost() + cost);
					leftState.parent = currentState;
					stateQueue.add(leftState);
					beenThere.add(leftState);
				}
			}
			
			if (y < (width - 1)) {
				State rightState = new State(x, y+1);
				c = new Color(board.getRGB(x, y+1));
				cost = c.getGreen();
				
				if (beenThere.contains(rightState)) {
					tempState = beenThere.floor(rightState);
					if (currentState.getCost() + cost < tempState.getCost()) {
						tempState.setCost(currentState.getCost() + cost);
						tempState.parent = currentState;
					}
				}
				else {
					rightState.setCost(currentState.getCost() + cost);
					rightState.parent = currentState;
					stateQueue.add(rightState);
					beenThere.add(rightState);
				}
			}	
			
		}
		return -1;
	}
}

class State {
	private int x;
	private int y;
	private int cost;
	State parent;
	
	State() {
		
	}
	
	State(int newX, int newY) {
		x = newX;
		y = newY;
	}
	
	public boolean isEquals(State test) {
		if ((x == test.getX()) && (y == test.getY())) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int newX) {
		x = newX;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int newY) {
		y = newY;
	}
	
	public int getCost() {
		return cost;
	}
	
	public void setCost(int newCost) {
		cost = newCost;
	}
	
	public String getPosition() {
		return new String("(" + Integer.toString(x) + "," + Integer.toString(y) + ")");
	}
}

class CostComparator implements Comparator<State> {
	public int compare(State x, State y) {
		if (x.getCost() < y.getCost()) {
			return -1;
		} else if(x.getCost() > y.getCost()) {
			return 1;
		} else {
			return 0;
		}
	}
}

class StateComparator implements Comparator<State> {
	public int compare(State left, State right) {
		if (left.getX() < right.getX()) {
			return -1;
		} else if (left.getX() > right.getX()) {
			return 1;
		} else if (left.getY() < right.getY()) {
			return -1;
		} else if (left.getY() > right.getY()) {
			return 1;
		} else {
			return 0;
		}
	}
}
