import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

class BWInf42Arukone {

	/**
	 * Erstellt ein Arukone R�tsel mit gegebener Gr��e
	 */
	public static void main(String[] args) {

		int size = 6;

		// Erstellt das R�tsel
		ArukoneCreator creator = new ArukoneCreator(size);
		creator.create();
		// Erh�lt Array mit den Anfangs- und Endknoten des R�tsels
		int[][] arukone = creator.noPaths();

		// Gibt das R�tsel in der Konsole aus
		for(int y = size - 1; y >= 0; y--) {
			for(int x = 0; x < size; x++) {
				System.out.print(arukone[x][y] + " ");
			}
			System.out.println();
		}

		// Erstellt Datei mit Arukone R�tsel
		createFile(creator);

	}

	/**
	 * Erstellt Datei mit angegebenem Arkone R�tsel
	 * @param creator Die ArukoneCreator-Instanz des R�tsels
	 */
	public static void createFile(ArukoneCreator creator) {

		try {
			File file = new File("./arukone.txt");
			file.createNewFile();
			int[][] arukone = creator.noPaths();
			String c = creator.size + "\n" + creator.paths.size() + "\n";
			for(int y = creator.size - 1; y >= 0; y--) {
				for(int x = 0; x < creator.size; x++) {
					c += arukone[x][y] + " ";
				}
				c += "\n";
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(c);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class ArukoneCreator {

		int size;
		int[][] arukone;
		Map<Integer, Location[]> paths;

		public ArukoneCreator(int size) {
			this.size = size;
		}

		/**
		 * Erstellt das Arukone R�tsel mit gegebener Gr��e
		 */
		public void create() {

			// Erstellt das Array
			arukone = new int[size][size];

			// Rechnet Mindestanzahl an Paaren aus
			int pairs = size / 2 + 1;

			// F�gt f�r das Beispielprogramm unl�sbare Instanz in das R�tsel ein
			paths = new HashMap<>();
			// Im Array mit L�sungsweg, um die besuchten Koordinaten zu speichern
			arukone[0][1] = 1;
			arukone[0][2] = 1;
			arukone[0][3] = 1;
			arukone[1][3] = 1;
			arukone[2][3] = 1;
			arukone[2][2] = 1;
			arukone[2][1] = 1;
			arukone[2][0] = 1;
			// In der Map, um Anfang und Endposition der entsprechenden Zahl zuzuordnen
			paths.put(1, new Location[] {new Location(0, 1), new Location(2, 0)});
			arukone[0][0] = 2;
			arukone[1][0] = 2;
			arukone[1][1] = 2;
			arukone[1][2] = 2;
			paths.put(2, new Location[] {new Location(0, 0), new Location(1, 2)});

			// F�gt nebeneinander liegende Paare ein, bis Mindestanzahl an Paaren erf�llt ist
			for(int i = 3; i < pairs + 1; i++) {
				Location[] rand = randFreeNeighbors();
				arukone[rand[0].x][rand[0].y] = i;
				arukone[rand[1].x][rand[1].y] = i;
				paths.put(i, rand);
			}

		}
		
		/**
		 * @return Das Array mit den Paaren ohne die L�sung
		 */
		public int[][] noPaths() {
			
			// Erstellt Kopie
			int[][] noPaths = new int[size][size];
			
			// F�gt Anfangs- und Endpositionen in Kopie ein
			for(int i : paths.keySet()) {
				Location l1 = paths.get(i)[0];
				Location l2 = paths.get(i)[1];
				noPaths[l1.x][l1.y] = i;
				noPaths[l2.x][l2.y] = i;
			}
			
			return noPaths;
			
		}
		
		/**
		 * @return Zwei direkt nebeneinander liegende, freie Positionen im R�tsel
		 */
		public Location[] randFreeNeighbors() {
			// Zuf�llige Position
			Location loc = new Location(rand(size), rand(size));
			// Neues zuf�lliges Paar, wenn diese Position vergeben ist
			if(arukone[loc.x][loc.y] != 0)
				return randFreeNeighbors();
			// Zuf�lliger, freier Nachbar
			Location neighbor = randLocFrom(loc);
			
			return new Location[] {loc, neighbor};
		}

		/**
		 * @param from Die Ausgangsposition
		 * @return Eine zuf�llige, neben {@link from} liegende, freie Position
		 */
		public Location randLocFrom(Location from) {
			// Zuf�llige Richtung
			int dir = rand(4);
			int x = from.x;
			int y = from.y;
			// Modifizieren von x/y gem�der zuf�llig gew�hlten Richtung
			if(dir == 0)
				x++;
			else if(dir == 1)
				x--;
			else if(dir == 2)
				y++;
			else if(dir == 3)
				y--;
			// Neuberechnung der Position, falls sie nicht frei ist oder nicht im Spielfeld liegt
			if(x >= size || x < 0 || y >= size || y < 0 || arukone[x][y] != 0)
				return randLocFrom(from);
			
			return new Location(x, y);
		}

		/**
		 * @param bound Maximum
		 * @return Zuf�llige Zahl zwischen 0 und {@link bound}
		 */
		private int rand(int bound) {
			return ThreadLocalRandom.current().nextInt(bound);
		}

	}

	public static class Location {

		int x;
		int y;

		public Location(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return "Loc(" + x + ", " + y + ")";
		}

	}

}
