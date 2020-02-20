import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.util.ArrayList;

public class FractalExplorer {
	
	private int width;
	private int height;
	
	// Главное окно
	private JFrame frame;
	
	// Элементы на North
	private JPanel northP;
	private JComboBox chooseF;
	private JLabel textF;
	
	// Элементы на Center
	private JImageDisplay display;
	private Rectangle2D.Double range;
	
	// Элементы на South
	private JPanel southP;
	private JButton resetB;
	private JButton saveB;
	
	// Фракталы
	private ArrayList<FractalGenerator> fractals;
	
	/*
	* Классы-слушатели событий кнопки сброса и сохранения, и мыши
	*/
	private class resetButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Reset button clicked!");
			
			// Сброс границ фрактала и вызов функции отрисовки
			int index = chooseF.getSelectedIndex();
			if (index >= fractals.size()) {
				FractalExplorer.this.setStartImage();
				return;
			}
			
			fractals.get(index).getInitialRange(range);
			FractalExplorer.this.drawFractal(index);
		}
	}
	
	private class saveButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Save button clicked!");
		}
	}
	
	private class mouseClickListener implements MouseListener {
		
		// Событие нажатия на кнопку мыщи
		public void mouseClicked(MouseEvent e) {
			System.out.println("Mouse button clicked!");
			
			int index = chooseF.getSelectedIndex();
			if (index >= fractals.size()) return;
			
			// Координаты клика мыши
			int x = e.getX();
			int y = e.getY();
			
			// Перевод координат в комплексную плоскость
			double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, display.getWidth(), x);
			double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, display.getHeight(), y);
			
			// Нажатие левой кнопкой мыши
			if (e.getButton() == MouseEvent.BUTTON1) {
				// Масштабирование
				fractals.get(index).recenterAndZoomRange(range, xCoord, yCoord, 0.5);
			}
			
			// Нажатие правой кнопкой мыши
			if (e.getButton() == MouseEvent.BUTTON3) {
				// Масштабирование
				fractals.get(index).recenterAndZoomRange(range, xCoord, yCoord, 1.5);
			}
			
			// Перерисовка фрактала
			FractalExplorer.this.drawFractal(index);	
		}
		
		/*
		* Need just to override them (error with russian words here)
		*/
		public void mouseEntered(MouseEvent e) {}
 
        public void mouseExited(MouseEvent e) {}
 
        public void mousePressed(MouseEvent e) {}
 
        public void mouseReleased(MouseEvent e) {}
	}
	
	private class comboBoxClickListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			System.out.println("Click in ComboBox on " + chooseF.getSelectedItem());
			
			// За основу взята идея, что индекс из ComboBox соответствует индексу в ArrayList
			int index = chooseF.getSelectedIndex();
			
			if (index >= fractals.size()) {
				FractalExplorer.this.setStartImage();
				return;
			}
			
			// Настройка начального диапазона фрактала
			fractals.get(index).getInitialRange(range);
			
			// Вызов функции рисования
			FractalExplorer.this.drawFractal(index);
		}
	}
	
	/*
	* Конструкторы
	*/
	public FractalExplorer() {
		this(500);
	}
	
	public FractalExplorer(int size) {
		this(size, size);
	}
	
	public FractalExplorer(int width, int height) {
		this.width = width;
		this.height = height;
		
		// Создание объекта, содержащего диапазон
		this.range = new Rectangle2D.Double();
		
		// Создание объектов Фракталов
		fractals = new ArrayList<FractalGenerator>();
		fractals.add(new Mandelbrot());
		
		// ---Добавлять новые фракталы сюда---		
		fractals.add(new Tricorn());
		fractals.add(new BurningShip());
	}
	
	/*
	* Создание формы с компонентами
	*/
	public void createAndShowGUI() {
		// Создание формы
		this.frame = new JFrame("Fraktalz");
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.frame.setSize(this.width, this.height);
		this.frame.setResizable(false); 
		
		// Создание панелей
		northP = new JPanel();
		southP = new JPanel();
		
		// Добавление кнопки сброса масштабирования, и сохранения
		this.resetB = new JButton("Reset display");
		this.resetB.setPreferredSize(new Dimension(frame.getWidth() / 3, 30));
		southP.add(this.resetB);
		
		this.saveB = new JButton("Save image");
		this.saveB.setPreferredSize(new Dimension(frame.getWidth() / 3, 30));
		southP.add(this.saveB);
		
		// Добавление текста
		this.textF = new JLabel("Fraktals: ");
		Font font = saveB.getFont();
		textF.setFont(font);
		northP.add(this.textF);
		
		// Создание и заполнение списка элементами
		this.chooseF = new JComboBox();
		for (int i = 0; i < fractals.size(); i++) {
			chooseF.addItem(fractals.get(i).toString());
		}
		
		// Доавление начального пустого элемента
		chooseF.addItem("-Empty-");
		
		// Установка флага на пустом элементе
		chooseF.setSelectedIndex(fractals.size());
		
		// Задание размера и добавление на панель
		this.chooseF.setPreferredSize(new Dimension(frame.getWidth() / 4, 30));
		northP.add(this.chooseF);
		
		// Добавление панелей на форму
		frame.getContentPane().add(BorderLayout.NORTH, this.northP);
		frame.getContentPane().add(BorderLayout.SOUTH, this.southP);
		
		// Подгон под квадратную область после добавления панелей. 60 - сумма высот элементов панелей
		int height = frame.getHeight() - 60;
		int width = height;
		frame.setSize(width, frame.getHeight());
		
		// Создание панели рисования
		this.display = new JImageDisplay(width, height);
		//this.display = new JImageDisplay(this.frame.getWidth(), this.frame.getHeight());
		frame.getContentPane().add(BorderLayout.CENTER, this.display);
		
		// Добавление слушателя нажатия мыши по элементу
		display.addMouseListener(new mouseClickListener());
		
		// Добавление слушателей нажатия на кнопки
		resetB.addActionListener(new resetButtonListener());
		saveB.addActionListener(new saveButtonListener());
		chooseF.addActionListener(new comboBoxClickListener());
		
		frame.setVisible(true);
	}
	
	/*
	* Отрисовка фрактала.
	* В цикле идёт проход по всем пикселям и определяется, входит ли он в площадь фрактала
	* Степень входа определяется цветом пикселя.
	*/
	
	public void drawFractal(int index) {
		
		//System.out.println("Range = " + range.x + ", " + range.y + ", " + range.width + ", " + range.height + "\n");
		
		// Очистка картинки после предыдущего рисунка
		this.clearImage();
		
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				
				// Преобразование координат плоскости пикселей в координаты мнимой плоскости
				double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, display.getWidth(), x);
				double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, display.getHeight(), y);
				
				// Определение входа точки в множество Мандельброта
				int numOfIter = fractals.get(index).numIterations(xCoord, yCoord);
				
				// Логирование количества итераций каждой точки
				//if (numOfIter > 50)
					//System.out.println("x = " + x + ", y = " + y + ", xCoord = " + xCoord + ", yCoord = " + yCoord + ", iteration = " + numOfIter);
				
				int rgbColor;
				if (numOfIter != -1) {
					float hue = 0.7f + (float) numOfIter / 200f; 
					rgbColor = Color.HSBtoRGB(hue, 1f, 1f); 
					//display.drawPixel(x, y, Color.pink);
				} 
				else {
					rgbColor = Color.HSBtoRGB(0, 0, 0); 
					//display.drawPixel(x, y, Color.black);
				}
				
				display.drawPixel(x, y, new Color(rgbColor));
				
			}
		}
	}
	
	/*
	* Управление панелью для рисования
	*/
	public void setStartImage() {
		this.display.setStartImage();
	}
	
	public void clearImage() {
		this.display.clearImage();
	}
	
	/*
	* Точка входа
	*/
	public static void main(String[] args) {
		FractalExplorer explorer = new FractalExplorer(600);
		explorer.createAndShowGUI();
	}
}