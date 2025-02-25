import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.ArrayList;
import java.util.Random;
import tester.*;

/*User guide: You click a cell on the grid. 

 * If the cell has a mine, you lost.

 * If the cell shows a number, 

 * that number shows the number of adjacent mines to that cell.

 * If the cell is empty and has no neighboring mines, 

 * then it shows a floodfill on all the connected cells near

 * a mine.

 * If you think you know which cell has a mine, right click to put a 

 * flag of on.

 * Enjoy :| 

 */


//what doesn't change
interface WorldConstants {
  int CELL_SIZE = 20;
}

//is the game that uses the word java and uses the constants
class Minesweepergame extends World implements WorldConstants {
  int rows;
  int columns;
  int numMines;
  ArrayList<ArrayList<Cell>> grid;
  boolean gameOver;

  //is the game that uses the word java and uses the constants
  Minesweepergame(int rows, int columns, int numMines) {
    this.rows = rows;
    this.columns = columns;
    this.numMines = numMines;
    this.grid = new ArrayList<>();
    this.gameOver = false;
    initializeGrid();
    
    if (numMines > rows * columns || numMines == 0) {
      throw new IllegalArgumentException("Too many or too little mines");
    }
    
  }
  
  //use for testing grid
  Minesweepergame(int rows, int columns, int numMines, 
      ArrayList<ArrayList<Cell>> grid) {
    this.rows = rows;
    this.columns = columns;
    this.numMines = numMines;
    this.grid = grid;
    this.gameOver = false;
  }
  
  
  //use for testing grid and end
  Minesweepergame(int rows, int columns, int numMines, 
      ArrayList<ArrayList<Cell>> grid, boolean gameOver) {
    this.rows = rows;
    this.columns = columns;
    this.numMines = numMines;
    this.grid = grid;
    this.gameOver = gameOver;
  }
   
  //use for testing the random seed
  Minesweepergame(int rows, int columns, int numMines, int seed) {
    this.rows = rows;
    this.columns = columns;
    this.numMines = numMines;
    this.grid = new ArrayList<>();
    this.gameOver = false;
    initializeGrid();
    placeMines(new Random(seed));
  }

  //creates the grid
  void initializeGrid() {
    for (int i = 0; i < rows; i++) {
      ArrayList<Cell> row = new ArrayList<>();
      for (int j = 0; j < columns; j++) {
        row.add(new Cell());
      }
      grid.add(row);
    }
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        linkNeighbors(i, j);
      }
    }
    placeMines(new Random());
  }

    
  //Link the cells together when the game is initialized so that 
  //every cell has a list of its neighbors.
  void linkNeighbors(int row, int col) {
    int[] directions = {-1, 0, 1};
    for (int dr : directions) {
      for (int dc : directions) {
        if (dr != 0 || dc != 0) {
          int newRow = row + dr;
          int newCol = col + dc;
          if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < columns) {
            grid.get(row).get(col).neighbors.add(grid.get(newRow).get(newCol));
          }
        }
      }
    }
  }

  //Generates the random placement of mines
  void placeMines(Random random) {
    int minesPlaced = 0;
    while (minesPlaced < numMines) {
      int row = random.nextInt(rows);
      int col = random.nextInt(columns);
      Cell cell = grid.get(row).get(col);
      if (!cell.isMine) {
        cell.isMine = true;
        minesPlaced++;
        for (Cell neighbor : cell.neighbors) {
          neighbor.adjacentMines++;
        }
      }
    }
  }

  //makes the rows and columns
  @Override
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(columns * CELL_SIZE, rows * CELL_SIZE);
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        Cell currentCell = grid.get(i).get(j);
        WorldImage cellImage = currentCell.draw();
        // Centering cells within their grid position
        int x = j * CELL_SIZE + CELL_SIZE / 2;
        int y = i * CELL_SIZE + CELL_SIZE / 2;
        scene.placeImageXY(cellImage, x, y);
      }
    }
    return scene;
  }
   
  //figure on which cell is being clicked on
  @Override
  public void onMouseClicked(Posn pos, String whichClick) {
    int row = pos.y / CELL_SIZE;
    int col = pos.x / CELL_SIZE;
        
    if (row >= 0 && row < rows && col >= 0 && col < columns) {
      Cell cell = grid.get(row).get(col);
            
      if (!cell.isRevealed && !cell.isFlagged && whichClick.equals("LeftButton")) {
        cell.reveal();
        if (cell.isMine) {
          this.gameOver = true;
          this.revealAllMines();
        } else if (cell.adjacentMines == 0) {
          cell.floodFill();
        }
      } 
      else if (!cell.isRevealed && whichClick.equals("RightButton")) {
        cell.toggleFlag();
      }
            
      else if (!cell.isMine && this.wonGame()) {
        this.gameOver = true;
      }
                   
    }
        
  }
    
   
  //ends the game when word goes to the button
  public WorldEnd worldEnds() {
    WorldScene endScene = this.makeScene();
    if (this.gameOver) { 
      endScene.placeImageXY(new TextImage("You lost HAHAH", 20, Color.RED),
          CELL_SIZE * columns / 2 , CELL_SIZE * rows / 2 );
      return new WorldEnd(true, endScene);
    } 
    else if (this.wonGame()) {
      endScene.placeImageXY(new TextImage("You Won", 20, Color.RED), 
          CELL_SIZE * columns / 2, CELL_SIZE * rows / 2);
      return new WorldEnd(true, endScene);
    }
    return new WorldEnd(false, endScene);
  }
    
  //shows all the mines
  public void revealAllMines() {
    for (ArrayList<Cell> row : grid) {
      for (Cell cell : row) {
        if (cell.isMine) {
          cell.isRevealed = true;
        }
      }
    }
  }
 
  //when you win the game this happens
  public boolean wonGame() {  
    for (ArrayList<Cell> row : this.grid) {
      for (Cell c: row) {
        if (!c.isMine && !c.isRevealed) {
          return false;
        }
        else if (c.isMine && !c.isFlagged) {
          return false;
        }
      }
    }
    return true;
  }
}

//figure on which cell is being clicked on
class Cell {
  boolean isMine;
  int adjacentMines;
  boolean isRevealed;
  boolean isFlagged;
  ArrayList<Cell> neighbors;

  //represents a single cell in the game
  public Cell() {
    this.isMine = false;
    this.adjacentMines = 0;
    this.isRevealed = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
  }
  
  //for testing
  public Cell(boolean isMine, boolean isRevealed, boolean isFlagged) {
    this.isMine = false;
    this.adjacentMines = 0;
    this.isRevealed = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
  }
  
  

  //draws a visual representation of the game's cells, grid, mines, and flags
  public WorldImage draw() {
    WorldImage baseImage = new RectangleImage(WorldConstants.CELL_SIZE - 2, 
        WorldConstants.CELL_SIZE - 2, OutlineMode.SOLID, Color.DARK_GRAY);
    WorldImage cellContent = new EmptyImage();

    if (this.isRevealed) {
      baseImage = new RectangleImage(WorldConstants.CELL_SIZE - 2, 
          WorldConstants.CELL_SIZE - 2, OutlineMode.SOLID, Color.LIGHT_GRAY);

      if (this.isMine) {
        cellContent = new CircleImage(WorldConstants.CELL_SIZE / 4, OutlineMode.SOLID, 
            Color.BLACK);
      } else if (this.adjacentMines > 0) {
        cellContent = new TextImage(String.valueOf(this.adjacentMines), Color.BLACK);
      }
    } else if (this.isFlagged) {
      int side = WorldConstants.CELL_SIZE / 2;
      cellContent = new EquilateralTriangleImage(side, OutlineMode.SOLID, Color.ORANGE);
    }

    WorldImage outlineImage = new RectangleImage(WorldConstants.CELL_SIZE, 
        WorldConstants.CELL_SIZE, OutlineMode.OUTLINE, Color.BLACK);

    baseImage = new OverlayImage(cellContent, baseImage);

    return new OverlayImage(baseImage, outlineImage);
  }

  //is 'revealed' when clicked on cell
  public void reveal() {
    if (!this.isFlagged) {
      this.isRevealed = true;
    }
  }
  
  //flags/toggles a cell
  public void toggleFlag() {
    if (!this.isRevealed) {
      this.isFlagged = !isFlagged;
    }
  }
    
    
    
  //shows the adjacent cells that don't have a mine, but also shows cells that are next to mines
  public void floodFill() {
    if (!this.isMine) {
      this.isRevealed = true;
    }
    if (this.adjacentMines == 0) {
      for (Cell c : this.neighbors) {
        if (!c.isRevealed) {
          c.floodFill();
        }
      }
    }
  }
}    


class ExamplesMinesweeper {
  
  
  Minesweepergame ms1;
  
  Minesweepergame ms12;
  
  Minesweepergame ms3;
  
  ArrayList<Cell> al1;
  
  Minesweepergame ms2 = new Minesweepergame(2, 2, 2);
  
  Cell minedCell = new Cell(true, true, false);
  
  Cell normalCell;

  
   

  
  void initData() {
    ArrayList<Cell> row1 = new ArrayList<>();
    row1.add(this.minedCell);
    row1.add(new Cell(false, false, false));
    ArrayList<ArrayList<Cell>> grid = new ArrayList<>();
    //row2.add();
    //grid.add(row2)
    grid.add(row1);
    ms1 = new Minesweepergame(1,2,1, grid);
  
    
    this.al1 = new ArrayList<Cell>();
    this.al1.add(new Cell());
    this.al1.add(new Cell());
    this.al1.add(new Cell());
    this.al1.add(new Cell());
    this.al1.add(new Cell());
    this.al1.add(new Cell());
    this.al1.add(new Cell());
    this.al1.add(new Cell());
    
    
    ArrayList<Cell> row1s = new ArrayList<>();
    row1s.add(new Cell(false, false, false));
    row1s.add(new Cell(false, false, false));
    ArrayList<ArrayList<Cell>> grid2 = new ArrayList<>();
    //row2.add();
    //grid.add(row2)
    grid2.add(row1s);
    ms12 = new Minesweepergame(1,2,1, grid2);
    
    
    normalCell = new Cell(false, false, false);
    
    

    //row2.add();
    //grid.add(row2)

    ms3 = new Minesweepergame(1,2,1, grid, true);
    
  
  }
  
  //tests the visualizaition of the game big bang
  void testBigBang(Tester t) {
    Minesweepergame game = new Minesweepergame(30, 16, 60);
    int worldWidth = game.columns * WorldConstants.CELL_SIZE;
    int worldHeight = game.rows * WorldConstants.CELL_SIZE;
    game.bigBang(worldWidth, worldHeight, 0.1);
  }
  
  //tests if you won the game
  boolean testWonGame(Tester t) {
    initData();
    return t.checkExpect(this.ms1.wonGame(), false);
    
  }
  
  /*
  
  boolean testWorldEnds(Tester t) {
    initData();
    return t.checkExpect(ms3.worldEnds(), new WorldEnd(true, 
    endScene.placeImageXY(new TextImage("You lost HAHAH", 20, Color.RED),
        20 , 10 )));
    
  }
  
  */
  
  //tests if it draws a cell
  boolean testDraw(Tester t) {
    return t.checkExpect(new Cell().draw(), 
         new OverlayImage(new OverlayImage(new EmptyImage(), 
            new RectangleImage(18, 18, OutlineMode.SOLID, Color.DARK_GRAY)), 
            new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK)));
  }
  
  //tests to show shows all the mines
  void testRevealAllMines(Tester t) {
    initData();
    ms1.revealAllMines();
    t.checkExpect(minedCell.isRevealed, false);
    
  }
  
  
  /*
  boolean testWorldEnds(Tester t) {
    return t.checkExpect(this.ms1, null)
  }
*/
  

  
  
  //tests to make a scene
  boolean testMakeScene(Tester t) {
    return t.checkExpect(this.ms2.makeScene(), new WorldScene(40, 40));
  }
  
  
  /*
  //tests if it reveals all mines
  void testRevealAllMines(Tester t) {
    initData();
    ms1.grid.cell.isMine = true;
    ms1.revealAllMines();
  }
  */
  
  
  //tests if the cell is revealed when clicked
  void testReveal(Tester t) {
    initData();
    Cell c1 = new Cell();
    c1.isRevealed = false;
    c1.reveal();
    t.checkExpect(c1.isRevealed, true);
    
  }
  
  //tests to flood
  void TestfloodFill(Tester t) {
    initData();
    normalCell = new Cell(false, false, false);
    normalCell.floodFill();
    t.checkExpect(normalCell.isRevealed, true);
  }
  
  
  //tests to flag a cell
  void testToggleFlag(Tester t) {
    initData();
    Cell c1 = new Cell();
    c1.isFlagged = false;
    c1.toggleFlag();
    t.checkExpect(c1.isFlagged,  true);
    c1.toggleFlag();
    t.checkExpect(c1.isFlagged,  false);
  }
  
  

  

  /*
  //tests to count the number of mines
  void testCountMines(Tester t) {
    initData();
    Cell c1 = new Cell();
    t.checkExpect(c1.adjacentMines, 0);
    c1.countMines();
    t.checkExpect(c1.adjacentMines, 0);
    
  }
  */

  
  
}

    
    
