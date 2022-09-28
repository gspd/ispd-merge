package ispd.arquivo.xml;

import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaList;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaTrace;
import ispd.motor.carga.GerarCarga;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoadBuilder {
    static GerarCarga buildLoad(Document modelo) {
        NodeList cargas = modelo.getElementsByTagName("load");
        GerarCarga cargasConfiguracao = null;
        //Realiza leitura da configuração de carga do modelo
        if (cargas.getLength() != 0) {
            final Element cargaAux = (Element) cargas.item(0);
            cargas = cargaAux.getElementsByTagName("random");
            if (cargas.getLength() != 0) {
                final Element carga = (Element) cargas.item(0);
                final int numeroTarefas = Integer.parseInt(carga.getAttribute(
                        "tasks"));
                final int timeOfArrival = Integer.parseInt(carga.getAttribute(
                        "time_arrival"));
                int minComputacao = 0;
                int maxComputacao = 0;
                int AverageComputacao = 0;
                double ProbabilityComputacao = 0;
                int minComunicacao = 0;
                int maxComunicacao = 0;
                int AverageComunicacao = 0;
                double ProbabilityComunicacao = 0;
                final NodeList size = carga.getElementsByTagName("size");
                for (int i = 0; i < size.getLength(); i++) {
                    final Element size1 = (Element) size.item(i);
                    if ("computing".equals(size1.getAttribute("type"))) {
                        minComputacao = Integer.parseInt(size1.getAttribute(
                                "minimum"));
                        maxComputacao = Integer.parseInt(size1.getAttribute(
                                "maximum"));
                        AverageComputacao =
                                Integer.parseInt(size1.getAttribute("average"));
                        ProbabilityComputacao =
                                Double.parseDouble(size1.getAttribute(
                                        "probability"));
                    } else if ("communication".equals(
                            size1.getAttribute("type"))) {
                        minComunicacao = Integer.parseInt(size1.getAttribute(
                                "minimum"));
                        maxComunicacao = Integer.parseInt(size1.getAttribute(
                                "maximum"));
                        AverageComunicacao =
                                Integer.parseInt(size1.getAttribute("average"));
                        ProbabilityComunicacao =
                                Double.parseDouble(size1.getAttribute(
                                        "probability"));
                    }
                }
                cargasConfiguracao = new CargaRandom(numeroTarefas,
                        minComputacao, maxComputacao, AverageComputacao,
                        ProbabilityComputacao, minComunicacao, maxComunicacao
                        , AverageComunicacao, ProbabilityComunicacao,
                        timeOfArrival);
            }
            cargas = cargaAux.getElementsByTagName("node");
            if (cargas.getLength() != 0) {
                final List<CargaForNode> tarefasDoNo =
                        new ArrayList<CargaForNode>();
                for (int i = 0; i < cargas.getLength(); i++) {
                    final Element carga = (Element) cargas.item(i);
                    final String aplicacao = carga.getAttribute("application");
                    final String proprietario = carga.getAttribute("owner");
                    final String escalonador = carga.getAttribute("id_master");
                    final int numeroTarefas =
                            Integer.parseInt(carga.getAttribute(
                                    "tasks"));
                    double minComputacao = 0;
                    double maxComputacao = 0;
                    double minComunicacao = 0;
                    double maxComunicacao = 0;
                    final NodeList size = carga.getElementsByTagName("size");
                    for (int j = 0; j < size.getLength(); j++) {
                        final Element size1 = (Element) size.item(j);
                        if ("computing".equals(size1.getAttribute("type"))) {
                            minComputacao =
                                    Double.parseDouble(size1.getAttribute(
                                            "minimum"));
                            maxComputacao =
                                    Double.parseDouble(size1.getAttribute(
                                            "maximum"));
                        } else if ("communication".equals(
                                size1.getAttribute("type"))) {
                            minComunicacao =
                                    Double.parseDouble(size1.getAttribute(
                                            "minimum"));
                            maxComunicacao =
                                    Double.parseDouble(size1.getAttribute(
                                            "maximum"));
                        }
                    }
                    final CargaForNode item = new CargaForNode(aplicacao,
                            proprietario, escalonador, numeroTarefas,
                            maxComputacao, minComputacao, maxComunicacao,
                            minComunicacao);
                    tarefasDoNo.add(item);
                }
                cargasConfiguracao = new CargaList(tarefasDoNo,
                        GerarCarga.FORNODE);
            }
            final NodeList trace = cargaAux.getElementsByTagName("trace");
            if (trace.getLength() != 0) {
                final Element carga = (Element) trace.item(0);
                final File filepath = new File(carga.getAttribute("file_path"));
                final int taskCount = Integer.parseInt(carga.getAttribute(
                        "tasks"));
                final String formato = carga.getAttribute("format");
                if (filepath.exists()) {
                    cargasConfiguracao = new CargaTrace(filepath, taskCount
                            , formato);
                }
            }
        }
        return cargasConfiguracao;
    }
}