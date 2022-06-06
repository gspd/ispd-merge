package ispd.gui;

import DescreveSistema.DescreveIcone;
import DescreveSistema.DescreveSistema;
import Interface.AguardaSimulacao;
import ispd.ValidaValores;
import ispd.arquivo.IconicoXML;
import ispd.gui.configuracao.ConfiguraCluster;
import ispd.gui.configuracao.ConfiguraInternet;
import ispd.gui.configuracao.ConfiguraMaquina;
import ispd.gui.configuracao.ConfiguraRede;
import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaTaskNode;
import ispd.motor.carga.CargaTrace;
import ispd.motor.carga.GerarCarga;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.*;
import ispd.motor.metricas.MetricasUsuarios;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AreaDesenho extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    private ResourceBundle palavras;
    //Objetos principais da classe
    private int w, h;
    /**
     * Lista com os icones presentes na area de desenho
     */
    private HashSet<Icone> icones;
    /**
     * Lista com os usuarios/proprietarios do modelo criado
     */
    private HashSet<String> usuarios;
    /**
     * Objeto para Manipular as cargas
     */
    private GerarCarga cargasConfiguracao;
    /**
     * número de icones excluindo os links
     */
    private int numArestas;
    /**
     * número de links
     */
    private int numVertices;
    /**
     * número total de icones
     */
    private int numIcones;
    //Objetos usados para controlas os popupmenus
    private JPopupMenu popupMenu;
    private JPopupMenu popupMenu2;
    private JMenuItem botaoRemove;
    private JMenuItem botaoCopiar;
    private JMenuItem botaoColar;
    private JMenuItem botaoInverter;
    private JSeparator jSeparator1;
    //Objetos advindo da classe JanelaPrincipal
    private JPrincipal janelaPrincipal;
    //Objetos do cursor
    private Cursor hourglassCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    private Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    //Objetos usados para desenhar a regua e as grades
    private int units;
    private boolean metric;
    private int INCH;
    private boolean gridOn;
    //Objetos para desenhar Retângulo de seleção
    private boolean retangulo = false;
    private List<Icone> noRetangulo;
    private int retanguloX, retanguloY, retanguloLag, retanguloAlt;
    //Objetos para Selecionar texto na Area Lateral
    private boolean imprimeNosConectados;
    private boolean imprimeNosIndiretos;
    private boolean imprimeNosEscalonaveis;
    //Objetos usados para add um icone
    private int tipoIcone;
    private boolean botaoSelecaoIconeClicado;
    private boolean primeiroClique;
    private int posPrimeiroCliqueX;
    private int posPrimeiroCliqueY;
    private int posSegundoCliqueX;
    private int posSegundoCliqueY;
    private int verticeInicio;
    private int verticeFim;
    //Objetos usados para minipular os icones
    private Icone iconeAuxiliar;
    private Icone iconeAuxiliarNovo;
    private int posicaoMouseX;
    private int posicaoMouseY;
    private Icone iconeAuxiliarMatchRede;
    private Icone iconeNulo;
    private boolean iconeSelecionado;
    //Objetos para remover um icone
    private List<Icone> iconeAuxiliaRemover;
    //Obejtos para copiar um icone
    private Icone iconeCopiado;
    private boolean acaoColar;

    public AreaDesenho(int w, int h) {

        //Utiliza o idioma do sistema como padrão
        Locale locale = Locale.getDefault();
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", locale);

        addMouseListener(this);
        addMouseMotionListener(this);

        this.w = w;
        this.h = h;
        this.numArestas = 0;
        this.numVertices = 0;
        this.numIcones = 0;
        icones = new HashSet<Icone>();
        usuarios = new HashSet<String>();
        usuarios.add("user1");
        ValidaValores.removeTodosNomeIcone();
        metric = true;
        gridOn = false;
        INCH = Toolkit.getDefaultToolkit().getScreenResolution();
        tipoIcone = 0;
        botaoSelecaoIconeClicado = false;
        primeiroClique = false;
        cargasConfiguracao = null;
        imprimeNosConectados = false;
        imprimeNosIndiretos = false;
        imprimeNosEscalonaveis = true;
        acaoColar = false;
        iconeNulo = new Icone(-100, -100, -1, 0, 0);
        iconeAuxiliarMatchRede = iconeNulo;
        iconeAuxiliaRemover = new ArrayList<Icone>();
        noRetangulo = new ArrayList<Icone>();
    }

    public void setPaineis(JPrincipal janelaPrincipal) {
        this.janelaPrincipal = janelaPrincipal;
        this.initPopupMenu();
        this.initTexts();
    }

    public void initPopupMenu() {
        popupMenu = new JPopupMenu();
        popupMenu2 = new JPopupMenu();
        jSeparator1 = new JSeparator();

        botaoCopiar = new JMenuItem();
        botaoCopiar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCopiarActionPerformed(evt);
            }
        });
        //popupMenu.add(botaoCopiar);

        botaoInverter = new JMenuItem();
        //botaoInverter.setEnabled(false);
        botaoInverter.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoInverterActionPerformed(evt);
            }
        });
        //popupMenu.add(botaoInverter);

        popupMenu.add(jSeparator1);

        botaoRemove = new JMenuItem();
        botaoRemove.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoRemoveActionPerformed(evt);
            }
        });
        //popupMenu.add(botaoRemove);

        botaoColar = new JMenuItem();
        botaoColar.setEnabled(false);
        botaoColar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoColarActionPerformed(evt);
            }
        });
        popupMenu2.add(botaoColar);

    }

    //utilizado para inserir novo valor nas Strings dos componentes
    private void initTexts() {
        botaoCopiar.setText(palavras.getString("Copy"));
        botaoInverter.setText(palavras.getString("Turn Over"));
        botaoRemove.setText(palavras.getString("Remove"));
        botaoColar.setText(palavras.getString("Paste"));
    }

    private void montarBotoesPopupMenu(int tipo) {
        popupMenu.remove(botaoCopiar);
        popupMenu.remove(botaoRemove);
        popupMenu.remove(jSeparator1);
        popupMenu.remove(botaoInverter);
        if (tipo == 2) {
            popupMenu.add(botaoInverter);
            popupMenu.add(jSeparator1);
            popupMenu.add(botaoRemove);
        } else {
            popupMenu.add(botaoCopiar);
            popupMenu.add(jSeparator1);
            popupMenu.add(botaoRemove);
        }
    }

    public Icone adicionaAresta(int x, int y, int posPrimeiroCliqueX, int posPrimeiroCliqueY, int tipoIcone) {
        Icone I = new Icone(x, y, posPrimeiroCliqueX, posPrimeiroCliqueY, tipoIcone, numArestas, numIcones);
        numArestas++;
        numIcones++;
        icones.add(I);
        I.setEstaAtivo(true);
        I.setNoOrigem(verticeInicio);
        I.setNoDestino(verticeFim);
        I.setNome("icon" + I.getIdGlobal());
        ValidaValores.addNomeIcone(I.getNome());
        return I;
    }

    public Icone adicionaVertice(int x, int y, int tipoIcone) {
        Icone I = new Icone(x, y, tipoIcone, numVertices, numIcones);
        numVertices++;
        numIcones++;
        icones.add(I);
        I.setEstaAtivo(true);
        switch (I.getTipoIcone()) {
            case 1:
                this.janelaPrincipal.appendNotificacao(palavras.getString("Machine icon added."));
                break;
            case 3:
                this.janelaPrincipal.appendNotificacao(palavras.getString("Cluster icon added."));
                break;
            case 4:
                this.janelaPrincipal.appendNotificacao(palavras.getString("Internet icon added."));
                break;
        }
        I.setNome("icon" + I.getIdGlobal());
        ValidaValores.addNomeIcone(I.getNome());
        return I;
    }

    public int getIconWidth() {
        return w;
    }

    public int getIconHeight() {
        return h;
    }

    public HashSet<Icone> getIcones() {
        return icones;
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
        return new Dimension(w, h);
    }

    public void setConectados(boolean imprimeNosConectados) {
        this.imprimeNosConectados = imprimeNosConectados;
    }

    public void setIndiretos(boolean imprimeNosIndiretos) {
        this.imprimeNosIndiretos = imprimeNosIndiretos;
    }

    public void setEscalonaveis(boolean imprimeNosEscalonaveis) {
        this.imprimeNosEscalonaveis = imprimeNosEscalonaveis;
    }

    public void setIsMetric(boolean metric) {
        this.metric = metric;
        repaint();
    }

    public void setGrid(boolean gridOn) {
        this.gridOn = gridOn;
        repaint();
    }

    public HashSet<String> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(HashSet<String> usuarios) {
        this.usuarios = usuarios;
    }

    public GerarCarga getCargasConfiguracao() {
        return cargasConfiguracao;
    }

    public void setCargasConfiguracao(GerarCarga cargasConfiguracao) {
        this.cargasConfiguracao = cargasConfiguracao;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(255, 255, 255));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(new Color(220, 220, 220));

        if (metric) {
            units = (int) ((double) INCH / (double) 2.54);
        } else {
            units = (int) INCH / 2;
        }

        if (gridOn) {
            for (int _w = 0; _w <= w; _w += units) {
                g2d.drawLine(_w, 0, _w, h);
            }
            for (int _h = 0; _h <= h; _h += units) {
                g2d.drawLine(0, _h, w, _h);
            }
        }


        //Desenha a linha da conexão de rede antes dela se estabelcer.
        if (botaoSelecaoIconeClicado && primeiroClique) {
            g2d.setColor(new Color(0, 0, 0));
            g2d.drawLine(posPrimeiroCliqueX, posPrimeiroCliqueY, posicaoMouseX, posicaoMouseY);
        }


        // Desenha quadrado
        if (retangulo) {
            //g2d.setPaint(new GradientPaint(quadradoX,quadradoY,new Color(100, 100, 255),quadradoX+quadradoLag, quadradoY+quadradoAlt,new Color(200, 200, 255)));
            //g2d.fillRect(quadradoX, quadradoY, quadradoLag, quadradoAlt);
            //g.setColor(new Color(200, 200, 255));
            //g.fillRect(quadradoX, quadradoY, quadradoLag, quadradoAlt);
            g.setColor(Color.BLACK);
            if (retanguloLag < 0 && retanguloAlt < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX + retanguloLag, retanguloY + retanguloAlt, retanguloLag * -1, retanguloAlt * -1);
                g.setColor(new Color((float) 0, (float) 0, (float) 1, (float) 0.2));
                g.fillRect(retanguloX + retanguloLag, retanguloY + retanguloAlt, retanguloLag * -1, retanguloAlt * -1);
            } else if (retanguloLag < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX + retanguloLag, retanguloY, retanguloLag * -1, retanguloAlt);
                g.setColor(new Color((float) 0, (float) 0, (float) 1, (float) 0.2));
                g.fillRect(retanguloX + retanguloLag, retanguloY, retanguloLag * -1, retanguloAlt);
            } else if (retanguloAlt < 0) {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX, retanguloY + retanguloAlt, retanguloLag, retanguloAlt * -1);
                g.setColor(new Color((float) 0, (float) 0, (float) 1, (float) 0.2));
                g.fillRect(retanguloX, retanguloY + retanguloAlt, retanguloLag, retanguloAlt * -1);
            } else {
                g.setColor(Color.BLACK);
                g.drawRect(retanguloX, retanguloY, retanguloLag, retanguloAlt);
                g.setColor(new Color((float) 0, (float) 0, (float) 1, (float) 0.2));
                g.fillRect(retanguloX, retanguloY, retanguloLag, retanguloAlt);
            }
        }

        // Desenhamos todos os icones
        for (Icone I : icones) {
            if (I.getTipoIcone() == 2) {
                I.draw(g2d);
            }
        }
        for (Icone I : icones) {
            if (I.getTipoIcone() != 2) {
                I.draw(g2d);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (botaoSelecaoIconeClicado) {
            //Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            for (Icone I : icones) {
                I.setEstaAtivo(false);
            }
            posicaoMouseX = e.getX();
            posicaoMouseY = e.getY();
            if (tipoIcone == 2) {
                if (!primeiroClique) {
                    boolean achouIcone = false;
                    for (Icone I : icones) {
                        boolean clicado = I.getRectEnvolvente(e.getX(), e.getY());
                        if (clicado) {
                            posPrimeiroCliqueX = I.getNumX();
                            posPrimeiroCliqueY = I.getNumY();
                            verticeInicio = I.getIdGlobal();
                            achouIcone = true;
                            break;
                        }
                    }
                    if (achouIcone) {
                        primeiroClique = true;
                    } else {
                        JOptionPane.showMessageDialog(null, palavras.getString("You must click an icon."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
                        //setCursor(normalCursor);
                        //botaoSelecaoIconeClicado = false;
                    }
                } else {
                    boolean achouIcone = false;
                    for (Icone I : icones) {
                        boolean clicado = I.getRectEnvolvente(e.getX(), e.getY());
                        if (clicado && I.getTipoIcone() != 2) {
                            posSegundoCliqueX = I.getNumX();
                            posSegundoCliqueY = I.getNumY();
                            verticeFim = I.getIdGlobal();
                            if (verticeInicio != verticeFim) {
                                achouIcone = true;
                            }
                            break;
                        }
                    }
                    if (achouIcone) {
                        primeiroClique = false;
                        Icone I = adicionaAresta(posSegundoCliqueX, posSegundoCliqueY, posPrimeiroCliqueX, posPrimeiroCliqueY, tipoIcone);
                        this.janelaPrincipal.appendNotificacao(palavras.getString("Network connection added."));
                        this.janelaPrincipal.modificar();
                        this.setLabelAtributos(I);
                        //setCursor(normalCursor);
                        //botaoSelecaoIconeClicado = false;
                        //fors para adicionar numero do destino na origem e vice versa
                        for (Icone Ico : icones) {
                            if (Ico.getIdGlobal() == verticeInicio && Ico.getTipoIcone() != 2) {
                                Ico.addIdConexaoSaida(verticeFim);
                                break;
                            }
                        }
                        for (Icone Ico : icones) {
                            if (Ico.getIdGlobal() == verticeFim && Ico.getTipoIcone() != 2) {
                                Ico.addIdConexaoEntrada(verticeInicio);
                                break;
                            }
                        }
                        atualizaNosIndiretos();
                    } else {
                        JOptionPane.showMessageDialog(null, palavras.getString("You must click an icon."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
                        //setCursor(normalCursor);
                        //botaoSelecaoIconeClicado = false;
                    }
                }
            } else {
                Icone I = adicionaVertice(posicaoMouseX, posicaoMouseY, tipoIcone);
                this.janelaPrincipal.modificar();
                this.setLabelAtributos(I);
                //setCursor(normalCursor);
                //botaoSelecaoIconeClicado = false;
            }

        } else {
            janelaPrincipal.setSelectedIcon(null, null);
            iconeAuxiliarMatchRede = iconeNulo;
            for (Icone I : icones) {
                I.setEstaAtivo(false);
            }
            for (Icone I : icones) {
                boolean clicado = I.getRectEnvolvente(e.getX(), e.getY());
                if (clicado) {
                    I.setEstaAtivo(true);
                    this.setLabelAtributos(I);
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1:
                            if (e.getClickCount() == 2) {
                                setAtributos(I);
                            } else {
                                iconeAuxiliarMatchRede = I;
                            }
                            break;
                        case MouseEvent.BUTTON2:
                            break;
                        case MouseEvent.BUTTON3:
                            iconeAuxiliarNovo = I;
                            this.montarBotoesPopupMenu(I.getTipoIcone());
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                            break;
                    }
                    break;
                } else {
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1:
                            break;
                        case MouseEvent.BUTTON2:
                            break;
                        case MouseEvent.BUTTON3:
                            posicaoMouseX = e.getX();
                            posicaoMouseY = e.getY();
                            popupMenu2.show(e.getComponent(), e.getX(), e.getY());
                            break;
                    }
                }
            }

        }
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (retanguloLag < 0) {
            retanguloX += retanguloLag;
            retanguloLag *= -1;
        }
        if (retanguloAlt < 0) {
            retanguloY += retanguloAlt;
            retanguloAlt *= -1;
        }
        if (noRetangulo.isEmpty()) {
            for (Icone icone : icones) {
                if (retanguloX < icone.getNumX()
                        && icone.getNumX() < (retanguloX + retanguloLag)
                        && retanguloY < icone.getNumY()
                        && icone.getNumY() < (retanguloY + retanguloAlt)) {
                    icone.setEstaAtivo(true);
                    noRetangulo.add(icone);
                }
            }
        }
        retangulo = false;
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        posicaoMouseX = e.getX();
        posicaoMouseY = e.getY();
        if (botaoSelecaoIconeClicado) {
            setCursor(hourglassCursor);
        } else {
            setCursor(normalCursor);
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //Retira a seleção de todos os icones
        for (Icone I : icones) {
            I.setEstaAtivo(false);
        }
        //Busca e indica se algum ícone foi selecionado
        boolean selecionado = false;
        for (Icone I : icones) {
            boolean clicado = I.getRectEnvolvente(e.getX(), e.getY());
            if (clicado) {
                selecionado = true;
                //Indica que ícone foi selecionado e habilita áreas de configurações
                I.setEstaAtivo(true);
                if (I.getIdGlobal() != -1) {
                    this.setLabelAtributos(I);
                } else {
                    janelaPrincipal.setSelectedIcon(null, null);
                }
            }
            if (clicado && I.getTipoIcone() != Icone.NETWORK) {
                iconeAuxiliar = I;
                iconeSelecionado = true;
                break;
            } else {
                iconeSelecionado = false;
            }
        }
        //Verifica se foi selecionado item dentro do retangulo
        if (selecionado && !noRetangulo.isEmpty()) {
            boolean ativo = false;
            Iterator<Icone> it = noRetangulo.iterator();
            while (it.hasNext() && !ativo) {
                Icone ic = it.next();
                ativo = ic.getEstaAtivo();
            }
            if (!ativo) {
                noRetangulo.clear();
            } else {
                //Diferença entre ponto do mouse e o icone
                //Usado para atualizar posição
                for (Icone icone : noRetangulo) {
                    icone.setEstaAtivo(true);
                    if (icone.getTipoIcone() != Icone.NETWORK) {
                        icone.setPrePosition(icone.getNumX() - e.getX(), icone.getNumY() - e.getY());
                    }
                }
            }
        } else {
            retangulo = false;
            noRetangulo.clear();
        }
        //Indica ponto inicial do retangulo
        if (!selecionado) {
            retangulo = true;
            retanguloX = e.getX();
            retanguloY = e.getY();
            retanguloLag = 0;
            retanguloAlt = 0;
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //Arrasta icones selecionados pelo retangulo
        if (iconeSelecionado && !noRetangulo.isEmpty()) {
            for (Icone icone : noRetangulo) {
                if (icone.getTipoIcone() != Icone.NETWORK) {
                    int posX = e.getX();
                    int posY = e.getY();
                    icone.setPosition(posX + icone.getNumPreX(), posY + icone.getNumPreY());
                    conectarRede(icone);
                }
            }
            //Arrata um icone
        } else if (iconeSelecionado) {
            if (iconeAuxiliar.getTipoIcone() != Icone.NETWORK) {
                posicaoMouseX = e.getX();
                posicaoMouseY = e.getY();
                iconeAuxiliar.setPosition(posicaoMouseX, posicaoMouseY);
                conectarRede(iconeAuxiliar);
            }
            //Redefine dimensões do retangulo
        } else if (retangulo) {
            retanguloLag = e.getX() - retanguloX;
            retanguloAlt = e.getY() - retanguloY;
            int retX, retY, retLag, retAlt;
            if (retanguloLag < 0) {
                retX = retanguloX + retanguloLag;
                retLag = retanguloLag * -1;
            } else {
                retX = retanguloX;
                retLag = retanguloLag;
            }
            if (retanguloAlt < 0) {
                retY = retanguloY + retanguloAlt;
                retAlt = retanguloAlt * -1;
            } else {
                retY = retanguloY;
                retAlt = retanguloAlt;
            }
            //Seleciona icones dentro do retangulo
            for (Icone icone : icones) {
                if (retX < icone.getNumX()
                        && icone.getNumX() < (retX + retLag)
                        && retY < icone.getNumY()
                        && icone.getNumY() < (retY + retAlt)) {
                    icone.setEstaAtivo(true);
                } else {
                    icone.setEstaAtivo(false);
                }
            }
        }
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Icone I : icones) {
            I.move();
        }
        atualizaNosIndiretos();
        repaint();
    }

    public void iniciarSimulacao() {
        Object objetos[] = new Object[4];
        objetos[0] = true;
        objetos[1] = cargasConfiguracao.getTipo();
        objetos[2] = cargasConfiguracao.toString();
        objetos[3] = icones;
        AguardaSimulacao janela = new AguardaSimulacao(objetos);
    }

    public void setIconeSelecionado(int tipoIcone) {
        this.tipoIcone = tipoIcone;
        this.botaoSelecaoIconeClicado = true;
        if (tipoIcone == 2) {
            this.primeiroClique = false;
        }
    }

    public void semIconeSelecionado() {
        this.botaoSelecaoIconeClicado = false;
        this.primeiroClique = false;
    }

    public void removeIcone(Icone I) {
        icones.remove(I);
    }

    public void atualizaNosIndiretos() {
        //Remover nodes Indiretos
        for (Icone I : icones) {
            I.clearNosIndiretosEntrada();
            I.clearNosIndiretosSaida();
        }

        //Inserir Nodes Indiretos
        int numIcoInternet = 0;
        for (Icone I : icones) {
            if (I.getTipoIcone() == 4) {
                numIcoInternet++;
            }
        }
        for (int i = 0; i < numIcoInternet; i++) {
            for (Icone I1 : icones) {
                if (I1.getTipoIcone() == 4) {
                    HashSet<Integer> listaOrigem = I1.getObjetoConexaoEntrada();
                    HashSet<Integer> listaDestino = I1.getObjetoConexaoSaida();
                    for (int temp1 : listaDestino) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == temp1) {
                                HashSet<Integer> listaIndiretosEntrada = I2.getObjetoNosIndiretosEntrada();
                                for (Integer temp2 : listaOrigem) {
                                    if (!listaIndiretosEntrada.contains(temp2) && I2.getIdGlobal() != temp2) {
                                        listaIndiretosEntrada.add(temp2);
                                    }
                                }
                                I2.setObjetoNosIndiretosEntrada(listaIndiretosEntrada);
                            }
                        }
                    }
                    listaOrigem = I1.getObjetoConexaoEntrada();
                    listaDestino = I1.getObjetoConexaoSaida();
                    for (int temp1 : listaOrigem) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == temp1) {
                                HashSet<Integer> listaIndiretosSaida = I2.getObjetoNosIndiretosSaida();
                                for (Integer temp2 : listaDestino) {
                                    if (!listaIndiretosSaida.contains(temp2) && I2.getIdGlobal() != temp2) {
                                        listaIndiretosSaida.add(temp2);
                                    }
                                }
                                I2.setObjetoNosIndiretosSaida(listaIndiretosSaida);
                            }
                        }
                    }
                    listaOrigem = I1.getObjetoNosIndiretosEntrada();
                    listaDestino = I1.getObjetoConexaoSaida();
                    for (int temp1 : listaOrigem) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == temp1) {
                                HashSet<Integer> listaIndiretosSaida = I2.getObjetoNosIndiretosSaida();
                                for (Integer temp2 : listaDestino) {
                                    if (!listaIndiretosSaida.contains(temp2) && I2.getIdGlobal() != temp2) {
                                        listaIndiretosSaida.add(temp2);
                                    }
                                }
                                I2.setObjetoNosIndiretosSaida(listaIndiretosSaida);
                            }
                        }
                    }
                    listaOrigem = I1.getObjetoConexaoEntrada();
                    listaDestino = I1.getObjetoNosIndiretosSaida();
                    for (int temp1 : listaDestino) {
                        for (Icone I2 : icones) {
                            if (I2.getIdGlobal() == temp1) {
                                HashSet<Integer> listaIndiretosEntrada = I2.getObjetoNosIndiretosEntrada();
                                for (Integer temp2 : listaOrigem) {
                                    if (!listaIndiretosEntrada.contains(temp2) && I2.getIdGlobal() != temp2) {
                                        listaIndiretosEntrada.add(temp2);
                                    }
                                }
                                I2.setObjetoNosIndiretosEntrada(listaIndiretosEntrada);
                            }
                        }
                    }

                    /*HashSet<Integer> listaIndiretosEntrada = I1.getObjetoNosIndiretosEntrada();
                     for(int temp1:listaIndiretosEntrada){
                     for(Icone I2:icones){
                     if(I2.getIdGlobal()==temp1){
                     HashSet<Integer> listaDestino = I2.getObjetoNosIndiretos();
                     HashSet<Integer> listaOrigem2 = I1.getObjetoNosIndiretos();
                     for(int temp2:listaOrigem2){
                     if(!listaDestino.contains(temp2) && temp2!=I2.getID()){
                     listaDestino.add(temp2);
                     }
                     }
                     I2.setObjetoNosIndiretos(listaDestino);
                     }
                     }
                     }*/
                }
            }
        }

        //Atualiza nos escalonaveis
        //Remover nos Escalonaveis
        for (Icone I : icones) {
            I.clearNosEscalonaveis();
        }

        //adiciona nos escalonaveis
        for (Icone I : icones) {
            if (I.getTipoIcone() != 2 && I.getTipoIcone() != 4) {
                HashSet<Integer> listaOrigem1 = I.getObjetoConexaoSaida();
                HashSet<Integer> listaOrigem2 = I.getObjetoNosIndiretosSaida();
                HashSet<Integer> listaDestino = I.getObjetoNosEscalonaveis();
                //listaDestino.add(I.getIdGlobal());
                for (int temp1 : listaOrigem1) {
                    for (Icone I2 : icones) {
                        if (I2.getTipoIcone() != 2 && I2.getTipoIcone() != 4 && temp1 == I2.getIdGlobal()) {
                            listaDestino.add(temp1);
                        }
                    }
                }
                for (int temp1 : listaOrigem2) {
                    for (Icone I2 : icones) {
                        if (I2.getTipoIcone() != 2 && I2.getTipoIcone() != 4 && temp1 == I2.getIdGlobal()) {
                            listaDestino.add(temp1);
                        }
                    }
                }
                I.setObjetoNosEscalonaveis(listaDestino);
            }
        }
    }

    private void botaoRemoveActionPerformed(java.awt.event.ActionEvent evt) {
        acaoRemove();
    }

    private void acaoRemove() {
        int opcao = JOptionPane.showConfirmDialog(null, palavras.getString("Remove this icon?"), palavras.getString("Remove"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcao == JOptionPane.YES_OPTION) {
            for (Icone iconeRemover : iconeAuxiliaRemover) {
                if (iconeRemover.getTipoIcone() == 2) {
                    int j = 0;
                    for (Icone I1 : icones) {
                        if (I1.getIdGlobal() == iconeRemover.getNoOrigem() && I1.getTipoIcone() != 2) {
                            for (Icone I2 : icones) {
                                if (I2.getIdGlobal() == iconeRemover.getNoDestino() && I2.getTipoIcone() != 2) {
                                    I1.removeConexaoSaida(I2.getIdGlobal());
                                    I2.removeConexaoEntrada(I1.getIdGlobal());
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    ValidaValores.removeNomeIcone(iconeRemover.getNome());
                    removeIcone(iconeRemover);
                    this.janelaPrincipal.modificar();
                    atualizaNosIndiretos();
                } else {
                    int cont = 0;
                    //Remover dados das conexoes q entram
                    HashSet<Integer> listanos = iconeRemover.getObjetoConexaoEntrada();
                    for (int i : listanos) {
                        for (Icone I : icones) {
                            if (i == I.getIdGlobal() && I.getTipoIcone() != 2) {
                                I.removeConexaoSaida(iconeRemover.getIdGlobal());
                                break;
                            }
                        }
                    }
                    //Remover dados das conexoes q saem
                    listanos = iconeRemover.getObjetoConexaoSaida();
                    for (int i : listanos) {
                        for (Icone I : icones) {
                            if (i == I.getIdGlobal() && I.getTipoIcone() != 2) {
                                I.removeConexaoEntrada(iconeRemover.getIdGlobal());
                                break;
                            }
                        }
                    }
                    for (Icone I : icones) {
                        if (I.getTipoIcone() == 2 && ((I.getNumX() == iconeRemover.getNumX() && I.getNumY() == iconeRemover.getNumY()) || (I.getNumPreX() == iconeRemover.getNumX() && I.getNumPreY() == iconeRemover.getNumY()))) {
                            cont++;
                        }
                    }
                    for (int j = 0; j < cont; j++) {
                        for (Icone I : icones) {
                            if (I.getTipoIcone() == 2 && ((I.getNumX() == iconeRemover.getNumX() && I.getNumY() == iconeRemover.getNumY()) || (I.getNumPreX() == iconeRemover.getNumX() && I.getNumPreY() == iconeRemover.getNumY()))) {
                                ValidaValores.removeNomeIcone(I.getNome());
                                removeIcone(I);
                                break;
                            }
                        }
                    }
                    ValidaValores.removeNomeIcone(iconeRemover.getNome());
                    removeIcone(iconeRemover);
                    this.janelaPrincipal.modificar();
                    atualizaNosIndiretos();
                }
            }
            iconeAuxiliaRemover.clear();
            repaint();
        }
    }

    public void deletarIcone() {
        boolean iconeEncontrado = false;
        for (Icone I : icones) {
            if (I.getEstaAtivo() == true) {
                iconeEncontrado = true;
                iconeAuxiliaRemover.add(I);
            }
        }
        if (!iconeEncontrado) {
            JOptionPane.showMessageDialog(null, palavras.getString("No icon selected."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        } else {
            acaoRemove();
        }
    }

    private void botaoCopiarActionPerformed(java.awt.event.ActionEvent evt) {
        //Não copia conexão de rede
        if (iconeAuxiliarNovo.getTipoIcone() != 2) {
            iconeCopiado = iconeAuxiliarNovo;
            acaoColar = true;
            botaoColar.setEnabled(true);
        }
    }

    public void acaoCopiarIcone() {
        boolean iconeEncontrado = false;
        for (Icone I : icones) {
            if (I.getEstaAtivo() == true) {
                iconeEncontrado = true;
                iconeCopiado = I;
                acaoColar = true;
                botaoColar.setEnabled(true);
                break;
            }
        }
        if (!iconeEncontrado) {
            JOptionPane.showMessageDialog(null, palavras.getString("No icon selected."), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void botaoColarActionPerformed(java.awt.event.ActionEvent evt) {
        acaoColarIcone();
    }

    public void acaoColarIcone() {
        for (Icone i : icones) {
            i.setEstaAtivo(false);
        }
        if (acaoColar == true && iconeCopiado.getTipoIcone() != 2) {
            Icone I = adicionaVertice(posicaoMouseX, posicaoMouseY, iconeCopiado.getTipoIcone());
            I.setNome("icon" + I.getIdGlobal());
            ValidaValores.addNomeIcone(I.getNome());
            I.setPoderComputacional(iconeCopiado.getPoderComputacional());
            I.setTaxaOcupacao(iconeCopiado.getTaxaOcupacao());
            I.setLatencia(iconeCopiado.getLatencia());
            I.setBanda(iconeCopiado.getBanda());
            I.setAlgoritmo(iconeCopiado.getAlgoritmo());
            I.setNumeroEscravos(iconeCopiado.getNumeroEscravos());
            this.janelaPrincipal.modificar();
        } else {
            //colar conexão de rede
        }
        repaint();
    }

    private void botaoInverterActionPerformed(java.awt.event.ActionEvent evt) {
        if (iconeAuxiliarNovo.getTipoIcone() == 2) {
            iconeAuxiliarNovo.setEstaAtivo(false);
            Icone I = adicionaAresta(iconeAuxiliarNovo.getNumPreX(), iconeAuxiliarNovo.getNumPreY(), iconeAuxiliarNovo.getNumX(), iconeAuxiliarNovo.getNumY(), iconeAuxiliarNovo.getTipoIcone());
            I.setNoOrigem(iconeAuxiliarNovo.getNoDestino());
            I.setNoDestino(iconeAuxiliarNovo.getNoOrigem());
            I.setPoderComputacional(iconeAuxiliarNovo.getPoderComputacional());
            I.setTaxaOcupacao(iconeAuxiliarNovo.getTaxaOcupacao());
            I.setLatencia(iconeAuxiliarNovo.getLatencia());
            I.setBanda(iconeAuxiliarNovo.getBanda());
            I.setAlgoritmo(iconeAuxiliarNovo.getAlgoritmo());
            I.setNumeroEscravos(iconeAuxiliarNovo.getNumeroEscravos());
            this.janelaPrincipal.modificar();
            //fors para adicionar numero do destino na origem e vice versa
            for (Icone Ico : icones) {
                if (Ico.getIdGlobal() == iconeAuxiliarNovo.getNoDestino() && Ico.getTipoIcone() != 2) {
                    Ico.addIdConexaoSaida(iconeAuxiliarNovo.getNoOrigem());
                    break;
                }
            }
            for (Icone Ico : icones) {
                if (Ico.getIdGlobal() == iconeAuxiliarNovo.getNoOrigem() && Ico.getTipoIcone() != 2) {
                    Ico.addIdConexaoEntrada(iconeAuxiliarNovo.getNoDestino());
                    break;
                }
            }
            this.janelaPrincipal.appendNotificacao(palavras.getString("Network connection added."));
            this.janelaPrincipal.modificar();
            this.setLabelAtributos(I);
            atualizaNosIndiretos();
        }
    }

    private void setAtributos(Icone I) {
        ConfiguraMaquina configMaquina;
        ConfiguraRede configuraRede;
        ConfiguraCluster configuraCluster;
        ConfiguraInternet configuraInternet;

        this.janelaPrincipal.modificar();
        atualizaNosIndiretos();

        switch (I.getTipoIcone()) {
            case 1: {
                configMaquina = new ConfiguraMaquina(I);
                configMaquina.setVisible(true);
            }
            break;
            case 2: {
                configuraRede = new ConfiguraRede(I);
                configuraRede.setVisible(true);
            }
            break;
            case 3: {
                configuraCluster = new ConfiguraCluster(I);
                configuraCluster.setVisible(true);
            }
            break;
            case 4: {
                configuraInternet = new ConfiguraInternet(I);
                configuraInternet.setVisible(true);
            }
            break;
        }
        this.setLabelAtributos(I);
        repaint();
    }

    @Override
    public String toString() {
        StringBuilder saida = new StringBuilder();
        for (Icone I : icones) {
            if (I.getTipoIcone() == 1) {
                saida.append(String.format("MAQ %s %f %f ", I.getNome(), I.getPoderComputacional(), I.getTaxaOcupacao()));
                if (I.isMestre()) {
                    saida.append(String.format("MESTRE " + I.getAlgoritmo() + " LMAQ"));
                    List<Integer> lista = I.getEscravos();
                    for (int temp : lista) {
                        for (Icone Ico : icones) {
                            if (Ico.getIdGlobal() == temp && Ico.getTipoIcone() != 2) {
                                saida.append(" ").append(Ico.getNome());
                            }
                        }
                    }
                } else {
                    saida.append("ESCRAVO");
                }
                saida.append("\n");
            }
        }

        for (Icone I : icones) {
            if (I.getTipoIcone() == 3) {
                saida.append(String.format("CLUSTER %s %d %f %f %f %s\n", I.getNome(), I.getNumeroEscravos(), I.getPoderComputacional(), I.getBanda(), I.getLatencia(), I.getAlgoritmo()));
            }
        }

        for (Icone I : icones) {
            if (I.getTipoIcone() == 4) {
                saida.append(String.format("INET %s %f %f %f\n", I.getNome(), I.getBanda(), I.getLatencia(), I.getTaxaOcupacao()));
            }
        }

        for (Icone I : icones) {
            if (I.getTipoIcone() == 2) {
                saida.append(String.format("REDE %s %f %f %f CONECTA", I.getNome(), I.getBanda(), I.getLatencia(), I.getTaxaOcupacao()));
                for (Icone Ico : icones) {
                    if (Ico.getIdGlobal() == I.getNoOrigem() && Ico.getTipoIcone() != 2) {
                        saida.append(" ").append(Ico.getNome());
                    }
                }
                for (Icone Ico : icones) {
                    if (Ico.getIdGlobal() == I.getNoDestino() && Ico.getTipoIcone() != 2) {
                        saida.append(" ").append(Ico.getNome());
                    }
                }
                saida.append("\n");
            }
        }

        saida.append("CARGA");
        if (cargasConfiguracao != null) {
            switch (cargasConfiguracao.getTipo()) {
                case GerarCarga.RANDOM:
                    saida.append(" RANDOM\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
                case GerarCarga.FORNODE:
                    saida.append(" MAQUINA\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
                case GerarCarga.TRACE:
                    saida.append(" TRACE\n").append(cargasConfiguracao.toString()).append("\n");
                    break;
            }
        }
        return saida.toString();
    }

    public void setLabelAtributos(Icone I) {
        String Texto = "<html>";
        HashSet<Integer> listaEntrada = I.getObjetoConexaoEntrada();
        HashSet<Integer> listaSaida = I.getObjetoConexaoSaida();
        switch (I.getTipoIcone()) {
            case 1: {
                Texto += palavras.getString("Local ID:") + " " + String.valueOf(I.getIdLocal())
                        + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(I.getIdGlobal())
                        + "<br>" + palavras.getString("Label:") + " " + I.getNome()
                        + "<br>" + palavras.getString("X-coordinate:") + " " + String.valueOf(I.getNumX())
                        + "<br>" + palavras.getString("Y-coordinate:") + " " + String.valueOf(I.getNumY())
                        + "<br>" + palavras.getString("Computational power:") + " " + String.valueOf(I.getPoderComputacional())
                        + "<br>" + palavras.getString("Load Factor:") + " " + String.valueOf(I.getTaxaOcupacao());
                if (I.isMestre()) {
                    Texto = Texto
                            + "<br>" + palavras.getString("MASTER")
                            + "<br>" + palavras.getString("Scheduling algorithm:") + " " + I.getAlgoritmo();
                } else {
                    Texto = Texto
                            + "<br>" + palavras.getString("SLAVE");
                }
            }
            break;
            case 2: {
                Texto += palavras.getString("Local ID:") + " " + String.valueOf(I.getIdLocal())
                        + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(I.getIdGlobal())
                        + "<br>" + palavras.getString("Label:") + " " + I.getNome()
                        + "<br>" + palavras.getString("X1-coordinate:") + " " + String.valueOf(I.getNumX())
                        + "<br>" + palavras.getString("Y1-coordinate:") + " " + String.valueOf(I.getNumY())
                        + "<br>" + palavras.getString("X2-coordinate:") + " " + String.valueOf(I.getNumPreX())
                        + "<br>" + palavras.getString("Y2-coordinate:") + " " + String.valueOf(I.getNumPreY())
                        + "<br>" + palavras.getString("Bandwidth:") + " " + String.valueOf(I.getBanda())
                        + "<br>" + palavras.getString("Latency:") + " " + String.valueOf(I.getLatencia())
                        + "<br>" + palavras.getString("Load Factor:") + " " + String.valueOf(I.getTaxaOcupacao());
            }
            break;
            case 3: {
                Texto += palavras.getString("Local ID:") + " " + String.valueOf(I.getIdLocal())
                        + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(I.getIdGlobal())
                        + "<br>" + palavras.getString("Label:") + " " + I.getNome()
                        + "<br>" + palavras.getString("X-coordinate:") + " " + String.valueOf(I.getNumX())
                        + "<br>" + palavras.getString("Y-coordinate:") + " " + String.valueOf(I.getNumY())
                        + "<br>" + palavras.getString("Number of slaves:") + " " + String.valueOf(I.getNumeroEscravos())
                        + "<br>" + palavras.getString("Computing power:") + " " + String.valueOf(I.getPoderComputacional())
                        + "<br>" + palavras.getString("Bandwidth:") + " " + String.valueOf(I.getBanda())
                        + "<br>" + palavras.getString("Latency:") + " " + String.valueOf(I.getLatencia())
                        + "<br>" + palavras.getString("Scheduling algorithm:") + " " + I.getAlgoritmo();
            }
            break;
            case 4: {
                Texto += palavras.getString("Local ID:") + " " + String.valueOf(I.getIdLocal())
                        + "<br>" + palavras.getString("Global ID:") + " " + String.valueOf(I.getIdGlobal())
                        + "<br>" + palavras.getString("Label:") + " " + I.getNome()
                        + "<br>" + palavras.getString("X-coordinate:") + " " + String.valueOf(I.getNumX())
                        + "<br>" + palavras.getString("Y-coordinate:") + " " + String.valueOf(I.getNumY())
                        + "<br>" + palavras.getString("Bandwidth:") + " " + String.valueOf(I.getBanda())
                        + "<br>" + palavras.getString("Latency:") + " " + String.valueOf(I.getLatencia())
                        + "<br>" + palavras.getString("Load Factor:") + " " + String.valueOf(I.getTaxaOcupacao());
            }
            break;
        }
        if (imprimeNosConectados && I.getTipoIcone() != 2) {
            Texto = Texto + "<br>" + palavras.getString("Output Connection:");
            for (int i : listaSaida) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
            Texto = Texto + "<br>" + palavras.getString("Input Connection:");
            for (int i : listaEntrada) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
        }
        if (imprimeNosConectados && I.getTipoIcone() == 2) {
            Texto = Texto + "<br>" + palavras.getString("Source Node:") + " " + String.valueOf(I.getNoOrigem());
            Texto = Texto + "<br>" + palavras.getString("Destination Node:") + " " + String.valueOf(I.getNoDestino());
        }
        if (imprimeNosIndiretos && I.getTipoIcone() != 2) {
            listaEntrada = I.getObjetoNosIndiretosEntrada();
            listaSaida = I.getObjetoNosIndiretosSaida();
            Texto = Texto + "<br>" + palavras.getString("Output Nodes Indirectly Connected:");
            for (int i : listaSaida) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
            Texto = Texto + "<br>" + palavras.getString("Input Nodes Indirectly Connected:");
            for (int i : listaEntrada) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
        }
        if (imprimeNosEscalonaveis && I.getTipoIcone() != 2) {
            listaSaida = I.getObjetoNosEscalonaveis();
            Texto = Texto + "<br>" + palavras.getString("Schedulable Nodes:");
            for (int i : listaSaida) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
        }
        if (I.getTipoIcone() == 1 && I.isMestre()) {
            List<Integer> escravos = I.getEscravos();
            Texto = Texto + "<br>" + palavras.getString("Slave Nodes:");
            for (int i : escravos) {
                Texto = Texto + "<br>" + String.valueOf(i);
            }
        }
        Texto += "</html>";
        janelaPrincipal.setSelectedIcon(I, Texto);
    }

    /**
     * Transforma os icones da area de desenho em um Document xml dom
     *
     * @param descricao
     */
    public Document getDadosASalvar() {
        Document descricao = ispd.arquivo.IconicoXML.novoDocumento();
        Element system = descricao.createElement("system");
        system.setAttribute("version", "1");
        descricao.appendChild(system);
        for (String user : usuarios) {
            Element owner = descricao.createElement("owner");
            owner.setAttribute("id", user);
            system.appendChild(owner);
        }
        for (Icone I : icones) {
            Element aux = null;
            Element posicao = descricao.createElement("position");
            posicao.setAttribute("x", Integer.toString(I.getNumX()));
            posicao.setAttribute("y", Integer.toString(I.getNumY()));
            Element icon_id = descricao.createElement("icon_id");
            icon_id.setAttribute("global", Integer.toString(I.getIdGlobal()));
            icon_id.setAttribute("local", Integer.toString(I.getIdLocal()));
            switch (I.getTipoIcone()) {
                case Icone.MACHINE:
                    aux = descricao.createElement("machine");
                    aux.setAttribute("power", Double.toString(I.getPoderComputacional()));
                    aux.setAttribute("load", Double.toString(I.getTaxaOcupacao()));
                    aux.setAttribute("owner", I.getProprietario());
                    if (I.isMestre()) {
                        //preenche escravos
                        Element master = descricao.createElement("master");
                        master.setAttribute("scheduler", I.getAlgoritmo());
                        for (Integer escravo : I.getEscravos()) {
                            Element slave = descricao.createElement("slave");
                            slave.setAttribute("id", escravo.toString());
                            master.appendChild(slave);
                        }
                        aux.appendChild(master);
                    }
                    break;
                case Icone.NETWORK:
                    aux = descricao.createElement("link");
                    aux.setAttribute("bandwidth", Double.toString(I.getBanda()));
                    aux.setAttribute("load", Double.toString(I.getTaxaOcupacao()));
                    aux.setAttribute("latency", Double.toString(I.getLatencia()));
                    Element connect = descricao.createElement("connect");
                    connect.setAttribute("origination", Integer.toString(I.getNoOrigem()));
                    connect.setAttribute("destination", Integer.toString(I.getNoDestino()));
                    aux.appendChild(connect);
                    aux.appendChild(posicao);
                    posicao = descricao.createElement("position");
                    posicao.setAttribute("x", Integer.toString(I.getNumPreX()));
                    posicao.setAttribute("y", Integer.toString(I.getNumPreY()));
                    break;
                case Icone.CLUSTER:
                    aux = descricao.createElement("cluster");
                    aux.setAttribute("nodes", Integer.toString(I.getNumeroEscravos()));
                    aux.setAttribute("power", Double.toString(I.getPoderComputacional()));
                    aux.setAttribute("bandwidth", Double.toString(I.getBanda()));
                    aux.setAttribute("latency", Double.toString(I.getLatencia()));
                    aux.setAttribute("scheduler", I.getAlgoritmo());
                    aux.setAttribute("owner", I.getProprietario());
                    aux.setAttribute("master", I.isMestre().toString());
                    break;
                case Icone.INTERNET:
                    aux = descricao.createElement("internet");
                    aux.setAttribute("bandwidth", Double.toString(I.getBanda()));
                    aux.setAttribute("load", Double.toString(I.getTaxaOcupacao()));
                    aux.setAttribute("latency", Double.toString(I.getLatencia()));
                    break;
            }
            if (aux != null) {
                aux.setAttribute("id", I.getNome());
                aux.appendChild(posicao);
                aux.appendChild(icon_id);
                system.appendChild(aux);
            }
        }
        //configurar carga
        if (cargasConfiguracao != null) {
            Element load = descricao.createElement("load");
            if (cargasConfiguracao.getTipo() == GerarCarga.RANDOM) {
                CargaRandom random = (CargaRandom) cargasConfiguracao;
                Element xmlRandom = descricao.createElement("random");
                xmlRandom.setAttribute("tasks", random.getNumeroTarefas().toString());
                xmlRandom.setAttribute("time_arrival", random.getTimeToArrival().toString());
                Element size = descricao.createElement("size");
                size.setAttribute("type", "computing");
                size.setAttribute("maximum", random.getMaxComputacao().toString());
                size.setAttribute("average", random.getAverageComputacao().toString());
                size.setAttribute("minimum", random.getMinComputacao().toString());
                size.setAttribute("probability", random.getProbabilityComputacao().toString());
                xmlRandom.appendChild(size);
                size = descricao.createElement("size");
                size.setAttribute("type", "communication");
                size.setAttribute("maximum", random.getMaxComunicacao().toString());
                size.setAttribute("average", random.getAverageComunicacao().toString());
                size.setAttribute("minimum", random.getMinComunicacao().toString());
                size.setAttribute("probability", random.getProbabilityComunicacao().toString());
                xmlRandom.appendChild(size);
                load.appendChild(xmlRandom);
            } else if (cargasConfiguracao.getTipo() == GerarCarga.FORNODE) {
                CargaForNode cargaNo = (CargaForNode) cargasConfiguracao;
                List<CargaTaskNode> listaCargas = cargaNo.getConfiguracaoNo();
                for (int i = 0; i < listaCargas.size(); i++) {
                    Element xmlNode = descricao.createElement("node");
                    xmlNode.setAttribute("application", listaCargas.get(i).getAplicacao());
                    xmlNode.setAttribute("owner", listaCargas.get(i).getProprietario());
                    xmlNode.setAttribute("id_master", listaCargas.get(i).getEscalonador());
                    xmlNode.setAttribute("tasks", listaCargas.get(i).getNumeroTarefas().toString());
                    Element size = descricao.createElement("size");
                    size.setAttribute("type", "computing");
                    size.setAttribute("maximum", listaCargas.get(i).getMaxComputacao().toString());
                    size.setAttribute("minimum", listaCargas.get(i).getMinComputacao().toString());
                    xmlNode.appendChild(size);
                    size = descricao.createElement("size");
                    size.setAttribute("type", "communication");
                    size.setAttribute("maximum", listaCargas.get(i).getMaxComunicacao().toString());
                    size.setAttribute("minimum", listaCargas.get(i).getMinComunicacao().toString());
                    xmlNode.appendChild(size);
                    load.appendChild(xmlNode);
                }
            } else if (cargasConfiguracao.getTipo() == GerarCarga.TRACE) {
                CargaTrace trace = (CargaTrace) cargasConfiguracao;
                List traceV = trace.toVector();
                Element xmlTrace = descricao.createElement("trace");
                xmlTrace.setAttribute("file_path", trace.toString());
                xmlTrace.setAttribute("tasks", traceV.get(2).toString());
                xmlTrace.setAttribute("format", traceV.get(1).toString());
                load.appendChild(xmlTrace);
            }
            system.appendChild(load);
        }
        return descricao;
    }

    /**
     * Carrega a estrutura da arvore contida no Document para os icones da area
     * de desenho
     *
     * @param descricao carregado a partir de um arquivo .imsx
     */
    public void setDadosSalvos(Document descricao) {

        //Realiza leitura dos usuários/proprietários do modelo
        this.usuarios = IconicoXML.newSetUsers(descricao);
        //Realiza leitura dos icones
        this.icones = IconicoXML.newIcones(descricao);
        //Realiza leitura da configuração de carga do modelo
        this.cargasConfiguracao = IconicoXML.newGerarCarga(descricao);
        //Atuasliza número de vertices e arestas
        for (Icone icone : icones) {
            if (icone.getTipoIcone() == Icone.NETWORK && this.numArestas < icone.getIdLocal()) {
                this.numArestas = icone.getIdLocal();
            }
            if (icone.getTipoIcone() != Icone.NETWORK && this.numVertices < icone.getIdLocal()) {
                this.numVertices = icone.getIdLocal();
            }
            if (this.numIcones < icone.getIdGlobal()) {
                this.numIcones = icone.getIdGlobal();
            }
        }
        this.numIcones++;
        this.numVertices++;
        this.numArestas++;
        atualizaNosIndiretos();
        repaint();
    }

    public BufferedImage createImage() {
        int maiorx = 0;
        int maiory = 0;

        for (Icone I : icones) {
            if (I.getNumX() > maiorx) {
                maiorx = I.getNumX();
            }
            if (I.getNumY() > maiory) {
                maiory = I.getNumY();
            }
        }

        BufferedImage image = new BufferedImage(maiorx + 50, maiory + 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D gc = (Graphics2D) image.getGraphics();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setColor(new Color(255, 255, 255));
        gc.fillRect(0, 0, maiorx + 50, maiory + 50);
        gc.setColor(new Color(220, 220, 220));

        if (metric) {
            units = (int) ((double) INCH / (double) 2.54);
        } else {
            units = (int) INCH / 2;
        }
        if (gridOn) {
            for (int _w = 0; _w
                    <= maiorx + 50; _w += units) {
                gc.drawLine(_w, 0, _w, maiory + 50);
            }
            for (int _h = 0; _h
                    <= maiory + 50; _h += units) {
                gc.drawLine(0, _h, maiorx + 50, _h);
            }
        }

        // Desenhamos todos os icones
        for (Icone I : icones) {
            if (I.getTipoIcone() == 2) {
                I.draw(gc);
            }
        }
        for (Icone I : icones) {
            if (I.getTipoIcone() != 2) {
                I.draw(gc);
            }
        }
        return image;
    }

    /**
     * Metodo publico para efetuar a copia dos valores de uma conexão de rede
     * especifica informada pelo usuário para as demais conexões de rede.
     */
    public void matchNetwork() {
        if (iconeAuxiliarMatchRede.getTipoIcone() == 2) {
            double banda, taxa, latencia;
            int intMatch = iconeAuxiliarMatchRede.getIdGlobal();
            banda = iconeAuxiliarMatchRede.getBanda();
            taxa = iconeAuxiliarMatchRede.getTaxaOcupacao();
            latencia = iconeAuxiliarMatchRede.getLatencia();

            for (Icone I : icones) {
                if (I.getTipoIcone() == 2 && I.getIdGlobal() != intMatch) {
                    I.setNome("lan" + I.getIdGlobal());
                    I.setBanda(banda);
                    I.setTaxaOcupacao(taxa);
                    I.setLatencia(latencia);
                    ValidaValores.addNomeIcone(I.getNome());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, palavras.getString("Please select a network icon"), palavras.getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /*
     * Organiza icones na área de desenho
     */
    public void iconArrange() {
        //Distancia entre os icones
        int TAMANHO = 100;
        //posição inicial
        int linha = TAMANHO, coluna = TAMANHO;
        int pos_coluna = 0, totalVertice = 0;
        for (Icone icone : icones) {
            if (icone.getTipoIcone() != Icone.NETWORK) {
                totalVertice++;
            }
        }
        //número de elementos por linha
        int num_coluna = ((int) Math.sqrt(totalVertice)) + 1;
        //Organiza os icones na tela
        for (Icone icone : icones) {
            if (icone.getTipoIcone() != Icone.NETWORK) {
                icone.setPosition(coluna, linha);
                //busca por arestas conectadas ao vertice
                conectarRede(icone);
                coluna += TAMANHO;
                pos_coluna++;
                if (pos_coluna == num_coluna) {
                    pos_coluna = 0;
                    coluna = TAMANHO;
                    linha += TAMANHO;
                }
            }
        }
    }

    /*
     * Organiza icones na área de desenho
     */
    public void iconArrangeType() {
        //Distancia entre os icones
        int TAMANHO = 75;
        //posição inicial
        int posX = TAMANHO, posY = TAMANHO;
        List<Icone> mestres = new ArrayList<Icone>();
        List<Icone> internets = new ArrayList<Icone>();
        for (Icone icone : icones) {
            if (icone.getTipoIcone() == Icone.MACHINE && icone.isMestre()) {
                mestres.add(icone);
            } else if (icone.getTipoIcone() == Icone.INTERNET) {
                internets.add(icone);
            }
        }
        for (Icone icone : mestres) {
            int num_voltas = 2;
            int num_coluna = 3;
            if (icone.getEscravos().size() > 8) {
                num_voltas = (icone.getEscravos().size() - 8) / 4;
                num_coluna = 3 + (((int) num_voltas / 2) - 1) * 2;
            }
            int inicioX = posX;
            int inicioY = posY;
            int pos_coluna = 0;
            //mestre
            int posMestreX = inicioX + (TAMANHO * ((int) num_voltas / 2));
            int posMestreY = inicioY + (TAMANHO * ((int) num_voltas / 2));
            icone.setPosition(posMestreX, posMestreY);
            conectarRede(icone);
            //Escravos
            for (Icone icon : icones) {
                if (icone.getEscravos().contains(icon.getIdGlobal())) {
                    icon.setPosition(posX, posY);
                    //busca por arestas conectadas ao vertice
                    conectarRede(icon);
                    posX += TAMANHO;
                    pos_coluna++;
                    if (posX == posMestreX && posY == posMestreY) {
                        posX += TAMANHO;
                        pos_coluna++;
                    }
                    if (pos_coluna == num_coluna) {
                        pos_coluna = 0;
                        posX = inicioX;
                        posY += TAMANHO;
                    }
                }
            }
        }
        for (Icone icone : internets) {
            int num_voltas = 1;
            int num_coluna = 3;
            //adiciona elementos conectados no ícone de internet
            icone.getEscravos().clear();
            for (Icone I : icones) {
                if (I.getTipoIcone() == Icone.NETWORK) {
                    if (I.getNoDestino() == icone.getIdGlobal()) {
                        icone.getEscravos().add(I.getNoOrigem());
                    } else if (I.getNoOrigem() == icone.getIdGlobal()) {
                        icone.getEscravos().add(I.getNoDestino());
                    }
                }
            }
            if (icone.getEscravos().size() > 8) {
                num_voltas = (icone.getEscravos().size() - 8) / 4;
                num_coluna = 3 + (((int) num_voltas / 2) - 1) * 2;
            }
            int inicioX = posX;
            int inicioY = posY;
            int pos_coluna = 0;
            //mestre
            int posMestreX = inicioX + (TAMANHO * ((int) num_voltas / 2));
            int posMestreY = inicioY + (TAMANHO * ((int) num_voltas / 2));
            icone.setPosition(posMestreX, posMestreY);
            conectarRede(icone);
            //Escravos
            for (Icone icon : icones) {
                if (posX == posMestreX && posY == posMestreY) {
                    posX += TAMANHO;
                    pos_coluna++;
                }
                if (icone.getEscravos().contains(icon.getIdGlobal())
                        && icon.getTipoIcone() != Icone.INTERNET) {
                    icon.setPosition(posX, posY);
                    //busca por arestas conectadas ao vertice
                    conectarRede(icon);
                    posX += TAMANHO;
                    pos_coluna++;
                    if (pos_coluna == num_coluna) {
                        pos_coluna = 0;
                        posX = inicioX;
                        posY += TAMANHO;
                    }
                }
            }
        }
    }

    /**
     * Busca pelas arestas conectadas ao vertice Ao encontrar ajusta a posição
     * na tela
     *
     * @param vertice elemento que teve sua posição alterada
     */
    private void conectarRede(Icone vertice) {
        //busca por arestas conectadas ao vertice
        for (Icone I : icones) {
            if (I.getTipoIcone() == Icone.NETWORK && I.getNoOrigem() == vertice.getIdGlobal()) {
                I.setPrePosition(vertice.getNumX(), vertice.getNumY());
            }
            if (I.getTipoIcone() == Icone.NETWORK && I.getNoDestino() == vertice.getIdGlobal()) {
                I.setPosition(vertice.getNumX(), vertice.getNumY());
            }
        }
    }

    public void setIdioma(ResourceBundle palavras) {
        this.palavras = palavras;
        this.initTexts();
    }

    void setDadosSalvos(DescreveSistema descricao) {
        List<DescreveIcone> lista;
        ValidaValores.setListaNos(descricao.getListaNosLista());
        this.numIcones = descricao.getNumIcones();
        this.numVertices = descricao.getNumVertices();
        this.numArestas = descricao.getNumArestas();
        if (descricao.getCargasTipoConfiguracao() == GerarCarga.RANDOM) {
            this.cargasConfiguracao = CargaRandom.newGerarCarga(descricao.getCargasConfiguracao());
        } else if (descricao.getCargasTipoConfiguracao() == GerarCarga.FORNODE) {
            this.cargasConfiguracao = CargaForNode.newGerarCarga(descricao.getCargasConfiguracao());
        }

        lista = descricao.getIconeLista();
        for (DescreveIcone Ico : lista) {
            Icone I = new Icone(Ico.getX(), Ico.getY(), Ico.getPreX(), Ico.getPreY(), Ico.getTipoIcone(), Ico.getIdLocal(), Ico.getIdGlobal());
            icones.add(I);
            I.setNome(Ico.getNome());
            I.setPoderComputacional(Ico.getPoderComputacional());
            I.setTaxaOcupacao(Ico.getTaxaOcupacao());
            I.setLatencia(Ico.getLatencia());
            I.setBanda(Ico.getBanda());
            I.setMestre(Ico.getMestre());
            I.setAlgoritmo(Ico.getAlgoritmoEscalonamento());
            I.setEscravos(Ico.getEscravos());
            I.setConexaoEntrada(Ico.getConexaoEntrada());
            I.setConexaoSaida(Ico.getConexaoSaida());
            I.setNoDestino(Ico.getNoDestino());
            I.setNoOrigem(Ico.getNoOrigem());
            I.setNumeroEscravos(Ico.getNumeroEscravos());
        }
        atualizaNosIndiretos();
        repaint();
    }

    public Vector<String> getNosEscalonadores() {
        Vector<String> maquinas = new Vector<String>();

        for (Icone I : icones) {
            if (I.getTipoIcone() == 1 && I.isMestre()) {
                maquinas.add(I.getNome());
            }
            if (I.getTipoIcone() == 3 && I.isMestre()) {
                maquinas.add(I.getNome());
            }
        }
        return maquinas;
    }

    public RedeDeFilas getRedeDeFilas() {
        List<CS_Processamento> mestres = new ArrayList<CS_Processamento>();
        List<Integer> mestresNome = new ArrayList<Integer>();
        List<CS_Switch> clusters = new ArrayList<CS_Switch>();
        List<Integer> clustersNome = new ArrayList<Integer>();
        List<CS_Maquina> maqs = new ArrayList<CS_Maquina>();
        List<Integer> maqsNome = new ArrayList<Integer>();
        List<CS_Comunicacao> links = new ArrayList<CS_Comunicacao>();
        List<CS_Internet> nets = new ArrayList<CS_Internet>();
        List<Integer> netsNome = new ArrayList<Integer>();
        //cria lista de usuarios e o poder computacional cedido por cada um
        List<String> proprietarios = new ArrayList<String>();
        List<Double> poderComp = new ArrayList<Double>();
        for (Icone icone : getIcones()) {
            if (icone.getTipoIcone() == Icone.MACHINE) {
                if (proprietarios.contains(icone.getProprietario())) {
                    int index = proprietarios.indexOf(icone.getProprietario());
                    poderComp.set(index, poderComp.get(index) + icone.getPoderComputacional());
                } else {
                    proprietarios.add(icone.getProprietario());
                    poderComp.add(icone.getPoderComputacional());
                }
            } else if (icone.getTipoIcone() == Icone.CLUSTER && !icone.isMestre()) {
                if (proprietarios.contains(icone.getProprietario())) {
                    int index = proprietarios.indexOf(icone.getProprietario());
                    poderComp.set(index, poderComp.get(index) + (icone.getPoderComputacional() * icone.getNumeroEscravos()));
                } else {
                    proprietarios.add(icone.getProprietario());
                    poderComp.add(icone.getPoderComputacional() * icone.getNumeroEscravos());
                }
            } else if (icone.getTipoIcone() == Icone.CLUSTER && icone.isMestre()) {
                if (proprietarios.contains(icone.getProprietario())) {
                    int index = proprietarios.indexOf(icone.getProprietario());
                    poderComp.set(index, poderComp.get(index) + (icone.getPoderComputacional() * icone.getNumeroEscravos()) + icone.getPoderComputacional());
                } else {
                    proprietarios.add(icone.getProprietario());
                    poderComp.add((icone.getPoderComputacional() * icone.getNumeroEscravos()) + icone.getPoderComputacional());
                }
            }
        }
        //cria maquinas, mestres, internets e mestres dos clusters
        for (Icone icone : getIcones()) {
            switch (icone.getTipoIcone()) {
                case Icone.MACHINE:
                    if (icone.isMestre()) {
                        CS_Mestre mestre = new CS_Mestre(
                                icone.getNome(),
                                icone.getProprietario(),
                                icone.getPoderComputacional(),
                                icone.getTaxaOcupacao(),
                                icone.getAlgoritmo()/*Escalonador*/);
                        mestres.add(mestre);
                        mestresNome.add(icone.getIdGlobal());
                    } else {
                        CS_Maquina maq = new CS_Maquina(
                                icone.getNome(),
                                icone.getProprietario(),
                                icone.getPoderComputacional(),
                                1/*num processadores*/,
                                icone.getTaxaOcupacao());
                        maqs.add(maq);
                        maqsNome.add(icone.getIdGlobal());
                    }
                    break;
                case Icone.CLUSTER:
                    if (icone.isMestre()) {
                        CS_Mestre clust = new CS_Mestre(
                                icone.getNome(),
                                icone.getProprietario(),
                                icone.getPoderComputacional(),
                                icone.getTaxaOcupacao(),
                                icone.getAlgoritmo()/*Escalonador*/);
                        mestres.add(clust);
                        mestresNome.add(icone.getIdGlobal());
                    } else {
                        CS_Switch Switch = new CS_Switch(
                                icone.getNome(),
                                icone.getBanda(),
                                icone.getTaxaOcupacao(),
                                icone.getLatencia());
                        clusters.add(Switch);
                        clustersNome.add(icone.getIdGlobal());
                    }
                    break;
                case Icone.INTERNET:
                    CS_Internet net = new CS_Internet(
                            icone.getNome(),
                            icone.getBanda(),
                            icone.getTaxaOcupacao(),
                            icone.getLatencia());
                    nets.add(net);
                    netsNome.add(icone.getIdGlobal());
                    break;
            }
        }
        //cria os links e realiza a conexão entre os recursos
        for (Icone icone : getIcones()) {
            if (icone.getTipoIcone() == Icone.NETWORK) {
                CS_Link link = new CS_Link(
                        icone.getNome(),
                        icone.getBanda(),
                        icone.getTaxaOcupacao(),
                        icone.getLatencia());
                links.add(link);
                if (mestresNome.contains(icone.getNoDestino())) {
                    int index = mestresNome.indexOf(icone.getNoDestino());
                    CS_Mestre mestre = (CS_Mestre) mestres.get(index);
                    link.setConexoesSaida(mestre);
                    mestre.addConexoesEntrada(link);
                } else if (maqsNome.contains(icone.getNoDestino())) {
                    int index = maqsNome.indexOf(icone.getNoDestino());
                    CS_Maquina maq = (CS_Maquina) maqs.get(index);
                    link.setConexoesSaida(maq);
                    maq.addConexoesEntrada(link);
                } else if (netsNome.contains(icone.getNoDestino())) {
                    int index = netsNome.indexOf(icone.getNoDestino());
                    CS_Internet net = nets.get(index);
                    link.setConexoesSaida(net);
                    net.addConexoesEntrada(link);
                } else if (clustersNome.contains(icone.getNoDestino())) {
                    int index = clustersNome.indexOf(icone.getNoDestino());
                    CS_Switch swt = clusters.get(index);
                    link.setConexoesSaida(swt);
                    swt.addConexoesEntrada(link);
                }
                if (mestresNome.contains(icone.getNoOrigem())) {
                    int index = mestresNome.indexOf(icone.getNoOrigem());
                    CS_Mestre mestre = (CS_Mestre) mestres.get(index);
                    link.setConexoesEntrada(mestre);
                    mestre.addConexoesSaida(link);
                } else if (maqsNome.contains(icone.getNoOrigem())) {
                    int index = maqsNome.indexOf(icone.getNoOrigem());
                    CS_Maquina maq = (CS_Maquina) maqs.get(index);
                    link.setConexoesEntrada(maq);
                    maq.addConexoesSaida(link);
                } else if (netsNome.contains(icone.getNoOrigem())) {
                    int index = netsNome.indexOf(icone.getNoOrigem());
                    CS_Internet net = nets.get(index);
                    link.setConexoesEntrada(net);
                    net.addConexoesSaida(link);
                } else if (clustersNome.contains(icone.getNoOrigem())) {
                    int index = clustersNome.indexOf(icone.getNoOrigem());
                    CS_Switch swt = clusters.get(index);
                    link.setConexoesEntrada(swt);
                    swt.addConexoesSaida(link);
                }
            }
        }
        //adiciona os escravos aos mestres
        for (Icone icone : getIcones()) {
            if (icone.isMestre() && icone.getTipoIcone() != Icone.CLUSTER) {
                for (Integer escravo : icone.getEscravos()) {
                    if (maqsNome.contains(escravo)) {
                        int index = maqsNome.indexOf(escravo);
                        CS_Processamento maq = maqs.get(index);
                        index = mestresNome.indexOf(icone.getIdGlobal());
                        CS_Mestre mest = (CS_Mestre) mestres.get(index);
                        mest.addEscravo(maq);
                        if (maq instanceof CS_Maquina) {
                            CS_Maquina maqTemp = (CS_Maquina) maq;
                            maqTemp.addMestre(mest);
                        }
                    } else if (mestresNome.contains(escravo)) {
                        int index = mestresNome.indexOf(escravo);
                        CS_Processamento maq = mestres.get(index);
                        index = mestresNome.indexOf(icone.getIdGlobal());
                        CS_Mestre mest = (CS_Mestre) mestres.get(index);
                        mest.addEscravo(maq);
                    }
                }
            }
        }
        //cria os escravos dos clusters e realiza a conexão
        for (Icone icone : getIcones()) {
            if (icone.isMestre() && icone.getTipoIcone() == Icone.CLUSTER) {
                int index = mestresNome.indexOf(icone.getIdGlobal());
                CS_Mestre mestreCluster = (CS_Mestre) mestres.get(index);
                CS_Switch Switch = new CS_Switch(
                        icone.getNome(),
                        icone.getBanda(),
                        icone.getTaxaOcupacao(),
                        icone.getLatencia());
                links.add(Switch);
                mestreCluster.addConexoesEntrada(Switch);
                mestreCluster.addConexoesSaida(Switch);
                Switch.addConexoesEntrada(mestreCluster);
                Switch.addConexoesSaida(mestreCluster);
                for (int i = 0; i < icone.getNumeroEscravos(); i++) {
                    CS_Maquina maq = new CS_Maquina(
                            icone.getNome(),
                            icone.getProprietario(),
                            icone.getPoderComputacional(),
                            1/*numero de processadores*/,
                            icone.getTaxaOcupacao(),
                            i + 1);
                    maq.addConexoesSaida(Switch);
                    maq.addConexoesEntrada(Switch);
                    Switch.addConexoesEntrada(maq);
                    Switch.addConexoesSaida(maq);
                    maq.addMestre(mestreCluster);
                    mestreCluster.addEscravo(maq);
                    maqs.add(maq);
                }
            } else if (icone.getTipoIcone() == Icone.CLUSTER) {
                List<CS_Maquina> maqTemp = new ArrayList<CS_Maquina>();
                int index = clustersNome.indexOf(icone.getIdGlobal());
                CS_Switch Switch = clusters.get(index);
                links.add(Switch);
                for (int i = 0; i < icone.getNumeroEscravos(); i++) {
                    CS_Maquina maq = new CS_Maquina(
                            icone.getNome(),
                            icone.getProprietario(),
                            icone.getPoderComputacional(),
                            1/*numero de processadores*/,
                            icone.getTaxaOcupacao(),
                            i + 1);
                    maq.addConexoesSaida(Switch);
                    maq.addConexoesEntrada(Switch);
                    Switch.addConexoesEntrada(maq);
                    Switch.addConexoesSaida(maq);
                    maqTemp.add(maq);
                    maqs.add(maq);
                }
                //adiciona os escravos aos mestres
                Icone[] mestresArray = new Icone[getIcones().size()];
                getIcones().toArray(mestresArray);
                for (int i = 0; i < mestresArray.length; i++) {
                    if (mestresArray[i].isMestre() && mestresArray[i].getTipoIcone() != Icone.CLUSTER) {
                        if (mestresArray[i].getEscravos().contains(icone.getIdGlobal())) {
                            index = mestresNome.indexOf(mestresArray[i].getIdGlobal());
                            CS_Mestre mest = (CS_Mestre) mestres.get(index);
                            for (CS_Maquina maq : maqTemp) {
                                mest.addEscravo(maq);
                                maq.addMestre(mest);
                            }
                        }
                    }
                }
            }
        }
        //verifica se há usuarios sem nenhum recurso
        for (String user : this.usuarios) {
            if (!proprietarios.contains(user)) {
                proprietarios.add(user);
                poderComp.add(0.0);
            }
        }
        //cria as métricas de usuarios para cada mestre
        for (CS_Processamento mestre : mestres) {
            CS_Mestre mst = (CS_Mestre) mestre;
            MetricasUsuarios mu = new MetricasUsuarios();
            mu.addAllUsuarios(proprietarios, poderComp);
            mst.getEscalonador().setMetricaUsuarios(mu);
        }
        RedeDeFilas rdf = new RedeDeFilas(mestres, maqs, links, nets);
        //cria as métricas de usuarios globais da rede de filas
        MetricasUsuarios mu = new MetricasUsuarios();
        mu.addAllUsuarios(proprietarios, poderComp);
        rdf.setUsuarios(proprietarios);
        return rdf;
    }
}