import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

public class BWInf42Laubmaschen {
	
	/**
	 * Führt das Programm aus
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Grid Größe einlesen
		int gridSize = 7;
		if(args.length >= 1) gridSize = Integer.parseInt(args[0]);
		
		// Grid Instanz mit Größe x erstellen
		Grid grid = new Grid(gridSize);
		
		// UI erstellen
		buildUI(grid);
		
	}
	
	/**
	 * Erstellt UI von Grid
	 * @param grid Der Grid
	 */
	public static void buildUI(Grid grid) {
		
		// Erstellt JFrame
		JFrame frame = new JFrame("Laubmaschen");
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Erstellt UIGrid (erweitertes JPanel, siehe unten)
		UIGrid uiGrid = new UIGrid(300, grid);
		uiGrid.setLocation(100, 20);
		frame.getContentPane().add(uiGrid);
		
		// Erstelle Buttons
		JButton up = new JButton("^");
		up.setBounds(150, 420, 50, 50);
		up.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				grid.setDirection(Direction.UP);
				uiGrid.repaint();
			}
		});
		frame.getContentPane().add(up);
		up.repaint();
		
		JButton down = new JButton("v");
		down.setBounds(150, 470, 50, 50);
		down.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				grid.setDirection(Direction.DOWN);
				uiGrid.repaint();
			}
		});
		frame.getContentPane().add(down);
		down.repaint();
		
		JButton left = new JButton("<");
		left.setBounds(100, 470, 50, 50);
		left.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				grid.setDirection(Direction.LEFT);
				uiGrid.repaint();
			}
		});
		frame.getContentPane().add(left);
		left.repaint();
		
		JButton right = new JButton(">");
		right.setBounds(200, 470, 50, 50);
		right.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				grid.setDirection(Direction.RIGHT);
				uiGrid.repaint();
			}
		});
		frame.getContentPane().add(right);
		right.repaint();
		
		JButton blow = new JButton("B");
		blow.setBounds(200, 420, 50, 50);
		blow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				grid.blow();
				uiGrid.repaint();
			}
		});
		frame.getContentPane().add(blow);
		blow.repaint();
		
		// Erstelle Simulationseinstellungen
		JButton simulate = new JButton("Simuliere");
		simulate.setBounds(300, 420, 200, 50);
		simulate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					grid.simulate(uiGrid);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		});
		frame.getContentPane().add(simulate);
		simulate.repaint();
		
		JLabel lblSimSpeed = new JLabel("Simulationsgeschwindigkeit");
		lblSimSpeed.setBounds(300, 460, 200, 50);
		frame.getContentPane().add(lblSimSpeed);
		lblSimSpeed.repaint();
		
		JSlider simSpeed = new JSlider(1, 20, 2);
		simSpeed.setBounds(300, 500, 200, 50);
		simSpeed.setPaintTicks(true);
		simSpeed.setMajorTickSpacing(1);
		simSpeed.setPaintLabels(true);
		simSpeed.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				grid.setSimulationSpeed(simSpeed.getValue());
			}
		});
		frame.getContentPane().add(simSpeed);
		simSpeed.repaint();
		
	}
	
	
	public static class Grid {
		
		// Größe des Grids
		int size;

		// Aktuelle Position des Laubbläsers
		Location blower;
		// Aktuelle Richtung des Laubbläsers
		Direction dir = Direction.RIGHT;
		// Anzahl der Blätter auf dem Grid
		int[][] grid;
		
		// Simulationsgeschwindigkeit
		int simulationSpeed = 2;
		
		public Grid(int size) {
			if(size < 4) throw new IllegalArgumentException("Size must be at least 4");
			this.size = size;
			this.blower = new Location(0, 0);
			grid = new int[size][size];
			initGrid();
		}
		
		/**
		 * Initialisiere Grid mit 100 Blättern pro Feld
		 */
		private void initGrid() {
			for(int x = 0; x < size; x++) {
				for(int y = 0; y < size; y++) {
					grid[x][y] = 100;
				}
			}
		}
		
		/**
		 * Blase in aktuelle Richtung
		 */
		public void blow() {
			// Betroffene Positionen deklarieren und initialisieren
			Location target;
			Location behindTarget;
			Location leftTarget;
			Location rightTarget;
			
			target = dir.behindFrom(blower);
			behindTarget = dir.behindFrom(target);
			leftTarget = dir.leftFrom(target);
			rightTarget = dir.rightFrom(target);
			
			// Abbrechen, falls target-Position außerhalb des Grids liegt
			if(!containsLocation(target)) return;
			
			// Blätter auf dem aktuellen Feld
			int leaves = getLeaves(blower);
			// Überschuss nach rechts und links berechnen
			int goLeft = containsLocation(leftTarget) ? randomLeaves(leaves, 0.1) : 0;
			int goRight = containsLocation(rightTarget) ? randomLeaves(leaves, 0.1) : 0;
			// Überschuss von Gesamtblättern abziehen
			int goTarget = leaves - goLeft - goRight;
			
			// Blätterf dem target-Feld (hinter dem aktuellen Feld)
			int leavesBehindTarget = containsLocation(target) ? getLeaves(target) : 0;
			// Überschuss nach hinten berechnen
			int goBehind = randomLeaves(leavesBehindTarget, 0.1);
			
			// Blätter bewegen
			removeLeaves(target, goBehind);
			addLeaves(behindTarget, goBehind);
			
			setLeaves(blower, 0);
			addLeaves(target, goTarget);
			addLeaves(leftTarget, goLeft);
			addLeaves(rightTarget, goRight);
		}
		
		/**
		 * @param leaves Anzahl Blätter
		 * @param chance Chance für Blatt, auserwählt zu werden
		 * @return Wählt mich einer bestimmten Wahrscheinlichkeit Blätter aus einer Menge
		 */
		private int randomLeaves(int leaves, double chance) {
			int move = 0;
			for(int i = 0; i < leaves; i++) {
				if(Math.random() < chance) move++;
			}
			return move;
		}
		
		/**
		 * Simuliert optimalen Blaseprozess
		 * @param uiGrid Instanz des UIGrids
		 * @throws InterruptedException
		 */
		public void simulate(UIGrid uiGrid) throws InterruptedException {
			SimulationClock clock = new SimulationClock(this, uiGrid);
			clock.start();
		}
		
		
		/**
		 * @return Größe des Grids
		 */
		public int getSize() {
			return size;
		}
		
		/**
		 * @return Aktuelle Position des Laubbläsers
		 */
		public Location getBlower() {
			return blower;
		}
		
		/**
		 * Setzt aktuelle Position des Bläsers
		 * @param x X-Koordinate
		 * @param y Y-Koordinate
		 */
		public void setBlower(int x, int y) {
			blower.setX(x);
			blower.setY(y);
		}
		
		/**
		 * Richtung setzen
		 * @param dir Die Richtung
		 */
		public void setDirection(Direction dir) {
			this.dir = dir;
		}
		
		/**
		 * @return Die aktuelle Richtung des Laubbläsers
		 */
		public Direction getDirection() {
			return dir;
		}
		
		/**
		 * Simulationsgeschwindigkeit ändern
		 * @param simulationSpeed Die Simulationsgeschwindigkeit
		 */
		public void setSimulationSpeed(int simulationSpeed) {
			this.simulationSpeed = simulationSpeed;
		}
		
		/**
		 * @return Die Simulationsgeschwindigkeit
		 */
		public int getSimulationSpeed() {
			return simulationSpeed;
		}
		
		/**
		 * @param loc Die zu überprüfende Location
		 * @return Ob Location innerhalb des Grids ist
		 */
		public boolean containsLocation(Location loc) {
			return loc.getX() >= 0 && loc.getX() < size && loc.getY() >= 0 && loc.getY() < size;
		}
		
		/**
		 * Setzt Blätter auf Feld
		 * @param loc Das Feld
		 * @param leaves Anzahl der Blätter
		 */
		private void setLeaves(Location loc, int leaves) {
			if(!containsLocation(loc)) return;
			grid[loc.getX()][loc.getY()] = leaves;
		}
		
		/**
		 * Addiert Blätter auf Feld
		 * @param loc Das Feld
		 * @param leaves Anzahl der Blätter
		 */
		private void addLeaves(Location loc, int leaves) {
			setLeaves(loc, getLeaves(loc) + leaves);
		}
		
		/**
		 * Entfernt Blätter von Feld
		 * @param loc Das Feld
		 * @param leaves Anzahl der Blätter
		 */
		private void removeLeaves(Location loc, int leaves) {
			setLeaves(loc, getLeaves(loc) - leaves);
		}
		
		/**
		 * @param x X-Koordinate
		 * @param y Y-Koordinate
		 * @return Anzahl der Blätter auf Feld
		 */
		public int getLeaves(int x, int y) {
			return grid[x][y];
		}
		
		/**
		 * @param loc Das Feld
		 * @return Anzahl der Blätter auf Feld
		 */
		public int getLeaves(Location loc) {
			if(!containsLocation(loc)) return 0;
			return getLeaves(loc.getX(), loc.getY());
		}
		
		
		public static class SimulationClock extends Thread {
			
			// Grid und UIGrid
			Grid grid;
			UIGrid uiGrid;
			
			// Größe des Grids
			int size;
			
			public SimulationClock(Grid grid, UIGrid uiGrid) {
				this.grid = grid;
				this.uiGrid = uiGrid;
				this.size = grid.getSize();
			}
			
			@Override
			public void run() {
				// Bläst zuerst von oben auf 3-breites Mittelfeld herunter
				clearUpperRows();
				// Bläst unteres Laub auf Mittelfeld
				clearLowerRows();
				// Befreit linke Kante
				clearLeft();
				// Befreit rechte Kante
				clearRight();
				// Das Laub auf eine Linie bringen
				concentrateUpper();
				// Das Laub auf ein Feld blasen
				concentrateFinal();
			}
			
			/**
			 * Einen Blasevorgang simulieren
			 * @param x X-Koordinate
			 * @param y Y-Koordinate
			 * @param dir Richtung
			 */
			private void blow(int x, int y, Direction dir) {
				try {
					// Positioniert Bläser und Richtung
					grid.setBlower(x, y);
					grid.setDirection(dir);
					// Warten
					sleep(1000 / grid.getSimulationSpeed());
					// UI Aktualisieren
					uiGrid.repaint();
					// Blasen
					grid.blow();
					// Warten
					sleep(1000 / grid.getSimulationSpeed());
					// UI aktualisieren
					uiGrid.repaint();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			/**
			 * Obere Reihen auf Mittelfeld blasen
			 */
			private void clearUpperRows() {
				// Benutze (size - 2) / 2 mit ganzzahliger Division um bei gerade Größe ein Feld mehr zu bereinigen als
				// bei ungerader Größe
				int rowsToClear = (size - 2) / 2;
				for(int r = 0; r < rowsToClear; r++) {
					for(int x = 0; x < size; x++) {
						blow(x, r, Direction.DOWN);
					}
				}
			}
			
			/**
			 * Untere Reihen auf Mittelfeld blasen
			 */
			private void clearLowerRows() {
				// Benutze (size - 3) / 2 um bei gerade Größe gleich viele Felder zu bereinigen wie bei ungerader Größe
				int rowsToClear = (size - 3) / 2;
				for(int r = 0; r < rowsToClear; r++) {
					for(int x = 0; x < size; x++) {
						blow(x, size - r - 1, Direction.UP);
					}
				}
			}
			
			/**
			 * Linken Rand bereinigen
			 */
			private void clearLeft() {
				// Y berechnen
				int y = (size - 2) / 2 + 1;
				// Blasschritte durchführen
				blow(0, y, Direction.RIGHT);
				blow(0, y - 1, Direction.DOWN);
				blow(0, y, Direction.RIGHT);
				blow(0, y + 1, Direction.UP);
				blow(0, y, Direction.RIGHT);
			}
			
			/**
			 * Rechten Rand bereinigen
			 */
			private void clearRight() {
				// Y und X berechnen
				int y = (size - 2) / 2 + 1;
				int x = size - 1;
				// Blasschritte durchführen
				blow(x, y, Direction.LEFT);
				blow(x, y - 1, Direction.DOWN);
				blow(x, y, Direction.LEFT);
				blow(x, y + 1, Direction.UP);
				blow(x, y, Direction.LEFT);
			}
			
			/**
			 * So lange von einem Feld weiter weg von oben hinunter blasen, bis alle Blätter auf einer Linie sind
			 */
			private void concentrateUpper() {
				int y = size / 2 - 2;
				for(int i = 0; i < 2; i++) {
					for(int x = 1; x < size - 1; x++) {
						while(grid.getLeaves(x, y + 1 + i) > 0) {
							blow(x, y + i, Direction.DOWN);
						}
					}	
				}
			}
			
			/**
			 * So lange von einem Feld weiter weg von links nach rechts blasen, bis alle Blätter auf einem Feld sind
			 */
			private void concentrateFinal() {
				int y = size - (size - 3) / 2 - 1;
				for(int x = 1; grid.getLeaves(x, y) != grid.getSize() * grid.getSize() * 100; x++) {
					while(grid.getLeaves(x, y) != 0 && grid.getLeaves(x, y) != grid.getSize() * grid.getSize() * 100) {
						blow(x - 1, y, Direction.RIGHT);
					}
				}
			}
			
		}
		
	}
	
	public static class Location {
		
		int x;
		int y;
		
		public Location(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX() {
			return x;
		}
		
		public int getY() {
			return y;
		}
		
		public void setX(int x) {
			this.x = x;
		}
		
		public void setY(int y) {
			this.y = y;
		}
		
	}
	
	public static enum Direction {
		UP(0, -1, - 1, 0, 1, 0),
		DOWN(0, 1, 1, 0, -1, 0),
		LEFT(-1, 0, 0, 1, 0, -1),
		RIGHT(1, 0, 0, -1, 0, 1);

		// Transformationsmatrix für relatives Offset
		int behindXOff;
		int behindYOff;
		int leftXOff;
		int leftYOff;
		int rightXOff;
		int rightYOff;
		
		private Direction(int behindXOff, int behindYOff, int leftXOff, int leftYOff, int rightXOff, int rightYOff) {
			this.behindXOff = behindXOff;
			this.behindYOff = behindYOff;
			this.leftXOff = leftXOff;
			this.leftYOff = leftYOff;
			this.rightXOff = rightXOff;
			this.rightYOff = rightYOff;
		}
		
		/**
		 * @param loc Position
		 * @return Feld hinter Position, relativ durch Richtung
		 */
		public Location behindFrom(Location loc) {
			return new Location(loc.getX() + behindXOff, loc.getY() + behindYOff);
		}
		
		/**
		 * @param loc Position
		 * @return Feld links von Position, relativ durch Richtung
		 */
		public Location leftFrom(Location loc) {
			return new Location(loc.getX() + leftXOff, loc.getY() + leftYOff);
		}
		
		/**
		 * @param loc Position
		 * @return Feld rechts von Position, relativ durch Richtung
		 */
		public Location rightFrom(Location loc) {
			return new Location(loc.getX() + rightXOff, loc.getY() + rightYOff);
		}
		
		/**
		 * @return Umgekehrte Richtung
		 */
		public Direction invert() {
			switch(this) {
			case UP: return DOWN;
			case DOWN: return UP;
			case LEFT: return RIGHT;
			case RIGHT: return LEFT;
			}
			return UP;
		}
		
	}
	
	public static class UIGrid extends JPanel {
		
		// Größe des Grids in Pixel
		int size;
		// Grid Instanz
		Grid grid;
		// Pixel Größe eines Feldes
		int squareSize;
		
		// Offset
		private final int off = 50;
		
		public UIGrid(int size, Grid grid) {
			this.size = size;
			squareSize = size / grid.getSize();
			setSize(size + 2 * off, size + 2 * off);
			this.grid = grid;
			
			// Click Listener für Felder setzen
			addMouseListener(new MouseInputListener() {
				
				@Override
				public void mouseMoved(MouseEvent e) {}
				
				@Override
				public void mouseDragged(MouseEvent e) {}
				
				@Override
				public void mouseReleased(MouseEvent e) {}
				
				@Override
				public void mousePressed(MouseEvent e) {}
				
				@Override
				public void mouseExited(MouseEvent e) {}
				
				@Override
				public void mouseEntered(MouseEvent e) {}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					int x = (e.getX() - off) / squareSize;
					int y = (e.getY() - off) / squareSize;
					if(x >= grid.getSize() || y >= grid.getSize()) return;
					grid.setBlower(x, y);
					repaint();
				}
			});
		}
		
		@Override
		public void paintComponent(Graphics g) {
			// Gemalte Pixel löschen
			g.clearRect(0, 0, size + 2 * off, size + 2 * off);
			// gridSize setzen
			int gridSize = grid.getSize();

			// Großen Rahmen malen
			g.drawRect(off, off, size - 1, size - 1);
			// Einzelne Felder malen
			for(int x = 0; x < gridSize; x++) {
				for(int y = 0; y < gridSize; y++) {
					int lowerX = x * squareSize;
					int lowerY = y * squareSize;
					int upperX = (x + 1) * squareSize;
					int upperY = (y + 1) * squareSize;
					g.drawRect(lowerX + off, lowerY + off, squareSize, squareSize);
					
					// Anzahl der Blätter malen
					g.drawString(grid.getLeaves(x, y) + "", (lowerX + upperX) / 2 + off, (lowerY + upperY) / 2 + off);
				}
			}
			
			// Laubbläser malen
			Location blower = grid.getBlower();
			g.setColor(Color.RED);
			g.drawRect(blower.getX() * squareSize + off, blower.getY() * squareSize + off, squareSize, squareSize);
			// Hausmeister malen
			Location hm = grid.getDirection().invert().behindFrom(blower);
			g.drawString("HM", hm.getX() * squareSize + off, hm.getY() * squareSize + squareSize / 2 + off);
			
		}
		
	}
	
}
