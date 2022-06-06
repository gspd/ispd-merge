/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.componenteauxiliar;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author denison
 */
public class Torre extends JPanel implements MouseMotionListener, MouseListener, Runnable {

    public static int TAMANHO = 300;
    private Random sorteio = new Random();
    private Cursor cursor;
    private int cursorX;
    private int cursorY;
    private int dinheiro;
    private int vida;
    private int nivel;
    private int nivelT;
    private int mapaAtual;
    private boolean iniciar;
    private boolean pause;
    private ArrayList<Elemento> itens;
    private ArrayList<Elemento> mapa;
    private ArrayList<Elemento> opcoes;
    private Dimension tamanhoTela;
    private ArrayList<Elemento> inimigos;
    private int tipoInimigos;
    private Elemento selecionado;

    public Torre() {
        addMouseListener(this);
        addMouseMotionListener(this);
        //cursor = Toolkit.getDefaultToolkit().createCustomCursor(opcoes.get(0).getImage(), new Point(this.getX(),this.getY()), "img");
        cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        setCursor(cursor);
        dinheiro = 500;
        vida = 100;
        nivel = 1;
        nivelT = 0;
        mapaAtual = 1;
        pause = false;
        sorteio = new Random();
        tipoInimigos = 0;
        tamanhoTela = new Dimension(TAMANHO, TAMANHO);
        mapa = new ArrayList<Elemento>();
        itens = new ArrayList<Elemento>();
        opcoes = new ArrayList<Elemento>();
        for (int i = 0, x = 5; i < Elemento.OPCOES; i++, x += 60) {
            opcoes.add(new Elemento(x, 20, i));
        }
        inimigos = new ArrayList<Elemento>();
        for (int j = 0; j < 10; j++) {
            Elemento el = new Elemento(-50, -50, Elemento.OPCOES);
            inimigos.add(el);
        }
        Thread thread = new Thread(this);
        thread.start();
        Thread animacao = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (vida > 0) {
                        Thread.sleep(150);
                        for (Elemento elemento : itens) {
                            elemento.atualizarDesenho();
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Torre.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        animacao.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //Desenha cor da tela
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, TAMANHO, TAMANHO);
        // Desenha opções
        if (vida >= 100) {
            g.setColor(Color.GREEN);
            g.drawString(" :D " + vida, 120, 12);
        } else if (vida > 50) {
            g.setColor(Color.BLUE);
            g.drawString(" :) " + vida, 120, 12);
        } else if (vida > 10) {
            g.setColor(Color.RED);
            g.drawString(" :( " + vida, 120, 12);
        } else {
            g.setColor(Color.RED);
            g.drawString(" X( " + vida, 120, 12);
        }
        g.setColor(Color.BLACK);
        g.drawString(" $ " + dinheiro + "     Nivel " + nivelT, 10, 12);
        g.drawLine(0, 60, TAMANHO, 60);
        for (Elemento item : opcoes) {
            item.draw(g);
            g.drawString("$:" + item.valor, item.x + 20, item.y + 10);
        }
        //Desenha mapa
        desenharMapa(g);
        synchronized (itens) {
            // Desenhamos todos itens
            for (Elemento item : itens) {
                item.draw(g);
            }
        }
        // Desenha inimigos
        for (Elemento item : inimigos) {
            item.draw(g);
        }
        //Opções fora do jogo
        if (!iniciar) {
            g.setColor(Color.BLACK);
            g.drawRect(TAMANHO / 2 - 50, TAMANHO - 100, 100, 50);
            g.drawString("Iniciar", TAMANHO / 2 - 20, TAMANHO - 70);
        }
        if(selecionado != null && cursorY > 60 && dinheiro >= selecionado.valor) {
            selecionado.drawCursor(cursorX - selecionado.width / 2,cursorY - selecionado.height / 2, g);
        }
        if (vida > 0) {
            repaint();
        } else {
            g.setColor(Color.RED);
            g.drawString(" Fim de jogo! ", TAMANHO / 2 - 20, TAMANHO - 70);
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        if (me.getY() < 60) {
            for (Elemento item : opcoes) {
                item.selecionado = false;
            }
            selecionado = null;
            for (Elemento item : opcoes) {
                if (item.contains(me.getX(), me.getY())) {
                    item.selecionado = true;
                    selecionado = item;
                }
            }
        } else if (me.getY() > 60) {
            if (selecionado != null && dinheiro >= selecionado.valor) {
                boolean colisao = false;
                for (Elemento elemento : itens) {
                    if (elemento.contains(me.getX(), me.getY())) {
                        colisao = true;
                    }
                }
                if (!colisao) {
                    Elemento el = new Elemento(me.getX() - selecionado.width / 2, me.getY() - selecionado.height / 2, selecionado.tipo);
                    el.valor = el.getPoder();
                    itens.add(el);
                    dinheiro -= selecionado.valor;
                    selecionado.selecionado = false;
                    selecionado = null;
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (me.getButton() == MouseEvent.BUTTON2) {
            dinheiro += 100;
        }
        if (me.getButton() == MouseEvent.BUTTON1 && !iniciar) {
            if (me.getX() > TAMANHO / 2 - 50
                    && me.getX() < TAMANHO / 2 + 50
                    && me.getY() > TAMANHO - 100
                    && me.getY() < TAMANHO - 50) {
                iniciar = true;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        pause = false;
    }

    @Override
    public void mouseExited(MouseEvent me) {
        pause = true;
    }
    
    @Override
    public void mouseDragged(MouseEvent me) {
        
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        cursorX = me.getX();
        cursorY = me.getY();
    }
    
    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return tamanhoTela;
    }

    @Override
    public void run() {
        try {
            int mudarNivel = 100;
            while (vida > 0) {
                Thread.sleep(200);
                while (!pause && vida > 0) {
                    Thread.sleep(40);
                    //Teste para mudança de nível
                    if (mudarNivel >= 100 && nivel < Elemento.TOTALINIMIGOS) {
                        mudarNivel = 0;
                        iniciarNovo();
                    }
                    //Movimentação dos inimigos
                    boolean novoDisparo = true;
                    for (Elemento item : inimigos) {
                        if (item.selecionado) {
                            item.x += (item.getVelocidade() / 2) * item.tiroX;
                            item.y += (item.getVelocidade() / 2) * item.tiroY;
                            novoDisparo = false;
                        } else if (item.tiroX != 0 || item.tiroY != 0) {
                            item.x += item.getVelocidade() * item.tiroX;
                            item.y += item.getVelocidade() * item.tiroY;
                            novoDisparo = false;
                        }
                        if (item.valor <= 0) {
                            dinheiro++;
                            mudarNivel++;
                            item.parar();
                        } else if (item.x > TAMANHO || item.y > TAMANHO) {
                            vida -= 5;
                            mudarNivel++;
                            item.parar();
                        }
                    }
                    if (novoDisparo) {
                        reiniciarInimigos();
                    }
                    //Tiro dos canhões
                    for (Elemento item : itens) {
                        Elemento el = item.getAlvo(inimigos);
                        if (el != null) {
                            item.tiroX = el.x + el.width / 2;
                            item.tiroY = el.y + el.height / 2;
                            //vida - poder
                            el.valor -= item.valor;
                            if (item.tipo == Elemento.CANHAO_GELO) {
                                el.selecionado = true;
                            }
                        } else {
                            item.tiroX = item.x + item.width / 2;
                            item.tiroY = item.y + item.height / 2;
                        }
                    }
                    //Mudar trajetória dos inimigos de acordo com mapa
                    for (Elemento item : mapa) {
                        item.mudar(inimigos);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Torre.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void iniciarNovo() throws InterruptedException {
        dinheiro += 200;
        iniciar = false;
        nivelT++;
        mapaAtual++;
        if (mapaAtual > 4) {
            mapaAtual = 1;
        }
        for (Elemento elemento : itens) {
            dinheiro += elemento.getValor();
            vida++;
        }
        synchronized(itens){
            itens.clear();
        }
        while (!iniciar) {
            Thread.sleep(200);
        }
        mapa.clear();
        tipoInimigos = 0;
        reiniciarInimigos();
        switch (mapaAtual) {
            case 1:
                nivel++;
                break;
            case 2:
                mapa.add(new Elemento(TAMANHO / 2, 80, 20, 100, 0, 1));
                break;
            case 3:
                mapa.add(new Elemento(TAMANHO / 2, 80, 20, 100, 0, 1));
                mapa.add(new Elemento(10, TAMANHO - 20, TAMANHO - 20, 20, 1, 0));
                break;
            case 4:
                mapa.add(new Elemento(10, 80, 30, 100, 0, 1));
                mapa.add(new Elemento(10, TAMANHO - 30, 50, 30, 1, 0));
                mapa.add(new Elemento(200, TAMANHO - 30, 50, 30, 0, -1));
                mapa.add(new Elemento(190, 80, 50, 30, 1, 0));
                break;
        }
    }

    private void reiniciarInimigos() {
        int dec = 0;
        for (Elemento item : inimigos) {
            if (mapaAtual <= 3) {
                item.x = dec -= 10;
                item.y = 80 + sorteio.nextInt(80);
                item.setTipo(Elemento.OPCOES + tipoInimigos);
            }
            switch (mapaAtual) {
                case 4:
                    item.x = dec -= 10;
                    item.y = 80 + sorteio.nextInt(40);
                    item.setTipo(Elemento.OPCOES + tipoInimigos);
                    break;
            }
        }
        tipoInimigos++;
        if (tipoInimigos >= nivel) {
            tipoInimigos = 0;
        }
    }

    private void desenharMapa(Graphics g) {
        g.setColor(Color.GREEN);
        switch (mapaAtual) {
            case 1:
                g.drawLine(0, 80, TAMANHO, 80);
                g.drawLine(0, 170, TAMANHO, 170);
                break;
            case 2:
                g.drawLine(0, 80, TAMANHO / 2 + 10, 80);
                g.drawLine(0, 170, TAMANHO / 2 - 10, 170);
                g.drawLine(TAMANHO / 2 + 10, 80, TAMANHO / 2 + 10, TAMANHO);
                g.drawLine(TAMANHO / 2 - 10, 170, TAMANHO / 2 - 10, TAMANHO);
                break;
            case 3:
                g.drawLine(0, 80, TAMANHO / 2 + 20, 80);
                g.drawLine(0, 170, TAMANHO / 2 - 10, 170);
                g.drawLine(TAMANHO / 2 + 20, 80, TAMANHO / 2 + 20, TAMANHO - 30);
                g.drawLine(TAMANHO / 2 - 10, 170, TAMANHO / 2 - 10, TAMANHO);
                g.drawLine(TAMANHO / 2 + 20, TAMANHO - 30, TAMANHO, TAMANHO - 30);
                break;
            case 4:
                g.drawLine(0, 80, 40, 80);
                g.drawLine(0, 140, 5, 140);
                g.drawLine(40, 80, 40, TAMANHO - 35);
                g.drawLine(5, 140, 5, TAMANHO);
                g.drawLine(40, TAMANHO - 35, 190, TAMANHO - 35);
                g.drawLine(190, TAMANHO - 35, 190, 80);
                g.drawLine(230, TAMANHO, 230, 120);
                g.drawLine(190, 80, TAMANHO, 80);
                g.drawLine(230, 120, TAMANHO, 120);
                break;
        }
    }

    private static class Elemento extends Rectangle {
        //VALORES:
        //{posX, posY, cor ,custo/vida, alcance/velocidade, poder}

        private static final Object[][] VALORES = {
            //Canhões
            {25, 28, Color.MAGENTA, 100, 20, 2},
            {33, 33, Color.RED, 125, 15, 4},
            {33, 33, Color.GREEN, 200, 50, 2},
            {33, 33, Color.GRAY, 500, 10, 20},
            {25, 28, Color.BLUE, 100, 10, 1},
            //Inimigos
            {5, 5, Color.ORANGE, 100, 2},
            {3, 3, Color.BLACK, 150, 4},
            {8, 8, Color.PINK, 200, 10},
            {20, 20, Color.YELLOW, 2000, 4}};
        protected static final int CANHAO_VIOLET = 0;
        protected static final int CANHAO_RED = 1;
        protected static final int CANHAO_GREEN = 2;
        protected static final int CANHAO_GRAY = 3;
        protected static final int CANHAO_GELO = 4;
        private static final int OPCOES = 5;
        private static final int TOTALINIMIGOS = 4;
        protected int tipo;
        protected boolean selecionado;
        protected int valor;
        protected int tiroX, tiroY;
        private static final byte[] blue0 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8, 6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0, -47, 73, 68, 65, 84, 72, -119, -27, -43, -53, 21, -125, 64, 8, 5, 80, -56, 73, 97, -42, 98, 37, 86, 98, 45, -23, 108, 92, 36, -26, 0, -14, 121, -104, -104, 77, -40, -87, -64, 21, 71, 29, -94, 127, -114, -47, 45, -32, 15, 0, -72, -74, -125, 120, 19, 64, -11, -73, 22, 48, -65, -100, 121, -24, -13, 95, 64, 52, -80, 71, 3, -86, 16, 31, 104, 66, -39, 51, 61, 2, -85, 72, -9, -49, -69, -3, -94, 73, 18, 96, 100, -96, 59, -111, 39, -41, -128, 44, 5, 38, -78, 8, 8, -12, 32, -119, 36, -117, -105, -83, 107, -6, -87, -80, -105, 113, -7, 36, 77, -24, -4, -102, -128, 16, 14, 100, 72, 1, -31, 64, -123, -60, 80, 3, 32, -86, 127, 43, 108, 26, -23, 0, 0, 4, -119, 33, 16, -128, 18, 68, -100, -34, 79, 126, -66, 51, -38, 59, -51, 94, -17, -4, -43, 55, 61, -44, 87, -74, 76, -49, -125, -27, 81, 66, 33, -32, -11, 96, 123, 113, 15, -111, 116, -72, 51, -39, 84, -43, 4, 61, -104, -120, -122, -67, -24, -123, -121, -94, 117, -9, 58, 13, 111, 24, -59, -5, 113, 93, 25, 27, 43, -30, 97, -100, 64, 39, 72, 52, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] blue1 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8, 6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0, -28, 73, 68, 65, 84, 72, -119, -51, -107, 81, 18, -125, 48, 8, 68, -105, 78, 15, -42, -77, -28, 36, -98, -60, -77, -12, 102, -12, -93, -38, 38, 27, 52, -128, -83, -29, -2, 48, 26, -32, 65, 48, 6, -72, -88, 52, 26, 32, 7, 0, -18, -40, 8, -60, -22, -64, 21, 127, 11, 1, -54, -62, 41, -38, -66, -1, 1, -92, 5, 0, -64, 44, 33, -48, 8, -46, 39, 40, 102, -50, 93, -48, 30, 68, 27, 51, -45, -10, -81, -49, 54, -44, 13, -7, 2, -72, -48, 126, 54, -122, -109, 27, 66, -30, 110, -100, -118, 65, 24, -26, -36, -78, -67, -46, 52, 113, -10, 76, -89, 120, 39, 92, 117, -47, 97, 39, -71, -19, -30, -63, 15, 102, 21, -125, 56, 62, -41, 4, 68, -56, 46, -30, -63, -101, 78, 62, -120, 52, -58, -102, 69, 15, 10, 67, 42, 66, 37, 59, 105, -18, 0, -111, 20, -128, -94, 104, 107, -99, 127, -31, 83, -18, -109, 83, 110, -58, -116, 14, -35, -15, 28, -100, -87, -44, -52, 33, -11, -30, -12, 120, 63, 76, -49, 20, 104, 51, -121, -16, -30, -86, -54, -87, -85, -84, 78, -38, -60, 108, -28, 16, 0, -54, -117, -106, 44, -88, 55, -18, 62, 118, -13, 39, -36, -46, 103, -69, -2, -87, 23, 2, -67, 79, -31, 30, -67, 32, 126, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] blue2 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8, 6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0, -58, 73, 68, 65, 84, 72, -119, -27, -107, -39, 17, -61, 32, 12, 68, -91, 76, 10, 75, 45, -82, -60, -107, -92, -106, 116, 70, 62, -16, 1, 88, 98, 87, -15, 49, -98, -55, -2, 120, 108, 29, 79, 8, 97, 68, -2, 89, 41, 26, -96, 59, 0, 116, 108, 4, 98, -83, -128, -118, 127, -124, 0, -61, -60, 25, 82, -3, -3, 0, 72, 13, -104, 21, 0, 33, 8, -69, -55, 93, 63, 98, 37, -120, -125, -21, 96, -9, 100, -105, 110, 8, 121, 107, -3, 36, -123, -68, 83, -18, 121, -49, 109, -79, -69, 78, -4, 74, -4, 17, -122, -118, -75, 107, 123, 24, 79, -128, -4, -88, -69, 64, -48, 108, -32, 73, 67, 16, 118, 86, -69, 126, 76, -69, 114, -126, -10, 108, -84, -17, -80, -112, 75, -18, -109, -53, 111, -58, -74, 82, 47, 73, -17, 23, 96, -26, -48, -46, 56, -66, -14, -53, -8, -127, 32, 23, 96, -27, -48, -42, 56, -85, 112, -38, 84, 86, 38, -83, 98, -100, 28, 42, 34, -87, 53, 90, -78, -96, 108, -36, 19, -69, -15, 9, 61, 45, -19, 58, 83, 95, 11, 31, 51, 105, 42, 126, 107, -14, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] red0 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1, 28, 73, 68, 65, 84, 88, -123, -19, -104, 81, 14, 2, 33, 12, 68, 11, 39, -13, 104, 123, 52, 111, -122, 63, -82, 97, -53, 76, -95, 45, 26, 77, 108, 98, 12, -124, -19, -68, -76, 118, 68, 69, -14, -47, -78, 9, -54, 70, -128, 112, -82, 12, 4, -86, 64, 40, 95, 20, -62, 106, -127, 59, 103, -51, 0, 52, -67, 1, -105, -5, 33, 6, 0, -78, 118, -127, 120, 32, 40, 64, 22, 100, -75, 127, 16, -96, -96, 3, 99, -46, -87, -122, -85, 29, 72, -88, -87, -75, 62, -73, 18, 46, -120, 66, -60, 52, -100, 119, 60, 66, -107, 96, 34, -70, 58, 111, -127, -24, 5, -40, 116, 68, 60, 60, -30, 19, -37, -29, 15, 113, -58, 111, 66, 20, -11, 62, -37, -33, 1, 113, -7, -80, -49, 70, -48, 24, 97, 115, 104, 44, -120, -31, -63, 38, -40, 53, -75, -127, 17, 69, 10, -62, 32, -32, 3, -56, 45, 81, 117, -116, -106, -64, -68, 8, -94, -119, -120, 28, 55, -84, -65, 98, -37, 104, -39, -27, 27, 64, 52, 4, 2, 48, 65, -32, 1, 114, -116, -127, -12, 16, 22, -64, 20, 100, -11, -21, 27, -127, -44, 126, 99, 2, 64, 65, -68, -9, 7, 13, 82, -99, 0, 51, -95, 101, -101, -24, 65, 106, 0, -128, 9, -70, 125, -22, -44, -115, -38, -74, -98, -48, -24, 85, 34, 5, -127, 64, -62, 63, 7, -61, 16, -49, 82, -66, -84, 34, -40, -46, 101, -120, 6, 94, 23, 16, 5, 64, -49, -77, 40, 32, -55, 71, -29, -72, 127, -55, 125, -94, -56, -122, -1, 23, -78, -15, 0, 89, -87, 89, 32, 112, 104, 89, -45, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] red1 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1, 17, 73, 68, 65, 84, 88, -123, -19, -42, 81, 14, -61, 32, 8, 6, 96, -12, 100, 59, 90, -113, -74, -101, -79, -105, -51, 88, -4, 65, 64, 99, -74, 100, 36, 77, 99, 87, -31, 91, 55, -87, 68, -7, -32, -123, -71, -73, 40, -101, 0, -39, 60, 68, 68, 84, 87, 0, 12, -82, -99, 66, -36, 42, 10, 72, 10, 19, 69, -64, 34, -20, -72, 103, 39, 66, -83, -80, 2, -119, 32, -90, -119, -77, 16, 47, -126, -5, -84, 69, -100, 103, -13, 118, 33, 6, -128, 28, -93, -49, -68, -31, 70, -12, -59, 88, -100, 87, 0, 33, 4, -109, -2, -8, 87, 0, 33, 68, -113, 65, -29, -107, 110, -107, 106, 86, -69, -29, -113, -8, -60, 111, 34, -28, 10, 9, 52, -82, 52, -94, -3, -23, 11, -51, -33, 27, 6, -60, 92, 60, 22, -126, -75, 1, -6, -10, 43, 16, 13, 49, 76, -48, -38, 53, -38, 91, 68, 33, 8, -63, 68, 68, -41, 99, 52, -56, -62, -114, 6, -43, 60, 93, -66, 97, -102, 68, 32, 0, -124, -104, 21, -31, 80, -121, -12, 8, 11, -96, 38, -10, 2, 44, 72, -19, 47, 76, 0, 106, 5, 47, 64, -125, -44, 40, -96, 47, 82, 18, 0, 4, -87, 65, 0, 42, 86, 8, 122, -4, -112, 108, -37, -42, 86, -29, -111, 45, -65, 21, -23, 45, 69, 26, -15, 126, -108, -83, 101, 36, 126, -46, 16, -126, -63, 113, -125, 8, -128, 122, -65, 22, 5, 36, 57, 26, -41, -13, 75, -10, 19, -42, 27, -6, 88, -68, 0, 69, 86, 89, 30, -39, -114, 40, 78, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] green0 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0, -11, 73, 68, 65, 84, 88, -123, -59, -41, 81, 14, -125, 32, 12, 6, -32, -42, -20, 76, -37, -85, 87, -40, 78, -60, -119, -12, 10, 123, -43, 67, -55, 30, 22, 26, 10, -44, -96, -108, -14, 63, 17, -125, -12, 19, 77, -92, 8, 60, 30, -20, -126, -39, -64, 24, -64, -22, 7, 4, 1, -90, -49, -85, 125, -27, -9, 51, -69, -26, -41, -99, -58, -57, -78, -79, -23, 104, 13, 40, 65, 8, -111, 2, 18, -83, 74, -92, 26, -113, -46, -28, 99, -39, -40, 13, -91, -89, -85, 73, -70, 3, -23, -70, -124, -77, 2, 0, -4, 119, -94, -76, -61, 25, 34, -114, 38, -32, 44, 34, -62, 10, 32, 34, 44, 1, 34, -62, 18, -96, -122, 104, 1, -88, 32, 90, 1, -51, 8, 13, 64, 19, 66, 11, 112, 27, -95, 9, -72, -123, -48, 6, 92, 70, -12, 0, 92, 66, -12, 2, 84, 35, 122, 2, -86, 16, -67, 1, 25, 34, -3, 103, 88, 0, 24, 98, 20, -128, 33, 70, 1, 68, -124, 117, 24, -62, -81, -69, 120, 4, -45, -120, 116, -58, -92, -125, 110, -4, 10, 122, 65, -92, -106, 66, -67, -17, -88, 77, -38, 119, -128, 53, -92, -44, -127, -123, 12, -21, 69, 39, 55, 83, 113, 60, -103, -36, 13, -32, 102, -16, -31, -61, -12, 0, -128, 110, -74, 19, -72, 47, -43, 5, -116, 118, 98, 88, 126, -9, -70, 118, -106, 72, -78, -62, -113, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] green1 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1, 15, 73, 68, 65, 84, 88, -123, -59, -41, -63, 17, -125, 32, 16, 5, -48, 69, 83, 83, 114, -91, -123, -28, 110, 9, -10, 64, 15, -106, -32, 93, 91, -56, 85, 123, 82, 114, 72, -40, 17, 4, -126, -78, -84, -1, -60, 40, 51, -5, 100, 24, 103, 87, -128, 29, 13, 124, 17, -69, 5, 51, -64, -86, 111, 16, 8, -88, 94, -113, 50, -43, -98, 119, 92, 47, 77, 103, -67, 18, -36, 0, 31, 4, 17, 46, 96, 29, 38, 114, 76, -35, -73, -72, -42, -29, -116, 53, 110, -66, -51, -21, 48, 37, -99, -118, -17, 11, 99, 89, -102, 14, -22, -66, 5, 61, -50, -42, -13, -118, 11, 0, -16, 61, 9, -25, 62, -8, 17, 41, 57, 3, -120, -27, 48, -126, 26, 112, 24, -111, 11, 112, -17, -62, 97, 68, 41, 64, 50, -94, 36, 32, 9, 81, 26, -16, 23, -63, 1, -120, 34, -72, 0, 65, 4, 39, 32, -120, -32, 4, -112, 35, -50, 0, 72, 17, 103, 1, 100, -120, 28, 0, 9, 34, 23, -112, -115, -96, 0, 100, 33, -88, 0, 59, 68, -22, -1, -127, 18, 96, 33, -82, 2, 88, 8, -109, 80, 11, 70, 1, 8, -75, -114, -34, 70, 55, 6, -55, 73, -88, 119, -75, -26, 14, -45, -110, -105, 56, 114, 55, -37, -111, -126, 109, 2, 11, 1, -32, 55, -127, -103, 92, 54, -117, 86, 74, 98, 113, 17, -39, 92, 12, -96, 36, 104, 115, 49, 53, 0, 8, 37, -7, 4, -22, -115, 117, 65, 108, 78, -30, -78, 124, 0, -37, 81, 114, 94, 49, 105, 108, 112, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] green2 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0, -4, 73, 68, 65, 84, 88, -123, -59, -41, 65, 18, -125, 32, 12, 5, -48, 31, -89, 103, 106, -73, 94, -63, -98, -120, 19, -47, 43, 116, 107, 15, 5, 93, -88, -116, 82, 64, -127, 36, -3, 43, -57, 97, -56, 51, 58, 78, 32, 28, -29, -95, 23, -6, -71, 80, 6, 28, -22, 111, -120, 0, 24, -98, 15, -15, -54, -50, -50, 7, 8, 105, 3, 82, -112, -128, -120, 1, -111, -106, 37, -71, 26, -73, -44, 98, 103, 103, -111, -82, -20, -9, -91, -23, 14, -84, -120, 65, 11, 0, 44, -99, 112, 118, 94, 0, -5, -5, 34, -43, 42, -93, -118, -120, 59, -96, -114, -56, 1, -44, 16, 41, -128, 127, 125, -12, 16, 103, 0, 113, 68, -23, 21, -88, 32, -82, 2, -60, 16, 53, 0, 17, 68, 45, -128, 29, -47, 2, 96, 69, -76, 2, -40, 16, 61, 0, 22, 68, 47, -96, 27, -63, 1, -24, 66, 112, 1, -102, 17, -83, -128, -8, 119, -35, -116, -32, 6, 84, 35, 36, 0, 73, -60, 54, -126, 73, 0, 114, -93, 99, 114, -48, 77, 66, 24, -90, -17, -36, -20, -102, 61, 119, -108, -98, -4, -84, -67, 87, 18, -97, 59, 16, 67, 52, 1, 88, 79, 96, 97, -1, -18, -35, -21, 67, 0, 48, -104, 49, 20, -89, -62, 98, 49, -128, 25, -31, -73, 15, -45, 3, 32, 51, -22, 9, -52, 59, -44, 5, -19, 58, -15, -73, 124, 1, -120, 79, 91, 100, 51, -68, 114, 65, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] gray0 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1, -91, 73, 68, 65, 84, 88, -123, -19, 87, 81, -110, -61, 32, 8, 125, -20, -12, 94, -15, 104, 28, -115, -100, -52, -3, 104, -80, -120, -104, -112, 100, -37, -23, -57, -66, 25, -89, 70, 17, 30, 8, 106, -127, 127, 124, 17, -24, -124, 108, 125, -105, -2, -97, 3, -93, -74, -95, -42, -102, 106, 34, 50, -43, 113, -122, 105, -73, -96, -42, -13, 65, 88, -41, 21, 0, 80, 74, -55, -38, 28, 8, 84, 5, -116, 39, 89, -120, 72, -73, 78, 68, -4, -40, 46, -85, 22, 118, -21, 81, -32, 77, 24, 29, -94, -47, 73, 102, 6, -16, -118, -120, -47, -43, -124, 109, 78, 28, 18, -48, 61, 87, -125, -66, -87, 81, -109, 19, -115, -124, -114, -71, 124, -23, -40, -92, 8, -40, 57, 69, 41, -91, 25, 114, -98, 118, 125, 102, -98, -51, 17, 33, 81, 122, 81, -24, 109, -30, -7, -112, 91, 68, 99, 30, 15, 60, -93, -47, 34, 17, -19, 107, 52, 102, 67, -82, -58, 50, 6, -127, 103, 84, 76, -12, 58, -27, 67, 5, -8, 44, -65, -45, -104, 121, 90, 37, 54, 49, 41, -14, -38, 122, 108, 19, 115, 6, 17, 105, -51, 122, -82, -120, -86, -29, -31, 116, 16, -128, 74, 68, -45, 18, 84, 34, 71, 57, -95, 125, 59, 23, 17, -120, 72, -124, 68, 68, 36, -67, -33, 22, 25, 2, -64, -4, -18, 24, -74, 70, -61, -85, 99, -53, -78, 116, 11, 102, -91, 121, 68, 96, -113, 68, 71, 68, 13, 122, 34, 17, -20, 54, 100, 8, 0, -15, 118, 120, 34, 97, -114, 16, 81, 88, -90, 103, 9, -100, 65, 120, -87, 105, -97, -103, -121, 82, -124, 43, -61, -65, -62, -12, 118, -3, 20, -127, 67, 34, -97, 34, 48, 37, 114, -105, -64, 94, 117, -52, 16, -98, -84, 119, -110, -16, 78, -42, 70, 30, 95, -46, 23, 45, -86, 59, 115, 123, 68, -50, -56, -17, -54, 86, -9, -101, 49, 62, -21, -89, -11, 15, -49, -69, 13, 87, -68, -54, 36, -92, -43, 59, 92, -27, -107, 11, -64, 37, -83, -52, -53, -42, -19, 59, -125, -63, 86, -117, 4, -53, -96, -36, -106, 91, -8, 39, 70, 101, 29, -127, 72, -10, 117, -98, -108, -34, 30, 16, 60, -7, -67, 71, 44, -51, -40, 37, 68, -21, 55, 2, -31, -109, 31, 0, -56, 50, 124, 7, 60, 1, -3, 120, -5, 17, 123, -124, 95, 102, -54, -57, -21, -58, -110, 94, 109, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] gray1 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 33, 0, 0, 0, 33, 8, 6, 0, 0, 0, 87, -28, -62, 111, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 1, -66, 73, 68, 65, 84, 88, -123, -19, -105, 81, -110, -61, 32, 8, -122, 127, 59, -67, 87, 114, -77, 112, 52, 114, 50, -10, -95, -59, 5, 2, -58, 100, -89, 59, -5, -80, -1, -116, -109, 104, 20, 62, 81, -87, 5, -2, -11, -121, -44, 38, -5, -55, 7, 109, -105, 29, 15, 78, -119, 104, 104, 104, 93, 87, 0, -64, -78, 44, 104, 45, 53, 91, 66, 101, 31, 28, -64, -103, -13, 10, 4, 64, 6, 51, 69, 39, 0, -64, -52, -50, -24, 12, 76, 21, 9, 17, -119, 64, 67, 50, 7, -96, -17, -103, -13, -39, 54, -75, -107, 68, 38, -115, -120, 0, 16, 102, -18, -123, -120, 68, -37, -19, 55, -37, 22, 11, 17, -119, -68, -90, 126, 24, -89, 50, -33, -70, -98, 25, -47, 72, -52, -20, -94, 69, 68, -82, 14, -8, 37, -48, 101, 98, 102, 44, -53, 98, -65, 41, 72, 107, -111, -86, 114, 92, -75, 89, 8, 13, -69, 85, 113, 82, -100, -98, 120, -83, -113, -24, 12, -78, 65, 118, -125, -86, -78, 61, 48, -29, 80, -57, -102, -15, -19, -42, -58, -68, 35, 93, -114, -105, 87, -65, 65, 31, -90, 95, 3, -114, -77, -74, 16, 113, 63, 100, 18, -111, 94, -20, -72, -18, 36, 57, 33, -61, -116, 105, -99, 70, 24, -5, -84, -10, -123, 58, -76, 16, 102, -110, -35, -73, -115, -124, 85, -113, -118, -106, -69, -53, 114, 6, 48, -126, 112, 32, 42, 5, -47, -74, -72, 116, 89, 20, -76, -67, 2, 56, -125, 112, 32, -37, -74, -91, 32, -23, -96, 55, 0, 17, -59, 52, -98, 46, -1, 25, 68, 31, -88, -119, -57, 58, -49, 64, 44, -64, -74, 109, -89, 0, 87, -107, -90, 111, 77, -59, -74, 45, 73, -33, 67, 93, 73, -37, -70, 52, 46, -97, -24, 76, 99, 84, -10, 125, -65, 96, -6, -98, -54, 31, 53, 34, -118, 109, -65, 11, 114, 23, 96, 102, 99, 86, 74, 51, -20, -24, 40, 14, 13, -3, 80, -39, -116, 47, -39, 29, -35, 49, 103, 12, 93, 5, -104, -78, 45, -31, 57, -29, -68, 122, -97, -74, -1, 72, 58, 0, -41, -94, -48, 66, 125, -92, -61, -43, -63, 66, 8, -83, 0, -83, -45, -58, 98, 95, 121, -41, 103, 116, -16, -43, 35, 65, 124, 48, 110, -113, 89, -68, -44, -70, -66, 1, 32, -21, -5, -99, 71, 86, -17, 15, 72, -2, 119, -60, 25, 17, 119, 103, -73, -108, -115, 127, 3, -108, -9, -119, 102, 9, 63, -95, 8, -96, -107, -113, -89, -42, 51, 125, 1, -12, 83, 73, 96, 76, -110, 92, -110, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] violet0 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8, 6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0, -21, 73, 68, 65, 84, 72, -119, -19, -106, 73, 14, -61, 48, 8, 69, -79, -43, -125, -11, 102, 118, 110, -106, -101, -47, 77, -79, 108, -14, 25, 28, 85, 93, 5, 41, 82, 68, -32, 63, -16, -104, 66, 121, 99, -32, 43, -103, -60, 84, -112, 0, 58, -11, -31, -104, -34, 67, -115, 12, -28, 2, -40, 5, -67, -18, 0, 26, 53, 13, 98, 15, 84, 3, -120, 11, 104, -44, 96, -121, 59, -112, -53, 68, -49, 0, -32, 67, 11, 35, -124, -92, -86, -52, -60, 89, 16, -77, -86, -64, 96, -98, -39, 73, -74, -117, 76, 124, 56, -15, -65, -80, 7, -14, 64, 86, -5, -57, 18, -122, -121, -35, 65, 71, -28, -125, 121, -107, -100, -35, -115, -86, -101, 69, -27, 61, -24, -102, -27, -88, 71, 71, 117, -79, 10, 64, 29, -127, 124, -47, 93, -122, 11, 10, 70, 115, -29, 124, 31, 122, -107, -120, -88, -65, 77, 80, -15, -124, -100, -101, -111, 103, -35, -47, -55, 46, 40, 11, -112, 0, -98, 29, -3, 92, -59, 117, 50, 42, -64, 3, -12, 19, 44, -31, -96, 35, -3, -72, 0, 49, -72, 79, 20, 104, 126, -76, -24, -14, 13, 1, -120, -100, -65, 21, -99, -16, 29, 70, -10, 98, 44, 27, 123, 33, -101, -80, 99, 50, -65, 50, -82, 119, -17, -12, -116, -107, 15, -45, 30, 82, -22, 101, -5, 59, 120, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static final byte[] violet1 = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 28, 8, 6, 0, 0, 0, -108, 36, 20, -48, 0, 0, 0, 4, 115, 66, 73, 84, 8, 8, 8, 8, 124, 8, 100, -120, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 12, -21, 0, 0, 12, -21, 1, -27, -42, 68, -46, 0, 0, 0, -23, 73, 68, 65, 84, 72, -119, -19, -106, 75, 14, -61, 32, 12, 68, -121, -88, 7, -21, -51, -22, -93, -27, 102, -18, -94, -72, 34, -32, 111, -23, -90, 82, 29, 69, 34, 48, -10, -77, 33, -124, 52, -28, -115, -107, -66, -106, 113, 76, -119, 4, 64, -96, 119, -57, -48, 14, 99, 100, 32, 11, -96, 10, -118, 32, 38, -96, 2, 58, 2, -120, 11, -56, -116, 71, 16, 109, -95, 61, 51, -11, 110, 37, -103, 44, 51, 58, 11, 98, 102, -11, -24, 87, -59, -49, -84, 68, -53, 110, 12, 62, -125, -68, 106, -62, -123, -73, -126, 90, 125, 91, -112, 29, -5, 67, 126, 8, -110, -35, -120, 25, -67, 5, -55, 30, 1, 41, -65, 3, -50, -18, -2, -46, 103, -123, -91, -110, -113, 79, -67, 64, -49, 99, -125, -95, -125, 24, 0, 19, -120, 9, -60, 12, -66, -36, 4, 10, 125, 101, -116, -23, 94, 7, 101, 0, 18, -73, -11, 7, 0, 0, -99, 102, -23, -107, 51, -2, -91, 29, 98, 94, 32, 25, -48, 100, 46, 64, -30, 45, -81, -16, 32, -104, -125, 54, -27, 118, 1, 98, -22, 62, -103, 64, -42, -36, 47, 99, 26, 0, 0, 110, 122, -9, -22, -48, -89, -111, 61, -115, 101, 45, -54, 98, -57, 100, 125, 101, 94, -85, 127, 38, 21, 107, 79, 77, -7, 124, 40, -83, 34, 110, -15, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        private static Image DESENHOS[] = {
            Toolkit.getDefaultToolkit().createImage(blue0),
            Toolkit.getDefaultToolkit().createImage(blue1),
            Toolkit.getDefaultToolkit().createImage(blue2),
            Toolkit.getDefaultToolkit().createImage(red0),
            Toolkit.getDefaultToolkit().createImage(red1),
            Toolkit.getDefaultToolkit().createImage(green0),
            Toolkit.getDefaultToolkit().createImage(green1),
            Toolkit.getDefaultToolkit().createImage(green2),
            Toolkit.getDefaultToolkit().createImage(gray0),
            Toolkit.getDefaultToolkit().createImage(gray1),
            Toolkit.getDefaultToolkit().createImage(violet0),
            Toolkit.getDefaultToolkit().createImage(violet1)
        };
        private int desenhar;
        private boolean inverter = false;
        private int direcao = 1;

        public Elemento(int x, int y, int tipo) {
            super(x, y, (Integer) VALORES[tipo][0], (Integer) VALORES[tipo][1]);
            setTipo(tipo);
            tiroX = x + width / 2;
            tiroY = y + height / 2;
            selecionado = false;
            if (tipo == CANHAO_RED) {
                desenhar = 3;
            } else if (tipo == CANHAO_GREEN) {
                desenhar = 5;
            } else if (tipo == CANHAO_GRAY) {
                desenhar = 8;
            } else if (tipo == CANHAO_VIOLET) {
                desenhar = 10;
            } else {
                desenhar = 0;
            }
        }

        private Elemento(int x, int y, int w, int h, int incX, int incY) {
            super(x, y, w, h);
            tiroX = incX;
            tiroY = incY;
        }
        
        private void drawCursor(int locX, int locY, Graphics g) {
            if (locX < TAMANHO && locY < TAMANHO) {
                g.setColor(this.getColor());
                g.drawRect(
                        locX - getAlcance(), 
                        locY - getAlcance(),
                        width + getAlcance()*2,
                        height + getAlcance()*2);
                g.drawImage(getImage(), locX, locY, null);
            }
        }

        private void draw(Graphics g) {
            if (x < TAMANHO && y < TAMANHO) {
                if (tipo < OPCOES) {
                    g.setColor(getColor());
                    g.drawLine(x + width / 2, y + height / 2, tiroX, tiroY);
                }
                if (tipo >= OPCOES) {
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, width, height);
                    g.setColor(getColor());
                    g.fillRect(x, y, width, height);
                } else {
                    g.setColor(getColor());
                    if (inverter) {
                        g.drawImage(getImage(), x, y, width + x, height + y, width, 0, 0, height, null);
                    } else {
                        g.drawImage(getImage(), x, y, null);
                    }
                }
                if (selecionado) {
                    if (tipo >= OPCOES) {
                        g.setColor(Color.BLUE);
                    }
                    g.drawRect(x - 2, y - 2, width + 4, height + 4);
                }
            }
        }

        private Elemento getAlvo(ArrayList<Elemento> inimigos) {
            for (Elemento alvo : inimigos) {
                int alX = alvo.x + width / 2;
                int alY = alvo.y + height / 2;
                if (this.x - getAlcance() < alX && this.x + this.width + getAlcance() * 2 > alX
                        && this.y - getAlcance() < alY && this.y + this.height + getAlcance() * 2 > alY) {
                    return alvo;
                }
            }
            return null;
        }

        private int getVida() {
            return (Integer) VALORES[tipo][3];
        }

        private int getValor() {
            return (Integer) VALORES[tipo][3];
        }

        private int getPoder() {
            return (Integer) VALORES[tipo][5];
        }

        private int getAlcance() {
            return (Integer) VALORES[tipo][4];
        }

        private int getVelocidade() {
            return (Integer) VALORES[tipo][4];
        }

        private Color getColor() {
            return (Color) VALORES[tipo][2];
        }
        
        private Image getImage() {
            return DESENHOS[desenhar];
        }

        private void mudar(ArrayList<Elemento> inimigos) {
            for (Elemento alvo : inimigos) {
                if (this.contains(alvo)) {
                    alvo.tiroX = this.tiroX;
                    alvo.tiroY = this.tiroY;
                }
            }
        }

        private void parar() {
            this.selecionado = false;
            this.valor = 1;
            this.tiroX = 0;
            this.tiroY = 0;
            this.x = -50;
            this.y = -50;
        }

        private void setTipo(int tipo) {
            this.tipo = tipo;
            this.valor = (Integer) VALORES[tipo][3];
            this.width = (Integer) VALORES[tipo][0];
            this.height = (Integer) VALORES[tipo][1];
            this.tiroX = 1;
            this.tiroY = 0;
            this.valor = this.getVida();
        }

        private void atualizarDesenho() {
            if (tipo == CANHAO_GELO) {
                if (desenhar + 1 == 3) {
                    desenhar = 0;
                    inverter = !inverter;
                } else {
                    desenhar++;
                }
            } else if (tipo == CANHAO_RED) {
                if (desenhar + 1 == 5) {
                    desenhar = 3;
                } else {
                    desenhar++;
                    inverter = !inverter;
                }
            } else if (tipo == CANHAO_GREEN) {
                if (direcao == -1 && desenhar - 1 == 4) {
                    direcao = 1;
                }
                if (direcao == 1 && desenhar + 1 == 8) {
                    direcao = -1;
                } else {
                    desenhar += direcao;
                }
            } else if (tipo == CANHAO_GELO) {
                if (desenhar + 1 == 3) {
                    desenhar = 0;
                    inverter = !inverter;
                } else {
                    desenhar++;
                }
            } else if (tipo == CANHAO_GRAY) {
                if (desenhar + 1 == 10) {
                    desenhar = 8;
                    inverter = !inverter;
                } else {
                    desenhar++;
                }
            } else if (tipo == CANHAO_VIOLET) {
                if (desenhar + 1 == 12) {
                    desenhar = 10;
                    inverter = !inverter;
                } else {
                    desenhar++;
                }
            }
        }
    }
}