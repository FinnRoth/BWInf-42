import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BWInf42Paeckchen {
	
	/**
	 * Liest Eingabe-Datei ein und l�sst eine L�sung daf�r in die Konsole ausgeben.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Datei einlesen
		String content = readFile("src/paeckchen8.txt");
		
		// Datei formatieren
		Setting setting = parseSetting(content);
		
		DistributionResult res = setting.fillBoxes();
		System.out.println(res);
		
	}
	
	/**
	 * Formatiert den Inhalt der eingelesenen Datei.
	 * 
	 * @param in Der Inhalt der eingegebenen Datei
	 */
	public static Setting parseSetting(String in) {
		
		// In seine Zeilen aufteilen
		String[] lines = in.split("\n");

		// Anzahl Sorten und Stilrichtungen auslesen
		int types, styles;

		try {
			String[] typesStiles = lines[0].split(" ");
			types = Integer.parseInt(typesStiles[0]);
			styles = Integer.parseInt(typesStiles[1]);
		} catch(NumberFormatException e) {
			throw new IllegalStateException("The file doesn't match the valid format!");
		}
		
		// Kombinierbare Stilrichtungen einlesen
		List<Integer[]> combinables = new ArrayList<>();
		
		int i = 1;
		for(; i < lines.length; i++) {
			String line = lines[i];
			if(line.equals("")) break;
			int style1, style2;
			try {
				String[] stylesLine = line.split(" ");
				style1 = Integer.parseInt(stylesLine[0]);
				style2 = Integer.parseInt(stylesLine[1]);
			} catch(NumberFormatException e) {
				throw new IllegalStateException("The file doesn't match the valid format!");
			}
			combinables.add(new Integer[] {style1, style2});
		}
		
		// Verf�gbare Kleidungsst�cke einlesen
		Map<Clothing, Integer> stock = new LinkedHashMap<>();
		
		for(i++; i < lines.length; i++) {
			String line = lines[i];
			int type, style, amount;
			try {
				String[] amountLine = line.split(" ");
				type = Integer.parseInt(amountLine[0]);
				style = Integer.parseInt(amountLine[1]);
				amount = Integer.parseInt(amountLine[2]);
			} catch(NumberFormatException e) {
				throw new IllegalStateException("The file doesn't match the valid format!");
			}
			stock.put(new Clothing(type, style), amount);
		}
		
		Setting setting = new Setting(types, styles, combinables, stock);
		
		return setting;
		
	}
	
	/**
	 * Liest eine Datei ein und gibt den Inhalt als String zur�ck.
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
	
	
	public static class Setting {
		
		// Anzahl der Sorten in der Umgebung
		int types;
		// Anzahl der Stilrichtungen in der Umgebung
		int styles;
		// Liste von kombinierbaren Kleidungsst�cken
		List<Integer[]> combinables;
		// Bestand an verf�gbaren Kleidungsst�cken
		Map<Clothing, Integer> stock;
		
		
		public Setting(int types, int styles, List<Integer[]> combinables, Map<Clothing, Integer> stock) {
			this.types = types;
			this.styles = styles;
			this.combinables = combinables;
			this.stock = stock;
			for(Clothing clothing : new LinkedHashSet<>(stock.keySet())) {
				if(!isAvailable(clothing)) stock.remove(clothing);
			}
		}
		
		/**
		 * @return Die Anzahl der Sorten in der Umgebung
		 */
		public int getTypes() {
			return types;
		}
		
		/**
		 * @return Die Anzahl der Stilrichtungen in der Umgebung
		 */
		public int getStyles() {
			return styles;
		}
		
		/**
		 * @return Die Liste an kombinierbaren Stilrichtungen
		 */
		public List<Integer[]> getCombinable() {
			return combinables;
		}
		
		/**
		 * @return Der Bestand an verf�gbaren Kleidungsst�cken
		 */
		public Map<Clothing, Integer> getStock() {
			return stock;
		}
		
		/**
		 * Pr�ft, ob zwei Kleidungsst�cke kombinierbar sind.
		 * 
		 * @param clothin1 Kleidungsst�ck 1
		 * @param clothin2 Kleidungsst�ck 2
		 * @return ob zwei Kleidungsst�cke kombinierbar sind
		 */
		public boolean isCombinable(Clothing clothing1, Clothing clothing2) {
			for(Integer[] combinable : combinables) {
				if(clothing1.getStyle() == clothing2.getStyle() ||
						(combinable[0] == clothing1.getStyle() && combinable[1] == clothing2.getStyle()) ||
						(combinable[0] == clothing2.getStyle() && combinable[1] == clothing1.getStyle()))
					return true;
			}
			return false;
		}
		
		/**
		 * Entfernt ein Kleidungsst�ck aus dem Bestand.
		 * 
		 * @param clothing Das Kleidungsst�ck
		 */
		public void removeFromStock(Clothing clothing) {
			if(stock.containsKey(clothing)) stock.put(clothing, stock.get(clothing) - 1);
			if(stock.get(clothing) <= 0) stock.remove(clothing);
		}
		
		/**
		 * Pr�ft, ob ein Kleidungsst�ck noch verf�gbar ist.
		 * 
		 * @param clothing Das Kleidungsst�ck
		 * @return Ob ein Kleidungsst�ck noch verf�gbar ist
		 */
		public boolean isAvailable(Clothing clothing) {
			return stock.containsKey(clothing) && stock.get(clothing) > 0;
		}
		
		/**
		 * Pr�ft, ob die gegebene Sorte noch verf�gbar ist.
		 * @param type Die Sorte
		 * @return Ob die gegebene Sorte noch verf�gbar ist
		 */
		public boolean isTypeAvailable(int type) {
			for(Clothing clothing : stock.keySet()) {
				if(clothing.getType() == type && stock.get(clothing) > 0) return true;
			}
			return false;
		}
		
		/**
		 * F�llt Boxen mit m�glichst wenig Restbestand.
		 * @return Liste von mit Kleidungsst�cken gef�llten Boxen
		 */
		public DistributionResult fillBoxes() {
			
			List<Box> boxes = new ArrayList<>();
			
			// Ein Element von jeder Sorte zu einer Box hinzuf�gen, bis eine Sorte leer gegangen ist oder nichts mehr passt
			while(true) {
				Clothing[] match = recursiveMatch();
				if(match.length == 0) break;
				Box box = new Box(this);
				for(Clothing c : match) {
					box.addClothing(c);
					removeFromStock(c);
				}
				boxes.add(box);
			}
			
			// Rest auf die bereits erstellen Boxen verteilen
			distributeRest: while(true) {
				
				// Durch alle �brig gebliebenen Kleidungsst�cke iterieren
				for(Clothing clothing : new LinkedHashSet<>(stock.keySet())) {
					// F�r jede Box �berpr�fen, ob das Kleidungsst�ck hineinpasst, falls ja, zur Box hinzuf�gen
					for(Box box : boxes) {
						if(box.fitsIntoBox(clothing)) {
							box.addClothing(clothing);
							removeFromStock(clothing);
							continue distributeRest;
						}
					}
				}
				
				break;
			}
			
			// Boxen zur�ckgeben
			return new DistributionResult(boxes, stock);
			
		}
		
		/**
		 * Findet rekursiv einzelne, von der Stilrichtung zueinander passende Kleidungsst�cke.
		 * @return Ein Array aus stilistisch zueinander passenden Kleidungsst�cken, mit nur einem pro Sorte
		 */
		public Clothing[] recursiveMatch() {
			return recursiveMatch(new Clothing[0], types);
		}
		
		/**
		 * Findet rekursiv einzelne, von der Stilrichtung zueinander passende Kleidungsst�cke.
		 * @param in Bereits zueinander passende Kleidungsst�cke
		 * @param n Wie viele Kleidungsst�cke noch gefunden werden m�ssen
		 * @return Ein Array aus stilistisch zueinander passenden Kleidungsst�cken, mit nur einem pro Sorte
		 */
		private Clothing[] recursiveMatch(Clothing[] in, int n) {
			
			// Rekursive Abbruchbedingung
			if(n == 0) {
				return in;
			}
			
			// Welche Sorte gefunden werden soll
			int currentType = types - n + 1;
			// Iteration �ber alle Kleidungsst�cke
			typeLoop: for(Clothing clothing : stock.keySet()) {
				// Nur weitermachen, wenn die Sorte der zu findenden Sorte entspricht
				if(clothing.getType() != currentType) continue;
				
				// Falls in leer ist (Anfangsfall)
				if(in.length == 0) {
					// Rekursiver Aufruf, in + aktuelles Kleidungsst�ck als neues in, n wird verringert
					Clothing[] recRes = recursiveMatch(pushArray(in, clothing), n - 1);
					// Falls keine passenden Kleidungsst�cke gefunden wurden, behandle das n�chste Kleidungsst�ck,
					// andernfalls kann das Array zur�ckgegeben werden
					if(recRes.length == 0) continue typeLoop;
					return recRes;
				}
				// Iteration �ber alle bereits gefundenen Kleidungsst�cke
				for(Clothing c : in) {
					// Falls die beiden Kleidungsst�cke nicht kombinierbar sind, behandle das n�chste Kleidungsst�ck im Bestand
					if(!isCombinable(clothing, c)) continue typeLoop;
					// Falls die beiden Kleidungsst�cke kombinierbar sind, schreite ein Kleidungsst�ck weiter
					if(isCombinable(clothing, c)) {
						// Rekursiver Aufruf, in + aktuelles Kleidungsst�ck als neues in, n wird verringert
						Clothing[] recRes = recursiveMatch(pushArray(in, clothing), n - 1);
						// Falls keine Kleidungsst�cke gefunden wurden, behandle das n�chste Kleidungsst�ck im Bestand
						if(recRes.length == 0) continue typeLoop;
						// Andernfalls gib die gefundenen Kleidungsst�cke zur�ck
						return recRes;
					}
				}
			}
			
			return new Clothing[0];
		}
		
		/**
		 * F�gt ein Element zu einem Array hinzu
		 * @param <T> Typ des Elements
		 * @param arr Das Array
		 * @param item Das hinzuzuf�gende Element
		 * @return Ein neues Array mit dem Inhalt des alten Arrays und dem neuen Element
		 */
		private static <T> T[] pushArray(T[] arr, T item) {
	        T[] tmp = Arrays.copyOf(arr, arr.length + 1);
	        tmp[tmp.length - 1] = item;
	        return tmp;
	    }
		
		
		@Override
		public String toString() {
			String combinablesString = "";
			for(Integer[] combinable : combinables) {
				combinablesString += "\t\t" + combinable[0] + " " + combinable[1] + "\n";
			}
			String stockString = "";
			for(Clothing clothing : stock.keySet()) {
				stockString += "\t\t(" + clothing + ") * " + stock.get(clothing) + "\n";
			}
			return "Setting {\n\tTypes: " + types + ",\n\tStyles: " + styles + ",\n\tCombinables: [\n"
					+ combinablesString + "\t],\n\tStock: [\n" + stockString + "\t]\n}";
		}
		
	}
	
	public static class Clothing {
		
		// Sorte der Kleidungsst�ck
		int type;
		// Stil des Kleidungsst�ck
		int style;
		
		public Clothing(int type, int style) {
			this.type = type;
			this.style = style;
		}
		
		/**
		 * @return Die Sorte des Kleidungsst�ckes
		 */
		public int getType() {
			return type;
		}
		
		/**
		 * @return Die Stilrichtung der Kleidungsst�ckes
		 */
		public int getStyle() {
			return style;
		}
		
		@Override
		public String toString() {
			return type + " " + style;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Clothing)) return false;
			Clothing clothingObj = (Clothing) obj;
			return clothingObj.getType() == type && clothingObj.getStyle() == style;
		}
		
	}
	
	public static class Box {
		
		// Umgebung
		Setting setting;
		// Liste der Kleidungsst�cke der Box
		Map<Clothing, Integer> content;
		
		public Box(Setting setting) {
			this.setting = setting;
			content = new LinkedHashMap<>();
		}
		
		/**
		 * @return Den Inhalt der Box
		 */
		public Map<Clothing, Integer> getContent() {
			return content;
		}
		
		/**
		 * F�gt ein Kleidungsst�ck zur Box hinzu.
		 * 
		 * @param clothing Das zu hinzuf�gende Kleidungsst�ck
		 */
		public void addClothing(Clothing clothing) {
			// Falls existent, Anzahl von clothing erh�hen, falls nicht, auf 1 setzen
			content.put(clothing, content.containsKey(clothing) ? content.get(clothing) + 1 : 1);
		}
		
		/**
		 * Pr�ft, ob ein Kleidungsst�ck in die Box passt.<br><br>
		 * 
		 * In der Box d�rfen maximal 3 Kleidungsst�cke einer Sorte vorhanden sein und
		 * alle Stilrichtungen m�ssen zueinander passen.
		 * 
		 * @param clothing Das zu �berpr�fende Kleidungsst�ck
		 * @return Ob das Kleidungsst�ck in die Box passt
		 */
		public boolean fitsIntoBox(Clothing clothing) {
			if(getAmountOfType(clothing.getType()) >= 3) return false;
			for(Clothing c : content.keySet()) {
				if(!setting.isCombinable(c, clothing)) return false;
			}
			return true;
		}
		
		/**
		 * @return Ob in der Box von jeder Sorte mindestens eine vorhanden ist
		 */
		public boolean hasEnoughClothing() {
			for(int t = 1; t < setting.getTypes() + 1; t++) {
				if(getAmountOfType(t) <= 0) return false;
			}
			return true;
		}
		
		/**
		 * Gibt die Anzahl der Kleidungsst�cke von gegebener Sorte in der Box zur�ck.
		 * @param type Die Sorte
		 * @return Die Anzahl der Kleidungsst�cke von gegebener Sorte in der Box
		 */
		public int getAmountOfType(int type) {
			int count = 0;
			for(Clothing c : content.keySet()) {
				if(c.getType() == type) count += content.get(c);
			}
			return count;
		}
		
		@Override
		public String toString() {
			String s = "Box {";
			for(Clothing clothing : content.keySet()) {
				s += "\n\t(" + clothing + ") * " + content.get(clothing);
			}
			s += "\n}";
			return s;
		}
		
	}
	
	public static class DistributionResult {
		
		// Liste von gef�llten Boxen
		List<Box> boxes;
		// �brig gebliebener Bestand
		Map<Clothing, Integer> rest;
		
		public DistributionResult(List<Box> boxes, Map<Clothing, Integer> rest) {
			this.boxes = boxes;
			this.rest = rest;
		}
		
		/**
		 * Rundet einen double auf angegebene Nachkommastellen
		 * @param value Der zu rundende Wert
		 * @param places Die Anzahl der Nachkommastellen
		 * @return Den auf die angegebenen Nachkommastellen gerundeten double
		 */
		private static double round(double value, int places) {
		    if (places < 0) throw new IllegalArgumentException();

		    BigDecimal bd = BigDecimal.valueOf(value);
		    bd = bd.setScale(places, RoundingMode.HALF_UP);
		    return bd.doubleValue();
		}

		@Override
		public String toString() {
			String s = "Boxes:\n";
			for(Box box : boxes) {
				s += box + "\n";
			}
			s += "Rest:\n";
			for(Clothing clothing : rest.keySet()) {
				s += "- (" + clothing + ") * " + rest.get(clothing) + "\n";
			}
			int restAmount = 0;
			for(int amount : rest.values()) restAmount += amount;
			int inBoxAmount = 0;
			for(Box box : boxes) for(int amount : box.getContent().values()) inBoxAmount += amount;
			if(restAmount + inBoxAmount > 0)
				s += "Rest Ratio: " + restAmount + "/" + (restAmount + inBoxAmount) + " (" + round(((double) inBoxAmount / (restAmount + inBoxAmount)) * 100, 2) + "% of stock reused)";
			else
				s += "Rest Ratio: 0/0";
			return s;
		}
		
	}

}
