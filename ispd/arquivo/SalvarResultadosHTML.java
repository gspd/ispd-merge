package ispd.arquivo;

import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasGlobais;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

/**
 *
 * @author denison
 */
public class SalvarResultadosHTML {

    private String tabela;
    private String globais;
    private String satisfacao;
    private String tarefas;
    private String chartstxt;
    private BufferedImage charts[];

    public void setTabela(Object tabela[][]) {
        //Adicionando resultados na tabela do html
        this.tabela = "";
        for (Object[] item : tabela) {
            this.tabela += "<tr><td>" + item[0] + "</td><td>" + item[1] + "</td><td>" + item[2] + "</td><td>" + item[3] + "</td></tr>\n";
        }
    }

    public void setCharts(BufferedImage charts[]) {
        int cont = 0;
        for (BufferedImage item : charts) {
            if (item != null) {
                cont++;
            }
        }
        this.charts = new BufferedImage[cont];
        cont = 0;
        this.chartstxt = "";
        for (BufferedImage item : charts) {
            if (item != null) {
                this.charts[cont] = item;
                this.chartstxt += "<img alt=\"\" src=\"chart" + cont + ".png\" style=\"width: 600px; height: 300px;\" />\n";
                cont++;
            }
        }

    }

    public void setSatisfacao(Object[][] satisfacao) {
        if (satisfacao.length > 1) {
            this.satisfacao = "<ul>";
            for (int i = 0; i < satisfacao.length; i++) {
                this.satisfacao += "<li><strong>User" + satisfacao[i][0] + "</strong> = " + satisfacao[i][1] + " %</li>\n";
            }
            this.satisfacao += "</ul>";
        }
    }

    public void setMetricasGlobais(MetricasGlobais globais) {
        this.globais = "<li><strong>Total Simulated Time </strong>= " + globais.getTempoSimulacao() + "</li>\n";
        if (satisfacao == null) {
            this.globais += "<li><strong>Satisfaction</strong> = " + globais.getSatisfacaoMedia() + " %</li>\n";
        } else {
            this.globais += "<li><strong>Satisfaction</strong>" + satisfacao + "</li>\n";
        }
        this.globais += "<li><strong>Idleness of processing resources</strong> = " + globais.getOciosidadeComputacao() + " %</li>\n"
                + "<li><strong>Idleness of communication resources</strong> = " + globais.getOciosidadeComunicacao() + " %</li>\n"
                + "<li><strong>Efficiency</strong> = " + globais.getEficiencia() + " %</li>\n";
        if (globais.getEficiencia() > 70.0) {
            this.globais += "<li><span style=\"color:#00ff00;\"><strong>Efficiency GOOD</strong></span></li>\n";
        } else if (globais.getEficiencia() > 40.0) {
            this.globais += "<li><strong>Efficiency MEDIA</strong></li>\n ";
        } else {
            this.globais += "<li><span style=\"color:#ff0000;\"><strong>Efficiency BAD</strong></span></li>\n";
        }
    }

    public void setMetricasTarefas(Metricas metricas) {
        double tempoMedioSistemaComunicacao = metricas.getTempoMedioFilaComunicacao() + metricas.getTempoMedioComunicacao();
        double tempoMedioSistemaProcessamento = metricas.getTempoMedioFilaProcessamento() + metricas.getTempoMedioProcessamento();
        this.tarefas = "<ul><li><h2>Tasks</h2><ul><li><strong>Communication</strong><ul>\n"
                + "<li>Queue average time: " + metricas.getTempoMedioFilaComunicacao() + " seconds.</li>\n"
                + "<li>Communication average time: " + metricas.getTempoMedioComunicacao() + " seconds.</li>\n"
                + "<li>System average time: " + tempoMedioSistemaComunicacao + " seconds.</li>\n"
                + "</ul></li><li><strong>Processing</strong><ul>\n"
                + "<li>Queue average time: " + metricas.getTempoMedioFilaProcessamento() + " seconds.</li>\n"
                + "<li>Processing average time: " + metricas.getTempoMedioProcessamento() + " seconds.</li>\n"
                + "<li>System average time: " + tempoMedioSistemaProcessamento + " seconds.</li></ul></li></ul></li></ul>";
    }

    public String getHTMLText() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Simulation Results</title>\n"
                + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "    </head>\n"
                + "    <body background=\"fundo_html.jpg\" style=\"background-position: top center; background-repeat: no-repeat;\">\n"
                + "        <h1 id=\"topo\" style=\"text-align: center;\">\n"
                + "            <span style=\"color:#8b4513;\">\n"
                + "            <img alt=\"\" src=\"Logo_GSPD_100.jpg\" align=\"left\" style=\"width: 70px; height: 70px;\" />\n"
                + "            Simulation Results</span>\n"
                + "            <img alt=\"\" src=\"Logo_UNESP.jpg\" align=\"right\" style=\"width: 70px; height: 70px;\" />\n"
                + "        </h1>\n"
                + "        <hr /><br />\n"
                + "        <div>\n"
                + "            <a href=\"#global\">Global metrics</a> <br/>\n"
                + "            <a href=\"#table\">Table of Resource</a> <br/>\n"
                + "            <a href=\"#chart\">Charts</a> <br/>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"global\" style=\"text-align: center;\">\n"
                + "            Global metrics</h2>\n"
                + "        " + globais + tarefas
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"table\" style=\"text-align: center;\">\n"
                + "            Table of Resource\n"
                + "        </h2>\n"
                + "        <table align=\"center\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\" style=\"width: 80%;\">\n"
                + "            <thead>\n"
                + "                <tr>\n"
                + "                    <th scope=\"col\">\n"
                + "                        <span style=\"color:#800000;\">Label</span></th>\n"
                + "                    <th scope=\"col\">\n"
                + "                        <span style=\"color:#800000;\">Owner</span></th>\n"
                + "                    <th scope=\"col\">\n"
                + "                        <span style=\"color:#800000;\">Processing performed</span></th>\n"
                + "                    <th scope=\"col\">\n"
                + "                        <span style=\"color:#800000;\">Communication&nbsp;performed</span></th>\n"
                + "                </tr>\n"
                + "            </thead>\n"
                + "            <tbody>\n"
                + "                " + tabela
                + "            </tbody>\n"
                + "        </table>\n"
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <h2 id=\"chart\" style=\"text-align: center;\">\n"
                + "            Charts\n"
                + "        </h2>\n"
                + "        <p style=\"text-align: center;\">\n"
                + "        " + chartstxt
                + "        </p>\n"
                + "        <div>\n"
                + "            <a href=\"#topo\">Inicio</a>\n"
                + "        </div>\n"
                + "        <hr />\n"
                + "        <p style=\"font-size:10px;\">\n"
                + "            <a href=\"http://gspd.dcce.ibilce.unesp.br/\">GSPD</a></p>\n"
                + "    </body>\n"
                + "</html>";
    }

    public void gerarHTML(File diretorio) throws IOException {
        if (!diretorio.exists()) {
            if (!diretorio.mkdir()) {
                throw new IOException("Could not create directory");
            }
        }
        File arquivo = new File(diretorio, "result.html");
        FileWriter writer;
        writer = new FileWriter(arquivo);
        PrintWriter saida = new PrintWriter(writer, true);
        saida.print(this.getHTMLText());
        saida.close();
        writer.close();
        for (int i = 0; i < charts.length; i++) {
            arquivo = new File(diretorio, "chart" + i + ".png");
            ImageIO.write(charts[i], "png", arquivo);
        }
        arquivo = new File(diretorio, "fundo_html.jpg");
        if (!arquivo.exists()) {
            ImageIO.write(getImagem("fundo_html.jpg"), "jpg", arquivo);
        }
        arquivo = new File(diretorio, "Logo_GSPD_100.jpg");
        if (!arquivo.exists()) {
            ImageIO.write(getImagem("Logo_GSPD_100.jpg"), "jpg", arquivo);
        }
        arquivo = new File(diretorio, "Logo_UNESP.jpg");
        if (!arquivo.exists()) {
            ImageIO.write(getImagem("Logo_UNESP.jpg"), "jpg", arquivo);
        }
    }

    private RenderedImage getImagem(String img) throws IOException {
        return ImageIO.read(ispd.gui.JPrincipal.class.getResource("imagens/" + img));
    }
}