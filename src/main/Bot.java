package main;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static main.MainController.WIDTH;

public class Bot {
    private static final int maxDepth = 6;

    private int depth;                                  //Текущая глубина этого узла
    private ArrayList<Bot> children;                    //Дочерние узлы этого узла
    private Cells[][] move;                             //Ход, который привел к currentBoard
    private Cells[][] currentBoard;
    private Player turn;                                 //Ходит игрок с доской currentBoard
    private int value;                                  //Оценка позиции

    private int fromAttackX;           //Используется в констсрукторе, значащий повторную атаку для COMPUTER
    private int fromAttackY;

    private boolean isAttack = false;  //move - это атака?
    private int fromID;                //Координаты клеток, описывающие move
    private int toID;

    Bot(Cells[][] board, Player turn) {
        this.depth = 0;
        this.move = null;
        this.turn = turn;
        this.currentBoard = GameBoard.cloneBoard(board);
        this.children = new ArrayList<>();
    }

    Bot(Cells[][] board, int fromAttackX, int fromAttackY, Player turn) {
        this.isAttack = true;
        this.depth = 0;
        this.move = null;
        this.turn = turn;
        this.currentBoard = GameBoard.possibleAttacks(fromAttackX, fromAttackY, board, turn);
        this.children = new ArrayList<>();
        this.fromAttackX = fromAttackX;
        this.fromAttackY = fromAttackY;
    }

    private Bot(Cells[][] changedBoard, Cells[][] move, Player turn, int depth) {
        this.depth = depth;
        this.move = move;
        this.turn = turn;
        this.currentBoard = GameBoard.cloneBoard(changedBoard);
        this.children = new ArrayList<>();
        this.isAttack = check(this.move, this.currentBoard);
        this.fromID = findCellBot(move, Cells.BOT_FROM);
        this.toID = findCellBot(move, Cells.BOT_TO);
    }

    boolean isAttack() {
        return this.isAttack;
    }

    int getFromID() { return this.fromID; }
    int getToID() { return this.toID; }

    void bestMove() {
        int bestEval = - miniMax(-100000, 100000);
        for (Bot child : this.children) {
            if (child.value == bestEval) {
                if (child.isAttack) {
                    this.isAttack = true;
                }
                this.fromID = child.fromID;
                this.toID = child.toID;
                break;
            }
        }
    }

    private int miniMax(int alpha, int beta) {
        int a = alpha;
        int b = beta;
        int value;
        Player opponent = GameBoard.getOpponent(turn);

        Map<Cells[][], ArrayList<Cells[][]>> table = this.findAllPossibleMoves(turn);

        if (depth == Bot.maxDepth || table.isEmpty()) {
            value = evaluation();
            return value;
        }

        for (Cells[][] move : table.keySet()) {
            for (Cells[][] changedBoard : table.get(move)) {
                Bot node = new Bot(changedBoard, move, opponent, this.depth+1);
                this.children.add(node);

                value = -1 * node.miniMax(-1*b, -1*a);

                a = max(a, value);
                if(a >= beta) {
                    this.value = a;
                    return a;
                }
                b = a+1;
            }
        }
        this.value = a;
        return a;
    }

    private int evaluation() {
        Cells[][] endPos = this.currentBoard;
        int commonWhite = 0;
        int queenWhite = 0;
        int commonBlack = 0;
        int queenBlack = 0;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < WIDTH; y++) {
                if (endPos[x][y] == Cells.WHITE) commonWhite++;
                if (endPos[x][y] == Cells.WHITE_QUEEN) queenWhite++;
                if (endPos[x][y] == Cells.BLACK) commonBlack++;
                if (endPos[x][y] == Cells.BLACK_QUEEN) queenBlack++;
            }
        }
        if (commonBlack + queenBlack == 0 || commonWhite + queenWhite == 0) return 0;
        if (commonBlack + commonWhite < 14) return 43*(((int) (pow(4, queenBlack + 1) - 4))-((int) (pow(4, queenWhite + 1) - 4)))
                + 71*(commonBlack-commonWhite);
        return 43*(((int) (pow(4, queenBlack + 1) - 4))-((int) (pow(4, queenWhite + 1) - 4))) + 89*(commonBlack-commonWhite);
    }


    private Map<Cells[][], ArrayList<Cells[][]>> findAllPossibleMoves(Player curPlayer) {
        Map<Cells[][], ArrayList<Cells[][]>> firstStepRelatesWithHisMoves = new HashMap<>();
        boolean case1 = false;
        boolean case2 = false;
        if (GameBoard.boardContainsCell(this.currentBoard, Cells.PLACE_MOVE)) {
            case1 = true;
        }
        else if (GameBoard.canMakeOneAttack(this.currentBoard, curPlayer)) {
            case2 = true;
        }

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < WIDTH; y++) {
                Cells[][] temp;
                if (case1) temp = this.currentBoard;
                else if (case2) temp = GameBoard.possibleAttacks(x, y, this.currentBoard, curPlayer);
                else temp = GameBoard.possibleCommonMoves(x, y, this.currentBoard, curPlayer);

                for (int i = 0; i < WIDTH; i++)
                    for (int j = 0; j < WIDTH; j++) {
                        if (temp[i][j] == Cells.PLACE_MOVE) {
                            Cells[][] onePlaceMove = createBoardWithOnePlaceMove(temp, i, j);

                            ArrayList<Cells[][]> manyMoves = new ArrayList<>();

                            if (case1) {
                                manyMoves = GameBoard.consecutiveAttacksFromOnePosition(fromAttackX, fromAttackY,
                                        onePlaceMove, curPlayer);
                            }
                            else if (case2) {
                                manyMoves = GameBoard.consecutiveAttacksFromOnePosition(x, y, onePlaceMove, curPlayer);
                            }
                            else {
                                Cells[][] finish = GameBoard.commonMove(x, y, i, j, temp);
                                manyMoves.add(finish);
                            }

                            if (case1) {
                                onePlaceMove[fromAttackX][fromAttackY] = Cells.BOT_FROM;
                            } else onePlaceMove[x][y] = Cells.BOT_FROM;
                            onePlaceMove[i][j] = Cells.BOT_TO;

                            firstStepRelatesWithHisMoves.put(onePlaceMove, manyMoves);
                        }
                    }
            }
        }
        return firstStepRelatesWithHisMoves;
    }

    private Cells[][] createBoardWithOnePlaceMove(Cells[][] severalMove, int attackX, int attackY) {
        Cells[][] clone = GameBoard.cloneBoard(severalMove);
        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < WIDTH; y++) {
                if (clone[x][y] == Cells.PLACE_MOVE)
                    clone[x][y] = Cells.EMPTY;
                if (x == attackX && y == attackY)
                    clone[x][y] = Cells.PLACE_MOVE;
            }
        return clone;
    }

    private int findCellBot(Cells[][] board, Cells find) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < WIDTH; y++) {
                if (board[x][y] == find) {
                    return x * 10 + y;
                }
            }
        }
        return -99;
    }

    private boolean check(Cells[][] first, Cells[][] sec) {
        return GameBoard.amountOfDifferences(first, sec) > 2;
    }

}
