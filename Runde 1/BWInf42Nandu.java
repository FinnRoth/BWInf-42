import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class BWInf42Nandu {
	
	/**
	 * Liest Eingabe-Datei ein und lässt eine Lösung dafür in die Konsole ausgeben.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String in = readFile("./src/nandu6.txt");
		Slot[][] construction = parseConstruction(in);
		printTable(construction);

	}

	/**
	 * Gibt alle möglichen Zustände einer Konstruktion in der Konsole aus
	 * 
	 * @param construction Die Konstruktion
	 */
	public static void printTable(Slot[][] construction) {

		// Liste der Input Slots
		List<Slot> inputSlots = new ArrayList<>();
		// Liste der Output Slots
		List<Slot> outputSlots = new ArrayList<>();

		// Iteration durch alle Slots zum Finden der In- und Outputslots und Einfügen dieser in den Listen
		for(int y = construction[0].length - 1; y >= 0; y--) {
			for(int x = 0; x < construction.length; x++) {
				Slot s = construction[x][y];
				Brick b = s.getBrick();
				if(b instanceof InputBrick)
					inputSlots.add(s);
				else if(b instanceof OutputBrick)
					outputSlots.add(s);
			}
		}

		// Erstellen und Ausgeben des Tabellenkopfes ('| Input 1 || Input 2 |-| Output 1 || Output 2|...')
		String header = "";
		for(int i = 0; i < inputSlots.size(); i++)
			header += "| Input " + (i + 1) + "  |";
		header += "-";
		for(int i = 0; i < outputSlots.size(); i++)
			header += "| Output " + (i + 1) + " |";
		System.out.println(header);
		for(int i = 0; i < header.length(); i++)
			System.out.print("-");
		System.out.println("");

		// Iteration durch alle möglichen Zustände mithilfe von Zählen mit Binärzahlen (bin)
		for(int i = 0; i < Math.pow(2, inputSlots.size()); i++) {
			String bin = String.format("%0" + inputSlots.size() + "d", Integer.parseInt(Integer.toBinaryString(i)));

			String line = "";

			// Setzen und Ausgeben des Wertes der Inputsteine
			for(int j = 0; j < inputSlots.size(); j++) {
				InputBrick b = (InputBrick) inputSlots.get(j).getBrick();
				boolean val = bin.charAt(j) == '1';
				b.setVal(val);
				line += "| " + (val ? "on       |" : "off      |");
			}
			line += "-";
			// Berechnen und Ausgeben des Wertes der Outputsteine
			for(int j = 0; j < outputSlots.size(); j++) {
				boolean val = outputSlots.get(j).compute();
				line += "| " + (val ? "on       |" : "off      |");
			}

			System.out.println(line);

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
	 * Formatiert den Inhalt einer eingelesen Datei in ein zweidimensionales Array aus Slots.
	 * 
	 * @param in Der Inhalt der eingegebenen Datei
	 * @return Ein zweidimensionales Array aus Slots aus dem Format des eingegebenen Textes
	 */
	public static Slot[][] parseConstruction(String in) {

		// in in seine Zeilen aufteilen
		String[] lines = in.split("\n");

		// Breite und Höhe des Labyrinths auslesen
		int width, height;

		try {
			String[] dimensions = lines[0].split(" ");
			width = Integer.parseInt(dimensions[0]);
			height = Integer.parseInt(dimensions[1]);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("The file doesn't match the valid format!");
		}

		// Slot[][] mit entsprechenden Dimensionen erstellen
		Slot[][] construction = new Slot[width][height];

		// y rückwärts laufen lassen
		int y = height - 1;
		// Schleife durch alle Zeilen
		for(int l = 1; l < lines.length; l++) {
			String line = lines[l];
			// Alle mehrfachen Leerzeichen durch ein einfaches Leerzeichen ersetzen
			line = line.replace("  ", " ");
			// Zeile nach Leerzeichen in Bausteine aufspalten
			String[] bricks = line.split(" ");
			// Durch Bausteine durchiterieren
			for(int x = 0; x < width; x++) {
				try {
					String brick = bricks[x];
					// s, above1 und above2 standardmäßig als Einzelslot mit immer negativer Ausgabe setzen
					Slot s = new Slot(new InputBrick(false), 0), above1 = s, above2 = s;
					// b erstellen
					Brick b = null;
					// Wenn die Iteration tiefere y-Werte erreicht, sodass darüber ein Slot liegt, so werden
					// above1 und above2 auf diee darüberliegenden Slots gesetzt
					if(y < height - 1) {
						above1 = construction[x][y + 1];
						if(x < width - 1)
							above2 = construction[x + 1][y + 1];
					}
					// String überprüfen und entsprechende Brick-Instanz erstellen
					boolean doubleBrick = false;
					if(brick.equals("X")) {
						b = new EmptyBrick();
					} else if(brick.equals("W")) {
						b = new WhiteBrick(above1, above2);
						doubleBrick = true;
					} else if(brick.equals("R")) {
						b = new RedBrick(above1);
						doubleBrick = true;
					} else if(brick.equals("r")) {
						b = new RedBrick(above2);
						doubleBrick = true;
					} else if(brick.equals("B")) {
						b = new BlueBrick(above1, above2);
						doubleBrick = true;
					} else if(brick.startsWith("Q")) {
						b = new InputBrick(false);
					} else if(brick.startsWith("L")) {
						b = new OutputBrick(above1);
					} else {
						throw new IllegalStateException("The file doesn't match the valid format!");
					}
					// Wenn es zwei Slots breiter Stein ist, wird da nebenan liegende Slot mit derselben Brick-Instanz,
					// aber im zweiten 'slot' gefüllt und dieser Slot beim Einlesen übersprungen
					if(doubleBrick) {
						construction[x][y] = new Slot(b, 0);
						construction[x + 1][y] = new Slot(b, 1);
						x++;
					} else {
						construction[x][y] = new Slot(b, 0);
					}
				} catch(IndexOutOfBoundsException e) {
					throw new IllegalStateException("The file doesn't match the valid format!");
				}
			}
			y--;
		}

		return construction;

	}

	public static class Slot {

		Brick brick;
		int slot;

		public Slot(Brick brick, int slot) {
			this.brick = brick;
			this.slot = slot;
		}

		/**
		 * @return Die Ausgabe des Bausteins an dieser Stelle zu bestehenden Bedingungen
		 */
		public boolean compute() {
			return brick.compute()[slot];
		}

		/**
		 * @return Den Brick
		 */
		public Brick getBrick() {
			return brick;
		}

		/**
		 * @return Den Slot
		 */
		public int getSlot() {
			return slot;
		}

	}

	/**
	 * Abstrakte Klasse Brick, welche von den verschieden farbigen Baustein mit unterschiedlicher
	 * Funktion implementiert wird
	 */
	public static abstract class Brick {

		public abstract boolean[] compute();

	}

	public static class WhiteBrick extends Brick {

		Slot in1, in2;

		public WhiteBrick(Slot in1, Slot in2) {
			this.in1 = in1;
			this.in2 = in2;
		}

		/**
		 * @return Die Outputs eines weißen Bausteins
		 */
		public boolean[] compute() {
			boolean res = !(in1.compute() && in2.compute());
			return new boolean[] { res, res };
		}

	}

	public static class RedBrick extends Brick {

		Slot in1;

		public RedBrick(Slot in1) {
			this.in1 = in1;
		}
		
		/**
		 * @return Die Outputs eines roten Bausteins
		 */
		public boolean[] compute() {
			boolean res = !in1.compute();
			return new boolean[] { res, res };
		}

	}

	public static class BlueBrick extends Brick {

		Slot in1, in2;

		public BlueBrick(Slot in1, Slot in2) {
			this.in1 = in1;
			this.in2 = in2;
		}

		/**
		 * @return Die Outputs eines blauen Bausteins
		 */
		public boolean[] compute() {
			return new boolean[] { in1.compute(), in2.compute() };
		}

	}

	public static class InputBrick extends Brick {

		boolean val;

		public InputBrick(boolean val) {
			this.val = val;
		}

		public void setVal(boolean val) {
			this.val = val;
		}

		/**
		 * @return Das Output einer Taschenlampe
		 */
		public boolean[] compute() {
			return new boolean[] { val };
		}

	}

	public static class EmptyBrick extends Brick {

		/**
		 * @return False
		 */
		public boolean[] compute() {
			return new boolean[] { false };
		}

	}

	public static class OutputBrick extends Brick {

		Slot in1;

		public OutputBrick(Slot in1) {
			this.in1 = in1;
		}

		/**
		 * @return Das Output eines Outputbricks
		 */
		public boolean[] compute() {
			return new boolean[] { in1.compute() };
		}

	}

}
