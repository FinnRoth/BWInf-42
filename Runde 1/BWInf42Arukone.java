import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

class BWInf42Arukone {

	/**
	 * Erstellt ein Arukone Rätsel mit gegebener Größe
	 */
	public static void main(String[] args) {

		int size = 6;

		// Erstellt das Rätsel
		ArukoneCreator creator = new ArukoneCreator(size);
		creator.create();
		// Erhält Array mit den Anfangs- und Endknoten des Rätsels
		int[][] arukone = creator.noPaths();

		// Gibt das Rätsel in der Konsole aus
		for(int y = size - 1; y >= 0; y--) {
			for(int x = 0; x < size; x++) {
				System.out.print(arukone[x][y] + " ");
			}
			System.out.println();
		}

		// Erstellt Datei mit Arukone Rätsel
		createFile(creator);

	}

	/**
	 * Erstellt Datei mit angegebenem Arkone Rätsel
	 * @param creator Die ArukoneCreator-Instanz des Rätsels
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
		 * Erstellt das Arukone Rätsel mit gegebener Größe
		 */
		public void create() {

			// Erstellt das Array
			arukone = new int[size][size];

			// Rechnet Mindestanzahl an Paaren aus
			int pairs = size / 2 + 1;

			// Fügt für das Beispielprogramm unlösbare Instanz in das Rätsel ein
			paths = new HashMap<>();
			// Im Array mit Lösungsweg, um die besuchten Koordinaten zu speichern
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

			// Fügt nebeneinander liegende Paare ein, bis Mindestanzahl an Paaren erfüllt ist
			for(int i = 3; i < pairs + 1; i++) {
				Location[] rand = randFreeNeighbors();
				arukone[rand[0].x][rand[0].y] = i;
				arukone[rand[1].x][rand[1].y] = i;
				paths.put(i, rand);
			}

		}
		
		/**
		 * @return Das Array mit den Paaren ohne die Lösung
		 */
		public int[][] noPaths() {
			
			// Erstellt Kopie
			int[][] noPaths = new int[size][size];
			
			// Fügt Anfangs- und Endpositionen in Kopie ein
			for(int i : paths.keySet()) {
				Location l1 = paths.get(i)[0];
				Location l2 = paths.get(i)[1];
				noPaths[l1.x][l1.y] = i;
				noPaths[l2.x][l2.y] = i;
			}
			
			return noPaths;
			
		}
		
		/**
		 * @return Zwei direkt nebeneinander liegende, freie Positionen im Rätsel
		 */
		public Location[] randFreeNeighbors() {
			// Zufällige Position
			Location loc = new Location(rand(size), rand(size));
			// Neues zufälliges Paar, wenn diese Position vergeben ist
			if(arukone[loc.x][loc.y] != 0)
				return randFreeNeighbors();
			// Zufälliger, freier Nachbar
			Location neighbor = randLocFrom(loc);
			
			return new Location[] {loc, neighbor};
		}

		/**
		 * @param from Die Ausgangsposition
		 * @return Eine zufällige, neben {@link from} liegende, freie Position
		 */
		public Location randLocFrom(Location from) {
			// Zufällige Richtung
			int dir = rand(4);
			int x = from.x;
			int y = from.y;
			// Modifizieren von x/y gemä´der zufällig gewählten Richtung
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
		 * @return Zufällige Zahl zwischen 0 und {@link bound}
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
