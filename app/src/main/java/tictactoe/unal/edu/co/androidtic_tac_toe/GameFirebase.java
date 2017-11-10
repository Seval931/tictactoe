package tictactoe.unal.edu.co.androidtic_tac_toe;

/**
 * Created by CONTENIDOS on 08/11/2017.
 */

public class GameFirebase {
    private boolean gameOver;
    private String jugadorHost;
    private String jugador2;
    private String tablero;
    private String turno;


    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getJugadorHost() {
        return jugadorHost;
    }

    public void setJugadorHost(String jugador1) {this.jugadorHost= jugador1;}

    public String getJugador2() {
        return jugador2;
    }

    public void setJugador2(String jugador2) {
        this.jugador2 = jugador2;
    }

    public String getTablero() {
        return tablero;
    }

    public void setTablero(String tablero) {
        this.tablero = tablero;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }
}
