package main;

import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

import static main.MainController.WIDTH;
import static main.MainController.BOARD_EDGE_FOR_BLACK;
import static main.MainController.BOARD_EDGE_FOR_WHITE;

public class GameBoard {
    private static final int TWO_DIRECTION = 2;
    private static final int MAX_LONG_WAY_FOR_QUEEN = 7;
    private static final int MIN_LONG_WAY_FOR_CHECKER = 1;

    private int whiteScore;  //Количество срубленных шашек
    private int blackScore;

    private Cells[][] board;     //Модель игровой доски
    private Player turn;         //Чья очередь ходить

    private Cells[][] boardCopy; //Используется при выборе хода человеком
    private int oldPlaceX;       //Используется для обнаружения хода человека
    private int oldPlaceY;

    private boolean haveCommonMoves;     //Есть ли простые ходы для человека. Если нет, то есть атаки

    private int fromID;          //ID = 10*x + y; -- Идентификатиор хода игрока turn
    private int toID;
    private boolean attack;      //Ход человека - это атака?
    private boolean ready;       //Человек выбрал нужный для него ход

    private boolean againAttack = false; //Будет ли повторная атака этого игрока после его хода

    GameBoard(Cells[][] board, Player turn) {
        this.board = cloneBoard(board);
        this.boardCopy = cloneBoard(board);
        this.turn = turn;
    }

    Player getTurn() { return this.turn; }

    int getFromID() { return this.fromID; }

    int getToID() { return this.toID; }

    boolean isAttack() { return this.attack; }

    boolean getReady() { return this.ready; }

    int getWhiteScore() { return whiteScore; }

    int getBlackScore() { return blackScore; }

    Cells[][] getBoard() { return board; }

    Cells[][] moveHuman(int cordX, int cordY) {
        ready = false;
        if (turn == Player.HUMAN) {
            if (!againAttack) {
                if (getPlayer(board[cordX][cordY])==turn && canMakeOneAttack(board, turn)) {
                    haveCommonMoves = false;
                    oldPlaceX = cordX;
                    oldPlaceY = cordY;
                    boardCopy = possibleAttacks(cordX, cordY, board, turn);
                }
                else if (getPlayer(board[cordX][cordY])==turn && !canMakeOneAttack(board, turn)) {
                    haveCommonMoves = false;
                    oldPlaceX = cordX;
                    oldPlaceY = cordY;
                    boardCopy = possibleCommonMoves(cordX, cordY, board, turn);
                    if (boardContainsCell(boardCopy, Cells.PLACE_MOVE)) haveCommonMoves = true;
                }
            }

            if (boardCopy[cordX][cordY] == Cells.PLACE_MOVE) {
                ready = true;
                fromID = oldPlaceX*10 + oldPlaceY;
                toID = cordX*10 + cordY;
                if (haveCommonMoves) {
                    attack = false;
                    againAttack = false;
                    board = commonMove(oldPlaceX, oldPlaceY, cordX, cordY, board);
                    boardCopy = cloneBoard(board);
                    turnPlayer();
                }
                else {
                    attack = true;
                    blackScore++;
                    board = attackMove(oldPlaceX, oldPlaceY, cordX, cordY, board, turn);
                    againAttack = boardContainsCell(possibleAttacks(cordX, cordY, board, turn), Cells.PLACE_MOVE);
                    if (againAttack) {
                        oldPlaceX = cordX;
                        oldPlaceY = cordY;
                        boardCopy = possibleAttacks(cordX, cordY, board, turn);
                    } else {
                        boardCopy = cloneBoard(board);
                        turnPlayer();
                    }
                }
            }
        }
        return boardCopy;
    }

    void moveComputer() {
        ready = false;
        if (turn == Player.COMPUTER) {
            Bot black;
            if (againAttack) {
                black = new Bot(board, toID/10, toID%10, turn);
            } else black = new Bot(board, turn);
            black.bestMove();
            fromID = black.getFromID();
            toID = black.getToID();
            if (black.isAttack()) {
                attack = true;
                whiteScore++;
                board = attackMove(fromID/10, fromID%10, toID/10, toID%10, board, turn);
                againAttack = boardContainsCell(possibleAttacks(toID/10, toID%10, board, turn), Cells.PLACE_MOVE);
            }
            else {
                attack = false;
                againAttack = false;
                this.board = commonMove(fromID/10, fromID%10, toID/10, toID%10, board);
            }

            if (!againAttack) turnPlayer();
        }
    }

    static Cells[][] possibleAttacks(int fromPlaceX, int fromPlaceY,
                                     Cells[][] board, Player curPlayer) {
        Cells[][] clone = cloneBoard(board);
        if (getPlayer(clone[fromPlaceX][fromPlaceY]) != curPlayer) {
            return clone;
        }

        for (int i = 1; i <= TWO_DIRECTION; i++) {
            for (int j = 1; j <= TWO_DIRECTION; j++) {
                boolean flag = false;
                for (int g = 1; g < 7; g++) {
                    int toPlaceX = (int) (fromPlaceX + g * pow(-1, i));
                    int toPlaceY = (int) (fromPlaceY + g * pow(-1, j));
                    int newNextX = (int) (fromPlaceX + (g + 1) * pow(-1, i));
                    int newNextY = (int) (fromPlaceY + (g + 1) * pow(-1, j));
                    if (isOnBoard(toPlaceX, toPlaceY)) {
                        if (getPlayer(clone[toPlaceX][toPlaceY]) == curPlayer
                                || flag && clone[toPlaceX][toPlaceY] != Cells.EMPTY)
                            break;
                        if (flag) {
                            clone[toPlaceX][toPlaceY] = Cells.PLACE_MOVE;
                        }
                        if (getOpponent(clone[toPlaceX][toPlaceY]) == curPlayer) {
                            if (isOnBoard(newNextX, newNextY) && clone[newNextX][newNextY] == Cells.EMPTY) {
                                flag = true;
                            } else break;
                        }
                    } else break;
                    if ((clone[fromPlaceX][fromPlaceY] == Cells.WHITE ||
                            clone[fromPlaceX][fromPlaceY] == Cells.BLACK) && g == 2) break;
                }
            }
        }
        return clone;
    }

    static Cells[][] possibleCommonMoves(int fromPlaceX, int fromPlaceY,
                                         Cells[][] board, Player curPlayer) {
        Cells[][] clone = cloneBoard(board);
        if (getPlayer(board[fromPlaceX][fromPlaceY]) != curPlayer) {
            return clone;
        }
        for (int i=1; i <= TWO_DIRECTION; i++) {
            if (clone[fromPlaceX][fromPlaceY] == Cells.WHITE ||
                    clone[fromPlaceX][fromPlaceY] == Cells.BLACK) {
                int toPlaceX = (int) (fromPlaceX + pow(-1, i));
                int toPlaceY;
                if (curPlayer == Player.HUMAN) toPlaceY = fromPlaceY - 1;
                else toPlaceY = fromPlaceY + 1;
                if (isOnBoard(toPlaceX, toPlaceY) && clone[toPlaceX][toPlaceY] == Cells.EMPTY) {
                    clone[toPlaceX][toPlaceY] = Cells.PLACE_MOVE;
                }
            } else {
                for (int j=1; j <= TWO_DIRECTION; j++) {
                    for (int g = MIN_LONG_WAY_FOR_CHECKER; g <= MAX_LONG_WAY_FOR_QUEEN; g++) {
                        int toPlaceX = (int) (fromPlaceX + g * pow(-1, i));
                        int toPlaceY = (int) (fromPlaceY + g * pow(-1, j));
                        if (isOnBoard(toPlaceX, toPlaceY) && clone[toPlaceX][toPlaceY] == Cells.EMPTY) {
                            clone[toPlaceX][toPlaceY] = Cells.PLACE_MOVE;
                        } else break;
                    }
                }
            }
        }
        return clone;
    }

    static Cells[][] commonMove(int fromPlaceX, int fromPlaceY, int toPlaceX, int toPlaceY,
                                Cells[][] board) {
        Cells[][] clone = cloneBoard(board);
        clone[toPlaceX][toPlaceY] = clone[fromPlaceX][fromPlaceY];
        clone[fromPlaceX][fromPlaceY] = Cells.EMPTY;

        for (int i=0; i < WIDTH; i++) {
            for (int j=0; j < WIDTH; j++) {
                if (clone[i][j] == Cells.PLACE_MOVE) {
                    clone[i][j] = Cells.EMPTY;
                }
            }
        }

        if ((toPlaceY == BOARD_EDGE_FOR_WHITE || toPlaceY == BOARD_EDGE_FOR_BLACK) &&
                (clone[toPlaceX][toPlaceY] == Cells.WHITE || clone[toPlaceX][toPlaceY] == Cells.BLACK)) {
            if (toPlaceY == BOARD_EDGE_FOR_WHITE) clone[toPlaceX][toPlaceY] = Cells.WHITE_QUEEN;
            else clone[toPlaceX][toPlaceY] = Cells.BLACK_QUEEN;
        }

        return clone;
    }

    private static Cells[][] attackMove(int fromPlaceX, int fromPlaceY, int toPlaceX, int toPlaceY,
                                        Cells[][] board, Player curPlayer) {
        Cells[][] clone = cloneBoard(board);
        clone[toPlaceX][toPlaceY] = clone[fromPlaceX][fromPlaceY];
        clone[fromPlaceX][fromPlaceY] = Cells.EMPTY;

        for (int i=0; i < WIDTH; i++) {
            for (int j=0; j < WIDTH; j++) {
                if (clone[i][j] == Cells.PLACE_MOVE) {
                    clone[i][j] = Cells.EMPTY;
                }
            }
        }

        if (toPlaceY == BOARD_EDGE_FOR_WHITE && clone[toPlaceX][toPlaceY] == Cells.WHITE)
            clone[toPlaceX][toPlaceY] = Cells.WHITE_QUEEN;
        if (toPlaceY == BOARD_EDGE_FOR_BLACK && clone[toPlaceX][toPlaceY] == Cells.BLACK)
            clone[toPlaceX][toPlaceY] = Cells.BLACK_QUEEN;

        int directionX = (toPlaceX - fromPlaceX) / abs(toPlaceX - fromPlaceX);
        int directionY = (toPlaceY - fromPlaceY) / abs(toPlaceY - fromPlaceY);
        for (;;) {
            if (getOpponent(clone[fromPlaceX + directionX][fromPlaceY + directionY]) == curPlayer) {
                clone[fromPlaceX + directionX][fromPlaceY + directionY] = Cells.EMPTY;
                break;
            }
            directionX += directionX / abs(directionX);
            directionY += directionY / abs(directionY);
        }

        return clone;
    }

    static boolean canMakeOneAttack(Cells[][] board, Player curPlayer) {
        Cells[][] clone = cloneBoard(board);
        for (int i=0; i < WIDTH; i++) {
            for (int j=0; j < WIDTH; j++) {
                if (boardContainsCell(possibleAttacks(i, j, clone, curPlayer), Cells.PLACE_MOVE)) return true;
            }
        }
        return false;
    }

    static ArrayList<Cells[][]> consecutiveAttacksFromOnePosition(int fromX, int fromY,
                                                                  Cells[][] board, Player curPlayer) {
        Cells[][] clone = cloneBoard(board);
        ArrayList<Cells[][]> result = new ArrayList<>();
        for (int i=0; i < WIDTH; i++)
            for (int j=0; j < WIDTH; j++) {
                if (clone[i][j] == Cells.PLACE_MOVE) {
                    Cells[][] withoutCellOfAttack = attackMove(fromX, fromY, i, j, clone, curPlayer);
                    Cells[][] newBoard = possibleAttacks(i, j, withoutCellOfAttack, curPlayer);
                    if (boardContainsCell(newBoard, Cells.PLACE_MOVE)) {
                        result.addAll(consecutiveAttacksFromOnePosition(i, j, newBoard, curPlayer));
                    } else result.add(withoutCellOfAttack);
                }
            }
        return result;
    }

    private void turnPlayer() {
        if (turn == Player.COMPUTER) turn = Player.HUMAN;
        else turn = Player.COMPUTER;
    }

    static Cells[][] cloneBoard(Cells[][] mainBoard) {
        Cells[][] boardCopy = new Cells[WIDTH][WIDTH];
        for (int i=0; i < WIDTH; i++) {
            System.arraycopy(mainBoard[i], 0, boardCopy[i], 0, WIDTH);
        }
        return boardCopy;
    }

    static Player getOpponent(Cells cell) {
        if (cell == Cells.WHITE || cell == Cells.WHITE_QUEEN) return Player.COMPUTER;
        else if (cell == Cells.BLACK || cell == Cells.BLACK_QUEEN) return Player.HUMAN;
        else return null;
    }

    static Player getOpponent(Player player) {
        if (player == Player.COMPUTER) return Player.HUMAN;
        else if (player == Player.HUMAN) return Player.COMPUTER;
        else return null;
    }

    private static Player getPlayer(Cells cell) {
        if (cell == Cells.WHITE || cell == Cells.WHITE_QUEEN) return Player.HUMAN;
        else if (cell == Cells.BLACK || cell == Cells.BLACK_QUEEN) return Player.COMPUTER;
        else return null;
    }

    static boolean boardContainsCell(Cells[][] board, Cells cell) {
        for (Cells[] cells : board)
            for (Cells element : cells)
                if (element == cell) return true;
        return false;
    }

    static int amountOfDifferences(Cells[][] first, Cells[][] second) {
        int count = 0;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (first[i][j] != second[i][j]) count++;
            }
        }
        return count;
    }

    private static boolean isOnBoard(int x, int y) {
        return (x >= 0 && x < WIDTH && y >= 0 && y < WIDTH);
    }

}
