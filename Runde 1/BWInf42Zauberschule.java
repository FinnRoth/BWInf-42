import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class BWInf42Zauberschule {

	/**
	 * Liest Eingabe-Datei ein und lässt eine Lösung dafür in die Konsole ausgeben.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Datei einlesen
		String in = readFile("./src/zauberschule0.txt");

		// Datei in Unit[][][] umwandeln
		Unit[][][] blueprint = parseBlueprint(in);
		
		// Das Programm bei Fehlern in der Umwandlung abbrechen
		if(blueprint == null) return;
		
		// Den schnellsten Weg zur Lösung finden
		Way way = new Searcher(blueprint).search();
		// Den Weg in der Konsole ausgeben
		printSolution(blueprint, way);
		System.out.println("Time needed " + way.getTime() + "s");

	}
	
	/**
	 * Gibt einen Weg durch das Labyrinth in der Konsole aus.
	 * 
	 * @param blueprint Das Labyrinth als char[][][]
	 * @param way Die Way-Instanz, die zur Lösung führt
	 */
	public static void printSolution(Unit[][][] blueprint, Way way) {

		// Neuen Weg mit gleicher Startposition anlegen
		Way go = new Way(way.searcher.getStart());
		
		// Neues char[][][] mit den gleichen Dimensionen wie blueprint anlegen
		char[][][] chars = new char[blueprint.length][blueprint[0].length][blueprint[0][0].length];
		
		// Den Inhalt von blueprint in chars übertragen. Dabei ist PATH='.', WALL='#', ...
		for(int x = 0; x < blueprint.length; x++)
			for(int y = 0; y < blueprint[0].length; y++)
				for(int z = 0; z < blueprint[0][0].length; z++)
					chars[x][y][z] = blueprint[x][y][z].getChar();
		
		// Für jeden Turn im originalen Weg mithilfe des Weges go die neue Position berechnen und den dementsprechenden
		// Pfeil (< > v ^) in chars an dieser Position eintragen
		for(Turn turn : way.getPath()) {
			Location l = go.getCurrentLocation();
			go = go.turn(turn);
			char c;
			switch(turn) {
			case LEFT:
				c = '<';
				break;
			case RIGHT:
				c = '>';
				break;
			case FORWARD:
				c = '^';
				break;
			case BACKWARD:
				c = 'v';
				break;
			case UP: case DOWN:
				c = '!';
				break;

				default:
					c = '.';
					break;
			}
			if(blueprint[l.x][l.y][l.z] != Unit.START) chars[l.x][l.y][l.z] = c;
		}
		
		// Ausgabe von chars
		for(int z = 0; z < 2; z++) {
			for(int y = chars[0].length - 1; y >= 0; y--) {
				for(int x = 0; x < chars.length; x++) {
					System.out.print(chars[x][y][z]);
				}
				System.out.println();
			}
			System.out.println();
		}

	}
	
	/**
	 * Liest eine Datei ein und gibt den Inhalt als String zurück
	 * 
	 * @param path Der Pfad der einzulesenden Datei
	 * @return Den Inhalt der Datei als String
	 */
	public static String readFile(String path) {
		try {
			File file = new File(path);
			Scanner scan = new Scanner(file);
			String content = "";
			while(scan.hasNextLine()) {
				content += scan.nextLine() + "\n";
			}
			scan.close();
			return content;
		} catch(FileNotFoundException e) {
			System.err.println(path + " was not found!");
			System.exit(0);
			return null;
		}
	}

	/**
	 * Formatiert den Inhalt einer eingelesen Datei in ein dreidimensionales Array aus Units.
	 * 
	 * @param in Der Inhalt der eingegebenen Datei
	 * @return Ein dreidimensionales Array aus Units aus dem Format des eingegebenen Textes
	 */
	public static Unit[][][] parseBlueprint(String in) {

		// in in seine Zeilen aufteilen
		String[] lines = in.split("\n");

		// Breite und Höhe des Labyrinths auslesen
		int width, height;

		try {
			String[] dimensions = lines[0].split(" ");
			height = Integer.parseInt(dimensions[0]);
			width = Integer.parseInt(dimensions[1]);
		} catch(NumberFormatException e) {
			throw new IllegalStateException("The file doesn't match the valid format!");
		}

		// Unit[][][] mit entsprechenden Dimensionen erstellen
		Unit[][][] blueprint = new Unit[width][height][2];

		// Prüfvariablen dafür, ob 'A' und 'B' genau einmal in der Datei angegeben sind
		boolean hasStart = false;
		boolean hasGoal = false;

		// y rückwärts laufen lassen
		int y = height - 1;
		// z = 0 setzen
		int z = 0;
		// Schleife durch alle Zeilen
		for(int l = 1; l < lines.length; l++) {
			String line = lines[l];
			// z bei Leerzeile auf 1 setzen und y zurücksetzen
			if(line.equals("")) {
				z = 1;
				y = height - 1;
				continue;
			}
			// Bei Zeichenlänge, welche der angegebenen Breite widerspricht, wird ein Fehler geworfen
			if(line.length() != width)
				throw new IllegalStateException("The file doesn't match the valid format!");
			
			// Schleife durch die Zeichen der Zeile, welche das Zeichen überprüft und die dementsprechende
			// Unit in blueprint an der Stelle x, y, z einträgt
			for(int x = 0; x < width; x++) {
				char c = line.charAt(x);
				Unit u;
				switch(c) {
				case '#':
					u = Unit.WALL;
					break;
				case '.':
					u = Unit.PATH;
					break;
				case 'A':
					if(hasStart) {
						System.err.println("Can only handle one start (A)!");
						return null;
					}
					hasStart = true;
					u = Unit.START;
					break;
				case 'B':
					if(hasGoal) {
						System.err.println("Can only handle one goal (B)!");
						return null;
					}
					hasGoal = true;
					u = Unit.GOAL;
					break;

				default:
					throw new IllegalStateException("The file doesn't match the valid format!");
				}
				blueprint[x][y][z] = u;
			}
			y--;
		}
		
		if(!hasStart || !hasGoal) {
			System.err.println("Need at least one start (A) and one goal (B)");
			return null;
		}

		return blueprint;

	}

	/**
	 * Die Searcher-Klasse kann aus einem Labyrinth in Form eines Unit[][][] die schnellste Lösung finden.
	 */
	public static class Searcher {

		Unit[][][] blueprint;
		Location start;

		List<Location> visited;
		List<Way> ways;

		public Searcher(Unit[][][] blueprint) {
			this.blueprint = blueprint;
		}

		/**
		 * Gibt die Location des Startpunktes ('A') zurück.
		 * 
		 * @return Die Location des Startpunktes ('A')
		 */
		public Location findStart() {
			for(int z = 0; z < blueprint[0][0].length; z++) {
				for(int y = 0; y < blueprint[0].length; y++) {
					for(int x = 0; x < blueprint.length; x++) {
						if(blueprint[x][y][z] == Unit.START)
							return new Location(x, y, z);
					}
				}
			}
			throw new IllegalStateException("No start point found!");
		}
		
		/**
		 * Findet durch ausprobieren jedes mögliches Weges die schnellste Lösung durch das Labyrinth.
		 * 
		 * @return Den schnellsten Way durch das Labyrinth
		 */
		public Way search() {
			
			// Finden der Startposition
			start = findStart();
			// Zurücksetzen der Liste der besuchten Positionen
			visited = new ArrayList<>();
			// Die Startposition zur Liste der besuchten Positionen hinzufügen
			visited.add(start);

			// Liste der aktiven Ways zurücksetzen
			ways = new ArrayList<>();
			// Neuen Way zur Liste der aktiven Ways hinzufügen
			ways.add(new Way(this));

			// Solange der Letzte unter den aktiven Ways sein Ziel nicht erreicht hat
			while(!ways.get(ways.size() - 1).hasReachedGoal()) {
				// Liste der aktiven Ways kopieren
				List<Way> copy = new ArrayList<>();
				for(Way way : ways)
					copy.add(way);
				// Für jeden der kopierten Ways Zeit erhöhen, wait verringern und in jede mögliche Richtung gehen
				for(Way way : copy) {
					way.addTime();
					if(way.getWait() > 0) {
						way.removeWait();
						continue;
					}
					if(way.turnAll())
						break;
				}
			}
			// Restwartezeit zur vom Weg benötigten Zeit hinzufügen
			Way rightWay = ways.get(ways.size() - 1);
			rightWay.addTime(rightWay.getWait());
			
			return rightWay;

		}

		/**
		 * @param location Die Location
		 * @return Ob der Searcher die {@link location} schon besucht hat
		 */
		public boolean hasVisited(Location location) {
			return visited.contains(location);
		}
		
		/**
		 * @param x Die x-Koordinate
		 * @param y Die y-Koordinate
		 * @param z Die z-Koordinate
		 * @return Ob der Searcher die Location an {@link x} {@link y} {@link z} schon besucht hat
		 */
		public boolean hasVisited(int x, int y, int z) {
			return hasVisited(new Location(x, y, z));
		}

		/**
		 * Trägt die {@link location} in die Liste der besuchten Locations ein
		 * @param location Die Location
		 */
		public void visit(Location location) {
			visited.add(location);
		}
		
		/**
		 * @return Das Labyrinth, in dem der Searcher tätig ist, als Unit[][][]
		 */
		public Unit[][][] getBlueprint() {
			return blueprint;
		}
		
		/**
		 * Fügt einen Way zu der Liste der aktiven Ways hinzu
		 * @param way Der Way
		 */
		public void addWay(Way way) {
			ways.add(way);
		}

		/**
		 * @return Die Start-Location des Labyrinths
		 */
		public Location getStart() {
			return start;
		}

	}
	
	/**
	 * Behält als Klasse den Überblick über die Turns und die Zeit, die ein Weg zurückgelegt hat.
	 * Kann außerdem weitere Turns hinzufügen.
	 */
	public static class Way {

		List<Turn> path;
		Location current;
		Searcher searcher;
		int time;
		int wait;

		public Way(Location start) {
			this(start, new ArrayList<>(), 0, 0);
		}

		public Way(Searcher searcher) {
			this(searcher, new ArrayList<>(), 0, 0);
		}

		public Way(Searcher searcher, List<Turn> path, int time, int wait) {
			this.searcher = searcher;
			this.path = path;
			this.time = time;
			this.wait = wait;
			this.current = searcher.getStart();
		}

		public Way(Location start, List<Turn> path, int time, int wait) {
			this.path = path;
			this.time = time;
			this.wait = wait;
			this.current = start;
		}

		/**
		 * Führt einen Turn aus und berechnet die neue aktuelle Position nach befolgen aller im Weg
		 * enthaltenen Turns. Dabei wird nur eine Kopie modifiziert und zurückgegeben.
		 * 
		 * @param turn Der Turn
		 * @return Eine Kopie von sich selbst, nachdem der Turn ausgeführt wurde.
		 */
		public Way turn(Turn turn) {
			// Kopie von sich selbst erstellen
			Way way = null;
			if(searcher != null) way = new Way(searcher, getPath(), time, wait);
			else way = new Way(current, getPath(), time, wait);
			// Den Turn zur Kopie hinzfügen
			way.addTurn(turn);
			// Die jetztige Position in newLoc klonen
			Location newLoc = current.clone();
			int x = newLoc.getX();
			int y = newLoc.getY();
			int z = newLoc.getZ();
			// newLoc in Abhängigkeit vom Turn modifizieren
			// Bei UP und DOWN 2 weitere Iterationen aussetzen
			switch(turn) {
			case LEFT:
				newLoc = new Location(x - 1, y, z);
				break;
			case RIGHT:
				newLoc = new Location(x + 1, y, z);
				break;
			case FORWARD:
				newLoc = new Location(x, y + 1, z);
				break;
			case BACKWARD:
				newLoc = new Location(x, y - 1, z);
				break;
			case UP:
				way.wait(2);
				newLoc = new Location(x, y, z + 1);
				break;
			case DOWN:
				way.wait(2);
				newLoc = new Location(x, y, z - 1);
				break;
			}
			// Return null, wenn die neue Position bereits besucht wurde
			if(searcher != null && searcher.hasVisited(newLoc))
				return null;
			// newLoc als neue aktuelle Position der Kopie setzen
			way.setCurrentLocation(newLoc);
			// newLoc besuchen
			if(searcher != null) searcher.visit(newLoc);
			
			return way;
		}
		
		/**
		 * Fügt einen Turn zur Liste der Turns hinzu.
		 * @param turn Der Turn
		 */
		public void addTurn(Turn turn) {
			path.add(turn);
		}

		/**
		 * @return Ob durch befolgen der im Weg enthaltenen Turns das Ziel im Labyrinth erreicht wird
		 */
		public boolean hasReachedGoal() {
			return searcher != null && searcher.getBlueprint()[current.getX()][current.getY()][current.getZ()] == Unit.GOAL;
		}

		/**
		 * @return Die aktuelle Position im Labyrinth nach befolgen der im Weg enthaltenen Turns
		 */
		public Location getCurrentLocation() {
			return current;
		}
		
		/**
		 * Setzt die aktuelle Position im Labyrinth nach befolgen der im Weg enthaltenen Turns
		 */
		public void setCurrentLocation(Location current) {
			this.current = current;
		}

		/**
		 * Führt einen Turn in alle möglichen Richtungen gleichzeitig durch. Dabei wird je nur eine Kopie modifiziert.
		 * 
		 * @return Ob durch einen der durchgeführten Turns das Ziel erreicht wurde
		 */
		public boolean turnAll() {

			if(searcher == null) return false;

			// Durch alle möglichen Turns iterieren
			for(Turn turn : getPossibleTurns()) {
				// Den Turn bei einer Kopie durchführen
				Way newWay = turn(turn);
				// Überspringen, falls newWay = null
				if(newWay == null)
					continue;
				// Sich selbst aus der Liste der aktiven Ways entfernen, da von hier aus weitergegangen werden kann
				if(searcher.ways.contains(this))
					searcher.ways.remove(this);
				// Den neuen Weg zur Liste der aktiven Ways hinzufügen
				searcher.ways.add(newWay);
				// Bei Erreichen des Ziel true zurückgeben
				if(newWay.hasReachedGoal())
					return true;
			}

			return false;

		}

		/**
		 * @return Eine Liste der möglichen Turns, bei dem in keine Wand gelaufen wird und die noch nicht vom
		 * Searcher besucht wurden
		 */
		public List<Turn> getPossibleTurns() {
			Unit[][][] blueprint = searcher.getBlueprint();
			List<Turn> possibleTurns = new ArrayList<>();
			List<Unit> possibleUnits = Arrays.asList(Unit.PATH, Unit.GOAL);
			int x = current.getX();
			int y = current.getY();
			int z = current.getZ();
			if(possibleUnits.contains(blueprint[x - 1][y][z]) && !searcher.hasVisited(x - 1, y, z))
				possibleTurns.add(Turn.LEFT);
			if(possibleUnits.contains(blueprint[x + 1][y][z]) && !searcher.hasVisited(x + 1, y, z))
				possibleTurns.add(Turn.RIGHT);
			if(possibleUnits.contains(blueprint[x][y - 1][z]) && !searcher.hasVisited(x, y - 1, z))
				possibleTurns.add(Turn.BACKWARD);
			if(possibleUnits.contains(blueprint[x][y + 1][z]) && !searcher.hasVisited(x, y + 1, z))
				possibleTurns.add(Turn.FORWARD);
			if(z - 1 >= 0 && possibleUnits.contains(blueprint[x][y][z - 1]) && !searcher.hasVisited(x, y, z - 1))
				possibleTurns.add(Turn.DOWN);
			if(z + 1 <= 1 && possibleUnits.contains(blueprint[x][y][z + 1]) && !searcher.hasVisited(x, y, z + 1))
				possibleTurns.add(Turn.UP);
			return possibleTurns;
		}

		/**
		 * @return Eine Kopie der Liste der im Weg enthaltenen Turns
		 */
		public List<Turn> getPath() {
			List<Turn> path = new ArrayList<>();
			for(Turn turn : this.path)
				path.add(turn);
			return path;
		}

		
		/**
		 * @return Die vom Weg benötigte Zeit
		 */
		public int getTime() {
			return time;
		}
		
		/**
		 * Addiert {@link time} zum counter für die vom Weg benötigte Zeit.
		 * 
		 * @param time Die zu addierende Zeit
		 * @return Die neue Zeit
		 */
		public int addTime(int time) {
			this.time += time;
			return this.time;
		}

		/**
		 * Addiert 1 zum counter für die vom Weg benötigte Zeit.
		 */
		public void addTime() {
			time++;
		}

		/**
		 * @return Die Zeit, die noch gewartet werden muss, bevor der Weg weitergeführt werden kann
		 */
		public int getWait() {
			return wait;
		}

		/**
		 * Addiert {@link wait} zum counter für die Zeit, die noch gewartet werden muss, bevor der Weg
		 * weitergeführt werden kann.
		 * 
		 * @param wait Die zu addierende Zeit
		 */
		public void wait(int wait) {
			this.wait += wait;
		}
		
		/**
		 * Entfernt 1 vom counter für die Zeit, die noch gewartet werden muss, bevor der Weg weitergeführt werden kann.
		 */
		public void removeWait() {
			wait--;
		}

	}

	public static class Location {

		int x;
		int y;
		int z;

		public Location(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getZ() {
			return z;
		}

		public void setX(int x) {
			this.x = x;
		}

		public void setY(int y) {
			this.y = y;
		}

		public void setZ(int z) {
			this.z = z;
		}

		/**
		 * Vergleicht, ob eine andere Location die gleichen Koordinaten hat.
		 * 
		 * @return True, wenn die andere Location die gleichen Koordinaten hat
		 */
		public boolean equals(Object other) {
			if(!(other instanceof Location))
				return false;
			Location otherLoc = (Location) other;
			return otherLoc.x == x && otherLoc.y == y && otherLoc.z == z;
		}

		/**
		 * @return Eine Kopie der Location
		 */
		public Location clone() {
			return new Location(x, y, z);
		}

	}

	public static enum Unit {
		
		PATH('.'), WALL('#'), START('A'), GOAL('B');
		
		char c;
		
		private Unit(char c) {
			this.c = c;
		}
		
		public char getChar() {
			return c;
		}
	}

	public static enum Turn {
		LEFT, RIGHT, FORWARD, BACKWARD, UP, DOWN;
	}

}
