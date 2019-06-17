package solution;

import battleship.BattleShip;
import battleship.CellState;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

/**
 * A Sample random shooter - Takes no precaution on double shooting and has no
 * strategy once a ship is hit.
 *
 * @author VanPhuc Pham - 000761144
 * @author andrès Mauricio Penã Caroma-000769672
 */
public class SampleBot {

    private int gameSize;
    private BattleShip battleShip;
    private Random random;
    private boolean isPrintable = false;
    private CellState[][] map;
    private String status;
    final String HUNT_MODE = "HUNT";
    final String SINK_MODE = "SINK";
    private int[] lastPoint = new int[]{0, 0};
    private int currentHuntShip = 2;
    private int[] listOfShipSunk = new int[5];
    Stack<ArrayList> huntPoints = new Stack<>();
    Stack<Point> currentHuntPoints = new Stack<>();
    
    private int sinkModeCounter;

    /**
     * Constructor keeps a copy of the BattleShip instance
     *
     * @param b previously created battleship instance - should be a new game
     */
    
    
    public SampleBot(BattleShip b) {
        battleShip = b;
        gameSize = b.BOARDSIZE;
        random = new Random();   // Needed for random shooter - not required for more systematic approaches
        map = new CellState[gameSize][gameSize];

        //Preparing the hunt points
        prepareHuntCrossPoints(0, gameSize);
        prepareHuntBorderPoints(0, gameSize);

        for (int row = 0; row < gameSize; row++) {
            for (int col = 0; col < gameSize; col++) {
                map[row][col] = CellState.Empty;
            }
        }

        this.status = HUNT_MODE;
    }

    /**
     * Create a random shot and calls the battleship shoot method
     *
     * @return true if a Ship is hit, false otherwise
     */
    public boolean fireShot() {
        boolean hit;
        if (status.equals(HUNT_MODE)) {
            hit = fireHuntMode();
        } else {
            hit = fireSinkMode();
        }

        printMap();
        return hit;
    }

    /**
     * This method is executed only when hunt mode is active
     * @return 
     */
    private boolean fireHuntMode() {
        Point shot = getNextHuntPoint();

        //just in case there is any duplicated shot
        while (map[shot.x][shot.y] != CellState.Empty) {
            shot = getNextHuntPoint();
        }
        
        boolean hit = battleShip.shoot(shot);
        if (hit) {
            map[shot.x][shot.y] = CellState.Hit;
            lastPoint[0] = shot.x;
            lastPoint[1] = shot.y;
            status = SINK_MODE;
            this.sinkModeCounter = 1;
        } else {
            map[shot.x][shot.y] = CellState.Miss;
        }
        return hit;
    }
/**
     * This method is executed only when sink mode is active
     * @return 
     */
    private boolean fireSinkMode() {
        boolean hit = false; 
        boolean empty = false;
        int numberOfShipSunkNow = battleShip.numberOfShipsSunk(); //get the Number Sunk Ship on the map now
        int numberOfShipSunkAfterHit = battleShip.numberOfShipsSunk(); // get the Number Sunk Ship on the map after fire a hit
        // int[] distanceToBorder = new int[]{gameSize-1-lastPoint[0],gameSize-1-lastPoint[1]};
        int[][] nextHit = getNextHit();
        // This method will fire shots after hunt mode
        for (int i = 0; i < 4; i++) {
            int row = nextHit[i][0]; 
            int col = nextHit[i][1];
            if ((row == 0 && col == 0) && map[row][col] != CellState.Empty) {
            } else {
                Point shot = new Point(row, col);
                //System.out.println(shot);
                hit = battleShip.shoot(shot);
                if (hit) {
                    this.sinkModeCounter++;
                    map[row][col] = CellState.Hit;
                    status = SINK_MODE;

                    if (lastPoint[0] == row && i == 0) {//go to let
                        numberOfShipSunkAfterHit = HitHorizontalShipLeft(row, col);
                        if (numberOfShipSunkNow < numberOfShipSunkAfterHit) {
                            //store the ship that was
                            break;
                        }
                    } else if (lastPoint[0] == row && i == 1) {//go to right
                        numberOfShipSunkAfterHit = HitHorizontalShipRight(row, col);
                        if (numberOfShipSunkNow < numberOfShipSunkAfterHit) {
                            break;
                        }
                    } else if (lastPoint[1] == col && i == 2) {//go to up
                        numberOfShipSunkAfterHit = HitVerticalShipUp(row, col);
                        if (numberOfShipSunkNow < numberOfShipSunkAfterHit) {
                            break;
                        }
                    } else if (lastPoint[1] == col && i == 3) {//go to down 
                        numberOfShipSunkAfterHit = HitVerticalShipDown(row, col);
                        if (numberOfShipSunkNow < numberOfShipSunkAfterHit) {
                            break;
                        }
                    }
                    //printMap();
                } else {
                    map[row][col] = CellState.Miss;
                    status = SINK_MODE;
                }
            }

        }
        //row-col second
        status = HUNT_MODE;

        return hit;
    }
    /**
     * This method is to prepare the first four shots after hunt mode
     * @return the array of next shos
     */
    private int[][] getNextHit() {
        int row;
        int col;
        
        int[][] nextHit = new int[4][2];
        row = lastPoint[0];
        col = lastPoint[1] + 1 - currentHuntShip; 

        if (row < 10 && col < 10 && row >= 0 && col >= 0 && map[row][col] == CellState.Empty && row < 10 && col < 10) {
            nextHit[0][0] = row;
            nextHit[0][1] = col;

        } else {
            nextHit[0][0] = 0;
            nextHit[0][1] = 0;

        }

        row = lastPoint[0];
        col = lastPoint[1] - 1 + currentHuntShip;

        if (row < 10 && col < 10 && row >= 0 && col >= 0 && map[row][col] == CellState.Empty && row < 10 && col < 10) {
            nextHit[1][0] = row;
            nextHit[1][1] = col;

        } else {
            nextHit[1][0] = 0;
            nextHit[1][1] = 0;

        }

        row = lastPoint[0] + 1 - currentHuntShip;
        col = lastPoint[1];

        if (row < 10 && col < 10 && row >= 0 && col >= 0 && map[row][col] == CellState.Empty && row < 10 && col < 10) {
            nextHit[2][0] = row;
            nextHit[2][1] = col;

        } else {
            nextHit[2][0] = 0;
            nextHit[2][1] = 0;

        }

        row = lastPoint[0] + currentHuntShip - 1;
        col = lastPoint[1];

        if (row < 10 && col < 10 && row >= 0 && col >= 0 && map[row][col] == CellState.Empty) {
            nextHit[3][0] = row;
            nextHit[3][1] = col;

        } else {
            nextHit[3][0] = 0;
            nextHit[3][1] = 0;

        }

        return nextHit;
    }
    /**
     * After hunt mode, if go left and hit, this mode execute and fire the ship
     * @return number of ship Sunk
     */
    private int HitHorizontalShipLeft(int row, int col) {
        boolean hitNext = true; // condition to fire next Shot
        boolean hit = false; // hit or miss
        int numberOfNextHit = 3;
        int shipSunkNow = battleShip.numberOfShipsSunk(); // To get the number Of Ships now
        int shipSunkAfterHit = battleShip.numberOfShipsSunk(); // To get the number Of Ships sunk after a shot
        while (hitNext) {
            col = col - 1; // go left
            if (col < 0 || numberOfNextHit <= 0) { // limit the shot
                hitNext = false;
                break;
            } else {

                if (map[row][col] == CellState.Empty) {
                    Point shot = new Point(row, col);
                    hit = battleShip.shoot(shot);
                    numberOfNextHit--;
                    //printMap();
                    if (hit) {
                        this.sinkModeCounter++;
                        map[row][col] = CellState.Hit;
                        status = SINK_MODE;
                        shipSunkAfterHit = battleShip.numberOfShipsSunk(); // if hit a ship change to hunt mode
                        if (shipSunkNow < shipSunkAfterHit) {
                            hitNext = false;
                        }

                    } else {
                        map[row][col] = CellState.Miss;
                        status = SINK_MODE;

                        hitNext = false;
                        break;
                    }
                } else {
                    hitNext = false;
                    break;
                }

            }
        }
        return shipSunkAfterHit; // Get ship sink
    }
    /**
     * After hunt mode, if go right and hit, this mode execute and fire the ship
     * @return number of ship Sunk
     */
    private int HitHorizontalShipRight(int row, int col) {
        boolean hitNext = true;
        boolean hit = false;
        int numberOfNextHit = 3;
        int shipSunkNow = battleShip.numberOfShipsSunk();
        int shipSunkAfterHit = battleShip.numberOfShipsSunk();
        while (hitNext) {
            col = col + 1;
            if (col > 9 || numberOfNextHit <= 0) {
                hitNext = false;
                break;
            } else {
                if (map[row][col] == CellState.Empty) {
                    Point shot = new Point(row, col);
                    hit = battleShip.shoot(shot);
                    numberOfNextHit--;
                    if (hit) {
                        this.sinkModeCounter++;
                        map[row][col] = CellState.Hit;
                        status = SINK_MODE;
                        shipSunkAfterHit = battleShip.numberOfShipsSunk();
                        if (shipSunkNow < shipSunkAfterHit) {
                            hitNext = false;
                        }

                    } else {
                        map[row][col] = CellState.Miss;
                        status = SINK_MODE;
                        hitNext = false;
                        break;
                    }
                } else {
                    hitNext = false;
                    break;
                }

            }
        }
        return shipSunkAfterHit;
    }
   /**
     * After hunt mode, if go up and hit, this mode execute and fire the ship
     * @return number of ship Sunk
     */
    private int HitVerticalShipUp(int row, int col) {
        boolean hitNext = true;
        boolean hit = false;
        int numberOfNextHit = 3;
        int shipSunkNow = battleShip.numberOfShipsSunk();
        int shipSunkAfterHit = battleShip.numberOfShipsSunk();
        while (hitNext) {
            row = row - 1;
            if (row < 0 || numberOfNextHit <= 0) {
                hitNext = false;
                break;
            } else {
                if (map[row][col] == CellState.Empty) {
                    Point shot = new Point(row, col);
                    hit = battleShip.shoot(shot);
                    //printMap();
                    numberOfNextHit--;
                    if (hit) {
                        this.sinkModeCounter++;
                        map[row][col] = CellState.Hit;
                        status = SINK_MODE;
                        shipSunkAfterHit = battleShip.numberOfShipsSunk();
                        if (shipSunkNow < shipSunkAfterHit) {
                            hitNext = false;
                        }

                    } else {
                        map[row][col] = CellState.Miss;
                        status = SINK_MODE;
                        hitNext = false;
                        break;
                    }
                } else {
                    hitNext = false;
                    break;
                }

            }
        }
        return shipSunkAfterHit;
    }
/**
     * After hunt mode, if go down and hit, this mode execute and fire the ship
     * @return number of ship Sunk
     */
    private int HitVerticalShipDown(int row, int col) {
        boolean hitNext = true;
        boolean hit = false;
        int numberOfNextHit = 3;
        int shipSunkNow = battleShip.numberOfShipsSunk();
        int shipSunkAfterHit = battleShip.numberOfShipsSunk();
        while (hitNext) {
            row = row + 1;

            if (row > 9 || numberOfNextHit <= 0) {
                hitNext = false;
                break;
            } else {
                if (map[row][col] == CellState.Empty) {
                    Point shot = new Point(row, col);
                    hit = battleShip.shoot(shot);
                    //printMap();
                    numberOfNextHit--;
                    if (hit) {
                        this.sinkModeCounter++;
                        map[row][col] = CellState.Hit;
                        status = SINK_MODE;
                        shipSunkAfterHit = battleShip.numberOfShipsSunk();
                        if (shipSunkNow < shipSunkAfterHit) {
                            hitNext = false;
                        }

                    } else {
                        map[row][col] = CellState.Miss;
                        status = SINK_MODE;
                        hitNext = false;
                        break;
                    }
                } else {
                    hitNext = false;
                    break;
                }

            }
        }
        return shipSunkAfterHit;
    }
    // get the map
    public void printMap() {
        if(this.isPrintable){
            System.out.println("====BOARD====");
            for (int row = 0; row < gameSize; row++) {
                for (int col = 0; col < gameSize; col++) {
                    System.out.print(map[row][col]);
                }
                System.out.println("");
            }
            try {
                //Thread.sleep(500);
            } catch (Exception e) {

            }
        }
    }

    /**
     * This method prepares the starting (cross) points to shot and store them
     * in huntPoints start and end parameter is the area to prepare
     *
     * @param start
     * @param end
     */
    private void prepareHuntCrossPoints(int start, int end) {
        //only 4 points because it should be a cross
        for (int i = start; i < end / 2; i = i + 1) {
            ArrayList<Point> ps = new ArrayList<>();
            //vertical
            ps.add(new Point(end - 1 - i, end / 2 - i % 2));
            ps.add(new Point(i, end / 2 - i % 2));

            //horizontal
            ps.add(new Point(end / 2 - i % 2, i));
            ps.add(new Point(end / 2 - i % 2, end - 1 - i));
            huntPoints.push(ps);
        }
    }

    /**
     * This method prepares the starting (cross) points to shot and store them
     * in huntPoints start and end parameter is the area to prepare
     *
     * @param start
     * @param end
     */
    private void prepareMissingHuntPoints(int start, int end) {
        for (int i = start; i < end; i ++) {
            ArrayList<Point> ps = new ArrayList<>();
            //vertical
            for(int j = start; j < end; j++){
                int closeI = 0;
                int closeJ = 0;
                if(i < 9){
                    closeI = i+1;
                }
                if(j < 9){
                    closeJ = j+1;
                }
                if(map[i][j] == CellState.Empty && 
                    map[i][closeJ] == CellState.Empty &&
                        map[closeI][j] == CellState.Empty){
                    ps.add(new Point(i, j));
                }
            }
            huntPoints.push(ps);
        }
    }
    
    /**
     * This method prepares hunt border points
     * @param start
     * @param end 
     */
    private void prepareHuntBorderPoints(int start, int end) {
        for (int i = start; i < end; i ++) {
            ArrayList<Point> ps = new ArrayList<>();
            //vertical
            for(int j = start; j < end; j = j + 2){
                if(j + i%2 < gameSize){
                    ps.add(new Point(i, j + i%2));
                }
            }
            huntPoints.push(ps);
        }
    }

    /**
     * This method prepares the second step (corners) points to shot and store
     * them in huntPoints start and end parameter is the area to prepare
     *
     * @param start
     * @param end
     */
    private void prepareHuntCornerPoints(int start, int end) {
        ArrayList<Point> ps = new ArrayList<>();
                ps.add(new Point(start, start));//left corner
                ps.add(new Point(end-1, end-1)); //right corner
                ps.add(new Point(end-1, start)); //right corner
                ps.add(new Point(start, end-1)); //right corner
        huntPoints.push(ps);
    }

    
    /**
     * This Method returns the next point to hit for hunting mode
     * @return 
     */
    private Point getNextHuntPoint() {
        if (!huntPoints.isEmpty()) {
            if (currentHuntPoints.isEmpty()) {
                if (!huntPoints.empty()) {
                    ArrayList<Point> points = huntPoints.pop();
                    for (Point p : points) {
                        currentHuntPoints.push(p);
                    }
                }
            }
            return currentHuntPoints.pop();
        } else {//hidden ships
            boolean found = false;
            Point p = null;
            while (!found) {
                int row = random.nextInt(gameSize);
                int col = random.nextInt(gameSize);
                if (map[row][col] == CellState.Empty ){
                    p = new Point(row, col);
                    found = true;
                }
            }
            return p;
        }
    }

}
