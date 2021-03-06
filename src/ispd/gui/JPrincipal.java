/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * JPrincipal.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Created on 11/04/2011
 * 09-Set-2014 : Version 2.0;
 * 14-Out-2014 : adicionado configurações para simulação e resultados
 *
 */
package ispd.gui;

import ispd.arquivo.exportador.Exportador;
import ispd.arquivo.interpretador.gridsim.InterpretadorGridSim;
import ispd.arquivo.interpretador.simgrid.InterpretadorSimGrid;
import ispd.arquivo.xml.ConfiguracaoISPD;
import DescreveSistema.DescreveSistema;
import ispd.arquivo.exportador.Exportador;
import ispd.arquivo.xml.IconicoXML;
import ispd.arquivo.interpretador.gridsim.InterpretadorGridSim;
import ispd.arquivo.interpretador.simgrid.InterpretadorSimGrid;
import ispd.gui.auxiliar.Corner;
import ispd.gui.auxiliar.FiltroDeArquivos;
import ispd.gui.auxiliar.HtmlPane;
import ispd.gui.configuracao.JPanelConfigIcon;
import ispd.gui.iconico.grade.DesenhoGrade;
import ispd.gui.iconico.grade.ItemGrade;
import ispd.gui.JSelecionarFalhas;
import ispd.gui.*;
import ispd.gui.iconico.grade.VirtualMachine;
import ispd.gui.iconico.simulacao.JSimulacaoGrafica;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * Implementação da janela principal do iSPD
 *
 * @author denison
 *
 */
public class JPrincipal extends javax.swing.JFrame implements KeyListener {


    private EscolherClasse ChooseClass; //janela de escolha de qual tipo de serviço irá ser modelado
    private ConfigurarVMs JanelaVM; //janela de configuração de máquinas virtuais para IaaS
    private int tipoModelo; //define se o modelo é GRID, IAAS ou PAAS;

    public int getTipoModelo() {
        return tipoModelo;
    }

    public void setTipoModelo(int tipoModelo) {
        this.tipoModelo = tipoModelo;
    }

    /**
     * Creates new form Principal
     */
    public JPrincipal() {
        //Utiliza o idioma do sistema como padrão
        Locale locale = Locale.getDefault();
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", locale);
        configura = new ConfiguracaoISPD();
        String[] exts = {".ims", ".imsx", ".wmsx"};
        filtro = new FiltroDeArquivos(palavras.getString("Iconic Model of Simulation"), exts, true);
        initComponents();
        jFileChooser.setSelectedFile(configura.getLastFile());
        jSobre = new JSobre(this, true);
        jSobre.setLocationRelativeTo(this);
        // permite que o frame processe os eventos de teclado
        this.addKeyListener(this);
        this.jScrollPaneAreaDesenho.addKeyListener(this);
        this.jScrollPaneBarraLateral.addKeyListener(this);
        this.jScrollPaneBarraNotifica.addKeyListener(this);
        this.jScrollPaneProperties.addKeyListener(this);
        this.jTextAreaNotifica.addKeyListener(this);
        this.jToolBar.addKeyListener(this);
        this.jToggleButtonCluster.addKeyListener(this);
        this.jToggleButtonInternet.addKeyListener(this);
        this.jToggleButtonMaquina.addKeyListener(this);
        this.jToggleButtonRede.addKeyListener(this);
        this.jButtonTarefas.addKeyListener(this);
        this.jButtonUsuarios.addKeyListener(this);
        this.jButtonSimular.addKeyListener(this);
        //paineis de configuração
        this.jPanelSimples.addKeyListener(this);
        this.jPanelConfiguracao.addKeyListener(this);
        this.jPanelPropriedades.addKeyListener(this);
        this.maquinasVirtuais = null;
    }

    public HashSet<VirtualMachine> getMaquinasVirtuais() {
        return maquinasVirtuais;
    }

    public void setMaquinasVirtuais(HashSet<VirtualMachine> maquinasVirtuais) {
        this.maquinasVirtuais = maquinasVirtuais;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrameGerenciador = new ispd.gui.GerenciarEscalonador();
        jFrameGerenciadorCloud = new ispd.gui.GerenciarEscalonadorCloud();
        jFrameGerenciadorAlloc = new ispd.gui.GerenciarAlocadores();
        jPanelSimples = new ispd.gui.configuracao.JPanelSimples();
        jPanelSimples.setjLabelTexto(palavras.getString("No icon selected."));
        jFileChooser = new javax.swing.JFileChooser();
        jPanelConfiguracao = new ispd.gui.configuracao.JPanelConfigIcon();
        jPanelConfiguracao.setEscalonadores(jFrameGerenciador.getEscalonadores());
        jPanelConfiguracao.setEscalonadoresCloud(jFrameGerenciadorCloud.getEscalonadores());
        jPanelConfiguracao.setAlocadores(jFrameGerenciadorAlloc.getAlocadores());
        jScrollPaneAreaDesenho = new javax.swing.JScrollPane();
        jScrollPaneBarraLateral = new javax.swing.JScrollPane();
        jScrollPaneBarraNotifica = new javax.swing.JScrollPane();
        jTextAreaNotifica = new javax.swing.JTextArea();
        jToolBar = new javax.swing.JToolBar();
        jToggleButtonMaquina = new javax.swing.JToggleButton();
        jToggleButtonRede = new javax.swing.JToggleButton();
        jToggleButtonCluster = new javax.swing.JToggleButton();
        jToggleButtonInternet = new javax.swing.JToggleButton();
        javax.swing.JToolBar.Separator jSeparator4 = new javax.swing.JToolBar.Separator();
        jButtonTarefas = new javax.swing.JButton();
        jButtonConfigVM = new javax.swing.JButton();
        jButtonUsuarios = new javax.swing.JButton();
        jButtonSimular = new javax.swing.JButton();
        jButtonInjetarFalhas = new javax.swing.JButton();
        jScrollPaneProperties = new javax.swing.JScrollPane();
        jPanelPropriedades = new ispd.gui.configuracao.JPanelSimples();
        jPanelSimples.setjLabelTexto(palavras.getString("No icon selected."));
        jMenuBar = new javax.swing.JMenuBar();
        jMenuArquivo = new javax.swing.JMenu();
        jMenuItemNovo = new javax.swing.JMenuItem();
        jMenuItemAbrir = new javax.swing.JMenuItem();
        jMenuItemSalvar = new javax.swing.JMenuItem();
        jMenuItemSalvarComo = new javax.swing.JMenuItem();
        jMenuItemAbrirResult = new javax.swing.JMenuItem();
        jMenuImport = new javax.swing.JMenu();
        jMenuItemSimGrid = new javax.swing.JMenuItem();
        jMenuItemGridSim = new javax.swing.JMenuItem();
        jMenuExport = new javax.swing.JMenu();
        jMenuItemToJPG = new javax.swing.JMenuItem();
        jMenuItemToTXT = new javax.swing.JMenuItem();
        jMenuItemToSimGrid = new javax.swing.JMenuItem();
        jMenuItemToGridSim = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuIdioma = new javax.swing.JMenu();
        jMenuItemIngles = new javax.swing.JMenuItem();
        jMenuItemPortugues = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItemFechar = new javax.swing.JMenuItem();
        jMenuItemSair = new javax.swing.JMenuItem();
        jMenuEditar = new javax.swing.JMenu();
        jMenuItemCopy = new javax.swing.JMenuItem();
        jMenuItemPaste = new javax.swing.JMenuItem();
        jMenuItemDelete = new javax.swing.JMenuItem();
        jMenuItemEquiparar = new javax.swing.JMenuItem();
        jMenuItemOrganizar = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuExibir = new javax.swing.JMenu();
        jCheckBoxMenuItemConectado = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemIndireto = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemEscalonavel = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemGrade = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemRegua = new javax.swing.JCheckBoxMenuItem();
        jMenuFerramentas = new javax.swing.JMenu();
        jMenuItemGerenciar = new javax.swing.JMenuItem();
        jMenuItemGerar = new javax.swing.JMenuItem();
        jMenuItemGerenciarCloud = new javax.swing.JMenuItem();
        jMenuItemGerenciarAllocation = new javax.swing.JMenuItem();
        jMenuAjuda = new javax.swing.JMenu();
        jMenuItemAjuda = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSobre = new javax.swing.JMenuItem();

        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setFileFilter(filtro);
        jFileChooser.setFileView(new javax.swing.filechooser.FileView() {@Override
            public Icon getIcon(File f) {
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 && i < s.length() - 1) {
                    ext = s.substring(i + 1).toLowerCase();
                }
                if (ext != null) {
                    if (ext.equals("ims") || ext.equals("imsx")) {
                        java.net.URL imgURL = JPrincipal.class.getResource("imagens/Logo_iSPD_25.png");
                        if (imgURL != null) {
                            return new ImageIcon(imgURL);
                        }
                    }
                }
                return null;
            }});

            setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
            setTitle(palavras.getString("nomePrograma")); // NOI18N
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("imagens/Logo_iSPD_25.png")));
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    formWindowClosing(evt);
                }
            });

            jScrollPaneBarraLateral.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));

            jScrollPaneBarraNotifica.setBorder(javax.swing.BorderFactory.createTitledBorder(palavras.getString("Notifications"))); // NOI18N

            jTextAreaNotifica.setEditable(false);
            jTextAreaNotifica.setColumns(20);
            jTextAreaNotifica.setRows(5);
            jTextAreaNotifica.setBorder(null);
            jScrollPaneBarraNotifica.setViewportView(jTextAreaNotifica);

            jToolBar.setFloatable(false);

            jToggleButtonMaquina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/botao_no.gif"))); // NOI18N
            jToggleButtonMaquina.setToolTipText(palavras.getString("Selects machine icon for add to the model")); // NOI18N
            jToggleButtonMaquina.setEnabled(false);
            jToggleButtonMaquina.setFocusable(false);
            jToggleButtonMaquina.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jToggleButtonMaquina.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jToggleButtonMaquina.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jToggleButtonMaquinaActionPerformed(evt);
                }
            });
            jToolBar.add(jToggleButtonMaquina);

            jToggleButtonRede.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/botao_rede.gif"))); // NOI18N
            jToggleButtonRede.setToolTipText(palavras.getString("Selects network icon for add to the model")); // NOI18N
            jToggleButtonRede.setEnabled(false);
            jToggleButtonRede.setFocusable(false);
            jToggleButtonRede.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jToggleButtonRede.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jToggleButtonRede.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jToggleButtonRedeActionPerformed(evt);
                }
            });
            jToolBar.add(jToggleButtonRede);

            jToggleButtonCluster.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/botao_cluster.gif"))); // NOI18N
            jToggleButtonCluster.setToolTipText(palavras.getString("Selects cluster icon for add to the model")); // NOI18N
            jToggleButtonCluster.setEnabled(false);
            jToggleButtonCluster.setFocusable(false);
            jToggleButtonCluster.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jToggleButtonCluster.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jToggleButtonCluster.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jToggleButtonClusterActionPerformed(evt);
                }
            });
            jToolBar.add(jToggleButtonCluster);

            jToggleButtonInternet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/botao_internet.gif"))); // NOI18N
            jToggleButtonInternet.setToolTipText(palavras.getString("Selects internet icon for add to the model")); // NOI18N
            jToggleButtonInternet.setEnabled(false);
            jToggleButtonInternet.setFocusable(false);
            jToggleButtonInternet.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jToggleButtonInternet.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jToggleButtonInternet.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jToggleButtonInternetActionPerformed(evt);
                }
            });
            jToolBar.add(jToggleButtonInternet);
            jToolBar.add(jSeparator4);

            jButtonTarefas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/botao_tarefas.gif"))); // NOI18N
            jButtonTarefas.setToolTipText(palavras.getString("Selects insertion model of tasks")); // NOI18N
            jButtonTarefas.setEnabled(false);
            jButtonTarefas.setFocusable(false);
            jButtonTarefas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jButtonTarefas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jButtonTarefas.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonTarefasActionPerformed(evt);
                }
            });
            jToolBar.add(jButtonTarefas);

            jButtonConfigVM.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/vm_icon.png"))); // NOI18N
            jButtonConfigVM.setToolTipText("Add and remove the virtual machines");
            jButtonConfigVM.setEnabled(false);
            jButtonConfigVM.setFocusable(false);
            jButtonConfigVM.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jButtonConfigVM.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jButtonConfigVM.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonConfigVMActionPerformed(evt);
                }
            });
            jToolBar.add(jButtonConfigVM);

            jButtonUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/system-users.png"))); // NOI18N
            jButtonUsuarios.setToolTipText(palavras.getString("Add and remove users to the model")); // NOI18N
            jButtonUsuarios.setEnabled(false);
            jButtonUsuarios.setFocusable(false);
            jButtonUsuarios.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jButtonUsuarios.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jButtonUsuarios.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonUsuariosActionPerformed(evt);
                }
            });
            jToolBar.add(jButtonUsuarios);

            jButtonSimular.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/system-run.png"))); // NOI18N
            jButtonSimular.setText(palavras.getString("Simulate")); // NOI18N
            jButtonSimular.setToolTipText(palavras.getString("Starts the simulation")); // NOI18N
            jButtonSimular.setEnabled(false);
            jButtonSimular.setFocusable(false);
            jButtonSimular.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jButtonSimular.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jButtonSimular.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonSimularActionPerformed(evt);
                }
            });
            jToolBar.add(jButtonSimular);

            jButtonInjetarFalhas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/vermelho.png"))); // NOI18N
            jButtonInjetarFalhas.setText("Faults Injection");
            jButtonInjetarFalhas.setToolTipText("Select the faults");
            jButtonInjetarFalhas.setEnabled(false);
            jButtonInjetarFalhas.setFocusable(false);
            jButtonInjetarFalhas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jButtonInjetarFalhas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            jButtonInjetarFalhas.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonInjetarFalhasActionPerformed(evt);
                }
            });
            jToolBar.add(jButtonInjetarFalhas);

            jScrollPaneProperties.setBorder(javax.swing.BorderFactory.createTitledBorder(palavras.getString("Properties"))); // NOI18N
            jScrollPaneProperties.setViewportView(jPanelPropriedades);

            jMenuArquivo.setText(palavras.getString("File")); // NOI18N
            jMenuArquivo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuArquivoActionPerformed(evt);
                }
            });

            jMenuItemNovo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
            jMenuItemNovo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/insert-object_1.png"))); // NOI18N
            jMenuItemNovo.setText(palavras.getString("New")); // NOI18N
            jMenuItemNovo.setToolTipText(palavras.getString("Starts a new model")); // NOI18N
            jMenuItemNovo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemNovoActionPerformed(evt);
                }
            });
            jMenuArquivo.add(jMenuItemNovo);

            jMenuItemAbrir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
            jMenuItemAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-open.png"))); // NOI18N
            jMenuItemAbrir.setText(palavras.getString("Open")); // NOI18N
            jMenuItemAbrir.setToolTipText(palavras.getString("Opens an existing model")); // NOI18N
            jMenuItemAbrir.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemAbrirActionPerformed(evt);
                }
            });
            jMenuArquivo.add(jMenuItemAbrir);

            jMenuItemSalvar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
            jMenuItemSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-save_1.png"))); // NOI18N
            jMenuItemSalvar.setText(palavras.getString("Save")); // NOI18N
            jMenuItemSalvar.setToolTipText(palavras.getString("Save the open model")); // NOI18N
            jMenuItemSalvar.setEnabled(false);
            jMenuItemSalvar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemSalvarActionPerformed(evt);
                }
            });
            jMenuArquivo.add(jMenuItemSalvar);

            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("ispd/idioma/Idioma_en_US"); // NOI18N
            jMenuItemSalvarComo.setText(bundle.getString("Save as...")); // NOI18N
            jMenuItemSalvarComo.setEnabled(false);
            jMenuItemSalvarComo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemSalvarComoActionPerformed(evt);
                }
            });
            jMenuArquivo.add(jMenuItemSalvarComo);

            jMenuItemAbrirResult.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-open.png"))); // NOI18N
            jMenuItemAbrirResult.setText("Open Results");
            jMenuItemAbrirResult.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemAbrirResultActionPerformed(evt);
                }
            });
            jMenuArquivo.add(jMenuItemAbrirResult);

            jMenuImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-import.png"))); // NOI18N
            jMenuImport.setText(palavras.getString("Import")); // NOI18N

            jMenuItemSimGrid.setText(palavras.getString("SimGrid model")); // NOI18N
            jMenuItemSimGrid.setToolTipText(palavras.getString("Open model from the specification files of Simgrid")); // NOI18N
            jMenuItemSimGrid.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemSimGridActionPerformed(evt);
                }
            });
            jMenuImport.add(jMenuItemSimGrid);

            jMenuItemGridSim.setText(palavras.getString("GridSim model")); // NOI18N
            jMenuItemGridSim.setToolTipText(palavras.getString("Open model from the specification files of GridSim")); // NOI18N
            jMenuItemGridSim.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemGridSimActionPerformed(evt);
                }
            });
            jMenuImport.add(jMenuItemGridSim);

            jMenuArquivo.add(jMenuImport);

            jMenuExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-export.png"))); // NOI18N
            jMenuExport.setText(palavras.getString("Export")); // NOI18N

            jMenuItemToJPG.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, java.awt.event.InputEvent.CTRL_MASK));
            jMenuItemToJPG.setText(palavras.getString("to JPG")); // NOI18N
            jMenuItemToJPG.setToolTipText(palavras.getString("Creates a jpg file with the model image")); // NOI18N
            jMenuItemToJPG.setEnabled(false);
            jMenuItemToJPG.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemToJPGActionPerformed(evt);
                }
            });
            jMenuExport.add(jMenuItemToJPG);

            jMenuItemToTXT.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
            jMenuItemToTXT.setText(palavras.getString("to TXT")); // NOI18N
            jMenuItemToTXT.setToolTipText(palavras.getString("Creates a file in plain text with the model data according to the grammar of the iconic model")); // NOI18N
            jMenuItemToTXT.setEnabled(false);
            jMenuItemToTXT.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemToTXTActionPerformed(evt);
                }
            });
            jMenuExport.add(jMenuItemToTXT);

            jMenuItemToSimGrid.setText("to SimGrid");
            jMenuItemToSimGrid.setEnabled(false);
            jMenuItemToSimGrid.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemToSimGridActionPerformed(evt);
                }
            });
            jMenuExport.add(jMenuItemToSimGrid);

            jMenuItemToGridSim.setText("to GridSim");
            jMenuItemToGridSim.setEnabled(false);
            jMenuItemToGridSim.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemToGridSimActionPerformed(evt);
                }
            });
            jMenuExport.add(jMenuItemToGridSim);

            jMenuArquivo.add(jMenuExport);
            jMenuArquivo.add(jSeparator1);

            jMenuIdioma.setText(palavras.getString("Language")); // NOI18N

            jMenuItemIngles.setText(palavras.getString("English")); // NOI18N
            jMenuItemIngles.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemInglesActionPerformed(evt);
                }
            });
            jMenuIdioma.add(jMenuItemIngles);

            jMenuItemPortugues.setText(palavras.getString("Portuguese")); // NOI18N
            jMenuItemPortugues.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemPortuguesActionPerformed(evt);
                }
            });
            jMenuIdioma.add(jMenuItemPortugues);

            jMenuArquivo.add(jMenuIdioma);
            jMenuArquivo.add(jSeparator2);

            jMenuItemFechar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.CTRL_MASK));
            jMenuItemFechar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-close.png"))); // NOI18N
            jMenuItemFechar.setText(palavras.getString("Close")); // NOI18N
            jMenuItemFechar.setToolTipText(palavras.getString("Closes the currently open model")); // NOI18N
            jMenuItemFechar.setEnabled(false);
            jMenuItemFechar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemFecharActionPerformed(evt);
                }
            });
            jMenuArquivo.add(jMenuItemFechar);

            jMenuItemSair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
            jMenuItemSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/window-close.png"))); // NOI18N
            jMenuItemSair.setText(palavras.getString("Exit")); // NOI18N
            jMenuItemSair.setToolTipText(palavras.getString("Closes the program")); // NOI18N
            jMenuItemSair.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemSairActionPerformed(evt);
                }
            });
            jMenuArquivo.add(jMenuItemSair);

            jMenuBar.add(jMenuArquivo);

            jMenuEditar.setText(palavras.getString("Edit")); // NOI18N

            jMenuItemCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
            jMenuItemCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-copy.png"))); // NOI18N
            jMenuItemCopy.setText(palavras.getString("Copy")); // NOI18N
            jMenuItemCopy.setEnabled(false);
            jMenuItemCopy.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemCopyActionPerformed(evt);
                }
            });
            jMenuEditar.add(jMenuItemCopy);

            jMenuItemPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
            jMenuItemPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-paste.png"))); // NOI18N
            jMenuItemPaste.setText(palavras.getString("Paste")); // NOI18N
            jMenuItemPaste.setEnabled(false);
            jMenuItemPaste.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemPasteActionPerformed(evt);
                }
            });
            jMenuEditar.add(jMenuItemPaste);

            jMenuItemDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/edit-delete.png"))); // NOI18N
            jMenuItemDelete.setText(palavras.getString("Delete")); // NOI18N
            jMenuItemDelete.setEnabled(false);
            jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemDeleteActionPerformed(evt);
                }
            });
            jMenuEditar.add(jMenuItemDelete);

            jMenuItemEquiparar.setText(palavras.getString("Match network settings")); // NOI18N
            jMenuItemEquiparar.setToolTipText(palavras.getString("Matches the settings of icons of networks according to a selected icon")); // NOI18N
            jMenuItemEquiparar.setActionCommand(palavras.getString("Match network settings")); // NOI18N
            jMenuItemEquiparar.setEnabled(false);
            jMenuItemEquiparar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemEquipararActionPerformed(evt);
                }
            });
            jMenuEditar.add(jMenuItemEquiparar);

            jMenuItemOrganizar.setText("Arrange icons");
            jMenuItemOrganizar.setEnabled(false);
            jMenuItemOrganizar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemOrganizarActionPerformed(evt);
                }
            });
            jMenuEditar.add(jMenuItemOrganizar);
            jMenuEditar.add(jSeparator5);

            jMenuItem1.setText("Preferences");
            jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItem1ActionPerformed(evt);
                }
            });
            jMenuEditar.add(jMenuItem1);

            jMenuBar.add(jMenuEditar);

            jMenuExibir.setText(palavras.getString("View")); // NOI18N

            jCheckBoxMenuItemConectado.setText(palavras.getString("Show Connected Nodes")); // NOI18N
            jCheckBoxMenuItemConectado.setToolTipText(palavras.getString("Displays in the settings area, the list of nodes connected for the selected icon")); // NOI18N
            jCheckBoxMenuItemConectado.setEnabled(false);
            jCheckBoxMenuItemConectado.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBoxMenuItemConectadoActionPerformed(evt);
                }
            });
            jMenuExibir.add(jCheckBoxMenuItemConectado);

            jCheckBoxMenuItemIndireto.setText(palavras.getString("Show Indirectly Connected Nodes")); // NOI18N
            jCheckBoxMenuItemIndireto.setToolTipText(palavras.getString("Displays in the settings area, the list of nodes connected through the internet icon, to the icon selected")); // NOI18N
            jCheckBoxMenuItemIndireto.setEnabled(false);
            jCheckBoxMenuItemIndireto.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBoxMenuItemIndiretoActionPerformed(evt);
                }
            });
            jMenuExibir.add(jCheckBoxMenuItemIndireto);

            jCheckBoxMenuItemEscalonavel.setSelected(true);
            jCheckBoxMenuItemEscalonavel.setText(palavras.getString("Show Schedulable Nodes")); // NOI18N
            jCheckBoxMenuItemEscalonavel.setToolTipText(palavras.getString("Displays in the settings area, the list of nodes schedulable for the selected icon")); // NOI18N
            jCheckBoxMenuItemEscalonavel.setEnabled(false);
            jCheckBoxMenuItemEscalonavel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBoxMenuItemEscalonavelActionPerformed(evt);
                }
            });
            jMenuExibir.add(jCheckBoxMenuItemEscalonavel);

            jCheckBoxMenuItemGrade.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
            jCheckBoxMenuItemGrade.setSelected(true);
            jCheckBoxMenuItemGrade.setText(palavras.getString("Drawing grid")); // NOI18N
            jCheckBoxMenuItemGrade.setToolTipText(palavras.getString("Displays grid in the drawing area")); // NOI18N
            jCheckBoxMenuItemGrade.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBoxMenuItemGradeActionPerformed(evt);
                }
            });
            jMenuExibir.add(jCheckBoxMenuItemGrade);

            jCheckBoxMenuItemRegua.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
            jCheckBoxMenuItemRegua.setSelected(true);
            jCheckBoxMenuItemRegua.setText(palavras.getString("Drawing rule")); // NOI18N
            jCheckBoxMenuItemRegua.setToolTipText(palavras.getString("Displays rule in the drawing area")); // NOI18N
            jCheckBoxMenuItemRegua.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBoxMenuItemReguaActionPerformed(evt);
                }
            });
            jMenuExibir.add(jCheckBoxMenuItemRegua);

            jMenuBar.add(jMenuExibir);

            jMenuFerramentas.setText(palavras.getString("Tools")); // NOI18N
            jMenuFerramentas.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuFerramentasActionPerformed(evt);
                }
            });

            jMenuItemGerenciar.setText(palavras.getString("Manage Schedulers")); // NOI18N
            jMenuItemGerenciar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemGerenciarActionPerformed(evt);
                }
            });
            jMenuFerramentas.add(jMenuItemGerenciar);

            jMenuItemGerar.setText(palavras.getString("Generate Scheduler")); // NOI18N
            jMenuItemGerar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemGerarActionPerformed(evt);
                }
            });
            jMenuFerramentas.add(jMenuItemGerar);

            jMenuItemGerenciarCloud.setText("Manage Cloud Schedulers");
            jMenuItemGerenciarCloud.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemGerenciarCloudActionPerformed(evt);
                }
            });
            jMenuFerramentas.add(jMenuItemGerenciarCloud);

            jMenuItemGerenciarAllocation.setText("Manage Allocation Policies");
            jMenuItemGerenciarAllocation.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemGerenciarAllocationActionPerformed(evt);
                }
            });
            jMenuFerramentas.add(jMenuItemGerenciarAllocation);

            jMenuBar.add(jMenuFerramentas);

            jMenuAjuda.setText(palavras.getString("Help")); // NOI18N

            jMenuItemAjuda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/help-faq.png"))); // NOI18N
            jMenuItemAjuda.setText(palavras.getString("Help")); // NOI18N
            jMenuItemAjuda.setToolTipText(palavras.getString("Help")); // NOI18N
            jMenuItemAjuda.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemAjudaActionPerformed(evt);
                }
            });
            jMenuAjuda.add(jMenuItemAjuda);
            jMenuAjuda.add(jSeparator3);

            jMenuItemSobre.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/help-about.png"))); // NOI18N
            jMenuItemSobre.setText(palavras.getString("About") + " " + palavras.getString("nomePrograma"));
            jMenuItemSobre.setToolTipText(palavras.getString("About") + " " + palavras.getString("nomePrograma"));
            jMenuItemSobre.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jMenuItemSobreActionPerformed(evt);
                }
            });
            jMenuAjuda.add(jMenuItemSobre);

            jMenuBar.add(jMenuAjuda);

            setJMenuBar(jMenuBar);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPaneProperties)
                        .addComponent(jScrollPaneBarraLateral, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPaneBarraNotifica)
                        .addComponent(jToolBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                        .addComponent(jScrollPaneAreaDesenho))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jScrollPaneBarraLateral, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPaneProperties, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPaneAreaDesenho)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPaneBarraNotifica, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
            );

            pack();
        }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemGerenciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGerenciarActionPerformed
        // TODO add your handling code here:
        jFrameGerenciador.setLocationRelativeTo(this);
        jFrameGerenciador.setVisible(true);
    }//GEN-LAST:event_jMenuItemGerenciarActionPerformed

    private void jMenuItemSobreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSobreActionPerformed
        jSobre.setVisible(true);
    }//GEN-LAST:event_jMenuItemSobreActionPerformed

    private void jMenuItemInglesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInglesActionPerformed
        // TODO add your handling code here:
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", new Locale("en", "US"));
        initTexts();
        if (aDesenho != null) {
            aDesenho.setIdioma(palavras);
        }
    }//GEN-LAST:event_jMenuItemInglesActionPerformed

    private void jMenuItemPortuguesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPortuguesActionPerformed
        // TODO add your handling code here:
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", new Locale("pt", "BR"));
        initTexts();
        if (aDesenho != null) {
            aDesenho.setIdioma(palavras);
        }
    }//GEN-LAST:event_jMenuItemPortuguesActionPerformed

    private void jToggleButtonMaquinaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonMaquinaActionPerformed
        // TODO add your handling code here:
        //Desativa outros botões
        jToggleButtonRede.setSelected(false);
        jToggleButtonCluster.setSelected(false);
        jToggleButtonInternet.setSelected(false);

        //Realiza ação
        if (jToggleButtonMaquina.isSelected()) {
            aDesenho.setIconeSelecionado(DesenhoGrade.MACHINE);
            appendNotificacao(palavras.getString("Machine button selected."));
        } else {
            aDesenho.setIconeSelecionado(null);
        }
    }//GEN-LAST:event_jToggleButtonMaquinaActionPerformed

    private void jToggleButtonRedeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonRedeActionPerformed
        // TODO add your handling code here:
        //Desativa outros botões
        jToggleButtonMaquina.setSelected(false);
        jToggleButtonCluster.setSelected(false);
        jToggleButtonInternet.setSelected(false);

        if (jToggleButtonRede.isSelected()) {
            aDesenho.setIconeSelecionado(DesenhoGrade.NETWORK);
            appendNotificacao(palavras.getString("Network button selected."));
        } else {
            aDesenho.setIconeSelecionado(null);
        }
    }//GEN-LAST:event_jToggleButtonRedeActionPerformed

    private void jToggleButtonClusterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonClusterActionPerformed
        // TODO add your handling code here:
        //Desativa outros botões
        jToggleButtonMaquina.setSelected(false);
        jToggleButtonRede.setSelected(false);
        jToggleButtonInternet.setSelected(false);

        if (jToggleButtonCluster.isSelected()) {
            aDesenho.setIconeSelecionado(DesenhoGrade.CLUSTER);
            appendNotificacao(palavras.getString("Cluster button selected."));
        } else {
            aDesenho.setIconeSelecionado(null);
        }
    }//GEN-LAST:event_jToggleButtonClusterActionPerformed

    private void jToggleButtonInternetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonInternetActionPerformed
        // TODO add your handling code here:
        //Desativa outros botões
        jToggleButtonMaquina.setSelected(false);
        jToggleButtonRede.setSelected(false);
        jToggleButtonCluster.setSelected(false);

        if (jToggleButtonInternet.isSelected()) {
            aDesenho.setIconeSelecionado(DesenhoGrade.INTERNET);
            appendNotificacao(palavras.getString("Internet button selected."));
        } else {
            aDesenho.setIconeSelecionado(null);
        }
    }//GEN-LAST:event_jToggleButtonInternetActionPerformed

    private void jButtonTarefasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTarefasActionPerformed
        // TODO add your handling code here:
        if (aDesenho != null) {
            SelecionaCargas carga = new SelecionaCargas(this, true,
                    aDesenho.getUsuarios().toArray(),
                    aDesenho.getNosEscalonadores().toArray(),
                    aDesenho.getCargasConfiguracao(),
                    palavras);
            carga.setLocationRelativeTo(this);
            carga.setVisible(true);
            aDesenho.setCargasConfiguracao(carga.getCargasConfiguracao());
            aDesenho.setUsuarios(carga.getUsuarios());
            modificar();
        }
    }//GEN-LAST:event_jButtonTarefasActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        JPreferences jp = new JPreferences(this, true, configura);
        jp.setLocationRelativeTo(this);
        jp.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButtonSimularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSimularActionPerformed
        // TODO add your handling code here:
        JSimulacao janelaSimulacao = new JSimulacao(this, true, aDesenho.getGrade(), aDesenho.toString(), palavras, tipoModelo);
        janelaSimulacao.iniciarSimulacao();
        janelaSimulacao.setLocationRelativeTo(this);
        janelaSimulacao.setVisible(true);
        appendNotificacao(palavras.getString("Simulate button added."));
    }//GEN-LAST:event_jButtonSimularActionPerformed

    private void jMenuItemNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNovoActionPerformed
        int escolha = JOptionPane.YES_OPTION;
        if (modificado) {
            escolha = savarAlteracao();
        }
        ChooseClass = new EscolherClasse(this, true);
        ChooseClass.setLocationRelativeTo(this);
        ChooseClass.setVisible(true);
        aDesenho = new DesenhoGrade(1500, 1500);
        aDesenho.addKeyListener(this);
        aDesenho.setPaineis(this);
        //this.setRegua();
        jScrollPaneBarraLateral.setViewportView(null);
        jPanelPropriedades.setjLabelTexto("");
        jScrollPaneAreaDesenho.setViewportView(aDesenho);
        appendNotificacao(palavras.getString("New model opened"));
        abrirEdição(null);
        //novo modelo não salvo ainda
        modificar();
        this.tipoModelo = ChooseClass.getEscolha();
        aDesenho.setTipoModelo(tipoModelo);
        switch (tipoModelo) {

            case EscolherClasse.GRID:
                jButtonConfigVM.setVisible(false);
                break;
            case EscolherClasse.IAAS:
                jButtonConfigVM.setVisible(true);
                break;
            case EscolherClasse.PAAS:
                jButtonConfigVM.setVisible(false);
                break;

        }
    }//GEN-LAST:event_jMenuItemNovoActionPerformed

    private void jMenuItemAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAbrirActionPerformed
        // TODO add your handling code here:
        int escolha = JOptionPane.YES_OPTION;
        if (modificado) {
            escolha = savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            filtro.setDescricao(palavras.getString("Iconic Model of Simulation"));
            String[] exts = {".ims", ".imsx"};
            filtro.setExtensao(exts);
            jFileChooser.setAcceptAllFileFilterUsed(false);
            int returnVal = jFileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                //This is where a real application would open the file.
                //Abrir arquivo.
                if (file.getName().endsWith(".ims") || file.getName().endsWith(".imsx")) {
                    try {
                        if (file.getName().endsWith(".imsx")) {
                            //Realizar leitura do arquivoo xml...
                            Document descricao = ispd.arquivo.xml.IconicoXML.ler(file);
                            //Carregar na aDesenho
                            aDesenho = new DesenhoGrade(1500, 1500);
                            aDesenho.setGrade(descricao);
                            this.tipoModelo = aDesenho.getTipoModelo();
                            this.maquinasVirtuais = aDesenho.getMaquinasVirtuais();
                            switch (tipoModelo) {
                                case EscolherClasse.GRID:
                                    jButtonConfigVM.setVisible(false);
                                    break;
                                case EscolherClasse.IAAS:
                                    jButtonConfigVM.setVisible(true);
                                    break;
                                case EscolherClasse.PAAS:
                                    jButtonConfigVM.setVisible(false);
                                    break;
                            }

                        } else {
                            //Realiza leitura do arquivo da outra versão
                            FileInputStream arquivo = new FileInputStream(file);
                            ObjectInputStream objectInput = new ObjectInputStream(arquivo);
                            DescreveSistema descricao = (DescreveSistema) objectInput.readObject();
                            objectInput.close();
                            file = null;
                            //Carregar na aDesenho
                            aDesenho = new DesenhoGrade(1500, 1500);
                            aDesenho.setGrade(descricao);
                        }
                        aDesenho.addKeyListener(this);
                        aDesenho.setPaineis(this);
                        //this.setRegua();
                        jScrollPaneBarraLateral.setViewportView(null);
                        jPanelPropriedades.setjLabelTexto("");
                        jScrollPaneAreaDesenho.setViewportView(aDesenho);
                        appendNotificacao(palavras.getString("model opened"));
                        abrirEdição(file);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(null, palavras.getString("Error opening file.") + "\n" + ex.getMessage(), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                    } catch (ParserConfigurationException ex) {
                        Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(null, palavras.getString("Error opening file.") + "\n" + ex.getMessage(), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                    } catch (IOException ex) {
                        Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(null, palavras.getString("Error opening file.") + "\n" + ex.getMessage(), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                    } catch (SAXException ex) {
                        Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(null, palavras.getString("Error opening file.") + "\n" + ex.getMessage(), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                    } catch (Exception ex) {
                        Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(null, palavras.getString("Error opening file.") + "\n" + ex.getMessage(), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                    }
                } else {
                    if ("Torre".equals(jFileChooser.getSelectedFile().getName())) {
                        jScrollPaneAreaDesenho.setViewportView(new ispd.gui.auxiliar.Stalemate());
                    } else {
                        JOptionPane.showMessageDialog(null, palavras.getString("Invalid file"), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                    }
                }
            } else {
                //Cancelado
            }
        }
    }//GEN-LAST:event_jMenuItemAbrirActionPerformed

    private void jMenuItemSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSalvarActionPerformed
        // TODO add your handling code here:
        if (arquivoAberto == null) {
            jMenuItemSalvarComoActionPerformed(null);
        } else if (aDesenho != null /*&& modificado*/) {
            //Implementar ações para salvar conteudo
            Document docxml = aDesenho.getGrade();
            ispd.arquivo.xml.IconicoXML.escrever(docxml, arquivoAberto);
            appendNotificacao(palavras.getString("model saved"));
            salvarModificacao();
        }
    }//GEN-LAST:event_jMenuItemSalvarActionPerformed

    private void jMenuItemSimGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSimGridActionPerformed
        // TODO add your handling code here:
        filtro.setDescricao(palavras.getString("XML File"));
        filtro.setExtensao(".xml");
        jFileChooser.setAcceptAllFileFilterUsed(true);
        JOptionPane.showMessageDialog(null, palavras.getString("Select the application file."), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
        int returnVal = jFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file1 = jFileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(null, palavras.getString("Select the platform file."), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
            returnVal = jFileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file2 = jFileChooser.getSelectedFile();
                InterpretadorSimGrid interp = new InterpretadorSimGrid();
                interp.interpreta(file1, file2);
                try {
                    if (interp.getModelo() != null) {
                        aDesenho = new DesenhoGrade(1500, 1500);
                        aDesenho.setGrade(interp.getModelo());
                        aDesenho.iconArrange();
                        aDesenho.addKeyListener(this);
                        aDesenho.setPaineis(this);
                        //this.setRegua();
                        jScrollPaneBarraLateral.setViewportView(null);
                        jPanelPropriedades.setjLabelTexto("");
                        jScrollPaneAreaDesenho.setViewportView(aDesenho);
                        appendNotificacao(palavras.getString("model opened"));
                        abrirEdição(null);
                        //modelo não salvo ainda
                        modificar();
                    } else {
                        JOptionPane.showMessageDialog(null, palavras.getString("File not found.") + "\n", palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, palavras.getString("Error opening file.") + "\n" + e.getMessage(), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_jMenuItemSimGridActionPerformed

    private void jMenuItemToJPGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemToJPGActionPerformed
        // TODO add your handling code here:
        filtro.setDescricao(palavras.getString("JPG Image (.jpg)"));
        filtro.setExtensao(".jpg");
        jFileChooser.setAcceptAllFileFilterUsed(false);
        int returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            if (!file.getName().endsWith(".jpg")) {
                File temp = new File(file.toString() + ".jpg");
                file = temp;
            }
            BufferedImage img = aDesenho.createImage();
            try {
                ImageIO.write(img, "jpg", file);
            } catch (IOException ex) {
                Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItemToJPGActionPerformed

    private void jMenuItemFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFecharActionPerformed
        // TODO add your handling code here:
        int escolha = JOptionPane.YES_OPTION;
        if (modificado) {
            escolha = savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            jScrollPaneAreaDesenho.setViewportView(null);
            jScrollPaneBarraLateral.setViewportView(null);
            jPanelPropriedades.setjLabelTexto("");
            appendNotificacao(palavras.getString("model closed"));
            fecharEdicao();
        }
    }//GEN-LAST:event_jMenuItemFecharActionPerformed

    private void jMenuItemSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSairActionPerformed
        formWindowClosing(null);
    }//GEN-LAST:event_jMenuItemSairActionPerformed

    private void jMenuItemPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPasteActionPerformed
        // TODO add your handling code here:
        if (aDesenho != null) {
            aDesenho.botaoPainelActionPerformed(evt);
        }
}//GEN-LAST:event_jMenuItemPasteActionPerformed

    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteActionPerformed
        // TODO add your handling code here:
        if (aDesenho != null) {
            aDesenho.botaoIconeActionPerformed(evt);
        }
}//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void jMenuItemCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyActionPerformed
        // TODO add your handling code here:
        if (aDesenho != null) {
            aDesenho.botaoVerticeActionPerformed(evt);
        }
}//GEN-LAST:event_jMenuItemCopyActionPerformed

    private void jMenuItemEquipararActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemEquipararActionPerformed
        // TODO add your handling code here:
        if (aDesenho != null) {
            aDesenho.matchNetwork();
        }
}//GEN-LAST:event_jMenuItemEquipararActionPerformed

    private void jCheckBoxMenuItemConectadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemConectadoActionPerformed
        // TODO add your handling code here:
        if (!jCheckBoxMenuItemConectado.isSelected()) {
            jCheckBoxMenuItemConectado.setSelected(false);
            aDesenho.setConectados(false);
            appendNotificacao(palavras.getString("Connected Nodes hidden."));
        } else {
            jCheckBoxMenuItemConectado.setSelected(true);
            aDesenho.setConectados(true);
            appendNotificacao(palavras.getString("Connected Nodes unhidden."));
        }
    }//GEN-LAST:event_jCheckBoxMenuItemConectadoActionPerformed

    private void jCheckBoxMenuItemIndiretoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemIndiretoActionPerformed
        // TODO add your handling code here:
        if (!jCheckBoxMenuItemIndireto.isSelected()) {
            jCheckBoxMenuItemIndireto.setSelected(false);
            aDesenho.setIndiretos(false);
            appendNotificacao(palavras.getString("Indirectly Connected Nodes are now not being shown"));
        } else {
            jCheckBoxMenuItemIndireto.setSelected(true);
            aDesenho.setIndiretos(true);
            appendNotificacao(palavras.getString("Indirectly Connected Nodes are now being shown"));
        }
    }//GEN-LAST:event_jCheckBoxMenuItemIndiretoActionPerformed

    private void jCheckBoxMenuItemEscalonavelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemEscalonavelActionPerformed
        // TODO add your handling code here:
        if (!jCheckBoxMenuItemEscalonavel.isSelected()) {
            jCheckBoxMenuItemEscalonavel.setSelected(false);
            aDesenho.setEscalonaveis(false);
            appendNotificacao(palavras.getString("Schedulable Nodes hidden."));
        } else {
            jCheckBoxMenuItemEscalonavel.setSelected(true);
            aDesenho.setEscalonaveis(true);
            appendNotificacao(palavras.getString("Schedulable Nodes unhidden."));
        }
    }//GEN-LAST:event_jCheckBoxMenuItemEscalonavelActionPerformed

    private void jCheckBoxMenuItemGradeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemGradeActionPerformed
        // TODO add your handling code here:
        if (!jCheckBoxMenuItemGrade.isSelected()) {
            jCheckBoxMenuItemGrade.setSelected(false);
            if (aDesenho != null) {
                aDesenho.setGridOn(false);
            }
            appendNotificacao(palavras.getString("Drawing grid disabled."));
        } else {
            jCheckBoxMenuItemGrade.setSelected(true);
            if (aDesenho != null) {
                aDesenho.setGridOn(true);
            }
            appendNotificacao(palavras.getString("Drawing grid enabled."));
        }
    }//GEN-LAST:event_jCheckBoxMenuItemGradeActionPerformed

    private void jCheckBoxMenuItemReguaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemReguaActionPerformed
        // TODO add your handling code here:
        if (!jCheckBoxMenuItemRegua.isSelected()) {
            jCheckBoxMenuItemRegua.setSelected(false);
            if (evt != null) {
                appendNotificacao(palavras.getString("Drawing rule disabled."));
            }
            jScrollPaneAreaDesenho.setColumnHeaderView(null);
            jScrollPaneAreaDesenho.setRowHeaderView(null);
            //Set the corners.
            jScrollPaneAreaDesenho.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);
            jScrollPaneAreaDesenho.setCorner(JScrollPane.LOWER_LEFT_CORNER, null);
            jScrollPaneAreaDesenho.setCorner(JScrollPane.UPPER_RIGHT_CORNER, null);
        } else {
            jCheckBoxMenuItemRegua.setSelected(true);
            if (evt != null) {
                appendNotificacao(palavras.getString("Drawing rule enabled."));
            }
            jScrollPaneAreaDesenho.setColumnHeaderView(aDesenho.getColumnView());
            jScrollPaneAreaDesenho.setRowHeaderView(aDesenho.getRowView());

            jScrollPaneAreaDesenho.setCorner(JScrollPane.UPPER_LEFT_CORNER, aDesenho.getCorner());
            jScrollPaneAreaDesenho.setCorner(JScrollPane.LOWER_LEFT_CORNER, new Corner());
            jScrollPaneAreaDesenho.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new Corner());
        }
    }//GEN-LAST:event_jCheckBoxMenuItemReguaActionPerformed

    private void jMenuItemGerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGerarActionPerformed
        // TODO add your handling code here:
        if (tipoModelo == EscolherClasse.GRID) {
            GerarEscalonador ge = new GerarEscalonador(this, true, jFrameGerenciador.getEscalonadores().getDiretorio().getAbsolutePath(), palavras);
            ge.setEscalonadores(jFrameGerenciador.getEscalonadores());
            ge.setLocationRelativeTo(this);
            ge.setVisible(true);
            if (ge.getParse() != null) {
                jFrameGerenciador.atualizarEscalonadores();
            }
        }
        else if (tipoModelo == EscolherClasse.IAAS){
            GerarEscalonador ge = new GerarEscalonador(this, true, jFrameGerenciadorCloud.getEscalonadores().getDiretorio().getAbsolutePath(), palavras);
            ge.setEscalonadoresCloud(jFrameGerenciadorCloud.getEscalonadores());
            ge.setLocationRelativeTo(this);
            ge.setVisible(true);
            if (ge.getParse() != null) {
                jFrameGerenciadorCloud.atualizarEscalonadores();
            }
            
            GerarEscalonador ga = new GerarEscalonador(this, true, jFrameGerenciadorAlloc.getAlocadores().getDiretorio().getAbsolutePath(), palavras);
            ga.setAlocadores(jFrameGerenciadorAlloc.getAlocadores());
            ga.setLocationRelativeTo(this);
            ga.setVisible(true);
            if (ga.getParse() != null) {
                jFrameGerenciadorAlloc.atualizarAlocadores();
            }
            
        }
     
    }//GEN-LAST:event_jMenuItemGerarActionPerformed

    private void jMenuItemAjudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAjudaActionPerformed
        // TODO add your handling code here:
        TreeHelp help = new TreeHelp();
        help.setLocationRelativeTo(this);
        help.setVisible(true);
    }//GEN-LAST:event_jMenuItemAjudaActionPerformed

    private void jMenuItemToTXTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemToTXTActionPerformed
        // TODO add your handling code here:
        filtro.setDescricao(palavras.getString("Plane Text"));
        filtro.setExtensao(".txt");
        jFileChooser.setAcceptAllFileFilterUsed(false);
        int returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                File temp = new File(file.toString() + ".txt");
                file = temp;
            }
            FileWriter writer;
            try {
                writer = new FileWriter(file);
                PrintWriter saida = new PrintWriter(writer, true);
                saida.print(aDesenho.toString());
                saida.close();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(JPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItemToTXTActionPerformed

    private void jMenuItemSalvarComoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSalvarComoActionPerformed
        // TODO add your handling code here:
        if (aDesenho != null) {
            //Escolher o arquivo
            filtro.setDescricao(palavras.getString("Iconic Model of Simulation"));
            filtro.setExtensao(".imsx");
            jFileChooser.setAcceptAllFileFilterUsed(false);
            int returnVal = jFileChooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                if (!file.getName().endsWith(".imsx")) {
                    File temp = new File(file.toString() + ".imsx");
                    file = temp;
                }
                //Implementar ações para salvar conteudo
                Document docxml = aDesenho.getGrade();
                ispd.arquivo.xml.IconicoXML.escrever(docxml, file);
                appendNotificacao(palavras.getString("model saved"));
                abrirEdição(file);
            }
        }
    }//GEN-LAST:event_jMenuItemSalvarComoActionPerformed

    private void jButtonUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUsuariosActionPerformed
        // TODO add your handling code here:
        if (aDesenho != null) {
            JUsuarios jusers = new JUsuarios(this, true, aDesenho.getUsuarios(), palavras, aDesenho.getPerfil());
            jusers.setLocationRelativeTo(this);
            jusers.setVisible(true);
            aDesenho.setUsuarios(jusers.getUsuarios());
            aDesenho.setPerfil(jusers.getLimite());
            modificar();
        }
    }//GEN-LAST:event_jButtonUsuariosActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        configura.setLastFile(arquivoAberto);
        configura.save();
        if (modificado) {
            int escolha = savarAlteracao();
            if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing

    private void jMenuArquivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuArquivoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuArquivoActionPerformed

    private void jMenuItemGridSimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGridSimActionPerformed
        // TODO add your handling code here:
        filtro.setDescricao(palavras.getString("Java Source Files (. java)"));
        filtro.setExtensao(".java");
        jFileChooser.setAcceptAllFileFilterUsed(true);
        int returnVal = jFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            /*JWindow window = new JWindow(this);
             //window.add(new JLabel(new ImageIcon(getClass().getResource("imagens/simbolo_t.gif"))));
             window.add(new JLabel("Carregando..."));
             window.setSize(200, 100);
             JPanel panel = new JPanel();
             panel.setBorder(new javax.swing.border.EtchedBorder());
             window.getContentPane().add(panel, BorderLayout.CENTER);
             JLabel label = new JLabel("Carregando...");
             label.setFont(new Font("Verdana", Font.BOLD, 15));
             panel.add(label);
             JProgressBar progressBar = new JProgressBar();
             progressBar.setIndeterminate(true);
             window.getContentPane().add(progressBar, BorderLayout.SOUTH);
             window.setLocationRelativeTo(this);
             window.setVisible(true);*/
            final JDialog window = new JDialog(this, "Carregando");
            Thread th = new Thread() {
                @Override
                public void run() {
                    window.setSize(200, 100);
                    window.add(new JLabel("Carregando..."), BorderLayout.CENTER);
                    JProgressBar progressBar = new JProgressBar();
                    progressBar.setIndeterminate(true);
                    window.add(progressBar, BorderLayout.SOUTH);
                    window.setVisible(true);
                }
            };
            window.setLocationRelativeTo(this);
            th.start();
            try {
                File file = jFileChooser.getSelectedFile();
                InterpretadorGridSim interp = new InterpretadorGridSim();
                if (file.exists()) {
                    interp.interpreta(file);
                    Document descricao = interp.getDescricao();
                    if (interp.getW() > 1500) {
                        aDesenho = new DesenhoGrade(interp.getW(), interp.getW());
                    } else {
                        aDesenho = new DesenhoGrade(1500, 1500);
                    }
                    //Carregar na aDesenho
                    aDesenho.setGrade(descricao);
                    aDesenho.addKeyListener(this);
                    aDesenho.setPaineis(this);
                    //this.setRegua();
                    jScrollPaneBarraLateral.setViewportView(null);
                    jPanelPropriedades.setjLabelTexto("");
                    jScrollPaneAreaDesenho.setViewportView(aDesenho);
                    appendNotificacao(palavras.getString("model opened"));
                    abrirEdição(null);
                    //modelo não salvo ainda
                    modificar();
                } else {
                    JOptionPane.showMessageDialog(null, palavras.getString("File not found.") + "\n", palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, palavras.getString("Error opening file.") + "\n" + e.getMessage(), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
            }
            window.dispose();
        }
    }//GEN-LAST:event_jMenuItemGridSimActionPerformed

    private void jMenuItemOrganizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOrganizarActionPerformed
        // TODO add your handling code here:
        if (aDesenho != null) {
            if (jMenuItemOrganizar.getDisplayedMnemonicIndex() == 2) {
                jMenuItemOrganizar.setDisplayedMnemonicIndex(1);
                aDesenho.iconArrangeType();
            } else {
                jMenuItemOrganizar.setDisplayedMnemonicIndex(2);
                aDesenho.iconArrange();
            }
            aDesenho.repaint();
        }
    }//GEN-LAST:event_jMenuItemOrganizarActionPerformed

    private void jMenuItemToSimGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemToSimGridActionPerformed
        filtro.setDescricao(palavras.getString("XML File"));
        filtro.setExtensao(".xml");
        jFileChooser.setAcceptAllFileFilterUsed(false);
        int returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            if (!file.getName().endsWith(".xml")) {
                File temp = new File(file.toString() + ".xml");
                file = temp;
            }
            try {
                Exportador convert = new Exportador(aDesenho.getGrade());
                convert.toSimGrid(file);
                JOptionPane.showMessageDialog(this, palavras.getString("model saved"), "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), palavras.getString("WARNING"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItemToSimGridActionPerformed

    private void jMenuItemAbrirResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAbrirResultActionPerformed
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File dir = jFileChooser.getSelectedFile();
            if (dir.isDirectory() && dir.exists()) {
                try {
                    HtmlPane.openDefaultBrowser(new URL("file://" + dir.getAbsolutePath() + "/result.html"));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, palavras.getString("Error opening file.") + "\n" + e.getMessage(), palavras.getString("WARNING"), JOptionPane.PLAIN_MESSAGE);
                }
            }
        }
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }//GEN-LAST:event_jMenuItemAbrirResultActionPerformed

    private void jMenuItemToGridSimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemToGridSimActionPerformed
        filtro.setDescricao(palavras.getString("Java Source Files (. java)"));
        filtro.setExtensao(".java");
        jFileChooser.setAcceptAllFileFilterUsed(false);
        int returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            if (!file.getName().endsWith(".java")) {
                File temp = new File(file.toString() + ".java");
                file = temp;
            }
            try {
                Exportador convert = new Exportador(aDesenho.getGrade());
                convert.toGridSim(file);
                JOptionPane.showMessageDialog(this, palavras.getString("model saved"), "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), palavras.getString("WARNING"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItemToGridSimActionPerformed

    private void jButtonConfigVMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfigVMActionPerformed
        if (aDesenho.getNosEscalonadores().isEmpty()) {
            JOptionPane.showMessageDialog( // Caixa de mensagem  
                    null, // Janela da aplicação (opcional, pode ser null)  
                    "One or more VMMs need to be configurated", // Mensagem  
                    "WARNING!", // Título da caixa de mensagem  
                    JOptionPane.PLAIN_MESSAGE // Ícone da caixa de mensagem  
            );
        } else {
            JanelaVM = new ConfigurarVMs(this, true,
                    aDesenho.getUsuarios().toArray(),
                    aDesenho.getNosEscalonadores().toArray(),
                    maquinasVirtuais);
            JanelaVM.setLocationRelativeTo(this);
            JanelaVM.setVisible(true);
            //depois que a janela fechou..
            maquinasVirtuais = JanelaVM.getMaqVirtuais();
            aDesenho.setUsuarios(JanelaVM.atualizaUsuarios());
            aDesenho.setMaquinasVirtuais(maquinasVirtuais);
            modificar();
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonConfigVMActionPerformed

    private void jMenuItemGerenciarCloudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGerenciarCloudActionPerformed
        jFrameGerenciadorCloud.setLocationRelativeTo(this);
        jFrameGerenciadorCloud.setVisible(true);

    }//GEN-LAST:event_jMenuItemGerenciarCloudActionPerformed

    private void jMenuItemGerenciarAllocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGerenciarAllocationActionPerformed
        jFrameGerenciadorAlloc.setLocationRelativeTo(this);
        jFrameGerenciadorAlloc.setVisible(true);
    }//GEN-LAST:event_jMenuItemGerenciarAllocationActionPerformed

    private void jMenuFerramentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFerramentasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuFerramentasActionPerformed

    private void jButtonInjetarFalhasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInjetarFalhasActionPerformed
        JSelecionarFalhas sf =  new JSelecionarFalhas();
        sf.setVisible(true);
        
        //this.dispose();
       
    }//GEN-LAST:event_jButtonInjetarFalhasActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConfigVM;
    private javax.swing.JButton jButtonInjetarFalhas;
    private javax.swing.JButton jButtonSimular;
    private javax.swing.JButton jButtonTarefas;
    private javax.swing.JButton jButtonUsuarios;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemConectado;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemEscalonavel;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemGrade;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemIndireto;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemRegua;
    private javax.swing.JFileChooser jFileChooser;
    private ispd.gui.GerenciarEscalonador jFrameGerenciador;
    private ispd.gui.GerenciarAlocadores jFrameGerenciadorAlloc;
    private ispd.gui.GerenciarEscalonadorCloud jFrameGerenciadorCloud;
    private javax.swing.JMenu jMenuAjuda;
    private javax.swing.JMenu jMenuArquivo;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuEditar;
    private javax.swing.JMenu jMenuExibir;
    private javax.swing.JMenu jMenuExport;
    private javax.swing.JMenu jMenuFerramentas;
    private javax.swing.JMenu jMenuIdioma;
    private javax.swing.JMenu jMenuImport;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemAbrir;
    private javax.swing.JMenuItem jMenuItemAbrirResult;
    private javax.swing.JMenuItem jMenuItemAjuda;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JMenuItem jMenuItemEquiparar;
    private javax.swing.JMenuItem jMenuItemFechar;
    private javax.swing.JMenuItem jMenuItemGerar;
    private javax.swing.JMenuItem jMenuItemGerenciar;
    private javax.swing.JMenuItem jMenuItemGerenciarAllocation;
    private javax.swing.JMenuItem jMenuItemGerenciarCloud;
    private javax.swing.JMenuItem jMenuItemGridSim;
    private javax.swing.JMenuItem jMenuItemIngles;
    private javax.swing.JMenuItem jMenuItemNovo;
    private javax.swing.JMenuItem jMenuItemOrganizar;
    private javax.swing.JMenuItem jMenuItemPaste;
    private javax.swing.JMenuItem jMenuItemPortugues;
    private javax.swing.JMenuItem jMenuItemSair;
    private javax.swing.JMenuItem jMenuItemSalvar;
    private javax.swing.JMenuItem jMenuItemSalvarComo;
    private javax.swing.JMenuItem jMenuItemSimGrid;
    private javax.swing.JMenuItem jMenuItemSobre;
    private javax.swing.JMenuItem jMenuItemToGridSim;
    private javax.swing.JMenuItem jMenuItemToJPG;
    private javax.swing.JMenuItem jMenuItemToSimGrid;
    private javax.swing.JMenuItem jMenuItemToTXT;
    private ispd.gui.configuracao.JPanelConfigIcon jPanelConfiguracao;
    private ispd.gui.configuracao.JPanelSimples jPanelPropriedades;
    private ispd.gui.configuracao.JPanelSimples jPanelSimples;
    private javax.swing.JScrollPane jScrollPaneAreaDesenho;
    private javax.swing.JScrollPane jScrollPaneBarraLateral;
    private javax.swing.JScrollPane jScrollPaneBarraNotifica;
    private javax.swing.JScrollPane jScrollPaneProperties;
    private javax.swing.JTextArea jTextAreaNotifica;
    private javax.swing.JToggleButton jToggleButtonCluster;
    private javax.swing.JToggleButton jToggleButtonInternet;
    private javax.swing.JToggleButton jToggleButtonMaquina;
    private javax.swing.JToggleButton jToggleButtonRede;
    private javax.swing.JToolBar jToolBar;
    // End of variables declaration//GEN-END:variables
    private ResourceBundle palavras;
    private boolean modificado = false;//indica se arquivo atual foi modificado
    private File arquivoAberto = null;
    private DesenhoGrade aDesenho = null;
    private HashSet<VirtualMachine> maquinasVirtuais;
    private FiltroDeArquivos filtro;
    private JSobre jSobre;
    private ConfiguracaoISPD configura;

    public JPanelConfigIcon getjPanelConfiguracao() {
        return jPanelConfiguracao;
    }

    private void initTexts() {
        jScrollPaneBarraLateral.setBorder(javax.swing.BorderFactory.createTitledBorder(palavras.getString("Settings")));
        jScrollPaneProperties.setBorder(javax.swing.BorderFactory.createTitledBorder(palavras.getString("Properties")));
        jScrollPaneBarraNotifica.setBorder(javax.swing.BorderFactory.createTitledBorder(palavras.getString("Notifications")));

        jToggleButtonMaquina.setToolTipText(palavras.getString("Selects machine icon for add to the model"));
        jToggleButtonRede.setToolTipText(palavras.getString("Selects network icon for add to the model"));
        jToggleButtonCluster.setToolTipText(palavras.getString("Selects cluster icon for add to the model"));
        jToggleButtonInternet.setToolTipText(palavras.getString("Selects internet icon for add to the model"));
        jButtonTarefas.setToolTipText(palavras.getString("Selects insertion model of tasks")); // NOI18N
        jButtonUsuarios.setToolTipText(palavras.getString("Add and remove users to the model"));
        jButtonSimular.setText(palavras.getString("Simulate")); // NOI18N
        jButtonSimular.setToolTipText(palavras.getString("Starts the simulation"));
        //by Camila - injetar falhas
        jButtonInjetarFalhas.setText(palavras.getString("Simulate")); // NOI18N
        jButtonInjetarFalhas.setToolTipText(palavras.getString("Select the faults"));
        //Finished Camila

        jMenuArquivo.setText(palavras.getString("File")); // NOI18N
        jMenuItemNovo.setText(palavras.getString("New")); // NOI18N
        jMenuItemNovo.setToolTipText(palavras.getString("Starts a new model")); // NOI18N
        jMenuItemAbrir.setText(palavras.getString("Open")); // NOI18N
        jMenuItemAbrir.setToolTipText(palavras.getString("Opens an existing model")); // NOI18N
        jMenuItemSalvar.setText(palavras.getString("Save")); // NOI18N
        jMenuItemSalvar.setToolTipText(palavras.getString("Save the open model")); // NOI18N
        jMenuImport.setText(palavras.getString("Import")); // NOI18N
        jMenuItemSimGrid.setText(palavras.getString("SimGrid model")); // NOI18N
        jMenuItemSimGrid.setToolTipText(palavras.getString("Open model from the specification files of Simgrid")); // NOI18N
        jMenuExport.setText(palavras.getString("Export")); // NOI18N
        jMenuItemToJPG.setText(palavras.getString("to JPG")); // NOI18N
        jMenuItemToJPG.setToolTipText(palavras.getString("Creates a jpg file with the model image")); // NOI18N
        jMenuItemToTXT.setText(palavras.getString("to TXT")); // NOI18N
        jMenuItemToTXT.setToolTipText(palavras.getString("Creates a file in plain text with the model data according to the grammar of the iconic model")); // NOI18N
        jMenuIdioma.setText(palavras.getString("Language")); // NOI18N
        jMenuItemIngles.setText(palavras.getString("English")); // NOI18N
        jMenuItemPortugues.setText(palavras.getString("Portuguese")); // NOI18N
        jMenuItemFechar.setText(palavras.getString("Close")); // NOI18N
        jMenuItemFechar.setToolTipText(palavras.getString("Closes the currently open model")); // NOI18N
        jMenuItemSair.setText(palavras.getString("Exit")); // NOI18N
        jMenuItemSair.setToolTipText(palavras.getString("Closes the program")); // NOI18N

        jMenuEditar.setText(palavras.getString("Edit")); // NOI18N
        jMenuItemCopy.setText(palavras.getString("Copy")); // NOI18N
        jMenuItemPaste.setText(palavras.getString("Paste")); // NOI18N
        jMenuItemDelete.setText(palavras.getString("Delete")); // NOI18N
        jMenuItemEquiparar.setText(palavras.getString("Match network settings")); // NOI18N
        jMenuItemEquiparar.setToolTipText(palavras.getString("Matches the settings of icons of networks according to a selected icon")); // NOI18N

        jMenuExibir.setText(palavras.getString("View")); // NOI18N
        jCheckBoxMenuItemConectado.setText(palavras.getString("Show Connected Nodes")); // NOI18N
        jCheckBoxMenuItemConectado.setToolTipText(palavras.getString("Displays in the settings area, the list of nodes connected for the selected icon")); // NOI18N
        jCheckBoxMenuItemIndireto.setText(palavras.getString("Show Indirectly Connected Nodes")); // NOI18N
        jCheckBoxMenuItemIndireto.setToolTipText(palavras.getString("Displays in the settings area, the list of nodes connected through the internet icon, to the icon selected")); // NOI18N
        jCheckBoxMenuItemEscalonavel.setText(palavras.getString("Show Schedulable Nodes")); // NOI18N
        jCheckBoxMenuItemEscalonavel.setToolTipText(palavras.getString("Displays in the settings area, the list of nodes schedulable for the selected icon")); // NOI18N
        jCheckBoxMenuItemGrade.setText(palavras.getString("Drawing grid")); // NOI18N
        jCheckBoxMenuItemGrade.setToolTipText(palavras.getString("Displays grid in the drawing area")); // NOI18N
        jCheckBoxMenuItemRegua.setText(palavras.getString("Drawing rule")); // NOI18N
        jCheckBoxMenuItemRegua.setToolTipText(palavras.getString("Displays rule in the drawing area")); // NOI18N

        jMenuFerramentas.setText(palavras.getString("Tools")); // NOI18N
        jMenuItemGerenciar.setText(palavras.getString("Manage Schedulers")); // NOI18N
        jMenuItemGerar.setText(palavras.getString("Generate Scheduler")); // NOI18N

        jMenuAjuda.setText(palavras.getString("Help"));
        jMenuItemAjuda.setText(palavras.getString("Help"));
        jMenuItemAjuda.setToolTipText(palavras.getString("Help"));
        jMenuItemSobre.setText(palavras.getString("About") + " " + palavras.getString("nomePrograma"));
        jMenuItemSobre.setToolTipText(palavras.getString("About") + " " + palavras.getString("nomePrograma"));

        jPanelSimples.setjLabelTexto(palavras.getString("No icon selected."));
        jPanelConfiguracao.setPalavras(palavras);
    }

    public void appendNotificacao(String text) {
        jTextAreaNotifica.append(text + "\n");
    }

    /**
     * Indica que houve alterações no modelo aberto
     */
    public void modificar() {
        if (arquivoAberto == null) {
            this.setTitle("New_Model.ims [" + palavras.getString("modified") + "] - " + palavras.getString("nomePrograma"));
        } else {
            this.setTitle(arquivoAberto.getName() + " [" + palavras.getString("modified") + "] - " + palavras.getString("nomePrograma"));
        }
        this.modificado = true;
    }

    public void salvarModificacao() {
        if (arquivoAberto == null) {
            this.setTitle("New_Model.ims - " + palavras.getString("nomePrograma"));
        } else {
            this.setTitle(arquivoAberto.getName() + " - " + palavras.getString("nomePrograma"));
        }
        this.modificado = false;
    }

    private int savarAlteracao() {
        int escolha;
        if (arquivoAberto != null) {
            escolha = JOptionPane.showConfirmDialog(this, palavras.getString("Do you want to save changes to") + " " + arquivoAberto.getName());
        } else {
            escolha = JOptionPane.showConfirmDialog(this, palavras.getString("Do you want to save changes to") + " New_Model.ims");
        }
        if (escolha == JOptionPane.YES_OPTION) {
            //Implementar ações para salvar conteudo
            jMenuItemSalvarActionPerformed(null);
            salvarModificacao();
        }
        return escolha;
    }

    private void abrirEdição(File arquivo) {
        this.arquivoAberto = arquivo;
        //Realiza leitura das opções do menu Exibir
        aDesenho.setConectados(jCheckBoxMenuItemConectado.isSelected());
        aDesenho.setIndiretos(jCheckBoxMenuItemIndireto.isSelected());
        aDesenho.setEscalonaveis(jCheckBoxMenuItemEscalonavel.isSelected());
        aDesenho.setGridOn(jCheckBoxMenuItemGrade.isSelected());
        jCheckBoxMenuItemReguaActionPerformed(null);
        //Tirar seleção dos botões de icones
        jToggleButtonMaquina.setSelected(false);
        jToggleButtonCluster.setSelected(false);
        jToggleButtonInternet.setSelected(false);
        jToggleButtonRede.setSelected(false);
        setObjetosEnabled(true);
        salvarModificacao();
    }

    private void fecharEdicao() {
        this.setTitle(palavras.getString("nomePrograma"));
        this.arquivoAberto = null;
        setObjetosEnabled(false);
        this.modificado = false;
        //remove a regua
        jScrollPaneAreaDesenho.setColumnHeaderView(null);
        jScrollPaneAreaDesenho.setRowHeaderView(null);
        jScrollPaneAreaDesenho.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);
        jScrollPaneAreaDesenho.setCorner(JScrollPane.LOWER_LEFT_CORNER, null);
        jScrollPaneAreaDesenho.setCorner(JScrollPane.UPPER_RIGHT_CORNER, null);
    }

    //Habilitar Desabilita botões
    private void setObjetosEnabled(boolean opcao) {
        //Icones
        jToggleButtonCluster.setEnabled(opcao);
        jToggleButtonInternet.setEnabled(opcao);
        jToggleButtonMaquina.setEnabled(opcao);
        jToggleButtonRede.setEnabled(opcao);
        jButtonSimular.setEnabled(opcao);
        jButtonInjetarFalhas.setEnabled(opcao);
        jButtonTarefas.setEnabled(opcao);
        jButtonUsuarios.setEnabled(opcao);
        jButtonConfigVM.setEnabled(opcao);
        //Arquivo
        jMenuItemSalvar.setEnabled(opcao);
        jMenuItemSalvarComo.setEnabled(opcao);
        jMenuItemFechar.setEnabled(opcao);
        jMenuItemToJPG.setEnabled(opcao);
        jMenuItemToTXT.setEnabled(opcao);
        jMenuItemToSimGrid.setEnabled(opcao);
        jMenuItemToGridSim.setEnabled(opcao);
        //Editar
        jMenuItemEquiparar.setEnabled(opcao);
        jMenuItemOrganizar.setEnabled(opcao);
        jMenuItemCopy.setEnabled(opcao);
        jMenuItemPaste.setEnabled(opcao);
        jMenuItemDelete.setEnabled(opcao);
        //Exibir
        jCheckBoxMenuItemIndireto.setEnabled(opcao);
        jCheckBoxMenuItemConectado.setEnabled(opcao);
        jCheckBoxMenuItemEscalonavel.setEnabled(opcao);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent evt) {
        if (aDesenho != null) {
            if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                aDesenho.botaoIconeActionPerformed(null);
            }
            if (evt.getModifiers() == InputEvent.CTRL_MASK && evt.getKeyCode() == KeyEvent.VK_C) {
                aDesenho.botaoVerticeActionPerformed(null);
            }
            if (evt.getModifiers() == InputEvent.CTRL_MASK && evt.getKeyCode() == KeyEvent.VK_V) {
                aDesenho.botaoPainelActionPerformed(null);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void setSelectedIcon(ItemGrade icon, String Texto) {
        if (icon != null) {
            if (icon instanceof ispd.gui.iconico.grade.Machine || icon instanceof ispd.gui.iconico.grade.Cluster) {
                this.jPanelConfiguracao.setIcone(icon, aDesenho.getUsuarios(), tipoModelo);
            } else {
                this.jPanelConfiguracao.setIcone(icon);
            }
            jScrollPaneBarraLateral.setViewportView(jPanelConfiguracao);
            this.jPanelPropriedades.setjLabelTexto(Texto);
        } else {
            jScrollPaneBarraLateral.setViewportView(jPanelSimples);
            jPanelPropriedades.setjLabelTexto("");
        }
    }
}//public class JPrincipal jForm
